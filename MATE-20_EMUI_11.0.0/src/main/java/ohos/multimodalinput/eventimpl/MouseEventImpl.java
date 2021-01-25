package ohos.multimodalinput.eventimpl;

import android.view.MotionEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;
import ohos.multimodalinput.event.MmiPoint;
import ohos.multimodalinput.event.MouseEvent;
import ohos.utils.Parcel;

public class MouseEventImpl extends MouseEvent {
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218114065, "MouseEventImpl");
    static final int SOURCE_MOUSE = 8194;
    private MotionEvent motionEvent;

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public String getDeviceId() {
        return "";
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent
    public int getSourceDevice() {
        return 2;
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent, ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        return false;
    }

    @Override // ohos.multimodalinput.event.MultimodalEvent, ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        return false;
    }

    MouseEventImpl(MotionEvent motionEvent2) {
        this.motionEvent = motionEvent2;
    }

    @Override // ohos.multimodalinput.event.MouseEvent
    public int getAction() {
        int action = this.motionEvent.getAction();
        if (action == 2) {
            return 3;
        }
        if (action == 7) {
            return 5;
        }
        switch (action) {
            case 9:
                return 4;
            case 10:
                return 6;
            case 11:
                return 1;
            case 12:
                return 2;
            default:
                return 0;
        }
    }

    @Override // ohos.multimodalinput.event.MouseEvent
    public int getActionButton() {
        int actionButton = this.motionEvent.getActionButton();
        int i = 1;
        if (actionButton != 1) {
            i = 2;
            if (actionButton != 2) {
                i = 4;
                if (actionButton != 4) {
                    i = 8;
                    if (actionButton != 8) {
                        i = 16;
                        if (actionButton != 16) {
                            return 0;
                        }
                    }
                }
            }
        }
        return i;
    }

    @Override // ohos.multimodalinput.event.MouseEvent
    public int getPressedButtons() {
        return this.motionEvent.getButtonState();
    }

    @Override // ohos.multimodalinput.event.MouseEvent
    public MmiPoint getCursor() {
        return new MmiPoint(this.motionEvent.getX(), this.motionEvent.getY());
    }

    @Override // ohos.multimodalinput.event.MouseEvent
    public void setCursorOffset(float f, float f2) {
        this.motionEvent.offsetLocation(f, f2);
    }

    @Override // ohos.multimodalinput.event.MouseEvent
    public float getCursorDelta(int i) {
        float y;
        float historicalY;
        int historySize = this.motionEvent.getHistorySize();
        if (historySize <= 0) {
            return ConstantValue.MIN_ZOOM_VALUE;
        }
        if (i == 0) {
            y = this.motionEvent.getX();
            historicalY = this.motionEvent.getHistoricalX(historySize - 1);
        } else if (i == 1) {
            y = this.motionEvent.getY();
            historicalY = this.motionEvent.getHistoricalY(historySize - 1);
        } else {
            HiLog.warn(LOG_LABEL, "unsupported axis: %{public}d", Integer.valueOf(i));
            return ConstantValue.MIN_ZOOM_VALUE;
        }
        return y - historicalY;
    }

    @Override // ohos.multimodalinput.event.MouseEvent
    public float getScrollingDelta(int i) {
        if (i == 0) {
            return this.motionEvent.getAxisValue(10);
        }
        if (i == 1) {
            return this.motionEvent.getAxisValue(9);
        }
        HiLog.warn(LOG_LABEL, "unsupported axis: %{public}d", Integer.valueOf(i));
        return ConstantValue.MIN_ZOOM_VALUE;
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
