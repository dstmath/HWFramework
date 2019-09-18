package com.android.server.wm.utils;

import android.graphics.Rect;
import android.util.Size;
import android.util.Slog;
import android.view.DisplayCutout;
import java.util.List;
import java.util.Objects;

public class WmDisplayCutout {
    public static final WmDisplayCutout NO_CUTOUT = new WmDisplayCutout(DisplayCutout.NO_CUTOUT, null);
    public static final String TAG = "WmDisplayCutout";
    private final Size mFrameSize;
    private final DisplayCutout mInner;

    public WmDisplayCutout(DisplayCutout inner, Size frameSize) {
        this.mInner = inner;
        this.mFrameSize = frameSize;
    }

    public static WmDisplayCutout computeSafeInsets(DisplayCutout inner, int displayWidth, int displayHeight) {
        if (inner == DisplayCutout.NO_CUTOUT || inner.isBoundsEmpty()) {
            return NO_CUTOUT;
        }
        Size displaySize = new Size(displayWidth, displayHeight);
        return new WmDisplayCutout(inner.replaceSafeInsets(computeSafeInsets(displaySize, inner)), displaySize);
    }

    public WmDisplayCutout inset(int insetLeft, int insetTop, int insetRight, int insetBottom) {
        Size frame;
        DisplayCutout newInner = this.mInner.inset(insetLeft, insetTop, insetRight, insetBottom);
        if (this.mInner == newInner) {
            return this;
        }
        if (this.mFrameSize == null) {
            frame = null;
        } else {
            frame = new Size((this.mFrameSize.getWidth() - insetLeft) - insetRight, (this.mFrameSize.getHeight() - insetTop) - insetBottom);
        }
        return new WmDisplayCutout(newInner, frame);
    }

    public WmDisplayCutout calculateRelativeTo(Rect frame) {
        if (this.mInner.isEmpty()) {
            return this;
        }
        return inset(frame.left, frame.top, this.mFrameSize.getWidth() - frame.right, this.mFrameSize.getHeight() - frame.bottom);
    }

    public WmDisplayCutout computeSafeInsets(int width, int height) {
        return computeSafeInsets(this.mInner, width, height);
    }

    private static Rect computeSafeInsets(Size displaySize, DisplayCutout cutout) {
        if (displaySize.getWidth() < displaySize.getHeight()) {
            List<Rect> boundingRects = cutout.replaceSafeInsets(new Rect(0, displaySize.getHeight() / 2, 0, displaySize.getHeight() / 2)).getBoundingRects();
            return new Rect(0, findInsetForSide(displaySize, boundingRects, 48), 0, findInsetForSide(displaySize, boundingRects, 80));
        } else if (displaySize.getWidth() > displaySize.getHeight()) {
            List<Rect> boundingRects2 = cutout.replaceSafeInsets(new Rect(displaySize.getWidth() / 2, 0, displaySize.getWidth() / 2, 0)).getBoundingRects();
            return new Rect(findInsetForSide(displaySize, boundingRects2, 3), 0, findInsetForSide(displaySize, boundingRects2, 5), 0);
        } else {
            Slog.e(TAG, "not implemented: display=" + displaySize + " cutout=" + cutout, new Throwable());
            return new Rect();
        }
    }

    private static int findInsetForSide(Size display, List<Rect> boundingRects, int gravity) {
        int inset = 0;
        int size = boundingRects.size();
        for (int i = 0; i < size; i++) {
            Rect boundingRect = boundingRects.get(i);
            if (gravity != 3) {
                if (gravity != 5) {
                    if (gravity != 48) {
                        if (gravity != 80) {
                            throw new IllegalArgumentException("unknown gravity: " + gravity);
                        } else if (boundingRect.bottom == display.getHeight()) {
                            inset = Math.max(inset, display.getHeight() - boundingRect.top);
                        }
                    } else if (boundingRect.top == 0) {
                        inset = Math.max(inset, boundingRect.bottom);
                    }
                } else if (boundingRect.right == display.getWidth()) {
                    inset = Math.max(inset, display.getWidth() - boundingRect.left);
                }
            } else if (boundingRect.left == 0) {
                inset = Math.max(inset, boundingRect.right);
            }
        }
        return inset;
    }

    public DisplayCutout getDisplayCutout() {
        return this.mInner;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof WmDisplayCutout)) {
            return false;
        }
        WmDisplayCutout that = (WmDisplayCutout) o;
        if (Objects.equals(this.mInner, that.mInner) && Objects.equals(this.mFrameSize, that.mFrameSize)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mInner, this.mFrameSize});
    }
}
