package ohos.aafwk.utils.log;

import java.util.Objects;
import ohos.hiviewdfx.HiLogConstString;

public class KeyLog {
    public static final String CAN_REQUEST_PERMISSION = "can request permission";
    public static final String CONNECT_ABILITY = "connect ability";
    public static final String CREATE_WINDOW = "create window";
    public static final String DESTROY_WINDOW = "destroy window";
    public static final String DISCONNECT_ABILITY = "disconnect ability";
    public static final String DISPATCH_BACKKEY_PRESSED = "dispatch BackKey Pressed";
    public static final String DISPATCH_KEY_EVENT = "dispatch Key Event";
    public static final String DISPATCH_MOUSE_EVENT = "dispatch Mouse Event";
    public static final String DISPATCH_ORIENTATION_CHANGE = "dispatch Orientation Change";
    public static final String DISPATCH_ROTATION_EVENT = "dispatch Rotation Event";
    public static final String DISPATCH_TOUCH_EVENT = "dispatch Touch Event";
    public static final String KEYLOG_FMT_ARGS = "[%{public}s][%{public}s][%{public}s]: ";
    public static final String KEYLOG_FMT_NOARGS = "[%{public}s][%{public}s][%{public}s]";
    private static final String LABEL_ISNULL_LOG = "label is null";
    public static final String LIFECYCLE_ACTIVE = "lifecycle action ACTIVE";
    public static final String LIFECYCLE_BACKGROUND = "lifecycle action BACKGROUND";
    public static final String LIFECYCLE_FOREGROUND = "lifecycle action FOREGROUND";
    public static final String LIFECYCLE_INACTIVE = "lifecycle action INACTIVE";
    public static final String LIFECYCLE_START = "lifecycle action START";
    public static final String LIFECYCLE_STOP = "lifecycle action STOP";
    public static final String LOAD_ABILITY = "load ability";
    public static final String LOAD_WINDOW = "load window";
    public static final String ON_REQUEST_PERMISSIONS_RESULT = "on request permission result";
    public static final String POP_STACK = "pop from slice stack";
    public static final String PRESENT_SLICE = "present Slice (async)";
    public static final String PRESENT_SLICE_FORRESULT = "presentForResult Slice (async)";
    public static final String PUSH_STACK = "push to slice stack top";
    public static final String REMOVE_FROM_STACK = "remove from slice stack";
    public static final String REQUEST_PERMISSIONS_FROM_USER = "request permissions from user";
    public static final String SCHEDULE_CONNECT = "schedule connect ability";
    public static final String SCHEDULE_DISCONNECT = "schedule disconnect ability";
    public static final String SCHEDULE_LIFECYCLE = "schedule lifecycle";
    public static final String SET_UI_CONTENT = "set ui content";
    public static final String SET_UI_LAYOUT = "set ui window layout";
    public static final String SHOW_WINDOW = "show window";
    public static final String START_ABILITY = "start ability";
    public static final String START_ABILITY_FORRESULT = "start ability for result";
    public static final String STOP_ABILITY = "stop ability";
    public static final String TERMINATE_ABILITY = "terminate ability";
    public static final String TERMINATE_SLICE = "terminate (async)";

    public enum LogState {
        START,
        END
    }

    private KeyLog() {
    }

    public static int debug(@HiLogConstString String str, Object... objArr) {
        return Log.debug(LogLabel.LABEL_KEY, str, objArr);
    }

    public static int debug(LogLabel logLabel, String str, LogState logState) {
        Objects.requireNonNull(logLabel, LABEL_ISNULL_LOG);
        return Log.debug(LogLabel.LABEL_KEY, KEYLOG_FMT_NOARGS, logLabel.getTag(), str, logState);
    }

    public static int debugBound(@HiLogConstString String str, Object... objArr) {
        return Log.debug(LogLabel.LABEL_KEY_BOUND, str, objArr);
    }

    public static int debugBound(LogLabel logLabel, String str, LogState logState) {
        Objects.requireNonNull(logLabel, LABEL_ISNULL_LOG);
        return Log.debug(LogLabel.LABEL_KEY_BOUND, KEYLOG_FMT_NOARGS, logLabel.getTag(), str, logState);
    }

    public static int info(@HiLogConstString String str, Object... objArr) {
        return Log.info(LogLabel.LABEL_KEY, str, objArr);
    }

    public static int info(LogLabel logLabel, String str, LogState logState) {
        Objects.requireNonNull(logLabel, LABEL_ISNULL_LOG);
        return Log.info(LogLabel.LABEL_KEY, KEYLOG_FMT_NOARGS, logLabel.getTag(), str, logState);
    }

    public static int infoBound(@HiLogConstString String str, Object... objArr) {
        return Log.info(LogLabel.LABEL_KEY_BOUND, str, objArr);
    }

    public static int infoBound(LogLabel logLabel, String str, LogState logState) {
        Objects.requireNonNull(logLabel, LABEL_ISNULL_LOG);
        return Log.info(LogLabel.LABEL_KEY_BOUND, KEYLOG_FMT_NOARGS, logLabel.getTag(), str, logState);
    }
}
