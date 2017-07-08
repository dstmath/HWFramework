package tmsdkobf;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.util.SparseIntArray;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import tmsdk.common.CallerIdent;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.urlcheck.UrlCheckType;
import tmsdk.common.utils.d;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;
import tmsdkobf.kp.c;

/* compiled from: Unknown */
public class hv {
    public static String TAG;
    private static Object lock;
    private static int[] rd;
    private static hv rh;
    private HandlerThread mHandlerThread;
    private hz ra;
    private ht rb;
    private HashMap<Integer, hq> rc;
    private Set<Integer> re;
    private qi rf;
    private qi rg;
    private State ri;
    private State rj;
    private tmsdkobf.lp.a rk;
    private li rl;
    private ConcurrentHashMap<Integer, a> rm;
    private SparseIntArray rn;
    private Handler ro;
    private Callback rp;
    private HashSet<ks> rq;

    /* compiled from: Unknown */
    static class a {
        public c rs;
        public hw rt;

        public a(c cVar, hw hwVar) {
            this.rs = cVar;
            this.rt = hwVar;
        }
    }

    /* compiled from: Unknown */
    class b implements lg {
        final /* synthetic */ hv rr;
        tmsdkobf.hs.a ru;

        public b(hv hvVar, tmsdkobf.hs.a aVar) {
            this.rr = hvVar;
            this.ru = aVar;
        }

        public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
            Message.obtain(this.rr.ro, 9, new ia(i, i2, i3, i4, fsVar, this.ru)).sendToTarget();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.hv.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.hv.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.hv.<clinit>():void");
    }

    private hv() {
        State state;
        this.rc = new HashMap();
        this.re = new HashSet();
        this.rf = new qi("profile4", 43200000, 24);
        this.rg = new qi("profile2", 43200000, 12);
        this.ri = State.UNKNOWN;
        this.rj = State.UNKNOWN;
        this.rk = new tmsdkobf.lp.a() {
            final /* synthetic */ hv rr;

            {
                this.rr = r1;
            }
        };
        this.rl = new li() {
            final /* synthetic */ hv rr;

            {
                this.rr = r1;
            }

            public pl<Long, Integer, fs> a(int i, long j, int i2, fs fsVar) {
                if (i2 == 11052) {
                    pa.w(hv.TAG, "recv profile full upload push");
                    am amVar = (am) fsVar;
                    if (amVar != null) {
                        pa.w(hv.TAG, "profilePush.profileID : " + amVar.bf + " profilePush.profileCmd : " + amVar.bt);
                        switch (amVar.bt) {
                            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                                this.rr.c(amVar.bf, 0);
                                if (amVar.bf == 2) {
                                    this.rr.rb.u(true);
                                    kr.do().bb(2);
                                    break;
                                }
                                break;
                            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                                if (amVar.bf == 2) {
                                    this.rr.rb.u(false);
                                    break;
                                }
                                break;
                        }
                        return null;
                    }
                    pa.w(hv.TAG, "profilePush == null");
                    return null;
                }
                d.c(hv.TAG, "cmdId != ECmd.Cmd_SCProfilePushCmd : " + i2);
                return null;
            }
        };
        this.rm = new ConcurrentHashMap();
        this.rn = new SparseIntArray();
        this.rp = new Callback() {
            final /* synthetic */ hv rr;

            {
                this.rr = r1;
            }

            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                        this.rr.aR(message.arg1);
                        break;
                    case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                        int i = message.arg1;
                        ArrayList aQ = this.rr.aQ(i);
                        if (aQ != null && aQ.size() > 0) {
                            byte[] a = hu.a(hu.a(i, 0, aQ));
                            if (a != null) {
                                this.rr.rn.put(i, a.length);
                                break;
                            }
                        }
                        hu.h(hv.TAG, "get full upload jce");
                        return true;
                        break;
                    case FileInfo.TYPE_BIGFILE /*3*/:
                        this.rr.rn.delete(message.arg1);
                        break;
                    case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                        new Bundle().putInt("profile.id", message.arg1);
                        break;
                    case UrlCheckType.STEAL_ACCOUNT /*5*/:
                        this.rr.a((hx) message.obj);
                        break;
                    case UrlCheckType.TIPS_CHEAT /*6*/:
                        this.rr.b((tmsdkobf.hs.a) message.obj);
                        break;
                    case UrlCheckType.TIPS_DEFAULT /*7*/:
                        this.rr.a((hq) message.obj);
                        break;
                    case RubbishType.SCAN_FLAG_APK /*8*/:
                        this.rr.bH();
                        break;
                    case UrlCheckType.MAKE_MONEY /*9*/:
                        this.rr.a((ia) message.obj);
                        break;
                    case UrlCheckType.SEX /*10*/:
                        this.rr.re.clear();
                        break;
                }
                return true;
            }
        };
        this.rq = new HashSet();
        this.rb = ht.bD();
        this.mHandlerThread = ((lq) fe.ad(4)).bF("profile upload task queue");
        this.mHandlerThread.start();
        this.ro = new Handler(this.mHandlerThread.getLooper(), this.rp);
        this.ra = (hz) fe.ad(5);
        this.ra.a(11052, new am(), 2, this.rl);
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) TMSDKContext.getApplicaionContext().getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            state = State.DISCONNECTED;
            this.ri = state;
        } else {
            state = activeNetworkInfo.getState();
            this.ri = state;
        }
        this.rj = state;
        l(30000);
        rd = new int[6];
        for (int i = 1; i < 5; i++) {
            rd[i] = 0;
        }
    }

    private void a(hq hqVar) {
        if (hqVar != null) {
            int bA = hs.bz().bA();
            pa.w(TAG, "enqueue force push taskID : " + bA);
            if (hqVar.qS == null || !hqVar.qS.dC()) {
                this.rc.put(Integer.valueOf(bA), hqVar);
                bH();
            }
        }
    }

    private void a(tmsdkobf.hs.a aVar) {
        if (aVar != null) {
            hu.h(TAG, "---onSharkUnknown");
            if (aVar.bf > 0 && aVar.bf < 5) {
                qi aS = aS(aVar.bf);
                if (aS == null) {
                    c(aVar.bf, 0);
                } else if (aS.hK()) {
                    aS.hL();
                    c(aVar.bf, 0);
                    hu.h(TAG, "unknown err,full upload");
                } else {
                    hu.h(TAG, "unknown err,full upload,freq ctrl,ignore this");
                }
            }
        }
    }

    private void a(hx hxVar) {
        if (hxVar != null) {
            int i;
            int i2;
            fs a = hu.a(hxVar.bf, hxVar.rv, hxVar.rw);
            hu.h(TAG, "profileEnqueue");
            int i3 = a.bf;
            int i4 = a.bj;
            hs bz = hs.bz();
            if (this.rb.b(a) && i4 != 0) {
                hu.h(TAG, "ingnore this, the same as the last upload task");
                i = -1;
                i2 = 0;
            } else {
                byte[] a2 = hu.a(a);
                if (a2 == null || a2.length == 0) {
                    i = -2;
                    i2 = 0;
                } else {
                    i2 = a2.length;
                    if (i4 == 0) {
                        bz.aG(i3);
                        this.rb.aM(i3);
                    }
                    long b = (long) bz.b(a2, i3);
                    hu.h(TAG, "profileEnqueue taskID : " + b);
                    if ((b >= 0 ? 1 : null) == null) {
                        hu.h(TAG, "pushLast fail!!!");
                        i = -3;
                    } else {
                        this.rb.e(i3, a2.length);
                        if (i4 == 0) {
                            this.rn.put(i3, a2.length);
                        }
                        this.rb.a(a);
                        i = 0;
                    }
                }
            }
            a aVar = (a) this.rm.get(Integer.valueOf(i3));
            if (!(aVar == null || aVar.rt == null)) {
                aVar.rt.a(hxVar.rv, hxVar.rw, i, i2);
            }
            bH();
        }
    }

    private void a(ia iaVar) {
        int i = 0;
        if (iaVar != null) {
            tmsdkobf.hs.a aVar = iaVar.ru;
            int i2 = iaVar.dK;
            int i3 = iaVar.dJ;
            if (aVar != null) {
                HashSet hashSet;
                ArrayList arrayList = new ArrayList();
                ak bC = aVar.bC();
                if (!(bC == null || bC.bi == null)) {
                    Iterator it = bC.bi.iterator();
                    while (it.hasNext()) {
                        byte[] bArr = (byte[]) it.next();
                        if (bC.bf == 4) {
                            arrayList.add(ot.a(bArr, new al(), false));
                        }
                    }
                }
                d.e("TrafficCorrection", "ProfileUpload-retCode:[" + i3 + "]dataRetCode[" + i2 + "]");
                if (i3 != 0 || i2 != 0) {
                    i = -1;
                }
                synchronized (this.rq) {
                    hashSet = new HashSet(this.rq);
                }
                Iterator it2 = hashSet.iterator();
                while (it2.hasNext()) {
                    ((ks) it2.next()).a(arrayList, i);
                }
                this.re.remove(Integer.valueOf(aVar.qR));
                if (aVar.bC() == null) {
                    hu.h(TAG, "recv profile resp retCode : " + i3 + " dataRetCode : " + i2);
                } else {
                    hu.h(TAG, "recv profile resp retCode : " + i3 + " dataRetCode : " + i2 + " profileID : " + aVar.bC().bf + " actionID : " + aVar.bC().bj + " lastVerifyKey " + aVar.bC().bg + " presentVerifyKey " + aVar.bC().bh + " taskID " + aVar.qR);
                }
                if (i3 == 0) {
                    switch (i2) {
                        case SpaceManager.ERROR_CODE_PARAM /*-1*/:
                            c(aVar);
                            break;
                        case SpaceManager.ERROR_CODE_OK /*0*/:
                            b(aVar);
                            break;
                        default:
                            a(aVar);
                            break;
                    }
                } else if (i3 > 0) {
                    d(aVar);
                }
            }
        }
    }

    private void aR(int i) {
        a aVar = (a) this.rm.get(Integer.valueOf(i));
        if (aVar == null || aVar.rs == null) {
            hu.h(TAG, "profileID " + i + " callback null,can't full upload");
            return;
        }
        ArrayList dn = aVar.rs.dn();
        if (dn != null) {
            a(CallerIdent.getIdent(1, UpdateConfig.UPDATE_FLAG_PAY_LIST), i, 0, dn);
        } else {
            hu.h(TAG, "get full upload profile null,can't full upload");
        }
    }

    private qi aS(int i) {
        switch (i) {
            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                return this.rg;
            case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                return this.rf;
            default:
                return null;
        }
    }

    private void b(tmsdkobf.hs.a aVar) {
        if (aVar != null) {
            hs bz = hs.bz();
            switch (aVar.qX) {
                case SpaceManager.ERROR_CODE_OK /*0*/:
                    if (aVar.bC() != null) {
                        int i = aVar.bC().bf;
                        hu.h(TAG, "+++onSharkSuccess");
                        rd[i] = 0;
                        byte[] aH = bz.aH(aVar.qR);
                        if (aH != null) {
                            this.rb.f(i, aH.length);
                            hu.h(TAG, "popFirst success! taskID : " + aVar.qR);
                            break;
                        }
                        hu.h(TAG, "popFirst fail! queueQuantity : " + this.rb.aL(i) + " taskID : " + aVar.qR);
                        break;
                    }
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    bz.aH(aVar.qR);
                    hu.h(TAG, "popFirst success! taskID : " + aVar.qR);
                    break;
            }
        }
    }

    public static hv bG() {
        if (rh == null) {
            synchronized (lock) {
                if (rh == null) {
                    rh = new hv();
                }
            }
        }
        return rh;
    }

    private void bH() {
        hu.h(TAG, "uploadTask");
        List<tmsdkobf.hs.a> bB = hs.bz().bB();
        if (bB == null || bB.size() == 0) {
            d.f(TAG, "uploadTask no more task");
            return;
        }
        Iterator it = bB.iterator();
        while (it.hasNext()) {
            if (this.re.contains(Integer.valueOf(((tmsdkobf.hs.a) it.next()).qR))) {
                it.remove();
            }
        }
        if (bB == null || bB.size() == 0) {
            d.f(TAG, "all task is uploading");
            return;
        }
        Object obj = null;
        loop1:
        while (true) {
            Object obj2 = obj;
            for (tmsdkobf.hs.a aVar : bB) {
                if (aVar != null && aVar.bC() != null) {
                    int i = aVar.bC().bf;
                    if (aVar.bC().bj == 0) {
                        obj = obj2;
                    } else {
                        int i2 = this.rn.get(i);
                        int aL = this.rb.aL(i);
                        if (aL <= i2) {
                            obj = obj2;
                        } else if (i2 <= 0) {
                            obj = obj2;
                        } else {
                            hu.h(TAG, "queue more than full,then full upload. queue : " + aL + " quantity : " + i2);
                            c(i, 0);
                            obj = 1;
                        }
                    }
                }
            }
            break loop1;
        }
        if (obj2 == null) {
            for (tmsdkobf.hs.a aVar2 : bB) {
                if (aVar2.bC() != null) {
                    this.re.add(Integer.valueOf(aVar2.qR));
                    fs bC = aVar2.bC();
                    hu.h(TAG, "send : profileID " + bC.bf + " actionID " + bC.bj + " lastVerifyKey " + bC.bg + " presentVerifyKey " + bC.bh + " taskID " + aVar2.qR);
                    this.ra.a(1051, bC, new an(), 18, new b(this, aVar2), 90000);
                } else if (aVar2.qX != 1) {
                    hu.h(TAG, "ProfileQueueTask neither force push nor upload profile");
                } else {
                    hq hqVar = (hq) this.rc.remove(Integer.valueOf(aVar2.qR));
                    if (hqVar != null) {
                        if (!(hqVar == null || hqVar.qS == null)) {
                            if (!hqVar.qS.dC()) {
                            }
                        }
                        this.re.add(Integer.valueOf(aVar2.qR));
                        hu.h(TAG, "send : cmdid : " + hqVar.H + " taskID : " + hqVar.qR);
                        this.ra.c(hqVar.pid, hqVar.qG, hqVar.qH, hqVar.dF, hqVar.qI, hqVar.H, hqVar.qJ, hqVar.qK, hqVar.qL, hqVar.qM, hqVar.qN, hqVar.qO, hqVar.qP, hqVar.qQ);
                        b(aVar2);
                    }
                    b(aVar2);
                }
            }
        }
    }

    private void c(int i, long j) {
        Message obtain = Message.obtain(this.ro, 1, i, 0);
        this.ro.removeMessages(1);
        this.ro.sendMessageDelayed(obtain, j);
    }

    private void c(tmsdkobf.hs.a aVar) {
        if (aVar != null) {
            hu.h(TAG, "---onSharkFail");
            int[] iArr = rd;
            int i = aVar.bf;
            int i2 = iArr[i] + 1;
            iArr[i] = i2;
            if (i2 <= 2) {
                hu.h(TAG, "resend");
                l((long) (rd[aVar.bf] * 30000));
                return;
            }
            rd[aVar.bf] = 0;
            if (aVar.bf > 0 && aVar.bf < 5) {
                qi aS = aS(aVar.bf);
                if (aS == null) {
                    c(aVar.bf, 0);
                } else if (aS.hK()) {
                    aS.hL();
                    c(aVar.bf, 0);
                    hu.h(TAG, "err more than 2,full upload");
                } else {
                    hu.h(TAG, "err more than 2,full upload,freq ctrl,ignore this");
                }
            }
        }
    }

    private void d(tmsdkobf.hs.a aVar) {
        if (aVar != null) {
            hu.h(TAG, "---onSharkFail");
            int[] iArr = rd;
            int i = aVar.bf;
            int i2 = iArr[i] + 1;
            iArr[i] = i2;
            if (i2 <= 1) {
                hu.h(TAG, "resend");
                l((long) (rd[aVar.bf] * 30000));
                return;
            }
            rd[aVar.bf] = 0;
            hu.h(TAG, "err more than 1,wait next upload task");
        }
    }

    private void l(long j) {
        if (this.rj.compareTo(State.CONNECTED) != 0) {
            hu.h(TAG, "no network");
            return;
        }
        this.ro.removeMessages(8);
        this.ro.sendEmptyMessageDelayed(8, j);
    }

    public void a(long j, int i, c cVar, hw hwVar, int i2) {
        if (((a) this.rm.put(Integer.valueOf(i), new a(cVar, hwVar))) == null) {
            this.rn.put(i, i2);
        }
    }

    public void a(ks ksVar) {
        synchronized (this.rq) {
            this.rq.add(ksVar);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean a(long j, int i, int i2, ArrayList<fs> arrayList) {
        if (i <= 0 || i >= 5 || this.rm.get(Integer.valueOf(i)) == null) {
            return false;
        }
        hu.h(TAG, "profileUpload  profileID : " + i + " profileActionID : " + i2);
        Message.obtain(this.ro, 5, new hx(i, i2, arrayList, null)).sendToTarget();
        return true;
    }

    public ArrayList<fs> aQ(int i) {
        a aVar = (a) this.rm.get(Integer.valueOf(i));
        return (aVar == null || aVar.rs == null) ? null : aVar.rs.dn();
    }

    public WeakReference<ll> b(int i, int i2, int i3, long j, long j2, int i4, fs fsVar, byte[] bArr, fs fsVar2, int i5, lg lgVar, lh lhVar, long j3, long j4) {
        ll llVar = new ll();
        Handler handler = this.ro;
        Message.obtain(r22, 7, new hq(i, i2, i3, j, j2, i4, fsVar, bArr, fsVar2, i5, lgVar, lhVar, j3, j4, llVar)).sendToTarget();
        return new WeakReference(llVar);
    }

    public void b(ks ksVar) {
        synchronized (this.rq) {
            this.rq.remove(ksVar);
        }
    }

    public void g(int i, int i2) {
        this.rn.put(i, i2);
    }
}
