package tmsdkobf;

import android.content.Context;
import com.qq.taf.jce.JceStruct;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.e;
import tmsdk.common.utils.l;
import tmsdk.common.utils.l.a;
import tmsdk.common.utils.n;
import tmsdk.common.utils.q;
import tmsdkobf.nq.b;

public class gh extends nl {
    private static gh og = null;
    private boolean oc = true;
    private boolean od = false;
    private boolean oe = false;
    private boolean of = false;

    private gh() {
    }

    public static gh aA() {
        if (og == null) {
            Class cls = gh.class;
            synchronized (gh.class) {
                if (og == null) {
                    og = new gh();
                }
            }
        }
        return og;
    }

    public WeakReference<kd> a(int i, int i2, int i3, long j, long j2, int i4, JceStruct jceStruct, byte[] bArr, JceStruct jceStruct2, int i5, jy jyVar, jz jzVar, long j3, long j4) {
        return go.aU().b(i, i2, i3, j, j2, i4, jceStruct, bArr, jceStruct2, i5, jyVar, jzVar, j3, j4);
    }

    public void a(int i, long j, int i2, int i3, long j2, int i4, byte[] bArr, int i5, long j3, long j4, long j5) {
    }

    public void a(long j, int i, int i2) {
    }

    public void a(String str, boolean z) {
        if (str != null) {
            mb.n("SharkOutlet", "onSaveGuidToPhone() guid: " + str);
            gg.al().Z(str);
        }
    }

    public void a(HashMap<String, String> hashMap) {
    }

    public void a(boolean z, boolean z2, boolean z3) {
        this.oc = z;
        this.od = z2;
        this.oe = z3;
    }

    public boolean aB() {
        return this.oc;
    }

    public boolean aC() {
        return this.od;
    }

    public boolean aD() {
        return this.oe;
    }

    public b aE() {
        return gg.al().ap();
    }

    public String aF() {
        String as = gg.al().as();
        mb.n("SharkOutlet", "onGetGuidFromPhone() guid: " + as);
        return as;
    }

    public String aG() {
        return null;
    }

    public boolean aH() {
        return true;
    }

    public void aI() {
    }

    public String aJ() {
        return gg.al().aq();
    }

    public String aK() {
        return gg.al().ar();
    }

    public br aL() {
        mb.n("SharkOutlet", "onGetInfoSavedOfGuid()");
        return gg.al().av();
    }

    public long aM() {
        return -1;
    }

    public br aN() {
        Context applicaionContext = TMSDKContext.getApplicaionContext();
        String[] D = e.D(applicaionContext);
        long iV = l.iV();
        a aVar = new a();
        l.a(aVar);
        long j = aVar.LN;
        a aVar2 = new a();
        l.b(aVar2);
        long j2 = aVar2.LN;
        String N = l.N(applicaionContext);
        int i = 1;
        String str = "";
        if (im.bO() != null) {
            str = im.bO().getIMSI(1);
            i = 2;
        }
        String str2 = "";
        str2 = im.bO() == null ? l.M(applicaionContext) : im.bO().getIMSI(0);
        int Q = l.Q(applicaionContext);
        int R = l.R(applicaionContext);
        if (Q < R) {
            int i2 = Q;
            Q = R;
            R = i2;
        }
        br brVar = new br();
        brVar.dl = l.L(applicaionContext);
        brVar.imsi = str2;
        brVar.dU = str;
        if (N == null) {
            N = "";
        }
        brVar.dm = N;
        brVar.dn = "0";
        brVar.do = "0";
        brVar.dp = 13;
        brVar.dq = q.cI("19B7C7417A1AB190");
        brVar.L = 3059;
        brVar.dr = q.cI(im.bQ());
        brVar.ds = 2;
        brVar.dt = SmsCheckResult.ESCT_201;
        brVar.du = e.F(applicaionContext);
        try {
            brVar.dv = TMSDKContext.getApplicaionContext().getPackageName();
        } catch (Throwable th) {
        }
        brVar.dw = q.cI(l.iL());
        brVar.dx = n.iX();
        brVar.dy = q.cI(l.P(applicaionContext));
        brVar.dz = (short) 2052;
        brVar.dA = i;
        brVar.dB = D[2];
        brVar.ed = l.cE("ro.product.cpu.abi2");
        brVar.dC = e.iz();
        brVar.dD = e.iC();
        brVar.dE = Q + "*" + R;
        brVar.dF = iV;
        brVar.dG = e.iD();
        brVar.dH = j;
        brVar.ei = j2;
        brVar.dI = q.cI(l.iP());
        brVar.dJ = q.cI(l.iN());
        brVar.dK = q.cI(l.iO());
        brVar.version = "6.1.0";
        brVar.dY = 1;
        brVar.dZ = "";
        brVar.dN = l.iT();
        brVar.dQ = 0;
        brVar.dR = 0;
        brVar.ea = l.iQ();
        brVar.eb = l.iR();
        brVar.ec = l.cE("ro.build.product");
        brVar.ee = l.cE("ro.build.fingerprint");
        brVar.ef = l.cE("ro.product.locale.language");
        brVar.eg = l.cE("ro.product.locale.region");
        brVar.eh = l.getRadioVersion();
        brVar.dO = l.cE("ro.board.platform");
        brVar.ej = l.cE("ro.mediatek.platform");
        brVar.dP = l.cE("ro.sf.lcd_density");
        brVar.dL = l.cE("ro.product.name");
        brVar.dM = l.cE("ro.build.version.release");
        brVar.ek = l.iS();
        brVar.dS = false;
        brVar.el = 0;
        brVar.em = l.iU();
        brVar.en = l.S(true);
        brVar.eo = l.S(false);
        return brVar;
    }

    public long aO() {
        long au = gg.al().au();
        mb.n("SharkOutlet", "onGetGuidUpdateCheckTimeMillis() tm: " + au);
        return au;
    }

    public boolean aP() {
        return gg.al().ax();
    }

    public h aQ() {
        return gg.al().az();
    }

    public int aR() {
        return -1;
    }

    public String aS() {
        return gg.al().at();
    }

    public void aT() {
    }

    public nj.a ah(String str) {
        return gg.al().ag(str);
    }

    public void b(int i, int i2) {
    }

    public void b(String str, long j, List<String> list) {
        gg.al().a(str, j, list);
    }

    public void b(String str, boolean z) {
    }

    public void b(br brVar) {
        mb.n("SharkOutlet", "onSaveInfoOfGuid()");
        gg.al().a(brVar);
    }

    public void b(h hVar) {
        gg.al().a(hVar);
    }

    public void b(b bVar) {
        if (bVar != null) {
            gg.al().a(bVar);
        }
    }

    public void c(int i, int i2) {
    }

    public void c(String str, boolean z) {
        if (str != null) {
            gg.al().aa(str);
        }
    }

    public void d(int i, int i2) {
    }

    public void d(String str, boolean z) {
        if (str != null) {
            gg.al().ab(str);
        }
    }

    public void e(int i, int i2) {
    }

    public void f(boolean z) {
        gg.al().e(z);
    }

    public void g(long j) {
        mb.n("SharkOutlet", "onSaveGuidUpdateCheckTimeMillis() timeMillis: " + j);
        gg.al().f(j);
    }
}
