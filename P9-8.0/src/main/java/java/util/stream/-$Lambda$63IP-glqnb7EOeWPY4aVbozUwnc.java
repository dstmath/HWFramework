package java.util.stream;

import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.function.Supplier;

final /* synthetic */ class -$Lambda$63IP-glqnb7EOeWPY4aVbozUwnc implements IntFunction {

    /* renamed from: java.util.stream.-$Lambda$63IP-glqnb7EOeWPY4aVbozUwnc$1 */
    final /* synthetic */ class AnonymousClass1 implements Supplier {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0() {
            return ((AbstractPipeline) this.-$f0).lambda$-java_util_stream_AbstractPipeline_14339();
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final Object get() {
            return $m$0();
        }
    }

    /* renamed from: java.util.stream.-$Lambda$63IP-glqnb7EOeWPY4aVbozUwnc$2 */
    final /* synthetic */ class AnonymousClass2 implements Supplier {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0() {
            return AbstractPipeline.lambda$-java_util_stream_AbstractPipeline_20439((Spliterator) this.-$f0);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final Object get() {
            return $m$0();
        }
    }

    public final Object apply(int i) {
        return $m$0(i);
    }
}
