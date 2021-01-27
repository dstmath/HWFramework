package com.android.server.swing.notification;

import android.content.Context;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class HwSwingEventAvailabler {
    private static final int DEDAULT_LIST_SIZE = 2;
    protected final String TAG = getClass().getSimpleName();
    protected Context mContext;
    protected boolean mIsAvailable;
    protected ArrayList<IAvailableListener> mListeners;

    public interface IAvailableListener {
        void onAvailableChanged(boolean z);
    }

    /* access modifiers changed from: protected */
    public abstract void init();

    /* access modifiers changed from: protected */
    public abstract void release();

    public HwSwingEventAvailabler(Context context) {
        this.mContext = context;
    }

    public boolean isAvailable() {
        return this.mIsAvailable;
    }

    /* access modifiers changed from: protected */
    public void setAvailable(boolean isAvailable) {
        if (isAvailable != this.mIsAvailable) {
            this.mIsAvailable = isAvailable;
            notifyAvailableChanged(isAvailable);
        }
    }

    private void notifyAvailableChanged(boolean isAvailable) {
        ArrayList<IAvailableListener> clonedListeners = null;
        synchronized (this) {
            if (this.mListeners != null) {
                clonedListeners = new ArrayList<>(this.mListeners);
            }
        }
        if (clonedListeners != null) {
            Iterator<IAvailableListener> it = clonedListeners.iterator();
            while (it.hasNext()) {
                IAvailableListener listener = it.next();
                if (listener != null) {
                    listener.onAvailableChanged(isAvailable);
                }
            }
        }
    }

    public void addListener(IAvailableListener listener) {
        if (listener != null) {
            boolean needInit = false;
            synchronized (this) {
                if (this.mListeners == null) {
                    this.mListeners = new ArrayList<>(2);
                    needInit = true;
                }
                this.mListeners.add(listener);
            }
            if (needInit) {
                init();
            }
        }
    }

    public void removeListener(IAvailableListener listener) {
        if (listener != null) {
            boolean needRelease = false;
            synchronized (this) {
                if (this.mListeners != null) {
                    this.mListeners.remove(listener);
                    if (this.mListeners.size() <= 0) {
                        this.mListeners = null;
                        needRelease = true;
                    }
                } else {
                    return;
                }
            }
            if (needRelease) {
                release();
            }
        }
    }
}
