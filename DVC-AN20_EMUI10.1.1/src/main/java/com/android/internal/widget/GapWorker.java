package com.android.internal.widget;

import android.os.Trace;
import com.android.internal.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

/* access modifiers changed from: package-private */
public final class GapWorker implements Runnable {
    static final ThreadLocal<GapWorker> sGapWorker = new ThreadLocal<>();
    static Comparator<Task> sTaskComparator = new Comparator<Task>() {
        /* class com.android.internal.widget.GapWorker.AnonymousClass1 */

        public int compare(Task lhs, Task rhs) {
            if ((lhs.view == null) != (rhs.view == null)) {
                return lhs.view == null ? 1 : -1;
            }
            if (lhs.immediate != rhs.immediate) {
                return lhs.immediate ? -1 : 1;
            }
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
    };
    long mFrameIntervalNs;
    long mPostTimeNs;
    ArrayList<RecyclerView> mRecyclerViews = new ArrayList<>();
    private ArrayList<Task> mTasks = new ArrayList<>();

    GapWorker() {
    }

    /* access modifiers changed from: package-private */
    public static class Task {
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

    /* access modifiers changed from: package-private */
    public static class LayoutPrefetchRegistryImpl implements RecyclerView.LayoutManager.LayoutPrefetchRegistry {
        int mCount;
        int[] mPrefetchArray;
        int mPrefetchDx;
        int mPrefetchDy;

        LayoutPrefetchRegistryImpl() {
        }

        /* access modifiers changed from: package-private */
        public void setPrefetchVector(int dx, int dy) {
            this.mPrefetchDx = dx;
            this.mPrefetchDy = dy;
        }

        /* access modifiers changed from: package-private */
        public void collectPrefetchPositionsFromView(RecyclerView view, boolean nested) {
            this.mCount = 0;
            int[] iArr = this.mPrefetchArray;
            if (iArr != null) {
                Arrays.fill(iArr, -1);
            }
            RecyclerView.LayoutManager layout = view.mLayout;
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

        @Override // com.android.internal.widget.RecyclerView.LayoutManager.LayoutPrefetchRegistry
        public void addPosition(int layoutPosition, int pixelDistance) {
            if (pixelDistance >= 0) {
                int storagePosition = this.mCount * 2;
                int[] iArr = this.mPrefetchArray;
                if (iArr == null) {
                    this.mPrefetchArray = new int[4];
                    Arrays.fill(this.mPrefetchArray, -1);
                } else if (storagePosition >= iArr.length) {
                    int[] oldArray = this.mPrefetchArray;
                    this.mPrefetchArray = new int[(storagePosition * 2)];
                    System.arraycopy(oldArray, 0, this.mPrefetchArray, 0, oldArray.length);
                }
                int[] oldArray2 = this.mPrefetchArray;
                oldArray2[storagePosition] = layoutPosition;
                oldArray2[storagePosition + 1] = pixelDistance;
                this.mCount++;
                return;
            }
            throw new IllegalArgumentException("Pixel distance must be non-negative");
        }

        /* access modifiers changed from: package-private */
        public boolean lastPrefetchIncludedPosition(int position) {
            if (this.mPrefetchArray == null) {
                return false;
            }
            int count = this.mCount * 2;
            for (int i = 0; i < count; i += 2) {
                if (this.mPrefetchArray[i] == position) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public void clearPrefetchPositions() {
            int[] iArr = this.mPrefetchArray;
            if (iArr != null) {
                Arrays.fill(iArr, -1);
            }
        }
    }

    public void add(RecyclerView recyclerView) {
        this.mRecyclerViews.add(recyclerView);
    }

    public void remove(RecyclerView recyclerView) {
        this.mRecyclerViews.remove(recyclerView);
    }

    /* access modifiers changed from: package-private */
    public void postFromTraversal(RecyclerView recyclerView, int prefetchDx, int prefetchDy) {
        if (recyclerView.isAttachedToWindow() && this.mPostTimeNs == 0) {
            this.mPostTimeNs = recyclerView.getNanoTime();
            recyclerView.post(this);
        }
        recyclerView.mPrefetchRegistry.setPrefetchVector(prefetchDx, prefetchDy);
    }

    private void buildTaskList() {
        Task task;
        int viewCount = this.mRecyclerViews.size();
        int totalTaskCount = 0;
        for (int i = 0; i < viewCount; i++) {
            RecyclerView view = this.mRecyclerViews.get(i);
            view.mPrefetchRegistry.collectPrefetchPositionsFromView(view, false);
            totalTaskCount += view.mPrefetchRegistry.mCount;
        }
        this.mTasks.ensureCapacity(totalTaskCount);
        int totalTaskIndex = 0;
        for (int i2 = 0; i2 < viewCount; i2++) {
            RecyclerView view2 = this.mRecyclerViews.get(i2);
            LayoutPrefetchRegistryImpl prefetchRegistry = view2.mPrefetchRegistry;
            int viewVelocity = Math.abs(prefetchRegistry.mPrefetchDx) + Math.abs(prefetchRegistry.mPrefetchDy);
            for (int j = 0; j < prefetchRegistry.mCount * 2; j += 2) {
                if (totalTaskIndex >= this.mTasks.size()) {
                    task = new Task();
                    this.mTasks.add(task);
                } else {
                    task = this.mTasks.get(totalTaskIndex);
                }
                int distanceToItem = prefetchRegistry.mPrefetchArray[j + 1];
                task.immediate = distanceToItem <= viewVelocity;
                task.viewVelocity = viewVelocity;
                task.distanceToItem = distanceToItem;
                task.view = view2;
                task.position = prefetchRegistry.mPrefetchArray[j];
                totalTaskIndex++;
            }
        }
        Collections.sort(this.mTasks, sTaskComparator);
    }

    static boolean isPrefetchPositionAttached(RecyclerView view, int position) {
        int childCount = view.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            RecyclerView.ViewHolder holder = RecyclerView.getChildViewHolderInt(view.mChildHelper.getUnfilteredChildAt(i));
            if (holder.mPosition == position && !holder.isInvalid()) {
                return true;
            }
        }
        return false;
    }

    private RecyclerView.ViewHolder prefetchPositionWithDeadline(RecyclerView view, int position, long deadlineNs) {
        if (isPrefetchPositionAttached(view, position)) {
            return null;
        }
        RecyclerView.Recycler recycler = view.mRecycler;
        RecyclerView.ViewHolder holder = recycler.tryGetViewHolderForPositionByDeadline(position, false, deadlineNs);
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
        RecyclerView.ViewHolder holder = prefetchPositionWithDeadline(task.view, task.position, task.immediate ? Long.MAX_VALUE : deadlineNs);
        if (holder != null && holder.mNestedRecyclerView != null) {
            prefetchInnerRecyclerViewWithDeadline(holder.mNestedRecyclerView.get(), deadlineNs);
        }
    }

    private void flushTasksWithDeadline(long deadlineNs) {
        for (int i = 0; i < this.mTasks.size(); i++) {
            Task task = this.mTasks.get(i);
            if (task.view != null) {
                flushTaskWithDeadline(task, deadlineNs);
                task.clear();
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void prefetch(long deadlineNs) {
        buildTaskList();
        flushTasksWithDeadline(deadlineNs);
    }

    public void run() {
        try {
            Trace.beginSection("RV Prefetch");
            if (!this.mRecyclerViews.isEmpty()) {
                long lastFrameVsyncNs = TimeUnit.MILLISECONDS.toNanos(this.mRecyclerViews.get(0).getDrawingTime());
                if (lastFrameVsyncNs == 0) {
                    this.mPostTimeNs = 0;
                    Trace.endSection();
                    return;
                }
                prefetch(this.mFrameIntervalNs + lastFrameVsyncNs);
                this.mPostTimeNs = 0;
                Trace.endSection();
            }
        } finally {
            this.mPostTimeNs = 0;
            Trace.endSection();
        }
    }
}
