package java.util.stream;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.DoubleConsumer;

final /* synthetic */ class -$Lambda$15cJOyg3Zha5J4OQxNOU52CozsU implements BiConsumer {

    /* renamed from: java.util.stream.-$Lambda$15cJOyg3Zha5J4OQxNOU52CozsU$15 */
    final /* synthetic */ class AnonymousClass15 implements BinaryOperator {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(Object arg0, Object arg1) {
            return ((BiConsumer) this.-$f0).accept(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass15(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$15cJOyg3Zha5J4OQxNOU52CozsU$16 */
    final /* synthetic */ class AnonymousClass16 implements DoubleConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(double arg0) {
            ((java.util.stream.DoublePipeline.5.AnonymousClass1) this.-$f0).lambda$-java_util_stream_DoublePipeline$5$1_10563(arg0);
        }

        public /* synthetic */ AnonymousClass16(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(double d) {
            $m$0(d);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$15cJOyg3Zha5J4OQxNOU52CozsU$17 */
    final /* synthetic */ class AnonymousClass17 implements DoubleConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(double arg0) {
            ((Sink) this.-$f0).-java_util_stream_DoublePipeline-mthref-0(arg0);
        }

        public /* synthetic */ AnonymousClass17(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(double d) {
            $m$0(d);
        }
    }

    public final void accept(Object obj, Object obj2) {
        $m$0(obj, obj2);
    }
}
