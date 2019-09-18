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
import android.service.vr.IVrListener;

public abstract class VrListenerService extends Service {
    private static final int MSG_ON_CURRENT_VR_ACTIVITY_CHANGED = 1;
    public static final String SERVICE_INTERFACE = "android.service.vr.VrListenerService";
    private final IVrListener.Stub mBinder = new IVrListener.Stub() {
        public void focusedActivityChanged(ComponentName component, boolean running2dInVr, int pid) {
            VrListenerService.this.mHandler.obtainMessage(1, running2dInVr, pid, component).sendToTarget();
        }
    };
    /* access modifiers changed from: private */
    public final Handler mHandler = new VrListenerHandler(Looper.getMainLooper());

    private final class VrListenerHandler extends Handler {
        public VrListenerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            if (msg.what == 1) {
                VrListenerService vrListenerService = VrListenerService.this;
                ComponentName componentName = (ComponentName) msg.obj;
                if (msg.arg1 != 1) {
                    z = false;
                }
                vrListenerService.onCurrentVrActivityChanged(componentName, z, msg.arg2);
            }
        }
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public void onCurrentVrActivityChanged(ComponentName component) {
    }

    public void onCurrentVrActivityChanged(ComponentName component, boolean running2dInVr, int pid) {
        onCurrentVrActivityChanged(running2dInVr ? null : component);
    }

    public static final boolean isVrModePackageEnabled(Context context, ComponentName requestedComponent) {
        ActivityManager am = (ActivityManager) context.getSystemService(ActivityManager.class);
        if (am == null) {
            return false;
        }
        return am.isVrModePackageEnabled(requestedComponent);
    }
}
