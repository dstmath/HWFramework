package android.os.health;

import android.os.health.HealthKeys.Constant;
import android.os.health.HealthKeys.Constants;

public final class ProcessHealthStats {
    public static final Constants CONSTANTS = new Constants(ProcessHealthStats.class);
    @Constant(type = 1)
    public static final int MEASUREMENT_ANR_COUNT = 30005;
    @Constant(type = 1)
    public static final int MEASUREMENT_CRASHES_COUNT = 30004;
    @Constant(type = 1)
    public static final int MEASUREMENT_FOREGROUND_MS = 30006;
    @Constant(type = 1)
    public static final int MEASUREMENT_STARTS_COUNT = 30003;
    @Constant(type = 1)
    public static final int MEASUREMENT_SYSTEM_TIME_MS = 30002;
    @Constant(type = 1)
    public static final int MEASUREMENT_USER_TIME_MS = 30001;

    private ProcessHealthStats() {
    }
}
