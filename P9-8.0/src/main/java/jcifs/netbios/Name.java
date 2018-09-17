package jcifs.netbios;

import java.io.UnsupportedEncodingException;
import jcifs.Config;
import jcifs.util.Hexdump;

public class Name {
    private static final String DEFAULT_SCOPE = Config.getProperty("jcifs.netbios.scope");
    static final String OEM_ENCODING = Config.getProperty("jcifs.encoding", System.getProperty("file.encoding"));
    private static final int SCOPE_OFFSET = 33;
    private static final int TYPE_OFFSET = 31;
    public int hexCode;
    public String name;
    public String scope;
    int srcHashCode;

    Name() {
    }

    public Name(String name, int hexCode, String scope) {
        if (name.length() > 15) {
            name = name.substring(0, 15);
        }
        this.name = name.toUpperCase();
        this.hexCode = hexCode;
        if (scope == null || scope.length() <= 0) {
            scope = DEFAULT_SCOPE;
        }
        this.scope = scope;
        this.srcHashCode = 0;
    }

    int writeWireFormat(byte[] dst, int dstIndex) {
        dst[dstIndex] = (byte) 32;
        try {
            byte[] tmp = this.name.getBytes(OEM_ENCODING);
            int i = 0;
            while (i < tmp.length) {
                dst[((i * 2) + 1) + dstIndex] = (byte) (((tmp[i] & 240) >> 4) + 65);
                dst[((i * 2) + 2) + dstIndex] = (byte) ((tmp[i] & 15) + 65);
                i++;
            }
            while (i < 15) {
                dst[((i * 2) + 1) + dstIndex] = (byte) 67;
                dst[((i * 2) + 2) + dstIndex] = (byte) 65;
                i++;
            }
            dst[dstIndex + TYPE_OFFSET] = (byte) (((this.hexCode & 240) >> 4) + 65);
            dst[(dstIndex + TYPE_OFFSET) + 1] = (byte) ((this.hexCode & 15) + 65);
        } catch (UnsupportedEncodingException e) {
        }
        return writeScopeWireFormat(dst, dstIndex + SCOPE_OFFSET) + SCOPE_OFFSET;
    }

    int readWireFormat(byte[] src, int srcIndex) {
        byte[] tmp = new byte[SCOPE_OFFSET];
        int length = 15;
        for (int i = 0; i < 15; i++) {
            tmp[i] = (byte) (((src[((i * 2) + 1) + srcIndex] & 255) - 65) << 4);
            tmp[i] = (byte) (tmp[i] | ((byte) (((src[((i * 2) + 2) + srcIndex] & 255) - 65) & 15)));
            if (tmp[i] != (byte) 32) {
                length = i + 1;
            }
        }
        try {
            this.name = new String(tmp, 0, length, OEM_ENCODING);
        } catch (UnsupportedEncodingException e) {
        }
        this.hexCode = ((src[srcIndex + TYPE_OFFSET] & 255) - 65) << 4;
        this.hexCode |= ((src[(srcIndex + TYPE_OFFSET) + 1] & 255) - 65) & 15;
        return readScopeWireFormat(src, srcIndex + SCOPE_OFFSET) + SCOPE_OFFSET;
    }

    int writeScopeWireFormat(byte[] dst, int dstIndex) {
        if (this.scope == null) {
            dst[dstIndex] = (byte) 0;
            return 1;
        }
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) 46;
        try {
            System.arraycopy(this.scope.getBytes(OEM_ENCODING), 0, dst, dstIndex2, this.scope.length());
        } catch (UnsupportedEncodingException e) {
        }
        dstIndex = dstIndex2 + this.scope.length();
        dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) 0;
        int i = dstIndex2 - 2;
        int e2 = i - this.scope.length();
        int c = 0;
        while (true) {
            if (dst[i] == (byte) 46) {
                dst[i] = (byte) c;
                c = 0;
            } else {
                c++;
            }
            int i2 = i - 1;
            if (i <= e2) {
                dstIndex = dstIndex2;
                return this.scope.length() + 2;
            }
            i = i2;
        }
    }

    int readScopeWireFormat(byte[] src, int srcIndex) {
        int start = srcIndex;
        int srcIndex2 = srcIndex + 1;
        int n = src[srcIndex] & 255;
        if (n == 0) {
            this.scope = null;
            srcIndex = srcIndex2;
            return 1;
        }
        try {
            StringBuffer sb = new StringBuffer(new String(src, srcIndex2, n, OEM_ENCODING));
            srcIndex = srcIndex2 + n;
            while (true) {
                srcIndex2 = srcIndex;
                srcIndex = srcIndex2 + 1;
                try {
                    n = src[srcIndex2] & 255;
                    if (n == 0) {
                        break;
                    }
                    sb.append('.').append(new String(src, srcIndex, n, OEM_ENCODING));
                    srcIndex += n;
                } catch (UnsupportedEncodingException e) {
                }
            }
            this.scope = sb.toString();
        } catch (UnsupportedEncodingException e2) {
            srcIndex = srcIndex2;
        }
        return srcIndex - start;
    }

    public int hashCode() {
        int result = (this.name.hashCode() + (this.hexCode * 65599)) + (this.srcHashCode * 65599);
        if (this.scope == null || this.scope.length() == 0) {
            return result;
        }
        return result + this.scope.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Name)) {
            return false;
        }
        Name n = (Name) obj;
        if (this.scope == null && n.scope == null) {
            if (this.name.equals(n.name) && this.hexCode == n.hexCode) {
                return true;
            }
            return false;
        } else if (this.name.equals(n.name) && this.hexCode == n.hexCode && this.scope.equals(n.scope)) {
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        String n = this.name;
        if (n == null) {
            n = "null";
        } else if (n.charAt(0) == 1) {
            char[] c = n.toCharArray();
            c[0] = '.';
            c[1] = '.';
            c[14] = '.';
            n = new String(c);
        }
        sb.append(n).append("<").append(Hexdump.toHexString(this.hexCode, 2)).append(">");
        if (this.scope != null) {
            sb.append(".").append(this.scope);
        }
        return sb.toString();
    }
}
