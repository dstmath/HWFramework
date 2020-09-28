package com.android.internal.telephony;

import android.common.HwCfgKey;
import android.telephony.ServiceState;
import android.text.TextUtils;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import huawei.cust.HwGetCfgFileConfig;
import java.util.regex.Pattern;

public class CustPlmnMember {
    private static final int INVALID_RULE_PROP = -1;
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
        int ruleProp;
        Object obj = hplmn;
        char c = 0;
        if (currentState == null) {
            return false;
        }
        String regplmn = currentState.getOperatorNumeric();
        int netType = ServiceStateEx.getVoiceNetworkType(currentState);
        int netClass = TelephonyManagerEx.getNetworkClass(netType);
        boolean z = true;
        boolean isAllAvail = isAvail(custSpn) && isAvail(hplmn) && isAvail(regplmn);
        if (!isAllAvail) {
            RlogEx.i(LOG_TAG, "acquireFromCust() failed, custSpn or hplmm or regplmn is null or empty string");
            return false;
        } else if (!Pattern.matches(REGEX, custSpn)) {
            RlogEx.i(LOG_TAG, "acquireFromCust() failed, custSpn does not match with regex");
            return false;
        } else {
            String[] rules = custSpn.split(";");
            int length = rules.length;
            boolean match = false;
            int i = 0;
            while (i < length) {
                String[] rulePlmns = rules[i].split(":");
                try {
                    ruleProp = Integer.parseInt(rulePlmns[c]);
                } catch (NumberFormatException e) {
                    RlogEx.e(LOG_TAG, "acquireFromCust() NumberFormatException");
                    ruleProp = -1;
                }
                boolean custShowSpn = (ruleProp & 1) == z ? z : false;
                boolean custShowPlmn = (ruleProp & 2) == 2;
                String[] plmns = rulePlmns[1].split(",");
                if (plmns[0].equals(obj)) {
                    if (plmns[1].equals(regplmn)) {
                        if (4 == plmns.length || (5 == plmns.length && plmns[4].contains(String.valueOf(netClass + 1)))) {
                            this.showSpn = custShowSpn;
                            this.showPlmn = custShowPlmn;
                            this.rule = ruleProp;
                            this.plmn = plmns[2];
                            this.spn = plmns[3];
                            return true;
                        }
                        i++;
                        obj = hplmn;
                        netType = netType;
                        isAllAvail = isAllAvail;
                        c = 0;
                        z = true;
                    }
                }
                if (!plmns[0].equals(obj) || !plmns[1].equals("00000")) {
                    if (plmns[0].equals("00000") && plmns[1].equals(regplmn)) {
                        this.rule = ruleProp;
                        this.showSpn = custShowSpn;
                        this.showPlmn = custShowPlmn;
                        this.plmn = plmns[2];
                        this.spn = plmns[3];
                        match = true;
                    }
                    i++;
                    obj = hplmn;
                    netType = netType;
                    isAllAvail = isAllAvail;
                    c = 0;
                    z = true;
                } else {
                    this.rule = ruleProp;
                    this.showSpn = custShowSpn;
                    this.showPlmn = custShowPlmn;
                    this.plmn = plmns[2];
                    this.spn = plmns[3];
                    match = true;
                    i++;
                    obj = hplmn;
                    netType = netType;
                    isAllAvail = isAllAvail;
                    c = 0;
                    z = true;
                }
            }
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

    public boolean getCfgCustDisplayParams(String hplmn, ServiceState currentState, String custSpn, int slotid) {
        if (currentState == null) {
            return false;
        }
        boolean match = false;
        int cfgrule = 0;
        int netClass = TelephonyManagerEx.getNetworkClass(ServiceStateEx.getVoiceNetworkType(currentState));
        String regplmn = currentState.getOperatorNumeric();
        if (TextUtils.isEmpty(regplmn)) {
            return false;
        }
        HwCfgKey keyCollection = new HwCfgKey(custSpn, NETWORK_MCCMNC, SIM_MCCMNC, NETMORK_TYPE, PLMN, regplmn, hplmn, String.valueOf(netClass), slotid);
        String custplmn = (String) HwGetCfgFileConfig.getCfgFileData(keyCollection, String.class);
        keyCollection.rkey = RULE;
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
        if (match) {
            return true;
        }
        return false;
    }
}
