package com.android.server.security.panpay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.panpay.factoryreset.ClearSecureElemetTask;
import com.android.server.security.panpay.factoryreset.FactroyResetFlag;
import com.android.server.security.panpay.factoryreset.NetworkStatus;
import com.android.server.security.panpay.openapi.IPanPayOperator;
import com.android.server.security.panpay.openapi.impl.PanPayImpl;
import com.android.server.security.tsmagent.utils.HwLog;
import huawei.android.security.panpay.IPanPay;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PanPayService extends IPanPay.Stub implements IHwSecurityPlugin {
    private static final int ACTIVE = 1;
    public static final Object BINDLOCK = new Object();
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            Log.d(PanPayService.TAG, "create PanPayService");
            return new PanPayService(context);
        }

        public String getPluginPermission() {
            return null;
        }
    };
    public static final Object FLAGLOCK = new Object();
    private static final String INSE_HIDL_SERVICE_NAME = "HwInSExtNode";
    private static final String PANPAY_MANAGER_PERMISSION = "com.huawei.ukey.permission.UKEY_MANAGER";
    private static final String TAG = "PanPayService";
    private static final int UNSUPPORTED = -100;
    /* access modifiers changed from: private */
    public ClearSecureElemetTask clearTask = null;
    private ConnectReceiver connectReceiver = null;
    /* access modifiers changed from: private */
    public Context mContext = null;
    private IPanPayOperator mOperator;
    private int ret = -1;

    private class ConnectReceiver extends BroadcastReceiver {
        private ConnectReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                try {
                    if (NetworkStatus.isActive(context)) {
                        Log.d(PanPayService.TAG, "PanPayService Network isAvailable");
                        new Thread(new Runnable() {
                            public final void run() {
                                PanPayService.this.onNetworkConnected();
                            }
                        }).start();
                        return;
                    }
                    Log.d(PanPayService.TAG, "PanPayService Network notAvailable");
                } catch (Exception e) {
                    Log.d(PanPayService.TAG, "PanPayService Network isAvailable getconf Exception" + e.getMessage());
                }
            }
        }
    }

    private class PanPayThread extends Thread {
        private PanPayThread() {
        }

        public void run() {
            if (FactroyResetFlag.isActive()) {
                Log.d(PanPayService.TAG, "connectNetworkInfoReg start");
                PanPayService.this.connectNetworkInfoReg();
                ClearSecureElemetTask unused = PanPayService.this.clearTask = new ClearSecureElemetTask(PanPayService.this.mContext, $$Lambda$zsvdS7cWTK70W24jx4p2lR189s.INSTANCE);
            }
        }
    }

    public PanPayService(Context context) {
        this.mContext = context;
        this.mOperator = PanPayImpl.getInstance(context);
        Log.d(TAG, "create PanPayService PanPayService");
    }

    /* access modifiers changed from: private */
    public void onNetworkConnected() {
        if (!FactroyResetFlag.isActive()) {
            connectNetworkInfoUnReg();
            return;
        }
        Log.d(TAG, "start ClearSecureElemetTask");
        if (this.clearTask != null) {
            this.clearTask.start();
        }
    }

    public void onStart() {
        Log.d(TAG, "TA initCertification start");
        new PanPayThread().start();
    }

    public void onStop() {
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.security.panpay.PanPayService, android.os.IBinder] */
    public IBinder asBinder() {
        return this;
    }

    /* access modifiers changed from: private */
    public void connectNetworkInfoReg() {
        try {
            if (this.connectReceiver == null) {
                this.connectReceiver = new ConnectReceiver();
                IntentFilter intFilter = new IntentFilter();
                intFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                this.mContext.registerReceiver(this.connectReceiver, intFilter);
                Log.d(TAG, "reg");
            }
        } catch (Exception e) {
            Log.d(TAG, "reg Exception " + e.getMessage());
        }
    }

    private void connectNetworkInfoUnReg() {
        try {
            if (this.connectReceiver != null) {
                this.mContext.unregisterReceiver(this.connectReceiver);
                Log.d(TAG, "unreg");
            }
        } catch (Exception e) {
            Log.d(TAG, "UnReg Exception " + e.getMessage());
        }
    }

    public int checkEligibility(String spID) {
        return this.mOperator.checkEligibility(getCallingPackage(), spID);
    }

    public int checkEligibilityEx(String serviceId, String funCallId) {
        return -100;
    }

    public int syncSeInfo(String spID, String sign, String timeStamp) {
        return this.mOperator.syncSeInfo(getCallingPackage(), spID, sign, timeStamp);
    }

    public int syncSeInfoEx(String serviceId, String funCallId) {
        return -100;
    }

    public int createSSD(String spID, String sign, String timeStamp, String ssdAid) {
        if (!FactroyResetFlag.isActive()) {
            return this.mOperator.createSSD(getCallingPackage(), spID, sign, timeStamp, ssdAid);
        }
        Log.d(TAG, "createSSD getRstFactoryFlag is active");
        return -100;
    }

    public int createSSDEx(String serviceId, String funCallId, String ssdAid) {
        return -100;
    }

    public int deleteSSD(String spID, String sign, String timeStamp, String ssdAid) {
        return this.mOperator.deleteSSD(getCallingPackage(), spID, sign, timeStamp, ssdAid);
    }

    public int deleteSSDEx(String serviceId, String funCallId, String ssdAid) {
        return -100;
    }

    public int installApplet(String serviceId, String funCallId, String appletAid, String appletVersion) {
        return -100;
    }

    public int deleteApplet(String serviceId, String funCallId, String appletAid, String appletVersion) {
        return -100;
    }

    public int lockApplet(String serviceId, String funCallId, String appletAid, String appletVersion) {
        return -100;
    }

    public int unlockApplet(String serviceId, String funCallId, String appletAid, String appletVersion) {
        return -100;
    }

    public int activateApplet(String serviceId, String funCallId, String appletAid) {
        return -100;
    }

    public int commonExecute(String spID, String serviceId, String funCallId) {
        return this.mOperator.commonExecute(getCallingPackage(), serviceId, funCallId, spID);
    }

    public String getCPLC(String spID) {
        return this.mOperator.getCPLC(getCallingPackage(), spID);
    }

    public String getCIN(String spID) {
        return this.mOperator.getCIN(getCallingPackage(), spID);
    }

    public String getIIN(String spID) {
        return this.mOperator.getIIN(getCallingPackage(), spID);
    }

    public boolean getSwitch(String spID) {
        return this.mOperator.getSwitch(getCallingPackage(), spID);
    }

    public int setSwitch(String spID, boolean choice) {
        return this.mOperator.setSwitch(getCallingPackage(), choice, spID);
    }

    public String[] getLastErrorInfo(String spID) {
        return this.mOperator.getLastErrorInfo(getCallingPackage(), spID);
    }

    public int setConfig(String spID, Map config) {
        this.mContext.enforceCallingOrSelfPermission(PANPAY_MANAGER_PERMISSION, "does not have ukey manager permission!");
        try {
            return this.mOperator.setConfig(getCallingPackage(), spID, (HashMap) config);
        } catch (Exception e) {
            HwLog.e("TypeCastException:" + e.getMessage());
            return -1;
        }
    }

    private String getCallingPackage() throws IllegalArgumentException {
        if (this.mContext != null) {
            String[] pkgs = this.mContext.getPackageManager().getPackagesForUid(getCallingUid());
            if (pkgs != null && pkgs.length > 0) {
                HwLog.d("the caller pkg [ " + Arrays.toString(pkgs) + " ]");
                return pkgs[0];
            }
        }
        throw new IllegalArgumentException();
    }
}
