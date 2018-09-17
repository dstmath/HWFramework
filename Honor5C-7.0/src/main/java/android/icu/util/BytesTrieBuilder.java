package android.icu.util;

import android.icu.text.Bidi;
import android.icu.util.StringTrieBuilder.Option;
import dalvik.bytecode.Opcodes;
import java.nio.ByteBuffer;
import org.w3c.dom.traversal.NodeFilter;

public final class BytesTrieBuilder extends StringTrieBuilder {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private byte[] bytes;
    private int bytesLength;
    private final byte[] intBytes;

    private static final class BytesAsCharSequence implements CharSequence {
        private int len;
        private byte[] s;

        public BytesAsCharSequence(byte[] sequence, int length) {
            this.s = sequence;
            this.len = length;
        }

        public char charAt(int i) {
            return (char) (this.s[i] & Opcodes.OP_CONST_CLASS_JUMBO);
        }

        public int length() {
            return this.len;
        }

        public CharSequence subSequence(int start, int end) {
            return null;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.BytesTrieBuilder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.BytesTrieBuilder.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.BytesTrieBuilder.<clinit>():void");
    }

    public BytesTrieBuilder() {
        this.intBytes = new byte[5];
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
            this.bytes = new byte[NodeFilter.SHOW_DOCUMENT_FRAGMENT];
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
            int i3 = 1;
            if (i < 0 || i > 16777215) {
                this.intBytes[0] = Bidi.LEVEL_DEFAULT_RTL;
                this.intBytes[1] = (byte) (i >> 24);
                this.intBytes[2] = (byte) (i >> 16);
                this.intBytes[3] = (byte) (i >> 8);
                this.intBytes[4] = (byte) i;
                i3 = 5;
            } else {
                int length;
                if (i <= Opcodes.OP_SGET_SHORT_JUMBO) {
                    this.intBytes[0] = (byte) ((i >> 8) + 81);
                } else {
                    if (i <= 1179647) {
                        this.intBytes[0] = (byte) ((i >> 16) + Opcodes.OP_SPUT_CHAR);
                    } else {
                        this.intBytes[0] = Bidi.LEVEL_DEFAULT_LTR;
                        this.intBytes[1] = (byte) (i >> 16);
                        i3 = 2;
                    }
                    length = i3 + 1;
                    this.intBytes[i3] = (byte) (i >> 8);
                    i3 = length;
                }
                length = i3 + 1;
                this.intBytes[i3] = (byte) i;
                i3 = length;
            }
            byte[] bArr = this.intBytes;
            int i4 = this.intBytes[0] << 1;
            if (!isFinal) {
                i2 = 0;
            }
            bArr[0] = (byte) (i2 | i4);
            return write(this.intBytes, i3);
        }
        int i5 = (i + 16) << 1;
        if (!isFinal) {
            i2 = 0;
        }
        return write(i2 | i5);
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
        if (!-assertionsDisabled) {
            if ((i >= 0 ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (i <= Opcodes.OP_REM_LONG_2ADDR) {
            return write(i);
        }
        int length;
        if (i <= 12287) {
            this.intBytes[0] = (byte) ((i >> 8) + Opcodes.OP_AND_LONG_2ADDR);
            length = 1;
        } else {
            if (i <= 917503) {
                this.intBytes[0] = (byte) ((i >> 16) + Opcodes.OP_INVOKE_DIRECT_EMPTY);
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
