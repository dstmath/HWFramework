package com.android.server.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.util.Log;

public class HwCustTetheringImpl extends HwCustTethering {
    private static final String ACTION_DUAL_SIM_IMSI_CHANGE = "android.intent.action.ACTION_DUAL_SIM_IMSI_CHANGE";
    private static boolean DISABLE_AP_FOR_IMSI_SWITH = SystemProperties.getBoolean("ro.config.dualimsi.disableap", false);
    protected static final boolean HWDBG;
    private static final String TAG = "HwCustTetheringImpl";
    private ConnectivityManager mConnectivityManager = null;
    private Object mPublicSync = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d("TAG", "onReceive ACTION_DUAL_SIM_IMSI_CHANGE");
            if (intent != null && HwCustTetheringImpl.ACTION_DUAL_SIM_IMSI_CHANGE.equals(intent.getAction())) {
                synchronized (HwCustTetheringImpl.this.mPublicSync) {
                    if (HwCustTetheringImpl.this.mWifiManager != null && HwCustTetheringImpl.this.mWifiManager.isWifiApEnabled()) {
                        if (HwCustTetheringImpl.this.mConnectivityManager == null) {
                            HwCustTetheringImpl.this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
                        }
                        HwCustTetheringImpl.this.mConnectivityManager.stopTethering(0);
                    }
                }
            }
        }
    };
    private WifiManager mWifiManager = null;

    static {
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false : true;
        HWDBG = isLoggable;
    }

    public HwCustTetheringImpl(Context context) {
        super(context);
    }

    public void registerBroadcast(Object publicSync) {
        if (DISABLE_AP_FOR_IMSI_SWITH && this.mContext != null) {
            this.mPublicSync = publicSync;
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_DUAL_SIM_IMSI_CHANGE);
            this.mContext.registerReceiver(this.mReceiver, filter);
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
    }
}
