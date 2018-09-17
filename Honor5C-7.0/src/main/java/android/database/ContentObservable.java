package android.database;

import android.net.Uri;

public class ContentObservable extends Observable<ContentObserver> {
    public void registerObserver(ContentObserver observer) {
        super.registerObserver(observer);
    }

    @Deprecated
    public void dispatchChange(boolean selfChange) {
        dispatchChange(selfChange, null);
    }

    public void dispatchChange(boolean selfChange, Uri uri) {
        synchronized (this.mObservers) {
            for (ContentObserver observer : this.mObservers) {
                if (!selfChange || observer.deliverSelfNotifications()) {
                    observer.dispatchChange(selfChange, uri);
                }
            }
        }
    }

    @Deprecated
    public void notifyChange(boolean selfChange) {
        synchronized (this.mObservers) {
            for (ContentObserver observer : this.mObservers) {
                observer.onChange(selfChange, null);
            }
        }
    }
}
