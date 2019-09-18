package android.app.servertransaction;

import android.app.ClientTransactionHandler;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Trace;
import java.util.Objects;

public class MoveToDisplayItem extends ClientTransactionItem {
    public static final Parcelable.Creator<MoveToDisplayItem> CREATOR = new Parcelable.Creator<MoveToDisplayItem>() {
        public MoveToDisplayItem createFromParcel(Parcel in) {
            return new MoveToDisplayItem(in);
        }

        public MoveToDisplayItem[] newArray(int size) {
            return new MoveToDisplayItem[size];
        }
    };
    private Configuration mConfiguration;
    private int mTargetDisplayId;

    public void execute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        Trace.traceBegin(64, "activityMovedToDisplay");
        client.handleActivityConfigurationChanged(token, this.mConfiguration, this.mTargetDisplayId);
        Trace.traceEnd(64);
    }

    private MoveToDisplayItem() {
    }

    public static MoveToDisplayItem obtain(int targetDisplayId, Configuration configuration) {
        MoveToDisplayItem instance = (MoveToDisplayItem) ObjectPool.obtain(MoveToDisplayItem.class);
        if (instance == null) {
            instance = new MoveToDisplayItem();
        }
        instance.mTargetDisplayId = targetDisplayId;
        instance.mConfiguration = configuration;
        return instance;
    }

    public void recycle() {
        this.mTargetDisplayId = 0;
        this.mConfiguration = null;
        ObjectPool.recycle(this);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mTargetDisplayId);
        dest.writeTypedObject(this.mConfiguration, flags);
    }

    private MoveToDisplayItem(Parcel in) {
        this.mTargetDisplayId = in.readInt();
        this.mConfiguration = (Configuration) in.readTypedObject(Configuration.CREATOR);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MoveToDisplayItem other = (MoveToDisplayItem) o;
        if (this.mTargetDisplayId != other.mTargetDisplayId || !Objects.equals(this.mConfiguration, other.mConfiguration)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (31 * ((31 * 17) + this.mTargetDisplayId)) + this.mConfiguration.hashCode();
    }

    public String toString() {
        return "MoveToDisplayItem{targetDisplayId=" + this.mTargetDisplayId + ",configuration=" + this.mConfiguration + "}";
    }
}
