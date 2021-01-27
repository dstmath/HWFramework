package ohos.agp.database;

public class DataSetPublisher extends Publisher<DataSetSubscriber> {
    public void notifyChanged() {
        for (DataSetSubscriber dataSetSubscriber : this.mSubscribers) {
            dataSetSubscriber.onChanged();
        }
    }

    public void informInvalidated() {
        for (DataSetSubscriber dataSetSubscriber : this.mSubscribers) {
            dataSetSubscriber.onInvalidated();
        }
    }

    public void notifyItemChanged(int i) {
        for (DataSetSubscriber dataSetSubscriber : this.mSubscribers) {
            dataSetSubscriber.onItemChanged(i);
        }
    }

    public void notifyItemInserted(int i) {
        for (DataSetSubscriber dataSetSubscriber : this.mSubscribers) {
            dataSetSubscriber.onItemInserted(i);
        }
    }

    public void notifyItemRemoved(int i) {
        for (DataSetSubscriber dataSetSubscriber : this.mSubscribers) {
            dataSetSubscriber.onItemRemoved(i);
        }
    }

    public void notifyItemRangeChanged(int i, int i2) {
        for (DataSetSubscriber dataSetSubscriber : this.mSubscribers) {
            dataSetSubscriber.onItemRangeChanged(i, i2);
        }
    }

    public void notifyItemRangeInserted(int i, int i2) {
        for (DataSetSubscriber dataSetSubscriber : this.mSubscribers) {
            dataSetSubscriber.onItemRangeInserted(i, i2);
        }
    }

    public void notifyItemRangeRemoved(int i, int i2) {
        for (DataSetSubscriber dataSetSubscriber : this.mSubscribers) {
            dataSetSubscriber.onItemRangeRemoved(i, i2);
        }
    }
}
