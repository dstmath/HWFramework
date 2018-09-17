package huawei.com.android.internal.widget;

import android.graphics.RectF;
import android.text.Layout;
import android.view.MotionEvent;
import android.widget.Editor;
import android.widget.Editor.HandleView;
import android.widget.Editor.InsertionHandleView;
import android.widget.Editor.SelectionHandleView;
import android.widget.TextView;

public class HwEditor extends Editor {
    private static final String TAG = "HwEditor";
    private static final float WORD_END_SCALE = 0.55f;
    private int mCursorBottom = -1;
    private boolean mCursorShouldMoveEnd = false;
    private int mCursorTop = -1;
    private float mCursorX = -1.0f;
    private float mInsertHanlePosX = -1.0f;
    private boolean mPosIsLineEnd = false;
    private int mPreOffset = -1;
    private float mTouchPosX = -1.0f;

    public HwEditor(TextView textView) {
        super(textView);
    }

    public void selectAllAndShowEditor() {
        stopTextActionMode();
        selectAllText();
        invalidateActionModeAsync();
        showFloatingToolbar();
    }

    protected int adjustOffsetAtLineEndForTouchPos(int offset) {
        return adjustOffsetAtLineEnd(offset, true);
    }

    protected int adjustOffsetAtLineEndForInsertHanlePos(int offset) {
        return adjustOffsetAtLineEnd(offset, false);
    }

    private int adjustOffsetAtLineEnd(int offset, boolean isTouchPos) {
        Layout layout = this.mTextView.getLayout();
        if (!(-1.0f == getPos(isTouchPos) || layout == null)) {
            int currLine = layout.getLineForOffset(offset);
            boolean offsetNearEnd = offset == layout.getLineEnd(currLine) + -1 && currLine != layout.getLineCount() - 1;
            float start = -1.0f;
            float end = -1.0f;
            if (offsetNearEnd) {
                start = layout.getPrimaryHorizontal(offset) + ((float) this.mTextView.getCompoundPaddingLeft());
                end = layout.getLineMax(currLine) + ((float) this.mTextView.getCompoundPaddingLeft());
            } else if (offset == layout.getLineStart(currLine) && currLine > 0) {
                start = layout.getPrimaryHorizontal(offset - 1) + ((float) this.mTextView.getCompoundPaddingLeft());
                end = layout.getLineMax(currLine - 1) + ((float) this.mTextView.getCompoundPaddingLeft());
            }
            float XThresholdToSnap = start + ((end - start) * WORD_END_SCALE);
            if ((!offsetNearEnd && !this.mPosIsLineEnd) || end <= start || getPos(isTouchPos) < XThresholdToSnap) {
                this.mPosIsLineEnd = false;
            } else if (offsetNearEnd) {
                offset = layout.getLineEnd(currLine);
                this.mPosIsLineEnd = true;
            }
            setPos(-1.0f, isTouchPos);
        }
        if (isTouchPos) {
            if (this.mPosIsLineEnd) {
                this.mCursorShouldMoveEnd = true;
            } else {
                this.mCursorShouldMoveEnd = false;
            }
            if (offset == this.mPreOffset) {
                updateCursorsPositions();
            }
        }
        return offset;
    }

    private float getPos(boolean isTouchPos) {
        if (isTouchPos) {
            return this.mTouchPosX;
        }
        return this.mInsertHanlePosX;
    }

    private void setPos(float pos, boolean isTouchPos) {
        if (isTouchPos) {
            this.mTouchPosX = pos;
        } else {
            this.mInsertHanlePosX = pos;
        }
    }

    protected boolean adjustHandlePos(int[] coordinate, HandleView handleView, Layout layout, int offset, int line) {
        boolean insertionFlag = handleView instanceof InsertionHandleView;
        boolean selectionEndFlag = handleView instanceof SelectionHandleView;
        if (selectionEndFlag) {
            selectionEndFlag = ((SelectionHandleView) handleView).isStartHandle() ^ 1;
        }
        int prePosX = (int) layout.getPrimaryHorizontal(offset);
        int prePosY = layout.getLineBottom(line);
        if (line > 0 && this.mPosIsLineEnd && insertionFlag) {
            coordinate[0] = (coordinate[0] - prePosX) + ((int) layout.getLineMax(line - 1));
            coordinate[1] = (coordinate[1] - prePosY) + layout.getLineBottom(line - 1);
            this.mCursorShouldMoveEnd = true;
            if (offset == this.mPreOffset) {
                updateCursorsPositions();
            }
            return true;
        } else if (line <= 0 || !selectionEndFlag || offset != layout.getLineStart(line)) {
            return false;
        } else {
            this.mPreOffset = -1;
            coordinate[0] = (((coordinate[0] - prePosX) + this.mTextView.getWidth()) - this.mTextView.getCompoundPaddingLeft()) - this.mTextView.getCompoundPaddingRight();
            coordinate[1] = (coordinate[1] - prePosY) + layout.getLineBottom(line - 1);
            return true;
        }
    }

    protected void setPosWithMotionEvent(MotionEvent event, boolean isTouchPos) {
        int[] location = new int[2];
        this.mTextView.getLocationOnScreen(location);
        setPos(event.getRawX() - ((float) location[0]), isTouchPos);
    }

    protected boolean adjustCursorPos(int line, Layout layout) {
        if (line <= 0 || !this.mCursorShouldMoveEnd) {
            this.mPreOffset = this.mTextView.getSelectionStart();
            return false;
        }
        this.mCursorTop = layout.getLineTop(line - 1);
        this.mCursorBottom = layout.getLineTop(line);
        this.mCursorX = layout.getLineMax(line - 1);
        this.mCursorShouldMoveEnd = false;
        this.mPreOffset = this.mTextView.getSelectionStart();
        return true;
    }

    protected int getCursorTop() {
        return this.mCursorTop;
    }

    protected int getCursorBottom() {
        return this.mCursorBottom;
    }

    protected float getCursorX() {
        return this.mCursorX;
    }

    protected void setPosIsLineEnd(boolean flag) {
        this.mPosIsLineEnd = flag;
    }

    protected void adjustSelectionBounds(RectF selectionBounds, int line, Layout layout, int handleHeight) {
        if (line > 0 && this.mPosIsLineEnd) {
            float primaryHorizontal = layout.getLineMax(line - 1);
            selectionBounds.set(primaryHorizontal, (float) layout.getLineTop(line - 1), primaryHorizontal, (float) (layout.getLineTop(line) + handleHeight));
        }
    }

    protected void recogniseLineEnd() {
        int offset = this.mTextView.getSelectionEnd();
        Layout layout = this.mTextView.getLayout();
        if (-1.0f != this.mTouchPosX && layout != null) {
            int currLine = layout.getLineForOffset(offset);
            if (offset == layout.getLineStart(currLine) && currLine > 0) {
                float start = (float) (((int) layout.getPrimaryHorizontal(offset - 1)) + this.mTextView.getCompoundPaddingLeft());
                if (this.mTouchPosX >= start + ((((float) (((int) layout.getLineMax(currLine - 1)) + this.mTextView.getCompoundPaddingLeft())) - start) * WORD_END_SCALE)) {
                    this.mPosIsLineEnd = true;
                } else {
                    this.mPosIsLineEnd = false;
                }
            }
            this.mTouchPosX = -1.0f;
        }
    }
}
