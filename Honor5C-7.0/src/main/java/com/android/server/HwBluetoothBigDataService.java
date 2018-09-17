package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.SystemProperties;
import android.os.UserHandle;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.util.List;

public class HwBluetoothBigDataService {
    public static final String BIGDATA_RECEIVER_PACKAGENAME = "com.android.bluetooth";
    public static final String BLUETOOTH_BIGDATA = "com.android.bluetooth.bluetoothBigdata";
    private static final String TAG = "HwBluetoothBigDataService";
    private static HwBluetoothBigDataService mHwBluetoothBigDataService;
    boolean isChinaArea;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.HwBluetoothBigDataService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.HwBluetoothBigDataService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.HwBluetoothBigDataService.<clinit>():void");
    }

    public HwBluetoothBigDataService() {
        this.isChinaArea = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", AppHibernateCst.INVALID_PKG));
    }

    public static synchronized HwBluetoothBigDataService getHwBluetoothBigDataService() {
        HwBluetoothBigDataService hwBluetoothBigDataService;
        synchronized (HwBluetoothBigDataService.class) {
            if (mHwBluetoothBigDataService == null) {
                mHwBluetoothBigDataService = new HwBluetoothBigDataService();
            }
            hwBluetoothBigDataService = mHwBluetoothBigDataService;
        }
        return hwBluetoothBigDataService;
    }

    public void sendBigDataEvent(Context mContext, String bigDataEvent) {
        if (this.isChinaArea && getAppName(mContext, Binder.getCallingPid()) != null) {
            Intent intent = new Intent();
            intent.setAction(bigDataEvent);
            intent.setPackage(BIGDATA_RECEIVER_PACKAGENAME);
            intent.putExtra("appName", getAppName(mContext, Binder.getCallingPid()));
            mContext.sendBroadcastAsUser(intent, UserHandle.of(UserHandle.getCallingUserId()), BLUETOOTH_BIGDATA);
        }
    }

    public String getAppName(Context mContext, int pid) {
        if (pid <= 0) {
            return null;
        }
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService("activity");
        if (activityManager == null) {
            return null;
        }
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null || appProcesses.size() == 0) {
            return null;
        }
        String packageName = null;
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.pid == pid && appProcess.importance == 100) {
                packageName = appProcess.processName;
                break;
            }
        }
        return packageName;
    }
}
