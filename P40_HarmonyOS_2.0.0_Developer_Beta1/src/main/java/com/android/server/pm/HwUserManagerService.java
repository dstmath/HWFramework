package com.android.server.pm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.HiLog;
import com.huawei.android.app.AppGlobalsEx;
import com.huawei.android.content.pm.IPackageManagerEx;
import com.huawei.android.content.pm.UserInfoExt;
import com.huawei.android.os.EnvironmentEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.provider.SettingsEx;
import java.io.File;
import java.util.List;

public class HwUserManagerService extends UserManagerServiceEx {
    private static final int APPEXECFWK_DOMAIN = 218108160;
    private static final int CREATE_USER_STATUS = 0;
    private static final int DELETE_USER_STATUS = 1;
    private static final boolean IS_SUPPORT_CLONE_APP = SystemPropertiesEx.getBoolean("ro.config.hw_support_clone_app", false);
    private static final String TAG = "HwUserManagerService";
    private static boolean isSupportJni;
    private static HwUserManagerService sInstance = null;
    private Context mContext;

    private native void nativeSendUserChangedNotification(int i, int i2);

    static {
        isSupportJni = false;
        try {
            System.loadLibrary("hwtee_jni");
            isSupportJni = true;
        } catch (UnsatisfiedLinkError e) {
            HiLog.e((int) APPEXECFWK_DOMAIN, TAG, false, "can not find lib hwtee_jni", new Object[0]);
            isSupportJni = false;
        }
    }

    public HwUserManagerService(Context context, PackageManagerServiceEx pm, UserDataPreparerEx userDataPreparer, Object packagesLock) {
        super(context, pm, userDataPreparer, packagesLock);
        this.mContext = context;
        sInstance = this;
    }

    public static synchronized HwUserManagerService getInstance() {
        HwUserManagerService hwUserManagerService;
        synchronized (HwUserManagerService.class) {
            hwUserManagerService = sInstance;
        }
        return hwUserManagerService;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code != 1001) {
            return HwUserManagerService.super.onTransact(code, data, reply, flags);
        }
        HiLog.i((int) APPEXECFWK_DOMAIN, TAG, false, "onTransact MKDIR_FOR_USER_TRANSACTION.", new Object[0]);
        if (data == null) {
            return false;
        }
        createUserDir(data.readInt());
        return true;
    }

    public UserInfoExt createProfileForUser(String name, int flags, int userId, String[] disallowedPackages) {
        if (isStorageLow()) {
            return null;
        }
        boolean isClonedProfile = (67108864 & flags) != 0;
        UserInfoExt parent = null;
        if (IS_SUPPORT_CLONE_APP && isClonedProfile) {
            if ("1".equals(SystemPropertiesEx.get("persist.sys.primarysd", "0"))) {
                HiLog.i((int) APPEXECFWK_DOMAIN, TAG, false, "current default location is external sdcard and forbid to create user", new Object[0]);
                return null;
            } else if (userId != 0) {
                return null;
            } else {
                for (UserInfoExt user : HwUserManagerService.super.getProfiles(userId, true)) {
                    if (user.isClonedProfile()) {
                        return null;
                    }
                    if (user.getUserId() == userId) {
                        parent = user;
                        if (!parent.canHaveProfile()) {
                            return null;
                        }
                    }
                }
            }
        }
        UserInfoExt userInfo = HwUserManagerService.super.createProfileForUser(name, flags, userId, disallowedPackages);
        if (!(!isClonedProfile || parent == null || userInfo == null)) {
            pretreatClonedProfile(getmPm(), parent.getUserId(), userInfo.getUserId());
        }
        if (userInfo != null) {
            hwCreateUser(userInfo.getUserId());
        }
        return userInfo;
    }

    public UserInfoExt createUser(String name, int flags) {
        if (isStorageLow()) {
            return null;
        }
        if ((33554432 & flags) != 0) {
            for (UserInfoExt info : getUsers(true)) {
                if (info.isHwHiddenSpace()) {
                    HiLog.e((int) APPEXECFWK_DOMAIN, TAG, false, "Hidden space already exist!", new Object[0]);
                    return null;
                }
            }
        }
        UserInfoExt userInfo = HwUserManagerService.super.createUser(name, flags);
        if (userInfo == null) {
            return null;
        }
        if (userInfo.isGuest()) {
            HiLog.i((int) APPEXECFWK_DOMAIN, TAG, false, "Create one guest, disable setup activity.", new Object[0]);
            disableSetupActivity(userInfo.getUserId());
        }
        hwCreateUser(userInfo.getUserId());
        return userInfo;
    }

    /* access modifiers changed from: package-private */
    public void disableSetupActivity(int userId) {
        Intent mainIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT");
        try {
            ResolveInfo info = AppGlobalsEx.getPackageManager().resolveIntent(mainIntent, mainIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 0, userId);
            if (!(info == null || info.activityInfo == null)) {
                if (info.activityInfo.applicationInfo != null) {
                    if (info.priority <= 0) {
                        HiLog.i((int) APPEXECFWK_DOMAIN, TAG, false, "disableSetupActivity did not found setup activity.", new Object[0]);
                        return;
                    }
                    IPackageManagerEx.setApplicationEnabledSetting(info.activityInfo.applicationInfo.packageName, 3, 0, userId, (String) null);
                    long identity = Binder.clearCallingIdentity();
                    SettingsEx.Secure.putIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 1, userId);
                    Binder.restoreCallingIdentity(identity);
                    return;
                }
            }
            HiLog.i((int) APPEXECFWK_DOMAIN, TAG, false, "disableSetupActivity found resolveinfo null.", new Object[0]);
        } catch (RemoteException e) {
            HiLog.i((int) APPEXECFWK_DOMAIN, TAG, false, "disableSetupActivity remote error %{public}s", new Object[]{e.toString()});
        }
    }

    public void finishRemoveUser(int userHandle) {
        HwUserManagerService.super.finishRemoveUser(userHandle);
        hwRemoveUser(userHandle);
    }

    private void hwCreateUser(int userid) {
        if (userid > 0 && isSupportJni) {
            HiLog.i((int) APPEXECFWK_DOMAIN, TAG, false, "native create user %{public}d", new Object[]{Integer.valueOf(userid)});
            nativeSendUserChangedNotification(0, userid);
        }
    }

    private void hwRemoveUser(int userid) {
        if (userid > 0 && isSupportJni) {
            HiLog.i((int) APPEXECFWK_DOMAIN, TAG, false, "native remove user %{public}d", new Object[]{Integer.valueOf(userid)});
            nativeSendUserChangedNotification(1, userid);
        }
    }

    private void createUserDir(int userId) {
        File userDir = EnvironmentEx.getUserSystemDirectory(userId);
        if (userDir != null && !userDir.exists() && !userDir.mkdir()) {
            HiLog.w((int) APPEXECFWK_DOMAIN, TAG, false, "Failed to create user directory for %{public}d", new Object[]{Integer.valueOf(userId)});
        }
    }

    private boolean isStorageLow() {
        boolean isStorageLow = new PackageManagerServiceEx().isStorageLow();
        HiLog.i((int) APPEXECFWK_DOMAIN, TAG, false, "PackageManagerService.isStorageLow() = %{public}b", new Object[]{Boolean.valueOf(isStorageLow)});
        return isStorageLow;
    }

    private void pretreatClonedProfile(PackageManagerServiceEx pm, int parentUserId, int clonedProfileUserId) {
        if (IS_SUPPORT_CLONE_APP) {
            long callingId = Binder.clearCallingIdentity();
            try {
                pm.deleteNonRequiredAppsForClone(clonedProfileUserId, true);
                restoreDataForClone(pm, parentUserId, clonedProfileUserId);
                pm.flushPackageRestrictionsAsUser(clonedProfileUserId);
                HwUserManagerService.super.setUserRestriction("no_outgoing_calls", false, clonedProfileUserId);
                HwUserManagerService.super.setUserRestriction("no_sms", false, clonedProfileUserId);
                SettingsEx.Secure.putIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 1, clonedProfileUserId);
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    private void restoreDataForClone(PackageManagerServiceEx pm, int parentUserId, int clonedProfileUserId) {
        if (parentUserId == 0 && pm != null) {
            String cloneAppList = SettingsEx.Secure.getStringForUser(this.mContext.getContentResolver(), "clone_app_list", parentUserId);
            if (!TextUtils.isEmpty(cloneAppList)) {
                String[] pkgs = cloneAppList.split(";");
                for (String pkg : pkgs) {
                    if (!TextUtils.isEmpty(pkg)) {
                        if (pm.getPackageInfo(pkg, 0, parentUserId) != null) {
                            HiLog.i((int) APPEXECFWK_DOMAIN, TAG, false, "Install existing package [%{public}s] as user %{public}d", new Object[]{pkg, Integer.valueOf(clonedProfileUserId)});
                            pm.installExistingPackageAsUser(pkg, clonedProfileUserId, 0, 0, (List) null);
                            pm.setPackageStoppedState(pkg, false, clonedProfileUserId);
                        }
                    }
                }
            }
        }
    }

    public boolean isClonedProfile(int userId) {
        if (userId == 0) {
            return false;
        }
        boolean isClonedProfile = false;
        long ident = Binder.clearCallingIdentity();
        try {
            UserInfoExt userInfo = HwUserManagerService.super.getUserInfo(userId);
            if (userInfo != null) {
                isClonedProfile = userInfo.isClonedProfile();
            }
            return isClonedProfile;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public UserInfoExt getUserInfo(int userId) {
        int callingUserId = UserHandleEx.getUserId(Binder.getCallingUid());
        if (!isClonedProfile(userId) && !isClonedProfile(callingUserId)) {
            return HwUserManagerService.super.getUserInfo(userId);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            return HwUserManagerService.super.getUserInfo(userId);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }
}
