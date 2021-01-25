package com.android.server.wm;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.view.IWindow;
import android.view.InputApplicationHandle;
import android.view.InputChannel;
import android.view.InputWindowHandle;
import android.view.SurfaceControl;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public class InputConsumerImpl implements IBinder.DeathRecipient {
    final InputApplicationHandle mApplicationHandle;
    final InputChannel mClientChannel;
    final int mClientPid;
    final UserHandle mClientUser;
    final SurfaceControl mInputSurface;
    final String mName;
    private final Point mOldPosition = new Point();
    private final Rect mOldWindowCrop = new Rect();
    final InputChannel mServerChannel;
    final WindowManagerService mService;
    Rect mTmpClipRect = new Rect();
    private final Rect mTmpRect = new Rect();
    final IBinder mToken;
    final InputWindowHandle mWindowHandle;

    InputConsumerImpl(WindowManagerService service, IBinder token, String name, InputChannel inputChannel, int clientPid, UserHandle clientUser, int displayId) {
        this.mService = service;
        this.mToken = token;
        this.mName = name;
        this.mClientPid = clientPid;
        this.mClientUser = clientUser;
        InputChannel[] channels = InputChannel.openInputChannelPair(name);
        this.mServerChannel = channels[0];
        if (inputChannel != null) {
            channels[1].transferTo(inputChannel);
            channels[1].dispose();
            this.mClientChannel = inputChannel;
        } else {
            this.mClientChannel = channels[1];
        }
        this.mService.mInputManager.registerInputChannel(this.mServerChannel, (IBinder) null);
        this.mApplicationHandle = new InputApplicationHandle(new Binder());
        InputApplicationHandle inputApplicationHandle = this.mApplicationHandle;
        inputApplicationHandle.name = name;
        inputApplicationHandle.dispatchingTimeoutNanos = 5000000000L;
        this.mWindowHandle = new InputWindowHandle(inputApplicationHandle, (IWindow) null, displayId);
        InputWindowHandle inputWindowHandle = this.mWindowHandle;
        inputWindowHandle.name = name;
        inputWindowHandle.token = this.mServerChannel.getToken();
        InputWindowHandle inputWindowHandle2 = this.mWindowHandle;
        inputWindowHandle2.layoutParamsType = 2022;
        inputWindowHandle2.layer = getLayerLw(inputWindowHandle2.layoutParamsType);
        InputWindowHandle inputWindowHandle3 = this.mWindowHandle;
        inputWindowHandle3.layoutParamsFlags = 0;
        inputWindowHandle3.dispatchingTimeoutNanos = 5000000000L;
        inputWindowHandle3.visible = true;
        inputWindowHandle3.canReceiveKeys = false;
        inputWindowHandle3.hasFocus = false;
        inputWindowHandle3.hasWallpaper = false;
        inputWindowHandle3.paused = false;
        inputWindowHandle3.ownerPid = Process.myPid();
        this.mWindowHandle.ownerUid = Process.myUid();
        InputWindowHandle inputWindowHandle4 = this.mWindowHandle;
        inputWindowHandle4.inputFeatures = 0;
        inputWindowHandle4.scaleFactor = 1.0f;
        WindowManagerService windowManagerService = this.mService;
        SurfaceControl.Builder containerLayer = windowManagerService.makeSurfaceBuilder(windowManagerService.mRoot.getDisplayContent(displayId).getSession()).setContainerLayer();
        this.mInputSurface = containerLayer.setName("Input Consumer " + name).build();
    }

    /* access modifiers changed from: package-private */
    public void linkToDeathRecipient() {
        IBinder iBinder = this.mToken;
        if (iBinder != null) {
            try {
                iBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unlinkFromDeathRecipient() {
        IBinder iBinder = this.mToken;
        if (iBinder != null) {
            iBinder.unlinkToDeath(this, 0);
        }
    }

    /* access modifiers changed from: package-private */
    public void layout(SurfaceControl.Transaction t, int dw, int dh) {
        this.mTmpRect.set(0, 0, dw, dh);
        layout(t, this.mTmpRect);
    }

    /* access modifiers changed from: package-private */
    public void layout(SurfaceControl.Transaction t, Rect r) {
        this.mTmpClipRect.set(0, 0, r.width(), r.height());
        if (!this.mOldPosition.equals(r.left, r.top) || !this.mOldWindowCrop.equals(this.mTmpClipRect)) {
            t.setPosition(this.mInputSurface, (float) r.left, (float) r.top);
            t.setWindowCrop(this.mInputSurface, this.mTmpClipRect);
            this.mOldPosition.set(r.left, r.top);
            this.mOldWindowCrop.set(this.mTmpClipRect);
        }
    }

    /* access modifiers changed from: package-private */
    public void hide(SurfaceControl.Transaction t) {
        t.hide(this.mInputSurface);
    }

    /* access modifiers changed from: package-private */
    public void show(SurfaceControl.Transaction t, WindowState w) {
        t.show(this.mInputSurface);
        t.setInputWindowInfo(this.mInputSurface, this.mWindowHandle);
        t.setRelativeLayer(this.mInputSurface, w.getSurfaceControl(), 1);
    }

    /* access modifiers changed from: package-private */
    public void show(SurfaceControl.Transaction t, int layer) {
        t.show(this.mInputSurface);
        t.setInputWindowInfo(this.mInputSurface, this.mWindowHandle);
        t.setLayer(this.mInputSurface, layer);
    }

    private int getLayerLw(int windowType) {
        return (this.mService.mPolicy.getWindowLayerFromTypeLw(windowType) * 10000) + 1000;
    }

    /* access modifiers changed from: package-private */
    public void disposeChannelsLw() {
        this.mService.mInputManager.unregisterInputChannel(this.mServerChannel);
        this.mClientChannel.dispose();
        this.mServerChannel.dispose();
        unlinkFromDeathRecipient();
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        synchronized (this.mService.getWindowManagerLock()) {
            this.mService.mRoot.getDisplayContent(this.mWindowHandle.displayId).getInputMonitor().destroyInputConsumer(this.mName);
            unlinkFromDeathRecipient();
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String name, String prefix) {
        pw.println(prefix + "  name=" + name + " pid=" + this.mClientPid + " user=" + this.mClientUser);
    }
}
