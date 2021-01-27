package com.android.server.hidata.wavemapping.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

public class CellStateMonitor {
    private static final int DEFAULT_VALUE = -1;
    private static final int PHONE_TYPE_CDMA = 1;
    private static final int PHONE_TYPE_GSM = 2;
    private PhoneStateListener cellListener = new PhoneStateListener() {
        /* class com.android.server.hidata.wavemapping.util.CellStateMonitor.AnonymousClass1 */

        @Override // android.telephony.PhoneStateListener
        public void onCellLocationChanged(CellLocation location) {
            LogUtil.i(false, "onCellLocationChanged", new Object[0]);
            super.onCellLocationChanged(location);
            CellStateMonitor.this.processCellIdChange();
        }
    };
    private Context mContext;
    private Handler mHandler;
    private TelephonyManager mTelephonyManager;
    private int preService = 3;
    private BroadcastReceiver serviceListener = new BroadcastReceiver() {
        /* class com.android.server.hidata.wavemapping.util.CellStateMonitor.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                LogUtil.e(false, "intent is null", new Object[0]);
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                LogUtil.e(false, "action is null", new Object[0]);
                return;
            }
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                LogUtil.e(false, "bundle is null", new Object[0]);
                return;
            }
            char c = 65535;
            if (action.hashCode() == -2104353374 && action.equals("android.intent.action.SERVICE_STATE")) {
                c = 0;
            }
            if (c == 0) {
                ServiceState serviceState = ServiceState.newFromBundle(bundle);
                LogUtil.i(false, "service state changes: from %{public}d to %{public}d", Integer.valueOf(CellStateMonitor.this.preService), Integer.valueOf(serviceState.getState()));
                if (serviceState.getState() == 0 && CellStateMonitor.this.preService != 0) {
                    CellStateMonitor.this.mHandler.sendEmptyMessage(95);
                }
                if (serviceState.getState() != 0 && CellStateMonitor.this.preService == 0) {
                    CellStateMonitor.this.mHandler.sendEmptyMessage(96);
                }
                CellStateMonitor.this.preService = serviceState.getState();
            }
        }
    };

    public CellStateMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
    }

    public void startMonitor() {
        LogUtil.i(false, "startMonitor start listen for cell info", new Object[0]);
        this.mTelephonyManager.listen(this.cellListener, 16);
        if (this.mContext != null) {
            this.mContext.registerReceiver(this.serviceListener, new IntentFilter("android.intent.action.SERVICE_STATE"));
        }
    }

    public void stopMonitor() {
        this.mTelephonyManager.listen(this.cellListener, 0);
        this.mContext.unregisterReceiver(this.serviceListener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processCellIdChange() {
        if (this.mTelephonyManager != null) {
            this.mHandler.sendEmptyMessage(93);
        }
    }

    public String getCurrentCellId() {
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager != null) {
            CellLocation mCellLocation = telephonyManager.getCellLocation();
            if (mCellLocation == null) {
                return null;
            }
            int type = judgePhoneType(mCellLocation);
            if (type == 1) {
                LogUtil.i(false, "getCurrentCellId type is PHONE_TYPE_CDMA", new Object[0]);
                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) mCellLocation;
                int systemId = cdmaCellLocation.getSystemId();
                int networkId = cdmaCellLocation.getNetworkId();
                int cellId = cdmaCellLocation.getBaseStationId();
                if (systemId < 0 || networkId < 0 || cellId < 0) {
                    return null;
                }
                String cellIdString = Integer.toString(systemId) + Integer.toString(networkId) + Integer.toString(cellId);
                LogUtil.i(false, "getCurrentCellId PHONE_TYPE_CDMA cellIdString = %{private}s", cellIdString);
                return cellIdString;
            } else if (type != 2) {
                LogUtil.e(false, "getCurrentCellId type is error", new Object[0]);
                return null;
            } else {
                LogUtil.i(false, "getCurrentCellId type is PHONE_TYPE_GSM", new Object[0]);
                String plmn = this.mTelephonyManager.getNetworkOperator();
                int cellId2 = ((GsmCellLocation) mCellLocation).getCid();
                if (plmn == null || cellId2 < 0) {
                    return null;
                }
                String cellIdString2 = plmn + Integer.toString(cellId2);
                LogUtil.i(false, "getCurrentCellId PHONE_TYPE_GSM cellIdString = %{private}s", cellIdString2);
                return cellIdString2;
            }
        } else {
            LogUtil.e(false, "getCurrentCellId mTelephonyManager == null", new Object[0]);
            return null;
        }
    }

    private int judgePhoneType(CellLocation cellLocation) {
        if (cellLocation instanceof CdmaCellLocation) {
            LogUtil.i(false, "getCurrentCellId type type = PHONE_TYPE_CDMA", new Object[0]);
            return 1;
        } else if (!(cellLocation instanceof GsmCellLocation)) {
            return 0;
        } else {
            LogUtil.i(false, "getCurrentCellId type type = PHONE_TYPE_GSM", new Object[0]);
            return 2;
        }
    }

    public static int getCellRssi() {
        return -1;
    }
}
