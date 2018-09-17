package vendor.huawei.hardware.radio.V1_0;

import java.util.ArrayList;

public final class RILImsRegState {
    public static final int IMS_ERG_STATE_NOT_REGISTERED = 2;
    public static final int IMS_ERG_STATE_REGISTERED = 1;
    public static final int IMS_ERG_STATE_REGISTERING = 3;

    public static final String toString(int o) {
        if (o == 1) {
            return "IMS_ERG_STATE_REGISTERED";
        }
        if (o == 2) {
            return "IMS_ERG_STATE_NOT_REGISTERED";
        }
        if (o == 3) {
            return "IMS_ERG_STATE_REGISTERING";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("IMS_ERG_STATE_REGISTERED");
            flipped = 1;
        }
        if ((o & 2) == 2) {
            list.add("IMS_ERG_STATE_NOT_REGISTERED");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("IMS_ERG_STATE_REGISTERING");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
