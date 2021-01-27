package ohos.agp.components.events;

import ohos.multimodalinput.event.RotationEvent;

public final class RotationEventImpl extends RotationEvent {
    private static final String UNKNOWN_DEVICE_ID = "unknown_device";
    private final String mDeviceId;
    private final int mInputDeviceId;
    private final long mOccurredTime;
    private final float mRotationValue;
    private final int mSourceDevice;

    private RotationEventImpl(float f) {
        this(f, 0, UNKNOWN_DEVICE_ID, 0, 0);
    }

    private RotationEventImpl(float f, int i, String str, int i2, long j) {
        this.mRotationValue = f;
        this.mSourceDevice = i;
        this.mDeviceId = str;
        this.mInputDeviceId = i2;
        this.mOccurredTime = j;
    }

    public static RotationEvent obtain(float f) {
        return new RotationEventImpl(f);
    }

    public float getRotationValue() {
        return this.mRotationValue;
    }

    public int getSourceDevice() {
        return this.mSourceDevice;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public int getInputDeviceId() {
        return this.mInputDeviceId;
    }

    public long getOccurredTime() {
        return this.mOccurredTime;
    }
}
