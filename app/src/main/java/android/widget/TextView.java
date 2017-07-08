package android.widget;

import android.common.HwFrameworkFactory;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.hwcontrol.HwWidgetFactory;
import android.hwcontrol.HwWidgetFactory.HwTextView;
import android.hwtheme.HwThemeManager;
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
import android.provider.Settings.Secure;
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
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
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
import android.util.FloatMath;
import android.util.Log;
import android.util.TypedValue;
import android.view.AccessibilityIterators.TextSegmentIterator;
import android.view.ActionMode.Callback;
import android.view.Choreographer;
import android.view.Choreographer.FrameCallback;
import android.view.ContextMenu;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.IHwNsdImpl;
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
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.textservice.SpellCheckerSubtype;
import android.view.textservice.TextServicesManager;
import android.widget.RemoteViews.RemoteView;
import com.android.ims.ImsConferenceState;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.os.HwBootFail;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.FastMath;
import com.android.internal.util.Protocol;
import com.android.internal.widget.EditableInputConnection;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import com.nxp.nfc.gsma.internal.NxpNfcController;
import huawei.cust.HwCfgFilePolicy;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;
import javax.microedition.khronos.opengles.GL10;
import org.xmlpull.v1.XmlPullParserException;

@RemoteView
public class TextView extends View implements OnPreDrawListener {
    private static final /* synthetic */ int[] -android-text-Layout$AlignmentSwitchesValues = null;
    static final int ACCESSIBILITY_ACTION_PROCESS_TEXT_START_ID = 268435712;
    private static final int ACCESSIBILITY_ACTION_SHARE = 268435456;
    private static final int ANIMATED_SCROLL_GAP = 250;
    private static final int CHANGE_WATCHER_PRIORITY = 100;
    static final boolean DEBUG_EXTRACT = false;
    private static final int DECIMAL = 4;
    private static final int DEVICE_PROVISIONED_NO = 1;
    private static final int DEVICE_PROVISIONED_UNKNOWN = 0;
    private static final int DEVICE_PROVISIONED_YES = 2;
    private static final Spanned EMPTY_SPANNED = null;
    private static final int EMS = 1;
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
    private static final int[] MULTILINE_STATE_SET = null;
    private static final InputFilter[] NO_FILTERS = null;
    private static final int PIXELS = 2;
    static final int PROCESS_TEXT_REQUEST_CODE = 100;
    private static final int SANS = 1;
    private static final int SERIF = 2;
    private static final int SIGNED = 2;
    private static final RectF TEMP_RECTF = null;
    private static final Metrics UNKNOWN_BORING = null;
    static final int VERY_WIDE = 1048576;
    private static boolean mIsVibrateImplemented;
    static int sCurrentTextViewHash;
    static int sLastCursorX;
    static int sLastCursorY;
    static long sLastCutCopyOrTextChangedTime;
    int lastZorder;
    private boolean mAllowTransformationLengthChange;
    private int mAutoLinkMask;
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
    private int mLastLayoutDirection;
    long mLastNSDDrawCursorTime;
    boolean mLastNSDDrawRet;
    private long mLastScroll;
    private Layout mLayout;
    private ColorStateList mLinkTextColor;
    private boolean mLinksClickable;
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
    int mNSDRefreshTimes;
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
    @ExportedProperty(category = "text")
    private CharSequence mText;
    private ColorStateList mTextColor;
    private TextDirectionHeuristic mTextDir;
    int mTextEditSuggestionContainerLayout;
    int mTextEditSuggestionHighlightStyle;
    int mTextEditSuggestionItemLayout;
    private final TextPaint mTextPaint;
    int mTextSelectHandleLeftRes;
    int mTextSelectHandleRes;
    int mTextSelectHandleRightRes;
    private int mTextViewDirection;
    private TransformationMethod mTransformation;
    private CharSequence mTransformed;
    protected boolean mTrySelectAllAndShowEditor;
    private boolean mUserSetTextScaleX;
    private boolean mValidSetCursorEvent;
    private TextCopyFinishedListener textCopyFinishedListener;

    public interface OnEditorActionListener {
        boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent);
    }

    /* renamed from: android.widget.TextView.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ CharSequence val$error;

        AnonymousClass1(CharSequence val$error) {
            this.val$error = val$error;
        }

        public void run() {
            if (TextView.this.mEditor == null || !TextView.this.mEditor.mErrorWasChanged) {
                TextView.this.setError(this.val$error);
            }
        }
    }

    public enum BufferType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.TextView.BufferType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.TextView.BufferType.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.widget.TextView.BufferType.<clinit>():void");
        }
    }

    private class ChangeWatcher implements TextWatcher, SpanWatcher {
        private CharSequence mBeforeText;
        final /* synthetic */ TextView this$0;

        /* synthetic */ ChangeWatcher(TextView this$0, ChangeWatcher changeWatcher) {
            this(this$0);
        }

        private ChangeWatcher(TextView this$0) {
            this.this$0 = this$0;
        }

        public void beforeTextChanged(CharSequence buffer, int start, int before, int after) {
            if (AccessibilityManager.getInstance(this.this$0.mContext).isEnabled() && (!(TextView.isPasswordInputType(this.this$0.getInputType()) || this.this$0.hasPasswordTransformationMethod()) || this.this$0.shouldSpeakPasswordsForAccessibility())) {
                this.mBeforeText = buffer.toString();
            }
            this.this$0.sendBeforeTextChanged(buffer, start, before, after);
        }

        public void onTextChanged(CharSequence buffer, int start, int before, int after) {
            this.this$0.handleTextChanged(buffer, start, before, after);
            if (!AccessibilityManager.getInstance(this.this$0.mContext).isEnabled()) {
                return;
            }
            if (this.this$0.isFocused() || (this.this$0.isSelected() && this.this$0.isShown())) {
                this.this$0.sendAccessibilityEventTypeViewTextChanged(this.mBeforeText, start, before, after);
                this.mBeforeText = null;
            }
        }

        public void afterTextChanged(Editable buffer) {
            this.this$0.sendAfterTextChanged(buffer);
            if (MetaKeyKeyListener.getMetaState((CharSequence) buffer, (int) GL10.GL_EXP) != 0) {
                MetaKeyKeyListener.stopSelecting(this.this$0, buffer);
            }
        }

        public void onSpanChanged(Spannable buf, Object what, int s, int e, int st, int en) {
            if (!(this.this$0.mHwTextView == null || this.this$0.mEditor == null || !this.this$0.mValidSetCursorEvent)) {
                this.this$0.mHwTextView.playIvtEffect(this.this$0.mContext, "TEXTVIEW_SETCURSOR", what, s, en);
            }
            this.this$0.spanChange(buf, what, s, st, e, en);
        }

        public void onSpanAdded(Spannable buf, Object what, int s, int e) {
            this.this$0.spanChange(buf, what, TextView.KEY_EVENT_HANDLED, s, TextView.KEY_EVENT_HANDLED, e);
        }

        public void onSpanRemoved(Spannable buf, Object what, int s, int e) {
            this.this$0.spanChange(buf, what, s, TextView.KEY_EVENT_HANDLED, e, TextView.KEY_EVENT_HANDLED);
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

        public void drawText(Canvas c, int start, int end, float x, float y, Paint p) {
            c.drawText(this.mChars, start + this.mStart, end - start, x, y, p);
        }

        public void drawTextRun(Canvas c, int start, int end, int contextStart, int contextEnd, float x, float y, boolean isRtl, Paint p) {
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
        final Rect mCompoundRect;
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
        int mDrawableSaved;
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
        final Drawable[] mShowing;
        ColorStateList mTintList;
        Mode mTintMode;

        public Drawables(Context context) {
            boolean z = true;
            this.mCompoundRect = new Rect();
            this.mShowing = new Drawable[TextView.DECIMAL];
            this.mDrawableSaved = DRAWABLE_NONE;
            if (context.getApplicationInfo().targetSdkVersion >= 17 && context.getApplicationInfo().hasRtlSupport()) {
                z = TextView.DEBUG_EXTRACT;
            }
            this.mIsRtlCompatibilityMode = z;
            this.mOverride = TextView.DEBUG_EXTRACT;
        }

        public boolean hasMetadata() {
            return (this.mDrawablePadding != 0 || this.mHasTintMode) ? true : this.mHasTint;
        }

        public boolean resolveWithLayoutDirection(int layoutDirection) {
            Drawable previousLeft = this.mShowing[LEFT];
            Drawable previousRight = this.mShowing[RIGHT];
            this.mShowing[LEFT] = this.mDrawableLeftInitial;
            this.mShowing[RIGHT] = this.mDrawableRightInitial;
            if (!this.mIsRtlCompatibilityMode) {
                switch (layoutDirection) {
                    case TOP /*1*/:
                        if (this.mOverride) {
                            this.mShowing[RIGHT] = this.mDrawableStart;
                            this.mDrawableSizeRight = this.mDrawableSizeStart;
                            this.mDrawableHeightRight = this.mDrawableHeightStart;
                            this.mShowing[LEFT] = this.mDrawableEnd;
                            this.mDrawableSizeLeft = this.mDrawableSizeEnd;
                            this.mDrawableHeightLeft = this.mDrawableHeightEnd;
                            break;
                        }
                        break;
                    default:
                        if (this.mOverride) {
                            this.mShowing[LEFT] = this.mDrawableStart;
                            this.mDrawableSizeLeft = this.mDrawableSizeStart;
                            this.mDrawableHeightLeft = this.mDrawableHeightStart;
                            this.mShowing[RIGHT] = this.mDrawableEnd;
                            this.mDrawableSizeRight = this.mDrawableSizeEnd;
                            this.mDrawableHeightRight = this.mDrawableHeightEnd;
                            break;
                        }
                        break;
                }
            }
            if (this.mDrawableStart != null && this.mShowing[LEFT] == null) {
                this.mShowing[LEFT] = this.mDrawableStart;
                this.mDrawableSizeLeft = this.mDrawableSizeStart;
                this.mDrawableHeightLeft = this.mDrawableHeightStart;
            }
            if (this.mDrawableEnd != null && this.mShowing[RIGHT] == null) {
                this.mShowing[RIGHT] = this.mDrawableEnd;
                this.mDrawableSizeRight = this.mDrawableSizeEnd;
                this.mDrawableHeightRight = this.mDrawableHeightEnd;
            }
            applyErrorDrawableIfNeeded(layoutDirection);
            if (this.mShowing[LEFT] == previousLeft && this.mShowing[RIGHT] == previousRight) {
                return TextView.DEBUG_EXTRACT;
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
            this.mDrawableHeightError = LEFT;
            this.mDrawableSizeError = LEFT;
        }

        private void applyErrorDrawableIfNeeded(int layoutDirection) {
            switch (this.mDrawableSaved) {
                case LEFT /*0*/:
                    this.mShowing[RIGHT] = this.mDrawableTemp;
                    this.mDrawableSizeRight = this.mDrawableSizeTemp;
                    this.mDrawableHeightRight = this.mDrawableHeightTemp;
                    break;
                case TOP /*1*/:
                    this.mShowing[LEFT] = this.mDrawableTemp;
                    this.mDrawableSizeLeft = this.mDrawableSizeTemp;
                    this.mDrawableHeightLeft = this.mDrawableHeightTemp;
                    break;
            }
            if (this.mDrawableError != null) {
                switch (layoutDirection) {
                    case TOP /*1*/:
                        this.mDrawableSaved = TOP;
                        this.mDrawableTemp = this.mShowing[LEFT];
                        this.mDrawableSizeTemp = this.mDrawableSizeLeft;
                        this.mDrawableHeightTemp = this.mDrawableHeightLeft;
                        this.mShowing[LEFT] = this.mDrawableError;
                        this.mDrawableSizeLeft = this.mDrawableSizeError;
                        this.mDrawableHeightLeft = this.mDrawableHeightError;
                    default:
                        this.mDrawableSaved = LEFT;
                        this.mDrawableTemp = this.mShowing[RIGHT];
                        this.mDrawableSizeTemp = this.mDrawableSizeRight;
                        this.mDrawableHeightTemp = this.mDrawableHeightRight;
                        this.mShowing[RIGHT] = this.mDrawableError;
                        this.mDrawableSizeRight = this.mDrawableSizeError;
                        this.mDrawableHeightRight = this.mDrawableHeightError;
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
        private FrameCallback mRestartCallback;
        private float mScroll;
        private FrameCallback mStartCallback;
        private byte mStatus;
        private FrameCallback mTickCallback;
        private final WeakReference<TextView> mView;

        /* renamed from: android.widget.TextView.Marquee.1 */
        class AnonymousClass1 implements FrameCallback {
            final /* synthetic */ Marquee this$1;

            AnonymousClass1(Marquee this$1) {
                this.this$1 = this$1;
            }

            public void doFrame(long frameTimeNanos) {
                this.this$1.tick();
            }
        }

        /* renamed from: android.widget.TextView.Marquee.2 */
        class AnonymousClass2 implements FrameCallback {
            final /* synthetic */ Marquee this$1;

            AnonymousClass2(Marquee this$1) {
                this.this$1 = this$1;
            }

            public void doFrame(long frameTimeNanos) {
                this.this$1.mStatus = Marquee.MARQUEE_RUNNING;
                this.this$1.mLastAnimationMs = this.this$1.mChoreographer.getFrameTime();
                this.this$1.tick();
            }
        }

        /* renamed from: android.widget.TextView.Marquee.3 */
        class AnonymousClass3 implements FrameCallback {
            final /* synthetic */ Marquee this$1;

            AnonymousClass3(Marquee this$1) {
                this.this$1 = this$1;
            }

            public void doFrame(long frameTimeNanos) {
                if (this.this$1.mStatus == TextView.SIGNED) {
                    if (this.this$1.mRepeatLimit >= 0) {
                        Marquee marquee = this.this$1;
                        marquee.mRepeatLimit = marquee.mRepeatLimit + TextView.KEY_EVENT_HANDLED;
                    }
                    this.this$1.start(this.this$1.mRepeatLimit);
                }
            }
        }

        Marquee(TextView v) {
            this.mStatus = (byte) 0;
            this.mTickCallback = new AnonymousClass1(this);
            this.mStartCallback = new AnonymousClass2(this);
            this.mRestartCallback = new AnonymousClass3(this);
            this.mPixelsPerSecond = 30.0f * v.getContext().getResources().getDisplayMetrics().density;
            this.mView = new WeakReference(v);
            this.mChoreographer = Choreographer.getInstance();
        }

        void tick() {
            if (this.mStatus == TextView.SIGNED) {
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
                float lineWidth = textView.mLayout.getLineWidth(TextView.MARQUEE_FADE_NORMAL);
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
            return this.mScroll <= this.mFadeStop ? true : TextView.DEBUG_EXTRACT;
        }

        boolean shouldDrawGhost() {
            return (this.mStatus != TextView.SIGNED || this.mScroll <= this.mGhostStart) ? TextView.DEBUG_EXTRACT : true;
        }

        boolean isRunning() {
            return this.mStatus == TextView.SIGNED ? true : TextView.DEBUG_EXTRACT;
        }

        boolean isStopped() {
            return this.mStatus == null ? true : TextView.DEBUG_EXTRACT;
        }
    }

    public static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = null;
        ParcelableParcel editorState;
        CharSequence error;
        boolean frozenWithFocus;
        int selEnd;
        int selStart;
        CharSequence text;

        /* renamed from: android.widget.TextView.SavedState.1 */
        static class AnonymousClass1 implements Creator<SavedState> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m6createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public /* bridge */ /* synthetic */ Object[] m7newArray(int size) {
                return newArray(size);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.TextView.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.TextView.SavedState.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.widget.TextView.SavedState.<clinit>():void");
        }

        /* synthetic */ SavedState(Parcel in, SavedState savedState) {
            this(in);
        }

        SavedState(Parcelable superState) {
            super(superState);
            this.selStart = TextView.KEY_EVENT_HANDLED;
            this.selEnd = TextView.KEY_EVENT_HANDLED;
        }

        public void writeToParcel(Parcel out, int flags) {
            int i;
            super.writeToParcel(out, flags);
            out.writeInt(this.selStart);
            out.writeInt(this.selEnd);
            if (this.frozenWithFocus) {
                i = TextView.SANS;
            } else {
                i = TextView.MARQUEE_FADE_NORMAL;
            }
            out.writeInt(i);
            TextUtils.writeToParcel(this.text, out, flags);
            if (this.error == null) {
                out.writeInt(TextView.MARQUEE_FADE_NORMAL);
            } else {
                out.writeInt(TextView.SANS);
                TextUtils.writeToParcel(this.error, out, flags);
            }
            if (this.editorState == null) {
                out.writeInt(TextView.MARQUEE_FADE_NORMAL);
                return;
            }
            out.writeInt(TextView.SANS);
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
            boolean z = TextView.DEBUG_EXTRACT;
            super(in);
            this.selStart = TextView.KEY_EVENT_HANDLED;
            this.selEnd = TextView.KEY_EVENT_HANDLED;
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
            iArr[Alignment.ALIGN_CENTER.ordinal()] = SANS;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Alignment.ALIGN_LEFT.ordinal()] = SIGNED;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Alignment.ALIGN_NORMAL.ordinal()] = MONOSPACE;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Alignment.ALIGN_OPPOSITE.ordinal()] = DECIMAL;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Alignment.ALIGN_RIGHT.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        -android-text-Layout$AlignmentSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.TextView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.TextView.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.widget.TextView.<clinit>():void");
    }

    public void setTypeface(android.graphics.Typeface r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.TextView.setTypeface(android.graphics.Typeface, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.TextView.setTypeface(android.graphics.Typeface, int):void");
    }

    public static void preloadFontCache() {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.measureText("H");
    }

    public TextView(Context context) {
        this(context, null);
    }

    public TextView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.textViewStyle);
    }

    public TextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, MARQUEE_FADE_NORMAL);
    }

    public TextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        int n;
        int i;
        int attr;
        int i2;
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mHwTextView = null;
        this.mValidSetCursorEvent = DEBUG_EXTRACT;
        this.mEditableFactory = Factory.getInstance();
        this.mSpannableFactory = Spannable.Factory.getInstance();
        this.mTextViewDirection = SANS;
        this.mSoftwareBold = DEBUG_EXTRACT;
        this.mMarqueeRepeatLimit = MONOSPACE;
        this.mLastLayoutDirection = KEY_EVENT_HANDLED;
        this.mMarqueeFadeMode = MARQUEE_FADE_NORMAL;
        this.mBufferType = BufferType.NORMAL;
        this.mLocalesChanged = DEBUG_EXTRACT;
        this.mGravity = 8388659;
        this.mLinksClickable = true;
        this.mSpacingMult = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        this.mSpacingAdd = 0.0f;
        this.mMaximum = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mMaxMode = SANS;
        this.mMinimum = MARQUEE_FADE_NORMAL;
        this.mMinMode = SANS;
        this.mOldMaximum = this.mMaximum;
        this.mOldMaxMode = this.mMaxMode;
        this.mMaxWidth = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mMaxWidthMode = SIGNED;
        this.mMinWidth = MARQUEE_FADE_NORMAL;
        this.mMinWidthMode = SIGNED;
        this.mDesiredHeightAtMeasure = KEY_EVENT_HANDLED;
        this.mIncludePad = true;
        this.mDeferScroll = KEY_EVENT_HANDLED;
        this.mFilters = NO_FILTERS;
        this.mHighlightColor = 1714664933;
        this.mHighlightPathBogus = true;
        this.mDeviceProvisionedState = MARQUEE_FADE_NORMAL;
        this.mNSDRefreshTimes = MARQUEE_FADE_NORMAL;
        this.mLastNSDDrawRet = true;
        this.mLastNSDDrawCursorTime = 0;
        this.lastZorder = MARQUEE_FADE_NORMAL;
        if (attrs != null) {
            this.mStyle = attrs.getStyleAttribute();
        }
        this.mHwTextView = HwWidgetFactory.getHwTextView(context, this, attrs);
        this.mText = "";
        Resources res = getResources();
        CompatibilityInfo compat = res.getCompatibilityInfo();
        this.mTextPaint = new TextPaint((int) SANS);
        this.mTextPaint.density = res.getDisplayMetrics().density;
        this.mTextPaint.setCompatibilityScaling(compat.applicationScale);
        this.mHighlightPaint = new Paint(SANS);
        this.mHighlightPaint.setCompatibilityScaling(compat.applicationScale);
        this.mMovement = getDefaultMovementMethod();
        this.mTransformation = null;
        int textColorHighlight = MARQUEE_FADE_NORMAL;
        ColorStateList colorStateList = null;
        ColorStateList colorStateList2 = null;
        ColorStateList colorStateList3 = null;
        int textSize = 15;
        String str = null;
        boolean fontFamilyExplicit = DEBUG_EXTRACT;
        int typefaceIndex = KEY_EVENT_HANDLED;
        int styleIndex = KEY_EVENT_HANDLED;
        boolean z = DEBUG_EXTRACT;
        int shadowcolor = MARQUEE_FADE_NORMAL;
        float dx = 0.0f;
        float dy = 0.0f;
        float r = 0.0f;
        boolean z2 = DEBUG_EXTRACT;
        float letterSpacing = 0.0f;
        String str2 = null;
        this.mBreakStrategy = MARQUEE_FADE_NORMAL;
        this.mHyphenationFrequency = MARQUEE_FADE_NORMAL;
        Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(attrs, R.styleable.TextViewAppearance, defStyleAttr, defStyleRes);
        TypedArray appearance = null;
        int ap = a.getResourceId(MARQUEE_FADE_NORMAL, KEY_EVENT_HANDLED);
        a.recycle();
        if (ap != KEY_EVENT_HANDLED) {
            appearance = theme.obtainStyledAttributes(ap, R.styleable.TextAppearance);
        }
        if (appearance != null) {
            n = appearance.getIndexCount();
            for (i = MARQUEE_FADE_NORMAL; i < n; i += SANS) {
                attr = appearance.getIndex(i);
                switch (attr) {
                    case MARQUEE_FADE_NORMAL /*0*/:
                        textSize = appearance.getDimensionPixelSize(attr, textSize);
                        break;
                    case SANS /*1*/:
                        typefaceIndex = appearance.getInt(attr, KEY_EVENT_HANDLED);
                        break;
                    case SIGNED /*2*/:
                        styleIndex = appearance.getInt(attr, KEY_EVENT_HANDLED);
                        break;
                    case MONOSPACE /*3*/:
                        colorStateList = appearance.getColorStateList(attr);
                        break;
                    case DECIMAL /*4*/:
                        textColorHighlight = appearance.getColor(attr, textColorHighlight);
                        break;
                    case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                        colorStateList2 = appearance.getColorStateList(attr);
                        break;
                    case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                        colorStateList3 = appearance.getColorStateList(attr);
                        break;
                    case HwCfgFilePolicy.CLOUD_APN /*7*/:
                        shadowcolor = HwThemeManager.getShadowcolor(appearance, attr);
                        break;
                    case PGSdk.TYPE_VIDEO /*8*/:
                        dx = appearance.getFloat(attr, 0.0f);
                        break;
                    case PGSdk.TYPE_SCRLOCK /*9*/:
                        dy = appearance.getFloat(attr, 0.0f);
                        break;
                    case PGSdk.TYPE_CLOCK /*10*/:
                        r = appearance.getFloat(attr, 0.0f);
                        break;
                    case PGSdk.TYPE_IM /*11*/:
                        z = appearance.getBoolean(attr, DEBUG_EXTRACT);
                        break;
                    case PGSdk.TYPE_MUSIC /*12*/:
                        str = appearance.getString(attr);
                        break;
                    case HwPerformance.PERF_VAL_DEV_TYPE_MAX /*13*/:
                        z2 = appearance.getBoolean(attr, DEBUG_EXTRACT);
                        break;
                    case StatisticalConstant.TYPE_FINGER_BIAS_SPLIT_RIGHT /*14*/:
                        letterSpacing = appearance.getFloat(attr, 0.0f);
                        break;
                    case IndexSearchConstants.INDEX_BUILD_OP_MASK /*15*/:
                        str2 = appearance.getString(attr);
                        break;
                    default:
                        break;
                }
            }
            appearance.recycle();
        }
        boolean editable = getDefaultEditable();
        CharSequence inputMethod = null;
        int numeric = MARQUEE_FADE_NORMAL;
        CharSequence digits = null;
        boolean phone = DEBUG_EXTRACT;
        boolean autotext = DEBUG_EXTRACT;
        int autocap = KEY_EVENT_HANDLED;
        int buffertype = MARQUEE_FADE_NORMAL;
        boolean selectallonfocus = DEBUG_EXTRACT;
        Drawable drawableLeft = null;
        Drawable drawableTop = null;
        Drawable drawableRight = null;
        Drawable drawableBottom = null;
        Drawable drawableStart = null;
        Drawable drawableEnd = null;
        ColorStateList drawableTint = null;
        Mode drawableTintMode = null;
        int drawablePadding = MARQUEE_FADE_NORMAL;
        int ellipsize = KEY_EVENT_HANDLED;
        boolean singleLine = DEBUG_EXTRACT;
        int maxlength = KEY_EVENT_HANDLED;
        CharSequence text = "";
        CharSequence hint = null;
        boolean password = DEBUG_EXTRACT;
        int inputType = MARQUEE_FADE_NORMAL;
        a = theme.obtainStyledAttributes(attrs, R.styleable.TextView, defStyleAttr, defStyleRes);
        n = a.getIndexCount();
        Locale loc = Locale.getDefault();
        if ("my".equals(loc.getLanguage()) && "MM".equals(loc.getCountry())) {
            this.mSpacingMult = 1.18f;
        }
        for (i = MARQUEE_FADE_NORMAL; i < n; i += SANS) {
            attr = a.getIndex(i);
            switch (attr) {
                case MARQUEE_FADE_NORMAL /*0*/:
                    setEnabled(a.getBoolean(attr, isEnabled()));
                    break;
                case SIGNED /*2*/:
                    textSize = a.getDimensionPixelSize(attr, textSize);
                    break;
                case MONOSPACE /*3*/:
                    typefaceIndex = a.getInt(attr, typefaceIndex);
                    break;
                case DECIMAL /*4*/:
                    styleIndex = a.getInt(attr, styleIndex);
                    break;
                case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                    colorStateList = a.getColorStateList(attr);
                    break;
                case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                    textColorHighlight = a.getColor(attr, textColorHighlight);
                    break;
                case HwCfgFilePolicy.CLOUD_APN /*7*/:
                    colorStateList2 = a.getColorStateList(attr);
                    break;
                case PGSdk.TYPE_VIDEO /*8*/:
                    colorStateList3 = a.getColorStateList(attr);
                    break;
                case PGSdk.TYPE_SCRLOCK /*9*/:
                    ellipsize = a.getInt(attr, ellipsize);
                    break;
                case PGSdk.TYPE_CLOCK /*10*/:
                    setGravity(a.getInt(attr, KEY_EVENT_HANDLED));
                    break;
                case PGSdk.TYPE_IM /*11*/:
                    this.mAutoLinkMask = a.getInt(attr, MARQUEE_FADE_NORMAL);
                    break;
                case PGSdk.TYPE_MUSIC /*12*/:
                    this.mLinksClickable = a.getBoolean(attr, true);
                    break;
                case HwPerformance.PERF_VAL_DEV_TYPE_MAX /*13*/:
                    setMaxWidth(a.getDimensionPixelSize(attr, KEY_EVENT_HANDLED));
                    break;
                case StatisticalConstant.TYPE_FINGER_BIAS_SPLIT_RIGHT /*14*/:
                    setMaxHeight(a.getDimensionPixelSize(attr, KEY_EVENT_HANDLED));
                    break;
                case IndexSearchConstants.INDEX_BUILD_OP_MASK /*15*/:
                    setMinWidth(a.getDimensionPixelSize(attr, KEY_EVENT_HANDLED));
                    break;
                case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
                    setMinHeight(a.getDimensionPixelSize(attr, KEY_EVENT_HANDLED));
                    break;
                case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                    buffertype = a.getInt(attr, buffertype);
                    break;
                case PerfHub.PERF_TAG_IPA_CONTROL_TEMP /*18*/:
                    text = a.getText(attr);
                    break;
                case PerfHub.PERF_TAG_IPA_SUSTAINABLE_POWER /*19*/:
                    hint = a.getText(attr);
                    break;
                case HwPerformance.PERF_TAG_TASK_FORK_ON_B_CLUSTER /*20*/:
                    setTextScaleX(a.getFloat(attr, LayoutParams.BRIGHTNESS_OVERRIDE_FULL));
                    break;
                case HwPerformance.PERF_TAG_DEF_L_CPU_MIN /*21*/:
                    if (!a.getBoolean(attr, true)) {
                        setCursorVisible(DEBUG_EXTRACT);
                        break;
                    }
                    break;
                case HwPerformance.PERF_TAG_DEF_L_CPU_MAX /*22*/:
                    setMaxLines(a.getInt(attr, KEY_EVENT_HANDLED));
                    break;
                case HwPerformance.PERF_TAG_DEF_B_CPU_MIN /*23*/:
                    setLines(a.getInt(attr, KEY_EVENT_HANDLED));
                    break;
                case HwPerformance.PERF_TAG_DEF_B_CPU_MAX /*24*/:
                    setHeight(a.getDimensionPixelSize(attr, KEY_EVENT_HANDLED));
                    break;
                case PerfHub.PERF_TAG_DEF_GPU_MIN /*25*/:
                    setMinLines(a.getInt(attr, KEY_EVENT_HANDLED));
                    break;
                case PerfHub.PERF_TAG_DEF_GPU_MAX /*26*/:
                    setMaxEms(a.getInt(attr, KEY_EVENT_HANDLED));
                    break;
                case PerfHub.PERF_TAG_DEF_DDR_MIN /*27*/:
                    setEms(a.getInt(attr, KEY_EVENT_HANDLED));
                    break;
                case PerfHub.PERF_TAG_DEF_DDR_MAX /*28*/:
                    setWidth(a.getDimensionPixelSize(attr, KEY_EVENT_HANDLED));
                    break;
                case PerfHub.PERF_TAG_DEF_HMP_UP_THRES /*29*/:
                    setMinEms(a.getInt(attr, KEY_EVENT_HANDLED));
                    break;
                case StatisticalConstant.TYPE_POWERKEY_MAXXXXXX /*30*/:
                    if (!a.getBoolean(attr, DEBUG_EXTRACT)) {
                        break;
                    }
                    setHorizontallyScrolling(true);
                    break;
                case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CHANGE /*31*/:
                    password = a.getBoolean(attr, password);
                    break;
                case IndexSearchConstants.INDEX_BUILD_FLAG_INTERNAL_FILE /*32*/:
                    singleLine = a.getBoolean(attr, singleLine);
                    break;
                case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
                    selectallonfocus = a.getBoolean(attr, selectallonfocus);
                    break;
                case StatisticalConstant.TYPE_MULTIWINDOW_FRAME_SIZE_CHANGED /*34*/:
                    if (!a.getBoolean(attr, true)) {
                        setIncludeFontPadding(DEBUG_EXTRACT);
                        break;
                    }
                    break;
                case PerfHub.PERF_TAG_AVL_B_CPU_FREQ_LIST /*35*/:
                    maxlength = a.getInt(attr, KEY_EVENT_HANDLED);
                    break;
                case PerfHub.PERF_TAG_AVL_GPU_FREQ_LIST /*36*/:
                    shadowcolor = HwThemeManager.getShadowcolor(a, attr);
                    break;
                case PerfHub.PERF_TAG_AVL_DDR_FREQ_LIST /*37*/:
                    dx = a.getFloat(attr, 0.0f);
                    break;
                case PerfHub.PERF_TAG_CTRL_TYPE_NEW /*38*/:
                    dy = a.getFloat(attr, 0.0f);
                    break;
                case PerfHub.PERF_TAG_MAX /*39*/:
                    r = a.getFloat(attr, 0.0f);
                    break;
                case StatisticalConstant.TYPE_SINGLEHAND_START /*40*/:
                    numeric = a.getInt(attr, numeric);
                    break;
                case StatisticalConstant.TYPE_SINGLEHAND_ENTER /*41*/:
                    digits = a.getText(attr);
                    break;
                case StatisticalConstant.TYPE_SINGLEHAND_EXIT /*42*/:
                    phone = a.getBoolean(attr, phone);
                    break;
                case StatisticalConstant.TYPE_SINGLEHAND_ENTER_2S_EXIT /*43*/:
                    inputMethod = a.getText(attr);
                    break;
                case RILConstants.RIL_REQUEST_CHANGE_BARRING_PASSWORD /*44*/:
                    autocap = a.getInt(attr, autocap);
                    break;
                case RILConstants.RIL_REQUEST_QUERY_NETWORK_SELECTION_MODE /*45*/:
                    autotext = a.getBoolean(attr, autotext);
                    break;
                case RILConstants.RIL_REQUEST_SET_NETWORK_SELECTION_AUTOMATIC /*46*/:
                    editable = a.getBoolean(attr, editable);
                    break;
                case RILConstants.RIL_REQUEST_SET_NETWORK_SELECTION_MANUAL /*47*/:
                    this.mFreezesText = a.getBoolean(attr, DEBUG_EXTRACT);
                    break;
                case IndexSearchConstants.INDEX_BUILD_FLAG_EXTERNAL_FILE /*48*/:
                    drawableTop = a.getDrawable(attr);
                    break;
                case RILConstants.RIL_REQUEST_DTMF_START /*49*/:
                    drawableBottom = a.getDrawable(attr);
                    break;
                case StatisticalConstant.TYPE_SINGLEHAND_END /*50*/:
                    drawableLeft = a.getDrawable(attr);
                    break;
                case StatisticalConstant.TYPE_WIFI_SURFING /*51*/:
                    drawableRight = a.getDrawable(attr);
                    break;
                case StatisticalConstant.TYPE_WIFI_OPERATION_INFO /*52*/:
                    drawablePadding = a.getDimensionPixelSize(attr, drawablePadding);
                    break;
                case StatisticalConstant.TYPE_WIFI_DISCONNECT /*53*/:
                    this.mSpacingAdd = (float) a.getDimensionPixelSize(attr, (int) this.mSpacingAdd);
                    break;
                case StatisticalConstant.TYPE_WIFI_CONNECTION_ACTION /*54*/:
                    this.mSpacingMult = a.getFloat(attr, this.mSpacingMult);
                    break;
                case RILConstants.RIL_REQUEST_QUERY_CLIP /*55*/:
                    setMarqueeRepeatLimit(a.getInt(attr, this.mMarqueeRepeatLimit));
                    break;
                case RILConstants.RIL_REQUEST_LAST_DATA_CALL_FAIL_CAUSE /*56*/:
                    inputType = a.getInt(attr, MARQUEE_FADE_NORMAL);
                    break;
                case RILConstants.RIL_REQUEST_DATA_CALL_LIST /*57*/:
                    setPrivateImeOptions(a.getString(attr));
                    break;
                case RILConstants.RIL_REQUEST_RESET_RADIO /*58*/:
                    try {
                        setInputExtras(a.getResourceId(attr, MARQUEE_FADE_NORMAL));
                        break;
                    } catch (Throwable e) {
                        Log.w(LOG_TAG, "Failure reading input extras", e);
                        break;
                    } catch (Throwable e2) {
                        Log.w(LOG_TAG, "Failure reading input extras", e2);
                        break;
                    }
                case RILConstants.RIL_REQUEST_OEM_HOOK_RAW /*59*/:
                    createEditorIfNeeded();
                    this.mEditor.createInputContentTypeIfNeeded();
                    this.mEditor.mInputContentType.imeOptions = a.getInt(attr, this.mEditor.mInputContentType.imeOptions);
                    break;
                case StatisticalConstant.TYPE_MEDIA_FOUNCTION_STATICS /*60*/:
                    createEditorIfNeeded();
                    this.mEditor.createInputContentTypeIfNeeded();
                    this.mEditor.mInputContentType.imeActionLabel = a.getText(attr);
                    break;
                case StatisticalConstant.TYPE_WIFI_END /*61*/:
                    createEditorIfNeeded();
                    this.mEditor.createInputContentTypeIfNeeded();
                    this.mEditor.mInputContentType.imeActionId = a.getInt(attr, this.mEditor.mInputContentType.imeActionId);
                    break;
                case RILConstants.RIL_REQUEST_SET_SUPP_SVC_NOTIFICATION /*62*/:
                    this.mTextSelectHandleLeftRes = a.getResourceId(attr, MARQUEE_FADE_NORMAL);
                    break;
                case RILConstants.RIL_REQUEST_WRITE_SMS_TO_SIM /*63*/:
                    this.mTextSelectHandleRightRes = a.getResourceId(attr, MARQUEE_FADE_NORMAL);
                    break;
                case RILConstants.RIL_REQUEST_DELETE_SMS_ON_SIM /*64*/:
                    this.mTextSelectHandleRes = a.getResourceId(attr, MARQUEE_FADE_NORMAL);
                    break;
                case RILConstants.RIL_REQUEST_STK_GET_PROFILE /*67*/:
                    setTextIsSelectable(a.getBoolean(attr, DEBUG_EXTRACT));
                    break;
                case StatisticalConstant.TYPE_NAVIGATIONBAR_END /*70*/:
                    this.mCursorDrawableRes = a.getResourceId(attr, MARQUEE_FADE_NORMAL);
                    break;
                case StatisticalConstant.TYPE_SCREEN_SHOT /*71*/:
                    this.mTextEditSuggestionItemLayout = a.getResourceId(attr, MARQUEE_FADE_NORMAL);
                    break;
                case RILConstants.RIL_REQUEST_EXPLICIT_CALL_TRANSFER /*72*/:
                    z = a.getBoolean(attr, DEBUG_EXTRACT);
                    break;
                case RILConstants.RIL_REQUEST_SET_PREFERRED_NETWORK_TYPE /*73*/:
                    drawableStart = a.getDrawable(attr);
                    break;
                case RILConstants.RIL_REQUEST_GET_PREFERRED_NETWORK_TYPE /*74*/:
                    drawableEnd = a.getDrawable(attr);
                    break;
                case RILConstants.RIL_REQUEST_GET_NEIGHBORING_CELL_IDS /*75*/:
                    str = a.getString(attr);
                    fontFamilyExplicit = true;
                    break;
                case RILConstants.RIL_REQUEST_SET_LOCATION_UPDATES /*76*/:
                    z2 = a.getBoolean(attr, DEBUG_EXTRACT);
                    break;
                case RILConstants.RIL_REQUEST_CDMA_SET_SUBSCRIPTION_SOURCE /*77*/:
                    letterSpacing = a.getFloat(attr, 0.0f);
                    break;
                case RILConstants.RIL_REQUEST_CDMA_SET_ROAMING_PREFERENCE /*78*/:
                    str2 = a.getString(attr);
                    break;
                case RILConstants.RIL_REQUEST_CDMA_QUERY_ROAMING_PREFERENCE /*79*/:
                    drawableTint = a.getColorStateList(attr);
                    break;
                case StatisticalConstant.TYPE_SCREEN_SHOT_END /*80*/:
                    drawableTintMode = Drawable.parseTintMode(a.getInt(attr, KEY_EVENT_HANDLED), drawableTintMode);
                    break;
                case StatisticalConstant.TYPE_TOUCH_FORCE_CALL_NAVIGATION /*81*/:
                    this.mBreakStrategy = a.getInt(attr, MARQUEE_FADE_NORMAL);
                    break;
                case StatisticalConstant.TYPE_TOUCH_FORCE_OPEAN_APPLICATION /*82*/:
                    this.mHyphenationFrequency = a.getInt(attr, MARQUEE_FADE_NORMAL);
                    break;
                case RILConstants.RIL_REQUEST_CDMA_QUERY_PREFERRED_VOICE_PRIVACY_MODE /*83*/:
                    createEditorIfNeeded();
                    this.mEditor.mAllowUndo = a.getBoolean(attr, true);
                    break;
                case RILConstants.RIL_REQUEST_CDMA_FLASH /*84*/:
                    this.mTextEditSuggestionContainerLayout = a.getResourceId(attr, MARQUEE_FADE_NORMAL);
                    break;
                case RILConstants.RIL_REQUEST_CDMA_BURST_DTMF /*85*/:
                    this.mTextEditSuggestionHighlightStyle = a.getResourceId(attr, MARQUEE_FADE_NORMAL);
                    break;
                default:
                    break;
            }
        }
        a.recycle();
        BufferType bufferType = BufferType.EDITABLE;
        int variation = inputType & 4095;
        boolean passwordInputType = variation == 129 ? true : DEBUG_EXTRACT;
        boolean webPasswordInputType = variation == 225 ? true : DEBUG_EXTRACT;
        boolean numberPasswordInputType = variation == 18 ? true : DEBUG_EXTRACT;
        Editor editor;
        if (inputMethod == null) {
            if (digits == null) {
                if (inputType == 0) {
                    if (!phone) {
                        if (numeric == 0) {
                            if (!autotext && autocap == KEY_EVENT_HANDLED) {
                                if (!isTextSelectable()) {
                                    if (!editable) {
                                        if (this.mEditor != null) {
                                            this.mEditor.mKeyListener = null;
                                        }
                                        switch (buffertype) {
                                            case MARQUEE_FADE_NORMAL /*0*/:
                                                bufferType = BufferType.NORMAL;
                                                break;
                                            case SANS /*1*/:
                                                bufferType = BufferType.SPANNABLE;
                                                break;
                                            case SIGNED /*2*/:
                                                bufferType = BufferType.EDITABLE;
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                    createEditorIfNeeded();
                                    this.mEditor.mKeyListener = TextKeyListener.getInstance();
                                    this.mEditor.mInputType = SANS;
                                } else {
                                    if (this.mEditor != null) {
                                        this.mEditor.mKeyListener = null;
                                        this.mEditor.mInputType = MARQUEE_FADE_NORMAL;
                                    }
                                    bufferType = BufferType.SPANNABLE;
                                    setMovementMethod(ArrowKeyMovementMethod.getInstance());
                                }
                            } else {
                                Capitalize cap;
                                inputType = SANS;
                                switch (autocap) {
                                    case SANS /*1*/:
                                        cap = Capitalize.SENTENCES;
                                        inputType = GL10.GL_LIGHT1;
                                        break;
                                    case SIGNED /*2*/:
                                        cap = Capitalize.WORDS;
                                        inputType = 8193;
                                        break;
                                    case MONOSPACE /*3*/:
                                        cap = Capitalize.CHARACTERS;
                                        inputType = HwPerformance.PERF_EVENT_PROBE;
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
                            this.mEditor.mKeyListener = DigitsKeyListener.getInstance((numeric & SIGNED) != 0 ? SANS : MARQUEE_FADE_NORMAL, (numeric & DECIMAL) != 0 ? true : DEBUG_EXTRACT);
                            inputType = SIGNED;
                            if ((numeric & SIGNED) != 0) {
                                inputType = PerfHub.PERF_EVENT_RESTART;
                            }
                            if ((numeric & DECIMAL) != 0) {
                                inputType |= AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD;
                            }
                            this.mEditor.mInputType = inputType;
                        }
                    } else {
                        createEditorIfNeeded();
                        this.mEditor.mKeyListener = DialerKeyListener.getInstance();
                        this.mEditor.mInputType = MONOSPACE;
                    }
                } else {
                    setInputType(inputType, true);
                    singleLine = isMultilineInputType(inputType) ? DEBUG_EXTRACT : true;
                }
            } else {
                createEditorIfNeeded();
                this.mEditor.mKeyListener = DigitsKeyListener.getInstance(digits.toString());
                editor = this.mEditor;
                if (inputType != 0) {
                    i2 = inputType;
                } else {
                    i2 = SANS;
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
                    } catch (IncompatibleClassChangeError e3) {
                        this.mEditor.mInputType = SANS;
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
            ellipsize = MONOSPACE;
        }
        switch (ellipsize) {
            case SANS /*1*/:
                setEllipsize(TruncateAt.START);
                break;
            case SIGNED /*2*/:
                setEllipsize(TruncateAt.MIDDLE);
                break;
            case MONOSPACE /*3*/:
                setEllipsize(TruncateAt.END);
                break;
            case DECIMAL /*4*/:
                if (ViewConfiguration.get(context).isFadingMarqueeEnabled()) {
                    setHorizontalFadingEdgeEnabled(true);
                    this.mMarqueeFadeMode = MARQUEE_FADE_NORMAL;
                } else {
                    setHorizontalFadingEdgeEnabled(DEBUG_EXTRACT);
                    this.mMarqueeFadeMode = SANS;
                }
                setEllipsize(TruncateAt.MARQUEE);
                break;
        }
        if (colorStateList == null) {
            colorStateList = ColorStateList.valueOf(View.MEASURED_STATE_MASK);
        }
        setTextColor(colorStateList);
        setHintTextColor(colorStateList2);
        setLinkTextColor(colorStateList3);
        if (textColorHighlight != 0) {
            setHighlightColor(textColorHighlight);
        }
        setRawTextSize((float) textSize);
        setElegantTextHeight(z2);
        setLetterSpacing(letterSpacing);
        setFontFeatureSettings(str2);
        if (z) {
            setTransformationMethod(new AllCapsTransformationMethod(getContext()));
        }
        if (password || passwordInputType || webPasswordInputType || numberPasswordInputType) {
            setTransformationMethod(PasswordTransformationMethod.getInstance());
            typefaceIndex = MONOSPACE;
        } else if (this.mEditor != null) {
            i2 = this.mEditor.mInputType & 4095;
            if (r0 == 129) {
                typefaceIndex = MONOSPACE;
            }
        }
        if (!(typefaceIndex == KEY_EVENT_HANDLED || fontFamilyExplicit)) {
            str = null;
        }
        setTypefaceFromAttrs(str, typefaceIndex, styleIndex);
        if (shadowcolor != 0) {
            setShadowLayer(r, dx, dy, shadowcolor);
        }
        if (maxlength >= 0) {
            InputFilter[] inputFilterArr = new InputFilter[SANS];
            inputFilterArr[MARQUEE_FADE_NORMAL] = new LengthFilter(maxlength);
            setFilters(inputFilterArr);
        } else {
            setFilters(NO_FILTERS);
        }
        setText(text, bufferType);
        if (hint != null) {
            setHint(hint);
        }
        boolean focusable = (this.mMovement == null && getKeyListener() == null) ? DEBUG_EXTRACT : true;
        boolean isClickable = !focusable ? isClickable() : true;
        boolean isLongClickable = !focusable ? isLongClickable() : true;
        a = context.obtainStyledAttributes(attrs, R.styleable.View, defStyleAttr, defStyleRes);
        focusable = a.getBoolean(19, focusable);
        isClickable = a.getBoolean(30, isClickable);
        isLongClickable = a.getBoolean(31, isLongClickable);
        a.recycle();
        setFocusable(focusable);
        setClickable(isClickable);
        setLongClickable(isLongClickable);
        if (this.mEditor != null) {
            this.mEditor.prepareCursorControllers();
        }
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(SANS);
        }
    }

    private int[] parseDimensionArray(TypedArray dimens) {
        if (dimens == null) {
            return null;
        }
        int[] result = new int[dimens.length()];
        for (int i = MARQUEE_FADE_NORMAL; i < result.length; i += SANS) {
            result[i] = dimens.getDimensionPixelSize(i, MARQUEE_FADE_NORMAL);
        }
        return result;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PROCESS_TEXT_REQUEST_CODE) {
            return;
        }
        if (resultCode == KEY_EVENT_HANDLED && data != null) {
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
                Toast.makeText(getContext(), String.valueOf(result), (int) SANS).show();
            }
        } else if (this.mText instanceof Spannable) {
            Selection.setSelection((Spannable) this.mText, getSelectionEnd());
        }
    }

    private void setTypefaceFromAttrs(String familyName, int typefaceIndex, int styleIndex) {
        Typeface typeface = null;
        if (familyName != null) {
            typeface = Typeface.create(familyName, styleIndex);
            if (typeface != null) {
                setTypeface(typeface);
                return;
            }
        }
        switch (typefaceIndex) {
            case SANS /*1*/:
                typeface = Typeface.SANS_SERIF;
                break;
            case SIGNED /*2*/:
                typeface = Typeface.SERIF;
                break;
            case MONOSPACE /*3*/:
                typeface = Typeface.MONOSPACE;
                break;
        }
        setTypeface(typeface, styleIndex);
    }

    private void setRelativeDrawablesIfNeeded(Drawable start, Drawable end) {
        boolean hasRelativeDrawables;
        if (start == null && end == null) {
            hasRelativeDrawables = DEBUG_EXTRACT;
        } else {
            hasRelativeDrawables = true;
        }
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
                start.setBounds(MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, start.getIntrinsicWidth(), start.getIntrinsicHeight());
                start.setState(state);
                start.copyBounds(compoundRect);
                start.setCallback(this);
                dr.mDrawableStart = start;
                dr.mDrawableSizeStart = compoundRect.width();
                dr.mDrawableHeightStart = compoundRect.height();
            } else {
                dr.mDrawableHeightStart = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeStart = MARQUEE_FADE_NORMAL;
            }
            if (end != null) {
                end.setBounds(MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, end.getIntrinsicWidth(), end.getIntrinsicHeight());
                end.setState(state);
                end.copyBounds(compoundRect);
                end.setCallback(this);
                dr.mDrawableEnd = end;
                dr.mDrawableSizeEnd = compoundRect.width();
                dr.mDrawableHeightEnd = compoundRect.height();
            } else {
                dr.mDrawableHeightEnd = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeEnd = MARQUEE_FADE_NORMAL;
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
                    imm.hideSoftInputFromWindow(getWindowToken(), MARQUEE_FADE_NORMAL);
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

    protected boolean getDefaultEditable() {
        return DEBUG_EXTRACT;
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
        setKeyListenerOnly(input);
        fixFocusableAndClickableSettings();
        if (input != null) {
            createEditorIfNeeded();
            try {
                this.mEditor.mInputType = this.mEditor.mKeyListener.getInputType();
            } catch (IncompatibleClassChangeError e) {
                this.mEditor.mInputType = SANS;
            }
            setInputTypeSingleLine(this.mSingleLine);
        } else if (this.mEditor != null) {
            this.mEditor.mInputType = MARQUEE_FADE_NORMAL;
        }
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (imm != null) {
            imm.restartInput(this);
        }
    }

    private void setKeyListenerOnly(KeyListener input) {
        if (this.mEditor != null || input != null) {
            createEditorIfNeeded();
            if (this.mEditor.mKeyListener != input) {
                this.mEditor.mKeyListener = input;
                if (!(input == null || (this.mText instanceof Editable))) {
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
            if (!(movement == null || (this.mText instanceof Spannable))) {
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
            setFocusable(DEBUG_EXTRACT);
            setClickable(DEBUG_EXTRACT);
            setLongClickable(DEBUG_EXTRACT);
            return;
        }
        setFocusable(true);
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
                TransformationMethod2 method2 = (TransformationMethod2) method;
                boolean z = (isTextSelectable() || (this.mText instanceof Editable)) ? DEBUG_EXTRACT : true;
                this.mAllowTransformationLengthChange = z;
                method2.setLengthChangesAllowed(this.mAllowTransformationLengthChange);
            } else {
                this.mAllowTransformationLengthChange = DEBUG_EXTRACT;
            }
            setText(this.mText);
            if (hasPasswordTransformationMethod()) {
                notifyViewAccessibilityStateChangedIfNeeded(MARQUEE_FADE_NORMAL);
            }
            this.mTextDir = getTextDirectionHeuristic();
        }
    }

    public int getCompoundPaddingTop() {
        Drawables dr = this.mDrawables;
        if (dr == null || dr.mShowing[SANS] == null) {
            return this.mPaddingTop;
        }
        return (this.mPaddingTop + dr.mDrawablePadding) + dr.mDrawableSizeTop;
    }

    public int getCompoundPaddingBottom() {
        Drawables dr = this.mDrawables;
        if (dr == null || dr.mShowing[MONOSPACE] == null) {
            return this.mPaddingBottom;
        }
        return (this.mPaddingBottom + dr.mDrawablePadding) + dr.mDrawableSizeBottom;
    }

    public int getCompoundPaddingLeft() {
        Drawables dr = this.mDrawables;
        if (dr == null || dr.mShowing[MARQUEE_FADE_NORMAL] == null) {
            return this.mPaddingLeft;
        }
        return (this.mPaddingLeft + dr.mDrawablePadding) + dr.mDrawableSizeLeft;
    }

    public int getCompoundPaddingRight() {
        Drawables dr = this.mDrawables;
        if (dr == null || dr.mShowing[SIGNED] == null) {
            return this.mPaddingRight;
        }
        return (this.mPaddingRight + dr.mDrawablePadding) + dr.mDrawableSizeRight;
    }

    public int getCompoundPaddingStart() {
        resolveDrawables();
        switch (getLayoutDirection()) {
            case SANS /*1*/:
                return getCompoundPaddingRight();
            default:
                return getCompoundPaddingLeft();
        }
    }

    public int getCompoundPaddingEnd() {
        resolveDrawables();
        switch (getLayoutDirection()) {
            case SANS /*1*/:
                return getCompoundPaddingLeft();
            default:
                return getCompoundPaddingRight();
        }
    }

    public int getExtendedPaddingTop() {
        if (this.mMaxMode != SANS) {
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
        int gravity = this.mGravity & LogPower.APP_PROCESS_EXIT;
        if (gravity == 48) {
            return top;
        }
        if (gravity == 80) {
            return (top + viewht) - layoutht;
        }
        return ((viewht - layoutht) / SIGNED) + top;
    }

    public int getExtendedPaddingBottom() {
        if (this.mMaxMode != SANS) {
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
        int gravity = this.mGravity & LogPower.APP_PROCESS_EXIT;
        if (gravity == 48) {
            return (bottom + viewht) - layoutht;
        }
        if (gravity == 80) {
            return bottom;
        }
        return ((viewht - layoutht) / SIGNED) + bottom;
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
            dr.mDrawableHeightStart = MARQUEE_FADE_NORMAL;
            dr.mDrawableSizeStart = MARQUEE_FADE_NORMAL;
            dr.mDrawableHeightEnd = MARQUEE_FADE_NORMAL;
            dr.mDrawableSizeEnd = MARQUEE_FADE_NORMAL;
        }
        boolean drawables = (left == null && top == null && right == null && bottom == null) ? DEBUG_EXTRACT : true;
        if (drawables) {
            if (dr == null) {
                dr = new Drawables(getContext());
                this.mDrawables = dr;
            }
            this.mDrawables.mOverride = DEBUG_EXTRACT;
            if (!(dr.mShowing[MARQUEE_FADE_NORMAL] == left || dr.mShowing[MARQUEE_FADE_NORMAL] == null)) {
                dr.mShowing[MARQUEE_FADE_NORMAL].setCallback(null);
            }
            dr.mShowing[MARQUEE_FADE_NORMAL] = left;
            if (!(dr.mShowing[SANS] == top || dr.mShowing[SANS] == null)) {
                dr.mShowing[SANS].setCallback(null);
            }
            dr.mShowing[SANS] = top;
            if (!(dr.mShowing[SIGNED] == right || dr.mShowing[SIGNED] == null)) {
                dr.mShowing[SIGNED].setCallback(null);
            }
            dr.mShowing[SIGNED] = right;
            if (!(dr.mShowing[MONOSPACE] == bottom || dr.mShowing[MONOSPACE] == null)) {
                dr.mShowing[MONOSPACE].setCallback(null);
            }
            dr.mShowing[MONOSPACE] = bottom;
            Rect compoundRect = dr.mCompoundRect;
            int[] state = getDrawableState();
            if (left != null) {
                left.setState(state);
                left.copyBounds(compoundRect);
                left.setCallback(this);
                dr.mDrawableSizeLeft = compoundRect.width();
                dr.mDrawableHeightLeft = compoundRect.height();
            } else {
                dr.mDrawableHeightLeft = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeLeft = MARQUEE_FADE_NORMAL;
            }
            if (right != null) {
                right.setState(state);
                right.copyBounds(compoundRect);
                right.setCallback(this);
                dr.mDrawableSizeRight = compoundRect.width();
                dr.mDrawableHeightRight = compoundRect.height();
            } else {
                dr.mDrawableHeightRight = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeRight = MARQUEE_FADE_NORMAL;
            }
            if (top != null) {
                top.setState(state);
                top.copyBounds(compoundRect);
                top.setCallback(this);
                dr.mDrawableSizeTop = compoundRect.height();
                dr.mDrawableWidthTop = compoundRect.width();
            } else {
                dr.mDrawableWidthTop = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeTop = MARQUEE_FADE_NORMAL;
            }
            if (bottom != null) {
                bottom.setState(state);
                bottom.copyBounds(compoundRect);
                bottom.setCallback(this);
                dr.mDrawableSizeBottom = compoundRect.height();
                dr.mDrawableWidthBottom = compoundRect.width();
            } else {
                dr.mDrawableWidthBottom = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeBottom = MARQUEE_FADE_NORMAL;
            }
        } else if (dr != null) {
            if (dr.hasMetadata()) {
                for (int i = dr.mShowing.length + KEY_EVENT_HANDLED; i >= 0; i += KEY_EVENT_HANDLED) {
                    if (dr.mShowing[i] != null) {
                        dr.mShowing[i].setCallback(null);
                    }
                    dr.mShowing[i] = null;
                }
                dr.mDrawableHeightLeft = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeLeft = MARQUEE_FADE_NORMAL;
                dr.mDrawableHeightRight = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeRight = MARQUEE_FADE_NORMAL;
                dr.mDrawableWidthTop = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeTop = MARQUEE_FADE_NORMAL;
                dr.mDrawableWidthBottom = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeBottom = MARQUEE_FADE_NORMAL;
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
            left.setBounds(MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, left.getIntrinsicWidth(), left.getIntrinsicHeight());
        }
        if (right != null) {
            right.setBounds(MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, right.getIntrinsicWidth(), right.getIntrinsicHeight());
        }
        if (top != null) {
            top.setBounds(MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, top.getIntrinsicWidth(), top.getIntrinsicHeight());
        }
        if (bottom != null) {
            bottom.setBounds(MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, bottom.getIntrinsicWidth(), bottom.getIntrinsicHeight());
        }
        setCompoundDrawables(left, top, right, bottom);
    }

    @RemotableViewMethod
    public void setCompoundDrawablesRelative(Drawable start, Drawable top, Drawable end, Drawable bottom) {
        Drawables dr = this.mDrawables;
        if (dr != null) {
            if (dr.mShowing[MARQUEE_FADE_NORMAL] != null) {
                dr.mShowing[MARQUEE_FADE_NORMAL].setCallback(null);
            }
            Drawable[] drawableArr = dr.mShowing;
            dr.mDrawableLeftInitial = null;
            drawableArr[MARQUEE_FADE_NORMAL] = null;
            if (dr.mShowing[SIGNED] != null) {
                dr.mShowing[SIGNED].setCallback(null);
            }
            drawableArr = dr.mShowing;
            dr.mDrawableRightInitial = null;
            drawableArr[SIGNED] = null;
            dr.mDrawableHeightLeft = MARQUEE_FADE_NORMAL;
            dr.mDrawableSizeLeft = MARQUEE_FADE_NORMAL;
            dr.mDrawableHeightRight = MARQUEE_FADE_NORMAL;
            dr.mDrawableSizeRight = MARQUEE_FADE_NORMAL;
        }
        boolean drawables = (start == null && top == null && end == null) ? bottom != null ? true : DEBUG_EXTRACT : true;
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
            if (!(dr.mShowing[SANS] == top || dr.mShowing[SANS] == null)) {
                dr.mShowing[SANS].setCallback(null);
            }
            dr.mShowing[SANS] = top;
            if (!(dr.mDrawableEnd == end || dr.mDrawableEnd == null)) {
                dr.mDrawableEnd.setCallback(null);
            }
            dr.mDrawableEnd = end;
            if (!(dr.mShowing[MONOSPACE] == bottom || dr.mShowing[MONOSPACE] == null)) {
                dr.mShowing[MONOSPACE].setCallback(null);
            }
            dr.mShowing[MONOSPACE] = bottom;
            Rect compoundRect = dr.mCompoundRect;
            int[] state = getDrawableState();
            if (start != null) {
                start.setState(state);
                start.copyBounds(compoundRect);
                start.setCallback(this);
                dr.mDrawableSizeStart = compoundRect.width();
                dr.mDrawableHeightStart = compoundRect.height();
            } else {
                dr.mDrawableHeightStart = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeStart = MARQUEE_FADE_NORMAL;
            }
            if (end != null) {
                end.setState(state);
                end.copyBounds(compoundRect);
                end.setCallback(this);
                dr.mDrawableSizeEnd = compoundRect.width();
                dr.mDrawableHeightEnd = compoundRect.height();
            } else {
                dr.mDrawableHeightEnd = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeEnd = MARQUEE_FADE_NORMAL;
            }
            if (top != null) {
                top.setState(state);
                top.copyBounds(compoundRect);
                top.setCallback(this);
                dr.mDrawableSizeTop = compoundRect.height();
                dr.mDrawableWidthTop = compoundRect.width();
            } else {
                dr.mDrawableWidthTop = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeTop = MARQUEE_FADE_NORMAL;
            }
            if (bottom != null) {
                bottom.setState(state);
                bottom.copyBounds(compoundRect);
                bottom.setCallback(this);
                dr.mDrawableSizeBottom = compoundRect.height();
                dr.mDrawableWidthBottom = compoundRect.width();
            } else {
                dr.mDrawableWidthBottom = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeBottom = MARQUEE_FADE_NORMAL;
            }
        } else if (dr != null) {
            if (dr.hasMetadata()) {
                if (dr.mDrawableStart != null) {
                    dr.mDrawableStart.setCallback(null);
                }
                dr.mDrawableStart = null;
                if (dr.mShowing[SANS] != null) {
                    dr.mShowing[SANS].setCallback(null);
                }
                dr.mShowing[SANS] = null;
                if (dr.mDrawableEnd != null) {
                    dr.mDrawableEnd.setCallback(null);
                }
                dr.mDrawableEnd = null;
                if (dr.mShowing[MONOSPACE] != null) {
                    dr.mShowing[MONOSPACE].setCallback(null);
                }
                dr.mShowing[MONOSPACE] = null;
                dr.mDrawableHeightStart = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeStart = MARQUEE_FADE_NORMAL;
                dr.mDrawableHeightEnd = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeEnd = MARQUEE_FADE_NORMAL;
                dr.mDrawableWidthTop = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeTop = MARQUEE_FADE_NORMAL;
                dr.mDrawableWidthBottom = MARQUEE_FADE_NORMAL;
                dr.mDrawableSizeBottom = MARQUEE_FADE_NORMAL;
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
            start.setBounds(MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, start.getIntrinsicWidth(), start.getIntrinsicHeight());
        }
        if (end != null) {
            end.setBounds(MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, end.getIntrinsicWidth(), end.getIntrinsicHeight());
        }
        if (top != null) {
            top.setBounds(MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, top.getIntrinsicWidth(), top.getIntrinsicHeight());
        }
        if (bottom != null) {
            bottom.setBounds(MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, bottom.getIntrinsicWidth(), bottom.getIntrinsicHeight());
        }
        setCompoundDrawablesRelative(start, top, end, bottom);
    }

    public Drawable[] getCompoundDrawables() {
        Drawables dr = this.mDrawables;
        if (dr != null) {
            return (Drawable[]) dr.mShowing.clone();
        }
        Drawable[] drawableArr = new Drawable[DECIMAL];
        drawableArr[MARQUEE_FADE_NORMAL] = null;
        drawableArr[SANS] = null;
        drawableArr[SIGNED] = null;
        drawableArr[MONOSPACE] = null;
        return drawableArr;
    }

    public Drawable[] getCompoundDrawablesRelative() {
        Drawables dr = this.mDrawables;
        if (dr != null) {
            Drawable[] drawableArr = new Drawable[DECIMAL];
            drawableArr[MARQUEE_FADE_NORMAL] = dr.mDrawableStart;
            drawableArr[SANS] = dr.mShowing[SANS];
            drawableArr[SIGNED] = dr.mDrawableEnd;
            drawableArr[MONOSPACE] = dr.mShowing[MONOSPACE];
            return drawableArr;
        }
        drawableArr = new Drawable[DECIMAL];
        drawableArr[MARQUEE_FADE_NORMAL] = null;
        drawableArr[SANS] = null;
        drawableArr[SIGNED] = null;
        drawableArr[MONOSPACE] = null;
        return drawableArr;
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
        return dr != null ? dr.mDrawablePadding : MARQUEE_FADE_NORMAL;
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
                Drawable[] drawableArr = this.mDrawables.mShowing;
                int length = drawableArr.length;
                for (int i = MARQUEE_FADE_NORMAL; i < length; i += SANS) {
                    Drawable dr = drawableArr[i];
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
        if (left == this.mPaddingLeft && right == this.mPaddingRight && top == this.mPaddingTop) {
            if (bottom != this.mPaddingBottom) {
            }
            super.setPadding(left, top, right, bottom);
            invalidate();
        }
        nullLayouts();
        super.setPadding(left, top, right, bottom);
        invalidate();
    }

    public void setPaddingRelative(int start, int top, int end, int bottom) {
        if (start == getPaddingStart() && end == getPaddingEnd() && top == this.mPaddingTop) {
            if (bottom != this.mPaddingBottom) {
            }
            super.setPaddingRelative(start, top, end, bottom);
            invalidate();
        }
        nullLayouts();
        super.setPaddingRelative(start, top, end, bottom);
        invalidate();
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
        int textColorHighlight = ta.getColor(DECIMAL, MARQUEE_FADE_NORMAL);
        if (textColorHighlight != 0) {
            setHighlightColor(textColorHighlight);
        }
        ColorStateList textColor = ta.getColorStateList(MONOSPACE);
        if (textColor != null) {
            setTextColor(textColor);
        }
        int textSize = ta.getDimensionPixelSize(MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL);
        if (textSize != 0) {
            setRawTextSize((float) textSize);
        }
        ColorStateList textColorHint = ta.getColorStateList(5);
        if (textColorHint != null) {
            setHintTextColor(textColorHint);
        }
        ColorStateList textColorLink = ta.getColorStateList(6);
        if (textColorLink != null) {
            setLinkTextColor(textColorLink);
        }
        setTypefaceFromAttrs(ta.getString(12), ta.getInt(SANS, KEY_EVENT_HANDLED), ta.getInt(SIGNED, KEY_EVENT_HANDLED));
        int shadowColor = ta.getInt(7, MARQUEE_FADE_NORMAL);
        if (shadowColor != 0) {
            float dx = ta.getFloat(8, 0.0f);
            float dy = ta.getFloat(9, 0.0f);
            setShadowLayer(ta.getFloat(10, 0.0f), dx, dy, shadowColor);
        }
        if (ta.getBoolean(11, DEBUG_EXTRACT)) {
            setTransformationMethod(new AllCapsTransformationMethod(getContext()));
        }
        if (ta.hasValue(13)) {
            setElegantTextHeight(ta.getBoolean(13, DEBUG_EXTRACT));
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
        return typeface != null ? typeface.getStyle() : MARQUEE_FADE_NORMAL;
    }

    @RemotableViewMethod
    public void setTextSize(float size) {
        setTextSize(SIGNED, size);
    }

    public void setTextSize(int unit, float size) {
        Resources r;
        Context c = getContext();
        if (c == null) {
            r = Resources.getSystem();
        } else {
            r = c.getResources();
        }
        setRawTextSize(TypedValue.applyDimension(unit, size, r.getDisplayMetrics()));
    }

    private void setRawTextSize(float size) {
        if (size != this.mTextPaint.getTextSize()) {
            this.mTextPaint.setTextSize(size);
            if (this.mLayout != null) {
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

    @RemotableViewMethod
    public void setTextColor(int color) {
        this.mTextColor = ColorStateList.valueOf(color);
        updateTextColors();
    }

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
            return (URLSpan[]) ((Spanned) this.mText).getSpans(MARQUEE_FADE_NORMAL, this.mText.length(), URLSpan.class);
        }
        return new URLSpan[MARQUEE_FADE_NORMAL];
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
        if ((gravity & LogPower.APP_PROCESS_EXIT) == 0) {
            gravity |= 48;
        }
        boolean newLayout = DEBUG_EXTRACT;
        if ((gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) != (this.mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)) {
            newLayout = true;
        }
        if (gravity != this.mGravity) {
            invalidate();
        }
        this.mGravity = gravity;
        if (this.mLayout != null && newLayout) {
            makeNewLayout(this.mLayout.getWidth(), this.mHintLayout == null ? MARQUEE_FADE_NORMAL : this.mHintLayout.getWidth(), UNKNOWN_BORING, UNKNOWN_BORING, ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight(), true);
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
    public void setMinLines(int minlines) {
        this.mMinimum = minlines;
        this.mMinMode = SANS;
        requestLayout();
        invalidate();
    }

    public int getMinLines() {
        return this.mMinMode == SANS ? this.mMinimum : KEY_EVENT_HANDLED;
    }

    @RemotableViewMethod
    public void setMinHeight(int minHeight) {
        this.mMinimum = minHeight;
        this.mMinMode = SIGNED;
        requestLayout();
        invalidate();
    }

    public int getMinHeight() {
        return this.mMinMode == SIGNED ? this.mMinimum : KEY_EVENT_HANDLED;
    }

    @RemotableViewMethod
    public void setMaxLines(int maxlines) {
        this.mMaximum = maxlines;
        this.mMaxMode = SANS;
        requestLayout();
        invalidate();
    }

    public int getMaxLines() {
        return this.mMaxMode == SANS ? this.mMaximum : KEY_EVENT_HANDLED;
    }

    @RemotableViewMethod
    public void setMaxHeight(int maxHeight) {
        this.mMaximum = maxHeight;
        this.mMaxMode = SIGNED;
        requestLayout();
        invalidate();
    }

    public int getMaxHeight() {
        return this.mMaxMode == SIGNED ? this.mMaximum : KEY_EVENT_HANDLED;
    }

    @RemotableViewMethod
    public void setLines(int lines) {
        this.mMinimum = lines;
        this.mMaximum = lines;
        this.mMinMode = SANS;
        this.mMaxMode = SANS;
        requestLayout();
        invalidate();
    }

    @RemotableViewMethod
    public void setHeight(int pixels) {
        this.mMinimum = pixels;
        this.mMaximum = pixels;
        this.mMinMode = SIGNED;
        this.mMaxMode = SIGNED;
        requestLayout();
        invalidate();
    }

    @RemotableViewMethod
    public void setMinEms(int minems) {
        this.mMinWidth = minems;
        this.mMinWidthMode = SANS;
        requestLayout();
        invalidate();
    }

    public int getMinEms() {
        return this.mMinWidthMode == SANS ? this.mMinWidth : KEY_EVENT_HANDLED;
    }

    @RemotableViewMethod
    public void setMinWidth(int minpixels) {
        this.mMinWidth = minpixels;
        this.mMinWidthMode = SIGNED;
        requestLayout();
        invalidate();
    }

    public int getMinWidth() {
        return this.mMinWidthMode == SIGNED ? this.mMinWidth : KEY_EVENT_HANDLED;
    }

    @RemotableViewMethod
    public void setMaxEms(int maxems) {
        this.mMaxWidth = maxems;
        this.mMaxWidthMode = SANS;
        requestLayout();
        invalidate();
    }

    public int getMaxEms() {
        return this.mMaxWidthMode == SANS ? this.mMaxWidth : KEY_EVENT_HANDLED;
    }

    @RemotableViewMethod
    public void setMaxWidth(int maxpixels) {
        this.mMaxWidth = maxpixels;
        this.mMaxWidthMode = SIGNED;
        requestLayout();
        invalidate();
    }

    public int getMaxWidth() {
        return this.mMaxWidthMode == SIGNED ? this.mMaxWidth : KEY_EVENT_HANDLED;
    }

    @RemotableViewMethod
    public void setEms(int ems) {
        this.mMinWidth = ems;
        this.mMaxWidth = ems;
        this.mMinWidthMode = SANS;
        this.mMaxWidthMode = SANS;
        requestLayout();
        invalidate();
    }

    @RemotableViewMethod
    public void setWidth(int pixels) {
        this.mMinWidth = pixels;
        this.mMaxWidth = pixels;
        this.mMinWidthMode = SIGNED;
        this.mMaxWidthMode = SIGNED;
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
        append(text, MARQUEE_FADE_NORMAL, text.length());
    }

    public void append(CharSequence text, int start, int end) {
        if (!(this.mText instanceof Editable)) {
            setText(this.mText, BufferType.EDITABLE);
        }
        ((Editable) this.mText).append(text, start, end);
        if (this.mAutoLinkMask != 0 && Linkify.addLinks((Spannable) this.mText, this.mAutoLinkMask) && this.mLinksClickable && !textCanBeSelected()) {
            setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void updateTextColors() {
        boolean inval = DEBUG_EXTRACT;
        int color = this.mTextColor.getColorForState(getDrawableState(), MARQUEE_FADE_NORMAL);
        if (color != this.mCurTextColor) {
            this.mCurTextColor = color;
            inval = true;
        }
        if (this.mLinkTextColor != null) {
            color = this.mLinkTextColor.getColorForState(getDrawableState(), MARQUEE_FADE_NORMAL);
            if (color != this.mTextPaint.linkColor) {
                this.mTextPaint.linkColor = color;
                inval = true;
            }
        }
        if (this.mHintTextColor != null) {
            color = this.mHintTextColor.getColorForState(getDrawableState(), MARQUEE_FADE_NORMAL);
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
            Drawable[] drawableArr = this.mDrawables.mShowing;
            int length = drawableArr.length;
            for (int i = MARQUEE_FADE_NORMAL; i < length; i += SANS) {
                Drawable dr = drawableArr[i];
                if (dr != null && dr.isStateful() && dr.setState(state)) {
                    invalidateDrawable(dr);
                }
            }
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (this.mDrawables != null) {
            Drawable[] drawableArr = this.mDrawables.mShowing;
            int length = drawableArr.length;
            for (int i = MARQUEE_FADE_NORMAL; i < length; i += SANS) {
                Drawable dr = drawableArr[i];
                if (dr != null) {
                    dr.setHotspot(x, y);
                }
            }
        }
    }

    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        boolean freezesText = getFreezesText();
        boolean hasSelection = DEBUG_EXTRACT;
        int start = KEY_EVENT_HANDLED;
        int end = KEY_EVENT_HANDLED;
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
        SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) spannable.getSpans(MARQUEE_FADE_NORMAL, spannable.length(), SuggestionSpan.class);
        for (int i = MARQUEE_FADE_NORMAL; i < suggestionSpans.length; i += SANS) {
            int flags = suggestionSpans[i].getFlags();
            if (!((flags & SANS) == 0 || (flags & SIGNED) == 0)) {
                spannable.removeSpan(suggestionSpans[i]);
            }
        }
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            if (ss.text != null) {
                setText(ss.text);
            }
            if (ss.selStart >= 0 && ss.selEnd >= 0 && (this.mText instanceof Spannable)) {
                int len = this.mText.length();
                if (ss.selStart > len || ss.selEnd > len) {
                    String restored = "";
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
                post(new AnonymousClass1(ss.error));
            }
            if (ss.editorState != null) {
                createEditorIfNeeded();
                this.mEditor.restoreInstanceState(ss.editorState);
            }
            return;
        }
        super.onRestoreInstanceState(state);
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
        if (curr_lang.contains("en") && curr_country.contains("US") && "\u1019\u102f\u102d".equals(text)) {
            text = "\u1013\u102d\u102f";
        }
        setText(text, this.mBufferType);
    }

    @RemotableViewMethod
    public final void setTextKeepState(CharSequence text) {
        setTextKeepState(text, this.mBufferType);
    }

    public void setText(CharSequence text, BufferType type) {
        setText(text, type, true, MARQUEE_FADE_NORMAL);
        if (this.mCharWrapper != null) {
            this.mCharWrapper.mChars = null;
        }
    }

    private void setText(CharSequence text, BufferType type, boolean notifyBefore, int oldlen) {
        int i;
        if (text == null) {
            text = "";
        }
        if (!isSuggestionsEnabled()) {
            text = removeSuggestionSpans(text);
        }
        if (!this.mUserSetTextScaleX) {
            this.mTextPaint.setTextScaleX(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
        }
        if ((text instanceof Spanned) && ((Spanned) text).getSpanStart(TruncateAt.MARQUEE) >= 0) {
            if (ViewConfiguration.get(this.mContext).isFadingMarqueeEnabled()) {
                setHorizontalFadingEdgeEnabled(true);
                this.mMarqueeFadeMode = MARQUEE_FADE_NORMAL;
            } else {
                setHorizontalFadingEdgeEnabled(DEBUG_EXTRACT);
                this.mMarqueeFadeMode = SANS;
            }
            setEllipsize(TruncateAt.MARQUEE);
        }
        int n = this.mFilters.length;
        for (i = MARQUEE_FADE_NORMAL; i < n; i += SANS) {
            CharSequence out = this.mFilters[i].filter(text, MARQUEE_FADE_NORMAL, text.length(), EMPTY_SPANNED, MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL);
            if (out != null) {
                text = out;
            }
        }
        if (notifyBefore) {
            if (this.mText != null) {
                oldlen = this.mText.length();
                sendBeforeTextChanged(this.mText, MARQUEE_FADE_NORMAL, oldlen, text.length());
            } else {
                sendBeforeTextChanged("", MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, text.length());
            }
        }
        boolean needEditableForNotification = DEBUG_EXTRACT;
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
                if (this.mLinksClickable && !textCanBeSelected()) {
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
        if ((text instanceof Spannable) && !this.mAllowTransformationLengthChange) {
            Spannable sp = (Spannable) text;
            ChangeWatcher[] watchers = (ChangeWatcher[]) sp.getSpans(MARQUEE_FADE_NORMAL, sp.length(), ChangeWatcher.class);
            int count = watchers.length;
            for (i = MARQUEE_FADE_NORMAL; i < count; i += SANS) {
                sp.removeSpan(watchers[i]);
            }
            if (this.mChangeWatcher == null) {
                TextView textView = this;
                this.mChangeWatcher = new ChangeWatcher();
            }
            sp.setSpan(this.mChangeWatcher, MARQUEE_FADE_NORMAL, textLength, 6553618);
            if (this.mEditor != null) {
                this.mEditor.addSpanWatchers(sp);
            }
            if (this.mTransformation != null) {
                sp.setSpan(this.mTransformation, MARQUEE_FADE_NORMAL, textLength, 18);
            }
            if (this.mMovement != null) {
                this.mMovement.initialize(this, (Spannable) text);
                if (this.mEditor != null) {
                    this.mEditor.mSelectionMoved = DEBUG_EXTRACT;
                }
            }
        }
        if (this.mLayout != null) {
            checkForRelayout();
        }
        sendOnTextChanged(text, MARQUEE_FADE_NORMAL, oldlen, textLength);
        onTextChanged(text, MARQUEE_FADE_NORMAL, oldlen, textLength);
        notifyViewAccessibilityStateChangedIfNeeded(SIGNED);
        if (needEditableForNotification) {
            sendAfterTextChanged((Editable) text);
        }
        if (this.mEditor != null) {
            this.mEditor.prepareCursorControllers();
        }
    }

    public final void setText(char[] text, int start, int len) {
        int oldlen = MARQUEE_FADE_NORMAL;
        if (start < 0 || len < 0 || start + len > text.length) {
            throw new IndexOutOfBoundsException(start + ", " + len);
        }
        if (this.mText != null) {
            oldlen = this.mText.length();
            sendBeforeTextChanged(this.mText, MARQUEE_FADE_NORMAL, oldlen, len);
        } else {
            sendBeforeTextChanged("", MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, len);
        }
        if (this.mCharWrapper == null) {
            this.mCharWrapper = new CharWrapper(text, start, len);
        } else {
            this.mCharWrapper.set(text, start, len);
        }
        setText(this.mCharWrapper, this.mBufferType, DEBUG_EXTRACT, oldlen);
    }

    public final void setTextKeepState(CharSequence text, BufferType type) {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        int len = text.length();
        setText(text, type);
        if ((start >= 0 || end >= 0) && (this.mText instanceof Spannable)) {
            Selection.setSelection((Spannable) this.mText, Math.max(MARQUEE_FADE_NORMAL, Math.min(start, len)), Math.max(MARQUEE_FADE_NORMAL, Math.min(end, len)));
        }
    }

    @RemotableViewMethod
    public final void setText(int resid) {
        setText(getContext().getResources().getText(resid));
    }

    public final void setText(int resid, BufferType type) {
        setText(getContext().getResources().getText(resid), type);
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
        return (131087 & type) == 131073 ? true : DEBUG_EXTRACT;
    }

    CharSequence removeSuggestionSpans(CharSequence text) {
        if (text instanceof Spanned) {
            Spannable spannable;
            if (text instanceof Spannable) {
                spannable = (Spannable) text;
            } else {
                spannable = new SpannableString(text);
                Object text2 = spannable;
            }
            SuggestionSpan[] spans = (SuggestionSpan[]) spannable.getSpans(MARQUEE_FADE_NORMAL, text.length(), SuggestionSpan.class);
            for (int i = MARQUEE_FADE_NORMAL; i < spans.length; i += SANS) {
                spannable.removeSpan(spans[i]);
            }
        }
        return text;
    }

    public void setInputType(int type) {
        boolean z = DEBUG_EXTRACT;
        boolean wasPassword = isPasswordInputType(getInputType());
        boolean wasVisiblePassword = isVisiblePasswordInputType(getInputType());
        setInputType(type, DEBUG_EXTRACT);
        boolean isPassword = isPasswordInputType(type);
        boolean isVisiblePassword = isVisiblePasswordInputType(type);
        boolean forceUpdate = DEBUG_EXTRACT;
        if (isPassword) {
            setTransformationMethod(PasswordTransformationMethod.getInstance());
            setTypefaceFromAttrs(null, MONOSPACE, MARQUEE_FADE_NORMAL);
        } else if (isVisiblePassword) {
            if (this.mTransformation == PasswordTransformationMethod.getInstance()) {
                forceUpdate = true;
            }
            setTypefaceFromAttrs(null, MONOSPACE, MARQUEE_FADE_NORMAL);
        } else if (wasPassword || wasVisiblePassword) {
            setTypefaceFromAttrs(null, KEY_EVENT_HANDLED, KEY_EVENT_HANDLED);
            if (this.mTransformation == PasswordTransformationMethod.getInstance()) {
                forceUpdate = true;
            }
        }
        boolean singleLine = isMultilineInputType(type) ? DEBUG_EXTRACT : true;
        if (this.mSingleLine != singleLine || forceUpdate) {
            if (!isPassword) {
                z = true;
            }
            applySingleLine(singleLine, z, true);
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

    private static boolean isPasswordInputType(int inputType) {
        int variation = inputType & 4095;
        if (variation == LogPower.START_CAMERA || variation == MetricsEvent.ABOUT_LEGAL_SETTINGS || variation == 18) {
            return true;
        }
        return DEBUG_EXTRACT;
    }

    private static boolean isVisiblePasswordInputType(int inputType) {
        return (inputType & 4095) == LogPower.THERMAL_LAUNCH ? true : DEBUG_EXTRACT;
    }

    public void setRawInputType(int type) {
        if (type != 0 || this.mEditor != null) {
            createEditorIfNeeded();
            this.mEditor.mInputType = type;
        }
    }

    private void setInputType(int type, boolean direct) {
        KeyListener input;
        boolean z = true;
        int cls = type & 15;
        if (cls == SANS) {
            Capitalize cap;
            boolean autotext = (AccessibilityNodeInfo.ACTION_PASTE & type) != 0 ? true : DEBUG_EXTRACT;
            if ((type & HwPerformance.PERF_EVENT_RAW_REQ) != 0) {
                cap = Capitalize.CHARACTERS;
            } else if ((type & AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) != 0) {
                cap = Capitalize.WORDS;
            } else if ((type & GL10.GL_LIGHT0) != 0) {
                cap = Capitalize.SENTENCES;
            } else {
                cap = Capitalize.NONE;
            }
            input = TextKeyListener.getInstance(autotext, cap);
        } else if (cls == SIGNED) {
            boolean z2;
            if ((type & HwPerformance.PERF_EVENT_RAW_REQ) != 0) {
                z2 = true;
            } else {
                z2 = DEBUG_EXTRACT;
            }
            if ((type & AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) == 0) {
                z = DEBUG_EXTRACT;
            }
            input = DigitsKeyListener.getInstance(z2, z);
        } else if (cls == DECIMAL) {
            switch (type & InputType.TYPE_MASK_VARIATION) {
                case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
                    input = DateKeyListener.getInstance();
                    break;
                case IndexSearchConstants.INDEX_BUILD_FLAG_INTERNAL_FILE /*32*/:
                    input = TimeKeyListener.getInstance();
                    break;
                default:
                    input = DateTimeKeyListener.getInstance();
                    break;
            }
        } else if (cls == MONOSPACE) {
            input = DialerKeyListener.getInstance();
        } else {
            input = TextKeyListener.getInstance();
        }
        setRawInputType(type);
        if (direct) {
            createEditorIfNeeded();
            this.mEditor.mKeyListener = input;
            return;
        }
        setKeyListenerOnly(input);
    }

    public int getInputType() {
        return this.mEditor == null ? MARQUEE_FADE_NORMAL : this.mEditor.mInputType;
    }

    public void setImeOptions(int imeOptions) {
        createEditorIfNeeded();
        this.mEditor.createInputContentTypeIfNeeded();
        this.mEditor.mInputContentType.imeOptions = imeOptions;
    }

    public int getImeOptions() {
        return (this.mEditor == null || this.mEditor.mInputContentType == null) ? MARQUEE_FADE_NORMAL : this.mEditor.mInputContentType.imeOptions;
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
        return (this.mEditor == null || this.mEditor.mInputContentType == null) ? MARQUEE_FADE_NORMAL : this.mEditor.mInputContentType.imeActionId;
    }

    public void setOnEditorActionListener(OnEditorActionListener l) {
        createEditorIfNeeded();
        this.mEditor.createInputContentTypeIfNeeded();
        this.mEditor.mInputContentType.onEditorActionListener = l;
    }

    public void onEditorAction(int actionCode) {
        InputContentType ict;
        if (this.mEditor == null) {
            ict = null;
        } else {
            ict = this.mEditor.mInputContentType;
        }
        if (ict != null) {
            if (ict.onEditorActionListener != null && ict.onEditorActionListener.onEditorAction(this, actionCode, null)) {
                return;
            }
            View v;
            InputMethodManager imm;
            if (actionCode == 5) {
                v = focusSearch(SIGNED);
                if (v != null) {
                    if (v.requestFocus(SIGNED)) {
                        imm = InputMethodManager.peekInstance();
                        if (imm != null && imm.isSecImmEnabled()) {
                            imm.showSoftInput(v, MARQUEE_FADE_NORMAL);
                        }
                    } else {
                        throw new IllegalStateException("focus search returned a view that wasn't able to take focus!");
                    }
                }
                return;
            } else if (actionCode == 7) {
                v = focusSearch(SANS);
                if (v != null) {
                    if (v.requestFocus(SANS)) {
                        imm = InputMethodManager.peekInstance();
                        if (imm != null && imm.isSecImmEnabled()) {
                            imm.showSoftInput(v, MARQUEE_FADE_NORMAL);
                        }
                    } else {
                        throw new IllegalStateException("focus search returned a view that wasn't able to take focus!");
                    }
                }
                return;
            } else if (actionCode == 6) {
                imm = InputMethodManager.peekInstance();
                if (imm != null && imm.isActive(this)) {
                    imm.hideSoftInputFromWindow(getWindowToken(), MARQUEE_FADE_NORMAL);
                }
                return;
            }
        }
        ViewRootImpl viewRootImpl = getViewRootImpl();
        if (viewRootImpl != null) {
            long eventTime = SystemClock.uptimeMillis();
            viewRootImpl.dispatchKeyFromIme(new KeyEvent(eventTime, eventTime, MARQUEE_FADE_NORMAL, 66, MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, KEY_EVENT_HANDLED, MARQUEE_FADE_NORMAL, 22));
            viewRootImpl.dispatchKeyFromIme(new KeyEvent(SystemClock.uptimeMillis(), eventTime, SANS, 66, MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, KEY_EVENT_HANDLED, MARQUEE_FADE_NORMAL, 22));
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
            dr.setBounds(MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
            setError(error, dr);
        }
    }

    public void setError(CharSequence error, Drawable icon) {
        createEditorIfNeeded();
        this.mEditor.setError(error, icon);
        notifyViewAccessibilityStateChangedIfNeeded(MARQUEE_FADE_NORMAL);
    }

    protected boolean setFrame(int l, int t, int r, int b) {
        boolean result = super.setFrame(l, t, r, b);
        if (this.mEditor != null) {
            this.mEditor.setFrame();
        }
        restartMarqueeIfNeeded();
        return result;
    }

    private void restartMarqueeIfNeeded() {
        if (this.mRestartMarquee && this.mEllipsize == TruncateAt.MARQUEE) {
            this.mRestartMarquee = DEBUG_EXTRACT;
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
            boolean undoFilter = this.mEditor.mUndoInputFilter != null ? true : DEBUG_EXTRACT;
            boolean keyFilter = this.mEditor.mKeyListener instanceof InputFilter;
            int num = MARQUEE_FADE_NORMAL;
            if (undoFilter) {
                num = SANS;
            }
            if (keyFilter) {
                num += SANS;
            }
            if (num > 0) {
                InputFilter[] nf = new InputFilter[(filters.length + num)];
                System.arraycopy(filters, MARQUEE_FADE_NORMAL, nf, MARQUEE_FADE_NORMAL, filters.length);
                num = MARQUEE_FADE_NORMAL;
                if (undoFilter) {
                    nf[filters.length] = this.mEditor.mUndoInputFilter;
                    num = SANS;
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
        int gravity = this.mGravity & LogPower.APP_PROCESS_EXIT;
        Layout l = this.mLayout;
        if (!(forceNormal || this.mText.length() != 0 || this.mHintLayout == null)) {
            l = this.mHintLayout;
        }
        if (gravity == 48) {
            return MARQUEE_FADE_NORMAL;
        }
        int boxht = getBoxHeight(l);
        int textht = l.getHeight();
        if (textht >= boxht) {
            return MARQUEE_FADE_NORMAL;
        }
        if (gravity == 80) {
            return boxht - textht;
        }
        return (boxht - textht) >> SANS;
    }

    private int getBottomVerticalOffset(boolean forceNormal) {
        int gravity = this.mGravity & LogPower.APP_PROCESS_EXIT;
        Layout l = this.mLayout;
        if (!(forceNormal || this.mText.length() != 0 || this.mHintLayout == null)) {
            l = this.mHintLayout;
        }
        if (gravity == 80) {
            return MARQUEE_FADE_NORMAL;
        }
        int boxht = getBoxHeight(l);
        int textht = l.getHeight();
        if (textht >= boxht) {
            return MARQUEE_FADE_NORMAL;
        }
        if (gravity == 48) {
            return boxht - textht;
        }
        return (boxht - textht) >> SANS;
    }

    public void editorUpdate(Canvas canvas, boolean isUseHintLayout) {
        if (this.mEditor != null) {
            Path path = getUpdatedHighlightPath();
            if (isUseHintLayout) {
                if (this.mHintLayout != null) {
                    this.mEditor.onDraw(canvas, this.mHintLayout, path, this.mHighlightPaint, MARQUEE_FADE_NORMAL);
                }
            } else if (this.mLayout != null) {
                this.mEditor.onDraw(canvas, this.mLayout, path, this.mHighlightPaint, MARQUEE_FADE_NORMAL);
            }
        }
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
                if (thick < LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
                    thick = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
                }
                thick /= 2.0f;
                this.mHighlightPath.computeBounds(TEMP_RECTF, DEBUG_EXTRACT);
                Rect cursorbounds = new Rect((int) FloatMath.floor(TEMP_RECTF.left - thick), (int) FloatMath.floor(TEMP_RECTF.top - thick), (int) FloatMath.floor(TEMP_RECTF.right + thick), (int) FloatMath.floor(TEMP_RECTF.bottom + thick));
                if (HwFrameworkFactory.getHwNsdImpl().checkIfNsdSupportCursor() && HwFrameworkFactory.getHwNsdImpl().isCursorBlinkCase(this, cursorbounds)) {
                    IHwNsdImpl hwNsdImpl = HwFrameworkFactory.getHwNsdImpl();
                    int i = this.mNSDRefreshTimes + SANS;
                    this.mNSDRefreshTimes = i;
                    hwNsdImpl.drawBitmapCursor(i, this, cursorbounds);
                } else {
                    invalidate((int) FloatMath.floor((((float) horizontalPadding) + TEMP_RECTF.left) - thick), (int) FloatMath.floor((((float) verticalPadding) + TEMP_RECTF.top) - thick), (int) FloatMath.ceil((((float) horizontalPadding) + TEMP_RECTF.right) + thick), (int) FloatMath.ceil((((float) verticalPadding) + TEMP_RECTF.bottom) + thick));
                }
            }
            return;
        }
        for (int i2 = MARQUEE_FADE_NORMAL; i2 < this.mEditor.mCursorCount; i2 += SANS) {
            Rect bounds = this.mEditor.mCursorDrawable[i2].getBounds();
            if (HwFrameworkFactory.getHwNsdImpl().checkIfNsdSupportCursor() && HwFrameworkFactory.getHwNsdImpl().isCursorBlinkCase(this, bounds)) {
                int[] cursorPos = new int[SIGNED];
                getLocationOnScreen(cursorPos);
                cursorPos[MARQUEE_FADE_NORMAL] = cursorPos[MARQUEE_FADE_NORMAL] + ((getTotalPaddingLeft() + bounds.left) - this.mScrollX);
                cursorPos[SANS] = cursorPos[SANS] + (getTotalPaddingTop() + bounds.top);
                int zorder = HwFrameworkFactory.getHwNsdImpl().getTextViewZOrderId(getAttachInfo());
                if (zorder != this.lastZorder) {
                    Log.i("NSD", "APS: NSD: TextView.invalidateCursorPath. zorder changes: zorder = " + zorder + ", lastZorder = " + this.lastZorder);
                    invalidate(bounds.left + horizontalPadding, bounds.top + verticalPadding, bounds.right + horizontalPadding, bounds.bottom + verticalPadding);
                    this.mNSDRefreshTimes = MARQUEE_FADE_NORMAL;
                    this.lastZorder = zorder;
                } else {
                    int i3;
                    if (sLastCursorX == cursorPos[MARQUEE_FADE_NORMAL] && sLastCursorY == cursorPos[SANS] && this.mLastNSDDrawRet && System.currentTimeMillis() - this.mLastNSDDrawCursorTime <= 600) {
                        if (sCurrentTextViewHash != hashCode()) {
                        }
                        if (this.mNSDRefreshTimes < SIGNED) {
                            invalidate(bounds.left + horizontalPadding, bounds.top + verticalPadding, bounds.right + horizontalPadding, bounds.bottom + verticalPadding);
                        }
                        hwNsdImpl = HwFrameworkFactory.getHwNsdImpl();
                        i3 = this.mNSDRefreshTimes + SANS;
                        this.mNSDRefreshTimes = i3;
                        this.mLastNSDDrawRet = hwNsdImpl.drawBitmapCursor(i3, this, bounds);
                        this.mLastNSDDrawCursorTime = System.currentTimeMillis();
                        sCurrentTextViewHash = hashCode();
                    }
                    this.mNSDRefreshTimes = MARQUEE_FADE_NORMAL;
                    sLastCursorX = cursorPos[MARQUEE_FADE_NORMAL];
                    sLastCursorY = cursorPos[SANS];
                    if (this.mNSDRefreshTimes < SIGNED) {
                        invalidate(bounds.left + horizontalPadding, bounds.top + verticalPadding, bounds.right + horizontalPadding, bounds.bottom + verticalPadding);
                    }
                    hwNsdImpl = HwFrameworkFactory.getHwNsdImpl();
                    i3 = this.mNSDRefreshTimes + SANS;
                    this.mNSDRefreshTimes = i3;
                    this.mLastNSDDrawRet = hwNsdImpl.drawBitmapCursor(i3, this, bounds);
                    this.mLastNSDDrawCursorTime = System.currentTimeMillis();
                    sCurrentTextViewHash = hashCode();
                }
            } else {
                invalidate(bounds.left + horizontalPadding, bounds.top + verticalPadding, bounds.right + horizontalPadding, bounds.bottom + verticalPadding);
            }
        }
    }

    void invalidateCursor() {
        int where = getSelectionEnd();
        invalidateCursor(where, where, where);
    }

    private void invalidateCursor(int a, int b, int c) {
        if (a < 0 && b < 0) {
            if (c < 0) {
                return;
            }
        }
        invalidateRegion(Math.min(Math.min(a, b), c), Math.max(Math.max(a, b), c), true);
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
            top -= this.mLayout.getLineDescent(lineStart + KEY_EVENT_HANDLED);
        }
        if (start == end) {
            lineEnd = lineStart;
        } else {
            lineEnd = this.mLayout.getLineForOffset(end);
        }
        int bottom = this.mLayout.getLineBottom(lineEnd);
        if (invalidateCursor && this.mEditor != null) {
            for (int i = MARQUEE_FADE_NORMAL; i < this.mEditor.mCursorCount; i += SANS) {
                Rect bounds = this.mEditor.mCursorDrawable[i].getBounds();
                top = Math.min(top, bounds.top);
                bottom = Math.max(bottom, bounds.bottom);
            }
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
        this.mPreDrawRegistered = DEBUG_EXTRACT;
        this.mPreDrawListenerDetached = DEBUG_EXTRACT;
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
            if (curs < 0 && (this.mGravity & LogPower.APP_PROCESS_EXIT) == 80) {
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
            this.mEditor.mCreatedWithASelection = DEBUG_EXTRACT;
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
            this.mPreDrawListenerDetached = DEBUG_EXTRACT;
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
        return (this.mShadowRadius == 0.0f && this.mDrawables == null) ? DEBUG_EXTRACT : true;
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
            Drawable[] drawableArr = this.mDrawables.mShowing;
            int length = drawableArr.length;
            for (int i = MARQUEE_FADE_NORMAL; i < length; i += SANS) {
                if (who == drawableArr[i]) {
                    return true;
                }
            }
        }
        return verified;
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mDrawables != null) {
            Drawable[] drawableArr = this.mDrawables.mShowing;
            int length = drawableArr.length;
            for (int i = MARQUEE_FADE_NORMAL; i < length; i += SANS) {
                Drawable dr = drawableArr[i];
                if (dr != null) {
                    dr.jumpToCurrentState();
                }
            }
        }
    }

    public void invalidateDrawable(Drawable drawable) {
        boolean handled = DEBUG_EXTRACT;
        if (verifyDrawable(drawable)) {
            Rect dirty = drawable.getBounds();
            int scrollX = this.mScrollX;
            int scrollY = this.mScrollY;
            Drawables drawables = this.mDrawables;
            if (drawables != null) {
                int compoundPaddingTop;
                if (drawable == drawables.mShowing[MARQUEE_FADE_NORMAL]) {
                    compoundPaddingTop = getCompoundPaddingTop();
                    scrollX += this.mPaddingLeft;
                    scrollY += (((((this.mBottom - this.mTop) - getCompoundPaddingBottom()) - compoundPaddingTop) - drawables.mDrawableHeightLeft) / SIGNED) + compoundPaddingTop;
                    handled = true;
                } else if (drawable == drawables.mShowing[SIGNED]) {
                    compoundPaddingTop = getCompoundPaddingTop();
                    scrollX += ((this.mRight - this.mLeft) - this.mPaddingRight) - drawables.mDrawableSizeRight;
                    scrollY += (((((this.mBottom - this.mTop) - getCompoundPaddingBottom()) - compoundPaddingTop) - drawables.mDrawableHeightRight) / SIGNED) + compoundPaddingTop;
                    handled = true;
                } else if (drawable == drawables.mShowing[SANS]) {
                    compoundPaddingLeft = getCompoundPaddingLeft();
                    scrollX += (((((this.mRight - this.mLeft) - getCompoundPaddingRight()) - compoundPaddingLeft) - drawables.mDrawableWidthTop) / SIGNED) + compoundPaddingLeft;
                    scrollY += this.mPaddingTop;
                    handled = true;
                } else if (drawable == drawables.mShowing[MONOSPACE]) {
                    compoundPaddingLeft = getCompoundPaddingLeft();
                    scrollX += (((((this.mRight - this.mLeft) - getCompoundPaddingRight()) - compoundPaddingLeft) - drawables.mDrawableWidthBottom) / SIGNED) + compoundPaddingLeft;
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
        return this.mEditor == null ? DEBUG_EXTRACT : this.mEditor.mTextIsSelectable;
    }

    public void setTextIsSelectable(boolean selectable) {
        MovementMethod movementMethod = null;
        if (selectable || this.mEditor != null) {
            createEditorIfNeeded();
            if (this.mEditor.mTextIsSelectable != selectable) {
                this.mEditor.mTextIsSelectable = selectable;
                setFocusableInTouchMode(selectable);
                setFocusable(selectable);
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
            drawableState = super.onCreateDrawableState(extraSpace + SANS);
            View.mergeDrawableStates(drawableState, MULTILINE_STATE_SET);
        }
        if (isTextSelectable()) {
            int length = drawableState.length;
            for (int i = MARQUEE_FADE_NORMAL; i < length; i += SANS) {
                if (drawableState[i] == 16842919) {
                    int[] nonPressedState = new int[(length + KEY_EVENT_HANDLED)];
                    System.arraycopy(drawableState, MARQUEE_FADE_NORMAL, nonPressedState, MARQUEE_FADE_NORMAL, i);
                    System.arraycopy(drawableState, i + SANS, nonPressedState, i, (length - i) + KEY_EVENT_HANDLED);
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
                this.mHighlightPathBogus = DEBUG_EXTRACT;
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
                this.mHighlightPathBogus = DEBUG_EXTRACT;
            }
            highlightPaint.setColor(this.mCurTextColor);
            highlightPaint.setStyle(Style.STROKE);
            return this.mHighlightPath;
        }
    }

    public int getHorizontalOffsetForDrawables() {
        return MARQUEE_FADE_NORMAL;
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
        boolean isLayoutRtl = this.mTextViewDirection == SANS ? DEBUG_EXTRACT : true;
        int offset = getHorizontalOffsetForDrawables();
        int leftOffset = isRtlLocale() ? MARQUEE_FADE_NORMAL : offset;
        int rightOffset = isRtlLocale() ? offset : MARQUEE_FADE_NORMAL;
        Drawables dr = this.mDrawables;
        if (dr != null) {
            int vspace = ((bottom - top) - compoundPaddingBottom) - compoundPaddingTop;
            int hspace = ((right - left) - compoundPaddingRight) - compoundPaddingLeft;
            if (dr.mShowing[MARQUEE_FADE_NORMAL] != null) {
                canvas.save();
                canvas.translate((float) ((this.mPaddingLeft + scrollX) + leftOffset), (float) ((scrollY + compoundPaddingTop) + ((vspace - dr.mDrawableHeightLeft) / SIGNED)));
                dr.mShowing[MARQUEE_FADE_NORMAL].draw(canvas);
                canvas.restore();
            }
            if (dr.mShowing[SIGNED] != null) {
                canvas.save();
                canvas.translate((float) (((((scrollX + right) - left) - this.mPaddingRight) - dr.mDrawableSizeRight) - rightOffset), (float) ((scrollY + compoundPaddingTop) + ((vspace - dr.mDrawableHeightRight) / SIGNED)));
                dr.mShowing[SIGNED].draw(canvas);
                canvas.restore();
            }
            if (dr.mShowing[SANS] != null) {
                canvas.save();
                canvas.translate((float) (((scrollX + compoundPaddingLeft) + this.mHwCompoundPaddingLeft) + ((hspace - dr.mDrawableWidthTop) / SIGNED)), (float) (this.mPaddingTop + scrollY));
                dr.mShowing[SANS].draw(canvas);
                canvas.restore();
            }
            if (dr.mShowing[MONOSPACE] != null) {
                canvas.save();
                canvas.translate((float) ((scrollX + compoundPaddingLeft) + ((hspace - dr.mDrawableWidthBottom) / SIGNED)), (float) ((((scrollY + bottom) - top) - this.mPaddingBottom) - dr.mDrawableSizeBottom));
                dr.mShowing[MONOSPACE].draw(canvas);
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
        float clipTop = (float) (scrollY == 0 ? MARQUEE_FADE_NORMAL : extendedPaddingTop + scrollY);
        float clipRight = (float) (((right - left) - getCompoundPaddingRight()) + scrollX);
        int i = (bottom - top) + scrollY;
        if (scrollY == this.mLayout.getHeight() - (((this.mBottom - this.mTop) - compoundPaddingBottom) - compoundPaddingTop)) {
            extendedPaddingBottom = MARQUEE_FADE_NORMAL;
        }
        float clipBottom = (float) (i - extendedPaddingBottom);
        if (this.mShadowRadius != 0.0f) {
            clipLeft += Math.min(0.0f, this.mShadowDx - this.mShadowRadius);
            clipRight += Math.max(0.0f, this.mShadowDx + this.mShadowRadius);
            clipTop += Math.min(0.0f, this.mShadowDy - this.mShadowRadius);
            clipBottom += Math.max(0.0f, this.mShadowDy + this.mShadowRadius);
        }
        canvas.clipRect(clipLeft, clipTop, clipRight, clipBottom);
        int voffsetText = MARQUEE_FADE_NORMAL;
        int voffsetCursor = MARQUEE_FADE_NORMAL;
        if ((this.mGravity & LogPower.APP_PROCESS_EXIT) != 48) {
            voffsetText = getVerticalOffset(DEBUG_EXTRACT);
            voffsetCursor = getVerticalOffset(true);
        }
        canvas.translate((float) compoundPaddingLeft, (float) (extendedPaddingTop + voffsetText));
        int absoluteGravity = Gravity.getAbsoluteGravity(this.mGravity, this.mTextViewDirection == SANS ? MARQUEE_FADE_NORMAL : SANS);
        if (isMarqueeFadeEnabled()) {
            if (!this.mSingleLine && getLineCount() == SANS && canMarquee() && (absoluteGravity & 7) != MONOSPACE) {
                dx = this.mLayout.getLineRight(MARQUEE_FADE_NORMAL) - ((float) ((this.mRight - this.mLeft) - (getCompoundPaddingLeft() + getCompoundPaddingRight())));
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
            r.right = r.left + DECIMAL;
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
                    this.mHighlightPathBogus = DEBUG_EXTRACT;
                }
                synchronized (TEMP_RECTF) {
                    this.mHighlightPath.computeBounds(TEMP_RECTF, true);
                    r.left = ((int) TEMP_RECTF.left) + KEY_EVENT_HANDLED;
                    r.right = ((int) TEMP_RECTF.right) + SANS;
                }
            }
        }
        int paddingLeft = getCompoundPaddingLeft();
        int paddingTop = getExtendedPaddingTop();
        if ((this.mGravity & LogPower.APP_PROCESS_EXIT) != 48) {
            paddingTop += getVerticalOffset(DEBUG_EXTRACT);
        }
        r.offset(paddingLeft, paddingTop);
        r.bottom += getExtendedPaddingBottom();
    }

    public int getLineCount() {
        return this.mLayout != null ? this.mLayout.getLineCount() : MARQUEE_FADE_NORMAL;
    }

    public int getLineBounds(int line, Rect bounds) {
        if (this.mLayout == null) {
            if (bounds != null) {
                bounds.set(MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL);
            }
            return MARQUEE_FADE_NORMAL;
        }
        int baseline = this.mLayout.getLineBounds(line, bounds);
        int voffset = getExtendedPaddingTop();
        if ((this.mGravity & LogPower.APP_PROCESS_EXIT) != 48) {
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
        return getBaselineOffset() + this.mLayout.getLineBaseline(MARQUEE_FADE_NORMAL);
    }

    int getBaselineOffset() {
        int voffset = MARQUEE_FADE_NORMAL;
        if ((this.mGravity & LogPower.APP_PROCESS_EXIT) != 48) {
            voffset = getVerticalOffset(true);
        }
        if (View.isLayoutModeOptical(this.mParent)) {
            voffset -= getOpticalInsets().top;
        }
        return getExtendedPaddingTop() + voffset;
    }

    protected int getFadeTop(boolean offsetRequired) {
        if (this.mLayout == null) {
            return MARQUEE_FADE_NORMAL;
        }
        int voffset = MARQUEE_FADE_NORMAL;
        if ((this.mGravity & LogPower.APP_PROCESS_EXIT) != 48) {
            voffset = getVerticalOffset(true);
        }
        if (offsetRequired) {
            voffset += getTopPaddingOffset();
        }
        return getExtendedPaddingTop() + voffset;
    }

    protected int getFadeHeight(boolean offsetRequired) {
        return this.mLayout != null ? this.mLayout.getHeight() : MARQUEE_FADE_NORMAL;
    }

    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        if ((this.mText instanceof Spannable) && this.mLinksClickable) {
            int offset = getOffsetForPosition(event.getX(pointerIndex), event.getY(pointerIndex));
            if (((ClickableSpan[]) ((Spannable) this.mText).getSpans(offset, offset, ClickableSpan.class)).length > 0) {
                return PointerIcon.getSystemIcon(this.mContext, RILConstants.RIL_UNSOL_RESPONSE_VOICE_NETWORK_STATE_CHANGED);
            }
        }
        if (isTextSelectable() || isTextEditable()) {
            return PointerIcon.getSystemIcon(this.mContext, RILConstants.RIL_UNSOL_NITZ_TIME_RECEIVED);
        }
        return super.onResolvePointerIcon(event, pointerIndex);
    }

    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == DECIMAL && handleBackInTextActionModeIfNeeded(event)) {
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public boolean handleBackInTextActionModeIfNeeded(KeyEvent event) {
        if (this.mEditor == null || this.mEditor.mTextActionMode == null) {
            return DEBUG_EXTRACT;
        }
        DispatcherState state;
        if (event.getAction() == 0 && event.getRepeatCount() == 0) {
            state = getKeyDispatcherState();
            if (state != null) {
                state.startTracking(event, this);
            }
            return true;
        }
        if (event.getAction() == SANS) {
            state = getKeyDispatcherState();
            if (state != null) {
                state.handleUpEvent(event);
            }
            if (event.isTracking() && !event.isCanceled()) {
                stopTextActionMode();
                return true;
            }
        }
        return DEBUG_EXTRACT;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (doKeyDown(keyCode, event, null) == 0) {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        KeyEvent down = KeyEvent.changeAction(event, MARQUEE_FADE_NORMAL);
        int which = doKeyDown(keyCode, down, event);
        if (which == 0) {
            return super.onKeyMultiple(keyCode, repeatCount, event);
        }
        if (which == KEY_EVENT_HANDLED) {
            return true;
        }
        repeatCount += KEY_EVENT_HANDLED;
        KeyEvent up = KeyEvent.changeAction(event, SANS);
        if (which != SANS) {
            if (which == SIGNED) {
                this.mMovement.onKeyUp(this, (Spannable) this.mText, keyCode, up);
                while (true) {
                    repeatCount += KEY_EVENT_HANDLED;
                    if (repeatCount <= 0) {
                        break;
                    }
                    this.mMovement.onKeyDown(this, (Spannable) this.mText, keyCode, down);
                    this.mMovement.onKeyUp(this, (Spannable) this.mText, keyCode, up);
                }
            }
        } else {
            this.mEditor.mKeyListener.onKeyUp(this, (Editable) this.mText, keyCode, up);
            while (true) {
                repeatCount += KEY_EVENT_HANDLED;
                if (repeatCount <= 0) {
                    break;
                }
                this.mEditor.mKeyListener.onKeyDown(this, (Editable) this.mText, keyCode, down);
                this.mEditor.mKeyListener.onKeyUp(this, (Editable) this.mText, keyCode, up);
            }
            hideErrorIfUnchanged();
        }
        return true;
    }

    private boolean shouldAdvanceFocusOnEnter() {
        if (getKeyListener() == null) {
            return DEBUG_EXTRACT;
        }
        if (this.mSingleLine) {
            return true;
        }
        if (this.mEditor != null && (this.mEditor.mInputType & 15) == SANS) {
            int variation = this.mEditor.mInputType & InputType.TYPE_MASK_VARIATION;
            if (variation == 32 || variation == 48) {
                return true;
            }
        }
        return DEBUG_EXTRACT;
    }

    private boolean shouldAdvanceFocusOnTab() {
        if (!(getKeyListener() == null || this.mSingleLine || this.mEditor == null || (this.mEditor.mInputType & 15) != SANS)) {
            int variation = this.mEditor.mInputType & InputType.TYPE_MASK_VARIATION;
            if (variation == Protocol.BASE_DATA_CONNECTION || variation == Protocol.BASE_WIFI) {
                return DEBUG_EXTRACT;
            }
        }
        return true;
    }

    private int doKeyDown(int keyCode, KeyEvent event, KeyEvent otherEvent) {
        if (!isEnabled()) {
            return MARQUEE_FADE_NORMAL;
        }
        boolean doDown;
        boolean handled;
        if (event.getRepeatCount() == 0 && !KeyEvent.isModifierKey(keyCode)) {
            this.mPreventDefaultMovement = DEBUG_EXTRACT;
        }
        switch (keyCode) {
            case DECIMAL /*4*/:
                if (!(this.mEditor == null || this.mEditor.mTextActionMode == null)) {
                    stopTextActionMode();
                    return KEY_EVENT_HANDLED;
                }
            case HwPerformance.PERF_TAG_DEF_B_CPU_MIN /*23*/:
                if (event.hasNoModifiers() && shouldAdvanceFocusOnEnter()) {
                    return MARQUEE_FADE_NORMAL;
                }
            case StatisticalConstant.TYPE_WIFI_END /*61*/:
                if ((event.hasNoModifiers() || event.hasModifiers(SANS)) && shouldAdvanceFocusOnTab()) {
                    return MARQUEE_FADE_NORMAL;
                }
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                if (event.hasNoModifiers()) {
                    if (this.mEditor != null && this.mEditor.mInputContentType != null && this.mEditor.mInputContentType.onEditorActionListener != null && this.mEditor.mInputContentType.onEditorActionListener.onEditorAction(this, MARQUEE_FADE_NORMAL, event)) {
                        this.mEditor.mInputContentType.enterDown = true;
                        return KEY_EVENT_HANDLED;
                    } else if ((event.getFlags() & 16) != 0 || shouldAdvanceFocusOnEnter()) {
                        return hasOnClickListeners() ? MARQUEE_FADE_NORMAL : KEY_EVENT_HANDLED;
                    }
                }
                break;
            case MetricsEvent.ACTION_OVERVIEW_SELECT /*277*/:
                if (event.hasNoModifiers() && canCut() && onTextContextMenuItem(ID_CUT)) {
                    return KEY_EVENT_HANDLED;
                }
            case MetricsEvent.ACTION_VIEW_EMERGENCY_INFO /*278*/:
                if (event.hasNoModifiers() && canCopy() && onTextContextMenuItem(ID_COPY)) {
                    return KEY_EVENT_HANDLED;
                }
            case MetricsEvent.ACTION_EDIT_EMERGENCY_INFO /*279*/:
                if (event.hasNoModifiers() && canPaste() && onTextContextMenuItem(ID_PASTE)) {
                    return KEY_EVENT_HANDLED;
                }
        }
        if (!(this.mEditor == null || this.mEditor.mKeyListener == null)) {
            doDown = true;
            if (otherEvent != null) {
                try {
                    beginBatchEdit();
                    handled = this.mEditor.mKeyListener.onKeyOther(this, (Editable) this.mText, otherEvent);
                    hideErrorIfUnchanged();
                    doDown = DEBUG_EXTRACT;
                    if (handled) {
                        return KEY_EVENT_HANDLED;
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
                    return SANS;
                }
            }
        }
        if (!(this.mMovement == null || this.mLayout == null)) {
            doDown = true;
            if (otherEvent != null) {
                try {
                    handled = this.mMovement.onKeyOther(this, (Spannable) this.mText, otherEvent);
                    doDown = DEBUG_EXTRACT;
                    if (handled) {
                        return KEY_EVENT_HANDLED;
                    }
                } catch (AbstractMethodError e2) {
                }
            }
            if (doDown) {
                this.mValidSetCursorEvent = true;
                if (this.mMovement.onKeyDown(this, (Spannable) this.mText, keyCode, event)) {
                    if (event.getRepeatCount() == 0 && !KeyEvent.isModifierKey(keyCode)) {
                        this.mPreventDefaultMovement = true;
                    }
                    return SIGNED;
                }
                this.mValidSetCursorEvent = DEBUG_EXTRACT;
            }
        }
        int i = (!this.mPreventDefaultMovement || KeyEvent.isModifierKey(keyCode)) ? MARQUEE_FADE_NORMAL : KEY_EVENT_HANDLED;
        return i;
    }

    public void resetErrorChangedFlag() {
        if (this.mEditor != null) {
            this.mEditor.mErrorWasChanged = DEBUG_EXTRACT;
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
            this.mPreventDefaultMovement = DEBUG_EXTRACT;
        }
        InputMethodManager imm;
        switch (keyCode) {
            case HwPerformance.PERF_TAG_DEF_B_CPU_MIN /*23*/:
                if (event.hasNoModifiers() && !hasOnClickListeners() && this.mMovement != null && (this.mText instanceof Editable) && this.mLayout != null && onCheckIsTextEditor()) {
                    imm = InputMethodManager.peekInstance();
                    viewClicked(imm);
                    if (imm != null && getShowSoftInputOnFocus()) {
                        imm.showSoftInput(this, MARQUEE_FADE_NORMAL);
                    }
                }
                return super.onKeyUp(keyCode, event);
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                if (event.hasNoModifiers()) {
                    if (!(this.mEditor == null || this.mEditor.mInputContentType == null || this.mEditor.mInputContentType.onEditorActionListener == null || !this.mEditor.mInputContentType.enterDown)) {
                        this.mEditor.mInputContentType.enterDown = DEBUG_EXTRACT;
                        if (this.mEditor.mInputContentType.onEditorActionListener.onEditorAction(this, MARQUEE_FADE_NORMAL, event)) {
                            return true;
                        }
                    }
                    if (((event.getFlags() & 16) != 0 || shouldAdvanceFocusOnEnter()) && !hasOnClickListeners()) {
                        View v = focusSearch(LogPower.END_CHG_ROTATION);
                        if (v != null) {
                            if (v.requestFocus(LogPower.END_CHG_ROTATION)) {
                                super.onKeyUp(keyCode, event);
                                return true;
                            }
                            if (!ImsConferenceState.USER.equals(SystemProperties.get("ro.build.type", "eng"))) {
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
                                imm.hideSoftInputFromWindow(getWindowToken(), MARQUEE_FADE_NORMAL);
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
        return (this.mEditor == null || this.mEditor.mInputType == 0) ? DEBUG_EXTRACT : true;
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
                outAttrs.imeOptions = MARQUEE_FADE_NORMAL;
                outAttrs.hintLocales = null;
            }
            if (focusSearch(LogPower.END_CHG_ROTATION) != null) {
                outAttrs.imeOptions |= EditorInfo.IME_FLAG_NAVIGATE_NEXT;
            }
            if (focusSearch(33) != null) {
                outAttrs.imeOptions |= EditorInfo.IME_FLAG_NAVIGATE_PREVIOUS;
            }
            if ((outAttrs.imeOptions & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) == 0) {
                if ((outAttrs.imeOptions & EditorInfo.IME_FLAG_NAVIGATE_NEXT) != 0) {
                    outAttrs.imeOptions |= 5;
                } else {
                    outAttrs.imeOptions |= 6;
                }
                if (!shouldAdvanceFocusOnEnter()) {
                    outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_ENTER_ACTION;
                }
            }
            if (isMultilineInputType(outAttrs.inputType)) {
                outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_ENTER_ACTION;
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
            i += KEY_EVENT_HANDLED;
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
                start = MARQUEE_FADE_NORMAL;
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
                    TextUtils.copySpansFrom((Spanned) text.text, MARQUEE_FADE_NORMAL, end - start, Object.class, content, start);
                }
            }
        }
        Spannable sp = (Spannable) getText();
        N = sp.length();
        start = text.selectionStart;
        if (start < 0) {
            start = MARQUEE_FADE_NORMAL;
        } else if (start > N) {
            start = N;
        }
        end = text.selectionEnd;
        if (end < 0) {
            end = MARQUEE_FADE_NORMAL;
        } else if (end > N) {
            end = N;
        }
        Selection.setSelection(sp, start, end);
        if ((text.flags & SIGNED) != 0) {
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
        return DEBUG_EXTRACT;
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
        if (width < SANS) {
            width = MARQUEE_FADE_NORMAL;
        }
        int physicalWidth = width;
        if (this.mHorizontallyScrolling) {
            width = VERY_WIDE;
        }
        makeNewLayout(width, physicalWidth, UNKNOWN_BORING, UNKNOWN_BORING, physicalWidth, DEBUG_EXTRACT);
    }

    private boolean shouldUseDefaultLayoutAlignment() {
        boolean z = true;
        if (!isRtlLocale()) {
            return true;
        }
        int id = getId();
        if (id == KEY_EVENT_HANDLED) {
            return DEBUG_EXTRACT;
        }
        String checkPackName = "com.android.chrome";
        if (!checkPackName.equals(this.mContext.getPackageName())) {
            return DEBUG_EXTRACT;
        }
        if (!(id == this.mContext.getResources().getIdentifier("url_bar", "id", checkPackName) || id == this.mContext.getResources().getIdentifier("trailing_text", "id", checkPackName))) {
            z = DEBUG_EXTRACT;
        }
        return z;
    }

    private Alignment calAlign(int gravity) {
        Alignment align = Alignment.ALIGN_NORMAL;
        String currentLang = Locale.getDefault().getLanguage();
        boolean isIW = DEBUG_EXTRACT;
        if (currentLang.contains("iw")) {
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
            case SANS /*1*/:
                switch (this.mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
                    case SANS /*1*/:
                        return Alignment.ALIGN_CENTER;
                    case MONOSPACE /*3*/:
                        return Alignment.ALIGN_LEFT;
                    case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                        return Alignment.ALIGN_RIGHT;
                    case Gravity.START /*8388611*/:
                        return calAlign(Gravity.START);
                    case Gravity.END /*8388613*/:
                        return calAlign(Gravity.END);
                    default:
                        return Alignment.ALIGN_NORMAL;
                }
            case SIGNED /*2*/:
                return Alignment.ALIGN_NORMAL;
            case MONOSPACE /*3*/:
                return Alignment.ALIGN_OPPOSITE;
            case DECIMAL /*4*/:
                return Alignment.ALIGN_CENTER;
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                return isRtlLocale() ? Alignment.ALIGN_RIGHT : Alignment.ALIGN_LEFT;
            case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                return isRtlLocale() ? Alignment.ALIGN_LEFT : Alignment.ALIGN_RIGHT;
            default:
                return Alignment.ALIGN_NORMAL;
        }
    }

    protected void makeNewLayout(int wantWidth, int hintWidth, Metrics boring, Metrics hintBoring, int ellipsisWidth, boolean bringIntoView) {
        boolean testDirChange;
        stopMarquee();
        this.mOldMaximum = this.mMaximum;
        this.mOldMaxMode = this.mMaxMode;
        this.mHighlightPathBogus = true;
        if (wantWidth < 0) {
            wantWidth = MARQUEE_FADE_NORMAL;
        }
        if (hintWidth < 0) {
            hintWidth = MARQUEE_FADE_NORMAL;
        }
        Alignment alignment = getLayoutAlignment();
        if (!this.mSingleLine || this.mLayout == null) {
            testDirChange = DEBUG_EXTRACT;
        } else {
            boolean z = alignment != Alignment.ALIGN_NORMAL ? alignment == Alignment.ALIGN_OPPOSITE ? true : DEBUG_EXTRACT : true;
            testDirChange = z;
        }
        int oldDir = MARQUEE_FADE_NORMAL;
        if (testDirChange) {
            oldDir = this.mLayout.getParagraphDirection(MARQUEE_FADE_NORMAL);
        }
        boolean shouldEllipsize = (this.mEllipsize == null || getKeyListener() != null) ? DEBUG_EXTRACT : true;
        boolean switchEllipsize = this.mEllipsize == TruncateAt.MARQUEE ? this.mMarqueeFadeMode != 0 ? true : DEBUG_EXTRACT : DEBUG_EXTRACT;
        TruncateAt effectiveEllipsize = this.mEllipsize;
        if (this.mEllipsize == TruncateAt.MARQUEE && this.mMarqueeFadeMode == SANS) {
            effectiveEllipsize = TruncateAt.END_SMALL;
        }
        if (this.mTextDir == null) {
            this.mTextDir = getTextDirectionHeuristic();
        }
        this.mLayout = makeSingleLayout(wantWidth, boring, ellipsisWidth, alignment, shouldEllipsize, effectiveEllipsize, effectiveEllipsize == this.mEllipsize ? true : DEBUG_EXTRACT);
        if (switchEllipsize) {
            this.mSavedMarqueeModeLayout = makeSingleLayout(wantWidth, boring, ellipsisWidth, alignment, shouldEllipsize, effectiveEllipsize == TruncateAt.MARQUEE ? TruncateAt.END : TruncateAt.MARQUEE, effectiveEllipsize != this.mEllipsize ? true : DEBUG_EXTRACT);
        }
        shouldEllipsize = this.mEllipsize != null ? true : DEBUG_EXTRACT;
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
                Builder builder = Builder.obtain(this.mHint, MARQUEE_FADE_NORMAL, this.mHint.length(), this.mTextPaint, hintWidth).setAlignment(alignment).setTextDirection(this.mTextDir).setLineSpacing(this.mSpacingAdd, this.mSpacingMult).setIncludePad(this.mIncludePad).setBreakStrategy(this.mBreakStrategy).setHyphenationFrequency(this.mHyphenationFrequency);
                if (shouldEllipsize) {
                    builder.setEllipsize(this.mEllipsize).setEllipsizedWidth(ellipsisWidth).setMaxLines(this.mMaxMode == SANS ? this.mMaximum : HwBootFail.STAGE_BOOT_SUCCESS);
                }
                this.mHintLayout = builder.build();
            }
        }
        if (bringIntoView || (testDirChange && oldDir != this.mLayout.getParagraphDirection(MARQUEE_FADE_NORMAL))) {
            registerForPreDraw();
        }
        if (this.mEllipsize == TruncateAt.MARQUEE) {
            if (!compressText((float) ellipsisWidth)) {
                int height = this.mLayoutParams.height;
                if (height == -2 || height == KEY_EVENT_HANDLED) {
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
        Layout layout = null;
        if (this.mText instanceof Spannable) {
            layout = new DynamicLayout(this.mText, this.mTransformed, this.mTextPaint, wantWidth, alignment, this.mTextDir, this.mSpacingMult, this.mSpacingAdd, this.mIncludePad, this.mBreakStrategy, this.mHyphenationFrequency, getKeyListener() == null ? effectiveEllipsize : null, ellipsisWidth);
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
                        layout = BoringLayout.make(this.mTransformed, this.mTextPaint, wantWidth, alignment, this.mSpacingMult, this.mSpacingAdd, boring, this.mIncludePad);
                    } else {
                        layout = this.mSavedLayout.replaceOrMake(this.mTransformed, this.mTextPaint, wantWidth, alignment, this.mSpacingMult, this.mSpacingAdd, boring, this.mIncludePad);
                    }
                    if (useSaved) {
                        this.mSavedLayout = (BoringLayout) layout;
                    }
                } else if (shouldEllipsize && boring.width <= wantWidth) {
                    layout = (!useSaved || this.mSavedLayout == null) ? BoringLayout.make(this.mTransformed, this.mTextPaint, wantWidth, alignment, this.mSpacingMult, this.mSpacingAdd, boring, this.mIncludePad, effectiveEllipsize, ellipsisWidth) : this.mSavedLayout.replaceOrMake(this.mTransformed, this.mTextPaint, wantWidth, alignment, this.mSpacingMult, this.mSpacingAdd, boring, this.mIncludePad, effectiveEllipsize, ellipsisWidth);
                }
            }
        }
        if (layout != null) {
            return layout;
        }
        Builder builder = Builder.obtain(this.mTransformed, MARQUEE_FADE_NORMAL, this.mTransformed.length(), this.mTextPaint, wantWidth).setAlignment(alignment).setTextDirection(this.mTextDir).setLineSpacing(this.mSpacingAdd, this.mSpacingMult).setIncludePad(this.mIncludePad).setBreakStrategy(this.mBreakStrategy).setHyphenationFrequency(this.mHyphenationFrequency);
        if (shouldEllipsize) {
            builder.setEllipsize(effectiveEllipsize).setEllipsizedWidth(ellipsisWidth).setMaxLines(this.mMaxMode == SANS ? this.mMaximum : HwBootFail.STAGE_BOOT_SUCCESS);
        }
        return builder.build();
    }

    private boolean compressText(float width) {
        if (!isHardwareAccelerated() && width > 0.0f && this.mLayout != null && getLineCount() == SANS && !this.mUserSetTextScaleX && this.mTextPaint.getTextScaleX() == LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
            float overflow = ((this.mLayout.getLineWidth(MARQUEE_FADE_NORMAL) + LayoutParams.BRIGHTNESS_OVERRIDE_FULL) - width) / width;
            if (overflow > 0.0f && overflow <= 0.07f) {
                this.mTextPaint.setTextScaleX((LayoutParams.BRIGHTNESS_OVERRIDE_FULL - overflow) - 0.005f);
                post(new Runnable() {
                    public void run() {
                        TextView.this.requestLayout();
                    }
                });
                return true;
            }
        }
        return DEBUG_EXTRACT;
    }

    private static int desired(Layout layout) {
        int i;
        int n = layout.getLineCount();
        CharSequence text = layout.getText();
        float max = 0.0f;
        for (i = MARQUEE_FADE_NORMAL; i < n + KEY_EVENT_HANDLED; i += SANS) {
            if (text.charAt(layout.getLineEnd(i) + KEY_EVENT_HANDLED) != '\n') {
                return KEY_EVENT_HANDLED;
            }
        }
        for (i = MARQUEE_FADE_NORMAL; i < n; i += SANS) {
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
        int des = KEY_EVENT_HANDLED;
        boolean fromexisting = DEBUG_EXTRACT;
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
                    des = (int) Math.ceil((double) Layout.getDesiredWidth(this.mTransformed, this.mTextPaint));
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
                int hintDes = KEY_EVENT_HANDLED;
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
                        hintDes = (int) Math.ceil((double) Layout.getDesiredWidth(this.mHint, this.mTextPaint));
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
            if (this.mMaxWidthMode == SANS) {
                width = Math.min(width, this.mMaxWidth * getLineHeight());
            } else {
                width = Math.min(width, this.mMaxWidth);
            }
            if (this.mMinWidthMode == SANS) {
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
            want = VERY_WIDE;
        }
        int hintWant = want;
        hintWidth = this.mHintLayout == null ? hintWant : this.mHintLayout.getWidth();
        if (this.mLayout == null) {
            makeNewLayout(want, hintWant, boring, hintBoring, (width - getCompoundPaddingLeft()) - getCompoundPaddingRight(), DEBUG_EXTRACT);
        } else {
            boolean layoutChanged = (this.mLayout.getWidth() == want && hintWidth == hintWant) ? this.mLayout.getEllipsizedWidth() != (width - getCompoundPaddingLeft()) - getCompoundPaddingRight() ? true : DEBUG_EXTRACT : true;
            boolean widthChanged = (this.mHint == null && this.mEllipsize == null && want > this.mLayout.getWidth()) ? ((this.mLayout instanceof BoringLayout) || (fromexisting && des >= 0 && des <= want)) ? true : DEBUG_EXTRACT : DEBUG_EXTRACT;
            boolean maximumChanged = (this.mMaxMode == this.mOldMaxMode && this.mMaximum == this.mOldMaximum) ? DEBUG_EXTRACT : true;
            if (layoutChanged || maximumChanged) {
                if (maximumChanged || !widthChanged) {
                    makeNewLayout(want, hintWant, boring, hintBoring, (width - getCompoundPaddingLeft()) - getCompoundPaddingRight(), DEBUG_EXTRACT);
                } else {
                    this.mLayout.increaseWidthTo(want);
                }
            }
        }
        if (heightMode == EditorInfo.IME_FLAG_NO_ENTER_ACTION) {
            height = heightSize;
            this.mDesiredHeightAtMeasure = KEY_EVENT_HANDLED;
        } else {
            int desired = getDesiredHeight();
            height = desired;
            this.mDesiredHeightAtMeasure = desired;
            if (heightMode == RtlSpacingHelper.UNDEFINED) {
                height = Math.min(desired, heightSize);
            }
        }
        int unpaddedHeight = (height - getCompoundPaddingTop()) - getCompoundPaddingBottom();
        if (this.mMaxMode == SANS && this.mLayout.getLineCount() > this.mMaximum) {
            unpaddedHeight = Math.min(unpaddedHeight, this.mLayout.getLineTop(this.mMaximum));
        }
        if (this.mMovement != null || this.mLayout.getWidth() > unpaddedWidth || this.mLayout.getHeight() > unpaddedHeight) {
            registerForPreDraw();
        } else {
            scrollTo(MARQUEE_FADE_NORMAL, MARQUEE_FADE_NORMAL);
        }
        setMeasuredDimension(width, height);
    }

    private int getDesiredHeight() {
        boolean z = true;
        int desiredHeight = getDesiredHeight(this.mLayout, true);
        Layout layout = this.mHintLayout;
        if (this.mEllipsize == null) {
            z = DEBUG_EXTRACT;
        }
        return Math.max(desiredHeight, getDesiredHeight(layout, z));
    }

    private int getDesiredHeight(Layout layout, boolean cap) {
        if (layout == null) {
            return MARQUEE_FADE_NORMAL;
        }
        int linecount = layout.getLineCount();
        int pad = getCompoundPaddingTop() + getCompoundPaddingBottom();
        int desired = layout.getLineTop(linecount);
        Drawables dr = this.mDrawables;
        if (dr != null) {
            desired = Math.max(Math.max(desired, dr.mDrawableHeightLeft), dr.mDrawableHeightRight);
        }
        desired += pad;
        if (this.mMaxMode != SANS) {
            desired = Math.min(desired, this.mMaximum);
        } else if (cap && linecount > this.mMaximum) {
            desired = layout.getLineTop(this.mMaximum);
            if (dr != null) {
                desired = Math.max(Math.max(desired, dr.mDrawableHeightLeft), dr.mDrawableHeightRight);
            }
            desired += pad;
            linecount = this.mMaximum;
        }
        if (this.mMinMode != SANS) {
            desired = Math.max(desired, this.mMinimum);
        } else if (linecount < this.mMinimum) {
            desired += getLineHeight() * (this.mMinimum - linecount);
        }
        return Math.max(desired, getSuggestedMinimumHeight());
    }

    private void checkForResize() {
        boolean sizeChanged = DEBUG_EXTRACT;
        if (this.mLayout != null) {
            if (this.mLayoutParams.width == -2) {
                sizeChanged = true;
                invalidate();
            }
            if (this.mLayoutParams.height == -2) {
                if (getDesiredHeight() != getHeight()) {
                    sizeChanged = true;
                }
            } else if (this.mLayoutParams.height == KEY_EVENT_HANDLED && this.mDesiredHeightAtMeasure >= 0 && getDesiredHeight() != this.mDesiredHeightAtMeasure) {
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
            makeNewLayout(this.mLayout.getWidth(), this.mHintLayout == null ? MARQUEE_FADE_NORMAL : this.mHintLayout.getWidth(), UNKNOWN_BORING, UNKNOWN_BORING, ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight(), DEBUG_EXTRACT);
            if (this.mEllipsize != TruncateAt.MARQUEE) {
                if (this.mLayoutParams.height != -2 && this.mLayoutParams.height != KEY_EVENT_HANDLED) {
                    invalidate();
                    return;
                } else if (this.mLayout.getHeight() == oldht && (this.mHintLayout == null || this.mHintLayout.getHeight() == oldht)) {
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
            this.mDeferScroll = KEY_EVENT_HANDLED;
            bringPointIntoView(Math.min(curs, this.mText.length()));
        }
    }

    private boolean isShowingHint() {
        return (!TextUtils.isEmpty(this.mText) || TextUtils.isEmpty(this.mHint)) ? DEBUG_EXTRACT : true;
    }

    private boolean bringTextIntoView() {
        int scrollx;
        int scrolly;
        Layout layout = isShowingHint() ? this.mHintLayout : this.mLayout;
        int line = MARQUEE_FADE_NORMAL;
        if ((this.mGravity & LogPower.APP_PROCESS_EXIT) == 80) {
            line = layout.getLineCount() + KEY_EVENT_HANDLED;
        }
        Alignment a = layout.getParagraphAlignment(line);
        int dir = layout.getParagraphDirection(line);
        this.mTextViewDirection = dir;
        int hspace = ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        int vspace = ((this.mBottom - this.mTop) - getExtendedPaddingTop()) - getExtendedPaddingBottom();
        int ht = layout.getHeight();
        if (a == Alignment.ALIGN_NORMAL) {
            if (dir == SANS) {
                a = Alignment.ALIGN_LEFT;
            } else {
                a = Alignment.ALIGN_RIGHT;
            }
        } else if (a == Alignment.ALIGN_OPPOSITE) {
            if (dir == SANS) {
                a = Alignment.ALIGN_RIGHT;
            } else {
                a = Alignment.ALIGN_LEFT;
            }
        }
        if (a == Alignment.ALIGN_CENTER) {
            int left = (int) Math.floor((double) layout.getLineLeft(line));
            int right = (int) Math.ceil((double) layout.getLineRight(line));
            if (right - left < hspace) {
                scrollx = ((right + left) / SIGNED) - (hspace / SIGNED);
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
            scrolly = MARQUEE_FADE_NORMAL;
        } else if ((this.mGravity & LogPower.APP_PROCESS_EXIT) == 80) {
            scrolly = ht - vspace;
        } else {
            scrolly = MARQUEE_FADE_NORMAL;
        }
        if (scrollx == this.mScrollX && scrolly == this.mScrollY) {
            return DEBUG_EXTRACT;
        }
        scrollTo(scrollx, scrolly);
        return true;
    }

    public boolean bringPointIntoView(int offset) {
        if (isLayoutRequested()) {
            this.mDeferScroll = offset;
            return DEBUG_EXTRACT;
        }
        boolean changed = DEBUG_EXTRACT;
        Layout layout = isShowingHint() ? this.mHintLayout : this.mLayout;
        if (layout == null) {
            return DEBUG_EXTRACT;
        }
        int grav;
        int line = layout.getLineForOffset(offset);
        switch (-getandroid-text-Layout$AlignmentSwitchesValues()[layout.getParagraphAlignment(line).ordinal()]) {
            case SIGNED /*2*/:
                grav = SANS;
                break;
            case MONOSPACE /*3*/:
                grav = layout.getParagraphDirection(line);
                break;
            case DECIMAL /*4*/:
                grav = -layout.getParagraphDirection(line);
                break;
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                grav = KEY_EVENT_HANDLED;
                break;
            default:
                grav = MARQUEE_FADE_NORMAL;
                break;
        }
        int x = (int) layout.getPrimaryHorizontal(offset, grav > 0 ? true : DEBUG_EXTRACT);
        int top = layout.getLineTop(line);
        int bottom = layout.getLineTop(line + SANS);
        int left = (int) Math.floor((double) layout.getLineLeft(line));
        int right = (int) Math.ceil((double) layout.getLineRight(line));
        int ht = layout.getHeight();
        int hspace = ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        int vspace = ((this.mBottom - this.mTop) - getExtendedPaddingTop()) - getExtendedPaddingBottom();
        if (!this.mHorizontallyScrolling && right - left > hspace && right > x) {
            right = Math.max(x, left + hspace);
        }
        int hslack = (bottom - top) / SIGNED;
        int vslack = hslack;
        if (hslack > vspace / DECIMAL) {
            vslack = vspace / DECIMAL;
        }
        if (hslack > hspace / DECIMAL) {
            hslack = hspace / DECIMAL;
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
            vs = MARQUEE_FADE_NORMAL;
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
            hs = left - ((hspace - (right - left)) / SIGNED);
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
        int i = this.mScrollX;
        if (!(hs == r0 && vs == this.mScrollY)) {
            if (this.mScroller == null) {
                scrollTo(hs, vs);
            } else {
                int dx = hs - this.mScrollX;
                int dy = vs - this.mScrollY;
                if (AnimationUtils.currentAnimationTimeMillis() - this.mLastScroll > 250) {
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
        if (isFocused()) {
            if (this.mTempRect == null) {
                this.mTempRect = new Rect();
            }
            this.mTempRect.set(x - 2, top, x + SIGNED, bottom);
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
            return DEBUG_EXTRACT;
        }
        int start = getSelectionStart();
        if (start != getSelectionEnd()) {
            return DEBUG_EXTRACT;
        }
        int line = this.mLayout.getLineForOffset(start);
        int top = this.mLayout.getLineTop(line);
        int bottom = this.mLayout.getLineTop(line + SANS);
        int vspace = ((this.mBottom - this.mTop) - getExtendedPaddingTop()) - getExtendedPaddingBottom();
        int vslack = (bottom - top) / SIGNED;
        if (vslack > vspace / DECIMAL) {
            vslack = vspace / DECIMAL;
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
            return DEBUG_EXTRACT;
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
        if (line == this.mLayout.getLineCount() + KEY_EVENT_HANDLED) {
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
        if ((this.mGravity & LogPower.APP_PROCESS_EXIT) != 48) {
            return offset + getVerticalOffset(DEBUG_EXTRACT);
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
            return DEBUG_EXTRACT;
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
        if (this.mEditor != null && (this.mEditor.mInputType & 15) == SANS) {
            Editor editor;
            if (singleLine) {
                editor = this.mEditor;
                editor.mInputType &= -131073;
                return;
            }
            editor = this.mEditor;
            editor.mInputType |= Protocol.BASE_WIFI;
        }
    }

    private void applySingleLine(boolean singleLine, boolean applyTransformation, boolean changeMaxLines) {
        this.mSingleLine = singleLine;
        if (singleLine) {
            setLines(SANS);
            setHorizontallyScrolling(true);
            if (applyTransformation) {
                setTransformationMethod(SingleLineTransformationMethod.getInstance());
                return;
            }
            return;
        }
        if (changeMaxLines) {
            setMaxLines(HwBootFail.STAGE_BOOT_SUCCESS);
        }
        setHorizontallyScrolling(DEBUG_EXTRACT);
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
        return this.mEditor == null ? true : this.mEditor.mCursorVisible;
    }

    private boolean canMarquee() {
        int width = ((this.mRight - this.mLeft) - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        if (width <= 0) {
            return DEBUG_EXTRACT;
        }
        if (this.mLayout.getLineWidth(MARQUEE_FADE_NORMAL) > ((float) width)) {
            return true;
        }
        if (this.mMarqueeFadeMode == 0 || this.mSavedMarqueeModeLayout == null) {
            return DEBUG_EXTRACT;
        }
        if (this.mSavedMarqueeModeLayout.getLineWidth(MARQUEE_FADE_NORMAL) > ((float) width)) {
            return true;
        }
        return DEBUG_EXTRACT;
    }

    private void startMarquee() {
        if (getKeyListener() == null && !compressText((float) ((getWidth() - getCompoundPaddingLeft()) - getCompoundPaddingRight()))) {
            if ((this.mMarquee == null || this.mMarquee.isStopped()) && ((isFocused() || isSelected()) && getLineCount() == SANS && canMarquee())) {
                if (this.mMarqueeFadeMode == SANS) {
                    this.mMarqueeFadeMode = SIGNED;
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
        if (!(this.mMarquee == null || this.mMarquee.isStopped())) {
            this.mMarquee.stop();
        }
        if (this.mMarqueeFadeMode == SIGNED) {
            this.mMarqueeFadeMode = SANS;
            Layout tmp = this.mSavedMarqueeModeLayout;
            this.mSavedMarqueeModeLayout = this.mLayout;
            this.mLayout = tmp;
            setHorizontalFadingEdgeEnabled(DEBUG_EXTRACT);
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
        sendAccessibilityEvent(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
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
            for (int i = MARQUEE_FADE_NORMAL; i < count; i += SANS) {
                ((TextWatcher) list.get(i)).beforeTextChanged(text, start, before, after);
            }
        }
        removeIntersectingNonAdjacentSpans(start, start + before, SpellCheckSpan.class);
        removeIntersectingNonAdjacentSpans(start, start + before, SuggestionSpan.class);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private <T> void removeIntersectingNonAdjacentSpans(int start, int end, Class<T> type) {
        if (this.mText instanceof Editable) {
            Editable text = this.mText;
            T[] spans = text.getSpans(start, end, type);
            int length = spans.length;
            int i = MARQUEE_FADE_NORMAL;
            while (i < length) {
                int spanStart = text.getSpanStart(spans[i]);
                if (!(text.getSpanEnd(spans[i]) == start || spanStart == end)) {
                    text.removeSpan(spans[i]);
                    i += SANS;
                }
            }
        }
    }

    void removeAdjacentSuggestionSpans(int pos) {
        if (this.mText instanceof Editable) {
            Editable text = this.mText;
            SuggestionSpan[] spans = (SuggestionSpan[]) text.getSpans(pos, pos, SuggestionSpan.class);
            int length = spans.length;
            for (int i = MARQUEE_FADE_NORMAL; i < length; i += SANS) {
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
            for (int i = MARQUEE_FADE_NORMAL; i < count; i += SANS) {
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
            for (int i = MARQUEE_FADE_NORMAL; i < count; i += SANS) {
                ((TextWatcher) list.get(i)).afterTextChanged(text);
            }
        }
        hideErrorIfUnchanged();
    }

    void updateAfterEdit() {
        invalidate();
        int curs = getSelectionStart();
        if (curs >= 0 || (this.mGravity & LogPower.APP_PROCESS_EXIT) == 80) {
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
        InputMethodState ims = null;
        sLastCutCopyOrTextChangedTime = 0;
        if (this.mEditor != null) {
            ims = this.mEditor.mInputMethodState;
        }
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
        boolean selChanged = DEBUG_EXTRACT;
        int newSelStart = KEY_EVENT_HANDLED;
        int newSelEnd = KEY_EVENT_HANDLED;
        InputMethodState inputMethodState = this.mEditor == null ? null : this.mEditor.mInputMethodState;
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
            if (!(this.mEditor == null || isFocused())) {
                this.mEditor.mSelectionMoved = true;
            }
            if ((buf.getSpanFlags(what) & GL10.GL_NEVER) == 0) {
                if (newSelStart < 0) {
                    newSelStart = Selection.getSelectionStart(buf);
                }
                if (newSelEnd < 0) {
                    newSelEnd = Selection.getSelectionEnd(buf);
                }
                if (this.mEditor != null) {
                    this.mEditor.refreshTextActionMode();
                    if (!hasSelection() && this.mEditor.mTextActionMode == null && hasTransientState()) {
                        setHasTransientState(DEBUG_EXTRACT);
                    }
                }
                onSelectionChanged(newSelStart, newSelEnd);
            }
        }
        if ((what instanceof UpdateAppearance) || (what instanceof ParagraphStyle) || (what instanceof CharacterStyle)) {
            if (inputMethodState == null || inputMethodState.mBatchEditNesting == 0) {
                invalidate();
                this.mHighlightPathBogus = true;
                checkForResize();
            } else {
                inputMethodState.mContentChanged = true;
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
            if (inputMethodState != null && MetaKeyKeyListener.isSelectingMetaTracker(buf, what)) {
                inputMethodState.mSelectionModeChanged = true;
            }
            if (Selection.getSelectionStart(buf) >= 0) {
                if (inputMethodState == null || inputMethodState.mBatchEditNesting == 0) {
                    invalidateCursor();
                } else {
                    inputMethodState.mCursorChanged = true;
                }
            }
        }
        if (!(!(what instanceof ParcelableSpan) || inputMethodState == null || inputMethodState.mExtractedTextRequest == null)) {
            if (inputMethodState.mBatchEditNesting != 0) {
                if (oldStart >= 0) {
                    if (inputMethodState.mChangedStart > oldStart) {
                        inputMethodState.mChangedStart = oldStart;
                    }
                    if (inputMethodState.mChangedStart > oldEnd) {
                        inputMethodState.mChangedStart = oldEnd;
                    }
                }
                if (newStart >= 0) {
                    if (inputMethodState.mChangedStart > newStart) {
                        inputMethodState.mChangedStart = newStart;
                    }
                    if (inputMethodState.mChangedStart > newEnd) {
                        inputMethodState.mChangedStart = newEnd;
                    }
                }
            } else {
                inputMethodState.mContentChanged = true;
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
        if (this.mEditor != null && this.mEditor.mDiscardNextActionUp && action == SANS) {
            this.mEditor.mDiscardNextActionUp = DEBUG_EXTRACT;
            if (this.mEditor.mIsInsertionActionModeStartPending) {
                this.mEditor.startInsertionActionMode();
                this.mEditor.mIsInsertionActionModeStartPending = DEBUG_EXTRACT;
            }
            return superResult;
        }
        boolean z;
        if (action != SANS || (this.mEditor != null && this.mEditor.mIgnoreActionUpEvent)) {
            z = DEBUG_EXTRACT;
        } else {
            z = isFocused();
        }
        if ((this.mMovement != null || onCheckIsTextEditor()) && isEnabled() && (this.mText instanceof Spannable) && this.mLayout != null) {
            int handled = DEBUG_EXTRACT;
            this.mValidSetCursorEvent = true;
            if (this.mMovement != null) {
                handled = this.mMovement.onTouchEvent(this, (Spannable) this.mText, event);
            }
            boolean textIsSelectable = isTextSelectable();
            if (z && this.mLinksClickable && this.mAutoLinkMask != 0 && textIsSelectable) {
                ClickableSpan[] links = (ClickableSpan[]) ((Spannable) this.mText).getSpans(getSelectionStart(), getSelectionEnd(), ClickableSpan.class);
                if (links.length > 0) {
                    links[MARQUEE_FADE_NORMAL].onClick(this);
                    handled = SANS;
                }
            }
            if (z && (isTextEditable() || textIsSelectable)) {
                imm = InputMethodManager.peekInstance();
                viewClicked(imm);
                if (!textIsSelectable && this.mEditor.mShowSoftInputOnFocus) {
                    int showSoftInput;
                    if (imm != null) {
                        showSoftInput = imm.showSoftInput(this, MARQUEE_FADE_NORMAL);
                    } else {
                        showSoftInput = MARQUEE_FADE_NORMAL;
                    }
                    handled |= showSoftInput;
                }
                this.mEditor.onTouchUpEvent(event);
                handled = SANS;
            }
            if (handled != 0) {
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
        return super.onGenericMotionEvent(event);
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
        return ((this.mText instanceof Editable) && onCheckIsTextEditor()) ? isEnabled() : DEBUG_EXTRACT;
    }

    public boolean didTouchFocusSelect() {
        return this.mEditor != null ? this.mEditor.mTouchFocusSelected : DEBUG_EXTRACT;
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
        if (isMarqueeFadeEnabled() && this.mMarquee != null && !this.mMarquee.isStopped()) {
            Marquee marquee = this.mMarquee;
            if (marquee.shouldDrawLeftFade()) {
                return getHorizontalFadingEdgeStrength(marquee.getScroll(), 0.0f);
            }
            return 0.0f;
        } else if (getLineCount() != SANS) {
            return super.getLeftFadingEdgeStrength();
        } else {
            float lineLeft = getLayout().getLineLeft(MARQUEE_FADE_NORMAL);
            if (lineLeft > ((float) this.mScrollX)) {
                return 0.0f;
            }
            return getHorizontalFadingEdgeStrength((float) this.mScrollX, lineLeft);
        }
    }

    protected float getRightFadingEdgeStrength() {
        if (isMarqueeFadeEnabled() && this.mMarquee != null && !this.mMarquee.isStopped()) {
            Marquee marquee = this.mMarquee;
            return getHorizontalFadingEdgeStrength(marquee.getMaxFadeScroll(), marquee.getScroll());
        } else if (getLineCount() != SANS) {
            return super.getRightFadingEdgeStrength();
        } else {
            float rightEdge = (float) (this.mScrollX + ((getWidth() - getCompoundPaddingLeft()) - getCompoundPaddingRight()));
            float lineRight = getLayout().getLineRight(MARQUEE_FADE_NORMAL);
            if (lineRight < rightEdge) {
                return 0.0f;
            }
            return getHorizontalFadingEdgeStrength(rightEdge, lineRight);
        }
    }

    private final float getHorizontalFadingEdgeStrength(float position1, float position2) {
        int horizontalFadingEdgeLength = getHorizontalFadingEdgeLength();
        if (horizontalFadingEdgeLength == 0) {
            return 0.0f;
        }
        float diff = Math.abs(position1 - position2);
        if (diff > ((float) horizontalFadingEdgeLength)) {
            return LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        }
        return diff / ((float) horizontalFadingEdgeLength);
    }

    private final boolean isMarqueeFadeEnabled() {
        if (this.mEllipsize == TruncateAt.MARQUEE) {
            return this.mMarqueeFadeMode != SANS ? true : DEBUG_EXTRACT;
        } else {
            return DEBUG_EXTRACT;
        }
    }

    protected int computeHorizontalScrollRange() {
        if (this.mLayout == null) {
            return super.computeHorizontalScrollRange();
        }
        int lineWidth = (this.mSingleLine && (this.mGravity & 7) == MONOSPACE) ? (int) this.mLayout.getLineWidth(MARQUEE_FADE_NORMAL) : this.mLayout.getWidth();
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
        if (!outViews.contains(this) && (flags & SANS) != 0 && !TextUtils.isEmpty(searched) && !TextUtils.isEmpty(this.mText)) {
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
            int ap = a.getResourceId(SANS, MARQUEE_FADE_NORMAL);
            if (ap != 0) {
                TypedArray appearance = context.obtainStyledAttributes(ap, android.R.styleable.TextAppearance);
                colors = appearance.getColorStateList(MONOSPACE);
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
        if (!event.hasModifiers(HwPerformance.PERF_EVENT_RAW_REQ)) {
            if (event.hasModifiers(HwPerformance.PERF_EVENT_PROBE)) {
                switch (keyCode) {
                    case StatisticalConstant.TYPE_SINGLEHAND_END /*50*/:
                        if (canPaste()) {
                            return onTextContextMenuItem(ID_PASTE_AS_PLAIN_TEXT);
                        }
                        break;
                    case StatisticalConstant.TYPE_WIFI_CONNECTION_ACTION /*54*/:
                        if (canRedo()) {
                            return onTextContextMenuItem(ID_REDO);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        switch (keyCode) {
            case PerfHub.PERF_TAG_DEF_HMP_UP_THRES /*29*/:
                if (canSelectText()) {
                    return onTextContextMenuItem(ID_SELECT_ALL);
                }
                break;
            case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CHANGE /*31*/:
                if (canCopy()) {
                    return onTextContextMenuItem(ID_COPY);
                }
                break;
            case StatisticalConstant.TYPE_SINGLEHAND_END /*50*/:
                if (canPaste()) {
                    return onTextContextMenuItem(ID_PASTE);
                }
                break;
            case StatisticalConstant.TYPE_WIFI_OPERATION_INFO /*52*/:
                if (canCut()) {
                    return onTextContextMenuItem(ID_CUT);
                }
                break;
            case StatisticalConstant.TYPE_WIFI_CONNECTION_ACTION /*54*/:
                if (canUndo()) {
                    return onTextContextMenuItem(ID_UNDO);
                }
                break;
        }
        return super.onKeyShortcut(keyCode, event);
    }

    boolean canSelectText() {
        return (this.mText.length() == 0 || this.mEditor == null) ? DEBUG_EXTRACT : this.mEditor.hasSelectionController();
    }

    boolean textCanBeSelected() {
        boolean z = DEBUG_EXTRACT;
        if (this.mMovement == null || !this.mMovement.canSelectArbitrarily()) {
            return DEBUG_EXTRACT;
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
        if (this.mCurrentSpellCheckerLocaleCache != null || allowNullLocale) {
            return this.mCurrentSpellCheckerLocaleCache;
        }
        return Locale.getDefault();
    }

    public Locale getTextServicesLocale() {
        return getTextServicesLocale(DEBUG_EXTRACT);
    }

    public boolean isInExtractedMode() {
        return DEBUG_EXTRACT;
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
        Locale localeObject;
        SpellCheckerSubtype subtype = ((TextServicesManager) this.mContext.getSystemService("textservices")).getCurrentSpellCheckerSubtype(true);
        if (subtype != null) {
            localeObject = subtype.getLocaleObject();
        } else {
            localeObject = null;
        }
        this.mCurrentSpellCheckerLocaleCache = localeObject;
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

    private boolean shouldSpeakPasswordsForAccessibility() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), "speak_password", MARQUEE_FADE_NORMAL, -3) == SANS ? true : DEBUG_EXTRACT;
    }

    public CharSequence getAccessibilityClassName() {
        return TextView.class.getName();
    }

    public void onProvideStructure(ViewStructure structure) {
        boolean isPassword;
        super.onProvideStructure(structure);
        if (hasPasswordTransformationMethod()) {
            isPassword = true;
        } else {
            isPassword = isPasswordInputType(getInputType());
        }
        if (!isPassword) {
            if (this.mLayout == null) {
                assumeLayout();
            }
            Layout layout = this.mLayout;
            int lineCount = layout.getLineCount();
            if (lineCount <= SANS) {
                structure.setText(getText(), getSelectionStart(), getSelectionEnd());
            } else {
                int topLine;
                int bottomLine;
                int[] tmpCords = new int[SIGNED];
                getLocationInWindow(tmpCords);
                int topWindowLocation = tmpCords[SANS];
                View root = this;
                ViewParent viewParent = getParent();
                while (viewParent instanceof View) {
                    root = (View) viewParent;
                    viewParent = root.getParent();
                }
                int windowHeight = root.getHeight();
                if (topWindowLocation >= 0) {
                    topLine = getLineAtCoordinateUnclamped(0.0f);
                    bottomLine = getLineAtCoordinateUnclamped((float) (windowHeight + KEY_EVENT_HANDLED));
                } else {
                    topLine = getLineAtCoordinateUnclamped((float) (-topWindowLocation));
                    bottomLine = getLineAtCoordinateUnclamped((float) ((windowHeight + KEY_EVENT_HANDLED) - topWindowLocation));
                }
                int expandedTopLine = topLine - ((bottomLine - topLine) / SIGNED);
                if (expandedTopLine < 0) {
                    expandedTopLine = MARQUEE_FADE_NORMAL;
                }
                int expandedBottomLine = bottomLine + ((bottomLine - topLine) / SIGNED);
                if (expandedBottomLine >= lineCount) {
                    expandedBottomLine = lineCount + KEY_EVENT_HANDLED;
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
                CharSequence text = getText();
                if (expandedTopChar > 0 || expandedBottomChar < text.length()) {
                    text = text.subSequence(expandedTopChar, expandedBottomChar);
                }
                structure.setText(text, selStart - expandedTopChar, selEnd - expandedTopChar);
                int[] lineOffsets = new int[((bottomLine - topLine) + SANS)];
                int[] lineBaselines = new int[((bottomLine - topLine) + SANS)];
                int baselineOffset = getBaselineOffset();
                for (int i = topLine; i <= bottomLine; i += SANS) {
                    lineOffsets[i - topLine] = layout.getLineStart(i);
                    lineBaselines[i - topLine] = layout.getLineBaseline(i) + baselineOffset;
                }
                structure.setTextLines(lineOffsets, lineBaselines);
            }
            int style = MARQUEE_FADE_NORMAL;
            int typefaceStyle = getTypefaceStyle();
            if ((typefaceStyle & SANS) != 0) {
                style = SANS;
            }
            if ((typefaceStyle & SIGNED) != 0) {
                style |= SIGNED;
            }
            int paintFlags = this.mTextPaint.getFlags();
            if ((paintFlags & 32) != 0) {
                style |= SANS;
            }
            if ((paintFlags & 8) != 0) {
                style |= DECIMAL;
            }
            if ((paintFlags & 16) != 0) {
                style |= 8;
            }
            structure.setTextStyle(getTextSize(), getCurrentTextColor(), SANS, style);
        }
        structure.setHint(getHint());
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        event.setPassword(hasPasswordTransformationMethod());
        if (event.getEventType() == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
            event.setFromIndex(Selection.getSelectionStart(this.mText));
            event.setToIndex(Selection.getSelectionEnd(this.mText));
            event.setItemCount(this.mText.length());
        }
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        info.setPassword(hasPasswordTransformationMethod());
        info.setText(getTextForAccessibility());
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
            info.addAction((int) GL10.GL_DEPTH_BUFFER_BIT);
            info.addAction((int) GL10.GL_NEVER);
            info.setMovementGranularities(31);
            info.addAction((int) Protocol.BASE_WIFI);
        }
        if (isFocused()) {
            if (canCopy()) {
                info.addAction((int) GL10.GL_LIGHT0);
            }
            if (canPaste()) {
                info.addAction((int) AccessibilityNodeInfo.ACTION_PASTE);
            }
            if (canCut()) {
                info.addAction((int) Protocol.BASE_SYSTEM_RESERVED);
            }
            if (canShare()) {
                info.addAction(new AccessibilityAction(ACCESSIBILITY_ACTION_SHARE, getResources().getString(R.string.share)));
            }
            if (canProcessText()) {
                this.mEditor.mProcessTextIntentActionsHandler.onInitializeAccessibilityNodeInfo(info);
            }
        }
        int numFilters = this.mFilters.length;
        for (int i = MARQUEE_FADE_NORMAL; i < numFilters; i += SANS) {
            InputFilter filter = this.mFilters[i];
            if (filter instanceof LengthFilter) {
                info.setMaxTextLength(((LengthFilter) filter).getMax());
            }
        }
        if (!isSingleLine()) {
            info.setMultiLine(true);
        }
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        CharSequence text = null;
        if (this.mEditor != null && this.mEditor.mProcessTextIntentActionsHandler.performAccessibilityAction(action)) {
            return true;
        }
        switch (action) {
            case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
                return performAccessibilityActionClick(arguments);
            case GL10.GL_DEPTH_BUFFER_BIT /*256*/:
            case GL10.GL_NEVER /*512*/:
                ensureIterableTextForAccessibilitySelectable();
                return super.performAccessibilityActionInternal(action, arguments);
            case GL10.GL_LIGHT0 /*16384*/:
                return (isFocused() && canCopy() && onTextContextMenuItem(ID_COPY)) ? true : DEBUG_EXTRACT;
            case AccessibilityNodeInfo.ACTION_PASTE /*32768*/:
                return (isFocused() && canPaste() && onTextContextMenuItem(ID_PASTE)) ? true : DEBUG_EXTRACT;
            case Protocol.BASE_SYSTEM_RESERVED /*65536*/:
                return (isFocused() && canCut() && onTextContextMenuItem(ID_CUT)) ? true : DEBUG_EXTRACT;
            case Protocol.BASE_WIFI /*131072*/:
                ensureIterableTextForAccessibilitySelectable();
                text = getIterableTextForAccessibility();
                if (text == null) {
                    return DEBUG_EXTRACT;
                }
                int start;
                int end;
                if (arguments != null) {
                    start = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, KEY_EVENT_HANDLED);
                } else {
                    start = KEY_EVENT_HANDLED;
                }
                if (arguments != null) {
                    end = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, KEY_EVENT_HANDLED);
                } else {
                    end = KEY_EVENT_HANDLED;
                }
                if (!(getSelectionStart() == start && getSelectionEnd() == end)) {
                    if (start == end && end == KEY_EVENT_HANDLED) {
                        Selection.removeSelection((Spannable) text);
                        return true;
                    } else if (start >= 0 && start <= end && end <= text.length()) {
                        Selection.setSelection((Spannable) text, start, end);
                        if (this.mEditor != null) {
                            this.mEditor.startSelectionActionMode();
                        }
                        return true;
                    }
                }
                return DEBUG_EXTRACT;
            case AccessibilityNodeInfo.ACTION_SET_TEXT /*2097152*/:
                if (!isEnabled() || this.mBufferType != BufferType.EDITABLE) {
                    return DEBUG_EXTRACT;
                }
                if (arguments != null) {
                    text = arguments.getCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE);
                }
                setText(text);
                if (this.mText != null) {
                    int updatedTextLength = this.mText.length();
                    if (updatedTextLength > 0) {
                        Selection.setSelection((Spannable) this.mText, updatedTextLength);
                    }
                }
                return true;
            case ACCESSIBILITY_ACTION_SHARE /*268435456*/:
                return (isFocused() && canShare() && onTextContextMenuItem(ID_SHARE)) ? true : DEBUG_EXTRACT;
            default:
                return super.performAccessibilityActionInternal(action, arguments);
        }
    }

    private boolean performAccessibilityActionClick(Bundle arguments) {
        boolean handled = DEBUG_EXTRACT;
        if (!isEnabled()) {
            return DEBUG_EXTRACT;
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
            if (!(isTextSelectable() || !this.mEditor.mShowSoftInputOnFocus || imm == null)) {
                handled |= imm.showSoftInput(this, MARQUEE_FADE_NORMAL);
            }
        }
        return handled;
    }

    private boolean hasSpannableText() {
        return this.mText != null ? this.mText instanceof Spannable : DEBUG_EXTRACT;
    }

    public void sendAccessibilityEventInternal(int eventType) {
        if (eventType == AccessibilityNodeInfo.ACTION_PASTE && this.mEditor != null) {
            this.mEditor.mProcessTextIntentActionsHandler.initializeAccessibilityActions();
        }
        if (eventType != HwPerformance.PERF_EVENT_RAW_REQ) {
            super.sendAccessibilityEventInternal(eventType);
        }
    }

    private CharSequence getTextForAccessibility() {
        if (TextUtils.isEmpty(this.mText)) {
            return this.mHint;
        }
        if (hasPasswordTransformationMethod() && shouldSpeakPasswordsForAccessibility()) {
            return this.mText;
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
        return imm != null ? imm.isActive(this) : DEBUG_EXTRACT;
    }

    public boolean onTextContextMenuItem(int id) {
        int min = MARQUEE_FADE_NORMAL;
        int max = this.mText.length();
        if (isFocused()) {
            int selStart = getSelectionStart();
            int selEnd = getSelectionEnd();
            min = Math.max(MARQUEE_FADE_NORMAL, Math.min(selStart, selEnd));
            max = Math.max(MARQUEE_FADE_NORMAL, Math.max(selStart, selEnd));
        }
        switch (id) {
            case ID_SELECT_ALL /*16908319*/:
                selectAllText();
                return true;
            case ID_CUT /*16908320*/:
                setPrimaryClip(ClipData.newPlainText(null, getTransformedText(min, max)));
                deleteText_internal(min, max);
                return true;
            case ID_COPY /*16908321*/:
                setPrimaryClip(ClipData.newPlainText(null, getTransformedText(min, max)));
                stopTextActionMode();
                if (getTextCopyFinishedListener() != null) {
                    getTextCopyFinishedListener().copyDone();
                }
                return true;
            case ID_PASTE /*16908322*/:
                paste(min, max, true);
                return true;
            case ID_PASTE_AS_PLAIN_TEXT /*16908337*/:
                paste(min, max, DEBUG_EXTRACT);
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
                    } catch (NotFoundException e) {
                        Log.e(LOG_TAG, "Widget of Editor resource not found issue.", e);
                    }
                }
                return true;
            case ID_SHARE /*16908341*/:
                shareSelectedText();
                return true;
            default:
                return DEBUG_EXTRACT;
        }
    }

    CharSequence getTransformedText(int start, int end) {
        return removeSuggestionSpans(this.mTransformed.subSequence(start, end));
    }

    public boolean performLongClick() {
        boolean handled = DEBUG_EXTRACT;
        if (this.mEditor != null) {
            this.mEditor.mIsBeingLongClicked = true;
        }
        boolean hapticEffectDone = DEBUG_EXTRACT;
        if (super.performLongClick()) {
            handled = true;
            hapticEffectDone = true;
        }
        if (this.mEditor != null) {
            handled |= this.mEditor.performLongClick(handled);
            this.mEditor.mIsBeingLongClicked = DEBUG_EXTRACT;
        }
        if (handled) {
            if (this.mHwTextView == null || hapticEffectDone || !mIsVibrateImplemented) {
                performHapticFeedback(MARQUEE_FADE_NORMAL);
            } else {
                this.mHwTextView.playIvtEffect(this.mContext, "TEXTVIEW_LONG_PRESS_SELECTWORD");
            }
            if (this.mEditor != null) {
                this.mEditor.mDiscardNextActionUp = true;
            }
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
        if (this.mEditor == null || (this.mEditor.mInputType & 15) != SANS || (this.mEditor.mInputType & Protocol.BASE_CONNECTIVITY_MANAGER) > 0) {
            return DEBUG_EXTRACT;
        }
        int variation = this.mEditor.mInputType & InputType.TYPE_MASK_VARIATION;
        if (!(variation == 0 || variation == 48 || variation == 80 || variation == 64 || variation == LogPower.WAKELOCK_ACQUIRED)) {
            z = DEBUG_EXTRACT;
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

    protected void stopTextActionMode() {
        if (this.mEditor != null) {
            this.mEditor.stopTextActionMode();
        }
    }

    boolean canUndo() {
        return this.mEditor != null ? this.mEditor.canUndo() : DEBUG_EXTRACT;
    }

    boolean canRedo() {
        return this.mEditor != null ? this.mEditor.canRedo() : DEBUG_EXTRACT;
    }

    boolean canCut() {
        if (!hasPasswordTransformationMethod() && this.mText.length() > 0 && hasSelection() && (this.mText instanceof Editable) && this.mEditor != null && this.mEditor.mKeyListener != null) {
            return true;
        }
        return DEBUG_EXTRACT;
    }

    boolean canCopy() {
        if (!hasPasswordTransformationMethod() && this.mText.length() > 0 && hasSelection() && this.mEditor != null) {
            return true;
        }
        return DEBUG_EXTRACT;
    }

    boolean canShare() {
        if (getContext().canStartActivityForResult() && isDeviceProvisioned()) {
            return canCopy();
        }
        return DEBUG_EXTRACT;
    }

    boolean isDeviceProvisioned() {
        if (this.mDeviceProvisionedState == 0) {
            int i;
            if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", MARQUEE_FADE_NORMAL) != 0) {
                i = SIGNED;
            } else {
                i = SANS;
            }
            this.mDeviceProvisionedState = i;
        }
        if (this.mDeviceProvisionedState == SIGNED) {
            return true;
        }
        return DEBUG_EXTRACT;
    }

    boolean canPaste() {
        if (!(this.mText instanceof Editable) || this.mEditor == null || this.mEditor.mKeyListener == null || getSelectionStart() < 0 || getSelectionEnd() < 0) {
            return DEBUG_EXTRACT;
        }
        return ((ClipboardManager) getContext().getSystemService("clipboard")).hasPrimaryClip();
    }

    boolean canProcessText() {
        if (getId() == KEY_EVENT_HANDLED) {
            return DEBUG_EXTRACT;
        }
        return canShare();
    }

    boolean canSelectAllText() {
        if (!canSelectText() || hasPasswordTransformationMethod()) {
            return DEBUG_EXTRACT;
        }
        if (getSelectionStart() == 0 && getSelectionEnd() == this.mText.length()) {
            return DEBUG_EXTRACT;
        }
        return true;
    }

    boolean selectAllText() {
        int length = this.mText.length();
        Selection.setSelection((Spannable) this.mText, MARQUEE_FADE_NORMAL, length);
        return length > 0 ? true : DEBUG_EXTRACT;
    }

    void replaceSelectionWithText(CharSequence text) {
        ((Editable) this.mText).replace(getSelectionStart(), getSelectionEnd(), text);
    }

    private void paste(int min, int max, boolean withFormatting) {
        ClipData clip = ((ClipboardManager) getContext().getSystemService("clipboard")).getPrimaryClip();
        if (clip != null) {
            boolean didFirst = DEBUG_EXTRACT;
            for (int i = MARQUEE_FADE_NORMAL; i < clip.getItemCount(); i += SANS) {
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
        if (selectedText != null && !selectedText.isEmpty()) {
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null && imm.isActive(this)) {
                imm.hideSoftInputFromWindow(getWindowToken(), MARQUEE_FADE_NORMAL);
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
            return KEY_EVENT_HANDLED;
        }
        return getOffsetAtCoordinate(getLineAtCoordinate(y), x);
    }

    float convertToLocalHorizontalCoordinate(float x) {
        return Math.min((float) ((getWidth() - getTotalPaddingRight()) + KEY_EVENT_HANDLED), Math.max(0.0f, x - ((float) getTotalPaddingLeft()))) + ((float) getScrollX());
    }

    int getLineAtCoordinate(float y) {
        return getLayout().getLineForVertical((int) (Math.min((float) ((getHeight() - getTotalPaddingBottom()) + KEY_EVENT_HANDLED), Math.max(0.0f, y - ((float) getTotalPaddingTop()))) + ((float) getScrollY())));
    }

    int getLineAtCoordinateUnclamped(float y) {
        return getLayout().getLineForVertical((int) ((y - ((float) getTotalPaddingTop())) + ((float) getScrollY())));
    }

    int getOffsetAtCoordinate(int line, float x) {
        return getLayout().getOffsetForHorizontal(line, convertToLocalHorizontalCoordinate(x));
    }

    public boolean onDragEvent(DragEvent event) {
        switch (event.getAction()) {
            case SANS /*1*/:
                return this.mEditor != null ? this.mEditor.hasInsertionController() : DEBUG_EXTRACT;
            case SIGNED /*2*/:
                Selection.setSelection((Spannable) this.mText, getOffsetForPosition(event.getX(), event.getY()));
                return true;
            case MONOSPACE /*3*/:
                if (this.mEditor != null) {
                    this.mEditor.onDrop(event);
                }
                return true;
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                requestFocus();
                return true;
            default:
                return true;
        }
    }

    boolean isInBatchEditMode() {
        boolean z = DEBUG_EXTRACT;
        if (this.mEditor == null) {
            return DEBUG_EXTRACT;
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
        if (!hasPasswordTransformationMethod()) {
            boolean defaultIsRtl = getLayoutDirection() == SANS ? true : DEBUG_EXTRACT;
            switch (getTextDirection()) {
                case SIGNED /*2*/:
                    return TextDirectionHeuristics.ANYRTL_LTR;
                case MONOSPACE /*3*/:
                    return TextDirectionHeuristics.LTR;
                case DECIMAL /*4*/:
                    return TextDirectionHeuristics.RTL;
                case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                    return TextDirectionHeuristics.LOCALE;
                case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                    return TextDirectionHeuristics.FIRSTSTRONG_LTR;
                case HwCfgFilePolicy.CLOUD_APN /*7*/:
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
        } else if (isRtlLocale()) {
            return TextDirectionHeuristics.FIRSTSTRONG_LTR;
        } else {
            return TextDirectionHeuristics.LTR;
        }
    }

    public void onResolveDrawables(int layoutDirection) {
        if (this.mLastLayoutDirection != layoutDirection) {
            this.mLastLayoutDirection = layoutDirection;
            if (this.mDrawables != null && this.mDrawables.resolveWithLayoutDirection(layoutDirection)) {
                prepareDrawableForDisplay(this.mDrawables.mShowing[MARQUEE_FADE_NORMAL]);
                prepareDrawableForDisplay(this.mDrawables.mShowing[SIGNED]);
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
        this.mLastLayoutDirection = KEY_EVENT_HANDLED;
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
            case DECIMAL /*4*/:
                Spannable text = (Spannable) getIterableTextForAccessibility();
                if (!(TextUtils.isEmpty(text) || getLayout() == null)) {
                    LineTextSegmentIterator iterator = LineTextSegmentIterator.getInstance();
                    iterator.initialize(text, getLayout());
                    return iterator;
                }
            case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
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
        if (this.mTrySelectAllAndShowEditor && !this.mSelectAllAndShowEditorDone) {
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
