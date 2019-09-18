package com.huawei.wallet.sdk.common.utils;

import com.huawei.android.os.BuildEx;

public final class EMUIBuildUtil {
    public static final int EMUI_SDK_INT_CP3;
    private static final boolean isEmui50 = (isSupportBuildEx() && BuildEx.VERSION.EMUI_SDK_INT >= 11);

    public static class VERSION {
        public static final int EMUI_SDK_INT = EMUIBuildUtil.EMUI_SDK_INT_CP3;
    }

    public static class VERSION_CODES {
        public static final int CUR_DEVELOPMENT = 10000;
        public static final int EMUI_1_0 = 1;
        public static final int EMUI_1_5 = 2;
        public static final int EMUI_1_6 = 3;
        public static final int EMUI_2_0_JB = 4;
        public static final int EMUI_2_0_KK = 5;
        public static final int EMUI_2_3 = 6;
        public static final int EMUI_3_0 = 7;
        public static final int EMUI_3_0_5 = 8;
        public static final int EMUI_3_1 = 8;
        public static final int EMUI_4_0 = 9;
        public static final int EMUI_4_1 = 10;
        public static final int EMUI_5_0 = 11;
        public static final int EMUI_5_1 = 12;
        public static final int EMUI_5_1_b10x = 13;
        public static final int EMUI_5_1_b200 = 13;
        public static final int EMUI_6_0 = 14;
        public static final int EMUI_8_0_1 = 15;
        public static final int EMUI_9_0 = 17;
        public static final int EMUI_9_1 = 19;
        public static final int UNKNOWN_EMUI = 0;
    }

    static {
        int i = 0;
        if (isSupportBuildEx()) {
            i = BuildEx.VERSION.EMUI_SDK_INT;
        }
        EMUI_SDK_INT_CP3 = i;
    }

    private static boolean isSupportBuildEx() {
        return isClassSupport("com.huawei.android.os.BuildEx");
    }

    private static boolean isClassSupport(String classname) {
        try {
            Class.forName(classname);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isEmui50() {
        return isEmui50;
    }
}
