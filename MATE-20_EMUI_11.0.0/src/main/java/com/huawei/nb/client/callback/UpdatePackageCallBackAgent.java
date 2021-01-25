package com.huawei.nb.client.callback;

import com.huawei.nb.callback.IUpdatePackageCallBack;

public abstract class UpdatePackageCallBackAgent extends IUpdatePackageCallBack.Stub {
    @Override // com.huawei.nb.callback.IUpdatePackageCallBack
    public abstract int onRefresh(int i, long j, long j2, int i2, int i3, int i4, String str);
}
