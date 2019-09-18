package com.android.systemui.shared.recents.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.android.systemui.shared.system.ActivityManagerWrapper;

class BackgroundTaskLoader implements Runnable {
    static boolean DEBUG = false;
    static String TAG = "BackgroundTaskLoader";
    private boolean mCancelled;
    private Context mContext;
    private final IconLoader mIconLoader;
    private final TaskResourceLoadQueue mLoadQueue;
    private final HandlerThread mLoadThread;
    private final Handler mLoadThreadHandler;
    private final Handler mMainThreadHandler = new Handler();
    private final OnIdleChangedListener mOnIdleChangedListener;
    private boolean mStarted;
    private boolean mWaitingOnLoadQueue;

    interface OnIdleChangedListener {
        void onIdleChanged(boolean z);
    }

    public BackgroundTaskLoader(TaskResourceLoadQueue loadQueue, IconLoader iconLoader, OnIdleChangedListener onIdleChangedListener) {
        this.mLoadQueue = loadQueue;
        this.mIconLoader = iconLoader;
        this.mOnIdleChangedListener = onIdleChangedListener;
        this.mLoadThread = new HandlerThread("Recents-TaskResourceLoader", 10);
        this.mLoadThread.start();
        this.mLoadThreadHandler = new Handler(this.mLoadThread.getLooper());
    }

    /* access modifiers changed from: package-private */
    public void start(Context context) {
        this.mContext = context;
        this.mCancelled = false;
        if (!this.mStarted) {
            this.mStarted = true;
            this.mLoadThreadHandler.post(this);
            return;
        }
        synchronized (this.mLoadThread) {
            this.mLoadThread.notifyAll();
        }
    }

    /* access modifiers changed from: package-private */
    public void stop() {
        this.mCancelled = true;
        if (this.mWaitingOnLoadQueue) {
            this.mContext = null;
        }
    }

    public void run() {
        while (true) {
            if (this.mCancelled) {
                this.mContext = null;
                synchronized (this.mLoadThread) {
                    try {
                        this.mLoadThread.wait();
                    } catch (InterruptedException e) {
                        Log.e("TaskResourceLoadQueue", "InterruptedException", e);
                    }
                }
            } else {
                processLoadQueueItem();
                if (!this.mCancelled && this.mLoadQueue.isEmpty()) {
                    synchronized (this.mLoadQueue) {
                        try {
                            this.mWaitingOnLoadQueue = true;
                            this.mMainThreadHandler.post(new Runnable() {
                                public final void run() {
                                    BackgroundTaskLoader.this.mOnIdleChangedListener.onIdleChanged(true);
                                }
                            });
                            this.mLoadQueue.wait();
                            this.mMainThreadHandler.post(new Runnable() {
                                public final void run() {
                                    BackgroundTaskLoader.this.mOnIdleChangedListener.onIdleChanged(false);
                                }
                            });
                            this.mWaitingOnLoadQueue = false;
                        } catch (InterruptedException ie) {
                            String str = TAG;
                            Log.e(str, "run,InterruptedException: " + ie.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void processLoadQueueItem() {
        Task t = this.mLoadQueue.nextTask();
        if (t != null) {
            Drawable icon = this.mIconLoader.getIcon(t);
            if (DEBUG) {
                String str = TAG;
                Log.d(str, "Loading thumbnail: " + t.key);
            }
            ThumbnailData thumbnailData = ActivityManagerWrapper.getInstance().getTaskThumbnail(t.key.id, true);
            if (!this.mCancelled) {
                this.mMainThreadHandler.post(new Runnable(thumbnailData, icon) {
                    private final /* synthetic */ ThumbnailData f$1;
                    private final /* synthetic */ Drawable f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        Task.this.notifyTaskDataLoaded(this.f$1, this.f$2);
                    }
                });
            }
        }
    }
}
