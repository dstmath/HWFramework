package java.util.stream;

import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

final /* synthetic */ class -$Lambda$aRB2Ve9yTNaLYumrLKKo_GKLlhc implements BinaryOperator {

    /* renamed from: java.util.stream.-$Lambda$aRB2Ve9yTNaLYumrLKKo_GKLlhc$7 */
    final /* synthetic */ class AnonymousClass7 implements LongFunction {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(long arg0) {
            return Nodes.builder(arg0, (IntFunction) this.-$f0);
        }

        public /* synthetic */ AnonymousClass7(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(long j) {
            return $m$0(j);
        }
    }

    public final Object apply(Object obj, Object obj2) {
        return $m$0(obj, obj2);
    }
}
