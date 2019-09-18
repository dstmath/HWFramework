package com.huawei.wallet.sdk.business.idcard.idcard.server.card;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.secure.android.common.ssl.SecureSSLSocketFactory;
import com.huawei.wallet.sdk.business.buscard.model.ApplyPayOrderCallback;
import com.huawei.wallet.sdk.business.idcard.idcard.constant.ServerAddressConstant;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.ServerAccessBaseResponse;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json.JSONHelper;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.response.CardServerBaseResponse;
import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;
import com.huawei.wallet.sdk.common.utils.IOUtils;
import com.huawei.wallet.sdk.common.utils.NetworkUtil;
import com.huawei.wallet.sdk.common.utils.crypto.AES;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class NewHttpConnTask<Result, RequestParams> {
    private static final int DEFAULT_TIMEOUT = 30000;
    private static final int SERVER_OVERLOAD_ERRORCODE = 503;
    private int mConnTimeout = DEFAULT_TIMEOUT;
    protected Context mContext;
    private int mSocketTimeout = DEFAULT_TIMEOUT;
    private final String mUrl;

    /* access modifiers changed from: protected */
    public abstract String prepareRequestStr(RequestParams requestparams);

    /* access modifiers changed from: protected */
    public abstract Result readErrorResponse(int i, String str);

    /* access modifiers changed from: protected */
    public abstract Result readSuccessResponse(int i, String str, JSONObject jSONObject);

    public NewHttpConnTask(Context context, String url) {
        this.mContext = context;
        this.mUrl = url;
    }

    private HttpURLConnection openConnection(URL url) throws IOException, NoSuchAlgorithmException, KeyManagementException, CertificateException, IllegalAccessException, KeyStoreException {
        HttpURLConnection conn;
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
        if (this.mUrl.contains(ServerAddressConstant.IDCARD_CMD_DELAPP)) {
            conn.setRequestMethod("PUT");
        } else {
            conn.setRequestMethod("POST");
        }
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Charset", AES.CHAR_ENCODING);
        return conn;
    }

    /* JADX WARNING: Removed duplicated region for block: B:101:0x02ba A[Catch:{ MalformedURLException -> 0x0315, NoSuchAlgorithmException -> 0x02d0, KeyManagementException -> 0x028a, IOException -> 0x0244, CertificateException -> 0x01fe, IllegalAccessException -> 0x01b8, KeyStoreException -> 0x0172, all -> 0x016d, all -> 0x034a }] */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x02bf A[Catch:{ MalformedURLException -> 0x0315, NoSuchAlgorithmException -> 0x02d0, KeyManagementException -> 0x028a, IOException -> 0x0244, CertificateException -> 0x01fe, IllegalAccessException -> 0x01b8, KeyStoreException -> 0x0172, all -> 0x016d, all -> 0x034a }] */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0300 A[Catch:{ MalformedURLException -> 0x0315, NoSuchAlgorithmException -> 0x02d0, KeyManagementException -> 0x028a, IOException -> 0x0244, CertificateException -> 0x01fe, IllegalAccessException -> 0x01b8, KeyStoreException -> 0x0172, all -> 0x016d, all -> 0x034a }] */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x0305 A[Catch:{ MalformedURLException -> 0x0315, NoSuchAlgorithmException -> 0x02d0, KeyManagementException -> 0x028a, IOException -> 0x0244, CertificateException -> 0x01fe, IllegalAccessException -> 0x01b8, KeyStoreException -> 0x0172, all -> 0x016d, all -> 0x034a }] */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x0331 A[Catch:{ MalformedURLException -> 0x0315, NoSuchAlgorithmException -> 0x02d0, KeyManagementException -> 0x028a, IOException -> 0x0244, CertificateException -> 0x01fe, IllegalAccessException -> 0x01b8, KeyStoreException -> 0x0172, all -> 0x016d, all -> 0x034a }] */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x0336 A[Catch:{ MalformedURLException -> 0x0315, NoSuchAlgorithmException -> 0x02d0, KeyManagementException -> 0x028a, IOException -> 0x0244, CertificateException -> 0x01fe, IllegalAccessException -> 0x01b8, KeyStoreException -> 0x0172, all -> 0x016d, all -> 0x034a }] */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x01a2 A[Catch:{ MalformedURLException -> 0x0315, NoSuchAlgorithmException -> 0x02d0, KeyManagementException -> 0x028a, IOException -> 0x0244, CertificateException -> 0x01fe, IllegalAccessException -> 0x01b8, KeyStoreException -> 0x0172, all -> 0x016d, all -> 0x034a }] */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x01a7 A[Catch:{ MalformedURLException -> 0x0315, NoSuchAlgorithmException -> 0x02d0, KeyManagementException -> 0x028a, IOException -> 0x0244, CertificateException -> 0x01fe, IllegalAccessException -> 0x01b8, KeyStoreException -> 0x0172, all -> 0x016d, all -> 0x034a }] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x01e8 A[Catch:{ MalformedURLException -> 0x0315, NoSuchAlgorithmException -> 0x02d0, KeyManagementException -> 0x028a, IOException -> 0x0244, CertificateException -> 0x01fe, IllegalAccessException -> 0x01b8, KeyStoreException -> 0x0172, all -> 0x016d, all -> 0x034a }] */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x01ed A[Catch:{ MalformedURLException -> 0x0315, NoSuchAlgorithmException -> 0x02d0, KeyManagementException -> 0x028a, IOException -> 0x0244, CertificateException -> 0x01fe, IllegalAccessException -> 0x01b8, KeyStoreException -> 0x0172, all -> 0x016d, all -> 0x034a }] */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x022e A[Catch:{ MalformedURLException -> 0x0315, NoSuchAlgorithmException -> 0x02d0, KeyManagementException -> 0x028a, IOException -> 0x0244, CertificateException -> 0x01fe, IllegalAccessException -> 0x01b8, KeyStoreException -> 0x0172, all -> 0x016d, all -> 0x034a }] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0233 A[Catch:{ MalformedURLException -> 0x0315, NoSuchAlgorithmException -> 0x02d0, KeyManagementException -> 0x028a, IOException -> 0x0244, CertificateException -> 0x01fe, IllegalAccessException -> 0x01b8, KeyStoreException -> 0x0172, all -> 0x016d, all -> 0x034a }] */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x0274 A[Catch:{ MalformedURLException -> 0x0315, NoSuchAlgorithmException -> 0x02d0, KeyManagementException -> 0x028a, IOException -> 0x0244, CertificateException -> 0x01fe, IllegalAccessException -> 0x01b8, KeyStoreException -> 0x0172, all -> 0x016d, all -> 0x034a }] */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x0279 A[Catch:{ MalformedURLException -> 0x0315, NoSuchAlgorithmException -> 0x02d0, KeyManagementException -> 0x028a, IOException -> 0x0244, CertificateException -> 0x01fe, IllegalAccessException -> 0x01b8, KeyStoreException -> 0x0172, all -> 0x016d, all -> 0x034a }] */
    public Result processTask(RequestParams params) {
        Result result;
        int len;
        if (!NetworkUtil.isNetworkConnected(this.mContext)) {
            LogX.d("processTask, no network.");
            return readErrorResponse(-1, CardServerBaseResponse.RESPONSE_MESSAGE_NO_NETWORK_FAILED);
        }
        String requestStr = prepareRequestStr(params);
        if (requestStr == null) {
            LogX.d("processTask, invalid request params.");
            return readErrorResponse(1, CardServerBaseResponse.RESPONSE_MESSAGE_PARAMS_ERROR);
        }
        String srcTranID = "default";
        String commander = "default";
        try {
            JSONObject head = new JSONObject(JSONHelper.getStringValue(new JSONObject(requestStr), "header"));
            srcTranID = JSONHelper.getStringValue(head, "srcTranID");
            commander = JSONHelper.getStringValue(head, "commander");
        } catch (JSONException e) {
            LogX.e("Something wrong when get srcTranID and commander");
        }
        HttpURLConnection conn = null;
        DataOutputStream outStream = null;
        InputStream is = null;
        ByteArrayOutputStream outputStream = null;
        try {
            URL url = new URL(this.mUrl);
            conn = openConnection(url);
            conn.setRequestProperty("deviceId", PhoneDeviceUtil.getDeviceID(this.mContext));
            conn.setRequestProperty("bussiCertSign", JSONHelper.parseBussiCertSign(this.mContext, requestStr));
            conn.connect();
            outStream = new DataOutputStream(conn.getOutputStream());
            LogX.d("processTask request string : " + requestStr, true);
            outStream.write(requestStr.getBytes(AES.CHAR_ENCODING));
            outStream.flush();
            int resultCode = conn.getResponseCode();
            LogX.d("processTask connection result code : " + resultCode, true);
            if (200 == resultCode) {
                is = conn.getInputStream();
                outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[ApplyPayOrderCallback.RETURN_FAILED_CARDINFO_PIN_LOCKED];
                while (true) {
                    int read = is.read(buffer);
                    len = read;
                    if (read == -1) {
                        break;
                    }
                    int len2 = len;
                    try {
                        outputStream.write(buffer, 0, len2);
                        int i = len2;
                    } catch (MalformedURLException e2) {
                        urlEx = e2;
                        LogX.e("processTask url invalid.");
                        StringBuilder sb = new StringBuilder();
                        sb.append("RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION,urlEx = ");
                        sb.append(!TextUtils.isEmpty(urlEx.getMessage()) ? urlEx.getMessage() : "");
                        result = readErrorResponse(1, sb.toString());
                        closeStream(outStream, is, outputStream, conn);
                        return result;
                    } catch (NoSuchAlgorithmException e3) {
                        noSuchAlgorithmExceptionEx = e3;
                        LogX.e("processTask, NoSuchAlgorithmException : " + noSuchAlgorithmExceptionEx.getMessage(), true);
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx = ");
                        sb2.append(!TextUtils.isEmpty(noSuchAlgorithmExceptionEx.getMessage()) ? noSuchAlgorithmExceptionEx.getMessage() : "");
                        result = readErrorResponse(-2, sb2.toString());
                        closeStream(outStream, is, outputStream, conn);
                        return result;
                    } catch (KeyManagementException e4) {
                        keyManagementExceptionEx = e4;
                        LogX.e("processTask, KeyManagementException : " + keyManagementExceptionEx.getMessage(), true);
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append("RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx = ");
                        sb3.append(!TextUtils.isEmpty(keyManagementExceptionEx.getMessage()) ? keyManagementExceptionEx.getMessage() : "");
                        result = readErrorResponse(-2, sb3.toString());
                        closeStream(outStream, is, outputStream, conn);
                        return result;
                    } catch (IOException e5) {
                        ioEx = e5;
                        LogX.e("processTask IOException : " + ioEx.getMessage(), true);
                        StringBuilder sb4 = new StringBuilder();
                        sb4.append("RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx = ");
                        sb4.append(!TextUtils.isEmpty(ioEx.getMessage()) ? ioEx.getMessage() : "");
                        result = readErrorResponse(-2, sb4.toString());
                        closeStream(outStream, is, outputStream, conn);
                        return result;
                    } catch (CertificateException e6) {
                        e = e6;
                        LogX.e("processTask CertificateException : " + e.getMessage(), true);
                        StringBuilder sb5 = new StringBuilder();
                        sb5.append("RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException = ");
                        sb5.append(!TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
                        result = readErrorResponse(-2, sb5.toString());
                        closeStream(outStream, is, outputStream, conn);
                        return result;
                    } catch (IllegalAccessException e7) {
                        e = e7;
                        LogX.e("processTask IllegalAccessException : " + e.getMessage(), true);
                        StringBuilder sb6 = new StringBuilder();
                        sb6.append("RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException = ");
                        sb6.append(!TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
                        result = readErrorResponse(-2, sb6.toString());
                        closeStream(outStream, is, outputStream, conn);
                        return result;
                    } catch (KeyStoreException e8) {
                        e = e8;
                        LogX.e("processTask KeyStoreException : " + e.getMessage(), true);
                        StringBuilder sb7 = new StringBuilder();
                        sb7.append("RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException = ");
                        sb7.append(!TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
                        result = readErrorResponse(-2, sb7.toString());
                        closeStream(outStream, is, outputStream, conn);
                        return result;
                    } catch (Throwable th) {
                        result = th;
                        closeStream(outStream, is, outputStream, conn);
                        throw result;
                    }
                }
                int i2 = len;
                URL url2 = url;
                try {
                    String json = new String(outputStream.toByteArray(), AES.CHAR_ENCODING);
                    StringBuilder sb8 = new StringBuilder();
                    byte[] bArr = buffer;
                    sb8.append("NewHttpConnTask processTask json string : ");
                    sb8.append(json);
                    LogX.d(sb8.toString(), true);
                    result = handleResponse(json, srcTranID, commander);
                } catch (MalformedURLException e9) {
                    urlEx = e9;
                    LogX.e("processTask url invalid.");
                    StringBuilder sb9 = new StringBuilder();
                    sb9.append("RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION,urlEx = ");
                    sb9.append(!TextUtils.isEmpty(urlEx.getMessage()) ? urlEx.getMessage() : "");
                    result = readErrorResponse(1, sb9.toString());
                    closeStream(outStream, is, outputStream, conn);
                    return result;
                } catch (NoSuchAlgorithmException e10) {
                    noSuchAlgorithmExceptionEx = e10;
                    LogX.e("processTask, NoSuchAlgorithmException : " + noSuchAlgorithmExceptionEx.getMessage(), true);
                    StringBuilder sb22 = new StringBuilder();
                    sb22.append("RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx = ");
                    sb22.append(!TextUtils.isEmpty(noSuchAlgorithmExceptionEx.getMessage()) ? noSuchAlgorithmExceptionEx.getMessage() : "");
                    result = readErrorResponse(-2, sb22.toString());
                    closeStream(outStream, is, outputStream, conn);
                    return result;
                } catch (KeyManagementException e11) {
                    keyManagementExceptionEx = e11;
                    LogX.e("processTask, KeyManagementException : " + keyManagementExceptionEx.getMessage(), true);
                    StringBuilder sb32 = new StringBuilder();
                    sb32.append("RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx = ");
                    sb32.append(!TextUtils.isEmpty(keyManagementExceptionEx.getMessage()) ? keyManagementExceptionEx.getMessage() : "");
                    result = readErrorResponse(-2, sb32.toString());
                    closeStream(outStream, is, outputStream, conn);
                    return result;
                } catch (IOException e12) {
                    ioEx = e12;
                    LogX.e("processTask IOException : " + ioEx.getMessage(), true);
                    StringBuilder sb42 = new StringBuilder();
                    sb42.append("RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx = ");
                    sb42.append(!TextUtils.isEmpty(ioEx.getMessage()) ? ioEx.getMessage() : "");
                    result = readErrorResponse(-2, sb42.toString());
                    closeStream(outStream, is, outputStream, conn);
                    return result;
                } catch (CertificateException e13) {
                    e = e13;
                    LogX.e("processTask CertificateException : " + e.getMessage(), true);
                    StringBuilder sb52 = new StringBuilder();
                    sb52.append("RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException = ");
                    sb52.append(!TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
                    result = readErrorResponse(-2, sb52.toString());
                    closeStream(outStream, is, outputStream, conn);
                    return result;
                } catch (IllegalAccessException e14) {
                    e = e14;
                    LogX.e("processTask IllegalAccessException : " + e.getMessage(), true);
                    StringBuilder sb62 = new StringBuilder();
                    sb62.append("RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException = ");
                    sb62.append(!TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
                    result = readErrorResponse(-2, sb62.toString());
                    closeStream(outStream, is, outputStream, conn);
                    return result;
                } catch (KeyStoreException e15) {
                    e = e15;
                    LogX.e("processTask KeyStoreException : " + e.getMessage(), true);
                    StringBuilder sb72 = new StringBuilder();
                    sb72.append("RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException = ");
                    sb72.append(!TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
                    result = readErrorResponse(-2, sb72.toString());
                    closeStream(outStream, is, outputStream, conn);
                    return result;
                }
            } else {
                if (SERVER_OVERLOAD_ERRORCODE == resultCode) {
                    result = readErrorResponse(-4, CardServerBaseResponse.RESPONSE_MESSAGE_SERVER_OVERLOAD_ERROR);
                } else {
                    result = readErrorResponse(-2, CardServerBaseResponse.RESPONSE_MESSAGE_CONNECTION_FAILED);
                }
            }
        } catch (MalformedURLException e16) {
            urlEx = e16;
            LogX.e("processTask url invalid.");
            StringBuilder sb92 = new StringBuilder();
            sb92.append("RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION,urlEx = ");
            sb92.append(!TextUtils.isEmpty(urlEx.getMessage()) ? urlEx.getMessage() : "");
            result = readErrorResponse(1, sb92.toString());
            closeStream(outStream, is, outputStream, conn);
            return result;
        } catch (NoSuchAlgorithmException e17) {
            noSuchAlgorithmExceptionEx = e17;
            LogX.e("processTask, NoSuchAlgorithmException : " + noSuchAlgorithmExceptionEx.getMessage(), true);
            StringBuilder sb222 = new StringBuilder();
            sb222.append("RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx = ");
            sb222.append(!TextUtils.isEmpty(noSuchAlgorithmExceptionEx.getMessage()) ? noSuchAlgorithmExceptionEx.getMessage() : "");
            result = readErrorResponse(-2, sb222.toString());
            closeStream(outStream, is, outputStream, conn);
            return result;
        } catch (KeyManagementException e18) {
            keyManagementExceptionEx = e18;
            LogX.e("processTask, KeyManagementException : " + keyManagementExceptionEx.getMessage(), true);
            StringBuilder sb322 = new StringBuilder();
            sb322.append("RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx = ");
            sb322.append(!TextUtils.isEmpty(keyManagementExceptionEx.getMessage()) ? keyManagementExceptionEx.getMessage() : "");
            result = readErrorResponse(-2, sb322.toString());
            closeStream(outStream, is, outputStream, conn);
            return result;
        } catch (IOException e19) {
            ioEx = e19;
            LogX.e("processTask IOException : " + ioEx.getMessage(), true);
            StringBuilder sb422 = new StringBuilder();
            sb422.append("RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx = ");
            sb422.append(!TextUtils.isEmpty(ioEx.getMessage()) ? ioEx.getMessage() : "");
            result = readErrorResponse(-2, sb422.toString());
            closeStream(outStream, is, outputStream, conn);
            return result;
        } catch (CertificateException e20) {
            e = e20;
            LogX.e("processTask CertificateException : " + e.getMessage(), true);
            StringBuilder sb522 = new StringBuilder();
            sb522.append("RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException = ");
            sb522.append(!TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
            result = readErrorResponse(-2, sb522.toString());
            closeStream(outStream, is, outputStream, conn);
            return result;
        } catch (IllegalAccessException e21) {
            e = e21;
            LogX.e("processTask IllegalAccessException : " + e.getMessage(), true);
            StringBuilder sb622 = new StringBuilder();
            sb622.append("RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException = ");
            sb622.append(!TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
            result = readErrorResponse(-2, sb622.toString());
            closeStream(outStream, is, outputStream, conn);
            return result;
        } catch (KeyStoreException e22) {
            e = e22;
            LogX.e("processTask KeyStoreException : " + e.getMessage(), true);
            StringBuilder sb722 = new StringBuilder();
            sb722.append("RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException = ");
            sb722.append(!TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
            result = readErrorResponse(-2, sb722.toString());
            closeStream(outStream, is, outputStream, conn);
            return result;
        } catch (Throwable th2) {
            result = th2;
            closeStream(outStream, is, outputStream, conn);
            throw result;
        }
        closeStream(outStream, is, outputStream, conn);
        return result;
    }

    private void closeStream(DataOutputStream outStream, InputStream is, ByteArrayOutputStream outputStream, HttpURLConnection conn) {
        IOUtils.closeQuietly((OutputStream) outStream);
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly((OutputStream) outputStream);
        if (conn != null) {
            conn.disconnect();
        }
    }

    private HttpsURLConnection openHttpsConnection(URL url) throws IOException, NoSuchAlgorithmException, KeyManagementException, CertificateException, IllegalAccessException, KeyStoreException {
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
        initHttpsConnection(httpsURLConnection);
        return httpsURLConnection;
    }

    private HttpURLConnection openHttpConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    private void initHttpsConnection(HttpsURLConnection httpsURLConnection) throws NoSuchAlgorithmException, KeyManagementException, CertificateException, IllegalAccessException, KeyStoreException, IOException {
        httpsURLConnection.setSSLSocketFactory(SecureSSLSocketFactory.getInstance(this.mContext));
        httpsURLConnection.setHostnameVerifier(new StrictHostnameVerifier());
    }

    public boolean isNumber(String str) {
        if (str != null && !"".equals(str.trim()) && Pattern.compile("[0-9]*").matcher(str).matches()) {
            Long number = Long.valueOf(Long.parseLong(str));
            if (number.longValue() <= 2147483647L && number.longValue() >= -2147483648L) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public Result handleResponse(String responseStr, String srcTranID, String commander) {
        int returnCode;
        LogX.d("handleResponse response str", true);
        if (responseStr == null) {
            LogX.d("handleResponse, responseStr is null", true);
            return readSuccessResponse(-99, null, null);
        }
        String returnDesc = null;
        JSONObject dataObject = null;
        try {
            dataObject = new JSONObject(responseStr);
            String returnCodeStr = JSONHelper.getStringValue(dataObject, "returnCode");
            if (returnCodeStr == null) {
                LogX.d("handleResponse, returnCode is invalid.");
                return readSuccessResponse(-99, null, null);
            }
            if (isNumber(returnCodeStr)) {
                returnCode = Integer.parseInt(returnCodeStr);
            } else {
                returnCode = -98;
            }
            returnDesc = JSONHelper.getStringValue(dataObject, "returnDesc");
            if (returnCode != 0) {
                String errorInfo = JSONHelper.getStringValue(dataObject, "errorInfo");
                LogX.e("handleResponse, code:" + returnCode + ", msg:" + returnDesc + ", errorInfo:" + errorInfo);
                if (errorInfo != null) {
                    JSONObject info = new JSONObject(errorInfo);
                    String originalCode = JSONHelper.getStringValue(info, "originalCode");
                    String codeMsg = JSONHelper.getStringValue(info, "codeMsg");
                    if (!(originalCode == null || codeMsg == null)) {
                        returnDesc = originalCode + "|" + codeMsg;
                    }
                }
            }
            return readSuccessResponse(returnCode, returnDesc, dataObject);
        } catch (NumberFormatException ex) {
            LogX.e("readSuccessResponse, NumberFormatException : " + ex.getMessage(), true);
            returnCode = -99;
        } catch (JSONException ex2) {
            LogX.e("readSuccessResponse, JSONException : " + ex2.getMessage(), true);
            returnCode = -99;
        }
    }

    /* access modifiers changed from: protected */
    public void setErrorInfo(JSONObject dataObject, ServerAccessBaseResponse response) {
        if (dataObject != null && dataObject.has("errorInfo")) {
            ErrorInfo errorInfo = null;
            try {
                errorInfo = ErrorInfo.build(dataObject.getJSONObject("errorInfo"));
            } catch (JSONException e) {
                LogX.e("setErrorInfo, JSONException");
                response.returnCode = -99;
            }
            response.setErrorInfo(errorInfo);
        }
    }
}
