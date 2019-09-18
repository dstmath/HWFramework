package java.net;

import android.system.OsConstants;
import java.io.ObjectStreamException;

public final class Inet4Address extends InetAddress {
    public static final InetAddress ALL = new Inet4Address((String) null, new byte[]{-1, -1, -1, -1});
    public static final InetAddress ANY = new Inet4Address((String) null, new byte[]{0, 0, 0, 0});
    static final int INADDRSZ = 4;
    public static final InetAddress LOOPBACK = new Inet4Address("localhost", new byte[]{Byte.MAX_VALUE, 0, 0, 1});
    private static final long serialVersionUID = 3286316764910316507L;

    Inet4Address() {
        holder().hostName = null;
        holder().address = 0;
        holder().family = OsConstants.AF_INET;
    }

    Inet4Address(String hostName, byte[] addr) {
        holder().hostName = hostName;
        holder().family = OsConstants.AF_INET;
        if (addr != null && addr.length == 4) {
            holder().address = (addr[3] & 255) | ((addr[2] << 8) & 65280) | ((addr[1] << 16) & 16711680) | ((addr[0] << 24) & -16777216);
        }
        holder().originalHostName = hostName;
    }

    Inet4Address(String hostName, int address) {
        holder().hostName = hostName;
        holder().family = OsConstants.AF_INET;
        holder().address = address;
        holder().originalHostName = hostName;
    }

    private Object writeReplace() throws ObjectStreamException {
        InetAddress inet = new InetAddress();
        inet.holder().hostName = holder().getHostName();
        inet.holder().address = holder().getAddress();
        inet.holder().family = 2;
        return inet;
    }

    public boolean isMulticastAddress() {
        return (holder().getAddress() & -268435456) == -536870912;
    }

    public boolean isAnyLocalAddress() {
        return holder().getAddress() == 0;
    }

    public boolean isLoopbackAddress() {
        return getAddress()[0] == Byte.MAX_VALUE;
    }

    public boolean isLinkLocalAddress() {
        int address = holder().getAddress();
        return ((address >>> 24) & 255) == 169 && ((address >>> 16) & 255) == 254;
    }

    public boolean isSiteLocalAddress() {
        int address = holder().getAddress();
        return ((address >>> 24) & 255) == 10 || (((address >>> 24) & 255) == 172 && ((address >>> 16) & 240) == 16) || (((address >>> 24) & 255) == 192 && ((address >>> 16) & 255) == 168);
    }

    public boolean isMCGlobal() {
        byte[] byteAddr = getAddress();
        if ((byteAddr[0] & Character.DIRECTIONALITY_UNDEFINED) < 224 || (byteAddr[0] & Character.DIRECTIONALITY_UNDEFINED) > 238) {
            return false;
        }
        return ((byteAddr[0] & Character.DIRECTIONALITY_UNDEFINED) == 224 && byteAddr[1] == 0 && byteAddr[2] == 0) ? false : true;
    }

    public boolean isMCNodeLocal() {
        return false;
    }

    public boolean isMCLinkLocal() {
        int address = holder().getAddress();
        return ((address >>> 24) & 255) == 224 && ((address >>> 16) & 255) == 0 && ((address >>> 8) & 255) == 0;
    }

    public boolean isMCSiteLocal() {
        int address = holder().getAddress();
        return ((address >>> 24) & 255) == 239 && ((address >>> 16) & 255) == 255;
    }

    public boolean isMCOrgLocal() {
        int address = holder().getAddress();
        return ((address >>> 24) & 255) == 239 && ((address >>> 16) & 255) >= 192 && ((address >>> 16) & 255) <= 195;
    }

    public byte[] getAddress() {
        int address = holder().getAddress();
        return new byte[]{(byte) ((address >>> 24) & 255), (byte) ((address >>> 16) & 255), (byte) ((address >>> 8) & 255), (byte) (address & 255)};
    }

    public String getHostAddress() {
        return numericToTextFormat(getAddress());
    }

    public int hashCode() {
        return holder().getAddress();
    }

    public boolean equals(Object obj) {
        return obj != null && (obj instanceof Inet4Address) && ((InetAddress) obj).holder().getAddress() == holder().getAddress();
    }

    static String numericToTextFormat(byte[] src) {
        return (src[0] & Character.DIRECTIONALITY_UNDEFINED) + "." + (src[1] & Character.DIRECTIONALITY_UNDEFINED) + "." + (src[2] & Character.DIRECTIONALITY_UNDEFINED) + "." + (src[3] & Character.DIRECTIONALITY_UNDEFINED);
    }
}
