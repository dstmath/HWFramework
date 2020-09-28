package com.huawei.security.deviceauth;

import android.support.annotation.Nullable;

public interface HwDevAuthCallback {
    boolean onDataTransmit(String str, byte[] bArr);

    void onOperationFinished(String str, OperationCode operationCode, int i, @Nullable byte[] bArr);

    ConfirmParams onReceiveRequest(String str, OperationCode operationCode);

    void onSessionKeyReturned(String str, byte[] bArr);
}
