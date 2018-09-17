package java.util.concurrent;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

final /* synthetic */ class -$Lambda$xR9BLpu6SifNikvFgr4lEiECBsk implements BiConsumer {
    private final /* synthetic */ Object -$f0;
    private final /* synthetic */ Object -$f1;

    private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
        ((ConcurrentMap) this.-$f0).lambda$-java_util_concurrent_ConcurrentMap_11766((BiFunction) this.-$f1, arg0, arg1);
    }

    public /* synthetic */ -$Lambda$xR9BLpu6SifNikvFgr4lEiECBsk(Object obj, Object obj2) {
        this.-$f0 = obj;
        this.-$f1 = obj2;
    }

    public final void accept(Object obj, Object obj2) {
        $m$0(obj, obj2);
    }
}
