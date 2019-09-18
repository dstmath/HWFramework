package com.android.server;

import android.util.LogWriter;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.Watchdog;
import dalvik.system.AnnotatedStackTraceElement;
import dalvik.system.VMStack;
import java.io.PrintWriter;
import java.util.List;

class WatchdogDiagnostics {
    WatchdogDiagnostics() {
    }

    private static String getBlockedOnString(Object blockedOn) {
        return String.format("- waiting to lock <0x%08x> (a %s)", new Object[]{Integer.valueOf(System.identityHashCode(blockedOn)), blockedOn.getClass().getName()});
    }

    private static String getLockedString(Object heldLock) {
        return String.format("- locked <0x%08x> (a %s)", new Object[]{Integer.valueOf(System.identityHashCode(heldLock)), heldLock.getClass().getName()});
    }

    @VisibleForTesting
    public static boolean printAnnotatedStack(Thread thread, PrintWriter out) {
        AnnotatedStackTraceElement[] stack = VMStack.getAnnotatedThreadStackTrace(thread);
        if (stack == null) {
            return false;
        }
        out.println(thread.getName() + " annotated stack trace:");
        int length = stack.length;
        for (int i = 0; i < length; i++) {
            AnnotatedStackTraceElement element = stack[i];
            out.println("    at " + element.getStackTraceElement());
            if (element.getBlockedOn() != null) {
                out.println("    " + getBlockedOnString(element.getBlockedOn()));
            }
            if (element.getHeldLocks() != null) {
                for (Object held : element.getHeldLocks()) {
                    out.println("    " + getLockedString(held));
                }
            }
        }
        return true;
    }

    public static void diagnoseCheckers(List<Watchdog.HandlerChecker> blockedCheckers) {
        PrintWriter out = new PrintWriter(new LogWriter(5, "Watchdog", 3), true);
        for (int i = 0; i < blockedCheckers.size(); i++) {
            Thread blockedThread = blockedCheckers.get(i).getThread();
            if (!printAnnotatedStack(blockedThread, out)) {
                Slog.w("Watchdog", blockedThread.getName() + " stack trace:");
                StackTraceElement[] stackTrace = blockedThread.getStackTrace();
                int length = stackTrace.length;
                for (int i2 = 0; i2 < length; i2++) {
                    StackTraceElement element = stackTrace[i2];
                    Slog.w("Watchdog", "    at " + element);
                }
            }
        }
    }
}
