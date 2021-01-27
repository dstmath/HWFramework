package android.app.servertransaction;

import android.app.ClientTransactionHandler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Trace;

public class StopActivityItem extends ActivityLifecycleItem {
    public static final Parcelable.Creator<StopActivityItem> CREATOR = new Parcelable.Creator<StopActivityItem>() {
        /* class android.app.servertransaction.StopActivityItem.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public StopActivityItem createFromParcel(Parcel in) {
            return new StopActivityItem(in);
        }

        @Override // android.os.Parcelable.Creator
        public StopActivityItem[] newArray(int size) {
            return new StopActivityItem[size];
        }
    };
    private static final String TAG = "StopActivityItem";
    private int mConfigChanges;
    private boolean mShowWindow;

    @Override // android.app.servertransaction.BaseClientRequest
    public void execute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        Trace.traceBegin(64, "activityStop");
        client.handleStopActivity(token, this.mShowWindow, this.mConfigChanges, pendingActions, true, "STOP_ACTIVITY_ITEM");
        Trace.traceEnd(64);
    }

    @Override // android.app.servertransaction.BaseClientRequest
    public void postExecute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        client.reportStop(pendingActions);
    }

    @Override // android.app.servertransaction.ActivityLifecycleItem
    public int getTargetState() {
        return 5;
    }

    private StopActivityItem() {
    }

    public static StopActivityItem obtain(boolean showWindow, int configChanges) {
        StopActivityItem instance = (StopActivityItem) ObjectPool.obtain(StopActivityItem.class);
        if (instance == null) {
            instance = new StopActivityItem();
        }
        instance.mShowWindow = showWindow;
        instance.mConfigChanges = configChanges;
        return instance;
    }

    @Override // android.app.servertransaction.ActivityLifecycleItem, android.app.servertransaction.ObjectPoolItem
    public void recycle() {
        super.recycle();
        this.mShowWindow = false;
        this.mConfigChanges = 0;
        ObjectPool.recycle(this);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBoolean(this.mShowWindow);
        dest.writeInt(this.mConfigChanges);
    }

    private StopActivityItem(Parcel in) {
        this.mShowWindow = in.readBoolean();
        this.mConfigChanges = in.readInt();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StopActivityItem other = (StopActivityItem) o;
        if (this.mShowWindow == other.mShowWindow && this.mConfigChanges == other.mConfigChanges) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (((17 * 31) + (this.mShowWindow ? 1 : 0)) * 31) + this.mConfigChanges;
    }

    public String toString() {
        return "StopActivityItem{showWindow=" + this.mShowWindow + ",configChanges=" + this.mConfigChanges + "}";
    }
}
