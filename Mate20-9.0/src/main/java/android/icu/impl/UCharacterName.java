package android.icu.impl;

import android.icu.lang.UCharacter;
import android.icu.lang.UCharacterEnums;
import android.icu.text.UnicodeSet;
import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;

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

    static final class AlgorithmName {
        static final int TYPE_0_ = 0;
        static final int TYPE_1_ = 1;
        private char[] m_factor_;
        private byte[] m_factorstring_;
        private String m_prefix_;
        /* access modifiers changed from: private */
        public int m_rangeend_;
        /* access modifiers changed from: private */
        public int m_rangestart_;
        private byte m_type_;
        private int[] m_utilIntBuffer_ = new int[256];
        private StringBuffer m_utilStringBuffer_ = new StringBuffer();
        private byte m_variant_;

        AlgorithmName() {
        }

        /* access modifiers changed from: package-private */
        public boolean setInfo(int rangestart, int rangeend, byte type, byte variant) {
            if (rangestart < 0 || rangestart > rangeend || rangeend > 1114111 || (type != 0 && type != 1)) {
                return false;
            }
            this.m_rangestart_ = rangestart;
            this.m_rangeend_ = rangeend;
            this.m_type_ = type;
            this.m_variant_ = variant;
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean setFactor(char[] factor) {
            if (factor.length != this.m_variant_) {
                return false;
            }
            this.m_factor_ = factor;
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean setPrefix(String prefix) {
            if (prefix == null || prefix.length() <= 0) {
                return false;
            }
            this.m_prefix_ = prefix;
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean setFactorString(byte[] string) {
            this.m_factorstring_ = string;
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean contains(int ch) {
            return this.m_rangestart_ <= ch && ch <= this.m_rangeend_;
        }

        /* access modifiers changed from: package-private */
        public void appendName(int ch, StringBuffer str) {
            str.append(this.m_prefix_);
            switch (this.m_type_) {
                case 0:
                    str.append(Utility.hex((long) ch, this.m_variant_));
                    return;
                case 1:
                    int offset = ch - this.m_rangestart_;
                    int[] indexes = this.m_utilIntBuffer_;
                    synchronized (this.m_utilIntBuffer_) {
                        for (int i = this.m_variant_ - 1; i > 0; i--) {
                            int factor = this.m_factor_[i] & 255;
                            indexes[i] = offset % factor;
                            offset /= factor;
                        }
                        indexes[0] = offset;
                        str.append(getFactorString(indexes, this.m_variant_));
                    }
                    return;
                default:
                    return;
            }
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0053, code lost:
            r1 = r1 + 1;
         */
        public int getChar(String name) {
            int prefixlen = this.m_prefix_.length();
            if (name.length() < prefixlen || !this.m_prefix_.equals(name.substring(0, prefixlen))) {
                return -1;
            }
            switch (this.m_type_) {
                case 0:
                    try {
                        int result = Integer.parseInt(name.substring(prefixlen), 16);
                        if (this.m_rangestart_ > result || result > this.m_rangeend_) {
                            return -1;
                        }
                        return result;
                    } catch (NumberFormatException e) {
                        return -1;
                    }
                case 1:
                    int ch = this.m_rangestart_;
                    while (ch <= this.m_rangeend_) {
                        int offset = ch - this.m_rangestart_;
                        int[] indexes = this.m_utilIntBuffer_;
                        synchronized (this.m_utilIntBuffer_) {
                            for (int i = this.m_variant_ - 1; i > 0; i--) {
                                int factor = this.m_factor_[i] & 255;
                                indexes[i] = offset % factor;
                                offset /= factor;
                            }
                            indexes[0] = offset;
                            if (compareFactorString(indexes, this.m_variant_, name, prefixlen)) {
                                return ch;
                            }
                        }
                    }
                    break;
            }
            return -1;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v2, resolved type: char} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v4, resolved type: char} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v5, resolved type: char} */
        /* access modifiers changed from: package-private */
        /* JADX WARNING: Multi-variable type inference failed */
        public int add(int[] set, int maxlength) {
            int length = UCharacterName.add(set, this.m_prefix_);
            switch (this.m_type_) {
                case 0:
                    length += this.m_variant_;
                    break;
                case 1:
                    for (int i = this.m_variant_ - 1; i > 0; i--) {
                        int maxfactorlength = 0;
                        int count = 0;
                        for (int factor = this.m_factor_[i]; factor > 0; factor--) {
                            synchronized (this.m_utilStringBuffer_) {
                                this.m_utilStringBuffer_.setLength(0);
                                count = UCharacterUtility.getNullTermByteSubString(this.m_utilStringBuffer_, this.m_factorstring_, count);
                                int unused = UCharacterName.add(set, this.m_utilStringBuffer_);
                                if (this.m_utilStringBuffer_.length() > maxfactorlength) {
                                    maxfactorlength = this.m_utilStringBuffer_.length();
                                }
                            }
                        }
                        length += maxfactorlength;
                    }
                    break;
            }
            if (length > maxlength) {
                return length;
            }
            return maxlength;
        }

        private String getFactorString(int[] index, int length) {
            String stringBuffer;
            int size = this.m_factor_.length;
            if (index == null || length != size) {
                return null;
            }
            synchronized (this.m_utilStringBuffer_) {
                this.m_utilStringBuffer_.setLength(0);
                int count = 0;
                int size2 = size - 1;
                for (int i = 0; i <= size2; i++) {
                    char factor = this.m_factor_[i];
                    count = UCharacterUtility.getNullTermByteSubString(this.m_utilStringBuffer_, this.m_factorstring_, UCharacterUtility.skipNullTermByteSubString(this.m_factorstring_, count, index[i]));
                    if (i != size2) {
                        count = UCharacterUtility.skipNullTermByteSubString(this.m_factorstring_, count, (factor - index[i]) - 1);
                    }
                }
                stringBuffer = this.m_utilStringBuffer_.toString();
            }
            return stringBuffer;
        }

        private boolean compareFactorString(int[] index, int length, String str, int offset) {
            int size = this.m_factor_.length;
            if (index == null || length != size) {
                return false;
            }
            int size2 = size - 1;
            int strcount = offset;
            int count = 0;
            for (int i = 0; i <= size2; i++) {
                char factor = this.m_factor_[i];
                count = UCharacterUtility.skipNullTermByteSubString(this.m_factorstring_, count, index[i]);
                strcount = UCharacterUtility.compareNullTermByteSubString(str, this.m_factorstring_, strcount, count);
                if (strcount < 0) {
                    return false;
                }
                if (i != size2) {
                    count = UCharacterUtility.skipNullTermByteSubString(this.m_factorstring_, count, factor - index[i]);
                }
            }
            if (strcount != str.length()) {
                return false;
            }
            return true;
        }
    }

    static {
        try {
            INSTANCE = new UCharacterName();
        } catch (IOException e) {
            throw new MissingResourceException("Could not construct UCharacterName. Missing unames.icu", "", "");
        }
    }

    public String getName(int ch, int choice) {
        if (ch < 0 || ch > 1114111 || choice > 4) {
            return null;
        }
        String result = getAlgName(ch, choice);
        if (result == null || result.length() == 0) {
            if (choice == 2) {
                result = getExtendedName(ch);
            } else {
                result = getGroupName(ch, choice);
            }
        }
        return result;
    }

    public int getCharFromName(int choice, String name) {
        int result;
        if (choice >= 4 || name == null || name.length() == 0) {
            return -1;
        }
        int result2 = getExtendedChar(name.toLowerCase(Locale.ENGLISH), choice);
        if (result2 >= -1) {
            return result2;
        }
        String upperCaseName = name.toUpperCase(Locale.ENGLISH);
        if (choice == 0 || choice == 2) {
            int count = 0;
            if (this.m_algorithm_ != null) {
                count = this.m_algorithm_.length;
            }
            for (int count2 = count - 1; count2 >= 0; count2--) {
                int result3 = this.m_algorithm_[count2].getChar(upperCaseName);
                if (result3 >= 0) {
                    return result3;
                }
            }
        }
        if (choice == 2) {
            result = getGroupChar(upperCaseName, 0);
            if (result == -1) {
                result = getGroupChar(upperCaseName, 3);
            }
        } else {
            result = getGroupChar(upperCaseName, choice);
        }
        return result;
    }

    /*  JADX ERROR: JadxOverflowException in pass: LoopRegionVisitor
        jadx.core.utils.exceptions.JadxOverflowException: LoopRegionVisitor.assignOnlyInLoop endless recursion
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
        	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
        */
    public int getGroupLengths(int r12, char[] r13, char[] r14) {
        /*
            r11 = this;
            r0 = 65535(0xffff, float:9.1834E-41)
            r1 = 0
            r2 = 0
            int r3 = r11.m_groupsize_
            int r12 = r12 * r3
            char[] r3 = r11.m_groupinfo_
            int r4 = r12 + 1
            char r3 = r3[r4]
            char[] r4 = r11.m_groupinfo_
            int r5 = r12 + 2
            char r4 = r4[r5]
            int r3 = android.icu.impl.UCharacterUtility.toInt(r3, r4)
            r4 = 0
            r13[r4] = r4
        L_0x001c:
            r5 = 32
            if (r4 >= r5) goto L_0x0063
            byte[] r6 = r11.m_groupstring_
            byte r1 = r6[r3]
            r6 = 4
            r7 = r4
            r4 = r0
            r0 = r6
        L_0x0028:
            if (r0 < 0) goto L_0x005e
            int r8 = r1 >> r0
            r8 = r8 & 15
            byte r2 = (byte) r8
            r8 = 65535(0xffff, float:9.1834E-41)
            if (r4 != r8) goto L_0x003d
            r9 = 11
            if (r2 <= r9) goto L_0x003d
            int r8 = r2 + -12
            int r8 = r8 << r6
            char r4 = (char) r8
            goto L_0x005b
        L_0x003d:
            if (r4 == r8) goto L_0x0047
            r8 = r4 | r2
            int r8 = r8 + 12
            char r8 = (char) r8
            r14[r7] = r8
            goto L_0x004a
        L_0x0047:
            char r8 = (char) r2
            r14[r7] = r8
        L_0x004a:
            if (r7 >= r5) goto L_0x0056
            int r8 = r7 + 1
            char r9 = r13[r7]
            char r10 = r14[r7]
            int r9 = r9 + r10
            char r9 = (char) r9
            r13[r8] = r9
        L_0x0056:
            r4 = 65535(0xffff, float:9.1834E-41)
            int r7 = r7 + 1
        L_0x005b:
            int r0 = r0 + -4
            goto L_0x0028
        L_0x005e:
            int r3 = r3 + 1
            r0 = r4
            r4 = r7
            goto L_0x001c
        L_0x0063:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UCharacterName.getGroupLengths(int, char[], char[]):int");
    }

    public String getGroupName(int index, int length, int choice) {
        if (!(choice == 0 || choice == 2)) {
            if (59 >= this.m_tokentable_.length || this.m_tokentable_[59] == 65535) {
                int fieldIndex = choice == 4 ? 2 : choice;
                do {
                    int oldindex = index;
                    index += UCharacterUtility.skipByteSubString(this.m_groupstring_, index, length, (byte) 59);
                    length -= index - oldindex;
                    fieldIndex--;
                } while (fieldIndex > 0);
            } else {
                length = 0;
            }
        }
        synchronized (this.m_utilStringBuffer_) {
            int i = 0;
            this.m_utilStringBuffer_.setLength(0);
            while (true) {
                int i2 = i;
                if (i2 >= length) {
                    break;
                }
                byte b = this.m_groupstring_[index + i2];
                int i3 = i2 + 1;
                if (b < this.m_tokentable_.length) {
                    char token = this.m_tokentable_[b & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED];
                    if (token == 65534) {
                        token = this.m_tokentable_[(b << 8) | (this.m_groupstring_[index + i3] & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED)];
                        i3++;
                    }
                    if (token != 65535) {
                        UCharacterUtility.getNullTermByteSubString(this.m_utilStringBuffer_, this.m_tokenstring_, token);
                    } else if (b == 59) {
                        if (this.m_utilStringBuffer_.length() != 0 || choice != 2) {
                            break;
                        }
                    } else {
                        this.m_utilStringBuffer_.append((char) (b & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED));
                    }
                } else if (b == 59) {
                    break;
                } else {
                    this.m_utilStringBuffer_.append(b);
                }
                i = i3;
            }
            if (this.m_utilStringBuffer_.length() <= 0) {
                return null;
            }
            String stringBuffer = this.m_utilStringBuffer_.toString();
            return stringBuffer;
        }
    }

    public String getExtendedName(int ch) {
        String result = getName(ch, 0);
        if (result == null) {
            return getExtendedOr10Name(ch);
        }
        return result;
    }

    public int getGroup(int codepoint) {
        int endGroup = this.m_groupcount_;
        int msb = getCodepointMSB(codepoint);
        int result = 0;
        while (result < endGroup - 1) {
            int gindex = (result + endGroup) >> 1;
            if (msb < getGroupMSB(gindex)) {
                endGroup = gindex;
            } else {
                result = gindex;
            }
        }
        return result;
    }

    public String getExtendedOr10Name(int ch) {
        String result;
        String result2 = null;
        if (0 == 0) {
            int type = getType(ch);
            if (type >= TYPE_NAMES_.length) {
                result = UNKNOWN_TYPE_NAME_;
            } else {
                result = TYPE_NAMES_[type];
            }
            synchronized (this.m_utilStringBuffer_) {
                this.m_utilStringBuffer_.setLength(0);
                this.m_utilStringBuffer_.append('<');
                this.m_utilStringBuffer_.append(result);
                this.m_utilStringBuffer_.append('-');
                String chStr = Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
                for (int zeros = 4 - chStr.length(); zeros > 0; zeros--) {
                    this.m_utilStringBuffer_.append('0');
                }
                this.m_utilStringBuffer_.append(chStr);
                this.m_utilStringBuffer_.append('>');
                result2 = this.m_utilStringBuffer_.toString();
            }
        }
        return result2;
    }

    public int getGroupMSB(int gindex) {
        if (gindex >= this.m_groupcount_) {
            return -1;
        }
        return this.m_groupinfo_[this.m_groupsize_ * gindex];
    }

    public static int getCodepointMSB(int codepoint) {
        return codepoint >> 5;
    }

    public static int getGroupLimit(int msb) {
        return (msb << 5) + 32;
    }

    public static int getGroupMin(int msb) {
        return msb << 5;
    }

    public static int getGroupOffset(int codepoint) {
        return codepoint & 31;
    }

    public static int getGroupMinFromCodepoint(int codepoint) {
        return codepoint & -32;
    }

    public int getAlgorithmLength() {
        return this.m_algorithm_.length;
    }

    public int getAlgorithmStart(int index) {
        return this.m_algorithm_[index].m_rangestart_;
    }

    public int getAlgorithmEnd(int index) {
        return this.m_algorithm_[index].m_rangeend_;
    }

    public String getAlgorithmName(int index, int codepoint) {
        String result;
        synchronized (this.m_utilStringBuffer_) {
            this.m_utilStringBuffer_.setLength(0);
            this.m_algorithm_[index].appendName(codepoint, this.m_utilStringBuffer_);
            result = this.m_utilStringBuffer_.toString();
        }
        return result;
    }

    public synchronized String getGroupName(int ch, int choice) {
        int msb = getCodepointMSB(ch);
        int group = getGroup(ch);
        if (msb != this.m_groupinfo_[this.m_groupsize_ * group]) {
            return null;
        }
        int offset = ch & 31;
        return getGroupName(this.m_groupoffsets_[offset] + getGroupLengths(group, this.m_groupoffsets_, this.m_grouplengths_), this.m_grouplengths_[offset], choice);
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

    public void getCharNameCharacters(UnicodeSet set) {
        convert(this.m_nameSet_, set);
    }

    public void getISOCommentCharacters(UnicodeSet set) {
        convert(this.m_ISOCommentSet_, set);
    }

    /* access modifiers changed from: package-private */
    public boolean setToken(char[] token, byte[] tokenstring) {
        if (token == null || tokenstring == null || token.length <= 0 || tokenstring.length <= 0) {
            return false;
        }
        this.m_tokentable_ = token;
        this.m_tokenstring_ = tokenstring;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setAlgorithm(AlgorithmName[] alg) {
        if (alg == null || alg.length == 0) {
            return false;
        }
        this.m_algorithm_ = alg;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setGroupCountSize(int count, int size) {
        if (count <= 0 || size <= 0) {
            return false;
        }
        this.m_groupcount_ = count;
        this.m_groupsize_ = size;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setGroup(char[] group, byte[] groupstring) {
        if (group == null || groupstring == null || group.length <= 0 || groupstring.length <= 0) {
            return false;
        }
        this.m_groupinfo_ = group;
        this.m_groupstring_ = groupstring;
        return true;
    }

    private UCharacterName() throws IOException {
        new UCharacterNameReader(ICUBinary.getRequiredData(FILE_NAME_)).read(this);
    }

    private String getAlgName(int ch, int choice) {
        if (choice == 0 || choice == 2) {
            synchronized (this.m_utilStringBuffer_) {
                this.m_utilStringBuffer_.setLength(0);
                for (int index = this.m_algorithm_.length - 1; index >= 0; index--) {
                    if (this.m_algorithm_[index].contains(ch)) {
                        this.m_algorithm_[index].appendName(ch, this.m_utilStringBuffer_);
                        String stringBuffer = this.m_utilStringBuffer_.toString();
                        return stringBuffer;
                    }
                }
            }
        }
        return null;
    }

    private synchronized int getGroupChar(String name, int choice) {
        for (int i = 0; i < this.m_groupcount_; i++) {
            int result = getGroupChar(getGroupLengths(i, this.m_groupoffsets_, this.m_grouplengths_), this.m_grouplengths_, name, choice);
            if (result != -1) {
                return (this.m_groupinfo_[this.m_groupsize_ * i] << 5) | result;
            }
        }
        return -1;
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=byte, code=int, for r12v2, types: [byte, int] */
    private int getGroupChar(int index, char[] length, String name, int choice) {
        String str = name;
        int i = choice;
        int namelen = name.length();
        int index2 = index;
        int count = 0;
        int result = 0;
        while (result <= 32) {
            int nindex = 0;
            char c = length[result];
            int len = c;
            if (i != 0) {
                int fieldIndex = 2;
                len = c;
                if (i != 2) {
                    if (i != 4) {
                        fieldIndex = i;
                    }
                    len = c;
                    do {
                        int oldindex = index2;
                        index2 += UCharacterUtility.skipByteSubString(this.m_groupstring_, index2, len, (byte) 59);
                        len -= index2 - oldindex;
                        fieldIndex--;
                    } while (fieldIndex > 0);
                }
            }
            int b = count;
            int count2 = 0;
            while (count2 < len && nindex != -1 && nindex < namelen) {
                b = this.m_groupstring_[index2 + count2];
                count2++;
                if (b >= this.m_tokentable_.length) {
                    nindex = str.charAt(nindex) != (b & 255) ? -1 : nindex + 1;
                } else {
                    char token = this.m_tokentable_[b & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED];
                    if (token == 65534) {
                        token = this.m_tokentable_[(this.m_groupstring_[index2 + count2] & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED) | (b << 8)];
                        count2++;
                    }
                    if (token == 65535) {
                        int nindex2 = nindex + 1;
                        if (str.charAt(nindex) != (b & 255)) {
                            nindex = -1;
                        } else {
                            nindex = nindex2;
                        }
                    } else {
                        nindex = UCharacterUtility.compareNullTermByteSubString(str, this.m_tokenstring_, nindex, token);
                    }
                }
            }
            if (namelen == nindex && (count2 == len || this.m_groupstring_[index2 + count2] == 59)) {
                return result;
            }
            index2 += len;
            result++;
            count = b;
        }
        return -1;
    }

    private static int getType(int ch) {
        if (UCharacterUtility.isNonCharacter(ch)) {
            return 30;
        }
        int result = UCharacter.getType(ch);
        if (result == 18) {
            if (ch <= 56319) {
                result = 31;
            } else {
                result = 32;
            }
        }
        return result;
    }

    private static int getExtendedChar(String name, int choice) {
        int i = 0;
        if (name.charAt(0) != '<') {
            return -2;
        }
        if (choice == 2) {
            int endIndex = name.length() - 1;
            if (name.charAt(endIndex) == '>') {
                int startIndex = name.lastIndexOf(45);
                if (startIndex >= 0) {
                    int startIndex2 = startIndex + 1;
                    try {
                        int result = Integer.parseInt(name.substring(startIndex2, endIndex), 16);
                        String type = name.substring(1, startIndex2 - 1);
                        int length = TYPE_NAMES_.length;
                        while (true) {
                            if (i >= length) {
                                break;
                            } else if (type.compareTo(TYPE_NAMES_[i]) != 0) {
                                i++;
                            } else if (getType(result) == i) {
                                return result;
                            }
                        }
                    } catch (NumberFormatException e) {
                        return -1;
                    }
                }
            }
        }
        return -1;
    }

    private static void add(int[] set, char ch) {
        int i = ch >>> 5;
        set[i] = set[i] | (1 << (ch & 31));
    }

    private static boolean contains(int[] set, char ch) {
        return (set[ch >>> 5] & (1 << (ch & 31))) != 0;
    }

    /* access modifiers changed from: private */
    public static int add(int[] set, String str) {
        int result = str.length();
        for (int i = result - 1; i >= 0; i--) {
            add(set, str.charAt(i));
        }
        return result;
    }

    /* access modifiers changed from: private */
    public static int add(int[] set, StringBuffer str) {
        int result = str.length();
        for (int i = result - 1; i >= 0; i--) {
            add(set, str.charAt(i));
        }
        return result;
    }

    private int addAlgorithmName(int maxlength) {
        for (int i = this.m_algorithm_.length - 1; i >= 0; i--) {
            int result = this.m_algorithm_[i].add(this.m_nameSet_, maxlength);
            if (result > maxlength) {
                maxlength = result;
            }
        }
        return maxlength;
    }

    private int addExtendedName(int maxlength) {
        for (int i = TYPE_NAMES_.length - 1; i >= 0; i--) {
            int length = 9 + add(this.m_nameSet_, TYPE_NAMES_[i]);
            if (length > maxlength) {
                maxlength = length;
            }
        }
        return maxlength;
    }

    private int[] addGroupName(int offset, int length, byte[] tokenlength, int[] set) {
        int resultnlength = 0;
        int resultplength = 0;
        while (resultplength < length) {
            char b = (char) (this.m_groupstring_[offset + resultplength] & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED);
            resultplength++;
            if (b == ';') {
                break;
            } else if (b >= this.m_tokentable_.length) {
                add(set, b);
                resultnlength++;
            } else {
                char token = this.m_tokentable_[b & 255];
                if (token == 65534) {
                    b = (char) ((b << 8) | (this.m_groupstring_[offset + resultplength] & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED));
                    token = this.m_tokentable_[b];
                    resultplength++;
                }
                if (token == 65535) {
                    add(set, b);
                    resultnlength++;
                } else {
                    byte tlength = tokenlength[b];
                    if (tlength == 0) {
                        synchronized (this.m_utilStringBuffer_) {
                            this.m_utilStringBuffer_.setLength(0);
                            UCharacterUtility.getNullTermByteSubString(this.m_utilStringBuffer_, this.m_tokenstring_, token);
                            tlength = (byte) add(set, this.m_utilStringBuffer_);
                        }
                        tokenlength[b] = tlength;
                    }
                    resultnlength += tlength;
                }
            }
        }
        this.m_utilIntBuffer_[0] = resultnlength;
        this.m_utilIntBuffer_[1] = resultplength;
        return this.m_utilIntBuffer_;
    }

    private void addGroupName(int maxlength) {
        int maxisolength = 0;
        char[] offsets = new char[34];
        char[] lengths = new char[34];
        byte[] tokenlengths = new byte[this.m_tokentable_.length];
        int maxlength2 = maxlength;
        int i = 0;
        while (i < this.m_groupcount_) {
            int offset = getGroupLengths(i, offsets, lengths);
            int maxisolength2 = maxisolength;
            for (int linenumber = 0; linenumber < 32; linenumber++) {
                int lineoffset = offsets[linenumber] + offset;
                char length = lengths[linenumber];
                if (length != 0) {
                    int[] parsed = addGroupName(lineoffset, length, tokenlengths, this.m_nameSet_);
                    if (parsed[0] > maxlength2) {
                        maxlength2 = parsed[0];
                    }
                    int lineoffset2 = lineoffset + parsed[1];
                    if (parsed[1] < length) {
                        int length2 = length - parsed[1];
                        int[] parsed2 = addGroupName(lineoffset2, length2, tokenlengths, this.m_nameSet_);
                        if (parsed2[0] > maxlength2) {
                            maxlength2 = parsed2[0];
                        }
                        int lineoffset3 = lineoffset2 + parsed2[1];
                        if (parsed2[1] < length2) {
                            int length3 = length2 - parsed2[1];
                            if (addGroupName(lineoffset3, length3, tokenlengths, this.m_ISOCommentSet_)[1] > maxisolength2) {
                                maxisolength2 = length3;
                            }
                        }
                    }
                }
            }
            i++;
            maxisolength = maxisolength2;
        }
        this.m_maxISOCommentLength_ = maxisolength;
        this.m_maxNameLength_ = maxlength2;
    }

    private boolean initNameSetsLengths() {
        if (this.m_maxNameLength_ > 0) {
            return true;
        }
        for (int i = "0123456789ABCDEF<>-".length() - 1; i >= 0; i--) {
            add(this.m_nameSet_, "0123456789ABCDEF<>-".charAt(i));
        }
        this.m_maxNameLength_ = addAlgorithmName(0);
        this.m_maxNameLength_ = addExtendedName(this.m_maxNameLength_);
        addGroupName(this.m_maxNameLength_);
        return true;
    }

    private void convert(int[] set, UnicodeSet uset) {
        uset.clear();
        if (initNameSetsLengths()) {
            for (char c = 255; c > 0; c = (char) (c - 1)) {
                if (contains(set, c)) {
                    uset.add((int) c);
                }
            }
        }
    }
}
