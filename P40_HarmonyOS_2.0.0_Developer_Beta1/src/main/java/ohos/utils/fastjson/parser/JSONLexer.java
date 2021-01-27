package ohos.utils.fastjson.parser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import ohos.media.camera.params.adapter.InnerMetadata;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONException;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

public final class JSONLexer {
    public static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    public static final int END = 4;
    public static final char EOI = 26;
    static final int[] IA = new int[256];
    public static final int NOT_MATCH = -1;
    public static final int NOT_MATCH_NAME = -2;
    public static final int UNKNOWN = 0;
    private static boolean V6 = false;
    public static final int VALUE = 3;
    protected static final int[] digits = new int[103];
    public static final boolean[] firstIdentifierFlags = new boolean[256];
    public static final boolean[] identifierFlags = new boolean[256];
    private static final ThreadLocal<char[]> sbufLocal = new ThreadLocal<>();
    protected int bp;
    public Calendar calendar;
    protected char ch;
    public boolean disableCircularReferenceDetect;
    protected int eofPos;
    protected boolean exp;
    public int features;
    protected long fieldHash;
    protected boolean hasSpecial;
    protected boolean isDouble;
    protected final int len;
    public Locale locale;
    public int matchStat;
    protected int np;
    protected int pos;
    protected char[] sbuf;
    protected int sp;
    protected String stringDefaultValue;
    protected final String text;
    public TimeZone timeZone;
    protected int token;

    static boolean checkDate(char c, char c2, char c3, char c4, char c5, char c6, int i, int i2) {
        if (c >= '1' && c <= '3' && c2 >= '0' && c2 <= '9' && c3 >= '0' && c3 <= '9' && c4 >= '0' && c4 <= '9') {
            if (c5 == '0') {
                if (c6 < '1' || c6 > '9') {
                    return false;
                }
            } else if (!(c5 == '1' && (c6 == '0' || c6 == '1' || c6 == '2'))) {
                return false;
            }
            if (i == 48) {
                return i2 >= 49 && i2 <= 57;
            }
            if (i != 49 && i != 50) {
                return i == 51 && (i2 == 48 || i2 == 49);
            }
            if (i2 >= 48 && i2 <= 57) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001d, code lost:
        if (r5 <= '4') goto L_0x0020;
     */
    static boolean checkTime(char c, char c2, char c3, char c4, char c5, char c6) {
        if (c == '0') {
            if (c2 < '0' || c2 > '9') {
                return false;
            }
        } else if (c != '1') {
            if (c == '2') {
                if (c2 >= '0') {
                }
            }
            return false;
        } else if (c2 < '0' || c2 > '9') {
            return false;
        }
        if (c3 < '0' || c3 > '5') {
            if (!(c3 == '6' && c4 == '0')) {
                return false;
            }
        } else if (c4 < '0' || c4 > '9') {
            return false;
        }
        return (c5 < '0' || c5 > '5') ? c5 == '6' && c6 == '0' : c6 >= '0' && c6 <= '9';
    }

    static {
        int i;
        try {
            i = Class.forName("android.os.Build$VERSION").getField("SDK_INT").getInt(null);
        } catch (Exception unused) {
            i = -1;
        }
        char c = 0;
        V6 = i >= 23;
        for (int i2 = 48; i2 <= 57; i2++) {
            digits[i2] = i2 - 48;
        }
        for (int i3 = 97; i3 <= 102; i3++) {
            digits[i3] = (i3 - 97) + 10;
        }
        for (int i4 = 65; i4 <= 70; i4++) {
            digits[i4] = (i4 - 65) + 10;
        }
        Arrays.fill(IA, -1);
        int length = CA.length;
        for (int i5 = 0; i5 < length; i5++) {
            IA[CA[i5]] = i5;
        }
        IA[61] = 0;
        char c2 = 0;
        while (true) {
            boolean[] zArr = firstIdentifierFlags;
            if (c2 >= zArr.length) {
                break;
            }
            if (c2 >= 'A' && c2 <= 'Z') {
                zArr[c2] = true;
            } else if (c2 >= 'a' && c2 <= 'z') {
                firstIdentifierFlags[c2] = true;
            } else if (c2 == '_') {
                firstIdentifierFlags[c2] = true;
            }
            c2 = (char) (c2 + 1);
        }
        while (true) {
            boolean[] zArr2 = identifierFlags;
            if (c < zArr2.length) {
                if (c >= 'A' && c <= 'Z') {
                    zArr2[c] = true;
                } else if (c >= 'a' && c <= 'z') {
                    identifierFlags[c] = true;
                } else if (c == '_') {
                    identifierFlags[c] = true;
                } else if (c >= '0' && c <= '9') {
                    identifierFlags[c] = true;
                }
                c = (char) (c + 1);
            } else {
                return;
            }
        }
    }

    public JSONLexer(String str) {
        this(str, JSON.DEFAULT_PARSER_FEATURE);
    }

    public JSONLexer(char[] cArr, int i) {
        this(cArr, i, JSON.DEFAULT_PARSER_FEATURE);
    }

    public JSONLexer(char[] cArr, int i, int i2) {
        this(new String(cArr, 0, i), i2);
    }

    public JSONLexer(String str, int i) {
        char c;
        this.features = JSON.DEFAULT_PARSER_FEATURE;
        boolean z = false;
        this.exp = false;
        this.isDouble = false;
        this.timeZone = JSON.defaultTimeZone;
        this.locale = JSON.defaultLocale;
        String str2 = null;
        this.calendar = null;
        this.matchStat = 0;
        this.sbuf = sbufLocal.get();
        if (this.sbuf == null) {
            this.sbuf = new char[512];
        }
        this.features = i;
        this.text = str;
        this.len = this.text.length();
        this.bp = -1;
        int i2 = this.bp + 1;
        this.bp = i2;
        if (i2 >= this.len) {
            c = EOI;
        } else {
            c = this.text.charAt(i2);
        }
        this.ch = c;
        if (this.ch == 65279) {
            next();
        }
        this.stringDefaultValue = (Feature.InitStringFieldAsEmpty.mask & i) != 0 ? "" : str2;
        this.disableCircularReferenceDetect = (Feature.DisableCircularReferenceDetect.mask & i) != 0 ? true : z;
    }

    public final int token() {
        return this.token;
    }

    public void close() {
        char[] cArr = this.sbuf;
        if (cArr.length <= 8196) {
            sbufLocal.set(cArr);
        }
        this.sbuf = null;
    }

    public char next() {
        char c;
        int i = this.bp + 1;
        this.bp = i;
        if (i >= this.len) {
            c = EOI;
        } else {
            c = this.text.charAt(i);
        }
        this.ch = c;
        return c;
    }

    public final void config(Feature feature, boolean z) {
        if (z) {
            this.features |= feature.mask;
        } else {
            this.features &= ~feature.mask;
        }
        if (feature == Feature.InitStringFieldAsEmpty) {
            this.stringDefaultValue = z ? "" : null;
        }
        this.disableCircularReferenceDetect = (this.features & Feature.DisableCircularReferenceDetect.mask) != 0;
    }

    public final boolean isEnabled(Feature feature) {
        return (this.features & feature.mask) != 0;
    }

    public final void nextTokenWithChar(char c) {
        char c2;
        this.sp = 0;
        while (true) {
            char c3 = this.ch;
            if (c3 == c) {
                int i = this.bp + 1;
                this.bp = i;
                if (i >= this.len) {
                    c2 = EOI;
                } else {
                    c2 = this.text.charAt(i);
                }
                this.ch = c2;
                nextToken();
                return;
            } else if (c3 == ' ' || c3 == '\n' || c3 == '\r' || c3 == '\t' || c3 == '\f' || c3 == '\b') {
                next();
            } else {
                throw new JSONException("not match " + c + " - " + this.ch);
            }
        }
    }

    public final String numberString() {
        char charAt = this.text.charAt((this.np + this.sp) - 1);
        int i = this.sp;
        if (charAt == 'L' || charAt == 'S' || charAt == 'B' || charAt == 'F' || charAt == 'D') {
            i--;
        }
        return subString(this.np, i);
    }

    /* access modifiers changed from: protected */
    public char charAt(int i) {
        if (i >= this.len) {
            return EOI;
        }
        return this.text.charAt(i);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0027, code lost:
        scanNumber();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002a, code lost:
        return;
     */
    public final void nextToken() {
        char c;
        int i = 0;
        this.sp = 0;
        while (true) {
            this.pos = this.bp;
            char c2 = this.ch;
            if (c2 == '/') {
                skipComment();
            } else if (c2 == '\"') {
                scanString();
                return;
            } else if ((c2 < '0' || c2 > '9') && (c = this.ch) != '-') {
                if (c == ',') {
                    next();
                    this.token = 16;
                    return;
                }
                if (!(c == '\f' || c == '\r' || c == ' ')) {
                    if (c != ':') {
                        char c3 = EOI;
                        if (c == '[') {
                            int i2 = this.bp + 1;
                            this.bp = i2;
                            if (i2 < this.len) {
                                c3 = this.text.charAt(i2);
                            }
                            this.ch = c3;
                            this.token = 14;
                            return;
                        } else if (c == ']') {
                            next();
                            this.token = 15;
                            return;
                        } else if (c == 'f') {
                            if (this.text.startsWith("false", this.bp)) {
                                this.bp += 5;
                                this.ch = charAt(this.bp);
                                char c4 = this.ch;
                                if (c4 == ' ' || c4 == ',' || c4 == '}' || c4 == ']' || c4 == '\n' || c4 == '\r' || c4 == '\t' || c4 == 26 || c4 == '\f' || c4 == '\b' || c4 == ':') {
                                    this.token = 7;
                                    return;
                                }
                            }
                            throw new JSONException("scan false error");
                        } else if (c == 'n') {
                            if (this.text.startsWith("null", this.bp)) {
                                this.bp += 4;
                                i = 8;
                            } else if (this.text.startsWith("new", this.bp)) {
                                this.bp += 3;
                                i = 9;
                            }
                            if (i != 0) {
                                this.ch = charAt(this.bp);
                                char c5 = this.ch;
                                if (c5 == ' ' || c5 == ',' || c5 == '}' || c5 == ']' || c5 == '\n' || c5 == '\r' || c5 == '\t' || c5 == 26 || c5 == '\f' || c5 == '\b') {
                                    this.token = i;
                                    return;
                                }
                            }
                            throw new JSONException("scan null/new error");
                        } else if (c == '{') {
                            int i3 = this.bp + 1;
                            this.bp = i3;
                            if (i3 < this.len) {
                                c3 = this.text.charAt(i3);
                            }
                            this.ch = c3;
                            this.token = 12;
                            return;
                        } else if (c == '}') {
                            int i4 = this.bp + 1;
                            this.bp = i4;
                            if (i4 < this.len) {
                                c3 = this.text.charAt(i4);
                            }
                            this.ch = c3;
                            this.token = 13;
                            return;
                        } else if (!(c == 'S' || c == 'T')) {
                            if (c == 't') {
                                if (this.text.startsWith("true", this.bp)) {
                                    this.bp += 4;
                                    this.ch = charAt(this.bp);
                                    char c6 = this.ch;
                                    if (c6 == ' ' || c6 == ',' || c6 == '}' || c6 == ']' || c6 == '\n' || c6 == '\r' || c6 == '\t' || c6 == 26 || c6 == '\f' || c6 == '\b' || c6 == ':') {
                                        this.token = 6;
                                        return;
                                    }
                                }
                                throw new JSONException("scan true error");
                            } else if (c != 'u') {
                                switch (c) {
                                    case '\b':
                                    case '\t':
                                    case '\n':
                                        break;
                                    default:
                                        switch (c) {
                                            case '\'':
                                                scanString();
                                                return;
                                            case '(':
                                                next();
                                                this.token = 10;
                                                return;
                                            case ')':
                                                next();
                                                this.token = 11;
                                                return;
                                            default:
                                                int i5 = this.bp;
                                                int i6 = this.len;
                                                if (!(i5 == i6 || (c == 26 && i5 + 1 == i6))) {
                                                    char c7 = this.ch;
                                                    if (c7 <= 31 || c7 == 127) {
                                                        next();
                                                        continue;
                                                    } else {
                                                        this.token = 1;
                                                        next();
                                                        return;
                                                    }
                                                } else if (this.token != 20) {
                                                    this.token = 20;
                                                    int i7 = this.eofPos;
                                                    this.bp = i7;
                                                    this.pos = i7;
                                                    return;
                                                } else {
                                                    throw new JSONException("EOF error");
                                                }
                                        }
                                }
                            }
                        }
                    } else {
                        next();
                        this.token = 17;
                        return;
                    }
                }
                next();
            }
        }
        scanIdent();
    }

    public final void nextToken(int i) {
        this.sp = 0;
        while (true) {
            if (i != 2) {
                char c = EOI;
                if (i == 4) {
                    char c2 = this.ch;
                    if (c2 == '\"') {
                        this.pos = this.bp;
                        scanString();
                        return;
                    } else if (c2 >= '0' && c2 <= '9') {
                        this.pos = this.bp;
                        scanNumber();
                        return;
                    } else if (this.ch == '{') {
                        this.token = 12;
                        int i2 = this.bp + 1;
                        this.bp = i2;
                        if (i2 < this.len) {
                            c = this.text.charAt(i2);
                        }
                        this.ch = c;
                        return;
                    }
                } else if (i == 12) {
                    char c3 = this.ch;
                    if (c3 == '{') {
                        this.token = 12;
                        int i3 = this.bp + 1;
                        this.bp = i3;
                        if (i3 < this.len) {
                            c = this.text.charAt(i3);
                        }
                        this.ch = c;
                        return;
                    } else if (c3 == '[') {
                        this.token = 14;
                        int i4 = this.bp + 1;
                        this.bp = i4;
                        if (i4 < this.len) {
                            c = this.text.charAt(i4);
                        }
                        this.ch = c;
                        return;
                    }
                } else if (i != 18) {
                    if (i != 20) {
                        switch (i) {
                            case 14:
                                char c4 = this.ch;
                                if (c4 == '[') {
                                    this.token = 14;
                                    next();
                                    return;
                                } else if (c4 == '{') {
                                    this.token = 12;
                                    next();
                                    return;
                                }
                                break;
                            case 15:
                                if (this.ch == ']') {
                                    this.token = 15;
                                    next();
                                    return;
                                }
                                break;
                            case 16:
                                char c5 = this.ch;
                                if (c5 == ',') {
                                    this.token = 16;
                                    int i5 = this.bp + 1;
                                    this.bp = i5;
                                    if (i5 < this.len) {
                                        c = this.text.charAt(i5);
                                    }
                                    this.ch = c;
                                    return;
                                } else if (c5 == '}') {
                                    this.token = 13;
                                    int i6 = this.bp + 1;
                                    this.bp = i6;
                                    if (i6 < this.len) {
                                        c = this.text.charAt(i6);
                                    }
                                    this.ch = c;
                                    return;
                                } else if (c5 == ']') {
                                    this.token = 15;
                                    int i7 = this.bp + 1;
                                    this.bp = i7;
                                    if (i7 < this.len) {
                                        c = this.text.charAt(i7);
                                    }
                                    this.ch = c;
                                    return;
                                } else if (c5 == 26) {
                                    this.token = 20;
                                    return;
                                }
                                break;
                        }
                    }
                    if (this.ch == 26) {
                        this.token = 20;
                        return;
                    }
                } else {
                    nextIdent();
                    return;
                }
            } else {
                char c6 = this.ch;
                if (c6 < '0' || c6 > '9') {
                    char c7 = this.ch;
                    if (c7 == '\"') {
                        this.pos = this.bp;
                        scanString();
                        return;
                    } else if (c7 == '[') {
                        this.token = 14;
                        next();
                        return;
                    } else if (c7 == '{') {
                        this.token = 12;
                        next();
                        return;
                    }
                } else {
                    this.pos = this.bp;
                    scanNumber();
                    return;
                }
            }
            char c8 = this.ch;
            if (c8 == ' ' || c8 == '\n' || c8 == '\r' || c8 == '\t' || c8 == '\f' || c8 == '\b') {
                next();
            } else {
                nextToken();
                return;
            }
        }
    }

    public final void nextIdent() {
        while (true) {
            char c = this.ch;
            if (!(c <= ' ' && (c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == '\f' || c == '\b'))) {
                break;
            }
            next();
        }
        char c2 = this.ch;
        if (c2 == '_' || Character.isLetter(c2)) {
            scanIdent();
        } else {
            nextToken();
        }
    }

    public final Number integerValue() throws NumberFormatException {
        char c;
        char c2;
        char c3;
        boolean z;
        long j;
        long j2;
        char c4;
        char c5;
        int i = this.np;
        int i2 = this.sp + i;
        int i3 = i2 - 1;
        if (i3 >= this.len) {
            c = EOI;
        } else {
            c = this.text.charAt(i3);
        }
        if (c == 'B') {
            i2--;
            c2 = 'B';
        } else if (c == 'L') {
            i2--;
            c2 = 'L';
        } else if (c != 'S') {
            c2 = ' ';
        } else {
            i2--;
            c2 = 'S';
        }
        int i4 = this.np;
        if (i4 >= this.len) {
            c3 = EOI;
        } else {
            c3 = this.text.charAt(i4);
        }
        if (c3 == '-') {
            j = Long.MIN_VALUE;
            i++;
            z = true;
        } else {
            j = -9223372036854775807L;
            z = false;
        }
        if (i < i2) {
            int i5 = i + 1;
            if (i >= this.len) {
                c5 = EOI;
            } else {
                c5 = this.text.charAt(i);
            }
            j2 = (long) (-(c5 - '0'));
            i = i5;
        } else {
            j2 = 0;
        }
        while (i < i2) {
            int i6 = i + 1;
            if (i >= this.len) {
                c4 = EOI;
            } else {
                c4 = this.text.charAt(i);
            }
            int i7 = c4 - '0';
            if (j2 < -922337203685477580L) {
                return new BigInteger(numberString());
            }
            long j3 = j2 * 10;
            long j4 = (long) i7;
            if (j3 < j + j4) {
                return new BigInteger(numberString());
            }
            j2 = j3 - j4;
            i = i6;
        }
        if (!z) {
            long j5 = -j2;
            if (j5 > 2147483647L || c2 == 'L') {
                return Long.valueOf(j5);
            }
            if (c2 == 'S') {
                return Short.valueOf((short) ((int) j5));
            }
            if (c2 == 'B') {
                return Byte.valueOf((byte) ((int) j5));
            }
            return Integer.valueOf((int) j5);
        } else if (i <= this.np + 1) {
            throw new NumberFormatException(numberString());
        } else if (j2 < -2147483648L || c2 == 'L') {
            return Long.valueOf(j2);
        } else {
            if (c2 == 'S') {
                return Short.valueOf((short) ((int) j2));
            }
            if (c2 == 'B') {
                return Byte.valueOf((byte) ((int) j2));
            }
            return Integer.valueOf((int) j2);
        }
    }

    public final String scanSymbol(SymbolTable symbolTable) {
        char c;
        while (true) {
            c = this.ch;
            if (c != ' ' && c != '\n' && c != '\r' && c != '\t' && c != '\f' && c != '\b') {
                break;
            }
            next();
        }
        if (c == '\"') {
            return scanSymbol(symbolTable, '\"');
        }
        if (c == '\'') {
            return scanSymbol(symbolTable, '\'');
        }
        if (c == '}') {
            next();
            this.token = 13;
            return null;
        } else if (c == ',') {
            next();
            this.token = 16;
            return null;
        } else if (c != 26) {
            return scanSymbolUnQuoted(symbolTable);
        } else {
            this.token = 20;
            return null;
        }
    }

    public String scanSymbol(SymbolTable symbolTable, char c) {
        String str;
        char c2;
        int i = this.bp + 1;
        int indexOf = this.text.indexOf(c, i);
        if (indexOf != -1) {
            int i2 = indexOf - i;
            char[] sub_chars = sub_chars(this.bp + 1, i2);
            int i3 = indexOf;
            boolean z = false;
            while (i2 > 0 && sub_chars[i2 - 1] == '\\') {
                int i4 = i2 - 2;
                int i5 = 1;
                while (i4 >= 0 && sub_chars[i4] == '\\') {
                    i5++;
                    i4--;
                }
                if (i5 % 2 == 0) {
                    break;
                }
                int indexOf2 = this.text.indexOf(c, i3 + 1);
                int i6 = (indexOf2 - i3) + i2;
                if (i6 >= sub_chars.length) {
                    int length = (sub_chars.length * 3) / 2;
                    if (length < i6) {
                        length = i6;
                    }
                    char[] cArr = new char[length];
                    System.arraycopy(sub_chars, 0, cArr, 0, sub_chars.length);
                    sub_chars = cArr;
                }
                this.text.getChars(i3, indexOf2, sub_chars, i2);
                i3 = indexOf2;
                i2 = i6;
                z = true;
            }
            if (!z) {
                int i7 = 0;
                for (int i8 = 0; i8 < i2; i8++) {
                    char c3 = sub_chars[i8];
                    i7 = (i7 * 31) + c3;
                    if (c3 == '\\') {
                        z = true;
                    }
                }
                if (z) {
                    str = readString(sub_chars, i2);
                } else if (i2 < 20) {
                    str = symbolTable.addSymbol(sub_chars, 0, i2, i7);
                } else {
                    str = new String(sub_chars, 0, i2);
                }
            } else {
                str = readString(sub_chars, i2);
            }
            this.bp = i3 + 1;
            int i9 = this.bp;
            if (i9 >= this.len) {
                c2 = EOI;
            } else {
                c2 = this.text.charAt(i9);
            }
            this.ch = c2;
            return str;
        }
        throw new JSONException("unclosed str, " + info());
    }

    private static String readString(char[] cArr, int i) {
        int i2;
        char[] cArr2 = new char[i];
        int i3 = 0;
        int i4 = 0;
        while (i3 < i) {
            char c = cArr[i3];
            if (c != '\\') {
                cArr2[i4] = c;
                i4++;
            } else {
                i3++;
                char c2 = cArr[i3];
                if (c2 == '\"') {
                    i2 = i4 + 1;
                    cArr2[i4] = '\"';
                } else if (c2 != '\'') {
                    if (c2 != 'F') {
                        if (c2 == '\\') {
                            i2 = i4 + 1;
                            cArr2[i4] = '\\';
                        } else if (c2 == 'b') {
                            i2 = i4 + 1;
                            cArr2[i4] = '\b';
                        } else if (c2 != 'f') {
                            if (c2 == 'n') {
                                i2 = i4 + 1;
                                cArr2[i4] = '\n';
                            } else if (c2 == 'r') {
                                i2 = i4 + 1;
                                cArr2[i4] = '\r';
                            } else if (c2 != 'x') {
                                switch (c2) {
                                    case '/':
                                        i2 = i4 + 1;
                                        cArr2[i4] = '/';
                                        break;
                                    case '0':
                                        i2 = i4 + 1;
                                        cArr2[i4] = 0;
                                        break;
                                    case '1':
                                        i2 = i4 + 1;
                                        cArr2[i4] = 1;
                                        break;
                                    case '2':
                                        i2 = i4 + 1;
                                        cArr2[i4] = 2;
                                        break;
                                    case '3':
                                        i2 = i4 + 1;
                                        cArr2[i4] = 3;
                                        break;
                                    case '4':
                                        i2 = i4 + 1;
                                        cArr2[i4] = 4;
                                        break;
                                    case '5':
                                        i2 = i4 + 1;
                                        cArr2[i4] = 5;
                                        break;
                                    case '6':
                                        i2 = i4 + 1;
                                        cArr2[i4] = 6;
                                        break;
                                    case '7':
                                        i2 = i4 + 1;
                                        cArr2[i4] = 7;
                                        break;
                                    default:
                                        switch (c2) {
                                            case SystemAbilityDefinition.ABILITY_TOOLS_SERVICE_ID /* 116 */:
                                                i2 = i4 + 1;
                                                cArr2[i4] = '\t';
                                                break;
                                            case InnerMetadata.SceneDetectionType.SMART_SUGGEST_MODE_BEAUTY /* 117 */:
                                                i2 = i4 + 1;
                                                int i5 = i3 + 1;
                                                int i6 = i5 + 1;
                                                int i7 = i6 + 1;
                                                i3 = i7 + 1;
                                                cArr2[i4] = (char) Integer.parseInt(new String(new char[]{cArr[i5], cArr[i6], cArr[i7], cArr[i3]}), 16);
                                                break;
                                            case 'v':
                                                i2 = i4 + 1;
                                                cArr2[i4] = 11;
                                                break;
                                            default:
                                                throw new JSONException("unclosed.str.lit");
                                        }
                                }
                            } else {
                                i2 = i4 + 1;
                                int[] iArr = digits;
                                int i8 = i3 + 1;
                                i3 = i8 + 1;
                                cArr2[i4] = (char) ((iArr[cArr[i8]] * 16) + iArr[cArr[i3]]);
                            }
                        }
                    }
                    i2 = i4 + 1;
                    cArr2[i4] = '\f';
                } else {
                    i2 = i4 + 1;
                    cArr2[i4] = '\'';
                }
                i4 = i2;
            }
            i3++;
        }
        return new String(cArr2, 0, i4);
    }

    public String info() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("pos ");
        sb.append(this.bp);
        sb.append(", json : ");
        if (this.len < 65536) {
            str = this.text;
        } else {
            str = this.text.substring(0, 65536);
        }
        sb.append(str);
        return sb.toString();
    }

    /* access modifiers changed from: protected */
    public void skipComment() {
        next();
        char c = this.ch;
        if (c == '/') {
            do {
                next();
            } while (this.ch != '\n');
            next();
        } else if (c == '*') {
            next();
            while (true) {
                char c2 = this.ch;
                if (c2 == 26) {
                    return;
                }
                if (c2 == '*') {
                    next();
                    if (this.ch == '/') {
                        next();
                        return;
                    }
                } else {
                    next();
                }
            }
        } else {
            throw new JSONException("invalid comment");
        }
    }

    public final String scanSymbolUnQuoted(SymbolTable symbolTable) {
        int i = this.ch;
        boolean[] zArr = firstIdentifierFlags;
        if (i >= zArr.length || zArr[i]) {
            this.np = this.bp;
            this.sp = 1;
            while (true) {
                char next = next();
                boolean[] zArr2 = identifierFlags;
                if (next < zArr2.length && !zArr2[next]) {
                    break;
                }
                i = (i * 31) + next;
                this.sp++;
            }
            this.ch = charAt(this.bp);
            this.token = 18;
            if (this.sp != 4 || !this.text.startsWith("null", this.np)) {
                return symbolTable.addSymbol(this.text, this.np, this.sp, i);
            }
            return null;
        }
        throw new JSONException("illegal identifier : " + this.ch + ", " + info());
    }

    public final void scanString() {
        char c;
        char c2 = this.ch;
        int i = this.bp + 1;
        int indexOf = this.text.indexOf(c2, i);
        if (indexOf != -1) {
            int i2 = indexOf - i;
            char[] sub_chars = sub_chars(this.bp + 1, i2);
            boolean z = false;
            while (i2 > 0 && sub_chars[i2 - 1] == '\\') {
                int i3 = i2 - 2;
                int i4 = 1;
                while (i3 >= 0 && sub_chars[i3] == '\\') {
                    i4++;
                    i3--;
                }
                if (i4 % 2 == 0) {
                    break;
                }
                int indexOf2 = this.text.indexOf(c2, indexOf + 1);
                int i5 = (indexOf2 - indexOf) + i2;
                if (i5 >= sub_chars.length) {
                    int length = (sub_chars.length * 3) / 2;
                    if (length < i5) {
                        length = i5;
                    }
                    char[] cArr = new char[length];
                    System.arraycopy(sub_chars, 0, cArr, 0, sub_chars.length);
                    sub_chars = cArr;
                }
                this.text.getChars(indexOf, indexOf2, sub_chars, i2);
                indexOf = indexOf2;
                i2 = i5;
                z = true;
            }
            if (!z) {
                for (int i6 = 0; i6 < i2; i6++) {
                    if (sub_chars[i6] == '\\') {
                        z = true;
                    }
                }
            }
            this.sbuf = sub_chars;
            this.sp = i2;
            this.np = this.bp;
            this.hasSpecial = z;
            this.bp = indexOf + 1;
            int i7 = this.bp;
            if (i7 >= this.len) {
                c = EOI;
            } else {
                c = this.text.charAt(i7);
            }
            this.ch = c;
            this.token = 4;
            return;
        }
        throw new JSONException("unclosed str, " + info());
    }

    public String scanStringValue(char c) {
        String str;
        char c2;
        int i = this.bp + 1;
        int indexOf = this.text.indexOf(c, i);
        if (indexOf != -1) {
            if (V6) {
                str = this.text.substring(i, indexOf);
            } else {
                int i2 = indexOf - i;
                str = new String(sub_chars(this.bp + 1, i2), 0, i2);
            }
            if (str.indexOf(92) != -1) {
                while (true) {
                    int i3 = indexOf - 1;
                    int i4 = 0;
                    while (i3 >= 0 && this.text.charAt(i3) == '\\') {
                        i4++;
                        i3--;
                    }
                    if (i4 % 2 == 0) {
                        break;
                    }
                    indexOf = this.text.indexOf(c, indexOf + 1);
                }
                int i5 = indexOf - i;
                str = readString(sub_chars(this.bp + 1, i5), i5);
            }
            this.bp = indexOf + 1;
            int i6 = this.bp;
            if (i6 >= this.len) {
                c2 = EOI;
            } else {
                c2 = this.text.charAt(i6);
            }
            this.ch = c2;
            return str;
        }
        throw new JSONException("unclosed str, " + info());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x007a, code lost:
        r0 = r7;
     */
    public final int intValue() {
        char c;
        boolean z;
        int i;
        char c2;
        char c3;
        int i2 = this.np;
        int i3 = this.sp + i2;
        if (i2 >= this.len) {
            c = 26;
        } else {
            c = this.text.charAt(i2);
        }
        int i4 = 0;
        if (c == '-') {
            i = Integer.MIN_VALUE;
            i2++;
            z = true;
        } else {
            i = -2147483647;
            z = false;
        }
        if (i2 < i3) {
            int i5 = i2 + 1;
            if (i2 >= this.len) {
                c3 = 26;
            } else {
                c3 = this.text.charAt(i2);
            }
            i4 = -(c3 - '0');
            i2 = i5;
        }
        while (true) {
            if (i2 >= i3) {
                break;
            }
            int i6 = i2 + 1;
            if (i2 >= this.len) {
                c2 = 26;
            } else {
                c2 = this.text.charAt(i2);
            }
            if (c2 == 'L' || c2 == 'S' || c2 == 'B') {
                break;
            }
            int i7 = c2 - '0';
            if (i4 >= -214748364) {
                int i8 = i4 * 10;
                if (i8 >= i + i7) {
                    i4 = i8 - i7;
                    i2 = i6;
                } else {
                    throw new NumberFormatException(numberString());
                }
            } else {
                throw new NumberFormatException(numberString());
            }
        }
        if (!z) {
            return -i4;
        }
        if (i2 > this.np + 1) {
            return i4;
        }
        throw new NumberFormatException(numberString());
    }

    public byte[] bytesValue() {
        return decodeFast(this.text, this.np + 1, this.sp);
    }

    private void scanIdent() {
        this.np = this.bp - 1;
        this.hasSpecial = false;
        do {
            this.sp++;
            next();
        } while (Character.isLetterOrDigit(this.ch));
        String stringVal = stringVal();
        if (stringVal.equals("null")) {
            this.token = 8;
        } else if (stringVal.equals("true")) {
            this.token = 6;
        } else if (stringVal.equals("false")) {
            this.token = 7;
        } else if (stringVal.equals("new")) {
            this.token = 9;
        } else if (stringVal.equals("undefined")) {
            this.token = 23;
        } else if (stringVal.equals("Set")) {
            this.token = 21;
        } else if (stringVal.equals("TreeSet")) {
            this.token = 22;
        } else {
            this.token = 18;
        }
    }

    public final String stringVal() {
        if (this.hasSpecial) {
            return readString(this.sbuf, this.sp);
        }
        return subString(this.np + 1, this.sp);
    }

    private final String subString(int i, int i2) {
        char[] cArr = this.sbuf;
        if (i2 < cArr.length) {
            this.text.getChars(i, i + i2, cArr, 0);
            return new String(this.sbuf, 0, i2);
        }
        char[] cArr2 = new char[i2];
        this.text.getChars(i, i2 + i, cArr2, 0);
        return new String(cArr2);
    }

    /* access modifiers changed from: package-private */
    public final char[] sub_chars(int i, int i2) {
        char[] cArr = this.sbuf;
        if (i2 < cArr.length) {
            this.text.getChars(i, i2 + i, cArr, 0);
            return this.sbuf;
        }
        char[] cArr2 = new char[i2];
        this.sbuf = cArr2;
        this.text.getChars(i, i2 + i, cArr2, 0);
        return cArr2;
    }

    public final boolean isBlankInput() {
        int i = 0;
        while (true) {
            char charAt = charAt(i);
            boolean z = true;
            if (charAt == 26) {
                return true;
            }
            if (charAt > ' ' || !(charAt == ' ' || charAt == '\n' || charAt == '\r' || charAt == '\t' || charAt == '\f' || charAt == '\b')) {
                z = false;
            }
            if (!z) {
                return false;
            }
            i++;
        }
    }

    /* access modifiers changed from: package-private */
    public final void skipWhitespace() {
        while (true) {
            char c = this.ch;
            if (c > '/') {
                return;
            }
            if (c == ' ' || c == '\r' || c == '\n' || c == '\t' || c == '\f' || c == '\b') {
                next();
            } else if (c == '/') {
                skipComment();
            } else {
                return;
            }
        }
    }

    public final void scanNumber() {
        char c;
        char c2;
        char c3;
        char c4;
        char c5;
        char c6;
        char c7;
        int i = this.bp;
        this.np = i;
        this.exp = false;
        if (this.ch == '-') {
            this.sp++;
            int i2 = i + 1;
            this.bp = i2;
            if (i2 >= this.len) {
                c7 = 26;
            } else {
                c7 = this.text.charAt(i2);
            }
            this.ch = c7;
        }
        while (true) {
            char c8 = this.ch;
            if (c8 < '0' || c8 > '9') {
                break;
            }
            this.sp++;
            int i3 = this.bp + 1;
            this.bp = i3;
            if (i3 >= this.len) {
                c6 = 26;
            } else {
                c6 = this.text.charAt(i3);
            }
            this.ch = c6;
        }
        this.isDouble = false;
        if (this.ch == '.') {
            this.sp++;
            int i4 = this.bp + 1;
            this.bp = i4;
            if (i4 >= this.len) {
                c4 = 26;
            } else {
                c4 = this.text.charAt(i4);
            }
            this.ch = c4;
            this.isDouble = true;
            while (true) {
                char c9 = this.ch;
                if (c9 < '0' || c9 > '9') {
                    break;
                }
                this.sp++;
                int i5 = this.bp + 1;
                this.bp = i5;
                if (i5 >= this.len) {
                    c5 = 26;
                } else {
                    c5 = this.text.charAt(i5);
                }
                this.ch = c5;
            }
        }
        char c10 = this.ch;
        if (c10 == 'L') {
            this.sp++;
            next();
        } else if (c10 == 'S') {
            this.sp++;
            next();
        } else if (c10 == 'B') {
            this.sp++;
            next();
        } else if (c10 == 'F') {
            this.sp++;
            next();
            this.isDouble = true;
        } else if (c10 == 'D') {
            this.sp++;
            next();
            this.isDouble = true;
        } else if (c10 == 'e' || c10 == 'E') {
            this.sp++;
            int i6 = this.bp + 1;
            this.bp = i6;
            if (i6 >= this.len) {
                c = 26;
            } else {
                c = this.text.charAt(i6);
            }
            this.ch = c;
            char c11 = this.ch;
            if (c11 == '+' || c11 == '-') {
                this.sp++;
                int i7 = this.bp + 1;
                this.bp = i7;
                if (i7 >= this.len) {
                    c3 = 26;
                } else {
                    c3 = this.text.charAt(i7);
                }
                this.ch = c3;
            }
            while (true) {
                char c12 = this.ch;
                if (c12 < '0' || c12 > '9') {
                    break;
                }
                this.sp++;
                int i8 = this.bp + 1;
                this.bp = i8;
                if (i8 >= this.len) {
                    c2 = 26;
                } else {
                    c2 = this.text.charAt(i8);
                }
                this.ch = c2;
            }
            char c13 = this.ch;
            if (c13 == 'D' || c13 == 'F') {
                this.sp++;
                next();
            }
            this.exp = true;
            this.isDouble = true;
        }
        if (this.isDouble) {
            this.token = 3;
        } else {
            this.token = 2;
        }
    }

    public boolean scanBoolean() {
        boolean z = false;
        int i = 1;
        if (this.text.startsWith("false", this.bp)) {
            i = 5;
        } else if (this.text.startsWith("true", this.bp)) {
            z = true;
            i = 4;
        } else {
            char c = this.ch;
            if (c == '1') {
                z = true;
            } else if (c != '0') {
                this.matchStat = -1;
                return false;
            }
        }
        this.bp += i;
        this.ch = charAt(this.bp);
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:145:0x0288 A[Catch:{ NumberFormatException -> 0x02ca }] */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x029e A[Catch:{ NumberFormatException -> 0x02ca }] */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x02a9 A[Catch:{ NumberFormatException -> 0x02ca }] */
    public final Number scanNumberValue() {
        long j;
        boolean z;
        int i;
        char c;
        int i2;
        boolean z2;
        char c2;
        boolean z3;
        char[] cArr;
        char c3;
        int i3;
        int i4;
        char[] cArr2;
        Number number;
        char c4;
        char c5;
        char c6;
        char c7;
        char c8;
        long j2;
        long j3;
        char c9;
        long j4;
        long j5;
        char c10;
        char c11;
        int i5 = this.bp;
        this.np = 0;
        if (this.ch == '-') {
            j = Long.MIN_VALUE;
            this.np++;
            int i6 = i5 + 1;
            this.bp = i6;
            if (i6 >= this.len) {
                c11 = EOI;
            } else {
                c11 = this.text.charAt(i6);
            }
            this.ch = c11;
            z = true;
        } else {
            j = -9223372036854775807L;
            z = false;
        }
        boolean z4 = false;
        long j6 = 0;
        int i7 = 1;
        while (true) {
            char c12 = this.ch;
            i = 18;
            c = '0';
            if (c12 < '0' || c12 > '9') {
                break;
            }
            int i8 = c12 - '0';
            if (i7 < 18) {
                j4 = j6 * 10;
                j5 = (long) i8;
            } else {
                if (j6 < -922337203685477580L) {
                    z4 = true;
                }
                j4 = j6 * 10;
                j5 = (long) i8;
                if (j4 < j + j5) {
                    z4 = true;
                }
            }
            j6 = j4 - j5;
            this.np++;
            int i9 = this.bp + 1;
            this.bp = i9;
            if (i9 >= this.len) {
                c10 = EOI;
            } else {
                c10 = this.text.charAt(i9);
            }
            this.ch = c10;
            i7++;
        }
        Number number2 = null;
        if (this.ch == '.') {
            this.np++;
            int i10 = this.bp + 1;
            this.bp = i10;
            if (i10 >= this.len) {
                c8 = EOI;
            } else {
                c8 = this.text.charAt(i10);
            }
            this.ch = c8;
            boolean z5 = z4;
            int i11 = i7;
            i2 = 0;
            while (true) {
                char c13 = this.ch;
                if (c13 < c || c13 > '9') {
                    break;
                }
                i2++;
                int i12 = c13 - '0';
                if (i11 < i) {
                    j2 = j6 * 10;
                    j3 = (long) i12;
                } else {
                    if (j6 < -922337203685477580L) {
                        z5 = true;
                    }
                    j2 = j6 * 10;
                    j3 = (long) i12;
                    if (j2 < j + j3) {
                        z5 = true;
                    }
                }
                j6 = j2 - j3;
                this.np++;
                int i13 = this.bp + 1;
                this.bp = i13;
                if (i13 >= this.len) {
                    c9 = EOI;
                } else {
                    c9 = this.text.charAt(i13);
                }
                this.ch = c9;
                i11++;
                c = '0';
                i = 18;
            }
            if (!z) {
                j6 = -j6;
            }
            z2 = true;
            z4 = z5;
        } else {
            if (!z) {
                j6 = -j6;
            }
            char c14 = this.ch;
            if (c14 == 'L') {
                this.np++;
                next();
                number2 = Long.valueOf(j6);
            } else if (c14 == 'S') {
                this.np++;
                next();
                number2 = Short.valueOf((short) ((int) j6));
            } else if (c14 == 'B') {
                this.np++;
                next();
                number2 = Byte.valueOf((byte) ((int) j6));
            } else if (c14 == 'F') {
                this.np++;
                next();
                number2 = Float.valueOf((float) j6);
            } else if (c14 == 'D') {
                this.np++;
                next();
                number2 = Double.valueOf((double) j6);
            }
            z2 = false;
            i2 = 0;
        }
        char c15 = this.ch;
        if (c15 == 'e' || c15 == 'E') {
            this.np++;
            int i14 = this.bp + 1;
            this.bp = i14;
            if (i14 >= this.len) {
                c4 = EOI;
            } else {
                c4 = this.text.charAt(i14);
            }
            this.ch = c4;
            char c16 = this.ch;
            if (c16 == '+' || c16 == '-') {
                this.np++;
                int i15 = this.bp + 1;
                this.bp = i15;
                if (i15 >= this.len) {
                    c7 = EOI;
                } else {
                    c7 = this.text.charAt(i15);
                }
                this.ch = c7;
            }
            while (true) {
                char c17 = this.ch;
                if (c17 < '0' || c17 > '9') {
                    break;
                }
                this.np++;
                int i16 = this.bp + 1;
                this.bp = i16;
                if (i16 >= this.len) {
                    c6 = EOI;
                } else {
                    c6 = this.text.charAt(i16);
                }
                this.ch = c6;
            }
            char c18 = this.ch;
            if (c18 == 'D' || c18 == 'F') {
                this.np++;
                c5 = this.ch;
                next();
            } else {
                c5 = 0;
            }
            c2 = c5;
            z3 = true;
        } else {
            z3 = false;
            c2 = 0;
        }
        if (z2 || z3) {
            int i17 = this.bp - i5;
            if (c2 != 0) {
                i17--;
            }
            if (z3 || (this.features & Feature.UseBigDecimal.mask) == 0) {
                char[] cArr3 = this.sbuf;
                if (i17 < cArr3.length) {
                    this.text.getChars(i5, i5 + i17, cArr3, 0);
                    cArr = this.sbuf;
                } else {
                    char[] cArr4 = new char[i17];
                    this.text.getChars(i5, i5 + i17, cArr4, 0);
                    cArr = cArr4;
                }
                if (i17 > 9 || z3) {
                    String str = new String(cArr, 0, i17);
                    if (c2 == 'F') {
                        return Float.valueOf(str);
                    }
                    return Double.valueOf(Double.parseDouble(str));
                }
                try {
                    char c19 = cArr[0];
                    if (c19 != '-') {
                        if (c19 != '+') {
                            i3 = 1;
                            c3 = '0';
                            int i18 = c19 - c3;
                            int i19 = 0;
                            while (i3 < i17) {
                                char c20 = cArr[i3];
                                if (c20 == '.') {
                                    i19 = 1;
                                } else {
                                    i18 = (i18 * 10) + (c20 - '0');
                                    if (i19 != 0) {
                                        i19 *= 10;
                                    }
                                }
                                i3++;
                            }
                            if (c2 != 'F') {
                                float f = ((float) i18) / ((float) i19);
                                if (z) {
                                    f = -f;
                                }
                                return Float.valueOf(f);
                            }
                            double d = ((double) i18) / ((double) i19);
                            if (z) {
                                d = -d;
                            }
                            return Double.valueOf(d);
                        }
                    }
                    c3 = '0';
                    i3 = 2;
                    c19 = cArr[1];
                    int i182 = c19 - c3;
                    int i192 = 0;
                    while (i3 < i17) {
                    }
                    if (c2 != 'F') {
                    }
                } catch (NumberFormatException e) {
                    throw new JSONException(e.getMessage() + ", " + info(), e);
                }
            } else if (!z4) {
                return BigDecimal.valueOf(j6, i2);
            } else {
                char[] cArr5 = this.sbuf;
                if (i17 < cArr5.length) {
                    i4 = 0;
                    this.text.getChars(i5, i5 + i17, cArr5, 0);
                    cArr2 = this.sbuf;
                } else {
                    i4 = 0;
                    char[] cArr6 = new char[i17];
                    this.text.getChars(i5, i5 + i17, cArr6, 0);
                    cArr2 = cArr6;
                }
                return new BigDecimal(cArr2, i4, i17);
            }
        } else {
            if (z4) {
                int i20 = this.bp;
                char[] cArr7 = new char[(i20 - i5)];
                this.text.getChars(i5, i20, cArr7, 0);
                number = new BigInteger(new String(cArr7));
            } else {
                number = number2;
            }
            if (number != null) {
                return number;
            }
            if (j6 <= -2147483648L || j6 >= 2147483647L) {
                return Long.valueOf(j6);
            }
            return Integer.valueOf((int) j6);
        }
    }

    public final long scanLongValue() {
        boolean z;
        long j;
        char c;
        this.np = 0;
        if (this.ch == '-') {
            j = Long.MIN_VALUE;
            this.np++;
            int i = this.bp + 1;
            this.bp = i;
            if (i < this.len) {
                this.ch = this.text.charAt(i);
                z = true;
            } else {
                throw new JSONException("syntax error, " + info());
            }
        } else {
            z = false;
            j = -9223372036854775807L;
        }
        long j2 = 0;
        while (true) {
            char c2 = this.ch;
            if (c2 < '0' || c2 > '9') {
                break;
            }
            int i2 = c2 - '0';
            if (j2 >= -922337203685477580L) {
                long j3 = j2 * 10;
                long j4 = (long) i2;
                if (j3 >= j + j4) {
                    j2 = j3 - j4;
                    this.np++;
                    int i3 = this.bp + 1;
                    this.bp = i3;
                    if (i3 >= this.len) {
                        c = EOI;
                    } else {
                        c = this.text.charAt(i3);
                    }
                    this.ch = c;
                } else {
                    throw new JSONException("error long value, " + j3 + ", " + info());
                }
            } else {
                throw new JSONException("error long value, " + j2 + ", " + info());
            }
        }
        return !z ? -j2 : j2;
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0076  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0086  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002c  */
    public final long longValue() throws NumberFormatException {
        long j;
        boolean z;
        long j2;
        int i;
        char c;
        int i2 = this.np;
        int i3 = this.sp + i2;
        if (charAt(i2) == '-') {
            j = Long.MIN_VALUE;
            i2++;
            z = true;
        } else {
            j = -9223372036854775807L;
            z = false;
        }
        if (i2 < i3) {
            i = i2 + 1;
            j2 = (long) (-(charAt(i2) - '0'));
        } else {
            j2 = 0;
            if (i2 < i3) {
                i = i2 + 1;
                if (i2 >= this.len) {
                    c = EOI;
                } else {
                    c = this.text.charAt(i2);
                }
                if (c == 'L' || c == 'S' || c == 'B') {
                    i2 = i;
                } else {
                    int i4 = c - '0';
                    if (j2 >= -922337203685477580L) {
                        long j3 = j2 * 10;
                        long j4 = (long) i4;
                        if (j3 >= j + j4) {
                            j2 = j3 - j4;
                        }
                        throw new NumberFormatException(numberString());
                    }
                    throw new NumberFormatException(numberString());
                }
            }
            if (z) {
                return -j2;
            }
            if (i2 > this.np + 1) {
                return j2;
            }
            throw new NumberFormatException(numberString());
        }
        i2 = i;
        if (i2 < i3) {
        }
        if (z) {
        }
    }

    public final Number decimalValue(boolean z) {
        char c;
        char[] cArr;
        boolean z2;
        int i = (this.np + this.sp) - 1;
        if (i >= this.len) {
            c = EOI;
        } else {
            c = this.text.charAt(i);
        }
        if (c == 'F') {
            try {
                return Float.valueOf(Float.parseFloat(numberString()));
            } catch (NumberFormatException e) {
                throw new JSONException(e.getMessage() + ", " + info());
            }
        } else if (c == 'D') {
            return Double.valueOf(Double.parseDouble(numberString()));
        } else {
            if (z) {
                return decimalValue();
            }
            char charAt = this.text.charAt((this.np + this.sp) - 1);
            int i2 = this.sp;
            if (charAt == 'L' || charAt == 'S' || charAt == 'B' || charAt == 'F' || charAt == 'D') {
                i2--;
            }
            int i3 = this.np;
            int i4 = 0;
            if (i2 < this.sbuf.length) {
                this.text.getChars(i3, i3 + i2, this.sbuf, 0);
                cArr = this.sbuf;
            } else {
                char[] cArr2 = new char[i2];
                this.text.getChars(i3, i3 + i2, cArr2, 0);
                cArr = cArr2;
            }
            if (i2 > 9 || this.exp) {
                return Double.valueOf(Double.parseDouble(new String(cArr, 0, i2)));
            }
            char c2 = cArr[0];
            int i5 = 2;
            if (c2 == '-') {
                c2 = cArr[1];
                z2 = true;
            } else {
                if (c2 == '+') {
                    c2 = cArr[1];
                } else {
                    i5 = 1;
                }
                z2 = false;
            }
            int i6 = c2 - '0';
            while (i5 < i2) {
                char c3 = cArr[i5];
                if (c3 == '.') {
                    i4 = 1;
                } else {
                    i6 = (i6 * 10) + (c3 - '0');
                    if (i4 != 0) {
                        i4 *= 10;
                    }
                }
                i5++;
            }
            double d = ((double) i6) / ((double) i4);
            if (z2) {
                d = -d;
            }
            return Double.valueOf(d);
        }
    }

    public final BigDecimal decimalValue() {
        char charAt = this.text.charAt((this.np + this.sp) - 1);
        int i = this.sp;
        if (charAt == 'L' || charAt == 'S' || charAt == 'B' || charAt == 'F' || charAt == 'D') {
            i--;
        }
        int i2 = this.np;
        char[] cArr = this.sbuf;
        if (i < cArr.length) {
            this.text.getChars(i2, i2 + i, cArr, 0);
            return new BigDecimal(this.sbuf, 0, i);
        }
        char[] cArr2 = new char[i];
        this.text.getChars(i2, i + i2, cArr2, 0);
        return new BigDecimal(cArr2);
    }

    public boolean matchField(long j) {
        char c;
        char c2;
        char c3;
        char c4;
        char c5;
        char c6 = this.ch;
        int i = this.bp + 1;
        int i2 = 1;
        while (c6 != '\"' && c6 != '\'') {
            if (c6 > ' ' || !(c6 == ' ' || c6 == '\n' || c6 == '\r' || c6 == '\t' || c6 == '\f' || c6 == '\b')) {
                this.fieldHash = 0;
                this.matchStat = -2;
                return false;
            }
            int i3 = i2 + 1;
            int i4 = this.bp + i2;
            if (i4 >= this.len) {
                c6 = EOI;
            } else {
                c6 = this.text.charAt(i4);
            }
            i2 = i3;
        }
        int i5 = i;
        long j2 = -3750763034362895579L;
        while (true) {
            if (i5 >= this.len) {
                break;
            }
            char charAt = this.text.charAt(i5);
            if (charAt == c6) {
                i2 += (i5 - i) + 1;
                break;
            }
            j2 = 1099511628211L * (j2 ^ ((long) charAt));
            i5++;
        }
        if (j2 != j) {
            this.matchStat = -2;
            this.fieldHash = j2;
            return false;
        }
        int i6 = i2 + 1;
        int i7 = this.bp + i2;
        if (i7 >= this.len) {
            c = EOI;
        } else {
            c = this.text.charAt(i7);
        }
        while (c != ':') {
            if (c > ' ' || !(c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == '\f' || c == '\b')) {
                throw new JSONException("match feild error expect ':'");
            }
            int i8 = i6 + 1;
            int i9 = this.bp + i6;
            if (i9 >= this.len) {
                c = EOI;
            } else {
                c = this.text.charAt(i9);
            }
            i6 = i8;
        }
        int i10 = this.bp + i6;
        if (i10 >= this.len) {
            c2 = EOI;
        } else {
            c2 = this.text.charAt(i10);
        }
        if (c2 == '{') {
            this.bp = i10 + 1;
            int i11 = this.bp;
            if (i11 >= this.len) {
                c5 = EOI;
            } else {
                c5 = this.text.charAt(i11);
            }
            this.ch = c5;
            this.token = 12;
        } else if (c2 == '[') {
            this.bp = i10 + 1;
            int i12 = this.bp;
            if (i12 >= this.len) {
                c4 = EOI;
            } else {
                c4 = this.text.charAt(i12);
            }
            this.ch = c4;
            this.token = 14;
        } else {
            this.bp = i10;
            int i13 = this.bp;
            if (i13 >= this.len) {
                c3 = EOI;
            } else {
                c3 = this.text.charAt(i13);
            }
            this.ch = c3;
            nextToken();
        }
        return true;
    }

    private int matchFieldHash(long j) {
        char c;
        char c2 = this.ch;
        int i = this.bp;
        int i2 = 1;
        while (c2 != '\"' && c2 != '\'') {
            if (c2 == ' ' || c2 == '\n' || c2 == '\r' || c2 == '\t' || c2 == '\f' || c2 == '\b') {
                int i3 = i2 + 1;
                int i4 = this.bp + i2;
                if (i4 >= this.len) {
                    c2 = EOI;
                } else {
                    c2 = this.text.charAt(i4);
                }
                i2 = i3;
            } else {
                this.fieldHash = 0;
                this.matchStat = -2;
                return 0;
            }
        }
        long j2 = -3750763034362895579L;
        int i5 = this.bp + i2;
        while (true) {
            if (i5 >= this.len) {
                break;
            }
            char charAt = this.text.charAt(i5);
            if (charAt == c2) {
                i2 += (i5 - this.bp) - i2;
                break;
            }
            j2 = 1099511628211L * (((long) charAt) ^ j2);
            i5++;
        }
        if (j2 != j) {
            this.fieldHash = j2;
            this.matchStat = -2;
            return 0;
        }
        int i6 = i2 + 1;
        int i7 = this.bp + i6;
        if (i7 >= this.len) {
            c = EOI;
        } else {
            c = this.text.charAt(i7);
        }
        while (c != ':') {
            if (c > ' ' || !(c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == '\f' || c == '\b')) {
                throw new JSONException("match feild error expect ':'");
            }
            int i8 = i6 + 1;
            int i9 = this.bp + i6;
            if (i9 >= this.len) {
                c = EOI;
            } else {
                c = this.text.charAt(i9);
            }
            i6 = i8;
        }
        return i6 + 1;
    }

    public int scanFieldInt(long j) {
        char c;
        int i;
        char c2;
        char c3;
        int i2;
        this.matchStat = 0;
        int matchFieldHash = matchFieldHash(j);
        if (matchFieldHash == 0) {
            return 0;
        }
        int i3 = matchFieldHash + 1;
        int i4 = this.bp + matchFieldHash;
        int i5 = this.len;
        char c4 = EOI;
        if (i4 >= i5) {
            c = 26;
        } else {
            c = this.text.charAt(i4);
        }
        boolean z = c == '\"';
        if (z) {
            int i6 = i3 + 1;
            int i7 = this.bp + i3;
            if (i7 >= this.len) {
                c = 26;
            } else {
                c = this.text.charAt(i7);
            }
            i3 = i6;
            z = true;
        }
        boolean z2 = c == '-';
        if (z2) {
            int i8 = i3 + 1;
            int i9 = this.bp + i3;
            if (i9 >= this.len) {
                c = 26;
            } else {
                c = this.text.charAt(i9);
            }
            i3 = i8;
        }
        if (c < '0' || c > '9') {
            this.matchStat = -1;
            return 0;
        }
        int i10 = c - '0';
        while (true) {
            i = i3 + 1;
            int i11 = this.bp + i3;
            if (i11 >= this.len) {
                c2 = 26;
            } else {
                c2 = this.text.charAt(i11);
            }
            if (c2 < '0' || c2 > '9') {
                break;
            }
            i10 = (i10 * 10) + (c2 - '0');
            i3 = i;
        }
        if (c2 == '.') {
            this.matchStat = -1;
            return 0;
        }
        if (c2 != '\"') {
            c3 = c2;
            i2 = i;
        } else if (!z) {
            this.matchStat = -1;
            return 0;
        } else {
            i2 = i + 1;
            int i12 = this.bp + i;
            c3 = i12 >= this.len ? 26 : this.text.charAt(i12);
        }
        if (i10 < 0) {
            this.matchStat = -1;
            return 0;
        }
        while (c3 != ',') {
            if (c3 <= ' ' && (c3 == ' ' || c3 == '\n' || c3 == '\r' || c3 == '\t' || c3 == '\f' || c3 == '\b')) {
                int i13 = i2 + 1;
                int i14 = this.bp + i2;
                if (i14 >= this.len) {
                    c3 = 26;
                } else {
                    c3 = this.text.charAt(i14);
                }
                i2 = i13;
            } else if (c3 == '}') {
                int i15 = i2 + 1;
                char charAt = charAt(this.bp + i2);
                if (charAt == ',') {
                    this.token = 16;
                    this.bp += i15 - 1;
                    int i16 = this.bp + 1;
                    this.bp = i16;
                    if (i16 < this.len) {
                        c4 = this.text.charAt(i16);
                    }
                    this.ch = c4;
                } else if (charAt == ']') {
                    this.token = 15;
                    this.bp += i15 - 1;
                    int i17 = this.bp + 1;
                    this.bp = i17;
                    if (i17 < this.len) {
                        c4 = this.text.charAt(i17);
                    }
                    this.ch = c4;
                } else if (charAt == '}') {
                    this.token = 13;
                    this.bp += i15 - 1;
                    int i18 = this.bp + 1;
                    this.bp = i18;
                    if (i18 < this.len) {
                        c4 = this.text.charAt(i18);
                    }
                    this.ch = c4;
                } else if (charAt == 26) {
                    this.token = 20;
                    this.bp += i15 - 1;
                    this.ch = EOI;
                } else {
                    this.matchStat = -1;
                    return 0;
                }
                this.matchStat = 4;
                return z2 ? -i10 : i10;
            } else {
                this.matchStat = -1;
                return 0;
            }
        }
        this.bp += i2 - 1;
        int i19 = this.bp + 1;
        this.bp = i19;
        if (i19 < this.len) {
            c4 = this.text.charAt(i19);
        }
        this.ch = c4;
        this.matchStat = 3;
        this.token = 16;
        return z2 ? -i10 : i10;
    }

    public final int[] scanFieldIntArray(long j) {
        char c;
        char c2;
        int[] iArr;
        int i;
        int i2;
        char c3;
        int i3;
        boolean z;
        int[] iArr2;
        int i4;
        int i5;
        char c4;
        char c5;
        this.matchStat = 0;
        int matchFieldHash = matchFieldHash(j);
        int[] iArr3 = null;
        if (matchFieldHash == 0) {
            return null;
        }
        int i6 = matchFieldHash + 1;
        int i7 = this.bp + matchFieldHash;
        if (i7 >= this.len) {
            c = 26;
        } else {
            c = this.text.charAt(i7);
        }
        int i8 = -1;
        if (c != '[') {
            this.matchStat = -1;
            return null;
        }
        int i9 = i6 + 1;
        int i10 = this.bp + i6;
        if (i10 >= this.len) {
            c2 = 26;
        } else {
            c2 = this.text.charAt(i10);
        }
        int[] iArr4 = new int[16];
        if (c2 == ']') {
            int i11 = i9 + 1;
            int i12 = this.bp + i9;
            if (i12 >= this.len) {
                c3 = 26;
            } else {
                c3 = this.text.charAt(i12);
            }
            i2 = 0;
            i = i11;
            iArr = iArr4;
        } else {
            iArr = iArr4;
            int i13 = 0;
            while (true) {
                if (c2 == '-') {
                    i3 = i9 + 1;
                    int i14 = this.bp + i9;
                    if (i14 >= this.len) {
                        c2 = 26;
                    } else {
                        c2 = this.text.charAt(i14);
                    }
                    z = true;
                } else {
                    i3 = i9;
                    z = false;
                }
                if (c2 >= '0') {
                    if (c2 > '9') {
                        i4 = i8;
                        iArr2 = null;
                        break;
                    }
                    int i15 = c2 - '0';
                    while (true) {
                        i5 = i3 + 1;
                        int i16 = this.bp + i3;
                        if (i16 >= this.len) {
                            c4 = 26;
                        } else {
                            c4 = this.text.charAt(i16);
                        }
                        if (c4 < '0' || c4 > '9') {
                            break;
                        }
                        i15 = (i15 * 10) + (c4 - '0');
                        i3 = i5;
                    }
                    if (i13 >= iArr.length) {
                        int[] iArr5 = new int[((iArr.length * 3) / 2)];
                        System.arraycopy(iArr, 0, iArr5, 0, i13);
                        iArr = iArr5;
                    }
                    i2 = i13 + 1;
                    if (z) {
                        i15 = -i15;
                    }
                    iArr[i13] = i15;
                    if (c4 == ',') {
                        int i17 = i5 + 1;
                        int i18 = this.bp + i5;
                        if (i18 >= this.len) {
                            c5 = 26;
                        } else {
                            c5 = this.text.charAt(i18);
                        }
                        c4 = c5;
                        i5 = i17;
                    } else if (c4 == ']') {
                        i = i5 + 1;
                        int i19 = this.bp + i5;
                        if (i19 >= this.len) {
                            c3 = 26;
                        } else {
                            c3 = this.text.charAt(i19);
                        }
                    }
                    i13 = i2;
                    iArr3 = null;
                    i9 = i5;
                    i8 = -1;
                    c2 = c4;
                } else {
                    iArr2 = iArr3;
                    i4 = i8;
                    break;
                }
            }
            this.matchStat = i4;
            return iArr2;
        }
        if (i2 != iArr.length) {
            int[] iArr6 = new int[i2];
            System.arraycopy(iArr, 0, iArr6, 0, i2);
            iArr = iArr6;
        }
        if (c3 == ',') {
            this.bp += i - 1;
            next();
            this.matchStat = 3;
            this.token = 16;
            return iArr;
        } else if (c3 == '}') {
            int i20 = i + 1;
            char charAt = charAt(this.bp + i);
            if (charAt == ',') {
                this.token = 16;
                this.bp += i20 - 1;
                next();
            } else if (charAt == ']') {
                this.token = 15;
                this.bp += i20 - 1;
                next();
            } else if (charAt == '}') {
                this.token = 13;
                this.bp += i20 - 1;
                next();
            } else if (charAt == 26) {
                this.bp += i20 - 1;
                this.token = 20;
                this.ch = EOI;
            } else {
                this.matchStat = -1;
                return null;
            }
            this.matchStat = 4;
            return iArr;
        } else {
            this.matchStat = -1;
            return null;
        }
    }

    public long scanFieldLong(long j) {
        char c;
        int i;
        char c2;
        char c3;
        char c4;
        char c5;
        char c6;
        boolean z = false;
        this.matchStat = 0;
        int matchFieldHash = matchFieldHash(j);
        if (matchFieldHash == 0) {
            return 0;
        }
        int i2 = matchFieldHash + 1;
        int i3 = this.bp + matchFieldHash;
        if (i3 >= this.len) {
            c = EOI;
        } else {
            c = this.text.charAt(i3);
        }
        boolean z2 = c == '\"';
        if (z2) {
            int i4 = i2 + 1;
            int i5 = this.bp + i2;
            if (i5 >= this.len) {
                c = EOI;
            } else {
                c = this.text.charAt(i5);
            }
            i2 = i4;
        }
        if (c == '-') {
            z = true;
        }
        if (z) {
            int i6 = i2 + 1;
            int i7 = this.bp + i2;
            if (i7 >= this.len) {
                c = EOI;
            } else {
                c = this.text.charAt(i7);
            }
            i2 = i6;
        }
        if (c < '0' || c > '9') {
            this.matchStat = -1;
            return 0;
        }
        long j2 = (long) (c - '0');
        while (true) {
            i = i2 + 1;
            int i8 = this.bp + i2;
            if (i8 >= this.len) {
                c2 = EOI;
            } else {
                c2 = this.text.charAt(i8);
            }
            if (c2 < '0' || c2 > '9') {
                break;
            }
            j2 = (j2 * 10) + ((long) (c2 - '0'));
            i2 = i;
        }
        if (c2 == '.') {
            this.matchStat = -1;
            return 0;
        }
        if (c2 == '\"') {
            if (!z2) {
                this.matchStat = -1;
                return 0;
            }
            int i9 = i + 1;
            int i10 = this.bp + i;
            if (i10 >= this.len) {
                c2 = EOI;
            } else {
                c2 = this.text.charAt(i10);
            }
            i = i9;
        }
        if (j2 < 0) {
            this.matchStat = -1;
            return 0;
        } else if (c2 == ',') {
            this.bp += i - 1;
            int i11 = this.bp + 1;
            this.bp = i11;
            if (i11 >= this.len) {
                c6 = EOI;
            } else {
                c6 = this.text.charAt(i11);
            }
            this.ch = c6;
            this.matchStat = 3;
            this.token = 16;
            return z ? -j2 : j2;
        } else if (c2 == '}') {
            int i12 = i + 1;
            char charAt = charAt(this.bp + i);
            if (charAt == ',') {
                this.token = 16;
                this.bp += i12 - 1;
                int i13 = this.bp + 1;
                this.bp = i13;
                if (i13 >= this.len) {
                    c5 = EOI;
                } else {
                    c5 = this.text.charAt(i13);
                }
                this.ch = c5;
            } else if (charAt == ']') {
                this.token = 15;
                this.bp += i12 - 1;
                int i14 = this.bp + 1;
                this.bp = i14;
                if (i14 >= this.len) {
                    c4 = EOI;
                } else {
                    c4 = this.text.charAt(i14);
                }
                this.ch = c4;
            } else if (charAt == '}') {
                this.token = 13;
                this.bp += i12 - 1;
                int i15 = this.bp + 1;
                this.bp = i15;
                if (i15 >= this.len) {
                    c3 = EOI;
                } else {
                    c3 = this.text.charAt(i15);
                }
                this.ch = c3;
            } else if (charAt == 26) {
                this.token = 20;
                this.bp += i12 - 1;
                this.ch = EOI;
            } else {
                this.matchStat = -1;
                return 0;
            }
            this.matchStat = 4;
            return z ? -j2 : j2;
        } else {
            this.matchStat = -1;
            return 0;
        }
    }

    public String scanFieldString(long j) {
        String str;
        char c;
        char c2;
        boolean z;
        this.matchStat = 0;
        int matchFieldHash = matchFieldHash(j);
        if (matchFieldHash == 0) {
            return null;
        }
        int i = matchFieldHash + 1;
        int i2 = this.bp + matchFieldHash;
        if (i2 >= this.len) {
            throw new JSONException("unclosed str, " + info());
        } else if (this.text.charAt(i2) != '\"') {
            this.matchStat = -1;
            return this.stringDefaultValue;
        } else {
            int i3 = this.bp + i;
            int indexOf = this.text.indexOf(34, i3);
            if (indexOf != -1) {
                if (V6) {
                    str = this.text.substring(i3, indexOf);
                } else {
                    int i4 = indexOf - i3;
                    str = new String(sub_chars(this.bp + i, i4), 0, i4);
                }
                if (str.indexOf(92) != -1) {
                    boolean z2 = false;
                    while (true) {
                        int i5 = indexOf - 1;
                        z = z2;
                        int i6 = 0;
                        while (i5 >= 0 && this.text.charAt(i5) == '\\') {
                            i6++;
                            i5--;
                            z = true;
                        }
                        if (i6 % 2 == 0) {
                            break;
                        }
                        indexOf = this.text.indexOf(34, indexOf + 1);
                        z2 = z;
                    }
                    int i7 = indexOf - i3;
                    char[] sub_chars = sub_chars(this.bp + i, i7);
                    if (z) {
                        str = readString(sub_chars, i7);
                    } else {
                        str = new String(sub_chars, 0, i7);
                        if (str.indexOf(92) != -1) {
                            str = readString(sub_chars, i7);
                        }
                    }
                }
                int i8 = indexOf + 1;
                int i9 = this.len;
                char c3 = EOI;
                if (i8 >= i9) {
                    c = 26;
                } else {
                    c = this.text.charAt(i8);
                }
                if (c == ',') {
                    this.bp = i8;
                    int i10 = this.bp + 1;
                    this.bp = i10;
                    if (i10 < this.len) {
                        c3 = this.text.charAt(i10);
                    }
                    this.ch = c3;
                    this.matchStat = 3;
                    this.token = 16;
                    return str;
                } else if (c == '}') {
                    int i11 = i8 + 1;
                    if (i11 >= this.len) {
                        c2 = 26;
                    } else {
                        c2 = this.text.charAt(i11);
                    }
                    if (c2 == ',') {
                        this.token = 16;
                        this.bp = i11;
                        next();
                    } else if (c2 == ']') {
                        this.token = 15;
                        this.bp = i11;
                        next();
                    } else if (c2 == '}') {
                        this.token = 13;
                        this.bp = i11;
                        next();
                    } else if (c2 == 26) {
                        this.token = 20;
                        this.bp = i11;
                        this.ch = EOI;
                    } else {
                        this.matchStat = -1;
                        return this.stringDefaultValue;
                    }
                    this.matchStat = 4;
                    return str;
                } else {
                    this.matchStat = -1;
                    return this.stringDefaultValue;
                }
            } else {
                throw new JSONException("unclosed str, " + info());
            }
        }
    }

    public Date scanFieldDate(long j) {
        char c;
        int i;
        char c2;
        Date date;
        int i2;
        char c3;
        char c4;
        this.matchStat = 0;
        int matchFieldHash = matchFieldHash(j);
        if (matchFieldHash == 0) {
            return null;
        }
        int i3 = this.bp;
        char c5 = this.ch;
        int i4 = matchFieldHash + 1;
        int i5 = matchFieldHash + i3;
        int i6 = this.len;
        char c6 = EOI;
        if (i5 >= i6) {
            c = 26;
        } else {
            c = this.text.charAt(i5);
        }
        if (c == '\"') {
            int i7 = this.bp;
            int i8 = i7 + i4;
            int i9 = i4 + 1;
            int i10 = i7 + i4;
            if (i10 < this.len) {
                this.text.charAt(i10);
            }
            int indexOf = this.text.indexOf(34, this.bp + i9);
            if (indexOf != -1) {
                int i11 = indexOf - i8;
                this.bp = i8;
                if (scanISO8601DateIfMatch(false, i11)) {
                    date = this.calendar.getTime();
                    int i12 = i9 + i11;
                    i = i12 + 1;
                    c2 = charAt(i12 + i3);
                    this.bp = i3;
                } else {
                    this.bp = i3;
                    this.matchStat = -1;
                    return null;
                }
            } else {
                throw new JSONException("unclosed str");
            }
        } else if (c < '0' || c > '9') {
            this.matchStat = -1;
            return null;
        } else {
            long j2 = (long) (c - '0');
            while (true) {
                i2 = i4 + 1;
                int i13 = this.bp + i4;
                if (i13 >= this.len) {
                    c3 = 26;
                } else {
                    c3 = this.text.charAt(i13);
                }
                if (c3 < '0' || c3 > '9') {
                    break;
                }
                j2 = (j2 * 10) + ((long) (c3 - '0'));
                i4 = i2;
            }
            if (c3 == '.') {
                this.matchStat = -1;
                return null;
            }
            if (c3 == '\"') {
                int i14 = i2 + 1;
                int i15 = this.bp + i2;
                if (i15 >= this.len) {
                    c4 = 26;
                } else {
                    c4 = this.text.charAt(i15);
                }
                c2 = c4;
                i = i14;
            } else {
                c2 = c3;
                i = i2;
            }
            if (j2 < 0) {
                this.matchStat = -1;
                return null;
            }
            date = new Date(j2);
        }
        if (c2 == ',') {
            this.bp += i - 1;
            int i16 = this.bp + 1;
            this.bp = i16;
            if (i16 < this.len) {
                c6 = this.text.charAt(i16);
            }
            this.ch = c6;
            this.matchStat = 3;
            this.token = 16;
            return date;
        } else if (c2 == '}') {
            int i17 = i + 1;
            char charAt = charAt(this.bp + i);
            if (charAt == ',') {
                this.token = 16;
                this.bp += i17 - 1;
                int i18 = this.bp + 1;
                this.bp = i18;
                if (i18 < this.len) {
                    c6 = this.text.charAt(i18);
                }
                this.ch = c6;
            } else if (charAt == ']') {
                this.token = 15;
                this.bp += i17 - 1;
                int i19 = this.bp + 1;
                this.bp = i19;
                if (i19 < this.len) {
                    c6 = this.text.charAt(i19);
                }
                this.ch = c6;
            } else if (charAt == '}') {
                this.token = 13;
                this.bp += i17 - 1;
                int i20 = this.bp + 1;
                this.bp = i20;
                if (i20 < this.len) {
                    c6 = this.text.charAt(i20);
                }
                this.ch = c6;
            } else if (charAt == 26) {
                this.token = 20;
                this.bp += i17 - 1;
                this.ch = EOI;
            } else {
                this.bp = i3;
                this.ch = c5;
                this.matchStat = -1;
                return null;
            }
            this.matchStat = 4;
            return date;
        } else {
            this.bp = i3;
            this.ch = c5;
            this.matchStat = -1;
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0097  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0099  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00b5  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c2  */
    public boolean scanFieldBoolean(long j) {
        boolean z;
        int i;
        int i2;
        int i3;
        char c;
        int i4;
        this.matchStat = 0;
        int matchFieldHash = matchFieldHash(j);
        if (matchFieldHash == 0) {
            return false;
        }
        if (this.text.startsWith("false", this.bp + matchFieldHash)) {
            i = matchFieldHash + 5;
        } else {
            if (this.text.startsWith("true", this.bp + matchFieldHash)) {
                i = matchFieldHash + 4;
            } else if (this.text.startsWith("\"false\"", this.bp + matchFieldHash)) {
                i = matchFieldHash + 7;
            } else if (this.text.startsWith("\"true\"", this.bp + matchFieldHash)) {
                i = matchFieldHash + 6;
            } else if (this.text.charAt(this.bp + matchFieldHash) == '1') {
                i = matchFieldHash + 1;
            } else if (this.text.charAt(this.bp + matchFieldHash) == '0') {
                i = matchFieldHash + 1;
            } else if (this.text.startsWith("\"1\"", this.bp + matchFieldHash)) {
                i = matchFieldHash + 3;
            } else if (this.text.startsWith("\"0\"", this.bp + matchFieldHash)) {
                i = matchFieldHash + 3;
            } else {
                this.matchStat = -1;
                return false;
            }
            z = true;
            int i5 = i + 1;
            i2 = this.bp + i;
            i3 = this.len;
            char c2 = EOI;
            if (i2 < i3) {
                c = 26;
            } else {
                c = this.text.charAt(i2);
            }
            while (c != ',') {
                if (c != '}' && (c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == '\f' || c == '\b')) {
                    int i6 = i5 + 1;
                    int i7 = this.bp + i5;
                    if (i7 >= this.len) {
                        c = 26;
                    } else {
                        c = this.text.charAt(i7);
                    }
                    i5 = i6;
                } else if (c == '}') {
                    int i8 = i5 + 1;
                    char charAt = charAt(this.bp + i5);
                    if (charAt == ',') {
                        this.token = 16;
                        this.bp += i8 - 1;
                        int i9 = this.bp + 1;
                        this.bp = i9;
                        if (i9 < this.len) {
                            c2 = this.text.charAt(i9);
                        }
                        this.ch = c2;
                    } else if (charAt == ']') {
                        this.token = 15;
                        this.bp += i8 - 1;
                        int i10 = this.bp + 1;
                        this.bp = i10;
                        if (i10 < this.len) {
                            c2 = this.text.charAt(i10);
                        }
                        this.ch = c2;
                    } else if (charAt == '}') {
                        this.token = 13;
                        this.bp += i8 - 1;
                        int i11 = this.bp + 1;
                        this.bp = i11;
                        if (i11 < this.len) {
                            c2 = this.text.charAt(i11);
                        }
                        this.ch = c2;
                    } else if (charAt == 26) {
                        this.token = 20;
                        this.bp += i8 - 1;
                        this.ch = EOI;
                    } else {
                        this.matchStat = -1;
                        return false;
                    }
                    this.matchStat = 4;
                    return z;
                } else {
                    this.matchStat = -1;
                    return false;
                }
            }
            this.bp += i5 - 1;
            i4 = this.bp + 1;
            this.bp = i4;
            if (i4 < this.len) {
                c2 = this.text.charAt(i4);
            }
            this.ch = c2;
            this.matchStat = 3;
            this.token = 16;
            return z;
        }
        z = false;
        int i52 = i + 1;
        i2 = this.bp + i;
        i3 = this.len;
        char c22 = EOI;
        if (i2 < i3) {
        }
        while (c != ',') {
        }
        this.bp += i52 - 1;
        i4 = this.bp + 1;
        this.bp = i4;
        if (i4 < this.len) {
        }
        this.ch = c22;
        this.matchStat = 3;
        this.token = 16;
        return z;
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    public final float scanFieldFloat(long r18) {
        /*
        // Method dump skipped, instructions count: 326
        */
        throw new UnsupportedOperationException("Method not decompiled: ohos.utils.fastjson.parser.JSONLexer.scanFieldFloat(long):float");
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0124  */
    public final float[] scanFieldFloatArray(long r19) {
        /*
        // Method dump skipped, instructions count: 538
        */
        throw new UnsupportedOperationException("Method not decompiled: ohos.utils.fastjson.parser.JSONLexer.scanFieldFloatArray(long):float[]");
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x013b  */
    public final float[][] scanFieldFloatArray2(long r21) {
        /*
        // Method dump skipped, instructions count: 663
        */
        throw new UnsupportedOperationException("Method not decompiled: ohos.utils.fastjson.parser.JSONLexer.scanFieldFloatArray2(long):float[][]");
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    public final double scanFieldDouble(long r18) {
        /*
        // Method dump skipped, instructions count: 332
        */
        throw new UnsupportedOperationException("Method not decompiled: ohos.utils.fastjson.parser.JSONLexer.scanFieldDouble(long):double");
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0124  */
    public final double[] scanFieldDoubleArray(long r19) {
        /*
        // Method dump skipped, instructions count: 538
        */
        throw new UnsupportedOperationException("Method not decompiled: ohos.utils.fastjson.parser.JSONLexer.scanFieldDoubleArray(long):double[]");
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x013b  */
    public final double[][] scanFieldDoubleArray2(long r21) {
        /*
        // Method dump skipped, instructions count: 662
        */
        throw new UnsupportedOperationException("Method not decompiled: ohos.utils.fastjson.parser.JSONLexer.scanFieldDoubleArray2(long):double[][]");
    }

    public long scanFieldSymbol(long j) {
        char c;
        char c2;
        char c3;
        char c4;
        this.matchStat = 0;
        int matchFieldHash = matchFieldHash(j);
        if (matchFieldHash == 0) {
            return 0;
        }
        int i = matchFieldHash + 1;
        int i2 = this.bp + matchFieldHash;
        int i3 = this.len;
        char c5 = EOI;
        if (i2 >= i3) {
            c = 26;
        } else {
            c = this.text.charAt(i2);
        }
        if (c != '\"') {
            this.matchStat = -1;
            return 0;
        }
        long j2 = -3750763034362895579L;
        int i4 = this.bp;
        while (true) {
            int i5 = i + 1;
            int i6 = this.bp + i;
            if (i6 >= this.len) {
                c2 = 26;
            } else {
                c2 = this.text.charAt(i6);
            }
            if (c2 == '\"') {
                int i7 = i5 + 1;
                int i8 = this.bp + i5;
                if (i8 >= this.len) {
                    c3 = 26;
                } else {
                    c3 = this.text.charAt(i8);
                }
                if (c3 == ',') {
                    this.bp += i7 - 1;
                    int i9 = this.bp + 1;
                    this.bp = i9;
                    if (i9 < this.len) {
                        c5 = this.text.charAt(i9);
                    }
                    this.ch = c5;
                    this.matchStat = 3;
                    return j2;
                } else if (c3 == '}') {
                    int i10 = i7 + 1;
                    int i11 = this.bp + i7;
                    if (i11 >= this.len) {
                        c4 = 26;
                    } else {
                        c4 = this.text.charAt(i11);
                    }
                    if (c4 == ',') {
                        this.token = 16;
                        this.bp += i10 - 1;
                        next();
                    } else if (c4 == ']') {
                        this.token = 15;
                        this.bp += i10 - 1;
                        next();
                    } else if (c4 == '}') {
                        this.token = 13;
                        this.bp += i10 - 1;
                        next();
                    } else if (c4 == 26) {
                        this.token = 20;
                        this.bp += i10 - 1;
                        this.ch = EOI;
                    } else {
                        this.matchStat = -1;
                        return 0;
                    }
                    this.matchStat = 4;
                    return j2;
                } else {
                    this.matchStat = -1;
                    return 0;
                }
            } else {
                j2 = (j2 ^ ((long) c2)) * 1099511628211L;
                if (c2 == '\\') {
                    this.matchStat = -1;
                    return 0;
                }
                i = i5;
            }
        }
    }

    public boolean scanISO8601DateIfMatch(boolean z) {
        return scanISO8601DateIfMatch(z, this.len - this.bp);
    }

    /* JADX WARNING: Removed duplicated region for block: B:111:0x0225 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0226  */
    public boolean scanISO8601DateIfMatch(boolean z, int i) {
        char c;
        char c2;
        char c3;
        char c4;
        int i2;
        int i3;
        int i4;
        char c5;
        char c6;
        int i5;
        char c7;
        char c8;
        char c9;
        char charAt;
        int i6;
        int i7;
        char charAt2;
        char charAt3;
        char charAt4;
        char charAt5;
        if (!z && i > 13) {
            char charAt6 = charAt(this.bp);
            char charAt7 = charAt(this.bp + 1);
            char charAt8 = charAt(this.bp + 2);
            char charAt9 = charAt(this.bp + 3);
            char charAt10 = charAt(this.bp + 4);
            char charAt11 = charAt(this.bp + 5);
            char charAt12 = charAt((this.bp + i) - 1);
            char charAt13 = charAt((this.bp + i) - 2);
            if (charAt6 == '/' && charAt7 == 'D' && charAt8 == 'a' && charAt9 == 't' && charAt10 == 'e' && charAt11 == '(' && charAt12 == '/' && charAt13 == ')') {
                int i8 = -1;
                for (int i9 = 6; i9 < i; i9++) {
                    char charAt14 = charAt(this.bp + i9);
                    if (charAt14 != '+') {
                        if (charAt14 < '0' || charAt14 > '9') {
                            break;
                        }
                    } else {
                        i8 = i9;
                    }
                }
                if (i8 == -1) {
                    return false;
                }
                int i10 = this.bp + 6;
                long parseLong = Long.parseLong(subString(i10, i8 - i10));
                this.calendar = Calendar.getInstance(this.timeZone, this.locale);
                this.calendar.setTimeInMillis(parseLong);
                this.token = 5;
                return true;
            }
        }
        if (i == 8 || i == 14 || ((i == 16 && ((charAt5 = charAt(this.bp + 10)) == 'T' || charAt5 == ' ')) || (i == 17 && charAt(this.bp + 6) != '-'))) {
            int i11 = 0;
            if (z) {
                return false;
            }
            char charAt15 = charAt(this.bp);
            char charAt16 = charAt(this.bp + 1);
            char charAt17 = charAt(this.bp + 2);
            char charAt18 = charAt(this.bp + 3);
            char charAt19 = charAt(this.bp + 4);
            char charAt20 = charAt(this.bp + 5);
            char charAt21 = charAt(this.bp + 6);
            char charAt22 = charAt(this.bp + 7);
            char charAt23 = charAt(this.bp + 8);
            boolean z2 = charAt19 == '-' && charAt22 == '-';
            boolean z3 = z2 && i == 16;
            boolean z4 = z2 && i == 17;
            if (z4 || z3) {
                c = charAt(this.bp + 9);
                c4 = charAt20;
                c3 = charAt21;
                c2 = charAt23;
            } else {
                c4 = charAt19;
                c3 = charAt20;
                c2 = charAt21;
                c = charAt22;
            }
            if (!checkDate(charAt15, charAt16, charAt17, charAt18, c4, c3, c2, c)) {
                return false;
            }
            setCalendar(charAt15, charAt16, charAt17, charAt18, c4, c3, c2, c);
            if (i != 8) {
                char charAt24 = charAt(this.bp + 9);
                char charAt25 = charAt(this.bp + 10);
                char charAt26 = charAt(this.bp + 11);
                char charAt27 = charAt(this.bp + 12);
                char charAt28 = charAt(this.bp + 13);
                if (!(z4 && charAt25 == 'T' && charAt28 == ':' && charAt(this.bp + 16) == 'Z') && (!z3 || !((charAt25 == ' ' || charAt25 == 'T') && charAt28 == ':'))) {
                    c5 = charAt28;
                    c6 = charAt26;
                    charAt26 = charAt23;
                } else {
                    charAt25 = charAt(this.bp + 14);
                    c6 = charAt(this.bp + 15);
                    charAt24 = charAt27;
                    c5 = '0';
                    charAt27 = '0';
                }
                if (!checkTime(charAt26, charAt24, charAt25, c6, charAt27, c5)) {
                    return false;
                }
                if (i != 17 || z4) {
                    i2 = 0;
                } else {
                    char charAt29 = charAt(this.bp + 14);
                    char charAt30 = charAt(this.bp + 15);
                    char charAt31 = charAt(this.bp + 16);
                    if (charAt29 < '0' || charAt29 > '9' || charAt30 < '0' || charAt30 > '9' || charAt31 < '0' || charAt31 > '9') {
                        return false;
                    }
                    i2 = ((charAt29 - '0') * 100) + ((charAt30 - '0') * 10) + (charAt31 - '0');
                }
                int i12 = (charAt24 - '0') + ((charAt26 - '0') * 10);
                i4 = ((charAt25 - '0') * 10) + (c6 - '0');
                i3 = ((charAt27 - '0') * 10) + (c5 - '0');
                i11 = i12;
            } else {
                i4 = 0;
                i3 = 0;
                i2 = 0;
            }
            this.calendar.set(11, i11);
            this.calendar.set(12, i4);
            this.calendar.set(13, i3);
            this.calendar.set(14, i2);
            this.token = 5;
            return true;
        } else if (i < 9) {
            return false;
        } else {
            char charAt32 = charAt(this.bp);
            char charAt33 = charAt(this.bp + 1);
            char charAt34 = charAt(this.bp + 2);
            char charAt35 = charAt(this.bp + 3);
            char charAt36 = charAt(this.bp + 4);
            char charAt37 = charAt(this.bp + 5);
            char charAt38 = charAt(this.bp + 6);
            char charAt39 = charAt(this.bp + 7);
            char charAt40 = charAt(this.bp + 8);
            char charAt41 = charAt(this.bp + 9);
            if ((charAt36 == '-' && charAt39 == '-') || (charAt36 == '/' && charAt39 == '/')) {
                charAt39 = charAt33;
                c9 = charAt38;
                c7 = charAt41;
                c8 = charAt40;
                i5 = 10;
            } else {
                if (charAt36 == '-' && charAt38 == '-') {
                    if (charAt40 == ' ') {
                        charAt38 = charAt32;
                        charAt40 = charAt34;
                        charAt41 = charAt35;
                        c9 = charAt37;
                        c7 = charAt39;
                        charAt37 = '0';
                        c8 = '0';
                        i5 = 8;
                    } else {
                        charAt38 = charAt32;
                        charAt41 = charAt35;
                        c9 = charAt37;
                        c8 = charAt39;
                        c7 = charAt40;
                        charAt37 = '0';
                        i5 = 9;
                        charAt39 = charAt33;
                        charAt40 = charAt34;
                        if (checkDate(charAt38, charAt39, charAt40, charAt41, charAt37, c9, c8, c7)) {
                        }
                    }
                } else if ((charAt34 == '.' && charAt37 == '.') || (charAt34 == '-' && charAt37 == '-')) {
                    c8 = charAt32;
                    c7 = charAt33;
                    charAt37 = charAt35;
                    c9 = charAt36;
                    i5 = 10;
                    if (checkDate(charAt38, charAt39, charAt40, charAt41, charAt37, c9, c8, c7)) {
                    }
                } else if (charAt36 != 24180 && charAt36 != 45380) {
                    return false;
                } else {
                    if (charAt39 == 26376 || charAt39 == 50900) {
                        if (charAt41 == 26085 || charAt41 == 51068) {
                            charAt39 = charAt33;
                            charAt41 = charAt35;
                            c9 = charAt38;
                            c7 = charAt40;
                            i5 = 10;
                            c8 = '0';
                            charAt38 = charAt32;
                            charAt40 = charAt34;
                            if (checkDate(charAt38, charAt39, charAt40, charAt41, charAt37, c9, c8, c7)) {
                                return false;
                            }
                            setCalendar(charAt38, charAt39, charAt40, charAt41, charAt37, c9, c8, c7);
                            char charAt42 = charAt(this.bp + i5);
                            if (charAt42 == 'T' || (charAt42 == ' ' && !z)) {
                                int i13 = i5 + 9;
                                if (!(i >= i13 && charAt(this.bp + i5 + 3) == ':' && charAt(this.bp + i5 + 6) == ':')) {
                                    return false;
                                }
                                char charAt43 = charAt(this.bp + i5 + 1);
                                char charAt44 = charAt(this.bp + i5 + 2);
                                char charAt45 = charAt(this.bp + i5 + 4);
                                char charAt46 = charAt(this.bp + i5 + 5);
                                char charAt47 = charAt(this.bp + i5 + 7);
                                char charAt48 = charAt(this.bp + i5 + 8);
                                if (!checkTime(charAt43, charAt44, charAt45, charAt46, charAt47, charAt48)) {
                                    return false;
                                }
                                setTime(charAt43, charAt44, charAt45, charAt46, charAt47, charAt48);
                                char charAt49 = charAt(this.bp + i5 + 9);
                                if (charAt49 == '.') {
                                    int i14 = i5 + 11;
                                    if (i >= i14 && (charAt = charAt(this.bp + i5 + 10)) >= '0' && charAt <= '9') {
                                        int i15 = charAt - '0';
                                        if (i <= i14 || (charAt4 = charAt(this.bp + i5 + 11)) < '0' || charAt4 > '9') {
                                            i6 = 1;
                                        } else {
                                            i15 = (i15 * 10) + (charAt4 - '0');
                                            i6 = 2;
                                        }
                                        if (i6 == 2 && (charAt3 = charAt(this.bp + i5 + 12)) >= '0' && charAt3 <= '9') {
                                            i15 = (i15 * 10) + (charAt3 - '0');
                                            i6 = 3;
                                        }
                                        this.calendar.set(14, i15);
                                        char charAt50 = charAt(this.bp + i5 + 10 + i6);
                                        if (charAt50 == '+' || charAt50 == '-') {
                                            char charAt51 = charAt(this.bp + i5 + 10 + i6 + 1);
                                            if (charAt51 >= '0' && charAt51 <= '1' && (charAt2 = charAt(this.bp + i5 + 10 + i6 + 2)) >= '0' && charAt2 <= '9') {
                                                char charAt52 = charAt(this.bp + i5 + 10 + i6 + 3);
                                                if (charAt52 == ':') {
                                                    if (!(charAt(this.bp + i5 + 10 + i6 + 4) == '0' && charAt(this.bp + i5 + 10 + i6 + 5) == '0')) {
                                                        return false;
                                                    }
                                                    i7 = 6;
                                                } else if (charAt52 != '0') {
                                                    i7 = 3;
                                                } else if (charAt(this.bp + i5 + 10 + i6 + 4) != '0') {
                                                    return false;
                                                } else {
                                                    i7 = 5;
                                                }
                                                setTimeZone(charAt50, charAt51, charAt2);
                                            }
                                        } else if (charAt50 == 'Z') {
                                            if (this.calendar.getTimeZone().getRawOffset() != 0) {
                                                String[] availableIDs = TimeZone.getAvailableIDs(0);
                                                if (availableIDs.length > 0) {
                                                    this.calendar.setTimeZone(TimeZone.getTimeZone(availableIDs[0]));
                                                }
                                            }
                                            i7 = 1;
                                        } else {
                                            i7 = 0;
                                        }
                                        int i16 = i5 + 10 + i6 + i7;
                                        char charAt53 = charAt(this.bp + i16);
                                        if (!(charAt53 == 26 || charAt53 == '\"')) {
                                            return false;
                                        }
                                        int i17 = this.bp + i16;
                                        this.bp = i17;
                                        this.ch = charAt(i17);
                                        this.token = 5;
                                        return true;
                                    }
                                    return false;
                                }
                                this.calendar.set(14, 0);
                                int i18 = this.bp + i13;
                                this.bp = i18;
                                this.ch = charAt(i18);
                                this.token = 5;
                                if (charAt49 == 'Z' && this.calendar.getTimeZone().getRawOffset() != 0) {
                                    String[] availableIDs2 = TimeZone.getAvailableIDs(0);
                                    if (availableIDs2.length > 0) {
                                        this.calendar.setTimeZone(TimeZone.getTimeZone(availableIDs2[0]));
                                    }
                                }
                                return true;
                            } else if (charAt42 == '\"' || charAt42 == 26 || charAt42 == 26085 || charAt42 == 51068) {
                                this.calendar.set(11, 0);
                                this.calendar.set(12, 0);
                                this.calendar.set(13, 0);
                                this.calendar.set(14, 0);
                                int i19 = this.bp + i5;
                                this.bp = i19;
                                this.ch = charAt(i19);
                                this.token = 5;
                                return true;
                            } else if ((charAt42 != '+' && charAt42 != '-') || this.len != i5 + 6 || charAt(this.bp + i5 + 3) != ':' || charAt(this.bp + i5 + 4) != '0' || charAt(this.bp + i5 + 5) != '0') {
                                return false;
                            } else {
                                setTime('0', '0', '0', '0', '0', '0');
                                this.calendar.set(14, 0);
                                setTimeZone(charAt42, charAt(this.bp + i5 + 1), charAt(this.bp + i5 + 2));
                                return true;
                            }
                        } else if (charAt(this.bp + 10) != 26085 && charAt(this.bp + 10) != 51068) {
                            return false;
                        } else {
                            charAt39 = charAt33;
                            i5 = 11;
                            c9 = charAt38;
                            c7 = charAt41;
                            c8 = charAt40;
                        }
                    } else if (charAt38 != 26376 && charAt38 != 50900) {
                        return false;
                    } else {
                        if (charAt40 == 26085 || charAt40 == 51068) {
                            charAt38 = charAt32;
                            charAt40 = charAt34;
                            charAt41 = charAt35;
                            c9 = charAt37;
                            c7 = charAt39;
                            i5 = 10;
                            charAt37 = '0';
                            c8 = '0';
                        } else if (charAt41 != 26085 && charAt41 != 51068) {
                            return false;
                        } else {
                            charAt38 = charAt32;
                            charAt41 = charAt35;
                            c9 = charAt37;
                            c8 = charAt39;
                            c7 = charAt40;
                            i5 = 10;
                            charAt37 = '0';
                            charAt39 = charAt33;
                            charAt40 = charAt34;
                            if (checkDate(charAt38, charAt39, charAt40, charAt41, charAt37, c9, c8, c7)) {
                            }
                        }
                    }
                }
                charAt39 = charAt33;
                if (checkDate(charAt38, charAt39, charAt40, charAt41, charAt37, c9, c8, c7)) {
                }
            }
            charAt38 = charAt32;
            charAt40 = charAt34;
            charAt41 = charAt35;
            if (checkDate(charAt38, charAt39, charAt40, charAt41, charAt37, c9, c8, c7)) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setTime(char c, char c2, char c3, char c4, char c5, char c6) {
        this.calendar.set(11, ((c - '0') * 10) + (c2 - '0'));
        this.calendar.set(12, ((c3 - '0') * 10) + (c4 - '0'));
        this.calendar.set(13, ((c5 - '0') * 10) + (c6 - '0'));
    }

    /* access modifiers changed from: protected */
    public void setTimeZone(char c, char c2, char c3) {
        int i = (((c2 - '0') * 10) + (c3 - '0')) * SystemAbilityDefinition.SUBSYS_SENSORS_SYS_ABILITY_ID_BEGIN * 1000;
        if (c == '-') {
            i = -i;
        }
        if (this.calendar.getTimeZone().getRawOffset() != i) {
            String[] availableIDs = TimeZone.getAvailableIDs(i);
            if (availableIDs.length > 0) {
                this.calendar.setTimeZone(TimeZone.getTimeZone(availableIDs[0]));
            }
        }
    }

    private void setCalendar(char c, char c2, char c3, char c4, char c5, char c6, char c7, char c8) {
        this.calendar = Calendar.getInstance(this.timeZone, this.locale);
        this.calendar.set(1, ((c - '0') * 1000) + ((c2 - '0') * 100) + ((c3 - '0') * 10) + (c4 - '0'));
        this.calendar.set(2, (((c5 - '0') * 10) + (c6 - '0')) - 1);
        this.calendar.set(5, ((c7 - '0') * 10) + (c8 - '0'));
    }

    public static final byte[] decodeFast(String str, int i, int i2) {
        int i3;
        int i4 = 0;
        if (i2 == 0) {
            return new byte[0];
        }
        int i5 = (i + i2) - 1;
        while (i < i5 && IA[str.charAt(i)] < 0) {
            i++;
        }
        while (i5 > 0 && IA[str.charAt(i5)] < 0) {
            i5--;
        }
        int i6 = str.charAt(i5) == '=' ? str.charAt(i5 + -1) == '=' ? 2 : 1 : 0;
        int i7 = (i5 - i) + 1;
        if (i2 > 76) {
            i3 = (str.charAt(76) == '\r' ? i7 / 78 : 0) << 1;
        } else {
            i3 = 0;
        }
        int i8 = (((i7 - i3) * 6) >> 3) - i6;
        byte[] bArr = new byte[i8];
        int i9 = (i8 / 3) * 3;
        int i10 = i;
        int i11 = 0;
        int i12 = 0;
        while (i11 < i9) {
            int i13 = i10 + 1;
            int i14 = i13 + 1;
            int i15 = i14 + 1;
            int i16 = i15 + 1;
            int i17 = (IA[str.charAt(i10)] << 18) | (IA[str.charAt(i13)] << 12) | (IA[str.charAt(i14)] << 6) | IA[str.charAt(i15)];
            int i18 = i11 + 1;
            bArr[i11] = (byte) (i17 >> 16);
            int i19 = i18 + 1;
            bArr[i18] = (byte) (i17 >> 8);
            int i20 = i19 + 1;
            bArr[i19] = (byte) i17;
            if (i3 > 0 && (i12 = i12 + 1) == 19) {
                i16 += 2;
                i12 = 0;
            }
            i10 = i16;
            i11 = i20;
        }
        if (i11 < i8) {
            int i21 = 0;
            while (i10 <= i5 - i6) {
                i4 |= IA[str.charAt(i10)] << (18 - (i21 * 6));
                i21++;
                i10++;
            }
            int i22 = 16;
            while (i11 < i8) {
                bArr[i11] = (byte) (i4 >> i22);
                i22 -= 8;
                i11++;
            }
        }
        return bArr;
    }
}
