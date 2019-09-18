package defpackage;

import android.content.Context;
import android.util.Log;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallManager;
import com.huawei.android.feature.module.DynamicModule;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.dynamicload.IPushManager;

/* renamed from: av  reason: default package */
public abstract class av {
    protected FeatureLocalInstallManager W;
    protected Context X;
    protected int ak = 0;

    public static void a(int i) {
        if (-10 == i) {
            Log.e("PushLogSys", "pushcore not exist");
        } else if (-11 == i) {
            Log.e("PushLogSys", "pushcore parse error");
        } else if (-12 == i) {
            Log.e("PushLogSys", "pushcore signature is error");
        } else if (-15 == i) {
            Log.e("PushLogSys", "pushcore version is invalid");
        } else if (-17 == i) {
            Log.e("PushLogSys", "copy pushcore to directory error");
        } else if (-18 == i) {
            Log.e("PushLogSys", "pushcore is installing, can not install now");
        } else if (-19 == i) {
            Log.e("PushLogSys", "pushcore path parse error");
        } else if (-21 == i || -22 == i) {
            Log.e("PushLogSys", "FeatureLocalInstallManager class loader error");
        } else if (-26 == i) {
            Log.e("PushLogSys", "feature directory do not exist");
        } else if (-100 == i) {
            Log.e("PushLogSys", "other internal error");
        }
    }

    private boolean h() {
        try {
            if (this.W.getInstallModules().contains("pushcore")) {
                DynamicModule dynamicModule = new DynamicModule("pushcore");
                aw awVar = new aw(this);
                if (!PushService.b().Y) {
                    IPushManager iPushManager = (IPushManager) dynamicModule.getClassInstance("com.huawei.android.pushagent.PushManagerImpl", awVar);
                    if (iPushManager != null) {
                        Log.i("PushLogSys", "start pushcore service");
                        iPushManager.startPushService(this.X);
                        PushService.b().Y = true;
                        PushService.b().aa = iPushManager;
                        return true;
                    }
                    Log.e("PushLogSys", "start pushcore service error");
                    return false;
                }
                Log.i("PushLogSys", "pushcore service is running");
                return true;
            }
            Log.e("PushLogSys", "pushcore apk not exist");
            return false;
        } catch (Exception e) {
            Log.e("PushLogSys", "load pushcore exception: " + bb.a(e));
            return false;
        }
    }

    public final void g() {
        for (int i = 0; i < 3; i++) {
            Log.i("PushLogSys", "try run push time is " + i);
            if (h()) {
                Log.i("PushLogSys", "run push once result is true");
                return;
            }
        }
    }
}
