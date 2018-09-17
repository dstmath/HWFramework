package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import com.qq.taf.jce.JceStruct;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;

public class nz extends BaseManagerC implements tmsdkobf.nw.c, tmsdkobf.nw.d, tmsdkobf.nw.e {
    private static int GG = 0;
    private static final long[] GH = new long[]{100, 60000, 120000};
    private nl CT;
    private nw Dm;
    private ExecutorService EO;
    private TreeMap<Integer, oh<JceStruct, ka, c>> FQ = new TreeMap();
    private Handler FR = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 11:
                    Object[] objArr = (Object[]) message.obj;
                    d dVar = (d) objArr[0];
                    if (dVar.Gc <= 0) {
                        dVar.Gk.onFinish(dVar.Ec, ((Integer) objArr[1]).intValue(), ((Integer) objArr[2]).intValue(), ((Integer) objArr[3]).intValue(), dVar.Gi);
                        return;
                    } else if (dVar.Ha != null) {
                        dVar.Ha.a(dVar.FM, dVar.Gc, dVar.Ec, ((Integer) objArr[1]).intValue(), ((Integer) objArr[2]).intValue(), ((Integer) objArr[3]).intValue(), dVar.GZ);
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        }
    };
    private boolean GA = false;
    private LinkedList<no> GB = null;
    private boolean GC = false;
    private boolean GD = false;
    private boolean GE = false;
    private boolean GF = false;
    private List<jw> GI = new ArrayList();
    private List<jw> GJ = new ArrayList();
    private List<a> GK = new ArrayList();
    private a GL = null;
    private List<b> Go = new ArrayList();
    private ArrayList<d> Gp = new ArrayList();
    private ok<Long> Gq = new ok(CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY);
    private kj Gr;
    private boolean Gs = false;
    private boolean Gt = false;
    private boolean Gu = false;
    private boolean Gv = false;
    private boolean Gw = false;
    private boolean Gx = false;
    private boolean Gy = false;
    private boolean Gz = false;
    private final String TAG = "SharkProtocolQueue";
    private Context mContext;
    private Handler vH = new Handler(nu.getLooper()) {
        public void handleMessage(Message message) {
            int i;
            Iterator it;
            Iterator it2;
            List<b> arrayList;
            switch (message.what) {
                case 1:
                    nz.this.vH.removeMessages(1);
                    i = 0;
                    e eVar = new e(nz.this, null);
                    ArrayList arrayList2 = new ArrayList();
                    synchronized (nz.this.Gp) {
                        it = nz.this.Gp.iterator();
                        while (it.hasNext()) {
                            d dVar = (d) it.next();
                            boolean z = true;
                            if (nz.this.Gr != null) {
                                z = nz.this.Gr.d(dVar.Gf, dVar.Gg);
                            }
                            if ((dVar.Gj & 1073741824) != 0) {
                                if (z) {
                                    eVar.He.add(dVar);
                                } else {
                                    arrayList2.add(dVar);
                                }
                            } else if (dVar.Hc.cJ()) {
                                nt.ga().bp(dVar.Ec);
                            } else if (z) {
                                eVar.a(Integer.valueOf(dVar.Ec), dVar);
                            } else {
                                arrayList2.add(dVar);
                            }
                            i++;
                        }
                        nz.this.Gp.clear();
                        if (arrayList2.size() > 0) {
                            nz.this.Gp.addAll(arrayList2);
                        }
                    }
                    if (i > 0) {
                        nz.this.EO.submit(eVar);
                        return;
                    }
                    return;
                case 2:
                    nu.Ev = true;
                    mb.d("SharkProtocolQueue", "[shark_init]=========== MSG_INIT_FINISH ==========");
                    synchronized (nz.this.Gp) {
                        i = nz.this.Gp.size();
                    }
                    if (i > 0) {
                        nz.this.vH.sendEmptyMessage(1);
                    }
                    if (nz.this.Gs) {
                        nz.this.N(false);
                    }
                    if (nz.this.Gt) {
                        nz.this.N(true);
                    }
                    if (nz.this.Gu) {
                        nz.this.gz();
                    }
                    if (nz.this.Gv) {
                        nz.this.gh();
                    }
                    if (nz.this.Gw) {
                        nz.this.onReady();
                    }
                    if (nz.this.Gx) {
                        nz.this.gA();
                    }
                    if (nz.this.Gy) {
                        nz.this.gm();
                    }
                    if (nz.this.Gz) {
                        nz.this.gn();
                    }
                    if (nz.this.GA) {
                        nz.this.gB();
                    }
                    if (nz.this.GC) {
                        nz.this.gx();
                    }
                    if (nz.this.GB != null) {
                        it2 = nz.this.GB.iterator();
                        while (it2.hasNext()) {
                            no noVar = (no) it2.next();
                            if (noVar != null) {
                                nz.this.b(noVar.ii, noVar.DK, noVar.DL);
                            }
                        }
                        nz.this.GB = null;
                    }
                    if (nz.this.GD) {
                        nz.this.GD = false;
                        CharSequence b = nz.this.b();
                        if (!TextUtils.isEmpty(b)) {
                            mb.n("SharkProtocolQueue", "[cu_guid] notifyGuidGot on init finished");
                            nz.this.i(0, b);
                        }
                    }
                    if (nz.this.GE) {
                        nz.this.gC();
                    }
                    if (nz.this.GF) {
                        nz.this.gD();
                        return;
                    }
                    return;
                case 3:
                    mb.d("SharkProtocolQueue", "[shark_push]handle MSG_CLEAR_EXPIRED_PUSH");
                    arrayList = new ArrayList();
                    List arrayList3 = new ArrayList();
                    synchronized (nz.this.Go) {
                        if (nz.this.Go.size() > 0) {
                            long currentTimeMillis = System.currentTimeMillis();
                            for (b bVar : nz.this.Go) {
                                if ((currentTimeMillis - bVar.GT < 600000 ? 1 : null) == null) {
                                    arrayList.add(bVar);
                                } else {
                                    arrayList3.add(bVar);
                                }
                            }
                            nz.this.Go.clear();
                            nz.this.Go.addAll(arrayList3);
                        }
                    }
                    mb.d("SharkProtocolQueue", "[shark_push]handle MSG_CLEAR_EXPIRED_PUSH, expired: " + arrayList.size() + " remain: " + arrayList3.size());
                    if (arrayList.size() > 0) {
                        for (b bVar2 : arrayList) {
                            if (bVar2.GW != 0) {
                                mb.d("SharkProtocolQueue", "[shark_push]no need to sendPushResp() for expired gift, cmd: " + bVar2.GU.bz + " pushId: " + bVar2.ex);
                            } else {
                                mb.d("SharkProtocolQueue", "[shark_push]sendPushResp() for expired push, cmd: " + bVar2.GU.bz + " pushId: " + bVar2.ex);
                                nz.this.a(bVar2.GU.ey, bVar2.ex, bVar2.GU.bz, null, null, -2, -1000000001);
                            }
                        }
                        return;
                    }
                    return;
                case 4:
                    mb.d("SharkProtocolQueue", "[shark_push]handle MSG_CLEAR_PUSH_CACHE");
                    arrayList = new ArrayList();
                    synchronized (nz.this.Go) {
                        if (nz.this.Go.size() > 0) {
                            arrayList.addAll(nz.this.Go);
                            nz.this.Go.clear();
                        }
                    }
                    mb.d("SharkProtocolQueue", "[shark_push]handle MSG_CLEAR_PUSH_CACHE, " + arrayList.size() + " -> 0");
                    if (arrayList.size() > 0) {
                        for (b bVar3 : arrayList) {
                            if (bVar3.GW != 0) {
                                mb.d("SharkProtocolQueue", "[shark_push]no need to sendPushResp() on gift cleared, cmd: " + bVar3.GU.bz + " pushId: " + bVar3.ex);
                            } else {
                                mb.d("SharkProtocolQueue", "[shark_push]sendPushResp() on push cleared, cmd: " + bVar3.GU.bz + " pushId: " + bVar3.ex);
                                nz.this.a(bVar3.GU.ey, bVar3.ex, bVar3.GU.bz, null, null, -2, -1000000001);
                            }
                        }
                        return;
                    }
                    return;
                case 5:
                    oh ohVar;
                    i = message.arg1;
                    mb.d("SharkProtocolQueue", "[shark_push]handle MSG_CHECK_CACHED_PUSH for cmd: " + i);
                    synchronized (nz.this.FQ) {
                        ohVar = (oh) nz.this.FQ.get(Integer.valueOf(i));
                    }
                    List arrayList4 = new ArrayList();
                    List arrayList5 = new ArrayList();
                    synchronized (nz.this.Go) {
                        if (nz.this.Go.size() > 0) {
                            for (b bVar4 : nz.this.Go) {
                                if (bVar4.GU.bz != i) {
                                    arrayList5.add(bVar4);
                                } else {
                                    arrayList4.add(bVar4);
                                }
                            }
                            nz.this.Go.clear();
                            nz.this.Go.addAll(arrayList5);
                        }
                    }
                    mb.d("SharkProtocolQueue", "[shark_push]handle MSG_CHECK_CACHED_PUSH, fixed: " + arrayList4.size() + " remain: " + arrayList5.size());
                    if (ohVar != null && arrayList4 != null && arrayList4.size() > 0) {
                        oh ohVar2 = ohVar;
                        List list = arrayList4;
                        final List list2 = arrayList4;
                        final int i2 = i;
                        final oh ohVar3 = ohVar;
                        ((ki) fj.D(4)).addTask(new Runnable() {
                            public void run() {
                                for (b bVar : list2) {
                                    if (bVar.GW != 0) {
                                        mb.d("SharkProtocolQueue", "[shark_push]handle cached gift, cmd: " + i2 + " pushId: " + bVar.ex);
                                        nz.this.b(bVar.ex, bVar.GU, bVar.GV, ohVar3);
                                    } else {
                                        mb.d("SharkProtocolQueue", "[shark_push]handle cached push, cmd: " + i2 + " pushId: " + bVar.ex);
                                        nz.this.a(bVar.ex, bVar.GU, bVar.GV, ohVar3);
                                    }
                                }
                            }
                        }, "shark callback: check cached push");
                        return;
                    }
                    return;
                case 6:
                    mb.n("SharkProtocolQueue", "[shark_vip] handle: MSG_RESET_VIP_RULE, expired VipRule: " + nz.this.Gr);
                    nz.this.Gr = null;
                    mb.n("SharkProtocolQueue", "[shark_vip] triggle MSG_SEND_SHARK on VipRule expired ");
                    if (nu.Ev) {
                        nz.this.vH.sendEmptyMessage(1);
                        return;
                    }
                    return;
                case 7:
                    mb.n("SharkProtocolQueue", "[cu_guid]handle: MSG_REQUEST_REG_GUID");
                    ni.x(TMSDKContext.getApplicaionContext());
                    return;
                default:
                    return;
            }
        }
    };

    private final class a extends if {
        private boolean vL;

        private a() {
        }

        /* synthetic */ a(nz nzVar, AnonymousClass1 anonymousClass1) {
            this();
        }

        private void k(Context context) {
            if (!this.vL) {
                try {
                    String packageName = context.getPackageName();
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(String.format("action.guid.got:%s", new Object[]{packageName}));
                    intentFilter.addAction(String.format("action.rsa.got:%s", new Object[]{packageName}));
                    intentFilter.addAction(String.format("action.reg.guid:%s", new Object[]{packageName}));
                    intentFilter.addAction(String.format("action.up.rsa:%s", new Object[]{packageName}));
                    intentFilter.addAction(String.format("action.d.a:%s", new Object[]{packageName}));
                    context.registerReceiver(this, intentFilter);
                    this.vL = true;
                } catch (Throwable th) {
                    mb.b("SharkProtocolQueue", "[cu_guid] register: " + th, th);
                    th.printStackTrace();
                }
            }
        }

        public void doOnRecv(final Context context, final Intent intent) {
            final String action = intent.getAction();
            ((ki) fj.D(4)).addTask(new Runnable() {
                public void run() {
                    String packageName = context.getPackageName();
                    String format = String.format("action.guid.got:%s", new Object[]{packageName});
                    String format2 = String.format("action.rsa.got:%s", new Object[]{packageName});
                    String format3 = String.format("action.reg.guid:%s", new Object[]{packageName});
                    String format4 = String.format("action.up.rsa:%s", new Object[]{packageName});
                    String format5 = String.format("action.d.a:%s", new Object[]{packageName});
                    int intExtra;
                    if (format.equals(action)) {
                        nz.this.vH.removeMessages(7);
                        intExtra = intent.getIntExtra("k.rc", -1);
                        String stringExtra = intent.getStringExtra("k.g");
                        if (intExtra == 0 && !nu.aB()) {
                            mb.n("SharkProtocolQueue", "[cu_guid] doOnRecv(), !sendProcess, refreshGuid on recv broadcast");
                            nz.this.N(true);
                        }
                        mb.n("SharkProtocolQueue", "[cu_guid] doOnRecv(), notifyGuidGot on recv broadcast: " + action);
                        nz.this.i(intExtra, stringExtra);
                    } else if (format2.equals(action)) {
                        intExtra = intent.getIntExtra("k.rc", -1);
                        tmsdkobf.nq.b bVar = null;
                        if (intExtra == 0) {
                            bVar = new tmsdkobf.nq.b();
                            bVar.DX = intent.getStringExtra("k.r.k");
                            bVar.DW = intent.getStringExtra("k.r.s");
                            if (!nu.aB()) {
                                mb.n("SharkProtocolQueue", "[rsa_key] doOnRecv(), !sendProcess, refreshRsaKey on recv broadcast");
                                nz.this.N(false);
                            }
                        }
                        mb.n("SharkProtocolQueue", "[rsa_key] doOnRecv(), notifyRsaKeyGot on recv broadcast: " + action);
                        nz.this.b(intExtra, bVar);
                    } else if (format3.equals(action)) {
                        if (nu.aB()) {
                            mb.n("SharkProtocolQueue", "[rsa_key] doOnRecv(), triggerRegGuid on recv broadcast: " + action);
                            nz.this.gh();
                        }
                    } else if (format4.equals(action)) {
                        if (nu.aB()) {
                            mb.n("SharkProtocolQueue", "[rsa_key] doOnRecv(), triggerUpdateRsaKey on recv broadcast: " + action);
                            nz.this.gz();
                        }
                    } else if (format5.equals(action) && nu.aB()) {
                        try {
                            intExtra = intent.getIntExtra("k.sa", 0);
                            if (intExtra == 1) {
                                Bundle extras = intent.getExtras();
                                nz.this.a((kj) extras.getSerializable("v.r"), extras.getLong("vt.m", 35000));
                            } else if (intExtra == 2) {
                                nz.this.gx();
                            }
                        } catch (Throwable th) {
                            mb.b("SharkProtocolQueue", "[shark_vip] doOnRecv(), setVipRule: " + th, th);
                        }
                    }
                }
            }, "GuidOrRsaKeyGotReceiver onRecv");
        }
    }

    private class b {
        long GT;
        ce GU;
        byte[] GV;
        int GW = 0;
        long ex;

        public b(int i, long j, long j2, ce ceVar, byte[] bArr) {
            this.GW = i;
            this.GT = j;
            this.ex = j2;
            this.GU = ceVar;
            this.GV = bArr;
        }
    }

    public static class c {
        public boolean GX;
        public long mr;

        public c(boolean z, long j) {
            this.GX = z;
            this.mr = j;
        }
    }

    private class d {
        public int Ec;
        public int FM;
        public long Fv = System.currentTimeMillis();
        public byte[] GY;
        public byte[] GZ;
        public int Gc;
        public int Gf;
        public long Gg;
        public JceStruct Gh;
        public JceStruct Gi;
        public int Gj;
        public jy Gk;
        public jz Ha;
        public int Hb;
        public kd Hc;
        public int eB;
        public long ex;
        public long ov = -1;
        public long ow = 0;

        d(int i, int i2, long j, int i3, JceStruct jceStruct, byte[] bArr, JceStruct jceStruct2, int i4, jy jyVar, jz jzVar) {
            this.FM = i;
            this.Gc = i2;
            this.Gg = j;
            this.Gf = i3;
            this.Gh = jceStruct;
            this.GY = bArr;
            this.Gi = jceStruct2;
            this.Gj = i4;
            this.Gk = jyVar;
            this.Ha = jzVar;
            this.Hc = new kd();
        }

        public boolean gp() {
            boolean z = true;
            long abs = Math.abs(System.currentTimeMillis() - this.Fv);
            if (abs < (!((this.ov > 0 ? 1 : (this.ov == 0 ? 0 : -1)) <= 0) ? this.ov : 180000)) {
                z = false;
            }
            if (z) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("cmdId|").append(this.Gf);
                stringBuilder.append("|mIpcSeqNo|").append(this.Gc);
                stringBuilder.append("|mSeqNo|").append(this.Ec);
                stringBuilder.append("|pushId|").append(this.ex);
                stringBuilder.append("|mCallerIdent|").append(this.Gg);
                stringBuilder.append("|callBackTimeout|").append(this.ov);
                stringBuilder.append("|time(s)|").append(abs / 1000);
                nv.c("ocean", "[ocean][time_out]SharkProtocolQueue.SharkSendTask.isTimeOut(), " + stringBuilder.toString(), null, null);
            }
            return z;
        }
    }

    private class e implements Runnable {
        private TreeMap<Integer, d> Hd;
        private ArrayList<d> He;
        private Handler Hf;
        private Handler Hg;

        private e() {
            this.Hd = new TreeMap();
            this.He = new ArrayList();
            this.Hf = new Handler(nu.getLooper()) {
                public void handleMessage(Message message) {
                    of ofVar = (of) message.obj;
                    ce ceVar = new ce();
                    ceVar.eB = -11050000;
                    ceVar.ez = message.what;
                    if (ofVar != null) {
                        ceVar.bz = ofVar.ii;
                    }
                    mb.o("SharkProtocolQueue", "接收超时：seq: " + ceVar.ez + " cmdId: " + ceVar.bz);
                    e.this.d(ceVar);
                }
            };
            this.Hg = new Handler(nu.getLooper()) {
                public void handleMessage(Message message) {
                    switch (message.what) {
                        case 1:
                            ce ceVar = new ce();
                            ceVar.eB = -10000017;
                            ceVar.ez = message.arg1;
                            ceVar.bz = message.arg2;
                            mb.o("SharkProtocolQueue", "[time_out]发送请求超时： seq: " + ceVar.ez + " cmdId: " + ceVar.bz);
                            e.this.d(ceVar);
                            return;
                        default:
                            return;
                    }
                }
            };
        }

        /* synthetic */ e(nz nzVar, AnonymousClass1 anonymousClass1) {
            this();
        }

        private void a(ce ceVar, d dVar, Integer num, Integer num2, Integer num3) {
            dVar.Hc.setState(2);
            final int bj = ne.bj(num2.intValue());
            if (ceVar != null) {
                nt.ga().a("SharkProtocolQueue", num.intValue(), ceVar.ez, ceVar, 30, bj);
                nt.ga().bq(ceVar.ez);
            } else {
                nt.ga().a("SharkProtocolQueue", num.intValue(), dVar.Ec, ceVar, 30, bj);
                nt.ga().bq(dVar.Ec);
            }
            if (dVar.Gk != null || dVar.Ha != null) {
                switch (kc.al(dVar.Gj)) {
                    case 8:
                        nz.this.FR.sendMessage(nz.this.FR.obtainMessage(11, new Object[]{dVar, num, Integer.valueOf(bj), num3}));
                        break;
                    case 16:
                        if (dVar.Ha == null || dVar.Gc <= 0) {
                            dVar.Gk.onFinish(dVar.Ec, num.intValue(), bj, num3.intValue(), dVar.Gi);
                            break;
                        } else {
                            dVar.Ha.a(dVar.FM, dVar.Gc, dVar.Ec, num.intValue(), bj, num3.intValue(), dVar.GZ);
                            break;
                        }
                        break;
                    default:
                        final d dVar2 = dVar;
                        final Integer num4 = num;
                        final Integer num5 = num3;
                        Runnable anonymousClass7 = new Runnable() {
                            public void run() {
                                if (dVar2.Ha != null && dVar2.Gc > 0) {
                                    dVar2.Ha.a(dVar2.FM, dVar2.Gc, dVar2.Ec, num4.intValue(), bj, num5.intValue(), dVar2.GZ);
                                } else {
                                    dVar2.Gk.onFinish(dVar2.Ec, num4.intValue(), bj, num5.intValue(), dVar2.Gi);
                                }
                            }
                        };
                        if (num.intValue() != 2016 && num.intValue() != 12016) {
                            ((ki) fj.D(4)).addTask(anonymousClass7, "shark callback");
                            break;
                        } else {
                            ((ki) fj.D(4)).a(anonymousClass7, "shark callback(urgent)");
                            break;
                        }
                        break;
                }
            }
        }

        private void b(boolean z, int i, int i2, ArrayList<ce> arrayList) {
            if (i != 0) {
                by(i);
                return;
            }
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ce ceVar = (ce) it.next();
                if (bx(ceVar.ez)) {
                    d(ceVar);
                } else if (nz.b(ceVar)) {
                    nz.this.a(z, i2, ceVar);
                } else if (nz.c(ceVar)) {
                    nz.this.b(z, i2, ceVar);
                } else {
                    mb.s("SharkProtocolQueue", "No callback xx: cmd : " + ceVar.bz + " seqNo : " + ceVar.ey + " refSeqNo : " + ceVar.ez);
                }
            }
        }

        private void by(int i) {
            Set<Entry> gw = gw();
            synchronized (this.Hd) {
                this.Hd.clear();
            }
            for (Entry entry : gw) {
                try {
                    a(null, (d) entry.getValue(), Integer.valueOf(((d) entry.getValue()).Gf), Integer.valueOf(i), Integer.valueOf(-1));
                } catch (Throwable e) {
                    mb.a("SharkProtocolQueue", "callback crash", e);
                }
            }
        }

        /* JADX WARNING: Missing block: B:8:0x002d, code:
            if (r13.data != null) goto L_0x0055;
     */
        /* JADX WARNING: Missing block: B:23:0x0057, code:
            if (r13.eB != 0) goto L_0x002f;
     */
        /* JADX WARNING: Missing block: B:26:0x005b, code:
            if (r9.Ha != null) goto L_0x00a4;
     */
        /* JADX WARNING: Missing block: B:27:0x005d, code:
            r7 = tmsdkobf.nh.a(tmsdkobf.nz.u(r12.GM), tmsdkobf.nz.v(r12.GM).ap().DX.getBytes(), r13.data, r9.Gi, false, r13.eE);
     */
        /* JADX WARNING: Missing block: B:28:0x007e, code:
            if (r7 != null) goto L_0x002f;
     */
        /* JADX WARNING: Missing block: B:29:0x0080, code:
            if (r8 != null) goto L_0x002f;
     */
        /* JADX WARNING: Missing block: B:31:0x0084, code:
            if (r9.Gi == null) goto L_0x002f;
     */
        /* JADX WARNING: Missing block: B:32:0x0086, code:
            r13.eB = tmsdkobf.ne.bj(-11000300);
     */
        /* JADX WARNING: Missing block: B:33:0x0090, code:
            r10 = move-exception;
     */
        /* JADX WARNING: Missing block: B:34:0x0091, code:
            tmsdkobf.mb.a("SharkProtocolQueue", "sashimi decode fail", r10);
            r13.eB = tmsdkobf.ne.bj(-11000900);
     */
        /* JADX WARNING: Missing block: B:37:0x00a6, code:
            if (r9.Gc <= 0) goto L_0x005d;
     */
        /* JADX WARNING: Missing block: B:38:0x00a8, code:
            r8 = tmsdkobf.nh.a(tmsdkobf.nz.u(r12.GM), tmsdkobf.nz.v(r12.GM).ap().DX.getBytes(), r13.data, r13.eE);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void d(ce ceVar) {
            d dVar;
            JceStruct jceStruct = null;
            byte[] bArr = null;
            this.Hf.removeMessages(ceVar.ez);
            synchronized (this.Hd) {
                dVar = (d) this.Hd.get(Integer.valueOf(ceVar.ez));
                if (dVar != null) {
                    this.Hd.remove(Integer.valueOf(ceVar.ez));
                } else {
                    return;
                }
            }
            if (dVar.GZ != bArr) {
                dVar.GZ = bArr;
            }
            if (dVar.Gi != jceStruct) {
                dVar.Gi = jceStruct;
            }
            try {
                a(ceVar, dVar, Integer.valueOf(ceVar.bz), Integer.valueOf(ceVar.eB), Integer.valueOf(ceVar.eC));
            } catch (Throwable e) {
                mb.b("SharkProtocolQueue", "callback crash", e);
            }
        }

        public void a(Integer num, d dVar) {
            this.Hd.put(num, dVar);
        }

        public boolean bx(int i) {
            boolean containsKey;
            synchronized (this.Hd) {
                containsKey = this.Hd.containsKey(Integer.valueOf(i));
            }
            return containsKey;
        }

        public Set<Entry<Integer, d>> gw() {
            TreeMap treeMap;
            synchronized (this.Hd) {
                treeMap = (TreeMap) this.Hd.clone();
            }
            return treeMap.entrySet();
        }

        public void run() {
            long j = 0;
            try {
                bw bwVar;
                ArrayList arrayList = new ArrayList();
                ArrayList arrayList2 = new ArrayList();
                ArrayList arrayList3 = new ArrayList();
                ArrayList arrayList4 = new ArrayList();
                for (Entry entry : gw()) {
                    if (!((d) entry.getValue()).Hc.cJ()) {
                        if (((d) entry.getValue()).gp()) {
                            this.Hg.obtainMessage(1, ((d) entry.getValue()).Ec, ((d) entry.getValue()).Gf).sendToTarget();
                        } else {
                            ((d) entry.getValue()).Hc.setState(1);
                            bwVar = new bw();
                            bwVar.bz = ((d) entry.getValue()).Gf;
                            bwVar.ey = ((d) entry.getValue()).Ec;
                            bwVar.eA = ((d) entry.getValue()).Gg;
                            bwVar.ez = 0;
                            bwVar.data = null;
                            if (((d) entry.getValue()).GY == null) {
                                bwVar.data = nh.a(nz.this.mContext, ((d) entry.getValue()).Gh, bwVar.bz, bwVar);
                            } else {
                                bwVar.data = nh.a(nz.this.mContext, ((d) entry.getValue()).GY, bwVar.bz, bwVar);
                            }
                            long j2 = ((d) entry.getValue()).ov;
                            if ((j2 > 0 ? 1 : null) == null) {
                                j2 = 180000;
                            }
                            mb.r("SharkProtocolQueue", "[shark_timer]对seq: " + bwVar.ey + "计时(ms): " + j2);
                            this.Hf.sendMessageDelayed(Message.obtain(this.Hf, bwVar.ey, new of(bwVar.bz)), j2);
                            if ((((d) entry.getValue()).Gj & 2048) != 0) {
                                arrayList.add(bwVar);
                            } else if ((((d) entry.getValue()).Gj & 512) != 0) {
                                arrayList2.add(bwVar);
                            } else if ((((d) entry.getValue()).Gj & IncomingSmsFilterConsts.PAY_SMS) == 0) {
                                arrayList4.add(bwVar);
                            } else {
                                arrayList3.add(bwVar);
                            }
                            nt.ga().a("SharkProtocolQueue", bwVar.bz, bwVar.ey, bwVar, 0);
                            if ((((d) entry.getValue()).ow <= j ? 1 : null) == null) {
                                j = ((d) entry.getValue()).ow;
                            }
                        }
                    }
                }
                Iterator it = this.He.iterator();
                while (it.hasNext()) {
                    d dVar = (d) it.next();
                    if (!dVar.gp()) {
                        bwVar = new bw();
                        bwVar.bz = dVar.Gf;
                        bwVar.ey = ns.fW().fP();
                        bwVar.ez = dVar.Ec;
                        bwVar.data = null;
                        bwVar.eB = dVar.eB;
                        bwVar.eC = dVar.Hb;
                        bv bvVar = new bv();
                        bvVar.ex = dVar.ex;
                        bwVar.eD = bvVar;
                        mb.n("SharkProtocolQueue", "resp push, seqNo: " + bwVar.ey + " pushId: " + dVar.ex);
                        try {
                            if (dVar.GY == null) {
                                bwVar.data = nh.a(nz.this.mContext, dVar.Gh, bwVar.bz, bwVar);
                            } else {
                                bwVar.data = nh.a(nz.this.mContext, dVar.GY, bwVar.bz, bwVar);
                            }
                        } catch (Exception e) {
                        }
                        if ((dVar.Gj & 2048) != 0) {
                            arrayList.add(bwVar);
                        } else if ((dVar.Gj & 512) != 0) {
                            arrayList2.add(bwVar);
                        } else if ((dVar.Gj & IncomingSmsFilterConsts.PAY_SMS) == 0) {
                            arrayList4.add(bwVar);
                        } else {
                            arrayList3.add(bwVar);
                        }
                        nt.ga().a("SharkProtocolQueue", bwVar.bz, bwVar.ey, bwVar, 0);
                    } else if (dVar.Gf != 1103) {
                        mb.o("SharkProtocolQueue", "[time_out]发送push的自动回包超时： mSeqNo: " + dVar.Ec + " pushId: " + dVar.ex + " mCmdId: " + dVar.Gf);
                    } else {
                        mb.o("SharkProtocolQueue", "[time_out]发送push的业务回包超时： mSeqNo: " + dVar.Ec + " pushId: " + dVar.ex);
                    }
                }
                if (arrayList.size() > 0) {
                    nz.this.Dm.a(2048, j, true, arrayList, new tmsdkobf.nw.b() {
                        public void a(boolean z, int i, int i2, ArrayList<ce> arrayList) {
                            e.this.b(z, i, i2, arrayList);
                        }
                    });
                }
                if (arrayList2.size() > 0) {
                    nz.this.Dm.a(512, j, true, arrayList2, new tmsdkobf.nw.b() {
                        public void a(boolean z, int i, int i2, ArrayList<ce> arrayList) {
                            e.this.b(z, i, i2, arrayList);
                        }
                    });
                }
                if (arrayList3.size() > 0) {
                    nz.this.Dm.a((int) IncomingSmsFilterConsts.PAY_SMS, j, true, arrayList3, new tmsdkobf.nw.b() {
                        public void a(boolean z, int i, int i2, ArrayList<ce> arrayList) {
                            e.this.b(z, i, i2, arrayList);
                        }
                    });
                }
                if (arrayList4.size() > 0) {
                    nz.this.Dm.a(0, j, true, arrayList4, new tmsdkobf.nw.b() {
                        public void a(boolean z, int i, int i2, ArrayList<ce> arrayList) {
                            e.this.b(z, i, i2, arrayList);
                        }
                    });
                }
            } catch (Throwable e2) {
                mb.c("SharkProtocolQueue", "run shark task e: " + e2.toString(), e2);
                by(-10001200);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x00fd A:{SYNTHETIC, Splitter: B:22:0x00fd} */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x0017 A:{Catch:{ Exception -> 0x0170 }} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0113 A:{Catch:{ Exception -> 0x0170 }} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002d A:{Catch:{ Exception -> 0x0170 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void a(long j, ce ceVar, byte[] bArr, oh<JceStruct, ka, c> ohVar) {
        byte[] a;
        Throwable e;
        oh a2;
        JceStruct jceStruct = null;
        if (ceVar.data != null) {
            if (((c) ohVar.Il).GX) {
                try {
                    a = nh.a(this.mContext, bArr, ceVar.data, ceVar.eE);
                } catch (Throwable e2) {
                    mb.b("SharkProtocolQueue", "[shark_push]handleCallbackForPush(), dataForReceive2JceBytes exception: " + e2, e2);
                    a(ceVar.ey, j, ceVar.bz, null, null, -1);
                }
                if (((c) ohVar.Il).GX) {
                    a2 = ((ka) ohVar.second).a(ceVar.ey, j, ceVar.bz, jceStruct);
                } else {
                    a2 = ((kb) ohVar.second).a(ceVar.ey, j, ceVar.bz, a);
                }
                if (a2 != null) {
                    mb.n("SharkProtocolQueue", "[shark_push]handleCallbackForPush(), donot send PushStatus for user: |pushId|" + j + "|cmd|" + ceVar.bz);
                    return;
                }
                mb.n("SharkProtocolQueue", "[shark_push]handleCallbackForPush(), send PushStatus for user: |pushId|" + j + "|cmd|" + a2.second + "|JceStruct|" + a2.Il);
                a(ceVar.ey, j, ((Integer) a2.second).intValue(), (JceStruct) a2.Il, null, 1);
                return;
            } else if (ohVar.first != null) {
                try {
                    jceStruct = nh.a(this.mContext, bArr, ceVar.data, (JceStruct) ohVar.first, true, ceVar.eE);
                } catch (Throwable e22) {
                    mb.b("SharkProtocolQueue", "[shark_push]handleCallbackForPush(), dataForReceive2JceStruct exception: " + e22, e22);
                    a(ceVar.ey, j, ceVar.bz, null, null, -1);
                }
            }
        }
        a = null;
        try {
            if (((c) ohVar.Il).GX) {
            }
            if (a2 != null) {
            }
        } catch (Throwable e3) {
            mb.b("SharkProtocolQueue", "[shark_push]handleCallbackForPush(), callback exception: " + e3, e3);
            e22 = e3;
        }
    }

    public static boolean a(ce ceVar) {
        boolean z = false;
        if (ceVar == null) {
            return false;
        }
        if (ceVar.ez != 0) {
            z = true;
        }
        return z;
    }

    private void b(int i, tmsdkobf.nq.b bVar) {
        List<a> arrayList = new ArrayList();
        synchronized (this.GK) {
            if (this.GK.size() > 0) {
                arrayList.addAll(this.GK);
            }
        }
        if (arrayList.size() > 0) {
            for (a a : arrayList) {
                a.a(i, bVar);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x00ba A:{SYNTHETIC, Splitter: B:21:0x00ba} */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x0014 A:{Catch:{ Exception -> 0x00e7 }} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00d0 A:{Catch:{ Exception -> 0x00e7 }} */
    /* JADX WARNING: Removed duplicated region for block: B:26:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void b(long j, ce ceVar, byte[] bArr, oh<JceStruct, ka, c> ohVar) {
        byte[] a;
        oh a2;
        JceStruct jceStruct = null;
        if (ceVar.data != null) {
            if (((c) ohVar.Il).GX) {
                try {
                    a = nh.a(this.mContext, this.Dm.ap().DX.getBytes(), ceVar.data, ceVar.eE);
                } catch (Throwable e) {
                    mb.b("SharkProtocolQueue", "[shark_push]handleCallbackForGift(), dataForReceive2JceBytes exception: " + e, e);
                }
                if (((c) ohVar.Il).GX) {
                    a2 = ((ka) ohVar.second).a(ceVar.ey, j, ceVar.bz, jceStruct);
                } else {
                    a2 = ((kb) ohVar.second).a(ceVar.ey, j, ceVar.bz, a);
                }
                if (a2 == null) {
                    c(ceVar.ey, ((Integer) a2.second).intValue(), (JceStruct) a2.Il);
                    return;
                }
                return;
            } else if (ohVar.first != null) {
                try {
                    jceStruct = nh.a(this.mContext, this.Dm.ap().DX.getBytes(), ceVar.data, (JceStruct) ohVar.first, true, ceVar.eE);
                } catch (Throwable e2) {
                    mb.b("SharkProtocolQueue", "[shark_push]handleCallbackForGift(), dataForReceive2JceStruct exception: " + e2, e2);
                }
            }
        }
        a = null;
        try {
            if (((c) ohVar.Il).GX) {
            }
            if (a2 == null) {
            }
        } catch (Throwable e3) {
            mb.b("SharkProtocolQueue", "[shark_push]handleCallbackForGift(), callback exception: " + e3, e3);
        }
    }

    public static boolean b(ce ceVar) {
        return (ceVar == null || ceVar.ez != 0 || ceVar.eO == null || ceVar.eO.ex == 0) ? false : true;
    }

    public static boolean c(ce ceVar) {
        boolean z = false;
        if (ceVar == null) {
            return false;
        }
        if (!(a(ceVar) || b(ceVar))) {
            z = true;
        }
        return z;
    }

    private void i(int i, String str) {
        List<jw> arrayList = new ArrayList();
        synchronized (this.GI) {
            if (this.GI.size() > 0) {
                arrayList.addAll(this.GI);
                this.GI.clear();
            }
        }
        synchronized (this.GJ) {
            if (this.GJ.size() > 0) {
                arrayList.addAll(this.GJ);
            }
        }
        if (arrayList.size() > 0) {
            for (jw c : arrayList) {
                c.c(i, str);
            }
        }
    }

    public void N(boolean z) {
        if (z) {
            if (nu.Ev) {
                this.Dm.fC();
            } else {
                this.Gt = true;
            }
        } else if (nu.Ev) {
            this.Dm.gk();
        } else {
            this.Gs = true;
        }
    }

    public long a(boolean z, int i, ce ceVar) {
        if (ceVar == null || !b(ceVar)) {
            return -1;
        }
        long j = 0;
        if (ceVar.eO != null) {
            j = ceVar.eO.ex;
        }
        mb.d("SharkProtocolQueue", "[shark_push]onPush(), ECmd: " + ceVar.bz + " seqNo: " + ceVar.ey + " pushId: " + j + " isTcpChannel: " + z);
        a(j, ceVar.bz, i, ceVar.ey, -1000000001);
        if (ceVar.eB != 0) {
            mb.o("SharkProtocolQueue", "[shark_push]onPush(), push with error, drop it, ECmd: " + ceVar.bz + " seqNo: " + ceVar.ey + " pushId: " + j + " isTcpChannel: " + z + " retCode: " + ceVar.eB);
            return -1;
        } else if (this.Gq.d(Long.valueOf(j))) {
            mb.s("SharkProtocolQueue", "[shark_push]onPush(), push duplicate, drop it, ECmd: " + ceVar.bz + " seqNo: " + ceVar.ey + " pushId: " + j);
            return -1;
        } else {
            oh ohVar;
            this.Gq.push(Long.valueOf(j));
            synchronized (this.FQ) {
                ohVar = (oh) this.FQ.get(Integer.valueOf(ceVar.bz));
            }
            if (ohVar != null) {
                mb.d("SharkProtocolQueue", "[shark_push]onPush(), someone listen to it, callback now, ECmd: " + ceVar.bz + " seqNo: " + ceVar.ey + " pushId: " + j);
                a(j, ceVar, this.Dm.ap().DX.getBytes(), ohVar);
                return ohVar.Il == null ? -1 : ((c) ohVar.Il).mr;
            } else {
                int size;
                synchronized (this.Go) {
                    this.Go.add(new b(0, System.currentTimeMillis(), j, ceVar, this.Dm.ap().DX.getBytes()));
                    size = this.Go.size();
                }
                mb.s("SharkProtocolQueue", "[shark_push]onPush(), nobody listen to it, ECmd: " + ceVar.bz + " seqNo: " + ceVar.ey + " pushId: " + j + " cache for " + 600 + "s" + " pushSize: " + size);
                this.vH.removeMessages(3);
                if (size < 20) {
                    this.vH.sendEmptyMessageDelayed(3, 600000);
                } else {
                    this.vH.sendEmptyMessageDelayed(3, 2000);
                    this.vH.sendEmptyMessageDelayed(3, 600000);
                }
                return -1;
            }
        }
    }

    public WeakReference<kd> a(int i, long j, int i2, JceStruct jceStruct, byte[] bArr, int i3) {
        return a(i, j, i2, jceStruct, bArr, i3, 0);
    }

    public WeakReference<kd> a(int i, long j, int i2, JceStruct jceStruct, byte[] bArr, int i3, int i4) {
        nv.z("SharkProtocolQueue", "[shark_push]sendPushResp(), pushSeqNo: " + i + " pushId: " + j + " cmdId: " + i2 + " result: " + i3 + " retCode: " + i4);
        JceStruct anVar = new an();
        anVar.bz = i2;
        anVar.status = i3;
        if (bArr != null && bArr.length > 0) {
            anVar.bA = bArr;
        } else if (jceStruct != null) {
            anVar.bA = nn.d(jceStruct);
        }
        d dVar = new d(0, 0, -1, 1103, jceStruct, nh.b(anVar), null, 1073741824, null, null);
        dVar.Ec = i;
        dVar.ex = j;
        dVar.eB = i4;
        synchronized (this.Gp) {
            this.Gp.add(dVar);
        }
        if (nu.Ev) {
            this.vH.sendEmptyMessage(1);
        }
        return new WeakReference(dVar.Hc);
    }

    public void a(long j, int i, int i2, int i3, int i4) {
        nv.z("SharkProtocolQueue", "autoReplyPush()  pushId: " + j + " cmdId: " + i + " serverSharkSeqNo: " + i2 + " serverSashimiSeqNo: " + i3 + " errCode: " + i4);
        d dVar = new d(Process.myPid(), 0, 0, i, null, new byte[0], null, 1073741824, null, null);
        dVar.eB = i4;
        dVar.Ec = i3;
        dVar.ex = j;
        synchronized (this.Gp) {
            this.Gp.add(dVar);
        }
        if (nu.Ev) {
            this.vH.sendEmptyMessage(1);
        }
    }

    public void a(long j, int i, JceStruct jceStruct, int i2, ka kaVar, boolean z) {
        if (kaVar != null) {
            synchronized (this.FQ) {
                this.FQ.put(Integer.valueOf(i), new oh(jceStruct, kaVar, new c(z, j)));
            }
            mb.d("SharkProtocolQueue", "[shark_push]registerSharkPush(), for cmd: " + i);
            if (nu.Ev) {
                this.vH.obtainMessage(5, i, 0).sendToTarget();
            }
        }
    }

    public void a(jw jwVar) {
        if (jwVar != null) {
            synchronized (this.GJ) {
                if (!this.GJ.contains(jwVar)) {
                    this.GJ.add(jwVar);
                }
            }
        }
    }

    public void a(kj kjVar, long j) {
        if (kjVar != null) {
            mb.n("SharkProtocolQueue", "[shark_vip] setVipRule(): " + kjVar + ", valid time(ms): " + j);
            this.Gr = kjVar;
            this.vH.removeMessages(6);
            if ((j <= 0 ? 1 : null) == null) {
                this.vH.sendEmptyMessageDelayed(6, j);
            }
        }
    }

    public void a(a aVar) {
        if (aVar != null) {
            synchronized (this.GK) {
                if (!this.GK.contains(aVar)) {
                    this.GK.add(aVar);
                }
            }
        }
    }

    public long b(boolean z, int i, ce ceVar) {
        if (ceVar == null || !c(ceVar)) {
            return -1;
        }
        mb.d("SharkProtocolQueue", "[shark_push]onGotGift(), ECmd: " + ceVar.bz + " seqNo: " + ceVar.ey + " pushId: " + 0 + " isTcpChannel: " + z);
        if (ceVar.eB == 0) {
            oh ohVar;
            synchronized (this.FQ) {
                ohVar = (oh) this.FQ.get(Integer.valueOf(ceVar.bz));
            }
            if (ohVar != null) {
                mb.d("SharkProtocolQueue", "[shark_push]onGotGift(), someone listen to it, callback now, ECmd: " + ceVar.bz + " seqNo: " + ceVar.ey);
                b(0, ceVar, this.Dm.ap().DX.getBytes(), ohVar);
                return ohVar.Il == null ? -1 : ((c) ohVar.Il).mr;
            } else {
                int size;
                synchronized (this.Go) {
                    this.Go.add(new b(1, System.currentTimeMillis(), 0, ceVar, this.Dm.ap().DX.getBytes()));
                    size = this.Go.size();
                }
                mb.s("SharkProtocolQueue", "[shark_push]onGotGift(), nobody listen to it, ECmd: " + ceVar.bz + " seqNo: " + ceVar.ey + " cache for " + 600 + "s" + " pushSize: " + size);
                this.vH.removeMessages(3);
                if (size < 20) {
                    this.vH.sendEmptyMessageDelayed(3, 600000);
                } else {
                    this.vH.sendEmptyMessageDelayed(3, 2000);
                    this.vH.sendEmptyMessageDelayed(3, 600000);
                }
                return -1;
            }
        }
        mb.o("SharkProtocolQueue", "[shark_push]onGotGift(), gift with error, drop it, ECmd: " + ceVar.bz + " seqNo: " + ceVar.ey + " pushId: " + 0 + " isTcpChannel: " + z + " retCode: " + ceVar.eB);
        return -1;
    }

    public String b() {
        return this.Dm != null ? this.Dm.b() : "";
    }

    public void b(int i, int i2, int i3) {
        if (nu.Ev) {
            this.Dm.b(i, i2, i3);
            return;
        }
        if (this.GB == null) {
            this.GB = new LinkedList();
        }
        this.GB.add(new no(i, i2, i3));
    }

    public void b(jw jwVar) {
        if (jwVar != null) {
            synchronized (this.GJ) {
                if (this.GJ.contains(jwVar)) {
                    this.GJ.remove(jwVar);
                }
            }
        }
    }

    public void b(a aVar) {
        if (aVar != null) {
            synchronized (this.GK) {
                if (this.GK.contains(aVar)) {
                    this.GK.remove(aVar);
                }
            }
        }
    }

    public WeakReference<kd> c(int i, int i2, int i3, long j, long j2, int i4, JceStruct jceStruct, byte[] bArr, JceStruct jceStruct2, int i5, jy jyVar, jz jzVar, long j3, long j4) {
        mb.d("SharkProtocolQueue", "sendShark() cmdId: " + i4 + " pushSeqNo: " + i3);
        if (i3 > 0) {
            return a(i3, j, i4, jceStruct, bArr, 1);
        }
        d dVar = new d(i, i2, j2, i4, jceStruct, bArr, jceStruct2, i5, jyVar, jzVar);
        dVar.Ec = ns.fW().fP();
        dVar.ov = j3;
        dVar.ow = j4;
        synchronized (this.Gp) {
            this.Gp.add(dVar);
        }
        nt.ga().a(dVar.Ec, j3, null);
        if (nu.Ev) {
            this.vH.sendEmptyMessage(1);
        }
        return new WeakReference(dVar.Hc);
    }

    public WeakReference<kd> c(int i, final int i2, JceStruct jceStruct) {
        mb.n("SharkProtocolQueue", "[shark_push]sendGiftResp(): giftSeqNo: " + i + " acmdId: " + i2 + " respStruct: " + jceStruct);
        if (i2 == 156) {
            mb.r("SharkProtocolQueue", "[ip_list]sendGiftResp(): giftSeqNo: " + i + " acmdId: " + i2 + " respStruct: " + jceStruct);
        }
        return nu.gf().a(i2, jceStruct, null, 0, new jy() {
            public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                mb.n("SharkProtocolQueue", "[shark_push]sendGiftResp()-onFinish() seqNo: " + i + " cmdId: " + i2 + " retCode: " + i3 + " dataRetCode: " + i4);
                if (i2 == 156) {
                    mb.r("SharkProtocolQueue", "[ip_list]sendGiftResp()-onFinish() seqNo: " + i + " cmdId: " + i2 + " retCode: " + i3 + " dataRetCode: " + i4 + " resp: " + jceStruct);
                }
            }
        });
    }

    public void c(nl nlVar) {
        mb.d("SharkProtocolQueue", "[shark_init]initSync()");
        this.CT = nlVar;
        synchronized (this) {
            if (this.GL == null) {
                this.GL = new a(this, null);
                this.GL.k(this.mContext);
                mb.d("SharkProtocolQueue", "[shark_init][cu_guid][rsa_key] initSync(), register guid & rsakey event");
            }
        }
    }

    public void gA() {
        if (nu.aB()) {
            if (nu.Ev) {
                this.Dm.gj().gA();
            } else {
                this.Gx = true;
            }
        }
    }

    public void gB() {
        if (nu.Ev) {
            this.Dm.go();
        } else {
            this.GA = true;
        }
    }

    public void gC() {
        if (nu.aB()) {
            if (nu.Ev) {
                this.Dm.gj().gC();
            } else {
                this.GE = true;
            }
        }
    }

    public void gD() {
        if (nu.aB()) {
            if (nu.Ev) {
                this.Dm.gj().gD();
            } else {
                this.GF = true;
            }
        }
    }

    public int getSingletonType() {
        return 1;
    }

    public void gh() {
        if (nu.Ev) {
            this.Dm.gh();
        } else {
            this.Gv = true;
        }
    }

    public nl gl() {
        return this.CT;
    }

    public void gm() {
        if (nu.Ev) {
            this.Dm.gm();
        } else {
            this.Gy = true;
        }
    }

    public void gn() {
        if (nu.Ev) {
            this.Dm.gn();
        } else {
            this.Gz = true;
        }
    }

    void gx() {
        if (nu.Ev) {
            this.vH.removeMessages(4);
            this.vH.sendEmptyMessage(4);
            return;
        }
        this.GC = true;
    }

    public void gy() {
        mb.n("SharkProtocolQueue", "[shark_init]initAsync()");
        this.Dm = new nw(TMSDKContext.getApplicaionContext(), this.CT, this, this, this, nu.gc(), nu.gd());
        this.EO = Executors.newSingleThreadExecutor();
        nt.ga().b(this.CT);
        this.vH.sendEmptyMessage(2);
    }

    public void gz() {
        if (nu.Ev) {
            this.Dm.y(1000);
        } else {
            this.Gu = true;
        }
    }

    public void onCreate(Context context) {
        mb.d("SharkProtocolQueue", "[shark_init]onCreate()");
        this.mContext = context;
    }

    public void onReady() {
        if (!nu.Ev) {
            this.Gw = true;
        } else if (this.Dm != null) {
            this.Dm.onReady();
        }
    }

    public ka v(int i, int i2) {
        ka kaVar = null;
        synchronized (this.FQ) {
            if (this.FQ.containsKey(Integer.valueOf(i))) {
                kaVar = (ka) ((oh) this.FQ.remove(Integer.valueOf(i))).second;
            }
        }
        return kaVar;
    }
}
