package com.android.internal.telephony;

import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Telephony.Sms.Intents;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import com.android.internal.content.PackageMonitor;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.telephony.gsm.SmsCbConstants;
import com.google.android.mms.ContentType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public final class SmsApplication {
    private static final String BLUETOOTH_PACKAGE_NAME = "com.android.bluetooth";
    private static final String CONTACTS_PACKAGE_NAME = "com.android.contacts";
    private static final boolean DEBUG_MULTIUSER = false;
    private static final String HWSYSTEMMANAGER_PACKAGE_NAME = "com.huawei.systemmanager";
    static final String LOG_TAG = "SmsApplication";
    private static final String MMS_PACKAGE_NAME = "com.android.mms";
    private static final String MMS_SERVICE_PACKAGE_NAME = "com.android.mms.service";
    private static final String PHONE_PACKAGE_NAME = "com.android.phone";
    private static final String SCHEME_MMS = "mms";
    private static final String SCHEME_MMSTO = "mmsto";
    private static final String SCHEME_SMS = "sms";
    private static final String SCHEME_SMSTO = "smsto";
    private static final String TELEPHONY_PROVIDER_PACKAGE_NAME = "com.android.providers.telephony";
    private static SmsPackageMonitor sSmsPackageMonitor;

    public static class SmsApplicationData {
        public String mApplicationName;
        public String mMmsReceiverClass;
        public String mPackageName;
        public String mProviderChangedReceiverClass;
        public String mRespondViaMessageClass;
        public String mSendToClass;
        public String mSmsAppChangedReceiverClass;
        public String mSmsReceiverClass;
        public int mUid;

        public boolean isComplete() {
            if (this.mSmsReceiverClass == null || this.mMmsReceiverClass == null || this.mRespondViaMessageClass == null || this.mSendToClass == null) {
                return SmsApplication.DEBUG_MULTIUSER;
            }
            return true;
        }

        public SmsApplicationData(String applicationName, String packageName, int uid) {
            this.mApplicationName = applicationName;
            this.mPackageName = packageName;
            this.mUid = uid;
        }

        public String toString() {
            return "mApplicationName: " + this.mApplicationName + " mPackageName: " + this.mPackageName + " mSmsReceiverClass: " + this.mSmsReceiverClass + " mMmsReceiverClass: " + this.mMmsReceiverClass + " mRespondViaMessageClass: " + this.mRespondViaMessageClass + " mSendToClass: " + this.mSendToClass + " mSmsAppChangedClass: " + this.mSmsAppChangedReceiverClass + " mProviderChangedReceiverClass: " + this.mProviderChangedReceiverClass + " mUid: " + this.mUid;
        }
    }

    private static final class SmsPackageMonitor extends PackageMonitor {
        final Context mContext;

        public SmsPackageMonitor(Context context) {
            this.mContext = context;
        }

        public void onPackageDisappeared(String packageName, int reason) {
            onPackageChanged();
        }

        public void onPackageAppeared(String packageName, int reason) {
            onPackageChanged();
        }

        public void onPackageModified(String packageName) {
            onPackageChanged();
        }

        private void onPackageChanged() {
            PackageManager packageManager = this.mContext.getPackageManager();
            Context userContext = this.mContext;
            int userId = getSendingUserId();
            if (userId != 0) {
                try {
                    userContext = this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, new UserHandle(userId));
                } catch (NameNotFoundException e) {
                }
            }
            ComponentName componentName = SmsApplication.getDefaultSendToApplication(userContext, true);
            if (componentName != null) {
                SmsApplication.configurePreferredActivity(packageManager, componentName, userId);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.SmsApplication.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.SmsApplication.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SmsApplication.<clinit>():void");
    }

    private static int getIncomingUserId(Context context) {
        int contextUserId = context.getUserId();
        int callingUid = Binder.getCallingUid();
        if (UserHandle.getAppId(callingUid) < 10000) {
            return contextUserId;
        }
        return UserHandle.getUserId(callingUid);
    }

    public static Collection<SmsApplicationData> getApplicationCollection(Context context) {
        int userId = getIncomingUserId(context);
        long token = Binder.clearCallingIdentity();
        try {
            Collection<SmsApplicationData> applicationCollectionInternal = getApplicationCollectionInternal(context, userId);
            return applicationCollectionInternal;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private static Collection<SmsApplicationData> getApplicationCollectionInternal(Context context, int userId) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> smsReceivers = packageManager.queryBroadcastReceiversAsUser(new Intent(Intents.SMS_DELIVER_ACTION), 0, userId);
        HashMap<String, SmsApplicationData> receivers = new HashMap();
        for (ResolveInfo resolveInfo : smsReceivers) {
            String packageName;
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo != null) {
                if ("android.permission.BROADCAST_SMS".equals(activityInfo.permission)) {
                    packageName = activityInfo.packageName;
                    if (!receivers.containsKey(packageName)) {
                        SmsApplicationData smsApplicationData = new SmsApplicationData(resolveInfo.loadLabel(packageManager).toString(), packageName, activityInfo.applicationInfo.uid);
                        smsApplicationData.mSmsReceiverClass = activityInfo.name;
                        receivers.put(packageName, smsApplicationData);
                    }
                }
            }
        }
        Intent intent = new Intent(Intents.WAP_PUSH_DELIVER_ACTION);
        intent.setDataAndType(null, ContentType.MMS_MESSAGE);
        for (ResolveInfo resolveInfo2 : packageManager.queryBroadcastReceiversAsUser(intent, 0, userId)) {
            SmsApplicationData smsApplicationData2;
            activityInfo = resolveInfo2.activityInfo;
            if (activityInfo != null) {
                if ("android.permission.BROADCAST_WAP_PUSH".equals(activityInfo.permission)) {
                    smsApplicationData2 = (SmsApplicationData) receivers.get(activityInfo.packageName);
                    if (smsApplicationData2 != null) {
                        smsApplicationData2.mMmsReceiverClass = activityInfo.name;
                    }
                }
            }
        }
        for (ResolveInfo resolveInfo22 : packageManager.queryIntentServicesAsUser(new Intent("android.intent.action.RESPOND_VIA_MESSAGE", Uri.fromParts(SCHEME_SMSTO, "", null)), 0, userId)) {
            ServiceInfo serviceInfo = resolveInfo22.serviceInfo;
            if (serviceInfo != null) {
                if ("android.permission.SEND_RESPOND_VIA_MESSAGE".equals(serviceInfo.permission)) {
                    smsApplicationData2 = (SmsApplicationData) receivers.get(serviceInfo.packageName);
                    if (smsApplicationData2 != null) {
                        smsApplicationData2.mRespondViaMessageClass = serviceInfo.name;
                    }
                }
            }
        }
        for (ResolveInfo resolveInfo222 : packageManager.queryIntentActivitiesAsUser(new Intent("android.intent.action.SENDTO", Uri.fromParts(SCHEME_SMSTO, "", null)), 0, userId)) {
            activityInfo = resolveInfo222.activityInfo;
            if (activityInfo != null) {
                smsApplicationData2 = (SmsApplicationData) receivers.get(activityInfo.packageName);
                if (smsApplicationData2 != null) {
                    smsApplicationData2.mSendToClass = activityInfo.name;
                }
            }
        }
        for (ResolveInfo resolveInfo2222 : packageManager.queryBroadcastReceiversAsUser(new Intent(Intents.ACTION_DEFAULT_SMS_PACKAGE_CHANGED), 0, userId)) {
            activityInfo = resolveInfo2222.activityInfo;
            if (activityInfo != null) {
                smsApplicationData2 = (SmsApplicationData) receivers.get(activityInfo.packageName);
                if (smsApplicationData2 != null) {
                    smsApplicationData2.mSmsAppChangedReceiverClass = activityInfo.name;
                }
            }
        }
        for (ResolveInfo resolveInfo22222 : packageManager.queryBroadcastReceiversAsUser(new Intent(Intents.ACTION_EXTERNAL_PROVIDER_CHANGE), 0, userId)) {
            activityInfo = resolveInfo22222.activityInfo;
            if (activityInfo != null) {
                smsApplicationData2 = (SmsApplicationData) receivers.get(activityInfo.packageName);
                if (smsApplicationData2 != null) {
                    smsApplicationData2.mProviderChangedReceiverClass = activityInfo.name;
                }
            }
        }
        for (ResolveInfo resolveInfo222222 : smsReceivers) {
            activityInfo = resolveInfo222222.activityInfo;
            if (activityInfo != null) {
                packageName = activityInfo.packageName;
                smsApplicationData2 = (SmsApplicationData) receivers.get(packageName);
                if (!(smsApplicationData2 == null || smsApplicationData2.isComplete())) {
                    receivers.remove(packageName);
                }
            }
        }
        return receivers.values();
    }

    private static SmsApplicationData getApplicationForPackage(Collection<SmsApplicationData> applications, String packageName) {
        if (packageName == null) {
            return null;
        }
        for (SmsApplicationData application : applications) {
            if (application.mPackageName.contentEquals(packageName)) {
                return application;
            }
        }
        return null;
    }

    private static SmsApplicationData getApplication(Context context, boolean updateIfNeeded, int userId) {
        if (!((TelephonyManager) context.getSystemService("phone")).isSmsCapable()) {
            return null;
        }
        Collection<SmsApplicationData> applications = getApplicationCollectionInternal(context, userId);
        String defaultApplication = Secure.getStringForUser(context.getContentResolver(), "sms_default_application", userId);
        SmsApplicationData applicationData = null;
        if (defaultApplication != null) {
            applicationData = getApplicationForPackage(applications, defaultApplication);
        }
        if (updateIfNeeded && r1 == null) {
            applicationData = getApplicationForPackage(applications, context.getResources().getString(17039430));
            if (applicationData == null && applications.size() != 0) {
                applicationData = applications.toArray()[0];
            }
            if (applicationData != null) {
                setDefaultApplicationInternal(applicationData.mPackageName, context, userId);
            }
        }
        if (applicationData != null) {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService("appops");
            if ((updateIfNeeded || applicationData.mUid == Process.myUid()) && appOps.checkOp(15, applicationData.mUid, applicationData.mPackageName) != 0) {
                Rlog.e(LOG_TAG, applicationData.mPackageName + " lost OP_WRITE_SMS: " + (updateIfNeeded ? " (fixing)" : " (no permission to fix)"));
                if (updateIfNeeded) {
                    appOps.setMode(15, applicationData.mUid, applicationData.mPackageName, 0);
                } else {
                    applicationData = null;
                }
            }
            if (updateIfNeeded) {
                PackageManager packageManager = context.getPackageManager();
                configurePreferredActivity(packageManager, new ComponentName(applicationData.mPackageName, applicationData.mSendToClass), userId);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, PHONE_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, BLUETOOTH_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, MMS_SERVICE_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, TELEPHONY_PROVIDER_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemUid(appOps, TelephonyEventLog.TAG_RIL_REQUEST);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, HWSYSTEMMANAGER_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, CONTACTS_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, MMS_PACKAGE_NAME);
            }
        }
        return applicationData;
    }

    public static void setDefaultApplication(String packageName, Context context) {
        if (!HwTelephonyFactory.getHwInnerSmsManager().shouldSetDefaultApplicationForPackage(packageName, context)) {
            Rlog.d(LOG_TAG, "packageName " + packageName + "is not allowed to set to default application, return.");
        } else if (((TelephonyManager) context.getSystemService("phone")).isSmsCapable()) {
            int userId = getIncomingUserId(context);
            long token = Binder.clearCallingIdentity();
            try {
                setDefaultApplicationInternal(packageName, context, userId);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    private static void setDefaultApplicationInternal(String packageName, Context context, int userId) {
        String oldPackageName = Secure.getStringForUser(context.getContentResolver(), "sms_default_application", userId);
        if (packageName == null || oldPackageName == null || !packageName.equals(oldPackageName)) {
            PackageManager packageManager = context.getPackageManager();
            Collection<SmsApplicationData> applications = getApplicationCollection(context);
            SmsApplicationData applicationForPackage = oldPackageName != null ? getApplicationForPackage(applications, oldPackageName) : null;
            SmsApplicationData applicationData = getApplicationForPackage(applications, packageName);
            if (applicationData != null) {
                AppOpsManager appOps = (AppOpsManager) context.getSystemService("appops");
                if (oldPackageName != null) {
                    try {
                        appOps.setMode(15, packageManager.getPackageInfo(oldPackageName, SmsCbConstants.SERIAL_NUMBER_ETWS_EMERGENCY_USER_ALERT).applicationInfo.uid, oldPackageName, 1);
                    } catch (NameNotFoundException e) {
                        Rlog.w(LOG_TAG, "Old SMS package not found: " + oldPackageName);
                    }
                }
                Secure.putStringForUser(context.getContentResolver(), "sms_default_application", applicationData.mPackageName, userId);
                configurePreferredActivity(packageManager, new ComponentName(applicationData.mPackageName, applicationData.mSendToClass), userId);
                appOps.setMode(15, applicationData.mUid, applicationData.mPackageName, 0);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, PHONE_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, BLUETOOTH_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, MMS_SERVICE_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, TELEPHONY_PROVIDER_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemUid(appOps, TelephonyEventLog.TAG_RIL_REQUEST);
                if (!(applicationForPackage == null || applicationForPackage.mSmsAppChangedReceiverClass == null)) {
                    Intent oldAppIntent = new Intent(Intents.ACTION_DEFAULT_SMS_PACKAGE_CHANGED);
                    oldAppIntent.setComponent(new ComponentName(applicationForPackage.mPackageName, applicationForPackage.mSmsAppChangedReceiverClass));
                    oldAppIntent.putExtra(Intents.EXTRA_IS_DEFAULT_SMS_APP, DEBUG_MULTIUSER);
                    context.sendBroadcast(oldAppIntent);
                }
                if (applicationData.mSmsAppChangedReceiverClass != null) {
                    Intent intent = new Intent(Intents.ACTION_DEFAULT_SMS_PACKAGE_CHANGED);
                    intent.setComponent(new ComponentName(applicationData.mPackageName, applicationData.mSmsAppChangedReceiverClass));
                    intent.putExtra(Intents.EXTRA_IS_DEFAULT_SMS_APP, true);
                    context.sendBroadcast(intent);
                }
                MetricsLogger.action(context, 266, applicationData.mPackageName);
                context.sendBroadcast(new Intent("com.android.telephony.DEFAULT_SMS_CHANGED"));
            }
        }
    }

    private static void assignWriteSmsPermissionToSystemApp(Context context, PackageManager packageManager, AppOpsManager appOps, String packageName) {
        if (packageManager.checkSignatures(context.getPackageName(), packageName) == 0 || HwTelephonyFactory.getHwInnerSmsManager().allowToSetSmsWritePermission(packageName)) {
            try {
                PackageInfo info = packageManager.getPackageInfo(packageName, 0);
                if (appOps.checkOp(15, info.applicationInfo.uid, packageName) != 0) {
                    Rlog.w(LOG_TAG, packageName + " does not have OP_WRITE_SMS:  (fixing)");
                    appOps.setMode(15, info.applicationInfo.uid, packageName, 0);
                }
            } catch (NameNotFoundException e) {
                Rlog.e(LOG_TAG, "Package not found: " + packageName);
            }
            return;
        }
        Rlog.e(LOG_TAG, packageName + " does not have system signature");
    }

    private static void assignWriteSmsPermissionToSystemUid(AppOpsManager appOps, int uid) {
        appOps.setUidMode(15, uid, 0);
    }

    public static void initSmsPackageMonitor(Context context) {
        sSmsPackageMonitor = new SmsPackageMonitor(context);
        sSmsPackageMonitor.register(context, context.getMainLooper(), UserHandle.ALL, DEBUG_MULTIUSER);
    }

    private static void configurePreferredActivity(PackageManager packageManager, ComponentName componentName, int userId) {
        replacePreferredActivity(packageManager, componentName, userId, SCHEME_SMS);
        replacePreferredActivity(packageManager, componentName, userId, SCHEME_SMSTO);
        replacePreferredActivity(packageManager, componentName, userId, SCHEME_MMS);
        replacePreferredActivity(packageManager, componentName, userId, SCHEME_MMSTO);
    }

    private static void replacePreferredActivity(PackageManager packageManager, ComponentName componentName, int userId, String scheme) {
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivitiesAsUser(new Intent("android.intent.action.SENDTO", Uri.fromParts(scheme, "", null)), 65600, userId);
        int n = resolveInfoList.size();
        ComponentName[] set = new ComponentName[n];
        for (int i = 0; i < n; i++) {
            ResolveInfo info = (ResolveInfo) resolveInfoList.get(i);
            set[i] = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SENDTO");
        intentFilter.addCategory("android.intent.category.DEFAULT");
        intentFilter.addDataScheme(scheme);
        packageManager.replacePreferredActivityAsUser(intentFilter, 2129920, set, componentName, userId);
    }

    public static SmsApplicationData getSmsApplicationData(String packageName, Context context) {
        return getApplicationForPackage(getApplicationCollection(context), packageName);
    }

    public static ComponentName getDefaultSmsApplication(Context context, boolean updateIfNeeded) {
        int userId = getIncomingUserId(context);
        long token = Binder.clearCallingIdentity();
        ComponentName componentName = null;
        try {
            SmsApplicationData smsApplicationData = getApplication(context, updateIfNeeded, userId);
            if (smsApplicationData != null) {
                componentName = new ComponentName(smsApplicationData.mPackageName, smsApplicationData.mSmsReceiverClass);
            }
            Binder.restoreCallingIdentity(token);
            return componentName;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static ComponentName getDefaultMmsApplication(Context context, boolean updateIfNeeded) {
        int userId = getIncomingUserId(context);
        long token = Binder.clearCallingIdentity();
        ComponentName componentName = null;
        try {
            SmsApplicationData smsApplicationData = getApplication(context, updateIfNeeded, userId);
            if (smsApplicationData != null) {
                componentName = new ComponentName(smsApplicationData.mPackageName, smsApplicationData.mMmsReceiverClass);
            }
            Binder.restoreCallingIdentity(token);
            return componentName;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static ComponentName getDefaultRespondViaMessageApplication(Context context, boolean updateIfNeeded) {
        int userId = getIncomingUserId(context);
        long token = Binder.clearCallingIdentity();
        ComponentName componentName = null;
        try {
            SmsApplicationData smsApplicationData = getApplication(context, updateIfNeeded, userId);
            if (smsApplicationData != null) {
                componentName = new ComponentName(smsApplicationData.mPackageName, smsApplicationData.mRespondViaMessageClass);
            }
            Binder.restoreCallingIdentity(token);
            return componentName;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static ComponentName getDefaultSendToApplication(Context context, boolean updateIfNeeded) {
        int userId = getIncomingUserId(context);
        long token = Binder.clearCallingIdentity();
        ComponentName componentName = null;
        try {
            SmsApplicationData smsApplicationData = getApplication(context, updateIfNeeded, userId);
            if (smsApplicationData != null) {
                componentName = new ComponentName(smsApplicationData.mPackageName, smsApplicationData.mSendToClass);
            }
            Binder.restoreCallingIdentity(token);
            return componentName;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static ComponentName getDefaultExternalTelephonyProviderChangedApplication(Context context, boolean updateIfNeeded) {
        int userId = getIncomingUserId(context);
        long token = Binder.clearCallingIdentity();
        ComponentName componentName = null;
        try {
            SmsApplicationData smsApplicationData = getApplication(context, updateIfNeeded, userId);
            if (!(smsApplicationData == null || smsApplicationData.mProviderChangedReceiverClass == null)) {
                componentName = new ComponentName(smsApplicationData.mPackageName, smsApplicationData.mProviderChangedReceiverClass);
            }
            Binder.restoreCallingIdentity(token);
            return componentName;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static boolean shouldWriteMessageForPackage(String packageName, Context context) {
        boolean z = true;
        if (SmsManager.getDefault().getAutoPersisting()) {
            return true;
        }
        if (isDefaultSmsApplication(context, packageName)) {
            z = DEBUG_MULTIUSER;
        }
        return z;
    }

    public static boolean isDefaultSmsApplication(Context context, String packageName) {
        if (packageName == null) {
            return DEBUG_MULTIUSER;
        }
        String defaultSmsPackage = getDefaultSmsApplicationPackageName(context);
        if ((defaultSmsPackage == null || !defaultSmsPackage.equals(packageName)) && !BLUETOOTH_PACKAGE_NAME.equals(packageName)) {
            return DEBUG_MULTIUSER;
        }
        return true;
    }

    private static String getDefaultSmsApplicationPackageName(Context context) {
        ComponentName component = getDefaultSmsApplication(context, DEBUG_MULTIUSER);
        if (component != null) {
            return component.getPackageName();
        }
        return null;
    }
}
