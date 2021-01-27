package defpackage;

import android.util.Log;
import com.huawei.android.feature.install.localinstall.IFeatureLocalInstall;
import com.huawei.android.pushagent.PushService;

/* access modifiers changed from: package-private */
/* renamed from: af  reason: default package */
public final class af implements IFeatureLocalInstall {
    final /* synthetic */ am M;
    final /* synthetic */ String N;
    final /* synthetic */ String O;
    final /* synthetic */ ae P;

    af(ae aeVar, am amVar, String str, String str2) {
        this.P = aeVar;
        this.M = amVar;
        this.N = str;
        this.O = str2;
    }

    @Override // com.huawei.android.feature.install.localinstall.IFeatureLocalInstall
    public final void onInstallFeatureBegin() {
        Log.i("PushLogSys", "begin install pushcore. curr time is " + this.P.L);
    }

    @Override // com.huawei.android.feature.install.localinstall.IFeatureLocalInstall
    public final void onInstallFeatureEnd() {
        Log.i("PushLogSys", "install pushcore end. curr time is " + this.P.L);
    }

    @Override // com.huawei.android.feature.install.localinstall.IFeatureLocalInstall
    public final void onInstallProgressUpdate(String str, int i) {
        Log.i("PushLogSys", "the module " + str + " install end. result is " + i + ". curr time is " + this.P.L);
        if (i == 0) {
            Log.i("PushLogSys", "install pushcore success");
            this.M.a("pushVersion", this.N);
            this.P.d();
        } else if (-13 == i) {
            Log.i("PushLogSys", "pushcore is lower version");
            this.P.d();
        } else if (-14 == i) {
            Log.i("PushLogSys", "install pushcore, local feature is using, stop progress to update");
            this.M.a("pushVersion", this.N);
            PushService.getInstance().exitProcess();
        } else {
            Log.e("PushLogSys", "handle install pushcore error");
            ae.a(i);
            this.P.L++;
            this.P.b(this.O, this.N);
        }
    }
}
