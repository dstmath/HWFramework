package java.util.stream;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;

final /* synthetic */ class -$Lambda$DJvCeprCIGMk0JvfSkTmQUmEYKA implements IntFunction {

    /* renamed from: java.util.stream.-$Lambda$DJvCeprCIGMk0JvfSkTmQUmEYKA$2 */
    final /* synthetic */ class AnonymousClass2 implements DoubleConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(double arg0) {
            ((Sink) this.-$f0).-java_util_stream_ReferencePipeline$9$1-mthref-0(arg0);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(double d) {
            $m$0(d);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$DJvCeprCIGMk0JvfSkTmQUmEYKA$3 */
    final /* synthetic */ class AnonymousClass3 implements IntConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(int arg0) {
            ((Sink) this.-$f0).-java_util_stream_ReferencePipeline$8$1-mthref-0(arg0);
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(int i) {
            $m$0(i);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$DJvCeprCIGMk0JvfSkTmQUmEYKA$4 */
    final /* synthetic */ class AnonymousClass4 implements LongConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(long arg0) {
            ((Sink) this.-$f0).-java_util_stream_ReferencePipeline$10$1-mthref-0(arg0);
        }

        public /* synthetic */ AnonymousClass4(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(long j) {
            $m$0(j);
        }
    }

    /* renamed from: java.util.stream.-$Lambda$DJvCeprCIGMk0JvfSkTmQUmEYKA$5 */
    final /* synthetic */ class AnonymousClass5 implements Consumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((BiConsumer) this.-$f0).lambda$-java_util_stream_ReferencePipeline_19478(this.-$f1, arg0);
        }

        public /* synthetic */ AnonymousClass5(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    public final Object apply(int i) {
        return $m$0(i);
    }
}
