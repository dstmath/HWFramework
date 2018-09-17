package tmsdkobf;

import android.os.Bundle;
import com.huawei.systemmanager.rainbow.comm.request.util.HsmRainbowConst;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/* compiled from: Unknown */
public class mt {
    protected HttpParams Bl;
    private boolean Bm;
    private String Bn;
    private int Bo;
    protected a Bp;

    /* compiled from: Unknown */
    public interface a {
        void a(Bundle bundle);

        void b(Bundle bundle);
    }

    public mt() {
        this.Bl = null;
        this.Bm = false;
        this.Bn = null;
        this.Bo = 0;
        this.Bp = null;
    }

    public void E(boolean z) {
        this.Bm = z;
    }

    protected void a(int i, Bundle bundle) {
        if (this.Bp != null) {
            if (i == 1) {
                this.Bp.a(bundle);
            } else if (i == 2) {
                this.Bp.b(bundle);
            }
        }
    }

    public void a(a aVar) {
        this.Bp = aVar;
    }

    protected HttpClient eZ() {
        if (this.Bl == null) {
            this.Bl = new BasicHttpParams();
        }
        HttpConnectionParams.setConnectionTimeout(this.Bl, 10000);
        HttpConnectionParams.setSoTimeout(this.Bl, HsmRainbowConst.NO_NEED_UPDATE);
        HttpConnectionParams.setSocketBufferSize(this.Bl, 4096);
        HttpClientParams.setRedirecting(this.Bl, true);
        HttpClient defaultHttpClient = new DefaultHttpClient(this.Bl);
        if (this.Bm) {
            defaultHttpClient.getParams().setParameter("http.route.default-proxy", new HttpHost(this.Bn, this.Bo));
        }
        return defaultHttpClient;
    }

    public void g(String str, int i) {
        this.Bn = str;
        this.Bo = i;
    }
}
