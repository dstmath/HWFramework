package com.android.server.rms.iaware.bigdata;

import android.rms.iaware.AwareLog;

public class BigDataSupervisor {
    private static final String TAG = "BigDataSupervisor";

    private enum BigDataCache {
        AwareBroadcastDumpRadar("AwareBroadcastDumpRadar", 1000),
        AppCleanupDumpRadar("AppCleanupDumpRadar", 500);
        
        private String mFeatureName;
        private int mRecordLimit;

        private BigDataCache(String name, int limit) {
            this.mFeatureName = name;
            this.mRecordLimit = limit;
        }

        public String getName() {
            return this.mFeatureName;
        }

        public int getLimit() {
            return this.mRecordLimit;
        }

        public static int getLimitByName(String name) {
            BigDataCache[] values = values();
            for (BigDataCache bdc : values) {
                if (name.equals(bdc.getName())) {
                    return bdc.getLimit();
                }
            }
            return 0;
        }
    }

    public boolean canRecord(BigDataSupervisor obj, String name) {
        int recCount = obj.monitorBigDataRecord();
        AwareLog.d(TAG, "featureName= " + name + "has recordCount " + recCount + " limit= " + BigDataCache.getLimitByName(name));
        if (recCount >= BigDataCache.getLimitByName(name)) {
            return false;
        }
        return true;
    }

    public int monitorBigDataRecord() {
        return 0;
    }
}
