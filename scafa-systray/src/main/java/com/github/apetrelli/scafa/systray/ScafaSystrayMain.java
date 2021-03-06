/**
 * Scafa - A universal non-caching proxy for the road warrior
 * Copyright (C) 2015  Antonio Petrelli
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
 */
package com.github.apetrelli.scafa.systray;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import com.github.apetrelli.scafa.ScafaLauncher;
import com.github.apetrelli.scafa.jxtrayicon.JXTrayIcon;

public class ScafaSystrayMain {

    private static final Logger LOG = Logger.getLogger(ScafaSystrayMain.class.getName());

    public static void main(String[] args) {
        if (SystemTray.isSupported()) {
            BufferedImage image = null;
            try (InputStream is = ScafaSystrayMain.class.getResourceAsStream("/scafa.png")) {
                image = ImageIO.read(is);
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Cannot load systray image", e);
                System.exit(1);
            }
            try {
                JXTrayIcon trayIcon = new JXTrayIcon(image);
                trayIcon.setToolTip("Scafa");
                JPopupMenu menu = new JPopupMenu();
                JMenu profilesMenu = new JMenu("Profiles");
                ScafaLauncher launcher = new ScafaLauncher();
                launcher.initialize();
                String[] profiles = launcher.getProfiles();
                for (int i = 0; i < profiles.length; i++) {
                    JCheckBoxMenuItem item = new JCheckBoxMenuItem(profiles[i]);
                    profilesMenu.add(item);
                }
                menu.add(profilesMenu);
                trayIcon.setJPopupMenu(menu);
                menu.setInvoker(null);
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException e) {
                LOG.log(Level.SEVERE, "Cannot add systray image", e);
                System.exit(1);
            }
        }

    }

}
