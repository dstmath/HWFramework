package com.android.server.wm;

import android.os.Looper;
import android.util.Slog;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.MotionEvent;

class DragInputEventReceiver extends InputEventReceiver {
    private final DragDropController mDragDropController;
    private boolean mIsStartEvent = true;
    private boolean mMuteInput = false;
    private boolean mStylusButtonDownAtStart;

    DragInputEventReceiver(InputChannel inputChannel, Looper looper, DragDropController controller) {
        super(inputChannel, looper);
        this.mDragDropController = controller;
    }

    public void onInputEvent(InputEvent event, int displayId) {
        boolean handled = false;
        try {
            if ((event instanceof MotionEvent) && (event.getSource() & 2) != 0) {
                if (!this.mMuteInput) {
                    MotionEvent motionEvent = (MotionEvent) event;
                    float newX = motionEvent.getRawX();
                    float newY = motionEvent.getRawY();
                    boolean isStylusButtonDown = (motionEvent.getButtonState() & 32) != 0;
                    if (this.mIsStartEvent) {
                        this.mStylusButtonDownAtStart = isStylusButtonDown;
                        this.mIsStartEvent = false;
                    }
                    switch (motionEvent.getAction()) {
                        case 0:
                            finishInputEvent(event, false);
                            return;
                        case 1:
                            this.mMuteInput = true;
                            break;
                        case 2:
                            if (this.mStylusButtonDownAtStart && !isStylusButtonDown) {
                                this.mMuteInput = true;
                                break;
                            }
                        case 3:
                            this.mMuteInput = true;
                            break;
                        default:
                            finishInputEvent(event, false);
                            return;
                    }
                    this.mDragDropController.handleMotionEvent(true ^ this.mMuteInput, newX, newY);
                    handled = true;
                    finishInputEvent(event, handled);
                    return;
                }
            }
            finishInputEvent(event, false);
        } catch (Exception e) {
            Slog.e("WindowManager", "Exception caught by drag handleMotion", e);
        } catch (Throwable th) {
            finishInputEvent(event, false);
            throw th;
        }
    }
}
