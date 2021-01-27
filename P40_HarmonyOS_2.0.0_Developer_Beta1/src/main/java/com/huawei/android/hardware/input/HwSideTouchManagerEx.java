package com.huawei.android.hardware.input;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.huawei.android.hardware.input.HwInputManager;
import com.huawei.android.view.HwExtDisplaySizeUtil;
import com.huawei.annotation.HwSystemApi;
import java.util.ArrayList;
import java.util.List;

public class HwSideTouchManagerEx {
    @HwSystemApi
    public static final String COMMAND_EXPOSE_SIDE_AREA = "THP_Ring_ExposeSideArea";
    @HwSystemApi
    public static final String COMMAND_GET_TUNE_TIME_PRMT = "THP_GetTuneTimePrmt";
    @HwSystemApi
    public static final String COMMAND_MAP_SIDE_AREA = "THP_Ring_MapSideArea";
    @HwSystemApi
    public static final String COMMAND_REMOVE_SIDE_AREA = "THP_Ring_RemoveSideArea";
    @HwSystemApi
    public static final String COMMAND_RESET_SIDE_AREA = "THP_Ring_ResetSideGesture";
    @HwSystemApi
    public static final String COMMAND_SET_TUNE_TIME_PRMT = "THP_SetTuneTimePrmt";
    @HwSystemApi
    public static final String COMMAND_SET_VOLUME_GUI_INFO = "THP_Ring_SetVolumeGUIInfo";
    @HwSystemApi
    public static final String COMMAND_VOLUME_GET_STATE = "THP_GetState";
    @HwSystemApi
    public static final int DEFAULT_VOLUME_MODE = 2248;
    @HwSystemApi
    public static final String ENABLE = "enable";
    @HwSystemApi
    public static final String EXTRA_INFO = "extraInfo";
    public static final String EXTRA_INFO_KEY = "extra_info_key";
    public static final int EXTRA_INFO_VOLUME_PANEL_CNT = 1;
    @HwSystemApi
    public static final int FLAG_VOLUME_TRIGGER = 4096;
    @HwSystemApi
    public static final String FROM_RECT = "fromRect";
    @HwSystemApi
    public static final String GUI_STATE = "guiState";
    private static final int INFO_ARRAY_SIZE = 7;
    private static final int INFO_REGION_NUM = 2;
    public static final String KEY_SIDE_POSITION = "volume_side_slide_position";
    public static final String KEY_VOLUME_MODE = "volume_side_slide_mode";
    public static final int REGION_TYPE_GREEN = 0;
    public static final int REGION_TYPE_HOT = 1;
    @HwSystemApi
    public static final String SEPARATE = "#";
    public static final int SIDE_TOUCH_BOTH = 3;
    public static final int SIDE_TOUCH_DISABLE = 0;
    public static final int SIDE_TOUCH_HIGH_TRIGGER_SENSITIVITY = 1;
    public static final int SIDE_TOUCH_LEFT = 1;
    public static final int SIDE_TOUCH_LOW_TRIGGER_SENSITIVITY = 3;
    public static final int SIDE_TOUCH_MEDIUM_TRIGGER_SENSITIVITY = 2;
    public static final int SIDE_TOUCH_NONE = 0;
    public static final int SIDE_TOUCH_RIGHT = 2;
    @HwSystemApi
    public static final String SIDE_TOUCH_TRIGGER_SENSITIVITY = "triggerSensitivity";
    public static final int SIDE_TOUCH_WITHOUT_SOLID = 1;
    public static final int SIDE_TOUCH_WITH_SOLID = 2;
    private static final String TAG = "HwSideTouchManagerEx";
    @HwSystemApi
    public static final String TOUCH_REGION_RECT = "rect";
    @HwSystemApi
    public static final String TOUCH_REGION_TYPE = "type";
    @HwSystemApi
    public static final String TO_RECT = "toRect";
    public static final int TSA_EVENT_LEFT_VOLUME_INDICATOR_OFF = 262144;
    public static final int TSA_EVENT_LEFT_VOLUME_TOUCH = 65536;
    public static final int TSA_EVENT_LEFT_VOLUME_TRIGGER = 1024;
    public static final int TSA_EVENT_NONE = 0;
    public static final int TSA_EVENT_PHONE_HELD_ON_LEFT_HAND = 2;
    public static final int TSA_EVENT_PHONE_HELD_ON_RIGHT_HAND = 4;
    public static final int TSA_EVENT_PHONE_OUTOF_WATER = 64;
    public static final int TSA_EVENT_PHONE_UNDER_WATER = 32;
    public static final int TSA_EVENT_POWER_DOUBLE_CLICK = 32768;
    public static final int TSA_EVENT_PROXIMITY_DETECTED = 128;
    public static final int TSA_EVENT_PROXIMITY_PICK_UP_GESTURE_DETECTED = 256;
    public static final int TSA_EVENT_PROXIMITY_PUT_DOWN_GESTURE_DETECTED = 512;
    public static final int TSA_EVENT_RIGHT_VOLUME_INDICATOR_OFF = 524288;
    public static final int TSA_EVENT_RIGHT_VOLUME_TOUCH = 131072;
    public static final int TSA_EVENT_RIGHT_VOLUME_TRIGGER = 2048;
    public static final int TSA_EVENT_THENAR_LONG_PRESS_ON_LEFT = 8;
    public static final int TSA_EVENT_THENAR_LONG_PRESS_ON_RIGHT = 16;
    public static final int TSA_EVENT_VOLUME_DOWN = 8192;
    public static final int TSA_EVENT_VOLUME_MUTE = 16384;
    public static final int TSA_EVENT_VOLUME_TRIGGER_OFF = 1048576;
    public static final int TSA_EVENT_VOLUME_UP = 4096;
    @HwSystemApi
    public static final int TYPE_ENABLE_SIDE_TOUCH = 6;
    @HwSystemApi
    public static final int TYPE_GET_EXTRA_INFO = 12;
    @HwSystemApi
    public static final int TYPE_GET_SIDE_TOUCH_REGION = 3;
    @HwSystemApi
    public static final int TYPE_REGISTER_SIDE_TOUCH_LISTENER = 7;
    @HwSystemApi
    public static final int TYPE_SET_SIDE_POSITION = 8;
    @HwSystemApi
    public static final int TYPE_SET_SIDE_TOUCHREGION = 2;
    @HwSystemApi
    public static final int TYPE_SET_SIDE_TOUCH_GET_STATE = 11;
    @HwSystemApi
    public static final int TYPE_SET_SIDE_TOUCH_MAPPING = 5;
    @HwSystemApi
    public static final int TYPE_SET_SIDE_TOUCH_TRIGGER_SENSITIVITY = 10;
    @HwSystemApi
    public static final int TYPE_SET_VOLUME_PANEL_INFO = 1;
    @HwSystemApi
    public static final int TYPE_SIDE_TOUCH_GET_VOLUME_MODE = 9;
    @HwSystemApi
    public static final int TYPE_SIDE_TOUCH_VOLUME_MODE = 4;
    @HwSystemApi
    public static final String VOLUME_ACTIVE_AREA_END = "volumeActiveAreaEnd";
    @HwSystemApi
    public static final String VOLUME_ACTIVE_AREA_START = "volumeActiveAreaStart";
    @HwSystemApi
    public static final String VOLUME_FLICK_DOWN = "volumeFlickDown";
    @HwSystemApi
    public static final int VOLUME_FLICK_THRESHOLD_MAX = 150;
    @HwSystemApi
    public static final int VOLUME_FLICK_THRESHOLD_MIN = 75;
    @HwSystemApi
    public static final String VOLUME_FLICK_UP = "volumeFlickUp";
    @HwSystemApi
    public static final String VOLUME_GESTURE = "volumeGesture";
    @HwSystemApi
    public static final String VOLUME_GREEN_AREA_END = "volumeGreenAreaEnd";
    @HwSystemApi
    public static final String VOLUME_GREEN_AREA_START = "volumeGreenAreaStart";
    public static final int VOLUME_MODE_DISABLE = 0;
    public static final int VOLUME_MODE_DOUBLE_CLICK_MAPPING = 2248;
    public static final int VOLUME_MODE_DOUBLE_CLICK_STEP = 2184;
    public static final int VOLUME_MODE_DOUBLE_SLIDE_MAPPING = 204;
    public static final int VOLUME_MODE_DOUBLE_SLIDE_STEP = 140;
    public static final int VOLUME_MODE_MAPPING = 202;
    public static final int VOLUME_MODE_SINGLE_CLICK_MAPPING = 4296;
    public static final int VOLUME_MODE_SINGLE_CLICK_STEP = 4232;
    public static final int VOLUME_MODE_STEP = 138;
    @HwSystemApi
    public static final int VOLUME_PANEL_STATUS_VISIBLE = 1;
    @HwSystemApi
    public static final String VOLUME_SIED_ENABLED = "volumeSideEnabled";
    @HwSystemApi
    public static final String VOLUME_TOUCH_MIN = "volumeTouchMin";
    @HwSystemApi
    public static final int VOLUME_TOUCH_MIN_THRESHOLD_MAX = 0;
    @HwSystemApi
    public static final int VOLUME_TOUCH_MIN_THRESHOLD_MIN = -32768;
    @HwSystemApi
    public static final String X_END_POS = "xEndPos";
    @HwSystemApi
    public static final String X_MAP_TO = "xMapTo";
    @HwSystemApi
    public static final String X_START_POS = "xStartPos";
    @HwSystemApi
    public static final String Y_END_POS = "yEndPos";
    @HwSystemApi
    public static final String Y_MAP_TO = "yMapTo";
    @HwSystemApi
    public static final String Y_START_POS = "yStartPos";
    private static HwSideTouchManagerEx mInstance = null;
    private List<HwExtEventListener> listeners = new ArrayList();
    HwInputManager.HwTHPEventListener mEventListener = new HwInputManager.HwTHPEventListener() {
        /* class com.huawei.android.hardware.input.HwSideTouchManagerEx.AnonymousClass1 */

        public void onHwTHPEvent(int event) {
            synchronized (HwSideTouchManagerEx.this.mLock) {
                for (HwExtEventListener listener : HwSideTouchManagerEx.this.listeners) {
                    if (listener != null) {
                        listener.onHwSideEvent(event);
                    }
                }
            }
        }

        public void onHwTpEvent(int eventClass, int eventCode, String extraInfo) {
            synchronized (HwSideTouchManagerEx.this.mLock) {
                for (HwExtEventListener listener : HwSideTouchManagerEx.this.listeners) {
                    if (listener != null) {
                        listener.onHwTpEvent(eventClass, eventCode, extraInfo);
                    }
                }
            }
        }
    };
    private final Object mLock = new Object();

    private HwSideTouchManagerEx() {
    }

    public static synchronized HwSideTouchManagerEx getInstance() {
        HwSideTouchManagerEx hwSideTouchManagerEx;
        synchronized (HwSideTouchManagerEx.class) {
            if (mInstance == null) {
                mInstance = new HwSideTouchManagerEx();
            }
            hwSideTouchManagerEx = mInstance;
        }
        return hwSideTouchManagerEx;
    }

    public static void setVolumePanelInfo(Context context, int[] volumePanelInfo) {
        if (context == null) {
            Log.e(TAG, "setVolumePanelInfo, context is null");
        } else if (volumePanelInfo == null) {
            Log.e(TAG, "volumePanelInfo is null");
        } else if (volumePanelInfo.length < 7) {
            Log.e(TAG, "setVolumePanelInfo, volumePanelInfo is invalid");
        } else if (volumePanelInfo[1] == 0 && volumePanelInfo[2] == 0 && volumePanelInfo[3] == 0 && volumePanelInfo[4] == 0) {
            Log.e(TAG, "setVolumePanelInfo, volumePanelInfo is invalid!");
        } else {
            Log.i(TAG, "setVolumePanelInfo:" + context.getPackageName());
            if (volumePanelInfo.length == 7) {
                Bundle bundle = new Bundle();
                bundle.putInt(GUI_STATE, volumePanelInfo[0]);
                bundle.putInt(X_START_POS, volumePanelInfo[1]);
                bundle.putInt(X_END_POS, volumePanelInfo[2]);
                bundle.putInt(Y_START_POS, volumePanelInfo[3]);
                bundle.putInt(Y_END_POS, volumePanelInfo[4]);
                bundle.putInt(X_MAP_TO, volumePanelInfo[5]);
                bundle.putInt(Y_MAP_TO, volumePanelInfo[6]);
                HwInputManager.runSideTouchCommandByType(1, bundle);
            }
        }
    }

    public static void setSideTouchRegion(Context context, int type, Rect rect) {
        if (context == null) {
            Log.e(TAG, "setSideTouchRegion, context is null");
            return;
        }
        Log.i(TAG, "setSideTouchRegion:" + context.getPackageName() + " Rect info:" + rect.toString());
        Bundle bundle = new Bundle();
        bundle.putInt(TOUCH_REGION_TYPE, type);
        bundle.putParcelable(TOUCH_REGION_RECT, rect);
        HwInputManager.runSideTouchCommandByType(2, bundle);
    }

    public static Rect getSideTouchRegion(Context context, int type) {
        if (context == null) {
            Log.e(TAG, "getSideTouchRegion, context is null");
            return null;
        }
        Log.i(TAG, "getSideTouchRegion:" + context.getPackageName() + "type:" + type);
        Bundle bundle = new Bundle();
        Rect rect = new Rect();
        bundle.putInt(TOUCH_REGION_TYPE, type);
        int[] result = HwInputManager.runSideTouchCommandByType(3, bundle);
        if (result == null || result.length != 2) {
            Log.e(TAG, "getSideTouchRegion result error");
            return rect;
        }
        rect.top = result[0];
        rect.bottom = result[1];
        return rect;
    }

    public static void setSideTouchVolumeMode(int mode) {
        Log.i(TAG, "setSideTouchVolumeMode:" + mode);
        Bundle bundle = new Bundle();
        bundle.putInt(VOLUME_GESTURE, mode);
        HwInputManager.runSideTouchCommandByType(4, bundle);
    }

    public static int getSideTouchVolumeMode() {
        Log.i(TAG, "getSideTouchVolumeMode:");
        int[] volumeModes = HwInputManager.runSideTouchCommandByType(9, (Bundle) null);
        if (volumeModes != null) {
            Log.i(TAG, "getSideTouchVolumeMode is " + volumeModes[0]);
            return volumeModes[0];
        }
        Log.e(TAG, "getSideTouchVolumeMode error , return mapping as default");
        return 2248;
    }

    public static void setSideTouchMapping(Context context, Rect fromRect, Rect toRect) {
        if (context == null) {
            Log.e(TAG, "setSideTouchMapping, param is null");
        } else if (fromRect == null || toRect == null) {
            Log.e(TAG, "setSideTouchMapping, rect is null");
        } else if (fromRect.equals(toRect)) {
            Log.w(TAG, "setSideTouchMapping, fromRect and toRect are same");
        } else {
            Log.i(TAG, "setSideTouchMapping:" + context.getPackageName() + "fromRect:" + fromRect.toString() + "toRect:" + toRect.toString());
            Bundle bundle = new Bundle();
            bundle.putParcelable(FROM_RECT, fromRect);
            bundle.putParcelable(TO_RECT, toRect);
            HwInputManager.runSideTouchCommandByType(5, bundle);
        }
    }

    public static void enableSideTouch(Context context, Rect rect, boolean isEnable) {
        if (context == null || rect == null) {
            Log.e(TAG, "enableSideTouch, param is null");
            return;
        }
        Log.i(TAG, "enableSideTouch: " + context.getPackageName() + "Rect info:" + rect.toString());
        Bundle bundle = new Bundle();
        bundle.putParcelable(TO_RECT, rect);
        bundle.putBoolean(ENABLE, isEnable);
        HwInputManager.runSideTouchCommandByType(6, bundle);
    }

    public static void configSideTouch(Context context, int sidePosition) {
        if (context == null) {
            Log.e(TAG, "configSideTouch, param is null");
            return;
        }
        Log.i(TAG, "configSideTouch: " + context.getPackageName() + "sidePosition:" + sidePosition);
        Bundle bundle = new Bundle();
        bundle.putInt(VOLUME_SIED_ENABLED, sidePosition);
        HwInputManager.runSideTouchCommandByType(8, bundle);
    }

    public static void setSideTouchTriggerSensitivity(Context context, int triggerSensitivity) {
        if (context == null) {
            Log.e(TAG, "setSideTouchTriggerSensitivity, param is null");
            return;
        }
        Log.i(TAG, "setSideTouchTriggerSensitivity: " + context.getPackageName() + " ,triggerSensitivity:" + triggerSensitivity);
        Bundle bundle = new Bundle();
        bundle.putInt(SIDE_TOUCH_TRIGGER_SENSITIVITY, triggerSensitivity);
        HwInputManager.runSideTouchCommandByType(10, bundle);
    }

    public void registerListener(Context context, HwExtEventListener listener) {
        if (context == null || listener == null) {
            Log.e(TAG, "registerListener, param is null");
            return;
        }
        synchronized (this.mLock) {
            this.listeners.add(listener);
        }
        Log.i(TAG, "HwExtTHPEventListener registerListener:" + context.getPackageName());
        HwInputManager.getInstance(context).registerListener(this.mEventListener, (Handler) null);
    }

    public void unregisterListener(Context context, HwExtEventListener listener) {
        if (context == null || listener == null) {
            Log.e(TAG, "unregisterListener, param is null");
            return;
        }
        synchronized (this.mLock) {
            this.listeners.remove(listener);
            if (this.listeners.size() == 0) {
                Log.i(TAG, "HwExtTHPEventListener unregisterListener:" + context.getPackageName());
                HwInputManager.getInstance(context).unregisterListener(this.mEventListener);
            }
        }
    }

    public Bundle getExtraInfo(int getType) {
        Log.i(TAG, "getExtraInfo  type is  " + getType);
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_INFO, getType);
        int[] extrainfo = HwInputManager.runSideTouchCommandByType(12, bundle);
        if (extrainfo != null) {
            Log.i(TAG, "getExtraInfo is " + extrainfo[0]);
            Bundle retData = new Bundle();
            retData.putInt(EXTRA_INFO_KEY, extrainfo[0]);
            return retData;
        }
        Log.e(TAG, "getExtraInfo error");
        return null;
    }

    public int getSideTouchMode() {
        HwExtDisplaySizeUtil util = HwExtDisplaySizeUtil.getInstance();
        if (util != null) {
            return util.getSideTouchMode();
        }
        return 0;
    }
}
