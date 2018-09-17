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

public class lv {
    protected HttpParams yW = null;
    private boolean yX = false;
    private String yY = null;
    private int yZ = 0;
    protected a za = null;

    public interface a {
        void a(Bundle bundle);

        void b(Bundle bundle);
    }

    protected void a(int i, Bundle bundle) {
        if (this.za != null) {
            if (i == 1) {
                this.za.a(bundle);
            } else if (i == 2) {
                this.za.b(bundle);
            }
        }
    }

    public void a(a aVar) {
        this.za = aVar;
    }

    public void d(String str, int i) {
        this.yY = str;
        this.yZ = i;
    }

    protected HttpClient eH() {
        if (this.yW == null) {
            this.yW = new BasicHttpParams();
        }
        HttpConnectionParams.setConnectionTimeout(this.yW, 10000);
        HttpConnectionParams.setSoTimeout(this.yW, HsmRainbowConst.NO_NEED_UPDATE);
        HttpConnectionParams.setSocketBufferSize(this.yW, 4096);
        HttpClientParams.setRedirecting(this.yW, true);
        HttpClient defaultHttpClient = new DefaultHttpClient(this.yW);
        if (this.yX) {
            defaultHttpClient.getParams().setParameter("http.route.default-proxy", new HttpHost(this.yY, this.yZ));
        }
        return defaultHttpClient;
    }

    public void u(boolean z) {
        this.yX = z;
    }
}
