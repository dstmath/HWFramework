package com.android.server.wm;

import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.MotionEvent;
import android.view.WindowManagerPolicy.PointerEventListener;
import com.android.server.UiThread;
import java.util.ArrayList;

public class PointerEventDispatcher extends InputEventReceiver {
    ArrayList<PointerEventListener> mListeners;
    PointerEventListener[] mListenersArray;

    public PointerEventDispatcher(InputChannel inputChannel) {
        super(inputChannel, UiThread.getHandler().getLooper());
        this.mListeners = new ArrayList();
        this.mListenersArray = new PointerEventListener[0];
    }

    public void onInputEvent(InputEvent event) {
        try {
            if ((event instanceof MotionEvent) && (event.getSource() & 2) != 0) {
                PointerEventListener[] listeners;
                MotionEvent motionEvent = (MotionEvent) event;
                synchronized (this.mListeners) {
                    if (this.mListenersArray == null) {
                        this.mListenersArray = new PointerEventListener[this.mListeners.size()];
                        this.mListeners.toArray(this.mListenersArray);
                    }
                    listeners = this.mListenersArray;
                }
                for (PointerEventListener onPointerEvent : listeners) {
                    onPointerEvent.onPointerEvent(motionEvent);
                }
            }
            finishInputEvent(event, false);
        } catch (Throwable th) {
            finishInputEvent(event, false);
        }
    }

    public void registerInputEventListener(PointerEventListener listener) {
        synchronized (this.mListeners) {
            if (this.mListeners.contains(listener)) {
                throw new IllegalStateException("registerInputEventListener: trying to register" + listener + " twice.");
            }
            this.mListeners.add(listener);
            this.mListenersArray = null;
        }
    }

    public void unregisterInputEventListener(PointerEventListener listener) {
        synchronized (this.mListeners) {
            if (this.mListeners.contains(listener)) {
                this.mListeners.remove(listener);
                this.mListenersArray = null;
            } else {
                throw new IllegalStateException("registerInputEventListener: " + listener + " not registered.");
            }
        }
    }
}
