package android.os.health;

import android.os.health.HealthKeys.Constant;
import android.os.health.HealthKeys.Constants;

public final class PidHealthStats {
    public static final Constants CONSTANTS = new Constants(PidHealthStats.class);
    @Constant(type = 1)
    public static final int MEASUREMENT_WAKE_NESTING_COUNT = 20001;
    @Constant(type = 1)
    public static final int MEASUREMENT_WAKE_START_MS = 20003;
    @Constant(type = 1)
    public static final int MEASUREMENT_WAKE_SUM_MS = 20002;

    private PidHealthStats() {
    }
}
