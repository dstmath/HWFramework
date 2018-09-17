package tmsdkobf;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class nw implements tmsdkobf.og.d {
    private final Object DA = new Object();
    private nl Dz;
    private od EE;
    private nq EF;
    private ni EG;
    private oi EH;
    private e EI;
    private SparseArray<pf> EJ = null;
    private f EK = null;
    private f EL = null;
    private ArrayList<f> EM = new ArrayList();
    private LinkedHashMap<Integer, f> EN = new LinkedHashMap();
    private ExecutorService EO;
    private boolean EP;
    private long EQ = 0;
    private boolean ER = false;
    private boolean ES = false;
    private long ET = 0;
    private long EU = 0;
    private c EV;
    private tmsdkobf.od.a EW = new tmsdkobf.od.a() {
        public void a(boolean z, int i, byte[] bArr, f fVar) {
            mb.n("SharkNetwork", "onFinish(), retCode: " + i);
            if (i != 0) {
                nw.this.a(z, i, fVar);
            } else if (bArr != null) {
                mb.d("SharkNetwork", "onFinish() retData.length: " + bArr.length);
                int c;
                if (nu.t(bArr)) {
                    c = nt.ga().c(bArr[0]);
                    if (c >= 0) {
                        f fVar2;
                        synchronized (nw.this.EN) {
                            fVar2 = (f) nw.this.EN.get(Integer.valueOf(c));
                        }
                        if (fVar2 != null) {
                            nw.this.a(z, fVar2, 0, 0, null);
                        }
                    }
                } else {
                    try {
                        cf r = nn.r(bArr);
                        if (r != null) {
                            f b;
                            cf cfVar = r;
                            ArrayList arrayList = cfVar.eQ;
                            c = cfVar.ez;
                            if (nw.this.EK != null && nw.this.EK.Fq == c) {
                                b = nw.this.EK;
                            } else if (nw.this.EL != null && nw.this.EL.Fq == c) {
                                b = nw.this.EL;
                            } else {
                                synchronized (nw.this.EN) {
                                    b = (f) nw.this.EN.get(Integer.valueOf(c));
                                }
                            }
                            if (arrayList != null) {
                                int i2 = 0;
                                Iterator it = arrayList.iterator();
                                while (it.hasNext()) {
                                    ce ceVar = (ce) it.next();
                                    mb.n("SharkNetwork_CMDID", "[" + i2 + "]收包：cmd id:[" + ceVar.bz + "]seqNo:[" + ceVar.ey + "]refSeqNo:[" + ceVar.ez + "]retCode:[" + ceVar.eB + "]dataRetCode:[" + ceVar.eC + "]");
                                    i2++;
                                }
                            }
                            if (arrayList != null) {
                                nw.r(arrayList);
                                mb.d("SharkNetwork", "onFinish() sharkSeq: " + c + " ssTag: " + b + " shark回包或push个数: " + arrayList.size());
                                boolean a = nw.this.s(arrayList);
                                mb.n("SharkNetwork", "[rsa_key]onFinish() 密钥是否过期：" + (!a ? "否" : "是"));
                                if (a) {
                                    nw.this.EP = true;
                                    nw.this.vH.removeMessages(1);
                                    nw.this.vH.sendEmptyMessageDelayed(1, 100);
                                    return;
                                }
                                ArrayList a2 = nw.this.a(b, z, cfVar, arrayList);
                                if (a2 != null && a2.size() > 0) {
                                    Iterator it2 = a2.iterator();
                                    while (it2.hasNext()) {
                                        ce ceVar2 = (ce) it2.next();
                                        if (ceVar2 != null) {
                                            nt.ga().a("SharkNetwork", ceVar2.bz, ceVar2.ez, ceVar2, 17, i, bArr == null ? null : String.format("%d/%d", new Object[]{Integer.valueOf(bArr.length + 4), Integer.valueOf(arrayList.size())}));
                                            oe bC = oe.bC(ceVar2.ez);
                                            if (bC != null) {
                                                bC.HE = String.valueOf(nh.w(nw.this.mContext));
                                                bC.errorCode = ceVar2.eB;
                                                bC.bB(ceVar2.bz);
                                                bC.f(nw.this.Dz);
                                            }
                                        }
                                    }
                                }
                                nw.this.a(z, b, 0, cfVar.ey, a2);
                                nw.this.gi();
                            } else {
                                mb.o("SharkNetwork", "onFinish() null == respSashimiList");
                                nw.this.a(z, b, -21000005, cfVar.ey, null);
                                return;
                            }
                        }
                        mb.o("SharkNetwork", "onFinish() null == obj");
                        nw.this.a(z, -21000400, fVar);
                    } catch (Exception e) {
                        mb.o("SharkNetwork", "onFinish() e: " + e.toString());
                        nw.this.a(z, -21000400, fVar);
                    }
                }
            } else {
                mb.o("SharkNetwork", "onFinish() null == retData");
                nw.this.a(z, -21000005, fVar);
            }
        }

        public void b(boolean z, int i, f fVar) {
            if (fVar == null) {
                mb.o("SharkNetwork", "onSendFailed(), isTcpChannel: " + z + " retCode: " + i);
            } else {
                mb.o("SharkNetwork", "onSendFailed(), isTcpChannel: " + z + " retCode: " + i + " seqNo: " + fVar.Fq);
            }
            if (i != 0) {
                nw.this.a(z, i, fVar);
            }
        }
    };
    private boolean EX = true;
    private boolean EY = true;
    private long EZ = 0;
    private Handler Fa = new Handler(nu.getLooper()) {
        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case 1:
                    nw.this.b((f) message.obj);
                    return;
                default:
                    return;
            }
        }
    };
    private Context mContext;
    private Handler vH = new Handler(nu.getLooper()) {
        private void b(final boolean z, final f fVar) {
            nw.this.EO.submit(new Runnable() {
                public void run() {
                    ArrayList arrayList;
                    if (nw.this.EJ != null) {
                        if (!fVar.Fm) {
                            synchronized (nw.this.EJ) {
                                pf pfVar = (pf) nw.this.EJ.get(997);
                                if (pfVar != null) {
                                    if (!pfVar.hI()) {
                                        mb.s("SharkNetwork", "[network_control] cloud cmd: fp donot connect, use http channel");
                                        fVar.Fo = true;
                                    }
                                }
                            }
                        }
                        arrayList = fVar.Ft;
                        if (arrayList != null && arrayList.size() > 0) {
                            mb.n("SharkNetwork", "[network_control] before control, sashimis.size(): " + arrayList.size());
                            Iterator it = arrayList.iterator();
                            while (it.hasNext()) {
                                bw bwVar = (bw) it.next();
                                if (bwVar != null) {
                                    pf pfVar2;
                                    synchronized (nw.this.EJ) {
                                        pfVar2 = (pf) nw.this.EJ.get(bwVar.bz);
                                    }
                                    if (pfVar2 != null) {
                                        if (pfVar2.hI()) {
                                            pfVar2.hJ();
                                        } else {
                                            it.remove();
                                            nv.b("SharkNetwork", "network ctrl donot connect, cmdid : " + bwVar.bz, null, null);
                                            mb.s("SharkNetwork", "[network_control] cloud cmd: donot connect, cmdid : " + bwVar.bz);
                                            ce ceVar = new ce();
                                            ceVar.bz = bwVar.bz;
                                            ceVar.eB = -7;
                                            nw.this.a(true, false, fVar, -20000007, 0, ceVar);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    arrayList = fVar.Ft;
                    if (arrayList != null && arrayList.size() > 0) {
                        mb.n("SharkNetwork", "[network_control] after control, sashimis.size(): " + arrayList.size());
                        try {
                            nw.this.a(z, fVar);
                            return;
                        } catch (Exception e) {
                            mb.e("SharkNetwork", e);
                            return;
                        }
                    }
                    mb.s("SharkNetwork", "[network_control] no sashimi can connect, control by cloud cmd!");
                }
            });
        }

        /* JADX WARNING: Missing block: B:31:0x00d5, code:
            r3 = r2.iterator();
     */
        /* JADX WARNING: Missing block: B:33:0x00dd, code:
            if (r3.hasNext() == false) goto L_0x0008;
     */
        /* JADX WARNING: Missing block: B:34:0x00df, code:
            r4 = (tmsdkobf.nw.f) r3.next();
     */
        /* JADX WARNING: Missing block: B:35:0x00e5, code:
            if (r4 == null) goto L_0x00d9;
     */
        /* JADX WARNING: Missing block: B:37:0x00ed, code:
            if (tmsdkobf.nw.l(r9.Fb) != false) goto L_0x011a;
     */
        /* JADX WARNING: Missing block: B:39:0x00f1, code:
            if (r4.Fl != false) goto L_0x0129;
     */
        /* JADX WARNING: Missing block: B:40:0x00f3, code:
            b(true, r4);
     */
        /* JADX WARNING: Missing block: B:51:0x011a, code:
            tmsdkobf.mb.s("SharkNetwork", "[rsa_key] MSG_SHARK_SEND, rsakey expired suddenly, handleOnNeedRsaKey()");
            tmsdkobf.nw.b(r9.Fb, true);
     */
        /* JADX WARNING: Missing block: B:52:0x0128, code:
            return;
     */
        /* JADX WARNING: Missing block: B:53:0x0129, code:
            b(false, r4);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    nw.this.vH.removeMessages(0);
                    if (nw.this.EK == null || message.arg1 != 1) {
                        if (nw.this.EL == null || message.arg1 != 2) {
                            mb.o("SharkNetwork", "MSG_SHARK_SEND_VIP null");
                            break;
                        }
                        mb.n("SharkNetwork", "MSG_SHARK_SEND_VIP mSharkSendGuid");
                        b(true, nw.this.EL);
                        break;
                    }
                    mb.n("SharkNetwork", "MSG_SHARK_SEND_VIP mSharkSendRsa");
                    b(false, nw.this.EK);
                    break;
                    break;
                case 1:
                    mb.n("SharkNetwork", "MSG_SHARK_SEND");
                    nw.this.vH.removeMessages(1);
                    tmsdkobf.nq.b ap = nw.this.EF.ap();
                    if (TextUtils.isEmpty(ap.DW) || TextUtils.isEmpty(ap.DX)) {
                        mb.s("SharkNetwork", "[rsa_key] MSG_SHARK_SEND, without rsakey, handleOnNeedRsaKey()");
                        nw.this.M(false);
                        return;
                    } else if (nw.this.EP) {
                        mb.s("SharkNetwork", "[rsa_key] MSG_SHARK_SEND, rsakey expired, handleOnNeedRsaKey()");
                        nw.this.M(true);
                        return;
                    } else if (nw.this.EG.fB()) {
                        mb.s("SharkNetwork", "[cu_guid] MSG_SHARK_SEND, without guid, handleOnNeedGuid()");
                        nw.this.gg();
                        return;
                    } else {
                        synchronized (nw.this.EM) {
                            if (nw.this.EM.size() > 0) {
                                ArrayList arrayList = (ArrayList) nw.this.EM.clone();
                                nw.this.EM.clear();
                                break;
                            }
                            return;
                        }
                    }
                    break;
                case 2:
                    nw.this.vH.removeMessages(2);
                    mb.n("SharkNetwork", "[rsa_key]msg: MSG_SHARK_UPDATE_RSAKEY");
                    nw.this.EO.submit(new Runnable() {
                        public void run() {
                            int i;
                            boolean i2;
                            nw.this.vH.removeMessages(2);
                            if (nw.this.EQ <= 0) {
                                i2 = 1;
                            } else {
                                i2 = false;
                            }
                            if (i2 == 0) {
                                if (Math.abs(System.currentTimeMillis() - nw.this.EQ) > 60000) {
                                    i2 = 1;
                                } else {
                                    i2 = false;
                                }
                                if (i2 == 0) {
                                    mb.n("SharkNetwork", "[rsa_key]update rsa succ in 60s, no need to update now");
                                    synchronized (nw.this.DA) {
                                        nw.this.ER = false;
                                    }
                                    nw.this.EP = false;
                                    nw.this.vH.sendEmptyMessage(1);
                                    mb.n("SharkNetwork", "[rsa_key]update rsa succ in 60s, no need to update now, broadcast after 5s");
                                    nw.this.vH.removeMessages(5);
                                    nw.this.vH.sendEmptyMessageDelayed(5, 5000);
                                    return;
                                }
                            }
                            try {
                                nw.this.EF.a(new tmsdkobf.nq.a() {
                                    public void a(int i, int i2, int i3) {
                                        int bj = ne.bj(i3);
                                        mb.d("SharkNetwork", "[rsa_key]onUpdateFinish(), ret: " + bj);
                                        synchronized (nw.this.DA) {
                                            nw.this.ER = false;
                                        }
                                        nt.ga().a("SharkNetwork", i2, i, (ce) null, 30, bj);
                                        nt.ga().bq(i);
                                        if (bj != 0) {
                                            nq.a(nw.this.mContext, bj, null);
                                        } else {
                                            nq.a(nw.this.mContext, bj, nw.this.ap());
                                        }
                                        nw.this.bw(bj);
                                    }
                                });
                            } catch (Exception e) {
                                mb.o("SharkNetwork", "[rsa_key] MSG_SHARK_UPDATE_RSAKEY e: " + e.toString());
                                synchronized (nw.this.DA) {
                                    nw.this.ER = false;
                                    nw.this.bw(-20000014);
                                }
                            }
                        }
                    });
                    break;
                case 3:
                    mb.n("SharkNetwork", "[cu_guid]MSG_SHARK_GET_GUID");
                    nw.this.vH.removeMessages(3);
                    nw.this.EO.submit(new Runnable() {
                        public void run() {
                            nw.this.vH.removeMessages(3);
                            try {
                                nw.this.EG.a(new tmsdkobf.ni.a() {
                                    public void a(int i, int i2, int i3, String str) {
                                        int bj = ne.bj(i3);
                                        mb.d("SharkNetwork", "[cu_guid]onGuidFinish(), send broadcast, ret: " + bj);
                                        synchronized (nw.this.DA) {
                                            nw.this.ES = false;
                                        }
                                        nt.ga().a("SharkNetwork", i2, i, (ce) null, 30, bj);
                                        nt.ga().bq(i);
                                        ni.a(nw.this.mContext, bj, str);
                                        nw.this.bv(bj);
                                    }
                                });
                            } catch (Exception e) {
                                mb.o("SharkNetwork", "[cu_guid]register guid exception: " + e.toString());
                                synchronized (nw.this.DA) {
                                    nw.this.ES = false;
                                    nw.this.bv(-20000014);
                                }
                            }
                        }
                    });
                    break;
                case 4:
                    nw.this.EO.submit(new Runnable() {
                        public void run() {
                            if (nw.this.EG != null) {
                                mb.r("SharkNetwork", "[cu_guid]deal msg: guid info changed, check update guid");
                                nw.this.EG.a(true, null);
                            }
                        }
                    });
                    break;
                case 5:
                    nq.a(nw.this.mContext, 0, nw.this.ap());
                    break;
                case 6:
                    mb.n("SharkNetwork", "[cu_guid]handle: MSG_REQUEST_SENDPROCESS_GET_GUID");
                    ni.x(nw.this.mContext);
                    break;
                case 7:
                    mb.n("SharkNetwork", "[rsa_key]handle: MSG_REQUEST_SENDPROCESS_UPDATE_RSAKEY");
                    nq.y(nw.this.mContext);
                    break;
                case 8:
                    mb.n("SharkNetwork", "[cu_vid]deal msg: MSG_REGISTER_VID_IFNEED");
                    nw.this.EH.gB();
                    break;
                case 9:
                    mb.n("SharkNetwork", "[cu_vid]deal msg: MSG_UPDATE_VID_IFNEED");
                    nw.this.EH.c(0, false);
                    break;
            }
        }
    };

    public interface b {
        void a(boolean z, int i, int i2, ArrayList<ce> arrayList);
    }

    interface a {
        void a(int i, tmsdkobf.nq.b bVar);
    }

    public interface c {
        long a(boolean z, int i, ce ceVar);

        long b(boolean z, int i, ce ceVar);
    }

    public interface d {
        void a(long j, int i, JceStruct jceStruct, int i2, ka kaVar, boolean z);
    }

    public interface e {
        void a(jw jwVar);

        void a(a aVar);

        void b(jw jwVar);

        void b(a aVar);
    }

    public static class f {
        public int Fh = 0;
        public boolean Fi = false;
        public boolean Fj = false;
        public boolean Fk = false;
        public boolean Fl = false;
        public boolean Fm = false;
        public boolean Fn = false;
        public boolean Fo = false;
        public boolean Fp = false;
        public int Fq;
        public tmsdkobf.nq.b Fr;
        public long Fs;
        public ArrayList<bw> Ft;
        public b Fu;
        public long Fv = System.currentTimeMillis();
        public boolean Fw = false;
        public byte Fx = (byte) 0;
        public long Fy = -1;

        public f(int i, boolean z, boolean z2, boolean z3, long j, ArrayList<bw> arrayList, b bVar, long j2) {
            this.Fh = i;
            this.Fi = z;
            this.Fl = z2;
            this.Fm = z3;
            this.Fs = j;
            this.Ft = arrayList;
            this.Fu = bVar;
            this.Fq = ns.fX().fP();
            this.Fy = j2;
        }

        public boolean gp() {
            boolean z = true;
            long abs = Math.abs(System.currentTimeMillis() - this.Fv);
            if (abs < 180000) {
                z = false;
            }
            if (z) {
                nv.c("ocean", "[ocean][time_out]SharkNetwork.SharkSend.isTimeOut(), SharkSend.seqNoTag: " + this.Fq + " time(s): " + (abs / 1000), null, null);
                if (this.Ft != null) {
                    int size = this.Ft.size();
                    for (int i = 0; i < size; i++) {
                        bw bwVar = (bw) this.Ft.get(i);
                        if (bwVar != null) {
                            nv.c("ocean", "[ocean][time_out]SharkNetwork.SharkSend.isTimeOut(), cmdId|" + bwVar.bz + "|seqNo|" + bwVar.ey, null, null);
                        }
                    }
                }
            }
            return z;
        }

        public boolean gq() {
            return this.Fl || this.Fm;
        }

        public boolean gr() {
            return this.Fi;
        }
    }

    public nw(Context context, nl nlVar, e eVar, c cVar, d dVar, boolean z, String str) {
        mb.n("SharkNetwork", "[shark_init]SharkNetwork() isTest: " + z + " serverAdd: " + str);
        this.mContext = context;
        this.Dz = nlVar;
        this.EI = eVar;
        this.EV = cVar;
        this.EF = new nq(context, this);
        this.EG = new ni(context, this, z);
        this.EH = new oi(context, this, z);
        this.EO = Executors.newSingleThreadExecutor();
        this.EE = new od(this.Dz.aB(), context, nlVar, z, this.EW, this, dVar, this, str);
        if (this.Dz.aB()) {
            a(dVar);
            this.EH.c(dVar);
            if (im.bG()) {
                gi();
            }
        }
    }

    private void M(boolean z) {
        int i = 0;
        if (z) {
            bt(3);
        } else {
            bt(2);
        }
        if (nu.aC()) {
            mb.r("SharkNetwork", "[rsa_key] handleOnNeedRsaKey(), isSemiSendProcess, regRsaKeyListener() & requestSendProcessUpdateRsaKey()");
            a anonymousClass3 = new a() {
                public void a(int i, tmsdkobf.nq.b bVar) {
                    mb.n("SharkNetwork", "[rsa_key] IRsaKeyListener.onCallback(), isSemiSendProcess, unregRsaKeyListener(this) and call onRsaKeyUpdated(errCode)");
                    if (nw.this.EI != null) {
                        nw.this.EI.b((a) this);
                    }
                    nw.this.bw(i);
                }
            };
            if (this.EI != null) {
                this.EI.a(anonymousClass3);
            }
            long j = 2000;
            if (this.EY) {
                this.EY = false;
                j = 0;
            }
            this.vH.removeMessages(7);
            this.vH.sendEmptyMessageDelayed(7, j);
            return;
        }
        StringBuilder append = new StringBuilder().append("[rsa_key] handleOnNeedRsaKey(), isSendProcess, triggerUpdateRsaKey() in(ms) ");
        String str = "SharkNetwork";
        if (z) {
            i = 2000;
        }
        mb.r(str, append.append(i).toString());
        y(!z ? 0 : 2000);
    }

    private final ArrayList<ce> a(f fVar, boolean z, cf cfVar, ArrayList<ce> arrayList) {
        if (arrayList == null) {
            return null;
        }
        ArrayList<ce> arrayList2 = new ArrayList();
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            ce ceVar = (ce) arrayList.get(i);
            if (ceVar != null) {
                mb.n("SharkNetwork", "checkFilterList(), rs.refSeqNo: " + ceVar.ez + " rs.cmd: " + ceVar.bz + " rs.retCode: " + ceVar.eB + " rs.dataRetCode: " + ceVar.eC + " rs.data.length: " + (ceVar.data == null ? 0 : ceVar.data.length));
                if (!a(z, cfVar, ceVar)) {
                    arrayList2.add(ceVar);
                }
            }
        }
        return arrayList2;
    }

    private oh<Long, Integer, JceStruct> a(long j, int i, by byVar) {
        if (byVar != null) {
            mb.r("SharkNetwork", "[cu_guid_p]handlePushRefreshGuid(), |pushId=" + j + "|serverShasimiSeqNo=" + i);
            this.EG.a(true, byVar.et);
            return null;
        }
        mb.s("SharkNetwork", "[cu_guid_p]handlePushRefreshGuid(), scPushRefreshGuid == null");
        return null;
    }

    private oh<Long, Integer, JceStruct> a(long j, int i, g gVar) {
        if (gVar != null) {
            om gQ = this.EE.gQ();
            if (gQ != null) {
                gQ.a(j, i, gVar);
            }
            mb.d("SharkNetwork", "[ip_list]report push status, |pushId=" + j);
            c cVar = new c();
            cVar.hash = gVar.hash;
            return new oh(Long.valueOf(j), Integer.valueOf(156), cVar);
        }
        mb.s("SharkNetwork", "[ip_list]handleHIPList(), scHIPList == null");
        return null;
    }

    private void a(d dVar) {
        ka anonymousClass6 = new ka() {
            public oh<Long, Integer, JceStruct> a(int i, long j, int i2, JceStruct jceStruct) {
                if (jceStruct != null) {
                    switch (i2) {
                        case 10155:
                            return nw.this.a(j, i, (g) jceStruct);
                        case 15081:
                            return nw.this.a(j, i, (by) jceStruct);
                        default:
                            return null;
                    }
                }
                mb.o("SharkNetwork", "[ip_list][cu_guid_p]onRecvPush() null == push");
                return null;
            }
        };
        dVar.a(0, 10155, new g(), 0, anonymousClass6, false);
        dVar.a(0, 15081, new by(), 0, anonymousClass6, false);
        mb.n("SharkNetwork", "[ip_list][cu_guid_p]registerSharkPush, Cmd_SCHIPList,Cmd_SCPushRefreshGuid: 10155,15081");
    }

    private void a(boolean z, int i, f fVar) {
        mb.n("SharkNetwork", "runError(), ret = " + i + " isTcpChannel: " + z);
        if (fVar != null) {
            ArrayList arrayList = new ArrayList();
            if (this.EK != null && this.EK.Fq == fVar.Fq) {
                mb.n("SharkNetwork", "runError(), updating rsa, only callback rsa");
                arrayList.add(this.EK);
                bu(this.EK.Fq);
            } else if (this.EL != null && this.EL.Fq == fVar.Fq) {
                mb.n("SharkNetwork", "runError(), updating guid, only callback guid");
                arrayList.add(this.EL);
                bu(this.EL.Fq);
            } else {
                mb.n("SharkNetwork", "runError(), call back failed for this seqNo: " + fVar.Fq);
                arrayList.add(fVar);
                bu(fVar.Fq);
                synchronized (this.EM) {
                    mb.d("SharkNetwork", "runError(), callback failed for mSharkQueueWaiting, size(): " + this.EM.size());
                    arrayList.addAll(this.EM);
                    this.EM.clear();
                }
            }
            mb.n("SharkNetwork", "runError(), callback error, ret: " + i + " values.size(): " + arrayList.size());
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                a(z, (f) it.next(), i, 0, null);
            }
            return;
        }
        mb.s("SharkNetwork", "runError(), failedSharkSend == null");
    }

    private void a(boolean z, f fVar, int i, int i2, ArrayList<ce> arrayList) {
        a(false, z, fVar, i, i2, (ArrayList) arrayList);
    }

    private void a(boolean z, boolean z2, int i) {
        mb.d("SharkNetwork", "onSharkVipError(), retCode = " + i + " 事件： " + (!z2 ? "注册guid" : "交换密钥"));
        ArrayList arrayList = new ArrayList();
        synchronized (this.EN) {
            mb.d("SharkNetwork", "onSharkVipError(), callback failed for all sending: " + this.EN.keySet());
            arrayList.addAll(this.EN.values());
            this.EN.clear();
        }
        synchronized (this.EM) {
            mb.d("SharkNetwork", "onSharkVipError(), callback failed for mSharkQueueWaiting, size(): " + this.EM.size());
            arrayList.addAll(this.EM);
            this.EM.clear();
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            a(z, (f) it.next(), i, 0, null);
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0025, code:
            if (r6.Ft.size() > 0) goto L_0x000a;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void a(boolean z, boolean z2, f fVar, int i, int i2, ArrayList<ce> arrayList) {
        if (fVar != null) {
            if (z) {
                if (fVar.Ft != null) {
                }
            }
            try {
                bu(fVar.Fq);
                fVar.Fu.a(z2, i, i2, arrayList);
            } catch (Throwable e) {
                mb.c("SharkNetwork", "runError() callback crash", e);
            }
        }
    }

    private void a(boolean z, boolean z2, f fVar, int i, int i2, ce ceVar) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(ceVar);
        a(z, z2, fVar, i, i2, arrayList);
    }

    private boolean a(boolean z, cf cfVar, ce ceVar) {
        if (ceVar == null) {
            return false;
        }
        boolean z2 = false;
        if (nz.b(ceVar)) {
            this.EV.a(z, cfVar.ey, ceVar);
            nv.b("ocean", "[ocean]guid|" + this.EG.b() + "|push|" + "通道|" + (!z ? "http|" : "tcp|") + "sharkSeqNo|" + cfVar.ey + "|ECmd|" + ceVar.bz + "|seqNo|" + ceVar.ey + "|refSeqNo|" + ceVar.ez + "|ret|" + 0 + (ceVar.eO == null ? "" : "|pushId|" + ceVar.eO.ex), null, ceVar);
            qg.d(65541, r1);
            z2 = true;
        } else if (nz.c(ceVar)) {
            this.EV.b(z, cfVar.ey, ceVar);
            nv.b("ocean", "[ocean]guid|" + this.EG.b() + "|gift|" + "通道|" + (!z ? "http|" : "tcp|") + "sharkSeqNo|" + cfVar.ey + "|ECmd|" + ceVar.bz + "|seqNo|" + ceVar.ey + "|refSeqNo|" + ceVar.ez + "|ret|" + 0 + (ceVar.eO == null ? "" : "|pushId|" + ceVar.eO.ex), null, ceVar);
            qg.d(65541, r1);
            z2 = true;
        }
        return z2;
    }

    private void b(final f fVar) {
        if (fVar != null) {
            mb.d("SharkNetwork", "runTimeout(), will check timeout for sharkSend with seqNoTag: " + fVar.Fq);
            this.Fa.removeMessages(1, fVar);
            ((ki) fj.D(4)).addTask(new Runnable() {
                public void run() {
                    f c = nw.this.bu(fVar.Fq);
                    if (c != null) {
                        mb.n("SharkNetwork", "runTimeout(), sharkSend.seqNoTag: " + fVar.Fq + " isSent: " + fVar.Fw);
                        nw.this.a(fVar.Fn, c, !c.Fw ? -21000020 : -21050000, 0, null);
                    }
                }
            }, "runTimeout");
        }
    }

    private void bt(int i) {
        ArrayList arrayList;
        synchronized (this.EM) {
            arrayList = (ArrayList) this.EM.clone();
        }
        if (arrayList != null && arrayList.size() > 0) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                f fVar = (f) it.next();
                if (!(fVar == null || fVar.Ft == null || fVar.Ft.size() <= 0)) {
                    Iterator it2 = fVar.Ft.iterator();
                    while (it2.hasNext()) {
                        bw bwVar = (bw) it2.next();
                        if (bwVar != null) {
                            nt.ga().a("SharkNetwork", bwVar.bz, bwVar.ey, bwVar, i);
                        }
                    }
                }
            }
        }
    }

    private f bu(int i) {
        f fVar;
        mb.d("SharkNetwork", "removeSendingBySeqNoTag() seqNoTag: " + i);
        synchronized (this.EN) {
            fVar = (f) this.EN.remove(Integer.valueOf(i));
        }
        return fVar;
    }

    private void bv(int i) {
        if (i != 0) {
            int abs = i <= 0 ? -800000000 + i : Math.abs(-800000000) + i;
            mb.n("SharkNetwork", "[cu_guid] onGuidRegisterResult(), guid failed, call onSharkVipError(), " + abs);
            a(false, false, abs);
            return;
        }
        this.vH.sendEmptyMessage(1);
    }

    private void bw(int i) {
        if (i != 0) {
            int abs = i <= 0 ? -900000000 + i : Math.abs(-900000000) + i;
            mb.n("SharkNetwork", "[cu_guid] onRsaKeyUpdateResult(), rsa failed, call onSharkVipError(), " + abs);
            a(false, true, abs);
            return;
        }
        this.EQ = System.currentTimeMillis();
        this.EP = false;
        synchronized (this.DA) {
            if (this.ES) {
                mb.s("SharkNetwork", "[cu_guid] onRsaKeyUpdateResult(), update rsa succ, allow register guid!");
                this.ES = false;
            }
        }
        Object<f> arrayList = new ArrayList();
        synchronized (this.EN) {
            if (this.EN.size() > 0) {
                for (f fVar : this.EN.values()) {
                    if (fVar.Fj || fVar.Fk) {
                        mb.n("SharkNetwork", "[cu_guid][cu_guid] onRsaKeyUpdateResult(), rsa or guid, should not revert and resend after rsa updated, rsa?" + fVar.Fj + " guid?" + fVar.Fk);
                    } else {
                        arrayList.add(fVar);
                    }
                }
                this.EN.clear();
            }
        }
        if (arrayList.size() <= 0) {
            mb.n("SharkNetwork", "[cu_guid] onRsaKeyUpdateResult(), rsa succ, no need to revert and resend data");
        } else {
            mb.n("SharkNetwork", "[cu_guid] onRsaKeyUpdateResult(), rsa succ, revert and resend data, size: " + arrayList.size());
            for (f c : arrayList) {
                c(c);
            }
            synchronized (this.EM) {
                this.EM.addAll(arrayList);
            }
        }
        mb.n("SharkNetwork", "[cu_guid] onRsaKeyUpdateResult(), rsa succ, send MSG_SHARK_SEND");
        this.vH.sendEmptyMessage(1);
    }

    private void c(f fVar) {
        if (fVar == null || fVar.Ft == null || fVar.Fr == null || fVar.Fr.DX == null) {
            mb.o("SharkNetwork", "[rsa_key]revertClientSashimiData() something null");
            return;
        }
        Iterator it = fVar.Ft.iterator();
        while (it.hasNext()) {
            bw bwVar = (bw) it.next();
            if (!(bwVar == null || bwVar.data == null)) {
                if ((bwVar.eE & 2) == 0) {
                    bwVar.data = nh.decrypt(bwVar.data, fVar.Fr.DX.getBytes());
                }
                if (bwVar.data == null) {
                    mb.o("SharkNetwork", "[rsa_key]revertClientSashimiData(), revert failed, cmd: " + bwVar.bz);
                } else {
                    mb.d("SharkNetwork", "[rsa_key]revertClientSashimiData(), revert succ, cmd: " + bwVar.bz + " len: " + bwVar.data.length);
                }
                nt.ga().a("SharkNetwork", bwVar.bz, bwVar.ey, bwVar, 13);
            }
        }
    }

    private void gg() {
        bt(4);
        if (nu.aC()) {
            mb.n("SharkNetwork", "[cu_guid] handleOnNeedGuid(), isSemiSendProcess, no guid, regGuidListener() & requestSendProcessRegisterGuid()");
            jw anonymousClass2 = new jw() {
                public void c(int i, String str) {
                    mb.n("SharkNetwork", "[cu_guid] IGuidCallback.onCallback(), unregGuidListener(this) and call onGuidRegisterResult(errCode)");
                    if (nw.this.EI != null) {
                        nw.this.EI.b((jw) this);
                    }
                    nw.this.bv(i);
                }
            };
            if (this.EI != null) {
                this.EI.a(anonymousClass2);
            }
            long j = 2000;
            if (this.EX) {
                this.EX = false;
                j = 0;
            }
            this.vH.removeMessages(6);
            this.vH.sendEmptyMessageDelayed(6, j);
            return;
        }
        gh();
    }

    private synchronized void gi() {
        Object obj = null;
        synchronized (this) {
            if (this.Dz.aB()) {
                if (this.EZ != 0) {
                    if (System.currentTimeMillis() - this.EZ > 300000) {
                        obj = 1;
                    }
                    if (obj == null) {
                        return;
                    }
                }
                this.EZ = System.currentTimeMillis();
                this.vH.removeMessages(8);
                this.vH.sendEmptyMessage(8);
                mb.n("SharkNetwork", "[cu_vid]triggerRegVidIfNeed(), send msg: MSG_REGISTER_VID_IFNEED in 5s");
                return;
            }
        }
    }

    private static void r(ArrayList<ce> arrayList) {
        if (arrayList != null && arrayList.size() > 0) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ce ceVar = (ce) it.next();
                if (ceVar != null && ceVar.eB == 3) {
                    mb.n("SharkNetwork", "[shark_v4][shark_fin]mazu said need sharkfin, cmdId: " + ceVar.bz + " ClientSashimi.seqNo: " + ceVar.ez + " ServerSashimi.seqNo: " + ceVar.ey);
                    nh.fy();
                    break;
                }
            }
        }
    }

    private boolean s(ArrayList<ce> arrayList) {
        if (arrayList == null || arrayList.size() != 1) {
            return false;
        }
        ce ceVar = (ce) arrayList.get(0);
        if (ceVar == null) {
            return false;
        }
        return 2 == ceVar.eB;
    }

    protected tmsdkobf.nq.b a(boolean z, f fVar) {
        if (fVar == null) {
            return null;
        }
        Iterator it;
        bw bwVar;
        tmsdkobf.nq.b bVar = null;
        if (z) {
            bVar = this.EF.ap();
            fVar.Fr = bVar;
            it = fVar.Ft.iterator();
            while (it.hasNext()) {
                bwVar = (bw) it.next();
                if (bwVar != null && bwVar.data != null && bwVar.data.length > 0 && (bwVar.eE & 2) == 0) {
                    bwVar.data = nh.encrypt(bwVar.data, bVar.DX.getBytes());
                    if (bwVar.data == null) {
                        mb.o("SharkNetwork", "[ocean][rsa_key]encrypt failed, cmdId: " + bwVar.bz);
                    }
                }
            }
        }
        if (fVar.Ft != null && fVar.Ft.size() > 0) {
            it = fVar.Ft.iterator();
            while (it.hasNext()) {
                bwVar = (bw) it.next();
                if (bwVar != null) {
                    if (bwVar.ez == 0) {
                        fVar.Fp = true;
                    }
                    nt.ga().a("SharkNetwork", bwVar.bz, bwVar.ey, bwVar, 5);
                }
            }
        }
        synchronized (this.EN) {
            mb.d("SharkNetwork", "spSend() sharkSend.seqNoTag: " + fVar.Fq);
            this.EN.put(Integer.valueOf(fVar.Fq), fVar);
        }
        this.Fa.sendMessageDelayed(Message.obtain(this.Fa, 1, fVar), !((fVar.Fy > 0 ? 1 : (fVar.Fy == 0 ? 0 : -1)) <= 0) ? fVar.Fy : 180000);
        this.EE.d(fVar);
        return bVar;
    }

    public void a(int i, long j, boolean z, ArrayList<bw> arrayList, b bVar) {
        a(new f(i, false, false, false, j, arrayList, bVar, 0));
        if (z) {
            this.EG.a(false, null);
        }
    }

    protected void a(ArrayList<bw> arrayList, b bVar) {
        int i = 0;
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            bw bwVar = (bw) it.next();
            mb.n("SharkNetwork_CMDID", "[" + i + "]Rsa发包请求：cmd id:[" + bwVar.bz + "]seqNo:[" + bwVar.ey + "]refSeqNo:[" + bwVar.ez + "]retCode:[" + bwVar.eB + "]dataRetCode:[" + bwVar.eC + "]");
            i++;
        }
        this.EK = new f(0, true, false, false, 0, arrayList, bVar, 0);
        this.EK.Fj = true;
        this.vH.obtainMessage(0, 1, 0).sendToTarget();
    }

    public void a(f fVar) {
        if (fVar != null && fVar.Fu != null && fVar.Ft != null && fVar.Ft.size() > 0) {
            synchronized (this.EM) {
                this.EM.add(fVar);
                mb.n("SharkNetwork", "asyncSendShark() mSharkQueueWaiting.size(): " + this.EM.size());
            }
            Iterator it = fVar.Ft.iterator();
            while (it.hasNext()) {
                bw bwVar = (bw) it.next();
                if (bwVar != null) {
                    mb.n("SharkNetwork_CMDID", "[" + 0 + "]发包请求：cmd id:[" + bwVar.bz + "]seqNo:[" + bwVar.ey + "]refSeqNo:[" + bwVar.ez + "]retCode:[" + bwVar.eB + "]dataRetCode:[" + bwVar.eC + "]");
                    nt.ga().a("SharkNetwork", bwVar.bz, bwVar.ey, bwVar, 1);
                }
            }
            this.vH.sendEmptyMessage(1);
        }
    }

    protected tmsdkobf.nq.b ap() {
        return this.EF.ap();
    }

    public String b() {
        return this.EG.b();
    }

    public void b(int i, int i2, int i3) {
        if (i2 > 0) {
            if (this.EJ == null) {
                Class cls = nw.class;
                synchronized (nw.class) {
                    if (this.EJ == null) {
                        this.EJ = new SparseArray();
                    }
                }
            }
            pf pfVar = new pf("network_control_" + i, (long) (i2 * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY), i3);
            synchronized (this.EJ) {
                this.EJ.append(i, pfVar);
                mb.d("SharkNetwork", "[network_control]handleNetworkControl : cmdid|" + i + "|timeSpan|" + i2 + "|maxTimes|" + i3 + " size: " + this.EJ.size());
            }
        }
    }

    protected void b(ArrayList<bw> arrayList, b bVar) {
        int i = 0;
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            bw bwVar = (bw) it.next();
            mb.n("SharkNetwork_CMDID", "[" + i + "]Guid发包请求：cmd id:[" + bwVar.bz + "]seqNo:[" + bwVar.ey + "]refSeqNo:[" + bwVar.ez + "]retCode:[" + bwVar.eB + "]dataRetCode:[" + bwVar.eC + "]");
            i++;
        }
        this.EL = new f(0, true, false, false, 0, arrayList, bVar, 0);
        this.EL.Fk = true;
        this.vH.obtainMessage(0, 2, 0).sendToTarget();
    }

    void fC() {
        mb.n("SharkNetwork", "[cu_guid]refreshGuid()");
        this.EG.fC();
    }

    void gh() {
        synchronized (this.DA) {
            if (this.ES) {
                if (!lr.a(System.currentTimeMillis(), this.EU, 3)) {
                    mb.s("SharkNetwork", "[cu_guid]registering guid, ignore");
                    return;
                }
            }
            this.ES = true;
            this.EU = System.currentTimeMillis();
            this.vH.removeMessages(3);
            this.vH.sendEmptyMessageDelayed(3, 1000);
        }
    }

    public og gj() {
        return this.EE.gj();
    }

    void gk() {
        mb.n("SharkNetwork", "[rsa_key]refreshRsaKey()");
        this.EF.refresh();
    }

    protected nl gl() {
        return this.Dz;
    }

    public void gm() {
        if (this.vH != null) {
            mb.r("SharkNetwork", "[cu_guid]send msg: guid info changed, check update guid in 15s");
            this.vH.removeMessages(4);
            this.vH.sendEmptyMessage(4);
        }
    }

    public void gn() {
        if (this.vH != null) {
            mb.n("SharkNetwork", "[cu_vid] updateVidIfNeed(), send MSG_UPDATE_VID_IFNEED in 2s");
            this.vH.removeMessages(9);
            this.vH.sendEmptyMessageDelayed(9, 2000);
        }
    }

    public void go() {
        gi();
    }

    public void onReady() {
        mb.r("SharkNetwork", "[cu_guid]onReady(), check update guid");
        this.EG.a(true, null);
    }

    void y(long j) {
        synchronized (this.DA) {
            if (this.ER) {
                if (!lr.a(System.currentTimeMillis(), this.ET, 3)) {
                    return;
                }
            }
            this.ER = true;
            this.ET = System.currentTimeMillis();
            this.vH.removeMessages(2);
            this.vH.sendEmptyMessageDelayed(2, j);
        }
    }
}
