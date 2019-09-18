package java.nio;

import java.util.Spliterator;
import java.util.function.IntConsumer;

class CharBufferSpliterator implements Spliterator.OfInt {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final CharBuffer buffer;
    private int index;
    private final int limit;

    CharBufferSpliterator(CharBuffer buffer2) {
        this(buffer2, buffer2.position(), buffer2.limit());
    }

    CharBufferSpliterator(CharBuffer buffer2, int origin, int limit2) {
        this.buffer = buffer2;
        this.index = origin <= limit2 ? origin : limit2;
        this.limit = limit2;
    }

    public Spliterator.OfInt trySplit() {
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
        if (action != null) {
            CharBuffer cb = this.buffer;
            int hi = this.limit;
            this.index = hi;
            for (int i = this.index; i < hi; i++) {
                action.accept(cb.getUnchecked(i));
            }
            return;
        }
        throw new NullPointerException();
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
