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

package org.darkware.wpman.agents;

import org.darkware.wpman.ContextManager;
import org.darkware.wpman.WPManager;
import org.darkware.wpman.config.WordpressConfig;
import org.darkware.wpman.events.InstallationFileChange;
import org.darkware.wpman.security.ChecksumDatabase;
import org.darkware.wpman.security.DirectoryScanner;
import org.darkware.wpman.security.ScanResults;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;

/**
 * The {@code WPIntegrityCheckAgent} is an {@link WPAgent} which performs periodic checks of the files
 * in the WordPress instance.
 * <p>
 * The agent is primarily a driver for the {@link DirectoryScanner} and {@link ChecksumDatabase} classes. In
 * the case where changed files are found, it dispatches an {@link InstallationFileChange} event.
 *
 * @author jeff
 * @since 2016-03-08
 */
public class WPIntegrityCheckAgent extends WPPeriodicAgent
{
    private final WordpressConfig config;
    private final ChecksumDatabase checksums;

    /**
     * Create a new {@code WPIntegrityCheckAgent}.
     */
    public WPIntegrityCheckAgent()
    {
        super("integrity", Duration.ofMinutes(30));

        this.config = ContextManager.local().getContextualInstance(WordpressConfig.class);
        if (ContextManager.local().has(ChecksumDatabase.class))
        {
            this.checksums = ContextManager.local().getContextualInstance(ChecksumDatabase.class);
        }
        else
        {
            this.checksums = new ChecksumDatabase(this.config.getDataFile("integrityDb"), this.config.getBasePath());
            ContextManager.local().registerInstance(this.checksums);
        }
    }

    @Override
    public void executeAction()
    {
        DirectoryScanner scanner = new DirectoryScanner(this.config.getBasePath(), this.checksums);
        scanner.updateChecksums(true);
        ScanResults results = scanner.scan();

        Set<Path> newFiles = results.getNewFiles();
        Set<Path> changedFiles = results.getChangedFiles();
        Set<Path> missingFiles = results.getMissingFiles();

        if (results.foundChanges())
        {
            WPManager.log.info("Integrity Scan: New files found: {}", newFiles.size());
            WPManager.log.info("Integrity Scan: Changed files found: {}", changedFiles.size());
            WPManager.log.info("Integrity Scan: Missing files found: {}", missingFiles.size());

            changedFiles.stream().forEach(p -> WPManager.log.warn("File changed: {}", p));
            newFiles.stream().forEach(p -> WPManager.log.warn("New file found: {}", p));

            // Dispatch an event
            this.getManager().dispatchEvent(new InstallationFileChange(results));

            // Write the new state of the database
            this.checksums.writeDatabase();
        }
    }
}
