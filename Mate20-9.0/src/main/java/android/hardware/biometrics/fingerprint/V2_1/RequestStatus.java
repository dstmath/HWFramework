package android.hardware.biometrics.fingerprint.V2_1;

import java.util.ArrayList;

public final class RequestStatus {
    public static final int SYS_EACCES = -13;
    public static final int SYS_EAGAIN = -11;
    public static final int SYS_EBUSY = -16;
    public static final int SYS_EFAULT = -14;
    public static final int SYS_EINTR = -4;
    public static final int SYS_EINVAL = -22;
    public static final int SYS_EIO = -5;
    public static final int SYS_ENOENT = -2;
    public static final int SYS_ENOMEM = -12;
    public static final int SYS_ENOSPC = -28;
    public static final int SYS_ETIMEDOUT = -110;
    public static final int SYS_OK = 0;
    public static final int SYS_UNKNOWN = 1;

    public static final String toString(int o) {
        if (o == 1) {
            return "SYS_UNKNOWN";
        }
        if (o == 0) {
            return "SYS_OK";
        }
        if (o == -2) {
            return "SYS_ENOENT";
        }
        if (o == -4) {
            return "SYS_EINTR";
        }
        if (o == -5) {
            return "SYS_EIO";
        }
        if (o == -11) {
            return "SYS_EAGAIN";
        }
        if (o == -12) {
            return "SYS_ENOMEM";
        }
        if (o == -13) {
            return "SYS_EACCES";
        }
        if (o == -14) {
            return "SYS_EFAULT";
        }
        if (o == -16) {
            return "SYS_EBUSY";
        }
        if (o == -22) {
            return "SYS_EINVAL";
        }
        if (o == -28) {
            return "SYS_ENOSPC";
        }
        if (o == -110) {
            return "SYS_ETIMEDOUT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("SYS_UNKNOWN");
            flipped = 0 | 1;
        }
        list.add("SYS_OK");
        if ((o & -2) == -2) {
            list.add("SYS_ENOENT");
            flipped |= -2;
        }
        if ((o & -4) == -4) {
            list.add("SYS_EINTR");
            flipped |= -4;
        }
        if ((o & -5) == -5) {
            list.add("SYS_EIO");
            flipped |= -5;
        }
        if ((o & -11) == -11) {
            list.add("SYS_EAGAIN");
            flipped |= -11;
        }
        if ((o & -12) == -12) {
            list.add("SYS_ENOMEM");
            flipped |= -12;
        }
        if ((o & -13) == -13) {
            list.add("SYS_EACCES");
            flipped |= -13;
        }
        if ((o & -14) == -14) {
            list.add("SYS_EFAULT");
            flipped |= -14;
        }
        if ((o & -16) == -16) {
            list.add("SYS_EBUSY");
            flipped |= -16;
        }
        if ((o & -22) == -22) {
            list.add("SYS_EINVAL");
            flipped |= -22;
        }
        if ((o & -28) == -28) {
            list.add("SYS_ENOSPC");
            flipped |= -28;
        }
        if ((o & SYS_ETIMEDOUT) == -110) {
            list.add("SYS_ETIMEDOUT");
            flipped |= SYS_ETIMEDOUT;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
