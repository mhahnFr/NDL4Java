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
import java.util.List;

/**
 * This class acts as main class of this project. It provides the wrapping functions according to the API
 * the main NDL project defines. See the file {@code ndl.h} for the full details of the API.
 *
 * @author mhahnFr
 * @since 10.05.24
 */
public final class NDL {
    /** The function pointer to the function {@code ndl_queryDarkMode}.      */
    private final MethodHandle ndlQueryDarkMode;
    /** The function pointer to the function {@code ndl_registerCallback}.   */
    private final MethodHandle ndlRegisterCallback;
    /** The function pointer to the function {@code ndl_deregisterCallback}. */
    private final MethodHandle ndlDeregisterCallback;
    /** The function pointer to the generated Java callback function.        */
    private final MemorySegment callback;
    /** The list of the registered callbacks Java side.                      */
    private final List<DarkModeCallback> callbacks = new ArrayList<>();

    /** The global singleton instance of this class.                         */
    private static NDL instance;

    /**
     * Initializes the instance. Loads the library of the NDL project and loads
     * the function pointers to the NDL API. Creates a Java callback able to be
     * registered in the native NDL library.
     *
     * @throws NDLException if the functions could not be loaded or the callback could not be created
     * @see Constants#LIBRARY_NAME
     * @see #ndlQueryDarkMode
     * @see #ndlRegisterCallback
     * @see #ndlDeregisterCallback
     * @see #callback
     */
    private NDL() {
        System.loadLibrary(Constants.LIBRARY_NAME);

        ndlQueryDarkMode = loadNDLFunction("ndl_queryDarkMode");
        ndlRegisterCallback = loadNDLFunction("ndl_registerCallback", ValueLayout.ADDRESS);
        ndlDeregisterCallback = loadNDLFunction("ndl_deregisterCallback", ValueLayout.ADDRESS);

        try {
            callback = loadCallback();
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            throw new NDLException("NDL4Java: Could not create native callback", e);
        }
    }

    /**
     * The function to be registered in the native library as dark mode callback. Calls the registered Java callbacks.
     *
     * @see #callbacks
     */
    @PrivateApi
    private void ndlCallback() {
        for (final var cb : callbacks) {
            cb.darkModeChanged();
        }
    }

    /**
     * Creates and returns a native function pointer to the method {@link #ndlCallback()}.
     *
     * @return a function pointer to the Java callback function
     * @throws NoSuchMethodException if the method was not found
     * @throws IllegalAccessException if the method could not be accessed
     */
    private MemorySegment loadCallback() throws NoSuchMethodException, IllegalAccessException {
        final var linker = Linker.nativeLinker();
        final var handle = MethodHandles.lookup().bind(this, "ndlCallback", MethodType.methodType(void.class));
        final var nativeDescription = FunctionDescriptor.ofVoid();
        return linker.upcallStub(handle, nativeDescription, Arena.ofAuto());
    }

    /**
     * Loads a function from the NDL API. All NDL functions return a {@link Boolean}.
     *
     * @param name the name of the native function to load
     * @param args the argument types of the native function
     * @return the function pointer to the described native function
     * @throws NDLException if the lookup failed
     */
    private static MethodHandle loadNDLFunction(final String name, final ValueLayout... args) {
        final var linker = Linker.nativeLinker();
        final var lookup = SymbolLookup.loaderLookup();
        final var address = lookup.find(name)
                .orElseThrow(() -> new NDLException("NDL4Java: Required function '" + name
                        + "' not found; is the library '" + Constants.LIBRARY_NAME + "' loaded?"));
        final var descriptor = FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, args);
        return linker.downcallHandle(address, descriptor);
    }

    /**
     * Registers the given Java callback. If no Java callbacks have been registered, the library registers itself
     * in the native library.
     *
     * @param callback the callback to be registered
     * @throws NDLException if the native callback registration failed
     * @see #ndlRegisterCallback
     * @see #callbacks
     * @see #callback
     */
    private void registerCallbackImpl(final DarkModeCallback callback) {
        if (callbacks.isEmpty()) {
            final boolean result;
            try {
                result = (boolean) ndlRegisterCallback.invokeExact(this.callback);
            } catch (final Throwable e) {
                throw new NDLException("NDL4Java: Caught unexpected error", e);
            }
            if (!result) {
                throw new NDLException("NDL4Java: Could not register native callback");
            }
        }
        callbacks.add(callback);
    }

    /**
     * Unregisters the given Java callback. If this was the last registered callback, the library unregisters itself
     * from the native library.
     *
     * @param callback the callback to be unregistered
     * @throws NDLException if the deregistration of the native callback failed
     * @see #ndlDeregisterCallback
     * @see #callbacks
     * @see #callback
     */
    private void deregisterCallbackImpl(final DarkModeCallback callback) {
        callbacks.remove(callback);
        if (callbacks.isEmpty()) {
            final boolean result;
            try {
                result = (boolean) ndlDeregisterCallback.invokeExact(this.callback);
            } catch (final Throwable e) {
                throw new NDLException("NDL4Java: Caught unexpected error", e);
            }
            if (!result) {
                throw new NDLException("NDL4Java: Failed to deregister native callback");
            }
        }
    }

    /**
     * Queries and returns whether the system uses a dark UI theme.
     *
     * @return whether the system uses a dark theme
     * @see #ndlQueryDarkMode
     */
    private boolean queryDarkModeImpl() {
        try {
            return (boolean) ndlQueryDarkMode.invokeExact();
        } catch (final Throwable e) {
            throw new NDLException("NDL4Java: Caught unexpected error", e);
        }
    }

    /**
     * Returns the singleton instance of this class. Creates the instance if necessary.
     *
     * @return the singleton instance
     * @see #instance
     */
    private static NDL getInstance() {
        if (instance == null) {
            instance = new NDL();
        }
        return instance;
    }

    /**
     * Registers the given callback. It is called when the operating system switches its theme.
     *
     * @param callback the callback to be registered
     * @throws NDLException if an error happens
     */
    @PublicApi
    public static void registerCallback(final DarkModeCallback callback) {
        getInstance().registerCallbackImpl(callback);
    }

    /**
     * Unregisters the given callback.
     *
     * @param callback the callback to be unregistered
     * @throws NDLException if an error happens
     */
    @PublicApi
    public static void deregisterCallback(final DarkModeCallback callback) {
        getInstance().deregisterCallbackImpl(callback);
    }

    /**
     * Queries and returns whether the operating system uses a dark theme at the moment.
     *
     * @return whether the operating system uses the dark mode
     * @throws NDLException if an error happens
     */
    @PublicApi
    public static boolean queryDarkMode() {
        return getInstance().queryDarkModeImpl();
    }
}