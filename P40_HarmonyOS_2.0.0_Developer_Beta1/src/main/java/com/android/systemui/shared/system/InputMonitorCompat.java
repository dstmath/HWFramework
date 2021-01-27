package com.android.systemui.shared.system;

import android.os.Bundle;
import android.os.Looper;
import android.view.Choreographer;
import android.view.InputMonitor;
import com.android.systemui.shared.system.InputChannelCompat;

public class InputMonitorCompat {
    private final InputMonitor mInputMonitor;

    private InputMonitorCompat(InputMonitor monitor) {
        this.mInputMonitor = monitor;
    }

    public void pilferPointers() {
        this.mInputMonitor.pilferPointers();
    }

    public void dispose() {
        this.mInputMonitor.dispose();
    }

    public InputChannelCompat.InputEventReceiver getInputReceiver(Looper looper, Choreographer choreographer, InputChannelCompat.InputEventListener listener) {
        return new InputChannelCompat.InputEventReceiver(this.mInputMonitor.getInputChannel(), looper, choreographer, listener);
    }

    public static InputMonitorCompat fromBundle(Bundle bundle, String key) {
        return new InputMonitorCompat(bundle.getParcelable(key));
    }
}
