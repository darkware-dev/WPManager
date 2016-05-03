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

/**
 * This is an internal storage class which is a parsing target for policy data and all child
 * configuration objects. It is part of the somewhat questionable structure which allows for
 * low-cost live policy reloading. This is accomplished by retaining a single top-level
 * delegating facade which composes and delegates to just one of these internal data objects.
 * Upon reload, the internal data object is replaced, and all other objects which retain a
 * live reference to the facade will see the updated policy immediately.
 *
 * @author jeff
 * @since 2016-05-03
 */
class WordpressPolicyData
{
}
