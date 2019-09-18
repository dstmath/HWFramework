package com.android.server;

import android.net.Network;
import android.net.NetworkStats;
import android.os.Environment;
import android.util.Log;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

public class NetPluginDelegate {
    private static final boolean LOGV = false;
    private static final String TAG = "ConnectivityExtension";
    private static Class tetherExtensionClass = null;
    private static Object tetherExtensionObj = null;

    public static void getTetherStats(NetworkStats uidStats, NetworkStats devStats, NetworkStats xtStats) {
        if (loadTetherExtJar()) {
            try {
                tetherExtensionClass.getMethod("getTetherStats", new Class[]{NetworkStats.class, NetworkStats.class, NetworkStats.class}).invoke(tetherExtensionObj, new Object[]{uidStats, devStats, xtStats});
            } catch (NoSuchMethodException | SecurityException | InvocationTargetException e) {
                e.printStackTrace();
                Log.w(TAG, "Failed to invoke getTetherStats()");
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.w(TAG, "Error calling getTetherStats Method on extension jar");
            }
        }
    }

    public static NetworkStats peekTetherStats() {
        NetworkStats ret_val = null;
        if (!loadTetherExtJar()) {
            return null;
        }
        try {
            ret_val = (NetworkStats) tetherExtensionClass.getMethod("peekTetherStats", new Class[0]).invoke(tetherExtensionObj, new Object[0]);
        } catch (NoSuchMethodException | SecurityException | InvocationTargetException e) {
            e.printStackTrace();
            Log.w(TAG, "Failed to invoke peekTetherStats()");
        } catch (Exception e2) {
            e2.printStackTrace();
            Log.w(TAG, "Error calling peekTetherStats Method on extension jar");
        }
        return ret_val;
    }

    public static void natStarted(String intIface, String extIface) {
        if (loadTetherExtJar()) {
            try {
                tetherExtensionClass.getMethod("natStarted", new Class[]{String.class, String.class}).invoke(tetherExtensionObj, new Object[]{intIface, extIface});
            } catch (NoSuchMethodException | SecurityException | InvocationTargetException e) {
                e.printStackTrace();
                Log.w(TAG, "Failed to invoke natStarted()");
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.w(TAG, "Error calling natStarted Method on extension jar");
            }
        }
    }

    public static void natStopped(String intIface, String extIface) {
        if (loadTetherExtJar()) {
            try {
                tetherExtensionClass.getMethod("natStopped", new Class[]{String.class, String.class}).invoke(tetherExtensionObj, new Object[]{intIface, extIface});
            } catch (NoSuchMethodException | SecurityException | InvocationTargetException e) {
                e.printStackTrace();
                Log.w(TAG, "Failed to invoke natStopped()");
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.w(TAG, "Error calling natStopped Method on extension jar");
            }
        }
    }

    public static void setQuota(String iface, long quota) {
        if (loadTetherExtJar()) {
            try {
                tetherExtensionClass.getMethod("setQuota", new Class[]{String.class, Long.TYPE}).invoke(tetherExtensionObj, new Object[]{iface, Long.valueOf(quota)});
            } catch (NoSuchMethodException | SecurityException | InvocationTargetException e) {
                e.printStackTrace();
                Log.w(TAG, "Failed to invoke setQuota()");
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.w(TAG, "Error calling setQuota Method on extension jar");
            }
        }
    }

    public static void setUpstream(Network net) {
        if (loadTetherExtJar()) {
            try {
                tetherExtensionClass.getMethod("setUpstream", new Class[]{Network.class}).invoke(tetherExtensionObj, new Object[]{net});
            } catch (NoSuchMethodException | SecurityException | InvocationTargetException e) {
                e.printStackTrace();
                Log.w(TAG, "Failed to invoke setUpstream()");
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.w(TAG, "Error calling setUpstream Method on extension jar");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x007c, code lost:
        return true;
     */
    private static synchronized boolean loadTetherExtJar() {
        synchronized (NetPluginDelegate.class) {
            String realProviderPath = Environment.getRootDirectory().getAbsolutePath() + "/framework/ConnectivityExt.jar";
            if (tetherExtensionClass != null && tetherExtensionObj != null) {
                return true;
            }
            if (!new File(realProviderPath).exists()) {
                Log.w(TAG, "ConnectivityExt jar file not present");
                return false;
            } else if (tetherExtensionClass == null && tetherExtensionObj == null) {
                try {
                    tetherExtensionClass = new PathClassLoader(realProviderPath, ClassLoader.getSystemClassLoader()).loadClass("com.qualcomm.qti.tetherstatsextension.TetherStatsReporting");
                    tetherExtensionObj = tetherExtensionClass.newInstance();
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                    Log.w(TAG, "Failed to find, instantiate or access ConnectivityExt jar ");
                    return false;
                } catch (Exception e2) {
                    e2.printStackTrace();
                    Log.w(TAG, "unable to load ConnectivityExt jar");
                    return false;
                }
            }
        }
    }
}
