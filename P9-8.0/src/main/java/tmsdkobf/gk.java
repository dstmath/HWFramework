package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.net.NetworkInfo;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.creator.BaseManagerC;

public class gk extends BaseManagerC implements kh {
    private gj oj;
    private gi ok;

    public ov a(String str, int i) {
        return null;
    }

    public ov a(ov ovVar, int i) {
        return null;
    }

    public void a(pg pgVar) {
    }

    public boolean ai(String str) {
        return false;
    }

    public void b(pg pgVar) {
    }

    public ArrayList<ov> f(int i, int i2) {
        return null;
    }

    public NetworkInfo getActiveNetworkInfo() {
        return this.ok.getActiveNetworkInfo();
    }

    public PackageInfo getPackageInfo(String str, int i) {
        return null;
    }

    public void onCreate(Context context) {
        this.oj = new gj();
        this.ok = new gi(context);
    }

    public List<ResolveInfo> queryIntentServices(Intent intent, int i) {
        return null;
    }
}
