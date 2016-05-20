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

package org.darkware.wpman.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.darkware.wpman.data.WPBlog;
import org.darkware.wpman.services.PostNotificationService;

/**
 * This is a container for configuration used by the {@link PostNotificationService}.
 *
 * @author jeff
 * @since 2016-03-29
 */
public class PostNotificationConfig
{
    @JsonProperty("blog")
    private String blog;

    @JsonProperty("updateCategory")
    private String updateCategory;
    @JsonProperty("installCategory")
    private String installCategory;
    @JsonProperty("uninstallCategory")
    private String uninstallCategory;

    @JsonProperty("pluginCategory")
    private String pluginCategory;
    @JsonProperty("themeCategory")
    private String themeCategory;
    @JsonProperty("coreCategory")
    private String coreCategory;

    @JsonProperty("notificationUser")
    private String notificationUser;

    /**
     * Create a new container for post notification configuration using default values. Note that some
     * fields do not have default values and the configuration is incomplete without setting some fields.
     */
    public PostNotificationConfig()
    {
        super();

        this.installCategory = "install";
        this.updateCategory = "update";
        this.uninstallCategory = "uninstall";

        this.pluginCategory = "plugin";
        this.themeCategory = "theme";
        this.coreCategory = "core";

        this.notificationUser = "wpmanager";
    }

    /**
     * Fetch the subdomain of the {@link WPBlog} to post notifications to.
     *
     * @return The top-level subdomain of the blog.
     */
    public String getBlog()
    {
        return this.blog;
    }

    /**
     * Set the subdomain of the blog to post notifications to.
     *
     * @param blog A {@code String} representing the subdomain.
     */
    public void setBlog(final String blog)
    {
        this.blog = blog;
    }

    /**
     * Fetch the internal identifier (the "slug") of the category to use for update notifications.
     *
     * @return The slug of the category for updates.
     */
    public String getUpdateCategory()
    {
        return this.updateCategory;
    }

    /**
     * Set the internal identifier (the "slug") of the category to use for update notifications.
     *
     * @param updateCategory The slug of the category for updates.
     */
    public void setUpdateCategory(final String updateCategory)
    {
        this.updateCategory = updateCategory;
    }

    /**
     * Fetch the internal identifier (the "slug") of the category to use for install notifications.
     *
     * @return The slug of the category for installs.
     */
    public String getInstallCategory()
    {
        return this.installCategory;
    }

    /**
     * Set the internal identifier (the "slug") of the category to use for install notifications.
     *
     * @param installCategory The slug of the category for installs.
     */
    public void setInstallCategory(final String installCategory)
    {
        this.installCategory = installCategory;
    }

    /**
     * Fetch the internal identifier (the "slug") of the category to use for uninstall notifications.
     *
     * @return The slug of the category for removals.
     */
    public String getUninstallCategory()
    {
        return this.uninstallCategory;
    }

    /**
     * Set the internal identifier (the "slug") of the category to use for uninstall notifications.
     *
     * @param uninstallCategory The slug of the category for removals.
     */
    public void setUninstallCategory(final String uninstallCategory)
    {
        this.uninstallCategory = uninstallCategory;
    }

    /**
     * Fetch the internal identifier (the "slug") of the category to use for notifications involving
     * changes to plugins.
     *
     * @return The slug of the category for plugin notifications.
     */
    public String getPluginCategory()
    {
        return this.pluginCategory;
    }

    /**
     * Set the internal identifier (the "slug") of the category to use for notifications involving
     * changes to plugins.
     *
     * @param pluginCategory The slug of the category for plugin notifications.
     */
    public void setPluginCategory(final String pluginCategory)
    {
        this.pluginCategory = pluginCategory;
    }

    /**
     * Fetch the internal identifier (the "slug") of the category to use for notifications involving
     * changes to themes.
     *
     * @return The slug of the category for theme notifications.
     */
    public String getThemeCategory()
    {
        return this.themeCategory;
    }

    /**
     * Set the internal identifier (the "slug") of the category to use for notifications involving
     * changes to themes.
     *
     * @param themeCategory The slug of the category for theme notifications.
     */
    public void setThemeCategory(final String themeCategory)
    {
        this.themeCategory = themeCategory;
    }

    /**
     * Fetch the internal identifier (the "slug") of the category to use for notifications involving
     * changes to the core software.
     *
     * @return The slug of the category for core notifications.
     */
    public String getCoreCategory()
    {
        return this.coreCategory;
    }

    /**
     * Set the internal identifier (the "slug") of the category to use for notifications involving
     * changes to plugins.
     *
     * @param coreCategory The slug of the category for core notifications.
     */
    public void setCoreCategory(final String coreCategory)
    {
        this.coreCategory = coreCategory;
    }

    /**
     * Fetch the user login for the user to list as the author of notification posts.
     *
     * @return The user login, as a lowercase {@code String}
     */
    public String getNotificationUser()
    {
        return this.notificationUser;
    }

    /**
     * Set the user login to use as the notification post author.
     *
     * @param notificationUser The user name.
     */
    public void setNotificationUser(final String notificationUser)
    {
        if (notificationUser == null) throw new IllegalArgumentException("Post notification user cannot be null");
        this.notificationUser = notificationUser.toLowerCase();
    }
}
