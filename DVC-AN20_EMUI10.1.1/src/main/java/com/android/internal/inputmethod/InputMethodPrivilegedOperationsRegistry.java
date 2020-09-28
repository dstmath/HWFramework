package com.android.internal.inputmethod;

import android.os.IBinder;
import com.android.internal.annotations.GuardedBy;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public final class InputMethodPrivilegedOperationsRegistry {
    private static final Object sLock = new Object();
    private static InputMethodPrivilegedOperations sNop;
    @GuardedBy({"sLock"})
    private static WeakHashMap<IBinder, WeakReference<InputMethodPrivilegedOperations>> sRegistry;

    private InputMethodPrivilegedOperationsRegistry() {
    }

    private static InputMethodPrivilegedOperations getNopOps() {
        if (sNop == null) {
            sNop = new InputMethodPrivilegedOperations();
        }
        return sNop;
    }

    public static void put(IBinder token, InputMethodPrivilegedOperations ops) {
        synchronized (sLock) {
            if (sRegistry == null) {
                sRegistry = new WeakHashMap<>();
            }
            WeakReference<InputMethodPrivilegedOperations> previousOps = sRegistry.put(token, new WeakReference<>(ops));
            if (previousOps != null) {
                throw new IllegalStateException(previousOps.get() + " is already registered for  this token=" + token + " newOps=" + ops);
            }
        }
    }

    public static InputMethodPrivilegedOperations get(IBinder token) {
        synchronized (sLock) {
            if (sRegistry == null) {
                return getNopOps();
            }
            WeakReference<InputMethodPrivilegedOperations> wrapperRef = sRegistry.get(token);
            if (wrapperRef == null) {
                return getNopOps();
            }
            InputMethodPrivilegedOperations wrapper = wrapperRef.get();
            if (wrapper != null) {
                return wrapper;
            }
            return getNopOps();
        }
    }

    public static void remove(IBinder token) {
        synchronized (sLock) {
            if (sRegistry != null) {
                sRegistry.remove(token);
                if (sRegistry.isEmpty()) {
                    sRegistry = null;
                }
            }
        }
    }

    public static boolean isRegistered(IBinder token) {
        synchronized (sLock) {
            if (sRegistry == null) {
                return false;
            }
            return sRegistry.containsKey(token);
        }
    }
}
