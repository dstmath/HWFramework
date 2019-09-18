package com.android.server.wifi;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.connectivity.WifiBatteryStats;
import android.util.Log;
import com.android.internal.app.IBatteryStats;
import com.android.server.wifi.nano.WifiMetricsProto;
import java.io.PrintWriter;

public class WifiPowerMetrics {
    private static final String TAG = "WifiPowerMetrics";
    private final IBatteryStats mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));

    public WifiMetricsProto.WifiPowerStats buildProto() {
        WifiMetricsProto.WifiPowerStats m = new WifiMetricsProto.WifiPowerStats();
        WifiBatteryStats stats = getStats();
        if (stats != null) {
            m.loggingDurationMs = stats.getLoggingDurationMs();
            m.energyConsumedMah = ((double) stats.getEnergyConsumedMaMs()) / 3600000.0d;
            m.idleTimeMs = stats.getIdleTimeMs();
            m.rxTimeMs = stats.getRxTimeMs();
            m.txTimeMs = stats.getTxTimeMs();
        }
        return m;
    }

    public void dump(PrintWriter pw) {
        WifiMetricsProto.WifiPowerStats s = buildProto();
        if (s != null) {
            pw.println("Wifi power metrics:");
            pw.println("Logging duration (time on battery): " + s.loggingDurationMs);
            pw.println("Energy consumed by wifi (mAh): " + s.energyConsumedMah);
            pw.println("Amount of time wifi is in idle (ms): " + s.idleTimeMs);
            pw.println("Amount of time wifi is in rx (ms): " + s.rxTimeMs);
            pw.println("Amount of time wifi is in tx (ms): " + s.txTimeMs);
        }
    }

    private WifiBatteryStats getStats() {
        try {
            return this.mBatteryStats.getWifiBatteryStats();
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to obtain Wifi power stats from BatteryStats");
            return null;
        }
    }
}
