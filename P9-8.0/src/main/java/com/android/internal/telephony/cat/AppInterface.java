package com.android.internal.telephony.cat;

import android.content.ComponentName;

public interface AppInterface {
    public static final String ALPHA_STRING = "alpha_string";
    public static final String CARD_STATUS = "card_status";
    public static final String CAT_ALPHA_NOTIFY_ACTION = "com.android.internal.stk.alpha_notify";
    public static final String CAT_CMD_ACTION = "com.android.internal.stk.command";
    public static final String CAT_ICC_STATUS_CHANGE = "com.android.internal.stk.icc_status_change";
    public static final String CAT_REFRESH_FILE_CHANGE_NOTIFY_ACTION = "com.nttdocomo.android.lpa.intent.action.stk.refresh_file_change_notify";
    public static final String CAT_SESSION_END_ACTION = "com.android.internal.stk.session_end";
    public static final String REFRESH_RESULT = "refresh_result";
    public static final String STK_PERMISSION = "android.permission.RECEIVE_STK_COMMANDS";

    public enum CommandType {
        DISPLAY_TEXT(33),
        GET_INKEY(34),
        GET_INPUT(35),
        LAUNCH_BROWSER(21),
        PLAY_TONE(32),
        REFRESH(1),
        SELECT_ITEM(36),
        SEND_SS(17),
        SEND_USSD(18),
        SEND_SMS(19),
        SEND_DTMF(20),
        SET_UP_EVENT_LIST(5),
        SET_UP_IDLE_MODE_TEXT(40),
        SET_UP_MENU(37),
        SET_UP_CALL(16),
        SET_POLL_INTERVALL(3),
        PROVIDE_LOCAL_INFORMATION(38),
        LANGUAGE_NOTIFICATION(53),
        OPEN_CHANNEL(64),
        CLOSE_CHANNEL(65),
        RECEIVE_DATA(66),
        SEND_DATA(67),
        GET_CHANNEL_STATUS(68);
        
        private int mValue;

        private CommandType(int value) {
            this.mValue = value;
        }

        public int value() {
            return this.mValue;
        }

        public static CommandType fromInt(int value) {
            for (CommandType e : values()) {
                if (e.mValue == value) {
                    return e;
                }
            }
            return null;
        }
    }

    String getLanguageNotificationCode();

    void onCmdResponse(CatResponseMessage catResponseMessage);

    void onEventDownload(CatEventMessage catEventMessage);

    static ComponentName getDefaultSTKApplication() {
        return ComponentName.unflattenFromString("com.android.stk/.StkCmdReceiver");
    }
}
