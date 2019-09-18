package vendor.huawei.hardware.hwfactoryinterface.V1_0;

import java.util.ArrayList;

public final class FactoryParaType {
    public static final int BSN = 0;
    public static final int COLORTEMP = 3;
    public static final int RUNTEST_TIME = 4;
    public static final int SMARTPA = 2;
    public static final int SN = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "BSN";
        }
        if (o == 1) {
            return "SN";
        }
        if (o == 2) {
            return "SMARTPA";
        }
        if (o == 3) {
            return "COLORTEMP";
        }
        if (o == 4) {
            return "RUNTEST_TIME";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("BSN");
        if ((o & 1) == 1) {
            list.add("SN");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("SMARTPA");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("COLORTEMP");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("RUNTEST_TIME");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
