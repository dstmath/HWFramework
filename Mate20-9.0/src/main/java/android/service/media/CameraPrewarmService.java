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
    /* access modifiers changed from: private */
    public boolean mCameraIntentFired;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                super.handleMessage(msg);
            } else {
                boolean unused = CameraPrewarmService.this.mCameraIntentFired = true;
            }
        }
    };

    public abstract void onCooldown(boolean z);

    public abstract void onPrewarm();

    public IBinder onBind(Intent intent) {
        if (!ACTION_PREWARM.equals(intent.getAction())) {
            return null;
        }
        onPrewarm();
        return new Messenger(this.mHandler).getBinder();
    }

    public boolean onUnbind(Intent intent) {
        if (ACTION_PREWARM.equals(intent.getAction())) {
            onCooldown(this.mCameraIntentFired);
        }
        return false;
    }
}
