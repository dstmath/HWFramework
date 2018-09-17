package android.common;

import android.os.Bundle;

public interface HwFrameworkMonitor {
    public static final String KEY_ACTION = "action";
    public static final String KEY_ACTION_COUNT = "actionCount";
    public static final String KEY_BROADCAST_INTENT = "intent";
    public static final String KEY_COMPONENT = "component";
    public static final String KEY_CPU_STATE = "cpuState";
    public static final String KEY_CPU_TIME = "cpuTime";
    public static final String KEY_ERROR_TYPE = "errorType";
    public static final String KEY_EXTRA = "extra";
    public static final String KEY_MMS_BROADCAST_FLAG = "mmsFlag";
    public static final String KEY_PACKAGE = "package";
    public static final String KEY_REASON = "reason";
    public static final String KEY_RECEIVER = "receiver";
    public static final String KEY_RECEIVE_TIME = "receiveTime";
    public static final String KEY_VERSION_CODE = "versionCode";
    public static final String KEY_VERSION_NAME = "versionName";
    public static final int SCENE_BROADCAST_OVERLENGTH = 907400002;
    public static final int SCENE_BROADCAST_OVERTIME = 907400003;
    public static final int SCENE_CHECK_PASSWORD_FAIL = 907034001;
    public static final int SCENE_CHECK_PASSWORD_FAIL_ERROR_TYPE_1001 = 1001;
    public static final int SCENE_CHECK_PASSWORD_FAIL_ERROR_TYPE_1002 = 1002;
    public static final int SCENE_CHECK_PASSWORD_FAIL_ERROR_TYPE_1003 = 1003;
    public static final int SCENE_CHECK_PASSWORD_FAIL_ERROR_TYPE_1004 = 1004;
    public static final int SCENE_CHECK_PASSWORD_FAIL_ERROR_TYPE_1005 = 1005;
    public static final int SCENE_CHECK_PASSWORD_FAIL_ERROR_TYPE_1006 = 1006;
    public static final int SCENE_CHECK_PASSWORD_FAIL_ERROR_TYPE_1007 = 1007;
    public static final int SCENE_CPU_SPEED_TOO_LARGE = 907400016;
    public static final int SCENE_DPM_FACTORY_RESET = 907400018;
    public static final int SCENE_PACKAGE_INSTALL_FAIL_SD = 907400000;
    public static final int SCENE_PACKAGE_INSTALL_FAIL_UID = 907400001;
    public static final int SCENE_TRAFFIC_SPENT_TOO_MUCH = 907400017;
    public static final String UNKNOWN = "unknown";

    boolean monitor(int i, Bundle bundle);
}
