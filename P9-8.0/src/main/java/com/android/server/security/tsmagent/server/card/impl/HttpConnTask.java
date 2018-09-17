package com.android.server.security.tsmagent.server.card.impl;

import android.content.Context;
import com.android.server.security.tsmagent.constant.ServiceConfig;
import com.android.server.security.tsmagent.server.CardServerBaseRequest;
import com.android.server.security.tsmagent.server.CardServerBaseResponse;
import com.android.server.security.tsmagent.server.HttpConnectionBase;
import com.android.server.security.tsmagent.server.card.request.TsmParamQueryRequest;
import com.android.server.security.tsmagent.server.wallet.impl.JSONHelper;
import com.android.server.security.tsmagent.utils.HwLog;
import com.android.server.security.tsmagent.utils.NetworkUtil;
import com.android.server.security.tsmagent.utils.StringUtil;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class HttpConnTask extends HttpConnectionBase {
    protected abstract CardServerBaseResponse readSuccessResponse(int i, String str, JSONObject jSONObject);

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

    /* JADX WARNING: Removed duplicated region for block: B:62:0x01eb A:{Splitter: B:22:0x0133, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x01eb A:{Splitter: B:22:0x0133, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x01e8 A:{Splitter: B:14:0x00b8, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x01e8 A:{Splitter: B:14:0x00b8, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x01a7 A:{Splitter: B:9:0x0039, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x01a7 A:{Splitter: B:9:0x0039, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Missing block: B:46:0x01a7, code:
            r5 = e;
     */
    /* JADX WARNING: Missing block: B:48:?, code:
            com.android.server.security.tsmagent.utils.HwLog.e("processTask, Exception : " + r5.getMessage());
            r15 = readErrorResponse(-2);
     */
    /* JADX WARNING: Missing block: B:49:0x01cc, code:
            closeStream(r9, r6, r11, r3);
     */
    /* JADX WARNING: Missing block: B:60:0x01e8, code:
            r5 = e;
     */
    /* JADX WARNING: Missing block: B:61:0x01e9, code:
            r9 = r10;
     */
    /* JADX WARNING: Missing block: B:62:0x01eb, code:
            r5 = e;
     */
    /* JADX WARNING: Missing block: B:63:0x01ec, code:
            r11 = r12;
            r9 = r10;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public CardServerBaseResponse processTask(CardServerBaseRequest params) {
        CardServerBaseResponse result;
        Throwable th;
        if (NetworkUtil.isNetworkConnected(this.mContext)) {
            String requestStr = prepareRequestStr(params);
            if (requestStr == null) {
                HwLog.d("processTask, invalid request params.");
                return readErrorResponse(-3);
            }
            HttpURLConnection conn = null;
            DataOutputStream outStream = null;
            InputStream is = null;
            ByteArrayOutputStream outputStream = null;
            try {
                URL url = new URL(this.mUrl);
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
                conn.setRequestProperty("Content-Type", "xml/json");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.connect();
                DataOutputStream outStream2 = new DataOutputStream(conn.getOutputStream());
                try {
                    if (params instanceof TsmParamQueryRequest) {
                        TsmParamQueryRequest request = (TsmParamQueryRequest) params;
                        request.setCplc("***************");
                        request.setTsmParamIMEI("***************");
                    }
                    HwLog.d("processTask request string : " + prepareRequestStr(params));
                    outStream2.write(requestStr.getBytes("UTF-8"));
                    outStream2.flush();
                    int resultCode = conn.getResponseCode();
                    HwLog.d("processTask connection result code : " + resultCode);
                    if (200 == resultCode) {
                        is = conn.getInputStream();
                        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
                        try {
                            byte[] buffer = new byte[1024];
                            while (true) {
                                int len = is.read(buffer);
                                if (len == -1) {
                                    break;
                                }
                                outputStream2.write(buffer, 0, len);
                            }
                            result = handleResponse(new String(outputStream2.toByteArray(), "UTF-8"));
                            outputStream = outputStream2;
                        } catch (MalformedURLException e) {
                            outputStream = outputStream2;
                            outStream = outStream2;
                            try {
                                HwLog.e("processTask url invalid.");
                                result = readErrorResponse(-3);
                                closeStream(outStream, is, outputStream, conn);
                                return result;
                            } catch (Throwable th2) {
                                th = th2;
                                closeStream(outStream, is, outputStream, conn);
                                throw th;
                            }
                        } catch (IOException e2) {
                        } catch (Throwable th3) {
                            th = th3;
                            outputStream = outputStream2;
                            outStream = outStream2;
                            closeStream(outStream, is, outputStream, conn);
                            throw th;
                        }
                    } else if (503 == resultCode) {
                        result = readErrorResponse(-4);
                    } else {
                        result = readErrorResponse(-2);
                    }
                    closeStream(outStream2, is, outputStream, conn);
                } catch (MalformedURLException e3) {
                    outStream = outStream2;
                    HwLog.e("processTask url invalid.");
                    result = readErrorResponse(-3);
                    closeStream(outStream, is, outputStream, conn);
                    return result;
                } catch (IOException e4) {
                } catch (Throwable th4) {
                    th = th4;
                    outStream = outStream2;
                    closeStream(outStream, is, outputStream, conn);
                    throw th;
                }
            } catch (MalformedURLException e5) {
            } catch (IOException e6) {
            }
            return result;
        }
        HwLog.d("processTask, no network.");
        return readErrorResponse(-1);
    }

    public CardServerBaseResponse handleResponse(String responseStr) {
        NumberFormatException ex;
        JSONObject jSONObject;
        JSONException ex2;
        HwLog.d("handleResponse response str : " + responseStr);
        String returnDesc = null;
        JSONObject dataObject = null;
        if (responseStr == null) {
            return readSuccessResponse(-99, null, null);
        }
        int returnCode;
        try {
            JSONObject responseJson = new JSONObject(responseStr);
            try {
                int keyIndex = JSONHelper.getIntValue(responseJson, "keyIndex");
                String merchantID = JSONHelper.getStringValue(responseJson, "merchantID");
                String errorCode = JSONHelper.getStringValue(responseJson, "errorCode");
                String errorMsg = JSONHelper.getStringValue(responseJson, "errorMsg");
                String responseDataStr = JSONHelper.getStringValue(responseJson, "response");
                if (errorCode != null) {
                    HwLog.w("handleResponse, error code : " + errorCode + ",error msg : " + errorMsg);
                    return readSuccessResponse(Integer.parseInt(errorCode), errorMsg, null);
                } else if (ServiceConfig.WALLET_MERCHANT_ID.equals(merchantID) && -1 == keyIndex && !StringUtil.isTrimedEmpty(responseDataStr)) {
                    HwLog.d("handleResponse, responseDataStr : " + responseDataStr);
                    JSONObject dataObject2 = new JSONObject(responseDataStr);
                    try {
                        String returnCodeStr = JSONHelper.getStringValue(dataObject2, "returnCode");
                        if (returnCodeStr == null) {
                            HwLog.d("handleResponse, returnCode is invalid.");
                            return readSuccessResponse(-99, null, null);
                        }
                        if (isNumber(returnCodeStr)) {
                            returnCode = Integer.parseInt(returnCodeStr);
                        } else {
                            returnCode = -98;
                        }
                        returnDesc = JSONHelper.getStringValue(dataObject2, "returnDesc");
                        dataObject = dataObject2;
                        return readSuccessResponse(returnCode, returnDesc, dataObject);
                    } catch (NumberFormatException e) {
                        ex = e;
                        jSONObject = responseJson;
                        dataObject = dataObject2;
                        HwLog.e("readSuccessResponse, NumberFormatException : " + ex);
                        returnCode = -99;
                        return readSuccessResponse(returnCode, returnDesc, dataObject);
                    } catch (JSONException e2) {
                        ex2 = e2;
                        jSONObject = responseJson;
                        dataObject = dataObject2;
                        HwLog.e("readSuccessResponse, JSONException : " + ex2);
                        returnCode = -99;
                        return readSuccessResponse(returnCode, returnDesc, dataObject);
                    }
                } else {
                    HwLog.d("handleResponse, unexpected error from server.");
                    return readSuccessResponse(-99, null, null);
                }
            } catch (NumberFormatException e3) {
                ex = e3;
                HwLog.e("readSuccessResponse, NumberFormatException : " + ex);
                returnCode = -99;
                return readSuccessResponse(returnCode, returnDesc, dataObject);
            } catch (JSONException e4) {
                ex2 = e4;
                jSONObject = responseJson;
                HwLog.e("readSuccessResponse, JSONException : " + ex2);
                returnCode = -99;
                return readSuccessResponse(returnCode, returnDesc, dataObject);
            }
        } catch (NumberFormatException e5) {
            ex = e5;
            HwLog.e("readSuccessResponse, NumberFormatException : " + ex);
            returnCode = -99;
            return readSuccessResponse(returnCode, returnDesc, dataObject);
        } catch (JSONException e6) {
            ex2 = e6;
            HwLog.e("readSuccessResponse, JSONException : " + ex2);
            returnCode = -99;
            return readSuccessResponse(returnCode, returnDesc, dataObject);
        }
    }

    public boolean isNumber(String str) {
        if (!(str == null || ("".equals(str.trim()) ^ 1) == 0 || !Pattern.compile("[0-9]*").matcher(str).matches())) {
            Long number = Long.valueOf(Long.parseLong(str));
            if (number.longValue() <= 2147483647L && number.longValue() >= -2147483648L) {
                return true;
            }
        }
        return false;
    }
}
