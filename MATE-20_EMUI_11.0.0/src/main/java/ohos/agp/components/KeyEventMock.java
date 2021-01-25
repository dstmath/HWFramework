package ohos.agp.components;

import ohos.multimodalinput.event.KeyEvent;

public class KeyEventMock extends KeyEvent {
    public static final int ACTION_DOWN = 0;
    public static final int ACTION_UP = 1;
    private final int mAction;
    private final String mDeviceId;
    private final int mInputDeviceId;
    private final int mKeyCode;
    private final long mKeyDownDuration;
    private final long mOccurredTime;
    private final int mSourceDevice;

    private KeyEventMock(int i, int i2) {
        this(i, i2, 0, 0, "mock_device", 0, 0);
    }

    private KeyEventMock(int i, int i2, long j, int i3, String str, int i4, long j2) {
        this.mAction = i;
        this.mKeyCode = i2;
        this.mKeyDownDuration = j;
        this.mSourceDevice = i3;
        this.mDeviceId = str;
        this.mInputDeviceId = i4;
        this.mOccurredTime = j2;
    }

    public static KeyEvent obtain(int i, int i2) {
        return new KeyEventMock(i, i2);
    }

    public boolean isKeyDown() {
        return this.mAction == 0;
    }

    public int getKeyCode() {
        return this.mKeyCode;
    }

    public long getKeyDownDuration() {
        return this.mKeyDownDuration;
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
