package android.icu.impl.coll;

import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;

public final class CollationKeys {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int CASE_LOWER_FIRST_COMMON_HIGH = 13;
    private static final int CASE_LOWER_FIRST_COMMON_LOW = 1;
    private static final int CASE_LOWER_FIRST_COMMON_MAX_COUNT = 7;
    private static final int CASE_LOWER_FIRST_COMMON_MIDDLE = 7;
    private static final int CASE_UPPER_FIRST_COMMON_HIGH = 15;
    private static final int CASE_UPPER_FIRST_COMMON_LOW = 3;
    private static final int CASE_UPPER_FIRST_COMMON_MAX_COUNT = 13;
    private static final int QUAT_COMMON_HIGH = 252;
    private static final int QUAT_COMMON_LOW = 28;
    private static final int QUAT_COMMON_MAX_COUNT = 113;
    private static final int QUAT_COMMON_MIDDLE = 140;
    private static final int QUAT_SHIFTED_LIMIT_BYTE = 27;
    static final int SEC_COMMON_HIGH = 69;
    private static final int SEC_COMMON_LOW = 5;
    private static final int SEC_COMMON_MAX_COUNT = 33;
    private static final int SEC_COMMON_MIDDLE = 37;
    public static final LevelCallback SIMPLE_LEVEL_FALLBACK = null;
    private static final int TER_LOWER_FIRST_COMMON_HIGH = 69;
    private static final int TER_LOWER_FIRST_COMMON_LOW = 5;
    private static final int TER_LOWER_FIRST_COMMON_MAX_COUNT = 33;
    private static final int TER_LOWER_FIRST_COMMON_MIDDLE = 37;
    private static final int TER_ONLY_COMMON_HIGH = 197;
    private static final int TER_ONLY_COMMON_LOW = 5;
    private static final int TER_ONLY_COMMON_MAX_COUNT = 97;
    private static final int TER_ONLY_COMMON_MIDDLE = 101;
    private static final int TER_UPPER_FIRST_COMMON_HIGH = 197;
    private static final int TER_UPPER_FIRST_COMMON_LOW = 133;
    private static final int TER_UPPER_FIRST_COMMON_MAX_COUNT = 33;
    private static final int TER_UPPER_FIRST_COMMON_MIDDLE = 165;
    private static final int[] levelMasks = null;

    public static class LevelCallback {
        boolean needToWrite(int level) {
            return true;
        }
    }

    public static abstract class SortKeyByteSink {
        private int appended_;
        protected byte[] buffer_;

        protected abstract void AppendBeyondCapacity(byte[] bArr, int i, int i2, int i3);

        protected abstract boolean Resize(int i, int i2);

        public SortKeyByteSink(byte[] dest) {
            this.appended_ = 0;
            this.buffer_ = dest;
        }

        public void setBufferAndAppended(byte[] dest, int app) {
            this.buffer_ = dest;
            this.appended_ = app;
        }

        public void Append(byte[] bytes, int n) {
            if (n > 0 && bytes != null) {
                int length = this.appended_;
                this.appended_ += n;
                if (n <= this.buffer_.length - length) {
                    System.arraycopy(bytes, 0, this.buffer_, length, n);
                } else {
                    AppendBeyondCapacity(bytes, 0, n, length);
                }
            }
        }

        public void Append(int b) {
            if (this.appended_ < this.buffer_.length || Resize(CollationKeys.CASE_LOWER_FIRST_COMMON_LOW, this.appended_)) {
                this.buffer_[this.appended_] = (byte) b;
            }
            this.appended_ += CollationKeys.CASE_LOWER_FIRST_COMMON_LOW;
        }

        public int NumberOfBytesAppended() {
            return this.appended_;
        }

        public int GetRemainingCapacity() {
            return this.buffer_.length - this.appended_;
        }

        public boolean Overflowed() {
            return this.appended_ > this.buffer_.length ? true : CollationKeys.-assertionsDisabled;
        }
    }

    private static final class SortKeyLevel {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private static final int INITIAL_CAPACITY = 40;
        byte[] buffer;
        int len;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationKeys.SortKeyLevel.<clinit>():void");
        }

        SortKeyLevel() {
            this.buffer = new byte[INITIAL_CAPACITY];
            this.len = 0;
        }

        boolean isEmpty() {
            return this.len == 0 ? true : -assertionsDisabled;
        }

        int length() {
            return this.len;
        }

        byte getAt(int index) {
            return this.buffer[index];
        }

        byte[] data() {
            return this.buffer;
        }

        void appendByte(int b) {
            if (this.len < this.buffer.length || ensureCapacity(CollationKeys.CASE_LOWER_FIRST_COMMON_LOW)) {
                byte[] bArr = this.buffer;
                int i = this.len;
                this.len = i + CollationKeys.CASE_LOWER_FIRST_COMMON_LOW;
                bArr[i] = (byte) b;
            }
        }

        void appendWeight16(int w) {
            Object obj = null;
            if (!-assertionsDisabled) {
                if ((DexFormat.MAX_TYPE_IDX & w) != 0) {
                    obj = CollationKeys.CASE_LOWER_FIRST_COMMON_LOW;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            byte b0 = (byte) (w >>> 8);
            byte b1 = (byte) w;
            int appendLength = b1 == null ? CollationKeys.CASE_LOWER_FIRST_COMMON_LOW : 2;
            if (this.len + appendLength <= this.buffer.length || ensureCapacity(appendLength)) {
                byte[] bArr = this.buffer;
                int i = this.len;
                this.len = i + CollationKeys.CASE_LOWER_FIRST_COMMON_LOW;
                bArr[i] = b0;
                if (b1 != null) {
                    bArr = this.buffer;
                    i = this.len;
                    this.len = i + CollationKeys.CASE_LOWER_FIRST_COMMON_LOW;
                    bArr[i] = b1;
                }
            }
        }

        void appendWeight32(long w) {
            if (!-assertionsDisabled) {
                if ((w != 0 ? CollationKeys.CASE_LOWER_FIRST_COMMON_LOW : 0) == 0) {
                    throw new AssertionError();
                }
            }
            byte[] bytes = new byte[]{(byte) ((int) (w >>> 24)), (byte) ((int) (w >>> 16)), (byte) ((int) (w >>> 8)), (byte) ((int) w)};
            int appendLength = bytes[CollationKeys.CASE_LOWER_FIRST_COMMON_LOW] == null ? CollationKeys.CASE_LOWER_FIRST_COMMON_LOW : bytes[2] == null ? 2 : bytes[CollationKeys.CASE_UPPER_FIRST_COMMON_LOW] == null ? CollationKeys.CASE_UPPER_FIRST_COMMON_LOW : 4;
            if (this.len + appendLength <= this.buffer.length || ensureCapacity(appendLength)) {
                byte[] bArr = this.buffer;
                int i = this.len;
                this.len = i + CollationKeys.CASE_LOWER_FIRST_COMMON_LOW;
                bArr[i] = bytes[0];
                if (bytes[CollationKeys.CASE_LOWER_FIRST_COMMON_LOW] != null) {
                    bArr = this.buffer;
                    int i2 = this.len;
                    this.len = i2 + CollationKeys.CASE_LOWER_FIRST_COMMON_LOW;
                    bArr[i2] = bytes[CollationKeys.CASE_LOWER_FIRST_COMMON_LOW];
                    if (bytes[2] != null) {
                        bArr = this.buffer;
                        int i3 = this.len;
                        this.len = i3 + CollationKeys.CASE_LOWER_FIRST_COMMON_LOW;
                        bArr[i3] = bytes[2];
                        if (bytes[CollationKeys.CASE_UPPER_FIRST_COMMON_LOW] != null) {
                            bArr = this.buffer;
                            i3 = this.len;
                            this.len = i3 + CollationKeys.CASE_LOWER_FIRST_COMMON_LOW;
                            bArr[i3] = bytes[CollationKeys.CASE_UPPER_FIRST_COMMON_LOW];
                        }
                    }
                }
            }
        }

        void appendReverseWeight16(int w) {
            Object obj = null;
            if (!-assertionsDisabled) {
                if ((DexFormat.MAX_TYPE_IDX & w) != 0) {
                    obj = CollationKeys.CASE_LOWER_FIRST_COMMON_LOW;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            byte b0 = (byte) (w >>> 8);
            byte b1 = (byte) w;
            int appendLength = b1 == null ? CollationKeys.CASE_LOWER_FIRST_COMMON_LOW : 2;
            if (this.len + appendLength > this.buffer.length && !ensureCapacity(appendLength)) {
                return;
            }
            if (b1 == null) {
                byte[] bArr = this.buffer;
                int i = this.len;
                this.len = i + CollationKeys.CASE_LOWER_FIRST_COMMON_LOW;
                bArr[i] = b0;
                return;
            }
            this.buffer[this.len] = b1;
            this.buffer[this.len + CollationKeys.CASE_LOWER_FIRST_COMMON_LOW] = b0;
            this.len += 2;
        }

        void appendTo(SortKeyByteSink sink) {
            Object obj = CollationKeys.CASE_LOWER_FIRST_COMMON_LOW;
            if (!-assertionsDisabled) {
                if (this.len <= 0 || this.buffer[this.len - 1] != (byte) 1) {
                    obj = null;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            sink.Append(this.buffer, this.len - 1);
        }

        private boolean ensureCapacity(int appendCapacity) {
            int newCapacity = this.buffer.length * 2;
            int altCapacity = this.len + (appendCapacity * 2);
            if (newCapacity < altCapacity) {
                newCapacity = altCapacity;
            }
            if (newCapacity < Opcodes.OP_MUL_FLOAT_2ADDR) {
                newCapacity = Opcodes.OP_MUL_FLOAT_2ADDR;
            }
            byte[] newbuf = new byte[newCapacity];
            System.arraycopy(this.buffer, 0, newbuf, 0, this.len);
            this.buffer = newbuf;
            return true;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationKeys.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationKeys.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationKeys.<clinit>():void");
    }

    public static void writeSortKeyUpToQuaternary(android.icu.impl.coll.CollationIterator r1, boolean[] r2, android.icu.impl.coll.CollationSettings r3, android.icu.impl.coll.CollationKeys.SortKeyByteSink r4, int r5, android.icu.impl.coll.CollationKeys.LevelCallback r6, boolean r7) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationKeys.writeSortKeyUpToQuaternary(android.icu.impl.coll.CollationIterator, boolean[], android.icu.impl.coll.CollationSettings, android.icu.impl.coll.CollationKeys$SortKeyByteSink, int, android.icu.impl.coll.CollationKeys$LevelCallback, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationKeys.writeSortKeyUpToQuaternary(android.icu.impl.coll.CollationIterator, boolean[], android.icu.impl.coll.CollationSettings, android.icu.impl.coll.CollationKeys$SortKeyByteSink, int, android.icu.impl.coll.CollationKeys$LevelCallback, boolean):void");
    }

    private static SortKeyLevel getSortKeyLevel(int levels, int level) {
        return (levels & level) != 0 ? new SortKeyLevel() : null;
    }

    private CollationKeys() {
    }
}
