package android.os;

import java.util.Iterator;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

final /* synthetic */ class -$Lambda$G_Gcg0ia_B_NRvJUIh_Nis__dWA implements ToIntFunction {

    /* renamed from: android.os.-$Lambda$G_Gcg0ia_B_NRvJUIh_Nis__dWA$2 */
    final /* synthetic */ class AnonymousClass2 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return HidlSupport.deepEquals(((Iterator) this.-$f0).next(), arg0);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: android.os.-$Lambda$G_Gcg0ia_B_NRvJUIh_Nis__dWA$3 */
    final /* synthetic */ class AnonymousClass3 implements IntPredicate {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ boolean $m$0(int arg0) {
            return HidlSupport.deepEquals(((Object[]) this.-$f0)[arg0], ((Object[]) this.-$f1)[arg0]);
        }

        public /* synthetic */ AnonymousClass3(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final boolean test(int i) {
            return $m$0(i);
        }
    }

    public final int applyAsInt(Object obj) {
        return $m$0(obj);
    }
}
