package com.android.server.wm;

import android.graphics.Region;
import android.view.MotionEvent;
import android.view.WindowManagerPolicy.PointerEventListener;

public class StackTapPointerEventListener implements PointerEventListener {
    private static final float TAP_MOTION_SLOP_INCHES = 0.125f;
    private static final int TAP_TIMEOUT_MSEC = 300;
    private final DisplayContent mDisplayContent;
    private float mDownX;
    private float mDownY;
    private final int mMotionSlop;
    private int mPointerId;
    private final WindowManagerService mService;
    private final Region mTouchExcludeRegion = new Region();

    public StackTapPointerEventListener(WindowManagerService service, DisplayContent displayContent) {
        this.mService = service;
        this.mDisplayContent = displayContent;
        this.mMotionSlop = (int) (((float) displayContent.getDisplayInfo().logicalDensityDpi) * TAP_MOTION_SLOP_INCHES);
    }

    public void onPointerEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        int index;
        switch (action & 255) {
            case 0:
                this.mPointerId = motionEvent.getPointerId(0);
                this.mDownX = motionEvent.getX();
                this.mDownY = motionEvent.getY();
                return;
            case 1:
            case 6:
                index = (65280 & action) >> 8;
                if (this.mPointerId == motionEvent.getPointerId(index)) {
                    int x = (int) motionEvent.getX(index);
                    int y = (int) motionEvent.getY(index);
                    synchronized (this) {
                        if (motionEvent.getEventTime() - motionEvent.getDownTime() < 300 && Math.abs(((float) x) - this.mDownX) < ((float) this.mMotionSlop) && Math.abs(((float) y) - this.mDownY) < ((float) this.mMotionSlop) && (this.mTouchExcludeRegion.contains(x, y) ^ 1) != 0) {
                            this.mService.mH.obtainMessage(31, x, y, this.mDisplayContent).sendToTarget();
                        }
                    }
                    this.mPointerId = -1;
                    return;
                }
                return;
            case 2:
                if (this.mPointerId >= 0) {
                    index = motionEvent.findPointerIndex(this.mPointerId);
                    if (index < 0) {
                        this.mPointerId = -1;
                        return;
                    } else if (motionEvent.getEventTime() - motionEvent.getDownTime() > 300 || index < 0 || Math.abs(motionEvent.getX(index) - this.mDownX) > ((float) this.mMotionSlop) || Math.abs(motionEvent.getY(index) - this.mDownY) > ((float) this.mMotionSlop)) {
                        this.mPointerId = -1;
                        return;
                    } else {
                        return;
                    }
                }
                return;
            default:
                return;
        }
    }

    void setTouchExcludeRegion(Region newRegion) {
        synchronized (this) {
            this.mTouchExcludeRegion.set(newRegion);
        }
    }
}
