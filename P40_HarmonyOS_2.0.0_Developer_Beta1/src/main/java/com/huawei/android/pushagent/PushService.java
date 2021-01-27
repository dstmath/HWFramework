package com.huawei.android.pushagent;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.FeatureInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import com.huawei.android.content.ContextEx;
import com.huawei.android.feature.BuildConfig;
import com.huawei.android.feature.compat.InstallCompat;
import com.huawei.android.feature.install.InstallStorageManager;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallManager;
import com.huawei.android.feature.model.InstallSessionStatus;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.pushagent.dynamicload.IPushManager;
import java.io.File;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

public class PushService extends Service {
    private static final byte HMS_VERSION = 1;
    private static final byte[] INSTALL_MANAGER_LOCK = new byte[0];
    private static final byte LOCAL_VERSION = 2;
    private static final byte NC_VERSION = 0;
    private static final String TAG = "PushLogSys";
    private static PushService gPushService;
    private Context context;
    private FeatureLocalInstallManager mInstallManager;
    IPushManager pushCoreManager;
    private CountDownLatch pushCoreManagerLatch;
    private HandlerThread pushHandlerThread = new HandlerThread("sys push");
    PushInnerReceiver pushInnerReceiver;
    private boolean pushServiceRunning = false;
    PushSysReceiver pushSysReceiver;

    /* access modifiers changed from: package-private */
    public class PushInnerReceiver extends BroadcastReceiver {
        private PushInnerReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            long versionCode;
            byte electPush;
            if (PushService.this.checkParameter(context, intent)) {
                String action = intent.getAction();
                Log.i(PushService.TAG, "sys push inner receiver get action is ".concat(String.valueOf(action)));
                if ("com.huawei.android.push.intent.CHECK_HWPUSH_VERSION".equals(action) && context.getPackageName().equals(intent.getStringExtra("Remote_Package_Name"))) {
                    Object b = new an(context).b("latestVersion", -1L);
                    long intValue = b instanceof Integer ? (long) ((Integer) b).intValue() : b instanceof Long ? ((Long) b).longValue() : -1;
                    synchronized (PushService.INSTALL_MANAGER_LOCK) {
                        versionCode = ah.b(context, PushService.this.mInstallManager).getVersionCode();
                    }
                    Log.i(PushService.TAG, "check pushcore version trs version is " + intValue + ". localVersion is " + versionCode);
                    if (versionCode <= intValue && 2 != (electPush = PushService.this.electPush())) {
                        Log.i(PushService.TAG, "TRS update pushcore version , need install push again");
                        PushService.this.loadPush(electPush);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class PushSysReceiver extends BroadcastReceiver {
        private PushSysReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            byte electPush;
            if (PushService.this.checkParameter(context, intent)) {
                String action = intent.getAction();
                Uri data = intent.getData();
                String str = BuildConfig.FLAVOR;
                if (data != null) {
                    str = data.getSchemeSpecificPart();
                }
                Log.i(PushService.TAG, "sys push system receiver get action is " + action + ". pkgName is " + str);
                am amVar = new am(context);
                if (!"android.intent.action.PACKAGE_ADDED".equals(action)) {
                    return;
                }
                if ((amVar.g().equals(str) || "com.huawei.android.pushagent".equals(str)) && 2 != (electPush = PushService.this.electPush())) {
                    Log.i(PushService.TAG, "HMS or NC update pushcore version, need install push again");
                    PushService.this.loadPush(electPush);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkParameter(Context context2, Intent intent) {
        if (context2 == null || intent == null) {
            Log.e(TAG, "PushInnerReceiver context is null or intent is null");
            return false;
        }
        try {
            intent.getStringExtra("TestIntent");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "intent has some error");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private byte electPush() {
        long versionCode;
        long versionCode2;
        long versionCode3;
        synchronized (INSTALL_MANAGER_LOCK) {
            versionCode = ah.b(this.context, this.mInstallManager).getVersionCode();
            versionCode2 = ai.c(this.context, this.mInstallManager).getVersionCode();
        }
        synchronized (INSTALL_MANAGER_LOCK) {
            versionCode3 = ad.a(this.context, this.mInstallManager).getVersionCode();
        }
        byte b = versionCode >= ((versionCode2 > versionCode3 ? 1 : (versionCode2 == versionCode3 ? 0 : -1)) > 0 ? versionCode2 : versionCode3) ? LOCAL_VERSION : versionCode2 >= versionCode3 ? NC_VERSION : HMS_VERSION;
        Log.i(TAG, "local pushcore version is " + versionCode + ". NC pushcore version is " + versionCode2 + ". HMS pushcore version is " + versionCode3 + ". selected version is " + ((int) b) + ", [0:NC, 1:HMS, 2:LOCAL]");
        return b;
    }

    public static synchronized PushService getInstance() {
        PushService pushService;
        synchronized (PushService.class) {
            pushService = gPushService;
        }
        return pushService;
    }

    private void installLoadPushCore() {
        InstallStorageManager.initBaseDir(new File("/data/misc/hwpush"));
        synchronized (INSTALL_MANAGER_LOCK) {
            this.mInstallManager = new FeatureLocalInstallManager(this.context);
        }
        final int i = -100;
        try {
            i = InstallCompat.install(this.context);
        } catch (Exception e) {
            Log.e(TAG, "pre install pushcore from local error: " + ao.a(e));
        }
        Log.i(TAG, "pre install pushcore result is ".concat(String.valueOf(i)));
        this.pushCoreManagerLatch = new CountDownLatch(1);
        this.pushHandlerThread.start();
        Looper looper = this.pushHandlerThread.getLooper();
        if (looper != null) {
            new Handler(looper).post(new Runnable() {
                /* class com.huawei.android.pushagent.PushService.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    if (i != 0) {
                        Log.e(PushService.TAG, "handle local pushcore install error");
                        synchronized (PushService.INSTALL_MANAGER_LOCK) {
                            ah.b(PushService.this.context, PushService.this.mInstallManager);
                            ah.a(i);
                        }
                    }
                    PushService.this.loadPush(PushService.this.electPush());
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadPush(byte b) {
        synchronized (INSTALL_MANAGER_LOCK) {
            if (2 == b) {
                ah.b(this.context, this.mInstallManager).d();
            } else if (1 == b) {
                ad a = ad.a(this.context, this.mInstallManager);
                Log.i(TAG, "begin install HMS push core");
                if (ao.i() == 0 || !ao.h()) {
                    a.a("package://com.huawei.hwid/feature/pushcore_v2.fpk", "HMS");
                } else {
                    a.a("package://com.huawei.hwid/feature/pushcore.fpk", "HMS");
                }
            } else {
                ai c = ai.c(this.context, this.mInstallManager);
                Log.i(TAG, "begin install NC push core");
                if (ao.i() == 0 || !ao.h()) {
                    c.a("package://com.huawei.android.pushagent/feature/pushcore_v2.fpk", "NC");
                } else {
                    c.a("package://com.huawei.android.pushagent/feature/pushcore.fpk", "NC");
                }
            }
        }
    }

    private void registerInnerReceiver() {
        this.pushInnerReceiver = new PushInnerReceiver();
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.huawei.android.push.intent.CHECK_HWPUSH_VERSION");
            ContextEx.registerReceiverAsUser(this.context, this.pushInnerReceiver, UserHandleEx.ALL, intentFilter, "com.huawei.pushagent.permission.INNER_RECEIVER", (Handler) null);
        } catch (Exception e) {
            Log.e(TAG, "register sys push inner receiver error");
        }
    }

    private void registerSysReceiver() {
        this.pushSysReceiver = new PushSysReceiver();
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
            intentFilter.addDataScheme("package");
            ContextEx.registerReceiverAsUser(this.context, this.pushSysReceiver, UserHandleEx.ALL, intentFilter, (String) null, (Handler) null);
        } catch (Exception e) {
            Log.e(TAG, "register sys push inner receiver error");
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void setHmsPkg() {
        String str;
        char c;
        ac acVar = new ac(this.context);
        if (acVar.J == null) {
            FeatureInfo[] systemAvailableFeatures = acVar.context.getPackageManager().getSystemAvailableFeatures();
            HashSet hashSet = new HashSet();
            for (FeatureInfo featureInfo : systemAvailableFeatures) {
                if (featureInfo.name != null) {
                    hashSet.add(featureInfo.name);
                }
            }
            String str2 = hashSet.contains("com.huawei.software.features.handset") ? "0" : hashSet.contains("com.huawei.software.features.pad") ? "1" : hashSet.contains("com.huawei.software.features.mobiletv") ? "5" : hashSet.contains("com.huawei.software.features.tv") ? "4" : hashSet.contains("com.huawei.software.features.kidwatch") ? "3" : hashSet.contains("com.huawei.software.features.watch") ? "2" : "-1";
            switch (str2.hashCode()) {
                case 48:
                    if (str2.equals("0")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 49:
                    if (str2.equals("1")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 50:
                    if (str2.equals("2")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 51:
                default:
                    c = 65535;
                    break;
                case 52:
                    if (str2.equals("4")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 53:
                    if (str2.equals("5")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                case 1:
                    str = "com.huawei.hwid";
                    break;
                case 2:
                    str = "com.huawei.hwid.tv";
                    break;
                case 3:
                case InstallSessionStatus.INSTALLING /* 4 */:
                    str = "com.huawei.hms";
                    break;
                default:
                    str = "com.huawei.hwid";
                    break;
            }
        } else {
            str = acVar.J;
        }
        new am(this.context).a("hmsPkg", str);
        Log.d(TAG, "hms pkgname: ".concat(String.valueOf(str)));
    }

    private static void setService(PushService pushService) {
        gPushService = pushService;
    }

    private void unregisterInnerReceiver() {
        try {
            this.context.unregisterReceiver(this.pushInnerReceiver);
        } catch (Exception e) {
            Log.e(TAG, "unregister sys push inner receiver error");
        }
    }

    private void unregisterSysReceiver() {
        try {
            this.context.unregisterReceiver(this.pushSysReceiver);
        } catch (Exception e) {
            Log.e(TAG, "unregister sys push system receiver error");
        }
    }

    public void exitProcess() {
        Log.i(TAG, "sys push process exit");
        new am(this.context).a("rebootReason", 2);
        Process.killProcess(Process.myPid());
    }

    public boolean isPushServiceRunning() {
        return this.pushServiceRunning;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "system push begin await");
        try {
            this.pushCoreManagerLatch.await();
        } catch (InterruptedException e) {
            Log.e(TAG, "push core manager latch await error");
        }
        Log.i(TAG, "system push await over");
        try {
            if (this.pushCoreManager != null) {
                Log.i(TAG, "pushCoreManager onBind");
                return this.pushCoreManager.onBind(intent);
            }
            Log.e(TAG, "onBind pushCoreManager is null");
            return null;
        } catch (Exception e2) {
            Log.e(TAG, "onBind pushCoreManager onBind exception");
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        if (this.context == null) {
            Log.e(TAG, "context is null, oncreate failed");
            this.context = this;
        }
        super.onCreate();
        setService(this);
        setHmsPkg();
        installLoadPushCore();
        registerInnerReceiver();
        registerSysReceiver();
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.i(TAG, "sys push on destroy");
        unregisterInnerReceiver();
        unregisterSysReceiver();
        try {
            if (this.pushCoreManager != null) {
                this.pushCoreManager.destroyPushService();
            }
        } catch (Exception e) {
            Log.e(TAG, "destroy pushcore service error");
        }
        super.onDestroy();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        Log.d(TAG, "sys push onStartCommand");
        if (intent != null) {
            return 1;
        }
        Log.i(TAG, "onStartCommand, intent is null, maybe restart service called by android system");
        return 1;
    }

    public void pushCoreManageEnd() {
        if (this.pushCoreManagerLatch != null) {
            this.pushCoreManagerLatch.countDown();
        }
    }

    public boolean setParam(Service service, Bundle bundle) {
        this.context = service;
        return true;
    }

    public void setPushCoreManager(IPushManager iPushManager) {
        this.pushCoreManager = iPushManager;
    }

    public void setPushServiceRunning(boolean z) {
        this.pushServiceRunning = z;
    }
}
