package com.huawei.hwwifiproservice;

import android.content.Context;
import android.os.Handler;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.wifi.HwHiLog;

public class CellStateMonitor {
    private static final int PHONE_TYPE_CDMA = 1;
    private static final int PHONE_TYPE_GSM = 2;
    private PhoneStateListener celllistener = new PhoneStateListener() {
        /* class com.huawei.hwwifiproservice.CellStateMonitor.AnonymousClass1 */

        @Override // android.telephony.PhoneStateListener
        public void onCellLocationChanged(CellLocation location) {
            HwHiLog.i(MessageUtil.TAG, false, "onCellLocationChanged", new Object[0]);
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
        HwHiLog.i(MessageUtil.TAG, false, "startMonitor start listen for cell info", new Object[0]);
        this.mTelephonyManager.listen(this.celllistener, 16);
    }

    public void stopMonitor() {
        this.mTelephonyManager.listen(this.celllistener, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processCellIDChange() {
        if (this.mTelephonyManager != null) {
            this.mHandler.sendEmptyMessage(20);
        }
    }

    public String getCurrentCellid() {
        CellLocation mCellLocation;
        int type;
        if (this.mTelephonyManager != null) {
            int subId = HwTelephonyManager.getDefault().getPreferredDataSubscription();
            if (subId < 0) {
                HwHiLog.e(MessageUtil.TAG, false, "getCurrentCellid subId = %{private}d", new Object[]{Integer.valueOf(subId)});
                return null;
            }
            int phoneId = SubscriptionManager.getPhoneId(subId);
            HwHiLog.d(MessageUtil.TAG, false, "getCurrentCellid phoneId = %{private}d", new Object[]{Integer.valueOf(phoneId)});
            if (phoneId < 0 || phoneId >= 2 || (mCellLocation = HwTelephonyManager.getDefault().getCellLocation(phoneId)) == null) {
                return null;
            }
            if (mCellLocation instanceof CdmaCellLocation) {
                type = 1;
                HwHiLog.d(MessageUtil.TAG, false, "getCurrentCellid type type = PHONE_TYPE_CDMA", new Object[0]);
            } else if (mCellLocation instanceof GsmCellLocation) {
                type = 2;
                HwHiLog.d(MessageUtil.TAG, false, "getCurrentCellid type type = PHONE_TYPE_GSM", new Object[0]);
            } else {
                type = 0;
            }
            if (type == 1) {
                HwHiLog.d(MessageUtil.TAG, false, "getCurrentCellid type is PHONE_TYPE_CDMA", new Object[0]);
                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) mCellLocation;
                int systemid = cdmaCellLocation.getSystemId();
                int networkid = cdmaCellLocation.getNetworkId();
                int cellid = cdmaCellLocation.getBaseStationId();
                if (systemid < 0 || networkid < 0 || cellid < 0) {
                    String plmn = this.mTelephonyManager.getNetworkOperator();
                    int cellid2 = cdmaCellLocation.getCid();
                    if (plmn == null || cellid2 < 0) {
                        HwHiLog.e(MessageUtil.TAG, false, "getCurrentCellid cellid == null", new Object[0]);
                        return null;
                    }
                    String cellidString = plmn + Integer.toString(cellid2);
                    HwHiLog.d(MessageUtil.TAG, false, "getCurrentCellid VOLTE cellidString = %{private}s", new Object[]{cellidString});
                    return cellidString;
                }
                String cellidString2 = Integer.toString(systemid) + Integer.toString(networkid) + Integer.toString(cellid);
                HwHiLog.d(MessageUtil.TAG, false, "getCurrentCellid PHONE_TYPE_CDMA cellidString = %{private}s", new Object[]{cellidString2});
                return cellidString2;
            } else if (type != 2) {
                HwHiLog.e(MessageUtil.TAG, false, "getCurrentCellid type is error", new Object[0]);
                return null;
            } else {
                HwHiLog.d(MessageUtil.TAG, false, "getCurrentCellid type is PHONE_TYPE_GSM", new Object[0]);
                String plmn2 = this.mTelephonyManager.getNetworkOperator();
                int cellid3 = ((GsmCellLocation) mCellLocation).getCid();
                if (plmn2 == null || cellid3 < 0) {
                    return null;
                }
                String cellidString3 = plmn2 + Integer.toString(cellid3);
                HwHiLog.d(MessageUtil.TAG, false, "getCurrentCellid PHONE_TYPE_GSM cellidString = %{private}s", new Object[]{cellidString3});
                return cellidString3;
            }
        } else {
            HwHiLog.w(MessageUtil.TAG, false, "getCurrentCellid mTelephonyManager == null", new Object[0]);
            return null;
        }
    }

    public static int getCellRssi() {
        return -1;
    }
}
