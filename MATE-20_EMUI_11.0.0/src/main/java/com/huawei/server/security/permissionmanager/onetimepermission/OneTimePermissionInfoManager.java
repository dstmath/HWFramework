package com.huawei.server.security.permissionmanager.onetimepermission;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import com.huawei.android.content.ContextEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.SlogEx;
import com.huawei.server.security.permissionmanager.OneTimePermAdapter;
import com.huawei.server.security.permissionmanager.util.HwPermUtils;
import java.lang.Thread;

public class OneTimePermissionInfoManager {
    private static final int EVT_ON_INFO_INIT = 1000;
    private static final int EVT_ON_INFO_UPDATE_ALL = 1002;
    private static final int EVT_ON_PACKAGE_ADD = 1001;
    private static final boolean IS_CHINA_AREA = HwPermUtils.IS_CHINA_AREA;
    private static final Object LOCK = new Object();
    private static final String TAG = "OneTimePermissionInfoManager";
    private static volatile OneTimePermissionInfoManager sInstance;
    private BroadcastReceiver mBootCompletedReceiver = new BootCompletedReceiver();
    private Context mContext;
    private Handler mHandler;
    private OneTimePermAdapter mOneTimePermAdapter;
    private BroadcastReceiver mPackageChangeReceiver = new PackageChangedReceiver();

    private OneTimePermissionInfoManager(Context context) {
        SlogEx.i(TAG, "one time manager init");
        this.mContext = context.getApplicationContext();
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            /* class com.huawei.server.security.permissionmanager.onetimepermission.OneTimePermissionInfoManager.AnonymousClass1 */

            @Override // java.lang.Thread.UncaughtExceptionHandler
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
                SlogEx.e(OneTimePermissionInfoManager.TAG, "Uncaught exception found");
            }
        });
        handlerThread.start();
        if (handlerThread.getLooper() != null) {
            this.mHandler = new OneTimePermissionHandler(handlerThread.getLooper());
        } else {
            SlogEx.w(TAG, "get null looper.");
        }
        this.mOneTimePermAdapter = OneTimePermAdapter.getInstance(context);
    }

    public static OneTimePermissionInfoManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (context != null) {
                    try {
                        if (sInstance == null) {
                            sInstance = new OneTimePermissionInfoManager(context);
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    throw new IllegalArgumentException("Input value is null!");
                }
            }
        }
        return sInstance;
    }

    public void setOneTimePermissionInfo(Bundle params) {
        Message msg = this.mHandler.obtainMessage(EVT_ON_INFO_UPDATE_ALL);
        msg.setData(params);
        this.mHandler.sendMessage(msg);
    }

    public Bundle getOneTimePermissionInfo() {
        return this.mOneTimePermAdapter.getOneTimePermissionInfo();
    }

    public void registerReceiver() {
        if (IS_CHINA_AREA) {
            IntentFilter packageChangedFilter = new IntentFilter();
            packageChangedFilter.addAction("android.intent.action.PACKAGE_ADDED");
            packageChangedFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            packageChangedFilter.addDataScheme("package");
            ContextEx.registerReceiverAsUser(this.mContext, this.mPackageChangeReceiver, UserHandleEx.ALL, packageChangedFilter, (String) null, (Handler) null);
            IntentFilter bootCompletedFilter = new IntentFilter();
            bootCompletedFilter.addAction("android.intent.action.BOOT_COMPLETED");
            bootCompletedFilter.addAction("android.intent.action.LOCKED_BOOT_COMPLETED");
            this.mContext.registerReceiver(this.mBootCompletedReceiver, bootCompletedFilter);
        }
    }

    private class PackageChangedReceiver extends BroadcastReceiver {
        private PackageChangedReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Uri uri;
            if (context == null || intent == null) {
                SlogEx.e(OneTimePermissionInfoManager.TAG, "receive invalid broadcase");
            } else if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false) && (uri = intent.getData()) != null) {
                String packageName = uri.getSchemeSpecificPart();
                if (packageName == null) {
                    SlogEx.w(OneTimePermissionInfoManager.TAG, "onReceive packageName null");
                } else if ("android.intent.action.PACKAGE_ADDED".equals(intent.getAction())) {
                    Message.obtain(OneTimePermissionInfoManager.this.mHandler, OneTimePermissionInfoManager.EVT_ON_PACKAGE_ADD, packageName).sendToTarget();
                }
            }
        }
    }

    private class BootCompletedReceiver extends BroadcastReceiver {
        private BootCompletedReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                SlogEx.e(OneTimePermissionInfoManager.TAG, "receive invalid broadcase");
                return;
            }
            String action = intent.getAction();
            if ("android.intent.action.BOOT_COMPLETED".equals(action) || "android.intent.action.LOCKED_BOOT_COMPLETED".equals(action)) {
                Message.obtain(OneTimePermissionInfoManager.this.mHandler, (int) OneTimePermissionInfoManager.EVT_ON_INFO_INIT).sendToTarget();
            }
        }
    }

    private class OneTimePermissionHandler extends Handler {
        OneTimePermissionHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OneTimePermissionInfoManager.EVT_ON_INFO_INIT /* 1000 */:
                    OneTimePermissionInfoManager.this.mOneTimePermAdapter.initInfoForInstalledPkg();
                    return;
                case OneTimePermissionInfoManager.EVT_ON_PACKAGE_ADD /* 1001 */:
                    if (msg.obj instanceof String) {
                        OneTimePermissionInfoManager.this.mOneTimePermAdapter.updateInfoForPkgAdd((String) msg.obj);
                        return;
                    }
                    return;
                case OneTimePermissionInfoManager.EVT_ON_INFO_UPDATE_ALL /* 1002 */:
                    Bundle params = msg.peekData();
                    if (params == null) {
                        SlogEx.w(OneTimePermissionInfoManager.TAG, "get invalid info to update.");
                        return;
                    } else {
                        OneTimePermissionInfoManager.this.mOneTimePermAdapter.updateInfoAll(params);
                        return;
                    }
                default:
                    return;
            }
        }
    }
}
