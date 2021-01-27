package com.huawei.server.security.securitydiagnose;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hwpartsecurityservices.BuildConfig;
import com.huawei.server.security.core.IHwSecurityPlugin;
import com.huawei.server.security.securitydiagnose.RootDetectReport;
import com.huawei.util.LogEx;
import huawei.android.security.IHwSecurityDiagnoseCallback;
import huawei.android.security.IHwSecurityDiagnosePlugin;
import java.util.ArrayList;
import java.util.List;

public class HwSecurityDiagnosePlugin extends IHwSecurityDiagnosePlugin.Stub implements IHwSecurityPlugin {
    private static final boolean CHINA_RELEASE_VERSION = "CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", BuildConfig.FLAVOR));
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        /* class com.huawei.server.security.securitydiagnose.HwSecurityDiagnosePlugin.AnonymousClass1 */

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public IHwSecurityPlugin createPlugin(Context contxt) {
            HwSecurityDiagnosePlugin hwSecurityDiagnosePlugin;
            synchronized (HwSecurityDiagnosePlugin.LOCK) {
                if (HwSecurityDiagnosePlugin.sInstance == null) {
                    HwSecurityDiagnosePlugin unused = HwSecurityDiagnosePlugin.sInstance = new HwSecurityDiagnosePlugin(contxt);
                }
                hwSecurityDiagnosePlugin = HwSecurityDiagnosePlugin.sInstance;
            }
            return hwSecurityDiagnosePlugin;
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return HwSecurityDiagnosePlugin.PERMISSION;
        }
    };
    private static final long DELAY_TRIGGER_ROOT_SCAN = 43200000;
    private static final int EVT_START_ROOT_CHECK = 1003;
    private static final boolean HW_DEBUG = (SystemPropertiesEx.get("ro.secure", "1").equals("0") || LogEx.getLogHWInfo() || (LogEx.getHWModuleLog() && Log.isLoggable(TAG, 4)));
    private static final int LIST_SIZE = 10;
    private static final Object LOCK = new Object();
    private static final Object LOCK_CALL_BACK_LIST = new Object();
    private static final String PERMISSION = "com.huawei.permission.SECURITY_DIAGNOSE";
    private static final String ROOT_ALARM_ACTION = "huawei.intent.action.ROOT_DETECT_REPORTER";
    private static final String TAG = "HwSecurityDiagnosePlugin";
    private static List<IHwSecurityDiagnoseCallback> sCallBackList = new ArrayList(10);
    private static HwSecurityDiagnosePlugin sInstance;
    private static volatile boolean sIsTriggered = false;
    private Context mContext;
    private Handler mMyHandler;
    private BroadcastReceiver mReceiver;
    private BroadcastReceiver mRootAlarmReceiver;
    private RootDetectReport.Listener mRootReportListener;

    private HwSecurityDiagnosePlugin(Context contxt) {
        this.mReceiver = new BroadcastReceiver() {
            /* class com.huawei.server.security.securitydiagnose.HwSecurityDiagnosePlugin.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    Log.e(HwSecurityDiagnosePlugin.TAG, "mReceiver intent is NULL!");
                    return;
                }
                String action = intent.getAction();
                if (TextUtils.isEmpty(action)) {
                    Log.e(HwSecurityDiagnosePlugin.TAG, "onReceive action is null");
                    return;
                }
                if (HwSecurityDiagnosePlugin.HW_DEBUG) {
                    Log.d(HwSecurityDiagnosePlugin.TAG, "onReceive ACTION : " + action);
                }
                if ("android.intent.action.LOCKED_BOOT_COMPLETED".equals(action)) {
                    HwSecurityDiagnosePlugin.this.mMyHandler.sendEmptyMessage(HwSecurityDiagnosePlugin.EVT_START_ROOT_CHECK);
                } else {
                    Log.e(HwSecurityDiagnosePlugin.TAG, "received the wrong action");
                }
            }
        };
        this.mRootAlarmReceiver = new BroadcastReceiver() {
            /* class com.huawei.server.security.securitydiagnose.HwSecurityDiagnosePlugin.AnonymousClass3 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent == null || TextUtils.isEmpty(intent.getAction())) {
                    Log.e(HwSecurityDiagnosePlugin.TAG, "mRootAlarmReceiver intent or action is NULL!");
                } else if (HwSecurityDiagnosePlugin.ROOT_ALARM_ACTION.equals(intent.getAction())) {
                    if (HwSecurityDiagnosePlugin.HW_DEBUG) {
                        Log.d(HwSecurityDiagnosePlugin.TAG, "alarmManager trigger");
                    }
                    RootDetectReport.getInstance().triggerRootScan();
                }
            }
        };
        this.mMyHandler = new Handler() {
            /* class com.huawei.server.security.securitydiagnose.HwSecurityDiagnosePlugin.AnonymousClass4 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == HwSecurityDiagnosePlugin.EVT_START_ROOT_CHECK) {
                    HwSecurityDiagnosePlugin.this.triggerRootCheck();
                }
            }
        };
        this.mRootReportListener = new RootDetectReport.Listener() {
            /* class com.huawei.server.security.securitydiagnose.HwSecurityDiagnosePlugin.AnonymousClass5 */

            @Override // com.huawei.server.security.securitydiagnose.RootDetectReport.Listener
            public void onRootReport() {
                HwSecurityDiagnosePlugin.this.onRootStatus();
            }
        };
        this.mContext = contxt;
    }

    private void setTriggerTime() {
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "Time trigger error");
        } else if (context.getSystemService("alarm") instanceof AlarmManager) {
            Intent intent = new Intent(ROOT_ALARM_ACTION);
            intent.setPackage(this.mContext.getPackageName());
            ((AlarmManager) this.mContext.getSystemService("alarm")).setRepeating(3, DELAY_TRIGGER_ROOT_SCAN + SystemClock.elapsedRealtime(), DELAY_TRIGGER_ROOT_SCAN, PendingIntent.getBroadcast(this.mContext, 0, intent, 268435456));
        } else {
            Log.e(TAG, "set trigger Time failed");
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: com.huawei.server.security.securitydiagnose.HwSecurityDiagnosePlugin */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public IBinder asBinder() {
        return this;
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStart() {
        if (HW_DEBUG) {
            Log.d(TAG, "onStart()");
        }
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.LOCKED_BOOT_COMPLETED"));
        this.mContext.registerReceiver(this.mRootAlarmReceiver, new IntentFilter(ROOT_ALARM_ACTION), PERMISSION, null);
        RootDetectReport.init(this.mContext);
        RootDetectReport.getInstance().setListener(this.mRootReportListener);
        AppLayerStpProxy.init(this.mContext);
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStop() {
        this.mContext.unregisterReceiver(this.mReceiver);
        this.mContext.unregisterReceiver(this.mRootAlarmReceiver);
    }

    public int report(int reporterID, Bundle data) {
        return 0;
    }

    public int getSystemStatus() {
        if (HW_DEBUG) {
            Log.d(TAG, "getSystemStatus");
        }
        checkPermission(PERMISSION);
        return 0;
    }

    private void checkPermission(String permission) {
        Context context = this.mContext;
        context.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }

    /* access modifiers changed from: private */
    public static class RootCheckTrigger extends Thread {
        private RootCheckTrigger() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            if (HwSecurityDiagnosePlugin.HW_DEBUG) {
                Log.d(HwSecurityDiagnosePlugin.TAG, "rootCheckTask START!");
            }
            RootDetectReport.getInstance().triggerRootScan();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void triggerRootCheck() {
        new RootCheckTrigger().start();
        setTriggerTime();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onRootStatus() {
        List<IHwSecurityDiagnoseCallback> copyList = new ArrayList<>(10);
        synchronized (LOCK_CALL_BACK_LIST) {
            copyList.addAll(sCallBackList);
            sCallBackList.clear();
            sIsTriggered = false;
            if (HW_DEBUG) {
                Log.d(TAG, "lock in onRootStatus");
            }
        }
        int rootResult = AppLayerStpProxy.getInstance().getEachItemRootStatus();
        if (rootResult < 0) {
            Log.e(TAG, "get each item root status failed. rootResult = " + rootResult);
            rootResult = -1;
        }
        int copyListSize = copyList.size();
        for (int i = 0; i < copyListSize; i++) {
            try {
                if (HW_DEBUG) {
                    Log.d(TAG, "onRootStatus callbackID:" + i);
                }
                copyList.get(i).onRootStatus(rootResult);
            } catch (RemoteException e) {
                Log.e(TAG, "callback error");
            }
        }
        copyList.clear();
        if (HW_DEBUG) {
            Log.d(TAG, "root result is : " + rootResult);
        }
    }

    public void getRootStatus(IHwSecurityDiagnoseCallback callback) {
        checkPermission(PERMISSION);
        boolean isNeedTrigger = false;
        synchronized (LOCK_CALL_BACK_LIST) {
            if (!sIsTriggered) {
                isNeedTrigger = true;
                sIsTriggered = true;
                if (HW_DEBUG) {
                    Log.d(TAG, "need to trigger root status");
                }
            }
            if (!sCallBackList.contains(callback)) {
                sCallBackList.add(callback);
                if (HW_DEBUG) {
                    Log.d(TAG, "getRootStatus list size:" + sCallBackList.size());
                }
            } else if (HW_DEBUG) {
                Log.d(TAG, "callback has in list,do not trigger again");
            }
        }
        if (isNeedTrigger) {
            if (HW_DEBUG) {
                Log.d(TAG, "trigger root status");
            }
            triggerRootCheck();
        }
    }

    public int getRootStatusSync() {
        checkPermission(PERMISSION);
        return AppLayerStpProxy.getInstance().getRootStatusSync();
    }

    public int getSystemStatusSync() {
        checkPermission(PERMISSION);
        return AppLayerStpProxy.getInstance().getSystemStatusSync();
    }

    public int sendThreatenInfo(int id, byte status, byte credible, byte version, String name, String additionInfo) {
        checkPermission(PERMISSION);
        return AppLayerStpProxy.getInstance().sendThreatenInfo(id, status, credible, version, name, additionInfo);
    }

    public int getStpStatusByCategory(int category, boolean inDetail, boolean withHistory, char[] outBuff, int[] outBuffLen) {
        checkPermission(PERMISSION);
        return AppLayerStpProxy.getInstance().getStpStatusByCategory(category, inDetail, withHistory, outBuff, outBuffLen);
    }

    public int startKernelDetection(int uid) {
        checkPermission(PERMISSION);
        return KernelDetection.getInstance().startInspection(uid);
    }

    public int updateKernelDetectionConfig(int[] conf) {
        checkPermission(PERMISSION);
        return KernelDetection.getInstance().updateKernelDetectionConfig(conf);
    }

    public int stopKernelDetection(int uid) {
        checkPermission(PERMISSION);
        return KernelDetection.getInstance().stopInspection(uid);
    }
}
