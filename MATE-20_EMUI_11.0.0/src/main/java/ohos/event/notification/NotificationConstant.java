package ohos.event.notification;

public final class NotificationConstant {
    public static final int APP_CANCEL_ALL_REASON_DELETE = 9;
    public static final int APP_CANCEL_REASON_DELETE = 8;
    public static final int APP_CANCEL_REASON_OTHER = 10;
    public static final int CANCEL_ALL_REASON_DELETE = 3;
    public static final int CANCEL_REASON_DELETE = 2;
    public static final int CLICK_REASON_DELETE = 1;
    public static final int ERROR_REASON_DELETE = 4;
    public static final String EXTRA_INPUTS_SOURCE = "notification_user_input_source";
    public static final int PACKAGE_BANNED_REASON_DELETE = 7;
    public static final int PACKAGE_CHANGED_REASON_DELETE = 5;
    public static final int USER_STOPPED_REASON_DELETE = 6;

    public enum InputsSource {
        FREE_FORM_INPUT,
        OPTION
    }

    public enum SemanticActionButton {
        NONE_ACTION_BUTTON,
        REPLY_ACTION_BUTTON,
        READ_ACTION_BUTTON,
        UNREAD_ACTION_BUTTON,
        DELETE_ACTION_BUTTON,
        ARCHIVE_ACTION_BUTTON,
        MUTE_ACTION_BUTTON,
        UNMUTE_ACTION_BUTTON,
        THUMBS_UP_ACTION_BUTTON,
        THUMBS_DOWN_ACTION_BUTTON,
        CALL_ACTION_BUTTON
    }

    private NotificationConstant() {
    }
}
