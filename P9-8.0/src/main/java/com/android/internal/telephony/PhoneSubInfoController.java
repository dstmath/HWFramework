package com.android.internal.telephony;

import android.app.AppOpsManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.android.internal.telephony.uicc.IsimRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;

public class PhoneSubInfoController extends AbstractPhoneSubInfo {
    private static final boolean DBG = true;
    public static final boolean IS_QCRIL_CROSS_MAPPING = SystemProperties.getBoolean("ro.hwpp.qcril_cross_mapping", false);
    public static final int SUB1 = 0;
    public static final int SUB2 = 1;
    private static final String TAG = "PhoneSubInfoController";
    private static final boolean VDBG = false;
    private final AppOpsManager mAppOps;
    private final Context mContext;
    private final Phone[] mPhone;

    public PhoneSubInfoController(Context context, Phone[] phone) {
        this.mPhone = phone;
        if (ServiceManager.getService("iphonesubinfo") == null) {
            ServiceManager.addService("iphonesubinfo", this);
        }
        this.mContext = context;
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
    }

    public String getDeviceId(String callingPackage) {
        return getDeviceIdForPhone(SubscriptionManager.getPhoneId(getDefaultSubscription()), callingPackage);
    }

    public String getDeviceIdForPhone(int phoneId, String callingPackage) {
        phoneId = getIMEIExProcess(phoneId);
        if (!checkReadPhoneStateHw(callingPackage, "getDeviceId")) {
            return null;
        }
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            phoneId = 0;
        }
        Phone phone = this.mPhone[phoneId];
        if (phone != null) {
            return phone.getDeviceId();
        }
        loge("getDeviceIdForPhone phone " + phoneId + " is null");
        return null;
    }

    public String getNaiForSubscriber(int subId, String callingPackage) {
        Phone phone = getPhone(subId);
        if (phone == null) {
            loge("getNai phone is null for Subscription:" + subId);
            return null;
        } else if (checkReadPhoneState(callingPackage, "getNai")) {
            return phone.getNai();
        } else {
            return null;
        }
    }

    public String getImeiForSubscriber(int subId, String callingPackage) {
        Phone phone = getPhone(subId);
        if (phone == null) {
            loge("getDeviceId phone is null for Subscription:" + subId);
            return null;
        } else if (checkReadPhoneStateHw(callingPackage, "getImei")) {
            return phone.getImei();
        } else {
            return null;
        }
    }

    public String getDeviceSvn(String callingPackage) {
        return getDeviceSvnUsingSubId(getDefaultSubscription(), callingPackage);
    }

    public String getDeviceSvnUsingSubId(int subId, String callingPackage) {
        Phone phone = getPhone(subId);
        if (phone == null) {
            loge("getDeviceSvn phone is null");
            return null;
        } else if (checkReadPhoneState(callingPackage, "getDeviceSvn")) {
            return phone.getDeviceSvn();
        } else {
            return null;
        }
    }

    public String getSubscriberId(String callingPackage) {
        return getSubscriberIdForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getSubscriberIdForSubscriber(int subId, String callingPackage) {
        Phone phone = getPhone(subId);
        if (phone == null) {
            loge("getSubscriberId phone is null for Subscription:" + subId);
            return null;
        } else if (checkReadPhoneState(callingPackage, "getSubscriberId")) {
            return phone.getSubscriberId();
        } else {
            return null;
        }
    }

    public String getIccSerialNumber(String callingPackage) {
        return getIccSerialNumberForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getIccSerialNumberForSubscriber(int subId, String callingPackage) {
        Phone phone = getPhone(subId);
        if (phone == null) {
            loge("getIccSerialNumber phone is null for Subscription:" + subId);
            return null;
        } else if (checkReadPhoneState(callingPackage, "getIccSerialNumber")) {
            return phone.getIccSerialNumber();
        } else {
            return null;
        }
    }

    public String getLine1Number(String callingPackage) {
        return getLine1NumberForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getLine1NumberForSubscriber(int subId, String callingPackage) {
        Phone phone = getPhone(subId);
        if (phone == null) {
            loge("getLine1Number phone is null for Subscription:" + subId);
            return null;
        } else if (checkReadPhoneNumber(callingPackage, "getLine1Number")) {
            return phone.getLine1Number();
        } else {
            return null;
        }
    }

    public String getLine1AlphaTag(String callingPackage) {
        return getLine1AlphaTagForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getLine1AlphaTagForSubscriber(int subId, String callingPackage) {
        Phone phone = getPhone(subId);
        if (phone == null) {
            loge("getLine1AlphaTag phone is null for Subscription:" + subId);
            return null;
        } else if (checkReadPhoneState(callingPackage, "getLine1AlphaTag")) {
            return phone.getLine1AlphaTag();
        } else {
            return null;
        }
    }

    public String getMsisdn(String callingPackage) {
        return getMsisdnForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getMsisdnForSubscriber(int subId, String callingPackage) {
        Phone phone = getPhone(subId);
        if (phone == null) {
            loge("getMsisdn phone is null for Subscription:" + subId);
            return null;
        } else if (checkReadPhoneState(callingPackage, "getMsisdn")) {
            return phone.getMsisdn();
        } else {
            return null;
        }
    }

    public String getVoiceMailNumber(String callingPackage) {
        return getVoiceMailNumberForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getVoiceMailNumberForSubscriber(int subId, String callingPackage) {
        Phone phone = getPhone(subId);
        if (phone == null) {
            loge("getVoiceMailNumber phone is null for Subscription:" + subId);
            return null;
        } else if (checkReadPhoneState(callingPackage, "getVoiceMailNumber")) {
            return PhoneNumberUtils.extractNetworkPortion(phone.getVoiceMailNumber());
        } else {
            return null;
        }
    }

    public String getCompleteVoiceMailNumber() {
        return getCompleteVoiceMailNumberForSubscriber(getDefaultSubscription());
    }

    public String getCompleteVoiceMailNumberForSubscriber(int subId) {
        Phone phone = getPhone(subId);
        if (phone != null) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CALL_PRIVILEGED", "Requires CALL_PRIVILEGED");
            return phone.getVoiceMailNumber();
        }
        loge("getCompleteVoiceMailNumber phone is null for Subscription:" + subId);
        return null;
    }

    public String getVoiceMailAlphaTag(String callingPackage) {
        return getVoiceMailAlphaTagForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getVoiceMailAlphaTagForSubscriber(int subId, String callingPackage) {
        Phone phone = getPhone(subId);
        if (phone == null) {
            loge("getVoiceMailAlphaTag phone is null for Subscription:" + subId);
            return null;
        } else if (checkReadPhoneState(callingPackage, "getVoiceMailAlphaTag")) {
            return phone.getVoiceMailAlphaTag();
        } else {
            return null;
        }
    }

    private Phone getPhone(int subId) {
        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            phoneId = 0;
        }
        return this.mPhone[phoneId];
    }

    private void enforcePrivilegedPermissionOrCarrierPrivilege(Phone phone) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE") != 0) {
            log("No read privileged phone permission, check carrier privilege next.");
            UiccCard uiccCard = phone.getUiccCard();
            if (uiccCard == null) {
                throw new SecurityException("No Carrier Privilege: No UICC");
            } else if (uiccCard.getCarrierPrivilegeStatusForCurrentTransaction(this.mContext.getPackageManager()) != 1) {
                throw new SecurityException("No Carrier Privilege.");
            }
        }
    }

    private int getDefaultSubscription() {
        return PhoneFactory.getDefaultSubscription();
    }

    public String getIsimImpi() {
        Phone phone = getPhone(getDefaultSubscription());
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        IsimRecords isim = phone.getIsimRecords();
        if (isim != null) {
            return isim.getIsimImpi();
        }
        return null;
    }

    public String getIsimDomain() {
        Phone phone = getPhone(getDefaultSubscription());
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        IsimRecords isim = phone.getIsimRecords();
        if (isim != null) {
            return isim.getIsimDomain();
        }
        return null;
    }

    public String[] getIsimImpu() {
        Phone phone = getPhone(getDefaultSubscription());
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        IsimRecords isim = phone.getIsimRecords();
        if (isim != null) {
            return isim.getIsimImpu();
        }
        return null;
    }

    public String getIsimIst() throws RemoteException {
        Phone phone = getPhone(getDefaultSubscription());
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        IsimRecords isim = phone.getIsimRecords();
        if (isim != null) {
            return isim.getIsimIst();
        }
        return null;
    }

    public String[] getIsimPcscf() throws RemoteException {
        Phone phone = getPhone(getDefaultSubscription());
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        IsimRecords isim = phone.getIsimRecords();
        if (isim != null) {
            return isim.getIsimPcscf();
        }
        return null;
    }

    public String getIsimChallengeResponse(String nonce) throws RemoteException {
        Phone phone = getPhone(getDefaultSubscription());
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "Requires READ_PRIVILEGED_PHONE_STATE");
        IsimRecords isim = phone.getIsimRecords();
        if (isim != null) {
            return isim.getIsimChallengeResponse(nonce);
        }
        return null;
    }

    public String getIccSimChallengeResponse(int subId, int appType, int authType, String data) throws RemoteException {
        Phone phone = getPhone(subId);
        enforcePrivilegedPermissionOrCarrierPrivilege(phone);
        UiccCard uiccCard = phone.getUiccCard();
        if (uiccCard == null) {
            loge("getIccSimChallengeResponse() UiccCard is null");
            return null;
        }
        UiccCardApplication uiccApp = uiccCard.getApplicationByType(appType);
        if (uiccApp == null) {
            loge("getIccSimChallengeResponse() no app with specified type -- " + appType);
            return null;
        }
        if (Log.HWINFO) {
            loge("getIccSimChallengeResponse() found app " + uiccApp.getAid() + " specified type -- " + appType);
        }
        if (authType == 128 || authType == 129) {
            return uiccApp.getIccRecords().getIccSimChallengeResponse(authType, data);
        }
        loge("getIccSimChallengeResponse() unsupported authType: " + authType);
        return null;
    }

    public String getGroupIdLevel1(String callingPackage) {
        return getGroupIdLevel1ForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getGroupIdLevel1ForSubscriber(int subId, String callingPackage) {
        Phone phone = getPhone(subId);
        if (phone == null) {
            loge("getGroupIdLevel1 phone is null for Subscription:" + subId);
            return null;
        } else if (checkReadPhoneState(callingPackage, "getGroupIdLevel1")) {
            return phone.getGroupIdLevel1();
        } else {
            return null;
        }
    }

    private boolean checkReadPhoneState(String callingPackage, String message) {
        boolean z = true;
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", message);
            return true;
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", message);
            if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) != 0) {
                z = false;
            }
            return z;
        }
    }

    private boolean checkReadPhoneStateHw(String callingPackage, String message) {
        boolean z = true;
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", message);
            return true;
        } catch (SecurityException e) {
            if (isSystemApp(callingPackage)) {
                return true;
            }
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", message);
            if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) != 0) {
                z = false;
            }
            return z;
        }
    }

    private boolean isSystemApp(String packageName) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            if (!(appInfo == null || (appInfo.flags & 1) == 0)) {
                log(packageName + " allowed.");
                return true;
            }
        } catch (NameNotFoundException e) {
            loge(packageName + " not found.");
        }
        return false;
    }

    private boolean checkReadPhoneNumber(String callingPackage, String message) {
        boolean z = true;
        if (this.mAppOps.noteOp(15, Binder.getCallingUid(), callingPackage) == 0) {
            return z;
        }
        try {
            return checkReadPhoneState(callingPackage, message);
        } catch (SecurityException e) {
            AppOpsManager appOpsManager;
            int opCode;
            try {
                this.mContext.enforceCallingOrSelfPermission("android.permission.READ_SMS", message);
                appOpsManager = this.mAppOps;
                opCode = AppOpsManager.permissionToOpCode("android.permission.READ_SMS");
                if (opCode == -1) {
                    return z;
                }
                if (this.mAppOps.noteOp(opCode, Binder.getCallingUid(), callingPackage) != 0) {
                    z = false;
                }
                return z;
            } catch (SecurityException e2) {
                try {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_NUMBERS", message);
                    appOpsManager = this.mAppOps;
                    opCode = AppOpsManager.permissionToOpCode("android.permission.READ_PHONE_NUMBERS");
                    if (opCode == -1) {
                        return z;
                    }
                    if (this.mAppOps.noteOp(opCode, Binder.getCallingUid(), callingPackage) != 0) {
                        z = false;
                    }
                    return z;
                } catch (SecurityException e3) {
                    throw new SecurityException(message + ": Neither user " + Binder.getCallingUid() + " nor current process has " + "android.permission.READ_PHONE_STATE" + ", " + "android.permission.READ_SMS" + ", or " + "android.permission.READ_PHONE_STATE" + ".");
                }
            }
        }
    }

    private void log(String s) {
        Rlog.d(TAG, s);
    }

    private void loge(String s) {
        Rlog.e(TAG, s);
    }

    public Phone getPhoneHw(int subId) {
        return getPhone(subId);
    }

    public Context getContextHw() {
        return this.mContext;
    }

    public int getIMEIExProcess(int subId) {
        if (IS_QCRIL_CROSS_MAPPING) {
            int mainslot = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
            subId = mainslot == subId ? 0 : 1;
            Rlog.i(TAG, "getIMEIExProcess after comparesubId=" + subId + ",mainslot=" + mainslot);
        }
        return subId;
    }
}
