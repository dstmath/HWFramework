package android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemProperties;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
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
    private static final boolean DEBUG = false;
    public static final int LISTVIEW_STUB_MASK_HIGH_SPEED_STABLE_ANIMATOR = 4;
    public static final int LISTVIEW_STUB_MASK_SCROLL_MULTI_SELECT = 1;
    public static final int LISTVIEW_STUB_MASK_SPRING_ANIMATOR = 2;
    private static final float MAX_OVER_SCROLLY_DISTANCE_FACTOR = 0.45f;
    private static final int PRESSED_STATE_HWDURATION = 16;
    private static final String SWIPELAYOUT = "huawei.android.widget.SwipeLayout";
    private static final String TAG = "AbsListView";
    public static final int TOUCH_MODE_SCROLL_MULTI_SELECT = 7;
    protected int mAnimOffset;
    protected int mAnimatingFirstPos;
    protected int mAnimindex;
    private HashSet<Integer> mCheckedIdOnMove;
    private int[] mChildPositionOnLevel;
    private CheckBox mCurItemView;
    private boolean mExitScrollEnterMultiSelectFlag;
    private boolean mFirstChecked;
    private boolean mFirstHasChangedOnMove;
    private boolean mIsHwTheme;
    protected boolean mIsSupportAnim;
    private int mItemHeight;
    private int mLevel;
    protected ValueAnimator mListDeleteAnimator;
    private int mListRightBoundary;
    private int mMarkWidthOfCheckedTextView;
    private int mMask;
    protected ArrayList<Object> mVisibleItems;
    private float sX;
    private float sY;

    protected class AnimateAdapterDataSetObserver extends AbsListView.AdapterDataSetObserver {
        protected AnimateAdapterDataSetObserver() {
            super();
        }

        public void onChanged() {
            if (!HwAbsListView.this.startDataChangeAnimation()) {
                onChangedAnimDone();
            }
        }

        public void onChangedAnimDone() {
            HwAbsListView.this.mListDeleteAnimator = null;
            HwAbsListView.this.mAnimOffset = 0;
            super.onChanged();
        }
    }

    private static class LeveledView {
        public int level;
        public String path;
        public View view;

        public LeveledView(View view2, int level2, String path2) {
            this.view = view2;
            this.level = level2;
            this.path = path2;
        }

        public String toString() {
            return "(" + this.path + "):" + this.view;
        }
    }

    public HwAbsListView(Context context) {
        super(context);
        this.mChildPositionOnLevel = new int[20];
        this.mLevel = 0;
        this.mCheckedIdOnMove = new HashSet<>();
        this.mExitScrollEnterMultiSelectFlag = false;
        this.sX = -1.0f;
        this.sY = -1.0f;
        this.mIsSupportAnim = false;
        this.mAnimOffset = 0;
        this.mVisibleItems = new ArrayList<>();
        this.mIsHwTheme = checkIsHwTheme(context, null);
        initMask(this.mIsHwTheme);
        if (this.mIsHwTheme) {
            setOverScrollMode(0);
        }
    }

    public HwAbsListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842858);
    }

    public HwAbsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwAbsListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mChildPositionOnLevel = new int[20];
        this.mLevel = 0;
        this.mCheckedIdOnMove = new HashSet<>();
        this.mExitScrollEnterMultiSelectFlag = false;
        this.sX = -1.0f;
        this.sY = -1.0f;
        this.mIsSupportAnim = false;
        this.mAnimOffset = 0;
        this.mVisibleItems = new ArrayList<>();
        this.mIsHwTheme = checkIsHwTheme(context, attrs);
        initMask(this.mIsHwTheme);
        if (this.mIsHwTheme) {
            setOverScrollMode(0);
        }
        initSpringBackEffect();
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

    public void setOverScrollMode(int mode) {
        super.setOverScrollMode(mode);
        if (hasSpringAnimatorMask()) {
            setEdgeGlowTopBottom(null, null);
        }
    }

    /* access modifiers changed from: protected */
    public boolean getCheckedStateForMultiSelect(boolean curState) {
        if (hasScrollMultiSelectMask()) {
            return getItemCheckedState(curState);
        }
        return curState;
    }

    private boolean getItemCheckedState(boolean curChecked) {
        if (getTouchMode() == 7) {
            return this.mFirstChecked;
        }
        return curChecked;
    }

    /* access modifiers changed from: protected */
    public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        int i;
        HwAbsListView hwAbsListView;
        if (!hasSpringAnimatorMask()) {
            return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
        }
        int newDeltaY = deltaY;
        if (isTouchEvent) {
            hwAbsListView = this;
            i = scrollY;
            newDeltaY = hwAbsListView.getElasticInterpolation(deltaY, i);
        } else {
            hwAbsListView = this;
            int i2 = deltaY;
            i = scrollY;
        }
        int maxOverScrollYDistance = (int) (((float) hwAbsListView.getHeight()) * MAX_OVER_SCROLLY_DISTANCE_FACTOR);
        hwAbsListView.invalidate();
        return super.overScrollBy(deltaX, newDeltaY, scrollX, i, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollYDistance, isTouchEvent);
    }

    private int getElasticInterpolation(int delta, int currentPos) {
        float len = (float) Math.abs(currentPos);
        int newDeltaY = (int) ((Math.sqrt(((double) (250.0f * ((float) Math.abs(delta)))) + Math.pow((double) len, 2.0d)) - ((double) len)) * ((double) Math.signum((float) delta)));
        if (Math.abs(newDeltaY) > Math.abs(delta)) {
            return delta;
        }
        return newDeltaY;
    }

    /* access modifiers changed from: protected */
    public void dismissCurrentPressed() {
        View child = getChildAt(getMotionPosition() - getFirstVisiblePosition());
        if (child != null) {
            child.setPressed(false);
        }
    }

    /* access modifiers changed from: protected */
    public void enterMultiSelectModeIfNeeded(int motionPosition, int x) {
        if (hasScrollMultiSelectMask() && getTouchMode() != 7 && inCheckableViewOnDown(getChildAt(motionPosition - getFirstVisiblePosition()), x)) {
            setTouchMode(7);
            if (this.mCurItemView != null) {
                this.mFirstChecked = !this.mCurItemView.isChecked();
            } else if (getChoiceMode() != 0) {
                this.mFirstChecked = !getCheckStates().get(motionPosition, false);
            }
            this.mFirstHasChangedOnMove = false;
            this.mCheckedIdOnMove.clear();
        }
    }

    private boolean inCheckableViewOnDown(View view, int x) {
        boolean z = false;
        if (view instanceof CheckedTextView) {
            if (view.getVisibility() != 0) {
                return false;
            }
            CheckedTextView ctView = (CheckedTextView) view;
            if (ctView.getCheckMarkDrawable() != null) {
                this.mMarkWidthOfCheckedTextView = ctView.getCheckMarkDrawable().getIntrinsicWidth();
                if (ctView.isCheckMarkAtStartEx()) {
                    if (x < view.getLeft() + this.mMarkWidthOfCheckedTextView && x > view.getLeft()) {
                        z = true;
                    }
                    return z;
                }
                if (x > view.getRight() - this.mMarkWidthOfCheckedTextView && x < view.getRight()) {
                    z = true;
                }
                return z;
            }
        } else if (view instanceof ViewGroup) {
            return searchCheckableView((ViewGroup) view, x);
        }
        return false;
    }

    private boolean searchCheckableView(ViewGroup root, int x) {
        Stack stack = new Stack();
        int level = 0;
        String path = "";
        String childPath = "";
        boolean found = false;
        stack.push(new LeveledView(root, 0, WifiEnterpriseConfig.ENGINE_DISABLE));
        while (true) {
            if (stack.empty()) {
                break;
            }
            LeveledView curView = (LeveledView) stack.pop();
            if (curView.view instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) curView.view;
                level = curView.level + 1;
                path = curView.path;
                if (level > this.mChildPositionOnLevel.length) {
                    break;
                }
                String childPath2 = childPath;
                int i = 0;
                while (i < vg.getChildCount()) {
                    View childView = vg.getChildAt(i);
                    childPath2 = path + "," + i;
                    if (!(childView instanceof CheckBox) || childView.getVisibility() != 0) {
                        int i2 = x;
                        if (childView instanceof ViewGroup) {
                            stack.push(new LeveledView(childView, level, childPath2));
                        }
                        i++;
                    } else {
                        stack.push(new LeveledView(childView, level, path + "," + i));
                        if (inCheckBox(childView, x)) {
                            found = true;
                        }
                    }
                }
                int i3 = x;
                childPath = childPath2;
            } else {
                int i4 = x;
            }
        }
        int i5 = x;
        if (found && (((LeveledView) stack.peek()).view instanceof CheckBox)) {
            LeveledView check = (LeveledView) stack.pop();
            if (check.path != null && !check.path.isEmpty()) {
                String[] leveledPath = check.path.split(",");
                if (leveledPath != null && leveledPath.length <= this.mChildPositionOnLevel.length) {
                    this.mLevel = leveledPath.length;
                    this.mCurItemView = (CheckBox) check.view;
                    int i6 = 0;
                    while (true) {
                        int i7 = i6;
                        if (i7 >= leveledPath.length) {
                            break;
                        }
                        try {
                            this.mChildPositionOnLevel[i7] = Integer.parseInt(leveledPath[i7]);
                            i6 = i7 + 1;
                        } catch (Exception e) {
                            this.mLevel = 0;
                            this.mCurItemView = null;
                            return false;
                        }
                    }
                }
            }
        }
        return found && this.mLevel > 0;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (this.mListRightBoundary != getRight() && this.mMultiSelectAutoScrollFlag) {
            this.mListRightBoundary = getRight();
            if (this.mPositionScroller != null) {
                this.mPositionScroller.stop();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onMultiSelectMove(MotionEvent ev, int pointerIndex) {
        int x = (int) ev.getX(pointerIndex);
        int y = (int) ev.getY(pointerIndex);
        int motionPosition = findClosestMotionRow(y);
        if (this.mExitScrollEnterMultiSelectFlag) {
            this.mExitScrollEnterMultiSelectFlag = false;
            setTouchMode(7);
        }
        if ((getTouchMode() == 7 || getTouchMode() == 4) && hasScrollMultiSelectMask()) {
            clickItemIfNeeded(motionPosition, x);
        }
        if (this.mMultiSelectAutoScrollFlag) {
            int lastViewIndex = getChildCount() - 1;
            if (lastViewIndex >= 0) {
                int i = this.mFirstPosition + lastViewIndex;
                View lastView = getChildAt(lastViewIndex);
                if (lastView != null) {
                    int lastViewHeight = lastView.getHeight();
                    if (getTouchMode() == 7) {
                        if (y >= getBottom() - lastViewHeight && !this.mIsAutoScroll) {
                            this.mIsAutoScroll = true;
                            smoothScrollToPosition(getCount() - 1);
                        } else if (y <= getTop() + lastViewHeight && !this.mIsAutoScroll) {
                            this.mIsAutoScroll = true;
                            smoothScrollToPosition(0);
                        }
                    }
                    if (y >= getTop() + lastViewHeight && y <= getBottom() - lastViewHeight && this.mIsAutoScroll) {
                        if (this.mPositionScroller != null) {
                            this.mPositionScroller.stop();
                        }
                        setTouchMode(7);
                        this.mIsAutoScroll = false;
                        this.mExitScrollEnterMultiSelectFlag = true;
                    }
                }
            }
        }
    }

    private void clickItemIfNeeded(int motionPosition, int x) {
        ListAdapter adapter = getAdapter();
        if (adapter != null && getCount() > 0 && motionPosition != -1 && motionPosition < adapter.getCount()) {
            View view = getChildAt(motionPosition - getFirstVisiblePosition());
            if (view != null && !this.mCheckedIdOnMove.contains(Integer.valueOf(motionPosition)) && findCheckbleView(view, x)) {
                performItemClickInner(view, motionPosition, adapter.getItemId(motionPosition));
                this.mCheckedIdOnMove.add(Integer.valueOf(motionPosition));
            }
        }
    }

    private boolean performItemClickInner(View view, int position, long id) {
        if (position == getMotionPosition()) {
            this.mFirstHasChangedOnMove = true;
        }
        if (getChoiceMode() != 0) {
            if (this.mFirstChecked != getCheckStates().get(position, false)) {
                return performItemClick(view, position, id);
            }
            return true;
        } else if (this.mCurItemView == null || this.mFirstChecked == this.mCurItemView.isChecked()) {
            return false;
        } else {
            if (this.mCurItemView == null) {
                return true;
            }
            if (this.mCurItemView.getOnCheckedChangeListener() == null) {
                AdapterView.OnItemClickListener listener = getOnItemClickListener();
                if (listener == null) {
                    return true;
                }
                listener.onItemClick(this, view, position, id);
                return true;
            }
            this.mCurItemView.setChecked(this.mFirstChecked);
            return true;
        }
    }

    private boolean findCheckbleView(View view, int x) {
        boolean needResearch = false;
        if (view.getClass().getName().indexOf("ContactListItemView") >= 0) {
            needResearch = true;
        }
        return (needResearch && inCheckableViewOnDown(view, x)) || (!needResearch && inCheckableViewOnMove(view, x));
    }

    private boolean inCheckableViewOnMove(View view, int x) {
        boolean z = false;
        if (view instanceof CheckedTextView) {
            CheckedTextView ctView = (CheckedTextView) view;
            if (ctView.getCheckMarkDrawable() != null) {
                if (ctView.isCheckMarkAtStartEx()) {
                    if (x < view.getLeft() + this.mMarkWidthOfCheckedTextView && x > view.getLeft()) {
                        z = true;
                    }
                    return z;
                }
                if (x > view.getRight() - this.mMarkWidthOfCheckedTextView && x < view.getRight()) {
                    z = true;
                }
                return z;
            }
        } else if (view instanceof ViewGroup) {
            View cbView = getCheckableView(view);
            if (cbView != null) {
                try {
                    this.mCurItemView = (CheckBox) cbView;
                    return inCheckBox(cbView, x);
                } catch (ClassCastException e) {
                    Log.w(TAG, "Judge in checkbox view on move cast fialue.");
                }
            }
        }
        return false;
    }

    private boolean inCheckBox(View cb, int x) {
        int[] location = new int[2];
        cb.getLocationOnScreen(location);
        return x > location[0] && x < location[0] + cb.getWidth();
    }

    private View getCheckableView(View view) {
        if (this.mLevel <= 0) {
            return null;
        }
        View check = view;
        int i = 1;
        while (i < this.mLevel && i < this.mChildPositionOnLevel.length) {
            try {
                check = ((ViewGroup) check).getChildAt(this.mChildPositionOnLevel[i]);
                i++;
            } catch (Exception e) {
                return null;
            }
        }
        return check;
    }

    /* access modifiers changed from: protected */
    public void onTouchUpEx(MotionEvent ev) {
        if (hasScrollMultiSelectMask() && getTouchMode() == 7 && !eatTouchUpForMultiSelect(ev)) {
            setTouchMode(0);
        }
        super.onTouchUpEx(ev);
        onMultiSelectUp();
    }

    private boolean eatTouchUpForMultiSelect(MotionEvent ev) {
        if (getTouchMode() != 7 || (pointToPosition((int) ev.getX(), (int) ev.getY()) == getMotionPosition() && !this.mFirstHasChangedOnMove)) {
            return false;
        }
        return true;
    }

    private void onMultiSelectUp() {
        if (hasScrollMultiSelectMask() && getTouchMode() == 7) {
            setTouchMode(-1);
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isSwipeLayoutItem() && isWidgetScollHorizontal(ev)) {
            return false;
        }
        if (inMultiSelectMoveMode(ev)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private boolean isSwipeLayoutItem() {
        int itemPositon = getMotionPosition();
        if (itemPositon <= -1 || itemPositon >= getChildCount() || !getChildAt(itemPositon).getClass().getName().equals(SWIPELAYOUT)) {
            return false;
        }
        return true;
    }

    private boolean isWidgetScollHorizontal(MotionEvent ev) {
        int action = ev.getAction();
        if (action == 0) {
            this.sX = ev.getRawX();
            this.sY = ev.getRawY();
        } else if (action == 2) {
            if (((float) Math.toDegrees(Math.atan((double) Math.abs((ev.getRawY() - this.sY) / (ev.getRawX() - this.sX))))) < 30.0f) {
                return true;
            }
        }
        return false;
    }

    private boolean inMultiSelectMoveMode(MotionEvent ev) {
        return hasScrollMultiSelectMask() && getTouchMode() == 7 && ev.getActionMasked() == 2;
    }

    /* access modifiers changed from: protected */
    public void setIgnoreScrollMultiSelectStub() {
        this.mMask &= -2;
    }

    /* access modifiers changed from: protected */
    public void enableScrollMultiSelectStub() {
        if ((this.mMask & 1) == 0) {
            this.mMask |= 1;
        }
    }

    /* access modifiers changed from: protected */
    public void setStableItemHeight(OverScroller scroller, AbsListView.FlingRunnable fr) {
        if (hasHighSpeedStableMask()) {
            if (getChildCount() > 2) {
                this.mItemHeight = getChildAt(1).getTop() - getChildAt(0).getTop();
                scroller.getIHwSplineOverScroller().setStableItemHeight(this.mItemHeight);
            }
            if (!scroller.isFinished()) {
                removeCallbacks(fr);
            }
        }
    }

    /* access modifiers changed from: protected */
    public int adjustFlingDistance(int delta) {
        boolean isMore = false;
        if (delta > 0) {
            int screen = ((getHeight() - this.mPaddingBottom) - this.mPaddingTop) - 1;
            if (Math.abs(delta) > Math.abs(screen)) {
                isMore = true;
            }
            int delta2 = Math.min(screen, delta);
            if (!isMore || this.mItemHeight <= 0) {
                return delta2;
            }
            return (delta2 / this.mItemHeight) * this.mItemHeight;
        }
        int screen2 = -(((getHeight() - this.mPaddingBottom) - this.mPaddingTop) - 1);
        if (Math.abs(delta) > Math.abs(screen2)) {
            isMore = true;
        }
        int delta3 = Math.max(screen2, delta);
        if (!isMore || this.mItemHeight <= 0) {
            return delta3;
        }
        return (delta3 / this.mItemHeight) * this.mItemHeight;
    }

    /* access modifiers changed from: protected */
    public boolean hasScrollMultiSelectMask() {
        return (this.mMask & 1) != 0;
    }

    /* access modifiers changed from: protected */
    public boolean hasSpringAnimatorMask() {
        return (this.mMask & 2) != 0;
    }

    /* access modifiers changed from: protected */
    public boolean hasHighSpeedStableMask() {
        return (this.mMask & 4) != 0;
    }

    /* access modifiers changed from: protected */
    public boolean checkIsEnabled(ListAdapter adapter, int position) {
        return adapter.isEnabled(position);
    }

    /* access modifiers changed from: protected */
    public int getPressedStateDuration() {
        return 16;
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
        if (this.mListDeleteAnimator == null && this.mIsSupportAnim) {
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
        if (this.mListDeleteAnimator != null) {
            this.mListDeleteAnimator.cancel();
            this.mListDeleteAnimator = null;
        }
        int olditemcount = this.mItemCount;
        int newItemCount = this.mAdapter.getCount();
        if (olditemcount - newItemCount != 1) {
            Log.w(ANIM_TAG, "ListView::startDataChangeAnimation: not delete 1");
            return false;
        }
        this.mAnimindex = -1;
        this.mAnimOffset = 0;
        this.mAnimatingFirstPos = this.mFirstPosition;
        int start = this.mFirstPosition;
        int childCount = getChildCount();
        if (childCount != this.mVisibleItems.size()) {
            Log.w(ANIM_TAG, "ListView::startDataChangeAnimation count not sync: " + childCount + ", " + dataCount);
            return false;
        }
        int end = (start + childCount) - 1;
        Object id = 0;
        for (int i = start; i <= end; i++) {
            if (this.mAnimOffset + i < newItemCount) {
                if (this.mAdapter instanceof ArrayAdapter) {
                    id = this.mAdapter.getItem(this.mAnimOffset + i);
                } else {
                    id = Long.valueOf(this.mAdapter.getItemId(this.mAnimOffset + i));
                }
            }
            if (!id.equals(this.mVisibleItems.get(i - this.mFirstPosition)) || this.mAnimOffset + i == newItemCount) {
                if (this.mAnimindex != -1) {
                    Log.w(ANIM_TAG, "ListView::startDataChangeAnimation: error, list view is changed without onFirstPositionChange.");
                    return false;
                }
                this.mAnimindex = i - this.mFirstPosition;
                this.mAnimOffset--;
            }
        }
        if (this.mAnimindex == -1) {
            Log.w(ANIM_TAG, "ListView::startDataChangeAnimation no visible item is deleted ");
            return false;
        }
        this.mListDeleteAnimator = ValueAnimator.ofInt(new int[]{100, 0});
        this.mListDeleteAnimator.setInterpolator(new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f));
        this.mListDeleteAnimator.setDuration((long) SystemProperties.getInt("durationList", 200));
        final View v = getChildAt(this.mAnimindex);
        final int height0 = v.getHeight();
        this.mListDeleteAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if (HwAbsListView.this.mFirstPosition != HwAbsListView.this.mAnimatingFirstPos) {
                    Log.w(HwAbsListView.ANIM_TAG, "ListView::onAnimationUpdate: cancel pos is changed" + HwAbsListView.this.mAnimatingFirstPos + ", " + HwAbsListView.this.mFirstPosition);
                    animation.cancel();
                    return;
                }
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                lp.height = (int) (((float) height0) * (((float) ((Integer) animation.getAnimatedValue()).intValue()) / 100.0f));
                if (lp.height == 0) {
                    lp.height = 1;
                }
                v.setLayoutParams(lp);
            }
        });
        this.mListDeleteAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                HwAbsListView.this.mAnimOffset = 0;
                HwAbsListView.this.mListDeleteAnimator = null;
                HwAbsListView.this.mAnimindex = 0;
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                lp.height = 0;
                v.setLayoutParams(lp);
                HwAbsListView.this.mAnimatingFirstPos = 0;
                if (HwAbsListView.this.mDataSetObserver instanceof AnimateAdapterDataSetObserver) {
                    ((AnimateAdapterDataSetObserver) HwAbsListView.this.mDataSetObserver).onChangedAnimDone();
                }
            }
        });
        this.mListDeleteAnimator.start();
        return true;
    }

    /* access modifiers changed from: package-private */
    public View obtainView(int position, boolean[] outMetadata) {
        if (this.mIsSupportAnim && this.mAnimOffset != 0 && position > this.mAnimindex + this.mAnimatingFirstPos) {
            position += this.mAnimOffset;
        }
        return super.obtainView(position, outMetadata);
    }
}
