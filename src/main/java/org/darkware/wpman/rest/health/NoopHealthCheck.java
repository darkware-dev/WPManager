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

package org.darkware.wpman.rest.health;

import com.codahale.metrics.health.HealthCheck;

/**
 * @author jeff
 * @since 2016-03-27
 */
public class NoopHealthCheck extends HealthCheck
{
    public NoopHealthCheck()
    {
        super();
    }

    @Override
    protected Result check() throws Exception
    {
        return Result.healthy();
    }
}
