package com.android.server.devicepolicy;

public class LegacyGetPolicy {
    private static final int[] CODE_LEGACY_GET_POLICY = {4001, 4009, 4002, 4003, 1503, 1005, 5012, 4011, 4012, 4013, 4014, 4015, 4016, 4017, 4018, 4025, 4026, 1025, 1007, 1009, 1011, 1013, 1015, 1017, 1021, 1019, 1023, 2506, 1027, 1029, 1031, 1033, 1035, 1037, 1039, 1505, 4021, 4022, 4023, 5021, 5022, 4024};

    public static boolean isLegacyGetPolicy(int code) {
        return PolicyUtils.getIndexFromArray(code, CODE_LEGACY_GET_POLICY) != -1;
    }
}
