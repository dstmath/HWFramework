package com.huawei.android.hardware.input;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.huawei.android.hardware.input.HwInputManager;
import com.huawei.android.view.HwExtDisplaySizeUtil;
import java.util.ArrayList;
import java.util.List;

public class HwSideTouchManagerEx {
    public static final String EXTRA_INFO_KEY = "extra_info_key";
    public static final int EXTRA_INFO_VOLUME_PANEL_CNT = 1;
    private static final int INFO_ARRAY_SIZE = 7;
    private static final int INFO_REGION_NUM = 2;
    public static final String KEY_SIDE_POSITION = "volume_side_slide_position";
    public static final String KEY_VOLUME_MODE = "volume_side_slide_mode";
    public static final int REGION_TYPE_GREEN = 0;
    public static final int REGION_TYPE_HOT = 1;
    public static final int SIDE_TOUCH_BOTH = 3;
    public static final int SIDE_TOUCH_DISABLE = 0;
    public static final int SIDE_TOUCH_HIGH_TRIGGER_SENSITIVITY = 1;
    public static final int SIDE_TOUCH_LEFT = 1;
    public static final int SIDE_TOUCH_LOW_TRIGGER_SENSITIVITY = 3;
    public static final int SIDE_TOUCH_MEDIUM_TRIGGER_SENSITIVITY = 2;
    public static final int SIDE_TOUCH_NONE = 0;
    public static final int SIDE_TOUCH_RIGHT = 2;
    public static final int SIDE_TOUCH_WITHOUT_SOLID = 1;
    public static final int SIDE_TOUCH_WITH_SOLID = 2;
    private static final String TAG = "HwSideTouchManagerEx";
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
    public static final int VOLUME_MODE_DISABLE = 0;
    public static final int VOLUME_MODE_DOUBLE_CLICK_MAPPING = 2248;
    public static final int VOLUME_MODE_DOUBLE_CLICK_STEP = 2184;
    public static final int VOLUME_MODE_DOUBLE_SLIDE_MAPPING = 204;
    public static final int VOLUME_MODE_DOUBLE_SLIDE_STEP = 140;
    public static final int VOLUME_MODE_MAPPING = 202;
    public static final int VOLUME_MODE_SINGLE_CLICK_MAPPING = 4296;
    public static final int VOLUME_MODE_SINGLE_CLICK_STEP = 4232;
    public static final int VOLUME_MODE_STEP = 138;
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
                    Log.i(HwSideTouchManagerEx.TAG, "HwExtEventListener onHwSideEvent :" + event);
                }
            }
        }

        public void onHwTpEvent(int eventClass, int eventCode, String extraInfo) {
            synchronized (HwSideTouchManagerEx.this.mLock) {
                for (HwExtEventListener listener : HwSideTouchManagerEx.this.listeners) {
                    if (listener != null) {
                        listener.onHwTpEvent(eventClass, eventCode, extraInfo);
                    }
                    Log.i(HwSideTouchManagerEx.TAG, "HwExtEventListener onHwTpEvent eventClass =" + eventClass + ", eventCode =" + eventCode + ", extraInfo =" + extraInfo);
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
                bundle.putInt("guiState", volumePanelInfo[0]);
                bundle.putInt("xStartPos", volumePanelInfo[1]);
                bundle.putInt("xEndPos", volumePanelInfo[2]);
                bundle.putInt("yStartPos", volumePanelInfo[3]);
                bundle.putInt("yEndPos", volumePanelInfo[4]);
                bundle.putInt("xMapTo", volumePanelInfo[5]);
                bundle.putInt("yMapTo", volumePanelInfo[6]);
                HwInputManager.sendTPCommand(1, bundle);
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
        bundle.putInt("type", type);
        bundle.putParcelable("rect", rect);
        HwInputManager.sendTPCommand(2, bundle);
    }

    public static Rect getSideTouchRegion(Context context, int type) {
        if (context == null) {
            Log.e(TAG, "getSideTouchRegion, context is null");
            return null;
        }
        Log.i(TAG, "getSideTouchRegion:" + context.getPackageName() + "type:" + type);
        Bundle bundle = new Bundle();
        Rect rect = new Rect();
        bundle.putInt("type", type);
        int[] result = HwInputManager.sendTPCommand(3, bundle);
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
        bundle.putInt("volumeGesture", mode);
        HwInputManager.sendTPCommand(4, bundle);
    }

    public static int getSideTouchVolumeMode() {
        Log.i(TAG, "getSideTouchVolumeMode:");
        int[] volumeModes = HwInputManager.sendTPCommand(9, (Bundle) null);
        if (volumeModes != null) {
            Log.i(TAG, "getSideTouchVolumeMode is " + volumeModes[0]);
            return volumeModes[0];
        }
        Log.e(TAG, "getSideTouchVolumeMode error , return mapping as default");
        return VOLUME_MODE_DOUBLE_CLICK_MAPPING;
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
            bundle.putParcelable("fromRect", fromRect);
            bundle.putParcelable("toRect", toRect);
            HwInputManager.sendTPCommand(5, bundle);
        }
    }

    public static void enableSideTouch(Context context, Rect rect, boolean isEnable) {
        if (context == null || rect == null) {
            Log.e(TAG, "enableSideTouch, param is null");
            return;
        }
        Log.i(TAG, "enableSideTouch: " + context.getPackageName() + "Rect info:" + rect.toString());
        Bundle bundle = new Bundle();
        bundle.putParcelable("toRect", rect);
        bundle.putBoolean("enable", isEnable);
        HwInputManager.sendTPCommand(6, bundle);
    }

    public static void configSideTouch(Context context, int sidePosition) {
        if (context == null) {
            Log.e(TAG, "configSideTouch, param is null");
            return;
        }
        Log.i(TAG, "configSideTouch: " + context.getPackageName() + "sidePosition:" + sidePosition);
        Bundle bundle = new Bundle();
        bundle.putInt("volumeSideEnabled", sidePosition);
        HwInputManager.sendTPCommand(8, bundle);
    }

    public static void setSideTouchTriggerSensitivity(Context context, int triggerSensitivity) {
        if (context == null) {
            Log.e(TAG, "setSideTouchTriggerSensitivity, param is null");
            return;
        }
        Log.i(TAG, "setSideTouchTriggerSensitivity: " + context.getPackageName() + " ,triggerSensitivity:" + triggerSensitivity);
        Bundle bundle = new Bundle();
        bundle.putInt("triggerSensitivity", triggerSensitivity);
        HwInputManager.sendTPCommand(10, bundle);
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
        bundle.putInt("extraInfo", getType);
        int[] extrainfo = HwInputManager.sendTPCommand(12, bundle);
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
