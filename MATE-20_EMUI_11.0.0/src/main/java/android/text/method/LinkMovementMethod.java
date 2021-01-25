package android.text.method;

import android.annotation.UnsupportedAppUsage;
import android.text.Layout;
import android.text.NoCopySpan;
import android.text.Selection;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.textclassifier.TextLinks;
import android.widget.TextView;

public class LinkMovementMethod extends ScrollingMovementMethod {
    private static final int CLICK = 1;
    private static final int DOWN = 3;
    private static Object FROM_BELOW = new NoCopySpan.Concrete();
    private static final int HIDE_FLOATING_TOOLBAR_DELAY_MS = 200;
    private static final int UP = 2;
    @UnsupportedAppUsage
    private static LinkMovementMethod sInstance;

    @Override // android.text.method.BaseMovementMethod, android.text.method.MovementMethod
    public boolean canSelectArbitrarily() {
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean handleMovementKey(TextView widget, Spannable buffer, int keyCode, int movementMetaState, KeyEvent event) {
        if ((keyCode == 23 || keyCode == 66) && KeyEvent.metaStateHasNoModifiers(movementMetaState) && event.getAction() == 0 && event.getRepeatCount() == 0 && action(1, widget, buffer)) {
            return true;
        }
        return super.handleMovementKey(widget, buffer, keyCode, movementMetaState, event);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.ScrollingMovementMethod, android.text.method.BaseMovementMethod
    public boolean up(TextView widget, Spannable buffer) {
        if (action(2, widget, buffer)) {
            return true;
        }
        return super.up(widget, buffer);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.ScrollingMovementMethod, android.text.method.BaseMovementMethod
    public boolean down(TextView widget, Spannable buffer) {
        if (action(3, widget, buffer)) {
            return true;
        }
        return super.down(widget, buffer);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.ScrollingMovementMethod, android.text.method.BaseMovementMethod
    public boolean left(TextView widget, Spannable buffer) {
        if (action(2, widget, buffer)) {
            return true;
        }
        return super.left(widget, buffer);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.ScrollingMovementMethod, android.text.method.BaseMovementMethod
    public boolean right(TextView widget, Spannable buffer) {
        if (action(3, widget, buffer)) {
            return true;
        }
        return super.right(widget, buffer);
    }

    private boolean action(int what, TextView widget, Spannable buffer) {
        int areaBot;
        Layout layout = widget.getLayout();
        int padding = widget.getTotalPaddingTop() + widget.getTotalPaddingBottom();
        int areaTop = widget.getScrollY();
        int areaBot2 = (widget.getHeight() + areaTop) - padding;
        int lineTop = layout.getLineForVertical(areaTop);
        int lineBot = layout.getLineForVertical(areaBot2);
        int first = layout.getLineStart(lineTop);
        int last = layout.getLineEnd(lineBot);
        ClickableSpan[] candidates = (ClickableSpan[]) buffer.getSpans(first, last, ClickableSpan.class);
        int a = Selection.getSelectionStart(buffer);
        int b = Selection.getSelectionEnd(buffer);
        int selStart = Math.min(a, b);
        int selEnd = Math.max(a, b);
        if (selStart < 0) {
            if (buffer.getSpanStart(FROM_BELOW) >= 0) {
                int length = buffer.length();
                selEnd = length;
                selStart = length;
            }
        }
        if (selStart > last) {
            selEnd = Integer.MAX_VALUE;
            selStart = Integer.MAX_VALUE;
        }
        if (selEnd < first) {
            selEnd = -1;
            selStart = -1;
        }
        if (what != 1) {
            if (what == 2) {
                int bestStart = -1;
                int bestEnd = -1;
                int i = 0;
                while (i < candidates.length) {
                    int end = buffer.getSpanEnd(candidates[i]);
                    if (end >= selEnd && selStart != selEnd) {
                        areaBot = areaBot2;
                    } else if (end > bestEnd) {
                        areaBot = areaBot2;
                        bestStart = buffer.getSpanStart(candidates[i]);
                        bestEnd = end;
                    } else {
                        areaBot = areaBot2;
                    }
                    i++;
                    areaBot2 = areaBot;
                }
                if (bestStart >= 0) {
                    Selection.setSelection(buffer, bestEnd, bestStart);
                    return true;
                }
            } else if (what == 3) {
                int bestEnd2 = Integer.MAX_VALUE;
                int bestStart2 = Integer.MAX_VALUE;
                int i2 = 0;
                while (i2 < candidates.length) {
                    int start = buffer.getSpanStart(candidates[i2]);
                    if ((start > selStart || selStart == selEnd) && start < bestStart2) {
                        bestEnd2 = buffer.getSpanEnd(candidates[i2]);
                        bestStart2 = start;
                    }
                    i2++;
                    areaTop = areaTop;
                }
                if (bestEnd2 < Integer.MAX_VALUE) {
                    Selection.setSelection(buffer, bestStart2, bestEnd2);
                    return true;
                }
            }
        } else if (selStart == selEnd) {
            return false;
        } else {
            ClickableSpan[] links = (ClickableSpan[]) buffer.getSpans(selStart, selEnd, ClickableSpan.class);
            if (links.length != 1) {
                return false;
            }
            ClickableSpan link = links[0];
            if (link instanceof TextLinks.TextLinkSpan) {
                ((TextLinks.TextLinkSpan) link).onClick(widget, 1);
            } else {
                link.onClick(widget);
            }
        }
        return false;
    }

    @Override // android.text.method.ScrollingMovementMethod, android.text.method.BaseMovementMethod, android.text.method.MovementMethod
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();
        if (action == 1 || action == 0) {
            int x = ((int) event.getX()) - widget.getTotalPaddingLeft();
            int y = ((int) event.getY()) - widget.getTotalPaddingTop();
            int x2 = x + widget.getScrollX();
            int y2 = y + widget.getScrollY();
            Layout layout = widget.getLayout();
            int off = layout.getOffsetForHorizontal(layout.getLineForVertical(y2), (float) x2);
            ClickableSpan[] links = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
            if (links.length != 0) {
                ClickableSpan link = links[0];
                if (action == 1) {
                    if (link instanceof TextLinks.TextLinkSpan) {
                        ((TextLinks.TextLinkSpan) link).onClick(widget, 0);
                    } else {
                        link.onClick(widget);
                    }
                } else if (action == 0) {
                    if (widget.getContext().getApplicationInfo().targetSdkVersion >= 28) {
                        widget.hideFloatingToolbar(200);
                    }
                    Selection.setSelection(buffer, buffer.getSpanStart(link), buffer.getSpanEnd(link));
                }
                return true;
            }
            Selection.removeSelection(buffer);
        }
        return super.onTouchEvent(widget, buffer, event);
    }

    @Override // android.text.method.BaseMovementMethod, android.text.method.MovementMethod
    public void initialize(TextView widget, Spannable text) {
        Selection.removeSelection(text);
        text.removeSpan(FROM_BELOW);
    }

    @Override // android.text.method.ScrollingMovementMethod, android.text.method.BaseMovementMethod, android.text.method.MovementMethod
    public void onTakeFocus(TextView view, Spannable text, int dir) {
        Selection.removeSelection(text);
        if ((dir & 1) != 0) {
            text.setSpan(FROM_BELOW, 0, 0, 34);
        } else {
            text.removeSpan(FROM_BELOW);
        }
    }

    public static MovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new LinkMovementMethod();
        }
        return sInstance;
    }
}
