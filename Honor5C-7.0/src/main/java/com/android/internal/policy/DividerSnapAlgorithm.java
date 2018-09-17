package com.android.internal.policy;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.view.DisplayInfo;
import android.view.WindowManager.LayoutParams;
import com.android.internal.R;
import com.android.internal.widget.AutoScrollHelper;
import java.util.ArrayList;

public class DividerSnapAlgorithm {
    private static final int MIN_DISMISS_VELOCITY_DP_PER_SECOND = 600;
    private static final int MIN_FLING_VELOCITY_DP_PER_SECOND = 400;
    private static final int SNAP_FIXED_RATIO = 1;
    private static final int SNAP_MODE_16_9 = 0;
    private static final int SNAP_ONLY_1_1 = 2;
    private final SnapTarget mDismissEndTarget;
    private final SnapTarget mDismissStartTarget;
    private final int mDisplayHeight;
    private final int mDisplayWidth;
    private final int mDividerSize;
    private final SnapTarget mFirstSplitTarget;
    private final float mFixedRatio;
    private final Rect mInsets;
    private boolean mIsHorizontalDivision;
    private final SnapTarget mLastSplitTarget;
    private final SnapTarget mMiddleTarget;
    private final float mMinDismissVelocityPxPerSecond;
    private final float mMinFlingVelocityPxPerSecond;
    private final int mMinimalSizeResizableTask;
    private final int mSnapMode;
    private final ArrayList<SnapTarget> mTargets;

    public static class SnapTarget {
        public static final int FLAG_DISMISS_END = 2;
        public static final int FLAG_DISMISS_START = 1;
        public static final int FLAG_NONE = 0;
        private final float distanceMultiplier;
        public final int flag;
        public final int position;
        public final int taskPosition;

        public SnapTarget(int position, int taskPosition, int flag) {
            this(position, taskPosition, flag, LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
        }

        public SnapTarget(int position, int taskPosition, int flag, float distanceMultiplier) {
            this.position = position;
            this.taskPosition = taskPosition;
            this.flag = flag;
            this.distanceMultiplier = distanceMultiplier;
        }
    }

    public static DividerSnapAlgorithm create(Context ctx, Rect insets) {
        boolean z = true;
        DisplayInfo displayInfo = new DisplayInfo();
        ((DisplayManager) ctx.getSystemService(DisplayManager.class)).getDisplay(SNAP_MODE_16_9).getDisplayInfo(displayInfo);
        int dividerWindowWidth = ctx.getResources().getDimensionPixelSize(R.dimen.docked_stack_divider_thickness);
        int dividerInsets = ctx.getResources().getDimensionPixelSize(R.dimen.docked_stack_divider_insets);
        Resources resources = ctx.getResources();
        int i = displayInfo.logicalWidth;
        int i2 = displayInfo.logicalHeight;
        int i3 = dividerWindowWidth - (dividerInsets * SNAP_ONLY_1_1);
        if (ctx.getApplicationContext().getResources().getConfiguration().orientation != SNAP_FIXED_RATIO) {
            z = false;
        }
        return new DividerSnapAlgorithm(resources, i, i2, i3, z, insets);
    }

    public DividerSnapAlgorithm(Resources res, int displayWidth, int displayHeight, int dividerSize, boolean isHorizontalDivision, Rect insets) {
        this.mTargets = new ArrayList();
        this.mInsets = new Rect();
        this.mMinFlingVelocityPxPerSecond = res.getDisplayMetrics().density * 400.0f;
        this.mMinDismissVelocityPxPerSecond = res.getDisplayMetrics().density * 600.0f;
        this.mDividerSize = dividerSize;
        this.mDisplayWidth = displayWidth;
        this.mDisplayHeight = displayHeight;
        this.mIsHorizontalDivision = isHorizontalDivision;
        this.mInsets.set(insets);
        int navigationBarHeight = getNavigationBarHeight(res);
        if (navigationBarHeight > 0) {
            if (this.mIsHorizontalDivision) {
                this.mInsets.bottom = navigationBarHeight;
            } else {
                this.mInsets.right = navigationBarHeight;
            }
        }
        this.mSnapMode = res.getInteger(R.integer.config_dockedStackDividerSnapMode);
        this.mFixedRatio = res.getFraction(R.fraction.docked_stack_divider_fixed_ratio, SNAP_FIXED_RATIO, SNAP_FIXED_RATIO);
        this.mMinimalSizeResizableTask = SNAP_MODE_16_9;
        calculateTargets(isHorizontalDivision);
        this.mFirstSplitTarget = (SnapTarget) this.mTargets.get(SNAP_FIXED_RATIO);
        this.mLastSplitTarget = (SnapTarget) this.mTargets.get(this.mTargets.size() - 2);
        this.mDismissStartTarget = (SnapTarget) this.mTargets.get(SNAP_MODE_16_9);
        this.mDismissEndTarget = (SnapTarget) this.mTargets.get(this.mTargets.size() - 1);
        this.mMiddleTarget = (SnapTarget) this.mTargets.get(this.mTargets.size() / SNAP_ONLY_1_1);
    }

    public boolean isSplitScreenFeasible() {
        int size;
        int statusBarSize = this.mInsets.top;
        int navBarSize = this.mIsHorizontalDivision ? this.mInsets.bottom : this.mInsets.right;
        if (this.mIsHorizontalDivision) {
            size = this.mDisplayHeight;
        } else {
            size = this.mDisplayWidth;
        }
        if ((((size - navBarSize) - statusBarSize) - this.mDividerSize) / SNAP_ONLY_1_1 >= this.mMinimalSizeResizableTask) {
            return true;
        }
        return false;
    }

    public SnapTarget calculateSnapTarget(int position, float velocity) {
        return calculateSnapTarget(position, velocity, true);
    }

    public SnapTarget calculateSnapTarget(int position, float velocity, boolean hardDismiss) {
        if (position < this.mFirstSplitTarget.position && velocity < (-this.mMinDismissVelocityPxPerSecond)) {
            return this.mDismissStartTarget;
        }
        if (position > this.mLastSplitTarget.position && velocity > this.mMinDismissVelocityPxPerSecond) {
            return this.mDismissEndTarget;
        }
        if (Math.abs(velocity) < this.mMinFlingVelocityPxPerSecond) {
            return snap(position, hardDismiss);
        }
        if (velocity < 0.0f) {
            return this.mFirstSplitTarget;
        }
        return this.mLastSplitTarget;
    }

    public SnapTarget calculateNonDismissingSnapTarget(int position) {
        SnapTarget target = snap(position, false);
        if (target == this.mDismissStartTarget) {
            return this.mFirstSplitTarget;
        }
        if (target == this.mDismissEndTarget) {
            return this.mLastSplitTarget;
        }
        return target;
    }

    public float calculateDismissingFraction(int position) {
        if (position < this.mFirstSplitTarget.position) {
            return LayoutParams.BRIGHTNESS_OVERRIDE_FULL - (((float) (position - getStartInset())) / ((float) (this.mFirstSplitTarget.position - getStartInset())));
        }
        if (position > this.mLastSplitTarget.position) {
            return ((float) (position - this.mLastSplitTarget.position)) / ((float) ((this.mDismissEndTarget.position - this.mLastSplitTarget.position) - this.mDividerSize));
        }
        return 0.0f;
    }

    public SnapTarget getClosestDismissTarget(int position) {
        if (position < this.mFirstSplitTarget.position) {
            return this.mDismissStartTarget;
        }
        if (position > this.mLastSplitTarget.position) {
            return this.mDismissEndTarget;
        }
        if (position - this.mDismissStartTarget.position < this.mDismissEndTarget.position - position) {
            return this.mDismissStartTarget;
        }
        return this.mDismissEndTarget;
    }

    public SnapTarget getFirstSplitTarget() {
        return this.mFirstSplitTarget;
    }

    public SnapTarget getLastSplitTarget() {
        return this.mLastSplitTarget;
    }

    public SnapTarget getDismissStartTarget() {
        return this.mDismissStartTarget;
    }

    public SnapTarget getDismissEndTarget() {
        return this.mDismissEndTarget;
    }

    private int getStartInset() {
        if (this.mIsHorizontalDivision) {
            return this.mInsets.top;
        }
        return this.mInsets.left;
    }

    private int getEndInset() {
        if (this.mIsHorizontalDivision) {
            return this.mInsets.bottom;
        }
        return this.mInsets.right;
    }

    private SnapTarget snap(int position, boolean hardDismiss) {
        int minIndex = -1;
        float minDistance = AutoScrollHelper.NO_MAX;
        int size = this.mTargets.size();
        for (int i = SNAP_MODE_16_9; i < size; i += SNAP_FIXED_RATIO) {
            SnapTarget target = (SnapTarget) this.mTargets.get(i);
            float distance = (float) Math.abs(position - target.position);
            if (hardDismiss) {
                distance /= target.distanceMultiplier;
            }
            if (distance < minDistance) {
                minIndex = i;
                minDistance = distance;
            }
        }
        return (SnapTarget) this.mTargets.get(minIndex);
    }

    private void calculateTargets(boolean isHorizontalDivision) {
        int dividerMax;
        this.mTargets.clear();
        if (isHorizontalDivision) {
            dividerMax = this.mDisplayHeight;
        } else {
            dividerMax = this.mDisplayWidth;
        }
        this.mTargets.add(new SnapTarget(-this.mDividerSize, -this.mDividerSize, SNAP_FIXED_RATIO, 0.35f));
        switch (this.mSnapMode) {
            case SNAP_MODE_16_9 /*0*/:
                addRatio16_9Targets(isHorizontalDivision, dividerMax);
                break;
            case SNAP_FIXED_RATIO /*1*/:
                addFixedDivisionTargets(isHorizontalDivision, dividerMax);
                break;
            case SNAP_ONLY_1_1 /*2*/:
                addMiddleTarget(isHorizontalDivision);
                break;
        }
        this.mTargets.add(new SnapTarget(dividerMax - (isHorizontalDivision ? this.mInsets.bottom : this.mInsets.right), dividerMax, SNAP_ONLY_1_1, 0.35f));
    }

    private void addNonDismissingTargets(boolean isHorizontalDivision, int topPosition, int bottomPosition, int dividerMax) {
        maybeAddTarget(topPosition, topPosition - this.mInsets.top);
        addMiddleTarget(isHorizontalDivision);
        maybeAddTarget(bottomPosition, (dividerMax - this.mInsets.bottom) - (this.mDividerSize + bottomPosition));
    }

    private void addFixedDivisionTargets(boolean isHorizontalDivision, int dividerMax) {
        int end;
        int start = isHorizontalDivision ? this.mInsets.top : this.mInsets.left;
        if (isHorizontalDivision) {
            end = this.mDisplayHeight - this.mInsets.bottom;
        } else {
            end = this.mDisplayWidth - this.mInsets.right;
        }
        int size = ((int) (this.mFixedRatio * ((float) (end - start)))) - (this.mDividerSize / SNAP_ONLY_1_1);
        addNonDismissingTargets(isHorizontalDivision, start + size, (end - size) - this.mDividerSize, dividerMax);
    }

    private void addRatio16_9Targets(boolean isHorizontalDivision, int dividerMax) {
        int end;
        int endOther;
        int start = isHorizontalDivision ? this.mInsets.top : this.mInsets.left;
        if (isHorizontalDivision) {
            end = this.mDisplayHeight - this.mInsets.bottom;
        } else {
            end = this.mDisplayWidth - this.mInsets.right;
        }
        int startOther = isHorizontalDivision ? this.mInsets.left : this.mInsets.top;
        if (isHorizontalDivision) {
            endOther = this.mDisplayWidth - this.mInsets.right;
        } else {
            endOther = this.mDisplayHeight - this.mInsets.bottom;
        }
        int sizeInt = (int) Math.floor((double) (0.5625f * ((float) (endOther - startOther))));
        addNonDismissingTargets(isHorizontalDivision, start + sizeInt, (end - sizeInt) - this.mDividerSize, dividerMax);
    }

    private void maybeAddTarget(int position, int smallerSize) {
        if (smallerSize >= this.mMinimalSizeResizableTask) {
            this.mTargets.add(new SnapTarget(position, position, SNAP_MODE_16_9));
        }
    }

    private void addMiddleTarget(boolean isHorizontalDivision) {
        int position = DockedDividerUtils.calculateMiddlePosition(isHorizontalDivision, this.mInsets, this.mDisplayWidth, this.mDisplayHeight, this.mDividerSize);
        this.mTargets.add(new SnapTarget(position, position, SNAP_MODE_16_9));
    }

    public SnapTarget getMiddleTarget() {
        return this.mMiddleTarget;
    }

    public SnapTarget getNextTarget(SnapTarget snapTarget) {
        int index = this.mTargets.indexOf(snapTarget);
        if (index == -1 || index >= this.mTargets.size() - 1) {
            return snapTarget;
        }
        return (SnapTarget) this.mTargets.get(index + SNAP_FIXED_RATIO);
    }

    public SnapTarget getPreviousTarget(SnapTarget snapTarget) {
        int index = this.mTargets.indexOf(snapTarget);
        if (index == -1 || index <= 0) {
            return snapTarget;
        }
        return (SnapTarget) this.mTargets.get(index - 1);
    }

    public boolean isFirstSplitTargetAvailable() {
        return this.mFirstSplitTarget != this.mMiddleTarget;
    }

    public boolean isLastSplitTargetAvailable() {
        return this.mLastSplitTarget != this.mMiddleTarget;
    }

    public SnapTarget cycleNonDismissTarget(SnapTarget snapTarget, int increment) {
        int index = this.mTargets.indexOf(snapTarget);
        if (index == -1) {
            return snapTarget;
        }
        SnapTarget newTarget = (SnapTarget) this.mTargets.get(((this.mTargets.size() + index) + increment) % this.mTargets.size());
        if (newTarget == this.mDismissStartTarget) {
            return this.mLastSplitTarget;
        }
        if (newTarget == this.mDismissEndTarget) {
            return this.mFirstSplitTarget;
        }
        return newTarget;
    }

    private int getNavigationBarHeight(Resources res) {
        if (res == null) {
            return SNAP_MODE_16_9;
        }
        int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return res.getDimensionPixelSize(resourceId);
        }
        return SNAP_MODE_16_9;
    }
}
