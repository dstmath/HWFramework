package android.widget;

import android.view.View;

public class HwParallelWorkerDummy implements HwParallelWorker {
    @Override // android.widget.HwParallelWorker
    public void postTaskToParallelWorkerByListView(boolean isDown) {
    }

    @Override // android.widget.HwParallelWorker
    public boolean isPrefetchOptimizeEnable() {
        return false;
    }

    @Override // android.widget.HwParallelWorker
    public void resetPrefetchTaskStatus() {
    }

    @Override // android.widget.HwParallelWorker
    public int getPrefetchTaskStatus() {
        return 0;
    }

    @Override // android.widget.HwParallelWorker
    public void recordObtainViewTimeDelay(long nanoTimeDelay) {
    }

    @Override // android.widget.HwParallelWorker
    public View getPrefetchView() {
        return null;
    }

    @Override // android.widget.HwParallelWorker
    public boolean isPrefetchViewScrap() {
        return false;
    }

    @Override // android.widget.HwParallelWorker
    public boolean isPrefetchViewPosValid(int position) {
        return false;
    }

    @Override // android.widget.HwParallelWorker
    public void clearPrefetchInfo() {
    }
}
