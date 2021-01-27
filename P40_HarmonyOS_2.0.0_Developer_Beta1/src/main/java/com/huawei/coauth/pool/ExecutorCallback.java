package com.huawei.coauth.pool;

import com.huawei.coauth.pool.helper.AuthAttributes;
import com.huawei.coauth.pool.helper.AuthMessage;

public interface ExecutorCallback {
    int onBeginExecute(long j, byte[] bArr, int[] iArr, AuthAttributes authAttributes);

    int onEndExecute(long j, AuthAttributes authAttributes);

    AuthAttributes onGetProperty(int[] iArr, AuthAttributes authAttributes);

    void onMessengerReady(ExecutorMessenger executorMessenger);

    int onReceiveData(long j, long j2, int i, int i2, AuthMessage authMessage);

    int onSetProperty(int[] iArr, AuthAttributes authAttributes);

    int prepareExecute(long j, byte[] bArr, int[] iArr, AuthAttributes authAttributes);
}
