package android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.IntProperty;
import android.util.MathUtils;
import android.util.Property;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.ImageView;
import com.android.hwext.internal.R;

/* access modifiers changed from: package-private */
public class FastScroller {
    private static Property<View, Integer> BOTTOM = new IntProperty<View>("bottom") {
        /* class android.widget.FastScroller.AnonymousClass6 */

        public void setValue(View object, int value) {
            object.setBottom(value);
        }

        public Integer get(View object) {
            return Integer.valueOf(object.getBottom());
        }
    };
    private static final int DURATION_CROSS_FADE = 50;
    private static final int DURATION_FADE_IN = 150;
    private static final int DURATION_FADE_OUT = 300;
    private static final int DURATION_RESIZE = 100;
    private static final long FADE_TIMEOUT = 1500;
    private static Property<View, Integer> LEFT = new IntProperty<View>("left") {
        /* class android.widget.FastScroller.AnonymousClass3 */

        public void setValue(View object, int value) {
            object.setLeft(value);
        }

        public Integer get(View object) {
            return Integer.valueOf(object.getLeft());
        }
    };
    private static final int MIN_PAGES = 4;
    private static final int OVERLAY_ABOVE_THUMB = 2;
    private static final int OVERLAY_AT_THUMB = 1;
    private static final int OVERLAY_FLOATING = 0;
    private static final int PREVIEW_LEFT = 0;
    private static final int PREVIEW_RIGHT = 1;
    private static Property<View, Integer> RIGHT = new IntProperty<View>("right") {
        /* class android.widget.FastScroller.AnonymousClass5 */

        public void setValue(View object, int value) {
            object.setRight(value);
        }

        public Integer get(View object) {
            return Integer.valueOf(object.getRight());
        }
    };
    private static final int STATE_DRAGGING = 2;
    private static final int STATE_NONE = 0;
    private static final int STATE_VISIBLE = 1;
    private static final long TAP_TIMEOUT = ((long) ViewConfiguration.getTapTimeout());
    private static final int THUMB_POSITION_INSIDE = 1;
    private static final int THUMB_POSITION_MIDPOINT = 0;
    private static Property<View, Integer> TOP = new IntProperty<View>("top") {
        /* class android.widget.FastScroller.AnonymousClass4 */

        public void setValue(View object, int value) {
            object.setTop(value);
        }

        public Integer get(View object) {
            return Integer.valueOf(object.getTop());
        }
    };
    private boolean mAlwaysShow;
    @UnsupportedAppUsage
    private final Rect mContainerRect = new Rect();
    private int mCurrentSection = -1;
    private AnimatorSet mDecorAnimation;
    private final Runnable mDeferHide = new Runnable() {
        /* class android.widget.FastScroller.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            FastScroller.this.setState(0);
        }
    };
    private boolean mEnabled;
    private int mFirstVisibleItem;
    @UnsupportedAppUsage
    private int mHeaderCount;
    private float mInitialTouchY;
    private boolean mLayoutFromRight;
    private final AbsListView mList;
    private Adapter mListAdapter;
    @UnsupportedAppUsage
    private boolean mLongList;
    private boolean mMatchDragPosition;
    @UnsupportedAppUsage
    private final int mMinimumTouchTarget;
    private int mOldChildCount;
    private int mOldItemCount;
    private final ViewGroupOverlay mOverlay;
    private int mOverlayPosition;
    private long mPendingDrag = -1;
    private AnimatorSet mPreviewAnimation;
    private final View mPreviewImage;
    private int mPreviewMinHeight;
    private int mPreviewMinWidth;
    private int mPreviewPadding;
    private final int[] mPreviewResId = new int[2];
    private final TextView mPrimaryText;
    private int mScaledTouchSlop;
    private int mScrollBarStyle;
    private boolean mScrollCompleted;
    private int mScrollbarPosition = -1;
    private final TextView mSecondaryText;
    private SectionIndexer mSectionIndexer;
    private Object[] mSections;
    private boolean mShowingPreview;
    private boolean mShowingPrimary;
    private int mState;
    private final Animator.AnimatorListener mSwitchPrimaryListener = new AnimatorListenerAdapter() {
        /* class android.widget.FastScroller.AnonymousClass2 */

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            FastScroller fastScroller = FastScroller.this;
            fastScroller.mShowingPrimary = !fastScroller.mShowingPrimary;
        }
    };
    private final Rect mTempBounds = new Rect();
    private final Rect mTempMargins = new Rect();
    private int mTextAppearance;
    private ColorStateList mTextColor;
    private float mTextSize;
    @UnsupportedAppUsage
    private Drawable mThumbDrawable;
    @UnsupportedAppUsage
    private final ImageView mThumbImage;
    private int mThumbMinHeight;
    private int mThumbMinWidth;
    private float mThumbOffset;
    private int mThumbPosition;
    private float mThumbRange;
    @UnsupportedAppUsage
    private Drawable mTrackDrawable;
    @UnsupportedAppUsage
    private final ImageView mTrackImage;
    private boolean mUpdatingLayout;
    private int mWidth;

    @UnsupportedAppUsage
    public FastScroller(AbsListView listView, int styleResId) {
        this.mList = listView;
        this.mOldItemCount = listView.getCount();
        this.mOldChildCount = listView.getChildCount();
        Context context = listView.getContext();
        this.mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mScrollBarStyle = listView.getScrollBarStyle();
        boolean z = true;
        this.mScrollCompleted = true;
        this.mState = 1;
        this.mMatchDragPosition = context.getApplicationInfo().targetSdkVersion < 11 ? false : z;
        this.mTrackImage = new ImageView(context);
        this.mTrackImage.setScaleType(ImageView.ScaleType.FIT_XY);
        this.mThumbImage = new ImageView(context);
        this.mThumbImage.setScaleType(ImageView.ScaleType.FIT_XY);
        this.mPreviewImage = new View(context);
        this.mPreviewImage.setAlpha(0.0f);
        this.mPrimaryText = createPreviewTextView(context);
        this.mSecondaryText = createPreviewTextView(context);
        if (HwWidgetFactory.isHwTheme(context)) {
            this.mMinimumTouchTarget = listView.getResources().getDimensionPixelSize(R.dimen.emui_fast_scroller_minimum_touch_target);
        } else {
            this.mMinimumTouchTarget = listView.getResources().getDimensionPixelSize(com.android.internal.R.dimen.fast_scroller_minimum_touch_target);
        }
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
        int width = 0;
        this.mTrackImage.setImageDrawable(this.mTrackDrawable);
        Drawable drawable = this.mTrackDrawable;
        if (drawable != null) {
            width = Math.max(0, drawable.getIntrinsicWidth());
        }
        this.mThumbImage.setImageDrawable(this.mThumbDrawable);
        this.mThumbImage.setMinimumWidth(this.mThumbMinWidth);
        this.mThumbImage.setMinimumHeight(this.mThumbMinHeight);
        Drawable drawable2 = this.mThumbDrawable;
        if (drawable2 != null) {
            width = Math.max(width, drawable2.getIntrinsicWidth());
        }
        this.mWidth = Math.max(width, this.mThumbMinWidth);
        int i = this.mTextAppearance;
        if (i != 0) {
            this.mPrimaryText.setTextAppearance(i);
            this.mSecondaryText.setTextAppearance(this.mTextAppearance);
        }
        ColorStateList colorStateList = this.mTextColor;
        if (colorStateList != null) {
            this.mPrimaryText.setTextColor(colorStateList);
            this.mSecondaryText.setTextColor(this.mTextColor);
        }
        float f = this.mTextSize;
        if (f > 0.0f) {
            this.mPrimaryText.setTextSize(0, f);
            this.mSecondaryText.setTextSize(0, this.mTextSize);
        }
        int padding = this.mPreviewPadding;
        this.mPrimaryText.setIncludeFontPadding(false);
        this.mPrimaryText.setPadding(padding, padding, padding, padding);
        this.mSecondaryText.setIncludeFontPadding(false);
        this.mSecondaryText.setPadding(padding, padding, padding, padding);
        refreshDrawablePressedState();
    }

    public void setStyle(int resId) {
        TypedArray ta = this.mList.getContext().obtainStyledAttributes(null, com.android.internal.R.styleable.FastScroll, 16843767, resId);
        int N = ta.getIndexCount();
        for (int i = 0; i < N; i++) {
            int index = ta.getIndex(i);
            switch (index) {
                case 0:
                    this.mTextAppearance = ta.getResourceId(index, 0);
                    break;
                case 1:
                    this.mTextSize = (float) ta.getDimensionPixelSize(index, 0);
                    break;
                case 2:
                    this.mTextColor = ta.getColorStateList(index);
                    break;
                case 3:
                    this.mPreviewPadding = ta.getDimensionPixelSize(index, 0);
                    break;
                case 4:
                    this.mPreviewMinWidth = ta.getDimensionPixelSize(index, 0);
                    break;
                case 5:
                    this.mPreviewMinHeight = ta.getDimensionPixelSize(index, 0);
                    break;
                case 6:
                    this.mThumbPosition = ta.getInt(index, 0);
                    break;
                case 7:
                    this.mPreviewResId[0] = ta.getResourceId(index, 0);
                    break;
                case 8:
                    this.mPreviewResId[1] = ta.getResourceId(index, 0);
                    break;
                case 9:
                    this.mOverlayPosition = ta.getInt(index, 0);
                    break;
                case 10:
                    this.mThumbDrawable = ta.getDrawable(index);
                    break;
                case 11:
                    this.mThumbMinHeight = ta.getDimensionPixelSize(index, 0);
                    break;
                case 12:
                    this.mThumbMinWidth = ta.getDimensionPixelSize(index, 0);
                    break;
                case 13:
                    this.mTrackDrawable = ta.getDrawable(index);
                    break;
            }
        }
        ta.recycle();
        updateAppearance();
    }

    @UnsupportedAppUsage
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
        return this.mEnabled && (this.mLongList || this.mAlwaysShow);
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
            setState(1);
        } else if (this.mState == 1) {
            postAutoHide();
        } else if (peekIfEnabled) {
            setState(1);
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
        setState(0);
    }

    public void setScrollbarPosition(int position) {
        boolean z = true;
        if (position == 0) {
            position = this.mList.isLayoutRtl() ? 1 : 2;
        }
        if (this.mScrollbarPosition != position) {
            this.mScrollbarPosition = position;
            if (position == 1) {
                z = false;
            }
            this.mLayoutFromRight = z;
            this.mPreviewImage.setBackgroundResource(this.mPreviewResId[this.mLayoutFromRight ? 1 : 0]);
            int textMinWidth = Math.max(0, (this.mPreviewMinWidth - this.mPreviewImage.getPaddingLeft()) - this.mPreviewImage.getPaddingRight());
            this.mPrimaryText.setMinimumWidth(textMinWidth);
            this.mSecondaryText.setMinimumWidth(textMinWidth);
            int textMinHeight = Math.max(0, (this.mPreviewMinHeight - this.mPreviewImage.getPaddingTop()) - this.mPreviewImage.getPaddingBottom());
            this.mPrimaryText.setMinimumHeight(textMinHeight);
            this.mSecondaryText.setMinimumHeight(textMinHeight);
            updateLayout();
        }
    }

    public int getWidth() {
        return this.mWidth;
    }

    @UnsupportedAppUsage
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateLayout();
    }

    public void onItemCountChanged(int childCount, int itemCount) {
        if (this.mOldItemCount != itemCount || this.mOldChildCount != childCount) {
            this.mOldItemCount = itemCount;
            this.mOldChildCount = childCount;
            if ((itemCount - childCount > 0) && this.mState != 2) {
                setThumbPos(getPosFromItemCount(this.mList.getFirstVisiblePosition(), childCount, itemCount));
            }
            updateLongList(childCount, itemCount);
        }
    }

    private void updateLongList(int childCount, int itemCount) {
        boolean longList = childCount > 0 && itemCount / childCount >= 4;
        if (this.mLongList != longList) {
            this.mLongList = longList;
            onStateDependencyChanged(false);
        }
    }

    private TextView createPreviewTextView(Context context) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-2, -2);
        TextView textView = new TextView(context);
        textView.setLayoutParams(params);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
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
        view.setPivotX(this.mLayoutFromRight ? (float) (bounds.right - bounds.left) : 0.0f);
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

    /* access modifiers changed from: protected */
    public void measureViewToSide(View view, View adjacent, Rect margins, Rect out) {
        int marginRight;
        int marginTop;
        int marginLeft;
        int maxWidth;
        int left;
        int right;
        if (margins == null) {
            marginLeft = 0;
            marginTop = 0;
            marginRight = 0;
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
        int adjMaxHeight = Math.max(0, container.height());
        int adjMaxWidth = Math.max(0, (maxWidth - marginLeft) - marginRight);
        view.measure(View.MeasureSpec.makeMeasureSpec(adjMaxWidth, Integer.MIN_VALUE), View.MeasureSpec.makeSafeMeasureSpec(adjMaxHeight, 0));
        int width = Math.min(adjMaxWidth, view.getMeasuredWidth());
        if (this.mLayoutFromRight) {
            right = (adjacent == null ? container.right : adjacent.getLeft()) - marginRight;
            left = right - width;
        } else {
            left = (adjacent == null ? container.left : adjacent.getRight()) + marginLeft;
            right = left + width;
        }
        out.set(left, marginTop, right, marginTop + view.getMeasuredHeight());
    }

    /* JADX INFO: Multiple debug info for r1v1 int: [D('right' int), D('marginLeft' int)] */
    private void measureFloating(View preview, Rect margins, Rect out) {
        int marginRight;
        int marginTop;
        int marginLeft;
        if (margins == null) {
            marginLeft = 0;
            marginTop = 0;
            marginRight = 0;
        } else {
            marginLeft = margins.left;
            marginTop = margins.top;
            marginRight = margins.right;
        }
        Rect container = this.mContainerRect;
        int containerWidth = container.width();
        preview.measure(View.MeasureSpec.makeMeasureSpec(Math.max(0, (containerWidth - marginLeft) - marginRight), Integer.MIN_VALUE), View.MeasureSpec.makeSafeMeasureSpec(Math.max(0, container.height()), 0));
        int containerHeight = container.height();
        int width = preview.getMeasuredWidth();
        int top = (containerHeight / 10) + marginTop + container.top;
        int left = ((containerWidth - width) / 2) + container.left;
        out.set(left, top, left + width, preview.getMeasuredHeight() + top);
    }

    private void updateContainerRect() {
        AbsListView list = this.mList;
        list.resolvePadding();
        Rect container = this.mContainerRect;
        container.left = 0;
        container.top = 0;
        container.right = list.getWidth();
        container.bottom = list.getHeight();
        int scrollbarStyle = this.mScrollBarStyle;
        if (scrollbarStyle == 16777216 || scrollbarStyle == 0) {
            container.left += list.getPaddingLeft();
            container.top += list.getPaddingTop();
            container.right -= list.getPaddingRight();
            container.bottom -= list.getPaddingBottom();
            if (scrollbarStyle == 16777216) {
                int width = getWidth();
                if (this.mScrollbarPosition == 2) {
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
        int thumbHalfHeight;
        View track = this.mTrackImage;
        View thumb = this.mThumbImage;
        Rect container = this.mContainerRect;
        track.measure(View.MeasureSpec.makeMeasureSpec(Math.max(0, container.width()), Integer.MIN_VALUE), View.MeasureSpec.makeSafeMeasureSpec(Math.max(0, container.height()), 0));
        if (this.mThumbPosition == 1) {
            thumbHalfHeight = container.top;
            top = container.bottom;
        } else {
            int thumbHalfHeight2 = thumb.getHeight() / 2;
            int top2 = container.top + thumbHalfHeight2;
            top = container.bottom - thumbHalfHeight2;
            thumbHalfHeight = top2;
        }
        int trackWidth = track.getMeasuredWidth();
        int left = thumb.getLeft() + ((thumb.getWidth() - trackWidth) / 2);
        track.layout(left, thumbHalfHeight, left + trackWidth, top);
    }

    private void updateOffsetAndRange() {
        float max;
        float min;
        View trackImage = this.mTrackImage;
        View thumbImage = this.mThumbImage;
        if (this.mThumbPosition == 1) {
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void setState(int state) {
        this.mList.removeCallbacks(this.mDeferHide);
        if (this.mAlwaysShow && state == 0) {
            state = 1;
        }
        if (state != this.mState) {
            if (state == 0) {
                transitionToHidden();
            } else if (state == 1) {
                transitionToVisible();
            } else if (state == 2) {
                if (transitionPreviewLayout(this.mCurrentSection)) {
                    transitionToDragging();
                } else {
                    transitionToVisible();
                }
            }
            this.mState = state;
            refreshDrawablePressedState();
        }
    }

    private void refreshDrawablePressedState() {
        boolean isPressed = this.mState == 2;
        this.mThumbImage.setPressed(isPressed);
        this.mTrackImage.setPressed(isPressed);
    }

    private void transitionToHidden() {
        AnimatorSet animatorSet = this.mDecorAnimation;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        Animator fadeOut = groupAnimatorOfFloat(View.ALPHA, 0.0f, this.mThumbImage, this.mTrackImage, this.mPreviewImage, this.mPrimaryText, this.mSecondaryText).setDuration(300);
        Animator slideOut = groupAnimatorOfFloat(View.TRANSLATION_X, (float) (this.mLayoutFromRight ? this.mThumbImage.getWidth() : -this.mThumbImage.getWidth()), this.mThumbImage, this.mTrackImage).setDuration(300);
        this.mDecorAnimation = new AnimatorSet();
        this.mDecorAnimation.playTogether(fadeOut, slideOut);
        this.mDecorAnimation.start();
        this.mShowingPreview = false;
    }

    private void transitionToVisible() {
        AnimatorSet animatorSet = this.mDecorAnimation;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        Animator fadeIn = groupAnimatorOfFloat(View.ALPHA, 1.0f, this.mThumbImage, this.mTrackImage).setDuration(150);
        Animator fadeOut = groupAnimatorOfFloat(View.ALPHA, 0.0f, this.mPreviewImage, this.mPrimaryText, this.mSecondaryText).setDuration(300);
        Animator slideIn = groupAnimatorOfFloat(View.TRANSLATION_X, 0.0f, this.mThumbImage, this.mTrackImage).setDuration(150);
        this.mDecorAnimation = new AnimatorSet();
        this.mDecorAnimation.playTogether(fadeIn, fadeOut, slideIn);
        this.mDecorAnimation.start();
        this.mShowingPreview = false;
    }

    private void transitionToDragging() {
        AnimatorSet animatorSet = this.mDecorAnimation;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        Animator fadeIn = groupAnimatorOfFloat(View.ALPHA, 1.0f, this.mThumbImage, this.mTrackImage, this.mPreviewImage).setDuration(150);
        Animator slideIn = groupAnimatorOfFloat(View.TRANSLATION_X, 0.0f, this.mThumbImage, this.mTrackImage).setDuration(150);
        this.mDecorAnimation = new AnimatorSet();
        this.mDecorAnimation.playTogether(fadeIn, slideIn);
        this.mDecorAnimation.start();
        this.mShowingPreview = true;
    }

    private void postAutoHide() {
        this.mList.removeCallbacks(this.mDeferHide);
        this.mList.postDelayed(this.mDeferHide, FADE_TIMEOUT);
    }

    public void onScroll(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean hasMoreItems = false;
        if (!isEnabled()) {
            setState(0);
            return;
        }
        if (totalItemCount - visibleItemCount > 0) {
            hasMoreItems = true;
        }
        if (hasMoreItems && this.mState != 2) {
            setThumbPos(getPosFromItemCount(firstVisibleItem, visibleItemCount, totalItemCount));
        }
        this.mScrollCompleted = true;
        if (this.mFirstVisibleItem != firstVisibleItem) {
            this.mFirstVisibleItem = firstVisibleItem;
            if (this.mState != 2) {
                setState(1);
                postAutoHide();
            }
        }
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
        int targetIndex;
        this.mScrollCompleted = false;
        int count = this.mList.getCount();
        Object[] sections = this.mSections;
        int sectionCount = sections == null ? 0 : sections.length;
        if (sections == null || sectionCount <= 1) {
            int index = MathUtils.constrain((int) (((float) count) * position), 0, count - 1);
            AbsListView absListView = this.mList;
            if (absListView instanceof ExpandableListView) {
                ExpandableListView expList = (ExpandableListView) absListView;
                expList.setSelectionFromTop(expList.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(this.mHeaderCount + index)), 0);
            } else if (absListView instanceof ListView) {
                ((ListView) absListView).setSelectionFromTop(this.mHeaderCount + index, 0);
            } else {
                absListView.setSelection(this.mHeaderCount + index);
            }
            sectionIndex = -1;
        } else {
            int exactSection = MathUtils.constrain((int) (((float) sectionCount) * position), 0, sectionCount - 1);
            int targetSection = exactSection;
            int targetIndex2 = this.mSectionIndexer.getPositionForSection(targetSection);
            sectionIndex = targetSection;
            int nextIndex = count;
            int prevIndex = targetIndex2;
            int prevSection = targetSection;
            int nextSection = targetSection + 1;
            if (targetSection < sectionCount - 1) {
                nextIndex = this.mSectionIndexer.getPositionForSection(targetSection + 1);
            }
            if (nextIndex == targetIndex2) {
                while (true) {
                    if (targetSection > 0) {
                        targetSection--;
                        prevIndex = this.mSectionIndexer.getPositionForSection(targetSection);
                        if (prevIndex == targetIndex2) {
                            if (targetSection == 0) {
                                sectionIndex = 0;
                                break;
                            }
                        } else {
                            prevSection = targetSection;
                            sectionIndex = targetSection;
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            int nextNextSection = nextSection + 1;
            while (nextNextSection < sectionCount && this.mSectionIndexer.getPositionForSection(nextNextSection) == nextIndex) {
                nextNextSection++;
                nextSection++;
            }
            float prevPosition = ((float) prevSection) / ((float) sectionCount);
            float nextPosition = ((float) nextSection) / ((float) sectionCount);
            float snapThreshold = count == 0 ? Float.MAX_VALUE : 0.125f / ((float) count);
            if (prevSection != exactSection || position - prevPosition >= snapThreshold) {
                targetIndex = ((int) ((((float) (nextIndex - prevIndex)) * (position - prevPosition)) / (nextPosition - prevPosition))) + prevIndex;
            } else {
                targetIndex = prevIndex;
            }
            int targetIndex3 = MathUtils.constrain(targetIndex, 0, count - 1);
            AbsListView absListView2 = this.mList;
            if (absListView2 instanceof ExpandableListView) {
                ExpandableListView expList2 = (ExpandableListView) absListView2;
                expList2.setSelectionFromTop(expList2.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(this.mHeaderCount + targetIndex3)), 0);
            } else if (absListView2 instanceof ListView) {
                ((ListView) absListView2).setSelectionFromTop(this.mHeaderCount + targetIndex3, 0);
            } else {
                absListView2.setSelection(this.mHeaderCount + targetIndex3);
            }
        }
        if (this.mCurrentSection != sectionIndex) {
            this.mCurrentSection = sectionIndex;
            boolean hasPreview = transitionPreviewLayout(sectionIndex);
            if (!this.mShowingPreview && hasPreview) {
                transitionToDragging();
            } else if (this.mShowingPreview && !hasPreview) {
                transitionToVisible();
            }
        }
    }

    /* JADX INFO: Multiple debug info for r4v0 android.graphics.Rect: [D('bounds' android.graphics.Rect), D('section' java.lang.Object)] */
    private boolean transitionPreviewLayout(int sectionIndex) {
        TextView target;
        TextView showing;
        Object section;
        Object[] sections = this.mSections;
        String text = null;
        if (sections != null && sectionIndex >= 0 && sectionIndex < sections.length && (section = sections[sectionIndex]) != null) {
            text = section.toString();
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
        AnimatorSet animatorSet = this.mPreviewAnimation;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        Animator showTarget = animateAlpha(target, 1.0f).setDuration(50);
        Animator hideShowing = animateAlpha(showing, 0.0f).setDuration(50);
        hideShowing.addListener(this.mSwitchPrimaryListener);
        bounds.left -= preview.getPaddingLeft();
        bounds.top -= preview.getPaddingTop();
        bounds.right += preview.getPaddingRight();
        bounds.bottom += preview.getPaddingBottom();
        Animator resizePreview = animateBounds(preview, bounds);
        resizePreview.setDuration(100);
        this.mPreviewAnimation = new AnimatorSet();
        AnimatorSet.Builder builder = this.mPreviewAnimation.play(hideShowing).with(showTarget);
        builder.with(resizePreview);
        int previewWidth = (preview.getWidth() - preview.getPaddingLeft()) - preview.getPaddingRight();
        int targetWidth = target.getWidth();
        if (targetWidth > previewWidth) {
            target.setScaleX(((float) previewWidth) / ((float) targetWidth));
            builder.with(animateScaleX(target, 1.0f).setDuration(100));
        } else {
            target.setScaleX(1.0f);
        }
        int showingWidth = showing.getWidth();
        if (showingWidth > targetWidth) {
            builder.with(animateScaleX(showing, ((float) targetWidth) / ((float) showingWidth)).setDuration(100));
        }
        this.mPreviewAnimation.start();
        return !TextUtils.isEmpty(text);
    }

    /* access modifiers changed from: protected */
    public void setThumbPos(float position) {
        float previewPos;
        float thumbMiddle = (this.mThumbRange * position) + this.mThumbOffset;
        ImageView imageView = this.mThumbImage;
        imageView.setTranslationY(thumbMiddle - (((float) imageView.getHeight()) / 2.0f));
        View previewImage = this.mPreviewImage;
        float previewHalfHeight = ((float) previewImage.getHeight()) / 2.0f;
        int i = this.mOverlayPosition;
        if (i == 1) {
            previewPos = thumbMiddle;
        } else if (i != 2) {
            previewPos = 0.0f;
        } else {
            previewPos = thumbMiddle - previewHalfHeight;
        }
        Rect container = this.mContainerRect;
        float previewTop = MathUtils.constrain(previewPos, ((float) container.top) + previewHalfHeight, ((float) container.bottom) - previewHalfHeight) - previewHalfHeight;
        previewImage.setTranslationY(previewTop);
        this.mPrimaryText.setTranslationY(previewTop);
        this.mSecondaryText.setTranslationY(previewTop);
    }

    private float getPosFromMotionEvent(float y) {
        float f = this.mThumbRange;
        if (f <= 0.0f) {
            return 0.0f;
        }
        return MathUtils.constrain((y - this.mThumbOffset) / f, 0.0f, 1.0f);
    }

    private float getPosFromItemCount(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        float incrementalPos;
        int nextSectionPos;
        float posWithinSection;
        int currentVisibleSize;
        int maxSize;
        int nextSectionPos2;
        Object[] objArr;
        SectionIndexer sectionIndexer = this.mSectionIndexer;
        if (sectionIndexer == null || this.mListAdapter == null) {
            getSectionsFromIndexer();
        }
        if (visibleItemCount == 0 || totalItemCount == 0) {
            return 0.0f;
        }
        if (((sectionIndexer == null || (objArr = this.mSections) == null || objArr.length <= 0) ? false : true) && this.mMatchDragPosition) {
            int i = this.mHeaderCount;
            int firstVisibleItem2 = firstVisibleItem - i;
            if (firstVisibleItem2 < 0) {
                return 0.0f;
            }
            int totalItemCount2 = totalItemCount - i;
            View child = this.mList.getChildAt(0);
            if (child == null || child.getHeight() == 0) {
                incrementalPos = 0.0f;
            } else {
                incrementalPos = ((float) (this.mList.getPaddingTop() - child.getTop())) / ((float) child.getHeight());
            }
            int section = sectionIndexer.getSectionForPosition(firstVisibleItem2);
            int sectionPos = sectionIndexer.getPositionForSection(section);
            int sectionCount = this.mSections.length;
            if (section < sectionCount - 1) {
                if (section + 1 < sectionCount) {
                    nextSectionPos2 = sectionIndexer.getPositionForSection(section + 1);
                } else {
                    nextSectionPos2 = totalItemCount2 - 1;
                }
                nextSectionPos = nextSectionPos2 - sectionPos;
            } else {
                nextSectionPos = totalItemCount2 - sectionPos;
            }
            if (nextSectionPos == 0) {
                posWithinSection = 0.0f;
            } else {
                posWithinSection = ((((float) firstVisibleItem2) + incrementalPos) - ((float) sectionPos)) / ((float) nextSectionPos);
            }
            float result = (((float) section) + posWithinSection) / ((float) sectionCount);
            if (firstVisibleItem2 <= 0 || firstVisibleItem2 + visibleItemCount != totalItemCount2) {
                return result;
            }
            View lastChild = this.mList.getChildAt(visibleItemCount - 1);
            int bottomPadding = this.mList.getPaddingBottom();
            if (this.mList.getClipToPadding()) {
                int maxSize2 = lastChild.getHeight();
                currentVisibleSize = (this.mList.getHeight() - bottomPadding) - lastChild.getTop();
                maxSize = maxSize2;
            } else {
                int maxSize3 = lastChild.getHeight() + bottomPadding;
                currentVisibleSize = this.mList.getHeight() - lastChild.getTop();
                maxSize = maxSize3;
            }
            if (currentVisibleSize <= 0 || maxSize <= 0) {
                return result;
            }
            return result + ((1.0f - result) * (((float) currentVisibleSize) / ((float) maxSize)));
        } else if (visibleItemCount == totalItemCount) {
            return 0.0f;
        } else {
            return ((float) firstVisibleItem) / ((float) (totalItemCount - visibleItemCount));
        }
    }

    private void cancelFling() {
        MotionEvent cancelFling = MotionEvent.obtain(0, 0, 3, 0.0f, 0.0f, 0);
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
        setState(2);
        if (this.mListAdapter == null && this.mList != null) {
            getSectionsFromIndexer();
        }
        AbsListView absListView = this.mList;
        if (absListView != null) {
            absListView.requestDisallowInterceptTouchEvent(true);
            this.mList.reportScrollStateChange(1);
        }
        cancelFling();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0015, code lost:
        if (r0 != 3) goto L_0x006f;
     */
    @UnsupportedAppUsage
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }
        int actionMasked = ev.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    if (!isPointInside(ev.getX(), ev.getY())) {
                        cancelPendingDrag();
                    } else {
                        long j = this.mPendingDrag;
                        if (j >= 0 && j <= SystemClock.uptimeMillis()) {
                            beginDrag();
                            scrollTo(getPosFromMotionEvent(this.mInitialTouchY));
                            return onTouchEvent(ev);
                        }
                    }
                }
            }
            cancelPendingDrag();
        } else if (isPointInside(ev.getX(), ev.getY())) {
            if (!this.mList.isInScrollingContainer()) {
                return true;
            }
            this.mInitialTouchY = ev.getY();
            startPendingDrag();
        }
        return false;
    }

    public boolean onInterceptHoverEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }
        int actionMasked = ev.getActionMasked();
        if ((actionMasked == 9 || actionMasked == 7) && this.mState == 0 && isPointInside(ev.getX(), ev.getY())) {
            setState(1);
            postAutoHide();
        }
        return false;
    }

    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        if (this.mState == 2 || isPointInside(event.getX(), event.getY())) {
            return PointerIcon.getSystemIcon(this.mList.getContext(), 1000);
        }
        return null;
    }

    @UnsupportedAppUsage
    public boolean onTouchEvent(MotionEvent me) {
        if (!isEnabled()) {
            return false;
        }
        int actionMasked = me.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked == 1) {
                if (this.mPendingDrag >= 0) {
                    beginDrag();
                    float pos = getPosFromMotionEvent(me.getY());
                    setThumbPos(pos);
                    scrollTo(pos);
                }
                if (this.mState == 2) {
                    AbsListView absListView = this.mList;
                    if (absListView != null) {
                        absListView.requestDisallowInterceptTouchEvent(false);
                        this.mList.reportScrollStateChange(0);
                    }
                    setState(1);
                    postAutoHide();
                    return true;
                }
            } else if (actionMasked == 2) {
                if (this.mPendingDrag >= 0 && Math.abs(me.getY() - this.mInitialTouchY) > ((float) this.mScaledTouchSlop)) {
                    beginDrag();
                }
                if (this.mState == 2) {
                    float pos2 = getPosFromMotionEvent(me.getY());
                    setThumbPos(pos2);
                    if (this.mScrollCompleted) {
                        scrollTo(pos2);
                    }
                    return true;
                }
            } else if (actionMasked == 3) {
                cancelPendingDrag();
            }
        } else if (isPointInside(me.getX(), me.getY()) && !this.mList.isInScrollingContainer()) {
            beginDrag();
            return true;
        }
        return false;
    }

    private boolean isPointInside(float x, float y) {
        return isPointInsideX(x) && (this.mTrackDrawable != null || isPointInsideY(y));
    }

    private boolean isPointInsideX(float x) {
        float offset = this.mThumbImage.getTranslationX();
        float targetSizeDiff = ((float) this.mMinimumTouchTarget) - ((((float) this.mThumbImage.getRight()) + offset) - (((float) this.mThumbImage.getLeft()) + offset));
        float adjust = 0.0f;
        if (targetSizeDiff > 0.0f) {
            adjust = targetSizeDiff;
        }
        return this.mLayoutFromRight ? x >= ((float) this.mThumbImage.getLeft()) - adjust : x <= ((float) this.mThumbImage.getRight()) + adjust;
    }

    private boolean isPointInsideY(float y) {
        float offset = this.mThumbImage.getTranslationY();
        float top = ((float) this.mThumbImage.getTop()) + offset;
        float bottom = ((float) this.mThumbImage.getBottom()) + offset;
        float targetSizeDiff = ((float) this.mMinimumTouchTarget) - (bottom - top);
        float adjust = 0.0f;
        if (targetSizeDiff > 0.0f) {
            adjust = targetSizeDiff / 2.0f;
        }
        return y >= top - adjust && y <= bottom + adjust;
    }

    private static Animator groupAnimatorOfFloat(Property<View, Float> property, float value, View... views) {
        AnimatorSet animSet = new AnimatorSet();
        AnimatorSet.Builder builder = null;
        for (int i = views.length - 1; i >= 0; i--) {
            Animator anim = ObjectAnimator.ofFloat(views[i], property, value);
            if (builder == null) {
                builder = animSet.play(anim);
            } else {
                builder.with(anim);
            }
        }
        return animSet;
    }

    private static Animator animateScaleX(View v, float target) {
        return ObjectAnimator.ofFloat(v, View.SCALE_X, target);
    }

    private static Animator animateAlpha(View v, float alpha) {
        return ObjectAnimator.ofFloat(v, View.ALPHA, alpha);
    }

    private static Animator animateBounds(View v, Rect bounds) {
        return ObjectAnimator.ofPropertyValuesHolder(v, PropertyValuesHolder.ofInt(LEFT, bounds.left), PropertyValuesHolder.ofInt(TOP, bounds.top), PropertyValuesHolder.ofInt(RIGHT, bounds.right), PropertyValuesHolder.ofInt(BOTTOM, bounds.bottom));
    }

    /* access modifiers changed from: protected */
    public Rect getContainerRect() {
        return this.mContainerRect;
    }

    /* access modifiers changed from: protected */
    public ImageView getThumbImage() {
        return this.mThumbImage;
    }

    /* access modifiers changed from: protected */
    public ImageView getTrackImage() {
        return this.mTrackImage;
    }

    /* access modifiers changed from: protected */
    public boolean getLayoutFromRight() {
        return this.mLayoutFromRight;
    }
}
