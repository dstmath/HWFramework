package android.net.shared;

import android.os.Parcel;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressUtils {
    public static void parcelInetAddress(Parcel parcel, InetAddress address, int flags) {
        parcel.writeByteArray(address != null ? address.getAddress() : null);
    }

    public static InetAddress unparcelInetAddress(Parcel in) {
        byte[] addressArray = in.createByteArray();
        if (addressArray == null) {
            return null;
        }
        try {
            return InetAddress.getByAddress(addressArray);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private InetAddressUtils() {
    }
}
