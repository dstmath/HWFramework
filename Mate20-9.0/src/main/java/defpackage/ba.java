package defpackage;

import android.util.Log;
import com.huawei.android.feature.install.localinstall.IFeatureLocalInstall;
import com.huawei.android.pushagent.PushService;

/* renamed from: ba  reason: default package */
final class ba implements IFeatureLocalInstall {
    final /* synthetic */ ay ao;

    ba(ay ayVar) {
        this.ao = ayVar;
    }

    public final void onInstallFeatureBegin() {
        Log.i("PushLogSys", "begin force install NC pushcore. curr time is " + this.ao.ak);
    }

    public final void onInstallFeatureEnd() {
        Log.i("PushLogSys", "force install NC pushcore end. curr time is " + this.ao.ak);
    }

    public final void onInstallProgressUpdate(String str, int i) {
        Log.i("PushLogSys", "the module NC " + str + " install end. result is " + i + ". curr time is " + this.ao.ak);
        if (i == 0) {
            Log.i("PushLogSys", "force install NC pushcore success");
            be.a(this.ao.X).a("pushVersion", "NC");
            this.ao.g();
        } else if (-13 == i) {
            Log.i("PushLogSys", "force install NC pushcore, it is lower version");
            this.ao.g();
        } else if (-14 == i) {
            Log.i("PushLogSys", "force install NC pushcore, local feature is using, stop progress to update");
            be.a(this.ao.X).a("pushVersion", "NC");
            PushService.b();
            PushService.d();
        } else {
            Log.e("PushLogSys", "handle force NC install pushcore error");
            ay.a(i);
            this.ao.ak++;
            this.ao.k();
        }
    }
}
