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

package org.darkware.wpman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author jeff
 * @since 2016-02-09
 */
public class ConfigWatcher extends Thread
{
    /** A shared logger for any ConfigWatcher logging requirements. */
    protected static Logger log = LoggerFactory.getLogger("ConfigWatcher");

    private final Path rootDir;
    private final AtomicBoolean enabled;

    public ConfigWatcher(final Path configDir)
    {
        super();

        this.rootDir = configDir;
        this.enabled = new AtomicBoolean(true);
    }

    @Override
    public void run()
    {
        try (final WatchService watcher = FileSystems.getDefault().newWatchService())
        {
            final WatchKey key = this.rootDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

            while(this.enabled.get())
            {
                final WatchKey watchKey = watcher.take();
                for (WatchEvent<?> event : watchKey.pollEvents())
                {
                    // Grab the event kind
                    WatchEvent.Kind<?> kind = event.kind();

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();

                    if (kind == StandardWatchEventKinds.OVERFLOW)
                    {
                        Thread.yield();
                        continue;
                    }
                    else if (kind == StandardWatchEventKinds.ENTRY_MODIFY)
                    {
                        this.fireChange(filename);
                    }
                }

                // Reset
                boolean valid = watchKey.reset();
                if (!valid) break;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    protected void fireChange(final Path changedFile)
    {
        ConfigWatcher.log.info("Noticed file change: {}", changedFile);
    }
}
