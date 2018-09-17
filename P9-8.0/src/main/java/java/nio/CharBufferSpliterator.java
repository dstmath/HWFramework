package java.nio;

import java.util.Spliterator.OfInt;
import java.util.function.IntConsumer;

class CharBufferSpliterator implements OfInt {
    static final /* synthetic */ boolean -assertionsDisabled = (CharBufferSpliterator.class.desiredAssertionStatus() ^ 1);
    private final CharBuffer buffer;
    private int index;
    private final int limit;

    CharBufferSpliterator(CharBuffer buffer) {
        this(buffer, buffer.position(), buffer.limit());
    }

    CharBufferSpliterator(CharBuffer buffer, int origin, int limit) {
        if (-assertionsDisabled || origin <= limit) {
            this.buffer = buffer;
            if (origin > limit) {
                origin = limit;
            }
            this.index = origin;
            this.limit = limit;
            return;
        }
        throw new AssertionError();
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
