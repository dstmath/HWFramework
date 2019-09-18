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
        public ActivityConfigurationChangeItem createFromParcel(Parcel in) {
            return new ActivityConfigurationChangeItem(in);
        }

        public ActivityConfigurationChangeItem[] newArray(int size) {
            return new ActivityConfigurationChangeItem[size];
        }
    };
    private Configuration mConfiguration;

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

    public void recycle() {
        this.mConfiguration = null;
        ObjectPool.recycle(this);
    }

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
