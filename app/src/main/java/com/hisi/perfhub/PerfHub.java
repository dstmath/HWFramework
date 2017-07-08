package com.hisi.perfhub;

public final class PerfHub {
    public static final int PERF_CTRL_TYPE_HIGHPERF = 0;
    public static final int PERF_CTRL_TYPE_LOW_TEMP_LOW_VOLTAGE = 4;
    public static final int PERF_CTRL_TYPE_LOW_VOLTAGE = 3;
    public static final int PERF_CTRL_TYPE_MAX = 5;
    public static final int PERF_CTRL_TYPE_SPEC_SCENE = 1;
    public static final int PERF_CTRL_TYPE_THERMAL_PROTECT = 2;
    public static final int PERF_EVENT_ANIMATION = 7;
    public static final int PERF_EVENT_APP_START = 4;
    public static final int PERF_EVENT_BOOT_COMPLETE = 0;
    public static final int PERF_EVENT_DECODEBITMAP = 4101;
    public static final int PERF_EVENT_INTERACTION = 1;
    public static final int PERF_EVENT_IOP = 4100;
    public static final int PERF_EVENT_LANDSCAPE = 4103;
    public static final int PERF_EVENT_LIST_FLING = 8;
    public static final int PERF_EVENT_MAX = 12;
    public static final int PERF_EVENT_OFF = 0;
    public static final int PERF_EVENT_ON = 1;
    public static final int PERF_EVENT_PROBE = 4097;
    public static final int PERF_EVENT_PROBE_DRAWFRAME = 4102;
    public static final int PERF_EVENT_RAW_REQ = 4096;
    public static final int PERF_EVENT_RESTART = 4098;
    public static final int PERF_EVENT_ROTATING = 6;
    public static final int PERF_EVENT_SCREEN_OFF = 10;
    public static final int PERF_EVENT_SCREEN_ON = 9;
    public static final int PERF_EVENT_STATUSBAR = 11;
    public static final int PERF_EVENT_SUSTAINEDMODE = 4099;
    public static final int PERF_EVENT_VSYNC_OFF = 3;
    public static final int PERF_EVENT_VSYNC_ON = 2;
    public static final int PERF_EVENT_WINDOW_SWITCH = 5;
    public static final int PERF_HMP_POLICY_STATE_OFF = 0;
    public static final int PERF_HMP_POLICY_STATE_ON = 1;
    public static final int PERF_HMP_PRIORITY_0 = 0;
    public static final int PERF_HMP_PRIORITY_1 = 1;
    public static final int PERF_HMP_PRIORITY_2 = 2;
    public static final int PERF_HMP_PRIORITY_3 = 3;
    public static final int PERF_HMP_PRIORITY_4 = 4;
    public static final int PERF_HMP_PRIORITY_5 = 5;
    public static final int PERF_HMP_PRIORITY_MAX = 6;
    public static final int PERF_IOPREFETCH_START = 0;
    public static final int PERF_IOPREFETCH_STOP = 1;
    public static final int PERF_STATE_OFF = 0;
    public static final int PERF_STATE_ON = 1;
    public static final int PERF_TAG_AVL_B_CPU_FREQ_LIST = 35;
    public static final int PERF_TAG_AVL_DDR_FREQ_LIST = 37;
    public static final int PERF_TAG_AVL_GPU_FREQ_LIST = 36;
    public static final int PERF_TAG_AVL_L_CPU_FREQ_LIST = 34;
    public static final int PERF_TAG_B_CPU_CUR = 6;
    public static final int PERF_TAG_B_CPU_MAX = 5;
    public static final int PERF_TAG_B_CPU_MIN = 4;
    public static final int PERF_TAG_CTRL_TYPE = 0;
    public static final int PERF_TAG_CTRL_TYPE_NEW = 38;
    public static final int PERF_TAG_DDR_CUR = 12;
    public static final int PERF_TAG_DDR_MAX = 11;
    public static final int PERF_TAG_DDR_MIN = 10;
    public static final int PERF_TAG_DEF_B_CPU_MAX = 24;
    public static final int PERF_TAG_DEF_B_CPU_MIN = 23;
    public static final int PERF_TAG_DEF_DDR_MAX = 28;
    public static final int PERF_TAG_DEF_DDR_MIN = 27;
    public static final int PERF_TAG_DEF_GPU_MAX = 26;
    public static final int PERF_TAG_DEF_GPU_MIN = 25;
    public static final int PERF_TAG_DEF_HMP_DN_THRES = 30;
    public static final int PERF_TAG_DEF_HMP_UP_THRES = 29;
    public static final int PERF_TAG_DEF_IPA_CONTROL_TEMP = 32;
    public static final int PERF_TAG_DEF_IPA_SUSTAINABLE_POWER = 33;
    public static final int PERF_TAG_DEF_IPA_SWITCH_TEMP = 31;
    public static final int PERF_TAG_DEF_L_CPU_MAX = 22;
    public static final int PERF_TAG_DEF_L_CPU_MIN = 21;
    public static final int PERF_TAG_GPU_CUR = 9;
    public static final int PERF_TAG_GPU_MAX = 8;
    public static final int PERF_TAG_GPU_MIN = 7;
    public static final int PERF_TAG_HMP_DN_THRES = 14;
    public static final int PERF_TAG_HMP_POLICY_STATE = 16;
    public static final int PERF_TAG_HMP_PRIORITY = 15;
    public static final int PERF_TAG_HMP_UP_THRES = 13;
    public static final int PERF_TAG_IPA_CONTROL_TEMP = 18;
    public static final int PERF_TAG_IPA_SUSTAINABLE_POWER = 19;
    public static final int PERF_TAG_IPA_SWITCH_TEMP = 17;
    public static final int PERF_TAG_L_CPU_CUR = 3;
    public static final int PERF_TAG_L_CPU_MAX = 2;
    public static final int PERF_TAG_L_CPU_MIN = 1;
    public static final int PERF_TAG_MAX = 39;
    public static final int PERF_TAG_TASK_FORK_ON_B_CLUSTER = 20;
    public static final int REQUEST_FAILED = -1;
    public static final int REQUEST_SUCCEEDED = 0;
    private static final String TAG = "PF_API_JAVA";

    private native int native_perf_config_get(int[] iArr, int[] iArr2);

    private native int native_perf_config_set(int[] iArr, int[] iArr2);

    private native int native_perf_event(int i, String str, int[] iArr);

    public int perfEvent(int eventId, String PackageName, int... payload) {
        int ret = native_perf_event(eventId, PackageName, payload);
        if (ret != 0) {
            return REQUEST_FAILED;
        }
        return ret;
    }

    public int perfConfigSet(int[] tags, int[] values) {
        if (native_perf_config_set(tags, values) != 0) {
            return REQUEST_FAILED;
        }
        return REQUEST_SUCCEEDED;
    }

    public int perfConfigGet(int[] tags, int[] values) {
        if (native_perf_config_get(tags, values) != 0) {
            return REQUEST_FAILED;
        }
        return REQUEST_SUCCEEDED;
    }
}
