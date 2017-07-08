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
    public static int GET_APPS_WITH_INTERACT_ACROSS_USERS_PERM;
    public static int GET_IMES;
    public static int GET_NON_LAUNCHABLE_APPS;
    public static int GET_REQUIRED_FOR_SYSTEM_USER;
    private List<ApplicationInfo> mAllApps;
    private final IPackageManager mPackageManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.AppsQueryHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.AppsQueryHelper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.AppsQueryHelper.<clinit>():void");
    }

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
                if (!systemAppsOnly || appInfo.isSystemApp()) {
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
                if (!systemAppsOnly || appInfo.isSystemApp()) {
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
                if (systemAppsOnly) {
                    if (!packageInfo.applicationInfo.isSystemApp()) {
                    }
                }
                if (!result.contains(packageInfo.packageName)) {
                    result.add(packageInfo.packageName);
                }
            }
        }
        if (imes) {
            resolveInfos = queryIntentServicesAsUser(new Intent("android.view.InputMethod"), user.getIdentifier());
            resolveInfosSize = resolveInfos.size();
            for (i = 0; i < resolveInfosSize; i++) {
                ServiceInfo serviceInfo = ((ResolveInfo) resolveInfos.get(i)).serviceInfo;
                if (systemAppsOnly) {
                    if (!serviceInfo.applicationInfo.isSystemApp()) {
                    }
                }
                if (!result.contains(serviceInfo.packageName)) {
                    result.add(serviceInfo.packageName);
                }
            }
        }
        if (requiredForSystemUser) {
            allAppsSize = this.mAllApps.size();
            for (i = 0; i < allAppsSize; i++) {
                appInfo = (ApplicationInfo) this.mAllApps.get(i);
                if ((!systemAppsOnly || appInfo.isSystemApp()) && appInfo.isRequiredForSystemUser()) {
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
            return this.mPackageManager.queryIntentActivities(intent, null, GLES10.GL_TEXTURE_ENV_MODE, userId).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    protected List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int userId) {
        try {
            return this.mPackageManager.queryIntentServices(intent, null, 32896, userId).getList();
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
