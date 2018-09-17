package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Iterator;
import tmsdk.common.utils.d;
import tmsdkobf.pb.b;

/* compiled from: Unknown */
public class ol {
    private boolean EA;
    private pb Ew;
    private volatile boolean Ex;
    private volatile String Ey;
    private volatile long Ez;
    private final String TAG;
    private Context mContext;

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.ol.1 */
    class AnonymousClass1 implements b {
        final /* synthetic */ a EB;
        final /* synthetic */ bj EC;
        final /* synthetic */ ol ED;

        AnonymousClass1(ol olVar, a aVar, bj bjVar) {
            this.ED = olVar;
            this.EB = aVar;
            this.EC = bjVar;
        }

        public void a(boolean z, int i, int i2, ArrayList<bq> arrayList) {
            if (i != 0) {
                d.c("GuidCertifier", "checkGuid() ESharkCode.ERR_NONE != retCode, retCode: " + i);
                this.EB.c(false, null);
            } else if (arrayList == null) {
                this.EB.c(false, null);
            } else if (arrayList.size() > 0) {
                bq bqVar = (bq) arrayList.get(0);
                if (bqVar != null) {
                    d.e("GuidCertifier", "checkGuid() rs.seqNo: " + bqVar.dG + " rs.cmd: " + bqVar.aZ + " rs.retCode: " + bqVar.dJ + " rs.dataRetCode: " + bqVar.dK);
                    if (1 == bqVar.dJ) {
                        d.c("GuidCertifier", "checkGuid() \u63a5\u5165\u5c42\u5931\u8d25\u4e86");
                        this.EB.c(false, null);
                        return;
                    } else if (bqVar.dK == 0) {
                        byte[] bArr = bqVar.data;
                        if (bArr != null) {
                            d.e("GuidCertifier", "checkGuid() rs.data.length: " + bqVar.data.length);
                            try {
                                fs a = ok.a(this.ED.mContext, this.ED.Ew.gg().Ft.getBytes(), bArr, new bo(), false);
                                if (a != null) {
                                    this.ED.a(((bo) a).r, this.EC);
                                    d.d("GuidCertifier", "checkGuid() ret.guid mGuid: " + this.ED.Ey);
                                    this.EB.c(true, null);
                                    return;
                                }
                                d.c("GuidCertifier", "checkGuid() null == js");
                                this.EB.c(false, null);
                                return;
                            } catch (Exception e) {
                                d.c("GuidCertifier", "checkGuid() convert failed");
                                this.EB.c(false, null);
                                return;
                            }
                        }
                        this.EB.c(false, null);
                        return;
                    } else {
                        d.c("GuidCertifier", "checkGuid() \u4e1a\u52a1\u5c42\u5931\u8d25\u4e86");
                        this.EB.c(false, null);
                        return;
                    }
                }
                this.EB.c(false, null);
            } else {
                this.EB.c(false, null);
            }
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.ol.2 */
    class AnonymousClass2 implements b {
        final /* synthetic */ bj EC;
        final /* synthetic */ ol ED;

        AnonymousClass2(ol olVar, bj bjVar) {
            this.ED = olVar;
            this.EC = bjVar;
        }

        public void a(boolean z, int i, int i2, ArrayList<bq> arrayList) {
            d.e("GuidCertifier", "updateGuid() retCode: " + i);
            if (i != 0) {
                d.c("GuidCertifier", "updateGuid() ESharkCode.ERR_NONE != retCode, retCode: " + i);
            } else if (arrayList != null && arrayList.size() > 0) {
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    bq bqVar = (bq) it.next();
                    if (bqVar == null) {
                        return;
                    }
                    if (10002 == bqVar.aZ) {
                        d.e("GuidCertifier", "updateGuid() rs.seqNo: " + bqVar.dG + "rs.cmd" + bqVar.aZ + " rs.retCode: " + bqVar.dJ + " rs.dataRetCode: " + bqVar.dK);
                        if (1 != bqVar.dJ && bqVar.dJ == 0) {
                            d.d("GuidCertifier", "updateGuid() succed, mGuid: " + this.ED.Ey);
                            this.ED.a(this.ED.Ey, this.EC);
                        } else {
                            return;
                        }
                    }
                    d.e("GuidCertifier", "updateGuid() rs.cmd: " + bqVar.aZ);
                }
            }
        }
    }

    /* compiled from: Unknown */
    public interface a {
        void c(boolean z, String str);
    }

    public ol(Context context, pb pbVar) {
        this.TAG = "GuidCertifier";
        this.Ex = false;
        this.Ez = 0;
        this.EA = false;
        this.mContext = context;
        this.Ew = pbVar;
        if (this.Ew.gm().aF() == this.EA) {
            pa.b("ocean", "[ocean]common: guid is ok", null, null);
        } else {
            pa.b("ocean", "[ocean]common: clean guid", null, null);
            this.Ew.gm().az("");
            this.Ew.gm().aA("");
        }
        fM();
    }

    private bj H(boolean z) {
        if (!fN() && !z) {
            return null;
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (!mp.a(currentTimeMillis, this.Ew.gm().az(), 720) && !z) {
            return null;
        }
        this.Ew.gm().h(currentTimeMillis);
        bj fO = fO();
        bj aw = this.Ew.gm().aw();
        if (fO == null || aw == null) {
            d.c("GuidCertifier", "needUpdateInfoOfGuid() null == realInfo || null == savedInfo");
            return null;
        }
        d.d("GuidCertifier", "needUpdateInfoOfGuid() printCSRegist() savedInfo: ");
        d(aw);
        return (c(fO.di, aw.di) | ((((((((((((((((((((((((((((((((((((((((((((((((((v(fO.cC, aw.cC) | 0) | v(fO.imsi, aw.imsi)) | v(fO.dk, aw.dk)) | v(fO.cD, aw.cD)) | v(fO.cE, aw.cE)) | v(fO.cF, aw.cF)) | s(fO.product, aw.product)) | v(fO.cG, aw.cG)) | s(fO.u, aw.u)) | v(fO.cH, aw.cH)) | s(fO.cI, aw.cI)) | s(fO.cJ, aw.cJ)) | c(fO.cK, aw.cK)) | v(fO.cL, aw.cL)) | v(fO.cM, aw.cM)) | s(fO.cN, aw.cN)) | v(fO.cO, aw.cO)) | s(fO.cP, aw.cP)) | s(fO.cQ, aw.cQ)) | v(fO.cR, aw.cR)) | v(fO.dt, aw.dt)) | v(fO.cS, aw.cS)) | s(fO.cT, aw.cT)) | v(fO.cU, aw.cU)) | c(fO.cV, aw.cV)) | c(fO.cW, aw.cW)) | c(fO.cX, aw.cX)) | c(fO.dy, aw.dy)) | v(fO.cY, aw.cY)) | v(fO.cZ, aw.cZ)) | v(fO.da, aw.da)) | v(fO.version, aw.version)) | s(fO.do, aw.do)) | v(fO.dp, aw.dp)) | v(fO.dd, aw.dd)) | s(fO.dg, aw.dg)) | s(fO.dh, aw.dh)) | v(fO.dq, aw.dq)) | v(fO.dr, aw.dr)) | v(fO.ds, aw.ds)) | v(fO.du, aw.du)) | v(fO.dv, aw.dv)) | v(fO.dw, aw.dw)) | v(fO.dx, aw.dx)) | v(fO.de, aw.de)) | v(fO.dz, aw.dz)) | v(fO.df, aw.df)) | v(fO.db, aw.db)) | v(fO.dc, aw.dc)) | v(fO.dA, aw.dA))) != 0 ? fO : null;
    }

    private void a(String str, bj bjVar) {
        if (!TextUtils.isEmpty(str)) {
            d.d("GuidCertifier", "saveGuid:[" + str + "]");
            this.Ey = str;
            this.Ex = true;
            this.Ew.gm().o(this.EA);
            this.Ew.gm().az(str);
            this.Ew.gm().aA(str);
            this.Ew.gm().b(bjVar);
        }
    }

    private void c(bj bjVar) {
        d.e("GuidCertifier", "updateGuid() mGuid: " + this.Ey);
        d.e("GuidCertifier", "updateGuid() encodeKey: " + this.Ew.gg());
        fs e = e(bjVar);
        bm bmVar = new bm();
        bmVar.dG = oz.gi().fP();
        bmVar.aZ = 2;
        bmVar.data = ok.c(e);
        if (bmVar.data != null && bmVar.data.length > 0) {
            d.d("GuidCertifier", "updateGuid() printCSRegist()");
            d(e.dC);
            ArrayList arrayList = new ArrayList();
            arrayList.add(bmVar);
            this.Ew.a(0, false, arrayList, new AnonymousClass2(this, bjVar));
            return;
        }
        d.c("GuidCertifier", "updateGuid() data:[" + bmVar.data + "]");
    }

    private boolean c(long j, long j2) {
        return j != j2;
    }

    private boolean c(boolean z, boolean z2) {
        return z != z2;
    }

    private void d(tmsdkobf.bj r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ol.d(tmsdkobf.bj):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ol.d(tmsdkobf.bj):void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ol.d(tmsdkobf.bj):void");
    }

    private bk e(bj bjVar) {
        bk bkVar = new bk();
        bkVar.dC = bjVar;
        bkVar.dD = c();
        return bkVar;
    }

    private boolean fN() {
        long currentTimeMillis = System.currentTimeMillis();
        if (!mp.a(currentTimeMillis, this.Ez, 60)) {
            return false;
        }
        this.Ez = currentTimeMillis;
        return true;
    }

    private bj fO() {
        bj ay = this.Ew.gm().ay();
        if (ay != null) {
            if (ay.cC == null) {
                ay.cC = "";
            }
            return ay;
        }
        throw new RuntimeException("reqRegist is null");
    }

    private boolean s(int i, int i2) {
        return i != i2;
    }

    private boolean v(String str, String str2) {
        boolean z = false;
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        if (TextUtils.isEmpty(str2)) {
            return true;
        }
        if (!str.equals(str2)) {
            z = true;
        }
        return z;
    }

    public void G(boolean z) {
        boolean z2 = false;
        if (!fL()) {
            bj H = H(z);
            String str = "GuidCertifier";
            StringBuilder append = new StringBuilder().append("checUpdateGuid() forceCheck: ").append(z).append(" need: ");
            if (H != null) {
                z2 = true;
            }
            d.e(str, append.append(z2).toString());
            if (H != null) {
                c(H);
            }
        }
    }

    public void a(a aVar) {
        d.e("GuidCertifier", "checkGuid()");
        if (fL()) {
            d.e("GuidCertifier", "checkGuid() encodeKey: " + this.Ew.gg());
            fs fO = fO();
            bm bmVar = new bm();
            bmVar.dG = oz.gi().fP();
            bmVar.aZ = 1;
            bmVar.data = ok.c(fO);
            if (bmVar.data != null && bmVar.data.length > 0) {
                d.d("GuidCertifier", "checkGuid() printCSRegist()");
                d(fO);
                ArrayList arrayList = new ArrayList();
                arrayList.add(bmVar);
                this.Ew.b(arrayList, new AnonymousClass1(this, aVar, fO));
                return;
            }
            d.c("GuidCertifier", "checkGuid() data:[" + bmVar.data + "]");
            return;
        }
        d.e("GuidCertifier", "checkGuid() !need, mGuid: " + this.Ey);
    }

    public String c() {
        return this.Ey != null ? this.Ey : "";
    }

    public boolean fL() {
        if (TextUtils.isEmpty(c()) || !this.Ex) {
            return true;
        }
        d.d("GuidCertifier", "checkDoRegist() \u5df2\u7ecf\u6ce8\u518c\u8fc7.");
        return false;
    }

    public void fM() {
        d.d("GuidCertifier", "refreshGuid()");
        this.Ey = this.Ew.gm().as();
        if (TextUtils.isEmpty(this.Ey)) {
            this.Ex = false;
        } else {
            this.Ex = true;
        }
    }
}
