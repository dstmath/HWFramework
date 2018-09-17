package com.huawei.internal.telephony.cat;

import com.android.internal.telephony.cat.AppInterface.CommandType;
import com.android.internal.telephony.cat.CatService;
import com.huawei.android.util.NoExtAPIException;

public class AppInterfaceEx {
    public static final String ALPHA_STRING = "alpha_string";
    public static final String CARD_STATUS = "card_status";
    public static final String CAT_ALPHA_NOTIFY_ACTION = "com.android.internal.stk.alpha_notify";
    public static final String CAT_ICC_STATUS_CHANGE = "com.android.internal.stk.icc_status_change";
    public static final String CAT_IDLE_SCREEN_ACTION = "com.huawei.intent.action.stk.idle_screen";
    public static final String CHECK_SCREEN_IDLE_ACTION = "android.intent.action.stk.check_screen_idle";
    public static final String REFRESH_RESULT = "refresh_result";

    public static void sendLanguageSelection(int languageFirst, int languageSecond, CatService obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static String getLanguageNotificationCode(CatService obj) {
        return obj.getLanguageNotificationCode();
    }

    public static void setLanguageSelectionStateAvailable(boolean languageSelectionStateAvailable, CatService obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean getLanguageSelectionStateAvailable(CatService obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static CommandType getState_LANGUAGE_NOTIFICATION() {
        return CommandType.LANGUAGE_NOTIFICATION;
    }
}
