package com.android.systemui.shared.system;

import android.content.pm.ParceledListSlice;
import android.graphics.Rect;
import android.os.RemoteException;
import android.view.IPinnedStackController;
import android.view.IPinnedStackListener;
import java.util.ArrayList;
import java.util.List;

public class PinnedStackListenerForwarder extends IPinnedStackListener.Stub {
    private List<IPinnedStackListener> mListeners = new ArrayList();

    public void addListener(IPinnedStackListener listener) {
        this.mListeners.add(listener);
    }

    public void removeListener(IPinnedStackListener listener) {
        this.mListeners.remove(listener);
    }

    public void onListenerRegistered(IPinnedStackController controller) throws RemoteException {
        for (IPinnedStackListener listener : this.mListeners) {
            listener.onListenerRegistered(controller);
        }
    }

    public void onMovementBoundsChanged(Rect insetBounds, Rect normalBounds, Rect animatingBounds, boolean fromImeAdjustment, boolean fromShelfAdjustment, int displayRotation) throws RemoteException {
        for (IPinnedStackListener listener : this.mListeners) {
            listener.onMovementBoundsChanged(insetBounds, normalBounds, animatingBounds, fromImeAdjustment, fromShelfAdjustment, displayRotation);
        }
    }

    public void onImeVisibilityChanged(boolean imeVisible, int imeHeight) throws RemoteException {
        for (IPinnedStackListener listener : this.mListeners) {
            listener.onImeVisibilityChanged(imeVisible, imeHeight);
        }
    }

    public void onShelfVisibilityChanged(boolean shelfVisible, int shelfHeight) throws RemoteException {
        for (IPinnedStackListener listener : this.mListeners) {
            listener.onShelfVisibilityChanged(shelfVisible, shelfHeight);
        }
    }

    public void onMinimizedStateChanged(boolean isMinimized) throws RemoteException {
        for (IPinnedStackListener listener : this.mListeners) {
            listener.onMinimizedStateChanged(isMinimized);
        }
    }

    public void onActionsChanged(ParceledListSlice actions) throws RemoteException {
        for (IPinnedStackListener listener : this.mListeners) {
            listener.onActionsChanged(actions);
        }
    }
}
