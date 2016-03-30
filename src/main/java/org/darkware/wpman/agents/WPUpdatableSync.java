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

import org.darkware.cltools.utils.ListFile;
import org.darkware.wpman.WPManager;
import org.darkware.wpman.WPManagerConfiguration;
import org.darkware.wpman.actions.WPAction;
import org.darkware.wpman.data.WPUpdatableComponent;
import org.joda.time.Duration;

import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jeff
 * @since 2016-02-13
 */
public abstract class WPUpdatableSync<T extends WPUpdatableComponent> extends WPPeriodicAgent
{
    protected String objectType;

    public WPUpdatableSync(final String name, final String objectType, final Duration interval)
    {
        super(name, interval);
        this.objectType = objectType;
    }

    /**
     * Creates a {@link WPAction} for installing the given item.
     *
     * @param itemId The item identifier.
     * @return An {@code WPAction}.
     */
    protected abstract WPAction getInstallAction(String itemId);

    /**
     * Creates a {@link WPAction} for removing the given item.
     *
     * @param itemId The item identifier.
     * @return An {@code WPAction}.
     */
    protected abstract WPAction getRemoveAction(String itemId);

    private void installItem(final String itemId)
    {
        this.getManager().scheduleAction(this.getInstallAction(itemId));
    }

    private void removeItem(final String itemId)
    {
        this.getManager().scheduleAction(this.getRemoveAction(itemId));
    }

    /**
     * Fetch the WP-CLI updatable object type. This is used in composing WPCLI commands.
     *
     * @return The type of WordPress object.
     */
    protected final String getObjectType()
    {
        return this.objectType;
    }

    /**
     * Fetch the plural of the object type. This is used for composing directory paths and
     * configuration data.
     *
     * @return The type of WordPress object.
     */
    protected final String getObjectTypePlural()
    {
        return this.objectType + "s";
    }

    protected final Path getAutoInstallListPath()
    {
        WPManagerConfiguration config = this.getManager().getConfig();

        Path autoInstallListPath = config.getWordpress().getUpdateableCollection(this.getObjectType()).getAutoInstallList();

        return autoInstallListPath;
    }

    protected final Path getIgnoreListPath()
    {
        WPManagerConfiguration config = this.getManager().getConfig();

        Path ignoreList = config.getWordpress().getUpdateableCollection(this.getObjectType()).getIgnoreList();

        return ignoreList;
    }

    protected abstract Set<String> getInstalledItemIds();

    protected abstract Stream<T> getUpdatableList();

    @Override
    public void executeAction()
    {
        WPManager.log.info("Starting {} synchronization.", this.getObjectType());

        ListFile ignoreFile = new ListFile(this.getIgnoreListPath());
        ignoreFile.setCommentTokens("#", ";", "//");
        Set<String> ignore = ignoreFile.stream().collect(Collectors.toSet());

        ListFile autoInstallFile = new ListFile(this.getAutoInstallListPath());
        autoInstallFile.setCommentTokens("#", ";", "//");

        // Collect the set of installed item ids
        Set<String> installedItems = new TreeSet<>(this.getInstalledItemIds());

        // Collect the set of requested item ids
        Set<String> requestedItems = new TreeSet<>();
        autoInstallFile.stream().forEach(requestedItems::add);

        // Install missing items
        requestedItems.stream()
                      .filter(i ->!ignore.contains(i))
                      .filter(p -> !installedItems.contains(p))
                      .forEach(this::installItem);

        // Remove extraneous items
        installedItems.stream()
                      .filter(i -> !ignore.contains(i))
                      .filter(p -> !requestedItems.contains(p))
                      .forEach(this::removeItem);

        // Find items to update, ignoring any item scheduled for removal
        this.getUpdatableList()
            .filter(i -> !ignore.contains(i.getId()))
            .filter(WPUpdatableComponent::hasUpdate)
            .map(WPUpdatableComponent::getId).forEach(this::installItem);
    }
}
