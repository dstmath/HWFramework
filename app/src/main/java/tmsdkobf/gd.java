package tmsdkobf;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.net.NetworkInfo;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class gd implements lp {
    private long lU;
    private gc oh;

    public gd(long j) {
        this.lU = j;
        this.oh = (gc) ManagerCreatorC.getManager(gc.class);
    }

    public py a(py pyVar, int i) {
        d.d("PhoneSystemInfoServiceProxy", "Id = " + this.lU + "|getAppInfo2 flag=" + i);
        return this.oh.a(pyVar, i);
    }

    public void a(qj qjVar) {
        d.d("PhoneSystemInfoServiceProxy", "Id = " + this.lU + "|addPackageChangeListener");
        this.oh.a(qjVar);
    }

    public boolean aC(String str) {
        d.d("PhoneSystemInfoServiceProxy", "Id = " + this.lU + "|isPackageInstalled pkg=" + str);
        return this.oh.aC(str);
    }

    public py b(String str, int i) {
        d.d("PhoneSystemInfoServiceProxy", "Id = " + this.lU + "|getAppInfo pkg=" + str + " flag=" + i);
        return this.oh.b(str, i);
    }

    public void b(qj qjVar) {
        d.d("PhoneSystemInfoServiceProxy", "Id = " + this.lU + "|removePackageChangeListener");
        this.oh.b(qjVar);
    }

    public int c(String str, int i) {
        d.d("PhoneSystemInfoServiceProxy", "Id = " + this.lU + "|getAppVersionStatus pkg=" + str);
        return this.oh.c(str, i);
    }

    public ArrayList<py> c(int i, int i2) {
        d.d("PhoneSystemInfoServiceProxy", "Id = " + this.lU + "|getInstalledApp");
        return this.oh.c(i, i2);
    }

    public NetworkInfo getActiveNetworkInfo() {
        d.d("PhoneSystemInfoServiceProxy", "Id = " + this.lU + "|getActiveNetworkInfo");
        return this.oh.getActiveNetworkInfo();
    }

    public PackageInfo getPackageInfo(String str, int i) {
        d.d("PhoneSystemInfoServiceProxy", "Id = " + this.lU + "|getPackageInfo pkg=" + str);
        return this.oh.getPackageInfo(str, i);
    }

    public List<ResolveInfo> queryIntentServices(Intent intent, int i) {
        d.d("PhoneSystemInfoServiceProxy", "Id = " + this.lU + "|queryIntentServices");
        return this.oh.queryIntentServices(intent, i);
    }
}
