package com.huawei.dmsdpsdk2.hiplay;

import com.huawei.dmsdpsdk2.HwLog;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class HieventHiplayError {
    public static final int ERROR_CODE_CANCELFAILED = 4;
    public static final int ERROR_CODE_DEVICETIMEOUT = 5;
    public static final int ERROR_CODE_DISCOVERTIMEOUT = 1;
    public static final int ERROR_CODE_HMS_CONNECTFAIL = 2;
    public static final int ERROR_CODE_RETRYTIMES = 3;
    public static final int HIEVENT_HIPLAY_ERROR = 950006240;
    private static String TAG = "HieventHiplayError";
    private static Constructor hieventConstructor;
    private static Class klassHievent;
    private static Class klassHiview;
    private static Method putIntMethod;
    private static Method reportMethod;

    HieventHiplayError() {
    }

    static {
        klassHiview = null;
        klassHievent = null;
        hieventConstructor = null;
        putIntMethod = null;
        reportMethod = null;
        try {
            klassHiview = Class.forName("com.huawei.android.app.HiViewEx");
            klassHievent = Class.forName("com.huawei.android.app.HiEventEx");
            hieventConstructor = klassHievent.getDeclaredConstructor(Integer.TYPE);
            putIntMethod = klassHievent.getMethod("putInt", String.class, Integer.TYPE);
            reportMethod = klassHiview.getMethod("report", klassHievent);
        } catch (ClassNotFoundException e) {
            String str = TAG;
            HwLog.e(str, "clinit ClassNotFoundException " + e.toString());
        } catch (NoSuchMethodException e2) {
            String str2 = TAG;
            HwLog.e(str2, "clinit NoSuchMethodException " + e2.toString());
        }
    }

    public static void reportError(int errorcode) {
        Constructor constructor = hieventConstructor;
        if (constructor != null && putIntMethod != null && reportMethod != null) {
            try {
                Object event = constructor.newInstance(Integer.valueOf((int) HIEVENT_HIPLAY_ERROR));
                putIntMethod.invoke(event, "errorcode", Integer.valueOf(errorcode));
                reportMethod.invoke(null, event);
            } catch (InstantiationException e) {
                String str = TAG;
                HwLog.e(str, "reportError InstantiationException " + e.toString());
            } catch (IllegalAccessException e2) {
                String str2 = TAG;
                HwLog.e(str2, "reportError IllegalAccessException " + e2.toString());
            } catch (InvocationTargetException e3) {
                String str3 = TAG;
                HwLog.e(str3, "reportError InvocationTargetException " + e3.toString());
            }
        }
    }
}
