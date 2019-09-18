package sun.net.util;

public class IPAddressUtil {
    private static final int INADDR16SZ = 16;
    private static final int INADDR4SZ = 4;
    private static final int INT16SZ = 2;

    public static byte[] textToNumericFormatV4(String src) {
        byte[] res = new byte[4];
        boolean newOctet = true;
        int len = src.length();
        if (len == 0 || len > 15) {
            String str = src;
            return null;
        }
        int currByte = 0;
        long tmpValue = 0;
        for (int i = 0; i < len; i++) {
            char c = src.charAt(i);
            if (c != '.') {
                int digit = Character.digit(c, 10);
                if (digit < 0) {
                    return null;
                }
                tmpValue = (tmpValue * 10) + ((long) digit);
                newOctet = false;
            } else if (newOctet || tmpValue < 0 || tmpValue > 255 || currByte == 3) {
                return null;
            } else {
                res[currByte] = (byte) ((int) (tmpValue & 255));
                tmpValue = 0;
                newOctet = true;
                currByte++;
            }
        }
        String str2 = src;
        if (newOctet || tmpValue < 0 || tmpValue >= (1 << ((4 - currByte) * 8))) {
            return null;
        }
        switch (currByte) {
            case 0:
            case 1:
            case 2:
                return null;
            case 3:
                res[3] = (byte) ((int) ((tmpValue >> 0) & 255));
                break;
        }
        return res;
    }

    public static byte[] textToNumericFormatV6(String src) {
        int i;
        byte[] bArr;
        String str = src;
        if (src.length() < 2) {
            return null;
        }
        char[] srcb = src.toCharArray();
        int i2 = 16;
        byte[] dst = new byte[16];
        int srcb_length = srcb.length;
        int pc = str.indexOf("%");
        if (pc == srcb_length - 1) {
            return null;
        }
        int i3 = -1;
        if (pc != -1) {
            srcb_length = pc;
        }
        int i4 = 0;
        if (srcb[0] == ':') {
            i4 = 0 + 1;
            if (srcb[i4] != ':') {
                return null;
            }
        }
        boolean saw_xdigit = false;
        int curtok = i;
        int curtok2 = 0;
        int colonp = -1;
        int val = 0;
        while (true) {
            if (i >= srcb_length) {
                bArr = null;
                int i5 = i;
                break;
            }
            int i6 = i + 1;
            char i7 = srcb[i];
            int chval = Character.digit(i7, i2);
            if (chval != i3) {
                val = (val << 4) | chval;
                if (val > 65535) {
                    return null;
                }
                saw_xdigit = true;
                i = i6;
                i2 = 16;
            } else if (i7 == ':') {
                curtok = i6;
                if (!saw_xdigit) {
                    if (colonp != i3) {
                        return null;
                    }
                    colonp = curtok2;
                    i = i6;
                    i2 = 16;
                } else if (i6 == srcb_length || curtok2 + 2 > 16) {
                    return null;
                } else {
                    int j = curtok2 + 1;
                    dst[curtok2] = (byte) ((val >> 8) & 255);
                    curtok2 = j + 1;
                    dst[j] = (byte) (val & 255);
                    saw_xdigit = false;
                    val = 0;
                    i = i6;
                    i2 = 16;
                    i3 = -1;
                }
            } else {
                int i8 = 46;
                if (i7 != '.' || curtok2 + 4 > 16) {
                    return null;
                }
                String ia4 = str.substring(curtok, srcb_length);
                int dot_count = 0;
                int index = 0;
                while (true) {
                    int indexOf = ia4.indexOf(i8, index);
                    int index2 = indexOf;
                    if (indexOf == -1) {
                        break;
                    }
                    dot_count++;
                    index = index2 + 1;
                    String str2 = src;
                    i8 = 46;
                }
                int j2 = dot_count;
                if (j2 != 3) {
                    return null;
                }
                byte[] v4addr = textToNumericFormatV4(ia4);
                if (v4addr == null) {
                    return null;
                }
                int k = 0;
                while (true) {
                    int dot_count2 = j2;
                    if (k >= 4) {
                        break;
                    }
                    dst[curtok2] = v4addr[k];
                    k++;
                    curtok2++;
                    j2 = dot_count2;
                }
                saw_xdigit = false;
                bArr = null;
            }
        }
        if (saw_xdigit) {
            if (curtok2 + 2 > 16) {
                return bArr;
            }
            int j3 = curtok2 + 1;
            dst[curtok2] = (byte) ((val >> 8) & 255);
            curtok2 = j3 + 1;
            dst[j3] = (byte) (val & 255);
        }
        if (colonp != -1) {
            int n = curtok2 - colonp;
            if (curtok2 == 16) {
                return null;
            }
            for (int i9 = 1; i9 <= n; i9++) {
                dst[16 - i9] = dst[(colonp + n) - i9];
                dst[(colonp + n) - i9] = 0;
            }
            curtok2 = 16;
        }
        if (curtok2 != 16) {
            return null;
        }
        byte[] newdst = convertFromIPv4MappedAddress(dst);
        if (newdst != null) {
            return newdst;
        }
        return dst;
    }

    public static boolean isIPv4LiteralAddress(String src) {
        return textToNumericFormatV4(src) != null;
    }

    public static boolean isIPv6LiteralAddress(String src) {
        return textToNumericFormatV6(src) != null;
    }

    public static byte[] convertFromIPv4MappedAddress(byte[] addr) {
        if (!isIPv4MappedAddress(addr)) {
            return null;
        }
        byte[] newAddr = new byte[4];
        System.arraycopy(addr, 12, newAddr, 0, 4);
        return newAddr;
    }

    private static boolean isIPv4MappedAddress(byte[] addr) {
        if (addr.length >= 16 && addr[0] == 0 && addr[1] == 0 && addr[2] == 0 && addr[3] == 0 && addr[4] == 0 && addr[5] == 0 && addr[6] == 0 && addr[7] == 0 && addr[8] == 0 && addr[9] == 0 && addr[10] == -1 && addr[11] == -1) {
            return true;
        }
        return false;
    }
}
