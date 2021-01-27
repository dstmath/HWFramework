package ohos.data.search;

import android.os.SharedMemory;
import android.system.ErrnoException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import ohos.data.search.listener.IIndexChangeListener;
import ohos.data.search.model.ChangedIndexContent;
import ohos.data.search.model.IndexData;
import ohos.data.searchimpl.connect.IIndexChangeCallback;
import ohos.data.searchimpl.model.InnerChangedIndexContent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

class IndexChangeCallback extends IIndexChangeCallback.Stub {
    private static final int DELETE = 2;
    private static final int INSERT = 0;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109504, "IndexChangeCallback");
    private static final int UPDATE = 1;
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

    @Override // ohos.data.searchimpl.connect.IIndexChangeCallback
    public void onDataChangedLarge(String str, SharedMemory sharedMemory, int i) {
        ChangedIndexContent changedIndexContent;
        ChangedIndexContent changedIndexContent2 = new ChangedIndexContent(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        try {
            if (this.indexChangeListener != null) {
                List<IndexData> readIndexDataList = SharedMemoryHelper.readIndexDataList(sharedMemory);
                if (i == 0) {
                    changedIndexContent = new ChangedIndexContent(readIndexDataList, Collections.emptyList(), Collections.emptyList());
                } else if (i == 1) {
                    changedIndexContent = new ChangedIndexContent(Collections.emptyList(), readIndexDataList, Collections.emptyList());
                } else if (i == 2) {
                    changedIndexContent = new ChangedIndexContent(Collections.emptyList(), Collections.emptyList(), readIndexDataList);
                } else {
                    changedIndexContent = new ChangedIndexContent(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
                }
                changedIndexContent2 = changedIndexContent;
            }
        } catch (ErrnoException e) {
            HiLog.error(LABEL, "mapReadOnly error, %{public}s", new Object[]{e.getMessage()});
        } catch (Throwable th) {
            SharedMemoryHelper.releaseMemory(sharedMemory);
            throw th;
        }
        SharedMemoryHelper.releaseMemory(sharedMemory);
        IIndexChangeListener iIndexChangeListener = this.indexChangeListener;
        if (iIndexChangeListener != null) {
            iIndexChangeListener.onDataChanged(str, changedIndexContent2);
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
