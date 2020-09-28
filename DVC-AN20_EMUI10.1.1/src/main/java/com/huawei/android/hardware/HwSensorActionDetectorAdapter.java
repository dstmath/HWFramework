package com.huawei.android.hardware;

import com.huawei.android.hardware.HwSensorManager;

public class HwSensorActionDetectorAdapter implements HwSensorManager.SensorEventDetector {
    @Override // com.huawei.android.hardware.HwSensorManager.SensorEventDetector
    public void onDirectionChanged(int direction) {
    }

    @Override // com.huawei.android.hardware.HwSensorManager.SensorEventDetector
    public void onTiltToMove(float dx, float dy) {
    }

    @Override // com.huawei.android.hardware.HwSensorManager.SensorEventDetector
    public void onCorrect() {
    }

    @Override // com.huawei.android.hardware.HwSensorManager.SensorEventDetector
    public void onSwing() {
    }
}
