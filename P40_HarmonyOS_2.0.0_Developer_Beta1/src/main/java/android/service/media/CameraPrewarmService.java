package android.service.media;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public abstract class CameraPrewarmService extends Service {
    public static final String ACTION_PREWARM = "android.service.media.CameraPrewarmService.ACTION_PREWARM";
    public static final int MSG_CAMERA_FIRED = 1;
    private boolean mCameraIntentFired;
    private final Handler mHandler = new Handler() {
        /* class android.service.media.CameraPrewarmService.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                super.handleMessage(msg);
            } else {
                CameraPrewarmService.this.mCameraIntentFired = true;
            }
        }
    };

    public abstract void onCooldown(boolean z);

    public abstract void onPrewarm();

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        if (!ACTION_PREWARM.equals(intent.getAction())) {
            return null;
        }
        onPrewarm();
        return new Messenger(this.mHandler).getBinder();
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        if (!ACTION_PREWARM.equals(intent.getAction())) {
            return false;
        }
        onCooldown(this.mCameraIntentFired);
        return false;
    }
}
