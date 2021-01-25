package ohos.data.search;

import java.util.List;
import ohos.data.search.model.IndexData;
import ohos.data.search.model.Recommendation;
import ohos.data.searchimpl.connect.ISearchSession;

public class SearchSession {
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
        return ConvertUtils.innerIndexDatas2IndexDatas(this.proxy.search(str, i, i2));
    }

    public List<Recommendation> groupSearch(String str, int i) {
        return ConvertUtils.innerRecommendations2Recommendations(this.proxy.groupSearch(str, i));
    }
}
