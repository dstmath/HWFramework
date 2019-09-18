package com.android.internal.telephony.metrics;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.connectivity.CellularBatteryStats;
import com.android.internal.app.IBatteryStats;
import com.android.internal.telephony.nano.TelephonyProto;

public class ModemPowerMetrics {
    private final IBatteryStats mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));

    public TelephonyProto.ModemPowerStats buildProto() {
        TelephonyProto.ModemPowerStats m = new TelephonyProto.ModemPowerStats();
        CellularBatteryStats stats = getStats();
        if (stats != null) {
            m.loggingDurationMs = stats.getLoggingDurationMs();
            m.energyConsumedMah = ((double) stats.getEnergyConsumedMaMs()) / 3600000.0d;
            m.numPacketsTx = stats.getNumPacketsTx();
            m.cellularKernelActiveTimeMs = stats.getKernelActiveTimeMs();
            if (stats.getTimeInRxSignalStrengthLevelMs() != null && stats.getTimeInRxSignalStrengthLevelMs().length > 0) {
                m.timeInVeryPoorRxSignalLevelMs = stats.getTimeInRxSignalStrengthLevelMs()[0];
            }
            m.sleepTimeMs = stats.getSleepTimeMs();
            m.idleTimeMs = stats.getIdleTimeMs();
            m.rxTimeMs = stats.getRxTimeMs();
            long[] t = stats.getTxTimeMs();
            m.txTimeMs = new long[t.length];
            for (int i = 0; i < t.length; i++) {
                m.txTimeMs[i] = t[i];
            }
        }
        return m;
    }

    private CellularBatteryStats getStats() {
        try {
            return this.mBatteryStats.getCellularBatteryStats();
        } catch (RemoteException e) {
            return null;
        }
    }
}
