package com.huawei.pgmng.log;

import android.os.SystemClock;
import android.util.Log;
import java.util.HashMap;

public class LogPower {
    public static final int ABNORMAL_EVENT_RAD = 227;
    public static final int ACTIVITY_PAUSED = 101;
    public static final int ACTIVITY_RESUMED = 100;
    public static final int ADD_PROCESS_DEPENDENCY = 166;
    public static final int ADD_VIEW = 151;
    public static final int ALARM_BLOCKED = 115;
    public static final int ALARM_START = 121;
    public static final int ALL_DOWNLOAD_FINISH = 110;
    public static final int APPWIDGET_ENABLED = 168;
    public static final int APP_EXIT = 108;
    public static final int APP_FOCUS_CHANGE = 230;
    public static final int APP_IN_PIP = 222;
    public static final int APP_LAUNCHER = 109;
    public static final int APP_NETWORK_QUALITY = 229;
    public static final int APP_OUT_PIP = 223;
    public static final int APP_PROCESS_EXIT = 112;
    public static final int APP_PROCESS_START = 111;
    public static final int APP_RUN_BG = 114;
    public static final int APP_RUN_FRONT = 113;
    public static final int APP_START_SPEEDUP = 139;
    public static final int AUDIO_ADD_ACTIVE_TRACK = 219;
    public static final int AUDIO_CREATAE_TRACK = 217;
    public static final int AUDIO_LOOPING = 196;
    public static final int AUDIO_OUTPUT_CLOSE = 198;
    public static final int AUDIO_RELEASE_TRACK = 218;
    public static final int AUDIO_REMOVE_ACTIVE_TRACK = 220;
    public static final int AUDIO_SESSION_ID_NEW = 162;
    public static final int AUDIO_SESSION_ID_RELEASE = 163;
    public static final int AUDIO_SESSION_START = 164;
    public static final int AUDIO_SESSION_STOP = 165;
    public static final int AUDIO_SESSION_UNKNOWN_STOP = 206;
    public static final int AUDIO_SET_FINAL_VOLUME = 221;
    public static final int AUDIO_START = 147;
    public static final int AUDIO_VOLUME = 201;
    public static final int BACK_KEY_PRESS = 213;
    public static final int BLE_SOCKECT_CLOSED = 173;
    public static final int BLE_SOCKECT_CONNECTED = 172;
    public static final int BT_ACTIVE_APP = 181;
    public static final int BT_INACTIVE_APP = 182;
    public static final int COM_THERMAL_EVENT = 146;
    public static final int CPU_LOAD_ABNORMAL = 185;
    public static final int DISABLE_SENSOR = 144;
    public static final int DISABLE_SENSOR_DETAIL = 193;
    public static final int DOUBLE_TAP = 209;
    public static final int ENABLE_SENSOR = 143;
    public static final int ENABLE_SENSOR_DETAIL = 192;
    public static final int END_CAMERA = 134;
    public static final int END_CHG_ROTATION = 130;
    public static final int END_WEBKIT_CANVAS = 132;
    public static final long FILTER_DURATION = 5000;
    public static final int FIRST_IAWARE_TAG = 2000;
    public static final int FLING_END = 211;
    public static final int FLING_FINISH = 155;
    public static final int FLING_START = 154;
    public static final int FOREGROUND_SERVICE_START = 233;
    public static final int FOREGROUND_SERVICE_STOP = 234;
    public static final int FREEZER_EXCEPTION = 148;
    public static final int FULL_SCREEN = 120;
    public static final int FULL_SCREEN_END = 135;
    public static final int GAMEOF3D_PAUSED = 107;
    public static final int GAMEOF3D_RESUMED = 106;
    public static final int GOOGLE_CONNECTED_CHECK = 194;
    public static final int GPS_END = 157;
    public static final int GPS_MEASURE_END = 232;
    public static final int GPS_MEASURE_START = 231;
    public static final int GPS_REQ_END = 203;
    public static final int GPS_REQ_START = 202;
    public static final int GPS_START = 156;
    public static final int GPU_DRAW = 153;
    public static final int HARD_KEY_EVENT = 174;
    public static final int HW_PUSH_FINISH = 119;
    public static final int KEYBOARD_HIDE = 118;
    public static final int KEYBOARD_SHOW = 117;
    public static final int LAST_IAWARE_TAG = 2999;
    public static final int LONG_PRESS = 212;
    public static final int LOW_POWER_AUDIO_RESET = 171;
    public static final int LOW_POWER_AUDIO_START = 169;
    public static final int LOW_POWER_AUDIO_STOP = 170;
    public static final int MEDIA_CODEC_RELEASE = 226;
    public static final int MEDIA_CODEC_START = 224;
    public static final int MEDIA_CODEC_STOP = 225;
    public static final int MEDIA_DECODE_TYPE = 138;
    public static final int MEDIA_RECORDER_END = 178;
    public static final int MEDIA_RECORDER_START = 177;
    public static final int MIME_TYPE = 127;
    public static final int MOBILE_RADIO_ACTIVE_STATE = 191;
    public static final int MUSIC_AUDIO_PLAY = 140;
    public static final int NATIVE_ACTIVITY_CREATED = 186;
    public static final int NATIVE_ACTIVITY_DESTROYED = 187;
    public static final int NOTIFICATION_CANCEL = 123;
    public static final int NOTIFICATION_CANCEL_ALL = 124;
    public static final int NOTIFICATION_ENQUEUE = 122;
    public static final int NOTIFICATION_UPDATE = 195;
    public static final int PAY_BEGIN = 214;
    public static final int PC_WEBVIEW_END = 150;
    public static final int PC_WEBVIEW_START = 149;
    public static final int REMOVE_PROCESS_DEPENDENCY = 167;
    public static final int REMOVE_VIEW = 152;
    public static final int RESET_BATTERY_STATS = 190;
    public static final int SCALE_END = 210;
    public static final int SCREEN_OFF = 116;
    public static final int SCREEN_SHOT_END = 176;
    public static final int SCREEN_SHOT_START = 175;
    public static final int SINGLE_TAP = 208;
    public static final int SPEED_UP_END = 180;
    public static final int SPEED_UP_START = 179;
    public static final int START_CAMERA = 129;
    public static final int START_CHG_ROTATION = 128;
    public static final int START_WEBKIT_CANVAS = 131;
    public static final int SURFACEVIEW_CREATED = 141;
    public static final int SURFACEVIEW_DESTROYED = 142;
    public static final int SYSTEMUI_PANEL_FULLY_OPEN = 197;
    public static final int TEST_FOR_CHANNEL = 100000;
    public static final int TEXTUREVIEW_CREATED = 183;
    public static final int TEXTUREVIEW_DESTROYED = 184;
    public static final int THERMAL_LAUNCH = 145;
    public static final int TOUCH_DOWN = 125;
    public static final int TOUCH_UP = 126;
    public static final int TRASH_WAKELOCK = 133;
    public static final int VIDEO_END = 137;
    public static final int VIDEO_RECORD_START = 204;
    public static final int VIDEO_RECORD_STOP = 205;
    public static final int VIDEO_START = 136;
    public static final int WAKELOCK_ACQUIRED = 160;
    public static final int WAKELOCK_RELEASED = 161;
    public static final int WEBPAGE_FINISHED = 105;
    public static final int WEBPAGE_STARTED = 104;
    public static final int WEBVIEW_PAUSED = 103;
    public static final int WEBVIEW_RESUMED = 102;
    public static final int WIDGET_UPDATE = 200;
    public static final int WIFI_PKT_FILTER_ABNORMAL = 228;
    public static final int WIFI_SCAN_END = 159;
    public static final int WIFI_SCAN_START = 158;
    private static final HashMap<String, String> mLastNotifyPkg = new HashMap<>(2);
    private static final HashMap<String, Long> mLastNotifyTime = new HashMap<>(2);
    private static StringBuffer mMsgBuffer = new StringBuffer(256);

    protected LogPower() {
    }

    public static int push(int tag) {
        return printlnPower(tag, "", null, null, null);
    }

    public static int push(int tag, String packageName) {
        return printlnPower(tag, packageName, null, null, null);
    }

    public static int push(int tag, String packageName, String value) {
        return printlnPower(tag, packageName, value, null, null);
    }

    public static int push(int tag, String packageName, String value, String className) {
        return printlnPower(tag, packageName, value, className, null);
    }

    public static int push(int tag, String packageName, String value, String className, String[] extend) {
        return printlnPower(tag, packageName, value, className, extend);
    }

    private static int printlnPower(int tag, String packageName, String value, String className, String[] extend) {
        String msg;
        synchronized (mMsgBuffer) {
            if (packageName != null) {
                try {
                    mMsgBuffer.append(packageName);
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (value != null) {
                StringBuffer stringBuffer = mMsgBuffer;
                stringBuffer.append("|");
                stringBuffer.append(value);
            }
            if (className != null) {
                if (value == null) {
                    mMsgBuffer.append("|");
                }
                StringBuffer stringBuffer2 = mMsgBuffer;
                stringBuffer2.append("|");
                stringBuffer2.append(className);
            }
            if (extend != null) {
                for (String str : extend) {
                    StringBuffer stringBuffer3 = mMsgBuffer;
                    stringBuffer3.append("|");
                    stringBuffer3.append(str);
                }
            }
            msg = mMsgBuffer.toString();
            mMsgBuffer.delete(0, mMsgBuffer.length());
        }
        return Log.print_powerlog_native(6, Integer.toString(tag), msg);
    }

    protected static int pushIAware(int tag, String msg) {
        if (tag < 2000 || tag > 2999) {
            return -1;
        }
        return Log.print_powerlog_native(6, Integer.toString(tag), msg);
    }

    public static void notifyAction(int uid, String reason, String pkg, String pid, String fromPackage) {
        if ("acquire_provider".equals(reason) || "bindservice".equals(reason)) {
            String lastPkg = mLastNotifyPkg.get(reason);
            Long lastTime = mLastNotifyTime.get(reason);
            long now = SystemClock.elapsedRealtime();
            if (lastPkg == null || lastTime == null || !lastPkg.equals(pkg) || now - lastTime.longValue() >= 5000) {
                mLastNotifyPkg.put(reason, pkg);
                mLastNotifyTime.put(reason, Long.valueOf(now));
            } else {
                return;
            }
        }
        if (fromPackage == null) {
            push(148, reason, pkg, pid);
        } else {
            push(148, reason, pkg, pid, new String[]{fromPackage});
        }
    }

    public static void handleTimeOut(String reason, String pkg, String pid) {
        push(148, reason, pkg, pid);
    }
}
