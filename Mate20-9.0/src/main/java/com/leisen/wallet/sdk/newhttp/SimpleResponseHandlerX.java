package com.leisen.wallet.sdk.newhttp;

import com.leisen.wallet.sdk.util.LogUtil;
import java.io.UnsupportedEncodingException;

public abstract class SimpleResponseHandlerX extends AsyncHttpResponseHandlerX {
    public abstract void OnFailure(String str, Throwable th);

    public abstract void onSuccess(String str);

    public void onSuccess(int statusCode, byte[] responseBody) {
        onSuccess(getResponseString(responseBody, getCharset()));
    }

    public void onFailure(int statusCode, byte[] responseBody, Throwable error) {
        OnFailure(getResponseString(responseBody, getCharset()), error);
    }

    private String getResponseString(byte[] data, String charset) {
        String str = null;
        if (data != null) {
            try {
                str = new String(data, charset);
            } catch (UnsupportedEncodingException e) {
                LogUtil.e("", "==>" + e.getMessage());
                return null;
            }
        }
        return str;
    }
}
