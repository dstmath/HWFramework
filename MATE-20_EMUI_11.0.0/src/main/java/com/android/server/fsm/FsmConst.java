package com.android.server.fsm;

public class FsmConst {
    public static final int POLICY_TYPE_INTELLIGENT = 1;
    public static final int POLICY_TYPE_NORMAL = 0;
    public static final int POLICY_TYPE_TENT = 2;
    public static final int POLICY_TYPE_TEST = 3;
    public static final String TAG_FSM_FOLDSCREEN_MANAGER_SERVICE = "Fsm_FoldScreenManagerService";
    public static final String TAG_FSM_FOLDSCREEN_STATE_IMPL = "Fsm_FoldScreenStateImpl";
    public static final String TAG_FSM_INTELLIGENT_POSTURE_PREPROCESS = "Fsm_IntelligentPosturePreprocess";
    public static final String TAG_FSM_MAG_WAKEUP_MANAGER = "Fsm_MagnetometerWakeupManager";
    public static final String TAG_FSM_NORMAL_POSTURE_PREPROCESS = "Fsm_NormalPosturePreprocess";
    public static final String TAG_FSM_POSTURE_PREPROCESS_MANAGER = "Fsm_PosturePreprocessManager";
    public static final String TAG_FSM_REPORT_MONITOR_PREPROCESS = "Fsm_ReportMonitorProcess";
    public static final String TAG_FSM_SENSOR_FOLDSTATE_MANAGER = "Fsm_SensorFoldStateManager";
    public static final String TAG_FSM_SENSOR_POSTURE_MANAGER = "Fsm_SensorPostureManager";
    public static final String TAG_FSM_SENSOR_POSTURE_PROCESS = "Fsm_SensorPostureProcess";
    public static final String TAG_FSM_STATE_MACHINE = "Fsm_PostureStateMachine";
    public static final String TAG_FSM_TENT_SENSOR_PREPROCESS = "Fsm_TentSensorProcess";
    public static final String TAG_FSM_TEST_POSTURE_PREPROCESS = "Fsm_TestPosturePreprocess";
    public static final int WAKE_UP_TYPE_DEFAULT = 0;
    public static final int WAKE_UP_TYPE_MAGNETOMETER = 4;
    public static final int WAKE_UP_TYPE_PICK_UP = 2;
    public static final int WAKE_UP_TYPE_SWITCH_SCREEN = 1;
}
