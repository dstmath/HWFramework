package com.huawei.wallet.sdk.business.bankcard.api;

import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import java.util.Map;

public interface CardAndIssuerInfoCacheApi {
    IssuerInfoItem cacheIssuerInfoItem(String str);

    Map<String, IssuerInfoItem> cacheIssuerInfoItems();

    Map<String, IssuerInfoItem> cacheIssuerInfoItemsOnlyLocal();

    void loadLocalCardProductInfo();

    void loadLocalIssuerInfo();
}
