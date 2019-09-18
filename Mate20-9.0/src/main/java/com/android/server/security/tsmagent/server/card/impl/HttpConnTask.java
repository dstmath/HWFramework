package com.android.server.security.tsmagent.server.card.impl;

import android.content.Context;
import com.android.server.HwNetworkPropertyChecker;
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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class HttpConnTask extends HttpConnectionBase {
    /* access modifiers changed from: protected */
    public abstract CardServerBaseResponse readSuccessResponse(int i, String str, JSONObject jSONObject);

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

    public CardServerBaseResponse processTask(CardServerBaseRequest params) {
        CardServerBaseResponse result;
        CardServerBaseRequest cardServerBaseRequest = params;
        if (!NetworkUtil.isNetworkConnected(this.mContext)) {
            HwLog.d("processTask, no network.");
            return readErrorResponse(-1);
        }
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
            if (HwNetworkPropertyChecker.NetworkCheckerThread.TYPE_HTTPS.equals(url.getProtocol())) {
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
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.connect();
            outStream = new DataOutputStream(conn.getOutputStream());
            if (cardServerBaseRequest instanceof TsmParamQueryRequest) {
                TsmParamQueryRequest request = (TsmParamQueryRequest) cardServerBaseRequest;
                request.setCplc("***************");
                request.setTsmParamIMEI("***************");
            }
            HwLog.d("processTask request string : " + prepareRequestStr(params));
            outStream.write(requestStr.getBytes("UTF-8"));
            outStream.flush();
            int resultCode = conn.getResponseCode();
            HwLog.d("processTask connection result code : " + resultCode);
            if (200 == resultCode) {
                is = conn.getInputStream();
                outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                while (true) {
                    int read = is.read(buffer);
                    int len = read;
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(buffer, 0, len);
                }
                result = handleResponse(new String(outputStream.toByteArray(), "UTF-8"));
            } else if (503 == resultCode) {
                result = readErrorResponse(-4);
            } else {
                result = readErrorResponse(-2);
            }
        } catch (MalformedURLException e) {
            HwLog.e("processTask url invalid.");
            result = readErrorResponse(-3);
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException e2) {
            HwLog.e("processTask, Exception : " + e2.getMessage());
            result = readErrorResponse(-2);
        } catch (Throwable th) {
            closeStream(null, null, null, null);
            throw th;
        }
        closeStream(outStream, is, outputStream, conn);
        return result;
    }

    public CardServerBaseResponse handleResponse(String responseStr) {
        int returnCode;
        HwLog.d("handleResponse response str : " + responseStr);
        String returnDesc = null;
        JSONObject dataObject = null;
        if (responseStr == null) {
            return readSuccessResponse(-99, null, null);
        }
        try {
            JSONObject responseJson = new JSONObject(responseStr);
            int keyIndex = JSONHelper.getIntValue(responseJson, "keyIndex");
            String merchantID = JSONHelper.getStringValue(responseJson, "merchantID");
            String errorCode = JSONHelper.getStringValue(responseJson, "errorCode");
            String errorMsg = JSONHelper.getStringValue(responseJson, "errorMsg");
            String responseDataStr = JSONHelper.getStringValue(responseJson, "response");
            if (errorCode != null) {
                HwLog.w("handleResponse, error code : " + errorCode + ",error msg : " + errorMsg);
                return readSuccessResponse(Integer.parseInt(errorCode), errorMsg, null);
            }
            if (ServiceConfig.getWalletId().equals(merchantID) && -1 == keyIndex) {
                if (!StringUtil.isTrimedEmpty(responseDataStr)) {
                    HwLog.d("handleResponse, responseDataStr : " + responseDataStr);
                    dataObject = new JSONObject(responseDataStr);
                    String returnCodeStr = JSONHelper.getStringValue(dataObject, "returnCode");
                    if (returnCodeStr == null) {
                        HwLog.d("handleResponse, returnCode is invalid.");
                        return readSuccessResponse(-99, null, null);
                    }
                    if (isNumber(returnCodeStr)) {
                        returnCode = Integer.parseInt(returnCodeStr);
                    } else {
                        returnCode = -98;
                    }
                    returnDesc = JSONHelper.getStringValue(dataObject, "returnDesc");
                    return readSuccessResponse(returnCode, returnDesc, dataObject);
                }
            }
            HwLog.d("handleResponse, unexpected error from server.");
            return readSuccessResponse(-99, null, null);
        } catch (NumberFormatException ex) {
            HwLog.e("readSuccessResponse, NumberFormatException : " + ex);
            returnCode = -99;
        } catch (JSONException ex2) {
            HwLog.e("readSuccessResponse, JSONException : " + ex2);
            returnCode = -99;
        }
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
}
