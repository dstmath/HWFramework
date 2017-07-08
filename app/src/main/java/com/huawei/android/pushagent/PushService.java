package com.huawei.android.pushagent;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.microkernel.MKService;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.model.pushcommand.PushCommandProcessor;
import defpackage.a;
import defpackage.ae;
import defpackage.ag;
import defpackage.ah;
import defpackage.ao;
import defpackage.as;
import defpackage.at;
import defpackage.au;
import defpackage.aw;
import defpackage.az;
import defpackage.b;
import defpackage.ba;
import defpackage.bb;
import defpackage.bl;
import defpackage.bq;
import defpackage.bt;
import defpackage.bu;
import defpackage.bv;
import defpackage.c;
import defpackage.d;
import defpackage.g;
import defpackage.o;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

public class PushService extends MKService {
    private static String TAG;
    private static PushService c;
    private static long g;
    private static boolean h;
    private static boolean i;
    private LinkedList a;
    private d b;
    private Context context;
    private c d;
    private long e;
    private boolean f;
    private long interval;
    private NetworkCallback j;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.pushagent.PushService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.pushagent.PushService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.pushagent.PushService.<clinit>():void");
    }

    public PushService() {
        this.a = new LinkedList();
        this.d = null;
        this.e = 0;
        this.f = false;
        this.context = null;
        this.interval = 30000;
        this.j = new b(this);
    }

    private synchronized void a() {
        a(new ah(this.context), null);
        a(new PushCommandProcessor(this.context), null);
        a(new as(this.context), null);
        a(new at(this.context), null);
    }

    private void a(Context context, Network network, boolean z) {
        Intent intent = new Intent("com.huawei.push.action.NET_CHANGED");
        intent.putExtra("networkState", z);
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
            if (network != null) {
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                if (networkInfo != null) {
                    int type = networkInfo.getType();
                    aw.d(TAG, "netType is " + type);
                    intent.putExtra("networkType", type);
                }
            }
        } catch (Exception e) {
            aw.e(TAG, e.toString());
        }
        a(intent);
    }

    public static void a(Intent intent) {
        try {
            PushService c = c();
            if (c == null) {
                aw.e(TAG, "sendBroadcast error, pushService is null");
                return;
            }
            aw.i(TAG, "broadcast(),and mReceivers  " + c.a.size());
            c.b(intent);
        } catch (Throwable e) {
            aw.d(TAG, "call PushService:broadcast() cause " + e.toString(), e);
        }
    }

    private static void a(PushService pushService) {
        c = pushService;
    }

    private void a(o oVar, IntentFilter intentFilter) {
        this.a.add(oVar);
    }

    private static boolean a(Context context) {
        return System.currentTimeMillis() < g || (ae.l(context).aa() * 1000) + g < System.currentTimeMillis();
    }

    private static boolean a(Context context, String str) {
        boolean z = true;
        aw.d(TAG, "check frameworkPushService");
        if (context == null) {
            aw.i(TAG, "mContext is null");
            return false;
        }
        bt btVar = new bt(context, "pushConfig");
        int i = btVar.getInt("NeedMyServiceRun");
        if ("android.intent.action.PACKAGE_ADDED".equals(str) || "android.intent.action.PACKAGE_REMOVED".equals(str) || i == 0) {
            String ae = bu.ae(context);
            long x = bu.x(context, ae);
            aw.d(TAG, "get voted push version is " + x + ", pkgName is " + ae);
            boolean z2 = SystemProperties.getBoolean("ro.config.push_enable", "CN".equals(SystemProperties.get("ro.product.locale.region")));
            aw.d(TAG, "push_enable is " + z2);
            if (0 >= x || x > 2530 || (2523 < x && x <= 2530 && z2)) {
                if (1 != i) {
                    btVar.a("NeedMyServiceRun", Integer.valueOf(1));
                }
                aw.d(TAG, "start frameworkPush service");
                return true;
            }
            if (1 == i) {
                aw.i(TAG, "Push app not suitable or push_enable is false, stop frameworkPush  service");
                ao.z(context).bv.clear();
                au.b(context, 3);
                ChannelMgr.g(context).aT();
            }
            if (2 == i) {
                return false;
            }
            btVar.a("NeedMyServiceRun", Integer.valueOf(2));
            return false;
        }
        if (1 != i) {
            z = false;
        }
        return z;
    }

    private void b() {
        try {
            aw.d(TAG, "initProcess(),and mReceivers  " + this.a.size());
            if (bv.cs()) {
                bv.c(1, 0);
            }
            bb.aa(this.context);
            if (d(this.context)) {
                i = true;
                Intent intent = new Intent("com.huawei.intent.action.PUSH").setPackage(this.context.getPackageName());
                intent.putExtra("EXTRA_INTENT_TYPE", "delay_start_push");
                bq.b(this.context, intent, this.interval);
            } else {
                a();
            }
            b(this.context);
            e(this.context);
        } catch (Throwable e) {
            aw.a(TAG, "Exception:registerMyReceiver: " + e.toString(), e);
            stopService();
        }
    }

    private void b(Context context) {
        int i = 0;
        if (this.d == null) {
            this.d = new c();
        }
        IntentFilter intentFilter = new IntentFilter();
        for (String addAction : ba.bQ()) {
            intentFilter.addAction(addAction);
        }
        context.registerReceiverAsUser(this.d, UserHandle.ALL, intentFilter, null, null);
        IntentFilter intentFilter2 = new IntentFilter();
        while (i < ba.bP().length) {
            intentFilter2.addAction(ba.bP()[i]);
            i++;
        }
        context.registerReceiverAsUser(this.d, UserHandle.ALL, intentFilter2, "com.huawei.pushagent.permission.INNER_RECEIVER", null);
        intentFilter2 = new IntentFilter();
        intentFilter2.addAction("com.huawei.android.push.intent.REGISTER_SPECIAL");
        context.registerReceiverAsUser(this.d, UserHandle.ALL, intentFilter2, "com.huawei.android.permission.ANTITHEFT", null);
        if ("android".equals(context.getPackageName())) {
            c(context);
        }
    }

    private synchronized void b(Intent intent) {
        if (intent == null) {
            aw.d(TAG, "when broadcastToProcess, intent is null");
        } else {
            aw.d(TAG, "broadcastToProcess, intent is:" + intent.getAction());
            Iterator it = this.a.iterator();
            while (it.hasNext()) {
                this.b.a((o) it.next(), intent);
            }
        }
    }

    public static synchronized PushService c() {
        PushService pushService;
        synchronized (PushService.class) {
            pushService = c;
        }
        return pushService;
    }

    private void c(Context context) {
        if (this.d == null) {
            this.d = new c();
        }
        IntentFilter intentFilter = new IntentFilter();
        for (String addAction : ba.bR()) {
            intentFilter.addAction(addAction);
        }
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter2.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter2.addDataScheme("package");
        context.registerReceiverAsUser(this.d, UserHandle.ALL, intentFilter, null, null);
        context.registerReceiverAsUser(this.d, UserHandle.ALL, intentFilter2, null, null);
    }

    public static void d() {
        aw.d(TAG, "call exitProcess");
        if (c != null) {
            c.f = true;
            c.stopService();
        }
    }

    private boolean d(Context context) {
        aw.d(TAG, "enter needDelayIntent");
        if (!TextUtils.isEmpty(az.k(context, "device_info", "deviceId"))) {
            aw.d(TAG, "local deviceId is not empty");
            return false;
        } else if (h) {
            return false;
        } else {
            h = true;
            if (TextUtils.isEmpty(au.U(context))) {
                aw.d(TAG, "first enter, imei is empty, begin to wait 1 minute");
                return true;
            }
            aw.d(TAG, "first enter, imei is not empty, no deed to wait");
            return false;
        }
    }

    private static void e() {
        g = System.currentTimeMillis();
    }

    private void e(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        Builder builder = new Builder();
        builder.addCapability(13);
        builder.addTransportType(0);
        builder.addTransportType(1);
        NetworkRequest build = builder.build();
        try {
            aw.i(TAG, " registerNetworkCallback success ");
            connectivityManager.registerNetworkCallback(build, this.j);
        } catch (Exception e) {
            aw.i(TAG, " registerNetworkCallback = " + e.toString());
        }
    }

    private void f() {
        bq.w(this.context, "com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT");
        bq.h(this.context, new Intent("com.huawei.intent.action.PUSH").putExtra("EXTRA_INTENT_TYPE", "com.huawei.android.push.intent.HEARTBEAT_REQ").putExtra("heartbeat_interval", 2592000000L).setPackage(this.context.getPackageName()));
    }

    public Context getContext() {
        return this.context != null ? this.context : MKService.getAppContext() != null ? MKService.getAppContext() : this;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        if (this.context == null) {
            this.context = getContext();
        }
        Thread.setDefaultUncaughtExceptionHandler(new a(this));
        super.onCreate();
        if (new File("/data/misc/hwpush").exists()) {
            try {
                aw.init(this.context);
                aw.i(TAG, "PushService:onCreate()");
                this.e = System.currentTimeMillis();
                try {
                    this.b = new d(this.context);
                    this.b.start();
                    int i = 0;
                    while (this.b.mHandler == null) {
                        int i2 = i + 1;
                        if (i > 80) {
                            aw.e(TAG, "call mReceiverDispatcher run after " + i2 + ", " + " but handler is null");
                            stopService();
                            return;
                        }
                        Thread.sleep(100);
                        if (i2 % 10 == 0) {
                            aw.d(TAG, "wait hander created: " + i2);
                            i = i2;
                        } else {
                            i = i2;
                        }
                    }
                    ChannelMgr.g(this.context);
                    a(this);
                    b();
                    return;
                } catch (Throwable e) {
                    aw.a(TAG, "create ReceiverDispatcher thread or get channelMgr exception ,stopself, " + e.toString(), e);
                    stopService();
                    return;
                }
            } catch (Throwable e2) {
                aw.a(TAG, "Exception:Log.init: " + e2.toString(), e2);
                stopService();
                return;
            }
        }
        Log.e(TAG, "hwpush_files dir is not exist, can not work!");
        stopService();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDestroy() {
        aw.i(TAG, "enter PushService:onDestroy(), needExitService is:" + c.f);
        try {
            b(new Intent("com.huawei.intent.action.PUSH_OFF").putExtra("Remote_Package_Name", this.context.getPackageName()).setPackage(this.context.getPackageName()));
        } catch (Throwable e) {
            aw.d(TAG, "call PushService:onDestroy() in broadcastToProcess cause " + e.toString(), e);
        }
        Thread.sleep(1000);
        try {
            if (this.d != null) {
                f();
                this.context.unregisterReceiver(this.d);
            }
            ((ConnectivityManager) this.context.getSystemService("connectivity")).unregisterNetworkCallback(this.j);
        } catch (Throwable e2) {
            aw.d(TAG, "call PushService:onDestroy() in unregisterInnerReceiver cause " + e2.toString(), e2);
        }
        try {
            if (!(this.b == null || this.b.mHandler == null)) {
                this.b.mHandler.getLooper().quit();
            }
        } catch (Throwable e22) {
            aw.d(TAG, "call PushService:onDestroy() in unregisterReceiver cause " + e22.toString(), e22);
        }
        if (!this.f) {
            long a = System.currentTimeMillis() - this.e > ae.l(this.context).J() * 1000 ? 0 : ag.a(this.context, "run_time_less_times", 0) + 1;
            long K = a == 0 ? ae.l(this.context).K() * 1000 : a == 1 ? ae.l(this.context).L() * 1000 : a == 2 ? ae.l(this.context).M() * 1000 : a >= 3 ? ae.l(this.context).N() * 1000 : 0;
            aw.d(TAG, "next start time will be " + (K / 1000) + " seconds later" + " run_time_less_times is " + a + "times");
            ag.a(this.context, new g("run_time_less_times", Long.class, Long.valueOf(a)));
            bq.c(this.context, new Intent().setClassName("android", "com.huawei.android.pushagentproxy.PushService"), K);
            bl.ad(this.context);
        }
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        try {
            aw.d(TAG, "PushService onStartCommand");
            if (intent != null) {
                aw.d(TAG, "onStartCommand, intent is:" + intent.toURI());
                b(intent);
            } else {
                aw.i(TAG, "onStartCommand, intent is null ,mybe restart service called by android system");
                bl.ad(this.context);
            }
        } catch (Throwable e) {
            aw.d(TAG, "call PushService:onStartCommand() cause " + e.toString(), e);
        }
        return 1;
    }

    public boolean setParam(Service service, Bundle bundle) {
        this.context = service;
        return true;
    }
}
