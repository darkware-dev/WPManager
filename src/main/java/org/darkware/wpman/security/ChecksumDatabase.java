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
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * A {@code ChecksumDatabase} is a threadsafe collection of file {@code Path Paths} and the most
 * recently accepted hashes of their contents. This is used as a comparison against the current
 * state of the files on the filesystem. It supports registering directories to be temporarily
 * excluded from checks, though it is up to the scanning code to check each path.
 * <p>
 * The state of the database can be read from and written to a file on the local filesystem. The format
 * is simple, but is not expected to adhere to normal checksum file lists.
 * <p>
 * The database is intended to operate in a concurrent environment with multiple threads reading and
 * writing entries at any time.
 *
 * @author jeff
 * @since 2016-02-25
 */
public class ChecksumDatabase
{
    /** A shared {@link Logger} for code that performs actions on the database. */
    protected static final Logger log = LoggerFactory.getLogger("Integrity");

    private final Path root;
    private final Map<Path, String> hashes;
    private final Set<Path> suppressed;
    private final Path dbFile;
    private final AtomicBoolean initialized;
    private final ReadWriteLock lock;

    /**
     * Create a new {@code ChecksumDatabase} for files under the declared root and using the
     * given file for saved state.
     *
     * @param dbFile The file to load and store the database data into.
     * @param root  The highest level directory represented in the database.
     */
    public ChecksumDatabase(final Path dbFile, final Path root)
    {
        super();

        this.root = root;
        this.dbFile = dbFile;
        this.hashes = new ConcurrentSkipListMap<>();
        this.suppressed = new ConcurrentSkipListSet<>();
        this.initialized = new AtomicBoolean(false);
        this.lock = new ReentrantReadWriteLock();

        this.initialize();
    }

    /**
     * Fetch the set of database entries that exist as descendants of the given base directory.
     *
     * @param baseDir The base directory to fetch.
     * @return A {@code Set} of {@code Path Paths} which are strict descendants of the given root path.
     */
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

    /**
     * Load the database from the attached file. The file path is set in the constructor.
     */
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

    /**
     * Write the database to the attached file path. This path is declared in the constructor and cannot be
     * changed.
     */
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

    /**
     * Initialize this database. This sets various internal state, including a preliminary read of the
     * storage file, if it exists, or an initial scan of the root directory if it does not.
     */
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

    /**
     * Normalize the given {@code Path} so that internal {@code Path} operations don't have to think about
     * absolute/relative comparisons or paths that don't represent concrete files.
     *
     * @param file The {@code Path} to normalize.
     * @return An absolute {@code Path} to a concrete file which is a descendant of the database root.
     * @throws IllegalArgumentException If the path does not exist, was not a path to a regular file, or pointed
     * to a file that was outside the database root.
     */
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

    /**
     * Check to see if the given file has a checksum in the database.
     *
     * @param file The file to check.
     * @return {@code true} if a checksum exists, {@code false} if no entry exists.
     */
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

    /**
     * Check to see if the checksum of the given file matches the checksum in the database. This implicitly
     * includes a call to {@link #normalize(Path)} and {@link #hasChecksum(Path)}.
     *
     * @param file The file to check.
     * @return {@code true} if the checksum of the file matches the database checksum, {@code false} if the
     * checksum is different, or if no checksum existed.
     */
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

    /**
     * Update the checksum for the given file using the current file contents.
     *
     * @param file The file to update the checksum for.
     */
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

    /**
     * Remove a given {@code Path} from the database.
     * <p>
     * This does not change the underlying filesystem. If the file still exists then it is very likely that
     * it will be added back to the database during the next integrity or update scan. While this may seem like
     * a good way to force a database update, it's far easier and more consistent to use {@link #update(Path)}.
     *
     * @param file The file {@code Path} to remove.
     */
    public void remove(final Path file)
    {
        this.lock.writeLock().lock();
        try
        {
            this.hashes.remove(file);
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Remove a {@code Collection} of files from the database. This will remove all {@code Paths} in the
     * supplied {@code Collection} from the database.
     * <p>
     * <em>Note:</em> Validity checks on the file paths are largely ignored to ensure that a best effort is
     * made to remove all paths that might be in the collection.
     *
     * @param files
     */
    public void removeAll(final Collection<Path> files)
    {
        this.lock.writeLock().lock();
        try
        {
            files.forEach(this.hashes::remove);
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Remove all database entries that are descendants of a given path.
     * <p>
     * <em>Note:</em> The method won't throw an exception if the given path does not point to a file, however
     * this will only result in possibly removing that file.
     *
     * @param dir A {@code Path} to a directory contained by the database.
     * @throws IllegalArgumentException If the {@code Path} points to a location outside the database root.
     */
    public void removeDirectory(final Path dir)
    {
        this.lock.writeLock().lock();
        try
        {
            this.hashes.entrySet().removeIf(e -> e.getKey().startsWith(dir));
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Add an advisory entry for paths which are undergoing changes and should not be reported. This
     * does not change any internal behavior and {@link #check(Path)} will still operate against the most
     * recently calculated checksums. However, code which uses the database <em>should</em> check for
     * suppressed entries while reporting results.
     *
     * @param path The path to suppress.
     */
    public void suppress(final Path path)
    {
        this.suppressed.add(path);
    }

    /**
     * Checks to see if a given file is suppressed. Files are suppressed if either their exact path has been
     * added to the suppressed list or if any of their parent directories are added.
     *
     * @param file The file to check.
     * @return {@code true} if the file should be suppressed, otherwise {@code false}.
     */
    public boolean isSuppressed(final Path file)
    {
        return (this.suppressed.stream().filter(file::startsWith).count() > 0);
    }

    /**
     * Remove the suppression advisory for the given path.
     *
     * @param path The path to clear suppression on.
     */
    public void unsuppress(final Path path)
    {
        this.suppressed.remove(path);
    }

    /**
     * Calculate a checksum on the given file.
     *
     * @param file The file to calculate a checksum for.
     * @return The checksum as a {@code String}.
     * @see #doChecksum(ReadableByteChannel)
     */
    protected String calculateChecksum(final Path file)
    {
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ))
        {
            return this.doChecksum(channel);
        }
        catch (IOException e)
        {
            ChecksumDatabase.log.error("Failed to calculate checksum on {}: {}", file, e.getLocalizedMessage());
            return "";
        }
    }

    /**
     * Perform a checksum calculation on the given {@link ReadableByteChannel}. Other code should not create
     * implementations which are dependant on any particular characteristics of the checksum, but the checksum
     * is very likely to be based on a cryptographic-strength hash. The results of the checksum are encoded as
     * a base64 {@code String}.
     *
     * @param channel The {@code ReadableByteChannel} to read data from.
     * @return A Base64 encoded {@code String} representing the checksum.
     * @throws IOException If there was an error while reading data from the channel.
     * @see Base64#encodeBase64String(byte[])
     */
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
