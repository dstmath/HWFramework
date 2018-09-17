package com.huawei.android.pushagent;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.model.a.i;
import com.huawei.android.pushagent.model.c.b;
import com.huawei.android.pushagent.model.c.e;
import com.huawei.android.pushagent.utils.d.c;
import com.huawei.android.pushagent.utils.threadpool.a;
import com.huawei.android.pushagent.utils.tools.ShutdownReceiver;
import com.huawei.android.pushagent.utils.tools.d;
import java.io.File;
import java.util.LinkedList;

public class PushService extends Service {
    private static String TAG = "PushLog2951";
    private static PushService ii = null;
    private Context ih = null;
    private a ij = null;
    private NetworkCallback ik = new d(this);
    private b il;
    private LinkedList<e> im = new LinkedList();
    private long in = 0;
    private ShutdownReceiver io = null;
    private boolean ip = false;

    public boolean setParam(Service service, Bundle bundle) {
        this.ih = service;
        return true;
    }

    public void onCreate() {
        if (this.ih == null) {
            Log.e(TAG, "context is null, oncreate failed");
            this.ih = this;
        }
        Thread.setDefaultUncaughtExceptionHandler(new e(this));
        super.onCreate();
        if (new File("/data/misc/hwpush").exists()) {
            try {
                c.sn(this.ih);
                c.sh(TAG, "PushService:onCreate()");
                this.in = System.currentTimeMillis();
                try {
                    this.il = new b(this.ih);
                    this.il.start();
                    int i = 0;
                    while (this.il.ir == null) {
                        int i2 = i + 1;
                        if (i > 80) {
                            c.sf(TAG, "call mReceiverDispatcher run after " + i2 + " times, " + " but handler is null");
                            stopSelf();
                            return;
                        }
                        Thread.sleep(100);
                        if (i2 % 10 == 0) {
                            c.sg(TAG, "waiting for hander created times: " + i2);
                        }
                        i = i2;
                    }
                    zl(this);
                    if (d.qo()) {
                        d.qp(1, 0);
                    }
                    a.op(new f(this));
                    return;
                } catch (Throwable e) {
                    c.si(TAG, "create ReceiverDispatcher thread or get channelMgr exception ,stopself, " + e.toString(), e);
                    stopSelf();
                    return;
                }
            } catch (Throwable e2) {
                c.si(TAG, "Exception:Log.init: " + e2.toString(), e2);
                stopSelf();
                return;
            }
        }
        Log.e(TAG, "hwpush_files dir is not exist, can not work!");
        stopSelf();
    }

    private static void zl(PushService pushService) {
        ii = pushService;
    }

    private synchronized void ze() {
        zh(new com.huawei.android.pushagent.model.c.a(this.ih));
        zh(new b(this.ih));
        zh(new com.huawei.android.pushagent.model.c.c(this.ih));
        zh(new com.huawei.android.pushagent.model.c.d(this.ih));
    }

    private void zh(e eVar) {
        this.im.add(eVar);
    }

    private void zf() {
        try {
            c.sg(TAG, "initSystem(),and mReceivers  " + this.im.size());
            com.huawei.android.pushagent.b.b.ym(this.ih);
            ze();
            com.huawei.android.pushagent.a.a.ya(this.ih);
            com.huawei.android.pushagent.a.a.yd();
            com.huawei.android.pushagent.a.a.xv(0);
            zg(this.ih);
            zi(this.ih);
            zk(this.ih);
            yx(new Intent("com.huawei.action.CONNECT_PUSHSRV"));
            yx(new Intent("com.huawei.action.push.intent.CHECK_CHANNEL_CYCLE"));
            c.sh(TAG, "initProcess success");
        } catch (Throwable e) {
            c.si(TAG, "Exception:registerMyReceiver: " + e.toString(), e);
            stopSelf();
        }
    }

    public static synchronized PushService yy() {
        PushService pushService;
        synchronized (PushService.class) {
            pushService = ii;
        }
        return pushService;
    }

    public static void yx(Intent intent) {
        try {
            PushService yy = yy();
            if (yy == null) {
                c.sf(TAG, "sendBroadcast error, pushService is null");
            } else {
                yy.zb(intent);
            }
        } catch (Throwable e) {
            c.se(TAG, "call PushService:broadcast() cause " + e.toString(), e);
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        try {
            c.sg(TAG, "PushService onStartCommand");
            if (intent != null) {
                c.sg(TAG, "onStartCommand, intent is:" + intent.toURI());
                zb(intent);
            } else {
                c.sh(TAG, "onStartCommand, intent is null ,mybe restart service called by android system");
                com.huawei.android.pushagent.utils.f.a.te(this.ih);
            }
        } catch (Throwable e) {
            c.se(TAG, "call PushService:onStartCommand() cause " + e.toString(), e);
        }
        return 1;
    }

    private synchronized void zb(Intent intent) {
        if (intent == null) {
            c.sf(TAG, "when broadcastToProcess, intent is null");
            return;
        }
        c.sg(TAG, "broadcastToProcess, intent is:" + intent.getAction());
        for (e zq : this.im) {
            this.il.zq(zq, intent);
        }
        c.sh(TAG, "dispatchIntent over");
    }

    public static void za() {
        c.sg(TAG, "call exitProcess");
        if (ii != null) {
            ii.ip = true;
            ii.stopSelf();
        }
    }

    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:34:0x0138, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:35:0x0139, code:
            com.huawei.android.pushagent.utils.d.c.se(TAG, "call PushService:onDestroy() cause " + r0.toString(), r0);
     */
    /* JADX WARNING: Missing block: B:54:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDestroy() {
        c.sh(TAG, "enter PushService:onDestroy(), needExitService is:" + ii.ip);
        try {
            zb(new Intent("com.huawei.intent.action.PUSH_OFF").putExtra("Remote_Package_Name", this.ih.getPackageName()).setPackage(this.ih.getPackageName()));
        } catch (Throwable e) {
            c.se(TAG, "call PushService:onDestroy() in broadcastToProcess cause " + e.toString(), e);
        }
        Thread.sleep(1000);
        try {
            if (this.ij != null) {
                zc();
                this.ih.unregisterReceiver(this.ij);
            }
            ((ConnectivityManager) this.ih.getSystemService("connectivity")).unregisterNetworkCallback(this.ik);
            if (this.io != null) {
                this.ih.unregisterReceiver(this.io);
            }
        } catch (Throwable e2) {
            c.se(TAG, "call PushService:onDestroy() in unregisterInnerReceiver cause " + e2.toString(), e2);
        }
        try {
            if (!(this.il == null || this.il.ir == null)) {
                this.il.ir.getLooper().quit();
            }
        } catch (Throwable e22) {
            c.se(TAG, "call PushService:onDestroy() in unregisterReceiver cause " + e22.toString(), e22);
        }
        if (!this.ip) {
            long j;
            long ep = i.ea(this.ih).ep();
            if (System.currentTimeMillis() - this.in > g.aq(this.ih).dd() * 1000) {
                j = 0;
            } else {
                j = ep + 1;
            }
            ep = j == 0 ? g.aq(this.ih).de() * 1000 : j == 1 ? g.aq(this.ih).df() * 1000 : j == 2 ? g.aq(this.ih).dg() * 1000 : j >= 3 ? g.aq(this.ih).dh() * 1000 : 0;
            c.sg(TAG, "next start time will be " + (ep / 1000) + " seconds later" + " run_time_less_times is " + j + "times");
            i.ea(this.ih).eq(j);
            com.huawei.android.pushagent.utils.tools.a.qk(this.ih, new Intent().setClassName("android", "com.huawei.android.pushagentproxy.PushService"), ep);
            com.huawei.android.pushagent.utils.f.a.te(this.ih);
        }
        super.onDestroy();
    }

    private void zg(Context context) {
        int i = 0;
        if (this.ij == null) {
            this.ij = new a(this, null);
        }
        IntentFilter intentFilter = new IntentFilter();
        for (String addAction : com.huawei.android.pushagent.constant.a.vj()) {
            intentFilter.addAction(addAction);
        }
        context.registerReceiverAsUser(this.ij, UserHandle.ALL, intentFilter, null, null);
        IntentFilter intentFilter2 = new IntentFilter();
        while (i < com.huawei.android.pushagent.constant.a.vl().length) {
            intentFilter2.addAction(com.huawei.android.pushagent.constant.a.vl()[i]);
            i++;
        }
        context.registerReceiverAsUser(this.ij, UserHandle.ALL, intentFilter2, "com.huawei.pushagent.permission.INNER_RECEIVER", null);
        intentFilter2 = new IntentFilter();
        intentFilter2.addAction("com.huawei.android.push.intent.REGISTER_SPECIAL");
        context.registerReceiverAsUser(this.ij, UserHandle.ALL, intentFilter2, "com.huawei.android.permission.ANTITHEFT", null);
        intentFilter2 = new IntentFilter();
        intentFilter2.addAction("com.huawei.android.push.intent.ACTION_TERMINAL_PROTOCAL");
        context.registerReceiverAsUser(this.ij, UserHandle.ALL, intentFilter2, "com.huawei.android.permission.TERMINAL_PROTOCAL", null);
        if ("android".equals(context.getPackageName())) {
            zj(context);
        }
        if (com.huawei.android.pushagent.utils.b.un()) {
            c.sh(TAG, "register HW network policy broadcast.");
            intentFilter2 = new IntentFilter();
            intentFilter2.addAction("com.huawei.systemmanager.changedata");
            context.registerReceiverAsUser(this.ij, UserHandle.ALL, intentFilter2, "android.permission.CONNECTIVITY_INTERNAL", null);
        }
    }

    private void zj(Context context) {
        if (this.ij == null) {
            this.ij = new a(this, null);
        }
        IntentFilter intentFilter = new IntentFilter();
        for (String addAction : com.huawei.android.pushagent.constant.a.vk()) {
            intentFilter.addAction(addAction);
        }
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter2.addDataScheme("package");
        context.registerReceiverAsUser(this.ij, UserHandle.ALL, intentFilter, null, null);
        context.registerReceiverAsUser(this.ij, UserHandle.ALL, intentFilter2, null, null);
    }

    private void zk(Context context) {
        if (this.io == null) {
            this.io = new ShutdownReceiver();
        }
        Context context2 = context;
        context2.registerReceiverAsUser(this.io, UserHandle.ALL, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"), null, null);
    }

    private void zc() {
        com.huawei.android.pushagent.utils.tools.a.qg(this.ih, "com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT");
        com.huawei.android.pushagent.utils.tools.a.qh(this.ih, new Intent("com.huawei.intent.action.PUSH").putExtra("EXTRA_INTENT_TYPE", "com.huawei.android.push.intent.HEARTBEAT_REQ").setPackage(this.ih.getPackageName()));
    }

    public Context yz() {
        return this.ih;
    }

    private void zi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        Builder builder = new Builder();
        builder.addCapability(13);
        builder.addTransportType(0);
        builder.addTransportType(1);
        NetworkRequest build = builder.build();
        try {
            c.sh(TAG, " registerNetworkCallback success ");
            connectivityManager.registerNetworkCallback(build, this.ik);
        } catch (Exception e) {
            c.sh(TAG, " registerNetworkCallback = " + e.toString());
        }
    }

    private void zd(Context context, Network network, boolean z) {
        yx(new Intent("com.huawei.push.action.NET_CHANGED"));
    }
}
