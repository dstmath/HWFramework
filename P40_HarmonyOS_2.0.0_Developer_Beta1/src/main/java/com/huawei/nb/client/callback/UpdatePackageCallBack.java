package com.huawei.nb.client.callback;

import com.huawei.nb.client.ai.UpdateStatus;

public interface UpdatePackageCallBack {
    void onRefresh(UpdateStatus updateStatus, long j, long j2, int i, int i2, int i3, String str);
}
