package defpackage;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.feature.install.InstallRequest;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallManager;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallRequest;
import com.huawei.android.feature.module.DynamicModule;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.dynamicload.IPushManager;

/* renamed from: ae  reason: default package */
public abstract class ae {
    protected int L = 0;
    protected Context context;
    protected FeatureLocalInstallManager mInstallManager;

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

    /* access modifiers changed from: private */
    public void b(String str, String str2) {
        if (this.L >= 3 || this.L < 0) {
            Log.e("PushLogSys", "install pushcore error, try to load local pushcore");
            d();
            return;
        }
        Log.i("PushLogSys", "the path of pushcore is ".concat(String.valueOf(str)));
        Handler handler = new Handler();
        InstallRequest.Builder newBuilder = InstallRequest.newBuilder();
        this.mInstallManager.startInstall(ao.i() == 0 ? newBuilder.addModule(new FeatureLocalInstallRequest("pushcore", str)).build() : ao.h() ? newBuilder.addModule(new FeatureLocalInstallRequest("pushcore", str, "1E3EEE2A88A6DF75FB4AF56ADC8373BB818F3CB90A4935C7821582B8CEBB694C")).build() : newBuilder.addModule(new FeatureLocalInstallRequest("pushcore", str)).build(), new af(this, new am(this.context), str2, str), handler);
    }

    private boolean e() {
        try {
            if (this.mInstallManager.getInstallModules().contains("pushcore")) {
                DynamicModule dynamicModule = new DynamicModule("pushcore");
                ag agVar = new ag(this);
                if (!PushService.getInstance().isPushServiceRunning()) {
                    IPushManager iPushManager = (IPushManager) dynamicModule.getClassInstance("com.huawei.android.pushagent.PushManagerImpl", agVar);
                    if (iPushManager != null) {
                        Log.i("PushLogSys", "start pushcore service");
                        iPushManager.startPushService(this.context);
                        PushService.getInstance().setPushServiceRunning(true);
                        PushService.getInstance().setPushCoreManager(iPushManager);
                        PushService.getInstance().pushCoreManageEnd();
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
            Log.e("PushLogSys", "load pushcore exception: " + ao.a(e));
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public final long a(String str, String str2, String str3) {
        Bundle bundle;
        try {
            ApplicationInfo applicationInfo = this.context.getPackageManager().getApplicationInfo(str, 128);
            if (applicationInfo == null || (bundle = applicationInfo.metaData) == null) {
                return -1;
            }
            String string = bundle.getString(str3);
            if (TextUtils.isEmpty(string) || string.length() <= str2.length()) {
                return -1;
            }
            return Long.parseLong(string.substring(str2.length()));
        } catch (Exception e) {
            Log.e("PushLogSys", "get meta push version code error: " + ao.a(e));
            return -1;
        }
    }

    public final void a(String str, String str2) {
        this.L = 0;
        b(str, str2);
    }

    public final void d() {
        for (int i = 0; i < 3; i++) {
            Log.i("PushLogSys", "try run push time is ".concat(String.valueOf(i)));
            if (e()) {
                Log.i("PushLogSys", "run push once result is true");
                return;
            }
        }
    }
}
