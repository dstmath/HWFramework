package com.android.internal.telephony;

import android.app.AppOpsManager;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto;
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
    private static final String PROP_CUST_DFLT_SMS_APP = "ro.config.default_sms_app";
    private static final String SCHEME_MMS = "mms";
    private static final String SCHEME_MMSTO = "mmsto";
    private static final String SCHEME_SMS = "sms";
    private static final String SCHEME_SMSTO = "smsto";
    private static final String TELEPHONY_PROVIDER_PACKAGE_NAME = "com.android.providers.telephony";
    private static SmsPackageMonitor sSmsPackageMonitor = null;

    public static class SmsApplicationData {
        private String mApplicationName;
        /* access modifiers changed from: private */
        public String mMmsReceiverClass;
        public String mPackageName;
        /* access modifiers changed from: private */
        public String mProviderChangedReceiverClass;
        /* access modifiers changed from: private */
        public String mRespondViaMessageClass;
        /* access modifiers changed from: private */
        public String mSendToClass;
        /* access modifiers changed from: private */
        public String mSimFullReceiverClass;
        /* access modifiers changed from: private */
        public String mSmsAppChangedReceiverClass;
        /* access modifiers changed from: private */
        public String mSmsReceiverClass;
        /* access modifiers changed from: private */
        public int mUid;

        public boolean isComplete() {
            return (this.mSmsReceiverClass == null || this.mMmsReceiverClass == null || this.mRespondViaMessageClass == null || this.mSendToClass == null) ? false : true;
        }

        public SmsApplicationData(String packageName, int uid) {
            this.mPackageName = packageName;
            this.mUid = uid;
        }

        public String getApplicationName(Context context) {
            if (this.mApplicationName == null) {
                PackageManager pm = context.getPackageManager();
                String str = null;
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfoAsUser(this.mPackageName, 0, UserHandle.getUserId(this.mUid));
                    if (appInfo != null) {
                        CharSequence label = pm.getApplicationLabel(appInfo);
                        if (label != null) {
                            str = label.toString();
                        }
                        this.mApplicationName = str;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    return null;
                }
            }
            return this.mApplicationName;
        }

        public String toString() {
            return " mPackageName: " + this.mPackageName + " mSmsReceiverClass: " + this.mSmsReceiverClass + " mMmsReceiverClass: " + this.mMmsReceiverClass + " mRespondViaMessageClass: " + this.mRespondViaMessageClass + " mSendToClass: " + this.mSendToClass + " mSmsAppChangedClass: " + this.mSmsAppChangedReceiverClass + " mProviderChangedReceiverClass: " + this.mProviderChangedReceiverClass + " mSimFullReceiverClass: " + this.mSimFullReceiverClass + " mUid: " + this.mUid;
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
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
            ComponentName componentName = SmsApplication.getDefaultSendToApplication(userContext, true);
            if (componentName != null) {
                SmsApplication.configurePreferredActivity(packageManager, componentName, userId);
            }
        }
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
            return getApplicationCollectionInternal(context, userId);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private static Collection<SmsApplicationData> getApplicationCollectionInternal(Context context, int userId) {
        int i = userId;
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> smsReceivers = packageManager.queryBroadcastReceiversAsUser(new Intent("android.provider.Telephony.SMS_DELIVER"), 0, i);
        HashMap<String, SmsApplicationData> receivers = new HashMap<>();
        for (ResolveInfo resolveInfo : smsReceivers) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo != null && "android.permission.BROADCAST_SMS".equals(activityInfo.permission)) {
                String packageName = activityInfo.packageName;
                if (!receivers.containsKey(packageName)) {
                    SmsApplicationData smsApplicationData = new SmsApplicationData(packageName, activityInfo.applicationInfo.uid);
                    String unused = smsApplicationData.mSmsReceiverClass = activityInfo.name;
                    receivers.put(packageName, smsApplicationData);
                }
            }
        }
        Intent intent = new Intent("android.provider.Telephony.WAP_PUSH_DELIVER");
        intent.setDataAndType(null, "application/vnd.wap.mms-message");
        for (ResolveInfo resolveInfo2 : packageManager.queryBroadcastReceiversAsUser(intent, 0, i)) {
            ActivityInfo activityInfo2 = resolveInfo2.activityInfo;
            if (activityInfo2 != null && "android.permission.BROADCAST_WAP_PUSH".equals(activityInfo2.permission)) {
                SmsApplicationData smsApplicationData2 = receivers.get(activityInfo2.packageName);
                if (smsApplicationData2 != null) {
                    String unused2 = smsApplicationData2.mMmsReceiverClass = activityInfo2.name;
                }
            }
        }
        for (ResolveInfo resolveInfo3 : packageManager.queryIntentServicesAsUser(new Intent("android.intent.action.RESPOND_VIA_MESSAGE", Uri.fromParts(SCHEME_SMSTO, "", null)), 0, i)) {
            ServiceInfo serviceInfo = resolveInfo3.serviceInfo;
            if (serviceInfo != null && "android.permission.SEND_RESPOND_VIA_MESSAGE".equals(serviceInfo.permission)) {
                SmsApplicationData smsApplicationData3 = receivers.get(serviceInfo.packageName);
                if (smsApplicationData3 != null) {
                    String unused3 = smsApplicationData3.mRespondViaMessageClass = serviceInfo.name;
                }
            }
        }
        for (ResolveInfo resolveInfo4 : packageManager.queryIntentActivitiesAsUser(new Intent("android.intent.action.SENDTO", Uri.fromParts(SCHEME_SMSTO, "", null)), 0, i)) {
            ActivityInfo activityInfo3 = resolveInfo4.activityInfo;
            if (activityInfo3 != null) {
                SmsApplicationData smsApplicationData4 = receivers.get(activityInfo3.packageName);
                if (smsApplicationData4 != null) {
                    String unused4 = smsApplicationData4.mSendToClass = activityInfo3.name;
                }
            }
        }
        for (ResolveInfo resolveInfo5 : packageManager.queryBroadcastReceiversAsUser(new Intent("android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED"), 0, i)) {
            ActivityInfo activityInfo4 = resolveInfo5.activityInfo;
            if (activityInfo4 != null) {
                SmsApplicationData smsApplicationData5 = receivers.get(activityInfo4.packageName);
                if (smsApplicationData5 != null) {
                    String unused5 = smsApplicationData5.mSmsAppChangedReceiverClass = activityInfo4.name;
                }
            }
        }
        for (ResolveInfo resolveInfo6 : packageManager.queryBroadcastReceiversAsUser(new Intent("android.provider.action.EXTERNAL_PROVIDER_CHANGE"), 0, i)) {
            ActivityInfo activityInfo5 = resolveInfo6.activityInfo;
            if (activityInfo5 != null) {
                SmsApplicationData smsApplicationData6 = receivers.get(activityInfo5.packageName);
                if (smsApplicationData6 != null) {
                    String unused6 = smsApplicationData6.mProviderChangedReceiverClass = activityInfo5.name;
                }
            }
        }
        for (ResolveInfo resolveInfo7 : packageManager.queryBroadcastReceiversAsUser(new Intent("android.provider.Telephony.SIM_FULL"), 0, i)) {
            ActivityInfo activityInfo6 = resolveInfo7.activityInfo;
            if (activityInfo6 != null) {
                SmsApplicationData smsApplicationData7 = receivers.get(activityInfo6.packageName);
                if (smsApplicationData7 != null) {
                    String unused7 = smsApplicationData7.mSimFullReceiverClass = activityInfo6.name;
                }
                int i2 = userId;
            }
        }
        for (ResolveInfo resolveInfo8 : smsReceivers) {
            ActivityInfo activityInfo7 = resolveInfo8.activityInfo;
            if (activityInfo7 != null) {
                String packageName2 = activityInfo7.packageName;
                SmsApplicationData smsApplicationData8 = receivers.get(packageName2);
                if (smsApplicationData8 != null && !smsApplicationData8.isComplete()) {
                    receivers.remove(packageName2);
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

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v5, resolved type: java.lang.Object[]} */
    /* JADX WARNING: type inference failed for: r8v6 */
    /* JADX WARNING: Multi-variable type inference failed */
    private static SmsApplicationData getApplication(Context context, boolean updateIfNeeded, int userId) {
        SmsApplicationData applicationData;
        assignWriteSmsPermissionToAFW(context, updateIfNeeded, userId);
        if (!((TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY)).isSmsCapable()) {
            return null;
        }
        Collection<SmsApplicationData> applications = getApplicationCollectionInternal(context, userId);
        String defaultApplication = Settings.Secure.getStringForUser(context.getContentResolver(), "sms_default_application", userId);
        SmsApplicationData applicationData2 = null;
        if (defaultApplication != null) {
            applicationData2 = getApplicationForPackage(applications, defaultApplication);
        }
        if (updateIfNeeded && applicationData == null) {
            String defaultPackage = context.getResources().getString(17039923);
            String custDefaultSmsApp = SystemProperties.get(PROP_CUST_DFLT_SMS_APP, defaultPackage);
            Rlog.d(LOG_TAG, "custDefaultSmsApp = " + custDefaultSmsApp + ", defaultPackage = " + defaultPackage);
            applicationData = getApplicationForPackage(applications, custDefaultSmsApp);
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
                StringBuilder sb = new StringBuilder();
                sb.append(applicationData.mPackageName);
                sb.append(" lost OP_WRITE_SMS: ");
                sb.append(updateIfNeeded ? " (fixing)" : " (no permission to fix)");
                Rlog.e(LOG_TAG, sb.toString());
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
                assignWriteSmsPermissionToSystemUid(appOps, 1001);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, HWSYSTEMMANAGER_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, CONTACTS_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, MMS_PACKAGE_NAME);
            }
        }
        return applicationData;
    }

    public static void setDefaultApplication(String packageName, Context context) {
        if (!HwFrameworkFactory.getHwBaseInnerSmsManager().shouldSetDefaultApplicationForPackage(packageName, context)) {
            Rlog.d(LOG_TAG, "packageName " + packageName + "is not allowed to set to default application, return.");
        } else if (((TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY)).isSmsCapable()) {
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
        String oldPackageName = Settings.Secure.getStringForUser(context.getContentResolver(), "sms_default_application", userId);
        if (packageName == null || oldPackageName == null || !packageName.equals(oldPackageName)) {
            PackageManager packageManager = context.getPackageManager();
            Collection<SmsApplicationData> applications = getApplicationCollection(context);
            SmsApplicationData oldAppData = oldPackageName != null ? getApplicationForPackage(applications, oldPackageName) : null;
            SmsApplicationData applicationData = getApplicationForPackage(applications, packageName);
            if (applicationData != null) {
                AppOpsManager appOps = (AppOpsManager) context.getSystemService("appops");
                if (oldPackageName != null) {
                    try {
                        appOps.setMode(15, packageManager.getPackageInfoAsUser(oldPackageName, 0, userId).applicationInfo.uid, oldPackageName, 1);
                    } catch (PackageManager.NameNotFoundException e) {
                        Rlog.w(LOG_TAG, "Old SMS package not found: " + oldPackageName);
                    }
                }
                Settings.Secure.putStringForUser(context.getContentResolver(), "sms_default_application", applicationData.mPackageName, userId);
                configurePreferredActivity(packageManager, new ComponentName(applicationData.mPackageName, applicationData.mSendToClass), userId);
                appOps.setMode(15, applicationData.mUid, applicationData.mPackageName, 0);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, PHONE_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, BLUETOOTH_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, MMS_SERVICE_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, TELEPHONY_PROVIDER_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemUid(appOps, 1001);
                if (!(oldAppData == null || oldAppData.mSmsAppChangedReceiverClass == null)) {
                    Intent oldAppIntent = new Intent("android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED");
                    oldAppIntent.setComponent(new ComponentName(oldAppData.mPackageName, oldAppData.mSmsAppChangedReceiverClass));
                    oldAppIntent.putExtra("android.provider.extra.IS_DEFAULT_SMS_APP", false);
                    context.sendBroadcast(oldAppIntent);
                }
                if (applicationData.mSmsAppChangedReceiverClass != null) {
                    Intent intent = new Intent("android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED");
                    intent.setComponent(new ComponentName(applicationData.mPackageName, applicationData.mSmsAppChangedReceiverClass));
                    intent.putExtra("android.provider.extra.IS_DEFAULT_SMS_APP", true);
                    context.sendBroadcast(intent);
                }
                MetricsLogger.action(context, (int) MetricsProto.MetricsEvent.ACTION_DEFAULT_SMS_APP_CHANGED, applicationData.mPackageName);
                context.sendBroadcast(new Intent("com.android.telephony.DEFAULT_SMS_CHANGED"));
            }
        }
    }

    private static void assignWriteSmsPermissionToSystemApp(Context context, PackageManager packageManager, AppOpsManager appOps, String packageName) {
        if (packageManager.checkSignatures(context.getPackageName(), packageName) == 0 || HwFrameworkFactory.getHwBaseInnerSmsManager().allowToSetSmsWritePermission(packageName)) {
            try {
                PackageInfo info = packageManager.getPackageInfo(packageName, 0);
                if (appOps.checkOp(15, info.applicationInfo.uid, packageName) != 0) {
                    Rlog.w(LOG_TAG, packageName + " does not have OP_WRITE_SMS:  (fixing)");
                    appOps.setMode(15, info.applicationInfo.uid, packageName, 0);
                }
            } catch (PackageManager.NameNotFoundException e) {
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
        sSmsPackageMonitor.register(context, context.getMainLooper(), UserHandle.ALL, false);
    }

    /* access modifiers changed from: private */
    public static void configurePreferredActivity(PackageManager packageManager, ComponentName componentName, int userId) {
        replacePreferredActivity(packageManager, componentName, userId, SCHEME_SMS);
        replacePreferredActivity(packageManager, componentName, userId, SCHEME_SMSTO);
        replacePreferredActivity(packageManager, componentName, userId, "mms");
        replacePreferredActivity(packageManager, componentName, userId, SCHEME_MMSTO);
    }

    private static void replacePreferredActivity(PackageManager packageManager, ComponentName componentName, int userId, String scheme) {
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivitiesAsUser(new Intent("android.intent.action.SENDTO", Uri.fromParts(scheme, "", null)), 65600, userId);
        int n = resolveInfoList.size();
        ComponentName[] set = new ComponentName[n];
        for (int i = 0; i < n; i++) {
            ResolveInfo info = resolveInfoList.get(i);
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
        ComponentName component = null;
        try {
            SmsApplicationData smsApplicationData = getApplication(context, updateIfNeeded, userId);
            if (smsApplicationData != null) {
                component = new ComponentName(smsApplicationData.mPackageName, smsApplicationData.mSmsReceiverClass);
            }
            return component;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static ComponentName getDefaultMmsApplication(Context context, boolean updateIfNeeded) {
        int userId = getIncomingUserId(context);
        long token = Binder.clearCallingIdentity();
        ComponentName component = null;
        try {
            SmsApplicationData smsApplicationData = getApplication(context, updateIfNeeded, userId);
            if (smsApplicationData != null) {
                component = new ComponentName(smsApplicationData.mPackageName, smsApplicationData.mMmsReceiverClass);
            }
            return component;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static ComponentName getDefaultRespondViaMessageApplication(Context context, boolean updateIfNeeded) {
        int userId = getIncomingUserId(context);
        long token = Binder.clearCallingIdentity();
        ComponentName component = null;
        try {
            SmsApplicationData smsApplicationData = getApplication(context, updateIfNeeded, userId);
            if (smsApplicationData != null) {
                component = new ComponentName(smsApplicationData.mPackageName, smsApplicationData.mRespondViaMessageClass);
            }
            return component;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static ComponentName getDefaultSendToApplication(Context context, boolean updateIfNeeded) {
        int userId = getIncomingUserId(context);
        long token = Binder.clearCallingIdentity();
        ComponentName component = null;
        try {
            SmsApplicationData smsApplicationData = getApplication(context, updateIfNeeded, userId);
            if (smsApplicationData != null) {
                component = new ComponentName(smsApplicationData.mPackageName, smsApplicationData.mSendToClass);
            }
            return component;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static ComponentName getDefaultExternalTelephonyProviderChangedApplication(Context context, boolean updateIfNeeded) {
        int userId = getIncomingUserId(context);
        long token = Binder.clearCallingIdentity();
        ComponentName component = null;
        try {
            SmsApplicationData smsApplicationData = getApplication(context, updateIfNeeded, userId);
            if (!(smsApplicationData == null || smsApplicationData.mProviderChangedReceiverClass == null)) {
                component = new ComponentName(smsApplicationData.mPackageName, smsApplicationData.mProviderChangedReceiverClass);
            }
            return component;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static ComponentName getDefaultSimFullApplication(Context context, boolean updateIfNeeded) {
        int userId = getIncomingUserId(context);
        long token = Binder.clearCallingIdentity();
        ComponentName component = null;
        try {
            SmsApplicationData smsApplicationData = getApplication(context, updateIfNeeded, userId);
            if (!(smsApplicationData == null || smsApplicationData.mSimFullReceiverClass == null)) {
                component = new ComponentName(smsApplicationData.mPackageName, smsApplicationData.mSimFullReceiverClass);
            }
            return component;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static boolean shouldWriteMessageForPackage(String packageName, Context context) {
        if (SmsManager.getDefault().getAutoPersisting()) {
            return true;
        }
        return !isDefaultSmsApplication(context, packageName);
    }

    public static boolean isDefaultSmsApplication(Context context, String packageName) {
        if (packageName == null) {
            return false;
        }
        String defaultSmsPackage = getDefaultSmsApplicationPackageName(context);
        if ((defaultSmsPackage == null || !defaultSmsPackage.equals(packageName)) && !BLUETOOTH_PACKAGE_NAME.equals(packageName)) {
            return false;
        }
        return true;
    }

    private static String getDefaultSmsApplicationPackageName(Context context) {
        ComponentName component = getDefaultSmsApplication(context, false);
        if (component != null) {
            return component.getPackageName();
        }
        return null;
    }

    private static void assignWriteSmsPermissionToAFW(Context context, boolean updateIfNeeded, int userId) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService("appops");
        PackageManager packageManager = context.getPackageManager();
        Log.i(LOG_TAG, "updatedNeeded = " + updateIfNeeded + " for userId = " + userId);
        if (((UserManager) context.getSystemService("user")).isManagedProfile(userId) && updateIfNeeded) {
            assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, MMS_PACKAGE_NAME);
        }
    }
}
