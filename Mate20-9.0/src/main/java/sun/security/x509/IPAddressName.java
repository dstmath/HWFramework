package sun.security.x509;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import sun.misc.HexDumpEncoder;
import sun.security.util.BitArray;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class IPAddressName implements GeneralNameInterface {
    private static final int MASKSIZE = 16;
    private byte[] address;
    private boolean isIPv4;
    private String name;

    public IPAddressName(DerValue derValue) throws IOException {
        this(derValue.getOctetString());
    }

    public IPAddressName(byte[] address2) throws IOException {
        if (address2.length == 4 || address2.length == 8) {
            this.isIPv4 = true;
        } else if (address2.length == 16 || address2.length == 32) {
            this.isIPv4 = false;
        } else {
            throw new IOException("Invalid IPAddressName");
        }
        this.address = address2;
    }

    public IPAddressName(String name2) throws IOException {
        if (name2 == null || name2.length() == 0) {
            throw new IOException("IPAddress cannot be null or empty");
        } else if (name2.charAt(name2.length() - 1) == '/') {
            throw new IOException("Invalid IPAddress: " + name2);
        } else if (name2.indexOf(58) >= 0) {
            parseIPv6(name2);
            this.isIPv4 = false;
        } else if (name2.indexOf(46) >= 0) {
            parseIPv4(name2);
            this.isIPv4 = true;
        } else {
            throw new IOException("Invalid IPAddress: " + name2);
        }
    }

    private void parseIPv4(String name2) throws IOException {
        int slashNdx = name2.indexOf(47);
        if (slashNdx == -1) {
            this.address = InetAddress.getByName(name2).getAddress();
            return;
        }
        this.address = new byte[8];
        byte[] mask = InetAddress.getByName(name2.substring(slashNdx + 1)).getAddress();
        System.arraycopy(InetAddress.getByName(name2.substring(0, slashNdx)).getAddress(), 0, this.address, 0, 4);
        System.arraycopy(mask, 0, this.address, 4, 4);
    }

    private void parseIPv6(String name2) throws IOException {
        int slashNdx = name2.indexOf(47);
        if (slashNdx == -1) {
            this.address = InetAddress.getByName(name2).getAddress();
            return;
        }
        this.address = new byte[32];
        System.arraycopy(InetAddress.getByName(name2.substring(0, slashNdx)).getAddress(), 0, this.address, 0, 16);
        int prefixLen = Integer.parseInt(name2.substring(slashNdx + 1));
        if (prefixLen < 0 || prefixLen > 128) {
            throw new IOException("IPv6Address prefix length (" + prefixLen + ") in out of valid range [0,128]");
        }
        BitArray bitArray = new BitArray(128);
        for (int i = 0; i < prefixLen; i++) {
            bitArray.set(i, true);
        }
        byte[] maskArray = bitArray.toByteArray();
        for (int i2 = 0; i2 < 16; i2++) {
            this.address[16 + i2] = maskArray[i2];
        }
    }

    public int getType() {
        return 7;
    }

    public void encode(DerOutputStream out) throws IOException {
        out.putOctetString(this.address);
    }

    public String toString() {
        try {
            return "IPAddress: " + getName();
        } catch (IOException e) {
            new HexDumpEncoder();
            return "IPAddress: " + enc.encodeBuffer(this.address);
        }
    }

    public String getName() throws IOException {
        if (this.name != null) {
            return this.name;
        }
        int i = 0;
        if (this.isIPv4) {
            byte[] host = new byte[4];
            System.arraycopy(this.address, 0, host, 0, 4);
            this.name = InetAddress.getByAddress(host).getHostAddress();
            if (this.address.length == 8) {
                System.arraycopy(this.address, 4, new byte[4], 0, 4);
                this.name += "/" + InetAddress.getByAddress(new byte[4]).getHostAddress();
            }
        } else {
            byte[] host2 = new byte[16];
            System.arraycopy(this.address, 0, host2, 0, 16);
            this.name = InetAddress.getByAddress(host2).getHostAddress();
            if (this.address.length == 32) {
                byte[] maskBytes = new byte[16];
                for (int i2 = 16; i2 < 32; i2++) {
                    maskBytes[i2 - 16] = this.address[i2];
                }
                BitArray ba = new BitArray(128, maskBytes);
                while (i < 128 && ba.get(i)) {
                    i++;
                }
                this.name += "/" + i;
                while (i < 128) {
                    if (!ba.get(i)) {
                        i++;
                    } else {
                        throw new IOException("Invalid IPv6 subdomain - set bit " + i + " not contiguous");
                    }
                }
            }
        }
        return this.name;
    }

    public byte[] getBytes() {
        return (byte[]) this.address.clone();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IPAddressName)) {
            return false;
        }
        byte[] other = ((IPAddressName) obj).address;
        if (other.length != this.address.length) {
            return false;
        }
        if (this.address.length != 8 && this.address.length != 32) {
            return Arrays.equals(other, this.address);
        }
        int maskLen = this.address.length / 2;
        for (int i = 0; i < maskLen; i++) {
            if (((byte) (this.address[i] & this.address[i + maskLen])) != ((byte) (other[i] & other[i + maskLen]))) {
                return false;
            }
        }
        for (int i2 = maskLen; i2 < this.address.length; i2++) {
            if (this.address[i2] != other[i2]) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int retval = 0;
        for (int i = 0; i < this.address.length; i++) {
            retval += this.address[i] * i;
        }
        return retval;
    }

    public int constrains(GeneralNameInterface inputName) throws UnsupportedOperationException {
        if (inputName == null) {
            return -1;
        }
        if (inputName.getType() != 7) {
            return -1;
        }
        if (((IPAddressName) inputName).equals(this)) {
            return 0;
        }
        byte[] otherAddress = ((IPAddressName) inputName).address;
        if (otherAddress.length == 4 && this.address.length == 4) {
            return 3;
        }
        if ((otherAddress.length == 8 && this.address.length == 8) || (otherAddress.length == 32 && this.address.length == 32)) {
            boolean otherSubsetOfThis = true;
            boolean thisSubsetOfOther = true;
            boolean thisEmpty = false;
            boolean otherEmpty = false;
            int maskOffset = this.address.length / 2;
            for (int i = 0; i < maskOffset; i++) {
                if (((byte) (this.address[i] & this.address[i + maskOffset])) != this.address[i]) {
                    thisEmpty = true;
                }
                if (((byte) (otherAddress[i] & otherAddress[i + maskOffset])) != otherAddress[i]) {
                    otherEmpty = true;
                }
                if (!(((byte) (this.address[i + maskOffset] & otherAddress[i + maskOffset])) == this.address[i + maskOffset] && ((byte) (this.address[i] & this.address[i + maskOffset])) == ((byte) (otherAddress[i] & this.address[i + maskOffset])))) {
                    otherSubsetOfThis = false;
                }
                if (((byte) (otherAddress[i + maskOffset] & this.address[i + maskOffset])) != otherAddress[i + maskOffset] || ((byte) (otherAddress[i] & otherAddress[i + maskOffset])) != ((byte) (this.address[i] & otherAddress[i + maskOffset]))) {
                    thisSubsetOfOther = false;
                }
            }
            if (thisEmpty || otherEmpty) {
                if (thisEmpty && otherEmpty) {
                    return 0;
                }
                if (thisEmpty) {
                    return 2;
                }
                return 1;
            } else if (otherSubsetOfThis) {
                return 1;
            } else {
                if (thisSubsetOfOther) {
                    return 2;
                }
                return 3;
            }
        } else if (otherAddress.length == 8 || otherAddress.length == 32) {
            int i2 = 0;
            int maskOffset2 = otherAddress.length / 2;
            while (i2 < maskOffset2 && (this.address[i2] & otherAddress[i2 + maskOffset2]) == otherAddress[i2]) {
                i2++;
            }
            if (i2 == maskOffset2) {
                return 2;
            }
            return 3;
        } else if (this.address.length != 8 && this.address.length != 32) {
            return 3;
        } else {
            int i3 = 0;
            int maskOffset3 = this.address.length / 2;
            while (i3 < maskOffset3 && (otherAddress[i3] & this.address[i3 + maskOffset3]) == this.address[i3]) {
                i3++;
            }
            if (i3 == maskOffset3) {
                return 1;
            }
            return 3;
        }
    }

    public int subtreeDepth() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("subtreeDepth() not defined for IPAddressName");
    }
}
