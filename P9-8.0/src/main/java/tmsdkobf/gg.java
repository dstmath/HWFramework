package tmsdkobf;

import android.os.Environment;
import android.text.TextUtils;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import com.qq.taf.jce.JceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import tmsdk.common.TMSDKContext;
import tmsdkobf.nj.a;
import tmsdkobf.nq.b;

public class gg {
    private static int VERSION = 3;
    public static String nY;
    private static gg ob = null;
    private jx nZ = gf.S().U();
    private gm oa = new gm();

    static {
        nY = null;
        try {
            nY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/.tmfs/" + "sk_v" + (!TMSDKContext.getStrFromEnvMap(TMSDKContext.PRE_IS_TEST).equals("true") ? "" : "_test") + ".dat";
        } catch (Throwable th) {
        }
    }

    private gg() {
        am();
    }

    private long ac(String str) {
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean ad(String str) {
        try {
            return Boolean.parseBoolean(str);
        } catch (Exception e) {
            return false;
        }
    }

    private int ae(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean af(String str) {
        try {
            return Boolean.parseBoolean(str);
        } catch (Exception e) {
            return false;
        }
    }

    public static gg al() {
        if (ob == null) {
            Class cls = gg.class;
            synchronized (gg.class) {
                if (ob == null) {
                    ob = new gg();
                }
            }
        }
        return ob;
    }

    private synchronized void am() {
        if (this.nZ != null) {
            int i = this.nZ.getInt("key_shark_dao_ver", -1);
            if (i < 1) {
                a(aw());
            }
            if (i < 2) {
                Object an = an();
                Object ao = ao();
                if (!(TextUtils.isEmpty(an) || TextUtils.isEmpty(ao))) {
                    mb.n("SharkDao", "translate rsakey...");
                    b bVar = new b();
                    bVar.DX = an;
                    bVar.DW = ao;
                    a(bVar);
                }
            }
            this.nZ.putInt("key_shark_dao_ver", VERSION);
        }
    }

    private String an() {
        return kk.d(TMSDKContext.getApplicaionContext(), this.nZ.getString("key_ek", ""));
    }

    private String ao() {
        return kk.d(TMSDKContext.getApplicaionContext(), this.nZ.getString("key_sid", ""));
    }

    private LinkedHashMap<String, a> ay() {
        LinkedHashMap<String, a> linkedHashMap = new LinkedHashMap();
        Object d = kk.d(TMSDKContext.getApplicaionContext(), this.nZ.getString("key_hips", ""));
        if (TextUtils.isEmpty(d)) {
            mb.n("SharkDao", "[ip_list]getAllHIPListInfos(), none is saved");
            return linkedHashMap;
        }
        String[] split = d.split("\\|");
        if (split == null || split.length == 0) {
            mb.s("SharkDao", "[ip_list]getAllHIPListInfos(), item number is 0!");
            return linkedHashMap;
        }
        String[] strArr = split;
        for (Object obj : split) {
            if (!TextUtils.isEmpty(obj)) {
                String[] split2 = obj.split(",");
                if (split2 != null && split2.length > 0) {
                    try {
                        Object obj2 = split2[0];
                        long parseLong = Long.parseLong(split2[1]);
                        String[] split3 = split2[2].split("#");
                        if (split3 != null) {
                            linkedHashMap.put(obj2, new a(parseLong, nj.a(Arrays.asList(split3), false), false));
                        }
                    } catch (Exception e) {
                        mb.o("SharkDao", "[ip_list]getAllHIPListInfos() exception: " + e);
                    }
                }
            }
        }
        mb.n("SharkDao", "[ip_list]getAllHIPListInfos(), size: " + linkedHashMap.size());
        return linkedHashMap;
    }

    public void Z(String str) {
        String c = kk.c(TMSDKContext.getApplicaionContext(), str);
        if (c != null) {
            this.nZ.putString("key_gd", c);
        }
    }

    public void a(String str, long j, List<String> list) {
        if (str != null) {
            String str2;
            Object obj = (((j > 0 ? 1 : (j == 0 ? 0 : -1)) <= 0 ? 1 : null) != null || list == null) ? 1 : null;
            mb.d("SharkDao", "[ip_list]setHIPListInfo(), op=" + (obj == null ? "[set] " : "[delete] ") + "|key=" + str);
            LinkedHashMap ay = ay();
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            for (Entry entry : ay.entrySet()) {
                str2 = (String) entry.getKey();
                a aVar = (a) entry.getValue();
                if (!(str2 == null || aVar == null)) {
                    if (aVar.isValid()) {
                        linkedHashMap.put(str2, aVar);
                    } else {
                        mb.o("SharkDao", "[ip_list]setHIPListInfo(), remove expired:　" + str2);
                    }
                }
            }
            if (obj == null) {
                a aVar2 = new a(j, list, false);
                if (aVar2.isValid()) {
                    linkedHashMap.put(str, aVar2);
                }
            } else {
                linkedHashMap.remove(str);
            }
            if (linkedHashMap.size() > 10) {
                ArrayList arrayList = new ArrayList(linkedHashMap.keySet());
                mb.n("SharkDao", "[ip_list]setHIPListInfo(), too manay, keyList: " + arrayList);
                String str3 = (String) arrayList.get(0);
                linkedHashMap.remove(str3);
                mb.n("SharkDao", "[ip_list]setHIPListInfo(), too manay, remove firstKey: " + str3);
            }
            StringBuilder stringBuilder = new StringBuilder();
            int i = 0;
            for (Entry entry2 : linkedHashMap.entrySet()) {
                String str4 = (String) entry2.getKey();
                a aVar3 = (a) entry2.getValue();
                if (!(str4 == null || aVar3 == null)) {
                    long j2 = aVar3.DG;
                    if ((j2 > System.currentTimeMillis() ? 1 : null) != null) {
                        StringBuilder stringBuilder2 = new StringBuilder();
                        int i2 = 0;
                        for (String str5 : aVar3.DH) {
                            if (i2 > 0) {
                                stringBuilder2.append("#");
                            }
                            stringBuilder2.append(str5);
                            i2++;
                        }
                        StringBuilder stringBuilder3 = new StringBuilder();
                        stringBuilder3.append(str4).append(",").append(j2).append(",").append(stringBuilder2.toString());
                        if (i > 0) {
                            stringBuilder.append("|");
                        }
                        stringBuilder.append(stringBuilder3.toString());
                        i++;
                    }
                }
            }
            mb.n("SharkDao", "[ip_list]setHIPListInfo(), new size: " + i + ", before encode: " + stringBuilder.toString());
            str2 = kk.c(TMSDKContext.getApplicaionContext(), stringBuilder.toString());
            if (str2 != null) {
                this.nZ.putString("key_hips", str2);
                return;
            } else {
                mb.o("SharkDao", "[ip_list]getEncodeString for HIPLists failed");
                return;
            }
        }
        mb.o("SharkDao", "[ip_list]setHIPListInfo(), bad arg, key == null");
    }

    public void a(br brVar) {
        try {
            this.oa.a(10000, brVar.toByteArray("UTF-8"));
        } catch (Throwable th) {
            mb.e("SharkDao", th);
        }
    }

    public void a(h hVar) {
        if (hVar != null) {
            byte[] d = nn.d(hVar);
            if (d != null) {
                String bytesToHexString = com.tencent.tcuser.util.a.bytesToHexString(d);
                if (bytesToHexString != null) {
                    String c = kk.c(TMSDKContext.getApplicaionContext(), bytesToHexString);
                    if (c != null) {
                        this.nZ.putString("key_s_c", c);
                        mb.n("SharkDao", "[shark_conf]setSharkConf() succ");
                    }
                }
            }
        }
    }

    public void a(b bVar) {
        String str = "" + bVar.DX + "|" + bVar.DW;
        mb.n("SharkDao", "[rsa_key]setRsaKey(), str: " + str);
        String c = kk.c(TMSDKContext.getApplicaionContext(), str);
        if (c != null) {
            this.nZ.putString("key_rsa", c);
        }
    }

    public void aa(String str) {
        String c = kk.c(TMSDKContext.getApplicaionContext(), str);
        if (c != null) {
            this.nZ.putString("key_vd", c);
            mb.n("SharkDao", "[cu_vid] setVidInPhone() vid: " + str);
        }
    }

    public void ab(String str) {
        String c = kk.c(TMSDKContext.getApplicaionContext(), str);
        if (c != null) {
            boolean z = false;
            if (nY != null) {
                z = kl.a(c.getBytes(), nY);
            }
            mb.n("SharkDao", "[cu_vid] setVidInSD(), vid: " + str + " isSaved: " + z);
        }
    }

    public a ag(String str) {
        return (a) ay().get(str);
    }

    public b ap() {
        String d = kk.d(TMSDKContext.getApplicaionContext(), this.nZ.getString("key_rsa", ""));
        if (TextUtils.isEmpty(d)) {
            return null;
        }
        int indexOf = d.indexOf("|");
        if (indexOf <= 0 || indexOf >= d.length() - 1) {
            return null;
        }
        b bVar = new b();
        bVar.DX = d.substring(0, indexOf);
        bVar.DW = d.substring(indexOf + 1);
        return bVar;
    }

    public String aq() {
        String d = kk.d(TMSDKContext.getApplicaionContext(), this.nZ.getString("key_vd", ""));
        mb.n("SharkDao", "[cu_vid] getVidInPhone() vid: " + d);
        return d;
    }

    public String ar() {
        String str = null;
        byte[] aV = kl.aV(nY);
        if (aV != null) {
            str = kk.d(TMSDKContext.getApplicaionContext(), new String(aV));
        }
        mb.n("SharkDao", "[cu_vid] getVidInSD(), vid: " + str);
        return str;
    }

    public String as() {
        return kk.d(TMSDKContext.getApplicaionContext(), this.nZ.getString("key_gd", ""));
    }

    public String at() {
        return kk.d(TMSDKContext.getApplicaionContext(), this.nZ.getString("key_ws_gd", null));
    }

    public long au() {
        try {
            return Long.parseLong(kk.d(TMSDKContext.getApplicaionContext(), this.nZ.getString("key_gd_ck_tm", "")));
        } catch (Exception e) {
            return 0;
        }
    }

    public br av() {
        br brVar = new br();
        try {
            byte[] M = this.oa.M(10000);
            if (M != null) {
                JceInputStream jceInputStream = new JceInputStream(M);
                jceInputStream.setServerEncoding("UTF-8");
                brVar.readFrom(jceInputStream);
            }
        } catch (Throwable th) {
            mb.e("SharkDao", th);
        }
        return brVar;
    }

    @Deprecated
    public br aw() {
        br brVar = new br();
        brVar.dl = this.oa.L(1);
        if (brVar.dl == null) {
            brVar.dl = "";
        }
        brVar.imsi = this.oa.L(2);
        brVar.dU = this.oa.L(32);
        brVar.dm = this.oa.L(3);
        brVar.dn = this.oa.L(4);
        brVar.do = this.oa.L(5);
        brVar.dp = ae(this.oa.L(6));
        brVar.dq = this.oa.L(7);
        brVar.L = ae(this.oa.L(8));
        brVar.dr = this.oa.L(9);
        brVar.ds = ae(this.oa.L(10));
        brVar.dt = ae(this.oa.L(11));
        brVar.du = af(this.oa.L(12));
        brVar.dv = this.oa.L(13);
        brVar.dw = this.oa.L(14);
        brVar.dx = ae(this.oa.L(15));
        brVar.dy = this.oa.L(16);
        brVar.dz = (short) ((short) ae(this.oa.L(17)));
        brVar.dA = ae(this.oa.L(18));
        brVar.dB = this.oa.L(19);
        brVar.ed = this.oa.L(36);
        brVar.dC = this.oa.L(20);
        brVar.dD = ae(this.oa.L(21));
        brVar.dE = this.oa.L(22);
        brVar.dF = ac(this.oa.L(23));
        brVar.dG = ac(this.oa.L(24));
        brVar.dH = ac(this.oa.L(25));
        brVar.ei = ac(this.oa.L(41));
        brVar.dI = this.oa.L(26);
        brVar.dJ = this.oa.L(27);
        brVar.dK = this.oa.L(28);
        brVar.version = this.oa.L(29);
        brVar.dY = ae(this.oa.L(30));
        brVar.dZ = this.oa.L(31);
        brVar.dN = this.oa.L(44);
        brVar.dQ = this.oa.g(45, -1);
        brVar.dR = this.oa.g(46, -1);
        brVar.ea = this.oa.L(33);
        brVar.eb = this.oa.L(34);
        brVar.ec = this.oa.L(35);
        brVar.ee = this.oa.L(37);
        brVar.ef = this.oa.L(38);
        brVar.eg = this.oa.L(39);
        brVar.eh = this.oa.L(40);
        brVar.dO = this.oa.L(50);
        brVar.ej = this.oa.L(42);
        brVar.dP = this.oa.L(47);
        brVar.dL = this.oa.L(48);
        brVar.dM = this.oa.L(49);
        brVar.ek = this.oa.L(43);
        brVar.dS = ad(this.oa.L(51));
        brVar.el = ae(this.oa.L(52));
        return brVar;
    }

    public boolean ax() {
        return af(this.oa.L(CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY));
    }

    public h az() {
        Object d = kk.d(TMSDKContext.getApplicaionContext(), this.nZ.getString("key_s_c", ""));
        return TextUtils.isEmpty(d) ? null : (h) nn.a(com.tencent.tcuser.util.a.at(d), new h(), false);
    }

    public void e(boolean z) {
        this.oa.b(CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY, Boolean.toString(z));
    }

    public void f(long j) {
        String c = kk.c(TMSDKContext.getApplicaionContext(), Long.toString(j));
        if (c != null) {
            this.nZ.putString("key_gd_ck_tm", c);
        }
    }
}
