package com.huawei.nb.searchmanager.client;

import android.os.RemoteException;
import android.os.SharedMemory;
import android.system.ErrnoException;
import com.huawei.nb.searchmanager.client.model.IndexData;
import com.huawei.nb.searchmanager.client.model.Recommendation;
import com.huawei.nb.searchmanager.client.model.Token;
import com.huawei.nb.searchmanager.utils.SharedMemoryHelper;
import com.huawei.nb.searchmanager.utils.logger.DSLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SearchSession {
    private static final String TAG = "SearchSession";
    private ISearchSession searchSessionProxy;

    SearchSession(ISearchSession iSearchSession) {
        this.searchSessionProxy = iSearchSession;
    }

    /* access modifiers changed from: package-private */
    public ISearchSession getSearchSessionProxy() {
        return this.searchSessionProxy;
    }

    public List<String> getTopFieldValues(String str, int i) {
        try {
            return this.searchSessionProxy.getTopFieldValues(str, i);
        } catch (RemoteException e) {
            DSLog.et(TAG, "getTopFieldValues remote exception: %s", e.getMessage());
            return null;
        }
    }

    public int getSearchHitCount(String str) {
        int i;
        long currentTimeMillis = System.currentTimeMillis();
        try {
            i = this.searchSessionProxy.getSearchHitCount(str);
        } catch (RemoteException e) {
            DSLog.et(TAG, "getSearchHitCount remote exception: %s", e.getMessage());
            i = 0;
        }
        DSLog.dt(TAG, "get search hit count is " + i + ", cost " + (System.currentTimeMillis() - currentTimeMillis), new Object[0]);
        return i;
    }

    public List<IndexData> search(String str, int i, int i2) {
        List<IndexData> emptyList = Collections.emptyList();
        long currentTimeMillis = System.currentTimeMillis();
        SharedMemory sharedMemory = null;
        try {
            sharedMemory = SharedMemory.create("SearchSharedMemory", SharedMemoryHelper.LARGE_THRESHOLD);
            emptyList = this.searchSessionProxy.searchLarge(str, i, i2, sharedMemory);
            if (emptyList == null) {
                DSLog.it(TAG, "use sharedMemory to return search result.", new Object[0]);
                emptyList = SharedMemoryHelper.readIndexDataList(sharedMemory);
            }
        } catch (RemoteException e) {
            DSLog.et(TAG, "search remote exception: %s", e.getMessage());
        } catch (ErrnoException e2) {
            DSLog.et(TAG, "Failed to search, read reply memory error, errMsg: %s", e2.getMessage());
        } catch (Throwable th) {
            SharedMemoryHelper.releaseMemory(null);
            throw th;
        }
        SharedMemoryHelper.releaseMemory(sharedMemory);
        if (emptyList != null) {
            DSLog.it(TAG, "search end, hit size is " + emptyList.size() + ", cost " + (System.currentTimeMillis() - currentTimeMillis), new Object[0]);
        }
        return emptyList;
    }

    public List<Recommendation> groupSearch(String str, int i) {
        List<Recommendation> list;
        long currentTimeMillis = System.currentTimeMillis();
        try {
            list = this.searchSessionProxy.groupSearch(str, i);
        } catch (RemoteException e) {
            DSLog.et(TAG, "group search remote exception: %s", e.getMessage());
            list = null;
        }
        if (list != null) {
            DSLog.dt(TAG, "groupSearch end, size is " + list.size() + ", cost " + (System.currentTimeMillis() - currentTimeMillis), new Object[0]);
        } else {
            DSLog.dt(TAG, "groupSearch end, cost " + (System.currentTimeMillis() - currentTimeMillis), new Object[0]);
        }
        return list;
    }

    public List<Recommendation> groupTimeline(String str, String str2) {
        ArrayList arrayList = new ArrayList();
        long currentTimeMillis = System.currentTimeMillis();
        Token token = new Token();
        while (!token.isFinish()) {
            List<Recommendation> doGroupTimeline = doGroupTimeline(str, str2, token);
            if (doGroupTimeline != null && !doGroupTimeline.isEmpty()) {
                arrayList.addAll(doGroupTimeline);
            }
        }
        DSLog.dt(TAG, "groupTimeline end, size is " + arrayList.size() + ", cost " + (System.currentTimeMillis() - currentTimeMillis), new Object[0]);
        return arrayList;
    }

    private List<Recommendation> doGroupTimeline(String str, String str2, Token token) {
        try {
            return this.searchSessionProxy.groupTimeline(str, str2, token);
        } catch (RemoteException e) {
            token.setFinish(true);
            DSLog.et(TAG, "group Timeline remote exception: %s", e.getMessage());
            return null;
        }
    }

    public Map<String, List<Recommendation>> coverSearch(String str, List<String> list, int i) {
        Map map;
        long currentTimeMillis = System.currentTimeMillis();
        try {
            map = this.searchSessionProxy.coverSearch(str, list, i);
        } catch (RemoteException e) {
            DSLog.et(TAG, "cover search remote exception: %s", e.getMessage());
            map = null;
        }
        if (map != null) {
            DSLog.dt(TAG, "coverSearch end, size is " + map.size() + ", cost" + (System.currentTimeMillis() - currentTimeMillis), new Object[0]);
        } else {
            DSLog.dt(TAG, "coverSearch end, cost " + (System.currentTimeMillis() - currentTimeMillis), new Object[0]);
        }
        return map;
    }
}
