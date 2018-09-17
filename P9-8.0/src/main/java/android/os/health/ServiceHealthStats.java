package android.os.health;

import android.os.health.HealthKeys.Constant;
import android.os.health.HealthKeys.Constants;

public final class ServiceHealthStats {
    public static final Constants CONSTANTS = new Constants(ServiceHealthStats.class);
    @Constant(type = 1)
    public static final int MEASUREMENT_LAUNCH_COUNT = 50002;
    @Constant(type = 1)
    public static final int MEASUREMENT_START_SERVICE_COUNT = 50001;

    private ServiceHealthStats() {
    }
}
