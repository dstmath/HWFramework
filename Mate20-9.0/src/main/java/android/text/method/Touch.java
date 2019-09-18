package android.text.method;

import android.text.Layout;
import android.text.NoCopySpan;
import android.text.Spannable;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

public class Touch {

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
            right = 0;
            left = Integer.MAX_VALUE;
            for (int i = top; i <= bottom; i++) {
                left = (int) Math.min((float) left, layout.getLineLeft(i));
                right = (int) Math.max((float) right, layout.getLineRight(i));
            }
        } else {
            left = 0;
            right = availableWidth;
        }
        int right2 = right;
        int actualWidth = right2 - left;
        if (actualWidth >= availableWidth) {
            x2 = Math.max(Math.min(x, right2 - availableWidth), left);
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
        float dy;
        float dx;
        switch (event.getActionMasked()) {
            case 0:
                DragState[] ds = (DragState[]) buffer.getSpans(0, buffer.length(), DragState.class);
                for (DragState removeSpan : ds) {
                    buffer.removeSpan(removeSpan);
                }
                buffer.setSpan(new DragState(event.getX(), event.getY(), widget.getScrollX(), widget.getScrollY()), 0, 0, 17);
                return true;
            case 1:
                DragState[] ds2 = (DragState[]) buffer.getSpans(0, buffer.length(), DragState.class);
                for (DragState removeSpan2 : ds2) {
                    buffer.removeSpan(removeSpan2);
                }
                return ds2.length > 0 && ds2[0].mUsed;
            case 2:
                DragState[] ds3 = (DragState[]) buffer.getSpans(0, buffer.length(), DragState.class);
                if (ds3.length > 0) {
                    if (!ds3[0].mFarEnough) {
                        int slop = ViewConfiguration.get(widget.getContext()).getScaledTouchSlop();
                        if (Math.abs(event.getX() - ds3[0].mX) >= ((float) slop) || Math.abs(event.getY() - ds3[0].mY) >= ((float) slop)) {
                            ds3[0].mFarEnough = true;
                        }
                    }
                    if (ds3[0].mFarEnough) {
                        ds3[0].mUsed = true;
                        if (((event.getMetaState() & 1) == 0 && MetaKeyKeyListener.getMetaState((CharSequence) buffer, 1) != 1 && MetaKeyKeyListener.getMetaState((CharSequence) buffer, 2048) == 0) ? false : true) {
                            dx = event.getX() - ds3[0].mX;
                            dy = event.getY() - ds3[0].mY;
                        } else {
                            dx = ds3[0].mX - event.getX();
                            dy = ds3[0].mY - event.getY();
                        }
                        ds3[0].mX = event.getX();
                        ds3[0].mY = event.getY();
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
                break;
        }
        return false;
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
}
