package com.huawei.nb.coordinator.helper.verify;

import android.content.Context;
import android.os.Bundle;
import com.huawei.nb.coordinator.helper.http.HttpRequest;

public class VerifyNone implements IVerify {
    @Override // com.huawei.nb.coordinator.helper.verify.IVerify
    public boolean generateAuthorization(Context context, HttpRequest.Builder builder, String str, Bundle bundle) {
        return true;
    }

    @Override // com.huawei.nb.coordinator.helper.verify.IVerify
    public String verifyTokenHeader() {
        return null;
    }
}
