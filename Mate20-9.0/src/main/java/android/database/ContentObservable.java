package android.database;

import android.net.Uri;
import java.util.Iterator;

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
            Iterator it = this.mObservers.iterator();
            while (it.hasNext()) {
                ContentObserver observer = (ContentObserver) it.next();
                if (!selfChange || observer.deliverSelfNotifications()) {
                    observer.dispatchChange(selfChange, uri);
                }
            }
        }
    }

    @Deprecated
    public void notifyChange(boolean selfChange) {
        synchronized (this.mObservers) {
            Iterator it = this.mObservers.iterator();
            while (it.hasNext()) {
                ((ContentObserver) it.next()).onChange(selfChange, null);
            }
        }
    }
}
