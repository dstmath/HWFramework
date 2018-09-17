package tmsdkobf;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.net.NetworkInfo;
import java.util.ArrayList;
import java.util.List;

public interface pa {
    ov a(String str, int i);

    ov a(ov ovVar, int i);

    boolean ai(String str);

    ArrayList<ov> f(int i, int i2);

    NetworkInfo getActiveNetworkInfo();

    PackageInfo getPackageInfo(String str, int i);

    List<ResolveInfo> queryIntentServices(Intent intent, int i);
}
