package com.android.internal.view;

import android.graphics.Rect;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.MergedConfiguration;
import android.view.DragEvent;
import android.view.IWindow.Stub;
import android.view.IWindowLayoutObserver;
import android.view.IWindowSession;
import com.android.internal.os.IResultReceiver;

public class BaseIWindow extends Stub {
    public int mSeq;
    private IWindowSession mSession;

    public void setSession(IWindowSession session) {
        this.mSession = session;
    }

    public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeNavBar, int displayId) {
        if (reportDraw) {
            try {
                this.mSession.finishDrawing(this);
            } catch (RemoteException e) {
            }
        }
    }

    public void moved(int newX, int newY) {
    }

    public void dispatchAppVisibility(boolean visible) {
    }

    public void updateSurfaceStatus(boolean status) {
    }

    public void dispatchGetNewSurface() {
    }

    public void windowFocusChanged(boolean hasFocus, boolean touchEnabled) {
    }

    public void executeCommand(String command, String parameters, ParcelFileDescriptor out) {
    }

    public void closeSystemDialogs(String reason) {
    }

    public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep, boolean sync) {
        if (sync) {
            try {
                this.mSession.wallpaperOffsetsComplete(asBinder());
            } catch (RemoteException e) {
            }
        }
    }

    public void dispatchDragEvent(DragEvent event) {
        if (event.getAction() == 3) {
            try {
                this.mSession.reportDropResult(this, false);
            } catch (RemoteException e) {
            }
        }
    }

    public void updatePointerIcon(float x, float y) {
        InputManager.getInstance().setPointerIconType(1);
    }

    public void dispatchSystemUiVisibilityChanged(int seq, int globalUi, int localValue, int localChanges) {
        this.mSeq = seq;
    }

    public void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras, boolean sync) {
        if (sync) {
            try {
                this.mSession.wallpaperCommandComplete(asBinder(), null);
            } catch (RemoteException e) {
            }
        }
    }

    public void dispatchWindowShown() {
    }

    public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
    }

    public void dispatchPointerCaptureChanged(boolean hasCapture) {
    }

    public void registerWindowObserver(IWindowLayoutObserver observer, long period) {
    }

    public void unRegisterWindowObserver(IWindowLayoutObserver observer) {
    }
}
