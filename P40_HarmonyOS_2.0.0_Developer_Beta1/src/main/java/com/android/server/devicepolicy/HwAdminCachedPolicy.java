package com.android.server.devicepolicy;

public class HwAdminCachedPolicy {
    private static final int[] CODE_CACHED_LIST_POLICY = {4004, 4005, 4006, 4007, 4008, 4019, 4010, 4027, 4020, 4028};
    private static final int[] CODE_CACHED_POLICY = {4001, 4009, 4003, 4012, 4013, 4014, 4002, 4015, 4016, 4017, 4018, 4025, 4026, 4021, 4022, 4023, 5021, 5022, 4024};

    private HwAdminCachedPolicy() {
    }

    public static boolean isCachedPolicy(int code, boolean isList) {
        return PolicyUtils.getIndexFromArray(code, isList ? CODE_CACHED_LIST_POLICY : CODE_CACHED_POLICY) != -1;
    }
}
