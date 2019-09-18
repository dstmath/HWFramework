package com.android.server.hidata.wavemapping.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

public class CellStateMonitor {
    private static final int PHONE_TYPE_CDMA = 1;
    private static final int PHONE_TYPE_GSM = 2;
    private PhoneStateListener celllistener = new PhoneStateListener() {
        public void onCellLocationChanged(CellLocation location) {
            LogUtil.i("onCellLocationChanged");
            super.onCellLocationChanged(location);
            CellStateMonitor.this.processCellIDChange();
        }
    };
    private Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private TelephonyManager mTelephonyManager;
    /* access modifiers changed from: private */
    public int preService = 3;
    private BroadcastReceiver serviceListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                LogUtil.w(" action is null");
                return;
            }
            char c = 65535;
            if (action.hashCode() == -2104353374 && action.equals("android.intent.action.SERVICE_STATE")) {
                c = 0;
            }
            if (c == 0) {
                ServiceState serviceState = ServiceState.newFromBundle(intent.getExtras());
                LogUtil.i("service state changes: from " + CellStateMonitor.this.preService + " to " + serviceState.getState());
                if (serviceState.getState() == 0 && CellStateMonitor.this.preService != 0) {
                    CellStateMonitor.this.mHandler.sendEmptyMessage(95);
                }
                if (serviceState.getState() != 0 && CellStateMonitor.this.preService == 0) {
                    CellStateMonitor.this.mHandler.sendEmptyMessage(96);
                }
                int unused = CellStateMonitor.this.preService = serviceState.getState();
            }
        }
    };

    public CellStateMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
    }

    public void startMonitor() {
        LogUtil.i("startMonitor start listen for cell info");
        this.mTelephonyManager.listen(this.celllistener, 16);
        if (this.mContext != null) {
            this.mContext.registerReceiver(this.serviceListener, new IntentFilter("android.intent.action.SERVICE_STATE"));
        }
    }

    public void stopMonitor() {
        this.mTelephonyManager.listen(this.celllistener, 0);
        this.mContext.unregisterReceiver(this.serviceListener);
    }

    /* access modifiers changed from: private */
    public void processCellIDChange() {
        if (this.mTelephonyManager != null) {
            this.mHandler.sendEmptyMessage(93);
        }
    }
}
