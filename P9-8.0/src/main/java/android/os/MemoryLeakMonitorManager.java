package android.os;

import android.app.Application;
import android.util.Log;
import dalvik.system.PathClassLoader;
import java.io.File;

public class MemoryLeakMonitorManager {
    private static final String MONITOR_CLASS_NAME = "com.huawei.MemoryLeakMonitor.MemoryLeakMonitorImpl";
    private static final String TAG = "MemoryLeakMonitorManager";
    private static String apkPath = "/system/framework/MemoryLeakMonitor.jar";
    private static PathClassLoader mClassLoader = null;
    private static boolean mloadedJar = false;

    private static void initMemoryLeakMonitor() {
        File jarFile = new File(apkPath);
        if (jarFile.exists()) {
            Log.i(TAG, "get push File path is " + jarFile.getAbsolutePath());
            try {
                mClassLoader = new PathClassLoader(jarFile.getAbsolutePath(), ClassLoader.getSystemClassLoader());
                if (mClassLoader != null) {
                    Log.i(TAG, "load MemoryLeakMonitor.jar success");
                    try {
                        if (mClassLoader.loadClass(MONITOR_CLASS_NAME) != null) {
                            Log.i(TAG, "load com.huawei.MemoryLeakMonitor.MemoryLeakMonitorImpl success");
                            mloadedJar = true;
                        }
                        return;
                    } catch (ClassNotFoundException ex) {
                        Log.e(TAG, "MemoryLeakMonitorImpl ClassNotFoundException, ex:", ex);
                        mloadedJar = false;
                        return;
                    } catch (Exception ex2) {
                        Log.e(TAG, "MemoryLeakMonitorImpl Exception, ex:", ex2);
                        mloadedJar = false;
                        return;
                    }
                }
                return;
            } catch (IllegalArgumentException ex3) {
                Log.e(TAG, "MemoryLeakMonitor Exception, ex:", ex3);
                mloadedJar = false;
                return;
            } catch (Exception ex22) {
                Log.e(TAG, "MemoryLeakMonitor Exception, ex:", ex22);
                mloadedJar = false;
                return;
            }
        }
        Log.e(TAG, "MemoryLeakMonitor.jar is not exist!");
    }

    public static void installMemoryLeakMonitor(Application application) {
        initMemoryLeakMonitor();
        if (mloadedJar) {
            Log.i(TAG, "installMemoryLeakMonitor");
            try {
                mClassLoader.loadClass(MONITOR_CLASS_NAME).getMethod("installMemoryLeakMonitor", new Class[]{Application.class}).invoke(null, new Object[]{application});
            } catch (ClassNotFoundException ex) {
                Log.e(TAG, "installMemoryLeakMonitor ClassNotFoundException, ex:", ex);
            } catch (NoSuchMethodException ex2) {
                Log.e(TAG, "installMemoryLeakMonitor NoSuchMethodException, ex:", ex2);
            } catch (Exception ex3) {
                Log.e(TAG, "installMemoryLeakMonitor Exception, ex:", ex3);
            }
            Log.i(TAG, "installMemoryLeakMonitor true");
        }
    }

    public static void watchMemoryLeak(Object objReference) {
        if (mloadedJar) {
            Log.i(TAG, "watchMemoryLeak");
            try {
                mClassLoader.loadClass(MONITOR_CLASS_NAME).getMethod("watchMemoryLeak", new Class[]{Object.class}).invoke(null, new Object[]{objReference});
            } catch (ClassNotFoundException ex) {
                Log.e(TAG, "watchMemoryLeak ClassNotFoundException, ex:", ex);
            } catch (NoSuchMethodException ex2) {
                Log.e(TAG, "watchMemoryLeak NoSuchMethodException, ex:", ex2);
            } catch (Exception ex3) {
                Log.e(TAG, "watchMemoryLeak Exception, ex:", ex3);
            }
            Log.i(TAG, "watchMemoryLeak true");
        }
    }
}
