package tmsdkobf;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.tcc.DeepCleanEngine;
import tmsdk.common.tcc.DeepCleanEngine.Callback;
import tmsdk.common.tcc.QFile;
import tmsdk.common.tcc.SdcardScannerFactory;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.d;
import tmsdk.common.utils.i;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.deepclean.rubbish.SoftRubModel;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;

/* compiled from: Unknown */
public class gk {
    private Map<String, SoftRubModel> oG;
    public long oH;

    /* compiled from: Unknown */
    public interface a {
        void k(long j);
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.gk.3 */
    static class AnonymousClass3 implements Callback {
        final /* synthetic */ Set oJ;
        final /* synthetic */ a oK;
        final /* synthetic */ gi oL;
        final /* synthetic */ boolean oM;

        AnonymousClass3(Set set, a aVar, gi giVar, boolean z) {
            this.oJ = set;
            this.oK = aVar;
            this.oL = giVar;
            this.oM = z;
        }

        public String getDetailRule(String str) {
            return gk.a(this.oL, this.oM);
        }

        public void onFoundComRubbish(String str, String str2, long j) {
        }

        public void onFoundEmptyDir(String str, long j) {
            this.oJ.add(str);
            this.oK.k(j);
        }

        public void onFoundSoftRubbish(String str, String str2, String str3, long j) {
            long j2 = j / 1000;
            StringBuilder append = new StringBuilder().append(str2);
            if (str3 == null) {
                str3 = "";
            }
            this.oJ.add(append.append(str3).toString());
            this.oK.k(j2);
        }

        public void onProcessChange(int i) {
        }

        public void onVisit(QFile qFile) {
        }
    }

    public gk() {
        this.oH = 0;
    }

    public static String a(gi giVar, boolean z) {
        if (giVar == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(!z ? "1;" : "0;");
        if (giVar.op != null) {
            for (gv a : giVar.op) {
                a(stringBuilder, a, z);
            }
        }
        return stringBuilder.toString();
    }

    public static Set<String> a(gi giVar, a aVar, boolean z) {
        Set<String> hashSet = new HashSet();
        String[] strArr = new String[]{giVar.om};
        DeepCleanEngine deepCleanEngine = SdcardScannerFactory.getDeepCleanEngine(new AnonymousClass3(hashSet, aVar, giVar, z));
        if (deepCleanEngine == null) {
            return hashSet;
        }
        deepCleanEngine.setRootPaths(strArr);
        List aP = aP();
        if (aP != null) {
            String[] strArr2 = new String[aP.size()];
            for (int i = 0; i < strArr2.length; i++) {
                StringBuilder stringBuilder = new StringBuilder();
                gm.a(stringBuilder, (gm) aP.get(i));
                strArr2[i] = stringBuilder.toString();
            }
            deepCleanEngine.setOtherFilterRule(strArr2);
        }
        for (String scanPath : gq.aT()) {
            deepCleanEngine.scanPath(scanPath, giVar.om);
        }
        deepCleanEngine.release();
        return hashSet;
    }

    private static void a(StringBuilder stringBuilder, gv gvVar, boolean z) {
        for (String str : gvVar.op) {
            stringBuilder.append('0');
            stringBuilder.append(gvVar.mID);
            stringBuilder.append(':');
            stringBuilder.append('7');
            if (z) {
                if (gvVar.pw == 3 || gvVar.pw == 4) {
                    stringBuilder.append('0');
                } else {
                    stringBuilder.append('1');
                }
            } else if (gvVar.pw != 3) {
                stringBuilder.append('1');
            } else {
                stringBuilder.append('0');
            }
            if (!TextUtils.isEmpty(str)) {
                stringBuilder.append(':');
                stringBuilder.append('1');
                stringBuilder.append(str);
            }
            if (!TextUtils.isEmpty(gvVar.mFileName)) {
                stringBuilder.append(':');
                stringBuilder.append('2');
                stringBuilder.append(gvVar.mFileName);
            }
            if (!TextUtils.isEmpty(gvVar.pb)) {
                stringBuilder.append(':');
                stringBuilder.append('3');
                stringBuilder.append(gvVar.pb);
            }
            if (!TextUtils.isEmpty(gvVar.pc)) {
                stringBuilder.append(':');
                stringBuilder.append('4');
                stringBuilder.append(gvVar.pc);
            }
            if (!TextUtils.isEmpty(gvVar.pd)) {
                stringBuilder.append(':');
                stringBuilder.append('5');
                stringBuilder.append(gvVar.pd);
            }
            if (!TextUtils.isEmpty(gvVar.pe)) {
                stringBuilder.append(':');
                stringBuilder.append('6');
                stringBuilder.append(gvVar.pe);
            }
            stringBuilder.append(';');
        }
    }

    public static List<gm> aP() {
        cx cxVar = (cx) nj.b(TMSDKContext.getApplicaionContext(), UpdateConfig.intToString(40248) + ".dat", UpdateConfig.intToString(40248), new cx(), "UTF-8");
        if (cxVar == null || cxVar.gw == null) {
            return null;
        }
        List<gm> arrayList = new ArrayList();
        Iterator it = cxVar.gw.iterator();
        while (it.hasNext()) {
            cw cwVar = (cw) it.next();
            if ("3".equals(cwVar.go)) {
                gm gmVar = new gm();
                gmVar.pa = cwVar.gp;
                if (!TextUtils.isEmpty(cwVar.gt)) {
                    String[] split = cwVar.gt.split("&");
                    if (split != null) {
                        for (String str : split) {
                            String str2;
                            if (str2.length() > 2) {
                                char charAt = str2.charAt(0);
                                str2 = str2.substring(2);
                                switch (charAt) {
                                    case SmsCheckResult.ESC_TEL_95013 /*49*/:
                                        gmVar.mFileName = str2;
                                        break;
                                    case SmsCheckResult.ESC_TEL_OTHER /*50*/:
                                        gmVar.pb = str2;
                                        break;
                                    case '3':
                                        gmVar.pc = str2;
                                        break;
                                    case '4':
                                        gmVar.pd = str2;
                                        break;
                                    case '5':
                                        gmVar.pe = str2;
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    }
                }
                arrayList.add(gmVar);
            }
        }
        return arrayList;
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
                        String str2 = (String) map.get(Integer.valueOf(10));
                        if (!(str2 == null || str2.equals(""))) {
                            int intValue = Integer.valueOf(str2).intValue();
                            if (intValue > 0 && gvVar.pw == 3) {
                                if (z) {
                                    if (TextUtils.isEmpty(gvVar.mDescription)) {
                                        gvVar.mDescription = "Data Cache";
                                    }
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
                                    if (TextUtils.isEmpty(gvVar.mDescription)) {
                                        gvVar2.mDescription = "Data Cache";
                                    }
                                    gvVar2.mDescription += "(" + String.format(i.dh("eng_days_ago"), new Object[]{Integer.valueOf(intValue)}) + ")";
                                } else {
                                    gvVar2.mDescription += "(" + String.format(i.dh("cn_days_ago"), new Object[]{Integer.valueOf(intValue)}) + ")";
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
                        if (z && TextUtils.isEmpty(gvVar.mDescription)) {
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

    public void a(SoftRubModel softRubModel) {
        if (this.oG == null) {
            this.oG = new HashMap();
        }
        this.oG.put(softRubModel.mPkgName, softRubModel);
    }

    public SoftRubModel aG(String str) {
        boolean bo = hb.bo();
        qd systemInfoService = TMServiceFactory.getSystemInfoService();
        cx cxVar = (cx) nj.b(TMSDKContext.getApplicaionContext(), UpdateConfig.DEEPCLEAN_SDCARD_SCAN_RULE_NAME_V2, UpdateConfig.intToString(40225), new cx(), "UTF-8");
        if (cxVar == null || cxVar.gw == null) {
            return null;
        }
        gi giVar;
        List<gv> arrayList = new ArrayList();
        Map hashMap = new HashMap();
        Iterator it = cxVar.gw.iterator();
        while (it.hasNext()) {
            cw cwVar = (cw) it.next();
            if (cwVar.go != null) {
                try {
                    Integer valueOf = Integer.valueOf(cwVar.go);
                    if (valueOf != null) {
                        switch (valueOf.intValue()) {
                            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                                if (cwVar.gp != null && cwVar.gq != null && cwVar.gr != null) {
                                    giVar = (gi) hashMap.get(cwVar.gp);
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
                                    hashMap.put(cwVar.gp, giVar);
                                    break;
                                }
                                return null;
                                break;
                            case FileInfo.TYPE_BIGFILE /*3*/:
                                if (cwVar.gp == null || cwVar.gq == null || cwVar.gr == null || cwVar.gs == null) {
                                    return null;
                                }
                                gv gvVar = new gv();
                                gvVar.op = new ArrayList();
                                String[] split = cwVar.gp.split("#");
                                if (split != null) {
                                    int aI;
                                    gvVar.om = cwVar.gq;
                                    String str2 = (bo && !TextUtils.isEmpty(cwVar.gu)) ? cwVar.gu : cwVar.gr;
                                    gvVar.mDescription = str2;
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
                                            String str3 = (bo && !TextUtils.isEmpty(cwVar.gu)) ? cwVar.gu + "(" + String.format(i.dh("eng_days_ago"), new Object[]{Integer.valueOf(aI)}) + ")" : cwVar.gr + "(" + String.format(i.dh("cn_days_ago"), new Object[]{Integer.valueOf(aI)}) + ")";
                                            gvVar2.mDescription = str3;
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
                                return null;
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
            giVar = (gi) hashMap.get(gvVar3.om);
            if (giVar != null) {
                if (giVar.op == null) {
                    giVar.op = new ArrayList();
                }
                giVar.op.add(gvVar3);
            }
        }
        this.oH = 0;
        a anonymousClass1 = new a() {
            final /* synthetic */ gk oI;

            {
                this.oI = r1;
            }

            public void k(long j) {
                gk gkVar = this.oI;
                gkVar.oH += j;
            }
        };
        boolean aC = systemInfoService.aC(str);
        SoftRubModel softRubModel = null;
        for (Entry value : hashMap.entrySet()) {
            gi giVar2 = (gi) value.getValue();
            if (giVar2 != null && giVar2.on.containsKey(str)) {
                Object obj;
                Collection a;
                for (String str22 : giVar2.on.keySet()) {
                    if (!str22.equals(str) && systemInfoService.aC(str22)) {
                        obj = null;
                        if (obj != null) {
                            a = a(giVar2, anonymousClass1, aC);
                            if (a != null) {
                                if (softRubModel == null) {
                                    softRubModel = new SoftRubModel();
                                    softRubModel.mPkgName = str;
                                    softRubModel.mAppName = (String) giVar2.on.get(str);
                                }
                                if (softRubModel.mRubbishFiles == null) {
                                    softRubModel.mRubbishFiles = new ArrayList();
                                }
                                softRubModel.mRubbishFiles.addAll(a);
                            }
                        }
                    }
                }
                obj = 1;
                if (obj != null) {
                    a = a(giVar2, anonymousClass1, aC);
                    if (a != null) {
                        if (softRubModel == null) {
                            softRubModel = new SoftRubModel();
                            softRubModel.mPkgName = str;
                            softRubModel.mAppName = (String) giVar2.on.get(str);
                        }
                        if (softRubModel.mRubbishFiles == null) {
                            softRubModel.mRubbishFiles = new ArrayList();
                        }
                        softRubModel.mRubbishFiles.addAll(a);
                    }
                }
            }
            softRubModel = softRubModel;
        }
        if (softRubModel == null || softRubModel.mRubbishFiles == null || softRubModel.mRubbishFiles.size() <= 0) {
            return null;
        }
        softRubModel.mRubbishFileSize = this.oH;
        return softRubModel;
    }

    public SoftRubModel aH(String str) {
        boolean bo = hb.bo();
        qd systemInfoService = TMServiceFactory.getSystemInfoService();
        String c = fm.c(TccCryptor.encrypt(str.getBytes(), null));
        List<gi> aD = gg.aI().aD(c);
        if (aD == null || aD.size() == 0) {
            return null;
        }
        this.oH = 0;
        a anonymousClass2 = new a() {
            final /* synthetic */ gk oI;

            {
                this.oI = r1;
            }

            public void k(long j) {
                gk gkVar = this.oI;
                gkVar.oH += j;
            }
        };
        boolean aC = systemInfoService.aC(str);
        SoftRubModel softRubModel = null;
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
                    }
                    Collection a = a(giVar, anonymousClass2, aC);
                    if (a != null) {
                        if (softRubModel == null) {
                            softRubModel = new SoftRubModel();
                            softRubModel.mPkgName = str;
                            byte[] bArr = (byte[]) giVar.oo.get(c);
                            if (bArr == null) {
                                return null;
                            }
                            softRubModel.mAppName = new String(TccCryptor.decrypt(bArr, null));
                        }
                        if (softRubModel.mRubbishFiles == null) {
                            softRubModel.mRubbishFiles = new ArrayList();
                        }
                        softRubModel.mRubbishFiles.addAll(a);
                    }
                }
            }
            softRubModel = softRubModel;
        }
        if (softRubModel == null || softRubModel.mRubbishFiles == null || softRubModel.mRubbishFiles.size() <= 0) {
            return null;
        }
        softRubModel.mRubbishFileSize = this.oH;
        return softRubModel;
    }
}
