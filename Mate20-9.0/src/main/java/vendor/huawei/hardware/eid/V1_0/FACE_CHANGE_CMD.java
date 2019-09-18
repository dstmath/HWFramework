package vendor.huawei.hardware.eid.V1_0;

import java.util.ArrayList;

public final class FACE_CHANGE_CMD {
    public static final int FACE_CHANGE_CLEAR = 2;
    public static final int FACE_CHANGE_QUERY = 1;
    public static final int FACE_CHANGE_SET = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "FACE_CHANGE_SET";
        }
        if (o == 1) {
            return "FACE_CHANGE_QUERY";
        }
        if (o == 2) {
            return "FACE_CHANGE_CLEAR";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("FACE_CHANGE_SET");
        if ((o & 1) == 1) {
            list.add("FACE_CHANGE_QUERY");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("FACE_CHANGE_CLEAR");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
