package com.android.server;

import android.util.LogWriter;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.Watchdog;
import dalvik.system.AnnotatedStackTraceElement;
import dalvik.system.VMStack;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

class WatchdogDiagnostics {
    WatchdogDiagnostics() {
    }

    private static String getBlockedOnString(Object blockedOn) {
        return String.format("- waiting to lock <0x%08x> (a %s)", Integer.valueOf(System.identityHashCode(blockedOn)), blockedOn.getClass().getName());
    }

    private static String getLockedString(Object heldLock) {
        return String.format("- locked <0x%08x> (a %s)", Integer.valueOf(System.identityHashCode(heldLock)), heldLock.getClass().getName());
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
                Object[] heldLocks = element.getHeldLocks();
                for (Object held : heldLocks) {
                    out.println("    " + getLockedString(held));
                }
            }
        }
        return true;
    }

    public static void diagnoseCheckers(List<Watchdog.HandlerChecker> blockedCheckers) {
        PrintWriter out = new PrintWriter((Writer) new LogWriter(5, "Watchdog", 3), true);
        for (int i = 0; i < blockedCheckers.size(); i++) {
            Thread blockedThread = blockedCheckers.get(i).getThread();
            if (!printAnnotatedStack(blockedThread, out)) {
                Slog.w("Watchdog", blockedThread.getName() + " stack trace:");
                StackTraceElement[] stackTrace = blockedThread.getStackTrace();
                for (StackTraceElement element : stackTrace) {
                    Slog.w("Watchdog", "    at " + element);
                }
            }
        }
    }
}
