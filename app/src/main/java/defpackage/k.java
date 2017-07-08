package defpackage;

import android.content.Context;
import android.text.TextUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

/* renamed from: k */
public class k extends h {
    public k(Context context) {
        super(context, "PushRouteInfo");
    }

    public k(Context context, String str) {
        this(context);
        this.t = h.b(str);
    }

    private HashMap a(String str, String str2) {
        String str3 = "\\d{1,3}";
        HashMap hashMap = new HashMap();
        for (String str4 : this.t.keySet()) {
            if (str4.matches(str + str3)) {
                hashMap.put(Long.valueOf(getLong(str4, 1)), Long.valueOf(getLong(str4.replace(str, str2), 2147483647L)));
            }
        }
        return hashMap;
    }

    public long A() {
        return getLong("serverRec4_min", 1800);
    }

    public long B() {
        return getLong("serverRec5_min", 1800);
    }

    public long C() {
        return getLong("noNetHeartbeat", 7200);
    }

    public long D() {
        return getLong("connTrsItval", 300);
    }

    public long E() {
        return getLong("connTrsErrItval", 1800);
    }

    public long F() {
        return getLong("SrvMaxFail_times", 6);
    }

    public long G() {
        return getLong("maxQTRS_times", 6);
    }

    public long H() {
        return getLong("socketConnTimeOut", 30);
    }

    public long I() {
        return getLong("socketConnectReadOut", 20);
    }

    public long J() {
        return getLong("pushLeastRun_time", 30);
    }

    public long K() {
        return getLong("push1StartInt", 3);
    }

    public long L() {
        return getLong("push2StartInt", 30);
    }

    public long M() {
        return getLong("push3StartInt", 600);
    }

    public long N() {
        return getLong("push4StartInt", 1800);
    }

    public long O() {
        return getLong("pollingInterval", 1800);
    }

    public String P() {
        return getString("pollingIp", "");
    }

    public int Q() {
        return getInt("pollingPort", -1);
    }

    public int R() {
        return getInt("pollingId", -1);
    }

    public long S() {
        return getLong("tokenRegTimeOut", 30);
    }

    public long T() {
        return getLong("firstQueryTRSDayTimes", 6);
    }

    public long U() {
        return getLong("firstQueryTRSHourTimes", 2);
    }

    public long V() {
        return getLong("maxQueryTRSDayTimes", 1);
    }

    public HashMap W() {
        return a("flowcInterval", "flowcVlomes");
    }

    public long X() {
        return getLong("wifiFirstQueryTRSDayTimes", 18);
    }

    public long Y() {
        return getLong("wifiFirstQueryTRSHourTimes", 6);
    }

    public long Z() {
        return getLong("wifiMaxQueryTRSDayTimes", 3);
    }

    public boolean a(k kVar) {
        aw.d("PushLog2828", "wifiMinHeartbeat=" + t() + ",wifiMaxHeartbeat=" + u() + ",3gMinHeartbeat=" + v() + ",3gMaxHeartbeat=" + w());
        return t() == kVar.t() && u() == kVar.u() && v() == kVar.v() && w() == kVar.w();
    }

    public long aa() {
        return getLong("cloneCheckItval", 600);
    }

    public long ab() {
        return getLong("updateFilesItval", 300);
    }

    public long ac() {
        return getLong("stopServiceItval", 5);
    }

    public long ad() {
        return getLong("heartBeatRspTimeOut", 10) * 1000;
    }

    public HashMap ae() {
        return a("wifiFlowcInterval", "wifiFlowcVlomes");
    }

    public long af() {
        return getLong("ConnRange", 600) * 1000;
    }

    public int ag() {
        return getInt("MaxConnTimes", 3);
    }

    public boolean ah() {
        return getInt("allowPry", 0) == 1;
    }

    public HashMap ai() {
        HashMap hashMap = new HashMap();
        String str = "apn_";
        for (Entry entry : this.t.entrySet()) {
            String str2 = (String) entry.getKey();
            if (str2.startsWith(str)) {
                hashMap.put(str2, (String) entry.getValue());
            }
        }
        return hashMap;
    }

    public int aj() {
        return getInt("grpNum", 0);
    }

    public String ak() {
        String str = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDiCa5gkvCb+/dHAcgN1WMm0ItA\rY1njDoy6bPCE+oHZI439lmjP14PH7n2xtKsuybPbzPAGwuXq4doRz+wB8JiOUjNQ\rVI88zNzDDhdV3pxQlFgk61VojWtVBH2H45qMPMbMs4HdVs0Qcida2IhXOi6eAyRK\rp3PApI7e/ta1FHYKiwIDAQAB";
        return getString("publicKey", "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDiCa5gkvCb+/dHAcgN1WMm0ItA\rY1njDoy6bPCE+oHZI439lmjP14PH7n2xtKsuybPbzPAGwuXq4doRz+wB8JiOUjNQ\rVI88zNzDDhdV3pxQlFgk61VojWtVBH2H45qMPMbMs4HdVs0Qcida2IhXOi6eAyRK\rp3PApI7e/ta1FHYKiwIDAQAB");
    }

    public boolean al() {
        return ("".equals(p()) || -1 == q() || getResult() != 0) ? false : true;
    }

    public boolean am() {
        return ("".equals(P()) || -1 == Q() || getResult() != 0) ? false : true;
    }

    public long an() {
        return getLong("fir3gHb", 300000);
    }

    public long ao() {
        return getLong("firWifiHb", 170000);
    }

    public long ap() {
        return getLong("ReConnInterval", 300) * 1000;
    }

    public long aq() {
        return getLong("KeepConnTime", 300) * 1000;
    }

    public long ar() {
        return getLong("hbvalid", 1296000) * 1000;
    }

    public boolean as() {
        return getInt("allowBastet", 1) == 1;
    }

    public boolean at() {
        return getInt("needSolinger", 1) == 1;
    }

    public long au() {
        return getLong("msgResponseTimeOut", 3600) * 1000;
    }

    public long av() {
        return getLong("resetBastetTimeOut", 300) * 1000;
    }

    public long aw() {
        return getLong("responseMsgTimeout", 60) * 1000;
    }

    public void b(long j) {
        a("wifiMinHeartbeat", Long.valueOf(j));
    }

    public void c(long j) {
        a("wifiMaxHeartbeat", Long.valueOf(j));
    }

    public void d(long j) {
        a("g3MinHeartbeat", Long.valueOf(j));
    }

    public void e(long j) {
        a("g3MaxHeartbeat", Long.valueOf(j));
    }

    protected boolean f(String str) {
        String o = o();
        aw.d("PushLog2828", "old belongId = " + o + ", current belongId = " + str);
        return o.equals(str);
    }

    public boolean g(String str) {
        boolean z = false;
        Object string = getString("whiteList", "");
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(string)) {
            aw.d("PushLog2828", str + " is not in whiteList");
        } else {
            try {
                z = Arrays.asList(string.split("\\|")).contains(str);
            } catch (Exception e) {
                aw.e("PushLog2828", e.toString());
            }
            aw.d("PushLog2828", " is whiteList:" + z);
        }
        return z;
    }

    public /* bridge */ /* synthetic */ int getInt(String str, int i) {
        return super.getInt(str, i);
    }

    public /* bridge */ /* synthetic */ long getLong(String str, long j) {
        return super.getLong(str, j);
    }

    public int getResult() {
        return getInt("result", -1);
    }

    public /* bridge */ /* synthetic */ String getString(String str, String str2) {
        return super.getString(str, str2);
    }

    public /* bridge */ /* synthetic */ HashMap h() {
        return super.h();
    }

    public /* bridge */ /* synthetic */ boolean i() {
        return super.i();
    }

    public boolean isMultiSimEnabled() {
        return getInt("isMultiSimEnabled", 0) != 0;
    }

    public boolean isValid() {
        return al();
    }

    public String o() {
        return getString("belongId", "-1");
    }

    public String p() {
        return getString("serverIp", "");
    }

    public int q() {
        return getInt("serverPort", -1);
    }

    public long r() {
        return getLong("trsValid_min", 7200);
    }

    public long s() {
        return getLong("trsValid_max", 2592000);
    }

    public long t() {
        return getLong("wifiMinHeartbeat", 1800);
    }

    public /* bridge */ /* synthetic */ String toString() {
        return super.toString();
    }

    public long u() {
        return getLong("wifiMaxHeartbeat", 1800);
    }

    public long v() {
        return getLong("g3MinHeartbeat", 900);
    }

    public long w() {
        return getLong("g3MaxHeartbeat", 1800);
    }

    public long x() {
        return getLong("serverRec1_min", 1);
    }

    public long y() {
        return getLong("serverRec2_min", 30);
    }

    public long z() {
        return getLong("serverRec3_min", 300);
    }
}
