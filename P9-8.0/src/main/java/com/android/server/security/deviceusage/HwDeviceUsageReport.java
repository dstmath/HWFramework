package com.android.server.security.deviceusage;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Slog;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import huawei.android.security.IHwSecurityDiagnosePlugin;
import huawei.android.security.IHwSecurityService.Stub;
import java.util.Date;

public class HwDeviceUsageReport {
    private static final int DEVICE_SECURE_DIAGNOSE_ID = 2;
    private static final boolean HW_DEBUG;
    private static final int ISBN_ID = 0;
    private static final String TAG = "HwDeviceUsageReport";
    private Bundle mDeviceUseData = new Bundle();
    private IHwSecurityDiagnosePlugin mHwSecurityDiagnosePlugin;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HW_DEBUG = isLoggable;
    }

    public HwDeviceUsageReport(Context context) {
        if (HW_DEBUG) {
            Slog.d(TAG, "HwDeviceUsageReport  create");
        }
        IBinder b = ServiceManager.getService("securityserver");
        if (b != null) {
            if (HW_DEBUG) {
                Slog.d(TAG, "getHwSecurityService");
            }
            try {
                IBinder diagnoseServiceBinder = Stub.asInterface(b).querySecurityInterface(2);
                if (diagnoseServiceBinder != null) {
                    this.mHwSecurityDiagnosePlugin = IHwSecurityDiagnosePlugin.Stub.asInterface(diagnoseServiceBinder);
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "Error in get Diagnose Service ");
            }
        }
    }

    public void reportFirstUseTime(long time) {
        if (HW_DEBUG) {
            Slog.d(TAG, "getFirstUseTime");
        }
        this.mDeviceUseData.putString(HwSecDiagnoseConstant.DEVICE_RENEW_TIME, new Date(time).toString());
        this.mDeviceUseData.putString(HwSecDiagnoseConstant.DEVICE_RENEW_SN_CODE, getISBN());
        if (this.mHwSecurityDiagnosePlugin != null) {
            try {
                this.mHwSecurityDiagnosePlugin.report(101, this.mDeviceUseData);
            } catch (RemoteException e) {
                Slog.e(TAG, "report EXCEPTION = " + e);
            }
        }
    }

    private String getISBN() {
        return HwOEMInfoAdapter.getISBNOrSN(0);
    }
}
