package tmsdkobf;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.net.NetworkInfo;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.utils.f;

public class gl implements kh {
    private long mr;
    private gk ol = ((gk) ManagerCreatorC.getManager(gk.class));

    public gl(long j) {
        this.mr = j;
    }

    public ov a(String str, int i) {
        f.f("PhoneSystemInfoServiceProxy", "Id = " + this.mr + "|getAppInfo pkg=" + str + " flag=" + i);
        return this.ol.a(str, i);
    }

    public ov a(ov ovVar, int i) {
        f.f("PhoneSystemInfoServiceProxy", "Id = " + this.mr + "|getAppInfo2 flag=" + i);
        return this.ol.a(ovVar, i);
    }

    public void a(pg pgVar) {
        f.f("PhoneSystemInfoServiceProxy", "Id = " + this.mr + "|addPackageChangeListener");
        this.ol.a(pgVar);
    }

    public boolean ai(String str) {
        f.f("PhoneSystemInfoServiceProxy", "Id = " + this.mr + "|isPackageInstalled pkg=" + str);
        return this.ol.ai(str);
    }

    public void b(pg pgVar) {
        f.f("PhoneSystemInfoServiceProxy", "Id = " + this.mr + "|removePackageChangeListener");
        this.ol.b(pgVar);
    }

    public ArrayList<ov> f(int i, int i2) {
        f.f("PhoneSystemInfoServiceProxy", "Id = " + this.mr + "|getInstalledApp");
        return this.ol.f(i, i2);
    }

    public NetworkInfo getActiveNetworkInfo() {
        f.f("PhoneSystemInfoServiceProxy", "Id = " + this.mr + "|getActiveNetworkInfo");
        return this.ol.getActiveNetworkInfo();
    }

    public PackageInfo getPackageInfo(String str, int i) {
        f.f("PhoneSystemInfoServiceProxy", "Id = " + this.mr + "|getPackageInfo pkg=" + str);
        return this.ol.getPackageInfo(str, i);
    }

    public List<ResolveInfo> queryIntentServices(Intent intent, int i) {
        f.f("PhoneSystemInfoServiceProxy", "Id = " + this.mr + "|queryIntentServices");
        return this.ol.queryIntentServices(intent, i);
    }
}
