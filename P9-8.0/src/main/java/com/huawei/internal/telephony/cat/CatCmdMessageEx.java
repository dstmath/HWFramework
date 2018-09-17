package com.huawei.internal.telephony.cat;

import com.android.internal.telephony.cat.CatCmdMessage;
import com.huawei.android.util.NoExtAPIException;
import java.util.Arrays;

public class CatCmdMessageEx {

    public static final class SetupEventListConstants {
        public static final int BROWSER_TERMINATION_EVENT = 8;
        public static final int BROWSING_STATUS_EVENT = 15;
        public static final int HCI_CONNECTIVITY_EVENT = 19;
        public static final int IDLE_SCREEN_AVAILABLE_EVENT = 5;
        public static final int LANGUAGE_SELECTION_EVENT = 7;
        public static final int USER_ACTIVITY_EVENT = 4;
    }

    public static class SetupEventListSettings {
        public int[] eventList;

        public SetupEventListSettings(com.android.internal.telephony.cat.CatCmdMessage.SetupEventListSettings event) {
            if (event != null) {
                this.eventList = Arrays.copyOf(event.eventList, event.eventList.length);
            }
        }
    }

    public static SetupEventListSettings getSetEventList(CatCmdMessage obj) {
        return new SetupEventListSettings(obj.getSetEventList());
    }

    public static String getCallNumber(CatCmdMessage obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static int[] get_eventList(CatCmdMessage obj) {
        return obj.getSetEventList().eventList;
    }

    public static boolean hasIconLoadFailed(CatCmdMessage obj) {
        return obj.hasIconLoadFailed();
    }

    public static boolean getLoadOptionalIconFailed(CatCmdMessage obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static String getLanguageNotification(CatCmdMessage obj) {
        return obj.getLanguageNotification();
    }
}
