package com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet;

import android.content.Context;
import com.huawei.secure.android.common.ssl.SecureSSLSocketFactory;
import com.huawei.wallet.sdk.business.buscard.model.ApplyPayOrderCallback;
import com.huawei.wallet.sdk.business.idcard.commonbase.util.log.LogErrorConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.impl.JSONHelper;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.NetworkUtil;
import com.huawei.wallet.sdk.common.utils.crypto.AES;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.conn.ssl.StrictHostnameVerifier;

public abstract class HttpConnTask<Result, RequestParams> {
    private static final String CONTENT_TYPE_APP_JSON = "application/json";
    private static final int DEFAULT_TIMEOUT = 30000;
    protected static final int ERROR_CODE_CONNECTION_FAILED = -2;
    protected static final int ERROR_CODE_NO_NETWORK = -1;
    protected static final int ERROR_CODE_PARAMS_ERROR = -3;
    private static final String TAG = "HttpConnTask";
    private int mConnTimeout = DEFAULT_TIMEOUT;
    protected Context mContext;
    private int mSocketTimeout = DEFAULT_TIMEOUT;
    private String mUrl;

    /* access modifiers changed from: protected */
    public abstract String prepareRequestStr(RequestParams requestparams);

    /* access modifiers changed from: protected */
    public abstract Result readErrorResponse(int i);

    /* access modifiers changed from: protected */
    public abstract Result readSuccessResponse(String str);

    public HttpConnTask(Context context, String url) {
        this.mContext = context;
        this.mUrl = url;
    }

    public HttpConnTask(Context context, String url, int connTimeout, int socketTimeout) {
        this.mContext = context;
        this.mUrl = url;
        this.mConnTimeout = connTimeout;
        this.mSocketTimeout = socketTimeout;
    }

    public Result processTask(RequestParams params) {
        Result result;
        int resultCode;
        if (!NetworkUtil.isNetworkConnected(this.mContext)) {
            LogC.d("HttpConnTask processTask, no network.", false);
            return readErrorResponse(-1);
        }
        String requestStr = prepareRequestStr(params);
        if (requestStr == null) {
            LogC.d("HttpConnTask processTask, invalid request params.", false);
            return readErrorResponse(-3);
        }
        HttpURLConnection conn = null;
        DataOutputStream outStream = null;
        InputStream is = null;
        ByteArrayOutputStream outputStream = null;
        try {
            URL url = new URL(this.mUrl);
            LogC.i("get url in Task:", false);
            if ("https".equals(url.getProtocol())) {
                conn = openHttpsConnection(url);
            } else {
                conn = openHttpConnection(url);
            }
            conn.setConnectTimeout(this.mConnTimeout);
            conn.setReadTimeout(this.mSocketTimeout);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE_APP_JSON);
            conn.setRequestProperty("Charset", AES.CHAR_ENCODING);
            conn.setRequestProperty("deviceId", PhoneDeviceUtil.getDeviceID(this.mContext));
            conn.setRequestProperty("bussiCertSign", JSONHelper.parseBussiCertSign(this.mContext, requestStr));
            conn.connect();
            outStream = new DataOutputStream(conn.getOutputStream());
            LogC.d("processTask request string : " + requestStr, true);
            outStream.write(requestStr.getBytes(AES.CHAR_ENCODING));
            outStream.flush();
            LogC.d(TAG, "processTask connection result code : " + resultCode, true);
            if (200 == resultCode) {
                is = conn.getInputStream();
                outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[ApplyPayOrderCallback.RETURN_FAILED_CARDINFO_PIN_LOCKED];
                while (true) {
                    int read = is.read(buffer);
                    int len = read;
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(buffer, 0, len);
                }
                String json = new String(outputStream.toByteArray(), AES.CHAR_ENCODING);
                StringBuilder sb = new StringBuilder();
                URL url2 = url;
                sb.append("HttpConnTask processTask json string : ");
                sb.append(json);
                LogC.d(sb.toString(), true);
                result = readSuccessResponse(json);
            } else {
                String err = "url:" + this.mUrl + " rc:" + resultCode;
                LogC.e("Service err. resultCode :" + resultCode + LogErrorConstant.NET_RESPONSE_OTHER_ERR + LogErrorConstant.getLocalAndErrMap("HttpConnTask.processTask", err), false);
                result = readErrorResponse(-2);
            }
        } catch (MalformedURLException e) {
            String err2 = "url:" + this.mUrl;
            LogC.e("processTask url invalid: " + this.mUrl + LogErrorConstant.NET_URI_FORMAT_ERR + LogErrorConstant.getLocalAndErrMap("HttpConnTask.processTask", err2), false);
            result = readErrorResponse(-3);
        } catch (NoSuchAlgorithmException noSuchAlgorithmExceptionEx) {
            LogC.e("processTask, NoSuchAlgorithmException : " + noSuchAlgorithmExceptionEx.getMessage(), true);
            result = readErrorResponse(-2);
        } catch (KeyManagementException keyManagementExceptionEx) {
            LogC.e("processTask, KeyManagementException : " + keyManagementExceptionEx.getMessage(), true);
            result = readErrorResponse(-2);
        } catch (IOException ioEx) {
            LogC.e("processTask IOException : " + ioEx.getMessage(), true);
            result = readErrorResponse(-2);
        } catch (IllegalAccessException ioEx2) {
            LogC.e("processTask IllegalAccessException : " + ioEx2.getMessage(), true);
            result = readErrorResponse(-2);
        } catch (CertificateException ioEx3) {
            LogC.e("processTask CertificateException : " + ioEx3.getMessage(), true);
            result = readErrorResponse(-2);
        } catch (KeyStoreException e2) {
            LogC.e("processTask KeyStoreException : " + e2.getMessage(), true);
            result = readErrorResponse(-2);
        } catch (Throwable result2) {
            closeStream(null, null, null, null);
            throw result2;
        }
        closeStream(outStream, is, outputStream, conn);
        return result;
    }

    private void closeStream(DataOutputStream outStream, InputStream is, ByteArrayOutputStream outputStream, HttpURLConnection conn) {
        if (outStream != null) {
            try {
                outStream.close();
            } catch (IOException e) {
                LogC.e("HttpConnTask processTask close stream error1.", false);
            }
        }
        if (is != null) {
            try {
                is.close();
            } catch (IOException e2) {
                LogC.e("HttpConnTask processTask close stream error2.", false);
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e3) {
                LogC.e("HttpConnTask processTask close stream error3.", false);
            }
        }
        if (conn != null) {
            conn.disconnect();
        }
    }

    private HttpURLConnection openHttpConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    private HttpsURLConnection openHttpsConnection(URL url) throws IOException, NoSuchAlgorithmException, KeyManagementException, CertificateException, IllegalAccessException, KeyStoreException {
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
        initHttpsConnection(httpsURLConnection);
        return httpsURLConnection;
    }

    private void initHttpsConnection(HttpsURLConnection httpsURLConnection) throws NoSuchAlgorithmException, KeyManagementException, CertificateException, IllegalAccessException, KeyStoreException, IOException {
        httpsURLConnection.setSSLSocketFactory(SecureSSLSocketFactory.getInstance(this.mContext));
        httpsURLConnection.setHostnameVerifier(new StrictHostnameVerifier());
    }
}
