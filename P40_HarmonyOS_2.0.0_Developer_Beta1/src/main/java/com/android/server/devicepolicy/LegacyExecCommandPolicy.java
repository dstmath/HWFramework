package com.android.server.devicepolicy;

public class LegacyExecCommandPolicy {
    private static final int[] CODE_LEGACY_EXEC_COMMAND_POLICY_LISTS = {2505, 3001, 3002, 3004, 3005, 2508, 2509, 2511, 2512, 2514, 2515, 5007, 5008, 1508, 2517, 2518};
    private static final int[] CODE_LEGACY_EXEC_COMMAND_POLICY_NONES = {2001, 1502, 1501, 2504, 6001, 1512, 3503, 1507};
    private static final int[] CODE_LEGACY_EXEC_COMMAND_POLICY_STRINGS = {2501, 3007, 2503, 1510, 5002, 1511, 5006};
    private static final int ERROR_INDEX = -1;
    public static final int TYPE_COMMAND_LIST = 2;
    public static final int TYPE_COMMAND_NONE = 0;
    public static final int TYPE_COMMAND_STRING = 1;
    public static final int TYPE_COMMAND_UNKNOW = -1;

    private LegacyExecCommandPolicy() {
    }

    public static int getLegacyExecCommandPolicyType(int code) {
        if (PolicyUtils.getIndexFromArray(code, CODE_LEGACY_EXEC_COMMAND_POLICY_NONES) != -1) {
            return 0;
        }
        if (PolicyUtils.getIndexFromArray(code, CODE_LEGACY_EXEC_COMMAND_POLICY_STRINGS) != -1) {
            return 1;
        }
        if (PolicyUtils.getIndexFromArray(code, CODE_LEGACY_EXEC_COMMAND_POLICY_LISTS) != -1) {
            return 2;
        }
        return -1;
    }
}
