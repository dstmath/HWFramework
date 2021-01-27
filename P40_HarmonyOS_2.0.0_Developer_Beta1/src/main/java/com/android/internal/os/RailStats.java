package com.android.internal.os;

import android.util.ArrayMap;
import android.util.Slog;
import java.util.Map;

public final class RailStats {
    private static final String CELLULAR_SUBSYSTEM = "cellular";
    private static final String TAG = "RailStats";
    private static final String WIFI_SUBSYSTEM = "wifi";
    private long mCellularTotalEnergyUseduWs = 0;
    private Map<Long, RailInfoData> mRailInfoData = new ArrayMap();
    private boolean mRailStatsAvailability = true;
    private long mWifiTotalEnergyUseduWs = 0;

    public void updateRailData(long index, String railName, String subSystemName, long timestampSinceBootMs, long energyUsedSinceBootuWs) {
        if (subSystemName.equals("wifi") || subSystemName.equals(CELLULAR_SUBSYSTEM)) {
            RailInfoData node = this.mRailInfoData.get(Long.valueOf(index));
            if (node == null) {
                this.mRailInfoData.put(Long.valueOf(index), new RailInfoData(index, railName, subSystemName, timestampSinceBootMs, energyUsedSinceBootuWs));
                if (subSystemName.equals("wifi")) {
                    this.mWifiTotalEnergyUseduWs += energyUsedSinceBootuWs;
                } else if (subSystemName.equals(CELLULAR_SUBSYSTEM)) {
                    this.mCellularTotalEnergyUseduWs += energyUsedSinceBootuWs;
                }
            } else {
                long energyUsedSinceLastLoguWs = energyUsedSinceBootuWs - node.energyUsedSinceBootuWs;
                if (timestampSinceBootMs - node.timestampSinceBootMs < 0 || energyUsedSinceLastLoguWs < 0) {
                    energyUsedSinceLastLoguWs = node.energyUsedSinceBootuWs;
                }
                node.timestampSinceBootMs = timestampSinceBootMs;
                node.energyUsedSinceBootuWs = energyUsedSinceBootuWs;
                if (subSystemName.equals("wifi")) {
                    this.mWifiTotalEnergyUseduWs += energyUsedSinceLastLoguWs;
                } else if (subSystemName.equals(CELLULAR_SUBSYSTEM)) {
                    this.mCellularTotalEnergyUseduWs += energyUsedSinceLastLoguWs;
                }
            }
        }
    }

    public void resetCellularTotalEnergyUsed() {
        this.mCellularTotalEnergyUseduWs = 0;
    }

    public void resetWifiTotalEnergyUsed() {
        this.mWifiTotalEnergyUseduWs = 0;
    }

    public long getCellularTotalEnergyUseduWs() {
        return this.mCellularTotalEnergyUseduWs;
    }

    public long getWifiTotalEnergyUseduWs() {
        return this.mWifiTotalEnergyUseduWs;
    }

    public void reset() {
        this.mCellularTotalEnergyUseduWs = 0;
        this.mWifiTotalEnergyUseduWs = 0;
    }

    public RailStats getRailStats() {
        return this;
    }

    public void setRailStatsAvailability(boolean railStatsAvailability) {
        this.mRailStatsAvailability = railStatsAvailability;
    }

    public boolean isRailStatsAvailable() {
        return this.mRailStatsAvailability;
    }

    public static class RailInfoData {
        private static final String TAG = "RailInfoData";
        public long energyUsedSinceBootuWs;
        public long index;
        public String railName;
        public String subSystemName;
        public long timestampSinceBootMs;

        private RailInfoData(long index2, String railName2, String subSystemName2, long timestampSinceBootMs2, long energyUsedSinceBoot) {
            this.index = index2;
            this.railName = railName2;
            this.subSystemName = subSystemName2;
            this.timestampSinceBootMs = timestampSinceBootMs2;
            this.energyUsedSinceBootuWs = energyUsedSinceBoot;
        }

        public void printData() {
            Slog.d(TAG, "Index = " + this.index);
            Slog.d(TAG, "RailName = " + this.railName);
            Slog.d(TAG, "SubSystemName = " + this.subSystemName);
            Slog.d(TAG, "TimestampSinceBootMs = " + this.timestampSinceBootMs);
            Slog.d(TAG, "EnergyUsedSinceBootuWs = " + this.energyUsedSinceBootuWs);
        }
    }
}
