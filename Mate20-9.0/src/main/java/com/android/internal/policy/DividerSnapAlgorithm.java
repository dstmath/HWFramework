package com.android.internal.policy;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.view.DisplayInfo;
import com.huawei.android.fsm.HwFoldScreenManager;
import java.util.ArrayList;

public class DividerSnapAlgorithm {
    private static final int MIN_DISMISS_VELOCITY_DP_PER_SECOND = 600;
    private static final int MIN_FLING_VELOCITY_DP_PER_SECOND = 400;
    private static final int SNAP_FIXED_RATIO = 1;
    private static final int SNAP_MODE_16_9 = 0;
    private static final int SNAP_MODE_MINIMIZED = 3;
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
    private final int mTaskHeightInMinimizedMode;

    public static class SnapTarget {
        public static final int FLAG_DISMISS_END = 2;
        public static final int FLAG_DISMISS_START = 1;
        public static final int FLAG_NONE = 0;
        /* access modifiers changed from: private */
        public final float distanceMultiplier;
        public final int flag;
        public final int position;
        public final int taskPosition;

        public SnapTarget(int position2, int taskPosition2, int flag2) {
            this(position2, taskPosition2, flag2, 1.0f);
        }

        public SnapTarget(int position2, int taskPosition2, int flag2, float distanceMultiplier2) {
            this.position = position2;
            this.taskPosition = taskPosition2;
            this.flag = flag2;
            this.distanceMultiplier = distanceMultiplier2;
        }
    }

    public static DividerSnapAlgorithm create(Context ctx, Rect insets) {
        DisplayInfo displayInfo = new DisplayInfo();
        ((DisplayManager) ctx.getSystemService(DisplayManager.class)).getDisplay(0).getDisplayInfo(displayInfo);
        int dividerWindowWidth = ctx.getResources().getDimensionPixelSize(17105035);
        int dividerInsets = ctx.getResources().getDimensionPixelSize(17105034);
        Resources resources = ctx.getResources();
        int i = displayInfo.logicalWidth;
        int i2 = displayInfo.logicalHeight;
        int i3 = dividerWindowWidth - (2 * dividerInsets);
        boolean z = true;
        if (ctx.getApplicationContext().getResources().getConfiguration().orientation != 1) {
            z = false;
        }
        DividerSnapAlgorithm dividerSnapAlgorithm = new DividerSnapAlgorithm(resources, i, i2, i3, z, insets);
        return dividerSnapAlgorithm;
    }

    public DividerSnapAlgorithm(Resources res, int displayWidth, int displayHeight, int dividerSize, boolean isHorizontalDivision, Rect insets) {
        this(res, displayWidth, displayHeight, dividerSize, isHorizontalDivision, insets, -1, false);
    }

    public DividerSnapAlgorithm(Resources res, int displayWidth, int displayHeight, int dividerSize, boolean isHorizontalDivision, Rect insets, int dockSide) {
        this(res, displayWidth, displayHeight, dividerSize, isHorizontalDivision, insets, dockSide, false);
    }

    public DividerSnapAlgorithm(Resources res, int displayWidth, int displayHeight, int dividerSize, boolean isHorizontalDivision, Rect insets, int dockSide, boolean isMinimizedMode) {
        int i;
        this.mTargets = new ArrayList<>();
        this.mInsets = new Rect();
        this.mMinFlingVelocityPxPerSecond = 400.0f * res.getDisplayMetrics().density;
        this.mMinDismissVelocityPxPerSecond = 600.0f * res.getDisplayMetrics().density;
        this.mDividerSize = dividerSize;
        this.mDisplayWidth = displayWidth;
        this.mDisplayHeight = displayHeight;
        this.mIsHorizontalDivision = isHorizontalDivision;
        this.mInsets.set(insets);
        if (isMinimizedMode) {
            i = 3;
        } else {
            i = !isInFoldFullDisplayMode() ? res.getInteger(17694777) : 2;
        }
        this.mSnapMode = i;
        this.mFixedRatio = res.getFraction(18022404, 1, 1);
        this.mMinimalSizeResizableTask = 0;
        this.mTaskHeightInMinimizedMode = res.getDimensionPixelSize(17105326);
        calculateTargets(isHorizontalDivision, dockSide);
        this.mFirstSplitTarget = this.mTargets.get(1);
        this.mLastSplitTarget = this.mTargets.get(this.mTargets.size() - 2);
        this.mDismissStartTarget = this.mTargets.get(0);
        this.mDismissEndTarget = this.mTargets.get(this.mTargets.size() - 1);
        this.mMiddleTarget = this.mTargets.get(this.mTargets.size() / 2);
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
        return (((size - navBarSize) - statusBarSize) - this.mDividerSize) / 2 >= this.mMinimalSizeResizableTask;
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
            return 1.0f - (((float) (position - getStartInset())) / ((float) (this.mFirstSplitTarget.position - getStartInset())));
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
        float minDistance = Float.MAX_VALUE;
        int size = this.mTargets.size();
        for (int i = 0; i < size; i++) {
            SnapTarget target = this.mTargets.get(i);
            float distance = (float) Math.abs(position - target.position);
            if (hardDismiss) {
                distance /= target.distanceMultiplier;
            }
            if (distance < minDistance) {
                minIndex = i;
                minDistance = distance;
            }
        }
        return this.mTargets.get(minIndex);
    }

    private void calculateTargets(boolean isHorizontalDivision, int dockedSide) {
        int dividerMax;
        this.mTargets.clear();
        if (isHorizontalDivision) {
            dividerMax = this.mDisplayHeight;
        } else {
            dividerMax = this.mDisplayWidth;
        }
        int navBarSize = isHorizontalDivision ? this.mInsets.bottom : this.mInsets.right;
        int startPos = -this.mDividerSize;
        if (dockedSide == 3) {
            startPos += this.mInsets.left;
        }
        this.mTargets.add(new SnapTarget(startPos, startPos, 1, 0.35f));
        switch (this.mSnapMode) {
            case 0:
                addRatio16_9Targets(isHorizontalDivision, dividerMax);
                break;
            case 1:
                addFixedDivisionTargets(isHorizontalDivision, dividerMax);
                break;
            case 2:
                addMiddleTarget(isHorizontalDivision);
                break;
            case 3:
                addMinimizedTarget(isHorizontalDivision, dockedSide);
                break;
        }
        this.mTargets.add(new SnapTarget(dividerMax - navBarSize, dividerMax, 2, 0.35f));
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
        int size = ((int) (this.mFixedRatio * ((float) (end - start)))) - (this.mDividerSize / 2);
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
            this.mTargets.add(new SnapTarget(position, position, 0));
        }
    }

    private void addMiddleTarget(boolean isHorizontalDivision) {
        if (isInFoldFullDisplayMode()) {
            isHorizontalDivision = false;
        }
        int position = DockedDividerUtils.calculateMiddlePosition(isHorizontalDivision, this.mInsets, this.mDisplayWidth, this.mDisplayHeight, this.mDividerSize);
        this.mTargets.add(new SnapTarget(position, position, 0));
    }

    private void addMinimizedTarget(boolean isHorizontalDivision, int dockedSide) {
        int position = this.mTaskHeightInMinimizedMode + this.mInsets.top;
        if (!isHorizontalDivision) {
            if (dockedSide == 1) {
                position += this.mInsets.left;
            } else if (dockedSide == 3) {
                position = ((this.mDisplayWidth - position) - this.mInsets.right) - this.mDividerSize;
            }
        }
        this.mTargets.add(new SnapTarget(position, position, 0));
    }

    public SnapTarget getMiddleTarget() {
        return this.mMiddleTarget;
    }

    public SnapTarget getNextTarget(SnapTarget snapTarget) {
        int index = this.mTargets.indexOf(snapTarget);
        if (index == -1 || index >= this.mTargets.size() - 1) {
            return snapTarget;
        }
        return this.mTargets.get(index + 1);
    }

    public SnapTarget getPreviousTarget(SnapTarget snapTarget) {
        int index = this.mTargets.indexOf(snapTarget);
        if (index == -1 || index <= 0) {
            return snapTarget;
        }
        return this.mTargets.get(index - 1);
    }

    public boolean showMiddleSplitTargetForAccessibility() {
        return this.mTargets.size() + -2 > 1;
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
        SnapTarget newTarget = this.mTargets.get(((this.mTargets.size() + index) + increment) % this.mTargets.size());
        if (newTarget == this.mDismissStartTarget) {
            return this.mLastSplitTarget;
        }
        if (newTarget == this.mDismissEndTarget) {
            return this.mFirstSplitTarget;
        }
        return newTarget;
    }

    private boolean isInFoldFullDisplayMode() {
        if (HwFoldScreenManager.isFoldable() && HwFoldScreenManager.getDisplayMode() == 1) {
            return true;
        }
        return false;
    }
}
