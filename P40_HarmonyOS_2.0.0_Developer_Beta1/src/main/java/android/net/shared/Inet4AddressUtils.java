package android.net.shared;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Inet4AddressUtils {
    public static Inet4Address intToInet4AddressHTL(int hostAddress) {
        return intToInet4AddressHTH(Integer.reverseBytes(hostAddress));
    }

    public static Inet4Address intToInet4AddressHTH(int hostAddress) {
        try {
            return (Inet4Address) InetAddress.getByAddress(new byte[]{(byte) ((hostAddress >> 24) & 255), (byte) ((hostAddress >> 16) & 255), (byte) ((hostAddress >> 8) & 255), (byte) (hostAddress & 255)});
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    public static int inet4AddressToIntHTH(Inet4Address inetAddr) throws IllegalArgumentException {
        byte[] addr = inetAddr.getAddress();
        return ((addr[0] & 255) << 24) | ((addr[1] & 255) << 16) | ((addr[2] & 255) << 8) | (addr[3] & 255);
    }

    public static int inet4AddressToIntHTL(Inet4Address inetAddr) {
        return Integer.reverseBytes(inet4AddressToIntHTH(inetAddr));
    }

    public static int prefixLengthToV4NetmaskIntHTH(int prefixLength) throws IllegalArgumentException {
        if (prefixLength < 0 || prefixLength > 32) {
            throw new IllegalArgumentException("Invalid prefix length (0 <= prefix <= 32)");
        } else if (prefixLength == 0) {
            return 0;
        } else {
            return -1 << (32 - prefixLength);
        }
    }

    public static int prefixLengthToV4NetmaskIntHTL(int prefixLength) throws IllegalArgumentException {
        return Integer.reverseBytes(prefixLengthToV4NetmaskIntHTH(prefixLength));
    }

    public static int netmaskToPrefixLength(Inet4Address netmask) {
        int i = inet4AddressToIntHTH(netmask);
        int prefixLength = Integer.bitCount(i);
        if (Integer.numberOfTrailingZeros(i) == 32 - prefixLength) {
            return prefixLength;
        }
        throw new IllegalArgumentException("Non-contiguous netmask: " + Integer.toHexString(i));
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

    public static Inet4Address getBroadcastAddress(Inet4Address addr, int prefixLength) throws IllegalArgumentException {
        return intToInet4AddressHTH(inet4AddressToIntHTH(addr) | (~prefixLengthToV4NetmaskIntHTH(prefixLength)));
    }

    public static Inet4Address getPrefixMaskAsInet4Address(int prefixLength) throws IllegalArgumentException {
        return intToInet4AddressHTH(prefixLengthToV4NetmaskIntHTH(prefixLength));
    }
}
