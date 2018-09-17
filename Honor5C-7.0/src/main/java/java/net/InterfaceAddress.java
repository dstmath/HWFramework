package java.net;

public class InterfaceAddress {
    private InetAddress address;
    private Inet4Address broadcast;
    private short maskLength;

    InterfaceAddress() {
        this.address = null;
        this.broadcast = null;
        this.maskLength = (short) 0;
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
        boolean equals = this.address == null ? cmp.address == null : this.address.equals(cmp.address);
        if (!equals) {
            return false;
        }
        equals = this.broadcast == null ? cmp.broadcast == null : this.broadcast.equals(cmp.broadcast);
        return equals && this.maskLength == cmp.maskLength;
    }

    public int hashCode() {
        return ((this.broadcast != null ? this.broadcast.hashCode() : 0) + this.address.hashCode()) + this.maskLength;
    }

    public String toString() {
        return this.address + "/" + this.maskLength + " [" + this.broadcast + "]";
    }
}
