package android.icu.util;

import android.icu.text.Bidi;
import android.icu.util.StringTrieBuilder.Option;
import dalvik.bytecode.Opcodes;
import java.nio.ByteBuffer;

public final class BytesTrieBuilder extends StringTrieBuilder {
    static final /* synthetic */ boolean -assertionsDisabled = (BytesTrieBuilder.class.desiredAssertionStatus() ^ 1);
    private byte[] bytes;
    private int bytesLength;
    private final byte[] intBytes = new byte[5];

    private static final class BytesAsCharSequence implements CharSequence {
        private int len;
        private byte[] s;

        public BytesAsCharSequence(byte[] sequence, int length) {
            this.s = sequence;
            this.len = length;
        }

        public char charAt(int i) {
            return (char) (this.s[i] & 255);
        }

        public int length() {
            return this.len;
        }

        public CharSequence subSequence(int start, int end) {
            return null;
        }
    }

    public BytesTrieBuilder add(byte[] sequence, int length, int value) {
        addImpl(new BytesAsCharSequence(sequence, length), value);
        return this;
    }

    public BytesTrie build(Option buildOption) {
        buildBytes(buildOption);
        return new BytesTrie(this.bytes, this.bytes.length - this.bytesLength);
    }

    public ByteBuffer buildByteBuffer(Option buildOption) {
        buildBytes(buildOption);
        return ByteBuffer.wrap(this.bytes, this.bytes.length - this.bytesLength, this.bytesLength);
    }

    private void buildBytes(Option buildOption) {
        if (this.bytes == null) {
            this.bytes = new byte[1024];
        }
        buildImpl(buildOption);
    }

    public BytesTrieBuilder clear() {
        clearImpl();
        this.bytes = null;
        this.bytesLength = 0;
        return this;
    }

    @Deprecated
    protected boolean matchNodesCanHaveValues() {
        return false;
    }

    @Deprecated
    protected int getMaxBranchLinearSubNodeLength() {
        return 5;
    }

    @Deprecated
    protected int getMinLinearMatch() {
        return 16;
    }

    @Deprecated
    protected int getMaxLinearMatchLength() {
        return 16;
    }

    private void ensureCapacity(int length) {
        if (length > this.bytes.length) {
            int newCapacity = this.bytes.length;
            do {
                newCapacity *= 2;
            } while (newCapacity <= length);
            byte[] newBytes = new byte[newCapacity];
            System.arraycopy(this.bytes, this.bytes.length - this.bytesLength, newBytes, newBytes.length - this.bytesLength, this.bytesLength);
            this.bytes = newBytes;
        }
    }

    @Deprecated
    protected int write(int b) {
        int newLength = this.bytesLength + 1;
        ensureCapacity(newLength);
        this.bytesLength = newLength;
        this.bytes[this.bytes.length - this.bytesLength] = (byte) b;
        return this.bytesLength;
    }

    @Deprecated
    protected int write(int offset, int length) {
        int newLength = this.bytesLength + length;
        ensureCapacity(newLength);
        this.bytesLength = newLength;
        int bytesOffset = this.bytes.length - this.bytesLength;
        int offset2 = offset;
        while (length > 0) {
            int bytesOffset2 = bytesOffset + 1;
            offset = offset2 + 1;
            this.bytes[bytesOffset] = (byte) this.strings.charAt(offset2);
            length--;
            bytesOffset = bytesOffset2;
            offset2 = offset;
        }
        return this.bytesLength;
    }

    private int write(byte[] b, int length) {
        int newLength = this.bytesLength + length;
        ensureCapacity(newLength);
        this.bytesLength = newLength;
        System.arraycopy(b, 0, this.bytes, this.bytes.length - this.bytesLength, length);
        return this.bytesLength;
    }

    @Deprecated
    protected int writeValueAndFinal(int i, boolean isFinal) {
        int i2 = 1;
        if (i < 0 || i > 64) {
            int length = 1;
            if (i < 0 || i > 16777215) {
                this.intBytes[0] = Bidi.LEVEL_DEFAULT_RTL;
                this.intBytes[1] = (byte) (i >> 24);
                this.intBytes[2] = (byte) (i >> 16);
                this.intBytes[3] = (byte) (i >> 8);
                this.intBytes[4] = (byte) i;
                length = 5;
            } else {
                int length2;
                if (i <= Opcodes.OP_SGET_SHORT_JUMBO) {
                    this.intBytes[0] = (byte) ((i >> 8) + 81);
                } else {
                    if (i <= 1179647) {
                        this.intBytes[0] = (byte) ((i >> 16) + 108);
                    } else {
                        this.intBytes[0] = Bidi.LEVEL_DEFAULT_LTR;
                        this.intBytes[1] = (byte) (i >> 16);
                        length = 2;
                    }
                    length2 = length + 1;
                    this.intBytes[length] = (byte) (i >> 8);
                    length = length2;
                }
                length2 = length + 1;
                this.intBytes[length] = (byte) i;
                length = length2;
            }
            byte[] bArr = this.intBytes;
            int i3 = this.intBytes[0] << 1;
            if (!isFinal) {
                i2 = 0;
            }
            bArr[0] = (byte) (i2 | i3);
            return write(this.intBytes, length);
        }
        int i4 = (i + 16) << 1;
        if (!isFinal) {
            i2 = 0;
        }
        return write(i2 | i4);
    }

    @Deprecated
    protected int writeValueAndType(boolean hasValue, int value, int node) {
        int offset = write(node);
        if (hasValue) {
            return writeValueAndFinal(value, false);
        }
        return offset;
    }

    @Deprecated
    protected int writeDeltaTo(int jumpTarget) {
        int i = this.bytesLength - jumpTarget;
        if (!-assertionsDisabled && i < 0) {
            throw new AssertionError();
        } else if (i <= 191) {
            return write(i);
        } else {
            int length;
            if (i <= 12287) {
                this.intBytes[0] = (byte) ((i >> 8) + 192);
                length = 1;
            } else {
                if (i <= 917503) {
                    this.intBytes[0] = (byte) ((i >> 16) + 240);
                    length = 2;
                } else {
                    if (i <= 16777215) {
                        this.intBytes[0] = (byte) -2;
                        length = 3;
                    } else {
                        this.intBytes[0] = (byte) -1;
                        this.intBytes[1] = (byte) (i >> 24);
                        length = 4;
                    }
                    this.intBytes[1] = (byte) (i >> 16);
                }
                this.intBytes[1] = (byte) (i >> 8);
            }
            int length2 = length + 1;
            this.intBytes[length] = (byte) i;
            return write(this.intBytes, length2);
        }
    }
}
