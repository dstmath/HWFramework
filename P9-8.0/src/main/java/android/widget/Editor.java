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
import android.hdm.HwDeviceManager;
import android.metrics.LogMaker;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable.ClassLoaderCreator;
import android.os.ParcelableParcel;
import android.os.SystemClock;
import android.provider.Settings;
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
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LogException;
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
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.CursorAnchorInfo.Builder;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputMethodManager;
import android.view.textclassifier.TextClassification;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;
import com.android.internal.R;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Editor {
    static final int BLINK = 500;
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_UNDO = false;
    private static final int DRAG_SHADOW_MAX_TEXT_LENGTH = 20;
    static final int EXTRACT_NOTHING = -2;
    static final int EXTRACT_UNKNOWN = -1;
    public static final int HANDLE_TYPE_SELECTION_END = 1;
    public static final int HANDLE_TYPE_SELECTION_START = 0;
    private static final float LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS = 0.5f;
    private static final int MENU_ITEM_ORDER_ASSIST = 2;
    private static final int MENU_ITEM_ORDER_AUTOFILL = 11;
    private static final int MENU_ITEM_ORDER_COPY = 7;
    private static final int MENU_ITEM_ORDER_CUT = 6;
    private static final int MENU_ITEM_ORDER_PASTE = 8;
    private static final int MENU_ITEM_ORDER_PASTE_AS_PLAIN_TEXT = 9;
    private static final int MENU_ITEM_ORDER_PROCESS_TEXT_INTENT_ACTIONS_START = 100;
    private static final int MENU_ITEM_ORDER_REDO = 4;
    private static final int MENU_ITEM_ORDER_REPLACE = 10;
    private static final int MENU_ITEM_ORDER_SELECT_ALL = 1;
    private static final int MENU_ITEM_ORDER_SHARE = 5;
    private static final int MENU_ITEM_ORDER_UNDO = 3;
    private static final int OFFSET_ON_IMAGE_SPAN = 1;
    private static final String TAG = "Editor";
    private static final int TAP_STATE_DOUBLE_TAP = 2;
    private static final int TAP_STATE_FIRST_TAP = 1;
    private static final int TAP_STATE_INITIAL = 0;
    private static final int TAP_STATE_TRIPLE_CLICK = 3;
    private static final String UNDO_OWNER_TAG = "Editor";
    private static final int UNSET_LINE = -1;
    private static final int UNSET_X_VALUE = -1;
    boolean mAllowUndo = true;
    private Blink mBlink;
    private float mContextMenuAnchorX;
    private float mContextMenuAnchorY;
    private CorrectionHighlighter mCorrectionHighlighter;
    boolean mCreatedWithASelection;
    private final CursorAnchorInfoNotifier mCursorAnchorInfoNotifier = new CursorAnchorInfoNotifier(this, null);
    int mCursorCount;
    final Drawable[] mCursorDrawable = new Drawable[2];
    boolean mCursorVisible = true;
    Callback mCustomInsertionActionModeCallback;
    Callback mCustomSelectionActionModeCallback;
    boolean mDiscardNextActionUp;
    CharSequence mError;
    private ErrorPopup mErrorPopup;
    boolean mErrorWasChanged;
    boolean mFrozenWithFocus;
    boolean mIgnoreActionUpEvent;
    boolean mInBatchEditControllers;
    InputContentType mInputContentType;
    InputMethodState mInputMethodState;
    int mInputType = 0;
    private Runnable mInsertionActionModeRunnable;
    private boolean mInsertionControllerEnabled;
    private InsertionPointCursorController mInsertionPointCursorController;
    boolean mIsBeingLongClicked;
    boolean mIsInsertionActionModeStartPending = false;
    KeyListener mKeyListener;
    private int mLastButtonState;
    private float mLastDownPositionX;
    private float mLastDownPositionY;
    private long mLastTouchUpTime = 0;
    private final MetricsLogger mMetricsLogger = new MetricsLogger();
    private final OnMenuItemClickListener mOnContextMenuItemClickListener = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            if (Editor.this.mProcessTextIntentActionsHandler.performMenuItemAction(item)) {
                return true;
            }
            return Editor.this.mTextView.onTextContextMenuItem(item.getItemId());
        }
    };
    private PositionListener mPositionListener;
    private boolean mPreserveSelection;
    final ProcessTextIntentActionsHandler mProcessTextIntentActionsHandler;
    private boolean mRestartActionModeOnNextRefresh;
    boolean mSelectAllOnFocus;
    private Drawable mSelectHandleCenter;
    private Drawable mSelectHandleLeft;
    private Drawable mSelectHandleRight;
    private SelectionActionModeHelper mSelectionActionModeHelper;
    private boolean mSelectionControllerEnabled;
    SelectionModifierCursorController mSelectionModifierCursorController;
    boolean mSelectionMoved;
    long mShowCursor;
    private boolean mShowErrorAfterAttach;
    private final Runnable mShowFloatingToolbar = new Runnable() {
        public void run() {
            if (Editor.this.mTextActionMode != null) {
                Editor.this.mTextActionMode.hide(0);
            }
        }
    };
    boolean mShowSoftInputOnFocus = true;
    private Runnable mShowSuggestionRunnable;
    private SpanController mSpanController;
    SpellChecker mSpellChecker;
    private final SuggestionHelper mSuggestionHelper = new SuggestionHelper(this, null);
    SuggestionRangeSpan mSuggestionRangeSpan;
    private SuggestionsPopupWindow mSuggestionsPopupWindow;
    private int mTapState = 0;
    private Rect mTempRect;
    private ActionMode mTextActionMode;
    private int mTextHeightOffset = 0;
    boolean mTextIsSelectable;
    private TextRenderNode[] mTextRenderNodes;
    protected TextView mTextView;
    boolean mTouchFocusSelected;
    final UndoInputFilter mUndoInputFilter = new UndoInputFilter(this);
    private final UndoManager mUndoManager = new UndoManager();
    private UndoOwner mUndoOwner = this.mUndoManager.getOwner("Editor", this);
    private boolean mUpdateWordIteratorText;
    private WordIterator mWordIterator;
    private WordIterator mWordIteratorWithText;

    private class Blink implements Runnable {
        private boolean mCancelled;

        /* synthetic */ Blink(Editor this$0, Blink -this1) {
            this();
        }

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
            this.mCancelled = false;
        }
    }

    private class CorrectionHighlighter {
        private static final int FADE_OUT_DURATION = 400;
        private int mEnd;
        private long mFadingStartTime;
        private final Paint mPaint = new Paint(1);
        private final Path mPath = new Path();
        private int mStart;
        private RectF mTempRectF;

        public CorrectionHighlighter() {
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
            invalidate(false);
        }

        private boolean updatePaint() {
            long duration = SystemClock.uptimeMillis() - this.mFadingStartTime;
            if (duration > 400) {
                return false;
            }
            this.mPaint.setColor((Editor.this.mTextView.mHighlightColor & 16777215) + (((int) (((float) Color.alpha(Editor.this.mTextView.mHighlightColor)) * (1.0f - (((float) duration) / 400.0f)))) << 24));
            return true;
        }

        private boolean updatePath() {
            Layout layout = Editor.this.mTextView.getLayout();
            if (layout == null) {
                return false;
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
                this.mPath.computeBounds(this.mTempRectF, false);
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

        /* synthetic */ CursorAnchorInfoNotifier(Editor this$0, CursorAnchorInfoNotifier -this1) {
            this();
        }

        private CursorAnchorInfoNotifier() {
            this.mSelectionInfoBuilder = new Builder();
            this.mTmpIntOffset = new int[2];
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
                            Builder builder = this.mSelectionInfoBuilder;
                            builder.reset();
                            int selectionStart = Editor.this.mTextView.getSelectionStart();
                            builder.setSelectionRange(selectionStart, Editor.this.mTextView.getSelectionEnd());
                            this.mViewToScreenMatrix.set(Editor.this.mTextView.getMatrix());
                            Editor.this.mTextView.getLocationOnScreen(this.mTmpIntOffset);
                            this.mViewToScreenMatrix.postTranslate((float) this.mTmpIntOffset[0], (float) this.mTmpIntOffset[1]);
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
                                boolean hasComposingText = composingTextStart >= 0 && composingTextStart < composingTextEnd;
                                if (hasComposingText) {
                                    builder.setComposingText(composingTextStart, text.subSequence(composingTextStart, composingTextEnd));
                                    Editor.this.mTextView.populateCharacterBounds(builder, composingTextStart, composingTextEnd, viewportToContentHorizontalOffset, viewportToContentVerticalOffset);
                                }
                            }
                            if (selectionStart >= 0) {
                                int offset = selectionStart;
                                int line = layout.getLineForOffset(selectionStart);
                                float insertionMarkerX = layout.getPrimaryHorizontal(selectionStart) + viewportToContentHorizontalOffset;
                                float insertionMarkerTop = ((float) layout.getLineTop(line)) + viewportToContentVerticalOffset;
                                float insertionMarkerBaseline = ((float) layout.getLineBaseline(line)) + viewportToContentVerticalOffset;
                                float insertionMarkerBottom = ((float) layout.getLineBottom(line)) + viewportToContentVerticalOffset;
                                boolean isTopVisible = Editor.this.mTextView.isPositionVisible(insertionMarkerX, insertionMarkerTop);
                                boolean isBottomVisible = Editor.this.mTextView.isPositionVisible(insertionMarkerX, insertionMarkerBottom);
                                int insertionMarkerFlags = 0;
                                if (isTopVisible || isBottomVisible) {
                                    insertionMarkerFlags = 1;
                                }
                                if (!(isTopVisible && (isBottomVisible ^ 1) == 0)) {
                                    insertionMarkerFlags |= 2;
                                }
                                if (layout.isRtlCharAt(selectionStart)) {
                                    insertionMarkerFlags |= 4;
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
            this.mPopupWindow.setWindowLayoutType(1005);
            this.mPopupWindow.setWidth(-2);
            this.mPopupWindow.setHeight(-2);
            initContentView();
            this.mContentView.-wrap18(new LayoutParams(-2, -2));
            this.mPopupWindow.setContentView(this.mContentView);
        }

        public void show() {
            Editor.this.getPositionListener().addSubscriber(this, false);
            computeLocalPosition();
            PositionListener positionListener = Editor.this.getPositionListener();
            updatePosition(positionListener.getPositionX(), positionListener.getPositionY());
        }

        protected void measureContent() {
            DisplayMetrics displayMetrics = Editor.this.mTextView.getResources().getDisplayMetrics();
            this.mContentView.measure(MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, Integer.MIN_VALUE));
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
                this.mPopupWindow.update(positionX, positionY, -1, -1);
            } else {
                this.mPopupWindow.showAtLocation(Editor.this.mTextView, 0, positionX, positionY);
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
        private static final int POPUP_TEXT_LAYOUT = 17367286;
        private TextView mDeleteTextView;
        private EasyEditSpan mEasyEditSpan;
        private EasyEditDeleteListener mOnDeleteListener;

        /* synthetic */ EasyEditPopupWindow(Editor this$0, EasyEditPopupWindow -this1) {
            this();
        }

        private EasyEditPopupWindow() {
            super();
        }

        protected void createPopupWindow() {
            this.mPopupWindow = new PopupWindow(Editor.this.mTextView.getContext(), null, (int) R.attr.textSelectHandleWindowStyle);
            this.mPopupWindow.setInputMethodMode(2);
            this.mPopupWindow.setClippingEnabled(true);
        }

        protected void initContentView() {
            LinearLayout linearLayout = new LinearLayout(Editor.this.mTextView.getContext());
            linearLayout.setOrientation(0);
            this.mContentView = linearLayout;
            this.mContentView.setBackgroundResource(R.drawable.text_edit_side_paste_window);
            LayoutInflater inflater = (LayoutInflater) Editor.this.mTextView.getContext().getSystemService("layout_inflater");
            LayoutParams wrapContent = new LayoutParams(-2, -2);
            this.mDeleteTextView = (TextView) inflater.inflate(17367286, null);
            this.mDeleteTextView.-wrap18(wrapContent);
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
                this.mEasyEditSpan.setDeleteEnabled(false);
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
        public static final ClassLoaderCreator<EditOperation> CREATOR = new ClassLoaderCreator<EditOperation>() {
            public EditOperation createFromParcel(Parcel in) {
                return new EditOperation(in, null);
            }

            public EditOperation createFromParcel(Parcel in, ClassLoader loader) {
                return new EditOperation(in, loader);
            }

            public EditOperation[] newArray(int size) {
                return new EditOperation[size];
            }
        };
        private static final int TYPE_DELETE = 1;
        private static final int TYPE_INSERT = 0;
        private static final int TYPE_REPLACE = 2;
        private boolean mFrozen;
        private boolean mIsComposition;
        private int mNewCursorPos;
        private String mNewText;
        private int mOldCursorPos;
        private String mOldText;
        private int mStart;
        private int mType;

        public EditOperation(Editor editor, String oldText, int dstart, String newText, boolean isComposition) {
            super(editor.mUndoOwner);
            this.mOldText = oldText;
            this.mNewText = newText;
            if (this.mNewText.length() > 0 && this.mOldText.length() == 0) {
                this.mType = 0;
            } else if (this.mNewText.length() != 0 || this.mOldText.length() <= 0) {
                this.mType = 2;
            } else {
                this.mType = 1;
            }
            this.mStart = dstart;
            this.mOldCursorPos = editor.mTextView.getSelectionStart();
            this.mNewCursorPos = this.mNewText.length() + dstart;
            this.mIsComposition = isComposition;
        }

        public EditOperation(Parcel src, ClassLoader loader) {
            boolean z;
            boolean z2 = true;
            super(src, loader);
            this.mType = src.readInt();
            this.mOldText = src.readString();
            this.mNewText = src.readString();
            this.mStart = src.readInt();
            this.mOldCursorPos = src.readInt();
            this.mNewCursorPos = src.readInt();
            if (src.readInt() == 1) {
                z = true;
            } else {
                z = false;
            }
            this.mFrozen = z;
            if (src.readInt() != 1) {
                z2 = false;
            }
            this.mIsComposition = z2;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = 1;
            dest.writeInt(this.mType);
            dest.writeString(this.mOldText);
            dest.writeString(this.mNewText);
            dest.writeInt(this.mStart);
            dest.writeInt(this.mOldCursorPos);
            dest.writeInt(this.mNewCursorPos);
            if (this.mFrozen) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            if (!this.mIsComposition) {
                i2 = 0;
            }
            dest.writeInt(i2);
        }

        private int getNewTextEnd() {
            return this.mStart + this.mNewText.length();
        }

        private int getOldTextEnd() {
            return this.mStart + this.mOldText.length();
        }

        public void commit() {
        }

        public void undo() {
            modifyText((Editable) ((Editor) getOwnerData()).mTextView.getText(), this.mStart, getNewTextEnd(), this.mOldText, this.mStart, this.mOldCursorPos);
        }

        public void redo() {
            modifyText((Editable) ((Editor) getOwnerData()).mTextView.getText(), this.mStart, getOldTextEnd(), this.mNewText, this.mStart, this.mNewCursorPos);
        }

        private boolean mergeWith(EditOperation edit) {
            if (this.mFrozen) {
                return false;
            }
            switch (this.mType) {
                case 0:
                    return mergeInsertWith(edit);
                case 1:
                    return mergeDeleteWith(edit);
                case 2:
                    return mergeReplaceWith(edit);
                default:
                    return false;
            }
        }

        private boolean mergeInsertWith(EditOperation edit) {
            if (edit.mType == 0) {
                if (getNewTextEnd() != edit.mStart) {
                    return false;
                }
                this.mNewText += edit.mNewText;
                this.mNewCursorPos = edit.mNewCursorPos;
                this.mFrozen = edit.mFrozen;
                this.mIsComposition = edit.mIsComposition;
                return true;
            } else if (!this.mIsComposition || edit.mType != 2 || this.mStart > edit.mStart || getNewTextEnd() < edit.getOldTextEnd()) {
                return false;
            } else {
                this.mNewText = this.mNewText.substring(0, edit.mStart - this.mStart) + edit.mNewText + this.mNewText.substring(edit.getOldTextEnd() - this.mStart, this.mNewText.length());
                this.mNewCursorPos = edit.mNewCursorPos;
                this.mIsComposition = edit.mIsComposition;
                return true;
            }
        }

        private boolean mergeDeleteWith(EditOperation edit) {
            if (edit.mType != 1 || this.mStart != edit.getOldTextEnd()) {
                return false;
            }
            this.mStart = edit.mStart;
            this.mOldText = edit.mOldText + this.mOldText;
            this.mNewCursorPos = edit.mNewCursorPos;
            this.mIsComposition = edit.mIsComposition;
            return true;
        }

        private boolean mergeReplaceWith(EditOperation edit) {
            if (edit.mType == 0 && getNewTextEnd() == edit.mStart) {
                this.mNewText += edit.mNewText;
                this.mNewCursorPos = edit.mNewCursorPos;
                return true;
            } else if (!this.mIsComposition) {
                return false;
            } else {
                if (edit.mType == 1 && this.mStart <= edit.mStart && getNewTextEnd() >= edit.getOldTextEnd()) {
                    this.mNewText = this.mNewText.substring(0, edit.mStart - this.mStart) + this.mNewText.substring(edit.getOldTextEnd() - this.mStart, this.mNewText.length());
                    if (this.mNewText.isEmpty()) {
                        this.mType = 1;
                    }
                    this.mNewCursorPos = edit.mNewCursorPos;
                    this.mIsComposition = edit.mIsComposition;
                    return true;
                } else if (edit.mType != 2 || this.mStart != edit.mStart || !TextUtils.equals(this.mNewText, edit.mOldText)) {
                    return false;
                } else {
                    this.mNewText = edit.mNewText;
                    this.mNewCursorPos = edit.mNewCursorPos;
                    this.mIsComposition = edit.mIsComposition;
                    return true;
                }
            }
        }

        public void forceMergeWith(EditOperation edit) {
            if (!mergeWith(edit)) {
                Editable editable = (Editable) ((Editor) getOwnerData()).mTextView.getText();
                Editable originalText = new SpannableStringBuilder(editable.toString());
                modifyText(originalText, this.mStart, getNewTextEnd(), this.mOldText, this.mStart, this.mOldCursorPos);
                Editable finalText = new SpannableStringBuilder(editable.toString());
                modifyText(finalText, edit.mStart, edit.getOldTextEnd(), edit.mNewText, edit.mStart, edit.mNewCursorPos);
                this.mType = 2;
                this.mNewText = finalText.toString();
                this.mOldText = originalText.toString();
                this.mStart = 0;
                this.mNewCursorPos = edit.mNewCursorPos;
                this.mIsComposition = edit.mIsComposition;
            }
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
                case 0:
                    return "insert";
                case 1:
                    return "delete";
                case 2:
                    return "replace";
                default:
                    return LogException.NO_VALUE;
            }
        }

        public String toString() {
            return "[mType=" + getTypeString() + ", " + "mOldText=" + this.mOldText + ", " + "mNewText=" + this.mNewText + ", " + "mStart=" + this.mStart + ", " + "mOldCursorPos=" + this.mOldCursorPos + ", " + "mNewCursorPos=" + this.mNewCursorPos + ", " + "mFrozen=" + this.mFrozen + ", " + "mIsComposition=" + this.mIsComposition + "]";
        }
    }

    private static class ErrorPopup extends PopupWindow {
        private boolean mAbove = false;
        private int mPopupInlineErrorAboveBackgroundId = 0;
        private int mPopupInlineErrorBackgroundId = 0;
        private final TextView mView;

        ErrorPopup(TextView v, int width, int height) {
            super((View) v, width, height);
            this.mView = v;
            this.mPopupInlineErrorBackgroundId = getResourceId(this.mPopupInlineErrorBackgroundId, 289);
            this.mView.setBackgroundResource(this.mPopupInlineErrorBackgroundId);
        }

        void fixDirection(boolean above) {
            this.mAbove = above;
            if (above) {
                this.mPopupInlineErrorAboveBackgroundId = getResourceId(this.mPopupInlineErrorAboveBackgroundId, 288);
            } else {
                this.mPopupInlineErrorBackgroundId = getResourceId(this.mPopupInlineErrorBackgroundId, 289);
            }
            this.mView.setBackgroundResource(above ? this.mPopupInlineErrorAboveBackgroundId : this.mPopupInlineErrorBackgroundId);
        }

        private int getResourceId(int currentId, int index) {
            if (currentId != 0) {
                return currentId;
            }
            TypedArray styledAttributes = this.mView.getContext().obtainStyledAttributes(android.R.styleable.Theme);
            currentId = styledAttributes.getResourceId(index, 0);
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

        /* synthetic */ HandleView(Editor this$0, Drawable drawableLtr, Drawable drawableRtl, int id, HandleView -this4) {
            this(drawableLtr, drawableRtl, id);
        }

        public abstract int getCurrentCursorOffset();

        protected abstract int getHorizontalGravity(boolean z);

        protected abstract int getHotspotX(Drawable drawable, boolean z);

        public abstract void updatePosition(float f, float f2);

        protected abstract void updateSelection(int i);

        private HandleView(Drawable drawableLtr, Drawable drawableRtl, int id) {
            super(Editor.this.mTextView.getContext());
            this.mPreviousOffset = -1;
            this.mPositionHasChanged = true;
            this.mPrevLine = -1;
            this.mPreviousLineTouched = -1;
            this.mPreviousOffsetsTimes = new long[5];
            this.mPreviousOffsets = new int[5];
            this.mPreviousOffsetIndex = 0;
            this.mNumberPreviousOffsets = 0;
            setId(id);
            this.mContainer = new PopupWindow(Editor.this.mTextView.getContext(), null, (int) R.attr.textSelectHandleWindowStyle);
            this.mContainer.setSplitTouchEnabled(true);
            this.mContainer.setClippingEnabled(false);
            this.mContainer.setWindowLayoutType(1002);
            this.mContainer.setWidth(-2);
            this.mContainer.setHeight(-2);
            this.mContainer.setContentView(this);
            this.mDrawableLtr = drawableLtr;
            this.mDrawableRtl = drawableRtl;
            this.mMinSize = Editor.this.mTextView.getContext().getResources().getDimensionPixelSize(R.dimen.text_handle_min_size);
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
                Layout layout = Editor.this.mTextView.getLayout();
                if (layout != null) {
                    int offset = getCurrentCursorOffset();
                    boolean isRtlCharAtOffset = isAtRtlRun(layout, offset);
                    Drawable oldDrawable = this.mDrawable;
                    this.mDrawable = isRtlCharAtOffset ? this.mDrawableRtl : this.mDrawableLtr;
                    this.mHotspotX = getHotspotX(this.mDrawable, isRtlCharAtOffset);
                    this.mHorizontalGravity = getHorizontalGravity(isRtlCharAtOffset);
                    if (oldDrawable != this.mDrawable && isShowing()) {
                        this.mPositionX = ((getCursorHorizontalPosition(layout, offset) - this.mHotspotX) - getHorizontalOffset()) + getCursorOffset();
                        this.mPositionX += Editor.this.mTextView.viewportToContentHorizontalOffset();
                        this.mPositionHasChanged = true;
                        updatePosition(this.mLastParentX, this.mLastParentY, false, false);
                        postInvalidate();
                    }
                }
            }
        }

        private void startTouchUpFilter(int offset) {
            this.mNumberPreviousOffsets = 0;
            addPositionToTouchUpFilter(offset);
        }

        private void addPositionToTouchUpFilter(int offset) {
            this.mPreviousOffsetIndex = (this.mPreviousOffsetIndex + 1) % 5;
            this.mPreviousOffsets[this.mPreviousOffsetIndex] = offset;
            this.mPreviousOffsetsTimes[this.mPreviousOffsetIndex] = SystemClock.uptimeMillis();
            this.mNumberPreviousOffsets++;
        }

        private void filterOnTouchUp() {
            long now = SystemClock.uptimeMillis();
            int i = 0;
            int index = this.mPreviousOffsetIndex;
            int iMax = Math.min(this.mNumberPreviousOffsets, 5);
            while (i < iMax && now - this.mPreviousOffsetsTimes[index] < 150) {
                i++;
                index = ((this.mPreviousOffsetIndex - i) + 5) % 5;
            }
            if (i > 0 && i < iMax && now - this.mPreviousOffsetsTimes[index] > 350) {
                positionAtCursorOffset(this.mPreviousOffsets[index], false);
            }
        }

        public boolean offsetHasBeenChanged() {
            return this.mNumberPreviousOffsets > 1;
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            -wrap6(getPreferredWidth(), getPreferredHeight());
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
                Editor.this.getPositionListener().addSubscriber(this, true);
                this.mPreviousOffset = -1;
                positionAtCursorOffset(getCurrentCursorOffset(), false);
            }
        }

        protected void dismiss() {
            this.mIsDragging = false;
            this.mContainer.dismiss();
            onDetached();
        }

        public void hide() {
            dismiss();
            Editor.this.getPositionListener().removeSubscriber(this);
        }

        public boolean isShowing() {
            return this.mContainer.isShowing();
        }

        private boolean isVisible() {
            if (this.mIsDragging) {
                return true;
            }
            if (Editor.this.mTextView.isInBatchEditMode()) {
                return false;
            }
            return Editor.this.mTextView.isPositionVisible((float) ((this.mPositionX + this.mHotspotX) + getHorizontalOffset()), (float) this.mPositionY);
        }

        protected boolean isAtRtlRun(Layout layout, int offset) {
            return layout.isRtlCharAt(offset);
        }

        public float getHorizontal(Layout layout, int offset) {
            return layout.getPrimaryHorizontal(offset);
        }

        protected int getOffsetAtCoordinate(Layout layout, int line, float x) {
            return Editor.this.mTextView.getOffsetAtCoordinate(line, x);
        }

        protected void positionAtCursorOffset(int offset, boolean forceUpdatePosition) {
            if (Editor.this.mTextView.getLayout() == null) {
                Editor.this.prepareCursorControllers();
                return;
            }
            Layout layout = Editor.this.mTextView.getLayout();
            boolean offsetChanged = offset != this.mPreviousOffset;
            if (offsetChanged || forceUpdatePosition) {
                if (offsetChanged) {
                    updateSelection(offset);
                    addPositionToTouchUpFilter(offset);
                }
                int line = layout.getLineForOffset(offset);
                this.mPrevLine = line;
                this.mPositionX = ((getCursorHorizontalPosition(layout, offset) - this.mHotspotX) - getHorizontalOffset()) + getCursorOffset();
                this.mPositionY = layout.getLineBottom(line);
                int[] coordinate = new int[]{this.mPositionX, this.mPositionY};
                if (Editor.this.adjustHandlePos(coordinate, this, layout, offset, line)) {
                    this.mPositionX = coordinate[0];
                    this.mPositionY = coordinate[1];
                }
                this.mPositionX += Editor.this.mTextView.viewportToContentHorizontalOffset();
                this.mPositionY += Editor.this.mTextView.viewportToContentVerticalOffset();
                this.mPreviousOffset = offset;
                this.mPositionHasChanged = true;
            }
        }

        int getCursorHorizontalPosition(Layout layout, int offset) {
            if (Editor.this.mTextView == null || Editor.this.mTextView.getPaddingStart() != 0) {
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
                    int[] pts = new int[]{(this.mPositionX + this.mHotspotX) + getHorizontalOffset(), this.mPositionY};
                    Editor.this.mTextView.transformFromViewToWindowSpace(pts);
                    pts[0] = pts[0] - (this.mHotspotX + getHorizontalOffset());
                    if (isShowing()) {
                        this.mContainer.update(pts[0], pts[1], -1, -1);
                    } else {
                        this.mContainer.showAtLocation(Editor.this.mTextView, 0, pts[0], pts[1]);
                    }
                } else if (isShowing()) {
                    Log.d("Editor", "HandleView is showing but not visible, so dismiss it.");
                    dismiss();
                }
                this.mPositionHasChanged = false;
            }
        }

        protected void onDraw(Canvas c) {
            int drawWidth = this.mDrawable.getIntrinsicWidth();
            int left = getHorizontalOffset();
            this.mDrawable.setBounds(left, 0, left + drawWidth, this.mDrawable.getIntrinsicHeight());
            this.mDrawable.draw(c);
        }

        private int getHorizontalOffset() {
            int width = getPreferredWidth();
            int drawWidth = this.mDrawable.getIntrinsicWidth();
            switch (this.mHorizontalGravity) {
                case 3:
                    return 0;
                case 5:
                    return width - drawWidth;
                default:
                    return (width - drawWidth) / 2;
            }
        }

        protected int getCursorOffset() {
            return 0;
        }

        public boolean onTouchEvent(MotionEvent ev) {
            Editor.this.updateFloatingToolbarVisibility(ev);
            float yInWindow;
            switch (ev.getActionMasked()) {
                case 0:
                    startTouchUpFilter(getCurrentCursorOffset());
                    PositionListener positionListener = Editor.this.getPositionListener();
                    this.mLastParentX = positionListener.getPositionX();
                    this.mLastParentY = positionListener.getPositionY();
                    this.mLastParentXOnScreen = positionListener.getPositionXOnScreen();
                    this.mLastParentYOnScreen = positionListener.getPositionYOnScreen();
                    yInWindow = (ev.getRawY() - ((float) this.mLastParentYOnScreen)) + ((float) this.mLastParentY);
                    this.mTouchToWindowOffsetX = ((ev.getRawX() - ((float) this.mLastParentXOnScreen)) + ((float) this.mLastParentX)) - ((float) this.mPositionX);
                    this.mTouchToWindowOffsetY = yInWindow - ((float) this.mPositionY);
                    this.mIsDragging = true;
                    this.mPreviousLineTouched = -1;
                    break;
                case 1:
                    filterOnTouchUp();
                    this.mIsDragging = false;
                    updateDrawable();
                    break;
                case 2:
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
                case 3:
                    this.mIsDragging = false;
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
        int imeOptions = 0;
        OnEditorActionListener onEditorActionListener;
        String privateImeOptions;

        InputContentType() {
        }
    }

    static class InputMethodState {
        int mBatchEditNesting;
        int mChangedDelta;
        int mChangedEnd;
        int mChangedStart;
        boolean mContentChanged;
        boolean mCursorChanged;
        final ExtractedText mExtractedText = new ExtractedText();
        ExtractedTextRequest mExtractedTextRequest;
        boolean mSelectionModeChanged;

        InputMethodState() {
        }
    }

    protected class InsertionHandleView extends HandleView {
        private static final int DELAY_BEFORE_HANDLE_FADES_OUT = 4000;
        private static final int RECENT_CUT_COPY_DURATION = 15000;
        private float mDownPositionX;
        private float mDownPositionY;
        private Runnable mHider;

        public InsertionHandleView(Drawable drawable) {
            super(Editor.this, drawable, drawable, R.id.insertion_handle, null);
        }

        public void show() {
            super.show();
            long durationSinceCutOrCopy = SystemClock.uptimeMillis() - TextView.sLastCutCopyOrTextChangedTime;
            if (Editor.this.mInsertionActionModeRunnable != null && (Editor.this.mTapState == 2 || Editor.this.mTapState == 3 || Editor.this.isCursorInsideEasyCorrectionSpan())) {
                Editor.this.mTextView.removeCallbacks(Editor.this.mInsertionActionModeRunnable);
            }
            if (!(Editor.this.mTapState == 2 || Editor.this.mTapState == 3 || (Editor.this.isCursorInsideEasyCorrectionSpan() ^ 1) == 0 || durationSinceCutOrCopy >= 15000 || Editor.this.mTextActionMode != null)) {
                if (Editor.this.mInsertionActionModeRunnable == null) {
                    Editor.this.mInsertionActionModeRunnable = new Runnable() {
                        public void run() {
                            Editor.this.startInsertionActionMode();
                        }
                    };
                }
                Editor.this.mTextView.postDelayed(Editor.this.mInsertionActionModeRunnable, 0);
            }
            hideAfterDelay();
        }

        private void hideAfterDelay() {
            if (this.mHider == null) {
                this.mHider = new Runnable() {
                    public void run() {
                        InsertionHandleView.this.hide();
                    }
                };
            } else {
                removeHiderCallback();
            }
            Editor.this.mTextView.postDelayed(this.mHider, 4000);
        }

        private void removeHiderCallback() {
            if (this.mHider != null) {
                Editor.this.mTextView.removeCallbacks(this.mHider);
            }
        }

        protected int getHotspotX(Drawable drawable, boolean isRtlRun) {
            return drawable.getIntrinsicWidth() / 2;
        }

        protected int getHorizontalGravity(boolean isRtlRun) {
            return 1;
        }

        protected int getCursorOffset() {
            int offset = super.getCursorOffset();
            Drawable cursor = Editor.this.mCursorCount > 0 ? Editor.this.mCursorDrawable[0] : null;
            if (cursor == null) {
                return offset;
            }
            cursor.getPadding(Editor.this.mTempRect);
            return offset + (((cursor.getIntrinsicWidth() - Editor.this.mTempRect.left) - Editor.this.mTempRect.right) / 2);
        }

        int getCursorHorizontalPosition(Layout layout, int offset) {
            Drawable drawable = Editor.this.mCursorCount > 0 ? Editor.this.mCursorDrawable[0] : null;
            if (drawable == null) {
                return super.getCursorHorizontalPosition(layout, offset);
            }
            return Editor.this.clampHorizontalPosition(drawable, getHorizontal(layout, offset)) + Editor.this.mTempRect.left;
        }

        public boolean onTouchEvent(MotionEvent ev) {
            boolean result = super.onTouchEvent(ev);
            Editor.this.setPosWithMotionEvent(ev, false);
            switch (ev.getActionMasked()) {
                case 0:
                    this.mDownPositionX = ev.getRawX();
                    this.mDownPositionY = ev.getRawY();
                    break;
                case 1:
                    if (!offsetHasBeenChanged()) {
                        float deltaX = this.mDownPositionX - ev.getRawX();
                        float deltaY = this.mDownPositionY - ev.getRawY();
                        float distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);
                        int touchSlop = ViewConfiguration.get(Editor.this.mTextView.getContext()).getScaledTouchSlop();
                        if (distanceSquared < ((float) (touchSlop * touchSlop))) {
                            if (Editor.this.mTextActionMode != null) {
                                Editor.this.stopTextActionMode();
                            } else {
                                Editor.this.startInsertionActionMode();
                            }
                        }
                    } else if (Editor.this.mTextActionMode != null) {
                        Editor.this.mTextActionMode.invalidateContentRect();
                    }
                    hideAfterDelay();
                    break;
                case 3:
                    hideAfterDelay();
                    break;
            }
            return result;
        }

        public int getCurrentCursorOffset() {
            Editor.this.recogniseLineEnd();
            return Editor.this.mTextView.getSelectionStart();
        }

        public void updateSelection(int offset) {
            Selection.setSelection((Spannable) Editor.this.mTextView.getText(), offset);
        }

        public void updatePosition(float x, float y) {
            int offset;
            Layout layout = Editor.this.mTextView.getLayout();
            if (layout != null) {
                if (this.mPreviousLineTouched == -1) {
                    this.mPreviousLineTouched = Editor.this.mTextView.getLineAtCoordinate(y);
                }
                int currLine = Editor.this.getCurrentLineAdjustedForSlop(layout, this.mPreviousLineTouched, y);
                offset = getOffsetAtCoordinate(layout, currLine, x);
                this.mPreviousLineTouched = currLine;
                offset = Editor.this.adjustOffsetAtLineEndForInsertHanlePos(offset);
            } else {
                offset = -1;
            }
            positionAtCursorOffset(offset, false);
            if (Editor.this.mTextActionMode != null) {
                Editor.this.invalidateActionMode();
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

        /* synthetic */ InsertionPointCursorController(Editor this$0, InsertionPointCursorController -this1) {
            this();
        }

        private InsertionPointCursorController() {
        }

        public void show() {
            getHandle().show();
            if (Editor.this.mSelectionModifierCursorController != null) {
                Editor.this.mSelectionModifierCursorController.hide();
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
            if (Editor.this.mSelectHandleCenter == null) {
                Editor.this.mSelectHandleCenter = Editor.this.mTextView.getContext().getDrawable(Editor.this.mTextView.mTextSelectHandleRes);
            }
            if (this.mHandle == null) {
                this.mHandle = new InsertionHandleView(Editor.this.mSelectHandleCenter);
            }
            return this.mHandle;
        }

        public void onDetached() {
            Editor.this.mTextView.getViewTreeObserver().removeOnTouchModeChangeListener(this);
            if (this.mHandle != null) {
                this.mHandle.onDetached();
            }
        }

        public boolean isCursorBeingModified() {
            return this.mHandle != null ? this.mHandle.isDragging() : false;
        }

        public boolean isActive() {
            return this.mHandle != null ? this.mHandle.isShowing() : false;
        }

        public void invalidateHandle() {
            if (this.mHandle != null) {
                this.mHandle.invalidate();
            }
        }
    }

    private class PositionListener implements OnPreDrawListener {
        private static final int MAXIMUM_NUMBER_OF_LISTENERS = 7;
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

        /* synthetic */ PositionListener(Editor this$0, PositionListener -this1) {
            this();
        }

        private PositionListener() {
            this.mPositionListeners = new TextViewPositionListener[7];
            this.mCanMove = new boolean[7];
            this.mPositionHasChanged = true;
            this.mTempCoords = new int[2];
        }

        public void addSubscriber(TextViewPositionListener positionListener, boolean canMove) {
            if (this.mNumberOfListeners == 0) {
                updatePosition();
                Editor.this.mTextView.getViewTreeObserver().addOnPreDrawListener(this);
            }
            int emptySlotIndex = -1;
            int i = 0;
            while (i < 7) {
                TextViewPositionListener listener = this.mPositionListeners[i];
                if (listener != positionListener) {
                    if (emptySlotIndex < 0 && listener == null) {
                        emptySlotIndex = i;
                    }
                    i++;
                } else {
                    return;
                }
            }
            this.mPositionListeners[emptySlotIndex] = positionListener;
            this.mCanMove[emptySlotIndex] = canMove;
            this.mNumberOfListeners++;
        }

        public void removeSubscriber(TextViewPositionListener positionListener) {
            for (int i = 0; i < 7; i++) {
                if (this.mPositionListeners[i] == positionListener) {
                    this.mPositionListeners[i] = null;
                    this.mNumberOfListeners--;
                    break;
                }
            }
            if (this.mNumberOfListeners == 0) {
                Editor.this.mTextView.getViewTreeObserver().removeOnPreDrawListener(this);
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
            boolean hasSelection = Editor.this.mTextView != null ? Editor.this.mTextView.hasSelection() : false;
            SelectionModifierCursorController selectionController = Editor.this.getSelectionController();
            boolean forceUpdate = (!hasSelection || selectionController == null) ? false : selectionController.isActive() ^ 1;
            int i = 0;
            while (i < 7) {
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
                i++;
            }
            this.mScrollHasChanged = false;
            return true;
        }

        private void updatePosition() {
            Editor.this.mTextView.getLocationInWindow(this.mTempCoords);
            boolean z = (this.mTempCoords[0] == this.mPositionX && this.mTempCoords[1] == this.mPositionY) ? false : true;
            this.mPositionHasChanged = z;
            this.mPositionX = this.mTempCoords[0];
            this.mPositionY = this.mTempCoords[1];
            Editor.this.mTextView.getLocationOnScreen(this.mTempCoords);
            this.mPositionXOnScreen = this.mTempCoords[0];
            this.mPositionYOnScreen = this.mTempCoords[1];
        }

        public void onScrollChanged() {
            this.mScrollHasChanged = true;
        }
    }

    static final class ProcessTextIntentActionsHandler {
        private final SparseArray<AccessibilityAction> mAccessibilityActions;
        private final SparseArray<Intent> mAccessibilityIntents;
        private final Context mContext;
        private final Editor mEditor;
        private final PackageManager mPackageManager;
        private final String mPackageName;
        private final List<ResolveInfo> mSupportedActivities;
        private final TextView mTextView;

        /* synthetic */ ProcessTextIntentActionsHandler(Editor editor, ProcessTextIntentActionsHandler -this1) {
            this(editor);
        }

        private ProcessTextIntentActionsHandler(Editor editor) {
            this.mAccessibilityIntents = new SparseArray();
            this.mAccessibilityActions = new SparseArray();
            this.mSupportedActivities = new ArrayList();
            this.mEditor = (Editor) Preconditions.checkNotNull(editor);
            this.mTextView = (TextView) Preconditions.checkNotNull(this.mEditor.mTextView);
            this.mContext = (Context) Preconditions.checkNotNull(this.mTextView.getContext());
            this.mPackageManager = (PackageManager) Preconditions.checkNotNull(this.mContext.getPackageManager());
            this.mPackageName = (String) Preconditions.checkNotNull(this.mContext.getPackageName());
        }

        public void onInitializeMenu(Menu menu) {
            loadSupportedActivities();
            int size = this.mSupportedActivities.size();
            int i = 0;
            while (i < size) {
                ResolveInfo resolveInfo = (ResolveInfo) this.mSupportedActivities.get(i);
                int i2 = i + 1;
                menu.add(0, 0, i + 100, getLabel(resolveInfo)).setIntent(createProcessTextIntentForResolveInfo(resolveInfo)).setShowAsAction(1);
                i = i2 + 1;
            }
        }

        public boolean performMenuItemAction(MenuItem item) {
            return fireIntent(item.getIntent());
        }

        public void initializeAccessibilityActions() {
            this.mAccessibilityIntents.clear();
            this.mAccessibilityActions.clear();
            int i = 0;
            loadSupportedActivities();
            for (ResolveInfo resolveInfo : this.mSupportedActivities) {
                int i2 = i + 1;
                int actionId = 268435712 + i;
                this.mAccessibilityActions.put(actionId, new AccessibilityAction(actionId, getLabel(resolveInfo)));
                this.mAccessibilityIntents.put(actionId, createProcessTextIntentForResolveInfo(resolveInfo));
                i = i2;
            }
        }

        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo nodeInfo) {
            for (int i = 0; i < this.mAccessibilityActions.size(); i++) {
                nodeInfo.addAction((AccessibilityAction) this.mAccessibilityActions.valueAt(i));
            }
        }

        public boolean performAccessibilityAction(int actionId) {
            return fireIntent((Intent) this.mAccessibilityIntents.get(actionId));
        }

        private boolean fireIntent(Intent intent) {
            if (intent == null || !"android.intent.action.PROCESS_TEXT".equals(intent.getAction())) {
                return false;
            }
            intent.putExtra("android.intent.extra.PROCESS_TEXT", this.mTextView.getSelectedText());
            this.mEditor.mPreserveSelection = true;
            this.mTextView.-wrap19(intent, 100);
            return true;
        }

        private void loadSupportedActivities() {
            this.mSupportedActivities.clear();
            for (ResolveInfo info : this.mTextView.getContext().getPackageManager().queryIntentActivities(createProcessTextIntent(), 0)) {
                if (isSupportedActivity(info)) {
                    this.mSupportedActivities.add(info);
                }
            }
        }

        private boolean isSupportedActivity(ResolveInfo info) {
            if (this.mPackageName.equals(info.activityInfo.packageName)) {
                return true;
            }
            if (info.activityInfo.exported) {
                return info.activityInfo.permission == null || this.mContext.checkSelfPermission(info.activityInfo.permission) == 0;
            } else {
                return false;
            }
        }

        private Intent createProcessTextIntentForResolveInfo(ResolveInfo info) {
            return createProcessTextIntent().putExtra("android.intent.extra.PROCESS_TEXT_READONLY", this.mTextView.isTextEditable() ^ 1).setClassName(info.activityInfo.packageName, info.activityInfo.name);
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
        private boolean mInWord = false;
        private boolean mLanguageDirectionChanged = false;
        private float mPrevX;
        private final float mTextViewEdgeSlop;
        private final int[] mTextViewLocation = new int[2];
        private float mTouchWordDelta;

        public SelectionHandleView(Drawable drawableLtr, Drawable drawableRtl, int id, int handleType) {
            super(Editor.this, drawableLtr, drawableRtl, id, null);
            this.mHandleType = handleType;
            this.mTextViewEdgeSlop = (float) (ViewConfiguration.get(Editor.this.mTextView.getContext()).getScaledTouchSlop() * 4);
        }

        public boolean isStartHandle() {
            return this.mHandleType == 0;
        }

        protected int getHotspotX(Drawable drawable, boolean isRtlRun) {
            if (isRtlRun == isStartHandle()) {
                return drawable.getIntrinsicWidth() / 4;
            }
            return (drawable.getIntrinsicWidth() * 3) / 4;
        }

        protected int getHorizontalGravity(boolean isRtlRun) {
            return isRtlRun == isStartHandle() ? 3 : 5;
        }

        public int getCurrentCursorOffset() {
            if (!isStartHandle()) {
                Editor.this.recogniseLineEnd();
            }
            return isStartHandle() ? Editor.this.mTextView.getSelectionStart() : Editor.this.mTextView.getSelectionEnd();
        }

        protected void updateSelection(int offset) {
            if (isStartHandle()) {
                Selection.setSelection((Spannable) Editor.this.mTextView.getText(), offset, Editor.this.mTextView.getSelectionEnd());
            } else {
                Selection.setSelection((Spannable) Editor.this.mTextView.getText(), Editor.this.mTextView.getSelectionStart(), offset);
            }
            updateDrawable();
            if (Editor.this.mTextActionMode != null) {
                Editor.this.invalidateActionMode();
            }
        }

        /* JADX WARNING: Missing block: B:83:0x0243, code:
            if (r27.this$0.mTextView.canScrollHorizontally(r6 ? -1 : 1) == false) goto L_0x0245;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void updatePosition(float x, float y) {
            Layout layout = Editor.this.mTextView.getLayout();
            if (!isStartHandle()) {
                Editor.this.setPosIsLineEnd(false);
            }
            if (layout == null) {
                positionAndAdjustForCrossingHandles(Editor.this.mTextView.getOffsetForPosition(x, y));
                return;
            }
            if (this.mPreviousLineTouched == -1) {
                this.mPreviousLineTouched = Editor.this.mTextView.getLineAtCoordinate(y);
            }
            boolean positionCursor = false;
            int anotherHandleOffset = isStartHandle() ? Editor.this.mTextView.getSelectionEnd() : Editor.this.mTextView.getSelectionStart();
            int currLine = Editor.this.getCurrentLineAdjustedForSlop(layout, this.mPreviousLineTouched, y);
            int initialOffset = getOffsetAtCoordinate(layout, currLine, x);
            if ((isStartHandle() && initialOffset >= anotherHandleOffset) || (!isStartHandle() && initialOffset <= anotherHandleOffset)) {
                currLine = layout.getLineForOffset(anotherHandleOffset);
                initialOffset = getOffsetAtCoordinate(layout, currLine, x);
            }
            int offset = initialOffset;
            int wordEnd = Editor.this.getWordEnd(offset);
            int wordStart = Editor.this.getWordStart(offset);
            if (this.mPrevX == -1.0f) {
                this.mPrevX = x;
            }
            int currentOffset = getCurrentCursorOffset();
            boolean rtlAtCurrentOffset = isAtRtlRun(layout, currentOffset);
            boolean atRtl = isAtRtlRun(layout, offset);
            boolean isLvlBoundary = layout.isLevelBoundary(offset);
            if (isLvlBoundary || ((rtlAtCurrentOffset && (atRtl ^ 1) != 0) || (!rtlAtCurrentOffset && atRtl))) {
                this.mLanguageDirectionChanged = true;
                this.mTouchWordDelta = 0.0f;
                positionAndAdjustForCrossingHandles(offset);
            } else if (!this.mLanguageDirectionChanged || (isLvlBoundary ^ 1) == 0) {
                float xDiff = x - this.mPrevX;
                boolean isExpanding = isStartHandle() ? currLine < this.mPreviousLineTouched : currLine > this.mPreviousLineTouched;
                if (atRtl == isStartHandle()) {
                    isExpanding |= xDiff > 0.0f ? 1 : 0;
                } else {
                    isExpanding |= xDiff < 0.0f ? 1 : 0;
                }
                float touchXOnScreen = ((this.mTouchToWindowOffsetX + x) - ((float) this.mLastParentX)) + ((float) this.mLastParentXOnScreen);
                if (Editor.this.mTextView.getHorizontallyScrolling() && positionNearEdgeOfScrollingView(touchXOnScreen, atRtl)) {
                    if (!isStartHandle() || Editor.this.mTextView.getScrollX() == 0) {
                        if (!isStartHandle()) {
                        }
                    }
                    if (isExpanding && ((isStartHandle() && offset < currentOffset) || (!isStartHandle() && offset > currentOffset))) {
                        int nextOffset;
                        this.mTouchWordDelta = 0.0f;
                        if (atRtl == isStartHandle()) {
                            nextOffset = layout.getOffsetToRightOf(this.mPreviousOffset);
                        } else {
                            nextOffset = layout.getOffsetToLeftOf(this.mPreviousOffset);
                        }
                        positionAndAdjustForCrossingHandles(nextOffset);
                        return;
                    }
                }
                if (isExpanding) {
                    int wordBoundary = isStartHandle() ? wordStart : wordEnd;
                    boolean snapToWord = (!this.mInWord || (isStartHandle() ? currLine < this.mPrevLine : currLine > this.mPrevLine)) ? atRtl == isAtRtlRun(layout, wordBoundary) : false;
                    if (snapToWord) {
                        if (layout.getLineForOffset(wordBoundary) != currLine) {
                            wordBoundary = isStartHandle() ? layout.getLineStart(currLine) : layout.getLineEnd(currLine);
                        }
                        int offsetThresholdToSnap;
                        if (isStartHandle()) {
                            offsetThresholdToSnap = wordEnd - ((wordEnd - wordBoundary) / 2);
                        } else {
                            offsetThresholdToSnap = wordStart + ((wordBoundary - wordStart) / 2);
                        }
                        if (isStartHandle() && (offset <= offsetThresholdToSnap || currLine < this.mPrevLine)) {
                            offset = wordStart;
                        } else if (isStartHandle() || (offset < offsetThresholdToSnap && currLine <= this.mPrevLine)) {
                            offset = this.mPreviousOffset;
                        } else {
                            offset = wordEnd;
                            Editor.this.setPosIsLineEnd(true);
                        }
                    }
                    if ((!isStartHandle() || offset >= initialOffset) && (isStartHandle() || offset <= initialOffset)) {
                        this.mTouchWordDelta = 0.0f;
                    } else {
                        this.mTouchWordDelta = Editor.this.mTextView.convertToLocalHorizontalCoordinate(x) - getHorizontal(layout, offset);
                    }
                    positionCursor = true;
                } else {
                    int adjustedOffset = getOffsetAtCoordinate(layout, currLine, x - this.mTouchWordDelta);
                    boolean shrinking = isStartHandle() ? adjustedOffset > this.mPreviousOffset || currLine > this.mPrevLine : adjustedOffset < this.mPreviousOffset || currLine < this.mPrevLine;
                    if (shrinking) {
                        if (currLine != this.mPrevLine) {
                            offset = isStartHandle() ? wordStart : wordEnd;
                            if ((!isStartHandle() || offset >= initialOffset) && (isStartHandle() || offset <= initialOffset)) {
                                this.mTouchWordDelta = 0.0f;
                            } else {
                                this.mTouchWordDelta = Editor.this.mTextView.convertToLocalHorizontalCoordinate(x) - getHorizontal(layout, offset);
                            }
                        } else {
                            offset = adjustedOffset;
                        }
                        positionCursor = true;
                    } else if ((isStartHandle() && adjustedOffset < this.mPreviousOffset) || (!isStartHandle() && adjustedOffset > this.mPreviousOffset)) {
                        this.mTouchWordDelta = Editor.this.mTextView.convertToLocalHorizontalCoordinate(x) - getHorizontal(layout, this.mPreviousOffset);
                    }
                }
                if (positionCursor) {
                    this.mPreviousLineTouched = currLine;
                    positionAndAdjustForCrossingHandles(offset);
                }
                this.mPrevX = x;
            } else {
                positionAndAdjustForCrossingHandles(offset);
                this.mTouchWordDelta = 0.0f;
                this.mLanguageDirectionChanged = false;
            }
        }

        protected void positionAtCursorOffset(int offset, boolean forceUpdatePosition) {
            offset = Editor.this.resetOffsetForImageSpan(offset, isStartHandle());
            super.positionAtCursorOffset(offset, forceUpdatePosition);
            this.mInWord = offset != -1 ? Editor.this.getWordIteratorWithText().isBoundary(offset) ^ 1 : false;
        }

        public boolean onTouchEvent(MotionEvent event) {
            boolean superResult = super.onTouchEvent(event);
            if (event.getActionMasked() == 0) {
                this.mTouchWordDelta = 0.0f;
                this.mPrevX = -1.0f;
            }
            return superResult;
        }

        private void positionAndAdjustForCrossingHandles(int offset) {
            int anotherHandleOffset = isStartHandle() ? Editor.this.mTextView.getSelectionEnd() : Editor.this.mTextView.getSelectionStart();
            if ((isStartHandle() && offset >= anotherHandleOffset) || (!isStartHandle() && offset <= anotherHandleOffset)) {
                this.mTouchWordDelta = 0.0f;
                Layout layout = Editor.this.mTextView.getLayout();
                if (!(layout == null || offset == anotherHandleOffset)) {
                    float horiz = getHorizontal(layout, offset);
                    float anotherHandleHoriz = getHorizontal(layout, anotherHandleOffset, isStartHandle() ^ 1);
                    float currentHoriz = getHorizontal(layout, this.mPreviousOffset);
                    if ((currentHoriz < anotherHandleHoriz && horiz < anotherHandleHoriz) || (currentHoriz > anotherHandleHoriz && horiz > anotherHandleHoriz)) {
                        int currentOffset = getCurrentCursorOffset();
                        long range = layout.getRunRange(isStartHandle() ? currentOffset : Math.max(currentOffset - 1, 0));
                        if (isStartHandle()) {
                            offset = TextUtils.unpackRangeStartFromLong(range);
                        } else {
                            offset = TextUtils.unpackRangeEndFromLong(range);
                        }
                        positionAtCursorOffset(offset, false);
                        return;
                    }
                }
                offset = Editor.this.getNextCursorOffset(anotherHandleOffset, isStartHandle() ^ 1);
            }
            positionAtCursorOffset(offset, false);
        }

        private boolean positionNearEdgeOfScrollingView(float x, boolean atRtl) {
            Editor.this.mTextView.getLocationOnScreen(this.mTextViewLocation);
            return atRtl == isStartHandle() ? x > ((float) ((this.mTextViewLocation[0] + Editor.this.mTextView.getWidth()) - Editor.this.mTextView.getPaddingRight())) - this.mTextViewEdgeSlop : x < ((float) (this.mTextViewLocation[0] + Editor.this.mTextView.getPaddingLeft())) + this.mTextViewEdgeSlop;
        }

        protected boolean isAtRtlRun(Layout layout, int offset) {
            return layout.isRtlCharAt(isStartHandle() ? offset : Math.max(offset - 1, 0));
        }

        public float getHorizontal(Layout layout, int offset) {
            return getHorizontal(layout, offset, isStartHandle());
        }

        private float getHorizontal(Layout layout, int offset, boolean startHandle) {
            return layout.isRtlCharAt(startHandle ? offset : Math.max(offset + -1, 0)) == (layout.getParagraphDirection(layout.getLineForOffset(offset)) == -1) ? layout.getPrimaryHorizontal(offset) : layout.getSecondaryHorizontal(offset);
        }

        protected int getOffsetAtCoordinate(Layout layout, int line, float x) {
            float localX = Editor.this.mTextView.convertToLocalHorizontalCoordinate(x);
            int primaryOffset = layout.getOffsetForHorizontal(line, localX, true);
            if (!layout.isLevelBoundary(primaryOffset)) {
                return primaryOffset;
            }
            int secondaryOffset = layout.getOffsetForHorizontal(line, localX, false);
            int currentOffset = getCurrentCursorOffset();
            int primaryDiff = Math.abs(primaryOffset - currentOffset);
            int secondaryDiff = Math.abs(secondaryOffset - currentOffset);
            if (primaryDiff < secondaryDiff) {
                return primaryOffset;
            }
            if (primaryDiff > secondaryDiff) {
                return secondaryOffset;
            }
            if (layout.isRtlCharAt(isStartHandle() ? currentOffset : Math.max(currentOffset - 1, 0)) != (layout.getParagraphDirection(line) == -1)) {
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
        private int mDragAcceleratorMode = 0;
        private SelectionHandleView mEndHandle;
        private boolean mGestureStayedInTapRegion;
        private boolean mHaventMovedEnoughToStartDrag;
        private int mLineSelectionIsOn = -1;
        private int mMaxTouchOffset;
        private int mMinTouchOffset;
        private SelectionHandleView mStartHandle;
        private int mStartOffset = -1;
        private boolean mSwitchedLines = false;

        SelectionModifierCursorController() {
            resetTouchOffsets();
        }

        public void show() {
            if (!Editor.this.mTextView.isInBatchEditMode()) {
                initDrawables();
                initHandles();
            }
        }

        private void initDrawables() {
            if (Editor.this.mSelectHandleLeft == null) {
                Editor.this.mSelectHandleLeft = Editor.this.mTextView.getContext().getDrawable(Editor.this.mTextView.mTextSelectHandleLeftRes);
            }
            if (Editor.this.mSelectHandleRight == null) {
                Editor.this.mSelectHandleRight = Editor.this.mTextView.getContext().getDrawable(Editor.this.mTextView.mTextSelectHandleRightRes);
            }
        }

        private void initHandles() {
            if (this.mStartHandle == null) {
                this.mStartHandle = new SelectionHandleView(Editor.this.mSelectHandleLeft, Editor.this.mSelectHandleRight, R.id.selection_start_handle, 0);
            }
            if (this.mEndHandle == null) {
                this.mEndHandle = new SelectionHandleView(Editor.this.mSelectHandleRight, Editor.this.mSelectHandleLeft, R.id.selection_end_handle, 1);
            }
            this.mStartHandle.show();
            this.mEndHandle.show();
            Editor.this.hideInsertionPointCursorController();
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
            this.mStartOffset = Editor.this.mTextView.getOffsetForPosition(Editor.this.mLastDownPositionX, Editor.this.mLastDownPositionY);
            this.mLineSelectionIsOn = Editor.this.mTextView.getLineAtCoordinate(Editor.this.mLastDownPositionY);
            hide();
            Editor.this.mTextView.getParent().requestDisallowInterceptTouchEvent(true);
            Editor.this.mTextView.cancelLongPress();
        }

        public void onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            boolean isMouse = event.isFromSource(InputDevice.SOURCE_MOUSE);
            float deltaX;
            float deltaY;
            float distanceSquared;
            switch (event.getActionMasked()) {
                case 0:
                    if (Editor.this.extractedTextModeWillBeStarted()) {
                        hide();
                        return;
                    }
                    int offsetForPosition = Editor.this.mTextView.getOffsetForPosition(eventX, eventY);
                    this.mMaxTouchOffset = offsetForPosition;
                    this.mMinTouchOffset = offsetForPosition;
                    if (this.mGestureStayedInTapRegion && (Editor.this.mTapState == 2 || Editor.this.mTapState == 3)) {
                        deltaX = eventX - this.mDownPositionX;
                        deltaY = eventY - this.mDownPositionY;
                        distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);
                        int doubleTapSlop = ViewConfiguration.get(Editor.this.mTextView.getContext()).getScaledDoubleTapSlop();
                        if ((distanceSquared < ((float) (doubleTapSlop * doubleTapSlop))) && (isMouse || Editor.this.isPositionOnText(eventX, eventY))) {
                            if (Editor.this.mTapState == 2) {
                                Editor.this.selectCurrentWordAndStartDrag();
                            } else if (Editor.this.mTapState == 3) {
                                selectCurrentParagraphAndStartDrag();
                            }
                            Editor.this.mDiscardNextActionUp = true;
                        }
                    }
                    this.mDownPositionX = eventX;
                    this.mDownPositionY = eventY;
                    this.mGestureStayedInTapRegion = true;
                    this.mHaventMovedEnoughToStartDrag = true;
                    return;
                case 1:
                    if (isDragAcceleratorActive()) {
                        updateSelection(event);
                        Editor.this.mTextView.getParent().requestDisallowInterceptTouchEvent(false);
                        resetDragAcceleratorState();
                        if (Editor.this.mTextView.hasSelection()) {
                            Editor.this.startSelectionActionModeAsync(this.mHaventMovedEnoughToStartDrag);
                            return;
                        }
                        return;
                    }
                    return;
                case 2:
                    ViewConfiguration viewConfig = ViewConfiguration.get(Editor.this.mTextView.getContext());
                    int touchSlop = viewConfig.getScaledTouchSlop();
                    if (this.mGestureStayedInTapRegion || this.mHaventMovedEnoughToStartDrag) {
                        deltaX = eventX - this.mDownPositionX;
                        deltaY = eventY - this.mDownPositionY;
                        distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);
                        if (this.mGestureStayedInTapRegion) {
                            int doubleTapTouchSlop = viewConfig.getScaledDoubleTapTouchSlop();
                            this.mGestureStayedInTapRegion = distanceSquared <= ((float) (doubleTapTouchSlop * doubleTapTouchSlop));
                        }
                        if (this.mHaventMovedEnoughToStartDrag) {
                            this.mHaventMovedEnoughToStartDrag = distanceSquared <= ((float) (touchSlop * touchSlop));
                        }
                    }
                    if (isMouse && (isDragAcceleratorActive() ^ 1) != 0) {
                        int offset = Editor.this.mTextView.getOffsetForPosition(eventX, eventY);
                        if (Editor.this.mTextView.hasSelection() && ((!this.mHaventMovedEnoughToStartDrag || this.mStartOffset != offset) && offset >= Editor.this.mTextView.getSelectionStart() && offset <= Editor.this.mTextView.getSelectionEnd())) {
                            Editor.this.startDragAndDrop();
                            return;
                        } else if (this.mStartOffset != offset) {
                            Editor.this.stopTextActionMode();
                            enterDrag(1);
                            Editor.this.mDiscardNextActionUp = true;
                            this.mHaventMovedEnoughToStartDrag = false;
                        }
                    }
                    if (this.mStartHandle == null || !this.mStartHandle.isShowing()) {
                        updateSelection(event);
                        return;
                    }
                    return;
                case 5:
                case 6:
                    if (Editor.this.mTextView.getContext().getPackageManager().hasSystemFeature("android.hardware.touchscreen.multitouch.distinct")) {
                        updateMinAndMaxOffsets(event);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        private void updateSelection(MotionEvent event) {
            if (Editor.this.mTextView.getLayout() != null) {
                switch (this.mDragAcceleratorMode) {
                    case 1:
                        updateCharacterBasedSelection(event);
                        return;
                    case 2:
                        updateWordBasedSelection(event);
                        return;
                    case 3:
                        updateParagraphBasedSelection(event);
                        return;
                    default:
                        return;
                }
            }
        }

        private boolean selectCurrentParagraphAndStartDrag() {
            if (Editor.this.mInsertionActionModeRunnable != null) {
                Editor.this.mTextView.removeCallbacks(Editor.this.mInsertionActionModeRunnable);
            }
            Editor.this.stopTextActionMode();
            if (!Editor.this.selectCurrentParagraph()) {
                return false;
            }
            enterDrag(3);
            return true;
        }

        private void updateCharacterBasedSelection(MotionEvent event) {
            Selection.setSelection((Spannable) Editor.this.mTextView.getText(), this.mStartOffset, Editor.this.mTextView.getOffsetForPosition(event.getX(), event.getY()));
        }

        private void updateWordBasedSelection(MotionEvent event) {
            if (!this.mHaventMovedEnoughToStartDrag) {
                int currLine;
                int startOffset;
                boolean isMouse = event.isFromSource(InputDevice.SOURCE_MOUSE);
                ViewConfiguration viewConfig = ViewConfiguration.get(Editor.this.mTextView.getContext());
                float eventX = event.getX();
                float eventY = event.getY();
                if (isMouse) {
                    currLine = Editor.this.mTextView.getLineAtCoordinate(eventY);
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
                    currLine = Editor.this.getCurrentLineAdjustedForSlop(Editor.this.mTextView.getLayout(), this.mLineSelectionIsOn, y);
                    if (!(this.mSwitchedLines || currLine == this.mLineSelectionIsOn)) {
                        this.mSwitchedLines = true;
                        return;
                    }
                }
                int offset = Editor.this.mTextView.getOffsetAtCoordinate(currLine, eventX);
                if (this.mStartOffset < offset) {
                    offset = Editor.this.getWordEnd(offset);
                    startOffset = Editor.this.getWordStart(this.mStartOffset);
                } else {
                    offset = Editor.this.getWordStart(offset);
                    startOffset = Editor.this.getWordEnd(this.mStartOffset);
                    if (startOffset == offset) {
                        offset = Editor.this.getNextCursorOffset(offset, false);
                    }
                }
                this.mLineSelectionIsOn = currLine;
                Selection.setSelection((Spannable) Editor.this.mTextView.getText(), startOffset, offset);
            }
        }

        private void updateParagraphBasedSelection(MotionEvent event) {
            int offset = Editor.this.mTextView.getOffsetForPosition(event.getX(), event.getY());
            long paragraphsRange = Editor.this.getParagraphsRange(Math.min(offset, this.mStartOffset), Math.max(offset, this.mStartOffset));
            Selection.setSelection((Spannable) Editor.this.mTextView.getText(), TextUtils.unpackRangeStartFromLong(paragraphsRange), TextUtils.unpackRangeEndFromLong(paragraphsRange));
        }

        private void updateMinAndMaxOffsets(MotionEvent event) {
            int pointerCount = event.getPointerCount();
            for (int index = 0; index < pointerCount; index++) {
                int offset = Editor.this.mTextView.getOffsetForPosition(event.getX(index), event.getY(index));
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
            this.mMaxTouchOffset = -1;
            this.mMinTouchOffset = -1;
            resetDragAcceleratorState();
        }

        private void resetDragAcceleratorState() {
            this.mStartOffset = -1;
            this.mDragAcceleratorMode = 0;
            this.mSwitchedLines = false;
            int selectionStart = Editor.this.mTextView.getSelectionStart();
            int selectionEnd = Editor.this.mTextView.getSelectionEnd();
            if (selectionStart > selectionEnd) {
                Selection.setSelection((Spannable) Editor.this.mTextView.getText(), selectionEnd, selectionStart);
            }
        }

        public boolean isSelectionStartDragged() {
            return this.mStartHandle != null ? this.mStartHandle.isDragging() : false;
        }

        public boolean isCursorBeingModified() {
            if (isDragAcceleratorActive() || isSelectionStartDragged()) {
                return true;
            }
            return this.mEndHandle != null ? this.mEndHandle.isDragging() : false;
        }

        public boolean isDragAcceleratorActive() {
            return this.mDragAcceleratorMode != 0;
        }

        public void onTouchModeChanged(boolean isInTouchMode) {
            if (!isInTouchMode) {
                hide();
            }
        }

        public void onDetached() {
            Editor.this.mTextView.getViewTreeObserver().removeOnTouchModeChangeListener(this);
            if (this.mStartHandle != null) {
                this.mStartHandle.onDetached();
            }
            if (this.mEndHandle != null) {
                this.mEndHandle.onDetached();
            }
        }

        public boolean isActive() {
            if (this.mStartHandle == null || !this.mStartHandle.isShowing() || this.mEndHandle == null) {
                return false;
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

    private class SpanController implements SpanWatcher {
        private static final int DISPLAY_TIMEOUT_MS = 3000;
        private Runnable mHidePopup;
        private EasyEditPopupWindow mPopupWindow;

        /* synthetic */ SpanController(Editor this$0, SpanController -this1) {
            this();
        }

        private SpanController() {
        }

        private boolean isNonIntermediateSelectionSpan(Spannable text, Object span) {
            if ((Selection.SELECTION_START == span || Selection.SELECTION_END == span) && (text.getSpanFlags(span) & 512) == 0) {
                return true;
            }
            return false;
        }

        public void onSpanAdded(Spannable text, Object span, int start, int end) {
            if (isNonIntermediateSelectionSpan(text, span)) {
                Editor.this.sendUpdateSelection();
            } else if (span instanceof EasyEditSpan) {
                if (this.mPopupWindow == null) {
                    this.mPopupWindow = new EasyEditPopupWindow(Editor.this, null);
                    this.mHidePopup = new Runnable() {
                        public void run() {
                            SpanController.this.hide();
                        }
                    };
                }
                if (this.mPopupWindow.mEasyEditSpan != null) {
                    this.mPopupWindow.mEasyEditSpan.setDeleteEnabled(false);
                }
                this.mPopupWindow.setEasyEditSpan((EasyEditSpan) span);
                this.mPopupWindow.setOnDeleteListener(new EasyEditDeleteListener() {
                    public void onDeleteClick(EasyEditSpan span) {
                        Editable editable = (Editable) Editor.this.mTextView.getText();
                        int start = editable.getSpanStart(span);
                        int end = editable.getSpanEnd(span);
                        if (start >= 0 && end >= 0) {
                            SpanController.this.sendEasySpanNotification(1, span);
                            Editor.this.mTextView.deleteText_internal(start, end);
                        }
                        editable.removeSpan(span);
                    }
                });
                if (Editor.this.mTextView.getWindowVisibility() == 0 && Editor.this.mTextView.getLayout() != null && !Editor.this.extractedTextModeWillBeStarted()) {
                    this.mPopupWindow.show();
                    Editor.this.mTextView.removeCallbacks(this.mHidePopup);
                    Editor.this.mTextView.postDelayed(this.mHidePopup, 3000);
                }
            }
        }

        public void onSpanRemoved(Spannable text, Object span, int start, int end) {
            if (isNonIntermediateSelectionSpan(text, span)) {
                Editor.this.sendUpdateSelection();
            } else if (this.mPopupWindow != null && span == this.mPopupWindow.mEasyEditSpan) {
                hide();
            }
        }

        public void onSpanChanged(Spannable text, Object span, int previousStart, int previousEnd, int newStart, int newEnd) {
            if (isNonIntermediateSelectionSpan(text, span)) {
                Editor.this.sendUpdateSelection();
            } else if (this.mPopupWindow != null && (span instanceof EasyEditSpan)) {
                EasyEditSpan easyEditSpan = (EasyEditSpan) span;
                sendEasySpanNotification(2, easyEditSpan);
                text.removeSpan(easyEditSpan);
            }
        }

        public void hide() {
            if (this.mPopupWindow != null) {
                this.mPopupWindow.hide();
                Editor.this.mTextView.removeCallbacks(this.mHidePopup);
            }
        }

        private void sendEasySpanNotification(int textChangedType, EasyEditSpan span) {
            try {
                PendingIntent pendingIntent = span.getPendingIntent();
                if (pendingIntent != null) {
                    Intent intent = new Intent();
                    intent.putExtra(EasyEditSpan.EXTRA_TEXT_CHANGED_TYPE, textChangedType);
                    pendingIntent.send(Editor.this.mTextView.getContext(), 0, intent);
                }
            } catch (CanceledException e) {
                Log.w("Editor", "PendingIntent for notification cannot be sent", e);
            }
        }
    }

    private class SuggestionHelper {
        private final HashMap<SuggestionSpan, Integer> mSpansLengths;
        private final Comparator<SuggestionSpan> mSuggestionSpanComparator;

        private class SuggestionSpanComparator implements Comparator<SuggestionSpan> {
            /* synthetic */ SuggestionSpanComparator(SuggestionHelper this$1, SuggestionSpanComparator -this1) {
                this();
            }

            private SuggestionSpanComparator() {
            }

            public int compare(SuggestionSpan span1, SuggestionSpan span2) {
                int flag1 = span1.getFlags();
                int flag2 = span2.getFlags();
                if (flag1 != flag2) {
                    boolean easy1 = (flag1 & 1) != 0;
                    boolean easy2 = (flag2 & 1) != 0;
                    boolean misspelled1 = (flag1 & 2) != 0;
                    boolean misspelled2 = (flag2 & 2) != 0;
                    if (easy1 && (misspelled1 ^ 1) != 0) {
                        return -1;
                    }
                    if (easy2 && (misspelled2 ^ 1) != 0) {
                        return 1;
                    }
                    if (misspelled1) {
                        return -1;
                    }
                    if (misspelled2) {
                        return 1;
                    }
                }
                return ((Integer) SuggestionHelper.this.mSpansLengths.get(span1)).intValue() - ((Integer) SuggestionHelper.this.mSpansLengths.get(span2)).intValue();
            }
        }

        /* synthetic */ SuggestionHelper(Editor this$0, SuggestionHelper -this1) {
            this();
        }

        private SuggestionHelper() {
            this.mSuggestionSpanComparator = new SuggestionSpanComparator(this, null);
            this.mSpansLengths = new HashMap();
        }

        private SuggestionSpan[] getSortedSuggestionSpans() {
            int pos = Editor.this.mTextView.getSelectionStart();
            Spannable spannable = (Spannable) Editor.this.mTextView.getText();
            SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) spannable.getSpans(pos, pos, SuggestionSpan.class);
            this.mSpansLengths.clear();
            for (SuggestionSpan suggestionSpan : suggestionSpans) {
                this.mSpansLengths.put(suggestionSpan, Integer.valueOf(spannable.getSpanEnd(suggestionSpan) - spannable.getSpanStart(suggestionSpan)));
            }
            Arrays.sort(suggestionSpans, this.mSuggestionSpanComparator);
            this.mSpansLengths.clear();
            return suggestionSpans;
        }

        public int getSuggestionInfo(SuggestionInfo[] suggestionInfos, SuggestionSpanInfo misspelledSpanInfo) {
            Spannable spannable = (Spannable) Editor.this.mTextView.getText();
            SuggestionSpan[] suggestionSpans = getSortedSuggestionSpans();
            if (suggestionSpans.length == 0) {
                return 0;
            }
            int numberOfSuggestions = 0;
            for (SuggestionSpan suggestionSpan : suggestionSpans) {
                int spanStart = spannable.getSpanStart(suggestionSpan);
                int spanEnd = spannable.getSpanEnd(suggestionSpan);
                if (!(misspelledSpanInfo == null || (suggestionSpan.getFlags() & 2) == 0)) {
                    misspelledSpanInfo.mSuggestionSpan = suggestionSpan;
                    misspelledSpanInfo.mSpanStart = spanStart;
                    misspelledSpanInfo.mSpanEnd = spanEnd;
                }
                String[] suggestions = suggestionSpan.getSuggestions();
                int nbSuggestions = suggestions.length;
                for (int suggestionIndex = 0; suggestionIndex < nbSuggestions; suggestionIndex++) {
                    CharSequence suggestion = suggestions[suggestionIndex];
                    for (int i = 0; i < numberOfSuggestions; i++) {
                        SuggestionInfo otherSuggestionInfo = suggestionInfos[i];
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
                    suggestionInfo.mSuggestionStart = 0;
                    suggestionInfo.mSuggestionEnd = suggestion.length();
                    suggestionInfo.mText.replace(0, suggestionInfo.mText.length(), suggestion);
                    numberOfSuggestions++;
                    if (numberOfSuggestions >= suggestionInfos.length) {
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

        /* synthetic */ SuggestionInfo(SuggestionInfo -this0) {
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

        /* synthetic */ SuggestionSpanInfo(SuggestionSpanInfo -this0) {
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
        private boolean mIsShowingUp = false;
        private final SuggestionSpanInfo mMisspelledSpanInfo = new SuggestionSpanInfo();
        private int mNumberOfSuggestions;
        private SuggestionInfo[] mSuggestionInfos;
        private ListView mSuggestionListView;
        private SuggestionAdapter mSuggestionsAdapter;

        private class CustomPopupWindow extends PopupWindow {
            /* synthetic */ CustomPopupWindow(SuggestionsPopupWindow this$1, CustomPopupWindow -this1) {
                this();
            }

            private CustomPopupWindow() {
            }

            public void dismiss() {
                if (isShowing()) {
                    super.dismiss();
                    Editor.this.getPositionListener().removeSubscriber(SuggestionsPopupWindow.this);
                    ((Spannable) Editor.this.mTextView.getText()).removeSpan(Editor.this.mSuggestionRangeSpan);
                    Editor.this.mTextView.setCursorVisible(SuggestionsPopupWindow.this.mCursorWasVisibleBeforeSuggestions);
                    if (Editor.this.hasInsertionController() && (Editor.this.extractedTextModeWillBeStarted() ^ 1) != 0) {
                        Editor.this.getInsertionController().show();
                    }
                }
            }
        }

        private class SuggestionAdapter extends BaseAdapter {
            private LayoutInflater mInflater;

            /* synthetic */ SuggestionAdapter(SuggestionsPopupWindow this$1, SuggestionAdapter -this1) {
                this();
            }

            private SuggestionAdapter() {
                this.mInflater = (LayoutInflater) SuggestionsPopupWindow.this.mContext.getSystemService("layout_inflater");
            }

            public int getCount() {
                return SuggestionsPopupWindow.this.mNumberOfSuggestions;
            }

            public Object getItem(int position) {
                return SuggestionsPopupWindow.this.mSuggestionInfos[position];
            }

            public long getItemId(int position) {
                return (long) position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) convertView;
                if (textView == null) {
                    textView = (TextView) this.mInflater.inflate(Editor.this.mTextView.mTextEditSuggestionItemLayout, parent, false);
                }
                textView.setText(SuggestionsPopupWindow.this.mSuggestionInfos[position].mText);
                return textView;
            }
        }

        public SuggestionsPopupWindow() {
            super();
            this.mCursorWasVisibleBeforeSuggestions = Editor.this.mCursorVisible;
        }

        protected void setUp() {
            this.mContext = applyDefaultTheme(Editor.this.mTextView.getContext());
            this.mHighlightSpan = new TextAppearanceSpan(this.mContext, Editor.this.mTextView.mTextEditSuggestionHighlightStyle);
        }

        private Context applyDefaultTheme(Context originalContext) {
            int themeId;
            TypedArray a = originalContext.obtainStyledAttributes(new int[]{R.attr.isLightTheme});
            if (a.getBoolean(0, true)) {
                themeId = R.style.ThemeOverlay_Material_Light;
            } else {
                themeId = R.style.ThemeOverlay_Material_Dark;
            }
            a.recycle();
            return new ContextThemeWrapper(originalContext, themeId);
        }

        protected void createPopupWindow() {
            this.mPopupWindow = new CustomPopupWindow(this, null);
            this.mPopupWindow.setInputMethodMode(2);
            this.mPopupWindow.setBackgroundDrawable(new ColorDrawable(0));
            this.mPopupWindow.setFocusable(true);
            this.mPopupWindow.setClippingEnabled(false);
        }

        protected void initContentView() {
            this.mContentView = (ViewGroup) ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(Editor.this.mTextView.mTextEditSuggestionContainerLayout, null);
            this.mContainerView = (LinearLayout) this.mContentView.findViewById(R.id.suggestionWindowContainer);
            MarginLayoutParams lp = (MarginLayoutParams) this.mContainerView.getLayoutParams();
            this.mContainerMarginWidth = lp.leftMargin + lp.rightMargin;
            this.mContainerMarginTop = lp.topMargin;
            this.mClippingLimitLeft = lp.leftMargin;
            this.mClippingLimitRight = lp.rightMargin;
            this.mSuggestionListView = (ListView) this.mContentView.findViewById(R.id.suggestionContainer);
            this.mSuggestionsAdapter = new SuggestionAdapter(this, null);
            this.mSuggestionListView.setAdapter(this.mSuggestionsAdapter);
            this.mSuggestionListView.setOnItemClickListener(this);
            this.mSuggestionInfos = new SuggestionInfo[5];
            for (int i = 0; i < this.mSuggestionInfos.length; i++) {
                this.mSuggestionInfos[i] = new SuggestionInfo();
            }
            this.mAddToDictionaryButton = (TextView) this.mContentView.findViewById(R.id.addToDictionaryButton);
            this.mAddToDictionaryButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    SuggestionSpan misspelledSpan = Editor.this.findEquivalentSuggestionSpan(SuggestionsPopupWindow.this.mMisspelledSpanInfo);
                    if (misspelledSpan != null) {
                        Editable editable = (Editable) Editor.this.mTextView.getText();
                        int spanStart = editable.getSpanStart(misspelledSpan);
                        int spanEnd = editable.getSpanEnd(misspelledSpan);
                        if (spanStart >= 0 && spanEnd > spanStart) {
                            String originalText = TextUtils.substring(editable, spanStart, spanEnd);
                            Intent intent = new Intent(Settings.ACTION_USER_DICTIONARY_INSERT);
                            intent.putExtra("word", originalText);
                            intent.putExtra("locale", Editor.this.mTextView.getTextServicesLocale().toString());
                            intent.setFlags(intent.getFlags() | 268435456);
                            Editor.this.mTextView.getContext().startActivity(intent);
                            editable.removeSpan(SuggestionsPopupWindow.this.mMisspelledSpanInfo.mSuggestionSpan);
                            Selection.setSelection(editable, spanEnd);
                            Editor.this.updateSpellCheckSpans(spanStart, spanEnd, false);
                            SuggestionsPopupWindow.this.hideWithCleanUp();
                        }
                    }
                }
            });
            this.mDeleteButton = (TextView) this.mContentView.findViewById(R.id.deleteButton);
            this.mDeleteButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Editable editable = (Editable) Editor.this.mTextView.getText();
                    int spanUnionStart = editable.getSpanStart(Editor.this.mSuggestionRangeSpan);
                    int spanUnionEnd = editable.getSpanEnd(Editor.this.mSuggestionRangeSpan);
                    if (spanUnionStart >= 0 && spanUnionEnd > spanUnionStart) {
                        if (spanUnionEnd < editable.length() && Character.isSpaceChar(editable.charAt(spanUnionEnd)) && (spanUnionStart == 0 || Character.isSpaceChar(editable.charAt(spanUnionStart - 1)))) {
                            spanUnionEnd++;
                        }
                        Editor.this.mTextView.deleteText_internal(spanUnionStart, spanUnionEnd);
                    }
                    SuggestionsPopupWindow.this.hideWithCleanUp();
                }
            });
        }

        public boolean isShowingUp() {
            return this.mIsShowingUp;
        }

        public void onParentLostFocus() {
            this.mIsShowingUp = false;
        }

        public ViewGroup getContentViewForTesting() {
            return this.mContentView;
        }

        public void show() {
            if ((Editor.this.mTextView.getText() instanceof Editable) && !Editor.this.extractedTextModeWillBeStarted() && updateSuggestions()) {
                this.mCursorWasVisibleBeforeSuggestions = Editor.this.mCursorVisible;
                Editor.this.mTextView.setCursorVisible(false);
                this.mIsShowingUp = true;
                super.show();
            }
        }

        protected void measureContent() {
            DisplayMetrics displayMetrics = Editor.this.mTextView.getResources().getDisplayMetrics();
            int horizontalMeasure = MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, Integer.MIN_VALUE);
            int verticalMeasure = MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, Integer.MIN_VALUE);
            int width = 0;
            View view = null;
            for (int i = 0; i < this.mNumberOfSuggestions; i++) {
                view = this.mSuggestionsAdapter.getView(i, view, this.mContentView);
                view.getLayoutParams().width = -2;
                view.measure(horizontalMeasure, verticalMeasure);
                width = Math.max(width, view.getMeasuredWidth());
            }
            if (this.mAddToDictionaryButton.getVisibility() != 8) {
                this.mAddToDictionaryButton.measure(horizontalMeasure, verticalMeasure);
                width = Math.max(width, this.mAddToDictionaryButton.getMeasuredWidth());
            }
            this.mDeleteButton.measure(horizontalMeasure, verticalMeasure);
            width = Math.max(width, this.mDeleteButton.getMeasuredWidth()) + ((this.mContainerView.getPaddingLeft() + this.mContainerView.getPaddingRight()) + this.mContainerMarginWidth);
            this.mContentView.measure(MeasureSpec.makeMeasureSpec(width, 1073741824), verticalMeasure);
            Drawable popupBackground = this.mPopupWindow.getBackground();
            if (popupBackground != null) {
                if (Editor.this.mTempRect == null) {
                    Editor.this.mTempRect = new Rect();
                }
                popupBackground.getPadding(Editor.this.mTempRect);
                width += Editor.this.mTempRect.left + Editor.this.mTempRect.right;
            }
            this.mPopupWindow.setWidth(width);
        }

        protected int getTextOffset() {
            return (Editor.this.mTextView.getSelectionStart() + Editor.this.mTextView.getSelectionStart()) / 2;
        }

        protected int getVerticalLocalPosition(int line) {
            return Editor.this.mTextView.getLayout().getLineBottom(line) - this.mContainerMarginTop;
        }

        protected int clipVertically(int positionY) {
            return Math.min(positionY, Editor.this.mTextView.getResources().getDisplayMetrics().heightPixels - this.mContentView.getMeasuredHeight());
        }

        private void hideWithCleanUp() {
            for (SuggestionInfo info : this.mSuggestionInfos) {
                info.clear();
            }
            this.mMisspelledSpanInfo.clear();
            hide();
        }

        private boolean updateSuggestions() {
            Spannable spannable = (Spannable) Editor.this.mTextView.getText();
            this.mNumberOfSuggestions = Editor.this.mSuggestionHelper.getSuggestionInfo(this.mSuggestionInfos, this.mMisspelledSpanInfo);
            if (this.mNumberOfSuggestions == 0 && this.mMisspelledSpanInfo.mSuggestionSpan == null) {
                return false;
            }
            int i;
            int underlineColor;
            int spanUnionStart = Editor.this.mTextView.getText().length();
            int spanUnionEnd = 0;
            for (i = 0; i < this.mNumberOfSuggestions; i++) {
                SuggestionSpanInfo spanInfo = this.mSuggestionInfos[i].mSuggestionSpanInfo;
                spanUnionStart = Math.min(spanUnionStart, spanInfo.mSpanStart);
                spanUnionEnd = Math.max(spanUnionEnd, spanInfo.mSpanEnd);
            }
            if (this.mMisspelledSpanInfo.mSuggestionSpan != null) {
                spanUnionStart = Math.min(spanUnionStart, this.mMisspelledSpanInfo.mSpanStart);
                spanUnionEnd = Math.max(spanUnionEnd, this.mMisspelledSpanInfo.mSpanEnd);
            }
            for (i = 0; i < this.mNumberOfSuggestions; i++) {
                highlightTextDifferences(this.mSuggestionInfos[i], spanUnionStart, spanUnionEnd);
            }
            int addToDictionaryButtonVisibility = 8;
            if (this.mMisspelledSpanInfo.mSuggestionSpan != null && this.mMisspelledSpanInfo.mSpanStart >= 0 && this.mMisspelledSpanInfo.mSpanEnd > this.mMisspelledSpanInfo.mSpanStart) {
                addToDictionaryButtonVisibility = 0;
            }
            this.mAddToDictionaryButton.setVisibility(addToDictionaryButtonVisibility);
            if (Editor.this.mSuggestionRangeSpan == null) {
                Editor.this.mSuggestionRangeSpan = new SuggestionRangeSpan();
            }
            if (this.mNumberOfSuggestions != 0) {
                underlineColor = this.mSuggestionInfos[0].mSuggestionSpanInfo.mSuggestionSpan.getUnderlineColor();
            } else {
                underlineColor = this.mMisspelledSpanInfo.mSuggestionSpan.getUnderlineColor();
            }
            if (underlineColor == 0) {
                Editor.this.mSuggestionRangeSpan.setBackgroundColor(Editor.this.mTextView.mHighlightColor);
            } else {
                Editor.this.mSuggestionRangeSpan.setBackgroundColor((16777215 & underlineColor) + (((int) (((float) Color.alpha(underlineColor)) * 0.4f)) << 24));
            }
            try {
                spannable.setSpan(Editor.this.mSuggestionRangeSpan, spanUnionStart, spanUnionEnd, 33);
            } catch (IndexOutOfBoundsException e) {
                Log.d("Editor", "setSpan IndexOutOfBoundsException");
            }
            this.mSuggestionsAdapter.notifyDataSetChanged();
            return true;
        }

        private void highlightTextDifferences(SuggestionInfo suggestionInfo, int unionStart, int unionEnd) {
            Spannable text = (Spannable) Editor.this.mTextView.getText();
            int spanStart = suggestionInfo.mSuggestionSpanInfo.mSpanStart;
            int spanEnd = suggestionInfo.mSuggestionSpanInfo.mSpanEnd;
            suggestionInfo.mSuggestionStart = spanStart - unionStart;
            suggestionInfo.mSuggestionEnd = suggestionInfo.mSuggestionStart + suggestionInfo.mText.length();
            suggestionInfo.mText.setSpan(this.mHighlightSpan, 0, suggestionInfo.mText.length(), 33);
            String textAsString = text.toString();
            suggestionInfo.mText.insert(0, textAsString.substring(unionStart, spanStart));
            suggestionInfo.mText.append(textAsString.substring(spanEnd, unionEnd));
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Editor.this.replaceWithSuggestion(this.mSuggestionInfos[position]);
            hideWithCleanUp();
        }
    }

    private class TextActionModeCallback extends Callback2 {
        private final int mHandleHeight;
        private final boolean mHasSelection;
        private final RectF mSelectionBounds = new RectF();
        private final Path mSelectionPath = new Path();

        public TextActionModeCallback(boolean hasSelection) {
            this.mHasSelection = hasSelection;
            if (this.mHasSelection) {
                SelectionModifierCursorController selectionController = Editor.this.getSelectionController();
                if (selectionController.mStartHandle == null) {
                    selectionController.initDrawables();
                    selectionController.initHandles();
                    selectionController.hide();
                }
                this.mHandleHeight = Math.max(Editor.this.mSelectHandleLeft.getMinimumHeight(), Editor.this.mSelectHandleRight.getMinimumHeight());
                return;
            }
            InsertionPointCursorController insertionController = Editor.this.getInsertionController();
            if (insertionController != null) {
                insertionController.getHandle();
                this.mHandleHeight = Editor.this.mSelectHandleCenter.getMinimumHeight();
                return;
            }
            this.mHandleHeight = 0;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(null);
            mode.setSubtitle(null);
            mode.setTitleOptionalHint(true);
            populateMenuWithItems(menu);
            Callback customCallback = getCustomCallback();
            if (customCallback == null || customCallback.onCreateActionMode(mode, menu)) {
                if (Editor.this.mTextView.canProcessText()) {
                    Editor.this.mProcessTextIntentActionsHandler.onInitializeMenu(menu);
                }
                if (!menu.hasVisibleItems() && mode.getCustomView() == null) {
                    return false;
                }
                if (this.mHasSelection && (Editor.this.mTextView.hasTransientState() ^ 1) != 0) {
                    Editor.this.mTextView.setHasTransientState(true);
                }
                return true;
            }
            Selection.setSelection((Spannable) Editor.this.mTextView.getText(), Editor.this.mTextView.getSelectionEnd());
            return false;
        }

        private Callback getCustomCallback() {
            if (this.mHasSelection) {
                return Editor.this.mCustomSelectionActionModeCallback;
            }
            return Editor.this.mCustomInsertionActionModeCallback;
        }

        private void populateMenuWithItems(Menu menu) {
            if (Editor.this.mTextView.canCut()) {
                menu.add(0, (int) R.id.cut, 6, (int) R.string.cut).setAlphabeticShortcut(StateProperty.TARGET_X).setShowAsAction(2);
            }
            if (Editor.this.mTextView.canCopy()) {
                menu.add(0, (int) R.id.copy, 7, (int) R.string.copy).setAlphabeticShortcut('c').setShowAsAction(2);
            }
            if (Editor.this.mTextView.canPaste()) {
                menu.add(0, (int) R.id.paste, 8, (int) R.string.paste).setAlphabeticShortcut('v').setShowAsAction(2);
            }
            if (Editor.this.mTextView.canShare()) {
                menu.add(0, (int) R.id.shareText, 5, (int) R.string.share).setShowAsAction(1);
            }
            if (Editor.this.mTextView.canRequestAutofill()) {
                String selected = Editor.this.mTextView.getSelectedText();
                if (selected == null || selected.isEmpty()) {
                    menu.add(0, (int) R.id.autofill, 11, (int) R.string.autofill).setShowAsAction(Integer.MIN_VALUE);
                }
            }
            if (Editor.this.mTextView.canPasteAsPlainText()) {
                menu.add(0, (int) R.id.pasteAsPlainText, 9, (int) R.string.paste_as_plain_text).setShowAsAction(1);
            }
            updateSelectAllItem(menu);
            updateReplaceItem(menu);
            updateAssistMenuItem(menu);
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            updateSelectAllItem(menu);
            updateReplaceItem(menu);
            updateAssistMenuItem(menu);
            Callback customCallback = getCustomCallback();
            if (customCallback != null) {
                return customCallback.onPrepareActionMode(mode, menu);
            }
            return true;
        }

        private void updateSelectAllItem(Menu menu) {
            boolean canSelectAll = Editor.this.mTextView.canSelectAllText();
            boolean selectAllItemExists = menu.findItem(R.id.selectAll) != null;
            if (canSelectAll && (selectAllItemExists ^ 1) != 0) {
                menu.add(0, (int) R.id.selectAll, 1, (int) R.string.selectAll).setShowAsAction(1);
            } else if (!canSelectAll && selectAllItemExists) {
                menu.removeItem(R.id.selectAll);
            }
        }

        private void updateReplaceItem(Menu menu) {
            boolean canReplace = Editor.this.mTextView.isSuggestionsEnabled() ? Editor.this.shouldOfferToShowSuggestions() : false;
            boolean replaceItemExists = menu.findItem(R.id.replaceText) != null;
            if (canReplace && (replaceItemExists ^ 1) != 0) {
                menu.add(0, (int) R.id.replaceText, 10, (int) R.string.replace).setShowAsAction(1);
            } else if (!canReplace && replaceItemExists) {
                menu.removeItem(R.id.replaceText);
            }
        }

        private void updateAssistMenuItem(Menu menu) {
            menu.removeItem(R.id.textAssist);
            TextClassification textClassification = Editor.this.getSelectionActionModeHelper().getTextClassification();
            if (Editor.this.mTextView.isDeviceProvisioned() && textClassification != null) {
                Drawable icon = textClassification.getIcon();
                CharSequence label = textClassification.getLabel();
                OnClickListener onClickListener = textClassification.getOnClickListener();
                Intent intent = textClassification.getIntent();
                if (icon != null || (TextUtils.isEmpty(label) ^ 1) != 0) {
                    if (onClickListener != null || intent != null) {
                        menu.add((int) R.id.textAssist, (int) R.id.textAssist, 2, label).setIcon(icon).setShowAsAction(2);
                        Editor.this.mMetricsLogger.write(new LogMaker(MetricsEvent.TEXT_SELECTION_MENU_ITEM_ASSIST).setType(1).setSubtype(textClassification.getLogType()));
                    }
                }
            }
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Editor.this.getSelectionActionModeHelper().onSelectionAction();
            if (Editor.this.mProcessTextIntentActionsHandler.performMenuItemAction(item)) {
                return true;
            }
            Callback customCallback = getCustomCallback();
            if (customCallback != null && customCallback.onActionItemClicked(mode, item)) {
                return true;
            }
            TextClassification textClassification = Editor.this.getSelectionActionModeHelper().getTextClassification();
            if (R.id.textAssist == item.getItemId() && textClassification != null) {
                OnClickListener onClickListener = textClassification.getOnClickListener();
                if (onClickListener != null) {
                    onClickListener.onClick(Editor.this.mTextView);
                } else {
                    Intent intent = textClassification.getIntent();
                    if (intent != null) {
                        TextClassification.createStartActivityOnClickListener(Editor.this.mTextView.getContext(), intent).onClick(Editor.this.mTextView);
                    }
                }
                Editor.this.mMetricsLogger.action((int) MetricsEvent.ACTION_TEXT_SELECTION_MENU_ITEM_ASSIST, textClassification.getLogType());
                Editor.this.stopTextActionMode();
                return true;
            } else if (item.getItemId() != R.id.cut || !HwDeviceManager.disallowOp(23)) {
                return Editor.this.mTextView.onTextContextMenuItem(item.getItemId());
            } else {
                Toast toast = Toast.makeText(Editor.this.mTextView.getContext(), Editor.this.mTextView.getContext().getResources().getString(33685904), 1);
                toast.getWindowParams().type = 2006;
                toast.show();
                Log.i("Editor", "TextView cut is not allowed by MDM!");
                return true;
            }
        }

        public void onDestroyActionMode(ActionMode mode) {
            Editor.this.getSelectionActionModeHelper().onDestroyActionMode();
            Editor.this.mTextActionMode = null;
            Callback customCallback = getCustomCallback();
            if (customCallback != null) {
                customCallback.onDestroyActionMode(mode);
            }
            if (!Editor.this.mPreserveSelection) {
                Selection.setSelection((Spannable) Editor.this.mTextView.getText(), Editor.this.mTextView.getSelectionEnd());
            }
            if (Editor.this.mSelectionModifierCursorController != null) {
                Editor.this.mSelectionModifierCursorController.hide();
            }
        }

        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            if (!view.equals(Editor.this.mTextView) || Editor.this.mTextView.getLayout() == null) {
                super.onGetContentRect(mode, view, outRect);
                return;
            }
            if (Editor.this.mTextView.getSelectionStart() != Editor.this.mTextView.getSelectionEnd()) {
                this.mSelectionPath.reset();
                Editor.this.mTextView.getLayout().getSelectionPath(Editor.this.mTextView.getSelectionStart(), Editor.this.mTextView.getSelectionEnd(), this.mSelectionPath);
                this.mSelectionPath.computeBounds(this.mSelectionBounds, true);
                RectF rectF = this.mSelectionBounds;
                rectF.bottom += (float) this.mHandleHeight;
            } else if (Editor.this.mCursorCount == 2) {
                Rect firstCursorBounds = Editor.this.mCursorDrawable[0].getBounds();
                Rect secondCursorBounds = Editor.this.mCursorDrawable[1].getBounds();
                this.mSelectionBounds.set((float) Math.min(firstCursorBounds.left, secondCursorBounds.left), (float) Math.min(firstCursorBounds.top, secondCursorBounds.top), (float) Math.max(firstCursorBounds.right, secondCursorBounds.right), (float) (Math.max(firstCursorBounds.bottom, secondCursorBounds.bottom) + this.mHandleHeight));
            } else {
                Layout layout = Editor.this.mTextView.getLayout();
                int line = layout.getLineForOffset(Editor.this.mTextView.getSelectionStart());
                float primaryHorizontal = (float) Editor.this.clampHorizontalPosition(null, layout.getPrimaryHorizontal(Editor.this.mTextView.getSelectionStart()));
                this.mSelectionBounds.set(primaryHorizontal, (float) layout.getLineTop(line), primaryHorizontal, (float) (layout.getLineTop(line + 1) + this.mHandleHeight));
                Editor.this.adjustSelectionBounds(this.mSelectionBounds, line, layout, this.mHandleHeight);
            }
            int textHorizontalOffset = Editor.this.mTextView.viewportToContentHorizontalOffset();
            int textVerticalOffset = Editor.this.mTextView.viewportToContentVerticalOffset();
            outRect.set((int) Math.floor((double) (this.mSelectionBounds.left + ((float) textHorizontalOffset))), (int) Math.floor((double) (this.mSelectionBounds.top + ((float) textVerticalOffset))), (int) Math.ceil((double) (this.mSelectionBounds.right + ((float) textHorizontalOffset))), (int) Math.ceil((double) (this.mSelectionBounds.bottom + ((float) textVerticalOffset))));
        }
    }

    private static class TextRenderNode {
        boolean isDirty = true;
        boolean needsToBeShifted = true;
        RenderNode renderNode;

        public TextRenderNode(String name) {
            this.renderNode = RenderNode.create(name, null);
        }

        boolean needsRecord() {
            return !this.isDirty ? this.renderNode.isValid() ^ 1 : true;
        }
    }

    public static class UndoInputFilter implements InputFilter {
        private static final int MERGE_EDIT_MODE_FORCE_MERGE = 0;
        private static final int MERGE_EDIT_MODE_NEVER_MERGE = 1;
        private static final int MERGE_EDIT_MODE_NORMAL = 2;
        private final Editor mEditor;
        private boolean mExpanding;
        private boolean mHasComposition;
        private boolean mIsUserEdit;
        private boolean mPreviousOperationWasInSameBatchEdit;

        public UndoInputFilter(Editor editor) {
            this.mEditor = editor;
        }

        public void saveInstanceState(Parcel parcel) {
            int i;
            int i2 = 1;
            if (this.mIsUserEdit) {
                i = 1;
            } else {
                i = 0;
            }
            parcel.writeInt(i);
            if (this.mHasComposition) {
                i = 1;
            } else {
                i = 0;
            }
            parcel.writeInt(i);
            if (this.mExpanding) {
                i = 1;
            } else {
                i = 0;
            }
            parcel.writeInt(i);
            if (!this.mPreviousOperationWasInSameBatchEdit) {
                i2 = 0;
            }
            parcel.writeInt(i2);
        }

        public void restoreInstanceState(Parcel parcel) {
            boolean z;
            boolean z2 = true;
            if (parcel.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mIsUserEdit = z;
            if (parcel.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mHasComposition = z;
            if (parcel.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mExpanding = z;
            if (parcel.readInt() == 0) {
                z2 = false;
            }
            this.mPreviousOperationWasInSameBatchEdit = z2;
        }

        public void beginBatchEdit() {
            this.mIsUserEdit = true;
        }

        public void endBatchEdit() {
            this.mIsUserEdit = false;
            this.mPreviousOperationWasInSameBatchEdit = false;
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (!canUndoEdit(source, start, end, dest, dstart, dend)) {
                return null;
            }
            boolean hadComposition = this.mHasComposition;
            this.mHasComposition = isComposition(source);
            boolean wasExpanding = this.mExpanding;
            boolean shouldCreateSeparateState = false;
            if (end - start != dend - dstart) {
                this.mExpanding = end - start > dend - dstart;
                if (hadComposition && this.mExpanding != wasExpanding) {
                    shouldCreateSeparateState = true;
                }
            }
            handleEdit(source, start, end, dest, dstart, dend, shouldCreateSeparateState);
            return null;
        }

        void freezeLastEdit() {
            this.mEditor.mUndoManager.beginUpdate("Edit text");
            EditOperation lastEdit = getLastEdit();
            if (lastEdit != null) {
                lastEdit.mFrozen = true;
            }
            this.mEditor.mUndoManager.endUpdate();
        }

        private void handleEdit(CharSequence source, int start, int end, Spanned dest, int dstart, int dend, boolean shouldCreateSeparateState) {
            int mergeMode;
            if (isInTextWatcher() || this.mPreviousOperationWasInSameBatchEdit) {
                mergeMode = 0;
            } else if (shouldCreateSeparateState) {
                mergeMode = 1;
            } else {
                mergeMode = 2;
            }
            String newText = TextUtils.substring(source, start, end);
            EditOperation edit = new EditOperation(this.mEditor, TextUtils.substring(dest, dstart, dend), dstart, newText, this.mHasComposition);
            if (!this.mHasComposition || !TextUtils.equals(edit.mNewText, edit.mOldText)) {
                recordEdit(edit, mergeMode);
            }
        }

        private EditOperation getLastEdit() {
            return (EditOperation) this.mEditor.mUndoManager.getLastOperation(EditOperation.class, this.mEditor.mUndoOwner, 1);
        }

        private void recordEdit(EditOperation edit, int mergeMode) {
            UndoManager um = this.mEditor.mUndoManager;
            um.beginUpdate("Edit text");
            EditOperation lastEdit = getLastEdit();
            if (lastEdit == null) {
                um.addOperation(edit, 0);
            } else if (mergeMode == 0) {
                lastEdit.forceMergeWith(edit);
            } else if (!this.mIsUserEdit) {
                um.commitState(this.mEditor.mUndoOwner);
                um.addOperation(edit, 0);
            } else if (!(mergeMode == 2 && lastEdit.mergeWith(edit))) {
                um.commitState(this.mEditor.mUndoOwner);
                um.addOperation(edit, 0);
            }
            this.mPreviousOperationWasInSameBatchEdit = this.mIsUserEdit;
            um.endUpdate();
        }

        private boolean canUndoEdit(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (!this.mEditor.mAllowUndo || this.mEditor.mUndoManager.isInUndo() || !Editor.isValidRange(source, start, end) || (Editor.isValidRange(dest, dstart, dend) ^ 1) != 0) {
                return false;
            }
            if (start == end && dstart == dend) {
                return false;
            }
            return true;
        }

        private static boolean isComposition(CharSequence source) {
            boolean z = false;
            if (!(source instanceof Spannable)) {
                return false;
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
                return false;
            }
            return true;
        }
    }

    public Editor(TextView textView) {
        this.mTextView = textView;
        this.mTextView.setFilters(this.mTextView.getFilters());
        this.mProcessTextIntentActionsHandler = new ProcessTextIntentActionsHandler(this, null);
        this.mTextHeightOffset = this.mTextView.getResources().getDimensionPixelSize(com.android.hwext.internal.R.dimen.zeditor_height_add);
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
        this.mUndoOwner = this.mUndoManager.getOwner("Editor", this);
    }

    void forgetUndoRedo() {
        UndoOwner[] owners = new UndoOwner[]{this.mUndoOwner};
        this.mUndoManager.forgetUndos(owners, -1);
        this.mUndoManager.forgetRedos(owners, -1);
    }

    boolean canUndo() {
        UndoOwner[] owners = new UndoOwner[]{this.mUndoOwner};
        if (!this.mAllowUndo || this.mUndoManager.countUndos(owners) <= 0) {
            return false;
        }
        return true;
    }

    boolean canRedo() {
        UndoOwner[] owners = new UndoOwner[]{this.mUndoOwner};
        if (!this.mAllowUndo || this.mUndoManager.countRedos(owners) <= 0) {
            return false;
        }
        return true;
    }

    void undo() {
        if (this.mAllowUndo) {
            this.mUndoManager.undo(new UndoOwner[]{this.mUndoOwner}, 1);
        }
    }

    void redo() {
        if (this.mAllowUndo) {
            this.mUndoManager.redo(new UndoOwner[]{this.mUndoOwner}, 1);
        }
    }

    void replace() {
        if (this.mSuggestionsPopupWindow == null) {
            this.mSuggestionsPopupWindow = new SuggestionsPopupWindow();
        }
        hideCursorAndSpanControllers();
        this.mSuggestionsPopupWindow.show();
        Selection.setSelection((Spannable) this.mTextView.getText(), (this.mTextView.getSelectionStart() + this.mTextView.getSelectionEnd()) / 2);
    }

    void onAttachedToWindow() {
        if (this.mShowErrorAfterAttach) {
            showError();
            this.mShowErrorAfterAttach = false;
        }
        ViewTreeObserver observer = this.mTextView.getViewTreeObserver();
        if (this.mInsertionPointCursorController != null) {
            observer.addOnTouchModeChangeListener(this.mInsertionPointCursorController);
        }
        if (this.mSelectionModifierCursorController != null) {
            this.mSelectionModifierCursorController.resetTouchOffsets();
            observer.addOnTouchModeChangeListener(this.mSelectionModifierCursorController);
        }
        updateSpellCheckSpans(0, this.mTextView.getText().length(), true);
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
            for (int i = 0; i < this.mTextRenderNodes.length; i++) {
                RenderNode displayList = this.mTextRenderNodes[i] != null ? this.mTextRenderNodes[i].renderNode : null;
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
            this.mErrorPopup.setFocusable(false);
            this.mErrorPopup.setInputMethodMode(1);
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
            this.mShowErrorAfterAttach = false;
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
        this.mShowErrorAfterAttach = false;
    }

    private int getErrorX() {
        int i = 0;
        float scale = this.mTextView.getResources().getDisplayMetrics().density;
        Drawables dr = this.mTextView.mDrawables;
        switch (this.mTextView.getLayoutDirection()) {
            case 1:
                if (dr != null) {
                    i = dr.mDrawableSizeLeft;
                }
                return this.mTextView.getPaddingLeft() + ((i / 2) - ((int) ((25.0f * scale) + LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS)));
            default:
                if (dr != null) {
                    i = dr.mDrawableSizeRight;
                }
                return ((this.mTextView.getWidth() - this.mErrorPopup.getWidth()) - this.mTextView.getPaddingRight()) + (((-i) / 2) + ((int) ((25.0f * scale) + LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS)));
        }
    }

    private int getErrorY() {
        int height;
        int compoundPaddingTop = this.mTextView.getCompoundPaddingTop();
        int vspace = ((this.mTextView.getBottom() - this.mTextView.getTop()) - this.mTextView.getCompoundPaddingBottom()) - compoundPaddingTop;
        Drawables dr = this.mTextView.mDrawables;
        switch (this.mTextView.getLayoutDirection()) {
            case 1:
                if (dr == null) {
                    height = 0;
                    break;
                }
                height = dr.mDrawableHeightLeft;
                break;
            default:
                if (dr == null) {
                    height = 0;
                    break;
                }
                height = dr.mDrawableHeightRight;
                break;
        }
        return (((compoundPaddingTop + ((vspace - height) / 2)) + height) - this.mTextView.getHeight()) - ((int) ((2.0f * this.mTextView.getResources().getDisplayMetrics().density) + LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS));
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
        return this.mCursorVisible ? this.mTextView.isTextEditable() : false;
    }

    void prepareCursorControllers() {
        boolean isCursorVisible;
        boolean z = false;
        boolean windowSupportsHandles = false;
        LayoutParams params = this.mTextView.getRootView().getLayoutParams();
        if (params instanceof WindowManager.LayoutParams) {
            WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) params;
            windowSupportsHandles = windowParams.type >= 1000 ? windowParams.type > WindowManager.LayoutParams.LAST_SUB_WINDOW : true;
        }
        boolean enabled = windowSupportsHandles && this.mTextView.getLayout() != null;
        if (enabled) {
            isCursorVisible = isCursorVisible();
        } else {
            isCursorVisible = false;
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
        if (this.mSuggestionsPopupWindow != null && (this.mTextView.isInExtractedMode() || (this.mSuggestionsPopupWindow.isShowingUp() ^ 1) != 0)) {
            this.mSuggestionsPopupWindow.hide();
        }
        hideInsertionPointCursorController();
    }

    private void updateSpellCheckSpans(int start, int end, boolean createSpellChecker) {
        this.mTextView.removeAdjacentSuggestionSpans(start);
        this.mTextView.removeAdjacentSuggestionSpans(end);
        if (this.mTextView.isTextEditable() && this.mTextView.isSuggestionsEnabled() && (this.mTextView.isInExtractedMode() ^ 1) != 0) {
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
            case 0:
                suspendBlink();
                return;
            case 1:
                resumeBlink();
                return;
            default:
                return;
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
        if ((this.mInputType & 15) == 1) {
            if (password || passwordInputType) {
                this.mInputType = (this.mInputType & -4081) | 128;
            }
            if (webPasswordInputType) {
                this.mInputType = (this.mInputType & -4081) | 224;
            }
        } else if ((this.mInputType & 15) == 2 && numberPasswordInputType) {
            this.mInputType = (this.mInputType & -4081) | 16;
        }
    }

    private void chooseSize(PopupWindow pop, CharSequence text, TextView tv) {
        int wid = tv.getPaddingLeft() + tv.getPaddingRight();
        int ht = tv.getPaddingTop() + tv.getPaddingBottom();
        CharSequence charSequence = text;
        Layout l = new StaticLayout(charSequence, tv.getPaint(), this.mTextView.getResources().getDimensionPixelSize(R.dimen.textview_error_popup_default_width), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
        float max = 0.0f;
        for (int i = 0; i < l.getLineCount(); i++) {
            max = Math.max(max, l.getLineWidth(i));
        }
        pop.setWidth(((int) Math.ceil((double) max)) + wid);
        pop.setHeight((l.getHeight() + ht) + this.mTextHeightOffset);
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
        if (retOffset == -1) {
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
        if (retOffset == -1) {
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
        if (klass == 2 || klass == 3 || klass == 4 || variation == 16 || variation == 32 || variation == 208 || variation == 176) {
            return true;
        }
        return false;
    }

    boolean selectCurrentWord() {
        if (!this.mTextView.canSelectText()) {
            return false;
        }
        if (needsToSelectAllToSelectWordOrParagraph()) {
            return this.mTextView.selectAllText();
        }
        long lastTouchOffsets = getLastTouchOffsets();
        int minOffset = TextUtils.unpackRangeStartFromLong(lastTouchOffsets);
        int maxOffset = TextUtils.unpackRangeEndFromLong(lastTouchOffsets);
        if (minOffset < 0 || minOffset > this.mTextView.getText().length()) {
            return false;
        }
        if (maxOffset < 0 || maxOffset > this.mTextView.getText().length()) {
            return false;
        }
        int selectionStart;
        int selectionEnd;
        boolean z;
        URLSpan[] urlSpans = (URLSpan[]) ((Spanned) this.mTextView.getText()).getSpans(minOffset, maxOffset, URLSpan.class);
        ImageSpan[] imageSpans = (ImageSpan[]) ((Spanned) this.mTextView.getText()).getSpans(minOffset, maxOffset, ImageSpan.class);
        if (urlSpans.length >= 1) {
            URLSpan urlSpan = urlSpans[0];
            selectionStart = ((Spanned) this.mTextView.getText()).getSpanStart(urlSpan);
            selectionEnd = ((Spanned) this.mTextView.getText()).getSpanEnd(urlSpan);
        } else if (imageSpans.length >= 1) {
            ImageSpan imageSpan = imageSpans[0];
            selectionStart = ((Spanned) this.mTextView.getText()).getSpanStart(imageSpan);
            selectionEnd = ((Spanned) this.mTextView.getText()).getSpanEnd(imageSpan);
        } else {
            WordIterator wordIterator = getWordIterator();
            wordIterator.setCharSequence(this.mTextView.getText(), minOffset, maxOffset);
            selectionStart = wordIterator.getBeginning(minOffset);
            selectionEnd = wordIterator.getEnd(maxOffset);
            if (selectionStart == -1 || selectionEnd == -1 || selectionStart == selectionEnd) {
                long range = getCharClusterRange(minOffset);
                selectionStart = TextUtils.unpackRangeStartFromLong(range);
                selectionEnd = TextUtils.unpackRangeEndFromLong(range);
            }
        }
        Selection.setSelection((Spannable) this.mTextView.getText(), selectionStart, selectionEnd);
        if (selectionEnd > selectionStart) {
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    private boolean selectCurrentParagraph() {
        if (!this.mTextView.canSelectText()) {
            return false;
        }
        if (needsToSelectAllToSelectWordOrParagraph()) {
            return this.mTextView.selectAllText();
        }
        long lastTouchOffsets = getLastTouchOffsets();
        long paragraphsRange = getParagraphsRange(TextUtils.unpackRangeStartFromLong(lastTouchOffsets), TextUtils.unpackRangeEndFromLong(lastTouchOffsets));
        int start = TextUtils.unpackRangeStartFromLong(paragraphsRange);
        int end = TextUtils.unpackRangeEndFromLong(paragraphsRange);
        if (start >= end) {
            return false;
        }
        Selection.setSelection((Spannable) this.mTextView.getText(), start, end);
        return true;
    }

    private long getParagraphsRange(int startOffset, int endOffset) {
        Layout layout = this.mTextView.getLayout();
        if (layout == null) {
            return TextUtils.packRangeInLong(-1, -1);
        }
        CharSequence text = this.mTextView.getText();
        int minLine = layout.getLineForOffset(startOffset);
        while (minLine > 0 && text.charAt(layout.getLineEnd(minLine - 1) - 1) != 10) {
            minLine--;
        }
        int maxLine = layout.getLineForOffset(endOffset);
        while (maxLine < layout.getLineCount() - 1 && text.charAt(layout.getLineEnd(maxLine) - 1) != 10) {
            maxLine++;
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
            this.mWordIteratorWithText.setCharSequence(text, 0, text.length());
            this.mUpdateWordIteratorText = false;
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
            return TextUtils.packRangeInLong(getNextCursorOffset(clusterEndOffset, false), clusterEndOffset);
        } else if (offset - 1 < 0) {
            return TextUtils.packRangeInLong(offset, offset);
        } else {
            int clusterStartOffset = getNextCursorOffset(offset, false);
            return TextUtils.packRangeInLong(clusterStartOffset, getNextCursorOffset(clusterStartOffset, true));
        }
    }

    private boolean touchPositionIsInSelection() {
        int selectionStart = this.mTextView.getSelectionStart();
        int selectionEnd = this.mTextView.getSelectionEnd();
        if (selectionStart == selectionEnd) {
            return false;
        }
        if (selectionStart > selectionEnd) {
            int tmp = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = tmp;
            Selection.setSelection((Spannable) this.mTextView.getText(), selectionStart, tmp);
        }
        SelectionModifierCursorController selectionController = getSelectionController();
        boolean z = selectionController.getMinTouchOffset() >= selectionStart && selectionController.getMaxTouchOffset() < selectionEnd;
        return z;
    }

    private PositionListener getPositionListener() {
        if (this.mPositionListener == null) {
            this.mPositionListener = new PositionListener(this, null);
        }
        return this.mPositionListener;
    }

    private boolean isOffsetVisible(int offset) {
        Layout layout = this.mTextView.getLayout();
        if (layout == null) {
            return false;
        }
        return this.mTextView.isPositionVisible((float) (this.mTextView.viewportToContentHorizontalOffset() + ((int) layout.getPrimaryHorizontal(offset))), (float) (this.mTextView.viewportToContentVerticalOffset() + layout.getLineBottom(layout.getLineForOffset(offset))));
    }

    private boolean isPositionOnText(float x, float y) {
        Layout layout = this.mTextView.getLayout();
        if (layout == null) {
            return false;
        }
        int line = this.mTextView.getLineAtCoordinate(y);
        x = this.mTextView.convertToLocalHorizontalCoordinate(x);
        if (x >= layout.getLineLeft(line) && x <= layout.getLineRight(line)) {
            return true;
        }
        return false;
    }

    private void startDragAndDrop() {
        if (!this.mTextView.isInExtractedMode()) {
            int start = this.mTextView.getSelectionStart();
            int end = this.mTextView.getSelectionEnd();
            this.mTextView.startDragAndDrop(ClipData.newPlainText(null, this.mTextView.getTransformedText(start, end)), getTextThumbnailBuilder(start, end), new DragLocalState(this.mTextView, start, end), 256);
            stopTextActionMode();
            if (hasSelectionController()) {
                getSelectionController().resetTouchOffsets();
            }
        }
    }

    public boolean performLongClick(boolean handled) {
        if (!(handled || (isPositionOnText(this.mLastDownPositionX, this.mLastDownPositionY) ^ 1) == 0 || !this.mInsertionControllerEnabled)) {
            Selection.setSelection((Spannable) this.mTextView.getText(), this.mTextView.getOffsetForPosition(this.mLastDownPositionX, this.mLastDownPositionY));
            getInsertionController().show();
            this.mIsInsertionActionModeStartPending = true;
            handled = true;
            MetricsLogger.action(this.mTextView.getContext(), (int) MetricsEvent.TEXT_LONGPRESS, 0);
        }
        if (!(handled || this.mTextActionMode == null)) {
            if (touchPositionIsInSelection()) {
                startDragAndDrop();
                MetricsLogger.action(this.mTextView.getContext(), (int) MetricsEvent.TEXT_LONGPRESS, 2);
            } else {
                stopTextActionMode();
                selectCurrentWordAndStartDrag();
                MetricsLogger.action(this.mTextView.getContext(), (int) MetricsEvent.TEXT_LONGPRESS, 1);
            }
            handled = true;
        }
        if (!handled) {
            handled = selectCurrentWordAndStartDrag();
            if (handled) {
                MetricsLogger.action(this.mTextView.getContext(), (int) MetricsEvent.TEXT_LONGPRESS, 1);
            }
        }
        return handled;
    }

    private long getLastTouchOffsets() {
        SelectionModifierCursorController selectionController = getSelectionController();
        return TextUtils.packRangeInLong(selectionController.getMinTouchOffset(), selectionController.getMaxTouchOffset());
    }

    void onFocusChanged(boolean focused, int direction) {
        this.mShowCursor = SystemClock.uptimeMillis();
        ensureEndedBatchEdit();
        if (focused) {
            boolean z;
            int selStart = this.mTextView.getSelectionStart();
            int selEnd = this.mTextView.getSelectionEnd();
            boolean isFocusHighlighted = (this.mSelectAllOnFocus && selStart == 0) ? selEnd == this.mTextView.getText().length() : false;
            if (this.mFrozenWithFocus && this.mTextView.hasSelection()) {
                z = isFocusHighlighted ^ 1;
            } else {
                z = false;
            }
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
            this.mFrozenWithFocus = false;
            this.mSelectionMoved = false;
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
            SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) spannable.getSpans(0, spannable.length(), SuggestionSpan.class);
            for (int i = 0; i < suggestionSpans.length; i++) {
                int flags = suggestionSpans[i].getFlags();
                if ((flags & 1) != 0 && (flags & 2) == 0) {
                    suggestionSpans[i].setFlags(flags & -2);
                }
            }
        }
    }

    void sendOnTextChanged(int start, int after) {
        updateSpellCheckSpans(start, start + after, false);
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
        return -1;
    }

    void onWindowFocusChanged(boolean hasWindowFocus) {
        if (hasWindowFocus) {
            if (this.mBlink != null) {
                this.mBlink.uncancel();
                makeBlink();
            }
            if (this.mTextView.hasSelection() && (extractedTextModeWillBeStarted() ^ 1) != 0) {
                refreshTextActionMode();
                return;
            }
            return;
        }
        if (this.mBlink != null) {
            this.mBlink.cancel();
        }
        if (this.mInputContentType != null) {
            this.mInputContentType.enterDown = false;
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
            if ((this.mTapState != 1 && (this.mTapState != 2 || !isMouse)) || SystemClock.uptimeMillis() - this.mLastTouchUpTime > ((long) ViewConfiguration.getDoubleTapTimeout())) {
                this.mTapState = 1;
            } else if (this.mTapState == 1) {
                this.mTapState = 2;
            } else {
                this.mTapState = 3;
            }
        }
        if (action == 1) {
            this.mLastTouchUpTime = SystemClock.uptimeMillis();
        }
    }

    private boolean shouldFilterOutTouchEvent(MotionEvent event) {
        if (!event.isFromSource(InputDevice.SOURCE_MOUSE)) {
            return false;
        }
        boolean primaryButtonStateChanged = ((this.mLastButtonState ^ event.getButtonState()) & 1) != 0;
        int action = event.getActionMasked();
        if ((action == 0 || action == 1) && (primaryButtonStateChanged ^ 1) != 0) {
            return true;
        }
        return action == 2 && (event.isButtonPressed(1) ^ 1) != 0;
    }

    void onTouchEvent(MotionEvent event) {
        boolean filterOutEvent = shouldFilterOutTouchEvent(event);
        this.mLastButtonState = event.getButtonState();
        if (filterOutEvent) {
            if (event.getActionMasked() == 1) {
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
            this.mTouchFocusSelected = false;
            this.mIgnoreActionUpEvent = false;
        }
    }

    private void updateFloatingToolbarVisibility(MotionEvent event) {
        if (this.mTextActionMode != null) {
            switch (event.getActionMasked()) {
                case 1:
                case 3:
                    showFloatingToolbar();
                    return;
                case 2:
                    hideFloatingToolbar();
                    return;
                default:
                    return;
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
            invalidateActionModeAsync();
        }
    }

    public void beginBatchEdit() {
        this.mInBatchEditControllers = true;
        InputMethodState ims = this.mInputMethodState;
        if (ims != null) {
            int nesting = ims.mBatchEditNesting + 1;
            ims.mBatchEditNesting = nesting;
            if (nesting == 1) {
                ims.mCursorChanged = false;
                ims.mChangedDelta = 0;
                if (ims.mContentChanged) {
                    ims.mChangedStart = 0;
                    ims.mChangedEnd = this.mTextView.getText().length();
                } else {
                    ims.mChangedStart = -1;
                    ims.mChangedEnd = -1;
                    ims.mContentChanged = false;
                }
                this.mUndoInputFilter.beginBatchEdit();
                this.mTextView.onBeginBatchEdit();
            }
        }
    }

    public void endBatchEdit() {
        this.mInBatchEditControllers = false;
        InputMethodState ims = this.mInputMethodState;
        if (ims != null) {
            int nesting = ims.mBatchEditNesting - 1;
            ims.mBatchEditNesting = nesting;
            if (nesting == 0) {
                finishBatchEdit(ims);
            }
        }
    }

    void ensureEndedBatchEdit() {
        InputMethodState ims = this.mInputMethodState;
        if (ims != null && ims.mBatchEditNesting != 0) {
            ims.mBatchEditNesting = 0;
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
            if (cursorController != null && (cursorController.isActive() ^ 1) != 0 && (cursorController.isCursorBeingModified() ^ 1) != 0) {
                cursorController.show();
            }
        }
    }

    boolean extractText(ExtractedTextRequest request, ExtractedText outText) {
        return extractTextInternal(request, -1, -1, -1, outText);
    }

    private boolean extractTextInternal(ExtractedTextRequest request, int partialStartOffset, int partialEndOffset, int delta, ExtractedText outText) {
        if (request == null || outText == null) {
            return false;
        }
        CharSequence content = this.mTextView.getText();
        if (content == null) {
            return false;
        }
        if (partialStartOffset != -2) {
            int N = content.length();
            if (partialStartOffset < 0) {
                outText.partialEndOffset = -1;
                outText.partialStartOffset = -1;
                partialStartOffset = 0;
                partialEndOffset = N;
            } else {
                partialEndOffset += delta;
                if (content instanceof Spanned) {
                    Spanned spanned = (Spanned) content;
                    Object[] spans = spanned.getSpans(partialStartOffset, partialEndOffset, ParcelableSpan.class);
                    int i = spans.length;
                    while (i > 0) {
                        i--;
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
                    partialStartOffset = 0;
                }
                if (partialEndOffset > N) {
                    partialEndOffset = N;
                } else if (partialEndOffset < 0) {
                    partialEndOffset = 0;
                }
            }
            if ((request.flags & 1) != 0) {
                outText.text = content.subSequence(partialStartOffset, partialEndOffset);
            } else {
                outText.text = TextUtils.substring(content, partialStartOffset, partialEndOffset);
            }
        } else {
            outText.partialStartOffset = 0;
            outText.partialEndOffset = 0;
            outText.text = LogException.NO_VALUE;
        }
        outText.flags = 0;
        if (MetaKeyKeyListener.getMetaState(content, 2048) != 0) {
            outText.flags |= 2;
        }
        if (this.mTextView.isSingleLine()) {
            outText.flags |= 1;
        }
        outText.startOffset = 0;
        outText.selectionStart = this.mTextView.getSelectionStart();
        outText.selectionEnd = this.mTextView.getSelectionEnd();
        return true;
    }

    boolean reportExtractedText() {
        InputMethodState ims = this.mInputMethodState;
        if (ims != null) {
            boolean contentChanged = ims.mContentChanged;
            if (contentChanged || ims.mSelectionModeChanged) {
                ims.mContentChanged = false;
                ims.mSelectionModeChanged = false;
                ExtractedTextRequest req = ims.mExtractedTextRequest;
                if (req != null) {
                    InputMethodManager imm = InputMethodManager.peekInstance();
                    if (imm != null) {
                        if (ims.mChangedStart < 0 && (contentChanged ^ 1) != 0) {
                            ims.mChangedStart = -2;
                        }
                        if (extractTextInternal(req, ims.mChangedStart, ims.mChangedEnd, ims.mChangedDelta, ims.mExtractedText)) {
                            imm.updateExtractedText(this.mTextView, req.token, ims.mExtractedText);
                            ims.mChangedStart = -1;
                            ims.mChangedEnd = -1;
                            ims.mChangedDelta = 0;
                            ims.mContentChanged = false;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void sendUpdateSelection() {
        if (this.mInputMethodState != null && this.mInputMethodState.mBatchEditNesting <= 0) {
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null) {
                int selectionStart = this.mTextView.getSelectionStart();
                int selectionEnd = this.mTextView.getSelectionEnd();
                int candStart = -1;
                int candEnd = -1;
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
                int i;
                int blockIndex;
                if (this.mTextRenderNodes == null) {
                    this.mTextRenderNodes = (TextRenderNode[]) ArrayUtils.emptyArray(TextRenderNode.class);
                }
                DynamicLayout dynamicLayout = (DynamicLayout) layout;
                int[] blockEndLines = dynamicLayout.getBlockEndLines();
                int[] blockIndices = dynamicLayout.getBlockIndices();
                int numberOfBlocks = dynamicLayout.getNumberOfBlocks();
                int indexFirstChangedBlock = dynamicLayout.getIndexFirstChangedBlock();
                ArraySet<Integer> blockSet = dynamicLayout.getBlocksAlwaysNeedToBeRedrawn();
                if (blockSet != null) {
                    for (i = 0; i < blockSet.size(); i++) {
                        blockIndex = dynamicLayout.getBlockIndex(((Integer) blockSet.valueAt(i)).intValue());
                        if (!(blockIndex == -1 || this.mTextRenderNodes[blockIndex] == null)) {
                            this.mTextRenderNodes[blockIndex].needsToBeShifted = true;
                        }
                    }
                }
                int startBlock = Arrays.binarySearch(blockEndLines, 0, numberOfBlocks, firstLine);
                if (startBlock < 0) {
                    startBlock = -(startBlock + 1);
                }
                int startIndexToFindAvailableRenderNode = 0;
                int lastIndex = numberOfBlocks;
                for (i = Math.min(indexFirstChangedBlock, startBlock); i < numberOfBlocks; i++) {
                    blockIndex = blockIndices[i];
                    if (!(i < indexFirstChangedBlock || blockIndex == -1 || this.mTextRenderNodes[blockIndex] == null)) {
                        this.mTextRenderNodes[blockIndex].needsToBeShifted = true;
                    }
                    if (blockEndLines[i] >= firstLine) {
                        startIndexToFindAvailableRenderNode = drawHardwareAcceleratedInner(canvas, layout, highlight, highlightPaint, cursorOffsetVertical, blockEndLines, blockIndices, i, numberOfBlocks, startIndexToFindAvailableRenderNode);
                        if (blockEndLines[i] >= lastLine) {
                            lastIndex = Math.max(indexFirstChangedBlock, i + 1);
                            break;
                        }
                    }
                }
                if (blockSet != null) {
                    for (i = 0; i < blockSet.size(); i++) {
                        int block = ((Integer) blockSet.valueAt(i)).intValue();
                        blockIndex = dynamicLayout.getBlockIndex(block);
                        if (blockIndex == -1 || this.mTextRenderNodes[blockIndex] == null || this.mTextRenderNodes[blockIndex].needsToBeShifted) {
                            startIndexToFindAvailableRenderNode = drawHardwareAcceleratedInner(canvas, layout, highlight, highlightPaint, cursorOffsetVertical, blockEndLines, blockIndices, block, numberOfBlocks, startIndexToFindAvailableRenderNode);
                        }
                    }
                }
                dynamicLayout.setIndexFirstChangedBlock(lastIndex);
            } else {
                layout.drawText(canvas, firstLine, lastLine);
            }
        }
    }

    private int drawHardwareAcceleratedInner(Canvas canvas, Layout layout, Path highlight, Paint highlightPaint, int cursorOffsetVertical, int[] blockEndLines, int[] blockIndices, int blockInfoIndex, int numberOfBlocks, int startIndexToFindAvailableRenderNode) {
        int blockEndLine = blockEndLines[blockInfoIndex];
        int blockIndex = blockIndices[blockInfoIndex];
        if (blockIndex == -1) {
            blockIndex = getAvailableDisplayListIndex(blockIndices, numberOfBlocks, startIndexToFindAvailableRenderNode);
            blockIndices[blockInfoIndex] = blockIndex;
            if (this.mTextRenderNodes[blockIndex] != null) {
                this.mTextRenderNodes[blockIndex].isDirty = true;
            }
            startIndexToFindAvailableRenderNode = blockIndex + 1;
        }
        if (this.mTextRenderNodes[blockIndex] == null) {
            this.mTextRenderNodes[blockIndex] = new TextRenderNode("Text " + blockIndex);
        }
        boolean blockDisplayListIsInvalid = this.mTextRenderNodes[blockIndex].needsRecord();
        RenderNode blockDisplayList = this.mTextRenderNodes[blockIndex].renderNode;
        if (this.mTextRenderNodes[blockIndex].needsToBeShifted || blockDisplayListIsInvalid) {
            int blockBeginLine = blockInfoIndex == 0 ? 0 : blockEndLines[blockInfoIndex - 1] + 1;
            int top = layout.getLineTop(blockBeginLine);
            int bottom = layout.getLineBottom(blockEndLine);
            int left = 0;
            int right = this.mTextView.getWidth();
            if (this.mTextView.getHorizontallyScrolling()) {
                float min = Float.MAX_VALUE;
                float max = Float.MIN_VALUE;
                for (int line = blockBeginLine; line <= blockEndLine; line++) {
                    min = Math.min(min, layout.getLineLeft(line));
                    max = Math.max(max, layout.getLineRight(line));
                }
                left = (int) min;
                right = (int) (LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS + max);
            }
            if (blockDisplayListIsInvalid) {
                DisplayListCanvas displayListCanvas = blockDisplayList.start(right - left, bottom - top);
                try {
                    displayListCanvas.translate((float) (-left), (float) (-top));
                    layout.drawText(displayListCanvas, blockBeginLine, blockEndLine);
                    this.mTextRenderNodes[blockIndex].isDirty = false;
                } finally {
                    blockDisplayList.end(displayListCanvas);
                    blockDisplayList.setClipToBounds(false);
                }
            }
            blockDisplayList.setLeftTopRightBottom(left, top, right, bottom);
            this.mTextRenderNodes[blockIndex].needsToBeShifted = false;
        }
        ((DisplayListCanvas) canvas).drawRenderNode(blockDisplayList);
        return startIndexToFindAvailableRenderNode;
    }

    private int getAvailableDisplayListIndex(int[] blockIndices, int numberOfBlocks, int searchStartIndex) {
        int length = this.mTextRenderNodes.length;
        for (int i = searchStartIndex; i < length; i++) {
            boolean blockIndexFound = false;
            for (int j = 0; j < numberOfBlocks; j++) {
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
        boolean translate = cursorOffsetVertical != 0;
        if (translate) {
            canvas.translate(0.0f, (float) cursorOffsetVertical);
        }
        for (int i = 0; i < this.mCursorCount; i++) {
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
            invalidateActionMode();
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
            int i = 0;
            while (i < numberOfBlocks && blockEndLines[i] < firstLine) {
                i++;
            }
            while (i < numberOfBlocks) {
                int blockIndex = blockIndices[i];
                if (blockIndex != -1) {
                    this.mTextRenderNodes[blockIndex].isDirty = true;
                }
                if (blockEndLines[i] < lastLine) {
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    void invalidateTextDisplayList() {
        if (this.mTextRenderNodes != null) {
            for (int i = 0; i < this.mTextRenderNodes.length; i++) {
                if (this.mTextRenderNodes[i] != null) {
                    this.mTextRenderNodes[i].isDirty = true;
                }
            }
        }
    }

    protected void updateCursorsPositions() {
        if (this.mTextView.mCursorDrawableRes == 0) {
            this.mCursorCount = 0;
            return;
        }
        int i;
        Layout layout = this.mTextView.getLayout();
        int offset = this.mTextView.getSelectionStart();
        int line = layout.getLineForOffset(offset);
        int top = layout.getLineTop(line);
        int bottom = layout.getLineTop(line + 1);
        float PositionX = layout.getPrimaryHorizontal(offset, layout.shouldClampCursor(line));
        if (adjustCursorPos(line, layout)) {
            top = getCursorTop();
            bottom = getCursorBottom();
            PositionX = getCursorX();
        }
        if (layout.isLevelBoundary(offset)) {
            i = 2;
        } else {
            i = 1;
        }
        this.mCursorCount = i;
        int middle = bottom;
        if (this.mCursorCount == 2) {
            middle = (top + bottom) >> 1;
        }
        updateCursorPosition(0, top, middle, PositionX);
        if (this.mCursorCount == 2) {
            updateCursorPosition(1, middle, bottom, PositionX);
        }
    }

    void refreshTextActionMode() {
        if (extractedTextModeWillBeStarted()) {
            this.mRestartActionModeOnNextRefresh = false;
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
                        startSelectionActionModeAsync(false);
                    }
                } else if (selectionController == null || (selectionController.isActive() ^ 1) != 0) {
                    stopTextActionModeWithPreservingSelection();
                    startSelectionActionModeAsync(false);
                } else {
                    this.mTextActionMode.invalidateContentRect();
                }
            } else if (insertionController == null || (insertionController.isActive() ^ 1) != 0) {
                stopTextActionMode();
            } else if (this.mTextActionMode != null) {
                this.mTextActionMode.invalidateContentRect();
            }
            this.mRestartActionModeOnNextRefresh = false;
            return;
        }
        this.mRestartActionModeOnNextRefresh = false;
    }

    void startInsertionActionMode() {
        if (this.mInsertionActionModeRunnable != null) {
            this.mTextView.removeCallbacks(this.mInsertionActionModeRunnable);
        }
        if (!extractedTextModeWillBeStarted()) {
            stopTextActionMode();
            this.mTextActionMode = this.mTextView.startActionMode(new TextActionModeCallback(false), 1);
            if (!(this.mTextActionMode == null || getInsertionController() == null)) {
                getInsertionController().show();
            }
        }
    }

    TextView getTextView() {
        return this.mTextView;
    }

    ActionMode getTextActionMode() {
        return this.mTextActionMode;
    }

    void setRestartActionModeOnNextRefresh(boolean value) {
        this.mRestartActionModeOnNextRefresh = value;
    }

    void startSelectionActionModeAsync(boolean adjustSelection) {
        getSelectionActionModeHelper().startActionModeAsync(adjustSelection);
    }

    protected void invalidateActionModeAsync() {
        getSelectionActionModeHelper().invalidateActionModeAsync();
    }

    private void invalidateActionMode() {
        if (this.mTextActionMode != null) {
            this.mTextActionMode.invalidate();
        }
    }

    private SelectionActionModeHelper getSelectionActionModeHelper() {
        if (this.mSelectionActionModeHelper == null) {
            this.mSelectionActionModeHelper = new SelectionActionModeHelper(this);
        }
        return this.mSelectionActionModeHelper;
    }

    private boolean selectCurrentWordAndStartDrag() {
        if (this.mInsertionActionModeRunnable != null) {
            this.mTextView.removeCallbacks(this.mInsertionActionModeRunnable);
        }
        if (extractedTextModeWillBeStarted() || !checkField()) {
            return false;
        }
        if (!this.mTextView.hasSelection() && (selectCurrentWord() ^ 1) != 0) {
            return false;
        }
        stopTextActionModeWithPreservingSelection();
        getSelectionController().enterDrag(2);
        return true;
    }

    boolean checkField() {
        if (this.mTextView.canSelectText() && (this.mTextView.requestFocus() ^ 1) == 0) {
            return true;
        }
        Log.w("TextView", "TextView does not support text selection. Selection cancelled.");
        return false;
    }

    boolean startSelectionActionModeInternal() {
        if (extractedTextModeWillBeStarted()) {
            return false;
        }
        if (this.mTextActionMode != null) {
            invalidateActionMode();
            return false;
        } else if (!checkField() || (this.mTextView.hasSelection() ^ 1) != 0) {
            return false;
        } else {
            this.mTextActionMode = this.mTextView.startActionMode(new TextActionModeCallback(true), 1);
            boolean selectionStarted = this.mTextActionMode != null;
            if (selectionStarted && (this.mTextView.isTextSelectable() ^ 1) != 0 && this.mShowSoftInputOnFocus) {
                InputMethodManager imm = InputMethodManager.peekInstance();
                if (imm != null) {
                    imm.showSoftInput(this.mTextView, 0, null);
                }
            }
            return selectionStarted;
        }
    }

    private boolean extractedTextModeWillBeStarted() {
        boolean z = false;
        if (this.mTextView.isInExtractedMode()) {
            return false;
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
            return false;
        }
        Spannable spannable = (Spannable) text;
        int selectionStart = this.mTextView.getSelectionStart();
        int selectionEnd = this.mTextView.getSelectionEnd();
        SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) spannable.getSpans(selectionStart, selectionEnd, SuggestionSpan.class);
        if (suggestionSpans.length == 0) {
            return false;
        }
        int i;
        if (selectionStart == selectionEnd) {
            for (SuggestionSpan suggestions : suggestionSpans) {
                if (suggestions.getSuggestions().length > 0) {
                    return true;
                }
            }
            return false;
        }
        int minSpanStart = this.mTextView.getText().length();
        int maxSpanEnd = 0;
        int unionOfSpansCoveringSelectionStartStart = this.mTextView.getText().length();
        int unionOfSpansCoveringSelectionStartEnd = 0;
        boolean hasValidSuggestions = false;
        for (i = 0; i < suggestionSpans.length; i++) {
            int spanStart = spannable.getSpanStart(suggestionSpans[i]);
            int spanEnd = spannable.getSpanEnd(suggestionSpans[i]);
            minSpanStart = Math.min(minSpanStart, spanStart);
            maxSpanEnd = Math.max(maxSpanEnd, spanEnd);
            if (selectionStart >= spanStart && selectionStart <= spanEnd) {
                hasValidSuggestions = hasValidSuggestions || suggestionSpans[i].getSuggestions().length > 0;
                unionOfSpansCoveringSelectionStartStart = Math.min(unionOfSpansCoveringSelectionStartStart, spanStart);
                unionOfSpansCoveringSelectionStartEnd = Math.max(unionOfSpansCoveringSelectionStartEnd, spanEnd);
            }
        }
        if (!hasValidSuggestions) {
            return false;
        }
        if (unionOfSpansCoveringSelectionStartStart >= unionOfSpansCoveringSelectionStartEnd) {
            return false;
        }
        if (minSpanStart < unionOfSpansCoveringSelectionStartStart || maxSpanEnd > unionOfSpansCoveringSelectionStartEnd) {
            return false;
        }
        return true;
    }

    private boolean isCursorInsideEasyCorrectionSpan() {
        SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) ((Spannable) this.mTextView.getText()).getSpans(this.mTextView.getSelectionStart(), this.mTextView.getSelectionEnd(), SuggestionSpan.class);
        for (SuggestionSpan flags : suggestionSpans) {
            if ((flags.getFlags() & 1) != 0) {
                return true;
            }
        }
        return false;
    }

    void onTouchUpEvent(MotionEvent event) {
        if (!getSelectionActionModeHelper().resetSelection(getTextView().getOffsetForPosition(event.getX(), event.getY()))) {
            boolean selectAllGotFocus = this.mSelectAllOnFocus ? this.mTextView.didTouchFocusSelect() : false;
            hideCursorAndSpanControllers();
            stopTextActionMode();
            CharSequence text = this.mTextView.getText();
            if (!selectAllGotFocus && text.length() > 0) {
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
                                    Log.e("Editor", "Widget of Editor resource not found issue.", e);
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
        this.mPreserveSelection = false;
    }

    boolean hasInsertionController() {
        return this.mInsertionControllerEnabled;
    }

    boolean hasSelectionController() {
        return this.mSelectionControllerEnabled;
    }

    private InsertionPointCursorController getInsertionController() {
        if (!this.mInsertionControllerEnabled) {
            return null;
        }
        if (this.mInsertionPointCursorController == null) {
            this.mInsertionPointCursorController = new InsertionPointCursorController(this, null);
            this.mTextView.getViewTreeObserver().addOnTouchModeChangeListener(this.mInsertionPointCursorController);
        }
        return this.mInsertionPointCursorController;
    }

    SelectionModifierCursorController getSelectionController() {
        if (!this.mSelectionControllerEnabled) {
            return null;
        }
        if (this.mSelectionModifierCursorController == null) {
            this.mSelectionModifierCursorController = new SelectionModifierCursorController();
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
        int drawableWidth = 0;
        if (drawable != null) {
            drawable.getPadding(this.mTempRect);
            drawableWidth = drawable.getIntrinsicWidth();
        } else {
            this.mTempRect.setEmpty();
        }
        int scrollX = this.mTextView.getScrollX();
        float horizontalDiff = horizontal - ((float) scrollX);
        int viewClippedWidth = (this.mTextView.getWidth() - this.mTextView.getCompoundPaddingLeft()) - this.mTextView.getCompoundPaddingRight();
        if (horizontalDiff >= ((float) viewClippedWidth) - 1.0f) {
            return (viewClippedWidth + scrollX) - (drawableWidth - this.mTempRect.right);
        }
        if (Math.abs(horizontalDiff) <= 1.0f || (TextUtils.isEmpty(this.mTextView.getText()) && ((float) (1048576 - scrollX)) <= ((float) viewClippedWidth) + 1.0f && horizontal <= 1.0f)) {
            return scrollX - this.mTempRect.left;
        }
        return ((int) horizontal) - this.mTempRect.left;
    }

    public void onCommitCorrection(CorrectionInfo info) {
        if (this.mCorrectionHighlighter == null) {
            this.mCorrectionHighlighter = new CorrectionHighlighter();
        } else {
            this.mCorrectionHighlighter.invalidate(false);
        }
        this.mCorrectionHighlighter.highlight(info);
        this.mUndoInputFilter.freezeLastEdit();
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
        boolean z = false;
        if (!isCursorVisible() || (this.mTextView.isFocused() ^ 1) != 0) {
            return false;
        }
        int start = this.mTextView.getSelectionStart();
        if (start < 0) {
            return false;
        }
        int end = this.mTextView.getSelectionEnd();
        if (end < 0) {
            return false;
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
                this.mBlink = new Blink(this, null);
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
        if (end - start > 20) {
            end = TextUtils.unpackRangeEndFromLong(getCharClusterRange(start + 20));
        }
        shadowView.setText(this.mTextView.getTransformedText(start, end));
        shadowView.setTextColor(this.mTextView.getTextColors());
        shadowView.setTextAppearance(16);
        shadowView.setGravity(17);
        shadowView.-wrap18(new LayoutParams(-2, -2));
        int size = MeasureSpec.makeMeasureSpec(0, 0);
        shadowView.measure(size, size);
        shadowView.layout(0, 0, shadowView.getMeasuredWidth(), shadowView.getMeasuredHeight());
        shadowView.invalidate();
        return new DragShadowBuilder(shadowView);
    }

    void onDrop(DragEvent event) {
        SpannableStringBuilder content = new SpannableStringBuilder();
        DragAndDropPermissions permissions = DragAndDropPermissions.obtain(event);
        if (permissions != null) {
            permissions.takeTransient();
        }
        try {
            ClipData clipData = event.getClipData();
            int itemCount = clipData.getItemCount();
            for (int i = 0; i < itemCount; i++) {
                content.append(clipData.getItemAt(i).coerceToStyledText(this.mTextView.getContext()));
            }
            this.mTextView.beginBatchEdit();
            this.mUndoInputFilter.freezeLastEdit();
            try {
                boolean dragDropIntoItself;
                int offset = this.mTextView.getOffsetForPosition(event.getX(), event.getY());
                Object localState = event.getLocalState();
                DragLocalState dragLocalState = null;
                if (localState instanceof DragLocalState) {
                    dragLocalState = (DragLocalState) localState;
                }
                if (dragLocalState != null) {
                    dragDropIntoItself = dragLocalState.sourceTextView == this.mTextView;
                } else {
                    dragDropIntoItself = false;
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
                        this.mTextView.deleteText_internal(dragSourceStart, dragSourceEnd);
                        int prevCharIdx = Math.max(0, dragSourceStart - 1);
                        int nextCharIdx = Math.min(this.mTextView.getText().length(), dragSourceStart + 1);
                        if (nextCharIdx > prevCharIdx + 1) {
                            CharSequence t = this.mTextView.getTransformedText(prevCharIdx, nextCharIdx);
                            if (Character.isSpaceChar(t.charAt(0)) && Character.isSpaceChar(t.charAt(1))) {
                                this.mTextView.deleteText_internal(prevCharIdx, prevCharIdx + 1);
                            }
                        }
                    }
                    this.mTextView.endBatchEdit();
                    this.mUndoInputFilter.freezeLastEdit();
                }
            } finally {
                this.mTextView.endBatchEdit();
                this.mUndoInputFilter.freezeLastEdit();
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
            text.setSpan(this.mKeyListener, 0, textLength, 18);
        }
        if (this.mSpanController == null) {
            this.mSpanController = new SpanController(this, null);
        }
        text.setSpan(this.mSpanController, 0, textLength, 18);
    }

    void setContextMenuAnchor(float x, float y) {
        this.mContextMenuAnchorX = x;
        this.mContextMenuAnchorY = y;
    }

    void onCreateContextMenu(ContextMenu menu) {
        if (!this.mIsBeingLongClicked && !Float.isNaN(this.mContextMenuAnchorX) && !Float.isNaN(this.mContextMenuAnchorY)) {
            int offset = this.mTextView.getOffsetForPosition(this.mContextMenuAnchorX, this.mContextMenuAnchorY);
            if (offset != -1) {
                stopTextActionModeWithPreservingSelection();
                if (this.mTextView.canSelectText()) {
                    boolean isOnSelection = (!this.mTextView.hasSelection() || offset < this.mTextView.getSelectionStart()) ? false : offset <= this.mTextView.getSelectionEnd();
                    if (!isOnSelection) {
                        Selection.setSelection((Spannable) this.mTextView.getText(), offset);
                        stopTextActionMode();
                    }
                }
                if (shouldOfferToShowSuggestions()) {
                    int i;
                    SuggestionInfo[] suggestionInfoArray = new SuggestionInfo[5];
                    for (i = 0; i < suggestionInfoArray.length; i++) {
                        suggestionInfoArray[i] = new SuggestionInfo();
                    }
                    SubMenu subMenu = menu.addSubMenu(0, 0, 10, (int) R.string.replace);
                    int numItems = this.mSuggestionHelper.getSuggestionInfo(suggestionInfoArray, null);
                    for (i = 0; i < numItems; i++) {
                        final SuggestionInfo info = suggestionInfoArray[i];
                        subMenu.add(0, 0, i, info.mText).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                Editor.this.replaceWithSuggestion(info);
                                return true;
                            }
                        });
                    }
                }
                menu.add(0, (int) R.id.undo, 3, (int) R.string.undo).setAlphabeticShortcut(DateFormat.TIME_ZONE).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener).setEnabled(this.mTextView.canUndo());
                menu.add(0, (int) R.id.redo, 4, (int) R.string.redo).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener).setEnabled(this.mTextView.canRedo());
                menu.add(0, (int) R.id.cut, 6, (int) R.string.cut).setAlphabeticShortcut(StateProperty.TARGET_X).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener).setEnabled(this.mTextView.canCut());
                menu.add(0, (int) R.id.copy, 7, (int) R.string.copy).setAlphabeticShortcut('c').setOnMenuItemClickListener(this.mOnContextMenuItemClickListener).setEnabled(this.mTextView.canCopy());
                menu.add(0, (int) R.id.paste, 8, (int) R.string.paste).setAlphabeticShortcut('v').setEnabled(this.mTextView.canPaste()).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener);
                menu.add(0, (int) R.id.pasteAsPlainText, 9, (int) R.string.paste_as_plain_text).setEnabled(this.mTextView.canPasteAsPlainText()).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener);
                menu.add(0, (int) R.id.shareText, 5, (int) R.string.share).setEnabled(this.mTextView.canShare()).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener);
                menu.add(0, (int) R.id.selectAll, 1, (int) R.string.selectAll).setAlphabeticShortcut(DateFormat.AM_PM).setEnabled(this.mTextView.canSelectAllText()).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener);
                menu.add(0, (int) R.id.autofill, 11, (int) R.string.autofill).setEnabled(this.mTextView.canRequestAutofill()).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener);
                this.mPreserveSelection = true;
            }
        }
    }

    private SuggestionSpan findEquivalentSuggestionSpan(SuggestionSpanInfo suggestionSpanInfo) {
        Editable editable = (Editable) this.mTextView.getText();
        if (editable.getSpanStart(suggestionSpanInfo.mSuggestionSpan) >= 0) {
            return suggestionSpanInfo.mSuggestionSpan;
        }
        for (SuggestionSpan suggestionSpan : (SuggestionSpan[]) editable.getSpans(suggestionSpanInfo.mSpanStart, suggestionSpanInfo.mSpanEnd, SuggestionSpan.class)) {
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
                for (i = 0; i < length; i++) {
                    SuggestionSpan suggestionSpan = suggestionSpans[i];
                    suggestionSpansStarts[i] = editable.getSpanStart(suggestionSpan);
                    suggestionSpansEnds[i] = editable.getSpanEnd(suggestionSpan);
                    suggestionSpansFlags[i] = editable.getSpanFlags(suggestionSpan);
                    int suggestionSpanFlags = suggestionSpan.getFlags();
                    if ((suggestionSpanFlags & 2) != 0) {
                        suggestionSpan.setFlags((suggestionSpanFlags & -3) & -2);
                    }
                }
                targetSuggestionSpan.notifySelection(this.mTextView.getContext(), originalText, suggestionInfo.mSuggestionIndex);
                String suggestion = suggestionInfo.mText.subSequence(suggestionInfo.mSuggestionStart, suggestionInfo.mSuggestionEnd).toString();
                this.mTextView.replaceText_internal(spanStart, spanEnd, suggestion);
                targetSuggestionSpan.getSuggestions()[suggestionInfo.mSuggestionIndex] = originalText;
                int lengthDelta = suggestion.length() - (spanEnd - spanStart);
                i = 0;
                while (i < length) {
                    if (suggestionSpansStarts[i] <= spanStart && suggestionSpansEnds[i] >= spanEnd) {
                        this.mTextView.setSpan_internal(suggestionSpans[i], suggestionSpansStarts[i], suggestionSpansEnds[i] + lengthDelta, suggestionSpansFlags[i]);
                    }
                    i++;
                }
                int newCursorPosition = spanEnd + lengthDelta;
                this.mTextView.setCursorPosition_internal(newCursorPosition, newCursorPosition);
            }
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0012, code:
            return r8;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getCurrentLineAdjustedForSlop(Layout layout, int prevLine, float y) {
        int trueLine = this.mTextView.getLineAtCoordinate(y);
        if (layout == null || prevLine > layout.getLineCount() || layout.getLineCount() <= 0 || prevLine < 0 || Math.abs(trueLine - prevLine) >= 2) {
            return trueLine;
        }
        int currLine;
        float verticalOffset = (float) this.mTextView.viewportToContentVerticalOffset();
        int lineCount = layout.getLineCount();
        float slop = ((float) this.mTextView.getLineHeight()) * LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS;
        float yTopBound = Math.max((((float) layout.getLineTop(prevLine)) + verticalOffset) - slop, (((float) layout.getLineTop(0)) + verticalOffset) + slop);
        float yBottomBound = Math.min((((float) layout.getLineBottom(prevLine)) + verticalOffset) + slop, (((float) layout.getLineBottom(lineCount - 1)) + verticalOffset) - slop);
        if (y <= yTopBound) {
            currLine = Math.max(prevLine - 1, 0);
        } else if (y >= yBottomBound) {
            currLine = Math.min(prevLine + 1, lineCount - 1);
        } else {
            currLine = prevLine;
        }
        return currLine;
    }

    private static boolean isValidRange(CharSequence text, int start, int end) {
        return start >= 0 && start <= end && end <= text.length();
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
        if (imageSpans.length != 1) {
            return offset;
        }
        if (isStartHandle) {
            return ((Spanned) this.mTextView.getText()).getSpanStart(imageSpans[0]);
        }
        return ((Spanned) this.mTextView.getText()).getSpanEnd(imageSpans[0]);
    }

    protected int adjustOffsetAtLineEndForTouchPos(int offset) {
        return offset;
    }

    protected int adjustOffsetAtLineEndForInsertHanlePos(int offset) {
        return offset;
    }

    protected boolean adjustHandlePos(int[] coordinate, HandleView handleView, Layout layout, int offset, int line) {
        return false;
    }

    protected void setPosWithMotionEvent(MotionEvent event, boolean isTouchPos) {
    }

    protected boolean adjustCursorPos(int line, Layout layout) {
        return false;
    }

    protected int getCursorTop() {
        return -1;
    }

    protected int getCursorBottom() {
        return -1;
    }

    protected float getCursorX() {
        return -1.0f;
    }

    protected void setPosIsLineEnd(boolean flag) {
    }

    protected void adjustSelectionBounds(RectF selectionBounds, int line, Layout layout, int handleHeight) {
    }

    protected void recogniseLineEnd() {
    }
}
