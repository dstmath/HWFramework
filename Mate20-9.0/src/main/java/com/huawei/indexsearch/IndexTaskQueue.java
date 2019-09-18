package com.huawei.indexsearch;

import android.util.Log;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IndexTaskQueue implements IndexQueue<SearchTaskItem> {
    private static final String TAG = "IndexTaskQueue";
    private static IndexTaskQueue instance = null;
    private final BlockingQueue<SearchTaskItem> indexQueues = new LinkedBlockingQueue();

    private IndexTaskQueue() {
    }

    public static IndexTaskQueue getInstance() {
        IndexTaskQueue indexTaskQueue;
        synchronized (IndexTaskQueue.class) {
            if (instance == null) {
                instance = new IndexTaskQueue();
            }
            indexTaskQueue = instance;
        }
        return indexTaskQueue;
    }

    public SearchTaskItem take() {
        try {
            return this.indexQueues.take();
        } catch (InterruptedException e) {
            Log.e(TAG, " SearchTaskItem take operate InterruptedException.");
            return null;
        }
    }

    public boolean add(SearchTaskItem searchTaskItem) {
        return this.indexQueues.add(searchTaskItem);
    }

    public int getQueueSize() {
        return this.indexQueues.size();
    }

    public void clear() {
        this.indexQueues.clear();
    }
}
