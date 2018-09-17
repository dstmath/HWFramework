package android.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.IntProperty;
import android.util.MathUtils;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroupOverlay;
import android.view.WindowManager;
import android.widget.ImageView.ScaleType;
import com.android.internal.R;
import com.android.internal.util.AsyncService;
import com.android.internal.widget.AutoScrollHelper;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;

public class FastScroller {
    private static Property<View, Integer> BOTTOM = null;
    private static final int DURATION_CROSS_FADE = 50;
    private static final int DURATION_FADE_IN = 150;
    private static final int DURATION_FADE_OUT = 300;
    private static final int DURATION_RESIZE = 100;
    private static final long FADE_TIMEOUT = 1500;
    private static Property<View, Integer> LEFT = null;
    private static final int MIN_PAGES = 4;
    private static final int OVERLAY_ABOVE_THUMB = 2;
    private static final int OVERLAY_AT_THUMB = 1;
    private static final int OVERLAY_FLOATING = 0;
    private static final int PREVIEW_LEFT = 0;
    private static final int PREVIEW_RIGHT = 1;
    private static Property<View, Integer> RIGHT = null;
    private static final int STATE_DRAGGING = 2;
    private static final int STATE_NONE = 0;
    private static final int STATE_VISIBLE = 1;
    private static final long TAP_TIMEOUT = 0;
    private static final int THUMB_POSITION_INSIDE = 1;
    private static final int THUMB_POSITION_MIDPOINT = 0;
    private static Property<View, Integer> TOP;
    private boolean mAlwaysShow;
    private final Rect mContainerRect;
    private int mCurrentSection;
    private AnimatorSet mDecorAnimation;
    private final Runnable mDeferHide;
    private boolean mEnabled;
    private int mFirstVisibleItem;
    private int mHeaderCount;
    private float mInitialTouchY;
    private boolean mLayoutFromRight;
    private final AbsListView mList;
    private Adapter mListAdapter;
    private boolean mLongList;
    private boolean mMatchDragPosition;
    private final int mMinimumTouchTarget;
    private int mOldChildCount;
    private int mOldItemCount;
    private final ViewGroupOverlay mOverlay;
    private int mOverlayPosition;
    private long mPendingDrag;
    private AnimatorSet mPreviewAnimation;
    private final View mPreviewImage;
    private int mPreviewMinHeight;
    private int mPreviewMinWidth;
    private int mPreviewPadding;
    private final int[] mPreviewResId;
    private final TextView mPrimaryText;
    private int mScaledTouchSlop;
    private int mScrollBarStyle;
    private boolean mScrollCompleted;
    private int mScrollbarPosition;
    private final TextView mSecondaryText;
    private SectionIndexer mSectionIndexer;
    private Object[] mSections;
    private boolean mShowingPreview;
    private boolean mShowingPrimary;
    private int mState;
    private final AnimatorListener mSwitchPrimaryListener;
    private final Rect mTempBounds;
    private final Rect mTempMargins;
    private int mTextAppearance;
    private ColorStateList mTextColor;
    private float mTextSize;
    private Drawable mThumbDrawable;
    private final ImageView mThumbImage;
    private int mThumbMinHeight;
    private int mThumbMinWidth;
    private float mThumbOffset;
    private int mThumbPosition;
    private float mThumbRange;
    private Drawable mTrackDrawable;
    private final ImageView mTrackImage;
    private boolean mUpdatingLayout;
    private int mWidth;

    /* renamed from: android.widget.FastScroller.3 */
    static class AnonymousClass3 extends IntProperty<View> {
        AnonymousClass3(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, int value) {
            object.setLeft(value);
        }

        public Integer get(View object) {
            return Integer.valueOf(object.getLeft());
        }
    }

    /* renamed from: android.widget.FastScroller.4 */
    static class AnonymousClass4 extends IntProperty<View> {
        AnonymousClass4(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, int value) {
            object.setTop(value);
        }

        public Integer get(View object) {
            return Integer.valueOf(object.getTop());
        }
    }

    /* renamed from: android.widget.FastScroller.5 */
    static class AnonymousClass5 extends IntProperty<View> {
        AnonymousClass5(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, int value) {
            object.setRight(value);
        }

        public Integer get(View object) {
            return Integer.valueOf(object.getRight());
        }
    }

    /* renamed from: android.widget.FastScroller.6 */
    static class AnonymousClass6 extends IntProperty<View> {
        AnonymousClass6(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, int value) {
            object.setBottom(value);
        }

        public Integer get(View object) {
            return Integer.valueOf(object.getBottom());
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.FastScroller.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.FastScroller.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.widget.FastScroller.<clinit>():void");
    }

    public FastScroller(AbsListView listView, int styleResId) {
        boolean z = true;
        this.mTempBounds = new Rect();
        this.mTempMargins = new Rect();
        this.mContainerRect = new Rect();
        this.mPreviewResId = new int[STATE_DRAGGING];
        this.mCurrentSection = -1;
        this.mScrollbarPosition = -1;
        this.mPendingDrag = -1;
        this.mDeferHide = new Runnable() {
            public void run() {
                FastScroller.this.setState(FastScroller.STATE_NONE);
            }
        };
        this.mSwitchPrimaryListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                FastScroller.this.mShowingPrimary = !FastScroller.this.mShowingPrimary;
            }
        };
        this.mList = listView;
        this.mOldItemCount = listView.getCount();
        this.mOldChildCount = listView.getChildCount();
        Context context = listView.getContext();
        this.mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mScrollBarStyle = listView.getScrollBarStyle();
        this.mScrollCompleted = true;
        this.mState = THUMB_POSITION_INSIDE;
        if (context.getApplicationInfo().targetSdkVersion < 11) {
            z = false;
        }
        this.mMatchDragPosition = z;
        this.mTrackImage = new ImageView(context);
        this.mTrackImage.setScaleType(ScaleType.FIT_XY);
        this.mThumbImage = new ImageView(context);
        this.mThumbImage.setScaleType(ScaleType.FIT_XY);
        this.mPreviewImage = new View(context);
        this.mPreviewImage.setAlpha(0.0f);
        this.mPrimaryText = createPreviewTextView(context);
        this.mSecondaryText = createPreviewTextView(context);
        this.mMinimumTouchTarget = listView.getResources().getDimensionPixelSize(R.dimen.fast_scroller_minimum_touch_target);
        setStyle(styleResId);
        ViewGroupOverlay overlay = listView.getOverlay();
        this.mOverlay = overlay;
        overlay.add(this.mTrackImage);
        overlay.add(this.mThumbImage);
        overlay.add(this.mPreviewImage);
        overlay.add(this.mPrimaryText);
        overlay.add(this.mSecondaryText);
        getSectionsFromIndexer();
        updateLongList(this.mOldChildCount, this.mOldItemCount);
        setScrollbarPosition(listView.getVerticalScrollbarPosition());
        postAutoHide();
    }

    private void updateAppearance() {
        int width = STATE_NONE;
        this.mTrackImage.setImageDrawable(this.mTrackDrawable);
        if (this.mTrackDrawable != null) {
            width = Math.max(STATE_NONE, this.mTrackDrawable.getIntrinsicWidth());
        }
        this.mThumbImage.setImageDrawable(this.mThumbDrawable);
        this.mThumbImage.setMinimumWidth(this.mThumbMinWidth);
        this.mThumbImage.setMinimumHeight(this.mThumbMinHeight);
        if (this.mThumbDrawable != null) {
            width = Math.max(width, this.mThumbDrawable.getIntrinsicWidth());
        }
        this.mWidth = Math.max(width, this.mThumbMinWidth);
        if (this.mTextAppearance != 0) {
            this.mPrimaryText.setTextAppearance(this.mTextAppearance);
            this.mSecondaryText.setTextAppearance(this.mTextAppearance);
        }
        if (this.mTextColor != null) {
            this.mPrimaryText.setTextColor(this.mTextColor);
            this.mSecondaryText.setTextColor(this.mTextColor);
        }
        if (this.mTextSize > 0.0f) {
            this.mPrimaryText.setTextSize(STATE_NONE, this.mTextSize);
            this.mSecondaryText.setTextSize(STATE_NONE, this.mTextSize);
        }
        int padding = this.mPreviewPadding;
        this.mPrimaryText.setIncludeFontPadding(false);
        this.mPrimaryText.setPadding(padding, padding, padding, padding);
        this.mSecondaryText.setIncludeFontPadding(false);
        this.mSecondaryText.setPadding(padding, padding, padding, padding);
        refreshDrawablePressedState();
    }

    public void setStyle(int resId) {
        TypedArray ta = this.mList.getContext().obtainStyledAttributes(null, R.styleable.FastScroll, R.attr.fastScrollStyle, resId);
        int N = ta.getIndexCount();
        for (int i = STATE_NONE; i < N; i += THUMB_POSITION_INSIDE) {
            int index = ta.getIndex(i);
            switch (index) {
                case STATE_NONE /*0*/:
                    this.mTextAppearance = ta.getResourceId(index, STATE_NONE);
                    break;
                case THUMB_POSITION_INSIDE /*1*/:
                    this.mTextSize = (float) ta.getDimensionPixelSize(index, STATE_NONE);
                    break;
                case STATE_DRAGGING /*2*/:
                    this.mTextColor = ta.getColorStateList(index);
                    break;
                case HwCfgFilePolicy.BASE /*3*/:
                    this.mPreviewPadding = ta.getDimensionPixelSize(index, STATE_NONE);
                    break;
                case MIN_PAGES /*4*/:
                    this.mPreviewMinWidth = ta.getDimensionPixelSize(index, STATE_NONE);
                    break;
                case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                    this.mPreviewMinHeight = ta.getDimensionPixelSize(index, STATE_NONE);
                    break;
                case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                    this.mThumbPosition = ta.getInt(index, STATE_NONE);
                    break;
                case HwCfgFilePolicy.CLOUD_APN /*7*/:
                    this.mThumbDrawable = ta.getDrawable(index);
                    break;
                case PGSdk.TYPE_VIDEO /*8*/:
                    this.mThumbMinWidth = ta.getDimensionPixelSize(index, STATE_NONE);
                    break;
                case PGSdk.TYPE_SCRLOCK /*9*/:
                    this.mThumbMinHeight = ta.getDimensionPixelSize(index, STATE_NONE);
                    break;
                case PGSdk.TYPE_CLOCK /*10*/:
                    this.mTrackDrawable = ta.getDrawable(index);
                    break;
                case PGSdk.TYPE_IM /*11*/:
                    this.mPreviewResId[THUMB_POSITION_INSIDE] = ta.getResourceId(index, STATE_NONE);
                    break;
                case PGSdk.TYPE_MUSIC /*12*/:
                    this.mPreviewResId[STATE_NONE] = ta.getResourceId(index, STATE_NONE);
                    break;
                case HwPerformance.PERF_VAL_DEV_TYPE_MAX /*13*/:
                    this.mOverlayPosition = ta.getInt(index, STATE_NONE);
                    break;
                default:
                    break;
            }
        }
        ta.recycle();
        updateAppearance();
    }

    public void remove() {
        this.mOverlay.remove(this.mTrackImage);
        this.mOverlay.remove(this.mThumbImage);
        this.mOverlay.remove(this.mPreviewImage);
        this.mOverlay.remove(this.mPrimaryText);
        this.mOverlay.remove(this.mSecondaryText);
    }

    public void setEnabled(boolean enabled) {
        if (this.mEnabled != enabled) {
            this.mEnabled = enabled;
            onStateDependencyChanged(true);
        }
    }

    public boolean isEnabled() {
        if (this.mEnabled) {
            return !this.mLongList ? this.mAlwaysShow : true;
        } else {
            return false;
        }
    }

    public void setAlwaysShow(boolean alwaysShow) {
        if (this.mAlwaysShow != alwaysShow) {
            this.mAlwaysShow = alwaysShow;
            onStateDependencyChanged(false);
        }
    }

    public boolean isAlwaysShowEnabled() {
        return this.mAlwaysShow;
    }

    private void onStateDependencyChanged(boolean peekIfEnabled) {
        if (!isEnabled()) {
            stop();
        } else if (isAlwaysShowEnabled()) {
            setState(THUMB_POSITION_INSIDE);
        } else if (this.mState == THUMB_POSITION_INSIDE) {
            postAutoHide();
        } else if (peekIfEnabled) {
            setState(THUMB_POSITION_INSIDE);
            postAutoHide();
        }
        this.mList.resolvePadding();
    }

    public void setScrollBarStyle(int style) {
        if (this.mScrollBarStyle != style) {
            this.mScrollBarStyle = style;
            updateLayout();
        }
    }

    public void stop() {
        setState(STATE_NONE);
    }

    public void setScrollbarPosition(int position) {
        int i = THUMB_POSITION_INSIDE;
        if (position == 0) {
            position = this.mList.isLayoutRtl() ? THUMB_POSITION_INSIDE : STATE_DRAGGING;
        }
        if (this.mScrollbarPosition != position) {
            boolean z;
            this.mScrollbarPosition = position;
            if (position != THUMB_POSITION_INSIDE) {
                z = true;
            } else {
                z = false;
            }
            this.mLayoutFromRight = z;
            int[] iArr = this.mPreviewResId;
            if (!this.mLayoutFromRight) {
                i = STATE_NONE;
            }
            this.mPreviewImage.setBackgroundResource(iArr[i]);
            int textMinWidth = Math.max(STATE_NONE, (this.mPreviewMinWidth - this.mPreviewImage.getPaddingLeft()) - this.mPreviewImage.getPaddingRight());
            this.mPrimaryText.setMinimumWidth(textMinWidth);
            this.mSecondaryText.setMinimumWidth(textMinWidth);
            int textMinHeight = Math.max(STATE_NONE, (this.mPreviewMinHeight - this.mPreviewImage.getPaddingTop()) - this.mPreviewImage.getPaddingBottom());
            this.mPrimaryText.setMinimumHeight(textMinHeight);
            this.mSecondaryText.setMinimumHeight(textMinHeight);
            updateLayout();
        }
    }

    public int getWidth() {
        return this.mWidth;
    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateLayout();
    }

    public void onItemCountChanged(int childCount, int itemCount) {
        boolean hasMoreItems = false;
        if (this.mOldItemCount != itemCount || this.mOldChildCount != childCount) {
            this.mOldItemCount = itemCount;
            this.mOldChildCount = childCount;
            if (itemCount - childCount > 0) {
                hasMoreItems = true;
            }
            if (hasMoreItems && this.mState != STATE_DRAGGING) {
                setThumbPos(getPosFromItemCount(this.mList.getFirstVisiblePosition(), childCount, itemCount));
            }
            updateLongList(childCount, itemCount);
        }
    }

    private void updateLongList(int childCount, int itemCount) {
        boolean longList = childCount > 0 && itemCount / childCount >= MIN_PAGES;
        if (this.mLongList != longList) {
            this.mLongList = longList;
            onStateDependencyChanged(false);
        }
    }

    private TextView createPreviewTextView(Context context) {
        LayoutParams params = new LayoutParams(-2, -2);
        TextView textView = new TextView(context);
        textView.setLayoutParams(params);
        textView.setSingleLine(true);
        textView.setEllipsize(TruncateAt.MIDDLE);
        textView.setGravity(17);
        textView.setAlpha(0.0f);
        textView.setLayoutDirection(this.mList.getLayoutDirection());
        return textView;
    }

    public void updateLayout() {
        if (!this.mUpdatingLayout) {
            this.mUpdatingLayout = true;
            updateContainerRect();
            layoutThumb();
            layoutTrack();
            updateOffsetAndRange();
            Rect bounds = this.mTempBounds;
            measurePreview(this.mPrimaryText, bounds);
            applyLayout(this.mPrimaryText, bounds);
            measurePreview(this.mSecondaryText, bounds);
            applyLayout(this.mSecondaryText, bounds);
            if (this.mPreviewImage != null) {
                bounds.left -= this.mPreviewImage.getPaddingLeft();
                bounds.top -= this.mPreviewImage.getPaddingTop();
                bounds.right += this.mPreviewImage.getPaddingRight();
                bounds.bottom += this.mPreviewImage.getPaddingBottom();
                applyLayout(this.mPreviewImage, bounds);
            }
            this.mUpdatingLayout = false;
        }
    }

    private void applyLayout(View view, Rect bounds) {
        view.layout(bounds.left, bounds.top, bounds.right, bounds.bottom);
        view.setPivotX((float) (this.mLayoutFromRight ? bounds.right - bounds.left : STATE_NONE));
    }

    private void measurePreview(View v, Rect out) {
        Rect margins = this.mTempMargins;
        margins.left = this.mPreviewImage.getPaddingLeft();
        margins.top = this.mPreviewImage.getPaddingTop();
        margins.right = this.mPreviewImage.getPaddingRight();
        margins.bottom = this.mPreviewImage.getPaddingBottom();
        if (this.mOverlayPosition == 0) {
            measureFloating(v, margins, out);
        } else {
            measureViewToSide(v, this.mThumbImage, margins, out);
        }
    }

    protected void measureViewToSide(View view, View adjacent, Rect margins, Rect out) {
        int marginLeft;
        int marginTop;
        int marginRight;
        int maxWidth;
        int right;
        int left;
        if (margins == null) {
            marginLeft = STATE_NONE;
            marginTop = STATE_NONE;
            marginRight = STATE_NONE;
        } else {
            marginLeft = margins.left;
            marginTop = margins.top;
            marginRight = margins.right;
        }
        Rect container = this.mContainerRect;
        int containerWidth = container.width();
        if (adjacent == null) {
            maxWidth = containerWidth;
        } else if (this.mLayoutFromRight) {
            maxWidth = adjacent.getLeft();
        } else {
            maxWidth = containerWidth - adjacent.getRight();
        }
        int adjMaxHeight = Math.max(STATE_NONE, container.height());
        int adjMaxWidth = Math.max(STATE_NONE, (maxWidth - marginLeft) - marginRight);
        view.measure(MeasureSpec.makeMeasureSpec(adjMaxWidth, RtlSpacingHelper.UNDEFINED), MeasureSpec.makeSafeMeasureSpec(adjMaxHeight, STATE_NONE));
        int width = Math.min(adjMaxWidth, view.getMeasuredWidth());
        if (this.mLayoutFromRight) {
            right = (adjacent == null ? container.right : adjacent.getLeft()) - marginRight;
            left = right - width;
        } else {
            left = (adjacent == null ? container.left : adjacent.getRight()) + marginLeft;
            right = left + width;
        }
        int top = marginTop;
        out.set(left, top, right, top + view.getMeasuredHeight());
    }

    private void measureFloating(View preview, Rect margins, Rect out) {
        int marginLeft;
        int marginTop;
        int marginRight;
        if (margins == null) {
            marginLeft = STATE_NONE;
            marginTop = STATE_NONE;
            marginRight = STATE_NONE;
        } else {
            marginLeft = margins.left;
            marginTop = margins.top;
            marginRight = margins.right;
        }
        Rect container = this.mContainerRect;
        int containerWidth = container.width();
        int adjMaxHeight = Math.max(STATE_NONE, container.height());
        preview.measure(MeasureSpec.makeMeasureSpec(Math.max(STATE_NONE, (containerWidth - marginLeft) - marginRight), RtlSpacingHelper.UNDEFINED), MeasureSpec.makeSafeMeasureSpec(adjMaxHeight, STATE_NONE));
        int containerHeight = container.height();
        int width = preview.getMeasuredWidth();
        int top = ((containerHeight / 10) + marginTop) + container.top;
        int bottom = top + preview.getMeasuredHeight();
        int left = ((containerWidth - width) / STATE_DRAGGING) + container.left;
        out.set(left, top, left + width, bottom);
    }

    private void updateContainerRect() {
        AbsListView list = this.mList;
        list.resolvePadding();
        Rect container = this.mContainerRect;
        container.left = STATE_NONE;
        container.top = STATE_NONE;
        container.right = list.getWidth();
        container.bottom = list.getHeight();
        int scrollbarStyle = this.mScrollBarStyle;
        if (scrollbarStyle == AsyncService.CMD_ASYNC_SERVICE_DESTROY || scrollbarStyle == 0) {
            container.left += list.getPaddingLeft();
            container.top += list.getPaddingTop();
            container.right -= list.getPaddingRight();
            container.bottom -= list.getPaddingBottom();
            if (scrollbarStyle == AsyncService.CMD_ASYNC_SERVICE_DESTROY) {
                int width = getWidth();
                if (this.mScrollbarPosition == STATE_DRAGGING) {
                    container.right += width;
                } else {
                    container.left -= width;
                }
            }
        }
    }

    private void layoutThumb() {
        Rect bounds = this.mTempBounds;
        measureViewToSide(this.mThumbImage, null, null, bounds);
        applyLayout(this.mThumbImage, bounds);
    }

    private void layoutTrack() {
        int top;
        int bottom;
        View track = this.mTrackImage;
        View thumb = this.mThumbImage;
        Rect container = this.mContainerRect;
        track.measure(MeasureSpec.makeMeasureSpec(Math.max(STATE_NONE, container.width()), RtlSpacingHelper.UNDEFINED), MeasureSpec.makeSafeMeasureSpec(Math.max(STATE_NONE, container.height()), STATE_NONE));
        if (this.mThumbPosition == THUMB_POSITION_INSIDE) {
            top = container.top;
            bottom = container.bottom;
        } else {
            int thumbHalfHeight = thumb.getHeight() / STATE_DRAGGING;
            top = container.top + thumbHalfHeight;
            bottom = container.bottom - thumbHalfHeight;
        }
        int trackWidth = track.getMeasuredWidth();
        int left = thumb.getLeft() + ((thumb.getWidth() - trackWidth) / STATE_DRAGGING);
        track.layout(left, top, left + trackWidth, bottom);
    }

    private void updateOffsetAndRange() {
        float min;
        float max;
        View trackImage = this.mTrackImage;
        View thumbImage = this.mThumbImage;
        if (this.mThumbPosition == THUMB_POSITION_INSIDE) {
            float halfThumbHeight = ((float) thumbImage.getHeight()) / 2.0f;
            min = ((float) trackImage.getTop()) + halfThumbHeight;
            max = ((float) trackImage.getBottom()) - halfThumbHeight;
        } else {
            min = (float) trackImage.getTop();
            max = (float) trackImage.getBottom();
        }
        this.mThumbOffset = min;
        this.mThumbRange = max - min;
    }

    private void setState(int state) {
        this.mList.removeCallbacks(this.mDeferHide);
        if (this.mAlwaysShow && state == 0) {
            state = THUMB_POSITION_INSIDE;
        }
        if (state != this.mState) {
            switch (state) {
                case STATE_NONE /*0*/:
                    transitionToHidden();
                    break;
                case THUMB_POSITION_INSIDE /*1*/:
                    transitionToVisible();
                    break;
                case STATE_DRAGGING /*2*/:
                    if (!transitionPreviewLayout(this.mCurrentSection)) {
                        transitionToVisible();
                        break;
                    } else {
                        transitionToDragging();
                        break;
                    }
            }
            this.mState = state;
            refreshDrawablePressedState();
        }
    }

    private void refreshDrawablePressedState() {
        boolean isPressed = this.mState == STATE_DRAGGING;
        this.mThumbImage.setPressed(isPressed);
        this.mTrackImage.setPressed(isPressed);
    }

    private void transitionToHidden() {
        if (this.mDecorAnimation != null) {
            this.mDecorAnimation.cancel();
        }
        Animator fadeOut = groupAnimatorOfFloat(View.ALPHA, 0.0f, this.mThumbImage, this.mTrackImage, this.mPreviewImage, this.mPrimaryText, this.mSecondaryText).setDuration(300);
        float offset = (float) (this.mLayoutFromRight ? this.mThumbImage.getWidth() : -this.mThumbImage.getWidth());
        Property property = View.TRANSLATION_X;
        View[] viewArr = new View[STATE_DRAGGING];
        viewArr[STATE_NONE] = this.mThumbImage;
        viewArr[THUMB_POSITION_INSIDE] = this.mTrackImage;
        Animator slideOut = groupAnimatorOfFloat(property, offset, viewArr).setDuration(300);
        this.mDecorAnimation = new AnimatorSet();
        AnimatorSet animatorSet = this.mDecorAnimation;
        Animator[] animatorArr = new Animator[STATE_DRAGGING];
        animatorArr[STATE_NONE] = fadeOut;
        animatorArr[THUMB_POSITION_INSIDE] = slideOut;
        animatorSet.playTogether(animatorArr);
        this.mDecorAnimation.start();
        this.mShowingPreview = false;
    }

    private void transitionToVisible() {
        if (this.mDecorAnimation != null) {
            this.mDecorAnimation.cancel();
        }
        Property property = View.ALPHA;
        View[] viewArr = new View[STATE_DRAGGING];
        viewArr[STATE_NONE] = this.mThumbImage;
        viewArr[THUMB_POSITION_INSIDE] = this.mTrackImage;
        Animator fadeIn = groupAnimatorOfFloat(property, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, viewArr).setDuration(150);
        Animator fadeOut = groupAnimatorOfFloat(View.ALPHA, 0.0f, this.mPreviewImage, this.mPrimaryText, this.mSecondaryText).setDuration(300);
        property = View.TRANSLATION_X;
        viewArr = new View[STATE_DRAGGING];
        viewArr[STATE_NONE] = this.mThumbImage;
        viewArr[THUMB_POSITION_INSIDE] = this.mTrackImage;
        Animator slideIn = groupAnimatorOfFloat(property, 0.0f, viewArr).setDuration(150);
        this.mDecorAnimation = new AnimatorSet();
        this.mDecorAnimation.playTogether(new Animator[]{fadeIn, fadeOut, slideIn});
        this.mDecorAnimation.start();
        this.mShowingPreview = false;
    }

    private void transitionToDragging() {
        if (this.mDecorAnimation != null) {
            this.mDecorAnimation.cancel();
        }
        Animator fadeIn = groupAnimatorOfFloat(View.ALPHA, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, this.mThumbImage, this.mTrackImage, this.mPreviewImage).setDuration(150);
        Property property = View.TRANSLATION_X;
        View[] viewArr = new View[STATE_DRAGGING];
        viewArr[STATE_NONE] = this.mThumbImage;
        viewArr[THUMB_POSITION_INSIDE] = this.mTrackImage;
        Animator slideIn = groupAnimatorOfFloat(property, 0.0f, viewArr).setDuration(150);
        this.mDecorAnimation = new AnimatorSet();
        AnimatorSet animatorSet = this.mDecorAnimation;
        Animator[] animatorArr = new Animator[STATE_DRAGGING];
        animatorArr[STATE_NONE] = fadeIn;
        animatorArr[THUMB_POSITION_INSIDE] = slideIn;
        animatorSet.playTogether(animatorArr);
        this.mDecorAnimation.start();
        this.mShowingPreview = true;
    }

    private void postAutoHide() {
        this.mList.removeCallbacks(this.mDeferHide);
        this.mList.postDelayed(this.mDeferHide, FADE_TIMEOUT);
    }

    public void onScroll(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean hasMoreItems = false;
        if (isEnabled()) {
            if (totalItemCount - visibleItemCount > 0) {
                hasMoreItems = true;
            }
            if (hasMoreItems && this.mState != STATE_DRAGGING) {
                setThumbPos(getPosFromItemCount(firstVisibleItem, visibleItemCount, totalItemCount));
            }
            this.mScrollCompleted = true;
            if (this.mFirstVisibleItem != firstVisibleItem) {
                this.mFirstVisibleItem = firstVisibleItem;
                if (this.mState != STATE_DRAGGING) {
                    setState(THUMB_POSITION_INSIDE);
                    postAutoHide();
                }
            }
            return;
        }
        setState(STATE_NONE);
    }

    private void getSectionsFromIndexer() {
        this.mSectionIndexer = null;
        Adapter adapter = this.mList.getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            this.mHeaderCount = ((HeaderViewListAdapter) adapter).getHeadersCount();
            adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }
        if (adapter instanceof ExpandableListConnector) {
            ExpandableListAdapter expAdapter = ((ExpandableListConnector) adapter).getAdapter();
            if (expAdapter instanceof SectionIndexer) {
                this.mSectionIndexer = (SectionIndexer) expAdapter;
                this.mListAdapter = adapter;
                this.mSections = this.mSectionIndexer.getSections();
            }
        } else if (adapter instanceof SectionIndexer) {
            this.mListAdapter = adapter;
            this.mSectionIndexer = (SectionIndexer) adapter;
            this.mSections = this.mSectionIndexer.getSections();
        } else {
            this.mListAdapter = adapter;
            this.mSections = null;
        }
    }

    public void onSectionsChanged() {
        this.mListAdapter = null;
    }

    private void scrollTo(float position) {
        int sectionIndex;
        this.mScrollCompleted = false;
        int count = this.mList.getCount();
        Object[] sections = this.mSections;
        int sectionCount = sections == null ? STATE_NONE : sections.length;
        ExpandableListView expList;
        if (sections == null || sectionCount <= THUMB_POSITION_INSIDE) {
            int index = MathUtils.constrain((int) (((float) count) * position), STATE_NONE, count - 1);
            if (this.mList instanceof ExpandableListView) {
                expList = (ExpandableListView) this.mList;
                expList.setSelectionFromTop(expList.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(this.mHeaderCount + index)), STATE_NONE);
            } else {
                if (this.mList instanceof ListView) {
                    ((ListView) this.mList).setSelectionFromTop(this.mHeaderCount + index, STATE_NONE);
                } else {
                    this.mList.setSelection(this.mHeaderCount + index);
                }
            }
            sectionIndex = -1;
        } else {
            int exactSection = MathUtils.constrain((int) (((float) sectionCount) * position), STATE_NONE, sectionCount - 1);
            int targetSection = exactSection;
            int targetIndex = this.mSectionIndexer.getPositionForSection(exactSection);
            sectionIndex = exactSection;
            int nextIndex = count;
            int prevIndex = targetIndex;
            int prevSection = exactSection;
            int nextSection = exactSection + THUMB_POSITION_INSIDE;
            if (exactSection < sectionCount - 1) {
                nextIndex = this.mSectionIndexer.getPositionForSection(exactSection + THUMB_POSITION_INSIDE);
            }
            if (nextIndex == targetIndex) {
                while (targetSection > 0) {
                    targetSection--;
                    prevIndex = this.mSectionIndexer.getPositionForSection(targetSection);
                    if (prevIndex == targetIndex) {
                        if (targetSection == 0) {
                            sectionIndex = STATE_NONE;
                            break;
                        }
                    }
                    prevSection = targetSection;
                    sectionIndex = targetSection;
                    break;
                }
            }
            int nextNextSection = nextSection + THUMB_POSITION_INSIDE;
            while (nextNextSection < sectionCount) {
                if (this.mSectionIndexer.getPositionForSection(nextNextSection) != nextIndex) {
                    break;
                }
                nextNextSection += THUMB_POSITION_INSIDE;
                nextSection += THUMB_POSITION_INSIDE;
            }
            float prevPosition = ((float) prevSection) / ((float) sectionCount);
            float nextPosition = ((float) nextSection) / ((float) sectionCount);
            float snapThreshold = count == 0 ? AutoScrollHelper.NO_MAX : 0.125f / ((float) count);
            if (prevSection != exactSection || position - prevPosition >= snapThreshold) {
                targetIndex = prevIndex + ((int) ((((float) (nextIndex - prevIndex)) * (position - prevPosition)) / (nextPosition - prevPosition)));
            } else {
                targetIndex = prevIndex;
            }
            targetIndex = MathUtils.constrain(targetIndex, STATE_NONE, count - 1);
            if (this.mList instanceof ExpandableListView) {
                expList = this.mList;
                expList.setSelectionFromTop(expList.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(this.mHeaderCount + targetIndex)), STATE_NONE);
            } else {
                if (this.mList instanceof ListView) {
                    ((ListView) this.mList).setSelectionFromTop(this.mHeaderCount + targetIndex, STATE_NONE);
                } else {
                    this.mList.setSelection(this.mHeaderCount + targetIndex);
                }
            }
        }
        int i = this.mCurrentSection;
        if (r0 != sectionIndex) {
            this.mCurrentSection = sectionIndex;
            boolean hasPreview = transitionPreviewLayout(sectionIndex);
            if (!this.mShowingPreview && hasPreview) {
                transitionToDragging();
            } else if (this.mShowingPreview && !hasPreview) {
                transitionToVisible();
            }
        }
    }

    private boolean transitionPreviewLayout(int sectionIndex) {
        TextView showing;
        View target;
        Object[] sections = this.mSections;
        CharSequence text = null;
        if (sections != null && sectionIndex >= 0 && sectionIndex < sections.length) {
            Object section = sections[sectionIndex];
            if (section != null) {
                text = section.toString();
            }
        }
        Rect bounds = this.mTempBounds;
        View preview = this.mPreviewImage;
        if (this.mShowingPrimary) {
            showing = this.mPrimaryText;
            target = this.mSecondaryText;
        } else {
            showing = this.mSecondaryText;
            target = this.mPrimaryText;
        }
        target.setText(text);
        measurePreview(target, bounds);
        applyLayout(target, bounds);
        if (this.mPreviewAnimation != null) {
            this.mPreviewAnimation.cancel();
        }
        Animator showTarget = animateAlpha(target, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL).setDuration(50);
        Animator hideShowing = animateAlpha(showing, 0.0f).setDuration(50);
        hideShowing.addListener(this.mSwitchPrimaryListener);
        bounds.left -= preview.getPaddingLeft();
        bounds.top -= preview.getPaddingTop();
        bounds.right += preview.getPaddingRight();
        bounds.bottom += preview.getPaddingBottom();
        Animator resizePreview = animateBounds(preview, bounds);
        resizePreview.setDuration(100);
        this.mPreviewAnimation = new AnimatorSet();
        Builder builder = this.mPreviewAnimation.play(hideShowing).with(showTarget);
        builder.with(resizePreview);
        int previewWidth = (preview.getWidth() - preview.getPaddingLeft()) - preview.getPaddingRight();
        int targetWidth = target.getWidth();
        if (targetWidth > previewWidth) {
            target.setScaleX(((float) previewWidth) / ((float) targetWidth));
            builder.with(animateScaleX(target, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL).setDuration(100));
        } else {
            target.setScaleX(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
        }
        int showingWidth = showing.getWidth();
        if (showingWidth > targetWidth) {
            builder.with(animateScaleX(showing, ((float) targetWidth) / ((float) showingWidth)).setDuration(100));
        }
        this.mPreviewAnimation.start();
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        return true;
    }

    protected void setThumbPos(float position) {
        float previewPos;
        float thumbMiddle = (this.mThumbRange * position) + this.mThumbOffset;
        this.mThumbImage.setTranslationY(thumbMiddle - (((float) this.mThumbImage.getHeight()) / 2.0f));
        View previewImage = this.mPreviewImage;
        float previewHalfHeight = ((float) previewImage.getHeight()) / 2.0f;
        switch (this.mOverlayPosition) {
            case THUMB_POSITION_INSIDE /*1*/:
                previewPos = thumbMiddle;
                break;
            case STATE_DRAGGING /*2*/:
                previewPos = thumbMiddle - previewHalfHeight;
                break;
            default:
                previewPos = 0.0f;
                break;
        }
        Rect container = this.mContainerRect;
        float previewTop = MathUtils.constrain(previewPos, ((float) container.top) + previewHalfHeight, ((float) container.bottom) - previewHalfHeight) - previewHalfHeight;
        previewImage.setTranslationY(previewTop);
        this.mPrimaryText.setTranslationY(previewTop);
        this.mSecondaryText.setTranslationY(previewTop);
    }

    private float getPosFromMotionEvent(float y) {
        if (this.mThumbRange <= 0.0f) {
            return 0.0f;
        }
        return MathUtils.constrain((y - this.mThumbOffset) / this.mThumbRange, 0.0f, (float) WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
    }

    private float getPosFromItemCount(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        SectionIndexer sectionIndexer = this.mSectionIndexer;
        if (sectionIndexer == null || this.mListAdapter == null) {
            getSectionsFromIndexer();
        }
        if (visibleItemCount == 0 || totalItemCount == 0) {
            return 0.0f;
        }
        boolean hasSections;
        if (sectionIndexer == null || this.mSections == null) {
            hasSections = false;
        } else {
            hasSections = this.mSections.length > 0;
        }
        if (hasSections && this.mMatchDragPosition) {
            firstVisibleItem -= this.mHeaderCount;
            if (firstVisibleItem < 0) {
                return 0.0f;
            }
            float incrementalPos;
            int positionsInSection;
            float posWithinSection;
            totalItemCount -= this.mHeaderCount;
            View child = this.mList.getChildAt(STATE_NONE);
            if (child == null || child.getHeight() == 0) {
                incrementalPos = 0.0f;
            } else {
                incrementalPos = ((float) (this.mList.getPaddingTop() - child.getTop())) / ((float) child.getHeight());
            }
            int section = sectionIndexer.getSectionForPosition(firstVisibleItem);
            int sectionPos = sectionIndexer.getPositionForSection(section);
            int sectionCount = this.mSections.length;
            if (section < sectionCount - 1) {
                int nextSectionPos;
                if (section + THUMB_POSITION_INSIDE < sectionCount) {
                    nextSectionPos = sectionIndexer.getPositionForSection(section + THUMB_POSITION_INSIDE);
                } else {
                    nextSectionPos = totalItemCount - 1;
                }
                positionsInSection = nextSectionPos - sectionPos;
            } else {
                positionsInSection = totalItemCount - sectionPos;
            }
            if (positionsInSection == 0) {
                posWithinSection = 0.0f;
            } else {
                float f = (float) positionsInSection;
                posWithinSection = ((((float) firstVisibleItem) + incrementalPos) - ((float) sectionPos)) / r0;
            }
            float result = (((float) section) + posWithinSection) / ((float) sectionCount);
            if (firstVisibleItem > 0 && firstVisibleItem + visibleItemCount == totalItemCount) {
                int maxSize;
                int currentVisibleSize;
                View lastChild = this.mList.getChildAt(visibleItemCount - 1);
                int bottomPadding = this.mList.getPaddingBottom();
                if (this.mList.getClipToPadding()) {
                    maxSize = lastChild.getHeight();
                    currentVisibleSize = (this.mList.getHeight() - bottomPadding) - lastChild.getTop();
                } else {
                    maxSize = lastChild.getHeight() + bottomPadding;
                    currentVisibleSize = this.mList.getHeight() - lastChild.getTop();
                }
                if (currentVisibleSize > 0 && maxSize > 0) {
                    result += (WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL - result) * (((float) currentVisibleSize) / ((float) maxSize));
                }
            }
            return result;
        } else if (visibleItemCount == totalItemCount) {
            return 0.0f;
        } else {
            return ((float) firstVisibleItem) / ((float) (totalItemCount - visibleItemCount));
        }
    }

    private void cancelFling() {
        MotionEvent cancelFling = MotionEvent.obtain(TAP_TIMEOUT, TAP_TIMEOUT, 3, 0.0f, 0.0f, STATE_NONE);
        this.mList.onTouchEvent(cancelFling);
        cancelFling.recycle();
    }

    private void cancelPendingDrag() {
        this.mPendingDrag = -1;
    }

    private void startPendingDrag() {
        this.mPendingDrag = SystemClock.uptimeMillis() + TAP_TIMEOUT;
    }

    private void beginDrag() {
        this.mPendingDrag = -1;
        setState(STATE_DRAGGING);
        if (this.mListAdapter == null && this.mList != null) {
            getSectionsFromIndexer();
        }
        if (this.mList != null) {
            this.mList.requestDisallowInterceptTouchEvent(true);
            this.mList.reportScrollStateChange(THUMB_POSITION_INSIDE);
        }
        cancelFling();
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }
        switch (ev.getActionMasked()) {
            case STATE_NONE /*0*/:
                if (isPointInside(ev.getX(), ev.getY())) {
                    if (this.mList.isInScrollingContainer()) {
                        this.mInitialTouchY = ev.getY();
                        startPendingDrag();
                        break;
                    }
                    return true;
                }
                break;
            case THUMB_POSITION_INSIDE /*1*/:
            case HwCfgFilePolicy.BASE /*3*/:
                cancelPendingDrag();
                break;
            case STATE_DRAGGING /*2*/:
                if (!isPointInside(ev.getX(), ev.getY())) {
                    cancelPendingDrag();
                    break;
                } else if (this.mPendingDrag >= TAP_TIMEOUT && this.mPendingDrag <= SystemClock.uptimeMillis()) {
                    beginDrag();
                    scrollTo(getPosFromMotionEvent(this.mInitialTouchY));
                    return onTouchEvent(ev);
                }
        }
        return false;
    }

    public boolean onInterceptHoverEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }
        int actionMasked = ev.getActionMasked();
        if ((actionMasked == 9 || actionMasked == 7) && this.mState == 0 && isPointInside(ev.getX(), ev.getY())) {
            setState(THUMB_POSITION_INSIDE);
            postAutoHide();
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent me) {
        if (!isEnabled()) {
            return false;
        }
        float pos;
        switch (me.getActionMasked()) {
            case STATE_NONE /*0*/:
                if (isPointInside(me.getX(), me.getY()) && !this.mList.isInScrollingContainer()) {
                    beginDrag();
                    return true;
                }
            case THUMB_POSITION_INSIDE /*1*/:
                if (this.mPendingDrag >= TAP_TIMEOUT) {
                    beginDrag();
                    pos = getPosFromMotionEvent(me.getY());
                    setThumbPos(pos);
                    scrollTo(pos);
                }
                if (this.mState == STATE_DRAGGING) {
                    if (this.mList != null) {
                        this.mList.requestDisallowInterceptTouchEvent(false);
                        this.mList.reportScrollStateChange(STATE_NONE);
                    }
                    setState(THUMB_POSITION_INSIDE);
                    postAutoHide();
                    return true;
                }
                break;
            case STATE_DRAGGING /*2*/:
                if (this.mPendingDrag >= TAP_TIMEOUT && Math.abs(me.getY() - this.mInitialTouchY) > ((float) this.mScaledTouchSlop)) {
                    beginDrag();
                }
                if (this.mState == STATE_DRAGGING) {
                    pos = getPosFromMotionEvent(me.getY());
                    setThumbPos(pos);
                    if (this.mScrollCompleted) {
                        scrollTo(pos);
                    }
                    return true;
                }
                break;
            case HwCfgFilePolicy.BASE /*3*/:
                cancelPendingDrag();
                break;
        }
        return false;
    }

    private boolean isPointInside(float x, float y) {
        if (isPointInsideX(x)) {
            return this.mTrackDrawable == null ? isPointInsideY(y) : true;
        } else {
            return false;
        }
    }

    private boolean isPointInsideX(float x) {
        boolean z = true;
        float offset = this.mThumbImage.getTranslationX();
        float targetSizeDiff = ((float) this.mMinimumTouchTarget) - ((((float) this.mThumbImage.getRight()) + offset) - (((float) this.mThumbImage.getLeft()) + offset));
        float adjust = targetSizeDiff > 0.0f ? targetSizeDiff : 0.0f;
        if (this.mLayoutFromRight) {
            if (x < ((float) this.mThumbImage.getLeft()) - adjust) {
                z = false;
            }
            return z;
        }
        if (x > ((float) this.mThumbImage.getRight()) + adjust) {
            z = false;
        }
        return z;
    }

    private boolean isPointInsideY(float y) {
        float adjust = 0.0f;
        float offset = this.mThumbImage.getTranslationY();
        float top = ((float) this.mThumbImage.getTop()) + offset;
        float bottom = ((float) this.mThumbImage.getBottom()) + offset;
        float targetSizeDiff = ((float) this.mMinimumTouchTarget) - (bottom - top);
        if (targetSizeDiff > 0.0f) {
            adjust = targetSizeDiff / 2.0f;
        }
        if (y < top - adjust || y > bottom + adjust) {
            return false;
        }
        return true;
    }

    private static Animator groupAnimatorOfFloat(Property<View, Float> property, float value, View... views) {
        AnimatorSet animSet = new AnimatorSet();
        Builder builder = null;
        for (int i = views.length - 1; i >= 0; i--) {
            Object obj = views[i];
            float[] fArr = new float[THUMB_POSITION_INSIDE];
            fArr[STATE_NONE] = value;
            Animator anim = ObjectAnimator.ofFloat(obj, property, fArr);
            if (builder == null) {
                builder = animSet.play(anim);
            } else {
                builder.with(anim);
            }
        }
        return animSet;
    }

    private static Animator animateScaleX(View v, float target) {
        Property property = View.SCALE_X;
        float[] fArr = new float[THUMB_POSITION_INSIDE];
        fArr[STATE_NONE] = target;
        return ObjectAnimator.ofFloat(v, property, fArr);
    }

    private static Animator animateAlpha(View v, float alpha) {
        Property property = View.ALPHA;
        float[] fArr = new float[THUMB_POSITION_INSIDE];
        fArr[STATE_NONE] = alpha;
        return ObjectAnimator.ofFloat(v, property, fArr);
    }

    private static Animator animateBounds(View v, Rect bounds) {
        Property property = LEFT;
        int[] iArr = new int[THUMB_POSITION_INSIDE];
        iArr[STATE_NONE] = bounds.left;
        PropertyValuesHolder left = PropertyValuesHolder.ofInt(property, iArr);
        property = TOP;
        iArr = new int[THUMB_POSITION_INSIDE];
        iArr[STATE_NONE] = bounds.top;
        PropertyValuesHolder top = PropertyValuesHolder.ofInt(property, iArr);
        property = RIGHT;
        iArr = new int[THUMB_POSITION_INSIDE];
        iArr[STATE_NONE] = bounds.right;
        PropertyValuesHolder right = PropertyValuesHolder.ofInt(property, iArr);
        property = BOTTOM;
        iArr = new int[THUMB_POSITION_INSIDE];
        iArr[STATE_NONE] = bounds.bottom;
        PropertyValuesHolder bottom = PropertyValuesHolder.ofInt(property, iArr);
        PropertyValuesHolder[] propertyValuesHolderArr = new PropertyValuesHolder[MIN_PAGES];
        propertyValuesHolderArr[STATE_NONE] = left;
        propertyValuesHolderArr[THUMB_POSITION_INSIDE] = top;
        propertyValuesHolderArr[STATE_DRAGGING] = right;
        propertyValuesHolderArr[3] = bottom;
        return ObjectAnimator.ofPropertyValuesHolder(v, propertyValuesHolderArr);
    }

    protected Rect getContainerRect() {
        return this.mContainerRect;
    }

    protected ImageView getThumbImage() {
        return this.mThumbImage;
    }

    protected ImageView getTrackImage() {
        return this.mTrackImage;
    }

    protected boolean getLayoutFromRight() {
        return this.mLayoutFromRight;
    }
}
