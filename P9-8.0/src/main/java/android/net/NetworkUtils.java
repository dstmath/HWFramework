package android.net;

import android.os.Parcel;
import android.util.Log;
import android.util.Pair;
import java.io.FileDescriptor;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Locale;

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    public static native void attachControlPacketFilter(FileDescriptor fileDescriptor, int i) throws SocketException;

    public static native void attachDhcpFilter(FileDescriptor fileDescriptor) throws SocketException;

    public static native void attachRaFilter(FileDescriptor fileDescriptor, int i) throws SocketException;

    public static native boolean bindProcessToNetwork(int i);

    @Deprecated
    public static native boolean bindProcessToNetworkForHostResolution(int i);

    public static native int bindSocketToNetwork(int i, int i2);

    public static native int getBoundNetworkForProcess();

    public static native boolean protectFromVpn(int i);

    public static native boolean queryUserAccess(int i, int i2);

    public static native void setupRaSocket(FileDescriptor fileDescriptor, int i) throws SocketException;

    public static boolean protectFromVpn(FileDescriptor fd) {
        return protectFromVpn(fd.getInt$());
    }

    public static InetAddress intToInetAddress(int hostAddress) {
        try {
            return InetAddress.getByAddress(new byte[]{(byte) (hostAddress & 255), (byte) ((hostAddress >> 8) & 255), (byte) ((hostAddress >> 16) & 255), (byte) ((hostAddress >> 24) & 255)});
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    public static int inetAddressToInt(Inet4Address inetAddr) throws IllegalArgumentException {
        byte[] addr = inetAddr.getAddress();
        return ((((addr[3] & 255) << 24) | ((addr[2] & 255) << 16)) | ((addr[1] & 255) << 8)) | (addr[0] & 255);
    }

    public static int prefixLengthToNetmaskInt(int prefixLength) throws IllegalArgumentException {
        if (prefixLength >= 0 && prefixLength <= 32) {
            return Integer.reverseBytes(-1 << (32 - prefixLength));
        }
        throw new IllegalArgumentException("Invalid prefix length (0 <= prefix <= 32)");
    }

    public static int netmaskIntToPrefixLength(int netmask) {
        return Integer.bitCount(netmask);
    }

    public static int netmaskToPrefixLength(Inet4Address netmask) {
        int i = Integer.reverseBytes(inetAddressToInt(netmask));
        int prefixLength = Integer.bitCount(i);
        if (Integer.numberOfTrailingZeros(i) == 32 - prefixLength) {
            return prefixLength;
        }
        throw new IllegalArgumentException("Non-contiguous netmask: " + Integer.toHexString(i));
    }

    public static InetAddress numericToInetAddress(String addrString) throws IllegalArgumentException {
        return InetAddress.parseNumericAddress(addrString);
    }

    protected static void parcelInetAddress(Parcel parcel, InetAddress address, int flags) {
        parcel.writeByteArray(address != null ? address.getAddress() : null);
    }

    protected static InetAddress unparcelInetAddress(Parcel in) {
        InetAddress inetAddress = null;
        byte[] addressArray = in.createByteArray();
        if (addressArray == null) {
            return inetAddress;
        }
        try {
            return InetAddress.getByAddress(addressArray);
        } catch (UnknownHostException e) {
            return inetAddress;
        }
    }

    public static void maskRawAddress(byte[] array, int prefixLength) {
        if (prefixLength < 0 || prefixLength > array.length * 8) {
            throw new RuntimeException("IP address with " + array.length + " bytes has invalid prefix length " + prefixLength);
        }
        int offset = prefixLength / 8;
        byte mask = (byte) (255 << (8 - (prefixLength % 8)));
        if (offset < array.length) {
            array[offset] = (byte) (array[offset] & mask);
        }
        while (true) {
            offset++;
            if (offset < array.length) {
                array[offset] = (byte) 0;
            } else {
                return;
            }
        }
    }

    public static InetAddress getNetworkPart(InetAddress address, int prefixLength) {
        byte[] array = address.getAddress();
        maskRawAddress(array, prefixLength);
        try {
            return InetAddress.getByAddress(array);
        } catch (UnknownHostException e) {
            throw new RuntimeException("getNetworkPart error - " + e.toString());
        }
    }

    public static int getImplicitNetmask(Inet4Address address) {
        int firstByte = address.getAddress()[0] & 255;
        if (firstByte < 128) {
            return 8;
        }
        if (firstByte < 192) {
            return 16;
        }
        if (firstByte < 224) {
            return 24;
        }
        return 32;
    }

    public static Pair<InetAddress, Integer> parseIpAndMask(String ipAndMaskString) {
        Object address = null;
        int prefixLength = -1;
        try {
            String[] pieces = ipAndMaskString.split("/", 2);
            prefixLength = Integer.parseInt(pieces[1]);
            address = InetAddress.parseNumericAddress(pieces[0]);
        } catch (NullPointerException e) {
        } catch (ArrayIndexOutOfBoundsException e2) {
        } catch (NumberFormatException e3) {
        } catch (IllegalArgumentException e4) {
        }
        if (address != null && prefixLength != -1) {
            return new Pair(address, Integer.valueOf(prefixLength));
        }
        throw new IllegalArgumentException("Invalid IP address and mask " + ipAndMaskString);
    }

    public static boolean addressTypeMatches(InetAddress left, InetAddress right) {
        if ((left instanceof Inet4Address) && (right instanceof Inet4Address)) {
            return true;
        }
        return left instanceof Inet6Address ? right instanceof Inet6Address : false;
    }

    public static InetAddress hexToInet6Address(String addrHexString) throws IllegalArgumentException {
        try {
            return numericToInetAddress(String.format(Locale.US, "%s:%s:%s:%s:%s:%s:%s:%s", new Object[]{addrHexString.substring(0, 4), addrHexString.substring(4, 8), addrHexString.substring(8, 12), addrHexString.substring(12, 16), addrHexString.substring(16, 20), addrHexString.substring(20, 24), addrHexString.substring(24, 28), addrHexString.substring(28, 32)}));
        } catch (Exception e) {
            Log.e(TAG, "error in hexToInet6Address(" + addrHexString + "): " + e);
            throw new IllegalArgumentException(e);
        }
    }

    public static String[] makeStrings(Collection<InetAddress> addrs) {
        String[] result = new String[addrs.size()];
        int i = 0;
        for (InetAddress addr : addrs) {
            int i2 = i + 1;
            result[i] = addr.getHostAddress();
            i = i2;
        }
        return result;
    }

    public static String trimV4AddrZeros(String addr) {
        if (addr == null) {
            return null;
        }
        String[] octets = addr.split("\\.");
        if (octets.length != 4) {
            return addr;
        }
        StringBuilder builder = new StringBuilder(16);
        int i = 0;
        while (i < 4) {
            try {
                if (octets[i].length() > 3) {
                    return addr;
                }
                builder.append(Integer.parseInt(octets[i]));
                if (i < 3) {
                    builder.append('.');
                }
                i++;
            } catch (NumberFormatException e) {
                return addr;
            }
        }
        return builder.toString();
    }
}
