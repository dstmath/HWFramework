package com.android.systemui.shared.system;

import android.os.Bundle;
import android.os.Looper;
import android.view.BatchedInputEventReceiver;
import android.view.Choreographer;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventSender;
import android.view.MotionEvent;

public class InputChannelCompat {

    public interface InputEventListener {
        void onInputEvent(InputEvent inputEvent);
    }

    public static InputEventReceiver fromBundle(Bundle params, String key, Looper looper, Choreographer choreographer, InputEventListener listener) {
        return new InputEventReceiver(params.getParcelable(key), looper, choreographer, listener);
    }

    public static boolean mergeMotionEvent(MotionEvent src, MotionEvent target) {
        return target.addBatch(src);
    }

    public static class InputEventReceiver {
        private final InputChannel mInputChannel;
        private final BatchedInputEventReceiver mReceiver;

        public InputEventReceiver(InputChannel inputChannel, Looper looper, Choreographer choreographer, final InputEventListener listener) {
            this.mInputChannel = inputChannel;
            this.mReceiver = new BatchedInputEventReceiver(inputChannel, looper, choreographer) {
                /* class com.android.systemui.shared.system.InputChannelCompat.InputEventReceiver.AnonymousClass1 */

                public void onInputEvent(InputEvent event) {
                    listener.onInputEvent(event);
                    finishInputEvent(event, true);
                }
            };
        }

        public void dispose() {
            this.mReceiver.dispose();
            this.mInputChannel.dispose();
        }
    }

    public static class InputEventDispatcher {
        private final InputChannel mInputChannel;
        private final InputEventSender mSender;

        public InputEventDispatcher(InputChannel inputChannel, Looper looper) {
            this.mInputChannel = inputChannel;
            this.mSender = new InputEventSender(inputChannel, looper) {
                /* class com.android.systemui.shared.system.InputChannelCompat.InputEventDispatcher.AnonymousClass1 */
            };
        }

        public void dispatch(InputEvent ev) {
            this.mSender.sendInputEvent(ev.getSequenceNumber(), ev);
        }

        public void dispose() {
            this.mSender.dispose();
            this.mInputChannel.dispose();
        }
    }
}
