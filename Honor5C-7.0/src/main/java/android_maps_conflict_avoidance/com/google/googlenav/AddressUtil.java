package android_maps_conflict_avoidance.com.google.googlenav;

import android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBuf;

public class AddressUtil {
    private AddressUtil() {
    }

    public static String getAddressLine(int addressLineIndex, int number, ProtoBuf proto) {
        if (proto == null) {
            return "";
        }
        return proto.getCount(addressLineIndex) <= number ? "" : proto.getString(addressLineIndex, number);
    }
}
