package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.content.Context;
import android.os.Handler;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class CellStateMonitor {
    private static final int PHONE_TYPE_CDMA = 1;
    private static final int PHONE_TYPE_GSM = 2;
    private PhoneStateListener celllistener = new PhoneStateListener() {
        public void onCellLocationChanged(CellLocation location) {
            Log.e(MessageUtil.TAG, "onCellLocationChanged");
            super.onCellLocationChanged(location);
            CellStateMonitor.this.processCellIDChange();
        }
    };
    private Context mContext;
    private Handler mHandler;
    private TelephonyManager mTelephonyManager;

    public CellStateMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
    }

    public void startMonitor() {
        Log.e(MessageUtil.TAG, "startMonitor start listen for cell info");
        this.mTelephonyManager.listen(this.celllistener, 16);
    }

    public void stopMonitor() {
        this.mTelephonyManager.listen(this.celllistener, 0);
    }

    /* access modifiers changed from: private */
    public void processCellIDChange() {
        if (this.mTelephonyManager != null) {
            this.mHandler.sendEmptyMessage(20);
        }
    }

    public String getCurrentCellid() {
        int type;
        String cellidString = null;
        if (this.mTelephonyManager != null) {
            int phoneId = HwTelephonyManager.getDefault().getPreferredDataSubscription();
            Log.d(MessageUtil.TAG, "getCurrentCellid phoneId = " + phoneId);
            if (phoneId >= 0 && phoneId < 2) {
                CellLocation mCellLocation = HwTelephonyManager.getDefault().getCellLocation(phoneId);
                if (mCellLocation != null) {
                    if (mCellLocation instanceof CdmaCellLocation) {
                        type = 1;
                        Log.e(MessageUtil.TAG, "getCurrentCellid type type = PHONE_TYPE_CDMA");
                    } else if (mCellLocation instanceof GsmCellLocation) {
                        type = 2;
                        Log.e(MessageUtil.TAG, "getCurrentCellid type type = PHONE_TYPE_GSM");
                    } else {
                        type = 0;
                    }
                    switch (type) {
                        case 1:
                            Log.e(MessageUtil.TAG, "getCurrentCellid type is PHONE_TYPE_CDMA");
                            CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) mCellLocation;
                            int systemid = cdmaCellLocation.getSystemId();
                            int networkid = cdmaCellLocation.getNetworkId();
                            int cellid = cdmaCellLocation.getBaseStationId();
                            if (systemid >= 0 && networkid >= 0 && cellid >= 0) {
                                cellidString = Integer.toString(systemid) + Integer.toString(networkid) + Integer.toString(cellid);
                                Log.e(MessageUtil.TAG, "getCurrentCellid PHONE_TYPE_CDMA cellidString = " + cellidString);
                                break;
                            } else {
                                String plmn = this.mTelephonyManager.getNetworkOperator();
                                int cellid2 = cdmaCellLocation.getCid();
                                if (plmn != null && cellid2 >= 0) {
                                    cellidString = plmn + Integer.toString(cellid2);
                                    Log.e(MessageUtil.TAG, "getCurrentCellid VOLTE cellidString = " + cellidString);
                                    break;
                                } else {
                                    Log.e(MessageUtil.TAG, "getCurrentCellid cellid == null");
                                    return null;
                                }
                            }
                        case 2:
                            Log.e(MessageUtil.TAG, "getCurrentCellid type is PHONE_TYPE_GSM");
                            String plmn2 = this.mTelephonyManager.getNetworkOperator();
                            int cellid3 = ((GsmCellLocation) mCellLocation).getCid();
                            if (plmn2 != null && cellid3 >= 0) {
                                cellidString = plmn2 + Integer.toString(cellid3);
                                Log.e(MessageUtil.TAG, "getCurrentCellid PHONE_TYPE_GSM cellidString = " + cellidString);
                                break;
                            } else {
                                return null;
                            }
                        default:
                            Log.e(MessageUtil.TAG, "getCurrentCellid type is error");
                            break;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            Log.e(MessageUtil.TAG, "getCurrentCellid mTelephonyManager == null");
        }
        return cellidString;
    }

    public static int getCellRssi() {
        return -1;
    }
}
