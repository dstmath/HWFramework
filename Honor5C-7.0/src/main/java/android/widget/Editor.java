package android.widget;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.UndoManager;
import android.content.UndoOperation;
import android.content.UndoOwner;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable.ClassLoaderCreator;
import android.os.ParcelableParcel;
import android.os.SystemClock;
import android.text.DynamicLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.ParcelableSpan;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.method.KeyListener;
import android.text.method.MetaKeyKeyListener;
import android.text.method.MovementMethod;
import android.text.method.WordIterator;
import android.text.style.EasyEditSpan;
import android.text.style.ImageSpan;
import android.text.style.SuggestionRangeSpan;
import android.text.style.SuggestionSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.ActionMode.Callback2;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.DisplayListCanvas;
import android.view.DragAndDropPermissions;
import android.view.DragEvent;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.RenderNode;
import android.view.SubMenu;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.CursorAnchorInfo.Builder;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.AsyncService;
import com.android.internal.util.GrowingArrayUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.widget.AutoScrollHelper;
import com.huawei.pgmng.log.LogPower;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;

public class Editor {
    static final int BLINK = 500;
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_UNDO = false;
    private static int DRAG_SHADOW_MAX_TEXT_LENGTH = 0;
    static final int EXTRACT_NOTHING = -2;
    static final int EXTRACT_UNKNOWN = -1;
    public static final int HANDLE_TYPE_SELECTION_END = 1;
    public static final int HANDLE_TYPE_SELECTION_START = 0;
    private static final float LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS = 0.5f;
    private static final int MENU_ITEM_ORDER_COPY = 5;
    private static final int MENU_ITEM_ORDER_CUT = 4;
    private static final int MENU_ITEM_ORDER_PASTE = 6;
    private static final int MENU_ITEM_ORDER_PASTE_AS_PLAIN_TEXT = 7;
    private static final int MENU_ITEM_ORDER_PROCESS_TEXT_INTENT_ACTIONS_START = 10;
    private static final int MENU_ITEM_ORDER_REDO = 3;
    private static final int MENU_ITEM_ORDER_REPLACE = 9;
    private static final int MENU_ITEM_ORDER_SELECT_ALL = 1;
    private static final int MENU_ITEM_ORDER_SHARE = 8;
    private static final int MENU_ITEM_ORDER_UNDO = 2;
    private static final int OFFSET_ON_IMAGE_SPAN = 1;
    private static final String TAG = "Editor";
    private static final int TAP_STATE_DOUBLE_TAP = 2;
    private static final int TAP_STATE_FIRST_TAP = 1;
    private static final int TAP_STATE_INITIAL = 0;
    private static final int TAP_STATE_TRIPLE_CLICK = 3;
    private static final float[] TEMP_POSITION = null;
    private static final String UNDO_OWNER_TAG = "Editor";
    private static final int UNSET_LINE = -1;
    private static final int UNSET_X_VALUE = -1;
    boolean mAllowUndo;
    Blink mBlink;
    private float mContextMenuAnchorX;
    private float mContextMenuAnchorY;
    CorrectionHighlighter mCorrectionHighlighter;
    boolean mCreatedWithASelection;
    final CursorAnchorInfoNotifier mCursorAnchorInfoNotifier;
    int mCursorCount;
    final Drawable[] mCursorDrawable;
    boolean mCursorVisible;
    Callback mCustomInsertionActionModeCallback;
    Callback mCustomSelectionActionModeCallback;
    boolean mDiscardNextActionUp;
    CharSequence mError;
    ErrorPopup mErrorPopup;
    boolean mErrorWasChanged;
    boolean mFrozenWithFocus;
    boolean mIgnoreActionUpEvent;
    private boolean mIgnoreNextMouseActionUpOrDown;
    boolean mInBatchEditControllers;
    InputContentType mInputContentType;
    InputMethodState mInputMethodState;
    int mInputType;
    private Runnable mInsertionActionModeRunnable;
    boolean mInsertionControllerEnabled;
    InsertionPointCursorController mInsertionPointCursorController;
    boolean mIsBeingLongClicked;
    boolean mIsInsertionActionModeStartPending;
    KeyListener mKeyListener;
    private int mLastButtonState;
    float mLastDownPositionX;
    float mLastDownPositionY;
    private long mLastTouchUpTime;
    private final OnMenuItemClickListener mOnContextMenuItemClickListener;
    private PositionListener mPositionListener;
    private boolean mPreserveSelection;
    final ProcessTextIntentActionsHandler mProcessTextIntentActionsHandler;
    private boolean mRestartActionModeOnNextRefresh;
    boolean mSelectAllOnFocus;
    private Drawable mSelectHandleCenter;
    private Drawable mSelectHandleLeft;
    private Drawable mSelectHandleRight;
    boolean mSelectionControllerEnabled;
    SelectionModifierCursorController mSelectionModifierCursorController;
    boolean mSelectionMoved;
    long mShowCursor;
    boolean mShowErrorAfterAttach;
    private final Runnable mShowFloatingToolbar;
    boolean mShowSoftInputOnFocus;
    Runnable mShowSuggestionRunnable;
    private SpanController mSpanController;
    SpellChecker mSpellChecker;
    private final SuggestionHelper mSuggestionHelper;
    SuggestionRangeSpan mSuggestionRangeSpan;
    SuggestionsPopupWindow mSuggestionsPopupWindow;
    private int mTapState;
    private Rect mTempRect;
    ActionMode mTextActionMode;
    boolean mTextIsSelectable;
    TextRenderNode[] mTextRenderNodes;
    protected TextView mTextView;
    boolean mTouchFocusSelected;
    final UndoInputFilter mUndoInputFilter;
    private final UndoManager mUndoManager;
    private UndoOwner mUndoOwner;
    private boolean mUpdateWordIteratorText;
    WordIterator mWordIterator;
    private WordIterator mWordIteratorWithText;

    /* renamed from: android.widget.Editor.4 */
    class AnonymousClass4 implements OnMenuItemClickListener {
        final /* synthetic */ SuggestionInfo val$info;

        AnonymousClass4(SuggestionInfo val$info) {
            this.val$info = val$info;
        }

        public boolean onMenuItemClick(MenuItem item) {
            Editor.this.replaceWithSuggestion(this.val$info);
            return true;
        }
    }

    private class Blink implements Runnable {
        private boolean mCancelled;

        private Blink() {
        }

        public void run() {
            if (!this.mCancelled) {
                Editor.this.mTextView.removeCallbacks(this);
                if (Editor.this.shouldBlink()) {
                    if (Editor.this.mTextView.getLayout() != null) {
                        Editor.this.mTextView.invalidateCursorPath();
                    }
                    Editor.this.mTextView.postDelayed(this, 500);
                }
            }
        }

        void cancel() {
            if (!this.mCancelled) {
                Editor.this.mTextView.removeCallbacks(this);
                this.mCancelled = true;
            }
        }

        void uncancel() {
            this.mCancelled = Editor.DEBUG_UNDO;
        }
    }

    private class CorrectionHighlighter {
        private static final int FADE_OUT_DURATION = 400;
        private int mEnd;
        private long mFadingStartTime;
        private final Paint mPaint;
        private final Path mPath;
        private int mStart;
        private RectF mTempRectF;

        public CorrectionHighlighter() {
            this.mPath = new Path();
            this.mPaint = new Paint(Editor.TAP_STATE_FIRST_TAP);
            this.mPaint.setCompatibilityScaling(Editor.this.mTextView.getResources().getCompatibilityInfo().applicationScale);
            this.mPaint.setStyle(Style.FILL);
        }

        public void highlight(CorrectionInfo info) {
            this.mStart = info.getOffset();
            this.mEnd = this.mStart + info.getNewText().length();
            this.mFadingStartTime = SystemClock.uptimeMillis();
            if (this.mStart < 0 || this.mEnd < 0) {
                stopAnimation();
            }
        }

        public void draw(Canvas canvas, int cursorOffsetVertical) {
            if (updatePath() && updatePaint()) {
                if (cursorOffsetVertical != 0) {
                    canvas.translate(0.0f, (float) cursorOffsetVertical);
                }
                canvas.drawPath(this.mPath, this.mPaint);
                if (cursorOffsetVertical != 0) {
                    canvas.translate(0.0f, (float) (-cursorOffsetVertical));
                }
                invalidate(true);
                return;
            }
            stopAnimation();
            invalidate(Editor.DEBUG_UNDO);
        }

        private boolean updatePaint() {
            long duration = SystemClock.uptimeMillis() - this.mFadingStartTime;
            if (duration > 400) {
                return Editor.DEBUG_UNDO;
            }
            this.mPaint.setColor((Editor.this.mTextView.mHighlightColor & AsyncService.CMD_ASYNC_SERVICE_ON_START_INTENT) + (((int) (((float) Color.alpha(Editor.this.mTextView.mHighlightColor)) * (LayoutParams.BRIGHTNESS_OVERRIDE_FULL - (((float) duration) / 400.0f)))) << 24));
            return true;
        }

        private boolean updatePath() {
            Layout layout = Editor.this.mTextView.getLayout();
            if (layout == null) {
                return Editor.DEBUG_UNDO;
            }
            int length = Editor.this.mTextView.getText().length();
            int start = Math.min(length, this.mStart);
            int end = Math.min(length, this.mEnd);
            this.mPath.reset();
            layout.getSelectionPath(start, end, this.mPath);
            return true;
        }

        private void invalidate(boolean delayed) {
            if (Editor.this.mTextView.getLayout() != null) {
                if (this.mTempRectF == null) {
                    this.mTempRectF = new RectF();
                }
                this.mPath.computeBounds(this.mTempRectF, Editor.DEBUG_UNDO);
                int left = Editor.this.mTextView.getCompoundPaddingLeft();
                int top = Editor.this.mTextView.getExtendedPaddingTop() + Editor.this.mTextView.getVerticalOffset(true);
                if (delayed) {
                    Editor.this.mTextView.postInvalidateOnAnimation(((int) this.mTempRectF.left) + left, ((int) this.mTempRectF.top) + top, ((int) this.mTempRectF.right) + left, ((int) this.mTempRectF.bottom) + top);
                } else {
                    Editor.this.mTextView.postInvalidate((int) this.mTempRectF.left, (int) this.mTempRectF.top, (int) this.mTempRectF.right, (int) this.mTempRectF.bottom);
                }
            }
        }

        private void stopAnimation() {
            Editor.this.mCorrectionHighlighter = null;
        }
    }

    private interface TextViewPositionListener {
        void updatePosition(int i, int i2, boolean z, boolean z2);
    }

    private final class CursorAnchorInfoNotifier implements TextViewPositionListener {
        final Builder mSelectionInfoBuilder;
        final int[] mTmpIntOffset;
        final Matrix mViewToScreenMatrix;

        private CursorAnchorInfoNotifier() {
            this.mSelectionInfoBuilder = new Builder();
            this.mTmpIntOffset = new int[Editor.TAP_STATE_DOUBLE_TAP];
            this.mViewToScreenMatrix = new Matrix();
        }

        public void updatePosition(int parentPositionX, int parentPositionY, boolean parentPositionChanged, boolean parentScrolled) {
            InputMethodState ims = Editor.this.mInputMethodState;
            if (ims != null && ims.mBatchEditNesting <= 0) {
                InputMethodManager imm = InputMethodManager.peekInstance();
                if (imm != null) {
                    if (imm.isActive(Editor.this.mTextView) && imm.isCursorAnchorInfoEnabled()) {
                        Layout layout = Editor.this.mTextView.getLayout();
                        if (layout != null) {
                            int line;
                            int offset;
                            Builder builder = this.mSelectionInfoBuilder;
                            builder.reset();
                            int selectionStart = Editor.this.mTextView.getSelectionStart();
                            builder.setSelectionRange(selectionStart, Editor.this.mTextView.getSelectionEnd());
                            this.mViewToScreenMatrix.set(Editor.this.mTextView.getMatrix());
                            Editor.this.mTextView.getLocationOnScreen(this.mTmpIntOffset);
                            this.mViewToScreenMatrix.postTranslate((float) this.mTmpIntOffset[Editor.TAP_STATE_INITIAL], (float) this.mTmpIntOffset[Editor.TAP_STATE_FIRST_TAP]);
                            builder.setMatrix(this.mViewToScreenMatrix);
                            float viewportToContentHorizontalOffset = (float) Editor.this.mTextView.viewportToContentHorizontalOffset();
                            float viewportToContentVerticalOffset = (float) Editor.this.mTextView.viewportToContentVerticalOffset();
                            CharSequence text = Editor.this.mTextView.getText();
                            if (text instanceof Spannable) {
                                Spannable sp = (Spannable) text;
                                int composingTextStart = BaseInputConnection.getComposingSpanStart(sp);
                                int composingTextEnd = BaseInputConnection.getComposingSpanEnd(sp);
                                if (composingTextEnd < composingTextStart) {
                                    int temp = composingTextEnd;
                                    composingTextEnd = composingTextStart;
                                    composingTextStart = temp;
                                }
                                boolean hasComposingText = (composingTextStart < 0 || composingTextStart >= composingTextEnd) ? Editor.DEBUG_UNDO : true;
                                if (hasComposingText) {
                                    builder.setComposingText(composingTextStart, text.subSequence(composingTextStart, composingTextEnd));
                                    int minLine = layout.getLineForOffset(composingTextStart);
                                    int maxLine = layout.getLineForOffset(composingTextEnd + Editor.UNSET_X_VALUE);
                                    for (line = minLine; line <= maxLine; line += Editor.TAP_STATE_FIRST_TAP) {
                                        int lineStart = layout.getLineStart(line);
                                        int lineEnd = layout.getLineEnd(line);
                                        int offsetStart = Math.max(lineStart, composingTextStart);
                                        int offsetEnd = Math.min(lineEnd, composingTextEnd);
                                        boolean ltrLine = layout.getParagraphDirection(line) == Editor.TAP_STATE_FIRST_TAP ? true : Editor.DEBUG_UNDO;
                                        float[] widths = new float[(offsetEnd - offsetStart)];
                                        layout.getPaint().getTextWidths(text, offsetStart, offsetEnd, widths);
                                        float top = (float) layout.getLineTop(line);
                                        float bottom = (float) layout.getLineBottom(line);
                                        for (offset = offsetStart; offset < offsetEnd; offset += Editor.TAP_STATE_FIRST_TAP) {
                                            float left;
                                            float right;
                                            float charWidth = widths[offset - offsetStart];
                                            boolean isRtl = layout.isRtlCharAt(offset);
                                            float primary = layout.getPrimaryHorizontal(offset);
                                            float secondary = layout.getSecondaryHorizontal(offset);
                                            if (ltrLine) {
                                                if (isRtl) {
                                                    left = secondary - charWidth;
                                                    right = secondary;
                                                } else {
                                                    left = primary;
                                                    right = primary + charWidth;
                                                }
                                            } else if (isRtl) {
                                                left = primary - charWidth;
                                                right = primary;
                                            } else {
                                                left = secondary;
                                                right = secondary + charWidth;
                                            }
                                            float localLeft = left + viewportToContentHorizontalOffset;
                                            float localRight = right + viewportToContentHorizontalOffset;
                                            float localTop = top + viewportToContentVerticalOffset;
                                            float localBottom = bottom + viewportToContentVerticalOffset;
                                            boolean isTopLeftVisible = Editor.this.isPositionVisible(localLeft, localTop);
                                            boolean isBottomRightVisible = Editor.this.isPositionVisible(localRight, localBottom);
                                            int characterBoundsFlags = Editor.TAP_STATE_INITIAL;
                                            if (isTopLeftVisible || isBottomRightVisible) {
                                                characterBoundsFlags = Editor.TAP_STATE_FIRST_TAP;
                                            }
                                            if (!(isTopLeftVisible && isBottomRightVisible)) {
                                                characterBoundsFlags |= Editor.TAP_STATE_DOUBLE_TAP;
                                            }
                                            if (isRtl) {
                                                characterBoundsFlags |= Editor.MENU_ITEM_ORDER_CUT;
                                            }
                                            builder.addCharacterBounds(offset, localLeft, localTop, localRight, localBottom, characterBoundsFlags);
                                        }
                                    }
                                }
                            }
                            if (selectionStart >= 0) {
                                offset = selectionStart;
                                line = layout.getLineForOffset(selectionStart);
                                float insertionMarkerX = layout.getPrimaryHorizontal(selectionStart) + viewportToContentHorizontalOffset;
                                float insertionMarkerTop = ((float) layout.getLineTop(line)) + viewportToContentVerticalOffset;
                                float insertionMarkerBaseline = ((float) layout.getLineBaseline(line)) + viewportToContentVerticalOffset;
                                float insertionMarkerBottom = ((float) layout.getLineBottom(line)) + viewportToContentVerticalOffset;
                                boolean isTopVisible = Editor.this.isPositionVisible(insertionMarkerX, insertionMarkerTop);
                                boolean isBottomVisible = Editor.this.isPositionVisible(insertionMarkerX, insertionMarkerBottom);
                                int insertionMarkerFlags = Editor.TAP_STATE_INITIAL;
                                if (isTopVisible || isBottomVisible) {
                                    insertionMarkerFlags = Editor.TAP_STATE_FIRST_TAP;
                                }
                                if (!(isTopVisible && isBottomVisible)) {
                                    insertionMarkerFlags |= Editor.TAP_STATE_DOUBLE_TAP;
                                }
                                if (layout.isRtlCharAt(selectionStart)) {
                                    insertionMarkerFlags |= Editor.MENU_ITEM_ORDER_CUT;
                                }
                                builder.setInsertionMarkerLocation(insertionMarkerX, insertionMarkerTop, insertionMarkerBaseline, insertionMarkerBottom, insertionMarkerFlags);
                            }
                            imm.updateCursorAnchorInfo(Editor.this.mTextView, builder.build());
                        }
                    }
                }
            }
        }
    }

    private interface CursorController extends OnTouchModeChangeListener {
        void hide();

        boolean isActive();

        boolean isCursorBeingModified();

        void onDetached();

        void show();
    }

    private static class DragLocalState {
        public int end;
        public TextView sourceTextView;
        public int start;

        public DragLocalState(TextView sourceTextView, int start, int end) {
            this.sourceTextView = sourceTextView;
            this.start = start;
            this.end = end;
        }
    }

    private interface EasyEditDeleteListener {
        void onDeleteClick(EasyEditSpan easyEditSpan);
    }

    private abstract class PinnedPopupWindow implements TextViewPositionListener {
        int mClippingLimitLeft;
        int mClippingLimitRight;
        protected ViewGroup mContentView;
        protected PopupWindow mPopupWindow;
        int mPositionX;
        int mPositionY;

        protected abstract int clipVertically(int i);

        protected abstract void createPopupWindow();

        protected abstract int getTextOffset();

        protected abstract int getVerticalLocalPosition(int i);

        protected abstract void initContentView();

        protected void setUp() {
        }

        public PinnedPopupWindow() {
            setUp();
            createPopupWindow();
            this.mPopupWindow.setWindowLayoutType(RILConstants.RIL_UNSOL_RESPONSE_NEW_SMS_ON_SIM);
            this.mPopupWindow.setWidth(Editor.EXTRACT_NOTHING);
            this.mPopupWindow.setHeight(Editor.EXTRACT_NOTHING);
            initContentView();
            this.mContentView.setLayoutParams(new ViewGroup.LayoutParams((int) Editor.EXTRACT_NOTHING, (int) Editor.EXTRACT_NOTHING));
            this.mPopupWindow.setContentView(this.mContentView);
        }

        public void show() {
            Editor.this.getPositionListener().addSubscriber(this, Editor.DEBUG_UNDO);
            computeLocalPosition();
            PositionListener positionListener = Editor.this.getPositionListener();
            updatePosition(positionListener.getPositionX(), positionListener.getPositionY());
        }

        protected void measureContent() {
            DisplayMetrics displayMetrics = Editor.this.mTextView.getResources().getDisplayMetrics();
            this.mContentView.measure(MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, RtlSpacingHelper.UNDEFINED), MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, RtlSpacingHelper.UNDEFINED));
        }

        private void computeLocalPosition() {
            measureContent();
            int width = this.mContentView.getMeasuredWidth();
            int offset = getTextOffset();
            this.mPositionX = (int) (Editor.this.mTextView.getLayout().getPrimaryHorizontal(offset) - (((float) width) / 2.0f));
            this.mPositionX += Editor.this.mTextView.viewportToContentHorizontalOffset();
            this.mPositionY = getVerticalLocalPosition(Editor.this.mTextView.getLayout().getLineForOffset(offset));
            this.mPositionY += Editor.this.mTextView.viewportToContentVerticalOffset();
        }

        private void updatePosition(int parentPositionX, int parentPositionY) {
            int positionX = parentPositionX + this.mPositionX;
            int positionY = clipVertically(parentPositionY + this.mPositionY);
            DisplayMetrics displayMetrics = Editor.this.mTextView.getResources().getDisplayMetrics();
            positionX = Math.max(-this.mClippingLimitLeft, Math.min((displayMetrics.widthPixels - this.mContentView.getMeasuredWidth()) + this.mClippingLimitRight, positionX));
            if (isShowing()) {
                this.mPopupWindow.update(positionX, positionY, Editor.UNSET_X_VALUE, Editor.UNSET_X_VALUE);
            } else {
                this.mPopupWindow.showAtLocation(Editor.this.mTextView, (int) Editor.TAP_STATE_INITIAL, positionX, positionY);
            }
        }

        public void hide() {
            if (isShowing()) {
                this.mPopupWindow.dismiss();
                Editor.this.getPositionListener().removeSubscriber(this);
            }
        }

        public void updatePosition(int parentPositionX, int parentPositionY, boolean parentPositionChanged, boolean parentScrolled) {
            if (isShowing() && Editor.this.isOffsetVisible(getTextOffset())) {
                if (parentScrolled) {
                    computeLocalPosition();
                }
                updatePosition(parentPositionX, parentPositionY);
                return;
            }
            hide();
        }

        public boolean isShowing() {
            return this.mPopupWindow.isShowing();
        }
    }

    private class EasyEditPopupWindow extends PinnedPopupWindow implements OnClickListener {
        private static final int POPUP_TEXT_LAYOUT = 17367280;
        private TextView mDeleteTextView;
        private EasyEditSpan mEasyEditSpan;
        private EasyEditDeleteListener mOnDeleteListener;

        private EasyEditPopupWindow() {
            super();
        }

        protected void createPopupWindow() {
            this.mPopupWindow = new PopupWindow(Editor.this.mTextView.getContext(), null, (int) R.attr.textSelectHandleWindowStyle);
            this.mPopupWindow.setInputMethodMode(Editor.TAP_STATE_DOUBLE_TAP);
            this.mPopupWindow.setClippingEnabled(true);
        }

        protected void initContentView() {
            LinearLayout linearLayout = new LinearLayout(Editor.this.mTextView.getContext());
            linearLayout.setOrientation(Editor.TAP_STATE_INITIAL);
            this.mContentView = linearLayout;
            this.mContentView.setBackgroundResource(R.drawable.text_edit_side_paste_window);
            LayoutInflater inflater = (LayoutInflater) Editor.this.mTextView.getContext().getSystemService("layout_inflater");
            ViewGroup.LayoutParams wrapContent = new ViewGroup.LayoutParams((int) Editor.EXTRACT_NOTHING, (int) Editor.EXTRACT_NOTHING);
            this.mDeleteTextView = (TextView) inflater.inflate((int) POPUP_TEXT_LAYOUT, null);
            this.mDeleteTextView.setLayoutParams(wrapContent);
            this.mDeleteTextView.setText((int) R.string.delete);
            this.mDeleteTextView.setOnClickListener(this);
            this.mContentView.addView(this.mDeleteTextView);
        }

        public void setEasyEditSpan(EasyEditSpan easyEditSpan) {
            this.mEasyEditSpan = easyEditSpan;
        }

        private void setOnDeleteListener(EasyEditDeleteListener listener) {
            this.mOnDeleteListener = listener;
        }

        public void onClick(View view) {
            if (view == this.mDeleteTextView && this.mEasyEditSpan != null && this.mEasyEditSpan.isDeleteEnabled() && this.mOnDeleteListener != null) {
                this.mOnDeleteListener.onDeleteClick(this.mEasyEditSpan);
            }
        }

        public void hide() {
            if (this.mEasyEditSpan != null) {
                this.mEasyEditSpan.setDeleteEnabled(Editor.DEBUG_UNDO);
            }
            this.mOnDeleteListener = null;
            super.hide();
        }

        protected int getTextOffset() {
            return ((Editable) Editor.this.mTextView.getText()).getSpanEnd(this.mEasyEditSpan);
        }

        protected int getVerticalLocalPosition(int line) {
            return Editor.this.mTextView.getLayout().getLineBottom(line);
        }

        protected int clipVertically(int positionY) {
            return positionY;
        }
    }

    public static class EditOperation extends UndoOperation<Editor> {
        public static final ClassLoaderCreator<EditOperation> CREATOR = null;
        private static final int TYPE_DELETE = 1;
        private static final int TYPE_INSERT = 0;
        private static final int TYPE_REPLACE = 2;
        private int mNewCursorPos;
        private String mNewText;
        private int mNewTextStart;
        private int mOldCursorPos;
        private String mOldText;
        private int mOldTextStart;
        private int mType;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.Editor.EditOperation.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.Editor.EditOperation.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.Editor.EditOperation.<clinit>():void");
        }

        public EditOperation(Editor editor, String oldText, int dstart, String newText) {
            super(editor.mUndoOwner);
            this.mOldText = oldText;
            this.mNewText = newText;
            if (this.mNewText.length() > 0 && this.mOldText.length() == 0) {
                this.mType = TYPE_INSERT;
                this.mNewTextStart = dstart;
            } else if (this.mNewText.length() != 0 || this.mOldText.length() <= 0) {
                this.mType = TYPE_REPLACE;
                this.mNewTextStart = dstart;
                this.mOldTextStart = dstart;
            } else {
                this.mType = TYPE_DELETE;
                this.mOldTextStart = dstart;
            }
            this.mOldCursorPos = editor.mTextView.getSelectionStart();
            this.mNewCursorPos = this.mNewText.length() + dstart;
        }

        public EditOperation(Parcel src, ClassLoader loader) {
            super(src, loader);
            this.mType = src.readInt();
            this.mOldText = src.readString();
            this.mOldTextStart = src.readInt();
            this.mNewText = src.readString();
            this.mNewTextStart = src.readInt();
            this.mOldCursorPos = src.readInt();
            this.mNewCursorPos = src.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mType);
            dest.writeString(this.mOldText);
            dest.writeInt(this.mOldTextStart);
            dest.writeString(this.mNewText);
            dest.writeInt(this.mNewTextStart);
            dest.writeInt(this.mOldCursorPos);
            dest.writeInt(this.mNewCursorPos);
        }

        private int getNewTextEnd() {
            return this.mNewTextStart + this.mNewText.length();
        }

        private int getOldTextEnd() {
            return this.mOldTextStart + this.mOldText.length();
        }

        public void commit() {
        }

        public void undo() {
            modifyText((Editable) ((Editor) getOwnerData()).mTextView.getText(), this.mNewTextStart, getNewTextEnd(), this.mOldText, this.mOldTextStart, this.mOldCursorPos);
        }

        public void redo() {
            modifyText((Editable) ((Editor) getOwnerData()).mTextView.getText(), this.mOldTextStart, getOldTextEnd(), this.mNewText, this.mNewTextStart, this.mNewCursorPos);
        }

        private boolean mergeWith(EditOperation edit) {
            switch (this.mType) {
                case TYPE_INSERT /*0*/:
                    return mergeInsertWith(edit);
                case TYPE_DELETE /*1*/:
                    return mergeDeleteWith(edit);
                case TYPE_REPLACE /*2*/:
                    return mergeReplaceWith(edit);
                default:
                    return Editor.DEBUG_UNDO;
            }
        }

        private boolean mergeInsertWith(EditOperation edit) {
            if (edit.mType != 0 || getNewTextEnd() != edit.mNewTextStart) {
                return Editor.DEBUG_UNDO;
            }
            this.mNewText += edit.mNewText;
            this.mNewCursorPos = edit.mNewCursorPos;
            return true;
        }

        private boolean mergeDeleteWith(EditOperation edit) {
            if (edit.mType != TYPE_DELETE || this.mOldTextStart != edit.getOldTextEnd()) {
                return Editor.DEBUG_UNDO;
            }
            this.mOldTextStart = edit.mOldTextStart;
            this.mOldText = edit.mOldText + this.mOldText;
            this.mNewCursorPos = edit.mNewCursorPos;
            return true;
        }

        private boolean mergeReplaceWith(EditOperation edit) {
            if (edit.mType != 0 || getNewTextEnd() != edit.mNewTextStart) {
                return Editor.DEBUG_UNDO;
            }
            this.mOldText += edit.mOldText;
            this.mNewText += edit.mNewText;
            this.mNewCursorPos = edit.mNewCursorPos;
            return true;
        }

        public void forceMergeWith(EditOperation edit) {
            Editable editable = (Editable) ((Editor) getOwnerData()).mTextView.getText();
            Editable originalText = new SpannableStringBuilder(editable.toString());
            modifyText(originalText, this.mNewTextStart, getNewTextEnd(), this.mOldText, this.mOldTextStart, this.mOldCursorPos);
            Editable finalText = new SpannableStringBuilder(editable.toString());
            modifyText(finalText, edit.mOldTextStart, edit.getOldTextEnd(), edit.mNewText, edit.mNewTextStart, edit.mNewCursorPos);
            this.mType = TYPE_REPLACE;
            this.mNewText = finalText.toString();
            this.mNewTextStart = TYPE_INSERT;
            this.mOldText = originalText.toString();
            this.mOldTextStart = TYPE_INSERT;
            this.mNewCursorPos = edit.mNewCursorPos;
        }

        private static void modifyText(Editable text, int deleteFrom, int deleteTo, CharSequence newText, int newTextInsertAt, int newCursorPos) {
            if (Editor.isValidRange(text, deleteFrom, deleteTo) && newTextInsertAt <= text.length() - (deleteTo - deleteFrom)) {
                if (deleteFrom != deleteTo) {
                    text.delete(deleteFrom, deleteTo);
                }
                if (newText.length() != 0) {
                    text.insert(newTextInsertAt, newText);
                }
            }
            if (newCursorPos >= 0 && newCursorPos <= text.length()) {
                Selection.setSelection(text, newCursorPos);
            }
        }

        private String getTypeString() {
            switch (this.mType) {
                case TYPE_INSERT /*0*/:
                    return "insert";
                case TYPE_DELETE /*1*/:
                    return "delete";
                case TYPE_REPLACE /*2*/:
                    return "replace";
                default:
                    return "";
            }
        }

        public String toString() {
            return "[mType=" + getTypeString() + ", " + "mOldText=" + this.mOldText + ", " + "mOldTextStart=" + this.mOldTextStart + ", " + "mNewText=" + this.mNewText + ", " + "mNewTextStart=" + this.mNewTextStart + ", " + "mOldCursorPos=" + this.mOldCursorPos + ", " + "mNewCursorPos=" + this.mNewCursorPos + "]";
        }
    }

    private static class ErrorPopup extends PopupWindow {
        private boolean mAbove;
        private int mPopupInlineErrorAboveBackgroundId;
        private int mPopupInlineErrorBackgroundId;
        private final TextView mView;

        ErrorPopup(TextView v, int width, int height) {
            super((View) v, width, height);
            this.mAbove = Editor.DEBUG_UNDO;
            this.mPopupInlineErrorBackgroundId = Editor.TAP_STATE_INITIAL;
            this.mPopupInlineErrorAboveBackgroundId = Editor.TAP_STATE_INITIAL;
            this.mView = v;
            this.mPopupInlineErrorBackgroundId = getResourceId(this.mPopupInlineErrorBackgroundId, MetricsEvent.ACTION_ADD_EMERGENCY_CONTACT);
            this.mView.setBackgroundResource(this.mPopupInlineErrorBackgroundId);
        }

        void fixDirection(boolean above) {
            int i;
            this.mAbove = above;
            if (above) {
                this.mPopupInlineErrorAboveBackgroundId = getResourceId(this.mPopupInlineErrorAboveBackgroundId, MetricsEvent.ACTION_DELETE_EMERGENCY_CONTACT);
            } else {
                this.mPopupInlineErrorBackgroundId = getResourceId(this.mPopupInlineErrorBackgroundId, MetricsEvent.ACTION_ADD_EMERGENCY_CONTACT);
            }
            TextView textView = this.mView;
            if (above) {
                i = this.mPopupInlineErrorAboveBackgroundId;
            } else {
                i = this.mPopupInlineErrorBackgroundId;
            }
            textView.setBackgroundResource(i);
        }

        private int getResourceId(int currentId, int index) {
            if (currentId != 0) {
                return currentId;
            }
            TypedArray styledAttributes = this.mView.getContext().obtainStyledAttributes(android.R.styleable.Theme);
            currentId = styledAttributes.getResourceId(index, Editor.TAP_STATE_INITIAL);
            styledAttributes.recycle();
            return currentId;
        }

        public void update(int x, int y, int w, int h, boolean force) {
            super.update(x, y, w, h, force);
            boolean above = isAboveAnchor();
            if (above != this.mAbove) {
                fixDirection(above);
            }
        }
    }

    public abstract class HandleView extends View implements TextViewPositionListener {
        private static final int HISTORY_SIZE = 5;
        private static final int TOUCH_UP_FILTER_DELAY_AFTER = 150;
        private static final int TOUCH_UP_FILTER_DELAY_BEFORE = 350;
        private final PopupWindow mContainer;
        protected Drawable mDrawable;
        protected Drawable mDrawableLtr;
        protected Drawable mDrawableRtl;
        protected int mHorizontalGravity;
        protected int mHotspotX;
        private float mIdealVerticalOffset;
        private boolean mIsDragging;
        protected int mLastParentX;
        protected int mLastParentXOnScreen;
        protected int mLastParentY;
        protected int mLastParentYOnScreen;
        private int mMinSize;
        private int mNumberPreviousOffsets;
        private boolean mPositionHasChanged;
        private int mPositionX;
        private int mPositionY;
        protected int mPrevLine;
        protected int mPreviousLineTouched;
        protected int mPreviousOffset;
        private int mPreviousOffsetIndex;
        private final int[] mPreviousOffsets;
        private final long[] mPreviousOffsetsTimes;
        private float mTouchOffsetY;
        protected float mTouchToWindowOffsetX;
        protected float mTouchToWindowOffsetY;
        final /* synthetic */ Editor this$0;

        /* synthetic */ HandleView(Editor this$0, Drawable drawableLtr, Drawable drawableRtl, int id, HandleView handleView) {
            this(this$0, drawableLtr, drawableRtl, id);
        }

        public abstract int getCurrentCursorOffset();

        protected abstract int getHorizontalGravity(boolean z);

        protected abstract int getHotspotX(Drawable drawable, boolean z);

        public abstract void updatePosition(float f, float f2);

        protected abstract void updateSelection(int i);

        private HandleView(Editor this$0, Drawable drawableLtr, Drawable drawableRtl, int id) {
            this.this$0 = this$0;
            super(this$0.mTextView.getContext());
            this.mPreviousOffset = Editor.UNSET_X_VALUE;
            this.mPositionHasChanged = true;
            this.mPrevLine = Editor.UNSET_X_VALUE;
            this.mPreviousLineTouched = Editor.UNSET_X_VALUE;
            this.mPreviousOffsetsTimes = new long[HISTORY_SIZE];
            this.mPreviousOffsets = new int[HISTORY_SIZE];
            this.mPreviousOffsetIndex = Editor.TAP_STATE_INITIAL;
            this.mNumberPreviousOffsets = Editor.TAP_STATE_INITIAL;
            setId(id);
            this.mContainer = new PopupWindow(this$0.mTextView.getContext(), null, (int) R.attr.textSelectHandleWindowStyle);
            this.mContainer.setSplitTouchEnabled(true);
            this.mContainer.setClippingEnabled(Editor.DEBUG_UNDO);
            this.mContainer.setWindowLayoutType(RILConstants.RIL_UNSOL_RESPONSE_VOICE_NETWORK_STATE_CHANGED);
            this.mContainer.setWidth(Editor.EXTRACT_NOTHING);
            this.mContainer.setHeight(Editor.EXTRACT_NOTHING);
            this.mContainer.setContentView(this);
            this.mDrawableLtr = drawableLtr;
            this.mDrawableRtl = drawableRtl;
            this.mMinSize = this$0.mTextView.getContext().getResources().getDimensionPixelSize(R.dimen.text_handle_min_size);
            updateDrawable();
            int handleHeight = getPreferredHeight();
            this.mTouchOffsetY = ((float) handleHeight) * -0.3f;
            this.mIdealVerticalOffset = ((float) handleHeight) * 0.7f;
        }

        public float getIdealVerticalOffset() {
            return this.mIdealVerticalOffset;
        }

        protected void updateDrawable() {
            if (!this.mIsDragging) {
                Layout layout = this.this$0.mTextView.getLayout();
                if (layout != null) {
                    int offset = getCurrentCursorOffset();
                    boolean isRtlCharAtOffset = isAtRtlRun(layout, offset);
                    Drawable oldDrawable = this.mDrawable;
                    this.mDrawable = isRtlCharAtOffset ? this.mDrawableRtl : this.mDrawableLtr;
                    this.mHotspotX = getHotspotX(this.mDrawable, isRtlCharAtOffset);
                    this.mHorizontalGravity = getHorizontalGravity(isRtlCharAtOffset);
                    if (oldDrawable != this.mDrawable && isShowing()) {
                        this.mPositionX = ((getCursorHorizontalPosition(layout, offset) - this.mHotspotX) - getHorizontalOffset()) + getCursorOffset();
                        this.mPositionX += this.this$0.mTextView.viewportToContentHorizontalOffset();
                        this.mPositionHasChanged = true;
                        updatePosition(this.mLastParentX, this.mLastParentY, Editor.DEBUG_UNDO, Editor.DEBUG_UNDO);
                        postInvalidate();
                    }
                }
            }
        }

        private void startTouchUpFilter(int offset) {
            this.mNumberPreviousOffsets = Editor.TAP_STATE_INITIAL;
            addPositionToTouchUpFilter(offset);
        }

        private void addPositionToTouchUpFilter(int offset) {
            this.mPreviousOffsetIndex = (this.mPreviousOffsetIndex + Editor.TAP_STATE_FIRST_TAP) % HISTORY_SIZE;
            this.mPreviousOffsets[this.mPreviousOffsetIndex] = offset;
            this.mPreviousOffsetsTimes[this.mPreviousOffsetIndex] = SystemClock.uptimeMillis();
            this.mNumberPreviousOffsets += Editor.TAP_STATE_FIRST_TAP;
        }

        private void filterOnTouchUp() {
            long now = SystemClock.uptimeMillis();
            int i = Editor.TAP_STATE_INITIAL;
            int index = this.mPreviousOffsetIndex;
            int iMax = Math.min(this.mNumberPreviousOffsets, HISTORY_SIZE);
            while (i < iMax && now - this.mPreviousOffsetsTimes[index] < 150) {
                i += Editor.TAP_STATE_FIRST_TAP;
                index = ((this.mPreviousOffsetIndex - i) + HISTORY_SIZE) % HISTORY_SIZE;
            }
            if (i > 0 && i < iMax && now - this.mPreviousOffsetsTimes[index] > 350) {
                positionAtCursorOffset(this.mPreviousOffsets[index], Editor.DEBUG_UNDO);
            }
        }

        public boolean offsetHasBeenChanged() {
            return this.mNumberPreviousOffsets > Editor.TAP_STATE_FIRST_TAP ? true : Editor.DEBUG_UNDO;
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(getPreferredWidth(), getPreferredHeight());
        }

        public void invalidate() {
            super.invalidate();
            if (isShowing()) {
                positionAtCursorOffset(getCurrentCursorOffset(), true);
            }
        }

        private int getPreferredWidth() {
            return Math.max(this.mDrawable.getIntrinsicWidth(), this.mMinSize);
        }

        private int getPreferredHeight() {
            return Math.max(this.mDrawable.getIntrinsicHeight(), this.mMinSize);
        }

        public void show() {
            if (!isShowing()) {
                this.this$0.getPositionListener().addSubscriber(this, true);
                this.mPreviousOffset = Editor.UNSET_X_VALUE;
                positionAtCursorOffset(getCurrentCursorOffset(), Editor.DEBUG_UNDO);
            }
        }

        protected void dismiss() {
            this.mIsDragging = Editor.DEBUG_UNDO;
            this.mContainer.dismiss();
            onDetached();
        }

        public void hide() {
            dismiss();
            this.this$0.getPositionListener().removeSubscriber(this);
        }

        public boolean isShowing() {
            return this.mContainer.isShowing();
        }

        private boolean isVisible() {
            if (this.mIsDragging) {
                return true;
            }
            if (this.this$0.mTextView.isInBatchEditMode()) {
                return Editor.DEBUG_UNDO;
            }
            return this.this$0.isPositionVisible((float) ((this.mPositionX + this.mHotspotX) + getHorizontalOffset()), (float) this.mPositionY);
        }

        protected boolean isAtRtlRun(Layout layout, int offset) {
            return layout.isRtlCharAt(offset);
        }

        public float getHorizontal(Layout layout, int offset) {
            return layout.getPrimaryHorizontal(offset);
        }

        protected int getOffsetAtCoordinate(Layout layout, int line, float x) {
            return this.this$0.mTextView.getOffsetAtCoordinate(line, x);
        }

        protected void positionAtCursorOffset(int offset, boolean forceUpdatePosition) {
            if (this.this$0.mTextView.getLayout() == null) {
                this.this$0.prepareCursorControllers();
                return;
            }
            boolean offsetChanged;
            Layout layout = this.this$0.mTextView.getLayout();
            if (offset != this.mPreviousOffset) {
                offsetChanged = true;
            } else {
                offsetChanged = Editor.DEBUG_UNDO;
            }
            if (offsetChanged || forceUpdatePosition) {
                if (offsetChanged) {
                    updateSelection(offset);
                    addPositionToTouchUpFilter(offset);
                }
                int line = layout.getLineForOffset(offset);
                this.mPrevLine = line;
                this.mPositionX = ((getCursorHorizontalPosition(layout, offset) - this.mHotspotX) - getHorizontalOffset()) + getCursorOffset();
                this.mPositionY = layout.getLineBottom(line);
                int[] coordinate = new int[Editor.TAP_STATE_DOUBLE_TAP];
                coordinate[Editor.TAP_STATE_INITIAL] = this.mPositionX;
                coordinate[Editor.TAP_STATE_FIRST_TAP] = this.mPositionY;
                if (this.this$0.adjustHandlePos(coordinate, this, layout, offset, line)) {
                    this.mPositionX = coordinate[Editor.TAP_STATE_INITIAL];
                    this.mPositionY = coordinate[Editor.TAP_STATE_FIRST_TAP];
                }
                this.mPositionX += this.this$0.mTextView.viewportToContentHorizontalOffset();
                this.mPositionY += this.this$0.mTextView.viewportToContentVerticalOffset();
                this.mPreviousOffset = offset;
                this.mPositionHasChanged = true;
            }
        }

        int getCursorHorizontalPosition(Layout layout, int offset) {
            if (this.this$0.mTextView == null || this.this$0.mTextView.getPaddingStart() != 0) {
                return (int) (getHorizontal(layout, offset) - Editor.LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS);
            }
            return (int) getHorizontal(layout, offset);
        }

        public void updatePosition(int parentPositionX, int parentPositionY, boolean parentPositionChanged, boolean parentScrolled) {
            positionAtCursorOffset(getCurrentCursorOffset(), parentScrolled);
            if (parentPositionChanged || this.mPositionHasChanged) {
                if (this.mIsDragging) {
                    if (!(parentPositionX == this.mLastParentX && parentPositionY == this.mLastParentY)) {
                        this.mTouchToWindowOffsetX += (float) (parentPositionX - this.mLastParentX);
                        this.mTouchToWindowOffsetY += (float) (parentPositionY - this.mLastParentY);
                        this.mLastParentX = parentPositionX;
                        this.mLastParentY = parentPositionY;
                    }
                    onHandleMoved();
                }
                if (isVisible()) {
                    int[] pts = new int[Editor.TAP_STATE_DOUBLE_TAP];
                    pts[Editor.TAP_STATE_INITIAL] = (this.mPositionX + this.mHotspotX) + getHorizontalOffset();
                    pts[Editor.TAP_STATE_FIRST_TAP] = this.mPositionY;
                    this.this$0.mTextView.transformFromViewToWindowSpace(pts);
                    pts[Editor.TAP_STATE_INITIAL] = pts[Editor.TAP_STATE_INITIAL] - (this.mHotspotX + getHorizontalOffset());
                    if (isShowing()) {
                        this.mContainer.update(pts[Editor.TAP_STATE_INITIAL], pts[Editor.TAP_STATE_FIRST_TAP], Editor.UNSET_X_VALUE, Editor.UNSET_X_VALUE);
                    } else {
                        this.mContainer.showAtLocation(this.this$0.mTextView, (int) Editor.TAP_STATE_INITIAL, pts[Editor.TAP_STATE_INITIAL], pts[Editor.TAP_STATE_FIRST_TAP]);
                    }
                } else if (isShowing()) {
                    Log.d(Editor.UNDO_OWNER_TAG, "HandleView is showing but not visible, so dismiss it.");
                    dismiss();
                }
                this.mPositionHasChanged = Editor.DEBUG_UNDO;
            }
        }

        protected void onDraw(Canvas c) {
            int drawWidth = this.mDrawable.getIntrinsicWidth();
            int left = getHorizontalOffset();
            this.mDrawable.setBounds(left, Editor.TAP_STATE_INITIAL, left + drawWidth, this.mDrawable.getIntrinsicHeight());
            this.mDrawable.draw(c);
        }

        private int getHorizontalOffset() {
            int width = getPreferredWidth();
            int drawWidth = this.mDrawable.getIntrinsicWidth();
            switch (this.mHorizontalGravity) {
                case Editor.TAP_STATE_TRIPLE_CLICK /*3*/:
                    return Editor.TAP_STATE_INITIAL;
                case HISTORY_SIZE /*5*/:
                    return width - drawWidth;
                default:
                    return (width - drawWidth) / Editor.TAP_STATE_DOUBLE_TAP;
            }
        }

        protected int getCursorOffset() {
            return Editor.TAP_STATE_INITIAL;
        }

        public boolean onTouchEvent(MotionEvent ev) {
            this.this$0.updateFloatingToolbarVisibility(ev);
            float yInWindow;
            switch (ev.getActionMasked()) {
                case Editor.TAP_STATE_INITIAL /*0*/:
                    startTouchUpFilter(getCurrentCursorOffset());
                    PositionListener positionListener = this.this$0.getPositionListener();
                    this.mLastParentX = positionListener.getPositionX();
                    this.mLastParentY = positionListener.getPositionY();
                    this.mLastParentXOnScreen = positionListener.getPositionXOnScreen();
                    this.mLastParentYOnScreen = positionListener.getPositionYOnScreen();
                    yInWindow = (ev.getRawY() - ((float) this.mLastParentYOnScreen)) + ((float) this.mLastParentY);
                    this.mTouchToWindowOffsetX = ((ev.getRawX() - ((float) this.mLastParentXOnScreen)) + ((float) this.mLastParentX)) - ((float) this.mPositionX);
                    this.mTouchToWindowOffsetY = yInWindow - ((float) this.mPositionY);
                    this.mIsDragging = true;
                    this.mPreviousLineTouched = Editor.UNSET_X_VALUE;
                    break;
                case Editor.TAP_STATE_FIRST_TAP /*1*/:
                    filterOnTouchUp();
                    this.mIsDragging = Editor.DEBUG_UNDO;
                    updateDrawable();
                    break;
                case Editor.TAP_STATE_DOUBLE_TAP /*2*/:
                    float newVerticalOffset;
                    float xInWindow = (ev.getRawX() - ((float) this.mLastParentXOnScreen)) + ((float) this.mLastParentX);
                    yInWindow = (ev.getRawY() - ((float) this.mLastParentYOnScreen)) + ((float) this.mLastParentY);
                    float previousVerticalOffset = this.mTouchToWindowOffsetY - ((float) this.mLastParentY);
                    float currentVerticalOffset = (yInWindow - ((float) this.mPositionY)) - ((float) this.mLastParentY);
                    if (previousVerticalOffset < this.mIdealVerticalOffset) {
                        newVerticalOffset = Math.max(Math.min(currentVerticalOffset, this.mIdealVerticalOffset), previousVerticalOffset);
                    } else {
                        newVerticalOffset = Math.min(Math.max(currentVerticalOffset, this.mIdealVerticalOffset), previousVerticalOffset);
                    }
                    this.mTouchToWindowOffsetY = ((float) this.mLastParentY) + newVerticalOffset;
                    updatePosition(((xInWindow - this.mTouchToWindowOffsetX) + ((float) this.mHotspotX)) + ((float) getHorizontalOffset()), (yInWindow - this.mTouchToWindowOffsetY) + this.mTouchOffsetY);
                    break;
                case Editor.TAP_STATE_TRIPLE_CLICK /*3*/:
                    this.mIsDragging = Editor.DEBUG_UNDO;
                    updateDrawable();
                    break;
            }
            return true;
        }

        public boolean isDragging() {
            return this.mIsDragging;
        }

        void onHandleMoved() {
        }

        public void onDetached() {
        }
    }

    static class InputContentType {
        boolean enterDown;
        Bundle extras;
        int imeActionId;
        CharSequence imeActionLabel;
        LocaleList imeHintLocales;
        int imeOptions;
        OnEditorActionListener onEditorActionListener;
        String privateImeOptions;

        InputContentType() {
            this.imeOptions = Editor.TAP_STATE_INITIAL;
        }
    }

    static class InputMethodState {
        int mBatchEditNesting;
        int mChangedDelta;
        int mChangedEnd;
        int mChangedStart;
        boolean mContentChanged;
        boolean mCursorChanged;
        final ExtractedText mExtractedText;
        ExtractedTextRequest mExtractedTextRequest;
        boolean mSelectionModeChanged;

        InputMethodState() {
            this.mExtractedText = new ExtractedText();
        }
    }

    protected class InsertionHandleView extends HandleView {
        private static final int DELAY_BEFORE_HANDLE_FADES_OUT = 4000;
        private static final int RECENT_CUT_COPY_DURATION = 15000;
        private float mDownPositionX;
        private float mDownPositionY;
        private Runnable mHider;
        final /* synthetic */ Editor this$0;

        /* renamed from: android.widget.Editor.InsertionHandleView.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ InsertionHandleView this$1;

            AnonymousClass1(InsertionHandleView this$1) {
                this.this$1 = this$1;
            }

            public void run() {
                this.this$1.this$0.startInsertionActionMode();
            }
        }

        /* renamed from: android.widget.Editor.InsertionHandleView.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ InsertionHandleView this$1;

            AnonymousClass2(InsertionHandleView this$1) {
                this.this$1 = this$1;
            }

            public void run() {
                this.this$1.hide();
            }
        }

        public InsertionHandleView(Editor this$0, Drawable drawable) {
            this.this$0 = this$0;
            super(this$0, drawable, drawable, R.id.insertion_handle, null);
        }

        public void show() {
            super.show();
            long durationSinceCutOrCopy = SystemClock.uptimeMillis() - TextView.sLastCutCopyOrTextChangedTime;
            if (this.this$0.mInsertionActionModeRunnable != null) {
                if (!(this.this$0.mTapState == Editor.TAP_STATE_DOUBLE_TAP || this.this$0.mTapState == Editor.TAP_STATE_TRIPLE_CLICK)) {
                    if (this.this$0.isCursorInsideEasyCorrectionSpan()) {
                    }
                }
                this.this$0.mTextView.removeCallbacks(this.this$0.mInsertionActionModeRunnable);
            }
            if (!(this.this$0.mTapState == Editor.TAP_STATE_DOUBLE_TAP || this.this$0.mTapState == Editor.TAP_STATE_TRIPLE_CLICK || this.this$0.isCursorInsideEasyCorrectionSpan() || durationSinceCutOrCopy >= 15000 || this.this$0.mTextActionMode != null)) {
                if (this.this$0.mInsertionActionModeRunnable == null) {
                    this.this$0.mInsertionActionModeRunnable = new AnonymousClass1(this);
                }
                this.this$0.mTextView.postDelayed(this.this$0.mInsertionActionModeRunnable, 0);
            }
            hideAfterDelay();
        }

        private void hideAfterDelay() {
            if (this.mHider == null) {
                this.mHider = new AnonymousClass2(this);
            } else {
                removeHiderCallback();
            }
            this.this$0.mTextView.postDelayed(this.mHider, 4000);
        }

        private void removeHiderCallback() {
            if (this.mHider != null) {
                this.this$0.mTextView.removeCallbacks(this.mHider);
            }
        }

        protected int getHotspotX(Drawable drawable, boolean isRtlRun) {
            return drawable.getIntrinsicWidth() / Editor.TAP_STATE_DOUBLE_TAP;
        }

        protected int getHorizontalGravity(boolean isRtlRun) {
            return Editor.TAP_STATE_FIRST_TAP;
        }

        protected int getCursorOffset() {
            Drawable cursor = null;
            int offset = super.getCursorOffset();
            if (this.this$0.mCursorCount > 0) {
                cursor = this.this$0.mCursorDrawable[Editor.TAP_STATE_INITIAL];
            }
            if (cursor == null) {
                return offset;
            }
            cursor.getPadding(this.this$0.mTempRect);
            return offset + (((cursor.getIntrinsicWidth() - this.this$0.mTempRect.left) - this.this$0.mTempRect.right) / Editor.TAP_STATE_DOUBLE_TAP);
        }

        int getCursorHorizontalPosition(Layout layout, int offset) {
            Drawable drawable = null;
            if (this.this$0.mCursorCount > 0) {
                drawable = this.this$0.mCursorDrawable[Editor.TAP_STATE_INITIAL];
            }
            if (drawable == null) {
                return super.getCursorHorizontalPosition(layout, offset);
            }
            return this.this$0.clampHorizontalPosition(drawable, getHorizontal(layout, offset)) + this.this$0.mTempRect.left;
        }

        public boolean onTouchEvent(MotionEvent ev) {
            boolean result = super.onTouchEvent(ev);
            this.this$0.setPosWithMotionEvent(ev, Editor.DEBUG_UNDO);
            switch (ev.getActionMasked()) {
                case Editor.TAP_STATE_INITIAL /*0*/:
                    this.mDownPositionX = ev.getRawX();
                    this.mDownPositionY = ev.getRawY();
                    break;
                case Editor.TAP_STATE_FIRST_TAP /*1*/:
                    if (!offsetHasBeenChanged()) {
                        float deltaX = this.mDownPositionX - ev.getRawX();
                        float deltaY = this.mDownPositionY - ev.getRawY();
                        float distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);
                        int touchSlop = ViewConfiguration.get(this.this$0.mTextView.getContext()).getScaledTouchSlop();
                        if (distanceSquared < ((float) (touchSlop * touchSlop))) {
                            if (this.this$0.mTextActionMode != null) {
                                this.this$0.stopTextActionMode();
                            } else {
                                this.this$0.startInsertionActionMode();
                            }
                        }
                    } else if (this.this$0.mTextActionMode != null) {
                        this.this$0.mTextActionMode.invalidateContentRect();
                    }
                    hideAfterDelay();
                    break;
                case Editor.TAP_STATE_TRIPLE_CLICK /*3*/:
                    hideAfterDelay();
                    break;
            }
            return result;
        }

        public int getCurrentCursorOffset() {
            this.this$0.recogniseLineEnd();
            return this.this$0.mTextView.getSelectionStart();
        }

        public void updateSelection(int offset) {
            Selection.setSelection((Spannable) this.this$0.mTextView.getText(), offset);
        }

        public void updatePosition(float x, float y) {
            int offset;
            Layout layout = this.this$0.mTextView.getLayout();
            if (layout != null) {
                if (this.mPreviousLineTouched == Editor.UNSET_X_VALUE) {
                    this.mPreviousLineTouched = this.this$0.mTextView.getLineAtCoordinate(y);
                }
                int currLine = this.this$0.getCurrentLineAdjustedForSlop(layout, this.mPreviousLineTouched, y);
                offset = getOffsetAtCoordinate(layout, currLine, x);
                this.mPreviousLineTouched = currLine;
                offset = this.this$0.adjustOffsetAtLineEndForInsertHanlePos(offset);
            } else {
                offset = Editor.UNSET_X_VALUE;
            }
            positionAtCursorOffset(offset, Editor.DEBUG_UNDO);
            if (this.this$0.mTextActionMode != null) {
                this.this$0.mTextActionMode.invalidate();
            }
        }

        void onHandleMoved() {
            super.onHandleMoved();
            removeHiderCallback();
        }

        public void onDetached() {
            super.onDetached();
            removeHiderCallback();
        }
    }

    private class InsertionPointCursorController implements CursorController {
        private InsertionHandleView mHandle;
        final /* synthetic */ Editor this$0;

        /* synthetic */ InsertionPointCursorController(Editor this$0, InsertionPointCursorController insertionPointCursorController) {
            this(this$0);
        }

        private InsertionPointCursorController(Editor this$0) {
            this.this$0 = this$0;
        }

        public void show() {
            getHandle().show();
            if (this.this$0.mSelectionModifierCursorController != null) {
                this.this$0.mSelectionModifierCursorController.hide();
            }
        }

        public void hide() {
            if (this.mHandle != null) {
                this.mHandle.hide();
            }
        }

        public void onTouchModeChanged(boolean isInTouchMode) {
            if (!isInTouchMode) {
                hide();
            }
        }

        private InsertionHandleView getHandle() {
            if (this.this$0.mSelectHandleCenter == null) {
                this.this$0.mSelectHandleCenter = this.this$0.mTextView.getContext().getDrawable(this.this$0.mTextView.mTextSelectHandleRes);
            }
            if (this.mHandle == null) {
                this.mHandle = new InsertionHandleView(this.this$0, this.this$0.mSelectHandleCenter);
            }
            return this.mHandle;
        }

        public void onDetached() {
            this.this$0.mTextView.getViewTreeObserver().removeOnTouchModeChangeListener(this);
            if (this.mHandle != null) {
                this.mHandle.onDetached();
            }
        }

        public boolean isCursorBeingModified() {
            return this.mHandle != null ? this.mHandle.isDragging() : Editor.DEBUG_UNDO;
        }

        public boolean isActive() {
            return this.mHandle != null ? this.mHandle.isShowing() : Editor.DEBUG_UNDO;
        }

        public void invalidateHandle() {
            if (this.mHandle != null) {
                this.mHandle.invalidate();
            }
        }
    }

    private class PositionListener implements OnPreDrawListener {
        private final int MAXIMUM_NUMBER_OF_LISTENERS;
        private boolean[] mCanMove;
        private int mNumberOfListeners;
        private boolean mPositionHasChanged;
        private TextViewPositionListener[] mPositionListeners;
        private int mPositionX;
        private int mPositionXOnScreen;
        private int mPositionY;
        private int mPositionYOnScreen;
        private boolean mScrollHasChanged;
        final int[] mTempCoords;
        final /* synthetic */ Editor this$0;

        /* synthetic */ PositionListener(Editor this$0, PositionListener positionListener) {
            this(this$0);
        }

        private PositionListener(Editor this$0) {
            this.this$0 = this$0;
            this.MAXIMUM_NUMBER_OF_LISTENERS = Editor.MENU_ITEM_ORDER_PASTE_AS_PLAIN_TEXT;
            this.mPositionListeners = new TextViewPositionListener[Editor.MENU_ITEM_ORDER_PASTE_AS_PLAIN_TEXT];
            this.mCanMove = new boolean[Editor.MENU_ITEM_ORDER_PASTE_AS_PLAIN_TEXT];
            this.mPositionHasChanged = true;
            this.mTempCoords = new int[Editor.TAP_STATE_DOUBLE_TAP];
        }

        public void addSubscriber(TextViewPositionListener positionListener, boolean canMove) {
            if (this.mNumberOfListeners == 0) {
                updatePosition();
                this.this$0.mTextView.getViewTreeObserver().addOnPreDrawListener(this);
            }
            int emptySlotIndex = Editor.UNSET_X_VALUE;
            int i = Editor.TAP_STATE_INITIAL;
            while (i < Editor.MENU_ITEM_ORDER_PASTE_AS_PLAIN_TEXT) {
                TextViewPositionListener listener = this.mPositionListeners[i];
                if (listener != positionListener) {
                    if (emptySlotIndex < 0 && listener == null) {
                        emptySlotIndex = i;
                    }
                    i += Editor.TAP_STATE_FIRST_TAP;
                } else {
                    return;
                }
            }
            this.mPositionListeners[emptySlotIndex] = positionListener;
            this.mCanMove[emptySlotIndex] = canMove;
            this.mNumberOfListeners += Editor.TAP_STATE_FIRST_TAP;
        }

        public void removeSubscriber(TextViewPositionListener positionListener) {
            for (int i = Editor.TAP_STATE_INITIAL; i < Editor.MENU_ITEM_ORDER_PASTE_AS_PLAIN_TEXT; i += Editor.TAP_STATE_FIRST_TAP) {
                if (this.mPositionListeners[i] == positionListener) {
                    this.mPositionListeners[i] = null;
                    this.mNumberOfListeners += Editor.UNSET_X_VALUE;
                    break;
                }
            }
            if (this.mNumberOfListeners == 0) {
                this.this$0.mTextView.getViewTreeObserver().removeOnPreDrawListener(this);
            }
        }

        public int getPositionX() {
            return this.mPositionX;
        }

        public int getPositionY() {
            return this.mPositionY;
        }

        public int getPositionXOnScreen() {
            return this.mPositionXOnScreen;
        }

        public int getPositionYOnScreen() {
            return this.mPositionYOnScreen;
        }

        public boolean onPreDraw() {
            updatePosition();
            boolean hasSelection = this.this$0.mTextView != null ? this.this$0.mTextView.hasSelection() : Editor.DEBUG_UNDO;
            SelectionModifierCursorController selectionController = this.this$0.getSelectionController();
            boolean forceUpdate = (!hasSelection || selectionController == null || selectionController.isActive()) ? Editor.DEBUG_UNDO : true;
            int i = Editor.TAP_STATE_INITIAL;
            while (i < Editor.MENU_ITEM_ORDER_PASTE_AS_PLAIN_TEXT) {
                if (this.mPositionHasChanged || this.mScrollHasChanged || this.mCanMove[i] || forceUpdate) {
                    TextViewPositionListener positionListener = this.mPositionListeners[i];
                    if (positionListener != null) {
                        boolean z;
                        int i2 = this.mPositionX;
                        int i3 = this.mPositionY;
                        boolean z2 = this.mPositionHasChanged;
                        if (this.mScrollHasChanged) {
                            z = true;
                        } else {
                            z = forceUpdate;
                        }
                        positionListener.updatePosition(i2, i3, z2, z);
                    }
                }
                i += Editor.TAP_STATE_FIRST_TAP;
            }
            this.mScrollHasChanged = Editor.DEBUG_UNDO;
            return true;
        }

        private void updatePosition() {
            boolean z;
            this.this$0.mTextView.getLocationInWindow(this.mTempCoords);
            if (this.mTempCoords[Editor.TAP_STATE_INITIAL] == this.mPositionX && this.mTempCoords[Editor.TAP_STATE_FIRST_TAP] == this.mPositionY) {
                z = Editor.DEBUG_UNDO;
            } else {
                z = true;
            }
            this.mPositionHasChanged = z;
            this.mPositionX = this.mTempCoords[Editor.TAP_STATE_INITIAL];
            this.mPositionY = this.mTempCoords[Editor.TAP_STATE_FIRST_TAP];
            this.this$0.mTextView.getLocationOnScreen(this.mTempCoords);
            this.mPositionXOnScreen = this.mTempCoords[Editor.TAP_STATE_INITIAL];
            this.mPositionYOnScreen = this.mTempCoords[Editor.TAP_STATE_FIRST_TAP];
        }

        public void onScrollChanged() {
            this.mScrollHasChanged = true;
        }
    }

    static final class ProcessTextIntentActionsHandler {
        private final SparseArray<AccessibilityAction> mAccessibilityActions;
        private final SparseArray<Intent> mAccessibilityIntents;
        private final Editor mEditor;
        private final PackageManager mPackageManager;
        private final TextView mTextView;

        /* synthetic */ ProcessTextIntentActionsHandler(Editor editor, ProcessTextIntentActionsHandler processTextIntentActionsHandler) {
            this(editor);
        }

        private ProcessTextIntentActionsHandler(Editor editor) {
            this.mAccessibilityIntents = new SparseArray();
            this.mAccessibilityActions = new SparseArray();
            this.mEditor = (Editor) Preconditions.checkNotNull(editor);
            this.mTextView = (TextView) Preconditions.checkNotNull(this.mEditor.mTextView);
            this.mPackageManager = (PackageManager) Preconditions.checkNotNull(this.mTextView.getContext().getPackageManager());
        }

        public void onInitializeMenu(Menu menu) {
            int i = Editor.TAP_STATE_INITIAL;
            for (ResolveInfo resolveInfo : getSupportedActivities()) {
                int i2 = i + Editor.TAP_STATE_FIRST_TAP;
                menu.add((int) Editor.TAP_STATE_INITIAL, (int) Editor.TAP_STATE_INITIAL, i + Editor.MENU_ITEM_ORDER_PROCESS_TEXT_INTENT_ACTIONS_START, getLabel(resolveInfo)).setIntent(createProcessTextIntentForResolveInfo(resolveInfo)).setShowAsAction(Editor.TAP_STATE_FIRST_TAP);
                i = i2;
            }
        }

        public boolean performMenuItemAction(MenuItem item) {
            return fireIntent(item.getIntent());
        }

        public void initializeAccessibilityActions() {
            this.mAccessibilityIntents.clear();
            this.mAccessibilityActions.clear();
            int i = Editor.TAP_STATE_INITIAL;
            for (ResolveInfo resolveInfo : getSupportedActivities()) {
                int i2 = i + Editor.TAP_STATE_FIRST_TAP;
                int actionId = 268435712 + i;
                this.mAccessibilityActions.put(actionId, new AccessibilityAction(actionId, getLabel(resolveInfo)));
                this.mAccessibilityIntents.put(actionId, createProcessTextIntentForResolveInfo(resolveInfo));
                i = i2;
            }
        }

        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo nodeInfo) {
            for (int i = Editor.TAP_STATE_INITIAL; i < this.mAccessibilityActions.size(); i += Editor.TAP_STATE_FIRST_TAP) {
                nodeInfo.addAction((AccessibilityAction) this.mAccessibilityActions.valueAt(i));
            }
        }

        public boolean performAccessibilityAction(int actionId) {
            return fireIntent((Intent) this.mAccessibilityIntents.get(actionId));
        }

        private boolean fireIntent(Intent intent) {
            if (intent == null || !"android.intent.action.PROCESS_TEXT".equals(intent.getAction())) {
                return Editor.DEBUG_UNDO;
            }
            intent.putExtra("android.intent.extra.PROCESS_TEXT", this.mTextView.getSelectedText());
            this.mEditor.mPreserveSelection = true;
            this.mTextView.startActivityForResult(intent, 100);
            return true;
        }

        private List<ResolveInfo> getSupportedActivities() {
            return this.mTextView.getContext().getPackageManager().queryIntentActivities(createProcessTextIntent(), Editor.TAP_STATE_INITIAL);
        }

        private Intent createProcessTextIntentForResolveInfo(ResolveInfo info) {
            return createProcessTextIntent().putExtra("android.intent.extra.PROCESS_TEXT_READONLY", this.mTextView.isTextEditable() ? Editor.DEBUG_UNDO : true).setClassName(info.activityInfo.packageName, info.activityInfo.name);
        }

        private Intent createProcessTextIntent() {
            return new Intent().setAction("android.intent.action.PROCESS_TEXT").setType("text/plain");
        }

        private CharSequence getLabel(ResolveInfo resolveInfo) {
            return resolveInfo.loadLabel(this.mPackageManager);
        }
    }

    protected class SelectionHandleView extends HandleView {
        private final int mHandleType;
        private boolean mInWord;
        private boolean mLanguageDirectionChanged;
        private float mPrevX;
        private final float mTextViewEdgeSlop;
        private final int[] mTextViewLocation;
        private float mTouchWordDelta;
        final /* synthetic */ Editor this$0;

        public SelectionHandleView(Editor this$0, Drawable drawableLtr, Drawable drawableRtl, int id, int handleType) {
            this.this$0 = this$0;
            super(this$0, drawableLtr, drawableRtl, id, null);
            this.mInWord = Editor.DEBUG_UNDO;
            this.mLanguageDirectionChanged = Editor.DEBUG_UNDO;
            this.mTextViewLocation = new int[Editor.TAP_STATE_DOUBLE_TAP];
            this.mHandleType = handleType;
            this.mTextViewEdgeSlop = (float) (ViewConfiguration.get(this$0.mTextView.getContext()).getScaledTouchSlop() * Editor.MENU_ITEM_ORDER_CUT);
        }

        public boolean isStartHandle() {
            return this.mHandleType == 0 ? true : Editor.DEBUG_UNDO;
        }

        protected int getHotspotX(Drawable drawable, boolean isRtlRun) {
            if (isRtlRun == isStartHandle()) {
                return drawable.getIntrinsicWidth() / Editor.MENU_ITEM_ORDER_CUT;
            }
            return (drawable.getIntrinsicWidth() * Editor.TAP_STATE_TRIPLE_CLICK) / Editor.MENU_ITEM_ORDER_CUT;
        }

        protected int getHorizontalGravity(boolean isRtlRun) {
            return isRtlRun == isStartHandle() ? Editor.TAP_STATE_TRIPLE_CLICK : Editor.MENU_ITEM_ORDER_COPY;
        }

        public int getCurrentCursorOffset() {
            if (!isStartHandle()) {
                this.this$0.recogniseLineEnd();
            }
            return isStartHandle() ? this.this$0.mTextView.getSelectionStart() : this.this$0.mTextView.getSelectionEnd();
        }

        protected void updateSelection(int offset) {
            if (isStartHandle()) {
                Selection.setSelection((Spannable) this.this$0.mTextView.getText(), offset, this.this$0.mTextView.getSelectionEnd());
            } else {
                Selection.setSelection((Spannable) this.this$0.mTextView.getText(), this.this$0.mTextView.getSelectionStart(), offset);
            }
            updateDrawable();
            if (this.this$0.mTextActionMode != null) {
                this.this$0.mTextActionMode.invalidate();
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void updatePosition(float x, float y) {
            Layout layout = this.this$0.mTextView.getLayout();
            if (!isStartHandle()) {
                this.this$0.setPosIsLineEnd(Editor.DEBUG_UNDO);
            }
            if (layout == null) {
                positionAndAdjustForCrossingHandles(this.this$0.mTextView.getOffsetForPosition(x, y));
                return;
            }
            int offset;
            int wordEnd;
            int wordStart;
            int currentOffset;
            boolean rtlAtCurrentOffset;
            boolean atRtl;
            boolean isLvlBoundary;
            int i = this.mPreviousLineTouched;
            if (r0 == Editor.UNSET_X_VALUE) {
                this.mPreviousLineTouched = this.this$0.mTextView.getLineAtCoordinate(y);
            }
            boolean positionCursor = Editor.DEBUG_UNDO;
            int anotherHandleOffset = isStartHandle() ? this.this$0.mTextView.getSelectionEnd() : this.this$0.mTextView.getSelectionStart();
            int currLine = this.this$0.getCurrentLineAdjustedForSlop(layout, this.mPreviousLineTouched, y);
            int initialOffset = getOffsetAtCoordinate(layout, currLine, x);
            if (!isStartHandle() || initialOffset < anotherHandleOffset) {
                if (!isStartHandle() && initialOffset <= anotherHandleOffset) {
                }
                offset = initialOffset;
                wordEnd = this.this$0.getWordEnd(offset);
                wordStart = this.this$0.getWordStart(offset);
                if (this.mPrevX == LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
                    this.mPrevX = x;
                }
                currentOffset = getCurrentCursorOffset();
                rtlAtCurrentOffset = isAtRtlRun(layout, currentOffset);
                atRtl = isAtRtlRun(layout, offset);
                isLvlBoundary = layout.isLevelBoundary(offset);
                if (!isLvlBoundary || ((rtlAtCurrentOffset && !atRtl) || (!rtlAtCurrentOffset && atRtl))) {
                    this.mLanguageDirectionChanged = true;
                    this.mTouchWordDelta = 0.0f;
                    positionAndAdjustForCrossingHandles(offset);
                } else if (!this.mLanguageDirectionChanged || isLvlBoundary) {
                    boolean isExpanding;
                    float xDiff = x - this.mPrevX;
                    if (isStartHandle()) {
                        i = this.mPreviousLineTouched;
                        isExpanding = currLine < r0 ? true : Editor.DEBUG_UNDO;
                    } else {
                        i = this.mPreviousLineTouched;
                        isExpanding = currLine > r0 ? true : Editor.DEBUG_UNDO;
                    }
                    if (atRtl == isStartHandle()) {
                        isExpanding |= xDiff > 0.0f ? Editor.TAP_STATE_FIRST_TAP : Editor.TAP_STATE_INITIAL;
                    } else {
                        isExpanding |= xDiff < 0.0f ? Editor.TAP_STATE_FIRST_TAP : Editor.TAP_STATE_INITIAL;
                    }
                    float touchXOnScreen = ((this.mTouchToWindowOffsetX + x) - ((float) this.mLastParentX)) + ((float) this.mLastParentXOnScreen);
                    if (this.this$0.mTextView.getHorizontallyScrolling() && positionNearEdgeOfScrollingView(touchXOnScreen, atRtl)) {
                        if (isStartHandle()) {
                        }
                        if (!isStartHandle()) {
                        }
                    }
                    if (isExpanding) {
                        boolean snapToWord;
                        int offsetThresholdToSnap;
                        int wordBoundary = isStartHandle() ? wordStart : wordEnd;
                        if (this.mInWord) {
                            if (isStartHandle()) {
                                i = this.mPrevLine;
                            } else {
                                i = this.mPrevLine;
                            }
                            snapToWord = Editor.DEBUG_UNDO;
                            if (snapToWord) {
                                if (layout.getLineForOffset(wordBoundary) != currLine) {
                                    wordBoundary = isStartHandle() ? layout.getLineStart(currLine) : layout.getLineEnd(currLine);
                                }
                                if (isStartHandle()) {
                                    offsetThresholdToSnap = wordStart + ((wordBoundary - wordStart) / Editor.TAP_STATE_DOUBLE_TAP);
                                } else {
                                    offsetThresholdToSnap = wordEnd - ((wordEnd - wordBoundary) / Editor.TAP_STATE_DOUBLE_TAP);
                                }
                                if (isStartHandle()) {
                                    if (offset > offsetThresholdToSnap) {
                                        i = this.mPrevLine;
                                    }
                                    offset = wordStart;
                                }
                                if (!isStartHandle()) {
                                    if (offset < offsetThresholdToSnap) {
                                        i = this.mPrevLine;
                                    }
                                    offset = wordEnd;
                                    this.this$0.setPosIsLineEnd(true);
                                }
                                offset = this.mPreviousOffset;
                            }
                            if ((isStartHandle() || offset >= initialOffset) && (isStartHandle() || offset <= initialOffset)) {
                                this.mTouchWordDelta = 0.0f;
                            } else {
                                this.mTouchWordDelta = this.this$0.mTextView.convertToLocalHorizontalCoordinate(x) - getHorizontal(layout, offset);
                            }
                            positionCursor = true;
                        }
                        snapToWord = atRtl == isAtRtlRun(layout, wordBoundary) ? true : Editor.DEBUG_UNDO;
                        if (snapToWord) {
                            if (layout.getLineForOffset(wordBoundary) != currLine) {
                                if (isStartHandle()) {
                                }
                            }
                            if (isStartHandle()) {
                                offsetThresholdToSnap = wordStart + ((wordBoundary - wordStart) / Editor.TAP_STATE_DOUBLE_TAP);
                            } else {
                                offsetThresholdToSnap = wordEnd - ((wordEnd - wordBoundary) / Editor.TAP_STATE_DOUBLE_TAP);
                            }
                            if (isStartHandle()) {
                                if (offset > offsetThresholdToSnap) {
                                    i = this.mPrevLine;
                                }
                                offset = wordStart;
                            }
                            if (isStartHandle()) {
                                if (offset < offsetThresholdToSnap) {
                                    i = this.mPrevLine;
                                }
                                offset = wordEnd;
                                this.this$0.setPosIsLineEnd(true);
                            }
                            offset = this.mPreviousOffset;
                        }
                        if (isStartHandle()) {
                        }
                        this.mTouchWordDelta = 0.0f;
                        positionCursor = true;
                    } else {
                        boolean shrinking;
                        int adjustedOffset = getOffsetAtCoordinate(layout, currLine, x - this.mTouchWordDelta);
                        if (isStartHandle()) {
                            i = this.mPreviousOffset;
                            if (adjustedOffset <= r0) {
                                i = this.mPrevLine;
                                if (currLine <= r0) {
                                    shrinking = Editor.DEBUG_UNDO;
                                }
                            }
                            shrinking = true;
                        } else {
                            i = this.mPreviousOffset;
                            if (adjustedOffset >= r0) {
                                i = this.mPrevLine;
                                if (currLine >= r0) {
                                    shrinking = Editor.DEBUG_UNDO;
                                }
                            }
                            shrinking = true;
                        }
                        if (shrinking) {
                            i = this.mPrevLine;
                            if (currLine != r0) {
                                offset = isStartHandle() ? wordStart : wordEnd;
                                if ((!isStartHandle() || offset >= initialOffset) && (isStartHandle() || offset <= initialOffset)) {
                                    this.mTouchWordDelta = 0.0f;
                                } else {
                                    this.mTouchWordDelta = this.this$0.mTextView.convertToLocalHorizontalCoordinate(x) - getHorizontal(layout, offset);
                                }
                            } else {
                                offset = adjustedOffset;
                            }
                            positionCursor = true;
                        } else {
                            if (isStartHandle()) {
                                i = this.mPreviousOffset;
                            }
                            if (!isStartHandle()) {
                                i = this.mPreviousOffset;
                                if (adjustedOffset > r0) {
                                    this.mTouchWordDelta = this.this$0.mTextView.convertToLocalHorizontalCoordinate(x) - getHorizontal(layout, this.mPreviousOffset);
                                }
                            }
                        }
                    }
                    if (positionCursor) {
                        this.mPreviousLineTouched = currLine;
                        positionAndAdjustForCrossingHandles(offset);
                    }
                    this.mPrevX = x;
                    return;
                } else {
                    positionAndAdjustForCrossingHandles(offset);
                    this.mTouchWordDelta = 0.0f;
                    this.mLanguageDirectionChanged = Editor.DEBUG_UNDO;
                    return;
                }
            }
            currLine = layout.getLineForOffset(anotherHandleOffset);
            initialOffset = getOffsetAtCoordinate(layout, currLine, x);
            offset = initialOffset;
            wordEnd = this.this$0.getWordEnd(offset);
            wordStart = this.this$0.getWordStart(offset);
            if (this.mPrevX == LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
                this.mPrevX = x;
            }
            currentOffset = getCurrentCursorOffset();
            rtlAtCurrentOffset = isAtRtlRun(layout, currentOffset);
            atRtl = isAtRtlRun(layout, offset);
            isLvlBoundary = layout.isLevelBoundary(offset);
            if (isLvlBoundary) {
            }
            this.mLanguageDirectionChanged = true;
            this.mTouchWordDelta = 0.0f;
            positionAndAdjustForCrossingHandles(offset);
        }

        protected void positionAtCursorOffset(int offset, boolean forceUpdatePosition) {
            boolean z = Editor.DEBUG_UNDO;
            offset = this.this$0.resetOffsetForImageSpan(offset, isStartHandle());
            super.positionAtCursorOffset(offset, forceUpdatePosition);
            if (!(offset == Editor.UNSET_X_VALUE || this.this$0.getWordIteratorWithText().isBoundary(offset))) {
                z = true;
            }
            this.mInWord = z;
        }

        public boolean onTouchEvent(MotionEvent event) {
            boolean superResult = super.onTouchEvent(event);
            if (event.getActionMasked() == 0) {
                this.mTouchWordDelta = 0.0f;
                this.mPrevX = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            }
            return superResult;
        }

        private void positionAndAdjustForCrossingHandles(int offset) {
            boolean z = true;
            int anotherHandleOffset = isStartHandle() ? this.this$0.mTextView.getSelectionEnd() : this.this$0.mTextView.getSelectionStart();
            if ((isStartHandle() && offset >= anotherHandleOffset) || (!isStartHandle() && offset <= anotherHandleOffset)) {
                this.mTouchWordDelta = 0.0f;
                Layout layout = this.this$0.mTextView.getLayout();
                if (!(layout == null || offset == anotherHandleOffset)) {
                    boolean z2;
                    float horiz = getHorizontal(layout, offset);
                    if (isStartHandle()) {
                        z2 = Editor.DEBUG_UNDO;
                    } else {
                        z2 = true;
                    }
                    float anotherHandleHoriz = getHorizontal(layout, anotherHandleOffset, z2);
                    float currentHoriz = getHorizontal(layout, this.mPreviousOffset);
                    if ((currentHoriz < anotherHandleHoriz && horiz < anotherHandleHoriz) || (currentHoriz > anotherHandleHoriz && horiz > anotherHandleHoriz)) {
                        int offsetToGetRunRange;
                        int currentOffset = getCurrentCursorOffset();
                        if (isStartHandle()) {
                            offsetToGetRunRange = currentOffset;
                        } else {
                            offsetToGetRunRange = Math.max(currentOffset + Editor.UNSET_X_VALUE, Editor.TAP_STATE_INITIAL);
                        }
                        long range = layout.getRunRange(offsetToGetRunRange);
                        if (isStartHandle()) {
                            offset = TextUtils.unpackRangeStartFromLong(range);
                        } else {
                            offset = TextUtils.unpackRangeEndFromLong(range);
                        }
                        positionAtCursorOffset(offset, Editor.DEBUG_UNDO);
                        return;
                    }
                }
                Editor editor = this.this$0;
                if (isStartHandle()) {
                    z = Editor.DEBUG_UNDO;
                }
                offset = editor.getNextCursorOffset(anotherHandleOffset, z);
            }
            positionAtCursorOffset(offset, Editor.DEBUG_UNDO);
        }

        private boolean positionNearEdgeOfScrollingView(float x, boolean atRtl) {
            this.this$0.mTextView.getLocationOnScreen(this.mTextViewLocation);
            return atRtl == isStartHandle() ? x > ((float) ((this.mTextViewLocation[Editor.TAP_STATE_INITIAL] + this.this$0.mTextView.getWidth()) - this.this$0.mTextView.getPaddingRight())) - this.mTextViewEdgeSlop ? true : Editor.DEBUG_UNDO : x < ((float) (this.mTextViewLocation[Editor.TAP_STATE_INITIAL] + this.this$0.mTextView.getPaddingLeft())) + this.mTextViewEdgeSlop ? true : Editor.DEBUG_UNDO;
        }

        protected boolean isAtRtlRun(Layout layout, int offset) {
            return layout.isRtlCharAt(isStartHandle() ? offset : Math.max(offset + Editor.UNSET_X_VALUE, Editor.TAP_STATE_INITIAL));
        }

        public float getHorizontal(Layout layout, int offset) {
            return getHorizontal(layout, offset, isStartHandle());
        }

        private float getHorizontal(Layout layout, int offset, boolean startHandle) {
            boolean isRtlParagraph = Editor.DEBUG_UNDO;
            int line = layout.getLineForOffset(offset);
            boolean isRtlChar = layout.isRtlCharAt(startHandle ? offset : Math.max(offset + Editor.UNSET_X_VALUE, Editor.TAP_STATE_INITIAL));
            if (layout.getParagraphDirection(line) == Editor.UNSET_X_VALUE) {
                isRtlParagraph = true;
            }
            return isRtlChar == isRtlParagraph ? layout.getPrimaryHorizontal(offset) : layout.getSecondaryHorizontal(offset);
        }

        protected int getOffsetAtCoordinate(Layout layout, int line, float x) {
            boolean isRtlParagraph = true;
            float localX = this.this$0.mTextView.convertToLocalHorizontalCoordinate(x);
            int primaryOffset = layout.getOffsetForHorizontal(line, localX, true);
            if (!layout.isLevelBoundary(primaryOffset)) {
                return primaryOffset;
            }
            int secondaryOffset = layout.getOffsetForHorizontal(line, localX, Editor.DEBUG_UNDO);
            int currentOffset = getCurrentCursorOffset();
            int primaryDiff = Math.abs(primaryOffset - currentOffset);
            int secondaryDiff = Math.abs(secondaryOffset - currentOffset);
            if (primaryDiff < secondaryDiff) {
                return primaryOffset;
            }
            if (primaryDiff > secondaryDiff) {
                return secondaryOffset;
            }
            int offsetToCheck;
            if (isStartHandle()) {
                offsetToCheck = currentOffset;
            } else {
                offsetToCheck = Math.max(currentOffset + Editor.UNSET_X_VALUE, Editor.TAP_STATE_INITIAL);
            }
            boolean isRtlChar = layout.isRtlCharAt(offsetToCheck);
            if (layout.getParagraphDirection(line) != Editor.UNSET_X_VALUE) {
                isRtlParagraph = Editor.DEBUG_UNDO;
            }
            if (isRtlChar != isRtlParagraph) {
                primaryOffset = secondaryOffset;
            }
            return primaryOffset;
        }
    }

    class SelectionModifierCursorController implements CursorController {
        private static final int DRAG_ACCELERATOR_MODE_CHARACTER = 1;
        private static final int DRAG_ACCELERATOR_MODE_INACTIVE = 0;
        private static final int DRAG_ACCELERATOR_MODE_PARAGRAPH = 3;
        private static final int DRAG_ACCELERATOR_MODE_WORD = 2;
        private float mDownPositionX;
        private float mDownPositionY;
        private int mDragAcceleratorMode;
        private SelectionHandleView mEndHandle;
        private boolean mGestureStayedInTapRegion;
        private boolean mHaventMovedEnoughToStartDrag;
        private int mLineSelectionIsOn;
        private int mMaxTouchOffset;
        private int mMinTouchOffset;
        private SelectionHandleView mStartHandle;
        private int mStartOffset;
        private boolean mSwitchedLines;
        final /* synthetic */ Editor this$0;

        SelectionModifierCursorController(Editor this$0) {
            this.this$0 = this$0;
            this.mStartOffset = Editor.UNSET_X_VALUE;
            this.mLineSelectionIsOn = Editor.UNSET_X_VALUE;
            this.mSwitchedLines = Editor.DEBUG_UNDO;
            this.mDragAcceleratorMode = DRAG_ACCELERATOR_MODE_INACTIVE;
            resetTouchOffsets();
        }

        public void show() {
            if (!this.this$0.mTextView.isInBatchEditMode()) {
                initDrawables();
                initHandles();
            }
        }

        private void initDrawables() {
            if (this.this$0.mSelectHandleLeft == null) {
                this.this$0.mSelectHandleLeft = this.this$0.mTextView.getContext().getDrawable(this.this$0.mTextView.mTextSelectHandleLeftRes);
            }
            if (this.this$0.mSelectHandleRight == null) {
                this.this$0.mSelectHandleRight = this.this$0.mTextView.getContext().getDrawable(this.this$0.mTextView.mTextSelectHandleRightRes);
            }
        }

        private void initHandles() {
            if (this.mStartHandle == null) {
                this.mStartHandle = new SelectionHandleView(this.this$0, this.this$0.mSelectHandleLeft, this.this$0.mSelectHandleRight, R.id.selection_start_handle, DRAG_ACCELERATOR_MODE_INACTIVE);
            }
            if (this.mEndHandle == null) {
                this.mEndHandle = new SelectionHandleView(this.this$0, this.this$0.mSelectHandleRight, this.this$0.mSelectHandleLeft, R.id.selection_end_handle, DRAG_ACCELERATOR_MODE_CHARACTER);
            }
            this.mStartHandle.show();
            this.mEndHandle.show();
            this.this$0.hideInsertionPointCursorController();
        }

        public void hide() {
            if (this.mStartHandle != null) {
                this.mStartHandle.hide();
            }
            if (this.mEndHandle != null) {
                this.mEndHandle.hide();
            }
        }

        public void enterDrag(int dragAcceleratorMode) {
            show();
            this.mDragAcceleratorMode = dragAcceleratorMode;
            this.mStartOffset = this.this$0.mTextView.getOffsetForPosition(this.this$0.mLastDownPositionX, this.this$0.mLastDownPositionY);
            this.mLineSelectionIsOn = this.this$0.mTextView.getLineAtCoordinate(this.this$0.mLastDownPositionY);
            hide();
            this.this$0.mTextView.getParent().requestDisallowInterceptTouchEvent(true);
            this.this$0.mTextView.cancelLongPress();
        }

        public void onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            boolean isMouse = event.isFromSource(InputDevice.SOURCE_MOUSE);
            float deltaX;
            float deltaY;
            float distanceSquared;
            switch (event.getActionMasked()) {
                case DRAG_ACCELERATOR_MODE_INACTIVE /*0*/:
                    if (this.this$0.extractedTextModeWillBeStarted()) {
                        hide();
                        return;
                    }
                    int offsetForPosition = this.this$0.mTextView.getOffsetForPosition(eventX, eventY);
                    this.mMaxTouchOffset = offsetForPosition;
                    this.mMinTouchOffset = offsetForPosition;
                    if (this.mGestureStayedInTapRegion && (this.this$0.mTapState == DRAG_ACCELERATOR_MODE_WORD || this.this$0.mTapState == DRAG_ACCELERATOR_MODE_PARAGRAPH)) {
                        deltaX = eventX - this.mDownPositionX;
                        deltaY = eventY - this.mDownPositionY;
                        distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);
                        int doubleTapSlop = ViewConfiguration.get(this.this$0.mTextView.getContext()).getScaledDoubleTapSlop();
                        if ((distanceSquared < ((float) (doubleTapSlop * doubleTapSlop)) ? true : Editor.DEBUG_UNDO) && (isMouse || this.this$0.isPositionOnText(eventX, eventY))) {
                            if (this.this$0.mTapState == DRAG_ACCELERATOR_MODE_WORD) {
                                this.this$0.selectCurrentWordAndStartDrag();
                            } else if (this.this$0.mTapState == DRAG_ACCELERATOR_MODE_PARAGRAPH) {
                                selectCurrentParagraphAndStartDrag();
                            }
                            this.this$0.mDiscardNextActionUp = true;
                        }
                    }
                    this.mDownPositionX = eventX;
                    this.mDownPositionY = eventY;
                    this.mGestureStayedInTapRegion = true;
                    this.mHaventMovedEnoughToStartDrag = true;
                case DRAG_ACCELERATOR_MODE_CHARACTER /*1*/:
                    if (isDragAcceleratorActive()) {
                        updateSelection(event);
                        this.this$0.mTextView.getParent().requestDisallowInterceptTouchEvent(Editor.DEBUG_UNDO);
                        resetDragAcceleratorState();
                        if (this.this$0.mTextView.hasSelection()) {
                            this.this$0.startSelectionActionMode();
                        }
                    }
                case DRAG_ACCELERATOR_MODE_WORD /*2*/:
                    ViewConfiguration viewConfig = ViewConfiguration.get(this.this$0.mTextView.getContext());
                    int touchSlop = viewConfig.getScaledTouchSlop();
                    if (this.mGestureStayedInTapRegion || this.mHaventMovedEnoughToStartDrag) {
                        deltaX = eventX - this.mDownPositionX;
                        deltaY = eventY - this.mDownPositionY;
                        distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);
                        if (this.mGestureStayedInTapRegion) {
                            int doubleTapTouchSlop = viewConfig.getScaledDoubleTapTouchSlop();
                            this.mGestureStayedInTapRegion = distanceSquared <= ((float) (doubleTapTouchSlop * doubleTapTouchSlop)) ? true : Editor.DEBUG_UNDO;
                        }
                        if (this.mHaventMovedEnoughToStartDrag) {
                            this.mHaventMovedEnoughToStartDrag = distanceSquared <= ((float) (touchSlop * touchSlop)) ? true : Editor.DEBUG_UNDO;
                        }
                    }
                    if (isMouse && !isDragAcceleratorActive()) {
                        int offset = this.this$0.mTextView.getOffsetForPosition(eventX, eventY);
                        if (this.this$0.mTextView.hasSelection() && ((!this.mHaventMovedEnoughToStartDrag || this.mStartOffset != offset) && offset >= this.this$0.mTextView.getSelectionStart() && offset <= this.this$0.mTextView.getSelectionEnd())) {
                            this.this$0.startDragAndDrop();
                            return;
                        } else if (this.mStartOffset != offset) {
                            this.this$0.stopTextActionMode();
                            enterDrag(DRAG_ACCELERATOR_MODE_CHARACTER);
                            this.this$0.mDiscardNextActionUp = true;
                            this.mHaventMovedEnoughToStartDrag = Editor.DEBUG_UNDO;
                        }
                    }
                    if (this.mStartHandle == null || !this.mStartHandle.isShowing()) {
                        updateSelection(event);
                    }
                case Editor.MENU_ITEM_ORDER_COPY /*5*/:
                case Editor.MENU_ITEM_ORDER_PASTE /*6*/:
                    if (this.this$0.mTextView.getContext().getPackageManager().hasSystemFeature("android.hardware.touchscreen.multitouch.distinct")) {
                        updateMinAndMaxOffsets(event);
                    }
                default:
            }
        }

        private void updateSelection(MotionEvent event) {
            if (this.this$0.mTextView.getLayout() != null) {
                switch (this.mDragAcceleratorMode) {
                    case DRAG_ACCELERATOR_MODE_CHARACTER /*1*/:
                        updateCharacterBasedSelection(event);
                    case DRAG_ACCELERATOR_MODE_WORD /*2*/:
                        updateWordBasedSelection(event);
                    case DRAG_ACCELERATOR_MODE_PARAGRAPH /*3*/:
                        updateParagraphBasedSelection(event);
                    default:
                }
            }
        }

        private boolean selectCurrentParagraphAndStartDrag() {
            if (this.this$0.mInsertionActionModeRunnable != null) {
                this.this$0.mTextView.removeCallbacks(this.this$0.mInsertionActionModeRunnable);
            }
            this.this$0.stopTextActionMode();
            if (!this.this$0.selectCurrentParagraph()) {
                return Editor.DEBUG_UNDO;
            }
            enterDrag(DRAG_ACCELERATOR_MODE_PARAGRAPH);
            return true;
        }

        private void updateCharacterBasedSelection(MotionEvent event) {
            Selection.setSelection((Spannable) this.this$0.mTextView.getText(), this.mStartOffset, this.this$0.mTextView.getOffsetForPosition(event.getX(), event.getY()));
        }

        private void updateWordBasedSelection(MotionEvent event) {
            if (!this.mHaventMovedEnoughToStartDrag) {
                int currLine;
                int startOffset;
                boolean isMouse = event.isFromSource(InputDevice.SOURCE_MOUSE);
                ViewConfiguration viewConfig = ViewConfiguration.get(this.this$0.mTextView.getContext());
                float eventX = event.getX();
                float eventY = event.getY();
                if (isMouse) {
                    currLine = this.this$0.mTextView.getLineAtCoordinate(eventY);
                } else {
                    float y = eventY;
                    if (this.mSwitchedLines) {
                        float fingerOffset;
                        int touchSlop = viewConfig.getScaledTouchSlop();
                        if (this.mStartHandle != null) {
                            fingerOffset = this.mStartHandle.getIdealVerticalOffset();
                        } else {
                            fingerOffset = (float) touchSlop;
                        }
                        y = eventY - fingerOffset;
                    }
                    currLine = this.this$0.getCurrentLineAdjustedForSlop(this.this$0.mTextView.getLayout(), this.mLineSelectionIsOn, y);
                    if (!(this.mSwitchedLines || currLine == this.mLineSelectionIsOn)) {
                        this.mSwitchedLines = true;
                        return;
                    }
                }
                int offset = this.this$0.mTextView.getOffsetAtCoordinate(currLine, eventX);
                if (this.mStartOffset < offset) {
                    offset = this.this$0.getWordEnd(offset);
                    startOffset = this.this$0.getWordStart(this.mStartOffset);
                } else {
                    offset = this.this$0.getWordStart(offset);
                    startOffset = this.this$0.getWordEnd(this.mStartOffset);
                }
                this.mLineSelectionIsOn = currLine;
                Selection.setSelection((Spannable) this.this$0.mTextView.getText(), startOffset, offset);
            }
        }

        private void updateParagraphBasedSelection(MotionEvent event) {
            int offset = this.this$0.mTextView.getOffsetForPosition(event.getX(), event.getY());
            long paragraphsRange = this.this$0.getParagraphsRange(Math.min(offset, this.mStartOffset), Math.max(offset, this.mStartOffset));
            Selection.setSelection((Spannable) this.this$0.mTextView.getText(), TextUtils.unpackRangeStartFromLong(paragraphsRange), TextUtils.unpackRangeEndFromLong(paragraphsRange));
        }

        private void updateMinAndMaxOffsets(MotionEvent event) {
            int pointerCount = event.getPointerCount();
            for (int index = DRAG_ACCELERATOR_MODE_INACTIVE; index < pointerCount; index += DRAG_ACCELERATOR_MODE_CHARACTER) {
                int offset = this.this$0.mTextView.getOffsetForPosition(event.getX(index), event.getY(index));
                if (offset < this.mMinTouchOffset) {
                    this.mMinTouchOffset = offset;
                }
                if (offset > this.mMaxTouchOffset) {
                    this.mMaxTouchOffset = offset;
                }
            }
        }

        public int getMinTouchOffset() {
            return this.mMinTouchOffset;
        }

        public int getMaxTouchOffset() {
            return this.mMaxTouchOffset;
        }

        public void resetTouchOffsets() {
            this.mMaxTouchOffset = Editor.UNSET_X_VALUE;
            this.mMinTouchOffset = Editor.UNSET_X_VALUE;
            resetDragAcceleratorState();
        }

        private void resetDragAcceleratorState() {
            this.mStartOffset = Editor.UNSET_X_VALUE;
            this.mDragAcceleratorMode = DRAG_ACCELERATOR_MODE_INACTIVE;
            this.mSwitchedLines = Editor.DEBUG_UNDO;
            int selectionStart = this.this$0.mTextView.getSelectionStart();
            int selectionEnd = this.this$0.mTextView.getSelectionEnd();
            if (selectionStart > selectionEnd) {
                Selection.setSelection((Spannable) this.this$0.mTextView.getText(), selectionEnd, selectionStart);
            }
        }

        public boolean isSelectionStartDragged() {
            return this.mStartHandle != null ? this.mStartHandle.isDragging() : Editor.DEBUG_UNDO;
        }

        public boolean isCursorBeingModified() {
            if (isDragAcceleratorActive() || isSelectionStartDragged()) {
                return true;
            }
            return this.mEndHandle != null ? this.mEndHandle.isDragging() : Editor.DEBUG_UNDO;
        }

        public boolean isDragAcceleratorActive() {
            return this.mDragAcceleratorMode != 0 ? true : Editor.DEBUG_UNDO;
        }

        public void onTouchModeChanged(boolean isInTouchMode) {
            if (!isInTouchMode) {
                hide();
            }
        }

        public void onDetached() {
            this.this$0.mTextView.getViewTreeObserver().removeOnTouchModeChangeListener(this);
            if (this.mStartHandle != null) {
                this.mStartHandle.onDetached();
            }
            if (this.mEndHandle != null) {
                this.mEndHandle.onDetached();
            }
        }

        public boolean isActive() {
            if (this.mStartHandle == null || !this.mStartHandle.isShowing() || this.mEndHandle == null) {
                return Editor.DEBUG_UNDO;
            }
            return this.mEndHandle.isShowing();
        }

        public void invalidateHandles() {
            if (this.mStartHandle != null) {
                this.mStartHandle.invalidate();
            }
            if (this.mEndHandle != null) {
                this.mEndHandle.invalidate();
            }
        }
    }

    class SpanController implements SpanWatcher {
        private static final int DISPLAY_TIMEOUT_MS = 3000;
        private Runnable mHidePopup;
        private EasyEditPopupWindow mPopupWindow;
        final /* synthetic */ Editor this$0;

        /* renamed from: android.widget.Editor.SpanController.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ SpanController this$1;

            AnonymousClass1(SpanController this$1) {
                this.this$1 = this$1;
            }

            public void run() {
                this.this$1.hide();
            }
        }

        /* renamed from: android.widget.Editor.SpanController.2 */
        class AnonymousClass2 implements EasyEditDeleteListener {
            final /* synthetic */ SpanController this$1;

            AnonymousClass2(SpanController this$1) {
                this.this$1 = this$1;
            }

            public void onDeleteClick(EasyEditSpan span) {
                Editable editable = (Editable) this.this$1.this$0.mTextView.getText();
                int start = editable.getSpanStart(span);
                int end = editable.getSpanEnd(span);
                if (start >= 0 && end >= 0) {
                    this.this$1.sendEasySpanNotification(Editor.TAP_STATE_FIRST_TAP, span);
                    this.this$1.this$0.mTextView.deleteText_internal(start, end);
                }
                editable.removeSpan(span);
            }
        }

        SpanController(Editor this$0) {
            this.this$0 = this$0;
        }

        private boolean isNonIntermediateSelectionSpan(Spannable text, Object span) {
            if ((Selection.SELECTION_START == span || Selection.SELECTION_END == span) && (text.getSpanFlags(span) & GL10.GL_NEVER) == 0) {
                return true;
            }
            return Editor.DEBUG_UNDO;
        }

        public void onSpanAdded(Spannable text, Object span, int start, int end) {
            if (isNonIntermediateSelectionSpan(text, span)) {
                this.this$0.sendUpdateSelection();
            } else if (span instanceof EasyEditSpan) {
                if (this.mPopupWindow == null) {
                    this.mPopupWindow = new EasyEditPopupWindow(null);
                    this.mHidePopup = new AnonymousClass1(this);
                }
                if (this.mPopupWindow.mEasyEditSpan != null) {
                    this.mPopupWindow.mEasyEditSpan.setDeleteEnabled(Editor.DEBUG_UNDO);
                }
                this.mPopupWindow.setEasyEditSpan((EasyEditSpan) span);
                this.mPopupWindow.setOnDeleteListener(new AnonymousClass2(this));
                if (this.this$0.mTextView.getWindowVisibility() == 0 && this.this$0.mTextView.getLayout() != null && !this.this$0.extractedTextModeWillBeStarted()) {
                    this.mPopupWindow.show();
                    this.this$0.mTextView.removeCallbacks(this.mHidePopup);
                    this.this$0.mTextView.postDelayed(this.mHidePopup, 3000);
                }
            }
        }

        public void onSpanRemoved(Spannable text, Object span, int start, int end) {
            if (isNonIntermediateSelectionSpan(text, span)) {
                this.this$0.sendUpdateSelection();
            } else if (this.mPopupWindow != null && span == this.mPopupWindow.mEasyEditSpan) {
                hide();
            }
        }

        public void onSpanChanged(Spannable text, Object span, int previousStart, int previousEnd, int newStart, int newEnd) {
            if (isNonIntermediateSelectionSpan(text, span)) {
                this.this$0.sendUpdateSelection();
            } else if (this.mPopupWindow != null && (span instanceof EasyEditSpan)) {
                EasyEditSpan easyEditSpan = (EasyEditSpan) span;
                sendEasySpanNotification(Editor.TAP_STATE_DOUBLE_TAP, easyEditSpan);
                text.removeSpan(easyEditSpan);
            }
        }

        public void hide() {
            if (this.mPopupWindow != null) {
                this.mPopupWindow.hide();
                this.this$0.mTextView.removeCallbacks(this.mHidePopup);
            }
        }

        private void sendEasySpanNotification(int textChangedType, EasyEditSpan span) {
            try {
                PendingIntent pendingIntent = span.getPendingIntent();
                if (pendingIntent != null) {
                    Intent intent = new Intent();
                    intent.putExtra(EasyEditSpan.EXTRA_TEXT_CHANGED_TYPE, textChangedType);
                    pendingIntent.send(this.this$0.mTextView.getContext(), Editor.TAP_STATE_INITIAL, intent);
                }
            } catch (CanceledException e) {
                Log.w(Editor.UNDO_OWNER_TAG, "PendingIntent for notification cannot be sent", e);
            }
        }
    }

    private class SuggestionHelper {
        private final HashMap<SuggestionSpan, Integer> mSpansLengths;
        private final Comparator<SuggestionSpan> mSuggestionSpanComparator;
        final /* synthetic */ Editor this$0;

        private class SuggestionSpanComparator implements Comparator<SuggestionSpan> {
            final /* synthetic */ SuggestionHelper this$1;

            /* synthetic */ SuggestionSpanComparator(SuggestionHelper this$1, SuggestionSpanComparator suggestionSpanComparator) {
                this(this$1);
            }

            private SuggestionSpanComparator(SuggestionHelper this$1) {
                this.this$1 = this$1;
            }

            public /* bridge */ /* synthetic */ int compare(Object span1, Object span2) {
                return compare((SuggestionSpan) span1, (SuggestionSpan) span2);
            }

            public int compare(SuggestionSpan span1, SuggestionSpan span2) {
                int flag1 = span1.getFlags();
                int flag2 = span2.getFlags();
                if (flag1 != flag2) {
                    boolean easy1 = (flag1 & Editor.TAP_STATE_FIRST_TAP) != 0 ? true : Editor.DEBUG_UNDO;
                    boolean easy2 = (flag2 & Editor.TAP_STATE_FIRST_TAP) != 0 ? true : Editor.DEBUG_UNDO;
                    boolean misspelled1 = (flag1 & Editor.TAP_STATE_DOUBLE_TAP) != 0 ? true : Editor.DEBUG_UNDO;
                    boolean misspelled2 = (flag2 & Editor.TAP_STATE_DOUBLE_TAP) != 0 ? true : Editor.DEBUG_UNDO;
                    if (easy1 && !misspelled1) {
                        return Editor.UNSET_X_VALUE;
                    }
                    if (easy2 && !misspelled2) {
                        return Editor.TAP_STATE_FIRST_TAP;
                    }
                    if (misspelled1) {
                        return Editor.UNSET_X_VALUE;
                    }
                    if (misspelled2) {
                        return Editor.TAP_STATE_FIRST_TAP;
                    }
                }
                return ((Integer) this.this$1.mSpansLengths.get(span1)).intValue() - ((Integer) this.this$1.mSpansLengths.get(span2)).intValue();
            }
        }

        /* synthetic */ SuggestionHelper(Editor this$0, SuggestionHelper suggestionHelper) {
            this(this$0);
        }

        private SuggestionHelper(Editor this$0) {
            this.this$0 = this$0;
            this.mSuggestionSpanComparator = new SuggestionSpanComparator();
            this.mSpansLengths = new HashMap();
        }

        private SuggestionSpan[] getSortedSuggestionSpans() {
            int pos = this.this$0.mTextView.getSelectionStart();
            Spannable spannable = (Spannable) this.this$0.mTextView.getText();
            SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) spannable.getSpans(pos, pos, SuggestionSpan.class);
            this.mSpansLengths.clear();
            int length = suggestionSpans.length;
            for (int i = Editor.TAP_STATE_INITIAL; i < length; i += Editor.TAP_STATE_FIRST_TAP) {
                SuggestionSpan suggestionSpan = suggestionSpans[i];
                int start = spannable.getSpanStart(suggestionSpan);
                this.mSpansLengths.put(suggestionSpan, Integer.valueOf(spannable.getSpanEnd(suggestionSpan) - start));
            }
            Arrays.sort(suggestionSpans, this.mSuggestionSpanComparator);
            this.mSpansLengths.clear();
            return suggestionSpans;
        }

        public int getSuggestionInfo(SuggestionInfo[] suggestionInfos, SuggestionSpanInfo misspelledSpanInfo) {
            Spannable spannable = (Spannable) this.this$0.mTextView.getText();
            SuggestionSpan[] suggestionSpans = getSortedSuggestionSpans();
            if (suggestionSpans.length == 0) {
                return Editor.TAP_STATE_INITIAL;
            }
            int numberOfSuggestions = Editor.TAP_STATE_INITIAL;
            int length = suggestionSpans.length;
            for (int i = Editor.TAP_STATE_INITIAL; i < length; i += Editor.TAP_STATE_FIRST_TAP) {
                SuggestionSpan suggestionSpan = suggestionSpans[i];
                int spanStart = spannable.getSpanStart(suggestionSpan);
                int spanEnd = spannable.getSpanEnd(suggestionSpan);
                if (!(misspelledSpanInfo == null || (suggestionSpan.getFlags() & Editor.TAP_STATE_DOUBLE_TAP) == 0)) {
                    misspelledSpanInfo.mSuggestionSpan = suggestionSpan;
                    misspelledSpanInfo.mSpanStart = spanStart;
                    misspelledSpanInfo.mSpanEnd = spanEnd;
                }
                String[] suggestions = suggestionSpan.getSuggestions();
                int nbSuggestions = suggestions.length;
                for (int suggestionIndex = Editor.TAP_STATE_INITIAL; suggestionIndex < nbSuggestions; suggestionIndex += Editor.TAP_STATE_FIRST_TAP) {
                    CharSequence suggestion = suggestions[suggestionIndex];
                    for (int i2 = Editor.TAP_STATE_INITIAL; i2 < numberOfSuggestions; i2 += Editor.TAP_STATE_FIRST_TAP) {
                        SuggestionInfo otherSuggestionInfo = suggestionInfos[i2];
                        if (otherSuggestionInfo.mText.toString().equals(suggestion)) {
                            int otherSpanStart = otherSuggestionInfo.mSuggestionSpanInfo.mSpanStart;
                            int otherSpanEnd = otherSuggestionInfo.mSuggestionSpanInfo.mSpanEnd;
                            if (spanStart == otherSpanStart && spanEnd == otherSpanEnd) {
                                break;
                            }
                        }
                    }
                    SuggestionInfo suggestionInfo = suggestionInfos[numberOfSuggestions];
                    suggestionInfo.setSpanInfo(suggestionSpan, spanStart, spanEnd);
                    suggestionInfo.mSuggestionIndex = suggestionIndex;
                    suggestionInfo.mSuggestionStart = Editor.TAP_STATE_INITIAL;
                    suggestionInfo.mSuggestionEnd = suggestion.length();
                    suggestionInfo.mText.replace(Editor.TAP_STATE_INITIAL, suggestionInfo.mText.length(), suggestion);
                    numberOfSuggestions += Editor.TAP_STATE_FIRST_TAP;
                    int length2 = suggestionInfos.length;
                    if (numberOfSuggestions >= r0) {
                        return numberOfSuggestions;
                    }
                }
            }
            return numberOfSuggestions;
        }
    }

    private static final class SuggestionInfo {
        int mSuggestionEnd;
        int mSuggestionIndex;
        final SuggestionSpanInfo mSuggestionSpanInfo;
        int mSuggestionStart;
        final SpannableStringBuilder mText;

        /* synthetic */ SuggestionInfo(SuggestionInfo suggestionInfo) {
            this();
        }

        private SuggestionInfo() {
            this.mSuggestionSpanInfo = new SuggestionSpanInfo();
            this.mText = new SpannableStringBuilder();
        }

        void clear() {
            this.mSuggestionSpanInfo.clear();
            this.mText.clear();
        }

        void setSpanInfo(SuggestionSpan span, int spanStart, int spanEnd) {
            this.mSuggestionSpanInfo.mSuggestionSpan = span;
            this.mSuggestionSpanInfo.mSpanStart = spanStart;
            this.mSuggestionSpanInfo.mSpanEnd = spanEnd;
        }
    }

    private static final class SuggestionSpanInfo {
        int mSpanEnd;
        int mSpanStart;
        SuggestionSpan mSuggestionSpan;

        /* synthetic */ SuggestionSpanInfo(SuggestionSpanInfo suggestionSpanInfo) {
            this();
        }

        private SuggestionSpanInfo() {
        }

        void clear() {
            this.mSuggestionSpan = null;
        }
    }

    public class SuggestionsPopupWindow extends PinnedPopupWindow implements OnItemClickListener {
        private static final int MAX_NUMBER_SUGGESTIONS = 5;
        private static final String USER_DICTIONARY_EXTRA_LOCALE = "locale";
        private static final String USER_DICTIONARY_EXTRA_WORD = "word";
        private TextView mAddToDictionaryButton;
        private int mContainerMarginTop;
        private int mContainerMarginWidth;
        private LinearLayout mContainerView;
        private Context mContext;
        private boolean mCursorWasVisibleBeforeSuggestions;
        private TextView mDeleteButton;
        private TextAppearanceSpan mHighlightSpan;
        private boolean mIsShowingUp;
        private final SuggestionSpanInfo mMisspelledSpanInfo;
        private int mNumberOfSuggestions;
        private SuggestionInfo[] mSuggestionInfos;
        private ListView mSuggestionListView;
        private SuggestionAdapter mSuggestionsAdapter;
        final /* synthetic */ Editor this$0;

        /* renamed from: android.widget.Editor.SuggestionsPopupWindow.1 */
        class AnonymousClass1 implements OnClickListener {
            final /* synthetic */ SuggestionsPopupWindow this$1;

            AnonymousClass1(SuggestionsPopupWindow this$1) {
                this.this$1 = this$1;
            }

            public void onClick(View v) {
                SuggestionSpan misspelledSpan = this.this$1.this$0.findEquivalentSuggestionSpan(this.this$1.mMisspelledSpanInfo);
                if (misspelledSpan != null) {
                    Editable editable = (Editable) this.this$1.this$0.mTextView.getText();
                    int spanStart = editable.getSpanStart(misspelledSpan);
                    int spanEnd = editable.getSpanEnd(misspelledSpan);
                    if (spanStart >= 0 && spanEnd > spanStart) {
                        String originalText = TextUtils.substring(editable, spanStart, spanEnd);
                        Intent intent = new Intent("com.android.settings.USER_DICTIONARY_INSERT");
                        intent.putExtra(SuggestionsPopupWindow.USER_DICTIONARY_EXTRA_WORD, originalText);
                        intent.putExtra(SuggestionsPopupWindow.USER_DICTIONARY_EXTRA_LOCALE, this.this$1.this$0.mTextView.getTextServicesLocale().toString());
                        intent.setFlags(intent.getFlags() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                        this.this$1.this$0.mTextView.getContext().startActivity(intent);
                        editable.removeSpan(this.this$1.mMisspelledSpanInfo.mSuggestionSpan);
                        Selection.setSelection(editable, spanEnd);
                        this.this$1.this$0.updateSpellCheckSpans(spanStart, spanEnd, Editor.DEBUG_UNDO);
                        this.this$1.hideWithCleanUp();
                    }
                }
            }
        }

        /* renamed from: android.widget.Editor.SuggestionsPopupWindow.2 */
        class AnonymousClass2 implements OnClickListener {
            final /* synthetic */ SuggestionsPopupWindow this$1;

            AnonymousClass2(SuggestionsPopupWindow this$1) {
                this.this$1 = this$1;
            }

            public void onClick(View v) {
                Editable editable = (Editable) this.this$1.this$0.mTextView.getText();
                int spanUnionStart = editable.getSpanStart(this.this$1.this$0.mSuggestionRangeSpan);
                int spanUnionEnd = editable.getSpanEnd(this.this$1.this$0.mSuggestionRangeSpan);
                if (spanUnionStart >= 0 && spanUnionEnd > spanUnionStart) {
                    if (spanUnionEnd < editable.length() && Character.isSpaceChar(editable.charAt(spanUnionEnd)) && (spanUnionStart == 0 || Character.isSpaceChar(editable.charAt(spanUnionStart + Editor.UNSET_X_VALUE)))) {
                        spanUnionEnd += Editor.TAP_STATE_FIRST_TAP;
                    }
                    this.this$1.this$0.mTextView.deleteText_internal(spanUnionStart, spanUnionEnd);
                }
                this.this$1.hideWithCleanUp();
            }
        }

        private class CustomPopupWindow extends PopupWindow {
            final /* synthetic */ SuggestionsPopupWindow this$1;

            /* synthetic */ CustomPopupWindow(SuggestionsPopupWindow this$1, CustomPopupWindow customPopupWindow) {
                this(this$1);
            }

            private CustomPopupWindow(SuggestionsPopupWindow this$1) {
                this.this$1 = this$1;
            }

            public void dismiss() {
                if (isShowing()) {
                    super.dismiss();
                    this.this$1.this$0.getPositionListener().removeSubscriber(this.this$1);
                    ((Spannable) this.this$1.this$0.mTextView.getText()).removeSpan(this.this$1.this$0.mSuggestionRangeSpan);
                    this.this$1.this$0.mTextView.setCursorVisible(this.this$1.mCursorWasVisibleBeforeSuggestions);
                    if (this.this$1.this$0.hasInsertionController() && !this.this$1.this$0.extractedTextModeWillBeStarted()) {
                        this.this$1.this$0.getInsertionController().show();
                    }
                }
            }
        }

        private class SuggestionAdapter extends BaseAdapter {
            private LayoutInflater mInflater;
            final /* synthetic */ SuggestionsPopupWindow this$1;

            /* synthetic */ SuggestionAdapter(SuggestionsPopupWindow this$1, SuggestionAdapter suggestionAdapter) {
                this(this$1);
            }

            private SuggestionAdapter(SuggestionsPopupWindow this$1) {
                this.this$1 = this$1;
                this.mInflater = (LayoutInflater) this.this$1.mContext.getSystemService("layout_inflater");
            }

            public int getCount() {
                return this.this$1.mNumberOfSuggestions;
            }

            public Object getItem(int position) {
                return this.this$1.mSuggestionInfos[position];
            }

            public long getItemId(int position) {
                return (long) position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) convertView;
                if (textView == null) {
                    textView = (TextView) this.mInflater.inflate(this.this$1.this$0.mTextView.mTextEditSuggestionItemLayout, parent, (boolean) Editor.DEBUG_UNDO);
                }
                textView.setText(this.this$1.mSuggestionInfos[position].mText);
                return textView;
            }
        }

        public /* bridge */ /* synthetic */ void hide() {
            super.hide();
        }

        public /* bridge */ /* synthetic */ boolean isShowing() {
            return super.isShowing();
        }

        public /* bridge */ /* synthetic */ void updatePosition(int parentPositionX, int parentPositionY, boolean parentPositionChanged, boolean parentScrolled) {
            super.updatePosition(parentPositionX, parentPositionY, parentPositionChanged, parentScrolled);
        }

        public SuggestionsPopupWindow(Editor this$0) {
            this.this$0 = this$0;
            super();
            this.mIsShowingUp = Editor.DEBUG_UNDO;
            this.mMisspelledSpanInfo = new SuggestionSpanInfo();
            this.mCursorWasVisibleBeforeSuggestions = this$0.mCursorVisible;
        }

        protected void setUp() {
            this.mContext = applyDefaultTheme(this.this$0.mTextView.getContext());
            this.mHighlightSpan = new TextAppearanceSpan(this.mContext, this.this$0.mTextView.mTextEditSuggestionHighlightStyle);
        }

        private Context applyDefaultTheme(Context originalContext) {
            int themeId;
            int[] iArr = new int[Editor.TAP_STATE_FIRST_TAP];
            iArr[Editor.TAP_STATE_INITIAL] = R.attr.isLightTheme;
            TypedArray a = originalContext.obtainStyledAttributes(iArr);
            if (a.getBoolean(Editor.TAP_STATE_INITIAL, true)) {
                themeId = R.style.ThemeOverlay_Material_Light;
            } else {
                themeId = R.style.ThemeOverlay_Material_Dark;
            }
            a.recycle();
            return new ContextThemeWrapper(originalContext, themeId);
        }

        protected void createPopupWindow() {
            this.mPopupWindow = new CustomPopupWindow();
            this.mPopupWindow.setInputMethodMode(Editor.TAP_STATE_DOUBLE_TAP);
            this.mPopupWindow.setBackgroundDrawable(new ColorDrawable(Editor.TAP_STATE_INITIAL));
            this.mPopupWindow.setFocusable(true);
            this.mPopupWindow.setClippingEnabled(Editor.DEBUG_UNDO);
        }

        protected void initContentView() {
            this.mContentView = (ViewGroup) ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(this.this$0.mTextView.mTextEditSuggestionContainerLayout, null);
            this.mContainerView = (LinearLayout) this.mContentView.findViewById(R.id.suggestionWindowContainer);
            MarginLayoutParams lp = (MarginLayoutParams) this.mContainerView.getLayoutParams();
            this.mContainerMarginWidth = lp.leftMargin + lp.rightMargin;
            this.mContainerMarginTop = lp.topMargin;
            this.mClippingLimitLeft = lp.leftMargin;
            this.mClippingLimitRight = lp.rightMargin;
            this.mSuggestionListView = (ListView) this.mContentView.findViewById(R.id.suggestionContainer);
            this.mSuggestionsAdapter = new SuggestionAdapter();
            this.mSuggestionListView.setAdapter(this.mSuggestionsAdapter);
            this.mSuggestionListView.setOnItemClickListener(this);
            this.mSuggestionInfos = new SuggestionInfo[MAX_NUMBER_SUGGESTIONS];
            for (int i = Editor.TAP_STATE_INITIAL; i < this.mSuggestionInfos.length; i += Editor.TAP_STATE_FIRST_TAP) {
                this.mSuggestionInfos[i] = new SuggestionInfo();
            }
            this.mAddToDictionaryButton = (TextView) this.mContentView.findViewById(R.id.addToDictionaryButton);
            this.mAddToDictionaryButton.setOnClickListener(new AnonymousClass1(this));
            this.mDeleteButton = (TextView) this.mContentView.findViewById(R.id.deleteButton);
            this.mDeleteButton.setOnClickListener(new AnonymousClass2(this));
        }

        public boolean isShowingUp() {
            return this.mIsShowingUp;
        }

        public void onParentLostFocus() {
            this.mIsShowingUp = Editor.DEBUG_UNDO;
        }

        public ViewGroup getContentViewForTesting() {
            return this.mContentView;
        }

        public void show() {
            if ((this.this$0.mTextView.getText() instanceof Editable) && !this.this$0.extractedTextModeWillBeStarted() && updateSuggestions()) {
                this.mCursorWasVisibleBeforeSuggestions = this.this$0.mCursorVisible;
                this.this$0.mTextView.setCursorVisible(Editor.DEBUG_UNDO);
                this.mIsShowingUp = true;
                super.show();
            }
        }

        protected void measureContent() {
            DisplayMetrics displayMetrics = this.this$0.mTextView.getResources().getDisplayMetrics();
            int horizontalMeasure = MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, RtlSpacingHelper.UNDEFINED);
            int verticalMeasure = MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, RtlSpacingHelper.UNDEFINED);
            int width = Editor.TAP_STATE_INITIAL;
            View view = null;
            for (int i = Editor.TAP_STATE_INITIAL; i < this.mNumberOfSuggestions; i += Editor.TAP_STATE_FIRST_TAP) {
                view = this.mSuggestionsAdapter.getView(i, view, this.mContentView);
                view.getLayoutParams().width = Editor.EXTRACT_NOTHING;
                view.measure(horizontalMeasure, verticalMeasure);
                width = Math.max(width, view.getMeasuredWidth());
            }
            if (this.mAddToDictionaryButton.getVisibility() != Editor.MENU_ITEM_ORDER_SHARE) {
                this.mAddToDictionaryButton.measure(horizontalMeasure, verticalMeasure);
                width = Math.max(width, this.mAddToDictionaryButton.getMeasuredWidth());
            }
            this.mDeleteButton.measure(horizontalMeasure, verticalMeasure);
            width = Math.max(width, this.mDeleteButton.getMeasuredWidth()) + ((this.mContainerView.getPaddingLeft() + this.mContainerView.getPaddingRight()) + this.mContainerMarginWidth);
            this.mContentView.measure(MeasureSpec.makeMeasureSpec(width, EditorInfo.IME_FLAG_NO_ENTER_ACTION), verticalMeasure);
            Drawable popupBackground = this.mPopupWindow.getBackground();
            if (popupBackground != null) {
                if (this.this$0.mTempRect == null) {
                    this.this$0.mTempRect = new Rect();
                }
                popupBackground.getPadding(this.this$0.mTempRect);
                width += this.this$0.mTempRect.left + this.this$0.mTempRect.right;
            }
            this.mPopupWindow.setWidth(width);
        }

        protected int getTextOffset() {
            return (this.this$0.mTextView.getSelectionStart() + this.this$0.mTextView.getSelectionStart()) / Editor.TAP_STATE_DOUBLE_TAP;
        }

        protected int getVerticalLocalPosition(int line) {
            return this.this$0.mTextView.getLayout().getLineBottom(line) - this.mContainerMarginTop;
        }

        protected int clipVertically(int positionY) {
            return Math.min(positionY, this.this$0.mTextView.getResources().getDisplayMetrics().heightPixels - this.mContentView.getMeasuredHeight());
        }

        private void hideWithCleanUp() {
            SuggestionInfo[] suggestionInfoArr = this.mSuggestionInfos;
            int length = suggestionInfoArr.length;
            for (int i = Editor.TAP_STATE_INITIAL; i < length; i += Editor.TAP_STATE_FIRST_TAP) {
                suggestionInfoArr[i].clear();
            }
            this.mMisspelledSpanInfo.clear();
            hide();
        }

        private boolean updateSuggestions() {
            Spannable spannable = (Spannable) this.this$0.mTextView.getText();
            this.mNumberOfSuggestions = this.this$0.mSuggestionHelper.getSuggestionInfo(this.mSuggestionInfos, this.mMisspelledSpanInfo);
            if (this.mNumberOfSuggestions == 0 && this.mMisspelledSpanInfo.mSuggestionSpan == null) {
                return Editor.DEBUG_UNDO;
            }
            int i;
            int underlineColor;
            int spanUnionStart = this.this$0.mTextView.getText().length();
            int spanUnionEnd = Editor.TAP_STATE_INITIAL;
            for (i = Editor.TAP_STATE_INITIAL; i < this.mNumberOfSuggestions; i += Editor.TAP_STATE_FIRST_TAP) {
                SuggestionSpanInfo spanInfo = this.mSuggestionInfos[i].mSuggestionSpanInfo;
                spanUnionStart = Math.min(spanUnionStart, spanInfo.mSpanStart);
                spanUnionEnd = Math.max(spanUnionEnd, spanInfo.mSpanEnd);
            }
            if (this.mMisspelledSpanInfo.mSuggestionSpan != null) {
                spanUnionStart = Math.min(spanUnionStart, this.mMisspelledSpanInfo.mSpanStart);
                spanUnionEnd = Math.max(spanUnionEnd, this.mMisspelledSpanInfo.mSpanEnd);
            }
            for (i = Editor.TAP_STATE_INITIAL; i < this.mNumberOfSuggestions; i += Editor.TAP_STATE_FIRST_TAP) {
                highlightTextDifferences(this.mSuggestionInfos[i], spanUnionStart, spanUnionEnd);
            }
            int addToDictionaryButtonVisibility = Editor.MENU_ITEM_ORDER_SHARE;
            if (this.mMisspelledSpanInfo.mSuggestionSpan != null && this.mMisspelledSpanInfo.mSpanStart >= 0 && this.mMisspelledSpanInfo.mSpanEnd > this.mMisspelledSpanInfo.mSpanStart) {
                addToDictionaryButtonVisibility = Editor.TAP_STATE_INITIAL;
            }
            this.mAddToDictionaryButton.setVisibility(addToDictionaryButtonVisibility);
            if (this.this$0.mSuggestionRangeSpan == null) {
                this.this$0.mSuggestionRangeSpan = new SuggestionRangeSpan();
            }
            if (this.mNumberOfSuggestions != 0) {
                underlineColor = this.mSuggestionInfos[Editor.TAP_STATE_INITIAL].mSuggestionSpanInfo.mSuggestionSpan.getUnderlineColor();
            } else {
                underlineColor = this.mMisspelledSpanInfo.mSuggestionSpan.getUnderlineColor();
            }
            if (underlineColor == 0) {
                this.this$0.mSuggestionRangeSpan.setBackgroundColor(this.this$0.mTextView.mHighlightColor);
            } else {
                this.this$0.mSuggestionRangeSpan.setBackgroundColor((AsyncService.CMD_ASYNC_SERVICE_ON_START_INTENT & underlineColor) + (((int) (((float) Color.alpha(underlineColor)) * 0.4f)) << 24));
            }
            try {
                spannable.setSpan(this.this$0.mSuggestionRangeSpan, spanUnionStart, spanUnionEnd, 33);
            } catch (IndexOutOfBoundsException e) {
                Log.d(Editor.UNDO_OWNER_TAG, "setSpan IndexOutOfBoundsException");
            }
            this.mSuggestionsAdapter.notifyDataSetChanged();
            return true;
        }

        private void highlightTextDifferences(SuggestionInfo suggestionInfo, int unionStart, int unionEnd) {
            Spannable text = (Spannable) this.this$0.mTextView.getText();
            int spanStart = suggestionInfo.mSuggestionSpanInfo.mSpanStart;
            int spanEnd = suggestionInfo.mSuggestionSpanInfo.mSpanEnd;
            suggestionInfo.mSuggestionStart = spanStart - unionStart;
            suggestionInfo.mSuggestionEnd = suggestionInfo.mSuggestionStart + suggestionInfo.mText.length();
            suggestionInfo.mText.setSpan(this.mHighlightSpan, Editor.TAP_STATE_INITIAL, suggestionInfo.mText.length(), 33);
            String textAsString = text.toString();
            suggestionInfo.mText.insert((int) Editor.TAP_STATE_INITIAL, textAsString.substring(unionStart, spanStart));
            suggestionInfo.mText.append(textAsString.substring(spanEnd, unionEnd));
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            this.this$0.replaceWithSuggestion(this.mSuggestionInfos[position]);
            hideWithCleanUp();
        }
    }

    private class TextActionModeCallback extends Callback2 {
        private int mHandleHeight;
        private final boolean mHasSelection;
        private final RectF mSelectionBounds;
        private final Path mSelectionPath;
        final /* synthetic */ Editor this$0;

        public TextActionModeCallback(Editor this$0, boolean hasSelection) {
            this.this$0 = this$0;
            this.mSelectionPath = new Path();
            this.mSelectionBounds = new RectF();
            this.mHasSelection = hasSelection;
            if (this.mHasSelection) {
                SelectionModifierCursorController selectionController = this$0.getSelectionController();
                if (selectionController.mStartHandle == null) {
                    selectionController.initDrawables();
                    selectionController.initHandles();
                    selectionController.hide();
                }
                this.mHandleHeight = Math.max(this$0.mSelectHandleLeft.getMinimumHeight(), this$0.mSelectHandleRight.getMinimumHeight());
                return;
            }
            InsertionPointCursorController insertionController = this$0.getInsertionController();
            if (insertionController != null) {
                insertionController.getHandle();
                this.mHandleHeight = this$0.mSelectHandleCenter.getMinimumHeight();
            }
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(null);
            mode.setSubtitle(null);
            mode.setTitleOptionalHint(true);
            populateMenuWithItems(menu);
            Callback customCallback = getCustomCallback();
            if (customCallback == null || customCallback.onCreateActionMode(mode, menu)) {
                if (this.this$0.mTextView.canProcessText()) {
                    this.this$0.mProcessTextIntentActionsHandler.onInitializeMenu(menu);
                }
                if (!menu.hasVisibleItems() && mode.getCustomView() == null) {
                    return Editor.DEBUG_UNDO;
                }
                if (this.mHasSelection && !this.this$0.mTextView.hasTransientState()) {
                    this.this$0.mTextView.setHasTransientState(true);
                }
                return true;
            }
            Selection.setSelection((Spannable) this.this$0.mTextView.getText(), this.this$0.mTextView.getSelectionEnd());
            return Editor.DEBUG_UNDO;
        }

        private Callback getCustomCallback() {
            if (this.mHasSelection) {
                return this.this$0.mCustomSelectionActionModeCallback;
            }
            return this.this$0.mCustomInsertionActionModeCallback;
        }

        private void populateMenuWithItems(Menu menu) {
            if (this.this$0.mTextView.canCut()) {
                menu.add((int) Editor.TAP_STATE_INITIAL, (int) R.id.cut, (int) Editor.MENU_ITEM_ORDER_CUT, (int) R.string.cut).setAlphabeticShortcut(StateProperty.TARGET_X).setShowAsAction(Editor.TAP_STATE_DOUBLE_TAP);
            }
            if (this.this$0.mTextView.canCopy()) {
                menu.add((int) Editor.TAP_STATE_INITIAL, (int) R.id.copy, (int) Editor.MENU_ITEM_ORDER_COPY, (int) R.string.copy).setAlphabeticShortcut('c').setShowAsAction(Editor.TAP_STATE_DOUBLE_TAP);
            }
            if (this.this$0.mTextView.canPaste()) {
                menu.add((int) Editor.TAP_STATE_INITIAL, (int) R.id.paste, (int) Editor.MENU_ITEM_ORDER_PASTE, (int) R.string.paste).setAlphabeticShortcut('v').setShowAsAction(Editor.TAP_STATE_DOUBLE_TAP);
            }
            if (this.this$0.mTextView.canShare()) {
                menu.add((int) Editor.TAP_STATE_INITIAL, (int) R.id.shareText, (int) Editor.MENU_ITEM_ORDER_SHARE, (int) R.string.share).setShowAsAction(Editor.TAP_STATE_FIRST_TAP);
            }
            updateSelectAllItem(menu);
            updateReplaceItem(menu);
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            updateSelectAllItem(menu);
            updateReplaceItem(menu);
            Callback customCallback = getCustomCallback();
            if (customCallback != null) {
                return customCallback.onPrepareActionMode(mode, menu);
            }
            return true;
        }

        private void updateSelectAllItem(Menu menu) {
            boolean canSelectAll = this.this$0.mTextView.canSelectAllText();
            boolean selectAllItemExists = menu.findItem(R.id.selectAll) != null ? true : Editor.DEBUG_UNDO;
            if (canSelectAll && !selectAllItemExists) {
                menu.add((int) Editor.TAP_STATE_INITIAL, (int) R.id.selectAll, (int) Editor.TAP_STATE_FIRST_TAP, (int) R.string.selectAll).setShowAsAction(Editor.TAP_STATE_FIRST_TAP);
            } else if (!canSelectAll && selectAllItemExists) {
                menu.removeItem(R.id.selectAll);
            }
        }

        private void updateReplaceItem(Menu menu) {
            boolean -wrap11 = this.this$0.mTextView.isSuggestionsEnabled() ? this.this$0.shouldOfferToShowSuggestions() : Editor.DEBUG_UNDO;
            boolean replaceItemExists = menu.findItem(R.id.replaceText) != null ? true : Editor.DEBUG_UNDO;
            if (-wrap11 && !replaceItemExists) {
                menu.add((int) Editor.TAP_STATE_INITIAL, (int) R.id.replaceText, (int) Editor.MENU_ITEM_ORDER_REPLACE, (int) R.string.replace).setShowAsAction(Editor.TAP_STATE_FIRST_TAP);
            } else if (!-wrap11 && replaceItemExists) {
                menu.removeItem(R.id.replaceText);
            }
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (this.this$0.mProcessTextIntentActionsHandler.performMenuItemAction(item)) {
                return true;
            }
            Callback customCallback = getCustomCallback();
            if (customCallback == null || !customCallback.onActionItemClicked(mode, item)) {
                return this.this$0.mTextView.onTextContextMenuItem(item.getItemId());
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            this.this$0.mTextActionMode = null;
            Callback customCallback = getCustomCallback();
            if (customCallback != null) {
                customCallback.onDestroyActionMode(mode);
            }
            if (!this.this$0.mPreserveSelection) {
                Selection.setSelection((Spannable) this.this$0.mTextView.getText(), this.this$0.mTextView.getSelectionEnd());
            }
            if (this.this$0.mSelectionModifierCursorController != null) {
                this.this$0.mSelectionModifierCursorController.hide();
            }
        }

        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            if (!view.equals(this.this$0.mTextView) || this.this$0.mTextView.getLayout() == null) {
                super.onGetContentRect(mode, view, outRect);
                return;
            }
            if (this.this$0.mTextView.getSelectionStart() != this.this$0.mTextView.getSelectionEnd()) {
                this.mSelectionPath.reset();
                this.this$0.mTextView.getLayout().getSelectionPath(this.this$0.mTextView.getSelectionStart(), this.this$0.mTextView.getSelectionEnd(), this.mSelectionPath);
                this.mSelectionPath.computeBounds(this.mSelectionBounds, true);
                RectF rectF = this.mSelectionBounds;
                rectF.bottom += (float) this.mHandleHeight;
            } else if (this.this$0.mCursorCount == Editor.TAP_STATE_DOUBLE_TAP) {
                Rect firstCursorBounds = this.this$0.mCursorDrawable[Editor.TAP_STATE_INITIAL].getBounds();
                Rect secondCursorBounds = this.this$0.mCursorDrawable[Editor.TAP_STATE_FIRST_TAP].getBounds();
                this.mSelectionBounds.set((float) Math.min(firstCursorBounds.left, secondCursorBounds.left), (float) Math.min(firstCursorBounds.top, secondCursorBounds.top), (float) Math.max(firstCursorBounds.right, secondCursorBounds.right), (float) (Math.max(firstCursorBounds.bottom, secondCursorBounds.bottom) + this.mHandleHeight));
            } else {
                Layout layout = this.this$0.mTextView.getLayout();
                int line = layout.getLineForOffset(this.this$0.mTextView.getSelectionStart());
                float primaryHorizontal = (float) this.this$0.clampHorizontalPosition(null, layout.getPrimaryHorizontal(this.this$0.mTextView.getSelectionStart()));
                this.mSelectionBounds.set(primaryHorizontal, (float) layout.getLineTop(line), primaryHorizontal, (float) (layout.getLineTop(line + Editor.TAP_STATE_FIRST_TAP) + this.mHandleHeight));
                this.this$0.adjustSelectionBounds(this.mSelectionBounds, line, layout, this.mHandleHeight);
            }
            int textHorizontalOffset = this.this$0.mTextView.viewportToContentHorizontalOffset();
            int textVerticalOffset = this.this$0.mTextView.viewportToContentVerticalOffset();
            outRect.set((int) Math.floor((double) (this.mSelectionBounds.left + ((float) textHorizontalOffset))), (int) Math.floor((double) (this.mSelectionBounds.top + ((float) textVerticalOffset))), (int) Math.ceil((double) (this.mSelectionBounds.right + ((float) textHorizontalOffset))), (int) Math.ceil((double) (this.mSelectionBounds.bottom + ((float) textVerticalOffset))));
        }
    }

    private static class TextRenderNode {
        boolean isDirty;
        RenderNode renderNode;

        public TextRenderNode(String name) {
            this.isDirty = true;
            this.renderNode = RenderNode.create(name, null);
        }

        boolean needsRecord() {
            return (this.isDirty || !this.renderNode.isValid()) ? true : Editor.DEBUG_UNDO;
        }
    }

    public static class UndoInputFilter implements InputFilter {
        private final Editor mEditor;
        private boolean mForceMerge;
        private boolean mHasComposition;
        private boolean mIsUserEdit;

        public UndoInputFilter(Editor editor) {
            this.mEditor = editor;
        }

        public void saveInstanceState(Parcel parcel) {
            int i;
            int i2 = Editor.TAP_STATE_FIRST_TAP;
            if (this.mIsUserEdit) {
                i = Editor.TAP_STATE_FIRST_TAP;
            } else {
                i = Editor.TAP_STATE_INITIAL;
            }
            parcel.writeInt(i);
            if (!this.mHasComposition) {
                i2 = Editor.TAP_STATE_INITIAL;
            }
            parcel.writeInt(i2);
        }

        public void restoreInstanceState(Parcel parcel) {
            boolean z;
            boolean z2 = true;
            if (parcel.readInt() != 0) {
                z = true;
            } else {
                z = Editor.DEBUG_UNDO;
            }
            this.mIsUserEdit = z;
            if (parcel.readInt() == 0) {
                z2 = Editor.DEBUG_UNDO;
            }
            this.mHasComposition = z2;
        }

        public void setForceMerge(boolean forceMerge) {
            this.mForceMerge = forceMerge;
        }

        public void beginBatchEdit() {
            this.mIsUserEdit = true;
        }

        public void endBatchEdit() {
            this.mIsUserEdit = Editor.DEBUG_UNDO;
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (!canUndoEdit(source, start, end, dest, dstart, dend) || handleCompositionEdit(source, start, end, dstart)) {
                return null;
            }
            handleKeyboardEdit(source, start, end, dest, dstart, dend);
            return null;
        }

        private boolean handleCompositionEdit(CharSequence source, int start, int end, int dstart) {
            if (isComposition(source)) {
                this.mHasComposition = true;
                return true;
            }
            boolean hadComposition = this.mHasComposition;
            this.mHasComposition = Editor.DEBUG_UNDO;
            if (!hadComposition) {
                return Editor.DEBUG_UNDO;
            }
            if (start == end) {
                return true;
            }
            recordEdit(new EditOperation(this.mEditor, "", dstart, TextUtils.substring(source, start, end)), this.mForceMerge);
            return true;
        }

        private void handleKeyboardEdit(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            boolean isInTextWatcher = !this.mForceMerge ? isInTextWatcher() : true;
            recordEdit(new EditOperation(this.mEditor, TextUtils.substring(dest, dstart, dend), dstart, TextUtils.substring(source, start, end)), isInTextWatcher);
        }

        private void recordEdit(EditOperation edit, boolean forceMerge) {
            UndoManager um = this.mEditor.mUndoManager;
            um.beginUpdate("Edit text");
            EditOperation lastEdit = (EditOperation) um.getLastOperation(EditOperation.class, this.mEditor.mUndoOwner, Editor.TAP_STATE_FIRST_TAP);
            if (lastEdit == null) {
                um.addOperation(edit, Editor.TAP_STATE_INITIAL);
            } else if (forceMerge) {
                lastEdit.forceMergeWith(edit);
            } else if (!this.mIsUserEdit) {
                um.commitState(this.mEditor.mUndoOwner);
                um.addOperation(edit, Editor.TAP_STATE_INITIAL);
            } else if (!lastEdit.mergeWith(edit)) {
                um.commitState(this.mEditor.mUndoOwner);
                um.addOperation(edit, Editor.TAP_STATE_INITIAL);
            }
            um.endUpdate();
        }

        private boolean canUndoEdit(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (!this.mEditor.mAllowUndo || this.mEditor.mUndoManager.isInUndo() || !Editor.isValidRange(source, start, end) || !Editor.isValidRange(dest, dstart, dend)) {
                return Editor.DEBUG_UNDO;
            }
            if (start == end && dstart == dend) {
                return Editor.DEBUG_UNDO;
            }
            return true;
        }

        private boolean isComposition(CharSequence source) {
            boolean z = Editor.DEBUG_UNDO;
            if (!(source instanceof Spannable)) {
                return Editor.DEBUG_UNDO;
            }
            Spannable text = (Spannable) source;
            if (BaseInputConnection.getComposingSpanStart(text) < BaseInputConnection.getComposingSpanEnd(text)) {
                z = true;
            }
            return z;
        }

        private boolean isInTextWatcher() {
            CharSequence text = this.mEditor.mTextView.getText();
            if (!(text instanceof SpannableStringBuilder) || ((SpannableStringBuilder) text).getTextWatcherDepth() <= 0) {
                return Editor.DEBUG_UNDO;
            }
            return true;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.Editor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.Editor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.Editor.<clinit>():void");
    }

    public Editor(TextView textView) {
        this.mUndoManager = new UndoManager();
        this.mUndoOwner = this.mUndoManager.getOwner(UNDO_OWNER_TAG, this);
        this.mUndoInputFilter = new UndoInputFilter(this);
        this.mAllowUndo = true;
        this.mInputType = TAP_STATE_INITIAL;
        this.mCursorVisible = true;
        this.mShowSoftInputOnFocus = true;
        this.mCursorDrawable = new Drawable[TAP_STATE_DOUBLE_TAP];
        this.mTapState = TAP_STATE_INITIAL;
        this.mLastTouchUpTime = 0;
        this.mCursorAnchorInfoNotifier = new CursorAnchorInfoNotifier();
        this.mShowFloatingToolbar = new Runnable() {
            public void run() {
                if (Editor.this.mTextActionMode != null) {
                    Editor.this.mTextActionMode.hide(0);
                }
            }
        };
        this.mIsInsertionActionModeStartPending = DEBUG_UNDO;
        this.mSuggestionHelper = new SuggestionHelper();
        this.mOnContextMenuItemClickListener = new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (Editor.this.mProcessTextIntentActionsHandler.performMenuItemAction(item)) {
                    return true;
                }
                return Editor.this.mTextView.onTextContextMenuItem(item.getItemId());
            }
        };
        this.mTextView = textView;
        this.mTextView.setFilters(this.mTextView.getFilters());
        this.mProcessTextIntentActionsHandler = new ProcessTextIntentActionsHandler();
    }

    ParcelableParcel saveInstanceState() {
        ParcelableParcel state = new ParcelableParcel(getClass().getClassLoader());
        Parcel parcel = state.getParcel();
        this.mUndoManager.saveInstanceState(parcel);
        this.mUndoInputFilter.saveInstanceState(parcel);
        return state;
    }

    void restoreInstanceState(ParcelableParcel state) {
        Parcel parcel = state.getParcel();
        this.mUndoManager.restoreInstanceState(parcel, state.getClassLoader());
        this.mUndoInputFilter.restoreInstanceState(parcel);
        this.mUndoOwner = this.mUndoManager.getOwner(UNDO_OWNER_TAG, this);
    }

    void forgetUndoRedo() {
        UndoOwner[] owners = new UndoOwner[TAP_STATE_FIRST_TAP];
        owners[TAP_STATE_INITIAL] = this.mUndoOwner;
        this.mUndoManager.forgetUndos(owners, UNSET_X_VALUE);
        this.mUndoManager.forgetRedos(owners, UNSET_X_VALUE);
    }

    boolean canUndo() {
        UndoOwner[] owners = new UndoOwner[TAP_STATE_FIRST_TAP];
        owners[TAP_STATE_INITIAL] = this.mUndoOwner;
        if (!this.mAllowUndo || this.mUndoManager.countUndos(owners) <= 0) {
            return DEBUG_UNDO;
        }
        return true;
    }

    boolean canRedo() {
        UndoOwner[] owners = new UndoOwner[TAP_STATE_FIRST_TAP];
        owners[TAP_STATE_INITIAL] = this.mUndoOwner;
        if (!this.mAllowUndo || this.mUndoManager.countRedos(owners) <= 0) {
            return DEBUG_UNDO;
        }
        return true;
    }

    void undo() {
        if (this.mAllowUndo) {
            UndoOwner[] owners = new UndoOwner[TAP_STATE_FIRST_TAP];
            owners[TAP_STATE_INITIAL] = this.mUndoOwner;
            this.mUndoManager.undo(owners, TAP_STATE_FIRST_TAP);
        }
    }

    void redo() {
        if (this.mAllowUndo) {
            UndoOwner[] owners = new UndoOwner[TAP_STATE_FIRST_TAP];
            owners[TAP_STATE_INITIAL] = this.mUndoOwner;
            this.mUndoManager.redo(owners, TAP_STATE_FIRST_TAP);
        }
    }

    void replace() {
        if (this.mSuggestionsPopupWindow == null) {
            this.mSuggestionsPopupWindow = new SuggestionsPopupWindow(this);
        }
        hideCursorAndSpanControllers();
        this.mSuggestionsPopupWindow.show();
        Selection.setSelection((Spannable) this.mTextView.getText(), (this.mTextView.getSelectionStart() + this.mTextView.getSelectionEnd()) / TAP_STATE_DOUBLE_TAP);
    }

    void onAttachedToWindow() {
        if (this.mShowErrorAfterAttach) {
            showError();
            this.mShowErrorAfterAttach = DEBUG_UNDO;
        }
        ViewTreeObserver observer = this.mTextView.getViewTreeObserver();
        if (this.mInsertionPointCursorController != null) {
            observer.addOnTouchModeChangeListener(this.mInsertionPointCursorController);
        }
        if (this.mSelectionModifierCursorController != null) {
            this.mSelectionModifierCursorController.resetTouchOffsets();
            observer.addOnTouchModeChangeListener(this.mSelectionModifierCursorController);
        }
        updateSpellCheckSpans(TAP_STATE_INITIAL, this.mTextView.getText().length(), true);
        if (this.mTextView.hasSelection()) {
            refreshTextActionMode();
        }
        getPositionListener().addSubscriber(this.mCursorAnchorInfoNotifier, true);
        resumeBlink();
    }

    void onDetachedFromWindow() {
        getPositionListener().removeSubscriber(this.mCursorAnchorInfoNotifier);
        if (this.mError != null) {
            hideError();
        }
        suspendBlink();
        if (this.mInsertionPointCursorController != null) {
            this.mInsertionPointCursorController.onDetached();
        }
        if (this.mSelectionModifierCursorController != null) {
            this.mSelectionModifierCursorController.onDetached();
        }
        if (this.mShowSuggestionRunnable != null) {
            this.mTextView.removeCallbacks(this.mShowSuggestionRunnable);
        }
        if (this.mInsertionActionModeRunnable != null) {
            this.mTextView.removeCallbacks(this.mInsertionActionModeRunnable);
        }
        this.mTextView.removeCallbacks(this.mShowFloatingToolbar);
        discardTextDisplayLists();
        if (this.mSpellChecker != null) {
            this.mSpellChecker.closeSession();
            this.mSpellChecker = null;
        }
        hideCursorAndSpanControllers();
        stopTextActionModeWithPreservingSelection();
    }

    private void discardTextDisplayLists() {
        if (this.mTextRenderNodes != null) {
            for (int i = TAP_STATE_INITIAL; i < this.mTextRenderNodes.length; i += TAP_STATE_FIRST_TAP) {
                RenderNode displayList;
                if (this.mTextRenderNodes[i] != null) {
                    displayList = this.mTextRenderNodes[i].renderNode;
                } else {
                    displayList = null;
                }
                if (displayList != null && displayList.isValid()) {
                    displayList.discardDisplayList();
                }
            }
        }
    }

    private void showError() {
        if (this.mTextView.getWindowToken() == null) {
            this.mShowErrorAfterAttach = true;
            return;
        }
        if (this.mErrorPopup == null) {
            TextView err = (TextView) LayoutInflater.from(this.mTextView.getContext()).inflate((int) R.layout.textview_hint, null);
            float scale = this.mTextView.getResources().getDisplayMetrics().density;
            this.mErrorPopup = new ErrorPopup(err, (int) ((200.0f * scale) + LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS), (int) ((50.0f * scale) + LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS));
            this.mErrorPopup.setFocusable(DEBUG_UNDO);
            this.mErrorPopup.setInputMethodMode(TAP_STATE_FIRST_TAP);
        }
        TextView tv = (TextView) this.mErrorPopup.getContentView();
        chooseSize(this.mErrorPopup, this.mError, tv);
        tv.setText(this.mError);
        this.mErrorPopup.showAsDropDown(this.mTextView, getErrorX(), getErrorY());
        this.mErrorPopup.fixDirection(this.mErrorPopup.isAboveAnchor());
    }

    public void setError(CharSequence error, Drawable icon) {
        this.mError = TextUtils.stringOrSpannedString(error);
        this.mErrorWasChanged = true;
        if (this.mError == null) {
            setErrorIcon(null);
            if (this.mErrorPopup != null) {
                if (this.mErrorPopup.isShowing()) {
                    this.mErrorPopup.dismiss();
                }
                this.mErrorPopup = null;
            }
            this.mShowErrorAfterAttach = DEBUG_UNDO;
            return;
        }
        setErrorIcon(icon);
        if (this.mTextView.isFocused()) {
            showError();
        }
    }

    private void setErrorIcon(Drawable icon) {
        Drawables dr = this.mTextView.mDrawables;
        if (dr == null) {
            TextView textView = this.mTextView;
            dr = new Drawables(this.mTextView.getContext());
            textView.mDrawables = dr;
        }
        dr.setErrorDrawable(icon, this.mTextView);
        this.mTextView.resetResolvedDrawables();
        this.mTextView.invalidate();
        this.mTextView.requestLayout();
    }

    private void hideError() {
        if (this.mErrorPopup != null && this.mErrorPopup.isShowing()) {
            this.mErrorPopup.dismiss();
        }
        this.mShowErrorAfterAttach = DEBUG_UNDO;
    }

    private int getErrorX() {
        int i = TAP_STATE_INITIAL;
        float scale = this.mTextView.getResources().getDisplayMetrics().density;
        Drawables dr = this.mTextView.mDrawables;
        switch (this.mTextView.getLayoutDirection()) {
            case TAP_STATE_FIRST_TAP /*1*/:
                if (dr != null) {
                    i = dr.mDrawableSizeLeft;
                }
                return this.mTextView.getPaddingLeft() + ((i / TAP_STATE_DOUBLE_TAP) - ((int) ((25.0f * scale) + LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS)));
            default:
                if (dr != null) {
                    i = dr.mDrawableSizeRight;
                }
                return ((this.mTextView.getWidth() - this.mErrorPopup.getWidth()) - this.mTextView.getPaddingRight()) + (((-i) / TAP_STATE_DOUBLE_TAP) + ((int) ((25.0f * scale) + LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS)));
        }
    }

    private int getErrorY() {
        int height;
        int compoundPaddingTop = this.mTextView.getCompoundPaddingTop();
        int vspace = ((this.mTextView.getBottom() - this.mTextView.getTop()) - this.mTextView.getCompoundPaddingBottom()) - compoundPaddingTop;
        Drawables dr = this.mTextView.mDrawables;
        switch (this.mTextView.getLayoutDirection()) {
            case TAP_STATE_FIRST_TAP /*1*/:
                if (dr == null) {
                    height = TAP_STATE_INITIAL;
                    break;
                }
                height = dr.mDrawableHeightLeft;
                break;
            default:
                if (dr == null) {
                    height = TAP_STATE_INITIAL;
                    break;
                }
                height = dr.mDrawableHeightRight;
                break;
        }
        return (((compoundPaddingTop + ((vspace - height) / TAP_STATE_DOUBLE_TAP)) + height) - this.mTextView.getHeight()) - ((int) ((2.0f * this.mTextView.getResources().getDisplayMetrics().density) + LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS));
    }

    void createInputContentTypeIfNeeded() {
        if (this.mInputContentType == null) {
            this.mInputContentType = new InputContentType();
        }
    }

    void createInputMethodStateIfNeeded() {
        if (this.mInputMethodState == null) {
            this.mInputMethodState = new InputMethodState();
        }
    }

    boolean isCursorVisible() {
        return this.mCursorVisible ? this.mTextView.isTextEditable() : DEBUG_UNDO;
    }

    void prepareCursorControllers() {
        boolean enabled;
        boolean isCursorVisible;
        boolean z = DEBUG_UNDO;
        boolean windowSupportsHandles = DEBUG_UNDO;
        ViewGroup.LayoutParams params = this.mTextView.getRootView().getLayoutParams();
        if (params instanceof LayoutParams) {
            LayoutParams windowParams = (LayoutParams) params;
            windowSupportsHandles = windowParams.type >= RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED ? windowParams.type > LayoutParams.LAST_SUB_WINDOW ? true : DEBUG_UNDO : true;
        }
        if (!windowSupportsHandles || this.mTextView.getLayout() == null) {
            enabled = DEBUG_UNDO;
        } else {
            enabled = true;
        }
        if (enabled) {
            isCursorVisible = isCursorVisible();
        } else {
            isCursorVisible = DEBUG_UNDO;
        }
        this.mInsertionControllerEnabled = isCursorVisible;
        if (enabled) {
            z = this.mTextView.textCanBeSelected();
        }
        this.mSelectionControllerEnabled = z;
        if (!this.mInsertionControllerEnabled) {
            hideInsertionPointCursorController();
            if (this.mInsertionPointCursorController != null) {
                this.mInsertionPointCursorController.onDetached();
                this.mInsertionPointCursorController = null;
            }
        }
        if (!this.mSelectionControllerEnabled) {
            stopTextActionMode();
            if (this.mSelectionModifierCursorController != null) {
                this.mSelectionModifierCursorController.onDetached();
                this.mSelectionModifierCursorController = null;
            }
        }
    }

    void hideInsertionPointCursorController() {
        if (this.mInsertionPointCursorController != null) {
            this.mInsertionPointCursorController.hide();
        }
    }

    void hideCursorAndSpanControllers() {
        hideCursorControllers();
        hideSpanControllers();
    }

    private void hideSpanControllers() {
        if (this.mSpanController != null) {
            this.mSpanController.hide();
        }
    }

    private void hideCursorControllers() {
        if (this.mSuggestionsPopupWindow != null && (this.mTextView.isInExtractedMode() || !this.mSuggestionsPopupWindow.isShowingUp())) {
            this.mSuggestionsPopupWindow.hide();
        }
        hideInsertionPointCursorController();
    }

    private void updateSpellCheckSpans(int start, int end, boolean createSpellChecker) {
        this.mTextView.removeAdjacentSuggestionSpans(start);
        this.mTextView.removeAdjacentSuggestionSpans(end);
        if (this.mTextView.isTextEditable() && this.mTextView.isSuggestionsEnabled() && !this.mTextView.isInExtractedMode()) {
            if (this.mSpellChecker == null && createSpellChecker) {
                this.mSpellChecker = new SpellChecker(this.mTextView);
            }
            if (this.mSpellChecker != null) {
                this.mSpellChecker.spellCheck(start, end);
            }
        }
    }

    void onScreenStateChanged(int screenState) {
        switch (screenState) {
            case TAP_STATE_INITIAL /*0*/:
                suspendBlink();
            case TAP_STATE_FIRST_TAP /*1*/:
                resumeBlink();
            default:
        }
    }

    private void suspendBlink() {
        if (this.mBlink != null) {
            this.mBlink.cancel();
        }
    }

    private void resumeBlink() {
        if (this.mBlink != null) {
            this.mBlink.uncancel();
            makeBlink();
        }
    }

    void adjustInputType(boolean password, boolean passwordInputType, boolean webPasswordInputType, boolean numberPasswordInputType) {
        if ((this.mInputType & 15) == TAP_STATE_FIRST_TAP) {
            if (password || passwordInputType) {
                this.mInputType = (this.mInputType & -4081) | LogPower.START_CHG_ROTATION;
            }
            if (webPasswordInputType) {
                this.mInputType = (this.mInputType & -4081) | MetricsEvent.OVERVIEW_ACTIVITY;
            }
        } else if ((this.mInputType & 15) == TAP_STATE_DOUBLE_TAP && numberPasswordInputType) {
            this.mInputType = (this.mInputType & -4081) | 16;
        }
    }

    private void chooseSize(PopupWindow pop, CharSequence text, TextView tv) {
        int wid = tv.getPaddingLeft() + tv.getPaddingRight();
        int ht = tv.getPaddingTop() + tv.getPaddingBottom();
        CharSequence charSequence = text;
        Layout l = new StaticLayout(charSequence, tv.getPaint(), this.mTextView.getResources().getDimensionPixelSize(R.dimen.textview_error_popup_default_width), Alignment.ALIGN_NORMAL, LayoutParams.BRIGHTNESS_OVERRIDE_FULL, 0.0f, true);
        float max = 0.0f;
        for (int i = TAP_STATE_INITIAL; i < l.getLineCount(); i += TAP_STATE_FIRST_TAP) {
            max = Math.max(max, l.getLineWidth(i));
        }
        pop.setWidth(((int) Math.ceil((double) max)) + wid);
        pop.setHeight(l.getHeight() + ht);
    }

    void setFrame() {
        if (this.mErrorPopup != null) {
            chooseSize(this.mErrorPopup, this.mError, (TextView) this.mErrorPopup.getContentView());
            this.mErrorPopup.update(this.mTextView, getErrorX(), getErrorY(), this.mErrorPopup.getWidth(), this.mErrorPopup.getHeight());
        }
    }

    private int getWordStart(int offset) {
        int retOffset;
        if (getWordIteratorWithText().isOnPunctuation(getWordIteratorWithText().prevBoundary(offset))) {
            retOffset = getWordIteratorWithText().getPunctuationBeginning(offset);
        } else {
            retOffset = getWordIteratorWithText().getPrevWordBeginningOnTwoWordsBoundary(offset);
        }
        if (retOffset == UNSET_X_VALUE) {
            return offset;
        }
        return retOffset;
    }

    private int getWordEnd(int offset) {
        int retOffset;
        if (getWordIteratorWithText().isAfterPunctuation(getWordIteratorWithText().nextBoundary(offset))) {
            retOffset = getWordIteratorWithText().getPunctuationEnd(offset);
        } else {
            retOffset = getWordIteratorWithText().getNextWordEndOnTwoWordBoundary(offset);
        }
        if (retOffset == UNSET_X_VALUE) {
            return offset;
        }
        return retOffset;
    }

    private boolean needsToSelectAllToSelectWordOrParagraph() {
        if (this.mTextView.hasPasswordTransformationMethod()) {
            return true;
        }
        int inputType = this.mTextView.getInputType();
        int klass = inputType & 15;
        int variation = inputType & InputType.TYPE_MASK_VARIATION;
        if (klass == TAP_STATE_DOUBLE_TAP || klass == TAP_STATE_TRIPLE_CLICK || klass == MENU_ITEM_ORDER_CUT || variation == 16 || variation == 32 || variation == MetricsEvent.VOLUME_DIALOG_DETAILS || variation == LogPower.SCREEN_SHOT_END) {
            return true;
        }
        return DEBUG_UNDO;
    }

    private boolean selectCurrentWord() {
        if (!this.mTextView.canSelectText()) {
            return DEBUG_UNDO;
        }
        if (needsToSelectAllToSelectWordOrParagraph()) {
            return this.mTextView.selectAllText();
        }
        long lastTouchOffsets = getLastTouchOffsets();
        int minOffset = TextUtils.unpackRangeStartFromLong(lastTouchOffsets);
        int maxOffset = TextUtils.unpackRangeEndFromLong(lastTouchOffsets);
        if (minOffset < 0 || minOffset > this.mTextView.getText().length()) {
            return DEBUG_UNDO;
        }
        if (maxOffset < 0 || maxOffset > this.mTextView.getText().length()) {
            return DEBUG_UNDO;
        }
        int selectionStart;
        int selectionEnd;
        boolean z;
        URLSpan[] urlSpans = (URLSpan[]) ((Spanned) this.mTextView.getText()).getSpans(minOffset, maxOffset, URLSpan.class);
        ImageSpan[] imageSpans = (ImageSpan[]) ((Spanned) this.mTextView.getText()).getSpans(minOffset, maxOffset, ImageSpan.class);
        if (urlSpans.length >= TAP_STATE_FIRST_TAP) {
            URLSpan urlSpan = urlSpans[TAP_STATE_INITIAL];
            selectionStart = ((Spanned) this.mTextView.getText()).getSpanStart(urlSpan);
            selectionEnd = ((Spanned) this.mTextView.getText()).getSpanEnd(urlSpan);
        } else if (imageSpans.length >= TAP_STATE_FIRST_TAP) {
            ImageSpan imageSpan = imageSpans[TAP_STATE_INITIAL];
            selectionStart = ((Spanned) this.mTextView.getText()).getSpanStart(imageSpan);
            selectionEnd = ((Spanned) this.mTextView.getText()).getSpanEnd(imageSpan);
        } else {
            WordIterator wordIterator = getWordIterator();
            wordIterator.setCharSequence(this.mTextView.getText(), minOffset, maxOffset);
            selectionStart = wordIterator.getBeginning(minOffset);
            selectionEnd = wordIterator.getEnd(maxOffset);
            if (!(selectionStart == UNSET_X_VALUE || selectionEnd == UNSET_X_VALUE)) {
                if (selectionStart == selectionEnd) {
                }
            }
            long range = getCharClusterRange(minOffset);
            selectionStart = TextUtils.unpackRangeStartFromLong(range);
            selectionEnd = TextUtils.unpackRangeEndFromLong(range);
        }
        Selection.setSelection((Spannable) this.mTextView.getText(), selectionStart, selectionEnd);
        if (selectionEnd > selectionStart) {
            z = true;
        } else {
            z = DEBUG_UNDO;
        }
        return z;
    }

    private boolean selectCurrentParagraph() {
        if (!this.mTextView.canSelectText()) {
            return DEBUG_UNDO;
        }
        if (needsToSelectAllToSelectWordOrParagraph()) {
            return this.mTextView.selectAllText();
        }
        long lastTouchOffsets = getLastTouchOffsets();
        long paragraphsRange = getParagraphsRange(TextUtils.unpackRangeStartFromLong(lastTouchOffsets), TextUtils.unpackRangeEndFromLong(lastTouchOffsets));
        int start = TextUtils.unpackRangeStartFromLong(paragraphsRange);
        int end = TextUtils.unpackRangeEndFromLong(paragraphsRange);
        if (start >= end) {
            return DEBUG_UNDO;
        }
        Selection.setSelection((Spannable) this.mTextView.getText(), start, end);
        return true;
    }

    private long getParagraphsRange(int startOffset, int endOffset) {
        Layout layout = this.mTextView.getLayout();
        if (layout == null) {
            return TextUtils.packRangeInLong(UNSET_X_VALUE, UNSET_X_VALUE);
        }
        CharSequence text = this.mTextView.getText();
        int minLine = layout.getLineForOffset(startOffset);
        while (minLine > 0 && text.charAt(layout.getLineEnd(minLine + UNSET_X_VALUE) + UNSET_X_VALUE) != '\n') {
            minLine += UNSET_X_VALUE;
        }
        int maxLine = layout.getLineForOffset(endOffset);
        while (maxLine < layout.getLineCount() + UNSET_X_VALUE && text.charAt(layout.getLineEnd(maxLine) + UNSET_X_VALUE) != '\n') {
            maxLine += TAP_STATE_FIRST_TAP;
        }
        return TextUtils.packRangeInLong(layout.getLineStart(minLine), layout.getLineEnd(maxLine));
    }

    void onLocaleChanged() {
        this.mWordIterator = null;
        this.mWordIteratorWithText = null;
    }

    public WordIterator getWordIterator() {
        if (this.mWordIterator == null) {
            this.mWordIterator = new WordIterator(this.mTextView.getTextServicesLocale());
        }
        return this.mWordIterator;
    }

    private WordIterator getWordIteratorWithText() {
        if (this.mWordIteratorWithText == null) {
            this.mWordIteratorWithText = new WordIterator(this.mTextView.getTextServicesLocale());
            this.mUpdateWordIteratorText = true;
        }
        if (this.mUpdateWordIteratorText) {
            CharSequence text = this.mTextView.getText();
            this.mWordIteratorWithText.setCharSequence(text, TAP_STATE_INITIAL, text.length());
            this.mUpdateWordIteratorText = DEBUG_UNDO;
        }
        return this.mWordIteratorWithText;
    }

    private int getNextCursorOffset(int offset, boolean findAfterGivenOffset) {
        Layout layout = this.mTextView.getLayout();
        if (layout == null) {
            return offset;
        }
        return findAfterGivenOffset == layout.isRtlCharAt(offset) ? layout.getOffsetToLeftOf(offset) : layout.getOffsetToRightOf(offset);
    }

    private long getCharClusterRange(int offset) {
        if (offset < this.mTextView.getText().length()) {
            int clusterEndOffset = getNextCursorOffset(offset, true);
            return TextUtils.packRangeInLong(getNextCursorOffset(clusterEndOffset, DEBUG_UNDO), clusterEndOffset);
        } else if (offset + UNSET_X_VALUE < 0) {
            return TextUtils.packRangeInLong(offset, offset);
        } else {
            int clusterStartOffset = getNextCursorOffset(offset, DEBUG_UNDO);
            return TextUtils.packRangeInLong(clusterStartOffset, getNextCursorOffset(clusterStartOffset, true));
        }
    }

    private boolean touchPositionIsInSelection() {
        int selectionStart = this.mTextView.getSelectionStart();
        int selectionEnd = this.mTextView.getSelectionEnd();
        if (selectionStart == selectionEnd) {
            return DEBUG_UNDO;
        }
        if (selectionStart > selectionEnd) {
            int tmp = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = tmp;
            Selection.setSelection((Spannable) this.mTextView.getText(), selectionStart, tmp);
        }
        SelectionModifierCursorController selectionController = getSelectionController();
        boolean z = (selectionController.getMinTouchOffset() < selectionStart || selectionController.getMaxTouchOffset() >= selectionEnd) ? DEBUG_UNDO : true;
        return z;
    }

    private PositionListener getPositionListener() {
        if (this.mPositionListener == null) {
            this.mPositionListener = new PositionListener();
        }
        return this.mPositionListener;
    }

    private boolean isPositionVisible(float positionX, float positionY) {
        synchronized (TEMP_POSITION) {
            float[] position = TEMP_POSITION;
            position[TAP_STATE_INITIAL] = positionX;
            position[TAP_STATE_FIRST_TAP] = positionY;
            View view = this.mTextView;
            while (view != null) {
                if (view != this.mTextView) {
                    position[TAP_STATE_INITIAL] = position[TAP_STATE_INITIAL] - ((float) view.getScrollX());
                    position[TAP_STATE_FIRST_TAP] = position[TAP_STATE_FIRST_TAP] - ((float) view.getScrollY());
                }
                if (position[TAP_STATE_INITIAL] >= 0.0f && position[TAP_STATE_FIRST_TAP] >= 0.0f) {
                    if (position[TAP_STATE_INITIAL] <= ((float) view.getWidth()) && position[TAP_STATE_FIRST_TAP] <= ((float) view.getHeight())) {
                        if (!view.getMatrix().isIdentity()) {
                            view.getMatrix().mapPoints(position);
                        }
                        position[TAP_STATE_INITIAL] = position[TAP_STATE_INITIAL] + ((float) view.getLeft());
                        position[TAP_STATE_FIRST_TAP] = position[TAP_STATE_FIRST_TAP] + ((float) view.getTop());
                        ViewParent parent = view.getParent();
                        if (parent instanceof View) {
                            view = (View) parent;
                        } else {
                            view = null;
                        }
                    }
                }
                return DEBUG_UNDO;
            }
            return true;
        }
    }

    private boolean isOffsetVisible(int offset) {
        Layout layout = this.mTextView.getLayout();
        if (layout == null) {
            return DEBUG_UNDO;
        }
        return isPositionVisible((float) (this.mTextView.viewportToContentHorizontalOffset() + ((int) layout.getPrimaryHorizontal(offset))), (float) (this.mTextView.viewportToContentVerticalOffset() + layout.getLineBottom(layout.getLineForOffset(offset))));
    }

    private boolean isPositionOnText(float x, float y) {
        Layout layout = this.mTextView.getLayout();
        if (layout == null) {
            return DEBUG_UNDO;
        }
        int line = this.mTextView.getLineAtCoordinate(y);
        x = this.mTextView.convertToLocalHorizontalCoordinate(x);
        if (x >= layout.getLineLeft(line) && x <= layout.getLineRight(line)) {
            return true;
        }
        return DEBUG_UNDO;
    }

    private void startDragAndDrop() {
        if (!this.mTextView.isInExtractedMode()) {
            int start = this.mTextView.getSelectionStart();
            int end = this.mTextView.getSelectionEnd();
            this.mTextView.startDragAndDrop(ClipData.newPlainText(null, this.mTextView.getTransformedText(start, end)), getTextThumbnailBuilder(start, end), new DragLocalState(this.mTextView, start, end), GL10.GL_DEPTH_BUFFER_BIT);
            stopTextActionMode();
            if (hasSelectionController()) {
                getSelectionController().resetTouchOffsets();
            }
        }
    }

    public boolean performLongClick(boolean handled) {
        if (!(handled || isPositionOnText(this.mLastDownPositionX, this.mLastDownPositionY) || !this.mInsertionControllerEnabled)) {
            Selection.setSelection((Spannable) this.mTextView.getText(), this.mTextView.getOffsetForPosition(this.mLastDownPositionX, this.mLastDownPositionY));
            getInsertionController().show();
            this.mIsInsertionActionModeStartPending = true;
            handled = true;
        }
        if (!(handled || this.mTextActionMode == null)) {
            if (touchPositionIsInSelection()) {
                startDragAndDrop();
            } else {
                stopTextActionMode();
                selectCurrentWordAndStartDrag();
            }
            handled = true;
        }
        if (handled) {
            return handled;
        }
        return selectCurrentWordAndStartDrag();
    }

    private long getLastTouchOffsets() {
        SelectionModifierCursorController selectionController = getSelectionController();
        return TextUtils.packRangeInLong(selectionController.getMinTouchOffset(), selectionController.getMaxTouchOffset());
    }

    void onFocusChanged(boolean focused, int direction) {
        this.mShowCursor = SystemClock.uptimeMillis();
        ensureEndedBatchEdit();
        if (focused) {
            int selStart = this.mTextView.getSelectionStart();
            int selEnd = this.mTextView.getSelectionEnd();
            boolean isFocusHighlighted = (this.mSelectAllOnFocus && selStart == 0) ? selEnd == this.mTextView.getText().length() ? true : DEBUG_UNDO : DEBUG_UNDO;
            boolean z = (this.mFrozenWithFocus && this.mTextView.hasSelection()) ? isFocusHighlighted ? DEBUG_UNDO : true : DEBUG_UNDO;
            this.mCreatedWithASelection = z;
            if (!this.mFrozenWithFocus || selStart < 0 || selEnd < 0) {
                int lastTapPosition = getLastTapPosition();
                if (lastTapPosition >= 0) {
                    Selection.setSelection((Spannable) this.mTextView.getText(), lastTapPosition);
                }
                MovementMethod mMovement = this.mTextView.getMovementMethod();
                if (mMovement != null) {
                    mMovement.onTakeFocus(this.mTextView, (Spannable) this.mTextView.getText(), direction);
                }
                if ((this.mTextView.isInExtractedMode() || this.mSelectionMoved) && selStart >= 0 && selEnd >= 0) {
                    Selection.setSelection((Spannable) this.mTextView.getText(), selStart, selEnd);
                }
                if (this.mSelectAllOnFocus) {
                    this.mTextView.selectAllText();
                }
                this.mTouchFocusSelected = true;
            }
            this.mFrozenWithFocus = DEBUG_UNDO;
            this.mSelectionMoved = DEBUG_UNDO;
            if (this.mError != null) {
                showError();
            }
            makeBlink();
            return;
        }
        if (this.mError != null) {
            hideError();
        }
        this.mTextView.onEndBatchEdit();
        if (this.mTextView.isInExtractedMode()) {
            hideCursorAndSpanControllers();
            stopTextActionModeWithPreservingSelection();
        } else {
            hideCursorAndSpanControllers();
            if (this.mTextView.isTemporarilyDetached()) {
                stopTextActionModeWithPreservingSelection();
            } else {
                stopTextActionMode();
            }
            downgradeEasyCorrectionSpans();
        }
        if (this.mSelectionModifierCursorController != null) {
            this.mSelectionModifierCursorController.resetTouchOffsets();
        }
    }

    private void downgradeEasyCorrectionSpans() {
        CharSequence text = this.mTextView.getText();
        if (text instanceof Spannable) {
            Spannable spannable = (Spannable) text;
            SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) spannable.getSpans(TAP_STATE_INITIAL, spannable.length(), SuggestionSpan.class);
            for (int i = TAP_STATE_INITIAL; i < suggestionSpans.length; i += TAP_STATE_FIRST_TAP) {
                int flags = suggestionSpans[i].getFlags();
                if ((flags & TAP_STATE_FIRST_TAP) != 0 && (flags & TAP_STATE_DOUBLE_TAP) == 0) {
                    suggestionSpans[i].setFlags(flags & EXTRACT_NOTHING);
                }
            }
        }
    }

    void sendOnTextChanged(int start, int after) {
        updateSpellCheckSpans(start, start + after, DEBUG_UNDO);
        this.mUpdateWordIteratorText = true;
        hideCursorControllers();
        if (this.mSelectionModifierCursorController != null) {
            this.mSelectionModifierCursorController.resetTouchOffsets();
        }
        stopTextActionMode();
    }

    private int getLastTapPosition() {
        if (this.mSelectionModifierCursorController != null) {
            int lastTapPosition = this.mSelectionModifierCursorController.getMinTouchOffset();
            if (lastTapPosition >= 0) {
                if (lastTapPosition > this.mTextView.getText().length()) {
                    lastTapPosition = this.mTextView.getText().length();
                }
                return lastTapPosition;
            }
        }
        return UNSET_X_VALUE;
    }

    void onWindowFocusChanged(boolean hasWindowFocus) {
        if (hasWindowFocus) {
            if (this.mBlink != null) {
                this.mBlink.uncancel();
                makeBlink();
            }
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (this.mTextView.hasSelection() && !extractedTextModeWillBeStarted()) {
                refreshTextActionMode();
                return;
            }
            return;
        }
        if (this.mBlink != null) {
            this.mBlink.cancel();
        }
        if (this.mInputContentType != null) {
            this.mInputContentType.enterDown = DEBUG_UNDO;
        }
        hideCursorAndSpanControllers();
        stopTextActionModeWithPreservingSelection();
        if (this.mSuggestionsPopupWindow != null) {
            this.mSuggestionsPopupWindow.onParentLostFocus();
        }
        ensureEndedBatchEdit();
    }

    private void updateTapState(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == 0) {
            boolean isMouse = event.isFromSource(InputDevice.SOURCE_MOUSE);
            if ((this.mTapState != TAP_STATE_FIRST_TAP && (this.mTapState != TAP_STATE_DOUBLE_TAP || !isMouse)) || SystemClock.uptimeMillis() - this.mLastTouchUpTime > ((long) ViewConfiguration.getDoubleTapTimeout())) {
                this.mTapState = TAP_STATE_FIRST_TAP;
            } else if (this.mTapState == TAP_STATE_FIRST_TAP) {
                this.mTapState = TAP_STATE_DOUBLE_TAP;
            } else {
                this.mTapState = TAP_STATE_TRIPLE_CLICK;
            }
        }
        if (action == TAP_STATE_FIRST_TAP) {
            this.mLastTouchUpTime = SystemClock.uptimeMillis();
        }
    }

    private boolean shouldFilterOutTouchEvent(MotionEvent event) {
        if (!event.isFromSource(InputDevice.SOURCE_MOUSE)) {
            return DEBUG_UNDO;
        }
        boolean primaryButtonStateChanged = ((this.mLastButtonState ^ event.getButtonState()) & TAP_STATE_FIRST_TAP) != 0 ? true : DEBUG_UNDO;
        int action = event.getActionMasked();
        if ((action == 0 || action == TAP_STATE_FIRST_TAP) && !primaryButtonStateChanged) {
            return true;
        }
        return (action != TAP_STATE_DOUBLE_TAP || event.isButtonPressed(TAP_STATE_FIRST_TAP)) ? DEBUG_UNDO : true;
    }

    void onTouchEvent(MotionEvent event) {
        boolean filterOutEvent = shouldFilterOutTouchEvent(event);
        this.mLastButtonState = event.getButtonState();
        if (filterOutEvent) {
            if (event.getActionMasked() == TAP_STATE_FIRST_TAP) {
                this.mDiscardNextActionUp = true;
            }
            return;
        }
        setPosWithMotionEvent(event, true);
        updateTapState(event);
        updateFloatingToolbarVisibility(event);
        if (hasSelectionController()) {
            getSelectionController().onTouchEvent(event);
        }
        if (this.mShowSuggestionRunnable != null) {
            this.mTextView.removeCallbacks(this.mShowSuggestionRunnable);
            this.mShowSuggestionRunnable = null;
        }
        if (event.getActionMasked() == 0) {
            this.mLastDownPositionX = event.getX();
            this.mLastDownPositionY = event.getY();
            this.mTouchFocusSelected = DEBUG_UNDO;
            this.mIgnoreActionUpEvent = DEBUG_UNDO;
        }
    }

    private void updateFloatingToolbarVisibility(MotionEvent event) {
        if (this.mTextActionMode != null) {
            switch (event.getActionMasked()) {
                case TAP_STATE_FIRST_TAP /*1*/:
                case TAP_STATE_TRIPLE_CLICK /*3*/:
                    showFloatingToolbar();
                case TAP_STATE_DOUBLE_TAP /*2*/:
                    hideFloatingToolbar();
                default:
            }
        }
    }

    private void hideFloatingToolbar() {
        if (this.mTextActionMode != null) {
            this.mTextView.removeCallbacks(this.mShowFloatingToolbar);
            this.mTextActionMode.hide(-1);
        }
    }

    protected void showFloatingToolbar() {
        if (this.mTextActionMode != null) {
            this.mTextView.postDelayed(this.mShowFloatingToolbar, (long) ViewConfiguration.getDoubleTapTimeout());
        }
    }

    public void beginBatchEdit() {
        this.mInBatchEditControllers = true;
        InputMethodState ims = this.mInputMethodState;
        if (ims != null) {
            int nesting = ims.mBatchEditNesting + TAP_STATE_FIRST_TAP;
            ims.mBatchEditNesting = nesting;
            if (nesting == TAP_STATE_FIRST_TAP) {
                ims.mCursorChanged = DEBUG_UNDO;
                ims.mChangedDelta = TAP_STATE_INITIAL;
                if (ims.mContentChanged) {
                    ims.mChangedStart = TAP_STATE_INITIAL;
                    ims.mChangedEnd = this.mTextView.getText().length();
                } else {
                    ims.mChangedStart = UNSET_X_VALUE;
                    ims.mChangedEnd = UNSET_X_VALUE;
                    ims.mContentChanged = DEBUG_UNDO;
                }
                this.mUndoInputFilter.beginBatchEdit();
                this.mTextView.onBeginBatchEdit();
            }
        }
    }

    public void endBatchEdit() {
        this.mInBatchEditControllers = DEBUG_UNDO;
        InputMethodState ims = this.mInputMethodState;
        if (ims != null) {
            int nesting = ims.mBatchEditNesting + UNSET_X_VALUE;
            ims.mBatchEditNesting = nesting;
            if (nesting == 0) {
                finishBatchEdit(ims);
            }
        }
    }

    void ensureEndedBatchEdit() {
        InputMethodState ims = this.mInputMethodState;
        if (ims != null && ims.mBatchEditNesting != 0) {
            ims.mBatchEditNesting = TAP_STATE_INITIAL;
            finishBatchEdit(ims);
        }
    }

    void finishBatchEdit(InputMethodState ims) {
        this.mTextView.onEndBatchEdit();
        this.mUndoInputFilter.endBatchEdit();
        if (ims.mContentChanged || ims.mSelectionModeChanged) {
            this.mTextView.updateAfterEdit();
            reportExtractedText();
        } else if (ims.mCursorChanged) {
            this.mTextView.invalidateCursor();
        }
        sendUpdateSelection();
        if (this.mTextActionMode != null) {
            CursorController cursorController = this.mTextView.hasSelection() ? getSelectionController() : getInsertionController();
            if (cursorController != null && !cursorController.isActive() && !cursorController.isCursorBeingModified()) {
                cursorController.show();
            }
        }
    }

    boolean extractText(ExtractedTextRequest request, ExtractedText outText) {
        return extractTextInternal(request, UNSET_X_VALUE, UNSET_X_VALUE, UNSET_X_VALUE, outText);
    }

    private boolean extractTextInternal(ExtractedTextRequest request, int partialStartOffset, int partialEndOffset, int delta, ExtractedText outText) {
        if (request == null || outText == null) {
            return DEBUG_UNDO;
        }
        CharSequence content = this.mTextView.getText();
        if (content == null) {
            return DEBUG_UNDO;
        }
        if (partialStartOffset != EXTRACT_NOTHING) {
            int N = content.length();
            if (partialStartOffset < 0) {
                outText.partialEndOffset = UNSET_X_VALUE;
                outText.partialStartOffset = UNSET_X_VALUE;
                partialStartOffset = TAP_STATE_INITIAL;
                partialEndOffset = N;
            } else {
                partialEndOffset += delta;
                if (content instanceof Spanned) {
                    Spanned spanned = (Spanned) content;
                    Object[] spans = spanned.getSpans(partialStartOffset, partialEndOffset, ParcelableSpan.class);
                    int i = spans.length;
                    while (i > 0) {
                        i += UNSET_X_VALUE;
                        int j = spanned.getSpanStart(spans[i]);
                        if (j < partialStartOffset) {
                            partialStartOffset = j;
                        }
                        j = spanned.getSpanEnd(spans[i]);
                        if (j > partialEndOffset) {
                            partialEndOffset = j;
                        }
                    }
                }
                outText.partialStartOffset = partialStartOffset;
                outText.partialEndOffset = partialEndOffset - delta;
                if (partialStartOffset > N) {
                    partialStartOffset = N;
                } else if (partialStartOffset < 0) {
                    partialStartOffset = TAP_STATE_INITIAL;
                }
                if (partialEndOffset > N) {
                    partialEndOffset = N;
                } else if (partialEndOffset < 0) {
                    partialEndOffset = TAP_STATE_INITIAL;
                }
            }
            if ((request.flags & TAP_STATE_FIRST_TAP) != 0) {
                outText.text = content.subSequence(partialStartOffset, partialEndOffset);
            } else {
                outText.text = TextUtils.substring(content, partialStartOffset, partialEndOffset);
            }
        } else {
            outText.partialStartOffset = TAP_STATE_INITIAL;
            outText.partialEndOffset = TAP_STATE_INITIAL;
            outText.text = "";
        }
        outText.flags = TAP_STATE_INITIAL;
        if (MetaKeyKeyListener.getMetaState(content, (int) GL10.GL_EXP) != 0) {
            outText.flags |= TAP_STATE_DOUBLE_TAP;
        }
        if (this.mTextView.isSingleLine()) {
            outText.flags |= TAP_STATE_FIRST_TAP;
        }
        outText.startOffset = TAP_STATE_INITIAL;
        outText.selectionStart = this.mTextView.getSelectionStart();
        outText.selectionEnd = this.mTextView.getSelectionEnd();
        return true;
    }

    boolean reportExtractedText() {
        InputMethodState ims = this.mInputMethodState;
        if (ims != null) {
            boolean contentChanged = ims.mContentChanged;
            if (contentChanged || ims.mSelectionModeChanged) {
                ims.mContentChanged = DEBUG_UNDO;
                ims.mSelectionModeChanged = DEBUG_UNDO;
                ExtractedTextRequest req = ims.mExtractedTextRequest;
                if (req != null) {
                    InputMethodManager imm = InputMethodManager.peekInstance();
                    if (imm != null) {
                        if (ims.mChangedStart < 0 && !contentChanged) {
                            ims.mChangedStart = EXTRACT_NOTHING;
                        }
                        if (extractTextInternal(req, ims.mChangedStart, ims.mChangedEnd, ims.mChangedDelta, ims.mExtractedText)) {
                            imm.updateExtractedText(this.mTextView, req.token, ims.mExtractedText);
                            ims.mChangedStart = UNSET_X_VALUE;
                            ims.mChangedEnd = UNSET_X_VALUE;
                            ims.mChangedDelta = TAP_STATE_INITIAL;
                            ims.mContentChanged = DEBUG_UNDO;
                            return true;
                        }
                    }
                }
            }
        }
        return DEBUG_UNDO;
    }

    private void sendUpdateSelection() {
        if (this.mInputMethodState != null && this.mInputMethodState.mBatchEditNesting <= 0) {
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null) {
                int selectionStart = this.mTextView.getSelectionStart();
                int selectionEnd = this.mTextView.getSelectionEnd();
                int candStart = UNSET_X_VALUE;
                int candEnd = UNSET_X_VALUE;
                if (this.mTextView.getText() instanceof Spannable) {
                    Spannable sp = (Spannable) this.mTextView.getText();
                    candStart = BaseInputConnection.getComposingSpanStart(sp);
                    candEnd = BaseInputConnection.getComposingSpanEnd(sp);
                }
                imm.updateSelection(this.mTextView, selectionStart, selectionEnd, candStart, candEnd);
            }
        }
    }

    void onDraw(Canvas canvas, Layout layout, Path highlight, Paint highlightPaint, int cursorOffsetVertical) {
        int selectionStart = this.mTextView.getSelectionStart();
        int selectionEnd = this.mTextView.getSelectionEnd();
        InputMethodState ims = this.mInputMethodState;
        if (ims != null && ims.mBatchEditNesting == 0) {
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null && imm.isActive(this.mTextView) && (ims.mContentChanged || ims.mSelectionModeChanged)) {
                reportExtractedText();
            }
        }
        if (this.mCorrectionHighlighter != null) {
            this.mCorrectionHighlighter.draw(canvas, cursorOffsetVertical);
        }
        if (highlight != null && selectionStart == selectionEnd && this.mCursorCount > 0) {
            drawCursor(canvas, cursorOffsetVertical);
            highlight = null;
        }
        if (this.mTextView.canHaveDisplayList() && canvas.isHardwareAccelerated()) {
            drawHardwareAccelerated(canvas, layout, highlight, highlightPaint, cursorOffsetVertical);
        } else {
            layout.draw(canvas, highlight, highlightPaint, cursorOffsetVertical);
        }
    }

    private void drawHardwareAccelerated(Canvas canvas, Layout layout, Path highlight, Paint highlightPaint, int cursorOffsetVertical) {
        long lineRange = layout.getLineRangeForDraw(canvas);
        int firstLine = TextUtils.unpackRangeStartFromLong(lineRange);
        int lastLine = TextUtils.unpackRangeEndFromLong(lineRange);
        if (lastLine >= 0) {
            layout.drawBackground(canvas, highlight, highlightPaint, cursorOffsetVertical, firstLine, lastLine);
            if (layout instanceof DynamicLayout) {
                if (this.mTextRenderNodes == null) {
                    this.mTextRenderNodes = (TextRenderNode[]) ArrayUtils.emptyArray(TextRenderNode.class);
                }
                DynamicLayout dynamicLayout = (DynamicLayout) layout;
                int[] blockEndLines = dynamicLayout.getBlockEndLines();
                int[] blockIndices = dynamicLayout.getBlockIndices();
                int numberOfBlocks = dynamicLayout.getNumberOfBlocks();
                int indexFirstChangedBlock = dynamicLayout.getIndexFirstChangedBlock();
                int endOfPreviousBlock = UNSET_X_VALUE;
                int searchStartIndex = TAP_STATE_INITIAL;
                for (int i = TAP_STATE_INITIAL; i < numberOfBlocks; i += TAP_STATE_FIRST_TAP) {
                    int blockEndLine = blockEndLines[i];
                    int blockIndex = blockIndices[i];
                    if (blockIndex == UNSET_X_VALUE ? true : DEBUG_UNDO) {
                        blockIndex = getAvailableDisplayListIndex(blockIndices, numberOfBlocks, searchStartIndex);
                        blockIndices[i] = blockIndex;
                        if (this.mTextRenderNodes[blockIndex] != null) {
                            this.mTextRenderNodes[blockIndex].isDirty = true;
                        }
                        searchStartIndex = blockIndex + TAP_STATE_FIRST_TAP;
                    }
                    if (this.mTextRenderNodes[blockIndex] == null) {
                        this.mTextRenderNodes[blockIndex] = new TextRenderNode("Text " + blockIndex);
                    }
                    boolean blockDisplayListIsInvalid = this.mTextRenderNodes[blockIndex].needsRecord();
                    RenderNode blockDisplayList = this.mTextRenderNodes[blockIndex].renderNode;
                    if (i >= indexFirstChangedBlock || blockDisplayListIsInvalid) {
                        int blockBeginLine = endOfPreviousBlock + TAP_STATE_FIRST_TAP;
                        int top = layout.getLineTop(blockBeginLine);
                        int bottom = layout.getLineBottom(blockEndLine);
                        int left = TAP_STATE_INITIAL;
                        int right = this.mTextView.getWidth();
                        if (this.mTextView.getHorizontallyScrolling()) {
                            float min = AutoScrollHelper.NO_MAX;
                            float max = Float.MIN_VALUE;
                            for (int line = blockBeginLine; line <= blockEndLine; line += TAP_STATE_FIRST_TAP) {
                                min = Math.min(min, layout.getLineLeft(line));
                                max = Math.max(max, layout.getLineRight(line));
                            }
                            left = (int) min;
                            right = (int) (LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS + max);
                        }
                        if (blockDisplayListIsInvalid) {
                            Canvas displayListCanvas = blockDisplayList.start(right - left, bottom - top);
                            try {
                                displayListCanvas.translate((float) (-left), (float) (-top));
                                layout.drawText(displayListCanvas, blockBeginLine, blockEndLine);
                                this.mTextRenderNodes[blockIndex].isDirty = DEBUG_UNDO;
                            } finally {
                                blockDisplayList.end(displayListCanvas);
                                blockDisplayList.setClipToBounds(DEBUG_UNDO);
                            }
                        }
                        blockDisplayList.setLeftTopRightBottom(left, top, right, bottom);
                    }
                    ((DisplayListCanvas) canvas).drawRenderNode(blockDisplayList);
                    endOfPreviousBlock = blockEndLine;
                }
                dynamicLayout.setIndexFirstChangedBlock(numberOfBlocks);
            } else {
                layout.drawText(canvas, firstLine, lastLine);
            }
        }
    }

    private int getAvailableDisplayListIndex(int[] blockIndices, int numberOfBlocks, int searchStartIndex) {
        int length = this.mTextRenderNodes.length;
        for (int i = searchStartIndex; i < length; i += TAP_STATE_FIRST_TAP) {
            boolean blockIndexFound = DEBUG_UNDO;
            for (int j = TAP_STATE_INITIAL; j < numberOfBlocks; j += TAP_STATE_FIRST_TAP) {
                if (blockIndices[j] == i) {
                    blockIndexFound = true;
                    break;
                }
            }
            if (!blockIndexFound) {
                return i;
            }
        }
        this.mTextRenderNodes = (TextRenderNode[]) GrowingArrayUtils.append(this.mTextRenderNodes, length, null);
        return length;
    }

    private void drawCursor(Canvas canvas, int cursorOffsetVertical) {
        boolean translate = DEBUG_UNDO;
        if (cursorOffsetVertical != 0) {
            translate = true;
        }
        if (translate) {
            canvas.translate(0.0f, (float) cursorOffsetVertical);
        }
        for (int i = TAP_STATE_INITIAL; i < this.mCursorCount; i += TAP_STATE_FIRST_TAP) {
            this.mCursorDrawable[i].draw(canvas);
        }
        if (translate) {
            canvas.translate(0.0f, (float) (-cursorOffsetVertical));
        }
    }

    void invalidateHandlesAndActionMode() {
        if (this.mSelectionModifierCursorController != null) {
            this.mSelectionModifierCursorController.invalidateHandles();
        }
        if (this.mInsertionPointCursorController != null) {
            this.mInsertionPointCursorController.invalidateHandle();
        }
        if (this.mTextActionMode != null) {
            this.mTextActionMode.invalidate();
        }
    }

    void invalidateTextDisplayList(Layout layout, int start, int end) {
        if (this.mTextRenderNodes != null && (layout instanceof DynamicLayout)) {
            int firstLine = layout.getLineForOffset(start);
            int lastLine = layout.getLineForOffset(end);
            DynamicLayout dynamicLayout = (DynamicLayout) layout;
            int[] blockEndLines = dynamicLayout.getBlockEndLines();
            int[] blockIndices = dynamicLayout.getBlockIndices();
            int numberOfBlocks = dynamicLayout.getNumberOfBlocks();
            int i = TAP_STATE_INITIAL;
            while (i < numberOfBlocks && blockEndLines[i] < firstLine) {
                i += TAP_STATE_FIRST_TAP;
            }
            while (i < numberOfBlocks) {
                int blockIndex = blockIndices[i];
                if (blockIndex != UNSET_X_VALUE) {
                    this.mTextRenderNodes[blockIndex].isDirty = true;
                }
                if (blockEndLines[i] < lastLine) {
                    i += TAP_STATE_FIRST_TAP;
                } else {
                    return;
                }
            }
        }
    }

    void invalidateTextDisplayList() {
        if (this.mTextRenderNodes != null) {
            for (int i = TAP_STATE_INITIAL; i < this.mTextRenderNodes.length; i += TAP_STATE_FIRST_TAP) {
                if (this.mTextRenderNodes[i] != null) {
                    this.mTextRenderNodes[i].isDirty = true;
                }
            }
        }
    }

    protected void updateCursorsPositions() {
        if (this.mTextView.mCursorDrawableRes == 0) {
            this.mCursorCount = TAP_STATE_INITIAL;
            return;
        }
        int i;
        Layout layout = this.mTextView.getLayout();
        int offset = this.mTextView.getSelectionStart();
        int line = layout.getLineForOffset(offset);
        int top = layout.getLineTop(line);
        int bottom = layout.getLineTop(line + TAP_STATE_FIRST_TAP);
        float PositionX = layout.getPrimaryHorizontal(offset, layout.shouldClampCursor(line));
        if (adjustCursorPos(line, layout)) {
            top = getCursorTop();
            bottom = getCursorBottom();
            PositionX = getCursorX();
        }
        if (layout.isLevelBoundary(offset)) {
            i = TAP_STATE_DOUBLE_TAP;
        } else {
            i = TAP_STATE_FIRST_TAP;
        }
        this.mCursorCount = i;
        int middle = bottom;
        if (this.mCursorCount == TAP_STATE_DOUBLE_TAP) {
            middle = (top + bottom) >> TAP_STATE_FIRST_TAP;
        }
        updateCursorPosition(TAP_STATE_INITIAL, top, middle, PositionX);
        if (this.mCursorCount == TAP_STATE_DOUBLE_TAP) {
            updateCursorPosition(TAP_STATE_FIRST_TAP, middle, bottom, PositionX);
        }
    }

    void refreshTextActionMode() {
        if (extractedTextModeWillBeStarted()) {
            this.mRestartActionModeOnNextRefresh = DEBUG_UNDO;
            return;
        }
        boolean hasSelection = this.mTextView.hasSelection();
        SelectionModifierCursorController selectionController = getSelectionController();
        InsertionPointCursorController insertionController = getInsertionController();
        if ((selectionController == null || !selectionController.isCursorBeingModified()) && (insertionController == null || !insertionController.isCursorBeingModified())) {
            if (hasSelection) {
                hideInsertionPointCursorController();
                if (this.mTextActionMode == null) {
                    if (this.mRestartActionModeOnNextRefresh) {
                        startSelectionActionMode();
                    }
                } else if (selectionController == null || !selectionController.isActive()) {
                    stopTextActionModeWithPreservingSelection();
                    startSelectionActionMode();
                } else {
                    this.mTextActionMode.invalidateContentRect();
                }
            } else if (insertionController == null || !insertionController.isActive()) {
                stopTextActionMode();
            } else if (this.mTextActionMode != null) {
                this.mTextActionMode.invalidateContentRect();
            }
            this.mRestartActionModeOnNextRefresh = DEBUG_UNDO;
            return;
        }
        this.mRestartActionModeOnNextRefresh = DEBUG_UNDO;
    }

    void startInsertionActionMode() {
        if (this.mInsertionActionModeRunnable != null) {
            this.mTextView.removeCallbacks(this.mInsertionActionModeRunnable);
        }
        if (!extractedTextModeWillBeStarted()) {
            stopTextActionMode();
            this.mTextActionMode = this.mTextView.startActionMode(new TextActionModeCallback(this, DEBUG_UNDO), TAP_STATE_FIRST_TAP);
            if (!(this.mTextActionMode == null || getInsertionController() == null)) {
                getInsertionController().show();
            }
        }
    }

    protected boolean startSelectionActionMode() {
        boolean selectionStarted = startSelectionActionModeInternal();
        if (selectionStarted) {
            getSelectionController().show();
        }
        this.mRestartActionModeOnNextRefresh = DEBUG_UNDO;
        return selectionStarted;
    }

    private boolean selectCurrentWordAndStartDrag() {
        if (this.mInsertionActionModeRunnable != null) {
            this.mTextView.removeCallbacks(this.mInsertionActionModeRunnable);
        }
        if (extractedTextModeWillBeStarted() || !checkField()) {
            return DEBUG_UNDO;
        }
        if (!this.mTextView.hasSelection() && !selectCurrentWord()) {
            return DEBUG_UNDO;
        }
        stopTextActionModeWithPreservingSelection();
        getSelectionController().enterDrag(TAP_STATE_DOUBLE_TAP);
        return true;
    }

    boolean checkField() {
        if (this.mTextView.canSelectText() && this.mTextView.requestFocus()) {
            return true;
        }
        Log.w("TextView", "TextView does not support text selection. Selection cancelled.");
        return DEBUG_UNDO;
    }

    private boolean startSelectionActionModeInternal() {
        boolean selectionStarted = true;
        if (extractedTextModeWillBeStarted()) {
            return DEBUG_UNDO;
        }
        if (this.mTextActionMode != null) {
            this.mTextActionMode.invalidate();
            return DEBUG_UNDO;
        } else if (!checkField() || !this.mTextView.hasSelection()) {
            return DEBUG_UNDO;
        } else {
            this.mTextActionMode = this.mTextView.startActionMode(new TextActionModeCallback(this, true), TAP_STATE_FIRST_TAP);
            if (this.mTextActionMode == null) {
                selectionStarted = DEBUG_UNDO;
            }
            if (selectionStarted && !this.mTextView.isTextSelectable() && this.mShowSoftInputOnFocus) {
                InputMethodManager imm = InputMethodManager.peekInstance();
                if (imm != null) {
                    imm.showSoftInput(this.mTextView, TAP_STATE_INITIAL, null);
                }
            }
            return selectionStarted;
        }
    }

    boolean extractedTextModeWillBeStarted() {
        boolean z = DEBUG_UNDO;
        if (this.mTextView.isInExtractedMode()) {
            return DEBUG_UNDO;
        }
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (imm != null) {
            z = imm.isFullscreenMode();
        }
        return z;
    }

    private boolean shouldOfferToShowSuggestions() {
        CharSequence text = this.mTextView.getText();
        if (!(text instanceof Spannable)) {
            return DEBUG_UNDO;
        }
        Spannable spannable = (Spannable) text;
        int selectionStart = this.mTextView.getSelectionStart();
        int selectionEnd = this.mTextView.getSelectionEnd();
        SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) spannable.getSpans(selectionStart, selectionEnd, SuggestionSpan.class);
        if (suggestionSpans.length == 0) {
            return DEBUG_UNDO;
        }
        int i;
        if (selectionStart == selectionEnd) {
            for (i = TAP_STATE_INITIAL; i < suggestionSpans.length; i += TAP_STATE_FIRST_TAP) {
                if (suggestionSpans[i].getSuggestions().length > 0) {
                    return true;
                }
            }
            return DEBUG_UNDO;
        }
        int minSpanStart = this.mTextView.getText().length();
        int maxSpanEnd = TAP_STATE_INITIAL;
        int unionOfSpansCoveringSelectionStartStart = this.mTextView.getText().length();
        int unionOfSpansCoveringSelectionStartEnd = TAP_STATE_INITIAL;
        boolean hasValidSuggestions = DEBUG_UNDO;
        for (i = TAP_STATE_INITIAL; i < suggestionSpans.length; i += TAP_STATE_FIRST_TAP) {
            int spanStart = spannable.getSpanStart(suggestionSpans[i]);
            int spanEnd = spannable.getSpanEnd(suggestionSpans[i]);
            minSpanStart = Math.min(minSpanStart, spanStart);
            maxSpanEnd = Math.max(maxSpanEnd, spanEnd);
            if (selectionStart >= spanStart && selectionStart <= spanEnd) {
                hasValidSuggestions = (hasValidSuggestions || suggestionSpans[i].getSuggestions().length > 0) ? true : DEBUG_UNDO;
                unionOfSpansCoveringSelectionStartStart = Math.min(unionOfSpansCoveringSelectionStartStart, spanStart);
                unionOfSpansCoveringSelectionStartEnd = Math.max(unionOfSpansCoveringSelectionStartEnd, spanEnd);
            }
        }
        if (!hasValidSuggestions) {
            return DEBUG_UNDO;
        }
        if (unionOfSpansCoveringSelectionStartStart >= unionOfSpansCoveringSelectionStartEnd) {
            return DEBUG_UNDO;
        }
        if (minSpanStart < unionOfSpansCoveringSelectionStartStart || maxSpanEnd > unionOfSpansCoveringSelectionStartEnd) {
            return DEBUG_UNDO;
        }
        return true;
    }

    private boolean isCursorInsideEasyCorrectionSpan() {
        SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) ((Spannable) this.mTextView.getText()).getSpans(this.mTextView.getSelectionStart(), this.mTextView.getSelectionEnd(), SuggestionSpan.class);
        for (int i = TAP_STATE_INITIAL; i < suggestionSpans.length; i += TAP_STATE_FIRST_TAP) {
            if ((suggestionSpans[i].getFlags() & TAP_STATE_FIRST_TAP) != 0) {
                return true;
            }
        }
        return DEBUG_UNDO;
    }

    void onTouchUpEvent(MotionEvent event) {
        boolean didTouchFocusSelect = this.mSelectAllOnFocus ? this.mTextView.didTouchFocusSelect() : DEBUG_UNDO;
        hideCursorAndSpanControllers();
        stopTextActionMode();
        CharSequence text = this.mTextView.getText();
        if (!didTouchFocusSelect && text.length() > 0) {
            int offset = this.mTextView.getOffsetForPosition(event.getX(), event.getY());
            setPosWithMotionEvent(event, true);
            Selection.setSelection((Spannable) text, adjustOffsetAtLineEndForTouchPos(offset));
            if (this.mSpellChecker != null) {
                this.mSpellChecker.onSelectionChanged();
            }
            if (!extractedTextModeWillBeStarted()) {
                if (isCursorInsideEasyCorrectionSpan()) {
                    if (this.mInsertionActionModeRunnable != null) {
                        this.mTextView.removeCallbacks(this.mInsertionActionModeRunnable);
                    }
                    this.mShowSuggestionRunnable = new Runnable() {
                        public void run() {
                            try {
                                Editor.this.replace();
                            } catch (NotFoundException e) {
                                Log.e(Editor.UNDO_OWNER_TAG, "Widget of Editor resource not found issue.", e);
                            }
                        }
                    };
                    this.mTextView.postDelayed(this.mShowSuggestionRunnable, (long) ViewConfiguration.getDoubleTapTimeout());
                } else if (hasInsertionController()) {
                    getInsertionController().show();
                }
            }
        }
    }

    protected void stopTextActionMode() {
        if (this.mTextActionMode != null) {
            this.mTextActionMode.finish();
        }
    }

    private void stopTextActionModeWithPreservingSelection() {
        if (this.mTextActionMode != null) {
            this.mRestartActionModeOnNextRefresh = true;
        }
        this.mPreserveSelection = true;
        stopTextActionMode();
        this.mPreserveSelection = DEBUG_UNDO;
    }

    boolean hasInsertionController() {
        return this.mInsertionControllerEnabled;
    }

    boolean hasSelectionController() {
        return this.mSelectionControllerEnabled;
    }

    InsertionPointCursorController getInsertionController() {
        if (!this.mInsertionControllerEnabled) {
            return null;
        }
        if (this.mInsertionPointCursorController == null) {
            this.mInsertionPointCursorController = new InsertionPointCursorController();
            this.mTextView.getViewTreeObserver().addOnTouchModeChangeListener(this.mInsertionPointCursorController);
        }
        return this.mInsertionPointCursorController;
    }

    SelectionModifierCursorController getSelectionController() {
        if (!this.mSelectionControllerEnabled) {
            return null;
        }
        if (this.mSelectionModifierCursorController == null) {
            this.mSelectionModifierCursorController = new SelectionModifierCursorController(this);
            this.mTextView.getViewTreeObserver().addOnTouchModeChangeListener(this.mSelectionModifierCursorController);
        }
        return this.mSelectionModifierCursorController;
    }

    public Drawable[] getCursorDrawable() {
        return this.mCursorDrawable;
    }

    private void updateCursorPosition(int cursorIndex, int top, int bottom, float horizontal) {
        if (this.mCursorDrawable[cursorIndex] == null) {
            this.mCursorDrawable[cursorIndex] = this.mTextView.getContext().getDrawable(this.mTextView.mCursorDrawableRes);
        }
        Drawable drawable = this.mCursorDrawable[cursorIndex];
        horizontal = Math.max(LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS, horizontal - LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS);
        if (this.mTempRect == null) {
            this.mTempRect = new Rect();
        }
        drawable.getPadding(this.mTempRect);
        int left = ((int) horizontal) - this.mTempRect.left;
        drawable.setBounds(left, top - this.mTempRect.top, left + drawable.getIntrinsicWidth(), this.mTempRect.bottom + bottom);
    }

    private int clampHorizontalPosition(Drawable drawable, float horizontal) {
        horizontal = Math.max(LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS, horizontal - LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS);
        if (this.mTempRect == null) {
            this.mTempRect = new Rect();
        }
        int drawableWidth = TAP_STATE_INITIAL;
        if (drawable != null) {
            drawable.getPadding(this.mTempRect);
            drawableWidth = drawable.getIntrinsicWidth();
        } else {
            this.mTempRect.setEmpty();
        }
        int scrollX = this.mTextView.getScrollX();
        float horizontalDiff = horizontal - ((float) scrollX);
        int viewClippedWidth = (this.mTextView.getWidth() - this.mTextView.getCompoundPaddingLeft()) - this.mTextView.getCompoundPaddingRight();
        if (horizontalDiff >= ((float) viewClippedWidth) - LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
            return (viewClippedWidth + scrollX) - (drawableWidth - this.mTempRect.right);
        }
        if (Math.abs(horizontalDiff) <= LayoutParams.BRIGHTNESS_OVERRIDE_FULL || (TextUtils.isEmpty(this.mTextView.getText()) && ((float) (AccessibilityNodeInfo.ACTION_DISMISS - scrollX)) <= ((float) viewClippedWidth) + LayoutParams.BRIGHTNESS_OVERRIDE_FULL && horizontal <= LayoutParams.BRIGHTNESS_OVERRIDE_FULL)) {
            return scrollX - this.mTempRect.left;
        }
        return ((int) horizontal) - this.mTempRect.left;
    }

    public void onCommitCorrection(CorrectionInfo info) {
        if (this.mCorrectionHighlighter == null) {
            this.mCorrectionHighlighter = new CorrectionHighlighter();
        } else {
            this.mCorrectionHighlighter.invalidate(DEBUG_UNDO);
        }
        this.mCorrectionHighlighter.highlight(info);
    }

    void onScrollChanged() {
        if (this.mPositionListener != null) {
            this.mPositionListener.onScrollChanged();
        }
        if (this.mTextActionMode != null) {
            this.mTextActionMode.invalidateContentRect();
        }
    }

    private boolean shouldBlink() {
        boolean z = DEBUG_UNDO;
        if (!isCursorVisible() || !this.mTextView.isFocused()) {
            return DEBUG_UNDO;
        }
        int start = this.mTextView.getSelectionStart();
        if (start < 0) {
            return DEBUG_UNDO;
        }
        int end = this.mTextView.getSelectionEnd();
        if (end < 0) {
            return DEBUG_UNDO;
        }
        if (start == end) {
            z = true;
        }
        return z;
    }

    void makeBlink() {
        if (shouldBlink()) {
            this.mShowCursor = SystemClock.uptimeMillis();
            if (this.mBlink == null) {
                this.mBlink = new Blink();
            }
            this.mTextView.removeCallbacks(this.mBlink);
            this.mTextView.postDelayed(this.mBlink, 500);
        } else if (this.mBlink != null) {
            this.mTextView.removeCallbacks(this.mBlink);
        }
    }

    private DragShadowBuilder getTextThumbnailBuilder(int start, int end) {
        TextView shadowView = (TextView) View.inflate(this.mTextView.getContext(), R.layout.text_drag_thumbnail, null);
        if (shadowView == null) {
            throw new IllegalArgumentException("Unable to inflate text drag thumbnail");
        }
        if (end - start > DRAG_SHADOW_MAX_TEXT_LENGTH) {
            end = TextUtils.unpackRangeEndFromLong(getCharClusterRange(DRAG_SHADOW_MAX_TEXT_LENGTH + start));
        }
        shadowView.setText(this.mTextView.getTransformedText(start, end));
        shadowView.setTextColor(this.mTextView.getTextColors());
        shadowView.setTextAppearance(16);
        shadowView.setGravity(17);
        shadowView.setLayoutParams(new ViewGroup.LayoutParams((int) EXTRACT_NOTHING, (int) EXTRACT_NOTHING));
        int size = MeasureSpec.makeMeasureSpec(TAP_STATE_INITIAL, TAP_STATE_INITIAL);
        shadowView.measure(size, size);
        shadowView.layout(TAP_STATE_INITIAL, TAP_STATE_INITIAL, shadowView.getMeasuredWidth(), shadowView.getMeasuredHeight());
        shadowView.invalidate();
        return new DragShadowBuilder(shadowView);
    }

    void onDrop(DragEvent event) {
        StringBuilder content = new StringBuilder("");
        DragAndDropPermissions permissions = DragAndDropPermissions.obtain(event);
        if (permissions != null) {
            permissions.takeTransient();
        }
        try {
            boolean dragDropIntoItself;
            ClipData clipData = event.getClipData();
            int itemCount = clipData.getItemCount();
            for (int i = TAP_STATE_INITIAL; i < itemCount; i += TAP_STATE_FIRST_TAP) {
                content.append(clipData.getItemAt(i).coerceToStyledText(this.mTextView.getContext()));
            }
            int offset = this.mTextView.getOffsetForPosition(event.getX(), event.getY());
            DragLocalState localState = event.getLocalState();
            DragLocalState dragLocalState = null;
            if (localState instanceof DragLocalState) {
                dragLocalState = localState;
            }
            if (dragLocalState != null) {
                dragDropIntoItself = dragLocalState.sourceTextView == this.mTextView ? true : DEBUG_UNDO;
            } else {
                dragDropIntoItself = DEBUG_UNDO;
            }
            if (!dragDropIntoItself || offset < dragLocalState.start || offset >= dragLocalState.end) {
                int originalLength = this.mTextView.getText().length();
                int min = offset;
                int max = offset;
                Selection.setSelection((Spannable) this.mTextView.getText(), offset);
                this.mTextView.replaceText_internal(offset, offset, content);
                if (dragDropIntoItself) {
                    int dragSourceStart = dragLocalState.start;
                    int dragSourceEnd = dragLocalState.end;
                    if (offset <= dragSourceStart) {
                        int shift = this.mTextView.getText().length() - originalLength;
                        dragSourceStart += shift;
                        dragSourceEnd += shift;
                    }
                    this.mUndoInputFilter.setForceMerge(true);
                    try {
                        this.mTextView.deleteText_internal(dragSourceStart, dragSourceEnd);
                        int prevCharIdx = Math.max(TAP_STATE_INITIAL, dragSourceStart + UNSET_X_VALUE);
                        int nextCharIdx = Math.min(this.mTextView.getText().length(), dragSourceStart + TAP_STATE_FIRST_TAP);
                        if (nextCharIdx > prevCharIdx + TAP_STATE_FIRST_TAP) {
                            CharSequence t = this.mTextView.getTransformedText(prevCharIdx, nextCharIdx);
                            if (Character.isSpaceChar(t.charAt(TAP_STATE_INITIAL)) && Character.isSpaceChar(t.charAt(TAP_STATE_FIRST_TAP))) {
                                this.mTextView.deleteText_internal(prevCharIdx, prevCharIdx + TAP_STATE_FIRST_TAP);
                            }
                        }
                        this.mUndoInputFilter.setForceMerge(DEBUG_UNDO);
                    } catch (Throwable th) {
                        this.mUndoInputFilter.setForceMerge(DEBUG_UNDO);
                    }
                }
            }
        } finally {
            if (permissions != null) {
                permissions.release();
            }
        }
    }

    public void addSpanWatchers(Spannable text) {
        int textLength = text.length();
        if (this.mKeyListener != null) {
            text.setSpan(this.mKeyListener, TAP_STATE_INITIAL, textLength, 18);
        }
        if (this.mSpanController == null) {
            this.mSpanController = new SpanController(this);
        }
        text.setSpan(this.mSpanController, TAP_STATE_INITIAL, textLength, 18);
    }

    void setContextMenuAnchor(float x, float y) {
        this.mContextMenuAnchorX = x;
        this.mContextMenuAnchorY = y;
    }

    void onCreateContextMenu(ContextMenu menu) {
        if (!this.mIsBeingLongClicked && !Float.isNaN(this.mContextMenuAnchorX) && !Float.isNaN(this.mContextMenuAnchorY)) {
            int offset = this.mTextView.getOffsetForPosition(this.mContextMenuAnchorX, this.mContextMenuAnchorY);
            if (offset != UNSET_X_VALUE) {
                boolean isOnSelection;
                stopTextActionModeWithPreservingSelection();
                if (!this.mTextView.hasSelection() || offset < this.mTextView.getSelectionStart()) {
                    isOnSelection = DEBUG_UNDO;
                } else {
                    isOnSelection = offset <= this.mTextView.getSelectionEnd() ? true : DEBUG_UNDO;
                }
                if (!isOnSelection) {
                    Selection.setSelection((Spannable) this.mTextView.getText(), offset);
                    stopTextActionMode();
                }
                if (shouldOfferToShowSuggestions()) {
                    int i;
                    SuggestionInfo[] suggestionInfoArray = new SuggestionInfo[MENU_ITEM_ORDER_COPY];
                    for (i = TAP_STATE_INITIAL; i < suggestionInfoArray.length; i += TAP_STATE_FIRST_TAP) {
                        suggestionInfoArray[i] = new SuggestionInfo();
                    }
                    SubMenu subMenu = menu.addSubMenu((int) TAP_STATE_INITIAL, (int) TAP_STATE_INITIAL, (int) MENU_ITEM_ORDER_REPLACE, (int) R.string.replace);
                    int numItems = this.mSuggestionHelper.getSuggestionInfo(suggestionInfoArray, null);
                    for (i = TAP_STATE_INITIAL; i < numItems; i += TAP_STATE_FIRST_TAP) {
                        SuggestionInfo info = suggestionInfoArray[i];
                        subMenu.add((int) TAP_STATE_INITIAL, (int) TAP_STATE_INITIAL, i, info.mText).setOnMenuItemClickListener(new AnonymousClass4(info));
                    }
                }
                menu.add((int) TAP_STATE_INITIAL, (int) R.id.undo, (int) TAP_STATE_DOUBLE_TAP, (int) R.string.undo).setAlphabeticShortcut(DateFormat.TIME_ZONE).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener).setEnabled(this.mTextView.canUndo());
                menu.add((int) TAP_STATE_INITIAL, (int) R.id.redo, (int) TAP_STATE_TRIPLE_CLICK, (int) R.string.redo).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener).setEnabled(this.mTextView.canRedo());
                menu.add((int) TAP_STATE_INITIAL, (int) R.id.cut, (int) MENU_ITEM_ORDER_CUT, (int) R.string.cut).setAlphabeticShortcut(StateProperty.TARGET_X).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener).setEnabled(this.mTextView.canCut());
                menu.add((int) TAP_STATE_INITIAL, (int) R.id.copy, (int) MENU_ITEM_ORDER_COPY, (int) R.string.copy).setAlphabeticShortcut('c').setOnMenuItemClickListener(this.mOnContextMenuItemClickListener).setEnabled(this.mTextView.canCopy());
                menu.add((int) TAP_STATE_INITIAL, (int) R.id.paste, (int) MENU_ITEM_ORDER_PASTE, (int) R.string.paste).setAlphabeticShortcut('v').setEnabled(this.mTextView.canPaste()).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener);
                menu.add((int) TAP_STATE_INITIAL, (int) R.id.paste, (int) MENU_ITEM_ORDER_PASTE_AS_PLAIN_TEXT, (int) R.string.paste_as_plain_text).setEnabled(this.mTextView.canPaste()).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener);
                menu.add((int) TAP_STATE_INITIAL, (int) R.id.shareText, (int) MENU_ITEM_ORDER_SHARE, (int) R.string.share).setEnabled(this.mTextView.canShare()).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener);
                menu.add((int) TAP_STATE_INITIAL, (int) R.id.selectAll, (int) TAP_STATE_FIRST_TAP, (int) R.string.selectAll).setAlphabeticShortcut(DateFormat.AM_PM).setEnabled(this.mTextView.canSelectAllText()).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener);
                this.mPreserveSelection = true;
            }
        }
    }

    private SuggestionSpan findEquivalentSuggestionSpan(SuggestionSpanInfo suggestionSpanInfo) {
        Editable editable = (Editable) this.mTextView.getText();
        if (editable.getSpanStart(suggestionSpanInfo.mSuggestionSpan) >= 0) {
            return suggestionSpanInfo.mSuggestionSpan;
        }
        SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) editable.getSpans(suggestionSpanInfo.mSpanStart, suggestionSpanInfo.mSpanEnd, SuggestionSpan.class);
        int length = suggestionSpans.length;
        for (int i = TAP_STATE_INITIAL; i < length; i += TAP_STATE_FIRST_TAP) {
            SuggestionSpan suggestionSpan = suggestionSpans[i];
            if (editable.getSpanStart(suggestionSpan) == suggestionSpanInfo.mSpanStart && editable.getSpanEnd(suggestionSpan) == suggestionSpanInfo.mSpanEnd && suggestionSpan.equals(suggestionSpanInfo.mSuggestionSpan)) {
                return suggestionSpan;
            }
        }
        return null;
    }

    private void replaceWithSuggestion(SuggestionInfo suggestionInfo) {
        SuggestionSpan targetSuggestionSpan = findEquivalentSuggestionSpan(suggestionInfo.mSuggestionSpanInfo);
        if (targetSuggestionSpan != null) {
            Editable editable = (Editable) this.mTextView.getText();
            int spanStart = editable.getSpanStart(targetSuggestionSpan);
            int spanEnd = editable.getSpanEnd(targetSuggestionSpan);
            if (spanStart >= 0 && spanEnd > spanStart) {
                int i;
                String originalText = TextUtils.substring(editable, spanStart, spanEnd);
                SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) editable.getSpans(spanStart, spanEnd, SuggestionSpan.class);
                int length = suggestionSpans.length;
                int[] suggestionSpansStarts = new int[length];
                int[] suggestionSpansEnds = new int[length];
                int[] suggestionSpansFlags = new int[length];
                for (i = TAP_STATE_INITIAL; i < length; i += TAP_STATE_FIRST_TAP) {
                    SuggestionSpan suggestionSpan = suggestionSpans[i];
                    suggestionSpansStarts[i] = editable.getSpanStart(suggestionSpan);
                    suggestionSpansEnds[i] = editable.getSpanEnd(suggestionSpan);
                    suggestionSpansFlags[i] = editable.getSpanFlags(suggestionSpan);
                    int suggestionSpanFlags = suggestionSpan.getFlags();
                    if ((suggestionSpanFlags & TAP_STATE_DOUBLE_TAP) != 0) {
                        suggestionSpan.setFlags((suggestionSpanFlags & -3) & EXTRACT_NOTHING);
                    }
                }
                targetSuggestionSpan.notifySelection(this.mTextView.getContext(), originalText, suggestionInfo.mSuggestionIndex);
                int suggestionStart = suggestionInfo.mSuggestionStart;
                int suggestionEnd = suggestionInfo.mSuggestionEnd;
                String suggestion = suggestionInfo.mText.subSequence(suggestionStart, suggestionEnd).toString();
                this.mTextView.replaceText_internal(spanStart, spanEnd, suggestion);
                targetSuggestionSpan.getSuggestions()[suggestionInfo.mSuggestionIndex] = originalText;
                int lengthDelta = suggestion.length() - (spanEnd - spanStart);
                i = TAP_STATE_INITIAL;
                while (i < length) {
                    if (suggestionSpansStarts[i] <= spanStart && suggestionSpansEnds[i] >= spanEnd) {
                        this.mTextView.setSpan_internal(suggestionSpans[i], suggestionSpansStarts[i], suggestionSpansEnds[i] + lengthDelta, suggestionSpansFlags[i]);
                    }
                    i += TAP_STATE_FIRST_TAP;
                }
                int newCursorPosition = spanEnd + lengthDelta;
                this.mTextView.setCursorPosition_internal(newCursorPosition, newCursorPosition);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getCurrentLineAdjustedForSlop(Layout layout, int prevLine, float y) {
        int trueLine = this.mTextView.getLineAtCoordinate(y);
        if (layout == null || prevLine > layout.getLineCount() || layout.getLineCount() <= 0 || prevLine < 0 || Math.abs(trueLine - prevLine) >= TAP_STATE_DOUBLE_TAP) {
            return trueLine;
        }
        int currLine;
        float verticalOffset = (float) this.mTextView.viewportToContentVerticalOffset();
        int lineCount = layout.getLineCount();
        float slop = ((float) this.mTextView.getLineHeight()) * LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS;
        float yTopBound = Math.max((((float) layout.getLineTop(prevLine)) + verticalOffset) - slop, (((float) layout.getLineTop(TAP_STATE_INITIAL)) + verticalOffset) + slop);
        float yBottomBound = Math.min((((float) layout.getLineBottom(prevLine)) + verticalOffset) + slop, (((float) layout.getLineBottom(lineCount + UNSET_X_VALUE)) + verticalOffset) - slop);
        if (y <= yTopBound) {
            currLine = Math.max(prevLine + UNSET_X_VALUE, TAP_STATE_INITIAL);
        } else if (y >= yBottomBound) {
            currLine = Math.min(prevLine + TAP_STATE_FIRST_TAP, lineCount + UNSET_X_VALUE);
        } else {
            currLine = prevLine;
        }
        return currLine;
    }

    private static boolean isValidRange(CharSequence text, int start, int end) {
        return (start < 0 || start > end || end > text.length()) ? DEBUG_UNDO : true;
    }

    public SuggestionsPopupWindow getSuggestionsPopupWindowForTesting() {
        return this.mSuggestionsPopupWindow;
    }

    protected void selectAllText() {
        if (this.mTextView != null) {
            this.mTextView.selectAllText();
        }
    }

    protected void selectAllAndShowEditor() {
    }

    private int resetOffsetForImageSpan(int offset, boolean isStartHandle) {
        ImageSpan[] imageSpans = (ImageSpan[]) ((Spanned) this.mTextView.getText()).getSpans(offset, offset, ImageSpan.class);
        if (imageSpans.length != TAP_STATE_FIRST_TAP) {
            return offset;
        }
        if (isStartHandle) {
            return ((Spanned) this.mTextView.getText()).getSpanStart(imageSpans[TAP_STATE_INITIAL]);
        }
        return ((Spanned) this.mTextView.getText()).getSpanEnd(imageSpans[TAP_STATE_INITIAL]);
    }

    protected int adjustOffsetAtLineEndForTouchPos(int offset) {
        return offset;
    }

    protected int adjustOffsetAtLineEndForInsertHanlePos(int offset) {
        return offset;
    }

    protected boolean adjustHandlePos(int[] coordinate, HandleView handleView, Layout layout, int offset, int line) {
        return DEBUG_UNDO;
    }

    protected void setPosWithMotionEvent(MotionEvent event, boolean isTouchPos) {
    }

    protected boolean adjustCursorPos(int line, Layout layout) {
        return DEBUG_UNDO;
    }

    protected int getCursorTop() {
        return UNSET_X_VALUE;
    }

    protected int getCursorBottom() {
        return UNSET_X_VALUE;
    }

    protected float getCursorX() {
        return LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
    }

    protected void setPosIsLineEnd(boolean flag) {
    }

    protected void adjustSelectionBounds(RectF selectionBounds, int line, Layout layout, int handleHeight) {
    }

    protected void recogniseLineEnd() {
    }
}
