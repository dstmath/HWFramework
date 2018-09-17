package java.util;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

final /* synthetic */ class -$Lambda$4EqhxufgNKat19m0CB0-toH_lzo implements Comparator, Serializable {
    private final /* synthetic */ Object -$f0;

    /* renamed from: java.util.-$Lambda$4EqhxufgNKat19m0CB0-toH_lzo$1 */
    final /* synthetic */ class AnonymousClass1 implements Comparator, Serializable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ int $m$0(Object arg0, Object arg1) {
            return Double.compare(((ToDoubleFunction) this.-$f0).applyAsDouble(arg0), ((ToDoubleFunction) this.-$f0).applyAsDouble(arg1));
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final int compare(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.-$Lambda$4EqhxufgNKat19m0CB0-toH_lzo$2 */
    final /* synthetic */ class AnonymousClass2 implements Comparator, Serializable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ int $m$0(Object arg0, Object arg1) {
            return Integer.compare(((ToIntFunction) this.-$f0).applyAsInt(arg0), ((ToIntFunction) this.-$f0).applyAsInt(arg1));
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final int compare(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.-$Lambda$4EqhxufgNKat19m0CB0-toH_lzo$3 */
    final /* synthetic */ class AnonymousClass3 implements Comparator, Serializable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ int $m$0(Object arg0, Object arg1) {
            return Long.compare(((ToLongFunction) this.-$f0).applyAsLong(arg0), ((ToLongFunction) this.-$f0).applyAsLong(arg1));
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.-$f0 = obj;
        }

        public final int compare(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.-$Lambda$4EqhxufgNKat19m0CB0-toH_lzo$4 */
    final /* synthetic */ class AnonymousClass4 implements Comparator, Serializable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ int $m$0(Object arg0, Object arg1) {
            return ((Comparator) this.-$f0).compare(((Function) this.-$f1).apply(arg0), ((Function) this.-$f1).apply(arg1));
        }

        public /* synthetic */ AnonymousClass4(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final int compare(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.-$Lambda$4EqhxufgNKat19m0CB0-toH_lzo$5 */
    final /* synthetic */ class AnonymousClass5 implements Comparator, Serializable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ int $m$0(Object arg0, Object arg1) {
            return ((Comparator) this.-$f0).lambda$-java_util_Comparator_10127((Comparator) this.-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass5(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final int compare(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    private final /* synthetic */ int $m$0(Object arg0, Object arg1) {
        return ((Comparable) ((Function) this.-$f0).apply(arg0)).compareTo(((Function) this.-$f0).apply(arg1));
    }

    public /* synthetic */ -$Lambda$4EqhxufgNKat19m0CB0-toH_lzo(Object obj) {
        this.-$f0 = obj;
    }

    public final int compare(Object obj, Object obj2) {
        return $m$0(obj, obj2);
    }
}
