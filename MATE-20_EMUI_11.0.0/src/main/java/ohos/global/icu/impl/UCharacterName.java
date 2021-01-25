package ohos.global.icu.impl;

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.UnicodeSet;

public final class UCharacterName {
    static final int EXTENDED_CATEGORY_ = 33;
    private static final String FILE_NAME_ = "unames.icu";
    private static final int GROUP_MASK_ = 31;
    private static final int GROUP_SHIFT_ = 5;
    public static final UCharacterName INSTANCE;
    private static final int LEAD_SURROGATE_ = 31;
    public static final int LINES_PER_GROUP_ = 32;
    private static final int NON_CHARACTER_ = 30;
    private static final int OFFSET_HIGH_OFFSET_ = 1;
    private static final int OFFSET_LOW_OFFSET_ = 2;
    private static final int SINGLE_NIBBLE_MAX_ = 11;
    private static final int TRAIL_SURROGATE_ = 32;
    private static final String[] TYPE_NAMES_ = {"unassigned", "uppercase letter", "lowercase letter", "titlecase letter", "modifier letter", "other letter", "non spacing mark", "enclosing mark", "combining spacing mark", "decimal digit number", "letter number", "other number", "space separator", "line separator", "paragraph separator", "control", "format", "private use area", "surrogate", "dash punctuation", "start punctuation", "end punctuation", "connector punctuation", "other punctuation", "math symbol", "currency symbol", "modifier symbol", "other symbol", "initial punctuation", "final punctuation", "noncharacter", "lead surrogate", "trail surrogate"};
    private static final String UNKNOWN_TYPE_NAME_ = "unknown";
    private int[] m_ISOCommentSet_ = new int[8];
    private AlgorithmName[] m_algorithm_;
    public int m_groupcount_ = 0;
    private char[] m_groupinfo_;
    private char[] m_grouplengths_ = new char[33];
    private char[] m_groupoffsets_ = new char[33];
    int m_groupsize_ = 0;
    private byte[] m_groupstring_;
    private int m_maxISOCommentLength_;
    private int m_maxNameLength_;
    private int[] m_nameSet_ = new int[8];
    private byte[] m_tokenstring_;
    private char[] m_tokentable_;
    private int[] m_utilIntBuffer_ = new int[2];
    private StringBuffer m_utilStringBuffer_ = new StringBuffer();

    public static int getCodepointMSB(int i) {
        return i >> 5;
    }

    public static int getGroupLimit(int i) {
        return (i << 5) + 32;
    }

    public static int getGroupMin(int i) {
        return i << 5;
    }

    public static int getGroupMinFromCodepoint(int i) {
        return i & -32;
    }

    public static int getGroupOffset(int i) {
        return i & 31;
    }

    static {
        try {
            INSTANCE = new UCharacterName();
        } catch (IOException unused) {
            throw new MissingResourceException("Could not construct UCharacterName. Missing unames.icu", "", "");
        }
    }

    public String getName(int i, int i2) {
        if (i < 0 || i > 1114111 || i2 > 4) {
            return null;
        }
        String algName = getAlgName(i, i2);
        if (algName != null && algName.length() != 0) {
            return algName;
        }
        if (i2 == 2) {
            return getExtendedName(i);
        }
        return getGroupName(i, i2);
    }

    public int getCharFromName(int i, String str) {
        if (i >= 4 || str == null || str.length() == 0) {
            return -1;
        }
        int extendedChar = getExtendedChar(str.toLowerCase(Locale.ENGLISH), i);
        if (extendedChar >= -1) {
            return extendedChar;
        }
        String upperCase = str.toUpperCase(Locale.ENGLISH);
        if (i == 0 || i == 2) {
            AlgorithmName[] algorithmNameArr = this.m_algorithm_;
            for (int length = (algorithmNameArr != null ? algorithmNameArr.length : 0) - 1; length >= 0; length--) {
                int i2 = this.m_algorithm_[length].getChar(upperCase);
                if (i2 >= 0) {
                    return i2;
                }
            }
        }
        if (i != 2) {
            return getGroupChar(upperCase, i);
        }
        int groupChar = getGroupChar(upperCase, 0);
        if (groupChar == -1) {
            return getGroupChar(upperCase, 3);
        }
        return groupChar;
    }

    public int getGroupLengths(int i, char[] cArr, char[] cArr2) {
        int i2 = i * this.m_groupsize_;
        char[] cArr3 = this.m_groupinfo_;
        int i3 = UCharacterUtility.toInt(cArr3[i2 + 1], cArr3[i2 + 2]);
        int i4 = 0;
        cArr[0] = 0;
        char c = 65535;
        while (i4 < 32) {
            byte b = this.m_groupstring_[i3];
            char c2 = c;
            int i5 = i4;
            for (int i6 = 4; i6 >= 0; i6 -= 4) {
                byte b2 = (byte) ((b >> i6) & 15);
                if (c2 != 65535 || b2 <= 11) {
                    if (c2 != 65535) {
                        cArr2[i5] = (char) ((c2 | b2) + 12);
                    } else {
                        cArr2[i5] = (char) b2;
                    }
                    if (i5 < 32) {
                        cArr[i5 + 1] = (char) (cArr[i5] + cArr2[i5]);
                    }
                    i5++;
                    c2 = 65535;
                } else {
                    c2 = (char) ((b2 - 12) << 4);
                }
            }
            i3++;
            i4 = i5;
            c = c2;
        }
        return i3;
    }

    public String getGroupName(int i, int i2, int i3) {
        int i4 = 0;
        if (!(i3 == 0 || i3 == 2)) {
            char[] cArr = this.m_tokentable_;
            if (59 >= cArr.length || cArr[59] == 65535) {
                int i5 = i3 == 4 ? 2 : i3;
                do {
                    int skipByteSubString = UCharacterUtility.skipByteSubString(this.m_groupstring_, i, i2, (byte) 59) + i;
                    i2 -= skipByteSubString - i;
                    i5--;
                    i = skipByteSubString;
                } while (i5 > 0);
            } else {
                i2 = 0;
            }
        }
        synchronized (this.m_utilStringBuffer_) {
            this.m_utilStringBuffer_.setLength(0);
            while (true) {
                if (i4 >= i2) {
                    break;
                }
                byte b = this.m_groupstring_[i + i4];
                i4++;
                if (b < this.m_tokentable_.length) {
                    int i6 = b & 255;
                    char c = this.m_tokentable_[i6];
                    if (c == 65534) {
                        c = this.m_tokentable_[(b << 8) | (this.m_groupstring_[i + i4] & 255)];
                        i4++;
                    }
                    if (c != 65535) {
                        UCharacterUtility.getNullTermByteSubString(this.m_utilStringBuffer_, this.m_tokenstring_, c);
                    } else if (b == 59) {
                        if (this.m_utilStringBuffer_.length() != 0 || i3 != 2) {
                            break;
                        }
                    } else {
                        this.m_utilStringBuffer_.append((char) i6);
                    }
                } else if (b == 59) {
                    break;
                } else {
                    this.m_utilStringBuffer_.append((int) b);
                }
            }
            if (this.m_utilStringBuffer_.length() <= 0) {
                return null;
            }
            return this.m_utilStringBuffer_.toString();
        }
    }

    public String getExtendedName(int i) {
        String name = getName(i, 0);
        return name == null ? getExtendedOr10Name(i) : name;
    }

    public int getGroup(int i) {
        int i2 = this.m_groupcount_;
        int codepointMSB = getCodepointMSB(i);
        int i3 = 0;
        while (i3 < i2 - 1) {
            int i4 = (i3 + i2) >> 1;
            if (codepointMSB < getGroupMSB(i4)) {
                i2 = i4;
            } else {
                i3 = i4;
            }
        }
        return i3;
    }

    public String getExtendedOr10Name(int i) {
        String str;
        String stringBuffer;
        int type = getType(i);
        String[] strArr = TYPE_NAMES_;
        if (type >= strArr.length) {
            str = UNKNOWN_TYPE_NAME_;
        } else {
            str = strArr[type];
        }
        synchronized (this.m_utilStringBuffer_) {
            this.m_utilStringBuffer_.setLength(0);
            this.m_utilStringBuffer_.append('<');
            this.m_utilStringBuffer_.append(str);
            this.m_utilStringBuffer_.append(LocaleUtility.IETF_SEPARATOR);
            String upperCase = Integer.toHexString(i).toUpperCase(Locale.ENGLISH);
            for (int length = 4 - upperCase.length(); length > 0; length--) {
                this.m_utilStringBuffer_.append('0');
            }
            this.m_utilStringBuffer_.append(upperCase);
            this.m_utilStringBuffer_.append('>');
            stringBuffer = this.m_utilStringBuffer_.toString();
        }
        return stringBuffer;
    }

    public int getGroupMSB(int i) {
        if (i >= this.m_groupcount_) {
            return -1;
        }
        return this.m_groupinfo_[i * this.m_groupsize_];
    }

    public int getAlgorithmLength() {
        return this.m_algorithm_.length;
    }

    public int getAlgorithmStart(int i) {
        return this.m_algorithm_[i].m_rangestart_;
    }

    public int getAlgorithmEnd(int i) {
        return this.m_algorithm_[i].m_rangeend_;
    }

    public String getAlgorithmName(int i, int i2) {
        String stringBuffer;
        synchronized (this.m_utilStringBuffer_) {
            this.m_utilStringBuffer_.setLength(0);
            this.m_algorithm_[i].appendName(i2, this.m_utilStringBuffer_);
            stringBuffer = this.m_utilStringBuffer_.toString();
        }
        return stringBuffer;
    }

    public synchronized String getGroupName(int i, int i2) {
        int codepointMSB = getCodepointMSB(i);
        int group = getGroup(i);
        if (codepointMSB != this.m_groupinfo_[this.m_groupsize_ * group]) {
            return null;
        }
        int i3 = i & 31;
        return getGroupName(getGroupLengths(group, this.m_groupoffsets_, this.m_grouplengths_) + this.m_groupoffsets_[i3], this.m_grouplengths_[i3], i2);
    }

    public int getMaxCharNameLength() {
        if (initNameSetsLengths()) {
            return this.m_maxNameLength_;
        }
        return 0;
    }

    public int getMaxISOCommentLength() {
        if (initNameSetsLengths()) {
            return this.m_maxISOCommentLength_;
        }
        return 0;
    }

    public void getCharNameCharacters(UnicodeSet unicodeSet) {
        convert(this.m_nameSet_, unicodeSet);
    }

    public void getISOCommentCharacters(UnicodeSet unicodeSet) {
        convert(this.m_ISOCommentSet_, unicodeSet);
    }

    /* access modifiers changed from: package-private */
    public static final class AlgorithmName {
        static final int TYPE_0_ = 0;
        static final int TYPE_1_ = 1;
        private char[] m_factor_;
        private byte[] m_factorstring_;
        private String m_prefix_;
        private int m_rangeend_;
        private int m_rangestart_;
        private byte m_type_;
        private int[] m_utilIntBuffer_ = new int[256];
        private StringBuffer m_utilStringBuffer_ = new StringBuffer();
        private byte m_variant_;

        AlgorithmName() {
        }

        /* access modifiers changed from: package-private */
        public boolean setInfo(int i, int i2, byte b, byte b2) {
            if (i < 0 || i > i2 || i2 > 1114111) {
                return false;
            }
            if (b != 0 && b != 1) {
                return false;
            }
            this.m_rangestart_ = i;
            this.m_rangeend_ = i2;
            this.m_type_ = b;
            this.m_variant_ = b2;
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean setFactor(char[] cArr) {
            if (cArr.length != this.m_variant_) {
                return false;
            }
            this.m_factor_ = cArr;
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean setPrefix(String str) {
            if (str == null || str.length() <= 0) {
                return false;
            }
            this.m_prefix_ = str;
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean setFactorString(byte[] bArr) {
            this.m_factorstring_ = bArr;
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean contains(int i) {
            return this.m_rangestart_ <= i && i <= this.m_rangeend_;
        }

        /* access modifiers changed from: package-private */
        public void appendName(int i, StringBuffer stringBuffer) {
            stringBuffer.append(this.m_prefix_);
            byte b = this.m_type_;
            if (b == 0) {
                stringBuffer.append(Utility.hex((long) i, this.m_variant_));
            } else if (b == 1) {
                int i2 = i - this.m_rangestart_;
                int[] iArr = this.m_utilIntBuffer_;
                synchronized (iArr) {
                    for (int i3 = this.m_variant_ - 1; i3 > 0; i3--) {
                        int i4 = this.m_factor_[i3] & 255;
                        iArr[i3] = i2 % i4;
                        i2 /= i4;
                    }
                    iArr[0] = i2;
                    stringBuffer.append(getFactorString(iArr, this.m_variant_));
                }
            }
        }

        /* access modifiers changed from: package-private */
        public int getChar(String str) {
            int length = this.m_prefix_.length();
            if (str.length() >= length && this.m_prefix_.equals(str.substring(0, length))) {
                byte b = this.m_type_;
                if (b == 0) {
                    try {
                        int parseInt = Integer.parseInt(str.substring(length), 16);
                        if (this.m_rangestart_ <= parseInt && parseInt <= this.m_rangeend_) {
                            return parseInt;
                        }
                    } catch (NumberFormatException unused) {
                    }
                } else if (b == 1) {
                    for (int i = this.m_rangestart_; i <= this.m_rangeend_; i++) {
                        int i2 = i - this.m_rangestart_;
                        int[] iArr = this.m_utilIntBuffer_;
                        synchronized (iArr) {
                            for (int i3 = this.m_variant_ - 1; i3 > 0; i3--) {
                                int i4 = this.m_factor_[i3] & 255;
                                iArr[i3] = i2 % i4;
                                i2 /= i4;
                            }
                            iArr[0] = i2;
                            if (compareFactorString(iArr, this.m_variant_, str, length)) {
                                return i;
                            }
                        }
                    }
                }
            }
            return -1;
        }

        /* access modifiers changed from: package-private */
        public int add(int[] iArr, int i) {
            int add = UCharacterName.add(iArr, this.m_prefix_);
            byte b = this.m_type_;
            if (b == 0) {
                add += this.m_variant_;
            } else if (b == 1) {
                for (int i2 = this.m_variant_ - 1; i2 > 0; i2--) {
                    int i3 = 0;
                    int i4 = 0;
                    char c = this.m_factor_[i2];
                    while (c > 0) {
                        synchronized (this.m_utilStringBuffer_) {
                            this.m_utilStringBuffer_.setLength(0);
                            i4 = UCharacterUtility.getNullTermByteSubString(this.m_utilStringBuffer_, this.m_factorstring_, i4);
                            UCharacterName.add(iArr, this.m_utilStringBuffer_);
                            if (this.m_utilStringBuffer_.length() > i3) {
                                i3 = this.m_utilStringBuffer_.length();
                            }
                        }
                        c = (c == 1 ? 1 : 0) - 1;
                    }
                    add += i3;
                }
            }
            return add > i ? add : i;
        }

        private String getFactorString(int[] iArr, int i) {
            String stringBuffer;
            int length = this.m_factor_.length;
            if (iArr == null || i != length) {
                return null;
            }
            synchronized (this.m_utilStringBuffer_) {
                this.m_utilStringBuffer_.setLength(0);
                int i2 = length - 1;
                int i3 = 0;
                for (int i4 = 0; i4 <= i2; i4++) {
                    char c = this.m_factor_[i4];
                    i3 = UCharacterUtility.getNullTermByteSubString(this.m_utilStringBuffer_, this.m_factorstring_, UCharacterUtility.skipNullTermByteSubString(this.m_factorstring_, i3, iArr[i4]));
                    if (i4 != i2) {
                        i3 = UCharacterUtility.skipNullTermByteSubString(this.m_factorstring_, i3, (c - iArr[i4]) - 1);
                    }
                }
                stringBuffer = this.m_utilStringBuffer_.toString();
            }
            return stringBuffer;
        }

        private boolean compareFactorString(int[] iArr, int i, String str, int i2) {
            int length = this.m_factor_.length;
            if (iArr == null || i != length) {
                return false;
            }
            int i3 = length - 1;
            int i4 = 0;
            for (int i5 = 0; i5 <= i3; i5++) {
                char c = this.m_factor_[i5];
                i4 = UCharacterUtility.skipNullTermByteSubString(this.m_factorstring_, i4, iArr[i5]);
                i2 = UCharacterUtility.compareNullTermByteSubString(str, this.m_factorstring_, i2, i4);
                if (i2 < 0) {
                    return false;
                }
                if (i5 != i3) {
                    i4 = UCharacterUtility.skipNullTermByteSubString(this.m_factorstring_, i4, c - iArr[i5]);
                }
            }
            if (i2 != str.length()) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean setToken(char[] cArr, byte[] bArr) {
        if (cArr == null || bArr == null || cArr.length <= 0 || bArr.length <= 0) {
            return false;
        }
        this.m_tokentable_ = cArr;
        this.m_tokenstring_ = bArr;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setAlgorithm(AlgorithmName[] algorithmNameArr) {
        if (algorithmNameArr == null || algorithmNameArr.length == 0) {
            return false;
        }
        this.m_algorithm_ = algorithmNameArr;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setGroupCountSize(int i, int i2) {
        if (i <= 0 || i2 <= 0) {
            return false;
        }
        this.m_groupcount_ = i;
        this.m_groupsize_ = i2;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setGroup(char[] cArr, byte[] bArr) {
        if (cArr == null || bArr == null || cArr.length <= 0 || bArr.length <= 0) {
            return false;
        }
        this.m_groupinfo_ = cArr;
        this.m_groupstring_ = bArr;
        return true;
    }

    private UCharacterName() throws IOException {
        new UCharacterNameReader(ICUBinary.getRequiredData(FILE_NAME_)).read(this);
    }

    private String getAlgName(int i, int i2) {
        if (!(i2 == 0 || i2 == 2)) {
            return null;
        }
        synchronized (this.m_utilStringBuffer_) {
            this.m_utilStringBuffer_.setLength(0);
            for (int length = this.m_algorithm_.length - 1; length >= 0; length--) {
                if (this.m_algorithm_[length].contains(i)) {
                    this.m_algorithm_[length].appendName(i, this.m_utilStringBuffer_);
                    return this.m_utilStringBuffer_.toString();
                }
            }
            return null;
        }
    }

    private synchronized int getGroupChar(String str, int i) {
        for (int i2 = 0; i2 < this.m_groupcount_; i2++) {
            int groupChar = getGroupChar(getGroupLengths(i2, this.m_groupoffsets_, this.m_grouplengths_), this.m_grouplengths_, str, i);
            if (groupChar != -1) {
                return (this.m_groupinfo_[i2 * this.m_groupsize_] << 5) | groupChar;
            }
        }
        return -1;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:49:0x0020 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:48:0x0031 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r18v0, types: [char[]] */
    /* JADX WARN: Type inference failed for: r7v1, types: [char] */
    /* JADX WARN: Type inference failed for: r7v2 */
    /* JADX WARN: Type inference failed for: r15v1 */
    /* JADX WARN: Type inference failed for: r13v2 */
    /* JADX WARN: Type inference failed for: r7v3, types: [int] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0054  */
    /* JADX WARNING: Unknown variable types count: 2 */
    private int getGroupChar(int i, char[] cArr, String str, int i2) {
        int i3;
        int i4;
        int i5;
        byte b;
        char[] cArr2;
        int length = str.length();
        int i6 = i;
        int i7 = 0;
        while (true) {
            int i8 = -1;
            if (i7 > 32) {
                return -1;
            }
            char c = cArr[i7];
            if (i2 != 0) {
                int i9 = 2;
                if (i2 != 2) {
                    if (i2 != 4) {
                        i9 = i2;
                    }
                    while (true) {
                        i3 = UCharacterUtility.skipByteSubString(this.m_groupstring_, i6, c == true ? 1 : 0, (byte) 59) + i6;
                        c -= i3 - i6;
                        i9--;
                        if (i9 <= 0) {
                            break;
                        }
                        i6 = i3;
                    }
                    i4 = 0;
                    i5 = 0;
                    while (i4 < c && i5 != i8 && i5 < length) {
                        byte[] bArr = this.m_groupstring_;
                        b = bArr[i3 + i4];
                        i4++;
                        cArr2 = this.m_tokentable_;
                        if (b < cArr2.length) {
                            i5 = str.charAt(i5) != (b & 255) ? i8 : i5 + 1;
                        } else {
                            ?? r15 = b & 255;
                            char c2 = cArr2[r15];
                            if (c2 == 65534) {
                                c2 = cArr2[(b << 8) | (bArr[i3 + i4] & 255)];
                                i4++;
                            }
                            if (c2 == 65535) {
                                int i10 = i5 + 1;
                                if (str.charAt(i5) != r15) {
                                    i8 = -1;
                                    i5 = -1;
                                } else {
                                    i5 = i10;
                                }
                            } else {
                                i5 = UCharacterUtility.compareNullTermByteSubString(str, this.m_tokenstring_, i5, c2);
                            }
                            i8 = -1;
                        }
                    }
                    if (length == i5 || !(i4 == c || this.m_groupstring_[i4 + i3] == 59)) {
                        i6 = i3 + (c == true ? 1 : 0);
                        i7++;
                    }
                }
            }
            i3 = i6;
            i4 = 0;
            i5 = 0;
            while (i4 < c) {
                byte[] bArr2 = this.m_groupstring_;
                b = bArr2[i3 + i4];
                i4++;
                cArr2 = this.m_tokentable_;
                if (b < cArr2.length) {
                }
            }
            if (length == i5) {
            }
            i6 = i3 + (c == true ? 1 : 0);
            i7++;
        }
        return i7;
    }

    private static int getType(int i) {
        if (UCharacterUtility.isNonCharacter(i)) {
            return 30;
        }
        int type = UCharacter.getType(i);
        if (type == 18) {
            return i <= 56319 ? 31 : 32;
        }
        return type;
    }

    private static int getExtendedChar(String str, int i) {
        int lastIndexOf;
        int i2;
        int i3;
        int i4 = 0;
        if (str.charAt(0) != '<') {
            return -2;
        }
        if (i == 2) {
            int length = str.length() - 1;
            if (str.charAt(length) == '>' && (lastIndexOf = str.lastIndexOf(45)) >= 0 && (i3 = length - (i2 = lastIndexOf + 1)) >= 1 && 8 >= i3) {
                try {
                    int parseInt = Integer.parseInt(str.substring(i2, length), 16);
                    if (parseInt >= 0 && 1114111 >= parseInt) {
                        int type = getType(parseInt);
                        String substring = str.substring(1, i2 - 1);
                        int length2 = TYPE_NAMES_.length;
                        while (true) {
                            if (i4 >= length2) {
                                break;
                            } else if (substring.compareTo(TYPE_NAMES_[i4]) != 0) {
                                i4++;
                            } else if (type == i4) {
                                return parseInt;
                            }
                        }
                    }
                } catch (NumberFormatException unused) {
                }
            }
        }
        return -1;
    }

    private static void add(int[] iArr, char c) {
        int i = c >>> 5;
        iArr[i] = (1 << (c & 31)) | iArr[i];
    }

    private static boolean contains(int[] iArr, char c) {
        return (iArr[c >>> 5] & (1 << (c & 31))) != 0;
    }

    /* access modifiers changed from: private */
    public static int add(int[] iArr, String str) {
        int length = str.length();
        for (int i = length - 1; i >= 0; i--) {
            add(iArr, str.charAt(i));
        }
        return length;
    }

    /* access modifiers changed from: private */
    public static int add(int[] iArr, StringBuffer stringBuffer) {
        int length = stringBuffer.length();
        for (int i = length - 1; i >= 0; i--) {
            add(iArr, stringBuffer.charAt(i));
        }
        return length;
    }

    private int addAlgorithmName(int i) {
        for (int length = this.m_algorithm_.length - 1; length >= 0; length--) {
            int add = this.m_algorithm_[length].add(this.m_nameSet_, i);
            if (add > i) {
                i = add;
            }
        }
        return i;
    }

    private int addExtendedName(int i) {
        for (int length = TYPE_NAMES_.length - 1; length >= 0; length--) {
            int add = add(this.m_nameSet_, TYPE_NAMES_[length]) + 9;
            if (add > i) {
                i = add;
            }
        }
        return i;
    }

    private int[] addGroupName(int i, int i2, byte[] bArr, int[] iArr) {
        int i3 = 0;
        int i4 = 0;
        while (i3 < i2) {
            byte[] bArr2 = this.m_groupstring_;
            char c = (char) (bArr2[i + i3] & 255);
            i3++;
            if (c == ';') {
                break;
            }
            char[] cArr = this.m_tokentable_;
            if (c >= cArr.length) {
                add(iArr, c);
            } else {
                char c2 = cArr[c & 255];
                if (c2 == 65534) {
                    c = (char) ((bArr2[i + i3] & 255) | (c << '\b'));
                    c2 = cArr[c];
                    i3++;
                }
                if (c2 == 65535) {
                    add(iArr, c);
                } else {
                    byte b = bArr[c];
                    if (b == 0) {
                        synchronized (this.m_utilStringBuffer_) {
                            this.m_utilStringBuffer_.setLength(0);
                            UCharacterUtility.getNullTermByteSubString(this.m_utilStringBuffer_, this.m_tokenstring_, c2);
                            b = (byte) add(iArr, this.m_utilStringBuffer_);
                        }
                        bArr[c] = b;
                    }
                    i4 += b;
                }
            }
            i4++;
        }
        int[] iArr2 = this.m_utilIntBuffer_;
        iArr2[0] = i4;
        iArr2[1] = i3;
        return iArr2;
    }

    private void addGroupName(int i) {
        char[] cArr = new char[34];
        char[] cArr2 = new char[34];
        byte[] bArr = new byte[this.m_tokentable_.length];
        int i2 = i;
        int i3 = 0;
        int i4 = 0;
        while (i3 < this.m_groupcount_) {
            int groupLengths = getGroupLengths(i3, cArr, cArr2);
            int i5 = i4;
            for (int i6 = 0; i6 < 32; i6++) {
                int i7 = cArr[i6] + groupLengths;
                char c = cArr2[i6];
                if (c != 0) {
                    int[] addGroupName = addGroupName(i7, c, bArr, this.m_nameSet_);
                    if (addGroupName[0] > i2) {
                        i2 = addGroupName[0];
                    }
                    int i8 = i7 + addGroupName[1];
                    if (addGroupName[1] < c) {
                        int i9 = c - addGroupName[1];
                        int[] addGroupName2 = addGroupName(i8, i9, bArr, this.m_nameSet_);
                        if (addGroupName2[0] > i2) {
                            i2 = addGroupName2[0];
                        }
                        int i10 = i8 + addGroupName2[1];
                        if (addGroupName2[1] < i9) {
                            int i11 = i9 - addGroupName2[1];
                            if (addGroupName(i10, i11, bArr, this.m_ISOCommentSet_)[1] > i5) {
                                i5 = i11;
                            }
                        }
                    }
                }
            }
            i3++;
            i4 = i5;
        }
        this.m_maxISOCommentLength_ = i4;
        this.m_maxNameLength_ = i2;
    }

    private boolean initNameSetsLengths() {
        if (this.m_maxNameLength_ > 0) {
            return true;
        }
        for (int i = 18; i >= 0; i--) {
            add(this.m_nameSet_, "0123456789ABCDEF<>-".charAt(i));
        }
        this.m_maxNameLength_ = addAlgorithmName(0);
        this.m_maxNameLength_ = addExtendedName(this.m_maxNameLength_);
        addGroupName(this.m_maxNameLength_);
        return true;
    }

    private void convert(int[] iArr, UnicodeSet unicodeSet) {
        unicodeSet.clear();
        if (initNameSetsLengths()) {
            for (char c = 255; c > 0; c = (char) (c - 1)) {
                if (contains(iArr, c)) {
                    unicodeSet.add(c);
                }
            }
        }
    }
}
