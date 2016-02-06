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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.darkware.wpman.wpcli.WPCLI;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Path basePath = Paths.get(System.getProperty("wpman.dir"));
        WPCLI.setPath(basePath.resolve("bin").resolve("wpcli"));

        Logger rootLogger = (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);

        WPManager.log.info("Base directory is: {}", System.getProperty("wpman.dir"));

        WPManager manager = new WPManager(args[0]);

        manager.start();
    }
}
