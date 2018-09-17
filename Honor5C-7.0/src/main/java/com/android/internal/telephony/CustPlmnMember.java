package com.android.internal.telephony;

import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import java.util.regex.Pattern;

public class CustPlmnMember {
    private static final String LOG_TAG = "CustPlmnMember";
    private static final String REGEX = "(\\d:([^:,;]{5,14},){2}[^:,;]{1,20},[^:,;]{1,20}(,[^:,;]{1,5})?;)*(\\d:([^:,;]{5,14},){2}[^:,;]{1,20},[^:,;]{1,20}(,[^:,;]{1,5})?;?)$";
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
        return (str == null || "".equals(str)) ? false : true;
    }

    public boolean acquireFromCust(String hplmn, ServiceState currentState, String custSpn) {
        String regplmn = currentState.getOperatorNumeric();
        int netType = currentState.getVoiceNetworkType();
        TelephonyManager.getDefault();
        int netClass = TelephonyManager.getNetworkClass(netType);
        if (!isAvail(custSpn) || !isAvail(hplmn) || !isAvail(regplmn)) {
            Rlog.d(LOG_TAG, "acquireFromCust() failed, custSpn or hplmm or regplmn is null or empty string");
            return false;
        } else if (Pattern.matches(REGEX, custSpn)) {
            boolean match = false;
            for (String rule_item : custSpn.split(";")) {
                String[] rule_plmns = rule_item.split(":");
                int rule_prop = Integer.parseInt(rule_plmns[0]);
                boolean custShowSpn = (rule_prop & 1) == 1;
                boolean custShowPlmn = (rule_prop & 2) == 2;
                String[] plmns = rule_plmns[1].split(",");
                if (plmns[0].equals(hplmn) && plmns[1].equals(regplmn)) {
                    if (4 == plmns.length || (5 == plmns.length && plmns[4].contains(String.valueOf(netClass + 1)))) {
                        this.showSpn = custShowSpn;
                        this.showPlmn = custShowPlmn;
                        this.rule = rule_prop;
                        this.plmn = plmns[2];
                        this.spn = plmns[3];
                        return true;
                    }
                } else if (plmns[0].equals(hplmn) && plmns[1].equals("00000")) {
                    this.rule = rule_prop;
                    this.showSpn = custShowSpn;
                    this.showPlmn = custShowPlmn;
                    this.plmn = plmns[2];
                    this.spn = plmns[3];
                    match = true;
                } else if (plmns[0].equals("00000") && plmns[1].equals(regplmn)) {
                    this.rule = rule_prop;
                    this.showSpn = custShowSpn;
                    this.showPlmn = custShowPlmn;
                    this.plmn = plmns[2];
                    this.spn = plmns[3];
                    match = true;
                }
            }
            if (match) {
                return true;
            }
            return false;
        } else {
            Rlog.d(LOG_TAG, "acquireFromCust() failed, custSpn does not match with regex");
            return false;
        }
    }
}
