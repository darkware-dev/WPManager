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

package org.darkware.wpman.actions;

import org.darkware.cltools.utils.FileSystemTools;
import org.darkware.wpman.ContextManager;
import org.darkware.wpman.WPManager;
import org.darkware.wpman.config.UpdatableConfig;
import org.darkware.wpman.data.WPUpdatableComponent;
import org.darkware.wpman.data.WPUpdatableType;
import org.darkware.wpman.events.WPInstallEvent;
import org.darkware.wpman.security.ChecksumDatabase;
import org.darkware.wpman.security.DirectoryScanner;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIFlag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The {@code WPItemAutoInstall} is an abstract implementation of an {@link WPAction} that can perform
 * installations and upgrades of updatable components (Themes and Plugins) with a respectable level of
 * perseverance in the face of predictable errors. The actions are integrated with a
 * {@link ChecksumDatabase} from the local context in order to suppress file change warnings during
 * the auto-install and to recalculate checksums after the installation is complete.
 * <p>
 * In the majority of cases, installations and upgrades are simple, one-step processes. However, rare
 * problems can result in failures that may result in disabled items, possibly with file system entries
 * that prevent future attempts at fixing them. This action attempts to auto-detect problems and take
 * appropriate actions to remedy them as soon as they are detected.
 * <p>
 * The classic situation is a failed upgrade. If an upgrade fails for whatever reason, one of the best
 * remedies is to immediately attempt to perform a reinstall of the item.
 * <p>
 * This action takes the above logic and merges into a single decision tree that handles both installation
 * and update requests, regardless of the initial state of the item being targeted. If the item is
 * recognized by WordPress, its updated. If not, it is installed. Following that action, the item's
 * existence is checked again. If it still doesn't exist, an install action is forced, hopefully replacing
 * the failed files. Further checks are done, and a final judgement on the success of the action is
 * returned.
 *
 * @author jeff
 * @since 2016-02-14
 */
public abstract class WPItemAutoInstall<T extends WPUpdatableComponent> extends WPBasicAction<Boolean>
{
    /** The item ID which declares which item is being installed. */
    protected final String installToken;
    /** The type of WordPress item being installed. */
    protected final WPUpdatableType itemType;
    /** The {@code ChecksumDatabase} tracking the file changes for this item. */
    protected final ChecksumDatabase checksums;

    /**
     * Create a new {@code WPItemAutoInstall} instance.
     *
     * @param itemType The type of item to install.
     * @param installToken The ID of the item to install.
     */
    public WPItemAutoInstall(final WPUpdatableType itemType, final String installToken)
    {
        super(WPActionCategory.INSTALL);

        this.installToken = installToken;
        this.itemType = itemType;
        this.checksums = ContextManager.local().getContextualInstance(ChecksumDatabase.class);
    }

    /**
     * Fetch the {@link WPUpdatableType} of the item which is being installed.
     *
     * @return The item type, as a {@code WPUpdatableType}.
     */
    public final WPUpdatableType getItemType()
    {
        return this.itemType;
    }

    @Override
    public String getDescription()
    {
        return "Auto-install " + this.getItemType() + ": " + this.installToken;
    }

    @Override
    public Boolean exec()
    {
        try
        {
            final UpdatableConfig config = this.getConfig();

            // Check to see if the item is already installed.
            T preInstall = this.getItem();

            if (preInstall != null)
            {
                // Do not attempt to update if the config disallows it.
                if (!config.isUpdatable())
                {
                    WPManager.log.info("Skipping updates to {}: {}. Disallowed by config", this.getItemType(), this.installToken, preInstall.getVersion());
                    return false;
                }

                // Attempt an update
                if (preInstall.hasUpdate())
                {
                    WPManager.log.info("Updating {}: {} ({})", this.getItemType(), this.installToken, preInstall.getVersion());
                    this.doUpdate();
                }
                else
                {
                    WPManager.log.info("The {} '{}' is already installed and up to date. ({}))", this.getItemType(), this.installToken,
                                       preInstall.getVersion());
                    return true;
                }
            }
            else
            {
                // Do not attempt to install if the config disallows it
                if (!config.isInstallable())
                {
                    WPManager.log.info("Skipping installation of {}: {}. Disallowed by config", this.getItemType(), this.installToken, preInstall.getVersion());
                    return false;
                }

                // Attempt an install
                WPManager.log.info("Installing {}: {}", this.getItemType(), this.installToken);
                this.doInstall();
            }

            // Check to see if the item install succeeded.
            WPManager.log.info("Checking {}: {}", this.getItemType(), this.installToken);
            T postInstall = this.getItem();
            if (postInstall == null)
            {
                WPManager.log.warn("The {} '{}' was not found after installation. Forcing a reinstall.", this.getItemType(), this.installToken);
                this.doInstall();
                postInstall = this.getItem();
            }
            else if (preInstall != null && postInstall.getVersion().equals(preInstall.getVersion()))
            {
                WPManager.log.info("Looking for {} v{} (was v{}, is v{}).", preInstall.getId(), preInstall.getUpdateVersion(), preInstall.getVersion(), postInstall.getVersion());
                WPManager.log.warn("The {} '{}' didn't register a version change. Forcing a reinstall.", this.getItemType(), this.installToken);
                this.doInstall();
                postInstall = this.getItem();
            }
            else
            {
                WPManager.log.info("Successfully installed {}: {} {}.", this.getItemType(), this.installToken, postInstall.getVersion());
            }

            // Send out an event to let everyone know we updated stuff
            if (preInstall == null)
            {
                this.getManager().dispatchEvent(WPInstallEvent.create(postInstall));
            }
            else
            {
                this.getManager().dispatchEvent(WPInstallEvent.create(postInstall, preInstall.getVersion()));
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            WPManager.log.warn("Error while auto-installing {} '{}' : {} : {}", this.getItemType(), this.installToken, t.getClass().getName(), t.getLocalizedMessage());
            return false;
        }

        return true;
    }

    /**
     * Perform a standard item update. This attempts to do a simple update of the item with no checking or
     * recovery. Those actions should be taken by calling code since the types of checking and recovery might
     * differ depending on the situation. This method does properly suppress file change reports while files are
     * changing.
     */
    protected void doUpdate()
    {
        try
        {
            // Suppress change notices on the item directory
            this.checksums.suppress(this.getItemDirectory());

            WPCLI update = this.getWPCWpcliFactory().build(this.getItemType().getToken(), "update", this.installToken);

            if (update.checkSuccess())
            {
                WPManager.log.info("Installed {}: {}", this.getItemType(), this.installToken);
            }
            else
            {
                WPManager.log.warn("Failed to update {}: {}", this.getItemType(), this.installToken);
            }

        }
        finally
        {
            this.expireItemContainer();
            this.checksums.unsuppress(this.getItemDirectory());
        }
    }

    /**
     * Perform a standard item install. This attempts to do a forced install of the item with no checking or
     * recovery. Those actions should be taken by calling code since the types of checking and recovery might
     * differ depending on the situation. This method does properly suppress file change reports while files are
     * changing.
     */
    protected void doInstall()
    {
        try
        {
            // Suppress change notices on the item directory
            this.checksums.suppress(this.getItemDirectory());

            // Move the existing directory, if it does exist
            if (Files.exists(this.getItemDirectory()))
            {
                try
                {
                    Path gutterDir = this.getGutterDirectory().resolve(this.installToken);
                    if (Files.exists(gutterDir)) FileSystemTools.deleteTree(gutterDir);
                    Files.move(this.getItemDirectory(), gutterDir);
                }
                catch (IOException e)
                {
                    WPManager.log.warn("Error while trying to move colliding directory to the gutter: {} : {}",
                                       this.getItemDirectory(), e.getLocalizedMessage());
                }
            }

            // Run an install
            WPCLI update = this.getWPCWpcliFactory().build(this.getItemType().getToken(), "install", this.installToken);
            update.loadThemes(false);
            update.loadPlugins(false);
            update.setOption(new WPCLIFlag("force"));

            if (update.checkSuccess())
            {
                WPManager.log.info("Installed {}: {}", this.getItemType(), this.installToken);
            }
            else
            {
                WPManager.log.warn("Failed to update {}: {}", this.getItemType(), this.installToken);
            }
        }
        finally
        {
            this.expireItemContainer();
            this.checksums.unsuppress(this.getItemDirectory());
        }
    }

    /**
     * Update the checksums for files associated with the named item.
     */
    protected void updateChecksums()
    {
        DirectoryScanner scanner = new DirectoryScanner(this.getItemDirectory(), this.checksums);
        scanner.updateChecksums(true);
        scanner.scan();
    }

    /**
     * Fetch the path where this item will be stored on the filesystem. This directory may or may
     * not exist.
     *
     * @return An absolute path to the storage location for this item.
     */
    protected Path getItemDirectory()
    {
        return this.getContainerPath().resolve(this.installToken);
    }

    /**
     * Fetch the directory to store discarded directories. These are usually directories that collide
     * with intended target directories.
     *
     * @return An absolute path to the gutter directory for the current item type.
     * @throws IOException if there is a problem moving files to the gutter directory.
     */
    protected Path getGutterDirectory() throws IOException
    {
        Path gutter = this.getManager().getConfig().getUpdatableCollection(this.getItemType()).getGutterDir();
        if (!Files.exists(gutter)) Files.createDirectories(gutter);
        return gutter;
    }

    /**
     * Fetch the {@link Path} to the directory containing the item to install or update.
     *
     * @return An absolute path to the containing folder.
     */
    protected abstract Path getContainerPath();

    /**
     * Attempt to retrieve a current-state data record for the item being worked with.
     *
     * @return A record of the currently installed item or {@code null} if the item is not
     * currently installed.
     */
    protected abstract T getItem();

    /**
     * Attempt to retrieve configuration for the targeted item.
     *
     * @return An {@link UpdatableConfig} object.
     */
    protected abstract UpdatableConfig getConfig();

    /**
     * Expire the {@code WPData} container which holds records for this item. This should be
     * done after any attempt to change the component, as it will trigger a data refresh the
     * next time the data is needed.
     */
    protected abstract void expireItemContainer();
}
