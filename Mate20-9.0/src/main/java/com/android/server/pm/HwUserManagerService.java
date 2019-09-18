package com.android.server.pm;

import android.app.AppGlobals;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Environment;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Slog;
import com.android.server.am.HwActivityManagerService;
import java.io.File;

public class HwUserManagerService extends UserManagerService {
    private static final int CREATE_USER_STATUS = 0;
    private static final int DELETE_USER_STATUS = 1;
    private static final String TAG = "HwUserManagerService";
    private static boolean isSupportJni;
    private static HwUserManagerService mInstance = null;
    private Context mContext;

    private native void nativeSendUserChangedNotification(int i, int i2);

    static {
        isSupportJni = false;
        try {
            System.loadLibrary("hwtee_jni");
            isSupportJni = true;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "can not find lib hwtee_jni");
            isSupportJni = false;
        }
    }

    public HwUserManagerService(Context context, PackageManagerService pm, UserDataPreparer userDataPreparer, Object packagesLock) {
        super(context, pm, userDataPreparer, packagesLock);
        this.mContext = context;
        mInstance = this;
    }

    public static synchronized HwUserManagerService getInstance() {
        HwUserManagerService hwUserManagerService;
        synchronized (HwUserManagerService.class) {
            hwUserManagerService = mInstance;
        }
        return hwUserManagerService;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code != 1001) {
            return HwUserManagerService.super.onTransact(code, data, reply, flags);
        }
        Flog.i(900, "onTransact MKDIR_FOR_USER_TRANSACTION.");
        createUserDir(data.readInt());
        return true;
    }

    public UserInfo createProfileForUser(String name, int flags, int userId, String[] disallowedPackages) {
        if (isStorageLow()) {
            return null;
        }
        boolean isClonedProfile = (67108864 & flags) != 0;
        UserInfo parent = null;
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP && isClonedProfile) {
            if ("1".equals(SystemProperties.get("persist.sys.primarysd", "0"))) {
                Slog.i(TAG, "current default location is external sdcard and forbid to create user");
                return null;
            } else if (userId != 0) {
                return null;
            } else {
                for (UserInfo user : HwUserManagerService.super.getProfiles(userId, true)) {
                    if (user.isClonedProfile()) {
                        return null;
                    }
                    if (user.id == userId) {
                        parent = user;
                        if (!parent.canHaveProfile()) {
                            return null;
                        }
                    }
                }
            }
        }
        UserInfo ui = HwUserManagerService.super.createProfileForUser(name, flags, userId, disallowedPackages);
        if (!(!isClonedProfile || parent == null || ui == null)) {
            pretreatClonedProfile(this.mPm, parent.id, ui.id);
        }
        if (ui != null) {
            hwCreateUser(ui.id);
        }
        return ui;
    }

    public UserInfo createUser(String name, int flags) {
        if (isStorageLow()) {
            return null;
        }
        if ((33554432 & flags) != 0) {
            for (UserInfo info : getUsers(true)) {
                if (info.isHwHiddenSpace()) {
                    Slog.e(TAG, "Hidden space already exist!");
                    return null;
                }
            }
        }
        UserInfo ui = HwUserManagerService.super.createUser(name, flags);
        if (ui == null) {
            return null;
        }
        if (ui.isGuest()) {
            Flog.i(900, "Create a guest, disable setup activity.");
            disableSetupActivity(ui.id);
        }
        hwCreateUser(ui.id);
        setDeviceProvisioned(ui.id);
        return ui;
    }

    /* access modifiers changed from: package-private */
    public void disableSetupActivity(int userId) {
        Intent mainIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT");
        IPackageManager pm = AppGlobals.getPackageManager();
        try {
            ResolveInfo info = pm.resolveIntent(mainIntent, mainIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 0, userId);
            if (!(info == null || info.activityInfo == null)) {
                if (info.activityInfo.applicationInfo != null) {
                    if (info.priority <= 0) {
                        Flog.i(900, "disableSetupActivity did not found setup activity.");
                        return;
                    }
                    pm.setApplicationEnabledSetting(info.activityInfo.applicationInfo.packageName, 3, 0, userId, null);
                    long identity = Binder.clearCallingIdentity();
                    Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 1, userId);
                    Binder.restoreCallingIdentity(identity);
                    return;
                }
            }
            Flog.i(900, "disableSetupActivity found resolveinfo null.");
        } catch (RemoteException e) {
            Flog.i(900, "disableSetupActivity remote error " + e);
        }
    }

    /* access modifiers changed from: package-private */
    public void setDeviceProvisioned(int userId) {
        Flog.i(900, "HwUserManagerService setDeviceProvisioned, userId " + userId);
        ContentResolver cr = this.mContext.getContentResolver();
        long identity = Binder.clearCallingIdentity();
        try {
            if ((Settings.Global.getInt(cr, "device_provisioned", 0) == 0 || Settings.Secure.getIntForUser(cr, "user_setup_complete", 0, userId) == 0) && ServiceManager.getService("package").getHwPMSEx().isSetupDisabled()) {
                Flog.i(900, "Setup is disabled putInt USER_SETUP_COMPLETE for userId " + userId);
                Settings.Global.putInt(cr, "device_provisioned", 1);
                Settings.Secure.putIntForUser(cr, "user_setup_complete", 1, userId);
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* access modifiers changed from: package-private */
    public void finishRemoveUser(int userHandle) {
        HwUserManagerService.super.finishRemoveUser(userHandle);
        hwRemoveUser(userHandle);
    }

    private void hwCreateUser(int userid) {
        if (userid > 0 && isSupportJni) {
            Slog.i(TAG, "native create user " + userid);
            nativeSendUserChangedNotification(0, userid);
        }
    }

    private void hwRemoveUser(int userid) {
        if (userid > 0 && isSupportJni) {
            Slog.i(TAG, "native remove user " + userid);
            nativeSendUserChangedNotification(1, userid);
        }
    }

    private void createUserDir(int userId) {
        File userDir = Environment.getUserSystemDirectory(userId);
        if (!userDir.exists() && !userDir.mkdir()) {
            Slog.w(TAG, "Failed to create user directory for " + userId);
        }
    }

    private boolean isStorageLow() {
        boolean isStorageLow = ServiceManager.getService("package").isStorageLow();
        Slog.i(TAG, "PackageManagerService.isStorageLow() = " + isStorageLow);
        return isStorageLow;
    }

    private void pretreatClonedProfile(PackageManagerService pm, int parentUserId, int clonedProfileUserId) {
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP) {
            long callingId = Binder.clearCallingIdentity();
            try {
                pm.deleteNonRequiredAppsForClone(clonedProfileUserId, true);
                restoreDataForClone(pm, parentUserId, clonedProfileUserId);
                pm.flushPackageRestrictionsAsUser(clonedProfileUserId);
                HwUserManagerService.super.setUserRestriction("no_outgoing_calls", false, clonedProfileUserId);
                HwUserManagerService.super.setUserRestriction("no_sms", false, clonedProfileUserId);
                Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 1, clonedProfileUserId);
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    private void restoreDataForClone(PackageManagerService pm, int parentUserId, int clonedProfileUserId) {
        if (parentUserId == 0) {
            String cloneAppList = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "clone_app_list", parentUserId);
            if (!TextUtils.isEmpty(cloneAppList)) {
                for (String pkg : cloneAppList.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                    if (!TextUtils.isEmpty(pkg) && pm.getPackageInfo(pkg, 0, parentUserId) != null) {
                        Slog.i(TAG, "Install existing package [" + pkg + "] as user " + clonedProfileUserId);
                        pm.installExistingPackageAsUser(pkg, clonedProfileUserId, 0, 0);
                        pm.setPackageStoppedState(pkg, false, clonedProfileUserId);
                        pm.restoreAppDataForClone(pkg, parentUserId, clonedProfileUserId);
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
            UserInfo ui = HwUserManagerService.super.getUserInfo(userId);
            if (ui != null) {
                isClonedProfile = ui.isClonedProfile();
            }
            return isClonedProfile;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public UserInfo getUserInfo(int userId) {
        int callingUserId = UserHandle.getUserId(Binder.getCallingUid());
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
