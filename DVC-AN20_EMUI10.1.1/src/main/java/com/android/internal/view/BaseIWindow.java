package com.android.internal.view;

import android.graphics.Rect;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.MergedConfiguration;
import android.view.DisplayCutout;
import android.view.DragEvent;
import android.view.IWindow;
import android.view.IWindowLayoutObserver;
import android.view.IWindowSession;
import android.view.InsetsSourceControl;
import android.view.InsetsState;
import com.android.internal.os.IResultReceiver;

public class BaseIWindow extends IWindow.Stub {
    public int mSeq;
    private IWindowSession mSession;

    public void setSession(IWindowSession session) {
        this.mSession = session;
    }

    @Override // android.view.IWindow
    public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeSystemBars, int displayId, DisplayCutout.ParcelableWrapper displayCutout) {
        if (reportDraw) {
            try {
                this.mSession.finishDrawing(this);
            } catch (RemoteException e) {
            }
        }
    }

    @Override // android.view.IWindow
    public void insetsChanged(InsetsState insetsState) {
    }

    @Override // android.view.IWindow
    public void insetsControlChanged(InsetsState insetsState, InsetsSourceControl[] activeControls) throws RemoteException {
    }

    @Override // android.view.IWindow
    public void moved(int newX, int newY) {
    }

    @Override // android.view.IWindow
    public void dispatchAppVisibility(boolean visible) {
    }

    @Override // android.view.IWindow
    public void dispatchGetNewSurface() {
    }

    @Override // android.view.IWindow
    public void windowFocusChanged(boolean hasFocus, boolean touchEnabled) {
    }

    @Override // android.view.IWindow
    public void executeCommand(String command, String parameters, ParcelFileDescriptor out) {
    }

    @Override // android.view.IWindow
    public void closeSystemDialogs(String reason) {
    }

    @Override // android.view.IWindow
    public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep, boolean sync) {
        if (sync) {
            try {
                this.mSession.wallpaperOffsetsComplete(asBinder());
            } catch (RemoteException e) {
            }
        }
    }

    @Override // android.view.IWindow
    public void dispatchDragEvent(DragEvent event) {
        if (event.getAction() == 3) {
            try {
                this.mSession.reportDropResult(this, false);
            } catch (RemoteException e) {
            }
        }
    }

    @Override // android.view.IWindow
    public void updatePointerIcon(float x, float y) {
        InputManager.getInstance().setPointerIconType(1);
    }

    @Override // android.view.IWindow
    public void dispatchSystemUiVisibilityChanged(int seq, int globalUi, int localValue, int localChanges) {
        this.mSeq = seq;
    }

    @Override // android.view.IWindow
    public void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras, boolean sync) {
        if (sync) {
            try {
                this.mSession.wallpaperCommandComplete(asBinder(), null);
            } catch (RemoteException e) {
            }
        }
    }

    @Override // android.view.IWindow
    public void dispatchWindowShown() {
    }

    @Override // android.view.IWindow
    public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
    }

    @Override // android.view.IWindow
    public void dispatchPointerCaptureChanged(boolean hasCapture) {
    }

    @Override // android.view.IWindow
    public void registerWindowObserver(IWindowLayoutObserver observer, long period) {
    }

    @Override // android.view.IWindow
    public void unRegisterWindowObserver(IWindowLayoutObserver observer) {
    }

    @Override // android.view.IWindow
    public void notifyFocusChanged() {
    }
}
