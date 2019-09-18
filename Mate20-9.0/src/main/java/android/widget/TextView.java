package android.widget;

import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UndoManager;
import android.content.res.ColorStateList;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.BaseCanvas;
import android.graphics.Canvas;
import android.graphics.Insets;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hdm.HwDeviceManager;
import android.hwcontrol.HwWidgetFactory;
import android.icu.text.DecimalFormatSymbols;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ParcelableParcel;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.rms.HwSysResource;
import android.text.BoringLayout;
import android.text.DynamicLayout;
import android.text.Editable;
import android.text.GetChars;
import android.text.GraphicsOperations;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.ParcelableSpan;
import android.text.PrecomputedText;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.AllCapsTransformationMethod;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.DateKeyListener;
import android.text.method.DateTimeKeyListener;
import android.text.method.DialerKeyListener;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.text.method.LinkMovementMethod;
import android.text.method.MetaKeyKeyListener;
import android.text.method.MovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.text.method.TextKeyListener;
import android.text.method.TimeKeyListener;
import android.text.method.TransformationMethod;
import android.text.method.TransformationMethod2;
import android.text.method.WordIterator;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ParagraphStyle;
import android.text.style.SpellCheckSpan;
import android.text.style.SuggestionSpan;
import android.text.style.URLSpan;
import android.text.style.UpdateAppearance;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.IntArray;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.AccessibilityIterators;
import android.view.ActionMode;
import android.view.Choreographer;
import android.view.ContextMenu;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.view.ViewHierarchyEncoder;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.ViewStructure;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.view.autofill.Helper;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.textclassifier.TextClassification;
import android.view.textclassifier.TextClassificationContext;
import android.view.textclassifier.TextClassificationManager;
import android.view.textclassifier.TextClassifier;
import android.view.textclassifier.TextLinks;
import android.view.textservice.SpellCheckerSubtype;
import android.view.textservice.TextServicesManager;
import android.widget.AccessibilityIterators;
import android.widget.Editor;
import android.widget.RemoteViews;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.FastMath;
import com.android.internal.util.Preconditions;
import com.android.internal.widget.EditableInputConnection;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParserException;

@RemoteViews.RemoteView
public class TextView extends View implements ViewTreeObserver.OnPreDrawListener {
    static final int ACCESSIBILITY_ACTION_PROCESS_TEXT_START_ID = 268435712;
    private static final int ACCESSIBILITY_ACTION_SHARE = 268435456;
    private static final int ANIMATED_SCROLL_GAP = 250;
    public static final int AUTO_SIZE_TEXT_TYPE_NONE = 0;
    public static final int AUTO_SIZE_TEXT_TYPE_UNIFORM = 1;
    private static final String BAIDU_INPUT_PACKAGE = "com.baidu.input_huawei";
    private static final int CHANGE_WATCHER_PRIORITY = 100;
    private static final int COLOR_FIX_FROM = -16777216;
    private static final int COLOR_FIX_TO = -14277082;
    static final boolean DEBUG_EXTRACT = false;
    private static final int DECIMAL = 4;
    private static final int DEFAULT_AUTO_SIZE_GRANULARITY_IN_PX = 1;
    private static final int DEFAULT_AUTO_SIZE_MAX_TEXT_SIZE_IN_SP = 112;
    private static final int DEFAULT_AUTO_SIZE_MIN_TEXT_SIZE_IN_SP = 12;
    private static final int DEFAULT_TYPEFACE = -1;
    private static final int DEVICE_PROVISIONED_NO = 1;
    private static final int DEVICE_PROVISIONED_UNKNOWN = 0;
    private static final int DEVICE_PROVISIONED_YES = 2;
    private static final int DISPLAY_PANEL_TYPE_AMOLED = 1;
    private static final int ELLIPSIZE_END = 3;
    private static final int ELLIPSIZE_MARQUEE = 4;
    private static final int ELLIPSIZE_MIDDLE = 2;
    private static final int ELLIPSIZE_NONE = 0;
    private static final int ELLIPSIZE_NOT_SET = -1;
    private static final int ELLIPSIZE_START = 1;
    private static final Spanned EMPTY_SPANNED = new SpannedString("");
    private static final int EMS = 1;
    private static final String EVENT_COPY = "ClipBoardCopy";
    private static final String EVENT_CUT = "ClipBoardClip";
    private static final String EVENT_OPERATORE = "eventOperator";
    private static final String EVENT_URI_CONTENT = "content://com.huawei.recsys.provider";
    private static final int FLOATING_TOOLBAR_SELECT_ALL_REFRESH_DELAY = 500;
    static final int ID_ASSIST = 16908353;
    static final int ID_AUTOFILL = 16908355;
    static final int ID_COPY = 16908321;
    static final int ID_CUT = 16908320;
    static final int ID_PASTE = 16908322;
    static final int ID_PASTE_AS_PLAIN_TEXT = 16908337;
    static final int ID_REDO = 16908339;
    static final int ID_REPLACE = 16908340;
    static final int ID_SELECT_ALL = 16908319;
    static final int ID_SHARE = 16908341;
    static final int ID_UNDO = 16908338;
    private static boolean IS_AMOLED = false;
    private static final int KEY_DOWN_HANDLED_BY_KEY_LISTENER = 1;
    private static final int KEY_DOWN_HANDLED_BY_MOVEMENT_METHOD = 2;
    private static final int KEY_EVENT_HANDLED = -1;
    private static final int KEY_EVENT_NOT_HANDLED = 0;
    private static final int LINES = 1;
    static final String LOG_TAG = "TextView";
    private static final int MARQUEE_FADE_NORMAL = 0;
    private static final int MARQUEE_FADE_SWITCH_SHOW_ELLIPSIS = 1;
    private static final int MARQUEE_FADE_SWITCH_SHOW_FADE = 2;
    private static final int MONOSPACE = 3;
    private static final float MSPACINGMULTFORMY = 1.18f;
    private static final float MSPACINGMULTFORSI = 1.08f;
    private static final int[] MULTILINE_STATE_SET = {16843597};
    private static final InputFilter[] NO_FILTERS = new InputFilter[0];
    private static final String PASTE_DONE_ACTION = "com.huawei.action.EDIT_TEXT_PASTE_FINISHED";
    private static final String PASTE_DONE_PERMISSION = "huawei.permission.GET_PASTE_INFO";
    private static final int PIXELS = 2;
    static final int PROCESS_TEXT_REQUEST_CODE = 100;
    private static final int SANS = 1;
    private static final int SERIF = 2;
    private static final int SIGNED = 2;
    private static final float[] TEMP_POSITION = new float[2];
    private static final RectF TEMP_RECTF = new RectF();
    @VisibleForTesting
    public static final BoringLayout.Metrics UNKNOWN_BORING = new BoringLayout.Metrics();
    private static final float UNSET_AUTO_SIZE_UNIFORM_CONFIGURATION_VALUE = -1.0f;
    static final int VERY_WIDE = 1048576;
    private static boolean isGotDisplayPanelType = false;
    private static final boolean mIsVibrateImplemented = false;
    private static final SparseIntArray sAppearanceValues = new SparseIntArray();
    static long sLastCutCopyOrTextChangedTime;
    boolean isHwTheme;
    boolean isSystemApp;
    private boolean mAllowTransformationLengthChange;
    private int mAutoLinkMask;
    private float mAutoSizeMaxTextSizeInPx;
    private float mAutoSizeMinTextSizeInPx;
    private float mAutoSizeStepGranularityInPx;
    private int[] mAutoSizeTextSizesInPx;
    private int mAutoSizeTextType;
    private Intent mBoardCast;
    private BoringLayout.Metrics mBoring;
    private int mBreakStrategy;
    private BufferType mBufferType;
    private ChangeWatcher mChangeWatcher;
    private CharWrapper mCharWrapper;
    private int mCurHintTextColor;
    @ViewDebug.ExportedProperty(category = "text")
    private int mCurTextColor;
    private volatile Locale mCurrentSpellCheckerLocaleCache;
    int mCursorDrawableRes;
    private int mDeferScroll;
    private int mDesiredHeightAtMeasure;
    private int mDeviceProvisionedState;
    Drawables mDrawables;
    private Editable.Factory mEditableFactory;
    /* access modifiers changed from: private */
    public Editor mEditor;
    private TextUtils.TruncateAt mEllipsize;
    private InputFilter[] mFilters;
    private boolean mFreezesText;
    @ViewDebug.ExportedProperty(category = "text")
    private int mGravity;
    private boolean mHasPresetAutoSizeValues;
    int mHighlightColor;
    private final Paint mHighlightPaint;
    private Path mHighlightPath;
    private boolean mHighlightPathBogus;
    private CharSequence mHint;
    private BoringLayout.Metrics mHintBoring;
    private Layout mHintLayout;
    private ColorStateList mHintTextColor;
    private boolean mHorizontallyScrolling;
    private int mHwCompoundPaddingLeft;
    /* access modifiers changed from: private */
    public HwWidgetFactory.HwTextView mHwTextView;
    private int mHyphenationFrequency;
    private boolean mIncludePad;
    private int mJustificationMode;
    private int mLastLayoutDirection;
    private long mLastScroll;
    private CharSequence mLastValueSentToAutofillManager;
    /* access modifiers changed from: private */
    public Layout mLayout;
    private ColorStateList mLinkTextColor;
    private boolean mLinksClickable;
    private boolean mListenerChanged;
    private ArrayList<TextWatcher> mListeners;
    private boolean mLocalesChanged;
    private Marquee mMarquee;
    private int mMarqueeFadeMode;
    private int mMarqueeRepeatLimit;
    private int mMaxMode;
    private int mMaxWidth;
    private int mMaxWidthMode;
    private int mMaximum;
    private int mMinMode;
    private int mMinWidth;
    private int mMinWidthMode;
    private int mMinimum;
    private MovementMethod mMovement;
    private boolean mNeedsAutoSizeText;
    private int mOldMaxMode;
    private int mOldMaximum;
    private boolean mPreDrawListenerDetached;
    private boolean mPreDrawRegistered;
    private PrecomputedText mPrecomputed;
    private boolean mPreventDefaultMovement;
    private boolean mRestartMarquee;
    private BoringLayout mSavedHintLayout;
    private BoringLayout mSavedLayout;
    private Layout mSavedMarqueeModeLayout;
    private Scroller mScroller;
    protected boolean mSelectAllAndShowEditorDone;
    private int mShadowColor;
    private float mShadowDx;
    private float mShadowDy;
    private float mShadowRadius;
    private boolean mSingleLine;
    private float mSpacingAdd;
    private float mSpacingMult;
    private Spannable mSpannable;
    private Spannable.Factory mSpannableFactory;
    private Rect mTempRect;
    private TextPaint mTempTextPaint;
    @ViewDebug.ExportedProperty(category = "text")
    private CharSequence mText;
    private TextClassifier mTextClassificationSession;
    private TextClassifier mTextClassifier;
    private ColorStateList mTextColor;
    private TextDirectionHeuristic mTextDir;
    int mTextEditSuggestionContainerLayout;
    int mTextEditSuggestionHighlightStyle;
    int mTextEditSuggestionItemLayout;
    private int mTextId;
    private final TextPaint mTextPaint;
    int mTextSelectHandleLeftRes;
    int mTextSelectHandleRes;
    int mTextSelectHandleRightRes;
    private boolean mTextSetFromXmlOrResourceId;
    private int mTextViewDirection;
    private TransformationMethod mTransformation;
    /* access modifiers changed from: private */
    public CharSequence mTransformed;
    protected boolean mTrySelectAllAndShowEditor;
    boolean mUseFallbackLineSpacing;
    private final boolean mUseInternationalizedInput;
    private boolean mUserSetTextScaleX;
    /* access modifiers changed from: private */
    public boolean mValidSetCursorEvent;
    private TextCopyFinishedListener textCopyFinishedListener;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AutoSizeTextType {
    }

    public enum BufferType {
        NORMAL,
        SPANNABLE,
        EDITABLE
    }

    private class ChangeWatcher implements TextWatcher, SpanWatcher {
        private CharSequence mBeforeText;

        private ChangeWatcher() {
        }

        public void beforeTextChanged(CharSequence buffer, int start, int before, int after) {
            if (AccessibilityManager.getInstance(TextView.this.mContext).isEnabled() && TextView.this.mTransformed != null) {
                this.mBeforeText = TextView.this.mTransformed.toString();
            }
            TextView.this.sendBeforeTextChanged(buffer, start, before, after);
        }

        public void onTextChanged(CharSequence buffer, int start, int before, int after) {
            TextView.this.handleTextChanged(buffer, start, before, after);
            if (!AccessibilityManager.getInstance(TextView.this.mContext).isEnabled()) {
                return;
            }
            if (TextView.this.isFocused() || (TextView.this.isSelected() && TextView.this.isShown())) {
                TextView.this.sendAccessibilityEventTypeViewTextChanged(this.mBeforeText, start, before, after);
                this.mBeforeText = null;
            }
        }

        public void afterTextChanged(Editable buffer) {
            TextView.this.sendAfterTextChanged(buffer);
            if (MetaKeyKeyListener.getMetaState((CharSequence) buffer, 2048) != 0) {
                MetaKeyKeyListener.stopSelecting(TextView.this, buffer);
            }
        }

        public void onSpanChanged(Spannable buf, Object what, int s, int e, int st, int en) {
            if (!(TextView.this.mHwTextView == null || TextView.this.mEditor == null || !TextView.this.mValidSetCursorEvent)) {
                TextView.this.mHwTextView.playIvtEffect(TextView.this.mContext, "TEXTVIEW_SETCURSOR", what, s, en);
            }
            TextView.this.spanChange(buf, what, s, st, e, en);
        }

        public void onSpanAdded(Spannable buf, Object what, int s, int e) {
            TextView.this.spanChange(buf, what, -1, s, -1, e);
        }

        public void onSpanRemoved(Spannable buf, Object what, int s, int e) {
            TextView.this.spanChange(buf, what, s, -1, e, -1);
        }
    }

    private static class CharWrapper implements CharSequence, GetChars, GraphicsOperations {
        /* access modifiers changed from: private */
        public char[] mChars;
        private int mLength;
        private int mStart;

        public CharWrapper(char[] chars, int start, int len) {
            this.mChars = chars;
            this.mStart = start;
            this.mLength = len;
        }

        /* access modifiers changed from: package-private */
        public void set(char[] chars, int start, int len) {
            this.mChars = chars;
            this.mStart = start;
            this.mLength = len;
        }

        public int length() {
            return this.mLength;
        }

        public char charAt(int off) {
            return this.mChars[this.mStart + off];
        }

        public String toString() {
            return new String(this.mChars, this.mStart, this.mLength);
        }

        public CharSequence subSequence(int start, int end) {
            if (start >= 0 && end >= 0 && start <= this.mLength && end <= this.mLength) {
                return new String(this.mChars, this.mStart + start, end - start);
            }
            throw new IndexOutOfBoundsException(start + ", " + end);
        }

        public void getChars(int start, int end, char[] buf, int off) {
            if (start < 0 || end < 0 || start > this.mLength || end > this.mLength) {
                throw new IndexOutOfBoundsException(start + ", " + end);
            }
            System.arraycopy(this.mChars, this.mStart + start, buf, off, end - start);
        }

        public void drawText(BaseCanvas c, int start, int end, float x, float y, Paint p) {
            c.drawText(this.mChars, start + this.mStart, end - start, x, y, p);
        }

        public void drawTextRun(BaseCanvas c, int start, int end, int contextStart, int contextEnd, float x, float y, boolean isRtl, Paint p) {
            c.drawTextRun(this.mChars, start + this.mStart, end - start, contextStart + this.mStart, contextEnd - contextStart, x, y, isRtl, p);
        }

        public float measureText(int start, int end, Paint p) {
            return p.measureText(this.mChars, this.mStart + start, end - start);
        }

        public int getTextWidths(int start, int end, float[] widths, Paint p) {
            return p.getTextWidths(this.mChars, this.mStart + start, end - start, widths);
        }

        public float getTextRunAdvances(int start, int end, int contextStart, int contextEnd, boolean isRtl, float[] advances, int advancesIndex, Paint p) {
            return p.getTextRunAdvances(this.mChars, start + this.mStart, end - start, contextStart + this.mStart, contextEnd - contextStart, isRtl, advances, advancesIndex);
        }

        public int getTextRunCursor(int contextStart, int contextEnd, int dir, int offset, int cursorOpt, Paint p) {
            return p.getTextRunCursor(this.mChars, contextStart + this.mStart, contextEnd - contextStart, dir, offset + this.mStart, cursorOpt);
        }
    }

    static class Drawables {
        static final int BOTTOM = 3;
        static final int DRAWABLE_LEFT = 1;
        static final int DRAWABLE_NONE = -1;
        static final int DRAWABLE_RIGHT = 0;
        static final int LEFT = 0;
        static final int RIGHT = 2;
        static final int TOP = 1;
        final Rect mCompoundRect = new Rect();
        Drawable mDrawableEnd;
        Drawable mDrawableError;
        int mDrawableHeightEnd;
        int mDrawableHeightError;
        int mDrawableHeightLeft;
        int mDrawableHeightRight;
        int mDrawableHeightStart;
        int mDrawableHeightTemp;
        Drawable mDrawableLeftInitial;
        int mDrawablePadding;
        Drawable mDrawableRightInitial;
        int mDrawableSaved = -1;
        int mDrawableSizeBottom;
        int mDrawableSizeEnd;
        int mDrawableSizeError;
        int mDrawableSizeLeft;
        int mDrawableSizeRight;
        int mDrawableSizeStart;
        int mDrawableSizeTemp;
        int mDrawableSizeTop;
        Drawable mDrawableStart;
        Drawable mDrawableTemp;
        int mDrawableWidthBottom;
        int mDrawableWidthTop;
        boolean mHasTint;
        boolean mHasTintMode;
        boolean mIsRtlCompatibilityMode;
        boolean mOverride;
        final Drawable[] mShowing = new Drawable[4];
        ColorStateList mTintList;
        PorterDuff.Mode mTintMode;

        public Drawables(Context context) {
            this.mIsRtlCompatibilityMode = context.getApplicationInfo().targetSdkVersion < 17 || !context.getApplicationInfo().hasRtlSupport();
            this.mOverride = false;
        }

        public boolean hasMetadata() {
            return this.mDrawablePadding != 0 || this.mHasTintMode || this.mHasTint;
        }

        public boolean resolveWithLayoutDirection(int layoutDirection) {
            Drawable previousLeft = this.mShowing[0];
            Drawable previousRight = this.mShowing[2];
            this.mShowing[0] = this.mDrawableLeftInitial;
            this.mShowing[2] = this.mDrawableRightInitial;
            if (this.mIsRtlCompatibilityMode) {
                if (this.mDrawableStart != null && this.mShowing[0] == null) {
                    this.mShowing[0] = this.mDrawableStart;
                    this.mDrawableSizeLeft = this.mDrawableSizeStart;
                    this.mDrawableHeightLeft = this.mDrawableHeightStart;
                }
                if (this.mDrawableEnd != null && this.mShowing[2] == null) {
                    this.mShowing[2] = this.mDrawableEnd;
                    this.mDrawableSizeRight = this.mDrawableSizeEnd;
                    this.mDrawableHeightRight = this.mDrawableHeightEnd;
                }
            } else if (layoutDirection != 1) {
                if (this.mOverride) {
                    this.mShowing[0] = this.mDrawableStart;
                    this.mDrawableSizeLeft = this.mDrawableSizeStart;
                    this.mDrawableHeightLeft = this.mDrawableHeightStart;
                    this.mShowing[2] = this.mDrawableEnd;
                    this.mDrawableSizeRight = this.mDrawableSizeEnd;
                    this.mDrawableHeightRight = this.mDrawableHeightEnd;
                }
            } else if (this.mOverride) {
                this.mShowing[2] = this.mDrawableStart;
                this.mDrawableSizeRight = this.mDrawableSizeStart;
                this.mDrawableHeightRight = this.mDrawableHeightStart;
                this.mShowing[0] = this.mDrawableEnd;
                this.mDrawableSizeLeft = this.mDrawableSizeEnd;
                this.mDrawableHeightLeft = this.mDrawableHeightEnd;
            }
            applyErrorDrawableIfNeeded(layoutDirection);
            if (this.mShowing[0] == previousLeft && this.mShowing[2] == previousRight) {
                return false;
            }
            return true;
        }

        public void setErrorDrawable(Drawable dr, TextView tv) {
            if (!(this.mDrawableError == dr || this.mDrawableError == null)) {
                this.mDrawableError.setCallback(null);
            }
            this.mDrawableError = dr;
            if (this.mDrawableError != null) {
                Rect compoundRect = this.mCompoundRect;
                this.mDrawableError.setState(tv.getDrawableState());
                this.mDrawableError.copyBounds(compoundRect);
                this.mDrawableError.setCallback(tv);
                this.mDrawableSizeError = compoundRect.width();
                this.mDrawableHeightError = compoundRect.height();
                return;
            }
            this.mDrawableHeightError = 0;
            this.mDrawableSizeError = 0;
        }

        private void applyErrorDrawableIfNeeded(int layoutDirection) {
            switch (this.mDrawableSaved) {
                case 0:
                    this.mShowing[2] = this.mDrawableTemp;
                    this.mDrawableSizeRight = this.mDrawableSizeTemp;
                    this.mDrawableHeightRight = this.mDrawableHeightTemp;
                    break;
                case 1:
                    this.mShowing[0] = this.mDrawableTemp;
                    this.mDrawableSizeLeft = this.mDrawableSizeTemp;
                    this.mDrawableHeightLeft = this.mDrawableHeightTemp;
                    break;
            }
            if (this.mDrawableError == null) {
                return;
            }
            if (layoutDirection != 1) {
                this.mDrawableSaved = 0;
                this.mDrawableTemp = this.mShowing[2];
                this.mDrawableSizeTemp = this.mDrawableSizeRight;
                this.mDrawableHeightTemp = this.mDrawableHeightRight;
                this.mShowing[2] = this.mDrawableError;
                this.mDrawableSizeRight = this.mDrawableSizeError;
                this.mDrawableHeightRight = this.mDrawableHeightError;
                return;
            }
            this.mDrawableSaved = 1;
            this.mDrawableTemp = this.mShowing[0];
            this.mDrawableSizeTemp = this.mDrawableSizeLeft;
            this.mDrawableHeightTemp = this.mDrawableHeightLeft;
            this.mShowing[0] = this.mDrawableError;
            this.mDrawableSizeLeft = this.mDrawableSizeError;
            this.mDrawableHeightLeft = this.mDrawableHeightError;
        }
    }

    private static final class Marquee {
        private static final int MARQUEE_DELAY = 1200;
        private static final float MARQUEE_DELTA_MAX = 0.07f;
        private static final int MARQUEE_DP_PER_SECOND = 30;
        private static final byte MARQUEE_RUNNING = 2;
        private static final byte MARQUEE_STARTING = 1;
        private static final byte MARQUEE_STOPPED = 0;
        /* access modifiers changed from: private */
        public final Choreographer mChoreographer;
        private float mFadeStop;
        private float mGhostOffset;
        private float mGhostStart;
        /* access modifiers changed from: private */
        public long mLastAnimationMs;
        private float mMaxFadeScroll;
        private float mMaxScroll;
        private final float mPixelsPerMs;
        /* access modifiers changed from: private */
        public int mRepeatLimit;
        private Choreographer.FrameCallback mRestartCallback = new Choreographer.FrameCallback() {
            public void doFrame(long frameTimeNanos) {
                if (Marquee.this.mStatus == 2) {
                    if (Marquee.this.mRepeatLimit >= 0) {
                        Marquee.access$910(Marquee.this);
                    }
                    Marquee.this.start(Marquee.this.mRepeatLimit);
                }
            }
        };
        private float mScroll;
        private Choreographer.FrameCallback mStartCallback = new Choreographer.FrameCallback() {
            public void doFrame(long frameTimeNanos) {
                byte unused = Marquee.this.mStatus = (byte) 2;
                long unused2 = Marquee.this.mLastAnimationMs = Marquee.this.mChoreographer.getFrameTime();
                Marquee.this.tick();
            }
        };
        /* access modifiers changed from: private */
        public byte mStatus = 0;
        private Choreographer.FrameCallback mTickCallback = new Choreographer.FrameCallback() {
            public void doFrame(long frameTimeNanos) {
                Marquee.this.tick();
            }
        };
        private final WeakReference<TextView> mView;

        static /* synthetic */ int access$910(Marquee x0) {
            int i = x0.mRepeatLimit;
            x0.mRepeatLimit = i - 1;
            return i;
        }

        Marquee(TextView v) {
            this.mPixelsPerMs = (30.0f * v.getContext().getResources().getDisplayMetrics().density) / 1000.0f;
            this.mView = new WeakReference<>(v);
            this.mChoreographer = Choreographer.getInstance();
        }

        /* access modifiers changed from: package-private */
        public void tick() {
            if (this.mStatus == 2) {
                this.mChoreographer.removeFrameCallback(this.mTickCallback);
                TextView textView = (TextView) this.mView.get();
                if (textView != null && (textView.isFocused() || textView.isSelected())) {
                    long currentMs = this.mChoreographer.getFrameTime();
                    this.mLastAnimationMs = currentMs;
                    this.mScroll += ((float) (currentMs - this.mLastAnimationMs)) * this.mPixelsPerMs;
                    if (this.mScroll > this.mMaxScroll) {
                        this.mScroll = this.mMaxScroll;
                        this.mChoreographer.postFrameCallbackDelayed(this.mRestartCallback, 1200);
                    } else {
                        this.mChoreographer.postFrameCallback(this.mTickCallback);
                    }
                    textView.invalidate();
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void stop() {
            this.mStatus = 0;
            this.mChoreographer.removeFrameCallback(this.mStartCallback);
            this.mChoreographer.removeFrameCallback(this.mRestartCallback);
            this.mChoreographer.removeFrameCallback(this.mTickCallback);
            resetScroll();
        }

        private void resetScroll() {
            this.mScroll = 0.0f;
            TextView textView = (TextView) this.mView.get();
            if (textView != null) {
                textView.invalidate();
            }
        }

        /* access modifiers changed from: package-private */
        public void start(int repeatLimit) {
            if (repeatLimit == 0) {
                stop();
                return;
            }
            this.mRepeatLimit = repeatLimit;
            TextView textView = (TextView) this.mView.get();
            if (!(textView == null || textView.mLayout == null)) {
                this.mStatus = 1;
                this.mScroll = 0.0f;
                int textWidth = (textView.getWidth() - textView.getCompoundPaddingLeft()) - textView.getCompoundPaddingRight();
                float lineWidth = textView.mLayout.getLineWidth(0);
                float gap = ((float) textWidth) / 3.0f;
                this.mGhostStart = (lineWidth - ((float) textWidth)) + gap;
                this.mMaxScroll = this.mGhostStart + ((float) textWidth);
                this.mGhostOffset = lineWidth + gap;
                this.mFadeStop = (((float) textWidth) / 6.0f) + lineWidth;
                this.mMaxFadeScroll = this.mGhostStart + lineWidth + lineWidth;
                textView.invalidate();
                this.mChoreographer.postFrameCallback(this.mStartCallback);
            }
        }

        /* access modifiers changed from: package-private */
        public float getGhostOffset() {
            return this.mGhostOffset;
        }

        /* access modifiers changed from: package-private */
        public float getScroll() {
            return this.mScroll;
        }

        /* access modifiers changed from: package-private */
        public float getMaxFadeScroll() {
            return this.mMaxFadeScroll;
        }

        /* access modifiers changed from: package-private */
        public boolean shouldDrawLeftFade() {
            return this.mScroll <= this.mFadeStop;
        }

        /* access modifiers changed from: package-private */
        public boolean shouldDrawGhost() {
            return this.mStatus == 2 && this.mScroll > this.mGhostStart;
        }

        /* access modifiers changed from: package-private */
        public boolean isRunning() {
            return this.mStatus == 2;
        }

        /* access modifiers changed from: package-private */
        public boolean isStopped() {
            return this.mStatus == 0;
        }
    }

    public interface OnEditorActionListener {
        boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent);
    }

    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        ParcelableParcel editorState;
        CharSequence error;
        boolean frozenWithFocus;
        int selEnd;
        int selStart;
        CharSequence text;

        SavedState(Parcelable superState) {
            super(superState);
            this.selStart = -1;
            this.selEnd = -1;
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.selStart);
            out.writeInt(this.selEnd);
            out.writeInt(this.frozenWithFocus ? 1 : 0);
            TextUtils.writeToParcel(this.text, out, flags);
            if (this.error == null) {
                out.writeInt(0);
            } else {
                out.writeInt(1);
                TextUtils.writeToParcel(this.error, out, flags);
            }
            if (this.editorState == null) {
                out.writeInt(0);
                return;
            }
            out.writeInt(1);
            this.editorState.writeToParcel(out, flags);
        }

        public String toString() {
            String str = "TextView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " start=" + this.selStart + " end=" + this.selEnd;
            if (this.text != null) {
                str = str + " text=" + this.text;
            }
            return str + "}";
        }

        private SavedState(Parcel in) {
            super(in);
            this.selStart = -1;
            this.selEnd = -1;
            this.selStart = in.readInt();
            this.selEnd = in.readInt();
            this.frozenWithFocus = in.readInt() != 0;
            this.text = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            if (in.readInt() != 0) {
                this.error = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            }
            if (in.readInt() != 0) {
                this.editorState = ParcelableParcel.CREATOR.createFromParcel(in);
            }
        }
    }

    private static class TextAppearanceAttributes {
        boolean mAllCaps;
        boolean mElegant;
        boolean mFallbackLineSpacing;
        String mFontFamily;
        boolean mFontFamilyExplicit;
        String mFontFeatureSettings;
        Typeface mFontTypeface;
        int mFontWeight;
        boolean mHasElegant;
        boolean mHasFallbackLineSpacing;
        boolean mHasLetterSpacing;
        float mLetterSpacing;
        int mShadowColor;
        float mShadowDx;
        float mShadowDy;
        float mShadowRadius;
        int mStyleIndex;
        ColorStateList mTextColor;
        int mTextColorHighlight;
        ColorStateList mTextColorHint;
        ColorStateList mTextColorLink;
        int mTextSize;
        int mTypefaceIndex;

        private TextAppearanceAttributes() {
            this.mTextColorHighlight = 0;
            this.mTextColor = null;
            this.mTextColorHint = null;
            this.mTextColorLink = null;
            this.mTextSize = 0;
            this.mFontFamily = null;
            this.mFontTypeface = null;
            this.mFontFamilyExplicit = false;
            this.mTypefaceIndex = -1;
            this.mStyleIndex = -1;
            this.mFontWeight = -1;
            this.mAllCaps = false;
            this.mShadowColor = 0;
            this.mShadowDx = 0.0f;
            this.mShadowDy = 0.0f;
            this.mShadowRadius = 0.0f;
            this.mHasElegant = false;
            this.mElegant = false;
            this.mHasFallbackLineSpacing = false;
            this.mFallbackLineSpacing = false;
            this.mHasLetterSpacing = false;
            this.mLetterSpacing = 0.0f;
            this.mFontFeatureSettings = null;
        }

        public String toString() {
            return "TextAppearanceAttributes {\n    mTextColorHighlight:" + this.mTextColorHighlight + "\n    mTextColor:" + this.mTextColor + "\n    mTextColorHint:" + this.mTextColorHint + "\n    mTextColorLink:" + this.mTextColorLink + "\n    mTextSize:" + this.mTextSize + "\n    mFontFamily:" + this.mFontFamily + "\n    mFontTypeface:" + this.mFontTypeface + "\n    mFontFamilyExplicit:" + this.mFontFamilyExplicit + "\n    mTypefaceIndex:" + this.mTypefaceIndex + "\n    mStyleIndex:" + this.mStyleIndex + "\n    mFontWeight:" + this.mFontWeight + "\n    mAllCaps:" + this.mAllCaps + "\n    mShadowColor:" + this.mShadowColor + "\n    mShadowDx:" + this.mShadowDx + "\n    mShadowDy:" + this.mShadowDy + "\n    mShadowRadius:" + this.mShadowRadius + "\n    mHasElegant:" + this.mHasElegant + "\n    mElegant:" + this.mElegant + "\n    mHasFallbackLineSpacing:" + this.mHasFallbackLineSpacing + "\n    mFallbackLineSpacing:" + this.mFallbackLineSpacing + "\n    mHasLetterSpacing:" + this.mHasLetterSpacing + "\n    mLetterSpacing:" + this.mLetterSpacing + "\n    mFontFeatureSettings:" + this.mFontFeatureSettings + "\n}";
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface XMLTypefaceAttr {
    }

    static {
        sAppearanceValues.put(6, 4);
        sAppearanceValues.put(5, 3);
        sAppearanceValues.put(7, 5);
        sAppearanceValues.put(8, 6);
        sAppearanceValues.put(2, 0);
        sAppearanceValues.put(3, 1);
        sAppearanceValues.put(75, 12);
        sAppearanceValues.put(4, 2);
        sAppearanceValues.put(94, 17);
        sAppearanceValues.put(72, 11);
        sAppearanceValues.put(36, 7);
        sAppearanceValues.put(37, 8);
        sAppearanceValues.put(38, 9);
        sAppearanceValues.put(39, 10);
        sAppearanceValues.put(76, 13);
        sAppearanceValues.put(90, 16);
        sAppearanceValues.put(77, 14);
        sAppearanceValues.put(78, 15);
    }

    public static void preloadFontCache() {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setTypeface(Typeface.DEFAULT);
        p.measureText(HwSysResource.BIGDATA_SU_SYS);
    }

    public TextView(Context context) {
        this(context, null);
    }

    public TextView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842884);
    }

    public TextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x057e, code lost:
        r9 = r75;
        r15 = r78;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x0626, code lost:
        r80 = r2;
        r81 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x0639, code lost:
        r9 = r75;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:136:0x06d7, code lost:
        r81 = r3;
        r2 = r70;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:308:0x0a08, code lost:
        if ((r1.mEditor.mInputType & android.os.FileObserver.ALL_EVENTS) == 129) goto L_0x0a11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x021c, code lost:
        r9 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x021d, code lost:
        r2 = r70;
        r15 = r75;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x02af, code lost:
        r80 = r2;
        r81 = r3;
     */
    /* JADX WARNING: Removed duplicated region for block: B:260:0x0914  */
    /* JADX WARNING: Removed duplicated region for block: B:261:0x0924  */
    /* JADX WARNING: Removed duplicated region for block: B:263:0x0930  */
    /* JADX WARNING: Removed duplicated region for block: B:266:0x0941  */
    /* JADX WARNING: Removed duplicated region for block: B:269:0x0947 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x0955  */
    /* JADX WARNING: Removed duplicated region for block: B:274:0x0961  */
    /* JADX WARNING: Removed duplicated region for block: B:276:0x0967  */
    /* JADX WARNING: Removed duplicated region for block: B:277:0x0973  */
    /* JADX WARNING: Removed duplicated region for block: B:279:0x0978  */
    /* JADX WARNING: Removed duplicated region for block: B:282:0x09a5  */
    /* JADX WARNING: Removed duplicated region for block: B:290:0x09bb  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x09da  */
    /* JADX WARNING: Removed duplicated region for block: B:296:0x09e0  */
    /* JADX WARNING: Removed duplicated region for block: B:297:0x09e6  */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x09ee A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:305:0x09fa  */
    /* JADX WARNING: Removed duplicated region for block: B:311:0x0a0f  */
    /* JADX WARNING: Removed duplicated region for block: B:314:0x0a14  */
    /* JADX WARNING: Removed duplicated region for block: B:315:0x0a1c  */
    /* JADX WARNING: Removed duplicated region for block: B:318:0x0a25  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x0a2e  */
    /* JADX WARNING: Removed duplicated region for block: B:321:0x0a44  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0a56  */
    /* JADX WARNING: Removed duplicated region for block: B:325:0x0a5c  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0a60  */
    /* JADX WARNING: Removed duplicated region for block: B:328:0x0a66  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0a6c  */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x0a78  */
    /* JADX WARNING: Removed duplicated region for block: B:342:0x0a88  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0abf  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0ad5  */
    /* JADX WARNING: Removed duplicated region for block: B:359:0x0af6  */
    /* JADX WARNING: Removed duplicated region for block: B:362:0x0b03  */
    /* JADX WARNING: Removed duplicated region for block: B:365:0x0b0e  */
    /* JADX WARNING: Removed duplicated region for block: B:366:0x0b13  */
    /* JADX WARNING: Removed duplicated region for block: B:369:0x0b1a  */
    /* JADX WARNING: Removed duplicated region for block: B:388:0x0b77  */
    /* JADX WARNING: Removed duplicated region for block: B:391:0x0b84  */
    /* JADX WARNING: Removed duplicated region for block: B:394:0x0b8b  */
    /* JADX WARNING: Removed duplicated region for block: B:397:0x0b92  */
    /* JADX WARNING: Removed duplicated region for block: B:400:0x0b9f  */
    /* JADX WARNING: Removed duplicated region for block: B:401:0x0ba8  */
    /* JADX WARNING: Removed duplicated region for block: B:404:0x0baf  */
    public TextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        int buffertype;
        BufferType bufferType;
        boolean singleLine;
        boolean numberPasswordInputType;
        boolean passwordInputType;
        boolean webPasswordInputType;
        boolean selectallonfocus;
        ColorStateList drawableTint;
        Context context2;
        Drawable drawableStart;
        int ellipsize;
        boolean isPassword;
        boolean isMonospaceEnforced;
        TextAppearanceAttributes attributes;
        int maxlength;
        int maxlength2;
        CharSequence hint;
        TypedArray a;
        TypedValue val;
        int focusable;
        int i;
        int firstBaselineToTopHeight;
        int lastBaselineToBottomHeight;
        int lineHeight;
        float autoSizeMinTextSizeInPx;
        int i2;
        float autoSizeMaxTextSizeInPx;
        boolean z;
        int inputType;
        TextKeyListener.Capitalize cap;
        boolean z2;
        int i3;
        int i4;
        boolean singleLine2;
        boolean editable;
        int buffertype2;
        boolean password;
        int i5;
        boolean editable2;
        int i6;
        boolean password2;
        int i7;
        int i8;
        boolean password3;
        int i9;
        boolean password4;
        int i10;
        Context context3 = context;
        AttributeSet attributeSet = attrs;
        int i11 = defStyleAttr;
        int i12 = defStyleRes;
        this.mHwTextView = null;
        this.mValidSetCursorEvent = false;
        this.mEditableFactory = Editable.Factory.getInstance();
        this.mSpannableFactory = Spannable.Factory.getInstance();
        this.mTextViewDirection = 1;
        this.mMarqueeRepeatLimit = 3;
        this.mLastLayoutDirection = -1;
        this.mMarqueeFadeMode = 0;
        this.mBufferType = BufferType.NORMAL;
        this.mLocalesChanged = false;
        this.mListenerChanged = false;
        this.mGravity = 8388659;
        this.mLinksClickable = true;
        this.mSpacingMult = 1.0f;
        this.mSpacingAdd = 0.0f;
        this.mMaximum = Integer.MAX_VALUE;
        this.mMaxMode = 1;
        this.mMinimum = 0;
        this.mMinMode = 1;
        this.mOldMaximum = this.mMaximum;
        this.mOldMaxMode = this.mMaxMode;
        this.mMaxWidth = Integer.MAX_VALUE;
        this.mMaxWidthMode = 2;
        this.mMinWidth = 0;
        this.mMinWidthMode = 2;
        this.mDesiredHeightAtMeasure = -1;
        this.mIncludePad = true;
        this.mDeferScroll = -1;
        this.mFilters = NO_FILTERS;
        this.mHighlightColor = 1714664933;
        this.mHighlightPathBogus = true;
        this.mDeviceProvisionedState = 0;
        this.mAutoSizeTextType = 0;
        this.mNeedsAutoSizeText = false;
        this.mAutoSizeStepGranularityInPx = -1.0f;
        this.mAutoSizeMinTextSizeInPx = -1.0f;
        this.mAutoSizeMaxTextSizeInPx = -1.0f;
        this.mAutoSizeTextSizesInPx = EmptyArray.INT;
        this.mHasPresetAutoSizeValues = false;
        this.mTextSetFromXmlOrResourceId = false;
        this.mTextId = 0;
        this.isHwTheme = false;
        this.isSystemApp = false;
        this.mBoardCast = new Intent(PASTE_DONE_ACTION);
        this.mBoardCast.setPackage(BAIDU_INPUT_PACKAGE);
        this.mHwTextView = HwWidgetFactory.getHwTextView(context3, this, attributeSet);
        if (getImportantForAutofill() == 0) {
            setImportantForAutofill(1);
        }
        setTextInternal("");
        Resources res = getResources();
        CompatibilityInfo compat = res.getCompatibilityInfo();
        this.mTextPaint = new TextPaint(1);
        this.mTextPaint.density = res.getDisplayMetrics().density;
        this.mTextPaint.setCompatibilityScaling(compat.applicationScale);
        this.mHighlightPaint = new Paint(1);
        this.mHighlightPaint.setCompatibilityScaling(compat.applicationScale);
        this.mMovement = getDefaultMovementMethod();
        this.mTransformation = null;
        TextAppearanceAttributes attributes2 = new TextAppearanceAttributes();
        attributes2.mTextColor = ColorStateList.valueOf(-16777216);
        attributes2.mTextSize = 15;
        this.mBreakStrategy = 0;
        this.mHyphenationFrequency = 0;
        this.mJustificationMode = 0;
        Resources.Theme theme = context.getTheme();
        TypedArray a2 = theme.obtainStyledAttributes(attributeSet, R.styleable.TextViewAppearance, i11, i12);
        TypedArray appearance = null;
        int ap = a2.getResourceId(0, -1);
        a2.recycle();
        TypedArray appearance2 = ap != -1 ? theme.obtainStyledAttributes(ap, R.styleable.TextAppearance) : appearance;
        if (appearance2 != null) {
            readTextAppearance(context3, appearance2, attributes2, false);
            attributes2.mFontFamilyExplicit = false;
            appearance2.recycle();
        }
        boolean editable3 = getDefaultEditable();
        float autoSizeMinTextSizeInPx2 = -1.0f;
        float autoSizeMaxTextSizeInPx2 = -1.0f;
        float autoSizeStepGranularityInPx = -1.0f;
        TypedArray a3 = theme.obtainStyledAttributes(attributeSet, R.styleable.TextView, i11, i12);
        readTextAppearance(context3, a3, attributes2, true);
        int n = a3.getIndexCount();
        Locale loc = Locale.getDefault();
        if ("my".equals(loc.getLanguage()) != 0 && "MM".equals(loc.getCountry())) {
            this.mSpacingMult = MSPACINGMULTFORMY;
        } else if ("si".equals(loc.getLanguage())) {
            this.mSpacingMult = MSPACINGMULTFORSI;
        }
        Resources.Theme theme2 = theme;
        int i13 = ap;
        TypedArray typedArray = appearance2;
        TextAppearanceAttributes attributes3 = attributes2;
        Resources resources = res;
        CompatibilityInfo compatibilityInfo = compat;
        boolean editable4 = editable3;
        CharSequence inputMethod = null;
        int numeric = 0;
        CharSequence digits = null;
        boolean phone = false;
        boolean autotext = false;
        int autocap = -1;
        int buffertype3 = 0;
        boolean selectallonfocus2 = false;
        Drawable drawableLeft = null;
        Drawable drawableTop = null;
        Drawable drawableRight = null;
        Drawable drawableBottom = null;
        Drawable drawableStart2 = null;
        Drawable drawableEnd = null;
        ColorStateList drawableTint2 = null;
        PorterDuff.Mode drawableTintMode = null;
        int drawablePadding = 0;
        int ellipsize2 = -1;
        boolean singleLine3 = false;
        int maxlength3 = -1;
        CharSequence text = "";
        CharSequence hint2 = null;
        boolean password5 = false;
        int inputType2 = 0;
        int lastBaselineToBottomHeight2 = -1;
        int lineHeight2 = -1;
        int firstBaselineToTopHeight2 = -1;
        boolean textIsSetFromXml = false;
        int i14 = 0;
        while (true) {
            buffertype = buffertype3;
            int buffertype4 = i14;
            if (buffertype4 < n) {
                int n2 = n;
                int attr = a3.getIndex(buffertype4);
                if (attr == 0) {
                    editable = editable4;
                    singleLine2 = singleLine3;
                    i4 = buffertype4;
                    password = password5;
                    i5 = ellipsize2;
                    buffertype2 = buffertype;
                    setEnabled(a3.getBoolean(attr, isEnabled()));
                } else if (attr != 67) {
                    switch (attr) {
                        case 9:
                            i4 = buffertype4;
                            ellipsize2 = a3.getInt(attr, ellipsize2);
                            editable4 = editable4;
                            password5 = password5;
                            buffertype3 = buffertype;
                            break;
                        case 10:
                            editable2 = editable4;
                            i6 = buffertype4;
                            password2 = password5;
                            buffertype2 = buffertype;
                            setGravity(a3.getInt(attr, -1));
                            break;
                        case 11:
                            editable2 = editable4;
                            i6 = buffertype4;
                            password2 = password5;
                            buffertype2 = buffertype;
                            this.mAutoLinkMask = a3.getInt(attr, 0);
                            break;
                        case 12:
                            editable2 = editable4;
                            i6 = buffertype4;
                            password2 = password5;
                            buffertype2 = buffertype;
                            this.mLinksClickable = a3.getBoolean(attr, true);
                            break;
                        case 13:
                            editable2 = editable4;
                            i6 = buffertype4;
                            password2 = password5;
                            buffertype2 = buffertype;
                            setMaxWidth(a3.getDimensionPixelSize(attr, -1));
                            break;
                        case 14:
                            editable2 = editable4;
                            i6 = buffertype4;
                            password2 = password5;
                            buffertype2 = buffertype;
                            setMaxHeight(a3.getDimensionPixelSize(attr, -1));
                            break;
                        case 15:
                            editable2 = editable4;
                            i6 = buffertype4;
                            password2 = password5;
                            buffertype2 = buffertype;
                            setMinWidth(a3.getDimensionPixelSize(attr, -1));
                            break;
                        case 16:
                            i6 = buffertype4;
                            password2 = password5;
                            buffertype2 = buffertype;
                            editable2 = editable4;
                            setMinHeight(a3.getDimensionPixelSize(attr, -1));
                            break;
                        case 17:
                            i4 = buffertype4;
                            password5 = password5;
                            buffertype3 = a3.getInt(attr, buffertype);
                            break;
                        case 18:
                            i4 = buffertype4;
                            this.mTextId = a3.getResourceId(attr, 0);
                            text = a3.getText(attr);
                            password5 = password5;
                            buffertype3 = buffertype;
                            textIsSetFromXml = true;
                            break;
                        case 19:
                            i7 = buffertype4;
                            hint2 = a3.getText(attr);
                            password5 = password5;
                            break;
                        case 20:
                            i8 = buffertype4;
                            password = password5;
                            setTextScaleX(a3.getFloat(attr, 1.0f));
                            break;
                        case 21:
                            i8 = buffertype4;
                            password = password5;
                            if (!a3.getBoolean(attr, true)) {
                                setCursorVisible(false);
                                break;
                            }
                            break;
                        case 22:
                            i8 = buffertype4;
                            password = password5;
                            setMaxLines(a3.getInt(attr, -1));
                            break;
                        case 23:
                            i8 = buffertype4;
                            password = password5;
                            setLines(a3.getInt(attr, -1));
                            break;
                        case 24:
                            i8 = buffertype4;
                            password = password5;
                            setHeight(a3.getDimensionPixelSize(attr, -1));
                            break;
                        case 25:
                            i8 = buffertype4;
                            password = password5;
                            setMinLines(a3.getInt(attr, -1));
                            break;
                        case 26:
                            i8 = buffertype4;
                            password = password5;
                            setMaxEms(a3.getInt(attr, -1));
                            break;
                        case 27:
                            i8 = buffertype4;
                            password = password5;
                            setEms(a3.getInt(attr, -1));
                            break;
                        case 28:
                            i8 = buffertype4;
                            password = password5;
                            setWidth(a3.getDimensionPixelSize(attr, -1));
                            break;
                        case 29:
                            i8 = buffertype4;
                            password = password5;
                            setMinEms(a3.getInt(attr, -1));
                            break;
                        case 30:
                            i8 = buffertype4;
                            password = password5;
                            if (a3.getBoolean(attr, false)) {
                                setHorizontallyScrolling(true);
                                break;
                            }
                            break;
                        case 31:
                            i7 = buffertype4;
                            password5 = a3.getBoolean(attr, password5);
                            break;
                        case 32:
                            i9 = buffertype4;
                            password3 = password5;
                            singleLine3 = a3.getBoolean(attr, singleLine3);
                            break;
                        case 33:
                            i9 = buffertype4;
                            password3 = password5;
                            selectallonfocus2 = a3.getBoolean(attr, selectallonfocus2);
                            break;
                        case 34:
                            i10 = buffertype4;
                            password4 = password5;
                            if (!a3.getBoolean(attr, true)) {
                                setIncludeFontPadding(false);
                                break;
                            }
                            break;
                        case 35:
                            i9 = buffertype4;
                            password3 = password5;
                            maxlength3 = a3.getInt(attr, -1);
                            break;
                        default:
                            switch (attr) {
                                case 40:
                                    i9 = buffertype4;
                                    password3 = password5;
                                    numeric = a3.getInt(attr, numeric);
                                    break;
                                case 41:
                                    i9 = buffertype4;
                                    password3 = password5;
                                    digits = a3.getText(attr);
                                    break;
                                case 42:
                                    i9 = buffertype4;
                                    password3 = password5;
                                    phone = a3.getBoolean(attr, phone);
                                    break;
                                case 43:
                                    i9 = buffertype4;
                                    password3 = password5;
                                    inputMethod = a3.getText(attr);
                                    break;
                                case 44:
                                    i9 = buffertype4;
                                    password3 = password5;
                                    autocap = a3.getInt(attr, autocap);
                                    break;
                                case 45:
                                    i9 = buffertype4;
                                    password3 = password5;
                                    autotext = a3.getBoolean(attr, autotext);
                                    break;
                                case 46:
                                    i9 = buffertype4;
                                    password3 = password5;
                                    editable4 = a3.getBoolean(attr, editable4);
                                    break;
                                case 47:
                                    i10 = buffertype4;
                                    password4 = password5;
                                    this.mFreezesText = a3.getBoolean(attr, false);
                                    break;
                                case 48:
                                    i9 = buffertype4;
                                    password3 = password5;
                                    drawableTop = a3.getDrawable(attr);
                                    break;
                                case 49:
                                    i9 = buffertype4;
                                    password3 = password5;
                                    drawableBottom = a3.getDrawable(attr);
                                    break;
                                case 50:
                                    i9 = buffertype4;
                                    password3 = password5;
                                    drawableLeft = a3.getDrawable(attr);
                                    break;
                                case 51:
                                    i9 = buffertype4;
                                    password3 = password5;
                                    drawableRight = a3.getDrawable(attr);
                                    break;
                                case 52:
                                    i9 = buffertype4;
                                    password3 = password5;
                                    drawablePadding = a3.getDimensionPixelSize(attr, drawablePadding);
                                    break;
                                case 53:
                                    i10 = buffertype4;
                                    password4 = password5;
                                    this.mSpacingAdd = (float) a3.getDimensionPixelSize(attr, (int) this.mSpacingAdd);
                                    break;
                                case 54:
                                    i10 = buffertype4;
                                    password4 = password5;
                                    this.mSpacingMult = a3.getFloat(attr, this.mSpacingMult);
                                    if ("my".equals(loc.getLanguage()) && "MM".equals(loc.getCountry())) {
                                        if (this.mSpacingMult < MSPACINGMULTFORMY) {
                                            this.mSpacingMult = MSPACINGMULTFORMY;
                                            break;
                                        }
                                    }
                                    if ("si".equals(loc.getLanguage()) && this.mSpacingMult < MSPACINGMULTFORSI) {
                                        this.mSpacingMult = MSPACINGMULTFORSI;
                                        break;
                                    }
                                    break;
                                case 55:
                                    i10 = buffertype4;
                                    password4 = password5;
                                    setMarqueeRepeatLimit(a3.getInt(attr, this.mMarqueeRepeatLimit));
                                    break;
                                case 56:
                                    i7 = buffertype4;
                                    boolean z3 = password5;
                                    inputType2 = a3.getInt(attr, 0);
                                    break;
                                case 57:
                                    i10 = buffertype4;
                                    password4 = password5;
                                    setPrivateImeOptions(a3.getString(attr));
                                    break;
                                case 58:
                                    i10 = buffertype4;
                                    password4 = password5;
                                    try {
                                        setInputExtras(a3.getResourceId(attr, 0));
                                        break;
                                    } catch (XmlPullParserException e) {
                                        Log.w(LOG_TAG, "Failure reading input extras", e);
                                        break;
                                    } catch (IOException e2) {
                                        Log.w(LOG_TAG, "Failure reading input extras", e2);
                                        break;
                                    }
                                case 59:
                                    i10 = buffertype4;
                                    password4 = password5;
                                    createEditorIfNeeded();
                                    this.mEditor.createInputContentTypeIfNeeded();
                                    this.mEditor.mInputContentType.imeOptions = a3.getInt(attr, this.mEditor.mInputContentType.imeOptions);
                                    break;
                                case 60:
                                    i10 = buffertype4;
                                    password4 = password5;
                                    createEditorIfNeeded();
                                    this.mEditor.createInputContentTypeIfNeeded();
                                    this.mEditor.mInputContentType.imeActionLabel = a3.getText(attr);
                                    break;
                                case 61:
                                    i10 = buffertype4;
                                    password4 = password5;
                                    createEditorIfNeeded();
                                    this.mEditor.createInputContentTypeIfNeeded();
                                    this.mEditor.mInputContentType.imeActionId = a3.getInt(attr, this.mEditor.mInputContentType.imeActionId);
                                    break;
                                case 62:
                                    i10 = buffertype4;
                                    password4 = password5;
                                    this.mTextSelectHandleLeftRes = a3.getResourceId(attr, 0);
                                    break;
                                case 63:
                                    i10 = buffertype4;
                                    password4 = password5;
                                    this.mTextSelectHandleRightRes = a3.getResourceId(attr, 0);
                                    break;
                                case 64:
                                    i10 = buffertype4;
                                    password4 = password5;
                                    this.mTextSelectHandleRes = a3.getResourceId(attr, 0);
                                    break;
                                default:
                                    switch (attr) {
                                        case 70:
                                            i10 = buffertype4;
                                            password4 = password5;
                                            this.mCursorDrawableRes = a3.getResourceId(attr, 0);
                                            break;
                                        case 71:
                                            i10 = buffertype4;
                                            password4 = password5;
                                            this.mTextEditSuggestionItemLayout = a3.getResourceId(attr, 0);
                                            break;
                                        default:
                                            switch (attr) {
                                                case 73:
                                                    i7 = buffertype4;
                                                    boolean z4 = password5;
                                                    drawableStart2 = a3.getDrawable(attr);
                                                    break;
                                                case 74:
                                                    i7 = buffertype4;
                                                    boolean z5 = password5;
                                                    drawableEnd = a3.getDrawable(attr);
                                                    break;
                                                default:
                                                    switch (attr) {
                                                        case 79:
                                                            i7 = buffertype4;
                                                            boolean z6 = password5;
                                                            drawableTint2 = a3.getColorStateList(attr);
                                                            break;
                                                        case 80:
                                                            i7 = buffertype4;
                                                            boolean z7 = password5;
                                                            drawableTintMode = Drawable.parseTintMode(a3.getInt(attr, -1), drawableTintMode);
                                                            break;
                                                        case 81:
                                                            i10 = buffertype4;
                                                            password4 = password5;
                                                            this.mBreakStrategy = a3.getInt(attr, 0);
                                                            break;
                                                        case 82:
                                                            i10 = buffertype4;
                                                            password4 = password5;
                                                            this.mHyphenationFrequency = a3.getInt(attr, 0);
                                                            break;
                                                        case 83:
                                                            i10 = buffertype4;
                                                            createEditorIfNeeded();
                                                            password4 = password5;
                                                            this.mEditor.mAllowUndo = a3.getBoolean(attr, true);
                                                            break;
                                                        case 84:
                                                            i4 = buffertype4;
                                                            this.mAutoSizeTextType = a3.getInt(attr, 0);
                                                            break;
                                                        case 85:
                                                            i7 = buffertype4;
                                                            autoSizeStepGranularityInPx = a3.getDimension(attr, -1.0f);
                                                            break;
                                                        case 86:
                                                            i4 = buffertype4;
                                                            int autoSizeStepSizeArrayResId = a3.getResourceId(attr, 0);
                                                            if (autoSizeStepSizeArrayResId > 0) {
                                                                TypedArray autoSizePresetTextSizes = a3.getResources().obtainTypedArray(autoSizeStepSizeArrayResId);
                                                                setupAutoSizeUniformPresetSizes(autoSizePresetTextSizes);
                                                                autoSizePresetTextSizes.recycle();
                                                                break;
                                                            }
                                                            break;
                                                        case 87:
                                                            i7 = buffertype4;
                                                            autoSizeMinTextSizeInPx2 = a3.getDimension(attr, -1.0f);
                                                            break;
                                                        case 88:
                                                            i7 = buffertype4;
                                                            autoSizeMaxTextSizeInPx2 = a3.getDimension(attr, -1.0f);
                                                            break;
                                                        case 89:
                                                            i4 = buffertype4;
                                                            this.mJustificationMode = a3.getInt(attr, 0);
                                                            break;
                                                        default:
                                                            switch (attr) {
                                                                case 91:
                                                                    i7 = buffertype4;
                                                                    firstBaselineToTopHeight2 = a3.getDimensionPixelSize(attr, -1);
                                                                    break;
                                                                case 92:
                                                                    i7 = buffertype4;
                                                                    lastBaselineToBottomHeight2 = a3.getDimensionPixelSize(attr, -1);
                                                                    break;
                                                                case 93:
                                                                    i7 = buffertype4;
                                                                    lineHeight2 = a3.getDimensionPixelSize(attr, -1);
                                                                    break;
                                                                default:
                                                                    switch (attr) {
                                                                        case 95:
                                                                            i4 = buffertype4;
                                                                            this.mTextEditSuggestionContainerLayout = a3.getResourceId(attr, 0);
                                                                            break;
                                                                        case 96:
                                                                            i4 = buffertype4;
                                                                            this.mTextEditSuggestionHighlightStyle = a3.getResourceId(attr, 0);
                                                                            break;
                                                                        default:
                                                                            editable = editable4;
                                                                            singleLine2 = singleLine3;
                                                                            i4 = buffertype4;
                                                                            break;
                                                                    }
                                                            }
                                                    }
                                            }
                                    }
                            }
                    }
                } else {
                    editable = editable4;
                    i4 = buffertype4;
                    password = password5;
                    i5 = ellipsize2;
                    buffertype2 = buffertype;
                    singleLine2 = singleLine3;
                    setTextIsSelectable(a3.getBoolean(attr, false));
                }
                ellipsize2 = i5;
                editable4 = editable;
                singleLine3 = singleLine2;
                int i15 = buffertype2;
                password5 = password;
                buffertype3 = i15;
                i14 = i4 + 1;
                n = n2;
            } else {
                boolean editable5 = editable4;
                boolean singleLine4 = singleLine3;
                int i16 = n;
                boolean password6 = password5;
                int ellipsize3 = ellipsize2;
                int buffertype5 = buffertype;
                a3.recycle();
                BufferType bufferType2 = BufferType.EDITABLE;
                int inputType3 = inputType2;
                int variation = inputType3 & FileObserver.ALL_EVENTS;
                Locale locale = loc;
                boolean passwordInputType2 = variation == 129;
                boolean webPasswordInputType2 = variation == 225;
                boolean numberPasswordInputType2 = variation == 18;
                boolean editable6 = editable5;
                int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
                this.mUseInternationalizedInput = targetSdkVersion >= 26;
                this.mUseFallbackLineSpacing = targetSdkVersion >= 28;
                if (inputMethod != null) {
                    int i17 = variation;
                    CharSequence inputMethod2 = inputMethod;
                    try {
                        Class<?> c = Class.forName(inputMethod2.toString());
                        try {
                            createEditorIfNeeded();
                            CharSequence charSequence = inputMethod2;
                            Class<?> c2 = c;
                            try {
                                Class<?> cls = c2;
                            } catch (InstantiationException e3) {
                                ex = e3;
                                Class<?> cls2 = c2;
                                int i18 = targetSdkVersion;
                                throw new RuntimeException(ex);
                            } catch (IllegalAccessException e4) {
                                ex = e4;
                                Class<?> cls3 = c2;
                                int i19 = targetSdkVersion;
                                throw new RuntimeException(ex);
                            }
                            try {
                                this.mEditor.mKeyListener = (KeyListener) c2.newInstance();
                                try {
                                    this.mEditor.mInputType = inputType3 != 0 ? inputType3 : this.mEditor.mKeyListener.getInputType();
                                    int i20 = targetSdkVersion;
                                } catch (IncompatibleClassChangeError e5) {
                                    int i21 = targetSdkVersion;
                                    this.mEditor.mInputType = 1;
                                }
                                boolean z8 = phone;
                                CharSequence charSequence2 = digits;
                            } catch (InstantiationException e6) {
                                ex = e6;
                                int i22 = targetSdkVersion;
                            } catch (IllegalAccessException e7) {
                                ex = e7;
                                int i23 = targetSdkVersion;
                                throw new RuntimeException(ex);
                            }
                        } catch (InstantiationException e8) {
                            ex = e8;
                            CharSequence charSequence3 = inputMethod2;
                            int i24 = targetSdkVersion;
                            Class<?> cls4 = c;
                            throw new RuntimeException(ex);
                        } catch (IllegalAccessException e9) {
                            ex = e9;
                            CharSequence charSequence4 = inputMethod2;
                            int i25 = targetSdkVersion;
                            Class<?> cls5 = c;
                            throw new RuntimeException(ex);
                        }
                    } catch (ClassNotFoundException ex) {
                        CharSequence charSequence5 = inputMethod2;
                        int i26 = targetSdkVersion;
                        throw new RuntimeException(ex);
                    }
                } else {
                    int i27 = targetSdkVersion;
                    CharSequence charSequence6 = inputMethod;
                    if (digits != null) {
                        createEditorIfNeeded();
                        CharSequence digits2 = digits;
                        this.mEditor.mKeyListener = DigitsKeyListener.getInstance(digits2.toString());
                        Editor editor = this.mEditor;
                        if (inputType3 != 0) {
                            i3 = inputType3;
                        } else {
                            i3 = 1;
                        }
                        editor.mInputType = i3;
                        CharSequence charSequence7 = digits2;
                        boolean z9 = phone;
                    } else {
                        CharSequence digits3 = digits;
                        if (inputType3 != 0) {
                            setInputType(inputType3, true);
                            singleLine = !isMultilineInputType(inputType3);
                            CharSequence charSequence8 = digits3;
                            boolean z10 = phone;
                        } else {
                            if (phone) {
                                createEditorIfNeeded();
                                this.mEditor.mKeyListener = DialerKeyListener.getInstance();
                                this.mEditor.mInputType = 3;
                                CharSequence charSequence9 = digits3;
                                boolean z11 = phone;
                                inputType3 = 3;
                            } else {
                                if (numeric != 0) {
                                    createEditorIfNeeded();
                                    Editor editor2 = this.mEditor;
                                    boolean z12 = (numeric & 2) != 0;
                                    if ((numeric & 4) != 0) {
                                        CharSequence charSequence10 = digits3;
                                        z2 = true;
                                    } else {
                                        CharSequence charSequence11 = digits3;
                                        z2 = false;
                                    }
                                    boolean z13 = phone;
                                    editor2.mKeyListener = DigitsKeyListener.getInstance(null, z12, z2);
                                    inputType = this.mEditor.mKeyListener.getInputType();
                                    this.mEditor.mInputType = inputType;
                                } else {
                                    boolean z14 = phone;
                                    if (autotext || autocap != -1) {
                                        inputType = 1;
                                        switch (autocap) {
                                            case 1:
                                                cap = TextKeyListener.Capitalize.SENTENCES;
                                                inputType = 1 | 16384;
                                                break;
                                            case 2:
                                                cap = TextKeyListener.Capitalize.WORDS;
                                                inputType = 1 | 8192;
                                                break;
                                            case 3:
                                                cap = TextKeyListener.Capitalize.CHARACTERS;
                                                inputType = 1 | 4096;
                                                break;
                                            default:
                                                cap = TextKeyListener.Capitalize.NONE;
                                                break;
                                        }
                                        createEditorIfNeeded();
                                        this.mEditor.mKeyListener = TextKeyListener.getInstance(autotext, cap);
                                        this.mEditor.mInputType = inputType;
                                    } else if (editable6) {
                                        createEditorIfNeeded();
                                        this.mEditor.mKeyListener = TextKeyListener.getInstance();
                                        this.mEditor.mInputType = 1;
                                    } else {
                                        if (!isTextSelectable()) {
                                            if (this.mEditor != null) {
                                                this.mEditor.mKeyListener = null;
                                            }
                                            switch (buffertype5) {
                                                case 0:
                                                    bufferType = BufferType.NORMAL;
                                                    break;
                                                case 1:
                                                    bufferType = BufferType.SPANNABLE;
                                                    break;
                                                case 2:
                                                    bufferType = BufferType.EDITABLE;
                                                    break;
                                            }
                                        } else {
                                            if (this.mEditor != null) {
                                                this.mEditor.mKeyListener = null;
                                                this.mEditor.mInputType = 0;
                                            }
                                            bufferType = BufferType.SPANNABLE;
                                            setMovementMethod(ArrowKeyMovementMethod.getInstance());
                                        }
                                        singleLine = singleLine4;
                                        if (this.mEditor != null) {
                                            int i28 = inputType3;
                                            int i29 = autocap;
                                            passwordInputType = passwordInputType2;
                                            webPasswordInputType = webPasswordInputType2;
                                            numberPasswordInputType = numberPasswordInputType2;
                                            this.mEditor.adjustInputType(password6, passwordInputType, webPasswordInputType, numberPasswordInputType);
                                        } else {
                                            int i30 = inputType3;
                                            int i31 = autocap;
                                            passwordInputType = passwordInputType2;
                                            webPasswordInputType = webPasswordInputType2;
                                            numberPasswordInputType = numberPasswordInputType2;
                                        }
                                        if (selectallonfocus2) {
                                            createEditorIfNeeded();
                                            selectallonfocus = selectallonfocus2;
                                            this.mEditor.mSelectAllOnFocus = true;
                                            if (bufferType == BufferType.NORMAL) {
                                                bufferType = BufferType.SPANNABLE;
                                            }
                                        } else {
                                            selectallonfocus = selectallonfocus2;
                                        }
                                        drawableTint = drawableTint2;
                                        if (drawableTint == null || drawableTintMode != null) {
                                            if (this.mDrawables == null) {
                                                boolean z15 = autotext;
                                                context2 = context;
                                                this.mDrawables = new Drawables(context2);
                                            } else {
                                                context2 = context;
                                            }
                                            if (drawableTint != null) {
                                                this.mDrawables.mTintList = drawableTint;
                                                ColorStateList colorStateList = drawableTint;
                                                z = true;
                                                this.mDrawables.mHasTint = true;
                                            } else {
                                                ColorStateList colorStateList2 = drawableTint;
                                                z = true;
                                            }
                                            if (drawableTintMode != null) {
                                                this.mDrawables.mTintMode = drawableTintMode;
                                                this.mDrawables.mHasTintMode = z;
                                            }
                                        } else {
                                            ColorStateList colorStateList3 = drawableTint;
                                            boolean z16 = autotext;
                                            context2 = context;
                                        }
                                        PorterDuff.Mode mode = drawableTintMode;
                                        TypedArray typedArray2 = a3;
                                        Drawable drawableTop2 = drawableTop;
                                        Drawable drawableBottom2 = drawableBottom;
                                        Drawable drawableLeft2 = drawableLeft;
                                        setCompoundDrawablesWithIntrinsicBounds(drawableLeft2, drawableTop2, drawableRight, drawableBottom2);
                                        Drawable drawable = drawableTop2;
                                        Drawable drawable2 = drawableBottom2;
                                        drawableStart = drawableStart2;
                                        Drawable drawableEnd2 = drawableEnd;
                                        setRelativeDrawablesIfNeeded(drawableStart, drawableEnd2);
                                        setCompoundDrawablePadding(drawablePadding);
                                        setInputTypeSingleLine(singleLine);
                                        applySingleLine(singleLine, singleLine, singleLine);
                                        if (singleLine || getKeyListener() != null) {
                                        } else {
                                            Drawable drawable3 = drawableStart;
                                            if (ellipsize3 == -1) {
                                                ellipsize = 3;
                                                switch (ellipsize) {
                                                    case 1:
                                                        setEllipsize(TextUtils.TruncateAt.START);
                                                        break;
                                                    case 2:
                                                        setEllipsize(TextUtils.TruncateAt.MIDDLE);
                                                        break;
                                                    case 3:
                                                        setEllipsize(TextUtils.TruncateAt.END);
                                                        break;
                                                    case 4:
                                                        if (ViewConfiguration.get(context).isFadingMarqueeEnabled()) {
                                                            setHorizontalFadingEdgeEnabled(true);
                                                            this.mMarqueeFadeMode = 0;
                                                        } else {
                                                            setHorizontalFadingEdgeEnabled(false);
                                                            this.mMarqueeFadeMode = 1;
                                                        }
                                                        setEllipsize(TextUtils.TruncateAt.MARQUEE);
                                                        break;
                                                }
                                                isPassword = !password6 || passwordInputType || webPasswordInputType || numberPasswordInputType;
                                                if (isPassword) {
                                                    if (this.mEditor != null) {
                                                        Drawable drawable4 = drawableEnd2;
                                                    }
                                                    isMonospaceEnforced = false;
                                                    if (isMonospaceEnforced) {
                                                        boolean z17 = isMonospaceEnforced;
                                                        attributes = attributes3;
                                                        attributes.mTypefaceIndex = 3;
                                                    } else {
                                                        boolean z18 = isMonospaceEnforced;
                                                        attributes = attributes3;
                                                    }
                                                    applyTextAppearance(attributes);
                                                    if (isPassword) {
                                                        setTransformationMethod(PasswordTransformationMethod.getInstance());
                                                    }
                                                    if (maxlength3 >= 0) {
                                                        boolean z19 = singleLine;
                                                        boolean z20 = isPassword;
                                                        maxlength = maxlength3;
                                                        setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxlength)});
                                                    } else {
                                                        boolean z21 = isPassword;
                                                        maxlength = maxlength3;
                                                        setFilters(NO_FILTERS);
                                                    }
                                                    CharSequence text2 = text;
                                                    setText(text2, bufferType);
                                                    if (textIsSetFromXml) {
                                                        maxlength2 = maxlength;
                                                        this.mTextSetFromXmlOrResourceId = true;
                                                    } else {
                                                        maxlength2 = maxlength;
                                                    }
                                                    if (hint2 != null) {
                                                        hint = hint2;
                                                        setHint(hint);
                                                    } else {
                                                        hint = hint2;
                                                    }
                                                    boolean canInputOrMove = this.mMovement == null || getKeyListener() != null;
                                                    boolean clickable = !canInputOrMove || isClickable();
                                                    boolean longClickable = !canInputOrMove || isLongClickable();
                                                    int focusable2 = getFocusable();
                                                    boolean z22 = canInputOrMove;
                                                    CharSequence charSequence12 = hint;
                                                    BufferType bufferType3 = bufferType;
                                                    CharSequence charSequence13 = text2;
                                                    boolean z23 = selectallonfocus;
                                                    int i32 = maxlength2;
                                                    AttributeSet attributeSet2 = attrs;
                                                    a = context2.obtainStyledAttributes(attributeSet2, R.styleable.View, defStyleAttr, defStyleRes);
                                                    val = new TypedValue();
                                                    if (a.getValue(19, val)) {
                                                        TextAppearanceAttributes textAppearanceAttributes = attributes;
                                                        focusable = val.type == 18 ? val.data == 0 ? 0 : 1 : val.data;
                                                    } else {
                                                        focusable = focusable2;
                                                    }
                                                    TypedValue typedValue = val;
                                                    boolean clickable2 = a.getBoolean(30, clickable);
                                                    Drawable drawable5 = drawableLeft2;
                                                    boolean longClickable2 = a.getBoolean(31, longClickable);
                                                    a.recycle();
                                                    if (focusable != getFocusable()) {
                                                        setFocusable(focusable);
                                                    }
                                                    setClickable(clickable2);
                                                    setLongClickable(longClickable2);
                                                    if (this.mEditor != null) {
                                                        this.mEditor.prepareCursorControllers();
                                                    }
                                                    if (getImportantForAccessibility() == 0) {
                                                        i = 1;
                                                        setImportantForAccessibility(1);
                                                    } else {
                                                        i = 1;
                                                    }
                                                    if (supportsAutoSizeText()) {
                                                        TypedArray typedArray3 = a;
                                                        if (this.mAutoSizeTextType == i) {
                                                            if (!this.mHasPresetAutoSizeValues) {
                                                                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                                                                if (autoSizeMinTextSizeInPx2 == -1.0f) {
                                                                    boolean z24 = clickable2;
                                                                    i2 = 2;
                                                                    autoSizeMinTextSizeInPx = TypedValue.applyDimension(2, 12.0f, displayMetrics);
                                                                } else {
                                                                    i2 = 2;
                                                                    autoSizeMinTextSizeInPx = autoSizeMinTextSizeInPx2;
                                                                }
                                                                if (autoSizeMaxTextSizeInPx2 == -1.0f) {
                                                                    int i33 = focusable;
                                                                    autoSizeMaxTextSizeInPx = TypedValue.applyDimension(i2, 112.0f, displayMetrics);
                                                                } else {
                                                                    autoSizeMaxTextSizeInPx = autoSizeMaxTextSizeInPx2;
                                                                }
                                                                float autoSizeStepGranularityInPx2 = autoSizeStepGranularityInPx == -1.0f ? 1.0f : autoSizeStepGranularityInPx;
                                                                validateAndSetAutoSizeTextTypeUniformConfiguration(autoSizeMinTextSizeInPx, autoSizeMaxTextSizeInPx, autoSizeStepGranularityInPx2);
                                                                float f = autoSizeMaxTextSizeInPx;
                                                                float f2 = autoSizeStepGranularityInPx2;
                                                                float f3 = autoSizeMinTextSizeInPx;
                                                            } else {
                                                                int i34 = focusable;
                                                            }
                                                            setupAutoSizeText();
                                                        } else {
                                                            int i35 = focusable;
                                                        }
                                                    } else {
                                                        boolean z25 = clickable2;
                                                        int i36 = focusable;
                                                        this.mAutoSizeTextType = 0;
                                                    }
                                                    firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                                    if (firstBaselineToTopHeight >= 0) {
                                                        setFirstBaselineToTopHeight(firstBaselineToTopHeight);
                                                    }
                                                    lastBaselineToBottomHeight = lastBaselineToBottomHeight2;
                                                    if (lastBaselineToBottomHeight >= 0) {
                                                        setLastBaselineToBottomHeight(lastBaselineToBottomHeight);
                                                    }
                                                    lineHeight = lineHeight2;
                                                    if (lineHeight >= 0) {
                                                        setLineHeight(lineHeight);
                                                    }
                                                    IS_AMOLED = isAMOLED();
                                                    this.isHwTheme = IS_AMOLED ? HwWidgetFactory.checkIsHwTheme(getContext(), attributeSet2) : false;
                                                    this.isSystemApp = !IS_AMOLED && (context.getApplicationInfo().isSystemApp() || context.getApplicationInfo().isUpdatedSystemApp());
                                                    return;
                                                }
                                                isMonospaceEnforced = true;
                                                if (isMonospaceEnforced) {
                                                }
                                                applyTextAppearance(attributes);
                                                if (isPassword) {
                                                }
                                                if (maxlength3 >= 0) {
                                                }
                                                CharSequence text22 = text;
                                                setText(text22, bufferType);
                                                if (textIsSetFromXml) {
                                                }
                                                if (hint2 != null) {
                                                }
                                                if (this.mMovement == null) {
                                                }
                                                boolean clickable3 = !canInputOrMove || isClickable();
                                                boolean longClickable3 = !canInputOrMove || isLongClickable();
                                                int focusable22 = getFocusable();
                                                boolean z222 = canInputOrMove;
                                                CharSequence charSequence122 = hint;
                                                BufferType bufferType32 = bufferType;
                                                CharSequence charSequence132 = text22;
                                                boolean z232 = selectallonfocus;
                                                int i322 = maxlength2;
                                                AttributeSet attributeSet22 = attrs;
                                                a = context2.obtainStyledAttributes(attributeSet22, R.styleable.View, defStyleAttr, defStyleRes);
                                                val = new TypedValue();
                                                if (a.getValue(19, val)) {
                                                }
                                                TypedValue typedValue2 = val;
                                                boolean clickable22 = a.getBoolean(30, clickable3);
                                                Drawable drawable52 = drawableLeft2;
                                                boolean longClickable22 = a.getBoolean(31, longClickable3);
                                                a.recycle();
                                                if (focusable != getFocusable()) {
                                                }
                                                setClickable(clickable22);
                                                setLongClickable(longClickable22);
                                                if (this.mEditor != null) {
                                                }
                                                if (getImportantForAccessibility() == 0) {
                                                }
                                                if (supportsAutoSizeText()) {
                                                }
                                                firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                                if (firstBaselineToTopHeight >= 0) {
                                                }
                                                lastBaselineToBottomHeight = lastBaselineToBottomHeight2;
                                                if (lastBaselineToBottomHeight >= 0) {
                                                }
                                                lineHeight = lineHeight2;
                                                if (lineHeight >= 0) {
                                                }
                                                IS_AMOLED = isAMOLED();
                                                this.isHwTheme = IS_AMOLED ? HwWidgetFactory.checkIsHwTheme(getContext(), attributeSet22) : false;
                                                this.isSystemApp = !IS_AMOLED && (context.getApplicationInfo().isSystemApp() || context.getApplicationInfo().isUpdatedSystemApp());
                                                return;
                                            }
                                        }
                                        ellipsize = ellipsize3;
                                        switch (ellipsize) {
                                            case 1:
                                                break;
                                            case 2:
                                                break;
                                            case 3:
                                                break;
                                            case 4:
                                                break;
                                        }
                                        if (!password6) {
                                        }
                                        if (isPassword) {
                                        }
                                        isMonospaceEnforced = true;
                                        if (isMonospaceEnforced) {
                                        }
                                        applyTextAppearance(attributes);
                                        if (isPassword) {
                                        }
                                        if (maxlength3 >= 0) {
                                        }
                                        CharSequence text222 = text;
                                        setText(text222, bufferType);
                                        if (textIsSetFromXml) {
                                        }
                                        if (hint2 != null) {
                                        }
                                        if (this.mMovement == null) {
                                        }
                                        boolean clickable32 = !canInputOrMove || isClickable();
                                        boolean longClickable32 = !canInputOrMove || isLongClickable();
                                        int focusable222 = getFocusable();
                                        boolean z2222 = canInputOrMove;
                                        CharSequence charSequence1222 = hint;
                                        BufferType bufferType322 = bufferType;
                                        CharSequence charSequence1322 = text222;
                                        boolean z2322 = selectallonfocus;
                                        int i3222 = maxlength2;
                                        AttributeSet attributeSet222 = attrs;
                                        a = context2.obtainStyledAttributes(attributeSet222, R.styleable.View, defStyleAttr, defStyleRes);
                                        val = new TypedValue();
                                        if (a.getValue(19, val)) {
                                        }
                                        TypedValue typedValue22 = val;
                                        boolean clickable222 = a.getBoolean(30, clickable32);
                                        Drawable drawable522 = drawableLeft2;
                                        boolean longClickable222 = a.getBoolean(31, longClickable32);
                                        a.recycle();
                                        if (focusable != getFocusable()) {
                                        }
                                        setClickable(clickable222);
                                        setLongClickable(longClickable222);
                                        if (this.mEditor != null) {
                                        }
                                        if (getImportantForAccessibility() == 0) {
                                        }
                                        if (supportsAutoSizeText()) {
                                        }
                                        firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                        if (firstBaselineToTopHeight >= 0) {
                                        }
                                        lastBaselineToBottomHeight = lastBaselineToBottomHeight2;
                                        if (lastBaselineToBottomHeight >= 0) {
                                        }
                                        lineHeight = lineHeight2;
                                        if (lineHeight >= 0) {
                                        }
                                        IS_AMOLED = isAMOLED();
                                        this.isHwTheme = IS_AMOLED ? HwWidgetFactory.checkIsHwTheme(getContext(), attributeSet222) : false;
                                        this.isSystemApp = !IS_AMOLED && (context.getApplicationInfo().isSystemApp() || context.getApplicationInfo().isUpdatedSystemApp());
                                        return;
                                    }
                                }
                                inputType3 = inputType;
                            }
                            singleLine = singleLine4;
                        }
                        bufferType = bufferType2;
                        if (this.mEditor != null) {
                        }
                        if (selectallonfocus2) {
                        }
                        drawableTint = drawableTint2;
                        if (drawableTint == null) {
                        }
                        if (this.mDrawables == null) {
                        }
                        if (drawableTint != null) {
                        }
                        if (drawableTintMode != null) {
                        }
                        PorterDuff.Mode mode2 = drawableTintMode;
                        TypedArray typedArray22 = a3;
                        Drawable drawableTop22 = drawableTop;
                        Drawable drawableBottom22 = drawableBottom;
                        Drawable drawableLeft22 = drawableLeft;
                        setCompoundDrawablesWithIntrinsicBounds(drawableLeft22, drawableTop22, drawableRight, drawableBottom22);
                        Drawable drawable6 = drawableTop22;
                        Drawable drawable22 = drawableBottom22;
                        drawableStart = drawableStart2;
                        Drawable drawableEnd22 = drawableEnd;
                        setRelativeDrawablesIfNeeded(drawableStart, drawableEnd22);
                        setCompoundDrawablePadding(drawablePadding);
                        setInputTypeSingleLine(singleLine);
                        applySingleLine(singleLine, singleLine, singleLine);
                        if (singleLine) {
                        }
                        ellipsize = ellipsize3;
                        switch (ellipsize) {
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                            case 4:
                                break;
                        }
                        if (!password6) {
                        }
                        if (isPassword) {
                        }
                        isMonospaceEnforced = true;
                        if (isMonospaceEnforced) {
                        }
                        applyTextAppearance(attributes);
                        if (isPassword) {
                        }
                        if (maxlength3 >= 0) {
                        }
                        CharSequence text2222 = text;
                        setText(text2222, bufferType);
                        if (textIsSetFromXml) {
                        }
                        if (hint2 != null) {
                        }
                        if (this.mMovement == null) {
                        }
                        boolean clickable322 = !canInputOrMove || isClickable();
                        boolean longClickable322 = !canInputOrMove || isLongClickable();
                        int focusable2222 = getFocusable();
                        boolean z22222 = canInputOrMove;
                        CharSequence charSequence12222 = hint;
                        BufferType bufferType3222 = bufferType;
                        CharSequence charSequence13222 = text2222;
                        boolean z23222 = selectallonfocus;
                        int i32222 = maxlength2;
                        AttributeSet attributeSet2222 = attrs;
                        a = context2.obtainStyledAttributes(attributeSet2222, R.styleable.View, defStyleAttr, defStyleRes);
                        val = new TypedValue();
                        if (a.getValue(19, val)) {
                        }
                        TypedValue typedValue222 = val;
                        boolean clickable2222 = a.getBoolean(30, clickable322);
                        Drawable drawable5222 = drawableLeft22;
                        boolean longClickable2222 = a.getBoolean(31, longClickable322);
                        a.recycle();
                        if (focusable != getFocusable()) {
                        }
                        setClickable(clickable2222);
                        setLongClickable(longClickable2222);
                        if (this.mEditor != null) {
                        }
                        if (getImportantForAccessibility() == 0) {
                        }
                        if (supportsAutoSizeText()) {
                        }
                        firstBaselineToTopHeight = firstBaselineToTopHeight2;
                        if (firstBaselineToTopHeight >= 0) {
                        }
                        lastBaselineToBottomHeight = lastBaselineToBottomHeight2;
                        if (lastBaselineToBottomHeight >= 0) {
                        }
                        lineHeight = lineHeight2;
                        if (lineHeight >= 0) {
                        }
                        IS_AMOLED = isAMOLED();
                        this.isHwTheme = IS_AMOLED ? HwWidgetFactory.checkIsHwTheme(getContext(), attributeSet2222) : false;
                        this.isSystemApp = !IS_AMOLED && (context.getApplicationInfo().isSystemApp() || context.getApplicationInfo().isUpdatedSystemApp());
                        return;
                    }
                }
                singleLine = singleLine4;
                bufferType = bufferType2;
                if (this.mEditor != null) {
                }
                if (selectallonfocus2) {
                }
                drawableTint = drawableTint2;
                if (drawableTint == null) {
                }
                if (this.mDrawables == null) {
                }
                if (drawableTint != null) {
                }
                if (drawableTintMode != null) {
                }
                PorterDuff.Mode mode22 = drawableTintMode;
                TypedArray typedArray222 = a3;
                Drawable drawableTop222 = drawableTop;
                Drawable drawableBottom222 = drawableBottom;
                Drawable drawableLeft222 = drawableLeft;
                setCompoundDrawablesWithIntrinsicBounds(drawableLeft222, drawableTop222, drawableRight, drawableBottom222);
                Drawable drawable62 = drawableTop222;
                Drawable drawable222 = drawableBottom222;
                drawableStart = drawableStart2;
                Drawable drawableEnd222 = drawableEnd;
                setRelativeDrawablesIfNeeded(drawableStart, drawableEnd222);
                setCompoundDrawablePadding(drawablePadding);
                setInputTypeSingleLine(singleLine);
                applySingleLine(singleLine, singleLine, singleLine);
                if (singleLine) {
                }
                ellipsize = ellipsize3;
                switch (ellipsize) {
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                }
                if (!password6) {
                }
                if (isPassword) {
                }
                isMonospaceEnforced = true;
                if (isMonospaceEnforced) {
                }
                applyTextAppearance(attributes);
                if (isPassword) {
                }
                if (maxlength3 >= 0) {
                }
                CharSequence text22222 = text;
                setText(text22222, bufferType);
                if (textIsSetFromXml) {
                }
                if (hint2 != null) {
                }
                if (this.mMovement == null) {
                }
                boolean clickable3222 = !canInputOrMove || isClickable();
                boolean longClickable3222 = !canInputOrMove || isLongClickable();
                int focusable22222 = getFocusable();
                boolean z222222 = canInputOrMove;
                CharSequence charSequence122222 = hint;
                BufferType bufferType32222 = bufferType;
                CharSequence charSequence132222 = text22222;
                boolean z232222 = selectallonfocus;
                int i322222 = maxlength2;
                AttributeSet attributeSet22222 = attrs;
                a = context2.obtainStyledAttributes(attributeSet22222, R.styleable.View, defStyleAttr, defStyleRes);
                val = new TypedValue();
                if (a.getValue(19, val)) {
                }
                TypedValue typedValue2222 = val;
                boolean clickable22222 = a.getBoolean(30, clickable3222);
                Drawable drawable52222 = drawableLeft222;
                boolean longClickable22222 = a.getBoolean(31, longClickable3222);
                a.recycle();
                if (focusable != getFocusable()) {
                }
                setClickable(clickable22222);
                setLongClickable(longClickable22222);
                if (this.mEditor != null) {
                }
                if (getImportantForAccessibility() == 0) {
                }
                if (supportsAutoSizeText()) {
                }
                firstBaselineToTopHeight = firstBaselineToTopHeight2;
                if (firstBaselineToTopHeight >= 0) {
                }
                lastBaselineToBottomHeight = lastBaselineToBottomHeight2;
                if (lastBaselineToBottomHeight >= 0) {
                }
                lineHeight = lineHeight2;
                if (lineHeight >= 0) {
                }
                IS_AMOLED = isAMOLED();
                this.isHwTheme = IS_AMOLED ? HwWidgetFactory.checkIsHwTheme(getContext(), attributeSet22222) : false;
                this.isSystemApp = !IS_AMOLED && (context.getApplicationInfo().isSystemApp() || context.getApplicationInfo().isUpdatedSystemApp());
                return;
            }
        }
        editable = editable4;
        singleLine2 = singleLine3;
        i5 = ellipsize2;
        buffertype2 = buffertype;
        password = password4;
        ellipsize2 = i5;
        editable4 = editable;
        singleLine3 = singleLine2;
        int i152 = buffertype2;
        password5 = password;
        buffertype3 = i152;
        i14 = i4 + 1;
        n = n2;
    }

    private boolean isAMOLED() {
        int type;
        if (isGotDisplayPanelType) {
            return IS_AMOLED;
        }
        boolean z = true;
        isGotDisplayPanelType = true;
        PowerManager manager = (PowerManager) getContext().getSystemService("power");
        if (manager == null) {
            return false;
        }
        Log.d(LOG_TAG, "get Display Panel Type is : " + type);
        if (type != 1) {
            z = false;
        }
        return z;
    }

    private void setTextInternal(CharSequence text) {
        this.mText = text;
        PrecomputedText precomputedText = null;
        this.mSpannable = text instanceof Spannable ? (Spannable) text : null;
        if (text instanceof PrecomputedText) {
            precomputedText = (PrecomputedText) text;
        }
        this.mPrecomputed = precomputedText;
    }

    public void setAutoSizeTextTypeWithDefaults(int autoSizeTextType) {
        if (supportsAutoSizeText()) {
            switch (autoSizeTextType) {
                case 0:
                    clearAutoSizeConfiguration();
                    return;
                case 1:
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    validateAndSetAutoSizeTextTypeUniformConfiguration(TypedValue.applyDimension(2, 12.0f, displayMetrics), TypedValue.applyDimension(2, 112.0f, displayMetrics), 1.0f);
                    if (setupAutoSizeText()) {
                        autoSizeText();
                        invalidate();
                        return;
                    }
                    return;
                default:
                    throw new IllegalArgumentException("Unknown auto-size text type: " + autoSizeTextType);
            }
        }
    }

    public void setAutoSizeTextTypeUniformWithConfiguration(int autoSizeMinTextSize, int autoSizeMaxTextSize, int autoSizeStepGranularity, int unit) {
        if (supportsAutoSizeText()) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            validateAndSetAutoSizeTextTypeUniformConfiguration(TypedValue.applyDimension(unit, (float) autoSizeMinTextSize, displayMetrics), TypedValue.applyDimension(unit, (float) autoSizeMaxTextSize, displayMetrics), TypedValue.applyDimension(unit, (float) autoSizeStepGranularity, displayMetrics));
            if (setupAutoSizeText()) {
                autoSizeText();
                invalidate();
            }
        }
    }

    public void setAutoSizeTextTypeUniformWithPresetSizes(int[] presetSizes, int unit) {
        if (supportsAutoSizeText()) {
            int presetSizesLength = presetSizes.length;
            if (presetSizesLength > 0) {
                int[] presetSizesInPx = new int[presetSizesLength];
                if (unit == 0) {
                    presetSizesInPx = Arrays.copyOf(presetSizes, presetSizesLength);
                } else {
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    for (int i = 0; i < presetSizesLength; i++) {
                        presetSizesInPx[i] = Math.round(TypedValue.applyDimension(unit, (float) presetSizes[i], displayMetrics));
                    }
                }
                this.mAutoSizeTextSizesInPx = cleanupAutoSizePresetSizes(presetSizesInPx);
                if (!setupAutoSizeUniformPresetSizesConfiguration()) {
                    throw new IllegalArgumentException("None of the preset sizes is valid: " + Arrays.toString(presetSizes));
                }
            } else {
                this.mHasPresetAutoSizeValues = false;
            }
            if (setupAutoSizeText()) {
                autoSizeText();
                invalidate();
            }
        }
    }

    public int getAutoSizeTextType() {
        return this.mAutoSizeTextType;
    }

    public int getAutoSizeStepGranularity() {
        return Math.round(this.mAutoSizeStepGranularityInPx);
    }

    public int getAutoSizeMinTextSize() {
        return Math.round(this.mAutoSizeMinTextSizeInPx);
    }

    public int getAutoSizeMaxTextSize() {
        return Math.round(this.mAutoSizeMaxTextSizeInPx);
    }

    public int[] getAutoSizeTextAvailableSizes() {
        return this.mAutoSizeTextSizesInPx;
    }

    private void setupAutoSizeUniformPresetSizes(TypedArray textSizes) {
        int textSizesLength = textSizes.length();
        int[] parsedSizes = new int[textSizesLength];
        if (textSizesLength > 0) {
            for (int i = 0; i < textSizesLength; i++) {
                parsedSizes[i] = textSizes.getDimensionPixelSize(i, -1);
            }
            this.mAutoSizeTextSizesInPx = cleanupAutoSizePresetSizes(parsedSizes);
            setupAutoSizeUniformPresetSizesConfiguration();
        }
    }

    private boolean setupAutoSizeUniformPresetSizesConfiguration() {
        int sizesLength = this.mAutoSizeTextSizesInPx.length;
        this.mHasPresetAutoSizeValues = sizesLength > 0;
        if (this.mHasPresetAutoSizeValues) {
            this.mAutoSizeTextType = 1;
            this.mAutoSizeMinTextSizeInPx = (float) this.mAutoSizeTextSizesInPx[0];
            this.mAutoSizeMaxTextSizeInPx = (float) this.mAutoSizeTextSizesInPx[sizesLength - 1];
            this.mAutoSizeStepGranularityInPx = -1.0f;
        }
        return this.mHasPresetAutoSizeValues;
    }

    private void validateAndSetAutoSizeTextTypeUniformConfiguration(float autoSizeMinTextSizeInPx, float autoSizeMaxTextSizeInPx, float autoSizeStepGranularityInPx) {
        if (autoSizeMinTextSizeInPx <= 0.0f) {
            throw new IllegalArgumentException("Minimum auto-size text size (" + autoSizeMinTextSizeInPx + "px) is less or equal to (0px)");
        } else if (autoSizeMaxTextSizeInPx <= autoSizeMinTextSizeInPx) {
            throw new IllegalArgumentException("Maximum auto-size text size (" + autoSizeMaxTextSizeInPx + "px) is less or equal to minimum auto-size text size (" + autoSizeMinTextSizeInPx + "px)");
        } else if (autoSizeStepGranularityInPx > 0.0f) {
            this.mAutoSizeTextType = 1;
            this.mAutoSizeMinTextSizeInPx = autoSizeMinTextSizeInPx;
            this.mAutoSizeMaxTextSizeInPx = autoSizeMaxTextSizeInPx;
            this.mAutoSizeStepGranularityInPx = autoSizeStepGranularityInPx;
            this.mHasPresetAutoSizeValues = false;
        } else {
            throw new IllegalArgumentException("The auto-size step granularity (" + autoSizeStepGranularityInPx + "px) is less or equal to (0px)");
        }
    }

    private void clearAutoSizeConfiguration() {
        this.mAutoSizeTextType = 0;
        this.mAutoSizeMinTextSizeInPx = -1.0f;
        this.mAutoSizeMaxTextSizeInPx = -1.0f;
        this.mAutoSizeStepGranularityInPx = -1.0f;
        this.mAutoSizeTextSizesInPx = EmptyArray.INT;
        this.mNeedsAutoSizeText = false;
    }

    private int[] cleanupAutoSizePresetSizes(int[] presetValues) {
        int[] iArr;
        if (presetValuesLength == 0) {
            return presetValues;
        }
        Arrays.sort(presetValues);
        IntArray uniqueValidSizes = new IntArray();
        for (int currentPresetValue : presetValues) {
            if (currentPresetValue > 0 && uniqueValidSizes.binarySearch(currentPresetValue) < 0) {
                uniqueValidSizes.add(currentPresetValue);
            }
        }
        if (presetValuesLength == uniqueValidSizes.size()) {
            iArr = presetValues;
        } else {
            iArr = uniqueValidSizes.toArray();
        }
        return iArr;
    }

    private boolean setupAutoSizeText() {
        if (!supportsAutoSizeText() || this.mAutoSizeTextType != 1) {
            this.mNeedsAutoSizeText = false;
        } else {
            if (!this.mHasPresetAutoSizeValues || this.mAutoSizeTextSizesInPx.length == 0) {
                int autoSizeValuesLength = ((int) Math.floor((double) ((this.mAutoSizeMaxTextSizeInPx - this.mAutoSizeMinTextSizeInPx) / this.mAutoSizeStepGranularityInPx))) + 1;
                int[] autoSizeTextSizesInPx = new int[autoSizeValuesLength];
                for (int i = 0; i < autoSizeValuesLength; i++) {
                    autoSizeTextSizesInPx[i] = Math.round(this.mAutoSizeMinTextSizeInPx + (((float) i) * this.mAutoSizeStepGranularityInPx));
                }
                this.mAutoSizeTextSizesInPx = cleanupAutoSizePresetSizes(autoSizeTextSizesInPx);
            }
            this.mNeedsAutoSizeText = true;
        }
        return this.mNeedsAutoSizeText;
    }

    private int[] parseDimensionArray(TypedArray dimens) {
        if (dimens == null) {
            return null;
        }
        int[] result = new int[dimens.length()];
        for (int i = 0; i < result.length; i++) {
            result[i] = dimens.getDimensionPixelSize(i, 0);
        }
        return result;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 100) {
            return;
        }
        if (resultCode == -1 && data != null) {
            CharSequence result = data.getCharSequenceExtra("android.intent.extra.PROCESS_TEXT");
            if (result == null) {
                return;
            }
            if (isTextEditable()) {
                replaceSelectionWithText(result);
                if (this.mEditor != null) {
                    this.mEditor.refreshTextActionMode();
                }
            } else if (result.length() > 0) {
                Toast.makeText(getContext(), (CharSequence) String.valueOf(result), 1).show();
            }
        } else if (this.mSpannable != null) {
            Selection.setSelection(this.mSpannable, getSelectionEnd());
        }
    }

    private void setTypefaceFromAttrs(Typeface typeface, String familyName, int typefaceIndex, int style, int weight) {
        if (typeface == null && familyName != null) {
            resolveStyleAndSetTypeface(Typeface.create(familyName, 0), style, weight);
        } else if (typeface != null) {
            resolveStyleAndSetTypeface(typeface, style, weight);
        } else {
            switch (typefaceIndex) {
                case 1:
                    resolveStyleAndSetTypeface(Typeface.SANS_SERIF, style, weight);
                    return;
                case 2:
                    resolveStyleAndSetTypeface(Typeface.SERIF, style, weight);
                    return;
                case 3:
                    resolveStyleAndSetTypeface(Typeface.MONOSPACE, style, weight);
                    return;
                default:
                    resolveStyleAndSetTypeface(null, style, weight);
                    return;
            }
        }
    }

    private void resolveStyleAndSetTypeface(Typeface typeface, int style, int weight) {
        if (weight >= 0) {
            setTypeface(Typeface.create(typeface, Math.min(1000, weight), (style & 2) != 0));
        } else {
            setTypeface(typeface, style);
        }
    }

    private void setRelativeDrawablesIfNeeded(Drawable start, Drawable end) {
        if ((start == null && end == null) ? false : true) {
            Drawables dr = this.mDrawables;
            if (dr == null) {
                Drawables drawables = new Drawables(getContext());
                dr = drawables;
                this.mDrawables = drawables;
            }
            this.mDrawables.mOverride = true;
            Rect compoundRect = dr.mCompoundRect;
            int[] state = getDrawableState();
            if (start != null) {
                start.setBounds(0, 0, start.getIntrinsicWidth(), start.getIntrinsicHeight());
                start.setState(state);
                start.copyBounds(compoundRect);
                start.setCallback(this);
                dr.mDrawableStart = start;
                dr.mDrawableSizeStart = compoundRect.width();
                dr.mDrawableHeightStart = compoundRect.height();
            } else {
                dr.mDrawableHeightStart = 0;
                dr.mDrawableSizeStart = 0;
            }
            if (end != null) {
                end.setBounds(0, 0, end.getIntrinsicWidth(), end.getIntrinsicHeight());
                end.setState(state);
                end.copyBounds(compoundRect);
                end.setCallback(this);
                dr.mDrawableEnd = end;
                dr.mDrawableSizeEnd = compoundRect.width();
                dr.mDrawableHeightEnd = compoundRect.height();
            } else {
                dr.mDrawableHeightEnd = 0;
                dr.mDrawableSizeEnd = 0;
            }
            resetResolvedDrawables();
            resolveDrawables();
            applyCompoundDrawableTint();
        }
    }

    @RemotableViewMethod
    public void setEnabled(boolean enabled) {
        if (enabled != isEnabled()) {
            if (!enabled) {
                InputMethodManager imm = InputMethodManager.peekInstance();
                if (imm != null && imm.isActive(this)) {
                    imm.hideSoftInputFromWindow(getWindowToken(), 0);
                }
            }
            super.setEnabled(enabled);
            if (enabled) {
                InputMethodManager imm2 = InputMethodManager.peekInstance();
                if (imm2 != null) {
                    imm2.restartInput(this);
                }
            }
            if (this.mEditor != null) {
                this.mEditor.invalidateTextDisplayList();
                this.mEditor.prepareCursorControllers();
                this.mEditor.makeBlink();
            }
        }
    }

    public void setTypeface(Typeface tf, int style) {
        Typeface tf2;
        float f = 0.0f;
        boolean z = false;
        if (style > 0) {
            if (tf == null) {
                tf2 = Typeface.defaultFromStyle(style);
            } else {
                tf2 = Typeface.create(tf, style);
            }
            setTypeface(tf2);
            int need = (~(tf2 != null ? tf2.getStyle() : 0)) & style;
            TextPaint textPaint = this.mTextPaint;
            if ((need & 1) != 0) {
                z = true;
            }
            textPaint.setFakeBoldText(z);
            TextPaint textPaint2 = this.mTextPaint;
            if ((need & 2) != 0) {
                f = -0.25f;
            }
            textPaint2.setTextSkewX(f);
            return;
        }
        this.mTextPaint.setFakeBoldText(false);
        this.mTextPaint.setTextSkewX(0.0f);
        setTypeface(tf);
    }

    /* access modifiers changed from: protected */
    public boolean getDefaultEditable() {
        return false;
    }

    /* access modifiers changed from: protected */
    public MovementMethod getDefaultMovementMethod() {
        return null;
    }

    @ViewDebug.CapturedViewProperty
    public CharSequence getText() {
        return this.mText;
    }

    public int length() {
        return this.mText.length();
    }

    public Editable getEditableText() {
        if (this.mText instanceof Editable) {
            return (Editable) this.mText;
        }
        return null;
    }

    public int getLineHeight() {
        return FastMath.round((((float) this.mTextPaint.getFontMetricsInt(null)) * this.mSpacingMult) + this.mSpacingAdd);
    }

    public final Layout getLayout() {
        return this.mLayout;
    }

    /* access modifiers changed from: package-private */
    public final Layout getHintLayout() {
        return this.mHintLayout;
    }

    public final UndoManager getUndoManager() {
        throw new UnsupportedOperationException("not implemented");
    }

    @VisibleForTesting
    public final Editor getEditorForTesting() {
        return this.mEditor;
    }

    public final void setUndoManager(UndoManager undoManager, String tag) {
        throw new UnsupportedOperationException("not implemented");
    }

    public final KeyListener getKeyListener() {
        if (this.mEditor == null) {
            return null;
        }
        return this.mEditor.mKeyListener;
    }

    public void setKeyListener(KeyListener input) {
        this.mListenerChanged = true;
        setKeyListenerOnly(input);
        fixFocusableAndClickableSettings();
        if (input != null) {
            createEditorIfNeeded();
            setInputTypeFromEditor();
        } else if (this.mEditor != null) {
            this.mEditor.mInputType = 0;
        }
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (imm != null) {
            imm.restartInput(this);
        }
    }

    private void setInputTypeFromEditor() {
        try {
            this.mEditor.mInputType = this.mEditor.mKeyListener.getInputType();
        } catch (IncompatibleClassChangeError e) {
            this.mEditor.mInputType = 1;
        }
        setInputTypeSingleLine(this.mSingleLine);
    }

    private void setKeyListenerOnly(KeyListener input) {
        if (this.mEditor != null || input != null) {
            createEditorIfNeeded();
            if (this.mEditor.mKeyListener != input) {
                this.mEditor.mKeyListener = input;
                if (input != null && !(this.mText instanceof Editable)) {
                    setText(this.mText);
                }
                setFilters((Editable) this.mText, this.mFilters);
            }
        }
    }

    public final MovementMethod getMovementMethod() {
        return this.mMovement;
    }

    public final void setMovementMethod(MovementMethod movement) {
        if (this.mMovement != movement) {
            this.mMovement = movement;
            if (movement != null && this.mSpannable == null) {
                setText(this.mText);
            }
            fixFocusableAndClickableSettings();
            if (this.mEditor != null) {
                this.mEditor.prepareCursorControllers();
            }
        }
    }

    private void fixFocusableAndClickableSettings() {
        if (this.mMovement == null && (this.mEditor == null || this.mEditor.mKeyListener == null)) {
            setFocusable(16);
            setClickable(false);
            setLongClickable(false);
            return;
        }
        setFocusable(1);
        setClickable(true);
        setLongClickable(true);
    }

    public final TransformationMethod getTransformationMethod() {
        return this.mTransformation;
    }

    public final void setTransformationMethod(TransformationMethod method) {
        if (method != this.mTransformation) {
            if (!(this.mTransformation == null || this.mSpannable == null)) {
                this.mSpannable.removeSpan(this.mTransformation);
            }
            this.mTransformation = method;
            if (method instanceof TransformationMethod2) {
                TransformationMethod2 method2 = (TransformationMethod2) method;
                this.mAllowTransformationLengthChange = !isTextSelectable() && !(this.mText instanceof Editable);
                method2.setLengthChangesAllowed(this.mAllowTransformationLengthChange);
            } else {
                this.mAllowTransformationLengthChange = false;
            }
            setText(this.mText);
            if (hasPasswordTransformationMethod()) {
                notifyViewAccessibilityStateChangedIfNeeded(0);
            }
            this.mTextDir = getTextDirectionHeuristic();
        }
    }

    public int getCompoundPaddingTop() {
        Drawables dr = this.mDrawables;
        if (dr == null || dr.mShowing[1] == null) {
            return this.mPaddingTop;
        }
        return this.mPaddingTop + dr.mDrawablePadding + dr.mDrawableSizeTop;
    }

    public int getCompoundPaddingBottom() {
        Drawables dr = this.mDrawables;
        if (dr == null || dr.mShowing[3] == null) {
            return this.mPaddingBottom;
        }
        return this.mPaddingBottom + dr.mDrawablePadding + dr.mDrawableSizeBottom;
    }

    public int getCompoundPaddingLeft() {
        Drawables dr = this.mDrawables;
        if (dr == null || dr.mShowing[0] == null) {
            return this.mPaddingLeft;
        }
        return this.mPaddingLeft + dr.mDrawablePadding + dr.mDrawableSizeLeft;
    }

    public int getCompoundPaddingRight() {
        Drawables dr = this.mDrawables;
        if (dr == null || dr.mShowing[2] == null) {
            return this.mPaddingRight;
        }
        return this.mPaddingRight + dr.mDrawablePadding + dr.mDrawableSizeRight;
    }

    public int getCompoundPaddingStart() {
        resolveDrawables();
        if (getLayoutDirection() != 1) {
            return getCompoundPaddingLeft();
        }
        return getCompoundPaddingRight();
    }

    public int getCompoundPaddingEnd() {
        resolveDrawables();
        if (getLayoutDirection() != 1) {
            return getCompoundPaddingRight();
        }
        return getCompoundPaddingLeft();
    }

    public int getExtendedPaddingTop() {
        if (this.mMaxMode != 1) {
            return getCompoundPaddingTop();
        }
        if (this.mLayout == null) {
            assumeLayout();
        }
        if (this.mLayout.getLineCount() <= this.mMaximum) {
            return getCompoundPaddingTop();
        }
        int top = getCompoundPaddingTop();
        int viewht = (getHeight() - top) - getCompoundPaddingBottom();
        int layoutht = this.mLayout.getLineTop(this.mMaximum);
        if (layoutht >= viewht) {
            return top;
        }
        int gravity = this.mGravity & 112;
        if (gravity == 48) {
            return top;
        }
        if (gravity == 80) {
            return (top + viewht) - layoutht;
        }
        return ((viewht - layoutht) / 2) + top;
    }

    public int getExtendedPaddingBottom() {
        if (this.mMaxMode != 1) {
            return getCompoundPaddingBottom();
        }
        if (this.mLayout == null) {
            assumeLayout();
        }
        if (this.mLayout.getLineCount() <= this.mMaximum) {
            return getCompoundPaddingBottom();
        }
        int top = getCompoundPaddingTop();
        int bottom = getCompoundPaddingBottom();
        int viewht = (getHeight() - top) - bottom;
        int layoutht = this.mLayout.getLineTop(this.mMaximum);
        if (layoutht >= viewht) {
            return bottom;
        }
        int gravity = this.mGravity & 112;
        if (gravity == 48) {
            return (bottom + viewht) - layoutht;
        }
        if (gravity == 80) {
            return bottom;
        }
        return ((viewht - layoutht) / 2) + bottom;
    }

    public int getTotalPaddingLeft() {
        return getCompoundPaddingLeft();
    }

    public int getTotalPaddingRight() {
        return getCompoundPaddingRight();
    }

    public int getTotalPaddingStart() {
        return getCompoundPaddingStart();
    }

    public int getTotalPaddingEnd() {
        return getCompoundPaddingEnd();
    }

    public int getTotalPaddingTop() {
        return getExtendedPaddingTop() + getVerticalOffset(true);
    }

    public int getTotalPaddingBottom() {
        return getExtendedPaddingBottom() + getBottomVerticalOffset(true);
    }

    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        Drawables dr = this.mDrawables;
        if (dr != null) {
            if (dr.mDrawableStart != null) {
                dr.mDrawableStart.setCallback(null);
            }
            dr.mDrawableStart = null;
            if (dr.mDrawableEnd != null) {
                dr.mDrawableEnd.setCallback(null);
            }
            dr.mDrawableEnd = null;
            dr.mDrawableHeightStart = 0;
            dr.mDrawableSizeStart = 0;
            dr.mDrawableHeightEnd = 0;
            dr.mDrawableSizeEnd = 0;
        }
        if ((left == null && top == null && right == null && bottom == null) ? false : true) {
            if (dr == null) {
                Drawables drawables = new Drawables(getContext());
                dr = drawables;
                this.mDrawables = drawables;
            }
            this.mDrawables.mOverride = false;
            if (!(dr.mShowing[0] == left || dr.mShowing[0] == null)) {
                dr.mShowing[0].setCallback(null);
            }
            dr.mShowing[0] = left;
            if (!(dr.mShowing[1] == top || dr.mShowing[1] == null)) {
                dr.mShowing[1].setCallback(null);
            }
            dr.mShowing[1] = top;
            if (!(dr.mShowing[2] == right || dr.mShowing[2] == null)) {
                dr.mShowing[2].setCallback(null);
            }
            dr.mShowing[2] = right;
            if (!(dr.mShowing[3] == bottom || dr.mShowing[3] == null)) {
                dr.mShowing[3].setCallback(null);
            }
            dr.mShowing[3] = bottom;
            Rect compoundRect = dr.mCompoundRect;
            int[] state = getDrawableState();
            if (left != null) {
                left.setState(state);
                left.copyBounds(compoundRect);
                left.setCallback(this);
                dr.mDrawableSizeLeft = compoundRect.width();
                dr.mDrawableHeightLeft = compoundRect.height();
            } else {
                dr.mDrawableHeightLeft = 0;
                dr.mDrawableSizeLeft = 0;
            }
            if (right != null) {
                right.setState(state);
                right.copyBounds(compoundRect);
                right.setCallback(this);
                dr.mDrawableSizeRight = compoundRect.width();
                dr.mDrawableHeightRight = compoundRect.height();
            } else {
                dr.mDrawableHeightRight = 0;
                dr.mDrawableSizeRight = 0;
            }
            if (top != null) {
                top.setState(state);
                top.copyBounds(compoundRect);
                top.setCallback(this);
                dr.mDrawableSizeTop = compoundRect.height();
                dr.mDrawableWidthTop = compoundRect.width();
            } else {
                dr.mDrawableWidthTop = 0;
                dr.mDrawableSizeTop = 0;
            }
            if (bottom != null) {
                bottom.setState(state);
                bottom.copyBounds(compoundRect);
                bottom.setCallback(this);
                dr.mDrawableSizeBottom = compoundRect.height();
                dr.mDrawableWidthBottom = compoundRect.width();
            } else {
                dr.mDrawableWidthBottom = 0;
                dr.mDrawableSizeBottom = 0;
            }
        } else if (dr != null) {
            if (!dr.hasMetadata()) {
                this.mDrawables = null;
            } else {
                int i = dr.mShowing.length - 1;
                while (true) {
                    int i2 = i;
                    if (i2 < 0) {
                        break;
                    }
                    if (dr.mShowing[i2] != null) {
                        dr.mShowing[i2].setCallback(null);
                    }
                    dr.mShowing[i2] = null;
                    i = i2 - 1;
                }
                dr.mDrawableHeightLeft = 0;
                dr.mDrawableSizeLeft = 0;
                dr.mDrawableHeightRight = 0;
                dr.mDrawableSizeRight = 0;
                dr.mDrawableWidthTop = 0;
                dr.mDrawableSizeTop = 0;
                dr.mDrawableWidthBottom = 0;
                dr.mDrawableSizeBottom = 0;
            }
        }
        if (dr != null) {
            dr.mDrawableLeftInitial = left;
            dr.mDrawableRightInitial = right;
        }
        resetResolvedDrawables();
        resolveDrawables();
        applyCompoundDrawableTint();
        invalidate();
        requestLayout();
    }

    @RemotableViewMethod
    public void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom) {
        Drawable drawable;
        Drawable drawable2;
        Drawable drawable3;
        Context context = getContext();
        Drawable drawable4 = null;
        if (left != 0) {
            drawable = context.getDrawable(left);
        } else {
            drawable = null;
        }
        if (top != 0) {
            drawable2 = context.getDrawable(top);
        } else {
            drawable2 = null;
        }
        if (right != 0) {
            drawable3 = context.getDrawable(right);
        } else {
            drawable3 = null;
        }
        if (bottom != 0) {
            drawable4 = context.getDrawable(bottom);
        }
        setCompoundDrawablesWithIntrinsicBounds(drawable, drawable2, drawable3, drawable4);
    }

    @RemotableViewMethod
    public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        if (left != null) {
            left.setBounds(0, 0, left.getIntrinsicWidth(), left.getIntrinsicHeight());
        }
        if (right != null) {
            right.setBounds(0, 0, right.getIntrinsicWidth(), right.getIntrinsicHeight());
        }
        if (top != null) {
            top.setBounds(0, 0, top.getIntrinsicWidth(), top.getIntrinsicHeight());
        }
        if (bottom != null) {
            bottom.setBounds(0, 0, bottom.getIntrinsicWidth(), bottom.getIntrinsicHeight());
        }
        setCompoundDrawables(left, top, right, bottom);
    }

    @RemotableViewMethod
    public void setCompoundDrawablesRelative(Drawable start, Drawable top, Drawable end, Drawable bottom) {
        Drawables dr = this.mDrawables;
        if (dr != null) {
            if (dr.mShowing[0] != null) {
                dr.mShowing[0].setCallback(null);
            }
            Drawable[] drawableArr = dr.mShowing;
            dr.mDrawableLeftInitial = null;
            drawableArr[0] = null;
            if (dr.mShowing[2] != null) {
                dr.mShowing[2].setCallback(null);
            }
            Drawable[] drawableArr2 = dr.mShowing;
            dr.mDrawableRightInitial = null;
            drawableArr2[2] = null;
            dr.mDrawableHeightLeft = 0;
            dr.mDrawableSizeLeft = 0;
            dr.mDrawableHeightRight = 0;
            dr.mDrawableSizeRight = 0;
        }
        if ((start == null && top == null && end == null && bottom == null) ? false : true) {
            if (dr == null) {
                Drawables drawables = new Drawables(getContext());
                dr = drawables;
                this.mDrawables = drawables;
            }
            this.mDrawables.mOverride = true;
            if (!(dr.mDrawableStart == start || dr.mDrawableStart == null)) {
                dr.mDrawableStart.setCallback(null);
            }
            dr.mDrawableStart = start;
            if (!(dr.mShowing[1] == top || dr.mShowing[1] == null)) {
                dr.mShowing[1].setCallback(null);
            }
            dr.mShowing[1] = top;
            if (!(dr.mDrawableEnd == end || dr.mDrawableEnd == null)) {
                dr.mDrawableEnd.setCallback(null);
            }
            dr.mDrawableEnd = end;
            if (!(dr.mShowing[3] == bottom || dr.mShowing[3] == null)) {
                dr.mShowing[3].setCallback(null);
            }
            dr.mShowing[3] = bottom;
            Rect compoundRect = dr.mCompoundRect;
            int[] state = getDrawableState();
            if (start != null) {
                start.setState(state);
                start.copyBounds(compoundRect);
                start.setCallback(this);
                dr.mDrawableSizeStart = compoundRect.width();
                dr.mDrawableHeightStart = compoundRect.height();
            } else {
                dr.mDrawableHeightStart = 0;
                dr.mDrawableSizeStart = 0;
            }
            if (end != null) {
                end.setState(state);
                end.copyBounds(compoundRect);
                end.setCallback(this);
                dr.mDrawableSizeEnd = compoundRect.width();
                dr.mDrawableHeightEnd = compoundRect.height();
            } else {
                dr.mDrawableHeightEnd = 0;
                dr.mDrawableSizeEnd = 0;
            }
            if (top != null) {
                top.setState(state);
                top.copyBounds(compoundRect);
                top.setCallback(this);
                dr.mDrawableSizeTop = compoundRect.height();
                dr.mDrawableWidthTop = compoundRect.width();
            } else {
                dr.mDrawableWidthTop = 0;
                dr.mDrawableSizeTop = 0;
            }
            if (bottom != null) {
                bottom.setState(state);
                bottom.copyBounds(compoundRect);
                bottom.setCallback(this);
                dr.mDrawableSizeBottom = compoundRect.height();
                dr.mDrawableWidthBottom = compoundRect.width();
            } else {
                dr.mDrawableWidthBottom = 0;
                dr.mDrawableSizeBottom = 0;
            }
        } else if (dr != null) {
            if (!dr.hasMetadata()) {
                this.mDrawables = null;
            } else {
                if (dr.mDrawableStart != null) {
                    dr.mDrawableStart.setCallback(null);
                }
                dr.mDrawableStart = null;
                if (dr.mShowing[1] != null) {
                    dr.mShowing[1].setCallback(null);
                }
                dr.mShowing[1] = null;
                if (dr.mDrawableEnd != null) {
                    dr.mDrawableEnd.setCallback(null);
                }
                dr.mDrawableEnd = null;
                if (dr.mShowing[3] != null) {
                    dr.mShowing[3].setCallback(null);
                }
                dr.mShowing[3] = null;
                dr.mDrawableHeightStart = 0;
                dr.mDrawableSizeStart = 0;
                dr.mDrawableHeightEnd = 0;
                dr.mDrawableSizeEnd = 0;
                dr.mDrawableWidthTop = 0;
                dr.mDrawableSizeTop = 0;
                dr.mDrawableWidthBottom = 0;
                dr.mDrawableSizeBottom = 0;
            }
        }
        resetResolvedDrawables();
        resolveDrawables();
        invalidate();
        requestLayout();
    }

    @RemotableViewMethod
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(int start, int top, int end, int bottom) {
        Drawable drawable;
        Drawable drawable2;
        Drawable drawable3;
        Context context = getContext();
        Drawable drawable4 = null;
        if (start != 0) {
            drawable = context.getDrawable(start);
        } else {
            drawable = null;
        }
        if (top != 0) {
            drawable2 = context.getDrawable(top);
        } else {
            drawable2 = null;
        }
        if (end != 0) {
            drawable3 = context.getDrawable(end);
        } else {
            drawable3 = null;
        }
        if (bottom != 0) {
            drawable4 = context.getDrawable(bottom);
        }
        setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, drawable2, drawable3, drawable4);
    }

    @RemotableViewMethod
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(Drawable start, Drawable top, Drawable end, Drawable bottom) {
        if (start != null) {
            start.setBounds(0, 0, start.getIntrinsicWidth(), start.getIntrinsicHeight());
        }
        if (end != null) {
            end.setBounds(0, 0, end.getIntrinsicWidth(), end.getIntrinsicHeight());
        }
        if (top != null) {
            top.setBounds(0, 0, top.getIntrinsicWidth(), top.getIntrinsicHeight());
        }
        if (bottom != null) {
            bottom.setBounds(0, 0, bottom.getIntrinsicWidth(), bottom.getIntrinsicHeight());
        }
        setCompoundDrawablesRelative(start, top, end, bottom);
    }

    public Drawable[] getCompoundDrawables() {
        Drawables dr = this.mDrawables;
        if (dr != null) {
            return (Drawable[]) dr.mShowing.clone();
        }
        return new Drawable[]{null, null, null, null};
    }

    public Drawable[] getCompoundDrawablesRelative() {
        Drawables dr = this.mDrawables;
        if (dr != null) {
            return new Drawable[]{dr.mDrawableStart, dr.mShowing[1], dr.mDrawableEnd, dr.mShowing[3]};
        }
        return new Drawable[]{null, null, null, null};
    }

    @RemotableViewMethod
    public void setCompoundDrawablePadding(int pad) {
        Drawables dr = this.mDrawables;
        if (pad != 0) {
            if (dr == null) {
                Drawables drawables = new Drawables(getContext());
                dr = drawables;
                this.mDrawables = drawables;
            }
            dr.mDrawablePadding = pad;
        } else if (dr != null) {
            dr.mDrawablePadding = pad;
        }
        invalidate();
        requestLayout();
    }

    public int getCompoundDrawablePadding() {
        Drawables dr = this.mDrawables;
        if (dr != null) {
            return dr.mDrawablePadding;
        }
        return 0;
    }

    public void setCompoundDrawableTintList(ColorStateList tint) {
        if (this.mDrawables == null) {
            this.mDrawables = new Drawables(getContext());
        }
        this.mDrawables.mTintList = tint;
        this.mDrawables.mHasTint = true;
        applyCompoundDrawableTint();
    }

    public ColorStateList getCompoundDrawableTintList() {
        if (this.mDrawables != null) {
            return this.mDrawables.mTintList;
        }
        return null;
    }

    public void setCompoundDrawableTintMode(PorterDuff.Mode tintMode) {
        if (this.mDrawables == null) {
            this.mDrawables = new Drawables(getContext());
        }
        this.mDrawables.mTintMode = tintMode;
        this.mDrawables.mHasTintMode = true;
        applyCompoundDrawableTint();
    }

    public PorterDuff.Mode getCompoundDrawableTintMode() {
        if (this.mDrawables != null) {
            return this.mDrawables.mTintMode;
        }
        return null;
    }

    private void applyCompoundDrawableTint() {
        if (this.mDrawables != null) {
            if (this.mDrawables.mHasTint || this.mDrawables.mHasTintMode) {
                ColorStateList tintList = this.mDrawables.mTintList;
                PorterDuff.Mode tintMode = this.mDrawables.mTintMode;
                boolean hasTint = this.mDrawables.mHasTint;
                boolean hasTintMode = this.mDrawables.mHasTintMode;
                int[] state = getDrawableState();
                for (Drawable dr : this.mDrawables.mShowing) {
                    if (!(dr == null || dr == this.mDrawables.mDrawableError)) {
                        dr.mutate();
                        if (hasTint) {
                            dr.setTintList(tintList);
                        }
                        if (hasTintMode) {
                            dr.setTintMode(tintMode);
                        }
                        if (dr.isStateful()) {
                            dr.setState(state);
                        }
                    }
                }
            }
        }
    }

    public void setPadding(int left, int top, int right, int bottom) {
        if (!(left == this.mPaddingLeft && right == this.mPaddingRight && top == this.mPaddingTop && bottom == this.mPaddingBottom)) {
            nullLayouts();
        }
        super.setPadding(left, top, right, bottom);
        invalidate();
    }

    public void setPaddingRelative(int start, int top, int end, int bottom) {
        if (!(start == getPaddingStart() && end == getPaddingEnd() && top == this.mPaddingTop && bottom == this.mPaddingBottom)) {
            nullLayouts();
        }
        super.setPaddingRelative(start, top, end, bottom);
        invalidate();
    }

    public void setFirstBaselineToTopHeight(int firstBaselineToTopHeight) {
        int fontMetricsTop;
        Preconditions.checkArgumentNonnegative(firstBaselineToTopHeight);
        Paint.FontMetricsInt fontMetrics = getPaint().getFontMetricsInt();
        if (getIncludeFontPadding()) {
            fontMetricsTop = fontMetrics.top;
        } else {
            fontMetricsTop = fontMetrics.ascent;
        }
        if (firstBaselineToTopHeight > Math.abs(fontMetricsTop)) {
            setPadding(getPaddingLeft(), firstBaselineToTopHeight - (-fontMetricsTop), getPaddingRight(), getPaddingBottom());
        }
    }

    public void setLastBaselineToBottomHeight(int lastBaselineToBottomHeight) {
        int fontMetricsBottom;
        Preconditions.checkArgumentNonnegative(lastBaselineToBottomHeight);
        Paint.FontMetricsInt fontMetrics = getPaint().getFontMetricsInt();
        if (getIncludeFontPadding()) {
            fontMetricsBottom = fontMetrics.bottom;
        } else {
            fontMetricsBottom = fontMetrics.descent;
        }
        if (lastBaselineToBottomHeight > Math.abs(fontMetricsBottom)) {
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), lastBaselineToBottomHeight - fontMetricsBottom);
        }
    }

    public int getFirstBaselineToTopHeight() {
        return getPaddingTop() - getPaint().getFontMetricsInt().top;
    }

    public int getLastBaselineToBottomHeight() {
        return getPaddingBottom() + getPaint().getFontMetricsInt().bottom;
    }

    public final int getAutoLinkMask() {
        return this.mAutoLinkMask;
    }

    public void setTextAppearance(int resId) {
        setTextAppearance(this.mContext, resId);
    }

    @Deprecated
    public void setTextAppearance(Context context, int resId) {
        TypedArray ta = context.obtainStyledAttributes(resId, android.R.styleable.TextAppearance);
        TextAppearanceAttributes attributes = new TextAppearanceAttributes();
        readTextAppearance(context, ta, attributes, false);
        ta.recycle();
        applyTextAppearance(attributes);
    }

    private void readTextAppearance(Context context, TypedArray appearance, TextAppearanceAttributes attributes, boolean styleArray) {
        int n = appearance.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = appearance.getIndex(i);
            int index = attr;
            if (styleArray) {
                index = sAppearanceValues.get(attr, -1);
                if (index == -1) {
                }
            }
            switch (index) {
                case 0:
                    attributes.mTextSize = appearance.getDimensionPixelSize(attr, attributes.mTextSize);
                    break;
                case 1:
                    attributes.mTypefaceIndex = appearance.getInt(attr, attributes.mTypefaceIndex);
                    if (attributes.mTypefaceIndex != -1 && !attributes.mFontFamilyExplicit) {
                        attributes.mFontFamily = null;
                        break;
                    }
                case 2:
                    attributes.mStyleIndex = appearance.getInt(attr, attributes.mStyleIndex);
                    break;
                case 3:
                    attributes.mTextColor = appearance.getColorStateList(attr);
                    break;
                case 4:
                    attributes.mTextColorHighlight = appearance.getColor(attr, attributes.mTextColorHighlight);
                    break;
                case 5:
                    attributes.mTextColorHint = appearance.getColorStateList(attr);
                    break;
                case 6:
                    attributes.mTextColorLink = appearance.getColorStateList(attr);
                    break;
                case 7:
                    attributes.mShadowColor = appearance.getInt(attr, attributes.mShadowColor);
                    break;
                case 8:
                    attributes.mShadowDx = appearance.getFloat(attr, attributes.mShadowDx);
                    break;
                case 9:
                    attributes.mShadowDy = appearance.getFloat(attr, attributes.mShadowDy);
                    break;
                case 10:
                    attributes.mShadowRadius = appearance.getFloat(attr, attributes.mShadowRadius);
                    break;
                case 11:
                    attributes.mAllCaps = appearance.getBoolean(attr, attributes.mAllCaps);
                    break;
                case 12:
                    if (!context.isRestricted() && context.canLoadUnsafeResources()) {
                        try {
                            attributes.mFontTypeface = appearance.getFont(attr);
                        } catch (Resources.NotFoundException | UnsupportedOperationException e) {
                        }
                    }
                    if (attributes.mFontTypeface == null) {
                        attributes.mFontFamily = appearance.getString(attr);
                    }
                    attributes.mFontFamilyExplicit = true;
                    break;
                case 13:
                    attributes.mHasElegant = true;
                    attributes.mElegant = appearance.getBoolean(attr, attributes.mElegant);
                    break;
                case 14:
                    attributes.mHasLetterSpacing = true;
                    attributes.mLetterSpacing = appearance.getFloat(attr, attributes.mLetterSpacing);
                    break;
                case 15:
                    attributes.mFontFeatureSettings = appearance.getString(attr);
                    break;
                case 16:
                    attributes.mHasFallbackLineSpacing = true;
                    attributes.mFallbackLineSpacing = appearance.getBoolean(attr, attributes.mFallbackLineSpacing);
                    break;
                case 17:
                    attributes.mFontWeight = appearance.getInt(attr, attributes.mFontWeight);
                    break;
            }
        }
    }

    private void applyTextAppearance(TextAppearanceAttributes attributes) {
        if (attributes.mTextColor != null) {
            setTextColor(attributes.mTextColor);
        }
        if (attributes.mTextColorHint != null) {
            setHintTextColor(attributes.mTextColorHint);
        }
        if (attributes.mTextColorLink != null) {
            setLinkTextColor(attributes.mTextColorLink);
        }
        if (attributes.mTextColorHighlight != 0) {
            setHighlightColor(attributes.mTextColorHighlight);
        }
        if (attributes.mTextSize != 0) {
            setRawTextSize((float) attributes.mTextSize, true);
        }
        if (attributes.mTypefaceIndex != -1 && !attributes.mFontFamilyExplicit) {
            attributes.mFontFamily = null;
        }
        setTypefaceFromAttrs(attributes.mFontTypeface, attributes.mFontFamily, attributes.mTypefaceIndex, attributes.mStyleIndex, attributes.mFontWeight);
        if (attributes.mShadowColor != 0) {
            setShadowLayer(attributes.mShadowRadius, attributes.mShadowDx, attributes.mShadowDy, attributes.mShadowColor);
        }
        if (attributes.mAllCaps) {
            setTransformationMethod(new AllCapsTransformationMethod(getContext()));
        }
        if (attributes.mHasElegant) {
            setElegantTextHeight(attributes.mElegant);
        }
        if (attributes.mHasFallbackLineSpacing) {
            setFallbackLineSpacing(attributes.mFallbackLineSpacing);
        }
        if (attributes.mHasLetterSpacing) {
            setLetterSpacing(attributes.mLetterSpacing);
        }
        if (attributes.mFontFeatureSettings != null) {
            setFontFeatureSettings(attributes.mFontFeatureSettings);
        }
    }

    public Locale getTextLocale() {
        return this.mTextPaint.getTextLocale();
    }

    public LocaleList getTextLocales() {
        return this.mTextPaint.getTextLocales();
    }

    private void changeListenerLocaleTo(Locale locale) {
        KeyListener listener;
        if (!this.mListenerChanged && this.mEditor != null) {
            KeyListener listener2 = this.mEditor.mKeyListener;
            if (listener2 instanceof DigitsKeyListener) {
                listener = DigitsKeyListener.getInstance(locale, (DigitsKeyListener) listener2);
            } else if (listener2 instanceof DateKeyListener) {
                listener = DateKeyListener.getInstance(locale);
            } else if (listener2 instanceof TimeKeyListener) {
                listener = TimeKeyListener.getInstance(locale);
            } else if (listener2 instanceof DateTimeKeyListener) {
                listener = DateTimeKeyListener.getInstance(locale);
            } else {
                return;
            }
            boolean wasPasswordType = isPasswordInputType(this.mEditor.mInputType);
            setKeyListenerOnly(listener);
            setInputTypeFromEditor();
            if (wasPasswordType) {
                int newInputClass = this.mEditor.mInputType & 15;
                if (newInputClass == 1) {
                    this.mEditor.mInputType |= 128;
                } else if (newInputClass == 2) {
                    this.mEditor.mInputType |= 16;
                }
            }
        }
    }

    public void setTextLocale(Locale locale) {
        this.mLocalesChanged = true;
        this.mTextPaint.setTextLocale(locale);
        if (this.mLayout != null) {
            nullLayouts();
            requestLayout();
            invalidate();
        }
    }

    public void setTextLocales(LocaleList locales) {
        this.mLocalesChanged = true;
        this.mTextPaint.setTextLocales(locales);
        if (this.mLayout != null) {
            nullLayouts();
            requestLayout();
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!this.mLocalesChanged) {
            this.mTextPaint.setTextLocales(LocaleList.getDefault());
            if (this.mLayout != null) {
                nullLayouts();
                requestLayout();
                invalidate();
            }
        }
    }

    @ViewDebug.ExportedProperty(category = "text")
    public float getTextSize() {
        return this.mTextPaint.getTextSize();
    }

    @ViewDebug.ExportedProperty(category = "text")
    public float getScaledTextSize() {
        return this.mTextPaint.getTextSize() / this.mTextPaint.density;
    }

    @ViewDebug.ExportedProperty(category = "text", mapping = {@ViewDebug.IntToString(from = 0, to = "NORMAL"), @ViewDebug.IntToString(from = 1, to = "BOLD"), @ViewDebug.IntToString(from = 2, to = "ITALIC"), @ViewDebug.IntToString(from = 3, to = "BOLD_ITALIC")})
    public int getTypefaceStyle() {
        Typeface typeface = this.mTextPaint.getTypeface();
        if (typeface != null) {
            return typeface.getStyle();
        }
        return 0;
    }

    @RemotableViewMethod
    public void setTextSize(float size) {
        setTextSize(2, size);
    }

    public void setTextSize(int unit, float size) {
        if (!isAutoSizeEnabled()) {
            setTextSizeInternal(unit, size, true);
        }
    }

    private void setTextSizeInternal(int unit, float size, boolean shouldRequestLayout) {
        Resources r;
        Context c = getContext();
        if (c == null) {
            r = Resources.getSystem();
        } else {
            r = c.getResources();
        }
        setRawTextSize(TypedValue.applyDimension(unit, size, r.getDisplayMetrics()), shouldRequestLayout);
    }

    private void setRawTextSize(float size, boolean shouldRequestLayout) {
        if (size != this.mTextPaint.getTextSize()) {
            this.mTextPaint.setTextSize(size);
            if (shouldRequestLayout && this.mLayout != null) {
                this.mNeedsAutoSizeText = false;
                nullLayouts();
                requestLayout();
                invalidate();
            }
        }
    }

    public float getTextScaleX() {
        return this.mTextPaint.getTextScaleX();
    }

    @RemotableViewMethod
    public void setTextScaleX(float size) {
        if (size != this.mTextPaint.getTextScaleX()) {
            this.mUserSetTextScaleX = true;
            this.mTextPaint.setTextScaleX(size);
            if (this.mLayout != null) {
                nullLayouts();
                requestLayout();
                invalidate();
            }
        }
    }

    public void setTypeface(Typeface tf) {
        if (this.mTextPaint.getTypeface() != tf) {
            this.mTextPaint.setTypeface(tf);
            if (this.mLayout != null) {
                nullLayouts();
                requestLayout();
                invalidate();
            }
        }
    }

    public Typeface getTypeface() {
        return this.mTextPaint.getTypeface();
    }

    public void setElegantTextHeight(boolean elegant) {
        if (elegant != this.mTextPaint.isElegantTextHeight()) {
            this.mTextPaint.setElegantTextHeight(elegant);
            if (this.mLayout != null) {
                nullLayouts();
                requestLayout();
                invalidate();
            }
        }
    }

    public void setFallbackLineSpacing(boolean enabled) {
        if (this.mUseFallbackLineSpacing != enabled) {
            this.mUseFallbackLineSpacing = enabled;
            if (this.mLayout != null) {
                nullLayouts();
                requestLayout();
                invalidate();
            }
        }
    }

    public boolean isFallbackLineSpacing() {
        return this.mUseFallbackLineSpacing;
    }

    public boolean isElegantTextHeight() {
        return this.mTextPaint.isElegantTextHeight();
    }

    public float getLetterSpacing() {
        return this.mTextPaint.getLetterSpacing();
    }

    @RemotableViewMethod
    public void setLetterSpacing(float letterSpacing) {
        if (letterSpacing != this.mTextPaint.getLetterSpacing()) {
            this.mTextPaint.setLetterSpacing(letterSpacing);
            if (this.mLayout != null) {
                nullLayouts();
                requestLayout();
                invalidate();
            }
        }
    }

    public String getFontFeatureSettings() {
        return this.mTextPaint.getFontFeatureSettings();
    }

    public String getFontVariationSettings() {
        return this.mTextPaint.getFontVariationSettings();
    }

    public void setBreakStrategy(int breakStrategy) {
        this.mBreakStrategy = breakStrategy;
        if (this.mLayout != null) {
            nullLayouts();
            requestLayout();
            invalidate();
        }
    }

    public int getBreakStrategy() {
        return this.mBreakStrategy;
    }

    public void setHyphenationFrequency(int hyphenationFrequency) {
        this.mHyphenationFrequency = hyphenationFrequency;
        if (this.mLayout != null) {
            nullLayouts();
            requestLayout();
            invalidate();
        }
    }

    public int getHyphenationFrequency() {
        return this.mHyphenationFrequency;
    }

    public PrecomputedText.Params getTextMetricsParams() {
        return new PrecomputedText.Params(new TextPaint((Paint) this.mTextPaint), getTextDirectionHeuristic(), this.mBreakStrategy, this.mHyphenationFrequency);
    }

    public void setTextMetricsParams(PrecomputedText.Params params) {
        this.mTextPaint.set(params.getTextPaint());
        this.mUserSetTextScaleX = true;
        this.mTextDir = params.getTextDirection();
        this.mBreakStrategy = params.getBreakStrategy();
        this.mHyphenationFrequency = params.getHyphenationFrequency();
        if (this.mLayout != null) {
            nullLayouts();
            requestLayout();
            invalidate();
        }
    }

    public void setJustificationMode(int justificationMode) {
        this.mJustificationMode = justificationMode;
        if (this.mLayout != null) {
            nullLayouts();
            requestLayout();
            invalidate();
        }
    }

    public int getJustificationMode() {
        return this.mJustificationMode;
    }

    @RemotableViewMethod
    public void setFontFeatureSettings(String fontFeatureSettings) {
        if (fontFeatureSettings != this.mTextPaint.getFontFeatureSettings()) {
            this.mTextPaint.setFontFeatureSettings(fontFeatureSettings);
            if (this.mLayout != null) {
                nullLayouts();
                requestLayout();
                invalidate();
            }
        }
    }

    public boolean setFontVariationSettings(String fontVariationSettings) {
        String existingSettings = this.mTextPaint.getFontVariationSettings();
        if (fontVariationSettings == existingSettings || (fontVariationSettings != null && fontVariationSettings.equals(existingSettings))) {
            return true;
        }
        boolean effective = this.mTextPaint.setFontVariationSettings(fontVariationSettings);
        if (effective && this.mLayout != null) {
            nullLayouts();
            requestLayout();
            invalidate();
        }
        return effective;
    }

    @RemotableViewMethod
    public void setTextColor(int color) {
        this.mTextColor = ColorStateList.valueOf(color);
        updateTextColors();
    }

    @RemotableViewMethod
    public void setTextColor(ColorStateList colors) {
        if (colors != null) {
            this.mTextColor = colors;
            updateTextColors();
            return;
        }
        throw new NullPointerException();
    }

    public final ColorStateList getTextColors() {
        return this.mTextColor;
    }

    public final int getCurrentTextColor() {
        return this.mCurTextColor;
    }

    @RemotableViewMethod
    public void setHighlightColor(int color) {
        if (this.mHighlightColor != color) {
            this.mHighlightColor = color;
            invalidate();
        }
    }

    public int getHighlightColor() {
        return this.mHighlightColor;
    }

    @RemotableViewMethod
    public final void setShowSoftInputOnFocus(boolean show) {
        createEditorIfNeeded();
        this.mEditor.mShowSoftInputOnFocus = show;
    }

    public final boolean getShowSoftInputOnFocus() {
        return this.mEditor == null || this.mEditor.mShowSoftInputOnFocus;
    }

    public void setShadowLayer(float radius, float dx, float dy, int color) {
        this.mTextPaint.setShadowLayer(radius, dx, dy, color);
        this.mShadowRadius = radius;
        this.mShadowDx = dx;
        this.mShadowDy = dy;
        this.mShadowColor = color;
        if (this.mEditor != null) {
            this.mEditor.invalidateTextDisplayList();
            this.mEditor.invalidateHandlesAndActionMode();
        }
        invalidate();
    }

    public float getShadowRadius() {
        return this.mShadowRadius;
    }

    public float getShadowDx() {
        return this.mShadowDx;
    }

    public float getShadowDy() {
        return this.mShadowDy;
    }

    public int getShadowColor() {
        return this.mShadowColor;
    }

    public TextPaint getPaint() {
        return this.mTextPaint;
    }

    @RemotableViewMethod
    public final void setAutoLinkMask(int mask) {
        this.mAutoLinkMask = mask;
    }

    @RemotableViewMethod
    public final void setLinksClickable(boolean whether) {
        this.mLinksClickable = whether;
    }

    public final boolean getLinksClickable() {
        return this.mLinksClickable;
    }

    public URLSpan[] getUrls() {
        if (this.mText instanceof Spanned) {
            return (URLSpan[]) ((Spanned) this.mText).getSpans(0, this.mText.length(), URLSpan.class);
        }
        return new URLSpan[0];
    }

    @RemotableViewMethod
    public final void setHintTextColor(int color) {
        this.mHintTextColor = ColorStateList.valueOf(color);
        updateTextColors();
    }

    public final void setHintTextColor(ColorStateList colors) {
        this.mHintTextColor = colors;
        updateTextColors();
    }

    public final ColorStateList getHintTextColors() {
        return this.mHintTextColor;
    }

    public final int getCurrentHintTextColor() {
        return this.mHintTextColor != null ? this.mCurHintTextColor : this.mCurTextColor;
    }

    @RemotableViewMethod
    public final void setLinkTextColor(int color) {
        this.mLinkTextColor = ColorStateList.valueOf(color);
        updateTextColors();
    }

    public final void setLinkTextColor(ColorStateList colors) {
        this.mLinkTextColor = colors;
        updateTextColors();
    }

    public final ColorStateList getLinkTextColors() {
        return this.mLinkTextColor;
    }

    public void setGravity(int gravity) {
        if ((gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
            gravity |= Gravity.START;
        }
        if ((gravity & 112) == 0) {
            gravity |= 48;
        }
        boolean newLayout = false;
        if ((gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) != (8388615 & this.mGravity)) {
            newLayout = true;
        }
        if (gravity != this.mGravity) {
            invalidate();
        }
        this.mGravity = gravity;
        if (this.mLayout != null && newLayout) {
            makeNewLayout(this.mLayout.getWidth(), this.mHintLayout == null ? 0 : this.mHintLayout.getWidth(), UNKNOWN_BORING, UNKNOWN_BORING, ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight(), true);
        }
    }

    public int getGravity() {
        return this.mGravity;
    }

    public int getPaintFlags() {
        return this.mTextPaint.getFlags();
    }

    @RemotableViewMethod
    public void setPaintFlags(int flags) {
        if (this.mTextPaint.getFlags() != flags) {
            this.mTextPaint.setFlags(flags);
            if (this.mLayout != null) {
                nullLayouts();
                requestLayout();
                invalidate();
            }
        }
    }

    public void setHorizontallyScrolling(boolean whether) {
        if (this.mHorizontallyScrolling != whether) {
            this.mHorizontallyScrolling = whether;
            if (this.mLayout != null) {
                nullLayouts();
                requestLayout();
                invalidate();
            }
        }
    }

    public boolean getHorizontallyScrolling() {
        return this.mHorizontallyScrolling;
    }

    @RemotableViewMethod
    public void setMinLines(int minLines) {
        this.mMinimum = minLines;
        this.mMinMode = 1;
        requestLayout();
        invalidate();
    }

    public int getMinLines() {
        if (this.mMinMode == 1) {
            return this.mMinimum;
        }
        return -1;
    }

    @RemotableViewMethod
    public void setMinHeight(int minPixels) {
        this.mMinimum = minPixels;
        this.mMinMode = 2;
        requestLayout();
        invalidate();
    }

    public int getMinHeight() {
        if (this.mMinMode == 2) {
            return this.mMinimum;
        }
        return -1;
    }

    @RemotableViewMethod
    public void setMaxLines(int maxLines) {
        this.mMaximum = maxLines;
        this.mMaxMode = 1;
        requestLayout();
        invalidate();
    }

    public int getMaxLines() {
        if (this.mMaxMode == 1) {
            return this.mMaximum;
        }
        return -1;
    }

    @RemotableViewMethod
    public void setMaxHeight(int maxPixels) {
        this.mMaximum = maxPixels;
        this.mMaxMode = 2;
        requestLayout();
        invalidate();
    }

    public int getMaxHeight() {
        if (this.mMaxMode == 2) {
            return this.mMaximum;
        }
        return -1;
    }

    @RemotableViewMethod
    public void setLines(int lines) {
        this.mMinimum = lines;
        this.mMaximum = lines;
        this.mMinMode = 1;
        this.mMaxMode = 1;
        requestLayout();
        invalidate();
    }

    @RemotableViewMethod
    public void setHeight(int pixels) {
        this.mMinimum = pixels;
        this.mMaximum = pixels;
        this.mMinMode = 2;
        this.mMaxMode = 2;
        requestLayout();
        invalidate();
    }

    @RemotableViewMethod
    public void setMinEms(int minEms) {
        this.mMinWidth = minEms;
        this.mMinWidthMode = 1;
        requestLayout();
        invalidate();
    }

    public int getMinEms() {
        if (this.mMinWidthMode == 1) {
            return this.mMinWidth;
        }
        return -1;
    }

    @RemotableViewMethod
    public void setMinWidth(int minPixels) {
        this.mMinWidth = minPixels;
        this.mMinWidthMode = 2;
        requestLayout();
        invalidate();
    }

    public int getMinWidth() {
        if (this.mMinWidthMode == 2) {
            return this.mMinWidth;
        }
        return -1;
    }

    @RemotableViewMethod
    public void setMaxEms(int maxEms) {
        this.mMaxWidth = maxEms;
        this.mMaxWidthMode = 1;
        requestLayout();
        invalidate();
    }

    public int getMaxEms() {
        if (this.mMaxWidthMode == 1) {
            return this.mMaxWidth;
        }
        return -1;
    }

    @RemotableViewMethod
    public void setMaxWidth(int maxPixels) {
        this.mMaxWidth = maxPixels;
        this.mMaxWidthMode = 2;
        requestLayout();
        invalidate();
    }

    public int getMaxWidth() {
        if (this.mMaxWidthMode == 2) {
            return this.mMaxWidth;
        }
        return -1;
    }

    @RemotableViewMethod
    public void setEms(int ems) {
        this.mMinWidth = ems;
        this.mMaxWidth = ems;
        this.mMinWidthMode = 1;
        this.mMaxWidthMode = 1;
        requestLayout();
        invalidate();
    }

    @RemotableViewMethod
    public void setWidth(int pixels) {
        this.mMinWidth = pixels;
        this.mMaxWidth = pixels;
        this.mMinWidthMode = 2;
        this.mMaxWidthMode = 2;
        requestLayout();
        invalidate();
    }

    public void setLineSpacing(float add, float mult) {
        if (this.mSpacingAdd != add || this.mSpacingMult != mult) {
            this.mSpacingAdd = add;
            this.mSpacingMult = mult;
            if (this.mLayout != null) {
                nullLayouts();
                requestLayout();
                invalidate();
            }
        }
    }

    public float getLineSpacingMultiplier() {
        return this.mSpacingMult;
    }

    public float getLineSpacingExtra() {
        return this.mSpacingAdd;
    }

    public void setLineHeight(int lineHeight) {
        Preconditions.checkArgumentNonnegative(lineHeight);
        int fontHeight = getPaint().getFontMetricsInt(null);
        if (lineHeight != fontHeight) {
            setLineSpacing((float) (lineHeight - fontHeight), 1.0f);
        }
    }

    public final void append(CharSequence text) {
        append(text, 0, text.length());
    }

    public void append(CharSequence text, int start, int end) {
        if (!(this.mText instanceof Editable)) {
            setText(this.mText, BufferType.EDITABLE);
        }
        ((Editable) this.mText).append(text, start, end);
        if (this.mAutoLinkMask != 0 && Linkify.addLinks(this.mSpannable, this.mAutoLinkMask) && this.mLinksClickable && !textCanBeSelected()) {
            setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void updateTextColors() {
        boolean inval = false;
        int[] drawableState = getDrawableState();
        int color = this.mTextColor.getColorForState(drawableState, 0);
        if (color != this.mCurTextColor) {
            this.mCurTextColor = color;
            inval = true;
        }
        if (this.mLinkTextColor != null) {
            int color2 = this.mLinkTextColor.getColorForState(drawableState, 0);
            if (color2 != this.mTextPaint.linkColor) {
                this.mTextPaint.linkColor = color2;
                inval = true;
            }
        }
        if (this.mHintTextColor != null) {
            int color3 = this.mHintTextColor.getColorForState(drawableState, 0);
            if (color3 != this.mCurHintTextColor) {
                this.mCurHintTextColor = color3;
                if (this.mText.length() == 0) {
                    inval = true;
                }
            }
        }
        if (inval) {
            if (this.mEditor != null) {
                this.mEditor.invalidateTextDisplayList();
            }
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        if ((this.mTextColor != null && this.mTextColor.isStateful()) || ((this.mHintTextColor != null && this.mHintTextColor.isStateful()) || (this.mLinkTextColor != null && this.mLinkTextColor.isStateful()))) {
            updateTextColors();
        }
        if (this.mDrawables != null) {
            int[] state = getDrawableState();
            for (Drawable dr : this.mDrawables.mShowing) {
                if (dr != null && dr.isStateful() && dr.setState(state)) {
                    invalidateDrawable(dr);
                }
            }
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (this.mDrawables != null) {
            for (Drawable dr : this.mDrawables.mShowing) {
                if (dr != null) {
                    dr.setHotspot(x, y);
                }
            }
        }
    }

    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        boolean freezesText = getFreezesText();
        boolean hasSelection = false;
        int start = -1;
        int end = -1;
        if (this.mText != null) {
            start = getSelectionStart();
            end = getSelectionEnd();
            if (start >= 0 || end >= 0) {
                hasSelection = true;
            }
        }
        if (!freezesText && !hasSelection) {
            return superState;
        }
        SavedState ss = new SavedState(superState);
        if (freezesText) {
            if (this.mText instanceof Spanned) {
                Spannable sp = new SpannableStringBuilder(this.mText);
                if (this.mEditor != null) {
                    removeMisspelledSpans(sp);
                    sp.removeSpan(this.mEditor.mSuggestionRangeSpan);
                }
                ss.text = sp;
            } else {
                ss.text = this.mText.toString();
            }
        }
        if (hasSelection) {
            ss.selStart = start;
            ss.selEnd = end;
        }
        if (isFocused() && start >= 0 && end >= 0) {
            ss.frozenWithFocus = true;
        }
        ss.error = getError();
        if (this.mEditor != null) {
            ss.editorState = this.mEditor.saveInstanceState();
        }
        return ss;
    }

    /* access modifiers changed from: package-private */
    public void removeMisspelledSpans(Spannable spannable) {
        int flags = 0;
        SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) spannable.getSpans(0, spannable.length(), SuggestionSpan.class);
        while (true) {
            int i = flags;
            if (i < suggestionSpans.length) {
                int flags2 = suggestionSpans[i].getFlags();
                if (!((flags2 & 1) == 0 || (flags2 & 2) == 0)) {
                    spannable.removeSpan(suggestionSpans[i]);
                }
                flags = i + 1;
            } else {
                return;
            }
        }
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        if (ss.text != null) {
            setText(ss.text);
        }
        if (ss.selStart >= 0 && ss.selEnd >= 0 && this.mSpannable != null) {
            int len = this.mText.length();
            if (ss.selStart > len || ss.selEnd > len) {
                String restored = "";
                if (ss.text != null) {
                    restored = "(restored) ";
                }
                Log.e(LOG_TAG, "Saved cursor position " + ss.selStart + "/" + ss.selEnd + " out of range for " + restored + "text " + this.mText);
            } else {
                Selection.setSelection(this.mSpannable, ss.selStart, ss.selEnd);
                if (ss.frozenWithFocus) {
                    createEditorIfNeeded();
                    this.mEditor.mFrozenWithFocus = true;
                }
            }
        }
        if (ss.error != null) {
            final CharSequence error = ss.error;
            post(new Runnable() {
                public void run() {
                    if (TextView.this.mEditor == null || !TextView.this.mEditor.mErrorWasChanged) {
                        TextView.this.setError(error);
                    }
                }
            });
        }
        if (ss.editorState != null) {
            createEditorIfNeeded();
            this.mEditor.restoreInstanceState(ss.editorState);
        }
    }

    @RemotableViewMethod
    public void setFreezesText(boolean freezesText) {
        this.mFreezesText = freezesText;
    }

    public boolean getFreezesText() {
        return this.mFreezesText;
    }

    public final void setEditableFactory(Editable.Factory factory) {
        this.mEditableFactory = factory;
        setText(this.mText);
    }

    public final void setSpannableFactory(Spannable.Factory factory) {
        this.mSpannableFactory = factory;
        setText(this.mText);
    }

    @RemotableViewMethod
    public final void setText(CharSequence text) {
        setText(text, this.mBufferType);
    }

    @RemotableViewMethod
    public final void setTextKeepState(CharSequence text) {
        setTextKeepState(text, this.mBufferType);
    }

    public void setText(CharSequence text, BufferType type) {
        setText(text, type, true, 0);
        if (this.mCharWrapper != null) {
            char[] unused = this.mCharWrapper.mChars = null;
        }
    }

    private void setText(CharSequence text, BufferType type, boolean notifyBefore, int oldlen) {
        Spannable s2;
        this.mTextSetFromXmlOrResourceId = false;
        if (text == null) {
            text = "";
        }
        if (!isSuggestionsEnabled()) {
            text = removeSuggestionSpans(text);
        }
        if (!this.mUserSetTextScaleX) {
            this.mTextPaint.setTextScaleX(1.0f);
        }
        if ((text instanceof Spanned) && ((Spanned) text).getSpanStart(TextUtils.TruncateAt.MARQUEE) >= 0) {
            if (ViewConfiguration.get(this.mContext).isFadingMarqueeEnabled()) {
                setHorizontalFadingEdgeEnabled(true);
                this.mMarqueeFadeMode = 0;
            } else {
                setHorizontalFadingEdgeEnabled(false);
                this.mMarqueeFadeMode = 1;
            }
            setEllipsize(TextUtils.TruncateAt.MARQUEE);
        }
        CharSequence text2 = text;
        for (InputFilter filter : this.mFilters) {
            CharSequence out = filter.filter(text2, 0, text2.length(), EMPTY_SPANNED, 0, 0);
            if (out != null) {
                text2 = out;
            }
        }
        if (notifyBefore) {
            if (this.mText != null) {
                oldlen = this.mText.length();
                sendBeforeTextChanged(this.mText, 0, oldlen, text2.length());
            } else {
                sendBeforeTextChanged("", 0, 0, text2.length());
            }
        }
        boolean needEditableForNotification = false;
        if (!(this.mListeners == null || this.mListeners.size() == 0)) {
            needEditableForNotification = true;
        }
        PrecomputedText precomputed = text2 instanceof PrecomputedText ? (PrecomputedText) text2 : null;
        if (type == BufferType.EDITABLE || getKeyListener() != null || needEditableForNotification) {
            createEditorIfNeeded();
            this.mEditor.forgetUndoRedo();
            Editable t = this.mEditableFactory.newEditable(text2);
            text2 = t;
            setFilters(t, this.mFilters);
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null) {
                imm.restartInput(this);
            }
        } else if (precomputed != null) {
            if (this.mTextDir == null) {
                this.mTextDir = getTextDirectionHeuristic();
            }
            if (!precomputed.getParams().isSameTextMetricsInternal(getPaint(), this.mTextDir, this.mBreakStrategy, this.mHyphenationFrequency)) {
                throw new IllegalArgumentException("PrecomputedText's Parameters don't match the parameters of this TextView.Consider using setTextMetricsParams(precomputedText.getParams()) to override the settings of this TextView: PrecomputedText: " + precomputed.getParams() + "TextView: " + getTextMetricsParams());
            }
        } else if (type == BufferType.SPANNABLE || this.mMovement != null) {
            text2 = this.mSpannableFactory.newSpannable(text2);
        } else if (!(text2 instanceof CharWrapper)) {
            text2 = TextUtils.stringOrSpannedString(text2);
        }
        if (this.mAutoLinkMask != 0) {
            if (type == BufferType.EDITABLE || (text2 instanceof Spannable)) {
                s2 = (Spannable) text2;
            } else {
                s2 = this.mSpannableFactory.newSpannable(text2);
            }
            if (Linkify.addLinks(s2, this.mAutoLinkMask)) {
                text2 = s2;
                type = type == BufferType.EDITABLE ? BufferType.EDITABLE : BufferType.SPANNABLE;
                setTextInternal(text2);
                if (this.mLinksClickable && !textCanBeSelected()) {
                    setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
        }
        this.mBufferType = type;
        setTextInternal(text2);
        if (this.mTransformation == null) {
            this.mTransformed = text2;
        } else {
            this.mTransformed = this.mTransformation.getTransformation(text2, this);
        }
        int textLength = text2.length();
        if ((text2 instanceof Spannable) && !this.mAllowTransformationLengthChange) {
            Spannable sp = (Spannable) text2;
            for (ChangeWatcher removeSpan : (ChangeWatcher[]) sp.getSpans(0, sp.length(), ChangeWatcher.class)) {
                sp.removeSpan(removeSpan);
            }
            if (this.mChangeWatcher == null) {
                this.mChangeWatcher = new ChangeWatcher();
            }
            sp.setSpan(this.mChangeWatcher, 0, textLength, 6553618);
            if (this.mEditor != null) {
                this.mEditor.addSpanWatchers(sp);
            }
            if (this.mTransformation != null) {
                sp.setSpan(this.mTransformation, 0, textLength, 18);
            }
            if (this.mMovement != null) {
                this.mMovement.initialize(this, (Spannable) text2);
                if (this.mEditor != null) {
                    this.mEditor.mSelectionMoved = false;
                }
            }
        }
        if (this.mLayout != null) {
            checkForRelayout();
        }
        sendOnTextChanged(text2, 0, oldlen, textLength);
        onTextChanged(text2, 0, oldlen, textLength);
        notifyViewAccessibilityStateChangedIfNeeded(2);
        if (needEditableForNotification) {
            sendAfterTextChanged((Editable) text2);
        } else {
            notifyAutoFillManagerAfterTextChangedIfNeeded();
        }
        if (this.mEditor != null) {
            this.mEditor.prepareCursorControllers();
        }
    }

    public final void setText(char[] text, int start, int len) {
        int oldlen = 0;
        if (start < 0 || len < 0 || start + len > text.length) {
            throw new IndexOutOfBoundsException(start + ", " + len);
        }
        if (this.mText != null) {
            oldlen = this.mText.length();
            sendBeforeTextChanged(this.mText, 0, oldlen, len);
        } else {
            sendBeforeTextChanged("", 0, 0, len);
        }
        if (this.mCharWrapper == null) {
            this.mCharWrapper = new CharWrapper(text, start, len);
        } else {
            this.mCharWrapper.set(text, start, len);
        }
        setText(this.mCharWrapper, this.mBufferType, false, oldlen);
    }

    public final void setTextKeepState(CharSequence text, BufferType type) {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        int len = text.length();
        setText(text, type);
        if ((start >= 0 || end >= 0) && this.mSpannable != null) {
            Selection.setSelection(this.mSpannable, Math.max(0, Math.min(start, len)), Math.max(0, Math.min(end, len)));
        }
    }

    @RemotableViewMethod
    public final void setText(int resid) {
        setText(getContext().getResources().getText(resid));
        this.mTextSetFromXmlOrResourceId = true;
        this.mTextId = resid;
    }

    public final void setText(int resid, BufferType type) {
        setText(getContext().getResources().getText(resid), type);
        this.mTextSetFromXmlOrResourceId = true;
        this.mTextId = resid;
    }

    @RemotableViewMethod
    public final void setHint(CharSequence hint) {
        setHintInternal(hint);
        if (this.mEditor != null && isInputMethodTarget()) {
            this.mEditor.reportExtractedText();
        }
    }

    private void setHintInternal(CharSequence hint) {
        this.mHint = TextUtils.stringOrSpannedString(hint);
        if (this.mLayout != null) {
            checkForRelayout();
        }
        if (this.mText.length() == 0) {
            invalidate();
        }
        if (this.mEditor != null && this.mText.length() == 0 && this.mHint != null) {
            this.mEditor.invalidateTextDisplayList();
        }
    }

    @RemotableViewMethod
    public final void setHint(int resid) {
        setHint(getContext().getResources().getText(resid));
    }

    @ViewDebug.CapturedViewProperty
    public CharSequence getHint() {
        return this.mHint;
    }

    /* access modifiers changed from: package-private */
    public boolean isSingleLine() {
        return this.mSingleLine;
    }

    private static boolean isMultilineInputType(int type) {
        return (131087 & type) == 131073;
    }

    /* access modifiers changed from: package-private */
    public CharSequence removeSuggestionSpans(CharSequence text) {
        Spannable spannable;
        if (text instanceof Spanned) {
            if (text instanceof Spannable) {
                spannable = (Spannable) text;
            } else {
                spannable = this.mSpannableFactory.newSpannable(text);
            }
            int i = 0;
            SuggestionSpan[] spans = (SuggestionSpan[]) spannable.getSpans(0, text.length(), SuggestionSpan.class);
            if (spans.length != 0) {
                text = spannable;
                while (true) {
                    int i2 = i;
                    if (i2 >= spans.length) {
                        break;
                    }
                    spannable.removeSpan(spans[i2]);
                    i = i2 + 1;
                }
            } else {
                return text;
            }
        }
        return text;
    }

    public void setInputType(int type) {
        boolean wasPassword = isPasswordInputType(getInputType());
        boolean wasVisiblePassword = isVisiblePasswordInputType(getInputType());
        setInputType(type, false);
        boolean isPassword = isPasswordInputType(type);
        boolean isVisiblePassword = isVisiblePasswordInputType(type);
        boolean forceUpdate = false;
        if (isPassword) {
            setTransformationMethod(PasswordTransformationMethod.getInstance());
            setTypefaceFromAttrs(null, null, 3, 0, -1);
        } else if (isVisiblePassword) {
            if (this.mTransformation == PasswordTransformationMethod.getInstance()) {
                forceUpdate = true;
            }
            setTypefaceFromAttrs(null, null, 3, 0, -1);
        } else if (wasPassword || wasVisiblePassword) {
            setTypefaceFromAttrs(null, null, -1, 0, -1);
            if (this.mTransformation == PasswordTransformationMethod.getInstance()) {
                forceUpdate = true;
            }
        }
        boolean singleLine = !isMultilineInputType(type);
        if (this.mSingleLine != singleLine || forceUpdate) {
            applySingleLine(singleLine, !isPassword, true);
        }
        if (!isSuggestionsEnabled()) {
            setTextInternal(removeSuggestionSpans(this.mText));
        }
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (imm != null) {
            imm.restartInput(this);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasPasswordTransformationMethod() {
        return this.mTransformation instanceof PasswordTransformationMethod;
    }

    static boolean isPasswordInputType(int inputType) {
        int variation = inputType & FileObserver.ALL_EVENTS;
        return variation == 129 || variation == 225 || variation == 18;
    }

    private static boolean isVisiblePasswordInputType(int inputType) {
        return (inputType & FileObserver.ALL_EVENTS) == 145;
    }

    public void setRawInputType(int type) {
        if (type != 0 || this.mEditor != null) {
            createEditorIfNeeded();
            this.mEditor.mInputType = type;
        }
    }

    private Locale getCustomLocaleForKeyListenerOrNull() {
        if (!this.mUseInternationalizedInput) {
            return null;
        }
        LocaleList locales = getImeHintLocales();
        if (locales == null) {
            return null;
        }
        return locales.get(0);
    }

    private void setInputType(int type, boolean direct) {
        KeyListener input;
        KeyListener input2;
        TextKeyListener.Capitalize cap;
        int cls = type & 15;
        boolean autotext = true;
        if (cls == 1) {
            if ((32768 & type) == 0) {
                autotext = false;
            }
            if ((type & 4096) != 0) {
                cap = TextKeyListener.Capitalize.CHARACTERS;
            } else if ((type & 8192) != 0) {
                cap = TextKeyListener.Capitalize.WORDS;
            } else if ((type & 16384) != 0) {
                cap = TextKeyListener.Capitalize.SENTENCES;
            } else {
                cap = TextKeyListener.Capitalize.NONE;
            }
            input = TextKeyListener.getInstance(autotext, cap);
        } else if (cls == 2) {
            Locale locale = getCustomLocaleForKeyListenerOrNull();
            boolean z = (type & 4096) != 0;
            if ((type & 8192) == 0) {
                autotext = false;
            }
            input = DigitsKeyListener.getInstance(locale, z, autotext);
            if (locale != null) {
                int newType = input.getInputType();
                if ((newType & 15) != 2) {
                    if ((type & 16) != 0) {
                        newType |= 128;
                    }
                    type = newType;
                }
            }
        } else if (cls == 4) {
            Locale locale2 = getCustomLocaleForKeyListenerOrNull();
            int i = type & InputType.TYPE_MASK_VARIATION;
            if (i == 16) {
                input2 = DateKeyListener.getInstance(locale2);
            } else if (i != 32) {
                input2 = DateTimeKeyListener.getInstance(locale2);
            } else {
                input2 = TimeKeyListener.getInstance(locale2);
            }
            if (this.mUseInternationalizedInput) {
                type = input2.getInputType();
            }
            input = input2;
        } else {
            input = cls == 3 ? DialerKeyListener.getInstance() : TextKeyListener.getInstance();
        }
        setRawInputType(type);
        this.mListenerChanged = false;
        if (direct) {
            createEditorIfNeeded();
            this.mEditor.mKeyListener = input;
            return;
        }
        setKeyListenerOnly(input);
    }

    public int getInputType() {
        if (this.mEditor == null) {
            return 0;
        }
        return this.mEditor.mInputType;
    }

    public void setImeOptions(int imeOptions) {
        createEditorIfNeeded();
        this.mEditor.createInputContentTypeIfNeeded();
        this.mEditor.mInputContentType.imeOptions = imeOptions;
    }

    public int getImeOptions() {
        if (this.mEditor == null || this.mEditor.mInputContentType == null) {
            return 0;
        }
        return this.mEditor.mInputContentType.imeOptions;
    }

    public void setImeActionLabel(CharSequence label, int actionId) {
        createEditorIfNeeded();
        this.mEditor.createInputContentTypeIfNeeded();
        this.mEditor.mInputContentType.imeActionLabel = label;
        this.mEditor.mInputContentType.imeActionId = actionId;
    }

    public CharSequence getImeActionLabel() {
        if (this.mEditor == null || this.mEditor.mInputContentType == null) {
            return null;
        }
        return this.mEditor.mInputContentType.imeActionLabel;
    }

    public int getImeActionId() {
        if (this.mEditor == null || this.mEditor.mInputContentType == null) {
            return 0;
        }
        return this.mEditor.mInputContentType.imeActionId;
    }

    public void setOnEditorActionListener(OnEditorActionListener l) {
        createEditorIfNeeded();
        this.mEditor.createInputContentTypeIfNeeded();
        this.mEditor.mInputContentType.onEditorActionListener = l;
    }

    public void onEditorAction(int actionCode) {
        int i = actionCode;
        Editor.InputContentType ict = this.mEditor == null ? null : this.mEditor.mInputContentType;
        if (ict != null) {
            if (ict.onEditorActionListener != null && ict.onEditorActionListener.onEditorAction(this, i, null)) {
                return;
            }
            if (i == 5) {
                View v = focusSearch(2);
                if (v != null) {
                    if (v.requestFocus(2)) {
                        InputMethodManager imm = InputMethodManager.peekInstance();
                        if (imm != null && imm.isSecImmEnabled()) {
                            imm.showSoftInput(v, 0);
                        }
                    } else {
                        throw new IllegalStateException("focus search returned a view that wasn't able to take focus!");
                    }
                }
                return;
            } else if (i == 7) {
                View v2 = focusSearch(1);
                if (v2 != null) {
                    if (v2.requestFocus(1)) {
                        InputMethodManager imm2 = InputMethodManager.peekInstance();
                        if (imm2 != null && imm2.isSecImmEnabled()) {
                            imm2.showSoftInput(v2, 0);
                        }
                    } else {
                        throw new IllegalStateException("focus search returned a view that wasn't able to take focus!");
                    }
                }
                return;
            } else if (i == 6) {
                InputMethodManager imm3 = InputMethodManager.peekInstance();
                if (imm3 != null && imm3.isActive(this)) {
                    imm3.hideSoftInputFromWindow(getWindowToken(), 0);
                }
                return;
            }
        }
        ViewRootImpl viewRootImpl = getViewRootImpl();
        if (viewRootImpl != null) {
            long eventTime = SystemClock.uptimeMillis();
            long j = eventTime;
            KeyEvent keyEvent = r4;
            KeyEvent keyEvent2 = new KeyEvent(eventTime, j, 0, 66, 0, 0, -1, 0, 22);
            viewRootImpl.dispatchKeyFromIme(keyEvent);
            KeyEvent keyEvent3 = new KeyEvent(SystemClock.uptimeMillis(), j, 1, 66, 0, 0, -1, 0, 22);
            viewRootImpl.dispatchKeyFromIme(keyEvent3);
        }
    }

    public void setPrivateImeOptions(String type) {
        createEditorIfNeeded();
        this.mEditor.createInputContentTypeIfNeeded();
        this.mEditor.mInputContentType.privateImeOptions = type;
    }

    public String getPrivateImeOptions() {
        if (this.mEditor == null || this.mEditor.mInputContentType == null) {
            return null;
        }
        return this.mEditor.mInputContentType.privateImeOptions;
    }

    public void setInputExtras(int xmlResId) throws XmlPullParserException, IOException {
        createEditorIfNeeded();
        XmlResourceParser parser = getResources().getXml(xmlResId);
        this.mEditor.createInputContentTypeIfNeeded();
        this.mEditor.mInputContentType.extras = new Bundle();
        getResources().parseBundleExtras(parser, this.mEditor.mInputContentType.extras);
    }

    public Bundle getInputExtras(boolean create) {
        if (this.mEditor == null && !create) {
            return null;
        }
        createEditorIfNeeded();
        if (this.mEditor.mInputContentType == null) {
            if (!create) {
                return null;
            }
            this.mEditor.createInputContentTypeIfNeeded();
        }
        if (this.mEditor.mInputContentType.extras == null) {
            if (!create) {
                return null;
            }
            this.mEditor.mInputContentType.extras = new Bundle();
        }
        return this.mEditor.mInputContentType.extras;
    }

    public void setImeHintLocales(LocaleList hintLocales) {
        createEditorIfNeeded();
        this.mEditor.createInputContentTypeIfNeeded();
        this.mEditor.mInputContentType.imeHintLocales = hintLocales;
        if (this.mUseInternationalizedInput) {
            changeListenerLocaleTo(hintLocales == null ? null : hintLocales.get(0));
        }
    }

    public LocaleList getImeHintLocales() {
        if (this.mEditor == null || this.mEditor.mInputContentType == null) {
            return null;
        }
        return this.mEditor.mInputContentType.imeHintLocales;
    }

    public CharSequence getError() {
        if (this.mEditor == null) {
            return null;
        }
        return this.mEditor.mError;
    }

    @RemotableViewMethod
    public void setError(CharSequence error) {
        if (error == null) {
            setError(null, null);
        } else if (this.mHwTextView != null) {
            this.mHwTextView.setError(this, getContext(), error);
        } else {
            Drawable dr = getContext().getDrawable(17302805);
            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
            setError(error, dr);
        }
    }

    public void setError(CharSequence error, Drawable icon) {
        createEditorIfNeeded();
        this.mEditor.setError(error, icon);
        notifyViewAccessibilityStateChangedIfNeeded(0);
    }

    /* access modifiers changed from: protected */
    public boolean setFrame(int l, int t, int r, int b) {
        boolean result = super.setFrame(l, t, r, b);
        if (this.mEditor != null) {
            this.mEditor.setFrame();
        }
        restartMarqueeIfNeeded();
        return result;
    }

    private void restartMarqueeIfNeeded() {
        if (this.mRestartMarquee && this.mEllipsize == TextUtils.TruncateAt.MARQUEE) {
            this.mRestartMarquee = false;
            startMarquee();
        }
    }

    public void setFilters(InputFilter[] filters) {
        if (filters != null) {
            this.mFilters = filters;
            if (this.mText instanceof Editable) {
                setFilters((Editable) this.mText, filters);
                return;
            }
            return;
        }
        throw new IllegalArgumentException();
    }

    private void setFilters(Editable e, InputFilter[] filters) {
        if (this.mEditor != null) {
            boolean undoFilter = this.mEditor.mUndoInputFilter != null;
            boolean keyFilter = this.mEditor.mKeyListener instanceof InputFilter;
            int num = 0;
            if (undoFilter) {
                num = 0 + 1;
            }
            if (keyFilter) {
                num++;
            }
            if (num > 0) {
                InputFilter[] nf = new InputFilter[(filters.length + num)];
                System.arraycopy(filters, 0, nf, 0, filters.length);
                int num2 = 0;
                if (undoFilter) {
                    nf[filters.length] = this.mEditor.mUndoInputFilter;
                    num2 = 0 + 1;
                }
                if (keyFilter) {
                    nf[filters.length + num2] = (InputFilter) this.mEditor.mKeyListener;
                }
                e.setFilters(nf);
                return;
            }
        }
        e.setFilters(filters);
    }

    public InputFilter[] getFilters() {
        return this.mFilters;
    }

    private int getBoxHeight(Layout l) {
        int padding;
        Insets opticalInsets = isLayoutModeOptical(this.mParent) ? getOpticalInsets() : Insets.NONE;
        if (l == this.mHintLayout) {
            padding = getCompoundPaddingTop() + getCompoundPaddingBottom();
        } else {
            padding = getExtendedPaddingTop() + getExtendedPaddingBottom();
        }
        return (getMeasuredHeight() - padding) + opticalInsets.top + opticalInsets.bottom;
    }

    /* access modifiers changed from: package-private */
    public int getVerticalOffset(boolean forceNormal) {
        int gravity = this.mGravity & 112;
        Layout l = this.mLayout;
        if (!forceNormal && this.mText.length() == 0 && this.mHintLayout != null) {
            l = this.mHintLayout;
        }
        if (gravity == 48) {
            return 0;
        }
        int boxht = getBoxHeight(l);
        int textht = l.getHeight();
        if (textht >= boxht) {
            return 0;
        }
        if (gravity == 80) {
            return boxht - textht;
        }
        return (boxht - textht) >> 1;
    }

    private int getBottomVerticalOffset(boolean forceNormal) {
        int gravity = this.mGravity & 112;
        Layout l = this.mLayout;
        if (!forceNormal && this.mText.length() == 0 && this.mHintLayout != null) {
            l = this.mHintLayout;
        }
        if (gravity == 80) {
            return 0;
        }
        int boxht = getBoxHeight(l);
        int textht = l.getHeight();
        if (textht >= boxht) {
            return 0;
        }
        if (gravity == 48) {
            return boxht - textht;
        }
        return (boxht - textht) >> 1;
    }

    /* access modifiers changed from: package-private */
    public void invalidateCursorPath() {
        if (this.mHighlightPathBogus) {
            invalidateCursor();
            return;
        }
        int horizontalPadding = getCompoundPaddingLeft();
        int verticalPadding = getExtendedPaddingTop() + getVerticalOffset(true);
        if (this.mEditor.mDrawableForCursor == null) {
            synchronized (TEMP_RECTF) {
                float thick = (float) Math.ceil((double) this.mTextPaint.getStrokeWidth());
                if (thick < 1.0f) {
                    thick = 1.0f;
                }
                float thick2 = thick / 2.0f;
                this.mHighlightPath.computeBounds(TEMP_RECTF, false);
                invalidate((int) Math.floor((double) ((((float) horizontalPadding) + TEMP_RECTF.left) - thick2)), (int) Math.floor((double) ((((float) verticalPadding) + TEMP_RECTF.top) - thick2)), (int) Math.ceil((double) (((float) horizontalPadding) + TEMP_RECTF.right + thick2)), (int) Math.ceil((double) (((float) verticalPadding) + TEMP_RECTF.bottom + thick2)));
            }
            return;
        }
        Rect bounds = this.mEditor.mDrawableForCursor.getBounds();
        invalidate(bounds.left + horizontalPadding, bounds.top + verticalPadding, bounds.right + horizontalPadding, bounds.bottom + verticalPadding);
    }

    /* access modifiers changed from: package-private */
    public void invalidateCursor() {
        int where = getSelectionEnd();
        invalidateCursor(where, where, where);
    }

    private void invalidateCursor(int a, int b, int c) {
        if (a >= 0 || b >= 0 || c >= 0) {
            invalidateRegion(Math.min(Math.min(a, b), c), Math.max(Math.max(a, b), c), true);
        }
    }

    /* access modifiers changed from: package-private */
    public void invalidateRegion(int start, int end, boolean invalidateCursor) {
        int lineEnd;
        int right;
        int left;
        if (this.mLayout == null) {
            invalidate();
            return;
        }
        int lineStart = this.mLayout.getLineForOffset(start);
        int top = this.mLayout.getLineTop(lineStart);
        if (lineStart > 0) {
            top -= this.mLayout.getLineDescent(lineStart - 1);
        }
        if (start == end) {
            lineEnd = lineStart;
        } else {
            lineEnd = this.mLayout.getLineForOffset(end);
        }
        int bottom = this.mLayout.getLineBottom(lineEnd);
        if (!(!invalidateCursor || this.mEditor == null || this.mEditor.mDrawableForCursor == null)) {
            Rect bounds = this.mEditor.mDrawableForCursor.getBounds();
            top = Math.min(top, bounds.top);
            bottom = Math.max(bottom, bounds.bottom);
        }
        int compoundPaddingLeft = getCompoundPaddingLeft();
        int verticalPadding = getExtendedPaddingTop() + getVerticalOffset(true);
        if (lineStart != lineEnd || invalidateCursor) {
            left = compoundPaddingLeft;
            right = getWidth() - getCompoundPaddingRight();
        } else {
            left = ((int) this.mLayout.getPrimaryHorizontal(start)) + compoundPaddingLeft;
            right = ((int) (((double) this.mLayout.getPrimaryHorizontal(end)) + 1.0d)) + compoundPaddingLeft;
        }
        invalidate(this.mScrollX + left, verticalPadding + top, this.mScrollX + right, verticalPadding + bottom);
    }

    private void registerForPreDraw() {
        if (!this.mPreDrawRegistered) {
            getViewTreeObserver().addOnPreDrawListener(this);
            this.mPreDrawRegistered = true;
        }
    }

    private void unregisterForPreDraw() {
        getViewTreeObserver().removeOnPreDrawListener(this);
        this.mPreDrawRegistered = false;
        this.mPreDrawListenerDetached = false;
    }

    public boolean onPreDraw() {
        if (this.mLayout == null) {
            assumeLayout();
        }
        if (this.mMovement != null) {
            int curs = getSelectionEnd();
            if (!(this.mEditor == null || this.mEditor.mSelectionModifierCursorController == null || !this.mEditor.mSelectionModifierCursorController.isSelectionStartDragged())) {
                curs = getSelectionStart();
            }
            if (curs < 0 && (this.mGravity & 112) == 80) {
                curs = this.mText.length();
            }
            if (curs >= 0) {
                bringPointIntoView(curs);
            }
        } else {
            bringTextIntoView();
        }
        if (this.mEditor != null && this.mEditor.mCreatedWithASelection) {
            this.mEditor.refreshTextActionMode();
            this.mEditor.mCreatedWithASelection = false;
        }
        unregisterForPreDraw();
        return true;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mEditor != null) {
            this.mEditor.onAttachedToWindow();
        }
        if (this.mPreDrawListenerDetached) {
            getViewTreeObserver().addOnPreDrawListener(this);
            this.mPreDrawListenerDetached = false;
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindowInternal() {
        if (this.mPreDrawRegistered) {
            getViewTreeObserver().removeOnPreDrawListener(this);
            this.mPreDrawListenerDetached = true;
        }
        resetResolvedDrawables();
        if (this.mEditor != null) {
            this.mEditor.onDetachedFromWindow();
        }
        super.onDetachedFromWindowInternal();
    }

    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        if (this.mEditor != null) {
            this.mEditor.onScreenStateChanged(screenState);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isPaddingOffsetRequired() {
        return (this.mShadowRadius == 0.0f && this.mDrawables == null) ? false : true;
    }

    /* access modifiers changed from: protected */
    public int getLeftPaddingOffset() {
        return (getCompoundPaddingLeft() - this.mPaddingLeft) + ((int) Math.min(0.0f, this.mShadowDx - this.mShadowRadius));
    }

    /* access modifiers changed from: protected */
    public int getTopPaddingOffset() {
        return (int) Math.min(0.0f, this.mShadowDy - this.mShadowRadius);
    }

    /* access modifiers changed from: protected */
    public int getBottomPaddingOffset() {
        return (int) Math.max(0.0f, this.mShadowDy + this.mShadowRadius);
    }

    /* access modifiers changed from: protected */
    public int getRightPaddingOffset() {
        return (-(getCompoundPaddingRight() - this.mPaddingRight)) + ((int) Math.max(0.0f, this.mShadowDx + this.mShadowRadius));
    }

    /* access modifiers changed from: protected */
    public boolean verifyDrawable(Drawable who) {
        boolean verified = super.verifyDrawable(who);
        if (!verified && this.mDrawables != null) {
            for (Drawable dr : this.mDrawables.mShowing) {
                if (who == dr) {
                    return true;
                }
            }
        }
        return verified;
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mDrawables != null) {
            for (Drawable dr : this.mDrawables.mShowing) {
                if (dr != null) {
                    dr.jumpToCurrentState();
                }
            }
        }
    }

    public void invalidateDrawable(Drawable drawable) {
        boolean handled = false;
        if (verifyDrawable(drawable)) {
            Rect dirty = drawable.getBounds();
            int scrollX = this.mScrollX;
            int scrollY = this.mScrollY;
            Drawables drawables = this.mDrawables;
            if (drawables != null) {
                if (drawable == drawables.mShowing[0]) {
                    int compoundPaddingTop = getCompoundPaddingTop();
                    int compoundPaddingBottom = getCompoundPaddingBottom();
                    scrollX += this.mPaddingLeft;
                    scrollY += (((((this.mBottom - this.mTop) - compoundPaddingBottom) - compoundPaddingTop) - drawables.mDrawableHeightLeft) / 2) + compoundPaddingTop;
                    handled = true;
                } else if (drawable == drawables.mShowing[2]) {
                    int compoundPaddingTop2 = getCompoundPaddingTop();
                    int compoundPaddingBottom2 = getCompoundPaddingBottom();
                    scrollX += ((this.mRight - this.mLeft) - this.mPaddingRight) - drawables.mDrawableSizeRight;
                    scrollY += (((((this.mBottom - this.mTop) - compoundPaddingBottom2) - compoundPaddingTop2) - drawables.mDrawableHeightRight) / 2) + compoundPaddingTop2;
                    handled = true;
                } else if (drawable == drawables.mShowing[1]) {
                    int compoundPaddingLeft = getCompoundPaddingLeft();
                    scrollX += (((((this.mRight - this.mLeft) - getCompoundPaddingRight()) - compoundPaddingLeft) - drawables.mDrawableWidthTop) / 2) + compoundPaddingLeft;
                    scrollY += this.mPaddingTop;
                    handled = true;
                } else if (drawable == drawables.mShowing[3]) {
                    int compoundPaddingLeft2 = getCompoundPaddingLeft();
                    scrollX += (((((this.mRight - this.mLeft) - getCompoundPaddingRight()) - compoundPaddingLeft2) - drawables.mDrawableWidthBottom) / 2) + compoundPaddingLeft2;
                    scrollY += ((this.mBottom - this.mTop) - this.mPaddingBottom) - drawables.mDrawableSizeBottom;
                    handled = true;
                }
            }
            if (handled) {
                invalidate(dirty.left + scrollX, dirty.top + scrollY, dirty.right + scrollX, dirty.bottom + scrollY);
            }
        }
        if (!handled) {
            super.invalidateDrawable(drawable);
        }
    }

    public boolean hasOverlappingRendering() {
        return !(getBackground() == null || getBackground().getCurrent() == null) || this.mSpannable != null || hasSelection() || isHorizontalFadingEdgeEnabled();
    }

    public boolean isTextSelectable() {
        if (this.mEditor == null) {
            return false;
        }
        return this.mEditor.mTextIsSelectable;
    }

    public void setTextIsSelectable(boolean selectable) {
        if (selectable || this.mEditor != null) {
            createEditorIfNeeded();
            if (this.mEditor.mTextIsSelectable != selectable) {
                this.mEditor.mTextIsSelectable = selectable;
                setFocusableInTouchMode(selectable);
                setFocusable(16);
                setClickable(selectable);
                setLongClickable(selectable);
                setMovementMethod(selectable ? ArrowKeyMovementMethod.getInstance() : null);
                setText(this.mText, selectable ? BufferType.SPANNABLE : BufferType.NORMAL);
                this.mEditor.prepareCursorControllers();
            }
        }
    }

    /* access modifiers changed from: protected */
    public int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState;
        if (this.mSingleLine) {
            drawableState = super.onCreateDrawableState(extraSpace);
        } else {
            drawableState = super.onCreateDrawableState(extraSpace + 1);
            mergeDrawableStates(drawableState, MULTILINE_STATE_SET);
        }
        if (isTextSelectable()) {
            int length = drawableState.length;
            for (int i = 0; i < length; i++) {
                if (drawableState[i] == 16842919) {
                    int[] nonPressedState = new int[(length - 1)];
                    System.arraycopy(drawableState, 0, nonPressedState, 0, i);
                    System.arraycopy(drawableState, i + 1, nonPressedState, i, (length - i) - 1);
                    return nonPressedState;
                }
            }
        }
        return drawableState;
    }

    private Path getUpdatedHighlightPath() {
        Paint highlightPaint = this.mHighlightPaint;
        int selStart = getSelectionStart();
        int selEnd = getSelectionEnd();
        if (this.mMovement == null) {
            return null;
        }
        if ((!isFocused() && !isPressed()) || selStart < 0) {
            return null;
        }
        if (selStart != selEnd) {
            if (this.mHighlightPathBogus) {
                if (this.mHighlightPath == null) {
                    this.mHighlightPath = new Path();
                }
                this.mHighlightPath.reset();
                this.mLayout.getSelectionPath(selStart, selEnd, this.mHighlightPath);
                this.mHighlightPathBogus = false;
            }
            highlightPaint.setColor(this.mHighlightColor);
            highlightPaint.setStyle(Paint.Style.FILL);
            return this.mHighlightPath;
        } else if (this.mEditor == null || !this.mEditor.shouldRenderCursor()) {
            return null;
        } else {
            if (this.mHighlightPathBogus) {
                if (this.mHighlightPath == null) {
                    this.mHighlightPath = new Path();
                }
                this.mHighlightPath.reset();
                this.mLayout.getCursorPath(selStart, this.mHighlightPath, this.mText);
                this.mEditor.updateCursorPosition();
                this.mHighlightPathBogus = false;
            }
            highlightPaint.setColor(this.mCurTextColor);
            highlightPaint.setStyle(Paint.Style.STROKE);
            return this.mHighlightPath;
        }
    }

    public int getHorizontalOffsetForDrawables() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        int colorOri;
        int bottom;
        float clipTop;
        float clipTop2;
        float clipRight;
        float clipBottom;
        int voffsetCursor;
        int i;
        float clipBottom2;
        int layoutDirection;
        int color;
        int compoundPaddingLeft;
        Layout layout;
        int cursorOffsetVertical;
        Canvas canvas2 = canvas;
        restartMarqueeIfNeeded();
        super.onDraw(canvas);
        int compoundPaddingLeft2 = getCompoundPaddingLeft();
        int compoundPaddingTop = getCompoundPaddingTop();
        int compoundPaddingRight = getCompoundPaddingRight();
        int compoundPaddingBottom = getCompoundPaddingBottom();
        int scrollX = this.mScrollX;
        int scrollY = this.mScrollY;
        int right = this.mRight;
        int left = this.mLeft;
        int bottom2 = this.mBottom;
        int top = this.mTop;
        boolean isLayoutRtl = this.mTextViewDirection != 1;
        int offset = getHorizontalOffsetForDrawables();
        int leftOffset = isRtlLocale() ? 0 : offset;
        int rightOffset = isRtlLocale() ? offset : 0;
        Drawables dr = this.mDrawables;
        if (dr != null) {
            int vspace = ((bottom2 - top) - compoundPaddingBottom) - compoundPaddingTop;
            int hspace = ((right - left) - compoundPaddingRight) - compoundPaddingLeft2;
            if (dr.mShowing[0] != null) {
                canvas.save();
                canvas2.translate((float) (this.mPaddingLeft + scrollX + leftOffset), (float) (scrollY + compoundPaddingTop + ((vspace - dr.mDrawableHeightLeft) / 2)));
                dr.mShowing[0].draw(canvas2);
                canvas.restore();
            }
            if (dr.mShowing[2] != null) {
                canvas.save();
                canvas2.translate((float) (((((scrollX + right) - left) - this.mPaddingRight) - dr.mDrawableSizeRight) - rightOffset), (float) (scrollY + compoundPaddingTop + ((vspace - dr.mDrawableHeightRight) / 2)));
                dr.mShowing[2].draw(canvas2);
                canvas.restore();
            }
            if (dr.mShowing[1] != null) {
                canvas.save();
                canvas2.translate((float) (scrollX + compoundPaddingLeft2 + this.mHwCompoundPaddingLeft + ((hspace - dr.mDrawableWidthTop) / 2)), (float) (this.mPaddingTop + scrollY));
                dr.mShowing[1].draw(canvas2);
                canvas.restore();
            }
            if (dr.mShowing[3] != null) {
                canvas.save();
                canvas2.translate((float) (scrollX + compoundPaddingLeft2 + ((hspace - dr.mDrawableWidthBottom) / 2)), (float) ((((scrollY + bottom2) - top) - this.mPaddingBottom) - dr.mDrawableSizeBottom));
                dr.mShowing[3].draw(canvas2);
                canvas.restore();
            }
        }
        int color2 = this.mCurTextColor;
        if (this.mLayout == null) {
            assumeLayout();
        }
        Layout layout2 = this.mLayout;
        if (this.mHint != null && this.mText.length() == 0) {
            if (this.mHintTextColor != null) {
                color2 = this.mCurHintTextColor;
            }
            layout2 = this.mHintLayout;
        }
        Layout layout3 = layout2;
        int colorOri2 = color2;
        if (IS_AMOLED) {
            colorOri = colorOri2;
            if (color2 == -16777216 && this.isHwTheme && this.isSystemApp) {
                color2 = COLOR_FIX_TO;
            }
        } else {
            colorOri = colorOri2;
        }
        int color3 = color2;
        this.mTextPaint.setColor(color3);
        int color4 = color3;
        this.mTextPaint.drawableState = getDrawableState();
        canvas.save();
        int extendedPaddingTop = getExtendedPaddingTop();
        int extendedPaddingBottom = getExtendedPaddingBottom();
        int maxScrollY = this.mLayout.getHeight() - (((this.mBottom - this.mTop) - compoundPaddingBottom) - compoundPaddingTop);
        float clipLeft = (float) (compoundPaddingLeft2 + scrollX);
        int i2 = compoundPaddingTop;
        float clipTop3 = scrollY == 0 ? 0.0f : (float) (extendedPaddingTop + scrollY);
        Layout layout4 = layout3;
        float clipRight2 = (float) (((right - left) - getCompoundPaddingRight()) + scrollX);
        int maxScrollY2 = maxScrollY;
        float clipBottom3 = (float) (((bottom2 - top) + scrollY) - (scrollY == maxScrollY ? 0 : extendedPaddingBottom));
        Drawables dr2 = dr;
        int top2 = top;
        if (this.mShadowRadius != 0.0f) {
            bottom = bottom2;
            float clipLeft2 = clipLeft + Math.min(0.0f, this.mShadowDx - this.mShadowRadius);
            float clipRight3 = clipRight2 + Math.max(0.0f, this.mShadowDx + this.mShadowRadius);
            float clipTop4 = clipTop3 + Math.min(0.0f, this.mShadowDy - this.mShadowRadius);
            clipBottom = clipBottom3 + Math.max(0.0f, this.mShadowDy + this.mShadowRadius);
            clipRight = clipRight3;
            clipTop2 = clipTop4;
            clipTop = clipLeft2;
        } else {
            bottom = bottom2;
            clipBottom = clipBottom3;
            clipRight = clipRight2;
            clipTop2 = clipTop3;
            clipTop = clipLeft;
        }
        canvas2.clipRect(clipTop, clipTop2, clipRight, clipBottom);
        int voffsetText = 0;
        if ((this.mGravity & 112) != 48) {
            int voffsetText2 = getVerticalOffset(false);
            i = 1;
            voffsetCursor = getVerticalOffset(true);
            voffsetText = voffsetText2;
        } else {
            i = 1;
            voffsetCursor = 0;
        }
        canvas2.translate((float) compoundPaddingLeft2, (float) (extendedPaddingTop + voffsetText));
        int layoutDirection2 = this.mTextViewDirection == i ? 0 : 1;
        int absoluteGravity = Gravity.getAbsoluteGravity(this.mGravity, layoutDirection2);
        if (isMarqueeFadeEnabled()) {
            if (this.mSingleLine || getLineCount() != 1 || !canMarquee() || (absoluteGravity & 7) == 3) {
                layoutDirection = layoutDirection2;
                clipBottom2 = clipBottom;
            } else {
                int width = this.mRight - this.mLeft;
                layoutDirection = layoutDirection2;
                clipBottom2 = clipBottom;
                float dx = this.mLayout.getLineRight(0) - ((float) (width - (getCompoundPaddingLeft() + getCompoundPaddingRight())));
                int i3 = width;
                canvas2.translate(isLayoutRtl ? -dx : dx, 0.0f);
            }
            if (this.mMarquee != null && this.mMarquee.isRunning()) {
                float dx2 = -this.mMarquee.getScroll();
                canvas2.translate(isLayoutRtl ? -dx2 : dx2, 0.0f);
            }
        } else {
            layoutDirection = layoutDirection2;
            clipBottom2 = clipBottom;
        }
        int cursorOffsetVertical2 = voffsetCursor - voffsetText;
        Path highlight = getUpdatedHighlightPath();
        if (this.mEditor != null) {
            int i4 = compoundPaddingLeft2;
            float f = clipTop;
            compoundPaddingLeft = colorOri;
            color = color4;
            int i5 = maxScrollY2;
            Path highlight2 = highlight;
            Layout layout5 = layout4;
            int i6 = layoutDirection;
            int cursorOffsetVertical3 = cursorOffsetVertical2;
            Drawables drawables = dr2;
            float f2 = clipBottom2;
            float f3 = clipRight;
            int i7 = top2;
            float f4 = clipTop2;
            int i8 = bottom;
            this.mEditor.onDraw(canvas2, layout5, highlight2, this.mHighlightPaint, cursorOffsetVertical3);
            highlight = highlight2;
            layout = layout5;
            cursorOffsetVertical = cursorOffsetVertical3;
        } else {
            int cursorOffsetVertical4 = cursorOffsetVertical2;
            int i9 = compoundPaddingLeft2;
            float f5 = clipTop;
            compoundPaddingLeft = colorOri;
            color = color4;
            Layout layout6 = layout4;
            int i10 = maxScrollY2;
            Drawables drawables2 = dr2;
            int i11 = top2;
            int i12 = bottom;
            int i13 = layoutDirection;
            float f6 = clipBottom2;
            float f7 = clipRight;
            float f8 = clipTop2;
            layout = layout6;
            cursorOffsetVertical = cursorOffsetVertical4;
            layout.draw(canvas2, highlight, this.mHighlightPaint, cursorOffsetVertical);
        }
        if (this.mMarquee != null && this.mMarquee.shouldDrawGhost()) {
            float dx3 = this.mMarquee.getGhostOffset();
            canvas2.translate(isLayoutRtl ? -dx3 : dx3, 0.0f);
            layout.draw(canvas2, highlight, this.mHighlightPaint, cursorOffsetVertical);
        }
        if (IS_AMOLED && this.isHwTheme && this.isSystemApp && compoundPaddingLeft != color) {
            this.mTextPaint.setColor(compoundPaddingLeft);
        }
        canvas.restore();
    }

    public void getFocusedRect(Rect r) {
        if (this.mLayout == null) {
            super.getFocusedRect(r);
            return;
        }
        int selEnd = getSelectionEnd();
        if (selEnd < 0) {
            super.getFocusedRect(r);
            return;
        }
        int selStart = getSelectionStart();
        if (selStart < 0 || selStart >= selEnd) {
            int line = this.mLayout.getLineForOffset(selEnd);
            r.top = this.mLayout.getLineTop(line);
            r.bottom = this.mLayout.getLineBottom(line);
            r.left = ((int) this.mLayout.getPrimaryHorizontal(selEnd)) - 2;
            r.right = r.left + 4;
        } else {
            int lineStart = this.mLayout.getLineForOffset(selStart);
            int lineEnd = this.mLayout.getLineForOffset(selEnd);
            r.top = this.mLayout.getLineTop(lineStart);
            r.bottom = this.mLayout.getLineBottom(lineEnd);
            if (lineStart == lineEnd) {
                r.left = (int) this.mLayout.getPrimaryHorizontal(selStart);
                r.right = (int) this.mLayout.getPrimaryHorizontal(selEnd);
            } else {
                if (this.mHighlightPathBogus) {
                    if (this.mHighlightPath == null) {
                        this.mHighlightPath = new Path();
                    }
                    this.mHighlightPath.reset();
                    this.mLayout.getSelectionPath(selStart, selEnd, this.mHighlightPath);
                    this.mHighlightPathBogus = false;
                }
                synchronized (TEMP_RECTF) {
                    this.mHighlightPath.computeBounds(TEMP_RECTF, true);
                    r.left = ((int) TEMP_RECTF.left) - 1;
                    r.right = ((int) TEMP_RECTF.right) + 1;
                }
            }
        }
        int paddingLeft = getCompoundPaddingLeft();
        int paddingTop = getExtendedPaddingTop();
        if ((this.mGravity & 112) != 48) {
            paddingTop += getVerticalOffset(false);
        }
        r.offset(paddingLeft, paddingTop);
        r.bottom += getExtendedPaddingBottom();
    }

    public int getLineCount() {
        if (this.mLayout != null) {
            return this.mLayout.getLineCount();
        }
        return 0;
    }

    public int getLineBounds(int line, Rect bounds) {
        if (this.mLayout == null) {
            if (bounds != null) {
                bounds.set(0, 0, 0, 0);
            }
            return 0;
        }
        int baseline = this.mLayout.getLineBounds(line, bounds);
        int voffset = getExtendedPaddingTop();
        if ((this.mGravity & 112) != 48) {
            voffset += getVerticalOffset(true);
        }
        if (bounds != null) {
            bounds.offset(getCompoundPaddingLeft(), voffset);
        }
        return baseline + voffset;
    }

    public int getBaseline() {
        if (this.mLayout == null) {
            return super.getBaseline();
        }
        return getBaselineOffset() + this.mLayout.getLineBaseline(0);
    }

    /* access modifiers changed from: package-private */
    public int getBaselineOffset() {
        int voffset = 0;
        if ((this.mGravity & 112) != 48) {
            voffset = getVerticalOffset(true);
        }
        if (isLayoutModeOptical(this.mParent)) {
            voffset -= getOpticalInsets().top;
        }
        return getExtendedPaddingTop() + voffset;
    }

    /* access modifiers changed from: protected */
    public int getFadeTop(boolean offsetRequired) {
        if (this.mLayout == null) {
            return 0;
        }
        int voffset = 0;
        if ((this.mGravity & 112) != 48) {
            voffset = getVerticalOffset(true);
        }
        if (offsetRequired) {
            voffset += getTopPaddingOffset();
        }
        return getExtendedPaddingTop() + voffset;
    }

    /* access modifiers changed from: protected */
    public int getFadeHeight(boolean offsetRequired) {
        if (this.mLayout != null) {
            return this.mLayout.getHeight();
        }
        return 0;
    }

    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        if (this.mSpannable != null && this.mLinksClickable) {
            int offset = getOffsetForPosition(event.getX(pointerIndex), event.getY(pointerIndex));
            if (((ClickableSpan[]) this.mSpannable.getSpans(offset, offset, ClickableSpan.class)).length > 0) {
                return PointerIcon.getSystemIcon(this.mContext, 1002);
            }
        }
        if (isTextSelectable() || isTextEditable()) {
            return PointerIcon.getSystemIcon(this.mContext, 1008);
        }
        return super.onResolvePointerIcon(event, pointerIndex);
    }

    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode != 4 || !handleBackInTextActionModeIfNeeded(event)) {
            return super.onKeyPreIme(keyCode, event);
        }
        return true;
    }

    public boolean handleBackInTextActionModeIfNeeded(KeyEvent event) {
        if (this.mEditor == null || this.mEditor.getTextActionMode() == null) {
            return false;
        }
        if (event.getAction() == 0 && event.getRepeatCount() == 0) {
            KeyEvent.DispatcherState state = getKeyDispatcherState();
            if (state != null) {
                state.startTracking(event, this);
            }
            return true;
        }
        if (event.getAction() == 1) {
            KeyEvent.DispatcherState state2 = getKeyDispatcherState();
            if (state2 != null) {
                state2.handleUpEvent(event);
            }
            if (event.isTracking() && !event.isCanceled()) {
                stopTextActionMode();
                return true;
            }
        }
        return false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (doKeyDown(keyCode, event, null) == 0) {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        KeyEvent down = KeyEvent.changeAction(event, 0);
        int which = doKeyDown(keyCode, down, event);
        if (which == 0) {
            return super.onKeyMultiple(keyCode, repeatCount, event);
        }
        if (which == -1) {
            return true;
        }
        int repeatCount2 = repeatCount - 1;
        KeyEvent up = KeyEvent.changeAction(event, 1);
        if (which == 1) {
            this.mEditor.mKeyListener.onKeyUp(this, (Editable) this.mText, keyCode, up);
            while (true) {
                repeatCount2--;
                if (repeatCount2 <= 0) {
                    break;
                }
                this.mEditor.mKeyListener.onKeyDown(this, (Editable) this.mText, keyCode, down);
                this.mEditor.mKeyListener.onKeyUp(this, (Editable) this.mText, keyCode, up);
            }
            hideErrorIfUnchanged();
        } else if (which == 2) {
            this.mMovement.onKeyUp(this, this.mSpannable, keyCode, up);
            while (true) {
                repeatCount2--;
                if (repeatCount2 <= 0) {
                    break;
                }
                this.mMovement.onKeyDown(this, this.mSpannable, keyCode, down);
                this.mMovement.onKeyUp(this, this.mSpannable, keyCode, up);
            }
        }
        return true;
    }

    private boolean shouldAdvanceFocusOnEnter() {
        if (getKeyListener() == null) {
            return false;
        }
        if (this.mSingleLine) {
            return true;
        }
        if (this.mEditor != null && (this.mEditor.mInputType & 15) == 1) {
            int variation = this.mEditor.mInputType & InputType.TYPE_MASK_VARIATION;
            if (variation == 32 || variation == 48) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldAdvanceFocusOnTab() {
        if (getKeyListener() != null && !this.mSingleLine && this.mEditor != null && (this.mEditor.mInputType & 15) == 1) {
            int variation = this.mEditor.mInputType & InputType.TYPE_MASK_VARIATION;
            if (variation == 262144 || variation == 131072) {
                return false;
            }
        }
        return true;
    }

    private boolean isDirectionalNavigationKey(int keyCode) {
        switch (keyCode) {
            case 19:
            case 20:
            case 21:
            case 22:
                return true;
            default:
                return false;
        }
    }

    private int doKeyDown(int keyCode, KeyEvent event, KeyEvent otherEvent) {
        int i = 0;
        if (!isEnabled()) {
            return 0;
        }
        if (event.getRepeatCount() == 0 && !KeyEvent.isModifierKey(keyCode)) {
            this.mPreventDefaultMovement = false;
        }
        if (keyCode != 4) {
            if (keyCode != 23) {
                if (keyCode != 61) {
                    if (keyCode != 66) {
                        switch (keyCode) {
                            case 277:
                                if (event.hasNoModifiers() && canCut() && onTextContextMenuItem(ID_CUT)) {
                                    return -1;
                                }
                            case 278:
                                if (event.hasNoModifiers() && canCopy() && onTextContextMenuItem(ID_COPY)) {
                                    return -1;
                                }
                            case 279:
                                if (event.hasNoModifiers() && canPaste() && onTextContextMenuItem(ID_PASTE)) {
                                    return -1;
                                }
                        }
                    } else if (event.hasNoModifiers()) {
                        if (this.mEditor != null && this.mEditor.mInputContentType != null && this.mEditor.mInputContentType.onEditorActionListener != null && this.mEditor.mInputContentType.onEditorActionListener.onEditorAction(this, 0, event)) {
                            this.mEditor.mInputContentType.enterDown = true;
                            return -1;
                        } else if ((event.getFlags() & 16) != 0 || shouldAdvanceFocusOnEnter()) {
                            if (hasOnClickListeners()) {
                                return 0;
                            }
                            return -1;
                        }
                    }
                } else if ((event.hasNoModifiers() || event.hasModifiers(1)) && shouldAdvanceFocusOnTab()) {
                    return 0;
                }
            } else if (event.hasNoModifiers() && shouldAdvanceFocusOnEnter()) {
                return 0;
            }
        } else if (!(this.mEditor == null || this.mEditor.getTextActionMode() == null)) {
            stopTextActionMode();
            return -1;
        }
        if (!(this.mEditor == null || this.mEditor.mKeyListener == null)) {
            boolean doDown = true;
            if (otherEvent != null) {
                try {
                    beginBatchEdit();
                    boolean handled = this.mEditor.mKeyListener.onKeyOther(this, (Editable) this.mText, otherEvent);
                    hideErrorIfUnchanged();
                    doDown = false;
                    if (handled) {
                        endBatchEdit();
                        return -1;
                    }
                } catch (AbstractMethodError e) {
                } catch (Throwable th) {
                    endBatchEdit();
                    throw th;
                }
                endBatchEdit();
            }
            if (doDown) {
                beginBatchEdit();
                boolean handled2 = this.mEditor.mKeyListener.onKeyDown(this, (Editable) this.mText, keyCode, event);
                endBatchEdit();
                hideErrorIfUnchanged();
                if (handled2) {
                    return 1;
                }
            }
        }
        if (!(this.mMovement == null || this.mLayout == null)) {
            boolean doDown2 = true;
            if (otherEvent != null) {
                try {
                    doDown2 = false;
                    if (this.mMovement.onKeyOther(this, this.mSpannable, otherEvent)) {
                        return -1;
                    }
                } catch (AbstractMethodError e2) {
                }
            }
            if (doDown2) {
                this.mValidSetCursorEvent = true;
                if (this.mMovement.onKeyDown(this, this.mSpannable, keyCode, event)) {
                    if (event.getRepeatCount() == 0 && !KeyEvent.isModifierKey(keyCode)) {
                        this.mPreventDefaultMovement = true;
                    }
                    return 2;
                }
                this.mValidSetCursorEvent = false;
            }
            if (event.getSource() == 257 && isDirectionalNavigationKey(keyCode)) {
                return -1;
            }
        }
        if (this.mPreventDefaultMovement && !KeyEvent.isModifierKey(keyCode)) {
            i = -1;
        }
        return i;
    }

    public void resetErrorChangedFlag() {
        if (this.mEditor != null) {
            this.mEditor.mErrorWasChanged = false;
        }
    }

    public void hideErrorIfUnchanged() {
        if (this.mEditor != null && this.mEditor.mError != null && !this.mEditor.mErrorWasChanged) {
            setError(null, null);
        }
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!isEnabled()) {
            return super.onKeyUp(keyCode, event);
        }
        if (!KeyEvent.isModifierKey(keyCode)) {
            this.mPreventDefaultMovement = false;
        }
        if (keyCode == 23) {
            if (event.hasNoModifiers() && !hasOnClickListeners() && this.mMovement != null && (this.mText instanceof Editable) && this.mLayout != null && onCheckIsTextEditor()) {
                InputMethodManager imm = InputMethodManager.peekInstance();
                viewClicked(imm);
                if (imm != null && getShowSoftInputOnFocus()) {
                    imm.showSoftInput(this, 0);
                }
            }
            return super.onKeyUp(keyCode, event);
        } else if (keyCode == 66 && event.hasNoModifiers()) {
            if (!(this.mEditor == null || this.mEditor.mInputContentType == null || this.mEditor.mInputContentType.onEditorActionListener == null || !this.mEditor.mInputContentType.enterDown)) {
                this.mEditor.mInputContentType.enterDown = false;
                if (this.mEditor.mInputContentType.onEditorActionListener.onEditorAction(this, 0, event)) {
                    return true;
                }
            }
            if (((event.getFlags() & 16) != 0 || shouldAdvanceFocusOnEnter()) && !hasOnClickListeners()) {
                View v = focusSearch(130);
                if (v != null) {
                    if (v.requestFocus(130)) {
                        super.onKeyUp(keyCode, event);
                        return true;
                    }
                    throw new IllegalStateException("focus search returned a view that wasn't able to take focus!");
                } else if ((event.getFlags() & 16) != 0) {
                    InputMethodManager imm2 = InputMethodManager.peekInstance();
                    if (imm2 != null && imm2.isActive(this)) {
                        imm2.hideSoftInputFromWindow(getWindowToken(), 0);
                    }
                }
            }
            return super.onKeyUp(keyCode, event);
        } else if (this.mEditor != null && this.mEditor.mKeyListener != null && this.mEditor.mKeyListener.onKeyUp(this, (Editable) this.mText, keyCode, event)) {
            return true;
        } else {
            if (this.mMovement == null || this.mLayout == null || !this.mMovement.onKeyUp(this, this.mSpannable, keyCode, event)) {
                return super.onKeyUp(keyCode, event);
            }
            return true;
        }
    }

    public boolean onCheckIsTextEditor() {
        return (this.mEditor == null || this.mEditor.mInputType == 0) ? false : true;
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if (onCheckIsTextEditor() && isEnabled()) {
            this.mEditor.createInputMethodStateIfNeeded();
            outAttrs.inputType = getInputType();
            if (this.mEditor.mInputContentType != null) {
                outAttrs.imeOptions = this.mEditor.mInputContentType.imeOptions;
                outAttrs.privateImeOptions = this.mEditor.mInputContentType.privateImeOptions;
                outAttrs.actionLabel = this.mEditor.mInputContentType.imeActionLabel;
                outAttrs.actionId = this.mEditor.mInputContentType.imeActionId;
                outAttrs.extras = this.mEditor.mInputContentType.extras;
                outAttrs.hintLocales = this.mEditor.mInputContentType.imeHintLocales;
            } else {
                outAttrs.imeOptions = 0;
                outAttrs.hintLocales = null;
            }
            if (focusSearch(130) != null) {
                outAttrs.imeOptions |= 134217728;
            }
            if (focusSearch(33) != null) {
                outAttrs.imeOptions |= 67108864;
            }
            if ((outAttrs.imeOptions & 255) == 0) {
                if ((outAttrs.imeOptions & 134217728) != 0) {
                    outAttrs.imeOptions |= 5;
                } else {
                    outAttrs.imeOptions |= 6;
                }
                if (!shouldAdvanceFocusOnEnter()) {
                    outAttrs.imeOptions |= 1073741824;
                }
            }
            if (isMultilineInputType(outAttrs.inputType)) {
                outAttrs.imeOptions |= 1073741824;
            }
            outAttrs.hintText = this.mHint;
            if (this.mText instanceof Editable) {
                InputConnection ic = new EditableInputConnection(this);
                outAttrs.initialSelStart = getSelectionStart();
                outAttrs.initialSelEnd = getSelectionEnd();
                outAttrs.initialCapsMode = ic.getCursorCapsMode(getInputType());
                return ic;
            }
        }
        return null;
    }

    public boolean extractText(ExtractedTextRequest request, ExtractedText outText) {
        createEditorIfNeeded();
        return this.mEditor.extractText(request, outText);
    }

    static void removeParcelableSpans(Spannable spannable, int start, int end) {
        Object[] spans = spannable.getSpans(start, end, ParcelableSpan.class);
        int i = spans.length;
        while (i > 0) {
            i--;
            spannable.removeSpan(spans[i]);
        }
    }

    public void setExtractedText(ExtractedText text) {
        Editable content = getEditableText();
        if (text.text != null) {
            if (content == null) {
                setText(text.text, BufferType.EDITABLE);
            } else {
                int start = 0;
                int end = content.length();
                if (text.partialStartOffset >= 0) {
                    int N = content.length();
                    start = text.partialStartOffset;
                    if (start > N) {
                        start = N;
                    }
                    end = text.partialEndOffset;
                    if (end > N) {
                        end = N;
                    }
                }
                int start2 = start;
                int end2 = end;
                removeParcelableSpans(content, start2, end2);
                if (!TextUtils.equals(content.subSequence(start2, end2), text.text)) {
                    content.replace(start2, end2, text.text);
                } else if (text.text instanceof Spanned) {
                    TextUtils.copySpansFrom((Spanned) text.text, 0, end2 - start2, Object.class, content, start2);
                }
            }
        }
        Spannable sp = (Spannable) getText();
        int N2 = sp.length();
        int start3 = text.selectionStart;
        if (start3 < 0) {
            start3 = 0;
        } else if (start3 > N2) {
            start3 = N2;
        }
        int end3 = text.selectionEnd;
        if (end3 < 0) {
            end3 = 0;
        } else if (end3 > N2) {
            end3 = N2;
        }
        Selection.setSelection(sp, start3, end3);
        if ((text.flags & 2) != 0) {
            MetaKeyKeyListener.startSelecting(this, sp);
        } else {
            MetaKeyKeyListener.stopSelecting(this, sp);
        }
        setHintInternal(text.hint);
    }

    public void setExtracting(ExtractedTextRequest req) {
        if (this.mEditor.mInputMethodState != null) {
            this.mEditor.mInputMethodState.mExtractedTextRequest = req;
        }
        this.mEditor.hideCursorAndSpanControllers();
        stopTextActionMode();
        if (this.mEditor.mSelectionModifierCursorController != null) {
            this.mEditor.mSelectionModifierCursorController.resetTouchOffsets();
        }
    }

    public void onCommitCompletion(CompletionInfo text) {
    }

    public void onCommitCorrection(CorrectionInfo info) {
        if (this.mEditor != null) {
            this.mEditor.onCommitCorrection(info);
        }
    }

    public void beginBatchEdit() {
        if (this.mEditor != null) {
            this.mEditor.beginBatchEdit();
        }
    }

    public void endBatchEdit() {
        if (this.mEditor != null) {
            this.mEditor.endBatchEdit();
        }
    }

    public void onBeginBatchEdit() {
    }

    public void onEndBatchEdit() {
    }

    public boolean onPrivateIMECommand(String action, Bundle data) {
        if (!"com.huawei.inputmethod.intelligent.INPUT_COMPOSING".equals(action) || data == null || this.mFilters == null) {
            return false;
        }
        boolean disableLengthFilter = data.getBoolean("intelligent_composing");
        for (int i = 0; i < this.mFilters.length; i++) {
            if (this.mFilters[i] instanceof InputFilter.LengthFilter) {
                InputFilter.LengthFilter lf = (InputFilter.LengthFilter) this.mFilters[i];
                if (disableLengthFilter) {
                    lf.disableFilter();
                } else {
                    lf.enableFilter();
                }
            }
        }
        return true;
    }

    public boolean onCommitText(CharSequence text, int newCursorPosition) {
        enableLengthFilter();
        return false;
    }

    public boolean onFinishComposingText() {
        enableLengthFilter();
        return false;
    }

    private void enableLengthFilter() {
        if (this.mFilters != null) {
            for (int i = 0; i < this.mFilters.length; i++) {
                if (this.mFilters[i] instanceof InputFilter.LengthFilter) {
                    ((InputFilter.LengthFilter) this.mFilters[i]).enableFilter();
                }
            }
        }
    }

    @VisibleForTesting
    public void nullLayouts() {
        if ((this.mLayout instanceof BoringLayout) && this.mSavedLayout == null) {
            this.mSavedLayout = (BoringLayout) this.mLayout;
        }
        if ((this.mHintLayout instanceof BoringLayout) && this.mSavedHintLayout == null) {
            this.mSavedHintLayout = (BoringLayout) this.mHintLayout;
        }
        this.mHintLayout = null;
        this.mLayout = null;
        this.mSavedMarqueeModeLayout = null;
        this.mHintBoring = null;
        this.mBoring = null;
        if (this.mEditor != null) {
            this.mEditor.prepareCursorControllers();
        }
    }

    private void assumeLayout() {
        int width = ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        if (width < 1) {
            width = 0;
        }
        int physicalWidth = width;
        if (this.mHorizontallyScrolling) {
            width = 1048576;
        }
        makeNewLayout(width, physicalWidth, UNKNOWN_BORING, UNKNOWN_BORING, physicalWidth, false);
    }

    private Layout.Alignment getLayoutAlignment() {
        switch (getTextAlignment()) {
            case 1:
                int i = this.mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
                if (i == 1) {
                    return Layout.Alignment.ALIGN_CENTER;
                }
                if (i == 3) {
                    return Layout.Alignment.ALIGN_LEFT;
                }
                if (i == 5) {
                    return Layout.Alignment.ALIGN_RIGHT;
                }
                if (i == 8388611) {
                    return Layout.Alignment.ALIGN_NORMAL;
                }
                if (i != 8388613) {
                    return Layout.Alignment.ALIGN_NORMAL;
                }
                return Layout.Alignment.ALIGN_OPPOSITE;
            case 2:
                return Layout.Alignment.ALIGN_NORMAL;
            case 3:
                return Layout.Alignment.ALIGN_OPPOSITE;
            case 4:
                return Layout.Alignment.ALIGN_CENTER;
            case 5:
                return getLayoutDirection() == 1 ? Layout.Alignment.ALIGN_RIGHT : Layout.Alignment.ALIGN_LEFT;
            case 6:
                return getLayoutDirection() == 1 ? Layout.Alignment.ALIGN_LEFT : Layout.Alignment.ALIGN_RIGHT;
            default:
                return Layout.Alignment.ALIGN_NORMAL;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:110:0x0221, code lost:
        if (r21 != r14.mLayout.getParagraphDirection(r3 ? 1 : 0)) goto L_0x0229;
     */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x024d  */
    /* JADX WARNING: Removed duplicated region for block: B:128:? A[RETURN, SYNTHETIC] */
    @VisibleForTesting
    public void makeNewLayout(int wantWidth, int hintWidth, BoringLayout.Metrics boring, BoringLayout.Metrics hintBoring, int ellipsisWidth, boolean bringIntoView) {
        int oldDir;
        boolean z;
        Layout.Alignment alignment;
        boolean z2;
        int oldDir2;
        boolean shouldEllipsize;
        int height;
        BoringLayout.Metrics hintBoring2;
        boolean shouldEllipsize2;
        Layout.Alignment alignment2;
        boolean z3;
        Layout.Alignment alignment3;
        int oldDir3;
        boolean z4;
        int oldDir4 = ellipsisWidth;
        stopMarquee();
        this.mOldMaximum = this.mMaximum;
        this.mOldMaxMode = this.mMaxMode;
        this.mHighlightPathBogus = true;
        int wantWidth2 = wantWidth < 0 ? 0 : wantWidth;
        int hintWidth2 = hintWidth < 0 ? 0 : hintWidth;
        Layout.Alignment alignment4 = getLayoutAlignment();
        boolean testDirChange = this.mSingleLine && this.mLayout != null && (alignment4 == Layout.Alignment.ALIGN_NORMAL || alignment4 == Layout.Alignment.ALIGN_OPPOSITE);
        int oldDir5 = 0;
        if (testDirChange) {
            oldDir5 = this.mLayout.getParagraphDirection(0);
        }
        int oldDir6 = oldDir5;
        boolean shouldEllipsize3 = this.mEllipsize != null && getKeyListener() == null;
        boolean switchEllipsize = this.mEllipsize == TextUtils.TruncateAt.MARQUEE && this.mMarqueeFadeMode != 0;
        TextUtils.TruncateAt effectiveEllipsize = this.mEllipsize;
        if (this.mEllipsize == TextUtils.TruncateAt.MARQUEE && this.mMarqueeFadeMode == 1) {
            effectiveEllipsize = TextUtils.TruncateAt.END_SMALL;
        }
        TextUtils.TruncateAt effectiveEllipsize2 = effectiveEllipsize;
        if (this.mTextDir == null) {
            this.mTextDir = getTextDirectionHeuristic();
        }
        this.mLayout = makeSingleLayout(wantWidth2, boring, oldDir4, alignment4, shouldEllipsize3, effectiveEllipsize2, effectiveEllipsize2 == this.mEllipsize);
        if (switchEllipsize) {
            TextUtils.TruncateAt truncateAt = effectiveEllipsize2;
            oldDir = oldDir6;
            z = false;
            alignment = alignment4;
            z2 = true;
            this.mSavedMarqueeModeLayout = makeSingleLayout(wantWidth2, boring, oldDir4, alignment4, shouldEllipsize3, effectiveEllipsize2 == TextUtils.TruncateAt.MARQUEE ? TextUtils.TruncateAt.END : TextUtils.TruncateAt.MARQUEE, effectiveEllipsize2 != this.mEllipsize);
        } else {
            oldDir = oldDir6;
            z = false;
            alignment = alignment4;
            z2 = true;
        }
        boolean shouldEllipsize4 = this.mEllipsize != null ? z2 : z;
        this.mHintLayout = null;
        if (this.mHint != null) {
            int hintWidth3 = shouldEllipsize4 ? wantWidth2 : hintWidth2;
            BoringLayout.Metrics metrics = hintBoring;
            if (metrics == UNKNOWN_BORING) {
                BoringLayout.Metrics hintBoring3 = BoringLayout.isBoring(this.mHint, this.mTextPaint, this.mTextDir, this.mHintBoring);
                if (hintBoring3 != null) {
                    this.mHintBoring = hintBoring3;
                }
                hintBoring2 = hintBoring3;
            } else {
                hintBoring2 = metrics;
            }
            if (hintBoring2 != null) {
                if (hintBoring2.width > hintWidth3) {
                    z3 = z2;
                    alignment3 = alignment;
                    oldDir3 = oldDir;
                } else if (!shouldEllipsize4 || hintBoring2.width <= oldDir4) {
                    if (this.mSavedHintLayout != null) {
                        z4 = z2;
                        Layout.Alignment alignment5 = alignment;
                        oldDir2 = oldDir;
                        alignment2 = alignment5;
                        boolean z5 = z;
                        this.mHintLayout = this.mSavedHintLayout.replaceOrMake(this.mHint, this.mTextPaint, hintWidth3, alignment5, this.mSpacingMult, this.mSpacingAdd, hintBoring2, this.mIncludePad);
                    } else {
                        z4 = z2;
                        alignment2 = alignment;
                        oldDir2 = oldDir;
                        this.mHintLayout = BoringLayout.make(this.mHint, this.mTextPaint, hintWidth3, alignment2, this.mSpacingMult, this.mSpacingAdd, hintBoring2, this.mIncludePad);
                    }
                    this.mSavedHintLayout = (BoringLayout) this.mHintLayout;
                    shouldEllipsize2 = shouldEllipsize4;
                    shouldEllipsize = z4;
                } else {
                    z3 = z2;
                    alignment3 = alignment;
                    oldDir3 = oldDir;
                }
                if (!shouldEllipsize4 || hintBoring2.width > hintWidth3) {
                    shouldEllipsize2 = shouldEllipsize4;
                    shouldEllipsize = z3;
                } else if (this.mSavedHintLayout != null) {
                    shouldEllipsize2 = shouldEllipsize4;
                    shouldEllipsize = z3;
                    this.mHintLayout = this.mSavedHintLayout.replaceOrMake(this.mHint, this.mTextPaint, hintWidth3, alignment2, this.mSpacingMult, this.mSpacingAdd, hintBoring2, this.mIncludePad, this.mEllipsize, oldDir4);
                } else {
                    shouldEllipsize2 = shouldEllipsize4;
                    shouldEllipsize = z3;
                    this.mHintLayout = BoringLayout.make(this.mHint, this.mTextPaint, hintWidth3, alignment2, this.mSpacingMult, this.mSpacingAdd, hintBoring2, this.mIncludePad, this.mEllipsize, oldDir4);
                }
            } else {
                alignment2 = alignment;
                oldDir2 = oldDir;
                shouldEllipsize2 = shouldEllipsize4;
                shouldEllipsize = z2;
            }
            if (this.mHintLayout == null) {
                z = false;
                StaticLayout.Builder builder = StaticLayout.Builder.obtain(this.mHint, 0, this.mHint.length(), this.mTextPaint, hintWidth3).setAlignment(alignment2).setTextDirection(this.mTextDir).setLineSpacing(this.mSpacingAdd, this.mSpacingMult).setIncludePad(this.mIncludePad).setUseLineSpacingFromFallbacks(this.mUseFallbackLineSpacing).setBreakStrategy(this.mBreakStrategy).setHyphenationFrequency(this.mHyphenationFrequency).setJustificationMode(this.mJustificationMode).setMaxLines(this.mMaxMode == shouldEllipsize ? this.mMaximum : Integer.MAX_VALUE);
                if (shouldEllipsize2) {
                    builder.setEllipsize(this.mEllipsize).setEllipsizedWidth(oldDir4);
                }
                this.mHintLayout = builder.build();
            } else {
                z = false;
            }
        } else {
            oldDir2 = oldDir;
            boolean z6 = shouldEllipsize4;
            shouldEllipsize = z2;
            Layout.Alignment alignment6 = alignment;
            BoringLayout.Metrics metrics2 = hintBoring;
            int i = hintWidth2;
        }
        if (!bringIntoView) {
            if (!testDirChange) {
            }
            if (this.mEllipsize == TextUtils.TruncateAt.MARQUEE && !compressText((float) oldDir4)) {
                height = this.mLayoutParams.height;
                if (height != -2 || height == -1) {
                    this.mRestartMarquee = shouldEllipsize;
                } else {
                    startMarquee();
                }
            }
            if (this.mEditor == null) {
                this.mEditor.prepareCursorControllers();
                return;
            }
            return;
        }
        registerForPreDraw();
        height = this.mLayoutParams.height;
        if (height != -2) {
        }
        this.mRestartMarquee = shouldEllipsize;
        if (this.mEditor == null) {
        }
    }

    @VisibleForTesting
    public boolean useDynamicLayout() {
        return isTextSelectable() || (this.mSpannable != null && this.mPrecomputed == null);
    }

    /* access modifiers changed from: protected */
    public Layout makeSingleLayout(int wantWidth, BoringLayout.Metrics boring, int ellipsisWidth, Layout.Alignment alignment, boolean shouldEllipsize, TextUtils.TruncateAt effectiveEllipsize, boolean useSaved) {
        BoringLayout.Metrics boring2;
        Layout result;
        int i = wantWidth;
        int i2 = ellipsisWidth;
        Layout.Alignment alignment2 = alignment;
        TextUtils.TruncateAt truncateAt = effectiveEllipsize;
        Layout result2 = null;
        if (useDynamicLayout()) {
            result2 = DynamicLayout.Builder.obtain(this.mText, this.mTextPaint, i).setDisplayText(this.mTransformed).setAlignment(alignment2).setTextDirection(this.mTextDir).setLineSpacing(this.mSpacingAdd, this.mSpacingMult).setIncludePad(this.mIncludePad).setUseLineSpacingFromFallbacks(this.mUseFallbackLineSpacing).setBreakStrategy(this.mBreakStrategy).setHyphenationFrequency(this.mHyphenationFrequency).setJustificationMode(this.mJustificationMode).setEllipsize(getKeyListener() == null ? truncateAt : null).setEllipsizedWidth(i2).build();
            BoringLayout.Metrics metrics = boring;
        } else {
            BoringLayout.Metrics metrics2 = boring;
            if (metrics2 == UNKNOWN_BORING) {
                BoringLayout.Metrics boring3 = BoringLayout.isBoring(this.mTransformed, this.mTextPaint, this.mTextDir, this.mBoring);
                if (boring3 != null) {
                    this.mBoring = boring3;
                }
                boring2 = boring3;
            } else {
                boring2 = metrics2;
            }
            if (boring2 != null) {
                if (boring2.width <= i && (truncateAt == null || boring2.width <= i2)) {
                    if (!useSaved || this.mSavedLayout == null) {
                        result = BoringLayout.make(this.mTransformed, this.mTextPaint, i, alignment2, this.mSpacingMult, this.mSpacingAdd, boring2, this.mIncludePad);
                    } else {
                        result = this.mSavedLayout.replaceOrMake(this.mTransformed, this.mTextPaint, i, alignment2, this.mSpacingMult, this.mSpacingAdd, boring2, this.mIncludePad);
                    }
                    result2 = result;
                    if (useSaved) {
                        this.mSavedLayout = (BoringLayout) result2;
                    }
                    BoringLayout.Metrics metrics3 = boring2;
                } else if (shouldEllipsize && boring2.width <= i) {
                    if (!useSaved || this.mSavedLayout == null) {
                        result2 = BoringLayout.make(this.mTransformed, this.mTextPaint, i, alignment2, this.mSpacingMult, this.mSpacingAdd, boring2, this.mIncludePad, effectiveEllipsize, i2);
                    } else {
                        BoringLayout.Metrics metrics4 = boring2;
                        result2 = this.mSavedLayout.replaceOrMake(this.mTransformed, this.mTextPaint, i, alignment2, this.mSpacingMult, this.mSpacingAdd, boring2, this.mIncludePad, truncateAt, i2);
                    }
                }
            }
        }
        if (result2 == null) {
            StaticLayout.Builder builder = StaticLayout.Builder.obtain(this.mTransformed, 0, this.mTransformed.length(), this.mTextPaint, i).setAlignment(alignment2).setTextDirection(this.mTextDir).setLineSpacing(this.mSpacingAdd, this.mSpacingMult).setIncludePad(this.mIncludePad).setUseLineSpacingFromFallbacks(this.mUseFallbackLineSpacing).setBreakStrategy(this.mBreakStrategy).setHyphenationFrequency(this.mHyphenationFrequency).setJustificationMode(this.mJustificationMode).setMaxLines(this.mMaxMode == 1 ? this.mMaximum : Integer.MAX_VALUE);
            if (shouldEllipsize) {
                builder.setEllipsize(effectiveEllipsize).setEllipsizedWidth(i2);
            } else {
                TextUtils.TruncateAt truncateAt2 = effectiveEllipsize;
            }
            return builder.build();
        }
        TextUtils.TruncateAt truncateAt3 = effectiveEllipsize;
        return result2;
    }

    private boolean compressText(float width) {
        if (!isHardwareAccelerated() && width > 0.0f && this.mLayout != null && getLineCount() == 1 && !this.mUserSetTextScaleX && this.mTextPaint.getTextScaleX() == 1.0f) {
            float overflow = ((this.mLayout.getLineWidth(0) + 1.0f) - width) / width;
            if (overflow > 0.0f && overflow <= 0.07f) {
                this.mTextPaint.setTextScaleX((1.0f - overflow) - 0.005f);
                post(new Runnable() {
                    public void run() {
                        TextView.this.requestLayout();
                    }
                });
                return true;
            }
        }
        return false;
    }

    private static int desired(Layout layout) {
        int n = layout.getLineCount();
        CharSequence text = layout.getText();
        float max = 0.0f;
        for (int i = 0; i < n - 1; i++) {
            if (text.charAt(layout.getLineEnd(i) - 1) != 10) {
                return -1;
            }
        }
        for (int i2 = 0; i2 < n; i2++) {
            max = Math.max(max, layout.getLineWidth(i2));
        }
        return (int) Math.ceil((double) max);
    }

    public void setIncludeFontPadding(boolean includepad) {
        if (this.mIncludePad != includepad) {
            this.mIncludePad = includepad;
            if (this.mLayout != null) {
                nullLayouts();
                requestLayout();
                invalidate();
            }
        }
    }

    public boolean getIncludeFontPadding() {
        return this.mIncludePad;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean fromexisting;
        BoringLayout.Metrics hintBoring;
        BoringLayout.Metrics boring;
        int width;
        int des;
        int unpaddedWidth;
        int unpaddedWidth2;
        int height;
        int hintWant;
        boolean z;
        boolean maximumChanged;
        int width2;
        int width3;
        int width4;
        int hintWidth;
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        BoringLayout.Metrics boring2 = UNKNOWN_BORING;
        BoringLayout.Metrics hintBoring2 = UNKNOWN_BORING;
        if (this.mTextDir == null) {
            this.mTextDir = getTextDirectionHeuristic();
        }
        int des2 = -1;
        boolean fromexisting2 = false;
        float widthLimit = widthMode == Integer.MIN_VALUE ? (float) widthSize : Float.MAX_VALUE;
        if (widthMode == 1073741824) {
            boring = boring2;
            hintBoring = hintBoring2;
            des = -1;
            fromexisting = false;
            width = widthSize;
        } else {
            if (this.mLayout != null && this.mEllipsize == null) {
                des2 = desired(this.mLayout);
            }
            if (des2 < 0) {
                boring2 = BoringLayout.isBoring(this.mTransformed, this.mTextPaint, this.mTextDir, this.mBoring);
                if (boring2 != null) {
                    this.mBoring = boring2;
                }
            } else {
                fromexisting2 = true;
            }
            if (boring2 == null || boring2 == UNKNOWN_BORING) {
                if (des2 < 0) {
                    des2 = (int) Math.ceil((double) Layout.getDesiredWidthWithLimit(this.mTransformed, 0, this.mTransformed.length(), this.mTextPaint, this.mTextDir, widthLimit));
                }
                width2 = des2;
            } else {
                width2 = boring2.width;
            }
            Drawables dr = this.mDrawables;
            if (dr != null) {
                width2 = Math.max(Math.max(width2, dr.mDrawableWidthTop), dr.mDrawableWidthBottom);
            }
            if (this.mHint != null) {
                int hintDes = -1;
                if (this.mHintLayout != null && this.mEllipsize == null) {
                    hintDes = desired(this.mHintLayout);
                }
                if (hintDes < 0) {
                    hintBoring2 = BoringLayout.isBoring(this.mHint, this.mTextPaint, this.mTextDir, this.mHintBoring);
                    if (hintBoring2 != null) {
                        this.mHintBoring = hintBoring2;
                    }
                }
                if (hintBoring2 == null || hintBoring2 == UNKNOWN_BORING) {
                    if (hintDes < 0) {
                        hintDes = (int) Math.ceil((double) Layout.getDesiredWidthWithLimit(this.mHint, 0, this.mHint.length(), this.mTextPaint, this.mTextDir, widthLimit));
                    }
                    hintWidth = hintDes;
                } else {
                    hintWidth = hintBoring2.width;
                }
                if (hintWidth > width2) {
                    width2 = hintWidth;
                }
            }
            int width5 = width2 + getCompoundPaddingLeft() + getCompoundPaddingRight();
            if (this.mMaxWidthMode == 1) {
                width3 = Math.min(width5, this.mMaxWidth * getLineHeight());
            } else {
                width3 = Math.min(width5, this.mMaxWidth);
            }
            if (this.mMinWidthMode == 1) {
                width4 = Math.max(width3, this.mMinWidth * getLineHeight());
            } else {
                width4 = Math.max(width3, this.mMinWidth);
            }
            int width6 = Math.max(width4, getSuggestedMinimumWidth());
            if (widthMode == Integer.MIN_VALUE) {
                width6 = Math.min(widthSize, width6);
            }
            boring = boring2;
            hintBoring = hintBoring2;
            des = des2;
            fromexisting = fromexisting2;
            width = width6;
        }
        int want = (width - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        int unpaddedWidth3 = want;
        if (this.mHorizontallyScrolling) {
            want = 1048576;
        }
        int want2 = want;
        int hintWant2 = want2;
        int hintWidth2 = this.mHintLayout == null ? hintWant2 : this.mHintLayout.getWidth();
        if (this.mLayout == null) {
            int i = hintWidth2;
            int i2 = widthMode;
            int i3 = widthSize;
            int widthSize2 = des;
            unpaddedWidth = unpaddedWidth3;
            unpaddedWidth2 = 1073741824;
            makeNewLayout(want2, hintWant2, boring, hintBoring, (width - getCompoundPaddingLeft()) - getCompoundPaddingRight(), false);
            int i4 = hintWant2;
            int i5 = want2;
        } else {
            int hintWidth3 = hintWidth2;
            int hintWant3 = hintWant2;
            unpaddedWidth = unpaddedWidth3;
            int i6 = widthMode;
            int i7 = widthSize;
            unpaddedWidth2 = 1073741824;
            int widthSize3 = des;
            int want3 = want2;
            if (this.mLayout.getWidth() == want3) {
                hintWant = hintWant3;
                if (hintWidth3 == hintWant && this.mLayout.getEllipsizedWidth() == (width - getCompoundPaddingLeft()) - getCompoundPaddingRight()) {
                    z = false;
                    boolean layoutChanged = z;
                    boolean widthChanged = this.mHint != null && this.mEllipsize == null && want3 > this.mLayout.getWidth() && ((this.mLayout instanceof BoringLayout) || (fromexisting && widthSize3 >= 0 && widthSize3 <= want3));
                    maximumChanged = this.mMaxMode == this.mOldMaxMode || this.mMaximum != this.mOldMaximum;
                    if (layoutChanged || maximumChanged) {
                        if (!maximumChanged || !widthChanged) {
                            int i8 = hintWant;
                            int i9 = want3;
                            makeNewLayout(want3, hintWant, boring, hintBoring, (width - getCompoundPaddingLeft()) - getCompoundPaddingRight(), false);
                        } else {
                            this.mLayout.increaseWidthTo(want3);
                        }
                    }
                }
            } else {
                hintWant = hintWant3;
            }
            z = true;
            boolean layoutChanged2 = z;
            boolean widthChanged2 = this.mHint != null && this.mEllipsize == null && want3 > this.mLayout.getWidth() && ((this.mLayout instanceof BoringLayout) || (fromexisting && widthSize3 >= 0 && widthSize3 <= want3));
            maximumChanged = this.mMaxMode == this.mOldMaxMode || this.mMaximum != this.mOldMaximum;
            if (!maximumChanged) {
            }
            int i82 = hintWant;
            int i92 = want3;
            makeNewLayout(want3, hintWant, boring, hintBoring, (width - getCompoundPaddingLeft()) - getCompoundPaddingRight(), false);
        }
        if (heightMode == unpaddedWidth2) {
            height = heightSize;
            this.mDesiredHeightAtMeasure = -1;
        } else {
            int desired = getDesiredHeight();
            int height2 = desired;
            this.mDesiredHeightAtMeasure = desired;
            if (heightMode == Integer.MIN_VALUE) {
                height = Math.min(desired, heightSize);
            } else {
                height = height2;
            }
        }
        int unpaddedHeight = (height - getCompoundPaddingTop()) - getCompoundPaddingBottom();
        if (this.mMaxMode == 1 && this.mLayout.getLineCount() > this.mMaximum) {
            unpaddedHeight = Math.min(unpaddedHeight, this.mLayout.getLineTop(this.mMaximum));
        }
        if (this.mMovement != null) {
        } else if (this.mLayout.getWidth() <= unpaddedWidth && this.mLayout.getHeight() <= unpaddedHeight) {
            scrollTo(0, 0);
            setMeasuredDimension(width, height);
        }
        registerForPreDraw();
        setMeasuredDimension(width, height);
    }

    private void autoSizeText() {
        int availableWidth;
        if (isAutoSizeEnabled()) {
            if (this.mNeedsAutoSizeText) {
                if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0) {
                    if (this.mHorizontallyScrolling) {
                        availableWidth = 1048576;
                    } else {
                        availableWidth = (getMeasuredWidth() - getTotalPaddingLeft()) - getTotalPaddingRight();
                    }
                    int availableHeight = (getMeasuredHeight() - getExtendedPaddingBottom()) - getExtendedPaddingTop();
                    if (availableWidth > 0 && availableHeight > 0) {
                        synchronized (TEMP_RECTF) {
                            TEMP_RECTF.setEmpty();
                            TEMP_RECTF.right = (float) availableWidth;
                            TEMP_RECTF.bottom = (float) availableHeight;
                            float optimalTextSize = (float) findLargestTextSizeWhichFits(TEMP_RECTF);
                            if (optimalTextSize != getTextSize()) {
                                setTextSizeInternal(0, optimalTextSize, false);
                                makeNewLayout(availableWidth, 0, UNKNOWN_BORING, UNKNOWN_BORING, ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight(), false);
                            }
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
            this.mNeedsAutoSizeText = true;
        }
    }

    private int findLargestTextSizeWhichFits(RectF availableSpace) {
        int sizesCount = this.mAutoSizeTextSizesInPx.length;
        if (sizesCount != 0) {
            int bestSizeIndex = 0;
            int lowIndex = 0 + 1;
            int highIndex = sizesCount - 1;
            while (lowIndex <= highIndex) {
                int sizeToTryIndex = (lowIndex + highIndex) / 2;
                if (suggestedSizeFitsInSpace(this.mAutoSizeTextSizesInPx[sizeToTryIndex], availableSpace)) {
                    bestSizeIndex = lowIndex;
                    lowIndex = sizeToTryIndex + 1;
                } else {
                    highIndex = sizeToTryIndex - 1;
                    bestSizeIndex = highIndex;
                }
            }
            return this.mAutoSizeTextSizesInPx[bestSizeIndex];
        }
        throw new IllegalStateException("No available text sizes to choose from.");
    }

    private boolean suggestedSizeFitsInSpace(int suggestedSizeInPx, RectF availableSpace) {
        CharSequence text;
        if (this.mTransformed != null) {
            text = this.mTransformed;
        } else {
            text = getText();
        }
        int maxLines = getMaxLines();
        if (this.mTempTextPaint == null) {
            this.mTempTextPaint = new TextPaint();
        } else {
            this.mTempTextPaint.reset();
        }
        this.mTempTextPaint.set(getPaint());
        this.mTempTextPaint.setTextSize((float) suggestedSizeInPx);
        StaticLayout.Builder layoutBuilder = StaticLayout.Builder.obtain(text, 0, text.length(), this.mTempTextPaint, Math.round(availableSpace.right));
        layoutBuilder.setAlignment(getLayoutAlignment()).setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier()).setIncludePad(getIncludeFontPadding()).setUseLineSpacingFromFallbacks(this.mUseFallbackLineSpacing).setBreakStrategy(getBreakStrategy()).setHyphenationFrequency(getHyphenationFrequency()).setJustificationMode(getJustificationMode()).setMaxLines(this.mMaxMode == 1 ? this.mMaximum : Integer.MAX_VALUE).setTextDirection(getTextDirectionHeuristic());
        StaticLayout layout = layoutBuilder.build();
        if ((maxLines == -1 || layout.getLineCount() <= maxLines) && ((float) layout.getHeight()) <= availableSpace.bottom) {
            return true;
        }
        return false;
    }

    private int getDesiredHeight() {
        boolean z = true;
        int desiredHeight = getDesiredHeight(this.mLayout, true);
        Layout layout = this.mHintLayout;
        if (this.mEllipsize == null) {
            z = false;
        }
        return Math.max(desiredHeight, getDesiredHeight(layout, z));
    }

    private int getDesiredHeight(Layout layout, boolean cap) {
        if (layout == null) {
            return 0;
        }
        int desired = layout.getHeight(cap);
        Drawables dr = this.mDrawables;
        if (dr != null) {
            desired = Math.max(Math.max(desired, dr.mDrawableHeightLeft), dr.mDrawableHeightRight);
        }
        int linecount = layout.getLineCount();
        int padding = getCompoundPaddingTop() + getCompoundPaddingBottom();
        int desired2 = desired + padding;
        if (this.mMaxMode != 1) {
            desired2 = Math.min(desired2, this.mMaximum);
        } else if (cap && linecount > this.mMaximum && ((layout instanceof DynamicLayout) || (layout instanceof BoringLayout))) {
            int desired3 = layout.getLineTop(this.mMaximum);
            if (dr != null) {
                desired3 = Math.max(Math.max(desired3, dr.mDrawableHeightLeft), dr.mDrawableHeightRight);
            }
            desired2 = desired3 + padding;
            linecount = this.mMaximum;
        }
        if (this.mMinMode != 1) {
            desired2 = Math.max(desired2, this.mMinimum);
        } else if (linecount < this.mMinimum) {
            desired2 += getLineHeight() * (this.mMinimum - linecount);
        }
        return Math.max(desired2, getSuggestedMinimumHeight());
    }

    private void checkForResize() {
        boolean sizeChanged = false;
        if (this.mLayout != null) {
            if (this.mLayoutParams.width == -2) {
                sizeChanged = true;
                invalidate();
            }
            if (this.mLayoutParams.height == -2) {
                if (getDesiredHeight() != getHeight()) {
                    sizeChanged = true;
                }
            } else if (this.mLayoutParams.height == -1 && this.mDesiredHeightAtMeasure >= 0 && getDesiredHeight() != this.mDesiredHeightAtMeasure) {
                sizeChanged = true;
            }
        }
        if (sizeChanged) {
            requestLayout();
        }
    }

    private void checkForRelayout() {
        if ((this.mLayoutParams.width != -2 || (this.mMaxWidthMode == this.mMinWidthMode && this.mMaxWidth == this.mMinWidth)) && ((this.mHint == null || this.mHintLayout != null) && ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight() > 0)) {
            int oldht = this.mLayout.getHeight();
            makeNewLayout(this.mLayout.getWidth(), this.mHintLayout == null ? 0 : this.mHintLayout.getWidth(), UNKNOWN_BORING, UNKNOWN_BORING, ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight(), false);
            if (this.mEllipsize != TextUtils.TruncateAt.MARQUEE) {
                if (this.mLayoutParams.height != -2 && this.mLayoutParams.height != -1) {
                    autoSizeText();
                    invalidate();
                    return;
                } else if (this.mLayout.getHeight() == oldht && (this.mHintLayout == null || this.mHintLayout.getHeight() == oldht)) {
                    autoSizeText();
                    invalidate();
                    return;
                }
            }
            requestLayout();
            invalidate();
        } else {
            nullLayouts();
            requestLayout();
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mDeferScroll >= 0) {
            int curs = this.mDeferScroll;
            this.mDeferScroll = -1;
            bringPointIntoView(Math.min(curs, this.mText.length()));
        }
        autoSizeText();
    }

    private boolean isShowingHint() {
        return TextUtils.isEmpty(this.mText) && !TextUtils.isEmpty(this.mHint);
    }

    private boolean bringTextIntoView() {
        int left;
        int scrolly;
        int scrollx;
        Layout layout = isShowingHint() ? this.mHintLayout : this.mLayout;
        int line = 0;
        if ((this.mGravity & 112) == 80) {
            line = layout.getLineCount() - 1;
        }
        Layout.Alignment a = layout.getParagraphAlignment(line);
        int dir = layout.getParagraphDirection(line);
        this.mTextViewDirection = dir;
        int hspace = ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        int vspace = ((this.mBottom - this.mTop) - getExtendedPaddingTop()) - getExtendedPaddingBottom();
        int ht = layout.getHeight();
        if (a == Layout.Alignment.ALIGN_NORMAL) {
            a = dir == 1 ? Layout.Alignment.ALIGN_LEFT : Layout.Alignment.ALIGN_RIGHT;
        } else if (a == Layout.Alignment.ALIGN_OPPOSITE) {
            a = dir == 1 ? Layout.Alignment.ALIGN_RIGHT : Layout.Alignment.ALIGN_LEFT;
        }
        if (a == Layout.Alignment.ALIGN_CENTER) {
            int left2 = (int) Math.floor((double) layout.getLineLeft(line));
            int right = (int) Math.ceil((double) layout.getLineRight(line));
            if (right - left2 < hspace) {
                scrollx = ((right + left2) / 2) - (hspace / 2);
            } else if (dir < 0) {
                scrollx = right - hspace;
            } else {
                scrollx = left2;
            }
            left = scrollx;
        } else if (a == Layout.Alignment.ALIGN_RIGHT) {
            left = ((int) Math.ceil((double) layout.getLineRight(line))) - hspace;
        } else {
            left = (int) Math.floor((double) layout.getLineLeft(line));
        }
        if (ht < vspace) {
            scrolly = 0;
        } else if ((this.mGravity & 112) == 80) {
            scrolly = ht - vspace;
        } else {
            scrolly = 0;
        }
        if (left == this.mScrollX && scrolly == this.mScrollY) {
            return false;
        }
        scrollTo(left, scrolly);
        return true;
    }

    public boolean bringPointIntoView(int offset) {
        int grav;
        int vs;
        boolean changed;
        int i = offset;
        if (isLayoutRequested()) {
            this.mDeferScroll = i;
            return false;
        }
        Layout layout = isShowingHint() ? this.mHintLayout : this.mLayout;
        if (layout == null) {
            return false;
        }
        int line = layout.getLineForOffset(i);
        switch (layout.getParagraphAlignment(line)) {
            case ALIGN_LEFT:
                grav = 1;
                break;
            case ALIGN_RIGHT:
                grav = -1;
                break;
            case ALIGN_NORMAL:
                grav = layout.getParagraphDirection(line);
                break;
            case ALIGN_OPPOSITE:
                grav = -layout.getParagraphDirection(line);
                break;
            default:
                grav = 0;
                break;
        }
        boolean clamped = grav > 0;
        int x = (int) layout.getPrimaryHorizontal(i, clamped);
        int top = layout.getLineTop(line);
        int bottom = layout.getLineTop(line + 1);
        int left = (int) Math.floor((double) layout.getLineLeft(line));
        int right = (int) Math.ceil((double) layout.getLineRight(line));
        int ht = layout.getHeight();
        int hspace = ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        int vspace = ((this.mBottom - this.mTop) - getExtendedPaddingTop()) - getExtendedPaddingBottom();
        if (!this.mHorizontallyScrolling && right - left > hspace && right > x) {
            right = Math.max(x, left + hspace);
        }
        int hslack = (bottom - top) / 2;
        int vslack = hslack;
        if (vslack > vspace / 4) {
            vslack = vspace / 4;
        }
        if (hslack > hspace / 4) {
            hslack = hspace / 4;
        }
        int hs = this.mScrollX;
        Layout layout2 = layout;
        int vs2 = this.mScrollY;
        boolean z = clamped;
        if (top - vs2 < vslack) {
            vs2 = top - vslack;
        }
        int vs3 = vs2;
        if (bottom - vs2 > vspace - vslack) {
            vs = bottom - (vspace - vslack);
        } else {
            vs = vs3;
        }
        if (ht - vs < vspace) {
            vs = ht - vspace;
        }
        if (0 - vs > 0) {
            vs = 0;
        }
        if (grav != 0) {
            if (x - hs < hslack) {
                hs = x - hslack;
            }
            int hs2 = hs;
            if (x - hs > hspace - hslack) {
                hs = x - (hspace - hslack);
            } else {
                hs = hs2;
            }
        }
        if (grav < 0) {
            if (left - hs > 0) {
                hs = left;
            }
            if (right - hs < hspace) {
                hs = right - hspace;
            }
        } else if (grav > 0) {
            if (right - hs < hspace) {
                hs = right - hspace;
            }
            if (left - hs > 0) {
                hs = left;
            }
        } else if (right - left <= hspace) {
            hs = left - ((hspace - (right - left)) / 2);
        } else if (x > right - hslack) {
            hs = right - hspace;
        } else if (x < left + hslack) {
            hs = left;
        } else if (left > hs) {
            hs = left;
        } else if (right < hs + hspace) {
            hs = right - hspace;
        } else {
            if (x - hs < hslack) {
                hs = x - hslack;
            }
            int hs3 = hs;
            if (x - hs > hspace - hslack) {
                hs = x - (hspace - hslack);
            } else {
                hs = hs3;
            }
        }
        if (hs == this.mScrollX && vs == this.mScrollY) {
            int i2 = hs;
            int i3 = vslack;
            int i4 = hslack;
            int i5 = vs;
            changed = false;
        } else {
            if (this.mScroller == null) {
                scrollTo(hs, vs);
                int i6 = hs;
                int i7 = vslack;
                int i8 = hslack;
                int i9 = vs;
            } else {
                int i10 = vslack;
                int i11 = hslack;
                long duration = AnimationUtils.currentAnimationTimeMillis() - this.mLastScroll;
                int dx = hs - this.mScrollX;
                int dy = vs - this.mScrollY;
                if (duration > 250) {
                    int i12 = hs;
                    int i13 = vs;
                    this.mScroller.startScroll(this.mScrollX, this.mScrollY, dx, dy);
                    awakenScrollBars(this.mScroller.getDuration());
                    invalidate();
                } else {
                    int i14 = vs;
                    if (!this.mScroller.isFinished()) {
                        this.mScroller.abortAnimation();
                    }
                    scrollBy(dx, dy);
                }
                int i15 = dx;
                this.mLastScroll = AnimationUtils.currentAnimationTimeMillis();
            }
            changed = true;
        }
        if (isFocused()) {
            if (this.mTempRect == null) {
                this.mTempRect = new Rect();
            }
            this.mTempRect.set(x - 2, top, x + 2, bottom);
            getInterestingRect(this.mTempRect, line);
            this.mTempRect.offset(this.mScrollX, this.mScrollY);
            if (requestRectangleOnScreen(this.mTempRect)) {
                changed = true;
            }
        }
        return changed;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x00a2  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00ab  */
    public boolean moveCursorToVisibleOffset() {
        int newStart;
        if (!(this.mText instanceof Spannable)) {
            return false;
        }
        int start = getSelectionStart();
        if (start != getSelectionEnd()) {
            return false;
        }
        int line = this.mLayout.getLineForOffset(start);
        int top = this.mLayout.getLineTop(line);
        int bottom = this.mLayout.getLineTop(line + 1);
        int vspace = ((this.mBottom - this.mTop) - getExtendedPaddingTop()) - getExtendedPaddingBottom();
        int vslack = (bottom - top) / 2;
        if (vslack > vspace / 4) {
            vslack = vspace / 4;
        }
        int vs = this.mScrollY;
        if (top < vs + vslack) {
            line = this.mLayout.getLineForVertical(vs + vslack + (bottom - top));
        } else if (bottom > (vspace + vs) - vslack) {
            line = this.mLayout.getLineForVertical(((vspace + vs) - vslack) - (bottom - top));
        }
        int hspace = ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        int hs = this.mScrollX;
        int leftChar = this.mLayout.getOffsetForHorizontal(line, (float) hs);
        int rightChar = this.mLayout.getOffsetForHorizontal(line, (float) (hspace + hs));
        int lowChar = leftChar < rightChar ? leftChar : rightChar;
        int highChar = leftChar > rightChar ? leftChar : rightChar;
        int newStart2 = start;
        if (newStart2 < lowChar) {
            newStart = lowChar;
        } else {
            if (newStart2 > highChar) {
                newStart = highChar;
            }
            if (newStart2 == start) {
                int i = start;
                Selection.setSelection(this.mSpannable, newStart2);
                return true;
            }
            return false;
        }
        newStart2 = newStart;
        if (newStart2 == start) {
        }
    }

    public void computeScroll() {
        if (this.mScroller != null && this.mScroller.computeScrollOffset()) {
            this.mScrollX = this.mScroller.getCurrX();
            this.mScrollY = this.mScroller.getCurrY();
            invalidateParentCaches();
            postInvalidate();
        }
    }

    private void getInterestingRect(Rect r, int line) {
        convertFromViewportToContentCoordinates(r);
        if (line == 0) {
            r.top -= getExtendedPaddingTop();
        }
        if (line == this.mLayout.getLineCount() - 1) {
            r.bottom += getExtendedPaddingBottom();
        }
    }

    private void convertFromViewportToContentCoordinates(Rect r) {
        int horizontalOffset = viewportToContentHorizontalOffset();
        r.left += horizontalOffset;
        r.right += horizontalOffset;
        int verticalOffset = viewportToContentVerticalOffset();
        r.top += verticalOffset;
        r.bottom += verticalOffset;
    }

    /* access modifiers changed from: package-private */
    public int viewportToContentHorizontalOffset() {
        return getCompoundPaddingLeft() - this.mScrollX;
    }

    /* access modifiers changed from: package-private */
    public int viewportToContentVerticalOffset() {
        int offset = getExtendedPaddingTop() - this.mScrollY;
        if ((this.mGravity & 112) != 48) {
            return offset + getVerticalOffset(false);
        }
        return offset;
    }

    public void debug(int depth) {
        String output;
        super.debug(depth);
        String output2 = debugIndent(depth);
        String output3 = output2 + "frame={" + this.mLeft + ", " + this.mTop + ", " + this.mRight + ", " + this.mBottom + "} scroll={" + this.mScrollX + ", " + this.mScrollY + "} ";
        if (this.mText != null) {
            output = output3 + "mText=\"" + this.mText + "\" ";
            if (this.mLayout != null) {
                output = output + "mLayout width=" + this.mLayout.getWidth() + " height=" + this.mLayout.getHeight();
            }
        } else {
            output = output3 + "mText=NULL";
        }
        Log.d("View", output);
    }

    @ViewDebug.ExportedProperty(category = "text")
    public int getSelectionStart() {
        return Selection.getSelectionStart(getText());
    }

    @ViewDebug.ExportedProperty(category = "text")
    public int getSelectionEnd() {
        return Selection.getSelectionEnd(getText());
    }

    public boolean hasSelection() {
        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();
        return selectionStart >= 0 && selectionEnd > 0 && selectionStart != selectionEnd;
    }

    /* access modifiers changed from: package-private */
    public String getSelectedText() {
        if (!hasSelection()) {
            return null;
        }
        int start = getSelectionStart();
        int end = getSelectionEnd();
        return String.valueOf(start > end ? this.mText.subSequence(end, start) : this.mText.subSequence(start, end));
    }

    public void setSingleLine() {
        setSingleLine(true);
    }

    public void setAllCaps(boolean allCaps) {
        if (allCaps) {
            setTransformationMethod(new AllCapsTransformationMethod(getContext()));
        } else {
            setTransformationMethod(null);
        }
    }

    public boolean isAllCaps() {
        TransformationMethod method = getTransformationMethod();
        return method != null && (method instanceof AllCapsTransformationMethod);
    }

    @RemotableViewMethod
    public void setSingleLine(boolean singleLine) {
        setInputTypeSingleLine(singleLine);
        applySingleLine(singleLine, true, true);
    }

    private void setInputTypeSingleLine(boolean singleLine) {
        if (this.mEditor != null && (this.mEditor.mInputType & 15) == 1) {
            if (singleLine) {
                this.mEditor.mInputType &= -131073;
                return;
            }
            this.mEditor.mInputType |= 131072;
        }
    }

    private void applySingleLine(boolean singleLine, boolean applyTransformation, boolean changeMaxLines) {
        this.mSingleLine = singleLine;
        if (singleLine) {
            setLines(1);
            setHorizontallyScrolling(true);
            if (applyTransformation) {
                setTransformationMethod(SingleLineTransformationMethod.getInstance());
                return;
            }
            return;
        }
        if (changeMaxLines) {
            setMaxLines(Integer.MAX_VALUE);
        }
        setHorizontallyScrolling(false);
        if (applyTransformation) {
            setTransformationMethod(null);
        }
    }

    public void setEllipsize(TextUtils.TruncateAt where) {
        if (this.mEllipsize != where) {
            this.mEllipsize = where;
            if (this.mLayout != null) {
                nullLayouts();
                requestLayout();
                invalidate();
            }
        }
    }

    public void setMarqueeRepeatLimit(int marqueeLimit) {
        this.mMarqueeRepeatLimit = marqueeLimit;
    }

    public int getMarqueeRepeatLimit() {
        return this.mMarqueeRepeatLimit;
    }

    @ViewDebug.ExportedProperty
    public TextUtils.TruncateAt getEllipsize() {
        return this.mEllipsize;
    }

    @RemotableViewMethod
    public void setSelectAllOnFocus(boolean selectAllOnFocus) {
        createEditorIfNeeded();
        this.mEditor.mSelectAllOnFocus = selectAllOnFocus;
        if (selectAllOnFocus && !(this.mText instanceof Spannable)) {
            setText(this.mText, BufferType.SPANNABLE);
        }
    }

    @RemotableViewMethod
    public void setCursorVisible(boolean visible) {
        if (!visible || this.mEditor != null) {
            createEditorIfNeeded();
            if (this.mEditor.mCursorVisible != visible) {
                this.mEditor.mCursorVisible = visible;
                invalidate();
                this.mEditor.makeBlink();
                this.mEditor.prepareCursorControllers();
            }
        }
    }

    public boolean isCursorVisible() {
        if (this.mEditor == null) {
            return true;
        }
        return this.mEditor.mCursorVisible;
    }

    private boolean canMarquee() {
        int width = ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        if (width <= 0) {
            return false;
        }
        if (this.mLayout.getLineWidth(0) > ((float) width) || (this.mMarqueeFadeMode != 0 && this.mSavedMarqueeModeLayout != null && this.mSavedMarqueeModeLayout.getLineWidth(0) > ((float) width))) {
            return true;
        }
        return false;
    }

    private void startMarquee() {
        if (getKeyListener() == null && !compressText((float) ((getWidth() - getCompoundPaddingLeft()) - getCompoundPaddingRight()))) {
            if ((this.mMarquee == null || this.mMarquee.isStopped()) && ((isFocused() || isSelected()) && getLineCount() == 1 && canMarquee())) {
                if (this.mMarqueeFadeMode == 1) {
                    this.mMarqueeFadeMode = 2;
                    Layout tmp = this.mLayout;
                    this.mLayout = this.mSavedMarqueeModeLayout;
                    this.mSavedMarqueeModeLayout = tmp;
                    setHorizontalFadingEdgeEnabled(true);
                    requestLayout();
                    invalidate();
                }
                if (this.mMarquee == null) {
                    this.mMarquee = new Marquee(this);
                }
                this.mMarquee.start(this.mMarqueeRepeatLimit);
            }
        }
    }

    private void stopMarquee() {
        if (this.mMarquee != null && !this.mMarquee.isStopped()) {
            this.mMarquee.stop();
        }
        if (this.mMarqueeFadeMode == 2) {
            this.mMarqueeFadeMode = 1;
            Layout tmp = this.mSavedMarqueeModeLayout;
            this.mSavedMarqueeModeLayout = this.mLayout;
            this.mLayout = tmp;
            setHorizontalFadingEdgeEnabled(false);
            requestLayout();
            invalidate();
        }
    }

    private void startStopMarquee(boolean start) {
        if (this.mEllipsize != TextUtils.TruncateAt.MARQUEE) {
            return;
        }
        if (start) {
            startMarquee();
        } else {
            stopMarquee();
        }
    }

    /* access modifiers changed from: protected */
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
    }

    /* access modifiers changed from: protected */
    public void onSelectionChanged(int selStart, int selEnd) {
        sendAccessibilityEvent(8192);
    }

    public void addTextChangedListener(TextWatcher watcher) {
        if (this.mListeners == null) {
            this.mListeners = new ArrayList<>();
        }
        this.mListeners.add(watcher);
    }

    public void removeTextChangedListener(TextWatcher watcher) {
        if (this.mListeners != null) {
            int i = this.mListeners.indexOf(watcher);
            if (i >= 0) {
                this.mListeners.remove(i);
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendBeforeTextChanged(CharSequence text, int start, int before, int after) {
        if (this.mListeners != null) {
            ArrayList<TextWatcher> list = this.mListeners;
            int count = list.size();
            for (int i = 0; i < count; i++) {
                list.get(i).beforeTextChanged(text, start, before, after);
            }
        }
        removeIntersectingNonAdjacentSpans(start, start + before, SpellCheckSpan.class);
        removeIntersectingNonAdjacentSpans(start, start + before, SuggestionSpan.class);
    }

    private <T> void removeIntersectingNonAdjacentSpans(int start, int end, Class<T> type) {
        if (this.mText instanceof Editable) {
            Editable text = (Editable) this.mText;
            T[] spans = text.getSpans(start, end, type);
            int length = spans.length;
            for (int i = 0; i < length; i++) {
                int spanStart = text.getSpanStart(spans[i]);
                if (text.getSpanEnd(spans[i]) == start || spanStart == end) {
                    break;
                }
                text.removeSpan(spans[i]);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeAdjacentSuggestionSpans(int pos) {
        if (this.mText instanceof Editable) {
            Editable text = (Editable) this.mText;
            SuggestionSpan[] spans = (SuggestionSpan[]) text.getSpans(pos, pos, SuggestionSpan.class);
            int length = spans.length;
            for (int i = 0; i < length; i++) {
                int spanStart = text.getSpanStart(spans[i]);
                int spanEnd = text.getSpanEnd(spans[i]);
                if ((spanEnd == pos || spanStart == pos) && SpellChecker.haveWordBoundariesChanged(text, pos, pos, spanStart, spanEnd)) {
                    text.removeSpan(spans[i]);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sendOnTextChanged(CharSequence text, int start, int before, int after) {
        if (this.mListeners != null) {
            ArrayList<TextWatcher> list = this.mListeners;
            int count = list.size();
            for (int i = 0; i < count; i++) {
                list.get(i).onTextChanged(text, start, before, after);
            }
        }
        if (this.mEditor != null) {
            this.mEditor.sendOnTextChanged(start, before, after);
        }
    }

    /* access modifiers changed from: package-private */
    public void sendAfterTextChanged(Editable text) {
        if (this.mListeners != null) {
            ArrayList<TextWatcher> list = this.mListeners;
            int count = list.size();
            for (int i = 0; i < count; i++) {
                list.get(i).afterTextChanged(text);
            }
        }
        notifyAutoFillManagerAfterTextChangedIfNeeded();
        hideErrorIfUnchanged();
    }

    private void notifyAutoFillManagerAfterTextChangedIfNeeded() {
        if (isAutofillable()) {
            AutofillManager afm = (AutofillManager) this.mContext.getSystemService(AutofillManager.class);
            if (afm != null) {
                if (this.mLastValueSentToAutofillManager == null || !this.mLastValueSentToAutofillManager.equals(this.mText)) {
                    if (Helper.sVerbose) {
                        Log.v(LOG_TAG, "notifying AFM after text changed");
                    }
                    afm.notifyValueChanged(this);
                    this.mLastValueSentToAutofillManager = this.mText;
                } else if (Helper.sVerbose) {
                    Log.v(LOG_TAG, "not notifying AFM on unchanged text");
                }
            }
        }
    }

    private boolean isAutofillable() {
        return getAutofillType() != 0;
    }

    /* access modifiers changed from: package-private */
    public void updateAfterEdit() {
        invalidate();
        int curs = getSelectionStart();
        if (curs >= 0 || (this.mGravity & 112) == 80) {
            registerForPreDraw();
        }
        checkForResize();
        if (curs >= 0) {
            this.mHighlightPathBogus = true;
            if (this.mEditor != null) {
                this.mEditor.makeBlink();
            }
            bringPointIntoView(curs);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleTextChanged(CharSequence buffer, int start, int before, int after) {
        sLastCutCopyOrTextChangedTime = 0;
        Editor.InputMethodState ims = this.mEditor == null ? null : this.mEditor.mInputMethodState;
        if (ims == null || ims.mBatchEditNesting == 0) {
            updateAfterEdit();
        }
        if (ims != null) {
            ims.mContentChanged = true;
            if (ims.mChangedStart < 0) {
                ims.mChangedStart = start;
                ims.mChangedEnd = start + before;
            } else {
                ims.mChangedStart = Math.min(ims.mChangedStart, start);
                ims.mChangedEnd = Math.max(ims.mChangedEnd, (start + before) - ims.mChangedDelta);
            }
            ims.mChangedDelta += after - before;
        }
        resetErrorChangedFlag();
        sendOnTextChanged(buffer, start, before, after);
        onTextChanged(buffer, start, before, after);
    }

    /* access modifiers changed from: package-private */
    public void spanChange(Spanned buf, Object what, int oldStart, int newStart, int oldEnd, int newEnd) {
        boolean selChanged = false;
        int newSelStart = -1;
        int newSelEnd = -1;
        Editor.InputMethodState ims = this.mEditor == null ? null : this.mEditor.mInputMethodState;
        if (what == Selection.SELECTION_END) {
            selChanged = true;
            newSelEnd = newStart;
            if (oldStart >= 0 || newStart >= 0) {
                invalidateCursor(Selection.getSelectionStart(buf), oldStart, newStart);
                checkForResize();
                registerForPreDraw();
                if (this.mEditor != null) {
                    this.mEditor.makeBlink();
                }
            }
        }
        if (what == Selection.SELECTION_START) {
            selChanged = true;
            newSelStart = newStart;
            if (oldStart >= 0 || newStart >= 0) {
                invalidateCursor(Selection.getSelectionEnd(buf), oldStart, newStart);
            }
        }
        if (selChanged) {
            this.mHighlightPathBogus = true;
            if (this.mEditor != null && !isFocused()) {
                this.mEditor.mSelectionMoved = true;
            }
            if ((buf.getSpanFlags(what) & 512) == 0) {
                if (newSelStart < 0) {
                    newSelStart = Selection.getSelectionStart(buf);
                }
                if (newSelEnd < 0) {
                    newSelEnd = Selection.getSelectionEnd(buf);
                }
                if (this.mEditor != null) {
                    this.mEditor.refreshTextActionMode();
                    if (!hasSelection() && this.mEditor.getTextActionMode() == null && hasTransientState()) {
                        setHasTransientState(false);
                    }
                }
                onSelectionChanged(newSelStart, newSelEnd);
            }
        }
        if ((what instanceof UpdateAppearance) || (what instanceof ParagraphStyle) || (what instanceof CharacterStyle)) {
            if (ims == null || ims.mBatchEditNesting == 0) {
                invalidate();
                this.mHighlightPathBogus = true;
                checkForResize();
            } else {
                ims.mContentChanged = true;
            }
            if (this.mEditor != null) {
                if (oldStart >= 0) {
                    this.mEditor.invalidateTextDisplayList(this.mLayout, oldStart, oldEnd);
                }
                if (newStart >= 0) {
                    this.mEditor.invalidateTextDisplayList(this.mLayout, newStart, newEnd);
                }
                this.mEditor.invalidateHandlesAndActionMode();
            }
        }
        if (MetaKeyKeyListener.isMetaTracker(buf, what)) {
            this.mHighlightPathBogus = true;
            if (ims != null && MetaKeyKeyListener.isSelectingMetaTracker(buf, what)) {
                ims.mSelectionModeChanged = true;
            }
            if (Selection.getSelectionStart(buf) >= 0) {
                if (ims == null || ims.mBatchEditNesting == 0) {
                    invalidateCursor();
                } else {
                    ims.mCursorChanged = true;
                }
            }
        }
        if (!(!(what instanceof ParcelableSpan) || ims == null || ims.mExtractedTextRequest == null)) {
            if (ims.mBatchEditNesting != 0) {
                if (oldStart >= 0) {
                    if (ims.mChangedStart > oldStart) {
                        ims.mChangedStart = oldStart;
                    }
                    if (ims.mChangedStart > oldEnd) {
                        ims.mChangedStart = oldEnd;
                    }
                }
                if (newStart >= 0) {
                    if (ims.mChangedStart > newStart) {
                        ims.mChangedStart = newStart;
                    }
                    if (ims.mChangedStart > newEnd) {
                        ims.mChangedStart = newEnd;
                    }
                }
            } else {
                ims.mContentChanged = true;
            }
        }
        if (this.mEditor != null && this.mEditor.mSpellChecker != null && newStart < 0 && (what instanceof SpellCheckSpan)) {
            this.mEditor.mSpellChecker.onSpellCheckSpanRemoved((SpellCheckSpan) what);
        }
    }

    /* access modifiers changed from: protected */
    public void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (isTemporarilyDetached()) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
            return;
        }
        if (this.mEditor != null) {
            this.mEditor.onFocusChanged(focused, direction);
        }
        if (focused && this.mSpannable != null) {
            MetaKeyKeyListener.resetMetaState(this.mSpannable);
        }
        startStopMarquee(focused);
        if (this.mTransformation != null) {
            this.mTransformation.onFocusChanged(this, this.mText, focused, direction, previouslyFocusedRect);
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (this.mEditor != null) {
            this.mEditor.onWindowFocusChanged(hasWindowFocus);
        }
        startStopMarquee(hasWindowFocus);
    }

    /* access modifiers changed from: protected */
    public void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (this.mEditor != null && visibility != 0) {
            this.mEditor.hideCursorAndSpanControllers();
            stopTextActionMode();
        }
    }

    public void clearComposingText() {
        if (this.mText instanceof Spannable) {
            BaseInputConnection.removeComposingSpans(this.mSpannable);
        }
    }

    public void setSelected(boolean selected) {
        boolean wasSelected = isSelected();
        super.setSelected(selected);
        if (selected != wasSelected && this.mEllipsize == TextUtils.TruncateAt.MARQUEE) {
            if (selected) {
                startMarquee();
            } else {
                stopMarquee();
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == 0) {
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null) {
                imm.resetInTransitionState();
            }
        }
        if (this.mEditor != null) {
            this.mEditor.onTouchEvent(event);
            if (this.mEditor.mSelectionModifierCursorController != null && this.mEditor.mSelectionModifierCursorController.isDragAcceleratorActive()) {
                return true;
            }
        }
        boolean superResult = super.onTouchEvent(event);
        if (this.mEditor == null || !this.mEditor.mDiscardNextActionUp || action != 1) {
            boolean touchIsFinished = action == 1 && (this.mEditor == null || !this.mEditor.mIgnoreActionUpEvent) && isFocused();
            if ((this.mMovement != null || onCheckIsTextEditor()) && isEnabled() && (this.mText instanceof Spannable) && this.mLayout != null) {
                boolean handled = false;
                this.mValidSetCursorEvent = true;
                if (this.mMovement != null) {
                    handled = false | this.mMovement.onTouchEvent(this, this.mSpannable, event);
                }
                boolean textIsSelectable = isTextSelectable();
                if (touchIsFinished && this.mLinksClickable && this.mAutoLinkMask != 0 && textIsSelectable) {
                    ClickableSpan[] links = (ClickableSpan[]) this.mSpannable.getSpans(getSelectionStart(), getSelectionEnd(), ClickableSpan.class);
                    if (links.length > 0) {
                        links[0].onClick(this);
                        handled = true;
                    }
                }
                if (touchIsFinished && (isTextEditable() || textIsSelectable)) {
                    InputMethodManager imm2 = InputMethodManager.peekInstance();
                    viewClicked(imm2);
                    if (isTextEditable() && this.mEditor.mShowSoftInputOnFocus && imm2 != null) {
                        imm2.showSoftInput(this, 0);
                    }
                    this.mEditor.onTouchUpEvent(event);
                    handled = true;
                }
                if (handled) {
                    return true;
                }
            }
            return superResult;
        }
        this.mEditor.mDiscardNextActionUp = false;
        if (this.mEditor.mIsInsertionActionModeStartPending) {
            this.mEditor.startInsertionActionMode();
            this.mEditor.mIsInsertionActionModeStartPending = false;
        }
        return superResult;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        if (!(this.mMovement == null || !(this.mText instanceof Spannable) || this.mLayout == null)) {
            try {
                if (this.mMovement.onGenericMotionEvent(this, this.mSpannable, event)) {
                    return true;
                }
            } catch (AbstractMethodError e) {
            }
        }
        return super.onGenericMotionEvent(event);
    }

    /* access modifiers changed from: protected */
    public void onCreateContextMenu(ContextMenu menu) {
        if (this.mEditor != null) {
            this.mEditor.onCreateContextMenu(menu);
        }
    }

    public boolean showContextMenu() {
        if (this.mEditor != null) {
            this.mEditor.setContextMenuAnchor(Float.NaN, Float.NaN);
        }
        return super.showContextMenu();
    }

    public boolean showContextMenu(float x, float y) {
        if (this.mEditor != null) {
            this.mEditor.setContextMenuAnchor(x, y);
        }
        return super.showContextMenu(x, y);
    }

    /* access modifiers changed from: package-private */
    public boolean isTextEditable() {
        return (this.mText instanceof Editable) && onCheckIsTextEditor() && isEnabled();
    }

    public boolean didTouchFocusSelect() {
        return this.mEditor != null && this.mEditor.mTouchFocusSelected;
    }

    public void cancelLongPress() {
        super.cancelLongPress();
        if (this.mEditor != null) {
            this.mEditor.mIgnoreActionUpEvent = true;
        }
    }

    public boolean onTrackballEvent(MotionEvent event) {
        if (this.mMovement == null || this.mSpannable == null || this.mLayout == null || !this.mMovement.onTrackballEvent(this, this.mSpannable, event)) {
            return super.onTrackballEvent(event);
        }
        return true;
    }

    public void setScroller(Scroller s) {
        this.mScroller = s;
    }

    /* access modifiers changed from: protected */
    public float getLeftFadingEdgeStrength() {
        if (isMarqueeFadeEnabled() && this.mMarquee != null && !this.mMarquee.isStopped()) {
            Marquee marquee = this.mMarquee;
            if (marquee.shouldDrawLeftFade()) {
                return getHorizontalFadingEdgeStrength(marquee.getScroll(), 0.0f);
            }
            return 0.0f;
        } else if (getLineCount() != 1) {
            return super.getLeftFadingEdgeStrength();
        } else {
            float lineLeft = getLayout().getLineLeft(0);
            if (lineLeft > ((float) this.mScrollX)) {
                return 0.0f;
            }
            return getHorizontalFadingEdgeStrength((float) this.mScrollX, lineLeft);
        }
    }

    /* access modifiers changed from: protected */
    public float getRightFadingEdgeStrength() {
        if (isMarqueeFadeEnabled() && this.mMarquee != null && !this.mMarquee.isStopped()) {
            Marquee marquee = this.mMarquee;
            return getHorizontalFadingEdgeStrength(marquee.getMaxFadeScroll(), marquee.getScroll());
        } else if (getLineCount() != 1) {
            return super.getRightFadingEdgeStrength();
        } else {
            float rightEdge = (float) (this.mScrollX + ((getWidth() - getCompoundPaddingLeft()) - getCompoundPaddingRight()));
            float lineRight = getLayout().getLineRight(0);
            if (lineRight < rightEdge) {
                return 0.0f;
            }
            return getHorizontalFadingEdgeStrength(rightEdge, lineRight);
        }
    }

    private float getHorizontalFadingEdgeStrength(float position1, float position2) {
        int horizontalFadingEdgeLength = getHorizontalFadingEdgeLength();
        if (horizontalFadingEdgeLength == 0) {
            return 0.0f;
        }
        float diff = Math.abs(position1 - position2);
        if (diff > ((float) horizontalFadingEdgeLength)) {
            return 1.0f;
        }
        return diff / ((float) horizontalFadingEdgeLength);
    }

    private boolean isMarqueeFadeEnabled() {
        return this.mEllipsize == TextUtils.TruncateAt.MARQUEE && this.mMarqueeFadeMode != 1;
    }

    /* access modifiers changed from: protected */
    public int computeHorizontalScrollRange() {
        if (this.mLayout == null) {
            return super.computeHorizontalScrollRange();
        }
        return (!this.mSingleLine || (this.mGravity & 7) != 3) ? this.mLayout.getWidth() : (int) this.mLayout.getLineWidth(0);
    }

    /* access modifiers changed from: protected */
    public int computeVerticalScrollRange() {
        if (this.mLayout != null) {
            return this.mLayout.getHeight();
        }
        return super.computeVerticalScrollRange();
    }

    /* access modifiers changed from: protected */
    public int computeVerticalScrollExtent() {
        return (getHeight() - getCompoundPaddingTop()) - getCompoundPaddingBottom();
    }

    public void findViewsWithText(ArrayList<View> outViews, CharSequence searched, int flags) {
        super.findViewsWithText(outViews, searched, flags);
        if (!outViews.contains(this) && (flags & 1) != 0 && !TextUtils.isEmpty(searched) && !TextUtils.isEmpty(this.mText)) {
            if (this.mText.toString().toLowerCase().contains(searched.toString().toLowerCase())) {
                outViews.add(this);
            }
        }
    }

    public static ColorStateList getTextColors(Context context, TypedArray attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(android.R.styleable.TextView);
            ColorStateList colors = a.getColorStateList(5);
            if (colors == null) {
                int ap = a.getResourceId(1, 0);
                if (ap != 0) {
                    TypedArray appearance = context.obtainStyledAttributes(ap, android.R.styleable.TextAppearance);
                    colors = appearance.getColorStateList(3);
                    appearance.recycle();
                }
            }
            a.recycle();
            return colors;
        }
        throw new NullPointerException();
    }

    public static int getTextColor(Context context, TypedArray attrs, int def) {
        ColorStateList colors = getTextColors(context, attrs);
        if (colors == null) {
            return def;
        }
        return colors.getDefaultColor();
    }

    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        if (event.hasModifiers(4096)) {
            if (keyCode != 29) {
                if (keyCode != 31) {
                    if (keyCode != 50) {
                        if (keyCode != 52) {
                            if (keyCode == 54 && canUndo()) {
                                return onTextContextMenuItem(ID_UNDO);
                            }
                        } else if (canCut()) {
                            return onTextContextMenuItem(ID_CUT);
                        }
                    } else if (canPaste()) {
                        return onTextContextMenuItem(ID_PASTE);
                    }
                } else if (canCopy()) {
                    return onTextContextMenuItem(ID_COPY);
                }
            } else if (canSelectText()) {
                return onTextContextMenuItem(ID_SELECT_ALL);
            }
        } else if (event.hasModifiers(4097)) {
            if (keyCode != 50) {
                if (keyCode == 54 && canRedo()) {
                    return onTextContextMenuItem(ID_REDO);
                }
            } else if (canPaste()) {
                return onTextContextMenuItem(ID_PASTE_AS_PLAIN_TEXT);
            }
        }
        return super.onKeyShortcut(keyCode, event);
    }

    /* access modifiers changed from: package-private */
    public boolean canSelectText() {
        return (this.mText.length() == 0 || this.mEditor == null || !this.mEditor.hasSelectionController()) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public boolean textCanBeSelected() {
        boolean z = false;
        if (this.mMovement == null || !this.mMovement.canSelectArbitrarily()) {
            return false;
        }
        if (isTextEditable() || (isTextSelectable() && (this.mText instanceof Spannable) && isEnabled())) {
            z = true;
        }
        return z;
    }

    private Locale getTextServicesLocale(boolean allowNullLocale) {
        updateTextServicesLocaleAsync();
        if (this.mCurrentSpellCheckerLocaleCache != null || allowNullLocale) {
            return this.mCurrentSpellCheckerLocaleCache;
        }
        return Locale.getDefault();
    }

    public Locale getTextServicesLocale() {
        return getTextServicesLocale(false);
    }

    public boolean isInExtractedMode() {
        return false;
    }

    private boolean isAutoSizeEnabled() {
        return supportsAutoSizeText() && this.mAutoSizeTextType != 0;
    }

    /* access modifiers changed from: protected */
    public boolean supportsAutoSizeText() {
        return true;
    }

    public Locale getSpellCheckerLocale() {
        return getTextServicesLocale(true);
    }

    private void updateTextServicesLocaleAsync() {
        AsyncTask.execute((Runnable) new Runnable() {
            public void run() {
                TextView.this.updateTextServicesLocaleLocked();
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateTextServicesLocaleLocked() {
        Locale locale;
        SpellCheckerSubtype subtype = ((TextServicesManager) this.mContext.getSystemService("textservices")).getCurrentSpellCheckerSubtype(true);
        if (subtype != null) {
            locale = subtype.getLocaleObject();
        } else {
            locale = null;
        }
        this.mCurrentSpellCheckerLocaleCache = locale;
    }

    /* access modifiers changed from: package-private */
    public void onLocaleChanged() {
        this.mEditor.onLocaleChanged();
    }

    public WordIterator getWordIterator() {
        if (this.mEditor != null) {
            return this.mEditor.getWordIterator();
        }
        return null;
    }

    public void onPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        super.onPopulateAccessibilityEventInternal(event);
        CharSequence text = getTextForAccessibility();
        if (!TextUtils.isEmpty(text)) {
            event.getText().add(text);
        }
    }

    public CharSequence getAccessibilityClassName() {
        return TextView.class.getName();
    }

    public void onProvideStructure(ViewStructure structure) {
        super.onProvideStructure(structure);
        onProvideAutoStructureForAssistOrAutofill(structure, false);
    }

    public void onProvideAutofillStructure(ViewStructure structure, int flags) {
        super.onProvideAutofillStructure(structure, flags);
        onProvideAutoStructureForAssistOrAutofill(structure, true);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:52:0x010a, code lost:
        if (r3 < r7.length()) goto L_0x010f;
     */
    private void onProvideAutoStructureForAssistOrAutofill(ViewStructure structure, boolean forAutofill) {
        int bottomLine;
        int topLine;
        ViewStructure viewStructure = structure;
        boolean isPassword = hasPasswordTransformationMethod() || isPasswordInputType(getInputType());
        if (forAutofill) {
            viewStructure.setDataIsSensitive(!this.mTextSetFromXmlOrResourceId);
            if (this.mTextId != 0) {
                try {
                    viewStructure.setTextIdEntry(getResources().getResourceEntryName(this.mTextId));
                } catch (Resources.NotFoundException e) {
                    if (Helper.sVerbose) {
                        Log.v(LOG_TAG, "onProvideAutofillStructure(): cannot set name for text id " + this.mTextId + ": " + e.getMessage());
                    }
                }
            }
        }
        if (!isPassword || forAutofill) {
            if (this.mLayout == null) {
                assumeLayout();
            }
            Layout layout = this.mLayout;
            int lineCount = layout.getLineCount();
            if (lineCount <= 1) {
                CharSequence text = getText();
                if (forAutofill) {
                    viewStructure.setText(text);
                } else {
                    viewStructure.setText(text, getSelectionStart(), getSelectionEnd());
                }
                boolean z = isPassword;
                int i = lineCount;
            } else {
                int[] tmpCords = new int[2];
                getLocationInWindow(tmpCords);
                int topWindowLocation = tmpCords[1];
                View root = this;
                ViewParent viewParent = getParent();
                while (viewParent instanceof View) {
                    root = (View) viewParent;
                    viewParent = root.getParent();
                }
                int windowHeight = root.getHeight();
                if (topWindowLocation >= 0) {
                    topLine = getLineAtCoordinateUnclamped(0.0f);
                    bottomLine = getLineAtCoordinateUnclamped((float) (windowHeight - 1));
                } else {
                    topLine = getLineAtCoordinateUnclamped((float) (-topWindowLocation));
                    bottomLine = getLineAtCoordinateUnclamped((float) ((windowHeight - 1) - topWindowLocation));
                }
                int expandedTopLine = topLine - ((bottomLine - topLine) / 2);
                if (expandedTopLine < 0) {
                    expandedTopLine = 0;
                }
                int expandedTopLine2 = expandedTopLine;
                int expandedBottomLine = bottomLine + ((bottomLine - topLine) / 2);
                if (expandedBottomLine >= lineCount) {
                    expandedBottomLine = lineCount - 1;
                }
                int expandedTopChar = layout.getLineStart(expandedTopLine2);
                int i2 = expandedTopLine2;
                int expandedBottomChar = layout.getLineEnd(expandedBottomLine);
                boolean z2 = isPassword;
                int i3 = getSelectionStart();
                int i4 = lineCount;
                int lineCount2 = getSelectionEnd();
                if (i3 < lineCount2) {
                    if (i3 < expandedTopChar) {
                        expandedTopChar = i3;
                    }
                    if (lineCount2 > expandedBottomChar) {
                        expandedBottomChar = lineCount2;
                    }
                }
                int i5 = expandedBottomLine;
                CharSequence text2 = getText();
                if (expandedTopChar <= 0) {
                    int[] iArr = tmpCords;
                }
                text2 = text2.subSequence(expandedTopChar, expandedBottomChar);
                if (forAutofill) {
                    viewStructure.setText(text2);
                } else {
                    int i6 = expandedBottomChar;
                    viewStructure.setText(text2, i3 - expandedTopChar, lineCount2 - expandedTopChar);
                    int[] lineOffsets = new int[((bottomLine - topLine) + 1)];
                    int i7 = expandedTopChar;
                    int[] lineBaselines = new int[((bottomLine - topLine) + 1)];
                    int baselineOffset = getBaselineOffset();
                    int i8 = topLine;
                    while (true) {
                        int selStart = i3;
                        int i9 = i8;
                        if (i9 > bottomLine) {
                            break;
                        }
                        lineOffsets[i9 - topLine] = layout.getLineStart(i9);
                        lineBaselines[i9 - topLine] = layout.getLineBaseline(i9) + baselineOffset;
                        i8 = i9 + 1;
                        i3 = selStart;
                    }
                    viewStructure.setTextLines(lineOffsets, lineBaselines);
                }
            }
            if (!forAutofill) {
                int style = 0;
                int typefaceStyle = getTypefaceStyle();
                if ((typefaceStyle & 1) != 0) {
                    style = 0 | 1;
                }
                if ((typefaceStyle & 2) != 0) {
                    style |= 2;
                }
                int paintFlags = this.mTextPaint.getFlags();
                if ((paintFlags & 32) != 0) {
                    style |= 1;
                }
                if ((paintFlags & 8) != 0) {
                    style |= 4;
                }
                if ((paintFlags & 16) != 0) {
                    style |= 8;
                }
                viewStructure.setTextStyle(getTextSize(), getCurrentTextColor(), 1, style);
            } else {
                viewStructure.setMinTextEms(getMinEms());
                viewStructure.setMaxTextEms(getMaxEms());
                int maxLength = -1;
                InputFilter[] filters = getFilters();
                int length = filters.length;
                int i10 = 0;
                while (true) {
                    if (i10 >= length) {
                        break;
                    }
                    InputFilter filter = filters[i10];
                    if (filter instanceof InputFilter.LengthFilter) {
                        maxLength = ((InputFilter.LengthFilter) filter).getMax();
                        break;
                    }
                    i10++;
                }
                viewStructure.setMaxTextLength(maxLength);
            }
        } else {
            boolean z3 = isPassword;
        }
        viewStructure.setHint(getHint());
        viewStructure.setInputType(getInputType());
    }

    /* access modifiers changed from: package-private */
    public boolean canRequestAutofill() {
        if (!isAutofillable()) {
            return false;
        }
        AutofillManager afm = (AutofillManager) this.mContext.getSystemService(AutofillManager.class);
        if (afm != null) {
            return afm.isEnabled();
        }
        return false;
    }

    private void requestAutofill() {
        AutofillManager afm = (AutofillManager) this.mContext.getSystemService(AutofillManager.class);
        if (afm != null) {
            afm.requestAutofill(this);
        }
    }

    public void autofill(AutofillValue value) {
        if (!value.isText() || !isTextEditable()) {
            Log.w(LOG_TAG, value + " could not be autofilled into " + this);
            return;
        }
        setText(value.getTextValue(), this.mBufferType, true, 0);
        CharSequence text = getText();
        if (text instanceof Spannable) {
            Selection.setSelection((Spannable) text, text.length());
        }
    }

    public int getAutofillType() {
        return isTextEditable() ? 1 : 0;
    }

    public AutofillValue getAutofillValue() {
        if (isTextEditable()) {
            return AutofillValue.forText(TextUtils.trimToParcelableSize(getText()));
        }
        return null;
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        event.setPassword(hasPasswordTransformationMethod());
        if (event.getEventType() == 8192) {
            event.setFromIndex(Selection.getSelectionStart(this.mText));
            event.setToIndex(Selection.getSelectionEnd(this.mText));
            event.setItemCount(this.mText.length());
        }
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        info.setPassword(hasPasswordTransformationMethod());
        info.setText(getTextForAccessibility());
        info.setHintText(this.mHint);
        info.setShowingHintText(isShowingHint());
        if (this.mBufferType == BufferType.EDITABLE) {
            info.setEditable(true);
            if (isEnabled()) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_TEXT);
            }
        }
        if (this.mEditor != null) {
            info.setInputType(this.mEditor.mInputType);
            if (this.mEditor.mError != null) {
                info.setContentInvalid(true);
                info.setError(this.mEditor.mError);
            }
        }
        if (!TextUtils.isEmpty(this.mText)) {
            info.addAction(256);
            info.addAction(512);
            info.setMovementGranularities(31);
            info.addAction(131072);
            info.setAvailableExtraData(Arrays.asList(new String[]{AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY}));
        }
        if (isFocused()) {
            if (canCopy()) {
                info.addAction(16384);
            }
            if (canPaste()) {
                info.addAction(32768);
            }
            if (canCut()) {
                info.addAction(65536);
            }
            if (canShare()) {
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(268435456, getResources().getString(17041089)));
            }
            if (canProcessText()) {
                this.mEditor.mProcessTextIntentActionsHandler.onInitializeAccessibilityNodeInfo(info);
            }
        }
        for (InputFilter filter : this.mFilters) {
            if (filter instanceof InputFilter.LengthFilter) {
                info.setMaxTextLength(((InputFilter.LengthFilter) filter).getMax());
            }
        }
        if (isSingleLine() == 0) {
            info.setMultiLine(true);
        }
    }

    public void addExtraDataToAccessibilityNodeInfo(AccessibilityNodeInfo info, String extraDataKey, Bundle arguments) {
        if (arguments != null && extraDataKey.equals(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY)) {
            int positionInfoStartIndex = arguments.getInt(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_START_INDEX, -1);
            int positionInfoLength = arguments.getInt(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_LENGTH, -1);
            if (positionInfoLength <= 0 || positionInfoStartIndex < 0 || positionInfoStartIndex >= this.mText.length()) {
                Log.e(LOG_TAG, "Invalid arguments for accessibility character locations");
                return;
            }
            RectF[] boundingRects = new RectF[positionInfoLength];
            CursorAnchorInfo.Builder builder = new CursorAnchorInfo.Builder();
            CursorAnchorInfo.Builder builder2 = builder;
            int i = positionInfoStartIndex;
            populateCharacterBounds(builder2, i, positionInfoStartIndex + positionInfoLength, (float) viewportToContentHorizontalOffset(), (float) viewportToContentVerticalOffset());
            CursorAnchorInfo cursorAnchorInfo = builder.setMatrix(null).build();
            for (int i2 = 0; i2 < positionInfoLength; i2++) {
                if ((cursorAnchorInfo.getCharacterBoundsFlags(positionInfoStartIndex + i2) & 1) == 1) {
                    RectF bounds = cursorAnchorInfo.getCharacterBounds(positionInfoStartIndex + i2);
                    if (bounds != null) {
                        mapRectFromViewToScreenCoords(bounds, true);
                        boundingRects[i2] = bounds;
                    }
                }
            }
            info.getExtras().putParcelableArray(extraDataKey, boundingRects);
        }
    }

    public void populateCharacterBounds(CursorAnchorInfo.Builder builder, int startIndex, int endIndex, float viewportToContentHorizontalOffset, float viewportToContentVerticalOffset) {
        float left;
        float right;
        int i = startIndex;
        int i2 = endIndex;
        int minLine = this.mLayout.getLineForOffset(i);
        int maxLine = this.mLayout.getLineForOffset(i2 - 1);
        int line = minLine;
        while (line <= maxLine) {
            int lineStart = this.mLayout.getLineStart(line);
            int lineEnd = this.mLayout.getLineEnd(line);
            int offsetStart = Math.max(lineStart, i);
            int offsetEnd = Math.min(lineEnd, i2);
            boolean z = true;
            if (this.mLayout.getParagraphDirection(line) != 1) {
                z = false;
            }
            boolean ltrLine = z;
            float[] widths = new float[(offsetEnd - offsetStart)];
            this.mLayout.getPaint().getTextWidths(this.mTransformed, offsetStart, offsetEnd, widths);
            float top = (float) this.mLayout.getLineTop(line);
            float bottom = (float) this.mLayout.getLineBottom(line);
            int offset = offsetStart;
            while (true) {
                int offset2 = offset;
                if (offset2 >= offsetEnd) {
                    break;
                }
                float charWidth = widths[offset2 - offsetStart];
                boolean isRtl = this.mLayout.isRtlCharAt(offset2);
                int minLine2 = minLine;
                float primary = this.mLayout.getPrimaryHorizontal(offset2);
                int maxLine2 = maxLine;
                float secondary = this.mLayout.getSecondaryHorizontal(offset2);
                if (ltrLine) {
                    if (isRtl) {
                        left = secondary - charWidth;
                        right = secondary;
                    } else {
                        left = primary;
                        right = primary + charWidth;
                    }
                } else if (!isRtl) {
                    left = secondary;
                    right = secondary + charWidth;
                } else {
                    left = primary - charWidth;
                    right = primary;
                }
                float f = primary;
                float primary2 = left + viewportToContentHorizontalOffset;
                float f2 = secondary;
                float localRight = right + viewportToContentHorizontalOffset;
                float localTop = top + viewportToContentVerticalOffset;
                int lineStart2 = lineStart;
                float localBottom = bottom + viewportToContentVerticalOffset;
                boolean isTopLeftVisible = isPositionVisible(primary2, localTop);
                boolean isBottomRightVisible = isPositionVisible(localRight, localBottom);
                int characterBoundsFlags = 0;
                if (isTopLeftVisible || isBottomRightVisible) {
                    characterBoundsFlags = 0 | 1;
                }
                if (!isTopLeftVisible || !isBottomRightVisible) {
                    characterBoundsFlags |= 2;
                }
                if (isRtl) {
                    characterBoundsFlags |= 4;
                }
                builder.addCharacterBounds(offset2, primary2, localTop, localRight, localBottom, characterBoundsFlags);
                offset = offset2 + 1;
                minLine = minLine2;
                maxLine = maxLine2;
                lineStart = lineStart2;
                int i3 = startIndex;
                int i4 = endIndex;
            }
            int i5 = maxLine;
            line++;
            i = startIndex;
            i2 = endIndex;
        }
        int i6 = maxLine;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x007d, code lost:
        return false;
     */
    public boolean isPositionVisible(float positionX, float positionY) {
        synchronized (TEMP_POSITION) {
            float[] position = TEMP_POSITION;
            position[0] = positionX;
            position[1] = positionY;
            View view = this;
            while (view != null) {
                if (view != this) {
                    position[0] = position[0] - ((float) view.getScrollX());
                    position[1] = position[1] - ((float) view.getScrollY());
                }
                if (position[0] >= 0.0f && position[1] >= 0.0f && position[0] <= ((float) view.getWidth())) {
                    if (position[1] <= ((float) view.getHeight())) {
                        if (!view.getMatrix().isIdentity()) {
                            view.getMatrix().mapPoints(position);
                        }
                        position[0] = position[0] + ((float) view.getLeft());
                        position[1] = position[1] + ((float) view.getTop());
                        ViewParent parent = view.getParent();
                        if (parent instanceof View) {
                            view = (View) parent;
                        } else {
                            view = null;
                        }
                    }
                }
            }
            return true;
        }
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        int start;
        int end;
        CharSequence text;
        if (this.mEditor != null && this.mEditor.mProcessTextIntentActionsHandler.performAccessibilityAction(action)) {
            return true;
        }
        if (action == 16) {
            return performAccessibilityActionClick(arguments);
        }
        if (action == 256 || action == 512) {
            ensureIterableTextForAccessibilitySelectable();
            return super.performAccessibilityActionInternal(action, arguments);
        } else if (action != 16384) {
            if (action != 32768) {
                if (action != 65536) {
                    if (action == 131072) {
                        ensureIterableTextForAccessibilitySelectable();
                        CharSequence text2 = getIterableTextForAccessibility();
                        if (text2 == null) {
                            return false;
                        }
                        if (arguments != null) {
                            start = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, -1);
                        } else {
                            start = -1;
                        }
                        if (arguments != null) {
                            end = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, -1);
                        } else {
                            end = -1;
                        }
                        if (!(getSelectionStart() == start && getSelectionEnd() == end)) {
                            if (start == end && end == -1) {
                                Selection.removeSelection((Spannable) text2);
                                return true;
                            } else if (start >= 0 && start <= end && end <= text2.length()) {
                                Selection.setSelection((Spannable) text2, start, end);
                                if (this.mEditor != null) {
                                    this.mEditor.startSelectionActionModeAsync(false);
                                }
                                return true;
                            }
                        }
                        return false;
                    } else if (action != 2097152) {
                        if (action != 268435456) {
                            return super.performAccessibilityActionInternal(action, arguments);
                        }
                        if (!isFocused() || !canShare() || !onTextContextMenuItem(ID_SHARE)) {
                            return false;
                        }
                        return true;
                    } else if (!isEnabled() || this.mBufferType != BufferType.EDITABLE) {
                        return false;
                    } else {
                        if (arguments != null) {
                            text = arguments.getCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE);
                        } else {
                            text = null;
                        }
                        setText(text);
                        if (this.mText != null) {
                            int updatedTextLength = this.mText.length();
                            if (updatedTextLength > 0) {
                                Selection.setSelection(this.mSpannable, updatedTextLength);
                            }
                        }
                        return true;
                    }
                } else if (!isFocused() || !canCut() || !onTextContextMenuItem(ID_CUT)) {
                    return false;
                } else {
                    return true;
                }
            } else if (!isFocused() || !canPaste() || !onTextContextMenuItem(ID_PASTE)) {
                return false;
            } else {
                return true;
            }
        } else if (!isFocused() || !canCopy() || !onTextContextMenuItem(ID_COPY)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean performAccessibilityActionClick(Bundle arguments) {
        boolean handled = false;
        if (!isEnabled()) {
            return false;
        }
        if (isClickable() || isLongClickable()) {
            if (isFocusable() && !isFocused()) {
                requestFocus();
            }
            performClick();
            handled = true;
        }
        if ((this.mMovement != null || onCheckIsTextEditor()) && hasSpannableText() && this.mLayout != null && ((isTextEditable() || isTextSelectable()) && isFocused())) {
            InputMethodManager imm = InputMethodManager.peekInstance();
            viewClicked(imm);
            if (!isTextSelectable() && this.mEditor.mShowSoftInputOnFocus && imm != null) {
                handled |= imm.showSoftInput(this, 0);
            }
        }
        return handled;
    }

    private boolean hasSpannableText() {
        return this.mText != null && (this.mText instanceof Spannable);
    }

    public void sendAccessibilityEventInternal(int eventType) {
        if (eventType == 32768 && this.mEditor != null) {
            this.mEditor.mProcessTextIntentActionsHandler.initializeAccessibilityActions();
        }
        super.sendAccessibilityEventInternal(eventType);
    }

    public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {
        if (event.getEventType() != 4096) {
            super.sendAccessibilityEventUnchecked(event);
        }
    }

    private CharSequence getTextForAccessibility() {
        if (TextUtils.isEmpty(this.mText)) {
            return this.mHint;
        }
        return TextUtils.trimToParcelableSize(this.mTransformed);
    }

    /* access modifiers changed from: package-private */
    public void sendAccessibilityEventTypeViewTextChanged(CharSequence beforeText, int fromIndex, int removedCount, int addedCount) {
        AccessibilityEvent event = AccessibilityEvent.obtain(16);
        event.setFromIndex(fromIndex);
        event.setRemovedCount(removedCount);
        event.setAddedCount(addedCount);
        event.setBeforeText(beforeText);
        sendAccessibilityEventUnchecked(event);
    }

    public boolean isInputMethodTarget() {
        InputMethodManager imm = InputMethodManager.peekInstance();
        return imm != null && imm.isActive(this);
    }

    public boolean onTextContextMenuItem(int id) {
        int min = 0;
        int max = this.mText.length();
        if (isFocused()) {
            int selStart = getSelectionStart();
            int selEnd = getSelectionEnd();
            min = Math.max(0, Math.min(selStart, selEnd));
            max = Math.max(0, Math.max(selStart, selEnd));
        }
        if (id != ID_AUTOFILL) {
            switch (id) {
                case ID_SELECT_ALL /*16908319*/:
                    boolean hadSelection = hasSelection();
                    selectAllText();
                    if (this.mEditor != null && hadSelection) {
                        this.mEditor.invalidateActionModeAsync();
                    }
                    return true;
                case ID_CUT /*16908320*/:
                    if (!setPrimaryClip(ClipData.newPlainText(null, getTransformedText(min, max)))) {
                        Toast.makeText(getContext(), 17040054, 0).show();
                    } else if (HwDeviceManager.disallowOp(23)) {
                        Log.i(LOG_TAG, "TextView cut is not allowed by MDM!");
                        return true;
                    } else {
                        deleteText_internal(min, max);
                    }
                    sendMessageToRecsys(EVENT_CUT);
                    return true;
                case ID_COPY /*16908321*/:
                    int selStart2 = getSelectionStart();
                    int selEnd2 = getSelectionEnd();
                    if (setPrimaryClip(ClipData.newPlainText(null, getTransformedText(Math.max(0, Math.min(selStart2, selEnd2)), Math.max(0, Math.max(selStart2, selEnd2)))))) {
                        stopTextActionMode();
                    } else {
                        Toast.makeText(getContext(), 17040054, 0).show();
                    }
                    if (getTextCopyFinishedListener() != null) {
                        getTextCopyFinishedListener().copyDone();
                    }
                    sendMessageToRecsys(EVENT_COPY);
                    return true;
                case ID_PASTE /*16908322*/:
                    paste(min, max, true);
                    return true;
                default:
                    switch (id) {
                        case ID_PASTE_AS_PLAIN_TEXT /*16908337*/:
                            paste(min, max, false);
                            return true;
                        case ID_UNDO /*16908338*/:
                            if (this.mEditor != null) {
                                this.mEditor.undo();
                            }
                            return true;
                        case ID_REDO /*16908339*/:
                            if (this.mEditor != null) {
                                this.mEditor.redo();
                            }
                            return true;
                        case ID_REPLACE /*16908340*/:
                            if (this.mEditor != null) {
                                try {
                                    this.mEditor.replace();
                                } catch (Resources.NotFoundException e) {
                                    Log.e(LOG_TAG, "Widget of Editor resource not found issue.", e);
                                }
                            }
                            return true;
                        case ID_SHARE /*16908341*/:
                            shareSelectedText();
                            return true;
                        default:
                            return false;
                    }
            }
        } else {
            requestAutofill();
            stopTextActionMode();
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public CharSequence getTransformedText(int start, int end) {
        return removeSuggestionSpans(this.mTransformed.subSequence(start, end));
    }

    private void sendMessageToRecsys(String str) {
        try {
            ContentValues valuesCopy = new ContentValues();
            valuesCopy.put(EVENT_OPERATORE, str);
            Context context = getContext();
            if (context == null) {
                Log.w(LOG_TAG, "onTextContextMenuItem context is null " + str + "Connect the com.huawei.recsys.provider failed!");
                return;
            }
            ContentResolver contentResolver = context.getContentResolver();
            if (contentResolver == null) {
                Log.w(LOG_TAG, "onTextContextMenuItem contentResolver is null " + str + "Connect the com.huawei.recsys.provider failed!");
                return;
            }
            contentResolver.insert(Uri.parse(EVENT_URI_CONTENT), valuesCopy);
        } catch (IllegalArgumentException e) {
            Log.w(LOG_TAG, "onTextContextMenuItem " + str + "Connect the com.huawei.recsys.provider failed!");
        }
    }

    public boolean performLongClick() {
        boolean handled = false;
        boolean performedHapticFeedback = false;
        if (this.mEditor != null) {
            this.mEditor.mIsBeingLongClicked = true;
        }
        if (super.performLongClick()) {
            handled = true;
            performedHapticFeedback = true;
        }
        if (this.mEditor != null) {
            handled |= this.mEditor.performLongClick(handled);
            this.mEditor.mIsBeingLongClicked = false;
        }
        if (handled) {
            if (!performedHapticFeedback) {
                performHapticFeedback(0);
            }
            if (this.mEditor != null) {
                this.mEditor.mDiscardNextActionUp = true;
            }
        } else {
            MetricsLogger.action(this.mContext, 629, 0);
        }
        return handled;
    }

    /* access modifiers changed from: protected */
    public void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
        super.onScrollChanged(horiz, vert, oldHoriz, oldVert);
        if (this.mEditor != null) {
            this.mEditor.onScrollChanged();
        }
    }

    public boolean isSuggestionsEnabled() {
        boolean z = false;
        if (this.mEditor == null || (this.mEditor.mInputType & 15) != 1 || (this.mEditor.mInputType & 524288) > 0) {
            return false;
        }
        int variation = this.mEditor.mInputType & InputType.TYPE_MASK_VARIATION;
        if (variation == 0 || variation == 48 || variation == 80 || variation == 64 || variation == 160) {
            z = true;
        }
        return z;
    }

    public void setCustomSelectionActionModeCallback(ActionMode.Callback actionModeCallback) {
        createEditorIfNeeded();
        this.mEditor.mCustomSelectionActionModeCallback = actionModeCallback;
    }

    public ActionMode.Callback getCustomSelectionActionModeCallback() {
        if (this.mEditor == null) {
            return null;
        }
        return this.mEditor.mCustomSelectionActionModeCallback;
    }

    public void setCustomInsertionActionModeCallback(ActionMode.Callback actionModeCallback) {
        createEditorIfNeeded();
        this.mEditor.mCustomInsertionActionModeCallback = actionModeCallback;
    }

    public ActionMode.Callback getCustomInsertionActionModeCallback() {
        if (this.mEditor == null) {
            return null;
        }
        return this.mEditor.mCustomInsertionActionModeCallback;
    }

    public void setTextClassifier(TextClassifier textClassifier) {
        this.mTextClassifier = textClassifier;
    }

    public TextClassifier getTextClassifier() {
        if (!isDeviceProvisioned()) {
            return TextClassifier.NO_OP;
        }
        if (this.mTextClassifier != null) {
            return this.mTextClassifier;
        }
        TextClassificationManager tcm = (TextClassificationManager) this.mContext.getSystemService(TextClassificationManager.class);
        if (tcm != null) {
            return tcm.getTextClassifier();
        }
        return TextClassifier.NO_OP;
    }

    /* access modifiers changed from: package-private */
    public TextClassifier getTextClassificationSession() {
        String widgetType;
        if (this.mTextClassificationSession == null || this.mTextClassificationSession.isDestroyed()) {
            TextClassificationManager tcm = (TextClassificationManager) this.mContext.getSystemService(TextClassificationManager.class);
            if (tcm != null) {
                if (isTextEditable()) {
                    widgetType = TextClassifier.WIDGET_TYPE_EDITTEXT;
                } else if (isTextSelectable()) {
                    widgetType = TextClassifier.WIDGET_TYPE_TEXTVIEW;
                } else {
                    widgetType = TextClassifier.WIDGET_TYPE_UNSELECTABLE_TEXTVIEW;
                }
                TextClassificationContext textClassificationContext = new TextClassificationContext.Builder(this.mContext.getPackageName(), widgetType).build();
                if (this.mTextClassifier != null) {
                    this.mTextClassificationSession = tcm.createTextClassificationSession(textClassificationContext, this.mTextClassifier);
                } else {
                    this.mTextClassificationSession = tcm.createTextClassificationSession(textClassificationContext);
                }
            } else {
                this.mTextClassificationSession = TextClassifier.NO_OP;
            }
        }
        return this.mTextClassificationSession;
    }

    /* access modifiers changed from: package-private */
    public boolean usesNoOpTextClassifier() {
        return getTextClassifier() == TextClassifier.NO_OP;
    }

    public boolean requestActionMode(TextLinks.TextLinkSpan clickedSpan) {
        Preconditions.checkNotNull(clickedSpan);
        if (!(this.mText instanceof Spanned)) {
            return false;
        }
        int start = ((Spanned) this.mText).getSpanStart(clickedSpan);
        int end = ((Spanned) this.mText).getSpanEnd(clickedSpan);
        if (start < 0 || end > this.mText.length() || start >= end) {
            return false;
        }
        createEditorIfNeeded();
        this.mEditor.startLinkActionModeAsync(start, end);
        return true;
    }

    public boolean handleClick(TextLinks.TextLinkSpan clickedSpan) {
        Preconditions.checkNotNull(clickedSpan);
        if (this.mText instanceof Spanned) {
            Spanned spanned = (Spanned) this.mText;
            int start = spanned.getSpanStart(clickedSpan);
            int end = spanned.getSpanEnd(clickedSpan);
            if (start >= 0 && end <= this.mText.length() && start < end) {
                Supplier<TextClassification> supplier = new Supplier(new TextClassification.Request.Builder(this.mText, start, end).setDefaultLocales(getTextLocales()).build()) {
                    private final /* synthetic */ TextClassification.Request f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final Object get() {
                        return TextView.this.getTextClassifier().classifyText(this.f$1);
                    }
                };
                CompletableFuture.supplyAsync(supplier).completeOnTimeout(null, 1, TimeUnit.SECONDS).thenAccept($$Lambda$TextView$jQz3_DIfGrNeNdu_95_wi6UkW4E.INSTANCE);
                return true;
            }
        }
        return false;
    }

    static /* synthetic */ void lambda$handleClick$1(TextClassification classification) {
        if (classification == null) {
            Log.d(LOG_TAG, "Timeout while classifying text");
        } else if (!classification.getActions().isEmpty()) {
            try {
                classification.getActions().get(0).getActionIntent().send();
            } catch (PendingIntent.CanceledException e) {
                Log.e(LOG_TAG, "Error sending PendingIntent", e);
            }
        } else {
            Log.d(LOG_TAG, "No link action to perform");
        }
    }

    /* access modifiers changed from: protected */
    public void stopTextActionMode() {
        if (this.mEditor != null) {
            this.mEditor.stopTextActionMode();
        }
    }

    public void hideFloatingToolbar(int durationMs) {
        if (this.mEditor != null) {
            this.mEditor.hideFloatingToolbar(durationMs);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean canUndo() {
        return this.mEditor != null && this.mEditor.canUndo();
    }

    /* access modifiers changed from: package-private */
    public boolean canRedo() {
        return this.mEditor != null && this.mEditor.canRedo();
    }

    /* access modifiers changed from: package-private */
    public boolean canCut() {
        if (!hasPasswordTransformationMethod() && this.mText.length() > 0 && hasSelection() && (this.mText instanceof Editable) && this.mEditor != null && this.mEditor.mKeyListener != null) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean canCopy() {
        if (!hasPasswordTransformationMethod() && this.mText.length() > 0 && hasSelection() && this.mEditor != null) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean canShare() {
        if (!getContext().canStartActivityForResult() || !isDeviceProvisioned()) {
            return false;
        }
        return canCopy();
    }

    /* access modifiers changed from: package-private */
    public boolean isDeviceProvisioned() {
        if (this.mDeviceProvisionedState == 0) {
            this.mDeviceProvisionedState = Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0 ? 2 : 1;
        }
        if (this.mDeviceProvisionedState == 2) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean canPaste() {
        return (this.mText instanceof Editable) && this.mEditor != null && this.mEditor.mKeyListener != null && getSelectionStart() >= 0 && getSelectionEnd() >= 0 && ((ClipboardManager) getContext().getSystemService("clipboard")).hasPrimaryClip();
    }

    /* access modifiers changed from: package-private */
    public boolean canPasteAsPlainText() {
        if (!canPaste()) {
            return false;
        }
        ClipData clipData = ((ClipboardManager) getContext().getSystemService("clipboard")).getPrimaryClip();
        ClipDescription description = clipData.getDescription();
        boolean isPlainType = description.hasMimeType("text/plain");
        CharSequence text = clipData.getItemAt(0).getText();
        if (!isPlainType || !(text instanceof Spanned) || !TextUtils.hasStyleSpan((Spanned) text)) {
            return description.hasMimeType("text/html");
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean canProcessText() {
        if (getId() == -1) {
            return false;
        }
        return canShare();
    }

    /* access modifiers changed from: package-private */
    public boolean canSelectAllText() {
        return canSelectText() && !hasPasswordTransformationMethod() && !(getSelectionStart() == 0 && getSelectionEnd() == this.mText.length());
    }

    /* access modifiers changed from: package-private */
    public boolean selectAllText() {
        if (this.mEditor != null) {
            hideFloatingToolbar(500);
        }
        int length = this.mText.length();
        Selection.setSelection(this.mSpannable, 0, length);
        if (length > 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void replaceSelectionWithText(CharSequence text) {
        ((Editable) this.mText).replace(getSelectionStart(), getSelectionEnd(), text);
    }

    private void paste(int min, int max, boolean withFormatting) {
        CharSequence paste;
        ClipData clip = ((ClipboardManager) getContext().getSystemService("clipboard")).getPrimaryClip();
        if (clip != null) {
            boolean didFirst = false;
            for (int i = 0; i < clip.getItemCount(); i++) {
                if (withFormatting) {
                    paste = clip.getItemAt(i).coerceToStyledText(getContext());
                } else {
                    CharSequence text = clip.getItemAt(i).coerceToText(getContext());
                    paste = text instanceof Spanned ? text.toString() : text;
                }
                if (paste != null) {
                    if (!didFirst) {
                        Selection.setSelection(this.mSpannable, max);
                        ((Editable) this.mText).replace(min, max, paste);
                        didFirst = true;
                    } else {
                        ((Editable) this.mText).insert(getSelectionEnd(), "\n");
                        ((Editable) this.mText).insert(getSelectionEnd(), paste);
                    }
                }
            }
            sLastCutCopyOrTextChangedTime = 0;
            if (this.mBoardCast != null) {
                getContext().sendBroadcast(this.mBoardCast, PASTE_DONE_PERMISSION);
            }
        }
    }

    private void shareSelectedText() {
        String selectedText = getSelectedText();
        if (selectedText != null && !selectedText.isEmpty()) {
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null && imm.isActive(this)) {
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
            }
            Intent sharingIntent = new Intent("android.intent.action.SEND");
            sharingIntent.setType("text/plain");
            sharingIntent.removeExtra("android.intent.extra.TEXT");
            sharingIntent.putExtra("android.intent.extra.TEXT", (String) TextUtils.trimToParcelableSize(selectedText));
            getContext().startActivity(Intent.createChooser(sharingIntent, this.mContext.getResources().getText(17041089)));
            Selection.setSelection(this.mSpannable, getSelectionEnd());
        }
    }

    private boolean setPrimaryClip(ClipData clip) {
        try {
            ((ClipboardManager) getContext().getSystemService("clipboard")).setPrimaryClip(clip);
            sLastCutCopyOrTextChangedTime = SystemClock.uptimeMillis();
            return true;
        } catch (Throwable th) {
            return false;
        }
    }

    public int getOffsetForPosition(float x, float y) {
        if (getLayout() == null) {
            return -1;
        }
        return getOffsetAtCoordinate(getLineAtCoordinate(y), x);
    }

    /* access modifiers changed from: package-private */
    public float convertToLocalHorizontalCoordinate(float x) {
        return Math.min((float) ((getWidth() - getTotalPaddingRight()) - 1), Math.max(0.0f, x - ((float) getTotalPaddingLeft()))) + ((float) getScrollX());
    }

    /* access modifiers changed from: package-private */
    public int getLineAtCoordinate(float y) {
        return getLayout().getLineForVertical((int) (Math.min((float) ((getHeight() - getTotalPaddingBottom()) - 1), Math.max(0.0f, y - ((float) getTotalPaddingTop()))) + ((float) getScrollY())));
    }

    /* access modifiers changed from: package-private */
    public int getLineAtCoordinateUnclamped(float y) {
        return getLayout().getLineForVertical((int) ((y - ((float) getTotalPaddingTop())) + ((float) getScrollY())));
    }

    /* access modifiers changed from: package-private */
    public int getOffsetAtCoordinate(int line, float x) {
        return getLayout().getOffsetForHorizontal(line, convertToLocalHorizontalCoordinate(x));
    }

    public boolean onDragEvent(DragEvent event) {
        int action = event.getAction();
        boolean z = true;
        if (action != 5) {
            switch (action) {
                case 1:
                    if (this.mEditor == null || !this.mEditor.hasInsertionController()) {
                        z = false;
                    }
                    return z;
                case 2:
                    if (this.mText instanceof Spannable) {
                        Selection.setSelection(this.mSpannable, getOffsetForPosition(event.getX(), event.getY()));
                    }
                    return true;
                case 3:
                    if (this.mEditor != null) {
                        this.mEditor.onDrop(event);
                    }
                    return true;
                default:
                    return true;
            }
        } else {
            requestFocus();
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInBatchEditMode() {
        boolean z = false;
        if (this.mEditor == null) {
            return false;
        }
        Editor.InputMethodState ims = this.mEditor.mInputMethodState;
        if (ims == null) {
            return this.mEditor.mInBatchEditControllers;
        }
        if (ims.mBatchEditNesting > 0) {
            z = true;
        }
        return z;
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        TextDirectionHeuristic newTextDir = getTextDirectionHeuristic();
        if (this.mTextDir != newTextDir) {
            this.mTextDir = newTextDir;
            if (this.mLayout != null) {
                checkForRelayout();
            }
        }
    }

    /* access modifiers changed from: protected */
    public TextDirectionHeuristic getTextDirectionHeuristic() {
        TextDirectionHeuristic textDirectionHeuristic;
        if (hasPasswordTransformationMethod()) {
            return TextDirectionHeuristics.LTR;
        }
        boolean z = true;
        if (this.mEditor == null || (this.mEditor.mInputType & 15) != 3) {
            if (getLayoutDirection() != 1) {
                z = false;
            }
            boolean defaultIsRtl = z;
            switch (getTextDirection()) {
                case 2:
                    return TextDirectionHeuristics.ANYRTL_LTR;
                case 3:
                    return TextDirectionHeuristics.LTR;
                case 4:
                    return TextDirectionHeuristics.RTL;
                case 5:
                    return TextDirectionHeuristics.LOCALE;
                case 6:
                    return TextDirectionHeuristics.FIRSTSTRONG_LTR;
                case 7:
                    return TextDirectionHeuristics.FIRSTSTRONG_RTL;
                default:
                    if (defaultIsRtl) {
                        textDirectionHeuristic = TextDirectionHeuristics.FIRSTSTRONG_RTL;
                    } else {
                        textDirectionHeuristic = TextDirectionHeuristics.FIRSTSTRONG_LTR;
                    }
                    return textDirectionHeuristic;
            }
        } else {
            byte digitDirection = Character.getDirectionality(DecimalFormatSymbols.getInstance(getTextLocale()).getDigitStrings()[0].codePointAt(0));
            if (digitDirection == 1 || digitDirection == 2) {
                return TextDirectionHeuristics.RTL;
            }
            return TextDirectionHeuristics.LTR;
        }
    }

    public void onResolveDrawables(int layoutDirection) {
        if (this.mLastLayoutDirection != layoutDirection) {
            this.mLastLayoutDirection = layoutDirection;
            if (this.mDrawables != null && this.mDrawables.resolveWithLayoutDirection(layoutDirection)) {
                prepareDrawableForDisplay(this.mDrawables.mShowing[0]);
                prepareDrawableForDisplay(this.mDrawables.mShowing[2]);
                applyCompoundDrawableTint();
            }
        }
    }

    private void prepareDrawableForDisplay(Drawable dr) {
        if (dr != null) {
            dr.setLayoutDirection(getLayoutDirection());
            if (dr.isStateful()) {
                dr.setState(getDrawableState());
                dr.jumpToCurrentState();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void resetResolvedDrawables() {
        super.resetResolvedDrawables();
        this.mLastLayoutDirection = -1;
    }

    /* access modifiers changed from: protected */
    public void viewClicked(InputMethodManager imm) {
        if (imm != null) {
            imm.viewClicked(this);
        }
    }

    /* access modifiers changed from: protected */
    public void deleteText_internal(int start, int end) {
        ((Editable) this.mText).delete(start, end);
    }

    /* access modifiers changed from: protected */
    public void replaceText_internal(int start, int end, CharSequence text) {
        ((Editable) this.mText).replace(start, end, text);
    }

    /* access modifiers changed from: protected */
    public void setSpan_internal(Object span, int start, int end, int flags) {
        ((Editable) this.mText).setSpan(span, start, end, flags);
    }

    /* access modifiers changed from: protected */
    public void setCursorPosition_internal(int start, int end) {
        Selection.setSelection((Editable) this.mText, start, end);
    }

    private void createEditorIfNeeded() {
        if (this.mEditor != null) {
            return;
        }
        if (this.mHwTextView != null) {
            this.mEditor = this.mHwTextView.getEditor(this);
        } else {
            this.mEditor = new Editor(this);
        }
    }

    public CharSequence getIterableTextForAccessibility() {
        return this.mText;
    }

    private void ensureIterableTextForAccessibilitySelectable() {
        if (!(this.mText instanceof Spannable)) {
            setText(this.mText, BufferType.SPANNABLE);
        }
    }

    public AccessibilityIterators.TextSegmentIterator getIteratorForGranularity(int granularity) {
        if (granularity == 4) {
            Spannable text = (Spannable) getIterableTextForAccessibility();
            if (!TextUtils.isEmpty(text) && getLayout() != null) {
                AccessibilityIterators.LineTextSegmentIterator iterator = AccessibilityIterators.LineTextSegmentIterator.getInstance();
                iterator.initialize(text, getLayout());
                return iterator;
            }
        } else if (granularity == 16 && !TextUtils.isEmpty((Spannable) getIterableTextForAccessibility()) && getLayout() != null) {
            AccessibilityIterators.PageTextSegmentIterator iterator2 = AccessibilityIterators.PageTextSegmentIterator.getInstance();
            iterator2.initialize(this);
            return iterator2;
        }
        return super.getIteratorForGranularity(granularity);
    }

    public int getAccessibilitySelectionStart() {
        return getSelectionStart();
    }

    public boolean isAccessibilitySelectionExtendable() {
        return true;
    }

    public int getAccessibilitySelectionEnd() {
        return getSelectionEnd();
    }

    public void setAccessibilitySelection(int start, int end) {
        if (getAccessibilitySelectionStart() != start || getAccessibilitySelectionEnd() != end) {
            CharSequence text = getIterableTextForAccessibility();
            if (Math.min(start, end) < 0 || Math.max(start, end) > text.length()) {
                Selection.removeSelection((Spannable) text);
            } else {
                Selection.setSelection((Spannable) text, start, end);
            }
            if (this.mEditor != null) {
                this.mEditor.hideCursorAndSpanControllers();
                this.mEditor.stopTextActionMode();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void encodeProperties(ViewHierarchyEncoder stream) {
        super.encodeProperties(stream);
        TextUtils.TruncateAt ellipsize = getEllipsize();
        String str = null;
        stream.addProperty("text:ellipsize", ellipsize == null ? null : ellipsize.name());
        stream.addProperty("text:textSize", getTextSize());
        stream.addProperty("text:scaledTextSize", getScaledTextSize());
        stream.addProperty("text:typefaceStyle", getTypefaceStyle());
        stream.addProperty("text:selectionStart", getSelectionStart());
        stream.addProperty("text:selectionEnd", getSelectionEnd());
        stream.addProperty("text:curTextColor", this.mCurTextColor);
        if (this.mText != null) {
            str = this.mText.toString();
        }
        stream.addProperty("text:text", str);
        stream.addProperty("text:gravity", this.mGravity);
    }

    public void setCursorDrawableRes(int cursorDrawableRes) {
        this.mCursorDrawableRes = cursorDrawableRes;
    }

    public void setTextSelectHandleRes(int leftRes, int midRes, int rightRes) {
        this.mTextSelectHandleLeftRes = leftRes;
        this.mTextSelectHandleRes = midRes;
        this.mTextSelectHandleRightRes = rightRes;
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mTrySelectAllAndShowEditor && !this.mSelectAllAndShowEditorDone) {
            selectAllAndShowEditor();
            this.mSelectAllAndShowEditorDone = true;
        }
    }

    public void trySelectAllAndShowEditor() {
        this.mTrySelectAllAndShowEditor = true;
    }

    /* access modifiers changed from: protected */
    public void selectAllAndShowEditor() {
        if (this.mEditor != null) {
            this.mEditor.selectAllAndShowEditor();
        }
    }

    public void addTextCopyFinishedListener(TextCopyFinishedListener listener) {
        this.textCopyFinishedListener = listener;
    }

    public TextCopyFinishedListener getTextCopyFinishedListener() {
        return this.textCopyFinishedListener;
    }

    public Editor getEditor() {
        return this.mEditor;
    }

    public void setHwCompoundPadding(int left, int top, int right, int bottom) {
        this.mHwCompoundPaddingLeft = left;
    }
}
