package android.os.health;

import android.os.health.HealthKeys;

public final class PackageHealthStats {
    public static final HealthKeys.Constants CONSTANTS = new HealthKeys.Constants(PackageHealthStats.class);
    @HealthKeys.Constant(type = 4)
    public static final int MEASUREMENTS_WAKEUP_ALARMS_COUNT = 40002;
    @HealthKeys.Constant(type = 2)
    public static final int STATS_SERVICES = 40001;

    private PackageHealthStats() {
    }
}
