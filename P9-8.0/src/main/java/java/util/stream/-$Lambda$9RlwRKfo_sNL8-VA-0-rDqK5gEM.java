package java.util.stream;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$9RlwRKfo_sNL8-VA-0-rDqK5gEM implements BiConsumer {

    /* renamed from: java.util.stream.-$Lambda$9RlwRKfo_sNL8-VA-0-rDqK5gEM$3 */
    final /* synthetic */ class AnonymousClass3 implements Consumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            java.util.stream.DistinctOps.AnonymousClass1.lambda$-java_util_stream_DistinctOps$1_3835((AtomicBoolean) this.-$f0, (ConcurrentHashMap) this.-$f1, arg0);
        }

        public /* synthetic */ AnonymousClass3(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    public final void accept(Object obj, Object obj2) {
        $m$0(obj, obj2);
    }
}
