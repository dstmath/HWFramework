package tmsdkobf;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import tmsdk.common.ErrorCode;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.urlcheck.UrlCheckType;
import tmsdk.common.utils.d;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;
import tmsdkobf.po.b;

/* compiled from: Unknown */
public class pk extends px implements b, tmsdkobf.ps.a {
    private static long HO;
    private static int HR;
    private static long HU;
    private static long HV;
    private static long HW;
    public static int HX;
    public static boolean HY;
    private static Object Ik;
    private static pk Il;
    static final /* synthetic */ boolean fJ = false;
    private on Et;
    private li FR;
    protected ph.b FS;
    pj HE;
    private ps HJ;
    private AtomicInteger HK;
    private pw HL;
    private or HM;
    private long HN;
    private boolean HP;
    private Object HQ;
    private Runnable HS;
    private byte HT;
    private int HZ;
    private AtomicInteger Ia;
    private om Ib;
    private op Ic;
    private op Id;
    private pt<ow> Ie;
    private LinkedList<ow> If;
    private byte Ig;
    boolean Ih;
    Handler Ii;
    private a Ij;
    private Context context;
    HandlerThread vZ;

    /* compiled from: Unknown */
    public interface a {
        void a(boolean z, boolean z2, long j, ArrayList<bm> arrayList, pb.b bVar);
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.pk.2 */
    class AnonymousClass2 extends op {
        final /* synthetic */ pk Im;

        AnonymousClass2(pk pkVar, int i) {
            this.Im = pkVar;
            super(i);
        }

        protected void cb(int i) {
            pa.b("TmsTcpManager", "hb fail : " + i, null, null);
            mj.bB(-10018);
        }

        protected void onSuccess() {
            pa.b("TmsTcpManager", "hb success", null, null);
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.pk.3 */
    class AnonymousClass3 extends op {
        final /* synthetic */ pk Im;

        AnonymousClass3(pk pkVar, int i) {
            this.Im = pkVar;
            super(i);
        }

        protected void cb(int i) {
            this.Im.Ig = (byte) (byte) 0;
            pa.b("TmsTcpManager", "fp fail : " + i, null, null);
            mj.bB(-10024);
            this.Im.Et.av(i);
            this.Im.a(5, null, i, 0, true);
        }

        protected void onSuccess() {
            pa.b("TmsTcpManager", "fp success", null, null);
            this.Im.Ig = (byte) (byte) 1;
            pa.b("TmsTcpManager", "check wait fp success tasks", null, null);
            this.Im.a(3, null, 0, 0, true);
            this.Im.Et.av(0);
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.pk.6 */
    class AnonymousClass6 extends Handler {
        private boolean Fk;
        final /* synthetic */ pk Im;

        AnonymousClass6(pk pkVar, Looper looper) {
            this.Im = pkVar;
            super(looper);
            this.Fk = false;
        }

        private void b(ow owVar, int i) {
            if (owVar != null && owVar.Fm != null) {
                owVar.Fm.a(false, i, owVar.Fn);
            }
        }

        public void handleMessage(Message message) {
            if (this.Im.gY()) {
                ow owVar;
                switch (message.what) {
                    case SpaceManager.ERROR_CODE_OK /*0*/:
                        if (!this.Im.Ie.isEmpty()) {
                            int i = -900000;
                            ow owVar2 = (ow) this.Im.Ie.get();
                            if (owVar2 != null) {
                                i = this.Im.gV();
                                if (i == 0) {
                                    int x;
                                    this.Fk = owVar2.Fk;
                                    ll llVar = owVar2.Fl;
                                    if (llVar != null) {
                                        llVar.setState(1);
                                        if (llVar.dC()) {
                                            i = -11;
                                        } else {
                                            x = this.Im.HL.x(owVar2.data);
                                            llVar.setState(2);
                                        }
                                    } else {
                                        x = this.Im.HL.x(owVar2.data);
                                    }
                                    i = x;
                                    this.Im.Et.at(i);
                                    if (i == 0) {
                                        this.Im.Ie.poll();
                                        if (owVar2.Fn != null && owVar2.Fn.Gf) {
                                            this.Im.w(owVar2.data);
                                        }
                                        if (!this.Fk) {
                                            this.Im.HL.hz();
                                            if (!this.Im.HP) {
                                                synchronized (this.Im.HQ) {
                                                    if (!this.Im.HP) {
                                                        this.Im.gU();
                                                        this.Im.HP = true;
                                                        pa.b("TmsTcpManager", "remain connection", null, null);
                                                        break;
                                                    }
                                                    break;
                                                }
                                            }
                                            mk.eU().cu("remain_connect_action");
                                            mk.eU().a("remain_connect_action", (long) (pk.HR * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY), this.Im.HS);
                                            pa.b("TmsTcpManager", "refresh remain connection time, tcp will derease ref after " + pk.HR + " s.", null, null);
                                        }
                                        if (!this.Im.Ie.isEmpty()) {
                                            this.Im.a(0, null, 0, 0, true);
                                        }
                                    }
                                }
                            }
                            if (!(i == 0 || i == -11)) {
                                this.Im.a(owVar2, i);
                            }
                            b(owVar2, i);
                            break;
                        }
                    case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                        this.Im.gV();
                        break;
                    case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                        this.Im.If.add((ow) message.obj);
                        break;
                    case FileInfo.TYPE_BIGFILE /*3*/:
                        if (this.Im.If != null && this.Im.If.size() > 0) {
                            this.Im.cn(3);
                            pa.b("TmsTcpManager", "fp success. send waiting fp queue : " + this.Im.If.size(), null, null);
                            Iterator it = this.Im.If.iterator();
                            while (it.hasNext()) {
                                owVar = (ow) it.next();
                                if (owVar != null) {
                                    this.Im.Ie.add(owVar);
                                }
                            }
                            this.Im.If.clear();
                            this.Im.a(0, null, 0, 0, true);
                            break;
                        }
                    case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                        this.Im.gW();
                        break;
                    case UrlCheckType.STEAL_ACCOUNT /*5*/:
                        this.Im.cn(5);
                        if (this.Im.If != null && this.Im.If.size() > 0) {
                            int i2 = message.arg1;
                            pa.b("TmsTcpManager", "send wait fp tasks change to http : " + this.Im.If.size(), null, null);
                            Iterator it2 = this.Im.If.iterator();
                            while (it2.hasNext()) {
                                owVar = (ow) it2.next();
                                if (owVar != null) {
                                    this.Im.HM.a(owVar.Fm, owVar.Fn, i2, owVar.data);
                                }
                            }
                            this.Im.If.clear();
                            break;
                        }
                        return;
                        break;
                    case UrlCheckType.TIPS_CHEAT /*6*/:
                        this.Im.gZ();
                        this.Im.Et.aw(-50000);
                        this.Im.v(null);
                        if (this.Im.HK.get() > 0) {
                            if (this.Im.hg() < this.Im.hh()) {
                                pa.x("TmsTcpManager", "no resp data, reconnect : " + this.Im.gX());
                                break;
                            }
                            this.Im.a(5, null, 0, 0, true);
                            pa.x("TmsTcpManager", "no resp data, no more reconnect");
                            break;
                        }
                    case UrlCheckType.TIPS_DEFAULT /*7*/:
                        this.Im.gZ();
                        if (this.Im.HK.get() > 0) {
                            switch (this.Im.gX()) {
                                case -230000:
                                    pa.b("TmsTcpManager", "don't allow connect, no try reconnect more", null, null);
                                    break;
                                case -220000:
                                    pa.b("TmsTcpManager", "no network, no try reconnect more", null, null);
                                    break;
                                case SpaceManager.ERROR_CODE_OK /*0*/:
                                    pa.b("TmsTcpManager", "try to connect success", null, null);
                                    break;
                                default:
                                    break;
                            }
                        }
                    case RubbishType.SCAN_FLAG_APK /*8*/:
                        this.Im.v((byte[]) message.obj);
                        break;
                    case UrlCheckType.MAKE_MONEY /*9*/:
                        this.Im.cn(9);
                        this.Im.cn(6);
                        this.Im.cn(7);
                        mj.bB(ErrorCode.ERR_CORRECTION_FEEDBACK_UPLOAD_FAIL);
                        if (this.Im.HK.get() > 0) {
                            pa.b("TmsTcpManager", "no connection -> has connection : connectionState : " + this.Im.HT, null, null);
                            break;
                        }
                    case UrlCheckType.SEX /*10*/:
                        this.Im.cn(10);
                        this.Im.cn(6);
                        this.Im.cn(7);
                        mj.bB(ErrorCode.ERR_CORRECTION_BAD_SMS);
                        pa.b("TmsTcpManager", "has connection -> no connection : " + this.Im.HT, null, null);
                        this.Im.HT = (byte) (byte) 0;
                        this.Im.Ig = (byte) (byte) 0;
                        break;
                    case UrlCheckType.PRIVATE_SERVER /*11*/:
                        this.Im.hd();
                        break;
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.pk.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.pk.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.pk.<clinit>():void");
    }

    private pk() {
        this.HK = new AtomicInteger(0);
        this.HL = null;
        this.context = null;
        this.HN = 0;
        this.HP = false;
        this.HQ = new Object();
        this.HS = new Runnable() {
            final /* synthetic */ pk Im;

            {
                this.Im = r1;
            }

            public void run() {
                if (this.Im.HP) {
                    synchronized (this.Im.HQ) {
                        if (this.Im.HP) {
                            pa.b("TmsTcpManager", "keep timeout, decrease ref", null, null);
                            this.Im.closeConnection();
                            this.Im.HP = false;
                        }
                    }
                }
            }
        };
        this.HT = (byte) 0;
        this.HZ = 0;
        this.Ia = new AtomicInteger(0);
        this.Ic = new AnonymousClass2(this, 10999);
        this.Id = new AnonymousClass3(this, 10997);
        this.Ie = new pt(new Comparator<ow>() {
            final /* synthetic */ pk Im;

            {
                this.Im = r1;
            }

            public int a(ow owVar, ow owVar2) {
                return lk.bg(owVar2.qM) - lk.bg(owVar.qM);
            }

            public /* synthetic */ int compare(Object obj, Object obj2) {
                return a((ow) obj, (ow) obj2);
            }
        });
        this.If = new LinkedList();
        this.Ig = (byte) 0;
        this.Ih = false;
        this.vZ = null;
        this.Ii = null;
        this.context = TMSDKContext.getApplicaionContext();
        this.HL = new pw(this.context);
        this.HL.a((px) this);
        int currentTimeMillis = (int) System.currentTimeMillis();
        if (currentTimeMillis < 0) {
            currentTimeMillis = -currentTimeMillis;
        }
        this.Ia.set(currentTimeMillis / 100);
        this.vZ = jq.ct().bF("sendHandlerThread");
        this.vZ.start();
        if (!fJ) {
            if (!(HV < ((long) HX))) {
                throw new AssertionError();
            }
        }
        this.Ii = new AnonymousClass6(this, this.vZ.getLooper());
    }

    private static void L(boolean z) {
        HY = z;
        if (z) {
            HU = 10000;
            HV = 20;
            HW = 20;
            HX = 10;
            HR = 60;
            HO = 15000;
        }
    }

    private synchronized void M(boolean z) {
        this.Ih = z;
    }

    private pl<Long, Integer, fs> a(long j, e eVar) {
        d.e("TmsTcpManager", "handleSharkConfPush()");
        if (this.Ib == null) {
            pa.b("TmsTcpManager", "handleSharkConfPush() null == mIConfigOutlet", null, null);
            return null;
        } else if (eVar != null) {
            pa.b("TmsTcpManager", "hash : " + eVar.hash, null, null);
            pa.b("TmsTcpManager", "set hb interval and ports", null, null);
            if (eVar.interval > 0) {
                co(eVar.interval);
                this.Ib.ap(eVar.interval);
                pa.b("TmsTcpManager", "hb interval : " + eVar.interval, null, null);
            }
            if (eVar.l != null && eVar.l.size() > 0) {
                if (this.HE != null) {
                    this.HE.gJ();
                }
                Iterator it = eVar.l.iterator();
                while (it.hasNext()) {
                    Integer num = (Integer) it.next();
                    if (!(this.HE == null || num == null)) {
                        this.HE.cm(num.intValue());
                    }
                    pa.b("TmsTcpManager", "port : " + num, null, null);
                }
                this.Ib.k(eVar.l);
                pa.w("TmsTcpManager", "handleSharkConfPush() interval: " + eVar.interval + " ports.size(): " + eVar.l.size() + " hash: " + eVar.hash);
            } else {
                pa.w("TmsTcpManager", "handleSharkConfPush() interval: " + eVar.interval + " hash: " + eVar.hash);
            }
            if (eVar.n > 0) {
                HR = eVar.n;
                this.Ib.aq(eVar.n);
                pa.b("TmsTcpManager", "remain connection time : " + eVar.interval, null, null);
            }
            if (eVar.m != null) {
                this.Ib.l(eVar.m);
                pg.gB().a(this.Ib);
            }
            b bVar = new b();
            bVar.hash = eVar.hash;
            return new pl(Long.valueOf(j), Integer.valueOf(1101), bVar);
        } else {
            pa.b("TmsTcpManager", "handleSharkConfPush() scSharkConf == null", null, null);
            return null;
        }
    }

    private final void a(int i, Object obj, int i2, long j, boolean z) {
        if (this.Ii != null) {
            if (z) {
                this.Ii.removeMessages(i);
            }
            try {
                this.Ii.sendMessageDelayed(Message.obtain(this.Ii, i, i2, 0, obj), j);
            } catch (NullPointerException e) {
            }
        }
    }

    private void a(ow owVar, int i) {
        if (owVar != null) {
            pa.b("TmsTcpManager", "tcp fial, go to http. retCode : " + i, null, null);
            if (this.HM != null) {
                this.HM.a(owVar.Fm, owVar.Fn, i, owVar.data);
            }
            a(5, null, i, 0, true);
        }
    }

    private void a(tmsdkobf.pb.a aVar) {
        this.FR = new li() {
            final /* synthetic */ pk Im;

            {
                this.Im = r1;
            }

            public pl<Long, Integer, fs> a(int i, long j, int i2, fs fsVar) {
                if (fsVar != null) {
                    switch (i2) {
                        case 11101:
                            return this.Im.a(j, (e) fsVar);
                        default:
                            return null;
                    }
                }
                pa.b("TmsTcpManager", "onRecvPush() null == push", null, null);
                return null;
            }
        };
        aVar.a(0, 11101, new e(), 0, this.FR, false);
    }

    private final void cn(int i) {
        if (this.Ii != null) {
            try {
                this.Ii.removeMessages(i);
            } catch (NullPointerException e) {
            }
        }
    }

    public static pk gQ() {
        if (Il == null) {
            synchronized (Ik) {
                if (Il == null) {
                    Il = new pk();
                }
            }
        }
        return Il;
    }

    private int gV() {
        int i;
        if (this.HL.hs()) {
            i = 0;
        } else if (this.HL.hv()) {
            i = this.HL.hx();
            pa.b("TmsTcpManager", "open connection : " + i, null, null);
        } else {
            i = -220000;
            pa.b("TmsTcpManager", "open connection no network.", null, null);
        }
        int bX = oi.bX(i);
        switch (bX) {
            case -220000:
                this.HT = (byte) 0;
                break;
            case SpaceManager.ERROR_CODE_OK /*0*/:
                this.HT = (byte) 1;
                break;
            default:
                this.HT = (byte) 0;
                a(7, null, 0, HU, true);
                break;
        }
        this.Et.au(bX);
        return bX;
    }

    private boolean gY() {
        return this.Ih;
    }

    private final void gZ() {
        cn(9);
        cn(10);
        cn(6);
        cn(7);
    }

    private void ha() {
        M(false);
        v(null);
        hb();
    }

    private void hb() {
        cn(0);
        cn(1);
        cn(2);
        cn(3);
        cn(4);
        cn(5);
        cn(6);
        cn(7);
        cn(8);
        cn(9);
        cn(10);
        cn(11);
    }

    private void u(byte[] bArr) {
        if (bArr != null) {
            a(8, bArr, 0, 0, true);
        }
    }

    private void v(byte[] bArr) {
        cn(8);
        cn(6);
    }

    private void w(byte[] bArr) {
        a(6, null, 0, HW * 1000, false);
    }

    public void a(int i, Object obj) {
        switch (i) {
            case UrlCheckType.PRIVATE_SERVER /*11*/:
            case UrlCheckType.MSG_REACTIONARY /*12*/:
                if (this.HK != null && this.HK.get() > 0) {
                    a(7, null, 0, HU, true);
                    break;
                }
        }
        pa.w("TmsTcpManager", "handleCode Exception Code : " + i);
    }

    void a(long j, boolean z, boolean z2, byte[] bArr, tmsdkobf.ph.a aVar, pb.d dVar) {
        if (z && z2) {
            if (!fJ) {
                throw new AssertionError();
            }
        } else if (mu.fb()) {
            pa.b("TmsTcpManager", "could not connect", null, null);
            mj.bB(-10020);
            this.FS.a(true, -230000, null);
        } else {
            boolean hv = this.HL.hv();
            pa.a("TmsTcpManager", "isNetworkConnected : " + hv, null, null);
            if (hv) {
                if ((j < ((long) (HR * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY)) ? 1 : null) == null) {
                    gU();
                    mk.eU().a("set_remain_connect_action", j, new Runnable() {
                        final /* synthetic */ pk Im;

                        {
                            this.Im = r1;
                        }

                        public void run() {
                            this.Im.closeConnection();
                        }
                    });
                }
                M(true);
                boolean z3 = false;
                if (z || z2) {
                    z3 = true;
                }
                ow owVar = new ow(bArr, 32, z3, null, aVar, dVar);
                if (!(dVar == null || dVar.Gj == null || dVar.Gj.size() <= 0)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    Iterator it = dVar.Gj.iterator();
                    while (it.hasNext()) {
                        bm bmVar = (bm) it.next();
                        if (bmVar != null) {
                            stringBuilder.append("|cmdid|");
                            stringBuilder.append(bmVar.aZ);
                        }
                    }
                    pa.a("TmsTcpManager", stringBuilder.toString(), null, null);
                }
                if (this.Ig == 1 || z2) {
                    this.Ie.add(owVar);
                    a(0, null, 0, 0, true);
                } else if (this.Ig != 2) {
                    if (this.Ig == null) {
                        pa.b("TmsTcpManager", "fp not send, send fp, enqueue task", null, null);
                        a(2, owVar, 0, 0, false);
                        hd();
                    }
                } else if (z) {
                    pa.b("TmsTcpManager", "wait fp, ignore hb", null, null);
                } else {
                    pa.b("TmsTcpManager", "wait fp, enqueue task", null, null);
                    a(2, owVar, 0, 0, false);
                }
                return;
            }
            this.FS.a(true, -220000, null);
        }
    }

    public void a(om omVar, on onVar, pj pjVar, ph.b bVar, a aVar, or orVar, tmsdkobf.pb.a aVar2) {
        this.HE = pjVar;
        L(pjVar.ae());
        if (this.HL != null) {
            this.HL.co(HX);
            this.HL.a(pjVar);
            this.HL.a(onVar);
        }
        this.Ib = omVar;
        this.Et = onVar;
        pu.IE = onVar;
        this.FS = bVar;
        this.HM = orVar;
        this.Ij = aVar;
        a(aVar2);
        this.HJ = ps.t(this.context);
        if (this.HJ != null) {
            this.HJ.b(this);
        }
        if (this.Ib != null) {
            int aA = this.Ib.aA();
            if (aA > 0) {
                co(aA);
                pa.b("TmsTcpManager", "hb interval : " + aA, null, null);
            }
            aA = this.Ib.aD();
            if (aA > 0) {
                HR = aA;
                pa.b("TmsTcpManager", "remain connection (s) : " + HR, null, null);
            }
        }
    }

    void a(byte[] bArr, tmsdkobf.ph.a aVar, pb.d dVar) {
        if (mu.fb()) {
            pa.b("TmsTcpManager", "could not connect", null, null);
            mj.bB(-10020);
            this.FS.a(true, -230000, null);
        } else if (dVar.Gb) {
            a(-1, true, false, bArr, aVar, dVar);
        } else {
            M(true);
            this.Ie.add(new ow(bArr, 32, true, null, aVar, dVar));
            a(0, null, 0, 0, true);
        }
    }

    public void b(int i, byte[] bArr) {
        u(bArr);
        hf();
        this.FS.a(true, 0, bArr);
    }

    void closeConnection() {
        int decrementAndGet = this.HK.decrementAndGet();
        pa.b("TmsTcpManager", "decrease ref : " + decrementAndGet, null, null);
        if (decrementAndGet <= 0) {
            this.HK.set(0);
            pa.b("TmsTcpManager", "reset ref : " + this.HK.get(), null, null);
            a(4, null, 0, 0, true);
        }
    }

    public void cn() {
        a(10, null, 0, 3000, true);
    }

    public void co() {
        a(9, null, 0, 3000, true);
    }

    void co(int i) {
        this.HL.co(i);
    }

    synchronized void gR() {
        this.HL.a(true, this);
    }

    synchronized void gS() {
        this.HL.a(false, this);
    }

    public synchronized void gT() {
        gS();
        gR();
    }

    void gU() {
        if (this.HK.get() < 0) {
            this.HK.set(0);
        }
        pa.b("TmsTcpManager", "increase ref : " + this.HK.incrementAndGet(), null, null);
    }

    void gW() {
        this.HT = (byte) 0;
        this.Ig = (byte) 0;
        pa.b("TmsTcpManager", "close connection immediately", null, null);
        hf();
        gS();
        ha();
        this.Ie.clear();
        this.HL.close();
        this.HK.set(0);
    }

    int gX() {
        this.Ig = (byte) 0;
        pa.b("TmsTcpManager", "reconnect() connection state : " + this.HT, null, null);
        int gX = this.HL.gX();
        if (gX == 0) {
            this.HT = (byte) 1;
            pa.b("TmsTcpManager", "reconnect success : " + gX, null, null);
            hd();
        } else {
            pa.b("TmsTcpManager", "reconnect fail : " + gX, null, null);
            this.HT = (byte) 0;
            if (!(gX == -220000 || gX == -230000)) {
                a(7, null, 0, HU, true);
            }
        }
        this.Et.au(gX);
        return gX;
    }

    synchronized void gw() {
        pa.b("TmsTcpManager", "get couldNotConnect cmd", null, null);
        if (mu.fb()) {
            pa.b("TmsTcpManager", "could not connect", null, null);
            a(4, null, 0, 0, true);
        }
    }

    void hc() {
        bm bmVar = new bm();
        bmVar.aZ = 999;
        bmVar.dG = oz.gi().fP();
        ArrayList arrayList = new ArrayList();
        arrayList.add(bmVar);
        this.Ij.a(true, false, 0, arrayList, this.Ic);
    }

    void hd() {
        if (this.Ig == (byte) 1 || this.Ig == (byte) 2) {
            pa.b("TmsTcpManager", "sending or receive fp, no more send : firstPkgState : " + this.Ig, null, null);
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (Math.abs(currentTimeMillis - this.HN) >= HO) {
            this.HN = currentTimeMillis;
            this.Ig = (byte) 2;
            cn(11);
            pa.b("TmsTcpManager", "send fp", null, null);
            bm bmVar = new bm();
            bmVar.aZ = 997;
            bmVar.dG = oz.gi().fP();
            ArrayList arrayList = new ArrayList();
            arrayList.add(bmVar);
            this.Ij.a(false, true, 0, arrayList, this.Id);
            return;
        }
        pa.x("TmsTcpManager", "first pkg too frequency : firstPkgState : " + this.Ig);
        a(11, null, 0, HO, true);
    }

    public void he() {
        pa.b("TmsTcpManager", "send hb", null, null);
        hc();
    }

    void hf() {
        this.HZ = 0;
    }

    int hg() {
        int i = this.HZ + 1;
        this.HZ = i;
        return i;
    }

    int hh() {
        return 2;
    }
}
