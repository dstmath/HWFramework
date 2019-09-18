package com.huawei.nb.coordinator.helper.verify;

import android.content.Context;
import com.huawei.nb.coordinator.helper.http.HttpRequest;

public interface IVerify extends IVerifyVar {
    boolean generateAuthorization(Context context, HttpRequest.Builder builder, String str) throws VerifyException;

    String verifyTokenHeader();
}
