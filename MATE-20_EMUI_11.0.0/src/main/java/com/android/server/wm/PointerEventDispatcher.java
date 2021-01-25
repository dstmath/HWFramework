package com.android.server.wm;

import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.MotionEvent;
import android.view.WindowManagerPolicyConstants;
import com.android.server.UiThread;
import java.util.ArrayList;

public class PointerEventDispatcher extends InputEventReceiver {
    private final InputChannel mInputChannel;
    private boolean mIsDispose = false;
    private final ArrayList<WindowManagerPolicyConstants.PointerEventListener> mListeners = new ArrayList<>();
    private WindowManagerPolicyConstants.PointerEventListener[] mListenersArray = new WindowManagerPolicyConstants.PointerEventListener[0];

    public PointerEventDispatcher(InputChannel inputChannel) {
        super(inputChannel, UiThread.getHandler().getLooper());
        this.mInputChannel = inputChannel;
    }

    public void onInputEvent(InputEvent event) {
        WindowManagerPolicyConstants.PointerEventListener[] listeners;
        try {
            if ((event instanceof MotionEvent) && (event.getSource() & 2) != 0) {
                MotionEvent motionEvent = (MotionEvent) event;
                synchronized (this.mListeners) {
                    if (this.mListenersArray == null) {
                        this.mListenersArray = new WindowManagerPolicyConstants.PointerEventListener[this.mListeners.size()];
                        this.mListeners.toArray(this.mListenersArray);
                    }
                    listeners = this.mListenersArray;
                }
                for (WindowManagerPolicyConstants.PointerEventListener pointerEventListener : listeners) {
                    pointerEventListener.onPointerEvent(motionEvent);
                }
            }
        } finally {
            finishInputEvent(event, false);
        }
    }

    public void registerInputEventListener(WindowManagerPolicyConstants.PointerEventListener listener) {
        synchronized (this.mListeners) {
            if (!this.mListeners.contains(listener)) {
                this.mListeners.add(listener);
                this.mListenersArray = null;
            } else {
                throw new IllegalStateException("registerInputEventListener: trying to register" + listener + " twice.");
            }
        }
    }

    public void unregisterInputEventListener(WindowManagerPolicyConstants.PointerEventListener listener) {
        synchronized (this.mListeners) {
            if (!this.mIsDispose) {
                if (this.mListeners.contains(listener)) {
                    this.mListeners.remove(listener);
                    this.mListenersArray = null;
                    return;
                }
                throw new IllegalStateException("registerInputEventListener: " + listener + " not registered.");
            }
        }
    }

    public void dispose() {
        PointerEventDispatcher.super.dispose();
        this.mInputChannel.dispose();
        synchronized (this.mListeners) {
            this.mListeners.clear();
            this.mListenersArray = null;
            this.mIsDispose = true;
        }
    }
}
