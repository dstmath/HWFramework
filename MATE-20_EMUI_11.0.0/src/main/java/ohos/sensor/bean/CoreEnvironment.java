package ohos.sensor.bean;

import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;

public final class CoreEnvironment extends SensorBean {
    public CoreEnvironment() {
        this(0, null, null, 0, ConstantValue.MIN_ZOOM_VALUE, ConstantValue.MIN_ZOOM_VALUE, 0, 0, 0, 0);
    }

    public CoreEnvironment(int i, String str, String str2, int i2, float f, float f2, int i3, int i4, long j, long j2) {
        super(i, str, str2, i2, f, f2, i3, i4, j, j2);
    }
}
