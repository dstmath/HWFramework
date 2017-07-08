package java.nio;

import java.util.Spliterator.OfInt;
import java.util.function.IntConsumer;

class CharBufferSpliterator implements OfInt {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private final CharBuffer buffer;
    private int index;
    private final int limit;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.nio.CharBufferSpliterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.nio.CharBufferSpliterator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.nio.CharBufferSpliterator.<clinit>():void");
    }

    CharBufferSpliterator(CharBuffer buffer) {
        this(buffer, buffer.position(), buffer.limit());
    }

    CharBufferSpliterator(CharBuffer buffer, int origin, int limit) {
        if (!-assertionsDisabled) {
            if ((origin <= limit ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        this.buffer = buffer;
        if (origin > limit) {
            origin = limit;
        }
        this.index = origin;
        this.limit = limit;
    }

    public OfInt trySplit() {
        int lo = this.index;
        int mid = (this.limit + lo) >>> 1;
        if (lo >= mid) {
            return null;
        }
        CharBuffer charBuffer = this.buffer;
        this.index = mid;
        return new CharBufferSpliterator(charBuffer, lo, mid);
    }

    public void forEachRemaining(IntConsumer action) {
        if (action == null) {
            throw new NullPointerException();
        }
        CharBuffer cb = this.buffer;
        int i = this.index;
        int hi = this.limit;
        this.index = hi;
        int i2 = i;
        while (i2 < hi) {
            i = i2 + 1;
            action.accept(cb.getUnchecked(i2));
            i2 = i;
        }
    }

    public boolean tryAdvance(IntConsumer action) {
        if (action == null) {
            throw new NullPointerException();
        } else if (this.index < 0 || this.index >= this.limit) {
            return false;
        } else {
            CharBuffer charBuffer = this.buffer;
            int i = this.index;
            this.index = i + 1;
            action.accept(charBuffer.getUnchecked(i));
            return true;
        }
    }

    public long estimateSize() {
        return (long) (this.limit - this.index);
    }

    public int characteristics() {
        return 16464;
    }
}
