package com.huawei.coauth.pool;

import com.huawei.coauth.pool.helper.AuthAttributes;
import com.huawei.coauth.pool.helper.AuthMessage;

public interface ExecutorMessenger {
    int finish(long j, int i, int i2, AuthAttributes authAttributes);

    int notify(AuthAttributes authAttributes);

    int progress(long j, int i, int i2, AuthAttributes authAttributes);

    int sendData(long j, long j2, int i, int i2, AuthMessage authMessage);
}
