package com.leisen.wallet.sdk.http;

import com.leisen.wallet.sdk.util.LogUtil;
import java.io.UnsupportedEncodingException;
import org.apache.http.Header;

public abstract class SimpleResponseHandler extends AsyncHttpResponseHandler {
    public abstract void OnFailure(String str, Throwable th);

    public abstract void onSuccess(String str);

    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        onSuccess(getResponseString(responseBody, getCharset()));
    }

    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        OnFailure(getResponseString(responseBody, getCharset()), error);
    }

    private String getResponseString(byte[] data, String charset) {
        String str;
        if (data != null) {
            try {
                str = new String(data, charset);
            } catch (UnsupportedEncodingException e) {
                LogUtil.e("", "==>" + e.getMessage());
                return null;
            }
        }
        str = null;
        return str;
    }
}
