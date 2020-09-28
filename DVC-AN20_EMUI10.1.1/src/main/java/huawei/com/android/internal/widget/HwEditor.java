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
    private static final int DEFAULT_VALUE = -1;
    private static final int MAX_LOCATION = 2;
    private static final String TAG = "HwEditor";
    private static final int TOUCH_POS = -1;
    private static final int VIBRATOR_SWITCH_OFF = 0;
    private static final float WORD_END_SCALE = 0.55f;
    private static String sCursorMoveVibrateType;
    private static boolean sIsSupportCursorMoveVibrator;
    private static boolean sIsSupportLongClickVibrator;
    private static String sLongClickVibrateType;
    private static Object sVibratorEx;
    private ChangeWatcher mChangeWatcher;
    private int mCursorBottom = -1;
    private int mCursorTop = -1;
    private float mCursorX = -1.0f;
    private boolean mHasSelectionLastTime = false;
    private float mInsertHandlePosX = -1.0f;
    private boolean mIsPosLineEnd = false;
    private int mPreOffset = -1;
    private boolean mShouldCursorMoveEnd = false;
    private boolean mShouldVibrate;
    private TextView mTextView;
    private float mTouchPosX = -1.0f;

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

    private int adjustOffsetAtLineEnd(int adjustOffset, boolean isTouchPos) {
        float end;
        float start;
        int offset = adjustOffset;
        Layout layout = this.mTextView.getLayout();
        if (!(getPos(isTouchPos) == -1.0f || layout == null)) {
            int currentLine = layout.getLineForOffset(offset);
            boolean isOffsetNearEnd = offset == layout.getLineEnd(currentLine) - 1 && currentLine != layout.getLineCount() - 1;
            if (isOffsetNearEnd) {
                start = layout.getPrimaryHorizontal(offset) + ((float) this.mTextView.getCompoundPaddingLeft());
                end = layout.getLineMax(currentLine) + ((float) this.mTextView.getCompoundPaddingLeft());
            } else if (offset != layout.getLineStart(currentLine) || currentLine <= 0) {
                start = -1.0f;
                end = -1.0f;
            } else {
                start = layout.getPrimaryHorizontal(offset - 1) + ((float) this.mTextView.getCompoundPaddingLeft());
                end = layout.getLineMax(currentLine - 1) + ((float) this.mTextView.getCompoundPaddingLeft());
            }
            float thresholdToSnapX = ((end - start) * WORD_END_SCALE) + start;
            if ((!isOffsetNearEnd && !this.mIsPosLineEnd) || end <= start || getPos(isTouchPos) < thresholdToSnapX) {
                this.mIsPosLineEnd = false;
            } else if (isOffsetNearEnd) {
                offset = layout.getLineEnd(currentLine);
                this.mIsPosLineEnd = true;
            }
            setPos(-1.0f, isTouchPos);
        }
        if (isTouchPos) {
            this.mShouldCursorMoveEnd = this.mIsPosLineEnd;
            if (offset == this.mPreOffset) {
                updateCursorPosition();
            }
        }
        return offset;
    }

    private float getPos(boolean isTouchPos) {
        return isTouchPos ? this.mTouchPosX : this.mInsertHandlePosX;
    }

    private void setPos(float pos, boolean isTouchPos) {
        if (isTouchPos) {
            this.mTouchPosX = pos;
        } else {
            this.mInsertHandlePosX = pos;
        }
    }

    /* access modifiers changed from: protected */
    public boolean adjustHandlePos(int[] coordinates, Editor.HandleView handleView, Layout layout, int offset, int line) {
        boolean isInsertionFlag = handleView instanceof Editor.InsertionHandleView;
        boolean isSelectionEndFlag = handleView instanceof Editor.SelectionHandleView;
        if (isSelectionEndFlag) {
            isSelectionEndFlag = !((Editor.SelectionHandleView) handleView).isStartHandle();
        }
        int prePosX = (int) layout.getPrimaryHorizontal(offset);
        int prePosY = layout.getLineBottomWithoutSpacing(line);
        if (line > 0 && this.mIsPosLineEnd && isInsertionFlag) {
            coordinates[0] = coordinates[0] + (-prePosX) + ((int) layout.getLineMax(line - 1));
            coordinates[1] = coordinates[1] + (-prePosY) + layout.getLineBottomWithoutSpacing(line - 1);
            this.mShouldCursorMoveEnd = true;
            if (offset == this.mPreOffset) {
                updateCursorPosition();
            }
            return true;
        } else if (line <= 0 || !isSelectionEndFlag || offset != layout.getLineStart(line)) {
            return false;
        } else {
            this.mPreOffset = -1;
            coordinates[0] = coordinates[0] + ((((-prePosX) + this.mTextView.getWidth()) - this.mTextView.getCompoundPaddingLeft()) - this.mTextView.getCompoundPaddingRight());
            coordinates[1] = coordinates[1] + (-prePosY) + layout.getLineBottomWithoutSpacing(line - 1);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void setPosWithMotionEvent(MotionEvent event, boolean isTouchPos) {
        int[] locations = new int[2];
        this.mTextView.getLocationOnScreen(locations);
        setPos(event.getRawX() - ((float) locations[0]), isTouchPos);
    }

    /* access modifiers changed from: protected */
    public boolean adjustCursorPos(int line, Layout layout) {
        if (line <= 0 || !this.mShouldCursorMoveEnd) {
            this.mPreOffset = this.mTextView.getSelectionStart();
            return false;
        }
        this.mCursorTop = layout.getLineTop(line - 1);
        this.mCursorBottom = layout.getLineBottomWithoutSpacing(line - 1);
        this.mCursorX = layout.getLineMax(line - 1);
        this.mShouldCursorMoveEnd = false;
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
    public void setPosIsLineEnd(boolean isLineEnd) {
        this.mIsPosLineEnd = isLineEnd;
    }

    /* access modifiers changed from: protected */
    public void adjustSelectionBounds(RectF selectionBounds, int line, Layout layout, int handleHeight) {
        if (line > 0 && this.mIsPosLineEnd) {
            float primaryHorizontal = layout.getLineMax(line - 1);
            selectionBounds.set(primaryHorizontal, (float) layout.getLineTop(line - 1), primaryHorizontal, (float) (layout.getLineTop(line) + handleHeight));
        }
    }

    /* access modifiers changed from: protected */
    public void recogniseLineEnd() {
        int offset = this.mTextView.getSelectionEnd();
        Layout layout = this.mTextView.getLayout();
        if (this.mTouchPosX != -1.0f && layout != null) {
            int currentLine = layout.getLineForOffset(offset);
            if (offset == layout.getLineStart(currentLine) && currentLine > 0) {
                float start = (float) (((int) layout.getPrimaryHorizontal(offset - 1)) + this.mTextView.getCompoundPaddingLeft());
                this.mIsPosLineEnd = this.mTouchPosX >= ((((float) (((int) layout.getLineMax(currentLine + -1)) + this.mTextView.getCompoundPaddingLeft())) - start) * WORD_END_SCALE) + start;
            }
            this.mTouchPosX = -1.0f;
        }
    }

    private void onInit(TextView textView) {
        this.mTextView = textView;
        this.mShouldVibrate = textView instanceof EditText;
        if (sVibratorEx == null) {
            try {
                Class<?> clazz = Class.forName("com.huawei.android.os.VibratorEx");
                sVibratorEx = clazz.newInstance();
                sLongClickVibrateType = (String) clazz.getDeclaredField("HW_VIBRATOR_TYPE_CONTROL_TEXT_EDIT").get(null);
                sCursorMoveVibrateType = (String) clazz.getDeclaredField("HW_VIBRATOR_TYPE_CONTROL_TEXT_CHOOSE_CURSOR_MOVE").get(null);
                Method isSupportHwVibrator = clazz.getDeclaredMethod("isSupportHwVibrator", String.class);
                sIsSupportLongClickVibrator = ((Boolean) isSupportHwVibrator.invoke(sVibratorEx, sLongClickVibrateType)).booleanValue();
                sIsSupportCursorMoveVibrator = ((Boolean) isSupportHwVibrator.invoke(sVibratorEx, sCursorMoveVibrateType)).booleanValue();
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
        String type = isMoveType ? sCursorMoveVibrateType : sLongClickVibrateType;
        if (!isVibratorSwitchOn()) {
            this.mTextView.setHapticFeedbackEnabled(false);
        } else if (sIsSupportCursorMoveVibrator || sIsSupportLongClickVibrator) {
            this.mTextView.setHapticFeedbackEnabled(false);
            try {
                sVibratorEx.getClass().getDeclaredMethod("setHwVibrator", String.class).invoke(sVibratorEx, type);
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
            if (!((Boolean) isActive.invoke(insertionCursorController, new Object[0])).booleanValue() || !((Boolean) isCursorBeingModified.invoke(insertionCursorController, new Object[0])).booleanValue()) {
                return false;
            }
            return true;
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
            } else if (!this.mShouldVibrate) {
            } else {
                if (hasSelection || isCursorHandleVisible()) {
                    vibrate(true);
                }
            }
        }
    }

    public boolean performLongClick(boolean isHandled) {
        vibrate(false);
        this.mShouldVibrate = false;
        boolean isLongClickPerformed = HwEditor.super.performLongClick(isHandled);
        this.mShouldVibrate = true;
        return isLongClickPerformed;
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

    private class ChangeWatcher implements SpanWatcher {
        private ChangeWatcher() {
        }

        public void onSpanAdded(Spannable text, Object what, int start, int end) {
        }

        public void onSpanRemoved(Spannable text, Object what, int start, int end) {
        }

        public void onSpanChanged(Spannable text, Object what, int oldStart, int oldEnd, int newStart, int newEnd) {
            if (what == Selection.SELECTION_END) {
                HwEditor.this.onSelectionChanged();
            }
        }
    }
}
