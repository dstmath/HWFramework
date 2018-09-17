package java.util.stream;

import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

final /* synthetic */ class -$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0 implements BiConsumer {

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$55 */
    final /* synthetic */ class AnonymousClass55 implements BiConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            Collectors.lambda$-java_util_stream_Collectors_27066((ToDoubleFunction) this.-$f0, (double[]) arg0, arg1);
        }

        public /* synthetic */ AnonymousClass55(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$56 */
    final /* synthetic */ class AnonymousClass56 implements BiConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            Collectors.lambda$-java_util_stream_Collectors_24417((ToIntFunction) this.-$f0, (long[]) arg0, arg1);
        }

        public /* synthetic */ AnonymousClass56(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$57 */
    final /* synthetic */ class AnonymousClass57 implements BiConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            Collectors.lambda$-java_util_stream_Collectors_25213((ToLongFunction) this.-$f0, (long[]) arg0, arg1);
        }

        public /* synthetic */ AnonymousClass57(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$58 */
    final /* synthetic */ class AnonymousClass58 implements BiConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            ((Object[]) arg0)[0] = ((BinaryOperator) this.-$f0).apply(((Object[]) arg0)[0], arg1);
        }

        public /* synthetic */ AnonymousClass58(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$59 */
    final /* synthetic */ class AnonymousClass59 implements BiConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            ((DoubleSummaryStatistics) arg0).-java_util_stream_DoublePipeline-mthref-5(((ToDoubleFunction) this.-$f0).applyAsDouble(arg1));
        }

        public /* synthetic */ AnonymousClass59(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$60 */
    final /* synthetic */ class AnonymousClass60 implements BiConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            ((IntSummaryStatistics) arg0).-java_util_stream_IntPipeline-mthref-6(((ToIntFunction) this.-$f0).applyAsInt(arg1));
        }

        public /* synthetic */ AnonymousClass60(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$61 */
    final /* synthetic */ class AnonymousClass61 implements BiConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            ((LongSummaryStatistics) arg0).-java_util_stream_LongPipeline-mthref-6(((ToLongFunction) this.-$f0).applyAsLong(arg1));
        }

        public /* synthetic */ AnonymousClass61(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$62 */
    final /* synthetic */ class AnonymousClass62 implements BiConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            Collectors.lambda$-java_util_stream_Collectors_22066((ToDoubleFunction) this.-$f0, (double[]) arg0, arg1);
        }

        public /* synthetic */ AnonymousClass62(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$63 */
    final /* synthetic */ class AnonymousClass63 implements BiConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            ((int[]) arg0)[0] = ((int[]) arg0)[0] + ((ToIntFunction) this.-$f0).applyAsInt(arg1);
        }

        public /* synthetic */ AnonymousClass63(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$64 */
    final /* synthetic */ class AnonymousClass64 implements BiConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            ((long[]) arg0)[0] = ((long[]) arg0)[0] + ((ToLongFunction) this.-$f0).applyAsLong(arg1);
        }

        public /* synthetic */ AnonymousClass64(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$65 */
    final /* synthetic */ class AnonymousClass65 implements BiFunction {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(Object arg0, Object arg1) {
            return ((Function) this.-$f0).lambda$-java_util_stream_Collectors_49854(arg1);
        }

        public /* synthetic */ AnonymousClass65(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$66 */
    final /* synthetic */ class AnonymousClass66 implements BiFunction {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(Object arg0, Object arg1) {
            return ((Function) this.-$f0).lambda$-java_util_stream_Collectors_49854(arg1);
        }

        public /* synthetic */ AnonymousClass66(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$67 */
    final /* synthetic */ class AnonymousClass67 implements BinaryOperator {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(Object arg0, Object arg1) {
            return Collectors.lambda$-java_util_stream_Collectors_13304((BinaryOperator) this.-$f0, (Map) arg0, (Map) arg1);
        }

        public /* synthetic */ AnonymousClass67(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$68 */
    final /* synthetic */ class AnonymousClass68 implements BinaryOperator {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(Object arg0, Object arg1) {
            return new Partition(((BinaryOperator) this.-$f0).apply(((Partition) arg0).forTrue, ((Partition) arg1).forTrue), ((BinaryOperator) this.-$f0).apply(((Partition) arg0).forFalse, ((Partition) arg1).forFalse));
        }

        public /* synthetic */ AnonymousClass68(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$69 */
    final /* synthetic */ class AnonymousClass69 implements BinaryOperator {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(Object arg0, Object arg1) {
            return ((Object[]) arg0)[0] = ((BinaryOperator) this.-$f0).apply(((Object[]) arg0)[0], ((Object[]) arg1)[0]);
        }

        public /* synthetic */ AnonymousClass69(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$70 */
    final /* synthetic */ class AnonymousClass70 implements BinaryOperator {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(Object arg0, Object arg1) {
            return ((Object[]) arg0)[0] = ((BinaryOperator) this.-$f0).apply(((Object[]) arg0)[0], ((Object[]) arg1)[0]);
        }

        public /* synthetic */ AnonymousClass70(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$71 */
    final /* synthetic */ class AnonymousClass71 implements Function {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(Object arg0) {
            return Collectors.lambda$-java_util_stream_Collectors_41242((Function) this.-$f0, (Map) arg0);
        }

        public /* synthetic */ AnonymousClass71(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$72 */
    final /* synthetic */ class AnonymousClass72 implements Function {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(Object arg0) {
            return Collectors.lambda$-java_util_stream_Collectors_49796((Function) this.-$f0, (ConcurrentMap) arg0);
        }

        public /* synthetic */ AnonymousClass72(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$73 */
    final /* synthetic */ class AnonymousClass73 implements Function {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(Object arg0) {
            return ((Supplier) this.-$f0).lambda$-java_util_stream_Collectors_49198();
        }

        public /* synthetic */ AnonymousClass73(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$74 */
    final /* synthetic */ class AnonymousClass74 implements Function {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(Object arg0) {
            return ((Supplier) this.-$f0).lambda$-java_util_stream_Collectors_49198();
        }

        public /* synthetic */ AnonymousClass74(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$75 */
    final /* synthetic */ class AnonymousClass75 implements Function {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(Object arg0) {
            return ((Supplier) this.-$f0).lambda$-java_util_stream_Collectors_49198();
        }

        public /* synthetic */ AnonymousClass75(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$76 */
    final /* synthetic */ class AnonymousClass76 implements Function {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(Object arg0) {
            return new Partition(((Collector) this.-$f0).finisher().lambda$-java_util_stream_Collectors_49854(((Partition) arg0).forTrue), ((Collector) this.-$f0).finisher().lambda$-java_util_stream_Collectors_49854(((Partition) arg0).forFalse));
        }

        public /* synthetic */ AnonymousClass76(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$77 */
    final /* synthetic */ class AnonymousClass77 implements Supplier {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0() {
            return new Object[]{this.-$f0};
        }

        public /* synthetic */ AnonymousClass77(Object obj) {
            this.-$f0 = obj;
        }

        public final Object get() {
            return $m$0();
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$78 */
    final /* synthetic */ class AnonymousClass78 implements Supplier {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0() {
            return new Partition(((Collector) this.-$f0).supplier().lambda$-java_util_stream_Collectors_49198(), ((Collector) this.-$f0).supplier().lambda$-java_util_stream_Collectors_49198());
        }

        public /* synthetic */ AnonymousClass78(Object obj) {
            this.-$f0 = obj;
        }

        public final Object get() {
            return $m$0();
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$79 */
    final /* synthetic */ class AnonymousClass79 implements Supplier {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0() {
            return new Consumer<T>((BinaryOperator) this.-$f0) {
                boolean present = false;
                T value = null;

                /* renamed from: accept */
                public void -java_util_stream_Collectors-mthref-13(T t) {
                    if (this.present) {
                        this.value = r1.apply(this.value, t);
                        return;
                    }
                    this.value = t;
                    this.present = true;
                }
            };
        }

        public /* synthetic */ AnonymousClass79(Object obj) {
            this.-$f0 = obj;
        }

        public final Object get() {
            return $m$0();
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$80 */
    final /* synthetic */ class AnonymousClass80 implements BiConsumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            ((BiConsumer) this.-$f0).lambda$-java_util_stream_ReferencePipeline_19478(arg0, ((Function) this.-$f1).lambda$-java_util_stream_Collectors_49854(arg1));
        }

        public /* synthetic */ AnonymousClass80(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$81 */
    final /* synthetic */ class AnonymousClass81 implements BiConsumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            Collectors.lambda$-java_util_stream_Collectors_52253((BiConsumer) this.-$f0, (Predicate) this.-$f1, (Partition) arg0, arg1);
        }

        public /* synthetic */ AnonymousClass81(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$82 */
    final /* synthetic */ class AnonymousClass82 implements BiConsumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            ((Object[]) arg0)[0] = ((BinaryOperator) this.-$f0).apply(((Object[]) arg0)[0], ((Function) this.-$f1).lambda$-java_util_stream_Collectors_49854(arg1));
        }

        public /* synthetic */ AnonymousClass82(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$83 */
    final /* synthetic */ class AnonymousClass83 implements BiConsumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            ((BiConsumer) this.-$f2).lambda$-java_util_stream_ReferencePipeline_19478(((Map) arg0).computeIfAbsent(Objects.requireNonNull(((Function) this.-$f0).lambda$-java_util_stream_Collectors_49854(arg1), "element cannot be mapped to a null key"), new AnonymousClass73((Supplier) this.-$f1)), arg1);
        }

        public /* synthetic */ AnonymousClass83(Object obj, Object obj2, Object obj3) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$84 */
    final /* synthetic */ class AnonymousClass84 implements BiConsumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            ((BiConsumer) this.-$f2).lambda$-java_util_stream_ReferencePipeline_19478(((ConcurrentMap) arg0).computeIfAbsent(Objects.requireNonNull(((Function) this.-$f0).lambda$-java_util_stream_Collectors_49854(arg1), "element cannot be mapped to a null key"), new AnonymousClass74((Supplier) this.-$f1)), arg1);
        }

        public /* synthetic */ AnonymousClass84(Object obj, Object obj2, Object obj3) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$85 */
    final /* synthetic */ class AnonymousClass85 implements BiConsumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            Collectors.lambda$-java_util_stream_Collectors_49016((Function) this.-$f0, (Supplier) this.-$f1, (BiConsumer) this.-$f2, (ConcurrentMap) arg0, arg1);
        }

        public /* synthetic */ AnonymousClass85(Object obj, Object obj2, Object obj3) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$86 */
    final /* synthetic */ class AnonymousClass86 implements BiConsumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            ((ConcurrentMap) arg0).merge(((Function) this.-$f0).lambda$-java_util_stream_Collectors_49854(arg1), ((Function) this.-$f1).lambda$-java_util_stream_Collectors_49854(arg1), (BinaryOperator) this.-$f2);
        }

        public /* synthetic */ AnonymousClass86(Object obj, Object obj2, Object obj3) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$87 */
    final /* synthetic */ class AnonymousClass87 implements BiConsumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(Object arg0, Object arg1) {
            ((Map) arg0).merge(((Function) this.-$f0).lambda$-java_util_stream_Collectors_49854(arg1), ((Function) this.-$f1).lambda$-java_util_stream_Collectors_49854(arg1), (BinaryOperator) this.-$f2);
        }

        public /* synthetic */ AnonymousClass87(Object obj, Object obj2, Object obj3) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
        }

        public final void accept(Object obj, Object obj2) {
            $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0$88 */
    final /* synthetic */ class AnonymousClass88 implements Supplier {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ Object $m$0() {
            return new StringJoiner((CharSequence) this.-$f0, (CharSequence) this.-$f1, (CharSequence) this.-$f2);
        }

        public /* synthetic */ AnonymousClass88(Object obj, Object obj2, Object obj3) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
        }

        public final Object get() {
            return $m$0();
        }
    }

    public final void accept(Object obj, Object obj2) {
        $m$0(obj, obj2);
    }
}
