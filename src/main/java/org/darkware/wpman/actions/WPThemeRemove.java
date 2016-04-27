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
import org.darkware.wpman.WPManager;
import org.darkware.wpman.data.WPTheme;
import org.darkware.wpman.wpcli.WPCLI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author jeff
 * @since 2016-02-10
 */
public class WPThemeRemove extends WPBasicAction<Boolean>
{
    private final String themeToken;

    public WPThemeRemove(final String themeToken)
    {
        super(WPActionCategory.MAINTENANCE);

        this.themeToken = themeToken;
    }

    @Override
    public String getDescription()
    {
        return "Auto-install theme: " + this.themeToken;
    }

    @Override
    public Boolean exec()
    {
        try
        {
            // Check to see if the theme is already installed.
            WPTheme preInstall = this.getManager().getData().getThemes().get(this.themeToken);

            if (preInstall == null)
            {
                WPManager.log.info("Theme is not loaded: {}", this.themeToken);
            }
            else
            {
                // Run an removal
                WPCLI delete = this.getWPCWpcliFactory().build("theme", "delete", this.themeToken);

                if (delete.checkSuccess())
                {
                    WPManager.log.info("Theme removed: {}", this.themeToken);
                }
                else
                {
                    WPManager.log.warn("Failed to remove theme: {}", this.themeToken);
                }
            }

            // Check to see if the directory remains. If so, remove it.
            Path themeDir = this.getManager().getThemeDir().resolve(this.themeToken);
            if (Files.exists(themeDir) && themeToken.equals(""))
            {
                WPManager.log.warn("Theme directory still exists: {}", themeDir);
                try
                {
                    FileSystemTools.deleteTree(themeDir);
                }
                catch (IOException e)
                {
                    WPManager.log.warn("Error while trying to remove theme directory: {}", themeDir);
                    Path themeGutter = this.getManager().getConfig().getThemeListConfig().getGutterDir();

                    try
                    {
                        if (Files.notExists(themeGutter)) Files.createDirectories(themeGutter);
                        Files.move(themeDir, themeGutter.resolve(this.themeToken));
                        WPManager.log.info("Theme directory moved to the gutter: {}", themeDir);
                    }
                    catch (IOException e2)
                    {
                        WPManager.log.warn("Error while trying to move theme directory to the gutter: {}", themeDir);
                    }
                }
            }
        }
        catch (Throwable t)
        {
            WPManager.log.warn("Unexpected error while removing '{}': {}", this.themeToken, t.getLocalizedMessage(), t);
        }
        finally
        {
            this.getManager().getData().getThemes().expire();
        }

        return true;
    }
}
