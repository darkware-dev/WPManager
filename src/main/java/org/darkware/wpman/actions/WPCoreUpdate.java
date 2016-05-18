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

import org.darkware.wpman.WPManager;
import org.darkware.wpman.data.Version;
import org.darkware.wpman.data.WPBlog;
import org.darkware.wpman.events.WPCoreUpdateEvent;
import org.darkware.wpman.wpcli.WPCLI;

/**
 * @author jeff
 * @since 2016-04-10
 */
public class WPCoreUpdate extends WPBasicAction<Boolean>
{
    private final Version originalVersion;
    private final Version updateTargetVersion;

    public WPCoreUpdate()
    {
        super(WPActionCategory.INSTALL);

        this.originalVersion = this.getManager().getData().getCore().getCoreVersion();
        this.updateTargetVersion = this.getManager().getData().getCore().getUpdateVersion();
    }

    @Override
    public String getDescription()
    {
        return "Updating WordPress to v" + this.updateTargetVersion;
    }

    @Override
    public Boolean exec()
    {
        if (this.getManager().getData().getCore().hasUpdate())
        {
            WPCLI update = this.getManager().getBuilder().build("core", "update");
            update.loadThemes(false);
            update.loadPlugins(false);

            // Run the update
            update.execute();

            // Expire the core data.
            this.getManager().dispatchEvent(new WPCoreUpdateEvent());

            // Check that the update happened.
            Version newVersion = this.getManager().getData().getCore().getCoreVersion();
            if (newVersion.equals(this.originalVersion))
            {
                WPManager.log.warn("WordPress update appears to have not happened.");
                return false;
            }
            else if (!newVersion.equals(this.updateTargetVersion))
            {
                WPManager.log.warn("WordPress updated to unexpected version: {} (expected {})", newVersion, this.updateTargetVersion);
                /*
                 * Even though we didn't get the version we expected, the core version changed so we should at least
                 * let the database updates go through to avoid other weirdness.
                 */
            }
            else
            {
                // Announce our success
                WPManager.log.info("Updated WordPress core to {}", newVersion);
            }

            for (WPBlog blog : this.getManager().getData().getBlogs())
            {
                final WPDatabaseUpdate updateAction = new WPDatabaseUpdate(blog);
                this.getManager().scheduleAction(updateAction);
            }

            return true;
        }
        else
        {
            WPManager.log.warn("No WordPress core update available.");
            return false;
        }
    }
}
