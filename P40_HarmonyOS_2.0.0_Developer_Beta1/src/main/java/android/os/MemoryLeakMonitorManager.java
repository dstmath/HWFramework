package android.os;

import android.app.Application;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import dalvik.system.PathClassLoader;
import java.io.File;

public class MemoryLeakMonitorManager {
    private static final boolean IS_DEBUG = "1".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, WifiEnterpriseConfig.ENGINE_DISABLE));
    private static final String MONITOR_CLASS_NAME = "com.huawei.MemoryLeakMonitor.MemoryLeakMonitorImpl";
    private static final String SYSTEM_DEBUGGABLE = "ro.debuggable";
    private static final String TAG = "MemoryLeakMonitorManager";
    private static String apkPath = "/system/framework/MemoryLeakMonitor.jar";
    private static PathClassLoader sClassLoader = null;
    private static boolean sIsloadedJar = false;

    private static void initMemoryLeakMonitor() {
        if (IS_DEBUG) {
            File jarFile = new File(apkPath);
            if (!jarFile.exists()) {
                Log.e(TAG, "MemoryLeakMonitor.jar is not exist!");
                return;
            }
            Log.i(TAG, "get push File path is " + jarFile.getAbsolutePath());
            try {
                sClassLoader = new PathClassLoader(jarFile.getAbsolutePath(), ClassLoader.getSystemClassLoader());
                if (sClassLoader != null) {
                    Log.i(TAG, "load MemoryLeakMonitor.jar success");
                    try {
                        if (sClassLoader.loadClass(MONITOR_CLASS_NAME) != null) {
                            Log.i(TAG, "load com.huawei.MemoryLeakMonitor.MemoryLeakMonitorImpl success");
                            sIsloadedJar = true;
                        }
                    } catch (ClassNotFoundException ex) {
                        Log.e(TAG, "MemoryLeakMonitorImpl ClassNotFoundException, ex:", ex);
                        sIsloadedJar = false;
                    } catch (Exception e) {
                        Log.e(TAG, "MemoryLeakMonitorImpl Exception.");
                        sIsloadedJar = false;
                    }
                }
            } catch (IllegalArgumentException ex2) {
                Log.e(TAG, "MemoryLeakMonitor IllegalArgumentException, ex:", ex2);
                sIsloadedJar = false;
            } catch (Exception e2) {
                Log.e(TAG, "MemoryLeakMonitor Exception");
                sIsloadedJar = false;
            }
        }
    }

    public static void installMemoryLeakMonitor(Application application) {
        initMemoryLeakMonitor();
        if (sIsloadedJar) {
            Log.i(TAG, "installMemoryLeakMonitor");
            try {
                sClassLoader.loadClass(MONITOR_CLASS_NAME).getMethod("installMemoryLeakMonitor", Application.class).invoke(null, application);
            } catch (ClassNotFoundException ex) {
                Log.e(TAG, "installMemoryLeakMonitor ClassNotFoundException, ex:", ex);
            } catch (NoSuchMethodException ex2) {
                Log.e(TAG, "installMemoryLeakMonitor NoSuchMethodException, ex:", ex2);
            } catch (Exception e) {
                Log.e(TAG, "installMemoryLeakMonitor Exception.");
            }
            Log.i(TAG, "installMemoryLeakMonitor true");
        }
    }

    public static void watchMemoryLeak(Object objReference) {
        if (sIsloadedJar) {
            Log.i(TAG, "watchMemoryLeak");
            try {
                sClassLoader.loadClass(MONITOR_CLASS_NAME).getMethod("watchMemoryLeak", Object.class).invoke(null, objReference);
            } catch (ClassNotFoundException ex) {
                Log.e(TAG, "watchMemoryLeak ClassNotFoundException, ex:", ex);
            } catch (NoSuchMethodException ex2) {
                Log.e(TAG, "watchMemoryLeak NoSuchMethodException, ex:", ex2);
            } catch (Exception e) {
                Log.e(TAG, "watchMemoryLeak Exception.");
            }
            Log.i(TAG, "watchMemoryLeak true");
        }
    }
}
