package java.util.stream;

import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.MatchOps.AnonymousClass1MatchSink;
import java.util.stream.MatchOps.AnonymousClass2MatchSink;
import java.util.stream.MatchOps.AnonymousClass3MatchSink;
import java.util.stream.MatchOps.AnonymousClass4MatchSink;

final /* synthetic */ class -$Lambda$6Ha_z5NODYn_Qs3Vg00tRqQ1oBM implements Supplier {
    private final /* synthetic */ Object -$f0;
    private final /* synthetic */ Object -$f1;

    /* renamed from: java.util.stream.-$Lambda$6Ha_z5NODYn_Qs3Vg00tRqQ1oBM$1 */
    final /* synthetic */ class AnonymousClass1 implements Supplier {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ Object $m$0() {
            return new AnonymousClass2MatchSink((MatchKind) this.-$f0, (IntPredicate) this.-$f1);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final Object get() {
            return $m$0();
        }
    }

    /* renamed from: java.util.stream.-$Lambda$6Ha_z5NODYn_Qs3Vg00tRqQ1oBM$2 */
    final /* synthetic */ class AnonymousClass2 implements Supplier {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ Object $m$0() {
            return new AnonymousClass3MatchSink((MatchKind) this.-$f0, (LongPredicate) this.-$f1);
        }

        public /* synthetic */ AnonymousClass2(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final Object get() {
            return $m$0();
        }
    }

    /* renamed from: java.util.stream.-$Lambda$6Ha_z5NODYn_Qs3Vg00tRqQ1oBM$3 */
    final /* synthetic */ class AnonymousClass3 implements Supplier {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ Object $m$0() {
            return new AnonymousClass1MatchSink((MatchKind) this.-$f0, (Predicate) this.-$f1);
        }

        public /* synthetic */ AnonymousClass3(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final Object get() {
            return $m$0();
        }
    }

    private final /* synthetic */ Object $m$0() {
        return new AnonymousClass4MatchSink((MatchKind) this.-$f0, (DoublePredicate) this.-$f1);
    }

    public /* synthetic */ -$Lambda$6Ha_z5NODYn_Qs3Vg00tRqQ1oBM(Object obj, Object obj2) {
        this.-$f0 = obj;
        this.-$f1 = obj2;
    }

    public final Object get() {
        return $m$0();
    }
}
