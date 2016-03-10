/*******************************************************************************
 * Copyright (c) 2016. darkware.org and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.darkware.wpman.security;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * @author jeff
 * @since 2016-02-25
 */
public class ChecksumDatabase
{
    protected static final Logger log = LoggerFactory.getLogger("Integrity");

    private final Path root;
    private final Map<Path, String> hashes;
    private final Path dbFile;
    private final AtomicBoolean initialized;
    private final ReadWriteLock lock;

    public ChecksumDatabase(final Path dbFile, final Path root)
    {
        super();

        this.root = root;
        this.dbFile = dbFile;
        this.hashes = new ConcurrentSkipListMap<>();
        this.initialized = new AtomicBoolean(false);
        this.lock = new ReentrantReadWriteLock();

        this.initialize();
    }

    public Set<Path> entriesForPath(final Path baseDir)
    {
        this.lock.readLock().lock();
        try
        {
            return this.hashes.keySet().stream().filter(p -> p.startsWith(baseDir)).collect(Collectors.toSet());
        }
        finally
        {
            this.lock.readLock().unlock();
        }
    }

    public void loadDatabase()
    {
        this.lock.writeLock().lock();
        try
        {
            this.hashes.clear();

            ChecksumDatabase.log.info("Reading integrity database: {}", this.dbFile);
            try (BufferedReader db = Files.newBufferedReader(this.dbFile, StandardCharsets.UTF_8))
            {
                String line;
                while ((line = db.readLine()) != null)
                {
                    int sep = line.lastIndexOf(':');
                    if (sep == -1) continue;

                    String path = line.substring(0, sep);
                    String checksum = line.substring(sep + 1);

                    this.hashes.put(Paths.get(path), checksum);
                }
                ChecksumDatabase.log.info("Read {} entries into the integrity database", this.hashes.size());
            }
            catch (IOException e)
            {
                ChecksumDatabase.log.error("Error while loading integrity database: {}", e.getLocalizedMessage(), e);
            }
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
    }

    public void writeDatabase()
    {
        this.lock.writeLock().lock();
        try
        {
            ChecksumDatabase.log.info("Writing integrity database: {}", this.dbFile);
            try (BufferedWriter db = Files.newBufferedWriter(this.dbFile, StandardCharsets.UTF_8,
                                                             StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                                                             StandardOpenOption.TRUNCATE_EXISTING))
            {
                for (Map.Entry<Path, String> entry : this.hashes.entrySet())
                {
                    db.write(entry.getKey().toString());
                    db.write(":");
                    db.write(entry.getValue());
                    db.newLine();
                }
            }
            catch (IOException e)
            {
                ChecksumDatabase.log.error("Error while writing integrity database: {}", e.getLocalizedMessage(), e);
            }
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
    }

    public void initialize()
    {
        if (this.initialized.compareAndSet(false, true))
        {
            this.lock.writeLock().lock();
            try
            {
                if (Files.exists(this.dbFile))
                {
                    // TODO: Do some checks for readability
                    // Load the file
                    this.loadDatabase();
                }
                else
                {
                    // Initialize the file
                    DirectoryScanner scanner = new DirectoryScanner(this.root, this);
                    scanner.updateChecksums(true);
                    scanner.scan();

                    this.writeDatabase();
                }
            }
            finally
            {
                this.lock.writeLock().unlock();
            }
        }
    }

    protected Path normalize(final Path file)
    {
        Path normalized = file;

        if (normalized.isAbsolute())
        {
            if (!file.startsWith(this.root)) throw new IllegalArgumentException("The given file is outside the database root path.");
        }
        else
        {
            normalized = this.root.resolve(normalized);
        }

        if (Files.notExists(normalized)) throw new IllegalArgumentException("The given file does not exist.");
        if (!Files.isRegularFile(normalized)) throw new IllegalArgumentException("The given path is not a file.");

        return normalized;
    }

    public boolean hasChecksum(final Path file)
    {
        this.lock.readLock().lock();
        try
        {
            return this.hashes.containsKey(this.normalize(file));
        }
        finally
        {
            this.lock.readLock().unlock();
        }
    }

    public boolean check(final Path file)
    {
        this.lock.readLock().lock();
        try
        {
            Path checkFile = this.normalize(file);
            if (this.hasChecksum(checkFile))
            {
                String checksum = this.calculateChecksum(checkFile);
                String expect = this.hashes.get(checkFile);
                return (checksum.equals(expect));
            }
            else return false;
        }
        finally
        {
            this.lock.readLock().unlock();
        }
    }

    public void update(final Path file)
    {
        this.lock.writeLock().lock();
        try
        {
            String checksum = this.calculateChecksum(file);
            this.hashes.put(file, checksum);
            ChecksumDatabase.log.debug("Updated checksum: {} = {}", file, checksum);
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
    }

    protected String calculateChecksum(final Path file)
    {
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ))
        {
            return this.doChecksum(channel);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            //TODO: Write a log message instead
            return "";
        }
    }

    protected String doChecksum(ReadableByteChannel channel) throws IOException
    {
        Hasher hasher = Hashing.sha256().newHasher();

        final ByteBuffer block = ByteBuffer.allocate(4096);
        while (channel.isOpen())
        {
            int bytesRead = channel.read(block);
            if (bytesRead > 0)
            {
                block.flip();
                hasher.putBytes(block.array(), 0, block.limit());
                block.clear();
            }
            else if (bytesRead == -1)
            {
                channel.close();
            }
        }

        return Base64.encodeBase64String(hasher.hash().asBytes());
    }
}
