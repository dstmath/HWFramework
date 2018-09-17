package com.huawei.android.view;

import android.os.RemoteException;
import android.view.IDockedStackListener;
import android.view.IDockedStackListener.Stub;

public class IDockedStackListenerEx {
    private IDockedStackListener mDockedStackListener = new Stub() {
        public void onAdjustedForImeChanged(boolean adjustedForIme, long animDuration) throws RemoteException {
            IDockedStackListenerEx.this.onAdjustedForImeChanged(adjustedForIme, animDuration);
        }

        public void onDividerVisibilityChanged(boolean visible) throws RemoteException {
            IDockedStackListenerEx.this.onDividerVisibilityChanged(visible);
        }

        public void onDockSideChanged(int newDockSide) throws RemoteException {
            IDockedStackListenerEx.this.onDockSideChanged(newDockSide);
        }

        public void onDockedStackExistsChanged(boolean exists) throws RemoteException {
            IDockedStackListenerEx.this.onDockedStackExistsChanged(exists);
        }

        public void onDockedStackMinimizedChanged(boolean minimized, long animDuration, boolean isHomeStackResizable) throws RemoteException {
            IDockedStackListenerEx.this.onDockedStackMinimizedChanged(minimized, animDuration, isHomeStackResizable);
        }
    };

    public IDockedStackListener getDockedStackListener() {
        return this.mDockedStackListener;
    }

    public void onAdjustedForImeChanged(boolean adjustedForIme, long animDuration) throws RemoteException {
    }

    public void onDividerVisibilityChanged(boolean visible) throws RemoteException {
    }

    public void onDockSideChanged(int newDockSide) throws RemoteException {
    }

    public void onDockedStackExistsChanged(boolean exists) throws RemoteException {
    }

    public void onDockedStackMinimizedChanged(boolean minimized, long animDuration, boolean isHomeStackResizable) throws RemoteException {
    }
}
