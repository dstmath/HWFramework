package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.Iterator;
import tmsdkobf.nw.b;

public class ni {
    private boolean CX = false;
    private nw Dm;
    private volatile boolean Dn = false;
    private volatile String Do = "";
    private volatile long Dp = 0;
    private Context mContext;

    public interface a {
        void a(int i, int i2, int i3, String str);
    }

    public ni(Context context, nw nwVar, boolean z) {
        this.mContext = context;
        this.Dm = nwVar;
        this.CX = z;
        CharSequence aF = this.Dm.gl().aF();
        boolean aP = this.Dm.gl().aP();
        if (this.CX == aP || TextUtils.isEmpty(aF)) {
            mb.n("GuidCertifier", "[cu_guid]GuidCertifier: no need to clean guid");
        } else {
            mb.n("GuidCertifier", "[cu_guid]GuidCertifier, clean guid for server change(isTest?): " + aP + " -> " + this.CX);
            this.Dm.gl().a("", false);
            this.Dm.gl().b("", false);
        }
        fC();
    }

    public static void a(Context context, int i, String str) {
        try {
            Intent intent = new Intent(String.format("action.guid.got:%s", new Object[]{context.getPackageName()}));
            intent.putExtra("k.rc", i);
            intent.putExtra("k.g", str);
            context.sendBroadcast(intent);
        } catch (Throwable th) {
            mb.b("GuidCertifier", "[cu_guid]sendBroadcast(): " + th, th);
        }
    }

    private void a(String str, br brVar, boolean z) {
        if (!TextUtils.isEmpty(str)) {
            this.Do = str == null ? "" : str;
            this.Dn = true;
            this.Dm.gl().f(this.CX);
            this.Dm.gl().a(str, true);
            this.Dm.gl().b(str, true);
            this.Dm.gl().b(brVar);
        }
    }

    private void a(final br brVar, String str) {
        mb.n("GuidCertifier", "[cu_guid]updateGuid(), for: " + this.Do);
        final int fP = ns.fW().fP();
        JceStruct b = b(brVar, str);
        bw bwVar = new bw();
        bwVar.ey = fP;
        bwVar.bz = 2;
        bwVar.data = nh.a(this.mContext, b, 2, bwVar);
        if (bwVar.data != null) {
            mb.n("GuidCertifier", "[cu_guid]updateGuid(), cur info: " + c(brVar));
            ArrayList arrayList = new ArrayList();
            arrayList.add(bwVar);
            nt.ga().a(bwVar.ey, -1, null);
            this.Dm.a(0, 0, false, arrayList, new b() {
                public void a(boolean z, int i, int i2, ArrayList<ce> arrayList) {
                    mb.d("GuidCertifier", "updateGuid() retCode: " + i);
                    if (i == 0) {
                        int i3 = -21250000;
                        if (arrayList != null && arrayList.size() > 0) {
                            Iterator it = arrayList.iterator();
                            while (it.hasNext()) {
                                ce ceVar = (ce) it.next();
                                if (ceVar != null && 10002 == ceVar.bz) {
                                    if (ceVar.eB != 0) {
                                        mb.o("GuidCertifier", "[cu_guid]updateGuid(), mazu error: " + ceVar.eB);
                                        i3 = ceVar.eB;
                                    } else if (ceVar.eC == 0) {
                                        i3 = 0;
                                        mb.d("GuidCertifier", "[cu_guid]updateGuid(), succ, save info to db, mGuid: " + ni.this.Do);
                                        ni.this.a(ni.this.Do, brVar, false);
                                    } else {
                                        mb.o("GuidCertifier", "[cu_guid]updateGuid(), dataRetCode: " + ceVar.eC);
                                        i3 = -21300000;
                                    }
                                }
                            }
                        } else {
                            mb.o("GuidCertifier", "[cu_guid]updateGuid(), no sashimi, serverSashimis: " + arrayList);
                            i3 = -21250000;
                        }
                        nt.ga().a("GuidCertifier", 10002, fP, null, 30, i3);
                        nt.ga().bq(fP);
                        return;
                    }
                    mb.o("GuidCertifier", "[cu_guid]updateGuid() ESharkCode.ERR_NONE != retCode, retCode: " + i);
                    nt.ga().a("GuidCertifier", 10002, fP, null, 30, i);
                    nt.ga().bq(fP);
                }
            });
            return;
        }
        mb.s("GuidCertifier", "[cu_guid]updateGuid(), jceStruct2DataForSend failed");
    }

    private br b(boolean z, String str) {
        int i = 0;
        if (fA()) {
            mb.n("GuidCertifier", "[cu_guid]getCurInfoOfGuidIfNeed(), should register, donnot update, mGuid: " + this.Do + " fromPhone: " + this.Dn);
            return null;
        } else if (!w(z)) {
            return null;
        } else {
            br fD = fD();
            if (fD == null) {
                mb.s("GuidCertifier", "[cu_guid]getCurInfoOfGuidIfNeed(), null == realInfo");
                return null;
            } else if (TextUtils.isEmpty(str)) {
                br aL = this.Dm.gl().aL();
                if (aL != null) {
                    int w = ((((((((((((((((((((((((((((((((((((((((((((((((((((w(fD.dl, aL.dl) | 0) | w(fD.imsi, aL.imsi)) | w(fD.dU, aL.dU)) | w(fD.dm, aL.dm)) | w(fD.dn, aL.dn)) | w(fD.do, aL.do)) | u(fD.dp, aL.dp)) | w(fD.dq, aL.dq)) | u(fD.L, aL.L)) | w(fD.dr, aL.dr)) | u(fD.ds, aL.ds)) | u(fD.dt, aL.dt)) | b(fD.du, aL.du)) | w(fD.dv, aL.dv)) | w(fD.dw, aL.dw)) | u(fD.dx, aL.dx)) | w(fD.dy, aL.dy)) | u(fD.dz, aL.dz)) | u(fD.dA, aL.dA)) | w(fD.dB, aL.dB)) | w(fD.ed, aL.ed)) | w(fD.dC, aL.dC)) | u(fD.dD, aL.dD)) | w(fD.dE, aL.dE)) | c(fD.dF, aL.dF)) | c(fD.dG, aL.dG)) | c(fD.dH, aL.dH)) | c(fD.ei, aL.ei)) | w(fD.dI, aL.dI)) | w(fD.dJ, aL.dJ)) | w(fD.dK, aL.dK)) | w(fD.version, aL.version)) | u(fD.dY, aL.dY)) | w(fD.dZ, aL.dZ)) | w(fD.dN, aL.dN)) | w(fD.ea, aL.ea)) | w(fD.eb, aL.eb)) | w(fD.ec, aL.ec)) | w(fD.ee, aL.ee)) | w(fD.ef, aL.ef)) | w(fD.eg, aL.eg)) | w(fD.eh, aL.eh)) | w(fD.dO, aL.dO)) | w(fD.ej, aL.ej)) | w(fD.dP, aL.dP)) | w(fD.dL, aL.dL)) | w(fD.dM, aL.dM)) | w(fD.ek, aL.ek)) | b(fD.dS, aL.dS)) | u(fD.el, aL.el)) | w(fD.em, aL.em)) | w(fD.en, aL.en)) | w(fD.eo, aL.eo);
                    Object aG = this.Dm.gl().aG();
                    String b = b();
                    if (!(TextUtils.isEmpty(aG) || aG.equals(b))) {
                        i = 1;
                    }
                    if ((w | i) != 0) {
                        mb.r("GuidCertifier", "[cu_guid]getCurInfoOfGuidIfNeed(), yes, |savedInfo|" + c(aL));
                        mb.r("GuidCertifier", "[cu_guid]getCurInfoOfGuidIfNeed(), yes, |realInfo|" + c(fD));
                        return fD;
                    }
                    mb.n("GuidCertifier", "[cu_guid]getCurInfoOfGuidIfNeed(), info not changed, no need");
                    return null;
                }
                mb.s("GuidCertifier", "[cu_guid]getCurInfoOfGuidIfNeed(), null == savedInfo");
                return null;
            } else {
                mb.s("GuidCertifier", "[cu_guid_p]getCurInfoOfGuidIfNeed(), refreshKey is not empty, server requires update guid: " + str);
                return fD;
            }
        }
    }

    private bt b(br brVar, String str) {
        bt btVar = new bt();
        btVar.eq = brVar;
        btVar.er = b();
        btVar.es = this.Dm.gl().aG();
        btVar.et = str;
        mb.n("GuidCertifier", "[cu_guid_p]getCSUpdateRegist(), sdGuid: " + btVar.es + " curGuid: " + btVar.er + " refreshKey: " + str);
        return btVar;
    }

    private boolean b(boolean z, boolean z2) {
        return z != z2;
    }

    private String c(br brVar) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("|imei|" + brVar.dl);
        stringBuilder.append("|imsi|" + brVar.imsi);
        stringBuilder.append("|imsi_2|" + brVar.dU);
        stringBuilder.append("|mac|" + brVar.dm);
        stringBuilder.append("|qq|" + brVar.dn);
        stringBuilder.append("|phone|" + brVar.do);
        stringBuilder.append("|product|" + brVar.dp);
        stringBuilder.append("|lc|" + brVar.dq);
        stringBuilder.append("|buildno|" + brVar.L);
        stringBuilder.append("|channelid|" + brVar.dr);
        stringBuilder.append("|platform|" + brVar.ds);
        stringBuilder.append("|subplatform|" + brVar.dt);
        stringBuilder.append("|isbuildin|" + brVar.du);
        stringBuilder.append("|pkgname|" + brVar.dv);
        stringBuilder.append("|ua|" + brVar.dw);
        stringBuilder.append("|sdkver|" + brVar.dx);
        stringBuilder.append("|androidid|" + brVar.dy);
        stringBuilder.append("|lang|" + brVar.dz);
        stringBuilder.append("|simnum|" + brVar.dA);
        stringBuilder.append("|cpu|" + brVar.dB);
        stringBuilder.append("|cpu_abi2|" + brVar.ed);
        stringBuilder.append("|cpufreq|" + brVar.dC);
        stringBuilder.append("|cpunum|" + brVar.dD);
        stringBuilder.append("|resolution|" + brVar.dE);
        stringBuilder.append("|ram|" + brVar.dF);
        stringBuilder.append("|rom|" + brVar.dG);
        stringBuilder.append("|sdcard|" + brVar.dH);
        stringBuilder.append("|inner_storage|" + brVar.ei);
        stringBuilder.append("|build_brand|" + brVar.dI);
        stringBuilder.append("|build_version_incremental|" + brVar.dJ);
        stringBuilder.append("|build_version_release|" + brVar.dK);
        stringBuilder.append("|version|" + brVar.version);
        stringBuilder.append("|extSdkVer|" + brVar.dY);
        stringBuilder.append("|pkgkey|" + brVar.dZ);
        stringBuilder.append("|manufactory|" + brVar.dN);
        stringBuilder.append("|cam_pix|" + brVar.dQ);
        stringBuilder.append("|front_cam_pix|" + brVar.dR);
        stringBuilder.append("|product_device|" + brVar.ea);
        stringBuilder.append("|product_board|" + brVar.eb);
        stringBuilder.append("|build_product|" + brVar.ec);
        stringBuilder.append("|rom_fingerprint|" + brVar.ee);
        stringBuilder.append("|product_lanuage|" + brVar.ef);
        stringBuilder.append("|product_region|" + brVar.eg);
        stringBuilder.append("|build_radiover|" + brVar.eh);
        stringBuilder.append("|board_platform|" + brVar.dO);
        stringBuilder.append("|board_platform_mtk|" + brVar.ej);
        stringBuilder.append("|screen_pdi|" + brVar.dP);
        stringBuilder.append("|romname|" + brVar.dL);
        stringBuilder.append("|romversion|" + brVar.dM);
        stringBuilder.append("|kernel_ver|" + brVar.ek);
        stringBuilder.append("|isdual|" + brVar.dS);
        stringBuilder.append("|rom_manufactory_version|" + brVar.em);
        stringBuilder.append("|insideCid|" + brVar.en);
        stringBuilder.append("|outsideCid|" + brVar.eo);
        return stringBuilder.toString();
    }

    private boolean c(long j, long j2) {
        return j != j2;
    }

    private br fD() {
        br aN = this.Dm.gl().aN();
        if (aN != null) {
            if (aN.dl == null) {
                aN.dl = "";
            }
            return aN;
        }
        throw new RuntimeException("onGetRealInfoOfGuid() return null");
    }

    private boolean u(int i, int i2) {
        return i != i2;
    }

    private boolean w(String str, String str2) {
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

    private boolean w(boolean z) {
        boolean z2 = true;
        long currentTimeMillis = System.currentTimeMillis();
        if (z) {
            mb.n("GuidCertifier", "[cu_guid]shouldCheckUpdate(), forceCheck, true");
            this.Dp = currentTimeMillis;
            this.Dm.gl().g(currentTimeMillis);
            return true;
        }
        boolean z3 = false;
        if (this.Dp > 0) {
            if (lr.a(currentTimeMillis, this.Dp, 60)) {
                mb.n("GuidCertifier", "[cu_guid]shouldCheckUpdate(), [mem] more than 1h, continue check...");
            }
            return z3;
        }
        mb.n("GuidCertifier", "[cu_guid]shouldCheckUpdate(), [mem] first check after boot, continue check...");
        this.Dp = currentTimeMillis;
        long aO = this.Dm.gl().aO();
        if (aO <= 0) {
            z2 = false;
        }
        if (!z2) {
            mb.n("GuidCertifier", "[cu_guid]shouldCheckUpdate(), [file] first check, just record the time");
            this.Dm.gl().g(currentTimeMillis);
        } else if (lr.a(currentTimeMillis, aO, 720)) {
            mb.n("GuidCertifier", "[cu_guid]shouldCheckUpdate(), [file] more than 12h, should check");
            z3 = true;
            this.Dm.gl().g(currentTimeMillis);
        } else {
            mb.n("GuidCertifier", "[cu_guid]shouldCheckUpdate(), [file] less than 12h, donnot check");
        }
        return z3;
    }

    static void x(Context context) {
        try {
            Intent intent = new Intent(String.format("action.reg.guid:%s", new Object[]{context.getPackageName()}));
            intent.setPackage(context.getPackageName());
            context.sendBroadcast(intent);
        } catch (Throwable th) {
            mb.b("GuidCertifier", "[cu_guid]requestSendProcessRegisterGuid(): " + th, th);
        }
    }

    public void a(final a aVar) {
        mb.n("GuidCertifier", "[cu_guid]registerGuid()");
        if (fA()) {
            this.Dm.gl().aT();
            final br fD = fD();
            bw bwVar = new bw();
            final int fP = ns.fW().fP();
            bwVar.ey = fP;
            bwVar.bz = 1;
            bwVar.data = nh.a(this.mContext, (JceStruct) fD, 1, bwVar);
            if (bwVar.data != null) {
                mb.n("GuidCertifier", "[cu_guid]registerGuid(), cur info: " + c(fD));
                ArrayList arrayList = new ArrayList();
                arrayList.add(bwVar);
                nt.ga().a(bwVar.ey, -1, null);
                this.Dm.b(arrayList, new b() {
                    public void a(boolean z, int i, int i2, ArrayList<ce> arrayList) {
                        if (i != 0) {
                            mb.o("GuidCertifier", "[cu_guid]registerGuid(), retCode: " + i);
                            aVar.a(fP, 1, i, null);
                        } else if (arrayList == null) {
                            mb.o("GuidCertifier", "[cu_guid]registerGuid(), null == serverSashimis");
                            aVar.a(fP, 1, -21250000, null);
                        } else if (arrayList.size() > 0) {
                            ce ceVar = (ce) arrayList.get(0);
                            if (ceVar == null) {
                                mb.o("GuidCertifier", "[cu_guid]registerGuid(), serverSashimi is null");
                                aVar.a(fP, 1, -21250000, null);
                            } else if (ceVar.eB != 0) {
                                mb.o("GuidCertifier", "[cu_guid]registerGuid(), mazu error: " + ceVar.eB);
                                aVar.a(fP, 1, ceVar.eB, null);
                            } else if (ceVar.eC == 0) {
                                byte[] bArr = ceVar.data;
                                if (bArr != null) {
                                    mb.d("GuidCertifier", "[cu_guid]registerGuid() rs.data.length: " + ceVar.data.length);
                                    try {
                                        JceStruct a = nh.a(ni.this.mContext, ni.this.Dm.ap().DX.getBytes(), bArr, new ca(), false, ceVar.eE);
                                        if (a != null) {
                                            ca caVar = (ca) a;
                                            mb.d("GuidCertifier", "[cu_guid]registerGuid(), guid got: " + caVar.I);
                                            ni.this.a(caVar.I, fD, true);
                                            aVar.a(fP, 1, 0, caVar.I);
                                            return;
                                        }
                                        mb.o("GuidCertifier", "[cu_guid]registerGuid(), decode jce failed: null");
                                        aVar.a(fP, 1, -21000400, null);
                                        return;
                                    } catch (Exception e) {
                                        mb.o("GuidCertifier", "[cu_guid]registerGuid(), decode jce exception: " + e);
                                        aVar.a(fP, 1, -21000400, null);
                                        return;
                                    }
                                }
                                mb.o("GuidCertifier", "[cu_guid]registerGuid(), null == respData");
                                aVar.a(fP, 1, -21000005, null);
                            } else {
                                mb.o("GuidCertifier", "[cu_guid]registerGuid(), dataRetCode: " + ceVar.eC);
                                aVar.a(fP, 1, -21300000, null);
                            }
                        } else {
                            mb.o("GuidCertifier", "[cu_guid]registerGuid(), serverSashimis.size() <= 0");
                            aVar.a(fP, 1, -21250000, null);
                        }
                    }
                });
                return;
            }
            mb.s("GuidCertifier", "[cu_guid]registerGuid(), jceStruct2DataForSend failed");
            aVar.a(fP, 1, -20001500, null);
            return;
        }
        mb.d("GuidCertifier", "[cu_guid]registerGuid(), not necessary, mGuid: " + this.Do);
    }

    public void a(boolean z, String str) {
        if (nu.aB()) {
            br b = b(z, str);
            if (b != null) {
                a(b, str);
                return;
            }
            return;
        }
        mb.n("GuidCertifier", "[cu_guid] checUpdateGuid(), not send process, ignore!");
    }

    public String b() {
        return this.Do == null ? "" : this.Do;
    }

    public boolean fA() {
        if (nu.aB()) {
            return TextUtils.isEmpty(b()) || !this.Dn;
        } else {
            return false;
        }
    }

    public boolean fB() {
        return TextUtils.isEmpty(b()) || !this.Dn;
    }

    public void fC() {
        this.Do = this.Dm.gl().aF();
        if (TextUtils.isEmpty(this.Do)) {
            this.Dn = false;
            this.Do = this.Dm.gl().aG();
            if (this.Do == null) {
                this.Do = "";
            }
        } else {
            this.Dn = true;
        }
        mb.n("GuidCertifier", "[cu_guid]refreshGuid(), mGuid: " + this.Do + " fromPhone: " + this.Dn);
    }
}
