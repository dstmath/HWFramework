package ohos.multimodalinput.eventimpl;

import android.view.MotionEvent;
import ohos.multimodalinput.event.RotationEvent;

public class RotationEventImpl extends RotationEvent {
    static final int SOURCE_ROTATION = 4194304;
    private MotionEvent motionEvent;

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public String getDeviceId() {
        return "";
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public int getSourceDevice() {
        return 5;
    }

    RotationEventImpl(MotionEvent motionEvent2) {
        this.motionEvent = motionEvent2;
    }

    @Override // ohos.multimodalinput.event.RotationEvent
    public float getRotationValue() {
        return this.motionEvent.getAxisValue(26);
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public int getInputDeviceId() {
        return this.motionEvent.getDeviceId();
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public long getOccurredTime() {
        return this.motionEvent.getEventTime();
    }
}
