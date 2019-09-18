package com.huawei.nb.client.callback;

import com.huawei.nb.client.ai.NetworkType;
import com.huawei.nb.client.ai.UpdatePackageInfo;
import java.util.List;

public interface UpdatePackageCheckCallBack {
    void onFinish(List<UpdatePackageInfo> list, NetworkType networkType);
}
