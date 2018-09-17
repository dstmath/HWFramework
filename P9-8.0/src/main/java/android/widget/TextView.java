package android.widget;

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
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.BaseCanvas;
import android.graphics.Canvas;
import android.graphics.Insets;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hdm.HwDeviceManager;
import android.hwcontrol.HwWidgetFactory;
import android.hwcontrol.HwWidgetFactory.HwTextView;
import android.hwtheme.HwThemeManager;
import android.icu.text.DecimalFormatSymbols;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.ParcelableParcel;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.rms.HwSysResource;
import android.text.BoringLayout;
import android.text.BoringLayout.Metrics;
import android.text.DynamicLayout;
import android.text.Editable;
import android.text.Editable.Factory;
import android.text.GetChars;
import android.text.GraphicsOperations;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.ParcelableSpan;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.StaticLayout;
import android.text.StaticLayout.Builder;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
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
import android.text.method.TextKeyListener.Capitalize;
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
import android.util.LogException;
import android.util.TypedValue;
import android.view.AccessibilityIterators.TextSegmentIterator;
import android.view.ActionMode.Callback;
import android.view.Choreographer;
import android.view.Choreographer.FrameCallback;
import android.view.ContextMenu;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewDebug.CapturedViewProperty;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.IntToString;
import android.view.ViewHierarchyEncoder;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.ViewStructure;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.animation.AnimationUtils;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.textclassifier.TextClassificationManager;
import android.view.textclassifier.TextClassifier;
import android.view.textservice.SpellCheckerSubtype;
import android.view.textservice.TextServicesManager;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.FastMath;
import com.android.internal.widget.EditableInputConnection;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import javax.microedition.khronos.opengles.GL10;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParserException;

@RemoteView
public class TextView extends View implements OnPreDrawListener {
    private static final /* synthetic */ int[] -android-text-Layout$AlignmentSwitchesValues = null;
    static final int ACCESSIBILITY_ACTION_PROCESS_TEXT_START_ID = 268435712;
    private static final int ACCESSIBILITY_ACTION_SHARE = 268435456;
    private static final int ANIMATED_SCROLL_GAP = 250;
    public static final int AUTO_SIZE_TEXT_TYPE_NONE = 0;
    public static final int AUTO_SIZE_TEXT_TYPE_UNIFORM = 1;
    private static final int CHANGE_WATCHER_PRIORITY = 100;
    static final boolean DEBUG_AUTOFILL = false;
    static final boolean DEBUG_EXTRACT = false;
    private static final int DECIMAL = 4;
    private static final int DEFAULT_AUTO_SIZE_GRANULARITY_IN_PX = 1;
    private static final int DEFAULT_AUTO_SIZE_MAX_TEXT_SIZE_IN_SP = 112;
    private static final int DEFAULT_AUTO_SIZE_MIN_TEXT_SIZE_IN_SP = 12;
    private static final int DEVICE_PROVISIONED_NO = 1;
    private static final int DEVICE_PROVISIONED_UNKNOWN = 0;
    private static final int DEVICE_PROVISIONED_YES = 2;
    private static final Spanned EMPTY_SPANNED = new SpannedString(LogException.NO_VALUE);
    private static final int EMS = 1;
    private static final String EVENT_COPY = "ClipBoardCopy";
    private static final String EVENT_CUT = "ClipBoardClip";
    private static final String EVENT_OPERATORE = "eventOperator";
    private static final String EVENT_URI_CONTENT = "content://com.huawei.recsys.provider";
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
    private static final int[] MULTILINE_STATE_SET = new int[]{R.attr.state_multiline};
    private static final InputFilter[] NO_FILTERS = new InputFilter[0];
    private static final int PIXELS = 2;
    static final int PROCESS_TEXT_REQUEST_CODE = 100;
    private static final int SANS = 1;
    private static final int SERIF = 2;
    private static final int SIGNED = 2;
    private static final float[] TEMP_POSITION = new float[2];
    private static final RectF TEMP_RECTF = new RectF();
    private static final Metrics UNKNOWN_BORING = new Metrics();
    private static final float UNSET_AUTO_SIZE_UNIFORM_CONFIGURATION_VALUE = -1.0f;
    static final int VERY_WIDE = 1048576;
    private static boolean mIsVibrateImplemented = SystemProperties.getBoolean("ro.config.touch_vibrate", false);
    static long sLastCutCopyOrTextChangedTime;
    private boolean mAllowTransformationLengthChange;
    private int mAutoLinkMask;
    private float mAutoSizeMaxTextSizeInPx;
    private float mAutoSizeMinTextSizeInPx;
    private float mAutoSizeStepGranularityInPx;
    private int[] mAutoSizeTextSizesInPx;
    private int mAutoSizeTextType;
    private Metrics mBoring;
    private int mBreakStrategy;
    private BufferType mBufferType;
    private ChangeWatcher mChangeWatcher;
    private CharWrapper mCharWrapper;
    private int mCurHintTextColor;
    @ExportedProperty(category = "text")
    private int mCurTextColor;
    private volatile Locale mCurrentSpellCheckerLocaleCache;
    int mCursorDrawableRes;
    private int mDeferScroll;
    private int mDesiredHeightAtMeasure;
    private int mDeviceProvisionedState;
    Drawables mDrawables;
    private Factory mEditableFactory;
    private Editor mEditor;
    private TruncateAt mEllipsize;
    private InputFilter[] mFilters;
    private boolean mFreezesText;
    @ExportedProperty(category = "text")
    private int mGravity;
    private boolean mHasPresetAutoSizeValues;
    int mHighlightColor;
    private final Paint mHighlightPaint;
    private Path mHighlightPath;
    private boolean mHighlightPathBogus;
    private CharSequence mHint;
    private Metrics mHintBoring;
    private Layout mHintLayout;
    private ColorStateList mHintTextColor;
    private boolean mHorizontallyScrolling;
    private int mHwCompoundPaddingLeft;
    private HwTextView mHwTextView;
    private int mHyphenationFrequency;
    private boolean mIncludePad;
    private int mJustificationMode;
    private int mLastLayoutDirection;
    private long mLastScroll;
    private Layout mLayout;
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
    private boolean mSoftwareBold;
    private float mSpacingAdd;
    private float mSpacingMult;
    private Spannable.Factory mSpannableFactory;
    private int mStyle;
    private Rect mTempRect;
    private TextPaint mTempTextPaint;
    @ExportedProperty(category = "text")
    private CharSequence mText;
    private TextClassifier mTextClassifier;
    private ColorStateList mTextColor;
    private TextDirectionHeuristic mTextDir;
    int mTextEditSuggestionContainerLayout;
    int mTextEditSuggestionHighlightStyle;
    int mTextEditSuggestionItemLayout;
    private boolean mTextFromResource;
    private final TextPaint mTextPaint;
    int mTextSelectHandleLeftRes;
    int mTextSelectHandleRes;
    int mTextSelectHandleRightRes;
    private int mTextViewDirection;
    private TransformationMethod mTransformation;
    private CharSequence mTransformed;
    protected boolean mTrySelectAllAndShowEditor;
    private final boolean mUseInternationalizedInput;
    private boolean mUserSetTextScaleX;
    private boolean mValidSetCursorEvent;
    private TextCopyFinishedListener textCopyFinishedListener;

    public interface OnEditorActionListener {
        boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent);
    }

    public enum BufferType {
        NORMAL,
        SPANNABLE,
        EDITABLE
    }

    private class ChangeWatcher implements TextWatcher, SpanWatcher {
        private CharSequence mBeforeText;

        /* synthetic */ ChangeWatcher(TextView this$0, ChangeWatcher -this1) {
            this();
        }

        private ChangeWatcher() {
        }

        public void beforeTextChanged(CharSequence buffer, int start, int before, int after) {
            if (!(!AccessibilityManager.getInstance(TextView.this.mContext).isEnabled() || (TextView.isPasswordInputType(TextView.this.getInputType()) ^ 1) == 0 || (TextView.this.hasPasswordTransformationMethod() ^ 1) == 0)) {
                this.mBeforeText = buffer.toString();
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
        private char[] mChars;
        private int mLength;
        private int mStart;

        public CharWrapper(char[] chars, int start, int len) {
            this.mChars = chars;
            this.mStart = start;
            this.mLength = len;
        }

        void set(char[] chars, int start, int len) {
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
            int count = end - start;
            int contextCount = contextEnd - contextStart;
            c.drawTextRun(this.mChars, start + this.mStart, count, contextStart + this.mStart, contextCount, x, y, isRtl, p);
        }

        public float measureText(int start, int end, Paint p) {
            return p.measureText(this.mChars, this.mStart + start, end - start);
        }

        public int getTextWidths(int start, int end, float[] widths, Paint p) {
            return p.getTextWidths(this.mChars, this.mStart + start, end - start, widths);
        }

        public float getTextRunAdvances(int start, int end, int contextStart, int contextEnd, boolean isRtl, float[] advances, int advancesIndex, Paint p) {
            int count = end - start;
            int contextCount = contextEnd - contextStart;
            return p.getTextRunAdvances(this.mChars, start + this.mStart, count, contextStart + this.mStart, contextCount, isRtl, advances, advancesIndex);
        }

        public int getTextRunCursor(int contextStart, int contextEnd, int dir, int offset, int cursorOpt, Paint p) {
            int contextCount = contextEnd - contextStart;
            return p.getTextRunCursor(this.mChars, contextStart + this.mStart, contextCount, dir, offset + this.mStart, cursorOpt);
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
        Mode mTintMode;

        public Drawables(Context context) {
            boolean hasRtlSupport;
            if (context.getApplicationInfo().targetSdkVersion >= 17) {
                hasRtlSupport = context.getApplicationInfo().hasRtlSupport() ^ 1;
            } else {
                hasRtlSupport = true;
            }
            this.mIsRtlCompatibilityMode = hasRtlSupport;
            this.mOverride = false;
        }

        public boolean hasMetadata() {
            return (this.mDrawablePadding != 0 || this.mHasTintMode) ? true : this.mHasTint;
        }

        public boolean resolveWithLayoutDirection(int layoutDirection) {
            Drawable previousLeft = this.mShowing[0];
            Drawable previousRight = this.mShowing[2];
            this.mShowing[0] = this.mDrawableLeftInitial;
            this.mShowing[2] = this.mDrawableRightInitial;
            if (!this.mIsRtlCompatibilityMode) {
                switch (layoutDirection) {
                    case 1:
                        if (this.mOverride) {
                            this.mShowing[2] = this.mDrawableStart;
                            this.mDrawableSizeRight = this.mDrawableSizeStart;
                            this.mDrawableHeightRight = this.mDrawableHeightStart;
                            this.mShowing[0] = this.mDrawableEnd;
                            this.mDrawableSizeLeft = this.mDrawableSizeEnd;
                            this.mDrawableHeightLeft = this.mDrawableHeightEnd;
                            break;
                        }
                        break;
                    default:
                        if (this.mOverride) {
                            this.mShowing[0] = this.mDrawableStart;
                            this.mDrawableSizeLeft = this.mDrawableSizeStart;
                            this.mDrawableHeightLeft = this.mDrawableHeightStart;
                            this.mShowing[2] = this.mDrawableEnd;
                            this.mDrawableSizeRight = this.mDrawableSizeEnd;
                            this.mDrawableHeightRight = this.mDrawableHeightEnd;
                            break;
                        }
                        break;
                }
            }
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
            if (this.mDrawableError != null) {
                switch (layoutDirection) {
                    case 1:
                        this.mDrawableSaved = 1;
                        this.mDrawableTemp = this.mShowing[0];
                        this.mDrawableSizeTemp = this.mDrawableSizeLeft;
                        this.mDrawableHeightTemp = this.mDrawableHeightLeft;
                        this.mShowing[0] = this.mDrawableError;
                        this.mDrawableSizeLeft = this.mDrawableSizeError;
                        this.mDrawableHeightLeft = this.mDrawableHeightError;
                        return;
                    default:
                        this.mDrawableSaved = 0;
                        this.mDrawableTemp = this.mShowing[2];
                        this.mDrawableSizeTemp = this.mDrawableSizeRight;
                        this.mDrawableHeightTemp = this.mDrawableHeightRight;
                        this.mShowing[2] = this.mDrawableError;
                        this.mDrawableSizeRight = this.mDrawableSizeError;
                        this.mDrawableHeightRight = this.mDrawableHeightError;
                        return;
                }
            }
        }
    }

    private static final class Marquee {
        private static final int MARQUEE_DELAY = 1200;
        private static final float MARQUEE_DELTA_MAX = 0.07f;
        private static final int MARQUEE_DP_PER_SECOND = 30;
        private static final byte MARQUEE_RUNNING = (byte) 2;
        private static final byte MARQUEE_STARTING = (byte) 1;
        private static final byte MARQUEE_STOPPED = (byte) 0;
        private final Choreographer mChoreographer;
        private float mFadeStop;
        private float mGhostOffset;
        private float mGhostStart;
        private long mLastAnimationMs;
        private float mMaxFadeScroll;
        private float mMaxScroll;
        private final float mPixelsPerSecond;
        private int mRepeatLimit;
        private FrameCallback mRestartCallback = new FrameCallback() {
            public void doFrame(long frameTimeNanos) {
                if (Marquee.this.mStatus == Marquee.MARQUEE_RUNNING) {
                    if (Marquee.this.mRepeatLimit >= 0) {
                        Marquee marquee = Marquee.this;
                        marquee.mRepeatLimit = marquee.mRepeatLimit - 1;
                    }
                    Marquee.this.start(Marquee.this.mRepeatLimit);
                }
            }
        };
        private float mScroll;
        private FrameCallback mStartCallback = new FrameCallback() {
            public void doFrame(long frameTimeNanos) {
                Marquee.this.mStatus = Marquee.MARQUEE_RUNNING;
                Marquee.this.mLastAnimationMs = Marquee.this.mChoreographer.getFrameTime();
                Marquee.this.tick();
            }
        };
        private byte mStatus = (byte) 0;
        private FrameCallback mTickCallback = new FrameCallback() {
            public void doFrame(long frameTimeNanos) {
                Marquee.this.tick();
            }
        };
        private final WeakReference<TextView> mView;

        Marquee(TextView v) {
            this.mPixelsPerSecond = 30.0f * v.getContext().getResources().getDisplayMetrics().density;
            this.mView = new WeakReference(v);
            this.mChoreographer = Choreographer.getInstance();
        }

        void tick() {
            if (this.mStatus == MARQUEE_RUNNING) {
                this.mChoreographer.removeFrameCallback(this.mTickCallback);
                TextView textView = (TextView) this.mView.get();
                if (textView != null && (textView.isFocused() || textView.isSelected())) {
                    long currentMs = this.mChoreographer.getFrameTime();
                    long deltaMs = currentMs - this.mLastAnimationMs;
                    this.mLastAnimationMs = currentMs;
                    this.mScroll += (((float) deltaMs) / 1000.0f) * this.mPixelsPerSecond;
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

        void stop() {
            this.mStatus = (byte) 0;
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

        void start(int repeatLimit) {
            if (repeatLimit == 0) {
                stop();
                return;
            }
            this.mRepeatLimit = repeatLimit;
            TextView textView = (TextView) this.mView.get();
            if (!(textView == null || textView.mLayout == null)) {
                this.mStatus = MARQUEE_STARTING;
                this.mScroll = 0.0f;
                int textWidth = (textView.getWidth() - textView.getCompoundPaddingLeft()) - textView.getCompoundPaddingRight();
                float lineWidth = textView.mLayout.getLineWidth(0);
                float gap = ((float) textWidth) / 3.0f;
                this.mGhostStart = (lineWidth - ((float) textWidth)) + gap;
                this.mMaxScroll = this.mGhostStart + ((float) textWidth);
                this.mGhostOffset = lineWidth + gap;
                this.mFadeStop = (((float) textWidth) / 6.0f) + lineWidth;
                this.mMaxFadeScroll = (this.mGhostStart + lineWidth) + lineWidth;
                textView.invalidate();
                this.mChoreographer.postFrameCallback(this.mStartCallback);
            }
        }

        float getGhostOffset() {
            return this.mGhostOffset;
        }

        float getScroll() {
            return this.mScroll;
        }

        float getMaxFadeScroll() {
            return this.mMaxFadeScroll;
        }

        boolean shouldDrawLeftFade() {
            return this.mScroll <= this.mFadeStop;
        }

        boolean shouldDrawGhost() {
            return this.mStatus == MARQUEE_RUNNING && this.mScroll > this.mGhostStart;
        }

        boolean isRunning() {
            return this.mStatus == MARQUEE_RUNNING;
        }

        boolean isStopped() {
            return this.mStatus == (byte) 0;
        }
    }

    public static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
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

        /* synthetic */ SavedState(Parcel in, SavedState -this1) {
            this(in);
        }

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
            boolean z = false;
            super(in);
            this.selStart = -1;
            this.selEnd = -1;
            this.selStart = in.readInt();
            this.selEnd = in.readInt();
            if (in.readInt() != 0) {
                z = true;
            }
            this.frozenWithFocus = z;
            this.text = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            if (in.readInt() != 0) {
                this.error = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            }
            if (in.readInt() != 0) {
                this.editorState = (ParcelableParcel) ParcelableParcel.CREATOR.createFromParcel(in);
            }
        }
    }

    private static /* synthetic */ int[] -getandroid-text-Layout$AlignmentSwitchesValues() {
        if (-android-text-Layout$AlignmentSwitchesValues != null) {
            return -android-text-Layout$AlignmentSwitchesValues;
        }
        int[] iArr = new int[Alignment.values().length];
        try {
            iArr[Alignment.ALIGN_CENTER.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Alignment.ALIGN_LEFT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Alignment.ALIGN_NORMAL.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Alignment.ALIGN_OPPOSITE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Alignment.ALIGN_RIGHT.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        -android-text-Layout$AlignmentSwitchesValues = iArr;
        return iArr;
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
        this(context, attrs, R.attr.textViewStyle);
    }

    public TextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x02bd A:{Splitter: B:26:0x02b2, ExcHandler: java.lang.UnsupportedOperationException (e java.lang.UnsupportedOperationException)} */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x0644 A:{Splitter: B:121:0x0636, ExcHandler: java.lang.UnsupportedOperationException (e java.lang.UnsupportedOperationException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public TextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        int n;
        int i;
        int attr;
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mHwTextView = null;
        this.mValidSetCursorEvent = false;
        this.mEditableFactory = Factory.getInstance();
        this.mSpannableFactory = Spannable.Factory.getInstance();
        this.mTextViewDirection = 1;
        this.mSoftwareBold = false;
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
        this.mTextFromResource = false;
        if (attrs != null) {
            this.mStyle = attrs.getStyleAttribute();
        }
        this.mHwTextView = HwWidgetFactory.getHwTextView(context, this, attrs);
        if (getImportantForAutofill() == 0) {
            setImportantForAutofill(1);
        }
        this.mText = LogException.NO_VALUE;
        Resources res = getResources();
        CompatibilityInfo compat = res.getCompatibilityInfo();
        this.mTextPaint = new TextPaint(1);
        this.mTextPaint.density = res.getDisplayMetrics().density;
        this.mTextPaint.setCompatibilityScaling(compat.applicationScale);
        this.mHighlightPaint = new Paint(1);
        this.mHighlightPaint.setCompatibilityScaling(compat.applicationScale);
        this.mMovement = getDefaultMovementMethod();
        this.mTransformation = null;
        int textColorHighlight = 0;
        ColorStateList textColor = null;
        ColorStateList textColorHint = null;
        ColorStateList textColorLink = null;
        int textSize = 15;
        String fontFamily = null;
        Typeface typeface = null;
        boolean fontFamilyExplicit = false;
        int typefaceIndex = -1;
        int styleIndex = -1;
        boolean allCaps = false;
        int shadowcolor = 0;
        float dx = 0.0f;
        float dy = 0.0f;
        float r = 0.0f;
        boolean elegant = false;
        float letterSpacing = 0.0f;
        String fontFeatureSettings = null;
        this.mBreakStrategy = 0;
        this.mHyphenationFrequency = 0;
        this.mJustificationMode = 0;
        Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(attrs, R.styleable.TextViewAppearance, defStyleAttr, defStyleRes);
        TypedArray appearance = null;
        int ap = a.getResourceId(0, -1);
        a.recycle();
        if (ap != -1) {
            appearance = theme.obtainStyledAttributes(ap, R.styleable.TextAppearance);
        }
        if (appearance != null) {
            n = appearance.getIndexCount();
            for (i = 0; i < n; i++) {
                attr = appearance.getIndex(i);
                switch (attr) {
                    case 0:
                        textSize = appearance.getDimensionPixelSize(attr, textSize);
                        break;
                    case 1:
                        typefaceIndex = appearance.getInt(attr, -1);
                        break;
                    case 2:
                        styleIndex = appearance.getInt(attr, -1);
                        break;
                    case 3:
                        textColor = appearance.getColorStateList(attr);
                        break;
                    case 4:
                        textColorHighlight = appearance.getColor(attr, textColorHighlight);
                        break;
                    case 5:
                        textColorHint = appearance.getColorStateList(attr);
                        break;
                    case 6:
                        textColorLink = appearance.getColorStateList(attr);
                        break;
                    case 7:
                        shadowcolor = HwThemeManager.getShadowcolor(appearance, attr);
                        break;
                    case 8:
                        dx = appearance.getFloat(attr, 0.0f);
                        break;
                    case 9:
                        dy = appearance.getFloat(attr, 0.0f);
                        break;
                    case 10:
                        r = appearance.getFloat(attr, 0.0f);
                        break;
                    case 11:
                        allCaps = appearance.getBoolean(attr, false);
                        break;
                    case 12:
                        if (!context.isRestricted() && context.canLoadUnsafeResources()) {
                            try {
                                typeface = appearance.getFont(attr);
                            } catch (UnsupportedOperationException e) {
                            }
                        }
                        if (typeface != null) {
                            break;
                        }
                        fontFamily = appearance.getString(attr);
                        break;
                    case 13:
                        elegant = appearance.getBoolean(attr, false);
                        break;
                    case 14:
                        letterSpacing = appearance.getFloat(attr, 0.0f);
                        break;
                    case 15:
                        fontFeatureSettings = appearance.getString(attr);
                        break;
                    default:
                        break;
                }
            }
            appearance.recycle();
        }
        boolean editable = getDefaultEditable();
        CharSequence inputMethod = null;
        int numeric = 0;
        CharSequence digits = null;
        boolean phone = false;
        boolean autotext = false;
        int autocap = -1;
        int buffertype = 0;
        boolean selectallonfocus = false;
        Drawable drawableLeft = null;
        Drawable drawableTop = null;
        Drawable drawableRight = null;
        Drawable drawableBottom = null;
        Drawable drawableStart = null;
        Drawable drawableEnd = null;
        ColorStateList drawableTint = null;
        Mode drawableTintMode = null;
        int drawablePadding = 0;
        int ellipsize = -1;
        boolean singleLine = false;
        int maxlength = -1;
        CharSequence text = LogException.NO_VALUE;
        CharSequence hint = null;
        boolean password = false;
        float autoSizeMinTextSizeInPx = -1.0f;
        float autoSizeMaxTextSizeInPx = -1.0f;
        float autoSizeStepGranularityInPx = -1.0f;
        int inputType = 0;
        a = theme.obtainStyledAttributes(attrs, R.styleable.TextView, defStyleAttr, defStyleRes);
        n = a.getIndexCount();
        Locale loc = Locale.getDefault();
        if ("my".equals(loc.getLanguage()) && "MM".equals(loc.getCountry())) {
            this.mSpacingMult = MSPACINGMULTFORMY;
        } else if ("si".equals(loc.getLanguage())) {
            this.mSpacingMult = MSPACINGMULTFORSI;
        }
        boolean fromResourceId = false;
        for (i = 0; i < n; i++) {
            attr = a.getIndex(i);
            switch (attr) {
                case 0:
                    setEnabled(a.getBoolean(attr, isEnabled()));
                    break;
                case 2:
                    textSize = a.getDimensionPixelSize(attr, textSize);
                    break;
                case 3:
                    typefaceIndex = a.getInt(attr, typefaceIndex);
                    break;
                case 4:
                    styleIndex = a.getInt(attr, styleIndex);
                    break;
                case 5:
                    textColor = a.getColorStateList(attr);
                    break;
                case 6:
                    textColorHighlight = a.getColor(attr, textColorHighlight);
                    break;
                case 7:
                    textColorHint = a.getColorStateList(attr);
                    break;
                case 8:
                    textColorLink = a.getColorStateList(attr);
                    break;
                case 9:
                    ellipsize = a.getInt(attr, ellipsize);
                    break;
                case 10:
                    setGravity(a.getInt(attr, -1));
                    break;
                case 11:
                    this.mAutoLinkMask = a.getInt(attr, 0);
                    break;
                case 12:
                    this.mLinksClickable = a.getBoolean(attr, true);
                    break;
                case 13:
                    setMaxWidth(a.getDimensionPixelSize(attr, -1));
                    break;
                case 14:
                    setMaxHeight(a.getDimensionPixelSize(attr, -1));
                    break;
                case 15:
                    setMinWidth(a.getDimensionPixelSize(attr, -1));
                    break;
                case 16:
                    setMinHeight(a.getDimensionPixelSize(attr, -1));
                    break;
                case 17:
                    buffertype = a.getInt(attr, buffertype);
                    break;
                case 18:
                    fromResourceId = true;
                    text = a.getText(attr);
                    break;
                case 19:
                    hint = a.getText(attr);
                    break;
                case 20:
                    setTextScaleX(a.getFloat(attr, 1.0f));
                    break;
                case 21:
                    if (!a.getBoolean(attr, true)) {
                        setCursorVisible(false);
                        break;
                    }
                    break;
                case 22:
                    setMaxLines(a.getInt(attr, -1));
                    break;
                case 23:
                    setLines(a.getInt(attr, -1));
                    break;
                case 24:
                    setHeight(a.getDimensionPixelSize(attr, -1));
                    break;
                case 25:
                    setMinLines(a.getInt(attr, -1));
                    break;
                case 26:
                    setMaxEms(a.getInt(attr, -1));
                    break;
                case 27:
                    setEms(a.getInt(attr, -1));
                    break;
                case 28:
                    setWidth(a.getDimensionPixelSize(attr, -1));
                    break;
                case 29:
                    setMinEms(a.getInt(attr, -1));
                    break;
                case 30:
                    if (!a.getBoolean(attr, false)) {
                        break;
                    }
                    setHorizontallyScrolling(true);
                    break;
                case 31:
                    password = a.getBoolean(attr, password);
                    break;
                case 32:
                    singleLine = a.getBoolean(attr, singleLine);
                    break;
                case 33:
                    selectallonfocus = a.getBoolean(attr, selectallonfocus);
                    break;
                case 34:
                    if (!a.getBoolean(attr, true)) {
                        setIncludeFontPadding(false);
                        break;
                    }
                    break;
                case 35:
                    maxlength = a.getInt(attr, -1);
                    break;
                case 36:
                    shadowcolor = HwThemeManager.getShadowcolor(a, attr);
                    break;
                case 37:
                    dx = a.getFloat(attr, 0.0f);
                    break;
                case 38:
                    dy = a.getFloat(attr, 0.0f);
                    break;
                case 39:
                    r = a.getFloat(attr, 0.0f);
                    break;
                case 40:
                    numeric = a.getInt(attr, numeric);
                    break;
                case 41:
                    digits = a.getText(attr);
                    break;
                case 42:
                    phone = a.getBoolean(attr, phone);
                    break;
                case 43:
                    inputMethod = a.getText(attr);
                    break;
                case 44:
                    autocap = a.getInt(attr, autocap);
                    break;
                case 45:
                    autotext = a.getBoolean(attr, autotext);
                    break;
                case 46:
                    editable = a.getBoolean(attr, editable);
                    break;
                case 47:
                    this.mFreezesText = a.getBoolean(attr, false);
                    break;
                case 48:
                    drawableTop = a.getDrawable(attr);
                    break;
                case 49:
                    drawableBottom = a.getDrawable(attr);
                    break;
                case 50:
                    drawableLeft = a.getDrawable(attr);
                    break;
                case 51:
                    drawableRight = a.getDrawable(attr);
                    break;
                case 52:
                    drawablePadding = a.getDimensionPixelSize(attr, drawablePadding);
                    break;
                case 53:
                    this.mSpacingAdd = (float) a.getDimensionPixelSize(attr, (int) this.mSpacingAdd);
                    break;
                case 54:
                    this.mSpacingMult = a.getFloat(attr, this.mSpacingMult);
                    if (!"my".equals(loc.getLanguage()) || !"MM".equals(loc.getCountry()) || this.mSpacingMult >= MSPACINGMULTFORMY) {
                        if ("si".equals(loc.getLanguage()) && this.mSpacingMult < MSPACINGMULTFORSI) {
                            this.mSpacingMult = MSPACINGMULTFORSI;
                            break;
                        }
                    }
                    this.mSpacingMult = MSPACINGMULTFORMY;
                    break;
                    break;
                case 55:
                    setMarqueeRepeatLimit(a.getInt(attr, this.mMarqueeRepeatLimit));
                    break;
                case 56:
                    inputType = a.getInt(attr, 0);
                    break;
                case 57:
                    setPrivateImeOptions(a.getString(attr));
                    break;
                case 58:
                    try {
                        setInputExtras(a.getResourceId(attr, 0));
                        break;
                    } catch (Throwable e2) {
                        Log.w(LOG_TAG, "Failure reading input extras", e2);
                        break;
                    } catch (Throwable e3) {
                        Log.w(LOG_TAG, "Failure reading input extras", e3);
                        break;
                    }
                case 59:
                    createEditorIfNeeded();
                    this.mEditor.createInputContentTypeIfNeeded();
                    this.mEditor.mInputContentType.imeOptions = a.getInt(attr, this.mEditor.mInputContentType.imeOptions);
                    break;
                case 60:
                    createEditorIfNeeded();
                    this.mEditor.createInputContentTypeIfNeeded();
                    this.mEditor.mInputContentType.imeActionLabel = a.getText(attr);
                    break;
                case 61:
                    createEditorIfNeeded();
                    this.mEditor.createInputContentTypeIfNeeded();
                    this.mEditor.mInputContentType.imeActionId = a.getInt(attr, this.mEditor.mInputContentType.imeActionId);
                    break;
                case 62:
                    this.mTextSelectHandleLeftRes = a.getResourceId(attr, 0);
                    break;
                case 63:
                    this.mTextSelectHandleRightRes = a.getResourceId(attr, 0);
                    break;
                case 64:
                    this.mTextSelectHandleRes = a.getResourceId(attr, 0);
                    break;
                case 67:
                    setTextIsSelectable(a.getBoolean(attr, false));
                    break;
                case 70:
                    this.mCursorDrawableRes = a.getResourceId(attr, 0);
                    break;
                case 71:
                    this.mTextEditSuggestionItemLayout = a.getResourceId(attr, 0);
                    break;
                case 72:
                    allCaps = a.getBoolean(attr, false);
                    break;
                case 73:
                    drawableStart = a.getDrawable(attr);
                    break;
                case 74:
                    drawableEnd = a.getDrawable(attr);
                    break;
                case 75:
                    if (!context.isRestricted() && context.canLoadUnsafeResources()) {
                        try {
                            typeface = a.getFont(attr);
                        } catch (UnsupportedOperationException e4) {
                        }
                    }
                    if (typeface == null) {
                        fontFamily = a.getString(attr);
                    }
                    fontFamilyExplicit = true;
                    break;
                case 76:
                    elegant = a.getBoolean(attr, false);
                    break;
                case 77:
                    letterSpacing = a.getFloat(attr, 0.0f);
                    break;
                case 78:
                    fontFeatureSettings = a.getString(attr);
                    break;
                case 79:
                    drawableTint = a.getColorStateList(attr);
                    break;
                case 80:
                    drawableTintMode = Drawable.parseTintMode(a.getInt(attr, -1), drawableTintMode);
                    break;
                case 81:
                    this.mBreakStrategy = a.getInt(attr, 0);
                    break;
                case 82:
                    this.mHyphenationFrequency = a.getInt(attr, 0);
                    break;
                case 83:
                    createEditorIfNeeded();
                    this.mEditor.mAllowUndo = a.getBoolean(attr, true);
                    break;
                case 84:
                    this.mAutoSizeTextType = a.getInt(attr, 0);
                    break;
                case 85:
                    autoSizeStepGranularityInPx = a.getDimension(attr, -1.0f);
                    break;
                case 86:
                    int autoSizeStepSizeArrayResId = a.getResourceId(attr, 0);
                    if (autoSizeStepSizeArrayResId <= 0) {
                        break;
                    }
                    TypedArray autoSizePresetTextSizes = a.getResources().obtainTypedArray(autoSizeStepSizeArrayResId);
                    setupAutoSizeUniformPresetSizes(autoSizePresetTextSizes);
                    autoSizePresetTextSizes.recycle();
                    break;
                case 87:
                    autoSizeMinTextSizeInPx = a.getDimension(attr, -1.0f);
                    break;
                case 88:
                    autoSizeMaxTextSizeInPx = a.getDimension(attr, -1.0f);
                    break;
                case 89:
                    this.mJustificationMode = a.getInt(attr, 0);
                    break;
                case 90:
                    this.mTextEditSuggestionContainerLayout = a.getResourceId(attr, 0);
                    break;
                case 91:
                    this.mTextEditSuggestionHighlightStyle = a.getResourceId(attr, 0);
                    break;
                default:
                    break;
            }
        }
        a.recycle();
        BufferType bufferType = BufferType.EDITABLE;
        int variation = inputType & 4095;
        boolean passwordInputType = variation == 129;
        boolean webPasswordInputType = variation == 225;
        boolean numberPasswordInputType = variation == 18;
        this.mUseInternationalizedInput = context.getApplicationInfo().targetSdkVersion >= 26;
        Editor editor;
        int i2;
        if (inputMethod == null) {
            if (digits == null) {
                if (inputType == 0) {
                    if (!phone) {
                        if (numeric == 0) {
                            if (!autotext && autocap == -1) {
                                if (!editable) {
                                    if (!isTextSelectable()) {
                                        if (this.mEditor != null) {
                                            this.mEditor.mKeyListener = null;
                                        }
                                        switch (buffertype) {
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
                                    }
                                    if (this.mEditor != null) {
                                        this.mEditor.mKeyListener = null;
                                        this.mEditor.mInputType = 0;
                                    }
                                    bufferType = BufferType.SPANNABLE;
                                    setMovementMethod(ArrowKeyMovementMethod.getInstance());
                                } else {
                                    createEditorIfNeeded();
                                    this.mEditor.mKeyListener = TextKeyListener.getInstance();
                                    this.mEditor.mInputType = 1;
                                }
                            } else {
                                Capitalize cap;
                                inputType = 1;
                                switch (autocap) {
                                    case 1:
                                        cap = Capitalize.SENTENCES;
                                        inputType = GL10.GL_LIGHT1;
                                        break;
                                    case 2:
                                        cap = Capitalize.WORDS;
                                        inputType = 8193;
                                        break;
                                    case 3:
                                        cap = Capitalize.CHARACTERS;
                                        inputType = 4097;
                                        break;
                                    default:
                                        cap = Capitalize.NONE;
                                        break;
                                }
                                createEditorIfNeeded();
                                this.mEditor.mKeyListener = TextKeyListener.getInstance(autotext, cap);
                                this.mEditor.mInputType = inputType;
                            }
                        } else {
                            createEditorIfNeeded();
                            this.mEditor.mKeyListener = DigitsKeyListener.getInstance(null, (numeric & 2) != 0, (numeric & 4) != 0);
                            this.mEditor.mInputType = this.mEditor.mKeyListener.getInputType();
                        }
                    } else {
                        createEditorIfNeeded();
                        this.mEditor.mKeyListener = DialerKeyListener.getInstance();
                        this.mEditor.mInputType = 3;
                    }
                } else {
                    setInputType(inputType, true);
                    singleLine = isMultilineInputType(inputType) ^ 1;
                }
            } else {
                createEditorIfNeeded();
                this.mEditor.mKeyListener = DigitsKeyListener.getInstance(digits.toString());
                editor = this.mEditor;
                if (inputType != 0) {
                    i2 = inputType;
                } else {
                    i2 = 1;
                }
                editor.mInputType = i2;
            }
        } else {
            try {
                Class<?> c = Class.forName(inputMethod.toString());
                try {
                    createEditorIfNeeded();
                    this.mEditor.mKeyListener = (KeyListener) c.newInstance();
                    try {
                        editor = this.mEditor;
                        if (inputType != 0) {
                            i2 = inputType;
                        } else {
                            i2 = this.mEditor.mKeyListener.getInputType();
                        }
                        editor.mInputType = i2;
                    } catch (IncompatibleClassChangeError e5) {
                        this.mEditor.mInputType = 1;
                    }
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                } catch (Throwable ex2) {
                    throw new RuntimeException(ex2);
                }
            } catch (Throwable ex3) {
                throw new RuntimeException(ex3);
            }
        }
        if (this.mEditor != null) {
            this.mEditor.adjustInputType(password, passwordInputType, webPasswordInputType, numberPasswordInputType);
        }
        if (selectallonfocus) {
            createEditorIfNeeded();
            this.mEditor.mSelectAllOnFocus = true;
            if (bufferType == BufferType.NORMAL) {
                bufferType = BufferType.SPANNABLE;
            }
        }
        if (!(drawableTint == null && drawableTintMode == null)) {
            if (this.mDrawables == null) {
                this.mDrawables = new Drawables(context);
            }
            if (drawableTint != null) {
                this.mDrawables.mTintList = drawableTint;
                this.mDrawables.mHasTint = true;
            }
            if (drawableTintMode != null) {
                this.mDrawables.mTintMode = drawableTintMode;
                this.mDrawables.mHasTintMode = true;
            }
        }
        setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
        setRelativeDrawablesIfNeeded(drawableStart, drawableEnd);
        setCompoundDrawablePadding(drawablePadding);
        setInputTypeSingleLine(singleLine);
        applySingleLine(singleLine, singleLine, singleLine);
        if (singleLine && getKeyListener() == null && ellipsize < 0) {
            ellipsize = 3;
        }
        switch (ellipsize) {
            case 1:
                setEllipsize(TruncateAt.START);
                break;
            case 2:
                setEllipsize(TruncateAt.MIDDLE);
                break;
            case 3:
                setEllipsize(TruncateAt.END);
                break;
            case 4:
                if (ViewConfiguration.get(context).isFadingMarqueeEnabled()) {
                    setHorizontalFadingEdgeEnabled(true);
                    this.mMarqueeFadeMode = 0;
                } else {
                    setHorizontalFadingEdgeEnabled(false);
                    this.mMarqueeFadeMode = 1;
                }
                setEllipsize(TruncateAt.MARQUEE);
                break;
        }
        if (textColor == null) {
            textColor = ColorStateList.valueOf(-16777216);
        }
        setTextColor(textColor);
        setHintTextColor(textColorHint);
        setLinkTextColor(textColorLink);
        if (textColorHighlight != 0) {
            setHighlightColor(textColorHighlight);
        }
        setRawTextSize((float) textSize, true);
        setElegantTextHeight(elegant);
        setLetterSpacing(letterSpacing);
        setFontFeatureSettings(fontFeatureSettings);
        if (allCaps) {
            setTransformationMethod(new AllCapsTransformationMethod(getContext()));
        }
        if (password || passwordInputType || webPasswordInputType || numberPasswordInputType) {
            setTransformationMethod(PasswordTransformationMethod.getInstance());
            typefaceIndex = 3;
        } else if (this.mEditor != null && (this.mEditor.mInputType & 4095) == 129) {
            typefaceIndex = 3;
        }
        if (!(typefaceIndex == -1 || (fontFamilyExplicit ^ 1) == 0)) {
            fontFamily = null;
        }
        setTypefaceFromAttrs(typeface, fontFamily, typefaceIndex, styleIndex);
        if (shadowcolor != 0) {
            setShadowLayer(r, dx, dy, shadowcolor);
        }
        if (maxlength >= 0) {
            setFilters(new InputFilter[]{new LengthFilter(maxlength)});
        } else {
            setFilters(NO_FILTERS);
        }
        setText(text, bufferType);
        if (fromResourceId) {
            this.mTextFromResource = true;
        }
        if (hint != null) {
            setHint(hint);
        }
        boolean canInputOrMove = (this.mMovement == null && getKeyListener() == null) ? false : true;
        boolean clickable = !canInputOrMove ? isClickable() : true;
        boolean longClickable = !canInputOrMove ? isLongClickable() : true;
        int focusable = getFocusable();
        a = context.obtainStyledAttributes(attrs, R.styleable.View, defStyleAttr, defStyleRes);
        TypedValue val = new TypedValue();
        if (a.getValue(19, val)) {
            focusable = val.type == 18 ? val.data == 0 ? 0 : 1 : val.data;
        }
        clickable = a.getBoolean(30, clickable);
        longClickable = a.getBoolean(31, longClickable);
        a.recycle();
        if (focusable != getFocusable()) {
            setFocusable(focusable);
        }
        setClickable(clickable);
        setLongClickable(longClickable);
        if (this.mEditor != null) {
            this.mEditor.prepareCursorControllers();
        }
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
        if (!supportsAutoSizeText()) {
            this.mAutoSizeTextType = 0;
        } else if (this.mAutoSizeTextType == 1) {
            if (!this.mHasPresetAutoSizeValues) {
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                if (autoSizeMinTextSizeInPx == -1.0f) {
                    autoSizeMinTextSizeInPx = TypedValue.applyDimension(2, 12.0f, displayMetrics);
                }
                if (autoSizeMaxTextSizeInPx == -1.0f) {
                    autoSizeMaxTextSizeInPx = TypedValue.applyDimension(2, 112.0f, displayMetrics);
                }
                if (autoSizeStepGranularityInPx == -1.0f) {
                    autoSizeStepGranularityInPx = 1.0f;
                }
                validateAndSetAutoSizeTextTypeUniformConfiguration(autoSizeMinTextSizeInPx, autoSizeMaxTextSizeInPx, autoSizeStepGranularityInPx);
            }
            setupAutoSizeText();
        }
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
            }
            this.mHasPresetAutoSizeValues = false;
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
            throw new IllegalArgumentException("Maximum auto-size text size (" + autoSizeMaxTextSizeInPx + "px) is less or equal to minimum auto-size " + "text size (" + autoSizeMinTextSizeInPx + "px)");
        } else if (autoSizeStepGranularityInPx <= 0.0f) {
            throw new IllegalArgumentException("The auto-size step granularity (" + autoSizeStepGranularityInPx + "px) is less or equal to (0px)");
        } else {
            this.mAutoSizeTextType = 1;
            this.mAutoSizeMinTextSizeInPx = autoSizeMinTextSizeInPx;
            this.mAutoSizeMaxTextSizeInPx = autoSizeMaxTextSizeInPx;
            this.mAutoSizeStepGranularityInPx = autoSizeStepGranularityInPx;
            this.mHasPresetAutoSizeValues = false;
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
        if (presetValuesLength != uniqueValidSizes.size()) {
            presetValues = uniqueValidSizes.toArray();
        }
        return presetValues;
    }

    private boolean setupAutoSizeText() {
        if (supportsAutoSizeText() && this.mAutoSizeTextType == 1) {
            if (!this.mHasPresetAutoSizeValues || this.mAutoSizeTextSizesInPx.length == 0) {
                int autoSizeValuesLength = ((int) Math.floor((double) ((this.mAutoSizeMaxTextSizeInPx - this.mAutoSizeMinTextSizeInPx) / this.mAutoSizeStepGranularityInPx))) + 1;
                int[] autoSizeTextSizesInPx = new int[autoSizeValuesLength];
                for (int i = 0; i < autoSizeValuesLength; i++) {
                    autoSizeTextSizesInPx[i] = Math.round(this.mAutoSizeMinTextSizeInPx + (((float) i) * this.mAutoSizeStepGranularityInPx));
                }
                this.mAutoSizeTextSizesInPx = cleanupAutoSizePresetSizes(autoSizeTextSizesInPx);
            }
            this.mNeedsAutoSizeText = true;
        } else {
            this.mNeedsAutoSizeText = false;
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
                Toast.makeText(getContext(), String.valueOf(result), 1).show();
            }
        } else if (this.mText instanceof Spannable) {
            Selection.setSelection((Spannable) this.mText, getSelectionEnd());
        }
    }

    private void setTypefaceFromAttrs(Typeface fontTypeface, String familyName, int typefaceIndex, int styleIndex) {
        Typeface tf = fontTypeface;
        if (fontTypeface == null && familyName != null) {
            tf = Typeface.create(familyName, styleIndex);
        } else if (!(fontTypeface == null || fontTypeface.getStyle() == styleIndex)) {
            tf = Typeface.create(fontTypeface, styleIndex);
        }
        if (tf != null) {
            setTypeface(tf);
            return;
        }
        switch (typefaceIndex) {
            case 1:
                tf = Typeface.SANS_SERIF;
                break;
            case 2:
                tf = Typeface.SERIF;
                break;
            case 3:
                tf = Typeface.MONOSPACE;
                break;
        }
        setTypeface(tf, styleIndex);
    }

    private void setRelativeDrawablesIfNeeded(Drawable start, Drawable end) {
        boolean hasRelativeDrawables = (start == null && end == null) ? false : true;
        if (hasRelativeDrawables) {
            Drawables dr = this.mDrawables;
            if (dr == null) {
                dr = new Drawables(getContext());
                this.mDrawables = dr;
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
            InputMethodManager imm;
            if (!enabled) {
                imm = InputMethodManager.peekInstance();
                if (imm != null && imm.isActive(this)) {
                    imm.hideSoftInputFromWindow(getWindowToken(), 0);
                }
            }
            super.setEnabled(enabled);
            if (enabled) {
                imm = InputMethodManager.peekInstance();
                if (imm != null) {
                    imm.restartInput(this);
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
        boolean z = false;
        if (style > 0) {
            float f;
            if (tf == null) {
                tf = Typeface.defaultFromStyle(style);
            } else {
                tf = Typeface.create(tf, style);
            }
            setTypeface(tf);
            int need = style & (~(tf != null ? tf.getStyle() : 0));
            TextPaint textPaint = this.mTextPaint;
            if ((need & 1) != 0) {
                z = true;
            }
            textPaint.setFakeBoldText(z);
            textPaint = this.mTextPaint;
            if ((need & 2) != 0) {
                f = -0.25f;
            } else {
                f = 0.0f;
            }
            textPaint.setTextSkewX(f);
            return;
        }
        this.mTextPaint.setFakeBoldText(false);
        this.mTextPaint.setTextSkewX(0.0f);
        setTypeface(tf);
    }

    protected boolean getDefaultEditable() {
        return false;
    }

    protected MovementMethod getDefaultMovementMethod() {
        return null;
    }

    @CapturedViewProperty
    public CharSequence getText() {
        return this.mText;
    }

    public int length() {
        return this.mText.length();
    }

    public Editable getEditableText() {
        return this.mText instanceof Editable ? (Editable) this.mText : null;
    }

    public int getLineHeight() {
        return FastMath.round((((float) this.mTextPaint.getFontMetricsInt(null)) * this.mSpacingMult) + this.mSpacingAdd);
    }

    public final Layout getLayout() {
        return this.mLayout;
    }

    final Layout getHintLayout() {
        return this.mHintLayout;
    }

    public final UndoManager getUndoManager() {
        throw new UnsupportedOperationException("not implemented");
    }

    public final Editor getEditorForTesting() {
        return this.mEditor;
    }

    public final void setUndoManager(UndoManager undoManager, String tag) {
        throw new UnsupportedOperationException("not implemented");
    }

    public final KeyListener getKeyListener() {
        return this.mEditor == null ? null : this.mEditor.mKeyListener;
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
                if (!(input == null || ((this.mText instanceof Editable) ^ 1) == 0)) {
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
            if (!(movement == null || ((this.mText instanceof Spannable) ^ 1) == 0)) {
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
            if (this.mTransformation != null && (this.mText instanceof Spannable)) {
                ((Spannable) this.mText).removeSpan(this.mTransformation);
            }
            this.mTransformation = method;
            if (method instanceof TransformationMethod2) {
                boolean z;
                TransformationMethod2 method2 = (TransformationMethod2) method;
                if (isTextSelectable()) {
                    z = false;
                } else {
                    z = (this.mText instanceof Editable) ^ 1;
                }
                this.mAllowTransformationLengthChange = z;
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
        return (this.mPaddingTop + dr.mDrawablePadding) + dr.mDrawableSizeTop;
    }

    public int getCompoundPaddingBottom() {
        Drawables dr = this.mDrawables;
        if (dr == null || dr.mShowing[3] == null) {
            return this.mPaddingBottom;
        }
        return (this.mPaddingBottom + dr.mDrawablePadding) + dr.mDrawableSizeBottom;
    }

    public int getCompoundPaddingLeft() {
        Drawables dr = this.mDrawables;
        if (dr == null || dr.mShowing[0] == null) {
            return this.mPaddingLeft;
        }
        return (this.mPaddingLeft + dr.mDrawablePadding) + dr.mDrawableSizeLeft;
    }

    public int getCompoundPaddingRight() {
        Drawables dr = this.mDrawables;
        if (dr == null || dr.mShowing[2] == null) {
            return this.mPaddingRight;
        }
        return (this.mPaddingRight + dr.mDrawablePadding) + dr.mDrawableSizeRight;
    }

    public int getCompoundPaddingStart() {
        resolveDrawables();
        switch (getLayoutDirection()) {
            case 1:
                return getCompoundPaddingRight();
            default:
                return getCompoundPaddingLeft();
        }
    }

    public int getCompoundPaddingEnd() {
        resolveDrawables();
        switch (getLayoutDirection()) {
            case 1:
                return getCompoundPaddingLeft();
            default:
                return getCompoundPaddingRight();
        }
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
        boolean drawables = (left == null && top == null && right == null && bottom == null) ? false : true;
        if (drawables) {
            if (dr == null) {
                dr = new Drawables(getContext());
                this.mDrawables = dr;
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
            if (dr.hasMetadata()) {
                for (int i = dr.mShowing.length - 1; i >= 0; i--) {
                    if (dr.mShowing[i] != null) {
                        dr.mShowing[i].setCallback(null);
                    }
                    dr.mShowing[i] = null;
                }
                dr.mDrawableHeightLeft = 0;
                dr.mDrawableSizeLeft = 0;
                dr.mDrawableHeightRight = 0;
                dr.mDrawableSizeRight = 0;
                dr.mDrawableWidthTop = 0;
                dr.mDrawableSizeTop = 0;
                dr.mDrawableWidthBottom = 0;
                dr.mDrawableSizeBottom = 0;
            } else {
                this.mDrawables = null;
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
        Drawable drawable4 = null;
        Context context = getContext();
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
            drawableArr = dr.mShowing;
            dr.mDrawableRightInitial = null;
            drawableArr[2] = null;
            dr.mDrawableHeightLeft = 0;
            dr.mDrawableSizeLeft = 0;
            dr.mDrawableHeightRight = 0;
            dr.mDrawableSizeRight = 0;
        }
        boolean drawables = (start == null && top == null && end == null) ? bottom != null : true;
        if (drawables) {
            if (dr == null) {
                dr = new Drawables(getContext());
                this.mDrawables = dr;
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
            if (dr.hasMetadata()) {
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
            } else {
                this.mDrawables = null;
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
        Drawable drawable4 = null;
        Context context = getContext();
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
        if (this.mDrawables != null) {
            return new Drawable[]{this.mDrawables.mDrawableStart, this.mDrawables.mShowing[1], this.mDrawables.mDrawableEnd, this.mDrawables.mShowing[3]};
        }
        return new Drawable[]{null, null, null, null};
    }

    @RemotableViewMethod
    public void setCompoundDrawablePadding(int pad) {
        Drawables dr = this.mDrawables;
        if (pad != 0) {
            if (dr == null) {
                dr = new Drawables(getContext());
                this.mDrawables = dr;
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
        return dr != null ? dr.mDrawablePadding : 0;
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
        return this.mDrawables != null ? this.mDrawables.mTintList : null;
    }

    public void setCompoundDrawableTintMode(Mode tintMode) {
        if (this.mDrawables == null) {
            this.mDrawables = new Drawables(getContext());
        }
        this.mDrawables.mTintMode = tintMode;
        this.mDrawables.mHasTintMode = true;
        applyCompoundDrawableTint();
    }

    public Mode getCompoundDrawableTintMode() {
        return this.mDrawables != null ? this.mDrawables.mTintMode : null;
    }

    private void applyCompoundDrawableTint() {
        if (this.mDrawables != null) {
            if (this.mDrawables.mHasTint || this.mDrawables.mHasTintMode) {
                ColorStateList tintList = this.mDrawables.mTintList;
                Mode tintMode = this.mDrawables.mTintMode;
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

    public final int getAutoLinkMask() {
        return this.mAutoLinkMask;
    }

    public void setTextAppearance(int resId) {
        setTextAppearance(this.mContext, resId);
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x0155 A:{Splitter: B:20:0x007a, ExcHandler: java.lang.UnsupportedOperationException (e java.lang.UnsupportedOperationException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @Deprecated
    public void setTextAppearance(Context context, int resId) {
        TypedArray ta = context.obtainStyledAttributes(resId, android.R.styleable.TextAppearance);
        int textColorHighlight = ta.getColor(4, 0);
        if (textColorHighlight != 0) {
            setHighlightColor(textColorHighlight);
        }
        ColorStateList textColor = ta.getColorStateList(3);
        if (textColor != null) {
            setTextColor(textColor);
        }
        int textSize = ta.getDimensionPixelSize(0, 0);
        if (textSize != 0) {
            setRawTextSize((float) textSize, true);
        }
        ColorStateList textColorHint = ta.getColorStateList(5);
        if (textColorHint != null) {
            setHintTextColor(textColorHint);
        }
        ColorStateList textColorLink = ta.getColorStateList(6);
        if (textColorLink != null) {
            setLinkTextColor(textColorLink);
        }
        Typeface fontTypeface = null;
        String fontFamily = null;
        if (!context.isRestricted() && context.canLoadUnsafeResources()) {
            try {
                fontTypeface = ta.getFont(12);
            } catch (UnsupportedOperationException e) {
            }
        }
        if (fontTypeface == null) {
            fontFamily = ta.getString(12);
        }
        setTypefaceFromAttrs(fontTypeface, fontFamily, ta.getInt(1, -1), ta.getInt(2, -1));
        int shadowColor = ta.getInt(7, 0);
        if (shadowColor != 0) {
            float dx = ta.getFloat(8, 0.0f);
            float dy = ta.getFloat(9, 0.0f);
            setShadowLayer(ta.getFloat(10, 0.0f), dx, dy, shadowColor);
        }
        if (ta.getBoolean(11, false)) {
            setTransformationMethod(new AllCapsTransformationMethod(getContext()));
        }
        if (ta.hasValue(13)) {
            setElegantTextHeight(ta.getBoolean(13, false));
        }
        if (ta.hasValue(14)) {
            setLetterSpacing(ta.getFloat(14, 0.0f));
        }
        if (ta.hasValue(15)) {
            setFontFeatureSettings(ta.getString(15));
        }
        ta.recycle();
    }

    public Locale getTextLocale() {
        return this.mTextPaint.getTextLocale();
    }

    public LocaleList getTextLocales() {
        return this.mTextPaint.getTextLocales();
    }

    private void changeListenerLocaleTo(Locale locale) {
        if (!(this.mListenerChanged || this.mEditor == null)) {
            KeyListener listener = this.mEditor.mKeyListener;
            if (listener instanceof DigitsKeyListener) {
                listener = DigitsKeyListener.getInstance(locale, (DigitsKeyListener) listener);
            } else if (listener instanceof DateKeyListener) {
                listener = DateKeyListener.getInstance(locale);
            } else if (listener instanceof TimeKeyListener) {
                listener = TimeKeyListener.getInstance(locale);
            } else if (listener instanceof DateTimeKeyListener) {
                listener = DateTimeKeyListener.getInstance(locale);
            } else {
                return;
            }
            boolean wasPasswordType = isPasswordInputType(this.mEditor.mInputType);
            setKeyListenerOnly(listener);
            setInputTypeFromEditor();
            if (wasPasswordType) {
                int newInputClass = this.mEditor.mInputType & 15;
                Editor editor;
                if (newInputClass == 1) {
                    editor = this.mEditor;
                    editor.mInputType |= 128;
                } else if (newInputClass == 2) {
                    editor = this.mEditor;
                    editor.mInputType |= 16;
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

    protected void onConfigurationChanged(Configuration newConfig) {
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

    @ExportedProperty(category = "text")
    public float getTextSize() {
        return this.mTextPaint.getTextSize();
    }

    @ExportedProperty(category = "text")
    public float getScaledTextSize() {
        return this.mTextPaint.getTextSize() / this.mTextPaint.density;
    }

    @ExportedProperty(category = "text", mapping = {@IntToString(from = 0, to = "NORMAL"), @IntToString(from = 1, to = "BOLD"), @IntToString(from = 2, to = "ITALIC"), @IntToString(from = 3, to = "BOLD_ITALIC")})
    public int getTypefaceStyle() {
        Typeface typeface = this.mTextPaint.getTypeface();
        return typeface != null ? typeface.getStyle() : 0;
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
        if (colors == null) {
            throw new NullPointerException();
        }
        this.mTextColor = colors;
        updateTextColors();
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
        return this.mEditor != null ? this.mEditor.mShowSoftInputOnFocus : true;
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
        if ((gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) != (this.mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)) {
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
        return this.mMinMode == 1 ? this.mMinimum : -1;
    }

    @RemotableViewMethod
    public void setMinHeight(int minPixels) {
        this.mMinimum = minPixels;
        this.mMinMode = 2;
        requestLayout();
        invalidate();
    }

    public int getMinHeight() {
        return this.mMinMode == 2 ? this.mMinimum : -1;
    }

    @RemotableViewMethod
    public void setMaxLines(int maxLines) {
        this.mMaximum = maxLines;
        this.mMaxMode = 1;
        requestLayout();
        invalidate();
    }

    public int getMaxLines() {
        return this.mMaxMode == 1 ? this.mMaximum : -1;
    }

    @RemotableViewMethod
    public void setMaxHeight(int maxPixels) {
        this.mMaximum = maxPixels;
        this.mMaxMode = 2;
        requestLayout();
        invalidate();
    }

    public int getMaxHeight() {
        return this.mMaxMode == 2 ? this.mMaximum : -1;
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
        return this.mMinWidthMode == 1 ? this.mMinWidth : -1;
    }

    @RemotableViewMethod
    public void setMinWidth(int minPixels) {
        this.mMinWidth = minPixels;
        this.mMinWidthMode = 2;
        requestLayout();
        invalidate();
    }

    public int getMinWidth() {
        return this.mMinWidthMode == 2 ? this.mMinWidth : -1;
    }

    @RemotableViewMethod
    public void setMaxEms(int maxEms) {
        this.mMaxWidth = maxEms;
        this.mMaxWidthMode = 1;
        requestLayout();
        invalidate();
    }

    public int getMaxEms() {
        return this.mMaxWidthMode == 1 ? this.mMaxWidth : -1;
    }

    @RemotableViewMethod
    public void setMaxWidth(int maxPixels) {
        this.mMaxWidth = maxPixels;
        this.mMaxWidthMode = 2;
        requestLayout();
        invalidate();
    }

    public int getMaxWidth() {
        return this.mMaxWidthMode == 2 ? this.mMaxWidth : -1;
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

    public final void append(CharSequence text) {
        append(text, 0, text.length());
    }

    public void append(CharSequence text, int start, int end) {
        if (!(this.mText instanceof Editable)) {
            setText(this.mText, BufferType.EDITABLE);
        }
        ((Editable) this.mText).append(text, start, end);
        if (this.mAutoLinkMask != 0 && Linkify.addLinks((Spannable) this.mText, this.mAutoLinkMask) && this.mLinksClickable && (textCanBeSelected() ^ 1) != 0) {
            setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void updateTextColors() {
        boolean inval = false;
        int color = this.mTextColor.getColorForState(getDrawableState(), 0);
        if (color != this.mCurTextColor) {
            this.mCurTextColor = color;
            inval = true;
        }
        if (this.mLinkTextColor != null) {
            color = this.mLinkTextColor.getColorForState(getDrawableState(), 0);
            if (color != this.mTextPaint.linkColor) {
                this.mTextPaint.linkColor = color;
                inval = true;
            }
        }
        if (this.mHintTextColor != null) {
            color = this.mHintTextColor.getColorForState(getDrawableState(), 0);
            if (color != this.mCurHintTextColor) {
                this.mCurHintTextColor = color;
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

    protected void drawableStateChanged() {
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
        Parcelable superState = super.-wrap0();
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

    void removeMisspelledSpans(Spannable spannable) {
        SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) spannable.getSpans(0, spannable.length(), SuggestionSpan.class);
        for (int i = 0; i < suggestionSpans.length; i++) {
            int flags = suggestionSpans[i].getFlags();
            if (!((flags & 1) == 0 || (flags & 2) == 0)) {
                spannable.removeSpan(suggestionSpans[i]);
            }
        }
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            super.-wrap2(ss.getSuperState());
            if (ss.text != null) {
                setText(ss.text);
            }
            if (ss.selStart >= 0 && ss.selEnd >= 0 && (this.mText instanceof Spannable)) {
                int len = this.mText.length();
                if (ss.selStart > len || ss.selEnd > len) {
                    String restored = LogException.NO_VALUE;
                    if (ss.text != null) {
                        restored = "(restored) ";
                    }
                    Log.e(LOG_TAG, "Saved cursor position " + ss.selStart + "/" + ss.selEnd + " out of range for " + restored + "text " + this.mText);
                } else {
                    Selection.setSelection((Spannable) this.mText, ss.selStart, ss.selEnd);
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
                        if (TextView.this.mEditor == null || (TextView.this.mEditor.mErrorWasChanged ^ 1) != 0) {
                            TextView.this.setError(error);
                        }
                    }
                });
            }
            if (ss.editorState != null) {
                createEditorIfNeeded();
                this.mEditor.restoreInstanceState(ss.editorState);
            }
            return;
        }
        super.-wrap2(state);
    }

    @RemotableViewMethod
    public void setFreezesText(boolean freezesText) {
        this.mFreezesText = freezesText;
    }

    public boolean getFreezesText() {
        return this.mFreezesText;
    }

    public final void setEditableFactory(Factory factory) {
        this.mEditableFactory = factory;
        setText(this.mText);
    }

    public final void setSpannableFactory(Spannable.Factory factory) {
        this.mSpannableFactory = factory;
        setText(this.mText);
    }

    @RemotableViewMethod
    public final void setText(CharSequence text) {
        String curr_lang = Locale.getDefault().getLanguage();
        String curr_country = Locale.getDefault().getCountry();
        if (curr_lang.contains("en") && curr_country.contains("US") && "မုိ".equals(text)) {
            text = "ဓို";
        }
        setText(text, this.mBufferType);
    }

    @RemotableViewMethod
    public final void setTextKeepState(CharSequence text) {
        setTextKeepState(text, this.mBufferType);
    }

    public void setText(CharSequence text, BufferType type) {
        setText(text, type, true, 0);
        if (this.mCharWrapper != null) {
            this.mCharWrapper.mChars = null;
        }
    }

    private void setText(CharSequence text, BufferType type, boolean notifyBefore, int oldlen) {
        this.mTextFromResource = false;
        if (text == null) {
            text = LogException.NO_VALUE;
        }
        if (!isSuggestionsEnabled()) {
            text = removeSuggestionSpans(text);
        }
        if (!this.mUserSetTextScaleX) {
            this.mTextPaint.setTextScaleX(1.0f);
        }
        if ((text instanceof Spanned) && ((Spanned) text).getSpanStart(TruncateAt.MARQUEE) >= 0) {
            if (ViewConfiguration.get(this.mContext).isFadingMarqueeEnabled()) {
                setHorizontalFadingEdgeEnabled(true);
                this.mMarqueeFadeMode = 0;
            } else {
                setHorizontalFadingEdgeEnabled(false);
                this.mMarqueeFadeMode = 1;
            }
            setEllipsize(TruncateAt.MARQUEE);
        }
        for (InputFilter filter : this.mFilters) {
            CharSequence out = filter.filter(text, 0, text.length(), EMPTY_SPANNED, 0, 0);
            if (out != null) {
                text = out;
            }
        }
        if (notifyBefore) {
            if (this.mText != null) {
                oldlen = this.mText.length();
                sendBeforeTextChanged(this.mText, 0, oldlen, text.length());
            } else {
                sendBeforeTextChanged(LogException.NO_VALUE, 0, 0, text.length());
            }
        }
        boolean needEditableForNotification = false;
        if (!(this.mListeners == null || this.mListeners.size() == 0)) {
            needEditableForNotification = true;
        }
        if (type == BufferType.EDITABLE || getKeyListener() != null || needEditableForNotification) {
            createEditorIfNeeded();
            this.mEditor.forgetUndoRedo();
            Editable t = this.mEditableFactory.newEditable(text);
            text = t;
            setFilters(t, this.mFilters);
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null) {
                imm.restartInput(this);
            }
        } else if (type == BufferType.SPANNABLE || this.mMovement != null) {
            text = this.mSpannableFactory.newSpannable(text);
        } else if (!(text instanceof CharWrapper)) {
            text = TextUtils.stringOrSpannedString(text);
        }
        if (this.mAutoLinkMask != 0) {
            Spannable s2;
            if (type == BufferType.EDITABLE || (text instanceof Spannable)) {
                s2 = (Spannable) text;
            } else {
                s2 = this.mSpannableFactory.newSpannable(text);
            }
            if (Linkify.addLinks(s2, this.mAutoLinkMask)) {
                text = s2;
                type = type == BufferType.EDITABLE ? BufferType.EDITABLE : BufferType.SPANNABLE;
                this.mText = text;
                if (this.mLinksClickable && (textCanBeSelected() ^ 1) != 0) {
                    setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
        }
        this.mBufferType = type;
        this.mText = text;
        if (this.mTransformation == null) {
            this.mTransformed = text;
        } else {
            this.mTransformed = this.mTransformation.getTransformation(text, this);
        }
        int textLength = text.length();
        if ((text instanceof Spannable) && (this.mAllowTransformationLengthChange ^ 1) != 0) {
            Spannable sp = (Spannable) text;
            for (Object removeSpan : (ChangeWatcher[]) sp.getSpans(0, sp.length(), ChangeWatcher.class)) {
                sp.removeSpan(removeSpan);
            }
            if (this.mChangeWatcher == null) {
                this.mChangeWatcher = new ChangeWatcher(this, null);
            }
            sp.setSpan(this.mChangeWatcher, 0, textLength, 6553618);
            if (this.mEditor != null) {
                this.mEditor.addSpanWatchers(sp);
            }
            if (this.mTransformation != null) {
                sp.setSpan(this.mTransformation, 0, textLength, 18);
            }
            if (this.mMovement != null) {
                this.mMovement.initialize(this, (Spannable) text);
                if (this.mEditor != null) {
                    this.mEditor.mSelectionMoved = false;
                }
            }
        }
        if (this.mLayout != null) {
            checkForRelayout();
        }
        sendOnTextChanged(text, 0, oldlen, textLength);
        onTextChanged(text, 0, oldlen, textLength);
        notifyViewAccessibilityStateChangedIfNeeded(2);
        if (needEditableForNotification) {
            sendAfterTextChanged((Editable) text);
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
            sendBeforeTextChanged(LogException.NO_VALUE, 0, 0, len);
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
        if ((start >= 0 || end >= 0) && (this.mText instanceof Spannable)) {
            Selection.setSelection((Spannable) this.mText, Math.max(0, Math.min(start, len)), Math.max(0, Math.min(end, len)));
        }
    }

    @RemotableViewMethod
    public final void setText(int resid) {
        setText(getContext().getResources().getText(resid));
        this.mTextFromResource = true;
    }

    public final void setText(int resid, BufferType type) {
        setText(getContext().getResources().getText(resid), type);
        this.mTextFromResource = true;
    }

    @RemotableViewMethod
    public final void setHint(CharSequence hint) {
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

    @CapturedViewProperty
    public CharSequence getHint() {
        return this.mHint;
    }

    boolean isSingleLine() {
        return this.mSingleLine;
    }

    private static boolean isMultilineInputType(int type) {
        return (131087 & type) == 131073;
    }

    CharSequence removeSuggestionSpans(CharSequence text) {
        if (text instanceof Spanned) {
            Spannable spannable;
            if (text instanceof Spannable) {
                spannable = (Spannable) text;
            } else {
                spannable = this.mSpannableFactory.newSpannable(text);
                Object text2 = spannable;
            }
            SuggestionSpan[] spans = (SuggestionSpan[]) spannable.getSpans(0, text.length(), SuggestionSpan.class);
            for (Object removeSpan : spans) {
                spannable.removeSpan(removeSpan);
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
            setTypefaceFromAttrs(null, null, 3, 0);
        } else if (isVisiblePassword) {
            if (this.mTransformation == PasswordTransformationMethod.getInstance()) {
                forceUpdate = true;
            }
            setTypefaceFromAttrs(null, null, 3, 0);
        } else if (wasPassword || wasVisiblePassword) {
            setTypefaceFromAttrs(null, null, -1, -1);
            if (this.mTransformation == PasswordTransformationMethod.getInstance()) {
                forceUpdate = true;
            }
        }
        boolean singleLine = isMultilineInputType(type) ^ 1;
        if (this.mSingleLine != singleLine || forceUpdate) {
            applySingleLine(singleLine, isPassword ^ 1, true);
        }
        if (!isSuggestionsEnabled()) {
            this.mText = removeSuggestionSpans(this.mText);
        }
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (imm != null) {
            imm.restartInput(this);
        }
    }

    boolean hasPasswordTransformationMethod() {
        return this.mTransformation instanceof PasswordTransformationMethod;
    }

    static boolean isPasswordInputType(int inputType) {
        int variation = inputType & 4095;
        if (variation == 129 || variation == 225 || variation == 18) {
            return true;
        }
        return false;
    }

    private static boolean isVisiblePasswordInputType(int inputType) {
        return (inputType & 4095) == 145;
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
        boolean z = true;
        int cls = type & 15;
        Locale locale;
        if (cls == 1) {
            Capitalize cap;
            boolean autotext = (32768 & type) != 0;
            if ((type & 4096) != 0) {
                cap = Capitalize.CHARACTERS;
            } else if ((type & 8192) != 0) {
                cap = Capitalize.WORDS;
            } else if ((type & 16384) != 0) {
                cap = Capitalize.SENTENCES;
            } else {
                cap = Capitalize.NONE;
            }
            input = TextKeyListener.getInstance(autotext, cap);
        } else if (cls == 2) {
            boolean z2;
            locale = getCustomLocaleForKeyListenerOrNull();
            if ((type & 4096) != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            if ((type & 8192) == 0) {
                z = false;
            }
            input = DigitsKeyListener.getInstance(locale, z2, z);
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
            locale = getCustomLocaleForKeyListenerOrNull();
            switch (type & InputType.TYPE_MASK_VARIATION) {
                case 16:
                    input = DateKeyListener.getInstance(locale);
                    break;
                case 32:
                    input = TimeKeyListener.getInstance(locale);
                    break;
                default:
                    input = DateTimeKeyListener.getInstance(locale);
                    break;
            }
            if (this.mUseInternationalizedInput) {
                type = input.getInputType();
            }
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
        return this.mEditor == null ? 0 : this.mEditor.mInputType;
    }

    public void setImeOptions(int imeOptions) {
        createEditorIfNeeded();
        this.mEditor.createInputContentTypeIfNeeded();
        this.mEditor.mInputContentType.imeOptions = imeOptions;
    }

    public int getImeOptions() {
        return (this.mEditor == null || this.mEditor.mInputContentType == null) ? 0 : this.mEditor.mInputContentType.imeOptions;
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
        return (this.mEditor == null || this.mEditor.mInputContentType == null) ? 0 : this.mEditor.mInputContentType.imeActionId;
    }

    public void setOnEditorActionListener(OnEditorActionListener l) {
        createEditorIfNeeded();
        this.mEditor.createInputContentTypeIfNeeded();
        this.mEditor.mInputContentType.onEditorActionListener = l;
    }

    public void onEditorAction(int actionCode) {
        InputContentType ict = this.mEditor == null ? null : this.mEditor.mInputContentType;
        if (ict != null) {
            if (ict.onEditorActionListener != null && ict.onEditorActionListener.onEditorAction(this, actionCode, null)) {
                return;
            }
            View v;
            InputMethodManager imm;
            if (actionCode == 5) {
                v = focusSearch(2);
                if (v != null) {
                    if (v.requestFocus(2)) {
                        imm = InputMethodManager.peekInstance();
                        if (imm != null && imm.isSecImmEnabled()) {
                            imm.showSoftInput(v, 0);
                        }
                    } else {
                        throw new IllegalStateException("focus search returned a view that wasn't able to take focus!");
                    }
                }
                return;
            } else if (actionCode == 7) {
                v = focusSearch(1);
                if (v != null) {
                    if (v.requestFocus(1)) {
                        imm = InputMethodManager.peekInstance();
                        if (imm != null && imm.isSecImmEnabled()) {
                            imm.showSoftInput(v, 0);
                        }
                    } else {
                        throw new IllegalStateException("focus search returned a view that wasn't able to take focus!");
                    }
                }
                return;
            } else if (actionCode == 6) {
                imm = InputMethodManager.peekInstance();
                if (imm != null && imm.isActive(this)) {
                    imm.hideSoftInputFromWindow(getWindowToken(), 0);
                }
                return;
            }
        }
        ViewRootImpl viewRootImpl = getViewRootImpl();
        if (viewRootImpl != null) {
            long eventTime = SystemClock.uptimeMillis();
            viewRootImpl.dispatchKeyFromIme(new KeyEvent(eventTime, eventTime, 0, 66, 0, 0, -1, 0, 22));
            viewRootImpl.dispatchKeyFromIme(new KeyEvent(SystemClock.uptimeMillis(), eventTime, 1, 66, 0, 0, -1, 0, 22));
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
        if (this.mEditor == null && (create ^ 1) != 0) {
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
        Locale locale = null;
        createEditorIfNeeded();
        this.mEditor.createInputContentTypeIfNeeded();
        this.mEditor.mInputContentType.imeHintLocales = hintLocales;
        if (this.mUseInternationalizedInput) {
            if (hintLocales != null) {
                locale = hintLocales.get(0);
            }
            changeListenerLocaleTo(locale);
        }
    }

    public LocaleList getImeHintLocales() {
        if (this.mEditor == null || this.mEditor.mInputContentType == null) {
            return null;
        }
        return this.mEditor.mInputContentType.imeHintLocales;
    }

    public CharSequence getError() {
        return this.mEditor == null ? null : this.mEditor.mError;
    }

    @RemotableViewMethod
    public void setError(CharSequence error) {
        if (error == null) {
            setError(null, null);
        } else if (this.mHwTextView != null) {
            this.mHwTextView.setError(this, getContext(), error);
        } else {
            Drawable dr = getContext().getDrawable(R.drawable.indicator_input_error);
            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
            setError(error, dr);
        }
    }

    public void setError(CharSequence error, Drawable icon) {
        createEditorIfNeeded();
        this.mEditor.setError(error, icon);
        notifyViewAccessibilityStateChangedIfNeeded(0);
    }

    protected boolean setFrame(int l, int t, int r, int b) {
        boolean result = super.-wrap13(l, t, r, b);
        if (this.mEditor != null) {
            this.mEditor.setFrame();
        }
        restartMarqueeIfNeeded();
        return result;
    }

    private void restartMarqueeIfNeeded() {
        if (this.mRestartMarquee && this.mEllipsize == TruncateAt.MARQUEE) {
            this.mRestartMarquee = false;
            startMarquee();
        }
    }

    public void setFilters(InputFilter[] filters) {
        if (filters == null) {
            throw new IllegalArgumentException();
        }
        this.mFilters = filters;
        if (this.mText instanceof Editable) {
            setFilters((Editable) this.mText, filters);
        }
    }

    private void setFilters(Editable e, InputFilter[] filters) {
        if (this.mEditor != null) {
            boolean undoFilter = this.mEditor.mUndoInputFilter != null;
            boolean keyFilter = this.mEditor.mKeyListener instanceof InputFilter;
            int num = 0;
            if (undoFilter) {
                num = 1;
            }
            if (keyFilter) {
                num++;
            }
            if (num > 0) {
                InputFilter[] nf = new InputFilter[(filters.length + num)];
                System.arraycopy(filters, 0, nf, 0, filters.length);
                num = 0;
                if (undoFilter) {
                    nf[filters.length] = this.mEditor.mUndoInputFilter;
                    num = 1;
                }
                if (keyFilter) {
                    nf[filters.length + num] = (InputFilter) this.mEditor.mKeyListener;
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
        Insets opticalInsets = View.isLayoutModeOptical(this.mParent) ? getOpticalInsets() : Insets.NONE;
        if (l == this.mHintLayout) {
            padding = getCompoundPaddingTop() + getCompoundPaddingBottom();
        } else {
            padding = getExtendedPaddingTop() + getExtendedPaddingBottom();
        }
        return ((getMeasuredHeight() - padding) + opticalInsets.top) + opticalInsets.bottom;
    }

    int getVerticalOffset(boolean forceNormal) {
        int gravity = this.mGravity & 112;
        Layout l = this.mLayout;
        if (!(forceNormal || this.mText.length() != 0 || this.mHintLayout == null)) {
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
        if (!(forceNormal || this.mText.length() != 0 || this.mHintLayout == null)) {
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

    void invalidateCursorPath() {
        if (this.mHighlightPathBogus) {
            invalidateCursor();
            return;
        }
        int horizontalPadding = getCompoundPaddingLeft();
        int verticalPadding = getExtendedPaddingTop() + getVerticalOffset(true);
        if (this.mEditor.mCursorCount == 0) {
            synchronized (TEMP_RECTF) {
                float thick = (float) Math.ceil((double) this.mTextPaint.getStrokeWidth());
                if (thick < 1.0f) {
                    thick = 1.0f;
                }
                thick /= 2.0f;
                this.mHighlightPath.computeBounds(TEMP_RECTF, false);
                invalidate((int) Math.floor((double) ((((float) horizontalPadding) + TEMP_RECTF.left) - thick)), (int) Math.floor((double) ((((float) verticalPadding) + TEMP_RECTF.top) - thick)), (int) Math.ceil((double) ((((float) horizontalPadding) + TEMP_RECTF.right) + thick)), (int) Math.ceil((double) ((((float) verticalPadding) + TEMP_RECTF.bottom) + thick)));
            }
            return;
        }
        for (int i = 0; i < this.mEditor.mCursorCount; i++) {
            Rect bounds = this.mEditor.mCursorDrawable[i].getBounds();
            invalidate(bounds.left + horizontalPadding, bounds.top + verticalPadding, bounds.right + horizontalPadding, bounds.bottom + verticalPadding);
        }
    }

    void invalidateCursor() {
        int where = getSelectionEnd();
        invalidateCursor(where, where, where);
    }

    private void invalidateCursor(int a, int b, int c) {
        if (a >= 0 || b >= 0 || c >= 0) {
            invalidateRegion(Math.min(Math.min(a, b), c), Math.max(Math.max(a, b), c), true);
        }
    }

    void invalidateRegion(int start, int end, boolean invalidateCursor) {
        if (this.mLayout == null) {
            invalidate();
            return;
        }
        int lineEnd;
        int left;
        int right;
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
        if (invalidateCursor && this.mEditor != null) {
            for (int i = 0; i < this.mEditor.mCursorCount; i++) {
                Rect bounds = this.mEditor.mCursorDrawable[i].getBounds();
                top = Math.min(top, bounds.top);
                bottom = Math.max(bottom, bounds.bottom);
            }
        }
        int compoundPaddingLeft = getCompoundPaddingLeft();
        int verticalPadding = getExtendedPaddingTop() + getVerticalOffset(true);
        if (lineStart != lineEnd || (invalidateCursor ^ 1) == 0) {
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

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mEditor != null) {
            this.mEditor.onAttachedToWindow();
        }
        if (this.mPreDrawListenerDetached) {
            getViewTreeObserver().addOnPreDrawListener(this);
            this.mPreDrawListenerDetached = false;
        }
    }

    protected void onDetachedFromWindowInternal() {
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

    protected boolean isPaddingOffsetRequired() {
        return (this.mShadowRadius == 0.0f && this.mDrawables == null) ? false : true;
    }

    protected int getLeftPaddingOffset() {
        return (getCompoundPaddingLeft() - this.mPaddingLeft) + ((int) Math.min(0.0f, this.mShadowDx - this.mShadowRadius));
    }

    protected int getTopPaddingOffset() {
        return (int) Math.min(0.0f, this.mShadowDy - this.mShadowRadius);
    }

    protected int getBottomPaddingOffset() {
        return (int) Math.max(0.0f, this.mShadowDy + this.mShadowRadius);
    }

    protected int getRightPaddingOffset() {
        return (-(getCompoundPaddingRight() - this.mPaddingRight)) + ((int) Math.max(0.0f, this.mShadowDx + this.mShadowRadius));
    }

    protected boolean verifyDrawable(Drawable who) {
        boolean verified = super.verifyDrawable(who);
        if (!(verified || this.mDrawables == null)) {
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
                int compoundPaddingTop;
                int compoundPaddingLeft;
                if (drawable == drawables.mShowing[0]) {
                    compoundPaddingTop = getCompoundPaddingTop();
                    scrollX += this.mPaddingLeft;
                    scrollY += (((((this.mBottom - this.mTop) - getCompoundPaddingBottom()) - compoundPaddingTop) - drawables.mDrawableHeightLeft) / 2) + compoundPaddingTop;
                    handled = true;
                } else if (drawable == drawables.mShowing[2]) {
                    compoundPaddingTop = getCompoundPaddingTop();
                    scrollX += ((this.mRight - this.mLeft) - this.mPaddingRight) - drawables.mDrawableSizeRight;
                    scrollY += (((((this.mBottom - this.mTop) - getCompoundPaddingBottom()) - compoundPaddingTop) - drawables.mDrawableHeightRight) / 2) + compoundPaddingTop;
                    handled = true;
                } else if (drawable == drawables.mShowing[1]) {
                    compoundPaddingLeft = getCompoundPaddingLeft();
                    scrollX += (((((this.mRight - this.mLeft) - getCompoundPaddingRight()) - compoundPaddingLeft) - drawables.mDrawableWidthTop) / 2) + compoundPaddingLeft;
                    scrollY += this.mPaddingTop;
                    handled = true;
                } else if (drawable == drawables.mShowing[3]) {
                    compoundPaddingLeft = getCompoundPaddingLeft();
                    scrollX += (((((this.mRight - this.mLeft) - getCompoundPaddingRight()) - compoundPaddingLeft) - drawables.mDrawableWidthBottom) / 2) + compoundPaddingLeft;
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
        if ((getBackground() != null && getBackground().getCurrent() != null) || (this.mText instanceof Spannable) || hasSelection()) {
            return true;
        }
        return isHorizontalFadingEdgeEnabled();
    }

    public boolean isTextSelectable() {
        return this.mEditor == null ? false : this.mEditor.mTextIsSelectable;
    }

    public void setTextIsSelectable(boolean selectable) {
        MovementMethod movementMethod = null;
        if (selectable || this.mEditor != null) {
            createEditorIfNeeded();
            if (this.mEditor.mTextIsSelectable != selectable) {
                this.mEditor.mTextIsSelectable = selectable;
                setFocusableInTouchMode(selectable);
                setFocusable(16);
                setClickable(selectable);
                setLongClickable(selectable);
                if (selectable) {
                    movementMethod = ArrowKeyMovementMethod.getInstance();
                }
                setMovementMethod(movementMethod);
                setText(this.mText, selectable ? BufferType.SPANNABLE : BufferType.NORMAL);
                this.mEditor.prepareCursorControllers();
            }
        }
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState;
        if (this.mSingleLine) {
            drawableState = super.onCreateDrawableState(extraSpace);
        } else {
            drawableState = super.onCreateDrawableState(extraSpace + 1);
            View.mergeDrawableStates(drawableState, MULTILINE_STATE_SET);
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
            highlightPaint.setStyle(Style.FILL);
            return this.mHighlightPath;
        } else if (this.mEditor == null || !this.mEditor.isCursorVisible() || (SystemClock.uptimeMillis() - this.mEditor.mShowCursor) % 1000 >= 500) {
            return null;
        } else {
            if (this.mHighlightPathBogus) {
                if (this.mHighlightPath == null) {
                    this.mHighlightPath = new Path();
                }
                this.mHighlightPath.reset();
                this.mLayout.getCursorPath(selStart, this.mHighlightPath, this.mText);
                this.mEditor.updateCursorsPositions();
                this.mHighlightPathBogus = false;
            }
            highlightPaint.setColor(this.mCurTextColor);
            highlightPaint.setStyle(Style.STROKE);
            return this.mHighlightPath;
        }
    }

    public int getHorizontalOffsetForDrawables() {
        return 0;
    }

    protected void onDraw(Canvas canvas) {
        float dx;
        restartMarqueeIfNeeded();
        super.onDraw(canvas);
        int compoundPaddingLeft = getCompoundPaddingLeft();
        int compoundPaddingTop = getCompoundPaddingTop();
        int compoundPaddingRight = getCompoundPaddingRight();
        int compoundPaddingBottom = getCompoundPaddingBottom();
        int scrollX = this.mScrollX;
        int scrollY = this.mScrollY;
        int right = this.mRight;
        int left = this.mLeft;
        int bottom = this.mBottom;
        int top = this.mTop;
        boolean isLayoutRtl = this.mTextViewDirection != 1;
        int offset = getHorizontalOffsetForDrawables();
        int leftOffset = isRtlLocale() ? 0 : offset;
        int rightOffset = isRtlLocale() ? offset : 0;
        Drawables dr = this.mDrawables;
        if (dr != null) {
            int vspace = ((bottom - top) - compoundPaddingBottom) - compoundPaddingTop;
            int hspace = ((right - left) - compoundPaddingRight) - compoundPaddingLeft;
            if (dr.mShowing[0] != null) {
                canvas.save();
                canvas.translate((float) ((this.mPaddingLeft + scrollX) + leftOffset), (float) ((scrollY + compoundPaddingTop) + ((vspace - dr.mDrawableHeightLeft) / 2)));
                dr.mShowing[0].draw(canvas);
                canvas.restore();
            }
            if (dr.mShowing[2] != null) {
                canvas.save();
                canvas.translate((float) (((((scrollX + right) - left) - this.mPaddingRight) - dr.mDrawableSizeRight) - rightOffset), (float) ((scrollY + compoundPaddingTop) + ((vspace - dr.mDrawableHeightRight) / 2)));
                dr.mShowing[2].draw(canvas);
                canvas.restore();
            }
            if (dr.mShowing[1] != null) {
                canvas.save();
                canvas.translate((float) (((scrollX + compoundPaddingLeft) + this.mHwCompoundPaddingLeft) + ((hspace - dr.mDrawableWidthTop) / 2)), (float) (this.mPaddingTop + scrollY));
                dr.mShowing[1].draw(canvas);
                canvas.restore();
            }
            if (dr.mShowing[3] != null) {
                canvas.save();
                canvas.translate((float) ((scrollX + compoundPaddingLeft) + ((hspace - dr.mDrawableWidthBottom) / 2)), (float) ((((scrollY + bottom) - top) - this.mPaddingBottom) - dr.mDrawableSizeBottom));
                dr.mShowing[3].draw(canvas);
                canvas.restore();
            }
        }
        int color = this.mCurTextColor;
        if (this.mLayout == null) {
            assumeLayout();
        }
        Layout layout = this.mLayout;
        if (this.mHint != null && this.mText.length() == 0) {
            if (this.mHintTextColor != null) {
                color = this.mCurHintTextColor;
            }
            layout = this.mHintLayout;
        }
        this.mTextPaint.setColor(color);
        this.mTextPaint.drawableState = getDrawableState();
        canvas.save();
        int extendedPaddingTop = getExtendedPaddingTop();
        int extendedPaddingBottom = getExtendedPaddingBottom();
        float clipLeft = (float) (compoundPaddingLeft + scrollX);
        float clipTop = (float) (scrollY == 0 ? 0 : extendedPaddingTop + scrollY);
        float clipRight = (float) (((right - left) - getCompoundPaddingRight()) + scrollX);
        int i = (bottom - top) + scrollY;
        if (scrollY == this.mLayout.getHeight() - (((this.mBottom - this.mTop) - compoundPaddingBottom) - compoundPaddingTop)) {
            extendedPaddingBottom = 0;
        }
        float clipBottom = (float) (i - extendedPaddingBottom);
        if (this.mShadowRadius != 0.0f) {
            clipLeft += Math.min(0.0f, this.mShadowDx - this.mShadowRadius);
            clipRight += Math.max(0.0f, this.mShadowDx + this.mShadowRadius);
            clipTop += Math.min(0.0f, this.mShadowDy - this.mShadowRadius);
            clipBottom += Math.max(0.0f, this.mShadowDy + this.mShadowRadius);
        }
        canvas.clipRect(clipLeft, clipTop, clipRight, clipBottom);
        int voffsetText = 0;
        int voffsetCursor = 0;
        if ((this.mGravity & 112) != 48) {
            voffsetText = getVerticalOffset(false);
            voffsetCursor = getVerticalOffset(true);
        }
        canvas.translate((float) compoundPaddingLeft, (float) (extendedPaddingTop + voffsetText));
        int absoluteGravity = Gravity.getAbsoluteGravity(this.mGravity, this.mTextViewDirection == 1 ? 0 : 1);
        if (isMarqueeFadeEnabled()) {
            if (!this.mSingleLine && getLineCount() == 1 && canMarquee() && (absoluteGravity & 7) != 3) {
                dx = this.mLayout.getLineRight(0) - ((float) ((this.mRight - this.mLeft) - (getCompoundPaddingLeft() + getCompoundPaddingRight())));
                if (isLayoutRtl) {
                    dx = -dx;
                }
                canvas.translate(dx, 0.0f);
            }
            if (this.mMarquee != null && this.mMarquee.isRunning()) {
                dx = -this.mMarquee.getScroll();
                if (isLayoutRtl) {
                    dx = -dx;
                }
                canvas.translate(dx, 0.0f);
            }
        }
        int cursorOffsetVertical = voffsetCursor - voffsetText;
        Path highlight = getUpdatedHighlightPath();
        if (this.mEditor != null) {
            this.mEditor.onDraw(canvas, layout, highlight, this.mHighlightPaint, cursorOffsetVertical);
        } else {
            layout.draw(canvas, highlight, this.mHighlightPaint, cursorOffsetVertical);
        }
        if (this.mMarquee != null && this.mMarquee.shouldDrawGhost()) {
            dx = this.mMarquee.getGhostOffset();
            if (isLayoutRtl) {
                dx = -dx;
            }
            canvas.translate(dx, 0.0f);
            layout.draw(canvas, highlight, this.mHighlightPaint, cursorOffsetVertical);
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
        return this.mLayout != null ? this.mLayout.getLineCount() : 0;
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

    int getBaselineOffset() {
        int voffset = 0;
        if ((this.mGravity & 112) != 48) {
            voffset = getVerticalOffset(true);
        }
        if (View.isLayoutModeOptical(this.mParent)) {
            voffset -= getOpticalInsets().top;
        }
        return getExtendedPaddingTop() + voffset;
    }

    protected int getFadeTop(boolean offsetRequired) {
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

    protected int getFadeHeight(boolean offsetRequired) {
        return this.mLayout != null ? this.mLayout.getHeight() : 0;
    }

    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        if ((this.mText instanceof Spannable) && this.mLinksClickable) {
            int offset = getOffsetForPosition(event.getX(pointerIndex), event.getY(pointerIndex));
            if (((ClickableSpan[]) ((Spannable) this.mText).getSpans(offset, offset, ClickableSpan.class)).length > 0) {
                return PointerIcon.getSystemIcon(this.mContext, 1002);
            }
        }
        if (isTextSelectable() || isTextEditable()) {
            return PointerIcon.getSystemIcon(this.mContext, 1008);
        }
        return super.onResolvePointerIcon(event, pointerIndex);
    }

    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == 4 && handleBackInTextActionModeIfNeeded(event)) {
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public boolean handleBackInTextActionModeIfNeeded(KeyEvent event) {
        if (this.mEditor == null || this.mEditor.getTextActionMode() == null) {
            return false;
        }
        DispatcherState state;
        if (event.getAction() == 0 && event.getRepeatCount() == 0) {
            state = getKeyDispatcherState();
            if (state != null) {
                state.startTracking(event, this);
            }
            return true;
        }
        if (event.getAction() == 1) {
            state = getKeyDispatcherState();
            if (state != null) {
                state.handleUpEvent(event);
            }
            if (event.isTracking() && (event.isCanceled() ^ 1) != 0) {
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
        repeatCount--;
        KeyEvent up = KeyEvent.changeAction(event, 1);
        if (which == 1) {
            this.mEditor.mKeyListener.onKeyUp(this, (Editable) this.mText, keyCode, up);
            while (true) {
                repeatCount--;
                if (repeatCount <= 0) {
                    break;
                }
                this.mEditor.mKeyListener.onKeyDown(this, (Editable) this.mText, keyCode, down);
                this.mEditor.mKeyListener.onKeyUp(this, (Editable) this.mText, keyCode, up);
            }
            hideErrorIfUnchanged();
        } else if (which == 2) {
            this.mMovement.onKeyUp(this, (Spannable) this.mText, keyCode, up);
            while (true) {
                repeatCount--;
                if (repeatCount <= 0) {
                    break;
                }
                this.mMovement.onKeyDown(this, (Spannable) this.mText, keyCode, down);
                this.mMovement.onKeyUp(this, (Spannable) this.mText, keyCode, up);
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
        if (!(getKeyListener() == null || (this.mSingleLine ^ 1) == 0 || this.mEditor == null || (this.mEditor.mInputType & 15) != 1)) {
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
        if (!isEnabled()) {
            return 0;
        }
        boolean doDown;
        if (event.getRepeatCount() == 0 && (KeyEvent.isModifierKey(keyCode) ^ 1) != 0) {
            this.mPreventDefaultMovement = false;
        }
        switch (keyCode) {
            case 4:
                if (!(this.mEditor == null || this.mEditor.getTextActionMode() == null)) {
                    stopTextActionMode();
                    return -1;
                }
            case 23:
                if (event.hasNoModifiers() && shouldAdvanceFocusOnEnter()) {
                    return 0;
                }
            case 61:
                if ((event.hasNoModifiers() || event.hasModifiers(1)) && shouldAdvanceFocusOnTab()) {
                    return 0;
                }
            case 66:
                if (event.hasNoModifiers()) {
                    if (this.mEditor != null && this.mEditor.mInputContentType != null && this.mEditor.mInputContentType.onEditorActionListener != null && this.mEditor.mInputContentType.onEditorActionListener.onEditorAction(this, 0, event)) {
                        this.mEditor.mInputContentType.enterDown = true;
                        return -1;
                    } else if ((event.getFlags() & 16) != 0 || shouldAdvanceFocusOnEnter()) {
                        return hasOnClickListeners() ? 0 : -1;
                    }
                }
                break;
            case 277:
                if (event.hasNoModifiers() && canCut() && onTextContextMenuItem(16908320)) {
                    return -1;
                }
            case 278:
                if (event.hasNoModifiers() && canCopy() && onTextContextMenuItem(16908321)) {
                    return -1;
                }
            case 279:
                if (event.hasNoModifiers() && canPaste() && onTextContextMenuItem(16908322)) {
                    return -1;
                }
        }
        if (!(this.mEditor == null || this.mEditor.mKeyListener == null)) {
            boolean handled;
            doDown = true;
            if (otherEvent != null) {
                try {
                    beginBatchEdit();
                    handled = this.mEditor.mKeyListener.onKeyOther(this, (Editable) this.mText, otherEvent);
                    hideErrorIfUnchanged();
                    doDown = false;
                    if (handled) {
                        return -1;
                    }
                    endBatchEdit();
                } catch (AbstractMethodError e) {
                } finally {
                    endBatchEdit();
                }
            }
            if (doDown) {
                beginBatchEdit();
                handled = this.mEditor.mKeyListener.onKeyDown(this, (Editable) this.mText, keyCode, event);
                endBatchEdit();
                hideErrorIfUnchanged();
                if (handled) {
                    return 1;
                }
            }
        }
        if (!(this.mMovement == null || this.mLayout == null)) {
            doDown = true;
            if (otherEvent != null) {
                try {
                    doDown = false;
                    if (this.mMovement.onKeyOther(this, (Spannable) this.mText, otherEvent)) {
                        return -1;
                    }
                } catch (AbstractMethodError e2) {
                }
            }
            if (doDown) {
                this.mValidSetCursorEvent = true;
                if (this.mMovement.onKeyDown(this, (Spannable) this.mText, keyCode, event)) {
                    if (event.getRepeatCount() == 0 && (KeyEvent.isModifierKey(keyCode) ^ 1) != 0) {
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
        int i = (!this.mPreventDefaultMovement || (KeyEvent.isModifierKey(keyCode) ^ 1) == 0) ? 0 : -1;
        return i;
    }

    public void resetErrorChangedFlag() {
        if (this.mEditor != null) {
            this.mEditor.mErrorWasChanged = false;
        }
    }

    public void hideErrorIfUnchanged() {
        if (this.mEditor != null && this.mEditor.mError != null && (this.mEditor.mErrorWasChanged ^ 1) != 0) {
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
        InputMethodManager imm;
        switch (keyCode) {
            case 23:
                if (event.hasNoModifiers() && !hasOnClickListeners() && this.mMovement != null && (this.mText instanceof Editable) && this.mLayout != null && onCheckIsTextEditor()) {
                    imm = InputMethodManager.peekInstance();
                    viewClicked(imm);
                    if (imm != null && getShowSoftInputOnFocus()) {
                        imm.showSoftInput(this, 0);
                    }
                }
                return super.onKeyUp(keyCode, event);
            case 66:
                if (event.hasNoModifiers()) {
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
                            if (!"user".equals(SystemProperties.get("ro.build.type", "eng"))) {
                                Log.d("View", "View hierachy: ");
                                getRootView().debug();
                                Log.d("View", "Found focusable view: ");
                                v.debug();
                                Log.d("View", "Current focused view: ");
                                debug();
                            }
                            throw new IllegalStateException("focus search returned a view that wasn't able to take focus!");
                        } else if ((event.getFlags() & 16) != 0) {
                            imm = InputMethodManager.peekInstance();
                            if (imm != null && imm.isActive(this)) {
                                imm.hideSoftInputFromWindow(getWindowToken(), 0);
                            }
                        }
                    }
                    return super.onKeyUp(keyCode, event);
                }
                break;
        }
        if (this.mEditor != null && this.mEditor.mKeyListener != null && this.mEditor.mKeyListener.onKeyUp(this, (Editable) this.mText, keyCode, event)) {
            return true;
        }
        if (this.mMovement == null || this.mLayout == null || !this.mMovement.onKeyUp(this, (Spannable) this.mText, keyCode, event)) {
            return super.onKeyUp(keyCode, event);
        }
        return true;
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
        int start;
        int end;
        int N;
        Editable content = getEditableText();
        if (text.text != null) {
            if (content == null) {
                setText(text.text, BufferType.EDITABLE);
            } else {
                start = 0;
                end = content.length();
                if (text.partialStartOffset >= 0) {
                    N = content.length();
                    start = text.partialStartOffset;
                    if (start > N) {
                        start = N;
                    }
                    end = text.partialEndOffset;
                    if (end > N) {
                        end = N;
                    }
                }
                removeParcelableSpans(content, start, end);
                if (!TextUtils.equals(content.subSequence(start, end), text.text)) {
                    content.replace(start, end, text.text);
                } else if (text.text instanceof Spanned) {
                    TextUtils.copySpansFrom((Spanned) text.text, 0, end - start, Object.class, content, start);
                }
            }
        }
        Spannable sp = (Spannable) getText();
        N = sp.length();
        start = text.selectionStart;
        if (start < 0) {
            start = 0;
        } else if (start > N) {
            start = N;
        }
        end = text.selectionEnd;
        if (end < 0) {
            end = 0;
        } else if (end > N) {
            end = N;
        }
        Selection.setSelection(sp, start, end);
        if ((text.flags & 2) != 0) {
            MetaKeyKeyListener.startSelecting(this, sp);
        } else {
            MetaKeyKeyListener.stopSelecting(this, sp);
        }
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
        return false;
    }

    private void nullLayouts() {
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

    private boolean shouldUseDefaultLayoutAlignment() {
        boolean z = true;
        if (!isRtlLocale()) {
            return true;
        }
        int id = getId();
        if (id == -1) {
            return false;
        }
        String checkPackName = "com.android.chrome";
        if (!checkPackName.equals(this.mContext.getPackageName())) {
            return false;
        }
        if (!(id == this.mContext.getResources().getIdentifier("url_bar", "id", checkPackName) || id == this.mContext.getResources().getIdentifier("trailing_text", "id", checkPackName))) {
            z = false;
        }
        return z;
    }

    private Alignment calAlign(int gravity) {
        Alignment align = Alignment.ALIGN_NORMAL;
        boolean isIW = false;
        if (Locale.getDefault().getLanguage().contains("iw")) {
            isIW = true;
        }
        if (gravity == Gravity.START) {
            if (getDefaultEditable() && isIW) {
                return Alignment.ALIGN_NORMAL;
            }
            if (shouldUseDefaultLayoutAlignment()) {
                return Alignment.ALIGN_NORMAL;
            }
            return Alignment.ALIGN_RIGHT;
        } else if (getDefaultEditable() && isIW) {
            return Alignment.ALIGN_OPPOSITE;
        } else {
            if (shouldUseDefaultLayoutAlignment()) {
                return Alignment.ALIGN_OPPOSITE;
            }
            return Alignment.ALIGN_LEFT;
        }
    }

    private Alignment getLayoutAlignment() {
        switch (getTextAlignment()) {
            case 1:
                switch (this.mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
                    case 1:
                        return Alignment.ALIGN_CENTER;
                    case 3:
                        return Alignment.ALIGN_LEFT;
                    case 5:
                        return Alignment.ALIGN_RIGHT;
                    case Gravity.START /*8388611*/:
                        return calAlign(Gravity.START);
                    case Gravity.END /*8388613*/:
                        return calAlign(Gravity.END);
                    default:
                        return Alignment.ALIGN_NORMAL;
                }
            case 2:
                return Alignment.ALIGN_NORMAL;
            case 3:
                return Alignment.ALIGN_OPPOSITE;
            case 4:
                return Alignment.ALIGN_CENTER;
            case 5:
                return isRtlLocale() ? Alignment.ALIGN_RIGHT : Alignment.ALIGN_LEFT;
            case 6:
                return isRtlLocale() ? Alignment.ALIGN_LEFT : Alignment.ALIGN_RIGHT;
            default:
                return Alignment.ALIGN_NORMAL;
        }
    }

    protected void makeNewLayout(int wantWidth, int hintWidth, Metrics boring, Metrics hintBoring, int ellipsisWidth, boolean bringIntoView) {
        stopMarquee();
        this.mOldMaximum = this.mMaximum;
        this.mOldMaxMode = this.mMaxMode;
        this.mHighlightPathBogus = true;
        if (wantWidth < 0) {
            wantWidth = 0;
        }
        if (hintWidth < 0) {
            hintWidth = 0;
        }
        Alignment alignment = getLayoutAlignment();
        boolean testDirChange = (!this.mSingleLine || this.mLayout == null) ? false : alignment != Alignment.ALIGN_NORMAL ? alignment == Alignment.ALIGN_OPPOSITE : true;
        int oldDir = 0;
        if (testDirChange) {
            oldDir = this.mLayout.getParagraphDirection(0);
        }
        boolean shouldEllipsize = this.mEllipsize != null && getKeyListener() == null;
        boolean switchEllipsize = this.mEllipsize == TruncateAt.MARQUEE ? this.mMarqueeFadeMode != 0 : false;
        TruncateAt effectiveEllipsize = this.mEllipsize;
        if (this.mEllipsize == TruncateAt.MARQUEE && this.mMarqueeFadeMode == 1) {
            effectiveEllipsize = TruncateAt.END_SMALL;
        }
        if (this.mTextDir == null) {
            this.mTextDir = getTextDirectionHeuristic();
        }
        this.mLayout = makeSingleLayout(wantWidth, boring, ellipsisWidth, alignment, shouldEllipsize, effectiveEllipsize, effectiveEllipsize == this.mEllipsize);
        if (switchEllipsize) {
            this.mSavedMarqueeModeLayout = makeSingleLayout(wantWidth, boring, ellipsisWidth, alignment, shouldEllipsize, effectiveEllipsize == TruncateAt.MARQUEE ? TruncateAt.END : TruncateAt.MARQUEE, effectiveEllipsize != this.mEllipsize);
        }
        shouldEllipsize = this.mEllipsize != null;
        this.mHintLayout = null;
        if (this.mHint != null) {
            if (shouldEllipsize) {
                hintWidth = wantWidth;
            }
            if (hintBoring == UNKNOWN_BORING) {
                hintBoring = BoringLayout.isBoring(this.mHint, this.mTextPaint, this.mTextDir, this.mHintBoring);
                if (hintBoring != null) {
                    this.mHintBoring = hintBoring;
                }
            }
            if (hintBoring != null) {
                if (hintBoring.width <= hintWidth && (!shouldEllipsize || hintBoring.width <= ellipsisWidth)) {
                    if (this.mSavedHintLayout != null) {
                        this.mHintLayout = this.mSavedHintLayout.replaceOrMake(this.mHint, this.mTextPaint, hintWidth, alignment, this.mSpacingMult, this.mSpacingAdd, hintBoring, this.mIncludePad);
                    } else {
                        this.mHintLayout = BoringLayout.make(this.mHint, this.mTextPaint, hintWidth, alignment, this.mSpacingMult, this.mSpacingAdd, hintBoring, this.mIncludePad);
                    }
                    this.mSavedHintLayout = (BoringLayout) this.mHintLayout;
                } else if (shouldEllipsize && hintBoring.width <= hintWidth) {
                    if (this.mSavedHintLayout != null) {
                        this.mHintLayout = this.mSavedHintLayout.replaceOrMake(this.mHint, this.mTextPaint, hintWidth, alignment, this.mSpacingMult, this.mSpacingAdd, hintBoring, this.mIncludePad, this.mEllipsize, ellipsisWidth);
                    } else {
                        this.mHintLayout = BoringLayout.make(this.mHint, this.mTextPaint, hintWidth, alignment, this.mSpacingMult, this.mSpacingAdd, hintBoring, this.mIncludePad, this.mEllipsize, ellipsisWidth);
                    }
                }
            }
            if (this.mHintLayout == null) {
                int i;
                Builder justificationMode = Builder.obtain(this.mHint, 0, this.mHint.length(), this.mTextPaint, hintWidth).setAlignment(alignment).setTextDirection(this.mTextDir).setLineSpacing(this.mSpacingAdd, this.mSpacingMult).setIncludePad(this.mIncludePad).setBreakStrategy(this.mBreakStrategy).setHyphenationFrequency(this.mHyphenationFrequency).setJustificationMode(this.mJustificationMode);
                if (this.mMaxMode == 1) {
                    i = this.mMaximum;
                } else {
                    i = Integer.MAX_VALUE;
                }
                Builder builder = justificationMode.setMaxLines(i);
                if (shouldEllipsize) {
                    builder.setEllipsize(this.mEllipsize).setEllipsizedWidth(ellipsisWidth);
                }
                this.mHintLayout = builder.build();
            }
        }
        if (bringIntoView || (testDirChange && oldDir != this.mLayout.getParagraphDirection(0))) {
            registerForPreDraw();
        }
        if (this.mEllipsize == TruncateAt.MARQUEE) {
            if (!compressText((float) ellipsisWidth)) {
                int height = this.mLayoutParams.height;
                if (height == -2 || height == -1) {
                    this.mRestartMarquee = true;
                } else {
                    startMarquee();
                }
            }
        }
        if (this.mEditor != null) {
            this.mEditor.prepareCursorControllers();
        }
    }

    protected Layout makeSingleLayout(int wantWidth, Metrics boring, int ellipsisWidth, Alignment alignment, boolean shouldEllipsize, TruncateAt effectiveEllipsize, boolean useSaved) {
        Layout result = null;
        if (this.mText instanceof Spannable) {
            result = new DynamicLayout(this.mText, this.mTransformed, this.mTextPaint, wantWidth, alignment, this.mTextDir, this.mSpacingMult, this.mSpacingAdd, this.mIncludePad, this.mBreakStrategy, this.mHyphenationFrequency, this.mJustificationMode, getKeyListener() == null ? effectiveEllipsize : null, ellipsisWidth);
        } else {
            if (boring == UNKNOWN_BORING) {
                boring = BoringLayout.isBoring(this.mTransformed, this.mTextPaint, this.mTextDir, this.mBoring);
                if (boring != null) {
                    this.mBoring = boring;
                }
            }
            if (boring != null) {
                if (boring.width <= wantWidth && (effectiveEllipsize == null || boring.width <= ellipsisWidth)) {
                    if (!useSaved || this.mSavedLayout == null) {
                        result = BoringLayout.make(this.mTransformed, this.mTextPaint, wantWidth, alignment, this.mSpacingMult, this.mSpacingAdd, boring, this.mIncludePad);
                    } else {
                        result = this.mSavedLayout.replaceOrMake(this.mTransformed, this.mTextPaint, wantWidth, alignment, this.mSpacingMult, this.mSpacingAdd, boring, this.mIncludePad);
                    }
                    if (useSaved) {
                        this.mSavedLayout = (BoringLayout) result;
                    }
                } else if (shouldEllipsize && boring.width <= wantWidth) {
                    result = (!useSaved || this.mSavedLayout == null) ? BoringLayout.make(this.mTransformed, this.mTextPaint, wantWidth, alignment, this.mSpacingMult, this.mSpacingAdd, boring, this.mIncludePad, effectiveEllipsize, ellipsisWidth) : this.mSavedLayout.replaceOrMake(this.mTransformed, this.mTextPaint, wantWidth, alignment, this.mSpacingMult, this.mSpacingAdd, boring, this.mIncludePad, effectiveEllipsize, ellipsisWidth);
                }
            }
        }
        if (result != null) {
            return result;
        }
        int i;
        Builder justificationMode = Builder.obtain(this.mTransformed, 0, this.mTransformed.length(), this.mTextPaint, wantWidth).setAlignment(alignment).setTextDirection(this.mTextDir).setLineSpacing(this.mSpacingAdd, this.mSpacingMult).setIncludePad(this.mIncludePad).setBreakStrategy(this.mBreakStrategy).setHyphenationFrequency(this.mHyphenationFrequency).setJustificationMode(this.mJustificationMode);
        if (this.mMaxMode == 1) {
            i = this.mMaximum;
        } else {
            i = Integer.MAX_VALUE;
        }
        Builder builder = justificationMode.setMaxLines(i);
        if (shouldEllipsize) {
            builder.setEllipsize(effectiveEllipsize).setEllipsizedWidth(ellipsisWidth);
        }
        return builder.build();
    }

    private boolean compressText(float width) {
        if (!isHardwareAccelerated() && width > 0.0f && this.mLayout != null && getLineCount() == 1 && (this.mUserSetTextScaleX ^ 1) != 0 && this.mTextPaint.getTextScaleX() == 1.0f) {
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
        int i;
        int n = layout.getLineCount();
        CharSequence text = layout.getText();
        float max = 0.0f;
        for (i = 0; i < n - 1; i++) {
            if (text.charAt(layout.getLineEnd(i) - 1) != 10) {
                return -1;
            }
        }
        for (i = 0; i < n; i++) {
            max = Math.max(max, layout.getLineWidth(i));
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

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int hintWidth;
        int height;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        Metrics boring = UNKNOWN_BORING;
        Metrics hintBoring = UNKNOWN_BORING;
        if (this.mTextDir == null) {
            this.mTextDir = getTextDirectionHeuristic();
        }
        int des = -1;
        boolean fromexisting = false;
        if (widthMode == 1073741824) {
            width = widthSize;
        } else {
            if (this.mLayout != null && this.mEllipsize == null) {
                des = desired(this.mLayout);
            }
            if (des < 0) {
                boring = BoringLayout.isBoring(this.mTransformed, this.mTextPaint, this.mTextDir, this.mBoring);
                if (boring != null) {
                    this.mBoring = boring;
                }
            } else {
                fromexisting = true;
            }
            if (boring == null || boring == UNKNOWN_BORING) {
                if (des < 0) {
                    des = (int) Math.ceil((double) Layout.getDesiredWidth(this.mTransformed, 0, this.mTransformed.length(), this.mTextPaint, this.mTextDir));
                }
                width = des;
            } else {
                width = boring.width;
            }
            Drawables dr = this.mDrawables;
            if (dr != null) {
                width = Math.max(Math.max(width, dr.mDrawableWidthTop), dr.mDrawableWidthBottom);
            }
            if (this.mHint != null) {
                int hintDes = -1;
                if (this.mHintLayout != null && this.mEllipsize == null) {
                    hintDes = desired(this.mHintLayout);
                }
                if (hintDes < 0) {
                    hintBoring = BoringLayout.isBoring(this.mHint, this.mTextPaint, this.mTextDir, this.mHintBoring);
                    if (hintBoring != null) {
                        this.mHintBoring = hintBoring;
                    }
                }
                if (hintBoring == null || hintBoring == UNKNOWN_BORING) {
                    if (hintDes < 0) {
                        hintDes = (int) Math.ceil((double) Layout.getDesiredWidth(this.mHint, 0, this.mHint.length(), this.mTextPaint, this.mTextDir));
                    }
                    hintWidth = hintDes;
                } else {
                    hintWidth = hintBoring.width;
                }
                if (hintWidth > width) {
                    width = hintWidth;
                }
            }
            width += getCompoundPaddingLeft() + getCompoundPaddingRight();
            if (this.mMaxWidthMode == 1) {
                width = Math.min(width, this.mMaxWidth * getLineHeight());
            } else {
                width = Math.min(width, this.mMaxWidth);
            }
            if (this.mMinWidthMode == 1) {
                width = Math.max(width, this.mMinWidth * getLineHeight());
            } else {
                width = Math.max(width, this.mMinWidth);
            }
            width = Math.max(width, getSuggestedMinimumWidth());
            if (widthMode == Integer.MIN_VALUE) {
                width = Math.min(widthSize, width);
            }
        }
        int want = (width - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        int unpaddedWidth = want;
        if (this.mHorizontallyScrolling) {
            want = 1048576;
        }
        int hintWant = want;
        hintWidth = this.mHintLayout == null ? hintWant : this.mHintLayout.getWidth();
        if (this.mLayout == null) {
            makeNewLayout(want, hintWant, boring, hintBoring, (width - getCompoundPaddingLeft()) - getCompoundPaddingRight(), false);
        } else {
            boolean layoutChanged = (this.mLayout.getWidth() == want && hintWidth == hintWant) ? this.mLayout.getEllipsizedWidth() != (width - getCompoundPaddingLeft()) - getCompoundPaddingRight() : true;
            boolean widthChanged = (this.mHint == null && this.mEllipsize == null && want > this.mLayout.getWidth()) ? !(this.mLayout instanceof BoringLayout) ? fromexisting && des >= 0 && des <= want : true : false;
            boolean maximumChanged = (this.mMaxMode == this.mOldMaxMode && this.mMaximum == this.mOldMaximum) ? false : true;
            if (layoutChanged || maximumChanged) {
                if (maximumChanged || !widthChanged) {
                    makeNewLayout(want, hintWant, boring, hintBoring, (width - getCompoundPaddingLeft()) - getCompoundPaddingRight(), false);
                } else {
                    this.mLayout.increaseWidthTo(want);
                }
            }
        }
        if (heightMode == 1073741824) {
            height = heightSize;
            this.mDesiredHeightAtMeasure = -1;
        } else {
            int desired = getDesiredHeight();
            height = desired;
            this.mDesiredHeightAtMeasure = desired;
            if (heightMode == Integer.MIN_VALUE) {
                height = Math.min(desired, heightSize);
            }
        }
        int unpaddedHeight = (height - getCompoundPaddingTop()) - getCompoundPaddingBottom();
        if (this.mMaxMode == 1 && this.mLayout.getLineCount() > this.mMaximum) {
            unpaddedHeight = Math.min(unpaddedHeight, this.mLayout.getLineTop(this.mMaximum));
        }
        if (this.mMovement != null || this.mLayout.getWidth() > unpaddedWidth || this.mLayout.getHeight() > unpaddedHeight) {
            registerForPreDraw();
        } else {
            -wrap17(0, 0);
        }
        -wrap6(width, height);
    }

    private void autoSizeText() {
        if (isAutoSizeEnabled()) {
            if (this.mNeedsAutoSizeText) {
                if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0) {
                    int availableWidth;
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
                }
                return;
            }
            this.mNeedsAutoSizeText = true;
        }
    }

    private int findLargestTextSizeWhichFits(RectF availableSpace) {
        int sizesCount = this.mAutoSizeTextSizesInPx.length;
        if (sizesCount == 0) {
            throw new IllegalStateException("No available text sizes to choose from.");
        }
        int bestSizeIndex = 0;
        int lowIndex = 1;
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

    private boolean suggestedSizeFitsInSpace(int suggestedSizeInPx, RectF availableSpace) {
        int i;
        CharSequence text = getText();
        int maxLines = getMaxLines();
        if (this.mTempTextPaint == null) {
            this.mTempTextPaint = new TextPaint();
        } else {
            this.mTempTextPaint.reset();
        }
        this.mTempTextPaint.set(getPaint());
        this.mTempTextPaint.setTextSize((float) suggestedSizeInPx);
        Builder layoutBuilder = Builder.obtain(text, 0, text.length(), this.mTempTextPaint, Math.round(availableSpace.right));
        Builder justificationMode = layoutBuilder.setAlignment(getLayoutAlignment()).setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier()).setIncludePad(getIncludeFontPadding()).setBreakStrategy(getBreakStrategy()).setHyphenationFrequency(getHyphenationFrequency()).setJustificationMode(getJustificationMode());
        if (this.mMaxMode == 1) {
            i = this.mMaximum;
        } else {
            i = Integer.MAX_VALUE;
        }
        justificationMode.setMaxLines(i).setTextDirection(getTextDirectionHeuristic());
        StaticLayout layout = layoutBuilder.build();
        return (maxLines == -1 || layout.getLineCount() <= maxLines) && ((float) layout.getHeight()) <= availableSpace.bottom;
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
        desired += padding;
        if (this.mMaxMode != 1) {
            desired = Math.min(desired, this.mMaximum);
        } else if (cap && linecount > this.mMaximum && (layout instanceof DynamicLayout)) {
            desired = layout.getLineTop(this.mMaximum);
            if (dr != null) {
                desired = Math.max(Math.max(desired, dr.mDrawableHeightLeft), dr.mDrawableHeightRight);
            }
            desired += padding;
            linecount = this.mMaximum;
        }
        if (this.mMinMode != 1) {
            desired = Math.max(desired, this.mMinimum);
        } else if (linecount < this.mMinimum) {
            desired += getLineHeight() * (this.mMinimum - linecount);
        }
        return Math.max(desired, getSuggestedMinimumHeight());
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
            if (this.mEllipsize != TruncateAt.MARQUEE) {
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

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mDeferScroll >= 0) {
            int curs = this.mDeferScroll;
            this.mDeferScroll = -1;
            bringPointIntoView(Math.min(curs, this.mText.length()));
        }
        autoSizeText();
    }

    private boolean isShowingHint() {
        return TextUtils.isEmpty(this.mText) ? TextUtils.isEmpty(this.mHint) ^ 1 : false;
    }

    private boolean bringTextIntoView() {
        int scrollx;
        int scrolly;
        Layout layout = isShowingHint() ? this.mHintLayout : this.mLayout;
        int line = 0;
        if ((this.mGravity & 112) == 80) {
            line = layout.getLineCount() - 1;
        }
        Alignment a = layout.getParagraphAlignment(line);
        int dir = layout.getParagraphDirection(line);
        this.mTextViewDirection = dir;
        int hspace = ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        int vspace = ((this.mBottom - this.mTop) - getExtendedPaddingTop()) - getExtendedPaddingBottom();
        int ht = layout.getHeight();
        if (a == Alignment.ALIGN_NORMAL) {
            a = dir == 1 ? Alignment.ALIGN_LEFT : Alignment.ALIGN_RIGHT;
        } else if (a == Alignment.ALIGN_OPPOSITE) {
            a = dir == 1 ? Alignment.ALIGN_RIGHT : Alignment.ALIGN_LEFT;
        }
        if (a == Alignment.ALIGN_CENTER) {
            int left = (int) Math.floor((double) layout.getLineLeft(line));
            int right = (int) Math.ceil((double) layout.getLineRight(line));
            if (right - left < hspace) {
                scrollx = ((right + left) / 2) - (hspace / 2);
            } else if (dir < 0) {
                scrollx = right - hspace;
            } else {
                scrollx = left;
            }
        } else if (a == Alignment.ALIGN_RIGHT) {
            scrollx = ((int) Math.ceil((double) layout.getLineRight(line))) - hspace;
        } else {
            scrollx = (int) Math.floor((double) layout.getLineLeft(line));
        }
        if (ht < vspace) {
            scrolly = 0;
        } else if ((this.mGravity & 112) == 80) {
            scrolly = ht - vspace;
        } else {
            scrolly = 0;
        }
        if (scrollx == this.mScrollX && scrolly == this.mScrollY) {
            return false;
        }
        -wrap17(scrollx, scrolly);
        return true;
    }

    public boolean bringPointIntoView(int offset) {
        if (isLayoutRequested()) {
            this.mDeferScroll = offset;
            return false;
        }
        boolean changed = false;
        Layout layout = isShowingHint() ? this.mHintLayout : this.mLayout;
        if (layout == null) {
            return false;
        }
        int grav;
        int line = layout.getLineForOffset(offset);
        switch (-getandroid-text-Layout$AlignmentSwitchesValues()[layout.getParagraphAlignment(line).ordinal()]) {
            case 2:
                grav = 1;
                break;
            case 3:
                grav = layout.getParagraphDirection(line);
                break;
            case 4:
                grav = -layout.getParagraphDirection(line);
                break;
            case 5:
                grav = -1;
                break;
            default:
                grav = 0;
                break;
        }
        int x = (int) layout.getPrimaryHorizontal(offset, grav > 0);
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
        if (hslack > vspace / 4) {
            vslack = vspace / 4;
        }
        if (hslack > hspace / 4) {
            hslack = hspace / 4;
        }
        int hs = this.mScrollX;
        int vs = this.mScrollY;
        if (top - vs < vslack) {
            vs = top - vslack;
        }
        if (bottom - vs > vspace - vslack) {
            vs = bottom - (vspace - vslack);
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
            if (x - hs > hspace - hslack) {
                hs = x - (hspace - hslack);
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
            if (x - hs > hspace - hslack) {
                hs = x - (hspace - hslack);
            }
        }
        if (!(hs == this.mScrollX && vs == this.mScrollY)) {
            if (this.mScroller == null) {
                -wrap17(hs, vs);
            } else {
                int dx = hs - this.mScrollX;
                int dy = vs - this.mScrollY;
                if (AnimationUtils.currentAnimationTimeMillis() - this.mLastScroll > 250) {
                    this.mScroller.startScroll(this.mScrollX, this.mScrollY, dx, dy);
                    -wrap1(this.mScroller.getDuration());
                    invalidate();
                } else {
                    if (!this.mScroller.isFinished()) {
                        this.mScroller.abortAnimation();
                    }
                    scrollBy(dx, dy);
                }
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

    public boolean moveCursorToVisibleOffset() {
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
            line = this.mLayout.getLineForVertical((vs + vslack) + (bottom - top));
        } else if (bottom > (vspace + vs) - vslack) {
            line = this.mLayout.getLineForVertical(((vspace + vs) - vslack) - (bottom - top));
        }
        int hspace = ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        int hs = this.mScrollX;
        int leftChar = this.mLayout.getOffsetForHorizontal(line, (float) hs);
        int rightChar = this.mLayout.getOffsetForHorizontal(line, (float) (hspace + hs));
        int lowChar = leftChar < rightChar ? leftChar : rightChar;
        int highChar = leftChar > rightChar ? leftChar : rightChar;
        int newStart = start;
        if (start < lowChar) {
            newStart = lowChar;
        } else if (start > highChar) {
            newStart = highChar;
        }
        if (newStart == start) {
            return false;
        }
        Selection.setSelection((Spannable) this.mText, newStart);
        return true;
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

    int viewportToContentHorizontalOffset() {
        return getCompoundPaddingLeft() - this.mScrollX;
    }

    int viewportToContentVerticalOffset() {
        int offset = getExtendedPaddingTop() - this.mScrollY;
        if ((this.mGravity & 112) != 48) {
            return offset + getVerticalOffset(false);
        }
        return offset;
    }

    public void debug(int depth) {
        super.debug(depth);
        String output = View.debugIndent(depth) + "frame={" + this.mLeft + ", " + this.mTop + ", " + this.mRight + ", " + this.mBottom + "} scroll={" + this.mScrollX + ", " + this.mScrollY + "} ";
        if (this.mText != null) {
            output = output + "mText=\"" + this.mText + "\" ";
            if (this.mLayout != null) {
                output = output + "mLayout width=" + this.mLayout.getWidth() + " height=" + this.mLayout.getHeight();
            }
        } else {
            output = output + "mText=NULL";
        }
        Log.d("View", output);
    }

    @ExportedProperty(category = "text")
    public int getSelectionStart() {
        return Selection.getSelectionStart(getText());
    }

    @ExportedProperty(category = "text")
    public int getSelectionEnd() {
        return Selection.getSelectionEnd(getText());
    }

    public boolean hasSelection() {
        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();
        if (selectionStart < 0 || selectionStart == selectionEnd) {
            return false;
        }
        return true;
    }

    String getSelectedText() {
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

    @RemotableViewMethod
    public void setSingleLine(boolean singleLine) {
        setInputTypeSingleLine(singleLine);
        applySingleLine(singleLine, true, true);
    }

    private void setInputTypeSingleLine(boolean singleLine) {
        if (this.mEditor != null && (this.mEditor.mInputType & 15) == 1) {
            Editor editor;
            if (singleLine) {
                editor = this.mEditor;
                editor.mInputType &= -131073;
                return;
            }
            editor = this.mEditor;
            editor.mInputType |= 131072;
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

    public void setEllipsize(TruncateAt where) {
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

    @ExportedProperty
    public TruncateAt getEllipsize() {
        return this.mEllipsize;
    }

    @RemotableViewMethod
    public void setSelectAllOnFocus(boolean selectAllOnFocus) {
        createEditorIfNeeded();
        this.mEditor.mSelectAllOnFocus = selectAllOnFocus;
        if (selectAllOnFocus && ((this.mText instanceof Spannable) ^ 1) != 0) {
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
        return this.mEditor == null ? true : this.mEditor.mCursorVisible;
    }

    private boolean canMarquee() {
        int width = ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        if (width <= 0) {
            return false;
        }
        if (this.mLayout.getLineWidth(0) > ((float) width)) {
            return true;
        }
        if (this.mMarqueeFadeMode == 0 || this.mSavedMarqueeModeLayout == null) {
            return false;
        }
        if (this.mSavedMarqueeModeLayout.getLineWidth(0) > ((float) width)) {
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
        if (!(this.mMarquee == null || (this.mMarquee.isStopped() ^ 1) == 0)) {
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
        if (this.mEllipsize != TruncateAt.MARQUEE) {
            return;
        }
        if (start) {
            startMarquee();
        } else {
            stopMarquee();
        }
    }

    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
    }

    protected void onSelectionChanged(int selStart, int selEnd) {
        sendAccessibilityEvent(8192);
    }

    public void addTextChangedListener(TextWatcher watcher) {
        if (this.mListeners == null) {
            this.mListeners = new ArrayList();
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

    private void sendBeforeTextChanged(CharSequence text, int start, int before, int after) {
        if (this.mListeners != null) {
            ArrayList<TextWatcher> list = this.mListeners;
            int count = list.size();
            for (int i = 0; i < count; i++) {
                ((TextWatcher) list.get(i)).beforeTextChanged(text, start, before, after);
            }
        }
        removeIntersectingNonAdjacentSpans(start, start + before, SpellCheckSpan.class);
        removeIntersectingNonAdjacentSpans(start, start + before, SuggestionSpan.class);
    }

    private <T> void removeIntersectingNonAdjacentSpans(int start, int end, Class<T> type) {
        if (this.mText instanceof Editable) {
            Editable text = this.mText;
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

    void removeAdjacentSuggestionSpans(int pos) {
        if (this.mText instanceof Editable) {
            Editable text = this.mText;
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

    void sendOnTextChanged(CharSequence text, int start, int before, int after) {
        if (this.mListeners != null) {
            ArrayList<TextWatcher> list = this.mListeners;
            int count = list.size();
            for (int i = 0; i < count; i++) {
                ((TextWatcher) list.get(i)).onTextChanged(text, start, before, after);
            }
        }
        if (this.mEditor != null) {
            this.mEditor.sendOnTextChanged(start, after);
        }
    }

    void sendAfterTextChanged(Editable text) {
        if (this.mListeners != null) {
            ArrayList<TextWatcher> list = this.mListeners;
            int count = list.size();
            for (int i = 0; i < count; i++) {
                ((TextWatcher) list.get(i)).afterTextChanged(text);
            }
        }
        notifyAutoFillManagerAfterTextChangedIfNeeded();
        hideErrorIfUnchanged();
    }

    private void notifyAutoFillManagerAfterTextChangedIfNeeded() {
        if (isAutofillable()) {
            AutofillManager afm = (AutofillManager) this.mContext.getSystemService(AutofillManager.class);
            if (afm != null) {
                afm.notifyValueChanged(this);
            }
        }
    }

    private boolean isAutofillable() {
        return getAutofillType() != 0;
    }

    void updateAfterEdit() {
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

    void handleTextChanged(CharSequence buffer, int start, int before, int after) {
        sLastCutCopyOrTextChangedTime = 0;
        InputMethodState ims = this.mEditor == null ? null : this.mEditor.mInputMethodState;
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

    void spanChange(Spanned buf, Object what, int oldStart, int newStart, int oldEnd, int newEnd) {
        boolean selChanged = false;
        int newSelStart = -1;
        int newSelEnd = -1;
        InputMethodState ims = this.mEditor == null ? null : this.mEditor.mInputMethodState;
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
            if (!(this.mEditor == null || (isFocused() ^ 1) == 0)) {
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

    public void dispatchStartTemporaryDetach() {
        super.onStartTemporaryDetach();
    }

    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (isTemporarilyDetached()) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
            return;
        }
        if (this.mEditor != null) {
            this.mEditor.onFocusChanged(focused, direction);
        }
        if (focused && (this.mText instanceof Spannable)) {
            MetaKeyKeyListener.resetMetaState(this.mText);
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

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (this.mEditor != null && visibility != 0) {
            this.mEditor.hideCursorAndSpanControllers();
            stopTextActionMode();
        }
    }

    public void clearComposingText() {
        if (this.mText instanceof Spannable) {
            BaseInputConnection.removeComposingSpans((Spannable) this.mText);
        }
    }

    public void setSelected(boolean selected) {
        boolean wasSelected = isSelected();
        super.setSelected(selected);
        if (selected != wasSelected && this.mEllipsize == TruncateAt.MARQUEE) {
            if (selected) {
                startMarquee();
            } else {
                stopMarquee();
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm;
        int action = event.getActionMasked();
        if (action == 0) {
            imm = InputMethodManager.peekInstance();
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
        if (this.mEditor != null && this.mEditor.mDiscardNextActionUp && action == 1) {
            this.mEditor.mDiscardNextActionUp = false;
            if (this.mEditor.mIsInsertionActionModeStartPending) {
                this.mEditor.startInsertionActionMode();
                this.mEditor.mIsInsertionActionModeStartPending = false;
            }
            return superResult;
        }
        boolean touchIsFinished;
        if (action != 1 || (this.mEditor != null && (this.mEditor.mIgnoreActionUpEvent ^ 1) == 0)) {
            touchIsFinished = false;
        } else {
            touchIsFinished = isFocused();
        }
        if ((this.mMovement != null || onCheckIsTextEditor()) && isEnabled() && (this.mText instanceof Spannable) && this.mLayout != null) {
            boolean handled = false;
            this.mValidSetCursorEvent = true;
            if (this.mMovement != null) {
                handled = this.mMovement.onTouchEvent(this, (Spannable) this.mText, event);
            }
            boolean textIsSelectable = isTextSelectable();
            if (touchIsFinished && this.mLinksClickable && this.mAutoLinkMask != 0 && textIsSelectable) {
                ClickableSpan[] links = (ClickableSpan[]) ((Spannable) this.mText).getSpans(getSelectionStart(), getSelectionEnd(), ClickableSpan.class);
                if (links.length > 0) {
                    links[0].onClick(this);
                    handled = true;
                }
            }
            if (touchIsFinished && (isTextEditable() || textIsSelectable)) {
                imm = InputMethodManager.peekInstance();
                viewClicked(imm);
                if (isTextEditable() && this.mEditor.mShowSoftInputOnFocus && imm != null) {
                    imm.showSoftInput(this, 0);
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

    public boolean onGenericMotionEvent(MotionEvent event) {
        if (!(this.mMovement == null || !(this.mText instanceof Spannable) || this.mLayout == null)) {
            try {
                if (this.mMovement.onGenericMotionEvent(this, (Spannable) this.mText, event)) {
                    return true;
                }
            } catch (AbstractMethodError e) {
            }
        }
        return super.-wrap8(event);
    }

    protected void onCreateContextMenu(ContextMenu menu) {
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

    boolean isTextEditable() {
        return ((this.mText instanceof Editable) && onCheckIsTextEditor()) ? isEnabled() : false;
    }

    public boolean didTouchFocusSelect() {
        return this.mEditor != null ? this.mEditor.mTouchFocusSelected : false;
    }

    public void cancelLongPress() {
        super.cancelLongPress();
        if (this.mEditor != null) {
            this.mEditor.mIgnoreActionUpEvent = true;
        }
    }

    public boolean onTrackballEvent(MotionEvent event) {
        if (this.mMovement == null || !(this.mText instanceof Spannable) || this.mLayout == null || !this.mMovement.onTrackballEvent(this, (Spannable) this.mText, event)) {
            return super.onTrackballEvent(event);
        }
        return true;
    }

    public void setScroller(Scroller s) {
        this.mScroller = s;
    }

    protected float getLeftFadingEdgeStrength() {
        if (isMarqueeFadeEnabled() && this.mMarquee != null && (this.mMarquee.isStopped() ^ 1) != 0) {
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

    protected float getRightFadingEdgeStrength() {
        if (isMarqueeFadeEnabled() && this.mMarquee != null && (this.mMarquee.isStopped() ^ 1) != 0) {
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
        if (this.mEllipsize == TruncateAt.MARQUEE) {
            return this.mMarqueeFadeMode != 1;
        } else {
            return false;
        }
    }

    protected int computeHorizontalScrollRange() {
        if (this.mLayout == null) {
            return super.computeHorizontalScrollRange();
        }
        int lineWidth = (this.mSingleLine && (this.mGravity & 7) == 3) ? (int) this.mLayout.getLineWidth(0) : this.mLayout.getWidth();
        return lineWidth;
    }

    protected int computeVerticalScrollRange() {
        if (this.mLayout != null) {
            return this.mLayout.getHeight();
        }
        return super.computeVerticalScrollRange();
    }

    protected int computeVerticalScrollExtent() {
        return (getHeight() - getCompoundPaddingTop()) - getCompoundPaddingBottom();
    }

    public void findViewsWithText(ArrayList<View> outViews, CharSequence searched, int flags) {
        super.findViewsWithText(outViews, searched, flags);
        if (!outViews.contains(this) && (flags & 1) != 0 && (TextUtils.isEmpty(searched) ^ 1) != 0 && (TextUtils.isEmpty(this.mText) ^ 1) != 0) {
            if (this.mText.toString().toLowerCase().contains(searched.toString().toLowerCase())) {
                outViews.add(this);
            }
        }
    }

    public static ColorStateList getTextColors(Context context, TypedArray attrs) {
        if (attrs == null) {
            throw new NullPointerException();
        }
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

    public static int getTextColor(Context context, TypedArray attrs, int def) {
        ColorStateList colors = getTextColors(context, attrs);
        if (colors == null) {
            return def;
        }
        return colors.getDefaultColor();
    }

    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        if (!event.hasModifiers(4096)) {
            if (event.hasModifiers(4097)) {
                switch (keyCode) {
                    case 50:
                        if (canPaste()) {
                            return onTextContextMenuItem(16908337);
                        }
                        break;
                    case 54:
                        if (canRedo()) {
                            return onTextContextMenuItem(16908339);
                        }
                        break;
                }
            }
        }
        switch (keyCode) {
            case 29:
                if (canSelectText()) {
                    return onTextContextMenuItem(16908319);
                }
                break;
            case 31:
                if (canCopy()) {
                    return onTextContextMenuItem(16908321);
                }
                break;
            case 50:
                if (canPaste()) {
                    return onTextContextMenuItem(16908322);
                }
                break;
            case 52:
                if (canCut()) {
                    return onTextContextMenuItem(16908320);
                }
                break;
            case 54:
                if (canUndo()) {
                    return onTextContextMenuItem(16908338);
                }
                break;
        }
        return super.onKeyShortcut(keyCode, event);
    }

    boolean canSelectText() {
        return (this.mText.length() == 0 || this.mEditor == null) ? false : this.mEditor.hasSelectionController();
    }

    boolean textCanBeSelected() {
        boolean z = false;
        if (this.mMovement == null || (this.mMovement.canSelectArbitrarily() ^ 1) != 0) {
            return false;
        }
        if (isTextEditable()) {
            z = true;
        } else if (isTextSelectable() && (this.mText instanceof Spannable)) {
            z = isEnabled();
        }
        return z;
    }

    private Locale getTextServicesLocale(boolean allowNullLocale) {
        updateTextServicesLocaleAsync();
        if (this.mCurrentSpellCheckerLocaleCache != null || (allowNullLocale ^ 1) == 0) {
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

    protected boolean supportsAutoSizeText() {
        return true;
    }

    public Locale getSpellCheckerLocale() {
        return getTextServicesLocale(true);
    }

    private void updateTextServicesLocaleAsync() {
        AsyncTask.execute(new Runnable() {
            public void run() {
                TextView.this.updateTextServicesLocaleLocked();
            }
        });
    }

    private void updateTextServicesLocaleLocked() {
        Locale locale;
        SpellCheckerSubtype subtype = ((TextServicesManager) this.mContext.getSystemService("textservices")).getCurrentSpellCheckerSubtype(true);
        if (subtype != null) {
            locale = subtype.getLocaleObject();
        } else {
            locale = null;
        }
        this.mCurrentSpellCheckerLocaleCache = locale;
    }

    void onLocaleChanged() {
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

    private void onProvideAutoStructureForAssistOrAutofill(ViewStructure structure, boolean forAutofill) {
        boolean isPassword;
        if (hasPasswordTransformationMethod()) {
            isPassword = true;
        } else {
            isPassword = isPasswordInputType(getInputType());
        }
        if (forAutofill) {
            structure.setDataIsSensitive(this.mTextFromResource ^ 1);
        }
        if (!isPassword || forAutofill) {
            if (this.mLayout == null) {
                assumeLayout();
            }
            Layout layout = this.mLayout;
            int lineCount = layout.getLineCount();
            CharSequence text;
            if (lineCount <= 1) {
                text = getText();
                if (forAutofill) {
                    structure.setText(text);
                } else {
                    structure.setText(text, getSelectionStart(), getSelectionEnd());
                }
            } else {
                int topLine;
                int bottomLine;
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
                int expandedBottomLine = bottomLine + ((bottomLine - topLine) / 2);
                if (expandedBottomLine >= lineCount) {
                    expandedBottomLine = lineCount - 1;
                }
                int expandedTopChar = layout.getLineStart(expandedTopLine);
                int expandedBottomChar = layout.getLineEnd(expandedBottomLine);
                int selStart = getSelectionStart();
                int selEnd = getSelectionEnd();
                if (selStart < selEnd) {
                    if (selStart < expandedTopChar) {
                        expandedTopChar = selStart;
                    }
                    if (selEnd > expandedBottomChar) {
                        expandedBottomChar = selEnd;
                    }
                }
                text = getText();
                if (expandedTopChar > 0 || expandedBottomChar < text.length()) {
                    text = text.subSequence(expandedTopChar, expandedBottomChar);
                }
                if (forAutofill) {
                    structure.setText(text);
                } else {
                    structure.setText(text, selStart - expandedTopChar, selEnd - expandedTopChar);
                    int[] lineOffsets = new int[((bottomLine - topLine) + 1)];
                    int[] lineBaselines = new int[((bottomLine - topLine) + 1)];
                    int baselineOffset = getBaselineOffset();
                    for (int i = topLine; i <= bottomLine; i++) {
                        lineOffsets[i - topLine] = layout.getLineStart(i);
                        lineBaselines[i - topLine] = layout.getLineBaseline(i) + baselineOffset;
                    }
                    structure.setTextLines(lineOffsets, lineBaselines);
                }
            }
            if (!forAutofill) {
                int style = 0;
                int typefaceStyle = getTypefaceStyle();
                if ((typefaceStyle & 1) != 0) {
                    style = 1;
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
                structure.setTextStyle(getTextSize(), getCurrentTextColor(), 1, style);
            }
        }
        structure.setHint(getHint());
        structure.setInputType(getInputType());
    }

    boolean canRequestAutofill() {
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
        if (value.isText() && (isTextEditable() ^ 1) == 0) {
            setText(value.getTextValue(), this.mBufferType, true, 0);
        } else {
            Log.w(LOG_TAG, value + " could not be autofilled into " + this);
        }
    }

    public int getAutofillType() {
        return isTextEditable() ? 1 : 0;
    }

    public AutofillValue getAutofillValue() {
        return isTextEditable() ? AutofillValue.forText(getText()) : null;
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
        if (this.mStyle == R.attr.listSeparatorTextViewStyle) {
            info.setEnabled(true);
        }
        if (this.mBufferType == BufferType.EDITABLE) {
            info.setEditable(true);
            if (isEnabled()) {
                info.addAction(AccessibilityAction.ACTION_SET_TEXT);
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
                info.addAction(new AccessibilityAction(268435456, getResources().getString(R.string.share)));
            }
            if (canProcessText()) {
                this.mEditor.mProcessTextIntentActionsHandler.onInitializeAccessibilityNodeInfo(info);
            }
        }
        for (InputFilter filter : this.mFilters) {
            if (filter instanceof LengthFilter) {
                info.setMaxTextLength(((LengthFilter) filter).getMax());
            }
        }
        if (!isSingleLine()) {
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
            populateCharacterBounds(builder, positionInfoStartIndex, positionInfoStartIndex + positionInfoLength, (float) viewportToContentHorizontalOffset(), (float) viewportToContentVerticalOffset());
            CursorAnchorInfo cursorAnchorInfo = builder.setMatrix(null).build();
            for (int i = 0; i < positionInfoLength; i++) {
                if ((cursorAnchorInfo.getCharacterBoundsFlags(positionInfoStartIndex + i) & 1) == 1) {
                    RectF bounds = cursorAnchorInfo.getCharacterBounds(positionInfoStartIndex + i);
                    if (bounds != null) {
                        mapRectFromViewToScreenCoords(bounds, true);
                        boundingRects[i] = bounds;
                    }
                }
            }
            info.getExtras().putParcelableArray(extraDataKey, boundingRects);
        }
    }

    public void populateCharacterBounds(CursorAnchorInfo.Builder builder, int startIndex, int endIndex, float viewportToContentHorizontalOffset, float viewportToContentVerticalOffset) {
        int minLine = this.mLayout.getLineForOffset(startIndex);
        int maxLine = this.mLayout.getLineForOffset(endIndex - 1);
        for (int line = minLine; line <= maxLine; line++) {
            int lineStart = this.mLayout.getLineStart(line);
            int lineEnd = this.mLayout.getLineEnd(line);
            int offsetStart = Math.max(lineStart, startIndex);
            int offsetEnd = Math.min(lineEnd, endIndex);
            boolean ltrLine = this.mLayout.getParagraphDirection(line) == 1;
            float[] widths = new float[(offsetEnd - offsetStart)];
            this.mLayout.getPaint().getTextWidths(this.mText, offsetStart, offsetEnd, widths);
            float top = (float) this.mLayout.getLineTop(line);
            float bottom = (float) this.mLayout.getLineBottom(line);
            for (int offset = offsetStart; offset < offsetEnd; offset++) {
                float left;
                float right;
                float charWidth = widths[offset - offsetStart];
                boolean isRtl = this.mLayout.isRtlCharAt(offset);
                float primary = this.mLayout.getPrimaryHorizontal(offset);
                float secondary = this.mLayout.getSecondaryHorizontal(offset);
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
                boolean isTopLeftVisible = isPositionVisible(localLeft, localTop);
                boolean isBottomRightVisible = isPositionVisible(localRight, localBottom);
                int characterBoundsFlags = 0;
                if (isTopLeftVisible || isBottomRightVisible) {
                    characterBoundsFlags = 1;
                }
                if (!(isTopLeftVisible && (isBottomRightVisible ^ 1) == 0)) {
                    characterBoundsFlags |= 2;
                }
                if (isRtl) {
                    characterBoundsFlags |= 4;
                }
                builder.addCharacterBounds(offset, localLeft, localTop, localRight, localBottom, characterBoundsFlags);
            }
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0038, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                if (position[0] >= 0.0f && position[1] >= 0.0f) {
                    if (position[0] <= ((float) view.getWidth()) && position[1] <= ((float) view.getHeight())) {
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
        if (this.mEditor != null && this.mEditor.mProcessTextIntentActionsHandler.performAccessibilityAction(action)) {
            return true;
        }
        CharSequence text;
        switch (action) {
            case 16:
                return performAccessibilityActionClick(arguments);
            case 256:
            case 512:
                ensureIterableTextForAccessibilitySelectable();
                return super.-wrap10(action, arguments);
            case 16384:
                return isFocused() && canCopy() && onTextContextMenuItem(16908321);
            case 32768:
                return isFocused() && canPaste() && onTextContextMenuItem(16908322);
            case 65536:
                return isFocused() && canCut() && onTextContextMenuItem(16908320);
            case 131072:
                ensureIterableTextForAccessibilitySelectable();
                text = getIterableTextForAccessibility();
                if (text == null) {
                    return false;
                }
                int start;
                int end;
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
                        Selection.removeSelection((Spannable) text);
                        return true;
                    } else if (start >= 0 && start <= end && end <= text.length()) {
                        Selection.setSelection((Spannable) text, start, end);
                        if (this.mEditor != null) {
                            this.mEditor.startSelectionActionModeAsync(false);
                        }
                        return true;
                    }
                }
                return false;
            case 2097152:
                if (!isEnabled() || this.mBufferType != BufferType.EDITABLE) {
                    return false;
                }
                if (arguments != null) {
                    text = arguments.getCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE);
                } else {
                    text = null;
                }
                setText(text);
                if (this.mText != null) {
                    int updatedTextLength = this.mText.length();
                    if (updatedTextLength > 0) {
                        Selection.setSelection((Spannable) this.mText, updatedTextLength);
                    }
                }
                return true;
            case 268435456:
                return isFocused() && canShare() && onTextContextMenuItem(16908341);
            default:
                return super.-wrap10(action, arguments);
        }
    }

    private boolean performAccessibilityActionClick(Bundle arguments) {
        boolean handled = false;
        if (!isEnabled()) {
            return false;
        }
        if (isClickable() || isLongClickable()) {
            if (isFocusable() && (isFocused() ^ 1) != 0) {
                requestFocus();
            }
            performClick();
            handled = true;
        }
        if ((this.mMovement != null || onCheckIsTextEditor()) && hasSpannableText() && this.mLayout != null && ((isTextEditable() || isTextSelectable()) && isFocused())) {
            InputMethodManager imm = InputMethodManager.peekInstance();
            viewClicked(imm);
            if (!(isTextSelectable() || !this.mEditor.mShowSoftInputOnFocus || imm == null)) {
                handled |= imm.showSoftInput(this, 0);
            }
        }
        return handled;
    }

    private boolean hasSpannableText() {
        return this.mText != null ? this.mText instanceof Spannable : false;
    }

    public void sendAccessibilityEventInternal(int eventType) {
        if (eventType == 32768 && this.mEditor != null) {
            this.mEditor.mProcessTextIntentActionsHandler.initializeAccessibilityActions();
        }
        if (eventType != 4096) {
            super.sendAccessibilityEventInternal(eventType);
        }
    }

    private CharSequence getTextForAccessibility() {
        if (TextUtils.isEmpty(this.mText)) {
            return this.mHint;
        }
        return this.mTransformed;
    }

    void sendAccessibilityEventTypeViewTextChanged(CharSequence beforeText, int fromIndex, int removedCount, int addedCount) {
        AccessibilityEvent event = AccessibilityEvent.obtain(16);
        event.setFromIndex(fromIndex);
        event.setRemovedCount(removedCount);
        event.setAddedCount(addedCount);
        event.setBeforeText(beforeText);
        sendAccessibilityEventUnchecked(event);
    }

    public boolean isInputMethodTarget() {
        InputMethodManager imm = InputMethodManager.peekInstance();
        return imm != null ? imm.isActive(this) : false;
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
        switch (id) {
            case 16908319:
                boolean hadSelection = hasSelection();
                selectAllText();
                if (this.mEditor != null && hadSelection) {
                    this.mEditor.invalidateActionModeAsync();
                }
                return true;
            case 16908320:
                setPrimaryClip(ClipData.newPlainText(null, getTransformedText(min, max)));
                if (HwDeviceManager.disallowOp(23)) {
                    Log.i(LOG_TAG, "TextView cut is not allowed by MDM!");
                    return true;
                }
                deleteText_internal(min, max);
                sendMessageToRecsys(EVENT_CUT);
                return true;
            case 16908321:
                setPrimaryClip(ClipData.newPlainText(null, getTransformedText(min, max)));
                stopTextActionMode();
                if (getTextCopyFinishedListener() != null) {
                    getTextCopyFinishedListener().copyDone();
                }
                sendMessageToRecsys(EVENT_COPY);
                return true;
            case 16908322:
                paste(min, max, true);
                return true;
            case 16908337:
                paste(min, max, false);
                return true;
            case 16908338:
                if (this.mEditor != null) {
                    this.mEditor.undo();
                }
                return true;
            case 16908339:
                if (this.mEditor != null) {
                    this.mEditor.redo();
                }
                return true;
            case 16908340:
                if (this.mEditor != null) {
                    try {
                        this.mEditor.replace();
                    } catch (NotFoundException e) {
                        Log.e(LOG_TAG, "Widget of Editor resource not found issue.", e);
                    }
                }
                return true;
            case 16908341:
                shareSelectedText();
                return true;
            case 16908355:
                requestAutofill();
                stopTextActionMode();
                return true;
            default:
                return false;
        }
    }

    CharSequence getTransformedText(int start, int end) {
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
            } else {
                contentResolver.insert(Uri.parse(EVENT_URI_CONTENT), valuesCopy);
            }
        } catch (IllegalArgumentException e) {
            Log.w(LOG_TAG, "onTextContextMenuItem " + str + "Connect the com.huawei.recsys.provider failed!");
        }
    }

    public boolean performLongClick() {
        boolean handled = false;
        if (this.mEditor != null) {
            this.mEditor.mIsBeingLongClicked = true;
        }
        boolean hapticEffectDone = false;
        if (super.-wrap11()) {
            handled = true;
            hapticEffectDone = true;
        }
        if (this.mEditor != null) {
            handled |= this.mEditor.performLongClick(handled);
            this.mEditor.mIsBeingLongClicked = false;
        }
        if (handled) {
            if (this.mHwTextView == null || (hapticEffectDone ^ 1) == 0 || !mIsVibrateImplemented) {
                performHapticFeedback(0);
            } else {
                this.mHwTextView.playIvtEffect(this.mContext, "TEXTVIEW_LONG_PRESS_SELECTWORD");
            }
            if (this.mEditor != null) {
                this.mEditor.mDiscardNextActionUp = true;
            }
        } else {
            MetricsLogger.action(this.mContext, (int) MetricsEvent.TEXT_LONGPRESS, 0);
        }
        return handled;
    }

    protected void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
        super.onScrollChanged(horiz, vert, oldHoriz, oldVert);
        if (this.mEditor != null) {
            this.mEditor.onScrollChanged();
        }
    }

    public boolean isSuggestionsEnabled() {
        boolean z = true;
        if (this.mEditor == null || (this.mEditor.mInputType & 15) != 1 || (this.mEditor.mInputType & 524288) > 0) {
            return false;
        }
        int variation = this.mEditor.mInputType & InputType.TYPE_MASK_VARIATION;
        if (!(variation == 0 || variation == 48 || variation == 80 || variation == 64 || variation == 160)) {
            z = false;
        }
        return z;
    }

    public void setCustomSelectionActionModeCallback(Callback actionModeCallback) {
        createEditorIfNeeded();
        this.mEditor.mCustomSelectionActionModeCallback = actionModeCallback;
    }

    public Callback getCustomSelectionActionModeCallback() {
        return this.mEditor == null ? null : this.mEditor.mCustomSelectionActionModeCallback;
    }

    public void setCustomInsertionActionModeCallback(Callback actionModeCallback) {
        createEditorIfNeeded();
        this.mEditor.mCustomInsertionActionModeCallback = actionModeCallback;
    }

    public Callback getCustomInsertionActionModeCallback() {
        return this.mEditor == null ? null : this.mEditor.mCustomInsertionActionModeCallback;
    }

    public void setTextClassifier(TextClassifier textClassifier) {
        this.mTextClassifier = textClassifier;
    }

    public TextClassifier getTextClassifier() {
        if (this.mTextClassifier == null) {
            TextClassificationManager tcm = (TextClassificationManager) this.mContext.getSystemService(TextClassificationManager.class);
            if (tcm != null) {
                this.mTextClassifier = tcm.getTextClassifier();
            } else {
                this.mTextClassifier = TextClassifier.NO_OP;
            }
        }
        return this.mTextClassifier;
    }

    protected void stopTextActionMode() {
        if (this.mEditor != null) {
            this.mEditor.stopTextActionMode();
        }
    }

    boolean canUndo() {
        return this.mEditor != null ? this.mEditor.canUndo() : false;
    }

    boolean canRedo() {
        return this.mEditor != null ? this.mEditor.canRedo() : false;
    }

    boolean canCut() {
        if (!hasPasswordTransformationMethod() && this.mText.length() > 0 && hasSelection() && (this.mText instanceof Editable) && this.mEditor != null && this.mEditor.mKeyListener != null) {
            return true;
        }
        return false;
    }

    boolean canCopy() {
        if (!hasPasswordTransformationMethod() && this.mText.length() > 0 && hasSelection() && this.mEditor != null) {
            return true;
        }
        return false;
    }

    boolean canShare() {
        if (getContext().canStartActivityForResult() && (isDeviceProvisioned() ^ 1) == 0) {
            return canCopy();
        }
        return false;
    }

    boolean isDeviceProvisioned() {
        if (this.mDeviceProvisionedState == 0) {
            int i;
            if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
                i = 2;
            } else {
                i = 1;
            }
            this.mDeviceProvisionedState = i;
        }
        if (this.mDeviceProvisionedState == 2) {
            return true;
        }
        return false;
    }

    boolean canPaste() {
        if (!(this.mText instanceof Editable) || this.mEditor == null || this.mEditor.mKeyListener == null || getSelectionStart() < 0 || getSelectionEnd() < 0) {
            return false;
        }
        return ((ClipboardManager) getContext().getSystemService("clipboard")).hasPrimaryClip();
    }

    boolean canPasteAsPlainText() {
        if (!canPaste()) {
            return false;
        }
        ClipData clipData = ((ClipboardManager) getContext().getSystemService("clipboard")).getPrimaryClip();
        ClipDescription description = clipData.getDescription();
        boolean isPlainType = description.hasMimeType("text/plain");
        CharSequence text = clipData.getItemAt(0).getText();
        if (isPlainType && (text instanceof Spanned) && TextUtils.hasStyleSpan((Spanned) text)) {
            return true;
        }
        return description.hasMimeType("text/html");
    }

    boolean canProcessText() {
        if (getId() == -1) {
            return false;
        }
        return canShare();
    }

    boolean canSelectAllText() {
        if (!canSelectText() || (hasPasswordTransformationMethod() ^ 1) == 0) {
            return false;
        }
        return (getSelectionStart() == 0 && getSelectionEnd() == this.mText.length()) ? false : true;
    }

    boolean selectAllText() {
        int length = this.mText.length();
        Selection.setSelection((Spannable) this.mText, 0, length);
        return length > 0;
    }

    void replaceSelectionWithText(CharSequence text) {
        ((Editable) this.mText).replace(getSelectionStart(), getSelectionEnd(), text);
    }

    private void paste(int min, int max, boolean withFormatting) {
        ClipData clip = ((ClipboardManager) getContext().getSystemService("clipboard")).getPrimaryClip();
        if (clip != null) {
            boolean didFirst = false;
            for (int i = 0; i < clip.getItemCount(); i++) {
                CharSequence paste;
                if (withFormatting) {
                    paste = clip.getItemAt(i).coerceToStyledText(getContext());
                } else {
                    CharSequence text = clip.getItemAt(i).coerceToText(getContext());
                    paste = text instanceof Spanned ? text.toString() : text;
                }
                if (paste != null) {
                    if (didFirst) {
                        ((Editable) this.mText).insert(getSelectionEnd(), "\n");
                        ((Editable) this.mText).insert(getSelectionEnd(), paste);
                    } else {
                        Selection.setSelection((Spannable) this.mText, max);
                        ((Editable) this.mText).replace(min, max, paste);
                        didFirst = true;
                    }
                }
            }
            sLastCutCopyOrTextChangedTime = 0;
        }
    }

    private void shareSelectedText() {
        String selectedText = getSelectedText();
        if (selectedText != null && (selectedText.isEmpty() ^ 1) != 0) {
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null && imm.isActive(this)) {
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
            }
            Intent sharingIntent = new Intent("android.intent.action.SEND");
            sharingIntent.setType("text/plain");
            sharingIntent.removeExtra("android.intent.extra.TEXT");
            sharingIntent.putExtra("android.intent.extra.TEXT", selectedText);
            getContext().startActivity(Intent.createChooser(sharingIntent, this.mContext.getResources().getText(R.string.share)));
            Selection.setSelection((Spannable) this.mText, getSelectionEnd());
        }
    }

    private void setPrimaryClip(ClipData clip) {
        ((ClipboardManager) getContext().getSystemService("clipboard")).setPrimaryClip(clip);
        sLastCutCopyOrTextChangedTime = SystemClock.uptimeMillis();
    }

    public int getOffsetForPosition(float x, float y) {
        if (getLayout() == null) {
            return -1;
        }
        return getOffsetAtCoordinate(getLineAtCoordinate(y), x);
    }

    float convertToLocalHorizontalCoordinate(float x) {
        return Math.min((float) ((getWidth() - getTotalPaddingRight()) - 1), Math.max(0.0f, x - ((float) getTotalPaddingLeft()))) + ((float) getScrollX());
    }

    int getLineAtCoordinate(float y) {
        return getLayout().getLineForVertical((int) (Math.min((float) ((getHeight() - getTotalPaddingBottom()) - 1), Math.max(0.0f, y - ((float) getTotalPaddingTop()))) + ((float) getScrollY())));
    }

    int getLineAtCoordinateUnclamped(float y) {
        return getLayout().getLineForVertical((int) ((y - ((float) getTotalPaddingTop())) + ((float) getScrollY())));
    }

    int getOffsetAtCoordinate(int line, float x) {
        return getLayout().getOffsetForHorizontal(line, convertToLocalHorizontalCoordinate(x));
    }

    public boolean onDragEvent(DragEvent event) {
        switch (event.getAction()) {
            case 1:
                return this.mEditor != null ? this.mEditor.hasInsertionController() : false;
            case 2:
                if (this.mText instanceof Spannable) {
                    Selection.setSelection((Spannable) this.mText, getOffsetForPosition(event.getX(), event.getY()));
                }
                return true;
            case 3:
                if (this.mEditor != null) {
                    this.mEditor.onDrop(event);
                }
                return true;
            case 5:
                requestFocus();
                return true;
            default:
                return true;
        }
    }

    boolean isInBatchEditMode() {
        boolean z = false;
        if (this.mEditor == null) {
            return false;
        }
        InputMethodState ims = this.mEditor.mInputMethodState;
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

    protected TextDirectionHeuristic getTextDirectionHeuristic() {
        if (hasPasswordTransformationMethod()) {
            if (isRtlLocale()) {
                return TextDirectionHeuristics.FIRSTSTRONG_LTR;
            }
            return TextDirectionHeuristics.LTR;
        } else if (this.mEditor == null || (this.mEditor.mInputType & 15) != 3) {
            boolean defaultIsRtl = getLayoutDirection() == 1;
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
                    TextDirectionHeuristic textDirectionHeuristic;
                    if (defaultIsRtl) {
                        textDirectionHeuristic = TextDirectionHeuristics.ANYRTL_LTR;
                    } else {
                        textDirectionHeuristic = TextDirectionHeuristics.FIRSTSTRONG_LTR;
                    }
                    return textDirectionHeuristic;
            }
        } else {
            byte digitDirection = Character.getDirectionality(DecimalFormatSymbols.getInstance(getTextLocale()).getDigitStrings()[0].codePointAt(0));
            if (digitDirection == (byte) 1 || digitDirection == (byte) 2) {
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

    protected void resetResolvedDrawables() {
        super.resetResolvedDrawables();
        this.mLastLayoutDirection = -1;
    }

    protected void viewClicked(InputMethodManager imm) {
        if (imm != null) {
            imm.viewClicked(this);
        }
    }

    protected void deleteText_internal(int start, int end) {
        ((Editable) this.mText).delete(start, end);
    }

    protected void replaceText_internal(int start, int end, CharSequence text) {
        ((Editable) this.mText).replace(start, end, text);
    }

    protected void setSpan_internal(Object span, int start, int end, int flags) {
        ((Editable) this.mText).setSpan(span, start, end, flags);
    }

    protected void setCursorPosition_internal(int start, int end) {
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

    public TextSegmentIterator getIteratorForGranularity(int granularity) {
        switch (granularity) {
            case 4:
                Spannable text = (Spannable) getIterableTextForAccessibility();
                if (!(TextUtils.isEmpty(text) || getLayout() == null)) {
                    LineTextSegmentIterator iterator = LineTextSegmentIterator.getInstance();
                    iterator.initialize(text, getLayout());
                    return iterator;
                }
            case 16:
                if (!(TextUtils.isEmpty((Spannable) getIterableTextForAccessibility()) || getLayout() == null)) {
                    PageTextSegmentIterator iterator2 = PageTextSegmentIterator.getInstance();
                    iterator2.initialize(this);
                    return iterator2;
                }
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

    protected void encodeProperties(ViewHierarchyEncoder stream) {
        String str = null;
        super.encodeProperties(stream);
        TruncateAt ellipsize = getEllipsize();
        stream.addProperty("text:ellipsize", ellipsize == null ? null : ellipsize.name());
        stream.addProperty("text:textSize", getTextSize());
        stream.addProperty("text:scaledTextSize", getScaledTextSize());
        stream.addProperty("text:typefaceStyle", getTypefaceStyle());
        stream.addProperty("text:selectionStart", getSelectionStart());
        stream.addProperty("text:selectionEnd", getSelectionEnd());
        stream.addProperty("text:curTextColor", this.mCurTextColor);
        String str2 = "text:text";
        if (this.mText != null) {
            str = this.mText.toString();
        }
        stream.addProperty(str2, str);
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

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mTrySelectAllAndShowEditor && (this.mSelectAllAndShowEditorDone ^ 1) != 0) {
            selectAllAndShowEditor();
            this.mSelectAllAndShowEditorDone = true;
        }
    }

    public void trySelectAllAndShowEditor() {
        this.mTrySelectAllAndShowEditor = true;
    }

    protected void selectAllAndShowEditor() {
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
