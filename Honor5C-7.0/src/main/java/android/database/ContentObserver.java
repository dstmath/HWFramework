package android.database;

import android.database.IContentObserver.Stub;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;

public abstract class ContentObserver {
    Handler mHandler;
    private final Object mLock;
    private Transport mTransport;

    private final class NotificationRunnable implements Runnable {
        private final boolean mSelfChange;
        private final Uri mUri;
        private final int mUserId;

        public NotificationRunnable(boolean selfChange, Uri uri, int userId) {
            this.mSelfChange = selfChange;
            this.mUri = uri;
            this.mUserId = userId;
        }

        public void run() {
            ContentObserver.this.onChange(this.mSelfChange, this.mUri, this.mUserId);
        }
    }

    private static final class Transport extends Stub {
        private ContentObserver mContentObserver;

        public Transport(ContentObserver contentObserver) {
            this.mContentObserver = contentObserver;
        }

        public void onChange(boolean selfChange, Uri uri, int userId) {
            ContentObserver contentObserver = this.mContentObserver;
            if (contentObserver != null) {
                contentObserver.dispatchChange(selfChange, uri, userId);
            }
        }

        public void releaseContentObserver() {
            this.mContentObserver = null;
        }
    }

    public ContentObserver(Handler handler) {
        this.mLock = new Object();
        this.mHandler = handler;
    }

    public IContentObserver getContentObserver() {
        IContentObserver iContentObserver;
        synchronized (this.mLock) {
            if (this.mTransport == null) {
                this.mTransport = new Transport(this);
            }
            iContentObserver = this.mTransport;
        }
        return iContentObserver;
    }

    public IContentObserver releaseContentObserver() {
        Transport oldTransport;
        synchronized (this.mLock) {
            oldTransport = this.mTransport;
            if (oldTransport != null) {
                oldTransport.releaseContentObserver();
                this.mTransport = null;
            }
        }
        return oldTransport;
    }

    public boolean deliverSelfNotifications() {
        return false;
    }

    public void onChange(boolean selfChange) {
    }

    public void onChange(boolean selfChange, Uri uri) {
        onChange(selfChange);
    }

    public void onChange(boolean selfChange, Uri uri, int userId) {
        onChange(selfChange, uri);
    }

    @Deprecated
    public final void dispatchChange(boolean selfChange) {
        dispatchChange(selfChange, null);
    }

    public final void dispatchChange(boolean selfChange, Uri uri) {
        dispatchChange(selfChange, uri, UserHandle.getCallingUserId());
    }

    private void dispatchChange(boolean selfChange, Uri uri, int userId) {
        if (this.mHandler == null) {
            onChange(selfChange, uri, userId);
        } else {
            this.mHandler.post(new NotificationRunnable(selfChange, uri, userId));
        }
    }
}
