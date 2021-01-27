package android.database;

public class DataSetObservable extends Observable<DataSetObserver> {
    public void notifyChanged() {
        synchronized (this.mObservers) {
            for (int i = this.mObservers.size() - 1; i >= 0; i--) {
                ((DataSetObserver) this.mObservers.get(i)).onChanged();
            }
        }
    }

    public void notifyInvalidated() {
        synchronized (this.mObservers) {
            for (int i = this.mObservers.size() - 1; i >= 0; i--) {
                ((DataSetObserver) this.mObservers.get(i)).onInvalidated();
            }
        }
    }
}
