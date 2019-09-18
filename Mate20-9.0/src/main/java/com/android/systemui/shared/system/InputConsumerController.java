package com.android.systemui.shared.system;

import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.BatchedInputEventReceiver;
import android.view.Choreographer;
import android.view.IWindowManager;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.WindowManagerGlobal;
import java.io.PrintWriter;

public class InputConsumerController {
    private static final String TAG = InputConsumerController.class.getSimpleName();
    private InputEventReceiver mInputEventReceiver;
    /* access modifiers changed from: private */
    public TouchListener mListener;
    private final String mName;
    private RegistrationListener mRegistrationListener;
    private final IBinder mToken = new Binder();
    private final IWindowManager mWindowManager;

    private final class InputEventReceiver extends BatchedInputEventReceiver {
        public InputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper, Choreographer.getSfInstance());
        }

        public void onInputEvent(InputEvent event, int displayId) {
            boolean handled = true;
            try {
                if (InputConsumerController.this.mListener != null && (event instanceof MotionEvent)) {
                    handled = InputConsumerController.this.mListener.onTouchEvent((MotionEvent) event);
                }
            } finally {
                finishInputEvent(event, handled);
            }
        }
    }

    public interface RegistrationListener {
        void onRegistrationChanged(boolean z);
    }

    public interface TouchListener {
        boolean onTouchEvent(MotionEvent motionEvent);
    }

    public InputConsumerController(IWindowManager windowManager, String name) {
        this.mWindowManager = windowManager;
        this.mName = name;
    }

    public static InputConsumerController getPipInputConsumer() {
        return new InputConsumerController(WindowManagerGlobal.getWindowManagerService(), "pip_input_consumer");
    }

    public static InputConsumerController getRecentsAnimationInputConsumer() {
        return new InputConsumerController(WindowManagerGlobal.getWindowManagerService(), "recents_animation_input_consumer");
    }

    public void setTouchListener(TouchListener listener) {
        this.mListener = listener;
    }

    public void setRegistrationListener(RegistrationListener listener) {
        this.mRegistrationListener = listener;
        if (this.mRegistrationListener != null) {
            this.mRegistrationListener.onRegistrationChanged(this.mInputEventReceiver != null);
        }
    }

    public boolean isRegistered() {
        return this.mInputEventReceiver != null;
    }

    public void registerInputConsumer() {
        if (this.mInputEventReceiver == null) {
            InputChannel inputChannel = new InputChannel();
            try {
                this.mWindowManager.destroyInputConsumer(this.mName);
                this.mWindowManager.createInputConsumer(this.mToken, this.mName, inputChannel);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to create input consumer", e);
            }
            this.mInputEventReceiver = new InputEventReceiver(inputChannel, Looper.myLooper());
            if (this.mRegistrationListener != null) {
                this.mRegistrationListener.onRegistrationChanged(true);
            }
        }
    }

    public void unregisterInputConsumer() {
        if (this.mInputEventReceiver != null) {
            try {
                this.mWindowManager.destroyInputConsumer(this.mName);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to destroy input consumer", e);
            }
            this.mInputEventReceiver.dispose();
            this.mInputEventReceiver = null;
            if (this.mRegistrationListener != null) {
                this.mRegistrationListener.onRegistrationChanged(false);
            }
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + TAG);
        StringBuilder sb = new StringBuilder();
        sb.append(prefix + "  ");
        sb.append("registered=");
        sb.append(this.mInputEventReceiver != null);
        pw.println(sb.toString());
    }
}
