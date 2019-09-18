package com.android.server.wm;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.SparseArray;
import android.view.IWallpaperVisibilityListener;

class WallpaperVisibilityListeners {
    private final SparseArray<RemoteCallbackList<IWallpaperVisibilityListener>> mDisplayListeners = new SparseArray<>();

    WallpaperVisibilityListeners() {
    }

    /* access modifiers changed from: package-private */
    public void registerWallpaperVisibilityListener(IWallpaperVisibilityListener listener, int displayId) {
        RemoteCallbackList<IWallpaperVisibilityListener> listeners = this.mDisplayListeners.get(displayId);
        if (listeners == null) {
            listeners = new RemoteCallbackList<>();
            this.mDisplayListeners.append(displayId, listeners);
        }
        listeners.register(listener);
    }

    /* access modifiers changed from: package-private */
    public void unregisterWallpaperVisibilityListener(IWallpaperVisibilityListener listener, int displayId) {
        RemoteCallbackList<IWallpaperVisibilityListener> listeners = this.mDisplayListeners.get(displayId);
        if (listeners != null) {
            listeners.unregister(listener);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyWallpaperVisibilityChanged(DisplayContent displayContent) {
        int displayId = displayContent.getDisplayId();
        boolean visible = displayContent.mWallpaperController.isWallpaperVisible();
        RemoteCallbackList<IWallpaperVisibilityListener> displayListeners = this.mDisplayListeners.get(displayId);
        if (displayListeners != null) {
            int i = displayListeners.beginBroadcast();
            while (i > 0) {
                i--;
                try {
                    displayListeners.getBroadcastItem(i).onWallpaperVisibilityChanged(visible, displayId);
                } catch (RemoteException e) {
                }
            }
            displayListeners.finishBroadcast();
        }
    }
}
