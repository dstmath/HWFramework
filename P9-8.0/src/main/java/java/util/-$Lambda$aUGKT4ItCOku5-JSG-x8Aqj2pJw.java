package java.util;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;

final /* synthetic */ class -$Lambda$aUGKT4ItCOku5-JSG-x8Aqj2pJw implements IntConsumer {
    private final /* synthetic */ Object -$f0;
    private final /* synthetic */ Object -$f1;

    /* renamed from: java.util.-$Lambda$aUGKT4ItCOku5-JSG-x8Aqj2pJw$1 */
    final /* synthetic */ class AnonymousClass1 implements IntConsumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(int arg0) {
            ((int[]) this.-$f0)[arg0] = ((IntUnaryOperator) this.-$f1).applyAsInt(arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void accept(int i) {
            $m$0(i);
        }
    }

    /* renamed from: java.util.-$Lambda$aUGKT4ItCOku5-JSG-x8Aqj2pJw$2 */
    final /* synthetic */ class AnonymousClass2 implements IntConsumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(int arg0) {
            ((long[]) this.-$f0)[arg0] = ((IntToLongFunction) this.-$f1).applyAsLong(arg0);
        }

        public /* synthetic */ AnonymousClass2(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void accept(int i) {
            $m$0(i);
        }
    }

    /* renamed from: java.util.-$Lambda$aUGKT4ItCOku5-JSG-x8Aqj2pJw$3 */
    final /* synthetic */ class AnonymousClass3 implements IntConsumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(int arg0) {
            ((Object[]) this.-$f0)[arg0] = ((IntFunction) this.-$f1).apply(arg0);
        }

        public /* synthetic */ AnonymousClass3(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void accept(int i) {
            $m$0(i);
        }
    }

    private final /* synthetic */ void $m$0(int arg0) {
        ((double[]) this.-$f0)[arg0] = ((IntToDoubleFunction) this.-$f1).applyAsDouble(arg0);
    }

    public /* synthetic */ -$Lambda$aUGKT4ItCOku5-JSG-x8Aqj2pJw(Object obj, Object obj2) {
        this.-$f0 = obj;
        this.-$f1 = obj2;
    }

    public final void accept(int i) {
        $m$0(i);
    }
}
