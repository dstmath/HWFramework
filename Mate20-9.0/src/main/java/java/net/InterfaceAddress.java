package java.net;

public class InterfaceAddress {
    private InetAddress address = null;
    private Inet4Address broadcast = null;
    private short maskLength = 0;

    InterfaceAddress() {
    }

    InterfaceAddress(InetAddress address2, Inet4Address broadcast2, InetAddress netmask) {
        this.address = address2;
        this.broadcast = broadcast2;
        this.maskLength = countPrefixLength(netmask);
    }

    private short countPrefixLength(InetAddress netmask) {
        short count = 0;
        for (byte b : netmask.getAddress()) {
            while (b != 0) {
                b = (byte) (b << 1);
                count = (short) (count + 1);
            }
        }
        return count;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public InetAddress getBroadcast() {
        return this.broadcast;
    }

    public short getNetworkPrefixLength() {
        return this.maskLength;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof InterfaceAddress)) {
            return false;
        }
        InterfaceAddress cmp = (InterfaceAddress) obj;
        if (this.address != null ? !this.address.equals(cmp.address) : cmp.address != null) {
            return false;
        }
        if (this.broadcast != null ? !this.broadcast.equals(cmp.broadcast) : cmp.broadcast != null) {
            return false;
        }
        if (this.maskLength != cmp.maskLength) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.address.hashCode() + (this.broadcast != null ? this.broadcast.hashCode() : 0) + this.maskLength;
    }

    public String toString() {
        return this.address + "/" + this.maskLength + " [" + this.broadcast + "]";
    }
}
