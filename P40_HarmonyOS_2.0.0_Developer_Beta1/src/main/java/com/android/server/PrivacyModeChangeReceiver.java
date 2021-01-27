package com.android.server;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Slog;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.util.List;

public class PrivacyModeChangeReceiver extends BroadcastReceiver {
    private static final boolean DEBUG = false;
    private static final String DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final int MAX_NUM = 20;
    private static final int MAX_PKG = 100;
    private static final String TAG = "PrivacyModeChangeReceiver";
    public static final int TRANSACTION_SET_ENABLED_VISITOR_SETTING = 1001;
    private ActivityManager mActivityManager;
    private Context mContext;
    private PackageManager mPackageManager;
    private IPackageManager mPackageManagerService;

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        this.mPackageManager = this.mContext.getPackageManager();
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (intent.getIntExtra("privacy_mode_value", 1) == 1) {
            new Thread("privacymodechange") {
                /* class com.android.server.PrivacyModeChangeReceiver.AnonymousClass1 */

                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    try {
                        PrivacyModeChangeReceiver.this.removeAllRecentTask();
                        PrivacyModeChangeReceiver.this.transactToPackageManagerService(1001, "setEnabledVisitorSetting", 2, 0);
                    } catch (Exception e) {
                        Slog.e(PrivacyModeChangeReceiver.TAG, "change to visitor mode failure.");
                    }
                }
            }.start();
        } else if (intent.getIntExtra("privacy_mode_value", 0) == 0) {
            new Thread("privacymodechange") {
                /* class com.android.server.PrivacyModeChangeReceiver.AnonymousClass2 */

                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    try {
                        PrivacyModeChangeReceiver.this.transactToPackageManagerService(1001, "setEnabledVisitorSetting", 1, 0);
                    } catch (Exception e) {
                        Slog.e(PrivacyModeChangeReceiver.TAG, "change to host mode failure.");
                    }
                }
            }.start();
        }
    }

    private void updateAppfromSetting(int state) {
        String pkgNameList = Settings.Secure.getString(this.mContext.getContentResolver(), "privacy_app_list");
        if (pkgNameList == null) {
            Slog.e(TAG, " pkgNameList = null ");
        } else if (pkgNameList.contains(AwarenessInnerConstants.SEMI_COLON_KEY)) {
            String[] pkgNameArray = pkgNameList.split(AwarenessInnerConstants.SEMI_COLON_KEY);
            int i = 0;
            while (i < 100 && i < pkgNameArray.length) {
                setApplicationEnabledSetting(pkgNameArray[i], state);
                i++;
            }
        } else {
            setApplicationEnabledSetting(pkgNameList, state);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeAllRecentTask() {
        List<ActivityManager.RecentTaskInfo> recentTasks = this.mActivityManager.getRecentTasks(20, 2);
        if (recentTasks != null) {
            int size = recentTasks.size();
            int i = 0;
            while (i < size && i < 20) {
                ActivityManager.RecentTaskInfo recentInfo = recentTasks.get(i);
                Intent intent = new Intent(recentInfo.baseIntent);
                if (recentInfo.origActivity != null) {
                    intent.setComponent(recentInfo.origActivity);
                }
                if (isCurrentHomeActivity(intent.getComponent(), null)) {
                    Slog.e(TAG, " isCurrentHomeActivity");
                } else if (!intent.getComponent().getPackageName().equals(this.mContext.getPackageName())) {
                    try {
                        ActivityManager.getService().removeTask(recentInfo.persistentId);
                    } catch (RemoteException e) {
                        Slog.e(TAG, "removeTask has error.");
                    }
                    recentTasks.clear();
                }
                i++;
            }
        }
    }

    private boolean isCurrentHomeActivity(ComponentName component, ActivityInfo homeInfo) {
        if (homeInfo == null) {
            homeInfo = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").resolveActivityInfo(this.mContext.getPackageManager(), 0);
        }
        if (homeInfo == null || !homeInfo.packageName.equals(component.getPackageName()) || !homeInfo.name.equals(component.getClassName())) {
            return false;
        }
        return true;
    }

    private void setApplicationEnabledSetting(String pkgName, int enabledStatus) {
        try {
            if (isAppExits(pkgName)) {
                this.mPackageManager.setApplicationEnabledSetting(pkgName, enabledStatus, 0);
                Slog.e(TAG, "the pkg " + pkgName + " enablestatus: " + enabledStatus);
            }
        } catch (SecurityException e) {
            Slog.e(TAG, "Unable to change enabled state of package xxx SecurityException: " + pkgName);
        } catch (Exception e2) {
            Slog.e(TAG, "Unable to change enabled state of package 1: " + pkgName);
        }
    }

    private boolean isAppExits(String pkgName) {
        if (this.mPackageManager == null || pkgName == null || "".equals(pkgName)) {
            return false;
        }
        try {
            if (this.mPackageManager.getPackageInfo(pkgName, 0) != null) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "The packageName " + pkgName + " is not exit");
            return false;
        }
    }

    private IPackageManager getPackageManager() {
        if (this.mPackageManagerService == null) {
            this.mPackageManagerService = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        }
        return this.mPackageManagerService;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized boolean transactToPackageManagerService(int code, String transactName, int enabledStatus, int flag) {
        boolean isSuccess;
        Throwable th;
        RemoteException localRemoteException;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        isSuccess = false;
        try {
            IBinder packageManagerServiceBinder = getPackageManager().asBinder();
            if (packageManagerServiceBinder != null) {
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    data.writeInt(enabledStatus);
                    data.writeInt(flag);
                    data.writeInt(this.mContext.getUserId());
                    boolean z = false;
                    packageManagerServiceBinder.transact(code, data, reply, 0);
                    reply.readException();
                    if (reply.readInt() == 0) {
                        z = true;
                    }
                    isSuccess = z;
                } catch (RemoteException e) {
                    localRemoteException = e;
                    try {
                        Slog.e(TAG, "transactToPackageManagerService RemoteException : " + localRemoteException.getMessage());
                        reply.recycle();
                        data.recycle();
                        return isSuccess;
                    } catch (Throwable th2) {
                        th = th2;
                        reply.recycle();
                        data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reply.recycle();
                    data.recycle();
                    throw th;
                }
            }
            reply.recycle();
            data.recycle();
        } catch (RemoteException e2) {
            localRemoteException = e2;
            Slog.e(TAG, "transactToPackageManagerService RemoteException : " + localRemoteException.getMessage());
            reply.recycle();
            data.recycle();
            return isSuccess;
        }
        return isSuccess;
    }
}
