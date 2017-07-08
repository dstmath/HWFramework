package com.android.server.pm;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Environment;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.util.Flog;
import android.util.Slog;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.security.trustcircle.IOTController;
import com.huawei.android.os.UserManagerEx;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.io.File;

public class HwUserManagerService extends UserManagerService {
    private static final int CREATE_USER_STATUS = 0;
    private static final int DELETE_USER_STATUS = 1;
    private static final String TAG = "HwUserManagerService";
    private static boolean isSupportJni;
    private static HwUserManagerService mInstance;
    private Context mContext;

    private native void nativeSendUserChangedNotification(int i, int i2);

    static {
        mInstance = null;
        isSupportJni = false;
        try {
            System.loadLibrary("hwtee_jni");
            isSupportJni = true;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "can not find lib hwtee_jni");
            isSupportJni = false;
        }
    }

    public HwUserManagerService(Context context, PackageManagerService pm, Object packagesLock) {
        super(context, pm, packagesLock);
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
        switch (code) {
            case IOTController.TYPE_SLAVE /*1001*/:
                createUserDir(data.readInt());
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    public UserInfo createProfileForUser(String name, int flags, int userid) {
        if (isStorageLow()) {
            return null;
        }
        UserInfo ui = super.createProfileForUser(name, flags, userid);
        if (ui != null) {
            hwCreateUser(ui.id);
        }
        return ui;
    }

    public UserInfo createUser(String name, int flags) {
        boolean isHwHiddenSpace = false;
        if (isStorageLow()) {
            return null;
        }
        if ((HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM & flags) != 0) {
            isHwHiddenSpace = true;
        }
        if (isHwHiddenSpace) {
            for (UserInfo info : getUsers(true)) {
                if (UserManagerEx.isHwHiddenSpace(info)) {
                    Slog.e(TAG, "Hidden space already exist!");
                    return null;
                }
            }
        }
        UserInfo ui = super.createUser(name, flags);
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

    void disableSetupActivity(int userId) {
        Intent mainIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT");
        IPackageManager pm = AppGlobals.getPackageManager();
        try {
            ResolveInfo info = pm.resolveIntent(mainIntent, mainIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), CREATE_USER_STATUS, userId);
            if (info == null || info.activityInfo == null || info.activityInfo.applicationInfo == null) {
                Flog.i(900, "disableSetupActivity found resolveinfo null.");
            } else if (info.priority <= 0) {
                Flog.i(900, "disableSetupActivity did not found setup activity.");
            } else {
                ActivityInfo ai = info.activityInfo;
                pm.setComponentEnabledSetting(new ComponentName(ai.applicationInfo.packageName, ai.name), 2, DELETE_USER_STATUS, userId);
                long identity = Binder.clearCallingIdentity();
                Secure.putIntForUser(this.mContext.getContentResolver(), "user_setup_complete", DELETE_USER_STATUS, userId);
                Binder.restoreCallingIdentity(identity);
            }
        } catch (RemoteException e) {
            Flog.i(900, "disableSetupActivity remote error " + e);
        }
    }

    void setDeviceProvisioned(int userId) {
        Flog.i(900, "HwUserManagerService setDeviceProvisioned, userId " + userId);
        ContentResolver cr = this.mContext.getContentResolver();
        long identity = Binder.clearCallingIdentity();
        try {
            if ((Global.getInt(cr, "device_provisioned", CREATE_USER_STATUS) == 0 || Secure.getIntForUser(cr, "user_setup_complete", CREATE_USER_STATUS, userId) == 0) && ((PackageManagerService) ServiceManager.getService(ControlScope.PACKAGE_ELEMENT_KEY)).isSetupDisabled()) {
                Flog.i(900, "Setup is disabled putInt USER_SETUP_COMPLETE for userId " + userId);
                Global.putInt(cr, "device_provisioned", DELETE_USER_STATUS);
                Secure.putIntForUser(cr, "user_setup_complete", DELETE_USER_STATUS, userId);
            }
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    void finishRemoveUser(int userHandle) {
        super.finishRemoveUser(userHandle);
        hwRemoveUser(userHandle);
    }

    private void hwCreateUser(int userid) {
        if (userid > 0 && isSupportJni) {
            Slog.i(TAG, "native create user " + userid);
            nativeSendUserChangedNotification(CREATE_USER_STATUS, userid);
        }
    }

    private void hwRemoveUser(int userid) {
        if (userid > 0 && isSupportJni) {
            Slog.i(TAG, "native remove user " + userid);
            nativeSendUserChangedNotification(DELETE_USER_STATUS, userid);
        }
    }

    private void createUserDir(int userId) {
        File userDir = Environment.getUserSystemDirectory(userId);
        if (!userDir.exists() && !userDir.mkdir()) {
            Slog.w(TAG, "Failed to create user directory for " + userId);
        }
    }

    private boolean isStorageLow() {
        boolean isStorageLow = ((PackageManagerService) ServiceManager.getService(ControlScope.PACKAGE_ELEMENT_KEY)).isStorageLow();
        Slog.i(TAG, "PackageManagerService.isStorageLow() = " + isStorageLow);
        return isStorageLow;
    }
}
