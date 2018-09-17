package java.util;

import java.io.Serializable;
import java.util.Map.Entry;

final /* synthetic */ class -$Lambda$NtA29M7WT1vsHnlynQ2fPZ1s5yA implements Comparator, Serializable {

    /* renamed from: java.util.-$Lambda$NtA29M7WT1vsHnlynQ2fPZ1s5yA$2 */
    final /* synthetic */ class AnonymousClass2 implements Comparator, Serializable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ int $m$0(Object arg0, Object arg1) {
            return ((Comparator) this.-$f0).compare(((Entry) arg0).getKey(), ((Entry) arg1).getKey());
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final int compare(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    /* renamed from: java.util.-$Lambda$NtA29M7WT1vsHnlynQ2fPZ1s5yA$3 */
    final /* synthetic */ class AnonymousClass3 implements Comparator, Serializable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ int $m$0(Object arg0, Object arg1) {
            return ((Comparator) this.-$f0).compare(((Entry) arg0).getValue(), ((Entry) arg1).getValue());
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.-$f0 = obj;
        }

        public final int compare(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    public final int compare(Object obj, Object obj2) {
        return $m$0(obj, obj2);
    }
}
