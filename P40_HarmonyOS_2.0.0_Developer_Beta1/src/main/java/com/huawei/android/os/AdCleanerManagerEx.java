package com.huawei.android.os;

import java.util.List;
import java.util.Map;

@Deprecated
public class AdCleanerManagerEx {
    private static final int CODE_AD_DEBUG = 1019;
    private static final int CODE_CLEAN_AD_STRATEGY = 1018;
    private static final int CODE_SET_AD_STRATEGY = 1017;
    private static final String DESCRIPTOR_ADCLEANER_MANAGER_EX = "android.os.AdCleanerManagerEx";
    private static final String TAG = "AdCleanerManagerEx";

    @Deprecated
    public static int printRuleMaps() {
        return -1;
    }

    @Deprecated
    public static int cleanAdFilterRules(List<String> list, boolean isNeedRest) {
        return -1;
    }

    @Deprecated
    public static int setAdFilterRules(Map<String, List<String>> map, Map<String, List<String>> map2, boolean isNeedRest) {
        return -1;
    }
}
