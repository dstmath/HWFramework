package android.hardware.cas.V1_0;

import java.util.ArrayList;

public final class Status {
    public static final int BAD_VALUE = 6;
    public static final int ERROR_CAS_CANNOT_HANDLE = 4;
    public static final int ERROR_CAS_DECRYPT = 13;
    public static final int ERROR_CAS_DECRYPT_UNIT_NOT_INITIALIZED = 12;
    public static final int ERROR_CAS_DEVICE_REVOKED = 11;
    public static final int ERROR_CAS_INSUFFICIENT_OUTPUT_PROTECTION = 9;
    public static final int ERROR_CAS_INVALID_STATE = 5;
    public static final int ERROR_CAS_LICENSE_EXPIRED = 2;
    public static final int ERROR_CAS_NOT_PROVISIONED = 7;
    public static final int ERROR_CAS_NO_LICENSE = 1;
    public static final int ERROR_CAS_RESOURCE_BUSY = 8;
    public static final int ERROR_CAS_SESSION_NOT_OPENED = 3;
    public static final int ERROR_CAS_TAMPER_DETECTED = 10;
    public static final int ERROR_CAS_UNKNOWN = 14;
    public static final int OK = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "OK";
        }
        if (o == 1) {
            return "ERROR_CAS_NO_LICENSE";
        }
        if (o == 2) {
            return "ERROR_CAS_LICENSE_EXPIRED";
        }
        if (o == 3) {
            return "ERROR_CAS_SESSION_NOT_OPENED";
        }
        if (o == 4) {
            return "ERROR_CAS_CANNOT_HANDLE";
        }
        if (o == 5) {
            return "ERROR_CAS_INVALID_STATE";
        }
        if (o == 6) {
            return "BAD_VALUE";
        }
        if (o == 7) {
            return "ERROR_CAS_NOT_PROVISIONED";
        }
        if (o == 8) {
            return "ERROR_CAS_RESOURCE_BUSY";
        }
        if (o == 9) {
            return "ERROR_CAS_INSUFFICIENT_OUTPUT_PROTECTION";
        }
        if (o == 10) {
            return "ERROR_CAS_TAMPER_DETECTED";
        }
        if (o == 11) {
            return "ERROR_CAS_DEVICE_REVOKED";
        }
        if (o == 12) {
            return "ERROR_CAS_DECRYPT_UNIT_NOT_INITIALIZED";
        }
        if (o == 13) {
            return "ERROR_CAS_DECRYPT";
        }
        if (o == 14) {
            return "ERROR_CAS_UNKNOWN";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("OK");
        if ((o & 1) == 1) {
            list.add("ERROR_CAS_NO_LICENSE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("ERROR_CAS_LICENSE_EXPIRED");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("ERROR_CAS_SESSION_NOT_OPENED");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("ERROR_CAS_CANNOT_HANDLE");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("ERROR_CAS_INVALID_STATE");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("BAD_VALUE");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("ERROR_CAS_NOT_PROVISIONED");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("ERROR_CAS_RESOURCE_BUSY");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("ERROR_CAS_INSUFFICIENT_OUTPUT_PROTECTION");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("ERROR_CAS_TAMPER_DETECTED");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("ERROR_CAS_DEVICE_REVOKED");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("ERROR_CAS_DECRYPT_UNIT_NOT_INITIALIZED");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("ERROR_CAS_DECRYPT");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("ERROR_CAS_UNKNOWN");
            flipped |= 14;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
