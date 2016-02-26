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

import com.sun.istack.internal.Nullable;
import org.darkware.cltools.utils.FileSystemTools;
import org.darkware.wpman.Config;
import org.darkware.wpman.WPManager;
import org.darkware.wpman.data.WPUpdatableComponent;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIFlag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author jeff
 * @since 2016-02-14
 */
public abstract class WPItemAutoInstall<T extends WPUpdatableComponent> extends WPBasicAction
{
    protected final String installToken;
    protected final String itemType;

    public WPItemAutoInstall(final String itemType, final String installToken)
    {
        super();
        this.installToken = installToken;
        this.itemType = itemType;
    }

    public String getItemType()
    {
        return itemType;
    }

    @Override
    public String getDescription()
    {
        return "Auto-install " + this.getItemType() + ": " + this.installToken;
    }

    @Override
    public void run()
    {
        try
        {
            // Check to see if the item is already installed.
            T preInstall = this.getItem();

            if (preInstall != null)
            {
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
                    return;
                }
            }
            else
            {
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
            }
            else if (preInstall != null && postInstall.getVersion().equals(preInstall.getVersion()))
            {
                WPManager.log.warn("The {} '{}' didn't register a version change. Forcing a reinstall.", this.getItemType(), this.installToken);
                this.doInstall();
            }
            else
            {
                WPManager.log.info("Successfully installed {}: {} {}.", this.getItemType(), this.installToken, postInstall.getVersion());
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            WPManager.log.warn("Error while auto-installing {} '{}' : {} : {}", this.getItemType(), this.installToken, t.getClass().getName(), t.getLocalizedMessage());
        }
        finally
        {

        }
    }

    protected void doUpdate()
    {
        WPCLI update = this.getWPCWpcliFactory().build(this.getItemType(), "update", this.installToken);

        if (update.checkSuccess())
        {
            WPManager.log.info("Installed {}: {}", this.getItemType(), this.installToken);
        }
        else
        {
            WPManager.log.warn("Failed to update {}: {}", this.getItemType(), this.installToken);
        }

        this.expireItemContainer();
    }

    protected void doInstall()
    {
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
                WPManager.log.warn("Error while trying to move colliding directory to the gutter: {} : {}", this.getItemDirectory(), e.getLocalizedMessage());
            }
        }

        // Run an install
        WPCLI update = this.getWPCWpcliFactory().build(this.getItemType(), "install", this.installToken);
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

        this.expireItemContainer();
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
     */
    protected Path getGutterDirectory() throws IOException
    {
        Path gutter = this.getManager().getConfig().readPath(Config.buildKey("wp.gutter", this.getItemType()));
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
    protected abstract @Nullable T getItem();

    /**
     * Expire the {@code WPData} container which holds records for this item. This should be
     * done after any attempt to change the component, as it will trigger a data refresh the
     * next time the data is needed.
     */
    protected abstract void expireItemContainer();
}
