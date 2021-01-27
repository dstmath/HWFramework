package android.widget;

import android.animation.ValueAnimator;
import android.annotation.UnsupportedAppUsage;
import android.app.PendingIntent;
import android.app.RemoteAction;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.UndoManager;
import android.content.UndoOperation;
import android.content.UndoOwner;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.RenderNode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hdm.HwDeviceManager;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ParcelableParcel;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.DynamicLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
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
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.DragAndDropPermissions;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputMethodManager;
import android.view.textclassifier.TextClassification;
import android.view.textclassifier.TextClassificationManager;
import android.widget.AdapterView;
import android.widget.Editor;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.transition.EpicenterTranslateClipReveal;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.view.FloatingActionMode;
import com.android.internal.widget.EditableInputConnection;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Editor {
    static final int BLINK = 500;
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_UNDO = false;
    private static final int DRAG_SHADOW_MAX_TEXT_LENGTH = 20;
    static final int EXTRACT_NOTHING = -2;
    static final int EXTRACT_UNKNOWN = -1;
    private static final boolean FLAG_USE_MAGNIFIER = true;
    public static final int HANDLE_TYPE_SELECTION_END = 1;
    public static final int HANDLE_TYPE_SELECTION_START = 0;
    private static final float LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS = 0.5f;
    private static final int MENU_ITEM_ORDER_ASSIST = 2;
    private static final int MENU_ITEM_ORDER_AUTOFILL = 11;
    private static final int MENU_ITEM_ORDER_COPY = 7;
    private static final int MENU_ITEM_ORDER_CUT = 6;
    private static final int MENU_ITEM_ORDER_PASTE = 8;
    private static final int MENU_ITEM_ORDER_PASTE_AS_PLAIN_TEXT = 9;
    private static final int MENU_ITEM_ORDER_PROCESS_TEXT_INTENT_ACTIONS_HUAWEI_BROWSER = 100;
    private static final int MENU_ITEM_ORDER_PROCESS_TEXT_INTENT_ACTIONS_START = 101;
    private static final int MENU_ITEM_ORDER_REDO = 4;
    private static final int MENU_ITEM_ORDER_REPLACE = 10;
    private static final int MENU_ITEM_ORDER_SECONDARY_ASSIST_ACTIONS_START = 50;
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
    @UnsupportedAppUsage
    boolean mCreatedWithASelection;
    private final CursorAnchorInfoNotifier mCursorAnchorInfoNotifier = new CursorAnchorInfoNotifier();
    boolean mCursorVisible = true;
    ActionMode.Callback mCustomInsertionActionModeCallback;
    ActionMode.Callback mCustomSelectionActionModeCallback;
    boolean mDiscardNextActionUp;
    Drawable mDrawableForCursor = null;
    CharSequence mError;
    private ErrorPopup mErrorPopup;
    boolean mErrorWasChanged;
    boolean mFrozenWithFocus;
    private final boolean mHapticTextHandleEnabled;
    boolean mIgnoreActionUpEvent;
    boolean mInBatchEditControllers;
    InputContentType mInputContentType;
    InputMethodState mInputMethodState;
    int mInputType = 0;
    private Runnable mInsertionActionModeRunnable;
    @UnsupportedAppUsage
    private boolean mInsertionControllerEnabled;
    private InsertionPointCursorController mInsertionPointCursorController;
    boolean mIsBeingLongClicked;
    boolean mIsInsertionActionModeStartPending = false;
    KeyListener mKeyListener;
    private int mLastButtonState;
    private float mLastDownPositionX;
    private float mLastDownPositionY;
    private long mLastTouchUpTime = 0;
    private float mLastUpPositionX;
    private float mLastUpPositionY;
    private final MagnifierMotionAnimator mMagnifierAnimator;
    private final ViewTreeObserver.OnDrawListener mMagnifierOnDrawListener = new ViewTreeObserver.OnDrawListener() {
        /* class android.widget.Editor.AnonymousClass2 */

        @Override // android.view.ViewTreeObserver.OnDrawListener
        public void onDraw() {
            if (Editor.this.mMagnifierAnimator != null) {
                Editor.this.mTextView.post(Editor.this.mUpdateMagnifierRunnable);
            }
        }
    };
    private final MetricsLogger mMetricsLogger = new MetricsLogger();
    private final MenuItem.OnMenuItemClickListener mOnContextMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
        /* class android.widget.Editor.AnonymousClass6 */

        @Override // android.view.MenuItem.OnMenuItemClickListener
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
    private boolean mRenderCursorRegardlessTiming;
    private boolean mRequestingLinkActionMode;
    private boolean mRestartActionModeOnNextRefresh;
    boolean mSelectAllOnFocus;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    Drawable mSelectHandleCenter;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    Drawable mSelectHandleLeft;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    Drawable mSelectHandleRight;
    private SelectionActionModeHelper mSelectionActionModeHelper;
    @UnsupportedAppUsage
    private boolean mSelectionControllerEnabled;
    SelectionModifierCursorController mSelectionModifierCursorController;
    boolean mSelectionMoved;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private long mShowCursor;
    private boolean mShowErrorAfterAttach;
    private final Runnable mShowFloatingToolbar = new Runnable() {
        /* class android.widget.Editor.AnonymousClass3 */

        @Override // java.lang.Runnable
        public void run() {
            if (Editor.this.mTextActionMode != null) {
                Editor.this.mTextActionMode.hide(0);
            }
        }
    };
    @UnsupportedAppUsage
    boolean mShowSoftInputOnFocus = true;
    private Runnable mShowSuggestionRunnable;
    private SpanController mSpanController;
    SpellChecker mSpellChecker;
    private final SuggestionHelper mSuggestionHelper = new SuggestionHelper();
    SuggestionRangeSpan mSuggestionRangeSpan;
    private SuggestionsPopupWindow mSuggestionsPopupWindow;
    private int mTapState = 0;
    private Rect mTempRect;
    private ActionMode mTextActionMode;
    boolean mTextIsSelectable;
    private TextRenderNode[] mTextRenderNodes;
    protected TextView mTextView;
    boolean mTouchFocusSelected;
    final UndoInputFilter mUndoInputFilter = new UndoInputFilter(this);
    private final UndoManager mUndoManager = new UndoManager();
    private UndoOwner mUndoOwner = this.mUndoManager.getOwner("Editor", this);
    private final Runnable mUpdateMagnifierRunnable = new Runnable() {
        /* class android.widget.Editor.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            Editor.this.mMagnifierAnimator.update();
        }
    };
    private boolean mUpdateWordIteratorText;
    private WordIterator mWordIterator;
    private WordIterator mWordIteratorWithText;

    /* access modifiers changed from: private */
    public interface CursorController extends ViewTreeObserver.OnTouchModeChangeListener {
        void hide();

        boolean isActive();

        boolean isCursorBeingModified();

        void onDetached();

        void show();
    }

    /* access modifiers changed from: private */
    public interface EasyEditDeleteListener {
        void onDeleteClick(EasyEditSpan easyEditSpan);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface HandleType {
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface MagnifierHandleTrigger {
        public static final int INSERTION = 0;
        public static final int SELECTION_END = 2;
        public static final int SELECTION_START = 1;
    }

    @interface TextActionMode {
        public static final int INSERTION = 1;
        public static final int SELECTION = 0;
        public static final int TEXT_LINK = 2;
    }

    /* access modifiers changed from: private */
    public interface TextViewPositionListener {
        void updatePosition(int i, int i2, boolean z, boolean z2);
    }

    /* access modifiers changed from: private */
    public static class TextRenderNode {
        boolean isDirty = true;
        boolean needsToBeShifted = true;
        RenderNode renderNode;

        public TextRenderNode(String name) {
            this.renderNode = RenderNode.create(name, null);
        }

        /* access modifiers changed from: package-private */
        public boolean needsRecord() {
            return this.isDirty || !this.renderNode.hasDisplayList();
        }
    }

    public Editor(TextView textView) {
        this.mTextView = textView;
        TextView textView2 = this.mTextView;
        textView2.setFilters(textView2.getFilters());
        this.mProcessTextIntentActionsHandler = new ProcessTextIntentActionsHandler();
        this.mHapticTextHandleEnabled = this.mTextView.getContext().getResources().getBoolean(R.bool.config_enableHapticTextHandle);
        this.mMagnifierAnimator = new MagnifierMotionAnimator(Magnifier.createBuilderWithOldMagnifierDefaults(this.mTextView).build());
    }

    /* access modifiers changed from: package-private */
    public ParcelableParcel saveInstanceState() {
        ParcelableParcel state = new ParcelableParcel(getClass().getClassLoader());
        Parcel parcel = state.getParcel();
        this.mUndoManager.saveInstanceState(parcel);
        this.mUndoInputFilter.saveInstanceState(parcel);
        return state;
    }

    /* access modifiers changed from: package-private */
    public void restoreInstanceState(ParcelableParcel state) {
        Parcel parcel = state.getParcel();
        this.mUndoManager.restoreInstanceState(parcel, state.getClassLoader());
        this.mUndoInputFilter.restoreInstanceState(parcel);
        this.mUndoOwner = this.mUndoManager.getOwner("Editor", this);
    }

    /* access modifiers changed from: package-private */
    public void forgetUndoRedo() {
        UndoOwner[] owners = {this.mUndoOwner};
        this.mUndoManager.forgetUndos(owners, -1);
        this.mUndoManager.forgetRedos(owners, -1);
    }

    /* access modifiers changed from: package-private */
    public boolean canUndo() {
        return this.mAllowUndo && this.mUndoManager.countUndos(new UndoOwner[]{this.mUndoOwner}) > 0;
    }

    /* access modifiers changed from: package-private */
    public boolean canRedo() {
        return this.mAllowUndo && this.mUndoManager.countRedos(new UndoOwner[]{this.mUndoOwner}) > 0;
    }

    /* access modifiers changed from: package-private */
    public void undo() {
        if (this.mAllowUndo) {
            this.mUndoManager.undo(new UndoOwner[]{this.mUndoOwner}, 1);
        }
    }

    /* access modifiers changed from: package-private */
    public void redo() {
        if (this.mAllowUndo) {
            this.mUndoManager.redo(new UndoOwner[]{this.mUndoOwner}, 1);
        }
    }

    /* access modifiers changed from: package-private */
    public void replace() {
        if (this.mSuggestionsPopupWindow == null) {
            this.mSuggestionsPopupWindow = new SuggestionsPopupWindow();
        }
        hideCursorAndSpanControllers();
        this.mSuggestionsPopupWindow.show();
        Selection.setSelection((Spannable) this.mTextView.getText(), (this.mTextView.getSelectionStart() + this.mTextView.getSelectionEnd()) / 2);
    }

    /* access modifiers changed from: package-private */
    public void onAttachedToWindow() {
        if (this.mShowErrorAfterAttach) {
            showError();
            this.mShowErrorAfterAttach = false;
        }
        ViewTreeObserver observer = this.mTextView.getViewTreeObserver();
        if (observer.isAlive()) {
            InsertionPointCursorController insertionPointCursorController = this.mInsertionPointCursorController;
            if (insertionPointCursorController != null) {
                observer.addOnTouchModeChangeListener(insertionPointCursorController);
            }
            SelectionModifierCursorController selectionModifierCursorController = this.mSelectionModifierCursorController;
            if (selectionModifierCursorController != null) {
                selectionModifierCursorController.resetTouchOffsets();
                observer.addOnTouchModeChangeListener(this.mSelectionModifierCursorController);
            }
            observer.addOnDrawListener(this.mMagnifierOnDrawListener);
        }
        updateSpellCheckSpans(0, this.mTextView.getText().length(), true);
        if (this.mTextView.hasSelection()) {
            refreshTextActionMode();
        }
        getPositionListener().addSubscriber(this.mCursorAnchorInfoNotifier, true);
        resumeBlink();
    }

    /* access modifiers changed from: package-private */
    public void onDetachedFromWindow() {
        getPositionListener().removeSubscriber(this.mCursorAnchorInfoNotifier);
        if (this.mError != null) {
            hideError();
        }
        suspendBlink();
        InsertionPointCursorController insertionPointCursorController = this.mInsertionPointCursorController;
        if (insertionPointCursorController != null) {
            insertionPointCursorController.onDetached();
        }
        SelectionModifierCursorController selectionModifierCursorController = this.mSelectionModifierCursorController;
        if (selectionModifierCursorController != null) {
            selectionModifierCursorController.onDetached();
        }
        Runnable runnable = this.mShowSuggestionRunnable;
        if (runnable != null) {
            this.mTextView.removeCallbacks(runnable);
        }
        Runnable runnable2 = this.mInsertionActionModeRunnable;
        if (runnable2 != null) {
            this.mTextView.removeCallbacks(runnable2);
        }
        this.mTextView.removeCallbacks(this.mShowFloatingToolbar);
        discardTextDisplayLists();
        SpellChecker spellChecker = this.mSpellChecker;
        if (spellChecker != null) {
            spellChecker.closeSession();
            this.mSpellChecker = null;
        }
        ViewTreeObserver observer = this.mTextView.getViewTreeObserver();
        if (observer.isAlive()) {
            observer.removeOnDrawListener(this.mMagnifierOnDrawListener);
        }
        hideCursorAndSpanControllers();
        stopTextActionModeWithPreservingSelection();
    }

    /* access modifiers changed from: package-private */
    public void hideHandleByKeyEvent() {
        InsertionPointCursorController controller = getInsertionController();
        if (controller != null && controller.isActive()) {
            controller.hide();
        }
    }

    private void discardTextDisplayLists() {
        if (this.mTextRenderNodes != null) {
            int i = 0;
            while (true) {
                TextRenderNode[] textRenderNodeArr = this.mTextRenderNodes;
                if (i < textRenderNodeArr.length) {
                    RenderNode displayList = textRenderNodeArr[i] != null ? textRenderNodeArr[i].renderNode : null;
                    if (displayList != null && displayList.hasDisplayList()) {
                        displayList.discardDisplayList();
                    }
                    i++;
                } else {
                    return;
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
            float scale = this.mTextView.getResources().getDisplayMetrics().density;
            this.mErrorPopup = new ErrorPopup((TextView) LayoutInflater.from(this.mTextView.getContext()).inflate(R.layout.textview_hint, (ViewGroup) null), (int) ((200.0f * scale) + LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS), (int) ((50.0f * scale) + LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS));
            this.mErrorPopup.setFocusable(false);
            this.mErrorPopup.setInputMethodMode(1);
        }
        TextView tv = (TextView) this.mErrorPopup.getContentView();
        chooseSize(this.mErrorPopup, this.mError, tv);
        tv.setText(this.mError);
        this.mErrorPopup.showAsDropDown(this.mTextView, getErrorX(), getErrorY(), 51);
        ErrorPopup errorPopup = this.mErrorPopup;
        errorPopup.fixDirection(errorPopup.isAboveAnchor());
    }

    public void setError(CharSequence error, Drawable icon) {
        this.mError = TextUtils.stringOrSpannedString(error);
        this.mErrorWasChanged = true;
        if (this.mError == null) {
            setErrorIcon(null);
            ErrorPopup errorPopup = this.mErrorPopup;
            if (errorPopup != null) {
                if (errorPopup.isShowing()) {
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
        TextView.Drawables dr = this.mTextView.mDrawables;
        if (dr == null) {
            TextView textView = this.mTextView;
            TextView.Drawables drawables = new TextView.Drawables(textView.getContext());
            dr = drawables;
            textView.mDrawables = drawables;
        }
        dr.setErrorDrawable(icon, this.mTextView);
        this.mTextView.resetResolvedDrawables();
        this.mTextView.invalidate();
        this.mTextView.requestLayout();
    }

    private void hideError() {
        ErrorPopup errorPopup = this.mErrorPopup;
        if (errorPopup != null && errorPopup.isShowing()) {
            this.mErrorPopup.dismiss();
        }
        this.mShowErrorAfterAttach = false;
    }

    private int getErrorX() {
        float scale = this.mTextView.getResources().getDisplayMetrics().density;
        TextView.Drawables dr = this.mTextView.mDrawables;
        int i = 0;
        if (this.mTextView.getLayoutDirection() != 1) {
            if (dr != null) {
                i = dr.mDrawableSizeRight;
            }
            return ((this.mTextView.getWidth() - this.mErrorPopup.getWidth()) - this.mTextView.getPaddingRight()) + ((-i) / 2) + ((int) ((25.0f * scale) + LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS));
        }
        if (dr != null) {
            i = dr.mDrawableSizeLeft;
        }
        return this.mTextView.getPaddingLeft() + ((i / 2) - ((int) ((25.0f * scale) + LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS)));
    }

    private int getErrorY() {
        int compoundPaddingTop = this.mTextView.getCompoundPaddingTop();
        int vspace = ((this.mTextView.getBottom() - this.mTextView.getTop()) - this.mTextView.getCompoundPaddingBottom()) - compoundPaddingTop;
        TextView.Drawables dr = this.mTextView.mDrawables;
        int height = 0;
        if (this.mTextView.getLayoutDirection() != 1) {
            if (dr != null) {
                height = dr.mDrawableHeightRight;
            }
        } else if (dr != null) {
            height = dr.mDrawableHeightLeft;
        }
        return (((((vspace - height) / 2) + compoundPaddingTop) + height) - this.mTextView.getHeight()) - ((int) ((2.0f * this.mTextView.getResources().getDisplayMetrics().density) + LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS));
    }

    /* access modifiers changed from: package-private */
    public void createInputContentTypeIfNeeded() {
        if (this.mInputContentType == null) {
            this.mInputContentType = new InputContentType();
        }
    }

    /* access modifiers changed from: package-private */
    public void createInputMethodStateIfNeeded() {
        if (this.mInputMethodState == null) {
            this.mInputMethodState = new InputMethodState();
        }
    }

    private boolean isCursorVisible() {
        return this.mCursorVisible && this.mTextView.isTextEditable();
    }

    /* access modifiers changed from: package-private */
    public boolean shouldRenderCursor() {
        if (!isCursorVisible()) {
            return false;
        }
        if (this.mRenderCursorRegardlessTiming) {
            return true;
        }
        if ((SystemClock.uptimeMillis() - this.mShowCursor) % 1000 < 500) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void prepareCursorControllers() {
        boolean windowSupportsHandles = false;
        ViewGroup.LayoutParams params = this.mTextView.getRootView().getLayoutParams();
        boolean z = true;
        if (params instanceof WindowManager.LayoutParams) {
            WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) params;
            windowSupportsHandles = windowParams.type < 1000 || windowParams.type > 1999;
        }
        boolean enabled = windowSupportsHandles && this.mTextView.getLayout() != null;
        this.mInsertionControllerEnabled = enabled && isCursorVisible();
        if (!enabled || !this.mTextView.textCanBeSelected()) {
            z = false;
        }
        this.mSelectionControllerEnabled = z;
        if (!this.mInsertionControllerEnabled) {
            hideInsertionPointCursorController();
            InsertionPointCursorController insertionPointCursorController = this.mInsertionPointCursorController;
            if (insertionPointCursorController != null) {
                insertionPointCursorController.onDetached();
                this.mInsertionPointCursorController = null;
            }
        }
        if (!this.mSelectionControllerEnabled) {
            lambda$startActionModeInternal$0$Editor();
            SelectionModifierCursorController selectionModifierCursorController = this.mSelectionModifierCursorController;
            if (selectionModifierCursorController != null) {
                selectionModifierCursorController.onDetached();
                this.mSelectionModifierCursorController = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void hideInsertionPointCursorController() {
        InsertionPointCursorController insertionPointCursorController = this.mInsertionPointCursorController;
        if (insertionPointCursorController != null) {
            insertionPointCursorController.hide();
        }
    }

    /* access modifiers changed from: package-private */
    public void hideCursorAndSpanControllers() {
        hideCursorControllers();
        hideSpanControllers();
    }

    private void hideSpanControllers() {
        SpanController spanController = this.mSpanController;
        if (spanController != null) {
            spanController.hide();
        }
    }

    private void hideCursorControllers() {
        if (this.mSuggestionsPopupWindow != null && (this.mTextView.isInExtractedMode() || !this.mSuggestionsPopupWindow.isShowingUp())) {
            this.mSuggestionsPopupWindow.hide();
        }
        hideInsertionPointCursorController();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSpellCheckSpans(int start, int end, boolean createSpellChecker) {
        this.mTextView.removeAdjacentSuggestionSpans(start);
        this.mTextView.removeAdjacentSuggestionSpans(end);
        if (this.mTextView.isTextEditable() && this.mTextView.isSuggestionsEnabled() && !this.mTextView.isInExtractedMode()) {
            if (this.mSpellChecker == null && createSpellChecker) {
                this.mSpellChecker = new SpellChecker(this.mTextView);
            }
            SpellChecker spellChecker = this.mSpellChecker;
            if (spellChecker != null) {
                spellChecker.spellCheck(start, end);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onScreenStateChanged(int screenState) {
        if (screenState == 0) {
            suspendBlink();
        } else if (screenState == 1) {
            resumeBlink();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void suspendBlink() {
        Blink blink = this.mBlink;
        if (blink != null) {
            blink.cancel();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resumeBlink() {
        Blink blink = this.mBlink;
        if (blink != null) {
            blink.uncancel();
            makeBlink();
        }
    }

    /* access modifiers changed from: package-private */
    public void adjustInputType(boolean password, boolean passwordInputType, boolean webPasswordInputType, boolean numberPasswordInputType) {
        int i = this.mInputType;
        if ((i & 15) == 1) {
            if (password || passwordInputType) {
                this.mInputType = (this.mInputType & -4081) | 128;
            }
            if (webPasswordInputType) {
                this.mInputType = (this.mInputType & -4081) | 224;
            }
        } else if ((i & 15) == 2 && numberPasswordInputType) {
            this.mInputType = (i & -4081) | 16;
        }
    }

    private void chooseSize(PopupWindow pop, CharSequence text, TextView tv) {
        int wid = tv.getPaddingLeft() + tv.getPaddingRight();
        int ht = tv.getPaddingTop() + tv.getPaddingBottom();
        StaticLayout l = StaticLayout.Builder.obtain(text, 0, text.length(), tv.getPaint(), this.mTextView.getResources().getDimensionPixelSize(R.dimen.textview_error_popup_default_width)).setUseLineSpacingFromFallbacks(tv.mUseFallbackLineSpacing).build();
        float max = 0.0f;
        for (int i = 0; i < l.getLineCount(); i++) {
            max = Math.max(max, l.getLineWidth(i));
        }
        pop.setWidth(((int) Math.ceil((double) max)) + wid);
        pop.setHeight(l.getHeight() + ht);
    }

    /* access modifiers changed from: package-private */
    public void setFrame() {
        ErrorPopup errorPopup = this.mErrorPopup;
        if (errorPopup != null) {
            chooseSize(this.mErrorPopup, this.mError, (TextView) errorPopup.getContentView());
            this.mErrorPopup.update(this.mTextView, getErrorX(), getErrorY(), this.mErrorPopup.getWidth(), this.mErrorPopup.getHeight());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: package-private */
    public boolean selectCurrentWord() {
        int selectionStart;
        int selectionEnd;
        if (!this.mTextView.canSelectText()) {
            return false;
        }
        if (needsToSelectAllToSelectWordOrParagraph()) {
            return this.mTextView.selectAllText();
        }
        long lastTouchOffsets = getLastTouchOffsets();
        int minOffset = TextUtils.unpackRangeStartFromLong(lastTouchOffsets);
        int maxOffset = TextUtils.unpackRangeEndFromLong(lastTouchOffsets);
        if (minOffset < 0 || minOffset > this.mTextView.getText().length() || maxOffset < 0 || maxOffset > this.mTextView.getText().length()) {
            return false;
        }
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
            int selectionEnd2 = wordIterator.getEnd(maxOffset);
            if (selectionStart == -1 || selectionEnd2 == -1 || selectionStart == selectionEnd2) {
                long range = getCharClusterRange(minOffset);
                selectionStart = TextUtils.unpackRangeStartFromLong(range);
                selectionEnd = TextUtils.unpackRangeEndFromLong(range);
            } else {
                selectionEnd = selectionEnd2;
            }
        }
        Selection.setSelection((Spannable) this.mTextView.getText(), selectionStart, selectionEnd);
        if (selectionEnd > selectionStart) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getParagraphsRange(int startOffset, int endOffset) {
        Layout layout = this.mTextView.getLayout();
        if (layout == null) {
            return TextUtils.packRangeInLong(-1, -1);
        }
        CharSequence text = this.mTextView.getText();
        int minLine = layout.getLineForOffset(startOffset);
        while (minLine > 0 && text.charAt(layout.getLineEnd(minLine - 1) - 1) != '\n') {
            minLine--;
        }
        int maxLine = layout.getLineForOffset(endOffset);
        while (maxLine < layout.getLineCount() - 1 && text.charAt(layout.getLineEnd(maxLine) - 1) != '\n') {
            maxLine++;
        }
        return TextUtils.packRangeInLong(layout.getLineStart(minLine), layout.getLineEnd(maxLine));
    }

    /* access modifiers changed from: package-private */
    public void onLocaleChanged() {
        this.mWordIterator = null;
        this.mWordIteratorWithText = null;
    }

    public WordIterator getWordIterator() {
        if (this.mWordIterator == null) {
            this.mWordIterator = new WordIterator(this.mTextView.getTextServicesLocale());
        }
        return this.mWordIterator;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
            selectionStart = selectionEnd;
            selectionEnd = selectionStart;
            Selection.setSelection((Spannable) this.mTextView.getText(), selectionStart, selectionEnd);
        }
        SelectionModifierCursorController selectionController = getSelectionController();
        int minOffset = selectionController.getMinTouchOffset();
        int maxOffset = selectionController.getMaxTouchOffset();
        if (minOffset < selectionStart || maxOffset >= selectionEnd) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private PositionListener getPositionListener() {
        if (this.mPositionListener == null) {
            this.mPositionListener = new PositionListener();
        }
        return this.mPositionListener;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isOffsetVisible(int offset) {
        Layout layout = this.mTextView.getLayout();
        if (layout == null) {
            return false;
        }
        int lineBottom = layout.getLineBottom(layout.getLineForOffset(offset));
        TextView textView = this.mTextView;
        return textView.isPositionVisible((float) (textView.viewportToContentHorizontalOffset() + ((int) layout.getPrimaryHorizontal(offset))), (float) (this.mTextView.viewportToContentVerticalOffset() + lineBottom));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPositionOnText(float x, float y) {
        Layout layout = this.mTextView.getLayout();
        if (layout == null) {
            return false;
        }
        int line = this.mTextView.getLineAtCoordinate(y);
        float x2 = this.mTextView.convertToLocalHorizontalCoordinate(x);
        if (x2 >= layout.getLineLeft(line) && x2 <= layout.getLineRight(line)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startDragAndDrop() {
        getSelectionActionModeHelper().onSelectionDrag();
        if (!this.mTextView.isInExtractedMode()) {
            int start = this.mTextView.getSelectionStart();
            int end = this.mTextView.getSelectionEnd();
            this.mTextView.startDragAndDrop(ClipData.newPlainText(null, this.mTextView.getTransformedText(start, end)), getTextThumbnailBuilder(start, end), new DragLocalState(this.mTextView, start, end), 256);
            lambda$startActionModeInternal$0$Editor();
            if (hasSelectionController()) {
                getSelectionController().resetTouchOffsets();
            }
        }
    }

    public boolean performLongClick(boolean handled) {
        if (!handled && !isPositionOnText(this.mLastDownPositionX, this.mLastDownPositionY) && this.mInsertionControllerEnabled) {
            Selection.setSelection((Spannable) this.mTextView.getText(), this.mTextView.getOffsetForPosition(this.mLastDownPositionX, this.mLastDownPositionY));
            getInsertionController().show();
            this.mIsInsertionActionModeStartPending = true;
            handled = true;
            MetricsLogger.action(this.mTextView.getContext(), 629, 0);
        }
        if (!handled && this.mTextActionMode != null) {
            if (touchPositionIsInSelection()) {
                startDragAndDrop();
                MetricsLogger.action(this.mTextView.getContext(), 629, 2);
            } else {
                lambda$startActionModeInternal$0$Editor();
                selectCurrentWordAndStartDrag();
                MetricsLogger.action(this.mTextView.getContext(), 629, 1);
            }
            handled = true;
        }
        if (!handled && (handled = selectCurrentWordAndStartDrag())) {
            MetricsLogger.action(this.mTextView.getContext(), 629, 1);
        }
        return handled;
    }

    /* access modifiers changed from: package-private */
    public float getLastUpPositionX() {
        return this.mLastUpPositionX;
    }

    /* access modifiers changed from: package-private */
    public float getLastUpPositionY() {
        return this.mLastUpPositionY;
    }

    private long getLastTouchOffsets() {
        SelectionModifierCursorController selectionController = getSelectionController();
        return TextUtils.packRangeInLong(selectionController.getMinTouchOffset(), selectionController.getMaxTouchOffset());
    }

    /* access modifiers changed from: package-private */
    public void onFocusChanged(boolean focused, int direction) {
        this.mShowCursor = SystemClock.uptimeMillis();
        ensureEndedBatchEdit();
        if (focused) {
            int selStart = this.mTextView.getSelectionStart();
            int selEnd = this.mTextView.getSelectionEnd();
            this.mCreatedWithASelection = this.mFrozenWithFocus && this.mTextView.hasSelection() && !(this.mSelectAllOnFocus && selStart == 0 && selEnd == this.mTextView.getText().length());
            if (!this.mFrozenWithFocus || selStart < 0 || selEnd < 0) {
                int lastTapPosition = getLastTapPosition();
                if (lastTapPosition >= 0) {
                    Selection.setSelection((Spannable) this.mTextView.getText(), lastTapPosition);
                }
                MovementMethod mMovement = this.mTextView.getMovementMethod();
                if (mMovement != null) {
                    TextView textView = this.mTextView;
                    mMovement.onTakeFocus(textView, (Spannable) textView.getText(), direction);
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
                lambda$startActionModeInternal$0$Editor();
            }
            downgradeEasyCorrectionSpans();
        }
        SelectionModifierCursorController selectionModifierCursorController = this.mSelectionModifierCursorController;
        if (selectionModifierCursorController != null) {
            selectionModifierCursorController.resetTouchOffsets();
        }
        ensureNoSelectionIfNonSelectable();
    }

    private void ensureNoSelectionIfNonSelectable() {
        if (!this.mTextView.textCanBeSelected() && this.mTextView.hasSelection()) {
            Selection.setSelection((Spannable) this.mTextView.getText(), this.mTextView.length(), this.mTextView.length());
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

    /* access modifiers changed from: package-private */
    public void sendOnTextChanged(int start, int before, int after) {
        getSelectionActionModeHelper().onTextChanged(start, start + before);
        updateSpellCheckSpans(start, start + after, false);
        this.mUpdateWordIteratorText = true;
        hideCursorControllers();
        SelectionModifierCursorController selectionModifierCursorController = this.mSelectionModifierCursorController;
        if (selectionModifierCursorController != null) {
            selectionModifierCursorController.resetTouchOffsets();
        }
        lambda$startActionModeInternal$0$Editor();
    }

    private int getLastTapPosition() {
        int lastTapPosition;
        SelectionModifierCursorController selectionModifierCursorController = this.mSelectionModifierCursorController;
        if (selectionModifierCursorController == null || (lastTapPosition = selectionModifierCursorController.getMinTouchOffset()) < 0) {
            return -1;
        }
        if (lastTapPosition > this.mTextView.getText().length()) {
            return this.mTextView.getText().length();
        }
        return lastTapPosition;
    }

    /* access modifiers changed from: package-private */
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (hasWindowFocus) {
            Blink blink = this.mBlink;
            if (blink != null) {
                blink.uncancel();
                makeBlink();
            }
            if (this.mTextView.hasSelection() && !extractedTextModeWillBeStarted()) {
                refreshTextActionMode();
                return;
            }
            return;
        }
        Blink blink2 = this.mBlink;
        if (blink2 != null) {
            blink2.cancel();
        }
        InputContentType inputContentType = this.mInputContentType;
        if (inputContentType != null) {
            inputContentType.enterDown = false;
        }
        hideCursorAndSpanControllers();
        stopTextActionModeWithPreservingSelection();
        SuggestionsPopupWindow suggestionsPopupWindow = this.mSuggestionsPopupWindow;
        if (suggestionsPopupWindow != null) {
            suggestionsPopupWindow.onParentLostFocus();
        }
        ensureEndedBatchEdit();
        ensureNoSelectionIfNonSelectable();
    }

    private void updateTapState(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == 0) {
            boolean isMouse = event.isFromSource(8194);
            int i = this.mTapState;
            if ((i != 1 && (i != 2 || !isMouse)) || SystemClock.uptimeMillis() - this.mLastTouchUpTime > ((long) ViewConfiguration.getDoubleTapTimeout())) {
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
        if (!event.isFromSource(8194)) {
            return false;
        }
        boolean primaryButtonStateChanged = ((this.mLastButtonState ^ event.getButtonState()) & 1) != 0;
        int action = event.getActionMasked();
        if ((action == 0 || action == 1) && !primaryButtonStateChanged) {
            return true;
        }
        if (action != 2 || event.isButtonPressed(1)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void onTouchEvent(MotionEvent event) {
        boolean filterOutEvent = shouldFilterOutTouchEvent(event);
        this.mLastButtonState = event.getButtonState();
        if (!filterOutEvent) {
            setPosWithMotionEvent(event, true);
            updateTapState(event);
            updateFloatingToolbarVisibility(event);
            if (hasSelectionController()) {
                getSelectionController().onTouchEvent(event);
            }
            Runnable runnable = this.mShowSuggestionRunnable;
            if (runnable != null) {
                this.mTextView.removeCallbacks(runnable);
                this.mShowSuggestionRunnable = null;
            }
            if (event.getActionMasked() == 1) {
                this.mLastUpPositionX = event.getX();
                this.mLastUpPositionY = event.getY();
            }
            if (event.getActionMasked() == 0) {
                this.mLastDownPositionX = event.getX();
                this.mLastDownPositionY = event.getY();
                this.mTouchFocusSelected = false;
                this.mIgnoreActionUpEvent = false;
            }
        } else if (event.getActionMasked() == 1) {
            this.mDiscardNextActionUp = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFloatingToolbarVisibility(MotionEvent event) {
        if (this.mTextActionMode != null) {
            int actionMasked = event.getActionMasked();
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    hideFloatingToolbar(-1);
                    return;
                } else if (actionMasked != 3) {
                    return;
                }
            }
            showFloatingToolbar();
        }
    }

    /* access modifiers changed from: package-private */
    public void hideFloatingToolbar(int duration) {
        if (this.mTextActionMode != null) {
            this.mTextView.removeCallbacks(this.mShowFloatingToolbar);
            this.mTextActionMode.hide((long) duration);
        }
    }

    /* access modifiers changed from: protected */
    public void showFloatingToolbar() {
        if (this.mTextActionMode != null) {
            this.mTextView.postDelayed(this.mShowFloatingToolbar, (long) ViewConfiguration.getDoubleTapTimeout());
            if (this.mTextView.getSelectionStart() > this.mTextView.getSelectionEnd()) {
                invalidateActionMode();
            } else {
                invalidateActionModeAsync();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private InputMethodManager getInputMethodManager() {
        return (InputMethodManager) this.mTextView.getContext().getSystemService(InputMethodManager.class);
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

    /* access modifiers changed from: package-private */
    public void ensureEndedBatchEdit() {
        InputMethodState ims = this.mInputMethodState;
        if (ims != null && ims.mBatchEditNesting != 0) {
            ims.mBatchEditNesting = 0;
            finishBatchEdit(ims);
        }
    }

    /* access modifiers changed from: package-private */
    public void finishBatchEdit(InputMethodState ims) {
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

    /* access modifiers changed from: package-private */
    public boolean extractText(ExtractedTextRequest request, ExtractedText outText) {
        return extractTextInternal(request, -1, -1, -1, outText);
    }

    private boolean extractTextInternal(ExtractedTextRequest request, int partialStartOffset, int partialEndOffset, int delta, ExtractedText outText) {
        CharSequence content;
        int partialEndOffset2;
        if (request == null || outText == null || (content = this.mTextView.getText()) == null) {
            return false;
        }
        if (partialStartOffset != -2) {
            int N = content.length();
            if (partialStartOffset < 0) {
                outText.partialEndOffset = -1;
                outText.partialStartOffset = -1;
                partialStartOffset = 0;
                partialEndOffset2 = N;
            } else {
                partialEndOffset2 = partialEndOffset + delta;
                if (content instanceof Spanned) {
                    Spanned spanned = (Spanned) content;
                    Object[] spans = spanned.getSpans(partialStartOffset, partialEndOffset2, ParcelableSpan.class);
                    int i = spans.length;
                    while (i > 0) {
                        i--;
                        int j = spanned.getSpanStart(spans[i]);
                        if (j < partialStartOffset) {
                            partialStartOffset = j;
                        }
                        int j2 = spanned.getSpanEnd(spans[i]);
                        if (j2 > partialEndOffset2) {
                            partialEndOffset2 = j2;
                        }
                    }
                }
                outText.partialStartOffset = partialStartOffset;
                outText.partialEndOffset = partialEndOffset2 - delta;
                if (partialStartOffset > N) {
                    partialStartOffset = N;
                } else if (partialStartOffset < 0) {
                    partialStartOffset = 0;
                }
                if (partialEndOffset2 > N) {
                    partialEndOffset2 = N;
                } else if (partialEndOffset2 < 0) {
                    partialEndOffset2 = 0;
                }
            }
            if ((request.flags & 1) != 0) {
                outText.text = content.subSequence(partialStartOffset, partialEndOffset2);
            } else {
                outText.text = TextUtils.substring(content, partialStartOffset, partialEndOffset2);
            }
        } else {
            outText.partialStartOffset = 0;
            outText.partialEndOffset = 0;
            outText.text = "";
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
        outText.hint = this.mTextView.getHint();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean reportExtractedText() {
        InputMethodManager imm;
        InputMethodState ims = this.mInputMethodState;
        if (ims == null) {
            return false;
        }
        boolean wasContentChanged = ims.mContentChanged;
        if (!wasContentChanged && !ims.mSelectionModeChanged) {
            return false;
        }
        ims.mContentChanged = false;
        ims.mSelectionModeChanged = false;
        ExtractedTextRequest req = ims.mExtractedTextRequest;
        if (req == null || (imm = getInputMethodManager()) == null) {
            return false;
        }
        if (ims.mChangedStart < 0 && !wasContentChanged) {
            ims.mChangedStart = -2;
        }
        if (!extractTextInternal(req, ims.mChangedStart, ims.mChangedEnd, ims.mChangedDelta, ims.mExtractedText)) {
            return false;
        }
        imm.updateExtractedText(this.mTextView, req.token, ims.mExtractedText);
        ims.mChangedStart = -1;
        ims.mChangedEnd = -1;
        ims.mChangedDelta = 0;
        ims.mContentChanged = false;
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendUpdateSelection() {
        InputMethodManager imm;
        int candEnd;
        int candStart;
        InputMethodState inputMethodState = this.mInputMethodState;
        if (inputMethodState != null && inputMethodState.mBatchEditNesting <= 0 && (imm = getInputMethodManager()) != null) {
            int selectionStart = this.mTextView.getSelectionStart();
            int selectionEnd = this.mTextView.getSelectionEnd();
            if (this.mTextView.getText() instanceof Spannable) {
                Spannable sp = (Spannable) this.mTextView.getText();
                candStart = EditableInputConnection.getComposingSpanStart(sp);
                candEnd = EditableInputConnection.getComposingSpanEnd(sp);
            } else {
                candStart = -1;
                candEnd = -1;
            }
            imm.updateSelection(this.mTextView, selectionStart, selectionEnd, candStart, candEnd);
        }
    }

    /* access modifiers changed from: package-private */
    public void onDraw(Canvas canvas, Layout layout, Path highlight, Paint highlightPaint, int cursorOffsetVertical) {
        InputMethodManager imm;
        int selectionStart = this.mTextView.getSelectionStart();
        int selectionEnd = this.mTextView.getSelectionEnd();
        InputMethodState ims = this.mInputMethodState;
        if (ims != null && ims.mBatchEditNesting == 0 && (imm = getInputMethodManager()) != null && imm.isActive(this.mTextView) && (ims.mContentChanged || ims.mSelectionModeChanged)) {
            reportExtractedText();
        }
        CorrectionHighlighter correctionHighlighter = this.mCorrectionHighlighter;
        if (correctionHighlighter != null) {
            correctionHighlighter.draw(canvas, cursorOffsetVertical);
        }
        if (!(highlight == null || selectionStart != selectionEnd || this.mDrawableForCursor == null)) {
            drawCursor(canvas, cursorOffsetVertical);
            highlight = null;
        }
        SelectionActionModeHelper selectionActionModeHelper = this.mSelectionActionModeHelper;
        if (selectionActionModeHelper != null) {
            selectionActionModeHelper.onDraw(canvas);
            if (this.mSelectionActionModeHelper.isDrawingHighlight()) {
                highlight = null;
            }
        }
        if (!this.mTextView.canHaveDisplayList() || !canvas.isHardwareAccelerated()) {
            layout.draw(canvas, highlight, highlightPaint, cursorOffsetVertical);
        } else {
            drawHardwareAccelerated(canvas, layout, highlight, highlightPaint, cursorOffsetVertical);
        }
    }

    private void drawHardwareAccelerated(Canvas canvas, Layout layout, Path highlight, Paint highlightPaint, int cursorOffsetVertical) {
        int numberOfBlocks;
        int[] blockEndLines;
        DynamicLayout dynamicLayout;
        int lastLine;
        int firstLine;
        ArraySet<Integer> blockSet;
        int lastIndex;
        int lastIndex2;
        int i;
        int lastIndex3;
        long lineRange;
        boolean z;
        int i2;
        int indexFirstChangedBlock;
        Editor editor = this;
        long lineRange2 = layout.getLineRangeForDraw(canvas);
        int firstLine2 = TextUtils.unpackRangeStartFromLong(lineRange2);
        int lastLine2 = TextUtils.unpackRangeEndFromLong(lineRange2);
        if (lastLine2 >= 0) {
            layout.drawBackground(canvas, highlight, highlightPaint, cursorOffsetVertical, firstLine2, lastLine2);
            if (layout instanceof DynamicLayout) {
                if (editor.mTextRenderNodes == null) {
                    editor.mTextRenderNodes = (TextRenderNode[]) ArrayUtils.emptyArray(TextRenderNode.class);
                }
                DynamicLayout dynamicLayout2 = (DynamicLayout) layout;
                int[] blockEndLines2 = dynamicLayout2.getBlockEndLines();
                int[] blockIndices = dynamicLayout2.getBlockIndices();
                int numberOfBlocks2 = dynamicLayout2.getNumberOfBlocks();
                int indexFirstChangedBlock2 = dynamicLayout2.getIndexFirstChangedBlock();
                ArraySet<Integer> blockSet2 = dynamicLayout2.getBlocksAlwaysNeedToBeRedrawn();
                int i3 = -1;
                boolean z2 = true;
                if (blockSet2 != null) {
                    int i4 = 0;
                    while (i4 < blockSet2.size()) {
                        int blockIndex = dynamicLayout2.getBlockIndex(blockSet2.valueAt(i4).intValue());
                        if (blockIndex != i3) {
                            TextRenderNode[] textRenderNodeArr = editor.mTextRenderNodes;
                            if (textRenderNodeArr[blockIndex] != null) {
                                textRenderNodeArr[blockIndex].needsToBeShifted = true;
                            }
                        }
                        i4++;
                        i3 = -1;
                    }
                }
                int startBlock = Arrays.binarySearch(blockEndLines2, 0, numberOfBlocks2, firstLine2);
                if (startBlock < 0) {
                    startBlock = -(startBlock + 1);
                }
                int startIndexToFindAvailableRenderNode = 0;
                int i5 = Math.min(indexFirstChangedBlock2, startBlock);
                while (true) {
                    if (i5 >= numberOfBlocks2) {
                        numberOfBlocks = numberOfBlocks2;
                        blockEndLines = blockEndLines2;
                        dynamicLayout = dynamicLayout2;
                        lastLine = lastLine2;
                        firstLine = firstLine2;
                        blockSet = blockSet2;
                        lastIndex = numberOfBlocks2;
                        break;
                    }
                    int blockIndex2 = blockIndices[i5];
                    if (i5 >= indexFirstChangedBlock2 && blockIndex2 != -1) {
                        TextRenderNode[] textRenderNodeArr2 = editor.mTextRenderNodes;
                        if (textRenderNodeArr2[blockIndex2] != null) {
                            textRenderNodeArr2[blockIndex2].needsToBeShifted = z2;
                        }
                    }
                    if (blockEndLines2[i5] < firstLine2) {
                        z = z2;
                        i2 = i5;
                        numberOfBlocks = numberOfBlocks2;
                        blockEndLines = blockEndLines2;
                        dynamicLayout = dynamicLayout2;
                        lastLine = lastLine2;
                        firstLine = firstLine2;
                        lineRange = lineRange2;
                        blockSet = blockSet2;
                        indexFirstChangedBlock = indexFirstChangedBlock2;
                    } else {
                        z = z2;
                        lineRange = lineRange2;
                        i2 = i5;
                        blockSet = blockSet2;
                        indexFirstChangedBlock = indexFirstChangedBlock2;
                        numberOfBlocks = numberOfBlocks2;
                        blockEndLines = blockEndLines2;
                        dynamicLayout = dynamicLayout2;
                        lastLine = lastLine2;
                        firstLine = firstLine2;
                        startIndexToFindAvailableRenderNode = drawHardwareAcceleratedInner(canvas, layout, highlight, highlightPaint, cursorOffsetVertical, blockEndLines2, blockIndices, i2, numberOfBlocks, startIndexToFindAvailableRenderNode);
                        if (blockEndLines[i2] >= lastLine) {
                            lastIndex = Math.max(indexFirstChangedBlock, i2 + 1);
                            break;
                        }
                    }
                    i5 = i2 + 1;
                    dynamicLayout2 = dynamicLayout;
                    lastLine2 = lastLine;
                    indexFirstChangedBlock2 = indexFirstChangedBlock;
                    blockSet2 = blockSet;
                    z2 = z;
                    lineRange2 = lineRange;
                    numberOfBlocks2 = numberOfBlocks;
                    blockEndLines2 = blockEndLines;
                    firstLine2 = firstLine;
                }
                if (blockSet != null) {
                    int i6 = 0;
                    while (i6 < blockSet.size()) {
                        int block = blockSet.valueAt(i6).intValue();
                        int blockIndex3 = dynamicLayout.getBlockIndex(block);
                        if (blockIndex3 != -1) {
                            TextRenderNode[] textRenderNodeArr3 = editor.mTextRenderNodes;
                            if (textRenderNodeArr3[blockIndex3] != null && !textRenderNodeArr3[blockIndex3].needsToBeShifted) {
                                i = i6;
                                lastIndex3 = lastIndex;
                                i6 = i + 1;
                                lastIndex = lastIndex3;
                                editor = this;
                            }
                        }
                        i = i6;
                        lastIndex3 = lastIndex;
                        startIndexToFindAvailableRenderNode = drawHardwareAcceleratedInner(canvas, layout, highlight, highlightPaint, cursorOffsetVertical, blockEndLines, blockIndices, block, numberOfBlocks, startIndexToFindAvailableRenderNode);
                        i6 = i + 1;
                        lastIndex = lastIndex3;
                        editor = this;
                    }
                    lastIndex2 = lastIndex;
                } else {
                    lastIndex2 = lastIndex;
                }
                dynamicLayout.setIndexFirstChangedBlock(lastIndex2);
                return;
            }
            layout.drawText(canvas, firstLine2, lastLine2);
        }
    }

    private int drawHardwareAcceleratedInner(Canvas canvas, Layout layout, Path highlight, Paint highlightPaint, int cursorOffsetVertical, int[] blockEndLines, int[] blockIndices, int blockInfoIndex, int numberOfBlocks, int startIndexToFindAvailableRenderNode) {
        int startIndexToFindAvailableRenderNode2;
        int blockIndex;
        int line;
        boolean z;
        int blockEndLine = blockEndLines[blockInfoIndex];
        int blockIndex2 = blockIndices[blockInfoIndex];
        boolean blockIsInvalid = blockIndex2 == -1;
        if (blockIsInvalid) {
            int blockIndex3 = getAvailableDisplayListIndex(blockIndices, numberOfBlocks, startIndexToFindAvailableRenderNode);
            blockIndices[blockInfoIndex] = blockIndex3;
            TextRenderNode[] textRenderNodeArr = this.mTextRenderNodes;
            if (textRenderNodeArr[blockIndex3] != null) {
                textRenderNodeArr[blockIndex3].isDirty = true;
            }
            startIndexToFindAvailableRenderNode2 = blockIndex3 + 1;
            blockIndex = blockIndex3;
        } else {
            startIndexToFindAvailableRenderNode2 = startIndexToFindAvailableRenderNode;
            blockIndex = blockIndex2;
        }
        TextRenderNode[] textRenderNodeArr2 = this.mTextRenderNodes;
        if (textRenderNodeArr2[blockIndex] == null) {
            textRenderNodeArr2[blockIndex] = new TextRenderNode("Text " + blockIndex);
        }
        boolean blockDisplayListIsInvalid = this.mTextRenderNodes[blockIndex].needsRecord();
        RenderNode blockDisplayList = this.mTextRenderNodes[blockIndex].renderNode;
        if (this.mTextRenderNodes[blockIndex].needsToBeShifted || blockDisplayListIsInvalid) {
            int blockBeginLine = blockInfoIndex == 0 ? 0 : blockEndLines[blockInfoIndex - 1] + 1;
            int top = layout.getLineTop(blockBeginLine);
            int bottom = layout.getLineBottom(blockEndLine);
            int right = this.mTextView.getWidth();
            if (this.mTextView.getHorizontallyScrolling()) {
                float min = Float.MAX_VALUE;
                float max = Float.MIN_VALUE;
                int line2 = blockBeginLine;
                while (line2 <= blockEndLine) {
                    min = Math.min(min, layout.getLineLeft(line2));
                    max = Math.max(max, layout.getLineRight(line2));
                    line2++;
                    blockIsInvalid = blockIsInvalid;
                }
                line = (int) min;
                right = (int) (LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS + max);
            } else {
                line = 0;
            }
            if (blockDisplayListIsInvalid) {
                RecordingCanvas recordingCanvas = blockDisplayList.beginRecording(right - line, bottom - top);
                try {
                    recordingCanvas.translate((float) (-line), (float) (-top));
                    layout.drawText(recordingCanvas, blockBeginLine, blockEndLine);
                    this.mTextRenderNodes[blockIndex].isDirty = false;
                    blockDisplayList.endRecording();
                    blockDisplayList.setClipToBounds(false);
                    z = false;
                } catch (Throwable th) {
                    blockDisplayList.endRecording();
                    blockDisplayList.setClipToBounds(false);
                    throw th;
                }
            } else {
                z = false;
            }
            blockDisplayList.setLeftTopRightBottom(line, top, right, bottom);
            this.mTextRenderNodes[blockIndex].needsToBeShifted = z;
        }
        ((RecordingCanvas) canvas).drawRenderNode(blockDisplayList);
        return startIndexToFindAvailableRenderNode2;
    }

    private int getAvailableDisplayListIndex(int[] blockIndices, int numberOfBlocks, int searchStartIndex) {
        int length = this.mTextRenderNodes.length;
        for (int i = searchStartIndex; i < length; i++) {
            boolean blockIndexFound = false;
            int j = 0;
            while (true) {
                if (j >= numberOfBlocks) {
                    break;
                } else if (blockIndices[j] == i) {
                    blockIndexFound = true;
                    break;
                } else {
                    j++;
                }
            }
            if (!blockIndexFound) {
                return i;
            }
        }
        this.mTextRenderNodes = (TextRenderNode[]) GrowingArrayUtils.append(this.mTextRenderNodes, length, (Object) null);
        return length;
    }

    private void drawCursor(Canvas canvas, int cursorOffsetVertical) {
        boolean translate = cursorOffsetVertical != 0;
        if (translate) {
            canvas.translate(0.0f, (float) cursorOffsetVertical);
        }
        Drawable drawable = this.mDrawableForCursor;
        if (drawable != null) {
            drawable.draw(canvas);
        }
        if (translate) {
            canvas.translate(0.0f, (float) (-cursorOffsetVertical));
        }
    }

    /* access modifiers changed from: package-private */
    public void invalidateHandlesAndActionMode() {
        SelectionModifierCursorController selectionModifierCursorController = this.mSelectionModifierCursorController;
        if (selectionModifierCursorController != null) {
            selectionModifierCursorController.invalidateHandles();
        }
        InsertionPointCursorController insertionPointCursorController = this.mInsertionPointCursorController;
        if (insertionPointCursorController != null) {
            insertionPointCursorController.invalidateHandle();
        }
        if (this.mTextActionMode != null) {
            invalidateActionMode();
        }
    }

    /* access modifiers changed from: package-private */
    public void invalidateTextDisplayList(Layout layout, int start, int end) {
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

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void invalidateTextDisplayList() {
        if (this.mTextRenderNodes != null) {
            int i = 0;
            while (true) {
                TextRenderNode[] textRenderNodeArr = this.mTextRenderNodes;
                if (i < textRenderNodeArr.length) {
                    if (textRenderNodeArr[i] != null) {
                        textRenderNodeArr[i].isDirty = true;
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateCursorPosition() {
        loadCursorDrawable();
        if (this.mDrawableForCursor != null) {
            Layout layout = this.mTextView.getLayout();
            int offset = this.mTextView.getSelectionStart();
            int line = layout.getLineForOffset(offset);
            int top = layout.getLineTop(line);
            int bottom = layout.getLineBottomWithoutSpacing(line);
            float PositionX = layout.getPrimaryHorizontal(offset, layout.shouldClampCursor(line));
            if (adjustCursorPos(line, layout)) {
                top = getCursorTop();
                bottom = getCursorBottom();
                PositionX = getCursorX();
            }
            updateCursorPosition(top, bottom, PositionX);
        }
    }

    /* access modifiers changed from: package-private */
    public void refreshTextActionMode() {
        if (extractedTextModeWillBeStarted()) {
            this.mRestartActionModeOnNextRefresh = false;
            return;
        }
        boolean hasSelection = this.mTextView.hasSelection();
        SelectionModifierCursorController selectionController = getSelectionController();
        InsertionPointCursorController insertionController = getInsertionController();
        if ((selectionController == null || !selectionController.isCursorBeingModified()) && (insertionController == null || !insertionController.isCursorBeingModified())) {
            if (hasSelection) {
                if (this.mTextView.getSelectionStart() > this.mTextView.getSelectionEnd()) {
                    stopTextActionModeWithPreservingSelection();
                    this.mRestartActionModeOnNextRefresh = false;
                }
                hideInsertionPointCursorController();
                if (this.mTextActionMode == null) {
                    if (this.mRestartActionModeOnNextRefresh) {
                        startSelectionActionModeAsync(false);
                    }
                } else if (selectionController == null || !selectionController.isActive()) {
                    stopTextActionModeWithPreservingSelection();
                    startSelectionActionModeAsync(false);
                } else {
                    this.mTextActionMode.invalidateContentRect();
                }
            } else if (insertionController == null || !insertionController.isActive()) {
                lambda$startActionModeInternal$0$Editor();
            } else {
                ActionMode actionMode = this.mTextActionMode;
                if (actionMode != null) {
                    actionMode.invalidateContentRect();
                }
            }
            this.mRestartActionModeOnNextRefresh = false;
            return;
        }
        this.mRestartActionModeOnNextRefresh = false;
    }

    /* access modifiers changed from: package-private */
    public void startInsertionActionMode() {
        Runnable runnable = this.mInsertionActionModeRunnable;
        if (runnable != null) {
            this.mTextView.removeCallbacks(runnable);
        }
        if (!extractedTextModeWillBeStarted()) {
            lambda$startActionModeInternal$0$Editor();
            this.mTextActionMode = this.mTextView.startActionMode(new TextActionModeCallback(1), 1);
            if (this.mTextActionMode != null && getInsertionController() != null) {
                getInsertionController().show();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public TextView getTextView() {
        return this.mTextView;
    }

    /* access modifiers changed from: package-private */
    public ActionMode getTextActionMode() {
        return this.mTextActionMode;
    }

    /* access modifiers changed from: package-private */
    public void setRestartActionModeOnNextRefresh(boolean value) {
        this.mRestartActionModeOnNextRefresh = value;
    }

    /* access modifiers changed from: protected */
    public void startSelectionActionModeAsync(boolean adjustSelection) {
        getSelectionActionModeHelper().startSelectionActionModeAsync(adjustSelection);
    }

    /* access modifiers changed from: package-private */
    public void startLinkActionModeAsync(int start, int end) {
        if (this.mTextView.getText() instanceof Spannable) {
            lambda$startActionModeInternal$0$Editor();
            this.mRequestingLinkActionMode = true;
            getSelectionActionModeHelper().startLinkActionModeAsync(start, end);
        }
    }

    /* access modifiers changed from: package-private */
    public void invalidateActionModeAsync() {
        getSelectionActionModeHelper().invalidateActionModeAsync();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void invalidateActionMode() {
        ActionMode actionMode = this.mTextActionMode;
        if (actionMode != null) {
            actionMode.invalidate();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private SelectionActionModeHelper getSelectionActionModeHelper() {
        if (this.mSelectionActionModeHelper == null) {
            this.mSelectionActionModeHelper = new SelectionActionModeHelper(this);
        }
        return this.mSelectionActionModeHelper;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean selectCurrentWordAndStartDrag() {
        Runnable runnable = this.mInsertionActionModeRunnable;
        if (runnable != null) {
            this.mTextView.removeCallbacks(runnable);
        }
        if (extractedTextModeWillBeStarted() || !checkField()) {
            return false;
        }
        if (!this.mTextView.hasSelection() && !selectCurrentWord()) {
            return false;
        }
        stopTextActionModeWithPreservingSelection();
        getSelectionController().enterDrag(2);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean checkField() {
        if (this.mTextView.canSelectText() && this.mTextView.requestFocus()) {
            return true;
        }
        Log.w("TextView", "TextView does not support text selection. Selection cancelled.");
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean startActionModeInternal(@TextActionMode int actionMode) {
        InputMethodManager imm;
        if (extractedTextModeWillBeStarted()) {
            return false;
        }
        if (this.mTextActionMode != null) {
            invalidateActionMode();
            return false;
        } else if (actionMode != 2 && (!checkField() || !this.mTextView.hasSelection())) {
            return false;
        } else {
            boolean selectionStarted = true;
            this.mTextActionMode = this.mTextView.startActionMode(new TextActionModeCallback(actionMode), 1);
            boolean selectableText = this.mTextView.isTextEditable() || this.mTextView.isTextSelectable();
            if (actionMode == 2 && !selectableText) {
                ActionMode actionMode2 = this.mTextActionMode;
                if (actionMode2 instanceof FloatingActionMode) {
                    ((FloatingActionMode) actionMode2).setOutsideTouchable(true, new PopupWindow.OnDismissListener() {
                        /* class android.widget.$$Lambda$Editor$TdqUlJ6RRep0wXYHaRH51nTa08I */

                        @Override // android.widget.PopupWindow.OnDismissListener
                        public final void onDismiss() {
                            Editor.this.lambda$startActionModeInternal$0$Editor();
                        }
                    });
                }
            }
            if (this.mTextActionMode == null) {
                selectionStarted = false;
            }
            if (selectionStarted && this.mTextView.isTextEditable() && !this.mTextView.isTextSelectable() && this.mShowSoftInputOnFocus && (imm = getInputMethodManager()) != null) {
                imm.showSoftInput(this.mTextView, 0, null);
            }
            return selectionStarted;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean extractedTextModeWillBeStarted() {
        InputMethodManager imm;
        if (this.mTextView.isInExtractedMode() || (imm = getInputMethodManager()) == null || !imm.isFullscreenMode()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        if (selectionStart == selectionEnd) {
            for (SuggestionSpan suggestionSpan : suggestionSpans) {
                if (suggestionSpan.getSuggestions().length > 0) {
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
        for (int i = 0; i < suggestionSpans.length; i++) {
            int spanStart = spannable.getSpanStart(suggestionSpans[i]);
            int spanEnd = spannable.getSpanEnd(suggestionSpans[i]);
            minSpanStart = Math.min(minSpanStart, spanStart);
            maxSpanEnd = Math.max(maxSpanEnd, spanEnd);
            if (selectionStart >= spanStart && selectionStart <= spanEnd) {
                boolean hasValidSuggestions2 = hasValidSuggestions || suggestionSpans[i].getSuggestions().length > 0;
                unionOfSpansCoveringSelectionStartStart = Math.min(unionOfSpansCoveringSelectionStartStart, spanStart);
                unionOfSpansCoveringSelectionStartEnd = Math.max(unionOfSpansCoveringSelectionStartEnd, spanEnd);
                hasValidSuggestions = hasValidSuggestions2;
            }
        }
        if (hasValidSuggestions && unionOfSpansCoveringSelectionStartStart < unionOfSpansCoveringSelectionStartEnd && minSpanStart >= unionOfSpansCoveringSelectionStartStart && maxSpanEnd <= unionOfSpansCoveringSelectionStartEnd) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isCursorInsideEasyCorrectionSpan() {
        SuggestionSpan[] suggestionSpans;
        for (SuggestionSpan suggestionSpan : (SuggestionSpan[]) ((Spannable) this.mTextView.getText()).getSpans(this.mTextView.getSelectionStart(), this.mTextView.getSelectionEnd(), SuggestionSpan.class)) {
            if ((suggestionSpan.getFlags() & 1) != 0) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void onTouchUpEvent(MotionEvent event) {
        if (!getSelectionActionModeHelper().resetSelection(getTextView().getOffsetForPosition(event.getX(), event.getY()))) {
            boolean selectAllGotFocus = this.mSelectAllOnFocus && this.mTextView.didTouchFocusSelect();
            hideCursorAndSpanControllers();
            lambda$startActionModeInternal$0$Editor();
            CharSequence text = this.mTextView.getText();
            if (!selectAllGotFocus && text.length() > 0) {
                int offset = this.mTextView.getOffsetForPosition(event.getX(), event.getY());
                setPosWithMotionEvent(event, true);
                int offset2 = adjustOffsetAtLineEndForTouchPos(offset);
                boolean shouldInsertCursor = true ^ this.mRequestingLinkActionMode;
                if (shouldInsertCursor) {
                    Selection.setSelection((Spannable) text, offset2);
                    SpellChecker spellChecker = this.mSpellChecker;
                    if (spellChecker != null) {
                        spellChecker.onSelectionChanged();
                    }
                }
                if (extractedTextModeWillBeStarted()) {
                    return;
                }
                if (isCursorInsideEasyCorrectionSpan()) {
                    Runnable runnable = this.mInsertionActionModeRunnable;
                    if (runnable != null) {
                        this.mTextView.removeCallbacks(runnable);
                    }
                    this.mShowSuggestionRunnable = new Runnable() {
                        /* class android.widget.$$Lambda$DZXn7FbDDFyBvNjIiG9_hfa7kw */

                        @Override // java.lang.Runnable
                        public final void run() {
                            Editor.this.replace();
                        }
                    };
                    this.mTextView.postDelayed(this.mShowSuggestionRunnable, (long) ViewConfiguration.getDoubleTapTimeout());
                } else if (!hasInsertionController()) {
                } else {
                    if (shouldInsertCursor) {
                        getInsertionController().show();
                    } else {
                        getInsertionController().hide();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void onTextOperationUserChanged() {
        SpellChecker spellChecker = this.mSpellChecker;
        if (spellChecker != null) {
            spellChecker.resetSession();
        }
    }

    /* access modifiers changed from: protected */
    /* renamed from: stopTextActionMode */
    public void lambda$startActionModeInternal$0$Editor() {
        ActionMode actionMode = this.mTextActionMode;
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    private void stopTextActionModeWithPreservingSelection() {
        if (this.mTextActionMode != null) {
            this.mRestartActionModeOnNextRefresh = true;
        }
        this.mPreserveSelection = true;
        lambda$startActionModeInternal$0$Editor();
        this.mPreserveSelection = false;
    }

    /* access modifiers changed from: package-private */
    public boolean hasInsertionController() {
        return this.mInsertionControllerEnabled;
    }

    /* access modifiers changed from: package-private */
    public boolean hasSelectionController() {
        return this.mSelectionControllerEnabled;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private InsertionPointCursorController getInsertionController() {
        if (!this.mInsertionControllerEnabled) {
            return null;
        }
        if (this.mInsertionPointCursorController == null) {
            this.mInsertionPointCursorController = new InsertionPointCursorController();
            this.mTextView.getViewTreeObserver().addOnTouchModeChangeListener(this.mInsertionPointCursorController);
        }
        return this.mInsertionPointCursorController;
    }

    /* access modifiers changed from: package-private */
    public SelectionModifierCursorController getSelectionController() {
        if (!this.mSelectionControllerEnabled) {
            return null;
        }
        if (this.mSelectionModifierCursorController == null) {
            this.mSelectionModifierCursorController = new SelectionModifierCursorController();
            this.mTextView.getViewTreeObserver().addOnTouchModeChangeListener(this.mSelectionModifierCursorController);
        }
        return this.mSelectionModifierCursorController;
    }

    @VisibleForTesting
    public Drawable getCursorDrawable() {
        return this.mDrawableForCursor;
    }

    private void updateCursorPosition(int top, int bottom, float horizontal) {
        loadCursorDrawable();
        int left = clampHorizontalPosition(this.mDrawableForCursor, horizontal);
        this.mDrawableForCursor.setBounds(left, top - this.mTempRect.top, left + this.mDrawableForCursor.getIntrinsicWidth(), this.mTempRect.bottom + bottom);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int clampHorizontalPosition(Drawable drawable, float horizontal) {
        float horizontal2 = Math.max((float) LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS, horizontal - LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS);
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
        float horizontalDiff = horizontal2 - ((float) scrollX);
        int viewClippedWidth = (this.mTextView.getWidth() - this.mTextView.getCompoundPaddingLeft()) - this.mTextView.getCompoundPaddingRight();
        if (horizontalDiff >= ((float) viewClippedWidth) - 1.0f) {
            return (viewClippedWidth + scrollX) - (drawableWidth - this.mTempRect.right);
        }
        if (Math.abs(horizontalDiff) <= 1.0f || (TextUtils.isEmpty(this.mTextView.getText()) && ((float) (1048576 - scrollX)) <= ((float) viewClippedWidth) + 1.0f && horizontal2 <= 1.0f)) {
            return scrollX - this.mTempRect.left;
        }
        return ((int) horizontal2) - this.mTempRect.left;
    }

    public void onCommitCorrection(CorrectionInfo info) {
        CorrectionHighlighter correctionHighlighter = this.mCorrectionHighlighter;
        if (correctionHighlighter == null) {
            this.mCorrectionHighlighter = new CorrectionHighlighter();
        } else {
            correctionHighlighter.invalidate(false);
        }
        this.mCorrectionHighlighter.highlight(info);
        this.mUndoInputFilter.freezeLastEdit();
    }

    /* access modifiers changed from: package-private */
    public void onScrollChanged() {
        PositionListener positionListener = this.mPositionListener;
        if (positionListener != null) {
            positionListener.onScrollChanged();
        }
        ActionMode actionMode = this.mTextActionMode;
        if (actionMode != null) {
            actionMode.invalidateContentRect();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldBlink() {
        int start;
        int end;
        return isCursorVisible() && this.mTextView.isFocused() && (start = this.mTextView.getSelectionStart()) >= 0 && (end = this.mTextView.getSelectionEnd()) >= 0 && start == end;
    }

    /* access modifiers changed from: package-private */
    public void makeBlink() {
        if (shouldBlink()) {
            this.mShowCursor = SystemClock.uptimeMillis();
            if (this.mBlink == null) {
                this.mBlink = new Blink();
            }
            this.mTextView.removeCallbacks(this.mBlink);
            this.mTextView.postDelayed(this.mBlink, 500);
            return;
        }
        Blink blink = this.mBlink;
        if (blink != null) {
            this.mTextView.removeCallbacks(blink);
        }
    }

    /* access modifiers changed from: private */
    public class Blink implements Runnable {
        private boolean mCancelled;

        private Blink() {
        }

        @Override // java.lang.Runnable
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

        /* access modifiers changed from: package-private */
        public void cancel() {
            if (!this.mCancelled) {
                Editor.this.mTextView.removeCallbacks(this);
                this.mCancelled = true;
            }
        }

        /* access modifiers changed from: package-private */
        public void uncancel() {
            this.mCancelled = false;
        }
    }

    private View.DragShadowBuilder getTextThumbnailBuilder(int start, int end) {
        TextView shadowView = (TextView) View.inflate(this.mTextView.getContext(), R.layout.text_drag_thumbnail, null);
        if (shadowView != null) {
            if (end - start > 20) {
                end = TextUtils.unpackRangeEndFromLong(getCharClusterRange(start + 20));
            }
            shadowView.setText(this.mTextView.getTransformedText(start, end));
            shadowView.setTextColor(this.mTextView.getTextColors());
            shadowView.setTextAppearance(16);
            shadowView.setGravity(17);
            shadowView.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
            boolean isInvalidSize = false;
            int size = View.MeasureSpec.makeMeasureSpec(0, 0);
            shadowView.measure(size, size);
            shadowView.layout(0, 0, shadowView.getMeasuredWidth(), shadowView.getMeasuredHeight());
            shadowView.invalidate();
            if (shadowView.getWidth() <= 0 || shadowView.getHeight() <= 0) {
                isInvalidSize = true;
            }
            if (isInvalidSize) {
                return new View.DragShadowBuilder() {
                    /* class android.widget.Editor.AnonymousClass4 */

                    @Override // android.view.View.DragShadowBuilder
                    public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
                        super.onProvideShadowMetrics(outShadowSize, outShadowTouchPoint);
                        outShadowSize.set(1, 1);
                    }
                };
            }
            return new View.DragShadowBuilder(shadowView);
        }
        throw new IllegalArgumentException("Unable to inflate text drag thumbnail");
    }

    /* access modifiers changed from: private */
    public static class DragLocalState {
        public int end;
        public TextView sourceTextView;
        public int start;

        public DragLocalState(TextView sourceTextView2, int start2, int end2) {
            this.sourceTextView = sourceTextView2;
            this.start = start2;
            this.end = end2;
        }
    }

    /* access modifiers changed from: package-private */
    public void onDrop(DragEvent event) {
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
                int offset = this.mTextView.getOffsetForPosition(event.getX(), event.getY());
                Object localState = event.getLocalState();
                DragLocalState dragLocalState = null;
                if (localState instanceof DragLocalState) {
                    dragLocalState = (DragLocalState) localState;
                }
                boolean dragDropIntoItself = dragLocalState != null && dragLocalState.sourceTextView == this.mTextView;
                if (!dragDropIntoItself || offset < dragLocalState.start || offset >= dragLocalState.end) {
                    int originalLength = this.mTextView.getText().length();
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
        KeyListener keyListener = this.mKeyListener;
        if (keyListener != null) {
            text.setSpan(keyListener, 0, textLength, 18);
        }
        if (this.mSpanController == null) {
            this.mSpanController = new SpanController();
        }
        text.setSpan(this.mSpanController, 0, textLength, 18);
    }

    /* access modifiers changed from: package-private */
    public void setContextMenuAnchor(float x, float y) {
        this.mContextMenuAnchorX = x;
        this.mContextMenuAnchorY = y;
    }

    /* access modifiers changed from: package-private */
    public void onCreateContextMenu(ContextMenu menu) {
        int offset;
        if (!(this.mIsBeingLongClicked || Float.isNaN(this.mContextMenuAnchorX) || Float.isNaN(this.mContextMenuAnchorY) || (offset = this.mTextView.getOffsetForPosition(this.mContextMenuAnchorX, this.mContextMenuAnchorY)) == -1)) {
            stopTextActionModeWithPreservingSelection();
            if (this.mTextView.canSelectText()) {
                if (!(this.mTextView.hasSelection() && offset >= this.mTextView.getSelectionStart() && offset <= this.mTextView.getSelectionEnd())) {
                    Selection.setSelection((Spannable) this.mTextView.getText(), offset);
                    lambda$startActionModeInternal$0$Editor();
                }
            }
            if (shouldOfferToShowSuggestions()) {
                SuggestionInfo[] suggestionInfoArray = new SuggestionInfo[5];
                for (int i = 0; i < suggestionInfoArray.length; i++) {
                    suggestionInfoArray[i] = new SuggestionInfo();
                }
                SubMenu subMenu = menu.addSubMenu(0, 0, 10, R.string.replace);
                int numItems = this.mSuggestionHelper.getSuggestionInfo(suggestionInfoArray, null);
                for (int i2 = 0; i2 < numItems; i2++) {
                    final SuggestionInfo info = suggestionInfoArray[i2];
                    subMenu.add(0, 0, i2, info.mText).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        /* class android.widget.Editor.AnonymousClass5 */

                        @Override // android.view.MenuItem.OnMenuItemClickListener
                        public boolean onMenuItemClick(MenuItem item) {
                            Editor.this.replaceWithSuggestion(info);
                            return true;
                        }
                    });
                }
            }
            menu.add(0, 16908338, 3, R.string.undo).setAlphabeticShortcut(DateFormat.TIME_ZONE).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener).setEnabled(this.mTextView.canUndo());
            menu.add(0, 16908339, 4, R.string.redo).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener).setEnabled(this.mTextView.canRedo());
            menu.add(0, 16908320, 6, 17039363).setAlphabeticShortcut(EpicenterTranslateClipReveal.StateProperty.TARGET_X).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener).setEnabled(this.mTextView.canCut());
            menu.add(0, 16908321, 7, 17039361).setAlphabeticShortcut('c').setOnMenuItemClickListener(this.mOnContextMenuItemClickListener).setEnabled(this.mTextView.canCopy());
            menu.add(0, 16908322, 8, 17039371).setAlphabeticShortcut('v').setEnabled(this.mTextView.canPaste()).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener);
            menu.add(0, 16908337, 9, 17039385).setEnabled(this.mTextView.canPasteAsPlainText()).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener);
            menu.add(0, 16908341, 5, R.string.share).setEnabled(this.mTextView.canShare()).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener);
            menu.add(0, 16908319, 1, 17039373).setAlphabeticShortcut(DateFormat.AM_PM).setEnabled(this.mTextView.canSelectAllText()).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener);
            menu.add(0, 16908355, 11, 17039386).setEnabled(this.mTextView.canRequestAutofill()).setOnMenuItemClickListener(this.mOnContextMenuItemClickListener);
            this.mPreserveSelection = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private SuggestionSpan findEquivalentSuggestionSpan(SuggestionSpanInfo suggestionSpanInfo) {
        Editable editable = (Editable) this.mTextView.getText();
        if (editable.getSpanStart(suggestionSpanInfo.mSuggestionSpan) >= 0) {
            return suggestionSpanInfo.mSuggestionSpan;
        }
        SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) editable.getSpans(suggestionSpanInfo.mSpanStart, suggestionSpanInfo.mSpanEnd, SuggestionSpan.class);
        for (SuggestionSpan suggestionSpan : suggestionSpans) {
            if (editable.getSpanStart(suggestionSpan) == suggestionSpanInfo.mSpanStart && editable.getSpanEnd(suggestionSpan) == suggestionSpanInfo.mSpanEnd && suggestionSpan.equals(suggestionSpanInfo.mSuggestionSpan)) {
                return suggestionSpan;
            }
        }
        return null;
    }

    /* JADX INFO: Multiple debug info for r12v2 int: [D('i' int), D('suggestionStart' int)] */
    /* JADX INFO: Multiple debug info for r1v3 int: [D('i' int), D('newCursorPosition' int)] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void replaceWithSuggestion(SuggestionInfo suggestionInfo) {
        SuggestionSpan[] suggestionSpans;
        int length;
        String originalText;
        int spanStart;
        SuggestionSpan targetSuggestionSpan = findEquivalentSuggestionSpan(suggestionInfo.mSuggestionSpanInfo);
        if (targetSuggestionSpan != null) {
            Editable editable = (Editable) this.mTextView.getText();
            int spanStart2 = editable.getSpanStart(targetSuggestionSpan);
            int spanEnd = editable.getSpanEnd(targetSuggestionSpan);
            if (spanStart2 < 0) {
                return;
            }
            if (spanEnd > spanStart2) {
                String originalText2 = TextUtils.substring(editable, spanStart2, spanEnd);
                SuggestionSpan[] suggestionSpans2 = (SuggestionSpan[]) editable.getSpans(spanStart2, spanEnd, SuggestionSpan.class);
                int length2 = suggestionSpans2.length;
                int[] suggestionSpansStarts = new int[length2];
                int[] suggestionSpansEnds = new int[length2];
                int[] suggestionSpansFlags = new int[length2];
                for (int i = 0; i < length2; i++) {
                    SuggestionSpan suggestionSpan = suggestionSpans2[i];
                    suggestionSpansStarts[i] = editable.getSpanStart(suggestionSpan);
                    suggestionSpansEnds[i] = editable.getSpanEnd(suggestionSpan);
                    suggestionSpansFlags[i] = editable.getSpanFlags(suggestionSpan);
                    int suggestionSpanFlags = suggestionSpan.getFlags();
                    if ((suggestionSpanFlags & 2) != 0) {
                        suggestionSpan.setFlags(suggestionSpanFlags & -3 & -2);
                    }
                }
                String suggestion = suggestionInfo.mText.subSequence(suggestionInfo.mSuggestionStart, suggestionInfo.mSuggestionEnd).toString();
                this.mTextView.replaceText_internal(spanStart2, spanEnd, suggestion);
                targetSuggestionSpan.getSuggestions()[suggestionInfo.mSuggestionIndex] = originalText2;
                int lengthDelta = suggestion.length() - (spanEnd - spanStart2);
                int i2 = 0;
                while (i2 < length2) {
                    if (suggestionSpansStarts[i2] > spanStart2 || suggestionSpansEnds[i2] < spanEnd) {
                        spanStart = spanStart2;
                        originalText = originalText2;
                        suggestionSpans = suggestionSpans2;
                        length = length2;
                    } else {
                        spanStart = spanStart2;
                        originalText = originalText2;
                        suggestionSpans = suggestionSpans2;
                        length = length2;
                        this.mTextView.setSpan_internal(suggestionSpans2[i2], suggestionSpansStarts[i2], suggestionSpansEnds[i2] + lengthDelta, suggestionSpansFlags[i2]);
                    }
                    i2++;
                    editable = editable;
                    spanStart2 = spanStart;
                    originalText2 = originalText;
                    length2 = length;
                    suggestionSpans2 = suggestionSpans;
                }
                int newCursorPosition = spanEnd + lengthDelta;
                this.mTextView.setCursorPosition_internal(newCursorPosition, newCursorPosition);
            }
        }
    }

    /* access modifiers changed from: private */
    public class SpanController implements SpanWatcher {
        private static final int DISPLAY_TIMEOUT_MS = 3000;
        private Runnable mHidePopup;
        private EasyEditPopupWindow mPopupWindow;

        private SpanController() {
        }

        private boolean isNonIntermediateSelectionSpan(Spannable text, Object span) {
            return (Selection.SELECTION_START == span || Selection.SELECTION_END == span) && (text.getSpanFlags(span) & 512) == 0;
        }

        @Override // android.text.SpanWatcher
        public void onSpanAdded(Spannable text, Object span, int start, int end) {
            if (isNonIntermediateSelectionSpan(text, span)) {
                Editor.this.sendUpdateSelection();
            } else if (span instanceof EasyEditSpan) {
                if (this.mPopupWindow == null) {
                    this.mPopupWindow = new EasyEditPopupWindow();
                    this.mHidePopup = new Runnable() {
                        /* class android.widget.Editor.SpanController.AnonymousClass1 */

                        @Override // java.lang.Runnable
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
                    /* class android.widget.Editor.SpanController.AnonymousClass2 */

                    @Override // android.widget.Editor.EasyEditDeleteListener
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

        @Override // android.text.SpanWatcher
        public void onSpanRemoved(Spannable text, Object span, int start, int end) {
            if (isNonIntermediateSelectionSpan(text, span)) {
                Editor.this.sendUpdateSelection();
                return;
            }
            EasyEditPopupWindow easyEditPopupWindow = this.mPopupWindow;
            if (easyEditPopupWindow != null && span == easyEditPopupWindow.mEasyEditSpan) {
                hide();
            }
        }

        @Override // android.text.SpanWatcher
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
            EasyEditPopupWindow easyEditPopupWindow = this.mPopupWindow;
            if (easyEditPopupWindow != null) {
                easyEditPopupWindow.hide();
                Editor.this.mTextView.removeCallbacks(this.mHidePopup);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sendEasySpanNotification(int textChangedType, EasyEditSpan span) {
            try {
                PendingIntent pendingIntent = span.getPendingIntent();
                if (pendingIntent != null) {
                    Intent intent = new Intent();
                    intent.putExtra(EasyEditSpan.EXTRA_TEXT_CHANGED_TYPE, textChangedType);
                    pendingIntent.send(Editor.this.mTextView.getContext(), 0, intent);
                }
            } catch (PendingIntent.CanceledException e) {
                Log.w("Editor", "PendingIntent for notification cannot be sent", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public class EasyEditPopupWindow extends PinnedPopupWindow implements View.OnClickListener {
        private static final int POPUP_TEXT_LAYOUT = 17367316;
        private TextView mDeleteTextView;
        private EasyEditSpan mEasyEditSpan;
        private EasyEditDeleteListener mOnDeleteListener;

        private EasyEditPopupWindow() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public void createPopupWindow() {
            this.mPopupWindow = new PopupWindow(Editor.this.mTextView.getContext(), (AttributeSet) null, 16843464);
            this.mPopupWindow.setInputMethodMode(2);
            this.mPopupWindow.setClippingEnabled(true);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public void initContentView() {
            LinearLayout linearLayout = new LinearLayout(Editor.this.mTextView.getContext());
            linearLayout.setOrientation(0);
            this.mContentView = linearLayout;
            this.mContentView.setBackgroundResource(R.drawable.text_edit_side_paste_window);
            ViewGroup.LayoutParams wrapContent = new ViewGroup.LayoutParams(-2, -2);
            this.mDeleteTextView = (TextView) ((LayoutInflater) Editor.this.mTextView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(17367316, (ViewGroup) null);
            this.mDeleteTextView.setLayoutParams(wrapContent);
            this.mDeleteTextView.setText(R.string.delete);
            this.mDeleteTextView.setOnClickListener(this);
            this.mContentView.addView(this.mDeleteTextView);
        }

        public void setEasyEditSpan(EasyEditSpan easyEditSpan) {
            this.mEasyEditSpan = easyEditSpan;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setOnDeleteListener(EasyEditDeleteListener listener) {
            this.mOnDeleteListener = listener;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            EasyEditSpan easyEditSpan;
            EasyEditDeleteListener easyEditDeleteListener;
            if (view == this.mDeleteTextView && (easyEditSpan = this.mEasyEditSpan) != null && easyEditSpan.isDeleteEnabled() && (easyEditDeleteListener = this.mOnDeleteListener) != null) {
                easyEditDeleteListener.onDeleteClick(this.mEasyEditSpan);
            }
        }

        @Override // android.widget.Editor.PinnedPopupWindow
        public void hide() {
            EasyEditSpan easyEditSpan = this.mEasyEditSpan;
            if (easyEditSpan != null) {
                easyEditSpan.setDeleteEnabled(false);
            }
            this.mOnDeleteListener = null;
            super.hide();
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public int getTextOffset() {
            return ((Editable) Editor.this.mTextView.getText()).getSpanEnd(this.mEasyEditSpan);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public int getVerticalLocalPosition(int line) {
            return Editor.this.mTextView.getLayout().getLineBottomWithoutSpacing(line);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public int clipVertically(int positionY) {
            return positionY;
        }
    }

    /* access modifiers changed from: private */
    public class PositionListener implements ViewTreeObserver.OnPreDrawListener {
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
            for (int i = 0; i < 7; i++) {
                TextViewPositionListener listener = this.mPositionListeners[i];
                if (listener != positionListener) {
                    if (emptySlotIndex < 0 && listener == null) {
                        emptySlotIndex = i;
                    }
                } else {
                    return;
                }
            }
            this.mPositionListeners[emptySlotIndex] = positionListener;
            this.mCanMove[emptySlotIndex] = canMove;
            this.mNumberOfListeners++;
        }

        public void removeSubscriber(TextViewPositionListener positionListener) {
            int i = 0;
            while (true) {
                if (i >= 7) {
                    break;
                }
                TextViewPositionListener[] textViewPositionListenerArr = this.mPositionListeners;
                if (textViewPositionListenerArr[i] == positionListener) {
                    textViewPositionListenerArr[i] = null;
                    this.mNumberOfListeners--;
                    break;
                }
                i++;
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

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            TextViewPositionListener positionListener;
            updatePosition();
            boolean hasSelection = Editor.this.mTextView != null && Editor.this.mTextView.hasSelection();
            SelectionModifierCursorController selectionController = Editor.this.getSelectionController();
            boolean forceUpdate = hasSelection && selectionController != null && !selectionController.isActive();
            for (int i = 0; i < 7; i++) {
                if ((this.mPositionHasChanged || this.mScrollHasChanged || this.mCanMove[i] || forceUpdate) && (positionListener = this.mPositionListeners[i]) != null) {
                    positionListener.updatePosition(this.mPositionX, this.mPositionY, this.mPositionHasChanged, this.mScrollHasChanged || forceUpdate);
                }
            }
            this.mScrollHasChanged = false;
            return true;
        }

        private void updatePosition() {
            Editor.this.mTextView.getLocationInWindow(this.mTempCoords);
            int[] iArr = this.mTempCoords;
            this.mPositionHasChanged = (iArr[0] == this.mPositionX && iArr[1] == this.mPositionY) ? false : true;
            int[] iArr2 = this.mTempCoords;
            this.mPositionX = iArr2[0];
            this.mPositionY = iArr2[1];
            Editor.this.mTextView.getLocationOnScreen(this.mTempCoords);
            int[] iArr3 = this.mTempCoords;
            this.mPositionXOnScreen = iArr3[0];
            this.mPositionYOnScreen = iArr3[1];
        }

        public void onScrollChanged() {
            this.mScrollHasChanged = true;
        }
    }

    /* access modifiers changed from: private */
    public abstract class PinnedPopupWindow implements TextViewPositionListener {
        int mClippingLimitLeft;
        int mClippingLimitRight;
        protected ViewGroup mContentView;
        protected PopupWindow mPopupWindow;
        int mPositionX;
        int mPositionY;

        /* access modifiers changed from: protected */
        public abstract int clipVertically(int i);

        /* access modifiers changed from: protected */
        public abstract void createPopupWindow();

        /* access modifiers changed from: protected */
        public abstract int getTextOffset();

        /* access modifiers changed from: protected */
        public abstract int getVerticalLocalPosition(int i);

        /* access modifiers changed from: protected */
        public abstract void initContentView();

        /* access modifiers changed from: protected */
        public void setUp() {
        }

        public PinnedPopupWindow() {
            setUp();
            createPopupWindow();
            this.mPopupWindow.setWindowLayoutType(1005);
            this.mPopupWindow.setWidth(-2);
            this.mPopupWindow.setHeight(-2);
            initContentView();
            this.mContentView.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
            this.mPopupWindow.setContentView(this.mContentView);
        }

        public void show() {
            Editor.this.getPositionListener().addSubscriber(this, false);
            computeLocalPosition();
            PositionListener positionListener = Editor.this.getPositionListener();
            updatePosition(positionListener.getPositionX(), positionListener.getPositionY());
        }

        /* access modifiers changed from: protected */
        public void measureContent() {
            DisplayMetrics displayMetrics = Editor.this.mTextView.getResources().getDisplayMetrics();
            this.mContentView.measure(View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, Integer.MIN_VALUE));
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
            int positionY = clipVertically(this.mPositionY + parentPositionY);
            DisplayMetrics displayMetrics = Editor.this.mTextView.getResources().getDisplayMetrics();
            int positionX = Math.max(-this.mClippingLimitLeft, Math.min((displayMetrics.widthPixels - this.mContentView.getMeasuredWidth()) + this.mClippingLimitRight, this.mPositionX + parentPositionX));
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

        @Override // android.widget.Editor.TextViewPositionListener
        public void updatePosition(int parentPositionX, int parentPositionY, boolean parentPositionChanged, boolean parentScrolled) {
            if (!isShowing() || !Editor.this.isOffsetVisible(getTextOffset())) {
                hide();
                return;
            }
            if (parentScrolled) {
                computeLocalPosition();
            }
            updatePosition(parentPositionX, parentPositionY);
        }

        public boolean isShowing() {
            return this.mPopupWindow.isShowing();
        }
    }

    /* access modifiers changed from: private */
    public static final class SuggestionInfo {
        int mSuggestionEnd;
        int mSuggestionIndex;
        final SuggestionSpanInfo mSuggestionSpanInfo;
        int mSuggestionStart;
        final SpannableStringBuilder mText;

        private SuggestionInfo() {
            this.mSuggestionSpanInfo = new SuggestionSpanInfo();
            this.mText = new SpannableStringBuilder();
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            this.mSuggestionSpanInfo.clear();
            this.mText.clear();
        }

        /* access modifiers changed from: package-private */
        public void setSpanInfo(SuggestionSpan span, int spanStart, int spanEnd) {
            SuggestionSpanInfo suggestionSpanInfo = this.mSuggestionSpanInfo;
            suggestionSpanInfo.mSuggestionSpan = span;
            suggestionSpanInfo.mSpanStart = spanStart;
            suggestionSpanInfo.mSpanEnd = spanEnd;
        }
    }

    /* access modifiers changed from: private */
    public static final class SuggestionSpanInfo {
        int mSpanEnd;
        int mSpanStart;
        SuggestionSpan mSuggestionSpan;

        private SuggestionSpanInfo() {
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            this.mSuggestionSpan = null;
        }
    }

    /* access modifiers changed from: private */
    public class SuggestionHelper {
        private final HashMap<SuggestionSpan, Integer> mSpansLengths;
        private final Comparator<SuggestionSpan> mSuggestionSpanComparator;

        private SuggestionHelper() {
            this.mSuggestionSpanComparator = new SuggestionSpanComparator();
            this.mSpansLengths = new HashMap<>();
        }

        private class SuggestionSpanComparator implements Comparator<SuggestionSpan> {
            private SuggestionSpanComparator() {
            }

            public int compare(SuggestionSpan span1, SuggestionSpan span2) {
                int flag1 = span1.getFlags();
                int flag2 = span2.getFlags();
                if (flag1 != flag2) {
                    boolean misspelled2 = false;
                    boolean easy1 = (flag1 & 1) != 0;
                    boolean easy2 = (flag2 & 1) != 0;
                    boolean misspelled1 = (flag1 & 2) != 0;
                    if ((flag2 & 2) != 0) {
                        misspelled2 = true;
                    }
                    if (easy1 && !misspelled1) {
                        return -1;
                    }
                    if (easy2 && !misspelled2) {
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

        /* JADX INFO: Multiple debug info for r1v5 android.widget.Editor$SuggestionInfo: [D('suggestionInfo' android.widget.Editor$SuggestionInfo), D('i' int)] */
        public int getSuggestionInfo(SuggestionInfo[] suggestionInfos, SuggestionSpanInfo misspelledSpanInfo) {
            Spannable spannable;
            SuggestionSpan[] suggestionSpans;
            SuggestionInfo otherSuggestionInfo;
            SuggestionSpanInfo suggestionSpanInfo = misspelledSpanInfo;
            Spannable spannable2 = (Spannable) Editor.this.mTextView.getText();
            SuggestionSpan[] suggestionSpans2 = getSortedSuggestionSpans();
            SuggestionInfo suggestionInfo = null;
            if (suggestionSpans2.length == 0) {
                return 0;
            }
            int length = suggestionSpans2.length;
            int numberOfSuggestions = 0;
            int numberOfSuggestions2 = 0;
            while (numberOfSuggestions2 < length) {
                SuggestionSpan suggestionSpan = suggestionSpans2[numberOfSuggestions2];
                int spanStart = spannable2.getSpanStart(suggestionSpan);
                int spanEnd = spannable2.getSpanEnd(suggestionSpan);
                if (!(suggestionSpanInfo == null || (suggestionSpan.getFlags() & 2) == 0)) {
                    suggestionSpanInfo.mSuggestionSpan = suggestionSpan;
                    suggestionSpanInfo.mSpanStart = spanStart;
                    suggestionSpanInfo.mSpanEnd = spanEnd;
                }
                String[] suggestions = suggestionSpan.getSuggestions();
                int nbSuggestions = suggestions.length;
                int suggestionIndex = 0;
                while (suggestionIndex < nbSuggestions) {
                    String suggestion = suggestions[suggestionIndex];
                    int i = 0;
                    while (true) {
                        if (i < numberOfSuggestions) {
                            SuggestionInfo otherSuggestionInfo2 = suggestionInfos[i];
                            spannable = spannable2;
                            if (otherSuggestionInfo2.mText.toString().equals(suggestion)) {
                                int otherSpanStart = otherSuggestionInfo2.mSuggestionSpanInfo.mSpanStart;
                                suggestionSpans = suggestionSpans2;
                                int otherSpanEnd = otherSuggestionInfo2.mSuggestionSpanInfo.mSpanEnd;
                                if (spanStart == otherSpanStart && spanEnd == otherSpanEnd) {
                                    otherSuggestionInfo = null;
                                    break;
                                }
                            } else {
                                suggestionSpans = suggestionSpans2;
                            }
                            i++;
                            spannable2 = spannable;
                            suggestionSpans2 = suggestionSpans;
                        } else {
                            spannable = spannable2;
                            suggestionSpans = suggestionSpans2;
                            SuggestionInfo suggestionInfo2 = suggestionInfos[numberOfSuggestions];
                            suggestionInfo2.setSpanInfo(suggestionSpan, spanStart, spanEnd);
                            suggestionInfo2.mSuggestionIndex = suggestionIndex;
                            otherSuggestionInfo = null;
                            suggestionInfo2.mSuggestionStart = 0;
                            suggestionInfo2.mSuggestionEnd = suggestion.length();
                            suggestionInfo2.mText.replace(0, suggestionInfo2.mText.length(), (CharSequence) suggestion);
                            numberOfSuggestions++;
                            if (numberOfSuggestions >= suggestionInfos.length) {
                                return numberOfSuggestions;
                            }
                        }
                    }
                    suggestionIndex++;
                    suggestionInfo = otherSuggestionInfo;
                    spannable2 = spannable;
                    suggestionSpans2 = suggestionSpans;
                }
                numberOfSuggestions2++;
                suggestionSpanInfo = misspelledSpanInfo;
            }
            return numberOfSuggestions;
        }
    }

    /* access modifiers changed from: private */
    public final class SuggestionsPopupWindow extends PinnedPopupWindow implements AdapterView.OnItemClickListener {
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
            private CustomPopupWindow() {
            }

            @Override // android.widget.PopupWindow
            public void dismiss() {
                if (isShowing()) {
                    super.dismiss();
                    Editor.this.getPositionListener().removeSubscriber(SuggestionsPopupWindow.this);
                    ((Spannable) Editor.this.mTextView.getText()).removeSpan(Editor.this.mSuggestionRangeSpan);
                    Editor.this.mTextView.setCursorVisible(SuggestionsPopupWindow.this.mCursorWasVisibleBeforeSuggestions);
                    if (Editor.this.hasInsertionController() && !Editor.this.extractedTextModeWillBeStarted()) {
                        Editor.this.getInsertionController().show();
                    }
                }
            }
        }

        public SuggestionsPopupWindow() {
            super();
            this.mCursorWasVisibleBeforeSuggestions = Editor.this.mCursorVisible;
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public void setUp() {
            this.mContext = applyDefaultTheme(Editor.this.mTextView.getContext());
            this.mHighlightSpan = new TextAppearanceSpan(this.mContext, Editor.this.mTextView.mTextEditSuggestionHighlightStyle);
        }

        private Context applyDefaultTheme(Context originalContext) {
            int themeId;
            TypedArray a = originalContext.obtainStyledAttributes(new int[]{16844176});
            if (a.getBoolean(0, true)) {
                themeId = 16974410;
            } else {
                themeId = 16974411;
            }
            a.recycle();
            return new ContextThemeWrapper(originalContext, themeId);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public void createPopupWindow() {
            this.mPopupWindow = new CustomPopupWindow();
            this.mPopupWindow.setInputMethodMode(2);
            this.mPopupWindow.setBackgroundDrawable(new ColorDrawable(0));
            this.mPopupWindow.setFocusable(true);
            this.mPopupWindow.setClippingEnabled(false);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public void initContentView() {
            this.mContentView = (ViewGroup) ((LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(Editor.this.mTextView.mTextEditSuggestionContainerLayout, (ViewGroup) null);
            this.mContainerView = (LinearLayout) this.mContentView.findViewById(R.id.suggestionWindowContainer);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.mContainerView.getLayoutParams();
            this.mContainerMarginWidth = lp.leftMargin + lp.rightMargin;
            this.mContainerMarginTop = lp.topMargin;
            this.mClippingLimitLeft = lp.leftMargin;
            this.mClippingLimitRight = lp.rightMargin;
            this.mSuggestionListView = (ListView) this.mContentView.findViewById(R.id.suggestionContainer);
            this.mSuggestionsAdapter = new SuggestionAdapter();
            this.mSuggestionListView.setAdapter((ListAdapter) this.mSuggestionsAdapter);
            this.mSuggestionListView.setOnItemClickListener(this);
            this.mSuggestionInfos = new SuggestionInfo[5];
            int i = 0;
            while (true) {
                SuggestionInfo[] suggestionInfoArr = this.mSuggestionInfos;
                if (i < suggestionInfoArr.length) {
                    suggestionInfoArr[i] = new SuggestionInfo();
                    i++;
                } else {
                    this.mAddToDictionaryButton = (TextView) this.mContentView.findViewById(R.id.addToDictionaryButton);
                    this.mAddToDictionaryButton.setOnClickListener(new View.OnClickListener() {
                        /* class android.widget.Editor.SuggestionsPopupWindow.AnonymousClass1 */

                        @Override // android.view.View.OnClickListener
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
                                    Editor.this.mTextView.startActivityAsTextOperationUserIfNecessary(intent);
                                    editable.removeSpan(SuggestionsPopupWindow.this.mMisspelledSpanInfo.mSuggestionSpan);
                                    Selection.setSelection(editable, spanEnd);
                                    Editor.this.updateSpellCheckSpans(spanStart, spanEnd, false);
                                    SuggestionsPopupWindow.this.hideWithCleanUp();
                                }
                            }
                        }
                    });
                    this.mDeleteButton = (TextView) this.mContentView.findViewById(R.id.deleteButton);
                    this.mDeleteButton.setOnClickListener(new View.OnClickListener() {
                        /* class android.widget.Editor.SuggestionsPopupWindow.AnonymousClass2 */

                        @Override // android.view.View.OnClickListener
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
                    return;
                }
            }
        }

        public boolean isShowingUp() {
            return this.mIsShowingUp;
        }

        public void onParentLostFocus() {
            this.mIsShowingUp = false;
        }

        /* access modifiers changed from: private */
        public class SuggestionAdapter extends BaseAdapter {
            private LayoutInflater mInflater;

            private SuggestionAdapter() {
                this.mInflater = (LayoutInflater) SuggestionsPopupWindow.this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            @Override // android.widget.Adapter
            public int getCount() {
                return SuggestionsPopupWindow.this.mNumberOfSuggestions;
            }

            @Override // android.widget.Adapter
            public Object getItem(int position) {
                return SuggestionsPopupWindow.this.mSuggestionInfos[position];
            }

            @Override // android.widget.Adapter
            public long getItemId(int position) {
                return (long) position;
            }

            @Override // android.widget.Adapter
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) convertView;
                if (textView == null) {
                    textView = (TextView) this.mInflater.inflate(Editor.this.mTextView.mTextEditSuggestionItemLayout, parent, false);
                }
                textView.setText(SuggestionsPopupWindow.this.mSuggestionInfos[position].mText);
                return textView;
            }
        }

        @Override // android.widget.Editor.PinnedPopupWindow
        public void show() {
            if ((Editor.this.mTextView.getText() instanceof Editable) && !Editor.this.extractedTextModeWillBeStarted()) {
                int i = 0;
                if (updateSuggestions()) {
                    this.mCursorWasVisibleBeforeSuggestions = Editor.this.mCursorVisible;
                    Editor.this.mTextView.setCursorVisible(false);
                    this.mIsShowingUp = true;
                    super.show();
                }
                ListView listView = this.mSuggestionListView;
                if (this.mNumberOfSuggestions == 0) {
                    i = 8;
                }
                listView.setVisibility(i);
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public void measureContent() {
            DisplayMetrics displayMetrics = Editor.this.mTextView.getResources().getDisplayMetrics();
            int horizontalMeasure = View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, Integer.MIN_VALUE);
            int verticalMeasure = View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, Integer.MIN_VALUE);
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
            int width2 = Math.max(width, this.mDeleteButton.getMeasuredWidth()) + this.mContainerView.getPaddingLeft() + this.mContainerView.getPaddingRight() + this.mContainerMarginWidth;
            this.mContentView.measure(View.MeasureSpec.makeMeasureSpec(width2, 1073741824), verticalMeasure);
            Drawable popupBackground = this.mPopupWindow.getBackground();
            if (popupBackground != null) {
                if (Editor.this.mTempRect == null) {
                    Editor.this.mTempRect = new Rect();
                }
                popupBackground.getPadding(Editor.this.mTempRect);
                width2 += Editor.this.mTempRect.left + Editor.this.mTempRect.right;
            }
            this.mPopupWindow.setWidth(width2);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public int getTextOffset() {
            return (Editor.this.mTextView.getSelectionStart() + Editor.this.mTextView.getSelectionStart()) / 2;
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public int getVerticalLocalPosition(int line) {
            return Editor.this.mTextView.getLayout().getLineBottomWithoutSpacing(line) - this.mContainerMarginTop;
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public int clipVertically(int positionY) {
            return Math.min(positionY, Editor.this.mTextView.getResources().getDisplayMetrics().heightPixels - this.mContentView.getMeasuredHeight());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void hideWithCleanUp() {
            for (SuggestionInfo info : this.mSuggestionInfos) {
                info.clear();
            }
            this.mMisspelledSpanInfo.clear();
            hide();
        }

        private boolean updateSuggestions() {
            int underlineColor;
            Spannable spannable = (Spannable) Editor.this.mTextView.getText();
            this.mNumberOfSuggestions = Editor.this.mSuggestionHelper.getSuggestionInfo(this.mSuggestionInfos, this.mMisspelledSpanInfo);
            if (this.mNumberOfSuggestions == 0 && this.mMisspelledSpanInfo.mSuggestionSpan == null) {
                return false;
            }
            int spanUnionStart = Editor.this.mTextView.getText().length();
            int spanUnionEnd = 0;
            for (int i = 0; i < this.mNumberOfSuggestions; i++) {
                SuggestionSpanInfo spanInfo = this.mSuggestionInfos[i].mSuggestionSpanInfo;
                spanUnionStart = Math.min(spanUnionStart, spanInfo.mSpanStart);
                spanUnionEnd = Math.max(spanUnionEnd, spanInfo.mSpanEnd);
            }
            if (this.mMisspelledSpanInfo.mSuggestionSpan != null) {
                spanUnionStart = Math.min(spanUnionStart, this.mMisspelledSpanInfo.mSpanStart);
                spanUnionEnd = Math.max(spanUnionEnd, this.mMisspelledSpanInfo.mSpanEnd);
            }
            for (int i2 = 0; i2 < this.mNumberOfSuggestions; i2++) {
                highlightTextDifferences(this.mSuggestionInfos[i2], spanUnionStart, spanUnionEnd);
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
            spannable.setSpan(Editor.this.mSuggestionRangeSpan, spanUnionStart, spanUnionEnd, 33);
            this.mSuggestionsAdapter.notifyDataSetChanged();
            return true;
        }

        private void highlightTextDifferences(SuggestionInfo suggestionInfo, int unionStart, int unionEnd) {
            int spanStart = suggestionInfo.mSuggestionSpanInfo.mSpanStart;
            int spanEnd = suggestionInfo.mSuggestionSpanInfo.mSpanEnd;
            suggestionInfo.mSuggestionStart = spanStart - unionStart;
            suggestionInfo.mSuggestionEnd = suggestionInfo.mSuggestionStart + suggestionInfo.mText.length();
            suggestionInfo.mText.setSpan(this.mHighlightSpan, 0, suggestionInfo.mText.length(), 33);
            String textAsString = ((Spannable) Editor.this.mTextView.getText()).toString();
            suggestionInfo.mText.insert(0, (CharSequence) textAsString.substring(unionStart, spanStart));
            suggestionInfo.mText.append((CharSequence) textAsString.substring(spanEnd, unionEnd));
        }

        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Editor.this.replaceWithSuggestion(this.mSuggestionInfos[position]);
            hideWithCleanUp();
        }
    }

    /* access modifiers changed from: private */
    public class TextActionModeCallback extends ActionMode.Callback2 {
        private final Map<MenuItem, View.OnClickListener> mAssistClickHandlers = new HashMap();
        private final int mHandleHeight;
        private final boolean mHasSelection;
        private final RectF mSelectionBounds = new RectF();
        private final Path mSelectionPath = new Path();

        TextActionModeCallback(@TextActionMode int mode) {
            this.mHasSelection = mode == 0 || (Editor.this.mTextIsSelectable && mode == 2);
            if (this.mHasSelection) {
                SelectionModifierCursorController selectionController = Editor.this.getSelectionController();
                if (selectionController != null && selectionController.mStartHandle == null) {
                    Editor.this.loadHandleDrawables(false);
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

        @Override // android.view.ActionMode.Callback
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            this.mAssistClickHandlers.clear();
            mode.setTitle((CharSequence) null);
            mode.setSubtitle((CharSequence) null);
            mode.setTitleOptionalHint(true);
            populateMenuWithItems(menu);
            ActionMode.Callback customCallback = getCustomCallback();
            if (customCallback == null || customCallback.onCreateActionMode(mode, menu)) {
                if (Editor.this.mTextView.canProcessText()) {
                    Editor.this.mProcessTextIntentActionsHandler.onInitializeMenu(menu);
                }
                if (this.mHasSelection && !Editor.this.mTextView.hasTransientState()) {
                    Editor.this.mTextView.setHasTransientState(true);
                }
                return true;
            }
            Selection.setSelection((Spannable) Editor.this.mTextView.getText(), Editor.this.mTextView.getSelectionEnd());
            return false;
        }

        private ActionMode.Callback getCustomCallback() {
            if (this.mHasSelection) {
                return Editor.this.mCustomSelectionActionModeCallback;
            }
            return Editor.this.mCustomInsertionActionModeCallback;
        }

        private void populateMenuWithItems(Menu menu) {
            String selected;
            if (Editor.this.mTextView.canCut()) {
                menu.add(0, 16908320, 6, 17039363).setAlphabeticShortcut(EpicenterTranslateClipReveal.StateProperty.TARGET_X).setIcon(com.android.hwext.internal.R.drawable.floatingtoolbar_cut).setShowAsAction(2);
            }
            if (Editor.this.mTextView.canCopy()) {
                menu.add(0, 16908321, 7, 17039361).setAlphabeticShortcut('c').setIcon(com.android.hwext.internal.R.drawable.floatingtoolbar_copy).setShowAsAction(2);
            }
            if (Editor.this.mTextView.canPaste()) {
                menu.add(0, 16908322, 8, 17039371).setAlphabeticShortcut('v').setIcon(com.android.hwext.internal.R.drawable.floatingtoolbar_paste).setShowAsAction(2);
            }
            if (Editor.this.mTextView.canShare()) {
                menu.add(0, 16908341, 5, R.string.share).setIcon(com.android.hwext.internal.R.drawable.floatingtoolbar_share).setShowAsAction(1);
            }
            if (Editor.this.mTextView.canRequestAutofill() && ((selected = Editor.this.mTextView.getSelectedText()) == null || selected.isEmpty())) {
                menu.add(0, 16908355, 11, 17039386).setIcon(com.android.hwext.internal.R.drawable.floatingtoolbar_autofill).setShowAsAction(0);
            }
            if (Editor.this.mTextView.canPasteAsPlainText()) {
                menu.add(0, 16908337, 9, 17039385).setIcon(com.android.hwext.internal.R.drawable.floatingtoolbar_plain_text).setShowAsAction(1);
            }
            updateSelectAllItem(menu);
            updateReplaceItem(menu);
            updateAssistMenuItems(menu);
        }

        @Override // android.view.ActionMode.Callback
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            updateSelectAllItem(menu);
            updateReplaceItem(menu);
            updateAssistMenuItems(menu);
            ActionMode.Callback customCallback = getCustomCallback();
            if (customCallback != null) {
                return customCallback.onPrepareActionMode(mode, menu);
            }
            return true;
        }

        private void updateSelectAllItem(Menu menu) {
            boolean canSelectAll = Editor.this.mTextView.canSelectAllText();
            boolean selectAllItemExists = menu.findItem(16908319) != null;
            if (canSelectAll && !selectAllItemExists) {
                menu.add(0, 16908319, 1, 17039373).setIcon(com.android.hwext.internal.R.drawable.floatingtoolbar_select_all).setShowAsAction(1);
            } else if (!canSelectAll && selectAllItemExists) {
                menu.removeItem(16908319);
            }
        }

        private void updateReplaceItem(Menu menu) {
            boolean canReplace = Editor.this.mTextView.isSuggestionsEnabled() && Editor.this.shouldOfferToShowSuggestions();
            boolean replaceItemExists = menu.findItem(16908340) != null;
            if (canReplace && !replaceItemExists) {
                menu.add(0, 16908340, 10, R.string.replace).setIcon(com.android.hwext.internal.R.drawable.floatingtoolbar_replace).setShowAsAction(1);
            } else if (!canReplace && replaceItemExists) {
                menu.removeItem(16908340);
            }
        }

        private void updateAssistMenuItems(Menu menu) {
            TextClassification textClassification;
            clearAssistMenuItems(menu);
            if (shouldEnableAssistMenuItems() && (textClassification = Editor.this.getSelectionActionModeHelper().getTextClassification()) != null) {
                if (!textClassification.getActions().isEmpty()) {
                    addAssistMenuItem(menu, textClassification.getActions().get(0), 16908353, 2, 2).setIntent(textClassification.getIntent());
                } else if (hasLegacyAssistItem(textClassification)) {
                    MenuItem item = menu.add(16908353, 16908353, 2, textClassification.getLabel()).setIcon(textClassification.getIcon()).setIntent(textClassification.getIntent());
                    item.setShowAsAction(2);
                    this.mAssistClickHandlers.put(item, TextClassification.createIntentOnClickListener(TextClassification.createPendingIntent(Editor.this.mTextView.getContext(), textClassification.getIntent(), createAssistMenuItemPendingIntentRequestCode())));
                }
                int count = textClassification.getActions().size();
                for (int i = 1; i < count; i++) {
                    addAssistMenuItem(menu, textClassification.getActions().get(i), 0, (i + 50) - 1, 0);
                }
            }
        }

        private MenuItem addAssistMenuItem(Menu menu, RemoteAction action, int itemId, int order, int showAsAction) {
            MenuItem item = menu.add(16908353, itemId, order, action.getTitle()).setContentDescription(action.getContentDescription());
            if (action.shouldShowIcon()) {
                item.setIcon(action.getIcon().loadDrawable(Editor.this.mTextView.getContext()));
            }
            item.setShowAsAction(showAsAction);
            this.mAssistClickHandlers.put(item, TextClassification.createIntentOnClickListener(action.getActionIntent()));
            return item;
        }

        private void clearAssistMenuItems(Menu menu) {
            int i = 0;
            while (i < menu.size()) {
                MenuItem menuItem = menu.getItem(i);
                if (menuItem.getGroupId() == 16908353) {
                    menu.removeItem(menuItem.getItemId());
                } else {
                    i++;
                }
            }
        }

        private boolean hasLegacyAssistItem(TextClassification classification) {
            return (classification.getIcon() != null || !TextUtils.isEmpty(classification.getLabel())) && !(classification.getIntent() == null && classification.getOnClickListener() == null);
        }

        private boolean onAssistMenuItemClicked(MenuItem assistMenuItem) {
            Intent intent;
            Preconditions.checkArgument(assistMenuItem.getGroupId() == 16908353);
            TextClassification textClassification = Editor.this.getSelectionActionModeHelper().getTextClassification();
            if (!shouldEnableAssistMenuItems() || textClassification == null) {
                return true;
            }
            View.OnClickListener onClickListener = this.mAssistClickHandlers.get(assistMenuItem);
            if (onClickListener == null && (intent = assistMenuItem.getIntent()) != null) {
                onClickListener = TextClassification.createIntentOnClickListener(TextClassification.createPendingIntent(Editor.this.mTextView.getContext(), intent, createAssistMenuItemPendingIntentRequestCode()));
            }
            if (onClickListener != null) {
                onClickListener.onClick(Editor.this.mTextView);
                Editor.this.lambda$startActionModeInternal$0$Editor();
            }
            return true;
        }

        private int createAssistMenuItemPendingIntentRequestCode() {
            if (Editor.this.mTextView.hasSelection()) {
                return Editor.this.mTextView.getText().subSequence(Editor.this.mTextView.getSelectionStart(), Editor.this.mTextView.getSelectionEnd()).hashCode();
            }
            return 0;
        }

        private boolean shouldEnableAssistMenuItems() {
            return Editor.this.mTextView.isDeviceProvisioned() && TextClassificationManager.getSettings(Editor.this.mTextView.getContext()).isSmartTextShareEnabled();
        }

        @Override // android.view.ActionMode.Callback
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Editor.this.getSelectionActionModeHelper().onSelectionAction(item.getItemId(), item.getTitle().toString());
            if (Editor.this.mProcessTextIntentActionsHandler.performMenuItemAction(item)) {
                return true;
            }
            ActionMode.Callback customCallback = getCustomCallback();
            if (customCallback != null && customCallback.onActionItemClicked(mode, item)) {
                return true;
            }
            if (item.getGroupId() == 16908353 && onAssistMenuItemClicked(item)) {
                return true;
            }
            if (item.getItemId() != 16908320 || !HwDeviceManager.disallowOp(23)) {
                return Editor.this.mTextView.onTextContextMenuItem(item.getItemId());
            }
            Toast toast = Toast.makeText(Editor.this.mTextView.getContext(), Editor.this.mTextView.getContext().getResources().getString(33685904), 1);
            toast.getWindowParams().type = 2006;
            toast.show();
            Log.i("Editor", "TextView cut is not allowed by MDM!");
            return true;
        }

        @Override // android.view.ActionMode.Callback
        public void onDestroyActionMode(ActionMode mode) {
            Editor.this.getSelectionActionModeHelper().onDestroyActionMode();
            Editor.this.mTextActionMode = null;
            ActionMode.Callback customCallback = getCustomCallback();
            if (customCallback != null) {
                customCallback.onDestroyActionMode(mode);
            }
            if (!Editor.this.mPreserveSelection) {
                Selection.setSelection((Spannable) Editor.this.mTextView.getText(), Editor.this.mTextView.getSelectionEnd());
            }
            if (Editor.this.mSelectionModifierCursorController != null) {
                Editor.this.mSelectionModifierCursorController.hide();
            }
            this.mAssistClickHandlers.clear();
            Editor.this.mRequestingLinkActionMode = false;
        }

        @Override // android.view.ActionMode.Callback2
        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            if (!view.equals(Editor.this.mTextView) || Editor.this.mTextView.getLayout() == null) {
                super.onGetContentRect(mode, view, outRect);
                return;
            }
            if (Editor.this.mTextView.getSelectionStart() != Editor.this.mTextView.getSelectionEnd()) {
                this.mSelectionPath.reset();
                Editor.this.mTextView.getLayout().getSelectionPath(Editor.this.mTextView.getSelectionStart(), Editor.this.mTextView.getSelectionEnd(), this.mSelectionPath);
                this.mSelectionPath.computeBounds(this.mSelectionBounds, true);
                this.mSelectionBounds.bottom += (float) this.mHandleHeight;
            } else {
                Layout layout = Editor.this.mTextView.getLayout();
                int line = layout.getLineForOffset(Editor.this.mTextView.getSelectionStart());
                Editor editor = Editor.this;
                float primaryHorizontal = (float) editor.clampHorizontalPosition(null, layout.getPrimaryHorizontal(editor.mTextView.getSelectionStart()));
                this.mSelectionBounds.set(primaryHorizontal, (float) layout.getLineTop(line), primaryHorizontal, (float) (layout.getLineBottom(line) + this.mHandleHeight));
                Editor.this.adjustSelectionBounds(this.mSelectionBounds, line, layout, this.mHandleHeight);
            }
            int textHorizontalOffset = Editor.this.mTextView.viewportToContentHorizontalOffset();
            int textVerticalOffset = Editor.this.mTextView.viewportToContentVerticalOffset();
            outRect.set((int) Math.floor((double) (this.mSelectionBounds.left + ((float) textHorizontalOffset))), (int) Math.floor((double) (this.mSelectionBounds.top + ((float) textVerticalOffset))), (int) Math.ceil((double) (this.mSelectionBounds.right + ((float) textHorizontalOffset))), (int) Math.ceil((double) (this.mSelectionBounds.bottom + ((float) textVerticalOffset))));
        }
    }

    /* access modifiers changed from: private */
    public final class CursorAnchorInfoNotifier implements TextViewPositionListener {
        final CursorAnchorInfo.Builder mSelectionInfoBuilder;
        final int[] mTmpIntOffset;
        final Matrix mViewToScreenMatrix;

        private CursorAnchorInfoNotifier() {
            this.mSelectionInfoBuilder = new CursorAnchorInfo.Builder();
            this.mTmpIntOffset = new int[2];
            this.mViewToScreenMatrix = new Matrix();
        }

        @Override // android.widget.Editor.TextViewPositionListener
        public void updatePosition(int parentPositionX, int parentPositionY, boolean parentPositionChanged, boolean parentScrolled) {
            InputMethodManager imm;
            Layout layout;
            int insertionMarkerFlags;
            int composingTextStart;
            int temp;
            InputMethodState ims = Editor.this.mInputMethodState;
            if (ims != null && ims.mBatchEditNesting <= 0 && (imm = Editor.this.getInputMethodManager()) != null && imm.isActive(Editor.this.mTextView) && imm.isCursorAnchorInfoEnabled() && (layout = Editor.this.mTextView.getLayout()) != null) {
                CursorAnchorInfo.Builder builder = this.mSelectionInfoBuilder;
                builder.reset();
                int selectionStart = Editor.this.mTextView.getSelectionStart();
                builder.setSelectionRange(selectionStart, Editor.this.mTextView.getSelectionEnd());
                this.mViewToScreenMatrix.set(Editor.this.mTextView.getMatrix());
                Editor.this.mTextView.getAbsLocationOnScreen(this.mTmpIntOffset);
                Matrix matrix = this.mViewToScreenMatrix;
                int[] iArr = this.mTmpIntOffset;
                boolean hasComposingText = false;
                matrix.postTranslate((float) iArr[0], (float) iArr[1]);
                builder.setMatrix(this.mViewToScreenMatrix);
                float viewportToContentHorizontalOffset = (float) Editor.this.mTextView.viewportToContentHorizontalOffset();
                float viewportToContentVerticalOffset = (float) Editor.this.mTextView.viewportToContentVerticalOffset();
                CharSequence text = Editor.this.mTextView.getText();
                if (text instanceof Spannable) {
                    Spannable sp = (Spannable) text;
                    int composingTextStart2 = EditableInputConnection.getComposingSpanStart(sp);
                    int composingTextEnd = EditableInputConnection.getComposingSpanEnd(sp);
                    if (composingTextEnd < composingTextStart2) {
                        composingTextStart = composingTextEnd;
                        temp = composingTextStart2;
                    } else {
                        composingTextStart = composingTextStart2;
                        temp = composingTextEnd;
                    }
                    if (composingTextStart >= 0 && composingTextStart < temp) {
                        hasComposingText = true;
                    }
                    if (hasComposingText) {
                        builder.setComposingText(composingTextStart, text.subSequence(composingTextStart, temp));
                        Editor.this.mTextView.populateCharacterBounds(builder, composingTextStart, temp, viewportToContentHorizontalOffset, viewportToContentVerticalOffset);
                    }
                }
                if (selectionStart >= 0) {
                    int line = layout.getLineForOffset(selectionStart);
                    float insertionMarkerX = layout.getPrimaryHorizontal(selectionStart) + viewportToContentHorizontalOffset;
                    float insertionMarkerTop = ((float) layout.getLineTop(line)) + viewportToContentVerticalOffset;
                    float insertionMarkerBaseline = ((float) layout.getLineBaseline(line)) + viewportToContentVerticalOffset;
                    float insertionMarkerBottom = ((float) layout.getLineBottomWithoutSpacing(line)) + viewportToContentVerticalOffset;
                    boolean isTopVisible = Editor.this.mTextView.isPositionVisible(insertionMarkerX, insertionMarkerTop);
                    boolean isBottomVisible = Editor.this.mTextView.isPositionVisible(insertionMarkerX, insertionMarkerBottom);
                    int insertionMarkerFlags2 = 0;
                    if (isTopVisible || isBottomVisible) {
                        insertionMarkerFlags2 = 0 | 1;
                    }
                    if (!isTopVisible || !isBottomVisible) {
                        insertionMarkerFlags2 |= 2;
                    }
                    if (layout.isRtlCharAt(selectionStart)) {
                        insertionMarkerFlags = insertionMarkerFlags2 | 4;
                    } else {
                        insertionMarkerFlags = insertionMarkerFlags2;
                    }
                    builder.setInsertionMarkerLocation(insertionMarkerX, insertionMarkerTop, insertionMarkerBaseline, insertionMarkerBottom, insertionMarkerFlags);
                }
                imm.updateCursorAnchorInfo(Editor.this.mTextView, builder.build());
            }
        }
    }

    /* access modifiers changed from: private */
    public static class MagnifierMotionAnimator {
        private static final long DURATION = 100;
        private float mAnimationCurrentX;
        private float mAnimationCurrentY;
        private float mAnimationStartX;
        private float mAnimationStartY;
        private final ValueAnimator mAnimator;
        private float mLastX;
        private float mLastY;
        private final Magnifier mMagnifier;
        private boolean mMagnifierIsShowing;

        private MagnifierMotionAnimator(Magnifier magnifier) {
            this.mMagnifier = magnifier;
            this.mAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            this.mAnimator.setDuration(DURATION);
            this.mAnimator.setInterpolator(new LinearInterpolator());
            this.mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class android.widget.$$Lambda$Editor$MagnifierMotionAnimator$ERaelOMgCHAzvKgSSZEhDYeIg */

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Editor.MagnifierMotionAnimator.this.lambda$new$0$Editor$MagnifierMotionAnimator(valueAnimator);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$Editor$MagnifierMotionAnimator(ValueAnimator animation) {
            float f = this.mAnimationStartX;
            this.mAnimationCurrentX = f + ((this.mLastX - f) * animation.getAnimatedFraction());
            float f2 = this.mAnimationStartY;
            this.mAnimationCurrentY = f2 + ((this.mLastY - f2) * animation.getAnimatedFraction());
            this.mMagnifier.show(this.mAnimationCurrentX, this.mAnimationCurrentY);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void show(float x, float y) {
            if (this.mMagnifierIsShowing && y != this.mLastY) {
                if (this.mAnimator.isRunning()) {
                    this.mAnimator.cancel();
                    this.mAnimationStartX = this.mAnimationCurrentX;
                    this.mAnimationStartY = this.mAnimationCurrentY;
                } else {
                    this.mAnimationStartX = this.mLastX;
                    this.mAnimationStartY = this.mLastY;
                }
                this.mAnimator.start();
            } else if (!this.mAnimator.isRunning()) {
                this.mMagnifier.show(x, y);
            }
            this.mLastX = x;
            this.mLastY = y;
            this.mMagnifierIsShowing = true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void update() {
            this.mMagnifier.update();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void dismiss() {
            this.mMagnifier.dismiss();
            this.mAnimator.cancel();
            this.mMagnifierIsShowing = false;
        }
    }

    @VisibleForTesting
    public abstract class HandleView extends View implements TextViewPositionListener {
        private static final int HISTORY_SIZE = 5;
        private static final int TOUCH_UP_FILTER_DELAY_AFTER = 150;
        private static final int TOUCH_UP_FILTER_DELAY_BEFORE = 350;
        private final PopupWindow mContainer;
        private float mCurrentDragInitialTouchRawX;
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
        private float mTextViewScaleX;
        private float mTextViewScaleY;
        private float mTouchOffsetY;
        protected float mTouchToWindowOffsetX;
        protected float mTouchToWindowOffsetY;

        public abstract int getCurrentCursorOffset();

        /* access modifiers changed from: protected */
        public abstract int getHorizontalGravity(boolean z);

        /* access modifiers changed from: protected */
        public abstract int getHotspotX(Drawable drawable, boolean z);

        /* access modifiers changed from: protected */
        public abstract int getMagnifierHandleTrigger();

        /* access modifiers changed from: protected */
        public abstract void updatePosition(float f, float f2, boolean z);

        /* access modifiers changed from: protected */
        public abstract void updateSelection(int i);

        private HandleView(Drawable drawableLtr, Drawable drawableRtl, int id) {
            super(Editor.this.mTextView.getContext());
            this.mPreviousOffset = -1;
            this.mPositionHasChanged = true;
            this.mPrevLine = -1;
            this.mPreviousLineTouched = -1;
            this.mCurrentDragInitialTouchRawX = -1.0f;
            this.mPreviousOffsetsTimes = new long[5];
            this.mPreviousOffsets = new int[5];
            this.mPreviousOffsetIndex = 0;
            this.mNumberPreviousOffsets = 0;
            setId(id);
            this.mContainer = new PopupWindow(Editor.this.mTextView.getContext(), (AttributeSet) null, 16843464);
            this.mContainer.setSplitTouchEnabled(true);
            this.mContainer.setClippingEnabled(false);
            this.mContainer.setWindowLayoutType(1002);
            this.mContainer.setWidth(-2);
            this.mContainer.setHeight(-2);
            this.mContainer.setContentView(this);
            setDrawables(drawableLtr, drawableRtl);
            this.mMinSize = Editor.this.mTextView.getContext().getResources().getDimensionPixelSize(R.dimen.text_handle_min_size);
            int handleHeight = getPreferredHeight();
            this.mTouchOffsetY = ((float) handleHeight) * -0.3f;
            this.mIdealVerticalOffset = ((float) handleHeight) * 0.7f;
        }

        public float getIdealVerticalOffset() {
            return this.mIdealVerticalOffset;
        }

        /* access modifiers changed from: package-private */
        public void setDrawables(Drawable drawableLtr, Drawable drawableRtl) {
            this.mDrawableLtr = drawableLtr;
            this.mDrawableRtl = drawableRtl;
            updateDrawable(true);
        }

        /* access modifiers changed from: protected */
        public void updateDrawable(boolean updateDrawableWhenDragging) {
            Layout layout;
            if ((updateDrawableWhenDragging || !this.mIsDragging) && (layout = Editor.this.mTextView.getLayout()) != null) {
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

        private void startTouchUpFilter(int offset) {
            this.mNumberPreviousOffsets = 0;
            addPositionToTouchUpFilter(offset);
        }

        private void addPositionToTouchUpFilter(int offset) {
            this.mPreviousOffsetIndex = (this.mPreviousOffsetIndex + 1) % 5;
            int[] iArr = this.mPreviousOffsets;
            int i = this.mPreviousOffsetIndex;
            iArr[i] = offset;
            this.mPreviousOffsetsTimes[i] = SystemClock.uptimeMillis();
            this.mNumberPreviousOffsets++;
        }

        private void filterOnTouchUp(boolean fromTouchScreen) {
            long now = SystemClock.uptimeMillis();
            int i = 0;
            int index = this.mPreviousOffsetIndex;
            int iMax = Math.min(this.mNumberPreviousOffsets, 5);
            while (i < iMax && now - this.mPreviousOffsetsTimes[index] < 150) {
                i++;
                index = ((this.mPreviousOffsetIndex - i) + 5) % 5;
            }
            if (i > 0 && i < iMax && now - this.mPreviousOffsetsTimes[index] > 350) {
                positionAtCursorOffset(this.mPreviousOffsets[index], false, fromTouchScreen);
            }
        }

        public boolean offsetHasBeenChanged() {
            return this.mNumberPreviousOffsets > 1;
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(getPreferredWidth(), getPreferredHeight());
        }

        @Override // android.view.View
        public void invalidate() {
            super.invalidate();
            if (isShowing()) {
                positionAtCursorOffset(getCurrentCursorOffset(), true, false);
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
                positionAtCursorOffset(getCurrentCursorOffset(), false, false);
            }
        }

        /* access modifiers changed from: protected */
        public void dismiss() {
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

        private boolean shouldShow() {
            if (this.mIsDragging) {
                return true;
            }
            if (Editor.this.mTextView.isInBatchEditMode()) {
                return false;
            }
            return Editor.this.mTextView.isPositionVisible((float) (this.mPositionX + this.mHotspotX + getHorizontalOffset()), (float) this.mPositionY);
        }

        private void setVisible(boolean visible) {
            this.mContainer.getContentView().setVisibility(visible ? 0 : 4);
        }

        /* access modifiers changed from: protected */
        public boolean isAtRtlRun(Layout layout, int offset) {
            return layout.isRtlCharAt(offset);
        }

        @VisibleForTesting
        public float getHorizontal(Layout layout, int offset) {
            return layout.getPrimaryHorizontal(offset);
        }

        /* access modifiers changed from: protected */
        public int getOffsetAtCoordinate(Layout layout, int line, float x) {
            return Editor.this.mTextView.getOffsetAtCoordinate(line, x);
        }

        /* access modifiers changed from: protected */
        public void positionAtCursorOffset(int offset, boolean forceUpdatePosition, boolean fromTouchScreen) {
            if (Editor.this.mTextView.getLayout() == null) {
                Editor.this.prepareCursorControllers();
                return;
            }
            Layout layout = Editor.this.mTextView.getLayout();
            boolean offsetChanged = offset != this.mPreviousOffset;
            if (offsetChanged || forceUpdatePosition) {
                if (offsetChanged) {
                    updateSelection(offset);
                    if (fromTouchScreen && Editor.this.mHapticTextHandleEnabled) {
                        Editor.this.mTextView.performHapticFeedback(9);
                    }
                    addPositionToTouchUpFilter(offset);
                }
                int line = layout.getLineForOffset(offset);
                this.mPrevLine = line;
                this.mPositionX = ((getCursorHorizontalPosition(layout, offset) - this.mHotspotX) - getHorizontalOffset()) + getCursorOffset();
                this.mPositionY = layout.getLineBottomWithoutSpacing(line);
                int[] coordinate = {this.mPositionX, this.mPositionY};
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

        /* access modifiers changed from: package-private */
        public int getCursorHorizontalPosition(Layout layout, int offset) {
            if (Editor.this.mTextView == null || Editor.this.mTextView.getPaddingStart() != 0) {
                return (int) (getHorizontal(layout, offset) - Editor.LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS);
            }
            return (int) getHorizontal(layout, offset);
        }

        @Override // android.widget.Editor.TextViewPositionListener
        public void updatePosition(int parentPositionX, int parentPositionY, boolean parentPositionChanged, boolean parentScrolled) {
            positionAtCursorOffset(getCurrentCursorOffset(), parentScrolled, false);
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
                if (shouldShow()) {
                    int[] pts = {this.mPositionX + this.mHotspotX + getHorizontalOffset(), this.mPositionY};
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

        /* access modifiers changed from: protected */
        @Override // android.view.View
        public void onDraw(Canvas c) {
            int drawWidth = this.mDrawable.getIntrinsicWidth();
            int left = getHorizontalOffset();
            Drawable drawable = this.mDrawable;
            drawable.setBounds(left, 0, left + drawWidth, drawable.getIntrinsicHeight());
            this.mDrawable.draw(c);
        }

        private int getHorizontalOffset() {
            int width = getPreferredWidth();
            int drawWidth = this.mDrawable.getIntrinsicWidth();
            int i = this.mHorizontalGravity;
            if (i == 3) {
                return 0;
            }
            if (i != 5) {
                return (width - drawWidth) / 2;
            }
            return width - drawWidth;
        }

        /* access modifiers changed from: protected */
        public int getCursorOffset() {
            return 0;
        }

        private boolean tooLargeTextForMagnifier() {
            Paint.FontMetrics fontMetrics = Editor.this.mTextView.getPaint().getFontMetrics();
            return this.mTextViewScaleY * (fontMetrics.descent - fontMetrics.ascent) > ((float) Math.round(((float) Editor.this.mMagnifierAnimator.mMagnifier.getHeight()) / Editor.this.mMagnifierAnimator.mMagnifier.getZoom()));
        }

        private boolean checkForTransforms() {
            if (Editor.this.mMagnifierAnimator.mMagnifierIsShowing) {
                return true;
            }
            if (!(Editor.this.mTextView.getRotation() == 0.0f && Editor.this.mTextView.getRotationX() == 0.0f && Editor.this.mTextView.getRotationY() == 0.0f)) {
                return false;
            }
            this.mTextViewScaleX = Editor.this.mTextView.getScaleX();
            this.mTextViewScaleY = Editor.this.mTextView.getScaleY();
            for (ViewParent viewParent = Editor.this.mTextView.getParent(); viewParent != null; viewParent = viewParent.getParent()) {
                if (viewParent instanceof View) {
                    View view = (View) viewParent;
                    if (!(view.getRotation() == 0.0f && view.getRotationX() == 0.0f && view.getRotationY() == 0.0f)) {
                        return false;
                    }
                    this.mTextViewScaleX *= view.getScaleX();
                    this.mTextViewScaleY *= view.getScaleY();
                }
            }
            return true;
        }

        /* JADX WARNING: Removed duplicated region for block: B:39:0x00ff  */
        /* JADX WARNING: Removed duplicated region for block: B:47:0x0124  */
        /* JADX WARNING: Removed duplicated region for block: B:56:0x0178  */
        /* JADX WARNING: Removed duplicated region for block: B:65:0x01e3  */
        private boolean obtainMagnifierShowCoordinates(MotionEvent event, PointF showPosInView) {
            int otherHandleOffset;
            int offset;
            boolean rtl;
            float touchXInView;
            float leftBound;
            float rightBound;
            float leftBound2;
            float contentWidth;
            float scaledTouchXInView;
            int trigger = getMagnifierHandleTrigger();
            if (trigger == 0) {
                offset = Editor.this.mTextView.getSelectionStart();
                otherHandleOffset = -1;
            } else if (trigger == 1) {
                offset = Editor.this.mTextView.getSelectionStart();
                otherHandleOffset = Editor.this.mTextView.getSelectionEnd();
            } else if (trigger != 2) {
                offset = -1;
                otherHandleOffset = -1;
            } else if (Editor.this.mTextView.getLayout().getPrimaryHorizontal(Editor.this.mTextView.getSelectionEnd()) == 0.0f) {
                offset = Editor.this.mTextView.getSelectionEnd() - 1;
                otherHandleOffset = Editor.this.mTextView.getSelectionStart();
            } else {
                offset = Editor.this.mTextView.getSelectionEnd();
                otherHandleOffset = Editor.this.mTextView.getSelectionStart();
            }
            if (offset == -1) {
                return false;
            }
            if (event.getActionMasked() == 0) {
                this.mCurrentDragInitialTouchRawX = event.getRawX();
            } else if (event.getActionMasked() == 1) {
                this.mCurrentDragInitialTouchRawX = -1.0f;
            }
            Layout layout = Editor.this.mTextView.getLayout();
            int lineNumber = layout.getLineForOffset(offset);
            boolean sameLineSelection = otherHandleOffset != -1 && lineNumber == layout.getLineForOffset(otherHandleOffset);
            if (sameLineSelection) {
                if ((offset < otherHandleOffset) != (getHorizontal(Editor.this.mTextView.getLayout(), offset) < getHorizontal(Editor.this.mTextView.getLayout(), otherHandleOffset))) {
                    rtl = true;
                    int[] textViewLocationOnScreen = new int[2];
                    Editor.this.mTextView.getLocationOnScreen(textViewLocationOnScreen);
                    touchXInView = event.getRawX() - ((float) textViewLocationOnScreen[0]);
                    float leftBound3 = (float) (Editor.this.mTextView.getTotalPaddingLeft() - Editor.this.mTextView.getScrollX());
                    float rightBound2 = (float) (Editor.this.mTextView.getTotalPaddingLeft() - Editor.this.mTextView.getScrollX());
                    if (sameLineSelection) {
                        if ((trigger == 2) ^ rtl) {
                            leftBound = leftBound3 + getHorizontal(Editor.this.mTextView.getLayout(), otherHandleOffset);
                            if (sameLineSelection) {
                                if ((trigger == 1) ^ rtl) {
                                    rightBound = rightBound2 + getHorizontal(Editor.this.mTextView.getLayout(), otherHandleOffset);
                                    float f = this.mTextViewScaleX;
                                    leftBound2 = leftBound * f;
                                    float rightBound3 = rightBound * f;
                                    contentWidth = (float) Math.round(((float) Editor.this.mMagnifierAnimator.mMagnifier.getWidth()) / Editor.this.mMagnifierAnimator.mMagnifier.getZoom());
                                    if (touchXInView >= leftBound2 - (contentWidth / 2.0f)) {
                                        return false;
                                    }
                                    if (touchXInView > rightBound3 + (contentWidth / 2.0f)) {
                                        return false;
                                    }
                                    if (this.mTextViewScaleX == 1.0f) {
                                        scaledTouchXInView = touchXInView;
                                    } else {
                                        float scaledTouchXInView2 = event.getRawX();
                                        float f2 = this.mCurrentDragInitialTouchRawX;
                                        scaledTouchXInView = (((scaledTouchXInView2 - f2) * this.mTextViewScaleX) + f2) - ((float) textViewLocationOnScreen[0]);
                                    }
                                    showPosInView.x = Math.max(leftBound2, Math.min(rightBound3, scaledTouchXInView));
                                    showPosInView.y = (((((float) (Editor.this.mTextView.getLayout().getLineTop(lineNumber) + Editor.this.mTextView.getLayout().getLineBottom(lineNumber))) / 2.0f) + ((float) Editor.this.mTextView.getTotalPaddingTop())) - ((float) Editor.this.mTextView.getScrollY())) * this.mTextViewScaleY;
                                    return true;
                                }
                            }
                            rightBound = rightBound2 + Editor.this.mTextView.getLayout().getLineRight(lineNumber);
                            float f3 = this.mTextViewScaleX;
                            leftBound2 = leftBound * f3;
                            float rightBound32 = rightBound * f3;
                            contentWidth = (float) Math.round(((float) Editor.this.mMagnifierAnimator.mMagnifier.getWidth()) / Editor.this.mMagnifierAnimator.mMagnifier.getZoom());
                            if (touchXInView >= leftBound2 - (contentWidth / 2.0f)) {
                            }
                        }
                    }
                    leftBound = leftBound3 + Editor.this.mTextView.getLayout().getLineLeft(lineNumber);
                    if (sameLineSelection) {
                    }
                    rightBound = rightBound2 + Editor.this.mTextView.getLayout().getLineRight(lineNumber);
                    float f32 = this.mTextViewScaleX;
                    leftBound2 = leftBound * f32;
                    float rightBound322 = rightBound * f32;
                    contentWidth = (float) Math.round(((float) Editor.this.mMagnifierAnimator.mMagnifier.getWidth()) / Editor.this.mMagnifierAnimator.mMagnifier.getZoom());
                    if (touchXInView >= leftBound2 - (contentWidth / 2.0f)) {
                    }
                }
            }
            rtl = false;
            int[] textViewLocationOnScreen2 = new int[2];
            Editor.this.mTextView.getLocationOnScreen(textViewLocationOnScreen2);
            touchXInView = event.getRawX() - ((float) textViewLocationOnScreen2[0]);
            float leftBound32 = (float) (Editor.this.mTextView.getTotalPaddingLeft() - Editor.this.mTextView.getScrollX());
            float rightBound22 = (float) (Editor.this.mTextView.getTotalPaddingLeft() - Editor.this.mTextView.getScrollX());
            if (sameLineSelection) {
            }
            leftBound = leftBound32 + Editor.this.mTextView.getLayout().getLineLeft(lineNumber);
            if (sameLineSelection) {
            }
            rightBound = rightBound22 + Editor.this.mTextView.getLayout().getLineRight(lineNumber);
            float f322 = this.mTextViewScaleX;
            leftBound2 = leftBound * f322;
            float rightBound3222 = rightBound * f322;
            contentWidth = (float) Math.round(((float) Editor.this.mMagnifierAnimator.mMagnifier.getWidth()) / Editor.this.mMagnifierAnimator.mMagnifier.getZoom());
            if (touchXInView >= leftBound2 - (contentWidth / 2.0f)) {
            }
        }

        private boolean handleOverlapsMagnifier(HandleView handle, Rect magnifierRect) {
            PopupWindow window = handle.mContainer;
            if (!window.hasDecorView()) {
                return false;
            }
            return Rect.intersects(new Rect(window.getDecorViewLayoutParams().x, window.getDecorViewLayoutParams().y, window.getDecorViewLayoutParams().x + window.getContentView().getWidth(), window.getDecorViewLayoutParams().y + window.getContentView().getHeight()), magnifierRect);
        }

        private HandleView getOtherSelectionHandle() {
            SelectionModifierCursorController controller = Editor.this.getSelectionController();
            if (controller == null || !controller.isActive()) {
                return null;
            }
            if (controller.mStartHandle != this) {
                return controller.mStartHandle;
            }
            return controller.mEndHandle;
        }

        private void updateHandlesVisibility() {
            Point magnifierTopLeft = Editor.this.mMagnifierAnimator.mMagnifier.getPosition();
            if (magnifierTopLeft != null) {
                Rect magnifierRect = new Rect(magnifierTopLeft.x, magnifierTopLeft.y, magnifierTopLeft.x + Editor.this.mMagnifierAnimator.mMagnifier.getWidth(), magnifierTopLeft.y + Editor.this.mMagnifierAnimator.mMagnifier.getHeight());
                setVisible(!handleOverlapsMagnifier(this, magnifierRect));
                HandleView otherHandle = getOtherSelectionHandle();
                if (otherHandle != null) {
                    otherHandle.setVisible(!handleOverlapsMagnifier(otherHandle, magnifierRect));
                }
            }
        }

        /* access modifiers changed from: protected */
        public final void updateMagnifier(MotionEvent event) {
            if (Editor.this.mMagnifierAnimator != null) {
                PointF showPosInView = new PointF();
                if (checkForTransforms() && !tooLargeTextForMagnifier() && obtainMagnifierShowCoordinates(event, showPosInView)) {
                    Editor.this.mRenderCursorRegardlessTiming = true;
                    Editor.this.mTextView.invalidateCursorPath();
                    Editor.this.suspendBlink();
                    Editor.this.mMagnifierAnimator.show(showPosInView.x, showPosInView.y);
                    updateHandlesVisibility();
                    return;
                }
                dismissMagnifier();
            }
        }

        /* access modifiers changed from: protected */
        public final void dismissMagnifier() {
            if (Editor.this.mMagnifierAnimator != null) {
                Editor.this.mMagnifierAnimator.dismiss();
                Editor.this.mRenderCursorRegardlessTiming = false;
                Editor.this.resumeBlink();
                setVisible(true);
                HandleView otherHandle = getOtherSelectionHandle();
                if (otherHandle != null) {
                    otherHandle.setVisible(true);
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:7:0x0014, code lost:
            if (r0 != 3) goto L_0x00d5;
         */
        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent ev) {
            float newVerticalOffset;
            Editor.this.updateFloatingToolbarVisibility(ev);
            int actionMasked = ev.getActionMasked();
            if (actionMasked != 0) {
                if (actionMasked == 1) {
                    filterOnTouchUp(ev.isFromSource(4098));
                } else if (actionMasked == 2) {
                    float xInWindow = (ev.getRawX() - ((float) this.mLastParentXOnScreen)) + ((float) this.mLastParentX);
                    float rawY = ev.getRawY() - ((float) this.mLastParentYOnScreen);
                    int i = this.mLastParentY;
                    float yInWindow = rawY + ((float) i);
                    float previousVerticalOffset = this.mTouchToWindowOffsetY - ((float) i);
                    float currentVerticalOffset = (yInWindow - ((float) this.mPositionY)) - ((float) i);
                    float newVerticalOffset2 = this.mIdealVerticalOffset;
                    if (previousVerticalOffset < newVerticalOffset2) {
                        newVerticalOffset = Math.max(Math.min(currentVerticalOffset, newVerticalOffset2), previousVerticalOffset);
                    } else {
                        newVerticalOffset = Math.min(Math.max(currentVerticalOffset, newVerticalOffset2), previousVerticalOffset);
                    }
                    this.mTouchToWindowOffsetY = ((float) this.mLastParentY) + newVerticalOffset;
                    updatePosition((xInWindow - this.mTouchToWindowOffsetX) + ((float) this.mHotspotX) + ((float) getHorizontalOffset()), (yInWindow - this.mTouchToWindowOffsetY) + this.mTouchOffsetY, ev.isFromSource(4098));
                }
                this.mIsDragging = false;
                updateDrawable(false);
            } else {
                startTouchUpFilter(getCurrentCursorOffset());
                PositionListener positionListener = Editor.this.getPositionListener();
                this.mLastParentX = positionListener.getPositionX();
                this.mLastParentY = positionListener.getPositionY();
                this.mLastParentXOnScreen = positionListener.getPositionXOnScreen();
                this.mLastParentYOnScreen = positionListener.getPositionYOnScreen();
                float xInWindow2 = (ev.getRawX() - ((float) this.mLastParentXOnScreen)) + ((float) this.mLastParentX);
                float yInWindow2 = (ev.getRawY() - ((float) this.mLastParentYOnScreen)) + ((float) this.mLastParentY);
                this.mTouchToWindowOffsetX = xInWindow2 - ((float) this.mPositionX);
                this.mTouchToWindowOffsetY = yInWindow2 - ((float) this.mPositionY);
                this.mIsDragging = true;
                this.mPreviousLineTouched = -1;
            }
            return true;
        }

        public boolean isDragging() {
            return this.mIsDragging;
        }

        /* access modifiers changed from: package-private */
        public void onHandleMoved() {
        }

        public void onDetached() {
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View
        public void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            setSystemGestureExclusionRects(Collections.singletonList(new Rect(0, 0, w, h)));
        }
    }

    /* access modifiers changed from: protected */
    public class InsertionHandleView extends HandleView {
        private static final int DELAY_BEFORE_HANDLE_FADES_OUT = 4000;
        private static final int RECENT_CUT_COPY_DURATION = 15000;
        private float mDownPositionX;
        private float mDownPositionY;
        private Runnable mHider;

        public InsertionHandleView(Drawable drawable) {
            super(drawable, drawable, R.id.insertion_handle);
        }

        @Override // android.widget.Editor.HandleView
        public void show() {
            super.show();
            long durationSinceCutOrCopy = SystemClock.uptimeMillis() - TextView.sLastCutCopyOrTextChangedTime;
            if (Editor.this.mInsertionActionModeRunnable != null && (Editor.this.mTapState == 2 || Editor.this.mTapState == 3 || Editor.this.isCursorInsideEasyCorrectionSpan())) {
                Editor.this.mTextView.removeCallbacks(Editor.this.mInsertionActionModeRunnable);
            }
            if (Editor.this.mTapState != 2 && Editor.this.mTapState != 3 && !Editor.this.isCursorInsideEasyCorrectionSpan() && durationSinceCutOrCopy < 15000 && Editor.this.mTextActionMode == null) {
                if (Editor.this.mInsertionActionModeRunnable == null) {
                    Editor.this.mInsertionActionModeRunnable = new Runnable() {
                        /* class android.widget.Editor.InsertionHandleView.AnonymousClass1 */

                        @Override // java.lang.Runnable
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
                    /* class android.widget.Editor.InsertionHandleView.AnonymousClass2 */

                    @Override // java.lang.Runnable
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

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public int getHotspotX(Drawable drawable, boolean isRtlRun) {
            return drawable.getIntrinsicWidth() / 2;
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public int getHorizontalGravity(boolean isRtlRun) {
            return 1;
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public int getCursorOffset() {
            int offset = super.getCursorOffset();
            if (Editor.this.mDrawableForCursor == null) {
                return offset;
            }
            Editor.this.mDrawableForCursor.getPadding(Editor.this.mTempRect);
            return offset + (((Editor.this.mDrawableForCursor.getIntrinsicWidth() - Editor.this.mTempRect.left) - Editor.this.mTempRect.right) / 2);
        }

        /* access modifiers changed from: package-private */
        @Override // android.widget.Editor.HandleView
        public int getCursorHorizontalPosition(Layout layout, int offset) {
            if (Editor.this.mDrawableForCursor == null) {
                return super.getCursorHorizontalPosition(layout, offset);
            }
            float horizontal = getHorizontal(layout, offset);
            Editor editor = Editor.this;
            return editor.clampHorizontalPosition(editor.mDrawableForCursor, horizontal) + Editor.this.mTempRect.left;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:7:0x0017, code lost:
            if (r1 != 3) goto L_0x0097;
         */
        @Override // android.widget.Editor.HandleView, android.view.View
        public boolean onTouchEvent(MotionEvent ev) {
            boolean result = super.onTouchEvent(ev);
            Editor.this.setPosWithMotionEvent(ev, false);
            int actionMasked = ev.getActionMasked();
            if (actionMasked != 0) {
                if (actionMasked != 1) {
                    if (actionMasked == 2) {
                        updateMagnifier(ev);
                    }
                } else if (!offsetHasBeenChanged()) {
                    float deltaX = this.mDownPositionX - ev.getRawX();
                    float deltaY = this.mDownPositionY - ev.getRawY();
                    float distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);
                    int touchSlop = ViewConfiguration.get(Editor.this.mTextView.getContext()).getScaledTouchSlop();
                    if (distanceSquared < ((float) (touchSlop * touchSlop))) {
                        if (Editor.this.mTextActionMode != null) {
                            Editor.this.lambda$startActionModeInternal$0$Editor();
                        } else {
                            Editor.this.startInsertionActionMode();
                        }
                    }
                } else if (Editor.this.mTextActionMode != null) {
                    Editor.this.mTextActionMode.invalidateContentRect();
                } else {
                    Editor.this.mInsertionControllerEnabled = true;
                    Editor.this.startInsertionActionMode();
                }
                hideAfterDelay();
                dismissMagnifier();
            } else {
                this.mDownPositionX = ev.getRawX();
                this.mDownPositionY = ev.getRawY();
                updateMagnifier(ev);
            }
            return result;
        }

        @Override // android.widget.Editor.HandleView
        public int getCurrentCursorOffset() {
            Editor.this.recogniseLineEnd();
            return Editor.this.mTextView.getSelectionStart();
        }

        @Override // android.widget.Editor.HandleView
        public void updateSelection(int offset) {
            Selection.setSelection((Spannable) Editor.this.mTextView.getText(), offset);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public void updatePosition(float x, float y, boolean fromTouchScreen) {
            int currLine;
            Layout layout = Editor.this.mTextView.getLayout();
            if (layout != null) {
                if (this.mPreviousLineTouched == -1) {
                    this.mPreviousLineTouched = Editor.this.mTextView.getLineAtCoordinate(y);
                }
                int currLine2 = Editor.this.getCurrentLineAdjustedForSlop(layout, this.mPreviousLineTouched, y);
                int offset = getOffsetAtCoordinate(layout, currLine2, x);
                this.mPreviousLineTouched = currLine2;
                currLine = Editor.this.adjustOffsetAtLineEndForInsertHanlePos(offset);
            } else {
                currLine = -1;
            }
            positionAtCursorOffset(currLine, false, fromTouchScreen);
            if (Editor.this.mTextActionMode != null) {
                Editor.this.invalidateActionMode();
            }
        }

        /* access modifiers changed from: package-private */
        @Override // android.widget.Editor.HandleView
        public void onHandleMoved() {
            super.onHandleMoved();
            removeHiderCallback();
        }

        @Override // android.widget.Editor.HandleView
        public void onDetached() {
            super.onDetached();
            removeHiderCallback();
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public int getMagnifierHandleTrigger() {
            return 0;
        }
    }

    @VisibleForTesting
    public final class SelectionHandleView extends HandleView {
        private final int mHandleType;
        private boolean mInWord = false;
        private boolean mLanguageDirectionChanged = false;
        private float mPrevX;
        private final float mTextViewEdgeSlop;
        private final int[] mTextViewLocation = new int[2];
        private float mTouchWordDelta;

        public SelectionHandleView(Drawable drawableLtr, Drawable drawableRtl, int id, int handleType) {
            super(drawableLtr, drawableRtl, id);
            this.mHandleType = handleType;
            this.mTextViewEdgeSlop = (float) (ViewConfiguration.get(Editor.this.mTextView.getContext()).getScaledTouchSlop() * 4);
        }

        public boolean isStartHandle() {
            return this.mHandleType == 0;
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public int getHotspotX(Drawable drawable, boolean isRtlRun) {
            if (isRtlRun == isStartHandle()) {
                return drawable.getIntrinsicWidth() / 4;
            }
            return (drawable.getIntrinsicWidth() * 3) / 4;
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public int getHorizontalGravity(boolean isRtlRun) {
            return isRtlRun == isStartHandle() ? 3 : 5;
        }

        @Override // android.widget.Editor.HandleView
        public int getCurrentCursorOffset() {
            if (!isStartHandle()) {
                Editor.this.recogniseLineEnd();
            }
            return isStartHandle() ? Editor.this.mTextView.getSelectionStart() : Editor.this.mTextView.getSelectionEnd();
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public void updateSelection(int offset) {
            if (isStartHandle()) {
                Selection.setSelection((Spannable) Editor.this.mTextView.getText(), offset, Editor.this.mTextView.getSelectionEnd());
            } else {
                Selection.setSelection((Spannable) Editor.this.mTextView.getText(), Editor.this.mTextView.getSelectionStart(), offset);
            }
            updateDrawable(false);
            if (Editor.this.mTextActionMode != null) {
                Editor.this.invalidateActionMode();
            }
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:129:0x01e3, code lost:
            if (r9 < r25.mPrevLine) goto L_0x01e8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:78:0x0148, code lost:
            if (r25.this$0.mTextView.canScrollHorizontally(r7 ? -1 : 1) != false) goto L_0x014a;
         */
        @Override // android.widget.Editor.HandleView
        public void updatePosition(float x, float y, boolean fromTouchScreen) {
            boolean isExpanding;
            boolean isExpanding2;
            boolean positionCursor;
            boolean shrinking;
            int offsetThresholdToSnap;
            int wordBoundary;
            int wordBoundary2;
            int nextOffset;
            Layout layout = Editor.this.mTextView.getLayout();
            if (!isStartHandle()) {
                Editor.this.setPosIsLineEnd(false);
            }
            if (layout == null) {
                positionAndAdjustForCrossingHandles(Editor.this.mTextView.getOffsetForPosition(x, y), fromTouchScreen);
                return;
            }
            if (this.mPreviousLineTouched == -1) {
                this.mPreviousLineTouched = Editor.this.mTextView.getLineAtCoordinate(y);
            }
            int anotherHandleOffset = isStartHandle() ? Editor.this.mTextView.getSelectionEnd() : Editor.this.mTextView.getSelectionStart();
            int currLine = Editor.this.getCurrentLineAdjustedForSlop(layout, this.mPreviousLineTouched, y);
            int initialOffset = getOffsetAtCoordinate(layout, currLine, x);
            if ((isStartHandle() && initialOffset >= anotherHandleOffset) || (!isStartHandle() && initialOffset <= anotherHandleOffset)) {
                currLine = layout.getLineForOffset(anotherHandleOffset);
                if (isStartHandle() && layout.getPrimaryHorizontal(anotherHandleOffset) == 0.0f) {
                    currLine--;
                }
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
            if (!isLvlBoundary) {
                if ((!rtlAtCurrentOffset || atRtl) && (rtlAtCurrentOffset || !atRtl)) {
                    if (!this.mLanguageDirectionChanged || isLvlBoundary) {
                        float xDiff = x - this.mPrevX;
                        if (isStartHandle()) {
                            isExpanding = currLine < this.mPreviousLineTouched;
                        } else {
                            isExpanding = currLine > this.mPreviousLineTouched;
                        }
                        if (atRtl == isStartHandle()) {
                            isExpanding2 = isExpanding | (xDiff > 0.0f);
                        } else {
                            isExpanding2 = isExpanding | (xDiff < 0.0f);
                        }
                        float touchXOnScreen = ((this.mTouchToWindowOffsetX + x) - ((float) this.mLastParentX)) + ((float) this.mLastParentXOnScreen);
                        if (Editor.this.mTextView.getHorizontallyScrolling()) {
                            if (positionNearEdgeOfScrollingView(touchXOnScreen, atRtl)) {
                                if (!isStartHandle() || Editor.this.mTextView.getScrollX() == 0) {
                                    if (!isStartHandle()) {
                                    }
                                }
                                if (isExpanding2 && ((isStartHandle() && offset < currentOffset) || (!isStartHandle() && offset > currentOffset))) {
                                    this.mTouchWordDelta = 0.0f;
                                    if (atRtl == isStartHandle()) {
                                        nextOffset = layout.getOffsetToRightOf(this.mPreviousOffset);
                                    } else {
                                        nextOffset = layout.getOffsetToLeftOf(this.mPreviousOffset);
                                    }
                                    positionAndAdjustForCrossingHandles(nextOffset, fromTouchScreen);
                                    return;
                                }
                            }
                        }
                        if (isExpanding2) {
                            int wordBoundary3 = isStartHandle() ? wordStart : wordEnd;
                            if ((!this.mInWord || (!isStartHandle() ? currLine > this.mPrevLine : currLine < this.mPrevLine)) && atRtl == isAtRtlRun(layout, wordBoundary3)) {
                                if (layout.getLineForOffset(wordBoundary3) != currLine) {
                                    wordBoundary3 = isStartHandle() ? layout.getLineStart(currLine) : layout.getLineEnd(currLine);
                                }
                                if (isStartHandle()) {
                                    offsetThresholdToSnap = wordEnd - ((wordEnd - wordBoundary3) / 2);
                                } else {
                                    offsetThresholdToSnap = ((wordBoundary3 - wordStart) / 2) + wordStart;
                                }
                                if (isStartHandle()) {
                                    if (offset > offsetThresholdToSnap) {
                                        wordBoundary = wordBoundary3;
                                    } else {
                                        wordBoundary = wordBoundary3;
                                    }
                                    wordBoundary2 = wordStart;
                                    offset = wordBoundary2;
                                } else {
                                    wordBoundary = wordBoundary3;
                                }
                                if (isStartHandle() || (offset < offsetThresholdToSnap && currLine <= this.mPrevLine)) {
                                    wordBoundary2 = this.mPreviousOffset;
                                } else {
                                    Editor.this.setPosIsLineEnd(true);
                                    wordBoundary2 = wordEnd;
                                }
                                offset = wordBoundary2;
                            } else if (this.mInWord && !isStartHandle()) {
                                if (this.mPreviousOffset == wordEnd - 1) {
                                    if (x >= layout.getPrimaryHorizontal(offset) + ((float) (Editor.this.mTextView.getLeft() + Editor.this.mTextView.getTotalPaddingLeft()))) {
                                        offset = wordEnd;
                                    }
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
                            if (isStartHandle()) {
                                shrinking = adjustedOffset > this.mPreviousOffset || currLine > this.mPrevLine;
                            } else {
                                shrinking = adjustedOffset < this.mPreviousOffset || currLine < this.mPrevLine;
                            }
                            if (shrinking) {
                                if (currLine != this.mPrevLine) {
                                    int offset2 = isStartHandle() ? wordStart : wordEnd;
                                    if ((!isStartHandle() || offset2 >= initialOffset) && (isStartHandle() || offset2 <= initialOffset)) {
                                        this.mTouchWordDelta = 0.0f;
                                    } else {
                                        this.mTouchWordDelta = Editor.this.mTextView.convertToLocalHorizontalCoordinate(x) - getHorizontal(layout, offset2);
                                    }
                                    offset = offset2;
                                } else {
                                    offset = adjustedOffset;
                                }
                                positionCursor = true;
                            } else {
                                if ((isStartHandle() && adjustedOffset < this.mPreviousOffset) || (!isStartHandle() && adjustedOffset > this.mPreviousOffset)) {
                                    this.mTouchWordDelta = Editor.this.mTextView.convertToLocalHorizontalCoordinate(x) - getHorizontal(layout, this.mPreviousOffset);
                                }
                                positionCursor = false;
                            }
                        }
                        if (positionCursor) {
                            this.mPreviousLineTouched = currLine;
                            positionAndAdjustForCrossingHandles(offset, fromTouchScreen);
                        }
                        this.mPrevX = x;
                        return;
                    }
                    positionAndAdjustForCrossingHandles(offset, fromTouchScreen);
                    this.mTouchWordDelta = 0.0f;
                    this.mLanguageDirectionChanged = false;
                    return;
                }
            }
            this.mLanguageDirectionChanged = true;
            this.mTouchWordDelta = 0.0f;
            positionAndAdjustForCrossingHandles(offset, fromTouchScreen);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public void positionAtCursorOffset(int offset, boolean forceUpdatePosition, boolean fromTouchScreen) {
            int offset2 = Editor.this.resetOffsetForImageSpan(offset, isStartHandle());
            super.positionAtCursorOffset(offset2, forceUpdatePosition, fromTouchScreen);
            this.mInWord = offset2 != -1 && !Editor.this.getWordIteratorWithText().isBoundary(offset2);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:7:0x0011, code lost:
            if (r1 != 3) goto L_0x0027;
         */
        @Override // android.widget.Editor.HandleView, android.view.View
        public boolean onTouchEvent(MotionEvent event) {
            boolean superResult = super.onTouchEvent(event);
            int actionMasked = event.getActionMasked();
            if (actionMasked != 0) {
                if (actionMasked != 1) {
                    if (actionMasked == 2) {
                        updateMagnifier(event);
                    }
                }
                dismissMagnifier();
            } else {
                this.mTouchWordDelta = 0.0f;
                this.mPrevX = -1.0f;
                updateMagnifier(event);
            }
            return superResult;
        }

        private void positionAndAdjustForCrossingHandles(int offset, boolean fromTouchScreen) {
            int offset2;
            int anotherHandleOffset = isStartHandle() ? Editor.this.mTextView.getSelectionEnd() : Editor.this.mTextView.getSelectionStart();
            if ((isStartHandle() && offset >= anotherHandleOffset) || (!isStartHandle() && offset <= anotherHandleOffset)) {
                this.mTouchWordDelta = 0.0f;
                Layout layout = Editor.this.mTextView.getLayout();
                if (!(layout == null || offset == anotherHandleOffset)) {
                    float horiz = getHorizontal(layout, offset);
                    float anotherHandleHoriz = getHorizontal(layout, anotherHandleOffset, !isStartHandle());
                    float currentHoriz = getHorizontal(layout, this.mPreviousOffset);
                    if ((currentHoriz < anotherHandleHoriz && horiz < anotherHandleHoriz) || (currentHoriz > anotherHandleHoriz && horiz > anotherHandleHoriz)) {
                        int currentOffset = getCurrentCursorOffset();
                        long range = layout.getRunRange(isStartHandle() ? currentOffset : Math.max(currentOffset - 1, 0));
                        if (isStartHandle()) {
                            offset2 = TextUtils.unpackRangeStartFromLong(range);
                        } else {
                            offset2 = TextUtils.unpackRangeEndFromLong(range);
                        }
                        positionAtCursorOffset(offset2, false, fromTouchScreen);
                        return;
                    }
                }
                offset = Editor.this.getNextCursorOffset(anotherHandleOffset, !isStartHandle());
            }
            positionAtCursorOffset(offset, false, fromTouchScreen);
        }

        private boolean positionNearEdgeOfScrollingView(float x, boolean atRtl) {
            Editor.this.mTextView.getLocationOnScreen(this.mTextViewLocation);
            boolean nearEdge = true;
            if (atRtl == isStartHandle()) {
                if (x <= ((float) ((this.mTextViewLocation[0] + Editor.this.mTextView.getWidth()) - Editor.this.mTextView.getPaddingRight())) - this.mTextViewEdgeSlop) {
                    nearEdge = false;
                }
                return nearEdge;
            }
            if (x >= ((float) (this.mTextViewLocation[0] + Editor.this.mTextView.getPaddingLeft())) + this.mTextViewEdgeSlop) {
                nearEdge = false;
            }
            return nearEdge;
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public boolean isAtRtlRun(Layout layout, int offset) {
            return layout.isRtlCharAt(isStartHandle() ? offset : Math.max(offset - 1, 0));
        }

        @Override // android.widget.Editor.HandleView
        public float getHorizontal(Layout layout, int offset) {
            return getHorizontal(layout, offset, isStartHandle());
        }

        private float getHorizontal(Layout layout, int offset, boolean startHandle) {
            int line = layout.getLineForOffset(offset);
            boolean isRtlParagraph = false;
            boolean isRtlChar = layout.isRtlCharAt(startHandle ? offset : Math.max(offset - 1, 0));
            if (layout.getParagraphDirection(line) == -1) {
                isRtlParagraph = true;
            }
            return isRtlChar == isRtlParagraph ? layout.getPrimaryHorizontal(offset) : layout.getSecondaryHorizontal(offset);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public int getOffsetAtCoordinate(Layout layout, int line, float x) {
            float localX = Editor.this.mTextView.convertToLocalHorizontalCoordinate(x);
            boolean isRtlParagraph = true;
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
            boolean isRtlChar = layout.isRtlCharAt(isStartHandle() ? currentOffset : Math.max(currentOffset - 1, 0));
            if (layout.getParagraphDirection(line) != -1) {
                isRtlParagraph = false;
            }
            return isRtlChar == isRtlParagraph ? primaryOffset : secondaryOffset;
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public int getMagnifierHandleTrigger() {
            if (isStartHandle()) {
                return 1;
            }
            return 2;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getCurrentLineAdjustedForSlop(Layout layout, int prevLine, float y) {
        int trueLine = this.mTextView.getLineAtCoordinate(y);
        if (layout == null || prevLine > layout.getLineCount() || layout.getLineCount() <= 0 || prevLine < 0 || Math.abs(trueLine - prevLine) >= 2) {
            return trueLine;
        }
        float verticalOffset = (float) this.mTextView.viewportToContentVerticalOffset();
        int lineCount = layout.getLineCount();
        float slop = ((float) this.mTextView.getLineHeight()) * LINE_SLOP_MULTIPLIER_FOR_HANDLEVIEWS;
        float yTopBound = Math.max((((float) layout.getLineTop(prevLine)) + verticalOffset) - slop, ((float) layout.getLineTop(0)) + verticalOffset + slop);
        float yBottomBound = Math.min(((float) layout.getLineBottom(prevLine)) + verticalOffset + slop, (((float) layout.getLineBottom(lineCount - 1)) + verticalOffset) - slop);
        if (y <= yTopBound) {
            return Math.max(prevLine - 1, 0);
        }
        if (y >= yBottomBound) {
            return Math.min(prevLine + 1, lineCount - 1);
        }
        return prevLine;
    }

    /* access modifiers changed from: package-private */
    public void loadCursorDrawable() {
        if (this.mDrawableForCursor == null) {
            this.mDrawableForCursor = this.mTextView.getTextCursorDrawable();
        }
    }

    /* access modifiers changed from: private */
    public class InsertionPointCursorController implements CursorController {
        private InsertionHandleView mHandle;

        private InsertionPointCursorController() {
        }

        @Override // android.widget.Editor.CursorController
        public void show() {
            getHandle().show();
            if (Editor.this.mSelectionModifierCursorController != null) {
                Editor.this.mSelectionModifierCursorController.hide();
            }
        }

        @Override // android.widget.Editor.CursorController
        public void hide() {
            InsertionHandleView insertionHandleView = this.mHandle;
            if (insertionHandleView != null) {
                insertionHandleView.hide();
            }
        }

        @Override // android.view.ViewTreeObserver.OnTouchModeChangeListener
        public void onTouchModeChanged(boolean isInTouchMode) {
            if (!isInTouchMode) {
                hide();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private InsertionHandleView getHandle() {
            if (this.mHandle == null) {
                Editor.this.loadHandleDrawables(false);
                Editor editor = Editor.this;
                this.mHandle = new InsertionHandleView(editor.mSelectHandleCenter);
            }
            return this.mHandle;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void reloadHandleDrawable() {
            InsertionHandleView insertionHandleView = this.mHandle;
            if (insertionHandleView != null) {
                insertionHandleView.setDrawables(Editor.this.mSelectHandleCenter, Editor.this.mSelectHandleCenter);
            }
        }

        @Override // android.widget.Editor.CursorController
        public void onDetached() {
            Editor.this.mTextView.getViewTreeObserver().removeOnTouchModeChangeListener(this);
            InsertionHandleView insertionHandleView = this.mHandle;
            if (insertionHandleView != null) {
                insertionHandleView.onDetached();
            }
        }

        @Override // android.widget.Editor.CursorController
        public boolean isCursorBeingModified() {
            InsertionHandleView insertionHandleView = this.mHandle;
            return insertionHandleView != null && insertionHandleView.isDragging();
        }

        @Override // android.widget.Editor.CursorController
        public boolean isActive() {
            InsertionHandleView insertionHandleView = this.mHandle;
            return insertionHandleView != null && insertionHandleView.isShowing();
        }

        public void invalidateHandle() {
            InsertionHandleView insertionHandleView = this.mHandle;
            if (insertionHandleView != null) {
                insertionHandleView.invalidate();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class SelectionModifierCursorController implements CursorController {
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

        @Override // android.widget.Editor.CursorController
        public void show() {
            if (!Editor.this.mTextView.isInBatchEditMode()) {
                Editor.this.loadHandleDrawables(false);
                initHandles();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void initHandles() {
            if (this.mStartHandle == null) {
                Editor editor = Editor.this;
                this.mStartHandle = new SelectionHandleView(editor.mSelectHandleLeft, Editor.this.mSelectHandleRight, R.id.selection_start_handle, 0);
            }
            if (this.mEndHandle == null) {
                Editor editor2 = Editor.this;
                this.mEndHandle = new SelectionHandleView(editor2.mSelectHandleRight, Editor.this.mSelectHandleLeft, R.id.selection_end_handle, 1);
            }
            this.mStartHandle.show();
            this.mEndHandle.show();
            Editor.this.hideInsertionPointCursorController();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void reloadHandleDrawables() {
            SelectionHandleView selectionHandleView = this.mStartHandle;
            if (selectionHandleView != null) {
                selectionHandleView.setDrawables(Editor.this.mSelectHandleLeft, Editor.this.mSelectHandleRight);
                this.mEndHandle.setDrawables(Editor.this.mSelectHandleRight, Editor.this.mSelectHandleLeft);
            }
        }

        @Override // android.widget.Editor.CursorController
        public void hide() {
            SelectionHandleView selectionHandleView = this.mStartHandle;
            if (selectionHandleView != null) {
                selectionHandleView.hide();
            }
            SelectionHandleView selectionHandleView2 = this.mEndHandle;
            if (selectionHandleView2 != null) {
                selectionHandleView2.hide();
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
            boolean isMouse = event.isFromSource(8194);
            int actionMasked = event.getActionMasked();
            boolean stayedInArea = false;
            if (actionMasked != 0) {
                if (actionMasked != 1) {
                    if (actionMasked == 2) {
                        ViewConfiguration viewConfig = ViewConfiguration.get(Editor.this.mTextView.getContext());
                        int touchSlop = viewConfig.getScaledTouchSlop();
                        if (this.mGestureStayedInTapRegion || this.mHaventMovedEnoughToStartDrag) {
                            float deltaX = eventX - this.mDownPositionX;
                            float deltaY = eventY - this.mDownPositionY;
                            float distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);
                            if (this.mGestureStayedInTapRegion) {
                                int doubleTapTouchSlop = viewConfig.getScaledDoubleTapTouchSlop();
                                this.mGestureStayedInTapRegion = distanceSquared <= ((float) (doubleTapTouchSlop * doubleTapTouchSlop));
                            }
                            if (this.mHaventMovedEnoughToStartDrag) {
                                this.mHaventMovedEnoughToStartDrag = distanceSquared <= ((float) (touchSlop * touchSlop));
                            }
                        }
                        if (isMouse && !isDragAcceleratorActive()) {
                            int offset = Editor.this.mTextView.getOffsetForPosition(eventX, eventY);
                            if (Editor.this.mTextView.hasSelection() && ((!this.mHaventMovedEnoughToStartDrag || this.mStartOffset != offset) && offset >= Editor.this.mTextView.getSelectionStart() && offset <= Editor.this.mTextView.getSelectionEnd())) {
                                Editor.this.startDragAndDrop();
                                return;
                            } else if (this.mStartOffset != offset) {
                                Editor.this.lambda$startActionModeInternal$0$Editor();
                                enterDrag(1);
                                Editor.this.mDiscardNextActionUp = true;
                                this.mHaventMovedEnoughToStartDrag = false;
                            }
                        }
                        SelectionHandleView selectionHandleView = this.mStartHandle;
                        if (selectionHandleView == null || !selectionHandleView.isShowing()) {
                            updateSelection(event);
                        }
                    } else if ((actionMasked == 5 || actionMasked == 6) && Editor.this.mTextView.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT)) {
                        updateMinAndMaxOffsets(event);
                    }
                } else if (isDragAcceleratorActive()) {
                    updateSelection(event);
                    Editor.this.mTextView.getParent().requestDisallowInterceptTouchEvent(false);
                    resetDragAcceleratorState();
                    if (Editor.this.mTextView.hasSelection()) {
                        Editor.this.startSelectionActionModeAsync(this.mHaventMovedEnoughToStartDrag);
                    }
                }
            } else if (Editor.this.extractedTextModeWillBeStarted()) {
                hide();
            } else {
                int offsetForPosition = Editor.this.mTextView.getOffsetForPosition(eventX, eventY);
                this.mMaxTouchOffset = offsetForPosition;
                this.mMinTouchOffset = offsetForPosition;
                if (this.mGestureStayedInTapRegion && (Editor.this.mTapState == 2 || Editor.this.mTapState == 3)) {
                    float deltaX2 = eventX - this.mDownPositionX;
                    float deltaY2 = eventY - this.mDownPositionY;
                    float distanceSquared2 = (deltaX2 * deltaX2) + (deltaY2 * deltaY2);
                    int doubleTapSlop = ViewConfiguration.get(Editor.this.mTextView.getContext()).getScaledDoubleTapSlop();
                    if (distanceSquared2 < ((float) (doubleTapSlop * doubleTapSlop))) {
                        stayedInArea = true;
                    }
                    if (stayedInArea && (isMouse || Editor.this.isPositionOnText(eventX, eventY))) {
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
            }
        }

        private void updateSelection(MotionEvent event) {
            if (Editor.this.mTextView.getLayout() != null) {
                int i = this.mDragAcceleratorMode;
                if (i == 1) {
                    updateCharacterBasedSelection(event);
                } else if (i == 2) {
                    updateWordBasedSelection(event);
                } else if (i == 3) {
                    updateParagraphBasedSelection(event);
                }
            }
        }

        private boolean selectCurrentParagraphAndStartDrag() {
            if (Editor.this.mInsertionActionModeRunnable != null) {
                Editor.this.mTextView.removeCallbacks(Editor.this.mInsertionActionModeRunnable);
            }
            Editor.this.lambda$startActionModeInternal$0$Editor();
            if (!Editor.this.selectCurrentParagraph()) {
                return false;
            }
            enterDrag(3);
            return true;
        }

        private void updateCharacterBasedSelection(MotionEvent event) {
            updateSelectionInternal(this.mStartOffset, Editor.this.mTextView.getOffsetForPosition(event.getX(), event.getY()), event.isFromSource(4098));
        }

        private void updateWordBasedSelection(MotionEvent event) {
            int currLine;
            int startOffset;
            int offset;
            float fingerOffset;
            if (!this.mHaventMovedEnoughToStartDrag) {
                boolean isMouse = event.isFromSource(8194);
                ViewConfiguration viewConfig = ViewConfiguration.get(Editor.this.mTextView.getContext());
                float eventX = event.getX();
                float eventY = event.getY();
                if (isMouse) {
                    currLine = Editor.this.mTextView.getLineAtCoordinate(eventY);
                } else {
                    float y = eventY;
                    if (this.mSwitchedLines) {
                        int touchSlop = viewConfig.getScaledTouchSlop();
                        SelectionHandleView selectionHandleView = this.mStartHandle;
                        if (selectionHandleView != null) {
                            fingerOffset = selectionHandleView.getIdealVerticalOffset();
                        } else {
                            fingerOffset = (float) touchSlop;
                        }
                        y = eventY - fingerOffset;
                    }
                    Editor editor = Editor.this;
                    int currLine2 = editor.getCurrentLineAdjustedForSlop(editor.mTextView.getLayout(), this.mLineSelectionIsOn, y);
                    if (this.mSwitchedLines || currLine2 == this.mLineSelectionIsOn) {
                        currLine = currLine2;
                    } else {
                        this.mSwitchedLines = true;
                        return;
                    }
                }
                int offset2 = Editor.this.mTextView.getOffsetAtCoordinate(currLine, eventX);
                if (this.mStartOffset < offset2) {
                    offset = Editor.this.getWordEnd(offset2);
                    startOffset = Editor.this.getWordStart(this.mStartOffset);
                } else {
                    offset = Editor.this.getWordStart(offset2);
                    startOffset = Editor.this.getWordEnd(this.mStartOffset);
                    if (startOffset == offset) {
                        offset = Editor.this.getNextCursorOffset(offset, false);
                    }
                }
                this.mLineSelectionIsOn = currLine;
                updateSelectionInternal(startOffset, offset, event.isFromSource(4098));
            }
        }

        private void updateParagraphBasedSelection(MotionEvent event) {
            int offset = Editor.this.mTextView.getOffsetForPosition(event.getX(), event.getY());
            long paragraphsRange = Editor.this.getParagraphsRange(Math.min(offset, this.mStartOffset), Math.max(offset, this.mStartOffset));
            updateSelectionInternal(TextUtils.unpackRangeStartFromLong(paragraphsRange), TextUtils.unpackRangeEndFromLong(paragraphsRange), event.isFromSource(4098));
        }

        private void updateSelectionInternal(int selectionStart, int selectionEnd, boolean fromTouchScreen) {
            boolean performHapticFeedback = fromTouchScreen && Editor.this.mHapticTextHandleEnabled && !(Editor.this.mTextView.getSelectionStart() == selectionStart && Editor.this.mTextView.getSelectionEnd() == selectionEnd);
            Selection.setSelection((Spannable) Editor.this.mTextView.getText(), selectionStart, selectionEnd);
            if (performHapticFeedback) {
                Editor.this.mTextView.performHapticFeedback(9);
            }
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
            if (selectionStart < 0 || selectionEnd < 0) {
                Selection.removeSelection((Spannable) Editor.this.mTextView.getText());
            } else if (selectionStart > selectionEnd) {
                Selection.setSelection((Spannable) Editor.this.mTextView.getText(), selectionEnd, selectionStart);
            }
        }

        public boolean isSelectionStartDragged() {
            SelectionHandleView selectionHandleView = this.mStartHandle;
            return selectionHandleView != null && selectionHandleView.isDragging();
        }

        @Override // android.widget.Editor.CursorController
        public boolean isCursorBeingModified() {
            SelectionHandleView selectionHandleView;
            return isDragAcceleratorActive() || isSelectionStartDragged() || ((selectionHandleView = this.mEndHandle) != null && selectionHandleView.isDragging());
        }

        public boolean isDragAcceleratorActive() {
            return this.mDragAcceleratorMode != 0;
        }

        @Override // android.view.ViewTreeObserver.OnTouchModeChangeListener
        public void onTouchModeChanged(boolean isInTouchMode) {
            if (!isInTouchMode) {
                hide();
            }
        }

        @Override // android.widget.Editor.CursorController
        public void onDetached() {
            Editor.this.mTextView.getViewTreeObserver().removeOnTouchModeChangeListener(this);
            SelectionHandleView selectionHandleView = this.mStartHandle;
            if (selectionHandleView != null) {
                selectionHandleView.onDetached();
            }
            SelectionHandleView selectionHandleView2 = this.mEndHandle;
            if (selectionHandleView2 != null) {
                selectionHandleView2.onDetached();
            }
        }

        @Override // android.widget.Editor.CursorController
        public boolean isActive() {
            SelectionHandleView selectionHandleView;
            SelectionHandleView selectionHandleView2 = this.mStartHandle;
            return selectionHandleView2 != null && selectionHandleView2.isShowing() && (selectionHandleView = this.mEndHandle) != null && selectionHandleView.isShowing();
        }

        public void invalidateHandles() {
            SelectionHandleView selectionHandleView = this.mStartHandle;
            if (selectionHandleView != null) {
                selectionHandleView.invalidate();
            }
            SelectionHandleView selectionHandleView2 = this.mEndHandle;
            if (selectionHandleView2 != null) {
                selectionHandleView2.invalidate();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void loadHandleDrawables(boolean overwrite) {
        if (this.mSelectHandleCenter == null || overwrite) {
            this.mSelectHandleCenter = this.mTextView.getTextSelectHandle();
            if (hasInsertionController()) {
                getInsertionController().reloadHandleDrawable();
            }
        }
        if (this.mSelectHandleLeft == null || this.mSelectHandleRight == null || overwrite) {
            this.mSelectHandleLeft = this.mTextView.getTextSelectHandleLeft();
            this.mSelectHandleRight = this.mTextView.getTextSelectHandleRight();
            if (hasSelectionController()) {
                getSelectionController().reloadHandleDrawables();
            }
        }
    }

    /* access modifiers changed from: private */
    public class CorrectionHighlighter {
        private static final int FADE_OUT_DURATION = 400;
        private int mEnd;
        private long mFadingStartTime;
        private final Paint mPaint = new Paint(1);
        private final Path mPath = new Path();
        private int mStart;
        private RectF mTempRectF;

        public CorrectionHighlighter() {
            this.mPaint.setCompatibilityScaling(Editor.this.mTextView.getResources().getCompatibilityInfo().applicationScale);
            this.mPaint.setStyle(Paint.Style.FILL);
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
            if (!updatePath() || !updatePaint()) {
                stopAnimation();
                invalidate(false);
                return;
            }
            if (cursorOffsetVertical != 0) {
                canvas.translate(0.0f, (float) cursorOffsetVertical);
            }
            canvas.drawPath(this.mPath, this.mPaint);
            if (cursorOffsetVertical != 0) {
                canvas.translate(0.0f, (float) (-cursorOffsetVertical));
            }
            invalidate(true);
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

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
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

    /* access modifiers changed from: private */
    public static class ErrorPopup extends PopupWindow {
        private boolean mAbove = false;
        private int mPopupInlineErrorAboveBackgroundId = 0;
        private int mPopupInlineErrorBackgroundId = 0;
        private final TextView mView;

        ErrorPopup(TextView v, int width, int height) {
            super(v, width, height);
            this.mView = v;
            this.mPopupInlineErrorBackgroundId = getResourceId(this.mPopupInlineErrorBackgroundId, 298);
            this.mView.setBackgroundResource(this.mPopupInlineErrorBackgroundId);
        }

        /* access modifiers changed from: package-private */
        public void fixDirection(boolean above) {
            this.mAbove = above;
            if (above) {
                this.mPopupInlineErrorAboveBackgroundId = getResourceId(this.mPopupInlineErrorAboveBackgroundId, 297);
            } else {
                this.mPopupInlineErrorBackgroundId = getResourceId(this.mPopupInlineErrorBackgroundId, 298);
            }
            this.mView.setBackgroundResource(above ? this.mPopupInlineErrorAboveBackgroundId : this.mPopupInlineErrorBackgroundId);
        }

        private int getResourceId(int currentId, int index) {
            if (currentId != 0) {
                return currentId;
            }
            TypedArray styledAttributes = this.mView.getContext().obtainStyledAttributes(android.R.styleable.Theme);
            int currentId2 = styledAttributes.getResourceId(index, 0);
            styledAttributes.recycle();
            return currentId2;
        }

        @Override // android.widget.PopupWindow
        public void update(int x, int y, int w, int h, boolean force) {
            super.update(x, y, w, h, force);
            boolean above = isAboveAnchor();
            if (above != this.mAbove) {
                fixDirection(above);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class InputContentType {
        boolean enterDown;
        Bundle extras;
        int imeActionId;
        CharSequence imeActionLabel;
        LocaleList imeHintLocales;
        int imeOptions = 0;
        TextView.OnEditorActionListener onEditorActionListener;
        @UnsupportedAppUsage
        String privateImeOptions;

        InputContentType() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class InputMethodState {
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

    /* access modifiers changed from: private */
    public static boolean isValidRange(CharSequence text, int start, int end) {
        return start >= 0 && start <= end && end <= text.length();
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

        @Retention(RetentionPolicy.SOURCE)
        private @interface MergeMode {
        }

        public UndoInputFilter(Editor editor) {
            this.mEditor = editor;
        }

        public void saveInstanceState(Parcel parcel) {
            parcel.writeInt(this.mIsUserEdit ? 1 : 0);
            parcel.writeInt(this.mHasComposition ? 1 : 0);
            parcel.writeInt(this.mExpanding ? 1 : 0);
            parcel.writeInt(this.mPreviousOperationWasInSameBatchEdit ? 1 : 0);
        }

        public void restoreInstanceState(Parcel parcel) {
            boolean z = true;
            this.mIsUserEdit = parcel.readInt() != 0;
            this.mHasComposition = parcel.readInt() != 0;
            this.mExpanding = parcel.readInt() != 0;
            if (parcel.readInt() == 0) {
                z = false;
            }
            this.mPreviousOperationWasInSameBatchEdit = z;
        }

        public void beginBatchEdit() {
            this.mIsUserEdit = true;
        }

        public void endBatchEdit() {
            this.mIsUserEdit = false;
            this.mPreviousOperationWasInSameBatchEdit = false;
        }

        @Override // android.text.InputFilter
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            boolean shouldCreateSeparateState;
            if (!canUndoEdit(source, start, end, dest, dstart, dend)) {
                return null;
            }
            boolean hadComposition = this.mHasComposition;
            this.mHasComposition = isComposition(source);
            boolean wasExpanding = this.mExpanding;
            if (end - start != dend - dstart) {
                this.mExpanding = end - start > dend - dstart;
                if (hadComposition && this.mExpanding != wasExpanding) {
                    shouldCreateSeparateState = true;
                    handleEdit(source, start, end, dest, dstart, dend, shouldCreateSeparateState);
                    return null;
                }
            }
            shouldCreateSeparateState = false;
            handleEdit(source, start, end, dest, dstart, dend, shouldCreateSeparateState);
            return null;
        }

        /* access modifiers changed from: package-private */
        public void freezeLastEdit() {
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
            } else if (mergeMode != 2 || !lastEdit.mergeWith(edit)) {
                um.commitState(this.mEditor.mUndoOwner);
                um.addOperation(edit, 0);
            }
            this.mPreviousOperationWasInSameBatchEdit = this.mIsUserEdit;
            um.endUpdate();
        }

        private boolean canUndoEdit(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (!this.mEditor.mAllowUndo || this.mEditor.mUndoManager.isInUndo() || !Editor.isValidRange(source, start, end) || !Editor.isValidRange(dest, dstart, dend)) {
                return false;
            }
            if (start == end && dstart == dend) {
                return false;
            }
            return true;
        }

        private static boolean isComposition(CharSequence source) {
            if (!(source instanceof Spannable)) {
                return false;
            }
            Spannable text = (Spannable) source;
            if (EditableInputConnection.getComposingSpanStart(text) < EditableInputConnection.getComposingSpanEnd(text)) {
                return true;
            }
            return false;
        }

        private boolean isInTextWatcher() {
            CharSequence text = this.mEditor.mTextView.getText();
            return (text instanceof SpannableStringBuilder) && ((SpannableStringBuilder) text).getTextWatcherDepth() > 0;
        }
    }

    public static class EditOperation extends UndoOperation<Editor> {
        public static final Parcelable.ClassLoaderCreator<EditOperation> CREATOR = new Parcelable.ClassLoaderCreator<EditOperation>() {
            /* class android.widget.Editor.EditOperation.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public EditOperation createFromParcel(Parcel in) {
                return new EditOperation(in, null);
            }

            @Override // android.os.Parcelable.ClassLoaderCreator
            public EditOperation createFromParcel(Parcel in, ClassLoader loader) {
                return new EditOperation(in, loader);
            }

            @Override // android.os.Parcelable.Creator
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
            super(src, loader);
            this.mType = src.readInt();
            this.mOldText = src.readString();
            this.mNewText = src.readString();
            this.mStart = src.readInt();
            this.mOldCursorPos = src.readInt();
            this.mNewCursorPos = src.readInt();
            boolean z = false;
            this.mFrozen = src.readInt() == 1;
            this.mIsComposition = src.readInt() == 1 ? true : z;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mType);
            dest.writeString(this.mOldText);
            dest.writeString(this.mNewText);
            dest.writeInt(this.mStart);
            dest.writeInt(this.mOldCursorPos);
            dest.writeInt(this.mNewCursorPos);
            dest.writeInt(this.mFrozen ? 1 : 0);
            dest.writeInt(this.mIsComposition ? 1 : 0);
        }

        private int getNewTextEnd() {
            return this.mStart + this.mNewText.length();
        }

        private int getOldTextEnd() {
            return this.mStart + this.mOldText.length();
        }

        @Override // android.content.UndoOperation
        public void commit() {
        }

        @Override // android.content.UndoOperation
        public void undo() {
            modifyText((Editable) ((Editor) getOwnerData()).mTextView.getText(), this.mStart, getNewTextEnd(), this.mOldText, this.mStart, this.mOldCursorPos);
        }

        @Override // android.content.UndoOperation
        public void redo() {
            modifyText((Editable) ((Editor) getOwnerData()).mTextView.getText(), this.mStart, getOldTextEnd(), this.mNewText, this.mStart, this.mNewCursorPos);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean mergeWith(EditOperation edit) {
            if (this.mFrozen) {
                return false;
            }
            int i = this.mType;
            if (i == 0) {
                return mergeInsertWith(edit);
            }
            if (i == 1) {
                return mergeDeleteWith(edit);
            }
            if (i != 2) {
                return false;
            }
            return mergeReplaceWith(edit);
        }

        private boolean mergeInsertWith(EditOperation edit) {
            int i = edit.mType;
            if (i == 0) {
                if (getNewTextEnd() != edit.mStart) {
                    return false;
                }
                this.mNewText += edit.mNewText;
                this.mNewCursorPos = edit.mNewCursorPos;
                this.mFrozen = edit.mFrozen;
                this.mIsComposition = edit.mIsComposition;
                return true;
            } else if (!this.mIsComposition || i != 2 || this.mStart > edit.mStart || getNewTextEnd() < edit.getOldTextEnd()) {
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
            int i = this.mType;
            if (i == 0) {
                return "insert";
            }
            if (i == 1) {
                return "delete";
            }
            if (i != 2) {
                return "";
            }
            return "replace";
        }

        public String toString() {
            return "[mType=" + getTypeString() + ", mOldText=" + this.mOldText + ", mNewText=" + this.mNewText + ", mStart=" + this.mStart + ", mOldCursorPos=" + this.mOldCursorPos + ", mNewCursorPos=" + this.mNewCursorPos + ", mFrozen=" + this.mFrozen + ", mIsComposition=" + this.mIsComposition + "]";
        }
    }

    /* access modifiers changed from: package-private */
    public static final class ProcessTextIntentActionsHandler {
        private final SparseArray<AccessibilityNodeInfo.AccessibilityAction> mAccessibilityActions;
        private final SparseArray<Intent> mAccessibilityIntents;
        private final Context mContext;
        private final Editor mEditor;
        private final PackageManager mPackageManager;
        private final String mPackageName;
        private final List<ResolveInfo> mSupportedActivities;
        private final TextView mTextView;

        private ProcessTextIntentActionsHandler(Editor editor) {
            this.mAccessibilityIntents = new SparseArray<>();
            this.mAccessibilityActions = new SparseArray<>();
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
            for (int i = 0; i < size; i++) {
                ResolveInfo resolveInfo = this.mSupportedActivities.get(i);
                if (resolveInfo.activityInfo.packageName != null) {
                    if ("com.huawei.browser".equals(resolveInfo.activityInfo.packageName)) {
                        menu.add(0, 0, 100, getLabel(resolveInfo)).setIntent(createProcessTextIntentForResolveInfo(resolveInfo)).setShowAsAction(0);
                    } else {
                        menu.add(0, 0, i + 101, getLabel(resolveInfo)).setIntent(createProcessTextIntentForResolveInfo(resolveInfo)).setShowAsAction(0);
                    }
                }
            }
        }

        public boolean performMenuItemAction(MenuItem item) {
            return fireIntent(item.getIntent());
        }

        /* JADX INFO: Multiple debug info for r0v4 int: [D('i' int), D('actionId' int)] */
        public void initializeAccessibilityActions() {
            this.mAccessibilityIntents.clear();
            this.mAccessibilityActions.clear();
            int actionId = 0;
            loadSupportedActivities();
            for (ResolveInfo resolveInfo : this.mSupportedActivities) {
                int i = actionId + 1;
                int actionId2 = actionId + 268435712;
                this.mAccessibilityActions.put(actionId2, new AccessibilityNodeInfo.AccessibilityAction(actionId2, getLabel(resolveInfo)));
                this.mAccessibilityIntents.put(actionId2, createProcessTextIntentForResolveInfo(resolveInfo));
                actionId = i;
            }
        }

        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo nodeInfo) {
            for (int i = 0; i < this.mAccessibilityActions.size(); i++) {
                nodeInfo.addAction(this.mAccessibilityActions.valueAt(i));
            }
        }

        public boolean performAccessibilityAction(int actionId) {
            return fireIntent(this.mAccessibilityIntents.get(actionId));
        }

        private boolean fireIntent(Intent intent) {
            if (intent == null || !Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
                return false;
            }
            intent.putExtra(Intent.EXTRA_PROCESS_TEXT, (String) TextUtils.trimToParcelableSize(this.mTextView.getSelectedText()));
            this.mEditor.mPreserveSelection = true;
            this.mTextView.startActivityForResult(intent, 100);
            return true;
        }

        private void loadSupportedActivities() {
            this.mSupportedActivities.clear();
            if (this.mContext.canStartActivityForResult()) {
                for (ResolveInfo info : this.mTextView.getContext().getPackageManager().queryIntentActivities(createProcessTextIntent(), 0)) {
                    if (isSupportedActivity(info)) {
                        this.mSupportedActivities.add(info);
                    }
                }
            }
        }

        private boolean isSupportedActivity(ResolveInfo info) {
            return this.mPackageName.equals(info.activityInfo.packageName) || (info.activityInfo.exported && (info.activityInfo.permission == null || this.mContext.checkSelfPermission(info.activityInfo.permission) == 0));
        }

        private Intent createProcessTextIntentForResolveInfo(ResolveInfo info) {
            return createProcessTextIntent().putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, !this.mTextView.isTextEditable()).setClassName(info.activityInfo.packageName, info.activityInfo.name);
        }

        private Intent createProcessTextIntent() {
            return new Intent().setAction(Intent.ACTION_PROCESS_TEXT).setType(ClipDescription.MIMETYPE_TEXT_PLAIN);
        }

        private CharSequence getLabel(ResolveInfo resolveInfo) {
            return resolveInfo.loadLabel(this.mPackageManager);
        }
    }

    /* access modifiers changed from: protected */
    public void selectAllText() {
        TextView textView = this.mTextView;
        if (textView != null) {
            textView.selectAllText();
        }
    }

    /* access modifiers changed from: protected */
    public void selectAllAndShowEditor() {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: protected */
    public int adjustOffsetAtLineEndForTouchPos(int offset) {
        return offset;
    }

    /* access modifiers changed from: protected */
    public int adjustOffsetAtLineEndForInsertHanlePos(int offset) {
        return offset;
    }

    /* access modifiers changed from: protected */
    public boolean adjustHandlePos(int[] coordinate, HandleView handleView, Layout layout, int offset, int line) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void setPosWithMotionEvent(MotionEvent event, boolean isTouchPos) {
    }

    /* access modifiers changed from: protected */
    public boolean adjustCursorPos(int line, Layout layout) {
        return false;
    }

    /* access modifiers changed from: protected */
    public int getCursorTop() {
        return -1;
    }

    /* access modifiers changed from: protected */
    public int getCursorBottom() {
        return -1;
    }

    /* access modifiers changed from: protected */
    public float getCursorX() {
        return -1.0f;
    }

    /* access modifiers changed from: protected */
    public void setPosIsLineEnd(boolean flag) {
    }

    /* access modifiers changed from: protected */
    public void adjustSelectionBounds(RectF selectionBounds, int line, Layout layout, int handleHeight) {
    }

    /* access modifiers changed from: protected */
    public void recogniseLineEnd() {
    }
}
