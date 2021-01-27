package com.huawei.nb.searchmanager.distribute;

import android.os.RemoteException;
import com.huawei.nb.searchmanager.client.model.IndexData;
import com.huawei.nb.searchmanager.client.model.Recommendation;
import java.util.List;
import java.util.Objects;

public class RemoteSearchSession {
    private IRemoteSearchCallback callback;

    public RemoteSearchSession(IRemoteSearchCallback iRemoteSearchCallback) {
        Objects.requireNonNull(iRemoteSearchCallback, "Remote search callback cannot be null");
        this.callback = iRemoteSearchCallback;
    }

    public List<String> getTopFieldValues(String str, int i) throws RemoteException {
        return this.callback.getTopFieldValues(str, i);
    }

    public int getSearchHitCount(String str) throws RemoteException {
        return this.callback.getSearchHitCount(str);
    }

    public List<IndexData> search(String str, int i, int i2) throws RemoteException {
        return this.callback.search(str, i, i2);
    }

    public List<Recommendation> groupSearch(String str, int i) throws RemoteException {
        return this.callback.groupSearch(str, i);
    }
}
