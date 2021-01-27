package com.android.server.policy;

import android.content.Context;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.am.HwActivityManagerService;
import com.android.server.policy.IHWExtMotionRotationProcessor;
import com.huawei.hwextdevice.HWExtDeviceEvent;
import com.huawei.hwextdevice.HWExtDeviceEventListener;
import com.huawei.hwextdevice.HWExtDeviceManager;
import com.huawei.hwextdevice.devices.HWExtMotion;

public class HWExtMotionRotationProcessorEx implements IHWExtMotionRotationProcessor {
    private static final String LOG_TAG = "HWEMRP";
    private static final int MOTION_VALUES_LEN = 2;
    private HWExtDeviceEventListener mHWEDListener = new HWExtDeviceEventListener() {
        /* class com.android.server.policy.HWExtMotionRotationProcessorEx.AnonymousClass1 */

        public void onDeviceDataChanged(HWExtDeviceEvent hwextDeviceEvent) {
            float[] deviceValues = hwextDeviceEvent.getDeviceValues();
            if (deviceValues == null) {
                Log.e(HWExtMotionRotationProcessorEx.LOG_TAG, "onDeviceDataChanged deviceValues is null");
            } else if (deviceValues.length >= 2) {
                int proposedRotation = (int) deviceValues[1];
                Log.i(HWExtMotionRotationProcessorEx.LOG_TAG, "onDeviceDataChanged proposedRotation:" + proposedRotation);
                int oldProposedRotation = HWExtMotionRotationProcessorEx.this.mProposedRotation;
                Log.i(HWExtMotionRotationProcessorEx.LOG_TAG, "onDeviceDataChanged oldProposedRotation:" + oldProposedRotation);
                HWExtMotionRotationProcessorEx.this.mProposedRotation = proposedRotation;
                if (proposedRotation != oldProposedRotation && proposedRotation >= 0) {
                    Log.i(HWExtMotionRotationProcessorEx.LOG_TAG, "notifyProposedRotation: " + HWExtMotionRotationProcessorEx.this.mProposedRotation);
                    HWExtMotionRotationProcessorEx.this.mWOLPxy.setCurrentOrientation(HWExtMotionRotationProcessorEx.this.mProposedRotation);
                    HWExtMotionRotationProcessorEx.this.mWOLPxy.notifyProposedRotation(HWExtMotionRotationProcessorEx.this.mProposedRotation);
                }
            }
        }
    };
    private HWExtDeviceManager mHWEDManager = null;
    private HWExtMotion mHWExtMotion = null;
    private int mProposedRotation = (SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90);
    private IHWExtMotionRotationProcessor.WindowOrientationListenerProxy mWOLPxy = null;

    public HWExtMotionRotationProcessorEx(IHWExtMotionRotationProcessor.WindowOrientationListenerProxy wolPxy) {
        this.mWOLPxy = wolPxy;
        initProcessor();
    }

    public void enableMotionRotation(Handler handler) {
        HWExtDeviceManager hWExtDeviceManager = this.mHWEDManager;
        if (hWExtDeviceManager != null) {
            hWExtDeviceManager.registerDeviceListener(this.mHWEDListener, this.mHWExtMotion, handler);
            Log.i(LOG_TAG, "registerDeviceListener mHWEDListener");
            return;
        }
        Log.e(LOG_TAG, "enableMotionRotation mHWEDManager is null");
    }

    public void disableMotionRotation() {
        HWExtDeviceManager hWExtDeviceManager = this.mHWEDManager;
        if (hWExtDeviceManager != null) {
            hWExtDeviceManager.unregisterDeviceListener(this.mHWEDListener, this.mHWExtMotion);
            Log.i(LOG_TAG, "unregisterDeviceListener mHWEDListener");
            return;
        }
        Log.e(LOG_TAG, "disableMotionRotation mHWEDManager is null");
    }

    public int getProposedRotation() {
        return this.mProposedRotation;
    }

    private void destroy() {
        if (this.mHWEDManager != null) {
            if (this.mHWExtMotion == null) {
                this.mHWExtMotion = null;
            }
            try {
                this.mHWEDManager.dispose();
                this.mHWEDManager = null;
            } catch (Exception e) {
                Log.e(LOG_TAG, "destroy error");
            }
        }
    }

    private void initProcessor() {
        this.mHWEDManager = HWExtDeviceManager.getInstance((Context) null);
        this.mHWExtMotion = new HWExtMotion((int) HwActivityManagerService.PREVIOUS_APP_ADJ);
    }
}
