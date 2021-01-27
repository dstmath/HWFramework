package android.os.health;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.os.BatteryStats;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.internal.app.IBatteryStats;

public class SystemHealthManager {
    private final IBatteryStats mBatteryStats;

    @UnsupportedAppUsage
    public SystemHealthManager() {
        this(IBatteryStats.Stub.asInterface(ServiceManager.getService(BatteryStats.SERVICE_NAME)));
    }

    public SystemHealthManager(IBatteryStats batteryStats) {
        this.mBatteryStats = batteryStats;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public static SystemHealthManager from(Context context) {
        return (SystemHealthManager) context.getSystemService(Context.SYSTEM_HEALTH_SERVICE);
    }

    public HealthStats takeUidSnapshot(int uid) {
        try {
            return this.mBatteryStats.takeUidSnapshot(uid).getHealthStats();
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }

    public HealthStats takeMyUidSnapshot() {
        return takeUidSnapshot(Process.myUid());
    }

    public HealthStats[] takeUidSnapshots(int[] uids) {
        try {
            HealthStatsParceler[] parcelers = this.mBatteryStats.takeUidSnapshots(uids);
            HealthStats[] results = new HealthStats[uids.length];
            int N = uids.length;
            for (int i = 0; i < N; i++) {
                results[i] = parcelers[i].getHealthStats();
            }
            return results;
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }
}
