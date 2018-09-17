package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.net.NetworkInfo;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.creator.BaseManagerC;

/* compiled from: Unknown */
public class gc extends BaseManagerC implements lp {
    private gb of;
    private ga og;

    public py a(py pyVar, int i) {
        return null;
    }

    public void a(qj qjVar) {
    }

    public boolean aC(String str) {
        return false;
    }

    public py b(String str, int i) {
        return null;
    }

    public void b(qj qjVar) {
    }

    public int c(String str, int i) {
        return 0;
    }

    public ArrayList<py> c(int i, int i2) {
        return null;
    }

    public NetworkInfo getActiveNetworkInfo() {
        return this.og.getActiveNetworkInfo();
    }

    public PackageInfo getPackageInfo(String str, int i) {
        return null;
    }

    public void onCreate(Context context) {
        this.of = new gb();
        this.og = new ga(context);
    }

    public List<ResolveInfo> queryIntentServices(Intent intent, int i) {
        return null;
    }
}
