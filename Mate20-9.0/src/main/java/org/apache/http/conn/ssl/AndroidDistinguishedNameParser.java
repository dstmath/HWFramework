package org.apache.http.conn.ssl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.security.auth.x500.X500Principal;

@Deprecated
final class AndroidDistinguishedNameParser {
    private int beg;
    private char[] chars;
    private int cur;
    private final String dn;
    private int end;
    private final int length = this.dn.length();
    private int pos;

    public AndroidDistinguishedNameParser(X500Principal principal) {
        this.dn = principal.getName("RFC2253");
    }

    private String nextAT() {
        while (this.pos < this.length && this.chars[this.pos] == ' ') {
            this.pos++;
        }
        if (this.pos == this.length) {
            return null;
        }
        this.beg = this.pos;
        this.pos++;
        while (this.pos < this.length && this.chars[this.pos] != '=' && this.chars[this.pos] != ' ') {
            this.pos++;
        }
        if (this.pos < this.length) {
            this.end = this.pos;
            if (this.chars[this.pos] == ' ') {
                while (this.pos < this.length && this.chars[this.pos] != '=' && this.chars[this.pos] == ' ') {
                    this.pos++;
                }
                if (this.chars[this.pos] != '=' || this.pos == this.length) {
                    throw new IllegalStateException("Unexpected end of DN: " + this.dn);
                }
            }
            this.pos++;
            while (this.pos < this.length && this.chars[this.pos] == ' ') {
                this.pos++;
            }
            if (this.end - this.beg > 4 && this.chars[this.beg + 3] == '.' && ((this.chars[this.beg] == 'O' || this.chars[this.beg] == 'o') && ((this.chars[this.beg + 1] == 'I' || this.chars[this.beg + 1] == 'i') && (this.chars[this.beg + 2] == 'D' || this.chars[this.beg + 2] == 'd')))) {
                this.beg += 4;
            }
            return new String(this.chars, this.beg, this.end - this.beg);
        }
        throw new IllegalStateException("Unexpected end of DN: " + this.dn);
    }

    private String quotedAV() {
        this.pos++;
        this.beg = this.pos;
        this.end = this.beg;
        while (this.pos != this.length) {
            if (this.chars[this.pos] == '\"') {
                this.pos++;
                while (this.pos < this.length && this.chars[this.pos] == ' ') {
                    this.pos++;
                }
                return new String(this.chars, this.beg, this.end - this.beg);
            }
            if (this.chars[this.pos] == '\\') {
                this.chars[this.end] = getEscaped();
            } else {
                this.chars[this.end] = this.chars[this.pos];
            }
            this.pos++;
            this.end++;
        }
        throw new IllegalStateException("Unexpected end of DN: " + this.dn);
    }

    private String hexAV() {
        if (this.pos + 4 < this.length) {
            this.beg = this.pos;
            this.pos++;
            while (true) {
                if (this.pos == this.length || this.chars[this.pos] == '+' || this.chars[this.pos] == ',' || this.chars[this.pos] == ';') {
                    this.end = this.pos;
                } else if (this.chars[this.pos] == ' ') {
                    this.end = this.pos;
                    this.pos++;
                    while (this.pos < this.length && this.chars[this.pos] == ' ') {
                        this.pos++;
                    }
                } else {
                    if (this.chars[this.pos] >= 'A' && this.chars[this.pos] <= 'F') {
                        char[] cArr = this.chars;
                        int i = this.pos;
                        cArr[i] = (char) (cArr[i] + ' ');
                    }
                    this.pos++;
                }
            }
            int hexLen = this.end - this.beg;
            if (hexLen < 5 || (hexLen & 1) == 0) {
                throw new IllegalStateException("Unexpected end of DN: " + this.dn);
            }
            byte[] encoded = new byte[(hexLen / 2)];
            int p = this.beg + 1;
            for (int i2 = 0; i2 < encoded.length; i2++) {
                encoded[i2] = (byte) getByte(p);
                p += 2;
            }
            return new String(this.chars, this.beg, hexLen);
        }
        throw new IllegalStateException("Unexpected end of DN: " + this.dn);
    }

    private String escapedAV() {
        this.beg = this.pos;
        this.end = this.pos;
        while (this.pos < this.length) {
            char c = this.chars[this.pos];
            if (c != ' ') {
                if (c != ';') {
                    if (c != '\\') {
                        switch (c) {
                            case '+':
                            case ',':
                                break;
                            default:
                                char[] cArr = this.chars;
                                int i = this.end;
                                this.end = i + 1;
                                cArr[i] = this.chars[this.pos];
                                this.pos++;
                                continue;
                        }
                    } else {
                        char[] cArr2 = this.chars;
                        int i2 = this.end;
                        this.end = i2 + 1;
                        cArr2[i2] = getEscaped();
                        this.pos++;
                    }
                }
                return new String(this.chars, this.beg, this.end - this.beg);
            }
            this.cur = this.end;
            this.pos++;
            char[] cArr3 = this.chars;
            int i3 = this.end;
            this.end = i3 + 1;
            cArr3[i3] = ' ';
            while (this.pos < this.length && this.chars[this.pos] == ' ') {
                char[] cArr4 = this.chars;
                int i4 = this.end;
                this.end = i4 + 1;
                cArr4[i4] = ' ';
                this.pos++;
            }
            if (this.pos == this.length || this.chars[this.pos] == ',' || this.chars[this.pos] == '+' || this.chars[this.pos] == ';') {
                return new String(this.chars, this.beg, this.cur - this.beg);
            }
        }
        return new String(this.chars, this.beg, this.end - this.beg);
    }

    private char getEscaped() {
        this.pos++;
        if (this.pos != this.length) {
            char c = this.chars[this.pos];
            if (!(c == ' ' || c == '%' || c == '\\' || c == '_')) {
                switch (c) {
                    case '\"':
                    case '#':
                        break;
                    default:
                        switch (c) {
                            case '*':
                            case '+':
                            case ',':
                                break;
                            default:
                                switch (c) {
                                    case ';':
                                    case '<':
                                    case '=':
                                    case '>':
                                        break;
                                    default:
                                        return getUTF8();
                                }
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
            if (this.pos == this.length || this.chars[this.pos] != '\\') {
                return '?';
            }
            this.pos++;
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
            char b12 = this.chars[position];
            if (b12 >= '0' && b12 <= '9') {
                b1 = b12 - '0';
            } else if (b12 >= 'a' && b12 <= 'f') {
                b1 = b12 - 'W';
            } else if (b12 < 'A' || b12 > 'F') {
                throw new IllegalStateException("Malformed DN: " + this.dn);
            } else {
                b1 = b12 - '7';
            }
            char b22 = this.chars[position + 1];
            if (b22 >= '0' && b22 <= '9') {
                b2 = b22 - '0';
            } else if (b22 >= 'a' && b22 <= 'f') {
                b2 = b22 - 'W';
            } else if (b22 < 'A' || b22 > 'F') {
                throw new IllegalStateException("Malformed DN: " + this.dn);
            } else {
                b2 = b22 - '7';
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
            if (this.pos == this.length) {
                return null;
            }
            switch (this.chars[this.pos]) {
                case '\"':
                    attValue = quotedAV();
                    break;
                case '#':
                    attValue = hexAV();
                    break;
                case '+':
                case ',':
                case ';':
                    break;
                default:
                    attValue = escapedAV();
                    break;
            }
            if (attributeType.equalsIgnoreCase(attType)) {
                return attValue;
            }
            if (this.pos >= this.length) {
                return null;
            }
            if (this.chars[this.pos] == ',' || this.chars[this.pos] == ';' || this.chars[this.pos] == '+') {
                this.pos++;
                attType = nextAT();
            } else {
                throw new IllegalStateException("Malformed DN: " + this.dn);
            }
        } while (attType != null);
        throw new IllegalStateException("Malformed DN: " + this.dn);
    }

    public List<String> getAllMostSpecificFirst(String attributeType) {
        this.pos = 0;
        this.beg = 0;
        this.end = 0;
        this.cur = 0;
        this.chars = this.dn.toCharArray();
        List<String> result = Collections.emptyList();
        String attType = nextAT();
        if (attType == null) {
            return result;
        }
        while (this.pos < this.length) {
            String attValue = "";
            switch (this.chars[this.pos]) {
                case '\"':
                    attValue = quotedAV();
                    break;
                case '#':
                    attValue = hexAV();
                    break;
                case '+':
                case ',':
                case ';':
                    break;
                default:
                    attValue = escapedAV();
                    break;
            }
            if (attributeType.equalsIgnoreCase(attType)) {
                if (result.isEmpty()) {
                    result = new ArrayList<>();
                }
                result.add(attValue);
            }
            if (this.pos >= this.length) {
                return result;
            }
            if (this.chars[this.pos] == ',' || this.chars[this.pos] == ';' || this.chars[this.pos] == '+') {
                this.pos++;
                attType = nextAT();
                if (attType == null) {
                    throw new IllegalStateException("Malformed DN: " + this.dn);
                }
            } else {
                throw new IllegalStateException("Malformed DN: " + this.dn);
            }
        }
        return result;
    }
}
