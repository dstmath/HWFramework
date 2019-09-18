package com.huawei.nb.client.callback;

import com.huawei.nb.callback.IDeleteResInfoCallBack;

public abstract class DeleteResInfoCallBackAgent extends IDeleteResInfoCallBack.Stub {
    public abstract void onFailure(int i, String str);

    public abstract void onSuccess();
}
