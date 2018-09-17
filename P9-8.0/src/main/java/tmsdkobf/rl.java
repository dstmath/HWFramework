package tmsdkobf;

import android.os.Environment;
import android.text.TextUtils;
import com.qq.taf.jce.JceInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.lang.MultiLangManager;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.tcc.DeepCleanEngine;
import tmsdk.common.tcc.QFile;
import tmsdk.common.tcc.SdcardScannerFactory;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.f;
import tmsdk.common.utils.m;

public class rl {
    static DeepCleanEngine PB = SdcardScannerFactory.getDeepCleanEngine(null);
    public static final List<String> Ps = rk.jZ();
    Map<String, ov> OK;
    private b PA = null;
    private String PC = null;
    private TreeMap<Integer, List<a>> PD = null;
    private HashMap<String, b> PE = new HashMap();
    private boolean Pt = false;
    public long Pu = 0;
    public long Pv = 0;
    public long Pw = 0;
    public long Px = 0;
    public long Py = 0;
    public long Pz = 0;

    static class a {
        public String Ow = "";
        public String Ox = "";
        public String PF = "";
        public String PG = "";
        public String PH = "";
        public String PI = "";
        public String PJ = "";
        public String PK = "";
        private String PL = null;
        public String[] PM = null;
        public String mDesc = "";
        public String mFileName = "";
        public String mPath = "";

        a() {
        }
    }

    static class b {
        public String MB;
        private String PL = null;
        public List<a> PN;
        public HashMap<String, List<a>> PO = new HashMap();
        public TreeMap<String, List<a>> PP = new TreeMap();
        public TreeMap<String, List<a>> PQ = new TreeMap();
        boolean PR = false;
        boolean PS = false;
        public String mAppName;
        public String mPkg;

        b() {
        }

        public void kl() {
            if (!this.PR && this != null && this.PN != null) {
                for (a aVar : this.PN) {
                    Map map;
                    if (!(this.PS || aVar.Ow == null || !aVar.Ow.equals("4"))) {
                        this.PS = true;
                    }
                    if (aVar.mPath.contains("*")) {
                        map = this.PP;
                        aVar.PM = aVar.mPath.split("/");
                    } else {
                        map = !aVar.mPath.contains("//") ? this.PO : this.PQ;
                    }
                    if (map != null) {
                        List list = (List) map.get(aVar.mPath);
                        if (list == null) {
                            list = new ArrayList();
                        }
                        list.add(aVar);
                        map.put(aVar.mPath, list);
                    }
                }
                this.PR = true;
            }
        }
    }

    private void Y(boolean z) {
        al alVar = (al) mk.b(TMSDKContext.getApplicaionContext(), UpdateConfig.intToString(40415) + ".dat", UpdateConfig.intToString(40415), new al(), "UTF-8");
        if (alVar != null && alVar.bt != null) {
            Iterator it = alVar.bt.iterator();
            while (it.hasNext()) {
                am amVar = (am) it.next();
                qv qvVar = new qv();
                Iterator it2 = amVar.bv.iterator();
                while (it2.hasNext()) {
                    Map map = (Map) it2.next();
                    if (map.get(Integer.valueOf(2)) != null) {
                        qvVar.MB = ((String) map.get(Integer.valueOf(2))).toLowerCase();
                    } else if (map.get(Integer.valueOf(5)) != null) {
                        String str = (String) map.get(Integer.valueOf(6));
                        String str2 = new String(TccCryptor.decrypt(lq.at(((String) map.get(Integer.valueOf(5))).toUpperCase()), null));
                        String str3 = (String) map.get(Integer.valueOf(17));
                        if (qvVar.Oz == null) {
                            qvVar.Oz = new HashMap();
                        }
                        if (z && !TextUtils.isEmpty(str3)) {
                            qvVar.Oz.put(str2, new String(TccCryptor.decrypt(lq.at(str3.toUpperCase()), null)));
                        } else {
                            qvVar.Oz.put(str2, new String(TccCryptor.decrypt(lq.at(str.toUpperCase()), null)));
                        }
                        if (qvVar.Oz == null) {
                            qvVar.Oz = new HashMap();
                        }
                        if (TextUtils.isEmpty(str)) {
                            qvVar.Oz.put(str2, "NoAppName");
                        } else {
                            qvVar.Oz.put(str2, new String(TccCryptor.decrypt(lq.at(str.toUpperCase()), null)));
                        }
                    }
                }
                qvVar.Ot = a(qvVar.MB, amVar.bw, z);
                if (!(qvVar.Ot == null || qvVar.Ot.size() == 0 || qvVar.Oz == null || qvVar.Oz.size() == 0)) {
                    b bVar = (b) this.PE.get(qvVar.MB);
                    if (bVar == null) {
                        bVar = new b();
                        bVar.MB = qvVar.MB;
                        bVar.PN = new ArrayList();
                        this.PE.put(qvVar.MB, bVar);
                    }
                    for (Entry key : qvVar.Oz.entrySet()) {
                        ov ovVar = (ov) this.OK.get(key.getKey());
                        if (ovVar != null) {
                            bVar.mPkg = ovVar.getPackageName();
                            bVar.mAppName = ovVar.getAppName();
                            break;
                        }
                    }
                    if (bVar.mPkg == null) {
                        Entry entry = (Entry) qvVar.Oz.entrySet().iterator().next();
                        bVar.mPkg = (String) entry.getKey();
                        bVar.mAppName = (String) entry.getValue();
                    }
                    for (qu quVar : qvVar.Ot) {
                        a aVar = new a();
                        aVar.mPath = (String) quVar.Ot.get(0);
                        aVar.PF = quVar.MB;
                        aVar.Ow = quVar.Ow;
                        aVar.mDesc = quVar.mDescription;
                        aVar.PG = Integer.toString(quVar.Nt);
                        if (!TextUtils.isEmpty(aVar.Ow)) {
                            bVar.PN.add(aVar);
                        }
                    }
                }
            }
        }
    }

    private int a(a aVar) {
        if (aVar == null) {
            return 0;
        }
        int i = 0;
        if (!aVar.mFileName.equals("")) {
            i = 1;
        }
        if (!aVar.PH.equals("")) {
            i++;
        }
        if (!aVar.PI.equals("")) {
            i++;
        }
        if (!aVar.PJ.equals("")) {
            i++;
        }
        if (!aVar.PK.equals("")) {
            i++;
        }
        return i;
    }

    public static List<qu> a(String str, byte[] bArr, boolean z) {
        Exception e;
        List<qu> list;
        try {
            JceInputStream jceInputStream = new JceInputStream(TccCryptor.decrypt(bArr, null));
            jceInputStream.setServerEncoding("UTF-8");
            ak akVar = new ak();
            akVar.readFrom(jceInputStream);
            List<qu> arrayList = new ArrayList();
            try {
                Iterator it = akVar.br.iterator();
                while (it.hasNext()) {
                    Map map = (Map) it.next();
                    String str2 = (String) map.get(Integer.valueOf(3));
                    qu a;
                    String str3;
                    if (TextUtils.isEmpty(str2)) {
                        str2 = (String) map.get(Integer.valueOf(4));
                        if (TextUtils.isEmpty(str2)) {
                            continue;
                        } else {
                            a = a(str, z, map, str2);
                            a.Ow = "4";
                            a.mFileName = (String) map.get(Integer.valueOf(11));
                            a.Ol = (String) map.get(Integer.valueOf(12));
                            a.Om = (String) map.get(Integer.valueOf(13));
                            a.On = (String) map.get(Integer.valueOf(14));
                            a.Oo = (String) map.get(Integer.valueOf(15));
                            str3 = (String) map.get(Integer.valueOf(23));
                            if (str3 != null) {
                                a.Nw = rg.dh(str3);
                            }
                            if (a.Ov > 100) {
                                a.Ov = 0;
                            }
                            arrayList.add(a);
                        }
                    } else {
                        a = a(str, z, map, str2);
                        a.Ow = "3";
                        str3 = (String) map.get(Integer.valueOf(23));
                        if (str3 != null) {
                            a.Nw = rg.dh(str3);
                        }
                        if (a.Ov > 100) {
                            a.Ov = 0;
                        }
                        String str4 = (String) map.get(Integer.valueOf(10));
                        if (!TextUtils.isEmpty(str4)) {
                            int intValue = Integer.valueOf(str4).intValue();
                            if (intValue > 0) {
                                a.Ox = str4;
                                a.mDescription += "(" + String.format(m.cF("in_recent_days"), new Object[]{Integer.valueOf(intValue)}) + ")";
                                a.Oo = "0," + intValue;
                                qu a2 = a(str, z, map, str2);
                                a2.Ow = "3";
                                a2.Nt = 1;
                                a2.Oo = "" + intValue + ",-";
                                a2.Ox = str4;
                                a2.mDescription += "(" + String.format(m.cF("days_ago"), new Object[]{Integer.valueOf(intValue)}) + ")";
                                a2.Nw = a.Nw;
                                arrayList.add(a2);
                            }
                        }
                        try {
                            arrayList.add(a);
                        } catch (Exception e2) {
                            e = e2;
                            list = arrayList;
                        }
                    }
                }
                return arrayList;
            } catch (Exception e3) {
                e = e3;
                list = arrayList;
                e.printStackTrace();
                return null;
            }
        } catch (Exception e4) {
            e = e4;
            e.printStackTrace();
            return null;
        }
    }

    private static qu a(String str, boolean z, Map<Integer, String> map, String str2) {
        qu quVar = new qu();
        quVar.MB = str;
        quVar.Nt = Integer.parseInt((String) map.get(Integer.valueOf(9)));
        quVar.Ot = new ArrayList();
        quVar.Ot.add(str2.toLowerCase());
        quVar.mDescription = !z ? (String) map.get(Integer.valueOf(8)) : (String) map.get(Integer.valueOf(18));
        if (TextUtils.isEmpty(quVar.mDescription)) {
            quVar.mDescription = "Data Cache";
        }
        quVar.Nu = rg.dg((String) map.get(Integer.valueOf(19)));
        quVar.Ne = (String) map.get(Integer.valueOf(20));
        quVar.Ou = (String) map.get(Integer.valueOf(21));
        quVar.Ov = (int) rg.df((String) map.get(Integer.valueOf(22)));
        return quVar;
    }

    private a a(b bVar, String str, String[] -l_9_R, QFile qFile) {
        if (bVar == null || bVar.PN == null || bVar.PN.size() == 0 || -l_9_R == null) {
            return null;
        }
        TreeMap treeMap = null;
        Object obj = null;
        String str2 = "";
        if (-l_9_R.length > 2) {
            int i = 0;
            for (String str3 : -l_9_R) {
                i++;
                if (i == -l_9_R.length) {
                    break;
                }
                if (!(str3 == null || str3.equals(""))) {
                    str2 = str2 + "/" + str3;
                }
            }
        }
        if (this.PC == null) {
            this.PC = str2;
        } else {
            if (str2.equals(this.PC)) {
                treeMap = this.PD;
                obj = 1;
            } else {
                this.PC = str2;
                this.PD = null;
            }
        }
        if (this.PC == null) {
            return null;
        }
        int i2;
        if (treeMap == null && obj == null) {
            List list;
            treeMap = new TreeMap();
            String str4 = "";
            i2 = 0;
            String[] strArr = -l_9_R;
            for (String str5 : -l_9_R) {
                i2++;
                if (i2 == -l_9_R.length) {
                    break;
                }
                if (!(str5 == null || str5.equals(""))) {
                    str4 = str4 + "/" + str5;
                    list = (List) bVar.PO.get(str4);
                    if (list == null) {
                        continue;
                    } else if (bVar.PS) {
                        List list2 = (List) treeMap.get(Integer.valueOf(i2));
                        if (list2 == null) {
                            list2 = new ArrayList();
                            treeMap.put(Integer.valueOf(i2), list2);
                        }
                        list2.addAll(list);
                    } else {
                        ArrayList arrayList = new ArrayList();
                        arrayList.add(list.get(0));
                        treeMap.put(Integer.valueOf(i2), arrayList);
                        this.PD = treeMap;
                        return (a) list.get(0);
                    }
                }
            }
            long currentTimeMillis = System.currentTimeMillis();
            for (Entry entry : bVar.PQ.entrySet()) {
                if (PB != null) {
                    if (PB.isMatchPath(this.PC, (String) entry.getKey())) {
                        list = (List) treeMap.get(Integer.valueOf(-l_9_R.length - 1));
                        if (list == null) {
                            list = new ArrayList();
                            treeMap.put(Integer.valueOf(-l_9_R.length - 1), list);
                        }
                        list.addAll((Collection) entry.getValue());
                    }
                }
            }
            this.Pu += System.currentTimeMillis() - currentTimeMillis;
            long currentTimeMillis2 = System.currentTimeMillis();
            for (Entry entry2 : bVar.PP.entrySet()) {
                String[] strArr2 = ((a) ((List) entry2.getValue()).get(0)).PM;
                if (a(strArr2, -l_9_R)) {
                    if (bVar.PS) {
                        List list3 = (List) treeMap.get(Integer.valueOf(strArr2.length));
                        if (list3 == null) {
                            list3 = new ArrayList();
                            treeMap.put(Integer.valueOf(strArr2.length), list3);
                        }
                        list3.addAll((Collection) entry2.getValue());
                    } else {
                        ArrayList arrayList2 = new ArrayList();
                        arrayList2.add(((List) entry2.getValue()).get(0));
                        treeMap.put(Integer.valueOf(strArr2.length), arrayList2);
                        this.PD = treeMap;
                        return (a) ((List) entry2.getValue()).get(0);
                    }
                }
            }
            this.Pw += System.currentTimeMillis() - currentTimeMillis2;
        }
        if (treeMap == null || treeMap.size() == 0) {
            return null;
        }
        if (obj == null) {
            this.PD = treeMap;
        }
        QFile qFile2 = qFile;
        for (i2 = ((Integer) treeMap.lastKey()).intValue(); i2 > 0; i2--) {
            List<a> list4 = (List) treeMap.get(Integer.valueOf(i2));
            if (list4 != null) {
                a aVar = null;
                for (a aVar2 : list4) {
                    if (aVar2.Ow.equals("4")) {
                        if (qFile2 == null) {
                            qFile2 = new QFile(Environment.getExternalStorageDirectory().getAbsolutePath() + str);
                            qFile2.fillExtraInfo();
                        }
                        if (PB != null) {
                            if (aVar2.mFileName.equals("") || PB.isMatchFile(-l_9_R[-l_9_R.length - 1], aVar2.mFileName)) {
                                if (!aVar2.PH.equals("")) {
                                    if (!PB.isMatchFileSize(qFile2.size, aVar2.PH)) {
                                    }
                                }
                                if (!aVar2.PI.equals("")) {
                                    if (!PB.isMatchTime(qFile2.createTime, aVar2.PI)) {
                                    }
                                }
                                if (!aVar2.PJ.equals("")) {
                                    if (!PB.isMatchTime(qFile2.modifyTime, aVar2.PJ)) {
                                    }
                                }
                                if (!aVar2.PK.equals("")) {
                                    if (!PB.isMatchTime(qFile2.accessTime, aVar2.PK)) {
                                    }
                                }
                            }
                        }
                    }
                    if (aVar == null || ((aVar.Ow.equals("3") && aVar2.Ow.equals("4")) || (!aVar2.Ow.equals("3") && a(aVar) < a(aVar2)))) {
                        aVar = aVar2;
                    }
                }
                if (aVar != null) {
                    return aVar;
                }
            }
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:2:0x0003, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean a(String[] strArr, String[] strArr2) {
        if (strArr == null || strArr2 == null || strArr.length >= strArr2.length) {
            return false;
        }
        int i = 0;
        while (i < strArr.length) {
            if (!strArr[i].equals("*") && !strArr[i].equals(strArr2[i])) {
                return false;
            }
            i++;
        }
        return true;
    }

    private b b(String str, String[] strArr) {
        if (str == null || strArr == null) {
            return null;
        }
        b c;
        if (this.PA == null || this.PA.MB == null || !str.startsWith(this.PA.MB)) {
            long currentTimeMillis = System.currentTimeMillis();
            c = c(str, strArr);
            this.Pz += System.currentTimeMillis() - currentTimeMillis;
            this.PA = c;
        } else {
            c = this.PA;
        }
        return c;
    }

    private b c(String str, String[] -l_4_R) {
        if (str == null || -l_4_R == null) {
            return null;
        }
        String str2 = "";
        for (String str3 : -l_4_R) {
            if (!(str3 == null || str3.equals(""))) {
                str2 = str2 + "/" + str3;
                b bVar = (b) this.PE.get(str2);
                if (bVar != null) {
                    return bVar;
                }
            }
        }
        return null;
    }

    private void kk() {
        ArrayList kn = rm.km().kn();
        if (kn != null) {
            Iterator it = kn.iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                b bVar = new b();
                bVar.MB = str;
                this.PE.put(str, bVar);
            }
        }
    }

    private String m(String str, boolean z) {
        String[] o = o(str, z);
        return o != null ? o[1] : null;
    }

    private String n(String str, boolean z) {
        String[] o = o(str, z);
        return o != null ? o[0] : null;
    }

    private String[] o(String str, boolean z) {
        return p(str, z);
    }

    private String[] p(String str, boolean z) {
        Map j = rm.km().j(str, z);
        if (j == null || j.size() == 0) {
            return null;
        }
        String str2 = null;
        String str3 = null;
        List arrayList = new ArrayList();
        List arrayList2 = new ArrayList();
        for (String str4 : j.keySet()) {
            try {
                arrayList.add(str4);
                arrayList2.add(j.get(str4));
            } catch (Exception e) {
            }
        }
        rj rjVar = new rj();
        rjVar.init();
        String str42 = rjVar.J(arrayList);
        if (str42 != null) {
            ov a = TMServiceFactory.getSystemInfoService().a(str42, 2048);
            if (a != null) {
                str3 = str42;
                str2 = a.getAppName();
                if (str2 == null) {
                    str2 = rjVar.cS(str42);
                }
            }
        } else {
            int K = rjVar.K(arrayList);
            if (K == -1) {
                K = 0;
            }
            str3 = (String) arrayList.get(K);
            str2 = (String) arrayList2.get(K);
        }
        if (str2 == null || str3 == null) {
            return new String[]{null, null};
        }
        f.e("xx", str3 + "  " + str2);
        return new String[]{str3.trim(), str2.trim()};
    }

    private List<a> q(String str, boolean z) {
        if (str == null) {
            return null;
        }
        byte[] bArr = null;
        try {
            JceInputStream jceInputStream;
            byte[] dm = rm.km().dm(str);
            if (dm != null) {
                jceInputStream = new JceInputStream(dm);
                jceInputStream.setServerEncoding("UTF-8");
                am amVar = new am();
                amVar.readFrom(jceInputStream);
                bArr = amVar.bw;
            }
            if (bArr != null) {
                jceInputStream = new JceInputStream(TccCryptor.decrypt(bArr, null));
                jceInputStream.setServerEncoding("UTF-8");
                ak akVar = new ak();
                akVar.readFrom(jceInputStream);
                List<a> arrayList = new ArrayList();
                Iterator it = akVar.br.iterator();
                while (it.hasNext()) {
                    Map map = (Map) it.next();
                    a aVar;
                    if (map.get(Integer.valueOf(3)) != null) {
                        aVar = new a();
                        aVar.Ow = "3";
                        aVar.PG = (String) map.get(Integer.valueOf(9));
                        aVar.mPath = ((String) map.get(Integer.valueOf(3))).toLowerCase();
                        aVar.PF = str;
                        if (aVar.mPath.equals("/")) {
                            aVar.mPath = str;
                        } else {
                            aVar.mPath = str + aVar.mPath.toLowerCase();
                        }
                        aVar.mDesc = !z ? (String) map.get(Integer.valueOf(8)) : (String) map.get(Integer.valueOf(18));
                        if (aVar.mDesc == null) {
                            aVar.mDesc = (String) map.get(Integer.valueOf(8));
                        }
                        aVar.Ox = (String) map.get(Integer.valueOf(10));
                        arrayList.add(aVar);
                    } else if (map.get(Integer.valueOf(4)) != null) {
                        aVar = new a();
                        aVar.Ow = "4";
                        aVar.PG = (String) map.get(Integer.valueOf(9));
                        aVar.mPath = ((String) map.get(Integer.valueOf(4))).toLowerCase();
                        aVar.PF = str;
                        if (aVar.mPath.equals("/")) {
                            aVar.mPath = str;
                        } else {
                            aVar.mPath = str + aVar.mPath.toLowerCase();
                        }
                        aVar.mDesc = !z ? (String) map.get(Integer.valueOf(8)) : (String) map.get(Integer.valueOf(18));
                        if (aVar.mDesc == null) {
                            aVar.mDesc = (String) map.get(Integer.valueOf(8));
                        }
                        aVar.mFileName = (String) map.get(Integer.valueOf(11));
                        aVar.PH = (String) map.get(Integer.valueOf(12));
                        aVar.PI = (String) map.get(Integer.valueOf(13));
                        aVar.PJ = (String) map.get(Integer.valueOf(14));
                        aVar.PK = (String) map.get(Integer.valueOf(15));
                        arrayList.add(aVar);
                    }
                }
                return arrayList;
            }
            f.e("xx", "null:" + str);
            return null;
        } catch (Exception e) {
            f.e("xx", e.getMessage());
            return null;
        }
    }

    public String a(String str, QFile qFile, boolean z) {
        if (str == null) {
            return null;
        }
        String[] split = str.split("/");
        b b = b(str, split);
        if (b == null) {
            return null;
        }
        if (this.Pt) {
            if (b.PN == null || b.PN.size() == 0) {
                b.PN = q(b.MB, z);
            }
            if (b.mAppName == null) {
                b.mAppName = m(b.MB, z);
            }
            if (b.mPkg == null) {
                b.mPkg = n(b.MB, z);
            }
        }
        if (b.PN == null || b.PN.size() == 0) {
            return b.mAppName;
        }
        b.kl();
        long currentTimeMillis = System.currentTimeMillis();
        a a = a(b, str, split, qFile);
        this.Py += System.currentTimeMillis() - currentTimeMillis;
        return a != null ? b.mAppName + " " + a.mDesc : b.mAppName;
    }

    protected Map<String, ov> jY() {
        ArrayList f = TMServiceFactory.getSystemInfoService().f(73, 2);
        int size = f.size();
        Map<String, ov> hashMap = new HashMap();
        for (int i = 0; i < size; i++) {
            hashMap.put(((ov) f.get(i)).getPackageName(), f.get(i));
        }
        return hashMap;
    }

    public String k(String str, boolean z) {
        if (str == null) {
            return null;
        }
        b b = b(str, str.split("/"));
        if (b == null) {
            return null;
        }
        if (b.mPkg == null) {
            String[] o = o(b.MB, z);
            if (o != null) {
                b.mPkg = o[0];
                b.mAppName = o[1];
            }
        }
        return b.mPkg;
    }

    public void kj() {
        boolean isENG = ((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG();
        m.T(isENG);
        qn qnVar = new qn();
        this.OK = jY();
        this.Pt = qnVar.jv();
        if (this.Pt) {
            kk();
        } else {
            Y(isENG);
        }
    }

    public String l(String str, boolean z) {
        if (str == null) {
            return null;
        }
        b b = b(str, str.split("/"));
        if (b == null) {
            return null;
        }
        if (b.mAppName == null) {
            String[] o = o(b.MB, z);
            if (o != null) {
                b.mPkg = o[0];
                b.mAppName = o[1];
            }
        }
        return b.mAppName;
    }
}
