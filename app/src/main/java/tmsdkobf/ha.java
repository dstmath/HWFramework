package tmsdkobf;

import android.util.Pair;
import java.util.List;
import tmsdk.common.tcc.DeepCleanEngine;
import tmsdk.common.tcc.DeepCleanEngine.Callback;
import tmsdk.common.tcc.QFile;
import tmsdk.common.tcc.SdcardScannerFactory;
import tmsdk.common.utils.d;
import tmsdk.common.utils.i;

/* compiled from: Unknown */
public class ha implements Callback {
    DeepCleanEngine oS;
    private List<String> pS;
    hb pT;
    hc pU;
    gp pV;
    boolean pW;
    int pX;
    private a pY;
    gw pZ;
    b qa;
    Pair<Integer, gx> qb;
    private boolean qc;
    int qd;
    int qe;

    /* compiled from: Unknown */
    public interface a {
        void a(int i, int i2, String str);

        void bn();
    }

    /* compiled from: Unknown */
    private class b {
        String pkgName;
        String qf;
        boolean qg;
        final /* synthetic */ ha qh;

        private b(ha haVar) {
            this.qh = haVar;
        }
    }

    public ha() {
        this.pX = 0;
        this.qc = false;
        this.qd = 0;
        this.qe = 0;
        this.pW = false;
        this.pS = gq.aT();
    }

    private b a(gw gwVar) {
        b bVar = new b();
        bVar.pkgName = this.pV.c(gwVar.bc());
        if (bVar.pkgName != null) {
            bVar.qf = this.pV.aM(bVar.pkgName);
            bVar.qg = false;
        } else {
            int d = this.pV.d(gwVar.bc());
            if (d != -1) {
                bVar.qf = gwVar.az(d);
            } else {
                bVar.qf = !hb.bo() ? "\u7591\u4f3c" + gwVar.az(0) : gwVar.az(0);
                d = 0;
            }
            bVar.pkgName = (String) gwVar.bc().get(d);
            bVar.qg = true;
        }
        return bVar;
    }

    private void a(String str, long j) {
        boolean bo = hb.bo();
        gt aW = this.pT.aW(str);
        if (aW != null) {
            if (aW.al() != 6) {
                if (aW.getStatus() == 0) {
                    String str2;
                    if (this.pS == null) {
                        str2 = null;
                    } else {
                        str2 = null;
                        for (String str3 : this.pS) {
                            String str32;
                            if (!str.startsWith(str32)) {
                                str32 = str2;
                            }
                            str2 = str32;
                        }
                    }
                    List<gm> br = this.pT.br();
                    if (!(br == null || this.oS == null || str2 == null)) {
                        for (gm gmVar : br) {
                            StringBuilder stringBuilder = new StringBuilder();
                            gm.a(stringBuilder, gmVar);
                            if (this.oS.isMatchComRule(str2, str, stringBuilder.toString())) {
                                aW.ps = true;
                                aW.setStatus(1);
                                break;
                            }
                        }
                    }
                }
                this.pU.b(aW);
            } else {
                gm gmVar2 = new gm();
                gmVar2.ph = false;
                gmVar2.mDescription = !bo ? i.dh("cn_broken_apk") : i.dh("eng_broken_apk");
                this.pU.a(gmVar2, str, j);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String a(gw gwVar, b bVar, boolean z) {
        if (gwVar == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(!bVar.qg ? "0;" : "1;");
        if (!bVar.qg || this.pV.aS()) {
            if (gwVar.op != null) {
                for (gv gvVar : gwVar.op) {
                    boolean z2;
                    if (z) {
                        if (bVar.qg) {
                            if (gvVar.pw == 3) {
                                z2 = false;
                                gv.a(stringBuilder, gvVar, bVar.qg, z2);
                            }
                        }
                    } else if (!(bVar.qg || gvVar.pw == 1 || gvVar.pw == 2)) {
                        z2 = false;
                        gv.a(stringBuilder, gvVar, bVar.qg, z2);
                    }
                    z2 = true;
                    gv.a(stringBuilder, gvVar, bVar.qg, z2);
                }
            }
        }
        return stringBuilder.toString();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public gx a(gv gvVar, b bVar, String str, String str2, long j) {
        boolean z;
        gx gxVar;
        if (!bVar.qg) {
            if (gvVar.pw != 1) {
                if (gvVar.pw != 2) {
                    return null;
                }
                z = true;
                gxVar = new gx();
                gxVar.pF = gvVar.mID;
                gxVar.pK = z;
                gxVar.e(str, str2);
                gxVar.mName = gvVar.mDescription;
                gxVar.mPkg = bVar.pkgName;
                gxVar.pG = bVar.qf;
                gxVar.mTotalSize = j;
                gxVar.pL = bVar.qg;
                if (z) {
                    gxVar.pE = 1;
                } else {
                    gxVar.pE = 0;
                }
                return gxVar;
            }
        }
        z = false;
        gxVar = new gx();
        gxVar.pF = gvVar.mID;
        gxVar.pK = z;
        gxVar.e(str, str2);
        gxVar.mName = gvVar.mDescription;
        gxVar.mPkg = bVar.pkgName;
        gxVar.pG = bVar.qf;
        gxVar.mTotalSize = j;
        gxVar.pL = bVar.qg;
        if (z) {
            gxVar.pE = 0;
        } else {
            gxVar.pE = 1;
        }
        return gxVar;
    }

    public void a(gp gpVar) {
        this.pV = gpVar;
    }

    public void a(a aVar) {
        this.pY = aVar;
    }

    public void a(hb hbVar) {
        this.pT = hbVar;
    }

    public void a(hc hcVar) {
        this.pU = hcVar;
    }

    public void bk() {
        int i = 0;
        if (this.oS == null) {
            d.g("DeepClean", "getDeepClean Engine ");
            this.oS = SdcardScannerFactory.getDeepCleanEngine(this);
            d.g("DeepClean", "getDeepClean Engine success");
        }
        if (this.oS != null) {
            List aU = gq.aU();
            if (aU != null) {
                this.pX = aU.size();
                this.oS.setWhitePaths(this.pT.bv());
                this.oS.setRootPaths(this.pT.bu());
                List bs = this.pT.bs();
                if (bs != null) {
                    String[] strArr = new String[bs.size()];
                    for (int i2 = 0; i2 < strArr.length; i2++) {
                        StringBuilder stringBuilder = new StringBuilder();
                        gm.a(stringBuilder, (gm) bs.get(i2));
                        strArr[i2] = stringBuilder.toString();
                    }
                    this.oS.setComRubRule(strArr);
                }
                List bt = this.pT.bt();
                if (bt != null) {
                    String[] strArr2 = new String[bt.size()];
                    while (i < strArr2.length) {
                        StringBuilder stringBuilder2 = new StringBuilder();
                        gm.a(stringBuilder2, (gm) bt.get(i));
                        strArr2[i] = stringBuilder2.toString();
                        i++;
                    }
                    this.oS.setOtherFilterRule(strArr2);
                }
                List<String> aT = gq.aT();
                if (aT != null) {
                    for (String str : aT) {
                        if (!this.pW) {
                            this.oS.scanPath(str, "/");
                            this.qd += this.qe;
                        }
                    }
                }
                this.oS.release();
                this.oS = null;
                System.currentTimeMillis();
                if (this.pW && this.pY != null) {
                    this.pY.bn();
                }
            }
        }
    }

    public void bm() {
        this.pW = true;
        if (this.oS != null) {
            this.oS.cancel();
        }
    }

    public String getDetailRule(String str) {
        gw aT = this.pT.aT(str);
        if (aT != null) {
            this.qa = a(aT);
            this.pZ = aT;
            return a(aT, this.qa, this.qc);
        }
        this.pZ = null;
        this.qa = null;
        return null;
    }

    public void onFoundComRubbish(String str, String str2, long j) {
        gm aU = this.pT.aU(str);
        if (aU != null) {
            d.g("DeepClean", "onFoundComRubbish " + aU.mDescription + " = " + str2);
            if (aU != this.pT.bq()) {
                this.pU.a(aU, str2, j);
            } else {
                a(str2, j);
            }
        }
    }

    public void onFoundEmptyDir(String str, long j) {
        String dh = !hb.bo() ? i.dh("cn_scan_item_empty_folders") : i.dh("eng_scan_item_empty_folders");
        gm gmVar = new gm();
        gmVar.mDescription = dh;
        gmVar.ph = false;
        this.pU.a(gmVar, str, j);
    }

    public void onFoundSoftRubbish(String str, String str2, String str3, long j) {
        gv gvVar = null;
        boolean bo = hb.bo();
        if (!this.pW) {
            try {
                int parseInt = Integer.parseInt(str);
                long j2 = j / 1000;
                int i = (int) (j % 1000);
                if (this.qb != null && ((Integer) this.qb.first).intValue() == parseInt) {
                    this.pU.a((gx) this.qb.second, i, str2, str3, j2);
                } else if (this.pZ != null && this.qa != null) {
                    if (parseInt == 0) {
                        if (this.pZ.pz == null) {
                            this.pZ.pz = new gv();
                            this.pZ.pz.mDescription = !bo ? i.dh("cn_deep_clean_other_rubbish") : i.dh("eng_deep_clean_other_rubbish");
                            this.pZ.pz.pw = 1;
                        }
                        gvVar = this.pZ.pz;
                    }
                    if (gvVar == null) {
                        gvVar = this.pZ.ax(parseInt);
                    }
                    if (gvVar != null) {
                        gx a = a(gvVar, this.qa, str2, str3, j2);
                        if (a != null) {
                            this.pU.a(a, i);
                            this.qb = new Pair(Integer.valueOf(parseInt), a);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    public void onProcessChange(int i) {
        if (this.pX != 0) {
            this.qe = i;
            int i2 = this.qd + this.qe;
            int i3 = (int) ((((float) i2) * 100.0f) / ((float) this.pX));
            if (i3 >= 100) {
                i3--;
            }
            d.g("DeepClean", "onProcessChange " + i2 + " / " + this.pX);
            if (this.pY != null) {
                this.pY.a(1, i3, null);
            }
        }
    }

    public void onVisit(QFile qFile) {
    }

    public void r(boolean z) {
        this.qc = z;
    }
}
