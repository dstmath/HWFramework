package com.android.server.wm;

import android.os.Process;
import android.view.InputChannel;
import com.android.server.input.InputApplicationHandle;
import com.android.server.input.InputWindowHandle;

class InputConsumerImpl {
    final InputApplicationHandle mApplicationHandle;
    final InputChannel mClientChannel;
    final InputChannel mServerChannel;
    final WindowManagerService mService;
    final InputWindowHandle mWindowHandle;

    InputConsumerImpl(WindowManagerService service, String name, InputChannel inputChannel) {
        this.mService = service;
        InputChannel[] channels = InputChannel.openInputChannelPair(name);
        this.mServerChannel = channels[0];
        if (inputChannel != null) {
            channels[1].transferTo(inputChannel);
            channels[1].dispose();
            this.mClientChannel = inputChannel;
        } else {
            this.mClientChannel = channels[1];
        }
        this.mService.mInputManager.registerInputChannel(this.mServerChannel, null);
        this.mApplicationHandle = new InputApplicationHandle(null);
        this.mApplicationHandle.name = name;
        this.mApplicationHandle.dispatchingTimeoutNanos = 5000000000L;
        this.mWindowHandle = new InputWindowHandle(this.mApplicationHandle, null, null, 0);
        this.mWindowHandle.name = name;
        this.mWindowHandle.inputChannel = this.mServerChannel;
        this.mWindowHandle.layoutParamsType = 2022;
        this.mWindowHandle.layer = getLayerLw(this.mWindowHandle.layoutParamsType);
        this.mWindowHandle.layoutParamsFlags = 0;
        this.mWindowHandle.dispatchingTimeoutNanos = 5000000000L;
        this.mWindowHandle.visible = true;
        this.mWindowHandle.canReceiveKeys = false;
        this.mWindowHandle.hasFocus = false;
        this.mWindowHandle.hasWallpaper = false;
        this.mWindowHandle.paused = false;
        this.mWindowHandle.ownerPid = Process.myPid();
        this.mWindowHandle.ownerUid = Process.myUid();
        this.mWindowHandle.inputFeatures = 0;
        this.mWindowHandle.scaleFactor = 1.0f;
    }

    void layout(int dw, int dh) {
        this.mWindowHandle.touchableRegion.set(0, 0, dw, dh);
        this.mWindowHandle.frameLeft = 0;
        this.mWindowHandle.frameTop = 0;
        this.mWindowHandle.frameRight = dw;
        this.mWindowHandle.frameBottom = dh;
    }

    private int getLayerLw(int windowType) {
        return (this.mService.mPolicy.getWindowLayerFromTypeLw(windowType) * 10000) + 1000;
    }

    void disposeChannelsLw() {
        this.mService.mInputManager.unregisterInputChannel(this.mServerChannel);
        this.mClientChannel.dispose();
        this.mServerChannel.dispose();
    }
}
