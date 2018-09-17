package com.huawei.android.pushagent.model.a;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.utils.a.e;
import com.huawei.android.pushagent.utils.d.c;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map.Entry;

public class g extends c {
    private static g s = null;

    private g(Context context) {
        super(context, "PushRouteInfo");
        y();
    }

    public static synchronized g aq(Context context) {
        synchronized (g.class) {
            g gVar;
            if (s != null) {
                gVar = s;
                return gVar;
            }
            s = new g(context);
            gVar = s;
            return gVar;
        }
    }

    public long bq() {
        return getLong("pushSrvValidTime", Long.MAX_VALUE);
    }

    public boolean bl(long j) {
        return setValue("pushSrvValidTime", Long.valueOf(j));
    }

    public int getResult() {
        return getInt("result", -1);
    }

    public boolean bm(int i) {
        return setValue("result", Integer.valueOf(i));
    }

    public String getBelongId() {
        return getString("belongId", "-1");
    }

    public int ba() {
        return getInt("isChkToken", 1);
    }

    public int bv() {
        return getInt("isChkNcToken", 1);
    }

    public String getAnalyticUrl() {
        return getString("analyticUrl", null);
    }

    public String bh() {
        return getString("passTrustPkgs", "");
    }

    public String bi() {
        return getString("noticeTrustPkgs", "");
    }

    public long bp() {
        return getLong("upAnalyticUrlInterval", 345600000);
    }

    public String getConnId() {
        return e.nu(getString("connId", ""));
    }

    public int cx() {
        return getInt("pushConnectLog", 0);
    }

    public int cy() {
        return getInt("heartbeatFailLog", 0);
    }

    public int cz() {
        return getInt("tokenReqLog", 0);
    }

    public int cv() {
        return getInt("onlineStatusLog", 0);
    }

    public int cw() {
        return getInt("msgLog", 0);
    }

    public int da() {
        return getInt("channelCloseLog", 0);
    }

    public long bb() {
        return getLong("minReportItval", 1800000);
    }

    public long bc() {
        return getLong("fatalMinReportItval", 1200000);
    }

    public int bd() {
        return getInt("reportMaxCount", 200);
    }

    public int be() {
        return getInt("reportUpperCount", 100);
    }

    public int bf() {
        return getInt("reportMaxByteCount", 2500);
    }

    public String getServerIP() {
        return getString("serverIp", "");
    }

    public int getServerPort() {
        return getInt("serverPort", -1);
    }

    public long br() {
        return getLong("trsValid_min", 7200);
    }

    public long bo() {
        return getLong("trsValid_max", 2592000);
    }

    public int bk() {
        return getInt("bastetInterval", 3);
    }

    public long getWifiMinHeartbeat() {
        return getLong("wifiMinHeartbeat", 1800);
    }

    public long getWifiMaxHeartbeat() {
        return getLong("wifiMaxHeartbeat", 1800);
    }

    public long get3GMinHeartbeat() {
        return getLong("g3MinHeartbeat", 900);
    }

    public long get3GMaxHeartbeat() {
        return getLong("g3MaxHeartbeat", 1800);
    }

    public long bw() {
        return getLong("serverRec1_min", 3);
    }

    public long bx() {
        return getLong("serverRec2_min", 10);
    }

    public long by() {
        return getLong("serverRec3_min", 30);
    }

    public long bz() {
        return getLong("serverRec4_min", 300);
    }

    public long ca() {
        return getLong("serverRec5_min", 300);
    }

    public long cb() {
        return getLong("serverRec6_min", 600);
    }

    public long cc() {
        return getLong("serverRec7_min", 900);
    }

    public long cd() {
        return getLong("serverRec8_min", 1800);
    }

    public long au() {
        return getLong("noNetHeartbeat", 7200);
    }

    public long bs() {
        return getLong("connTrsItval", 300);
    }

    public long bu() {
        return getLong("connTrsErrItval", 1800);
    }

    public long ce() {
        return getLong("SrvMaxFail_times", 6);
    }

    public long bt() {
        return getLong("maxQTRS_times", 6);
    }

    public long as() {
        return getLong("socketConnTimeOut", 30);
    }

    public long at() {
        return getLong("socketConnectReadOut", 10);
    }

    public long dd() {
        return getLong("pushLeastRun_time", 30);
    }

    public long de() {
        return getLong("push1StartInt", 3);
    }

    public long df() {
        return getLong("push2StartInt", 30);
    }

    public long dg() {
        return getLong("push3StartInt", 600);
    }

    public long dh() {
        return getLong("push4StartInt", 1800);
    }

    public long cn() {
        return getLong("firstQueryTRSDayTimes", 6);
    }

    public long co() {
        return getLong("firstQueryTRSHourTimes", 2);
    }

    public long cp() {
        return getLong("maxQueryTRSDayTimes", 1);
    }

    public HashMap<Long, Long> cq() {
        return dk("flowcInterval", "flowcVlomes");
    }

    public long cr() {
        return getLong("wifiFirstQueryTRSDayTimes", 18);
    }

    public long cs() {
        return getLong("wifiFirstQueryTRSHourTimes", 6);
    }

    public long ct() {
        return getLong("wifiMaxQueryTRSDayTimes", 3);
    }

    public long ay() {
        return getLong("stopServiceItval", 5);
    }

    public long aw() {
        return getLong("heartBeatRspTimeOut", 10) * 1000;
    }

    public HashMap<Long, Long> cu() {
        return dk("wifiFlowcInterval", "wifiFlowcVlomes");
    }

    public long cg() {
        return getLong("ConnRange", 600) * 1000;
    }

    public long ch() {
        return getLong("ConnRangeIdle", 1800) * 1000;
    }

    public int cf() {
        return getInt("MaxConnTimes", 4);
    }

    public boolean ar() {
        return getInt("allowPry", 0) == 1;
    }

    private HashMap<Long, Long> dk(String str, String str2) {
        String str3 = "\\d{1,3}";
        HashMap<Long, Long> hashMap = new HashMap();
        for (String str4 : v().keySet()) {
            if (str4.matches(str + str3)) {
                hashMap.put(Long.valueOf(getLong(str4, 1)), Long.valueOf(getLong(str4.replace(str, str2), 2147483647L)));
            }
        }
        return hashMap;
    }

    public HashMap<String, String> ax() {
        HashMap<String, String> hashMap = new HashMap();
        String str = "apn_";
        for (Entry entry : v().entrySet()) {
            String str2 = (String) entry.getKey();
            if (str2.startsWith(str)) {
                hashMap.put(str2, (String) entry.getValue());
            }
        }
        return hashMap;
    }

    public int db() {
        return getInt("grpNum", 0);
    }

    public String dc() {
        String str = "CE6935516BA17DB6174D77DAB902ED0F75D8C9B071FD46981BB1D05AA95F14277122B362304D6B3B865D1C00F5D8C6FF8BC2D432B8CDB11CF95B2450B7ADA9E20957068AD84E1BD4666E30BB103C5BCE485643755E7921AE0430A87C71DEB42F764779D4118F9A4183ABB2CBA6C31913AE6141DE168C51A270BADC91518DCE317F3309B50CCFB4B1949DC41520CBB3354C0CA3FC6943FE75DADA3B2A89397A3D68D6DC6AEBA0B6178AC0089FFEF6D2CF6DD36327C5AAB4ECE3A59B7D6B4E250D05746A19E8F052A90AB4A7F41958013E66EB207798DB766342701D0E8F6D5141B910887F7D43EE58A63AC9AF4D7B4A2B27B67C42DBD5142501DB629C3208E760B20BE1775C387F823733E9D5407F291B10C1846F77B7452EEF25B4720A103B90DD19B1B12CD7D0D0A1F7EEAAD0210E2C21494299D1E1E8FC83C088886E03BB1CDFD8D3B0AF28023D0F9E1AB8ACF0D4B5900EC2B5E3BCAE23020B581271136A56FB404CAAECE005D78DBB71ADE08ED965F9304F4F2CB13C6B3242CB04D28A05ED5D75669BEDF0F788AA3D8C1B3FFFEF3D2C0A2700E6E266E33D6ABFD6B7377D65FB60CB1C7288CE12CD584C357E84C446";
        str = getString("publicKey", "CE6935516BA17DB6174D77DAB902ED0F75D8C9B071FD46981BB1D05AA95F14277122B362304D6B3B865D1C00F5D8C6FF8BC2D432B8CDB11CF95B2450B7ADA9E20957068AD84E1BD4666E30BB103C5BCE485643755E7921AE0430A87C71DEB42F764779D4118F9A4183ABB2CBA6C31913AE6141DE168C51A270BADC91518DCE317F3309B50CCFB4B1949DC41520CBB3354C0CA3FC6943FE75DADA3B2A89397A3D68D6DC6AEBA0B6178AC0089FFEF6D2CF6DD36327C5AAB4ECE3A59B7D6B4E250D05746A19E8F052A90AB4A7F41958013E66EB207798DB766342701D0E8F6D5141B910887F7D43EE58A63AC9AF4D7B4A2B27B67C42DBD5142501DB629C3208E760B20BE1775C387F823733E9D5407F291B10C1846F77B7452EEF25B4720A103B90DD19B1B12CD7D0D0A1F7EEAAD0210E2C21494299D1E1E8FC83C088886E03BB1CDFD8D3B0AF28023D0F9E1AB8ACF0D4B5900EC2B5E3BCAE23020B581271136A56FB404CAAECE005D78DBB71ADE08ED965F9304F4F2CB13C6B3242CB04D28A05ED5D75669BEDF0F788AA3D8C1B3FFFEF3D2C0A2700E6E266E33D6ABFD6B7377D65FB60CB1C7288CE12CD584C357E84C446");
        CharSequence nu = e.nu(str);
        if (!TextUtils.isEmpty(nu)) {
            return nu;
        }
        c.sh("PushLog2951", "public key is empty, use origin.");
        bl(0);
        return str;
    }

    public boolean isValid() {
        if ("".equals(getServerIP()) || -1 == getServerPort() || getResult() != 0) {
            return false;
        }
        return true;
    }

    public boolean isNotAllowedPush() {
        int result = getResult();
        if (25 == result || 26 == result || 27 == result) {
            return true;
        }
        return false;
    }

    public long dl() {
        return getLong("fir3gHb", 300000);
    }

    public long dm() {
        return getLong("firWifiHb", 170000);
    }

    public long cj() {
        return getLong("ReConnInterval", 300) * 1000;
    }

    public long ck() {
        return getLong("ReConnIntervalIdle", 600) * 1000;
    }

    public long ci() {
        return getLong("KeepConnTime", 300) * 1000;
    }

    public static boolean dj(String str) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        try {
            simpleDateFormat.setLenient(false);
            simpleDateFormat.parse(str);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public String cl() {
        String string = getString("idlePeriodBegin", "00:00");
        if (string.length() == "HH:mm".length() && (dj(string) ^ 1) == 0) {
            return string;
        }
        return "00:00";
    }

    public String cm() {
        String string = getString("idlePeriodEnd", "06:00");
        if (string.length() == "HH:mm".length() && (dj(string) ^ 1) == 0) {
            return string;
        }
        return "06:00";
    }

    public long av() {
        return getLong("hbvalid", 1296000) * 1000;
    }

    public boolean bj() {
        if (getInt("allowBastet", 1) == 1) {
            return true;
        }
        return false;
    }

    public boolean bg() {
        if (getInt("needCheckAgreement", 1) == 1) {
            return true;
        }
        return false;
    }

    public boolean di() {
        if (getInt("needSolinger", 1) == 1) {
            return true;
        }
        return false;
    }

    public long az() {
        return getLong("msgResponseTimeOut", 3600) * 1000;
    }

    public long dn() {
        return getLong("resetBastetTimeOut", 30) * 1000;
    }

    public long do() {
        return getLong("responseMsgTimeout", 60) * 1000;
    }

    public long getNextConnectTrsInterval() {
        return getLong("nextConnectInterval", 86400) * 1000;
    }

    public boolean bn(long j) {
        return setValue("nextConnectInterval", Long.valueOf(j));
    }
}
