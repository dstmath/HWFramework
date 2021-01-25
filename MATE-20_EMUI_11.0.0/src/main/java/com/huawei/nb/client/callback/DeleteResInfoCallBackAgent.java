package com.huawei.nb.client.callback;

import com.huawei.nb.callback.IDeleteResInfoCallBack;

public abstract class DeleteResInfoCallBackAgent extends IDeleteResInfoCallBack.Stub {
    @Override // com.huawei.nb.callback.IDeleteResInfoCallBack
    public abstract void onFailure(int i, String str);

    @Override // com.huawei.nb.callback.IDeleteResInfoCallBack
    public abstract void onSuccess();
}
