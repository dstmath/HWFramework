package android.media;

import android.media.AudioRouting;
import android.os.Handler;

class NativeRoutingEventHandlerDelegate {
    /* access modifiers changed from: private */
    public AudioRouting mAudioRouting;
    private Handler mHandler;
    /* access modifiers changed from: private */
    public AudioRouting.OnRoutingChangedListener mOnRoutingChangedListener;

    NativeRoutingEventHandlerDelegate(AudioRouting audioRouting, AudioRouting.OnRoutingChangedListener listener, Handler handler) {
        this.mAudioRouting = audioRouting;
        this.mOnRoutingChangedListener = listener;
        this.mHandler = handler;
    }

    /* access modifiers changed from: package-private */
    public void notifyClient() {
        if (this.mHandler != null) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (NativeRoutingEventHandlerDelegate.this.mOnRoutingChangedListener != null) {
                        NativeRoutingEventHandlerDelegate.this.mOnRoutingChangedListener.onRoutingChanged(NativeRoutingEventHandlerDelegate.this.mAudioRouting);
                    }
                }
            });
        }
    }
}
