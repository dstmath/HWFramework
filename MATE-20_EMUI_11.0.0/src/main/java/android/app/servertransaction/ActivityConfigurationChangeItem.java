package android.app.servertransaction;

import android.app.ClientTransactionHandler;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Trace;
import java.util.Objects;

public class ActivityConfigurationChangeItem extends ClientTransactionItem {
    public static final Parcelable.Creator<ActivityConfigurationChangeItem> CREATOR = new Parcelable.Creator<ActivityConfigurationChangeItem>() {
        /* class android.app.servertransaction.ActivityConfigurationChangeItem.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ActivityConfigurationChangeItem createFromParcel(Parcel in) {
            return new ActivityConfigurationChangeItem(in);
        }

        @Override // android.os.Parcelable.Creator
        public ActivityConfigurationChangeItem[] newArray(int size) {
            return new ActivityConfigurationChangeItem[size];
        }
    };
    private Configuration mConfiguration;

    @Override // android.app.servertransaction.BaseClientRequest
    public void preExecute(ClientTransactionHandler client, IBinder token) {
        client.updatePendingActivityConfiguration(token, this.mConfiguration);
    }

    @Override // android.app.servertransaction.BaseClientRequest
    public void execute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        Trace.traceBegin(64, "activityConfigChanged");
        client.handleActivityConfigurationChanged(token, this.mConfiguration, -1);
        Trace.traceEnd(64);
    }

    private ActivityConfigurationChangeItem() {
    }

    public static ActivityConfigurationChangeItem obtain(Configuration config) {
        ActivityConfigurationChangeItem instance = (ActivityConfigurationChangeItem) ObjectPool.obtain(ActivityConfigurationChangeItem.class);
        if (instance == null) {
            instance = new ActivityConfigurationChangeItem();
        }
        instance.mConfiguration = config;
        return instance;
    }

    @Override // android.app.servertransaction.ObjectPoolItem
    public void recycle() {
        this.mConfiguration = null;
        ObjectPool.recycle(this);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedObject(this.mConfiguration, flags);
    }

    private ActivityConfigurationChangeItem(Parcel in) {
        this.mConfiguration = (Configuration) in.readTypedObject(Configuration.CREATOR);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.mConfiguration, ((ActivityConfigurationChangeItem) o).mConfiguration);
    }

    public int hashCode() {
        return this.mConfiguration.hashCode();
    }

    public String toString() {
        return "ActivityConfigurationChange{config=" + this.mConfiguration + "}";
    }
}
