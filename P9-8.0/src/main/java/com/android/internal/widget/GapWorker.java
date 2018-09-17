package com.android.internal.widget;

import android.os.Trace;
import com.android.internal.widget.RecyclerView.LayoutManager;
import com.android.internal.widget.RecyclerView.LayoutManager.LayoutPrefetchRegistry;
import com.android.internal.widget.RecyclerView.Recycler;
import com.android.internal.widget.RecyclerView.ViewHolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

final class GapWorker implements Runnable {
    static final ThreadLocal<GapWorker> sGapWorker = new ThreadLocal();
    static Comparator<Task> sTaskComparator = new Comparator<Task>() {
        public int compare(Task lhs, Task rhs) {
            int i;
            int i2 = -1;
            int i3 = 1;
            if (lhs.view == null) {
                i = 1;
            } else {
                i = 0;
            }
            if (i != (rhs.view == null ? 1 : 0)) {
                if (lhs.view != null) {
                    i3 = -1;
                }
                return i3;
            } else if (lhs.immediate != rhs.immediate) {
                if (!lhs.immediate) {
                    i2 = 1;
                }
                return i2;
            } else {
                int deltaViewVelocity = rhs.viewVelocity - lhs.viewVelocity;
                if (deltaViewVelocity != 0) {
                    return deltaViewVelocity;
                }
                int deltaDistanceToItem = lhs.distanceToItem - rhs.distanceToItem;
                if (deltaDistanceToItem != 0) {
                    return deltaDistanceToItem;
                }
                return 0;
            }
        }
    };
    long mFrameIntervalNs;
    long mPostTimeNs;
    ArrayList<RecyclerView> mRecyclerViews = new ArrayList();
    private ArrayList<Task> mTasks = new ArrayList();

    static class LayoutPrefetchRegistryImpl implements LayoutPrefetchRegistry {
        int mCount;
        int[] mPrefetchArray;
        int mPrefetchDx;
        int mPrefetchDy;

        LayoutPrefetchRegistryImpl() {
        }

        void setPrefetchVector(int dx, int dy) {
            this.mPrefetchDx = dx;
            this.mPrefetchDy = dy;
        }

        void collectPrefetchPositionsFromView(RecyclerView view, boolean nested) {
            this.mCount = 0;
            if (this.mPrefetchArray != null) {
                Arrays.fill(this.mPrefetchArray, -1);
            }
            LayoutManager layout = view.mLayout;
            if (view.mAdapter != null && layout != null && layout.isItemPrefetchEnabled()) {
                if (nested) {
                    if (!view.mAdapterHelper.hasPendingUpdates()) {
                        layout.collectInitialPrefetchPositions(view.mAdapter.getItemCount(), this);
                    }
                } else if (!view.hasPendingAdapterUpdates()) {
                    layout.collectAdjacentPrefetchPositions(this.mPrefetchDx, this.mPrefetchDy, view.mState, this);
                }
                if (this.mCount > layout.mPrefetchMaxCountObserved) {
                    layout.mPrefetchMaxCountObserved = this.mCount;
                    layout.mPrefetchMaxObservedInInitialPrefetch = nested;
                    view.mRecycler.updateViewCacheSize();
                }
            }
        }

        public void addPosition(int layoutPosition, int pixelDistance) {
            if (pixelDistance < 0) {
                throw new IllegalArgumentException("Pixel distance must be non-negative");
            }
            int storagePosition = this.mCount * 2;
            if (this.mPrefetchArray == null) {
                this.mPrefetchArray = new int[4];
                Arrays.fill(this.mPrefetchArray, -1);
            } else if (storagePosition >= this.mPrefetchArray.length) {
                int[] oldArray = this.mPrefetchArray;
                this.mPrefetchArray = new int[(storagePosition * 2)];
                System.arraycopy(oldArray, 0, this.mPrefetchArray, 0, oldArray.length);
            }
            this.mPrefetchArray[storagePosition] = layoutPosition;
            this.mPrefetchArray[storagePosition + 1] = pixelDistance;
            this.mCount++;
        }

        boolean lastPrefetchIncludedPosition(int position) {
            if (this.mPrefetchArray != null) {
                int count = this.mCount * 2;
                for (int i = 0; i < count; i += 2) {
                    if (this.mPrefetchArray[i] == position) {
                        return true;
                    }
                }
            }
            return false;
        }

        void clearPrefetchPositions() {
            if (this.mPrefetchArray != null) {
                Arrays.fill(this.mPrefetchArray, -1);
            }
        }
    }

    static class Task {
        public int distanceToItem;
        public boolean immediate;
        public int position;
        public RecyclerView view;
        public int viewVelocity;

        Task() {
        }

        public void clear() {
            this.immediate = false;
            this.viewVelocity = 0;
            this.distanceToItem = 0;
            this.view = null;
            this.position = 0;
        }
    }

    GapWorker() {
    }

    public void add(RecyclerView recyclerView) {
        this.mRecyclerViews.add(recyclerView);
    }

    public void remove(RecyclerView recyclerView) {
        boolean removeSuccess = this.mRecyclerViews.remove(recyclerView);
    }

    void postFromTraversal(RecyclerView recyclerView, int prefetchDx, int prefetchDy) {
        if (recyclerView.isAttachedToWindow() && this.mPostTimeNs == 0) {
            this.mPostTimeNs = recyclerView.getNanoTime();
            recyclerView.post(this);
        }
        recyclerView.mPrefetchRegistry.setPrefetchVector(prefetchDx, prefetchDy);
    }

    private void buildTaskList() {
        int i;
        RecyclerView view;
        int viewCount = this.mRecyclerViews.size();
        int totalTaskCount = 0;
        for (i = 0; i < viewCount; i++) {
            view = (RecyclerView) this.mRecyclerViews.get(i);
            view.mPrefetchRegistry.collectPrefetchPositionsFromView(view, false);
            totalTaskCount += view.mPrefetchRegistry.mCount;
        }
        this.mTasks.ensureCapacity(totalTaskCount);
        int totalTaskIndex = 0;
        for (i = 0; i < viewCount; i++) {
            view = (RecyclerView) this.mRecyclerViews.get(i);
            LayoutPrefetchRegistryImpl prefetchRegistry = view.mPrefetchRegistry;
            int viewVelocity = Math.abs(prefetchRegistry.mPrefetchDx) + Math.abs(prefetchRegistry.mPrefetchDy);
            for (int j = 0; j < prefetchRegistry.mCount * 2; j += 2) {
                Task task;
                boolean z;
                if (totalTaskIndex >= this.mTasks.size()) {
                    task = new Task();
                    this.mTasks.add(task);
                } else {
                    task = (Task) this.mTasks.get(totalTaskIndex);
                }
                int distanceToItem = prefetchRegistry.mPrefetchArray[j + 1];
                if (distanceToItem <= viewVelocity) {
                    z = true;
                } else {
                    z = false;
                }
                task.immediate = z;
                task.viewVelocity = viewVelocity;
                task.distanceToItem = distanceToItem;
                task.view = view;
                task.position = prefetchRegistry.mPrefetchArray[j];
                totalTaskIndex++;
            }
        }
        Collections.sort(this.mTasks, sTaskComparator);
    }

    static boolean isPrefetchPositionAttached(RecyclerView view, int position) {
        int childCount = view.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewHolder holder = RecyclerView.getChildViewHolderInt(view.mChildHelper.getUnfilteredChildAt(i));
            if (holder.mPosition == position && (holder.isInvalid() ^ 1) != 0) {
                return true;
            }
        }
        return false;
    }

    private ViewHolder prefetchPositionWithDeadline(RecyclerView view, int position, long deadlineNs) {
        if (isPrefetchPositionAttached(view, position)) {
            return null;
        }
        Recycler recycler = view.mRecycler;
        ViewHolder holder = recycler.tryGetViewHolderForPositionByDeadline(position, false, deadlineNs);
        if (holder != null) {
            if (holder.isBound()) {
                recycler.recycleView(holder.itemView);
            } else {
                recycler.addViewHolderToRecycledViewPool(holder, false);
            }
        }
        return holder;
    }

    private void prefetchInnerRecyclerViewWithDeadline(RecyclerView innerView, long deadlineNs) {
        if (innerView != null) {
            if (innerView.mDataSetHasChangedAfterLayout && innerView.mChildHelper.getUnfilteredChildCount() != 0) {
                innerView.removeAndRecycleViews();
            }
            LayoutPrefetchRegistryImpl innerPrefetchRegistry = innerView.mPrefetchRegistry;
            innerPrefetchRegistry.collectPrefetchPositionsFromView(innerView, true);
            if (innerPrefetchRegistry.mCount != 0) {
                try {
                    Trace.beginSection("RV Nested Prefetch");
                    innerView.mState.prepareForNestedPrefetch(innerView.mAdapter);
                    for (int i = 0; i < innerPrefetchRegistry.mCount * 2; i += 2) {
                        prefetchPositionWithDeadline(innerView, innerPrefetchRegistry.mPrefetchArray[i], deadlineNs);
                    }
                } finally {
                    Trace.endSection();
                }
            }
        }
    }

    private void flushTaskWithDeadline(Task task, long deadlineNs) {
        ViewHolder holder = prefetchPositionWithDeadline(task.view, task.position, task.immediate ? Long.MAX_VALUE : deadlineNs);
        if (holder != null && holder.mNestedRecyclerView != null) {
            prefetchInnerRecyclerViewWithDeadline((RecyclerView) holder.mNestedRecyclerView.get(), deadlineNs);
        }
    }

    private void flushTasksWithDeadline(long deadlineNs) {
        int i = 0;
        while (i < this.mTasks.size()) {
            Task task = (Task) this.mTasks.get(i);
            if (task.view != null) {
                flushTaskWithDeadline(task, deadlineNs);
                task.clear();
                i++;
            } else {
                return;
            }
        }
    }

    void prefetch(long deadlineNs) {
        buildTaskList();
        flushTasksWithDeadline(deadlineNs);
    }

    public void run() {
        try {
            Trace.beginSection("RV Prefetch");
            if (!this.mRecyclerViews.isEmpty()) {
                long lastFrameVsyncNs = TimeUnit.MILLISECONDS.toNanos(((RecyclerView) this.mRecyclerViews.get(0)).getDrawingTime());
                if (lastFrameVsyncNs == 0) {
                    this.mPostTimeNs = 0;
                    Trace.endSection();
                    return;
                }
                prefetch(lastFrameVsyncNs + this.mFrameIntervalNs);
                this.mPostTimeNs = 0;
                Trace.endSection();
            }
        } finally {
            this.mPostTimeNs = 0;
            Trace.endSection();
        }
    }
}
