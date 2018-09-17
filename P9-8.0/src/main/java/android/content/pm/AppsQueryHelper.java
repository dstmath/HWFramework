package android.content.pm;

import android.Manifest.permission;
import android.app.AppGlobals;
import android.content.Intent;
import android.opengl.GLES10;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ArraySet;
import java.util.ArrayList;
import java.util.List;

public class AppsQueryHelper {
    public static int GET_APPS_WITH_INTERACT_ACROSS_USERS_PERM = 2;
    public static int GET_IMES = 4;
    public static int GET_NON_LAUNCHABLE_APPS = 1;
    public static int GET_REQUIRED_FOR_SYSTEM_USER = 8;
    private List<ApplicationInfo> mAllApps;
    private final IPackageManager mPackageManager;

    public AppsQueryHelper(IPackageManager packageManager) {
        this.mPackageManager = packageManager;
    }

    public AppsQueryHelper() {
        this(AppGlobals.getPackageManager());
    }

    public List<String> queryApps(int flags, boolean systemAppsOnly, UserHandle user) {
        boolean nonLaunchableApps = (GET_NON_LAUNCHABLE_APPS & flags) > 0;
        boolean interactAcrossUsers = (GET_APPS_WITH_INTERACT_ACROSS_USERS_PERM & flags) > 0;
        boolean imes = (GET_IMES & flags) > 0;
        boolean requiredForSystemUser = (GET_REQUIRED_FOR_SYSTEM_USER & flags) > 0;
        if (this.mAllApps == null) {
            this.mAllApps = getAllApps(user.getIdentifier());
        }
        List<String> result = new ArrayList();
        int allAppsSize;
        int i;
        ApplicationInfo appInfo;
        if (flags == 0) {
            allAppsSize = this.mAllApps.size();
            for (i = 0; i < allAppsSize; i++) {
                appInfo = (ApplicationInfo) this.mAllApps.get(i);
                if (!systemAppsOnly || (appInfo.isSystemApp() ^ 1) == 0) {
                    result.add(appInfo.packageName);
                }
            }
            return result;
        }
        List<ResolveInfo> resolveInfos;
        int resolveInfosSize;
        if (nonLaunchableApps) {
            resolveInfos = queryIntentActivitiesAsUser(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), user.getIdentifier());
            ArraySet<String> appsWithLaunchers = new ArraySet();
            resolveInfosSize = resolveInfos.size();
            for (i = 0; i < resolveInfosSize; i++) {
                appsWithLaunchers.add(((ResolveInfo) resolveInfos.get(i)).activityInfo.packageName);
            }
            allAppsSize = this.mAllApps.size();
            for (i = 0; i < allAppsSize; i++) {
                appInfo = (ApplicationInfo) this.mAllApps.get(i);
                if (!systemAppsOnly || (appInfo.isSystemApp() ^ 1) == 0) {
                    String packageName = appInfo.packageName;
                    if (!appsWithLaunchers.contains(packageName)) {
                        result.add(packageName);
                    }
                }
            }
        }
        if (interactAcrossUsers) {
            List<PackageInfo> packagesHoldingPermissions = getPackagesHoldingPermission(permission.INTERACT_ACROSS_USERS, user.getIdentifier());
            int packagesHoldingPermissionsSize = packagesHoldingPermissions.size();
            for (i = 0; i < packagesHoldingPermissionsSize; i++) {
                PackageInfo packageInfo = (PackageInfo) packagesHoldingPermissions.get(i);
                if (!systemAppsOnly || (packageInfo.applicationInfo.isSystemApp() ^ 1) == 0) {
                    if (!result.contains(packageInfo.packageName)) {
                        result.add(packageInfo.packageName);
                    }
                }
            }
        }
        if (imes) {
            resolveInfos = queryIntentServicesAsUser(new Intent("android.view.InputMethod"), user.getIdentifier());
            resolveInfosSize = resolveInfos.size();
            for (i = 0; i < resolveInfosSize; i++) {
                ServiceInfo serviceInfo = ((ResolveInfo) resolveInfos.get(i)).serviceInfo;
                if (!systemAppsOnly || (serviceInfo.applicationInfo.isSystemApp() ^ 1) == 0) {
                    if (!result.contains(serviceInfo.packageName)) {
                        result.add(serviceInfo.packageName);
                    }
                }
            }
        }
        if (requiredForSystemUser) {
            allAppsSize = this.mAllApps.size();
            for (i = 0; i < allAppsSize; i++) {
                appInfo = (ApplicationInfo) this.mAllApps.get(i);
                if ((!systemAppsOnly || (appInfo.isSystemApp() ^ 1) == 0) && appInfo.isRequiredForSystemUser()) {
                    result.add(appInfo.packageName);
                }
            }
        }
        return result;
    }

    protected List<ApplicationInfo> getAllApps(int userId) {
        try {
            return this.mPackageManager.getInstalledApplications(GLES10.GL_TEXTURE_ENV_MODE, userId).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    protected List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int userId) {
        try {
            return this.mPackageManager.queryIntentActivities(intent, null, 795136, userId).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    protected List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int userId) {
        try {
            return this.mPackageManager.queryIntentServices(intent, null, 819328, userId).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    protected List<PackageInfo> getPackagesHoldingPermission(String perm, int userId) {
        try {
            return this.mPackageManager.getPackagesHoldingPermissions(new String[]{perm}, 0, userId).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
