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
    private static Class tetherExtensionClass;
    private static Object tetherExtensionObj;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.NetPluginDelegate.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.NetPluginDelegate.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.NetPluginDelegate.<clinit>():void");
    }

    public static void getTetherStats(NetworkStats uidStats, NetworkStats devStats, NetworkStats xtStats) {
        if (loadTetherExtJar()) {
            try {
                tetherExtensionClass.getMethod("getTetherStats", new Class[]{NetworkStats.class, NetworkStats.class, NetworkStats.class}).invoke(tetherExtensionObj, new Object[]{uidStats, devStats, xtStats});
            } catch (Exception e) {
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
            return ret_val;
        }
        try {
            ret_val = (NetworkStats) tetherExtensionClass.getMethod("peekTetherStats", new Class[0]).invoke(tetherExtensionObj, new Object[0]);
        } catch (Exception e) {
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
            } catch (Exception e) {
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
            } catch (Exception e) {
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
            } catch (Exception e) {
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
            } catch (Exception e) {
                e.printStackTrace();
                Log.w(TAG, "Failed to invoke setUpstream()");
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.w(TAG, "Error calling setUpstream Method on extension jar");
            }
        }
    }

    private static synchronized boolean loadTetherExtJar() {
        synchronized (NetPluginDelegate.class) {
            String realProvider = "com.qualcomm.qti.tetherstatsextension.TetherStatsReporting";
            String realProviderPath = Environment.getRootDirectory().getAbsolutePath() + "/framework/ConnectivityExt.jar";
            if (tetherExtensionClass != null && tetherExtensionObj != null) {
                return true;
            } else if (new File(realProviderPath).exists()) {
                if (tetherExtensionClass == null && tetherExtensionObj == null) {
                    try {
                        tetherExtensionClass = new PathClassLoader(realProviderPath, ClassLoader.getSystemClassLoader()).loadClass("com.qualcomm.qti.tetherstatsextension.TetherStatsReporting");
                        tetherExtensionObj = tetherExtensionClass.newInstance();
                    } catch (ReflectiveOperationException e) {
                        e.printStackTrace();
                        Log.w(TAG, "Failed to find, instantiate or access ConnectivityExt jar ");
                        return LOGV;
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        Log.w(TAG, "unable to load ConnectivityExt jar");
                        return LOGV;
                    }
                }
                return true;
            } else {
                Log.w(TAG, "ConnectivityExt jar file not present");
                return LOGV;
            }
        }
    }
}
