package com.huawei.nb.searchmanager.client.listener;

import com.huawei.nb.searchmanager.client.model.ChangedIndexContent;

public interface IIndexChangeListener {
    void onDataChanged(String str, ChangedIndexContent changedIndexContent);
}
