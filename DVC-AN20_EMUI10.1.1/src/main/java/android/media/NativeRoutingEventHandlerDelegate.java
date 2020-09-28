package android.media;

import android.media.AudioRouting;
import android.os.Handler;

/* access modifiers changed from: package-private */
public class NativeRoutingEventHandlerDelegate {
    private AudioRouting mAudioRouting;
    private Handler mHandler;
    private AudioRouting.OnRoutingChangedListener mOnRoutingChangedListener;

    NativeRoutingEventHandlerDelegate(AudioRouting audioRouting, AudioRouting.OnRoutingChangedListener listener, Handler handler) {
        this.mAudioRouting = audioRouting;
        this.mOnRoutingChangedListener = listener;
        this.mHandler = handler;
    }

    /* access modifiers changed from: package-private */
    public void notifyClient() {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.post(new Runnable() {
                /* class android.media.NativeRoutingEventHandlerDelegate.AnonymousClass1 */

                public void run() {
                    if (NativeRoutingEventHandlerDelegate.this.mOnRoutingChangedListener != null) {
                        NativeRoutingEventHandlerDelegate.this.mOnRoutingChangedListener.onRoutingChanged(NativeRoutingEventHandlerDelegate.this.mAudioRouting);
                    }
                }
            });
        }
    }
}
