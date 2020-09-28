package vendor.huawei.hardware.radio.V1_0;

import java.util.ArrayList;

public final class RILImsRadioTechType {
    public static final int IMS_RADIO_TECH_TYPE_1X_RTT = 6;
    public static final int IMS_RADIO_TECH_TYPE_EDGE = 2;
    public static final int IMS_RADIO_TECH_TYPE_EHRPD = 13;
    public static final int IMS_RADIO_TECH_TYPE_EVDO_0 = 7;
    public static final int IMS_RADIO_TECH_TYPE_EVDO_A = 8;
    public static final int IMS_RADIO_TECH_TYPE_EVDO_B = 12;
    public static final int IMS_RADIO_TECH_TYPE_GPRS = 1;
    public static final int IMS_RADIO_TECH_TYPE_GSM = 16;
    public static final int IMS_RADIO_TECH_TYPE_HSDPA = 9;
    public static final int IMS_RADIO_TECH_TYPE_HSPA = 11;
    public static final int IMS_RADIO_TECH_TYPE_HSPAP = 15;
    public static final int IMS_RADIO_TECH_TYPE_HSUPA = 10;
    public static final int IMS_RADIO_TECH_TYPE_IS95A = 4;
    public static final int IMS_RADIO_TECH_TYPE_IS95B = 5;
    public static final int IMS_RADIO_TECH_TYPE_IWLAN = 19;
    public static final int IMS_RADIO_TECH_TYPE_LTE = 14;
    public static final int IMS_RADIO_TECH_TYPE_TD_SCDMA = 17;
    public static final int IMS_RADIO_TECH_TYPE_UMTS = 3;
    public static final int IMS_RADIO_TECH_TYPE_UNKNOW = 0;
    public static final int IMS_RADIO_TECH_TYPE_WIFI = 18;
    public static final int IMS_RAIDO_THCH_TYPE_ANY = -1;

    public static final String toString(int o) {
        if (o == -1) {
            return "IMS_RAIDO_THCH_TYPE_ANY";
        }
        if (o == 0) {
            return "IMS_RADIO_TECH_TYPE_UNKNOW";
        }
        if (o == 1) {
            return "IMS_RADIO_TECH_TYPE_GPRS";
        }
        if (o == 2) {
            return "IMS_RADIO_TECH_TYPE_EDGE";
        }
        if (o == 3) {
            return "IMS_RADIO_TECH_TYPE_UMTS";
        }
        if (o == 4) {
            return "IMS_RADIO_TECH_TYPE_IS95A";
        }
        if (o == 5) {
            return "IMS_RADIO_TECH_TYPE_IS95B";
        }
        if (o == 6) {
            return "IMS_RADIO_TECH_TYPE_1X_RTT";
        }
        if (o == 7) {
            return "IMS_RADIO_TECH_TYPE_EVDO_0";
        }
        if (o == 8) {
            return "IMS_RADIO_TECH_TYPE_EVDO_A";
        }
        if (o == 9) {
            return "IMS_RADIO_TECH_TYPE_HSDPA";
        }
        if (o == 10) {
            return "IMS_RADIO_TECH_TYPE_HSUPA";
        }
        if (o == 11) {
            return "IMS_RADIO_TECH_TYPE_HSPA";
        }
        if (o == 12) {
            return "IMS_RADIO_TECH_TYPE_EVDO_B";
        }
        if (o == 13) {
            return "IMS_RADIO_TECH_TYPE_EHRPD";
        }
        if (o == 14) {
            return "IMS_RADIO_TECH_TYPE_LTE";
        }
        if (o == 15) {
            return "IMS_RADIO_TECH_TYPE_HSPAP";
        }
        if (o == 16) {
            return "IMS_RADIO_TECH_TYPE_GSM";
        }
        if (o == 17) {
            return "IMS_RADIO_TECH_TYPE_TD_SCDMA";
        }
        if (o == 18) {
            return "IMS_RADIO_TECH_TYPE_WIFI";
        }
        if (o == 19) {
            return "IMS_RADIO_TECH_TYPE_IWLAN";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & -1) == -1) {
            list.add("IMS_RAIDO_THCH_TYPE_ANY");
            flipped = 0 | -1;
        }
        list.add("IMS_RADIO_TECH_TYPE_UNKNOW");
        if ((o & 1) == 1) {
            list.add("IMS_RADIO_TECH_TYPE_GPRS");
            flipped |= 1;
        }
        if ((o & 2) == 2) {
            list.add("IMS_RADIO_TECH_TYPE_EDGE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("IMS_RADIO_TECH_TYPE_UMTS");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("IMS_RADIO_TECH_TYPE_IS95A");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("IMS_RADIO_TECH_TYPE_IS95B");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("IMS_RADIO_TECH_TYPE_1X_RTT");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("IMS_RADIO_TECH_TYPE_EVDO_0");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("IMS_RADIO_TECH_TYPE_EVDO_A");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("IMS_RADIO_TECH_TYPE_HSDPA");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("IMS_RADIO_TECH_TYPE_HSUPA");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("IMS_RADIO_TECH_TYPE_HSPA");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("IMS_RADIO_TECH_TYPE_EVDO_B");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("IMS_RADIO_TECH_TYPE_EHRPD");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("IMS_RADIO_TECH_TYPE_LTE");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("IMS_RADIO_TECH_TYPE_HSPAP");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("IMS_RADIO_TECH_TYPE_GSM");
            flipped |= 16;
        }
        if ((o & 17) == 17) {
            list.add("IMS_RADIO_TECH_TYPE_TD_SCDMA");
            flipped |= 17;
        }
        if ((o & 18) == 18) {
            list.add("IMS_RADIO_TECH_TYPE_WIFI");
            flipped |= 18;
        }
        if ((o & 19) == 19) {
            list.add("IMS_RADIO_TECH_TYPE_IWLAN");
            flipped |= 19;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
