package ohos.global.icu.text;

public final class CollationKey implements Comparable<CollationKey> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
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

    public CollationKey(String str, byte[] bArr) {
        this(str, bArr, -1);
    }

    private CollationKey(String str, byte[] bArr, int i) {
        this.m_source_ = str;
        this.m_key_ = bArr;
        this.m_hashCode_ = 0;
        this.m_length_ = i;
    }

    public CollationKey(String str, RawCollationKey rawCollationKey) {
        this.m_source_ = str;
        this.m_length_ = rawCollationKey.size - 1;
        this.m_key_ = rawCollationKey.releaseBytes();
        this.m_hashCode_ = 0;
    }

    public String getSourceString() {
        return this.m_source_;
    }

    public byte[] toByteArray() {
        int length = getLength() + 1;
        byte[] bArr = new byte[length];
        System.arraycopy(this.m_key_, 0, bArr, 0, length);
        return bArr;
    }

    public int compareTo(CollationKey collationKey) {
        int i = 0;
        while (true) {
            int i2 = this.m_key_[i] & 255;
            int i3 = collationKey.m_key_[i] & 255;
            if (i2 < i3) {
                return -1;
            }
            if (i2 > i3) {
                return 1;
            }
            if (i2 == 0) {
                return 0;
            }
            i++;
        }
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (!(obj instanceof CollationKey)) {
            return false;
        }
        return equals((CollationKey) obj);
    }

    public boolean equals(CollationKey collationKey) {
        if (this == collationKey) {
            return true;
        }
        if (collationKey == null) {
            return false;
        }
        int i = 0;
        while (true) {
            byte[] bArr = this.m_key_;
            if (bArr[i] != collationKey.m_key_[i]) {
                return false;
            }
            if (bArr[i] == 0) {
                return true;
            }
            i++;
        }
    }

    @Override // java.lang.Object
    public int hashCode() {
        if (this.m_hashCode_ == 0) {
            byte[] bArr = this.m_key_;
            if (bArr == null) {
                this.m_hashCode_ = 1;
            } else {
                StringBuilder sb = new StringBuilder(bArr.length >> 1);
                int i = 0;
                while (true) {
                    byte[] bArr2 = this.m_key_;
                    if (bArr2[i] == 0) {
                        break;
                    }
                    int i2 = i + 1;
                    if (bArr2[i2] == 0) {
                        break;
                    }
                    sb.append((char) ((bArr2[i2] & 255) | (bArr2[i] << 8)));
                    i += 2;
                }
                byte[] bArr3 = this.m_key_;
                if (bArr3[i] != 0) {
                    sb.append((char) (bArr3[i] << 8));
                }
                this.m_hashCode_ = sb.toString().hashCode();
            }
        }
        return this.m_hashCode_;
    }

    public CollationKey getBound(int i, int i2) {
        int i3;
        int i4;
        int i5;
        int i6 = i2;
        if (i2 > 0) {
            i3 = 0;
            i4 = 0;
            while (true) {
                byte[] bArr = this.m_key_;
                if (i3 >= bArr.length || bArr[i3] == 0) {
                    break;
                }
                i5 = i3 + 1;
                if (bArr[i3] == 1) {
                    i4++;
                    i6--;
                    if (i6 == 0 || i5 == bArr.length || bArr[i5] == 0) {
                        break;
                    }
                }
                i3 = i5;
            }
            i3 = i5 - 1;
        } else {
            i3 = 0;
            i4 = 0;
        }
        if (i6 <= 0) {
            byte[] bArr2 = new byte[(i3 + i + 1)];
            System.arraycopy(this.m_key_, 0, bArr2, 0, i3);
            if (i != 0) {
                if (i == 1) {
                    bArr2[i3] = 2;
                    i3++;
                } else if (i == 2) {
                    int i7 = i3 + 1;
                    bArr2[i3] = -1;
                    i3 = i7 + 1;
                    bArr2[i7] = -1;
                } else {
                    throw new IllegalArgumentException("Illegal boundType argument");
                }
            }
            bArr2[i3] = 0;
            return new CollationKey(null, bArr2, i3);
        }
        throw new IllegalArgumentException("Source collation key has only " + i4 + " strength level. Call getBound() again  with noOfLevels < " + i4);
    }

    public CollationKey merge(CollationKey collationKey) {
        int i;
        byte[] bArr;
        if (collationKey == null || collationKey.getLength() == 0) {
            throw new IllegalArgumentException("CollationKey argument can not be null or of 0 length");
        }
        byte[] bArr2 = new byte[(getLength() + collationKey.getLength() + 2)];
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        while (true) {
            byte[] bArr3 = this.m_key_;
            if (bArr3[i2] < 0 || bArr3[i2] >= 2) {
                bArr2[i3] = this.m_key_[i2];
                i3++;
                i2++;
            } else {
                i = i3 + 1;
                bArr2[i3] = 2;
                while (true) {
                    bArr = collationKey.m_key_;
                    if (bArr[i4] >= 0 && bArr[i4] < 2) {
                        break;
                    }
                    bArr2[i] = collationKey.m_key_[i4];
                    i++;
                    i4++;
                }
                if (this.m_key_[i2] != 1 || bArr[i4] != 1) {
                    break;
                }
                i2++;
                i4++;
                i3 = i + 1;
                bArr2[i] = 1;
            }
        }
        int i5 = this.m_length_ - i2;
        if (i5 > 0) {
            System.arraycopy(this.m_key_, i2, bArr2, i, i5);
            i += i5;
        } else {
            int i6 = collationKey.m_length_ - i4;
            if (i6 > 0) {
                System.arraycopy(collationKey.m_key_, i4, bArr2, i, i6);
                i += i6;
            }
        }
        bArr2[i] = 0;
        return new CollationKey(null, bArr2, i);
    }

    private int getLength() {
        int i = this.m_length_;
        if (i >= 0) {
            return i;
        }
        int length = this.m_key_.length;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                break;
            } else if (this.m_key_[i2] == 0) {
                length = i2;
                break;
            } else {
                i2++;
            }
        }
        this.m_length_ = length;
        return this.m_length_;
    }
}
