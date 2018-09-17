package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.l;
import tmsdk.common.utils.u;
import tmsdkobf.on.b;

public class nj implements om {
    private static a DE = null;
    private static a DF = null;
    public static final boolean Dw = TMSDKContext.getStrFromEnvMap(TMSDKContext.PRE_USE_IP_LIST).equals("true");
    private static String Dx = "mazu.3g.qq.com";
    private static nj Dy = null;
    private boolean CX = false;
    private final Object DA = new Object();
    private String DB = "key_notset";
    private a DC;
    private a DD;
    private nl Dz;
    private Context mContext;

    public static class a {
        public long DG;
        public List<String> DH = new ArrayList();
        public boolean DI = false;
        private int DJ = 0;

        public a(long j, List<String> list, boolean z) {
            this.DG = j;
            if (list != null) {
                this.DH.addAll(list);
            }
            this.DI = z;
        }

        private static String cf(String str) {
            if (TextUtils.isEmpty(str)) {
                return null;
            }
            String str2;
            int lastIndexOf = str.lastIndexOf(":");
            if (lastIndexOf < 0) {
                str2 = str + ":80";
                mb.o("HIPList", "conv2HttpIPPort(): invalid ipPort(missing port): " + str);
            } else {
                str2 = str.substring(0, lastIndexOf) + ":80";
            }
            if (str2.length() < "http://".length() || !str2.substring(0, "http://".length()).equalsIgnoreCase("http://")) {
                str2 = "http://" + str2;
            }
            return str2;
        }

        private a fL() {
            Collection linkedHashSet = new LinkedHashSet();
            for (String cf : this.DH) {
                String cf2 = cf(cf);
                if (cf2 != null) {
                    linkedHashSet.add(cf2);
                }
            }
            return new a(this.DG, new ArrayList(linkedHashSet), this.DI);
        }

        private b fM() {
            if (this.DJ >= this.DH.size()) {
                this.DJ = 0;
            }
            return nj.cc((String) this.DH.get(this.DJ));
        }

        private void fN() {
            this.DJ++;
            if (this.DJ >= this.DH.size()) {
                this.DJ = 0;
            }
        }

        private void fO() {
            this.DJ = 0;
        }

        private void s(List<String> list) {
            int size = this.DH.size();
            if (size < 2) {
                this.DH.addAll(nj.a(list, true));
            } else {
                this.DH.addAll(size - 1, nj.a(list, true));
            }
        }

        /* JADX WARNING: Missing block: B:8:0x001b, code:
            if ((java.lang.System.currentTimeMillis() > r6.DG) == false) goto L_0x0006;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean isValid() {
            if (!this.DI) {
            }
            if (this.DH.size() > 0) {
                return true;
            }
            return false;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("|mValidTimeMills=").append(this.DG).append("|mIsDefault=").append(this.DI).append("|mIPPortList=").append(this.DH);
            return stringBuilder.toString();
        }
    }

    public nj(Context context, boolean z, nl nlVar, String str) {
        mb.d("HIPList", "[ip_list]HIPList() isTest: " + z);
        this.mContext = context;
        this.CX = z;
        this.Dz = nlVar;
        if (TextUtils.isEmpty(str)) {
            String str2 = !this.CX ? this.Dz.fQ() != 1 ? "mazu.3g.qq.com" : "mazu-hk.3g.qq.com" : "mazutest.3g.qq.com";
            Dx = str2;
        } else {
            Dx = str;
        }
        if (Dw) {
            fG();
        } else {
            mb.s("HIPList", "[ip_list]HIPList(), not enable, use default");
            fH();
        }
        a(this);
    }

    private void A(boolean z) {
        a aVar;
        synchronized (this.DA) {
            aVar = !z ? this.DD : this.DC;
        }
        if (aVar == null) {
            fG();
        } else if (!aVar.isValid()) {
            fH();
        }
    }

    public static String a(nl nlVar) {
        return nlVar.fQ() != 1 ? "mazuburst.3g.qq.com" : "mazuburst-hk.3g.qq.com";
    }

    public static List<String> a(List<String> list, boolean z) {
        List arrayList = new ArrayList();
        if (list != null && list.size() > 0) {
            for (String str : list) {
                if (g(str, z)) {
                    arrayList.add(str);
                } else {
                    mb.o("HIPList", "[ip_list]drop invalid ipport: " + str);
                }
            }
        }
        return arrayList;
    }

    private void a(String str, a aVar, boolean z) {
        if (str == null || aVar == null || !aVar.isValid()) {
            mb.o("HIPList", "[ip_list]setWorkingHIPList(), bad arg or invalid, ignore");
            return;
        }
        a aVar2 = new a(aVar.DG, aVar.DH, aVar.DI);
        if (z) {
            aVar2.s(y(true));
            mb.n("HIPList", "[ip_list]setWorkingHIPList for " + (!this.CX ? " [release server]" : " [test server]") + ": " + aVar2.DH);
        }
        synchronized (this.DA) {
            this.DC = aVar2;
            this.DD = this.DC.fL();
            mb.n("HIPList", "[ip_list]setWorkingHIPList(), key changed: " + this.DB + " -> " + str);
            this.DB = str;
        }
    }

    public static void a(nj njVar) {
        Dy = njVar;
    }

    private String bm(int i) {
        String str = "" + (!this.CX ? "r_" : "t_");
        String str2 = "unknow";
        str2 = i != 1 ? "apn_" + i : !u.jh() ? "wifi_nonessid" : "wifi_" + u.getSSID();
        return str + str2;
    }

    private static b cc(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        int lastIndexOf = str.lastIndexOf(":");
        if (lastIndexOf <= 0 || lastIndexOf == str.length() - 1) {
            return null;
        }
        String substring = str.substring(0, lastIndexOf);
        String substring2 = str.substring(lastIndexOf + 1);
        if (TextUtils.isDigitsOnly(substring2)) {
            mb.n("HIPList", "[ip_list]getIPEndPointByStr(), ip: " + substring + " port: " + Integer.parseInt(substring2));
            return new b(substring, Integer.parseInt(substring2));
        }
        mb.n("HIPList", "[ip_list]getIPEndPointByStr(), invalid: " + str);
        return null;
    }

    private static boolean cd(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        try {
            if (str.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
                String[] split = str.split("\\.");
                return split.length >= 4 && Integer.parseInt(split[0]) <= 255 && Integer.parseInt(split[1]) <= 255 && Integer.parseInt(split[2]) <= 255 && Integer.parseInt(split[3]) <= 255;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private a f(String str, boolean z) {
        mb.n("HIPList", "[ip_list]loadSavedIPPortListInfo(), key: " + str);
        a ah = this.Dz.ah(str);
        if (ah == null) {
            mb.s("HIPList", "[ip_list]loadSavedIPPortListInfo(), no saved info for: " + str);
            return null;
        } else if (ah.isValid()) {
            a aVar = ah;
            mb.n("HIPList", "[ip_list]loadSavedIPPortListInfo(), saved info for: " + str + ": " + ah.toString());
            return aVar;
        } else {
            mb.s("HIPList", "[ip_list]loadSavedIPPortListInfo(), not valid");
            if (!z) {
                return null;
            }
            mb.s("HIPList", "[ip_list]loadSavedIPPortListInfo(), delete not valid info: " + str);
            this.Dz.b(str, 0, null);
            return null;
        }
    }

    public static nj fE() {
        return Dy;
    }

    /* JADX WARNING: Missing block: B:6:0x000d, code:
            r1 = f(r0, true);
     */
    /* JADX WARNING: Missing block: B:7:0x0011, code:
            if (r1 != null) goto L_0x004a;
     */
    /* JADX WARNING: Missing block: B:8:0x0013, code:
            fH();
     */
    /* JADX WARNING: Missing block: B:9:0x0016, code:
            return;
     */
    /* JADX WARNING: Missing block: B:24:0x004e, code:
            if (r1.isValid() == false) goto L_0x0013;
     */
    /* JADX WARNING: Missing block: B:25:0x0050, code:
            a(r0, r1, true);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void fG() {
        String fI = fI();
        synchronized (this.DA) {
            if (this.DB != null) {
                if (this.DB.equals(fI) && this.DC != null && this.DC.isValid()) {
                    mb.n("HIPList", "[ip_list]refreshWorkingIPList(), not necessary, key unchanged: " + fI);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:6:0x0012, code:
            a("key_default", x(true), false);
     */
    /* JADX WARNING: Missing block: B:7:0x001d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void fH() {
        mb.d("HIPList", "[ip_list]reset2Default()");
        synchronized (this.DA) {
            if (this.DB != null) {
                if (this.DB.equals("key_default") && this.DC != null && this.DC.isValid()) {
                    mb.n("HIPList", "[ip_list]reset2Default(), not necessary, key unchanged");
                }
            }
        }
    }

    private String fI() {
        String str = "" + (!this.CX ? "r_" : "t_");
        String str2 = "unknow";
        int w = nh.w(this.mContext);
        return str + (w != 1 ? "apn_" + w : "wifi_" + u.getSSID());
    }

    private int fK() {
        int S;
        if (4 != ln.yI) {
            S = l.S(this.mContext);
            if (-1 == S) {
                mb.d("HIPList", "[ip_list]getOperator(), unknow as china telecom");
                S = 2;
            }
        } else {
            mb.d("HIPList", "[ip_list]getOperator(), wifi as china telecom");
            S = 2;
        }
        mb.d("HIPList", "[ip_list]getOperator(), 0-mobile, 1-unicom, 2-telecom: " + S);
        return S;
    }

    private static boolean g(String str, boolean z) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        int lastIndexOf = str.lastIndexOf(":");
        if (lastIndexOf <= 0 || lastIndexOf == str.length() - 1) {
            return false;
        }
        return (z || cd(str.substring(0, lastIndexOf))) && TextUtils.isDigitsOnly(str.substring(lastIndexOf + 1));
    }

    private a x(boolean z) {
        if (z && DE != null) {
            return DE;
        }
        if (!z && DF != null) {
            return DF;
        }
        Collection y = y(z);
        Collection z2 = z(z);
        List arrayList = new ArrayList();
        arrayList.addAll(y);
        if (Dw) {
            arrayList.addAll(z2);
        }
        mb.n("HIPList", "[ip_list]getDefaultHIPListInfo for " + (!z ? "http" : "tcp") + (!this.CX ? " [release server]" : " [test server]") + ": " + arrayList);
        a aVar = new a(0, arrayList, true);
        if (z) {
            DE = aVar;
        } else {
            DF = aVar;
        }
        return aVar;
    }

    private List<String> y(boolean z) {
        List<String> arrayList = new ArrayList();
        List<Integer> arrayList2 = new ArrayList();
        if (z) {
            arrayList2.add(Integer.valueOf(443));
        } else {
            arrayList2.add(Integer.valueOf(80));
        }
        String str = Dx;
        for (Integer intValue : arrayList2) {
            int intValue2 = intValue.intValue();
            arrayList.add(String.format("%s:%d", new Object[]{str, Integer.valueOf(intValue2)}));
        }
        return arrayList;
    }

    private List<String> z(boolean z) {
        List<String> arrayList = new ArrayList();
        if (this.CX) {
            return arrayList;
        }
        List<Integer> arrayList2 = new ArrayList();
        if (z) {
            arrayList2.add(Integer.valueOf(443));
        } else {
            arrayList2.add(Integer.valueOf(80));
        }
        if (this.Dz.fQ() != 1) {
            String str;
            switch (fK()) {
                case 0:
                    str = "183.232.125.162";
                    break;
                case 1:
                    str = "163.177.71.153";
                    break;
                default:
                    str = "120.198.203.156";
                    break;
            }
            for (Integer intValue : arrayList2) {
                int intValue2 = intValue.intValue();
                arrayList.add(String.format("%s:%d", new Object[]{str, Integer.valueOf(intValue2)}));
            }
        } else {
            for (Integer intValue3 : arrayList2) {
                int intValue4 = intValue3.intValue();
                arrayList.add(String.format("%s:%d", new Object[]{"203.205.143.147", Integer.valueOf(intValue4)}));
                arrayList.add(String.format("%s:%d", new Object[]{"203.205.146.46", Integer.valueOf(intValue4)}));
                arrayList.add(String.format("%s:%d", new Object[]{"203.205.146.45", Integer.valueOf(intValue4)}));
            }
        }
        return arrayList;
    }

    public b B(boolean z) {
        A(true);
        synchronized (this.DA) {
            a aVar = !z ? this.DD : this.DC;
            if (aVar == null) {
                return null;
            }
            b b = aVar.fM();
            return b;
        }
    }

    public void C(boolean z) {
        A(true);
        synchronized (this.DA) {
            a aVar = !z ? this.DD : this.DC;
            if (aVar != null) {
                aVar.fN();
            }
        }
    }

    public void D(boolean z) {
        A(true);
        synchronized (this.DA) {
            a aVar = !z ? this.DD : this.DC;
            if (aVar != null) {
                aVar.fO();
            }
        }
    }

    public void E(boolean z) {
    }

    public ArrayList<String> F(boolean z) {
        A(true);
        synchronized (this.DA) {
            a aVar = !z ? this.DD : this.DC;
            if (aVar == null) {
                return null;
            }
            ArrayList<String> arrayList = (ArrayList) aVar.DH;
            return arrayList;
        }
    }

    public int G(boolean z) {
        ArrayList F = F(z);
        return F == null ? 0 : F.size();
    }

    public void a(long j, int i, JceStruct jceStruct) {
        mb.r("HIPList", "[ip_list]onIPListPush(), |pushId=" + j + "|seqNo=" + i);
        if (!Dw) {
            mb.s("HIPList", "[ip_list]onIPListPush(), not enable, use default");
        } else if (jceStruct == null) {
            mb.o("HIPList", "[ip_list]onIPListPush(), bad arg: jceStruct == null");
        } else if (jceStruct instanceof g) {
            g gVar = (g) jceStruct;
            a aVar = new a(System.currentTimeMillis() + (((long) gVar.q) * 1000), a(gVar.p, false), false);
            if (aVar.isValid()) {
                int w = nh.w(this.mContext);
                int i2 = gVar.s;
                if (i2 != w) {
                    mb.o("HIPList", "[ip_list]onIPListPush(), apn not match， just save, curApn: " + w + " pushedApn: " + i2);
                    this.Dz.b(bm(i2), aVar.DG, aVar.DH);
                } else {
                    String fI = fI();
                    this.Dz.b(fI, aVar.DG, aVar.DH);
                    a(fI, aVar, true);
                    mb.n("HIPList", "[ip_list]onIPListPush(), saved, key: " + fI);
                }
            } else {
                mb.s("HIPList", "[ip_list]onIPListPush(), not valid");
            }
        } else {
            mb.o("HIPList", "[ip_list]onIPListPush(), bad type, should be SCHIPList: " + jceStruct.getClass());
        }
    }

    public boolean ax() {
        return this.CX;
    }

    public void fF() {
        if (Dw) {
            mb.d("HIPList", "[ip_list]handleNetworkChange(), refreshWorkingHIPList, isTest: " + this.CX);
            fG();
        }
    }

    public String fJ() {
        String str = null;
        b B = B(false);
        if (B != null) {
            str = B.hd();
            if (str != null && (str.length() < "http://".length() || !str.substring(0, "http://".length()).equalsIgnoreCase("http://"))) {
                str = "http://" + str;
            }
            mb.n("HIPList", "[ip_list]getHttpIp(), httpIp: " + str);
        }
        if (str != null) {
            return str;
        }
        str = "http://" + Dx;
        mb.s("HIPList", "[ip_list]getHttpIp(), use default: " + str);
        return str;
    }
}
