package tmsdkobf;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.net.NetworkInfo;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public interface qd {
    py a(py pyVar, int i);

    boolean aC(String str);

    py b(String str, int i);

    int c(String str, int i);

    ArrayList<py> c(int i, int i2);

    NetworkInfo getActiveNetworkInfo();

    PackageInfo getPackageInfo(String str, int i);

    List<ResolveInfo> queryIntentServices(Intent intent, int i);
}
