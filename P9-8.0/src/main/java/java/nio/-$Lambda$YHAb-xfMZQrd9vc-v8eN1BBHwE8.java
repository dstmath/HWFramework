package java.nio;

import java.util.function.Supplier;

final /* synthetic */ class -$Lambda$YHAb-xfMZQrd9vc-v8eN1BBHwE8 implements Supplier {
    private final /* synthetic */ Object -$f0;

    private final /* synthetic */ Object $m$0() {
        return new CharBufferSpliterator((CharBuffer) this.-$f0);
    }

    public /* synthetic */ -$Lambda$YHAb-xfMZQrd9vc-v8eN1BBHwE8(Object obj) {
        this.-$f0 = obj;
    }

    public final Object get() {
        return $m$0();
    }
}
