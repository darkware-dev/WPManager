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
import org.darkware.wpman.data.WPBlog;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIError;

/**
 * @author jeff
 * @since 2016-04-10
 */
public class WPDatabaseUpdate extends WPBasicAction<Boolean>
{
    private final WPBlog blog;

    public WPDatabaseUpdate(final WPBlog blog)
    {
        super(WPActionCategory.INSTALL);

        this.blog = blog;
    }

    @Override
    public String getDescription()
    {
        return "Updating database schema for blog: " + this.blog.getSubDomain();
    }

    @Override
    public Boolean exec()
    {
        try
        {
            WPCLI updateDb = this.getManager().getBuilder().build("core", "update-db");
            updateDb.loadPlugins(false);
            updateDb.loadThemes(false);
            updateDb.setBlog(this.blog);

            updateDb.execute();
            WPManager.log.info("Updated database for blog: {}", this.blog.getSubDomain());

            return true;
        }
        catch (WPCLIError error)
        {
            WPManager.log.error("Error updating core database for {}: {}", this.blog.getSubDomain(), error.getLocalizedMessage());

            return false;
        }
    }
}
