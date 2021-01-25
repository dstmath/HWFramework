package com.huawei.indexsearch;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IndexTaskQueue implements IndexQueue<SearchTaskItem> {
    private static final Object LOCK = new Object();
    private static IndexTaskQueue instance = null;
    private final BlockingQueue<SearchTaskItem> indexQueues = new LinkedBlockingQueue();

    private IndexTaskQueue() {
    }

    public static IndexTaskQueue getInstance() {
        IndexTaskQueue indexTaskQueue;
        synchronized (LOCK) {
            if (instance == null) {
                instance = new IndexTaskQueue();
            }
            indexTaskQueue = instance;
        }
        return indexTaskQueue;
    }

    @Override // com.huawei.indexsearch.IndexQueue
    public SearchTaskItem take() {
        return this.indexQueues.poll();
    }

    public boolean add(SearchTaskItem searchTaskItem) {
        return this.indexQueues.add(searchTaskItem);
    }

    @Override // com.huawei.indexsearch.IndexQueue
    public int getQueueSize() {
        return this.indexQueues.size();
    }

    public void clear() {
        this.indexQueues.clear();
    }
}
