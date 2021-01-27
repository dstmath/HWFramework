package ohos.data.search.listener;

import ohos.data.search.model.ChangedIndexContent;

public interface IIndexChangeListener {
    void onDataChanged(String str, ChangedIndexContent changedIndexContent);
}
