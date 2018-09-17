package tmsdkobf;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.utils.f;
import tmsdkobf.kh.a;

public class gi {
    private Context mContext;
    private NetworkInfo oh;
    private List<a> oi = new ArrayList();

    public gi(Context context) {
        this.mContext = context;
    }

    public NetworkInfo getActiveNetworkInfo() {
        Exception e;
        NetworkInfo networkInfo = null;
        try {
            NetworkInfo activeNetworkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
            this.oh = activeNetworkInfo;
            try {
                if (this.oh != null) {
                    f.f("NetworkInfoManager", "network type:" + this.oh.getType());
                }
                return activeNetworkInfo;
            } catch (Exception e2) {
                e = e2;
                networkInfo = activeNetworkInfo;
                e.printStackTrace();
                return networkInfo;
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            return networkInfo;
        }
    }
}
