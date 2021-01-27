package com.huawei.internal.telephony.uicc;

import com.android.internal.telephony.uicc.AdnRecordCache;

public class AdnRecordCacheEx {
    private AdnRecordCache mAdnRecordCache;

    public static AdnRecordCacheEx from(AdnRecordCache adnRecordCache) {
        if (adnRecordCache == null) {
            return null;
        }
        AdnRecordCacheEx adnRecordCacheEx = new AdnRecordCacheEx();
        adnRecordCacheEx.setAdnRecordCache(adnRecordCache);
        return adnRecordCacheEx;
    }

    private void setAdnRecordCache(AdnRecordCache adnRecordCache) {
        this.mAdnRecordCache = adnRecordCache;
    }

    public void reset() {
        AdnRecordCache adnRecordCache = this.mAdnRecordCache;
        if (adnRecordCache != null) {
            adnRecordCache.reset();
        }
    }
}
