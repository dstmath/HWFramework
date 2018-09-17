package com.huawei.systemmanager.rainbow.comm.request;

import android.content.Context;
import com.huawei.systemmanager.rainbow.comm.request.exception.RainbowRequestException;

public interface ICommonRequest {

    public enum RequestType {
        REQUEST_GET,
        REQUEST_POST
    }

    String doGetRequest(String str) throws RainbowRequestException;

    String doPostRequest(String str, Object obj, boolean z, Context context);

    void setTimeout(int i, int i2);
}
