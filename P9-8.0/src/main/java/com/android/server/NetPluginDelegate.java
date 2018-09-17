package com.android.server;

import android.net.Network;
import android.net.NetworkStats;
import android.os.Environment;
import android.util.Log;
import dalvik.system.PathClassLoader;
import java.io.File;

public class NetPluginDelegate {
    private static final boolean LOGV = false;
    private static final String TAG = "ConnectivityExtension";
    private static Class tetherExtensionClass = null;
    private static Object tetherExtensionObj = null;

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0042 A:{Splitter: B:3:0x0007, ExcHandler: java.lang.reflect.InvocationTargetException (r0_1 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0042 A:{Splitter: B:3:0x0007, ExcHandler: java.lang.reflect.InvocationTargetException (r0_1 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:8:0x0042, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:0x0043, code:
            r0.printStackTrace();
            android.util.Log.w(TAG, "Failed to invoke getTetherStats()");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void getTetherStats(NetworkStats uidStats, NetworkStats devStats, NetworkStats xtStats) {
        if (loadTetherExtJar()) {
            try {
                tetherExtensionClass.getMethod("getTetherStats", new Class[]{NetworkStats.class, NetworkStats.class, NetworkStats.class}).invoke(tetherExtensionObj, new Object[]{uidStats, devStats, xtStats});
            } catch (Exception e) {
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.w(TAG, "Error calling getTetherStats Method on extension jar");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0030 A:{Splitter: B:3:0x0008, ExcHandler: java.lang.reflect.InvocationTargetException (r1_1 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0030 A:{Splitter: B:3:0x0008, ExcHandler: java.lang.reflect.InvocationTargetException (r1_1 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:8:0x0030, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:0x0031, code:
            r1.printStackTrace();
            android.util.Log.w(TAG, "Failed to invoke peekTetherStats()");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static NetworkStats peekTetherStats() {
        NetworkStats ret_val = null;
        if (!loadTetherExtJar()) {
            return ret_val;
        }
        try {
            ret_val = (NetworkStats) tetherExtensionClass.getMethod("peekTetherStats", new Class[0]).invoke(tetherExtensionObj, new Object[0]);
        } catch (Exception e) {
        } catch (Exception e2) {
            e2.printStackTrace();
            Log.w(TAG, "Error calling peekTetherStats Method on extension jar");
        }
        return ret_val;
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x003a A:{Splitter: B:3:0x0007, ExcHandler: java.lang.reflect.InvocationTargetException (r0_1 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x003a A:{Splitter: B:3:0x0007, ExcHandler: java.lang.reflect.InvocationTargetException (r0_1 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:8:0x003a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:0x003b, code:
            r0.printStackTrace();
            android.util.Log.w(TAG, "Failed to invoke natStarted()");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void natStarted(String intIface, String extIface) {
        if (loadTetherExtJar()) {
            try {
                tetherExtensionClass.getMethod("natStarted", new Class[]{String.class, String.class}).invoke(tetherExtensionObj, new Object[]{intIface, extIface});
            } catch (Exception e) {
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.w(TAG, "Error calling natStarted Method on extension jar");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x003a A:{Splitter: B:3:0x0007, ExcHandler: java.lang.reflect.InvocationTargetException (r0_1 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x003a A:{Splitter: B:3:0x0007, ExcHandler: java.lang.reflect.InvocationTargetException (r0_1 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:8:0x003a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:0x003b, code:
            r0.printStackTrace();
            android.util.Log.w(TAG, "Failed to invoke natStopped()");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void natStopped(String intIface, String extIface) {
        if (loadTetherExtJar()) {
            try {
                tetherExtensionClass.getMethod("natStopped", new Class[]{String.class, String.class}).invoke(tetherExtensionObj, new Object[]{intIface, extIface});
            } catch (Exception e) {
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.w(TAG, "Error calling natStopped Method on extension jar");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x003e A:{Splitter: B:3:0x0007, ExcHandler: java.lang.reflect.InvocationTargetException (r0_1 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x003e A:{Splitter: B:3:0x0007, ExcHandler: java.lang.reflect.InvocationTargetException (r0_1 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:8:0x003e, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:0x003f, code:
            r0.printStackTrace();
            android.util.Log.w(TAG, "Failed to invoke setQuota()");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void setQuota(String iface, long quota) {
        if (loadTetherExtJar()) {
            try {
                tetherExtensionClass.getMethod("setQuota", new Class[]{String.class, Long.TYPE}).invoke(tetherExtensionObj, new Object[]{iface, Long.valueOf(quota)});
            } catch (Exception e) {
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.w(TAG, "Error calling setQuota Method on extension jar");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0032 A:{Splitter: B:3:0x0007, ExcHandler: java.lang.reflect.InvocationTargetException (r0_1 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0032 A:{Splitter: B:3:0x0007, ExcHandler: java.lang.reflect.InvocationTargetException (r0_1 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:8:0x0032, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:0x0033, code:
            r0.printStackTrace();
            android.util.Log.w(TAG, "Failed to invoke setUpstream()");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void setUpstream(Network net) {
        if (loadTetherExtJar()) {
            try {
                tetherExtensionClass.getMethod("setUpstream", new Class[]{Network.class}).invoke(tetherExtensionObj, new Object[]{net});
            } catch (Exception e) {
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.w(TAG, "Error calling setUpstream Method on extension jar");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0077 A:{Splitter: B:20:0x004c, ExcHandler: java.lang.ClassNotFoundException (r2_0 'e' java.lang.ReflectiveOperationException)} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0077 A:{Splitter: B:20:0x004c, ExcHandler: java.lang.ClassNotFoundException (r2_0 'e' java.lang.ReflectiveOperationException)} */
    /* JADX WARNING: Missing block: B:23:0x0067, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:29:0x0077, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:31:?, code:
            r2.printStackTrace();
            android.util.Log.w(TAG, "Failed to find, instantiate or access ConnectivityExt jar ");
     */
    /* JADX WARNING: Missing block: B:33:0x0085, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static synchronized boolean loadTetherExtJar() {
        synchronized (NetPluginDelegate.class) {
            String realProvider = "com.qualcomm.qti.tetherstatsextension.TetherStatsReporting";
            String realProviderPath = Environment.getRootDirectory().getAbsolutePath() + "/framework/ConnectivityExt.jar";
            if (tetherExtensionClass != null && tetherExtensionObj != null) {
                return true;
            } else if (!new File(realProviderPath).exists()) {
                Log.w(TAG, "ConnectivityExt jar file not present");
                return false;
            } else if (tetherExtensionClass == null && tetherExtensionObj == null) {
                try {
                    tetherExtensionClass = new PathClassLoader(realProviderPath, ClassLoader.getSystemClassLoader()).loadClass("com.qualcomm.qti.tetherstatsextension.TetherStatsReporting");
                    tetherExtensionObj = tetherExtensionClass.newInstance();
                } catch (ReflectiveOperationException e) {
                } catch (Exception e2) {
                    e2.printStackTrace();
                    Log.w(TAG, "unable to load ConnectivityExt jar");
                    return false;
                }
            }
        }
    }
}
