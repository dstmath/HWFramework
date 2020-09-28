package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class SubscriptionType {
    public static final int SUBSCRIPTION_1 = 0;
    public static final int SUBSCRIPTION_2 = 1;
    public static final int SUBSCRIPTION_3 = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "SUBSCRIPTION_1";
        }
        if (o == 1) {
            return "SUBSCRIPTION_2";
        }
        if (o == 2) {
            return "SUBSCRIPTION_3";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("SUBSCRIPTION_1");
        if ((o & 1) == 1) {
            list.add("SUBSCRIPTION_2");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("SUBSCRIPTION_3");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
