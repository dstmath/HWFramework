package defpackage;

import android.util.Log;
import com.huawei.android.feature.install.localinstall.IFeatureLocalInstall;
import com.huawei.android.pushagent.PushService;

/* renamed from: au  reason: default package */
final class au implements IFeatureLocalInstall {
    final /* synthetic */ at aj;

    au(at atVar) {
        this.aj = atVar;
    }

    public final void onInstallFeatureBegin() {
        Log.i("PushLogSys", "begin install HMS pushcore. curr time is " + this.aj.time);
    }

    public final void onInstallFeatureEnd() {
        Log.i("PushLogSys", "install HMS pushcore end. curr time is " + this.aj.time);
    }

    public final void onInstallProgressUpdate(String str, int i) {
        Log.i("PushLogSys", "the module HMS " + str + " install end. result is " + i + ". curr time is " + this.aj.time);
        if (i == 0) {
            Log.i("PushLogSys", "install HMS pushcore success");
            be.a(this.aj.X).a("pushVersion", "HMS");
            this.aj.g();
        } else if (-13 == i) {
            Log.i("PushLogSys", "HMS pushcore is lower version");
            int unused = this.aj.time = this.aj.time + 1;
            this.aj.f();
        } else if (-14 == i) {
            Log.i("PushLogSys", "install HMS pushcore, local feature is using, stop progress to update");
            be.a(this.aj.X).a("pushVersion", "HMS");
            PushService.b();
            PushService.d();
        } else {
            Log.e("PushLogSys", "handle HMS install pushcore error");
            at.a(i);
            int unused2 = this.aj.time = this.aj.time + 1;
            this.aj.f();
        }
    }
}
