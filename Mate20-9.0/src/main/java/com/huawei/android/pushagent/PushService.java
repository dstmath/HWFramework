package com.huawei.android.pushagent;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;
import com.huawei.android.content.ContextEx;
import com.huawei.android.feature.compat.InstallCompat;
import com.huawei.android.feature.install.InstallStorageManager;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallManager;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.pushagent.dynamicload.IPushManager;
import java.io.File;

public class PushService extends Service {
    private static PushService T = null;
    /* access modifiers changed from: private */
    public static final byte[] Z = new byte[0];
    ar U = null;
    as V = null;
    /* access modifiers changed from: private */
    public FeatureLocalInstallManager W;
    private Context X = null;
    public boolean Y = false;
    public IPushManager aa;

    /* access modifiers changed from: private */
    public void a(aq aqVar) {
        synchronized (Z) {
            if (aq.LOCAL_VERSION == aqVar) {
                ax.b(this.X, this.W).g();
            } else if (aq.HMS_VERSION == aqVar) {
                at a = at.a(this.X, this.W);
                a.time = 0;
                a.f();
            } else if (aq.NC_VERSION == aqVar) {
                ay.c(this.X, this.W).i();
            } else {
                ay.c(this.X, this.W).j();
            }
        }
    }

    public static synchronized PushService b() {
        PushService pushService;
        synchronized (PushService.class) {
            pushService = T;
        }
        return pushService;
    }

    /* access modifiers changed from: private */
    public aq c() {
        long versionCode;
        long versionCode2;
        long j;
        aq aqVar;
        synchronized (Z) {
            versionCode = ax.b(this.X, this.W).getVersionCode();
            versionCode2 = ay.c(this.X, this.W).getVersionCode();
        }
        bf bfVar = new bf(this.X);
        int i = bfVar.NO;
        Object b = bfVar.b("forceLoad", Integer.valueOf(i));
        if (!(bfVar.YES == (b instanceof Integer ? ((Integer) b).intValue() : b instanceof Long ? (int) ((Long) b).longValue() : i))) {
            synchronized (Z) {
                j = at.a(this.X, this.W).getVersionCode();
            }
            if (versionCode < (versionCode2 > j ? versionCode2 : j)) {
                aqVar = versionCode2 >= j ? aq.NC_VERSION : aq.HMS_VERSION;
            }
            aqVar = aq.LOCAL_VERSION;
        } else if (versionCode != versionCode2) {
            aqVar = aq.NC_FORCE_VERSION;
            j = -1;
        } else {
            j = -1;
            aqVar = aq.LOCAL_VERSION;
        }
        Log.i("PushLogSys", "forceLoad is " + r5 + ". local pushcore version is " + versionCode + ". NC pushcore version is " + versionCode2 + ". HMS pushcore version is " + j + ". selected version is " + aqVar);
        return aqVar;
    }

    public static void d() {
        Log.i("PushLogSys", "sys push process exit");
        Process.killProcess(Process.myPid());
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        if (this.X == null) {
            Log.e("PushLogSys", "context is null, oncreate failed");
            this.X = this;
        }
        super.onCreate();
        T = this;
        InstallStorageManager.initBaseDir(new File("/data/misc/hwpush"));
        synchronized (Z) {
            this.W = new FeatureLocalInstallManager(this.X);
        }
        this.U = new ar(this, (byte) 0);
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.huawei.android.push.intent.CHECK_HWPUSH_VERSION");
            ContextEx.registerReceiverAsUser(this.X, this.U, UserHandleEx.ALL, intentFilter, "com.huawei.pushagent.permission.INNER_RECEIVER", null);
        } catch (Exception e) {
            Log.e("PushLogSys", "register sys push inner receiver error");
        }
        this.V = new as(this, (byte) 0);
        try {
            IntentFilter intentFilter2 = new IntentFilter();
            intentFilter2.addAction("android.intent.action.PACKAGE_ADDED");
            intentFilter2.addDataScheme("package");
            ContextEx.registerReceiverAsUser(this.X, this.V, UserHandleEx.ALL, intentFilter2, null, null);
        } catch (Exception e2) {
            Log.e("PushLogSys", "register sys push inner receiver error");
        }
        int i = -100;
        try {
            i = InstallCompat.install(this.X);
        } catch (Exception e3) {
            Log.e("PushLogSys", "pre install pushcore from local error: " + bb.a(e3));
        }
        Log.i("PushLogSys", "pre install pushcore result is " + i);
        if (i == 0) {
            a(c());
            return;
        }
        Log.e("PushLogSys", "handle local pushcore install error");
        synchronized (Z) {
            ax.b(this.X, this.W);
            ax.a(i);
        }
        a(aq.NC_FORCE_VERSION);
    }

    public void onDestroy() {
        Log.i("PushLogSys", "sys push on destroy");
        try {
            this.X.unregisterReceiver(this.U);
        } catch (Exception e) {
            Log.e("PushLogSys", "unregister sys push inner receiver error");
        }
        try {
            this.X.unregisterReceiver(this.V);
        } catch (Exception e2) {
            Log.e("PushLogSys", "unregister sys push system receiver error");
        }
        try {
            if (this.aa != null) {
                this.aa.destroyPushService();
            }
        } catch (Exception e3) {
            Log.e("PushLogSys", "destroy pushcore service error");
        }
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        Log.d("PushLogSys", "sys push onStartCommand");
        if (intent == null) {
            Log.i("PushLogSys", "onStartCommand, intent is null, maybe restart service called by android system");
        }
        return 1;
    }

    public boolean setParam(Service service, Bundle bundle) {
        this.X = service;
        return true;
    }
}
