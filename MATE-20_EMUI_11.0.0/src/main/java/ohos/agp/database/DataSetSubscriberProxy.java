package ohos.agp.database;

class DataSetSubscriberProxy extends DataSetSubscriber {
    long mNativeDataSetObserver = 0;

    private native void nativeOnChangedNotify(long j);

    private native void nativeOnInvalidatedNotify(long j);

    public DataSetSubscriberProxy(long j) {
        this.mNativeDataSetObserver = j;
    }

    @Override // ohos.agp.database.DataSetSubscriber
    public void onChanged() {
        nativeOnChangedNotify(this.mNativeDataSetObserver);
    }

    @Override // ohos.agp.database.DataSetSubscriber
    public void onInvalidated() {
        nativeOnInvalidatedNotify(this.mNativeDataSetObserver);
    }
}
