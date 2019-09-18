package huawei.com.android.internal.widget;

import android.graphics.RectF;
import android.provider.Settings;
import android.text.Layout;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.Editor;
import android.widget.TextView;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HwEditor extends Editor {
    private static final String TAG = "HwEditor";
    private static final int VIBRATOR_SWITCH_OFF = 0;
    private static final float WORD_END_SCALE = 0.55f;
    private static String mCursorMoveVibrateType;
    private static boolean mIsSupportCursorMoveVibrator;
    private static boolean mIsSupportLongClickVibrator;
    private static String mLongClickVibrateType;
    private static Object mVibratorEx;
    private ChangeWatcher mChangeWatcher;
    private int mCursorBottom = -1;
    private boolean mCursorShouldMoveEnd = false;
    private int mCursorTop = -1;
    private float mCursorX = -1.0f;
    private boolean mHasSelectionLastTime = false;
    private float mInsertHanlePosX = -1.0f;
    private boolean mPosIsLineEnd = false;
    private int mPreOffset = -1;
    private boolean mShouldVibrate;
    private TextView mTextView;
    private float mTouchPosX = -1.0f;

    private class ChangeWatcher implements SpanWatcher {
        private ChangeWatcher() {
        }

        public void onSpanAdded(Spannable text, Object what, int start, int end) {
        }

        public void onSpanRemoved(Spannable text, Object what, int start, int end) {
        }

        public void onSpanChanged(Spannable text, Object what, int ostart, int oend, int nstart, int nend) {
            if (Selection.SELECTION_END == what) {
                HwEditor.this.onSelectionChanged();
            }
        }
    }

    public HwEditor(TextView textView) {
        super(textView);
        onInit(textView);
    }

    public void selectAllAndShowEditor() {
        stopTextActionMode();
        selectAllText();
        startSelectionActionModeAsync(false);
        showFloatingToolbar();
    }

    /* access modifiers changed from: protected */
    public int adjustOffsetAtLineEndForTouchPos(int offset) {
        return adjustOffsetAtLineEnd(offset, true);
    }

    /* access modifiers changed from: protected */
    public int adjustOffsetAtLineEndForInsertHanlePos(int offset) {
        return adjustOffsetAtLineEnd(offset, false);
    }

    private int adjustOffsetAtLineEnd(int offset, boolean isTouchPos) {
        Layout layout = this.mTextView.getLayout();
        if (!(-1.0f == getPos(isTouchPos) || layout == null)) {
            int currLine = layout.getLineForOffset(offset);
            boolean offsetNearEnd = offset == layout.getLineEnd(currLine) - 1 && currLine != layout.getLineCount() - 1;
            float start = -1.0f;
            float end = -1.0f;
            if (offsetNearEnd) {
                start = layout.getPrimaryHorizontal(offset) + ((float) this.mTextView.getCompoundPaddingLeft());
                end = layout.getLineMax(currLine) + ((float) this.mTextView.getCompoundPaddingLeft());
            } else if (offset == layout.getLineStart(currLine) && currLine > 0) {
                start = layout.getPrimaryHorizontal(offset - 1) + ((float) this.mTextView.getCompoundPaddingLeft());
                end = layout.getLineMax(currLine - 1) + ((float) this.mTextView.getCompoundPaddingLeft());
            }
            float XThresholdToSnap = ((end - start) * WORD_END_SCALE) + start;
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
                updateCursorPosition();
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

    /* access modifiers changed from: protected */
    public boolean adjustHandlePos(int[] coordinate, Editor.HandleView handleView, Layout layout, int offset, int line) {
        boolean insertionFlag = handleView instanceof Editor.InsertionHandleView;
        boolean selectionEndFlag = handleView instanceof Editor.SelectionHandleView;
        if (selectionEndFlag) {
            selectionEndFlag = !((Editor.SelectionHandleView) handleView).isStartHandle();
        }
        int prePosX = (int) layout.getPrimaryHorizontal(offset);
        int prePosY = layout.getLineBottom(line);
        if (line > 0 && this.mPosIsLineEnd && insertionFlag) {
            coordinate[0] = (coordinate[0] - prePosX) + ((int) layout.getLineMax(line - 1));
            coordinate[1] = (coordinate[1] - prePosY) + layout.getLineBottom(line - 1);
            this.mCursorShouldMoveEnd = true;
            if (offset == this.mPreOffset) {
                updateCursorPosition();
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

    /* access modifiers changed from: protected */
    public void setPosWithMotionEvent(MotionEvent event, boolean isTouchPos) {
        int[] location = new int[2];
        this.mTextView.getLocationOnScreen(location);
        setPos(event.getRawX() - ((float) location[0]), isTouchPos);
    }

    /* access modifiers changed from: protected */
    public boolean adjustCursorPos(int line, Layout layout) {
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

    /* access modifiers changed from: protected */
    public int getCursorTop() {
        return this.mCursorTop;
    }

    /* access modifiers changed from: protected */
    public int getCursorBottom() {
        return this.mCursorBottom;
    }

    /* access modifiers changed from: protected */
    public float getCursorX() {
        return this.mCursorX;
    }

    /* access modifiers changed from: protected */
    public void setPosIsLineEnd(boolean flag) {
        this.mPosIsLineEnd = flag;
    }

    /* access modifiers changed from: protected */
    public void adjustSelectionBounds(RectF selectionBounds, int line, Layout layout, int handleHeight) {
        if (line > 0 && this.mPosIsLineEnd) {
            float primaryHorizontal = layout.getLineMax(line - 1);
            selectionBounds.set(primaryHorizontal, (float) layout.getLineTop(line - 1), primaryHorizontal, (float) (layout.getLineTop(line) + handleHeight));
        }
    }

    /* access modifiers changed from: protected */
    public void recogniseLineEnd() {
        int offset = this.mTextView.getSelectionEnd();
        Layout layout = this.mTextView.getLayout();
        if (-1.0f != this.mTouchPosX && layout != null) {
            int currLine = layout.getLineForOffset(offset);
            if (offset == layout.getLineStart(currLine) && currLine > 0) {
                float start = (float) (((int) layout.getPrimaryHorizontal(offset - 1)) + this.mTextView.getCompoundPaddingLeft());
                if (this.mTouchPosX >= ((((float) (((int) layout.getLineMax(currLine - 1)) + this.mTextView.getCompoundPaddingLeft())) - start) * WORD_END_SCALE) + start) {
                    this.mPosIsLineEnd = true;
                } else {
                    this.mPosIsLineEnd = false;
                }
            }
            this.mTouchPosX = -1.0f;
        }
    }

    /* access modifiers changed from: protected */
    public void onInit(TextView textView) {
        this.mTextView = textView;
        this.mShouldVibrate = textView instanceof EditText;
        if (mVibratorEx == null) {
            try {
                Class<?> clazz = Class.forName("com.huawei.android.os.VibratorEx");
                mVibratorEx = clazz.newInstance();
                mLongClickVibrateType = (String) clazz.getDeclaredField("HW_VIBRATOR_TYPE_CONTROL_TEXT_EDIT").get(null);
                mCursorMoveVibrateType = (String) clazz.getDeclaredField("HW_VIBRATOR_TYPE_CONTROL_TEXT_CHOOSE_CURSOR_MOVE").get(null);
                Method isSupportHwVibrator = clazz.getDeclaredMethod("isSupportHwVibrator", new Class[]{String.class});
                mIsSupportLongClickVibrator = ((Boolean) isSupportHwVibrator.invoke(mVibratorEx, new Object[]{mLongClickVibrateType})).booleanValue();
                mIsSupportCursorMoveVibrator = ((Boolean) isSupportHwVibrator.invoke(mVibratorEx, new Object[]{mCursorMoveVibrateType})).booleanValue();
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Not found VibratorEx");
            } catch (IllegalAccessException e2) {
                Log.e(TAG, "Illegal access isSupportHwVibrator");
            } catch (InstantiationException e3) {
                Log.e(TAG, "VibratorEx instantiation error");
            } catch (NoSuchMethodException e4) {
                Log.e(TAG, "Not found method isSupportHwVibrator");
            } catch (InvocationTargetException e5) {
                Log.e(TAG, "Invoke method isSupportHwVibrator error");
            } catch (NoSuchFieldException e6) {
                Log.e(TAG, "Not found field HW_VIBRATOR_TYPE_CONTROL_TEXT_EDIT");
            }
        }
    }

    private void vibrate(boolean isMoveType) {
        String type = isMoveType ? mCursorMoveVibrateType : mLongClickVibrateType;
        if (!isVibratorSwitchOn()) {
            this.mTextView.setHapticFeedbackEnabled(false);
        } else if (mIsSupportCursorMoveVibrator || mIsSupportLongClickVibrator) {
            this.mTextView.setHapticFeedbackEnabled(false);
            try {
                mVibratorEx.getClass().getDeclaredMethod("setHwVibrator", new Class[]{String.class}).invoke(mVibratorEx, new Object[]{type});
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "Not found method setHwVibrator");
            } catch (IllegalAccessException e2) {
                Log.e(TAG, "Illegal access setHwVibrator");
            } catch (InvocationTargetException e3) {
                Log.e(TAG, "Invoke method setHwVibrator error");
            }
        } else {
            this.mTextView.setHapticFeedbackEnabled(true);
        }
    }

    private boolean isVibratorSwitchOn() {
        return Settings.System.getInt(this.mTextView.getContext().getContentResolver(), "haptic_feedback_enabled", 0) != 0;
    }

    private boolean isCursorHandleVisible() {
        boolean z = false;
        try {
            Field insertionPointCursorController = Editor.class.getDeclaredField("mInsertionPointCursorController");
            insertionPointCursorController.setAccessible(true);
            Object insertionCursorController = insertionPointCursorController.get(this);
            if (insertionCursorController == null) {
                return false;
            }
            Class<?> clazz = insertionCursorController.getClass();
            Method isActive = clazz.getDeclaredMethod("isActive", new Class[0]);
            Method isCursorBeingModified = clazz.getDeclaredMethod("isCursorBeingModified", new Class[0]);
            if (((Boolean) isActive.invoke(insertionCursorController, new Object[0])).booleanValue() && ((Boolean) isCursorBeingModified.invoke(insertionCursorController, new Object[0])).booleanValue()) {
                z = true;
            }
            return z;
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "Not found method isCursorBeingModified");
            return false;
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "Illegal access isCursorBeingModified");
            return false;
        } catch (InvocationTargetException e3) {
            Log.e(TAG, "Invoke method isCursorBeingModified error");
            return false;
        } catch (NoSuchFieldException e4) {
            Log.e(TAG, "Not found field mInsertionPointCursorController");
            return false;
        }
    }

    private boolean hasSelection() {
        int start = this.mTextView.getSelectionStart();
        int end = this.mTextView.getSelectionEnd();
        return start >= 0 && end >= 0 && start != end;
    }

    /* access modifiers changed from: protected */
    public void onSelectionChanged() {
        if (this.mTextView.hasFocus()) {
            boolean hasSelection = hasSelection();
            if (hasSelection != this.mHasSelectionLastTime) {
                this.mHasSelectionLastTime = hasSelection;
                return;
            }
            if (this.mShouldVibrate && (hasSelection || isCursorHandleVisible())) {
                vibrate(true);
            }
        }
    }

    public boolean performLongClick(boolean handled) {
        vibrate(false);
        this.mShouldVibrate = false;
        boolean result = HwEditor.super.performLongClick(handled);
        this.mShouldVibrate = true;
        return result;
    }

    public void addSpanWatchers(Spannable text) {
        HwEditor.super.addSpanWatchers(text);
        if (this.mTextView instanceof EditText) {
            for (ChangeWatcher watcher : (ChangeWatcher[]) text.getSpans(0, text.length(), ChangeWatcher.class)) {
                text.removeSpan(watcher);
            }
            if (this.mChangeWatcher == null) {
                this.mChangeWatcher = new ChangeWatcher();
            }
            text.setSpan(this.mChangeWatcher, 0, text.length(), 18);
        }
    }

    public void beginBatchEdit() {
        HwEditor.super.beginBatchEdit();
        this.mShouldVibrate = false;
    }

    public void endBatchEdit() {
        HwEditor.super.endBatchEdit();
        this.mShouldVibrate = true;
    }
}
