package java.net;

import android.system.OsConstants;
import java.io.ObjectStreamException;

public final class Inet4Address extends InetAddress {
    public static final InetAddress ALL = new Inet4Address(null, new byte[]{(byte) -1, (byte) -1, (byte) -1, (byte) -1});
    public static final InetAddress ANY = new Inet4Address(null, new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0});
    static final int INADDRSZ = 4;
    public static final InetAddress LOOPBACK = new Inet4Address("localhost", new byte[]{Byte.MAX_VALUE, (byte) 0, (byte) 0, (byte) 1});
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
            holder().address = (((addr[3] & 255) | ((addr[2] << 8) & 65280)) | ((addr[1] << 16) & 16711680)) | ((addr[0] << 24) & -16777216);
        }
    }

    Inet4Address(String hostName, int address) {
        holder().hostName = hostName;
        holder().family = OsConstants.AF_INET;
        holder().address = address;
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
        if (getAddress()[0] == Byte.MAX_VALUE) {
            return true;
        }
        return false;
    }

    public boolean isLinkLocalAddress() {
        int address = holder().getAddress();
        if (((address >>> 24) & 255) == 169 && ((address >>> 16) & 255) == 254) {
            return true;
        }
        return false;
    }

    public boolean isSiteLocalAddress() {
        int address = holder().getAddress();
        if (((address >>> 24) & 255) == 10) {
            return true;
        }
        if (((address >>> 24) & 255) == 172 && ((address >>> 16) & 240) == 16) {
            return true;
        }
        if (((address >>> 24) & 255) != 192) {
            return false;
        }
        if (((address >>> 16) & 255) != 168) {
            return false;
        }
        return true;
    }

    public boolean isMCGlobal() {
        byte[] byteAddr = getAddress();
        if ((byteAddr[0] & 255) < 224 || (byteAddr[0] & 255) > 238) {
            return false;
        }
        if ((byteAddr[0] & 255) == 224 && byteAddr[1] == (byte) 0 && byteAddr[2] == (byte) 0) {
            return false;
        }
        return true;
    }

    public boolean isMCNodeLocal() {
        return false;
    }

    public boolean isMCLinkLocal() {
        int address = holder().getAddress();
        if (((address >>> 24) & 255) == 224 && ((address >>> 16) & 255) == 0 && ((address >>> 8) & 255) == 0) {
            return true;
        }
        return false;
    }

    public boolean isMCSiteLocal() {
        int address = holder().getAddress();
        if (((address >>> 24) & 255) == 239 && ((address >>> 16) & 255) == 255) {
            return true;
        }
        return false;
    }

    public boolean isMCOrgLocal() {
        int address = holder().getAddress();
        if (((address >>> 24) & 255) != 239 || ((address >>> 16) & 255) < 192 || ((address >>> 16) & 255) > 195) {
            return false;
        }
        return true;
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
        if (obj != null && (obj instanceof Inet4Address) && ((InetAddress) obj).holder().getAddress() == holder().getAddress()) {
            return true;
        }
        return false;
    }

    static String numericToTextFormat(byte[] src) {
        return (src[0] & 255) + "." + (src[1] & 255) + "." + (src[2] & 255) + "." + (src[3] & 255);
    }
}
