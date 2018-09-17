package android.icu.text;

import dalvik.bytecode.Opcodes;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public final class CollationKey implements Comparable<CollationKey> {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int MERGE_SEPERATOR_ = 2;
    private int m_hashCode_;
    private byte[] m_key_;
    private int m_length_;
    private String m_source_;

    public static final class BoundMode {
        public static final int COUNT = 3;
        public static final int LOWER = 0;
        public static final int UPPER = 1;
        public static final int UPPER_LONG = 2;

        private BoundMode() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CollationKey.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CollationKey.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CollationKey.<clinit>():void");
    }

    public CollationKey(String source, byte[] key) {
        this(source, key, -1);
    }

    private CollationKey(String source, byte[] key, int length) {
        this.m_source_ = source;
        this.m_key_ = key;
        this.m_hashCode_ = 0;
        this.m_length_ = length;
    }

    public CollationKey(String source, RawCollationKey key) {
        this.m_source_ = source;
        this.m_length_ = key.size - 1;
        this.m_key_ = key.releaseBytes();
        if (!-assertionsDisabled) {
            if ((this.m_key_[this.m_length_] == null ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        this.m_hashCode_ = 0;
    }

    public String getSourceString() {
        return this.m_source_;
    }

    public byte[] toByteArray() {
        int length = getLength() + 1;
        byte[] result = new byte[length];
        System.arraycopy(this.m_key_, 0, result, 0, length);
        return result;
    }

    public int compareTo(CollationKey target) {
        int i = 0;
        while (true) {
            int l = this.m_key_[i] & Opcodes.OP_CONST_CLASS_JUMBO;
            int r = target.m_key_[i] & Opcodes.OP_CONST_CLASS_JUMBO;
            if (l < r) {
                return -1;
            }
            if (l > r) {
                return 1;
            }
            if (l == 0) {
                return 0;
            }
            i++;
        }
    }

    public boolean equals(Object target) {
        if (target instanceof CollationKey) {
            return equals((CollationKey) target);
        }
        return -assertionsDisabled;
    }

    public boolean equals(CollationKey target) {
        if (this == target) {
            return true;
        }
        if (target == null) {
            return -assertionsDisabled;
        }
        CollationKey other = target;
        for (int i = 0; this.m_key_[i] == target.m_key_[i]; i++) {
            if (this.m_key_[i] == null) {
                return true;
            }
        }
        return -assertionsDisabled;
    }

    public int hashCode() {
        if (this.m_hashCode_ == 0) {
            if (this.m_key_ == null) {
                this.m_hashCode_ = 1;
            } else {
                StringBuilder key = new StringBuilder(this.m_key_.length >> 1);
                int i = 0;
                while (this.m_key_[i] != null && this.m_key_[i + 1] != null) {
                    key.append((char) ((this.m_key_[i] << 8) | (this.m_key_[i + 1] & Opcodes.OP_CONST_CLASS_JUMBO)));
                    i += MERGE_SEPERATOR_;
                }
                if (this.m_key_[i] != null) {
                    key.append((char) (this.m_key_[i] << 8));
                }
                this.m_hashCode_ = key.toString().hashCode();
            }
        }
        return this.m_hashCode_;
    }

    public CollationKey getBound(int boundType, int noOfLevels) {
        int offset;
        int offset2 = 0;
        int keystrength = 0;
        if (noOfLevels > 0) {
            while (offset2 < this.m_key_.length && this.m_key_[offset2] != null) {
                offset = offset2 + 1;
                if (this.m_key_[offset2] == 1) {
                    keystrength++;
                    noOfLevels--;
                    if (noOfLevels == 0 || offset == this.m_key_.length || this.m_key_[offset] == null) {
                        offset--;
                        break;
                    }
                }
                offset2 = offset;
            }
            offset = offset2;
        } else {
            offset = 0;
        }
        if (noOfLevels > 0) {
            throw new IllegalArgumentException("Source collation key has only " + keystrength + " strength level. Call getBound() again " + " with noOfLevels < " + keystrength);
        }
        byte[] resultkey = new byte[((offset + boundType) + 1)];
        System.arraycopy(this.m_key_, 0, resultkey, 0, offset);
        switch (boundType) {
            case XmlPullParser.START_DOCUMENT /*0*/:
                offset2 = offset;
                break;
            case NodeFilter.SHOW_ELEMENT /*1*/:
                offset2 = offset + 1;
                resultkey[offset] = (byte) 2;
                break;
            case MERGE_SEPERATOR_ /*2*/:
                offset2 = offset + 1;
                resultkey[offset] = (byte) -1;
                offset = offset2 + 1;
                resultkey[offset2] = (byte) -1;
                offset2 = offset;
                break;
            default:
                throw new IllegalArgumentException("Illegal boundType argument");
        }
        resultkey[offset2] = (byte) 0;
        return new CollationKey(null, resultkey, offset2);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public CollationKey merge(CollationKey source) {
        Object obj = 1;
        if (source == null || source.getLength() == 0) {
            throw new IllegalArgumentException("CollationKey argument can not be null or of 0 length");
        }
        int remainingLength;
        byte[] result = new byte[((getLength() + source.getLength()) + MERGE_SEPERATOR_)];
        int rindex = 0;
        int index = 0;
        int sourceindex = 0;
        while (true) {
            int rindex2;
            if (this.m_key_[index] < null || this.m_key_[index] >= (byte) 2) {
                rindex2 = rindex + 1;
                int index2 = index + 1;
                result[rindex] = this.m_key_[index];
                index = index2;
                rindex = rindex2;
            } else {
                rindex2 = rindex + 1;
                result[rindex] = (byte) 2;
                rindex = rindex2;
                while (true) {
                    if (source.m_key_[sourceindex] >= null && source.m_key_[sourceindex] < (byte) 2) {
                        break;
                    }
                    rindex2 = rindex + 1;
                    int sourceindex2 = sourceindex + 1;
                    result[rindex] = source.m_key_[sourceindex];
                    sourceindex = sourceindex2;
                    rindex = rindex2;
                }
                if (this.m_key_[index] == (byte) 1 && source.m_key_[sourceindex] == (byte) 1) {
                    index++;
                    sourceindex++;
                    rindex2 = rindex + 1;
                    result[rindex] = (byte) 1;
                    rindex = rindex2;
                } else {
                    remainingLength = this.m_length_ - index;
                }
            }
        }
        remainingLength = this.m_length_ - index;
        if (remainingLength > 0) {
            System.arraycopy(this.m_key_, index, result, rindex, remainingLength);
            rindex += remainingLength;
        } else {
            remainingLength = source.m_length_ - sourceindex;
            if (remainingLength > 0) {
                System.arraycopy(source.m_key_, sourceindex, result, rindex, remainingLength);
                rindex += remainingLength;
            }
        }
        result[rindex] = (byte) 0;
        if (!-assertionsDisabled) {
            if (rindex != result.length - 1) {
                obj = (byte) 0;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        return new CollationKey(null, result, rindex);
    }

    private int getLength() {
        if (this.m_length_ >= 0) {
            return this.m_length_;
        }
        int length = this.m_key_.length;
        for (int index = 0; index < length; index++) {
            if (this.m_key_[index] == null) {
                length = index;
                break;
            }
        }
        this.m_length_ = length;
        return this.m_length_;
    }
}
