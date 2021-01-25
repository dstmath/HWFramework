package android.hardware.biometrics.face.V1_0;

import java.util.ArrayList;

public final class UserHandle {
    public static final int NONE = -1;

    public static final String toString(int o) {
        if (o == -1) {
            return "NONE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & -1) == -1) {
            list.add("NONE");
            flipped = 0 | -1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
