package ohos.interwork.eventhandler;

import ohos.eventhandler.InnerEvent;
import ohos.interwork.utils.PacMapEx;

public final class InnerEventUtils {
    public static int getArg1(InnerEvent innerEvent) {
        if (innerEvent == null || innerEvent.object == null || !(innerEvent.object instanceof InnerEventExInfo)) {
            return 0;
        }
        return ((InnerEventExInfo) innerEvent.object).arg1;
    }

    public static int getArg2(InnerEvent innerEvent) {
        if (innerEvent == null || innerEvent.object == null || !(innerEvent.object instanceof InnerEventExInfo)) {
            return 0;
        }
        return ((InnerEventExInfo) innerEvent.object).arg2;
    }

    public static PacMapEx getPacMapEx(InnerEvent innerEvent) {
        if (innerEvent == null || innerEvent.object == null || !(innerEvent.object instanceof InnerEventExInfo)) {
            return null;
        }
        return ((InnerEventExInfo) innerEvent.object).pacMapEx;
    }

    public static void setExInfo(InnerEvent innerEvent, int i, int i2) {
        if (innerEvent != null) {
            InnerEventExInfo innerEventExInfo = new InnerEventExInfo();
            innerEventExInfo.arg1 = i;
            innerEventExInfo.arg2 = i2;
            innerEvent.object = innerEventExInfo;
        }
    }

    private InnerEventUtils() {
    }
}
