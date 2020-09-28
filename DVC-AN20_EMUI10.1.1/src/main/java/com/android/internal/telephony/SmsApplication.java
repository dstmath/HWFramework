package com.android.internal.telephony;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.role.RoleManager;
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
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Telephony;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import com.android.internal.logging.MetricsLogger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public final class SmsApplication {
    private static final String BLUETOOTH_PACKAGE_NAME = "com.android.bluetooth";
    private static final String CONTACTS_PACKAGE_NAME = "com.android.contacts";
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_MULTIUSER = false;
    private static final int[] DEFAULT_APP_EXCLUSIVE_APPOPS = {14, 15, 16, 19, 20, 57};
    private static final String HWSYSTEMMANAGER_PACKAGE_NAME = "com.huawei.systemmanager";
    static final String LOG_TAG = "SmsApplication";
    private static final String MMS_PACKAGE_NAME = "com.android.mms";
    private static final String MMS_SERVICE_PACKAGE_NAME = "com.android.mms.service";
    private static final String NEW_CONTACTS_PACKAGE_NAME = "com.huawei.contacts";
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

    private static int getIncomingUserId(Context context) {
        int contextUserId = context.getUserId();
        int callingUid = Binder.getCallingUid();
        if (UserHandle.getAppId(callingUid) < 10000) {
            return contextUserId;
        }
        return UserHandle.getUserId(callingUid);
    }

    public static Collection<SmsApplicationData> getApplicationCollection(Context context) {
        return getApplicationCollectionAsUser(context, getIncomingUserId(context));
    }

    public static Collection<SmsApplicationData> getApplicationCollectionAsUser(Context context, int userId) {
        long token = Binder.clearCallingIdentity();
        try {
            return getApplicationCollectionInternal(context, userId);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private static Collection<SmsApplicationData> getApplicationCollectionInternal(Context context, int userId) {
        Iterator<ResolveInfo> it;
        SmsApplicationData smsApplicationData;
        SmsApplicationData smsApplicationData2;
        SmsApplicationData smsApplicationData3;
        SmsApplicationData smsApplicationData4;
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> smsReceivers = packageManager.queryBroadcastReceiversAsUser(new Intent(Telephony.Sms.Intents.SMS_DELIVER_ACTION), 786432, userId);
        HashMap<String, SmsApplicationData> receivers = new HashMap<>();
        for (ResolveInfo resolveInfo : smsReceivers) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo != null && Manifest.permission.BROADCAST_SMS.equals(activityInfo.permission)) {
                String packageName = activityInfo.packageName;
                if (!receivers.containsKey(packageName)) {
                    SmsApplicationData smsApplicationData5 = new SmsApplicationData(packageName, activityInfo.applicationInfo.uid);
                    smsApplicationData5.mSmsReceiverClass = activityInfo.name;
                    receivers.put(packageName, smsApplicationData5);
                }
            }
        }
        Intent intent = new Intent(Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION);
        intent.setDataAndType(null, "application/vnd.wap.mms-message");
        for (ResolveInfo resolveInfo2 : packageManager.queryBroadcastReceiversAsUser(intent, 786432, userId)) {
            ActivityInfo activityInfo2 = resolveInfo2.activityInfo;
            if (!(activityInfo2 == null || !Manifest.permission.BROADCAST_WAP_PUSH.equals(activityInfo2.permission) || (smsApplicationData4 = receivers.get(activityInfo2.packageName)) == null)) {
                smsApplicationData4.mMmsReceiverClass = activityInfo2.name;
            }
        }
        for (ResolveInfo resolveInfo3 : packageManager.queryIntentServicesAsUser(new Intent(TelephonyManager.ACTION_RESPOND_VIA_MESSAGE, Uri.fromParts("smsto", "", null)), 786432, userId)) {
            ServiceInfo serviceInfo = resolveInfo3.serviceInfo;
            if (serviceInfo != null && Manifest.permission.SEND_RESPOND_VIA_MESSAGE.equals(serviceInfo.permission)) {
                SmsApplicationData smsApplicationData6 = receivers.get(serviceInfo.packageName);
                if (smsApplicationData6 != null) {
                    smsApplicationData6.mRespondViaMessageClass = serviceInfo.name;
                }
            }
        }
        for (ResolveInfo resolveInfo4 : packageManager.queryIntentActivitiesAsUser(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", "", null)), 786432, userId)) {
            ActivityInfo activityInfo3 = resolveInfo4.activityInfo;
            if (!(activityInfo3 == null || (smsApplicationData3 = receivers.get(activityInfo3.packageName)) == null)) {
                smsApplicationData3.mSendToClass = activityInfo3.name;
            }
        }
        for (ResolveInfo resolveInfo5 : packageManager.queryBroadcastReceiversAsUser(new Intent(Telephony.Sms.Intents.ACTION_DEFAULT_SMS_PACKAGE_CHANGED), 786432, userId)) {
            ActivityInfo activityInfo4 = resolveInfo5.activityInfo;
            if (!(activityInfo4 == null || (smsApplicationData2 = receivers.get(activityInfo4.packageName)) == null)) {
                smsApplicationData2.mSmsAppChangedReceiverClass = activityInfo4.name;
            }
        }
        for (ResolveInfo resolveInfo6 : packageManager.queryBroadcastReceiversAsUser(new Intent(Telephony.Sms.Intents.ACTION_EXTERNAL_PROVIDER_CHANGE), 786432, userId)) {
            ActivityInfo activityInfo5 = resolveInfo6.activityInfo;
            if (!(activityInfo5 == null || (smsApplicationData = receivers.get(activityInfo5.packageName)) == null)) {
                smsApplicationData.mProviderChangedReceiverClass = activityInfo5.name;
            }
        }
        for (ResolveInfo resolveInfo7 : packageManager.queryBroadcastReceiversAsUser(new Intent(Telephony.Sms.Intents.SIM_FULL_ACTION), 786432, userId)) {
            ActivityInfo activityInfo6 = resolveInfo7.activityInfo;
            if (activityInfo6 != null) {
                SmsApplicationData smsApplicationData7 = receivers.get(activityInfo6.packageName);
                if (smsApplicationData7 != null) {
                    smsApplicationData7.mSimFullReceiverClass = activityInfo6.name;
                }
            }
        }
        Iterator<ResolveInfo> it2 = smsReceivers.iterator();
        while (it2.hasNext()) {
            ActivityInfo activityInfo7 = it2.next().activityInfo;
            if (activityInfo7 != null) {
                String packageName2 = activityInfo7.packageName;
                SmsApplicationData smsApplicationData8 = receivers.get(packageName2);
                if (smsApplicationData8 == null) {
                    it = it2;
                } else if (!smsApplicationData8.isComplete()) {
                    StringBuilder sb = new StringBuilder();
                    it = it2;
                    sb.append("Package ");
                    sb.append(packageName2);
                    sb.append(" lacks required manifest declarations to be a default sms app: ");
                    sb.append(smsApplicationData8);
                    Log.w(LOG_TAG, sb.toString());
                    receivers.remove(packageName2);
                } else {
                    it = it2;
                }
                it2 = it;
            }
        }
        return receivers.values();
    }

    public static SmsApplicationData getApplicationForPackage(Collection<SmsApplicationData> applications, String packageName) {
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
        assignWriteSmsPermissionToAFW(context, updateIfNeeded, userId);
        RoleManager roleManager = (RoleManager) context.getSystemService(Context.ROLE_SERVICE);
        if (!((TelephonyManager) context.getSystemService("phone")).isSmsCapable() && (roleManager == null || !roleManager.isRoleAvailable(RoleManager.ROLE_SMS))) {
            return null;
        }
        Collection<SmsApplicationData> applications = getApplicationCollectionInternal(context, userId);
        String defaultApplication = getDefaultSmsPackage(context, userId);
        SmsApplicationData applicationData = null;
        if (defaultApplication != null) {
            applicationData = getApplicationForPackage(applications, defaultApplication);
        }
        if (applicationData != null) {
            if ((updateIfNeeded || applicationData.mUid == Process.myUid()) && !tryFixExclusiveSmsAppops(context, applicationData, updateIfNeeded)) {
                applicationData = null;
            }
            if (applicationData != null && updateIfNeeded) {
                defaultSmsAppChanged(context);
                PackageManager packageManager = context.getPackageManager();
                AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                assignExclusiveSmsPermissionsToSystemApp(context, packageManager, appOps, HWSYSTEMMANAGER_PACKAGE_NAME);
                assignExclusiveSmsPermissionsToSystemApp(context, packageManager, appOps, "com.android.contacts");
                assignExclusiveSmsPermissionsToSystemApp(context, packageManager, appOps, "com.huawei.contacts");
                assignExclusiveSmsPermissionsToSystemApp(context, packageManager, appOps, MMS_PACKAGE_NAME);
            }
        }
        return applicationData;
    }

    private static String getDefaultSmsPackage(Context context, int userId) {
        return ((RoleManager) context.getSystemService(RoleManager.class)).getDefaultSmsPackage(userId);
    }

    private static void defaultSmsAppChanged(Context context) {
        PackageManager packageManager = context.getPackageManager();
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        assignExclusiveSmsPermissionsToSystemApp(context, packageManager, appOps, "com.android.phone");
        assignExclusiveSmsPermissionsToSystemApp(context, packageManager, appOps, "com.android.bluetooth");
        assignExclusiveSmsPermissionsToSystemApp(context, packageManager, appOps, MMS_SERVICE_PACKAGE_NAME);
        assignExclusiveSmsPermissionsToSystemApp(context, packageManager, appOps, TELEPHONY_PROVIDER_PACKAGE_NAME);
        for (int appop : DEFAULT_APP_EXCLUSIVE_APPOPS) {
            appOps.setUidMode(appop, 1001, 0);
        }
    }

    private static boolean tryFixExclusiveSmsAppops(Context context, SmsApplicationData applicationData, boolean updateIfNeeded) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        int[] iArr = DEFAULT_APP_EXCLUSIVE_APPOPS;
        for (int appOp : iArr) {
            if (appOps.checkOp(appOp, applicationData.mUid, applicationData.mPackageName) != 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(applicationData.mPackageName);
                sb.append(" lost ");
                sb.append(AppOpsManager.modeToName(appOp));
                sb.append(": ");
                sb.append(updateIfNeeded ? " (fixing)" : " (no permission to fix)");
                Rlog.e(LOG_TAG, sb.toString());
                if (!updateIfNeeded) {
                    return false;
                }
                appOps.setUidMode(appOp, applicationData.mUid, 0);
            }
        }
        return true;
    }

    public static void setDefaultApplication(String packageName, Context context) {
        setDefaultApplicationAsUser(packageName, context, getIncomingUserId(context));
    }

    public static void setDefaultApplicationAsUser(String packageName, Context context, int userId) {
        if (!HwFrameworkFactory.getHwBaseInnerSmsManager().shouldSetDefaultApplicationForPackage(packageName, context)) {
            Rlog.d(LOG_TAG, "packageName " + packageName + "is not allowed to set to default application, return.");
            return;
        }
        RoleManager roleManager = (RoleManager) context.getSystemService(Context.ROLE_SERVICE);
        if (((TelephonyManager) context.getSystemService("phone")).isSmsCapable() || (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_SMS))) {
            long token = Binder.clearCallingIdentity();
            try {
                setDefaultApplicationInternal(packageName, context, userId);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    private static void setDefaultApplicationInternal(String packageName, Context context, int userId) {
        UserHandle.of(userId);
        String oldPackageName = getDefaultSmsPackage(context, userId);
        if (packageName == null || oldPackageName == null || !packageName.equals(oldPackageName)) {
            PackageManager packageManager = context.getPackageManager();
            Collection<SmsApplicationData> applications = getApplicationCollectionInternal(context, userId);
            SmsApplicationData applicationForPackage = oldPackageName != null ? getApplicationForPackage(applications, oldPackageName) : null;
            SmsApplicationData applicationData = getApplicationForPackage(applications, packageName);
            if (applicationData != null) {
                AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                if (oldPackageName != null) {
                    try {
                        setExclusiveAppops(oldPackageName, appOps, packageManager.getPackageInfoAsUser(oldPackageName, 0, userId).applicationInfo.uid, 3);
                    } catch (PackageManager.NameNotFoundException e) {
                        Rlog.w(LOG_TAG, "Old SMS package not found: " + oldPackageName);
                    }
                }
                CompletableFuture<Void> future = new CompletableFuture<>();
                ((RoleManager) context.getSystemService(RoleManager.class)).addRoleHolderAsUser(RoleManager.ROLE_SMS, applicationData.mPackageName, 0, UserHandle.of(userId), AsyncTask.THREAD_POOL_EXECUTOR, new Consumer(future) {
                    /* class com.android.internal.telephony.$$Lambda$SmsApplication$gDx3WUsTeTFaBSPUY_LFPZ9dE */
                    private final /* synthetic */ CompletableFuture f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        SmsApplication.lambda$setDefaultApplicationInternal$0(this.f$0, (Boolean) obj);
                    }
                });
                try {
                    future.get(5, TimeUnit.SECONDS);
                    defaultSmsAppChanged(context);
                } catch (InterruptedException | ExecutionException | TimeoutException e2) {
                    Log.e(LOG_TAG, "Exception while adding sms role holder " + applicationData, e2);
                }
            }
        }
    }

    static /* synthetic */ void lambda$setDefaultApplicationInternal$0(CompletableFuture future, Boolean successful) {
        if (successful.booleanValue()) {
            future.complete(null);
        } else {
            future.completeExceptionally(new RuntimeException());
        }
    }

    public static void broadcastSmsAppChange(Context context, UserHandle userHandle, String oldPackage, String newPackage) {
        Collection<SmsApplicationData> apps = getApplicationCollection(context);
        broadcastSmsAppChange(context, userHandle, getApplicationForPackage(apps, oldPackage), getApplicationForPackage(apps, newPackage));
    }

    private static void broadcastSmsAppChange(Context context, UserHandle userHandle, SmsApplicationData oldAppData, SmsApplicationData applicationData) {
        if (!(oldAppData == null || oldAppData.mSmsAppChangedReceiverClass == null)) {
            Intent oldAppIntent = new Intent(Telephony.Sms.Intents.ACTION_DEFAULT_SMS_PACKAGE_CHANGED);
            oldAppIntent.setComponent(new ComponentName(oldAppData.mPackageName, oldAppData.mSmsAppChangedReceiverClass));
            oldAppIntent.putExtra(Telephony.Sms.Intents.EXTRA_IS_DEFAULT_SMS_APP, false);
            context.sendBroadcastAsUser(oldAppIntent, userHandle);
        }
        if (!(applicationData == null || applicationData.mSmsAppChangedReceiverClass == null)) {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_DEFAULT_SMS_PACKAGE_CHANGED);
            intent.setComponent(new ComponentName(applicationData.mPackageName, applicationData.mSmsAppChangedReceiverClass));
            intent.putExtra(Telephony.Sms.Intents.EXTRA_IS_DEFAULT_SMS_APP, true);
            context.sendBroadcastAsUser(intent, userHandle);
        }
        context.sendBroadcastAsUser(new Intent(Telephony.Sms.Intents.ACTION_DEFAULT_SMS_PACKAGE_CHANGED_INTERNAL), userHandle, Manifest.permission.MONITOR_DEFAULT_SMS_PACKAGE);
        if (applicationData != null) {
            MetricsLogger.action(context, 266, applicationData.mPackageName);
        }
    }

    private static void assignExclusiveSmsPermissionsToSystemApp(Context context, PackageManager packageManager, AppOpsManager appOps, String packageName) {
        if (packageManager.checkSignatures(context.getPackageName(), packageName) == 0 || HwFrameworkFactory.getHwBaseInnerSmsManager().allowToSetSmsWritePermission(packageName)) {
            try {
                PackageInfo info = packageManager.getPackageInfo(packageName, 0);
                if (appOps.checkOp(15, info.applicationInfo.uid, packageName) != 0) {
                    Rlog.w(LOG_TAG, packageName + " does not have OP_WRITE_SMS:  (fixing)");
                    setExclusiveAppops(packageName, appOps, info.applicationInfo.uid, 0);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Rlog.e(LOG_TAG, "Package not found: " + packageName);
            }
        } else {
            Rlog.e(LOG_TAG, packageName + " does not have system signature");
        }
    }

    private static void setExclusiveAppops(String pkg, AppOpsManager appOpsManager, int uid, int mode) {
        for (int appop : DEFAULT_APP_EXCLUSIVE_APPOPS) {
            appOpsManager.setUidMode(appop, uid, mode);
        }
    }

    private static final class SmsPackageMonitor extends PackageMonitor {
        final Context mContext;

        public SmsPackageMonitor(Context context) {
            this.mContext = context;
        }

        @Override // com.android.internal.content.PackageMonitor
        public void onPackageDisappeared(String packageName, int reason) {
            onPackageChanged();
        }

        @Override // com.android.internal.content.PackageMonitor
        public void onPackageAppeared(String packageName, int reason) {
            onPackageChanged();
        }

        @Override // com.android.internal.content.PackageMonitor
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

    public static void initSmsPackageMonitor(Context context) {
        sSmsPackageMonitor = new SmsPackageMonitor(context);
        sSmsPackageMonitor.register(context, context.getMainLooper(), UserHandle.ALL, false);
    }

    /* access modifiers changed from: private */
    public static void configurePreferredActivity(PackageManager packageManager, ComponentName componentName, int userId) {
        replacePreferredActivity(packageManager, componentName, userId, SCHEME_SMS);
        replacePreferredActivity(packageManager, componentName, userId, "smsto");
        replacePreferredActivity(packageManager, componentName, userId, "mms");
        replacePreferredActivity(packageManager, componentName, userId, SCHEME_MMSTO);
    }

    private static void replacePreferredActivity(PackageManager packageManager, ComponentName componentName, int userId, String scheme) {
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivitiesAsUser(new Intent(Intent.ACTION_SENDTO, Uri.fromParts(scheme, "", null)), 65600, userId);
        int n = resolveInfoList.size();
        ComponentName[] set = new ComponentName[n];
        for (int i = 0; i < n; i++) {
            ResolveInfo info = resolveInfoList.get(i);
            set[i] = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SENDTO);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.addDataScheme(scheme);
        packageManager.replacePreferredActivityAsUser(intentFilter, 2129920, set, componentName, userId);
    }

    public static SmsApplicationData getSmsApplicationData(String packageName, Context context) {
        return getApplicationForPackage(getApplicationCollection(context), packageName);
    }

    public static ComponentName getDefaultSmsApplication(Context context, boolean updateIfNeeded) {
        return getDefaultSmsApplicationAsUser(context, updateIfNeeded, getIncomingUserId(context));
    }

    public static ComponentName getDefaultSmsApplicationAsUser(Context context, boolean updateIfNeeded, int userId) {
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

    private static void assignWriteSmsPermissionToAFW(Context context, boolean updateIfNeeded, int userId) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        PackageManager packageManager = context.getPackageManager();
        Log.i(LOG_TAG, "updatedNeeded = " + updateIfNeeded + " for userId = " + userId);
        if (((UserManager) context.getSystemService("user")).isManagedProfile(userId) && updateIfNeeded) {
            assignExclusiveSmsPermissionsToSystemApp(context, packageManager, appOps, MMS_PACKAGE_NAME);
        }
    }
}
