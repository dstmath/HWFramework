package ohos.data.search;

import java.util.Objects;
import ohos.data.search.listener.IIndexChangeListener;
import ohos.data.searchimpl.connect.IIndexChangeCallback;
import ohos.data.searchimpl.model.InnerChangedIndexContent;

class IndexChangeCallback extends IIndexChangeCallback.Stub {
    private IIndexChangeListener indexChangeListener;

    IndexChangeCallback(IIndexChangeListener iIndexChangeListener) {
        this.indexChangeListener = iIndexChangeListener;
    }

    @Override // ohos.data.searchimpl.connect.IIndexChangeCallback
    public void onDataChanged(String str, InnerChangedIndexContent innerChangedIndexContent) {
        if (this.indexChangeListener != null) {
            this.indexChangeListener.onDataChanged(str, ConvertUtils.innerIndexContent2IndexContent(innerChangedIndexContent));
        }
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof IndexChangeCallback)) {
            return false;
        }
        return Objects.equals(this.indexChangeListener, ((IndexChangeCallback) obj).indexChangeListener);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(this.indexChangeListener);
    }
}
