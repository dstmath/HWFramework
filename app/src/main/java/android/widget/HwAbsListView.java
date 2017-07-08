package android.widget;

import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.PtmLog;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AbsListView.FlingRunnable;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Filter.FilterListener;
import android.widget.RemoteViewsAdapter.RemoteAdapterConnectionCallback;
import com.android.internal.R;
import huawei.cust.HwCfgFilePolicy;
import java.util.HashSet;
import java.util.Stack;

public abstract class HwAbsListView extends AbsListView implements TextWatcher, OnGlobalLayoutListener, FilterListener, OnTouchModeChangeListener, RemoteAdapterConnectionCallback {
    private static final boolean DEBUG = false;
    public static final int LISTVIEW_STUB_MASK_HIGH_SPEED_STABLE_ANIMATOR = 4;
    public static final int LISTVIEW_STUB_MASK_SCROLL_MULTI_SELECT = 1;
    public static final int LISTVIEW_STUB_MASK_SPRING_ANIMATOR = 2;
    private static final float MAX_OVER_SCROLLY_DISTANCE_FACTOR = 0.45f;
    private static final float MAX_OVER_SCROLL_SCALE = 0.04f;
    private static final float OVER_SCROLL_SCALE_DELTA = 0.01f;
    private static final int PRESSED_STATE_HWDURATION = 16;
    private static final int SCALE_ITEM_NUM = 4;
    private static final String SWIPELAYOUT = "huawei.android.widget.SwipeLayout";
    private static final String TAG = "AbsListView";
    public static final int TOUCH_MODE_SCROLL_MULTI_SELECT = 7;
    private HashSet<Integer> mCheckedIdOnMove;
    private int[] mChildPositionOnLevel;
    private CheckBox mCurItemView;
    private boolean mFirstChecked;
    private boolean mFirstHasChangedOnMove;
    private boolean mIsHwTheme;
    private int mItemHeight;
    private int mLevel;
    private int mMarkWidthOfCheckedTextView;
    private int mMask;
    private float sX;
    private float sY;

    private static class LeveledView {
        public int level;
        public String path;
        public View view;

        public LeveledView(View view, int level, String path) {
            this.view = view;
            this.level = level;
            this.path = path;
        }

        public String toString() {
            return "(" + this.path + "):" + this.view;
        }
    }

    public HwAbsListView(Context context) {
        super(context);
        this.mChildPositionOnLevel = new int[20];
        this.mLevel = 0;
        this.mCheckedIdOnMove = new HashSet();
        this.sX = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        this.sY = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        this.mIsHwTheme = checkIsHwTheme(context, null);
        initMask(this.mIsHwTheme);
        if (this.mIsHwTheme) {
            setOverScrollMode(0);
        }
    }

    public HwAbsListView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.absListViewStyle);
    }

    public HwAbsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwAbsListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mChildPositionOnLevel = new int[20];
        this.mLevel = 0;
        this.mCheckedIdOnMove = new HashSet();
        this.sX = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        this.sY = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
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
            this.mMask = TOUCH_MODE_SCROLL_MULTI_SELECT;
        } else {
            this.mMask = SCALE_ITEM_NUM;
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

    protected boolean getCheckedStateForMultiSelect(boolean curState) {
        if (hasScrollMultiSelectMask()) {
            return getItemCheckedState(curState);
        }
        return curState;
    }

    private boolean getItemCheckedState(boolean curChecked) {
        if (getTouchMode() == TOUCH_MODE_SCROLL_MULTI_SELECT) {
            return this.mFirstChecked;
        }
        return curChecked;
    }

    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        if (!hasSpringAnimatorMask()) {
            return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
        }
        int newDeltaY = deltaY;
        if (isTouchEvent) {
            newDeltaY = getElasticInterpolation(deltaY, scrollY);
        }
        int maxOverScrollYDistance = (int) (((float) getHeight()) * MAX_OVER_SCROLLY_DISTANCE_FACTOR);
        int absY = Math.abs(scrollY);
        int itemNum = Math.min(SCALE_ITEM_NUM, getChildCount());
        int i;
        float deltaScaleY;
        if (scrollY < 0) {
            for (i = 0; i < itemNum; i += LISTVIEW_STUB_MASK_SCROLL_MULTI_SELECT) {
                deltaScaleY = ((MAX_OVER_SCROLL_SCALE - (((float) i) * OVER_SCROLL_SCALE_DELTA)) * ((float) absY)) / ((float) maxOverScrollYDistance);
                getChildAt(i).setScaleX(LayoutParams.BRIGHTNESS_OVERRIDE_FULL - deltaScaleY);
                getChildAt(i).setScaleY(LayoutParams.BRIGHTNESS_OVERRIDE_FULL - deltaScaleY);
            }
        } else {
            for (i = getChildCount() - 1; i >= getChildCount() - itemNum; i--) {
                deltaScaleY = ((MAX_OVER_SCROLL_SCALE - (((float) ((getChildCount() - 1) - i)) * OVER_SCROLL_SCALE_DELTA)) * ((float) absY)) / ((float) maxOverScrollYDistance);
                getChildAt(i).setScaleX(LayoutParams.BRIGHTNESS_OVERRIDE_FULL - deltaScaleY);
                getChildAt(i).setScaleY(LayoutParams.BRIGHTNESS_OVERRIDE_FULL - deltaScaleY);
            }
        }
        invalidate();
        return super.overScrollBy(deltaX, newDeltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollYDistance, isTouchEvent);
    }

    protected void layoutChildren() {
        super.layoutChildren();
        int itemNum = Math.min(SCALE_ITEM_NUM, getChildCount());
        for (int i = 0; i < itemNum; i += LISTVIEW_STUB_MASK_SCROLL_MULTI_SELECT) {
            if (getChildAt(i).getScaleX() != LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
                getChildAt(i).setScaleX(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                getChildAt(i).setScaleY(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
            }
            int index = (getChildCount() - 1) - i;
            if (getChildAt(index).getScaleX() != LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
                getChildAt(index).setScaleX(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                getChildAt(index).setScaleY(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
            }
        }
    }

    private int getElasticInterpolation(int delta, int currentPos) {
        float len = (float) Math.abs(currentPos);
        int newDeltaY = (int) ((Math.sqrt(((double) (250.0f * ((float) Math.abs(delta)))) + Math.pow((double) len, 2.0d)) - ((double) len)) * ((double) Math.signum((float) delta)));
        if (Math.abs(newDeltaY) > Math.abs(delta)) {
            return delta;
        }
        return newDeltaY;
    }

    protected void dismissCurrentPressed() {
        View child = getChildAt(getMotionPosition() - getFirstVisiblePosition());
        if (child != null) {
            child.setPressed(DEBUG);
        }
    }

    protected void enterMultiSelectModeIfNeeded(int motionPosition, int x) {
        boolean z = true;
        if (hasScrollMultiSelectMask() && getTouchMode() != TOUCH_MODE_SCROLL_MULTI_SELECT && inCheckableViewOnDown(getChildAt(motionPosition - getFirstVisiblePosition()), x)) {
            setTouchMode(TOUCH_MODE_SCROLL_MULTI_SELECT);
            if (this.mCurItemView != null) {
                if (this.mCurItemView.isChecked()) {
                    z = DEBUG;
                }
                this.mFirstChecked = z;
            } else if (getChoiceMode() != 0) {
                if (getCheckStates().get(motionPosition, DEBUG)) {
                    z = DEBUG;
                }
                this.mFirstChecked = z;
            }
            this.mFirstHasChangedOnMove = DEBUG;
            this.mCheckedIdOnMove.clear();
        }
    }

    private boolean inCheckableViewOnDown(View view, int x) {
        boolean z = DEBUG;
        if (view instanceof CheckedTextView) {
            if (view.getVisibility() != 0) {
                return DEBUG;
            }
            CheckedTextView ctView = (CheckedTextView) view;
            if (ctView.getCheckMarkDrawable() != null) {
                this.mMarkWidthOfCheckedTextView = ctView.getCheckMarkDrawable().getIntrinsicWidth();
                if (x > view.getRight() - this.mMarkWidthOfCheckedTextView && x < view.getRight()) {
                    z = true;
                }
                return z;
            }
        } else if (view instanceof ViewGroup) {
            return searchCheckableView((ViewGroup) view, x);
        }
        return DEBUG;
    }

    private boolean searchCheckableView(ViewGroup root, int x) {
        int i;
        boolean z;
        Stack<LeveledView> stack = new Stack();
        String path = "";
        String childPath = "";
        boolean found = DEBUG;
        stack.push(new LeveledView(root, 0, "0"));
        while (!stack.empty()) {
            LeveledView curView = (LeveledView) stack.pop();
            if (curView.view instanceof ViewGroup) {
                ViewGroup vg = curView.view;
                int level = curView.level + LISTVIEW_STUB_MASK_SCROLL_MULTI_SELECT;
                path = curView.path;
                if (level > this.mChildPositionOnLevel.length) {
                    break;
                }
                i = 0;
                while (i < vg.getChildCount()) {
                    View childView = vg.getChildAt(i);
                    childPath = path + PtmLog.PAIRE_DELIMETER + i;
                    if ((childView instanceof CheckBox) && childView.getVisibility() == 0) {
                        stack.push(new LeveledView(childView, level, path + PtmLog.PAIRE_DELIMETER + i));
                        if (inCheckBox(childView, x)) {
                            found = true;
                        }
                    } else {
                        if (childView instanceof ViewGroup) {
                            stack.push(new LeveledView(childView, level, childPath));
                        }
                        i += LISTVIEW_STUB_MASK_SCROLL_MULTI_SELECT;
                    }
                }
                continue;
            }
        }
        if (found && (((LeveledView) stack.peek()).view instanceof CheckBox)) {
            LeveledView check = (LeveledView) stack.pop();
            if (!(check.path == null || check.path.isEmpty())) {
                String[] leveledPath = check.path.split(PtmLog.PAIRE_DELIMETER);
                if (leveledPath != null && leveledPath.length <= this.mChildPositionOnLevel.length) {
                    this.mLevel = leveledPath.length;
                    this.mCurItemView = (CheckBox) check.view;
                    i = 0;
                    while (i < leveledPath.length) {
                        try {
                            this.mChildPositionOnLevel[i] = Integer.parseInt(leveledPath[i]);
                            i += LISTVIEW_STUB_MASK_SCROLL_MULTI_SELECT;
                        } catch (Exception e) {
                            this.mLevel = 0;
                            this.mCurItemView = null;
                            return DEBUG;
                        }
                    }
                }
            }
        }
        if (!found || this.mLevel <= 0) {
            z = DEBUG;
        } else {
            z = true;
        }
        return z;
    }

    protected void onMultiSelectMove(MotionEvent ev, int pointerIndex) {
        if (getTouchMode() == TOUCH_MODE_SCROLL_MULTI_SELECT && hasScrollMultiSelectMask()) {
            clickItemIfNeeded(findClosestMotionRow((int) ev.getY(pointerIndex)), (int) ev.getX(pointerIndex));
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
            if (this.mFirstChecked != getCheckStates().get(position, DEBUG)) {
                return performItemClick(view, position, id);
            }
            return true;
        } else if (this.mCurItemView == null || this.mFirstChecked == this.mCurItemView.isChecked()) {
            return DEBUG;
        } else {
            if (this.mCurItemView == null) {
                return true;
            }
            if (this.mCurItemView.getOnCheckedChangeListener() == null) {
                OnItemClickListener listener = getOnItemClickListener();
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
        boolean needResearch = DEBUG;
        if (view.getClass().getName().indexOf("ContactListItemView") >= 0) {
            needResearch = true;
        }
        if (needResearch && inCheckableViewOnDown(view, x)) {
            return true;
        }
        if (needResearch) {
            return DEBUG;
        }
        return inCheckableViewOnMove(view, x);
    }

    private boolean inCheckableViewOnMove(View view, int x) {
        if (view instanceof CheckedTextView) {
            if (((CheckedTextView) view).getCheckMarkDrawable() != null) {
                boolean z = (x <= view.getRight() - this.mMarkWidthOfCheckedTextView || x >= view.getRight()) ? DEBUG : true;
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
        return DEBUG;
    }

    private boolean inCheckBox(View cb, int x) {
        int[] location = new int[LISTVIEW_STUB_MASK_SPRING_ANIMATOR];
        cb.getLocationOnScreen(location);
        if (x <= location[0] || x >= location[0] + cb.getWidth()) {
            return DEBUG;
        }
        return true;
    }

    private View getCheckableView(View view) {
        if (this.mLevel <= 0) {
            return null;
        }
        View check = view;
        int i = LISTVIEW_STUB_MASK_SCROLL_MULTI_SELECT;
        while (i < this.mLevel && i < this.mChildPositionOnLevel.length) {
            try {
                check = ((ViewGroup) check).getChildAt(this.mChildPositionOnLevel[i]);
                i += LISTVIEW_STUB_MASK_SCROLL_MULTI_SELECT;
            } catch (Exception e) {
                return null;
            }
        }
        return check;
    }

    protected void onTouchUp(MotionEvent ev) {
        if (hasScrollMultiSelectMask() && getTouchMode() == TOUCH_MODE_SCROLL_MULTI_SELECT && !eatTouchUpForMultiSelect(ev)) {
            setTouchMode(0);
        }
        super.onTouchUp(ev);
        onMultiSelectUp();
    }

    private boolean eatTouchUpForMultiSelect(MotionEvent ev) {
        if (getTouchMode() != TOUCH_MODE_SCROLL_MULTI_SELECT || (pointToPosition((int) ev.getX(), (int) ev.getY()) == getMotionPosition() && !this.mFirstHasChangedOnMove)) {
            return DEBUG;
        }
        return true;
    }

    private void onMultiSelectUp() {
        if (hasScrollMultiSelectMask() && getTouchMode() == TOUCH_MODE_SCROLL_MULTI_SELECT) {
            setTouchMode(-1);
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isSwipeLayoutItem() && isWidgetScollHorizontal(ev)) {
            return DEBUG;
        }
        if (inMultiSelectMoveMode(ev)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private boolean isSwipeLayoutItem() {
        int itemPositon = getMotionPosition();
        if (itemPositon <= -1 || itemPositon >= getChildCount() || !getChildAt(itemPositon).getClass().getName().equals(SWIPELAYOUT)) {
            return DEBUG;
        }
        return true;
    }

    private boolean isWidgetScollHorizontal(MotionEvent ev) {
        switch (ev.getAction()) {
            case HwCfgFilePolicy.GLOBAL /*0*/:
                this.sX = ev.getRawX();
                this.sY = ev.getRawY();
                break;
            case LISTVIEW_STUB_MASK_SPRING_ANIMATOR /*2*/:
                if (((float) Math.toDegrees(Math.atan((double) Math.abs((ev.getRawY() - this.sY) / (ev.getRawX() - this.sX))))) < 30.0f) {
                    return true;
                }
                break;
        }
        return DEBUG;
    }

    private boolean inMultiSelectMoveMode(MotionEvent ev) {
        if (hasScrollMultiSelectMask() && getTouchMode() == TOUCH_MODE_SCROLL_MULTI_SELECT && ev.getActionMasked() == LISTVIEW_STUB_MASK_SPRING_ANIMATOR) {
            return true;
        }
        return DEBUG;
    }

    protected void setIgnoreScrollMultiSelectStub() {
        this.mMask &= -2;
    }

    protected void setStableItemHeight(OverScroller scroller, FlingRunnable fr) {
        if (hasHighSpeedStableMask()) {
            if (getChildCount() > LISTVIEW_STUB_MASK_SPRING_ANIMATOR) {
                this.mItemHeight = getChildAt(LISTVIEW_STUB_MASK_SCROLL_MULTI_SELECT).getTop() - getChildAt(0).getTop();
                scroller.getIHwSplineOverScroller().setStableItemHeight(this.mItemHeight);
            }
            if (!scroller.isFinished()) {
                removeCallbacks(fr);
            }
        }
    }

    protected int adjustFlingDistance(int delta) {
        boolean isMore;
        if (delta > 0) {
            int screen = ((getHeight() - this.mPaddingBottom) - this.mPaddingTop) - 1;
            isMore = Math.abs(delta) > Math.abs(screen) ? true : DEBUG;
            delta = Math.min(screen, delta);
            if (!isMore || this.mItemHeight <= 0) {
                return delta;
            }
            return (delta / this.mItemHeight) * this.mItemHeight;
        }
        screen = -(((getHeight() - this.mPaddingBottom) - this.mPaddingTop) - 1);
        isMore = Math.abs(delta) > Math.abs(screen) ? true : DEBUG;
        delta = Math.max(screen, delta);
        if (!isMore || this.mItemHeight <= 0) {
            return delta;
        }
        return (delta / this.mItemHeight) * this.mItemHeight;
    }

    protected boolean hasScrollMultiSelectMask() {
        return (this.mMask & LISTVIEW_STUB_MASK_SCROLL_MULTI_SELECT) != 0 ? true : DEBUG;
    }

    protected boolean hasSpringAnimatorMask() {
        return (this.mMask & LISTVIEW_STUB_MASK_SPRING_ANIMATOR) != 0 ? true : DEBUG;
    }

    protected boolean hasHighSpeedStableMask() {
        return (this.mMask & SCALE_ITEM_NUM) != 0 ? true : DEBUG;
    }

    protected boolean checkIsEnabled(ListAdapter adapter, int position) {
        return adapter.isEnabled(position);
    }

    protected int getPressedStateDuration() {
        return PRESSED_STATE_HWDURATION;
    }
}
