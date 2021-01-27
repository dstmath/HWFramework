package ohos.agp.database;

class DataSetSubscriberProxy extends DataSetSubscriber {
    final long mNativeDataSetObserver;

    private native void nativeOnChangedNotify(long j);

    private native void nativeOnInvalidatedNotify(long j);

    private native void nativeOnItemChangedNotify(long j, int i);

    private native void nativeOnItemInsertedNotify(long j, int i);

    private native void nativeOnItemRangeChangedNotify(long j, int i, int i2);

    private native void nativeOnItemRangeInsertedNotify(long j, int i, int i2);

    private native void nativeOnItemRangeRemovedNotify(long j, int i, int i2);

    private native void nativeOnItemRemovedNotify(long j, int i);

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

    @Override // ohos.agp.database.DataSetSubscriber
    public void onItemChanged(int i) {
        nativeOnItemChangedNotify(this.mNativeDataSetObserver, i);
    }

    @Override // ohos.agp.database.DataSetSubscriber
    public void onItemInserted(int i) {
        nativeOnItemInsertedNotify(this.mNativeDataSetObserver, i);
    }

    @Override // ohos.agp.database.DataSetSubscriber
    public void onItemRemoved(int i) {
        nativeOnItemRemovedNotify(this.mNativeDataSetObserver, i);
    }

    @Override // ohos.agp.database.DataSetSubscriber
    public void onItemRangeChanged(int i, int i2) {
        nativeOnItemRangeChangedNotify(this.mNativeDataSetObserver, i, i2);
    }

    @Override // ohos.agp.database.DataSetSubscriber
    public void onItemRangeInserted(int i, int i2) {
        nativeOnItemRangeInsertedNotify(this.mNativeDataSetObserver, i, i2);
    }

    @Override // ohos.agp.database.DataSetSubscriber
    public void onItemRangeRemoved(int i, int i2) {
        nativeOnItemRangeRemovedNotify(this.mNativeDataSetObserver, i, i2);
    }
}
