package com.android.server.devicepolicy;

public class LegacySetPolicy {
    private static final int[] CODE_LEGACY_SET_POLICY = {1004, 1006, 5011, 1024, 1008, 1010, 1012, 1016, 1014, 1020, 1018, 1022, 1026, 1028, 1030, 1032, 1034, 1036, 1038, 1513, 1504};

    public static boolean isLegacySetPolicy(int code) {
        return PolicyUtils.getIndexFromArray(code, CODE_LEGACY_SET_POLICY) != -1;
    }
}
