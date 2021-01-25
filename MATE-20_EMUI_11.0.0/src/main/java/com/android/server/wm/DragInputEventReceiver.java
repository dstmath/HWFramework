package com.android.server.wm;

import android.os.Looper;
import android.util.Slog;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.MotionEvent;

/* access modifiers changed from: package-private */
public class DragInputEventReceiver extends InputEventReceiver {
    private final DragDropController mDragDropController;
    private boolean mIsStartEvent = true;
    private boolean mMuteInput = false;
    private boolean mStylusButtonDownAtStart;

    DragInputEventReceiver(InputChannel inputChannel, Looper looper, DragDropController controller) {
        super(inputChannel, looper);
        this.mDragDropController = controller;
    }

    public void onInputEvent(InputEvent event) {
        boolean handled = false;
        try {
            if ((event instanceof MotionEvent) && (event.getSource() & 2) != 0) {
                if (!this.mMuteInput) {
                    MotionEvent motionEvent = (MotionEvent) event;
                    float newX = motionEvent.getRawX();
                    float newY = motionEvent.getRawY();
                    boolean z = false;
                    boolean isStylusButtonDown = (motionEvent.getButtonState() & 32) != 0;
                    if (this.mIsStartEvent) {
                        this.mStylusButtonDownAtStart = isStylusButtonDown;
                        this.mIsStartEvent = false;
                    }
                    int action = motionEvent.getAction();
                    if (action != 0) {
                        if (action == 1) {
                            Slog.i("WindowManager", "Got UP on move channel; dropping at " + newX + "," + newY);
                            this.mMuteInput = true;
                        } else if (action != 2) {
                            if (action != 3) {
                                finishInputEvent(event, false);
                                return;
                            } else {
                                Slog.i("WindowManager", "Drag cancelled!");
                                this.mMuteInput = true;
                            }
                        } else if (this.mStylusButtonDownAtStart && !isStylusButtonDown) {
                            this.mMuteInput = true;
                        }
                        DragDropController dragDropController = this.mDragDropController;
                        if (!this.mMuteInput) {
                            z = true;
                        }
                        dragDropController.handleMotionEvent(z, newX, newY);
                        handled = true;
                        finishInputEvent(event, handled);
                        return;
                    }
                    finishInputEvent(event, false);
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
