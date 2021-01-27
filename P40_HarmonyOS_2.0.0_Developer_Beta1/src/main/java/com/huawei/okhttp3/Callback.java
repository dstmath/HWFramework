package com.huawei.okhttp3;

import java.io.IOException;

@Deprecated
public interface Callback {
    void onFailure(Call call, IOException iOException);

    void onResponse(Call call, Response response) throws IOException;
}
