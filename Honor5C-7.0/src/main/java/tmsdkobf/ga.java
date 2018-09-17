package tmsdkobf;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.utils.d;
import tmsdkobf.lp.a;

/* compiled from: Unknown */
public class ga {
    private Context mContext;
    private NetworkInfo od;
    private List<a> oe;

    public ga(Context context) {
        this.oe = new ArrayList();
        this.mContext = context;
    }

    public NetworkInfo getActiveNetworkInfo() {
        NetworkInfo activeNetworkInfo;
        Exception e;
        try {
            activeNetworkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
            this.od = activeNetworkInfo;
            try {
                if (this.od != null) {
                    d.d("NetworkInfoManager", "network type:" + this.od.getType());
                }
            } catch (Exception e2) {
                e = e2;
                e.printStackTrace();
                return activeNetworkInfo;
            }
        } catch (Exception e3) {
            e = e3;
            activeNetworkInfo = null;
            e.printStackTrace();
            return activeNetworkInfo;
        }
        return activeNetworkInfo;
    }
}
