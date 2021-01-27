package com.huawei.okhttp3.internal.tls;

import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.internal.telephony.RILConstantsEx;
import javax.security.auth.x500.X500Principal;

@Deprecated
final class DistinguishedNameParser {
    private int beg;
    private char[] chars;
    private int cur;
    private final String dn;
    private int end;
    private final int length = this.dn.length();
    private int pos;

    DistinguishedNameParser(X500Principal principal) {
        this.dn = principal.getName("RFC2253");
    }

    private String nextAT() {
        while (true) {
            int i = this.pos;
            if (i >= this.length || this.chars[i] != ' ') {
                break;
            }
            this.pos = i + 1;
        }
        int i2 = this.pos;
        if (i2 == this.length) {
            return null;
        }
        this.beg = i2;
        this.pos = i2 + 1;
        while (true) {
            int i3 = this.pos;
            if (i3 >= this.length) {
                break;
            }
            char[] cArr = this.chars;
            if (cArr[i3] == '=' || cArr[i3] == ' ') {
                break;
            }
            this.pos = i3 + 1;
        }
        int i4 = this.pos;
        if (i4 < this.length) {
            this.end = i4;
            if (this.chars[i4] == ' ') {
                while (true) {
                    int i5 = this.pos;
                    if (i5 >= this.length) {
                        break;
                    }
                    char[] cArr2 = this.chars;
                    if (cArr2[i5] == '=' || cArr2[i5] != ' ') {
                        break;
                    }
                    this.pos = i5 + 1;
                }
                char[] cArr3 = this.chars;
                int i6 = this.pos;
                if (cArr3[i6] != '=' || i6 == this.length) {
                    throw new IllegalStateException("Unexpected end of DN: " + this.dn);
                }
            }
            this.pos++;
            while (true) {
                int i7 = this.pos;
                if (i7 >= this.length || this.chars[i7] != ' ') {
                    break;
                }
                this.pos = i7 + 1;
            }
            int i8 = this.end;
            int i9 = this.beg;
            if (i8 - i9 > 4) {
                char[] cArr4 = this.chars;
                if (cArr4[i9 + 3] == '.' && (cArr4[i9] == 'O' || cArr4[i9] == 'o')) {
                    char[] cArr5 = this.chars;
                    int i10 = this.beg;
                    if (cArr5[i10 + 1] == 'I' || cArr5[i10 + 1] == 'i') {
                        char[] cArr6 = this.chars;
                        int i11 = this.beg;
                        if (cArr6[i11 + 2] == 'D' || cArr6[i11 + 2] == 'd') {
                            this.beg += 4;
                        }
                    }
                }
            }
            char[] cArr7 = this.chars;
            int i12 = this.beg;
            return new String(cArr7, i12, this.end - i12);
        }
        throw new IllegalStateException("Unexpected end of DN: " + this.dn);
    }

    private String quotedAV() {
        this.pos++;
        this.beg = this.pos;
        this.end = this.beg;
        while (true) {
            int i = this.pos;
            if (i != this.length) {
                char[] cArr = this.chars;
                if (cArr[i] == '\"') {
                    this.pos = i + 1;
                    while (true) {
                        int i2 = this.pos;
                        if (i2 >= this.length || this.chars[i2] != ' ') {
                            break;
                        }
                        this.pos = i2 + 1;
                    }
                    char[] cArr2 = this.chars;
                    int i3 = this.beg;
                    return new String(cArr2, i3, this.end - i3);
                }
                if (cArr[i] == '\\') {
                    cArr[this.end] = getEscaped();
                } else {
                    cArr[this.end] = cArr[i];
                }
                this.pos++;
                this.end++;
            } else {
                throw new IllegalStateException("Unexpected end of DN: " + this.dn);
            }
        }
    }

    private String hexAV() {
        int i = this.pos;
        if (i + 4 < this.length) {
            this.beg = i;
            this.pos = i + 1;
            while (true) {
                int i2 = this.pos;
                if (i2 == this.length) {
                    break;
                }
                char[] cArr = this.chars;
                if (cArr[i2] == '+' || cArr[i2] == ',' || cArr[i2] == ';') {
                    break;
                } else if (cArr[i2] == ' ') {
                    this.end = i2;
                    this.pos = i2 + 1;
                    while (true) {
                        int i3 = this.pos;
                        if (i3 >= this.length || this.chars[i3] != ' ') {
                            break;
                        }
                        this.pos = i3 + 1;
                    }
                } else {
                    if (cArr[i2] >= 'A' && cArr[i2] <= 'F') {
                        cArr[i2] = (char) (cArr[i2] + ' ');
                    }
                    this.pos++;
                }
            }
            this.end = this.pos;
            int i4 = this.end;
            int i5 = this.beg;
            int hexLen = i4 - i5;
            if (hexLen < 5 || (hexLen & 1) == 0) {
                throw new IllegalStateException("Unexpected end of DN: " + this.dn);
            }
            byte[] encoded = new byte[(hexLen / 2)];
            int p = i5 + 1;
            for (int i6 = 0; i6 < encoded.length; i6++) {
                encoded[i6] = (byte) getByte(p);
                p += 2;
            }
            return new String(this.chars, this.beg, hexLen);
        }
        throw new IllegalStateException("Unexpected end of DN: " + this.dn);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x009b, code lost:
        r1 = r8.chars;
        r2 = r8.beg;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00a7, code lost:
        return new java.lang.String(r1, r2, r8.cur - r2);
     */
    private String escapedAV() {
        int i = this.pos;
        this.beg = i;
        this.end = i;
        while (true) {
            int i2 = this.pos;
            if (i2 < this.length) {
                char[] cArr = this.chars;
                char c = cArr[i2];
                if (c == ' ') {
                    int i3 = this.end;
                    this.cur = i3;
                    this.pos = i2 + 1;
                    this.end = i3 + 1;
                    cArr[i3] = ' ';
                    while (true) {
                        int i4 = this.pos;
                        if (i4 >= this.length) {
                            break;
                        }
                        char[] cArr2 = this.chars;
                        if (cArr2[i4] != ' ') {
                            break;
                        }
                        int i5 = this.end;
                        this.end = i5 + 1;
                        cArr2[i5] = ' ';
                        this.pos = i4 + 1;
                    }
                    int i6 = this.pos;
                    if (i6 == this.length) {
                        break;
                    }
                    char[] cArr3 = this.chars;
                    if (cArr3[i6] == ',' || cArr3[i6] == '+' || cArr3[i6] == ';') {
                        break;
                    }
                } else if (c == ';') {
                    break;
                } else if (c != '\\') {
                    if (c == '+' || c == ',') {
                        break;
                    }
                    int i7 = this.end;
                    this.end = i7 + 1;
                    cArr[i7] = cArr[i2];
                    this.pos = i2 + 1;
                } else {
                    int i8 = this.end;
                    this.end = i8 + 1;
                    cArr[i8] = getEscaped();
                    this.pos++;
                }
            } else {
                char[] cArr4 = this.chars;
                int i9 = this.beg;
                return new String(cArr4, i9, this.end - i9);
            }
        }
        char[] cArr5 = this.chars;
        int i10 = this.beg;
        return new String(cArr5, i10, this.end - i10);
    }

    private char getEscaped() {
        this.pos++;
        int i = this.pos;
        if (i != this.length) {
            char c = this.chars[i];
            if (!(c == ' ' || c == '%' || c == '\\' || c == '_' || c == '\"' || c == '#')) {
                switch (c) {
                    case '*':
                    case AppOpsManagerEx.OP_GET_USAGE_STATS /* 43 */:
                    case ',':
                        break;
                    default:
                        switch (c) {
                            case ';':
                            case '<':
                            case RILConstantsEx.NETWORK_MODE_CDMA_HDR_GSM_WCDMA_LTEFDD /* 61 */:
                            case '>':
                                break;
                            default:
                                return getUTF8();
                        }
                }
            }
            return this.chars[this.pos];
        }
        throw new IllegalStateException("Unexpected end of DN: " + this.dn);
    }

    private char getUTF8() {
        int count;
        int res;
        int res2 = getByte(this.pos);
        this.pos++;
        if (res2 < 128) {
            return (char) res2;
        }
        if (res2 < 192 || res2 > 247) {
            return '?';
        }
        if (res2 <= 223) {
            count = 1;
            res = res2 & 31;
        } else if (res2 <= 239) {
            count = 2;
            res = res2 & 15;
        } else {
            count = 3;
            res = res2 & 7;
        }
        for (int i = 0; i < count; i++) {
            this.pos++;
            int i2 = this.pos;
            if (i2 == this.length || this.chars[i2] != '\\') {
                return '?';
            }
            this.pos = i2 + 1;
            int b = getByte(this.pos);
            this.pos++;
            if ((b & 192) != 128) {
                return '?';
            }
            res = (res << 6) + (b & 63);
        }
        return (char) res;
    }

    private int getByte(int position) {
        int b1;
        int b2;
        if (position + 1 < this.length) {
            char c = this.chars[position];
            if (c >= '0' && c <= '9') {
                b1 = c - '0';
            } else if (c >= 'a' && c <= 'f') {
                b1 = c - 'W';
            } else if (c < 'A' || c > 'F') {
                throw new IllegalStateException("Malformed DN: " + this.dn);
            } else {
                b1 = c - '7';
            }
            char c2 = this.chars[position + 1];
            if (c2 >= '0' && c2 <= '9') {
                b2 = c2 - '0';
            } else if (c2 >= 'a' && c2 <= 'f') {
                b2 = c2 - 'W';
            } else if (c2 < 'A' || c2 > 'F') {
                throw new IllegalStateException("Malformed DN: " + this.dn);
            } else {
                b2 = c2 - '7';
            }
            return (b1 << 4) + b2;
        }
        throw new IllegalStateException("Malformed DN: " + this.dn);
    }

    public String findMostSpecific(String attributeType) {
        this.pos = 0;
        this.beg = 0;
        this.end = 0;
        this.cur = 0;
        this.chars = this.dn.toCharArray();
        String attType = nextAT();
        if (attType == null) {
            return null;
        }
        do {
            String attValue = "";
            int i = this.pos;
            if (i == this.length) {
                return null;
            }
            char c = this.chars[i];
            if (c == '\"') {
                attValue = quotedAV();
            } else if (c == '#') {
                attValue = hexAV();
            } else if (!(c == '+' || c == ',' || c == ';')) {
                attValue = escapedAV();
            }
            if (attributeType.equalsIgnoreCase(attType)) {
                return attValue;
            }
            int i2 = this.pos;
            if (i2 >= this.length) {
                return null;
            }
            char[] cArr = this.chars;
            if (cArr[i2] == ',' || cArr[i2] == ';' || cArr[i2] == '+') {
                this.pos++;
                attType = nextAT();
            } else {
                throw new IllegalStateException("Malformed DN: " + this.dn);
            }
        } while (attType != null);
        throw new IllegalStateException("Malformed DN: " + this.dn);
    }
}
