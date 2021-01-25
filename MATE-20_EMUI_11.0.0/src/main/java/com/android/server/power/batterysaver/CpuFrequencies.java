package com.android.server.power.batterysaver;

import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.slice.SliceClientPermissions;
import java.util.Map;

public class CpuFrequencies {
    private static final String TAG = "CpuFrequencies";
    @GuardedBy({"mLock"})
    private final ArrayMap<Integer, Long> mCoreAndFrequencies = new ArrayMap<>();
    private final Object mLock = new Object();

    public CpuFrequencies parseString(String cpuNumberAndFrequencies) {
        synchronized (this.mLock) {
            this.mCoreAndFrequencies.clear();
            try {
                for (String pair : cpuNumberAndFrequencies.split(SliceClientPermissions.SliceAuthority.DELIMITER)) {
                    String pair2 = pair.trim();
                    if (pair2.length() != 0) {
                        String[] coreAndFreq = pair2.split(":", 2);
                        if (coreAndFreq.length == 2) {
                            this.mCoreAndFrequencies.put(Integer.valueOf(Integer.parseInt(coreAndFreq[0])), Long.valueOf(Long.parseLong(coreAndFreq[1])));
                        } else {
                            throw new IllegalArgumentException("Wrong format");
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                Slog.wtf(TAG, "Invalid configuration: '" + cpuNumberAndFrequencies + "'");
            }
        }
        return this;
    }

    public ArrayMap<String, String> toSysFileMap() {
        ArrayMap<String, String> map = new ArrayMap<>();
        addToSysFileMap(map);
        return map;
    }

    public void addToSysFileMap(Map<String, String> map) {
        synchronized (this.mLock) {
            int size = this.mCoreAndFrequencies.size();
            for (int i = 0; i < size; i++) {
                int core = this.mCoreAndFrequencies.keyAt(i).intValue();
                long freq = this.mCoreAndFrequencies.valueAt(i).longValue();
                map.put("/sys/devices/system/cpu/cpu" + Integer.toString(core) + "/cpufreq/scaling_max_freq", Long.toString(freq));
            }
        }
    }
}
