package com.android.server.policy;

import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.policy.IHWExtMotionRotationProcessor;
import com.huawei.hwextdevice.HWExtDeviceEvent;
import com.huawei.hwextdevice.HWExtDeviceEventListener;
import com.huawei.hwextdevice.HWExtDeviceManager;
import com.huawei.hwextdevice.devices.HWExtMotion;

public class HWExtMotionRotationProcessorEx implements IHWExtMotionRotationProcessor {
    private static final int MOTION_VALUES_LEN = 2;
    private HWExtDeviceEventListener mHWEDListener = new HWExtDeviceEventListener() {
        public void onDeviceDataChanged(HWExtDeviceEvent hwextDeviceEvent) {
            float[] deviceValues = hwextDeviceEvent.getDeviceValues();
            if (deviceValues == null) {
                Log.e("HWEMRP", "onDeviceDataChanged  deviceValues is null ");
            } else if (deviceValues.length >= 2) {
                int proposedRotation = (int) deviceValues[1];
                Log.d("HWEMRP", "onDeviceDataChanged  proposedRotation:" + proposedRotation);
                int oldProposedRotation = HWExtMotionRotationProcessorEx.this.mProposedRotation;
                Log.d("HWEMRP", "onDeviceDataChanged  oldProposedRotation:" + oldProposedRotation);
                int unused = HWExtMotionRotationProcessorEx.this.mProposedRotation = proposedRotation;
                if (proposedRotation != oldProposedRotation && proposedRotation >= 0) {
                    Log.d("HWEMRP", "notifyProposedRotation: " + HWExtMotionRotationProcessorEx.this.mProposedRotation);
                    HWExtMotionRotationProcessorEx.this.mWOLPxy.setCurrentOrientation(HWExtMotionRotationProcessorEx.this.mProposedRotation);
                    HWExtMotionRotationProcessorEx.this.mWOLPxy.notifyProposedRotation(HWExtMotionRotationProcessorEx.this.mProposedRotation);
                }
            }
        }
    };
    private HWExtDeviceManager mHWEDManager = null;
    private HWExtMotion mHWExtMotion = null;
    /* access modifiers changed from: private */
    public int mProposedRotation = (SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90);
    /* access modifiers changed from: private */
    public IHWExtMotionRotationProcessor.WindowOrientationListenerProxy mWOLPxy = null;

    public HWExtMotionRotationProcessorEx(IHWExtMotionRotationProcessor.WindowOrientationListenerProxy wolPxy) {
        this.mWOLPxy = wolPxy;
        initProcessor();
    }

    public void enableMotionRotation(Handler handler) {
        if (this.mHWEDManager != null) {
            this.mHWEDManager.registerDeviceListener(this.mHWEDListener, this.mHWExtMotion, handler);
            Log.d("HWEMRP", "registerDeviceListener  mHWEDListener");
            return;
        }
        Log.e("HWEMRP", "enableMotionRotation  mHWEDManager is null ");
    }

    public void disableMotionRotation() {
        if (this.mHWEDManager != null) {
            this.mHWEDManager.unregisterDeviceListener(this.mHWEDListener, this.mHWExtMotion);
        } else {
            Log.e("HWEMRP", "disableMotionRotation  mHWEDManager is null ");
        }
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
                Log.e("HWEMRP", "destroy  error : " + e);
            }
        }
    }

    private void initProcessor() {
        this.mHWEDManager = HWExtDeviceManager.getInstance(null);
        this.mHWExtMotion = new HWExtMotion(700);
    }
}
