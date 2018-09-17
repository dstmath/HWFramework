package android.service.vr;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.service.vr.IVrListener.Stub;

public abstract class VrListenerService extends Service {
    private static final int MSG_ON_CURRENT_VR_ACTIVITY_CHANGED = 1;
    public static final String SERVICE_INTERFACE = "android.service.vr.VrListenerService";
    private final Stub mBinder = new Stub() {
        public void focusedActivityChanged(ComponentName component) {
            VrListenerService.this.mHandler.obtainMessage(1, component).sendToTarget();
        }
    };
    private final Handler mHandler = new VrListenerHandler(Looper.getMainLooper());

    private final class VrListenerHandler extends Handler {
        public VrListenerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    VrListenerService.this.onCurrentVrActivityChanged((ComponentName) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public void onCurrentVrActivityChanged(ComponentName component) {
    }

    public static final boolean isVrModePackageEnabled(Context context, ComponentName requestedComponent) {
        ActivityManager am = (ActivityManager) context.getSystemService(ActivityManager.class);
        if (am == null) {
            return false;
        }
        return am.isVrModePackageEnabled(requestedComponent);
    }
}
