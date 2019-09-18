package com.android.server.security.tsmagent.server;

import android.content.Context;
import com.android.server.security.tsmagent.server.CardServerBaseRequest;
import com.android.server.security.tsmagent.server.CardServerBaseResponse;
import com.android.server.security.tsmagent.server.wallet.PayX509TrustManager;
import com.android.server.security.tsmagent.utils.HwLog;
import com.android.server.security.tsmagent.utils.WalletSSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.json.JSONObject;

public abstract class HttpConnectionBase<Result extends CardServerBaseResponse, RequestParams extends CardServerBaseRequest> {
    public static final int DEFAULT_TIMEOUT = 30000;
    public static final int ERROR_CODE_CONNECTION_FAILED = -2;
    public static final int ERROR_CODE_NO_NETWORK = -1;
    public static final int ERROR_CODE_PARAMS_ERROR = -3;
    public static final int ERROR_CODE_SERVER_OVERLOAD = -4;
    public static final int HTTP_SC_OK = 200;
    public static final int SERVER_OVERLOAD_ERRORCODE = 503;
    protected int mConnTimeout = 30000;
    protected Context mContext;
    protected int mSocketTimeout = 30000;
    protected String mUrl;

    /* access modifiers changed from: protected */
    public abstract String prepareRequestStr(RequestParams requestparams);

    /* access modifiers changed from: protected */
    public abstract Result readErrorResponse(int i);

    /* access modifiers changed from: protected */
    public abstract CardServerBaseResponse readSuccessResponse(int i, String str, JSONObject jSONObject);

    /* access modifiers changed from: protected */
    public void closeStream(DataOutputStream outStream, InputStream is, ByteArrayOutputStream outputStream, HttpURLConnection conn) {
        if (outStream != null) {
            try {
                outStream.close();
            } catch (IOException e) {
                HwLog.e("processTask close stream error1.");
            }
        }
        if (is != null) {
            try {
                is.close();
            } catch (IOException e2) {
                HwLog.e("processTask close stream error2.");
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e3) {
                HwLog.e("processTask close stream error3.");
            }
        }
        if (conn != null) {
            conn.disconnect();
        }
    }

    /* access modifiers changed from: protected */
    public HttpsURLConnection openHttpsConnection(URL url) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
        initHttpsConnection(httpsURLConnection);
        return httpsURLConnection;
    }

    /* access modifiers changed from: protected */
    public HttpURLConnection openHttpConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    /* access modifiers changed from: protected */
    public void initHttpsConnection(HttpsURLConnection httpsURLConnection) throws NoSuchAlgorithmException, KeyManagementException {
        httpsURLConnection.setSSLSocketFactory(new WalletSSLSocketFactory(new PayX509TrustManager(this.mContext)));
        httpsURLConnection.setHostnameVerifier(new StrictHostnameVerifier());
    }
}
