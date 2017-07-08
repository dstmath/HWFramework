package com.android.server.wifi;

import android.content.Context;
import android.util.Log;
import com.huawei.connectivitylog.ConnectivityLogManager;

public class HwWifiServiceFactory {
    private static final String TAG = "HwWifiServiceFactory";
    private static final Object mLock = null;
    private static volatile Factory obj;

    public interface Factory {
        HwSupplicantHeartBeat getHwSupplicantHeartBeat(WifiStateMachine wifiStateMachine, WifiNative wifiNative);

        HwWifiCHRConst getHwWifiCHRConst();

        HwWifiCHRService getHwWifiCHRService();

        HwWifiCHRStateManager getHwWifiCHRStateManager();

        HwWifiDFTUtil getHwWifiDFTUtil();

        HwWifiMonitor getHwWifiMonitor();

        HwWifiServiceManager getHwWifiServiceManager();

        HwWifiStatStore getHwWifiStatStore();

        HwIsmCoexWifiStateTrack getIsmCoexWifiStateTrack(Context context, WifiStateMachine wifiStateMachine, WifiNative wifiNative);

        void initWifiCHRService(Context context);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.HwWifiServiceFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.HwWifiServiceFactory.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.HwWifiServiceFactory.<clinit>():void");
    }

    private static Factory getImplObject() {
        if (obj == null) {
            synchronized (mLock) {
                if (obj == null) {
                    try {
                        obj = (Factory) Class.forName("com.android.server.wifi.HwWifiServiceFactoryImpl").newInstance();
                    } catch (Exception e) {
                        Log.e(TAG, ": reflection exception is " + e);
                    }
                }
            }
        }
        Log.v(TAG, "get AllImpl object = " + obj);
        return obj;
    }

    public static HwWifiServiceManager getHwWifiServiceManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWifiServiceManager();
        }
        return DummyHwWifiServiceManager.getDefault();
    }

    public static HwWifiCHRStateManager getHwWifiCHRStateManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWifiCHRStateManager();
        }
        return null;
    }

    public static HwWifiCHRConst getHwWifiCHRConst() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWifiCHRConst();
        }
        return null;
    }

    public static HwWifiStatStore getHwWifiStatStore() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWifiStatStore();
        }
        return null;
    }

    public static HwWifiCHRService getHwWifiCHRService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWifiCHRService();
        }
        return null;
    }

    public static void initWifiCHRService(Context cxt) {
        ConnectivityLogManager.init(cxt);
        Factory obj = getImplObject();
        if (obj != null) {
            obj.initWifiCHRService(cxt);
        }
    }

    public static HwWifiMonitor getHwWifiMonitor() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWifiMonitor();
        }
        return null;
    }

    public static HwIsmCoexWifiStateTrack getIsmCoexWifiStateTrack(Context context, WifiStateMachine wifiStateMachine, WifiNative wifiNative) {
        Log.d(TAG, "getIsmCoexWifiStateTrack() is callled");
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getIsmCoexWifiStateTrack(context, wifiStateMachine, wifiNative);
        }
        return null;
    }

    public static HwSupplicantHeartBeat getHwSupplicantHeartBeat(WifiStateMachine wifiStateMachine, WifiNative wifiNative) {
        Log.d(TAG, "getHwSupplicantHeartBeat() is callled");
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwSupplicantHeartBeat(wifiStateMachine, wifiNative);
        }
        return null;
    }

    public static HwWifiDFTUtil getHwWifiDFTUtil() {
        Log.d(TAG, "getHwWifiDFTUtil() is callled");
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWifiDFTUtil();
        }
        return null;
    }
}
