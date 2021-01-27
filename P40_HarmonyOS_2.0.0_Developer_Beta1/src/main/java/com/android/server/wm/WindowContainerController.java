package com.android.server.wm;

import android.content.res.Configuration;
import com.android.server.wm.WindowContainer;
import com.android.server.wm.WindowContainerListener;

/* access modifiers changed from: package-private */
public class WindowContainerController<E extends WindowContainer, I extends WindowContainerListener> implements ConfigurationContainerListener {
    E mContainer;
    final WindowManagerGlobalLock mGlobalLock;
    final I mListener;
    final RootWindowContainer mRoot;
    final WindowManagerService mService;

    WindowContainerController(I listener, WindowManagerService service) {
        this.mListener = listener;
        this.mService = service;
        WindowManagerService windowManagerService = this.mService;
        WindowManagerGlobalLock windowManagerGlobalLock = null;
        this.mRoot = windowManagerService != null ? windowManagerService.mRoot : null;
        WindowManagerService windowManagerService2 = this.mService;
        this.mGlobalLock = windowManagerService2 != null ? windowManagerService2.mGlobalLock : windowManagerGlobalLock;
    }

    /* access modifiers changed from: package-private */
    public void setContainer(E container) {
        I i;
        if (this.mContainer == null || container == null) {
            this.mContainer = container;
            if (this.mContainer != null && (i = this.mListener) != null) {
                i.registerConfigurationChangeListener(this);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Can't set container=" + container + " for controller=" + this + " Already set to=" + this.mContainer);
    }

    /* access modifiers changed from: package-private */
    public void removeContainer() {
        E e = this.mContainer;
        if (e != null) {
            e.setController(null);
            this.mContainer = null;
            I i = this.mListener;
            if (i != null) {
                i.unregisterConfigurationChangeListener(this);
            }
        }
    }

    @Override // com.android.server.wm.ConfigurationContainerListener
    public void onRequestedOverrideConfigurationChanged(Configuration overrideConfiguration) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    this.mContainer.onRequestedOverrideConfigurationChanged(overrideConfiguration);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }
}
