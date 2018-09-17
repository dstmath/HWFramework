package android.os.health;

import android.os.health.HealthKeys.Constant;
import android.os.health.HealthKeys.Constants;

public final class PackageHealthStats {
    public static final Constants CONSTANTS = new Constants(PackageHealthStats.class);
    @Constant(type = 4)
    public static final int MEASUREMENTS_WAKEUP_ALARMS_COUNT = 40002;
    @Constant(type = 2)
    public static final int STATS_SERVICES = 40001;

    private PackageHealthStats() {
    }
}
