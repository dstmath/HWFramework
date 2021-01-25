package huawei.com.android.internal.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Scroller;
import com.android.internal.widget.ActionBarContextView;
import com.android.internal.widget.ActionBarOverlayLayout;
import com.android.internal.widget.DecorToolbar;
import com.huawei.internal.widget.ConstantValues;
import huawei.com.android.internal.app.HwActionBarImpl;
import java.util.ArrayList;

public class HwActionBarOverlayLayout extends ActionBarOverlayLayout {
    private static final int ALPHA_ANIMATION_DELAY = 133;
    private static final int ALPHA_ANIMATION_DURATION = 167;
    private static final float ALPHA_ANIMATION_START = 0.3325f;
    private static final int DEFAULT_INDEX_VALUE = -1;
    private static final int FLING_THRESHOLD = 2200;
    private static final int HW_OVERLAY_ACTIONBAR_NEG = -1;
    private static final int ILLEGAL_VALUE = -1;
    private static final int INVALID_POINTER = -1;
    private static final int MAX_CP_LOCS = 2;
    private static final int MAX_CT_LOCS = 2;
    private static final float NEG_Z_VALUE = -1.0f;
    private static final int SCROLL_ANIMATION_DURATION = 400;
    private static final int SPLIT_ACTION_BAR = -1;
    private static final int STAGE_ONE = 1;
    private static final int STAGE_RESET = 0;
    private static final int STAGE_TWO = 2;
    private static final String TAG = "HwActionBarOverlayLayout";
    private static final int THRESHOLD_DRAG_PIX = 10;
    private static final float THRESHOLD_PROGRESS = 0.5f;
    private static final int VELOCITY_LESS_THAN_ZERO = -1;
    private int mActivePointerId = -1;
    private HwActionBarImpl.InnerOnStageChangedListener mCallback;
    private boolean mCanDragFromContent = false;
    private int mCustViewHeight;
    private HwCustomPanel mCustomPanel;
    private int mDragStage = 0;
    private int mFirstCustViewHeight = 0;
    private GestureDetector mGestureDetector;
    private int mHwOverlayActionBar;
    private boolean mIsAnimationEnable = true;
    private boolean mIsBeingDragged;
    private boolean mIsDraggable = false;
    private boolean mIsDrawerOpened;
    private boolean mIsHwDrawerFeature;
    private boolean mIsLazyMode = false;
    private boolean mIsStage2Changed = false;
    private boolean mIsStageChanged = true;
    private boolean mIsStartStageChanged = false;
    private int mLastMotionY;
    private Scroller mScroller;
    private int mSecondCustViewHeight = 0;
    private HwActionBarImpl.InnerOnStageChangedListener mStartCallback;
    private final ArrayList<View> mStillViews = new ArrayList<>();
    private int mTouchSlop = 0;
    private int mUiOptions;

    public HwActionBarOverlayLayout(Context context) {
        super(context);
    }

    public HwActionBarOverlayLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (motionEvent == null) {
            return false;
        }
        initCustomPanel();
        if (!this.mIsDraggable || !this.mIsAnimationEnable) {
            return false;
        }
        int action = motionEvent.getAction();
        if (action == 2 && this.mIsBeingDragged) {
            return true;
        }
        interceptTouchEventResponse(motionEvent, action);
        return this.mIsBeingDragged;
    }

    private void interceptTouchEventResponse(MotionEvent motionEvent, int action) {
        int i = action & ConstantValues.MAX_CHANNEL_VALUE;
        if (i != 0) {
            if (i != 1) {
                if (i == 2) {
                    int activePointerId = this.mActivePointerId;
                    if (activePointerId != -1) {
                        int pointerIndex = motionEvent.findPointerIndex(activePointerId);
                        if (pointerIndex == -1) {
                            Log.e(TAG, "Invalid pointerId=" + activePointerId + " in onInterceptTouchEvent");
                            return;
                        }
                        int valueY = (int) motionEvent.getY(pointerIndex);
                        if (!inActionBarTop(valueY)) {
                            this.mIsBeingDragged = false;
                            return;
                        } else if (Math.abs(valueY - this.mLastMotionY) > this.mTouchSlop) {
                            this.mIsBeingDragged = true;
                            this.mLastMotionY = valueY;
                            return;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                } else if (i != 3) {
                    this.mIsBeingDragged = false;
                    this.mActivePointerId = -1;
                    return;
                }
            }
            this.mIsBeingDragged = false;
            this.mActivePointerId = -1;
            return;
        }
        this.mLastMotionY = (int) motionEvent.getY();
        this.mActivePointerId = motionEvent.getPointerId(0);
        this.mIsBeingDragged = false;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent == null) {
            return false;
        }
        initCustomPanel();
        if (this.mScroller == null) {
            initScroller();
        }
        if (!this.mIsDraggable || !this.mIsAnimationEnable) {
            return false;
        }
        if (!this.mScroller.isFinished()) {
            return true;
        }
        if (inActionBarTop((int) motionEvent.getY()) || inCustPanel((int) motionEvent.getY())) {
            if (this.mGestureDetector == null) {
                this.mGestureDetector = new GestureDetector(this.mContext, new SimpleGestureListener());
            }
            if (this.mGestureDetector.onTouchEvent(motionEvent)) {
                return true;
            }
        }
        touchEventResponse(motionEvent);
        return true;
    }

    private void touchEventResponse(MotionEvent motionEvent) {
        int action = motionEvent.getAction() & ConstantValues.MAX_CHANNEL_VALUE;
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    int activePointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (activePointerIndex == -1) {
                        Log.e(TAG, "Invalid pointerId=" + this.mActivePointerId + " in onTouchEvent");
                        return;
                    }
                    touchEventResponseForActionMove(motionEvent, activePointerIndex);
                    return;
                } else if (action != 3) {
                    if (action == 5) {
                        int index = motionEvent.getActionIndex();
                        this.mLastMotionY = (int) motionEvent.getY(index);
                        this.mActivePointerId = motionEvent.getPointerId(index);
                        return;
                    } else if (action != 6) {
                        this.mIsBeingDragged = false;
                        return;
                    } else {
                        onSecondaryPointerUp(motionEvent);
                        int pointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                        if (pointerIndex >= 0 && pointerIndex < motionEvent.getPointerCount()) {
                            this.mLastMotionY = (int) motionEvent.getY(pointerIndex);
                            return;
                        }
                        return;
                    }
                }
            }
            if (this.mIsBeingDragged) {
                this.mActivePointerId = -1;
                startAnimation(this.mScrollY);
                this.mIsBeingDragged = false;
                return;
            }
            return;
        }
        this.mLastMotionY = (int) motionEvent.getY();
        this.mActivePointerId = motionEvent.getPointerId(0);
        if (!this.mScroller.isFinished()) {
            this.mScroller.abortAnimation();
        }
    }

    private void touchEventResponseForActionMove(MotionEvent motionEvent, int activePointerIndex) {
        int i;
        int valueY = (int) motionEvent.getY(activePointerIndex);
        int deltaY = this.mLastMotionY - valueY;
        if (!this.mIsBeingDragged && Math.abs(deltaY) > (i = this.mTouchSlop)) {
            this.mIsBeingDragged = true;
            deltaY = deltaY > 0 ? deltaY - i : deltaY + i;
        }
        if (this.mIsBeingDragged) {
            this.mLastMotionY = valueY;
            int clampY = this.mScrollY + deltaY;
            if (clampY >= 0) {
                deltaY -= clampY + 0;
            } else {
                int i2 = this.mCustViewHeight;
                if (clampY <= (-i2)) {
                    deltaY -= clampY - (-i2);
                }
            }
            moveBy(deltaY);
        }
    }

    private void onSecondaryPointerUp(MotionEvent motionEvent) {
        int pointerIndex = (motionEvent.getAction() & 65280) >> 8;
        if (motionEvent.getPointerId(pointerIndex) == this.mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            this.mLastMotionY = (int) motionEvent.getY(newPointerIndex);
            this.mActivePointerId = motionEvent.getPointerId(newPointerIndex);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        HwCustomPanel hwCustomPanel;
        MotionEvent event = MotionEvent.obtain(motionEvent);
        initCustomPanel();
        if (!inCustPanel((int) event.getY()) || (hwCustomPanel = this.mCustomPanel) == null || !hwCustomPanel.dispatchTouchEvent(event)) {
            event.recycle();
            return HwActionBarOverlayLayout.super.dispatchTouchEvent(event);
        }
        event.recycle();
        return true;
    }

    private class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener {
        private SimpleGestureListener() {
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            boolean z = false;
            if (Math.abs(velocityY) <= 2200.0f) {
                return false;
            }
            HwActionBarOverlayLayout hwActionBarOverlayLayout = HwActionBarOverlayLayout.this;
            int i = hwActionBarOverlayLayout.mScrollY;
            if (Math.signum(velocityY) != HwActionBarOverlayLayout.NEG_Z_VALUE) {
                z = true;
            }
            hwActionBarOverlayLayout.startFlingAnimation(i, z);
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startFlingAnimation(int startY, boolean isDown) {
        int finalPos;
        int i = 0;
        if (isCustomPanelEmpty()) {
            if (isDown) {
                i = -this.mCustViewHeight;
            }
            finalPos = i;
        } else {
            int finalPos2 = Math.abs(startY);
            int i2 = this.mFirstCustViewHeight;
            if (finalPos2 <= i2) {
                if (isDown) {
                    i = -i2;
                }
                finalPos = i;
            } else {
                int finalPos3 = Math.abs(startY);
                int i3 = this.mCustViewHeight;
                if (finalPos3 <= i3) {
                    finalPos = isDown ? -i3 : -this.mFirstCustViewHeight;
                } else {
                    finalPos = 0;
                }
            }
        }
        smoothScrollTo(startY, finalPos, SCROLL_ANIMATION_DURATION);
    }

    private boolean inCustPanel(int valueY) {
        View actionBarTop = getActionBarContainer();
        if (actionBarTop == null) {
            return false;
        }
        if (valueY > actionBarTop.getTop() - this.mScrollY || valueY < actionBarTop.getTop()) {
            return false;
        }
        return true;
    }

    public void setActionBarDraggable(boolean isDraggable) {
        this.mIsDraggable = isDraggable;
    }

    public void setLazyMode(boolean isLazyMode) {
        this.mIsLazyMode = isLazyMode;
    }

    private boolean isCustomPanelEmpty() {
        initCustomPanel();
        return this.mFirstCustViewHeight == 0;
    }

    private void initScroller() {
        if (this.mScroller == null) {
            this.mScroller = new Scroller(this.mContext, AnimationUtils.loadInterpolator(this.mContext, 34078722));
        }
    }

    private void initCustomPanel() {
        if (this.mCustomPanel == null) {
            ActionBarOverlayLayout.ActionBarVisibilityCallback callback = getActionBar();
            HwActionBarImpl actionBar = null;
            if (callback instanceof HwActionBarImpl) {
                actionBar = (HwActionBarImpl) callback;
            }
            if (actionBar != null) {
                this.mCustomPanel = actionBar.getCustomPanel();
            } else {
                Log.w(TAG, "CustomPanel is not initialized correctly, CustViewHeight = " + this.mCustViewHeight);
                return;
            }
        }
        this.mFirstCustViewHeight = 0;
        this.mSecondCustViewHeight = 0;
        this.mCustViewHeight = 0;
        HwCustomPanel hwCustomPanel = this.mCustomPanel;
        if (hwCustomPanel != null) {
            View firstChild = hwCustomPanel.getChildAt(0);
            if (firstChild != null) {
                this.mFirstCustViewHeight = firstChild.getHeight();
                View secondChild = this.mCustomPanel.getChildAt(1);
                if (secondChild != null) {
                    this.mSecondCustViewHeight = secondChild.getHeight();
                }
                this.mCustViewHeight = this.mFirstCustViewHeight + this.mSecondCustViewHeight;
            } else {
                this.mCustViewHeight = this.mCustomPanel.getHeight() - getActionBarHeight();
            }
            setCustomPanelBackground();
        }
    }

    private void setCustomPanelBackground() {
        if (HwWidgetUtils.isActionbarBackgroundThemed(getContext())) {
            int[] cpLocs = new int[2];
            int[] ctLocs = new int[2];
            this.mCustomPanel.getLocationOnScreen(cpLocs);
            View actionBarTop = getActionBarContainer();
            if (actionBarTop != null) {
                actionBarTop.getLocationOnScreen(ctLocs);
            }
            if (cpLocs[1] < ctLocs[1]) {
                this.mCustomPanel.setBackgroundDrawable(getContext().getResources().getDrawable(33751611));
            } else {
                this.mCustomPanel.setBackgroundDrawable(null);
            }
        }
    }

    private boolean inActionBarTop(int valueY) {
        View actionBarTop = getActionBarContainer();
        if (actionBarTop == null) {
            return false;
        }
        int scrollY = this.mScrollY;
        if (valueY < actionBarTop.getTop() - scrollY || valueY > actionBarTop.getBottom() - scrollY) {
            return false;
        }
        return true;
    }

    public void setCanDragFromContent(boolean canDragFromContent) {
        this.mCanDragFromContent = canDragFromContent;
    }

    private void smoothScrollTo(int startY, int positionY, int duration) {
        this.mScroller.startScroll(0, startY, 0, positionY - startY, duration);
        postInvalidateOnAnimation();
    }

    public void computeScroll() {
        if (this.mScroller == null) {
            initScroller();
        }
        if (this.mScroller.computeScrollOffset()) {
            custScrollTo(this.mScroller.getCurrY());
            postInvalidateOnAnimation();
        }
    }

    private void moveBy(int deltaY) {
        custScrollTo(this.mScrollY + deltaY);
        invalidate();
    }

    private void custScrollTo(int positionY) {
        View actionBarBottom = getActionBarBottom();
        if (actionBarBottom != null) {
            actionBarBottom.setTranslationY((float) positionY);
        }
        int size = this.mStillViews.size();
        for (int i = 0; i < size; i++) {
            View view = this.mStillViews.get(i);
            if (view != null) {
                view.setTranslationY((float) positionY);
            }
        }
        scrollTo(0, positionY);
        HwCustomPanel hwCustomPanel = this.mCustomPanel;
        if (hwCustomPanel != null) {
            hwCustomPanel.setClipY(positionY - getActionBarHeight());
        }
        setAlphaOfCustDragView(positionY);
    }

    private void setAlphaOfCustDragView(int positionY) {
        HwCustomPanel hwCustomPanel = this.mCustomPanel;
        View secondChild = null;
        View firstChild = hwCustomPanel != null ? hwCustomPanel.getChildAt(0) : null;
        HwCustomPanel hwCustomPanel2 = this.mCustomPanel;
        if (hwCustomPanel2 != null) {
            secondChild = hwCustomPanel2.getChildAt(1);
        }
        if (Math.abs(positionY) == 0) {
            this.mDragStage = 0;
            setViewAlpha(firstChild, 0.0f);
            setViewAlpha(secondChild, 0.0f);
            HwActionBarImpl.InnerOnStageChangedListener innerOnStageChangedListener = this.mStartCallback;
            if (innerOnStageChangedListener != null && this.mIsStartStageChanged) {
                this.mIsStartStageChanged = false;
                innerOnStageChangedListener.onExitNextStage();
            }
        } else if (Math.abs(positionY) <= this.mFirstCustViewHeight) {
            this.mDragStage = 1;
            setViewAlpha(secondChild, 0.0f);
            HwActionBarImpl.InnerOnStageChangedListener innerOnStageChangedListener2 = this.mCallback;
            if (innerOnStageChangedListener2 != null && this.mIsStageChanged) {
                innerOnStageChangedListener2.onExitNextStage();
                this.mIsStageChanged = false;
                this.mIsStage2Changed = true;
            }
            if (this.mStartCallback != null && Math.abs(positionY) >= 10 && !this.mIsStartStageChanged) {
                this.mIsStartStageChanged = true;
                this.mStartCallback.onEnterNextStage();
            }
            setViewAlpha(firstChild, getAlphaInterpolation((((float) (-positionY)) * 1.0f) / ((float) this.mFirstCustViewHeight)));
        } else if (Math.abs(positionY) <= this.mCustViewHeight) {
            this.mDragStage = 2;
            setViewAlpha(firstChild, 1.0f);
            HwActionBarImpl.InnerOnStageChangedListener innerOnStageChangedListener3 = this.mCallback;
            if (innerOnStageChangedListener3 != null && this.mIsStage2Changed) {
                innerOnStageChangedListener3.onEnterNextStage();
                this.mIsStageChanged = true;
                this.mIsStage2Changed = false;
            }
            setViewAlpha(secondChild, getAlphaInterpolation((((float) ((-positionY) - this.mFirstCustViewHeight)) * 1.0f) / ((float) this.mSecondCustViewHeight)));
        }
    }

    private void setViewAlpha(View view, float alpha) {
        if (view != null) {
            view.setAlpha(alpha);
        }
    }

    private float getAlphaInterpolation(float input) {
        float progress;
        if (input < ALPHA_ANIMATION_START) {
            progress = 0.0f;
        } else {
            progress = ((input - ALPHA_ANIMATION_START) * 400.0f) / 167.0f;
        }
        if (progress > 1.0f) {
            return 1.0f;
        }
        return progress;
    }

    private void startAnimation(int startY) {
        int finalPos = 0;
        int i = 0;
        if (isCustomPanelEmpty()) {
            if (this.mIsLazyMode) {
                finalPos = -this.mCustViewHeight;
                this.mIsLazyMode = false;
            } else if (this.mCanDragFromContent) {
                finalPos = 0;
                this.mCanDragFromContent = false;
            } else if (this.mCustViewHeight != 0) {
                int i2 = this.mCustViewHeight;
                if ((((float) Math.abs(this.mScrollY)) * 1.0f) / ((float) i2) > 0.5f) {
                    i = -i2;
                }
                finalPos = i;
            }
        } else if (this.mIsLazyMode) {
            finalPos = -this.mFirstCustViewHeight;
            this.mIsLazyMode = false;
        } else if (this.mCanDragFromContent) {
            finalPos = 0;
            this.mCanDragFromContent = false;
        } else if (Math.abs(startY) <= this.mFirstCustViewHeight) {
            int i3 = this.mFirstCustViewHeight;
            if ((((float) Math.abs(this.mScrollY)) * 1.0f) / ((float) i3) > 0.5f) {
                i = -i3;
            }
            finalPos = i;
        } else if (Math.abs(startY) <= this.mCustViewHeight) {
            int abs = Math.abs(this.mScrollY);
            int i4 = this.mFirstCustViewHeight;
            finalPos = (((float) (abs - i4)) * 1.0f) / ((float) this.mSecondCustViewHeight) > 0.5f ? -this.mCustViewHeight : -i4;
        }
        smoothScrollTo(startY, finalPos, SCROLL_ANIMATION_DURATION);
    }

    public void pullBackAnimation() {
        int i = this.mScrollY;
        int i2 = this.mCustViewHeight;
        if (i == (-i2)) {
            smoothScrollTo(-i2, 0, SCROLL_ANIMATION_DURATION);
        }
    }

    public void resetDragAnimation() {
        if (this.mIsDraggable) {
            initCustomPanel();
            if (this.mScroller == null) {
                initScroller();
            }
            if (!this.mScroller.isFinished()) {
                this.mScroller.forceFinished(true);
            }
            custScrollTo(0);
            invalidate();
        }
    }

    public void startStageAnimation(int stage, boolean isScrollDown) {
        if (!isCustomPanelEmpty()) {
            if (stage == 1) {
                startStageOneAnimation(isScrollDown);
            } else if (stage == 2) {
                startStageTwoAnimation(isScrollDown);
            }
        }
    }

    private void startStageOneAnimation(boolean isScrollDown) {
        if (!isScrollDown) {
            int i = this.mScrollY;
            int i2 = this.mFirstCustViewHeight;
            if (i == (-i2)) {
                smoothScrollTo(-i2, 0, SCROLL_ANIMATION_DURATION);
            }
        } else if (this.mScrollY == 0) {
            smoothScrollTo(0, -this.mFirstCustViewHeight, SCROLL_ANIMATION_DURATION);
        }
    }

    private void startStageTwoAnimation(boolean isScrollDown) {
        if (isScrollDown) {
            int i = this.mScrollY;
            int i2 = this.mFirstCustViewHeight;
            if (i == (-i2)) {
                smoothScrollTo(-i2, -this.mCustViewHeight, SCROLL_ANIMATION_DURATION);
                return;
            }
            return;
        }
        int i3 = this.mScrollY;
        int i4 = this.mCustViewHeight;
        if (i3 == (-i4)) {
            smoothScrollTo(-i4, -this.mFirstCustViewHeight, SCROLL_ANIMATION_DURATION);
        }
    }

    public int getDragAnimationStage() {
        return this.mDragStage;
    }

    public void setStillView(View view, boolean isStill) {
        if (isStill) {
            this.mStillViews.add(view);
        } else {
            this.mStillViews.remove(view);
        }
    }

    public void setAnimationEnable(boolean isEnable) {
        this.mIsAnimationEnable = isEnable;
    }

    public void setCallback(HwActionBarImpl.InnerOnStageChangedListener callback) {
        this.mCallback = callback;
    }

    public void setStartStageCallback(HwActionBarImpl.InnerOnStageChangedListener callback) {
        this.mStartCallback = callback;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        HwActionBarOverlayLayout.super.onConfigurationChanged(newConfig);
        boolean z = true;
        if (getContext().getResources().getConfiguration().orientation == 2) {
            z = false;
        }
        setAnimationEnable(z);
        setUiOptions(this.mUiOptions);
    }

    public void setUiOptions(int uiOptions) {
        ViewGroup actionBarBottom;
        this.mUiOptions = uiOptions;
        HwActionBarOverlayLayout.super.setUiOptions(uiOptions);
        DecorToolbar decorToolbar = getDecorToolbar();
        if ((getActionBarBottom() instanceof ViewGroup) && (actionBarBottom = (ViewGroup) getActionBarBottom()) != null && decorToolbar != null && decorToolbar.canSplit()) {
            boolean isSplitWhenNarrow = (uiOptions & 1) != 0;
            decorToolbar.setSplitView(actionBarBottom);
            decorToolbar.setSplitWhenNarrow(isSplitWhenNarrow);
            ActionBarContextView actionBarView = findViewById(16908723);
            if (actionBarView != null) {
                actionBarView.setSplitView(actionBarBottom);
                actionBarView.setSplitWhenNarrow(isSplitWhenNarrow);
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getBottomInset(int bottomInset) {
        return 0;
    }

    public void setHwDrawerFeature(boolean isOpen, int overlayActionBar) {
        this.mIsHwDrawerFeature = isOpen;
        if (isOpen) {
            this.mHwOverlayActionBar = overlayActionBar;
        } else {
            this.mHwOverlayActionBar = 0;
        }
        initHwDrawerFeature();
    }

    /* access modifiers changed from: protected */
    public void initHwDrawerFeature() {
        float valueZ = 0.0f;
        View view = null;
        int i = this.mHwOverlayActionBar;
        if (i == 1) {
            view = getContent();
            valueZ = 1.0f;
        } else if (i == -1) {
            view = getActionBarContainer();
            valueZ = NEG_Z_VALUE;
        }
        if (!this.mIsHwDrawerFeature) {
            View view2 = getContent();
            if (!(view2 == null || view2.getTranslationZ() == 0.0f)) {
                view2.setTranslationZ(0.0f);
            }
            View view3 = getActionBarContainer();
            if (view3 != null && view3.getTranslationZ() != 0.0f) {
                view3.setTranslationZ(0.0f);
            }
        } else if (view != null) {
            view.setTranslationZ(valueZ);
        }
    }

    public void setDrawerOpend(boolean isOpen) {
        this.mIsDrawerOpened = isOpen;
    }

    public ArrayList<View> buildTouchDispatchChildList() {
        ArrayList<View> list = HwActionBarOverlayLayout.super.buildTouchDispatchChildList();
        if (this.mIsHwDrawerFeature && list != null) {
            if (!this.mIsDrawerOpened) {
                swapTouchDispatchTarget(list, 16908290, 16908719);
            } else if (this.mHwOverlayActionBar == -1) {
                swapTouchDispatchTarget(list, 16909433, 16908290);
            }
        }
        return list;
    }

    private void swapTouchDispatchTarget(ArrayList<View> list, int id1, int id2) {
        if (list != null) {
            View view1 = null;
            int index1 = -1;
            View view2 = null;
            int index2 = -1;
            int size = list.size();
            for (int i = 0; i < size; i++) {
                View child = list.get(i);
                if (child.getId() == id1) {
                    view1 = child;
                    index1 = i;
                } else if (child.getId() == id2) {
                    view2 = child;
                    index2 = i;
                }
            }
            if (index1 > index2 && index2 > -1) {
                list.set(index1, view2);
                list.set(index2, view1);
            }
        }
    }
}
