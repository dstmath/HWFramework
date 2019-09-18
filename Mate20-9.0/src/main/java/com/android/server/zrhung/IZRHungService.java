package com.android.server.zrhung;

import android.zrhung.ZrHungData;

public interface IZRHungService {
    public static final int ANR_APPEYE = 2;
    public static final int ANR_NONE = 0;
    public static final int ANR_ORIGINAL = 1;
    public static final String EVENT_HANDLESHOWANRDIALOG = "handleshowdialog";
    public static final String EVENT_RECOVERRESULT = "recoverresult";
    public static final String EVENT_SETTIME = "settime";
    public static final String EVENT_SHOWANRDIALOG = "showanrdialog";
    public static final String EVENT_SOCKETRECOVER = "socketrecover";
    public static final String PARAM_PID = "pid";
    public static final String PARAM_UID = "uid";
    public static final String PARA_APPEYEMESSAGE = "appeyemessage";
    public static final String PARA_EVENT = "event";
    public static final String PARA_PACKAGENAME = "packageName";
    public static final String PARA_PROCNAME = "processName";
    public static final String PARA_RESULT = "result";
    public static final String TYPE_APPEYE = "appeye";
    public static final String TYPE_ORIGINAL = "original";
    public static final String ZRHUNG_EVENTTYPE = "eventtype";

    boolean sendEvent(ZrHungData zrHungData);
}
