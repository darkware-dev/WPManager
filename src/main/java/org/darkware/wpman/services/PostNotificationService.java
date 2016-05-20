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

package org.darkware.wpman.services;

import com.google.common.eventbus.Subscribe;
import org.darkware.wpman.config.PostNotificationConfig;
import org.darkware.wpman.data.WPBlog;
import org.darkware.wpman.data.WPTaxonomy;
import org.darkware.wpman.data.WPUser;
import org.darkware.wpman.events.WPPluginInstallEvent;
import org.darkware.wpman.events.WPPluginUpdateEvent;
import org.darkware.wpman.events.WPThemeInstallEvent;
import org.darkware.wpman.events.WPThemeUpdateEvent;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIOption;
import org.darkware.wpman.wpcli.WPCLIPostCategories;
import org.darkware.wpman.wpcli.WPCLIPostTags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

/**
 * A {@code PostNotificationService} is a {@link WPService} which listens for various events and
 * posts summaries of the results to one or more WordPress blogs on the WordPress instance being
 * managed.
 *
 * @author jeff
 * @since 2016-03-12
 */
public class PostNotificationService extends WPService
{
    protected static final Logger log = LoggerFactory.getLogger("PostNotify");

    private WPBlog postBlog;
    private PostNotificationConfig postConfig;
    private WPTaxonomy postCategories;
    private WPUser postUser;

    /**
     * Create a new {@code PostNotificationService} based on the current context.
     */
    public PostNotificationService()
    {
        super();
    }

    @Override
    protected void beforeActivation()
    {
        super.beforeActivation();

        // Fetch a local copy of the config
        this.postConfig = this.getConfig().getNotification().getPostNotification();

        // Resolve the target blog
        String postBlogName = this.postConfig.getBlog();
        this.postBlog = this.getManager().getData().getBlogs().get(postBlogName);
        if (this.postBlog == null) throw new IllegalStateException("Notification post blog does not exist.");

        // Resolve the target blogs category taxonomy
        this.postCategories = this.postBlog.getTaxonomy("category");

        // Resolve the post author
        this.postUser = this.postBlog.getUsers().get(this.postConfig.getNotificationUser());
        if (this.postUser == null) throw new IllegalStateException("Could not resolve the user '" +
            this.postConfig.getNotificationUser() + "' for the blog '" + this.postBlog.getSubDomain() + "'.");
    }

    /**
     * Create a new post with the given title. The post will be set to take the post content from an internal
     * stream (via {@link WPCLI#getStdin()} and to immediately publish on submission.
     *
     * @param title The title of the post.
     * @return A {@link WPCLI} command prepared to create a post.
     */
    protected WPCLI createPost(final String title)
    {
        WPCLI poster = this.getManager().getBuilder().build("post", "create", "-");

        poster.setBlog(this.postBlog);
        //poster.setOption(new WPCLIOption<>("post_type", "page"));
        poster.setOption(new WPCLIOption<>("post_title", title));
        poster.setOption(new WPCLIOption<>("post_status", "publish"));
        poster.setOption(new WPCLIOption<>("post_author", this.postUser.getId()));

        return poster;
    }

    /**
     * This is the event handler to post notifications that plugins have been updated.
     *
     * @param event The event describing the update.
     */
    @Subscribe
    public void onPluginUpdate(WPPluginUpdateEvent event)
    {
        try
        {
            WPCLI post = this.createPost("Updated Plugin: " + event.getItem().getName());
            post.setOption(new WPCLIPostCategories(this.postCategories.getTerm(this.postConfig.getPluginCategory()),
                                                   this.postCategories.getTerm(this.postConfig.getUpdateCategory())));
            post.setOption(new WPCLIPostTags(event.getItem().getId()));

            PrintWriter postContent = post.getStdin();
            postContent.printf("<h4>Plugin Update: %s</h4>\n", event.getItem().getName());
            postContent.printf("<p><span style=\"color: #999999; font-size: 80%%;\">Previous Version:</span> %s<br/>" +
                               "<span style=\"color: #999999; font-size: 80%%;\">Updated Version:</span> %s</p>\n",
                               event.getItem().getVersion(), event.getPreviousVersion());
            postContent.printf("<p>%s</p>\n", event.getItem().getDescription());

            post.execute();
            PostNotificationService.log.info("Posted new notification: Updated Plugin - " + event.getItem().getName());
        }
        catch (Throwable t)
        {
            PostNotificationService.log.error("Failed to create notification: {}", t.getLocalizedMessage(), t);
        }
    }

    /**
     * This is the event handler to post notifications that themes have been updated.
     *
     * @param event The event describing the update.
     */
    @Subscribe
    public void onThemeUpdate(WPThemeUpdateEvent event)
    {
        try
        {
            WPCLI post = this.createPost("Updated Theme: " + event.getItem().getName());
            post.setOption(new WPCLIPostCategories(this.postCategories.getTerm(this.postConfig.getThemeCategory()),
                                                   this.postCategories.getTerm(this.postConfig.getUpdateCategory())));
            post.setOption(new WPCLIPostTags(event.getItem().getId()));

            PrintWriter postContent = post.getStdin();
            postContent.printf("<h4>Theme Update: %s</h4>\n", event.getItem().getName());
            postContent.printf("<p><span style=\"color: #999999; font-size: 80%%;\">Previous Version:</span> %s<br/>" +
                               "<span style=\"color: #999999; font-size: 80%%;\">Updated Version:</span> %s</p>\n",
                               event.getItem().getVersion(), event.getPreviousVersion());
            postContent.printf("<p>%s</p>\n", event.getItem().getDescription());

            post.execute();
            PostNotificationService.log.info("Posted new notification: Updated Theme - " + event.getItem().getName());
        }
        catch (Throwable t)
        {
            PostNotificationService.log.error("Failed to create notification: {}", t.getLocalizedMessage(), t);
        }
    }

    /**
     * This is the event handler to post notifications that plugins have been updated.
     *
     * @param event The event describing the update.
     */
    @Subscribe
    public void onPluginInstall(WPPluginInstallEvent event)
    {
        try
        {
            WPCLI post = this.createPost("Installed Plugin: " + event.getItem().getName());
            post.setOption(new WPCLIPostCategories(this.postCategories.getTerm(this.postConfig.getPluginCategory()),
                                                   this.postCategories.getTerm(this.postConfig.getInstallCategory())));
            post.setOption(new WPCLIPostTags(event.getItem().getId()));

            PrintWriter postContent = post.getStdin();
            postContent.printf("<h4>Plugin Install: %s</h4>\n", event.getItem().getName());
            postContent.printf("<p><span style=\"color: #999999; font-size: 80%%;\">Installed Version:</span> %s</p>\n",
                               event.getItem().getVersion());
            postContent.printf("<p>%s</p>\n", event.getItem().getDescription());

            post.execute();
            PostNotificationService.log.info(
                    "Posted new notification: Installed Plugin - " + event.getItem().getName());
        }
        catch (Throwable t)
        {
            PostNotificationService.log.error("Failed to create notification: {}", t.getLocalizedMessage(), t);
        }
    }

    /**
     * This is the event handler to post notifications that themes have been updated.
     *
     * @param event The event describing the update.
     */
    @Subscribe
    public void onThemeInstall(WPThemeInstallEvent event)
    {
        try
        {
            WPCLI post = this.createPost("Installed Theme: " + event.getItem().getName());
            post.setOption(new WPCLIPostCategories(this.postCategories.getTerm(this.postConfig.getThemeCategory()),
                                                   this.postCategories.getTerm(this.postConfig.getInstallCategory())));
            post.setOption(new WPCLIPostTags(event.getItem().getId()));

            PrintWriter postContent = post.getStdin();
            postContent.printf("<h4>Theme Install: %s</h4>\n", event.getItem().getName());
            postContent.printf("<p><span style=\"color: #999999; font-size: 80%%;\">Installed Version:</span> %s</p>\n",
                               event.getItem().getVersion());
            postContent.printf("<p>%s</p>\n", event.getItem().getDescription());

            post.execute();
            PostNotificationService.log.info("Posted new notification: Installed Theme - " + event.getItem().getName());
        }
        catch (Throwable t)
        {
            PostNotificationService.log.error("Failed to create notification: {}", t.getLocalizedMessage(), t);
        }
    }

}
