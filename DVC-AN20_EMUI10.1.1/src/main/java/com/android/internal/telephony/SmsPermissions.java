package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.telephony.Rlog;
import android.util.Log;

public class SmsPermissions {
    static final String LOG_TAG = "SmsPermissions";
    @UnsupportedAppUsage
    private final AppOpsManager mAppOps;
    @UnsupportedAppUsage
    private final Context mContext;
    @UnsupportedAppUsage
    private final Phone mPhone;

    public SmsPermissions(Phone phone, Context context, AppOpsManager appOps) {
        this.mPhone = phone;
        this.mContext = context;
        this.mAppOps = appOps;
    }

    public boolean checkCallingCanSendText(boolean persistMessageForNonDefaultSmsApp, String callingPackage, String message) {
        if (!persistMessageForNonDefaultSmsApp) {
            try {
                enforceCallerIsImsAppOrCarrierApp(message);
                return true;
            } catch (SecurityException e) {
                this.mContext.enforceCallingPermission("android.permission.MODIFY_PHONE_STATE", message);
            }
        }
        return checkCallingCanSendSms(callingPackage, message);
    }

    public void enforceCallerIsImsAppOrCarrierApp(String message) {
        int callingUid = Binder.getCallingUid();
        String carrierImsPackage = CarrierSmsUtils.getCarrierImsPackageForIntent(this.mContext, this.mPhone, new Intent("android.service.carrier.CarrierMessagingService"));
        if (carrierImsPackage != null) {
            try {
                if (callingUid == this.mContext.getPackageManager().getPackageUid(carrierImsPackage, 0)) {
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {
                if (Rlog.isLoggable("SMS", 3)) {
                    log("Cannot find configured carrier ims package");
                }
            }
        }
        TelephonyPermissions.enforceCallingOrSelfCarrierPrivilege(this.mPhone.getSubId(), message);
    }

    public boolean checkCallingCanSendSms(String callingPackage, String message) {
        this.mContext.enforceCallingPermission("android.permission.SEND_SMS", message);
        return this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0;
    }

    public boolean checkCallingOrSelfCanSendSms(String callingPackage, String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.SEND_SMS", message);
        return this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void log(String msg) {
        Log.d(LOG_TAG, "[IccSmsInterfaceManager] " + msg);
    }
}
