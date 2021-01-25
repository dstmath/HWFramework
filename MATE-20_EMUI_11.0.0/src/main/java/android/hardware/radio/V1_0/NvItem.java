package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class NvItem {
    public static final int CDMA_1X_ADVANCED_ENABLED = 57;
    public static final int CDMA_ACCOLC = 4;
    public static final int CDMA_BC10 = 52;
    public static final int CDMA_BC14 = 53;
    public static final int CDMA_EHRPD_ENABLED = 58;
    public static final int CDMA_EHRPD_FORCED = 59;
    public static final int CDMA_MDN = 3;
    public static final int CDMA_MEID = 1;
    public static final int CDMA_MIN = 2;
    public static final int CDMA_PRL_VERSION = 51;
    public static final int CDMA_SO68 = 54;
    public static final int CDMA_SO73_COP0 = 55;
    public static final int CDMA_SO73_COP1TO7 = 56;
    public static final int DEVICE_MSL = 11;
    public static final int LTE_BAND_ENABLE_25 = 71;
    public static final int LTE_BAND_ENABLE_26 = 72;
    public static final int LTE_BAND_ENABLE_41 = 73;
    public static final int LTE_HIDDEN_BAND_PRIORITY_25 = 77;
    public static final int LTE_HIDDEN_BAND_PRIORITY_26 = 78;
    public static final int LTE_HIDDEN_BAND_PRIORITY_41 = 79;
    public static final int LTE_SCAN_PRIORITY_25 = 74;
    public static final int LTE_SCAN_PRIORITY_26 = 75;
    public static final int LTE_SCAN_PRIORITY_41 = 76;
    public static final int MIP_PROFILE_AAA_AUTH = 33;
    public static final int MIP_PROFILE_AAA_SPI = 39;
    public static final int MIP_PROFILE_HA_AUTH = 34;
    public static final int MIP_PROFILE_HA_SPI = 38;
    public static final int MIP_PROFILE_HOME_ADDRESS = 32;
    public static final int MIP_PROFILE_MN_AAA_SS = 41;
    public static final int MIP_PROFILE_MN_HA_SS = 40;
    public static final int MIP_PROFILE_NAI = 31;
    public static final int MIP_PROFILE_PRI_HA_ADDR = 35;
    public static final int MIP_PROFILE_REV_TUN_PREF = 37;
    public static final int MIP_PROFILE_SEC_HA_ADDR = 36;
    public static final int OMADM_HFA_LEVEL = 18;
    public static final int RTN_ACTIVATION_DATE = 13;
    public static final int RTN_LIFE_CALLS = 15;
    public static final int RTN_LIFE_DATA_RX = 17;
    public static final int RTN_LIFE_DATA_TX = 16;
    public static final int RTN_LIFE_TIMER = 14;
    public static final int RTN_RECONDITIONED_STATUS = 12;

    public static final String toString(int o) {
        if (o == 1) {
            return "CDMA_MEID";
        }
        if (o == 2) {
            return "CDMA_MIN";
        }
        if (o == 3) {
            return "CDMA_MDN";
        }
        if (o == 4) {
            return "CDMA_ACCOLC";
        }
        if (o == 11) {
            return "DEVICE_MSL";
        }
        if (o == 12) {
            return "RTN_RECONDITIONED_STATUS";
        }
        if (o == 13) {
            return "RTN_ACTIVATION_DATE";
        }
        if (o == 14) {
            return "RTN_LIFE_TIMER";
        }
        if (o == 15) {
            return "RTN_LIFE_CALLS";
        }
        if (o == 16) {
            return "RTN_LIFE_DATA_TX";
        }
        if (o == 17) {
            return "RTN_LIFE_DATA_RX";
        }
        if (o == 18) {
            return "OMADM_HFA_LEVEL";
        }
        if (o == 31) {
            return "MIP_PROFILE_NAI";
        }
        if (o == 32) {
            return "MIP_PROFILE_HOME_ADDRESS";
        }
        if (o == 33) {
            return "MIP_PROFILE_AAA_AUTH";
        }
        if (o == 34) {
            return "MIP_PROFILE_HA_AUTH";
        }
        if (o == 35) {
            return "MIP_PROFILE_PRI_HA_ADDR";
        }
        if (o == 36) {
            return "MIP_PROFILE_SEC_HA_ADDR";
        }
        if (o == 37) {
            return "MIP_PROFILE_REV_TUN_PREF";
        }
        if (o == 38) {
            return "MIP_PROFILE_HA_SPI";
        }
        if (o == 39) {
            return "MIP_PROFILE_AAA_SPI";
        }
        if (o == 40) {
            return "MIP_PROFILE_MN_HA_SS";
        }
        if (o == 41) {
            return "MIP_PROFILE_MN_AAA_SS";
        }
        if (o == 51) {
            return "CDMA_PRL_VERSION";
        }
        if (o == 52) {
            return "CDMA_BC10";
        }
        if (o == 53) {
            return "CDMA_BC14";
        }
        if (o == 54) {
            return "CDMA_SO68";
        }
        if (o == 55) {
            return "CDMA_SO73_COP0";
        }
        if (o == 56) {
            return "CDMA_SO73_COP1TO7";
        }
        if (o == 57) {
            return "CDMA_1X_ADVANCED_ENABLED";
        }
        if (o == 58) {
            return "CDMA_EHRPD_ENABLED";
        }
        if (o == 59) {
            return "CDMA_EHRPD_FORCED";
        }
        if (o == 71) {
            return "LTE_BAND_ENABLE_25";
        }
        if (o == 72) {
            return "LTE_BAND_ENABLE_26";
        }
        if (o == 73) {
            return "LTE_BAND_ENABLE_41";
        }
        if (o == 74) {
            return "LTE_SCAN_PRIORITY_25";
        }
        if (o == 75) {
            return "LTE_SCAN_PRIORITY_26";
        }
        if (o == 76) {
            return "LTE_SCAN_PRIORITY_41";
        }
        if (o == 77) {
            return "LTE_HIDDEN_BAND_PRIORITY_25";
        }
        if (o == 78) {
            return "LTE_HIDDEN_BAND_PRIORITY_26";
        }
        if (o == 79) {
            return "LTE_HIDDEN_BAND_PRIORITY_41";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("CDMA_MEID");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("CDMA_MIN");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("CDMA_MDN");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("CDMA_ACCOLC");
            flipped |= 4;
        }
        if ((o & 11) == 11) {
            list.add("DEVICE_MSL");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("RTN_RECONDITIONED_STATUS");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("RTN_ACTIVATION_DATE");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("RTN_LIFE_TIMER");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("RTN_LIFE_CALLS");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("RTN_LIFE_DATA_TX");
            flipped |= 16;
        }
        if ((o & 17) == 17) {
            list.add("RTN_LIFE_DATA_RX");
            flipped |= 17;
        }
        if ((o & 18) == 18) {
            list.add("OMADM_HFA_LEVEL");
            flipped |= 18;
        }
        if ((o & 31) == 31) {
            list.add("MIP_PROFILE_NAI");
            flipped |= 31;
        }
        if ((o & 32) == 32) {
            list.add("MIP_PROFILE_HOME_ADDRESS");
            flipped |= 32;
        }
        if ((o & 33) == 33) {
            list.add("MIP_PROFILE_AAA_AUTH");
            flipped |= 33;
        }
        if ((o & 34) == 34) {
            list.add("MIP_PROFILE_HA_AUTH");
            flipped |= 34;
        }
        if ((o & 35) == 35) {
            list.add("MIP_PROFILE_PRI_HA_ADDR");
            flipped |= 35;
        }
        if ((o & 36) == 36) {
            list.add("MIP_PROFILE_SEC_HA_ADDR");
            flipped |= 36;
        }
        if ((o & 37) == 37) {
            list.add("MIP_PROFILE_REV_TUN_PREF");
            flipped |= 37;
        }
        if ((o & 38) == 38) {
            list.add("MIP_PROFILE_HA_SPI");
            flipped |= 38;
        }
        if ((o & 39) == 39) {
            list.add("MIP_PROFILE_AAA_SPI");
            flipped |= 39;
        }
        if ((o & 40) == 40) {
            list.add("MIP_PROFILE_MN_HA_SS");
            flipped |= 40;
        }
        if ((o & 41) == 41) {
            list.add("MIP_PROFILE_MN_AAA_SS");
            flipped |= 41;
        }
        if ((o & 51) == 51) {
            list.add("CDMA_PRL_VERSION");
            flipped |= 51;
        }
        if ((o & 52) == 52) {
            list.add("CDMA_BC10");
            flipped |= 52;
        }
        if ((o & 53) == 53) {
            list.add("CDMA_BC14");
            flipped |= 53;
        }
        if ((o & 54) == 54) {
            list.add("CDMA_SO68");
            flipped |= 54;
        }
        if ((o & 55) == 55) {
            list.add("CDMA_SO73_COP0");
            flipped |= 55;
        }
        if ((o & 56) == 56) {
            list.add("CDMA_SO73_COP1TO7");
            flipped |= 56;
        }
        if ((o & 57) == 57) {
            list.add("CDMA_1X_ADVANCED_ENABLED");
            flipped |= 57;
        }
        if ((o & 58) == 58) {
            list.add("CDMA_EHRPD_ENABLED");
            flipped |= 58;
        }
        if ((o & 59) == 59) {
            list.add("CDMA_EHRPD_FORCED");
            flipped |= 59;
        }
        if ((o & 71) == 71) {
            list.add("LTE_BAND_ENABLE_25");
            flipped |= 71;
        }
        if ((o & 72) == 72) {
            list.add("LTE_BAND_ENABLE_26");
            flipped |= 72;
        }
        if ((o & 73) == 73) {
            list.add("LTE_BAND_ENABLE_41");
            flipped |= 73;
        }
        if ((o & 74) == 74) {
            list.add("LTE_SCAN_PRIORITY_25");
            flipped |= 74;
        }
        if ((o & 75) == 75) {
            list.add("LTE_SCAN_PRIORITY_26");
            flipped |= 75;
        }
        if ((o & 76) == 76) {
            list.add("LTE_SCAN_PRIORITY_41");
            flipped |= 76;
        }
        if ((o & 77) == 77) {
            list.add("LTE_HIDDEN_BAND_PRIORITY_25");
            flipped |= 77;
        }
        if ((o & 78) == 78) {
            list.add("LTE_HIDDEN_BAND_PRIORITY_26");
            flipped |= 78;
        }
        if ((o & 79) == 79) {
            list.add("LTE_HIDDEN_BAND_PRIORITY_41");
            flipped |= 79;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
