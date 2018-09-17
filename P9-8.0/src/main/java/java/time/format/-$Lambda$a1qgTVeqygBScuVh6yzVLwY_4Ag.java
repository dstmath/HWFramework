package java.time.format;

import java.time.chrono.Chronology;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$a1qgTVeqygBScuVh6yzVLwY_4Ag implements TemporalQuery {

    /* renamed from: java.time.format.-$Lambda$a1qgTVeqygBScuVh6yzVLwY_4Ag$1 */
    final /* synthetic */ class AnonymousClass1 implements Consumer {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ int -$f1;
        private final /* synthetic */ long -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ReducedPrinterParser) this.-$f3).lambda$-java_time_format_DateTimeFormatterBuilder$ReducedPrinterParser_132487((DateTimeParseContext) this.-$f4, this.-$f2, this.-$f0, this.-$f1, (Chronology) arg0);
        }

        public /* synthetic */ AnonymousClass1(int i, int i2, long j, Object obj, Object obj2) {
            this.-$f0 = i;
            this.-$f1 = i2;
            this.-$f2 = j;
            this.-$f3 = obj;
            this.-$f4 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    public final Object queryFrom(TemporalAccessor temporalAccessor) {
        return $m$0(temporalAccessor);
    }
}
