package com.huawei.server.rme.hyperhold;

import android.content.Context;
import java.util.Collections;
import java.util.Map;

public class AdvancedKillerAwareUtils {

    public enum MemoryLevelListType {
        LEVEL_0,
        LEVEL_1,
        LEVEL_2,
        LEVEL_3,
        LEVEL_4,
        LEVEL_5,
        LEVEL_CACHED_PROCESS
    }

    public AdvancedKillerAwareUtils(Context context) {
    }

    public Map<String, AdvancedKillerPackageInfo> getPackageInfoMap(MemoryLevelListType type) {
        return Collections.emptyMap();
    }

    public long getMemAvailable() {
        return 0;
    }

    public boolean setSchedPriority() {
        return false;
    }

    public void resetSchedPriority(boolean isProcFast) {
    }

    public void beginKillFast() {
    }

    public void endKillFast() {
    }
}
