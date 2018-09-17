package com.android.internal.telephony;

import java.util.ArrayList;
import java.util.List;

public class HwIccIdUtil {
    private static final List<String> CMCC_ICCID_ARRAY = new ArrayList();
    private static final List<String> CMCC_MCCMNC_ARRAY = new ArrayList();
    private static final List<String> CT_ICCID_ARRAY = new ArrayList();
    private static final List<String> CU_ICCID_ARRAY = new ArrayList();
    private static final List<String> CU_MCCMNC_ARRAY = new ArrayList();
    public static final int ICCID_LEN_MINIMUM = 7;
    public static final int ICCID_LEN_SIX = 6;
    public static final int MCCMNC_LEN_MINIMUM = 5;
    private static final String PREFIX_LOCAL_ICCID = "8986";
    private static final String PREFIX_LOCAL_MCC = "460";

    static {
        CMCC_ICCID_ARRAY.clear();
        CMCC_ICCID_ARRAY.add("898600");
        CMCC_ICCID_ARRAY.add("898602");
        CMCC_ICCID_ARRAY.add("898607");
        CMCC_ICCID_ARRAY.add("898212");
        CU_ICCID_ARRAY.clear();
        CU_ICCID_ARRAY.add("898601");
        CU_ICCID_ARRAY.add("898609");
        CT_ICCID_ARRAY.clear();
        CT_ICCID_ARRAY.add("898603");
        CT_ICCID_ARRAY.add("898611");
        CT_ICCID_ARRAY.add("898606");
        CT_ICCID_ARRAY.add("8985302");
        CT_ICCID_ARRAY.add("8985307");
        CMCC_MCCMNC_ARRAY.clear();
        CMCC_MCCMNC_ARRAY.add("46000");
        CMCC_MCCMNC_ARRAY.add("46002");
        CMCC_MCCMNC_ARRAY.add("46007");
        CMCC_MCCMNC_ARRAY.add("46008");
        CU_MCCMNC_ARRAY.clear();
        CU_MCCMNC_ARRAY.add("46001");
        CU_MCCMNC_ARRAY.add("46006");
        CU_MCCMNC_ARRAY.add("46009");
    }

    public static boolean isCMCC(String inn) {
        Object inn2;
        if (inn2 != null && inn2.length() >= 7) {
            inn2 = inn2.substring(0, 6);
        }
        return CMCC_ICCID_ARRAY.contains(inn2);
    }

    public static boolean isCT(String inn) {
        Object inn2;
        if (inn2 != null && inn2.startsWith(PREFIX_LOCAL_ICCID) && inn2.length() >= 7) {
            inn2 = inn2.substring(0, 6);
        }
        return CT_ICCID_ARRAY.contains(inn2);
    }

    public static boolean isCU(String inn) {
        Object inn2;
        if (inn2 != null && inn2.length() >= 7) {
            inn2 = inn2.substring(0, 6);
        }
        return CU_ICCID_ARRAY.contains(inn2);
    }

    public static boolean isCMCCByMccMnc(String mccMnc) {
        Object mccMnc2;
        if (mccMnc2 != null && mccMnc2.length() > 5) {
            mccMnc2 = mccMnc2.substring(0, 5);
        }
        return CMCC_MCCMNC_ARRAY.contains(mccMnc2);
    }

    public static boolean isCUByMccMnc(String mccMnc) {
        Object mccMnc2;
        if (mccMnc2 != null && mccMnc2.length() > 5) {
            mccMnc2 = mccMnc2.substring(0, 5);
        }
        return CU_MCCMNC_ARRAY.contains(mccMnc2);
    }
}
