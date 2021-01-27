package ohos.data.search;

import android.os.SharedMemory;
import android.system.ErrnoException;
import java.util.Collections;
import java.util.List;
import ohos.data.orm.OrmConfig;
import ohos.data.search.model.IndexData;
import ohos.data.search.model.Recommendation;
import ohos.data.searchimpl.connect.ISearchSession;
import ohos.data.searchimpl.model.InnerIndexData;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class SearchSession {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109504, "SearchSession");
    private ISearchSession proxy;

    SearchSession() {
    }

    /* access modifiers changed from: package-private */
    public void setProxy(ISearchSession iSearchSession) {
        this.proxy = iSearchSession;
    }

    /* access modifiers changed from: package-private */
    public ISearchSession getProxy() {
        return this.proxy;
    }

    public int getSearchHitCount(String str) {
        return this.proxy.getSearchHitCount(str);
    }

    public List<IndexData> search(String str, int i, int i2) {
        List<IndexData> list;
        Collections.emptyList();
        List<IndexData> emptyList = Collections.emptyList();
        long currentTimeMillis = System.currentTimeMillis();
        SharedMemory sharedMemory = null;
        try {
            sharedMemory = SharedMemory.create("SearchSharedMemory", OrmConfig.MAX_ENCRYPT_KEY_SIZE);
            List<InnerIndexData> searchLarge = this.proxy.searchLarge(str, i, i2, sharedMemory);
            if (searchLarge == null) {
                HiLog.info(LABEL, "use sharedMemory to return search result.", new Object[0]);
                list = SharedMemoryHelper.readIndexDataList(sharedMemory);
            } else {
                list = ConvertUtils.innerIndexDatas2IndexDatas(searchLarge);
            }
            emptyList = list;
        } catch (ErrnoException e) {
            HiLog.error(LABEL, "Failed to search, read reply memory error, errMsg: %{public}s", new Object[]{e.getMessage()});
        } catch (Throwable th) {
            SharedMemoryHelper.releaseMemory(null);
            throw th;
        }
        SharedMemoryHelper.releaseMemory(sharedMemory);
        HiLog.info(LABEL, "search end, hit size is %{public}d, cost %{public}d", new Object[]{Integer.valueOf(emptyList.size()), Long.valueOf(System.currentTimeMillis() - currentTimeMillis)});
        return emptyList;
    }

    public List<Recommendation> groupSearch(String str, int i) {
        long currentTimeMillis = System.currentTimeMillis();
        List<Recommendation> innerRecommendations2Recommendations = ConvertUtils.innerRecommendations2Recommendations(this.proxy.groupSearch(str, i));
        HiLog.info(LABEL, "groupSearch end, hit size is %{public}d, cost %{public}d", new Object[]{Integer.valueOf(innerRecommendations2Recommendations.size()), Long.valueOf(System.currentTimeMillis() - currentTimeMillis)});
        return innerRecommendations2Recommendations;
    }
}
