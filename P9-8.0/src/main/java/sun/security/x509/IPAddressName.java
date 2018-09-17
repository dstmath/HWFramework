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

    public IPAddressName(byte[] address) throws IOException {
        if (address.length == 4 || address.length == 8) {
            this.isIPv4 = true;
        } else if (address.length == 16 || address.length == 32) {
            this.isIPv4 = false;
        } else {
            throw new IOException("Invalid IPAddressName");
        }
        this.address = address;
    }

    public IPAddressName(String name) throws IOException {
        if (name == null || name.length() == 0) {
            throw new IOException("IPAddress cannot be null or empty");
        } else if (name.charAt(name.length() - 1) == '/') {
            throw new IOException("Invalid IPAddress: " + name);
        } else if (name.indexOf(58) >= 0) {
            parseIPv6(name);
            this.isIPv4 = false;
        } else if (name.indexOf(46) >= 0) {
            parseIPv4(name);
            this.isIPv4 = true;
        } else {
            throw new IOException("Invalid IPAddress: " + name);
        }
    }

    private void parseIPv4(String name) throws IOException {
        int slashNdx = name.indexOf(47);
        if (slashNdx == -1) {
            this.address = InetAddress.getByName(name).getAddress();
            return;
        }
        this.address = new byte[8];
        byte[] mask = InetAddress.getByName(name.substring(slashNdx + 1)).getAddress();
        System.arraycopy(InetAddress.getByName(name.substring(0, slashNdx)).getAddress(), 0, this.address, 0, 4);
        System.arraycopy(mask, 0, this.address, 4, 4);
    }

    private void parseIPv6(String name) throws IOException {
        int slashNdx = name.indexOf(47);
        if (slashNdx == -1) {
            this.address = InetAddress.getByName(name).getAddress();
            return;
        }
        this.address = new byte[32];
        System.arraycopy(InetAddress.getByName(name.substring(0, slashNdx)).getAddress(), 0, this.address, 0, 16);
        int prefixLen = Integer.parseInt(name.substring(slashNdx + 1));
        if (prefixLen > 128) {
            throw new IOException("IPv6Address prefix is longer than 128");
        }
        int i;
        BitArray bitArray = new BitArray(128);
        for (i = 0; i < prefixLen; i++) {
            bitArray.set(i, true);
        }
        byte[] maskArray = bitArray.toByteArray();
        for (i = 0; i < 16; i++) {
            this.address[i + 16] = maskArray[i];
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
            return "IPAddress: " + new HexDumpEncoder().encodeBuffer(this.address);
        }
    }

    public String getName() throws IOException {
        if (this.name != null) {
            return this.name;
        }
        byte[] host;
        if (this.isIPv4) {
            host = new byte[4];
            System.arraycopy(this.address, 0, host, 0, 4);
            this.name = InetAddress.getByAddress(host).getHostAddress();
            if (this.address.length == 8) {
                byte[] mask = new byte[4];
                System.arraycopy(this.address, 4, mask, 0, 4);
                this.name += "/" + InetAddress.getByAddress(mask).getHostAddress();
            }
        } else {
            host = new byte[16];
            System.arraycopy(this.address, 0, host, 0, 16);
            this.name = InetAddress.getByAddress(host).getHostAddress();
            if (this.address.length == 32) {
                int i;
                byte[] maskBytes = new byte[16];
                for (i = 16; i < 32; i++) {
                    maskBytes[i - 16] = this.address[i];
                }
                BitArray ba = new BitArray(128, maskBytes);
                i = 0;
                while (i < 128 && ba.get(i)) {
                    i++;
                }
                this.name += "/" + i;
                while (i < 128) {
                    if (ba.get(i)) {
                        throw new IOException("Invalid IPv6 subdomain - set bit " + i + " not contiguous");
                    }
                    i++;
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
        byte[] other = ((IPAddressName) obj).getBytes();
        if (other.length != this.address.length) {
            return false;
        }
        if (this.address.length != 8 && this.address.length != 32) {
            return Arrays.equals(other, this.address);
        }
        int i;
        int maskLen = this.address.length / 2;
        byte[] maskedThis = new byte[maskLen];
        byte[] maskedOther = new byte[maskLen];
        for (i = 0; i < maskLen; i++) {
            maskedThis[i] = (byte) (this.address[i] & this.address[i + maskLen]);
            maskedOther[i] = (byte) (other[i] & other[i + maskLen]);
            if (maskedThis[i] != maskedOther[i]) {
                return false;
            }
        }
        for (i = maskLen; i < this.address.length; i++) {
            if (this.address[i] != other[i]) {
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
        byte[] otherAddress = ((IPAddressName) inputName).getBytes();
        if (otherAddress.length == 4 && this.address.length == 4) {
            return 3;
        }
        int maskOffset;
        int i;
        if ((otherAddress.length == 8 && this.address.length == 8) || (otherAddress.length == 32 && this.address.length == 32)) {
            boolean otherSubsetOfThis = true;
            boolean thisSubsetOfOther = true;
            boolean thisEmpty = false;
            boolean otherEmpty = false;
            maskOffset = this.address.length / 2;
            i = 0;
            while (i < maskOffset) {
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
                i++;
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
            i = 0;
            maskOffset = otherAddress.length / 2;
            while (i < maskOffset && (this.address[i] & otherAddress[i + maskOffset]) == otherAddress[i]) {
                i++;
            }
            if (i == maskOffset) {
                return 2;
            }
            return 3;
        } else if (this.address.length != 8 && this.address.length != 32) {
            return 3;
        } else {
            i = 0;
            maskOffset = this.address.length / 2;
            while (i < maskOffset && (otherAddress[i] & this.address[i + maskOffset]) == this.address[i]) {
                i++;
            }
            if (i == maskOffset) {
                return 1;
            }
            return 3;
        }
    }

    public int subtreeDepth() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("subtreeDepth() not defined for IPAddressName");
    }
}
