package com.android.server.appwidget;

import android.content.Context;
import android.util.Log;
import com.android.server.AppWidgetBackupBridge;
import com.android.server.FgThread;
import com.android.server.SystemService;
import com.android.server.appwidget.HwAppWidgetServiceFactory;

public class AppWidgetService extends SystemService {
    private static final String TAG = "AppWidgetService";
    private final AppWidgetServiceImpl mImpl;

    public AppWidgetService(Context context) {
        super(context);
        HwAppWidgetServiceFactory.IHwAppWidgetService iAppWidgetMS = HwAppWidgetServiceFactory.getHwAppWidgetService();
        if (iAppWidgetMS != null) {
            this.mImpl = iAppWidgetMS.getAppWidgetImpl(context);
        } else {
            this.mImpl = new AppWidgetServiceImpl(context);
        }
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.appwidget.AppWidgetServiceImpl, android.os.IBinder] */
    public void onStart() {
        this.mImpl.onStart();
        publishBinderService("appwidget", this.mImpl);
        AppWidgetBackupBridge.register(this.mImpl);
    }

    public void onBootPhase(int phase) {
        if (phase == 550) {
            Log.d(TAG, "onBootPhase=600");
            this.mImpl.setSafeMode(isSafeMode());
        }
    }

    public void onUnlockUser(int userHandle) {
        FgThread.getHandler().post(new Runnable(userHandle) {
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                AppWidgetService.this.mImpl.onUserUnlocked(this.f$1);
            }
        });
    }

    public void onStopUser(int userHandle) {
        this.mImpl.onUserStopped(userHandle);
    }

    public void onSwitchUser(int userHandle) {
        this.mImpl.reloadWidgetsMaskedStateForGroup(userHandle);
    }
}
