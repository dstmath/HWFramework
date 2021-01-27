package com.android.server.swing;

import android.content.Context;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.huawei.android.app.HiViewEx;

public class HwSwingReport {
    private static final int ACTION_UNKNOWN = 0;
    public static final int BASE_REPORT_ID = 991380000;
    private static final String CHARACTERISTICS = SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT);
    private static final String END_COMMA = "}";
    private static final int EVENTID_SWING_MOTION_ACTION = 991383000;
    private static final int EVENTID_SWING_MOTION_RESPONCE = 991383001;
    public static final int EVENT_ID_SWING_EYE_GAZE_ACTION = 991383002;
    private static final String EYE_GAZE = "eye_gaze";
    private static final int FETCH = 11;
    private static final int HOVER_SCREEN_OFF = 13;
    private static final int HOVER_SCREEN_ON = 12;
    private static final boolean IS_TV = "tv".equals(CHARACTERISTICS);
    private static final int MEDIA_FORWARD_REWIND = 6;
    private static final int MEDIA_PAUSE = 7;
    private static final int PEOPLE_COUNT = 10;
    private static final int PUSH = 5;
    private static final int REPORT_INTERVAL_TIME = (IS_TV ? Constant.MILLISEC_TO_HOURS : 18000000);
    private static final int REPORT_THRESHOLD = (IS_TV ? 50 : 200);
    private static final int SWIPE_DOWN = 4;
    private static final int SWIPE_LEFT = 1;
    private static final int SWIPE_RIGHT = 2;
    private static final int SWIPE_UP = 3;
    private static final String TAG = "HwSwingReport";
    private static final int VOLUME_MUTE = 9;
    private static final int VOLUME_UP_DOWN = 8;
    private static Context sContext;
    private static String sFocusPkgName = "";
    private static long sLastMotionActionReportTime = 0;
    private static long sLastMotionResponseReportTime = 0;

    private HwSwingReport() {
    }

    private static int getMotionGestureType(String motionAction) {
        if (HwSwingMotionGestureConstant.VALUE_SLIDE_LEFT.equals(motionAction)) {
            return 1;
        }
        if (HwSwingMotionGestureConstant.VALUE_SLIDE_RIGHT.equals(motionAction)) {
            return 2;
        }
        if (HwSwingMotionGestureConstant.VALUE_SLIDE_UP.equals(motionAction)) {
            return 3;
        }
        if (HwSwingMotionGestureConstant.VALUE_SLIDE_DOWN.equals(motionAction)) {
            return 4;
        }
        if (HwSwingMotionGestureConstant.VALUE_PUSH.equals(motionAction)) {
            return 5;
        }
        if (HwSwingMotionGestureConstant.VALUE_FETCH.equals(motionAction)) {
            return 11;
        }
        if (HwSwingMotionGestureConstant.VALUE_HOVER_SCREEN_ON.equals(motionAction)) {
            return 12;
        }
        if (HwSwingMotionGestureConstant.VALUE_HOVER_SCREEN_OFF.equals(motionAction)) {
            return 13;
        }
        return 0;
    }

    private static int getTvPeopleCountType(int motionStatus, int motionAction, int originType) {
        if (motionStatus != 13) {
            return originType;
        }
        if (motionAction == 1 || motionAction == 2) {
            return 10;
        }
        return originType;
    }

    private static int getTvGestureType(int motionStatus, int motionAction, int originType) {
        if (motionStatus != 12) {
            return originType;
        }
        if (motionAction == 1) {
            return 9;
        }
        if (motionAction == 2) {
            return 7;
        }
        if (motionAction == 4) {
            return 6;
        }
        if (motionAction != 8) {
            return originType;
        }
        return 8;
    }

    private static String convertMotionGestureToString(int motionStatus, int motionAction, int motionCount, String pkgName) {
        int motionType = getTvGestureType(motionStatus, motionAction, getTvPeopleCountType(motionStatus, motionAction, 0));
        if (motionType == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder("{motionType:");
        sb.append(motionType);
        sb.append(",eventCount:");
        sb.append(motionCount);
        if (pkgName != null) {
            sb.append(",pkgName:");
            sb.append(pkgName);
        }
        sb.append(END_COMMA);
        return sb.toString();
    }

    private static String convertMotionGestureToString(String motionAction, int motionCount, String pkgName) {
        int motionType = getMotionGestureType(motionAction);
        StringBuilder sb = new StringBuilder("{motionType:");
        sb.append(motionType);
        sb.append(",eventCount:");
        sb.append(motionCount);
        if (pkgName != null) {
            sb.append(",pkgName:");
            sb.append(pkgName);
        }
        sb.append(END_COMMA);
        return sb.toString();
    }

    public static void setFocusPkgName(String focusPkgName) {
        sFocusPkgName = focusPkgName == null ? "NA" : focusPkgName;
    }

    public static void setContext(Context context) {
        sContext = context;
    }

    public static boolean reportMotionEventAction(int motionStatus, int motionAction, int motionOffset, int motionCount, boolean isContainPkg) {
        String content;
        String pkgName = isContainPkg ? sFocusPkgName : null;
        long now = SystemClock.uptimeMillis();
        if ((now - sLastMotionActionReportTime < ((long) REPORT_INTERVAL_TIME) && motionCount < REPORT_THRESHOLD) || (content = convertMotionGestureToString(motionStatus, motionAction, motionCount, pkgName)) == null) {
            return false;
        }
        Log.i(TAG, "reportMotionEventAction content : " + content + ",eventId :" + EVENTID_SWING_MOTION_ACTION);
        HiViewEx.report(HiViewEx.byContent((int) EVENTID_SWING_MOTION_ACTION, sContext, content));
        sLastMotionActionReportTime = now;
        return true;
    }

    public static boolean reportMotionEventAction(String motionAction, int motionCount, boolean isContainPkg) {
        String pkgName = isContainPkg ? sFocusPkgName : null;
        long now = SystemClock.uptimeMillis();
        if (now - sLastMotionActionReportTime < ((long) REPORT_INTERVAL_TIME) && motionCount < REPORT_THRESHOLD) {
            return false;
        }
        String content = convertMotionGestureToString(motionAction, motionCount, pkgName);
        Log.i(TAG, "reportMotionEventAction content : " + content + ",eventId :" + EVENTID_SWING_MOTION_ACTION);
        HiViewEx.report(HiViewEx.byContent((int) EVENTID_SWING_MOTION_ACTION, sContext, content));
        sLastMotionActionReportTime = now;
        return true;
    }

    public static boolean reportMotionEventActionTimely(String motionAction, boolean isContainPkg) {
        String content = convertMotionGestureToString(motionAction, 1, isContainPkg ? sFocusPkgName : null);
        Log.i(TAG, "reportMotionEventActionTimely content : " + content + ",eventId :" + EVENTID_SWING_MOTION_ACTION);
        HiViewEx.report(HiViewEx.byContent((int) EVENTID_SWING_MOTION_ACTION, sContext, content));
        return true;
    }

    public static boolean reportMotionEventResponse(int motionStatus, int motionAction, int motionOffset, int motionCount, boolean isContainPkg) {
        String content;
        String pkgName = isContainPkg ? sFocusPkgName : null;
        long now = SystemClock.uptimeMillis();
        if ((now - sLastMotionResponseReportTime < ((long) REPORT_INTERVAL_TIME) && motionCount < REPORT_THRESHOLD) || (content = convertMotionGestureToString(motionStatus, motionAction, motionCount, pkgName)) == null) {
            return false;
        }
        Log.i(TAG, "reportMotionEventResponse content : " + content + ",eventId :" + EVENTID_SWING_MOTION_RESPONCE);
        HiViewEx.report(HiViewEx.byContent((int) EVENTID_SWING_MOTION_RESPONCE, sContext, content));
        sLastMotionResponseReportTime = now;
        return true;
    }

    public static boolean reportMotionEventResponse(String motionAction, int motionCount, boolean isContainPkg) {
        String pkgName = isContainPkg ? sFocusPkgName : null;
        long now = SystemClock.uptimeMillis();
        if (now - sLastMotionResponseReportTime < ((long) REPORT_INTERVAL_TIME) && motionCount < REPORT_THRESHOLD) {
            return false;
        }
        String content = convertMotionGestureToString(motionAction, motionCount, pkgName);
        Log.i(TAG, "reportMotionEventResponse content : " + content + ",eventId :" + EVENTID_SWING_MOTION_RESPONCE);
        HiViewEx.report(HiViewEx.byContent((int) EVENTID_SWING_MOTION_RESPONCE, sContext, content));
        sLastMotionResponseReportTime = now;
        return true;
    }

    public static boolean reportMotionEventResponseTimely(String motionAction, boolean isContainPkg) {
        String content = convertMotionGestureToString(motionAction, 1, isContainPkg ? sFocusPkgName : null);
        Log.i(TAG, "reportMotionEventResponseTimely content : " + content + ",eventId :" + EVENTID_SWING_MOTION_RESPONCE);
        HiViewEx.report(HiViewEx.byContent((int) EVENTID_SWING_MOTION_RESPONCE, sContext, content));
        return true;
    }

    public static void reportSwingEyeGaze(Context context, int eventId, int eventCount) {
        String content = "{eventType:" + EYE_GAZE + ",eventCount:" + eventCount + END_COMMA;
        Log.i(TAG, "reportSwingEyeGaze content : " + content);
        HiViewEx.report(HiViewEx.byContent(eventId, context, content));
    }
}
