package ohos.multimodalinput.eventimpl;

import android.view.MotionEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;
import ohos.multimodalinput.event.MmiPoint;
import ohos.multimodalinput.event.TouchEvent;
import ohos.utils.Parcel;

class TouchEventImpl extends TouchEvent {
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218114065, "TouchEventImpl");
    static final int SOURCE_TOUCHSCREEN = 4098;
    private MotionEvent motionEvent;

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public String getDeviceId() {
        return "";
    }

    @Override // ohos.multimodalinput.event.TouchEvent
    public float getForcePrecision() {
        return ConstantValue.MIN_ZOOM_VALUE;
    }

    @Override // ohos.multimodalinput.event.TouchEvent
    public float getMaxForce() {
        return ConstantValue.MIN_ZOOM_VALUE;
    }

    @Override // ohos.multimodalinput.event.TouchEvent
    public int getTapCount() {
        return 0;
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent, ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        return false;
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent, ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        return false;
    }

    TouchEventImpl(MotionEvent motionEvent2) {
        this.motionEvent = motionEvent2;
    }

    @Override // ohos.multimodalinput.event.TouchEvent
    public int getAction() {
        int actionMasked = this.motionEvent.getActionMasked();
        if (actionMasked == 0) {
            return 1;
        }
        if (actionMasked == 1) {
            return 2;
        }
        if (actionMasked == 2) {
            return 3;
        }
        if (actionMasked == 3) {
            return 6;
        }
        if (actionMasked == 5) {
            return 4;
        }
        if (actionMasked == 6) {
            return 5;
        }
        HiLog.error(LOG_LABEL, "unknown action: %{public}d", Integer.valueOf(actionMasked));
        return 0;
    }

    @Override // ohos.multimodalinput.event.TouchEvent
    public int getIndex() {
        return this.motionEvent.getActionIndex();
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public int getSourceDevice() {
        int source = this.motionEvent.getSource();
        if ((source & SOURCE_TOUCHSCREEN) == SOURCE_TOUCHSCREEN) {
            return 0;
        }
        HiLog.error(LOG_LABEL, "unknown source: %{public}d", Integer.valueOf(source));
        return -1;
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public int getInputDeviceId() {
        return this.motionEvent.getDeviceId();
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public long getOccurredTime() {
        return this.motionEvent.getEventTime();
    }

    @Override // ohos.multimodalinput.event.ManipulationEvent
    public long getStartTime() {
        return this.motionEvent.getDownTime();
    }

    @Override // ohos.multimodalinput.event.ManipulationEvent
    public int getPhase() {
        int action = getAction();
        switch (action) {
            case 1:
                return 1;
            case 2:
                return 3;
            case 3:
            case 4:
            case 5:
                return 2;
            case 6:
                return 4;
            default:
                HiLog.error(LOG_LABEL, "unknown phase action: %{public}d", Integer.valueOf(action));
                return 0;
        }
    }

    @Override // ohos.multimodalinput.event.ManipulationEvent
    public MmiPoint getPointerPosition(int i) {
        return new MmiPoint(this.motionEvent.getX(i), this.motionEvent.getY(i));
    }

    @Override // ohos.multimodalinput.event.ManipulationEvent
    public void setScreenOffset(float f, float f2) {
        this.motionEvent.offsetLocation(f, f2);
    }

    @Override // ohos.multimodalinput.event.ManipulationEvent
    public MmiPoint getPointerScreenPosition(int i) {
        return new MmiPoint(this.motionEvent.getRawX(i), this.motionEvent.getRawY(i));
    }

    @Override // ohos.multimodalinput.event.ManipulationEvent
    public int getPointerCount() {
        return this.motionEvent.getPointerCount();
    }

    @Override // ohos.multimodalinput.event.ManipulationEvent
    public int getPointerId(int i) {
        return this.motionEvent.getPointerId(i);
    }

    @Override // ohos.multimodalinput.event.ManipulationEvent
    public float getForce(int i) {
        return this.motionEvent.getPressure(i);
    }

    @Override // ohos.multimodalinput.event.ManipulationEvent
    public float getRadius(int i) {
        return this.motionEvent.getSize(i);
    }
}
