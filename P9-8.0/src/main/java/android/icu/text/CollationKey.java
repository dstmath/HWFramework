package android.icu.text;

public final class CollationKey implements Comparable<CollationKey> {
    static final /* synthetic */ boolean -assertionsDisabled = (CollationKey.class.desiredAssertionStatus() ^ 1);
    private static final int MERGE_SEPERATOR_ = 2;
    private int m_hashCode_;
    private byte[] m_key_;
    private int m_length_;
    private String m_source_;

    public static final class BoundMode {
        @Deprecated
        public static final int COUNT = 3;
        public static final int LOWER = 0;
        public static final int UPPER = 1;
        public static final int UPPER_LONG = 2;

        private BoundMode() {
        }
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
        if (-assertionsDisabled || this.m_key_[this.m_length_] == (byte) 0) {
            this.m_hashCode_ = 0;
            return;
        }
        throw new AssertionError();
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
            int l = this.m_key_[i] & 255;
            int r = target.m_key_[i] & 255;
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
        return false;
    }

    public boolean equals(CollationKey target) {
        if (this == target) {
            return true;
        }
        if (target == null) {
            return false;
        }
        CollationKey other = target;
        for (int i = 0; this.m_key_[i] == target.m_key_[i]; i++) {
            if (this.m_key_[i] == (byte) 0) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        if (this.m_hashCode_ == 0) {
            if (this.m_key_ == null) {
                this.m_hashCode_ = 1;
            } else {
                StringBuilder key = new StringBuilder(this.m_key_.length >> 1);
                int i = 0;
                while (this.m_key_[i] != (byte) 0 && this.m_key_[i + 1] != (byte) 0) {
                    key.append((char) ((this.m_key_[i] << 8) | (this.m_key_[i + 1] & 255)));
                    i += 2;
                }
                if (this.m_key_[i] != (byte) 0) {
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
            while (offset2 < this.m_key_.length && this.m_key_[offset2] != (byte) 0) {
                offset = offset2 + 1;
                if (this.m_key_[offset2] == (byte) 1) {
                    keystrength++;
                    noOfLevels--;
                    if (noOfLevels == 0 || offset == this.m_key_.length || this.m_key_[offset] == (byte) 0) {
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
            case 0:
                offset2 = offset;
                break;
            case 1:
                offset2 = offset + 1;
                resultkey[offset] = (byte) 2;
                break;
            case 2:
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

    public CollationKey merge(CollationKey source) {
        if (source == null || source.getLength() == 0) {
            throw new IllegalArgumentException("CollationKey argument can not be null or of 0 length");
        }
        int remainingLength;
        byte[] result = new byte[((getLength() + source.getLength()) + 2)];
        int rindex = 0;
        int index = 0;
        int sourceindex = 0;
        while (true) {
            int rindex2;
            if (this.m_key_[index] < (byte) 0 || this.m_key_[index] >= (byte) 2) {
                rindex2 = rindex + 1;
                int index2 = index + 1;
                result[rindex] = this.m_key_[index];
                index = index2;
                rindex = rindex2;
            } else {
                rindex2 = rindex + 1;
                result[rindex] = (byte) 2;
                while (true) {
                    rindex = rindex2;
                    if (source.m_key_[sourceindex] >= (byte) 0 && source.m_key_[sourceindex] < (byte) 2) {
                        break;
                    }
                    rindex2 = rindex + 1;
                    int sourceindex2 = sourceindex + 1;
                    result[rindex] = source.m_key_[sourceindex];
                    sourceindex = sourceindex2;
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
        if (-assertionsDisabled || rindex == result.length - 1) {
            return new CollationKey(null, result, rindex);
        }
        throw new AssertionError();
    }

    private int getLength() {
        if (this.m_length_ >= 0) {
            return this.m_length_;
        }
        int length = this.m_key_.length;
        for (int index = 0; index < length; index++) {
            if (this.m_key_[index] == (byte) 0) {
                length = index;
                break;
            }
        }
        this.m_length_ = length;
        return this.m_length_;
    }
}
