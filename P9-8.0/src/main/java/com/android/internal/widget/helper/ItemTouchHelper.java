package com.android.internal.widget.helper;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import com.android.internal.R;
import com.android.internal.widget.RecyclerView;
import com.android.internal.widget.RecyclerView.ChildDrawingOrderCallback;
import com.android.internal.widget.RecyclerView.ItemAnimator;
import com.android.internal.widget.RecyclerView.ItemDecoration;
import com.android.internal.widget.RecyclerView.LayoutManager;
import com.android.internal.widget.RecyclerView.OnChildAttachStateChangeListener;
import com.android.internal.widget.RecyclerView.OnItemTouchListener;
import com.android.internal.widget.RecyclerView.State;
import com.android.internal.widget.RecyclerView.ViewHolder;
import java.util.ArrayList;
import java.util.List;

public class ItemTouchHelper extends ItemDecoration implements OnChildAttachStateChangeListener {
    static final int ACTION_MODE_DRAG_MASK = 16711680;
    private static final int ACTION_MODE_IDLE_MASK = 255;
    static final int ACTION_MODE_SWIPE_MASK = 65280;
    public static final int ACTION_STATE_DRAG = 2;
    public static final int ACTION_STATE_IDLE = 0;
    public static final int ACTION_STATE_SWIPE = 1;
    static final int ACTIVE_POINTER_ID_NONE = -1;
    public static final int ANIMATION_TYPE_DRAG = 8;
    public static final int ANIMATION_TYPE_SWIPE_CANCEL = 4;
    public static final int ANIMATION_TYPE_SWIPE_SUCCESS = 2;
    static final boolean DEBUG = false;
    static final int DIRECTION_FLAG_COUNT = 8;
    public static final int DOWN = 2;
    public static final int END = 32;
    public static final int LEFT = 4;
    private static final int PIXELS_PER_SECOND = 1000;
    public static final int RIGHT = 8;
    public static final int START = 16;
    static final String TAG = "ItemTouchHelper";
    public static final int UP = 1;
    int mActionState = 0;
    int mActivePointerId = -1;
    Callback mCallback;
    private ChildDrawingOrderCallback mChildDrawingOrderCallback = null;
    private List<Integer> mDistances;
    private long mDragScrollStartTimeInMs;
    float mDx;
    float mDy;
    GestureDetector mGestureDetector;
    float mInitialTouchX;
    float mInitialTouchY;
    float mMaxSwipeVelocity;
    private final OnItemTouchListener mOnItemTouchListener = new OnItemTouchListener() {
        public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent event) {
            ItemTouchHelper.this.mGestureDetector.onTouchEvent(event);
            int action = event.getActionMasked();
            if (action == 0) {
                ItemTouchHelper.this.mActivePointerId = event.getPointerId(0);
                ItemTouchHelper.this.mInitialTouchX = event.getX();
                ItemTouchHelper.this.mInitialTouchY = event.getY();
                ItemTouchHelper.this.obtainVelocityTracker();
                if (ItemTouchHelper.this.mSelected == null) {
                    RecoverAnimation animation = ItemTouchHelper.this.findAnimation(event);
                    if (animation != null) {
                        ItemTouchHelper itemTouchHelper = ItemTouchHelper.this;
                        itemTouchHelper.mInitialTouchX -= animation.mX;
                        itemTouchHelper = ItemTouchHelper.this;
                        itemTouchHelper.mInitialTouchY -= animation.mY;
                        ItemTouchHelper.this.endRecoverAnimation(animation.mViewHolder, true);
                        if (ItemTouchHelper.this.mPendingCleanup.remove(animation.mViewHolder.itemView)) {
                            ItemTouchHelper.this.mCallback.clearView(ItemTouchHelper.this.mRecyclerView, animation.mViewHolder);
                        }
                        ItemTouchHelper.this.select(animation.mViewHolder, animation.mActionState);
                        ItemTouchHelper.this.updateDxDy(event, ItemTouchHelper.this.mSelectedFlags, 0);
                    }
                }
            } else if (action == 3 || action == 1) {
                ItemTouchHelper.this.mActivePointerId = -1;
                ItemTouchHelper.this.select(null, 0);
            } else if (ItemTouchHelper.this.mActivePointerId != -1) {
                int index = event.findPointerIndex(ItemTouchHelper.this.mActivePointerId);
                if (index >= 0) {
                    ItemTouchHelper.this.checkSelectForSwipe(action, event, index);
                }
            }
            if (ItemTouchHelper.this.mVelocityTracker != null) {
                ItemTouchHelper.this.mVelocityTracker.addMovement(event);
            }
            if (ItemTouchHelper.this.mSelected != null) {
                return true;
            }
            return false;
        }

        public void onTouchEvent(RecyclerView recyclerView, MotionEvent event) {
            ItemTouchHelper.this.mGestureDetector.onTouchEvent(event);
            if (ItemTouchHelper.this.mVelocityTracker != null) {
                ItemTouchHelper.this.mVelocityTracker.addMovement(event);
            }
            if (ItemTouchHelper.this.mActivePointerId != -1) {
                int action = event.getActionMasked();
                int activePointerIndex = event.findPointerIndex(ItemTouchHelper.this.mActivePointerId);
                if (activePointerIndex >= 0) {
                    ItemTouchHelper.this.checkSelectForSwipe(action, event, activePointerIndex);
                }
                ViewHolder viewHolder = ItemTouchHelper.this.mSelected;
                if (viewHolder != null) {
                    switch (action) {
                        case 1:
                            break;
                        case 2:
                            if (activePointerIndex >= 0) {
                                ItemTouchHelper.this.updateDxDy(event, ItemTouchHelper.this.mSelectedFlags, activePointerIndex);
                                ItemTouchHelper.this.moveIfNecessary(viewHolder);
                                ItemTouchHelper.this.mRecyclerView.removeCallbacks(ItemTouchHelper.this.mScrollRunnable);
                                ItemTouchHelper.this.mScrollRunnable.run();
                                ItemTouchHelper.this.mRecyclerView.invalidate();
                                break;
                            }
                            break;
                        case 3:
                            if (ItemTouchHelper.this.mVelocityTracker != null) {
                                ItemTouchHelper.this.mVelocityTracker.clear();
                                break;
                            }
                            break;
                        case 6:
                            int pointerIndex = event.getActionIndex();
                            if (event.getPointerId(pointerIndex) == ItemTouchHelper.this.mActivePointerId) {
                                ItemTouchHelper.this.mActivePointerId = event.getPointerId(pointerIndex == 0 ? 1 : 0);
                                ItemTouchHelper.this.updateDxDy(event, ItemTouchHelper.this.mSelectedFlags, pointerIndex);
                                break;
                            }
                            break;
                    }
                    ItemTouchHelper.this.select(null, 0);
                    ItemTouchHelper.this.mActivePointerId = -1;
                }
            }
        }

        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            if (disallowIntercept) {
                ItemTouchHelper.this.select(null, 0);
            }
        }
    };
    View mOverdrawChild = null;
    int mOverdrawChildPosition = -1;
    final List<View> mPendingCleanup = new ArrayList();
    List<RecoverAnimation> mRecoverAnimations = new ArrayList();
    RecyclerView mRecyclerView;
    final Runnable mScrollRunnable = new Runnable() {
        public void run() {
            if (ItemTouchHelper.this.mSelected != null && ItemTouchHelper.this.scrollIfNecessary()) {
                if (ItemTouchHelper.this.mSelected != null) {
                    ItemTouchHelper.this.moveIfNecessary(ItemTouchHelper.this.mSelected);
                }
                ItemTouchHelper.this.mRecyclerView.removeCallbacks(ItemTouchHelper.this.mScrollRunnable);
                ItemTouchHelper.this.mRecyclerView.postOnAnimation(this);
            }
        }
    };
    ViewHolder mSelected = null;
    int mSelectedFlags;
    float mSelectedStartX;
    float mSelectedStartY;
    private int mSlop;
    private List<ViewHolder> mSwapTargets;
    float mSwipeEscapeVelocity;
    private final float[] mTmpPosition = new float[2];
    private Rect mTmpRect;
    VelocityTracker mVelocityTracker;

    public interface ViewDropHandler {
        void prepareForDrop(View view, View view2, int i, int i2);
    }

    private class RecoverAnimation implements AnimatorListener {
        final int mActionState;
        final int mAnimationType;
        boolean mEnded = false;
        private float mFraction;
        public boolean mIsPendingCleanup;
        boolean mOverridden = false;
        final float mStartDx;
        final float mStartDy;
        final float mTargetX;
        final float mTargetY;
        private final ValueAnimator mValueAnimator;
        final ViewHolder mViewHolder;
        float mX;
        float mY;

        RecoverAnimation(ViewHolder viewHolder, int animationType, int actionState, float startDx, float startDy, float targetX, float targetY) {
            this.mActionState = actionState;
            this.mAnimationType = animationType;
            this.mViewHolder = viewHolder;
            this.mStartDx = startDx;
            this.mStartDy = startDy;
            this.mTargetX = targetX;
            this.mTargetY = targetY;
            this.mValueAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            this.mValueAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    RecoverAnimation.this.setFraction(animation.getAnimatedFraction());
                }
            });
            this.mValueAnimator.setTarget(viewHolder.itemView);
            this.mValueAnimator.addListener(this);
            setFraction(0.0f);
        }

        public void setDuration(long duration) {
            this.mValueAnimator.setDuration(duration);
        }

        public void start() {
            this.mViewHolder.setIsRecyclable(false);
            this.mValueAnimator.start();
        }

        public void cancel() {
            this.mValueAnimator.cancel();
        }

        public void setFraction(float fraction) {
            this.mFraction = fraction;
        }

        public void update() {
            if (this.mStartDx == this.mTargetX) {
                this.mX = this.mViewHolder.itemView.getTranslationX();
            } else {
                this.mX = this.mStartDx + (this.mFraction * (this.mTargetX - this.mStartDx));
            }
            if (this.mStartDy == this.mTargetY) {
                this.mY = this.mViewHolder.itemView.getTranslationY();
            } else {
                this.mY = this.mStartDy + (this.mFraction * (this.mTargetY - this.mStartDy));
            }
        }

        public void onAnimationStart(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            if (!this.mEnded) {
                this.mViewHolder.setIsRecyclable(true);
            }
            this.mEnded = true;
        }

        public void onAnimationCancel(Animator animation) {
            setFraction(1.0f);
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    public static abstract class Callback {
        private static final int ABS_HORIZONTAL_DIR_FLAGS = 789516;
        public static final int DEFAULT_DRAG_ANIMATION_DURATION = 200;
        public static final int DEFAULT_SWIPE_ANIMATION_DURATION = 250;
        private static final long DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS = 2000;
        static final int RELATIVE_DIR_FLAGS = 3158064;
        private static final Interpolator sDragScrollInterpolator = new Interpolator() {
            public float getInterpolation(float t) {
                return (((t * t) * t) * t) * t;
            }
        };
        private static final Interpolator sDragViewScrollCapInterpolator = new Interpolator() {
            public float getInterpolation(float t) {
                t -= 1.0f;
                return ((((t * t) * t) * t) * t) + 1.0f;
            }
        };
        private static final ItemTouchUIUtil sUICallback = new ItemTouchUIUtilImpl();
        private int mCachedMaxScrollSpeed = -1;

        public abstract int getMovementFlags(RecyclerView recyclerView, ViewHolder viewHolder);

        public abstract boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder viewHolder2);

        public abstract void onSwiped(ViewHolder viewHolder, int i);

        public static ItemTouchUIUtil getDefaultUIUtil() {
            return sUICallback;
        }

        public static int convertToRelativeDirection(int flags, int layoutDirection) {
            int masked = flags & ABS_HORIZONTAL_DIR_FLAGS;
            if (masked == 0) {
                return flags;
            }
            flags &= ~masked;
            if (layoutDirection == 0) {
                return flags | (masked << 2);
            }
            return (flags | ((masked << 1) & -789517)) | (((masked << 1) & ABS_HORIZONTAL_DIR_FLAGS) << 2);
        }

        public static int makeMovementFlags(int dragFlags, int swipeFlags) {
            return (makeFlag(0, swipeFlags | dragFlags) | makeFlag(1, swipeFlags)) | makeFlag(2, dragFlags);
        }

        public static int makeFlag(int actionState, int directions) {
            return directions << (actionState * 8);
        }

        public int convertToAbsoluteDirection(int flags, int layoutDirection) {
            int masked = flags & RELATIVE_DIR_FLAGS;
            if (masked == 0) {
                return flags;
            }
            flags &= ~masked;
            if (layoutDirection == 0) {
                return flags | (masked >> 2);
            }
            return (flags | ((masked >> 1) & -3158065)) | (((masked >> 1) & RELATIVE_DIR_FLAGS) >> 2);
        }

        final int getAbsoluteMovementFlags(RecyclerView recyclerView, ViewHolder viewHolder) {
            return convertToAbsoluteDirection(getMovementFlags(recyclerView, viewHolder), recyclerView.getLayoutDirection());
        }

        boolean hasDragFlag(RecyclerView recyclerView, ViewHolder viewHolder) {
            if ((16711680 & getAbsoluteMovementFlags(recyclerView, viewHolder)) != 0) {
                return true;
            }
            return false;
        }

        boolean hasSwipeFlag(RecyclerView recyclerView, ViewHolder viewHolder) {
            if ((65280 & getAbsoluteMovementFlags(recyclerView, viewHolder)) != 0) {
                return true;
            }
            return false;
        }

        public boolean canDropOver(RecyclerView recyclerView, ViewHolder current, ViewHolder target) {
            return true;
        }

        public boolean isLongPressDragEnabled() {
            return true;
        }

        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        public int getBoundingBoxMargin() {
            return 0;
        }

        public float getSwipeThreshold(ViewHolder viewHolder) {
            return 0.5f;
        }

        public float getMoveThreshold(ViewHolder viewHolder) {
            return 0.5f;
        }

        public float getSwipeEscapeVelocity(float defaultValue) {
            return defaultValue;
        }

        public float getSwipeVelocityThreshold(float defaultValue) {
            return defaultValue;
        }

        public ViewHolder chooseDropTarget(ViewHolder selected, List<ViewHolder> dropTargets, int curX, int curY) {
            int right = curX + selected.itemView.getWidth();
            int bottom = curY + selected.itemView.getHeight();
            ViewHolder winner = null;
            int winnerScore = -1;
            int dx = curX - selected.itemView.getLeft();
            int dy = curY - selected.itemView.getTop();
            int targetsSize = dropTargets.size();
            for (int i = 0; i < targetsSize; i++) {
                int diff;
                int score;
                ViewHolder target = (ViewHolder) dropTargets.get(i);
                if (dx > 0) {
                    diff = target.itemView.getRight() - right;
                    if (diff < 0 && target.itemView.getRight() > selected.itemView.getRight()) {
                        score = Math.abs(diff);
                        if (score > winnerScore) {
                            winnerScore = score;
                            winner = target;
                        }
                    }
                }
                if (dx < 0) {
                    diff = target.itemView.getLeft() - curX;
                    if (diff > 0 && target.itemView.getLeft() < selected.itemView.getLeft()) {
                        score = Math.abs(diff);
                        if (score > winnerScore) {
                            winnerScore = score;
                            winner = target;
                        }
                    }
                }
                if (dy < 0) {
                    diff = target.itemView.getTop() - curY;
                    if (diff > 0 && target.itemView.getTop() < selected.itemView.getTop()) {
                        score = Math.abs(diff);
                        if (score > winnerScore) {
                            winnerScore = score;
                            winner = target;
                        }
                    }
                }
                if (dy > 0) {
                    diff = target.itemView.getBottom() - bottom;
                    if (diff < 0 && target.itemView.getBottom() > selected.itemView.getBottom()) {
                        score = Math.abs(diff);
                        if (score > winnerScore) {
                            winnerScore = score;
                            winner = target;
                        }
                    }
                }
            }
            return winner;
        }

        public void onSelectedChanged(ViewHolder viewHolder, int actionState) {
            if (viewHolder != null) {
                sUICallback.onSelected(viewHolder.itemView);
            }
        }

        private int getMaxDragScroll(RecyclerView recyclerView) {
            if (this.mCachedMaxScrollSpeed == -1) {
                this.mCachedMaxScrollSpeed = recyclerView.getResources().getDimensionPixelSize(R.dimen.item_touch_helper_max_drag_scroll_per_frame);
            }
            return this.mCachedMaxScrollSpeed;
        }

        public void onMoved(RecyclerView recyclerView, ViewHolder viewHolder, int fromPos, ViewHolder target, int toPos, int x, int y) {
            LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof ViewDropHandler) {
                ((ViewDropHandler) layoutManager).prepareForDrop(viewHolder.itemView, target.itemView, x, y);
                return;
            }
            if (layoutManager.canScrollHorizontally()) {
                if (layoutManager.getDecoratedLeft(target.itemView) <= recyclerView.getPaddingLeft()) {
                    recyclerView.scrollToPosition(toPos);
                }
                if (layoutManager.getDecoratedRight(target.itemView) >= recyclerView.getWidth() - recyclerView.getPaddingRight()) {
                    recyclerView.scrollToPosition(toPos);
                }
            }
            if (layoutManager.canScrollVertically()) {
                if (layoutManager.getDecoratedTop(target.itemView) <= recyclerView.getPaddingTop()) {
                    recyclerView.scrollToPosition(toPos);
                }
                if (layoutManager.getDecoratedBottom(target.itemView) >= recyclerView.getHeight() - recyclerView.getPaddingBottom()) {
                    recyclerView.scrollToPosition(toPos);
                }
            }
        }

        void onDraw(Canvas c, RecyclerView parent, ViewHolder selected, List<RecoverAnimation> recoverAnimationList, int actionState, float dX, float dY) {
            int count;
            int recoverAnimSize = recoverAnimationList.size();
            for (int i = 0; i < recoverAnimSize; i++) {
                RecoverAnimation anim = (RecoverAnimation) recoverAnimationList.get(i);
                anim.update();
                count = c.save();
                onChildDraw(c, parent, anim.mViewHolder, anim.mX, anim.mY, anim.mActionState, false);
                c.restoreToCount(count);
            }
            if (selected != null) {
                count = c.save();
                onChildDraw(c, parent, selected, dX, dY, actionState, true);
                c.restoreToCount(count);
            }
        }

        void onDrawOver(Canvas c, RecyclerView parent, ViewHolder selected, List<RecoverAnimation> recoverAnimationList, int actionState, float dX, float dY) {
            int i;
            RecoverAnimation anim;
            int count;
            int recoverAnimSize = recoverAnimationList.size();
            for (i = 0; i < recoverAnimSize; i++) {
                anim = (RecoverAnimation) recoverAnimationList.get(i);
                count = c.save();
                onChildDrawOver(c, parent, anim.mViewHolder, anim.mX, anim.mY, anim.mActionState, false);
                c.restoreToCount(count);
            }
            if (selected != null) {
                count = c.save();
                onChildDrawOver(c, parent, selected, dX, dY, actionState, true);
                c.restoreToCount(count);
            }
            boolean hasRunningAnimation = false;
            for (i = recoverAnimSize - 1; i >= 0; i--) {
                anim = (RecoverAnimation) recoverAnimationList.get(i);
                if (anim.mEnded && (anim.mIsPendingCleanup ^ 1) != 0) {
                    recoverAnimationList.remove(i);
                } else if (!anim.mEnded) {
                    hasRunningAnimation = true;
                }
            }
            if (hasRunningAnimation) {
                parent.invalidate();
            }
        }

        public void clearView(RecyclerView recyclerView, ViewHolder viewHolder) {
            sUICallback.clearView(viewHolder.itemView);
        }

        public void onChildDraw(Canvas c, RecyclerView recyclerView, ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            sUICallback.onDraw(c, recyclerView, viewHolder.itemView, dX, dY, actionState, isCurrentlyActive);
        }

        public void onChildDrawOver(Canvas c, RecyclerView recyclerView, ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            sUICallback.onDrawOver(c, recyclerView, viewHolder.itemView, dX, dY, actionState, isCurrentlyActive);
        }

        public long getAnimationDuration(RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
            ItemAnimator itemAnimator = recyclerView.getItemAnimator();
            if (itemAnimator == null) {
                int i;
                if (animationType == 8) {
                    i = 200;
                } else {
                    i = 250;
                }
                return (long) i;
            }
            long moveDuration;
            if (animationType == 8) {
                moveDuration = itemAnimator.getMoveDuration();
            } else {
                moveDuration = itemAnimator.getRemoveDuration();
            }
            return moveDuration;
        }

        public int interpolateOutOfBoundsScroll(RecyclerView recyclerView, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
            float timeRatio;
            int direction = (int) Math.signum((float) viewSizeOutOfBounds);
            int cappedScroll = (int) (((float) (direction * getMaxDragScroll(recyclerView))) * sDragViewScrollCapInterpolator.getInterpolation(Math.min(1.0f, (((float) Math.abs(viewSizeOutOfBounds)) * 1.0f) / ((float) viewSize))));
            if (msSinceStartScroll > DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS) {
                timeRatio = 1.0f;
            } else {
                timeRatio = ((float) msSinceStartScroll) / 2000.0f;
            }
            int value = (int) (((float) cappedScroll) * sDragScrollInterpolator.getInterpolation(timeRatio));
            if (value != 0) {
                return value;
            }
            int i;
            if (viewSizeOutOfBounds > 0) {
                i = 1;
            } else {
                i = -1;
            }
            return i;
        }
    }

    private class ItemTouchHelperGestureListener extends SimpleOnGestureListener {
        ItemTouchHelperGestureListener() {
        }

        public boolean onDown(MotionEvent e) {
            return true;
        }

        public void onLongPress(MotionEvent e) {
            View child = ItemTouchHelper.this.findChildView(e);
            if (child != null) {
                ViewHolder vh = ItemTouchHelper.this.mRecyclerView.getChildViewHolder(child);
                if (vh != null && ItemTouchHelper.this.mCallback.hasDragFlag(ItemTouchHelper.this.mRecyclerView, vh) && e.getPointerId(0) == ItemTouchHelper.this.mActivePointerId) {
                    int index = e.findPointerIndex(ItemTouchHelper.this.mActivePointerId);
                    float x = e.getX(index);
                    float y = e.getY(index);
                    ItemTouchHelper.this.mInitialTouchX = x;
                    ItemTouchHelper.this.mInitialTouchY = y;
                    ItemTouchHelper itemTouchHelper = ItemTouchHelper.this;
                    ItemTouchHelper.this.mDy = 0.0f;
                    itemTouchHelper.mDx = 0.0f;
                    if (ItemTouchHelper.this.mCallback.isLongPressDragEnabled()) {
                        ItemTouchHelper.this.select(vh, 2);
                    }
                }
            }
        }
    }

    public static abstract class SimpleCallback extends Callback {
        private int mDefaultDragDirs;
        private int mDefaultSwipeDirs;

        public SimpleCallback(int dragDirs, int swipeDirs) {
            this.mDefaultSwipeDirs = swipeDirs;
            this.mDefaultDragDirs = dragDirs;
        }

        public void setDefaultSwipeDirs(int defaultSwipeDirs) {
            this.mDefaultSwipeDirs = defaultSwipeDirs;
        }

        public void setDefaultDragDirs(int defaultDragDirs) {
            this.mDefaultDragDirs = defaultDragDirs;
        }

        public int getSwipeDirs(RecyclerView recyclerView, ViewHolder viewHolder) {
            return this.mDefaultSwipeDirs;
        }

        public int getDragDirs(RecyclerView recyclerView, ViewHolder viewHolder) {
            return this.mDefaultDragDirs;
        }

        public int getMovementFlags(RecyclerView recyclerView, ViewHolder viewHolder) {
            return Callback.makeMovementFlags(getDragDirs(recyclerView, viewHolder), getSwipeDirs(recyclerView, viewHolder));
        }
    }

    public ItemTouchHelper(Callback callback) {
        this.mCallback = callback;
    }

    private static boolean hitTest(View child, float x, float y, float left, float top) {
        if (x < left || x > ((float) child.getWidth()) + left || y < top || y > ((float) child.getHeight()) + top) {
            return false;
        }
        return true;
    }

    public void attachToRecyclerView(RecyclerView recyclerView) {
        if (this.mRecyclerView != recyclerView) {
            if (this.mRecyclerView != null) {
                destroyCallbacks();
            }
            this.mRecyclerView = recyclerView;
            if (this.mRecyclerView != null) {
                Resources resources = recyclerView.getResources();
                this.mSwipeEscapeVelocity = resources.getDimension(R.dimen.item_touch_helper_swipe_escape_velocity);
                this.mMaxSwipeVelocity = resources.getDimension(R.dimen.item_touch_helper_swipe_escape_max_velocity);
                setupCallbacks();
            }
        }
    }

    private void setupCallbacks() {
        this.mSlop = ViewConfiguration.get(this.mRecyclerView.getContext()).getScaledTouchSlop();
        this.mRecyclerView.addItemDecoration(this);
        this.mRecyclerView.addOnItemTouchListener(this.mOnItemTouchListener);
        this.mRecyclerView.addOnChildAttachStateChangeListener(this);
        initGestureDetector();
    }

    private void destroyCallbacks() {
        this.mRecyclerView.removeItemDecoration(this);
        this.mRecyclerView.removeOnItemTouchListener(this.mOnItemTouchListener);
        this.mRecyclerView.removeOnChildAttachStateChangeListener(this);
        for (int i = this.mRecoverAnimations.size() - 1; i >= 0; i--) {
            this.mCallback.clearView(this.mRecyclerView, ((RecoverAnimation) this.mRecoverAnimations.get(0)).mViewHolder);
        }
        this.mRecoverAnimations.clear();
        this.mOverdrawChild = null;
        this.mOverdrawChildPosition = -1;
        releaseVelocityTracker();
    }

    private void initGestureDetector() {
        if (this.mGestureDetector == null) {
            this.mGestureDetector = new GestureDetector(this.mRecyclerView.getContext(), new ItemTouchHelperGestureListener());
        }
    }

    private void getSelectedDxDy(float[] outPosition) {
        if ((this.mSelectedFlags & 12) != 0) {
            outPosition[0] = (this.mSelectedStartX + this.mDx) - ((float) this.mSelected.itemView.getLeft());
        } else {
            outPosition[0] = this.mSelected.itemView.getTranslationX();
        }
        if ((this.mSelectedFlags & 3) != 0) {
            outPosition[1] = (this.mSelectedStartY + this.mDy) - ((float) this.mSelected.itemView.getTop());
        } else {
            outPosition[1] = this.mSelected.itemView.getTranslationY();
        }
    }

    public void onDrawOver(Canvas c, RecyclerView parent, State state) {
        float dx = 0.0f;
        float dy = 0.0f;
        if (this.mSelected != null) {
            getSelectedDxDy(this.mTmpPosition);
            dx = this.mTmpPosition[0];
            dy = this.mTmpPosition[1];
        }
        this.mCallback.onDrawOver(c, parent, this.mSelected, this.mRecoverAnimations, this.mActionState, dx, dy);
    }

    public void onDraw(Canvas c, RecyclerView parent, State state) {
        this.mOverdrawChildPosition = -1;
        float dx = 0.0f;
        float dy = 0.0f;
        if (this.mSelected != null) {
            getSelectedDxDy(this.mTmpPosition);
            dx = this.mTmpPosition[0];
            dy = this.mTmpPosition[1];
        }
        this.mCallback.onDraw(c, parent, this.mSelected, this.mRecoverAnimations, this.mActionState, dx, dy);
    }

    void select(ViewHolder selected, int actionState) {
        if (selected != this.mSelected || actionState != this.mActionState) {
            this.mDragScrollStartTimeInMs = Long.MIN_VALUE;
            int prevActionState = this.mActionState;
            endRecoverAnimation(selected, true);
            this.mActionState = actionState;
            if (actionState == 2) {
                this.mOverdrawChild = selected.itemView;
                addChildDrawingOrderCallback();
            }
            int actionStateMask = (1 << ((actionState * 8) + 8)) - 1;
            boolean preventLayout = false;
            if (this.mSelected != null) {
                ViewHolder prevSelected = this.mSelected;
                if (prevSelected.itemView.getParent() != null) {
                    int swipeDir;
                    float targetTranslateX;
                    float targetTranslateY;
                    int animationType;
                    if (prevActionState == 2) {
                        swipeDir = 0;
                    } else {
                        swipeDir = swipeIfNecessary(prevSelected);
                    }
                    releaseVelocityTracker();
                    switch (swipeDir) {
                        case 1:
                        case 2:
                            targetTranslateX = 0.0f;
                            targetTranslateY = Math.signum(this.mDy) * ((float) this.mRecyclerView.getHeight());
                            break;
                        case 4:
                        case 8:
                        case 16:
                        case 32:
                            targetTranslateY = 0.0f;
                            targetTranslateX = Math.signum(this.mDx) * ((float) this.mRecyclerView.getWidth());
                            break;
                        default:
                            targetTranslateX = 0.0f;
                            targetTranslateY = 0.0f;
                            break;
                    }
                    if (prevActionState == 2) {
                        animationType = 8;
                    } else if (swipeDir > 0) {
                        animationType = 2;
                    } else {
                        animationType = 4;
                    }
                    getSelectedDxDy(this.mTmpPosition);
                    float currentTranslateX = this.mTmpPosition[0];
                    float currentTranslateY = this.mTmpPosition[1];
                    final ViewHolder viewHolder = prevSelected;
                    RecoverAnimation rv = new RecoverAnimation(this, prevSelected, animationType, prevActionState, currentTranslateX, currentTranslateY, targetTranslateX, targetTranslateY) {
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            if (!this.mOverridden) {
                                if (swipeDir <= 0) {
                                    this.mCallback.clearView(this.mRecyclerView, viewHolder);
                                } else {
                                    this.mPendingCleanup.add(viewHolder.itemView);
                                    this.mIsPendingCleanup = true;
                                    if (swipeDir > 0) {
                                        this.postDispatchSwipe(this, swipeDir);
                                    }
                                }
                                if (this.mOverdrawChild == viewHolder.itemView) {
                                    this.removeChildDrawingOrderCallbackIfNecessary(viewHolder.itemView);
                                }
                            }
                        }
                    };
                    rv.setDuration(this.mCallback.getAnimationDuration(this.mRecyclerView, animationType, targetTranslateX - currentTranslateX, targetTranslateY - currentTranslateY));
                    this.mRecoverAnimations.add(rv);
                    rv.start();
                    preventLayout = true;
                } else {
                    removeChildDrawingOrderCallbackIfNecessary(prevSelected.itemView);
                    this.mCallback.clearView(this.mRecyclerView, prevSelected);
                }
                this.mSelected = null;
            }
            if (selected != null) {
                this.mSelectedFlags = (this.mCallback.getAbsoluteMovementFlags(this.mRecyclerView, selected) & actionStateMask) >> (this.mActionState * 8);
                this.mSelectedStartX = (float) selected.itemView.getLeft();
                this.mSelectedStartY = (float) selected.itemView.getTop();
                this.mSelected = selected;
                if (actionState == 2) {
                    this.mSelected.itemView.performHapticFeedback(0);
                }
            }
            ViewParent rvParent = this.mRecyclerView.getParent();
            if (rvParent != null) {
                rvParent.requestDisallowInterceptTouchEvent(this.mSelected != null);
            }
            if (!preventLayout) {
                this.mRecyclerView.getLayoutManager().requestSimpleAnimationsInNextLayout();
            }
            this.mCallback.onSelectedChanged(this.mSelected, this.mActionState);
            this.mRecyclerView.invalidate();
        }
    }

    void postDispatchSwipe(final RecoverAnimation anim, final int swipeDir) {
        this.mRecyclerView.post(new Runnable() {
            public void run() {
                if (ItemTouchHelper.this.mRecyclerView != null && ItemTouchHelper.this.mRecyclerView.isAttachedToWindow() && (anim.mOverridden ^ 1) != 0 && anim.mViewHolder.getAdapterPosition() != -1) {
                    ItemAnimator animator = ItemTouchHelper.this.mRecyclerView.getItemAnimator();
                    if ((animator == null || (animator.isRunning(null) ^ 1) != 0) && (ItemTouchHelper.this.hasRunningRecoverAnim() ^ 1) != 0) {
                        ItemTouchHelper.this.mCallback.onSwiped(anim.mViewHolder, swipeDir);
                    } else {
                        ItemTouchHelper.this.mRecyclerView.post(this);
                    }
                }
            }
        });
    }

    boolean hasRunningRecoverAnim() {
        int size = this.mRecoverAnimations.size();
        for (int i = 0; i < size; i++) {
            if (!((RecoverAnimation) this.mRecoverAnimations.get(i)).mEnded) {
                return true;
            }
        }
        return false;
    }

    boolean scrollIfNecessary() {
        if (this.mSelected == null) {
            this.mDragScrollStartTimeInMs = Long.MIN_VALUE;
            return false;
        }
        long now = System.currentTimeMillis();
        long scrollDuration = this.mDragScrollStartTimeInMs == Long.MIN_VALUE ? 0 : now - this.mDragScrollStartTimeInMs;
        LayoutManager lm = this.mRecyclerView.getLayoutManager();
        if (this.mTmpRect == null) {
            this.mTmpRect = new Rect();
        }
        int scrollX = 0;
        int scrollY = 0;
        lm.calculateItemDecorationsForChild(this.mSelected.itemView, this.mTmpRect);
        if (lm.canScrollHorizontally()) {
            int curX = (int) (this.mSelectedStartX + this.mDx);
            int leftDiff = (curX - this.mTmpRect.left) - this.mRecyclerView.getPaddingLeft();
            if (this.mDx < 0.0f && leftDiff < 0) {
                scrollX = leftDiff;
            } else if (this.mDx > 0.0f) {
                int rightDiff = ((this.mSelected.itemView.getWidth() + curX) + this.mTmpRect.right) - (this.mRecyclerView.getWidth() - this.mRecyclerView.getPaddingRight());
                if (rightDiff > 0) {
                    scrollX = rightDiff;
                }
            }
        }
        if (lm.canScrollVertically()) {
            int curY = (int) (this.mSelectedStartY + this.mDy);
            int topDiff = (curY - this.mTmpRect.top) - this.mRecyclerView.getPaddingTop();
            if (this.mDy < 0.0f && topDiff < 0) {
                scrollY = topDiff;
            } else if (this.mDy > 0.0f) {
                int bottomDiff = ((this.mSelected.itemView.getHeight() + curY) + this.mTmpRect.bottom) - (this.mRecyclerView.getHeight() - this.mRecyclerView.getPaddingBottom());
                if (bottomDiff > 0) {
                    scrollY = bottomDiff;
                }
            }
        }
        if (scrollX != 0) {
            scrollX = this.mCallback.interpolateOutOfBoundsScroll(this.mRecyclerView, this.mSelected.itemView.getWidth(), scrollX, this.mRecyclerView.getWidth(), scrollDuration);
        }
        if (scrollY != 0) {
            scrollY = this.mCallback.interpolateOutOfBoundsScroll(this.mRecyclerView, this.mSelected.itemView.getHeight(), scrollY, this.mRecyclerView.getHeight(), scrollDuration);
        }
        if (scrollX == 0 && scrollY == 0) {
            this.mDragScrollStartTimeInMs = Long.MIN_VALUE;
            return false;
        }
        if (this.mDragScrollStartTimeInMs == Long.MIN_VALUE) {
            this.mDragScrollStartTimeInMs = now;
        }
        this.mRecyclerView.scrollBy(scrollX, scrollY);
        return true;
    }

    private List<ViewHolder> findSwapTargets(ViewHolder viewHolder) {
        if (this.mSwapTargets == null) {
            this.mSwapTargets = new ArrayList();
            this.mDistances = new ArrayList();
        } else {
            this.mSwapTargets.clear();
            this.mDistances.clear();
        }
        int margin = this.mCallback.getBoundingBoxMargin();
        int left = Math.round(this.mSelectedStartX + this.mDx) - margin;
        int top = Math.round(this.mSelectedStartY + this.mDy) - margin;
        int right = (viewHolder.itemView.getWidth() + left) + (margin * 2);
        int bottom = (viewHolder.itemView.getHeight() + top) + (margin * 2);
        int centerX = (left + right) / 2;
        int centerY = (top + bottom) / 2;
        LayoutManager lm = this.mRecyclerView.getLayoutManager();
        int childCount = lm.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View other = lm.getChildAt(i);
            if (other != viewHolder.itemView && other.getBottom() >= top && other.getTop() <= bottom && other.getRight() >= left && other.getLeft() <= right) {
                ViewHolder otherVh = this.mRecyclerView.getChildViewHolder(other);
                if (this.mCallback.canDropOver(this.mRecyclerView, this.mSelected, otherVh)) {
                    int dx = Math.abs(centerX - ((other.getLeft() + other.getRight()) / 2));
                    int dy = Math.abs(centerY - ((other.getTop() + other.getBottom()) / 2));
                    int dist = (dx * dx) + (dy * dy);
                    int pos = 0;
                    int cnt = this.mSwapTargets.size();
                    int j = 0;
                    while (j < cnt && dist > ((Integer) this.mDistances.get(j)).intValue()) {
                        pos++;
                        j++;
                    }
                    this.mSwapTargets.add(pos, otherVh);
                    this.mDistances.add(pos, Integer.valueOf(dist));
                }
            }
        }
        return this.mSwapTargets;
    }

    void moveIfNecessary(ViewHolder viewHolder) {
        if (!this.mRecyclerView.isLayoutRequested() && this.mActionState == 2) {
            float threshold = this.mCallback.getMoveThreshold(viewHolder);
            int x = (int) (this.mSelectedStartX + this.mDx);
            int y = (int) (this.mSelectedStartY + this.mDy);
            if (((float) Math.abs(y - viewHolder.itemView.getTop())) >= ((float) viewHolder.itemView.getHeight()) * threshold || ((float) Math.abs(x - viewHolder.itemView.getLeft())) >= ((float) viewHolder.itemView.getWidth()) * threshold) {
                List<ViewHolder> swapTargets = findSwapTargets(viewHolder);
                if (swapTargets.size() != 0) {
                    ViewHolder target = this.mCallback.chooseDropTarget(viewHolder, swapTargets, x, y);
                    if (target == null) {
                        this.mSwapTargets.clear();
                        this.mDistances.clear();
                        return;
                    }
                    int toPosition = target.getAdapterPosition();
                    int fromPosition = viewHolder.getAdapterPosition();
                    if (this.mCallback.onMove(this.mRecyclerView, viewHolder, target)) {
                        this.mCallback.onMoved(this.mRecyclerView, viewHolder, fromPosition, target, toPosition, x, y);
                    }
                }
            }
        }
    }

    public void onChildViewAttachedToWindow(View view) {
    }

    public void onChildViewDetachedFromWindow(View view) {
        removeChildDrawingOrderCallbackIfNecessary(view);
        ViewHolder holder = this.mRecyclerView.getChildViewHolder(view);
        if (holder != null) {
            if (this.mSelected == null || holder != this.mSelected) {
                endRecoverAnimation(holder, false);
                if (this.mPendingCleanup.remove(holder.itemView)) {
                    this.mCallback.clearView(this.mRecyclerView, holder);
                }
            } else {
                select(null, 0);
            }
        }
    }

    int endRecoverAnimation(ViewHolder viewHolder, boolean override) {
        for (int i = this.mRecoverAnimations.size() - 1; i >= 0; i--) {
            RecoverAnimation anim = (RecoverAnimation) this.mRecoverAnimations.get(i);
            if (anim.mViewHolder == viewHolder) {
                anim.mOverridden |= override;
                if (!anim.mEnded) {
                    anim.cancel();
                }
                this.mRecoverAnimations.remove(i);
                return anim.mAnimationType;
            }
        }
        return 0;
    }

    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        outRect.setEmpty();
    }

    void obtainVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
        }
        this.mVelocityTracker = VelocityTracker.obtain();
    }

    private void releaseVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private ViewHolder findSwipedView(MotionEvent motionEvent) {
        LayoutManager lm = this.mRecyclerView.getLayoutManager();
        if (this.mActivePointerId == -1) {
            return null;
        }
        int pointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
        float dy = motionEvent.getY(pointerIndex) - this.mInitialTouchY;
        float absDx = Math.abs(motionEvent.getX(pointerIndex) - this.mInitialTouchX);
        float absDy = Math.abs(dy);
        if (absDx < ((float) this.mSlop) && absDy < ((float) this.mSlop)) {
            return null;
        }
        if (absDx > absDy && lm.canScrollHorizontally()) {
            return null;
        }
        if (absDy > absDx && lm.canScrollVertically()) {
            return null;
        }
        View child = findChildView(motionEvent);
        if (child == null) {
            return null;
        }
        return this.mRecyclerView.getChildViewHolder(child);
    }

    boolean checkSelectForSwipe(int action, MotionEvent motionEvent, int pointerIndex) {
        if (this.mSelected != null || action != 2 || this.mActionState == 2 || (this.mCallback.isItemViewSwipeEnabled() ^ 1) != 0) {
            return false;
        }
        if (this.mRecyclerView.getScrollState() == 1) {
            return false;
        }
        ViewHolder vh = findSwipedView(motionEvent);
        if (vh == null) {
            return false;
        }
        int swipeFlags = (65280 & this.mCallback.getAbsoluteMovementFlags(this.mRecyclerView, vh)) >> 8;
        if (swipeFlags == 0) {
            return false;
        }
        float x = motionEvent.getX(pointerIndex);
        float dx = x - this.mInitialTouchX;
        float dy = motionEvent.getY(pointerIndex) - this.mInitialTouchY;
        float absDx = Math.abs(dx);
        float absDy = Math.abs(dy);
        if (absDx < ((float) this.mSlop) && absDy < ((float) this.mSlop)) {
            return false;
        }
        if (absDx > absDy) {
            if (dx < 0.0f && (swipeFlags & 4) == 0) {
                return false;
            }
            if (dx > 0.0f && (swipeFlags & 8) == 0) {
                return false;
            }
        } else if (dy < 0.0f && (swipeFlags & 1) == 0) {
            return false;
        } else {
            if (dy > 0.0f && (swipeFlags & 2) == 0) {
                return false;
            }
        }
        this.mDy = 0.0f;
        this.mDx = 0.0f;
        this.mActivePointerId = motionEvent.getPointerId(0);
        select(vh, 1);
        return true;
    }

    View findChildView(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (this.mSelected != null) {
            View selectedView = this.mSelected.itemView;
            if (hitTest(selectedView, x, y, this.mSelectedStartX + this.mDx, this.mSelectedStartY + this.mDy)) {
                return selectedView;
            }
        }
        for (int i = this.mRecoverAnimations.size() - 1; i >= 0; i--) {
            RecoverAnimation anim = (RecoverAnimation) this.mRecoverAnimations.get(i);
            View view = anim.mViewHolder.itemView;
            if (hitTest(view, x, y, anim.mX, anim.mY)) {
                return view;
            }
        }
        return this.mRecyclerView.findChildViewUnder(x, y);
    }

    public void startDrag(ViewHolder viewHolder) {
        if (!this.mCallback.hasDragFlag(this.mRecyclerView, viewHolder)) {
            Log.e(TAG, "Start drag has been called but dragging is not enabled");
        } else if (viewHolder.itemView.getParent() != this.mRecyclerView) {
            Log.e(TAG, "Start drag has been called with a view holder which is not a child of the RecyclerView which is controlled by this ItemTouchHelper.");
        } else {
            obtainVelocityTracker();
            this.mDy = 0.0f;
            this.mDx = 0.0f;
            select(viewHolder, 2);
        }
    }

    public void startSwipe(ViewHolder viewHolder) {
        if (!this.mCallback.hasSwipeFlag(this.mRecyclerView, viewHolder)) {
            Log.e(TAG, "Start swipe has been called but swiping is not enabled");
        } else if (viewHolder.itemView.getParent() != this.mRecyclerView) {
            Log.e(TAG, "Start swipe has been called with a view holder which is not a child of the RecyclerView controlled by this ItemTouchHelper.");
        } else {
            obtainVelocityTracker();
            this.mDy = 0.0f;
            this.mDx = 0.0f;
            select(viewHolder, 1);
        }
    }

    RecoverAnimation findAnimation(MotionEvent event) {
        if (this.mRecoverAnimations.isEmpty()) {
            return null;
        }
        View target = findChildView(event);
        for (int i = this.mRecoverAnimations.size() - 1; i >= 0; i--) {
            RecoverAnimation anim = (RecoverAnimation) this.mRecoverAnimations.get(i);
            if (anim.mViewHolder.itemView == target) {
                return anim;
            }
        }
        return null;
    }

    void updateDxDy(MotionEvent ev, int directionFlags, int pointerIndex) {
        float x = ev.getX(pointerIndex);
        float y = ev.getY(pointerIndex);
        this.mDx = x - this.mInitialTouchX;
        this.mDy = y - this.mInitialTouchY;
        if ((directionFlags & 4) == 0) {
            this.mDx = Math.max(0.0f, this.mDx);
        }
        if ((directionFlags & 8) == 0) {
            this.mDx = Math.min(0.0f, this.mDx);
        }
        if ((directionFlags & 1) == 0) {
            this.mDy = Math.max(0.0f, this.mDy);
        }
        if ((directionFlags & 2) == 0) {
            this.mDy = Math.min(0.0f, this.mDy);
        }
    }

    private int swipeIfNecessary(ViewHolder viewHolder) {
        if (this.mActionState == 2) {
            return 0;
        }
        int originalMovementFlags = this.mCallback.getMovementFlags(this.mRecyclerView, viewHolder);
        int flags = (this.mCallback.convertToAbsoluteDirection(originalMovementFlags, this.mRecyclerView.getLayoutDirection()) & 65280) >> 8;
        if (flags == 0) {
            return 0;
        }
        int originalFlags = (originalMovementFlags & 65280) >> 8;
        int swipeDir;
        if (Math.abs(this.mDx) > Math.abs(this.mDy)) {
            swipeDir = checkHorizontalSwipe(viewHolder, flags);
            if (swipeDir <= 0) {
                swipeDir = checkVerticalSwipe(viewHolder, flags);
                if (swipeDir > 0) {
                    return swipeDir;
                }
            } else if ((originalFlags & swipeDir) == 0) {
                return Callback.convertToRelativeDirection(swipeDir, this.mRecyclerView.getLayoutDirection());
            } else {
                return swipeDir;
            }
        }
        swipeDir = checkVerticalSwipe(viewHolder, flags);
        if (swipeDir > 0) {
            return swipeDir;
        }
        swipeDir = checkHorizontalSwipe(viewHolder, flags);
        if (swipeDir > 0) {
            if ((originalFlags & swipeDir) == 0) {
                return Callback.convertToRelativeDirection(swipeDir, this.mRecyclerView.getLayoutDirection());
            }
            return swipeDir;
        }
        return 0;
    }

    private int checkHorizontalSwipe(ViewHolder viewHolder, int flags) {
        if ((flags & 12) != 0) {
            int dirFlag = this.mDx > 0.0f ? 8 : 4;
            if (this.mVelocityTracker != null && this.mActivePointerId > -1) {
                this.mVelocityTracker.computeCurrentVelocity(1000, this.mCallback.getSwipeVelocityThreshold(this.mMaxSwipeVelocity));
                float xVelocity = this.mVelocityTracker.getXVelocity(this.mActivePointerId);
                float yVelocity = this.mVelocityTracker.getYVelocity(this.mActivePointerId);
                int velDirFlag = xVelocity > 0.0f ? 8 : 4;
                float absXVelocity = Math.abs(xVelocity);
                if ((velDirFlag & flags) != 0 && dirFlag == velDirFlag && absXVelocity >= this.mCallback.getSwipeEscapeVelocity(this.mSwipeEscapeVelocity) && absXVelocity > Math.abs(yVelocity)) {
                    return velDirFlag;
                }
            }
            float threshold = ((float) this.mRecyclerView.getWidth()) * this.mCallback.getSwipeThreshold(viewHolder);
            if ((flags & dirFlag) == 0 || Math.abs(this.mDx) <= threshold) {
                return 0;
            }
            return dirFlag;
        }
        return 0;
    }

    private int checkVerticalSwipe(ViewHolder viewHolder, int flags) {
        if ((flags & 3) != 0) {
            int dirFlag = this.mDy > 0.0f ? 2 : 1;
            if (this.mVelocityTracker != null && this.mActivePointerId > -1) {
                this.mVelocityTracker.computeCurrentVelocity(1000, this.mCallback.getSwipeVelocityThreshold(this.mMaxSwipeVelocity));
                float xVelocity = this.mVelocityTracker.getXVelocity(this.mActivePointerId);
                float yVelocity = this.mVelocityTracker.getYVelocity(this.mActivePointerId);
                int velDirFlag = yVelocity > 0.0f ? 2 : 1;
                float absYVelocity = Math.abs(yVelocity);
                if ((velDirFlag & flags) != 0 && velDirFlag == dirFlag && absYVelocity >= this.mCallback.getSwipeEscapeVelocity(this.mSwipeEscapeVelocity) && absYVelocity > Math.abs(xVelocity)) {
                    return velDirFlag;
                }
            }
            float threshold = ((float) this.mRecyclerView.getHeight()) * this.mCallback.getSwipeThreshold(viewHolder);
            if ((flags & dirFlag) == 0 || Math.abs(this.mDy) <= threshold) {
                return 0;
            }
            return dirFlag;
        }
        return 0;
    }

    private void addChildDrawingOrderCallback() {
        if (VERSION.SDK_INT < 21) {
            if (this.mChildDrawingOrderCallback == null) {
                this.mChildDrawingOrderCallback = new ChildDrawingOrderCallback() {
                    public int onGetChildDrawingOrder(int childCount, int i) {
                        if (ItemTouchHelper.this.mOverdrawChild == null) {
                            return i;
                        }
                        int childPosition = ItemTouchHelper.this.mOverdrawChildPosition;
                        if (childPosition == -1) {
                            childPosition = ItemTouchHelper.this.mRecyclerView.indexOfChild(ItemTouchHelper.this.mOverdrawChild);
                            ItemTouchHelper.this.mOverdrawChildPosition = childPosition;
                        }
                        if (i == childCount - 1) {
                            return childPosition;
                        }
                        if (i >= childPosition) {
                            i++;
                        }
                        return i;
                    }
                };
            }
            this.mRecyclerView.setChildDrawingOrderCallback(this.mChildDrawingOrderCallback);
        }
    }

    void removeChildDrawingOrderCallbackIfNecessary(View view) {
        if (view == this.mOverdrawChild) {
            this.mOverdrawChild = null;
            if (this.mChildDrawingOrderCallback != null) {
                this.mRecyclerView.setChildDrawingOrderCallback(null);
            }
        }
    }
}
