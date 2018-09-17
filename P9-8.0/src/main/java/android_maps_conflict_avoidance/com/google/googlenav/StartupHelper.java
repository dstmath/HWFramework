package android_maps_conflict_avoidance.com.google.googlenav;

import java.util.Vector;

public class StartupHelper {
    private static Vector startupCallbacksForBgThread = new Vector();
    private static Vector startupCallbacksForUiThread = new Vector();

    public static void addPostStartupBgCallback(Runnable runnable) {
        addPostStartupCallback(runnable, false);
    }

    private static void addPostStartupCallback(Runnable runnable, boolean needsUiThread) {
        Class cls = StartupHelper.class;
        synchronized (StartupHelper.class) {
            if (startupCallbacksForUiThread != null) {
                if (needsUiThread) {
                    startupCallbacksForUiThread.addElement(runnable);
                } else {
                    startupCallbacksForBgThread.addElement(runnable);
                }
                return;
            }
            runnable.run();
        }
    }
}
