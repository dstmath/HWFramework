package java.util.stream;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.LongConsumer;

final /* synthetic */ class -$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA implements BiConsumer {

    /* renamed from: java.util.stream.-$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA$13 */
    final /* synthetic */ class AnonymousClass13 implements BinaryOperator {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(Object arg0, Object arg1) {
            return ((BiConsumer) this.-$f0).lambda$-java_util_stream_ReferencePipeline_19478(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass13(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA$14 */
    final /* synthetic */ class AnonymousClass14 implements LongConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(long arg0) {
            ((java.util.stream.LongPipeline.6.AnonymousClass1) this.-$f0).lambda$-java_util_stream_LongPipeline$6$1_11125(arg0);
        }

        public /* synthetic */ AnonymousClass14(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(long j) {
            $m$0(j);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA$15 */
    final /* synthetic */ class AnonymousClass15 implements LongConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(long arg0) {
            ((Sink) this.-$f0).-java_util_stream_LongPipeline-mthref-0(arg0);
        }

        public /* synthetic */ AnonymousClass15(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(long j) {
            $m$0(j);
        }
    }

    public final void accept(Object obj, Object obj2) {
        $m$0(obj, obj2);
    }
}
