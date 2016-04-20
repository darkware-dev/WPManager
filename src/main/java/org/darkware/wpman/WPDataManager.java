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

//import com.google.gson.reflect.TypeToken;

import com.google.common.reflect.TypeToken;
import org.darkware.wpman.data.*;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIFieldsOption;
import org.darkware.wpman.wpcli.WPCLIOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author jeff
 * @since 2016-01-30
 */
public class WPDataManager extends WPComponent
{
    protected WPDataManager()
    {
        super();
    }

    public Version getCoreVersion()
    {
        WPCLI versionCmd = this.buildCommand("core", "version");
        return new Version(versionCmd.readValue());
    }

    public Version getCoreUpdateVersion(Version currentVersion)
    {
        WPCLI updateCmd = this.buildCommand("core", "check-update");
        updateCmd.loadPlugins(false);
        updateCmd.loadThemes(false);

        List<List<String>> updateData = updateCmd.readCSV();
        if (updateData.size() < 2) return currentVersion;

        else return new Version(updateData.get(1).get(0));
    }

    public WPLanguage getLanguage()
    {
        WPCLI languageCmd = this.buildCommand("core", "language", "list");
        languageCmd.loadPlugins(false);
        languageCmd.loadThemes(false);
        languageCmd.setOption(new WPCLIFieldsOption("language", "native_name"));
        languageCmd.setOption(new WPCLIOption<>("status", "active"));

        List<WPLanguage> languages = languageCmd.readJSON(new TypeToken<List<WPLanguage>>() {});

        return languages.get(0);
    }

    public List<WPCronHook> getCronEvents(final WPBlog blog)
    {
        WPCLI eventListCmd = this.buildCommand("cron", "event", "list");
        eventListCmd.loadThemes(false);
        eventListCmd.setBlog(blog);
        eventListCmd.setOption(new WPCLIFieldsOption("hook", "next_run"));

        return eventListCmd.readJSON(new TypeToken<List<WPCronHook>>(){});
    }

    public List<WPPlugin> getPlugins()
    {
        WPCLI pluginListCmd = this.buildCommand("plugin", "list");
        pluginListCmd.loadPlugins(false);
        pluginListCmd.loadThemes(false);

        WPCLIFieldsOption fields = new WPCLIFieldsOption();
        fields.add("name");
        fields.add("description");
        fields.add("title");
        fields.add("status");
        fields.add("version");
        fields.add("update_version");
        pluginListCmd.setOption(fields);

        return pluginListCmd.readJSON(new TypeToken<List<WPPlugin>>(){});
    }

    protected WPCLI getThemeListCommand()
    {
        WPCLI themeListCmd = this.buildCommand("theme", "list");
        themeListCmd.loadThemes(false);
        themeListCmd.loadPlugins(false);

        WPCLIFieldsOption fields = new WPCLIFieldsOption();
        fields.add("name");
        fields.add("description");
        fields.add("title");
        fields.add("status");
        fields.add("version");
        fields.add("update_version");
        fields.add("enabled");
        themeListCmd.setOption(fields);

        return themeListCmd;
    }

    public List<WPTheme> getThemes()
    {
        WPCLI themeListCmd = this.getThemeListCommand();

        return themeListCmd.readJSON(new TypeToken<List<WPTheme>>(){});
    }

    public List<WPBlog> getBlogs()
    {
        WPCLI listCmd = this.buildCommand("site", "list");
        listCmd.loadPlugins(false);
        listCmd.loadThemes(false);

        WPCLIFieldsOption fields = new WPCLIFieldsOption();
        fields.add("blog_id");
        fields.add("domain");
        fields.add("url");
        fields.add("last_updated");
        fields.add("registered");
        fields.add("public");
        fields.add("deleted");
        listCmd.setOption(fields);

        return listCmd.readJSON(new TypeToken<List<WPBlog>>(){});
    }

    public List<WPPlugin> getPluginsForBlog(final WPBlog blog)
    {
        List<WPPlugin> plugins = new ArrayList<>();

        return plugins;
    }

    /**
     * Fetch the set of users associated with a given blog.
     *
     * @param blog The blog to fetch users for.
     * @return A {@code Set} of {@link WPUser}s.
     */
    public Set<WPUser> getUsersForBlog(final WPBlog blog)
    {
        WPCLI userListCmd = this.buildCommand("user", "list");
        userListCmd.loadPlugins(false);
        userListCmd.loadThemes(false);
        userListCmd.setBlog(blog);

        WPCLIFieldsOption fields = new WPCLIFieldsOption();
        fields.add("ID");
        fields.add("user_login");
        fields.add("display_name");
        fields.add("user_email");
        fields.add("registered");
        fields.add("roles");

        userListCmd.setOption(fields);

        return userListCmd.readJSON(new TypeToken<Set<WPUser>>() {});
    }

    public WPTheme getThemeForBlog(final WPBlog blog)
    {
        WPCLI blogTheme = this.getThemeListCommand();
        blogTheme.setBlog(blog);
        blogTheme.restrictList("status", WPThemeStatus.ACTIVE);

        List<WPTheme> activeThemes = blogTheme.readJSON(new TypeToken<List<WPTheme>>(){});

        return activeThemes.get(0);
    }
}
