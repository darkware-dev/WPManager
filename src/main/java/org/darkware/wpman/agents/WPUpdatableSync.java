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

import org.darkware.wpman.actions.WPAction;
import org.darkware.wpman.config.UpdatableCollectionConfig;
import org.darkware.wpman.config.UpdatableConfig;
import org.darkware.wpman.data.WPUpdatableComponent;
import org.darkware.wpman.data.WPUpdatableType;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * The {@code WPUpdatableSync} class is an abstract implementation of an agent which periodically
 * checks to see that a set of updatable components matches a defined list.
 *
 * @author jeff
 * @since 2016-02-13
 */
public abstract class WPUpdatableSync<T extends WPUpdatableComponent, C extends UpdatableConfig> extends WPPeriodicAgent
{
    /** The type of updatable object being synchronized. */
    protected WPUpdatableType objectType;

    /**
     * Creates a new {@code WPUpdatableSync} instance.
     *
     * @param name The name of the agent.
     * @param objectType The type of updatable object being synchronized.
     * @param interval The amount of time between agent runs.
     */
    public WPUpdatableSync(final String name, final WPUpdatableType objectType, final Duration interval)
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
    protected final WPUpdatableType getObjectType()
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
        return this.objectType.getPlural();
    }

    /**
     * Fetch the set of all items which are already installed.
     *
     * @return A {@link Set} of item IDs which are installed.
     */
    protected abstract Set<String> getInstalledItemIds();

    /**
     * Fetch a map of updatable items and their configurations.
     *
     * @return A {@link Map} of item IDs to their {@link UpdatableConfig}s.
     */
    protected abstract Map<String, C> getCollectionConfig();

    /**
     * Fetch the stream of items to check for synchronization.
     *
     * @return A {@link Stream} of updatable items.
     */
    protected abstract Stream<T> getUpdatableList();

    @Override
    public void executeAction()
    {
        UpdatableCollectionConfig collectionConfig = this.getManager().getConfig().getUpdatableCollection(this.getObjectType());
        Map<String, C> configs = this.getCollectionConfig();

        // Collect the set of installed item ids
        Set<String> installedItems = new TreeSet<>(this.getInstalledItemIds());

        // Install missing items, Update existing items
        for (Map.Entry<String, C> configEntry: configs.entrySet())
        {
            final String id = configEntry.getKey();
            final C config = configEntry.getValue();

            // If the item is already installed, mosey on down the road.
            //     ... because we'll take care of updates later.
            if (installedItems.contains(id)) continue;

            // Ignore items that don't want to be installed
            if (!config.isInstallable()) continue;

            // Schedule an install
            this.installItem(id);
        }

        // Remove extraneous items
        if (collectionConfig.getRemoveUnknown())
        {
            for (String id : installedItems)
            {
                // Ignore items that are configured
                if (configs.containsKey(id)) continue;

                this.removeItem(id);
            }
        }

        // Find items to update, ignoring any item scheduled for removal
        this.getUpdatableList()
            .filter(i -> configs.containsKey(i.getId()) && configs.get(i.getId()).isUpdatable())
            .filter(WPUpdatableComponent::hasUpdate)
            .map(WPUpdatableComponent::getId).forEach(this::installItem);
    }
}
