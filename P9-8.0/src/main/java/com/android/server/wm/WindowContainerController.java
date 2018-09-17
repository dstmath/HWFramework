package com.android.server.wm;

class WindowContainerController<E extends WindowContainer, I extends WindowContainerListener> {
    E mContainer;
    final I mListener;
    final RootWindowContainer mRoot;
    final WindowManagerService mService;
    final WindowHashMap mWindowMap;

    WindowContainerController(I listener, WindowManagerService service) {
        RootWindowContainer rootWindowContainer;
        WindowHashMap windowHashMap = null;
        this.mListener = listener;
        this.mService = service;
        if (this.mService != null) {
            rootWindowContainer = this.mService.mRoot;
        } else {
            rootWindowContainer = null;
        }
        this.mRoot = rootWindowContainer;
        if (this.mService != null) {
            windowHashMap = this.mService.mWindowMap;
        }
        this.mWindowMap = windowHashMap;
    }

    void setContainer(E container) {
        if (this.mContainer == null || container == null) {
            this.mContainer = container;
            return;
        }
        throw new IllegalArgumentException("Can't set container=" + container + " for controller=" + this + " Already set to=" + this.mContainer);
    }

    void removeContainer() {
        if (this.mContainer != null) {
            this.mContainer.setController(null);
            this.mContainer = null;
        }
    }

    boolean checkCallingPermission(String permission, String func) {
        return this.mService.checkCallingPermission(permission, func);
    }
}
