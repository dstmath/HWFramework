package com.android.server.devicepolicy;

public class LegacyGetListPolicy {
    private static final int[] CODE_LEGACY_GET_LIST_POLICY = {4004, 4005, 4006, 4007, 4008, 4019, 4010, 3006, 3003, 2507, 2510, 2513, 2516, 5009, 4027, 4020, 4028};

    public static boolean isLegacyGetListPolicy(int code) {
        return PolicyUtils.getIndexFromArray(code, CODE_LEGACY_GET_LIST_POLICY) != -1;
    }
}
