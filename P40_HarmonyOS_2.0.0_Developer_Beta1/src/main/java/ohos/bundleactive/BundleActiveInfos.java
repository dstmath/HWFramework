package ohos.bundleactive;

import com.huawei.ohos.bundleactiveadapter.BundleActiveInfosAdapter;

public final class BundleActiveInfos {
    private BundleActiveInfosAdapter mInfosAdapter;

    BundleActiveInfos(Object obj) {
        if (obj instanceof BundleActiveInfosAdapter) {
            this.mInfosAdapter = (BundleActiveInfosAdapter) obj;
        } else {
            this.mInfosAdapter = new BundleActiveInfosAdapter(null);
        }
    }

    public long queryAbilityPrevAccessMs() {
        return this.mInfosAdapter.queryAbilityPrevAccessMs();
    }

    public long queryAbilityInFgWholeMs() {
        return this.mInfosAdapter.queryAbilityInFgWholeMs();
    }

    public void merge(BundleActiveInfos bundleActiveInfos) {
        if (bundleActiveInfos != null) {
            this.mInfosAdapter.merge(bundleActiveInfos.mInfosAdapter);
        }
    }

    public long queryInfosBeginMs() {
        return this.mInfosAdapter.queryInfosBeginMs();
    }

    public long queryFgAbilityPrevAccessMs() {
        return this.mInfosAdapter.queryFgAbilityPrevAccessMs();
    }

    public long queryInfosEndMs() {
        return this.mInfosAdapter.queryInfosEndMs();
    }

    public long queryAbilityPrevSeenMs() {
        return this.mInfosAdapter.queryAbilityPrevSeenMs();
    }

    public long queryFgAbilityAccessWholeMs() {
        return this.mInfosAdapter.queryFgAbilityAccessWholeMs();
    }

    public long queryAbilitySeenWholeMs() {
        return this.mInfosAdapter.queryAbilitySeenWholeMs();
    }

    public String queryBundleName() {
        return this.mInfosAdapter.queryBundleName();
    }
}
