package com.android.server.wm;

import android.content.res.Configuration;
import android.os.Binder;
import android.util.Slog;
import android.view.Display;

public class DisplayWindowController extends WindowContainerController<DisplayContent, WindowContainerListener> {
    private final int mDisplayId;

    public DisplayWindowController(Display display, WindowContainerListener listener) {
        super(listener, WindowManagerService.getInstance());
        long callingIdentity;
        this.mDisplayId = display.getDisplayId();
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                callingIdentity = Binder.clearCallingIdentity();
                this.mRoot.createDisplayContent(display, this);
                Binder.restoreCallingIdentity(callingIdentity);
                if (this.mContainer == null) {
                    throw new IllegalArgumentException("Trying to add display=" + display + " dc=" + this.mRoot.getDisplayContent(this.mDisplayId));
                }
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002b, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0028, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    public void removeContainer() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    ((DisplayContent) this.mContainer).removeIfPossible();
                    super.removeContainer();
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
                    Slog.i("WindowManager", "removeDisplay: could not find displayId=" + this.mDisplayId);
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void onOverrideConfigurationChanged(Configuration overrideConfiguration) {
    }

    public void positionChildAt(StackWindowController child, int position) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else if (child == null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else if (child.mContainer == null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else {
                    ((DisplayContent) this.mContainer).positionStackAt(position, (TaskStack) child.mContainer);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void deferUpdateImeTarget() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                DisplayContent dc = this.mRoot.getDisplayContent(this.mDisplayId);
                if (dc != null) {
                    dc.deferUpdateImeTarget();
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    public void continueUpdateImeTarget() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                DisplayContent dc = this.mRoot.getDisplayContent(this.mDisplayId);
                if (dc != null) {
                    dc.continueUpdateImeTarget();
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    public String toString() {
        return "{DisplayWindowController displayId=" + this.mDisplayId + "}";
    }
}
