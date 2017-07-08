package com.android.server;

import android.app.AppGlobals;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.security.trustcircle.IOTController;
import com.huawei.hsm.HsmCoreServiceImpl;

public class HwCoreAppHelperService extends SystemService {
    private static final String HPC_MAIN_SERVICE = "com.huawei.parentcontrol.service.ControlService";
    private static final String HPC_PACKAGE_NAME = "com.huawei.parentcontrol";
    private static final String HSM_MAIN_SERVICE = "com.huawei.systemmanager.service.MainService";
    private static final String HSM_PACKAGE_NAME = "com.huawei.systemmanager";
    private static final String TAG = "HwCoreAppHelperService";
    private Context mContext;
    private IBinder mHsmCoreService;

    private class LocalReceiver extends BroadcastReceiver {
        private LocalReceiver() {
        }

        private String getPackageName(Intent intent) {
            Uri uri = intent.getData();
            return uri != null ? uri.getSchemeSpecificPart() : AppHibernateCst.INVALID_PKG;
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                String pkg = getPackageName(intent);
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                Slog.i(HwCoreAppHelperService.TAG, "LocalReceiver receives:" + action + ",pkg:" + pkg + ",uid:" + uid);
                if (TextUtils.isEmpty(pkg) || uid < 0) {
                    Slog.e(HwCoreAppHelperService.TAG, "invalidate pkg name or uid");
                    return;
                }
                int userId = UserHandle.getUserId(uid);
                if ("android.intent.action.PACKAGE_DATA_CLEARED".equals(action)) {
                    if (HwCoreAppHelperService.this.shoudNotStop(pkg, userId)) {
                        HwCoreAppHelperService.this.restartPackageAfterStopped(pkg, userId);
                    }
                } else if ("android.intent.action.PACKAGE_RESTARTED".equals(action) && HwCoreAppHelperService.this.shoudNotStop(pkg, userId) && explicitlyStopped(pkg, userId)) {
                    HwCoreAppHelperService.this.restartPackageAfterStopped(pkg, userId);
                }
            }
        }

        private boolean explicitlyStopped(String pkg, int userId) {
            try {
                ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(pkg, 0, userId);
                if (ai == null || (ai.flags & 2097152) == 0) {
                    return false;
                }
                Slog.i(HwCoreAppHelperService.TAG, "explicitlyStopped not stopped, pkg:" + pkg + ", uerId:" + userId);
                return true;
            } catch (IllegalArgumentException e) {
                Slog.w(HwCoreAppHelperService.TAG, "explicitlyStopped IllegalArgumentException,pkg:" + pkg);
                return false;
            } catch (Exception e2) {
                Slog.w(HwCoreAppHelperService.TAG, "explicitlyStopped Exception,pkg:" + pkg);
                return false;
            }
        }
    }

    public HwCoreAppHelperService(Context context) {
        super(context);
        this.mContext = null;
        this.mHsmCoreService = null;
        this.mContext = context;
    }

    public void onStart() {
        Slog.i(TAG, "start HwCoreAppHelperService");
        publishHsmCoreService();
    }

    private void registerStoppedAppReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_RESTARTED");
        filter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
        filter.addDataScheme(ControlScope.PACKAGE_ELEMENT_KEY);
        this.mContext.registerReceiverAsUser(new LocalReceiver(), UserHandle.ALL, filter, null, null);
    }

    public void onBootPhase(int phase) {
        if (phase == IOTController.TYPE_MASTER) {
            registerStoppedAppReceiver();
        }
    }

    private boolean shoudNotStop(String pkg, int userId) {
        if (HSM_PACKAGE_NAME.equals(pkg)) {
            return true;
        }
        return HPC_PACKAGE_NAME.equals(pkg) && userId == 0;
    }

    private void restartPackageAfterStopped(String pkg, int userId) {
        Slog.i(TAG, "oops, force stopped " + pkg + ", let's do something." + ", userId:" + userId);
        if (HSM_PACKAGE_NAME.equals(pkg)) {
            restartService(HSM_PACKAGE_NAME, HSM_MAIN_SERVICE, userId);
        } else if (HPC_PACKAGE_NAME.equals(pkg)) {
            restartService(HPC_PACKAGE_NAME, HPC_MAIN_SERVICE, userId);
        }
    }

    private void restartService(String apkName, String serviceName, int userId) {
        Intent target = new Intent();
        ComponentName cn = new ComponentName(apkName, serviceName);
        target.setComponent(cn);
        try {
            this.mContext.startServiceAsUser(target, new UserHandle(userId));
        } catch (Exception e) {
            Slog.e(TAG, "component not found:" + cn);
        }
    }

    private void publishHsmCoreService() {
        if (this.mHsmCoreService == null && this.mContext != null) {
            this.mHsmCoreService = new HsmCoreServiceImpl(this.mContext);
        }
        if (this.mHsmCoreService != null) {
            publishBinderService("system.hsmcore", this.mHsmCoreService);
        } else {
            Slog.e(TAG, "publishHsmCoreService failed!");
        }
    }
}
