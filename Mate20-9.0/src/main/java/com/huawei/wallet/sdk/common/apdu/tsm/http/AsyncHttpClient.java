package com.huawei.wallet.sdk.common.apdu.tsm.http;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.secure.android.common.ssl.SecureSSLSocketFactory;
import com.huawei.wallet.sdk.business.buscard.model.ApplyPayOrderCallback;
import com.huawei.wallet.sdk.business.clearssd.util.OperateUtil;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.common.apdu.response.CardServerBaseResponse;
import com.huawei.wallet.sdk.common.apdu.whitecard.WalletProcessTrace;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.IOUtils;
import com.huawei.wallet.sdk.common.utils.PropertyUtils;
import com.huawei.wallet.sdk.common.utils.crypto.AES;
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
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.conn.ssl.StrictHostnameVerifier;

public class AsyncHttpClient extends WalletProcessTrace {
    private static final int DEFAULT_SOCKET_TIMEOUT = 30000;
    private static final int SERVER_OVERLOAD_ERRORCODE = 503;
    private static final String TAG = "AsyncHttpClient|";

    public void post(Context context, String requestStr, String contentType, ResponseHandlerInterface responseHandler) {
        sendRequest(requestStr, contentType, responseHandler, context);
    }

    private String getSSDTSMAddress() {
        String countryCode = PropertyUtils.getProperty(AddressNameMgr.PROP_NAME_LOCALE_REGION, "CN");
        LogC.i("AsyncHttpClient|getSSDTSMAddress|countryCode: " + countryCode, false);
        if ("CN".equalsIgnoreCase(countryCode)) {
            return AddressNameMgr.TSM_SERVER_URL;
        }
        return "https://tsm-dre.wallet.hicloud.com/TSMAPKP/HwTSMServer/applicationBusiness.action";
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x035a A[Catch:{ MalformedURLException -> 0x0403, NoSuchAlgorithmException -> 0x03b9, KeyManagementException -> 0x036e, IOException -> 0x0323, CertificateException -> 0x02d8, IllegalAccessException -> 0x028d, KeyStoreException -> 0x0242, all -> 0x023d, all -> 0x044a }] */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x035f A[Catch:{ MalformedURLException -> 0x0403, NoSuchAlgorithmException -> 0x03b9, KeyManagementException -> 0x036e, IOException -> 0x0323, CertificateException -> 0x02d8, IllegalAccessException -> 0x028d, KeyStoreException -> 0x0242, all -> 0x023d, all -> 0x044a }] */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x03a5 A[Catch:{ MalformedURLException -> 0x0403, NoSuchAlgorithmException -> 0x03b9, KeyManagementException -> 0x036e, IOException -> 0x0323, CertificateException -> 0x02d8, IllegalAccessException -> 0x028d, KeyStoreException -> 0x0242, all -> 0x023d, all -> 0x044a }] */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x03aa A[Catch:{ MalformedURLException -> 0x0403, NoSuchAlgorithmException -> 0x03b9, KeyManagementException -> 0x036e, IOException -> 0x0323, CertificateException -> 0x02d8, IllegalAccessException -> 0x028d, KeyStoreException -> 0x0242, all -> 0x023d, all -> 0x044a }] */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x03f0 A[Catch:{ MalformedURLException -> 0x0403, NoSuchAlgorithmException -> 0x03b9, KeyManagementException -> 0x036e, IOException -> 0x0323, CertificateException -> 0x02d8, IllegalAccessException -> 0x028d, KeyStoreException -> 0x0242, all -> 0x023d, all -> 0x044a }] */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x03f5 A[Catch:{ MalformedURLException -> 0x0403, NoSuchAlgorithmException -> 0x03b9, KeyManagementException -> 0x036e, IOException -> 0x0323, CertificateException -> 0x02d8, IllegalAccessException -> 0x028d, KeyStoreException -> 0x0242, all -> 0x023d, all -> 0x044a }] */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x0433 A[Catch:{ MalformedURLException -> 0x0403, NoSuchAlgorithmException -> 0x03b9, KeyManagementException -> 0x036e, IOException -> 0x0323, CertificateException -> 0x02d8, IllegalAccessException -> 0x028d, KeyStoreException -> 0x0242, all -> 0x023d, all -> 0x044a }] */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x0438 A[Catch:{ MalformedURLException -> 0x0403, NoSuchAlgorithmException -> 0x03b9, KeyManagementException -> 0x036e, IOException -> 0x0323, CertificateException -> 0x02d8, IllegalAccessException -> 0x028d, KeyStoreException -> 0x0242, all -> 0x023d, all -> 0x044a }] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x0279 A[Catch:{ MalformedURLException -> 0x0403, NoSuchAlgorithmException -> 0x03b9, KeyManagementException -> 0x036e, IOException -> 0x0323, CertificateException -> 0x02d8, IllegalAccessException -> 0x028d, KeyStoreException -> 0x0242, all -> 0x023d, all -> 0x044a }] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x027e A[Catch:{ MalformedURLException -> 0x0403, NoSuchAlgorithmException -> 0x03b9, KeyManagementException -> 0x036e, IOException -> 0x0323, CertificateException -> 0x02d8, IllegalAccessException -> 0x028d, KeyStoreException -> 0x0242, all -> 0x023d, all -> 0x044a }] */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x02c4 A[Catch:{ MalformedURLException -> 0x0403, NoSuchAlgorithmException -> 0x03b9, KeyManagementException -> 0x036e, IOException -> 0x0323, CertificateException -> 0x02d8, IllegalAccessException -> 0x028d, KeyStoreException -> 0x0242, all -> 0x023d, all -> 0x044a }] */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x02c9 A[Catch:{ MalformedURLException -> 0x0403, NoSuchAlgorithmException -> 0x03b9, KeyManagementException -> 0x036e, IOException -> 0x0323, CertificateException -> 0x02d8, IllegalAccessException -> 0x028d, KeyStoreException -> 0x0242, all -> 0x023d, all -> 0x044a }] */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x030f A[Catch:{ MalformedURLException -> 0x0403, NoSuchAlgorithmException -> 0x03b9, KeyManagementException -> 0x036e, IOException -> 0x0323, CertificateException -> 0x02d8, IllegalAccessException -> 0x028d, KeyStoreException -> 0x0242, all -> 0x023d, all -> 0x044a }] */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x0314 A[Catch:{ MalformedURLException -> 0x0403, NoSuchAlgorithmException -> 0x03b9, KeyManagementException -> 0x036e, IOException -> 0x0323, CertificateException -> 0x02d8, IllegalAccessException -> 0x028d, KeyStoreException -> 0x0242, all -> 0x023d, all -> 0x044a }] */
    public void sendRequest(String requestStr, String contentType, ResponseHandlerInterface responseHandler, Context context) {
        String tsmURl;
        String str = requestStr;
        ResponseHandlerInterface responseHandlerInterface = responseHandler;
        Context context2 = context;
        HttpURLConnection conn = null;
        DataOutputStream outStream = null;
        InputStream is = null;
        ByteArrayOutputStream outputStream = null;
        try {
            if (OperateUtil.isSSD()) {
                LogC.i(getSubProcessPrefix() + "sendRequest, getSSDAddress", false);
                tsmURl = getSSDTSMAddress() + "?version=" + "2.0.6";
            } else {
                LogC.i(getSubProcessPrefix() + "sendRequest, get wallet business address", false);
                tsmURl = AddressNameMgr.getInstance().getAddress("TSM", context2);
            }
            URL url = new URL(tsmURl);
            if ("https".equals(url.getProtocol())) {
                conn = openHttpsConnection(url, context2);
            } else {
                conn = openHttpConnection(url);
            }
            conn.setConnectTimeout(DEFAULT_SOCKET_TIMEOUT);
            conn.setReadTimeout(DEFAULT_SOCKET_TIMEOUT);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            try {
                conn.setRequestProperty("Content-Type", contentType);
                conn.setRequestProperty("Charset", AES.CHAR_ENCODING);
                conn.connect();
                outStream = new DataOutputStream(conn.getOutputStream());
                LogC.d(getSubProcessPrefix() + "sendRequest request string : " + str, true);
                outStream.write(str.getBytes(AES.CHAR_ENCODING));
                outStream.flush();
                int resultCode = conn.getResponseCode();
                LogC.i(getSubProcessPrefix() + "sendRequest connection result code : " + resultCode, true);
                if (200 == resultCode) {
                    is = conn.getInputStream();
                    try {
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
                        String str2 = tsmURl;
                        String json = new String(outputStream.toByteArray(), AES.CHAR_ENCODING);
                        String str3 = getSubProcessPrefix() + "sendRequest";
                        StringBuilder sb = new StringBuilder();
                        InputStream is2 = is;
                        try {
                            sb.append("processTask request string : ");
                            sb.append(str);
                            LogC.d(str3, sb.toString(), false);
                            LogC.d(getSubProcessPrefix() + "sendRequest", "processTask json string : " + json, false);
                            handleResponse(json, responseHandlerInterface);
                            is = is2;
                        } catch (MalformedURLException e) {
                            urlEx = e;
                            is = is2;
                            LogC.e(getSubProcessPrefix() + "sendRequest url invalid.", false);
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append("RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION,urlEx = ");
                            sb2.append(TextUtils.isEmpty(urlEx.getMessage()) ? urlEx.getMessage() : "");
                            responseHandlerInterface.sendFailureMessage(1, sb2.toString());
                            closeStream(outStream, is, outputStream, conn);
                        } catch (NoSuchAlgorithmException e2) {
                            noSuchAlgorithmExceptionEx = e2;
                            is = is2;
                            LogC.e(getSubProcessPrefix() + "sendRequest, NoSuchAlgorithmException : " + noSuchAlgorithmExceptionEx.getMessage(), true);
                            StringBuilder sb3 = new StringBuilder();
                            sb3.append("RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx = ");
                            sb3.append(TextUtils.isEmpty(noSuchAlgorithmExceptionEx.getMessage()) ? noSuchAlgorithmExceptionEx.getMessage() : "");
                            responseHandlerInterface.sendFailureMessage(-2, sb3.toString());
                            closeStream(outStream, is, outputStream, conn);
                        } catch (KeyManagementException e3) {
                            keyManagementExceptionEx = e3;
                            is = is2;
                            LogC.e(getSubProcessPrefix() + "sendRequest, KeyManagementException : " + keyManagementExceptionEx.getMessage(), true);
                            StringBuilder sb4 = new StringBuilder();
                            sb4.append("RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx = ");
                            sb4.append(TextUtils.isEmpty(keyManagementExceptionEx.getMessage()) ? keyManagementExceptionEx.getMessage() : "");
                            responseHandlerInterface.sendFailureMessage(-2, sb4.toString());
                            closeStream(outStream, is, outputStream, conn);
                        } catch (IOException e4) {
                            ioEx = e4;
                            is = is2;
                            LogC.e(getSubProcessPrefix() + "sendRequest IOException : " + ioEx.getMessage(), true);
                            StringBuilder sb5 = new StringBuilder();
                            sb5.append("RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx = ");
                            sb5.append(TextUtils.isEmpty(ioEx.getMessage()) ? ioEx.getMessage() : "");
                            responseHandlerInterface.sendFailureMessage(-2, sb5.toString());
                            closeStream(outStream, is, outputStream, conn);
                        } catch (CertificateException e5) {
                            e = e5;
                            is = is2;
                            LogC.e(getSubProcessPrefix() + "sendRequest CertificateException : " + e.getMessage(), true);
                            StringBuilder sb6 = new StringBuilder();
                            sb6.append("RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException = ");
                            sb6.append(TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
                            responseHandlerInterface.sendFailureMessage(-2, sb6.toString());
                            closeStream(outStream, is, outputStream, conn);
                        } catch (IllegalAccessException e6) {
                            e = e6;
                            is = is2;
                            LogC.e(getSubProcessPrefix() + "sendRequest IllegalAccessException : " + e.getLocalizedMessage(), true);
                            StringBuilder sb7 = new StringBuilder();
                            sb7.append("RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException = ");
                            sb7.append(TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
                            responseHandlerInterface.sendFailureMessage(-2, sb7.toString());
                            closeStream(outStream, is, outputStream, conn);
                        } catch (KeyStoreException e7) {
                            e = e7;
                            is = is2;
                            LogC.e(getSubProcessPrefix() + "sendRequest KeyStoreException : " + e.getMessage(), true);
                            StringBuilder sb8 = new StringBuilder();
                            sb8.append("RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException = ");
                            sb8.append(TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
                            responseHandlerInterface.sendFailureMessage(-2, sb8.toString());
                            closeStream(outStream, is, outputStream, conn);
                        } catch (Throwable th) {
                            th = th;
                            is = is2;
                            closeStream(outStream, is, outputStream, conn);
                            throw th;
                        }
                    } catch (MalformedURLException e8) {
                        urlEx = e8;
                        InputStream inputStream = is;
                        LogC.e(getSubProcessPrefix() + "sendRequest url invalid.", false);
                        StringBuilder sb22 = new StringBuilder();
                        sb22.append("RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION,urlEx = ");
                        sb22.append(TextUtils.isEmpty(urlEx.getMessage()) ? urlEx.getMessage() : "");
                        responseHandlerInterface.sendFailureMessage(1, sb22.toString());
                        closeStream(outStream, is, outputStream, conn);
                    } catch (NoSuchAlgorithmException e9) {
                        noSuchAlgorithmExceptionEx = e9;
                        InputStream inputStream2 = is;
                        LogC.e(getSubProcessPrefix() + "sendRequest, NoSuchAlgorithmException : " + noSuchAlgorithmExceptionEx.getMessage(), true);
                        StringBuilder sb32 = new StringBuilder();
                        sb32.append("RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx = ");
                        sb32.append(TextUtils.isEmpty(noSuchAlgorithmExceptionEx.getMessage()) ? noSuchAlgorithmExceptionEx.getMessage() : "");
                        responseHandlerInterface.sendFailureMessage(-2, sb32.toString());
                        closeStream(outStream, is, outputStream, conn);
                    } catch (KeyManagementException e10) {
                        keyManagementExceptionEx = e10;
                        InputStream inputStream3 = is;
                        LogC.e(getSubProcessPrefix() + "sendRequest, KeyManagementException : " + keyManagementExceptionEx.getMessage(), true);
                        StringBuilder sb42 = new StringBuilder();
                        sb42.append("RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx = ");
                        sb42.append(TextUtils.isEmpty(keyManagementExceptionEx.getMessage()) ? keyManagementExceptionEx.getMessage() : "");
                        responseHandlerInterface.sendFailureMessage(-2, sb42.toString());
                        closeStream(outStream, is, outputStream, conn);
                    } catch (IOException e11) {
                        ioEx = e11;
                        InputStream inputStream4 = is;
                        LogC.e(getSubProcessPrefix() + "sendRequest IOException : " + ioEx.getMessage(), true);
                        StringBuilder sb52 = new StringBuilder();
                        sb52.append("RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx = ");
                        sb52.append(TextUtils.isEmpty(ioEx.getMessage()) ? ioEx.getMessage() : "");
                        responseHandlerInterface.sendFailureMessage(-2, sb52.toString());
                        closeStream(outStream, is, outputStream, conn);
                    } catch (CertificateException e12) {
                        e = e12;
                        InputStream inputStream5 = is;
                        LogC.e(getSubProcessPrefix() + "sendRequest CertificateException : " + e.getMessage(), true);
                        StringBuilder sb62 = new StringBuilder();
                        sb62.append("RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException = ");
                        sb62.append(TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
                        responseHandlerInterface.sendFailureMessage(-2, sb62.toString());
                        closeStream(outStream, is, outputStream, conn);
                    } catch (IllegalAccessException e13) {
                        e = e13;
                        InputStream inputStream6 = is;
                        LogC.e(getSubProcessPrefix() + "sendRequest IllegalAccessException : " + e.getLocalizedMessage(), true);
                        StringBuilder sb72 = new StringBuilder();
                        sb72.append("RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException = ");
                        sb72.append(TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
                        responseHandlerInterface.sendFailureMessage(-2, sb72.toString());
                        closeStream(outStream, is, outputStream, conn);
                    } catch (KeyStoreException e14) {
                        e = e14;
                        InputStream inputStream7 = is;
                        LogC.e(getSubProcessPrefix() + "sendRequest KeyStoreException : " + e.getMessage(), true);
                        StringBuilder sb82 = new StringBuilder();
                        sb82.append("RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException = ");
                        sb82.append(TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
                        responseHandlerInterface.sendFailureMessage(-2, sb82.toString());
                        closeStream(outStream, is, outputStream, conn);
                    } catch (Throwable th2) {
                        th = th2;
                        InputStream inputStream8 = is;
                        closeStream(outStream, is, outputStream, conn);
                        throw th;
                    }
                    closeStream(outStream, is, outputStream, conn);
                }
                String str4 = tsmURl;
                if (SERVER_OVERLOAD_ERRORCODE == resultCode) {
                    LogC.e(getSubProcessPrefix() + "sendRequest resultCode=SERVER_OVERLOAD_ERRORCODE.", false);
                    responseHandlerInterface.sendFailureMessage(-4, CardServerBaseResponse.RESPONSE_MESSAGE_SERVER_OVERLOAD_ERROR);
                } else {
                    LogC.e(getSubProcessPrefix() + "sendRequest resultCode=" + resultCode, false);
                    responseHandlerInterface.sendFailureMessage(-2, CardServerBaseResponse.RESPONSE_MESSAGE_CONNECTION_FAILED);
                }
                closeStream(outStream, is, outputStream, conn);
            } catch (MalformedURLException e15) {
                urlEx = e15;
                LogC.e(getSubProcessPrefix() + "sendRequest url invalid.", false);
                StringBuilder sb222 = new StringBuilder();
                sb222.append("RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION,urlEx = ");
                sb222.append(TextUtils.isEmpty(urlEx.getMessage()) ? urlEx.getMessage() : "");
                responseHandlerInterface.sendFailureMessage(1, sb222.toString());
                closeStream(outStream, is, outputStream, conn);
            } catch (NoSuchAlgorithmException e16) {
                noSuchAlgorithmExceptionEx = e16;
                LogC.e(getSubProcessPrefix() + "sendRequest, NoSuchAlgorithmException : " + noSuchAlgorithmExceptionEx.getMessage(), true);
                StringBuilder sb322 = new StringBuilder();
                sb322.append("RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx = ");
                sb322.append(TextUtils.isEmpty(noSuchAlgorithmExceptionEx.getMessage()) ? noSuchAlgorithmExceptionEx.getMessage() : "");
                responseHandlerInterface.sendFailureMessage(-2, sb322.toString());
                closeStream(outStream, is, outputStream, conn);
            } catch (KeyManagementException e17) {
                keyManagementExceptionEx = e17;
                LogC.e(getSubProcessPrefix() + "sendRequest, KeyManagementException : " + keyManagementExceptionEx.getMessage(), true);
                StringBuilder sb422 = new StringBuilder();
                sb422.append("RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx = ");
                sb422.append(TextUtils.isEmpty(keyManagementExceptionEx.getMessage()) ? keyManagementExceptionEx.getMessage() : "");
                responseHandlerInterface.sendFailureMessage(-2, sb422.toString());
                closeStream(outStream, is, outputStream, conn);
            } catch (IOException e18) {
                ioEx = e18;
                LogC.e(getSubProcessPrefix() + "sendRequest IOException : " + ioEx.getMessage(), true);
                StringBuilder sb522 = new StringBuilder();
                sb522.append("RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx = ");
                sb522.append(TextUtils.isEmpty(ioEx.getMessage()) ? ioEx.getMessage() : "");
                responseHandlerInterface.sendFailureMessage(-2, sb522.toString());
                closeStream(outStream, is, outputStream, conn);
            } catch (CertificateException e19) {
                e = e19;
                LogC.e(getSubProcessPrefix() + "sendRequest CertificateException : " + e.getMessage(), true);
                StringBuilder sb622 = new StringBuilder();
                sb622.append("RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException = ");
                sb622.append(TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
                responseHandlerInterface.sendFailureMessage(-2, sb622.toString());
                closeStream(outStream, is, outputStream, conn);
            } catch (IllegalAccessException e20) {
                e = e20;
                LogC.e(getSubProcessPrefix() + "sendRequest IllegalAccessException : " + e.getLocalizedMessage(), true);
                StringBuilder sb722 = new StringBuilder();
                sb722.append("RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException = ");
                sb722.append(TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
                responseHandlerInterface.sendFailureMessage(-2, sb722.toString());
                closeStream(outStream, is, outputStream, conn);
            } catch (KeyStoreException e21) {
                e = e21;
                LogC.e(getSubProcessPrefix() + "sendRequest KeyStoreException : " + e.getMessage(), true);
                StringBuilder sb822 = new StringBuilder();
                sb822.append("RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException = ");
                sb822.append(TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
                responseHandlerInterface.sendFailureMessage(-2, sb822.toString());
                closeStream(outStream, is, outputStream, conn);
            }
        } catch (MalformedURLException e22) {
            urlEx = e22;
            String str5 = contentType;
            LogC.e(getSubProcessPrefix() + "sendRequest url invalid.", false);
            StringBuilder sb2222 = new StringBuilder();
            sb2222.append("RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION,urlEx = ");
            sb2222.append(TextUtils.isEmpty(urlEx.getMessage()) ? urlEx.getMessage() : "");
            responseHandlerInterface.sendFailureMessage(1, sb2222.toString());
            closeStream(outStream, is, outputStream, conn);
        } catch (NoSuchAlgorithmException e23) {
            noSuchAlgorithmExceptionEx = e23;
            String str6 = contentType;
            LogC.e(getSubProcessPrefix() + "sendRequest, NoSuchAlgorithmException : " + noSuchAlgorithmExceptionEx.getMessage(), true);
            StringBuilder sb3222 = new StringBuilder();
            sb3222.append("RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx = ");
            sb3222.append(TextUtils.isEmpty(noSuchAlgorithmExceptionEx.getMessage()) ? noSuchAlgorithmExceptionEx.getMessage() : "");
            responseHandlerInterface.sendFailureMessage(-2, sb3222.toString());
            closeStream(outStream, is, outputStream, conn);
        } catch (KeyManagementException e24) {
            keyManagementExceptionEx = e24;
            String str7 = contentType;
            LogC.e(getSubProcessPrefix() + "sendRequest, KeyManagementException : " + keyManagementExceptionEx.getMessage(), true);
            StringBuilder sb4222 = new StringBuilder();
            sb4222.append("RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx = ");
            sb4222.append(TextUtils.isEmpty(keyManagementExceptionEx.getMessage()) ? keyManagementExceptionEx.getMessage() : "");
            responseHandlerInterface.sendFailureMessage(-2, sb4222.toString());
            closeStream(outStream, is, outputStream, conn);
        } catch (IOException e25) {
            ioEx = e25;
            String str8 = contentType;
            LogC.e(getSubProcessPrefix() + "sendRequest IOException : " + ioEx.getMessage(), true);
            StringBuilder sb5222 = new StringBuilder();
            sb5222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx = ");
            sb5222.append(TextUtils.isEmpty(ioEx.getMessage()) ? ioEx.getMessage() : "");
            responseHandlerInterface.sendFailureMessage(-2, sb5222.toString());
            closeStream(outStream, is, outputStream, conn);
        } catch (CertificateException e26) {
            e = e26;
            String str9 = contentType;
            LogC.e(getSubProcessPrefix() + "sendRequest CertificateException : " + e.getMessage(), true);
            StringBuilder sb6222 = new StringBuilder();
            sb6222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException = ");
            sb6222.append(TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
            responseHandlerInterface.sendFailureMessage(-2, sb6222.toString());
            closeStream(outStream, is, outputStream, conn);
        } catch (IllegalAccessException e27) {
            e = e27;
            String str10 = contentType;
            LogC.e(getSubProcessPrefix() + "sendRequest IllegalAccessException : " + e.getLocalizedMessage(), true);
            StringBuilder sb7222 = new StringBuilder();
            sb7222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException = ");
            sb7222.append(TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
            responseHandlerInterface.sendFailureMessage(-2, sb7222.toString());
            closeStream(outStream, is, outputStream, conn);
        } catch (KeyStoreException e28) {
            e = e28;
            String str11 = contentType;
            LogC.e(getSubProcessPrefix() + "sendRequest KeyStoreException : " + e.getMessage(), true);
            StringBuilder sb8222 = new StringBuilder();
            sb8222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException = ");
            sb8222.append(TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "");
            responseHandlerInterface.sendFailureMessage(-2, sb8222.toString());
            closeStream(outStream, is, outputStream, conn);
        } catch (Throwable th3) {
            th = th3;
            closeStream(outStream, is, outputStream, conn);
            throw th;
        }
    }

    private void handleResponse(String json, ResponseHandlerInterface responseHandler) {
        responseHandler.sendSuccessMessage(0, json);
    }

    private void closeStream(DataOutputStream outStream, InputStream is, ByteArrayOutputStream outputStream, HttpURLConnection conn) {
        IOUtils.closeQuietly((OutputStream) outStream);
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly((OutputStream) outputStream);
        if (conn != null) {
            conn.disconnect();
        }
    }

    private HttpsURLConnection openHttpsConnection(URL url, Context mContext) throws IOException, NoSuchAlgorithmException, KeyManagementException, CertificateException, IllegalAccessException, KeyStoreException {
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
        initHttpsConnection(httpsURLConnection, mContext);
        return httpsURLConnection;
    }

    private HttpURLConnection openHttpConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    private void initHttpsConnection(HttpsURLConnection httpsURLConnection, Context mContext) throws NoSuchAlgorithmException, KeyManagementException, CertificateException, IllegalAccessException, KeyStoreException, IOException {
        httpsURLConnection.setSSLSocketFactory(SecureSSLSocketFactory.getInstance(mContext));
        httpsURLConnection.setHostnameVerifier(new StrictHostnameVerifier());
    }

    public void setProcessPrefix(String processPrefix, String tag) {
        super.setProcessPrefix(processPrefix, TAG);
    }
}
