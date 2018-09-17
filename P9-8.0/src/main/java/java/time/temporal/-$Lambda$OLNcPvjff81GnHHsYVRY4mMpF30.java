package java.time.temporal;

import java.time.LocalDate;
import java.util.function.UnaryOperator;

final /* synthetic */ class -$Lambda$OLNcPvjff81GnHHsYVRY4mMpF30 implements TemporalAdjuster {

    /* renamed from: java.time.temporal.-$Lambda$OLNcPvjff81GnHHsYVRY4mMpF30$10 */
    final /* synthetic */ class AnonymousClass10 implements TemporalAdjuster {
        private final /* synthetic */ int -$f0;

        private final /* synthetic */ Temporal $m$0(Temporal arg0) {
            return TemporalAdjusters.lambda$-java_time_temporal_TemporalAdjusters_21123(this.-$f0, arg0);
        }

        public /* synthetic */ AnonymousClass10(int i) {
            this.-$f0 = i;
        }

        public final Temporal adjustInto(Temporal temporal) {
            return $m$0(temporal);
        }
    }

    /* renamed from: java.time.temporal.-$Lambda$OLNcPvjff81GnHHsYVRY4mMpF30$11 */
    final /* synthetic */ class AnonymousClass11 implements TemporalAdjuster {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ int -$f1;

        private final /* synthetic */ Temporal $m$0(Temporal arg0) {
            return arg0.with(ChronoField.DAY_OF_MONTH, 1);
        }

        public /* synthetic */ AnonymousClass11(int i, int i2) {
            this.-$f0 = i;
            this.-$f1 = i2;
        }

        public final Temporal adjustInto(Temporal temporal) {
            return $m$0(temporal);
        }
    }

    /* renamed from: java.time.temporal.-$Lambda$OLNcPvjff81GnHHsYVRY4mMpF30$12 */
    final /* synthetic */ class AnonymousClass12 implements TemporalAdjuster {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ int -$f1;

        private final /* synthetic */ Temporal $m$0(Temporal arg0) {
            return TemporalAdjusters.lambda$-java_time_temporal_TemporalAdjusters_15513(this.-$f0, this.-$f1, arg0);
        }

        public /* synthetic */ AnonymousClass12(int i, int i2) {
            this.-$f0 = i;
            this.-$f1 = i2;
        }

        public final Temporal adjustInto(Temporal temporal) {
            return $m$0(temporal);
        }
    }

    /* renamed from: java.time.temporal.-$Lambda$OLNcPvjff81GnHHsYVRY4mMpF30$6 */
    final /* synthetic */ class AnonymousClass6 implements TemporalAdjuster {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Temporal $m$0(Temporal arg0) {
            return arg0.with((LocalDate) ((UnaryOperator) this.-$f0).apply(LocalDate.from(arg0)));
        }

        public /* synthetic */ AnonymousClass6(Object obj) {
            this.-$f0 = obj;
        }

        public final Temporal adjustInto(Temporal temporal) {
            return $m$0(temporal);
        }
    }

    /* renamed from: java.time.temporal.-$Lambda$OLNcPvjff81GnHHsYVRY4mMpF30$7 */
    final /* synthetic */ class AnonymousClass7 implements TemporalAdjuster {
        private final /* synthetic */ int -$f0;

        private final /* synthetic */ Temporal $m$0(Temporal arg0) {
            return TemporalAdjusters.lambda$-java_time_temporal_TemporalAdjusters_17076(this.-$f0, arg0);
        }

        public /* synthetic */ AnonymousClass7(int i) {
            this.-$f0 = i;
        }

        public final Temporal adjustInto(Temporal temporal) {
            return $m$0(temporal);
        }
    }

    /* renamed from: java.time.temporal.-$Lambda$OLNcPvjff81GnHHsYVRY4mMpF30$8 */
    final /* synthetic */ class AnonymousClass8 implements TemporalAdjuster {
        private final /* synthetic */ int -$f0;

        private final /* synthetic */ Temporal $m$0(Temporal arg0) {
            return TemporalAdjusters.lambda$-java_time_temporal_TemporalAdjusters_18421(this.-$f0, arg0);
        }

        public /* synthetic */ AnonymousClass8(int i) {
            this.-$f0 = i;
        }

        public final Temporal adjustInto(Temporal temporal) {
            return $m$0(temporal);
        }
    }

    /* renamed from: java.time.temporal.-$Lambda$OLNcPvjff81GnHHsYVRY4mMpF30$9 */
    final /* synthetic */ class AnonymousClass9 implements TemporalAdjuster {
        private final /* synthetic */ int -$f0;

        private final /* synthetic */ Temporal $m$0(Temporal arg0) {
            return TemporalAdjusters.lambda$-java_time_temporal_TemporalAdjusters_19758(this.-$f0, arg0);
        }

        public /* synthetic */ AnonymousClass9(int i) {
            this.-$f0 = i;
        }

        public final Temporal adjustInto(Temporal temporal) {
            return $m$0(temporal);
        }
    }

    public final Temporal adjustInto(Temporal temporal) {
        return $m$0(temporal);
    }
}
