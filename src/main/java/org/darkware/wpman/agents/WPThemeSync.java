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
import org.darkware.wpman.actions.WPThemeAutoInstall;
import org.darkware.wpman.actions.WPThemeRemove;
import org.darkware.wpman.config.ThemeConfig;
import org.darkware.wpman.data.WPTheme;
import org.darkware.wpman.data.WPUpdatableComponent;
import org.darkware.wpman.data.WPUpdatableType;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is an agent responsible for installing new themes, removing old themes and keeping existing themes
 * up to date.
 *
 * @author jeff
 * @since 2016-02-09
 */
public class WPThemeSync extends WPUpdatableSync<WPTheme, ThemeConfig>
{
    /**
     * Create a theme synchronization agent.
     */
    public WPThemeSync()
    {
        super("Theme Sync", WPUpdatableType.THEME, Duration.ofMinutes(120));
    }

    protected Set<String> getInstalledItemIds()
    {
        return this.getManager().getData().getThemes().stream().map(WPUpdatableComponent::getId).collect(Collectors.toSet());
    }

    @Override
    protected Map<String, ThemeConfig> getCollectionConfig()
    {
        return this.getManager().getConfig().getThemeListConfig().getItems();
    }

    @Override
    protected WPAction getInstallAction(final String itemId)
    {
        return new WPThemeAutoInstall(itemId);
    }

    @Override
    protected WPAction getRemoveAction(final String itemId)
    {
        return new WPThemeRemove(itemId);
    }

    @Override
    protected Stream<WPTheme> getUpdatableList()
    {
        return this.getManager().getData().getThemes().stream();
    }
}
