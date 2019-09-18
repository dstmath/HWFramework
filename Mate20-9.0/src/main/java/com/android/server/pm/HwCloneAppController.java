package com.android.server.pm;

import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.PackageParser;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.pm.VersionedPackage;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.Preconditions;
import com.android.server.am.HwActivityManagerService;
import com.android.server.gesture.GestureNavConst;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwCloneAppController {
    private static final String CLONE_APP_LIST = "hw_clone_app_list.xml";
    private static final Set<String> SUPPORT_CLONE_APPS = new HashSet();
    private static final String TAG = "HwCloneAppController";
    private static volatile HwCloneAppController mInstance;
    private IHwPackageManagerServiceExInner mHwPmsExInner;
    private Context mPmsContext = this.mHwPmsExInner.getContextInner();
    private IHwPackageManagerInner mPmsInner = this.mHwPmsExInner.getIPmsInner();

    private HwCloneAppController(IHwPackageManagerServiceExInner pmsEx) {
        this.mHwPmsExInner = pmsEx;
    }

    public static HwCloneAppController getInstance(IHwPackageManagerServiceExInner pmsEx) {
        if (mInstance == null) {
            synchronized (HwCloneAppController.class) {
                if (mInstance == null) {
                    mInstance = new HwCloneAppController(pmsEx);
                }
            }
        }
        return mInstance;
    }

    public static boolean isSupportCloneAppInCust(String packageName) {
        return SUPPORT_CLONE_APPS.contains(packageName);
    }

    public static Set<String> getSupportCloneApps() {
        return SUPPORT_CLONE_APPS;
    }

    public void deleteNonSupportedAppsForClone() {
        long callingId = Binder.clearCallingIdentity();
        try {
            Iterator<UserInfo> it = PackageManagerService.sUserManager.getUsers(false).iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                UserInfo ui = it.next();
                if (ui.isClonedProfile()) {
                    deleteNonRequiredAppsForClone(ui.id, false);
                    break;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:132:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:133:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0183, code lost:
        r9 = new android.content.Intent("android.intent.action.MAIN");
        r9.addCategory("android.intent.category.LAUNCHER");
        r10 = r1.mPmsInner.queryIntentActivitiesInner(r9, r9.resolveTypeIfNeeded(r1.mPmsContext.getContentResolver()), 786432, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x01a2, code lost:
        if (r10 == null) goto L_0x01f3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x01a4, code lost:
        r0 = r10.getList().iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x01b0, code lost:
        if (r0.hasNext() == false) goto L_0x01f3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x01b2, code lost:
        r13 = (android.content.pm.ResolveInfo) r0.next();
        r7 = r13.activityInfo.getComponentName();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x01c6, code lost:
        if (r5.contains(r13.activityInfo.packageName) == false) goto L_0x01ef;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x01c8, code lost:
        r1.mPmsInner.setComponentEnabledSettingInner(r7, 2, 1, r2);
        android.util.Slog.i(TAG, "Disable [" + r7 + "] for clone user " + r2);
        r8 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x01f3, code lost:
        r6 = new android.content.Intent("android.intent.action.MAIN");
        r6.addCategory("android.intent.category.HOME");
        r12 = r1.mPmsInner.queryIntentActivitiesInner(r6, r6.resolveTypeIfNeeded(r1.mPmsContext.getContentResolver()), 786432, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0212, code lost:
        if (r12 == null) goto L_0x026d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0214, code lost:
        r0 = r12.getList().iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0220, code lost:
        if (r0.hasNext() == false) goto L_0x026d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0222, code lost:
        r13 = (android.content.pm.ResolveInfo) r0.next();
        r7 = r13.activityInfo.getComponentName();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0236, code lost:
        if (r5.contains(r13.activityInfo.packageName) == false) goto L_0x0264;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0238, code lost:
        r16 = r3;
        r17 = r4;
        r1.mPmsInner.setComponentEnabledSettingInner(r7, 2, 1, r2);
        android.util.Slog.i(TAG, "Disable [" + r7 + "] for clone user " + r2);
        r8 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0264, code lost:
        r16 = r3;
        r17 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0268, code lost:
        r3 = r16;
        r4 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x026d, code lost:
        r16 = r3;
        r17 = r4;
        r3 = r1.mPmsContext.getResources().getStringArray(33816591);
        r0 = null;
        r4 = r3.length;
        r13 = r7;
        r7 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0282, code lost:
        if (r7 >= r4) goto L_0x0366;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0284, code lost:
        r18 = r0;
        r20 = r3;
        r21 = r4;
        r4 = new android.content.Intent(r3[r7]);
        r22 = r5;
        r3 = r1.mPmsInner.queryIntentReceiversInner(r4, r4.resolveTypeIfNeeded(r1.mPmsContext.getContentResolver()), 786432, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x02a9, code lost:
        if (r3 != null) goto L_0x02b0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x02b0, code lost:
        r5 = r3.getList().iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0366, code lost:
        r18 = r0;
        r20 = r3;
        r22 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x036c, code lost:
        if (r8 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x036e, code lost:
        r1.mPmsInner.scheduleWritePackageRestrictionsLockedInner(r2);
     */
    public void deleteNonRequiredAppsForClone(int clonedProfileUserId, boolean isFirstCreat) {
        String[] procList;
        int i;
        Intent procIntent;
        Set<String> requiredAppsSet;
        ParceledListSlice<ResolveInfo> parceledList;
        Iterator it;
        Iterator it2;
        Intent procIntent2;
        int i2 = clonedProfileUserId;
        String[] disabledComponent = this.mPmsContext.getResources().getStringArray(33816590);
        boolean shouldUpdate = false;
        ComponentName component = null;
        for (String str : disabledComponent) {
            String[] componentArray = str.split("/");
            if (componentArray != null && componentArray.length == 2) {
                component = new ComponentName(componentArray[0], componentArray[1]);
                try {
                    if (this.mPmsInner.getComponentEnabledSettingInner(component, i2) != 2) {
                        this.mPmsInner.setComponentEnabledSettingInner(component, 2, 1, i2);
                        shouldUpdate = true;
                    }
                } catch (IllegalArgumentException | SecurityException e) {
                    Slog.d(TAG, "deleteNonRequiredComponentsForClone exception:" + e.getMessage());
                }
            }
        }
        String[] requiredAppsList = this.mPmsContext.getResources().getStringArray(33816586);
        Set<String> requiredAppsSet2 = new HashSet<>(Arrays.asList(requiredAppsList));
        UserInfo ui = PackageManagerService.sUserManager.getUserInfo(i2);
        synchronized (this.mPmsInner.getPackagesLock()) {
            try {
                for (Map.Entry<String, PackageSetting> entry : this.mPmsInner.getSettings().mPackages.entrySet()) {
                    try {
                        if (isFirstCreat) {
                            if (!requiredAppsSet2.contains(entry.getKey())) {
                                entry.getValue().setInstalled(false, i2);
                                shouldUpdate = true;
                                Slog.i(TAG, "Deleting non supported package [" + entry.getKey() + "] for clone user " + i2);
                            }
                        } else if (!isSupportCloneAppInCust(entry.getKey()) && !requiredAppsSet2.contains(entry.getKey())) {
                            entry.getValue().setInstalled(false, i2);
                            shouldUpdate = true;
                            Slog.i(TAG, "Deleting non supported package [" + entry.getKey() + "] for clone user " + i2);
                        } else if (requiredAppsSet2.contains(entry.getKey()) && entry.getValue().getInstalled(ui.profileGroupId) && !entry.getValue().getInstalled(i2)) {
                            entry.getValue().setInstalled(true, i2);
                            Slog.i(TAG, "Adding required package [" + entry.getKey() + "] for clone user " + i2);
                            shouldUpdate = true;
                        }
                    } catch (Throwable th) {
                        th = th;
                        String[] strArr = disabledComponent;
                        String[] strArr2 = requiredAppsList;
                        HashSet hashSet = requiredAppsSet2;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        }
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                String[] strArr3 = disabledComponent;
                String[] strArr4 = requiredAppsList;
                Set<String> set = requiredAppsSet2;
                while (true) {
                    break;
                }
                throw th;
            }
        }
        while (it.hasNext()) {
            ParceledListSlice<ResolveInfo> parceledList2 = parceledList;
            ResolveInfo resolveInfo = (ResolveInfo) it.next();
            ComponentName component2 = resolveInfo.activityInfo.getComponentName();
            if (component2 == null || isSupportCloneAppInCust(resolveInfo.activityInfo.packageName)) {
                procIntent2 = procIntent;
                it2 = it;
            } else {
                try {
                    ResolveInfo resolveInfo2 = resolveInfo;
                    procIntent2 = procIntent;
                    try {
                        this.mPmsInner.setComponentEnabledSettingInner(component2, 2, 1, i2);
                        Slog.i(TAG, "disableReceiversForClone package [" + component2 + "] for user " + i2);
                        it2 = it;
                    } catch (IllegalArgumentException | SecurityException e2) {
                        e = e2;
                        it2 = it;
                        Slog.d(TAG, "disableReceiversForClone exception:" + e.getMessage());
                        parceledList = parceledList2;
                        procIntent = procIntent2;
                        it = it2;
                    } catch (Exception e3) {
                        ex = e3;
                        StringBuilder sb = new StringBuilder();
                        it2 = it;
                        sb.append("disableReceiversForClone Exception ");
                        sb.append(ex);
                        Slog.e(TAG, sb.toString());
                        parceledList = parceledList2;
                        procIntent = procIntent2;
                        it = it2;
                    }
                } catch (IllegalArgumentException | SecurityException e4) {
                    e = e4;
                    ResolveInfo resolveInfo3 = resolveInfo;
                    procIntent2 = procIntent;
                    it2 = it;
                    Slog.d(TAG, "disableReceiversForClone exception:" + e.getMessage());
                    parceledList = parceledList2;
                    procIntent = procIntent2;
                    it = it2;
                } catch (Exception e5) {
                    ex = e5;
                    ResolveInfo resolveInfo4 = resolveInfo;
                    procIntent2 = procIntent;
                    StringBuilder sb2 = new StringBuilder();
                    it2 = it;
                    sb2.append("disableReceiversForClone Exception ");
                    sb2.append(ex);
                    Slog.e(TAG, sb2.toString());
                    parceledList = parceledList2;
                    procIntent = procIntent2;
                    it = it2;
                }
            }
            parceledList = parceledList2;
            procIntent = procIntent2;
            it = it2;
        }
        Intent procIntent3 = procIntent;
        int i3 = i3 + 1;
        String[] procList2 = procList;
        int length = i;
        requiredAppsSet2 = requiredAppsSet;
        Intent procIntent4 = procIntent3;
    }

    public static void initCloneAppsFromCust() {
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP) {
            File configFile = HwPackageManagerUtils.getCustomizedFileName(CLONE_APP_LIST, 0);
            if (configFile == null || !configFile.exists()) {
                Flog.i(205, "hw_clone_app_list.xml does not exists.");
                return;
            }
            InputStream inputStream = null;
            try {
                InputStream inputStream2 = new FileInputStream(configFile);
                XmlPullParser xmlParser = Xml.newPullParser();
                xmlParser.setInput(inputStream2, null);
                while (true) {
                    int next = xmlParser.next();
                    int xmlEventType = next;
                    if (next == 1) {
                        try {
                            break;
                        } catch (IOException e) {
                            Slog.e(TAG, "initCloneAppsFromCust:- IOE while closing stream", e);
                        }
                    } else if (xmlEventType == 2 && "package".equals(xmlParser.getName())) {
                        String packageName = xmlParser.getAttributeValue(null, "name");
                        if (!TextUtils.isEmpty(packageName)) {
                            SUPPORT_CLONE_APPS.add(packageName);
                        }
                    }
                }
                inputStream2.close();
            } catch (FileNotFoundException e2) {
                Log.e(TAG, "initCloneAppsFromCust");
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (XmlPullParserException e3) {
                Log.e(TAG, "initCloneAppsFromCust", e3);
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e4) {
                Log.e(TAG, "initCloneAppsFromCust", e4);
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                        Slog.e(TAG, "initCloneAppsFromCust:- IOE while closing stream", e5);
                    }
                }
                throw th;
            }
        }
    }

    public boolean checkUidPermissionGranted(String permName, int uid) {
        if (UserHandle.getUserId(uid) == 0 || !HwActivityManagerService.IS_SUPPORT_CLONE_APP || !PackageManagerService.sUserManager.isClonedProfile(UserHandle.getUserId(uid)) || (!"android.permission.INTERACT_ACROSS_USERS_FULL".equals(permName) && !"android.permission.INTERACT_ACROSS_USERS".equals(permName))) {
            return false;
        }
        return true;
    }

    public boolean checkPermissionGranted(String permName, int userId) {
        if (userId == 0 || !HwActivityManagerService.IS_SUPPORT_CLONE_APP || ((!"android.permission.INTERACT_ACROSS_USERS_FULL".equals(permName) && !"android.permission.INTERACT_ACROSS_USERS".equals(permName)) || !PackageManagerService.sUserManager.isClonedProfile(userId))) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x00bb  */
    public void restoreAppDataForClone(String pkgName, int parentUserId, int clonedProfileUserId) {
        int i;
        PackageParser.Package pkg;
        List<String> requestedPermissions;
        PackageParser.Package pkg2;
        String str = pkgName;
        int i2 = clonedProfileUserId;
        Installer mInstaller = this.mPmsInner.getInstallerInner();
        PackageParser.Package pkg3 = (PackageParser.Package) this.mPmsInner.getPackagesLock().get(str);
        String volumeUuid = pkg3.volumeUuid;
        String packageName = pkg3.packageName;
        ApplicationInfo app = pkg3.applicationInfo;
        int appId = UserHandle.getAppId(app.uid);
        String parentDataUserCePkgDir = Environment.getDataUserCePackageDirectory(volumeUuid, parentUserId, packageName).getPath() + File.separator + "_hwclone";
        String parentDataUserDePkgDir = Environment.getDataUserDePackageDirectory(volumeUuid, parentUserId, packageName).getPath() + File.separator + "_hwclone";
        String cloneDataUserCePkgDir = Environment.getDataUserCePackageDirectory(volumeUuid, i2, packageName).getPath();
        String cloneDataUserDePkgDir = Environment.getDataUserDePackageDirectory(volumeUuid, i2, packageName).getPath();
        Preconditions.checkNotNull(app.seInfo);
        try {
            int i3 = i2;
            ApplicationInfo applicationInfo = app;
            String str2 = packageName;
            String str3 = volumeUuid;
            pkg = pkg3;
            try {
                mInstaller.restoreCloneAppData(volumeUuid, packageName, i3, 3, appId, app.seInfo, parentDataUserCePkgDir, cloneDataUserCePkgDir, parentDataUserDePkgDir, cloneDataUserDePkgDir);
            } catch (Exception e) {
                e = e;
            }
        } catch (Exception e2) {
            e = e2;
            ApplicationInfo applicationInfo2 = app;
            String str4 = packageName;
            String str5 = volumeUuid;
            pkg = pkg3;
            Slog.e(TAG, "failed to restore clone app data for  " + str, e);
            requestedPermissions = pkg.requestedPermissions;
            if (requestedPermissions != null) {
            }
            int i4 = clonedProfileUserId;
        }
        requestedPermissions = pkg.requestedPermissions;
        if (requestedPermissions != null) {
            for (String perm : requestedPermissions) {
                if (this.mPmsInner.checkPermissionInner(perm, str, i) == 0) {
                    pkg2 = pkg;
                    int i5 = clonedProfileUserId;
                    if (-1 == this.mPmsInner.checkPermissionInner(perm, str, i5)) {
                        this.mPmsInner.grantRuntimePermissionInner(str, perm, i5);
                    }
                } else {
                    pkg2 = pkg;
                    int i6 = clonedProfileUserId;
                }
                pkg = pkg2;
            }
        }
        int i42 = clonedProfileUserId;
    }

    public void deletePackageVersioned(VersionedPackage versionedPackage, IPackageDeleteObserver2 observer, int userId, int deleteFlags) {
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP && (deleteFlags & 2) == 0 && versionedPackage != null && SUPPORT_CLONE_APPS.contains(versionedPackage.getPackageName()) && userId != 0 && PackageManagerService.sUserManager.isClonedProfile(userId)) {
            PackageParser.Package p = (PackageParser.Package) this.mPmsInner.getPackagesLock().get(versionedPackage.getPackageName());
            if (!(p == null || (p.applicationInfo.flags & 1) == 0)) {
                deleteFlags |= 4;
            }
        }
        if (versionedPackage != null) {
            this.mPmsInner.deletePackageVersionedImpl(versionedPackage, observer, userId, deleteFlags);
        }
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP && (deleteFlags & 2) == 0 && versionedPackage != null && SUPPORT_CLONE_APPS.contains(versionedPackage.getPackageName())) {
            long ident = Binder.clearCallingIdentity();
            try {
                for (UserInfo ui : PackageManagerService.sUserManager.getProfiles(userId, false)) {
                    if (ui.isClonedProfile() && ui.id != userId && ui.profileGroupId == userId) {
                        PackageSetting pkgSetting = (PackageSetting) this.mPmsInner.getSettings().mPackages.get(versionedPackage.getPackageName());
                        if (pkgSetting != null && pkgSetting.getInstalled(ui.id)) {
                            this.mPmsInner.deletePackageVersionedImpl(versionedPackage, observer, ui.id, deleteFlags);
                        }
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void deleteClonedProfileIfNeed(int[] removedUsers) {
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP && removedUsers != null && removedUsers.length > 0) {
            int length = removedUsers.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                int userId = removedUsers[i];
                long callingId = Binder.clearCallingIdentity();
                try {
                    UserInfo userInfo = PackageManagerService.sUserManager.getUserInfo(userId);
                    if (userInfo != null && userInfo.isClonedProfile() && !isAnyApkInstalledInClonedProfile(userId)) {
                        PackageManagerService.sUserManager.removeUser(userId);
                        Slog.i(TAG, "Remove cloned profile " + userId);
                        Intent clonedProfileIntent = new Intent("android.intent.action.USER_REMOVED");
                        clonedProfileIntent.setPackage(GestureNavConst.DEFAULT_LAUNCHER_PACKAGE);
                        clonedProfileIntent.addFlags(1342177280);
                        clonedProfileIntent.putExtra("android.intent.extra.USER", new UserHandle(userId));
                        clonedProfileIntent.putExtra("android.intent.extra.user_handle", userId);
                        this.mPmsContext.sendBroadcastAsUser(clonedProfileIntent, new UserHandle(userInfo.profileGroupId), null);
                        break;
                    }
                    Binder.restoreCallingIdentity(callingId);
                    i++;
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }
    }

    private boolean isAnyApkInstalledInClonedProfile(int clonedProfileUserId) {
        Intent launcherIntent = new Intent("android.intent.action.MAIN");
        launcherIntent.addCategory("android.intent.category.LAUNCHER");
        return this.mPmsInner.queryIntentActivitiesInner(launcherIntent, launcherIntent.resolveTypeIfNeeded(this.mPmsContext.getContentResolver()), 786432, clonedProfileUserId).getList().size() > 0;
    }

    public void preSendPackageBroadcast(String action, String pkg, Bundle extras, int flags, String targetPkg, IIntentReceiver finishedReceiver, int[] userIds, int[] instantUserIds) {
        String str = action;
        String str2 = pkg;
        int[] iArr = userIds;
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP && this.mPmsInner.getUserManagerInternalInner().hasClonedProfile()) {
            if (("android.intent.action.PACKAGE_ADDED".equals(str) || ("android.intent.action.PACKAGE_CHANGED".equals(str) && iArr != null)) && !SUPPORT_CLONE_APPS.contains(str2)) {
                long callingId = Binder.clearCallingIdentity();
                int cloneUserId = -1;
                if (iArr != null) {
                    int length = iArr.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        }
                        int userId = iArr[i];
                        if (userId != 0 && PackageManagerService.sUserManager.isClonedProfile(userId)) {
                            cloneUserId = userId;
                            break;
                        }
                        i++;
                    }
                } else {
                    try {
                        UserInfo userInfo = this.mPmsInner.getUserManagerInternalInner().findClonedProfile();
                        if (userInfo != null) {
                            cloneUserId = userInfo.id;
                        }
                    } catch (IllegalArgumentException | SecurityException e) {
                        Slog.d(TAG, "Set required Apps' component disabled failed" + e.getMessage());
                    } catch (Exception e2) {
                        Slog.e(TAG, "Set required Apps' component disabled failed");
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(callingId);
                        throw th;
                    }
                }
                Intent launcherIntent = new Intent("android.intent.action.MAIN");
                launcherIntent.addCategory("android.intent.category.LAUNCHER");
                launcherIntent.setPackage(str2);
                ParceledListSlice<ResolveInfo> parceledList = this.mPmsInner.queryIntentActivitiesInner(launcherIntent, launcherIntent.resolveTypeIfNeeded(this.mPmsContext.getContentResolver()), 786432, cloneUserId);
                if (parceledList != null) {
                    for (ResolveInfo resolveInfo : parceledList.getList()) {
                        this.mPmsInner.setComponentEnabledSettingInner(resolveInfo.activityInfo.getComponentName(), 2, 1, cloneUserId);
                    }
                }
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    public int updateFlags(int flags, int userId) {
        if (!HwActivityManagerService.IS_SUPPORT_CLONE_APP || userId == 0 || !this.mPmsInner.getUserManagerInternalInner().isClonedProfile(userId)) {
            return flags;
        }
        int callingUid = Binder.getCallingUid();
        if (userId != UserHandle.getUserId(callingUid) || !SUPPORT_CLONE_APPS.contains(this.mPmsInner.getNameForUidInner(callingUid))) {
            return flags;
        }
        return flags | 4202496;
    }

    public List<ResolveInfo> queryIntentActivitiesInternal(Intent intent, String resolvedType, int flags, int filterCallingUid, int userId, boolean resolveForStart, boolean allowDynamicSplits) {
        int flags2;
        int i = userId;
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP && i != 0) {
            int callingUid = Binder.getCallingUid();
            UserInfo ui = this.mPmsInner.getUserManagerInternalInner().getUserInfo(i);
            if (ui != null && ui.isClonedProfile() && i == UserHandle.getUserId(callingUid)) {
                boolean shouldCheckUninstall = (flags & 4202496) != 0 && UserHandle.getAppId(callingUid) == 1000;
                if (SUPPORT_CLONE_APPS.contains(this.mPmsInner.getNameForUidInner(callingUid))) {
                    if ((flags & 4202496) == 0) {
                        shouldCheckUninstall = true;
                    }
                    flags2 = flags | 4202496;
                } else {
                    flags2 = flags;
                }
                boolean shouldCheckUninstall2 = shouldCheckUninstall;
                List<ResolveInfo> result = this.mPmsInner.queryIntentActivitiesInternalImpl(intent, resolvedType, flags2, filterCallingUid, i, resolveForStart, allowDynamicSplits);
                if (shouldCheckUninstall2) {
                    Iterator<ResolveInfo> iterator = result.iterator();
                    while (iterator.hasNext()) {
                        ResolveInfo ri = iterator.next();
                        if (!this.mPmsInner.getSettings().isEnabledAndMatchLPr(ri.activityInfo, 786432, ui.profileGroupId) && !this.mPmsInner.getSettings().isEnabledAndMatchLPr(ri.activityInfo, 786432, i)) {
                            iterator.remove();
                        }
                    }
                }
                return result;
            }
        }
        return this.mPmsInner.queryIntentActivitiesInternalImpl(intent, resolvedType, flags, filterCallingUid, i, resolveForStart, allowDynamicSplits);
    }

    public ActivityInfo getActivityInfo(ComponentName component, int flags, int userId) {
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP && userId != 0) {
            int callingUid = Binder.getCallingUid();
            UserInfo ui = this.mPmsInner.getUserManagerInternalInner().getUserInfo(userId);
            if (ui != null && ui.isClonedProfile() && userId == UserHandle.getUserId(callingUid)) {
                boolean shouldCheckUninstall = (flags & 4202496) != 0 && UserHandle.getAppId(callingUid) == 1000;
                if (SUPPORT_CLONE_APPS.contains(this.mPmsInner.getNameForUidInner(callingUid))) {
                    if ((flags & 4202496) == 0) {
                        shouldCheckUninstall = true;
                    }
                    flags |= 4202496;
                }
                ActivityInfo ai = this.mPmsInner.getActivityInfoInternalInner(component, flags, Binder.getCallingUid(), userId);
                if (!shouldCheckUninstall || ai == null || this.mPmsInner.getSettings().isEnabledAndMatchLPr(ai, 786432, ui.profileGroupId) || this.mPmsInner.getSettings().isEnabledAndMatchLPr(ai, 786432, userId)) {
                    return ai;
                }
                return null;
            }
        }
        return this.mPmsInner.getActivityInfoInternalInner(component, flags, Binder.getCallingUid(), userId);
    }

    public boolean isPackageAvailable(String packageName, int userId) {
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP && userId != 0) {
            int callingUid = Binder.getCallingUid();
            if (userId == UserHandle.getUserId(callingUid)) {
                long callingId = Binder.clearCallingIdentity();
                try {
                    UserInfo ui = PackageManagerService.sUserManager.getUserInfo(userId);
                    if (ui.isClonedProfile() && SUPPORT_CLONE_APPS.contains(this.mPmsInner.getNameForUidInner(callingUid))) {
                        return this.mPmsInner.isPackageAvailableImpl(packageName, ui.profileGroupId);
                    }
                    Binder.restoreCallingIdentity(callingId);
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }
        return this.mPmsInner.isPackageAvailableImpl(packageName, userId);
    }

    public void preInstallExistingPackageAsUser(String packageName, int userId, int installFlags, int installReason) {
        if (userId != 0 && SUPPORT_CLONE_APPS.contains(packageName) && this.mPmsInner.getUserManagerInternalInner().isClonedProfile(userId)) {
            long callingId = Binder.clearCallingIdentity();
            try {
                this.mPmsInner.setPackageStoppedStateInner(packageName, true, userId);
                Slog.d(TAG, packageName + " is set stopped for user " + userId);
            } catch (IllegalArgumentException e) {
                Slog.w(TAG, "error in setPackageStoppedState for " + e.getMessage());
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(callingId);
                throw th;
            }
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public UserHandle getHwUserHandle(UserHandle user) {
        return new UserHandle(redirectInstallForClone(user.getIdentifier()));
    }

    private int redirectInstallForClone(int userId) {
        if (!HwActivityManagerService.IS_SUPPORT_CLONE_APP) {
            return userId;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            UserInfo ui = PackageManagerService.sUserManager.getUserInfo(userId);
            if (ui != null && ui.isClonedProfile()) {
                return ui.profileGroupId;
            }
            Binder.restoreCallingIdentity(ident);
            return userId;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }
}
