/*
 * NDL4Java - Native Dark mode Listener for Java.
 *
 * Copyright (C) 2024  mhahnFr
 *
 * This file is part of NDL4Java.
 *
 * NDL4Java is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NDL4Java is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NDL4Java.  If not, see <https://www.gnu.org/licenses/>.
 */

package mhahnFr.NDL;

import java.util.ArrayList;

/**
 * This class acts as main class of this project.
 *
 * @author mhahnFr
 * @since 10.05.24
 */
public final class NDL {
    private final ArrayList<DarkModeCallback> callbacks = new ArrayList<>();

    private static NDL instance;

    private NDL() {}

    private void registerCallbackImpl(final DarkModeCallback callback) {
        // TODO: Implement
        callbacks.add(callback);
    }

    private void deregisterCallbackImpl(final DarkModeCallback callback) {
        // TODO: Implement
        callbacks.remove(callback);
    }

    private boolean queryDarkModeImpl() {
        // TODO: Implement
        return false;
    }

    private static NDL getInstance() {
        if (instance == null) {
            instance = new NDL();
        }
        return instance;
    }

    public static void registerCallback(final DarkModeCallback callback) {
        getInstance().registerCallbackImpl(callback);
    }

    public static void deregisterCallback(final DarkModeCallback callback) {
        getInstance().deregisterCallbackImpl(callback);
    }

    public static boolean queryDarkMode() {
        return getInstance().queryDarkModeImpl();
    }
}