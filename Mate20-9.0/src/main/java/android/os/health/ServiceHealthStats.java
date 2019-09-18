package android.os.health;

import android.os.health.HealthKeys;

public final class ServiceHealthStats {
    public static final HealthKeys.Constants CONSTANTS = new HealthKeys.Constants(ServiceHealthStats.class);
    @HealthKeys.Constant(type = 1)
    public static final int MEASUREMENT_LAUNCH_COUNT = 50002;
    @HealthKeys.Constant(type = 1)
    public static final int MEASUREMENT_START_SERVICE_COUNT = 50001;

    private ServiceHealthStats() {
    }
}
