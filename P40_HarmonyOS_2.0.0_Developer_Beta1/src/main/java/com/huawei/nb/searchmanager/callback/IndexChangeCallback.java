package com.huawei.nb.searchmanager.callback;

import android.os.SharedMemory;
import android.system.ErrnoException;
import com.huawei.nb.searchmanager.callback.IIndexChangeCallback;
import com.huawei.nb.searchmanager.client.listener.IIndexChangeListener;
import com.huawei.nb.searchmanager.client.model.ChangedIndexContent;
import com.huawei.nb.searchmanager.client.model.IndexData;
import com.huawei.nb.searchmanager.utils.SharedMemoryHelper;
import com.huawei.nb.searchmanager.utils.logger.DSLog;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class IndexChangeCallback extends IIndexChangeCallback.Stub {
    private static final String TAG = "IndexChangeCallback";
    private IIndexChangeListener indexChangeListener;

    public IndexChangeCallback(IIndexChangeListener iIndexChangeListener) {
        this.indexChangeListener = iIndexChangeListener;
    }

    @Override // com.huawei.nb.searchmanager.callback.IIndexChangeCallback
    public void onDataChanged(String str, ChangedIndexContent changedIndexContent) {
        IIndexChangeListener iIndexChangeListener = this.indexChangeListener;
        if (iIndexChangeListener != null) {
            iIndexChangeListener.onDataChanged(str, changedIndexContent);
        }
    }

    @Override // com.huawei.nb.searchmanager.callback.IIndexChangeCallback
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
            String str2 = TAG;
            DSLog.et(str2, "mapReadOnly error, " + e.getMessage(), new Object[0]);
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
