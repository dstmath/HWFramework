package com.android.server.wifi.grs.requestremote;

import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class GrsResponse {
    private static final String CACHE_CONTROL_MAX_AGE = "max-age=";
    private static final int DEFAULT_CACHE_TIME = 86400;
    private static final int DEFAULT_ERROR_CODE = 9001;
    private static final int HTTP_SUCCESS = 200;
    private static final String KEY_ISSUCCESS = "isSuccess";
    private static final String KEY_SERVICE = "services";
    private static final int MAX_CACHE_TIME = 2592000;
    private static final int OBTAIN_ALL_FAILED = 2;
    private static final int OBTAIN_FAILED = 0;
    private static final int OBTAIN_SUCCESS = 1;
    private static final String RESP_HEADER_CACHE_CONTROL = "Cache-Control";
    private static final String RESP_HEADER_DATE = "Date";
    private static final String RESP_HEADER_EXPIRES = "Expires";
    private static final String TAG = GrsResponse.class.getSimpleName();
    private int mCode = 0;
    private Exception mErrorException;
    private int mIndex;
    private byte[] mResponseBody;
    private Map<String, List<String>> mResponseHeader;
    private String mResult;
    private String mUrl;

    public GrsResponse(int code, Map<String, List<String>> responseHeader, byte[] responseBody) {
        this.mCode = code;
        this.mResponseHeader = responseHeader;
        this.mResponseBody = responseBody;
        parseResponse();
    }

    public GrsResponse(Exception e) {
        this.mErrorException = e;
    }

    public String getResult() {
        return this.mResult;
    }

    private void setResult(String result) {
        this.mResult = result;
    }

    private void parseResponse() {
        parseBody();
    }

    public boolean isOk() {
        String str = TAG;
        Log.d(str, "GrsResponse return http code:" + this.mCode);
        return this.mCode == 200;
    }

    private void parseBody() {
        if (isOk()) {
            try {
                String content = byte2Str(this.mResponseBody);
                String str = TAG;
                Log.d(str, "GRS response, content = " + content);
                JSONObject js = new JSONObject(content);
                int successValue = js.getInt(KEY_ISSUCCESS);
                boolean isExistService = successValue == 0 && content.indexOf(KEY_SERVICE) != -1;
                if (successValue == 1 || isExistService) {
                    setResult(js.getJSONObject(KEY_SERVICE).toString());
                }
            } catch (JSONException e) {
                Log.d(TAG, "GrsResponse GrsResponse(String result) JSONException");
            }
        }
    }

    private static String byte2Str(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "StringUtils.byte2str error: UnsupportedEncodingException");
            return "";
        }
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public String getUrl() {
        return this.mUrl;
    }

    public int getIndex() {
        return this.mIndex;
    }

    public void setIndex(int index) {
        this.mIndex = index;
    }
}
