package com.android.server.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

public class HwCustTetheringImpl extends HwCustTethering {
    private static final String ACTION_DUAL_SIM_IMSI_CHANGE = "android.intent.action.ACTION_DUAL_SIM_IMSI_CHANGE";
    protected static final boolean HWDBG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final String TAG = "HwCustTetheringImpl";
    private static boolean disableApForIMSISwith = false;
    private ConnectivityManager mConnectivityManager = null;
    private Object mPublicSync = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.connectivity.HwCustTetheringImpl.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
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

    public HwCustTetheringImpl(Context context) {
        super(context);
    }

    public void registerBroadcast(Object publicSync) {
        if (this.mContext != null) {
            disableApForIMSISwith = "true".equals(Settings.Global.getString(this.mContext.getContentResolver(), "dualimsi.disableap"));
            if (disableApForIMSISwith) {
                this.mPublicSync = publicSync;
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_DUAL_SIM_IMSI_CHANGE);
                this.mContext.registerReceiver(this.mReceiver, filter);
                Context context = this.mContext;
                Context context2 = this.mContext;
                this.mWifiManager = (WifiManager) context.getSystemService("wifi");
            }
        }
    }
}
