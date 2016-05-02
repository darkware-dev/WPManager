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
import org.darkware.wpman.actions.WPPluginAutoInstall;
import org.darkware.wpman.actions.WPPluginRemove;
import org.darkware.wpman.data.WPPlugin;
import org.darkware.wpman.data.WPUpdatableComponent;
import org.darkware.wpman.data.WPUpdatableType;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 *
 * @author jeff
 * @since 2016-02-09
 */
public class WPPluginSync extends WPUpdatableSync<WPPlugin>
{
    /**
     * Create a plugin synchronization agent.
     */
    public WPPluginSync()
    {
        super("Plugin Sync", WPUpdatableType.PLUGIN, Duration.ofMinutes(360));
    }

    /**
     * Creates a {@link WPAction} for installing the given item.
     *
     * @param itemId The item identifier.
     * @return An {@code WPAction}.
     */
    @Override
    protected WPAction getInstallAction(final String itemId)
    {
        return new WPPluginAutoInstall(itemId);
    }

    @Override
    protected WPAction getRemoveAction(final String itemId)
    {
        return new WPPluginRemove(itemId);
    }

    @Override
    protected Set<String> getInstalledItemIds()
    {
        return this.getManager().getData().getPlugins().stream().map(WPUpdatableComponent::getId).collect(Collectors.toSet());
    }

    @Override
    protected Stream<WPPlugin> getUpdatableList()
    {
        return this.getManager().getData().getPlugins().stream();
    }
}
