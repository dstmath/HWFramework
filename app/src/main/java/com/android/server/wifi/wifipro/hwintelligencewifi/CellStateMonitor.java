package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.content.Context;
import android.os.Handler;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class CellStateMonitor {
    private static final int PHONE_TYPE_CDMA = 1;
    private static final int PHONE_TYPE_GSM = 2;
    private PhoneStateListener celllistener;
    private Context mContext;
    private Handler mHandler;
    private TelephonyManager mTelephonyManager;

    public CellStateMonitor(Context context, Handler handler) {
        this.celllistener = new PhoneStateListener() {
            public void onCellLocationChanged(CellLocation location) {
                Log.e(MessageUtil.TAG, "onCellLocationChanged");
                super.onCellLocationChanged(location);
                CellStateMonitor.this.processCellIDChange();
            }
        };
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

    private void processCellIDChange() {
        if (this.mTelephonyManager != null) {
            this.mHandler.sendEmptyMessage(20);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getCurrentCellid() {
        String cellidString = null;
        if (this.mTelephonyManager != null) {
            CellLocation mCellLocation = this.mTelephonyManager.getCellLocation();
            if (mCellLocation != null) {
                int type;
                if (mCellLocation instanceof CdmaCellLocation) {
                    type = PHONE_TYPE_CDMA;
                    Log.e(MessageUtil.TAG, "getCurrentCellid type type = PHONE_TYPE_CDMA");
                } else if (mCellLocation instanceof GsmCellLocation) {
                    type = PHONE_TYPE_GSM;
                    Log.e(MessageUtil.TAG, "getCurrentCellid type type = PHONE_TYPE_GSM");
                } else {
                    type = 0;
                }
                int cellid;
                switch (type) {
                    case PHONE_TYPE_CDMA /*1*/:
                        Log.e(MessageUtil.TAG, "getCurrentCellid type is PHONE_TYPE_CDMA");
                        CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) mCellLocation;
                        if (cdmaCellLocation != null) {
                            int systemid = cdmaCellLocation.getSystemId();
                            int networkid = cdmaCellLocation.getNetworkId();
                            cellid = cdmaCellLocation.getBaseStationId();
                            if (systemid >= 0 && networkid >= 0 && cellid >= 0) {
                                cellidString = Integer.toString(systemid) + Integer.toString(networkid) + Integer.toString(cellid);
                                Log.e(MessageUtil.TAG, "getCurrentCellid PHONE_TYPE_CDMA cellidString = " + cellidString);
                                break;
                            }
                            return null;
                        }
                        break;
                    case PHONE_TYPE_GSM /*2*/:
                        Log.e(MessageUtil.TAG, "getCurrentCellid type is PHONE_TYPE_GSM");
                        GsmCellLocation gsmCellLocation = (GsmCellLocation) mCellLocation;
                        if (gsmCellLocation != null) {
                            String plmn = this.mTelephonyManager.getNetworkOperator();
                            cellid = gsmCellLocation.getCid();
                            if (plmn != null && cellid >= 0) {
                                cellidString = plmn + Integer.toString(cellid);
                                Log.e(MessageUtil.TAG, "getCurrentCellid PHONE_TYPE_GSM cellidString = " + cellidString);
                                break;
                            }
                            return null;
                        }
                        break;
                    default:
                        Log.e(MessageUtil.TAG, "getCurrentCellid type is error");
                        break;
                }
            }
            return null;
        }
        Log.e(MessageUtil.TAG, "getCurrentCellid mTelephonyManager == null");
        return cellidString;
    }

    public static int getCellRssi() {
        return -1;
    }
}
