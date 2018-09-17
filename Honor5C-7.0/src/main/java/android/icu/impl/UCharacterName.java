package android.icu.impl;

import android.icu.lang.UCharacter;
import android.icu.text.UTF16;
import android.icu.text.UnicodeMatcher;
import android.icu.text.UnicodeSet;
import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.util.Locale;
import org.w3c.dom.traversal.NodeFilter;

public final class UCharacterName {
    static final int EXTENDED_CATEGORY_ = 33;
    private static final String FILE_NAME_ = "unames.icu";
    private static final int GROUP_MASK_ = 31;
    private static final int GROUP_SHIFT_ = 5;
    public static final UCharacterName INSTANCE = null;
    private static final int LEAD_SURROGATE_ = 31;
    public static final int LINES_PER_GROUP_ = 32;
    private static final int NON_CHARACTER_ = 30;
    private static final int OFFSET_HIGH_OFFSET_ = 1;
    private static final int OFFSET_LOW_OFFSET_ = 2;
    private static final int SINGLE_NIBBLE_MAX_ = 11;
    private static final int TRAIL_SURROGATE_ = 32;
    private static final String[] TYPE_NAMES_ = null;
    private static final String UNKNOWN_TYPE_NAME_ = "unknown";
    private int[] m_ISOCommentSet_;
    private AlgorithmName[] m_algorithm_;
    public int m_groupcount_;
    private char[] m_groupinfo_;
    private char[] m_grouplengths_;
    private char[] m_groupoffsets_;
    int m_groupsize_;
    private byte[] m_groupstring_;
    private int m_maxISOCommentLength_;
    private int m_maxNameLength_;
    private int[] m_nameSet_;
    private byte[] m_tokenstring_;
    private char[] m_tokentable_;
    private int[] m_utilIntBuffer_;
    private StringBuffer m_utilStringBuffer_;

    static final class AlgorithmName {
        static final int TYPE_0_ = 0;
        static final int TYPE_1_ = 1;
        private char[] m_factor_;
        private byte[] m_factorstring_;
        private String m_prefix_;
        private int m_rangeend_;
        private int m_rangestart_;
        private byte m_type_;
        private int[] m_utilIntBuffer_;
        private StringBuffer m_utilStringBuffer_;
        private byte m_variant_;

        AlgorithmName() {
            this.m_utilStringBuffer_ = new StringBuffer();
            this.m_utilIntBuffer_ = new int[NodeFilter.SHOW_DOCUMENT];
        }

        boolean setInfo(int rangestart, int rangeend, byte type, byte variant) {
            if (rangestart < 0 || rangestart > rangeend || rangeend > UnicodeSet.MAX_VALUE || (type != null && type != (byte) 1)) {
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
                case TYPE_0_ /*0*/:
                    str.append(Utility.hex((long) ch, this.m_variant_));
                case TYPE_1_ /*1*/:
                    int offset = ch - this.m_rangestart_;
                    int[] indexes = this.m_utilIntBuffer_;
                    synchronized (this.m_utilIntBuffer_) {
                        for (int i = this.m_variant_ - 1; i > 0; i--) {
                            int factor = this.m_factor_[i] & Opcodes.OP_CONST_CLASS_JUMBO;
                            indexes[i] = offset % factor;
                            offset /= factor;
                        }
                        indexes[TYPE_0_] = offset;
                        str.append(getFactorString(indexes, this.m_variant_));
                        break;
                    }
                default:
            }
        }

        int getChar(String name) {
            int prefixlen = this.m_prefix_.length();
            if (name.length() < prefixlen || !this.m_prefix_.equals(name.substring(TYPE_0_, prefixlen))) {
                return -1;
            }
            switch (this.m_type_) {
                case TYPE_0_ /*0*/:
                    try {
                        int result = Integer.parseInt(name.substring(prefixlen), 16);
                        if (this.m_rangestart_ > result || result > this.m_rangeend_) {
                            return -1;
                        }
                        return result;
                    } catch (NumberFormatException e) {
                        return -1;
                    }
                case TYPE_1_ /*1*/:
                    int ch = this.m_rangestart_;
                    while (ch <= this.m_rangeend_) {
                        int offset = ch - this.m_rangestart_;
                        int[] indexes = this.m_utilIntBuffer_;
                        synchronized (this.m_utilIntBuffer_) {
                            for (int i = this.m_variant_ - 1; i > 0; i--) {
                                int factor = this.m_factor_[i] & Opcodes.OP_CONST_CLASS_JUMBO;
                                indexes[i] = offset % factor;
                                offset /= factor;
                            }
                            indexes[TYPE_0_] = offset;
                            if (compareFactorString(indexes, this.m_variant_, name, prefixlen)) {
                                return ch;
                            }
                            ch += TYPE_1_;
                            break;
                        }
                    }
                    break;
            }
            return -1;
        }

        int add(int[] set, int maxlength) {
            int length = UCharacterName.add(set, this.m_prefix_);
            switch (this.m_type_) {
                case TYPE_0_ /*0*/:
                    length += this.m_variant_;
                    break;
                case TYPE_1_ /*1*/:
                    for (int i = this.m_variant_ - 1; i > 0; i--) {
                        int maxfactorlength = TYPE_0_;
                        int count = TYPE_0_;
                        for (int factor = this.m_factor_[i]; factor > 0; factor--) {
                            synchronized (this.m_utilStringBuffer_) {
                                this.m_utilStringBuffer_.delete(TYPE_0_, this.m_utilStringBuffer_.length());
                                count = UCharacterUtility.getNullTermByteSubString(this.m_utilStringBuffer_, this.m_factorstring_, count);
                                UCharacterName.add(set, this.m_utilStringBuffer_);
                                if (this.m_utilStringBuffer_.length() > maxfactorlength) {
                                    maxfactorlength = this.m_utilStringBuffer_.length();
                                }
                                break;
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
                this.m_utilStringBuffer_.delete(TYPE_0_, this.m_utilStringBuffer_.length());
                int count = TYPE_0_;
                size--;
                for (int i = TYPE_0_; i <= size; i += TYPE_1_) {
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
            int count = TYPE_0_;
            int strcount = offset;
            size--;
            for (int i = TYPE_0_; i <= size; i += TYPE_1_) {
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.UCharacterName.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.UCharacterName.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UCharacterName.<clinit>():void");
    }

    public String getName(int ch, int choice) {
        if (ch < 0 || ch > UnicodeSet.MAX_VALUE || choice > 4) {
            return null;
        }
        String result = getAlgName(ch, choice);
        if (result == null || result.length() == 0) {
            if (choice == OFFSET_LOW_OFFSET_) {
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
        if (choice == 0 || choice == OFFSET_LOW_OFFSET_) {
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
        if (choice == OFFSET_LOW_OFFSET_) {
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
        char length = UnicodeMatcher.ETHER;
        index *= this.m_groupsize_;
        int stringoffset = UCharacterUtility.toInt(this.m_groupinfo_[index + OFFSET_HIGH_OFFSET_], this.m_groupinfo_[index + OFFSET_LOW_OFFSET_]);
        offsets[0] = '\u0000';
        int i = 0;
        while (i < TRAIL_SURROGATE_) {
            byte b = this.m_groupstring_[stringoffset];
            for (int shift = 4; shift >= 0; shift -= 4) {
                byte n = (byte) ((b >> shift) & 15);
                if (length != UnicodeMatcher.ETHER || n <= SINGLE_NIBBLE_MAX_) {
                    if (length != UnicodeMatcher.ETHER) {
                        lengths[i] = (char) ((length | n) + 12);
                    } else {
                        lengths[i] = (char) n;
                    }
                    if (i < TRAIL_SURROGATE_) {
                        offsets[i + OFFSET_HIGH_OFFSET_] = (char) (offsets[i] + lengths[i]);
                    }
                    length = UnicodeMatcher.ETHER;
                    i += OFFSET_HIGH_OFFSET_;
                } else {
                    length = (char) ((n - 12) << 4);
                }
            }
            stringoffset += OFFSET_HIGH_OFFSET_;
        }
        return stringoffset;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getGroupName(int index, int length, int choice) {
        if (!(choice == 0 || choice == OFFSET_LOW_OFFSET_)) {
            if (59 >= this.m_tokentable_.length || this.m_tokentable_[59] == UnicodeMatcher.ETHER) {
                int fieldIndex = choice == 4 ? OFFSET_LOW_OFFSET_ : choice;
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
            this.m_utilStringBuffer_.delete(0, this.m_utilStringBuffer_.length());
            int i = 0;
            while (i < length) {
                byte b = this.m_groupstring_[index + i];
                i += OFFSET_HIGH_OFFSET_;
                if (b < this.m_tokentable_.length) {
                    char token = this.m_tokentable_[b & Opcodes.OP_CONST_CLASS_JUMBO];
                    if (token == '\ufffe') {
                        token = this.m_tokentable_[(b << 8) | (this.m_groupstring_[index + i] & Opcodes.OP_CONST_CLASS_JUMBO)];
                        i += OFFSET_HIGH_OFFSET_;
                    }
                    if (token != UnicodeMatcher.ETHER) {
                        UCharacterUtility.getNullTermByteSubString(this.m_utilStringBuffer_, this.m_tokenstring_, token);
                    } else if (b != 59) {
                        this.m_utilStringBuffer_.append((char) (b & Opcodes.OP_CONST_CLASS_JUMBO));
                    } else if (this.m_utilStringBuffer_.length() == 0 && choice == OFFSET_LOW_OFFSET_) {
                    }
                } else if (b == 59) {
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
            int gindex = (result + endGroup) >> OFFSET_HIGH_OFFSET_;
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
            this.m_utilStringBuffer_.delete(0, this.m_utilStringBuffer_.length());
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
        return codepoint >> GROUP_SHIFT_;
    }

    public static int getGroupLimit(int msb) {
        return (msb << GROUP_SHIFT_) + TRAIL_SURROGATE_;
    }

    public static int getGroupMin(int msb) {
        return msb << GROUP_SHIFT_;
    }

    public static int getGroupOffset(int codepoint) {
        return codepoint & LEAD_SURROGATE_;
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
            this.m_utilStringBuffer_.delete(0, this.m_utilStringBuffer_.length());
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
        int offset = ch & LEAD_SURROGATE_;
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
        this.m_groupcount_ = 0;
        this.m_groupsize_ = 0;
        this.m_groupoffsets_ = new char[EXTENDED_CATEGORY_];
        this.m_grouplengths_ = new char[EXTENDED_CATEGORY_];
        this.m_nameSet_ = new int[8];
        this.m_ISOCommentSet_ = new int[8];
        this.m_utilStringBuffer_ = new StringBuffer();
        this.m_utilIntBuffer_ = new int[OFFSET_LOW_OFFSET_];
        new UCharacterNameReader(ICUBinary.getRequiredData(FILE_NAME_)).read(this);
    }

    private String getAlgName(int ch, int choice) {
        if (choice == 0 || choice == OFFSET_LOW_OFFSET_) {
            synchronized (this.m_utilStringBuffer_) {
                this.m_utilStringBuffer_.delete(0, this.m_utilStringBuffer_.length());
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized int getGroupChar(String name, int choice) {
        int i = 0;
        while (true) {
            if (i >= this.m_groupcount_) {
                return -1;
            }
            int result = getGroupChar(getGroupLengths(i, this.m_groupoffsets_, this.m_grouplengths_), this.m_grouplengths_, name, choice);
            if (result != -1) {
                return (this.m_groupinfo_[this.m_groupsize_ * i] << GROUP_SHIFT_) | result;
            }
            i += OFFSET_HIGH_OFFSET_;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getGroupChar(int index, char[] length, String name, int choice) {
        int namelen = name.length();
        int result = 0;
        while (result <= TRAIL_SURROGATE_) {
            int len = length[result];
            int count;
            int nindex;
            byte b;
            int nindex2;
            char token;
            if (choice == 0 || choice == OFFSET_LOW_OFFSET_) {
                count = 0;
                nindex = 0;
                while (count < len && nindex != -1 && nindex < namelen) {
                    b = this.m_groupstring_[index + count];
                    count += OFFSET_HIGH_OFFSET_;
                    if (b < this.m_tokentable_.length) {
                        nindex2 = nindex + OFFSET_HIGH_OFFSET_;
                        if (name.charAt(nindex) != (b & Opcodes.OP_CONST_CLASS_JUMBO)) {
                            nindex2 = -1;
                        }
                    } else {
                        token = this.m_tokentable_[b & Opcodes.OP_CONST_CLASS_JUMBO];
                        if (token == '\ufffe') {
                            token = this.m_tokentable_[(b << 8) | (this.m_groupstring_[index + count] & Opcodes.OP_CONST_CLASS_JUMBO)];
                            count += OFFSET_HIGH_OFFSET_;
                        }
                        if (token != UnicodeMatcher.ETHER) {
                            nindex2 = nindex + OFFSET_HIGH_OFFSET_;
                            if (name.charAt(nindex) != (b & Opcodes.OP_CONST_CLASS_JUMBO)) {
                                nindex2 = -1;
                            }
                        } else {
                            nindex2 = UCharacterUtility.compareNullTermByteSubString(name, this.m_tokenstring_, nindex, token);
                        }
                    }
                    nindex = nindex2;
                }
                if (namelen != nindex && (count == len || this.m_groupstring_[index + count] == 59)) {
                    return result;
                }
                index += len;
                result += OFFSET_HIGH_OFFSET_;
            } else {
                int fieldIndex = choice == 4 ? OFFSET_LOW_OFFSET_ : choice;
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
                    count += OFFSET_HIGH_OFFSET_;
                    if (b < this.m_tokentable_.length) {
                        token = this.m_tokentable_[b & Opcodes.OP_CONST_CLASS_JUMBO];
                        if (token == '\ufffe') {
                            token = this.m_tokentable_[(b << 8) | (this.m_groupstring_[index + count] & Opcodes.OP_CONST_CLASS_JUMBO)];
                            count += OFFSET_HIGH_OFFSET_;
                        }
                        if (token != UnicodeMatcher.ETHER) {
                            nindex2 = UCharacterUtility.compareNullTermByteSubString(name, this.m_tokenstring_, nindex, token);
                        } else {
                            nindex2 = nindex + OFFSET_HIGH_OFFSET_;
                            if (name.charAt(nindex) != (b & Opcodes.OP_CONST_CLASS_JUMBO)) {
                                nindex2 = -1;
                            }
                        }
                    } else {
                        nindex2 = nindex + OFFSET_HIGH_OFFSET_;
                        if (name.charAt(nindex) != (b & Opcodes.OP_CONST_CLASS_JUMBO)) {
                            nindex2 = -1;
                        }
                    }
                    nindex = nindex2;
                }
                if (namelen != nindex) {
                }
                index += len;
                result += OFFSET_HIGH_OFFSET_;
            }
        }
        return -1;
    }

    private static int getType(int ch) {
        if (UCharacterUtility.isNonCharacter(ch)) {
            return NON_CHARACTER_;
        }
        int result = UCharacter.getType(ch);
        if (result == 18) {
            if (ch <= UTF16.LEAD_SURROGATE_MAX_VALUE) {
                result = LEAD_SURROGATE_;
            } else {
                result = TRAIL_SURROGATE_;
            }
        }
        return result;
    }

    private static int getExtendedChar(String name, int choice) {
        if (name.charAt(0) != '<') {
            return -2;
        }
        if (choice == OFFSET_LOW_OFFSET_) {
            int endIndex = name.length() - 1;
            if (name.charAt(endIndex) == '>') {
                int startIndex = name.lastIndexOf(45);
                if (startIndex >= 0) {
                    startIndex += OFFSET_HIGH_OFFSET_;
                    try {
                        int result = Integer.parseInt(name.substring(startIndex, endIndex), 16);
                        String type = name.substring(OFFSET_HIGH_OFFSET_, startIndex - 1);
                        int length = TYPE_NAMES_.length;
                        int i = 0;
                        while (i < length) {
                            if (type.compareTo(TYPE_NAMES_[i]) != 0) {
                                i += OFFSET_HIGH_OFFSET_;
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
        int i = ch >>> GROUP_SHIFT_;
        set[i] = set[i] | (OFFSET_HIGH_OFFSET_ << (ch & LEAD_SURROGATE_));
    }

    private static boolean contains(int[] set, char ch) {
        return (set[ch >>> GROUP_SHIFT_] & (OFFSET_HIGH_OFFSET_ << (ch & LEAD_SURROGATE_))) != 0;
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
            char b = (char) (this.m_groupstring_[offset + resultplength] & Opcodes.OP_CONST_CLASS_JUMBO);
            resultplength += OFFSET_HIGH_OFFSET_;
            if (b == ';') {
                break;
            } else if (b >= this.m_tokentable_.length) {
                add(set, b);
                resultnlength += OFFSET_HIGH_OFFSET_;
            } else {
                char token = this.m_tokentable_[b & Opcodes.OP_CONST_CLASS_JUMBO];
                if (token == '\ufffe') {
                    b = (char) ((b << 8) | (this.m_groupstring_[offset + resultplength] & Opcodes.OP_CONST_CLASS_JUMBO));
                    token = this.m_tokentable_[b];
                    resultplength += OFFSET_HIGH_OFFSET_;
                }
                if (token == UnicodeMatcher.ETHER) {
                    add(set, b);
                    resultnlength += OFFSET_HIGH_OFFSET_;
                } else {
                    byte tlength = tokenlength[b];
                    if (tlength == null) {
                        synchronized (this.m_utilStringBuffer_) {
                            this.m_utilStringBuffer_.delete(0, this.m_utilStringBuffer_.length());
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
        this.m_utilIntBuffer_[OFFSET_HIGH_OFFSET_] = resultplength;
        return this.m_utilIntBuffer_;
    }

    private void addGroupName(int maxlength) {
        int maxisolength = 0;
        char[] offsets = new char[34];
        char[] lengths = new char[34];
        byte[] tokenlengths = new byte[this.m_tokentable_.length];
        for (int i = 0; i < this.m_groupcount_; i += OFFSET_HIGH_OFFSET_) {
            int offset = getGroupLengths(i, offsets, lengths);
            for (int linenumber = 0; linenumber < TRAIL_SURROGATE_; linenumber += OFFSET_HIGH_OFFSET_) {
                int lineoffset = offset + offsets[linenumber];
                int length = lengths[linenumber];
                if (length != 0) {
                    int[] parsed = addGroupName(lineoffset, length, tokenlengths, this.m_nameSet_);
                    if (parsed[0] > maxlength) {
                        maxlength = parsed[0];
                    }
                    lineoffset += parsed[OFFSET_HIGH_OFFSET_];
                    if (parsed[OFFSET_HIGH_OFFSET_] < length) {
                        length -= parsed[OFFSET_HIGH_OFFSET_];
                        parsed = addGroupName(lineoffset, length, tokenlengths, this.m_nameSet_);
                        if (parsed[0] > maxlength) {
                            maxlength = parsed[0];
                        }
                        lineoffset += parsed[OFFSET_HIGH_OFFSET_];
                        if (parsed[OFFSET_HIGH_OFFSET_] < length) {
                            length -= parsed[OFFSET_HIGH_OFFSET_];
                            if (addGroupName(lineoffset, length, tokenlengths, this.m_ISOCommentSet_)[OFFSET_HIGH_OFFSET_] > maxisolength) {
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
            for (int i = '\u00ff'; i > '\u0000'; i = (char) (i - 1)) {
                if (contains(set, i)) {
                    uset.add(i);
                }
            }
        }
    }
}
