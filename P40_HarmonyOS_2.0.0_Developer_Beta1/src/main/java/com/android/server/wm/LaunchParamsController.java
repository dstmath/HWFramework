package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.WindowConfiguration;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public class LaunchParamsController {
    private final List<LaunchParamsModifier> mModifiers = new ArrayList();
    private final LaunchParamsPersister mPersister;
    private final ActivityTaskManagerService mService;
    private final LaunchParams mTmpCurrent = new LaunchParams();
    private final LaunchParams mTmpParams = new LaunchParams();
    private final LaunchParams mTmpResult = new LaunchParams();

    /* access modifiers changed from: package-private */
    public interface LaunchParamsModifier {
        public static final int PHASE_BOUNDS = 2;
        public static final int PHASE_DISPLAY = 0;
        public static final int PHASE_WINDOWING_MODE = 1;
        public static final int RESULT_CONTINUE = 2;
        public static final int RESULT_DONE = 1;
        public static final int RESULT_SKIP = 0;

        @Retention(RetentionPolicy.SOURCE)
        public @interface Phase {
        }

        @Retention(RetentionPolicy.SOURCE)
        public @interface Result {
        }

        int onCalculate(TaskRecord taskRecord, ActivityInfo.WindowLayout windowLayout, ActivityRecord activityRecord, ActivityRecord activityRecord2, ActivityOptions activityOptions, int i, LaunchParams launchParams, LaunchParams launchParams2);
    }

    LaunchParamsController(ActivityTaskManagerService service, LaunchParamsPersister persister) {
        this.mService = service;
        this.mPersister = persister;
    }

    /* access modifiers changed from: package-private */
    public void registerDefaultModifiers(ActivityStackSupervisor supervisor) {
        registerModifier(new TaskLaunchParamsModifier(supervisor));
    }

    /* access modifiers changed from: package-private */
    public void calculate(TaskRecord task, ActivityInfo.WindowLayout layout, ActivityRecord activity, ActivityRecord source, ActivityOptions options, int phase, LaunchParams result) {
        result.reset();
        if (!(task == null && activity == null) && (options == null || !WindowConfiguration.isHwMultiStackWindowingMode(options.getLaunchWindowingMode()))) {
            this.mPersister.getLaunchParams(task, activity, result);
        }
        for (int i = this.mModifiers.size() - 1; i >= 0; i--) {
            this.mTmpCurrent.set(result);
            this.mTmpResult.reset();
            int onCalculate = this.mModifiers.get(i).onCalculate(task, layout, activity, source, options, phase, this.mTmpCurrent, this.mTmpResult);
            if (onCalculate != 0) {
                if (onCalculate == 1) {
                    result.set(this.mTmpResult);
                    return;
                } else if (onCalculate == 2) {
                    result.set(this.mTmpResult);
                }
            }
        }
        if (activity != null && activity.requestedVrComponent != null) {
            result.mPreferredDisplayId = 0;
        } else if (this.mService.mVr2dDisplayId != -1) {
            result.mPreferredDisplayId = this.mService.mVr2dDisplayId;
        }
        result.mPreferredDisplayId = this.mService.mHwATMSEx.getPreferedDisplayId(activity, options, result.mPreferredDisplayId);
    }

    /* access modifiers changed from: package-private */
    public boolean layoutTask(TaskRecord task, ActivityInfo.WindowLayout layout) {
        return layoutTask(task, layout, null, null, null);
    }

    /* access modifiers changed from: package-private */
    public boolean layoutTask(TaskRecord task, ActivityInfo.WindowLayout layout, ActivityRecord activity, ActivityRecord source, ActivityOptions options) {
        calculate(task, layout, activity, source, options, 2, this.mTmpParams);
        if (this.mTmpParams.isEmpty()) {
            return false;
        }
        this.mService.mWindowManager.deferSurfaceLayout();
        try {
            if (this.mTmpParams.hasPreferredDisplay() && this.mTmpParams.mPreferredDisplayId != task.getStack().getDisplay().mDisplayId) {
                this.mService.moveStackToDisplay(task.getStackId(), this.mTmpParams.mPreferredDisplayId);
            }
            if (this.mTmpParams.hasWindowingMode() && this.mTmpParams.mWindowingMode != task.getStack().getWindowingMode()) {
                task.getStack().setWindowingMode(this.mTmpParams.mWindowingMode);
            }
            if (this.mTmpParams.mBounds.isEmpty()) {
                return false;
            }
            if (task.getStack().inFreeformWindowingMode()) {
                task.updateOverrideConfiguration(this.mTmpParams.mBounds);
                this.mService.mWindowManager.continueSurfaceLayout();
                return true;
            }
            task.setLastNonFullscreenBounds(this.mTmpParams.mBounds);
            this.mService.mWindowManager.continueSurfaceLayout();
            return false;
        } finally {
            this.mService.mWindowManager.continueSurfaceLayout();
        }
    }

    /* access modifiers changed from: package-private */
    public void registerModifier(LaunchParamsModifier modifier) {
        if (!this.mModifiers.contains(modifier)) {
            this.mModifiers.add(modifier);
        }
    }

    /* access modifiers changed from: package-private */
    public static class LaunchParams {
        final Rect mBounds = new Rect();
        int mPreferredDisplayId;
        int mWindowingMode;

        LaunchParams() {
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.mBounds.setEmpty();
            this.mPreferredDisplayId = -1;
            this.mWindowingMode = 0;
        }

        /* access modifiers changed from: package-private */
        public void set(LaunchParams params) {
            this.mBounds.set(params.mBounds);
            this.mPreferredDisplayId = params.mPreferredDisplayId;
            this.mWindowingMode = params.mWindowingMode;
        }

        /* access modifiers changed from: package-private */
        public boolean isEmpty() {
            return this.mBounds.isEmpty() && this.mPreferredDisplayId == -1 && this.mWindowingMode == 0;
        }

        /* access modifiers changed from: package-private */
        public boolean hasWindowingMode() {
            return this.mWindowingMode != 0;
        }

        /* access modifiers changed from: package-private */
        public boolean hasPreferredDisplay() {
            return this.mPreferredDisplayId != -1;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LaunchParams that = (LaunchParams) o;
            if (this.mPreferredDisplayId != that.mPreferredDisplayId || this.mWindowingMode != that.mWindowingMode) {
                return false;
            }
            Rect rect = this.mBounds;
            if (rect != null) {
                return rect.equals(that.mBounds);
            }
            if (that.mBounds == null) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            Rect rect = this.mBounds;
            return ((((rect != null ? rect.hashCode() : 0) * 31) + this.mPreferredDisplayId) * 31) + this.mWindowingMode;
        }
    }
}
