package android.text.method;

import android.text.Layout;
import android.text.Spannable;
import android.view.MotionEvent;
import android.widget.TextView;

public class ScrollingMovementMethod extends BaseMovementMethod implements MovementMethod {
    private static ScrollingMovementMethod sInstance;

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean left(TextView widget, Spannable buffer) {
        return scrollLeft(widget, buffer, 1);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean right(TextView widget, Spannable buffer) {
        return scrollRight(widget, buffer, 1);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean up(TextView widget, Spannable buffer) {
        return scrollUp(widget, buffer, 1);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean down(TextView widget, Spannable buffer) {
        return scrollDown(widget, buffer, 1);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean pageUp(TextView widget, Spannable buffer) {
        return scrollPageUp(widget, buffer);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean pageDown(TextView widget, Spannable buffer) {
        return scrollPageDown(widget, buffer);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean top(TextView widget, Spannable buffer) {
        return scrollTop(widget, buffer);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean bottom(TextView widget, Spannable buffer) {
        return scrollBottom(widget, buffer);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean lineStart(TextView widget, Spannable buffer) {
        return scrollLineStart(widget, buffer);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean lineEnd(TextView widget, Spannable buffer) {
        return scrollLineEnd(widget, buffer);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean home(TextView widget, Spannable buffer) {
        return top(widget, buffer);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean end(TextView widget, Spannable buffer) {
        return bottom(widget, buffer);
    }

    @Override // android.text.method.BaseMovementMethod, android.text.method.MovementMethod
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        return Touch.onTouchEvent(widget, buffer, event);
    }

    @Override // android.text.method.BaseMovementMethod, android.text.method.MovementMethod
    public void onTakeFocus(TextView widget, Spannable text, int dir) {
        Layout layout = widget.getLayout();
        if (!(layout == null || (dir & 2) == 0)) {
            widget.scrollTo(widget.getScrollX(), layout.getLineTop(0));
        }
        if (layout != null && (dir & 1) != 0) {
            widget.scrollTo(widget.getScrollX(), layout.getLineTop((layout.getLineCount() - 1) + 1) - (widget.getHeight() - (widget.getTotalPaddingTop() + widget.getTotalPaddingBottom())));
        }
    }

    public static MovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new ScrollingMovementMethod();
        }
        return sInstance;
    }
}
