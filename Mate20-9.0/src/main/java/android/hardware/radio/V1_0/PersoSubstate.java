package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class PersoSubstate {
    public static final int IN_PROGRESS = 1;
    public static final int READY = 2;
    public static final int RUIM_CORPORATE = 16;
    public static final int RUIM_CORPORATE_PUK = 22;
    public static final int RUIM_HRPD = 15;
    public static final int RUIM_HRPD_PUK = 21;
    public static final int RUIM_NETWORK1 = 13;
    public static final int RUIM_NETWORK1_PUK = 19;
    public static final int RUIM_NETWORK2 = 14;
    public static final int RUIM_NETWORK2_PUK = 20;
    public static final int RUIM_RUIM = 18;
    public static final int RUIM_RUIM_PUK = 24;
    public static final int RUIM_SERVICE_PROVIDER = 17;
    public static final int RUIM_SERVICE_PROVIDER_PUK = 23;
    public static final int SIM_CORPORATE = 5;
    public static final int SIM_CORPORATE_PUK = 10;
    public static final int SIM_NETWORK = 3;
    public static final int SIM_NETWORK_PUK = 8;
    public static final int SIM_NETWORK_SUBSET = 4;
    public static final int SIM_NETWORK_SUBSET_PUK = 9;
    public static final int SIM_SERVICE_PROVIDER = 6;
    public static final int SIM_SERVICE_PROVIDER_PUK = 11;
    public static final int SIM_SIM = 7;
    public static final int SIM_SIM_PUK = 12;
    public static final int UNKNOWN = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "UNKNOWN";
        }
        if (o == 1) {
            return "IN_PROGRESS";
        }
        if (o == 2) {
            return "READY";
        }
        if (o == 3) {
            return "SIM_NETWORK";
        }
        if (o == 4) {
            return "SIM_NETWORK_SUBSET";
        }
        if (o == 5) {
            return "SIM_CORPORATE";
        }
        if (o == 6) {
            return "SIM_SERVICE_PROVIDER";
        }
        if (o == 7) {
            return "SIM_SIM";
        }
        if (o == 8) {
            return "SIM_NETWORK_PUK";
        }
        if (o == 9) {
            return "SIM_NETWORK_SUBSET_PUK";
        }
        if (o == 10) {
            return "SIM_CORPORATE_PUK";
        }
        if (o == 11) {
            return "SIM_SERVICE_PROVIDER_PUK";
        }
        if (o == 12) {
            return "SIM_SIM_PUK";
        }
        if (o == 13) {
            return "RUIM_NETWORK1";
        }
        if (o == 14) {
            return "RUIM_NETWORK2";
        }
        if (o == 15) {
            return "RUIM_HRPD";
        }
        if (o == 16) {
            return "RUIM_CORPORATE";
        }
        if (o == 17) {
            return "RUIM_SERVICE_PROVIDER";
        }
        if (o == 18) {
            return "RUIM_RUIM";
        }
        if (o == 19) {
            return "RUIM_NETWORK1_PUK";
        }
        if (o == 20) {
            return "RUIM_NETWORK2_PUK";
        }
        if (o == 21) {
            return "RUIM_HRPD_PUK";
        }
        if (o == 22) {
            return "RUIM_CORPORATE_PUK";
        }
        if (o == 23) {
            return "RUIM_SERVICE_PROVIDER_PUK";
        }
        if (o == 24) {
            return "RUIM_RUIM_PUK";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("UNKNOWN");
        if ((o & 1) == 1) {
            list.add("IN_PROGRESS");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("READY");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("SIM_NETWORK");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("SIM_NETWORK_SUBSET");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("SIM_CORPORATE");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("SIM_SERVICE_PROVIDER");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("SIM_SIM");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("SIM_NETWORK_PUK");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("SIM_NETWORK_SUBSET_PUK");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("SIM_CORPORATE_PUK");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("SIM_SERVICE_PROVIDER_PUK");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("SIM_SIM_PUK");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("RUIM_NETWORK1");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("RUIM_NETWORK2");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("RUIM_HRPD");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("RUIM_CORPORATE");
            flipped |= 16;
        }
        if ((o & 17) == 17) {
            list.add("RUIM_SERVICE_PROVIDER");
            flipped |= 17;
        }
        if ((o & 18) == 18) {
            list.add("RUIM_RUIM");
            flipped |= 18;
        }
        if ((o & 19) == 19) {
            list.add("RUIM_NETWORK1_PUK");
            flipped |= 19;
        }
        if ((o & 20) == 20) {
            list.add("RUIM_NETWORK2_PUK");
            flipped |= 20;
        }
        if ((o & 21) == 21) {
            list.add("RUIM_HRPD_PUK");
            flipped |= 21;
        }
        if ((o & 22) == 22) {
            list.add("RUIM_CORPORATE_PUK");
            flipped |= 22;
        }
        if ((o & 23) == 23) {
            list.add("RUIM_SERVICE_PROVIDER_PUK");
            flipped |= 23;
        }
        if ((o & 24) == 24) {
            list.add("RUIM_RUIM_PUK");
            flipped |= 24;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
