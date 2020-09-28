package android.widget;

import android.Manifest;
import android.annotation.UnsupportedAppUsage;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.UndoManager;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.BaseCanvas;
import android.graphics.BlendMode;
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
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ParcelableParcel;
import android.os.Process;
import android.os.SystemClock;
import android.os.UserHandle;
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
import android.util.SparseArray;
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
import android.view.inspector.InspectionCompanion;
import android.view.inspector.IntFlagMapping;
import android.view.inspector.PropertyMapper;
import android.view.inspector.PropertyReader;
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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.IntFunction;
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
    private static final int CHANGE_WATCHER_PRIORITY = 100;
    static final boolean DEBUG_EXTRACT = false;
    private static final int DECIMAL = 4;
    private static final int DEFAULT_AUTO_SIZE_GRANULARITY_IN_PX = 1;
    private static final int DEFAULT_AUTO_SIZE_MAX_TEXT_SIZE_IN_SP = 112;
    private static final int DEFAULT_AUTO_SIZE_MIN_TEXT_SIZE_IN_SP = 12;
    private static final int DEFAULT_TYPEFACE = -1;
    private static final int DEVICE_PROVISIONED_NO = 1;
    private static final int DEVICE_PROVISIONED_UNKNOWN = 0;
    private static final int DEVICE_PROVISIONED_YES = 2;
    private static final int ELLIPSIZE_END = 3;
    private static final int ELLIPSIZE_MARQUEE = 4;
    private static final int ELLIPSIZE_MIDDLE = 2;
    private static final int ELLIPSIZE_NONE = 0;
    private static final int ELLIPSIZE_NOT_SET = -1;
    private static final int ELLIPSIZE_START = 1;
    private static final Spanned EMPTY_SPANNED = new SpannedString("");
    private static final int EMS = 1;
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
    private static final int KEY_DOWN_HANDLED_BY_KEY_LISTENER = 1;
    private static final int KEY_DOWN_HANDLED_BY_MOVEMENT_METHOD = 2;
    private static final int KEY_EVENT_HANDLED = -1;
    private static final int KEY_EVENT_NOT_HANDLED = 0;
    @UnsupportedAppUsage
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
    private static final SparseIntArray sAppearanceValues = new SparseIntArray();
    static long sLastCutCopyOrTextChangedTime;
    @UnsupportedAppUsage
    private boolean mAllowTransformationLengthChange;
    private int mAutoLinkMask;
    private float mAutoSizeMaxTextSizeInPx;
    private float mAutoSizeMinTextSizeInPx;
    private float mAutoSizeStepGranularityInPx;
    private int[] mAutoSizeTextSizesInPx;
    private int mAutoSizeTextType;
    @UnsupportedAppUsage
    private BoringLayout.Metrics mBoring;
    private int mBreakStrategy;
    @UnsupportedAppUsage
    private BufferType mBufferType;
    @UnsupportedAppUsage
    private ChangeWatcher mChangeWatcher;
    @UnsupportedAppUsage
    private CharWrapper mCharWrapper;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private int mCurHintTextColor;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    @ViewDebug.ExportedProperty(category = "text")
    private int mCurTextColor;
    private volatile Locale mCurrentSpellCheckerLocaleCache;
    private Drawable mCursorDrawable;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    int mCursorDrawableRes;
    private int mDeferScroll;
    @UnsupportedAppUsage
    private int mDesiredHeightAtMeasure;
    private int mDeviceProvisionedState;
    @UnsupportedAppUsage
    Drawables mDrawables;
    @UnsupportedAppUsage
    private Editable.Factory mEditableFactory;
    @UnsupportedAppUsage
    private Editor mEditor;
    private TextUtils.TruncateAt mEllipsize;
    private InputFilter[] mFilters;
    private boolean mFreezesText;
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = "text")
    private int mGravity;
    private boolean mHasPresetAutoSizeValues;
    @UnsupportedAppUsage
    int mHighlightColor;
    @UnsupportedAppUsage
    private final Paint mHighlightPaint;
    private Path mHighlightPath;
    @UnsupportedAppUsage
    private boolean mHighlightPathBogus;
    private CharSequence mHint;
    @UnsupportedAppUsage
    private BoringLayout.Metrics mHintBoring;
    @UnsupportedAppUsage
    private Layout mHintLayout;
    private ColorStateList mHintTextColor;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private boolean mHorizontallyScrolling;
    private int mHwCompoundPaddingLeft;
    private HwWidgetFactory.HwTextView mHwTextView;
    private int mHyphenationFrequency;
    @UnsupportedAppUsage
    private boolean mIncludePad;
    private int mJustificationMode;
    private int mLastLayoutDirection;
    private long mLastScroll;
    @UnsupportedAppUsage
    private Layout mLayout;
    private ColorStateList mLinkTextColor;
    private boolean mLinksClickable;
    private boolean mListenerChanged;
    @UnsupportedAppUsage(trackingBug = 123769451)
    private ArrayList<TextWatcher> mListeners;
    private boolean mLocalesChanged;
    @UnsupportedAppUsage(trackingBug = 124050217)
    private Marquee mMarquee;
    @UnsupportedAppUsage
    private int mMarqueeFadeMode;
    private int mMarqueeRepeatLimit;
    @UnsupportedAppUsage
    private int mMaxMode;
    @UnsupportedAppUsage
    private int mMaxWidth;
    @UnsupportedAppUsage
    private int mMaxWidthMode;
    @UnsupportedAppUsage
    private int mMaximum;
    @UnsupportedAppUsage
    private int mMinMode;
    @UnsupportedAppUsage
    private int mMinWidth;
    @UnsupportedAppUsage
    private int mMinWidthMode;
    @UnsupportedAppUsage
    private int mMinimum;
    private MovementMethod mMovement;
    private boolean mNeedsAutoSizeText;
    @UnsupportedAppUsage
    private int mOldMaxMode;
    @UnsupportedAppUsage
    private int mOldMaximum;
    private boolean mPreDrawListenerDetached;
    private boolean mPreDrawRegistered;
    private PrecomputedText mPrecomputed;
    private boolean mPreventDefaultMovement;
    @UnsupportedAppUsage
    private boolean mRestartMarquee;
    @UnsupportedAppUsage
    private BoringLayout mSavedHintLayout;
    @UnsupportedAppUsage
    private BoringLayout mSavedLayout;
    @UnsupportedAppUsage
    private Layout mSavedMarqueeModeLayout;
    private Scroller mScroller;
    protected boolean mSelectAllAndShowEditorDone;
    private int mShadowColor;
    @UnsupportedAppUsage
    private float mShadowDx;
    @UnsupportedAppUsage
    private float mShadowDy;
    @UnsupportedAppUsage
    private float mShadowRadius;
    @UnsupportedAppUsage
    private boolean mSingleLine;
    @UnsupportedAppUsage
    private float mSpacingAdd;
    @UnsupportedAppUsage
    private float mSpacingMult;
    private Spannable mSpannable;
    @UnsupportedAppUsage
    private Spannable.Factory mSpannableFactory;
    private Rect mTempRect;
    private TextPaint mTempTextPaint;
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = "text")
    private CharSequence mText;
    private TextClassificationContext mTextClassificationContext;
    private TextClassifier mTextClassificationSession;
    private TextClassifier mTextClassifier;
    private ColorStateList mTextColor;
    @UnsupportedAppUsage
    private TextDirectionHeuristic mTextDir;
    int mTextEditSuggestionContainerLayout;
    int mTextEditSuggestionHighlightStyle;
    int mTextEditSuggestionItemLayout;
    private int mTextId;
    private UserHandle mTextOperationUser;
    @UnsupportedAppUsage
    private final TextPaint mTextPaint;
    private Drawable mTextSelectHandle;
    private Drawable mTextSelectHandleLeft;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    int mTextSelectHandleLeftRes;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    int mTextSelectHandleRes;
    private Drawable mTextSelectHandleRight;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    int mTextSelectHandleRightRes;
    private boolean mTextSetFromXmlOrResourceId;
    private int mTextViewDirection;
    private TransformationMethod mTransformation;
    @UnsupportedAppUsage
    private CharSequence mTransformed;
    protected boolean mTrySelectAllAndShowEditor;
    boolean mUseFallbackLineSpacing;
    private final boolean mUseInternationalizedInput;
    @UnsupportedAppUsage
    private boolean mUserSetTextScaleX;
    private TextCopyFinishedListener textCopyFinishedListener;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AutoSizeTextType {
    }

    public enum BufferType {
        NORMAL,
        SPANNABLE,
        EDITABLE
    }

    public interface OnEditorActionListener {
        boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface XMLTypefaceAttr {
    }

    public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<TextView> {
        private int mAutoLinkId;
        private int mAutoSizeMaxTextSizeId;
        private int mAutoSizeMinTextSizeId;
        private int mAutoSizeStepGranularityId;
        private int mAutoSizeTextTypeId;
        private int mBreakStrategyId;
        private int mCursorVisibleId;
        private int mDrawableBlendModeId;
        private int mDrawablePaddingId;
        private int mDrawableTintId;
        private int mDrawableTintModeId;
        private int mElegantTextHeightId;
        private int mEllipsizeId;
        private int mFallbackLineSpacingId;
        private int mFirstBaselineToTopHeightId;
        private int mFontFeatureSettingsId;
        private int mFreezesTextId;
        private int mGravityId;
        private int mHintId;
        private int mHyphenationFrequencyId;
        private int mImeActionIdId;
        private int mImeActionLabelId;
        private int mImeOptionsId;
        private int mIncludeFontPaddingId;
        private int mInputTypeId;
        private int mJustificationModeId;
        private int mLastBaselineToBottomHeightId;
        private int mLetterSpacingId;
        private int mLineHeightId;
        private int mLineSpacingExtraId;
        private int mLineSpacingMultiplierId;
        private int mLinksClickableId;
        private int mMarqueeRepeatLimitId;
        private int mMaxEmsId;
        private int mMaxHeightId;
        private int mMaxLinesId;
        private int mMaxWidthId;
        private int mMinEmsId;
        private int mMinLinesId;
        private int mMinWidthId;
        private int mPrivateImeOptionsId;
        private boolean mPropertiesMapped = false;
        private int mScrollHorizontallyId;
        private int mShadowColorId;
        private int mShadowDxId;
        private int mShadowDyId;
        private int mShadowRadiusId;
        private int mSingleLineId;
        private int mTextAllCapsId;
        private int mTextColorHighlightId;
        private int mTextColorHintId;
        private int mTextColorId;
        private int mTextColorLinkId;
        private int mTextId;
        private int mTextIsSelectableId;
        private int mTextScaleXId;
        private int mTextSizeId;
        private int mTypefaceId;

        @Override // android.view.inspector.InspectionCompanion
        public void mapProperties(PropertyMapper propertyMapper) {
            IntFlagMapping autoLinkFlagMapping = new IntFlagMapping();
            autoLinkFlagMapping.add(2, 2, "email");
            autoLinkFlagMapping.add(8, 8, "map");
            autoLinkFlagMapping.add(4, 4, "phone");
            autoLinkFlagMapping.add(1, 1, "web");
            Objects.requireNonNull(autoLinkFlagMapping);
            this.mAutoLinkId = propertyMapper.mapIntFlag("autoLink", 16842928, new IntFunction() {
                /* class android.widget.$$Lambda$gFNlJIKfxqleu304aRWP5R5v1yY */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return IntFlagMapping.this.get(i);
                }
            });
            this.mAutoSizeMaxTextSizeId = propertyMapper.mapInt("autoSizeMaxTextSize", 16844102);
            this.mAutoSizeMinTextSizeId = propertyMapper.mapInt("autoSizeMinTextSize", 16844088);
            this.mAutoSizeStepGranularityId = propertyMapper.mapInt("autoSizeStepGranularity", 16844086);
            SparseArray<String> autoSizeTextTypeEnumMapping = new SparseArray<>();
            autoSizeTextTypeEnumMapping.put(0, "none");
            autoSizeTextTypeEnumMapping.put(1, "uniform");
            Objects.requireNonNull(autoSizeTextTypeEnumMapping);
            this.mAutoSizeTextTypeId = propertyMapper.mapIntEnum("autoSizeTextType", 16844085, new IntFunction() {
                /* class android.widget.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            SparseArray<String> breakStrategyEnumMapping = new SparseArray<>();
            breakStrategyEnumMapping.put(0, "simple");
            breakStrategyEnumMapping.put(1, "high_quality");
            breakStrategyEnumMapping.put(2, "balanced");
            Objects.requireNonNull(breakStrategyEnumMapping);
            this.mBreakStrategyId = propertyMapper.mapIntEnum("breakStrategy", 16843997, new IntFunction() {
                /* class android.widget.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mCursorVisibleId = propertyMapper.mapBoolean("cursorVisible", 16843090);
            this.mDrawableBlendModeId = propertyMapper.mapObject("drawableBlendMode", 80);
            this.mDrawablePaddingId = propertyMapper.mapInt("drawablePadding", 16843121);
            this.mDrawableTintId = propertyMapper.mapObject("drawableTint", 16843990);
            this.mDrawableTintModeId = propertyMapper.mapObject("drawableTintMode", 16843991);
            this.mElegantTextHeightId = propertyMapper.mapBoolean("elegantTextHeight", 16843869);
            this.mEllipsizeId = propertyMapper.mapObject("ellipsize", 16842923);
            this.mFallbackLineSpacingId = propertyMapper.mapBoolean("fallbackLineSpacing", 16844155);
            this.mFirstBaselineToTopHeightId = propertyMapper.mapInt("firstBaselineToTopHeight", 16844157);
            this.mFontFeatureSettingsId = propertyMapper.mapObject("fontFeatureSettings", 16843959);
            this.mFreezesTextId = propertyMapper.mapBoolean("freezesText", 16843116);
            this.mGravityId = propertyMapper.mapGravity("gravity", 16842927);
            this.mHintId = propertyMapper.mapObject("hint", 16843088);
            SparseArray<String> hyphenationFrequencyEnumMapping = new SparseArray<>();
            hyphenationFrequencyEnumMapping.put(0, "none");
            hyphenationFrequencyEnumMapping.put(1, "normal");
            hyphenationFrequencyEnumMapping.put(2, "full");
            Objects.requireNonNull(hyphenationFrequencyEnumMapping);
            this.mHyphenationFrequencyId = propertyMapper.mapIntEnum("hyphenationFrequency", 16843998, new IntFunction() {
                /* class android.widget.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mImeActionIdId = propertyMapper.mapInt("imeActionId", 16843366);
            this.mImeActionLabelId = propertyMapper.mapObject("imeActionLabel", 16843365);
            IntFlagMapping imeOptionsFlagMapping = new IntFlagMapping();
            imeOptionsFlagMapping.add(255, 6, "actionDone");
            imeOptionsFlagMapping.add(255, 2, "actionGo");
            imeOptionsFlagMapping.add(255, 5, "actionNext");
            imeOptionsFlagMapping.add(255, 1, "actionNone");
            imeOptionsFlagMapping.add(255, 7, "actionPrevious");
            imeOptionsFlagMapping.add(255, 3, "actionSearch");
            imeOptionsFlagMapping.add(255, 4, "actionSend");
            imeOptionsFlagMapping.add(255, 0, "actionUnspecified");
            imeOptionsFlagMapping.add(Integer.MIN_VALUE, Integer.MIN_VALUE, "flagForceAscii");
            imeOptionsFlagMapping.add(134217728, 134217728, "flagNavigateNext");
            imeOptionsFlagMapping.add(67108864, 67108864, "flagNavigatePrevious");
            imeOptionsFlagMapping.add(536870912, 536870912, "flagNoAccessoryAction");
            imeOptionsFlagMapping.add(1073741824, 1073741824, "flagNoEnterAction");
            imeOptionsFlagMapping.add(268435456, 268435456, "flagNoExtractUi");
            imeOptionsFlagMapping.add(33554432, 33554432, "flagNoFullscreen");
            imeOptionsFlagMapping.add(16777216, 16777216, "flagNoPersonalizedLearning");
            imeOptionsFlagMapping.add(-1, 0, "normal");
            Objects.requireNonNull(imeOptionsFlagMapping);
            this.mImeOptionsId = propertyMapper.mapIntFlag("imeOptions", 16843364, new IntFunction() {
                /* class android.widget.$$Lambda$gFNlJIKfxqleu304aRWP5R5v1yY */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return IntFlagMapping.this.get(i);
                }
            });
            this.mIncludeFontPaddingId = propertyMapper.mapBoolean("includeFontPadding", 16843103);
            IntFlagMapping inputTypeFlagMapping = new IntFlagMapping();
            inputTypeFlagMapping.add(4095, 20, "date");
            inputTypeFlagMapping.add(4095, 4, TextClassifier.TYPE_DATE_TIME);
            inputTypeFlagMapping.add(-1, 0, "none");
            inputTypeFlagMapping.add(4095, 2, "number");
            inputTypeFlagMapping.add(16773135, 8194, "numberDecimal");
            inputTypeFlagMapping.add(4095, 18, "numberPassword");
            inputTypeFlagMapping.add(16773135, 4098, "numberSigned");
            inputTypeFlagMapping.add(4095, 3, "phone");
            inputTypeFlagMapping.add(4095, 1, "text");
            inputTypeFlagMapping.add(16773135, 65537, "textAutoComplete");
            inputTypeFlagMapping.add(16773135, 32769, "textAutoCorrect");
            inputTypeFlagMapping.add(16773135, 4097, "textCapCharacters");
            inputTypeFlagMapping.add(16773135, 16385, "textCapSentences");
            inputTypeFlagMapping.add(16773135, 8193, "textCapWords");
            inputTypeFlagMapping.add(4095, 33, "textEmailAddress");
            inputTypeFlagMapping.add(4095, 49, "textEmailSubject");
            inputTypeFlagMapping.add(4095, 177, "textFilter");
            inputTypeFlagMapping.add(16773135, 262145, "textImeMultiLine");
            inputTypeFlagMapping.add(4095, 81, "textLongMessage");
            inputTypeFlagMapping.add(16773135, 131073, "textMultiLine");
            inputTypeFlagMapping.add(16773135, ConnectivityManager.CALLBACK_PRECHECK, "textNoSuggestions");
            inputTypeFlagMapping.add(4095, 129, "textPassword");
            inputTypeFlagMapping.add(4095, 97, "textPersonName");
            inputTypeFlagMapping.add(4095, 193, "textPhonetic");
            inputTypeFlagMapping.add(4095, 113, "textPostalAddress");
            inputTypeFlagMapping.add(4095, 65, "textShortMessage");
            inputTypeFlagMapping.add(4095, 17, "textUri");
            inputTypeFlagMapping.add(4095, 145, "textVisiblePassword");
            inputTypeFlagMapping.add(4095, 161, "textWebEditText");
            inputTypeFlagMapping.add(4095, 209, "textWebEmailAddress");
            inputTypeFlagMapping.add(4095, 225, "textWebPassword");
            inputTypeFlagMapping.add(4095, 36, "time");
            Objects.requireNonNull(inputTypeFlagMapping);
            this.mInputTypeId = propertyMapper.mapIntFlag("inputType", 16843296, new IntFunction() {
                /* class android.widget.$$Lambda$gFNlJIKfxqleu304aRWP5R5v1yY */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return IntFlagMapping.this.get(i);
                }
            });
            SparseArray<String> justificationModeEnumMapping = new SparseArray<>();
            justificationModeEnumMapping.put(0, "none");
            justificationModeEnumMapping.put(1, "inter_word");
            Objects.requireNonNull(justificationModeEnumMapping);
            this.mJustificationModeId = propertyMapper.mapIntEnum("justificationMode", 16844135, new IntFunction() {
                /* class android.widget.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mLastBaselineToBottomHeightId = propertyMapper.mapInt("lastBaselineToBottomHeight", 16844158);
            this.mLetterSpacingId = propertyMapper.mapFloat("letterSpacing", 16843958);
            this.mLineHeightId = propertyMapper.mapInt("lineHeight", 16844159);
            this.mLineSpacingExtraId = propertyMapper.mapFloat("lineSpacingExtra", 16843287);
            this.mLineSpacingMultiplierId = propertyMapper.mapFloat("lineSpacingMultiplier", 16843288);
            this.mLinksClickableId = propertyMapper.mapBoolean("linksClickable", 16842929);
            this.mMarqueeRepeatLimitId = propertyMapper.mapInt("marqueeRepeatLimit", 16843293);
            this.mMaxEmsId = propertyMapper.mapInt("maxEms", 16843095);
            this.mMaxHeightId = propertyMapper.mapInt("maxHeight", 16843040);
            this.mMaxLinesId = propertyMapper.mapInt("maxLines", 16843091);
            this.mMaxWidthId = propertyMapper.mapInt("maxWidth", 16843039);
            this.mMinEmsId = propertyMapper.mapInt("minEms", 16843098);
            this.mMinLinesId = propertyMapper.mapInt("minLines", 16843094);
            this.mMinWidthId = propertyMapper.mapInt("minWidth", 16843071);
            this.mPrivateImeOptionsId = propertyMapper.mapObject("privateImeOptions", 16843299);
            this.mScrollHorizontallyId = propertyMapper.mapBoolean("scrollHorizontally", 16843099);
            this.mShadowColorId = propertyMapper.mapColor("shadowColor", 16843105);
            this.mShadowDxId = propertyMapper.mapFloat("shadowDx", 16843106);
            this.mShadowDyId = propertyMapper.mapFloat("shadowDy", 16843107);
            this.mShadowRadiusId = propertyMapper.mapFloat("shadowRadius", 16843108);
            this.mSingleLineId = propertyMapper.mapBoolean("singleLine", 16843101);
            this.mTextId = propertyMapper.mapObject("text", 16843087);
            this.mTextAllCapsId = propertyMapper.mapBoolean("textAllCaps", 16843660);
            this.mTextColorId = propertyMapper.mapObject("textColor", 16842904);
            this.mTextColorHighlightId = propertyMapper.mapColor("textColorHighlight", 16842905);
            this.mTextColorHintId = propertyMapper.mapObject("textColorHint", 16842906);
            this.mTextColorLinkId = propertyMapper.mapObject("textColorLink", 16842907);
            this.mTextIsSelectableId = propertyMapper.mapBoolean("textIsSelectable", 16843542);
            this.mTextScaleXId = propertyMapper.mapFloat("textScaleX", 16843089);
            this.mTextSizeId = propertyMapper.mapFloat("textSize", 16842901);
            this.mTypefaceId = propertyMapper.mapObject("typeface", 16842902);
            this.mPropertiesMapped = true;
        }

        public void readProperties(TextView node, PropertyReader propertyReader) {
            if (this.mPropertiesMapped) {
                propertyReader.readIntFlag(this.mAutoLinkId, node.getAutoLinkMask());
                propertyReader.readInt(this.mAutoSizeMaxTextSizeId, node.getAutoSizeMaxTextSize());
                propertyReader.readInt(this.mAutoSizeMinTextSizeId, node.getAutoSizeMinTextSize());
                propertyReader.readInt(this.mAutoSizeStepGranularityId, node.getAutoSizeStepGranularity());
                propertyReader.readIntEnum(this.mAutoSizeTextTypeId, node.getAutoSizeTextType());
                propertyReader.readIntEnum(this.mBreakStrategyId, node.getBreakStrategy());
                propertyReader.readBoolean(this.mCursorVisibleId, node.isCursorVisible());
                propertyReader.readObject(this.mDrawableBlendModeId, node.getCompoundDrawableTintBlendMode());
                propertyReader.readInt(this.mDrawablePaddingId, node.getCompoundDrawablePadding());
                propertyReader.readObject(this.mDrawableTintId, node.getCompoundDrawableTintList());
                propertyReader.readObject(this.mDrawableTintModeId, node.getCompoundDrawableTintMode());
                propertyReader.readBoolean(this.mElegantTextHeightId, node.isElegantTextHeight());
                propertyReader.readObject(this.mEllipsizeId, node.getEllipsize());
                propertyReader.readBoolean(this.mFallbackLineSpacingId, node.isFallbackLineSpacing());
                propertyReader.readInt(this.mFirstBaselineToTopHeightId, node.getFirstBaselineToTopHeight());
                propertyReader.readObject(this.mFontFeatureSettingsId, node.getFontFeatureSettings());
                propertyReader.readBoolean(this.mFreezesTextId, node.getFreezesText());
                propertyReader.readGravity(this.mGravityId, node.getGravity());
                propertyReader.readObject(this.mHintId, node.getHint());
                propertyReader.readIntEnum(this.mHyphenationFrequencyId, node.getHyphenationFrequency());
                propertyReader.readInt(this.mImeActionIdId, node.getImeActionId());
                propertyReader.readObject(this.mImeActionLabelId, node.getImeActionLabel());
                propertyReader.readIntFlag(this.mImeOptionsId, node.getImeOptions());
                propertyReader.readBoolean(this.mIncludeFontPaddingId, node.getIncludeFontPadding());
                propertyReader.readIntFlag(this.mInputTypeId, node.getInputType());
                propertyReader.readIntEnum(this.mJustificationModeId, node.getJustificationMode());
                propertyReader.readInt(this.mLastBaselineToBottomHeightId, node.getLastBaselineToBottomHeight());
                propertyReader.readFloat(this.mLetterSpacingId, node.getLetterSpacing());
                propertyReader.readInt(this.mLineHeightId, node.getLineHeight());
                propertyReader.readFloat(this.mLineSpacingExtraId, node.getLineSpacingExtra());
                propertyReader.readFloat(this.mLineSpacingMultiplierId, node.getLineSpacingMultiplier());
                propertyReader.readBoolean(this.mLinksClickableId, node.getLinksClickable());
                propertyReader.readInt(this.mMarqueeRepeatLimitId, node.getMarqueeRepeatLimit());
                propertyReader.readInt(this.mMaxEmsId, node.getMaxEms());
                propertyReader.readInt(this.mMaxHeightId, node.getMaxHeight());
                propertyReader.readInt(this.mMaxLinesId, node.getMaxLines());
                propertyReader.readInt(this.mMaxWidthId, node.getMaxWidth());
                propertyReader.readInt(this.mMinEmsId, node.getMinEms());
                propertyReader.readInt(this.mMinLinesId, node.getMinLines());
                propertyReader.readInt(this.mMinWidthId, node.getMinWidth());
                propertyReader.readObject(this.mPrivateImeOptionsId, node.getPrivateImeOptions());
                propertyReader.readBoolean(this.mScrollHorizontallyId, node.isHorizontallyScrollable());
                propertyReader.readColor(this.mShadowColorId, node.getShadowColor());
                propertyReader.readFloat(this.mShadowDxId, node.getShadowDx());
                propertyReader.readFloat(this.mShadowDyId, node.getShadowDy());
                propertyReader.readFloat(this.mShadowRadiusId, node.getShadowRadius());
                propertyReader.readBoolean(this.mSingleLineId, node.isSingleLine());
                propertyReader.readObject(this.mTextId, node.getText());
                propertyReader.readBoolean(this.mTextAllCapsId, node.isAllCaps());
                propertyReader.readObject(this.mTextColorId, node.getTextColors());
                propertyReader.readColor(this.mTextColorHighlightId, node.getHighlightColor());
                propertyReader.readObject(this.mTextColorHintId, node.getHintTextColors());
                propertyReader.readObject(this.mTextColorLinkId, node.getLinkTextColors());
                propertyReader.readBoolean(this.mTextIsSelectableId, node.isTextSelectable());
                propertyReader.readFloat(this.mTextScaleXId, node.getTextScaleX());
                propertyReader.readFloat(this.mTextSizeId, node.getTextSize());
                propertyReader.readObject(this.mTypefaceId, node.getTypeface());
                return;
            }
            throw new InspectionCompanion.UninitializedPropertyMapException();
        }
    }

    static {
        sAppearanceValues.put(6, 4);
        sAppearanceValues.put(5, 3);
        sAppearanceValues.put(7, 5);
        sAppearanceValues.put(8, 6);
        sAppearanceValues.put(2, 0);
        sAppearanceValues.put(96, 19);
        sAppearanceValues.put(3, 1);
        sAppearanceValues.put(75, 12);
        sAppearanceValues.put(4, 2);
        sAppearanceValues.put(95, 18);
        sAppearanceValues.put(72, 11);
        sAppearanceValues.put(36, 7);
        sAppearanceValues.put(37, 8);
        sAppearanceValues.put(38, 9);
        sAppearanceValues.put(39, 10);
        sAppearanceValues.put(76, 13);
        sAppearanceValues.put(91, 17);
        sAppearanceValues.put(77, 14);
        sAppearanceValues.put(78, 15);
        sAppearanceValues.put(90, 16);
    }

    /* access modifiers changed from: package-private */
    public static class Drawables {
        static final int BOTTOM = 3;
        static final int DRAWABLE_LEFT = 1;
        static final int DRAWABLE_NONE = -1;
        static final int DRAWABLE_RIGHT = 0;
        static final int LEFT = 0;
        static final int RIGHT = 2;
        static final int TOP = 1;
        BlendMode mBlendMode;
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

        public Drawables(Context context) {
            this.mIsRtlCompatibilityMode = context.getApplicationInfo().targetSdkVersion < 17 || !context.getApplicationInfo().hasRtlSupport();
            this.mOverride = false;
        }

        public boolean hasMetadata() {
            return this.mDrawablePadding != 0 || this.mHasTintMode || this.mHasTint;
        }

        public boolean resolveWithLayoutDirection(int layoutDirection) {
            Drawable[] drawableArr = this.mShowing;
            Drawable previousLeft = drawableArr[0];
            Drawable previousRight = drawableArr[2];
            drawableArr[0] = this.mDrawableLeftInitial;
            drawableArr[2] = this.mDrawableRightInitial;
            if (this.mIsRtlCompatibilityMode) {
                Drawable drawable = this.mDrawableStart;
                if (drawable != null && drawableArr[0] == null) {
                    drawableArr[0] = drawable;
                    this.mDrawableSizeLeft = this.mDrawableSizeStart;
                    this.mDrawableHeightLeft = this.mDrawableHeightStart;
                }
                Drawable drawable2 = this.mDrawableEnd;
                if (drawable2 != null) {
                    Drawable[] drawableArr2 = this.mShowing;
                    if (drawableArr2[2] == null) {
                        drawableArr2[2] = drawable2;
                        this.mDrawableSizeRight = this.mDrawableSizeEnd;
                        this.mDrawableHeightRight = this.mDrawableHeightEnd;
                    }
                }
            } else if (layoutDirection != 1) {
                if (this.mOverride) {
                    drawableArr[0] = this.mDrawableStart;
                    this.mDrawableSizeLeft = this.mDrawableSizeStart;
                    this.mDrawableHeightLeft = this.mDrawableHeightStart;
                    drawableArr[2] = this.mDrawableEnd;
                    this.mDrawableSizeRight = this.mDrawableSizeEnd;
                    this.mDrawableHeightRight = this.mDrawableHeightEnd;
                }
            } else if (this.mOverride) {
                drawableArr[2] = this.mDrawableStart;
                this.mDrawableSizeRight = this.mDrawableSizeStart;
                this.mDrawableHeightRight = this.mDrawableHeightStart;
                drawableArr[0] = this.mDrawableEnd;
                this.mDrawableSizeLeft = this.mDrawableSizeEnd;
                this.mDrawableHeightLeft = this.mDrawableHeightEnd;
            }
            applyErrorDrawableIfNeeded(layoutDirection);
            Drawable[] drawableArr3 = this.mShowing;
            if (drawableArr3[0] == previousLeft && drawableArr3[2] == previousRight) {
                return false;
            }
            return true;
        }

        public void setErrorDrawable(Drawable dr, TextView tv) {
            Drawable drawable = this.mDrawableError;
            if (!(drawable == dr || drawable == null)) {
                drawable.setCallback(null);
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
            int i = this.mDrawableSaved;
            if (i == 0) {
                this.mShowing[2] = this.mDrawableTemp;
                this.mDrawableSizeRight = this.mDrawableSizeTemp;
                this.mDrawableHeightRight = this.mDrawableHeightTemp;
            } else if (i == 1) {
                this.mShowing[0] = this.mDrawableTemp;
                this.mDrawableSizeLeft = this.mDrawableSizeTemp;
                this.mDrawableHeightLeft = this.mDrawableHeightTemp;
            }
            Drawable drawable = this.mDrawableError;
            if (drawable == null) {
                return;
            }
            if (layoutDirection != 1) {
                this.mDrawableSaved = 0;
                Drawable[] drawableArr = this.mShowing;
                this.mDrawableTemp = drawableArr[2];
                this.mDrawableSizeTemp = this.mDrawableSizeRight;
                this.mDrawableHeightTemp = this.mDrawableHeightRight;
                drawableArr[2] = drawable;
                this.mDrawableSizeRight = this.mDrawableSizeError;
                this.mDrawableHeightRight = this.mDrawableHeightError;
                return;
            }
            this.mDrawableSaved = 1;
            Drawable[] drawableArr2 = this.mShowing;
            this.mDrawableTemp = drawableArr2[0];
            this.mDrawableSizeTemp = this.mDrawableSizeLeft;
            this.mDrawableHeightTemp = this.mDrawableHeightLeft;
            drawableArr2[0] = drawable;
            this.mDrawableSizeLeft = this.mDrawableSizeError;
            this.mDrawableHeightLeft = this.mDrawableHeightError;
        }
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

    /* JADX INFO: Multiple debug info for r3v12 int: [D('bufferType' android.widget.TextView$BufferType), D('targetSdkVersion' int)] */
    /* JADX INFO: Multiple debug info for r7v8 android.graphics.drawable.Drawable: [D('numeric' int), D('drawableRight' android.graphics.drawable.Drawable)] */
    /* JADX INFO: Multiple debug info for r2v33 'drawablePadding'  int: [D('ellipsize' int), D('drawablePadding' int)] */
    /* JADX INFO: Multiple debug info for r1v45 'autotext'  boolean: [D('autotext' boolean), D('webPasswordInputType' boolean)] */
    /* JADX INFO: Multiple debug info for r4v12 'numberPasswordInputType'  boolean: [D('autocap' int), D('numberPasswordInputType' boolean)] */
    /* JADX WARNING: Code restructure failed: missing block: B:316:0x11ee, code lost:
        if ((r1.mInputType & 4095) == 129) goto L_0x11f9;
     */
    /* JADX WARNING: Removed duplicated region for block: B:263:0x10f8  */
    /* JADX WARNING: Removed duplicated region for block: B:264:0x1106  */
    /* JADX WARNING: Removed duplicated region for block: B:266:0x1112  */
    /* JADX WARNING: Removed duplicated region for block: B:269:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:276:0x1133  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x113c  */
    /* JADX WARNING: Removed duplicated region for block: B:279:0x1146  */
    /* JADX WARNING: Removed duplicated region for block: B:281:0x114b  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x1197  */
    /* JADX WARNING: Removed duplicated region for block: B:305:0x11cc  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x11e0  */
    /* JADX WARNING: Removed duplicated region for block: B:319:0x11f5  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x11fc  */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x1204  */
    /* JADX WARNING: Removed duplicated region for block: B:326:0x120d  */
    /* JADX WARNING: Removed duplicated region for block: B:329:0x1218  */
    /* JADX WARNING: Removed duplicated region for block: B:330:0x122c  */
    /* JADX WARNING: Removed duplicated region for block: B:333:0x123e  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x1243  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x124b  */
    /* JADX WARNING: Removed duplicated region for block: B:339:0x124f  */
    /* JADX WARNING: Removed duplicated region for block: B:342:0x1256  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x126f  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x127b  */
    /* JADX WARNING: Removed duplicated region for block: B:355:0x1289  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x12ad  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x130f  */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x131c  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x1325  */
    /* JADX WARNING: Removed duplicated region for block: B:391:0x132a  */
    /* JADX WARNING: Removed duplicated region for block: B:394:0x1331  */
    /* JADX WARNING: Removed duplicated region for block: B:412:0x137d  */
    /* JADX WARNING: Removed duplicated region for block: B:414:0x1382  */
    /* JADX WARNING: Removed duplicated region for block: B:415:0x1388  */
    /* JADX WARNING: Removed duplicated region for block: B:418:0x138e  */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1395  */
    /* JADX WARNING: Removed duplicated region for block: B:462:? A[RETURN, SYNTHETIC] */
    public TextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray appearance;
        boolean singleLine;
        BufferType bufferType;
        Editor editor;
        boolean passwordInputType;
        boolean numberPasswordInputType;
        boolean webPasswordInputType;
        int ellipsize;
        boolean isPassword;
        boolean isMonospaceEnforced;
        TextAppearanceAttributes attributes;
        CharSequence charSequence;
        int n;
        int i;
        int focusable;
        Editor editor2;
        int i2;
        int i3;
        float autoSizeMinTextSizeInPx;
        float autoSizeMaxTextSizeInPx;
        float autoSizeStepGranularityInPx;
        Drawable drawableRight;
        int i4;
        boolean z;
        int i5;
        TextKeyListener.Capitalize cap;
        int i6;
        TextAppearanceAttributes attributes2;
        String str;
        String str2;
        Locale loc;
        String str3;
        int ellipsize2;
        int drawablePadding;
        boolean singleLine2;
        int firstBaselineToTopHeight;
        int buffertype;
        boolean editable;
        boolean password;
        boolean selectallonfocus;
        int numeric;
        boolean phone;
        int i7;
        boolean autotext;
        this.mHwTextView = null;
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
        this.mHwTextView = HwWidgetFactory.getHwTextView(context, this, attrs);
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
        TextAppearanceAttributes attributes3 = new TextAppearanceAttributes();
        attributes3.mTextColor = ColorStateList.valueOf(-16777216);
        attributes3.mTextSize = 15;
        this.mBreakStrategy = 0;
        this.mHyphenationFrequency = 0;
        this.mJustificationMode = 0;
        Resources.Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(attrs, R.styleable.TextViewAppearance, defStyleAttr, defStyleRes);
        TextAppearanceAttributes attributes4 = attributes3;
        saveAttributeDataForStyleable(context, R.styleable.TextViewAppearance, attrs, a, defStyleAttr, defStyleRes);
        int ap = a.getResourceId(0, -1);
        a.recycle();
        if (ap != -1) {
            TypedArray appearance2 = theme.obtainStyledAttributes(ap, R.styleable.TextAppearance);
            saveAttributeDataForStyleable(context, R.styleable.TextAppearance, null, appearance2, 0, ap);
            appearance = appearance2;
        } else {
            appearance = null;
        }
        if (appearance != null) {
            readTextAppearance(context, appearance, attributes4, false);
            attributes4.mFontFamilyExplicit = false;
            appearance.recycle();
        }
        boolean editable2 = getDefaultEditable();
        CharSequence inputMethod = null;
        CharSequence digits = null;
        float autoSizeMinTextSizeInPx2 = -1.0f;
        float autoSizeMaxTextSizeInPx2 = -1.0f;
        float autoSizeStepGranularityInPx2 = -1.0f;
        TypedArray a2 = theme.obtainStyledAttributes(attrs, R.styleable.TextView, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.TextView, attrs, a2, defStyleAttr, defStyleRes);
        readTextAppearance(context, a2, attributes4, true);
        int n2 = a2.getIndexCount();
        Locale loc2 = Locale.getDefault();
        String str4 = "my";
        String str5 = "MM";
        String str6 = "si";
        if (str4.equals(loc2.getLanguage()) && str5.equals(loc2.getCountry())) {
            this.mSpacingMult = MSPACINGMULTFORMY;
        } else if (str6.equals(loc2.getLanguage())) {
            this.mSpacingMult = MSPACINGMULTFORSI;
        }
        boolean editable3 = editable2;
        int numeric2 = 0;
        boolean phone2 = false;
        boolean autotext2 = false;
        int autocap = -1;
        int buffertype2 = 0;
        boolean selectallonfocus2 = false;
        Drawable drawableLeft = null;
        Drawable drawableTop = null;
        Drawable drawableRight2 = null;
        Drawable drawableBottom = null;
        Drawable drawableStart = null;
        Drawable drawableEnd = null;
        ColorStateList drawableTint = null;
        BlendMode drawableTintMode = null;
        int drawablePadding2 = 0;
        int ellipsize3 = -1;
        boolean singleLine3 = false;
        int maxlength = -1;
        CharSequence text = "";
        CharSequence hint = null;
        boolean password2 = false;
        int inputType = 0;
        int lastBaselineToBottomHeight = -1;
        int lineHeight = -1;
        boolean textIsSetFromXml = false;
        int i8 = 0;
        int firstBaselineToTopHeight2 = -1;
        while (i8 < n2) {
            int attr = a2.getIndex(i8);
            if (attr == 0) {
                str3 = str6;
                loc = loc2;
                str2 = str4;
                str = str5;
                firstBaselineToTopHeight = firstBaselineToTopHeight2;
                attributes2 = attributes4;
                ellipsize2 = ellipsize3;
                buffertype = buffertype2;
                password = password2;
                singleLine2 = singleLine3;
                selectallonfocus = selectallonfocus2;
                numeric = numeric2;
                phone = phone2;
                i7 = autocap;
                autotext = autotext2;
                editable = editable3;
                drawablePadding = drawablePadding2;
                setEnabled(a2.getBoolean(attr, isEnabled()));
            } else if (attr == 67) {
                str3 = str6;
                loc = loc2;
                str2 = str4;
                str = str5;
                firstBaselineToTopHeight = firstBaselineToTopHeight2;
                attributes2 = attributes4;
                ellipsize2 = ellipsize3;
                buffertype = buffertype2;
                password = password2;
                singleLine2 = singleLine3;
                selectallonfocus = selectallonfocus2;
                numeric = numeric2;
                phone = phone2;
                i7 = autocap;
                autotext = autotext2;
                editable = editable3;
                drawablePadding = drawablePadding2;
                setTextIsSelectable(a2.getBoolean(attr, false));
            } else if (attr == 70) {
                str3 = str6;
                loc = loc2;
                str2 = str4;
                str = str5;
                firstBaselineToTopHeight = firstBaselineToTopHeight2;
                attributes2 = attributes4;
                ellipsize2 = ellipsize3;
                buffertype = buffertype2;
                password = password2;
                singleLine2 = singleLine3;
                selectallonfocus = selectallonfocus2;
                numeric = numeric2;
                phone = phone2;
                i7 = autocap;
                autotext = autotext2;
                editable = editable3;
                drawablePadding = drawablePadding2;
                this.mCursorDrawableRes = a2.getResourceId(attr, 0);
            } else if (attr != 71) {
                if (attr == 73) {
                    str3 = str6;
                    loc = loc2;
                    str2 = str4;
                    str = str5;
                    attributes2 = attributes4;
                    drawableStart = a2.getDrawable(attr);
                    firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                } else if (attr == 74) {
                    str3 = str6;
                    loc = loc2;
                    str2 = str4;
                    str = str5;
                    attributes2 = attributes4;
                    drawableEnd = a2.getDrawable(attr);
                    firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                } else if (attr == 97) {
                    str3 = str6;
                    loc = loc2;
                    str2 = str4;
                    str = str5;
                    firstBaselineToTopHeight = firstBaselineToTopHeight2;
                    attributes2 = attributes4;
                    ellipsize2 = ellipsize3;
                    buffertype = buffertype2;
                    password = password2;
                    singleLine2 = singleLine3;
                    selectallonfocus = selectallonfocus2;
                    numeric = numeric2;
                    phone = phone2;
                    i7 = autocap;
                    autotext = autotext2;
                    editable = editable3;
                    drawablePadding = drawablePadding2;
                    this.mTextEditSuggestionContainerLayout = a2.getResourceId(attr, 0);
                } else if (attr != 98) {
                    switch (attr) {
                        case 9:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            attributes2 = attributes4;
                            ellipsize3 = a2.getInt(attr, ellipsize3);
                            firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                            break;
                        case 10:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            buffertype = buffertype2;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            drawablePadding = drawablePadding2;
                            setGravity(a2.getInt(attr, -1));
                            ellipsize2 = ellipsize3;
                            break;
                        case 11:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            buffertype = buffertype2;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            drawablePadding = drawablePadding2;
                            this.mAutoLinkMask = a2.getInt(attr, 0);
                            ellipsize2 = ellipsize3;
                            break;
                        case 12:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            buffertype = buffertype2;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            drawablePadding = drawablePadding2;
                            this.mLinksClickable = a2.getBoolean(attr, true);
                            ellipsize2 = ellipsize3;
                            break;
                        case 13:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            buffertype = buffertype2;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            drawablePadding = drawablePadding2;
                            setMaxWidth(a2.getDimensionPixelSize(attr, -1));
                            ellipsize2 = ellipsize3;
                            break;
                        case 14:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            buffertype = buffertype2;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            drawablePadding = drawablePadding2;
                            setMaxHeight(a2.getDimensionPixelSize(attr, -1));
                            ellipsize2 = ellipsize3;
                            break;
                        case 15:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            buffertype = buffertype2;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            drawablePadding = drawablePadding2;
                            setMinWidth(a2.getDimensionPixelSize(attr, -1));
                            ellipsize2 = ellipsize3;
                            break;
                        case 16:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            buffertype = buffertype2;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            drawablePadding = drawablePadding2;
                            setMinHeight(a2.getDimensionPixelSize(attr, -1));
                            ellipsize2 = ellipsize3;
                            break;
                        case 17:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            attributes2 = attributes4;
                            buffertype2 = a2.getInt(attr, buffertype2);
                            firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                            break;
                        case 18:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            attributes2 = attributes4;
                            textIsSetFromXml = true;
                            this.mTextId = a2.getResourceId(attr, 0);
                            text = a2.getText(attr);
                            firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                            break;
                        case 19:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            attributes2 = attributes4;
                            hint = a2.getText(attr);
                            firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                            break;
                        case 20:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            setTextScaleX(a2.getFloat(attr, 1.0f));
                            drawablePadding = drawablePadding2;
                            ellipsize2 = ellipsize3;
                            buffertype = buffertype2;
                            break;
                        case 21:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            if (a2.getBoolean(attr, true)) {
                                drawablePadding = drawablePadding2;
                                ellipsize2 = ellipsize3;
                                buffertype = buffertype2;
                                break;
                            } else {
                                setCursorVisible(false);
                                drawablePadding = drawablePadding2;
                                ellipsize2 = ellipsize3;
                                buffertype = buffertype2;
                                break;
                            }
                        case 22:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            setMaxLines(a2.getInt(attr, -1));
                            drawablePadding = drawablePadding2;
                            ellipsize2 = ellipsize3;
                            buffertype = buffertype2;
                            break;
                        case 23:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            setLines(a2.getInt(attr, -1));
                            drawablePadding = drawablePadding2;
                            ellipsize2 = ellipsize3;
                            buffertype = buffertype2;
                            break;
                        case 24:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            setHeight(a2.getDimensionPixelSize(attr, -1));
                            drawablePadding = drawablePadding2;
                            ellipsize2 = ellipsize3;
                            buffertype = buffertype2;
                            break;
                        case 25:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            setMinLines(a2.getInt(attr, -1));
                            drawablePadding = drawablePadding2;
                            ellipsize2 = ellipsize3;
                            buffertype = buffertype2;
                            break;
                        case 26:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            setMaxEms(a2.getInt(attr, -1));
                            drawablePadding = drawablePadding2;
                            ellipsize2 = ellipsize3;
                            buffertype = buffertype2;
                            break;
                        case 27:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            setEms(a2.getInt(attr, -1));
                            drawablePadding = drawablePadding2;
                            ellipsize2 = ellipsize3;
                            buffertype = buffertype2;
                            break;
                        case 28:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            setWidth(a2.getDimensionPixelSize(attr, -1));
                            drawablePadding = drawablePadding2;
                            ellipsize2 = ellipsize3;
                            buffertype = buffertype2;
                            break;
                        case 29:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            attributes2 = attributes4;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            setMinEms(a2.getInt(attr, -1));
                            drawablePadding = drawablePadding2;
                            ellipsize2 = ellipsize3;
                            buffertype = buffertype2;
                            break;
                        case 30:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            password = password2;
                            singleLine2 = singleLine3;
                            selectallonfocus = selectallonfocus2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            attributes2 = attributes4;
                            if (!a2.getBoolean(attr, false)) {
                                drawablePadding = drawablePadding2;
                                ellipsize2 = ellipsize3;
                                buffertype = buffertype2;
                                break;
                            } else {
                                setHorizontallyScrolling(true);
                                drawablePadding = drawablePadding2;
                                ellipsize2 = ellipsize3;
                                buffertype = buffertype2;
                                break;
                            }
                        case 31:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            password2 = a2.getBoolean(attr, password2);
                            attributes2 = attributes4;
                            firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                            singleLine3 = singleLine3;
                            break;
                        case 32:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            singleLine3 = a2.getBoolean(attr, singleLine3);
                            attributes2 = attributes4;
                            firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                            break;
                        case 33:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            selectallonfocus2 = a2.getBoolean(attr, selectallonfocus2);
                            attributes2 = attributes4;
                            firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                            break;
                        case 34:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                            numeric = numeric2;
                            phone = phone2;
                            i7 = autocap;
                            autotext = autotext2;
                            editable = editable3;
                            if (a2.getBoolean(attr, true)) {
                                drawablePadding = drawablePadding2;
                                attributes2 = attributes4;
                                ellipsize2 = ellipsize3;
                                buffertype = buffertype2;
                                password = password2;
                                singleLine2 = singleLine3;
                                selectallonfocus = selectallonfocus2;
                                break;
                            } else {
                                setIncludeFontPadding(false);
                                drawablePadding = drawablePadding2;
                                attributes2 = attributes4;
                                ellipsize2 = ellipsize3;
                                buffertype = buffertype2;
                                password = password2;
                                singleLine2 = singleLine3;
                                selectallonfocus = selectallonfocus2;
                                break;
                            }
                        case 35:
                            str3 = str6;
                            loc = loc2;
                            str2 = str4;
                            str = str5;
                            maxlength = a2.getInt(attr, -1);
                            attributes2 = attributes4;
                            firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                            break;
                        default:
                            switch (attr) {
                                case 40:
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    numeric2 = a2.getInt(attr, numeric2);
                                    attributes2 = attributes4;
                                    firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                    break;
                                case 41:
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    digits = a2.getText(attr);
                                    str = str5;
                                    attributes2 = attributes4;
                                    firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                    break;
                                case 42:
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    phone2 = a2.getBoolean(attr, phone2);
                                    str = str5;
                                    attributes2 = attributes4;
                                    firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                    break;
                                case 43:
                                    str3 = str6;
                                    loc = loc2;
                                    inputMethod = a2.getText(attr);
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                    break;
                                case 44:
                                    str3 = str6;
                                    loc = loc2;
                                    autocap = a2.getInt(attr, autocap);
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                    break;
                                case 45:
                                    str3 = str6;
                                    autotext2 = a2.getBoolean(attr, autotext2);
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                    break;
                                case 46:
                                    editable3 = a2.getBoolean(attr, editable3);
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                    break;
                                case 47:
                                    firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                    this.mFreezesText = a2.getBoolean(attr, false);
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    drawablePadding = drawablePadding2;
                                    attributes2 = attributes4;
                                    ellipsize2 = ellipsize3;
                                    buffertype = buffertype2;
                                    password = password2;
                                    singleLine2 = singleLine3;
                                    selectallonfocus = selectallonfocus2;
                                    numeric = numeric2;
                                    phone = phone2;
                                    i7 = autocap;
                                    autotext = autotext2;
                                    editable = editable3;
                                    break;
                                case 48:
                                    drawableTop = a2.getDrawable(attr);
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                    break;
                                case 49:
                                    drawableBottom = a2.getDrawable(attr);
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                    break;
                                case 50:
                                    drawableLeft = a2.getDrawable(attr);
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                    break;
                                case 51:
                                    drawableRight2 = a2.getDrawable(attr);
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                    break;
                                case 52:
                                    drawablePadding2 = a2.getDimensionPixelSize(attr, drawablePadding2);
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                    break;
                                case 53:
                                    firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                    this.mSpacingAdd = (float) a2.getDimensionPixelSize(attr, (int) this.mSpacingAdd);
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    ellipsize2 = ellipsize3;
                                    buffertype = buffertype2;
                                    password = password2;
                                    singleLine2 = singleLine3;
                                    selectallonfocus = selectallonfocus2;
                                    numeric = numeric2;
                                    phone = phone2;
                                    i7 = autocap;
                                    autotext = autotext2;
                                    editable = editable3;
                                    drawablePadding = drawablePadding2;
                                    break;
                                case 54:
                                    firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                    this.mSpacingMult = a2.getFloat(attr, this.mSpacingMult);
                                    if (str4.equals(loc2.getLanguage()) && str5.equals(loc2.getCountry())) {
                                        if (this.mSpacingMult < MSPACINGMULTFORMY) {
                                            this.mSpacingMult = MSPACINGMULTFORMY;
                                            str3 = str6;
                                            loc = loc2;
                                            str2 = str4;
                                            str = str5;
                                            attributes2 = attributes4;
                                            ellipsize2 = ellipsize3;
                                            buffertype = buffertype2;
                                            password = password2;
                                            singleLine2 = singleLine3;
                                            selectallonfocus = selectallonfocus2;
                                            numeric = numeric2;
                                            phone = phone2;
                                            i7 = autocap;
                                            autotext = autotext2;
                                            editable = editable3;
                                            drawablePadding = drawablePadding2;
                                            break;
                                        }
                                    }
                                    if (str6.equals(loc2.getLanguage())) {
                                        if (this.mSpacingMult < MSPACINGMULTFORSI) {
                                            this.mSpacingMult = MSPACINGMULTFORSI;
                                            str3 = str6;
                                            loc = loc2;
                                            str2 = str4;
                                            str = str5;
                                            attributes2 = attributes4;
                                            ellipsize2 = ellipsize3;
                                            buffertype = buffertype2;
                                            password = password2;
                                            singleLine2 = singleLine3;
                                            selectallonfocus = selectallonfocus2;
                                            numeric = numeric2;
                                            phone = phone2;
                                            i7 = autocap;
                                            autotext = autotext2;
                                            editable = editable3;
                                            drawablePadding = drawablePadding2;
                                            break;
                                        }
                                    }
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    ellipsize2 = ellipsize3;
                                    buffertype = buffertype2;
                                    password = password2;
                                    singleLine2 = singleLine3;
                                    selectallonfocus = selectallonfocus2;
                                    numeric = numeric2;
                                    phone = phone2;
                                    i7 = autocap;
                                    autotext = autotext2;
                                    editable = editable3;
                                    drawablePadding = drawablePadding2;
                                    break;
                                case 55:
                                    firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                    setMarqueeRepeatLimit(a2.getInt(attr, this.mMarqueeRepeatLimit));
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    ellipsize2 = ellipsize3;
                                    buffertype = buffertype2;
                                    password = password2;
                                    singleLine2 = singleLine3;
                                    selectallonfocus = selectallonfocus2;
                                    numeric = numeric2;
                                    phone = phone2;
                                    i7 = autocap;
                                    autotext = autotext2;
                                    editable = editable3;
                                    drawablePadding = drawablePadding2;
                                    break;
                                case 56:
                                    inputType = a2.getInt(attr, 0);
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    break;
                                case 57:
                                    firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                    setPrivateImeOptions(a2.getString(attr));
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    ellipsize2 = ellipsize3;
                                    buffertype = buffertype2;
                                    password = password2;
                                    singleLine2 = singleLine3;
                                    selectallonfocus = selectallonfocus2;
                                    numeric = numeric2;
                                    phone = phone2;
                                    i7 = autocap;
                                    autotext = autotext2;
                                    editable = editable3;
                                    drawablePadding = drawablePadding2;
                                    break;
                                case 58:
                                    firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                    try {
                                        setInputExtras(a2.getResourceId(attr, 0));
                                    } catch (XmlPullParserException e) {
                                        Log.w(LOG_TAG, "Failure reading input extras", e);
                                    } catch (IOException e2) {
                                        Log.w(LOG_TAG, "Failure reading input extras", e2);
                                        str3 = str6;
                                        loc = loc2;
                                        str2 = str4;
                                        str = str5;
                                        attributes2 = attributes4;
                                        ellipsize2 = ellipsize3;
                                        buffertype = buffertype2;
                                        password = password2;
                                        singleLine2 = singleLine3;
                                        selectallonfocus = selectallonfocus2;
                                        numeric = numeric2;
                                        phone = phone2;
                                        i7 = autocap;
                                        autotext = autotext2;
                                        editable = editable3;
                                        drawablePadding = drawablePadding2;
                                        break;
                                    }
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    ellipsize2 = ellipsize3;
                                    buffertype = buffertype2;
                                    password = password2;
                                    singleLine2 = singleLine3;
                                    selectallonfocus = selectallonfocus2;
                                    numeric = numeric2;
                                    phone = phone2;
                                    i7 = autocap;
                                    autotext = autotext2;
                                    editable = editable3;
                                    drawablePadding = drawablePadding2;
                                    break;
                                case 59:
                                    firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                    createEditorIfNeeded();
                                    this.mEditor.createInputContentTypeIfNeeded();
                                    this.mEditor.mInputContentType.imeOptions = a2.getInt(attr, this.mEditor.mInputContentType.imeOptions);
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    ellipsize2 = ellipsize3;
                                    buffertype = buffertype2;
                                    password = password2;
                                    singleLine2 = singleLine3;
                                    selectallonfocus = selectallonfocus2;
                                    numeric = numeric2;
                                    phone = phone2;
                                    i7 = autocap;
                                    autotext = autotext2;
                                    editable = editable3;
                                    drawablePadding = drawablePadding2;
                                    break;
                                case 60:
                                    firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                    createEditorIfNeeded();
                                    this.mEditor.createInputContentTypeIfNeeded();
                                    this.mEditor.mInputContentType.imeActionLabel = a2.getText(attr);
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    ellipsize2 = ellipsize3;
                                    buffertype = buffertype2;
                                    password = password2;
                                    singleLine2 = singleLine3;
                                    selectallonfocus = selectallonfocus2;
                                    numeric = numeric2;
                                    phone = phone2;
                                    i7 = autocap;
                                    autotext = autotext2;
                                    editable = editable3;
                                    drawablePadding = drawablePadding2;
                                    break;
                                case 61:
                                    firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                    createEditorIfNeeded();
                                    this.mEditor.createInputContentTypeIfNeeded();
                                    this.mEditor.mInputContentType.imeActionId = a2.getInt(attr, this.mEditor.mInputContentType.imeActionId);
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    ellipsize2 = ellipsize3;
                                    buffertype = buffertype2;
                                    password = password2;
                                    singleLine2 = singleLine3;
                                    selectallonfocus = selectallonfocus2;
                                    numeric = numeric2;
                                    phone = phone2;
                                    i7 = autocap;
                                    autotext = autotext2;
                                    editable = editable3;
                                    drawablePadding = drawablePadding2;
                                    break;
                                case 62:
                                    firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                    this.mTextSelectHandleLeftRes = a2.getResourceId(attr, 0);
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    ellipsize2 = ellipsize3;
                                    buffertype = buffertype2;
                                    password = password2;
                                    singleLine2 = singleLine3;
                                    selectallonfocus = selectallonfocus2;
                                    numeric = numeric2;
                                    phone = phone2;
                                    i7 = autocap;
                                    autotext = autotext2;
                                    editable = editable3;
                                    drawablePadding = drawablePadding2;
                                    break;
                                case 63:
                                    firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                    this.mTextSelectHandleRightRes = a2.getResourceId(attr, 0);
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    ellipsize2 = ellipsize3;
                                    buffertype = buffertype2;
                                    password = password2;
                                    singleLine2 = singleLine3;
                                    selectallonfocus = selectallonfocus2;
                                    numeric = numeric2;
                                    phone = phone2;
                                    i7 = autocap;
                                    autotext = autotext2;
                                    editable = editable3;
                                    drawablePadding = drawablePadding2;
                                    break;
                                case 64:
                                    firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                    this.mTextSelectHandleRes = a2.getResourceId(attr, 0);
                                    str3 = str6;
                                    loc = loc2;
                                    str2 = str4;
                                    str = str5;
                                    attributes2 = attributes4;
                                    ellipsize2 = ellipsize3;
                                    buffertype = buffertype2;
                                    password = password2;
                                    singleLine2 = singleLine3;
                                    selectallonfocus = selectallonfocus2;
                                    numeric = numeric2;
                                    phone = phone2;
                                    i7 = autocap;
                                    autotext = autotext2;
                                    editable = editable3;
                                    drawablePadding = drawablePadding2;
                                    break;
                                default:
                                    switch (attr) {
                                        case 79:
                                            drawableTint = a2.getColorStateList(attr);
                                            str3 = str6;
                                            loc = loc2;
                                            str2 = str4;
                                            str = str5;
                                            attributes2 = attributes4;
                                            break;
                                        case 80:
                                            drawableTintMode = Drawable.parseBlendMode(a2.getInt(attr, -1), drawableTintMode);
                                            str3 = str6;
                                            loc = loc2;
                                            str2 = str4;
                                            str = str5;
                                            attributes2 = attributes4;
                                            break;
                                        case 81:
                                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                            this.mBreakStrategy = a2.getInt(attr, 0);
                                            str3 = str6;
                                            loc = loc2;
                                            str2 = str4;
                                            str = str5;
                                            attributes2 = attributes4;
                                            ellipsize2 = ellipsize3;
                                            buffertype = buffertype2;
                                            password = password2;
                                            singleLine2 = singleLine3;
                                            selectallonfocus = selectallonfocus2;
                                            numeric = numeric2;
                                            phone = phone2;
                                            i7 = autocap;
                                            autotext = autotext2;
                                            editable = editable3;
                                            drawablePadding = drawablePadding2;
                                            break;
                                        case 82:
                                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                            this.mHyphenationFrequency = a2.getInt(attr, 0);
                                            str3 = str6;
                                            loc = loc2;
                                            str2 = str4;
                                            str = str5;
                                            attributes2 = attributes4;
                                            ellipsize2 = ellipsize3;
                                            buffertype = buffertype2;
                                            password = password2;
                                            singleLine2 = singleLine3;
                                            selectallonfocus = selectallonfocus2;
                                            numeric = numeric2;
                                            phone = phone2;
                                            i7 = autocap;
                                            autotext = autotext2;
                                            editable = editable3;
                                            drawablePadding = drawablePadding2;
                                            break;
                                        case 83:
                                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                            createEditorIfNeeded();
                                            this.mEditor.mAllowUndo = a2.getBoolean(attr, true);
                                            str3 = str6;
                                            loc = loc2;
                                            str2 = str4;
                                            str = str5;
                                            attributes2 = attributes4;
                                            ellipsize2 = ellipsize3;
                                            buffertype = buffertype2;
                                            password = password2;
                                            singleLine2 = singleLine3;
                                            selectallonfocus = selectallonfocus2;
                                            numeric = numeric2;
                                            phone = phone2;
                                            i7 = autocap;
                                            autotext = autotext2;
                                            editable = editable3;
                                            drawablePadding = drawablePadding2;
                                            break;
                                        case 84:
                                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                            this.mAutoSizeTextType = a2.getInt(attr, 0);
                                            str3 = str6;
                                            loc = loc2;
                                            str2 = str4;
                                            str = str5;
                                            attributes2 = attributes4;
                                            ellipsize2 = ellipsize3;
                                            buffertype = buffertype2;
                                            password = password2;
                                            singleLine2 = singleLine3;
                                            selectallonfocus = selectallonfocus2;
                                            numeric = numeric2;
                                            phone = phone2;
                                            i7 = autocap;
                                            autotext = autotext2;
                                            editable = editable3;
                                            drawablePadding = drawablePadding2;
                                            break;
                                        case 85:
                                            autoSizeStepGranularityInPx2 = a2.getDimension(attr, -1.0f);
                                            str3 = str6;
                                            loc = loc2;
                                            str2 = str4;
                                            str = str5;
                                            attributes2 = attributes4;
                                            firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                            break;
                                        case 86:
                                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                            int autoSizeStepSizeArrayResId = a2.getResourceId(attr, 0);
                                            if (autoSizeStepSizeArrayResId <= 0) {
                                                str3 = str6;
                                                loc = loc2;
                                                str2 = str4;
                                                str = str5;
                                                attributes2 = attributes4;
                                                ellipsize2 = ellipsize3;
                                                buffertype = buffertype2;
                                                password = password2;
                                                singleLine2 = singleLine3;
                                                selectallonfocus = selectallonfocus2;
                                                numeric = numeric2;
                                                phone = phone2;
                                                i7 = autocap;
                                                autotext = autotext2;
                                                editable = editable3;
                                                drawablePadding = drawablePadding2;
                                                break;
                                            } else {
                                                TypedArray autoSizePresetTextSizes = a2.getResources().obtainTypedArray(autoSizeStepSizeArrayResId);
                                                setupAutoSizeUniformPresetSizes(autoSizePresetTextSizes);
                                                autoSizePresetTextSizes.recycle();
                                                str3 = str6;
                                                loc = loc2;
                                                str2 = str4;
                                                str = str5;
                                                attributes2 = attributes4;
                                                ellipsize2 = ellipsize3;
                                                buffertype = buffertype2;
                                                password = password2;
                                                singleLine2 = singleLine3;
                                                selectallonfocus = selectallonfocus2;
                                                numeric = numeric2;
                                                phone = phone2;
                                                i7 = autocap;
                                                autotext = autotext2;
                                                editable = editable3;
                                                drawablePadding = drawablePadding2;
                                                break;
                                            }
                                        case 87:
                                            autoSizeMinTextSizeInPx2 = a2.getDimension(attr, -1.0f);
                                            str3 = str6;
                                            loc = loc2;
                                            str2 = str4;
                                            str = str5;
                                            attributes2 = attributes4;
                                            firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                            break;
                                        case 88:
                                            autoSizeMaxTextSizeInPx2 = a2.getDimension(attr, -1.0f);
                                            str3 = str6;
                                            loc = loc2;
                                            str2 = str4;
                                            str = str5;
                                            attributes2 = attributes4;
                                            firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                            break;
                                        case 89:
                                            firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                            this.mJustificationMode = a2.getInt(attr, 0);
                                            str3 = str6;
                                            loc = loc2;
                                            str2 = str4;
                                            str = str5;
                                            attributes2 = attributes4;
                                            ellipsize2 = ellipsize3;
                                            buffertype = buffertype2;
                                            password = password2;
                                            singleLine2 = singleLine3;
                                            selectallonfocus = selectallonfocus2;
                                            numeric = numeric2;
                                            phone = phone2;
                                            i7 = autocap;
                                            autotext = autotext2;
                                            editable = editable3;
                                            drawablePadding = drawablePadding2;
                                            break;
                                        default:
                                            switch (attr) {
                                                case 92:
                                                    firstBaselineToTopHeight2 = a2.getDimensionPixelSize(attr, -1);
                                                    str3 = str6;
                                                    loc = loc2;
                                                    str2 = str4;
                                                    str = str5;
                                                    attributes2 = attributes4;
                                                    break;
                                                case 93:
                                                    lastBaselineToBottomHeight = a2.getDimensionPixelSize(attr, -1);
                                                    str3 = str6;
                                                    loc = loc2;
                                                    str2 = str4;
                                                    str = str5;
                                                    attributes2 = attributes4;
                                                    firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                                    break;
                                                case 94:
                                                    lineHeight = a2.getDimensionPixelSize(attr, -1);
                                                    str3 = str6;
                                                    loc = loc2;
                                                    str2 = str4;
                                                    str = str5;
                                                    attributes2 = attributes4;
                                                    firstBaselineToTopHeight2 = firstBaselineToTopHeight2;
                                                    break;
                                                default:
                                                    str3 = str6;
                                                    loc = loc2;
                                                    str2 = str4;
                                                    str = str5;
                                                    firstBaselineToTopHeight = firstBaselineToTopHeight2;
                                                    attributes2 = attributes4;
                                                    ellipsize2 = ellipsize3;
                                                    buffertype = buffertype2;
                                                    password = password2;
                                                    singleLine2 = singleLine3;
                                                    selectallonfocus = selectallonfocus2;
                                                    numeric = numeric2;
                                                    phone = phone2;
                                                    i7 = autocap;
                                                    autotext = autotext2;
                                                    editable = editable3;
                                                    drawablePadding = drawablePadding2;
                                                    break;
                                            }
                                    }
                            }
                    }
                } else {
                    str3 = str6;
                    loc = loc2;
                    str2 = str4;
                    str = str5;
                    firstBaselineToTopHeight = firstBaselineToTopHeight2;
                    attributes2 = attributes4;
                    buffertype = buffertype2;
                    password = password2;
                    singleLine2 = singleLine3;
                    selectallonfocus = selectallonfocus2;
                    numeric = numeric2;
                    phone = phone2;
                    i7 = autocap;
                    autotext = autotext2;
                    editable = editable3;
                    drawablePadding = drawablePadding2;
                    ellipsize2 = ellipsize3;
                    this.mTextEditSuggestionHighlightStyle = a2.getResourceId(attr, 0);
                }
                i8++;
                n2 = n2;
                str6 = str3;
                loc2 = loc;
                str4 = str2;
                str5 = str;
                attributes4 = attributes2;
            } else {
                str3 = str6;
                loc = loc2;
                str2 = str4;
                str = str5;
                firstBaselineToTopHeight = firstBaselineToTopHeight2;
                attributes2 = attributes4;
                ellipsize2 = ellipsize3;
                buffertype = buffertype2;
                password = password2;
                singleLine2 = singleLine3;
                selectallonfocus = selectallonfocus2;
                numeric = numeric2;
                phone = phone2;
                i7 = autocap;
                autotext = autotext2;
                editable = editable3;
                drawablePadding = drawablePadding2;
                this.mTextEditSuggestionItemLayout = a2.getResourceId(attr, 0);
            }
            autotext2 = autotext;
            autocap = i7;
            phone2 = phone;
            numeric2 = numeric;
            selectallonfocus2 = selectallonfocus;
            password2 = password;
            editable3 = editable;
            buffertype2 = buffertype;
            firstBaselineToTopHeight2 = firstBaselineToTopHeight;
            singleLine3 = singleLine2;
            drawablePadding2 = drawablePadding;
            ellipsize3 = ellipsize2;
            i8++;
            n2 = n2;
            str6 = str3;
            loc2 = loc;
            str4 = str2;
            str5 = str;
            attributes4 = attributes2;
        }
        a2.recycle();
        BufferType bufferType2 = BufferType.EDITABLE;
        int variation = inputType & 4095;
        boolean passwordInputType2 = variation == 129;
        boolean webPasswordInputType2 = variation == 225;
        boolean numberPasswordInputType2 = variation == 18;
        int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        this.mUseInternationalizedInput = targetSdkVersion >= 26;
        this.mUseFallbackLineSpacing = targetSdkVersion >= 28;
        if (inputMethod != null) {
            try {
                Class<?> c = Class.forName(inputMethod.toString());
                try {
                    createEditorIfNeeded();
                    try {
                        this.mEditor.mKeyListener = (KeyListener) c.newInstance();
                        try {
                            Editor editor3 = this.mEditor;
                            if (inputType != 0) {
                                i6 = inputType;
                            } else {
                                i6 = this.mEditor.mKeyListener.getInputType();
                            }
                            editor3.mInputType = i6;
                        } catch (IncompatibleClassChangeError e3) {
                            this.mEditor.mInputType = 1;
                        }
                    } catch (InstantiationException e4) {
                        ex = e4;
                    } catch (IllegalAccessException e5) {
                        ex = e5;
                        throw new RuntimeException(ex);
                    }
                } catch (InstantiationException e6) {
                    ex = e6;
                    throw new RuntimeException(ex);
                } catch (IllegalAccessException e7) {
                    ex = e7;
                    throw new RuntimeException(ex);
                }
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        } else if (digits != null) {
            createEditorIfNeeded();
            this.mEditor.mKeyListener = DigitsKeyListener.getInstance(digits.toString());
            this.mEditor.mInputType = inputType != 0 ? inputType : 1;
        } else {
            if (inputType != 0) {
                setInputType(inputType, true);
                bufferType = bufferType2;
                singleLine = !isMultilineInputType(inputType);
            } else if (phone2) {
                createEditorIfNeeded();
                this.mEditor.mKeyListener = DialerKeyListener.getInstance();
                this.mEditor.mInputType = 3;
                bufferType = bufferType2;
                singleLine = singleLine3;
            } else if (numeric2 != 0) {
                createEditorIfNeeded();
                this.mEditor.mKeyListener = DigitsKeyListener.getInstance(null, (numeric2 & 2) != 0, (numeric2 & 4) != 0);
                this.mEditor.mInputType = this.mEditor.mKeyListener.getInputType();
                bufferType = bufferType2;
                singleLine = singleLine3;
            } else {
                if (autotext2) {
                    i5 = 2;
                } else if (autocap != -1) {
                    i5 = 2;
                } else if (editable3) {
                    createEditorIfNeeded();
                    this.mEditor.mKeyListener = TextKeyListener.getInstance();
                    this.mEditor.mInputType = 1;
                } else if (isTextSelectable()) {
                    Editor editor4 = this.mEditor;
                    if (editor4 != null) {
                        editor4.mKeyListener = null;
                        editor4.mInputType = 0;
                    }
                    BufferType bufferType3 = BufferType.SPANNABLE;
                    setMovementMethod(ArrowKeyMovementMethod.getInstance());
                    bufferType = bufferType3;
                    singleLine = singleLine3;
                } else {
                    Editor editor5 = this.mEditor;
                    if (editor5 != null) {
                        editor5.mKeyListener = null;
                    }
                    if (buffertype2 == 0) {
                        bufferType = BufferType.NORMAL;
                        singleLine = singleLine3;
                    } else if (buffertype2 == 1) {
                        bufferType = BufferType.SPANNABLE;
                        singleLine = singleLine3;
                    } else if (buffertype2 == 2) {
                        bufferType = BufferType.EDITABLE;
                        singleLine = singleLine3;
                    }
                }
                int inputType2 = 1;
                if (autocap == 1) {
                    cap = TextKeyListener.Capitalize.SENTENCES;
                    inputType2 = 1 | 16384;
                } else if (autocap == i5) {
                    cap = TextKeyListener.Capitalize.WORDS;
                    inputType2 = 1 | 8192;
                } else if (autocap != 3) {
                    cap = TextKeyListener.Capitalize.NONE;
                } else {
                    cap = TextKeyListener.Capitalize.CHARACTERS;
                    inputType2 = 1 | 4096;
                }
                createEditorIfNeeded();
                this.mEditor.mKeyListener = TextKeyListener.getInstance(autotext2, cap);
                this.mEditor.mInputType = inputType2;
                bufferType = bufferType2;
                singleLine = singleLine3;
            }
            editor = this.mEditor;
            if (editor == null) {
                passwordInputType = passwordInputType2;
                webPasswordInputType = webPasswordInputType2;
                numberPasswordInputType = numberPasswordInputType2;
                editor.adjustInputType(password2, passwordInputType, webPasswordInputType, numberPasswordInputType);
            } else {
                passwordInputType = passwordInputType2;
                webPasswordInputType = webPasswordInputType2;
                numberPasswordInputType = numberPasswordInputType2;
            }
            if (!selectallonfocus2) {
                createEditorIfNeeded();
                this.mEditor.mSelectAllOnFocus = true;
                if (bufferType == BufferType.NORMAL) {
                    bufferType = BufferType.SPANNABLE;
                }
            }
            if (drawableTint == null || drawableTintMode != null) {
                if (this.mDrawables == null) {
                    this.mDrawables = new Drawables(context);
                }
                if (drawableTint == null) {
                    Drawables drawables = this.mDrawables;
                    drawables.mTintList = drawableTint;
                    z = true;
                    drawables.mHasTint = true;
                } else {
                    z = true;
                }
                if (drawableTintMode != null) {
                    Drawables drawables2 = this.mDrawables;
                    drawables2.mBlendMode = drawableTintMode;
                    drawables2.mHasTintMode = z;
                }
            }
            Drawable drawableBottom2 = drawableBottom;
            Drawable drawableRight3 = drawableRight2;
            setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight3, drawableBottom2);
            setRelativeDrawablesIfNeeded(drawableStart, drawableEnd);
            setCompoundDrawablePadding(drawablePadding2);
            setInputTypeSingleLine(singleLine);
            applySingleLine(singleLine, singleLine, singleLine);
            if (singleLine || getKeyListener() != null) {
                ellipsize = ellipsize3;
            } else {
                ellipsize = ellipsize3;
                if (ellipsize == -1) {
                    ellipsize = 3;
                }
            }
            if (ellipsize != 1) {
                setEllipsize(TextUtils.TruncateAt.START);
            } else if (ellipsize == 2) {
                setEllipsize(TextUtils.TruncateAt.MIDDLE);
            } else if (ellipsize == 3) {
                setEllipsize(TextUtils.TruncateAt.END);
            } else if (ellipsize == 4) {
                if (ViewConfiguration.get(context).isFadingMarqueeEnabled()) {
                    setHorizontalFadingEdgeEnabled(true);
                    this.mMarqueeFadeMode = 0;
                } else {
                    setHorizontalFadingEdgeEnabled(false);
                    this.mMarqueeFadeMode = 1;
                }
                setEllipsize(TextUtils.TruncateAt.MARQUEE);
            }
            isPassword = !password2 || passwordInputType || webPasswordInputType || numberPasswordInputType;
            if (isPassword) {
                Editor editor6 = this.mEditor;
                if (editor6 != null) {
                }
                isMonospaceEnforced = false;
                if (isMonospaceEnforced) {
                    attributes = attributes4;
                    attributes.mTypefaceIndex = 3;
                } else {
                    attributes = attributes4;
                }
                applyTextAppearance(attributes);
                if (isPassword) {
                    setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                if (maxlength >= 0) {
                    setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxlength)});
                } else {
                    setFilters(NO_FILTERS);
                }
                setText(text, bufferType);
                if (this.mText == null) {
                    charSequence = "";
                    this.mText = charSequence;
                } else {
                    charSequence = "";
                }
                if (this.mTransformed == null) {
                    this.mTransformed = charSequence;
                }
                if (textIsSetFromXml) {
                    this.mTextSetFromXmlOrResourceId = true;
                }
                if (hint != null) {
                    setHint(hint);
                }
                TypedArray a3 = context.obtainStyledAttributes(attrs, R.styleable.View, defStyleAttr, defStyleRes);
                boolean canInputOrMove = this.mMovement == null || getKeyListener() != null;
                boolean clickable = !canInputOrMove || isClickable();
                boolean longClickable = !canInputOrMove || isLongClickable();
                int focusable2 = getFocusable();
                n = a3.getIndexCount();
                i = 0;
                focusable = focusable2;
                boolean clickable2 = clickable;
                boolean longClickable2 = longClickable;
                while (i < n) {
                    int attr2 = a3.getIndex(i);
                    if (attr2 == 19) {
                        TypedValue val = new TypedValue();
                        if (a3.getValue(attr2, val)) {
                            drawableRight = drawableRight3;
                            if (val.type == 18) {
                                i4 = val.data == 0 ? 0 : 1;
                            } else {
                                i4 = val.data;
                            }
                            focusable = i4;
                        } else {
                            drawableRight = drawableRight3;
                        }
                    } else if (attr2 == 30) {
                        clickable2 = a3.getBoolean(attr2, clickable2);
                        drawableRight = drawableRight3;
                    } else if (attr2 != 31) {
                        drawableRight = drawableRight3;
                    } else {
                        longClickable2 = a3.getBoolean(attr2, longClickable2);
                        drawableRight = drawableRight3;
                    }
                    i++;
                    n = n;
                    drawableBottom2 = drawableBottom2;
                    drawableRight3 = drawableRight;
                }
                a3.recycle();
                if (focusable != getFocusable()) {
                    setFocusable(focusable);
                }
                setClickable(clickable2);
                setLongClickable(longClickable2);
                editor2 = this.mEditor;
                if (editor2 != null) {
                    editor2.prepareCursorControllers();
                }
                if (getImportantForAccessibility() == 0) {
                    i2 = 1;
                    setImportantForAccessibility(1);
                } else {
                    i2 = 1;
                }
                if (!supportsAutoSizeText()) {
                    this.mAutoSizeTextType = 0;
                } else if (this.mAutoSizeTextType == i2) {
                    if (!this.mHasPresetAutoSizeValues) {
                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        if (autoSizeMinTextSizeInPx2 == -1.0f) {
                            i3 = 2;
                            autoSizeMinTextSizeInPx = TypedValue.applyDimension(2, 12.0f, displayMetrics);
                        } else {
                            i3 = 2;
                            autoSizeMinTextSizeInPx = autoSizeMinTextSizeInPx2;
                        }
                        if (autoSizeMaxTextSizeInPx2 == -1.0f) {
                            autoSizeMaxTextSizeInPx = TypedValue.applyDimension(i3, 112.0f, displayMetrics);
                        } else {
                            autoSizeMaxTextSizeInPx = autoSizeMaxTextSizeInPx2;
                        }
                        if (autoSizeStepGranularityInPx2 == -1.0f) {
                            autoSizeStepGranularityInPx = 1.0f;
                        } else {
                            autoSizeStepGranularityInPx = autoSizeStepGranularityInPx2;
                        }
                        validateAndSetAutoSizeTextTypeUniformConfiguration(autoSizeMinTextSizeInPx, autoSizeMaxTextSizeInPx, autoSizeStepGranularityInPx);
                    }
                    setupAutoSizeText();
                }
                if (firstBaselineToTopHeight2 >= 0) {
                    setFirstBaselineToTopHeight(firstBaselineToTopHeight2);
                }
                if (lastBaselineToBottomHeight >= 0) {
                    setLastBaselineToBottomHeight(lastBaselineToBottomHeight);
                }
                if (lineHeight >= 0) {
                    setLineHeight(lineHeight);
                    return;
                }
                return;
            }
            isMonospaceEnforced = true;
            if (isMonospaceEnforced) {
            }
            applyTextAppearance(attributes);
            if (isPassword) {
            }
            if (maxlength >= 0) {
            }
            setText(text, bufferType);
            if (this.mText == null) {
            }
            if (this.mTransformed == null) {
            }
            if (textIsSetFromXml) {
            }
            if (hint != null) {
            }
            TypedArray a32 = context.obtainStyledAttributes(attrs, R.styleable.View, defStyleAttr, defStyleRes);
            if (this.mMovement == null) {
            }
            if (!canInputOrMove) {
            }
            if (!canInputOrMove) {
            }
            int focusable22 = getFocusable();
            n = a32.getIndexCount();
            i = 0;
            focusable = focusable22;
            boolean clickable22 = clickable;
            boolean longClickable22 = longClickable;
            while (i < n) {
            }
            a32.recycle();
            if (focusable != getFocusable()) {
            }
            setClickable(clickable22);
            setLongClickable(longClickable22);
            editor2 = this.mEditor;
            if (editor2 != null) {
            }
            if (getImportantForAccessibility() == 0) {
            }
            if (!supportsAutoSizeText()) {
            }
            if (firstBaselineToTopHeight2 >= 0) {
            }
            if (lastBaselineToBottomHeight >= 0) {
            }
            if (lineHeight >= 0) {
            }
        }
        bufferType = bufferType2;
        singleLine = singleLine3;
        editor = this.mEditor;
        if (editor == null) {
        }
        if (!selectallonfocus2) {
        }
        if (drawableTint == null) {
        }
        if (this.mDrawables == null) {
        }
        if (drawableTint == null) {
        }
        if (drawableTintMode != null) {
        }
        Drawable drawableBottom22 = drawableBottom;
        Drawable drawableRight32 = drawableRight2;
        setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight32, drawableBottom22);
        setRelativeDrawablesIfNeeded(drawableStart, drawableEnd);
        setCompoundDrawablePadding(drawablePadding2);
        setInputTypeSingleLine(singleLine);
        applySingleLine(singleLine, singleLine, singleLine);
        if (singleLine) {
        }
        ellipsize = ellipsize3;
        if (ellipsize != 1) {
        }
        if (!password2) {
        }
        if (isPassword) {
        }
        isMonospaceEnforced = true;
        if (isMonospaceEnforced) {
        }
        applyTextAppearance(attributes);
        if (isPassword) {
        }
        if (maxlength >= 0) {
        }
        setText(text, bufferType);
        if (this.mText == null) {
        }
        if (this.mTransformed == null) {
        }
        if (textIsSetFromXml) {
        }
        if (hint != null) {
        }
        TypedArray a322 = context.obtainStyledAttributes(attrs, R.styleable.View, defStyleAttr, defStyleRes);
        if (this.mMovement == null) {
        }
        if (!canInputOrMove) {
        }
        if (!canInputOrMove) {
        }
        int focusable222 = getFocusable();
        n = a322.getIndexCount();
        i = 0;
        focusable = focusable222;
        boolean clickable222 = clickable;
        boolean longClickable222 = longClickable;
        while (i < n) {
        }
        a322.recycle();
        if (focusable != getFocusable()) {
        }
        setClickable(clickable222);
        setLongClickable(longClickable222);
        editor2 = this.mEditor;
        if (editor2 != null) {
        }
        if (getImportantForAccessibility() == 0) {
        }
        if (!supportsAutoSizeText()) {
        }
        if (firstBaselineToTopHeight2 >= 0) {
        }
        if (lastBaselineToBottomHeight >= 0) {
        }
        if (lineHeight >= 0) {
        }
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
        if (!supportsAutoSizeText()) {
            return;
        }
        if (autoSizeTextType == 0) {
            clearAutoSizeConfiguration();
        } else if (autoSizeTextType == 1) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            validateAndSetAutoSizeTextTypeUniformConfiguration(TypedValue.applyDimension(2, 12.0f, displayMetrics), TypedValue.applyDimension(2, 112.0f, displayMetrics), 1.0f);
            if (setupAutoSizeText()) {
                autoSizeText();
                invalidate();
            }
        } else {
            throw new IllegalArgumentException("Unknown auto-size text type: " + autoSizeTextType);
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
            int[] iArr = this.mAutoSizeTextSizesInPx;
            this.mAutoSizeMinTextSizeInPx = (float) iArr[0];
            this.mAutoSizeMaxTextSizeInPx = (float) iArr[sizesLength - 1];
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
        int presetValuesLength = presetValues.length;
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
            return presetValues;
        }
        return uniqueValidSizes.toArray();
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

    @Override // android.view.View
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 100) {
            return;
        }
        if (resultCode != -1 || data == null) {
            Spannable spannable = this.mSpannable;
            if (spannable != null) {
                Selection.setSelection(spannable, getSelectionEnd());
                return;
            }
            return;
        }
        CharSequence result = data.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        if (result == null) {
            return;
        }
        if (isTextEditable()) {
            replaceSelectionWithText(result);
            Editor editor = this.mEditor;
            if (editor != null) {
                editor.refreshTextActionMode();
            }
        } else if (result.length() > 0) {
            Toast.makeText(getContext(), String.valueOf(result), 1).show();
        }
    }

    private void setTypefaceFromAttrs(Typeface typeface, String familyName, int typefaceIndex, int style, int weight) {
        if (typeface == null && familyName != null) {
            resolveStyleAndSetTypeface(Typeface.create(familyName, 0), style, weight);
        } else if (typeface != null) {
            resolveStyleAndSetTypeface(typeface, style, weight);
        } else if (typefaceIndex == 1) {
            resolveStyleAndSetTypeface(Typeface.SANS_SERIF, style, weight);
        } else if (typefaceIndex == 2) {
            resolveStyleAndSetTypeface(Typeface.SERIF, style, weight);
        } else if (typefaceIndex != 3) {
            resolveStyleAndSetTypeface(null, style, weight);
        } else {
            resolveStyleAndSetTypeface(Typeface.MONOSPACE, style, weight);
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

    @Override // android.view.View
    @RemotableViewMethod
    public void setEnabled(boolean enabled) {
        InputMethodManager imm;
        InputMethodManager imm2;
        if (enabled != isEnabled()) {
            if (!enabled && (imm2 = getInputMethodManager()) != null && imm2.isActive(this)) {
                imm2.hideSoftInputFromWindow(getWindowToken(), 0);
            }
            super.setEnabled(enabled);
            if (enabled && (imm = getInputMethodManager()) != null) {
                imm.restartInput(this);
            }
            Editor editor = this.mEditor;
            if (editor != null) {
                editor.invalidateTextDisplayList();
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
        CharSequence charSequence = this.mText;
        if (charSequence instanceof Editable) {
            return (Editable) charSequence;
        }
        return null;
    }

    @VisibleForTesting
    public CharSequence getTransformed() {
        return this.mTransformed;
    }

    public int getLineHeight() {
        return FastMath.round((((float) this.mTextPaint.getFontMetricsInt(null)) * this.mSpacingMult) + this.mSpacingAdd);
    }

    public final Layout getLayout() {
        return this.mLayout;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
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
        Editor editor = this.mEditor;
        if (editor == null) {
            return null;
        }
        return editor.mKeyListener;
    }

    public void setKeyListener(KeyListener input) {
        this.mListenerChanged = true;
        setKeyListenerOnly(input);
        fixFocusableAndClickableSettings();
        if (input != null) {
            createEditorIfNeeded();
            setInputTypeFromEditor();
        } else {
            Editor editor = this.mEditor;
            if (editor != null) {
                editor.mInputType = 0;
            }
        }
        InputMethodManager imm = getInputMethodManager();
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
                if (input != null) {
                    CharSequence charSequence = this.mText;
                    if (!(charSequence instanceof Editable)) {
                        setText(charSequence);
                    }
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
            Editor editor = this.mEditor;
            if (editor != null) {
                editor.prepareCursorControllers();
            }
        }
    }

    private void fixFocusableAndClickableSettings() {
        Editor editor;
        if (this.mMovement == null && ((editor = this.mEditor) == null || editor.mKeyListener == null)) {
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
        Spannable spannable;
        TransformationMethod transformationMethod = this.mTransformation;
        if (method != transformationMethod) {
            if (!(transformationMethod == null || (spannable = this.mSpannable) == null)) {
                spannable.removeSpan(transformationMethod);
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
        int gravity;
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
        if (layoutht >= viewht || (gravity = this.mGravity & 112) == 48) {
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
        Context context = getContext();
        Drawable drawable = null;
        Drawable drawable2 = left != 0 ? context.getDrawable(left) : null;
        Drawable drawable3 = top != 0 ? context.getDrawable(top) : null;
        Drawable drawable4 = right != 0 ? context.getDrawable(right) : null;
        if (bottom != 0) {
            drawable = context.getDrawable(bottom);
        }
        setCompoundDrawablesWithIntrinsicBounds(drawable2, drawable3, drawable4, drawable);
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
        Context context = getContext();
        Drawable drawable = null;
        Drawable drawable2 = start != 0 ? context.getDrawable(start) : null;
        Drawable drawable3 = top != 0 ? context.getDrawable(top) : null;
        Drawable drawable4 = end != 0 ? context.getDrawable(end) : null;
        if (bottom != 0) {
            drawable = context.getDrawable(bottom);
        }
        setCompoundDrawablesRelativeWithIntrinsicBounds(drawable2, drawable3, drawable4, drawable);
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
        Drawables drawables = this.mDrawables;
        drawables.mTintList = tint;
        drawables.mHasTint = true;
        applyCompoundDrawableTint();
    }

    public ColorStateList getCompoundDrawableTintList() {
        Drawables drawables = this.mDrawables;
        if (drawables != null) {
            return drawables.mTintList;
        }
        return null;
    }

    public void setCompoundDrawableTintMode(PorterDuff.Mode tintMode) {
        setCompoundDrawableTintBlendMode(tintMode != null ? BlendMode.fromValue(tintMode.nativeInt) : null);
    }

    public void setCompoundDrawableTintBlendMode(BlendMode blendMode) {
        if (this.mDrawables == null) {
            this.mDrawables = new Drawables(getContext());
        }
        Drawables drawables = this.mDrawables;
        drawables.mBlendMode = blendMode;
        drawables.mHasTintMode = true;
        applyCompoundDrawableTint();
    }

    public PorterDuff.Mode getCompoundDrawableTintMode() {
        BlendMode mode = getCompoundDrawableTintBlendMode();
        if (mode != null) {
            return BlendMode.blendModeToPorterDuffMode(mode);
        }
        return null;
    }

    public BlendMode getCompoundDrawableTintBlendMode() {
        Drawables drawables = this.mDrawables;
        if (drawables != null) {
            return drawables.mBlendMode;
        }
        return null;
    }

    private void applyCompoundDrawableTint() {
        Drawables drawables = this.mDrawables;
        if (drawables != null) {
            if (drawables.mHasTint || this.mDrawables.mHasTintMode) {
                ColorStateList tintList = this.mDrawables.mTintList;
                BlendMode blendMode = this.mDrawables.mBlendMode;
                boolean hasTint = this.mDrawables.mHasTint;
                boolean hasTintMode = this.mDrawables.mHasTintMode;
                int[] state = getDrawableState();
                Drawable[] drawableArr = this.mDrawables.mShowing;
                for (Drawable dr : drawableArr) {
                    if (!(dr == null || dr == this.mDrawables.mDrawableError)) {
                        dr.mutate();
                        if (hasTint) {
                            dr.setTintList(tintList);
                        }
                        if (hasTintMode) {
                            dr.setTintBlendMode(blendMode);
                        }
                        if (dr.isStateful()) {
                            dr.setState(state);
                        }
                    }
                }
            }
        }
    }

    @Override // android.view.View
    public void setPadding(int left, int top, int right, int bottom) {
        if (!(left == this.mPaddingLeft && right == this.mPaddingRight && top == this.mPaddingTop && bottom == this.mPaddingBottom)) {
            nullLayouts();
        }
        super.setPadding(left, top, right, bottom);
        invalidate();
    }

    @Override // android.view.View
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

    @RemotableViewMethod
    public void setTextSelectHandle(Drawable textSelectHandle) {
        Preconditions.checkNotNull(textSelectHandle, "The text select handle should not be null.");
        this.mTextSelectHandle = textSelectHandle;
        this.mTextSelectHandleRes = 0;
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.loadHandleDrawables(true);
        }
    }

    @RemotableViewMethod
    public void setTextSelectHandle(int textSelectHandle) {
        Preconditions.checkArgument(textSelectHandle != 0, "The text select handle should be a valid drawable resource id.");
        setTextSelectHandle(this.mContext.getDrawable(textSelectHandle));
    }

    public Drawable getTextSelectHandle() {
        if (this.mTextSelectHandle == null && this.mTextSelectHandleRes != 0) {
            this.mTextSelectHandle = this.mContext.getDrawable(this.mTextSelectHandleRes);
        }
        return this.mTextSelectHandle;
    }

    @RemotableViewMethod
    public void setTextSelectHandleLeft(Drawable textSelectHandleLeft) {
        Preconditions.checkNotNull(textSelectHandleLeft, "The left text select handle should not be null.");
        this.mTextSelectHandleLeft = textSelectHandleLeft;
        this.mTextSelectHandleLeftRes = 0;
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.loadHandleDrawables(true);
        }
    }

    @RemotableViewMethod
    public void setTextSelectHandleLeft(int textSelectHandleLeft) {
        Preconditions.checkArgument(textSelectHandleLeft != 0, "The text select left handle should be a valid drawable resource id.");
        setTextSelectHandleLeft(this.mContext.getDrawable(textSelectHandleLeft));
    }

    public Drawable getTextSelectHandleLeft() {
        if (this.mTextSelectHandleLeft == null && this.mTextSelectHandleLeftRes != 0) {
            this.mTextSelectHandleLeft = this.mContext.getDrawable(this.mTextSelectHandleLeftRes);
        }
        return this.mTextSelectHandleLeft;
    }

    @RemotableViewMethod
    public void setTextSelectHandleRight(Drawable textSelectHandleRight) {
        Preconditions.checkNotNull(textSelectHandleRight, "The right text select handle should not be null.");
        this.mTextSelectHandleRight = textSelectHandleRight;
        this.mTextSelectHandleRightRes = 0;
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.loadHandleDrawables(true);
        }
    }

    @RemotableViewMethod
    public void setTextSelectHandleRight(int textSelectHandleRight) {
        Preconditions.checkArgument(textSelectHandleRight != 0, "The text select right handle should be a valid drawable resource id.");
        setTextSelectHandleRight(this.mContext.getDrawable(textSelectHandleRight));
    }

    public Drawable getTextSelectHandleRight() {
        if (this.mTextSelectHandleRight == null && this.mTextSelectHandleRightRes != 0) {
            this.mTextSelectHandleRight = this.mContext.getDrawable(this.mTextSelectHandleRightRes);
        }
        return this.mTextSelectHandleRight;
    }

    public void setTextCursorDrawable(Drawable textCursorDrawable) {
        this.mCursorDrawable = textCursorDrawable;
        this.mCursorDrawableRes = 0;
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.loadCursorDrawable();
        }
    }

    public void setTextCursorDrawable(int textCursorDrawable) {
        setTextCursorDrawable(textCursorDrawable != 0 ? this.mContext.getDrawable(textCursorDrawable) : null);
    }

    public Drawable getTextCursorDrawable() {
        if (this.mCursorDrawable == null && this.mCursorDrawableRes != 0) {
            this.mCursorDrawable = this.mContext.getDrawable(this.mCursorDrawableRes);
        }
        return this.mCursorDrawable;
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

    /* access modifiers changed from: private */
    public static class TextAppearanceAttributes {
        boolean mAllCaps;
        boolean mElegant;
        boolean mFallbackLineSpacing;
        String mFontFamily;
        boolean mFontFamilyExplicit;
        String mFontFeatureSettings;
        Typeface mFontTypeface;
        String mFontVariationSettings;
        int mFontWeight;
        boolean mHasElegant;
        boolean mHasFallbackLineSpacing;
        boolean mHasLetterSpacing;
        float mLetterSpacing;
        int mShadowColor;
        float mShadowDx;
        float mShadowDy;
        float mShadowRadius;
        ColorStateList mTextColor;
        int mTextColorHighlight;
        ColorStateList mTextColorHint;
        ColorStateList mTextColorLink;
        LocaleList mTextLocales;
        int mTextSize;
        int mTextStyle;
        int mTypefaceIndex;

        private TextAppearanceAttributes() {
            this.mTextColorHighlight = 0;
            this.mTextColor = null;
            this.mTextColorHint = null;
            this.mTextColorLink = null;
            this.mTextSize = -1;
            this.mTextLocales = null;
            this.mFontFamily = null;
            this.mFontTypeface = null;
            this.mFontFamilyExplicit = false;
            this.mTypefaceIndex = -1;
            this.mTextStyle = 0;
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
            this.mFontVariationSettings = null;
        }

        public String toString() {
            return "TextAppearanceAttributes {\n    mTextColorHighlight:" + this.mTextColorHighlight + "\n    mTextColor:" + this.mTextColor + "\n    mTextColorHint:" + this.mTextColorHint + "\n    mTextColorLink:" + this.mTextColorLink + "\n    mTextSize:" + this.mTextSize + "\n    mTextLocales:" + this.mTextLocales + "\n    mFontFamily:" + this.mFontFamily + "\n    mFontTypeface:" + this.mFontTypeface + "\n    mFontFamilyExplicit:" + this.mFontFamilyExplicit + "\n    mTypefaceIndex:" + this.mTypefaceIndex + "\n    mTextStyle:" + this.mTextStyle + "\n    mFontWeight:" + this.mFontWeight + "\n    mAllCaps:" + this.mAllCaps + "\n    mShadowColor:" + this.mShadowColor + "\n    mShadowDx:" + this.mShadowDx + "\n    mShadowDy:" + this.mShadowDy + "\n    mShadowRadius:" + this.mShadowRadius + "\n    mHasElegant:" + this.mHasElegant + "\n    mElegant:" + this.mElegant + "\n    mHasFallbackLineSpacing:" + this.mHasFallbackLineSpacing + "\n    mFallbackLineSpacing:" + this.mFallbackLineSpacing + "\n    mHasLetterSpacing:" + this.mHasLetterSpacing + "\n    mLetterSpacing:" + this.mLetterSpacing + "\n    mFontFeatureSettings:" + this.mFontFeatureSettings + "\n    mFontVariationSettings:" + this.mFontVariationSettings + "\n}";
        }
    }

    private void readTextAppearance(Context context, TypedArray appearance, TextAppearanceAttributes attributes, boolean styleArray) {
        int n = appearance.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = appearance.getIndex(i);
            int index = attr;
            if (!styleArray || (index = sAppearanceValues.get(attr, -1)) != -1) {
                switch (index) {
                    case 0:
                        attributes.mTextSize = appearance.getDimensionPixelSize(attr, attributes.mTextSize);
                        continue;
                    case 1:
                        attributes.mTypefaceIndex = appearance.getInt(attr, attributes.mTypefaceIndex);
                        if (attributes.mTypefaceIndex != -1 && !attributes.mFontFamilyExplicit) {
                            attributes.mFontFamily = null;
                            break;
                        }
                    case 2:
                        attributes.mTextStyle = appearance.getInt(attr, attributes.mTextStyle);
                        continue;
                    case 3:
                        attributes.mTextColor = appearance.getColorStateList(attr);
                        continue;
                    case 4:
                        attributes.mTextColorHighlight = appearance.getColor(attr, attributes.mTextColorHighlight);
                        continue;
                    case 5:
                        attributes.mTextColorHint = appearance.getColorStateList(attr);
                        continue;
                    case 6:
                        attributes.mTextColorLink = appearance.getColorStateList(attr);
                        continue;
                    case 7:
                        attributes.mShadowColor = appearance.getInt(attr, attributes.mShadowColor);
                        continue;
                    case 8:
                        attributes.mShadowDx = appearance.getFloat(attr, attributes.mShadowDx);
                        continue;
                    case 9:
                        attributes.mShadowDy = appearance.getFloat(attr, attributes.mShadowDy);
                        continue;
                    case 10:
                        attributes.mShadowRadius = appearance.getFloat(attr, attributes.mShadowRadius);
                        continue;
                    case 11:
                        attributes.mAllCaps = appearance.getBoolean(attr, attributes.mAllCaps);
                        continue;
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
                        continue;
                    case 13:
                        attributes.mHasElegant = true;
                        attributes.mElegant = appearance.getBoolean(attr, attributes.mElegant);
                        continue;
                    case 14:
                        attributes.mHasLetterSpacing = true;
                        attributes.mLetterSpacing = appearance.getFloat(attr, attributes.mLetterSpacing);
                        continue;
                    case 15:
                        attributes.mFontFeatureSettings = appearance.getString(attr);
                        continue;
                    case 16:
                        attributes.mFontVariationSettings = appearance.getString(attr);
                        continue;
                    case 17:
                        attributes.mHasFallbackLineSpacing = true;
                        attributes.mFallbackLineSpacing = appearance.getBoolean(attr, attributes.mFallbackLineSpacing);
                        continue;
                    case 18:
                        attributes.mFontWeight = appearance.getInt(attr, attributes.mFontWeight);
                        continue;
                    case 19:
                        String localeString = appearance.getString(attr);
                        if (localeString != null) {
                            LocaleList localeList = LocaleList.forLanguageTags(localeString);
                            if (!localeList.isEmpty()) {
                                attributes.mTextLocales = localeList;
                                break;
                            } else {
                                break;
                            }
                        } else {
                            continue;
                        }
                }
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
        if (attributes.mTextSize != -1) {
            setRawTextSize((float) attributes.mTextSize, true);
        }
        if (attributes.mTextLocales != null) {
            setTextLocales(attributes.mTextLocales);
        }
        if (attributes.mTypefaceIndex != -1 && !attributes.mFontFamilyExplicit) {
            attributes.mFontFamily = null;
        }
        setTypefaceFromAttrs(attributes.mFontTypeface, attributes.mFontFamily, attributes.mTypefaceIndex, attributes.mTextStyle, attributes.mFontWeight);
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
        if (attributes.mFontVariationSettings != null) {
            setFontVariationSettings(attributes.mFontVariationSettings);
        }
    }

    public Locale getTextLocale() {
        return this.mTextPaint.getTextLocale();
    }

    public LocaleList getTextLocales() {
        return this.mTextPaint.getTextLocales();
    }

    private void changeListenerLocaleTo(Locale locale) {
        Editor editor;
        KeyListener listener;
        if (!this.mListenerChanged && (editor = this.mEditor) != null) {
            KeyListener listener2 = editor.mKeyListener;
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
    @Override // android.view.View
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

    @UnsupportedAppUsage
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
        return new PrecomputedText.Params(new TextPaint(this.mTextPaint), getTextDirectionHeuristic(), this.mBreakStrategy, this.mHyphenationFrequency);
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
        if (fontVariationSettings == existingSettings) {
            return true;
        }
        if (fontVariationSettings != null && fontVariationSettings.equals(existingSettings)) {
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
        Editor editor = this.mEditor;
        return editor == null || editor.mShowSoftInputOnFocus;
    }

    public void setShadowLayer(float radius, float dx, float dy, int color) {
        this.mTextPaint.setShadowLayer(radius, dx, dy, color);
        this.mShadowRadius = radius;
        this.mShadowDx = dx;
        this.mShadowDy = dy;
        this.mShadowColor = color;
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.invalidateTextDisplayList();
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
        CharSequence charSequence = this.mText;
        if (charSequence instanceof Spanned) {
            return (URLSpan[]) ((Spanned) charSequence).getSpans(0, charSequence.length(), URLSpan.class);
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
        Layout layout = this.mLayout;
        if (layout != null && newLayout) {
            int want = layout.getWidth();
            Layout layout2 = this.mHintLayout;
            int hintWant = layout2 == null ? 0 : layout2.getWidth();
            BoringLayout.Metrics metrics = UNKNOWN_BORING;
            makeNewLayout(want, hintWant, metrics, metrics, ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight(), true);
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

    public final boolean isHorizontallyScrollable() {
        return this.mHorizontallyScrolling;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28)
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
        CharSequence charSequence = this.mText;
        if (!(charSequence instanceof Editable)) {
            setText(charSequence, BufferType.EDITABLE);
        }
        ((Editable) this.mText).append(text, start, end);
        int i = this.mAutoLinkMask;
        if (i != 0 && Linkify.addLinks(this.mSpannable, i) && this.mLinksClickable && !textCanBeSelected()) {
            setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void updateTextColors() {
        int color;
        int color2;
        boolean inval = false;
        int[] drawableState = getDrawableState();
        int color3 = this.mTextColor.getColorForState(drawableState, 0);
        if (color3 != this.mCurTextColor) {
            this.mCurTextColor = color3;
            inval = true;
        }
        ColorStateList colorStateList = this.mLinkTextColor;
        if (!(colorStateList == null || (color2 = colorStateList.getColorForState(drawableState, 0)) == this.mTextPaint.linkColor)) {
            this.mTextPaint.linkColor = color2;
            inval = true;
        }
        ColorStateList colorStateList2 = this.mHintTextColor;
        if (!(colorStateList2 == null || (color = colorStateList2.getColorForState(drawableState, 0)) == this.mCurHintTextColor)) {
            this.mCurHintTextColor = color;
            if (this.mText.length() == 0) {
                inval = true;
            }
        }
        if (inval) {
            Editor editor = this.mEditor;
            if (editor != null) {
                editor.invalidateTextDisplayList();
            }
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void drawableStateChanged() {
        ColorStateList colorStateList;
        ColorStateList colorStateList2;
        super.drawableStateChanged();
        ColorStateList colorStateList3 = this.mTextColor;
        if ((colorStateList3 != null && colorStateList3.isStateful()) || (((colorStateList = this.mHintTextColor) != null && colorStateList.isStateful()) || ((colorStateList2 = this.mLinkTextColor) != null && colorStateList2.isStateful()))) {
            updateTextColors();
        }
        if (this.mDrawables != null) {
            int[] state = getDrawableState();
            Drawable[] drawableArr = this.mDrawables.mShowing;
            for (Drawable dr : drawableArr) {
                if (dr != null && dr.isStateful() && dr.setState(state)) {
                    invalidateDrawable(dr);
                }
            }
        }
    }

    @Override // android.view.View
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        Drawables drawables = this.mDrawables;
        if (drawables != null) {
            Drawable[] drawableArr = drawables.mShowing;
            for (Drawable dr : drawableArr) {
                if (dr != null) {
                    dr.setHotspot(x, y);
                }
            }
        }
    }

    @Override // android.view.View
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
            CharSequence charSequence = this.mText;
            if (charSequence instanceof Spanned) {
                Spannable sp = new SpannableStringBuilder(charSequence);
                if (this.mEditor != null) {
                    removeMisspelledSpans(sp);
                    sp.removeSpan(this.mEditor.mSuggestionRangeSpan);
                }
                ss.text = sp;
            } else {
                ss.text = charSequence.toString();
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
        Editor editor = this.mEditor;
        if (editor != null) {
            ss.editorState = editor.saveInstanceState();
        }
        return ss;
    }

    /* access modifiers changed from: package-private */
    public void removeMisspelledSpans(Spannable spannable) {
        SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) spannable.getSpans(0, spannable.length(), SuggestionSpan.class);
        for (int i = 0; i < suggestionSpans.length; i++) {
            int flags = suggestionSpans[i].getFlags();
            if (!((flags & 1) == 0 || (flags & 2) == 0)) {
                spannable.removeSpan(suggestionSpans[i]);
            }
        }
    }

    @Override // android.view.View
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
                Log.e(LOG_TAG, "Saved cursor position " + ss.selStart + "/" + ss.selEnd + " out of range for " + restored + "text " + ((Object) this.mText));
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
                /* class android.widget.TextView.AnonymousClass1 */

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
        CharWrapper charWrapper = this.mCharWrapper;
        if (charWrapper != null) {
            charWrapper.mChars = null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:102:0x01bd  */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x01d0  */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x01d7  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x01e0  */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x01f0  */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x01ff  */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x0206  */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x020d  */
    /* JADX WARNING: Removed duplicated region for block: B:129:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x009d  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00a1  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0138  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0140  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0183  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0186  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0190  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x01b1 A[LOOP:1: B:98:0x01af->B:99:0x01b1, LOOP_END] */
    @UnsupportedAppUsage
    private void setText(CharSequence text, BufferType type, boolean notifyBefore, int oldlen) {
        CharSequence text2;
        int oldlen2;
        boolean needEditableForNotification;
        TransformationMethod transformationMethod;
        int textLength;
        Editor editor;
        Editor editor2;
        TransformationMethod transformationMethod2;
        MovementMethod movementMethod;
        Spannable s2;
        InputMethodManager imm;
        BufferType type2 = type;
        this.mTextSetFromXmlOrResourceId = false;
        if (text == null) {
            text2 = "";
        } else {
            text2 = text;
        }
        if (!isSuggestionsEnabled()) {
            text2 = removeSuggestionSpans(text2);
        }
        if (!this.mUserSetTextScaleX) {
            this.mTextPaint.setTextScaleX(1.0f);
        }
        if ((text2 instanceof Spanned) && ((Spanned) text2).getSpanStart(TextUtils.TruncateAt.MARQUEE) >= 0) {
            if (ViewConfiguration.get(this.mContext).isFadingMarqueeEnabled()) {
                setHorizontalFadingEdgeEnabled(true);
                this.mMarqueeFadeMode = 0;
            } else {
                setHorizontalFadingEdgeEnabled(false);
                this.mMarqueeFadeMode = 1;
            }
            setEllipsize(TextUtils.TruncateAt.MARQUEE);
        }
        int n = this.mFilters.length;
        for (int i = 0; i < n; i++) {
            CharSequence out = this.mFilters[i].filter(text2, 0, text2.length(), EMPTY_SPANNED, 0, 0);
            if (out != null) {
                text2 = out;
            }
        }
        if (notifyBefore) {
            CharSequence charSequence = this.mText;
            if (charSequence != null) {
                oldlen2 = charSequence.length();
                sendBeforeTextChanged(this.mText, 0, oldlen2, text2.length());
                needEditableForNotification = false;
                ArrayList<TextWatcher> arrayList = this.mListeners;
                if (!(arrayList == null || arrayList.size() == 0)) {
                    needEditableForNotification = true;
                }
                PrecomputedText precomputed = !(text2 instanceof PrecomputedText) ? (PrecomputedText) text2 : null;
                if (type2 != BufferType.EDITABLE || getKeyListener() != null || needEditableForNotification) {
                    createEditorIfNeeded();
                    this.mEditor.forgetUndoRedo();
                    Editable t = this.mEditableFactory.newEditable(text2);
                    text2 = t;
                    setFilters(t, this.mFilters);
                    imm = getInputMethodManager();
                    if (imm != null) {
                        imm.restartInput(this);
                    }
                } else if (precomputed != null) {
                    if (this.mTextDir == null) {
                        this.mTextDir = getTextDirectionHeuristic();
                    }
                    int checkResult = precomputed.getParams().checkResultUsable(getPaint(), this.mTextDir, this.mBreakStrategy, this.mHyphenationFrequency);
                    if (checkResult == 0) {
                        throw new IllegalArgumentException("PrecomputedText's Parameters don't match the parameters of this TextView.Consider using setTextMetricsParams(precomputedText.getParams()) to override the settings of this TextView: PrecomputedText: " + precomputed.getParams() + "TextView: " + getTextMetricsParams());
                    } else if (checkResult == 1) {
                        PrecomputedText.create(precomputed, getTextMetricsParams());
                    }
                } else if (type2 == BufferType.SPANNABLE || this.mMovement != null) {
                    text2 = this.mSpannableFactory.newSpannable(text2);
                } else if (!(text2 instanceof CharWrapper)) {
                    text2 = TextUtils.stringOrSpannedString(text2);
                }
                if (this.mAutoLinkMask != 0) {
                    if (type2 == BufferType.EDITABLE || (text2 instanceof Spannable)) {
                        s2 = (Spannable) text2;
                    } else {
                        s2 = this.mSpannableFactory.newSpannable(text2);
                    }
                    if (Linkify.addLinks(s2, this.mAutoLinkMask)) {
                        text2 = s2;
                        type2 = type2 == BufferType.EDITABLE ? BufferType.EDITABLE : BufferType.SPANNABLE;
                        setTextInternal(text2);
                        if (this.mLinksClickable && !textCanBeSelected()) {
                            setMovementMethod(LinkMovementMethod.getInstance());
                        }
                    }
                }
                this.mBufferType = type2;
                setTextInternal(text2);
                transformationMethod = this.mTransformation;
                if (transformationMethod != null) {
                    this.mTransformed = text2;
                } else {
                    this.mTransformed = transformationMethod.getTransformation(text2, this);
                }
                if (this.mTransformed == null) {
                    this.mTransformed = "";
                }
                textLength = text2.length();
                if ((text2 instanceof Spannable) && !this.mAllowTransformationLengthChange) {
                    Spannable sp = (Spannable) text2;
                    for (ChangeWatcher changeWatcher : (ChangeWatcher[]) sp.getSpans(0, sp.length(), ChangeWatcher.class)) {
                        sp.removeSpan(changeWatcher);
                    }
                    if (this.mChangeWatcher == null) {
                        this.mChangeWatcher = new ChangeWatcher();
                    }
                    sp.setSpan(this.mChangeWatcher, 0, textLength, 6553618);
                    editor2 = this.mEditor;
                    if (editor2 != null) {
                        editor2.addSpanWatchers(sp);
                    }
                    transformationMethod2 = this.mTransformation;
                    if (transformationMethod2 != null) {
                        sp.setSpan(transformationMethod2, 0, textLength, 18);
                    }
                    movementMethod = this.mMovement;
                    if (movementMethod != null) {
                        movementMethod.initialize(this, (Spannable) text2);
                        Editor editor3 = this.mEditor;
                        if (editor3 != null) {
                            editor3.mSelectionMoved = false;
                        }
                    }
                }
                if (this.mLayout != null) {
                    checkForRelayout();
                }
                sendOnTextChanged(text2, 0, oldlen2, textLength);
                onTextChanged(text2, 0, oldlen2, textLength);
                notifyViewAccessibilityStateChangedIfNeeded(2);
                if (!needEditableForNotification) {
                    sendAfterTextChanged((Editable) text2);
                } else {
                    notifyListeningManagersAfterTextChanged();
                }
                editor = this.mEditor;
                if (editor == null) {
                    editor.prepareCursorControllers();
                    return;
                }
                return;
            }
            sendBeforeTextChanged("", 0, 0, text2.length());
        }
        oldlen2 = oldlen;
        needEditableForNotification = false;
        ArrayList<TextWatcher> arrayList2 = this.mListeners;
        needEditableForNotification = true;
        if (!(text2 instanceof PrecomputedText)) {
        }
        if (type2 != BufferType.EDITABLE) {
        }
        createEditorIfNeeded();
        this.mEditor.forgetUndoRedo();
        Editable t2 = this.mEditableFactory.newEditable(text2);
        text2 = t2;
        setFilters(t2, this.mFilters);
        imm = getInputMethodManager();
        if (imm != null) {
        }
        if (this.mAutoLinkMask != 0) {
        }
        this.mBufferType = type2;
        setTextInternal(text2);
        transformationMethod = this.mTransformation;
        if (transformationMethod != null) {
        }
        if (this.mTransformed == null) {
        }
        textLength = text2.length();
        Spannable sp2 = (Spannable) text2;
        while (i < count) {
        }
        if (this.mChangeWatcher == null) {
        }
        sp2.setSpan(this.mChangeWatcher, 0, textLength, 6553618);
        editor2 = this.mEditor;
        if (editor2 != null) {
        }
        transformationMethod2 = this.mTransformation;
        if (transformationMethod2 != null) {
        }
        movementMethod = this.mMovement;
        if (movementMethod != null) {
        }
        if (this.mLayout != null) {
        }
        sendOnTextChanged(text2, 0, oldlen2, textLength);
        onTextChanged(text2, 0, oldlen2, textLength);
        notifyViewAccessibilityStateChangedIfNeeded(2);
        if (!needEditableForNotification) {
        }
        editor = this.mEditor;
        if (editor == null) {
        }
    }

    public final void setText(char[] text, int start, int len) {
        int oldlen = 0;
        if (start < 0 || len < 0 || start + len > text.length) {
            throw new IndexOutOfBoundsException(start + ", " + len);
        }
        CharSequence charSequence = this.mText;
        if (charSequence != null) {
            oldlen = charSequence.length();
            sendBeforeTextChanged(this.mText, 0, oldlen, len);
        } else {
            sendBeforeTextChanged("", 0, 0, len);
        }
        CharWrapper charWrapper = this.mCharWrapper;
        if (charWrapper == null) {
            this.mCharWrapper = new CharWrapper(text, start, len);
        } else {
            charWrapper.set(text, start, len);
        }
        setText(this.mCharWrapper, this.mBufferType, false, oldlen);
    }

    public final void setTextKeepState(CharSequence text, BufferType type) {
        Spannable spannable;
        int start = getSelectionStart();
        int end = getSelectionEnd();
        int len = text.length();
        setText(text, type);
        if ((start >= 0 || end >= 0) && (spannable = this.mSpannable) != null) {
            Selection.setSelection(spannable, Math.max(0, Math.min(start, len)), Math.max(0, Math.min(end, len)));
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
            SuggestionSpan[] spans = (SuggestionSpan[]) spannable.getSpans(0, text.length(), SuggestionSpan.class);
            if (spans.length == 0) {
                return text;
            }
            text = spannable;
            for (SuggestionSpan suggestionSpan : spans) {
                spannable.removeSpan(suggestionSpan);
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
        InputMethodManager imm = getInputMethodManager();
        if (imm != null) {
            imm.restartInput(this);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasPasswordTransformationMethod() {
        return this.mTransformation instanceof PasswordTransformationMethod;
    }

    public boolean isAnyPasswordInputType() {
        int inputType = getInputType();
        return isPasswordInputType(inputType) || isVisiblePasswordInputType(inputType);
    }

    static boolean isPasswordInputType(int inputType) {
        int variation = inputType & 4095;
        return variation == 129 || variation == 225 || variation == 18;
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
        LocaleList locales;
        if (this.mUseInternationalizedInput && (locales = getImeHintLocales()) != null) {
            return locales.get(0);
        }
        return null;
    }

    @UnsupportedAppUsage
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
        Editor editor = this.mEditor;
        if (editor == null) {
            return 0;
        }
        return editor.mInputType;
    }

    public void setImeOptions(int imeOptions) {
        createEditorIfNeeded();
        this.mEditor.createInputContentTypeIfNeeded();
        this.mEditor.mInputContentType.imeOptions = imeOptions;
    }

    public int getImeOptions() {
        Editor editor = this.mEditor;
        if (editor == null || editor.mInputContentType == null) {
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
        Editor editor = this.mEditor;
        if (editor == null || editor.mInputContentType == null) {
            return null;
        }
        return this.mEditor.mInputContentType.imeActionLabel;
    }

    public int getImeActionId() {
        Editor editor = this.mEditor;
        if (editor == null || editor.mInputContentType == null) {
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
        Editor editor = this.mEditor;
        Editor.InputContentType ict = editor == null ? null : editor.mInputContentType;
        if (ict != null) {
            if (ict.onEditorActionListener != null && ict.onEditorActionListener.onEditorAction(this, actionCode, null)) {
                return;
            }
            if (actionCode == 5) {
                View v = focusSearch(2);
                if (v != null && !v.requestFocus(2)) {
                    throw new IllegalStateException("focus search returned a view that wasn't able to take focus!");
                }
                return;
            } else if (actionCode == 7) {
                View v2 = focusSearch(1);
                if (v2 != null && !v2.requestFocus(1)) {
                    throw new IllegalStateException("focus search returned a view that wasn't able to take focus!");
                }
                return;
            } else if (actionCode == 6) {
                InputMethodManager imm = getInputMethodManager();
                if (imm != null && imm.isActive(this)) {
                    imm.hideSoftInputFromWindow(getWindowToken(), 0);
                    return;
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
        Editor editor = this.mEditor;
        if (editor == null || editor.mInputContentType == null) {
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
        Editor editor = this.mEditor;
        if (editor == null || editor.mInputContentType == null) {
            return null;
        }
        return this.mEditor.mInputContentType.imeHintLocales;
    }

    public CharSequence getError() {
        Editor editor = this.mEditor;
        if (editor == null) {
            return null;
        }
        return editor.mError;
    }

    @RemotableViewMethod
    public void setError(CharSequence error) {
        if (error == null) {
            setError(null, null);
            return;
        }
        HwWidgetFactory.HwTextView hwTextView = this.mHwTextView;
        if (hwTextView != null) {
            hwTextView.setError(this, getContext(), error);
            return;
        }
        Drawable dr = getContext().getDrawable(R.drawable.indicator_input_error);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        setError(error, dr);
    }

    public void setError(CharSequence error, Drawable icon) {
        createEditorIfNeeded();
        this.mEditor.setError(error, icon);
        notifyViewAccessibilityStateChangedIfNeeded(0);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean setFrame(int l, int t, int r, int b) {
        boolean result = super.setFrame(l, t, r, b);
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.setFrame();
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
            CharSequence charSequence = this.mText;
            if (charSequence instanceof Editable) {
                setFilters((Editable) charSequence, filters);
                return;
            }
            return;
        }
        throw new IllegalArgumentException();
    }

    private void setFilters(Editable e, InputFilter[] filters) {
        Editor editor = this.mEditor;
        if (editor != null) {
            boolean undoFilter = editor.mUndoInputFilter != null;
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
    @UnsupportedAppUsage
    public int getVerticalOffset(boolean forceNormal) {
        int boxht;
        int textht;
        int gravity = this.mGravity & 112;
        Layout l = this.mLayout;
        if (!forceNormal && this.mText.length() == 0 && this.mHintLayout != null) {
            l = this.mHintLayout;
        }
        if (gravity == 48 || (textht = l.getHeight()) >= (boxht = getBoxHeight(l))) {
            return 0;
        }
        if (gravity == 80) {
            return boxht - textht;
        }
        return (boxht - textht) >> 1;
    }

    private int getBottomVerticalOffset(boolean forceNormal) {
        int boxht;
        int textht;
        int gravity = this.mGravity & 112;
        Layout l = this.mLayout;
        if (!forceNormal && this.mText.length() == 0 && this.mHintLayout != null) {
            l = this.mHintLayout;
        }
        if (gravity == 80 || (textht = l.getHeight()) >= (boxht = getBoxHeight(l))) {
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
        Editor editor;
        Layout layout = this.mLayout;
        if (layout == null) {
            invalidate();
            return;
        }
        int lineStart = layout.getLineForOffset(start);
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
        if (!(!invalidateCursor || (editor = this.mEditor) == null || editor.mDrawableForCursor == null)) {
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

    @Override // android.view.ViewTreeObserver.OnPreDrawListener
    public boolean onPreDraw() {
        if (this.mLayout == null) {
            assumeLayout();
        }
        if (this.mMovement != null) {
            int curs = getSelectionEnd();
            Editor editor = this.mEditor;
            if (!(editor == null || editor.mSelectionModifierCursorController == null || !this.mEditor.mSelectionModifierCursorController.isSelectionStartDragged())) {
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
        Editor editor2 = this.mEditor;
        if (editor2 != null && editor2.mCreatedWithASelection) {
            this.mEditor.refreshTextActionMode();
            this.mEditor.mCreatedWithASelection = false;
        }
        unregisterForPreDraw();
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.onAttachedToWindow();
        }
        if (this.mPreDrawListenerDetached) {
            getViewTreeObserver().addOnPreDrawListener(this);
            this.mPreDrawListenerDetached = false;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindowInternal() {
        if (this.mPreDrawRegistered) {
            getViewTreeObserver().removeOnPreDrawListener(this);
            this.mPreDrawListenerDetached = true;
        }
        resetResolvedDrawables();
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.onDetachedFromWindow();
        }
        super.onDetachedFromWindowInternal();
    }

    @Override // android.view.View
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.onScreenStateChanged(screenState);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean isPaddingOffsetRequired() {
        return (this.mShadowRadius == 0.0f && this.mDrawables == null) ? false : true;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int getLeftPaddingOffset() {
        return (getCompoundPaddingLeft() - this.mPaddingLeft) + ((int) Math.min(0.0f, this.mShadowDx - this.mShadowRadius));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int getTopPaddingOffset() {
        return (int) Math.min(0.0f, this.mShadowDy - this.mShadowRadius);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int getBottomPaddingOffset() {
        return (int) Math.max(0.0f, this.mShadowDy + this.mShadowRadius);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int getRightPaddingOffset() {
        return (-(getCompoundPaddingRight() - this.mPaddingRight)) + ((int) Math.max(0.0f, this.mShadowDx + this.mShadowRadius));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean verifyDrawable(Drawable who) {
        Drawables drawables;
        boolean verified = super.verifyDrawable(who);
        if (!verified && (drawables = this.mDrawables) != null) {
            for (Drawable dr : drawables.mShowing) {
                if (who == dr) {
                    return true;
                }
            }
        }
        return verified;
    }

    @Override // android.view.View
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        Drawables drawables = this.mDrawables;
        if (drawables != null) {
            Drawable[] drawableArr = drawables.mShowing;
            for (Drawable dr : drawableArr) {
                if (dr != null) {
                    dr.jumpToCurrentState();
                }
            }
        }
    }

    @Override // android.graphics.drawable.Drawable.Callback, android.view.View
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

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return ((getBackground() == null || getBackground().getCurrent() == null) && this.mSpannable == null && !hasSelection() && !isHorizontalFadingEdgeEnabled() && this.mShadowColor == 0) ? false : true;
    }

    public boolean isTextSelectable() {
        Editor editor = this.mEditor;
        if (editor == null) {
            return false;
        }
        return editor.mTextIsSelectable;
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
    @Override // android.view.View
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

    @UnsupportedAppUsage
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
        if (selStart == selEnd) {
            Editor editor = this.mEditor;
            if (editor == null || !editor.shouldRenderCursor()) {
                return null;
            }
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
    }

    public int getHorizontalOffsetForDrawables() {
        return 0;
    }

    /* JADX INFO: Multiple debug info for r4v3 int: [D('clipBottom' float), D('cursorOffsetVertical' int)] */
    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        int color;
        int bottom;
        float clipTop;
        float clipTop2;
        float clipRight;
        float clipBottom;
        int voffsetCursor;
        int i;
        float clipBottom2;
        int layoutDirection;
        Layout layout;
        int cursorOffsetVertical;
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
        int bottom2 = this.mBottom;
        int top = this.mTop;
        boolean isLayoutRtl = this.mTextViewDirection != 1;
        int offset = getHorizontalOffsetForDrawables();
        int leftOffset = isRtlLocale() ? 0 : offset;
        int rightOffset = isRtlLocale() ? offset : 0;
        Drawables dr = this.mDrawables;
        if (dr != null) {
            int vspace = ((bottom2 - top) - compoundPaddingBottom) - compoundPaddingTop;
            int hspace = ((right - left) - compoundPaddingRight) - compoundPaddingLeft;
            if (dr.mShowing[0] != null) {
                canvas.save();
                canvas.translate((float) (this.mPaddingLeft + scrollX + leftOffset), (float) (scrollY + compoundPaddingTop + ((vspace - dr.mDrawableHeightLeft) / 2)));
                dr.mShowing[0].draw(canvas);
                canvas.restore();
            }
            if (dr.mShowing[2] != null) {
                canvas.save();
                canvas.translate((float) (((((scrollX + right) - left) - this.mPaddingRight) - dr.mDrawableSizeRight) - rightOffset), (float) (scrollY + compoundPaddingTop + ((vspace - dr.mDrawableHeightRight) / 2)));
                dr.mShowing[2].draw(canvas);
                canvas.restore();
            }
            if (dr.mShowing[1] != null) {
                canvas.save();
                canvas.translate((float) (scrollX + compoundPaddingLeft + ((hspace - dr.mDrawableWidthTop) / 2)), (float) (this.mPaddingTop + scrollY));
                dr.mShowing[1].draw(canvas);
                canvas.restore();
            }
            if (dr.mShowing[3] != null) {
                canvas.save();
                canvas.translate((float) (scrollX + compoundPaddingLeft + ((hspace - dr.mDrawableWidthBottom) / 2)), (float) ((((scrollY + bottom2) - top) - this.mPaddingBottom) - dr.mDrawableSizeBottom));
                dr.mShowing[3].draw(canvas);
                canvas.restore();
            }
        }
        int color2 = this.mCurTextColor;
        if (this.mLayout == null) {
            assumeLayout();
        }
        Layout layout2 = this.mLayout;
        if (this.mHint == null || this.mText.length() != 0) {
            color = color2;
        } else {
            if (this.mHintTextColor != null) {
                color2 = this.mCurHintTextColor;
            }
            layout2 = this.mHintLayout;
            color = color2;
        }
        this.mTextPaint.setColor(color);
        this.mTextPaint.drawableState = getDrawableState();
        canvas.save();
        int extendedPaddingTop = getExtendedPaddingTop();
        int extendedPaddingBottom = getExtendedPaddingBottom();
        int maxScrollY = this.mLayout.getHeight() - (((this.mBottom - this.mTop) - compoundPaddingBottom) - compoundPaddingTop);
        float clipLeft = (float) (compoundPaddingLeft + scrollX);
        float clipTop3 = scrollY == 0 ? 0.0f : (float) (extendedPaddingTop + scrollY);
        float clipRight2 = (float) (((right - left) - getCompoundPaddingRight()) + scrollX);
        float clipBottom3 = (float) (((bottom2 - top) + scrollY) - (scrollY == maxScrollY ? 0 : extendedPaddingBottom));
        float f = this.mShadowRadius;
        if (f != 0.0f) {
            bottom = bottom2;
            float clipLeft2 = clipLeft + Math.min(0.0f, this.mShadowDx - f);
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
        canvas.clipRect(clipTop, clipTop2, clipRight, clipBottom);
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
        canvas.translate((float) compoundPaddingLeft, (float) (extendedPaddingTop + voffsetText));
        int layoutDirection2 = this.mTextViewDirection == i ? 0 : 1;
        int absoluteGravity = Gravity.getAbsoluteGravity(this.mGravity, layoutDirection2);
        if (isMarqueeFadeEnabled()) {
            if (this.mSingleLine || getLineCount() != 1 || !canMarquee() || (absoluteGravity & 7) == 3) {
                layoutDirection = layoutDirection2;
                clipBottom2 = clipBottom;
            } else {
                layoutDirection = layoutDirection2;
                clipBottom2 = clipBottom;
                float dx = this.mLayout.getLineRight(0) - ((float) ((this.mRight - this.mLeft) - (getCompoundPaddingLeft() + getCompoundPaddingRight())));
                canvas.translate(isLayoutRtl ? -dx : dx, 0.0f);
            }
            Marquee marquee = this.mMarquee;
            if (marquee != null && marquee.isRunning()) {
                float dx2 = -this.mMarquee.getScroll();
                canvas.translate(isLayoutRtl ? -dx2 : dx2, 0.0f);
            }
        } else {
            layoutDirection = layoutDirection2;
            clipBottom2 = clipBottom;
        }
        int cursorOffsetVertical2 = voffsetCursor - voffsetText;
        Path highlight = getUpdatedHighlightPath();
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.onDraw(canvas, layout2, highlight, this.mHighlightPaint, cursorOffsetVertical2);
            layout = layout2;
            highlight = highlight;
            cursorOffsetVertical = cursorOffsetVertical2;
        } else {
            layout = layout2;
            cursorOffsetVertical = cursorOffsetVertical2;
            layout.draw(canvas, highlight, this.mHighlightPaint, cursorOffsetVertical);
        }
        Marquee marquee2 = this.mMarquee;
        if (marquee2 != null && marquee2.shouldDrawGhost()) {
            float dx3 = this.mMarquee.getGhostOffset();
            canvas.translate(isLayoutRtl ? -dx3 : dx3, 0.0f);
            layout.draw(canvas, highlight, this.mHighlightPaint, cursorOffsetVertical);
        }
        canvas.restore();
    }

    @Override // android.view.View
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
        Layout layout = this.mLayout;
        if (layout != null) {
            return layout.getLineCount();
        }
        return 0;
    }

    public int getLineBounds(int line, Rect bounds) {
        Layout layout = this.mLayout;
        if (layout == null) {
            if (bounds != null) {
                bounds.set(0, 0, 0, 0);
            }
            return 0;
        }
        int baseline = layout.getLineBounds(line, bounds);
        int voffset = getExtendedPaddingTop();
        if ((this.mGravity & 112) != 48) {
            voffset += getVerticalOffset(true);
        }
        if (bounds != null) {
            bounds.offset(getCompoundPaddingLeft(), voffset);
        }
        return baseline + voffset;
    }

    @Override // android.view.View
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
    @Override // android.view.View
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
    @Override // android.view.View
    public int getFadeHeight(boolean offsetRequired) {
        Layout layout = this.mLayout;
        if (layout != null) {
            return layout.getHeight();
        }
        return 0;
    }

    @Override // android.view.View
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

    @Override // android.view.View
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode != 4 || !handleBackInTextActionModeIfNeeded(event)) {
            return super.onKeyPreIme(keyCode, event);
        }
        return true;
    }

    public boolean handleBackInTextActionModeIfNeeded(KeyEvent event) {
        Editor editor = this.mEditor;
        if (editor == null || editor.getTextActionMode() == null) {
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

    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (doKeyDown(keyCode, event, null) == 0) {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override // android.view.KeyEvent.Callback, android.view.View
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
        int variation;
        if (getKeyListener() == null) {
            return false;
        }
        if (this.mSingleLine) {
            return true;
        }
        Editor editor = this.mEditor;
        if (editor != null && (editor.mInputType & 15) == 1 && ((variation = this.mEditor.mInputType & InputType.TYPE_MASK_VARIATION) == 32 || variation == 48)) {
            return true;
        }
        return false;
    }

    private boolean shouldAdvanceFocusOnTab() {
        Editor editor;
        int variation;
        if (getKeyListener() == null || this.mSingleLine || (editor = this.mEditor) == null || (editor.mInputType & 15) != 1 || ((variation = this.mEditor.mInputType & InputType.TYPE_MASK_VARIATION) != 262144 && variation != 131072)) {
            return true;
        }
        return false;
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
        if (event.getRepeatCount() == 0 && !KeyEvent.isModifierKey(keyCode)) {
            this.mPreventDefaultMovement = false;
        }
        if (keyCode == 4) {
            Editor editor = this.mEditor;
            if (!(editor == null || editor.getTextActionMode() == null)) {
                stopTextActionMode();
                return -1;
            }
        } else if (keyCode != 23) {
            if (keyCode != 61) {
                if (keyCode != 66) {
                    if (keyCode != 112) {
                        if (keyCode != 124) {
                            switch (keyCode) {
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
                        } else if (!event.hasModifiers(4096) || !canCopy()) {
                            if (event.hasModifiers(1) && canPaste() && onTextContextMenuItem(16908322)) {
                                return -1;
                            }
                        } else if (onTextContextMenuItem(16908321)) {
                            return -1;
                        }
                    } else if (event.hasModifiers(1) && canCut() && onTextContextMenuItem(16908320)) {
                        return -1;
                    }
                } else if (event.hasNoModifiers()) {
                    Editor editor2 = this.mEditor;
                    if (editor2 != null && editor2.mInputContentType != null && this.mEditor.mInputContentType.onEditorActionListener != null && this.mEditor.mInputContentType.onEditorActionListener.onEditorAction(this, 0, event)) {
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
        Editor editor3 = this.mEditor;
        if (!(editor3 == null || editor3.mKeyListener == null)) {
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
        MovementMethod movementMethod = this.mMovement;
        if (!(movementMethod == null || this.mLayout == null)) {
            boolean doDown2 = true;
            if (otherEvent != null) {
                try {
                    doDown2 = false;
                    if (movementMethod.onKeyOther(this, this.mSpannable, otherEvent)) {
                        return -1;
                    }
                } catch (AbstractMethodError e2) {
                }
            }
            if (!doDown2 || !this.mMovement.onKeyDown(this, this.mSpannable, keyCode, event)) {
                if (event.getSource() == 257 && isDirectionalNavigationKey(keyCode)) {
                    return -1;
                }
            } else if (event.getRepeatCount() != 0 || KeyEvent.isModifierKey(keyCode)) {
                return 2;
            } else {
                this.mPreventDefaultMovement = true;
                return 2;
            }
        }
        if (!this.mPreventDefaultMovement || KeyEvent.isModifierKey(keyCode)) {
            return 0;
        }
        return -1;
    }

    public void resetErrorChangedFlag() {
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.mErrorWasChanged = false;
        }
    }

    public void hideErrorIfUnchanged() {
        Editor editor = this.mEditor;
        if (editor != null && editor.mError != null && !this.mEditor.mErrorWasChanged) {
            setError(null, null);
        }
    }

    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        InputMethodManager imm;
        if (!isEnabled()) {
            return super.onKeyUp(keyCode, event);
        }
        if (!KeyEvent.isModifierKey(keyCode)) {
            this.mPreventDefaultMovement = false;
        }
        if (keyCode == 23) {
            if (event.hasNoModifiers() && !hasOnClickListeners() && this.mMovement != null && (this.mText instanceof Editable) && this.mLayout != null && onCheckIsTextEditor()) {
                InputMethodManager imm2 = getInputMethodManager();
                viewClicked(imm2);
                if (imm2 != null && getShowSoftInputOnFocus()) {
                    imm2.showSoftInput(this, 0);
                }
            }
            return super.onKeyUp(keyCode, event);
        } else if (keyCode == 66 && event.hasNoModifiers()) {
            Editor editor = this.mEditor;
            if (!(editor == null || editor.mInputContentType == null || this.mEditor.mInputContentType.onEditorActionListener == null || !this.mEditor.mInputContentType.enterDown)) {
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
                } else if (!((event.getFlags() & 16) == 0 || (imm = getInputMethodManager()) == null || !imm.isActive(this))) {
                    imm.hideSoftInputFromWindow(getWindowToken(), 0);
                }
            }
            return super.onKeyUp(keyCode, event);
        } else {
            Editor editor2 = this.mEditor;
            if (editor2 != null && editor2.mKeyListener != null && this.mEditor.mKeyListener.onKeyUp(this, (Editable) this.mText, keyCode, event)) {
                return true;
            }
            MovementMethod movementMethod = this.mMovement;
            if (movementMethod == null || this.mLayout == null || !movementMethod.onKeyUp(this, this.mSpannable, keyCode, event)) {
                return super.onKeyUp(keyCode, event);
            }
            return true;
        }
    }

    @Override // android.view.View
    public boolean onCheckIsTextEditor() {
        Editor editor = this.mEditor;
        return (editor == null || editor.mInputType == 0) ? false : true;
    }

    @Override // android.view.View
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
            outAttrs.targetInputMethodUser = this.mTextOperationUser;
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
        int end;
        int start;
        Editable content = getEditableText();
        if (text.text != null) {
            if (content == null) {
                setText(text.text, BufferType.EDITABLE);
            } else {
                int end2 = content.length();
                if (text.partialStartOffset >= 0) {
                    int N = content.length();
                    int start2 = text.partialStartOffset;
                    if (start2 > N) {
                        start2 = N;
                    }
                    int end3 = text.partialEndOffset;
                    if (end3 > N) {
                        end3 = N;
                    }
                    start = start2;
                    end = end3;
                } else {
                    start = 0;
                    end = end2;
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
        int N2 = sp.length();
        int start3 = text.selectionStart;
        if (start3 < 0) {
            start3 = 0;
        } else if (start3 > N2) {
            start3 = N2;
        }
        int end4 = text.selectionEnd;
        if (end4 < 0) {
            end4 = 0;
        } else if (end4 > N2) {
            end4 = N2;
        }
        Selection.setSelection(sp, start3, end4);
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
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.onCommitCorrection(info);
        }
    }

    public void beginBatchEdit() {
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.beginBatchEdit();
        }
    }

    public void endBatchEdit() {
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.endBatchEdit();
        }
    }

    public void onBeginBatchEdit() {
    }

    public void onEndBatchEdit() {
    }

    public boolean onPrivateIMECommand(String action, Bundle data) {
        return false;
    }

    @UnsupportedAppUsage
    @VisibleForTesting
    public void nullLayouts() {
        Layout layout = this.mLayout;
        if ((layout instanceof BoringLayout) && this.mSavedLayout == null) {
            this.mSavedLayout = (BoringLayout) layout;
        }
        Layout layout2 = this.mHintLayout;
        if ((layout2 instanceof BoringLayout) && this.mSavedHintLayout == null) {
            this.mSavedHintLayout = (BoringLayout) layout2;
        }
        this.mHintLayout = null;
        this.mLayout = null;
        this.mSavedMarqueeModeLayout = null;
        this.mHintBoring = null;
        this.mBoring = null;
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.prepareCursorControllers();
        }
    }

    @UnsupportedAppUsage
    private void assumeLayout() {
        int width = ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        if (width < 1) {
            width = 0;
        }
        if (this.mHorizontallyScrolling) {
            width = 1048576;
        }
        BoringLayout.Metrics metrics = UNKNOWN_BORING;
        makeNewLayout(width, width, metrics, metrics, width, false);
    }

    @UnsupportedAppUsage
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

    /* JADX DEBUG: Multi-variable search result rejected for r1v3, resolved type: int */
    /* JADX DEBUG: Multi-variable search result rejected for r3v2, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r3v3, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r3v4, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r3v6, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r1v12, resolved type: int */
    /* JADX DEBUG: Multi-variable search result rejected for r1v13, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x0221, code lost:
        if (r21 != r22.mLayout.getParagraphDirection(r6)) goto L_0x0229;
     */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x024d  */
    /* JADX WARNING: Removed duplicated region for block: B:128:? A[RETURN, SYNTHETIC] */
    @UnsupportedAppUsage
    @VisibleForTesting
    public void makeNewLayout(int wantWidth, int hintWidth, BoringLayout.Metrics boring, BoringLayout.Metrics hintBoring, int ellipsisWidth, boolean bringIntoView) {
        int wantWidth2;
        int hintWidth2;
        TextUtils.TruncateAt effectiveEllipsize;
        int oldDir;
        int i;
        Layout.Alignment alignment;
        int i2;
        int oldDir2;
        int i3;
        int oldDir3;
        boolean z;
        Editor editor;
        int height;
        BoringLayout.Metrics hintBoring2;
        Layout.Alignment alignment2;
        int i4;
        Layout.Alignment alignment3;
        int i5;
        int oldDir4;
        stopMarquee();
        this.mOldMaximum = this.mMaximum;
        this.mOldMaxMode = this.mMaxMode;
        this.mHighlightPathBogus = true;
        if (wantWidth < 0) {
            wantWidth2 = 0;
        } else {
            wantWidth2 = wantWidth;
        }
        if (hintWidth < 0) {
            hintWidth2 = 0;
        } else {
            hintWidth2 = hintWidth;
        }
        Layout.Alignment alignment4 = getLayoutAlignment();
        boolean testDirChange = this.mSingleLine && this.mLayout != null && (alignment4 == Layout.Alignment.ALIGN_NORMAL || alignment4 == Layout.Alignment.ALIGN_OPPOSITE);
        int oldDir5 = 0;
        if (testDirChange) {
            oldDir5 = this.mLayout.getParagraphDirection(0);
        }
        boolean shouldEllipsize = this.mEllipsize != null && getKeyListener() == null;
        boolean switchEllipsize = this.mEllipsize == TextUtils.TruncateAt.MARQUEE && this.mMarqueeFadeMode != 0;
        TextUtils.TruncateAt effectiveEllipsize2 = this.mEllipsize;
        if (this.mEllipsize == TextUtils.TruncateAt.MARQUEE && this.mMarqueeFadeMode == 1) {
            effectiveEllipsize = TextUtils.TruncateAt.END_SMALL;
        } else {
            effectiveEllipsize = effectiveEllipsize2;
        }
        if (this.mTextDir == null) {
            this.mTextDir = getTextDirectionHeuristic();
        }
        this.mLayout = makeSingleLayout(wantWidth2, boring, ellipsisWidth, alignment4, shouldEllipsize, effectiveEllipsize, effectiveEllipsize == this.mEllipsize);
        if (switchEllipsize) {
            oldDir = oldDir5;
            i = 0;
            alignment = alignment4;
            i2 = 1;
            this.mSavedMarqueeModeLayout = makeSingleLayout(wantWidth2, boring, ellipsisWidth, alignment4, shouldEllipsize, effectiveEllipsize == TextUtils.TruncateAt.MARQUEE ? TextUtils.TruncateAt.END : TextUtils.TruncateAt.MARQUEE, effectiveEllipsize != this.mEllipsize);
        } else {
            oldDir = oldDir5;
            i = 0;
            alignment = alignment4;
            i2 = 1;
        }
        int i6 = this.mEllipsize != null ? i2 : i;
        this.mHintLayout = null;
        if (this.mHint != null) {
            int hintWidth3 = i6 != 0 ? wantWidth2 : hintWidth2;
            if (hintBoring == UNKNOWN_BORING) {
                BoringLayout.Metrics hintBoring3 = BoringLayout.isBoring(this.mHint, this.mTextPaint, this.mTextDir, this.mHintBoring);
                if (hintBoring3 != null) {
                    this.mHintBoring = hintBoring3;
                }
                hintBoring2 = hintBoring3;
            } else {
                hintBoring2 = hintBoring;
            }
            if (hintBoring2 != null) {
                if (hintBoring2.width > hintWidth3) {
                    alignment3 = alignment;
                    i5 = i;
                    oldDir4 = oldDir;
                } else if (i6 == 0 || hintBoring2.width <= ellipsisWidth) {
                    BoringLayout boringLayout = this.mSavedHintLayout;
                    if (boringLayout != null) {
                        oldDir2 = oldDir;
                        alignment2 = alignment;
                        i4 = i;
                        this.mHintLayout = boringLayout.replaceOrMake(this.mHint, this.mTextPaint, hintWidth3, alignment, this.mSpacingMult, this.mSpacingAdd, hintBoring2, this.mIncludePad);
                    } else {
                        alignment2 = alignment;
                        i4 = i;
                        oldDir2 = oldDir;
                        this.mHintLayout = BoringLayout.make(this.mHint, this.mTextPaint, hintWidth3, alignment2, this.mSpacingMult, this.mSpacingAdd, hintBoring2, this.mIncludePad);
                    }
                    this.mSavedHintLayout = (BoringLayout) this.mHintLayout;
                } else {
                    alignment3 = alignment;
                    i5 = i;
                    oldDir4 = oldDir;
                }
                if (i6 != 0 && hintBoring2.width <= hintWidth3) {
                    BoringLayout boringLayout2 = this.mSavedHintLayout;
                    if (boringLayout2 != null) {
                        this.mHintLayout = boringLayout2.replaceOrMake(this.mHint, this.mTextPaint, hintWidth3, alignment2, this.mSpacingMult, this.mSpacingAdd, hintBoring2, this.mIncludePad, this.mEllipsize, ellipsisWidth);
                    } else {
                        this.mHintLayout = BoringLayout.make(this.mHint, this.mTextPaint, hintWidth3, alignment2, this.mSpacingMult, this.mSpacingAdd, hintBoring2, this.mIncludePad, this.mEllipsize, ellipsisWidth);
                    }
                }
            } else {
                alignment2 = alignment;
                i4 = i;
                oldDir2 = oldDir;
            }
            if (this.mHintLayout == null) {
                CharSequence charSequence = this.mHint;
                z = 1;
                StaticLayout.Builder builder = StaticLayout.Builder.obtain(charSequence, i4, charSequence.length(), this.mTextPaint, hintWidth3).setAlignment(alignment2).setTextDirection(this.mTextDir).setLineSpacing(this.mSpacingAdd, this.mSpacingMult).setIncludePad(this.mIncludePad).setUseLineSpacingFromFallbacks(this.mUseFallbackLineSpacing).setBreakStrategy(this.mBreakStrategy).setHyphenationFrequency(this.mHyphenationFrequency).setJustificationMode(this.mJustificationMode).setMaxLines(this.mMaxMode == 1 ? this.mMaximum : Integer.MAX_VALUE);
                if (i6 != 0) {
                    oldDir3 = ellipsisWidth;
                    i3 = i4;
                    builder.setEllipsize(this.mEllipsize).setEllipsizedWidth(oldDir3);
                } else {
                    oldDir3 = ellipsisWidth;
                    i3 = i4;
                }
                this.mHintLayout = builder.build();
            } else {
                oldDir3 = ellipsisWidth;
                i3 = i4;
                z = 1;
            }
        } else {
            i3 = i;
            oldDir2 = oldDir;
            oldDir3 = ellipsisWidth;
            z = i2;
        }
        if (!bringIntoView) {
            if (testDirChange) {
            }
            if (this.mEllipsize == TextUtils.TruncateAt.MARQUEE && !compressText((float) oldDir3)) {
                height = this.mLayoutParams.height;
                if (height != -2 || height == -1) {
                    this.mRestartMarquee = z;
                } else {
                    startMarquee();
                }
            }
            editor = this.mEditor;
            if (editor == null) {
                editor.prepareCursorControllers();
                return;
            }
            return;
        }
        registerForPreDraw();
        height = this.mLayoutParams.height;
        if (height != -2) {
        }
        this.mRestartMarquee = z;
        editor = this.mEditor;
        if (editor == null) {
        }
    }

    @VisibleForTesting
    public boolean useDynamicLayout() {
        return isTextSelectable() || (this.mSpannable != null && this.mPrecomputed == null);
    }

    /* access modifiers changed from: protected */
    public Layout makeSingleLayout(int wantWidth, BoringLayout.Metrics boring, int ellipsisWidth, Layout.Alignment alignment, boolean shouldEllipsize, TextUtils.TruncateAt effectiveEllipsize, boolean useSaved) {
        BoringLayout.Metrics boring2;
        BoringLayout boringLayout;
        BoringLayout boringLayout2;
        Layout result = null;
        if (useDynamicLayout()) {
            result = DynamicLayout.Builder.obtain(this.mText, this.mTextPaint, wantWidth).setDisplayText(this.mTransformed).setAlignment(alignment).setTextDirection(this.mTextDir).setLineSpacing(this.mSpacingAdd, this.mSpacingMult).setIncludePad(this.mIncludePad).setUseLineSpacingFromFallbacks(this.mUseFallbackLineSpacing).setBreakStrategy(this.mBreakStrategy).setHyphenationFrequency(this.mHyphenationFrequency).setJustificationMode(this.mJustificationMode).setEllipsize(getKeyListener() == null ? effectiveEllipsize : null).setEllipsizedWidth(ellipsisWidth).build();
        } else {
            if (boring == UNKNOWN_BORING) {
                BoringLayout.Metrics boring3 = BoringLayout.isBoring(this.mTransformed, this.mTextPaint, this.mTextDir, this.mBoring);
                if (boring3 != null) {
                    this.mBoring = boring3;
                }
                boring2 = boring3;
            } else {
                boring2 = boring;
            }
            if (boring2 != null) {
                if (boring2.width <= wantWidth && (effectiveEllipsize == null || boring2.width <= ellipsisWidth)) {
                    if (!useSaved || (boringLayout2 = this.mSavedLayout) == null) {
                        result = BoringLayout.make(this.mTransformed, this.mTextPaint, wantWidth, alignment, this.mSpacingMult, this.mSpacingAdd, boring2, this.mIncludePad);
                    } else {
                        result = boringLayout2.replaceOrMake(this.mTransformed, this.mTextPaint, wantWidth, alignment, this.mSpacingMult, this.mSpacingAdd, boring2, this.mIncludePad);
                    }
                    if (useSaved) {
                        this.mSavedLayout = (BoringLayout) result;
                    }
                } else if (shouldEllipsize && boring2.width <= wantWidth) {
                    result = (!useSaved || (boringLayout = this.mSavedLayout) == null) ? BoringLayout.make(this.mTransformed, this.mTextPaint, wantWidth, alignment, this.mSpacingMult, this.mSpacingAdd, boring2, this.mIncludePad, effectiveEllipsize, ellipsisWidth) : boringLayout.replaceOrMake(this.mTransformed, this.mTextPaint, wantWidth, alignment, this.mSpacingMult, this.mSpacingAdd, boring2, this.mIncludePad, effectiveEllipsize, ellipsisWidth);
                }
            }
        }
        if (result != null) {
            return result;
        }
        CharSequence charSequence = this.mTransformed;
        StaticLayout.Builder builder = StaticLayout.Builder.obtain(charSequence, 0, charSequence.length(), this.mTextPaint, wantWidth).setAlignment(alignment).setTextDirection(this.mTextDir).setLineSpacing(this.mSpacingAdd, this.mSpacingMult).setIncludePad(this.mIncludePad).setUseLineSpacingFromFallbacks(this.mUseFallbackLineSpacing).setBreakStrategy(this.mBreakStrategy).setHyphenationFrequency(this.mHyphenationFrequency).setJustificationMode(this.mJustificationMode).setMaxLines(this.mMaxMode == 1 ? this.mMaximum : Integer.MAX_VALUE);
        if (shouldEllipsize) {
            builder.setEllipsize(effectiveEllipsize).setEllipsizedWidth(ellipsisWidth);
        }
        return builder.build();
    }

    @UnsupportedAppUsage
    private boolean compressText(float width) {
        if (!isHardwareAccelerated() && width > 0.0f && this.mLayout != null && getLineCount() == 1 && !this.mUserSetTextScaleX && this.mTextPaint.getTextScaleX() == 1.0f) {
            float overflow = ((this.mLayout.getLineWidth(0) + 1.0f) - width) / width;
            if (overflow > 0.0f && overflow <= 0.07f) {
                this.mTextPaint.setTextScaleX((1.0f - overflow) - 0.005f);
                post(new Runnable() {
                    /* class android.widget.TextView.AnonymousClass2 */

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
            if (text.charAt(layout.getLineEnd(i) - 1) != '\n') {
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
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean fromexisting;
        BoringLayout.Metrics hintBoring;
        BoringLayout.Metrics boring;
        int width;
        int des;
        int unpaddedWidth;
        int unpaddedWidth2;
        int height;
        int i;
        int hintWant;
        boolean layoutChanged;
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
            Layout layout = this.mLayout;
            if (layout != null && this.mEllipsize == null) {
                des2 = desired(layout);
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
                    CharSequence charSequence = this.mTransformed;
                    des2 = (int) Math.ceil((double) Layout.getDesiredWidthWithLimit(charSequence, 0, charSequence.length(), this.mTextPaint, this.mTextDir, widthLimit));
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
                Layout layout2 = this.mHintLayout;
                if (layout2 != null && this.mEllipsize == null) {
                    hintDes = desired(layout2);
                }
                if (hintDes < 0 && (hintBoring2 = BoringLayout.isBoring(this.mHint, this.mTextPaint, this.mTextDir, this.mHintBoring)) != null) {
                    this.mHintBoring = hintBoring2;
                }
                if (hintBoring2 == null || hintBoring2 == UNKNOWN_BORING) {
                    if (hintDes < 0) {
                        CharSequence charSequence2 = this.mHint;
                        hintDes = (int) Math.ceil((double) Layout.getDesiredWidthWithLimit(charSequence2, 0, charSequence2.length(), this.mTextPaint, this.mTextDir, widthLimit));
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
                boring = boring2;
                hintBoring = hintBoring2;
                des = des2;
                fromexisting = fromexisting2;
                width = Math.min(widthSize, width6);
            } else {
                boring = boring2;
                hintBoring = hintBoring2;
                des = des2;
                fromexisting = fromexisting2;
                width = width6;
            }
        }
        int want = (width - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        if (this.mHorizontallyScrolling) {
            want = 1048576;
        }
        Layout layout3 = this.mHintLayout;
        int hintWidth2 = layout3 == null ? want : layout3.getWidth();
        Layout layout4 = this.mLayout;
        if (layout4 == null) {
            unpaddedWidth = want;
            unpaddedWidth2 = 1073741824;
            makeNewLayout(want, want, boring, hintBoring, (width - getCompoundPaddingLeft()) - getCompoundPaddingRight(), false);
        } else {
            unpaddedWidth = want;
            unpaddedWidth2 = 1073741824;
            if (layout4.getWidth() == want) {
                hintWant = want;
                if (hintWidth2 == hintWant && this.mLayout.getEllipsizedWidth() == (width - getCompoundPaddingLeft()) - getCompoundPaddingRight()) {
                    layoutChanged = false;
                    boolean widthChanged = this.mHint != null && this.mEllipsize == null && want > this.mLayout.getWidth() && ((this.mLayout instanceof BoringLayout) || (fromexisting && des >= 0 && des <= want));
                    maximumChanged = this.mMaxMode == this.mOldMaxMode || this.mMaximum != this.mOldMaximum;
                    if (!layoutChanged || maximumChanged) {
                        if (!maximumChanged || !widthChanged) {
                            makeNewLayout(want, hintWant, boring, hintBoring, (width - getCompoundPaddingLeft()) - getCompoundPaddingRight(), false);
                        } else {
                            this.mLayout.increaseWidthTo(want);
                        }
                    }
                }
            } else {
                hintWant = want;
            }
            layoutChanged = true;
            if (this.mHint != null) {
            }
            if (this.mMaxMode == this.mOldMaxMode) {
            }
            if (!layoutChanged) {
            }
            if (!maximumChanged) {
            }
            makeNewLayout(want, hintWant, boring, hintBoring, (width - getCompoundPaddingLeft()) - getCompoundPaddingRight(), false);
        }
        if (heightMode == unpaddedWidth2) {
            this.mDesiredHeightAtMeasure = -1;
            height = heightSize;
        } else {
            int desired = getDesiredHeight();
            height = desired;
            this.mDesiredHeightAtMeasure = desired;
            if (heightMode == Integer.MIN_VALUE) {
                height = Math.min(desired, heightSize);
            }
        }
        int unpaddedHeight = (height - getCompoundPaddingTop()) - getCompoundPaddingBottom();
        if (this.mMaxMode == 1 && this.mLayout.getLineCount() > (i = this.mMaximum)) {
            unpaddedHeight = Math.min(unpaddedHeight, this.mLayout.getLineTop(i));
        }
        if (this.mMovement == null) {
            if (this.mLayout.getWidth() <= unpaddedWidth && this.mLayout.getHeight() <= unpaddedHeight) {
                scrollTo(0, 0);
                setMeasuredDimension(width, height);
            }
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
        CharSequence text = this.mTransformed;
        if (text == null) {
            text = getText();
        }
        int maxLines = getMaxLines();
        TextPaint textPaint = this.mTempTextPaint;
        if (textPaint == null) {
            this.mTempTextPaint = new TextPaint();
        } else {
            textPaint.reset();
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

    @UnsupportedAppUsage
    private void checkForRelayout() {
        Layout layout;
        if ((this.mLayoutParams.width != -2 || (this.mMaxWidthMode == this.mMinWidthMode && this.mMaxWidth == this.mMinWidth)) && ((this.mHint == null || this.mHintLayout != null) && ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight() > 0)) {
            int oldht = this.mLayout.getHeight();
            int want = this.mLayout.getWidth();
            Layout layout2 = this.mHintLayout;
            int hintWant = layout2 == null ? 0 : layout2.getWidth();
            BoringLayout.Metrics metrics = UNKNOWN_BORING;
            makeNewLayout(want, hintWant, metrics, metrics, ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight(), false);
            if (this.mEllipsize != TextUtils.TruncateAt.MARQUEE) {
                if (this.mLayoutParams.height != -2 && this.mLayoutParams.height != -1) {
                    autoSizeText();
                    invalidate();
                    return;
                } else if (this.mLayout.getHeight() == oldht && ((layout = this.mHintLayout) == null || layout.getHeight() == oldht)) {
                    autoSizeText();
                    invalidate();
                    return;
                }
            }
            requestLayout();
            invalidate();
            return;
        }
        nullLayouts();
        requestLayout();
        invalidate();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
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

    @UnsupportedAppUsage
    private boolean bringTextIntoView() {
        int scrollx;
        int scrolly;
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
            int left = (int) Math.floor((double) layout.getLineLeft(line));
            int right = (int) Math.ceil((double) layout.getLineRight(line));
            if (right - left < hspace) {
                scrollx = ((right + left) / 2) - (hspace / 2);
            } else if (dir < 0) {
                scrollx = right - hspace;
            } else {
                scrollx = left;
            }
        } else if (a == Layout.Alignment.ALIGN_RIGHT) {
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
        scrollTo(scrollx, scrolly);
        return true;
    }

    /* JADX INFO: Multiple debug info for r2v2 int: [D('vslack' int), D('changed' boolean)] */
    /* JADX INFO: Multiple debug info for r4v2 int: [D('layout' android.text.Layout), D('vs' int)] */
    public boolean bringPointIntoView(int offset) {
        int grav;
        int vs;
        boolean changed;
        if (isLayoutRequested()) {
            this.mDeferScroll = offset;
            return false;
        }
        Layout layout = isShowingHint() ? this.mHintLayout : this.mLayout;
        if (layout == null) {
            return false;
        }
        int line = layout.getLineForOffset(offset);
        int i = AnonymousClass4.$SwitchMap$android$text$Layout$Alignment[layout.getParagraphAlignment(line).ordinal()];
        boolean clamped = true;
        if (i == 1) {
            grav = 1;
        } else if (i == 2) {
            grav = -1;
        } else if (i == 3) {
            grav = layout.getParagraphDirection(line);
        } else if (i != 4) {
            grav = 0;
        } else {
            grav = -layout.getParagraphDirection(line);
        }
        if (grav <= 0) {
            clamped = false;
        }
        int x = (int) layout.getPrimaryHorizontal(offset, clamped);
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
        int vs2 = this.mScrollY;
        if (top - vs2 < vslack) {
            vs2 = top - vslack;
        }
        if (bottom - vs2 > vspace - vslack) {
            vs = bottom - (vspace - vslack);
        } else {
            vs = vs2;
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
            } else {
                hs = hs;
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
            } else {
                hs = hs;
            }
        }
        if (hs == this.mScrollX && vs == this.mScrollY) {
            changed = false;
        } else {
            if (this.mScroller == null) {
                scrollTo(hs, vs);
            } else {
                long duration = AnimationUtils.currentAnimationTimeMillis() - this.mLastScroll;
                int dx = hs - this.mScrollX;
                int dy = vs - this.mScrollY;
                if (duration > 250) {
                    this.mScroller.startScroll(this.mScrollX, this.mScrollY, dx, dy);
                    awakenScrollBars(this.mScroller.getDuration());
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
        if (!isFocused()) {
            return changed;
        }
        if (this.mTempRect == null) {
            this.mTempRect = new Rect();
        }
        this.mTempRect.set(x - 2, top, x + 2, bottom);
        getInterestingRect(this.mTempRect, line);
        this.mTempRect.offset(this.mScrollX, this.mScrollY);
        if (requestRectangleOnScreen(this.mTempRect)) {
            return true;
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: android.widget.TextView$4  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$android$text$Layout$Alignment = new int[Layout.Alignment.values().length];

        static {
            try {
                $SwitchMap$android$text$Layout$Alignment[Layout.Alignment.ALIGN_LEFT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$text$Layout$Alignment[Layout.Alignment.ALIGN_RIGHT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$text$Layout$Alignment[Layout.Alignment.ALIGN_NORMAL.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$text$Layout$Alignment[Layout.Alignment.ALIGN_OPPOSITE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$text$Layout$Alignment[Layout.Alignment.ALIGN_CENTER.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public boolean moveCursorToVisibleOffset() {
        int start;
        if (!(this.mText instanceof Spannable) || (start = getSelectionStart()) != getSelectionEnd()) {
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
        int newStart = start;
        if (newStart < lowChar) {
            newStart = lowChar;
        } else if (newStart > highChar) {
            newStart = highChar;
        }
        if (newStart == start) {
            return false;
        }
        Selection.setSelection(this.mSpannable, newStart);
        return true;
    }

    @Override // android.view.View
    public void computeScroll() {
        Scroller scroller = this.mScroller;
        if (scroller != null && scroller.computeScrollOffset()) {
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
    @UnsupportedAppUsage
    public int viewportToContentVerticalOffset() {
        int offset = getExtendedPaddingTop() - this.mScrollY;
        if ((this.mGravity & 112) != 48) {
            return offset + getVerticalOffset(false);
        }
        return offset;
    }

    @Override // android.view.View
    public void debug(int depth) {
        String output;
        super.debug(depth);
        String output2 = debugIndent(depth) + "frame={" + this.mLeft + ", " + this.mTop + ", " + this.mRight + ", " + this.mBottom + "} scroll={" + this.mScrollX + ", " + this.mScrollY + "} ";
        if (this.mText != null) {
            output = output2 + "mText=\"" + ((Object) this.mText) + "\" ";
            if (this.mLayout != null) {
                output = output + "mLayout width=" + this.mLayout.getWidth() + " height=" + this.mLayout.getHeight();
            }
        } else {
            output = output2 + "mText=NULL";
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
        CharSequence charSequence = this.mText;
        return String.valueOf(start > end ? charSequence.subSequence(end, start) : charSequence.subSequence(start, end));
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
        Editor editor = this.mEditor;
        if (editor != null && (editor.mInputType & 15) == 1) {
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
        if (selectAllOnFocus) {
            CharSequence charSequence = this.mText;
            if (!(charSequence instanceof Spannable)) {
                setText(charSequence, BufferType.SPANNABLE);
            }
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
        Editor editor = this.mEditor;
        if (editor == null) {
            return true;
        }
        return editor.mCursorVisible;
    }

    private boolean canMarquee() {
        Layout layout;
        int width = ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        if (width <= 0) {
            return false;
        }
        if (this.mLayout.getLineWidth(0) > ((float) width) || (this.mMarqueeFadeMode != 0 && (layout = this.mSavedMarqueeModeLayout) != null && layout.getLineWidth(0) > ((float) width))) {
            return true;
        }
        return false;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private void startMarquee() {
        if (getKeyListener() == null && !compressText((float) ((getWidth() - getCompoundPaddingLeft()) - getCompoundPaddingRight()))) {
            Marquee marquee = this.mMarquee;
            if (marquee != null && !marquee.isStopped()) {
                return;
            }
            if ((isFocused() || isSelected()) && getLineCount() == 1 && canMarquee()) {
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
        Marquee marquee = this.mMarquee;
        if (marquee != null && !marquee.isStopped()) {
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

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
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
        int i;
        ArrayList<TextWatcher> arrayList = this.mListeners;
        if (arrayList != null && (i = arrayList.indexOf(watcher)) >= 0) {
            this.mListeners.remove(i);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBeforeTextChanged(CharSequence text, int start, int before, int after) {
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
        CharSequence charSequence = this.mText;
        if (charSequence instanceof Editable) {
            Editable text = (Editable) charSequence;
            Object[] spans = text.getSpans(start, end, type);
            int length = spans.length;
            for (int i = 0; i < length; i++) {
                int spanStart = text.getSpanStart(spans[i]);
                if (text.getSpanEnd(spans[i]) != start && spanStart != end) {
                    text.removeSpan(spans[i]);
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeAdjacentSuggestionSpans(int pos) {
        CharSequence charSequence = this.mText;
        if (charSequence instanceof Editable) {
            Editable text = (Editable) charSequence;
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
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.sendOnTextChanged(start, before, after);
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
        notifyListeningManagersAfterTextChanged();
        hideErrorIfUnchanged();
    }

    private void notifyListeningManagersAfterTextChanged() {
        AutofillManager afm;
        if (isAutofillable() && (afm = (AutofillManager) this.mContext.getSystemService(AutofillManager.class)) != null) {
            if (Helper.sVerbose) {
                Log.v(LOG_TAG, "notifyAutoFillManagerAfterTextChanged");
            }
            afm.notifyValueChanged(this);
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
            Editor editor = this.mEditor;
            if (editor != null) {
                editor.makeBlink();
            }
            bringPointIntoView(curs);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleTextChanged(CharSequence buffer, int start, int before, int after) {
        sLastCutCopyOrTextChangedTime = 0;
        Editor editor = this.mEditor;
        Editor.InputMethodState ims = editor == null ? null : editor.mInputMethodState;
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
        Editor editor = this.mEditor;
        Editor.InputMethodState ims = editor == null ? null : editor.mInputMethodState;
        if (what == Selection.SELECTION_END) {
            selChanged = true;
            newSelEnd = newStart;
            if (oldStart >= 0 || newStart >= 0) {
                invalidateCursor(Selection.getSelectionStart(buf), oldStart, newStart);
                checkForResize();
                registerForPreDraw();
                Editor editor2 = this.mEditor;
                if (editor2 != null) {
                    editor2.makeBlink();
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
                Editor editor3 = this.mEditor;
                if (editor3 != null) {
                    editor3.refreshTextActionMode();
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
            Editor editor4 = this.mEditor;
            if (editor4 != null) {
                if (oldStart >= 0) {
                    editor4.invalidateTextDisplayList(this.mLayout, oldStart, oldEnd);
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
        Editor editor5 = this.mEditor;
        if (editor5 != null && editor5.mSpellChecker != null && newStart < 0 && (what instanceof SpellCheckSpan)) {
            this.mEditor.mSpellChecker.onSpellCheckSpanRemoved((SpellCheckSpan) what);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        Spannable spannable;
        if (isTemporarilyDetached()) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
            return;
        }
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.onFocusChanged(focused, direction);
        }
        if (focused && (spannable = this.mSpannable) != null) {
            MetaKeyKeyListener.resetMetaState(spannable);
        }
        startStopMarquee(focused);
        TransformationMethod transformationMethod = this.mTransformation;
        if (transformationMethod != null) {
            transformationMethod.onFocusChanged(this, this.mText, focused, direction, previouslyFocusedRect);
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override // android.view.View
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.onWindowFocusChanged(hasWindowFocus);
        }
        startStopMarquee(hasWindowFocus);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        Editor editor = this.mEditor;
        if (editor != null && visibility != 0) {
            editor.hideCursorAndSpanControllers();
            stopTextActionMode();
        }
    }

    public void clearComposingText() {
        if (this.mText instanceof Spannable) {
            BaseInputConnection.removeComposingSpans(this.mSpannable);
        }
    }

    @Override // android.view.View
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

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        Editor editor;
        int action = event.getActionMasked();
        Editor editor2 = this.mEditor;
        if (editor2 != null) {
            editor2.onTouchEvent(event);
            if (this.mEditor.mSelectionModifierCursorController != null && this.mEditor.mSelectionModifierCursorController.isDragAcceleratorActive()) {
                return true;
            }
        }
        boolean superResult = super.onTouchEvent(event);
        Editor editor3 = this.mEditor;
        if (editor3 == null || !editor3.mDiscardNextActionUp || action != 1) {
            boolean touchIsFinished = action == 1 && ((editor = this.mEditor) == null || !editor.mIgnoreActionUpEvent) && isFocused();
            if ((this.mMovement != null || onCheckIsTextEditor()) && isEnabled() && (this.mText instanceof Spannable) && this.mLayout != null) {
                boolean handled = false;
                MovementMethod movementMethod = this.mMovement;
                if (movementMethod != null) {
                    handled = false | movementMethod.onTouchEvent(this, this.mSpannable, event);
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
                    InputMethodManager imm = getInputMethodManager();
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
        Editor editor4 = this.mEditor;
        editor4.mDiscardNextActionUp = false;
        if (editor4.mIsInsertionActionModeStartPending) {
            this.mEditor.startInsertionActionMode();
            this.mEditor.mIsInsertionActionModeStartPending = false;
        }
        return superResult;
    }

    @Override // android.view.View
    public boolean onGenericMotionEvent(MotionEvent event) {
        MovementMethod movementMethod = this.mMovement;
        if (!(movementMethod == null || !(this.mText instanceof Spannable) || this.mLayout == null)) {
            try {
                if (movementMethod.onGenericMotionEvent(this, this.mSpannable, event)) {
                    return true;
                }
            } catch (AbstractMethodError e) {
            }
        }
        return super.onGenericMotionEvent(event);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onCreateContextMenu(ContextMenu menu) {
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.onCreateContextMenu(menu);
        }
    }

    @Override // android.view.View
    public boolean showContextMenu() {
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.setContextMenuAnchor(Float.NaN, Float.NaN);
        }
        return super.showContextMenu();
    }

    @Override // android.view.View
    public boolean showContextMenu(float x, float y) {
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.setContextMenuAnchor(x, y);
        }
        return super.showContextMenu(x, y);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public boolean isTextEditable() {
        return (this.mText instanceof Editable) && onCheckIsTextEditor() && isEnabled();
    }

    public boolean didTouchFocusSelect() {
        Editor editor = this.mEditor;
        return editor != null && editor.mTouchFocusSelected;
    }

    @Override // android.view.View
    public void cancelLongPress() {
        super.cancelLongPress();
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.mIgnoreActionUpEvent = true;
        }
    }

    @Override // android.view.View
    public boolean onTrackballEvent(MotionEvent event) {
        Spannable spannable;
        MovementMethod movementMethod = this.mMovement;
        if (movementMethod == null || (spannable = this.mSpannable) == null || this.mLayout == null || !movementMethod.onTrackballEvent(this, spannable, event)) {
            return super.onTrackballEvent(event);
        }
        return true;
    }

    public void setScroller(Scroller s) {
        this.mScroller = s;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public float getLeftFadingEdgeStrength() {
        Marquee marquee;
        if (isMarqueeFadeEnabled() && (marquee = this.mMarquee) != null && !marquee.isStopped()) {
            Marquee marquee2 = this.mMarquee;
            if (marquee2.shouldDrawLeftFade()) {
                return getHorizontalFadingEdgeStrength(marquee2.getScroll(), 0.0f);
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
    @Override // android.view.View
    public float getRightFadingEdgeStrength() {
        Marquee marquee;
        if (isMarqueeFadeEnabled() && (marquee = this.mMarquee) != null && !marquee.isStopped()) {
            Marquee marquee2 = this.mMarquee;
            return getHorizontalFadingEdgeStrength(marquee2.getMaxFadeScroll(), marquee2.getScroll());
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
    @Override // android.view.View
    public int computeHorizontalScrollRange() {
        Layout layout = this.mLayout;
        if (layout != null) {
            return (!this.mSingleLine || (this.mGravity & 7) != 3) ? this.mLayout.getWidth() : (int) layout.getLineWidth(0);
        }
        return super.computeHorizontalScrollRange();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int computeVerticalScrollRange() {
        Layout layout = this.mLayout;
        if (layout != null) {
            return layout.getHeight();
        }
        return super.computeVerticalScrollRange();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int computeVerticalScrollExtent() {
        return (getHeight() - getCompoundPaddingTop()) - getCompoundPaddingBottom();
    }

    @Override // android.view.View
    public void findViewsWithText(ArrayList<View> outViews, CharSequence searched, int flags) {
        super.findViewsWithText(outViews, searched, flags);
        if (!outViews.contains(this) && (flags & 1) != 0 && !TextUtils.isEmpty(searched) && !TextUtils.isEmpty(this.mText)) {
            if (this.mText.toString().toLowerCase().contains(searched.toString().toLowerCase())) {
                outViews.add(this);
            }
        }
    }

    public static ColorStateList getTextColors(Context context, TypedArray attrs) {
        int ap;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(android.R.styleable.TextView);
            ColorStateList colors = a.getColorStateList(5);
            if (colors == null && (ap = a.getResourceId(1, 0)) != 0) {
                TypedArray appearance = context.obtainStyledAttributes(ap, android.R.styleable.TextAppearance);
                colors = appearance.getColorStateList(3);
                appearance.recycle();
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

    @Override // android.view.View
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        if (event.hasModifiers(4096)) {
            if (keyCode != 29) {
                if (keyCode != 31) {
                    if (keyCode != 50) {
                        if (keyCode != 52) {
                            if (keyCode == 54 && canUndo()) {
                                return onTextContextMenuItem(16908338);
                            }
                        } else if (canCut()) {
                            return onTextContextMenuItem(16908320);
                        }
                    } else if (canPaste()) {
                        return onTextContextMenuItem(16908322);
                    }
                } else if (canCopy()) {
                    return onTextContextMenuItem(16908321);
                }
            } else if (canSelectText()) {
                return onTextContextMenuItem(16908319);
            }
        } else if (event.hasModifiers(4097)) {
            if (keyCode != 50) {
                if (keyCode == 54 && canRedo()) {
                    return onTextContextMenuItem(16908339);
                }
            } else if (canPaste()) {
                return onTextContextMenuItem(16908337);
            }
        }
        return super.onKeyShortcut(keyCode, event);
    }

    /* access modifiers changed from: package-private */
    public boolean canSelectText() {
        Editor editor;
        return (this.mText.length() == 0 || (editor = this.mEditor) == null || !editor.hasSelectionController()) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public boolean textCanBeSelected() {
        MovementMethod movementMethod = this.mMovement;
        if (movementMethod == null || !movementMethod.canSelectArbitrarily()) {
            return false;
        }
        if (isTextEditable() || (isTextSelectable() && (this.mText instanceof Spannable) && isEnabled())) {
            return true;
        }
        return false;
    }

    @UnsupportedAppUsage
    private Locale getTextServicesLocale(boolean allowNullLocale) {
        updateTextServicesLocaleAsync();
        if (this.mCurrentSpellCheckerLocaleCache != null || allowNullLocale) {
            return this.mCurrentSpellCheckerLocaleCache;
        }
        return Locale.getDefault();
    }

    public final void setTextOperationUser(UserHandle user) {
        if (!Objects.equals(this.mTextOperationUser, user)) {
            if (user == null || Process.myUserHandle().equals(user) || getContext().checkSelfPermission(Manifest.permission.INTERACT_ACROSS_USERS_FULL) == 0) {
                this.mTextOperationUser = user;
                this.mCurrentSpellCheckerLocaleCache = null;
                Editor editor = this.mEditor;
                if (editor != null) {
                    editor.onTextOperationUserChanged();
                    return;
                }
                return;
            }
            throw new SecurityException("INTERACT_ACROSS_USERS_FULL is required. userId=" + user.getIdentifier() + " callingUserId" + UserHandle.myUserId());
        }
    }

    /* access modifiers changed from: package-private */
    public final TextServicesManager getTextServicesManagerForUser() {
        return (TextServicesManager) getServiceManagerForUser("android", TextServicesManager.class);
    }

    /* access modifiers changed from: package-private */
    public final ClipboardManager getClipboardManagerForUser() {
        return (ClipboardManager) getServiceManagerForUser(getContext().getPackageName(), ClipboardManager.class);
    }

    /* access modifiers changed from: package-private */
    public final TextClassificationManager getTextClassificationManagerForUser() {
        return (TextClassificationManager) getServiceManagerForUser(getContext().getPackageName(), TextClassificationManager.class);
    }

    /* access modifiers changed from: package-private */
    public final <T> T getServiceManagerForUser(String packageName, Class<T> managerClazz) {
        if (this.mTextOperationUser == null) {
            return (T) getContext().getSystemService(managerClazz);
        }
        try {
            return (T) getContext().createPackageContextAsUser(packageName, 0, this.mTextOperationUser).getSystemService(managerClazz);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void startActivityAsTextOperationUserIfNecessary(Intent intent) {
        if (this.mTextOperationUser != null) {
            getContext().startActivityAsUser(intent, this.mTextOperationUser);
        } else {
            getContext().startActivity(intent);
        }
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
        AsyncTask.execute(new Runnable() {
            /* class android.widget.TextView.AnonymousClass3 */

            public void run() {
                TextView.this.updateTextServicesLocaleLocked();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void updateTextServicesLocaleLocked() {
        Locale locale;
        TextServicesManager textServicesManager = getTextServicesManagerForUser();
        if (textServicesManager != null) {
            SpellCheckerSubtype subtype = textServicesManager.getCurrentSpellCheckerSubtype(true);
            if (subtype != null) {
                locale = subtype.getLocaleObject();
            } else {
                locale = null;
            }
            this.mCurrentSpellCheckerLocaleCache = locale;
        }
    }

    /* access modifiers changed from: package-private */
    public void onLocaleChanged() {
        this.mEditor.onLocaleChanged();
    }

    public WordIterator getWordIterator() {
        Editor editor = this.mEditor;
        if (editor != null) {
            return editor.getWordIterator();
        }
        return null;
    }

    @Override // android.view.View
    public void onPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        super.onPopulateAccessibilityEventInternal(event);
        CharSequence text = getTextForAccessibility();
        if (!TextUtils.isEmpty(text)) {
            event.getText().add(text);
        }
    }

    @Override // android.view.View
    public CharSequence getAccessibilityClassName() {
        return TextView.class.getName();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0118, code lost:
        if (r4 < r8.length()) goto L_0x011d;
     */
    @Override // android.view.View
    public void onProvideStructure(ViewStructure structure, int viewFor, int flags) {
        int i;
        int bottomLine;
        int topLine;
        super.onProvideStructure(structure, viewFor, flags);
        boolean isPassword = hasPasswordTransformationMethod() || isPasswordInputType(getInputType());
        if (viewFor == 1) {
            if (viewFor == 1) {
                structure.setDataIsSensitive(!this.mTextSetFromXmlOrResourceId);
            }
            if (this.mTextId != 0) {
                try {
                    structure.setTextIdEntry(getResources().getResourceEntryName(this.mTextId));
                } catch (Resources.NotFoundException e) {
                    if (Helper.sVerbose) {
                        Log.v(LOG_TAG, "onProvideAutofillStructure(): cannot set name for text id " + this.mTextId + ": " + e.getMessage());
                    }
                }
            }
        }
        if (!isPassword || viewFor == 1) {
            if (this.mLayout == null) {
                assumeLayout();
            }
            Layout layout = this.mLayout;
            int lineCount = layout.getLineCount();
            if (lineCount <= 1) {
                CharSequence text = getText();
                if (viewFor == 1) {
                    structure.setText(text);
                } else {
                    structure.setText(text, getSelectionStart(), getSelectionEnd());
                }
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
                int expandedTopLine2 = expandedTopLine < 0 ? 0 : expandedTopLine;
                int expandedBottomLine = bottomLine + ((bottomLine - topLine) / 2);
                if (expandedBottomLine >= lineCount) {
                    expandedBottomLine = lineCount - 1;
                }
                int expandedTopChar = layout.getLineStart(expandedTopLine2);
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
                CharSequence text2 = getText();
                if (expandedTopChar <= 0) {
                }
                text2 = text2.subSequence(expandedTopChar, expandedBottomChar);
                if (viewFor == 1) {
                    structure.setText(text2);
                } else {
                    structure.setText(text2, selStart - expandedTopChar, selEnd - expandedTopChar);
                    int[] lineOffsets = new int[((bottomLine - topLine) + 1)];
                    int[] lineBaselines = new int[((bottomLine - topLine) + 1)];
                    int baselineOffset = getBaselineOffset();
                    for (int i2 = topLine; i2 <= bottomLine; i2++) {
                        lineOffsets[i2 - topLine] = layout.getLineStart(i2);
                        lineBaselines[i2 - topLine] = layout.getLineBaseline(i2) + baselineOffset;
                    }
                    structure.setTextLines(lineOffsets, lineBaselines);
                }
            }
            if (viewFor == 0) {
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
                i = 1;
                structure.setTextStyle(getTextSize(), getCurrentTextColor(), 1, style);
            } else {
                i = 1;
            }
            if (viewFor == i) {
                structure.setMinTextEms(getMinEms());
                structure.setMaxTextEms(getMaxEms());
                int maxLength = -1;
                InputFilter[] filters = getFilters();
                int length = filters.length;
                int i3 = 0;
                while (true) {
                    if (i3 >= length) {
                        break;
                    }
                    InputFilter filter = filters[i3];
                    if (filter instanceof InputFilter.LengthFilter) {
                        maxLength = ((InputFilter.LengthFilter) filter).getMax();
                        break;
                    }
                    i3++;
                }
                structure.setMaxTextLength(maxLength);
            }
        }
        structure.setHint(getHint());
        structure.setInputType(getInputType());
    }

    /* access modifiers changed from: package-private */
    public boolean canRequestAutofill() {
        AutofillManager afm;
        if (isAutofillable() && (afm = (AutofillManager) this.mContext.getSystemService(AutofillManager.class)) != null) {
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

    @Override // android.view.View
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

    @Override // android.view.View
    public int getAutofillType() {
        return isTextEditable() ? 1 : 0;
    }

    @Override // android.view.View
    public AutofillValue getAutofillValue() {
        if (isTextEditable()) {
            return AutofillValue.forText(TextUtils.trimToParcelableSize(getText()));
        }
        return null;
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        event.setPassword(hasPasswordTransformationMethod());
        if (event.getEventType() == 8192) {
            event.setFromIndex(Selection.getSelectionStart(this.mText));
            event.setToIndex(Selection.getSelectionEnd(this.mText));
            event.setItemCount(this.mText.length());
        }
    }

    @Override // android.view.View
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
        Editor editor = this.mEditor;
        if (editor != null) {
            info.setInputType(editor.mInputType);
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
            info.setAvailableExtraData(Arrays.asList(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY));
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
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(268435456, getResources().getString(R.string.share)));
            }
            if (canProcessText()) {
                this.mEditor.mProcessTextIntentActionsHandler.onInitializeAccessibilityNodeInfo(info);
            }
        }
        int numFilters = this.mFilters.length;
        for (int i = 0; i < numFilters; i++) {
            InputFilter filter = this.mFilters[i];
            if (filter instanceof InputFilter.LengthFilter) {
                info.setMaxTextLength(((InputFilter.LengthFilter) filter).getMax());
            }
        }
        if (!isSingleLine()) {
            info.setMultiLine(true);
        }
    }

    @Override // android.view.View
    public void addExtraDataToAccessibilityNodeInfo(AccessibilityNodeInfo info, String extraDataKey, Bundle arguments) {
        RectF bounds;
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
                if ((cursorAnchorInfo.getCharacterBoundsFlags(positionInfoStartIndex + i) & 1) == 1 && (bounds = cursorAnchorInfo.getCharacterBounds(positionInfoStartIndex + i)) != null) {
                    mapRectFromViewToScreenCoords(bounds, true);
                    boundingRects[i] = bounds;
                }
            }
            info.getExtras().putParcelableArray(extraDataKey, boundingRects);
        }
    }

    /* JADX INFO: Multiple debug info for r15v4 float: [D('left' float), D('localLeft' float)] */
    /* JADX INFO: Multiple debug info for r3v4 float: [D('localBottom' float), D('minLine' int)] */
    public void populateCharacterBounds(CursorAnchorInfo.Builder builder, int startIndex, int endIndex, float viewportToContentHorizontalOffset, float viewportToContentVerticalOffset) {
        float right;
        float left;
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
            boolean ltrLine = true;
            if (this.mLayout.getParagraphDirection(line) != 1) {
                ltrLine = false;
            }
            float[] widths = new float[(offsetEnd - offsetStart)];
            this.mLayout.getPaint().getTextWidths(this.mTransformed, offsetStart, offsetEnd, widths);
            float top = (float) this.mLayout.getLineTop(line);
            float bottom = (float) this.mLayout.getLineBottom(line);
            int offset = offsetStart;
            while (offset < offsetEnd) {
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
                } else if (!isRtl) {
                    left = secondary;
                    right = secondary + charWidth;
                } else {
                    left = primary - charWidth;
                    right = primary;
                }
                float localLeft = left + viewportToContentHorizontalOffset;
                float localRight = right + viewportToContentHorizontalOffset;
                float localTop = top + viewportToContentVerticalOffset;
                float localBottom = bottom + viewportToContentVerticalOffset;
                boolean isTopLeftVisible = isPositionVisible(localLeft, localTop);
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
                builder.addCharacterBounds(offset, localLeft, localTop, localRight, localBottom, characterBoundsFlags);
                offset++;
                minLine = minLine;
            }
            line++;
            i = startIndex;
            i2 = endIndex;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
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
                return false;
            }
            return true;
        }
    }

    @Override // android.view.View
    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        int start;
        int end;
        CharSequence text;
        int updatedTextLength;
        Editor editor = this.mEditor;
        if (editor != null && editor.mProcessTextIntentActionsHandler.performAccessibilityAction(action)) {
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
                                Editor editor2 = this.mEditor;
                                if (editor2 != null) {
                                    editor2.startSelectionActionModeAsync(false);
                                }
                                return true;
                            }
                        }
                        return false;
                    } else if (action != 2097152) {
                        if (action != 268435456) {
                            return super.performAccessibilityActionInternal(action, arguments);
                        }
                        if (!isFocused() || !canShare() || !onTextContextMenuItem(16908341)) {
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
                        CharSequence charSequence = this.mText;
                        if (charSequence != null && (updatedTextLength = charSequence.length()) > 0) {
                            Selection.setSelection(this.mSpannable, updatedTextLength);
                        }
                        return true;
                    }
                } else if (!isFocused() || !canCut() || !onTextContextMenuItem(16908320)) {
                    return false;
                } else {
                    return true;
                }
            } else if (!isFocused() || !canPaste() || !onTextContextMenuItem(16908322)) {
                return false;
            } else {
                return true;
            }
        } else if (!isFocused() || !canCopy() || !onTextContextMenuItem(16908321)) {
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
        if ((this.mMovement == null && !onCheckIsTextEditor()) || !hasSpannableText() || this.mLayout == null) {
            return handled;
        }
        if ((!isTextEditable() && !isTextSelectable()) || !isFocused()) {
            return handled;
        }
        InputMethodManager imm = getInputMethodManager();
        viewClicked(imm);
        if (isTextSelectable() || !this.mEditor.mShowSoftInputOnFocus || imm == null) {
            return handled;
        }
        return handled | imm.showSoftInput(this, 0);
    }

    private boolean hasSpannableText() {
        CharSequence charSequence = this.mText;
        return charSequence != null && (charSequence instanceof Spannable);
    }

    @Override // android.view.View
    public void sendAccessibilityEventInternal(int eventType) {
        Editor editor;
        if (eventType == 32768 && (editor = this.mEditor) != null) {
            editor.mProcessTextIntentActionsHandler.initializeAccessibilityActions();
        }
        super.sendAccessibilityEventInternal(eventType);
    }

    @Override // android.view.View, android.view.accessibility.AccessibilityEventSource
    public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {
        if (event.getEventType() != 4096) {
            super.sendAccessibilityEventUnchecked(event);
        }
    }

    @UnsupportedAppUsage
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

    private InputMethodManager getInputMethodManager() {
        return (InputMethodManager) getContext().getSystemService(InputMethodManager.class);
    }

    public boolean isInputMethodTarget() {
        InputMethodManager imm = getInputMethodManager();
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
        if (id != 16908355) {
            switch (id) {
                case 16908319:
                    boolean hadSelection = hasSelection();
                    selectAllText();
                    Editor editor = this.mEditor;
                    if (editor != null && hadSelection) {
                        editor.invalidateActionModeAsync();
                    } else if (!canSelectText()) {
                        return true;
                    } else {
                        ActionMode actionMode = this.mEditor.getTextActionMode();
                        if (actionMode == null) {
                            this.mEditor.startActionModeInternal(0);
                            this.mEditor.getSelectionController().show();
                        } else {
                            actionMode.invalidate();
                        }
                    }
                    return true;
                case 16908320:
                    if (!setPrimaryClip(ClipData.newPlainText(null, getTransformedText(min, max)))) {
                        Toast.makeText(getContext(), (int) R.string.failed_to_copy_to_clipboard, 0).show();
                    } else if (HwDeviceManager.disallowOp(23)) {
                        Log.i(LOG_TAG, "TextView cut is not allowed by MDM!");
                        return true;
                    } else {
                        deleteText_internal(min, max);
                    }
                    return true;
                case 16908321:
                    int selStart2 = getSelectionStart();
                    int selEnd2 = getSelectionEnd();
                    if (setPrimaryClip(ClipData.newPlainText(null, getTransformedText(Math.max(0, Math.min(selStart2, selEnd2)), Math.max(0, Math.max(selStart2, selEnd2)))))) {
                        stopTextActionMode();
                    } else {
                        Toast.makeText(getContext(), (int) R.string.failed_to_copy_to_clipboard, 0).show();
                    }
                    if (getTextCopyFinishedListener() != null) {
                        getTextCopyFinishedListener().copyDone();
                    }
                    return true;
                case 16908322:
                    paste(min, max, true);
                    return true;
                default:
                    switch (id) {
                        case 16908337:
                            paste(min, max, false);
                            return true;
                        case 16908338:
                            Editor editor2 = this.mEditor;
                            if (editor2 != null) {
                                editor2.undo();
                            }
                            return true;
                        case 16908339:
                            Editor editor3 = this.mEditor;
                            if (editor3 != null) {
                                editor3.redo();
                            }
                            return true;
                        case 16908340:
                            Editor editor4 = this.mEditor;
                            if (editor4 != null) {
                                editor4.replace();
                            }
                            return true;
                        case 16908341:
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
    @UnsupportedAppUsage
    public CharSequence getTransformedText(int start, int end) {
        return removeSuggestionSpans(this.mTransformed.subSequence(start, end));
    }

    @Override // android.view.View
    public boolean performLongClick() {
        boolean handled = false;
        boolean performedHapticFeedback = false;
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.mIsBeingLongClicked = true;
        }
        if (super.performLongClick()) {
            handled = true;
            performedHapticFeedback = true;
        }
        Editor editor2 = this.mEditor;
        if (editor2 != null) {
            handled |= editor2.performLongClick(handled);
            this.mEditor.mIsBeingLongClicked = false;
        }
        if (handled) {
            if (!performedHapticFeedback) {
                performHapticFeedback(0);
            }
            Editor editor3 = this.mEditor;
            if (editor3 != null) {
                editor3.mDiscardNextActionUp = true;
            }
        } else {
            MetricsLogger.action(this.mContext, 629, 0);
        }
        return handled;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
        super.onScrollChanged(horiz, vert, oldHoriz, oldVert);
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.onScrollChanged();
        }
    }

    public boolean isSuggestionsEnabled() {
        Editor editor = this.mEditor;
        if (editor == null || (editor.mInputType & 15) != 1 || (this.mEditor.mInputType & 524288) > 0) {
            return false;
        }
        int variation = this.mEditor.mInputType & InputType.TYPE_MASK_VARIATION;
        if (variation == 0 || variation == 48 || variation == 80 || variation == 64 || variation == 160) {
            return true;
        }
        return false;
    }

    public void setCustomSelectionActionModeCallback(ActionMode.Callback actionModeCallback) {
        createEditorIfNeeded();
        this.mEditor.mCustomSelectionActionModeCallback = actionModeCallback;
    }

    public ActionMode.Callback getCustomSelectionActionModeCallback() {
        Editor editor = this.mEditor;
        if (editor == null) {
            return null;
        }
        return editor.mCustomSelectionActionModeCallback;
    }

    public void setCustomInsertionActionModeCallback(ActionMode.Callback actionModeCallback) {
        createEditorIfNeeded();
        this.mEditor.mCustomInsertionActionModeCallback = actionModeCallback;
    }

    public ActionMode.Callback getCustomInsertionActionModeCallback() {
        Editor editor = this.mEditor;
        if (editor == null) {
            return null;
        }
        return editor.mCustomInsertionActionModeCallback;
    }

    public void setTextClassifier(TextClassifier textClassifier) {
        this.mTextClassifier = textClassifier;
    }

    public TextClassifier getTextClassifier() {
        TextClassifier textClassifier = this.mTextClassifier;
        if (textClassifier != null) {
            return textClassifier;
        }
        TextClassificationManager tcm = getTextClassificationManagerForUser();
        if (tcm != null) {
            return tcm.getTextClassifier();
        }
        return TextClassifier.NO_OP;
    }

    /* access modifiers changed from: package-private */
    public TextClassifier getTextClassificationSession() {
        String widgetType;
        TextClassifier textClassifier = this.mTextClassificationSession;
        if (textClassifier == null || textClassifier.isDestroyed()) {
            TextClassificationManager tcm = getTextClassificationManagerForUser();
            if (tcm != null) {
                if (isTextEditable()) {
                    widgetType = TextClassifier.WIDGET_TYPE_EDITTEXT;
                } else if (isTextSelectable()) {
                    widgetType = TextClassifier.WIDGET_TYPE_TEXTVIEW;
                } else {
                    widgetType = TextClassifier.WIDGET_TYPE_UNSELECTABLE_TEXTVIEW;
                }
                this.mTextClassificationContext = new TextClassificationContext.Builder(this.mContext.getPackageName(), widgetType).build();
                TextClassifier textClassifier2 = this.mTextClassifier;
                if (textClassifier2 != null) {
                    this.mTextClassificationSession = tcm.createTextClassificationSession(this.mTextClassificationContext, textClassifier2);
                } else {
                    this.mTextClassificationSession = tcm.createTextClassificationSession(this.mTextClassificationContext);
                }
            } else {
                this.mTextClassificationSession = TextClassifier.NO_OP;
            }
        }
        return this.mTextClassificationSession;
    }

    /* access modifiers changed from: package-private */
    public TextClassificationContext getTextClassificationContext() {
        return this.mTextClassificationContext;
    }

    /* access modifiers changed from: package-private */
    public boolean usesNoOpTextClassifier() {
        return getTextClassifier() == TextClassifier.NO_OP;
    }

    public boolean requestActionMode(TextLinks.TextLinkSpan clickedSpan) {
        Preconditions.checkNotNull(clickedSpan);
        CharSequence charSequence = this.mText;
        if (!(charSequence instanceof Spanned)) {
            return false;
        }
        int start = ((Spanned) charSequence).getSpanStart(clickedSpan);
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
        CharSequence charSequence = this.mText;
        if (!(charSequence instanceof Spanned)) {
            return false;
        }
        Spanned spanned = (Spanned) charSequence;
        int start = spanned.getSpanStart(clickedSpan);
        int end = spanned.getSpanEnd(clickedSpan);
        if (start < 0 || end > this.mText.length() || start >= end) {
            return false;
        }
        Supplier<TextClassification> supplier = new Supplier(new TextClassification.Request.Builder(this.mText, start, end).setDefaultLocales(getTextLocales()).build()) {
            /* class android.widget.$$Lambda$TextView$DJlzb7VS7J_1890Kto7GAApQDN0 */
            private final /* synthetic */ TextClassification.Request f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Supplier
            public final Object get() {
                return TextView.this.lambda$handleClick$0$TextView(this.f$1);
            }
        };
        CompletableFuture.supplyAsync(supplier).completeOnTimeout(null, 1, TimeUnit.SECONDS).thenAccept((Consumer) $$Lambda$TextView$jQz3_DIfGrNeNdu_95_wi6UkW4E.INSTANCE);
        return true;
    }

    public /* synthetic */ TextClassification lambda$handleClick$0$TextView(TextClassification.Request request) {
        return getTextClassifier().classifyText(request);
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
    @UnsupportedAppUsage
    public void stopTextActionMode() {
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.lambda$startActionModeInternal$0$Editor();
        }
    }

    public void hideFloatingToolbar(int durationMs) {
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.hideFloatingToolbar(durationMs);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean canUndo() {
        Editor editor = this.mEditor;
        return editor != null && editor.canUndo();
    }

    /* access modifiers changed from: package-private */
    public boolean canRedo() {
        Editor editor = this.mEditor;
        return editor != null && editor.canRedo();
    }

    /* access modifiers changed from: package-private */
    public boolean canCut() {
        Editor editor;
        if (!hasPasswordTransformationMethod() && this.mText.length() > 0 && hasSelection() && (this.mText instanceof Editable) && (editor = this.mEditor) != null && editor.mKeyListener != null) {
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
        int i;
        if (this.mDeviceProvisionedState == 0) {
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
                i = 2;
            } else {
                i = 1;
            }
            this.mDeviceProvisionedState = i;
        }
        return this.mDeviceProvisionedState == 2;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public boolean canPaste() {
        Editor editor;
        return (this.mText instanceof Editable) && (editor = this.mEditor) != null && editor.mKeyListener != null && getSelectionStart() >= 0 && getSelectionEnd() >= 0 && getClipboardManagerForUser().hasPrimaryClip();
    }

    /* access modifiers changed from: package-private */
    public boolean canPasteAsPlainText() {
        ClipData clipData;
        if (!canPaste() || (clipData = getClipboardManagerForUser().getPrimaryClip()) == null) {
            return false;
        }
        ClipDescription description = clipData.getDescription();
        boolean isPlainType = description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
        CharSequence text = clipData.getItemAt(0).getText();
        if (!isPlainType || !(text instanceof Spanned) || !TextUtils.hasStyleSpan((Spanned) text)) {
            return description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML);
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
        CharSequence text;
        ClipData clip = getClipboardManagerForUser().getPrimaryClip();
        if (clip != null) {
            boolean didFirst = false;
            for (int i = 0; i < clip.getItemCount(); i++) {
                if (withFormatting) {
                    text = clip.getItemAt(i).coerceToStyledText(getContext());
                } else {
                    CharSequence text2 = clip.getItemAt(i).coerceToText(getContext());
                    text = text2 instanceof Spanned ? text2.toString() : text2;
                }
                if (text != null) {
                    if (!didFirst) {
                        Selection.setSelection(this.mSpannable, max);
                        ((Editable) this.mText).replace(min, max, text);
                        didFirst = true;
                    } else {
                        ((Editable) this.mText).insert(getSelectionEnd(), "\n");
                        ((Editable) this.mText).insert(getSelectionEnd(), text);
                    }
                }
            }
            sLastCutCopyOrTextChangedTime = 0;
        }
    }

    private void shareSelectedText() {
        String selectedText = getSelectedText();
        if (selectedText != null && !selectedText.isEmpty()) {
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null && imm.isActive(this)) {
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
            }
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType(ClipDescription.MIMETYPE_TEXT_PLAIN);
            sharingIntent.removeExtra(Intent.EXTRA_TEXT);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, (String) TextUtils.trimToParcelableSize(selectedText));
            getContext().startActivity(Intent.createChooser(sharingIntent, this.mContext.getResources().getText(R.string.share)));
            Selection.setSelection(this.mSpannable, getSelectionEnd());
        }
    }

    private boolean setPrimaryClip(ClipData clip) {
        try {
            getClipboardManagerForUser().setPrimaryClip(clip);
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
    @UnsupportedAppUsage
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

    @Override // android.view.View
    public boolean onDragEvent(DragEvent event) {
        int action = event.getAction();
        if (action == 1) {
            Editor editor = this.mEditor;
            if (editor == null || !editor.hasInsertionController()) {
                return false;
            }
            return true;
        } else if (action == 2) {
            if (this.mText instanceof Spannable) {
                Selection.setSelection(this.mSpannable, getOffsetForPosition(event.getX(), event.getY()));
            }
            return true;
        } else if (action == 3) {
            Editor editor2 = this.mEditor;
            if (editor2 != null) {
                editor2.onDrop(event);
            }
            return true;
        } else if (action != 5) {
            return true;
        } else {
            requestFocus();
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInBatchEditMode() {
        Editor editor = this.mEditor;
        if (editor == null) {
            return false;
        }
        Editor.InputMethodState ims = editor.mInputMethodState;
        if (ims == null) {
            return this.mEditor.mInBatchEditControllers;
        }
        if (ims.mBatchEditNesting > 0) {
            return true;
        }
        return false;
    }

    @Override // android.view.View
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

    public TextDirectionHeuristic getTextDirectionHeuristic() {
        if (hasPasswordTransformationMethod()) {
            return TextDirectionHeuristics.LTR;
        }
        Editor editor = this.mEditor;
        boolean defaultIsRtl = false;
        if (editor == null || (editor.mInputType & 15) != 3) {
            if (getLayoutDirection() == 1) {
                defaultIsRtl = true;
            }
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
                        return TextDirectionHeuristics.FIRSTSTRONG_RTL;
                    }
                    return TextDirectionHeuristics.FIRSTSTRONG_LTR;
            }
        } else {
            byte digitDirection = Character.getDirectionality(DecimalFormatSymbols.getInstance(getTextLocale()).getDigitStrings()[0].codePointAt(0));
            if (digitDirection == 1 || digitDirection == 2) {
                return TextDirectionHeuristics.RTL;
            }
            return TextDirectionHeuristics.LTR;
        }
    }

    @Override // android.view.View
    public void onResolveDrawables(int layoutDirection) {
        if (this.mLastLayoutDirection != layoutDirection) {
            this.mLastLayoutDirection = layoutDirection;
            Drawables drawables = this.mDrawables;
            if (drawables != null && drawables.resolveWithLayoutDirection(layoutDirection)) {
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
    @Override // android.view.View
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
    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    private void createEditorIfNeeded() {
        if (this.mEditor == null) {
            HwWidgetFactory.HwTextView hwTextView = this.mHwTextView;
            if (hwTextView != null) {
                this.mEditor = hwTextView.getEditor(this);
            } else {
                this.mEditor = new Editor(this);
            }
        }
    }

    @Override // android.view.View
    @UnsupportedAppUsage
    public CharSequence getIterableTextForAccessibility() {
        return this.mText;
    }

    private void ensureIterableTextForAccessibilitySelectable() {
        CharSequence charSequence = this.mText;
        if (!(charSequence instanceof Spannable)) {
            setText(charSequence, BufferType.SPANNABLE);
        }
    }

    @Override // android.view.View
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

    @Override // android.view.View
    public int getAccessibilitySelectionStart() {
        return getSelectionStart();
    }

    @Override // android.view.View
    public boolean isAccessibilitySelectionExtendable() {
        return true;
    }

    @Override // android.view.View
    public int getAccessibilitySelectionEnd() {
        return getSelectionEnd();
    }

    @Override // android.view.View
    public void setAccessibilitySelection(int start, int end) {
        if (getAccessibilitySelectionStart() != start || getAccessibilitySelectionEnd() != end) {
            CharSequence text = getIterableTextForAccessibility();
            if (Math.min(start, end) < 0 || Math.max(start, end) > text.length()) {
                Selection.removeSelection((Spannable) text);
            } else {
                Selection.setSelection((Spannable) text, start, end);
            }
            Editor editor = this.mEditor;
            if (editor != null) {
                editor.hideCursorAndSpanControllers();
                this.mEditor.lambda$startActionModeInternal$0$Editor();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
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
        CharSequence charSequence = this.mText;
        if (charSequence != null) {
            str = charSequence.toString();
        }
        stream.addProperty("text:text", str);
        stream.addProperty("text:gravity", this.mGravity);
    }

    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class android.widget.TextView.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        ParcelableParcel editorState;
        CharSequence error;
        boolean frozenWithFocus;
        int selEnd;
        int selStart;
        @UnsupportedAppUsage
        CharSequence text;

        SavedState(Parcelable superState) {
            super(superState);
            this.selStart = -1;
            this.selEnd = -1;
        }

        @Override // android.view.View.BaseSavedState, android.os.Parcelable, android.view.AbsSavedState
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
                str = str + " text=" + ((Object) this.text);
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

    /* access modifiers changed from: private */
    public static class CharWrapper implements CharSequence, GetChars, GraphicsOperations {
        private char[] mChars;
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
            int i;
            if (start >= 0 && end >= 0 && start <= (i = this.mLength) && end <= i) {
                return new String(this.mChars, this.mStart + start, end - start);
            }
            throw new IndexOutOfBoundsException(start + ", " + end);
        }

        @Override // android.text.GetChars
        public void getChars(int start, int end, char[] buf, int off) {
            int i;
            if (start < 0 || end < 0 || start > (i = this.mLength) || end > i) {
                throw new IndexOutOfBoundsException(start + ", " + end);
            }
            System.arraycopy(this.mChars, this.mStart + start, buf, off, end - start);
        }

        @Override // android.text.GraphicsOperations
        public void drawText(BaseCanvas c, int start, int end, float x, float y, Paint p) {
            c.drawText(this.mChars, start + this.mStart, end - start, x, y, p);
        }

        @Override // android.text.GraphicsOperations
        public void drawTextRun(BaseCanvas c, int start, int end, int contextStart, int contextEnd, float x, float y, boolean isRtl, Paint p) {
            char[] cArr = this.mChars;
            int i = this.mStart;
            c.drawTextRun(cArr, start + i, end - start, contextStart + i, contextEnd - contextStart, x, y, isRtl, p);
        }

        @Override // android.text.GraphicsOperations
        public float measureText(int start, int end, Paint p) {
            return p.measureText(this.mChars, this.mStart + start, end - start);
        }

        @Override // android.text.GraphicsOperations
        public int getTextWidths(int start, int end, float[] widths, Paint p) {
            return p.getTextWidths(this.mChars, this.mStart + start, end - start, widths);
        }

        @Override // android.text.GraphicsOperations
        public float getTextRunAdvances(int start, int end, int contextStart, int contextEnd, boolean isRtl, float[] advances, int advancesIndex, Paint p) {
            char[] cArr = this.mChars;
            int i = this.mStart;
            return p.getTextRunAdvances(cArr, start + i, end - start, contextStart + i, contextEnd - contextStart, isRtl, advances, advancesIndex);
        }

        @Override // android.text.GraphicsOperations
        public int getTextRunCursor(int contextStart, int contextEnd, boolean isRtl, int offset, int cursorOpt, Paint p) {
            char[] cArr = this.mChars;
            int i = this.mStart;
            return p.getTextRunCursor(cArr, contextStart + i, contextEnd - contextStart, isRtl, offset + i, cursorOpt);
        }
    }

    /* access modifiers changed from: private */
    public static final class Marquee {
        private static final int MARQUEE_DELAY = 1200;
        private static final float MARQUEE_DELTA_MAX = 0.07f;
        private static final int MARQUEE_DP_PER_SECOND = 30;
        private static final byte MARQUEE_RUNNING = 2;
        private static final byte MARQUEE_STARTING = 1;
        private static final byte MARQUEE_STOPPED = 0;
        private final Choreographer mChoreographer;
        private float mFadeStop;
        private float mGhostOffset;
        private float mGhostStart;
        private long mLastAnimationMs;
        private float mMaxFadeScroll;
        private float mMaxScroll;
        private final float mPixelsPerMs;
        private int mRepeatLimit;
        private Choreographer.FrameCallback mRestartCallback = new Choreographer.FrameCallback() {
            /* class android.widget.TextView.Marquee.AnonymousClass3 */

            @Override // android.view.Choreographer.FrameCallback
            public void doFrame(long frameTimeNanos) {
                if (Marquee.this.mStatus == 2) {
                    if (Marquee.this.mRepeatLimit >= 0) {
                        Marquee.access$910(Marquee.this);
                    }
                    Marquee marquee = Marquee.this;
                    marquee.start(marquee.mRepeatLimit);
                }
            }
        };
        private float mScroll;
        private Choreographer.FrameCallback mStartCallback = new Choreographer.FrameCallback() {
            /* class android.widget.TextView.Marquee.AnonymousClass2 */

            @Override // android.view.Choreographer.FrameCallback
            public void doFrame(long frameTimeNanos) {
                Marquee.this.mStatus = (byte) 2;
                Marquee marquee = Marquee.this;
                marquee.mLastAnimationMs = marquee.mChoreographer.getFrameTime();
                Marquee.this.tick();
            }
        };
        private byte mStatus = 0;
        private Choreographer.FrameCallback mTickCallback = new Choreographer.FrameCallback() {
            /* class android.widget.TextView.Marquee.AnonymousClass1 */

            @Override // android.view.Choreographer.FrameCallback
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
                TextView textView = this.mView.get();
                if (textView == null) {
                    return;
                }
                if (textView.isFocused() || textView.isSelected()) {
                    long currentMs = this.mChoreographer.getFrameTime();
                    this.mLastAnimationMs = currentMs;
                    this.mScroll += ((float) (currentMs - this.mLastAnimationMs)) * this.mPixelsPerMs;
                    float f = this.mScroll;
                    float f2 = this.mMaxScroll;
                    if (f > f2) {
                        this.mScroll = f2;
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
            TextView textView = this.mView.get();
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
            TextView textView = this.mView.get();
            if (textView != null && textView.mLayout != null) {
                this.mStatus = 1;
                this.mScroll = 0.0f;
                int textWidth = (textView.getWidth() - textView.getCompoundPaddingLeft()) - textView.getCompoundPaddingRight();
                float lineWidth = textView.mLayout.getLineWidth(0);
                float gap = ((float) textWidth) / 3.0f;
                this.mGhostStart = (lineWidth - ((float) textWidth)) + gap;
                float f = this.mGhostStart;
                this.mMaxScroll = ((float) textWidth) + f;
                this.mGhostOffset = lineWidth + gap;
                this.mFadeStop = (((float) textWidth) / 6.0f) + lineWidth;
                this.mMaxFadeScroll = f + lineWidth + lineWidth;
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

    /* access modifiers changed from: private */
    public class ChangeWatcher implements TextWatcher, SpanWatcher {
        private CharSequence mBeforeText;

        private ChangeWatcher() {
        }

        @Override // android.text.TextWatcher
        public void beforeTextChanged(CharSequence buffer, int start, int before, int after) {
            if (AccessibilityManager.getInstance(TextView.this.mContext).isEnabled() && TextView.this.mTransformed != null) {
                this.mBeforeText = TextView.this.mTransformed.toString();
            }
            TextView.this.sendBeforeTextChanged(buffer, start, before, after);
        }

        @Override // android.text.TextWatcher
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

        @Override // android.text.TextWatcher
        public void afterTextChanged(Editable buffer) {
            TextView.this.sendAfterTextChanged(buffer);
            if (MetaKeyKeyListener.getMetaState(buffer, 2048) != 0) {
                MetaKeyKeyListener.stopSelecting(TextView.this, buffer);
            }
        }

        @Override // android.text.SpanWatcher
        public void onSpanChanged(Spannable buf, Object what, int s, int e, int st, int en) {
            TextView.this.spanChange(buf, what, s, st, e, en);
        }

        @Override // android.text.SpanWatcher
        public void onSpanAdded(Spannable buf, Object what, int s, int e) {
            TextView.this.spanChange(buf, what, -1, s, -1, e);
        }

        @Override // android.text.SpanWatcher
        public void onSpanRemoved(Spannable buf, Object what, int s, int e) {
            TextView.this.spanChange(buf, what, s, -1, e, -1);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
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
        Editor editor = this.mEditor;
        if (editor != null) {
            editor.selectAllAndShowEditor();
        }
    }

    public void addTextCopyFinishedListener(TextCopyFinishedListener listener) {
        this.textCopyFinishedListener = listener;
    }

    public TextCopyFinishedListener getTextCopyFinishedListener() {
        return this.textCopyFinishedListener;
    }

    public void setHwCompoundPadding(int left, int top, int right, int bottom) {
        this.mHwCompoundPaddingLeft = left;
    }
}
