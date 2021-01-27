package com.huawei.android.view;

import android.os.RemoteException;
import android.view.IDockedStackListener;

public class IDockedStackListenerEx {
    private IDockedStackListener mDockedStackListener = new IDockedStackListener.Stub() {
        /* class com.huawei.android.view.IDockedStackListenerEx.AnonymousClass1 */

        public void onAdjustedForImeChanged(boolean adjustedForIme, long animDuration) throws RemoteException {
            IDockedStackListenerEx.this.onAdjustedForImeChanged(adjustedForIme, animDuration);
        }

        public void onDividerVisibilityChanged(boolean isVisible) throws RemoteException {
            IDockedStackListenerEx.this.onDividerVisibilityChanged(isVisible);
        }

        public void onDockSideChanged(int newDockSide) throws RemoteException {
            IDockedStackListenerEx.this.onDockSideChanged(newDockSide);
        }

        public void onDockedStackExistsChanged(boolean isExists) throws RemoteException {
            IDockedStackListenerEx.this.onDockedStackExistsChanged(isExists);
        }

        public void onDockedStackMinimizedChanged(boolean isMinimized, long animDuration, boolean isHomeStackResizable) throws RemoteException {
            IDockedStackListenerEx.this.onDockedStackMinimizedChanged(isMinimized, animDuration, isHomeStackResizable);
        }
    };

    public IDockedStackListener getDockedStackListener() {
        return this.mDockedStackListener;
    }

    public void onAdjustedForImeChanged(boolean isAdjustedForIme, long animDuration) throws RemoteException {
    }

    public void onDividerVisibilityChanged(boolean isVisible) throws RemoteException {
    }

    public void onDockSideChanged(int newDockSide) throws RemoteException {
    }

    public void onDockedStackExistsChanged(boolean isExists) throws RemoteException {
    }

    public void onDockedStackMinimizedChanged(boolean isMinimized, long animDuration, boolean isHomeStackResizable) throws RemoteException {
    }
}
