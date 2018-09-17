package java.util.function;

import java.util.Comparator;

final /* synthetic */ class -$Lambda$mIdd76aENPtnGc8CGzUjiSYitJQ implements BinaryOperator {
    private final /* synthetic */ Object -$f0;

    /* renamed from: java.util.function.-$Lambda$mIdd76aENPtnGc8CGzUjiSYitJQ$1 */
    final /* synthetic */ class AnonymousClass1 implements BinaryOperator {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(Object arg0, Object arg1) {
            return BinaryOperator.lambda$-java_util_function_BinaryOperator_2544((Comparator) this.-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    private final /* synthetic */ Object $m$0(Object arg0, Object arg1) {
        return BinaryOperator.lambda$-java_util_function_BinaryOperator_3246((Comparator) this.-$f0, arg0, arg1);
    }

    public /* synthetic */ -$Lambda$mIdd76aENPtnGc8CGzUjiSYitJQ(Object obj) {
        this.-$f0 = obj;
    }

    public final Object apply(Object obj, Object obj2) {
        return $m$0(obj, obj2);
    }
}
