package com.android.internal.util;

import android.os.Bundle;
import android.os.IProgressListener;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.MathUtils;
import com.android.internal.annotations.GuardedBy;

public class ProgressReporter {
    private static final int STATE_FINISHED = 2;
    private static final int STATE_INIT = 0;
    private static final int STATE_STARTED = 1;
    @GuardedBy("this")
    private Bundle mExtras = new Bundle();
    private final int mId;
    @GuardedBy("this")
    private final RemoteCallbackList<IProgressListener> mListeners = new RemoteCallbackList();
    @GuardedBy("this")
    private int mProgress = 0;
    @GuardedBy("this")
    private int[] mSegmentRange = new int[]{0, 100};
    @GuardedBy("this")
    private int mState = 0;

    public ProgressReporter(int id) {
        this.mId = id;
    }

    public void addListener(IProgressListener listener) {
        if (listener != null) {
            synchronized (this) {
                this.mListeners.register(listener);
                switch (this.mState) {
                    case 1:
                        try {
                            listener.onStarted(this.mId, null);
                            listener.onProgress(this.mId, this.mProgress, this.mExtras);
                            break;
                        } catch (RemoteException e) {
                            break;
                        }
                    case 2:
                        try {
                            listener.onFinished(this.mId, null);
                            break;
                        } catch (RemoteException e2) {
                            break;
                        }
                }
            }
        }
    }

    public void setProgress(int progress) {
        setProgress(progress, 100, null);
    }

    public void setProgress(int progress, CharSequence title) {
        setProgress(progress, 100, title);
    }

    public void setProgress(int n, int m) {
        setProgress(n, m, null);
    }

    public void setProgress(int n, int m, CharSequence title) {
        synchronized (this) {
            if (this.mState != 1) {
                throw new IllegalStateException("Must be started to change progress");
            }
            this.mProgress = this.mSegmentRange[0] + MathUtils.constrain((this.mSegmentRange[1] * n) / m, 0, this.mSegmentRange[1]);
            if (title != null) {
                this.mExtras.putCharSequence("android.intent.extra.TITLE", title);
            }
            notifyProgress(this.mId, this.mProgress, this.mExtras);
        }
    }

    public int[] startSegment(int size) {
        int[] lastRange;
        synchronized (this) {
            lastRange = this.mSegmentRange;
            this.mSegmentRange = new int[]{this.mProgress, (this.mSegmentRange[1] * size) / 100};
        }
        return lastRange;
    }

    public void endSegment(int[] lastRange) {
        synchronized (this) {
            this.mProgress = this.mSegmentRange[0] + this.mSegmentRange[1];
            this.mSegmentRange = lastRange;
        }
    }

    int getProgress() {
        return this.mProgress;
    }

    int[] getSegmentRange() {
        return this.mSegmentRange;
    }

    public void start() {
        synchronized (this) {
            this.mState = 1;
            notifyStarted(this.mId, null);
            notifyProgress(this.mId, this.mProgress, this.mExtras);
        }
    }

    public void finish() {
        synchronized (this) {
            this.mState = 2;
            notifyFinished(this.mId, null);
            this.mListeners.kill();
        }
    }

    private void notifyStarted(int id, Bundle extras) {
        for (int i = this.mListeners.beginBroadcast() - 1; i >= 0; i--) {
            try {
                ((IProgressListener) this.mListeners.getBroadcastItem(i)).onStarted(id, extras);
            } catch (RemoteException e) {
            }
        }
        this.mListeners.finishBroadcast();
    }

    private void notifyProgress(int id, int progress, Bundle extras) {
        for (int i = this.mListeners.beginBroadcast() - 1; i >= 0; i--) {
            try {
                ((IProgressListener) this.mListeners.getBroadcastItem(i)).onProgress(id, progress, extras);
            } catch (RemoteException e) {
            }
        }
        this.mListeners.finishBroadcast();
    }

    private void notifyFinished(int id, Bundle extras) {
        for (int i = this.mListeners.beginBroadcast() - 1; i >= 0; i--) {
            try {
                ((IProgressListener) this.mListeners.getBroadcastItem(i)).onFinished(id, extras);
            } catch (RemoteException e) {
            }
        }
        this.mListeners.finishBroadcast();
    }
}
