package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.WindowConfiguration;
import android.content.pm.ActivityInfo;
import android.freeform.HwFreeFormUtils;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Slog;
import android.util.SplitNotificationUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.HwServiceExFactory;
import com.android.server.wm.LaunchParamsController;
import com.huawei.server.wm.IHwTaskLaunchParamsModifierEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/* access modifiers changed from: package-private */
public class TaskLaunchParamsModifier implements LaunchParamsController.LaunchParamsModifier {
    public static final String APP_LOCK_NAME = "com.huawei.systemmanager/com.huawei.securitycenter.applock.password.AuthLaunchLockedAppActivity";
    public static final String APP_OPAQUE_LOCK_NAME = "com.huawei.systemmanager/com.huawei.securitycenter.applock.password.LockScreenLaunchLockedAppActivity";
    private static final int BOUNDS_CONFLICT_THRESHOLD = 4;
    private static final int CASCADING_OFFSET_DP = 75;
    private static final boolean DEBUG = true;
    private static final int DEFAULT_PORTRAIT_PHONE_HEIGHT_DP = 732;
    private static final int DEFAULT_PORTRAIT_PHONE_WIDTH_DP = 412;
    private static final int EPSILON = 2;
    private static final String FREEFORM_START_HEIGHT = "sDefaultFreeformHeight";
    private static final String FREEFORM_START_WIDTH = "sDefaultFreeformWidth";
    private static final String FREEFORM_START_X = "sDefaultFreeformStartX";
    private static final String FREEFORM_START_Y = "sDefaultFreeformStartY";
    public static final String HW_CHOOSER_ACTIVITY = "com.huawei.android.internal.app/.HwChooserActivity";
    private static final int MINIMAL_STEP = 1;
    private static final int STEP_DENOMINATOR = 16;
    private static final int SUPPORTS_SCREEN_RESIZEABLE_MASK = 539136;
    private static final String TAG = "ActivityTaskManager";
    private static ActivityDisplay mActivityDisplay = null;
    private static HashMap<String, Integer> mDefaultFreeformMaps = new HashMap<>();
    private static IHwTaskLaunchParamsModifierEx mHwTaskLaunchParamsModifierEx = HwServiceExFactory.getHwTaskLaunchParamsModifierEx();
    private StringBuilder mLogBuilder;
    private final ActivityStackSupervisor mSupervisor;
    private final Rect mTmpBounds = new Rect();
    private final int[] mTmpDirections = new int[2];

    TaskLaunchParamsModifier(ActivityStackSupervisor supervisor) {
        this.mSupervisor = supervisor;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int onCalculate(TaskRecord task, ActivityInfo.WindowLayout layout, ActivityRecord activity, ActivityRecord source, ActivityOptions options, LaunchParamsController.LaunchParams currentParams, LaunchParamsController.LaunchParams outParams) {
        return onCalculate(task, layout, activity, source, options, 2, currentParams, outParams);
    }

    @Override // com.android.server.wm.LaunchParamsController.LaunchParamsModifier
    public int onCalculate(TaskRecord task, ActivityInfo.WindowLayout layout, ActivityRecord activity, ActivityRecord source, ActivityOptions options, int phase, LaunchParamsController.LaunchParams currentParams, LaunchParamsController.LaunchParams outParams) {
        initLogBuilder(task, activity);
        int result = calculate(task, layout, activity, source, options, phase, currentParams, outParams);
        outputLog();
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:102:0x01ec A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x01ef  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x01bc  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x01d8  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x01e4  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x01e6  */
    private int calculate(TaskRecord task, ActivityInfo.WindowLayout layout, ActivityRecord activity, ActivityRecord source, ActivityOptions options, int phase, LaunchParamsController.LaunchParams currentParams, LaunchParamsController.LaunchParams outParams) {
        ActivityRecord root;
        int launchMode;
        boolean hasInitialBounds;
        boolean fullyResolvedCurrentParam;
        int launchMode2;
        int resolvedMode;
        int resolvedMode2;
        int i;
        int i2;
        if (task != null) {
            root = task.getRootActivity() == null ? activity : task.getRootActivity();
        } else {
            root = activity;
        }
        if (root == null) {
            return 0;
        }
        int displayId = getPreferredLaunchDisplay(task, options, source, currentParams);
        outParams.mPreferredDisplayId = displayId;
        ActivityDisplay display = this.mSupervisor.mRootActivityContainer.getActivityDisplay(displayId);
        appendLog("display-id=" + outParams.mPreferredDisplayId + " display-windowing-mode=" + display.getWindowingMode());
        if (phase == 0) {
            return 2;
        }
        if (options != null) {
            launchMode = options.getLaunchWindowingMode();
        } else {
            launchMode = 0;
        }
        if (launchMode == 0 && canInheritWindowingModeFromSource(display, source)) {
            launchMode = source.getWindowingMode();
        }
        boolean canApplyFreeformPolicy = canApplyFreeformWindowPolicy(display, launchMode);
        if (!this.mSupervisor.canUseActivityOptionsLaunchBounds(options) || (!canApplyFreeformPolicy && !canApplyPipWindowPolicy(launchMode))) {
            if (launchMode == 2) {
                appendLog("empty-window-layout-for-pip");
            } else if (launchMode == 1) {
                appendLog("activity-options-fullscreen=" + outParams.mBounds);
            } else if (layout != null && !WindowConfiguration.isHwMultiStackWindowingMode(launchMode) && canApplyFreeformPolicy) {
                getLayoutBounds(display, root, layout, this.mTmpBounds);
                if (!this.mTmpBounds.isEmpty()) {
                    launchMode = 5;
                    outParams.mBounds.set(this.mTmpBounds);
                    appendLog("bounds-from-layout=" + outParams.mBounds);
                    hasInitialBounds = true;
                } else {
                    appendLog("empty-window-layout");
                }
            }
            hasInitialBounds = false;
        } else {
            if (launchMode == 0) {
                i2 = 5;
            } else {
                i2 = launchMode;
            }
            launchMode = i2;
            outParams.mBounds.set(options.getLaunchBounds());
            appendLog("activity-options-bounds=" + outParams.mBounds);
            hasInitialBounds = true;
        }
        boolean fullyResolvedCurrentParam2 = false;
        if (!currentParams.isEmpty() && !hasInitialBounds && (!currentParams.hasPreferredDisplay() || displayId == currentParams.mPreferredDisplayId)) {
            if (currentParams.hasWindowingMode()) {
                launchMode = currentParams.mWindowingMode;
                if (!(options == null || activity == null)) {
                    if (HwFreeFormUtils.isFreeFormEnable() && currentParams.mWindowingMode == 1 && options.getLaunchWindowingMode() == 5 && !APP_LOCK_NAME.equals(activity.shortComponentName) && !APP_OPAQUE_LOCK_NAME.equals(activity.shortComponentName)) {
                        launchMode = 5;
                    }
                }
                fullyResolvedCurrentParam2 = launchMode != 5;
                appendLog("inherit-" + WindowConfiguration.windowingModeToString(launchMode));
            }
            if (!currentParams.mBounds.isEmpty()) {
                outParams.mBounds.set(currentParams.mBounds);
                if (launchMode == 5) {
                    if (HwFreeFormUtils.isFreeFormEnable()) {
                        adjustBoundsToFitInDisplay(display, outParams.mBounds);
                    }
                    appendLog("inherit-bounds=" + outParams.mBounds);
                    fullyResolvedCurrentParam = true;
                    if (display.inFreeformWindowingMode()) {
                        appendLog("non-freeform-display");
                    } else if (launchMode == 2) {
                        appendLog("picture-in-picture");
                    } else if (isTaskForcedMaximized(root)) {
                        outParams.mBounds.setEmpty();
                        appendLog("forced-maximize");
                        launchMode2 = 1;
                        outParams.mWindowingMode = launchMode2 == display.getWindowingMode() ? 0 : launchMode2;
                        if (phase == 1) {
                            return 2;
                        }
                        if (launchMode2 != 0) {
                            resolvedMode = launchMode2;
                        } else {
                            resolvedMode = display.getWindowingMode();
                        }
                        if (task == null || !task.mIsReparenting) {
                            resolvedMode2 = resolvedMode;
                        } else {
                            resolvedMode2 = 5;
                        }
                        if (!fullyResolvedCurrentParam) {
                            if (source != null && source.inFreeformWindowingMode() && resolvedMode2 == 5 && outParams.mBounds.isEmpty() && source.getDisplayId() == display.mDisplayId) {
                                cascadeBounds(source.getBounds(), display, outParams.mBounds);
                            }
                            i = 2;
                            getTaskBounds(root, display, layout, resolvedMode2, hasInitialBounds, outParams.mBounds);
                        } else if (resolvedMode2 == 5) {
                            if (currentParams.mPreferredDisplayId != displayId) {
                                adjustBoundsToFitInDisplay(display, outParams.mBounds);
                            }
                            adjustBoundsToAvoidConflictInDisplay(display, outParams.mBounds);
                            i = 2;
                        } else {
                            i = 2;
                        }
                        Rect calcBound = this.mSupervisor.mService.mHwATMSEx.checkBoundInheritFromSource(source, task);
                        if (!calcBound.isEmpty()) {
                            outParams.mBounds.set(calcBound);
                        }
                        return i;
                    }
                    launchMode2 = launchMode;
                    outParams.mWindowingMode = launchMode2 == display.getWindowingMode() ? 0 : launchMode2;
                    if (phase == 1) {
                    }
                }
            }
        }
        fullyResolvedCurrentParam = fullyResolvedCurrentParam2;
        if (display.inFreeformWindowingMode()) {
        }
        launchMode2 = launchMode;
        outParams.mWindowingMode = launchMode2 == display.getWindowingMode() ? 0 : launchMode2;
        if (phase == 1) {
        }
    }

    private boolean canInheritWindowingModeFromSource(ActivityDisplay display, ActivityRecord source) {
        if (source == null || display.inFreeformWindowingMode()) {
            return false;
        }
        int sourceWindowingMode = source.getWindowingMode();
        if (sourceWindowingMode != 1 && sourceWindowingMode != 5) {
            return false;
        }
        if ((sourceWindowingMode != 5 || !SplitNotificationUtils.getInstance(this.mSupervisor.mService.mContext).getListPkgName(3).contains(source.packageName)) && display.mDisplayId == source.getDisplayId()) {
            return true;
        }
        return false;
    }

    private int getPreferredLaunchDisplay(TaskRecord task, ActivityOptions options, ActivityRecord source, LaunchParamsController.LaunchParams currentParams) {
        if (!this.mSupervisor.mService.mSupportsMultiDisplay) {
            return 0;
        }
        int displayId = -1;
        int optionLaunchId = options != null ? options.getLaunchDisplayId() : -1;
        if (optionLaunchId != -1) {
            appendLog("display-from-option=" + optionLaunchId);
            displayId = optionLaunchId;
        }
        if (displayId == -1 && source != null && source.noDisplay) {
            displayId = source.mHandoverLaunchDisplayId;
            appendLog("display-from-no-display-source=" + displayId);
        }
        ActivityStack stack = (displayId != -1 || task == null) ? null : task.getStack();
        if (stack != null) {
            appendLog("display-from-task=" + stack.mDisplayId);
            displayId = stack.mDisplayId;
        }
        if (displayId == -1 && source != null) {
            int sourceDisplayId = source.getDisplayId();
            appendLog("display-from-source=" + sourceDisplayId);
            displayId = sourceDisplayId;
        }
        if (displayId != -1 && this.mSupervisor.mRootActivityContainer.getActivityDisplay(displayId) == null) {
            displayId = currentParams.mPreferredDisplayId;
        }
        int displayId2 = displayId == -1 ? currentParams.mPreferredDisplayId : displayId;
        if (displayId2 == -1 || this.mSupervisor.mRootActivityContainer.getActivityDisplay(displayId2) == null) {
            return 0;
        }
        return displayId2;
    }

    private boolean canApplyFreeformWindowPolicy(ActivityDisplay display, int launchMode) {
        return this.mSupervisor.mService.mSupportsFreeformWindowManagement && (display.inFreeformWindowingMode() || launchMode == 5 || WindowConfiguration.isHwMultiStackWindowingMode(launchMode) || launchMode == 12);
    }

    private boolean canApplyPipWindowPolicy(int launchMode) {
        return this.mSupervisor.mService.mSupportsPictureInPicture && launchMode == 2;
    }

    private void getLayoutBounds(ActivityDisplay display, ActivityRecord root, ActivityInfo.WindowLayout windowLayout, Rect outBounds) {
        int height;
        int width;
        float fractionOfHorizontalOffset;
        float fractionOfVerticalOffset;
        int verticalGravity = windowLayout.gravity & 112;
        int horizontalGravity = windowLayout.gravity & 7;
        if (!windowLayout.hasSpecifiedSize() && verticalGravity == 0 && horizontalGravity == 0) {
            outBounds.setEmpty();
            return;
        }
        Rect bounds = display.getBounds();
        int defaultWidth = bounds.width();
        int defaultHeight = bounds.height();
        if (!windowLayout.hasSpecifiedSize()) {
            outBounds.setEmpty();
            getTaskBounds(root, display, windowLayout, 5, false, outBounds);
            width = outBounds.width();
            height = outBounds.height();
        } else {
            width = defaultWidth;
            if (windowLayout.width > 0 && windowLayout.width < defaultWidth) {
                width = windowLayout.width;
            } else if (windowLayout.widthFraction > 0.0f && windowLayout.widthFraction < 1.0f) {
                width = (int) (((float) width) * windowLayout.widthFraction);
            }
            height = defaultHeight;
            if (windowLayout.height > 0 && windowLayout.height < defaultHeight) {
                height = windowLayout.height;
            } else if (windowLayout.heightFraction > 0.0f && windowLayout.heightFraction < 1.0f) {
                height = (int) (((float) height) * windowLayout.heightFraction);
            }
        }
        if (horizontalGravity == 3) {
            fractionOfHorizontalOffset = 0.0f;
        } else if (horizontalGravity != 5) {
            fractionOfHorizontalOffset = 0.5f;
        } else {
            fractionOfHorizontalOffset = 1.0f;
        }
        if (verticalGravity == 48) {
            fractionOfVerticalOffset = 0.0f;
        } else if (verticalGravity != 80) {
            fractionOfVerticalOffset = 0.5f;
        } else {
            fractionOfVerticalOffset = 1.0f;
        }
        outBounds.set(0, 0, width, height);
        outBounds.offset((int) (((float) (defaultWidth - width)) * fractionOfHorizontalOffset), (int) (((float) (defaultHeight - height)) * fractionOfVerticalOffset));
    }

    private boolean isTaskForcedMaximized(ActivityRecord root) {
        if (root.appInfo.targetSdkVersion < 4 || (root.appInfo.flags & SUPPORTS_SCREEN_RESIZEABLE_MASK) == 0) {
            return true;
        }
        return !root.isResizeable();
    }

    private int resolveOrientation(ActivityRecord activity) {
        int orientation = activity.info.screenOrientation;
        if (orientation != 0) {
            if (orientation != 1) {
                if (orientation != 11) {
                    if (orientation != 12) {
                        if (orientation != 14) {
                            switch (orientation) {
                                case 5:
                                    break;
                                case 6:
                                case 8:
                                    break;
                                case 7:
                                case 9:
                                    break;
                                default:
                                    return -1;
                            }
                        }
                        return 14;
                    }
                }
            }
            appendLog("activity-requested-portrait");
            return 1;
        }
        appendLog("activity-requested-landscape");
        return 0;
    }

    private void cascadeBounds(Rect srcBounds, ActivityDisplay display, Rect outBounds) {
        outBounds.set(srcBounds);
        int defaultOffset = (int) ((75.0f * (((float) display.getConfiguration().densityDpi) / 160.0f)) + 0.5f);
        display.getBounds(this.mTmpBounds);
        outBounds.offset(Math.min(defaultOffset, Math.max(0, this.mTmpBounds.right - srcBounds.right)), Math.min(defaultOffset, Math.max(0, this.mTmpBounds.bottom - srcBounds.bottom)));
    }

    private void getTaskBounds(ActivityRecord root, ActivityDisplay display, ActivityInfo.WindowLayout layout, int resolvedMode, boolean hasInitialBounds, Rect inOutBounds) {
        if (resolvedMode == 1) {
            inOutBounds.setEmpty();
            appendLog("maximized-bounds");
        } else if (resolvedMode != 5) {
            appendLog("skip-bounds-" + WindowConfiguration.windowingModeToString(resolvedMode));
        } else {
            int orientation = resolveOrientation(root, display, inOutBounds);
            if (orientation == 1 || orientation == 0) {
                getDefaultFreeformSize(display, layout, orientation, this.mTmpBounds);
                if (!hasInitialBounds && !sizeMatches(inOutBounds, this.mTmpBounds)) {
                    centerBounds(display, this.mTmpBounds.width(), this.mTmpBounds.height(), inOutBounds);
                    adjustBoundsToFitInDisplay(display, inOutBounds);
                    appendLog("freeform-size-mismatch=" + inOutBounds);
                } else if (orientation == orientationFromBounds(inOutBounds)) {
                    appendLog("freeform-size-orientation-match=" + inOutBounds);
                } else {
                    centerBounds(display, inOutBounds.height(), inOutBounds.width(), inOutBounds);
                    appendLog("freeform-orientation-mismatch=" + inOutBounds);
                }
                adjustBoundsToAvoidConflictInDisplay(display, inOutBounds);
                return;
            }
            throw new IllegalStateException("Orientation must be one of portrait or landscape, but it's " + ActivityInfo.screenOrientationToString(orientation));
        }
    }

    private int convertOrientationToScreenOrientation(int orientation) {
        if (orientation == 1) {
            return 1;
        }
        if (orientation != 2) {
            return -1;
        }
        return 0;
    }

    private int resolveOrientation(ActivityRecord root, ActivityDisplay display, Rect bounds) {
        int i;
        String str;
        int i2;
        String str2;
        int orientation = resolveOrientation(root);
        if (orientation == 14) {
            if (bounds.isEmpty()) {
                i2 = convertOrientationToScreenOrientation(display.getConfiguration().orientation);
            } else {
                i2 = orientationFromBounds(bounds);
            }
            orientation = i2;
            if (bounds.isEmpty()) {
                str2 = "locked-orientation-from-display=" + orientation;
            } else {
                str2 = "locked-orientation-from-bounds=" + bounds;
            }
            appendLog(str2);
        }
        if (orientation == -1) {
            if (bounds.isEmpty()) {
                i = 1;
            } else {
                i = orientationFromBounds(bounds);
            }
            orientation = i;
            if (bounds.isEmpty()) {
                str = "default-portrait";
            } else {
                str = "orientation-from-bounds=" + bounds;
            }
            appendLog(str);
        }
        return orientation;
    }

    private void getDefaultFreeformSize(ActivityDisplay display, ActivityInfo.WindowLayout layout, int orientation, Rect bounds) {
        int defaultWidth;
        int defaultHeight;
        int phoneWidth;
        int phoneHeight;
        Rect displayBounds = display.getBounds();
        int portraitHeight = Math.min(displayBounds.width(), displayBounds.height());
        int portraitWidth = (portraitHeight * portraitHeight) / Math.max(displayBounds.width(), displayBounds.height());
        if (orientation == 0) {
            defaultWidth = portraitHeight;
        } else {
            defaultWidth = portraitWidth;
        }
        if (orientation == 0) {
            defaultHeight = portraitWidth;
        } else {
            defaultHeight = portraitHeight;
        }
        float density = ((float) display.getConfiguration().densityDpi) / 160.0f;
        int phonePortraitWidth = (int) ((412.0f * density) + 0.5f);
        int phonePortraitHeight = (int) ((732.0f * density) + 0.5f);
        if (orientation == 0) {
            phoneWidth = phonePortraitHeight;
        } else {
            phoneWidth = phonePortraitWidth;
        }
        if (orientation == 0) {
            phoneHeight = phonePortraitWidth;
        } else {
            phoneHeight = phonePortraitHeight;
        }
        int layoutMinHeight = -1;
        int layoutMinWidth = layout == null ? -1 : layout.minWidth;
        if (layout != null) {
            layoutMinHeight = layout.minHeight;
        }
        bounds.set(0, 0, Math.min(defaultWidth, Math.max(phoneWidth, layoutMinWidth)), Math.min(defaultHeight, Math.max(phoneHeight, layoutMinHeight)));
    }

    private void centerBounds(ActivityDisplay display, int width, int height, Rect inOutBounds) {
        int top;
        int left;
        if (HwFreeFormUtils.isFreeFormEnable()) {
            mDefaultFreeformMaps = mHwTaskLaunchParamsModifierEx.computeDefaultParaForFreeForm(display, this.mSupervisor.mService.mContext);
            left = mDefaultFreeformMaps.get(FREEFORM_START_X).intValue();
            top = mDefaultFreeformMaps.get(FREEFORM_START_Y).intValue();
            width = mDefaultFreeformMaps.get(FREEFORM_START_WIDTH).intValue();
            height = mDefaultFreeformMaps.get(FREEFORM_START_HEIGHT).intValue();
        } else {
            if (inOutBounds.isEmpty()) {
                display.getBounds(inOutBounds);
            }
            left = inOutBounds.centerX() - (width / 2);
            top = inOutBounds.centerY() - (height / 2);
        }
        inOutBounds.set(left, top, left + width, top + height);
    }

    private void adjustBoundsToFitInDisplay(ActivityDisplay display, Rect inOutBounds) {
        int left;
        int dx;
        int dy;
        Rect displayBounds = display.getBounds();
        if (displayBounds.width() < inOutBounds.width() || displayBounds.height() < inOutBounds.height()) {
            if (this.mSupervisor.mRootActivityContainer.getConfiguration().getLayoutDirection() == 1) {
                left = displayBounds.width() - inOutBounds.width();
            } else {
                left = 0;
            }
            inOutBounds.offsetTo(left, 0);
            return;
        }
        if (inOutBounds.right > displayBounds.right) {
            dx = displayBounds.right - inOutBounds.right;
        } else if (inOutBounds.left < displayBounds.left) {
            dx = displayBounds.left - inOutBounds.left;
        } else {
            dx = 0;
        }
        if (inOutBounds.top < displayBounds.top) {
            dy = displayBounds.top - inOutBounds.top;
        } else if (inOutBounds.bottom > displayBounds.bottom) {
            dy = displayBounds.bottom - inOutBounds.bottom;
        } else {
            dy = 0;
        }
        inOutBounds.offset(dx, dy);
        if (HwFreeFormUtils.isFreeFormEnable()) {
            WindowManagerService windowManagerService = this.mSupervisor.mService.mWindowManager;
            if (!WindowManagerService.IS_TABLET) {
                if (HwFreeFormUtils.getLandscapeFreeformMaxLength() == 0 && display.mDisplay != null) {
                    Point maxDisplaySize = new Point();
                    display.mDisplay.getRealSize(maxDisplaySize);
                    HwFreeFormUtils.computeFreeFormSize(maxDisplaySize);
                }
                int landscapeMaxLength = HwFreeFormUtils.getLandscapeFreeformMaxLength() - this.mSupervisor.mService.mUiContext.getResources().getDimensionPixelSize(17105445);
                if (displayBounds.width() > displayBounds.height() && inOutBounds.height() > landscapeMaxLength) {
                    inOutBounds.set(inOutBounds.left, inOutBounds.top, inOutBounds.left + ((inOutBounds.width() * landscapeMaxLength) / inOutBounds.height()), inOutBounds.top + landscapeMaxLength);
                }
            }
        }
    }

    private void adjustBoundsToAvoidConflictInDisplay(ActivityDisplay display, Rect inOutBounds) {
        List<Rect> taskBoundsToCheck = new ArrayList<>();
        for (int i = 0; i < display.getChildCount(); i++) {
            ActivityStack stack = display.getChildAt(i);
            if (stack.inFreeformWindowingMode()) {
                for (int j = 0; j < stack.getChildCount(); j++) {
                    taskBoundsToCheck.add(stack.getChildAt(j).getBounds());
                }
            }
        }
        adjustBoundsToAvoidConflict(display.getBounds(), taskBoundsToCheck, inOutBounds);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void adjustBoundsToAvoidConflict(Rect displayBounds, List<Rect> taskBoundsToCheck, Rect inOutBounds) {
        if (displayBounds.contains(inOutBounds) && boundsConflict(taskBoundsToCheck, inOutBounds)) {
            calculateCandidateShiftDirections(displayBounds, inOutBounds);
            int[] iArr = this.mTmpDirections;
            for (int direction : iArr) {
                if (direction != 0) {
                    this.mTmpBounds.set(inOutBounds);
                    while (boundsConflict(taskBoundsToCheck, this.mTmpBounds) && displayBounds.contains(this.mTmpBounds)) {
                        shiftBounds(direction, displayBounds, this.mTmpBounds);
                    }
                    if (!boundsConflict(taskBoundsToCheck, this.mTmpBounds) && displayBounds.contains(this.mTmpBounds)) {
                        inOutBounds.set(this.mTmpBounds);
                        appendLog("avoid-bounds-conflict=" + inOutBounds);
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }

    private void calculateCandidateShiftDirections(Rect availableBounds, Rect initialBounds) {
        int i = 0;
        while (true) {
            int[] iArr = this.mTmpDirections;
            if (i >= iArr.length) {
                break;
            }
            iArr[i] = 0;
            i++;
        }
        int oneThirdWidth = ((availableBounds.left * 2) + availableBounds.right) / 3;
        int twoThirdWidth = (availableBounds.left + (availableBounds.right * 2)) / 3;
        int centerX = initialBounds.centerX();
        if (centerX < oneThirdWidth) {
            this.mTmpDirections[0] = 5;
        } else if (centerX > twoThirdWidth) {
            this.mTmpDirections[0] = 3;
        } else {
            int oneThirdHeight = ((availableBounds.top * 2) + availableBounds.bottom) / 3;
            int twoThirdHeight = (availableBounds.top + (availableBounds.bottom * 2)) / 3;
            int centerY = initialBounds.centerY();
            if (centerY < oneThirdHeight || centerY > twoThirdHeight) {
                int[] iArr2 = this.mTmpDirections;
                iArr2[0] = 5;
                iArr2[1] = 3;
                return;
            }
            int[] iArr3 = this.mTmpDirections;
            iArr3[0] = 85;
            iArr3[1] = 51;
        }
    }

    private boolean boundsConflict(List<Rect> taskBoundsToCheck, Rect candidateBounds) {
        Iterator<Rect> it = taskBoundsToCheck.iterator();
        while (true) {
            boolean bottomClose = false;
            if (!it.hasNext()) {
                return false;
            }
            Rect taskBounds = it.next();
            boolean leftClose = Math.abs(taskBounds.left - candidateBounds.left) < 4;
            boolean topClose = Math.abs(taskBounds.top - candidateBounds.top) < 4;
            boolean rightClose = Math.abs(taskBounds.right - candidateBounds.right) < 4;
            if (Math.abs(taskBounds.bottom - candidateBounds.bottom) < 4) {
                bottomClose = true;
            }
            if ((!leftClose || !topClose) && ((!leftClose || !bottomClose) && ((!rightClose || !topClose) && (!rightClose || !bottomClose)))) {
            }
        }
        return true;
    }

    private void shiftBounds(int direction, Rect availableRect, Rect inOutBounds) {
        int horizontalOffset;
        int verticalOffset;
        int i = direction & 7;
        if (i == 3) {
            horizontalOffset = -Math.max(1, availableRect.width() / 16);
        } else if (i != 5) {
            horizontalOffset = 0;
        } else {
            horizontalOffset = Math.max(1, availableRect.width() / 16);
        }
        int i2 = direction & 112;
        if (i2 == 48) {
            verticalOffset = -Math.max(1, availableRect.height() / 16);
        } else if (i2 != 80) {
            verticalOffset = 0;
        } else {
            verticalOffset = Math.max(1, availableRect.height() / 16);
        }
        inOutBounds.offset(horizontalOffset, verticalOffset);
    }

    private void initLogBuilder(TaskRecord task, ActivityRecord activity) {
        this.mLogBuilder = new StringBuilder("TaskLaunchParamsModifier:task=" + task + " activity=" + activity);
    }

    private void appendLog(String log) {
        StringBuilder sb = this.mLogBuilder;
        sb.append(" ");
        sb.append(log);
    }

    private void outputLog() {
        Slog.d(TAG, this.mLogBuilder.toString());
    }

    private static int orientationFromBounds(Rect bounds) {
        if (bounds.width() > bounds.height()) {
            return 0;
        }
        return 1;
    }

    private static boolean sizeMatches(Rect left, Rect right) {
        return Math.abs(right.width() - left.width()) < 2 && Math.abs(right.height() - left.height()) < 2;
    }

    public static boolean isAppLockActivity(String componentName) {
        return APP_LOCK_NAME.equals(componentName) || APP_OPAQUE_LOCK_NAME.equals(componentName);
    }
}
