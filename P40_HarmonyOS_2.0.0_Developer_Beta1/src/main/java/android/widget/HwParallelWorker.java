package android.widget;

import android.view.View;

public interface HwParallelWorker {
    void clearPrefetchInfo();

    int getPrefetchTaskStatus();

    View getPrefetchView();

    boolean isPrefetchOptimizeEnable();

    boolean isPrefetchViewPosValid(int i);

    boolean isPrefetchViewScrap();

    void postTaskToParallelWorkerByListView(boolean z);

    void recordObtainViewTimeDelay(long j);

    void resetPrefetchTaskStatus();
}
