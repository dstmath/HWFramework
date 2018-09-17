package javax.net.ssl;

import java.util.Arrays;

public abstract class SNIServerName {
    private static final char[] HEXES = "0123456789ABCDEF".toCharArray();
    private final byte[] encoded;
    private final int type;

    protected SNIServerName(int type, byte[] encoded) {
        if (type < 0) {
            throw new IllegalArgumentException("Server name type cannot be less than zero");
        } else if (type > 255) {
            throw new IllegalArgumentException("Server name type cannot be greater than 255");
        } else {
            this.type = type;
            if (encoded == null) {
                throw new NullPointerException("Server name encoded value cannot be null");
            }
            this.encoded = (byte[]) encoded.clone();
        }
    }

    public final int getType() {
        return this.type;
    }

    public final byte[] getEncoded() {
        return (byte[]) this.encoded.clone();
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (this == other) {
            return true;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        SNIServerName that = (SNIServerName) other;
        if (this.type == that.type) {
            z = Arrays.equals(this.encoded, that.encoded);
        }
        return z;
    }

    public int hashCode() {
        return ((this.type + 527) * 31) + Arrays.hashCode(this.encoded);
    }

    public String toString() {
        if (this.type == 0) {
            return "type=host_name (0), value=" + toHexString(this.encoded);
        }
        return "type=(" + this.type + "), value=" + toHexString(this.encoded);
    }

    private static String toHexString(byte[] bytes) {
        if (bytes.length == 0) {
            return "(empty)";
        }
        StringBuilder sb = new StringBuilder((bytes.length * 3) - 1);
        boolean isInitial = true;
        for (byte b : bytes) {
            if (isInitial) {
                isInitial = false;
            } else {
                sb.append(':');
            }
            int k = b & 255;
            sb.append(HEXES[k >>> 4]);
            sb.append(HEXES[k & 15]);
        }
        return sb.-java_util_stream_Collectors-mthref-7();
    }
}
