package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;

public class GeoWifInfoCollectManager {
    private static final boolean DEBUG = false;
    private static final String TAG = "HwGnssLog_GeoWifiInfoCollectManager";
    private static final int WIFI_SCAN_RESULTS_VALID_TIME = 5000;
    private boolean isWifiApListFlashed;
    private Context mContext;
    private long mLastScanTimeStamp;
    private WifiReceiver mWifiReceiver;

    private class WifiReceiver extends BroadcastReceiver {
        private WifiReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.net.wifi.SCAN_RESULTS")) {
                GeoWifInfoCollectManager.this.isWifiApListFlashed = true;
                GeoWifInfoCollectManager.this.mLastScanTimeStamp = System.currentTimeMillis();
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.GeoWifInfoCollectManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.GeoWifInfoCollectManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.GeoWifInfoCollectManager.<clinit>():void");
    }

    public GeoWifInfoCollectManager(Context context) {
        this.isWifiApListFlashed = DEBUG;
        this.mLastScanTimeStamp = 0;
        this.mContext = context;
        wifiStatusInit();
    }

    private void wifiStatusInit() {
        registerBroadcastReciver();
    }

    private void registerBroadcastReciver() {
        this.mWifiReceiver = new WifiReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mContext.registerReceiver(this.mWifiReceiver, intentFilter);
    }

    public boolean checkWifiInfoAvaiable() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(GnssConnectivityLogManager.SUBSYS_WIFI);
        long wifiScanDiffTime = System.currentTimeMillis() - this.mLastScanTimeStamp;
        if (wifiManager != null && wifiManager.isWifiEnabled() && this.isWifiApListFlashed && wifiScanDiffTime <= 5000) {
            return true;
        }
        return DEBUG;
    }

    public long getLastScanTimeStamp() {
        return this.mLastScanTimeStamp;
    }

    public void resetWifiApListFlashed() {
        this.isWifiApListFlashed = DEBUG;
    }
}
