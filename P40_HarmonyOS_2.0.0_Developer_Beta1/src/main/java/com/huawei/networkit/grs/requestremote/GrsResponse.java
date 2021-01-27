package com.huawei.networkit.grs.requestremote;

import android.text.TextUtils;
import com.huawei.networkit.grs.common.Logger;
import com.huawei.networkit.grs.common.StringUtils;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class GrsResponse {
    private static final String CACHE_CONTROL_MAX_AGE = "max-age=";
    private static final int DEFAULT_CACHE_TIME = 86400;
    public static final int DEFAULT_ERROR_CODE = 9001;
    private static final String KEY_ERRORCODE = "errorCode";
    private static final String KEY_ERRORDESC = "errorDesc";
    private static final String KEY_ISSUCCESS = "isSuccess";
    private static final String KEY_SERVICE = "services";
    private static final int MAX_CACHE_TIME = 2592000;
    public static final int OBTAIN_ALL_FAILED = 2;
    public static final int OBTAIN_FAILED = 0;
    public static final int OBTAIN_SUCCESS = 1;
    private static final String RESP_HEADER_CACHE_CONTROL = "Cache-Control";
    private static final String RESP_HEADER_DATE = "Date";
    private static final String RESP_HEADER_EXPIRES = "Expires";
    private static final String TAG = GrsResponse.class.getSimpleName();
    private String cacheExpireTime = "";
    private int code = 0;
    private int errorCode = DEFAULT_ERROR_CODE;
    private String errorDesc = "";
    private Exception errorException;
    private String errorListStr = "";
    private int index;
    private int isSuccess = 2;
    private long requestDelay;
    private byte[] responseBody;
    private Map<String, List<String>> responseHeader;
    private String result;
    private String url;

    public GrsResponse(int code2, Map<String, List<String>> responseHeader2, byte[] responseBody2, long requestDelay2) {
        this.code = code2;
        this.responseHeader = responseHeader2;
        this.responseBody = responseBody2;
        this.requestDelay = requestDelay2;
        parseResponse();
    }

    public GrsResponse(Exception exception, long requestDelay2) {
        this.errorException = exception;
        this.requestDelay = requestDelay2;
    }

    public String getErrorListStr() {
        return this.errorListStr;
    }

    private void setErrorListStr(String errorListStr2) {
        this.errorListStr = errorListStr2;
    }

    public String getResult() {
        return this.result;
    }

    private void setResult(String result2) {
        this.result = result2;
    }

    public String getCacheExpiretime() {
        return this.cacheExpireTime;
    }

    private void setCacheExpiretime(String cacheExpireTime2) {
        this.cacheExpireTime = cacheExpireTime2;
    }

    private void setIsSuccess(int isSuccess2) {
        this.isSuccess = isSuccess2;
    }

    public int getIsSuccess() {
        return this.isSuccess;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    private void setErrorCode(int errorCode2) {
        this.errorCode = errorCode2;
    }

    public String getErrorDesc() {
        return this.errorDesc;
    }

    private void setErrorDesc(String errorDesc2) {
        this.errorDesc = errorDesc2;
    }

    private void parseResponse() {
        parseHeader();
        parseBody();
    }

    public boolean isOK() {
        Logger.v(TAG, "GrsResponse return http code:%s", Integer.valueOf(this.code));
        return this.code == 200;
    }

    private void parseHeader() {
        if (isOK()) {
            try {
                getExpireTime(parseRespHeaders());
            } catch (JSONException e) {
                Logger.w(TAG, "parseHeader catch JSONException", e);
            }
        }
    }

    private void getExpireTime(Map<String, String> headers) throws JSONException {
        String dateStr;
        Date nowDate;
        if (headers != null) {
            if (headers.size() > 0) {
                long expireTime = 0;
                if (headers.containsKey(RESP_HEADER_CACHE_CONTROL)) {
                    String cacheControl = headers.get(RESP_HEADER_CACHE_CONTROL);
                    if (cacheControl.contains(CACHE_CONTROL_MAX_AGE)) {
                        try {
                            expireTime = Long.parseLong(cacheControl.substring(cacheControl.indexOf(CACHE_CONTROL_MAX_AGE) + CACHE_CONTROL_MAX_AGE.length()));
                            Logger.v(TAG, "Cache-Control value{%s}", Long.valueOf(expireTime));
                        } catch (NumberFormatException e) {
                            Logger.w(TAG, "getExpireTime addHeadersToResult NumberFormatException", e);
                        }
                    }
                } else if (headers.containsKey(RESP_HEADER_EXPIRES)) {
                    String expires = headers.get(RESP_HEADER_EXPIRES);
                    Logger.v(TAG, "expires is{%s}", expires);
                    DateFormat gmtDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.ROOT);
                    if (headers.containsKey(RESP_HEADER_DATE)) {
                        String dateStr2 = headers.get(RESP_HEADER_DATE);
                        Logger.v(TAG, "dateStr{%s}", dateStr2);
                        dateStr = dateStr2;
                    } else {
                        dateStr = null;
                    }
                    try {
                        Date dateExpires = gmtDateFormat.parse(expires);
                        if (TextUtils.isEmpty(dateStr)) {
                            nowDate = new Date();
                        } else {
                            nowDate = gmtDateFormat.parse(dateStr);
                        }
                        expireTime = (dateExpires.getTime() - nowDate.getTime()) / 1000;
                    } catch (ParseException e2) {
                        Logger.w(TAG, "getExpireTime ParseException.", e2);
                    }
                } else {
                    Logger.i(TAG, "response headers neither contains Cache-Control nor Expires.");
                }
                if (expireTime <= 0 || expireTime > 2592000) {
                    expireTime = 86400;
                }
                long expireTime2 = expireTime * 1000;
                Logger.v(TAG, "convert expireTime{%s}", Long.valueOf(expireTime2));
                setCacheExpiretime(String.valueOf(expireTime2 + System.currentTimeMillis()));
                return;
            }
        }
        Logger.w(TAG, "getExpireTime {headers == null} or {headers.size() <= 0}");
    }

    private Map<String, String> parseRespHeaders() {
        Map<String, String> header = new HashMap<>(16);
        Map<String, List<String>> map = this.responseHeader;
        if (map == null || map.size() <= 0) {
            Logger.v(TAG, "parseRespHeaders {respHeaders == null} or {respHeaders.size() <= 0}");
            return header;
        }
        for (Map.Entry<String, List<String>> entry : this.responseHeader.entrySet()) {
            header.put(entry.getKey(), entry.getValue().get(0));
        }
        return header;
    }

    private void parseBody() {
        if (!isOK()) {
            setIsSuccess(2);
            return;
        }
        try {
            String localResult = StringUtils.byte2Str(this.responseBody);
            JSONObject js = new JSONObject(localResult);
            int successValue = js.getInt(KEY_ISSUCCESS);
            setIsSuccess(successValue);
            boolean isExistService = successValue == 0 && localResult.indexOf("services") != -1;
            if (successValue == 1 || isExistService) {
                setResult(js.getJSONObject("services").toString());
                if (isExistService) {
                    setErrorListStr(js.getString("errorList"));
                    return;
                }
                return;
            }
            setIsSuccess(2);
            setErrorCode(js.getInt(KEY_ERRORCODE));
            setErrorDesc(js.getString(KEY_ERRORDESC));
        } catch (JSONException e) {
            Logger.w(TAG, "GrsResponse GrsResponse(String result) JSONException", e);
            setIsSuccess(2);
        }
    }

    public Exception getErrorException() {
        return this.errorException;
    }

    public void setUrl(String url2) {
        this.url = url2;
    }

    public String getUrl() {
        return this.url;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index2) {
        this.index = index2;
    }

    public int getCode() {
        return this.code;
    }

    public long getRequestDelay() {
        return this.requestDelay;
    }

    public void setRequestDelay(long requestDelay2) {
        this.requestDelay = requestDelay2;
    }
}
