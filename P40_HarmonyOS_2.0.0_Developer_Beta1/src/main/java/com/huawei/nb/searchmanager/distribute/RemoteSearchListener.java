package com.huawei.nb.searchmanager.distribute;

import com.huawei.nb.searchmanager.client.model.IndexData;
import com.huawei.nb.searchmanager.client.model.Recommendation;
import java.util.List;

public interface RemoteSearchListener {
    boolean beginRemoteSearch(DeviceInfo deviceInfo, String str);

    boolean endRemoteSearch(DeviceInfo deviceInfo, String str);

    int getSearchHitCount(String str);

    List<String> getTopFieldValues(String str, int i);

    List<Recommendation> groupSearch(String str, int i);

    List<IndexData> search(String str, int i, int i2);
}
