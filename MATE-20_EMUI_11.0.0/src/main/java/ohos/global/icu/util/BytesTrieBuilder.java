package ohos.global.icu.util;

import java.nio.ByteBuffer;
import ohos.global.icu.util.StringTrieBuilder;
import ohos.media.camera.params.Metadata;

public final class BytesTrieBuilder extends StringTrieBuilder {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private byte[] bytes;
    private int bytesLength;
    private final byte[] intBytes = new byte[5];

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.StringTrieBuilder
    @Deprecated
    public int getMaxBranchLinearSubNodeLength() {
        return 5;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.StringTrieBuilder
    @Deprecated
    public int getMaxLinearMatchLength() {
        return 16;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.StringTrieBuilder
    @Deprecated
    public int getMinLinearMatch() {
        return 16;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.StringTrieBuilder
    @Deprecated
    public boolean matchNodesCanHaveValues() {
        return false;
    }

    private static final class BytesAsCharSequence implements CharSequence {
        private int len;
        private byte[] s;

        @Override // java.lang.CharSequence
        public CharSequence subSequence(int i, int i2) {
            return null;
        }

        public BytesAsCharSequence(byte[] bArr, int i) {
            this.s = bArr;
            this.len = i;
        }

        @Override // java.lang.CharSequence
        public char charAt(int i) {
            return (char) (this.s[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE);
        }

        @Override // java.lang.CharSequence
        public int length() {
            return this.len;
        }
    }

    public BytesTrieBuilder add(byte[] bArr, int i, int i2) {
        addImpl(new BytesAsCharSequence(bArr, i), i2);
        return this;
    }

    public BytesTrie build(StringTrieBuilder.Option option) {
        buildBytes(option);
        byte[] bArr = this.bytes;
        return new BytesTrie(bArr, bArr.length - this.bytesLength);
    }

    public ByteBuffer buildByteBuffer(StringTrieBuilder.Option option) {
        buildBytes(option);
        byte[] bArr = this.bytes;
        int length = bArr.length;
        int i = this.bytesLength;
        return ByteBuffer.wrap(bArr, length - i, i);
    }

    private void buildBytes(StringTrieBuilder.Option option) {
        if (this.bytes == null) {
            this.bytes = new byte[1024];
        }
        buildImpl(option);
    }

    public BytesTrieBuilder clear() {
        clearImpl();
        this.bytes = null;
        this.bytesLength = 0;
        return this;
    }

    private void ensureCapacity(int i) {
        byte[] bArr = this.bytes;
        if (i > bArr.length) {
            int length = bArr.length;
            do {
                length *= 2;
            } while (length <= i);
            byte[] bArr2 = new byte[length];
            byte[] bArr3 = this.bytes;
            int length2 = bArr3.length;
            int i2 = this.bytesLength;
            System.arraycopy(bArr3, length2 - i2, bArr2, bArr2.length - i2, i2);
            this.bytes = bArr2;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.StringTrieBuilder
    @Deprecated
    public int write(int i) {
        int i2 = this.bytesLength + 1;
        ensureCapacity(i2);
        this.bytesLength = i2;
        byte[] bArr = this.bytes;
        int length = bArr.length;
        int i3 = this.bytesLength;
        bArr[length - i3] = (byte) i;
        return i3;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.StringTrieBuilder
    @Deprecated
    public int write(int i, int i2) {
        int i3 = this.bytesLength + i2;
        ensureCapacity(i3);
        this.bytesLength = i3;
        int length = this.bytes.length - this.bytesLength;
        while (i2 > 0) {
            this.bytes[length] = (byte) this.strings.charAt(i);
            i2--;
            length++;
            i++;
        }
        return this.bytesLength;
    }

    private int write(byte[] bArr, int i) {
        int i2 = this.bytesLength + i;
        ensureCapacity(i2);
        this.bytesLength = i2;
        byte[] bArr2 = this.bytes;
        System.arraycopy(bArr, 0, bArr2, bArr2.length - this.bytesLength, i);
        return this.bytesLength;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.StringTrieBuilder
    @Deprecated
    public int writeValueAndFinal(int i, boolean z) {
        int i2;
        int i3;
        if (i >= 0 && i <= 64) {
            return write(((i + 16) << 1) | z);
        }
        int i4 = 2;
        if (i < 0 || i > 16777215) {
            byte[] bArr = this.intBytes;
            bArr[0] = Byte.MAX_VALUE;
            bArr[1] = (byte) (i >> 24);
            bArr[2] = (byte) (i >> 16);
            bArr[3] = (byte) (i >> 8);
            bArr[4] = (byte) i;
            i2 = 5;
        } else {
            if (i <= 6911) {
                this.intBytes[0] = (byte) ((i >> 8) + 81);
                i3 = 1;
            } else {
                if (i <= 1179647) {
                    this.intBytes[0] = (byte) ((i >> 16) + 108);
                    i4 = 1;
                } else {
                    byte[] bArr2 = this.intBytes;
                    bArr2[0] = 126;
                    bArr2[1] = (byte) (i >> 16);
                }
                i3 = i4 + 1;
                this.intBytes[i4] = (byte) (i >> 8);
            }
            i2 = i3 + 1;
            this.intBytes[i3] = (byte) i;
        }
        byte[] bArr3 = this.intBytes;
        bArr3[0] = (byte) ((z ? 1 : 0) | (bArr3[0] << 1));
        return write(bArr3, i2);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.StringTrieBuilder
    @Deprecated
    public int writeValueAndType(boolean z, int i, int i2) {
        return z ? writeValueAndFinal(i, false) : write(i2);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.StringTrieBuilder
    @Deprecated
    public int writeDeltaTo(int i) {
        int i2;
        int i3 = this.bytesLength - i;
        if (i3 <= 191) {
            return write(i3);
        }
        if (i3 <= 12287) {
            this.intBytes[0] = (byte) ((i3 >> 8) + 192);
            i2 = 1;
        } else {
            if (i3 <= 917503) {
                this.intBytes[0] = (byte) ((i3 >> 16) + 240);
                i2 = 2;
            } else {
                if (i3 <= 16777215) {
                    this.intBytes[0] = -2;
                    i2 = 3;
                } else {
                    byte[] bArr = this.intBytes;
                    bArr[0] = -1;
                    bArr[1] = (byte) (i3 >> 24);
                    i2 = 4;
                }
                this.intBytes[1] = (byte) (i3 >> 16);
            }
            this.intBytes[1] = (byte) (i3 >> 8);
        }
        byte[] bArr2 = this.intBytes;
        bArr2[i2] = (byte) i3;
        return write(bArr2, i2 + 1);
    }
}
