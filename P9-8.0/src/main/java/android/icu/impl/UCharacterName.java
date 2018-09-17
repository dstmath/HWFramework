package android.icu.impl;

import android.icu.lang.UCharacter;
import android.icu.text.UTF16;
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
    private static final String[] TYPE_NAMES_ = new String[]{"unassigned", "uppercase letter", "lowercase letter", "titlecase letter", "modifier letter", "other letter", "non spacing mark", "enclosing mark", "combining spacing mark", "decimal digit number", "letter number", "other number", "space separator", "line separator", "paragraph separator", "control", "format", "private use area", "surrogate", "dash punctuation", "start punctuation", "end punctuation", "connector punctuation", "other punctuation", "math symbol", "currency symbol", "modifier symbol", "other symbol", "initial punctuation", "final punctuation", "noncharacter", "lead surrogate", "trail surrogate"};
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
        private int m_rangeend_;
        private int m_rangestart_;
        private byte m_type_;
        private int[] m_utilIntBuffer_ = new int[256];
        private StringBuffer m_utilStringBuffer_ = new StringBuffer();
        private byte m_variant_;

        AlgorithmName() {
        }

        boolean setInfo(int rangestart, int rangeend, byte type, byte variant) {
            if (rangestart < 0 || rangestart > rangeend || rangeend > 1114111 || (type != (byte) 0 && type != (byte) 1)) {
                return false;
            }
            this.m_rangestart_ = rangestart;
            this.m_rangeend_ = rangeend;
            this.m_type_ = type;
            this.m_variant_ = variant;
            return true;
        }

        boolean setFactor(char[] factor) {
            if (factor.length != this.m_variant_) {
                return false;
            }
            this.m_factor_ = factor;
            return true;
        }

        boolean setPrefix(String prefix) {
            if (prefix == null || prefix.length() <= 0) {
                return false;
            }
            this.m_prefix_ = prefix;
            return true;
        }

        boolean setFactorString(byte[] string) {
            this.m_factorstring_ = string;
            return true;
        }

        boolean contains(int ch) {
            return this.m_rangestart_ <= ch && ch <= this.m_rangeend_;
        }

        void appendName(int ch, StringBuffer str) {
            str.append(this.m_prefix_);
            switch (this.m_type_) {
                case (byte) 0:
                    str.append(Utility.hex((long) ch, this.m_variant_));
                    return;
                case (byte) 1:
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

        /* JADX WARNING: Missing block: B:30:0x0069, code:
            r0 = r0 + 1;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        int getChar(String name) {
            int prefixlen = this.m_prefix_.length();
            if (name.length() < prefixlen || (this.m_prefix_.equals(name.substring(0, prefixlen)) ^ 1) != 0) {
                return -1;
            }
            switch (this.m_type_) {
                case (byte) 0:
                    try {
                        int result = Integer.parseInt(name.substring(prefixlen), 16);
                        if (this.m_rangestart_ > result || result > this.m_rangeend_) {
                            return -1;
                        }
                        return result;
                    } catch (NumberFormatException e) {
                        return -1;
                    }
                case (byte) 1:
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

        int add(int[] set, int maxlength) {
            int length = UCharacterName.add(set, this.m_prefix_);
            switch (this.m_type_) {
                case (byte) 0:
                    length += this.m_variant_;
                    break;
                case (byte) 1:
                    for (int i = this.m_variant_ - 1; i > 0; i--) {
                        int maxfactorlength = 0;
                        int count = 0;
                        for (int factor = this.m_factor_[i]; factor > 0; factor--) {
                            synchronized (this.m_utilStringBuffer_) {
                                this.m_utilStringBuffer_.setLength(0);
                                count = UCharacterUtility.getNullTermByteSubString(this.m_utilStringBuffer_, this.m_factorstring_, count);
                                UCharacterName.add(set, this.m_utilStringBuffer_);
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
            int size = this.m_factor_.length;
            if (index == null || length != size) {
                return null;
            }
            String stringBuffer;
            synchronized (this.m_utilStringBuffer_) {
                this.m_utilStringBuffer_.setLength(0);
                int count = 0;
                size--;
                for (int i = 0; i <= size; i++) {
                    int factor = this.m_factor_[i];
                    count = UCharacterUtility.getNullTermByteSubString(this.m_utilStringBuffer_, this.m_factorstring_, UCharacterUtility.skipNullTermByteSubString(this.m_factorstring_, count, index[i]));
                    if (i != size) {
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
            int count = 0;
            int strcount = offset;
            size--;
            for (int i = 0; i <= size; i++) {
                int factor = this.m_factor_[i];
                count = UCharacterUtility.skipNullTermByteSubString(this.m_factorstring_, count, index[i]);
                strcount = UCharacterUtility.compareNullTermByteSubString(str, this.m_factorstring_, strcount, count);
                if (strcount < 0) {
                    return false;
                }
                if (i != size) {
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
        if (choice >= 4 || name == null || name.length() == 0) {
            return -1;
        }
        int result = getExtendedChar(name.toLowerCase(Locale.ENGLISH), choice);
        if (result >= -1) {
            return result;
        }
        String upperCaseName = name.toUpperCase(Locale.ENGLISH);
        if (choice == 0 || choice == 2) {
            int count = 0;
            if (this.m_algorithm_ != null) {
                count = this.m_algorithm_.length;
            }
            for (count--; count >= 0; count--) {
                result = this.m_algorithm_[count].getChar(upperCaseName);
                if (result >= 0) {
                    return result;
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

    public int getGroupLengths(int index, char[] offsets, char[] lengths) {
        char length = 65535;
        index *= this.m_groupsize_;
        int stringoffset = UCharacterUtility.toInt(this.m_groupinfo_[index + 1], this.m_groupinfo_[index + 2]);
        offsets[0] = 0;
        int i = 0;
        while (i < 32) {
            byte b = this.m_groupstring_[stringoffset];
            for (int shift = 4; shift >= 0; shift -= 4) {
                byte n = (byte) ((b >> shift) & 15);
                if (length != 65535 || n <= (byte) 11) {
                    if (length != 65535) {
                        lengths[i] = (char) ((length | n) + 12);
                    } else {
                        lengths[i] = (char) n;
                    }
                    if (i < 32) {
                        offsets[i + 1] = (char) (offsets[i] + lengths[i]);
                    }
                    length = 65535;
                    i++;
                } else {
                    length = (char) ((n - 12) << 4);
                }
            }
            stringoffset++;
        }
        return stringoffset;
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
            this.m_utilStringBuffer_.setLength(0);
            int i = 0;
            while (i < length) {
                byte b = this.m_groupstring_[index + i];
                i++;
                if (b < this.m_tokentable_.length) {
                    char token = this.m_tokentable_[b & 255];
                    if (token == 65534) {
                        token = this.m_tokentable_[(b << 8) | (this.m_groupstring_[index + i] & 255)];
                        i++;
                    }
                    if (token != 65535) {
                        UCharacterUtility.getNullTermByteSubString(this.m_utilStringBuffer_, this.m_tokenstring_, token);
                    } else if (b == (byte) 59) {
                        if (this.m_utilStringBuffer_.length() != 0 || choice != 2) {
                            break;
                        }
                    } else {
                        this.m_utilStringBuffer_.append((char) (b & 255));
                    }
                } else if (b == (byte) 59) {
                    break;
                } else {
                    this.m_utilStringBuffer_.append(b);
                }
            }
            if (this.m_utilStringBuffer_.length() > 0) {
                String stringBuffer = this.m_utilStringBuffer_.toString();
                return stringBuffer;
            }
            return null;
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
            result = this.m_utilStringBuffer_.toString();
        }
        return result;
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
        char msb = getCodepointMSB(ch);
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

    boolean setToken(char[] token, byte[] tokenstring) {
        if (token == null || tokenstring == null || token.length <= 0 || tokenstring.length <= 0) {
            return false;
        }
        this.m_tokentable_ = token;
        this.m_tokenstring_ = tokenstring;
        return true;
    }

    boolean setAlgorithm(AlgorithmName[] alg) {
        if (alg == null || alg.length == 0) {
            return false;
        }
        this.m_algorithm_ = alg;
        return true;
    }

    boolean setGroupCountSize(int count, int size) {
        if (count <= 0 || size <= 0) {
            return false;
        }
        this.m_groupcount_ = count;
        this.m_groupsize_ = size;
        return true;
    }

    boolean setGroup(char[] group, byte[] groupstring) {
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

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0045  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getGroupChar(int index, char[] length, String name, int choice) {
        int namelen = name.length();
        int result = 0;
        while (result <= 32) {
            int len = length[result];
            int count;
            int nindex;
            byte b;
            int nindex2;
            if (choice == 0 || choice == 2) {
                count = 0;
                nindex = 0;
                while (count < len && nindex != -1 && nindex < namelen) {
                    b = this.m_groupstring_[index + count];
                    count++;
                    if (b < this.m_tokentable_.length) {
                        nindex2 = nindex + 1;
                        if (name.charAt(nindex) != (b & 255)) {
                            nindex2 = -1;
                        }
                    } else {
                        char token = this.m_tokentable_[b & 255];
                        if (token == 65534) {
                            token = this.m_tokentable_[(b << 8) | (this.m_groupstring_[index + count] & 255)];
                            count++;
                        }
                        if (token == 65535) {
                            nindex2 = nindex + 1;
                            if (name.charAt(nindex) != (b & 255)) {
                                nindex2 = -1;
                            }
                        } else {
                            nindex2 = UCharacterUtility.compareNullTermByteSubString(name, this.m_tokenstring_, nindex, token);
                        }
                    }
                    nindex = nindex2;
                }
                if (namelen != nindex && (count == len || this.m_groupstring_[index + count] == (byte) 59)) {
                    return result;
                }
                index += len;
                result++;
            } else {
                int fieldIndex = choice == 4 ? 2 : choice;
                do {
                    int oldindex = index;
                    index += UCharacterUtility.skipByteSubString(this.m_groupstring_, index, len, (byte) 59);
                    len -= index - oldindex;
                    fieldIndex--;
                } while (fieldIndex > 0);
                count = 0;
                nindex = 0;
                while (count < len) {
                    b = this.m_groupstring_[index + count];
                    count++;
                    if (b < this.m_tokentable_.length) {
                    }
                    nindex = nindex2;
                }
                if (namelen != nindex) {
                }
                index += len;
                result++;
            }
        }
        return -1;
    }

    private static int getType(int ch) {
        if (UCharacterUtility.isNonCharacter(ch)) {
            return 30;
        }
        int result = UCharacter.getType(ch);
        if (result == 18) {
            if (ch <= UTF16.LEAD_SURROGATE_MAX_VALUE) {
                result = 31;
            } else {
                result = 32;
            }
        }
        return result;
    }

    private static int getExtendedChar(String name, int choice) {
        if (name.charAt(0) != '<') {
            return -2;
        }
        if (choice == 2) {
            int endIndex = name.length() - 1;
            if (name.charAt(endIndex) == '>') {
                int startIndex = name.lastIndexOf(45);
                if (startIndex >= 0) {
                    startIndex++;
                    try {
                        int result = Integer.parseInt(name.substring(startIndex, endIndex), 16);
                        String type = name.substring(1, startIndex - 1);
                        int length = TYPE_NAMES_.length;
                        int i = 0;
                        while (i < length) {
                            if (type.compareTo(TYPE_NAMES_[i]) != 0) {
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

    private static int add(int[] set, String str) {
        int result = str.length();
        for (int i = result - 1; i >= 0; i--) {
            add(set, str.charAt(i));
        }
        return result;
    }

    private static int add(int[] set, StringBuffer str) {
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
            int length = add(this.m_nameSet_, TYPE_NAMES_[i]) + 9;
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
            char b = (char) (this.m_groupstring_[offset + resultplength] & 255);
            resultplength++;
            if (b == ';') {
                break;
            } else if (b >= this.m_tokentable_.length) {
                add(set, b);
                resultnlength++;
            } else {
                char token = this.m_tokentable_[b & 255];
                if (token == 65534) {
                    b = (char) ((b << 8) | (this.m_groupstring_[offset + resultplength] & 255));
                    token = this.m_tokentable_[b];
                    resultplength++;
                }
                if (token == 65535) {
                    add(set, b);
                    resultnlength++;
                } else {
                    byte tlength = tokenlength[b];
                    if (tlength == (byte) 0) {
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
        for (int i = 0; i < this.m_groupcount_; i++) {
            int offset = getGroupLengths(i, offsets, lengths);
            for (int linenumber = 0; linenumber < 32; linenumber++) {
                int lineoffset = offset + offsets[linenumber];
                int length = lengths[linenumber];
                if (length != 0) {
                    int[] parsed = addGroupName(lineoffset, length, tokenlengths, this.m_nameSet_);
                    if (parsed[0] > maxlength) {
                        maxlength = parsed[0];
                    }
                    lineoffset += parsed[1];
                    if (parsed[1] < length) {
                        length -= parsed[1];
                        parsed = addGroupName(lineoffset, length, tokenlengths, this.m_nameSet_);
                        if (parsed[0] > maxlength) {
                            maxlength = parsed[0];
                        }
                        lineoffset += parsed[1];
                        if (parsed[1] < length) {
                            length -= parsed[1];
                            if (addGroupName(lineoffset, length, tokenlengths, this.m_ISOCommentSet_)[1] > maxisolength) {
                                maxisolength = length;
                            }
                        }
                    }
                }
            }
        }
        this.m_maxISOCommentLength_ = maxisolength;
        this.m_maxNameLength_ = maxlength;
    }

    private boolean initNameSetsLengths() {
        if (this.m_maxNameLength_ > 0) {
            return true;
        }
        String extra = "0123456789ABCDEF<>-";
        for (int i = extra.length() - 1; i >= 0; i--) {
            add(this.m_nameSet_, extra.charAt(i));
        }
        this.m_maxNameLength_ = addAlgorithmName(0);
        this.m_maxNameLength_ = addExtendedName(this.m_maxNameLength_);
        addGroupName(this.m_maxNameLength_);
        return true;
    }

    private void convert(int[] set, UnicodeSet uset) {
        uset.clear();
        if (initNameSetsLengths()) {
            for (int c = 255; c > 0; c = (char) (c - 1)) {
                if (contains(set, c)) {
                    uset.add(c);
                }
            }
        }
    }
}
