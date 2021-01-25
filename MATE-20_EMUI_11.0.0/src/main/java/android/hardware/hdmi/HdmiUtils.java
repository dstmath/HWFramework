package android.hardware.hdmi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class HdmiUtils {
    public static final int HDMI_RELATIVE_POSITION_ABOVE = 5;
    public static final int HDMI_RELATIVE_POSITION_BELOW = 2;
    public static final int HDMI_RELATIVE_POSITION_DIFFERENT_BRANCH = 7;
    public static final int HDMI_RELATIVE_POSITION_DIRECTLY_ABOVE = 4;
    public static final int HDMI_RELATIVE_POSITION_DIRECTLY_BELOW = 1;
    public static final int HDMI_RELATIVE_POSITION_SAME = 3;
    public static final int HDMI_RELATIVE_POSITION_SIBLING = 6;
    public static final int HDMI_RELATIVE_POSITION_UNKNOWN = 0;
    private static final int NPOS = -1;
    static final int TARGET_NOT_UNDER_LOCAL_DEVICE = -1;
    static final int TARGET_SAME_PHYSICAL_ADDRESS = 0;

    @Retention(RetentionPolicy.SOURCE)
    public @interface HdmiAddressRelativePosition {
    }

    private HdmiUtils() {
    }

    public static int getLocalPortFromPhysicalAddress(int targetPhysicalAddress, int myPhysicalAddress) {
        if (myPhysicalAddress == targetPhysicalAddress) {
            return 0;
        }
        int mask = 61440;
        int finalMask = 61440;
        int maskedAddress = myPhysicalAddress;
        while (maskedAddress != 0) {
            maskedAddress = myPhysicalAddress & mask;
            finalMask |= mask;
            mask >>= 4;
        }
        int portAddress = targetPhysicalAddress & finalMask;
        if (((finalMask << 4) & portAddress) != myPhysicalAddress) {
            return -1;
        }
        int port = portAddress & (mask << 4);
        while ((port >> 4) != 0) {
            port >>= 4;
        }
        return port;
    }

    public static boolean isValidPhysicalAddress(int address) {
        if (address < 0 || address >= 65535) {
            return false;
        }
        int mask = 61440;
        boolean hasZero = false;
        for (int i = 0; i < 4; i++) {
            if ((address & mask) == 0) {
                hasZero = true;
            } else if (hasZero) {
                return false;
            }
            mask >>= 4;
        }
        return true;
    }

    public static int getHdmiAddressRelativePosition(int src, int dest) {
        if (src == 65535 || dest == 65535) {
            return 0;
        }
        try {
            int firstDiffPos = physicalAddressFirstDifferentDigitPos(src, dest);
            if (firstDiffPos == -1) {
                return 3;
            }
            int mask = 61440 >> (firstDiffPos * 4);
            int nextPos = firstDiffPos + 1;
            if ((src & mask) == 0) {
                if (nextPos == 4 || ((61440 >> (nextPos * 4)) & dest) == 0) {
                    return 4;
                }
                return 5;
            } else if ((dest & mask) == 0) {
                if (nextPos == 4 || ((61440 >> (nextPos * 4)) & src) == 0) {
                    return 1;
                }
                return 2;
            } else if (nextPos == 4) {
                return 6;
            } else {
                if (((61440 >> (nextPos * 4)) & src) == 0 && ((61440 >> (nextPos * 4)) & dest) == 0) {
                    return 6;
                }
                return 7;
            }
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    private static int physicalAddressFirstDifferentDigitPos(int address1, int address2) throws IllegalArgumentException {
        if (!isValidPhysicalAddress(address1)) {
            throw new IllegalArgumentException(address1 + " is not a valid address.");
        } else if (isValidPhysicalAddress(address2)) {
            int mask = 61440;
            for (int i = 0; i < 4; i++) {
                if ((address1 & mask) != (address2 & mask)) {
                    return i;
                }
                mask >>= 4;
            }
            return -1;
        } else {
            throw new IllegalArgumentException(address2 + " is not a valid address.");
        }
    }
}
