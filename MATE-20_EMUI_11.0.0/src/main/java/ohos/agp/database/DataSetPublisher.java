package ohos.agp.database;

public class DataSetPublisher extends Publisher<DataSetSubscriber> {
    public void notifyChanged() {
        for (DataSetSubscriber dataSetSubscriber : this.mSubscribers) {
            dataSetSubscriber.onChanged();
        }
    }

    public void notifyInvalidated() {
        for (DataSetSubscriber dataSetSubscriber : this.mSubscribers) {
            dataSetSubscriber.onInvalidated();
        }
    }
}
