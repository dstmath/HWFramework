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
import huawei.com.android.internal.app.HwActionBarImpl;
import java.util.ArrayList;

public class HwActionBarOverlayLayout extends ActionBarOverlayLayout {
    private static final int ALPHA_ANIMATION_DELAY = 133;
    private static final int ALPHA_ANIMATION_DURATION = 167;
    private static final float ALPHA_ANIMATION_START = 0.3325f;
    private static final int FLING_THRESHOULD = 2200;
    private static final int INVALID_POINTER = -1;
    private static final int SCROLL_ANIMATION_DURATION = 400;
    private static final int STAGE_ONE = 1;
    private static final int STAGE_RESET = 0;
    private static final int STAGE_TWO = 2;
    private static final String TAG = "HwActionBarOverlayLayout";
    private int mActivePointerId = -1;
    private HwActionBarImpl.InnerOnStageChangedListener mCallback;
    private boolean mCanDragFromContent = false;
    private int mCustViewHeight;
    private HwCustomPanel mCustomPanel;
    private int mDragStage = 0;
    private boolean mDrawerOpend;
    private int mFirstCustViewHeight = 0;
    private GestureDetector mGestureDetector;
    private boolean mHwDrawerFeature;
    private int mHwOverlayActionBar;
    private boolean mIsAnimationEnable = true;
    private boolean mIsBeingDragged;
    private boolean mIsDraggable = false;
    private boolean mIsStage2Changed = false;
    private boolean mIsStageChanged = true;
    private boolean mIsStartStageChanged = false;
    private int mLastMotionY;
    private boolean mLazyMode = false;
    private Scroller mScroller;
    private int mSecondCustViewHeight = 0;
    private HwActionBarImpl.InnerOnStageChangedListener mStartCallback;
    private final ArrayList<View> mStillViews = new ArrayList<>();
    private int mTouchSlop = 0;
    private int mUiOptions;

    private class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener {
        private SimpleGestureListener() {
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float velocity = velocityY;
            boolean z = false;
            if (Math.abs(velocity) <= 2200.0f) {
                return false;
            }
            HwActionBarOverlayLayout hwActionBarOverlayLayout = HwActionBarOverlayLayout.this;
            int access$100 = HwActionBarOverlayLayout.this.mScrollY;
            if (Math.signum(velocity) != -1.0f) {
                z = true;
            }
            hwActionBarOverlayLayout.startFlingAnimation(access$100, z);
            return true;
        }
    }

    public HwActionBarOverlayLayout(Context context) {
        super(context);
    }

    public HwActionBarOverlayLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        initCustomPanel();
        if (!this.mIsDraggable || !this.mIsAnimationEnable) {
            return false;
        }
        int action = ev.getAction();
        if (action == 2 && this.mIsBeingDragged) {
            return true;
        }
        switch (action & 255) {
            case 0:
                this.mLastMotionY = (int) ev.getY();
                this.mActivePointerId = ev.getPointerId(0);
                this.mIsBeingDragged = false;
                break;
            case 1:
            case 3:
                this.mIsBeingDragged = false;
                this.mActivePointerId = -1;
                break;
            case 2:
                int activePointerId = this.mActivePointerId;
                if (activePointerId != -1) {
                    int pointerIndex = ev.findPointerIndex(activePointerId);
                    if (pointerIndex != -1) {
                        int y = (int) ev.getY(pointerIndex);
                        if (inActionBarTop(y)) {
                            if (Math.abs(y - this.mLastMotionY) > this.mTouchSlop) {
                                this.mIsBeingDragged = true;
                                this.mLastMotionY = y;
                                break;
                            }
                        } else {
                            this.mIsBeingDragged = false;
                            break;
                        }
                    } else {
                        Log.e(TAG, "Invalid pointerId=" + activePointerId + " in onInterceptTouchEvent");
                        break;
                    }
                }
                break;
            default:
                this.mIsBeingDragged = false;
                this.mActivePointerId = -1;
                break;
        }
        return this.mIsBeingDragged;
    }

    public boolean onTouchEvent(MotionEvent ev) {
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
        if (inActionBarTop((int) ev.getY()) || inCustPanel((int) ev.getY())) {
            if (this.mGestureDetector == null) {
                this.mGestureDetector = new GestureDetector(this.mContext, new SimpleGestureListener());
            }
            if (this.mGestureDetector.onTouchEvent(ev)) {
                return true;
            }
        }
        switch (ev.getAction() & 255) {
            case 0:
                this.mLastMotionY = (int) ev.getY();
                this.mActivePointerId = ev.getPointerId(0);
                if (!this.mScroller.isFinished()) {
                    this.mScroller.abortAnimation();
                    break;
                }
                break;
            case 1:
            case 3:
                if (this.mIsBeingDragged != 0) {
                    this.mActivePointerId = -1;
                    startAnimation(this.mScrollY);
                    this.mIsBeingDragged = false;
                    break;
                }
                break;
            case 2:
                int activePointerIndex = ev.findPointerIndex(this.mActivePointerId);
                if (activePointerIndex != -1) {
                    int y = (int) ev.getY(activePointerIndex);
                    int deltaY = this.mLastMotionY - y;
                    if (!this.mIsBeingDragged && Math.abs(deltaY) > this.mTouchSlop) {
                        this.mIsBeingDragged = true;
                        deltaY = deltaY > 0 ? deltaY - this.mTouchSlop : deltaY + this.mTouchSlop;
                    }
                    if (this.mIsBeingDragged) {
                        this.mLastMotionY = y;
                        int clampY = this.mScrollY + deltaY;
                        if (clampY >= 0) {
                            deltaY -= clampY + 0;
                        } else if (clampY <= (-this.mCustViewHeight)) {
                            deltaY -= clampY - (-this.mCustViewHeight);
                        }
                        moveBy(deltaY);
                        break;
                    }
                } else {
                    Log.e(TAG, "Invalid pointerId=" + this.mActivePointerId + " in onTouchEvent");
                    break;
                }
                break;
            case 5:
                int pointerIndex = ev.getActionIndex();
                this.mLastMotionY = (int) ev.getY(pointerIndex);
                this.mActivePointerId = ev.getPointerId(pointerIndex);
                break;
            case 6:
                onSecondaryPointerUp(ev);
                int pointerIndex2 = ev.findPointerIndex(this.mActivePointerId);
                if (pointerIndex2 >= 0 && pointerIndex2 < ev.getPointerCount()) {
                    this.mLastMotionY = (int) ev.getY(pointerIndex2);
                    break;
                }
            default:
                this.mIsBeingDragged = false;
                break;
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = (ev.getAction() & 65280) >> 8;
        if (ev.getPointerId(pointerIndex) == this.mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            this.mLastMotionY = (int) ev.getY(newPointerIndex);
            this.mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        MotionEvent ev = MotionEvent.obtain(event);
        initCustomPanel();
        if (!inCustPanel((int) event.getY()) || this.mCustomPanel == null || !this.mCustomPanel.dispatchTouchEvent(ev)) {
            ev.recycle();
            return HwActionBarOverlayLayout.super.dispatchTouchEvent(event);
        }
        ev.recycle();
        return true;
    }

    /* access modifiers changed from: private */
    public void startFlingAnimation(int startY, boolean isDown) {
        int finalPos = 0;
        if (isCustomPanelEmpty()) {
            if (isDown) {
                finalPos = -this.mCustViewHeight;
            } else {
                finalPos = 0;
            }
        } else if (Math.abs(startY) <= this.mFirstCustViewHeight) {
            if (isDown) {
                finalPos = -this.mFirstCustViewHeight;
            } else {
                finalPos = 0;
            }
        } else if (Math.abs(startY) <= this.mCustViewHeight) {
            if (isDown) {
                finalPos = -this.mCustViewHeight;
            } else {
                finalPos = -this.mFirstCustViewHeight;
            }
        }
        smoothScrollTo(startY, finalPos, SCROLL_ANIMATION_DURATION);
    }

    private boolean inCustPanel(int y) {
        View actionBarTop = getActionBarContainer();
        boolean rtn = false;
        if (actionBarTop == null) {
            return false;
        }
        if (y <= actionBarTop.getTop() - this.mScrollY && y >= actionBarTop.getTop()) {
            rtn = true;
        }
        return rtn;
    }

    public void setActionBarDraggable(boolean isDraggable) {
        this.mIsDraggable = isDraggable;
    }

    public void setLazyMode(boolean isLazyMode) {
        this.mLazyMode = isLazyMode;
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
            HwActionBarImpl actionBar = getActionBar();
            HwActionBarImpl actionBar2 = null;
            if (actionBar instanceof HwActionBarImpl) {
                actionBar2 = actionBar;
            }
            if (actionBar2 != null) {
                this.mCustomPanel = actionBar2.getCustomPanel();
            } else {
                Log.w(TAG, "CustomPanel is not initialized correctly, CustViewHeight = " + this.mCustViewHeight);
                return;
            }
        }
        this.mFirstCustViewHeight = 0;
        this.mSecondCustViewHeight = 0;
        this.mCustViewHeight = 0;
        if (this.mCustomPanel != null) {
            View firstChild = this.mCustomPanel.getChildAt(0);
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
            if (HwWidgetUtils.isActionbarBackgroundThemed(getContext())) {
                int[] cpLoc = new int[2];
                int[] ctLoc = new int[2];
                this.mCustomPanel.getLocationOnScreen(cpLoc);
                View actionBarTop = getActionBarContainer();
                if (actionBarTop != null) {
                    actionBarTop.getLocationOnScreen(ctLoc);
                }
                if (cpLoc[1] < ctLoc[1]) {
                    this.mCustomPanel.setBackgroundDrawable(getContext().getResources().getDrawable(33751611));
                } else {
                    this.mCustomPanel.setBackgroundDrawable(null);
                }
            }
        }
    }

    private boolean inActionBarTop(int y) {
        View actionBarTop = getActionBarContainer();
        boolean rtn = false;
        if (actionBarTop == null) {
            return false;
        }
        int scrollY = this.mScrollY;
        if (y >= actionBarTop.getTop() - scrollY && y <= actionBarTop.getBottom() - scrollY) {
            rtn = true;
        }
        return rtn;
    }

    public void setCanDragFromContent(boolean canDragFromContent) {
        this.mCanDragFromContent = canDragFromContent;
    }

    private void smoothScrollTo(int startY, int y, int duration) {
        this.mScroller.startScroll(0, startY, 0, y - startY, duration);
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

    private void custScrollTo(int y) {
        View actionBarBottom = getActionBarBottom();
        if (actionBarBottom != null) {
            actionBarBottom.setTranslationY((float) y);
        }
        int size = this.mStillViews.size();
        for (int i = 0; i < size; i++) {
            View v = this.mStillViews.get(i);
            if (v != null) {
                v.setTranslationY((float) y);
            }
        }
        scrollTo(0, y);
        if (this.mCustomPanel != null) {
            this.mCustomPanel.setClipY(y - getActionBarHeight());
        }
        View secondChild = null;
        View firstChild = this.mCustomPanel != null ? this.mCustomPanel.getChildAt(0) : null;
        if (this.mCustomPanel != null) {
            secondChild = this.mCustomPanel.getChildAt(1);
        }
        if (Math.abs(y) == 0) {
            this.mDragStage = 0;
            if (firstChild != null) {
                firstChild.setAlpha(0.0f);
            }
            if (secondChild != null) {
                secondChild.setAlpha(0.0f);
            }
            if (this.mStartCallback != null && this.mIsStartStageChanged) {
                this.mIsStartStageChanged = false;
                this.mStartCallback.onExitNextStage();
            }
        } else if (Math.abs(y) <= this.mFirstCustViewHeight) {
            this.mDragStage = 1;
            if (secondChild != null) {
                secondChild.setAlpha(0.0f);
            }
            if (this.mCallback != null && this.mIsStageChanged) {
                this.mCallback.onExitNextStage();
                this.mIsStageChanged = false;
                this.mIsStage2Changed = true;
            }
            if (this.mStartCallback != null && Math.abs(y) >= 10 && !this.mIsStartStageChanged) {
                this.mIsStartStageChanged = true;
                this.mStartCallback.onEnterNextStage();
            }
            float firstStageProgress = (((float) (-y)) * 1.0f) / ((float) this.mFirstCustViewHeight);
            if (firstChild != null) {
                firstChild.setAlpha(getAlphaInterpolation(firstStageProgress));
            }
        } else if (Math.abs(y) <= this.mCustViewHeight) {
            this.mDragStage = 2;
            if (firstChild != null) {
                firstChild.setAlpha(1.0f);
            }
            if (this.mCallback != null && this.mIsStage2Changed) {
                this.mCallback.onEnterNextStage();
                this.mIsStageChanged = true;
                this.mIsStage2Changed = false;
            }
            float secondStageProgress = (((float) ((-y) - this.mFirstCustViewHeight)) * 1.0f) / ((float) this.mSecondCustViewHeight);
            if (secondChild != null) {
                secondChild.setAlpha(getAlphaInterpolation(secondStageProgress));
            }
        }
    }

    private float getAlphaInterpolation(float input) {
        float progress;
        float progress2 = input;
        if (progress2 < ALPHA_ANIMATION_START) {
            progress = 0.0f;
        } else {
            progress = ((progress2 - ALPHA_ANIMATION_START) * 400.0f) / 167.0f;
        }
        if (progress > 1.0f) {
            return 1.0f;
        }
        return progress;
    }

    private void startAnimation(int startY) {
        int finalPos = 0;
        if (isCustomPanelEmpty()) {
            if (this.mLazyMode) {
                finalPos = -this.mCustViewHeight;
                this.mLazyMode = false;
            } else if (this.mCanDragFromContent) {
                finalPos = 0;
                this.mCanDragFromContent = false;
            } else if (this.mCustViewHeight != 0) {
                finalPos = (1.0f * ((float) Math.abs(this.mScrollY))) / ((float) this.mCustViewHeight) > 0.5f ? -this.mCustViewHeight : 0;
            }
        } else if (this.mLazyMode) {
            finalPos = -this.mFirstCustViewHeight;
            this.mLazyMode = false;
        } else if (this.mCanDragFromContent) {
            finalPos = 0;
            this.mCanDragFromContent = false;
        } else if (Math.abs(startY) <= this.mFirstCustViewHeight) {
            finalPos = (1.0f * ((float) Math.abs(this.mScrollY))) / ((float) this.mFirstCustViewHeight) > 0.5f ? -this.mFirstCustViewHeight : 0;
        } else if (Math.abs(startY) <= this.mCustViewHeight) {
            finalPos = (1.0f * ((float) (Math.abs(this.mScrollY) - this.mFirstCustViewHeight))) / ((float) this.mSecondCustViewHeight) > 0.5f ? -this.mCustViewHeight : -this.mFirstCustViewHeight;
        }
        smoothScrollTo(startY, finalPos, SCROLL_ANIMATION_DURATION);
    }

    public void pullBackAnimation() {
        if (this.mScrollY == (-this.mCustViewHeight)) {
            smoothScrollTo(-this.mCustViewHeight, 0, SCROLL_ANIMATION_DURATION);
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
            switch (stage) {
                case 1:
                    if (!isScrollDown) {
                        if (this.mScrollY == (-this.mFirstCustViewHeight)) {
                            smoothScrollTo(-this.mFirstCustViewHeight, 0, SCROLL_ANIMATION_DURATION);
                            break;
                        }
                    } else if (this.mScrollY == 0) {
                        smoothScrollTo(0, -this.mFirstCustViewHeight, SCROLL_ANIMATION_DURATION);
                        break;
                    }
                    break;
                case 2:
                    if (!isScrollDown) {
                        if (this.mScrollY == (-this.mCustViewHeight)) {
                            smoothScrollTo(-this.mCustViewHeight, -this.mFirstCustViewHeight, SCROLL_ANIMATION_DURATION);
                            break;
                        }
                    } else if (this.mScrollY == (-this.mFirstCustViewHeight)) {
                        smoothScrollTo(-this.mFirstCustViewHeight, -this.mCustViewHeight, SCROLL_ANIMATION_DURATION);
                        break;
                    }
                    break;
            }
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
        boolean z = false;
        if (!(getContext().getResources().getConfiguration().orientation == 2)) {
            z = true;
        }
        setAnimationEnable(z);
        setUiOptions(this.mUiOptions);
    }

    public void setUiOptions(int uiOptions) {
        this.mUiOptions = uiOptions;
        HwActionBarOverlayLayout.super.setUiOptions(uiOptions);
        DecorToolbar decorToolbar = getDecorToolbar();
        ViewGroup actionBarBottom = (ViewGroup) getActionBarBottom();
        if (actionBarBottom != null && decorToolbar != null && decorToolbar.canSplit()) {
            boolean splitWhenNarrow = (uiOptions & 1) != 0;
            decorToolbar.setSplitView(actionBarBottom);
            decorToolbar.setSplitWhenNarrow(splitWhenNarrow);
            ActionBarContextView cab = findViewById(16908697);
            if (cab != null) {
                cab.setSplitView(actionBarBottom);
                cab.setSplitWhenNarrow(splitWhenNarrow);
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getBottomInset(int bottomInset) {
        return 0;
    }

    public void setHwDrawerFeature(boolean open, int overlayActionBar) {
        this.mHwDrawerFeature = open;
        if (open) {
            this.mHwOverlayActionBar = overlayActionBar;
        } else {
            this.mHwOverlayActionBar = 0;
        }
        initHwDrawerFeature();
    }

    /* access modifiers changed from: protected */
    public void initHwDrawerFeature() {
        float z = 0.0f;
        View v = null;
        if (this.mHwOverlayActionBar == 1) {
            v = getContent();
            z = 1.0f;
        } else if (this.mHwOverlayActionBar == -1) {
            v = getActionBarContainer();
            z = -1.0f;
        }
        if (!this.mHwDrawerFeature) {
            View v2 = getContent();
            if (!(v2 == null || v2.getTranslationZ() == 0.0f)) {
                v2.setTranslationZ(0.0f);
            }
            View v3 = getActionBarContainer();
            if (v3 != null && v3.getTranslationZ() != 0.0f) {
                v3.setTranslationZ(0.0f);
            }
        } else if (v != null) {
            v.setTranslationZ(z);
        }
    }

    public void setDrawerOpend(boolean open) {
        this.mDrawerOpend = open;
    }

    public ArrayList<View> buildTouchDispatchChildList() {
        ArrayList<View> list = HwActionBarOverlayLayout.super.buildTouchDispatchChildList();
        if (this.mHwDrawerFeature && list != null) {
            if (!this.mDrawerOpend) {
                swapTouchDispatchTarget(list, 16908290, 16908693);
            } else if (this.mHwOverlayActionBar == -1) {
                swapTouchDispatchTarget(list, 16909362, 16908290);
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
