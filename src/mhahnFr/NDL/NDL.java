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

import mhahnFr.NDL.impl.Constants;
import mhahnFr.NDL.impl.PrivateApi;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;

// FIXME: Only save in UI thread!

/**
 * This class acts as main class of this project.
 *
 * @author mhahnFr
 * @since 10.05.24
 */
public final class NDL {
    private final MethodHandle ndlQueryDarkMode;
    private final MethodHandle ndlRegisterCallback;
    private final MethodHandle ndlDeregisterCallback;
    private final MemorySegment callback;
    private final ArrayList<DarkModeCallback> callbacks = new ArrayList<>();

    private static NDL instance;

    private NDL() {
        System.loadLibrary(Constants.LIBRARY_NAME);

        ndlQueryDarkMode = loadNDLFunction("ndl_queryDarkMode");
        ndlRegisterCallback = loadNDLFunction("ndl_registerCallback", ValueLayout.ADDRESS);
        ndlDeregisterCallback = loadNDLFunction("ndl_deregisterCallback", ValueLayout.ADDRESS);

        try {
            callback = loadCallback();
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @PrivateApi
    private void ndlCallback() {
        for (final var cb : callbacks) {
            cb.darkModeChanged();
        }
    }

    private MemorySegment loadCallback() throws NoSuchMethodException, IllegalAccessException {
        final var linker = Linker.nativeLinker();
        final var handle = MethodHandles.lookup().bind(this, "ndlCallback", MethodType.methodType(void.class));
        final var nativeDescription = FunctionDescriptor.ofVoid();
        return linker.upcallStub(handle, nativeDescription, Arena.ofAuto());
    }

    private static MethodHandle loadNDLFunction(final String name, final ValueLayout... args) {
        final var linker = Linker.nativeLinker();
        final var lookup = SymbolLookup.loaderLookup();
        final var address = lookup.find(name)
                .orElseThrow(() -> new NDLException("NDL4Java: Required function '" + name
                        + "' not found; is the library '" + Constants.LIBRARY_NAME + "' loaded?"));
        final var descriptor = FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, args);
        return linker.downcallHandle(address, descriptor);
    }

    private void registerCallbackImpl(final DarkModeCallback callback) {
        if (callbacks.isEmpty()) {
            try {
                final var _ = (boolean) ndlRegisterCallback.invokeExact(this.callback);
            } catch (final Throwable e) {
                throw new NDLException("NDL4Java: Caught unexpected error", e);
            }
        }
        callbacks.add(callback);
    }

    private void deregisterCallbackImpl(final DarkModeCallback callback) {
        callbacks.remove(callback);
        if (callbacks.isEmpty()) {
            try {
                final var _ = (boolean) ndlDeregisterCallback.invokeExact(this.callback);
            } catch (final Throwable e) {
                throw new NDLException("NDL4Java: Caught unexpected error", e);
            }
        }
    }

    private boolean queryDarkModeImpl() {
        try {
            return (boolean) ndlQueryDarkMode.invokeExact();
        } catch (final Throwable e) {
            throw new NDLException("NDL4Java: Caught unexpected error", e);
        }
    }

    private static NDL getInstance() {
        if (instance == null) {
            instance = new NDL();
        }
        return instance;
    }

    @PublicApi
    public static void registerCallback(final DarkModeCallback callback) {
        getInstance().registerCallbackImpl(callback);
    }

    @PublicApi
    public static void deregisterCallback(final DarkModeCallback callback) {
        getInstance().deregisterCallbackImpl(callback);
    }

    @PublicApi
    public static boolean queryDarkMode() {
        return getInstance().queryDarkModeImpl();
    }
}