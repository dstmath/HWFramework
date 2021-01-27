package com.huawei.security.dpermission.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.huawei.android.content.ContextEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.security.dpermission.DPermissionInitializer;
import com.huawei.security.dpermission.utils.DangerousPermissionDataHelper;
import ohos.event.commonevent.ActionMapper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.security.dpermissionkit.DPermissionKit;

public class PackageChangeReceiver extends BroadcastReceiver {
    private static final int APP_INSTALL_STATUS = 0;
    private static final int APP_REPLACE_STATUS = 2;
    private static final int APP_UNINSTALL_STATUS = 1;
    private static final String DATA_SCHEME = "package";
    private static final HiLogLabel DPERMISSION_LABEL = new HiLogLabel(3, (int) DPermissionInitializer.DPERMISSION_LOG_ID, "PackageChangeReceiver");
    private static final int EVT_PACKAGE_ADD = 1001;
    private static final int EVT_PACKAGE_REMOVE = 1002;
    private static final int EVT_PACKAGE_REPLACE = 1003;
    private static final int EVT_USER_REMOVED = 1004;
    private static final Object INSTANCE_LOCK = new Object();
    private static final int USER_HANDLER_INVALIDATE = -1000;
    private static volatile PackageChangeReceiver sInstance;
    private Handler handler;
    private Context mContext;

    private PackageChangeReceiver(Context context) {
        this.mContext = context;
        HandlerThread handlerThread = new HandlerThread("ZPackageChangeReceiver");
        handlerThread.start();
        if (handlerThread.getLooper() != null) {
            this.handler = new PermissionChangeHandler(handlerThread.getLooper());
        }
    }

    public static PackageChangeReceiver getInstance(Context context) {
        if (sInstance == null) {
            synchronized (INSTANCE_LOCK) {
                if (sInstance == null) {
                    sInstance = new PackageChangeReceiver(context);
                }
            }
        }
        return sInstance;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            char c = 65535;
            switch (action.hashCode()) {
                case -2061058799:
                    if (action.equals(ActionMapper.ACTION_USER_REMOVED)) {
                        c = 3;
                        break;
                    }
                    break;
                case -810471698:
                    if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
                        c = 2;
                        break;
                    }
                    break;
                case 525384130:
                    if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                        c = 1;
                        break;
                    }
                    break;
                case 1544582882:
                    if (action.equals("android.intent.action.PACKAGE_ADDED")) {
                        c = 0;
                        break;
                    }
                    break;
            }
            if (c != 0) {
                if (c != 1) {
                    if (c == 2) {
                        Message.obtain(this.handler, 1003, intent).sendToTarget();
                    } else if (c == 3) {
                        Message.obtain(this.handler, 1004, intent).sendToTarget();
                    }
                } else if (!isPackageReplacing(intent)) {
                    Message.obtain(this.handler, 1002, intent).sendToTarget();
                }
            } else if (!isPackageReplacing(intent)) {
                Message.obtain(this.handler, 1001, intent).sendToTarget();
            }
        }
    }

    public void register() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActionMapper.ACTION_USER_REMOVED);
        ContextEx.registerReceiverAsUser(this.mContext, this, UserHandleEx.ALL, intentFilter, (String) null, (Handler) null);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter2.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter2.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter2.addDataScheme(DATA_SCHEME);
        ContextEx.registerReceiverAsUser(this.mContext, this, UserHandleEx.ALL, intentFilter2, (String) null, (Handler) null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePackageAdded(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            int i = extras.getInt("android.intent.extra.UID");
            HiLog.debug(DPERMISSION_LABEL, "A app has been installed, uid -> %{public}d", new Object[]{Integer.valueOf(i)});
            DPermissionKit.getInstance().notifyUidPermissionChanged(i);
            DPermissionKit.getInstance().notifyAppStatusChanged(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePackageRemoved(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            HiLog.error(DPERMISSION_LABEL, "handlePackageRemoved bundle is null", new Object[0]);
            return;
        }
        int i = extras.getInt("android.intent.extra.UID");
        HiLog.debug(DPERMISSION_LABEL, "A app has been uninstalled, uid -> %{public}d", new Object[]{Integer.valueOf(i)});
        Context context = this.mContext;
        if (context == null) {
            HiLog.error(DPERMISSION_LABEL, "handlePackageRemoved mContext is null", new Object[0]);
            return;
        }
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            HiLog.error(DPERMISSION_LABEL, "handlePackageRemoved get packageManager failed", new Object[0]);
            return;
        }
        String[] packagesForUid = packageManager.getPackagesForUid(i);
        if (packagesForUid == null || packagesForUid.length == 0) {
            DPermissionKit.getInstance().notifyUidPermissionChanged(-i);
        } else {
            DPermissionKit.getInstance().notifyUidPermissionChanged(i);
        }
        DPermissionKit.getInstance().notifyAppStatusChanged(1);
        DangerousPermissionDataHelper.removePackage(this.mContext, getPkgNameFromIntent(intent));
    }

    private String getPkgNameFromIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            return data.getSchemeSpecificPart();
        }
        HiLog.warn(DPERMISSION_LABEL, "getPkgNameFromIntent: get null uri.", new Object[0]);
        return "";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePackageReplaced(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            int i = extras.getInt("android.intent.extra.UID");
            HiLog.debug(DPERMISSION_LABEL, "A app has been replaced, uid -> %{public}d", new Object[]{Integer.valueOf(i)});
            DPermissionKit.getInstance().notifyUidPermissionChanged(i);
            DPermissionKit.getInstance().notifyAppStatusChanged(2);
        }
        DangerousPermissionDataHelper.removePackage(this.mContext, getPkgNameFromIntent(intent));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUserRemoved(Intent intent) {
        int intExtra = intent.getIntExtra("android.intent.extra.user_handle", -1000);
        HiLog.debug(DPERMISSION_LABEL, "User removed, userId: %{public}d", new Object[]{Integer.valueOf(intExtra)});
        DPermissionKit.getInstance().notifyUidPermissionChanged((-intExtra) * 100000);
    }

    private boolean isPackageReplacing(Intent intent) {
        boolean booleanExtra = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
        if (booleanExtra) {
            HiLog.debug(DPERMISSION_LABEL, "Package is replacing, ignore this event.", new Object[0]);
        }
        return booleanExtra;
    }

    private class PermissionChangeHandler extends Handler {
        PermissionChangeHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1001:
                    if (message.obj instanceof Intent) {
                        PackageChangeReceiver.this.handlePackageAdded((Intent) message.obj);
                        return;
                    }
                    return;
                case 1002:
                    if (message.obj instanceof Intent) {
                        PackageChangeReceiver.this.handlePackageRemoved((Intent) message.obj);
                        return;
                    }
                    return;
                case 1003:
                    if (message.obj instanceof Intent) {
                        PackageChangeReceiver.this.handlePackageReplaced((Intent) message.obj);
                        return;
                    }
                    return;
                case 1004:
                    if (message.obj instanceof Intent) {
                        PackageChangeReceiver.this.handleUserRemoved((Intent) message.obj);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }
}
