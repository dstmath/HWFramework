package tmsdk.common.module.qscanner.impl;

import android.text.TextUtils;
import com.tencent.tcuser.util.a;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import tmsdk.common.utils.f;
import tmsdkobf.de;
import tmsdkobf.di;
import tmsdkobf.dq;

public class c {
    public static ArrayList<b> a(ArrayList<Integer> arrayList, Map<Integer, de> map) {
        if (arrayList == null || map == null) {
            return null;
        }
        ArrayList<b> arrayList2 = new ArrayList();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            Integer num = (Integer) it.next();
            de deVar = (de) map.get(num);
            if (deVar == null) {
                f.g("QScanInternalUtils", "[virus_scan]cannot find AD plugin info for id: " + num);
            } else {
                b bVar = new b();
                bVar.id = num.intValue();
                bVar.type = deVar.gq;
                bVar.BR = (((long) deVar.gs) << 32) | ((long) deVar.gr);
                bVar.banUrls = deVar.gt;
                bVar.banIps = deVar.gu;
                bVar.name = deVar.gv;
                arrayList2.add(bVar);
            }
        }
        return arrayList2;
    }

    public static di a(e eVar, int i) {
        if (eVar == null) {
            return null;
        }
        Iterator it;
        di diVar = new di();
        diVar.gI = i;
        diVar.gJ = null;
        diVar.gK = eVar.packageName;
        diVar.gL = a.at(eVar.bZ);
        ArrayList arrayList = new ArrayList();
        ArrayList ca = ca(eVar.Cb);
        int i2 = 0;
        if (ca.size() > 0) {
            it = ca.iterator();
            while (it.hasNext()) {
                ArrayList arrayList2 = (ArrayList) it.next();
                ArrayList arrayList3 = new ArrayList(arrayList2.size());
                Iterator it2 = arrayList2.iterator();
                while (it2.hasNext()) {
                    arrayList3.add(a.at((String) it2.next()));
                    i2++;
                }
                arrayList.add(arrayList3);
            }
        }
        if (i2 > 1) {
            diVar.ha = arrayList;
        }
        diVar.gM = (long) eVar.size;
        diVar.gN = eVar.softName;
        diVar.gO = eVar.versionCode;
        diVar.gP = eVar.version;
        diVar.gQ = 0;
        if (eVar.BQ == 1) {
            diVar.gQ |= 1;
        }
        if (eVar.BQ == 1 || eVar.BQ == 0) {
            diVar.gQ |= 2;
            if (eVar.Cn) {
                diVar.gQ |= 4;
            }
        }
        diVar.gR = eVar.cc;
        diVar.gS = eVar.gS;
        diVar.gn = eVar.type;
        diVar.gT = eVar.BU;
        diVar.gU = eVar.category;
        if (eVar.plugins == null || eVar.plugins.size() == 0) {
            diVar.gV = null;
        } else {
            diVar.gV = new ArrayList(eVar.plugins.size());
            it = eVar.plugins.iterator();
            while (it.hasNext()) {
                diVar.gV.add(Integer.valueOf(((b) it.next()).id));
            }
        }
        diVar.gW = 0;
        if (eVar.Cg) {
            diVar.gW |= 1;
        }
        if (eVar.Ch) {
            diVar.gW |= 2;
        }
        diVar.gX = eVar.Cm;
        diVar.gY = eVar.dp;
        diVar.official = eVar.official;
        return diVar;
    }

    public static void a(d dVar, e eVar) {
        if (eVar != null && dVar != null) {
            eVar.packageName = dVar.BS.nf;
            eVar.softName = dVar.BS.softName;
            eVar.version = dVar.BS.version;
            eVar.versionCode = dVar.BS.versionCode;
            eVar.path = dVar.BS.path;
            eVar.BQ = dVar.BS.BQ;
            eVar.size = dVar.BS.size;
            eVar.type = dVar.type;
            eVar.lL = dVar.lL;
            eVar.BU = dVar.BU;
            eVar.name = dVar.name;
            eVar.label = dVar.label;
            eVar.BT = dVar.BT;
            eVar.url = dVar.url;
            eVar.gS = dVar.lP;
            eVar.dp = dVar.dp;
            eVar.cc = dVar.BW;
            eVar.plugins = o(dVar.plugins);
            eVar.name = dVar.name;
            eVar.category = dVar.category;
        }
    }

    public static ArrayList<ArrayList<String>> ca(String str) {
        ArrayList<ArrayList<String>> arrayList = new ArrayList();
        if (TextUtils.isEmpty(str)) {
            return arrayList;
        }
        try {
            String[] split = str.split(";");
            if (split != null && split.length > 0) {
                String[] strArr = split;
                for (Object obj : split) {
                    if (!TextUtils.isEmpty(obj)) {
                        String[] split2 = obj.split(",");
                        if (split2 != null && split2.length > 0) {
                            ArrayList arrayList2 = new ArrayList();
                            String[] strArr2 = split2;
                            for (CharSequence charSequence : split2) {
                                if (!TextUtils.isEmpty(charSequence)) {
                                    arrayList2.add(charSequence);
                                }
                            }
                            if (arrayList2.size() > 0) {
                                arrayList.add(arrayList2);
                            }
                        }
                    }
                }
            }
        } catch (Throwable th) {
            f.b("QScanInternalUtils", "str2aalStr: " + th, th);
        }
        return arrayList;
    }

    private static ArrayList<b> o(List<dq> list) {
        if (list == null) {
            return null;
        }
        ArrayList<b> arrayList = new ArrayList(list.size() + 1);
        for (dq dqVar : list) {
            b bVar = new b();
            bVar.id = dqVar.id;
            bVar.type = dqVar.type;
            bVar.BR = (((long) dqVar.hN) << 32) | ((long) dqVar.hM);
            bVar.banUrls = dqVar.banUrls;
            bVar.banIps = dqVar.banIps;
            bVar.name = dqVar.name;
            arrayList.add(bVar);
        }
        return arrayList;
    }

    public static String q(ArrayList<ArrayList<String>> arrayList) {
        StringBuilder stringBuilder = new StringBuilder();
        if (arrayList != null) {
            for (int i = 0; i < arrayList.size(); i++) {
                if (i > 0) {
                    stringBuilder.append(";");
                }
                ArrayList arrayList2 = (ArrayList) arrayList.get(i);
                for (int i2 = 0; i2 < arrayList2.size(); i2++) {
                    if (i2 > 0) {
                        stringBuilder.append(",");
                    }
                    stringBuilder.append((String) arrayList2.get(i2));
                }
            }
        }
        return stringBuilder.toString();
    }
}
