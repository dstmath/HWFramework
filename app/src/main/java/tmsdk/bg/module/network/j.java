package tmsdk.bg.module.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import tmsdk.common.tcc.TrafficSmsParser.MatchRule;
import tmsdk.common.utils.d;
import tmsdk.common.utils.l;
import tmsdkobf.bb;
import tmsdkobf.nc;

/* compiled from: Unknown */
final class j {
    nc nf;
    private final String zo;
    private final String zp;
    private final String zq;
    private final String zr;
    private final String zs;
    private final String zt;
    int zu;

    public j(int i) {
        this.zo = "MATCH_RULE0";
        this.zp = "#COLUMN0#";
        this.zq = "#ITEM0#";
        this.zr = "MATCH_RULE1";
        this.zs = "#COLUMN1#";
        this.zt = "#ITEM1#";
        this.zu = 0;
        this.nf = new nc("traffic_correction_setting");
        this.zu = i;
    }

    public void A(boolean z) {
        if (this.zu != 1) {
            this.nf.a("STOP_STATE0", z, false);
        } else {
            this.nf.a("STOP_STATE1", z, false);
        }
    }

    public void a(String str, int i, int i2, String str2, int i3) {
        if (!l.dm(str)) {
            if (this.zu != 1) {
                this.nf.a("PROFILE_IMSI0", str, false);
                this.nf.a("PROFILE_PROVINCE0", i, false);
                this.nf.a("PROFILE_CITY0", i2, false);
                this.nf.a("PROFILE_CARRY0", str2, false);
                this.nf.a("PROFILE_BRAND0", i3, false);
            } else {
                this.nf.a("PROFILE_IMSI1", str, false);
                this.nf.a("PROFILE_PROVINCE1", i, false);
                this.nf.a("PROFILE_CITY1", i2, false);
                this.nf.a("PROFILE_CARRY1", str2, false);
                this.nf.a("PROFILE_BRAND1", i3, false);
            }
        }
    }

    public void br(int i) {
        if (this.zu != 1) {
            this.nf.a("SIM_CARD_CLOSINGDAY0", i, false);
        } else {
            this.nf.a("SIM_CARD_CLOSINGDAY1", i, false);
        }
    }

    public void bs(int i) {
        if (this.zu != 1) {
            this.nf.a("SMS_CORRECT_TIMEOUT0", i, false);
        } else {
            this.nf.a("SMS_CORRECT_TIMEOUT1", i, false);
        }
    }

    public void bt(int i) {
        if (this.zu != 1) {
            this.nf.a("AUTO_CORRECTION_FREQUENCY0", i, false);
        } else {
            this.nf.a("AUTO_CORRECTION_FREQUENCY1", i, false);
        }
    }

    public List<MatchRule> bu(int i) {
        String str = "MATCH_RULE0";
        String str2 = "#COLUMN0#";
        String str3 = "#ITEM0#";
        if (this.zu == 1) {
            str = "MATCH_RULE1";
            str2 = "#COLUMN1#";
            str3 = "#ITEM1#";
        }
        List arrayList = new ArrayList();
        str = this.nf.getString(str + i, "");
        if (!(str == null || "".equals(str))) {
            for (String split : str.split(r0)) {
                String[] split2 = split.split(str2);
                if (split2 != null && split2.length == 4) {
                    arrayList.add(new MatchRule(Integer.valueOf(split2[0]).intValue(), Integer.valueOf(split2[1]).intValue(), split2[2], split2[3]));
                }
            }
        }
        return arrayList;
    }

    public ProfileInfo c(int i, String str) {
        ProfileInfo profileInfo = new ProfileInfo();
        String str2 = "";
        int i2;
        if (this.zu != 1) {
            str2 = this.nf.getString("PROFILE_IMSI0", "");
            if (str2.compareTo(str) == 0) {
                profileInfo.imsi = str2;
                profileInfo.province = this.nf.getInt("PROFILE_PROVINCE0", -1);
                profileInfo.city = this.nf.getInt("PROFILE_CITY0", -1);
                profileInfo.carry = this.nf.getString("PROFILE_CARRY0", "");
                i2 = this.nf.getInt("PROFILE_BRAND0", -1);
                profileInfo.brand = i2;
            }
        } else {
            str2 = this.nf.getString("PROFILE_IMSI1", "");
            if (str2.compareTo(str) == 0) {
                profileInfo.imsi = str2;
                profileInfo.province = this.nf.getInt("PROFILE_PROVINCE1", -1);
                profileInfo.city = this.nf.getInt("PROFILE_CITY1", -1);
                profileInfo.carry = this.nf.getString("PROFILE_CARRY1", "");
                i2 = this.nf.getInt("PROFILE_BRAND1", -1);
                profileInfo.brand = i2;
            }
        }
        return profileInfo;
    }

    public void cd(String str) {
        if (str != null) {
            if (this.zu != 1) {
                this.nf.a("SIM_CARD_PROVINCE0", str, false);
            } else {
                this.nf.a("SIM_CARD_PROVINCE1", str, false);
            }
        }
    }

    public void ce(String str) {
        if (str != null) {
            if (this.zu != 1) {
                this.nf.a("SIM_CARD_CITY0", str, false);
            } else {
                this.nf.a("SIM_CARD_CITY1", str, false);
            }
        }
    }

    public void cf(String str) {
        if (str != null) {
            if (this.zu != 1) {
                this.nf.a("SIM_CARD_CARRY0", str, false);
            } else {
                this.nf.a("SIM_CARD_CARRY1", str, false);
            }
        }
    }

    public void cg(String str) {
        if (str != null) {
            if (this.zu != 1) {
                this.nf.a("SIM_CARD_BRAND0", str, false);
            } else {
                this.nf.a("SIM_CARD_BRAND1", str, false);
            }
        }
    }

    public void ch(String str) {
        if (str != null) {
            if (this.zu != 1) {
                this.nf.a("SIM_CARD_SUCCESS_UPLOAD_INFO0", str, false);
            } else {
                this.nf.a("SIM_CARD_SUCCESS_UPLOAD_INFO1", str, false);
            }
        }
    }

    public void ci(String str) {
        if (str != null) {
            if (this.zu != 1) {
                this.nf.a("QUERY_CODE0", str, false);
            } else {
                this.nf.a("QUERY_CODE1", str, false);
            }
        }
    }

    public void cj(String str) {
        if (str != null) {
            if (this.zu != 1) {
                this.nf.a("QUERY_PORT0", str, false);
            } else {
                this.nf.a("QUERY_PORT1", str, false);
            }
        }
    }

    public void ck(String str) {
        if (str != null) {
            if (this.zu != 1) {
                this.nf.a("CORRECTION_TYPE0", str, false);
            } else {
                this.nf.a("CORRECTION_TYPE1", str, false);
            }
        }
    }

    public void cl(String str) {
        if (str != null) {
            if (this.zu != 1) {
                this.nf.a("LOCAL_AUTO_CORRECTION_TIME0", str, false);
            } else {
                this.nf.a("LOCAL_AUTO_CORRECTION_TIME1", str, false);
            }
        }
    }

    public void cm(String str) {
        if (str != null) {
            if (this.zu != 1) {
                this.nf.a("SERVER_AUTO_CORRECTION_TIME0", str, false);
            } else {
                this.nf.a("SERVER_AUTO_CORRECTION_TIME1", str, false);
            }
        }
    }

    public String dW() {
        return this.zu != 1 ? this.nf.getString("SIM_CARD_PROVINCE0", "") : this.nf.getString("SIM_CARD_PROVINCE1", "");
    }

    public String dX() {
        return this.zu != 1 ? this.nf.getString("SIM_CARD_CITY0", "") : this.nf.getString("SIM_CARD_CITY1", "");
    }

    public String dY() {
        return this.zu != 1 ? this.nf.getString("SIM_CARD_CARRY0", "") : this.nf.getString("SIM_CARD_CARRY1", "");
    }

    public String dZ() {
        return this.zu != 1 ? this.nf.getString("SIM_CARD_BRAND0", "") : this.nf.getString("SIM_CARD_BRAND1", "");
    }

    public int ea() {
        return this.zu != 1 ? this.nf.getInt("SIM_CARD_CLOSINGDAY0", 1) : this.nf.getInt("SIM_CARD_CLOSINGDAY1", 1);
    }

    public String eb() {
        return this.zu != 1 ? this.nf.getString("SIM_CARD_SUCCESS_UPLOAD_INFO0", "") : this.nf.getString("SIM_CARD_SUCCESS_UPLOAD_INFO1", "");
    }

    public int ec() {
        return this.zu != 1 ? this.nf.getInt("SMS_CORRECT_TIMEOUT0", 5) : this.nf.getInt("SMS_CORRECT_TIMEOUT1", 5);
    }

    public String ed() {
        return this.zu != 1 ? this.nf.getString("QUERY_CODE0", "") : this.nf.getString("QUERY_CODE1", "");
    }

    public String ee() {
        return this.zu != 1 ? this.nf.getString("QUERY_PORT0", "") : this.nf.getString("QUERY_PORT1", "");
    }

    public String ef() {
        return this.zu != 1 ? this.nf.getString("CORRECTION_TYPE0", "") : this.nf.getString("CORRECTION_TYPE1", "");
    }

    public String eg() {
        return this.zu != 1 ? this.nf.getString("LOCAL_AUTO_CORRECTION_TIME0", "") : this.nf.getString("LOCAL_AUTO_CORRECTION_TIME1", "");
    }

    public String eh() {
        return this.zu != 1 ? this.nf.getString("SERVER_AUTO_CORRECTION_TIME0", "") : this.nf.getString("SERVER_AUTO_CORRECTION_TIME1", "");
    }

    public int ei() {
        return this.zu != 1 ? this.nf.getInt("AUTO_CORRECTION_FREQUENCY0", 1) : this.nf.getInt("AUTO_CORRECTION_FREQUENCY1", 1);
    }

    public boolean ej() {
        return this.zu != 1 ? this.nf.getBoolean("STOP_STATE0", false) : this.nf.getBoolean("STOP_STATE1", false);
    }

    public void o(List<bb> list) {
        int i = 1;
        String str = "MATCH_RULE0";
        String str2 = "#COLUMN0#";
        String str3 = "#ITEM0#";
        if (this.zu == 1) {
            str = "MATCH_RULE1";
            str2 = "#COLUMN1#";
            str3 = "#ITEM1#";
        }
        String str4 = str2;
        String str5 = str;
        str = str3;
        Map hashMap = new HashMap();
        for (bb bbVar : list) {
            str2 = (String) hashMap.get(str5 + bbVar.type);
            str2 = str2 != null ? str2 + str + bbVar.unit + str4 + bbVar.type + str4 + bbVar.prefix + str4 + bbVar.postfix : bbVar.unit + str4 + bbVar.type + str4 + bbVar.prefix + str4 + bbVar.postfix;
            hashMap.put(str5 + bbVar.type, str2);
            StringBuilder append = new StringBuilder().append("insertMatchRule--[");
            int i2 = i + 1;
            d.e("TrafficCorrection", append.append(i).append("][").append(str2).append("]").toString());
            i = i2;
        }
        Set<Entry> entrySet = hashMap.entrySet();
        this.nf.beginTransaction();
        for (Entry entry : entrySet) {
            this.nf.a((String) entry.getKey(), (String) entry.getValue(), false);
        }
        this.nf.dj();
    }
}
