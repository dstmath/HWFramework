package android.text.method;

import android.graphics.Rect;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;

public class ArrowKeyMovementMethod extends BaseMovementMethod implements MovementMethod {
    private static final Object LAST_TAP_DOWN = new Object();
    private static ArrowKeyMovementMethod sInstance;

    private static boolean isSelecting(Spannable buffer) {
        if (MetaKeyKeyListener.getMetaState((CharSequence) buffer, 1) == 1 || MetaKeyKeyListener.getMetaState((CharSequence) buffer, 2048) != 0) {
            return true;
        }
        return false;
    }

    private static int getCurrentLineTop(Spannable buffer, Layout layout) {
        return layout.getLineTop(layout.getLineForOffset(Selection.getSelectionEnd(buffer)));
    }

    private static int getPageHeight(TextView widget) {
        Rect rect = new Rect();
        if (widget.getGlobalVisibleRect(rect)) {
            return rect.height();
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public boolean handleMovementKey(TextView widget, Spannable buffer, int keyCode, int movementMetaState, KeyEvent event) {
        if (keyCode == 23 && KeyEvent.metaStateHasNoModifiers(movementMetaState) && event.getAction() == 0 && event.getRepeatCount() == 0 && MetaKeyKeyListener.getMetaState(buffer, 2048, event) != 0) {
            return widget.showContextMenu();
        }
        return super.handleMovementKey(widget, buffer, keyCode, movementMetaState, event);
    }

    /* access modifiers changed from: protected */
    public boolean left(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        if (isSelecting(buffer)) {
            return Selection.extendLeft(buffer, layout);
        }
        return Selection.moveLeft(buffer, layout);
    }

    /* access modifiers changed from: protected */
    public boolean right(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        if (isSelecting(buffer)) {
            return Selection.extendRight(buffer, layout);
        }
        return Selection.moveRight(buffer, layout);
    }

    /* access modifiers changed from: protected */
    public boolean up(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        if (isSelecting(buffer)) {
            return Selection.extendUp(buffer, layout);
        }
        return Selection.moveUp(buffer, layout);
    }

    /* access modifiers changed from: protected */
    public boolean down(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        if (isSelecting(buffer)) {
            return Selection.extendDown(buffer, layout);
        }
        return Selection.moveDown(buffer, layout);
    }

    /* access modifiers changed from: protected */
    public boolean pageUp(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        boolean selecting = isSelecting(buffer);
        int targetY = getCurrentLineTop(buffer, layout) - getPageHeight(widget);
        boolean handled = false;
        do {
            int previousSelectionEnd = Selection.getSelectionEnd(buffer);
            if (selecting) {
                Selection.extendUp(buffer, layout);
            } else {
                Selection.moveUp(buffer, layout);
            }
            if (Selection.getSelectionEnd(buffer) == previousSelectionEnd) {
                break;
            }
            handled = true;
        } while (getCurrentLineTop(buffer, layout) > targetY);
        return handled;
    }

    /* access modifiers changed from: protected */
    public boolean pageDown(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        boolean selecting = isSelecting(buffer);
        int targetY = getCurrentLineTop(buffer, layout) + getPageHeight(widget);
        boolean handled = false;
        do {
            int previousSelectionEnd = Selection.getSelectionEnd(buffer);
            if (selecting) {
                Selection.extendDown(buffer, layout);
            } else {
                Selection.moveDown(buffer, layout);
            }
            if (Selection.getSelectionEnd(buffer) == previousSelectionEnd) {
                break;
            }
            handled = true;
        } while (getCurrentLineTop(buffer, layout) < targetY);
        return handled;
    }

    /* access modifiers changed from: protected */
    public boolean top(TextView widget, Spannable buffer) {
        if (isSelecting(buffer)) {
            Selection.extendSelection(buffer, 0);
        } else {
            Selection.setSelection(buffer, 0);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean bottom(TextView widget, Spannable buffer) {
        if (isSelecting(buffer)) {
            Selection.extendSelection(buffer, buffer.length());
        } else {
            Selection.setSelection(buffer, buffer.length());
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean lineStart(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        if (isSelecting(buffer)) {
            return Selection.extendToLeftEdge(buffer, layout);
        }
        return Selection.moveToLeftEdge(buffer, layout);
    }

    /* access modifiers changed from: protected */
    public boolean lineEnd(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        if (isSelecting(buffer)) {
            return Selection.extendToRightEdge(buffer, layout);
        }
        return Selection.moveToRightEdge(buffer, layout);
    }

    /* access modifiers changed from: protected */
    public boolean leftWord(TextView widget, Spannable buffer) {
        int selectionEnd = widget.getSelectionEnd();
        WordIterator wordIterator = widget.getWordIterator();
        wordIterator.setCharSequence(buffer, selectionEnd, selectionEnd);
        return Selection.moveToPreceding(buffer, wordIterator, isSelecting(buffer));
    }

    /* access modifiers changed from: protected */
    public boolean rightWord(TextView widget, Spannable buffer) {
        int selectionEnd = widget.getSelectionEnd();
        WordIterator wordIterator = widget.getWordIterator();
        wordIterator.setCharSequence(buffer, selectionEnd, selectionEnd);
        return Selection.moveToFollowing(buffer, wordIterator, isSelecting(buffer));
    }

    /* access modifiers changed from: protected */
    public boolean home(TextView widget, Spannable buffer) {
        return lineStart(widget, buffer);
    }

    /* access modifiers changed from: protected */
    public boolean end(TextView widget, Spannable buffer) {
        return lineEnd(widget, buffer);
    }

    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int initialScrollX = -1;
        int initialScrollY = -1;
        int action = event.getAction();
        if (action == 1) {
            initialScrollX = Touch.getInitialScrollX(widget, buffer);
            initialScrollY = Touch.getInitialScrollY(widget, buffer);
        }
        boolean wasTouchSelecting = isSelecting(buffer);
        boolean handled = Touch.onTouchEvent(widget, buffer, event);
        if (widget.didTouchFocusSelect()) {
            return handled;
        }
        if (action == 0) {
            if (!isSelecting(buffer) || (!widget.isFocused() && !widget.requestFocus())) {
                return handled;
            }
            int offset = widget.getOffsetForPosition(event.getX(), event.getY());
            buffer.setSpan(LAST_TAP_DOWN, offset, offset, 34);
            widget.getParent().requestDisallowInterceptTouchEvent(true);
        } else if (widget.isFocused()) {
            if (action == 2) {
                if (isSelecting(buffer) && handled) {
                    int startOffset = buffer.getSpanStart(LAST_TAP_DOWN);
                    widget.cancelLongPress();
                    int offset2 = widget.getOffsetForPosition(event.getX(), event.getY());
                    Selection.setSelection(buffer, Math.min(startOffset, offset2), Math.max(startOffset, offset2));
                    return true;
                }
            } else if (action == 1) {
                if ((initialScrollY < 0 || initialScrollY == widget.getScrollY()) && (initialScrollX < 0 || initialScrollX == widget.getScrollX())) {
                    if (wasTouchSelecting) {
                        int startOffset2 = buffer.getSpanStart(LAST_TAP_DOWN);
                        int endOffset = widget.getOffsetForPosition(event.getX(), event.getY());
                        Selection.setSelection(buffer, Math.min(startOffset2, endOffset), Math.max(startOffset2, endOffset));
                        buffer.removeSpan(LAST_TAP_DOWN);
                    }
                    MetaKeyKeyListener.adjustMetaAfterKeypress(buffer);
                    MetaKeyKeyListener.resetLockedMeta(buffer);
                    return true;
                }
                widget.moveCursorToVisibleOffset();
                return true;
            }
        }
        return handled;
    }

    public boolean canSelectArbitrarily() {
        return true;
    }

    public void initialize(TextView widget, Spannable text) {
        Selection.setSelection(text, 0);
    }

    public void onTakeFocus(TextView view, Spannable text, int dir) {
        if ((dir & 130) == 0) {
            Selection.setSelection(text, text.length());
        } else if (view.getLayout() == null) {
            Selection.setSelection(text, text.length());
        }
    }

    public static MovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new ArrowKeyMovementMethod();
        }
        return sInstance;
    }
}
