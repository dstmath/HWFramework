package java.time.temporal;

/* renamed from: java.time.temporal.-$$Lambda$TemporalAdjusters$8EK8KVP193YLBVkxtkiyg8uZHVo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TemporalAdjusters$8EK8KVP193YLBVkxtkiyg8uZHVo implements TemporalAdjuster {
    public static final /* synthetic */ $$Lambda$TemporalAdjusters$8EK8KVP193YLBVkxtkiyg8uZHVo INSTANCE = new $$Lambda$TemporalAdjusters$8EK8KVP193YLBVkxtkiyg8uZHVo();

    private /* synthetic */ $$Lambda$TemporalAdjusters$8EK8KVP193YLBVkxtkiyg8uZHVo() {
    }

    public final Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.DAY_OF_MONTH, 1);
    }
}
