package com.android.server.wifi;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.connectivity.WifiBatteryStats;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.server.wifi.nano.WifiMetricsProto;
import java.io.PrintWriter;
import java.text.DecimalFormat;

public class WifiPowerMetrics {
    private static final String TAG = "WifiPowerMetrics";
    private final IBatteryStats mBatteryStats;

    public WifiPowerMetrics() {
        this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
    }

    @VisibleForTesting
    public WifiPowerMetrics(IBatteryStats batteryStats) {
        this.mBatteryStats = batteryStats;
    }

    public WifiMetricsProto.WifiPowerStats buildProto() {
        WifiMetricsProto.WifiPowerStats m = new WifiMetricsProto.WifiPowerStats();
        WifiBatteryStats stats = getStats();
        if (stats != null) {
            m.loggingDurationMs = stats.getLoggingDurationMs();
            m.energyConsumedMah = ((double) stats.getEnergyConsumedMaMs()) / 3600000.0d;
            m.idleTimeMs = stats.getIdleTimeMs();
            m.rxTimeMs = stats.getRxTimeMs();
            m.txTimeMs = stats.getTxTimeMs();
            m.wifiKernelActiveTimeMs = stats.getKernelActiveTimeMs();
            m.numPacketsTx = stats.getNumPacketsTx();
            m.numBytesTx = stats.getNumBytesTx();
            m.numPacketsRx = stats.getNumPacketsRx();
            m.numBytesRx = stats.getNumPacketsRx();
            m.sleepTimeMs = stats.getSleepTimeMs();
            m.scanTimeMs = stats.getScanTimeMs();
            m.monitoredRailEnergyConsumedMah = ((double) stats.getMonitoredRailChargeConsumedMaMs()) / 3600000.0d;
        }
        return m;
    }

    public WifiMetricsProto.WifiRadioUsage buildWifiRadioUsageProto() {
        WifiMetricsProto.WifiRadioUsage m = new WifiMetricsProto.WifiRadioUsage();
        WifiBatteryStats stats = getStats();
        if (stats != null) {
            m.loggingDurationMs = stats.getLoggingDurationMs();
            m.scanTimeMs = stats.getScanTimeMs();
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
            pw.println("Amount of time kernel is active because of wifi data (ms): " + s.wifiKernelActiveTimeMs);
            pw.println("Amount of time wifi is in sleep (ms): " + s.sleepTimeMs);
            pw.println("Amount of time wifi is scanning (ms): " + s.scanTimeMs);
            pw.println("Number of packets sent (tx): " + s.numPacketsTx);
            pw.println("Number of bytes sent (tx): " + s.numBytesTx);
            pw.println("Number of packets received (rx): " + s.numPacketsRx);
            pw.println("Number of bytes sent (rx): " + s.numBytesRx);
            pw.println("Energy consumed across measured wifi rails (mAh): " + new DecimalFormat("#.##").format(s.monitoredRailEnergyConsumedMah));
        }
        WifiMetricsProto.WifiRadioUsage wifiRadioUsage = buildWifiRadioUsageProto();
        pw.println("Wifi radio usage metrics:");
        pw.println("Logging duration (time on battery): " + wifiRadioUsage.loggingDurationMs);
        pw.println("Amount of time wifi is in scan mode while on battery (ms): " + wifiRadioUsage.scanTimeMs);
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
