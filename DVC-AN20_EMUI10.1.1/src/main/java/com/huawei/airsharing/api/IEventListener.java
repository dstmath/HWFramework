package com.huawei.airsharing.api;

public interface IEventListener {
    public static final int EVENT_ID_BINDER_DIED = 3200;
    public static final int EVENT_ID_DEVICE_ADD = 3001;
    public static final int EVENT_ID_DEVICE_CONN_FAIL = 3004;
    public static final int EVENT_ID_DEVICE_CONN_SUCC = 3003;
    public static final int EVENT_ID_DEVICE_DISCONN_SUCC = 3005;
    public static final int EVENT_ID_DEVICE_REMOVE = 3002;
    public static final int EVENT_ID_DEVICE_RTSP_CONN = 3007;
    public static final int EVENT_ID_DEVICE_SCAN_FINISH = 3006;
    public static final int EVENT_ID_DEVICE_UPDATE = 3008;
    public static final int EVENT_ID_HISIGHT_STATE_AUTH_CODE_REQ = 3107;
    public static final int EVENT_ID_HISIGHT_STATE_CONNECTED = 3100;
    public static final int EVENT_ID_HISIGHT_STATE_CONNECTING = 3106;
    @Deprecated
    public static final int EVENT_ID_HISIGHT_STATE_CONNECT_FAILD = 3101;
    public static final int EVENT_ID_HISIGHT_STATE_CONNECT_FAILED = 3101;
    @Deprecated
    public static final int EVENT_ID_HISIGHT_STATE_CONNNECTED = 3100;
    public static final int EVENT_ID_HISIGHT_STATE_DISCONNECTED = 3102;
    public static final int EVENT_ID_HISIGHT_STATE_PAUSED = 3105;
    public static final int EVENT_ID_HISIGHT_STATE_PLAYING = 3104;
    public static final int EVENT_ID_HISIGHT_STATE_SERVER_READY = 3103;
    public static final int EVENT_ID_MSDP_PORT_NUMBER = 3300;
    public static final int EVENT_ID_NOTICE_DIALOG_AGREE = 3503;
    public static final int EVENT_ID_NOTICE_DIALOG_CANCEL = 3502;
    public static final int EVENT_ID_NOTICE_DIALOG_NOT_SHOW = 3501;
    public static final int EVENT_ID_NOTICE_DIALOG_SHOW = 3500;
    public static final int EVENT_ID_NOTICE_ERROR_INFO = 3600;
    public static final int EVENT_ID_NOTIFY_DEVICE_DOWN = 101;
    public static final int EVENT_ID_NOTIFY_DEVICE_UP = 100;
    public static final int EVENT_ID_NOTIFY_DEVICE_UPDOWN = 102;
    public static final int EVENT_ID_NOTIFY_ENGINE_AUDIO_CAPTURE_ERR = 201;
    public static final int EVENT_ID_NOTIFY_ENGINE_AUDIO_ENCODE_ERR = 203;
    public static final int EVENT_ID_NOTIFY_ENGINE_MEDIASENDER_ERR = 205;
    public static final int EVENT_ID_NOTIFY_ENGINE_VIDEO_CAPTURE_ERR = 202;
    public static final int EVENT_ID_NOTIFY_ENGINE_VIDEO_ENCODE_ERR = 204;
    public static final int EVENT_ID_NOTIFY_PLAYER_MEDIARECEIVER_ERR = 305;
    public static final int EVENT_ID_NOTIFY_PLAYER_MEDIA_FOR_PLAY = 2000;
    public static final int EVENT_ID_NOTIFY_PLAYER_MEDIA_PAUSE = 2003;
    public static final int EVENT_ID_NOTIFY_PLAYER_MEDIA_PLAY = 2001;
    public static final int EVENT_ID_NOTIFY_PLAYER_MEDIA_POSITION_CHANGED = 2004;
    public static final int EVENT_ID_NOTIFY_PLAYER_MEDIA_STOP = 2002;
    public static final int EVENT_ID_NOTIFY_PLAYER_PUSH_FRAME = 310;
    public static final int EVENT_ID_NOTIFY_PLAYER_PUSH_FRAME_AUDIO = 312;
    public static final int EVENT_ID_NOTIFY_PLAYER_PUSH_RTSP = 311;
    public static final int EVENT_ID_NOTIFY_PLAYER_SERVER_UPDATE = 2006;
    public static final int EVENT_ID_NOTIFY_PLAYER_SET_VOLUME = 2008;
    public static final int EVENT_ID_NOTIFY_PLAYER_START_RESULT = 2010;
    public static final int EVENT_ID_WEAK_NETWORK = 3400;
    public static final String EVENT_TYPE_HOSTNETWORK_UNREACHABLE = "HOSTNETWORK_UNREACHABLE";
    public static final String EVENT_TYPE_LOCALNETWORK_UNREACHABLE = "LOCALNETWORK_UNREACHABLE";
    public static final String EVENT_TYPE_NETWORK_UNREACHABLE = "NETWORK_UNREACHABLE";
    public static final String EVENT_TYPE_NETWORK_WEAK = "NETWORK_WEAK";
    public static final String EVENT_TYPE_NOTIFY_BINDER_DIED = "EVENT_TYPE_NOTIFY_BINDER_DIED";
    public static final String EVENT_TYPE_PLAYER_MEDIA_PLAY_PLAYSTART = "EVENT_TYPE_PLAYER_MEDIA_PLAY_PLAYSTART";
    public static final String EVENT_TYPE_PLAYER_MEDIA_PLAY_PUSH_SUCC = "EVENT_TYPE_PLAYER_MEDIA_PLAY_PUSH_SUCC";
    public static final String EVENT_TYPE_PLAYER_MEDIA_STOP_GRABED = "EVENT_TYPE_PLAYER_MEDIA_STOP_GRABED";
    public static final String EVENT_TYPE_PLAYER_MEDIA_STOP_PLAYFINISH = "EVENT_TYPE_PLAYER_MEDIA_STOP_PLAYFINISH";
    public static final String EVENT_TYPE_PLAYER_MEDIA_STOP_PUSH_FAILED = "EVENT_TYPE_PLAYER_MEDIA_STOP_PUSH_FAILED";
    public static final String EVENT_TYPE_PLAYER_MEDIA_STOP_SWITCH_PUSH = "EVENT_TYPE_PLAYER_MEDIA_STOP_SWITCH_PUSH";
    public static final String EVENT_TYPE_PLAYER_SERVER_UPDATE_CONNECTED = "EVENT_TYPE_PLAYER_SERVER_UPDATE_CONNECTED";
    public static final String EVENT_TYPE_PLAYER_SERVER_UPDATE_DISCONNECTED = "EVENT_TYPE_PLAYER_SERVER_UPDATE_DISCONNECTED";
    public static final String EVENT_TYPE_PLAYER_SERVER_UPDATE_DOWN = "EVENT_TYPE_PLAYER_SERVER_UPDATE_DOWN";
    public static final String EVENT_TYPE_PLAYER_SERVER_UPDATE_UP = "EVENT_TYPE_PLAYER_SERVER_UPDATE_UP";
    public static final String EVENT_TYPE_PLAYER_START_FAILED = "EVENT_TYPE_PLAYER_SUBSCRIBE_FAILED";
    public static final String EVENT_TYPE_PLAYER_START_SUCCESS = "EVENT_TYPE_PLAYER_SUBSCRIBE_SUCCESS";
    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    public static final String EXTRA_EVENT_TYPE = "EXTRA_EVENT_TYPE";

    boolean onEvent(int i, String str);

    void onEventHandle(Event event);

    void onProjectionDeviceUpdate(int i, ProjectionDevice projectionDevice);

    @Deprecated
    default void onDisplayUpdate(int eventId, String devName, String devAddress, int priority) {
    }

    @Deprecated
    default void onMirrorUpdate(int eventId, String devName, String udn, int priority, boolean isSupportMirror) {
    }
}
