package android.text.method;

import android.text.Layout;
import android.text.NoCopySpan;
import android.text.Spannable;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

public class Touch {
    private Touch() {
    }

    public static void scrollTo(TextView widget, Layout layout, int x, int y) {
        int right;
        int left;
        int x2;
        int availableWidth = widget.getWidth() - (widget.getTotalPaddingLeft() + widget.getTotalPaddingRight());
        int top = layout.getLineForVertical(y);
        Layout.Alignment a = layout.getParagraphAlignment(top);
        boolean ltr = layout.getParagraphDirection(top) > 0;
        if (widget.getHorizontallyScrolling()) {
            int bottom = layout.getLineForVertical((widget.getHeight() + y) - (widget.getTotalPaddingTop() + widget.getTotalPaddingBottom()));
            left = Integer.MAX_VALUE;
            right = 0;
            for (int i = top; i <= bottom; i++) {
                left = (int) Math.min((float) left, layout.getLineLeft(i));
                right = (int) Math.max((float) right, layout.getLineRight(i));
            }
        } else {
            left = 0;
            right = availableWidth;
        }
        int actualWidth = right - left;
        if (actualWidth >= availableWidth) {
            x2 = Math.max(Math.min(x, right - availableWidth), left);
        } else if (a == Layout.Alignment.ALIGN_CENTER) {
            x2 = left - ((availableWidth - actualWidth) / 2);
        } else if ((!ltr || a != Layout.Alignment.ALIGN_OPPOSITE) && ((ltr || a != Layout.Alignment.ALIGN_NORMAL) && a != Layout.Alignment.ALIGN_RIGHT)) {
            x2 = left;
        } else {
            x2 = left - (availableWidth - actualWidth);
        }
        widget.scrollTo(x2, y);
    }

    public static boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        DragState[] ds;
        float dy;
        float dx;
        int actionMasked = event.getActionMasked();
        if (actionMasked == 0) {
            for (DragState dragState : (DragState[]) buffer.getSpans(0, buffer.length(), DragState.class)) {
                buffer.removeSpan(dragState);
            }
            buffer.setSpan(new DragState(event.getX(), event.getY(), widget.getScrollX(), widget.getScrollY()), 0, 0, 17);
            return true;
        } else if (actionMasked != 1) {
            if (actionMasked == 2) {
                DragState[] ds2 = (DragState[]) buffer.getSpans(0, buffer.length(), DragState.class);
                if (ds2.length > 0) {
                    if (!ds2[0].mFarEnough) {
                        int slop = ViewConfiguration.get(widget.getContext()).getScaledTouchSlop();
                        if (Math.abs(event.getX() - ds2[0].mX) >= ((float) slop) || Math.abs(event.getY() - ds2[0].mY) >= ((float) slop)) {
                            ds2[0].mFarEnough = true;
                        }
                    }
                    if (ds2[0].mFarEnough) {
                        ds2[0].mUsed = true;
                        if (((event.getMetaState() & 1) == 0 && MetaKeyKeyListener.getMetaState(buffer, 1) != 1 && MetaKeyKeyListener.getMetaState(buffer, 2048) == 0) ? false : true) {
                            dx = event.getX() - ds2[0].mX;
                            dy = event.getY() - ds2[0].mY;
                        } else {
                            dx = ds2[0].mX - event.getX();
                            dy = ds2[0].mY - event.getY();
                        }
                        ds2[0].mX = event.getX();
                        ds2[0].mY = event.getY();
                        int nx = widget.getScrollX() + ((int) dx);
                        int ny = widget.getScrollY() + ((int) dy);
                        int padding = widget.getTotalPaddingTop() + widget.getTotalPaddingBottom();
                        Layout layout = widget.getLayout();
                        int ny2 = Math.max(Math.min(ny, layout.getHeight() - (widget.getHeight() - padding)), 0);
                        int oldX = widget.getScrollX();
                        int oldY = widget.getScrollY();
                        scrollTo(widget, layout, nx, ny2);
                        if (!(oldX == widget.getScrollX() && oldY == widget.getScrollY())) {
                            widget.cancelLongPress();
                        }
                        return true;
                    }
                }
            }
            return false;
        } else {
            DragState[] ds3 = (DragState[]) buffer.getSpans(0, buffer.length(), DragState.class);
            for (DragState dragState2 : ds3) {
                buffer.removeSpan(dragState2);
            }
            return ds3.length > 0 && ds3[0].mUsed;
        }
    }

    public static int getInitialScrollX(TextView widget, Spannable buffer) {
        DragState[] ds = (DragState[]) buffer.getSpans(0, buffer.length(), DragState.class);
        if (ds.length > 0) {
            return ds[0].mScrollX;
        }
        return -1;
    }

    public static int getInitialScrollY(TextView widget, Spannable buffer) {
        DragState[] ds = (DragState[]) buffer.getSpans(0, buffer.length(), DragState.class);
        if (ds.length > 0) {
            return ds[0].mScrollY;
        }
        return -1;
    }

    private static class DragState implements NoCopySpan {
        public boolean mFarEnough;
        public int mScrollX;
        public int mScrollY;
        public boolean mUsed;
        public float mX;
        public float mY;

        public DragState(float x, float y, int scrollX, int scrollY) {
            this.mX = x;
            this.mY = y;
            this.mScrollX = scrollX;
            this.mScrollY = scrollY;
        }
    }
}
