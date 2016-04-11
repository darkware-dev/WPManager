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

package org.darkware.wpman.data;

/**
 * @author jeff
 * @since 2016-01-23
 */
public class WPCore extends WPDataComponent
{
    private Version version;
    private Version updateVersion;
    private WPLanguage language;

    public WPCore()
    {
        super();
    }

    @Override
    protected void refreshBaseData()
    {
        this.loadVersion();
        this.loadUpdateVersion();

        this.language = this.getManager().getDataManager().getLanguage();
    }

    protected void loadVersion()
    {
        this.version = this.getManager().getDataManager().getCoreVersion();
        WPData.log.info("WordPress version is: {}", this.version);
    }

    protected void loadUpdateVersion()
    {
        WPData.log.debug("Checking for core update.");
        this.updateVersion = this.getManager().getDataManager().getCoreUpdateVersion(this.getCoreVersion());
    }

    public Version getCoreVersion()
    {
        if (this.version == null) this.loadVersion();
        return this.version;
    }

    public Version getUpdateVersion()
    {
        if (this.updateVersion == null) this.loadUpdateVersion();
        return this.updateVersion;
    }

    public boolean hasUpdate()
    {
        return this.getUpdateVersion().greaterThan(this.getCoreVersion());
    }

    public WPLanguage getLanguage()
    {
        return language;
    }
}
