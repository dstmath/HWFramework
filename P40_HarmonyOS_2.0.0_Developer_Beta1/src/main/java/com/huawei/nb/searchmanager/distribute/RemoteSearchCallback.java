package com.huawei.nb.searchmanager.distribute;

import com.huawei.nb.searchmanager.client.model.IndexData;
import com.huawei.nb.searchmanager.client.model.Recommendation;
import com.huawei.nb.searchmanager.distribute.IRemoteSearchCallback;
import java.util.List;
import java.util.Objects;

public class RemoteSearchCallback extends IRemoteSearchCallback.Stub {
    private RemoteSearchListener remoteSearchListener;

    public RemoteSearchCallback(RemoteSearchListener remoteSearchListener2) {
        Objects.requireNonNull(remoteSearchListener2, "remote search listener cannot be null");
        this.remoteSearchListener = remoteSearchListener2;
    }

    @Override // com.huawei.nb.searchmanager.distribute.IRemoteSearchCallback
    public List<String> getTopFieldValues(String str, int i) {
        return this.remoteSearchListener.getTopFieldValues(str, i);
    }

    @Override // com.huawei.nb.searchmanager.distribute.IRemoteSearchCallback
    public int getSearchHitCount(String str) {
        return this.remoteSearchListener.getSearchHitCount(str);
    }

    @Override // com.huawei.nb.searchmanager.distribute.IRemoteSearchCallback
    public List<IndexData> search(String str, int i, int i2) {
        return this.remoteSearchListener.search(str, i, i2);
    }

    @Override // com.huawei.nb.searchmanager.distribute.IRemoteSearchCallback
    public List<Recommendation> groupSearch(String str, int i) {
        return this.remoteSearchListener.groupSearch(str, i);
    }

    @Override // com.huawei.nb.searchmanager.distribute.IRemoteSearchCallback
    public boolean beginRemoteSearch(DeviceInfo deviceInfo, String str) {
        return this.remoteSearchListener.beginRemoteSearch(deviceInfo, str);
    }

    @Override // com.huawei.nb.searchmanager.distribute.IRemoteSearchCallback
    public boolean endRemoteSearch(DeviceInfo deviceInfo, String str) {
        return this.remoteSearchListener.endRemoteSearch(deviceInfo, str);
    }
}
