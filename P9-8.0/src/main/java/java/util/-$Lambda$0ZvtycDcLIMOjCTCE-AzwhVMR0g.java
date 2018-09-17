package java.util;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

final /* synthetic */ class -$Lambda$0ZvtycDcLIMOjCTCE-AzwhVMR0g implements DoubleConsumer {
    private final /* synthetic */ Object -$f0;

    /* renamed from: java.util.-$Lambda$0ZvtycDcLIMOjCTCE-AzwhVMR0g$1 */
    final /* synthetic */ class AnonymousClass1 implements IntConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(int arg0) {
            ((Consumer) this.-$f0).accept(Integer.valueOf(arg0));
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(int i) {
            $m$0(i);
        }
    }

    /* renamed from: java.util.-$Lambda$0ZvtycDcLIMOjCTCE-AzwhVMR0g$2 */
    final /* synthetic */ class AnonymousClass2 implements LongConsumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(long arg0) {
            ((Consumer) this.-$f0).accept(Long.valueOf(arg0));
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(long j) {
            $m$0(j);
        }
    }

    private final /* synthetic */ void $m$0(double arg0) {
        ((Consumer) this.-$f0).accept(Double.valueOf(arg0));
    }

    public /* synthetic */ -$Lambda$0ZvtycDcLIMOjCTCE-AzwhVMR0g(Object obj) {
        this.-$f0 = obj;
    }

    public final void accept(double d) {
        $m$0(d);
    }
}
