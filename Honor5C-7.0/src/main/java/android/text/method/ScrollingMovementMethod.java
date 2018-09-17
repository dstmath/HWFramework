package android.text.method;

import android.text.Layout;
import android.text.Spannable;
import android.view.MotionEvent;
import android.widget.TextView;

public class ScrollingMovementMethod extends BaseMovementMethod implements MovementMethod {
    private static ScrollingMovementMethod sInstance;

    protected boolean left(TextView widget, Spannable buffer) {
        return scrollLeft(widget, buffer, 1);
    }

    protected boolean right(TextView widget, Spannable buffer) {
        return scrollRight(widget, buffer, 1);
    }

    protected boolean up(TextView widget, Spannable buffer) {
        return scrollUp(widget, buffer, 1);
    }

    protected boolean down(TextView widget, Spannable buffer) {
        return scrollDown(widget, buffer, 1);
    }

    protected boolean pageUp(TextView widget, Spannable buffer) {
        return scrollPageUp(widget, buffer);
    }

    protected boolean pageDown(TextView widget, Spannable buffer) {
        return scrollPageDown(widget, buffer);
    }

    protected boolean top(TextView widget, Spannable buffer) {
        return scrollTop(widget, buffer);
    }

    protected boolean bottom(TextView widget, Spannable buffer) {
        return scrollBottom(widget, buffer);
    }

    protected boolean lineStart(TextView widget, Spannable buffer) {
        return scrollLineStart(widget, buffer);
    }

    protected boolean lineEnd(TextView widget, Spannable buffer) {
        return scrollLineEnd(widget, buffer);
    }

    protected boolean home(TextView widget, Spannable buffer) {
        return top(widget, buffer);
    }

    protected boolean end(TextView widget, Spannable buffer) {
        return bottom(widget, buffer);
    }

    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        return Touch.onTouchEvent(widget, buffer, event);
    }

    public void onTakeFocus(TextView widget, Spannable text, int dir) {
        Layout layout = widget.getLayout();
        if (!(layout == null || (dir & 2) == 0)) {
            widget.scrollTo(widget.getScrollX(), layout.getLineTop(0));
        }
        if (layout != null && (dir & 1) != 0) {
            int line = layout.getLineCount() - 1;
            widget.scrollTo(widget.getScrollX(), layout.getLineTop(line + 1) - (widget.getHeight() - (widget.getTotalPaddingTop() + widget.getTotalPaddingBottom())));
        }
    }

    public static MovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new ScrollingMovementMethod();
        }
        return sInstance;
    }
}
