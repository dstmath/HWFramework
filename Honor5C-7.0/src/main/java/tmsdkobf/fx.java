package tmsdkobf;

import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class fx {
    private static int VERSION;
    private static fx ns;
    private lf nq;
    private hp nr;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.fx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.fx.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.fx.<clinit>():void");
    }

    private fx() {
        this.nq = fw.w().y();
        this.nr = new hp();
        P();
    }

    public static fx O() {
        if (ns == null) {
            synchronized (fx.class) {
                if (ns == null) {
                    ns = new fx();
                }
            }
        }
        return ns;
    }

    private void P() {
        if (this.nq != null) {
            if (this.nq.getInt("key_shark_dao_ver", -1) < VERSION) {
                a(ad());
            }
            this.nq.e("key_shark_dao_ver", VERSION);
        }
    }

    private long an(String str) {
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean ao(String str) {
        try {
            return Boolean.parseBoolean(str);
        } catch (Exception e) {
            return false;
        }
    }

    private int ap(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean aq(String str) {
        try {
            return Boolean.parseBoolean(str);
        } catch (Exception e) {
            return false;
        }
    }

    public int Q() {
        return this.nq.getInt("key_tcp_k_a_t", -1);
    }

    public String R() {
        return this.nq.getString("key_tcp_ctrl", null);
    }

    public String S() {
        return ls.c(TMSDKContext.getApplicaionContext(), this.nq.getString("key_ek", ""));
    }

    public String T() {
        return ls.c(TMSDKContext.getApplicaionContext(), this.nq.getString("key_sid", ""));
    }

    public String U() {
        return ls.c(TMSDKContext.getApplicaionContext(), this.nq.getString("key_gd", ""));
    }

    public long V() {
        try {
            return Long.parseLong(ls.c(TMSDKContext.getApplicaionContext(), this.nq.getString("key_gd_ck_tm", "")));
        } catch (Exception e) {
            return 0;
        }
    }

    public int W() {
        try {
            return Integer.parseInt(ls.c(TMSDKContext.getApplicaionContext(), this.nq.getString("key_ha", "")));
        } catch (Exception e) {
            return 0;
        }
    }

    public int X() {
        try {
            return Integer.parseInt(ls.c(TMSDKContext.getApplicaionContext(), this.nq.getString("key_ha_sn", "")));
        } catch (Exception e) {
            return 0;
        }
    }

    public long Y() {
        try {
            return Long.parseLong(ls.c(TMSDKContext.getApplicaionContext(), this.nq.getString("key_lt_tm", "")));
        } catch (Exception e) {
            return 0;
        }
    }

    public int Z() {
        return ap(this.nr.aD(8));
    }

    public void a(bj bjVar) {
        try {
            this.nr.a(10000, bjVar.toByteArray("UTF-8"));
        } catch (Throwable th) {
            d.c("SharkDao", th);
        }
    }

    public int aa() {
        return ap(this.nr.aD(6));
    }

    public String ab() {
        return this.nr.aD(2);
    }

    public bj ac() {
        bj bjVar = new bj();
        try {
            fq fqVar = new fq(this.nr.aE(10000));
            fqVar.ae("UTF-8");
            bjVar.readFrom(fqVar);
        } catch (Throwable th) {
            d.c("SharkDao", th);
        }
        return bjVar;
    }

    @Deprecated
    public bj ad() {
        bj bjVar = new bj();
        bjVar.cC = this.nr.aD(1);
        if (bjVar.cC == null) {
            bjVar.cC = "";
        }
        bjVar.imsi = this.nr.aD(2);
        bjVar.dk = this.nr.aD(32);
        bjVar.cD = this.nr.aD(3);
        bjVar.cE = this.nr.aD(4);
        bjVar.cF = this.nr.aD(5);
        bjVar.product = ap(this.nr.aD(6));
        bjVar.cG = this.nr.aD(7);
        bjVar.u = ap(this.nr.aD(8));
        bjVar.cH = this.nr.aD(9);
        bjVar.cI = ap(this.nr.aD(10));
        bjVar.cJ = ap(this.nr.aD(11));
        bjVar.cK = aq(this.nr.aD(12));
        bjVar.cL = this.nr.aD(13);
        bjVar.cM = this.nr.aD(14);
        bjVar.cN = ap(this.nr.aD(15));
        bjVar.cO = this.nr.aD(16);
        bjVar.cP = (short) ((short) ap(this.nr.aD(17)));
        bjVar.cQ = ap(this.nr.aD(18));
        bjVar.cR = this.nr.aD(19);
        bjVar.dt = this.nr.aD(36);
        bjVar.cS = this.nr.aD(20);
        bjVar.cT = ap(this.nr.aD(21));
        bjVar.cU = this.nr.aD(22);
        bjVar.cV = an(this.nr.aD(23));
        bjVar.cW = an(this.nr.aD(24));
        bjVar.cX = an(this.nr.aD(25));
        bjVar.dy = an(this.nr.aD(41));
        bjVar.cY = this.nr.aD(26);
        bjVar.cZ = this.nr.aD(27);
        bjVar.da = this.nr.aD(28);
        bjVar.version = this.nr.aD(29);
        bjVar.do = ap(this.nr.aD(30));
        bjVar.dp = this.nr.aD(31);
        bjVar.dd = this.nr.aD(44);
        bjVar.dg = this.nr.d(45, -1);
        bjVar.dh = this.nr.d(46, -1);
        bjVar.dq = this.nr.aD(33);
        bjVar.dr = this.nr.aD(34);
        bjVar.ds = this.nr.aD(35);
        bjVar.du = this.nr.aD(37);
        bjVar.dv = this.nr.aD(38);
        bjVar.dw = this.nr.aD(39);
        bjVar.dx = this.nr.aD(40);
        bjVar.de = this.nr.aD(50);
        bjVar.dz = this.nr.aD(42);
        bjVar.df = this.nr.aD(47);
        bjVar.db = this.nr.aD(48);
        bjVar.dc = this.nr.aD(49);
        bjVar.dA = this.nr.aD(43);
        bjVar.di = ao(this.nr.aD(51));
        bjVar.dB = ap(this.nr.aD(52));
        return bjVar;
    }

    public boolean ae() {
        return aq(this.nr.aD(CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY));
    }

    public int af() {
        try {
            return Integer.parseInt(ls.c(TMSDKContext.getApplicaionContext(), this.nq.getString("key_itsc", "")));
        } catch (Exception e) {
            return 0;
        }
    }

    public String ag() {
        return ls.c(TMSDKContext.getApplicaionContext(), this.nq.getString("key_pl", ""));
    }

    public String ah() {
        return ls.c(TMSDKContext.getApplicaionContext(), this.nq.getString("key_mc", ""));
    }

    public String ai() {
        return ls.c(TMSDKContext.getApplicaionContext(), this.nq.getString("key_nu", ""));
    }

    public String aj() {
        return ls.c(TMSDKContext.getApplicaionContext(), this.nq.getString("key_tc", ""));
    }

    public void aj(int i) {
        this.nq.e("key_tcp_k_a_t", i);
    }

    public void aj(String str) {
        this.nq.m("key_tcp_ctrl", str);
    }

    public long ak() {
        try {
            return Long.parseLong(ls.c(TMSDKContext.getApplicaionContext(), this.nq.getString("key_r_t", "")));
        } catch (Exception e) {
            return 0;
        }
    }

    public void ak(int i) {
        String b = ls.b(TMSDKContext.getApplicaionContext(), Integer.toString(i));
        if (b != null) {
            this.nq.m("key_ha", b);
        }
    }

    public void ak(String str) {
        String b = ls.b(TMSDKContext.getApplicaionContext(), str);
        if (b != null) {
            this.nq.m("key_ek", b);
        }
    }

    public void al(int i) {
        String b = ls.b(TMSDKContext.getApplicaionContext(), Integer.toString(i));
        if (b != null) {
            this.nq.m("key_ha_sn", b);
        }
    }

    public void al(String str) {
        String b = ls.b(TMSDKContext.getApplicaionContext(), str);
        if (b != null) {
            this.nq.m("key_sid", b);
        }
    }

    public void am(int i) {
        String b = ls.b(TMSDKContext.getApplicaionContext(), Integer.toString(i));
        if (b != null) {
            this.nq.m("key_itsc", b);
        }
    }

    public void am(String str) {
        String b = ls.b(TMSDKContext.getApplicaionContext(), str);
        if (b != null) {
            this.nq.m("key_gd", b);
        }
    }

    public void ar(String str) {
        String b = ls.b(TMSDKContext.getApplicaionContext(), str);
        if (b != null) {
            this.nq.m("key_pl", b);
        }
    }

    public void as(String str) {
        String b = ls.b(TMSDKContext.getApplicaionContext(), str);
        if (b != null) {
            this.nq.m("key_mc", b);
        }
    }

    public void at(String str) {
        String b = ls.b(TMSDKContext.getApplicaionContext(), str);
        if (b != null) {
            this.nq.m("key_nu", b);
        }
    }

    public void au(String str) {
        String b = ls.b(TMSDKContext.getApplicaionContext(), str);
        if (b != null) {
            this.nq.m("key_tc", b);
        }
    }

    public void av(String str) {
        String b = ls.b(TMSDKContext.getApplicaionContext(), str);
        if (b != null) {
            this.nq.m("key_cn_t_a", b);
        }
    }

    public void aw(String str) {
        String b = ls.b(TMSDKContext.getApplicaionContext(), str);
        if (b != null) {
            this.nq.m("key_cn_t_a_t", b);
        }
    }

    public void f(long j) {
        String b = ls.b(TMSDKContext.getApplicaionContext(), Long.toString(j));
        if (b != null) {
            this.nq.m("key_gd_ck_tm", b);
        }
    }

    public void g(long j) {
        String b = ls.b(TMSDKContext.getApplicaionContext(), Long.toString(j));
        if (b != null) {
            this.nq.m("key_lt_tm", b);
        }
    }

    public void n(boolean z) {
        this.nr.b(CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY, Boolean.toString(z));
    }
}
