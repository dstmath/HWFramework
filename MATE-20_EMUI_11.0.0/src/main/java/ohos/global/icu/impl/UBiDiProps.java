package ohos.global.icu.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import ohos.global.icu.impl.ICUBinary;
import ohos.global.icu.impl.Trie2;
import ohos.global.icu.text.DictionaryData;
import ohos.global.icu.text.UnicodeSet;
import ohos.global.icu.util.ICUUncheckedIOException;

public final class UBiDiProps {
    private static final int BIDI_CONTROL_SHIFT = 11;
    private static final int BPT_MASK = 768;
    private static final int BPT_SHIFT = 8;
    private static final int CLASS_MASK = 31;
    private static final String DATA_FILE_NAME = "ubidi.icu";
    private static final String DATA_NAME = "ubidi";
    private static final String DATA_TYPE = "icu";
    private static final int ESC_MIRROR_DELTA = -4;
    private static final int FMT = 1114195049;
    public static final UBiDiProps INSTANCE;
    private static final int IS_MIRRORED_SHIFT = 12;
    private static final int IX_JG_LIMIT = 5;
    private static final int IX_JG_LIMIT2 = 7;
    private static final int IX_JG_START = 4;
    private static final int IX_JG_START2 = 6;
    private static final int IX_MAX_VALUES = 15;
    private static final int IX_MIRROR_LENGTH = 3;
    private static final int IX_TOP = 16;
    private static final int IX_TRIE_SIZE = 2;
    private static final int JOIN_CONTROL_SHIFT = 10;
    private static final int JT_MASK = 224;
    private static final int JT_SHIFT = 5;
    private static final int MAX_JG_MASK = 16711680;
    private static final int MAX_JG_SHIFT = 16;
    private static final int MIRROR_DELTA_SHIFT = 13;
    private static final int MIRROR_INDEX_SHIFT = 21;
    private int[] indexes;
    private byte[] jgArray;
    private byte[] jgArray2;
    private int[] mirrors;
    private Trie2_16 trie;

    private static final int getClassFromProps(int i) {
        return i & 31;
    }

    private static final boolean getFlagFromProps(int i, int i2) {
        return ((i >> i2) & 1) != 0;
    }

    private static final int getMirrorCodePoint(int i) {
        return i & DictionaryData.TRANSFORM_OFFSET_MASK;
    }

    private static final int getMirrorDeltaFromProps(int i) {
        return ((short) i) >> 13;
    }

    private static final int getMirrorIndex(int i) {
        return i >>> 21;
    }

    private UBiDiProps() throws IOException {
        readData(ICUBinary.getData(DATA_FILE_NAME));
    }

    private void readData(ByteBuffer byteBuffer) throws IOException {
        ICUBinary.readHeader(byteBuffer, FMT, new IsAcceptable());
        int i = byteBuffer.getInt();
        if (i >= 16) {
            this.indexes = new int[i];
            this.indexes[0] = i;
            for (int i2 = 1; i2 < i; i2++) {
                this.indexes[i2] = byteBuffer.getInt();
            }
            this.trie = Trie2_16.createFromSerialized(byteBuffer);
            int i3 = this.indexes[2];
            int serializedLength = this.trie.getSerializedLength();
            if (serializedLength <= i3) {
                ICUBinary.skipBytes(byteBuffer, i3 - serializedLength);
                int i4 = this.indexes[3];
                if (i4 > 0) {
                    this.mirrors = ICUBinary.getInts(byteBuffer, i4, 0);
                }
                int[] iArr = this.indexes;
                this.jgArray = new byte[(iArr[5] - iArr[4])];
                byteBuffer.get(this.jgArray);
                int[] iArr2 = this.indexes;
                this.jgArray2 = new byte[(iArr2[7] - iArr2[6])];
                byteBuffer.get(this.jgArray2);
                return;
            }
            throw new IOException("ubidi.icu: not enough bytes for the trie");
        }
        throw new IOException("indexes[0] too small in ubidi.icu");
    }

    /* access modifiers changed from: private */
    public static final class IsAcceptable implements ICUBinary.Authenticate {
        private IsAcceptable() {
        }

        @Override // ohos.global.icu.impl.ICUBinary.Authenticate
        public boolean isDataVersionAcceptable(byte[] bArr) {
            return bArr[0] == 2;
        }
    }

    public final void addPropertyStarts(UnicodeSet unicodeSet) {
        Iterator<Trie2.Range> it = this.trie.iterator();
        while (it.hasNext()) {
            Trie2.Range next = it.next();
            if (next.leadSurrogate) {
                break;
            }
            unicodeSet.add(next.startCodePoint);
        }
        int i = this.indexes[3];
        for (int i2 = 0; i2 < i; i2++) {
            int mirrorCodePoint = getMirrorCodePoint(this.mirrors[i2]);
            unicodeSet.add(mirrorCodePoint, mirrorCodePoint + 1);
        }
        int[] iArr = this.indexes;
        int i3 = iArr[4];
        int i4 = iArr[5];
        byte[] bArr = this.jgArray;
        while (true) {
            int i5 = i4 - i3;
            byte b = 0;
            int i6 = i3;
            for (int i7 = 0; i7 < i5; i7++) {
                byte b2 = bArr[i7];
                if (b2 != b) {
                    unicodeSet.add(i6);
                    b = b2;
                }
                i6++;
            }
            if (b != 0) {
                unicodeSet.add(i4);
            }
            int[] iArr2 = this.indexes;
            if (i4 == iArr2[5]) {
                int i8 = iArr2[6];
                int i9 = iArr2[7];
                bArr = this.jgArray2;
                i3 = i8;
                i4 = i9;
            } else {
                return;
            }
        }
    }

    public final int getMaxValue(int i) {
        int i2 = this.indexes[15];
        if (i == 4096) {
            return i2 & 31;
        }
        if (i == 4117) {
            return (i2 & 768) >> 8;
        }
        if (i == 4102) {
            return (i2 & MAX_JG_MASK) >> 16;
        }
        if (i != 4103) {
            return -1;
        }
        return (i2 & 224) >> 5;
    }

    public final int getClass(int i) {
        return getClassFromProps(this.trie.get(i));
    }

    public final boolean isMirrored(int i) {
        return getFlagFromProps(this.trie.get(i), 12);
    }

    private final int getMirror(int i, int i2) {
        int mirrorDeltaFromProps = getMirrorDeltaFromProps(i2);
        if (mirrorDeltaFromProps != -4) {
            return i + mirrorDeltaFromProps;
        }
        int i3 = this.indexes[3];
        for (int i4 = 0; i4 < i3; i4++) {
            int i5 = this.mirrors[i4];
            int mirrorCodePoint = getMirrorCodePoint(i5);
            if (i == mirrorCodePoint) {
                return getMirrorCodePoint(this.mirrors[getMirrorIndex(i5)]);
            }
            if (i < mirrorCodePoint) {
                break;
            }
        }
        return i;
    }

    public final int getMirror(int i) {
        return getMirror(i, this.trie.get(i));
    }

    public final boolean isBidiControl(int i) {
        return getFlagFromProps(this.trie.get(i), 11);
    }

    public final boolean isJoinControl(int i) {
        return getFlagFromProps(this.trie.get(i), 10);
    }

    public final int getJoiningType(int i) {
        return (this.trie.get(i) & 224) >> 5;
    }

    public final int getJoiningGroup(int i) {
        byte b;
        int[] iArr = this.indexes;
        int i2 = iArr[4];
        int i3 = iArr[5];
        if (i2 > i || i >= i3) {
            int[] iArr2 = this.indexes;
            int i4 = iArr2[6];
            int i5 = iArr2[7];
            if (i4 > i || i >= i5) {
                return 0;
            }
            b = this.jgArray2[i - i4];
        } else {
            b = this.jgArray[i - i2];
        }
        return b & 255;
    }

    public final int getPairedBracketType(int i) {
        return (this.trie.get(i) & 768) >> 8;
    }

    public final int getPairedBracket(int i) {
        int i2 = this.trie.get(i);
        if ((i2 & 768) == 0) {
            return i;
        }
        return getMirror(i, i2);
    }

    static {
        try {
            INSTANCE = new UBiDiProps();
        } catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }
}
