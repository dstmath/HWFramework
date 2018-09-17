package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import com.qq.taf.jce.JceInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.m;

public class qs {
    public Map<String, qv> Od;

    private static qu a(String str, Map<Integer, String> map, String str2) {
        qu quVar = new qu();
        quVar.MB = str;
        quVar.Nt = Integer.parseInt((String) map.get(Integer.valueOf(9)));
        quVar.Ot = new ArrayList();
        quVar.Ot.add(str2.toLowerCase());
        quVar.mDescription = !m.iW() ? (String) map.get(Integer.valueOf(8)) : (String) map.get(Integer.valueOf(18));
        if (TextUtils.isEmpty(quVar.mDescription)) {
            quVar.mDescription = "Data Cache";
        }
        quVar.Nu = rg.dg((String) map.get(Integer.valueOf(19)));
        quVar.Ne = (String) map.get(Integer.valueOf(20));
        quVar.Ou = (String) map.get(Integer.valueOf(21));
        quVar.Ov = (int) rg.df((String) map.get(Integer.valueOf(22)));
        return quVar;
    }

    public static List<qu> b(String str, byte[] bArr) {
        byte[] bArr2 = ((am) nn.a(bArr, new am(), false)).bw;
        return bArr2 != null ? c(str, bArr2) : null;
    }

    private static List<qu> c(String str, byte[] bArr) {
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
                            a = a(str, map, str2);
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
                        a = a(str, map, str2);
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
                                a.mDescription += "(" + String.format(m.cF("in_recent_days"), new Object[]{Integer.valueOf(intValue)}) + ")";
                                a.Oo = "0," + intValue;
                                qu a2 = a(str, map, str2);
                                a2.Nt = 1;
                                a2.Oo = "" + intValue + ",-";
                                a2.mDescription += "(" + String.format(m.cF("days_ago"), new Object[]{Integer.valueOf(intValue)}) + ")";
                                a2.Ow = "3";
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

    private HashMap<String, String> jK() {
        ea eaVar = (ea) mk.b(TMSDKContext.getApplicaionContext(), UpdateConfig.intToString(40291) + ".dat", UpdateConfig.intToString(40291), new ea(), "UTF-8");
        if (eaVar == null || eaVar.iC == null) {
            return null;
        }
        HashMap<String, String> hashMap = new HashMap();
        Iterator it = eaVar.iC.iterator();
        while (it.hasNext()) {
            dz dzVar = (dz) it.next();
            if (!(dzVar.iu == null || dzVar.iv == null || dzVar.iw == null || !dzVar.iu.equals("1"))) {
                hashMap.put(dzVar.iv, dzVar.iw);
            }
        }
        return hashMap;
    }

    protected boolean T(Context context) {
        this.Od = new HashMap();
        al alVar = (al) mk.a(TMSDKContext.getApplicaionContext(), UpdateConfig.DEEPCLEAN_SDCARD_SCAN_RULE_NAME_V2_SDK, UpdateConfig.intToString(40415), new al(), "UTF-8");
        if (alVar == null || alVar.bt == null) {
            return false;
        }
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
                    String str2 = (String) map.get(Integer.valueOf(17));
                    String str3 = new String(TccCryptor.decrypt(lq.at(((String) map.get(Integer.valueOf(5))).toUpperCase()), null));
                    if (qvVar.Oz == null) {
                        qvVar.Oz = new HashMap();
                    }
                    if (m.iW() && !TextUtils.isEmpty(str2)) {
                        qvVar.Oz.put(str3, new String(TccCryptor.decrypt(lq.at(str2.toUpperCase()), null)));
                    } else {
                        qvVar.Oz.put(str3, new String(TccCryptor.decrypt(lq.at(str.toUpperCase()), null)));
                    }
                }
            }
            qvVar.Ot = c(qvVar.MB, amVar.bw);
            if (!(qvVar.Ot == null || qvVar.Ot.size() == 0 || qvVar.Oz == null || qvVar.Oz.size() == 0)) {
                this.Od.put(qvVar.MB, qvVar);
            }
        }
        return true;
    }

    protected boolean a(boolean z, qq qqVar) {
        if (qqVar == null) {
            return false;
        }
        ea eaVar = (ea) mk.b(TMSDKContext.getApplicaionContext(), UpdateConfig.intToString(40248) + ".dat", UpdateConfig.intToString(40248), new ea(), "UTF-8");
        if (eaVar == null || eaVar.iC == null) {
            return false;
        }
        HashMap hashMap = null;
        if (z) {
            hashMap = jK();
        }
        List arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        List arrayList3 = new ArrayList();
        Iterator it = eaVar.iC.iterator();
        while (it.hasNext()) {
            dz dzVar = (dz) it.next();
            qt qtVar = new qt();
            qtVar.Ok = dzVar.iv;
            if (hashMap != null) {
                qtVar.mDescription = (String) hashMap.get(dzVar.iw);
                qtVar.Oq = !TextUtils.isEmpty(dzVar.ix) ? (String) hashMap.get(dzVar.ix) : null;
            }
            if (qtVar.mDescription == null) {
                qtVar.mDescription = dzVar.iw;
            }
            if (qtVar.Oq == null) {
                qtVar.Oq = dzVar.ix;
            }
            qtVar.Or = !"1".equals(dzVar.iy);
            qtVar.Op = !TextUtils.isEmpty(dzVar.iA) ? dzVar.iA : "0";
            if (!TextUtils.isEmpty(dzVar.iz)) {
                String[] split = dzVar.iz.split("&");
                if (split != null) {
                    String[] strArr = split;
                    for (String str : split) {
                        if (str.length() > 2) {
                            char charAt = str.charAt(0);
                            String substring = str.substring(2);
                            switch (charAt) {
                                case '1':
                                    qtVar.mFileName = substring;
                                    break;
                                case '2':
                                    qtVar.Ol = substring;
                                    break;
                                case '3':
                                    qtVar.Om = substring;
                                    break;
                                case '4':
                                    qtVar.On = substring;
                                    break;
                                case '5':
                                    qtVar.Oo = substring;
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }
            if ("1".equals(dzVar.iu)) {
                arrayList.add(qtVar);
            } else {
                if ("2".equals(dzVar.iu)) {
                    arrayList2.add(qtVar);
                } else {
                    if ("3".equals(dzVar.iu)) {
                        arrayList3.add(qtVar);
                    }
                }
            }
        }
        qt qtVar2 = new qt();
        qtVar2.mFileName = ".apk";
        qtVar2.Op = "0";
        arrayList.add(qtVar2);
        if (arrayList2 != null && arrayList2.size() > 0) {
            qtVar2.Op = ((qt) arrayList2.get(0)).Op;
        }
        qqVar.A(arrayList3);
        qqVar.z(arrayList);
        qqVar.B(arrayList2);
        qqVar.a(qtVar2);
        return true;
    }
}
