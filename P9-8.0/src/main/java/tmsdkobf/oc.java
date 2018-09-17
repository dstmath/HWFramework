package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.f;
import tmsdk.common.utils.i;

public class oc {
    private nl CT;
    private PowerManager Eg;
    private a Ho;
    private b Hp;
    private h Hq;
    private AtomicInteger Hr = new AtomicInteger(0);
    private boolean Hs = false;
    private Runnable Ht = new Runnable() {
        public void run() {
            oc.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    synchronized (oc.this) {
                        if (oc.this.Hs) {
                            nv.z("SharkTcpControler", "[tcp_control][shark_conf][shark_alarm] keep after send timeout, tryCloseConnectionAsyn()");
                            oc.this.gI();
                            oc.this.Hs = false;
                        }
                    }
                }
            }, 5000);
            mb.d("SharkTcpControler", "[tcp_control][shark_conf][shark_alarm] keep after send timeout(by alarm), delay 5s by handler");
        }
    };
    private boolean Hu = false;
    private Context mContext = TMSDKContext.getApplicaionContext();
    private Handler mHandler = new Handler(nu.getLooper()) {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    mb.n("SharkTcpControler", "[tcp_control][shark_conf] MSG_EXE_RULE_OPEN");
                    oc.this.gJ();
                    oc.this.Ho.gP();
                    return;
                case 1:
                    mb.n("SharkTcpControler", "[tcp_control][shark_conf] MSG_EXE_RULE_CLOSE");
                    oc.this.gI();
                    return;
                case 3:
                    mb.n("SharkTcpControler", "[tcp_control][shark_conf] MSG_EXE_RULE_CYCLE");
                    oc.this.gL();
                    return;
                default:
                    return;
            }
        }
    };

    public interface a {
        void gP();

        void onClose();
    }

    private class b extends if {
        private b() {
        }

        /* synthetic */ b(oc ocVar, AnonymousClass1 anonymousClass1) {
            this();
        }

        public void doOnRecv(Context context, Intent intent) {
            mb.d("SharkTcpControler", "[tcp_control][shark_conf]doOnRecv()");
            String action = intent.getAction();
            String str = intent.getPackage();
            if (action == null || str == null || !str.equals(TMSDKContext.getApplicaionContext().getPackageName())) {
                mb.d("SharkTcpControler", "[tcp_control][shark_conf]TcpControlReceiver.onReceive(), null action or from other pkg, ignore");
                return;
            }
            if (action.equals("action_keep_alive_cycle")) {
                oc.this.mHandler.sendEmptyMessage(3);
            } else if (action.equals("action_keep_alive_close")) {
                oc.this.mHandler.sendEmptyMessage(1);
            }
        }
    }

    public oc(nl nlVar, a aVar) {
        this.CT = nlVar;
        this.Ho = aVar;
        try {
            this.Eg = (PowerManager) this.mContext.getSystemService("power");
        } catch (Throwable th) {
        }
    }

    private static final int bA(int i) {
        return bz(i * 60);
    }

    private static final int bz(int i) {
        return i * 60;
    }

    private static void d(h hVar) {
        if (hVar != null) {
            if (hVar.A != null && hVar.A.size() > 0) {
                t(hVar.A);
            } else {
                hVar.A = gK();
            }
            if (hVar.interval <= 30) {
                hVar.interval = 30;
            }
            if (hVar.B <= 0) {
                hVar.B = SmsCheckResult.ESCT_300;
            }
            if (hVar.E <= 0) {
                hVar.E = 120;
            }
            if (hVar.F <= 0) {
                hVar.F = 10;
            }
        }
    }

    private void gJ() {
        if (this.Hr.get() < 0) {
            this.Hr.set(0);
        }
        String str = "SharkTcpControler";
        mb.n(str, "[tcp_control][shark_conf]markKeepAlive(), refCount: " + this.Hr.incrementAndGet());
    }

    private static ArrayList<f> gK() {
        ArrayList<f> arrayList = new ArrayList();
        f fVar = new f();
        fVar.start = bA(0);
        fVar.n = bz(10);
        fVar.o = bz(60);
        arrayList.add(fVar);
        fVar = new f();
        fVar.start = bA(8);
        fVar.n = bz(15);
        fVar.o = bz(15);
        arrayList.add(fVar);
        fVar = new f();
        fVar.start = bA(15);
        fVar.n = bz(10);
        fVar.o = bz(20);
        arrayList.add(fVar);
        return arrayList;
    }

    private void gL() {
        f gN = gN();
        if (gN != null) {
            gM();
            if (ck("execRule")) {
                this.mHandler.sendEmptyMessage(0);
                oj.a(this.mContext, "action_keep_alive_close", (long) (gN.n * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY));
                mb.r("SharkTcpControler", "[tcp_control][shark_conf]now open connection, after " + gN.n + "s close connection");
            } else {
                mb.s("SharkTcpControler", "[tcp_control][f_p][h_b][shark_conf]execRule(), scSharkConf: donnot keepAlive!");
            }
            oj.a(this.mContext, "action_keep_alive_cycle", (long) ((gN.n + gN.o) * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY));
            mb.r("SharkTcpControler", "[tcp_control][shark_conf]execRule(), next cycle in " + (gN.n + gN.o) + "s");
            return;
        }
        mb.s("SharkTcpControler", "[tcp_control][shark_conf]no KeepAlivePolicy for current time!");
    }

    private void gM() {
        mb.d("SharkTcpControler", "[tcp_control][shark_conf]cancelOldAction()");
        oj.h(this.mContext, "action_keep_alive_close");
        oj.h(this.mContext, "action_keep_alive_cycle");
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(3);
        this.mHandler.removeMessages(0);
    }

    /* JADX WARNING: Missing block: B:6:0x000a, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private f gN() {
        synchronized (this) {
            h az = az();
            if (!(az == null || az.A == null || az.A.size() <= 0)) {
                int gO = gO();
                int size = az.A.size() - 1;
                while (size >= 0) {
                    f fVar = (f) az.A.get(size);
                    if (fVar.start > gO) {
                        size--;
                    } else {
                        mb.n("SharkTcpControler", "[tcp_control][shark_conf]getRuleAtNow(), fixed policy: start hour: " + (fVar.start / 3600) + " start: " + fVar.start + " keep: " + fVar.n + " close: " + fVar.o);
                        return fVar;
                    }
                }
            }
        }
    }

    private int gO() {
        Calendar instance = Calendar.getInstance();
        return instance != null ? ((instance.get(11) * 3600) + (instance.get(12) * 60)) + instance.get(13) : 0;
    }

    private static void t(List<f> list) {
        if (list != null && list.size() != 0) {
            if (((f) list.get(0)).start > 0) {
                f fVar = (f) list.get(list.size() - 1);
                f fVar2 = new f();
                fVar2.start = bA(0);
                fVar2.n = fVar.n;
                fVar2.o = fVar.o;
                list.add(0, fVar2);
            }
            try {
                Collections.sort(list, new Comparator<f>() {
                    /* renamed from: a */
                    public int compare(f fVar, f fVar2) {
                        return fVar.start - fVar2.start;
                    }
                });
            } catch (Throwable e) {
                mb.b("SharkTcpControler", "[tcp_control][shark_conf]checkAndSort() exception: " + e, e);
            }
        }
    }

    public h az() {
        synchronized (this) {
            if (this.Hq == null) {
                this.Hq = this.CT.aQ();
                if (this.Hq == null) {
                    this.Hq = new h();
                    if (nu.gc()) {
                        this.Hq.interval = 30;
                        this.Hq.B = 60;
                    } else {
                        this.Hq.interval = SmsCheckResult.ESCT_270;
                        this.Hq.B = SmsCheckResult.ESCT_300;
                    }
                    this.Hq.z = new ArrayList();
                    this.Hq.A = gK();
                    this.Hq.C = true;
                    this.Hq.D = true;
                    this.Hq.E = 120;
                    this.Hq.F = 10;
                } else {
                    d(this.Hq);
                }
            }
        }
        return this.Hq;
    }

    public void c(h hVar) {
        if (hVar != null) {
            synchronized (this) {
                this.Hq = hVar;
                this.CT.b(this.Hq);
                d(this.Hq);
            }
            return;
        }
        mb.s("SharkTcpControler", "[tcp_control][shark_conf]onSharkConfPush(), scSharkConf == null");
    }

    boolean ck(String str) {
        h az = az();
        if (az == null) {
            return true;
        }
        boolean z = true;
        if (!(az.C || eb.iJ == i.iG())) {
            z = false;
            mb.r("SharkTcpControler", "[tcp_control][shark_conf] shouldKeepAlive(), not allow in none wifi! timing: " + str);
        }
        if (z && !az.D) {
            boolean z2 = false;
            if (this.Eg != null) {
                try {
                    z2 = !this.Eg.isScreenOn();
                } catch (Throwable th) {
                }
            }
            if (z2) {
                mb.r("SharkTcpControler", "[tcp_control][shark_conf] shouldKeepAlive(), not allow on screen off! timing: " + str);
                z = false;
            }
        }
        return z;
    }

    public synchronized void gC() {
        if (!this.Hu) {
            f.d("SharkTcpControler", "[tcp_control][shark_conf]startTcpControl()");
            if (this.Hp == null) {
                this.Hp = new b(this, null);
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("action_keep_alive_close");
                intentFilter.addAction("action_keep_alive_cycle");
                try {
                    this.mContext.registerReceiver(this.Hp, intentFilter);
                } catch (Throwable th) {
                    mb.s("SharkTcpControler", "[tcp_control][shark_conf]registerReceiver exception: " + th);
                }
            }
            this.mHandler.sendEmptyMessage(3);
            this.Hu = true;
            return;
        }
        return;
    }

    public synchronized void gD() {
        if (this.Hu) {
            f.d("SharkTcpControler", "[tcp_control][shark_conf]stopTcpControl()");
            gM();
            if (this.Hp != null) {
                try {
                    this.mContext.unregisterReceiver(this.Hp);
                    this.Hp = null;
                } catch (Throwable th) {
                    mb.s("SharkTcpControler", "[tcp_control][shark_conf]unregisterReceiver exception: " + th);
                }
            }
            gI();
            this.Hu = false;
            return;
        }
        return;
    }

    public int gG() {
        return this.Hr.get();
    }

    public void gH() {
        this.Hr.set(0);
    }

    void gI() {
        int decrementAndGet = this.Hr.decrementAndGet();
        mb.n("SharkTcpControler", "[tcp_control][shark_conf]tryCloseConnectionAsyn, refCount: " + decrementAndGet);
        if (decrementAndGet <= 0) {
            this.Hr.set(0);
            this.Ho.onClose();
        }
    }

    void z(long -l_3_J) {
        Object obj = 1;
        long j = 1000 * ((long) az().B);
        if (j < -l_3_J) {
            obj = null;
        }
        if (obj != null) {
            -l_3_J = j;
        }
        synchronized (this) {
            if (!this.Hs) {
                mb.n("SharkTcpControler", "[tcp_control][shark_conf] extendConnectOnSend(), markKeepConnection()");
                gJ();
                this.Hs = true;
            }
        }
        mb.n("SharkTcpControler", "[tcp_control][shark_conf] " + (-l_3_J / 1000));
        lm.eC().bH("action_keep_alive_after_send_end");
        lm.eC().a("action_keep_alive_after_send_end", -l_3_J, this.Ht);
    }
}
