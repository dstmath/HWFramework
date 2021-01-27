package android.service.vr;

import android.annotation.UnsupportedAppUsage;
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
        /* class android.service.vr.VrListenerService.AnonymousClass1 */

        @Override // android.service.vr.IVrListener
        public void focusedActivityChanged(ComponentName component, boolean running2dInVr, int pid) {
            VrListenerService.this.mHandler.obtainMessage(1, running2dInVr ? 1 : 0, pid, component).sendToTarget();
        }
    };
    private final Handler mHandler = new VrListenerHandler(Looper.getMainLooper());

    private final class VrListenerHandler extends Handler {
        public VrListenerHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
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

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public void onCurrentVrActivityChanged(ComponentName component) {
    }

    @UnsupportedAppUsage
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
