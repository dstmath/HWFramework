package com.android.server.display;

import android.content.Context;
import android.os.Handler;
import com.huawei.android.hardware.input.HwInputManager;
import com.huawei.displayengine.DeLog;
import com.huawei.hwextdevice.HWExtDeviceEvent;
import com.huawei.hwextdevice.HWExtDeviceEventListener;
import com.huawei.hwextdevice.HWExtDeviceManager;
import com.huawei.hwextdevice.devices.HWExtMotion;
import com.huawei.msdp.devicestatus.DeviceStatusConstant;
import java.util.Date;

public class ThpListener {
    private static final int MOTION_VALUES_LEN = 2;
    private static final int SENSOR_GESTURE_DEVICE_FLOATING = 1;
    private static final int SENSOR_GESTURE_NONE = 0;
    private static final int SENSOR_GESTURE_PROXIMITY_PICK_UP = 2;
    private static final int SENSOR_GESTURE_PROXIMITY_PUT_DOWN = 4;
    private static final String SUPPORT_PROXIMITY_TYPE = "0006";
    private static final String TAG = "ThpListener";
    private final Context mContext;
    private int mEnabledSensor = 0;
    private HWExtDeviceManager mHWEDManager = null;
    private HWExtMotion mHWExtMotionPickup = null;
    private HWExtMotion mHWExtMotionPutdown = null;
    private HWExtDeviceEventListener mHWListenerMove = new HWExtDeviceEventListener() {
        /* class com.android.server.display.ThpListener.AnonymousClass1 */

        public void onDeviceDataChanged(HWExtDeviceEvent hwextDeviceEvent) {
            float[] deviceValues = hwextDeviceEvent.getDeviceValues();
            if (deviceValues == null) {
                DeLog.e(ThpListener.TAG, "onDeviceDataChanged pickup deviceValues is null ");
            } else if (deviceValues.length >= 2) {
                int gestureValue = ((int) deviceValues[1]) << 1;
                HwInputManager unused = ThpListener.this.mRunThpCommand;
                HwInputManager.runHwTHPCommand("THP_SetSensorGestureWithTimeStamp", Long.toString(new Date().getTime()) + "#" + String.format("%04X", Integer.valueOf(gestureValue)));
                DeLog.i(ThpListener.TAG, "the mGestureValue is " + gestureValue + ".");
            }
        }
    };
    private Handler mHandler;
    private HwInputManager mHwInputManager;
    private HwInputManager mRunThpCommand = null;

    public ThpListener(Context context) {
        DeLog.i(TAG, "start to init thp_listener.");
        this.mContext = context;
        Handler mHandler2 = new Handler();
        this.mHWEDManager = HWExtDeviceManager.getInstance(this.mContext);
        this.mRunThpCommand = HwInputManager.getInstance(this.mContext);
        if (this.mRunThpCommand != null) {
            String result = HwInputManager.runHwTHPCommand("THP_SetSupportedSensorGesture", SUPPORT_PROXIMITY_TYPE);
            DeLog.i(TAG, "THP_SetSupportedSensorGesture result is " + result + ".");
            if (result.equals("THPCommandNotSupported") || result.equals("InvalidCommand")) {
                DeLog.w(TAG, "this system do not support thp command");
                return;
            }
            HwInputManager hwInputManager = this.mRunThpCommand;
            String result2 = HwInputManager.runHwTHPCommand("THP_GetEnabledSensorGesture", "");
            if (result2 != null) {
                DeLog.i(TAG, "THP_GetEnabledSensorGesture result is " + result2 + ".");
                String[] strArray = result2.split("#");
                if (strArray != null) {
                    if (strArray.length > 1 && strArray[0].equals("OK")) {
                        try {
                            this.mEnabledSensor = Integer.parseInt(strArray[1]);
                            DeLog.i(TAG, "enable sensor listener is " + this.mEnabledSensor + ".");
                        } catch (NumberFormatException e) {
                            DeLog.e(TAG, "getInstance runthpcommand error: " + e);
                            return;
                        }
                    }
                    if (this.mHWEDManager != null) {
                        int i = this.mEnabledSensor;
                        if ((i & 2) > 0 || (i & 4) > 0) {
                            this.mHWExtMotionPickup = new HWExtMotion((int) DeviceStatusConstant.TYPE_TRIPLE_FINGER);
                            this.mHWEDManager.registerDeviceListener(this.mHWListenerMove, this.mHWExtMotionPickup, mHandler2);
                            DeLog.i(TAG, "register SENSOR_GESTURE_PROXIMITY_MOVE success.");
                            return;
                        }
                        return;
                    }
                    DeLog.e(TAG, "mHWEDManager is null, all register cannot run.");
                    return;
                }
                return;
            }
            return;
        }
        DeLog.e(TAG, "getInstance runthpcommand failed.");
    }
}
