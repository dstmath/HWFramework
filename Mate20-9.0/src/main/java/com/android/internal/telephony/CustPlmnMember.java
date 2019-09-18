package com.android.internal.telephony;

import android.common.HwCfgKey;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import huawei.cust.HwGetCfgFileConfig;
import java.util.regex.Pattern;

public class CustPlmnMember {
    private static final String LOG_TAG = "CustPlmnMember";
    private static final String NETMORK_TYPE = "network_type";
    private static final String NETWORK_MCCMNC = "network_mccmnc";
    private static final String PLMN = "plmn";
    private static final String REGEX = "(\\d:([^:,;]{5,14},){2}[^:,;]{1,20},[^:,;]{1,20}(,[^:,;]{1,5})?;)*(\\d:([^:,;]{5,14},){2}[^:,;]{1,20},[^:,;]{1,20}(,[^:,;]{1,5})?;?)$";
    private static final String RULE = "rule";
    private static final String SIM_MCCMNC = "sim_mccmnc";
    private static final String SPN = "spn";
    protected static final int SPN_RULE_SHOW_BOTH = 3;
    protected static final int SPN_RULE_SHOW_PLMN_ONLY = 2;
    protected static final int SPN_RULE_SHOW_PNN_PRIOR = 4;
    protected static final int SPN_RULE_SHOW_SPN_ONLY = 1;
    private static CustPlmnMember instance;
    public String plmn;
    public int rule;
    public boolean showPlmn;
    public boolean showSpn;
    public String spn;

    private CustPlmnMember() {
    }

    public static CustPlmnMember getInstance() {
        if (instance == null) {
            instance = new CustPlmnMember();
        }
        return instance;
    }

    private boolean isAvail(String str) {
        return str != null && !"".equals(str);
    }

    public boolean acquireFromCust(String hplmn, ServiceState currentState, String custSpn) {
        String str = hplmn;
        String str2 = custSpn;
        String regplmn = currentState.getOperatorNumeric();
        int netType = currentState.getVoiceNetworkType();
        TelephonyManager.getDefault();
        int netClass = TelephonyManager.getNetworkClass(netType);
        char c = 0;
        boolean z = true;
        boolean isAllAvail = isAvail(str2) && isAvail(hplmn) && isAvail(regplmn);
        if (!isAllAvail) {
            Rlog.d(LOG_TAG, "acquireFromCust() failed, custSpn or hplmm or regplmn is null or empty string");
            return false;
        } else if (!Pattern.matches(REGEX, str2)) {
            Rlog.d(LOG_TAG, "acquireFromCust() failed, custSpn does not match with regex");
            return false;
        } else {
            String[] rules = str2.split(";");
            int length = rules.length;
            boolean match = false;
            int i = 0;
            while (i < length) {
                String[] rule_plmns = rules[i].split(":");
                int rule_prop = Integer.parseInt(rule_plmns[c]);
                boolean custShowSpn = (rule_prop & true) == z ? z : false;
                boolean custShowPlmn = (rule_prop & 2) == 2;
                int netType2 = netType;
                String[] plmns = rule_plmns[1].split(",");
                boolean isAllAvail2 = isAllAvail;
                if (plmns[0].equals(str)) {
                    if (plmns[1].equals(regplmn)) {
                        if (4 == plmns.length || (5 == plmns.length && plmns[4].contains(String.valueOf(netClass + 1)))) {
                            this.showSpn = custShowSpn;
                            this.showPlmn = custShowPlmn;
                            this.rule = rule_prop;
                            this.plmn = plmns[2];
                            this.spn = plmns[3];
                            return true;
                        }
                        i++;
                        netType = netType2;
                        isAllAvail = isAllAvail2;
                        String str3 = custSpn;
                        c = 0;
                        z = true;
                    }
                }
                if (!plmns[0].equals(str) || !plmns[1].equals("00000")) {
                    if (plmns[0].equals("00000") && plmns[1].equals(regplmn)) {
                        this.rule = rule_prop;
                        this.showSpn = custShowSpn;
                        this.showPlmn = custShowPlmn;
                        this.plmn = plmns[2];
                        this.spn = plmns[3];
                        match = true;
                    }
                    i++;
                    netType = netType2;
                    isAllAvail = isAllAvail2;
                    String str32 = custSpn;
                    c = 0;
                    z = true;
                } else {
                    this.rule = rule_prop;
                    this.showSpn = custShowSpn;
                    this.showPlmn = custShowPlmn;
                    this.plmn = plmns[2];
                    this.spn = plmns[3];
                    match = true;
                    i++;
                    netType = netType2;
                    isAllAvail = isAllAvail2;
                    String str322 = custSpn;
                    c = 0;
                    z = true;
                }
            }
            boolean z2 = isAllAvail;
            if (match) {
                return true;
            }
            return false;
        }
    }

    public boolean judgeShowSpn(boolean showSpn2) {
        return this.rule == 0 ? showSpn2 : this.showSpn;
    }

    public String judgeSpn(String spn2) {
        return "####".equals(this.spn) ? spn2 : this.spn;
    }

    public String judgePlmn(String plmn2) {
        return "####".equals(this.plmn) ? plmn2 : this.plmn;
    }

    /* JADX WARNING: Removed duplicated region for block: B:53:0x00d7 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00d8 A[RETURN] */
    public boolean getCfgCustDisplayParams(String hplmn, ServiceState currentState, String custSpn, int slotid) {
        boolean z;
        boolean match = false;
        int cfgrule = 0;
        int netType = currentState.getVoiceNetworkType();
        TelephonyManager.getDefault();
        try {
            r7 = r7;
            z = true;
            try {
                HwCfgKey hwCfgKey = new HwCfgKey(custSpn, NETWORK_MCCMNC, SIM_MCCMNC, NETMORK_TYPE, PLMN, currentState.getOperatorNumeric(), hplmn, String.valueOf(TelephonyManager.getNetworkClass(netType)), slotid);
                HwCfgKey keyCollection = hwCfgKey;
                String custplmn = (String) HwGetCfgFileConfig.getCfgFileData(keyCollection, String.class);
                keyCollection.rkey = "rule";
                Integer custrule = (Integer) HwGetCfgFileConfig.getCfgFileData(keyCollection, Integer.class);
                keyCollection.rkey = "spn";
                String custspn = (String) HwGetCfgFileConfig.getCfgFileData(keyCollection, String.class);
                if (custrule != null) {
                    cfgrule = custrule.intValue();
                    this.rule = cfgrule;
                    match = true;
                    if (!(custspn == null || custplmn == null)) {
                        this.spn = custspn;
                        this.plmn = custplmn;
                    }
                }
                if (1 == cfgrule && custspn != null) {
                    this.showSpn = (cfgrule & 1) == 1;
                    this.showPlmn = (cfgrule & 2) == 2;
                    this.rule = cfgrule;
                    this.spn = custspn;
                    match = true;
                }
                if (2 == cfgrule && custplmn != null) {
                    this.showPlmn = (cfgrule & 2) == 2;
                    this.showSpn = (cfgrule & 1) == 1;
                    this.rule = cfgrule;
                    this.plmn = custplmn;
                    match = true;
                }
                if (!(3 != cfgrule || custplmn == null || custspn == null)) {
                    this.showPlmn = (cfgrule & 2) == 2;
                    this.showSpn = (cfgrule & 1) == 1;
                    this.rule = cfgrule;
                    this.spn = custspn;
                    this.plmn = custplmn;
                    match = true;
                }
            } catch (Exception e) {
                Rlog.d(LOG_TAG, "Exception: read net_sim_ue_pri error");
                if (match) {
                }
            }
        } catch (Exception e2) {
            z = true;
            Rlog.d(LOG_TAG, "Exception: read net_sim_ue_pri error");
            if (match) {
            }
        }
        if (match) {
            return z;
        }
        return false;
    }
}
