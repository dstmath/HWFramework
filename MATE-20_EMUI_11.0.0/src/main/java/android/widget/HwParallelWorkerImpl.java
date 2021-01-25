package android.widget;

import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import android.view.View;
import java.util.Vector;

public class HwParallelWorkerImpl implements HwParallelWorker, Runnable {
    private static final boolean IS_PROFETCH_ENABLE = SystemProperties.getBoolean("agp_prefetch_view_enable", false);
    private static final int PREFETCH_TASK_FINISH = 1;
    private static final int PREFETCH_TASK_NOT_FINISH = 0;
    private static final String TAG = "ParallelWorker";
    private ListView mCurrentListView;
    private boolean mIsSwipeDown;
    private int mPrefetchTaskStatus = 0;
    private PrefetchViewEntity mPrefetchView = new PrefetchViewEntity();
    private PrefetchViewPredictor mPrefetchViewPredictor = new PrefetchViewPredictor();
    private Task mTask = new Task();

    public HwParallelWorkerImpl(ListView view) {
        this.mCurrentListView = view;
    }

    public void postTaskToParallelWorkerByListView(boolean isDown) {
        ListView listView = this.mCurrentListView;
        if (listView != null && listView.isAttachedToWindow()) {
            this.mIsSwipeDown = isDown;
            this.mCurrentListView.post(this);
        }
    }

    public boolean isPrefetchOptimizeEnable() {
        return IS_PROFETCH_ENABLE;
    }

    public void resetPrefetchTaskStatus() {
        this.mPrefetchTaskStatus = 0;
    }

    public int getPrefetchTaskStatus() {
        return this.mPrefetchTaskStatus;
    }

    public void recordObtainViewTimeDelay(long nanoTimeDelay) {
        PrefetchViewPredictor prefetchViewPredictor = this.mPrefetchViewPredictor;
        if (prefetchViewPredictor != null) {
            prefetchViewPredictor.addObtainViewTimeDelay(nanoTimeDelay);
        }
    }

    public View getPrefetchView() {
        PrefetchViewEntity prefetchViewEntity = this.mPrefetchView;
        if (prefetchViewEntity != null) {
            return prefetchViewEntity.mView;
        }
        return null;
    }

    public boolean isPrefetchViewScrap() {
        PrefetchViewEntity prefetchViewEntity = this.mPrefetchView;
        if (prefetchViewEntity != null) {
            return prefetchViewEntity.mIsScrap;
        }
        return false;
    }

    public boolean isPrefetchViewPosValid(int position) {
        PrefetchViewEntity prefetchViewEntity = this.mPrefetchView;
        if (prefetchViewEntity == null || prefetchViewEntity.mPosition != position) {
            return false;
        }
        return true;
    }

    public void clearPrefetchInfo() {
        this.mPrefetchView.clear();
    }

    private void prefetch() {
        buildTask();
        flushTask();
    }

    private void buildTask() {
        int prefetchViewPos = generatePrefetchViewPos();
        int headerViewsEnd = this.mCurrentListView.getHeaderViewsCount();
        if (prefetchViewPos >= this.mCurrentListView.mItemCount - this.mCurrentListView.getFooterViewsCount() || prefetchViewPos <= headerViewsEnd) {
            this.mPrefetchTaskStatus = 1;
            Log.w(TAG, "AGP: view prefetch position is invalid.");
            this.mTask.clear();
            return;
        }
        this.mTask.mPrefetchPos = prefetchViewPos;
        this.mTask.mListView = this.mCurrentListView;
    }

    private int generatePrefetchViewPos() {
        int visibleFirstPos = this.mCurrentListView.getFirstVisiblePosition();
        int visibleLastPos = this.mCurrentListView.getLastVisiblePosition();
        if (this.mIsSwipeDown) {
            return visibleLastPos + 1;
        }
        return visibleFirstPos - 1;
    }

    private void flushTask() {
        if (this.mTask.mListView != null && this.mTask.mPrefetchPos != 0) {
            flushTaskExecute(this.mTask.mListView, this.mTask.mPrefetchPos);
            this.mTask.clear();
        }
    }

    private void flushTaskExecute(ListView view, int position) {
        if (isViewAlreadyPrefetch(position)) {
            this.mPrefetchTaskStatus = 1;
            return;
        }
        this.mPrefetchViewPredictor.setLastVsyncTime(view.getDrawingTime());
        boolean[] isScraps = new boolean[1];
        View prefetchView = this.mPrefetchViewPredictor.tryObtainViewByPos(view, position, isScraps);
        if (prefetchView != null) {
            savePrefetchView(prefetchView, position, isScraps[0]);
        }
    }

    private boolean isViewAlreadyPrefetch(int position) {
        if (this.mPrefetchView.mPosition == position) {
            return true;
        }
        return false;
    }

    private void savePrefetchView(View view, int position, boolean isScrap) {
        this.mPrefetchView.mView = view;
        this.mPrefetchView.mPosition = position;
        this.mPrefetchView.mIsScrap = isScrap;
    }

    @Override // java.lang.Runnable
    public void run() {
        try {
            Trace.traceBegin(8, "AGP:ParallelWorker");
            prefetch();
        } finally {
            Trace.traceEnd(8);
        }
    }

    /* access modifiers changed from: package-private */
    public static class Task {
        private ListView mListView;
        private int mPrefetchPos;

        Task() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clear() {
            this.mListView = null;
            this.mPrefetchPos = 0;
        }
    }

    public static class PrefetchViewEntity {
        private boolean mIsScrap;
        private int mPosition;
        private View mView;

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clear() {
            this.mView = null;
            this.mPosition = 0;
            this.mIsScrap = false;
        }
    }

    public final class PrefetchViewPredictor {
        private static final int ARRAY_CAPCIRY = 5;
        private static final double DEFALUT_OBTAIN_VIEW_TIME = 10.0d;
        private static final double FRAME_TIME = 16.6d;
        private static final double NS_PER_MS = 1000000.0d;
        private static final double TOTAL_TIME_DELAY_THRESHOLD = 24.900000000000002d;
        private static final double UI_TIME_DELAY_THRESHOLD = 5.0d;
        private double mLastFrameVsync = Double.MAX_VALUE;
        private Vector<Long> mObtainViewTimeArray = new Vector<>(5);

        public PrefetchViewPredictor() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setLastVsyncTime(long lastFrameVsync) {
            this.mLastFrameVsync = (double) lastFrameVsync;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addObtainViewTimeDelay(long nanoTimeDelay) {
            if (this.mObtainViewTimeArray.size() >= 5) {
                this.mObtainViewTimeArray.remove(0);
            }
            if (this.mObtainViewTimeArray.size() < 5) {
                this.mObtainViewTimeArray.add(Long.valueOf(nanoTimeDelay));
            }
        }

        private double getObtainViewTimeDelay() {
            if (this.mObtainViewTimeArray.isEmpty()) {
                return DEFALUT_OBTAIN_VIEW_TIME;
            }
            double sum = 0.0d;
            int arraySize = this.mObtainViewTimeArray.size();
            for (int i = 0; i < arraySize; i++) {
                sum += ((double) this.mObtainViewTimeArray.get(i).longValue()) / NS_PER_MS;
            }
            return sum / ((double) arraySize);
        }

        private boolean isPrefetchCanFinish() {
            double uiTimeDelay = (((double) System.nanoTime()) / NS_PER_MS) - this.mLastFrameVsync;
            return uiTimeDelay <= UI_TIME_DELAY_THRESHOLD && uiTimeDelay + getObtainViewTimeDelay() <= TOTAL_TIME_DELAY_THRESHOLD;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private View tryObtainViewByPos(ListView listView, int position, boolean[] outMetaData) {
            if (isPrefetchCanFinish()) {
                View view = listView.obtainView(position, outMetaData);
                HwParallelWorkerImpl.this.mPrefetchTaskStatus = 1;
                return view;
            }
            HwParallelWorkerImpl.this.mPrefetchTaskStatus = 0;
            return null;
        }
    }
}
