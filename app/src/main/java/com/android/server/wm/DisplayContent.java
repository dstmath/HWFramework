package com.android.server.wm;

import android.app.ActivityManager.StackId;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import com.android.server.wm.WindowManagerService.H;
import java.io.PrintWriter;
import java.util.ArrayList;

public class DisplayContent extends AbsDisplayContent {
    final boolean isDefaultDisplay;
    boolean layoutNeeded;
    int mBaseDisplayDensity;
    int mBaseDisplayHeight;
    Rect mBaseDisplayRect;
    int mBaseDisplayWidth;
    Rect mContentRect;
    boolean mDeferredRemoval;
    final DimLayerController mDimLayerController;
    private final Display mDisplay;
    private final int mDisplayId;
    private final DisplayInfo mDisplayInfo;
    private final DisplayMetrics mDisplayMetrics;
    boolean mDisplayScalingDisabled;
    final DockedStackDividerController mDividerControllerLocked;
    final ArrayList<WindowToken> mExitingTokens;
    private TaskStack mHomeStack;
    int mInitialDisplayDensity;
    int mInitialDisplayHeight;
    int mInitialDisplayWidth;
    Region mNonResizeableRegion;
    final WindowManagerService mService;
    private final ArrayList<TaskStack> mStacks;
    TaskTapPointerEventListener mTapDetector;
    final ArrayList<WindowState> mTapExcludedWindows;
    private final Rect mTmpRect;
    private final Rect mTmpRect2;
    private final Region mTmpRegion;
    final ArrayList<Task> mTmpTaskHistory;
    Region mTouchExcludeRegion;
    private final WindowList mWindows;
    int pendingLayoutChanges;

    public DisplayContent(Display display, WindowManagerService service) {
        boolean z = false;
        this.mWindows = new WindowList();
        this.mInitialDisplayWidth = 0;
        this.mInitialDisplayHeight = 0;
        this.mInitialDisplayDensity = 0;
        this.mBaseDisplayWidth = 0;
        this.mBaseDisplayHeight = 0;
        this.mBaseDisplayDensity = 0;
        this.mDisplayInfo = new DisplayInfo();
        this.mDisplayMetrics = new DisplayMetrics();
        this.mBaseDisplayRect = new Rect();
        this.mContentRect = new Rect();
        this.mExitingTokens = new ArrayList();
        this.mStacks = new ArrayList();
        this.mHomeStack = null;
        this.mTouchExcludeRegion = new Region();
        this.mNonResizeableRegion = new Region();
        this.mTmpRect = new Rect();
        this.mTmpRect2 = new Rect();
        this.mTmpRegion = new Region();
        this.mTmpTaskHistory = new ArrayList();
        this.mTapExcludedWindows = new ArrayList();
        this.mDisplay = display;
        this.mDisplayId = display.getDisplayId();
        display.getDisplayInfo(this.mDisplayInfo);
        display.getMetrics(this.mDisplayMetrics);
        if (this.mDisplayId == 0) {
            z = true;
        }
        this.isDefaultDisplay = z;
        this.mService = service;
        initializeDisplayBaseInfo();
        this.mDividerControllerLocked = new DockedStackDividerController(service, this);
        this.mDimLayerController = new DimLayerController(this);
    }

    int getDisplayId() {
        return this.mDisplayId;
    }

    WindowList getWindowList() {
        return this.mWindows;
    }

    Display getDisplay() {
        return this.mDisplay;
    }

    DisplayInfo getDisplayInfo() {
        return this.mDisplayInfo;
    }

    DisplayMetrics getDisplayMetrics() {
        return this.mDisplayMetrics;
    }

    DockedStackDividerController getDockedDividerController() {
        return this.mDividerControllerLocked;
    }

    public boolean hasAccess(int uid) {
        return this.mDisplay.hasAccess(uid);
    }

    public boolean isPrivate() {
        return (this.mDisplay.getFlags() & 4) != 0;
    }

    ArrayList<TaskStack> getStacks() {
        return this.mStacks;
    }

    ArrayList<Task> getTasks() {
        this.mTmpTaskHistory.clear();
        int numStacks = this.mStacks.size();
        for (int stackNdx = 0; stackNdx < numStacks; stackNdx++) {
            this.mTmpTaskHistory.addAll(((TaskStack) this.mStacks.get(stackNdx)).getTasks());
        }
        return this.mTmpTaskHistory;
    }

    TaskStack getHomeStack() {
        if (this.mHomeStack == null && this.mDisplayId == 0) {
            Slog.e("WindowManager", "getHomeStack: Returning null from this=" + this);
        }
        return this.mHomeStack;
    }

    void updateDisplayInfo() {
        this.mDisplay.getDisplayInfo(this.mDisplayInfo);
        this.mDisplay.getMetrics(this.mDisplayMetrics);
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            ((TaskStack) this.mStacks.get(i)).updateDisplayInfo(null);
        }
    }

    void initializeDisplayBaseInfo() {
        DisplayInfo newDisplayInfo = this.mService.mDisplayManagerInternal.getDisplayInfo(this.mDisplayId);
        if (newDisplayInfo != null) {
            this.mDisplayInfo.copyFrom(newDisplayInfo);
        }
        int i = this.mDisplayInfo.logicalWidth;
        this.mInitialDisplayWidth = i;
        this.mBaseDisplayWidth = i;
        i = this.mDisplayInfo.logicalHeight;
        this.mInitialDisplayHeight = i;
        this.mBaseDisplayHeight = i;
        i = this.mDisplayInfo.logicalDensityDpi;
        this.mInitialDisplayDensity = i;
        this.mBaseDisplayDensity = i;
        this.mBaseDisplayRect.set(0, 0, this.mBaseDisplayWidth, this.mBaseDisplayHeight);
    }

    void getLogicalDisplayRect(Rect out) {
        boolean rotated = true;
        int orientation = this.mDisplayInfo.rotation;
        if (!(orientation == 1 || orientation == 3)) {
            rotated = false;
        }
        int physWidth = rotated ? this.mBaseDisplayHeight : this.mBaseDisplayWidth;
        int physHeight = rotated ? this.mBaseDisplayWidth : this.mBaseDisplayHeight;
        int width = this.mDisplayInfo.logicalWidth;
        int left = (physWidth - width) / 2;
        int height = this.mDisplayInfo.logicalHeight;
        int top = (physHeight - height) / 2;
        out.set(left, top, left + width, top + height);
    }

    void getContentRect(Rect out) {
        out.set(this.mContentRect);
    }

    void attachStack(TaskStack stack, boolean onTop) {
        if (stack.mStackId == 0) {
            if (this.mHomeStack != null) {
                throw new IllegalArgumentException("attachStack: HOME_STACK_ID (0) not first.");
            }
            this.mHomeStack = stack;
        }
        if (onTop) {
            this.mStacks.add(stack);
        } else {
            this.mStacks.add(0, stack);
        }
        this.layoutNeeded = true;
    }

    void moveStack(TaskStack stack, boolean toTop) {
        if (!StackId.isAlwaysOnTop(stack.mStackId) || toTop) {
            if (!this.mStacks.remove(stack)) {
                Slog.wtf("WindowManager", "moving stack that was not added: " + stack, new Throwable());
            }
            int addIndex = toTop ? this.mStacks.size() : 0;
            if (toTop && this.mService.isStackVisibleLocked(4) && stack.mStackId != 4) {
                addIndex--;
                if (((TaskStack) this.mStacks.get(addIndex)).mStackId != 4) {
                    throw new IllegalStateException("Pinned stack isn't top stack??? " + this.mStacks);
                }
            }
            this.mStacks.add(addIndex, stack);
            return;
        }
        Slog.w("WindowManager", "Ignoring move of always-on-top stack=" + stack + " to bottom");
    }

    void detachStack(TaskStack stack) {
        this.mDimLayerController.removeDimLayerUser(stack);
        this.mStacks.remove(stack);
    }

    void resize(Rect contentRect) {
        this.mContentRect.set(contentRect);
    }

    int taskIdFromPoint(int x, int y) {
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            TaskStack stack = (TaskStack) this.mStacks.get(stackNdx);
            stack.getBounds(this.mTmpRect);
            if (this.mTmpRect.contains(x, y) && !stack.isAdjustedForMinimizedDockedStack()) {
                ArrayList<Task> tasks = stack.getTasks();
                for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                    Task task = (Task) tasks.get(taskNdx);
                    if (!(task.getTopVisibleAppMainWindow() == null || task.isFullscreen())) {
                        task.getDimBounds(this.mTmpRect);
                        if (this.mTmpRect.contains(x, y)) {
                            return task.mTaskId;
                        }
                    }
                }
                continue;
            }
        }
        return -1;
    }

    Task findTaskForControlPoint(int x, int y) {
        WindowManagerService windowManagerService = this.mService;
        int delta = WindowManagerService.dipToPixel(30, this.mDisplayMetrics);
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            TaskStack stack = (TaskStack) this.mStacks.get(stackNdx);
            if (!StackId.isTaskResizeAllowed(stack.mStackId)) {
                break;
            }
            ArrayList<Task> tasks = stack.getTasks();
            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                Task task = (Task) tasks.get(taskNdx);
                if (task.isFullscreen()) {
                    return null;
                }
                task.getDimBounds(this.mTmpRect);
                this.mTmpRect.inset(-delta, -delta);
                if (this.mTmpRect.contains(x, y)) {
                    this.mTmpRect.inset(delta, delta);
                    if (this.mTmpRect.contains(x, y)) {
                        return null;
                    }
                    return task;
                }
            }
        }
        return null;
    }

    void setTouchExcludeRegion(Task focusedTask) {
        this.mTouchExcludeRegion.set(this.mBaseDisplayRect);
        WindowManagerService windowManagerService = this.mService;
        int delta = WindowManagerService.dipToPixel(30, this.mDisplayMetrics);
        boolean addBackFocusedTask = false;
        this.mNonResizeableRegion.setEmpty();
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            TaskStack stack = (TaskStack) this.mStacks.get(stackNdx);
            ArrayList<Task> tasks = stack.getTasks();
            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                Task task = (Task) tasks.get(taskNdx);
                AppWindowToken token = task.getTopVisibleAppToken();
                if (token != null && token.isVisible()) {
                    task.getDimBounds(this.mTmpRect);
                    if (task == focusedTask) {
                        addBackFocusedTask = true;
                        this.mTmpRect2.set(this.mTmpRect);
                    }
                    boolean isFreeformed = task.inFreeformWorkspace();
                    if (task != focusedTask || isFreeformed) {
                        if (isFreeformed) {
                            this.mTmpRect.inset(-delta, -delta);
                            this.mTmpRect.intersect(this.mContentRect);
                        }
                        this.mTouchExcludeRegion.op(this.mTmpRect, Op.DIFFERENCE);
                    }
                    if (task.isTwoFingerScrollMode()) {
                        stack.getBounds(this.mTmpRect);
                        this.mNonResizeableRegion.op(this.mTmpRect, Op.UNION);
                        break;
                    }
                }
            }
        }
        if (addBackFocusedTask) {
            this.mTouchExcludeRegion.op(this.mTmpRect2, Op.UNION);
        }
        WindowState inputMethod = this.mService.mInputMethodWindow;
        if (inputMethod != null && inputMethod.isVisibleLw()) {
            inputMethod.getTouchableRegion(this.mTmpRegion);
            this.mTouchExcludeRegion.op(this.mTmpRegion, Op.UNION);
        }
        for (int i = this.mTapExcludedWindows.size() - 1; i >= 0; i--) {
            ((WindowState) this.mTapExcludedWindows.get(i)).getTouchableRegion(this.mTmpRegion);
            this.mTouchExcludeRegion.op(this.mTmpRegion, Op.UNION);
        }
        if (getDockedStackVisibleForUserLocked() != null) {
            this.mDividerControllerLocked.getTouchRegion(this.mTmpRect);
            this.mTmpRegion.set(this.mTmpRect);
            this.mTouchExcludeRegion.op(this.mTmpRegion, Op.UNION);
        }
        if (this.mTapDetector != null) {
            this.mTapDetector.setTouchExcludeRegion(this.mTouchExcludeRegion, this.mNonResizeableRegion);
        }
    }

    void switchUserStacks() {
        WindowList windows = getWindowList();
        for (int i = 0; i < windows.size(); i++) {
            WindowState win = (WindowState) windows.get(i);
            if (win.isHiddenFromUserLocked()) {
                win.hideLw(false);
            }
        }
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mStacks.get(stackNdx)).switchUser();
        }
    }

    void resetAnimationBackgroundAnimator() {
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mStacks.get(stackNdx)).resetAnimationBackgroundAnimator();
        }
    }

    boolean animateDimLayers() {
        return this.mDimLayerController.animateDimLayers();
    }

    void resetDimming() {
        this.mDimLayerController.resetDimming();
    }

    boolean isDimming() {
        return this.mDimLayerController.isDimming();
    }

    void stopDimmingIfNeeded() {
        this.mDimLayerController.stopDimmingIfNeeded();
    }

    void close() {
        this.mDimLayerController.close();
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mStacks.get(stackNdx)).close();
        }
    }

    boolean isAnimating() {
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            if (((TaskStack) this.mStacks.get(stackNdx)).isAnimating()) {
                return true;
            }
        }
        return false;
    }

    void checkForDeferredActions() {
        boolean animating = false;
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            TaskStack stack = (TaskStack) this.mStacks.get(stackNdx);
            if (stack.isAnimating()) {
                animating = true;
            } else {
                if (stack.mDeferDetach) {
                    this.mService.detachStackLocked(this, stack);
                }
                ArrayList<Task> tasks = stack.getTasks();
                for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                    AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                    for (int tokenNdx = tokens.size() - 1; tokenNdx >= 0; tokenNdx--) {
                        AppWindowToken wtoken = (AppWindowToken) tokens.get(tokenNdx);
                        if (wtoken.mIsExiting) {
                            wtoken.removeAppFromTaskLocked();
                        }
                    }
                }
            }
        }
        if (!animating && this.mDeferredRemoval) {
            this.mService.onDisplayRemoved(this.mDisplayId);
        }
    }

    void rotateBounds(int oldRotation, int newRotation, Rect bounds) {
        int rotationDelta = deltaRotation(oldRotation, newRotation);
        getLogicalDisplayRect(this.mTmpRect);
        switch (rotationDelta) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                this.mTmpRect2.set(bounds);
                break;
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                this.mTmpRect2.top = this.mTmpRect.bottom - bounds.right;
                this.mTmpRect2.left = bounds.top;
                this.mTmpRect2.right = this.mTmpRect2.left + bounds.height();
                this.mTmpRect2.bottom = this.mTmpRect2.top + bounds.width();
                break;
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                this.mTmpRect2.top = this.mTmpRect.bottom - bounds.bottom;
                this.mTmpRect2.left = this.mTmpRect.right - bounds.right;
                this.mTmpRect2.right = this.mTmpRect2.left + bounds.width();
                this.mTmpRect2.bottom = this.mTmpRect2.top + bounds.height();
                break;
            case H.REPORT_LOSING_FOCUS /*3*/:
                this.mTmpRect2.top = bounds.left;
                this.mTmpRect2.left = this.mTmpRect.right - bounds.bottom;
                this.mTmpRect2.right = this.mTmpRect2.left + bounds.height();
                this.mTmpRect2.bottom = this.mTmpRect2.top + bounds.width();
                break;
        }
        bounds.set(this.mTmpRect2);
    }

    static int deltaRotation(int oldRotation, int newRotation) {
        int delta = newRotation - oldRotation;
        if (delta < 0) {
            return delta + 4;
        }
        return delta;
    }

    public void dump(String prefix, PrintWriter pw) {
        int stackNdx;
        int i;
        pw.print(prefix);
        pw.print("Display: mDisplayId=");
        pw.println(this.mDisplayId);
        String subPrefix = "  " + prefix;
        pw.print(subPrefix);
        pw.print("init=");
        pw.print(this.mInitialDisplayWidth);
        pw.print("x");
        pw.print(this.mInitialDisplayHeight);
        pw.print(" ");
        pw.print(this.mInitialDisplayDensity);
        pw.print("dpi");
        if (this.mInitialDisplayWidth == this.mBaseDisplayWidth && this.mInitialDisplayHeight == this.mBaseDisplayHeight) {
            if (this.mInitialDisplayDensity != this.mBaseDisplayDensity) {
            }
            if (this.mDisplayScalingDisabled) {
                pw.println(" noscale");
            }
            pw.print(" cur=");
            pw.print(this.mDisplayInfo.logicalWidth);
            pw.print("x");
            pw.print(this.mDisplayInfo.logicalHeight);
            pw.print(" app=");
            pw.print(this.mDisplayInfo.appWidth);
            pw.print("x");
            pw.print(this.mDisplayInfo.appHeight);
            pw.print(" rng=");
            pw.print(this.mDisplayInfo.smallestNominalAppWidth);
            pw.print("x");
            pw.print(this.mDisplayInfo.smallestNominalAppHeight);
            pw.print("-");
            pw.print(this.mDisplayInfo.largestNominalAppWidth);
            pw.print("x");
            pw.println(this.mDisplayInfo.largestNominalAppHeight);
            pw.print(subPrefix);
            pw.print("deferred=");
            pw.print(this.mDeferredRemoval);
            pw.print(" layoutNeeded=");
            pw.println(this.layoutNeeded);
            pw.println();
            pw.println("  Application tokens in top down Z order:");
            for (stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((TaskStack) this.mStacks.get(stackNdx)).dump(prefix + "  ", pw);
            }
            pw.println();
            if (!this.mExitingTokens.isEmpty()) {
                pw.println();
                pw.println("  Exiting tokens:");
                for (i = this.mExitingTokens.size() - 1; i >= 0; i--) {
                    WindowToken token = (WindowToken) this.mExitingTokens.get(i);
                    pw.print("  Exiting #");
                    pw.print(i);
                    pw.print(' ');
                    pw.print(token);
                    pw.println(':');
                    token.dump(pw, "    ");
                }
            }
            pw.println();
            this.mDimLayerController.dump(prefix + "  ", pw);
            pw.println();
            this.mDividerControllerLocked.dump(prefix + "  ", pw);
        }
        pw.print(" base=");
        pw.print(this.mBaseDisplayWidth);
        pw.print("x");
        pw.print(this.mBaseDisplayHeight);
        pw.print(" ");
        pw.print(this.mBaseDisplayDensity);
        pw.print("dpi");
        if (this.mDisplayScalingDisabled) {
            pw.println(" noscale");
        }
        pw.print(" cur=");
        pw.print(this.mDisplayInfo.logicalWidth);
        pw.print("x");
        pw.print(this.mDisplayInfo.logicalHeight);
        pw.print(" app=");
        pw.print(this.mDisplayInfo.appWidth);
        pw.print("x");
        pw.print(this.mDisplayInfo.appHeight);
        pw.print(" rng=");
        pw.print(this.mDisplayInfo.smallestNominalAppWidth);
        pw.print("x");
        pw.print(this.mDisplayInfo.smallestNominalAppHeight);
        pw.print("-");
        pw.print(this.mDisplayInfo.largestNominalAppWidth);
        pw.print("x");
        pw.println(this.mDisplayInfo.largestNominalAppHeight);
        pw.print(subPrefix);
        pw.print("deferred=");
        pw.print(this.mDeferredRemoval);
        pw.print(" layoutNeeded=");
        pw.println(this.layoutNeeded);
        pw.println();
        pw.println("  Application tokens in top down Z order:");
        for (stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mStacks.get(stackNdx)).dump(prefix + "  ", pw);
        }
        pw.println();
        if (this.mExitingTokens.isEmpty()) {
            pw.println();
            pw.println("  Exiting tokens:");
            for (i = this.mExitingTokens.size() - 1; i >= 0; i--) {
                WindowToken token2 = (WindowToken) this.mExitingTokens.get(i);
                pw.print("  Exiting #");
                pw.print(i);
                pw.print(' ');
                pw.print(token2);
                pw.println(':');
                token2.dump(pw, "    ");
            }
        }
        pw.println();
        this.mDimLayerController.dump(prefix + "  ", pw);
        pw.println();
        this.mDividerControllerLocked.dump(prefix + "  ", pw);
    }

    public String toString() {
        return "Display " + this.mDisplayId + " info=" + this.mDisplayInfo + " stacks=" + this.mStacks;
    }

    TaskStack getDockedStackLocked() {
        TaskStack stack = (TaskStack) this.mService.mStackIdToStack.get(3);
        return (stack == null || !stack.isVisibleLocked()) ? null : stack;
    }

    TaskStack getDockedStackVisibleForUserLocked() {
        TaskStack stack = (TaskStack) this.mService.mStackIdToStack.get(3);
        return (stack == null || !stack.isVisibleForUserLocked()) ? null : stack;
    }

    WindowState getTouchableWinAtPointLocked(float xf, float yf) {
        int x = (int) xf;
        int y = (int) yf;
        for (int i = this.mWindows.size() - 1; i >= 0; i--) {
            WindowState window = (WindowState) this.mWindows.get(i);
            int flags = window.mAttrs.flags;
            if (window.isVisibleLw() && (flags & 16) == 0) {
                window.getVisibleBounds(this.mTmpRect);
                if (this.mTmpRect.contains(x, y)) {
                    window.getTouchableRegion(this.mTmpRegion);
                    int touchFlags = flags & 40;
                    if (this.mTmpRegion.contains(x, y) || touchFlags == 0) {
                        return window;
                    }
                } else {
                    continue;
                }
            }
        }
        return null;
    }
}
