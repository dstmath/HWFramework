package huawei.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.ViewParent;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Checkable;
import android.widget.GridView;
import android.widget.HwSpringBackHelper;
import android.widget.ListAdapter;
import huawei.android.view.HwPositionPair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class HwGridView extends GridView {
    private static final String ANIMATOR_TAG = "gridDeleteAnimation";
    private static final float APPER_ANIMATION_BEGIN_SCALE = 0.85f;
    private static final int APPER_ANIMATION_DELAY = 100;
    private static final int APPER_ANIMATION_DURATION = 200;
    private static final int AUTO_SCROLL_EDGE_HEIGHT_DP = 90;
    private static final float CHECK_ANIMATION_ALPHA_SCALE = 0.7f;
    private static final int CHECK_ANIMATION_DURATION = 150;
    private static final float CHECK_ANIMATION_SCALE_SCALE = 0.9f;
    public static final int CHOICE_MODE_MULTIPLE_MODAL_AUTO_SCROLL = 8;
    private static final float DELETE_ANIMATION_ALPHA_END = 0.0f;
    private static final int DELETE_ANIMATION_DURATION = 150;
    private static final float DELETE_ANIMATION_SCALE_END = 0.3f;
    private static final int DURATION_DELTA = 152;
    private static final int EDGE_INSIDE_LISTVIEW_DP = 90;
    private static final int EDGE_OUTSIDE_LISTVIEW_DP = 0;
    private static final boolean IS_ANIMATOR_DBG = false;
    private static final int LIST_DEFAULT_CAPACITY = 10;
    private static final int MAX_ALPHA_VALUE = 255;
    private static final float MAX_OVER_SCROLLY_DISTANCE_FACTOR = 0.45f;
    private static final int MIN_DURATION = 40;
    private static final int OVERFLOW_INDEX_FLAG = -2;
    private static final int STEP_DP = 16;
    private static final String TAG = "GridView";
    private List<DeleteItemInfo> mAppearItems;
    private Animator mDeleteAnimator;
    private int mDeleteCountAfterVisible;
    private int mDeleteCountBeforeVisible;
    private DeleteAnimatorCallback mDeleteInterface;
    private List<DeleteItemInfo> mDeleteItems;
    private List<DeleteItemInfo> mDisappearItems;
    private int mFirstVisiblePositionDeltaRowNum;
    private HwAutoScroller mHwAutoScroller;
    private HwSpringBackHelper mHwSpringBackHelper;
    private boolean mIsCheckAnimatorEnable;
    private boolean mIsRangeDelete;
    private int mLastVisiblePosBeforeDelete;
    private AbsListView.MultiChoiceModeListener mMultiChoiceModeCallback;
    private List<HwPositionPair> mPositionRangeBeforeVisible;
    private List<HwPositionPair> mPositionRangeVisible;

    public interface DeleteAnimatorCallback {
        int getItemPosition(Object obj);

        void notifyDataSetChanged();

        void notifyResult(boolean z);

        void remove(Object obj);
    }

    public HwGridView(Context context) {
        this(context, null);
    }

    public HwGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mDeleteInterface = null;
        this.mDeleteCountBeforeVisible = 0;
        this.mDeleteCountAfterVisible = 0;
        this.mLastVisiblePosBeforeDelete = -1;
        this.mFirstVisiblePositionDeltaRowNum = 0;
        this.mDeleteItems = new ArrayList((int) LIST_DEFAULT_CAPACITY);
        this.mDisappearItems = new ArrayList((int) LIST_DEFAULT_CAPACITY);
        this.mAppearItems = new ArrayList((int) LIST_DEFAULT_CAPACITY);
        this.mIsCheckAnimatorEnable = false;
        this.mMultiChoiceModeCallback = null;
        this.mDeleteAnimator = null;
        this.mIsRangeDelete = false;
        this.mHwSpringBackHelper = HwWidgetFactory.getHwSpringBackHelper();
    }

    public HwGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mDeleteInterface = null;
        this.mDeleteCountBeforeVisible = 0;
        this.mDeleteCountAfterVisible = 0;
        this.mLastVisiblePosBeforeDelete = -1;
        this.mFirstVisiblePositionDeltaRowNum = 0;
        this.mDeleteItems = new ArrayList((int) LIST_DEFAULT_CAPACITY);
        this.mDisappearItems = new ArrayList((int) LIST_DEFAULT_CAPACITY);
        this.mAppearItems = new ArrayList((int) LIST_DEFAULT_CAPACITY);
        this.mIsCheckAnimatorEnable = false;
        this.mMultiChoiceModeCallback = null;
        this.mDeleteAnimator = null;
        this.mIsRangeDelete = false;
        this.mHwSpringBackHelper = HwWidgetFactory.getHwSpringBackHelper();
    }

    /* access modifiers changed from: protected */
    public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        int newDeltaY;
        if (this.mMultiSelectAutoScrollFlag && this.mIsAutoScroll) {
            return false;
        }
        if (!hasSpringAnimatorMask()) {
            return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
        }
        if (isTouchEvent) {
            newDeltaY = getElasticInterpolation(deltaY, scrollY);
        } else {
            newDeltaY = deltaY;
        }
        return super.overScrollBy(deltaX, newDeltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, (int) (((float) getHeight()) * MAX_OVER_SCROLLY_DISTANCE_FACTOR), isTouchEvent);
    }

    public boolean onTouchEvent(MotionEvent event) {
        HwAutoScroller hwAutoScroller;
        if (event == null) {
            return false;
        }
        if (this.mMultiSelectAutoScrollFlag) {
            if (!this.mIsAutoScroll && (hwAutoScroller = this.mHwAutoScroller) != null) {
                hwAutoScroller.stop();
            }
            if (event.getActionMasked() == 1) {
                pauseHwAutoScroll();
            }
        }
        return super.onTouchEvent(event);
    }

    /* access modifiers changed from: protected */
    public void onMultiSelectMove(MotionEvent ev, int pointerIndex) {
        if (this.mMultiSelectAutoScrollFlag && ev != null) {
            int y = (int) ev.getY(pointerIndex);
            int edgeInsideView = (int) TypedValue.applyDimension(1, 90.0f, this.mContext.getResources().getDisplayMetrics());
            int edgeOutsideView = (int) TypedValue.applyDimension(1, DELETE_ANIMATION_ALPHA_END, this.mContext.getResources().getDisplayMetrics());
            int edgeHeight = (int) TypedValue.applyDimension(1, 90.0f, this.mContext.getResources().getDisplayMetrics());
            int stepPx = (int) TypedValue.applyDimension(1, 16.0f, this.mContext.getResources().getDisplayMetrics());
            double deltaDownY = 0.0d;
            if (y > getHeight() - edgeInsideView) {
                this.mIsAutoScroll = true;
                if (!isScrollToEnd(false)) {
                    if ((getHeight() - y) + edgeOutsideView > 0) {
                        deltaDownY = ((double) ((getHeight() - y) + edgeOutsideView)) / ((double) edgeHeight);
                    }
                    hwSmoothScrollBy(stepPx, (int) ((152.0d * deltaDownY) + 40.0d));
                }
            } else if (y < edgeInsideView) {
                this.mIsAutoScroll = true;
                if (!isScrollToEnd(true)) {
                    if (y + edgeOutsideView > 0) {
                        deltaDownY = ((double) (y + edgeOutsideView)) / ((double) edgeHeight);
                    }
                    hwSmoothScrollBy(-stepPx, (int) ((152.0d * deltaDownY) + 40.0d));
                }
            } else {
                pauseHwAutoScroll();
            }
        }
    }

    private boolean isScrollToEnd(boolean isScrollUp) {
        int firstVisiblePosition = getFirstVisiblePosition();
        int lastVisiblePosition = getLastVisiblePosition();
        if (isScrollUp) {
            View firstItem = getChildAt(0);
            if (firstItem == null) {
                this.mIsAutoScroll = false;
                return true;
            } else if (firstVisiblePosition > 0 || firstItem.getTop() < getPaddingTop()) {
                return false;
            } else {
                pauseHwAutoScroll();
                return true;
            }
        } else {
            View lastItem = getChildAt(getChildCount() - 1);
            if (lastItem == null) {
                this.mIsAutoScroll = false;
                return true;
            } else if (lastVisiblePosition < getCount() - 1 || lastItem.getBottom() > getHeight() - getPaddingBottom()) {
                return false;
            } else {
                pauseHwAutoScroll();
                return true;
            }
        }
    }

    private void hwSmoothScrollBy(int distance, int duration) {
        if (this.mHwAutoScroller == null) {
            this.mHwAutoScroller = new HwAutoScroller();
        }
        this.mHwAutoScroller.start(distance, duration);
    }

    private void pauseHwAutoScroll() {
        this.mIsAutoScroll = false;
        HwAutoScroller hwAutoScroller = this.mHwAutoScroller;
        if (hwAutoScroller != null) {
            hwAutoScroller.stop();
        }
    }

    /* access modifiers changed from: private */
    public class HwAutoScroller implements Runnable {
        private static final int DIVIDER = 2;
        private static final int DURATION_UPBOUND = 100;
        private static final int MILLI_SECOND_PER_FRAME = 17;
        private static final int PAUSE_COUNTER = 1000;
        private static final int PAUSE_INTERVAL_FRAME = 2;
        private static final int PAUSE_SCROLL_DURATION = 34;
        private int mCount;
        private int mDistance;
        private int mDuration;
        private boolean mIsPause;
        private boolean mIsScrolling;
        private int mPauseCount;
        private int mPreviousDistance;
        private int mPreviousDuration;

        private HwAutoScroller() {
            this.mPauseCount = PAUSE_COUNTER;
        }

        /* access modifiers changed from: package-private */
        public void start(int distance, int duration) {
            if (duration > DURATION_UPBOUND) {
                this.mDistance = distance / 2;
                this.mDuration = duration / 2;
            } else {
                this.mDistance = distance;
                this.mDuration = duration;
            }
            if (!this.mIsScrolling) {
                stop();
                HwGridView.this.postOnAnimation(this);
                this.mIsScrolling = true;
                this.mCount = 1;
                this.mPauseCount = PAUSE_COUNTER;
            }
        }

        public void run() {
            int i = this.mPauseCount;
            if (i <= 1) {
                this.mIsPause = true;
                this.mPauseCount = PAUSE_COUNTER;
            } else if (!this.mIsPause) {
                this.mPauseCount = i - 1;
            } else {
                this.mPauseCount = PAUSE_COUNTER;
            }
            if (this.mCount <= 1) {
                if (this.mIsPause) {
                    this.mIsPause = false;
                    int nextDistance = 0;
                    int i2 = this.mPreviousDuration;
                    if (i2 > 0) {
                        nextDistance = (int) Math.round(((double) (this.mPreviousDistance * PAUSE_SCROLL_DURATION)) / ((double) i2));
                    }
                    HwGridView.this.smoothScrollBy(nextDistance, PAUSE_SCROLL_DURATION);
                    this.mCount += 2;
                } else {
                    HwGridView.this.smoothScrollBy(this.mDistance, this.mDuration);
                    this.mPreviousDistance = this.mDistance;
                    int i3 = this.mDuration;
                    this.mPreviousDuration = i3;
                    this.mCount = i3 / MILLI_SECOND_PER_FRAME;
                }
                HwGridView.this.postOnAnimation(this);
                return;
            }
            HwGridView.this.postOnAnimation(this);
            this.mCount--;
        }

        /* access modifiers changed from: package-private */
        public void stop() {
            this.mIsScrolling = false;
            HwGridView.this.removeCallbacks(this);
        }
    }

    /* access modifiers changed from: private */
    @TargetApi(11)
    public class MultiChoiseModeListnerWrapper implements AbsListView.MultiChoiceModeListener {
        AbsListView.MultiChoiceModeListener mChoiceModeListener;

        private MultiChoiseModeListnerWrapper(AbsListView.MultiChoiceModeListener listener) {
            this.mChoiceModeListener = null;
            this.mChoiceModeListener = listener;
        }

        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean isChecked) {
            AbsListView.MultiChoiceModeListener multiChoiceModeListener = this.mChoiceModeListener;
            if (multiChoiceModeListener != null) {
                multiChoiceModeListener.onItemCheckedStateChanged(mode, position, id, isChecked);
            }
            if (HwGridView.this.mIsCheckAnimatorEnable) {
                HwGridView.this.startCheckStateAnimator(position, isChecked);
            }
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            AbsListView.MultiChoiceModeListener multiChoiceModeListener = this.mChoiceModeListener;
            if (multiChoiceModeListener != null) {
                return multiChoiceModeListener.onCreateActionMode(mode, menu);
            }
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            AbsListView.MultiChoiceModeListener multiChoiceModeListener = this.mChoiceModeListener;
            if (multiChoiceModeListener != null) {
                return multiChoiceModeListener.onPrepareActionMode(mode, menu);
            }
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            AbsListView.MultiChoiceModeListener multiChoiceModeListener = this.mChoiceModeListener;
            if (multiChoiceModeListener != null) {
                return multiChoiceModeListener.onActionItemClicked(mode, item);
            }
            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
            SparseBooleanArray checkedItems;
            AbsListView.MultiChoiceModeListener multiChoiceModeListener = this.mChoiceModeListener;
            if (multiChoiceModeListener != null) {
                multiChoiceModeListener.onDestroyActionMode(mode);
            }
            if (HwGridView.this.mIsCheckAnimatorEnable && (checkedItems = HwGridView.this.getCheckedItemPositions()) != null) {
                int checkedItemsCount = checkedItems.size();
                for (int i = 0; i < checkedItemsCount; i++) {
                    if (checkedItems.valueAt(i)) {
                        HwGridView.this.startCheckStateAnimator(checkedItems.keyAt(i), false);
                    }
                }
            }
        }
    }

    public void setMultiChoiceModeListener(AbsListView.MultiChoiceModeListener listener) {
        if (this.mIsCheckAnimatorEnable) {
            this.mMultiChoiceModeCallback = new MultiChoiseModeListnerWrapper(listener);
        } else {
            this.mMultiChoiceModeCallback = listener;
        }
        super.setMultiChoiceModeListener(this.mMultiChoiceModeCallback);
    }

    @TargetApi(11)
    private Animator getCheckStateAnimator(int position, View view, boolean isChecked) {
        ValueAnimator animatorAlpha;
        ValueAnimator animatorScale;
        if (view == null) {
            return null;
        }
        if (isChecked) {
            animatorAlpha = getAlphaAnimator(1.0f, CHECK_ANIMATION_ALPHA_SCALE, 150);
            animatorScale = getScaleAnimator(1.0f, CHECK_ANIMATION_SCALE_SCALE, 150, 17563661);
        } else {
            animatorAlpha = getAlphaAnimator(CHECK_ANIMATION_ALPHA_SCALE, 1.0f, 150);
            animatorScale = getScaleAnimator(CHECK_ANIMATION_SCALE_SCALE, 1.0f, 150, 17563661);
        }
        List<DeleteItemInfo> itemInfos = new ArrayList<>(1);
        itemInfos.add(new DeleteItemInfo(position));
        animatorAlpha.addUpdateListener(getAlphaAnimatorUpdateListener(itemInfos));
        animatorScale.addUpdateListener(getScaleAnimatorUpdateListener(itemInfos));
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorAlpha, animatorScale);
        return animatorSet;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startCheckStateAnimator(int position, boolean isChecked) {
        Animator animator;
        int firstVisiblePos = getFirstVisiblePosition();
        int lastVisiblePos = getLastVisiblePosition();
        if (position >= firstVisiblePos && position <= lastVisiblePos) {
            View view = getChildAt(position - firstVisiblePos);
            if (view == null) {
                Log.w(ANIMATOR_TAG, "startCheckStateAnimator: fail to get view. position " + position + ", isChecked " + isChecked + ", firstVisiblePos " + firstVisiblePos + ", lastVisiblePos " + lastVisiblePos);
                return;
            }
            float currentAlpha = view.getAlpha();
            float currentScale = view.getScaleX();
            if (isChecked) {
                if (currentAlpha == CHECK_ANIMATION_ALPHA_SCALE && currentScale == CHECK_ANIMATION_SCALE_SCALE) {
                    return;
                }
            } else if (currentAlpha == 1.0f && currentScale == 1.0f) {
                return;
            }
            if (this.mDeleteAnimator == null && (animator = getCheckStateAnimator(position, view, isChecked)) != null) {
                animator.start();
            }
        }
    }

    /* access modifiers changed from: private */
    public class AnimDrawable extends BitmapDrawable {
        private static final int TRANSLATION_DIVIDER = 2;
        private int mDeltaX;
        private int mDeltaY;
        private int mPositionX;
        private int mPositionY;
        private float mScaleX;
        private float mScaleY;

        private AnimDrawable(Resources res, Bitmap bitmap) {
            super(res, bitmap);
            this.mDeltaX = 0;
            this.mDeltaY = 0;
            this.mScaleX = HwGridView.APPER_ANIMATION_BEGIN_SCALE;
            this.mScaleY = HwGridView.APPER_ANIMATION_BEGIN_SCALE;
            setAlpha(0);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setPosition(int positionX, int positionY) {
            this.mPositionX = positionX;
            this.mPositionY = positionY;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setTranslation(int deltaX, int deltaY) {
            this.mDeltaX = deltaX;
            this.mDeltaY = deltaY;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setScale(float scaleX, float scaleY) {
            this.mScaleX = scaleX;
            this.mScaleY = scaleY;
        }

        public void draw(Canvas canvas) {
            canvas.save();
            float f = this.mScaleX;
            canvas.translate(((((float) this.mDeltaX) * (1.0f - f)) / 2.0f) + ((float) this.mPositionX), ((((float) this.mDeltaY) * (1.0f - f)) / 2.0f) + ((float) this.mPositionY));
            canvas.scale(this.mScaleX, this.mScaleY);
            super.draw(canvas);
            canvas.restore();
        }
    }

    /* access modifiers changed from: private */
    public class DeleteItemInfo {
        ListAdapter mAdapter = null;
        int mAppearLeft = -1;
        int mAppearTop = -1;
        AnimDrawable mDrawable = null;
        View mItemView = null;
        int mModulePos = -1;
        View mModuleView = null;
        ViewGroupOverlay mOverlayView = null;
        ViewGroup mParent = null;
        int mPosition = -1;
        int mTempPosition = -1;

        DeleteItemInfo(int position) {
            this.mPosition = position;
            this.mItemView = HwGridView.this.getChildAt(position - HwGridView.this.getFirstVisiblePosition());
        }

        DeleteItemInfo(int position, int numColumns, int modulePos) {
            this.mPosition = position;
            this.mModulePos = modulePos;
            int firstVisiblePos = HwGridView.this.getFirstVisiblePosition();
            this.mModuleView = HwGridView.this.getChildAt(modulePos - firstVisiblePos);
            int i = this.mPosition;
            if (i == this.mModulePos) {
                View view = this.mModuleView;
                if (view != null) {
                    this.mAppearTop = view.getTop();
                    this.mAppearLeft = this.mModuleView.getLeft();
                    return;
                }
                Log.e(HwGridView.ANIMATOR_TAG, "DeleteItemInfo: mModuleView is null!");
                return;
            }
            View leftModuleView = HwGridView.this.getChildAt((i - numColumns) - firstVisiblePos);
            View topModuleView = HwGridView.this.getChildAt(((i / numColumns) * numColumns) - firstVisiblePos);
            if (leftModuleView == null || topModuleView == null) {
                Log.e(HwGridView.ANIMATOR_TAG, "DeleteItemInfo: mModuleView is null!");
                return;
            }
            this.mAppearTop = topModuleView.getTop();
            this.mAppearLeft = leftModuleView.getLeft();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void prepareForAppear(ListAdapter adapter, ViewGroup parent) {
            if (adapter == null || parent == null) {
                Log.e(HwGridView.ANIMATOR_TAG, "prepareForAppear: input is invalid.");
                return;
            }
            this.mAdapter = adapter;
            this.mParent = parent;
            int i = this.mTempPosition;
            if (i < 0 || i >= adapter.getCount()) {
                Log.e(HwGridView.ANIMATOR_TAG, "prepareForAppear: the temp position is invalid.");
                return;
            }
            View view = this.mModuleView;
            if (view == null) {
                Log.e(HwGridView.ANIMATOR_TAG, "prepareForAppear: mModuleView is null.");
                return;
            }
            ViewParent viewParent = view.getParent();
            if (viewParent == null) {
                Log.e(HwGridView.ANIMATOR_TAG, "prepareForAppear: viewParent is null");
                return;
            }
            View view2 = this.mModuleView;
            if (view2 instanceof Checkable) {
                ((Checkable) view2).setChecked(false);
            }
            if (viewParent instanceof ViewGroup) {
                this.mOverlayView = ((ViewGroup) viewParent).getOverlay();
            } else {
                Log.e(HwGridView.ANIMATOR_TAG, "prepareForAppear: viewParent is not instance of ViewGroup");
            }
        }

        private void initDrawable() {
            if (this.mOverlayView == null) {
                Log.e(HwGridView.ANIMATOR_TAG, "initDrawable: mOverlayView is null.");
            } else if (this.mAppearLeft != -1 || this.mAppearTop != -1) {
                View appearView = this.mAdapter.getView(this.mTempPosition, this.mModuleView, this.mParent);
                View view = this.mModuleView;
                if (appearView != view || appearView == null) {
                    Log.e(HwGridView.ANIMATOR_TAG, "initDrawable: function getView has not use the convertView.");
                    return;
                }
                int width = view.getWidth();
                int height = this.mModuleView.getHeight();
                if (width <= 0 || height <= 0) {
                    Log.e(HwGridView.ANIMATOR_TAG, "initDrawable: mModuleView's width or height is invalid");
                    return;
                }
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                appearView.draw(new Canvas(bitmap));
                HwGridView hwGridView = HwGridView.this;
                this.mDrawable = new AnimDrawable(hwGridView.getResources(), bitmap);
                this.mDrawable.setBounds(0, 0, this.mModuleView.getWidth(), this.mModuleView.getHeight());
                this.mDrawable.setPosition(this.mAppearLeft, this.mAppearTop);
                this.mDrawable.setTranslation(this.mModuleView.getRight() - this.mModuleView.getLeft(), this.mModuleView.getBottom() - this.mModuleView.getTop());
                this.mOverlayView.add(this.mDrawable);
                this.mAdapter.getView(this.mModulePos, this.mModuleView, this.mParent);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setDrawableAlpha(int alpha) {
            if (this.mDrawable == null) {
                initDrawable();
            }
            AnimDrawable animDrawable = this.mDrawable;
            if (animDrawable != null) {
                animDrawable.setAlpha(alpha);
                ViewGroup viewGroup = this.mParent;
                if (viewGroup != null) {
                    viewGroup.requestLayout();
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setDrawableScale(float scaleX, float scaleY) {
            if (this.mDrawable == null) {
                initDrawable();
            }
            AnimDrawable animDrawable = this.mDrawable;
            if (animDrawable != null) {
                animDrawable.setScale(scaleX, scaleY);
                ViewGroup viewGroup = this.mParent;
                if (viewGroup != null) {
                    viewGroup.requestLayout();
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void resetAppearView() {
            ViewGroupOverlay viewGroupOverlay;
            AnimDrawable animDrawable = this.mDrawable;
            if (animDrawable == null || (viewGroupOverlay = this.mOverlayView) == null) {
                Log.w(HwGridView.ANIMATOR_TAG, "resetAppearView: has not set ViewOverlay!");
            } else {
                viewGroupOverlay.remove(animDrawable);
            }
        }
    }

    @TargetApi(11)
    private ValueAnimator getAlphaAnimator(float begin, float end, int duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(begin, end);
        animator.setDuration((long) duration);
        animator.setInterpolator(AnimationUtils.loadInterpolator(getContext(), 34078724));
        return animator;
    }

    @TargetApi(11)
    private ValueAnimator.AnimatorUpdateListener getAlphaAnimatorUpdateListener(final List<DeleteItemInfo> itemInfos) {
        return new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.widget.HwGridView.AnonymousClass1 */

            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                for (DeleteItemInfo itemInfo : itemInfos) {
                    if (itemInfo.mOverlayView != null) {
                        itemInfo.setDrawableAlpha((int) (255.0f * value));
                    } else {
                        View view = itemInfo.mItemView;
                        if (view != null) {
                            view.setAlpha(value);
                        }
                    }
                }
            }
        };
    }

    @TargetApi(11)
    private ValueAnimator getScaleAnimator(float begin, float end, int duration, int resId) {
        ValueAnimator animator = ValueAnimator.ofFloat(begin, end);
        animator.setDuration((long) duration);
        animator.setInterpolator(AnimationUtils.loadInterpolator(getContext(), resId));
        return animator;
    }

    @TargetApi(11)
    private ValueAnimator.AnimatorUpdateListener getScaleAnimatorUpdateListener(final List<DeleteItemInfo> itemInfos) {
        return new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.widget.HwGridView.AnonymousClass2 */

            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                for (DeleteItemInfo itemInfo : itemInfos) {
                    if (itemInfo.mOverlayView != null) {
                        itemInfo.setDrawableScale(value, value);
                    } else {
                        View view = itemInfo.mItemView;
                        if (view != null) {
                            view.setScaleX(value);
                            view.setScaleY(value);
                        }
                    }
                }
            }
        };
    }

    @TargetApi(11)
    private Animator getDeletedAnimator() {
        if (this.mDeleteItems.size() == 0) {
            return null;
        }
        ValueAnimator animatorAlpha = getAlphaAnimator(CHECK_ANIMATION_ALPHA_SCALE, DELETE_ANIMATION_ALPHA_END, 150);
        ValueAnimator animatorScale = getScaleAnimator(CHECK_ANIMATION_SCALE_SCALE, DELETE_ANIMATION_SCALE_END, 150, 17563663);
        animatorAlpha.addUpdateListener(getAlphaAnimatorUpdateListener(this.mDeleteItems));
        animatorScale.addUpdateListener(getScaleAnimatorUpdateListener(this.mDeleteItems));
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorAlpha, animatorScale);
        return animatorSet;
    }

    @TargetApi(11)
    private Animator getDisAppearAnimator() {
        if (this.mDisappearItems.size() == 0) {
            return null;
        }
        ValueAnimator animatorAlpha = getAlphaAnimator(1.0f, DELETE_ANIMATION_ALPHA_END, 150);
        animatorAlpha.addUpdateListener(getAlphaAnimatorUpdateListener(this.mDisappearItems));
        animatorAlpha.addListener(new AnimatorListenerAdapter() {
            /* class huawei.android.widget.HwGridView.AnonymousClass3 */

            public void onAnimationEnd(Animator animation) {
                for (DeleteItemInfo itemInfo : HwGridView.this.mDisappearItems) {
                    View view = itemInfo.mItemView;
                    if (view != null) {
                        view.setAlpha(HwGridView.DELETE_ANIMATION_ALPHA_END);
                    }
                }
            }
        });
        return animatorAlpha;
    }

    @TargetApi(11)
    private Animator getAppearAnimator() {
        if (this.mAppearItems.size() == 0) {
            return null;
        }
        ListAdapter adapter = getAdapter();
        if (adapter == null) {
            Log.e(ANIMATOR_TAG, "getAppearAnimator: the adapter is null.");
            return null;
        }
        for (DeleteItemInfo itemInfo : this.mAppearItems) {
            itemInfo.prepareForAppear(adapter, this);
        }
        ValueAnimator animatorAlpha = getAlphaAnimator(DELETE_ANIMATION_ALPHA_END, 1.0f, APPER_ANIMATION_DURATION);
        ValueAnimator animatorScale = getScaleAnimator(APPER_ANIMATION_BEGIN_SCALE, 1.0f, APPER_ANIMATION_DURATION, 17563661);
        animatorAlpha.addUpdateListener(getAlphaAnimatorUpdateListener(this.mAppearItems));
        animatorScale.addUpdateListener(getScaleAnimatorUpdateListener(this.mAppearItems));
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorAlpha, animatorScale);
        return animatorSet;
    }

    @TargetApi(11)
    private void startDeleteAnimator(Animator.AnimatorListener animatorListener) {
        AnimatorSet animatorSet = new AnimatorSet();
        Animator deleteAnimator = getDeletedAnimator();
        Animator disappearAnimator = getDisAppearAnimator();
        Animator appearAnimator = getAppearAnimator();
        Collection<Animator> items = new ArrayList<>((int) LIST_DEFAULT_CAPACITY);
        if (deleteAnimator != null) {
            items.add(deleteAnimator);
        }
        if (disappearAnimator != null) {
            items.add(disappearAnimator);
        }
        if (appearAnimator != null) {
            appearAnimator.setStartDelay(100);
            items.add(appearAnimator);
        }
        if (items.size() != 0) {
            animatorSet.playTogether(items);
            animatorSet.addListener(animatorListener);
            this.mDeleteAnimator = animatorSet;
            this.mDeleteAnimator.start();
            return;
        }
        Log.w(TAG, "startDeleteAnimator: no animator to play.");
        animatorListener.onAnimationEnd(null);
    }

    @TargetApi(11)
    private Animator.AnimatorListener getDeleteListener(final List<Pair<Boolean, Object>> positionMap) {
        return new AnimatorListenerAdapter() {
            /* class huawei.android.widget.HwGridView.AnonymousClass4 */

            public void onAnimationEnd(Animator animation) {
                for (DeleteItemInfo appearItemInfo : HwGridView.this.mAppearItems) {
                    View view = appearItemInfo.mItemView;
                    if (view != null) {
                        view.setAlpha(1.0f);
                        view.setScaleX(1.0f);
                        view.setScaleY(1.0f);
                    }
                    appearItemInfo.resetAppearView();
                }
                for (DeleteItemInfo itemInfo : HwGridView.this.mDeleteItems) {
                    View view2 = itemInfo.mItemView;
                    if (view2 != null) {
                        view2.setScaleY(1.0f);
                        view2.setScaleX(1.0f);
                        view2.setAlpha(1.0f);
                    }
                }
                for (DeleteItemInfo itemInfo2 : HwGridView.this.mDisappearItems) {
                    View view3 = itemInfo2.mItemView;
                    if (view3 != null) {
                        view3.setScaleY(1.0f);
                        view3.setScaleX(1.0f);
                        view3.setAlpha(1.0f);
                    }
                }
                HwGridView.this.deleteAfterAnimator(positionMap);
            }
        };
    }

    private void refreshFirstVisiblePosition() {
        ListAdapter adapter = getAdapter();
        if (adapter != null && adapter.getCount() != 0) {
            int firstPosition = getFirstVisiblePosition();
            int columns = getNumColumns();
            if (columns <= 0) {
                Log.e(ANIMATOR_TAG, "refreshFirstVisiblePosition: columns is zero.");
                return;
            }
            int newPosition = (((firstPosition - this.mDeleteCountBeforeVisible) / columns) * columns) - (this.mFirstVisiblePositionDeltaRowNum * columns);
            if (newPosition < 0) {
                newPosition = 0;
            }
            setFirstVisiblePosition(newPosition);
            if (getFirstVisiblePosition() != newPosition) {
                Log.w(ANIMATOR_TAG, "refreshFirstVisiblePosition: setFirstVisiblePosition failed. firstPosition " + firstPosition + ", newPosition " + newPosition + ", mDeleteCountBeforeVisible " + this.mDeleteCountBeforeVisible);
            }
        }
    }

    @TargetApi(5)
    private void deleteItemsProc(List<Pair<Boolean, Object>> positionMap, int firstIdx, int lastIdx, boolean isFirstTime) {
        int positionMapSize = positionMap.size();
        int correctLastIdx = lastIdx;
        if (lastIdx >= positionMapSize) {
            correctLastIdx = positionMapSize - 1;
        }
        for (int position = firstIdx; position <= correctLastIdx; position++) {
            Pair<Boolean, Object> item = positionMap.get(position);
            if (((Boolean) item.first).booleanValue()) {
                if (isFirstTime) {
                    setItemChecked(position, false);
                } else if (item.second == null) {
                    Log.e(ANIMATOR_TAG, "deleteAfterAnimator: saved item is null.");
                } else {
                    this.mDeleteInterface.remove(item.second);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @TargetApi(5)
    private void deleteAfterAnimator(List<Pair<Boolean, Object>> positionMap) {
        if (this.mIsRangeDelete) {
            deleteItemInRange(this.mPositionRangeVisible);
            deleteItemInRange(this.mPositionRangeBeforeVisible);
            this.mIsRangeDelete = false;
        } else {
            deleteItemsProc(positionMap, 0, this.mLastVisiblePosBeforeDelete, true);
            deleteItemsProc(positionMap, 0, this.mLastVisiblePosBeforeDelete, false);
        }
        refreshFirstVisiblePosition();
        clearSavedInfo();
        this.mDeleteInterface.notifyDataSetChanged();
        this.mDeleteInterface.notifyResult(true);
    }

    private void sortDeleteInfo() {
        this.mDeleteItems.sort(new Comparator<DeleteItemInfo>() {
            /* class huawei.android.widget.HwGridView.AnonymousClass5 */

            public int compare(DeleteItemInfo item1, DeleteItemInfo item2) {
                if (item1.mPosition > item2.mPosition) {
                    return 1;
                }
                if (item1.mPosition == item2.mPosition) {
                    return 0;
                }
                return -1;
            }
        });
    }

    @TargetApi(5)
    private boolean transmitItemToPosition(List<Object> deleteItems, List<Pair<Boolean, Object>> positionMap) {
        int itemCount = getAdapter().getCount();
        int firstPos = getFirstVisiblePosition();
        this.mLastVisiblePosBeforeDelete = getLastVisiblePosition();
        for (Object item : deleteItems) {
            int position = this.mDeleteInterface.getItemPosition(item);
            if (position < 0 || position >= itemCount) {
                Log.e(ANIMATOR_TAG, "transmitItemToPosition: position is invalid.");
                return false;
            }
            if (position < firstPos) {
                if (((Boolean) positionMap.get(position).first).booleanValue()) {
                    Log.w(ANIMATOR_TAG, "transmitItemToPosition: repeat delete item, position " + position);
                } else {
                    this.mDeleteCountBeforeVisible++;
                }
            } else if (position <= this.mLastVisiblePosBeforeDelete) {
                DeleteItemInfo deleteItemInfo = new DeleteItemInfo(position);
                if (this.mDeleteItems.indexOf(deleteItemInfo) == -1) {
                    this.mDeleteItems.add(deleteItemInfo);
                } else {
                    Log.w(ANIMATOR_TAG, "transmitItemToPosition: repeat delete visible item, position " + position);
                }
            } else if (((Boolean) positionMap.get(position).first).booleanValue()) {
                Log.w(ANIMATOR_TAG, "transmitItemToPosition: repeat delete item, position " + position);
            } else {
                this.mDeleteCountAfterVisible++;
            }
            positionMap.set(position, new Pair<>(true, item));
        }
        int positionMapSize = positionMap.size();
        deleteItemsProc(positionMap, this.mLastVisiblePosBeforeDelete + 1, positionMapSize - 1, true);
        deleteItemsProc(positionMap, this.mLastVisiblePosBeforeDelete + 1, positionMapSize - 1, false);
        if (this.mDeleteCountAfterVisible > 0) {
            this.mDeleteInterface.notifyDataSetChanged();
        }
        sortDeleteInfo();
        return true;
    }

    @TargetApi(5)
    private int getPositionWhenDeleteing(int itemCount, int lastVisiblePos, List<Pair<Boolean, Object>> positionMap, int lastTempPosition) {
        int tempIdx;
        for (int index = lastTempPosition + 1; index <= lastVisiblePos; index++) {
            if (!((Boolean) positionMap.get(index).first).booleanValue()) {
                return index;
            }
        }
        if (lastTempPosition <= lastVisiblePos) {
            tempIdx = lastVisiblePos + 1;
        } else {
            tempIdx = lastTempPosition + 1;
        }
        if (tempIdx < itemCount) {
            return tempIdx;
        }
        return -1;
    }

    private void clearSavedInfo() {
        this.mDeleteCountAfterVisible = 0;
        this.mDeleteCountBeforeVisible = 0;
        this.mLastVisiblePosBeforeDelete = -1;
        this.mFirstVisiblePositionDeltaRowNum = 0;
        this.mDisappearItems.clear();
        this.mAppearItems.clear();
        this.mDeleteItems.clear();
        this.mDeleteAnimator = null;
    }

    @TargetApi(5)
    private int getFirstTempPosition(int firstPos, List<Pair<Boolean, Object>> positionMap) {
        int columns = getNumColumns();
        if (columns <= 0) {
            Log.e(ANIMATOR_TAG, "getFirstTempPosition: ColumnWidth is invalid. columes " + columns);
            return OVERFLOW_INDEX_FLAG;
        }
        int modulus = (firstPos - this.mDeleteCountBeforeVisible) % columns;
        int childRowNum = (getChildCount() + (columns - 1)) / columns;
        int remainRowNum = ((columns - 1) + ((((positionMap.size() - firstPos) - this.mDeleteCountAfterVisible) - this.mDeleteItems.size()) + modulus)) / columns;
        if (remainRowNum < childRowNum) {
            this.mFirstVisiblePositionDeltaRowNum = childRowNum - remainRowNum;
            modulus += this.mFirstVisiblePositionDeltaRowNum * columns;
        }
        if (modulus == 0) {
            return -1;
        }
        int prePos = firstPos - 1;
        int count = 0;
        int idx = firstPos - 1;
        while (true) {
            if (idx < 0) {
                break;
            } else if (!((Boolean) positionMap.get(idx).first).booleanValue() && (count = count + 1) == modulus) {
                prePos = idx;
                break;
            } else {
                idx--;
            }
        }
        if (count == modulus) {
            return prePos;
        }
        Log.e(ANIMATOR_TAG, "getFirstTempPosition: fail to get prePos. count " + count + ", modulus " + modulus);
        return 0;
    }

    private int getFirstTempPositionEx(int firstPos, List<Pair<Boolean, Object>> positionMap) {
        int firstTempPosition = getFirstTempPosition(firstPos, positionMap);
        if (firstTempPosition > -1) {
            return firstTempPosition;
        }
        if (this.mDeleteCountBeforeVisible != 0) {
            return firstPos;
        }
        if (this.mDeleteItems.size() != 0) {
            return this.mDeleteItems.get(0).mPosition;
        }
        Log.e(ANIMATOR_TAG, "getFirstTempPositionEx: not delete item in or before screen.");
        return -1;
    }

    private void prepareBeforeAnimator(List<Pair<Boolean, Object>> positionMap) {
        int tempLastPos;
        int firstDeletePosition;
        boolean isDeleted;
        int firstDeletePosition2 = getFirstVisiblePosition();
        int lastPos = getLastVisiblePosition();
        int firstDeletePosition3 = -1;
        int firstTempPosition = getFirstTempPositionEx(firstDeletePosition2, positionMap);
        int lastTempPosition = firstTempPosition - 1;
        int itemCount = getAdapter().getCount();
        int numColumns = getNumColumns();
        if (firstTempPosition >= 0) {
            if (numColumns <= 0) {
                Log.e(ANIMATOR_TAG, "prepareBeforeAnimator: numColumns is null.");
                return;
            }
            if ((lastPos + 1) % numColumns != 0) {
                tempLastPos = (((lastPos + numColumns) / numColumns) * numColumns) - 1;
            } else {
                tempLastPos = lastPos;
            }
            int lastTempPosition2 = lastTempPosition;
            int visiblePos = firstDeletePosition2;
            while (visiblePos <= tempLastPos) {
                Iterator<DeleteItemInfo> it = this.mDeleteItems.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        firstDeletePosition = firstDeletePosition3;
                        isDeleted = false;
                        break;
                    } else if (it.next().mPosition == visiblePos) {
                        if (firstDeletePosition3 == -1) {
                            firstDeletePosition = visiblePos;
                            isDeleted = true;
                        } else {
                            firstDeletePosition = firstDeletePosition3;
                            isDeleted = true;
                        }
                    }
                }
                if (isItemNeedDisappear(isDeleted, firstDeletePosition, firstTempPosition, visiblePos, lastPos)) {
                    this.mDisappearItems.add(new DeleteItemInfo(visiblePos));
                }
                if (isItemNeedAppear(firstDeletePosition, firstTempPosition, visiblePos)) {
                    int tempPos = getPositionWhenDeleteing(itemCount, lastPos, positionMap, lastTempPosition2);
                    if (tempPos < 0 || tempPos >= getAdapter().getCount()) {
                        lastTempPosition2 = itemCount;
                    } else {
                        DeleteItemInfo itemInfo = new DeleteItemInfo(visiblePos, numColumns, visiblePos > lastPos ? firstDeletePosition2 : visiblePos);
                        itemInfo.mTempPosition = tempPos;
                        this.mAppearItems.add(itemInfo);
                        lastTempPosition2 = tempPos;
                    }
                }
                visiblePos++;
                firstDeletePosition3 = firstDeletePosition;
                firstDeletePosition2 = firstDeletePosition2;
            }
        }
    }

    private boolean isItemNeedAppear(int firstDeletePosition, int firstTempPosition, int position) {
        if (this.mDeleteCountBeforeVisible == 0 && firstDeletePosition == -1 && firstTempPosition > position) {
            return false;
        }
        return true;
    }

    private boolean isItemNeedDisappear(boolean isDeleted, int firstDeletePosition, int firstTempPosition, int position, int lastVisiblePosition) {
        if (isDeleted || position > lastVisiblePosition) {
            return false;
        }
        if (this.mDeleteCountBeforeVisible == 0 && firstDeletePosition == -1 && firstTempPosition > position) {
            return false;
        }
        return true;
    }

    private void playAnimatorBeforeDelete(List<Object> deleteItems, List<Pair<Boolean, Object>> positionMap) {
        prepareBeforeAnimator(positionMap);
        if (this.mDeleteItems.size() == 0 && this.mDisappearItems.size() == 0) {
            Log.w(ANIMATOR_TAG, "playAnimatorBeforeDelete: no visible item to delete.");
            if (this.mDeleteCountAfterVisible != deleteItems.size()) {
                Log.w(ANIMATOR_TAG, "playAnimatorBeforeDelete: deleted item count is not match deleteItems.");
            }
            clearSavedInfo();
            this.mDeleteInterface.notifyResult(true);
            return;
        }
        startDeleteAnimator(getDeleteListener(positionMap));
    }

    private boolean isDeletingAnimatorPlaying() {
        return (this.mDeleteItems.size() == 0 && this.mDisappearItems.size() == 0) ? false : true;
    }

    @TargetApi(5)
    public void deleteItemsWithAnimator(List<Object> deleteItems, DeleteAnimatorCallback callback) {
        boolean removeItemResult;
        if (callback == null) {
            Log.e(ANIMATOR_TAG, "deleteItemsWithAnimation: input callback is null.");
        } else if (deleteItems == null || deleteItems.size() == 0) {
            callback.notifyResult(false);
            Log.w(ANIMATOR_TAG, "deleteItemsWithAnimation: deleteItems is null.");
        } else if (isDeletingAnimatorPlaying() || this.mLastVisiblePosBeforeDelete >= 0) {
            Log.e(ANIMATOR_TAG, "deleteItemsWithAnimator:last animator has not end.");
        } else {
            this.mDeleteInterface = callback;
            ListAdapter adapter = getAdapter();
            if (adapter == null) {
                Log.e(ANIMATOR_TAG, "deleteItemsWithAnimation: adapter is null, set adapter before delete please.");
                this.mDeleteInterface.notifyResult(false);
                return;
            }
            clearSavedInfo();
            List<Pair<Boolean, Object>> allItemsFlags = new ArrayList<>(adapter.getCount());
            for (int idx = 0; idx < adapter.getCount(); idx++) {
                allItemsFlags.add(idx, new Pair<>(false, null));
            }
            if (deleteItems.isEmpty() || !(deleteItems.get(0) instanceof HwPositionPair)) {
                removeItemResult = transmitItemToPosition(deleteItems, allItemsFlags);
            } else {
                this.mIsRangeDelete = true;
                removeItemResult = removeItemInPositionRange(deleteItems, allItemsFlags);
            }
            if (!removeItemResult) {
                Log.e(ANIMATOR_TAG, "deleteItemsWithAnimation: fail to get items position.");
                clearSavedInfo();
                this.mDeleteInterface.notifyResult(false);
                return;
            }
            playAnimatorBeforeDelete(deleteItems, allItemsFlags);
        }
    }

    @TargetApi(5)
    private boolean removeItemInPositionRange(List<Object> deletePositionPairs, List<Pair<Boolean, Object>> positionMap) {
        ListAdapter adapter = getAdapter();
        int itemCount = adapter.getCount();
        int firstVisiblePosForDelete = getFirstVisiblePosition();
        this.mLastVisiblePosBeforeDelete = getLastVisiblePosition();
        this.mPositionRangeBeforeVisible = new ArrayList();
        this.mPositionRangeVisible = new ArrayList();
        List<HwPositionPair> positionRangeAfterVisible = new ArrayList<>();
        for (Object obj : deletePositionPairs) {
            if (!(obj instanceof HwPositionPair)) {
                Log.e(ANIMATOR_TAG, "removeItemInPositionRange, obj is not HwPositionPair type");
                return false;
            }
            HwPositionPair positionPair = (HwPositionPair) obj;
            HwPositionPair beforePair = mergePositionPair(positionPair, new HwPositionPair(-1, Integer.valueOf(firstVisiblePosForDelete - 1)));
            if (beforePair != null) {
                this.mPositionRangeBeforeVisible.add(beforePair);
            }
            HwPositionPair afterPair = mergePositionPair(positionPair, new HwPositionPair(Integer.valueOf(this.mLastVisiblePosBeforeDelete + 1), Integer.MAX_VALUE));
            if (afterPair != null) {
                positionRangeAfterVisible.add(afterPair);
            }
            HwPositionPair visiblePair = mergePositionPair(positionPair, new HwPositionPair(Integer.valueOf(firstVisiblePosForDelete), Integer.valueOf(this.mLastVisiblePosBeforeDelete)));
            if (visiblePair != null) {
                this.mPositionRangeVisible.add(visiblePair);
            }
        }
        this.mDeleteCountAfterVisible += deleteItemInRange(positionRangeAfterVisible);
        this.mDeleteCountBeforeVisible += getItemCount(this.mPositionRangeBeforeVisible);
        for (HwPositionPair positionPair2 : this.mPositionRangeBeforeVisible) {
            for (int position = ((Integer) positionPair2.first).intValue(); position <= ((Integer) positionPair2.second).intValue(); position++) {
                positionMap.set(position, new Pair<>(true, new HwPositionPair(Integer.valueOf(position), Integer.valueOf(position))));
            }
        }
        for (HwPositionPair positionPair3 : this.mPositionRangeVisible) {
            for (int position2 = ((Integer) positionPair3.first).intValue(); position2 <= ((Integer) positionPair3.second).intValue(); position2++) {
                this.mDeleteItems.add(new DeleteItemInfo(position2));
                positionMap.set(position2, new Pair<>(true, new HwPositionPair(Integer.valueOf(position2), Integer.valueOf(position2))));
            }
        }
        if (itemCount != adapter.getCount()) {
            this.mDeleteInterface.notifyDataSetChanged();
        }
        sortDeleteInfo();
        return true;
    }

    @TargetApi(5)
    private int deleteItemInRange(List<HwPositionPair> positionPairList) {
        int deletedItemNum = 0;
        Collections.sort(positionPairList, new Comparator<HwPositionPair>() {
            /* class huawei.android.widget.HwGridView.AnonymousClass6 */

            public int compare(HwPositionPair o1, HwPositionPair o2) {
                return o2.compareTo(o1);
            }
        });
        for (HwPositionPair positionPair : positionPairList) {
            for (int position = ((Integer) positionPair.second).intValue(); position >= ((Integer) positionPair.first).intValue(); position--) {
                setItemChecked(position, false);
            }
            this.mDeleteInterface.remove(positionPair);
            this.mDeleteInterface.notifyDataSetChanged();
            deletedItemNum += (((Integer) positionPair.second).intValue() - ((Integer) positionPair.first).intValue()) + 1;
        }
        return deletedItemNum;
    }

    @TargetApi(5)
    private int getItemCount(List<HwPositionPair> positionPairList) {
        int deletedItemNum = 0;
        for (HwPositionPair positionPair : positionPairList) {
            deletedItemNum += (((Integer) positionPair.second).intValue() - ((Integer) positionPair.first).intValue()) + 1;
        }
        return deletedItemNum;
    }

    @TargetApi(5)
    private HwPositionPair mergePositionPair(HwPositionPair firstPositionPair, HwPositionPair secondPositionPair) {
        int mergePairFirst = ((Integer) (((Integer) firstPositionPair.first).intValue() < ((Integer) secondPositionPair.first).intValue() ? secondPositionPair.first : firstPositionPair.first)).intValue();
        int mergePairSecond = ((Integer) (((Integer) firstPositionPair.second).intValue() < ((Integer) secondPositionPair.second).intValue() ? firstPositionPair.second : secondPositionPair.second)).intValue();
        if (mergePairFirst > mergePairSecond) {
            return null;
        }
        return new HwPositionPair(Integer.valueOf(mergePairFirst), Integer.valueOf(mergePairSecond));
    }

    public void stopDeleteAnimator() {
        Animator animator = this.mDeleteAnimator;
        if (animator != null) {
            animator.cancel();
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isDeletingAnimatorPlaying()) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    /* access modifiers changed from: protected */
    public void refreshViewByCheckStatus(View child, int position) {
        if (!this.mIsCheckAnimatorEnable || isDeletingAnimatorPlaying() || getChoiceMode() != 3 || getCheckStates() == null) {
            return;
        }
        if (isItemChecked(position)) {
            child.setAlpha(CHECK_ANIMATION_ALPHA_SCALE);
            child.setScaleX(CHECK_ANIMATION_SCALE_SCALE);
            child.setScaleY(CHECK_ANIMATION_SCALE_SCALE);
            return;
        }
        child.setAlpha(1.0f);
        child.setScaleX(1.0f);
        child.setScaleY(1.0f);
    }

    public void setCheckAnimatorEnable(boolean isEnable) {
        this.mIsCheckAnimatorEnable = isEnable;
        if (isEnable && this.mMultiChoiceModeCallback == null) {
            setMultiChoiceModeListener(null);
        }
    }

    private boolean checkIsHwTheme(Context context, AttributeSet attrs) {
        return HwWidgetFactory.checkIsHwTheme(context, attrs);
    }

    /* access modifiers changed from: protected */
    public boolean hasSpringAnimatorMask() {
        return checkIsHwTheme(this.mContext, null);
    }

    private int getElasticInterpolation(int delta, int currentPos) {
        HwSpringBackHelper hwSpringBackHelper = this.mHwSpringBackHelper;
        if (hwSpringBackHelper != null) {
            return hwSpringBackHelper.getDynamicCurvedRateDelta(getHeight(), delta, currentPos);
        }
        return delta;
    }
}
