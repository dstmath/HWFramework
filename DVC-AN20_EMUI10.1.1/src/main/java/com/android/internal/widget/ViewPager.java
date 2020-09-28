package com.android.internal.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.view.AbsSavedState;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.inspector.InspectionCompanion;
import android.view.inspector.PropertyMapper;
import android.view.inspector.PropertyReader;
import android.widget.EdgeEffect;
import android.widget.Scroller;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ViewPager extends ViewGroup {
    private static final int CLOSE_ENOUGH = 2;
    private static final Comparator<ItemInfo> COMPARATOR = new Comparator<ItemInfo>() {
        /* class com.android.internal.widget.ViewPager.AnonymousClass1 */

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
    private static final int[] LAYOUT_ATTRS = {16842931};
    private static final int MAX_SCROLL_X = 16777216;
    private static final int MAX_SETTLE_DURATION = 600;
    private static final int MIN_DISTANCE_FOR_FLING = 25;
    private static final int MIN_FLING_VELOCITY = 400;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_SETTLING = 2;
    private static final String TAG = "ViewPager";
    private static final boolean USE_CACHE = false;
    private static final Interpolator sInterpolator = new Interpolator() {
        /* class com.android.internal.widget.ViewPager.AnonymousClass2 */

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float t) {
            float t2 = t - 1.0f;
            return (t2 * t2 * t2 * t2 * t2) + 1.0f;
        }
    };
    private static final ViewPositionComparator sPositionComparator = new ViewPositionComparator();
    private int mActivePointerId;
    private PagerAdapter mAdapter;
    private OnAdapterChangeListener mAdapterChangeListener;
    private int mBottomPageBounds;
    private boolean mCalledSuper;
    private int mChildHeightMeasureSpec;
    private int mChildWidthMeasureSpec;
    private final int mCloseEnough;
    private int mCurItem;
    private int mDecorChildCount;
    private final int mDefaultGutterSize;
    private int mDrawingOrder;
    private ArrayList<View> mDrawingOrderedChildren;
    private final Runnable mEndScrollRunnable;
    private int mExpectedAdapterCount;
    private boolean mFirstLayout;
    private float mFirstOffset;
    private final int mFlingDistance;
    private int mGutterSize;
    private boolean mInLayout;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private OnPageChangeListener mInternalPageChangeListener;
    private boolean mIsBeingDragged;
    private boolean mIsUnableToDrag;
    private final ArrayList<ItemInfo> mItems;
    private float mLastMotionX;
    private float mLastMotionY;
    private float mLastOffset;
    private final EdgeEffect mLeftEdge;
    private int mLeftIncr;
    private Drawable mMarginDrawable;
    private final int mMaximumVelocity;
    private final int mMinimumVelocity;
    private PagerObserver mObserver;
    private int mOffscreenPageLimit;
    private OnPageChangeListener mOnPageChangeListener;
    private int mPageMargin;
    private PageTransformer mPageTransformer;
    private boolean mPopulatePending;
    private Parcelable mRestoredAdapterState;
    private ClassLoader mRestoredClassLoader;
    private int mRestoredCurItem;
    private final EdgeEffect mRightEdge;
    private int mScrollState;
    private final Scroller mScroller;
    private boolean mScrollingCacheEnabled;
    private final ItemInfo mTempItem;
    private final Rect mTempRect;
    private int mTopPageBounds;
    private final int mTouchSlop;
    private VelocityTracker mVelocityTracker;

    interface Decor {
    }

    /* access modifiers changed from: package-private */
    public interface OnAdapterChangeListener {
        void onAdapterChanged(PagerAdapter pagerAdapter, PagerAdapter pagerAdapter2);
    }

    public interface OnPageChangeListener {
        @UnsupportedAppUsage
        void onPageScrollStateChanged(int i);

        @UnsupportedAppUsage
        void onPageScrolled(int i, float f, int i2);

        @UnsupportedAppUsage
        void onPageSelected(int i);
    }

    public interface PageTransformer {
        void transformPage(View view, float f);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        int childIndex;
        public int gravity;
        public boolean isDecor;
        boolean needsMeasure;
        int position;
        float widthFactor = 0.0f;

        public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<LayoutParams> {
            private int mLayout_gravityId;
            private boolean mPropertiesMapped = false;

            @Override // android.view.inspector.InspectionCompanion
            public void mapProperties(PropertyMapper propertyMapper) {
                this.mLayout_gravityId = propertyMapper.mapGravity("layout_gravity", 16842931);
                this.mPropertiesMapped = true;
            }

            public void readProperties(LayoutParams node, PropertyReader propertyReader) {
                if (this.mPropertiesMapped) {
                    propertyReader.readGravity(this.mLayout_gravityId, node.gravity);
                    return;
                }
                throw new InspectionCompanion.UninitializedPropertyMapException();
            }
        }

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

    /* access modifiers changed from: package-private */
    public static class ItemInfo {
        Object object;
        float offset;
        int position;
        boolean scrolling;
        float widthFactor;

        ItemInfo() {
        }
    }

    public static class SimpleOnPageChangeListener implements OnPageChangeListener {
        @Override // com.android.internal.widget.ViewPager.OnPageChangeListener
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override // com.android.internal.widget.ViewPager.OnPageChangeListener
        public void onPageSelected(int position) {
        }

        @Override // com.android.internal.widget.ViewPager.OnPageChangeListener
        public void onPageScrollStateChanged(int state) {
        }
    }

    public ViewPager(Context context) {
        this(context, null);
    }

    public ViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ViewPager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mItems = new ArrayList<>();
        this.mTempItem = new ItemInfo();
        this.mTempRect = new Rect();
        this.mRestoredCurItem = -1;
        this.mRestoredAdapterState = null;
        this.mRestoredClassLoader = null;
        this.mLeftIncr = -1;
        this.mFirstOffset = -3.4028235E38f;
        this.mLastOffset = Float.MAX_VALUE;
        this.mOffscreenPageLimit = 1;
        this.mActivePointerId = -1;
        this.mFirstLayout = true;
        this.mEndScrollRunnable = new Runnable() {
            /* class com.android.internal.widget.ViewPager.AnonymousClass3 */

            public void run() {
                ViewPager.this.setScrollState(0);
                ViewPager.this.populate();
            }
        };
        this.mScrollState = 0;
        setWillNotDraw(false);
        setDescendantFocusability(262144);
        setFocusable(true);
        this.mScroller = new Scroller(context, sInterpolator);
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
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        removeCallbacks(this.mEndScrollRunnable);
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setScrollState(int newState) {
        if (this.mScrollState != newState) {
            this.mScrollState = newState;
            if (this.mPageTransformer != null) {
                enableLayers(newState != 0);
            }
            OnPageChangeListener onPageChangeListener = this.mOnPageChangeListener;
            if (onPageChangeListener != null) {
                onPageChangeListener.onPageScrollStateChanged(newState);
            }
        }
    }

    public void setAdapter(PagerAdapter adapter) {
        PagerAdapter pagerAdapter = this.mAdapter;
        if (pagerAdapter != null) {
            pagerAdapter.unregisterDataSetObserver(this.mObserver);
            this.mAdapter.startUpdate((ViewGroup) this);
            for (int i = 0; i < this.mItems.size(); i++) {
                ItemInfo ii = this.mItems.get(i);
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
            this.mAdapter.registerDataSetObserver(this.mObserver);
            this.mPopulatePending = false;
            boolean wasFirstLayout = this.mFirstLayout;
            this.mFirstLayout = true;
            this.mExpectedAdapterCount = this.mAdapter.getCount();
            if (this.mRestoredCurItem >= 0) {
                this.mAdapter.restoreState(this.mRestoredAdapterState, this.mRestoredClassLoader);
                setCurrentItemInternal(this.mRestoredCurItem, false, true);
                this.mRestoredCurItem = -1;
                this.mRestoredAdapterState = null;
                this.mRestoredClassLoader = null;
            } else if (!wasFirstLayout) {
                populate();
            } else {
                requestLayout();
            }
        }
        OnAdapterChangeListener onAdapterChangeListener = this.mAdapterChangeListener;
        if (!(onAdapterChangeListener == null || oldAdapter == adapter)) {
            onAdapterChangeListener.onAdapterChanged(oldAdapter, adapter);
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

    /* access modifiers changed from: package-private */
    public void setOnAdapterChangeListener(OnAdapterChangeListener listener) {
        this.mAdapterChangeListener = listener;
    }

    private int getPaddedWidth() {
        return (getMeasuredWidth() - getPaddingLeft()) - getPaddingRight();
    }

    public void setCurrentItem(int item) {
        this.mPopulatePending = false;
        setCurrentItemInternal(item, !this.mFirstLayout, false);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        this.mPopulatePending = false;
        setCurrentItemInternal(item, smoothScroll, false);
    }

    public int getCurrentItem() {
        return this.mCurItem;
    }

    /* access modifiers changed from: package-private */
    public boolean setCurrentItemInternal(int item, boolean smoothScroll, boolean always) {
        return setCurrentItemInternal(item, smoothScroll, always, 0);
    }

    /* access modifiers changed from: package-private */
    public boolean setCurrentItemInternal(int item, boolean smoothScroll, boolean always, int velocity) {
        OnPageChangeListener onPageChangeListener;
        OnPageChangeListener onPageChangeListener2;
        PagerAdapter pagerAdapter = this.mAdapter;
        boolean dispatchSelected = false;
        if (pagerAdapter == null || pagerAdapter.getCount() <= 0) {
            setScrollingCacheEnabled(false);
            return false;
        }
        int item2 = MathUtils.constrain(item, 0, this.mAdapter.getCount() - 1);
        if (always || this.mCurItem != item2 || this.mItems.size() == 0) {
            int pageLimit = this.mOffscreenPageLimit;
            int i = this.mCurItem;
            if (item2 > i + pageLimit || item2 < i - pageLimit) {
                for (int i2 = 0; i2 < this.mItems.size(); i2++) {
                    this.mItems.get(i2).scrolling = true;
                }
            }
            if (this.mCurItem != item2) {
                dispatchSelected = true;
            }
            if (this.mFirstLayout) {
                this.mCurItem = item2;
                if (dispatchSelected && (onPageChangeListener2 = this.mOnPageChangeListener) != null) {
                    onPageChangeListener2.onPageSelected(item2);
                }
                if (dispatchSelected && (onPageChangeListener = this.mInternalPageChangeListener) != null) {
                    onPageChangeListener.onPageSelected(item2);
                }
                requestLayout();
            } else {
                populate(item2);
                scrollToItem(item2, smoothScroll, velocity, dispatchSelected);
            }
            return true;
        }
        setScrollingCacheEnabled(false);
        return false;
    }

    private void scrollToItem(int position, boolean smoothScroll, int velocity, boolean dispatchSelected) {
        OnPageChangeListener onPageChangeListener;
        OnPageChangeListener onPageChangeListener2;
        OnPageChangeListener onPageChangeListener3;
        OnPageChangeListener onPageChangeListener4;
        int destX = getLeftEdgeForItem(position);
        if (smoothScroll) {
            smoothScrollTo(destX, 0, velocity);
            if (dispatchSelected && (onPageChangeListener4 = this.mOnPageChangeListener) != null) {
                onPageChangeListener4.onPageSelected(position);
            }
            if (dispatchSelected && (onPageChangeListener3 = this.mInternalPageChangeListener) != null) {
                onPageChangeListener3.onPageSelected(position);
                return;
            }
            return;
        }
        if (dispatchSelected && (onPageChangeListener2 = this.mOnPageChangeListener) != null) {
            onPageChangeListener2.onPageSelected(position);
        }
        if (dispatchSelected && (onPageChangeListener = this.mInternalPageChangeListener) != null) {
            onPageChangeListener.onPageSelected(position);
        }
        completeScroll(false);
        scrollTo(destX, 0);
        pageScrolled(destX);
    }

    private int getLeftEdgeForItem(int position) {
        ItemInfo info = infoForPosition(position);
        if (info == null) {
            return 0;
        }
        int width = getPaddedWidth();
        int scaledOffset = (int) (((float) width) * MathUtils.constrain(info.offset, this.mFirstOffset, this.mLastOffset));
        if (isLayoutRtl()) {
            return (16777216 - ((int) ((((float) width) * info.widthFactor) + 0.5f))) - scaledOffset;
        }
        return scaledOffset;
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.mOnPageChangeListener = listener;
    }

    public void setPageTransformer(boolean reverseDrawingOrder, PageTransformer transformer) {
        int i = 1;
        boolean hasTransformer = transformer != null;
        boolean needsPopulate = hasTransformer != (this.mPageTransformer != null);
        this.mPageTransformer = transformer;
        setChildrenDrawingOrderEnabled(hasTransformer);
        if (hasTransformer) {
            if (reverseDrawingOrder) {
                i = 2;
            }
            this.mDrawingOrder = i;
        } else {
            this.mDrawingOrder = 0;
        }
        if (needsPopulate) {
            populate();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public int getChildDrawingOrder(int childCount, int i) {
        return ((LayoutParams) this.mDrawingOrderedChildren.get(this.mDrawingOrder == 2 ? (childCount - 1) - i : i).getLayoutParams()).childIndex;
    }

    /* access modifiers changed from: package-private */
    public OnPageChangeListener setInternalPageChangeListener(OnPageChangeListener listener) {
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
        setWillNotDraw(d == null);
        invalidate();
    }

    public void setPageMarginDrawable(int resId) {
        setPageMarginDrawable(getContext().getDrawable(resId));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mMarginDrawable;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable marginDrawable = this.mMarginDrawable;
        if (marginDrawable != null && marginDrawable.isStateful() && marginDrawable.setState(getDrawableState())) {
            invalidateDrawable(marginDrawable);
        }
    }

    /* access modifiers changed from: package-private */
    public float distanceInfluenceForSnapDuration(float f) {
        return (float) Math.sin((double) ((float) (((double) (f - 0.5f)) * 0.4712389167638204d)));
    }

    /* access modifiers changed from: package-private */
    public void smoothScrollTo(int x, int y) {
        smoothScrollTo(x, y, 0);
    }

    /* access modifiers changed from: package-private */
    public void smoothScrollTo(int x, int y, int velocity) {
        int duration;
        if (getChildCount() == 0) {
            setScrollingCacheEnabled(false);
            return;
        }
        int sx = getScrollX();
        int sy = getScrollY();
        int dx = x - sx;
        int dy = y - sy;
        if (dx == 0 && dy == 0) {
            completeScroll(false);
            populate();
            setScrollState(0);
            return;
        }
        setScrollingCacheEnabled(true);
        setScrollState(2);
        int width = getPaddedWidth();
        int halfWidth = width / 2;
        float distance = ((float) halfWidth) + (((float) halfWidth) * distanceInfluenceForSnapDuration(Math.min(1.0f, (((float) Math.abs(dx)) * 1.0f) / ((float) width))));
        int velocity2 = Math.abs(velocity);
        if (velocity2 > 0) {
            duration = Math.round(Math.abs(distance / ((float) velocity2)) * 1000.0f) * 4;
        } else {
            duration = (int) ((1.0f + (((float) Math.abs(dx)) / (((float) this.mPageMargin) + (((float) width) * this.mAdapter.getPageWidth(this.mCurItem))))) * 100.0f);
        }
        this.mScroller.startScroll(sx, sy, dx, dy, Math.min(duration, 600));
        postInvalidateOnAnimation();
    }

    /* access modifiers changed from: package-private */
    public ItemInfo addNewItem(int position, int index) {
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

    /* access modifiers changed from: package-private */
    public void dataSetChanged() {
        int adapterCount = this.mAdapter.getCount();
        this.mExpectedAdapterCount = adapterCount;
        boolean needPopulate = this.mItems.size() < (this.mOffscreenPageLimit * 2) + 1 && this.mItems.size() < adapterCount;
        int newCurrItem = this.mCurItem;
        boolean isUpdating = false;
        int i = 0;
        while (i < this.mItems.size()) {
            ItemInfo ii = this.mItems.get(i);
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
            for (int i2 = 0; i2 < childCount; i2++) {
                LayoutParams lp = (LayoutParams) getChildAt(i2).getLayoutParams();
                if (!lp.isDecor) {
                    lp.widthFactor = 0.0f;
                }
            }
            setCurrentItemInternal(newCurrItem, false, true);
            requestLayout();
        }
    }

    public void populate() {
        populate(this.mCurItem);
    }

    /* JADX INFO: Multiple debug info for r0v22 float: [D('leftWidthNeeded' float), D('extraWidthRight' float)] */
    /* access modifiers changed from: package-private */
    public void populate(int newCurrentItem) {
        int focusDirection;
        ItemInfo oldCurInfo;
        String resName;
        Rect focusRect;
        ItemInfo ii;
        int curIndex;
        float f;
        float rightWidthNeeded;
        int startPos;
        int pageLimit;
        float leftWidthNeeded;
        int i = this.mCurItem;
        if (i != newCurrentItem) {
            int focusDirection2 = i < newCurrentItem ? 66 : 17;
            ItemInfo oldCurInfo2 = infoForPosition(this.mCurItem);
            this.mCurItem = newCurrentItem;
            focusDirection = focusDirection2;
            oldCurInfo = oldCurInfo2;
        } else {
            focusDirection = 2;
            oldCurInfo = null;
        }
        if (this.mAdapter == null) {
            sortChildDrawingOrder();
        } else if (this.mPopulatePending) {
            sortChildDrawingOrder();
        } else if (getWindowToken() != null) {
            this.mAdapter.startUpdate((ViewGroup) this);
            int pageLimit2 = this.mOffscreenPageLimit;
            int startPos2 = Math.max(0, this.mCurItem - pageLimit2);
            int N = this.mAdapter.getCount();
            int endPos = Math.min(N - 1, this.mCurItem + pageLimit2);
            if (N == this.mExpectedAdapterCount) {
                ItemInfo curItem = null;
                int curIndex2 = 0;
                while (true) {
                    if (curIndex2 >= this.mItems.size()) {
                        break;
                    }
                    ItemInfo ii2 = this.mItems.get(curIndex2);
                    if (ii2.position < this.mCurItem) {
                        curIndex2++;
                    } else if (ii2.position == this.mCurItem) {
                        curItem = ii2;
                    }
                }
                if (curItem == null && N > 0) {
                    curItem = addNewItem(this.mCurItem, curIndex2);
                }
                if (curItem != null) {
                    float extraWidthLeft = 0.0f;
                    int itemIndex = curIndex2 - 1;
                    ItemInfo ii3 = itemIndex >= 0 ? this.mItems.get(itemIndex) : null;
                    int clientWidth = getPaddedWidth();
                    if (clientWidth <= 0) {
                        curIndex = curIndex2;
                        f = 0.0f;
                    } else {
                        curIndex = curIndex2;
                        f = (((float) getPaddingLeft()) / ((float) clientWidth)) + (2.0f - curItem.widthFactor);
                    }
                    float leftWidthNeeded2 = f;
                    int pos = this.mCurItem - 1;
                    int curIndex3 = curIndex;
                    while (true) {
                        if (pos < 0) {
                            break;
                        }
                        if (extraWidthLeft < leftWidthNeeded2 || pos >= startPos2) {
                            leftWidthNeeded = leftWidthNeeded2;
                            if (ii3 == null || pos != ii3.position) {
                                extraWidthLeft += addNewItem(pos, itemIndex + 1).widthFactor;
                                curIndex3++;
                                ii3 = itemIndex >= 0 ? this.mItems.get(itemIndex) : null;
                            } else {
                                extraWidthLeft += ii3.widthFactor;
                                itemIndex--;
                                ii3 = itemIndex >= 0 ? this.mItems.get(itemIndex) : null;
                            }
                        } else if (ii3 == null) {
                            break;
                        } else {
                            leftWidthNeeded = leftWidthNeeded2;
                            if (pos == ii3.position && !ii3.scrolling) {
                                this.mItems.remove(itemIndex);
                                this.mAdapter.destroyItem((ViewGroup) this, pos, ii3.object);
                                itemIndex--;
                                curIndex3--;
                                ii3 = itemIndex >= 0 ? this.mItems.get(itemIndex) : null;
                            }
                        }
                        pos--;
                        leftWidthNeeded2 = leftWidthNeeded;
                    }
                    float extraWidthRight = curItem.widthFactor;
                    int itemIndex2 = curIndex3 + 1;
                    if (extraWidthRight < 2.0f) {
                        ItemInfo ii4 = itemIndex2 < this.mItems.size() ? this.mItems.get(itemIndex2) : null;
                        if (clientWidth <= 0) {
                            rightWidthNeeded = 0.0f;
                        } else {
                            rightWidthNeeded = (((float) getPaddingRight()) / ((float) clientWidth)) + 2.0f;
                        }
                        int pos2 = this.mCurItem + 1;
                        while (true) {
                            if (pos2 >= N) {
                                break;
                            }
                            if (extraWidthRight < rightWidthNeeded || pos2 <= endPos) {
                                pageLimit = pageLimit2;
                                startPos = startPos2;
                                if (ii4 == null || pos2 != ii4.position) {
                                    ItemInfo ii5 = addNewItem(pos2, itemIndex2);
                                    itemIndex2++;
                                    extraWidthRight += ii5.widthFactor;
                                    ii4 = itemIndex2 < this.mItems.size() ? this.mItems.get(itemIndex2) : null;
                                } else {
                                    extraWidthRight += ii4.widthFactor;
                                    itemIndex2++;
                                    ii4 = itemIndex2 < this.mItems.size() ? this.mItems.get(itemIndex2) : null;
                                }
                            } else if (ii4 == null) {
                                break;
                            } else {
                                pageLimit = pageLimit2;
                                if (pos2 != ii4.position || ii4.scrolling) {
                                    startPos = startPos2;
                                } else {
                                    this.mItems.remove(itemIndex2);
                                    startPos = startPos2;
                                    this.mAdapter.destroyItem((ViewGroup) this, pos2, ii4.object);
                                    ii4 = itemIndex2 < this.mItems.size() ? this.mItems.get(itemIndex2) : null;
                                }
                            }
                            pos2++;
                            pageLimit2 = pageLimit;
                            startPos2 = startPos;
                        }
                    }
                    calculatePageOffsets(curItem, curIndex3, oldCurInfo);
                }
                this.mAdapter.setPrimaryItem((ViewGroup) this, this.mCurItem, curItem != null ? curItem.object : null);
                this.mAdapter.finishUpdate((ViewGroup) this);
                int childCount = getChildCount();
                for (int i2 = 0; i2 < childCount; i2++) {
                    View child = getChildAt(i2);
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    lp.childIndex = i2;
                    if (!lp.isDecor) {
                        if (lp.widthFactor == 0.0f && (ii = infoForChild(child)) != null) {
                            lp.widthFactor = ii.widthFactor;
                            lp.position = ii.position;
                        }
                    }
                }
                sortChildDrawingOrder();
                if (hasFocus()) {
                    View currentFocused = findFocus();
                    ItemInfo ii6 = currentFocused != null ? infoForAnyChild(currentFocused) : null;
                    if (ii6 == null || ii6.position != this.mCurItem) {
                        for (int i3 = 0; i3 < getChildCount(); i3++) {
                            View child2 = getChildAt(i3);
                            ItemInfo ii7 = infoForChild(child2);
                            if (ii7 != null && ii7.position == this.mCurItem) {
                                if (currentFocused == null) {
                                    focusRect = null;
                                } else {
                                    focusRect = this.mTempRect;
                                    currentFocused.getFocusedRect(this.mTempRect);
                                    offsetDescendantRectToMyCoords(currentFocused, this.mTempRect);
                                    offsetRectIntoDescendantCoords(child2, this.mTempRect);
                                }
                                if (child2.requestFocus(focusDirection, focusRect)) {
                                    return;
                                }
                            }
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            try {
                resName = getResources().getResourceName(getId());
            } catch (Resources.NotFoundException e) {
                resName = Integer.toHexString(getId());
            }
            throw new IllegalStateException("The application's PagerAdapter changed the adapter's contents without calling PagerAdapter#notifyDataSetChanged! Expected adapter item count: " + this.mExpectedAdapterCount + ", found: " + N + " Pager id: " + resName + " Pager class: " + getClass() + " Problematic adapter: " + this.mAdapter.getClass());
        }
    }

    private void sortChildDrawingOrder() {
        if (this.mDrawingOrder != 0) {
            ArrayList<View> arrayList = this.mDrawingOrderedChildren;
            if (arrayList == null) {
                this.mDrawingOrderedChildren = new ArrayList<>();
            } else {
                arrayList.clear();
            }
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                this.mDrawingOrderedChildren.add(getChildAt(i));
            }
            Collections.sort(this.mDrawingOrderedChildren, sPositionComparator);
        }
    }

    private void calculatePageOffsets(ItemInfo curItem, int curIndex, ItemInfo oldCurInfo) {
        ItemInfo ii;
        ItemInfo ii2;
        int N = this.mAdapter.getCount();
        int width = getPaddedWidth();
        float marginOffset = width > 0 ? ((float) this.mPageMargin) / ((float) width) : 0.0f;
        if (oldCurInfo != null) {
            int oldCurPosition = oldCurInfo.position;
            if (oldCurPosition < curItem.position) {
                int itemIndex = 0;
                float offset = oldCurInfo.offset + oldCurInfo.widthFactor + marginOffset;
                int pos = oldCurPosition + 1;
                while (pos <= curItem.position && itemIndex < this.mItems.size()) {
                    ItemInfo ii3 = this.mItems.get(itemIndex);
                    while (true) {
                        ii2 = ii3;
                        if (pos > ii2.position && itemIndex < this.mItems.size() - 1) {
                            itemIndex++;
                            ii3 = this.mItems.get(itemIndex);
                        }
                    }
                    while (pos < ii2.position) {
                        offset += this.mAdapter.getPageWidth(pos) + marginOffset;
                        pos++;
                    }
                    ii2.offset = offset;
                    offset += ii2.widthFactor + marginOffset;
                    pos++;
                }
            } else if (oldCurPosition > curItem.position) {
                int itemIndex2 = this.mItems.size() - 1;
                float offset2 = oldCurInfo.offset;
                int pos2 = oldCurPosition - 1;
                while (pos2 >= curItem.position && itemIndex2 >= 0) {
                    ItemInfo ii4 = this.mItems.get(itemIndex2);
                    while (true) {
                        ii = ii4;
                        if (pos2 < ii.position && itemIndex2 > 0) {
                            itemIndex2--;
                            ii4 = this.mItems.get(itemIndex2);
                        }
                    }
                    while (pos2 > ii.position) {
                        offset2 -= this.mAdapter.getPageWidth(pos2) + marginOffset;
                        pos2--;
                    }
                    offset2 -= ii.widthFactor + marginOffset;
                    ii.offset = offset2;
                    pos2--;
                }
            }
        }
        int itemCount = this.mItems.size();
        float offset3 = curItem.offset;
        int pos3 = curItem.position - 1;
        this.mFirstOffset = curItem.position == 0 ? curItem.offset : -3.4028235E38f;
        this.mLastOffset = curItem.position == N + -1 ? (curItem.offset + curItem.widthFactor) - 1.0f : Float.MAX_VALUE;
        int i = curIndex - 1;
        while (i >= 0) {
            ItemInfo ii5 = this.mItems.get(i);
            while (pos3 > ii5.position) {
                offset3 -= this.mAdapter.getPageWidth(pos3) + marginOffset;
                pos3--;
            }
            offset3 -= ii5.widthFactor + marginOffset;
            ii5.offset = offset3;
            if (ii5.position == 0) {
                this.mFirstOffset = offset3;
            }
            i--;
            pos3--;
        }
        float offset4 = curItem.offset + curItem.widthFactor + marginOffset;
        int pos4 = curItem.position + 1;
        int i2 = curIndex + 1;
        while (i2 < itemCount) {
            ItemInfo ii6 = this.mItems.get(i2);
            while (pos4 < ii6.position) {
                offset4 += this.mAdapter.getPageWidth(pos4) + marginOffset;
                pos4++;
            }
            if (ii6.position == N - 1) {
                this.mLastOffset = (ii6.widthFactor + offset4) - 1.0f;
            }
            ii6.offset = offset4;
            offset4 += ii6.widthFactor + marginOffset;
            i2++;
            pos4++;
        }
    }

    public static class SavedState extends AbsSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.ClassLoaderCreator<SavedState>() {
            /* class com.android.internal.widget.ViewPager.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.ClassLoaderCreator
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            @Override // android.os.Parcelable.Creator
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

        @Override // android.os.Parcelable, android.view.AbsSavedState
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.position);
            out.writeParcelable(this.adapterState, flags);
        }

        public String toString() {
            return "FragmentPager.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " position=" + this.position + "}";
        }

        SavedState(Parcel in, ClassLoader loader2) {
            super(in, loader2);
            loader2 = loader2 == null ? getClass().getClassLoader() : loader2;
            this.position = in.readInt();
            this.adapterState = in.readParcelable(loader2);
            this.loader = loader2;
        }
    }

    @Override // android.view.View
    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.position = this.mCurItem;
        PagerAdapter pagerAdapter = this.mAdapter;
        if (pagerAdapter != null) {
            ss.adapterState = pagerAdapter.saveState();
        }
        return ss;
    }

    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        PagerAdapter pagerAdapter = this.mAdapter;
        if (pagerAdapter != null) {
            pagerAdapter.restoreState(ss.adapterState, ss.loader);
            setCurrentItemInternal(ss.position, false, true);
            return;
        }
        this.mRestoredCurItem = ss.position;
        this.mRestoredAdapterState = ss.adapterState;
        this.mRestoredClassLoader = ss.loader;
    }

    @Override // android.view.ViewGroup
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!checkLayoutParams(params)) {
            params = generateLayoutParams(params);
        }
        LayoutParams lp = (LayoutParams) params;
        lp.isDecor |= child instanceof Decor;
        if (!this.mInLayout) {
            super.addView(child, index, params);
        } else if (!lp.isDecor) {
            lp.needsMeasure = true;
            addViewInLayout(child, index, params);
        } else {
            throw new IllegalStateException("Cannot add pager decor view during layout");
        }
    }

    public Object getCurrent() {
        ItemInfo itemInfo = infoForPosition(getCurrentItem());
        if (itemInfo == null) {
            return null;
        }
        return itemInfo.object;
    }

    @Override // android.view.ViewGroup, android.view.ViewManager
    public void removeView(View view) {
        if (this.mInLayout) {
            removeViewInLayout(view);
        } else {
            super.removeView(view);
        }
    }

    /* access modifiers changed from: package-private */
    public ItemInfo infoForChild(View child) {
        for (int i = 0; i < this.mItems.size(); i++) {
            ItemInfo ii = this.mItems.get(i);
            if (this.mAdapter.isViewFromObject(child, ii.object)) {
                return ii;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ItemInfo infoForAnyChild(View child) {
        while (true) {
            ViewParent parent = child.getParent();
            if (parent == this) {
                return infoForChild(child);
            }
            if (parent == null || !(parent instanceof View)) {
                return null;
            }
            child = (View) parent;
        }
    }

    /* access modifiers changed from: package-private */
    public ItemInfo infoForPosition(int position) {
        for (int i = 0; i < this.mItems.size(); i++) {
            ItemInfo ii = this.mItems.get(i);
            if (ii.position == position) {
                return ii;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mFirstLayout = true;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        LayoutParams lp;
        int measuredWidth;
        int heightMode;
        int widthSize;
        int heightMode2;
        int heightSize;
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        int measuredWidth2 = getMeasuredWidth();
        int maxGutterSize = measuredWidth2 / 10;
        this.mGutterSize = Math.min(maxGutterSize, this.mDefaultGutterSize);
        int childWidthSize = (measuredWidth2 - getPaddingLeft()) - getPaddingRight();
        int childHeightSize = (getMeasuredHeight() - getPaddingTop()) - getPaddingBottom();
        int size = getChildCount();
        int i = 0;
        while (i < size) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams lp2 = (LayoutParams) child.getLayoutParams();
                if (lp2 == null || !lp2.isDecor) {
                    measuredWidth = measuredWidth2;
                    heightMode = maxGutterSize;
                } else {
                    int hgrav = lp2.gravity & 7;
                    int vgrav = lp2.gravity & 112;
                    int widthMode = Integer.MIN_VALUE;
                    int heightMode3 = Integer.MIN_VALUE;
                    boolean consumeVertical = vgrav == 48 || vgrav == 80;
                    boolean consumeHorizontal = hgrav == 3 || hgrav == 5;
                    if (consumeVertical) {
                        widthMode = 1073741824;
                    } else if (consumeHorizontal) {
                        heightMode3 = 1073741824;
                    }
                    measuredWidth = measuredWidth2;
                    if (lp2.width != -2) {
                        widthMode = 1073741824;
                        if (lp2.width != -1) {
                            widthSize = lp2.width;
                        } else {
                            widthSize = childWidthSize;
                        }
                    } else {
                        widthSize = childWidthSize;
                    }
                    if (lp2.height == -2) {
                        heightMode2 = heightMode3;
                        heightSize = childHeightSize;
                    } else if (lp2.height != -1) {
                        heightSize = lp2.height;
                        heightMode2 = 1073741824;
                    } else {
                        heightMode2 = 1073741824;
                        heightSize = childHeightSize;
                    }
                    heightMode = maxGutterSize;
                    child.measure(View.MeasureSpec.makeMeasureSpec(widthSize, widthMode), View.MeasureSpec.makeMeasureSpec(heightSize, heightMode2));
                    if (consumeVertical) {
                        childHeightSize -= child.getMeasuredHeight();
                    } else if (consumeHorizontal) {
                        childWidthSize -= child.getMeasuredWidth();
                    }
                }
            } else {
                measuredWidth = measuredWidth2;
                heightMode = maxGutterSize;
            }
            i++;
            maxGutterSize = heightMode;
            measuredWidth2 = measuredWidth;
        }
        this.mChildWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(childWidthSize, 1073741824);
        this.mChildHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(childHeightSize, 1073741824);
        this.mInLayout = true;
        populate();
        this.mInLayout = false;
        int size2 = getChildCount();
        for (int i2 = 0; i2 < size2; i2++) {
            View child2 = getChildAt(i2);
            if (child2.getVisibility() != 8 && ((lp = (LayoutParams) child2.getLayoutParams()) == null || !lp.isDecor)) {
                child2.measure(View.MeasureSpec.makeMeasureSpec((int) (((float) childWidthSize) * lp.widthFactor), 1073741824), this.mChildHeightMeasureSpec);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw) {
            int i = this.mPageMargin;
            recomputeScrollPosition(w, oldw, i, i);
        }
    }

    private void recomputeScrollPosition(int width, int oldWidth, int margin, int oldMargin) {
        if (oldWidth <= 0 || this.mItems.isEmpty()) {
            ItemInfo ii = infoForPosition(this.mCurItem);
            int scrollPos = (int) (((float) ((width - getPaddingLeft()) - getPaddingRight())) * (ii != null ? Math.min(ii.offset, this.mLastOffset) : 0.0f));
            if (scrollPos != getScrollX()) {
                completeScroll(false);
                scrollTo(scrollPos, getScrollY());
                return;
            }
            return;
        }
        int newOffsetPixels = (int) (((float) (((width - getPaddingLeft()) - getPaddingRight()) + margin)) * (((float) getScrollX()) / ((float) (((oldWidth - getPaddingLeft()) - getPaddingRight()) + oldMargin))));
        scrollTo(newOffsetPixels, getScrollY());
        if (!this.mScroller.isFinished()) {
            this.mScroller.startScroll(newOffsetPixels, 0, (int) (infoForPosition(this.mCurItem).offset * ((float) width)), 0, this.mScroller.getDuration() - this.mScroller.timePassed());
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int i;
        boolean z;
        int paddingLeft;
        int width;
        int count;
        int childLeft;
        int childLeft2;
        int childTop;
        int count2 = getChildCount();
        int width2 = r - l;
        int height = b - t;
        int paddingLeft2 = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int scrollX = getScrollX();
        int decorCount = 0;
        int i2 = 0;
        while (true) {
            i = 8;
            if (i2 >= count2) {
                break;
            }
            View child = getChildAt(i2);
            if (child.getVisibility() != 8) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.isDecor) {
                    int hgrav = lp.gravity & 7;
                    int vgrav = lp.gravity & 112;
                    if (hgrav == 1) {
                        childLeft2 = Math.max((width2 - child.getMeasuredWidth()) / 2, paddingLeft2);
                    } else if (hgrav == 3) {
                        childLeft2 = paddingLeft2;
                        paddingLeft2 += child.getMeasuredWidth();
                    } else if (hgrav != 5) {
                        childLeft2 = paddingLeft2;
                    } else {
                        childLeft2 = (width2 - paddingRight) - child.getMeasuredWidth();
                        paddingRight += child.getMeasuredWidth();
                    }
                    if (vgrav == 16) {
                        childTop = Math.max((height - child.getMeasuredHeight()) / 2, paddingTop);
                    } else if (vgrav == 48) {
                        childTop = paddingTop;
                        paddingTop += child.getMeasuredHeight();
                    } else if (vgrav != 80) {
                        childTop = paddingTop;
                    } else {
                        childTop = (height - paddingBottom) - child.getMeasuredHeight();
                        paddingBottom += child.getMeasuredHeight();
                    }
                    int childLeft3 = childLeft2 + scrollX;
                    child.layout(childLeft3, childTop, child.getMeasuredWidth() + childLeft3, childTop + child.getMeasuredHeight());
                    decorCount++;
                    paddingLeft2 = paddingLeft2;
                    paddingTop = paddingTop;
                }
            }
            i2++;
        }
        int childWidth = (width2 - paddingLeft2) - paddingRight;
        int i3 = 0;
        while (i3 < count2) {
            View child2 = getChildAt(i3);
            if (child2.getVisibility() == i) {
                count = count2;
                width = width2;
                paddingLeft = paddingLeft2;
            } else {
                LayoutParams lp2 = (LayoutParams) child2.getLayoutParams();
                if (lp2.isDecor) {
                    count = count2;
                    width = width2;
                    paddingLeft = paddingLeft2;
                } else {
                    ItemInfo ii = infoForChild(child2);
                    if (ii == null) {
                        count = count2;
                        width = width2;
                        paddingLeft = paddingLeft2;
                    } else {
                        if (lp2.needsMeasure) {
                            lp2.needsMeasure = false;
                            count = count2;
                            width = width2;
                            child2.measure(View.MeasureSpec.makeMeasureSpec((int) (((float) childWidth) * lp2.widthFactor), 1073741824), View.MeasureSpec.makeMeasureSpec((height - paddingTop) - paddingBottom, 1073741824));
                        } else {
                            count = count2;
                            width = width2;
                        }
                        int childMeasuredWidth = child2.getMeasuredWidth();
                        int startOffset = (int) (((float) childWidth) * ii.offset);
                        if (isLayoutRtl()) {
                            childLeft = ((16777216 - paddingRight) - startOffset) - childMeasuredWidth;
                        } else {
                            childLeft = paddingLeft2 + startOffset;
                        }
                        paddingLeft = paddingLeft2;
                        child2.layout(childLeft, paddingTop, childLeft + childMeasuredWidth, paddingTop + child2.getMeasuredHeight());
                    }
                }
            }
            i3++;
            count2 = count;
            width2 = width;
            paddingLeft2 = paddingLeft;
            i = 8;
        }
        this.mTopPageBounds = paddingTop;
        this.mBottomPageBounds = height - paddingBottom;
        this.mDecorChildCount = decorCount;
        if (this.mFirstLayout) {
            z = false;
            scrollToItem(this.mCurItem, false, 0, false);
        } else {
            z = false;
        }
        this.mFirstLayout = z;
    }

    @Override // android.view.View
    public void computeScroll() {
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
        postInvalidateOnAnimation();
    }

    private boolean pageScrolled(int scrollX) {
        int scrollStart;
        if (this.mItems.size() == 0) {
            this.mCalledSuper = false;
            onPageScrolled(0, 0.0f, 0);
            if (this.mCalledSuper) {
                return false;
            }
            throw new IllegalStateException("onPageScrolled did not call superclass implementation");
        }
        if (isLayoutRtl()) {
            scrollStart = 16777216 - scrollX;
        } else {
            scrollStart = scrollX;
        }
        ItemInfo ii = infoForFirstVisiblePage();
        int width = getPaddedWidth();
        int i = this.mPageMargin;
        int widthWithMargin = width + i;
        int currentPage = ii.position;
        float pageOffset = ((((float) scrollStart) / ((float) width)) - ii.offset) / (ii.widthFactor + (((float) i) / ((float) width)));
        this.mCalledSuper = false;
        onPageScrolled(currentPage, pageOffset, (int) (((float) widthWithMargin) * pageOffset));
        if (this.mCalledSuper) {
            return true;
        }
        throw new IllegalStateException("onPageScrolled did not call superclass implementation");
    }

    /* access modifiers changed from: protected */
    public void onPageScrolled(int position, float offset, int offsetPixels) {
        int childLeft;
        if (this.mDecorChildCount > 0) {
            int scrollX = getScrollX();
            int paddingLeft = getPaddingLeft();
            int paddingRight = getPaddingRight();
            int width = getWidth();
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.isDecor) {
                    int hgrav = lp.gravity & 7;
                    if (hgrav == 1) {
                        childLeft = Math.max((width - child.getMeasuredWidth()) / 2, paddingLeft);
                    } else if (hgrav == 3) {
                        childLeft = paddingLeft;
                        paddingLeft += child.getWidth();
                    } else if (hgrav != 5) {
                        childLeft = paddingLeft;
                    } else {
                        childLeft = (width - paddingRight) - child.getMeasuredWidth();
                        paddingRight += child.getMeasuredWidth();
                    }
                    int childOffset = (childLeft + scrollX) - child.getLeft();
                    if (childOffset != 0) {
                        child.offsetLeftAndRight(childOffset);
                    }
                }
            }
        }
        OnPageChangeListener onPageChangeListener = this.mOnPageChangeListener;
        if (onPageChangeListener != null) {
            onPageChangeListener.onPageScrolled(position, offset, offsetPixels);
        }
        OnPageChangeListener onPageChangeListener2 = this.mInternalPageChangeListener;
        if (onPageChangeListener2 != null) {
            onPageChangeListener2.onPageScrolled(position, offset, offsetPixels);
        }
        if (this.mPageTransformer != null) {
            int scrollX2 = getScrollX();
            int childCount2 = getChildCount();
            for (int i2 = 0; i2 < childCount2; i2++) {
                View child2 = getChildAt(i2);
                if (!((LayoutParams) child2.getLayoutParams()).isDecor) {
                    this.mPageTransformer.transformPage(child2, ((float) (child2.getLeft() - scrollX2)) / ((float) getPaddedWidth()));
                }
            }
        }
        this.mCalledSuper = true;
    }

    private void completeScroll(boolean postEvents) {
        boolean needPopulate = this.mScrollState == 2;
        if (needPopulate) {
            setScrollingCacheEnabled(false);
            this.mScroller.abortAnimation();
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = this.mScroller.getCurrX();
            int y = this.mScroller.getCurrY();
            if (!(oldX == x && oldY == y)) {
                scrollTo(x, y);
            }
        }
        this.mPopulatePending = false;
        for (int i = 0; i < this.mItems.size(); i++) {
            ItemInfo ii = this.mItems.get(i);
            if (ii.scrolling) {
                needPopulate = true;
                ii.scrolling = false;
            }
        }
        if (!needPopulate) {
            return;
        }
        if (postEvents) {
            postOnAnimation(this.mEndScrollRunnable);
        } else {
            this.mEndScrollRunnable.run();
        }
    }

    private boolean isGutterDrag(float x, float dx) {
        return (x < ((float) this.mGutterSize) && dx > 0.0f) || (x > ((float) (getWidth() - this.mGutterSize)) && dx < 0.0f);
    }

    private void enableLayers(boolean enable) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).setLayerType(enable ? 2 : 0, null);
        }
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        float y;
        float f;
        int action = ev.getAction() & 255;
        if (action == 3 || action == 1) {
            this.mIsBeingDragged = false;
            this.mIsUnableToDrag = false;
            this.mActivePointerId = -1;
            VelocityTracker velocityTracker = this.mVelocityTracker;
            if (velocityTracker == null) {
                return false;
            }
            velocityTracker.recycle();
            this.mVelocityTracker = null;
            return false;
        }
        if (action != 0) {
            if (this.mIsBeingDragged) {
                return true;
            }
            if (this.mIsUnableToDrag) {
                return false;
            }
        }
        if (action == 0) {
            float x = ev.getX();
            this.mInitialMotionX = x;
            this.mLastMotionX = x;
            float y2 = ev.getY();
            this.mInitialMotionY = y2;
            this.mLastMotionY = y2;
            this.mActivePointerId = ev.getPointerId(0);
            this.mIsUnableToDrag = false;
            this.mScroller.computeScrollOffset();
            if (this.mScrollState != 2 || Math.abs(this.mScroller.getFinalX() - this.mScroller.getCurrX()) <= this.mCloseEnough) {
                completeScroll(false);
                this.mIsBeingDragged = false;
            } else {
                this.mScroller.abortAnimation();
                this.mPopulatePending = false;
                populate();
                this.mIsBeingDragged = true;
                requestParentDisallowInterceptTouchEvent(true);
                setScrollState(1);
            }
        } else if (action == 2) {
            int activePointerId = this.mActivePointerId;
            if (activePointerId != -1) {
                int pointerIndex = ev.findPointerIndex(activePointerId);
                float x2 = ev.getX(pointerIndex);
                float dx = x2 - this.mLastMotionX;
                float xDiff = Math.abs(dx);
                float y3 = ev.getY(pointerIndex);
                float yDiff = Math.abs(y3 - this.mInitialMotionY);
                if (dx == 0.0f || isGutterDrag(this.mLastMotionX, dx)) {
                    y = y3;
                } else {
                    y = y3;
                    if (canScroll(this, false, (int) dx, (int) x2, (int) y3)) {
                        this.mLastMotionX = x2;
                        this.mLastMotionY = y;
                        this.mIsUnableToDrag = true;
                        return false;
                    }
                }
                if (xDiff > ((float) this.mTouchSlop) && 0.5f * xDiff > yDiff) {
                    this.mIsBeingDragged = true;
                    requestParentDisallowInterceptTouchEvent(true);
                    setScrollState(1);
                    if (dx > 0.0f) {
                        f = this.mInitialMotionX + ((float) this.mTouchSlop);
                    } else {
                        f = this.mInitialMotionX - ((float) this.mTouchSlop);
                    }
                    this.mLastMotionX = f;
                    this.mLastMotionY = y;
                    setScrollingCacheEnabled(true);
                } else if (yDiff > ((float) this.mTouchSlop)) {
                    this.mIsUnableToDrag = true;
                }
                if (this.mIsBeingDragged && performDrag(x2)) {
                    postInvalidateOnAnimation();
                }
            }
        } else if (action == 6) {
            onSecondaryPointerUp(ev);
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(ev);
        return this.mIsBeingDragged;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        PagerAdapter pagerAdapter;
        float nextPageOffset;
        float f;
        if ((ev.getAction() == 0 && ev.getEdgeFlags() != 0) || (pagerAdapter = this.mAdapter) == null || pagerAdapter.getCount() == 0) {
            return false;
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(ev);
        boolean needsInvalidate = false;
        int action = ev.getAction() & 255;
        if (action == 0) {
            this.mScroller.abortAnimation();
            this.mPopulatePending = false;
            populate();
            float x = ev.getX();
            this.mInitialMotionX = x;
            this.mLastMotionX = x;
            float y = ev.getY();
            this.mInitialMotionY = y;
            this.mLastMotionY = y;
            this.mActivePointerId = ev.getPointerId(0);
        } else if (action != 1) {
            if (action == 2) {
                if (!this.mIsBeingDragged) {
                    int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                    float x2 = ev.getX(pointerIndex);
                    float xDiff = Math.abs(x2 - this.mLastMotionX);
                    float y2 = ev.getY(pointerIndex);
                    float yDiff = Math.abs(y2 - this.mLastMotionY);
                    if (xDiff > ((float) this.mTouchSlop) && xDiff > yDiff) {
                        this.mIsBeingDragged = true;
                        requestParentDisallowInterceptTouchEvent(true);
                        float f2 = this.mInitialMotionX;
                        if (x2 - f2 > 0.0f) {
                            f = f2 + ((float) this.mTouchSlop);
                        } else {
                            f = f2 - ((float) this.mTouchSlop);
                        }
                        this.mLastMotionX = f;
                        this.mLastMotionY = y2;
                        setScrollState(1);
                        setScrollingCacheEnabled(true);
                        ViewParent parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                }
                if (this.mIsBeingDragged) {
                    needsInvalidate = false | performDrag(ev.getX(ev.findPointerIndex(this.mActivePointerId)));
                }
            } else if (action != 3) {
                if (action == 5) {
                    int index = ev.getActionIndex();
                    this.mLastMotionX = ev.getX(index);
                    this.mActivePointerId = ev.getPointerId(index);
                } else if (action == 6) {
                    onSecondaryPointerUp(ev);
                    this.mLastMotionX = ev.getX(ev.findPointerIndex(this.mActivePointerId));
                }
            } else if (this.mIsBeingDragged) {
                scrollToItem(this.mCurItem, true, 0, false);
                this.mActivePointerId = -1;
                endDrag();
                this.mLeftEdge.onRelease();
                this.mRightEdge.onRelease();
                needsInvalidate = true;
            }
        } else if (this.mIsBeingDragged) {
            VelocityTracker velocityTracker = this.mVelocityTracker;
            velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
            int initialVelocity = (int) velocityTracker.getXVelocity(this.mActivePointerId);
            this.mPopulatePending = true;
            float scrolledPages = ((float) getScrollStart()) / ((float) getPaddedWidth());
            ItemInfo ii = infoForFirstVisiblePage();
            int currentPage = ii.position;
            if (isLayoutRtl()) {
                nextPageOffset = (ii.offset - scrolledPages) / ii.widthFactor;
            } else {
                nextPageOffset = (scrolledPages - ii.offset) / ii.widthFactor;
            }
            setCurrentItemInternal(determineTargetPage(currentPage, nextPageOffset, initialVelocity, (int) (ev.getX(ev.findPointerIndex(this.mActivePointerId)) - this.mInitialMotionX)), true, true, initialVelocity);
            this.mActivePointerId = -1;
            endDrag();
            this.mLeftEdge.onRelease();
            this.mRightEdge.onRelease();
            needsInvalidate = true;
        }
        if (!needsInvalidate) {
            return true;
        }
        postInvalidateOnAnimation();
        return true;
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private boolean performDrag(float x) {
        EdgeEffect endEdge;
        EdgeEffect startEdge;
        float scrollStart;
        float startBound;
        float endBound;
        float clampedScrollStart;
        float targetScrollX;
        boolean needsInvalidate = false;
        int width = getPaddedWidth();
        float deltaX = this.mLastMotionX - x;
        this.mLastMotionX = x;
        if (isLayoutRtl()) {
            startEdge = this.mRightEdge;
            endEdge = this.mLeftEdge;
        } else {
            startEdge = this.mLeftEdge;
            endEdge = this.mRightEdge;
        }
        float nextScrollX = ((float) getScrollX()) + deltaX;
        if (isLayoutRtl()) {
            scrollStart = 1.6777216E7f - nextScrollX;
        } else {
            scrollStart = nextScrollX;
        }
        ItemInfo startItem = this.mItems.get(0);
        boolean startAbsolute = startItem.position == 0;
        if (startAbsolute) {
            startBound = startItem.offset * ((float) width);
        } else {
            startBound = ((float) width) * this.mFirstOffset;
        }
        ArrayList<ItemInfo> arrayList = this.mItems;
        ItemInfo endItem = arrayList.get(arrayList.size() - 1);
        boolean endAbsolute = endItem.position == this.mAdapter.getCount() - 1;
        if (endAbsolute) {
            endBound = endItem.offset * ((float) width);
        } else {
            endBound = ((float) width) * this.mLastOffset;
        }
        if (scrollStart < startBound) {
            if (startAbsolute) {
                startEdge.onPull(Math.abs(startBound - scrollStart) / ((float) width));
                needsInvalidate = true;
            }
            clampedScrollStart = startBound;
        } else if (scrollStart > endBound) {
            if (endAbsolute) {
                endEdge.onPull(Math.abs(scrollStart - endBound) / ((float) width));
                needsInvalidate = true;
            }
            clampedScrollStart = endBound;
        } else {
            clampedScrollStart = scrollStart;
        }
        if (isLayoutRtl()) {
            targetScrollX = 1.6777216E7f - clampedScrollStart;
        } else {
            targetScrollX = clampedScrollStart;
        }
        this.mLastMotionX += targetScrollX - ((float) ((int) targetScrollX));
        scrollTo((int) targetScrollX, getScrollY());
        pageScrolled((int) targetScrollX);
        return needsInvalidate;
    }

    private ItemInfo infoForFirstVisiblePage() {
        int startOffset = getScrollStart();
        int width = getPaddedWidth();
        float marginOffset = 0.0f;
        float scrollOffset = width > 0 ? ((float) startOffset) / ((float) width) : 0.0f;
        if (width > 0) {
            marginOffset = ((float) this.mPageMargin) / ((float) width);
        }
        int lastPos = -1;
        float lastOffset = 0.0f;
        float lastWidth = 0.0f;
        boolean first = true;
        ItemInfo lastItem = null;
        int N = this.mItems.size();
        int i = 0;
        while (i < N) {
            ItemInfo ii = this.mItems.get(i);
            if (!first && ii.position != lastPos + 1) {
                ii = this.mTempItem;
                ii.offset = lastOffset + lastWidth + marginOffset;
                ii.position = lastPos + 1;
                ii.widthFactor = this.mAdapter.getPageWidth(ii.position);
                i--;
            }
            float offset = ii.offset;
            if (!first && scrollOffset < offset) {
                return lastItem;
            }
            if (scrollOffset >= ii.widthFactor + offset + marginOffset) {
                if (i != this.mItems.size() - 1) {
                    first = false;
                    lastPos = ii.position;
                    lastOffset = offset;
                    lastWidth = ii.widthFactor;
                    lastItem = ii;
                    i++;
                    startOffset = startOffset;
                }
            }
            return ii;
        }
        return lastItem;
    }

    private int getScrollStart() {
        if (isLayoutRtl()) {
            return 16777216 - getScrollX();
        }
        return getScrollX();
    }

    private int determineTargetPage(int currentPage, float pageOffset, int velocity, int deltaX) {
        int targetPage;
        if (Math.abs(deltaX) <= this.mFlingDistance || Math.abs(velocity) <= this.mMinimumVelocity) {
            targetPage = (int) (((float) currentPage) - (((float) this.mLeftIncr) * (pageOffset + (currentPage >= this.mCurItem ? 0.4f : 0.6f))));
        } else {
            targetPage = currentPage - (velocity < 0 ? this.mLeftIncr : 0);
        }
        if (this.mItems.size() <= 0) {
            return targetPage;
        }
        ArrayList<ItemInfo> arrayList = this.mItems;
        return MathUtils.constrain(targetPage, this.mItems.get(0).position, arrayList.get(arrayList.size() - 1).position);
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        PagerAdapter pagerAdapter;
        super.draw(canvas);
        boolean needsInvalidate = false;
        int overScrollMode = getOverScrollMode();
        if (overScrollMode == 0 || (overScrollMode == 1 && (pagerAdapter = this.mAdapter) != null && pagerAdapter.getCount() > 1)) {
            if (!this.mLeftEdge.isFinished()) {
                int restoreCount = canvas.save();
                int height = (getHeight() - getPaddingTop()) - getPaddingBottom();
                int width = getWidth();
                canvas.rotate(270.0f);
                canvas.translate((float) ((-height) + getPaddingTop()), this.mFirstOffset * ((float) width));
                this.mLeftEdge.setSize(height, width);
                needsInvalidate = false | this.mLeftEdge.draw(canvas);
                canvas.restoreToCount(restoreCount);
            }
            if (!this.mRightEdge.isFinished()) {
                int restoreCount2 = canvas.save();
                int width2 = getWidth();
                int height2 = (getHeight() - getPaddingTop()) - getPaddingBottom();
                canvas.rotate(90.0f);
                canvas.translate((float) (-getPaddingTop()), (-(this.mLastOffset + 1.0f)) * ((float) width2));
                this.mRightEdge.setSize(height2, width2);
                needsInvalidate |= this.mRightEdge.draw(canvas);
                canvas.restoreToCount(restoreCount2);
            }
        } else {
            this.mLeftEdge.finish();
            this.mRightEdge.finish();
        }
        if (needsInvalidate) {
            postInvalidateOnAnimation();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        float widthFactor;
        float itemOffset;
        float left;
        float offset;
        ItemInfo ii;
        super.onDraw(canvas);
        if (this.mPageMargin > 0 && this.mMarginDrawable != null && this.mItems.size() > 0 && this.mAdapter != null) {
            int scrollX = getScrollX();
            int width = getWidth();
            float marginOffset = ((float) this.mPageMargin) / ((float) width);
            int itemIndex = 0;
            ItemInfo ii2 = this.mItems.get(0);
            float offset2 = ii2.offset;
            int itemCount = this.mItems.size();
            int firstPos = ii2.position;
            int lastPos = this.mItems.get(itemCount - 1).position;
            int pos = firstPos;
            while (pos < lastPos) {
                while (pos > ii2.position && itemIndex < itemCount) {
                    itemIndex++;
                    ii2 = this.mItems.get(itemIndex);
                }
                if (pos == ii2.position) {
                    itemOffset = ii2.offset;
                    widthFactor = ii2.widthFactor;
                } else {
                    itemOffset = offset2;
                    widthFactor = this.mAdapter.getPageWidth(pos);
                }
                float scaledOffset = ((float) width) * itemOffset;
                if (isLayoutRtl()) {
                    left = 1.6777216E7f - scaledOffset;
                } else {
                    left = (((float) width) * widthFactor) + scaledOffset;
                }
                float offset3 = itemOffset + widthFactor + marginOffset;
                int i = this.mPageMargin;
                if (((float) i) + left > ((float) scrollX)) {
                    ii = ii2;
                    offset = offset3;
                    this.mMarginDrawable.setBounds((int) left, this.mTopPageBounds, (int) (((float) i) + left + 0.5f), this.mBottomPageBounds);
                    this.mMarginDrawable.draw(canvas);
                } else {
                    ii = ii2;
                    offset = offset3;
                }
                if (left <= ((float) (scrollX + width))) {
                    pos++;
                    marginOffset = marginOffset;
                    itemIndex = itemIndex;
                    ii2 = ii;
                    offset2 = offset;
                } else {
                    return;
                }
            }
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = ev.getActionIndex();
        if (ev.getPointerId(pointerIndex) == this.mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            this.mLastMotionX = ev.getX(newPointerIndex);
            this.mActivePointerId = ev.getPointerId(newPointerIndex);
            VelocityTracker velocityTracker = this.mVelocityTracker;
            if (velocityTracker != null) {
                velocityTracker.clear();
            }
        }
    }

    private void endDrag() {
        this.mIsBeingDragged = false;
        this.mIsUnableToDrag = false;
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void setScrollingCacheEnabled(boolean enabled) {
        if (this.mScrollingCacheEnabled != enabled) {
            this.mScrollingCacheEnabled = enabled;
        }
    }

    @Override // android.view.View
    public boolean canScrollHorizontally(int direction) {
        if (this.mAdapter == null) {
            return false;
        }
        int width = getPaddedWidth();
        int scrollX = getScrollX();
        if (direction < 0) {
            if (scrollX > ((int) (((float) width) * this.mFirstOffset))) {
                return true;
            }
            return false;
        } else if (direction <= 0 || scrollX >= ((int) (((float) width) * this.mLastOffset))) {
            return false;
        } else {
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            int scrollX = v.getScrollX();
            int scrollY = v.getScrollY();
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() && y + scrollY >= child.getTop() && y + scrollY < child.getBottom() && canScroll(child, true, dx, (x + scrollX) - child.getLeft(), (y + scrollY) - child.getTop())) {
                    return true;
                }
            }
        }
        if (checkV) {
            if (v.canScrollHorizontally(-dx)) {
                return true;
            }
        }
        return false;
    }

    @Override // android.view.View, android.view.ViewGroup
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event) || executeKeyEvent(event);
    }

    public boolean executeKeyEvent(KeyEvent event) {
        if (event.getAction() != 0) {
            return false;
        }
        int keyCode = event.getKeyCode();
        if (keyCode == 21) {
            return arrowScroll(17);
        }
        if (keyCode == 22) {
            return arrowScroll(66);
        }
        if (keyCode != 61) {
            return false;
        }
        if (event.hasNoModifiers()) {
            return arrowScroll(2);
        }
        if (event.hasModifiers(1)) {
            return arrowScroll(1);
        }
        return false;
    }

    public boolean arrowScroll(int direction) {
        View currentFocused = findFocus();
        if (currentFocused == this) {
            currentFocused = null;
        } else if (currentFocused != null) {
            boolean isChild = false;
            ViewParent parent = currentFocused.getParent();
            while (true) {
                if (!(parent instanceof ViewGroup)) {
                    break;
                } else if (parent == this) {
                    isChild = true;
                    break;
                } else {
                    parent = parent.getParent();
                }
            }
            if (!isChild) {
                StringBuilder sb = new StringBuilder();
                sb.append(currentFocused.getClass().getSimpleName());
                for (ViewParent parent2 = currentFocused.getParent(); parent2 instanceof ViewGroup; parent2 = parent2.getParent()) {
                    sb.append(" => ");
                    sb.append(parent2.getClass().getSimpleName());
                }
                Log.e(TAG, "arrowScroll tried to find focus based on non-child current focused view " + sb.toString());
                currentFocused = null;
            }
        }
        boolean handled = false;
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
        ViewParent parent = child.getParent();
        while ((parent instanceof ViewGroup) && parent != this) {
            ViewGroup group = (ViewGroup) parent;
            outRect.left += group.getLeft();
            outRect.right += group.getRight();
            outRect.top += group.getTop();
            outRect.bottom += group.getBottom();
            parent = group.getParent();
        }
        return outRect;
    }

    /* access modifiers changed from: package-private */
    public boolean pageLeft() {
        return setCurrentItemInternal(this.mCurItem + this.mLeftIncr, true, false);
    }

    /* access modifiers changed from: package-private */
    public boolean pageRight() {
        return setCurrentItemInternal(this.mCurItem - this.mLeftIncr, true, false);
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        if (layoutDirection == 0) {
            this.mLeftIncr = -1;
        } else {
            this.mLeftIncr = 1;
        }
    }

    @Override // android.view.View, android.view.ViewGroup
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        ItemInfo ii;
        int focusableCount = views.size();
        int descendantFocusability = getDescendantFocusability();
        if (descendantFocusability != 393216) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == 0 && (ii = infoForChild(child)) != null && ii.position == this.mCurItem) {
                    child.addFocusables(views, direction, focusableMode);
                }
            }
        }
        if ((descendantFocusability == 262144 && focusableCount != views.size()) || !isFocusable()) {
            return;
        }
        if ((focusableMode & 1) != 1 || !isInTouchMode() || isFocusableInTouchMode()) {
            views.add(this);
        }
    }

    @Override // android.view.View, android.view.ViewGroup
    public void addTouchables(ArrayList<View> views) {
        ItemInfo ii;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == 0 && (ii = infoForChild(child)) != null && ii.position == this.mCurItem) {
                child.addTouchables(views);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        int end;
        int increment;
        int index;
        ItemInfo ii;
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
            if (child.getVisibility() == 0 && (ii = infoForChild(child)) != null && ii.position == this.mCurItem && child.requestFocus(direction, previouslyFocusedRect)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return generateDefaultLayoutParams();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return (p instanceof LayoutParams) && super.checkLayoutParams(p);
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        PagerAdapter pagerAdapter;
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(ViewPager.class.getName());
        event.setScrollable(canScroll());
        if (event.getEventType() == 4096 && (pagerAdapter = this.mAdapter) != null) {
            event.setItemCount(pagerAdapter.getCount());
            event.setFromIndex(this.mCurItem);
            event.setToIndex(this.mCurItem);
        }
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(ViewPager.class.getName());
        info.setScrollable(canScroll());
        if (canScrollHorizontally(1)) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_RIGHT);
        }
        if (canScrollHorizontally(-1)) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_LEFT);
        }
    }

    @Override // android.view.View
    public boolean performAccessibilityAction(int action, Bundle args) {
        if (super.performAccessibilityAction(action, args)) {
            return true;
        }
        if (action != 4096) {
            if (action == 8192 || action == 16908345) {
                if (!canScrollHorizontally(-1)) {
                    return false;
                }
                setCurrentItem(this.mCurItem - 1);
                return true;
            } else if (action != 16908347) {
                return false;
            }
        }
        if (!canScrollHorizontally(1)) {
            return false;
        }
        setCurrentItem(this.mCurItem + 1);
        return true;
    }

    private boolean canScroll() {
        PagerAdapter pagerAdapter = this.mAdapter;
        return pagerAdapter != null && pagerAdapter.getCount() > 1;
    }

    /* access modifiers changed from: private */
    public class PagerObserver extends DataSetObserver {
        private PagerObserver() {
        }

        @Override // android.database.DataSetObserver
        public void onChanged() {
            ViewPager.this.dataSetChanged();
        }

        @Override // android.database.DataSetObserver
        public void onInvalidated() {
            ViewPager.this.dataSetChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public static class ViewPositionComparator implements Comparator<View> {
        ViewPositionComparator() {
        }

        public int compare(View lhs, View rhs) {
            LayoutParams llp = (LayoutParams) lhs.getLayoutParams();
            LayoutParams rlp = (LayoutParams) rhs.getLayoutParams();
            if (llp.isDecor != rlp.isDecor) {
                return llp.isDecor ? 1 : -1;
            }
            return llp.position - rlp.position;
        }
    }
}
