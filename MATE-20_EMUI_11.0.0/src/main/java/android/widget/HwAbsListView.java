package android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemProperties;
import android.telephony.SmsManager;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.PathInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.RemoteViewsAdapter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

public abstract class HwAbsListView extends AbsListView implements TextWatcher, ViewTreeObserver.OnGlobalLayoutListener, Filter.FilterListener, ViewTreeObserver.OnTouchModeChangeListener, RemoteViewsAdapter.RemoteAdapterConnectionCallback {
    protected static final String ANIM_TAG = "listDeleteAnimation";
    private static final int AUTO_SCROLL_EDGE_HEIGHT_DP = 90;
    private static final boolean DEBUG = false;
    private static final int DEFAULT_COLLECT_LENGTH = 10;
    private static final int DURATION_DELTA = 152;
    private static final int EDGE_INSIDE_LISTVIEW_DP = 90;
    private static final int EDGE_OUTSIDE_LISTVIEW_DP = 0;
    private static final int INVALID_INDEX = -1;
    public static final int LISTVIEW_STUB_MASK_HIGH_SPEED_STABLE_ANIMATOR = 4;
    public static final int LISTVIEW_STUB_MASK_SCROLL_MULTI_SELECT = 1;
    public static final int LISTVIEW_STUB_MASK_SPRING_ANIMATOR = 2;
    private static final int LOCATION_COUNT = 2;
    private static final float MAX_OVER_SCROLLY_DISTANCE_FACTOR = 0.45f;
    private static final int MIN_DURATION = 40;
    private static final int NO_POSITION = -1;
    private static final int PRESSED_STATE_HWDURATION = 16;
    private static final int STEP_DP = 16;
    private static final String SWIPELAYOUT = "huawei.android.widget.SwipeLayout";
    private static final String TAG = "AbsListView";
    public static final int TOUCH_MODE_SCROLL_MULTI_SELECT = 7;
    protected int mAnimOffset;
    protected int mAnimatingFirstPos;
    protected int mAnimindex;
    private HashSet<Integer> mCheckedIdOnMove;
    private int[] mChildPositionOnLevel;
    private CheckBox mCurItemView;
    private HwAutoScroller mHwAutoScroller;
    private HwSpringBackHelper mHwSpringBackHelper;
    private boolean mIsExitScrollEnterMultiSelectFlag;
    private boolean mIsFirstChecked;
    private boolean mIsFirstHasChangedOnMove;
    private boolean mIsHwTheme;
    protected boolean mIsSupportAnim;
    private int mItemHeight;
    private int mLastMoveEventX;
    private int mLastMoveEventY;
    private int mLevel;
    protected ValueAnimator mListDeleteAnimator;
    private int mListRightBoundary;
    private int mMarkWidthOfCheckedTextView;
    private int mMask;
    private int mPreMotionPosition;
    private float mRawX;
    private float mRawY;
    protected ArrayList<Object> mVisibleItems;

    public HwAbsListView(Context context) {
        super(context);
        this.mIsSupportAnim = false;
        this.mAnimOffset = 0;
        this.mVisibleItems = new ArrayList<>(10);
        this.mChildPositionOnLevel = new int[20];
        this.mLevel = 0;
        this.mCheckedIdOnMove = new HashSet<>(10);
        this.mIsExitScrollEnterMultiSelectFlag = false;
        this.mPreMotionPosition = -1;
        this.mIsHwTheme = checkIsHwTheme(context, null);
        initMask(this.mIsHwTheme);
        if (this.mIsHwTheme) {
            setOverScrollMode(0);
        }
        this.mHwSpringBackHelper = HwWidgetFactory.getHwSpringBackHelper();
    }

    public HwAbsListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842858);
    }

    public HwAbsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwAbsListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mIsSupportAnim = false;
        this.mAnimOffset = 0;
        this.mVisibleItems = new ArrayList<>(10);
        this.mChildPositionOnLevel = new int[20];
        this.mLevel = 0;
        this.mCheckedIdOnMove = new HashSet<>(10);
        this.mIsExitScrollEnterMultiSelectFlag = false;
        this.mPreMotionPosition = -1;
        this.mIsHwTheme = checkIsHwTheme(context, attrs);
        initMask(this.mIsHwTheme);
        if (this.mIsHwTheme) {
            setOverScrollMode(0);
        }
        initSpringBackEffect();
        this.mHwSpringBackHelper = HwWidgetFactory.getHwSpringBackHelper();
    }

    private boolean checkIsHwTheme(Context context, AttributeSet attrs) {
        return HwWidgetFactory.checkIsHwTheme(context, attrs);
    }

    private void initMask(boolean isHwTheme) {
        if (isHwTheme) {
            this.mMask = 7;
        } else {
            this.mMask = 4;
        }
    }

    private void initSpringBackEffect() {
        if (hasSpringAnimatorMask()) {
            setEdgeGlowTopBottom(null, null);
            this.mOverflingDistance = 0;
        }
    }

    @Override // android.view.View
    public void setOverScrollMode(int mode) {
        super.setOverScrollMode(mode);
        if (hasSpringAnimatorMask()) {
            setEdgeGlowTopBottom(null, null);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView
    public boolean getCheckedStateForMultiSelect(boolean isCheckedCurState) {
        if (hasScrollMultiSelectMask()) {
            return getItemCheckedState(isCheckedCurState);
        }
        return isCheckedCurState;
    }

    private boolean getItemCheckedState(boolean isCurChecked) {
        if (getTouchMode() == 7) {
            return this.mIsFirstChecked;
        }
        return isCurChecked;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
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
        invalidate();
        return super.overScrollBy(deltaX, newDeltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, (int) (((float) getHeight()) * MAX_OVER_SCROLLY_DISTANCE_FACTOR), isTouchEvent);
    }

    private int getElasticInterpolation(int delta, int currentPos) {
        HwSpringBackHelper hwSpringBackHelper = this.mHwSpringBackHelper;
        if (hwSpringBackHelper != null) {
            return hwSpringBackHelper.getDynamicCurvedRateDelta(getHeight(), delta, currentPos);
        }
        float absCurrentPos = (float) Math.abs(currentPos);
        int newDeltaY = (int) ((Math.sqrt(((double) (250.0f * ((float) Math.abs(delta)))) + Math.pow((double) absCurrentPos, 2.0d)) - ((double) absCurrentPos)) * ((double) Math.signum((float) delta)));
        return Math.abs(newDeltaY) > Math.abs(delta) ? delta : newDeltaY;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView
    public void dismissCurrentPressed() {
        View child = getChildAt(getMotionPosition() - getFirstVisiblePosition());
        if (child != null) {
            child.setPressed(false);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView
    public void enterMultiSelectModeIfNeeded(int motionPosition, int eventX) {
        View view;
        if (hasScrollMultiSelectMask() && getTouchMode() != 7 && (view = getChildAt(motionPosition - getFirstVisiblePosition())) != null && inCheckableViewOnDown(view, eventX)) {
            setTouchMode(7);
            CheckBox checkBox = this.mCurItemView;
            if (checkBox != null) {
                this.mIsFirstChecked = !checkBox.isChecked();
            } else if (getChoiceMode() != 0) {
                this.mIsFirstChecked = !getCheckStates().get(motionPosition, false);
            }
            this.mIsFirstHasChangedOnMove = false;
            this.mCheckedIdOnMove.clear();
        }
    }

    private boolean inCheckableViewOnDown(View view, int eventX) {
        if (view instanceof CheckedTextView) {
            if (view.getVisibility() != 0) {
                return false;
            }
            CheckedTextView checkedTextView = (CheckedTextView) view;
            if (checkedTextView.getCheckMarkDrawable() != null) {
                this.mMarkWidthOfCheckedTextView = checkedTextView.getCheckMarkDrawable().getIntrinsicWidth();
                if (checkedTextView.isCheckMarkAtStartEx()) {
                    if (eventX >= view.getLeft() + this.mMarkWidthOfCheckedTextView || eventX <= view.getLeft()) {
                        return false;
                    }
                    return true;
                } else if (eventX <= view.getRight() - this.mMarkWidthOfCheckedTextView || eventX >= view.getRight()) {
                    return false;
                } else {
                    return true;
                }
            }
        } else if (view instanceof ViewGroup) {
            return searchCheckableView((ViewGroup) view, eventX);
        }
        return false;
    }

    private boolean searchCheckableView(ViewGroup root, int eventX) {
        Stack<LeveledView> stack = new Stack<>();
        boolean z = false;
        stack.push(new LeveledView(root, 0, WifiEnterpriseConfig.ENGINE_DISABLE));
        boolean isFound = false;
        while (true) {
            if (stack.empty()) {
                break;
            }
            LeveledView curView = stack.pop();
            if (curView.view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) curView.view;
                boolean z2 = true;
                int level = curView.level + 1;
                String path = curView.path;
                if (level > this.mChildPositionOnLevel.length) {
                    break;
                }
                int i = 0;
                while (i < viewGroup.getChildCount()) {
                    View childView = viewGroup.getChildAt(i);
                    String childPath = path + SmsManager.REGEX_PREFIX_DELIMITER + i;
                    boolean isCheckBoxAndVisible = (!(childView instanceof CheckBox) || childView.getVisibility() != 0) ? z : z2;
                    boolean isViewGroup = childView instanceof ViewGroup;
                    if (isCheckBoxAndVisible) {
                        stack.push(new LeveledView(childView, level, path + SmsManager.REGEX_PREFIX_DELIMITER + i));
                        isFound = inCheckBox(childView, eventX);
                    } else {
                        if (isViewGroup) {
                            stack.push(new LeveledView(childView, level, childPath));
                        }
                        i++;
                        viewGroup = viewGroup;
                        z = false;
                        z2 = true;
                    }
                }
                z = false;
            }
        }
        return checkStack(stack, isFound);
    }

    private boolean checkStack(Stack<LeveledView> stack, boolean isFound) {
        if (!isFound || stack.isEmpty() || !(stack.peek().view instanceof CheckBox)) {
            return isFound && this.mLevel > 0;
        }
        LeveledView check = stack.pop();
        if (check.path != null && !check.path.isEmpty()) {
            String[] leveledPaths = check.path.split(SmsManager.REGEX_PREFIX_DELIMITER);
            if (!(leveledPaths != null && leveledPaths.length <= this.mChildPositionOnLevel.length)) {
                return isFound && this.mLevel > 0;
            }
            this.mLevel = leveledPaths.length;
            if (check.view instanceof CheckBox) {
                this.mCurItemView = (CheckBox) check.view;
            }
            for (int i = 0; i < leveledPaths.length; i++) {
                try {
                    this.mChildPositionOnLevel[i] = Integer.parseInt(leveledPaths[i]);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "parse string to int error, leveledPath[i] = " + leveledPaths[i]);
                    this.mLevel = 0;
                    this.mCurItemView = null;
                    return false;
                } catch (Exception e2) {
                    this.mLevel = 0;
                    this.mCurItemView = null;
                    return false;
                }
            }
        }
        return isFound && this.mLevel > 0;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        super.onLayout(isChanged, left, top, right, bottom);
        if (this.mListRightBoundary != getRight() && this.mMultiSelectAutoScrollFlag) {
            this.mListRightBoundary = getRight();
            pauseOrResetHwAutoScroll(false);
        }
    }

    @Override // android.widget.AbsListView, android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        HwAutoScroller hwAutoScroller;
        if (event == null) {
            Log.w(TAG, "onTouchEvent() event is null!");
            return false;
        }
        if (!this.mIsAutoScroll && (hwAutoScroller = this.mHwAutoScroller) != null) {
            hwAutoScroller.stop();
            this.mPreMotionPosition = -1;
        }
        if (this.mMultiSelectAutoScrollFlag && event.getActionMasked() == 1) {
            pauseOrResetHwAutoScroll(false);
        }
        return super.onTouchEvent(event);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView
    public void onMultiSelectMove(MotionEvent event, int pointerIndex) {
        if (event != null) {
            int eventX = (int) event.getX(pointerIndex);
            int eventY = (int) event.getY(pointerIndex);
            int motionPosition = findClosestMotionRow(eventY);
            this.mLastMoveEventX = eventX;
            this.mLastMoveEventY = eventY;
            int edgeInsideView = (int) TypedValue.applyDimension(1, 90.0f, this.mContext.getResources().getDisplayMetrics());
            int stepPx = (int) TypedValue.applyDimension(1, 16.0f, this.mContext.getResources().getDisplayMetrics());
            if (this.mIsExitScrollEnterMultiSelectFlag) {
                this.mIsExitScrollEnterMultiSelectFlag = false;
                setTouchMode(7);
            }
            if ((getTouchMode() == 7 || (getTouchMode() == 4 && hasScrollMultiSelectMask())) && this.mPreMotionPosition != motionPosition) {
                clickItemIfNeeded(motionPosition, eventX);
                this.mPreMotionPosition = motionPosition;
            }
            if (this.mMultiSelectAutoScrollFlag) {
                if (getTouchMode() == 7 || getTouchMode() == 4) {
                    if (eventY > getHeight() - edgeInsideView && !isScrollToEnd(false)) {
                        this.mIsAutoScroll = true;
                        hwSmoothScrollBy(stepPx, calcScrollDuration(false, eventY));
                    } else if (eventY < edgeInsideView && !isScrollToEnd(true)) {
                        this.mIsAutoScroll = true;
                        hwSmoothScrollBy(-stepPx, calcScrollDuration(true, eventY));
                    }
                }
                if (eventY >= edgeInsideView && eventY <= getHeight() - edgeInsideView && this.mIsAutoScroll) {
                    pauseOrResetHwAutoScroll(true);
                }
            }
        }
    }

    private boolean isScrollToEnd(boolean isScrollUp) {
        int firstVisiblePosition = getFirstVisiblePosition();
        int lastVisiblePosition = getLastVisiblePosition();
        if (isScrollUp) {
            View firstItem = getChildAt(0);
            if (firstItem == null) {
                pauseOrResetHwAutoScroll(true);
                return true;
            } else if (firstVisiblePosition > 0 || firstItem.getTop() < getPaddingTop()) {
                return false;
            } else {
                pauseOrResetHwAutoScroll(true);
                return true;
            }
        } else {
            View lastItem = getChildAt(getChildCount() - 1);
            if (lastItem == null) {
                pauseOrResetHwAutoScroll(true);
                return true;
            } else if (lastVisiblePosition < getCount() - 1 || lastItem.getBottom() > getHeight() - getPaddingBottom()) {
                return false;
            } else {
                pauseOrResetHwAutoScroll(true);
                return true;
            }
        }
    }

    private int calcScrollDuration(boolean isScrollUp, int eventY) {
        int edgeHeight = (int) TypedValue.applyDimension(1, 90.0f, this.mContext.getResources().getDisplayMetrics());
        double edgeOutsideView = (double) TypedValue.applyDimension(1, 0.0f, this.mContext.getResources().getDisplayMetrics());
        double deltaUpY = 0.0d;
        if (isScrollUp) {
            if (((double) eventY) + edgeOutsideView > 0.0d) {
                deltaUpY = (((double) eventY) + edgeOutsideView) / ((double) edgeHeight);
            }
            return (int) ((152.0d * deltaUpY) + 40.0d);
        }
        if (((double) (getHeight() - eventY)) + edgeOutsideView > 0.0d) {
            deltaUpY = (((double) (getHeight() - eventY)) + edgeOutsideView) / ((double) edgeHeight);
        }
        return (int) ((152.0d * deltaUpY) + 40.0d);
    }

    private void hwSmoothScrollBy(int distance, int duration) {
        if (this.mHwAutoScroller == null) {
            this.mHwAutoScroller = new HwAutoScroller();
        }
        this.mHwAutoScroller.start(distance, duration);
    }

    private void pauseOrResetHwAutoScroll(boolean isPause) {
        HwAutoScroller hwAutoScroller = this.mHwAutoScroller;
        if (hwAutoScroller != null) {
            hwAutoScroller.stop();
            this.mPreMotionPosition = -1;
        }
        this.mIsAutoScroll = false;
        this.mIsExitScrollEnterMultiSelectFlag = isPause;
        if (isPause) {
            setTouchMode(7);
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
            this.mPauseCount = 1000;
        }

        /* access modifiers changed from: package-private */
        public void start(int distance, int duration) {
            if (duration > 100) {
                this.mDistance = distance / 2;
                this.mDuration = duration / 2;
            } else {
                this.mDistance = distance;
                this.mDuration = duration;
            }
            if (!this.mIsScrolling) {
                stop();
                HwAbsListView.this.postOnAnimation(this);
                this.mIsScrolling = true;
                this.mCount = 1;
                this.mPauseCount = 1000;
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            int i = this.mPauseCount;
            if (i <= 1) {
                this.mIsPause = true;
                this.mPauseCount = 1000;
            } else if (!this.mIsPause) {
                this.mPauseCount = i - 1;
            } else {
                this.mPauseCount = 1000;
            }
            if (this.mCount <= 1) {
                if (this.mIsPause) {
                    this.mIsPause = false;
                    int nextDistance = 0;
                    int i2 = this.mPreviousDuration;
                    if (i2 > 0) {
                        nextDistance = (int) Math.round(((double) (this.mPreviousDistance * 34)) / ((double) i2));
                    }
                    HwAbsListView.this.smoothScrollBy(nextDistance, 34);
                    this.mCount += 2;
                } else {
                    HwAbsListView.this.smoothScrollBy(this.mDistance, this.mDuration);
                    this.mPreviousDistance = this.mDistance;
                    int i3 = this.mDuration;
                    this.mPreviousDuration = i3;
                    this.mCount = i3 / 17;
                }
                HwAbsListView.this.postOnAnimation(this);
                return;
            }
            HwAbsListView.this.postOnAnimation(this);
            this.mCount--;
            HwAbsListView hwAbsListView = HwAbsListView.this;
            int motionPosition = hwAbsListView.findClosestMotionRow(hwAbsListView.mLastMoveEventY);
            if (motionPosition != HwAbsListView.this.mPreMotionPosition) {
                HwAbsListView hwAbsListView2 = HwAbsListView.this;
                hwAbsListView2.clickItemIfNeeded(motionPosition, hwAbsListView2.mLastMoveEventX);
                HwAbsListView.this.mPreMotionPosition = motionPosition;
            }
        }

        /* access modifiers changed from: package-private */
        public void stop() {
            this.mIsScrolling = false;
            HwAbsListView.this.removeCallbacks(this);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clickItemIfNeeded(int motionPosition, int eventX) {
        View view;
        ListAdapter adapter = (ListAdapter) getAdapter();
        if (adapter != null && getCount() > 0 && motionPosition != -1 && motionPosition < adapter.getCount() && (view = getChildAt(motionPosition - getFirstVisiblePosition())) != null && !this.mCheckedIdOnMove.contains(Integer.valueOf(motionPosition)) && findCheckbleView(view, eventX)) {
            performItemClickInner(view, motionPosition, adapter.getItemId(motionPosition));
            this.mCheckedIdOnMove.add(Integer.valueOf(motionPosition));
        }
    }

    private boolean performItemClickInner(View view, int position, long id) {
        boolean isCurItemViewFirstChecked = true;
        if (position == getMotionPosition()) {
            this.mIsFirstHasChangedOnMove = true;
        }
        if (getChoiceMode() == 0) {
            CheckBox checkBox = this.mCurItemView;
            if (!(checkBox == null || this.mIsFirstChecked == checkBox.isChecked())) {
                isCurItemViewFirstChecked = false;
            }
            if (isCurItemViewFirstChecked) {
                return false;
            }
            if (this.mCurItemView.getOnCheckedChangeListener() == null) {
                AdapterView.OnItemClickListener listener = getOnItemClickListener();
                if (listener == null) {
                    return true;
                }
                listener.onItemClick(this, view, position, id);
                return true;
            }
            this.mCurItemView.setChecked(this.mIsFirstChecked);
            return true;
        } else if (this.mIsFirstChecked != getCheckStates().get(position, false)) {
            return performItemClick(view, position, id);
        } else {
            return true;
        }
    }

    private boolean findCheckbleView(View view, int eventX) {
        boolean isNeedResearch = false;
        if (view.getClass().getName().indexOf("ContactListItemView") >= 0) {
            isNeedResearch = true;
        }
        return (isNeedResearch && inCheckableViewOnDown(view, eventX)) || (!isNeedResearch && inCheckableViewOnMove(view, eventX));
    }

    private boolean inCheckableViewOnMove(View view, int eventX) {
        View checkableView;
        if (view instanceof CheckedTextView) {
            CheckedTextView checkedTextView = (CheckedTextView) view;
            if (checkedTextView.getCheckMarkDrawable() != null) {
                if (checkedTextView.isCheckMarkAtStartEx()) {
                    if (eventX >= view.getLeft() + this.mMarkWidthOfCheckedTextView || eventX <= view.getLeft()) {
                        return false;
                    }
                    return true;
                } else if (eventX <= view.getRight() - this.mMarkWidthOfCheckedTextView || eventX >= view.getRight()) {
                    return false;
                } else {
                    return true;
                }
            }
        } else if ((view instanceof ViewGroup) && (checkableView = getCheckableView(view)) != null && (checkableView instanceof CheckBox)) {
            try {
                this.mCurItemView = (CheckBox) checkableView;
                return inCheckBox(checkableView, eventX);
            } catch (ClassCastException e) {
                Log.w(TAG, "Judge in checkbox view on move cast fialue.");
            }
        }
        return false;
    }

    private boolean inCheckBox(View checkbox, int eventX) {
        int[] checkboxLocations = new int[2];
        int[] listViewLocations = new int[2];
        checkbox.getLocationOnScreen(checkboxLocations);
        getLocationOnScreen(listViewLocations);
        int rowX = listViewLocations[0] + eventX;
        if (rowX <= checkboxLocations[0] || rowX >= checkboxLocations[0] + checkbox.getWidth()) {
            return false;
        }
        return true;
    }

    private View getCheckableView(View view) {
        if (this.mLevel <= 0) {
            return null;
        }
        View checkableView = view;
        int i = 1;
        while (i < this.mLevel && i < this.mChildPositionOnLevel.length) {
            try {
                if (checkableView != null) {
                    if (checkableView instanceof ViewGroup) {
                        checkableView = ((ViewGroup) checkableView).getChildAt(this.mChildPositionOnLevel[i]);
                        i++;
                    }
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        }
        return checkableView;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView
    public void onTouchUpEx(MotionEvent event) {
        if (hasScrollMultiSelectMask() && getTouchMode() == 7 && !eatTouchUpForMultiSelect(event)) {
            setTouchMode(0);
        }
        super.onTouchUpEx(event);
        onMultiSelectUp();
    }

    private boolean eatTouchUpForMultiSelect(MotionEvent event) {
        if (getTouchMode() != 7) {
            return false;
        }
        if (pointToPosition((int) event.getX(), (int) event.getY()) != getMotionPosition() || this.mIsFirstHasChangedOnMove) {
            return true;
        }
        return false;
    }

    private void onMultiSelectUp() {
        if (hasScrollMultiSelectMask() && getTouchMode() == 7) {
            setTouchMode(-1);
        }
    }

    @Override // android.widget.AbsListView, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (isSwipeLayoutItem() && isWidgetScollHorizontal(event)) {
            return false;
        }
        if (inMultiSelectMoveMode(event)) {
            return true;
        }
        return super.onInterceptTouchEvent(event);
    }

    private boolean isSwipeLayoutItem() {
        int itemPositon = getMotionPosition();
        if (itemPositon <= -1 || itemPositon >= getChildCount() || !getChildAt(itemPositon).getClass().getName().equals(SWIPELAYOUT)) {
            return false;
        }
        return true;
    }

    private boolean isWidgetScollHorizontal(MotionEvent event) {
        int action = event.getAction();
        if (action == 0) {
            this.mRawX = event.getRawX();
            this.mRawY = event.getRawY();
            return false;
        } else if (action != 2) {
            return false;
        } else {
            if (((float) Math.toDegrees(Math.atan((double) Math.abs((event.getRawY() - this.mRawY) / (event.getRawX() - this.mRawX))))) < 30.0f) {
                return true;
            }
            return false;
        }
    }

    private boolean inMultiSelectMoveMode(MotionEvent event) {
        return hasScrollMultiSelectMask() && getTouchMode() == 7 && event.getActionMasked() == 2;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView
    public void setIgnoreScrollMultiSelectStub() {
        this.mMask &= -2;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView
    public void enableScrollMultiSelectStub() {
        int i = this.mMask;
        if ((i & 1) == 0) {
            this.mMask = i | 1;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView
    public void setStableItemHeight(OverScroller scroller, AbsListView.FlingRunnable flingRunnable) {
        if (hasHighSpeedStableMask()) {
            if (getChildCount() > 2) {
                this.mItemHeight = getChildAt(1).getTop() - getChildAt(0).getTop();
                scroller.getIHwSplineOverScroller().setStableItemHeight(this.mItemHeight);
            }
            if (!scroller.isFinished()) {
                removeCallbacks(flingRunnable);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView
    public int adjustFlingDistance(int delta) {
        int i;
        int i2;
        boolean isMore = false;
        if (delta > 0) {
            int screen = ((getHeight() - this.mPaddingBottom) - this.mPaddingTop) - 1;
            if (Math.abs(delta) > Math.abs(screen)) {
                isMore = true;
            }
            int localDelta = screen <= delta ? screen : delta;
            if (!isMore || (i2 = this.mItemHeight) <= 0) {
                return localDelta;
            }
            return (localDelta / i2) * i2;
        }
        int screen2 = -(((getHeight() - this.mPaddingBottom) - this.mPaddingTop) - 1);
        if (Math.abs(delta) > Math.abs(screen2)) {
            isMore = true;
        }
        int localDelta2 = screen2 >= delta ? screen2 : delta;
        if (!isMore || (i = this.mItemHeight) <= 0) {
            return localDelta2;
        }
        return (localDelta2 / i) * i;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView
    public boolean hasScrollMultiSelectMask() {
        return (this.mMask & 1) != 0;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView
    public boolean hasSpringAnimatorMask() {
        return (this.mMask & 2) != 0;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView
    public boolean hasHighSpeedStableMask() {
        return (this.mMask & 4) != 0;
    }

    /* access modifiers changed from: private */
    public static class LeveledView {
        public int level;
        public String path;
        public View view;

        LeveledView(View view2, int level2, String path2) {
            this.view = view2;
            this.level = level2;
            this.path = path2;
        }

        public String toString() {
            return "(" + this.path + "):" + this.view;
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkIsEnabled(ListAdapter adapter, int position) {
        return adapter.isEnabled(position);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView
    public int getPressedStateDuration() {
        return 16;
    }

    private class AnimateAdapterDataSetObserver extends AbsListView.AdapterDataSetObserver {
        private AnimateAdapterDataSetObserver() {
            super();
        }

        @Override // android.widget.AbsListView.AdapterDataSetObserver, android.widget.AdapterView.AdapterDataSetObserver, android.database.DataSetObserver
        public void onChanged() {
            boolean isAnimValid = false;
            if (HwAbsListView.this.mIsSupportAnim) {
                isAnimValid = HwAbsListView.this.startDataChangeAnimation();
            }
            if (!isAnimValid) {
                onChangedAnimDone();
            }
        }

        public void onChangedAnimDone() {
            HwAbsListView hwAbsListView = HwAbsListView.this;
            hwAbsListView.mListDeleteAnimator = null;
            hwAbsListView.mAnimOffset = 0;
            super.onChanged();
        }
    }

    /* access modifiers changed from: protected */
    public void wrapObserver() {
        if (this.mDataSetObserver != null && this.mIsSupportAnim) {
            this.mAdapter.unregisterDataSetObserver(this.mDataSetObserver);
            this.mDataSetObserver = new AnimateAdapterDataSetObserver();
            this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
        }
    }

    /* access modifiers changed from: protected */
    public void onFirstPositionChange() {
        Object id;
        if (this.mListDeleteAnimator == null && this.mIsSupportAnim && this.mAdapter != null) {
            int start = this.mFirstPosition;
            int end = (getChildCount() + start) - 1;
            this.mVisibleItems.clear();
            for (int i = start; i <= end; i++) {
                if (this.mAdapter instanceof ArrayAdapter) {
                    id = this.mAdapter.getItem(i);
                } else {
                    id = Long.valueOf(this.mAdapter.getItemId(i));
                }
                this.mVisibleItems.add(id);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean startDataChangeAnimation() {
        ValueAnimator valueAnimator = this.mListDeleteAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.mListDeleteAnimator = null;
        }
        int oldItemCount = this.mItemCount;
        int newItemCount = this.mAdapter.getCount();
        if (oldItemCount - newItemCount != 1) {
            Log.w(ANIM_TAG, "ListView::startDataChangeAnimation: not delete 1");
            return false;
        }
        this.mAnimindex = -1;
        this.mAnimOffset = 0;
        this.mAnimatingFirstPos = this.mFirstPosition;
        int start = this.mFirstPosition;
        int childCount = getChildCount();
        int dataCount = this.mVisibleItems.size();
        if (childCount != dataCount) {
            Log.w(ANIM_TAG, "ListView::startDataChangeAnimation count not sync: " + childCount + ", " + dataCount);
            return false;
        }
        int end = (start + childCount) - 1;
        int id = 0;
        for (int i = start; i <= end; i++) {
            if (this.mAnimOffset + i < newItemCount) {
                if (this.mAdapter instanceof ArrayAdapter) {
                    id = this.mAdapter.getItem(this.mAnimOffset + i);
                } else {
                    id = Long.valueOf(this.mAdapter.getItemId(this.mAnimOffset + i));
                }
            }
            if (!this.mVisibleItems.get(i - this.mFirstPosition).equals(id) || this.mAnimOffset + i == newItemCount) {
                if (this.mAnimindex != -1) {
                    Log.w(ANIM_TAG, "ListView::startDataChangeAnimation: error, list view is changed without onFirstPositionChange.");
                    return false;
                }
                this.mAnimindex = i - this.mFirstPosition;
                this.mAnimOffset--;
            }
        }
        if (this.mAnimindex != -1) {
            return startDeleteAnimator();
        }
        Log.w(ANIM_TAG, "ListView::startDataChangeAnimation no visible item is deleted ");
        return false;
    }

    private boolean startDeleteAnimator() {
        this.mListDeleteAnimator = ValueAnimator.ofInt(100, 0);
        this.mListDeleteAnimator.setInterpolator(new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f));
        this.mListDeleteAnimator.setDuration((long) SystemProperties.getInt("durationList", 200));
        final View view = getChildAt(this.mAnimindex);
        final int viewHeight = view.getHeight();
        this.mListDeleteAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class android.widget.HwAbsListView.AnonymousClass1 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                if (HwAbsListView.this.mFirstPosition != HwAbsListView.this.mAnimatingFirstPos) {
                    Log.w(HwAbsListView.ANIM_TAG, "ListView::onAnimationUpdate: cancel pos is changed" + HwAbsListView.this.mAnimatingFirstPos + ", " + HwAbsListView.this.mFirstPosition);
                    animation.cancel();
                    return;
                }
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = (int) (((float) viewHeight) * (((float) ((Integer) animation.getAnimatedValue()).intValue()) / 100.0f));
                if (layoutParams.height == 0) {
                    layoutParams.height = 1;
                }
                view.setLayoutParams(layoutParams);
            }
        });
        this.mListDeleteAnimator.addListener(new AnimatorListenerAdapter() {
            /* class android.widget.HwAbsListView.AnonymousClass2 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                HwAbsListView hwAbsListView = HwAbsListView.this;
                hwAbsListView.mAnimOffset = 0;
                hwAbsListView.mListDeleteAnimator = null;
                hwAbsListView.mAnimindex = 0;
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = 0;
                view.setLayoutParams(layoutParams);
                HwAbsListView hwAbsListView2 = HwAbsListView.this;
                hwAbsListView2.mAnimatingFirstPos = 0;
                if (hwAbsListView2.mDataSetObserver instanceof AnimateAdapterDataSetObserver) {
                    ((AnimateAdapterDataSetObserver) HwAbsListView.this.mDataSetObserver).onChangedAnimDone();
                }
            }
        });
        this.mListDeleteAnimator.start();
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AbsListView
    public View obtainView(int position, boolean[] outMetadata) {
        int i;
        int localPosition = position;
        if (this.mIsSupportAnim && (i = this.mAnimOffset) != 0 && localPosition > this.mAnimindex + this.mAnimatingFirstPos) {
            localPosition += i;
        }
        return super.obtainView(localPosition, outMetadata);
    }
}
