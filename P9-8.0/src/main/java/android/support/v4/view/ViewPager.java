package android.support.v4.view;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.ClassLoaderCreator;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.interfaces.HwControlFactory;
import android.support.v4.interfaces.HwControlFactory.HwViewPager;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.widget.EdgeEffect;
import android.widget.Scroller;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ViewPager extends ViewGroup {
    private static final int CLOSE_ENOUGH = 2;
    private static final Comparator<ItemInfo> COMPARATOR = new Comparator<ItemInfo>() {
        public int compare(ItemInfo lhs, ItemInfo rhs) {
            return lhs.position - rhs.position;
        }
    };
    private static final boolean DEBUG = false;
    private static final int DEFAULT_GUTTER_SIZE = 16;
    private static final int DEFAULT_OFFSCREEN_PAGES = 1;
    private static final int DRAW_ORDER_DEFAULT = 0;
    private static final int DRAW_ORDER_FORWARD = 1;
    private static final int DRAW_ORDER_REVERSE = 2;
    private static final int INVALID_POINTER = -1;
    static final int[] LAYOUT_ATTRS = new int[]{16842931};
    private static final int MAX_SETTLE_DURATION = 600;
    private static final int MIN_DISTANCE_FOR_FLING = 25;
    private static final int MIN_FLING_VELOCITY = 400;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_SETTLING = 2;
    private static final String TAG = "ViewPager";
    private static final boolean USE_CACHE = false;
    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return ((((t * t) * t) * t) * t) + 1.0f;
        }
    };
    private static final ViewPositionComparator sPositionComparator = new ViewPositionComparator();
    private boolean isLayoutRtl = DEBUG;
    private int mActivePointerId = -1;
    PagerAdapter mAdapter;
    private List<OnAdapterChangeListener> mAdapterChangeListeners;
    private int mBottomPageBounds;
    private boolean mCalledSuper;
    private int mChildHeightMeasureSpec;
    private int mChildWidthMeasureSpec;
    private int mCloseEnough;
    int mCurItem;
    private int mDecorChildCount;
    private int mDefaultGutterSize;
    private int mDrawingOrder;
    private ArrayList<View> mDrawingOrderedChildren;
    private final Runnable mEndScrollRunnable = new Runnable() {
        public void run() {
            ViewPager.this.setScrollState(0);
            ViewPager.this.populate();
        }
    };
    private int mExpectedAdapterCount;
    private long mFakeDragBeginTime;
    private boolean mFakeDragging;
    private boolean mFirstLayout = true;
    private float mFirstOffset = -3.4028235E38f;
    private int mFlingDistance;
    private int mGutterSize;
    private HwViewPager mHwViewPager;
    private boolean mInLayout;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private OnPageChangeListener mInternalPageChangeListener;
    private boolean mIsBeingDragged;
    private boolean mIsScrollStarted;
    private boolean mIsUnableToDrag;
    private final ArrayList<ItemInfo> mItems = new ArrayList();
    private float mLastMotionX;
    private float mLastMotionY;
    private float mLastOffset = Float.MAX_VALUE;
    private EdgeEffect mLeftEdge;
    private Drawable mMarginDrawable;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    private boolean mNeedCalculatePageOffsets = DEBUG;
    private PagerObserver mObserver;
    private int mOffscreenPageLimit = 1;
    private OnPageChangeListener mOnPageChangeListener;
    private List<OnPageChangeListener> mOnPageChangeListeners;
    private int mPageMargin;
    private PageTransformer mPageTransformer;
    private int mPageTransformerLayerType;
    private boolean mPopulatePending;
    private Parcelable mRestoredAdapterState = null;
    private ClassLoader mRestoredClassLoader = null;
    private int mRestoredCurItem = -1;
    private EdgeEffect mRightEdge;
    private int mScrollState = 0;
    private Scroller mScroller;
    private boolean mScrollingCacheEnabled;
    private final ItemInfo mTempItem = new ItemInfo();
    private final Rect mTempRect = new Rect();
    private int mTopPageBounds;
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;

    public interface OnPageChangeListener {
        void onPageScrollStateChanged(int i);

        void onPageScrolled(int i, float f, int i2);

        void onPageSelected(int i);
    }

    public interface OnAdapterChangeListener {
        void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter pagerAdapter, @Nullable PagerAdapter pagerAdapter2);
    }

    @Inherited
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DecorView {
    }

    static class ItemInfo {
        Object object;
        float offset;
        int position;
        boolean scrolling;
        float widthFactor;

        ItemInfo() {
        }
    }

    public static class LayoutParams extends android.view.ViewGroup.LayoutParams {
        int childIndex;
        public int gravity;
        public boolean isDecor;
        boolean needsMeasure;
        int position;
        float widthFactor = 0.0f;

        public LayoutParams() {
            super(-1, -1);
        }

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, ViewPager.LAYOUT_ATTRS);
            this.gravity = a.getInteger(0, 48);
            a.recycle();
        }
    }

    class MyAccessibilityDelegate extends AccessibilityDelegateCompat {
        MyAccessibilityDelegate() {
        }

        public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(host, event);
            event.setClassName(ViewPager.class.getName());
            event.setScrollable(canScroll());
            if (event.getEventType() == 4096 && ViewPager.this.mAdapter != null) {
                event.setItemCount(ViewPager.this.mAdapter.getCount());
                event.setFromIndex(ViewPager.this.mCurItem);
                event.setToIndex(ViewPager.this.mCurItem);
            }
        }

        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.setClassName(ViewPager.class.getName());
            info.setScrollable(canScroll());
            if (ViewPager.this.canScrollHorizontally(1)) {
                info.addAction(4096);
            }
            if (ViewPager.this.canScrollHorizontally(-1)) {
                info.addAction(8192);
            }
        }

        public boolean performAccessibilityAction(View host, int action, Bundle args) {
            if (super.performAccessibilityAction(host, action, args)) {
                return true;
            }
            switch (action) {
                case 4096:
                    if (!ViewPager.this.canScrollHorizontally(1)) {
                        return ViewPager.DEBUG;
                    }
                    ViewPager.this.setCurrentItem(ViewPager.this.mCurItem + 1);
                    return true;
                case 8192:
                    if (!ViewPager.this.canScrollHorizontally(-1)) {
                        return ViewPager.DEBUG;
                    }
                    ViewPager.this.setCurrentItem(ViewPager.this.mCurItem - 1);
                    return true;
                default:
                    return ViewPager.DEBUG;
            }
        }

        private boolean canScroll() {
            return (ViewPager.this.mAdapter == null || ViewPager.this.mAdapter.getCount() <= 1) ? ViewPager.DEBUG : true;
        }
    }

    public interface PageTransformer {
        void transformPage(View view, float f);
    }

    private class PagerObserver extends DataSetObserver {
        PagerObserver() {
        }

        public void onChanged() {
            ViewPager.this.dataSetChanged();
        }

        public void onInvalidated() {
            ViewPager.this.dataSetChanged();
        }
    }

    public static class SavedState extends AbsSavedState {
        public static final Creator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        Parcelable adapterState;
        ClassLoader loader;
        int position;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.position);
            out.writeParcelable(this.adapterState, flags);
        }

        public String toString() {
            return "FragmentPager.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " position=" + this.position + "}";
        }

        SavedState(Parcel in, ClassLoader loader) {
            super(in, loader);
            if (loader == null) {
                loader = getClass().getClassLoader();
            }
            this.position = in.readInt();
            this.adapterState = in.readParcelable(loader);
            this.loader = loader;
        }
    }

    public static class SimpleOnPageChangeListener implements OnPageChangeListener {
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
        }

        public void onPageScrollStateChanged(int state) {
        }
    }

    static class ViewPositionComparator implements Comparator<View> {
        ViewPositionComparator() {
        }

        public int compare(View lhs, View rhs) {
            LayoutParams llp = (LayoutParams) lhs.getLayoutParams();
            LayoutParams rlp = (LayoutParams) rhs.getLayoutParams();
            if (llp.isDecor == rlp.isDecor) {
                return llp.position - rlp.position;
            }
            return llp.isDecor ? 1 : -1;
        }
    }

    public ViewPager(Context context) {
        super(context);
        initViewPager();
    }

    public ViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViewPager();
    }

    void initViewPager() {
        setWillNotDraw(DEBUG);
        setDescendantFocusability(262144);
        setFocusable(true);
        Context context = getContext();
        this.mHwViewPager = HwControlFactory.getHwViewPager(context);
        if (this.mHwViewPager != null) {
            this.mScroller = this.mHwViewPager.createScroller(context);
        } else {
            this.mScroller = new Scroller(context, sInterpolator);
        }
        String curentLang = Locale.getDefault().getLanguage();
        if ("ar".equalsIgnoreCase(curentLang) || "fa".equalsIgnoreCase(curentLang) || "iw".equalsIgnoreCase(curentLang) || "ug".equalsIgnoreCase(curentLang) || "ur".equalsIgnoreCase(curentLang)) {
            this.isLayoutRtl = true;
        }
        ViewConfiguration configuration = ViewConfiguration.get(context);
        float density = context.getResources().getDisplayMetrics().density;
        this.mTouchSlop = configuration.getScaledPagingTouchSlop();
        this.mMinimumVelocity = (int) (400.0f * density);
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        this.mLeftEdge = new EdgeEffect(context);
        this.mRightEdge = new EdgeEffect(context);
        this.mFlingDistance = (int) (25.0f * density);
        this.mCloseEnough = (int) (2.0f * density);
        this.mDefaultGutterSize = (int) (16.0f * density);
        ViewCompat.setAccessibilityDelegate(this, new MyAccessibilityDelegate());
        if (ViewCompat.getImportantForAccessibility(this) == 0) {
            ViewCompat.setImportantForAccessibility(this, 1);
        }
        ViewCompat.setOnApplyWindowInsetsListener(this, new OnApplyWindowInsetsListener() {
            private final Rect mTempRect = new Rect();

            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat originalInsets) {
                WindowInsetsCompat applied = ViewCompat.onApplyWindowInsets(v, originalInsets);
                if (applied.isConsumed()) {
                    return applied;
                }
                Rect res = this.mTempRect;
                res.left = applied.getSystemWindowInsetLeft();
                res.top = applied.getSystemWindowInsetTop();
                res.right = applied.getSystemWindowInsetRight();
                res.bottom = applied.getSystemWindowInsetBottom();
                int count = ViewPager.this.getChildCount();
                for (int i = 0; i < count; i++) {
                    WindowInsetsCompat childInsets = ViewCompat.dispatchApplyWindowInsets(ViewPager.this.getChildAt(i), applied);
                    res.left = Math.min(childInsets.getSystemWindowInsetLeft(), res.left);
                    res.top = Math.min(childInsets.getSystemWindowInsetTop(), res.top);
                    res.right = Math.min(childInsets.getSystemWindowInsetRight(), res.right);
                    res.bottom = Math.min(childInsets.getSystemWindowInsetBottom(), res.bottom);
                }
                return applied.replaceSystemWindowInsets(res.left, res.top, res.right, res.bottom);
            }
        });
    }

    protected void onDetachedFromWindow() {
        removeCallbacks(this.mEndScrollRunnable);
        if (!(this.mScroller == null || (this.mScroller.isFinished() ^ 1) == 0)) {
            this.mScroller.abortAnimation();
        }
        super.onDetachedFromWindow();
    }

    void setScrollState(int newState) {
        boolean z = DEBUG;
        if (this.mScrollState != newState) {
            this.mScrollState = newState;
            if (this.mPageTransformer != null) {
                if (newState != 0) {
                    z = true;
                }
                enableLayers(z);
            }
            dispatchOnScrollStateChanged(newState);
        }
    }

    public void setAdapter(PagerAdapter adapter) {
        int i;
        if (this.mAdapter != null) {
            this.mAdapter.setViewPagerObserver(null);
            this.mAdapter.startUpdate((ViewGroup) this);
            for (i = 0; i < this.mItems.size(); i++) {
                ItemInfo ii = (ItemInfo) this.mItems.get(i);
                this.mAdapter.destroyItem((ViewGroup) this, ii.position, ii.object);
            }
            this.mAdapter.finishUpdate((ViewGroup) this);
            this.mItems.clear();
            removeNonDecorViews();
            this.mCurItem = 0;
            scrollTo(0, 0);
        }
        PagerAdapter oldAdapter = this.mAdapter;
        this.mAdapter = adapter;
        this.mExpectedAdapterCount = 0;
        if (this.mAdapter != null) {
            if (this.mObserver == null) {
                this.mObserver = new PagerObserver();
            }
            this.mAdapter.setViewPagerObserver(this.mObserver);
            this.mPopulatePending = DEBUG;
            boolean wasFirstLayout = this.mFirstLayout;
            this.mFirstLayout = true;
            this.mExpectedAdapterCount = this.mAdapter.getCount();
            if (this.mRestoredCurItem >= 0) {
                this.mAdapter.restoreState(this.mRestoredAdapterState, this.mRestoredClassLoader);
                setCurrentItemInternal(this.mRestoredCurItem, DEBUG, true);
                this.mRestoredCurItem = -1;
                this.mRestoredAdapterState = null;
                this.mRestoredClassLoader = null;
            } else if (wasFirstLayout) {
                requestLayout();
            } else {
                populate();
            }
        }
        if (this.mAdapterChangeListeners != null && (this.mAdapterChangeListeners.isEmpty() ^ 1) != 0) {
            int count = this.mAdapterChangeListeners.size();
            for (i = 0; i < count; i++) {
                ((OnAdapterChangeListener) this.mAdapterChangeListeners.get(i)).onAdapterChanged(this, oldAdapter, adapter);
            }
        }
    }

    private void removeNonDecorViews() {
        int i = 0;
        while (i < getChildCount()) {
            if (!((LayoutParams) getChildAt(i).getLayoutParams()).isDecor) {
                removeViewAt(i);
                i--;
            }
            i++;
        }
    }

    public PagerAdapter getAdapter() {
        return this.mAdapter;
    }

    public void addOnAdapterChangeListener(@NonNull OnAdapterChangeListener listener) {
        if (this.mAdapterChangeListeners == null) {
            this.mAdapterChangeListeners = new ArrayList();
        }
        this.mAdapterChangeListeners.add(listener);
    }

    public void removeOnAdapterChangeListener(@NonNull OnAdapterChangeListener listener) {
        if (this.mAdapterChangeListeners != null) {
            this.mAdapterChangeListeners.remove(listener);
        }
    }

    private int getClientWidth() {
        return (getMeasuredWidth() - getPaddingLeft()) - getPaddingRight();
    }

    public void setCurrentItem(int item) {
        this.mPopulatePending = DEBUG;
        setCurrentItemInternal(item, this.mFirstLayout ^ 1, DEBUG);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        this.mPopulatePending = DEBUG;
        setCurrentItemInternal(item, smoothScroll, DEBUG);
    }

    public int getCurrentItem() {
        return this.mCurItem;
    }

    void setCurrentItemInternal(int item, boolean smoothScroll, boolean always) {
        setCurrentItemInternal(item, smoothScroll, always, 0);
    }

    void setCurrentItemInternal(int item, boolean smoothScroll, boolean always, int velocity) {
        if (this.mAdapter == null || this.mAdapter.getCount() <= 0) {
            setScrollingCacheEnabled(DEBUG);
        } else if (always || this.mCurItem != item || this.mItems.size() == 0) {
            if (item < 0) {
                item = 0;
            } else if (item >= this.mAdapter.getCount()) {
                item = this.mAdapter.getCount() - 1;
            }
            int pageLimit = this.mOffscreenPageLimit;
            if (item > this.mCurItem + pageLimit || item < this.mCurItem - pageLimit) {
                for (int i = 0; i < this.mItems.size(); i++) {
                    ((ItemInfo) this.mItems.get(i)).scrolling = true;
                }
            }
            boolean dispatchSelected = this.mCurItem != item ? true : DEBUG;
            if (this.mFirstLayout) {
                this.mCurItem = item;
                if (dispatchSelected) {
                    dispatchOnPageSelected(item);
                }
                requestLayout();
            } else {
                populate(item);
                scrollToItem(item, smoothScroll, velocity, dispatchSelected);
            }
        } else {
            setScrollingCacheEnabled(DEBUG);
        }
    }

    private void scrollToItem(int item, boolean smoothScroll, int velocity, boolean dispatchSelected) {
        ItemInfo curInfo = infoForPosition(item);
        int destX = 0;
        if (curInfo != null) {
            destX = (int) (((float) getClientWidth()) * Math.max(this.mFirstOffset, Math.min(curInfo.offset, this.mLastOffset)));
        }
        if (this.isLayoutRtl) {
            destX = -destX;
        }
        if (smoothScroll) {
            smoothScrollTo(destX, 0, velocity);
            if (dispatchSelected) {
                dispatchOnPageSelected(item);
                return;
            }
            return;
        }
        if (dispatchSelected) {
            dispatchOnPageSelected(item);
        }
        completeScroll(DEBUG);
        scrollTo(destX, 0);
        pageScrolled(destX);
    }

    @Deprecated
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.mOnPageChangeListener = listener;
    }

    public void addOnPageChangeListener(OnPageChangeListener listener) {
        if (this.mOnPageChangeListeners == null) {
            this.mOnPageChangeListeners = new ArrayList();
        }
        this.mOnPageChangeListeners.add(listener);
    }

    public void removeOnPageChangeListener(OnPageChangeListener listener) {
        if (this.mOnPageChangeListeners != null) {
            this.mOnPageChangeListeners.remove(listener);
        }
    }

    public void clearOnPageChangeListeners() {
        if (this.mOnPageChangeListeners != null) {
            this.mOnPageChangeListeners.clear();
        }
    }

    public void setPageTransformer(boolean reverseDrawingOrder, PageTransformer transformer) {
        setPageTransformer(reverseDrawingOrder, transformer, 2);
    }

    public void setPageTransformer(boolean reverseDrawingOrder, PageTransformer transformer, int pageLayerType) {
        boolean z;
        int i = 1;
        boolean hasTransformer = transformer != null ? true : DEBUG;
        if (this.mPageTransformer != null) {
            z = true;
        } else {
            z = false;
        }
        boolean needsPopulate = hasTransformer != z ? true : DEBUG;
        this.mPageTransformer = transformer;
        setChildrenDrawingOrderEnabled(hasTransformer);
        if (hasTransformer) {
            if (reverseDrawingOrder) {
                i = 2;
            }
            this.mDrawingOrder = i;
            this.mPageTransformerLayerType = pageLayerType;
        } else {
            this.mDrawingOrder = 0;
        }
        if (needsPopulate) {
            populate();
        }
    }

    protected int getChildDrawingOrder(int childCount, int i) {
        return ((LayoutParams) ((View) this.mDrawingOrderedChildren.get(this.mDrawingOrder == 2 ? (childCount - 1) - i : i)).getLayoutParams()).childIndex;
    }

    OnPageChangeListener setInternalPageChangeListener(OnPageChangeListener listener) {
        OnPageChangeListener oldListener = this.mInternalPageChangeListener;
        this.mInternalPageChangeListener = listener;
        return oldListener;
    }

    public int getOffscreenPageLimit() {
        return this.mOffscreenPageLimit;
    }

    public void setOffscreenPageLimit(int limit) {
        if (limit < 1) {
            Log.w(TAG, "Requested offscreen page limit " + limit + " too small; defaulting to " + 1);
            limit = 1;
        }
        if (limit != this.mOffscreenPageLimit) {
            this.mOffscreenPageLimit = limit;
            populate();
        }
    }

    public void setPageMargin(int marginPixels) {
        int oldMargin = this.mPageMargin;
        this.mPageMargin = marginPixels;
        int width = getWidth();
        recomputeScrollPosition(width, width, marginPixels, oldMargin);
        requestLayout();
    }

    public int getPageMargin() {
        return this.mPageMargin;
    }

    public void setPageMarginDrawable(Drawable d) {
        this.mMarginDrawable = d;
        if (d != null) {
            refreshDrawableState();
        }
        setWillNotDraw(d == null ? true : DEBUG);
        invalidate();
    }

    public void setPageMarginDrawable(@DrawableRes int resId) {
        setPageMarginDrawable(ContextCompat.getDrawable(getContext(), resId));
    }

    protected boolean verifyDrawable(Drawable who) {
        return (super.verifyDrawable(who) || who == this.mMarginDrawable) ? true : DEBUG;
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable d = this.mMarginDrawable;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
    }

    float distanceInfluenceForSnapDuration(float f) {
        return (float) Math.sin((double) ((f - 0.5f) * 0.47123894f));
    }

    void smoothScrollTo(int x, int y) {
        smoothScrollTo(x, y, 0);
    }

    void smoothScrollTo(int x, int y, int velocity) {
        if (getChildCount() == 0) {
            setScrollingCacheEnabled(DEBUG);
            return;
        }
        int sx;
        if ((this.mScroller != null ? this.mScroller.isFinished() ^ 1 : 0) != 0) {
            sx = this.mIsScrollStarted ? this.mScroller.getCurrX() : this.mScroller.getStartX();
            this.mScroller.abortAnimation();
            setScrollingCacheEnabled(DEBUG);
        } else {
            sx = getScrollX();
        }
        int sy = getScrollY();
        int dx = x - sx;
        int dy = y - sy;
        if (dx == 0 && dy == 0) {
            completeScroll(DEBUG);
            populate();
            setScrollState(0);
            return;
        }
        int duration;
        setScrollingCacheEnabled(true);
        setScrollState(2);
        int width = getClientWidth();
        int halfWidth = width / 2;
        float distance = ((float) halfWidth) + (((float) halfWidth) * distanceInfluenceForSnapDuration(Math.min(1.0f, (((float) Math.abs(dx)) * 1.0f) / ((float) width))));
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = Math.round(Math.abs(distance / ((float) velocity)) * 1000.0f) * 4;
        } else {
            duration = (int) ((1.0f + (((float) Math.abs(dx)) / (((float) this.mPageMargin) + (((float) width) * this.mAdapter.getPageWidth(this.mCurItem))))) * 100.0f);
        }
        duration = Math.min(duration, MAX_SETTLE_DURATION);
        this.mIsScrollStarted = DEBUG;
        this.mScroller.startScroll(sx, sy, dx, dy, duration);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    ItemInfo addNewItem(int position, int index) {
        ItemInfo ii = new ItemInfo();
        ii.position = position;
        ii.object = this.mAdapter.instantiateItem((ViewGroup) this, position);
        ii.widthFactor = this.mAdapter.getPageWidth(position);
        if (index < 0 || index >= this.mItems.size()) {
            this.mItems.add(ii);
        } else {
            this.mItems.add(index, ii);
        }
        return ii;
    }

    void dataSetChanged() {
        int adapterCount = this.mAdapter.getCount();
        this.mExpectedAdapterCount = adapterCount;
        boolean needPopulate = this.mItems.size() < (this.mOffscreenPageLimit * 2) + 1 ? this.mItems.size() < adapterCount ? true : DEBUG : DEBUG;
        int newCurrItem = this.mCurItem;
        boolean isUpdating = DEBUG;
        int i = 0;
        while (i < this.mItems.size()) {
            ItemInfo ii = (ItemInfo) this.mItems.get(i);
            int newPos = this.mAdapter.getItemPosition(ii.object);
            if (newPos != -1) {
                if (newPos == -2) {
                    this.mItems.remove(i);
                    i--;
                    if (!isUpdating) {
                        this.mAdapter.startUpdate((ViewGroup) this);
                        isUpdating = true;
                    }
                    this.mAdapter.destroyItem((ViewGroup) this, ii.position, ii.object);
                    needPopulate = true;
                    if (this.mCurItem == ii.position) {
                        newCurrItem = Math.max(0, Math.min(this.mCurItem, adapterCount - 1));
                        needPopulate = true;
                    }
                } else if (ii.position != newPos) {
                    if (ii.position == this.mCurItem) {
                        newCurrItem = newPos;
                    }
                    ii.position = newPos;
                    needPopulate = true;
                }
            }
            i++;
        }
        if (isUpdating) {
            this.mAdapter.finishUpdate((ViewGroup) this);
        }
        Collections.sort(this.mItems, COMPARATOR);
        if (needPopulate) {
            int childCount = getChildCount();
            for (i = 0; i < childCount; i++) {
                LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
                if (!lp.isDecor) {
                    lp.widthFactor = 0.0f;
                }
            }
            setCurrentItemInternal(newCurrItem, DEBUG, true);
            requestLayout();
        }
    }

    void populate() {
        populate(this.mCurItem);
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x0157  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x040d  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x01e3  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0206  */
    /* JADX WARNING: Removed duplicated region for block: B:124:0x041a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void populate(int newCurrentItem) {
        ItemInfo oldCurInfo = null;
        if (this.mCurItem != newCurrentItem) {
            oldCurInfo = infoForPosition(this.mCurItem);
            this.mCurItem = newCurrentItem;
        }
        if (this.mAdapter == null) {
            sortChildDrawingOrder();
        } else if (this.mPopulatePending) {
            sortChildDrawingOrder();
        } else if (getWindowToken() != null) {
            this.mAdapter.startUpdate((ViewGroup) this);
            int pageLimit = this.mOffscreenPageLimit;
            int startPos = Math.max(0, this.mCurItem - pageLimit);
            int N = this.mAdapter.getCount();
            int endPos = Math.min(N - 1, this.mCurItem + pageLimit);
            if (N != this.mExpectedAdapterCount) {
                String resName;
                try {
                    resName = getResources().getResourceName(getId());
                } catch (NotFoundException e) {
                    resName = Integer.toHexString(getId());
                }
                throw new IllegalStateException("The application's PagerAdapter changed the adapter's contents without calling PagerAdapter#notifyDataSetChanged! Expected adapter item count: " + this.mExpectedAdapterCount + ", found: " + N + " Pager id: " + resName + " Pager class: " + getClass() + " Problematic adapter: " + this.mAdapter.getClass());
            }
            int childCount;
            int i;
            ItemInfo curItem = null;
            int curIndex = 0;
            while (curIndex < this.mItems.size()) {
                ItemInfo ii = (ItemInfo) this.mItems.get(curIndex);
                if (ii.position >= this.mCurItem) {
                    View child;
                    if (ii.position == this.mCurItem) {
                        curItem = ii;
                    }
                    if (curItem == null && N > 0) {
                        curItem = addNewItem(this.mCurItem, curIndex);
                    }
                    if (curItem != null) {
                        float leftWidthNeeded;
                        float extraWidthLeft = 0.0f;
                        int itemIndex = curIndex - 1;
                        ii = itemIndex >= 0 ? (ItemInfo) this.mItems.get(itemIndex) : null;
                        int clientWidth = getClientWidth();
                        if (clientWidth <= 0) {
                            leftWidthNeeded = 0.0f;
                        } else {
                            leftWidthNeeded = (2.0f - curItem.widthFactor) + (((float) getPaddingLeft()) / ((float) clientWidth));
                        }
                        int pos = this.mCurItem - 1;
                        while (pos >= 0) {
                            if (extraWidthLeft < leftWidthNeeded || pos >= startPos) {
                                if (ii == null || pos != ii.position) {
                                    extraWidthLeft += addNewItem(pos, itemIndex + 1).widthFactor;
                                    curIndex++;
                                    ii = itemIndex >= 0 ? (ItemInfo) this.mItems.get(itemIndex) : null;
                                } else {
                                    extraWidthLeft += ii.widthFactor;
                                    itemIndex--;
                                    ii = itemIndex >= 0 ? (ItemInfo) this.mItems.get(itemIndex) : null;
                                }
                            } else if (ii == null) {
                                break;
                            } else {
                                if (pos == ii.position && (ii.scrolling ^ 1) != 0) {
                                    this.mItems.remove(itemIndex);
                                    this.mAdapter.destroyItem((ViewGroup) this, pos, ii.object);
                                    itemIndex--;
                                    curIndex--;
                                    ii = itemIndex >= 0 ? (ItemInfo) this.mItems.get(itemIndex) : null;
                                }
                            }
                            pos--;
                        }
                        float extraWidthRight = curItem.widthFactor;
                        itemIndex = curIndex + 1;
                        if (extraWidthRight < 2.0f) {
                            float rightWidthNeeded;
                            ii = itemIndex < this.mItems.size() ? (ItemInfo) this.mItems.get(itemIndex) : null;
                            if (clientWidth <= 0) {
                                rightWidthNeeded = 0.0f;
                            } else {
                                rightWidthNeeded = (((float) getPaddingRight()) / ((float) clientWidth)) + 2.0f;
                            }
                            pos = this.mCurItem + 1;
                            while (pos < N) {
                                if (extraWidthRight < rightWidthNeeded || pos <= endPos) {
                                    if (ii == null || pos != ii.position) {
                                        itemIndex++;
                                        extraWidthRight += addNewItem(pos, itemIndex).widthFactor;
                                        ii = itemIndex < this.mItems.size() ? (ItemInfo) this.mItems.get(itemIndex) : null;
                                    } else {
                                        extraWidthRight += ii.widthFactor;
                                        itemIndex++;
                                        ii = itemIndex < this.mItems.size() ? (ItemInfo) this.mItems.get(itemIndex) : null;
                                    }
                                } else if (ii == null) {
                                    break;
                                } else {
                                    if (pos == ii.position && (ii.scrolling ^ 1) != 0) {
                                        this.mItems.remove(itemIndex);
                                        this.mAdapter.destroyItem((ViewGroup) this, pos, ii.object);
                                        ii = itemIndex < this.mItems.size() ? (ItemInfo) this.mItems.get(itemIndex) : null;
                                    }
                                }
                                pos++;
                            }
                        }
                        calculatePageOffsets(curItem, curIndex, oldCurInfo);
                    }
                    this.mAdapter.setPrimaryItem((ViewGroup) this, this.mCurItem, curItem == null ? curItem.object : null);
                    this.mAdapter.finishUpdate((ViewGroup) this);
                    childCount = getChildCount();
                    for (i = 0; i < childCount; i++) {
                        child = getChildAt(i);
                        LayoutParams lp = (LayoutParams) child.getLayoutParams();
                        lp.childIndex = i;
                        if (!lp.isDecor && lp.widthFactor == 0.0f) {
                            ii = infoForChild(child);
                            if (ii != null) {
                                lp.widthFactor = ii.widthFactor;
                                lp.position = ii.position;
                            }
                        }
                    }
                    sortChildDrawingOrder();
                    if (hasFocus()) {
                        View currentFocused = findFocus();
                        ii = currentFocused != null ? infoForAnyChild(currentFocused) : null;
                        if (ii == null || ii.position != this.mCurItem) {
                            for (i = 0; i < getChildCount(); i++) {
                                child = getChildAt(i);
                                ii = infoForChild(child);
                                if (ii != null && ii.position == this.mCurItem && child.requestFocus(2)) {
                                    break;
                                }
                            }
                        }
                    }
                }
                curIndex++;
            }
            curItem = addNewItem(this.mCurItem, curIndex);
            if (curItem != null) {
            }
            if (curItem == null) {
            }
            this.mAdapter.setPrimaryItem((ViewGroup) this, this.mCurItem, curItem == null ? curItem.object : null);
            this.mAdapter.finishUpdate((ViewGroup) this);
            childCount = getChildCount();
            while (i < childCount) {
            }
            sortChildDrawingOrder();
            if (hasFocus()) {
            }
        }
    }

    private void sortChildDrawingOrder() {
        if (this.mDrawingOrder != 0) {
            if (this.mDrawingOrderedChildren == null) {
                this.mDrawingOrderedChildren = new ArrayList();
            } else {
                this.mDrawingOrderedChildren.clear();
            }
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                this.mDrawingOrderedChildren.add(getChildAt(i));
            }
            Collections.sort(this.mDrawingOrderedChildren, sPositionComparator);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x005e A:{LOOP_END, LOOP:2: B:18:0x005a->B:20:0x005e} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00a8 A:{LOOP_END, LOOP:5: B:33:0x00a4->B:35:0x00a8} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void calculatePageOffsets(ItemInfo curItem, int curIndex, ItemInfo oldCurInfo) {
        float offset;
        int pos;
        ItemInfo ii;
        int N = this.mAdapter.getCount();
        int width = getClientWidth();
        float marginOffset = width > 0 ? ((float) this.mPageMargin) / ((float) width) : 0.0f;
        if (oldCurInfo != null) {
            int oldCurPosition = oldCurInfo.position;
            int itemIndex;
            if (oldCurPosition < curItem.position) {
                itemIndex = 0;
                offset = (oldCurInfo.offset + oldCurInfo.widthFactor) + marginOffset;
                pos = oldCurPosition + 1;
                while (pos <= curItem.position && itemIndex < this.mItems.size()) {
                    ii = this.mItems.get(itemIndex);
                    while (true) {
                        ii = ii;
                        if (pos <= ii.position || itemIndex >= this.mItems.size() - 1) {
                            while (pos < ii.position) {
                                offset += this.mAdapter.getPageWidth(pos) + marginOffset;
                                pos++;
                            }
                        } else {
                            itemIndex++;
                            ii = this.mItems.get(itemIndex);
                        }
                    }
                    while (pos < ii.position) {
                    }
                    ii.offset = offset;
                    offset += ii.widthFactor + marginOffset;
                    pos++;
                }
            } else if (oldCurPosition > curItem.position) {
                itemIndex = this.mItems.size() - 1;
                offset = oldCurInfo.offset;
                pos = oldCurPosition - 1;
                while (pos >= curItem.position && itemIndex >= 0) {
                    ii = this.mItems.get(itemIndex);
                    while (true) {
                        ii = ii;
                        if (pos >= ii.position || itemIndex <= 0) {
                            while (pos > ii.position) {
                                offset -= this.mAdapter.getPageWidth(pos) + marginOffset;
                                pos--;
                            }
                        } else {
                            itemIndex--;
                            ii = this.mItems.get(itemIndex);
                        }
                    }
                    while (pos > ii.position) {
                    }
                    offset -= ii.widthFactor + marginOffset;
                    ii.offset = offset;
                    pos--;
                }
            }
        }
        int itemCount = this.mItems.size();
        offset = curItem.offset;
        pos = curItem.position - 1;
        this.mFirstOffset = curItem.position == 0 ? curItem.offset : -3.4028235E38f;
        this.mLastOffset = curItem.position == N + -1 ? (curItem.offset + curItem.widthFactor) - 1.0f : Float.MAX_VALUE;
        int i = curIndex - 1;
        while (i >= 0) {
            ii = (ItemInfo) this.mItems.get(i);
            while (pos > ii.position) {
                offset -= this.mAdapter.getPageWidth(pos) + marginOffset;
                pos--;
            }
            offset -= ii.widthFactor + marginOffset;
            ii.offset = offset;
            if (ii.position == 0) {
                this.mFirstOffset = offset;
            }
            i--;
            pos--;
        }
        offset = (curItem.offset + curItem.widthFactor) + marginOffset;
        pos = curItem.position + 1;
        i = curIndex + 1;
        while (i < itemCount) {
            ii = (ItemInfo) this.mItems.get(i);
            while (pos < ii.position) {
                offset += this.mAdapter.getPageWidth(pos) + marginOffset;
                pos++;
            }
            if (ii.position == N - 1) {
                this.mLastOffset = (ii.widthFactor + offset) - 1.0f;
            }
            ii.offset = offset;
            offset += ii.widthFactor + marginOffset;
            i++;
            pos++;
        }
        this.mNeedCalculatePageOffsets = DEBUG;
    }

    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.position = this.mCurItem;
        if (this.mAdapter != null) {
            ss.adapterState = this.mAdapter.saveState();
        }
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            if (this.mAdapter != null) {
                this.mAdapter.restoreState(ss.adapterState, ss.loader);
                setCurrentItemInternal(ss.position, DEBUG, true);
            } else {
                this.mRestoredCurItem = ss.position;
                this.mRestoredAdapterState = ss.adapterState;
                this.mRestoredClassLoader = ss.loader;
            }
            return;
        }
        super.onRestoreInstanceState(state);
    }

    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        if (!checkLayoutParams(params)) {
            params = generateLayoutParams(params);
        }
        LayoutParams lp = (LayoutParams) params;
        lp.isDecor |= isDecorView(child);
        if (!this.mInLayout) {
            super.addView(child, index, params);
        } else if (lp == null || !lp.isDecor) {
            lp.needsMeasure = true;
            addViewInLayout(child, index, params);
        } else {
            throw new IllegalStateException("Cannot add pager decor view during layout");
        }
    }

    private static boolean isDecorView(@NonNull View view) {
        return view.getClass().getAnnotation(DecorView.class) != null ? true : DEBUG;
    }

    public void removeView(View view) {
        if (this.mInLayout) {
            removeViewInLayout(view);
        } else {
            super.removeView(view);
        }
    }

    ItemInfo infoForChild(View child) {
        for (int i = 0; i < this.mItems.size(); i++) {
            ItemInfo ii = (ItemInfo) this.mItems.get(i);
            if (this.mAdapter.isViewFromObject(child, ii.object)) {
                return ii;
            }
        }
        return null;
    }

    ItemInfo infoForAnyChild(View child) {
        while (true) {
            View parent = child.getParent();
            if (parent == this) {
                return infoForChild(child);
            }
            if (parent == null || ((parent instanceof View) ^ 1) != 0) {
                return null;
            }
            child = parent;
        }
        return null;
    }

    ItemInfo infoForPosition(int position) {
        for (int i = 0; i < this.mItems.size(); i++) {
            ItemInfo ii = (ItemInfo) this.mItems.get(i);
            if (ii.position == position) {
                return ii;
            }
        }
        return null;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mFirstLayout = true;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        View child;
        LayoutParams lp;
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        int measuredWidth = getMeasuredWidth();
        this.mGutterSize = Math.min(measuredWidth / 10, this.mDefaultGutterSize);
        int childWidthSize = (measuredWidth - getPaddingLeft()) - getPaddingRight();
        int childHeightSize = (getMeasuredHeight() - getPaddingTop()) - getPaddingBottom();
        int size = getChildCount();
        for (i = 0; i < size; i++) {
            child = getChildAt(i);
            if (child.getVisibility() != 8) {
                lp = (LayoutParams) child.getLayoutParams();
                if (lp != null && lp.isDecor) {
                    int hgrav = lp.gravity & 7;
                    int vgrav = lp.gravity & 112;
                    int widthMode = ExploreByTouchHelper.INVALID_ID;
                    int heightMode = ExploreByTouchHelper.INVALID_ID;
                    boolean consumeVertical = (vgrav == 48 || vgrav == 80) ? true : DEBUG;
                    boolean consumeHorizontal = (hgrav == 3 || hgrav == 5) ? true : DEBUG;
                    if (consumeVertical) {
                        widthMode = 1073741824;
                    } else if (consumeHorizontal) {
                        heightMode = 1073741824;
                    }
                    int widthSize = childWidthSize;
                    int heightSize = childHeightSize;
                    if (lp.width != -2) {
                        widthMode = 1073741824;
                        if (lp.width != -1) {
                            widthSize = lp.width;
                        }
                    }
                    if (lp.height != -2) {
                        heightMode = 1073741824;
                        if (lp.height != -1) {
                            heightSize = lp.height;
                        }
                    }
                    child.measure(MeasureSpec.makeMeasureSpec(widthSize, widthMode), MeasureSpec.makeMeasureSpec(heightSize, heightMode));
                    if (consumeVertical) {
                        childHeightSize -= child.getMeasuredHeight();
                    } else if (consumeHorizontal) {
                        childWidthSize -= child.getMeasuredWidth();
                    }
                }
            }
        }
        this.mChildWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, 1073741824);
        this.mChildHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize, 1073741824);
        this.mInLayout = true;
        populate();
        this.mInLayout = DEBUG;
        size = getChildCount();
        for (i = 0; i < size; i++) {
            child = getChildAt(i);
            if (child.getVisibility() != 8) {
                lp = (LayoutParams) child.getLayoutParams();
                if (lp == null || (lp.isDecor ^ 1) != 0) {
                    child.measure(MeasureSpec.makeMeasureSpec((int) (((float) childWidthSize) * lp.widthFactor), 1073741824), this.mChildHeightMeasureSpec);
                }
            }
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw) {
            recomputeScrollPosition(w, oldw, this.mPageMargin, this.mPageMargin);
        }
    }

    private void recomputeScrollPosition(int width, int oldWidth, int margin, int oldMargin) {
        if (oldWidth <= 0 || (this.mItems.isEmpty() ^ 1) == 0) {
            ItemInfo ii = infoForPosition(this.mCurItem);
            int scrollPos = (int) (((float) ((width - getPaddingLeft()) - getPaddingRight())) * (ii != null ? Math.min(ii.offset, this.mLastOffset) : 0.0f));
            if (scrollPos != getScrollX()) {
                completeScroll(DEBUG);
                scrollTo(scrollPos, getScrollY());
            }
        } else if (this.mScroller.isFinished()) {
            scrollTo((int) (((float) (((width - getPaddingLeft()) - getPaddingRight()) + margin)) * (((float) getScrollX()) / ((float) (((oldWidth - getPaddingLeft()) - getPaddingRight()) + oldMargin)))), getScrollY());
        } else {
            this.mScroller.setFinalX(getCurrentItem() * getClientWidth());
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int i;
        View child;
        LayoutParams lp;
        int childLeft;
        int childTop;
        int count = getChildCount();
        int width = r - l;
        int height = b - t;
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int scrollX = getScrollX();
        int decorCount = 0;
        for (i = 0; i < count; i++) {
            child = getChildAt(i);
            if (child.getVisibility() != 8) {
                lp = (LayoutParams) child.getLayoutParams();
                if (lp.isDecor) {
                    int vgrav = lp.gravity & 112;
                    switch (lp.gravity & 7) {
                        case 1:
                            childLeft = Math.max((width - child.getMeasuredWidth()) / 2, paddingLeft);
                            break;
                        case 3:
                            childLeft = paddingLeft;
                            paddingLeft += child.getMeasuredWidth();
                            break;
                        case 5:
                            childLeft = (width - paddingRight) - child.getMeasuredWidth();
                            paddingRight += child.getMeasuredWidth();
                            break;
                        default:
                            childLeft = paddingLeft;
                            break;
                    }
                    switch (vgrav) {
                        case 16:
                            childTop = Math.max((height - child.getMeasuredHeight()) / 2, paddingTop);
                            break;
                        case 48:
                            childTop = paddingTop;
                            paddingTop += child.getMeasuredHeight();
                            break;
                        case 80:
                            childTop = (height - paddingBottom) - child.getMeasuredHeight();
                            paddingBottom += child.getMeasuredHeight();
                            break;
                        default:
                            childTop = paddingTop;
                            break;
                    }
                    childLeft += scrollX;
                    child.layout(childLeft, childTop, child.getMeasuredWidth() + childLeft, child.getMeasuredHeight() + childTop);
                    decorCount++;
                }
            }
        }
        int childWidth = (width - paddingLeft) - paddingRight;
        for (i = 0; i < count; i++) {
            child = getChildAt(i);
            if (child.getVisibility() != 8) {
                lp = (LayoutParams) child.getLayoutParams();
                if (!lp.isDecor) {
                    ItemInfo ii = infoForChild(child);
                    if (ii != null) {
                        childLeft = paddingLeft + ((int) (((float) childWidth) * ii.offset));
                        if (this.isLayoutRtl) {
                            childLeft = -childLeft;
                        }
                        childTop = paddingTop;
                        if (lp.needsMeasure) {
                            lp.needsMeasure = DEBUG;
                            child.measure(MeasureSpec.makeMeasureSpec((int) (((float) childWidth) * lp.widthFactor), 1073741824), MeasureSpec.makeMeasureSpec((height - paddingTop) - paddingBottom, 1073741824));
                        }
                        child.layout(childLeft, childTop, child.getMeasuredWidth() + childLeft, child.getMeasuredHeight() + childTop);
                    }
                }
            }
        }
        this.mTopPageBounds = paddingTop;
        this.mBottomPageBounds = height - paddingBottom;
        this.mDecorChildCount = decorCount;
        if (this.mFirstLayout) {
            scrollToItem(this.mCurItem, DEBUG, 0, DEBUG);
        }
        this.mFirstLayout = DEBUG;
    }

    public void computeScroll() {
        this.mIsScrollStarted = true;
        if (this.mScroller.isFinished() || !this.mScroller.computeScrollOffset()) {
            completeScroll(true);
            return;
        }
        int oldX = getScrollX();
        int oldY = getScrollY();
        int x = this.mScroller.getCurrX();
        int y = this.mScroller.getCurrY();
        if (!(oldX == x && oldY == y)) {
            scrollTo(x, y);
            if (!pageScrolled(x)) {
                this.mScroller.abortAnimation();
                scrollTo(0, y);
            }
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private boolean pageScrolled(int xpos) {
        if (this.mItems.size() != 0) {
            int i;
            ItemInfo ii = infoForCurrentScrollPosition();
            int width = getClientWidth();
            int widthWithMargin = width + this.mPageMargin;
            float marginOffset = ((float) this.mPageMargin) / ((float) width);
            int currentPage = ii.position;
            if (this.isLayoutRtl) {
                i = -1;
            } else {
                i = 1;
            }
            float pageOffset = ((((float) i) * (((float) xpos) / ((float) width))) - ii.offset) / (ii.widthFactor + marginOffset);
            int offsetPixels = (int) (((float) widthWithMargin) * pageOffset);
            this.mCalledSuper = DEBUG;
            onPageScrolled(currentPage, pageOffset, offsetPixels);
            if (this.mCalledSuper) {
                return true;
            }
            throw new IllegalStateException("onPageScrolled did not call superclass implementation");
        } else if (this.mFirstLayout) {
            return DEBUG;
        } else {
            this.mCalledSuper = DEBUG;
            onPageScrolled(0, 0.0f, 0);
            if (this.mCalledSuper) {
                return DEBUG;
            }
            throw new IllegalStateException("onPageScrolled did not call superclass implementation");
        }
    }

    @CallSuper
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        int scrollX;
        int childCount;
        int i;
        View child;
        if (this.mDecorChildCount > 0) {
            scrollX = getScrollX();
            int paddingLeft = getPaddingLeft();
            int paddingRight = getPaddingRight();
            int width = getWidth();
            childCount = getChildCount();
            for (i = 0; i < childCount; i++) {
                child = getChildAt(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.isDecor) {
                    int childLeft;
                    switch (lp.gravity & 7) {
                        case 1:
                            childLeft = Math.max((width - child.getMeasuredWidth()) / 2, paddingLeft);
                            break;
                        case 3:
                            childLeft = paddingLeft;
                            paddingLeft += child.getWidth();
                            break;
                        case 5:
                            childLeft = (width - paddingRight) - child.getMeasuredWidth();
                            paddingRight += child.getMeasuredWidth();
                            break;
                        default:
                            childLeft = paddingLeft;
                            break;
                    }
                    int childOffset = (childLeft + scrollX) - child.getLeft();
                    if (childOffset != 0) {
                        child.offsetLeftAndRight(childOffset);
                    }
                }
            }
        }
        dispatchOnPageScrolled(position, offset, offsetPixels);
        if (this.mPageTransformer != null) {
            scrollX = getScrollX();
            childCount = getChildCount();
            for (i = 0; i < childCount; i++) {
                child = getChildAt(i);
                if (!((LayoutParams) child.getLayoutParams()).isDecor) {
                    this.mPageTransformer.transformPage(child, ((float) (child.getLeft() - scrollX)) / ((float) getClientWidth()));
                }
            }
        }
        this.mCalledSuper = true;
        if (this.mHwViewPager != null) {
            this.mHwViewPager.tabScrollerFollowed(position, offset);
        }
    }

    private void dispatchOnPageScrolled(int position, float offset, int offsetPixels) {
        if (this.mOnPageChangeListener != null) {
            this.mOnPageChangeListener.onPageScrolled(position, offset, offsetPixels);
        }
        if (this.mOnPageChangeListeners != null) {
            int z = this.mOnPageChangeListeners.size();
            for (int i = 0; i < z; i++) {
                OnPageChangeListener listener = (OnPageChangeListener) this.mOnPageChangeListeners.get(i);
                if (listener != null) {
                    listener.onPageScrolled(position, offset, offsetPixels);
                }
            }
        }
        if (this.mInternalPageChangeListener != null) {
            this.mInternalPageChangeListener.onPageScrolled(position, offset, offsetPixels);
        }
    }

    private void dispatchOnPageSelected(int position) {
        if (this.mOnPageChangeListener != null) {
            this.mOnPageChangeListener.onPageSelected(position);
        }
        if (this.mOnPageChangeListeners != null) {
            int z = this.mOnPageChangeListeners.size();
            for (int i = 0; i < z; i++) {
                OnPageChangeListener listener = (OnPageChangeListener) this.mOnPageChangeListeners.get(i);
                if (listener != null) {
                    listener.onPageSelected(position);
                }
            }
        }
        if (this.mInternalPageChangeListener != null) {
            this.mInternalPageChangeListener.onPageSelected(position);
        }
    }

    private void dispatchOnScrollStateChanged(int state) {
        if (this.mOnPageChangeListener != null) {
            this.mOnPageChangeListener.onPageScrollStateChanged(state);
        }
        if (this.mOnPageChangeListeners != null) {
            int z = this.mOnPageChangeListeners.size();
            for (int i = 0; i < z; i++) {
                OnPageChangeListener listener = (OnPageChangeListener) this.mOnPageChangeListeners.get(i);
                if (listener != null) {
                    listener.onPageScrollStateChanged(state);
                }
            }
        }
        if (this.mInternalPageChangeListener != null) {
            this.mInternalPageChangeListener.onPageScrollStateChanged(state);
        }
    }

    private void completeScroll(boolean postEvents) {
        boolean needPopulate = this.mScrollState == 2 ? true : DEBUG;
        if (needPopulate) {
            setScrollingCacheEnabled(DEBUG);
            if (this.mScroller.isFinished() ^ 1) {
                this.mScroller.abortAnimation();
                int oldX = getScrollX();
                int oldY = getScrollY();
                int x = this.mScroller.getCurrX();
                int y = this.mScroller.getCurrY();
                if (!(oldX == x && oldY == y)) {
                    scrollTo(x, y);
                    if (x != oldX) {
                        pageScrolled(x);
                    }
                }
            }
        }
        this.mPopulatePending = DEBUG;
        for (int i = 0; i < this.mItems.size(); i++) {
            ItemInfo ii = (ItemInfo) this.mItems.get(i);
            if (ii.scrolling) {
                needPopulate = true;
                ii.scrolling = DEBUG;
            }
        }
        if (!needPopulate) {
            return;
        }
        if (postEvents) {
            ViewCompat.postOnAnimation(this, this.mEndScrollRunnable);
        } else {
            this.mEndScrollRunnable.run();
        }
    }

    private boolean isGutterDrag(float x, float dx) {
        if (x >= ((float) this.mGutterSize) || dx <= 0.0f) {
            return (x <= ((float) (getWidth() - this.mGutterSize)) || dx >= 0.0f) ? DEBUG : true;
        } else {
            return true;
        }
    }

    private void enableLayers(boolean enable) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).setLayerType(enable ? this.mPageTransformerLayerType : 0, null);
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & 255;
        if (action == 3 || action == 1) {
            resetTouch();
            return DEBUG;
        }
        if (action != 0) {
            if (this.mIsBeingDragged) {
                return true;
            }
            if (this.mIsUnableToDrag) {
                return DEBUG;
            }
        }
        switch (action) {
            case 0:
                float x = ev.getX();
                this.mInitialMotionX = x;
                this.mLastMotionX = x;
                x = ev.getY();
                this.mInitialMotionY = x;
                this.mLastMotionY = x;
                this.mActivePointerId = ev.getPointerId(0);
                this.mIsUnableToDrag = DEBUG;
                this.mIsScrollStarted = true;
                this.mScroller.computeScrollOffset();
                if (this.mScrollState == 2 && Math.abs(this.mScroller.getFinalX() - this.mScroller.getCurrX()) > this.mCloseEnough) {
                    this.mScroller.abortAnimation();
                    this.mPopulatePending = DEBUG;
                    populate();
                    this.mIsBeingDragged = true;
                    requestParentDisallowInterceptTouchEvent(true);
                    setScrollState(1);
                    break;
                }
                completeScroll(DEBUG);
                this.mIsBeingDragged = DEBUG;
                break;
            case 2:
                int activePointerId = this.mActivePointerId;
                if (activePointerId != -1) {
                    int pointerIndex = ev.findPointerIndex(activePointerId);
                    if (pointerIndex != -1) {
                        float x2 = ev.getX(pointerIndex);
                        float dx = x2 - this.mLastMotionX;
                        float xDiff = Math.abs(dx);
                        float y = ev.getY(pointerIndex);
                        float yDiff = Math.abs(y - this.mInitialMotionY);
                        if (!(dx == 0.0f || (isGutterDrag(this.mLastMotionX, dx) ^ 1) == 0)) {
                            if (canScroll(this, DEBUG, (int) dx, (int) x2, (int) y)) {
                                this.mLastMotionX = x2;
                                this.mLastMotionY = y;
                                this.mIsUnableToDrag = true;
                                return DEBUG;
                            }
                        }
                        if (xDiff > ((float) this.mTouchSlop) && 0.5f * xDiff > yDiff) {
                            this.mIsBeingDragged = true;
                            requestParentDisallowInterceptTouchEvent(true);
                            setScrollState(1);
                            this.mLastMotionX = dx > 0.0f ? this.mInitialMotionX + ((float) this.mTouchSlop) : this.mInitialMotionX - ((float) this.mTouchSlop);
                            this.mLastMotionY = y;
                            setScrollingCacheEnabled(true);
                        } else if (yDiff > ((float) this.mTouchSlop)) {
                            this.mIsUnableToDrag = true;
                        }
                        if (this.mIsBeingDragged && performDrag(x2)) {
                            ViewCompat.postInvalidateOnAnimation(this);
                            break;
                        }
                    }
                    Log.e(TAG, "pointerIndex out of range, pointerIndex = -1");
                    return true;
                }
                break;
            case 6:
                onSecondaryPointerUp(ev);
                break;
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(ev);
        return this.mIsBeingDragged;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (this.mFakeDragging) {
            return true;
        }
        if (ev.getAction() == 0 && ev.getEdgeFlags() != 0) {
            return DEBUG;
        }
        if (this.mAdapter == null || this.mAdapter.getCount() == 0) {
            return DEBUG;
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(ev);
        int action = ev.getAction();
        boolean needsInvalidate = DEBUG;
        float x;
        int activePointerIndex;
        int index;
        switch (action & 255) {
            case 0:
                this.mScroller.abortAnimation();
                this.mPopulatePending = DEBUG;
                populate();
                x = ev.getX();
                this.mInitialMotionX = x;
                this.mLastMotionX = x;
                x = ev.getY();
                this.mInitialMotionY = x;
                this.mLastMotionY = x;
                this.mActivePointerId = ev.getPointerId(0);
                break;
            case 1:
                if (this.mIsBeingDragged) {
                    VelocityTracker velocityTracker = this.mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getXVelocity(this.mActivePointerId);
                    this.mPopulatePending = true;
                    int width = getClientWidth();
                    int scrollX = getScrollX();
                    ItemInfo ii = infoForCurrentScrollPosition();
                    float marginOffset = ((float) this.mPageMargin) / ((float) width);
                    int currentPage = ii.position;
                    float pageOffset = ((((float) (this.isLayoutRtl ? -1 : 1)) * (((float) scrollX) / ((float) width))) - ii.offset) / (ii.widthFactor + marginOffset);
                    activePointerIndex = ev.findPointerIndex(this.mActivePointerId);
                    if (activePointerIndex != -1) {
                        setCurrentItemInternal(determineTargetPage(currentPage, pageOffset, initialVelocity, (int) (ev.getX(activePointerIndex) - this.mInitialMotionX)), true, true, initialVelocity);
                        needsInvalidate = resetTouch();
                        break;
                    }
                    Log.e(TAG, "activePointerIndex out of range, activePointerIndex = -1");
                    return true;
                }
                break;
            case 2:
                if (!this.mIsBeingDragged) {
                    int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                    if (pointerIndex == -1) {
                        needsInvalidate = resetTouch();
                        break;
                    }
                    float x2 = ev.getX(pointerIndex);
                    float xDiff = Math.abs(x2 - this.mLastMotionX);
                    float y = ev.getY(pointerIndex);
                    float yDiff = Math.abs(y - this.mLastMotionY);
                    if (xDiff > ((float) this.mTouchSlop) && xDiff > yDiff) {
                        this.mIsBeingDragged = true;
                        requestParentDisallowInterceptTouchEvent(true);
                        if (x2 - this.mInitialMotionX > 0.0f) {
                            x = this.mInitialMotionX + ((float) this.mTouchSlop);
                        } else {
                            x = this.mInitialMotionX - ((float) this.mTouchSlop);
                        }
                        this.mLastMotionX = x;
                        this.mLastMotionY = y;
                        setScrollState(1);
                        setScrollingCacheEnabled(true);
                        ViewParent parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                }
                if (this.mIsBeingDragged) {
                    activePointerIndex = ev.findPointerIndex(this.mActivePointerId);
                    if (activePointerIndex != -1) {
                        needsInvalidate = performDrag(ev.getX(activePointerIndex));
                        break;
                    }
                    Log.e(TAG, "activePointerIndex out of range, activePointerIndex = -1");
                    return true;
                }
                break;
            case 3:
                if (this.mIsBeingDragged) {
                    scrollToItem(this.mCurItem, true, 0, DEBUG);
                    needsInvalidate = resetTouch();
                    break;
                }
                break;
            case 5:
                index = ev.getActionIndex();
                if (index != -1) {
                    this.mLastMotionX = ev.getX(index);
                    this.mActivePointerId = ev.getPointerId(index);
                    break;
                }
                Log.e(TAG, "index out of range, index = -1");
                return true;
            case 6:
                onSecondaryPointerUp(ev);
                index = ev.findPointerIndex(this.mActivePointerId);
                if (index != -1) {
                    this.mLastMotionX = ev.getX(index);
                    break;
                }
                Log.e(TAG, "index out of range, index = -1");
                return true;
        }
        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
        return true;
    }

    private boolean resetTouch() {
        this.mActivePointerId = -1;
        endDrag();
        this.mLeftEdge.onRelease();
        this.mRightEdge.onRelease();
        return !this.mLeftEdge.isFinished() ? this.mRightEdge.isFinished() : true;
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private boolean performDrag(float x) {
        boolean needsInvalidate = DEBUG;
        float deltaX = this.mLastMotionX - x;
        this.mLastMotionX = x;
        float oldScrollX = (float) getScrollX();
        float scrollX = oldScrollX + deltaX;
        int width = getClientWidth();
        float leftBound = ((float) width) * this.mFirstOffset;
        float rightBound = ((float) width) * this.mLastOffset;
        boolean leftAbsolute = true;
        boolean rightAbsolute = true;
        ItemInfo firstItem = (ItemInfo) this.mItems.get(0);
        ItemInfo lastItem = (ItemInfo) this.mItems.get(this.mItems.size() - 1);
        if (firstItem.position != 0) {
            if (this.isLayoutRtl) {
                rightAbsolute = DEBUG;
            } else {
                leftAbsolute = DEBUG;
            }
            leftBound = firstItem.offset * ((float) width);
        }
        if (lastItem.position != this.mAdapter.getCount() - 1) {
            if (this.isLayoutRtl) {
                leftAbsolute = DEBUG;
            } else {
                rightAbsolute = DEBUG;
            }
            rightBound = lastItem.offset * ((float) width);
        }
        if (this.isLayoutRtl) {
            float tempf = rightBound;
            rightBound = -leftBound;
            leftBound = -tempf;
        }
        if (scrollX < leftBound) {
            if (leftAbsolute) {
                this.mLeftEdge.onPull(Math.abs(leftBound - scrollX) / ((float) width));
                needsInvalidate = true;
            }
            scrollX = this.mHwViewPager != null ? this.mHwViewPager.scrollEdgeBound(true, oldScrollX, deltaX, leftBound) : leftBound;
        } else if (scrollX > rightBound) {
            if (rightAbsolute) {
                this.mRightEdge.onPull(Math.abs(scrollX - rightBound) / ((float) width));
                needsInvalidate = true;
            }
            scrollX = this.mHwViewPager != null ? this.mHwViewPager.scrollEdgeBound(DEBUG, oldScrollX, deltaX, rightBound) : rightBound;
        }
        this.mLastMotionX += scrollX - ((float) ((int) scrollX));
        scrollTo((int) scrollX, getScrollY());
        pageScrolled((int) scrollX);
        return needsInvalidate;
    }

    private ItemInfo infoForCurrentScrollPosition() {
        int width = getClientWidth();
        float scrollOffset = width > 0 ? ((float) getScrollX()) / ((float) width) : 0.0f;
        float marginOffset = width > 0 ? ((float) this.mPageMargin) / ((float) width) : 0.0f;
        int lastPos = -1;
        float lastOffset = 0.0f;
        float lastWidth = 0.0f;
        boolean first = true;
        if (this.isLayoutRtl) {
            scrollOffset = -scrollOffset;
        }
        ItemInfo lastItem = null;
        int i = 0;
        while (i < this.mItems.size()) {
            ItemInfo ii = (ItemInfo) this.mItems.get(i);
            if (!(first || ii.position == lastPos + 1)) {
                ii = this.mTempItem;
                ii.offset = (lastOffset + lastWidth) + marginOffset;
                ii.position = lastPos + 1;
                ii.widthFactor = this.mAdapter.getPageWidth(ii.position);
                i--;
            }
            float offset = ii.offset;
            float leftBound = offset;
            float rightBound = (ii.widthFactor + offset) + marginOffset;
            if (!first && scrollOffset < offset) {
                return lastItem;
            }
            if (scrollOffset < rightBound || i == this.mItems.size() - 1) {
                return ii;
            }
            first = DEBUG;
            lastPos = ii.position;
            lastOffset = offset;
            lastWidth = ii.widthFactor;
            lastItem = ii;
            i++;
        }
        return lastItem;
    }

    private int determineTargetPage(int currentPage, float pageOffset, int velocity, int deltaX) {
        int targetPage;
        if (this.isLayoutRtl) {
            pageOffset = -pageOffset;
            velocity = -velocity;
            deltaX = -deltaX;
        }
        if (Math.abs(deltaX) <= this.mFlingDistance || Math.abs(velocity) <= this.mMinimumVelocity) {
            targetPage = currentPage + ((int) (pageOffset + (currentPage >= this.mCurItem ? 0.4f : 0.6f)));
        } else {
            targetPage = velocity > 0 ? currentPage : currentPage + 1;
        }
        if (this.mItems.size() <= 0) {
            return targetPage;
        }
        return Math.max(((ItemInfo) this.mItems.get(0)).position, Math.min(targetPage, ((ItemInfo) this.mItems.get(this.mItems.size() - 1)).position));
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        boolean needsInvalidate = DEBUG;
        int overScrollMode = getOverScrollMode();
        if (overScrollMode == 0 || (overScrollMode == 1 && this.mAdapter != null && this.mAdapter.getCount() > 1)) {
            int restoreCount;
            int height;
            int width;
            if (!this.mLeftEdge.isFinished()) {
                restoreCount = canvas.save();
                height = (getHeight() - getPaddingTop()) - getPaddingBottom();
                width = getWidth();
                canvas.rotate(270.0f);
                canvas.translate((float) ((-height) + getPaddingTop()), this.mFirstOffset * ((float) width));
                this.mLeftEdge.setSize(height, width);
                needsInvalidate = this.mLeftEdge.draw(canvas);
                canvas.restoreToCount(restoreCount);
            }
            if (!this.mRightEdge.isFinished()) {
                restoreCount = canvas.save();
                width = getWidth();
                height = (getHeight() - getPaddingTop()) - getPaddingBottom();
                canvas.rotate(90.0f);
                canvas.translate((float) (-getPaddingTop()), (-(this.mLastOffset + 1.0f)) * ((float) width));
                this.mRightEdge.setSize(height, width);
                needsInvalidate |= this.mRightEdge.draw(canvas);
                canvas.restoreToCount(restoreCount);
            }
        } else {
            this.mLeftEdge.finish();
            this.mRightEdge.finish();
        }
        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mPageMargin > 0 && this.mMarginDrawable != null && this.mItems.size() > 0 && this.mAdapter != null) {
            int scrollX = getScrollX();
            int width = getWidth();
            float marginOffset = ((float) this.mPageMargin) / ((float) width);
            int itemIndex = 0;
            ItemInfo ii = (ItemInfo) this.mItems.get(0);
            float offset = ii.offset;
            int itemCount = this.mItems.size();
            int firstPos = ii.position;
            int lastPos = ((ItemInfo) this.mItems.get(itemCount - 1)).position;
            int pos = firstPos;
            while (pos < lastPos) {
                float drawAt;
                while (pos > ii.position && itemIndex < itemCount) {
                    itemIndex++;
                    ii = (ItemInfo) this.mItems.get(itemIndex);
                }
                if (pos == ii.position) {
                    drawAt = (ii.offset + ii.widthFactor) * ((float) width);
                    offset = (ii.offset + ii.widthFactor) + marginOffset;
                } else {
                    float widthFactor = this.mAdapter.getPageWidth(pos);
                    drawAt = (offset + widthFactor) * ((float) width);
                    offset += widthFactor + marginOffset;
                }
                if (((float) this.mPageMargin) + drawAt > ((float) scrollX)) {
                    this.mMarginDrawable.setBounds(Math.round(drawAt), this.mTopPageBounds, Math.round(((float) this.mPageMargin) + drawAt), this.mBottomPageBounds);
                    this.mMarginDrawable.draw(canvas);
                }
                if (drawAt <= ((float) (scrollX + width))) {
                    pos++;
                } else {
                    return;
                }
            }
        }
    }

    public boolean beginFakeDrag() {
        if (this.mIsBeingDragged) {
            return DEBUG;
        }
        this.mFakeDragging = true;
        setScrollState(1);
        this.mLastMotionX = 0.0f;
        this.mInitialMotionX = 0.0f;
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            this.mVelocityTracker.clear();
        }
        long time = SystemClock.uptimeMillis();
        MotionEvent ev = MotionEvent.obtain(time, time, 0, 0.0f, 0.0f, 0);
        this.mVelocityTracker.addMovement(ev);
        ev.recycle();
        this.mFakeDragBeginTime = time;
        return true;
    }

    public void endFakeDrag() {
        if (this.mFakeDragging) {
            if (this.mAdapter != null) {
                int i;
                VelocityTracker velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                int initialVelocity = (int) velocityTracker.getXVelocity(this.mActivePointerId);
                this.mPopulatePending = true;
                int width = getClientWidth();
                int scrollX = getScrollX();
                ItemInfo ii = infoForCurrentScrollPosition();
                int currentPage = ii.position;
                if (this.isLayoutRtl) {
                    i = -1;
                } else {
                    i = 1;
                }
                setCurrentItemInternal(determineTargetPage(currentPage, ((((float) i) * (((float) scrollX) / ((float) width))) - ii.offset) / ii.widthFactor, initialVelocity, (int) (this.mLastMotionX - this.mInitialMotionX)), true, true, initialVelocity);
            }
            endDrag();
            this.mFakeDragging = DEBUG;
            return;
        }
        throw new IllegalStateException("No fake drag in progress. Call beginFakeDrag first.");
    }

    public void fakeDragBy(float xOffset) {
        if (!this.mFakeDragging) {
            throw new IllegalStateException("No fake drag in progress. Call beginFakeDrag first.");
        } else if (this.mAdapter != null) {
            this.mLastMotionX += xOffset;
            float scrollX = ((float) getScrollX()) - xOffset;
            int width = getClientWidth();
            float leftBound = ((float) width) * this.mFirstOffset;
            float rightBound = ((float) width) * this.mLastOffset;
            ItemInfo firstItem = (ItemInfo) this.mItems.get(0);
            ItemInfo lastItem = (ItemInfo) this.mItems.get(this.mItems.size() - 1);
            if (firstItem.position != 0) {
                leftBound = firstItem.offset * ((float) width);
            }
            if (lastItem.position != this.mAdapter.getCount() - 1) {
                rightBound = lastItem.offset * ((float) width);
            }
            if (scrollX < leftBound) {
                scrollX = leftBound;
            } else if (scrollX > rightBound) {
                scrollX = rightBound;
            }
            this.mLastMotionX += scrollX - ((float) ((int) scrollX));
            scrollTo((int) scrollX, getScrollY());
            pageScrolled((int) scrollX);
            MotionEvent ev = MotionEvent.obtain(this.mFakeDragBeginTime, SystemClock.uptimeMillis(), 2, this.mLastMotionX, 0.0f, 0);
            this.mVelocityTracker.addMovement(ev);
            ev.recycle();
        }
    }

    public boolean isFakeDragging() {
        return this.mFakeDragging;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = ev.getActionIndex();
        if (ev.getPointerId(pointerIndex) == this.mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            this.mLastMotionX = ev.getX(newPointerIndex);
            this.mActivePointerId = ev.getPointerId(newPointerIndex);
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.clear();
            }
        }
    }

    private void endDrag() {
        this.mIsBeingDragged = DEBUG;
        this.mIsUnableToDrag = DEBUG;
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void setScrollingCacheEnabled(boolean enabled) {
        if (this.mScrollingCacheEnabled != enabled) {
            this.mScrollingCacheEnabled = enabled;
        }
    }

    public boolean canScrollHorizontally(int direction) {
        boolean z = true;
        if (this.mAdapter == null) {
            return DEBUG;
        }
        int width = getClientWidth();
        int scrollX = getScrollX();
        if (direction < 0) {
            if (scrollX <= ((int) (((float) width) * this.mFirstOffset))) {
                z = DEBUG;
            }
            return z;
        } else if (direction <= 0) {
            return DEBUG;
        } else {
            if (scrollX >= ((int) (((float) width) * this.mLastOffset))) {
                z = DEBUG;
            }
            return z;
        }
    }

    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            int scrollX = v.getScrollX();
            int scrollY = v.getScrollY();
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() && y + scrollY >= child.getTop() && y + scrollY < child.getBottom()) {
                    if (canScroll(child, true, dx, (x + scrollX) - child.getLeft(), (y + scrollY) - child.getTop())) {
                        return true;
                    }
                }
            }
        }
        return checkV ? v.canScrollHorizontally(-dx) : DEBUG;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return !super.dispatchKeyEvent(event) ? executeKeyEvent(event) : true;
    }

    public boolean executeKeyEvent(KeyEvent event) {
        if (event.getAction() != 0) {
            return DEBUG;
        }
        switch (event.getKeyCode()) {
            case MotionEventCompat.AXIS_WHEEL /*21*/:
                if (event.hasModifiers(2)) {
                    return pageLeft();
                }
                return arrowScroll(17);
            case MotionEventCompat.AXIS_GAS /*22*/:
                if (event.hasModifiers(2)) {
                    return pageRight();
                }
                return arrowScroll(66);
            case 61:
                if (event.hasNoModifiers()) {
                    return arrowScroll(2);
                }
                if (event.hasModifiers(1)) {
                    return arrowScroll(1);
                }
                return DEBUG;
            default:
                return DEBUG;
        }
    }

    public boolean arrowScroll(int direction) {
        View currentFocused = findFocus();
        if (currentFocused == this) {
            currentFocused = null;
        } else if (currentFocused != null) {
            boolean isChild = DEBUG;
            for (ViewPager parent = currentFocused.getParent(); parent instanceof ViewGroup; parent = parent.getParent()) {
                if (parent == this) {
                    isChild = true;
                    break;
                }
            }
            if (!isChild) {
                StringBuilder sb = new StringBuilder();
                sb.append(currentFocused.getClass().getSimpleName());
                for (ViewParent parent2 = currentFocused.getParent(); parent2 instanceof ViewGroup; parent2 = parent2.getParent()) {
                    sb.append(" => ").append(parent2.getClass().getSimpleName());
                }
                Log.e(TAG, "arrowScroll tried to find focus based on non-child current focused view " + sb.toString());
                currentFocused = null;
            }
        }
        boolean handled = DEBUG;
        View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);
        if (nextFocused == null || nextFocused == currentFocused) {
            if (direction == 17 || direction == 1) {
                handled = pageLeft();
            } else if (direction == 66 || direction == 2) {
                handled = pageRight();
            }
        } else if (direction == 17) {
            handled = (currentFocused == null || getChildRectInPagerCoordinates(this.mTempRect, nextFocused).left < getChildRectInPagerCoordinates(this.mTempRect, currentFocused).left) ? nextFocused.requestFocus() : pageLeft();
        } else if (direction == 66) {
            handled = (currentFocused == null || getChildRectInPagerCoordinates(this.mTempRect, nextFocused).left > getChildRectInPagerCoordinates(this.mTempRect, currentFocused).left) ? nextFocused.requestFocus() : pageRight();
        }
        if (handled) {
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
        }
        return handled;
    }

    private Rect getChildRectInPagerCoordinates(Rect outRect, View child) {
        if (outRect == null) {
            outRect = new Rect();
        }
        if (child == null) {
            outRect.set(0, 0, 0, 0);
            return outRect;
        }
        outRect.left = child.getLeft();
        outRect.right = child.getRight();
        outRect.top = child.getTop();
        outRect.bottom = child.getBottom();
        ViewGroup parent = child.getParent();
        while ((parent instanceof ViewGroup) && parent != this) {
            ViewGroup group = parent;
            outRect.left += group.getLeft();
            outRect.right += group.getRight();
            outRect.top += group.getTop();
            outRect.bottom += group.getBottom();
            parent = group.getParent();
        }
        return outRect;
    }

    boolean pageLeft() {
        if (this.mCurItem <= 0) {
            return DEBUG;
        }
        setCurrentItem(this.mCurItem - 1, true);
        return true;
    }

    boolean pageRight() {
        if (this.mAdapter == null || this.mCurItem >= this.mAdapter.getCount() - 1) {
            return DEBUG;
        }
        setCurrentItem(this.mCurItem + 1, true);
        return true;
    }

    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        int focusableCount = views.size();
        int descendantFocusability = getDescendantFocusability();
        if (descendantFocusability != 393216) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == 0) {
                    ItemInfo ii = infoForChild(child);
                    if (ii != null && ii.position == this.mCurItem) {
                        child.addFocusables(views, direction, focusableMode);
                    }
                }
            }
        }
        if ((descendantFocusability == 262144 && focusableCount != views.size()) || !isFocusable()) {
            return;
        }
        if (!(((focusableMode & 1) == 1 && isInTouchMode() && (isFocusableInTouchMode() ^ 1) != 0) || views == null)) {
            views.add(this);
        }
    }

    public void addTouchables(ArrayList<View> views) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == 0) {
                ItemInfo ii = infoForChild(child);
                if (ii != null && ii.position == this.mCurItem) {
                    child.addTouchables(views);
                }
            }
        }
    }

    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        int index;
        int increment;
        int end;
        int count = getChildCount();
        if ((direction & 2) != 0) {
            index = 0;
            increment = 1;
            end = count;
        } else {
            index = count - 1;
            increment = -1;
            end = -1;
        }
        for (int i = index; i != end; i += increment) {
            View child = getChildAt(i);
            if (child.getVisibility() == 0) {
                ItemInfo ii = infoForChild(child);
                if (ii != null && ii.position == this.mCurItem && child.requestFocus(direction, previouslyFocusedRect)) {
                    return true;
                }
            }
        }
        return DEBUG;
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == 4096) {
            return super.dispatchPopulateAccessibilityEvent(event);
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == 0) {
                ItemInfo ii = infoForChild(child);
                if (ii != null && ii.position == this.mCurItem && child.dispatchPopulateAccessibilityEvent(event)) {
                    return true;
                }
            }
        }
        return DEBUG;
    }

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return generateDefaultLayoutParams();
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams ? super.checkLayoutParams(p) : DEBUG;
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }
}
