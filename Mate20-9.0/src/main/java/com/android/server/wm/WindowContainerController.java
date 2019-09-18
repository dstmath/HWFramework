package com.android.server.wm;

import android.content.res.Configuration;
import com.android.server.wm.WindowContainer;
import com.android.server.wm.WindowContainerListener;

class WindowContainerController<E extends WindowContainer, I extends WindowContainerListener> implements ConfigurationContainerListener {
    E mContainer;
    final I mListener;
    final RootWindowContainer mRoot;
    final WindowManagerService mService;
    final WindowHashMap mWindowMap;

    WindowContainerController(I listener, WindowManagerService service) {
        this.mListener = listener;
        this.mService = service;
        WindowHashMap windowHashMap = null;
        this.mRoot = this.mService != null ? this.mService.mRoot : null;
        this.mWindowMap = this.mService != null ? this.mService.mWindowMap : windowHashMap;
    }

    /* access modifiers changed from: package-private */
    public void setContainer(E container) {
        if (this.mContainer == null || container == null) {
            this.mContainer = container;
            if (this.mContainer != null && this.mListener != null) {
                this.mListener.registerConfigurationChangeListener(this);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Can't set container=" + container + " for controller=" + this + " Already set to=" + this.mContainer);
    }

    /* access modifiers changed from: package-private */
    public void removeContainer() {
        if (this.mContainer != null) {
            this.mContainer.setController(null);
            this.mContainer = null;
            if (this.mListener != null) {
                this.mListener.unregisterConfigurationChangeListener(this);
            }
        }
    }

    public void onOverrideConfigurationChanged(Configuration overrideConfiguration) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                this.mContainer.onOverrideConfigurationChanged(overrideConfiguration);
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }
}
