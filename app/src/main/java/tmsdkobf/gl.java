package tmsdkobf;

import android.text.TextUtils;
import android.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.tcc.DeepCleanEngine;
import tmsdk.common.tcc.DeepCleanEngine.Callback;
import tmsdk.common.tcc.QFile;
import tmsdk.common.tcc.SdcardScannerFactory;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.d;
import tmsdk.common.utils.i;
import tmsdk.fg.module.deepclean.RubbishEntity;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.deepclean.rubbish.SoftRubModel;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;

/* compiled from: Unknown */
public class gl {
    private static gl oU;
    public long oH;
    private Map<String, gi> oN;
    private List<String> oO;
    private gi oP;
    private HashMap<String, RubbishEntity> oQ;
    private SoftRubModel oR;
    private DeepCleanEngine oS;
    private String[] oT;
    Pair<Integer, RubbishEntity> oV;
    Callback oW;

    public gl() {
        this.oR = null;
        this.oH = 0;
        this.oW = new Callback() {
            final /* synthetic */ gl oX;

            {
                this.oX = r1;
            }

            public String getDetailRule(String str) {
                return gk.a(this.oX.oP, true);
            }

            public void onFoundComRubbish(String str, String str2, long j) {
            }

            public void onFoundEmptyDir(String str, long j) {
            }

            public void onFoundSoftRubbish(String str, String str2, String str3, long j) {
                long j2 = j / 1000;
                if (this.oX.a(this.oX.oP, str, str2, str3, j2) != null) {
                    gl glVar = this.oX;
                    glVar.oH += j2;
                }
            }

            public void onProcessChange(int i) {
            }

            public void onVisit(QFile qFile) {
            }
        };
        gv.bb();
        if (!new ge().aG()) {
            this.oN = new HashMap();
            if (!aR()) {
                this.oN = null;
            }
        }
        List aP = gk.aP();
        if (aP != null) {
            this.oT = new String[aP.size()];
            for (int i = 0; i < this.oT.length; i++) {
                StringBuilder stringBuilder = new StringBuilder();
                gm.a(stringBuilder, (gm) aP.get(i));
                this.oT[i] = stringBuilder.toString();
            }
        }
        this.oO = gq.aT();
    }

    private RubbishEntity a(gi giVar, String str, String str2, String str3, long j) {
        try {
            int parseInt = Integer.parseInt(str);
            RubbishEntity rubbishEntity;
            if (this.oV != null && ((Integer) this.oV.first).intValue() == parseInt) {
                rubbishEntity = (RubbishEntity) this.oV.second;
                rubbishEntity.size += j;
                List list = rubbishEntity.path;
                StringBuilder append = new StringBuilder().append(str2);
                if (str3 == null) {
                    str3 = "";
                }
                list.add(append.append(str3).toString());
                return rubbishEntity;
            }
            gv ax = giVar.ax(parseInt);
            if (ax != null) {
                rubbishEntity = (RubbishEntity) this.oQ.get(ax.mDescription);
                if (rubbishEntity == null) {
                    rubbishEntity = new RubbishEntity();
                    if (ax.pw == 1) {
                        rubbishEntity.isSuggest = true;
                        rubbishEntity.setStatus(1);
                    } else if (ax.pw != 2) {
                        return null;
                    } else {
                        rubbishEntity.isSuggest = false;
                    }
                    rubbishEntity.path = new ArrayList();
                    List list2 = rubbishEntity.path;
                    StringBuilder append2 = new StringBuilder().append(str2);
                    if (str3 == null) {
                        str3 = "";
                    }
                    list2.add(append2.append(str3).toString());
                    rubbishEntity.description = ax.mDescription;
                    rubbishEntity.size = j;
                    this.oQ.put(rubbishEntity.description, rubbishEntity);
                    this.oV = new Pair(Integer.valueOf(parseInt), rubbishEntity);
                    return rubbishEntity;
                }
                rubbishEntity.size += j;
                List list3 = rubbishEntity.path;
                StringBuilder append3 = new StringBuilder().append(str2);
                if (str3 == null) {
                    str3 = "";
                }
                list3.add(append3.append(str3).toString());
                this.oV = new Pair(Integer.valueOf(parseInt), rubbishEntity);
                return rubbishEntity;
            }
            d.d("SoftwareRubbishManagerInstall", "detailPath == null:");
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static gl aQ() {
        if (oU == null) {
            oU = new gl();
        }
        return oU;
    }

    private List<gv> b(String str, boolean z) {
        if (str == null) {
            return null;
        }
        try {
            fq fqVar = new fq(gg.aI().aE(str));
            fqVar.ae("UTF-8");
            ah ahVar = new ah();
            ahVar.readFrom(fqVar);
            byte[] bArr = ahVar.aW;
            if (bArr != null) {
                fqVar = new fq(TccCryptor.decrypt(bArr, null));
                fqVar.ae("UTF-8");
                ag agVar = new ag();
                agVar.readFrom(fqVar);
                List<gv> arrayList = new ArrayList();
                Iterator it = agVar.aT.iterator();
                while (it.hasNext()) {
                    Map map = (Map) it.next();
                    gv gvVar;
                    if (map.get(Integer.valueOf(3)) != null) {
                        gvVar = new gv();
                        gvVar.om = str;
                        gvVar.pw = Integer.valueOf((String) map.get(Integer.valueOf(9))).intValue();
                        gvVar.op = new ArrayList();
                        gvVar.op.add(((String) map.get(Integer.valueOf(3))).toLowerCase());
                        gvVar.mDescription = !z ? (String) map.get(Integer.valueOf(8)) : (String) map.get(Integer.valueOf(18));
                        if (gvVar.mDescription == null) {
                            gvVar.mDescription = (String) map.get(Integer.valueOf(8));
                        }
                        String str2 = (String) map.get(Integer.valueOf(10));
                        if (!(str2 == null || str2.equals(""))) {
                            int intValue = Integer.valueOf(str2).intValue();
                            if (intValue > 0 && gvVar.pw == 3) {
                                if (z) {
                                    gvVar.mDescription += "(" + String.format(i.dh("eng_in_recent_days"), new Object[]{Integer.valueOf(intValue)}) + ")";
                                } else {
                                    gvVar.mDescription += "(" + String.format(i.dh("cn_in_recent_days"), new Object[]{Integer.valueOf(intValue)}) + ")";
                                }
                                gvVar.pe = "0," + intValue;
                                gv gvVar2 = new gv();
                                gvVar2.om = gvVar.om;
                                gvVar2.pw = 1;
                                gvVar2.op = new ArrayList();
                                gvVar2.op.addAll(gvVar.op);
                                gvVar2.mDescription = !z ? (String) map.get(Integer.valueOf(8)) : (String) map.get(Integer.valueOf(18));
                                if (z) {
                                    gvVar2.mDescription += "(" + String.format(i.dh("eng_days_ago"), new Object[]{Integer.valueOf(intValue)}) + ")";
                                } else {
                                    gvVar2.mDescription += "(" + String.format(i.dh("cn_days_ago"), new Object[]{Integer.valueOf(intValue)}) + ")";
                                }
                                if (gvVar.mDescription == null && z) {
                                    gvVar.mDescription = ((String) map.get(Integer.valueOf(18))) + String.format(i.dh("eng_days_ago"), new Object[]{Integer.valueOf(intValue)});
                                } else {
                                    gvVar.mDescription = ((String) map.get(Integer.valueOf(8))) + String.format(i.dh("cn_days_ago"), new Object[]{Integer.valueOf(intValue)});
                                }
                                arrayList.add(gvVar2);
                            }
                        }
                        arrayList.add(gvVar);
                    } else if (map.get(Integer.valueOf(4)) != null) {
                        gvVar = new gv();
                        gvVar.om = str;
                        gvVar.pw = Integer.valueOf((String) map.get(Integer.valueOf(9))).intValue();
                        gvVar.op = new ArrayList();
                        gvVar.op.add(((String) map.get(Integer.valueOf(4))).toLowerCase());
                        gvVar.mDescription = !z ? (String) map.get(Integer.valueOf(8)) : (String) map.get(Integer.valueOf(18));
                        if (z && TextUtils.isEmpty((CharSequence) map.get(Integer.valueOf(18)))) {
                            gvVar.mDescription = "Data Cache";
                        }
                        gvVar.mFileName = (String) map.get(Integer.valueOf(11));
                        gvVar.pb = (String) map.get(Integer.valueOf(12));
                        gvVar.pc = (String) map.get(Integer.valueOf(13));
                        gvVar.pd = (String) map.get(Integer.valueOf(14));
                        gvVar.pe = (String) map.get(Integer.valueOf(15));
                        arrayList.add(gvVar);
                    }
                }
                return arrayList;
            }
            d.c("xx", "null:" + str);
            return null;
        } catch (Exception e) {
            d.c("xx", e);
            return null;
        }
    }

    public static void freeSoftwareRubbishManagerInstall() {
        oU = null;
        gv.bb();
    }

    public void a(gi giVar) {
        this.oP = giVar;
        this.oS = SdcardScannerFactory.getDeepCleanEngine(this.oW);
        if (this.oS != null) {
            this.oS.setRootPaths(new String[]{giVar.om});
            this.oS.setOtherFilterRule(this.oT);
            for (String scanPath : this.oO) {
                this.oS.scanPath(scanPath, giVar.om);
            }
            this.oS.release();
            this.oS = null;
        }
    }

    public SoftRubModel aG(String str) {
        d.d("SoftwareRubbishManagerInstall", "getRubAppInfo:");
        if (this.oN == null) {
            return null;
        }
        this.oR = null;
        this.oH = 0;
        qd systemInfoService = TMServiceFactory.getSystemInfoService();
        for (Entry value : this.oN.entrySet()) {
            gi giVar = (gi) value.getValue();
            if (giVar != null && giVar.on.containsKey(str)) {
                Object obj;
                for (String str2 : giVar.on.keySet()) {
                    if (!str2.equals(str) && systemInfoService.aC(str2)) {
                        obj = null;
                        break;
                    }
                }
                obj = 1;
                if (obj != null) {
                    if (this.oR == null) {
                        this.oR = new SoftRubModel();
                        this.oR.mPkgName = str;
                        this.oR.mAppName = (String) giVar.on.get(str);
                        this.oQ = new HashMap();
                    }
                    a(giVar);
                }
            }
        }
        if (this.oR == null || this.oQ == null || this.oQ.size() <= 0) {
            return null;
        }
        this.oR.mRubbishFileSize = this.oH;
        this.oR.mRubbishFilesInstall = new ArrayList();
        for (String str3 : this.oQ.keySet()) {
            this.oR.mRubbishFilesInstall.add((RubbishEntity) this.oQ.get(str3));
        }
        return this.oR;
    }

    public SoftRubModel aH(String str) {
        d.d("SoftwareRubbishManagerInstall", "getRubAppInfoFromDB:");
        this.oR = null;
        this.oH = 0;
        boolean bo = hb.bo();
        qd systemInfoService = TMServiceFactory.getSystemInfoService();
        String c = fm.c(TccCryptor.encrypt(str.getBytes(), null));
        List<gi> aD = gg.aI().aD(c);
        if (aD == null || aD.size() == 0) {
            return null;
        }
        for (gi giVar : aD) {
            if (giVar != null) {
                Object obj;
                giVar.oo = gg.aI().a(giVar.om, bo);
                for (String str2 : giVar.oo.keySet()) {
                    if (!str2.equals(c) && systemInfoService.aC(new String(TccCryptor.decrypt(fm.ac(str2), null)))) {
                        obj = null;
                        break;
                    }
                }
                obj = 1;
                if (obj == null) {
                    continue;
                } else {
                    if (new ge().aG()) {
                        giVar.op = b(giVar.om, bo);
                        if (this.oR == null) {
                            this.oR = new SoftRubModel();
                            this.oR.mPkgName = str;
                            this.oQ = new HashMap();
                            byte[] bArr = (byte[]) giVar.oo.get(c);
                            if (bArr == null) {
                                return null;
                            }
                            this.oR.mAppName = new String(TccCryptor.decrypt(bArr, null));
                        }
                    }
                    a(giVar);
                }
            }
        }
        if (this.oR == null || this.oQ == null || this.oQ.size() <= 0) {
            return null;
        }
        this.oR.mRubbishFileSize = this.oH;
        this.oR.mRubbishFilesInstall = new ArrayList();
        for (String str3 : this.oQ.keySet()) {
            this.oR.mRubbishFilesInstall.add((RubbishEntity) this.oQ.get(str3));
        }
        return this.oR;
    }

    public boolean aR() {
        boolean bo = hb.bo();
        cx cxVar = (cx) nj.b(TMSDKContext.getApplicaionContext(), UpdateConfig.DEEPCLEAN_SDCARD_SCAN_RULE_NAME_V2, UpdateConfig.intToString(40225), new cx(), "UTF-8");
        if (cxVar == null || cxVar.gw == null) {
            return false;
        }
        List<gv> arrayList = new ArrayList();
        Iterator it = cxVar.gw.iterator();
        while (it.hasNext()) {
            gi giVar;
            cw cwVar = (cw) it.next();
            if (cwVar.go != null) {
                try {
                    Integer valueOf = Integer.valueOf(cwVar.go);
                    if (valueOf != null) {
                        switch (valueOf.intValue()) {
                            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                                if (cwVar.gp != null && cwVar.gq != null && cwVar.gr != null) {
                                    giVar = (gi) this.oN.get(cwVar.gp);
                                    if (giVar != null) {
                                        if (!giVar.on.containsKey(cwVar.gq)) {
                                            if (!bo || TextUtils.isEmpty(cwVar.gs)) {
                                                giVar.on.put(cwVar.gq, cwVar.gr);
                                                break;
                                            }
                                            giVar.on.put(cwVar.gq, cwVar.gs);
                                            break;
                                        }
                                        break;
                                    }
                                    giVar = new gi();
                                    giVar.om = cwVar.gp;
                                    giVar.on = new HashMap();
                                    if (bo && !TextUtils.isEmpty(cwVar.gs)) {
                                        giVar.on.put(cwVar.gq, cwVar.gs);
                                    } else {
                                        giVar.on.put(cwVar.gq, cwVar.gr);
                                    }
                                    this.oN.put(cwVar.gp, giVar);
                                    break;
                                }
                                return false;
                                break;
                            case FileInfo.TYPE_BIGFILE /*3*/:
                                if (cwVar.gp == null || cwVar.gq == null || cwVar.gr == null || cwVar.gs == null) {
                                    return false;
                                }
                                gv gvVar = new gv();
                                gvVar.op = new ArrayList();
                                String[] split = cwVar.gp.split("#");
                                if (split != null) {
                                    int aI;
                                    gvVar.om = cwVar.gq;
                                    String str = (bo && !TextUtils.isEmpty(cwVar.gu)) ? cwVar.gu : cwVar.gr;
                                    gvVar.mDescription = str;
                                    for (String substring : split) {
                                        gvVar.op.add(substring.substring(gvVar.om.length()));
                                    }
                                    try {
                                        gvVar.pw = Integer.valueOf(cwVar.gs).intValue();
                                        arrayList.add(gvVar);
                                        aI = gn.aI(cwVar.gt);
                                        if (aI > 0 && gvVar.pw == 3) {
                                            gvVar.mDescription = !bo ? gvVar.mDescription + String.format(i.dh("cn_in_recent_days"), new Object[]{Integer.valueOf(aI)}) : gvVar.mDescription + String.format(i.dh("eng_in_recent_days"), new Object[]{Integer.valueOf(aI)});
                                            gvVar.pe = "0," + aI;
                                            gv gvVar2 = new gv();
                                            gvVar2.om = gvVar.om;
                                            gvVar2.op = new ArrayList();
                                            gvVar2.op.addAll(gvVar.op);
                                            String str2 = (bo && !TextUtils.isEmpty(cwVar.gu)) ? cwVar.gu + "(" + String.format(i.dh("eng_days_ago"), new Object[]{Integer.valueOf(aI)}) + ")" : cwVar.gr + "(" + String.format(i.dh("cn_days_ago"), new Object[]{Integer.valueOf(aI)}) + ")";
                                            gvVar2.mDescription = str2;
                                            gvVar2.pw = 1;
                                            gvVar2.pe = "" + aI + ",-";
                                            arrayList.add(gvVar2);
                                            break;
                                        }
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                        break;
                                    }
                                }
                                return false;
                                break;
                            case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                                continue;
                            default:
                                break;
                        }
                    }
                    continue;
                } catch (NumberFormatException e2) {
                }
            }
        }
        for (gv gvVar3 : arrayList) {
            giVar = (gi) this.oN.get(gvVar3.om);
            if (giVar != null) {
                if (giVar.op == null) {
                    giVar.op = new ArrayList();
                }
                giVar.op.add(gvVar3);
            }
        }
        return true;
    }
}
