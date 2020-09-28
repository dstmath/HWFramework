package vendor.huawei.hardware.mtkradio.V1_0;

import java.util.ArrayList;

public final class VendorSetting {
    public static final int VENDOR_SETTING_BIP_OVERRIDE_APN = 6;
    public static final int VENDOR_SETTING_BIP_PDN_NAME_REUSE = 7;
    public static final int VENDOR_SETTING_BIP_PDN_REUSE = 5;
    public static final int VENDOR_SETTING_CXP_CONFIG_OPTR = 0;
    public static final int VENDOR_SETTING_CXP_CONFIG_SBP = 3;
    public static final int VENDOR_SETTING_CXP_CONFIG_SEG = 2;
    public static final int VENDOR_SETTING_CXP_CONFIG_SPEC = 1;
    public static final int VENDOR_SETTING_CXP_CONFIG_SUBID = 4;
    public static final int VENDOR_SETTING_DATA_SSC_MODE = 15;
    public static final int VENDOR_SETTING_RADIO_AIRPLANE_MODE = 8;
    public static final int VENDOR_SETTING_RADIO_SILENT_REBOOT = 10;
    public static final int VENDOR_SETTING_RADIO_SIM_MODE = 9;
    public static final int VENDOR_SETTING_RCS_UA_ENABLE = 14;
    public static final int VENDOR_SETTING_VILTE_ENABLE = 12;
    public static final int VENDOR_SETTING_VIWIFI_ENABLE = 13;
    public static final int VENDOR_SETTING_VOLTE_ENABLE = 11;
    public static final int VENDOR_SETTING_WFC_ENABLE = 16;

    public static final String toString(int o) {
        if (o == 0) {
            return "VENDOR_SETTING_CXP_CONFIG_OPTR";
        }
        if (o == 1) {
            return "VENDOR_SETTING_CXP_CONFIG_SPEC";
        }
        if (o == 2) {
            return "VENDOR_SETTING_CXP_CONFIG_SEG";
        }
        if (o == 3) {
            return "VENDOR_SETTING_CXP_CONFIG_SBP";
        }
        if (o == 4) {
            return "VENDOR_SETTING_CXP_CONFIG_SUBID";
        }
        if (o == 5) {
            return "VENDOR_SETTING_BIP_PDN_REUSE";
        }
        if (o == 6) {
            return "VENDOR_SETTING_BIP_OVERRIDE_APN";
        }
        if (o == 7) {
            return "VENDOR_SETTING_BIP_PDN_NAME_REUSE";
        }
        if (o == 8) {
            return "VENDOR_SETTING_RADIO_AIRPLANE_MODE";
        }
        if (o == 9) {
            return "VENDOR_SETTING_RADIO_SIM_MODE";
        }
        if (o == 10) {
            return "VENDOR_SETTING_RADIO_SILENT_REBOOT";
        }
        if (o == 11) {
            return "VENDOR_SETTING_VOLTE_ENABLE";
        }
        if (o == 12) {
            return "VENDOR_SETTING_VILTE_ENABLE";
        }
        if (o == 13) {
            return "VENDOR_SETTING_VIWIFI_ENABLE";
        }
        if (o == 14) {
            return "VENDOR_SETTING_RCS_UA_ENABLE";
        }
        if (o == 15) {
            return "VENDOR_SETTING_DATA_SSC_MODE";
        }
        if (o == 16) {
            return "VENDOR_SETTING_WFC_ENABLE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("VENDOR_SETTING_CXP_CONFIG_OPTR");
        if ((o & 1) == 1) {
            list.add("VENDOR_SETTING_CXP_CONFIG_SPEC");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("VENDOR_SETTING_CXP_CONFIG_SEG");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("VENDOR_SETTING_CXP_CONFIG_SBP");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("VENDOR_SETTING_CXP_CONFIG_SUBID");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("VENDOR_SETTING_BIP_PDN_REUSE");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("VENDOR_SETTING_BIP_OVERRIDE_APN");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("VENDOR_SETTING_BIP_PDN_NAME_REUSE");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("VENDOR_SETTING_RADIO_AIRPLANE_MODE");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("VENDOR_SETTING_RADIO_SIM_MODE");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("VENDOR_SETTING_RADIO_SILENT_REBOOT");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("VENDOR_SETTING_VOLTE_ENABLE");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("VENDOR_SETTING_VILTE_ENABLE");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("VENDOR_SETTING_VIWIFI_ENABLE");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("VENDOR_SETTING_RCS_UA_ENABLE");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("VENDOR_SETTING_DATA_SSC_MODE");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("VENDOR_SETTING_WFC_ENABLE");
            flipped |= 16;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
