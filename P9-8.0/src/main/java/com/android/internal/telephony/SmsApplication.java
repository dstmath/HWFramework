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
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Telephony.Sms.Intents;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.LogException;
import com.android.internal.R;
import com.android.internal.content.PackageMonitor;
import com.android.internal.logging.MetricsLogger;
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
        private String mMmsReceiverClass;
        public String mPackageName;
        private String mProviderChangedReceiverClass;
        private String mRespondViaMessageClass;
        private String mSendToClass;
        private String mSimFullReceiverClass;
        private String mSmsAppChangedReceiverClass;
        private String mSmsReceiverClass;
        private int mUid;

        public boolean isComplete() {
            if (this.mSmsReceiverClass == null || this.mMmsReceiverClass == null || this.mRespondViaMessageClass == null || this.mSendToClass == null) {
                return false;
            }
            return true;
        }

        public SmsApplicationData(String packageName, int uid) {
            this.mPackageName = packageName;
            this.mUid = uid;
        }

        public String getApplicationName(Context context) {
            String str = null;
            if (this.mApplicationName == null) {
                PackageManager pm = context.getPackageManager();
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfoAsUser(this.mPackageName, 0, UserHandle.getUserId(this.mUid));
                    if (appInfo != null) {
                        CharSequence label = pm.getApplicationLabel(appInfo);
                        if (label != null) {
                            str = label.toString();
                        }
                        this.mApplicationName = str;
                    }
                } catch (NameNotFoundException e) {
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
                } catch (NameNotFoundException e) {
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
            Collection<SmsApplicationData> applicationCollectionInternal = getApplicationCollectionInternal(context, userId);
            return applicationCollectionInternal;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private static Collection<SmsApplicationData> getApplicationCollectionInternal(Context context, int userId) {
        ActivityInfo activityInfo;
        String packageName;
        SmsApplicationData smsApplicationData;
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> smsReceivers = packageManager.queryBroadcastReceiversAsUser(new Intent(Intents.SMS_DELIVER_ACTION), 0, userId);
        HashMap<String, SmsApplicationData> receivers = new HashMap();
        for (ResolveInfo resolveInfo : smsReceivers) {
            activityInfo = resolveInfo.activityInfo;
            if (activityInfo != null && "android.permission.BROADCAST_SMS".equals(activityInfo.permission)) {
                packageName = activityInfo.packageName;
                if (!receivers.containsKey(packageName)) {
                    SmsApplicationData smsApplicationData2 = new SmsApplicationData(packageName, activityInfo.applicationInfo.uid);
                    smsApplicationData2.mSmsReceiverClass = activityInfo.name;
                    receivers.put(packageName, smsApplicationData2);
                }
            }
        }
        Intent intent = new Intent(Intents.WAP_PUSH_DELIVER_ACTION);
        intent.setDataAndType(null, "application/vnd.wap.mms-message");
        for (ResolveInfo resolveInfo2 : packageManager.queryBroadcastReceiversAsUser(intent, 0, userId)) {
            activityInfo = resolveInfo2.activityInfo;
            if (activityInfo != null && "android.permission.BROADCAST_WAP_PUSH".equals(activityInfo.permission)) {
                smsApplicationData = (SmsApplicationData) receivers.get(activityInfo.packageName);
                if (smsApplicationData != null) {
                    smsApplicationData.mMmsReceiverClass = activityInfo.name;
                }
            }
        }
        for (ResolveInfo resolveInfo22 : packageManager.queryIntentServicesAsUser(new Intent(TelephonyManager.ACTION_RESPOND_VIA_MESSAGE, Uri.fromParts("smsto", LogException.NO_VALUE, null)), 0, userId)) {
            ServiceInfo serviceInfo = resolveInfo22.serviceInfo;
            if (serviceInfo != null && "android.permission.SEND_RESPOND_VIA_MESSAGE".equals(serviceInfo.permission)) {
                smsApplicationData = (SmsApplicationData) receivers.get(serviceInfo.packageName);
                if (smsApplicationData != null) {
                    smsApplicationData.mRespondViaMessageClass = serviceInfo.name;
                }
            }
        }
        for (ResolveInfo resolveInfo222 : packageManager.queryIntentActivitiesAsUser(new Intent("android.intent.action.SENDTO", Uri.fromParts("smsto", LogException.NO_VALUE, null)), 0, userId)) {
            activityInfo = resolveInfo222.activityInfo;
            if (activityInfo != null) {
                smsApplicationData = (SmsApplicationData) receivers.get(activityInfo.packageName);
                if (smsApplicationData != null) {
                    smsApplicationData.mSendToClass = activityInfo.name;
                }
            }
        }
        for (ResolveInfo resolveInfo2222 : packageManager.queryBroadcastReceiversAsUser(new Intent(Intents.ACTION_DEFAULT_SMS_PACKAGE_CHANGED), 0, userId)) {
            activityInfo = resolveInfo2222.activityInfo;
            if (activityInfo != null) {
                smsApplicationData = (SmsApplicationData) receivers.get(activityInfo.packageName);
                if (smsApplicationData != null) {
                    smsApplicationData.mSmsAppChangedReceiverClass = activityInfo.name;
                }
            }
        }
        for (ResolveInfo resolveInfo22222 : packageManager.queryBroadcastReceiversAsUser(new Intent(Intents.ACTION_EXTERNAL_PROVIDER_CHANGE), 0, userId)) {
            activityInfo = resolveInfo22222.activityInfo;
            if (activityInfo != null) {
                smsApplicationData = (SmsApplicationData) receivers.get(activityInfo.packageName);
                if (smsApplicationData != null) {
                    smsApplicationData.mProviderChangedReceiverClass = activityInfo.name;
                }
            }
        }
        for (ResolveInfo resolveInfo222222 : packageManager.queryBroadcastReceiversAsUser(new Intent(Intents.SIM_FULL_ACTION), 0, userId)) {
            activityInfo = resolveInfo222222.activityInfo;
            if (activityInfo != null) {
                smsApplicationData = (SmsApplicationData) receivers.get(activityInfo.packageName);
                if (smsApplicationData != null) {
                    smsApplicationData.mSimFullReceiverClass = activityInfo.name;
                }
            }
        }
        for (ResolveInfo resolveInfo2222222 : smsReceivers) {
            activityInfo = resolveInfo2222222.activityInfo;
            if (activityInfo != null) {
                packageName = activityInfo.packageName;
                smsApplicationData = (SmsApplicationData) receivers.get(packageName);
                if (!(smsApplicationData == null || smsApplicationData.isComplete())) {
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
        String defaultApplication = Secure.getStringForUser(context.getContentResolver(), Secure.SMS_DEFAULT_APPLICATION, userId);
        SmsApplicationData applicationData = null;
        if (defaultApplication != null) {
            applicationData = getApplicationForPackage(applications, defaultApplication);
        }
        if (updateIfNeeded && applicationData == null) {
            String defaultPackage = context.getResources().getString(R.string.default_sms_application);
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
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, "com.android.phone");
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, "com.android.bluetooth");
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, MMS_SERVICE_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, TELEPHONY_PROVIDER_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemUid(appOps, 1001);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, HWSYSTEMMANAGER_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, "com.android.contacts");
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, MMS_PACKAGE_NAME);
            }
        }
        return applicationData;
    }

    public static void setDefaultApplication(String packageName, Context context) {
        if (!HwFrameworkFactory.getHwBaseInnerSmsManager().shouldSetDefaultApplicationForPackage(packageName, context)) {
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
        String oldPackageName = Secure.getStringForUser(context.getContentResolver(), Secure.SMS_DEFAULT_APPLICATION, userId);
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
                    } catch (NameNotFoundException e) {
                        Rlog.w(LOG_TAG, "Old SMS package not found: " + oldPackageName);
                    }
                }
                Secure.putStringForUser(context.getContentResolver(), Secure.SMS_DEFAULT_APPLICATION, applicationData.mPackageName, userId);
                configurePreferredActivity(packageManager, new ComponentName(applicationData.mPackageName, applicationData.mSendToClass), userId);
                appOps.setMode(15, applicationData.mUid, applicationData.mPackageName, 0);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, "com.android.phone");
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, "com.android.bluetooth");
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, MMS_SERVICE_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemApp(context, packageManager, appOps, TELEPHONY_PROVIDER_PACKAGE_NAME);
                assignWriteSmsPermissionToSystemUid(appOps, 1001);
                if (!(oldAppData == null || oldAppData.mSmsAppChangedReceiverClass == null)) {
                    Intent oldAppIntent = new Intent(Intents.ACTION_DEFAULT_SMS_PACKAGE_CHANGED);
                    oldAppIntent.setComponent(new ComponentName(oldAppData.mPackageName, oldAppData.mSmsAppChangedReceiverClass));
                    oldAppIntent.putExtra(Intents.EXTRA_IS_DEFAULT_SMS_APP, false);
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
        if (packageManager.checkSignatures(context.getPackageName(), packageName) == 0 || (HwFrameworkFactory.getHwBaseInnerSmsManager().allowToSetSmsWritePermission(packageName) ^ 1) == 0) {
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
        sSmsPackageMonitor.register(context, context.getMainLooper(), UserHandle.ALL, false);
    }

    private static void configurePreferredActivity(PackageManager packageManager, ComponentName componentName, int userId) {
        replacePreferredActivity(packageManager, componentName, userId, SCHEME_SMS);
        replacePreferredActivity(packageManager, componentName, userId, "smsto");
        replacePreferredActivity(packageManager, componentName, userId, "mms");
        replacePreferredActivity(packageManager, componentName, userId, SCHEME_MMSTO);
    }

    private static void replacePreferredActivity(PackageManager packageManager, ComponentName componentName, int userId, String scheme) {
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivitiesAsUser(new Intent("android.intent.action.SENDTO", Uri.fromParts(scheme, LogException.NO_VALUE, null)), 65600, userId);
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

    public static ComponentName getDefaultSimFullApplication(Context context, boolean updateIfNeeded) {
        int userId = getIncomingUserId(context);
        long token = Binder.clearCallingIdentity();
        ComponentName componentName = null;
        try {
            SmsApplicationData smsApplicationData = getApplication(context, updateIfNeeded, userId);
            if (!(smsApplicationData == null || smsApplicationData.mSimFullReceiverClass == null)) {
                componentName = new ComponentName(smsApplicationData.mPackageName, smsApplicationData.mSimFullReceiverClass);
            }
            Binder.restoreCallingIdentity(token);
            return componentName;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static boolean shouldWriteMessageForPackage(String packageName, Context context) {
        if (SmsManager.getDefault().getAutoPersisting()) {
            return true;
        }
        return isDefaultSmsApplication(context, packageName) ^ 1;
    }

    public static boolean isDefaultSmsApplication(Context context, String packageName) {
        if (packageName == null) {
            return false;
        }
        String defaultSmsPackage = getDefaultSmsApplicationPackageName(context);
        if ((defaultSmsPackage == null || !defaultSmsPackage.equals(packageName)) && !"com.android.bluetooth".equals(packageName)) {
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
}
