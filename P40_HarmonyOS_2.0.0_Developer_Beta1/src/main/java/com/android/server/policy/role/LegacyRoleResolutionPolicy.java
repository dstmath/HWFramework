package com.android.server.policy.role;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.util.CollectionUtils;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.pm.HwCustPackageManagerService;
import com.android.server.pm.Installer;
import com.android.server.role.RoleManagerService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LegacyRoleResolutionPolicy implements RoleManagerService.RoleHoldersResolver {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "LegacyRoleResolutionPol";
    private final Context mContext;

    public LegacyRoleResolutionPolicy(Context context) {
        this.mContext = context;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // com.android.server.role.RoleManagerService.RoleHoldersResolver
    public List<String> getRoleHolders(String roleName, int userId) {
        char c;
        String str;
        switch (roleName.hashCode()) {
            case 443215373:
                if (roleName.equals("android.app.role.SMS")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 666116809:
                if (roleName.equals("android.app.role.DIALER")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 854448779:
                if (roleName.equals("android.app.role.HOME")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 1634943122:
                if (roleName.equals("android.app.role.ASSISTANT")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1834128197:
                if (roleName.equals("android.app.role.EMERGENCY")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1965677020:
                if (roleName.equals("android.app.role.BROWSER")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            String legacyAssistant = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "assistant", userId);
            if (legacyAssistant != null && !legacyAssistant.isEmpty()) {
                return Collections.singletonList(ComponentName.unflattenFromString(legacyAssistant).getPackageName());
            }
            String custDefaultAssist = SystemProperties.get("hw_sc.config_default_assistant", "");
            if (!TextUtils.isEmpty(custDefaultAssist)) {
                return Collections.singletonList(custDefaultAssist);
            }
            return Collections.emptyList();
        } else if (c == 1) {
            return CollectionUtils.singletonOrEmpty(((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).removeLegacyDefaultBrowserPackageName(userId));
        } else {
            if (c != 2) {
                String str2 = null;
                if (c == 3) {
                    String result = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "sms_default_application", userId);
                    if (result == null) {
                        Collection<SmsApplication.SmsApplicationData> applications = SmsApplication.getApplicationCollectionAsUser(this.mContext, userId);
                        String defaultPackage = this.mContext.getResources().getString(17039963);
                        String custDefaultSmsApp = SystemProperties.get("ro.config.default_sms_app", defaultPackage);
                        Log.i(LOG_TAG, "custDefaultSmsApp = " + custDefaultSmsApp + ", defaultPackage = " + defaultPackage);
                        SmsApplication.SmsApplicationData applicationData = SmsApplication.getApplicationForPackage(applications, custDefaultSmsApp);
                        if (applicationData == null && applications.size() != 0) {
                            applicationData = (SmsApplication.SmsApplicationData) applications.toArray()[0];
                        }
                        if (applicationData != null) {
                            str2 = applicationData.mPackageName;
                        }
                        result = str2;
                    }
                    return CollectionUtils.singletonOrEmpty(result);
                } else if (c == 4) {
                    ComponentName componentName = this.mContext.getPackageManager().getHomeActivities(new ArrayList<>());
                    if (componentName != null) {
                        str2 = componentName.getPackageName();
                    }
                    String packageName = str2;
                    if (packageName == null) {
                        packageName = getPreDefaultHome(userId);
                    }
                    return CollectionUtils.singletonOrEmpty(packageName);
                } else if (c == 5) {
                    return CollectionUtils.singletonOrEmpty(Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "emergency_assistance_application", userId));
                } else {
                    Slog.e(LOG_TAG, "Don't know how to find legacy role holders for " + roleName);
                    return Collections.emptyList();
                }
            } else {
                String setting = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "dialer_default_application", userId);
                if (!TextUtils.isEmpty(setting)) {
                    str = setting;
                } else {
                    str = ((TelecomManager) this.mContext.getSystemService(TelecomManager.class)).getSystemDialerPackage();
                }
                return CollectionUtils.singletonOrEmpty(str);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private String getPreDefaultHome(int userId) {
        long identity = Binder.clearCallingIdentity();
        try {
            Binder.restoreCallingIdentity((long) userId);
            String defaultLauncher = getDefaultLauncher(this.mContext);
            Slog.i(LOG_TAG, "defaultLauncher: " + defaultLauncher);
            Binder.restoreCallingIdentity(identity);
            return defaultLauncher;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    private String getDefaultLauncher(Context context) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfoList = getLauncherApps(context);
        if (resolveInfoList == null || resolveInfoList.size() == 0) {
            return null;
        }
        List<ComponentName> outActivities = new ArrayList<>(1);
        List<IntentFilter> filters = new ArrayList<>(1);
        IntentFilter filter = new IntentFilter("android.intent.action.MAIN");
        filter.addCategory("android.intent.category.HOME");
        filters.add(filter);
        for (ResolveInfo info : resolveInfoList) {
            filters.clear();
            filters.add(filter);
            pm.getPreferredActivities(filters, outActivities, info.activityInfo.packageName);
            if (outActivities.size() > 0) {
                Slog.i(LOG_TAG, "default launcher packageName: " + info.activityInfo.packageName);
                return info.activityInfo.packageName;
            }
        }
        return getCustDefaultLauncher(resolveInfoList);
    }

    private String getCustDefaultLauncher(List<ResolveInfo> resolveInfoList) {
        HwCustPackageManagerService mCustPackageManagerService = HwServiceFactory.getHuaweiPackageManagerService(this.mContext, new Installer(this.mContext), true, true).getHwPMSCustPackageManagerService();
        for (ResolveInfo info : resolveInfoList) {
            String defaultLauncher = "com.huawei.android.launcher";
            if (!(mCustPackageManagerService == null || info.activityInfo == null)) {
                String custDefaultLauncher = mCustPackageManagerService.getCustDefaultLauncher(this.mContext, info.activityInfo.applicationInfo.packageName);
                if (!TextUtils.isEmpty(custDefaultLauncher)) {
                    defaultLauncher = custDefaultLauncher;
                }
            }
            if (info.activityInfo != null && info.activityInfo.applicationInfo.packageName.equals(defaultLauncher)) {
                Slog.i(LOG_TAG, "Returning system default Launcher ");
                return defaultLauncher;
            }
        }
        return null;
    }

    private List<ResolveInfo> getLauncherApps(Context context) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        return context.getPackageManager().queryIntentActivities(intent, 786432);
    }
}
