package com.android.internal.widget;

import android.util.ArrayMap;
import android.util.LongSparseArray;
import android.util.Pools;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.RecyclerView;

class ViewInfoStore {
    private static final boolean DEBUG = false;
    @VisibleForTesting
    final ArrayMap<RecyclerView.ViewHolder, InfoRecord> mLayoutHolderMap = new ArrayMap<>();
    @VisibleForTesting
    final LongSparseArray<RecyclerView.ViewHolder> mOldChangedHolders = new LongSparseArray<>();

    static class InfoRecord {
        static final int FLAG_APPEAR = 2;
        static final int FLAG_APPEAR_AND_DISAPPEAR = 3;
        static final int FLAG_APPEAR_PRE_AND_POST = 14;
        static final int FLAG_DISAPPEARED = 1;
        static final int FLAG_POST = 8;
        static final int FLAG_PRE = 4;
        static final int FLAG_PRE_AND_POST = 12;
        static Pools.Pool<InfoRecord> sPool = new Pools.SimplePool(20);
        int flags;
        RecyclerView.ItemAnimator.ItemHolderInfo postInfo;
        RecyclerView.ItemAnimator.ItemHolderInfo preInfo;

        private InfoRecord() {
        }

        static InfoRecord obtain() {
            InfoRecord record = (InfoRecord) sPool.acquire();
            return record == null ? new InfoRecord() : record;
        }

        static void recycle(InfoRecord record) {
            record.flags = 0;
            record.preInfo = null;
            record.postInfo = null;
            sPool.release(record);
        }

        static void drainCache() {
            do {
            } while (sPool.acquire() != null);
        }
    }

    interface ProcessCallback {
        void processAppeared(RecyclerView.ViewHolder viewHolder, RecyclerView.ItemAnimator.ItemHolderInfo itemHolderInfo, RecyclerView.ItemAnimator.ItemHolderInfo itemHolderInfo2);

        void processDisappeared(RecyclerView.ViewHolder viewHolder, RecyclerView.ItemAnimator.ItemHolderInfo itemHolderInfo, RecyclerView.ItemAnimator.ItemHolderInfo itemHolderInfo2);

        void processPersistent(RecyclerView.ViewHolder viewHolder, RecyclerView.ItemAnimator.ItemHolderInfo itemHolderInfo, RecyclerView.ItemAnimator.ItemHolderInfo itemHolderInfo2);

        void unused(RecyclerView.ViewHolder viewHolder);
    }

    ViewInfoStore() {
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        this.mLayoutHolderMap.clear();
        this.mOldChangedHolders.clear();
    }

    /* access modifiers changed from: package-private */
    public void addToPreLayout(RecyclerView.ViewHolder holder, RecyclerView.ItemAnimator.ItemHolderInfo info) {
        InfoRecord record = this.mLayoutHolderMap.get(holder);
        if (record == null) {
            record = InfoRecord.obtain();
            this.mLayoutHolderMap.put(holder, record);
        }
        record.preInfo = info;
        record.flags |= 4;
    }

    /* access modifiers changed from: package-private */
    public boolean isDisappearing(RecyclerView.ViewHolder holder) {
        InfoRecord record = this.mLayoutHolderMap.get(holder);
        return (record == null || (record.flags & 1) == 0) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public RecyclerView.ItemAnimator.ItemHolderInfo popFromPreLayout(RecyclerView.ViewHolder vh) {
        return popFromLayoutStep(vh, 4);
    }

    /* access modifiers changed from: package-private */
    public RecyclerView.ItemAnimator.ItemHolderInfo popFromPostLayout(RecyclerView.ViewHolder vh) {
        return popFromLayoutStep(vh, 8);
    }

    private RecyclerView.ItemAnimator.ItemHolderInfo popFromLayoutStep(RecyclerView.ViewHolder vh, int flag) {
        RecyclerView.ItemAnimator.ItemHolderInfo info;
        int index = this.mLayoutHolderMap.indexOfKey(vh);
        if (index < 0) {
            return null;
        }
        InfoRecord record = this.mLayoutHolderMap.valueAt(index);
        if (record == null || (record.flags & flag) == 0) {
            return null;
        }
        record.flags &= ~flag;
        if (flag == 4) {
            info = record.preInfo;
        } else if (flag == 8) {
            info = record.postInfo;
        } else {
            throw new IllegalArgumentException("Must provide flag PRE or POST");
        }
        if ((record.flags & 12) == 0) {
            this.mLayoutHolderMap.removeAt(index);
            InfoRecord.recycle(record);
        }
        return info;
    }

    /* access modifiers changed from: package-private */
    public void addToOldChangeHolders(long key, RecyclerView.ViewHolder holder) {
        this.mOldChangedHolders.put(key, holder);
    }

    /* access modifiers changed from: package-private */
    public void addToAppearedInPreLayoutHolders(RecyclerView.ViewHolder holder, RecyclerView.ItemAnimator.ItemHolderInfo info) {
        InfoRecord record = this.mLayoutHolderMap.get(holder);
        if (record == null) {
            record = InfoRecord.obtain();
            this.mLayoutHolderMap.put(holder, record);
        }
        record.flags |= 2;
        record.preInfo = info;
    }

    /* access modifiers changed from: package-private */
    public boolean isInPreLayout(RecyclerView.ViewHolder viewHolder) {
        InfoRecord record = this.mLayoutHolderMap.get(viewHolder);
        return (record == null || (record.flags & 4) == 0) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public RecyclerView.ViewHolder getFromOldChangeHolders(long key) {
        return this.mOldChangedHolders.get(key);
    }

    /* access modifiers changed from: package-private */
    public void addToPostLayout(RecyclerView.ViewHolder holder, RecyclerView.ItemAnimator.ItemHolderInfo info) {
        InfoRecord record = this.mLayoutHolderMap.get(holder);
        if (record == null) {
            record = InfoRecord.obtain();
            this.mLayoutHolderMap.put(holder, record);
        }
        record.postInfo = info;
        record.flags |= 8;
    }

    /* access modifiers changed from: package-private */
    public void addToDisappearedInLayout(RecyclerView.ViewHolder holder) {
        InfoRecord record = this.mLayoutHolderMap.get(holder);
        if (record == null) {
            record = InfoRecord.obtain();
            this.mLayoutHolderMap.put(holder, record);
        }
        record.flags |= 1;
    }

    /* access modifiers changed from: package-private */
    public void removeFromDisappearedInLayout(RecyclerView.ViewHolder holder) {
        InfoRecord record = this.mLayoutHolderMap.get(holder);
        if (record != null) {
            record.flags &= -2;
        }
    }

    /* access modifiers changed from: package-private */
    public void process(ProcessCallback callback) {
        for (int index = this.mLayoutHolderMap.size() - 1; index >= 0; index--) {
            RecyclerView.ViewHolder viewHolder = this.mLayoutHolderMap.keyAt(index);
            InfoRecord record = this.mLayoutHolderMap.removeAt(index);
            if ((record.flags & 3) == 3) {
                callback.unused(viewHolder);
            } else if ((record.flags & 1) != 0) {
                if (record.preInfo == null) {
                    callback.unused(viewHolder);
                } else {
                    callback.processDisappeared(viewHolder, record.preInfo, record.postInfo);
                }
            } else if ((record.flags & 14) == 14) {
                callback.processAppeared(viewHolder, record.preInfo, record.postInfo);
            } else if ((record.flags & 12) == 12) {
                callback.processPersistent(viewHolder, record.preInfo, record.postInfo);
            } else if ((record.flags & 4) != 0) {
                callback.processDisappeared(viewHolder, record.preInfo, null);
            } else if ((record.flags & 8) != 0) {
                callback.processAppeared(viewHolder, record.preInfo, record.postInfo);
            } else {
                int i = record.flags;
            }
            InfoRecord.recycle(record);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeViewHolder(RecyclerView.ViewHolder holder) {
        int i = this.mOldChangedHolders.size() - 1;
        while (true) {
            if (i < 0) {
                break;
            } else if (holder == this.mOldChangedHolders.valueAt(i)) {
                this.mOldChangedHolders.removeAt(i);
                break;
            } else {
                i--;
            }
        }
        InfoRecord info = this.mLayoutHolderMap.remove(holder);
        if (info != null) {
            InfoRecord.recycle(info);
        }
    }

    /* access modifiers changed from: package-private */
    public void onDetach() {
        InfoRecord.drainCache();
    }

    public void onViewDetached(RecyclerView.ViewHolder viewHolder) {
        removeFromDisappearedInLayout(viewHolder);
    }
}
