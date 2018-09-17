package java.util.stream;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.Sink.OfDouble;
import java.util.stream.Sink.OfInt;
import java.util.stream.Sink.OfLong;

final /* synthetic */ class -$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc implements Consumer {

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$10 */
    final /* synthetic */ class AnonymousClass10 implements OfDouble {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(double arg0) {
            ((SpinedBuffer.OfDouble) this.-$f0).-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-0(arg0);
        }

        public /* synthetic */ AnonymousClass10(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(double d) {
            $m$0(d);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$11 */
    final /* synthetic */ class AnonymousClass11 implements OfInt {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(int arg0) {
            ((IntConsumer) this.-$f0).-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-1(arg0);
        }

        public /* synthetic */ AnonymousClass11(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(int i) {
            $m$0(i);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$12 */
    final /* synthetic */ class AnonymousClass12 implements OfInt {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(int arg0) {
            ((SpinedBuffer.OfInt) this.-$f0).-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-0(arg0);
        }

        public /* synthetic */ AnonymousClass12(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(int i) {
            $m$0(i);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$13 */
    final /* synthetic */ class AnonymousClass13 implements OfLong {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(long arg0) {
            ((LongConsumer) this.-$f0).-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-1(arg0);
        }

        public /* synthetic */ AnonymousClass13(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(long j) {
            $m$0(j);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$14 */
    final /* synthetic */ class AnonymousClass14 implements OfLong {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(long arg0) {
            ((SpinedBuffer.OfLong) this.-$f0).-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-0(arg0);
        }

        public /* synthetic */ AnonymousClass14(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(long j) {
            $m$0(j);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$15 */
    final /* synthetic */ class AnonymousClass15 implements Sink {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((Consumer) this.-$f0).-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(arg0);
        }

        public /* synthetic */ AnonymousClass15(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$16 */
    final /* synthetic */ class AnonymousClass16 implements Sink {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((SpinedBuffer) this.-$f0).-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-0(arg0);
        }

        public /* synthetic */ AnonymousClass16(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$17 */
    final /* synthetic */ class AnonymousClass17 implements Consumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((DistinctSpliterator) this.-$f0).lambda$-java_util_stream_StreamSpliterators$DistinctSpliterator_46149((Consumer) this.-$f1, arg0);
        }

        public /* synthetic */ AnonymousClass17(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$5 */
    final /* synthetic */ class AnonymousClass5 implements BooleanSupplier {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0() {
            return ((DoubleWrappingSpliterator) this.-$f0).lambda$-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator_16351();
        }

        public /* synthetic */ AnonymousClass5(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean getAsBoolean() {
            return $m$0();
        }
    }

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$6 */
    final /* synthetic */ class AnonymousClass6 implements BooleanSupplier {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0() {
            return ((IntWrappingSpliterator) this.-$f0).lambda$-java_util_stream_StreamSpliterators$IntWrappingSpliterator_12402();
        }

        public /* synthetic */ AnonymousClass6(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean getAsBoolean() {
            return $m$0();
        }
    }

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$7 */
    final /* synthetic */ class AnonymousClass7 implements BooleanSupplier {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0() {
            return ((LongWrappingSpliterator) this.-$f0).lambda$-java_util_stream_StreamSpliterators$LongWrappingSpliterator_14357();
        }

        public /* synthetic */ AnonymousClass7(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean getAsBoolean() {
            return $m$0();
        }
    }

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$8 */
    final /* synthetic */ class AnonymousClass8 implements BooleanSupplier {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0() {
            return ((WrappingSpliterator) this.-$f0).lambda$-java_util_stream_StreamSpliterators$WrappingSpliterator_10555();
        }

        public /* synthetic */ AnonymousClass8(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean getAsBoolean() {
            return $m$0();
        }
    }

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$9 */
    final /* synthetic */ class AnonymousClass9 implements OfDouble {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(double arg0) {
            ((DoubleConsumer) this.-$f0).-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-1(arg0);
        }

        public /* synthetic */ AnonymousClass9(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(double d) {
            $m$0(d);
        }
    }

    public final void accept(Object obj) {
        $m$0(obj);
    }
}
