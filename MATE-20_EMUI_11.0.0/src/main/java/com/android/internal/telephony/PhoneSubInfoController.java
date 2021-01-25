package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.app.AppOpsManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.ImsiEncryptionInfo;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.android.internal.telephony.IPhoneSubInfo;
import com.android.internal.telephony.uicc.IsimRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import huawei.android.security.IHwBehaviorCollectManager;

public class PhoneSubInfoController extends IPhoneSubInfo.Stub {
    private static final boolean DBG = true;
    public static final boolean IS_QCRIL_CROSS_MAPPING = SystemProperties.getBoolean("ro.hwpp.qcril_cross_mapping", false);
    private static final String MESSAGE_GET_IMEI = "getImei";
    public static final int SUB1 = 0;
    public static final int SUB2 = 1;
    private static final String TAG = "PhoneSubInfoController";
    private static final boolean VDBG = false;
    private final AppOpsManager mAppOps;
    @UnsupportedAppUsage
    private final Context mContext;
    @UnsupportedAppUsage
    private final Phone[] mPhone;

    /* access modifiers changed from: private */
    public interface CallPhoneMethodHelper<T> {
        T callMethod(Phone phone);
    }

    /* access modifiers changed from: private */
    public interface PermissionCheckHelper {
        boolean checkPermission(Context context, int i, String str, String str2);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.internal.telephony.PhoneSubInfoController */
    /* JADX WARN: Multi-variable type inference failed */
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
        return (String) callPhoneMethodForPhoneIdWithReadDeviceIdentifiersCheck(phoneId, callingPackage, "getDeviceId", $$Lambda$PhoneSubInfoController$LX6rN0XZFTVXkDiHGVCozgs8kHU.INSTANCE);
    }

    public String getNaiForSubscriber(int subId, String callingPackage) {
        return (String) callPhoneMethodForSubIdWithReadCheck(subId, callingPackage, "getNai", $$Lambda$PhoneSubInfoController$AAs5l6UPqOJI6iOy7O7wnhNgpN4.INSTANCE);
    }

    public String getImeiForSubscriber(int subId, String callingPackage) {
        return (String) callPhoneMethodForSubIdWithReadDeviceIdentifiersCheck(subId, callingPackage, MESSAGE_GET_IMEI, $$Lambda$PhoneSubInfoController$_djiy1W26lRIJyfoQefqkIQNgSU.INSTANCE);
    }

    public ImsiEncryptionInfo getCarrierInfoForImsiEncryption(int subId, int keyType, String callingPackage) {
        return (ImsiEncryptionInfo) callPhoneMethodForSubIdWithReadCheck(subId, callingPackage, "getCarrierInfoForImsiEncryption", new CallPhoneMethodHelper(keyType) {
            /* class com.android.internal.telephony.$$Lambda$PhoneSubInfoController$AjZFvwh3Ujx5W3fleFNksc6bLf0 */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.android.internal.telephony.PhoneSubInfoController.CallPhoneMethodHelper
            public final Object callMethod(Phone phone) {
                return phone.getCarrierInfoForImsiEncryption(this.f$0);
            }
        });
    }

    public void setCarrierInfoForImsiEncryption(int subId, String callingPackage, ImsiEncryptionInfo imsiEncryptionInfo) {
        callPhoneMethodForSubIdWithModifyCheck(subId, callingPackage, "setCarrierInfoForImsiEncryption", new CallPhoneMethodHelper(imsiEncryptionInfo) {
            /* class com.android.internal.telephony.$$Lambda$PhoneSubInfoController$ChCf_gnGN3K5prBkykg6tWs0aTk */
            private final /* synthetic */ ImsiEncryptionInfo f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.android.internal.telephony.PhoneSubInfoController.CallPhoneMethodHelper
            public final Object callMethod(Phone phone) {
                return phone.setCarrierInfoForImsiEncryption(this.f$0);
            }
        });
    }

    public void resetCarrierKeysForImsiEncryption(int subId, String callingPackage) {
        callPhoneMethodForSubIdWithModifyCheck(subId, callingPackage, "setCarrierInfoForImsiEncryption", $$Lambda$PhoneSubInfoController$Pb4HmeqsjasrNaXBByGh_CFogk.INSTANCE);
    }

    public String getDeviceSvn(String callingPackage) {
        return getDeviceSvnUsingSubId(getDefaultSubscription(), callingPackage);
    }

    public String getDeviceSvnUsingSubId(int subId, String callingPackage) {
        return (String) callPhoneMethodForSubIdWithReadCheck(subId, callingPackage, "getDeviceSvn", $$Lambda$PhoneSubInfoController$VgStcgP2F9IDb29Rx_E2o89A7U.INSTANCE);
    }

    public String getSubscriberId(String callingPackage) {
        return getSubscriberIdForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getSubscriberIdForSubscriber(int subId, String callingPackage) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.TELEPHONY_GETSUBSCRIBERIDFORSUBSCRIBER);
        }
        long identity = Binder.clearCallingIdentity();
        try {
            if (SubscriptionController.getInstance().isActiveSubId(subId, callingPackage)) {
                return (String) callPhoneMethodForSubIdWithReadSubscriberIdentifiersCheck(subId, callingPackage, "getSubscriberId", $$Lambda$PhoneSubInfoController$2WGP2Bp11k7_Xwi1N4YefElOUuM.INSTANCE);
            }
            if (!TelephonyPermissions.checkCallingOrSelfReadSubscriberIdentifiers(this.mContext, subId, callingPackage, "getSubscriberId")) {
                return null;
            }
            long identity2 = Binder.clearCallingIdentity();
            try {
                return SubscriptionController.getInstance().getImsiPrivileged(subId);
            } finally {
                Binder.restoreCallingIdentity(identity2);
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public String getIccSerialNumber(String callingPackage) {
        return getIccSerialNumberForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getIccSerialNumberForSubscriber(int subId, String callingPackage) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.TELEPHONY_GETICCSERIALNUMBERFORSUBSCRIBER);
        }
        return (String) callPhoneMethodForSubIdWithReadSubscriberIdentifiersCheck(subId, callingPackage, "getIccSerialNumber", $$Lambda$PhoneSubInfoController$1zkPy06BwndFkKrGCUI1ORIPJcI.INSTANCE);
    }

    public String getLine1Number(String callingPackage) {
        return getLine1NumberForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getLine1NumberForSubscriber(int subId, String callingPackage) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.TELEPHONY_GETLINE1NUMBERFORSUBSCRIBER);
        }
        return (String) callPhoneMethodForSubIdWithReadPhoneNumberCheck(subId, callingPackage, "getLine1Number", $$Lambda$PhoneSubInfoController$P0j9hvO3eUE9_1i1QM_ujl8Bpo.INSTANCE);
    }

    public String getLine1AlphaTag(String callingPackage) {
        return getLine1AlphaTagForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getLine1AlphaTagForSubscriber(int subId, String callingPackage) {
        return (String) callPhoneMethodForSubIdWithReadCheck(subId, callingPackage, "getLine1AlphaTag", $$Lambda$PhoneSubInfoController$hh4N6_N4PPm_vWjCdCRvS8Cw.INSTANCE);
    }

    public String getMsisdn(String callingPackage) {
        return getMsisdnForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getMsisdnForSubscriber(int subId, String callingPackage) {
        return (String) callPhoneMethodForSubIdWithReadCheck(subId, callingPackage, "getMsisdn", $$Lambda$PhoneSubInfoController$dmWmchcWksZlUJPg5OfrbagSrA.INSTANCE);
    }

    public String getVoiceMailNumber(String callingPackage) {
        return getVoiceMailNumberForSubscriber(getDefaultSubscription(), callingPackage);
    }

    public String getVoiceMailNumberForSubscriber(int subId, String callingPackage) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.TELEPHONY_GETVOICEMAILNUMBERFORSUBSCRIBER);
        }
        return (String) callPhoneMethodForSubIdWithReadCheck(subId, callingPackage, "getVoiceMailNumber", new CallPhoneMethodHelper() {
            /* class com.android.internal.telephony.$$Lambda$PhoneSubInfoController$Ja9yTBcEYPqTRBIPhL0otixVeE */

            @Override // com.android.internal.telephony.PhoneSubInfoController.CallPhoneMethodHelper
            public final Object callMethod(Phone phone) {
                return PhoneSubInfoController.this.lambda$getVoiceMailNumberForSubscriber$12$PhoneSubInfoController(phone);
            }
        });
    }

    public /* synthetic */ String lambda$getVoiceMailNumberForSubscriber$12$PhoneSubInfoController(Phone phone) {
        return PhoneNumberUtils.extractNetworkPortion(phone.getVoiceMailNumber());
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
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.TELEPHONY_GETVOICEMAILALPHATAGFORSUBSCRIBER);
        }
        return (String) callPhoneMethodForSubIdWithReadCheck(subId, callingPackage, "getVoiceMailAlphaTag", $$Lambda$PhoneSubInfoController$oLIrumQtrxqYONQeIeqNtbJdJMU.INSTANCE);
    }

    @UnsupportedAppUsage
    private Phone getPhone(int subId) {
        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            phoneId = 0;
        }
        return this.mPhone[phoneId];
    }

    private void enforcePrivilegedPermissionOrCarrierPrivilege(int subId, String message) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE") != 0) {
            TelephonyPermissions.enforceCallingOrSelfCarrierPrivilege(subId, message);
        }
    }

    private void enforceModifyPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "Requires MODIFY_PHONE_STATE");
    }

    @UnsupportedAppUsage
    private int getDefaultSubscription() {
        return PhoneFactory.getDefaultSubscription();
    }

    public String getIsimImpi(int subId) {
        return (String) callPhoneMethodForSubIdWithPrivilegedCheck(subId, "getIsimImpi", $$Lambda$PhoneSubInfoController$UaKjkq7sTW3Fbf04O086aBFm63M.INSTANCE);
    }

    static /* synthetic */ String lambda$getIsimImpi$14(Phone phone) {
        IsimRecords isim = phone.getIsimRecords();
        if (isim != null) {
            return isim.getIsimImpi();
        }
        return null;
    }

    public String getIsimDomain(int subId) {
        return (String) callPhoneMethodForSubIdWithPrivilegedCheck(subId, "getIsimDomain", $$Lambda$PhoneSubInfoController$ZOtVAnuhxrXl2L906I6eTOentP0.INSTANCE);
    }

    static /* synthetic */ String lambda$getIsimDomain$15(Phone phone) {
        IsimRecords isim = phone.getIsimRecords();
        if (isim != null) {
            return isim.getIsimDomain();
        }
        return null;
    }

    public String[] getIsimImpu(int subId) {
        return (String[]) callPhoneMethodForSubIdWithPrivilegedCheck(subId, "getIsimImpu", $$Lambda$PhoneSubInfoController$2xgrYNleR8FFzFT8hEQx3mDtZ8g.INSTANCE);
    }

    static /* synthetic */ String[] lambda$getIsimImpu$16(Phone phone) {
        IsimRecords isim = phone.getIsimRecords();
        if (isim != null) {
            return isim.getIsimImpu();
        }
        return null;
    }

    public String getIsimIst(int subId) throws RemoteException {
        return (String) callPhoneMethodForSubIdWithPrivilegedCheck(subId, "getIsimIst", $$Lambda$PhoneSubInfoController$rpyQeO7zACcc5v4krwU9_qRMHL8.INSTANCE);
    }

    static /* synthetic */ String lambda$getIsimIst$17(Phone phone) {
        IsimRecords isim = phone.getIsimRecords();
        if (isim != null) {
            return isim.getIsimIst();
        }
        return null;
    }

    public String[] getIsimPcscf(int subId) throws RemoteException {
        return (String[]) callPhoneMethodForSubIdWithPrivilegedCheck(subId, "getIsimPcscf", $$Lambda$PhoneSubInfoController$9_e7IQZG40sfOlFgD3_7E7x3p4o.INSTANCE);
    }

    static /* synthetic */ String[] lambda$getIsimPcscf$18(Phone phone) {
        IsimRecords isim = phone.getIsimRecords();
        if (isim != null) {
            return isim.getIsimPcscf();
        }
        return null;
    }

    public String getIccSimChallengeResponse(int subId, int appType, int authType, String data) throws RemoteException {
        return (String) callPhoneMethodWithPermissionCheck(subId, null, "getIccSimChallengeResponse", new CallPhoneMethodHelper(appType, authType, data) {
            /* class com.android.internal.telephony.$$Lambda$PhoneSubInfoController$16zFa5X_HsO5oSaupKDtHL0 */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ String f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.internal.telephony.PhoneSubInfoController.CallPhoneMethodHelper
            public final Object callMethod(Phone phone) {
                return PhoneSubInfoController.this.lambda$getIccSimChallengeResponse$19$PhoneSubInfoController(this.f$1, this.f$2, this.f$3, phone);
            }
        }, new PermissionCheckHelper() {
            /* class com.android.internal.telephony.$$Lambda$PhoneSubInfoController$NO5HxAafVP54fe9chLZKTACeyU */

            @Override // com.android.internal.telephony.PhoneSubInfoController.PermissionCheckHelper
            public final boolean checkPermission(Context context, int i, String str, String str2) {
                return PhoneSubInfoController.this.lambda$getIccSimChallengeResponse$20$PhoneSubInfoController(context, i, str, str2);
            }
        });
    }

    public /* synthetic */ String lambda$getIccSimChallengeResponse$19$PhoneSubInfoController(int appType, int authType, String data, Phone phone) {
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

    public /* synthetic */ boolean lambda$getIccSimChallengeResponse$20$PhoneSubInfoController(Context aContext, int aSubId, String aCallingPackage, String aMessage) {
        enforcePrivilegedPermissionOrCarrierPrivilege(aSubId, aMessage);
        return true;
    }

    public String getGroupIdLevel1ForSubscriber(int subId, String callingPackage) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.TELEPHONY_GETGROUPIDLEVEL1FORSUBSCRIBER);
        }
        return (String) callPhoneMethodForSubIdWithReadCheck(subId, callingPackage, "getGroupIdLevel1", $$Lambda$PhoneSubInfoController$bWluhZvk2XdQ0UidKfdpd0kwuw.INSTANCE);
    }

    private <T> T callPhoneMethodWithPermissionCheck(int subId, String callingPackage, String message, CallPhoneMethodHelper<T> callMethodHelper, PermissionCheckHelper permissionCheckHelper) {
        try {
            if (!permissionCheckHelper.checkPermission(this.mContext, subId, callingPackage, message)) {
                return null;
            }
        } catch (SecurityException phoneStateException) {
            if (!MESSAGE_GET_IMEI.equals(message) || !isSystemApp(callingPackage) || !isSystemAppByUid()) {
                throw phoneStateException;
            }
        }
        long identity = Binder.clearCallingIdentity();
        try {
            Phone phone = getPhone(subId);
            if (phone != null) {
                return callMethodHelper.callMethod(phone);
            }
            loge(message + " phone is null for Subscription:" + subId);
            Binder.restoreCallingIdentity(identity);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private <T> T callPhoneMethodForSubIdWithReadCheck(int subId, String callingPackage, String message, CallPhoneMethodHelper<T> callMethodHelper) {
        return (T) callPhoneMethodWithPermissionCheck(subId, callingPackage, message, callMethodHelper, $$Lambda$PhoneSubInfoController$qSXnUMuIwAZ0TQjtyVEfznh1w8o.INSTANCE);
    }

    private <T> T callPhoneMethodForSubIdWithReadDeviceIdentifiersCheck(int subId, String callingPackage, String message, CallPhoneMethodHelper<T> callMethodHelper) {
        return (T) callPhoneMethodWithPermissionCheck(subId, callingPackage, message, callMethodHelper, $$Lambda$PhoneSubInfoController$qVe7IcEgdBIfOarHqDJP3ePBBcI.INSTANCE);
    }

    private <T> T callPhoneMethodForSubIdWithReadSubscriberIdentifiersCheck(int subId, String callingPackage, String message, CallPhoneMethodHelper<T> callMethodHelper) {
        return (T) callPhoneMethodWithPermissionCheck(subId, callingPackage, message, callMethodHelper, $$Lambda$PhoneSubInfoController$EYZUPU0CYhRoptGCGJ9y78ujQM.INSTANCE);
    }

    private <T> T callPhoneMethodForSubIdWithPrivilegedCheck(int subId, String message, CallPhoneMethodHelper<T> callMethodHelper) {
        return (T) callPhoneMethodWithPermissionCheck(subId, null, message, callMethodHelper, new PermissionCheckHelper(message) {
            /* class com.android.internal.telephony.$$Lambda$PhoneSubInfoController$PONge0j2mBi_ILbtJD_7euF0uoM */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.PhoneSubInfoController.PermissionCheckHelper
            public final boolean checkPermission(Context context, int i, String str, String str2) {
                return PhoneSubInfoController.this.lambda$callPhoneMethodForSubIdWithPrivilegedCheck$25$PhoneSubInfoController(this.f$1, context, i, str, str2);
            }
        });
    }

    public /* synthetic */ boolean lambda$callPhoneMethodForSubIdWithPrivilegedCheck$25$PhoneSubInfoController(String message, Context aContext, int aSubId, String aCallingPackage, String aMessage) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", message);
        return true;
    }

    private <T> T callPhoneMethodForSubIdWithModifyCheck(int subId, String callingPackage, String message, CallPhoneMethodHelper<T> callMethodHelper) {
        return (T) callPhoneMethodWithPermissionCheck(subId, null, message, callMethodHelper, new PermissionCheckHelper() {
            /* class com.android.internal.telephony.$$Lambda$PhoneSubInfoController$OJMEn1lB_IZwTxTEU9sWCr__XKs */

            @Override // com.android.internal.telephony.PhoneSubInfoController.PermissionCheckHelper
            public final boolean checkPermission(Context context, int i, String str, String str2) {
                return PhoneSubInfoController.this.lambda$callPhoneMethodForSubIdWithModifyCheck$26$PhoneSubInfoController(context, i, str, str2);
            }
        });
    }

    public /* synthetic */ boolean lambda$callPhoneMethodForSubIdWithModifyCheck$26$PhoneSubInfoController(Context aContext, int aSubId, String aCallingPackage, String aMessage) {
        enforceModifyPermission();
        return true;
    }

    private <T> T callPhoneMethodForSubIdWithReadPhoneNumberCheck(int subId, String callingPackage, String message, CallPhoneMethodHelper<T> callMethodHelper) {
        return (T) callPhoneMethodWithPermissionCheck(subId, callingPackage, message, callMethodHelper, $$Lambda$PhoneSubInfoController$1TnOMFYcM13ZTJNoLjxguPwVcxw.INSTANCE);
    }

    private <T> T callPhoneMethodForPhoneIdWithReadDeviceIdentifiersCheck(int phoneId, String callingPackage, String message, CallPhoneMethodHelper<T> callMethodHelper) {
        int phoneId2 = getIMEIExProcess(phoneId);
        if (!SubscriptionManager.isValidPhoneId(phoneId2)) {
            phoneId2 = 0;
        }
        Phone phone = this.mPhone[phoneId2];
        if (phone == null) {
            return null;
        }
        try {
            if (!TelephonyPermissions.checkCallingOrSelfReadDeviceIdentifiers(this.mContext, phone.getSubId(), callingPackage, message)) {
                return null;
            }
        } catch (SecurityException phoneStateException) {
            if (!isSystemApp(callingPackage) || !isSystemAppByUid()) {
                throw phoneStateException;
            }
        }
        long identity = Binder.clearCallingIdentity();
        try {
            return callMethodHelper.callMethod(phone);
        } finally {
            Binder.restoreCallingIdentity(identity);
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
        } catch (PackageManager.NameNotFoundException e) {
            loge(packageName + " not found.");
        }
        return false;
    }

    private boolean isSystemAppByUid() {
        String[] packageNames;
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null || (packageNames = pm.getPackagesForUid(Binder.getCallingUid())) == null) {
            return false;
        }
        for (String packageName : packageNames) {
            if (!isSystemApp(packageName)) {
                return false;
            }
        }
        return true;
    }

    private void log(String s) {
        Rlog.i(TAG, s);
    }

    @UnsupportedAppUsage
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
        if (!IS_QCRIL_CROSS_MAPPING && !PhoneFactory.IS_QCOM_DUAL_LTE_STACK) {
            return subId;
        }
        int mainslot = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
        int subId2 = mainslot == subId ? 0 : 1;
        Rlog.i(TAG, "getIMEIExProcess after comparesubId=" + subId2 + ",mainslot=" + mainslot);
        return subId2;
    }
}
