package android.app.servertransaction;

import android.app.ActivityThread;
import android.app.ClientTransactionHandler;
import android.app.ResultInfo;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Trace;
import android.util.MergedConfiguration;
import android.util.Slog;
import com.android.internal.content.ReferrerIntent;
import java.util.List;
import java.util.Objects;

public class ActivityRelaunchItem extends ClientTransactionItem {
    public static final Parcelable.Creator<ActivityRelaunchItem> CREATOR = new Parcelable.Creator<ActivityRelaunchItem>() {
        /* class android.app.servertransaction.ActivityRelaunchItem.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ActivityRelaunchItem createFromParcel(Parcel in) {
            return new ActivityRelaunchItem(in);
        }

        @Override // android.os.Parcelable.Creator
        public ActivityRelaunchItem[] newArray(int size) {
            return new ActivityRelaunchItem[size];
        }
    };
    private static final String TAG = "ActivityRelaunchItem";
    private ActivityThread.ActivityClientRecord mActivityClientRecord;
    private MergedConfiguration mConfig;
    private int mConfigChanges;
    private List<ReferrerIntent> mPendingNewIntents;
    private List<ResultInfo> mPendingResults;
    private boolean mPreserveWindow;

    @Override // android.app.servertransaction.BaseClientRequest
    public void preExecute(ClientTransactionHandler client, IBinder token) {
        this.mActivityClientRecord = client.prepareRelaunchActivity(token, this.mPendingResults, this.mPendingNewIntents, this.mConfigChanges, this.mConfig, this.mPreserveWindow);
    }

    @Override // android.app.servertransaction.BaseClientRequest
    public void execute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        if (this.mActivityClientRecord != null) {
            Trace.traceBegin(64, "activityRestart");
            client.handleRelaunchActivity(this.mActivityClientRecord, pendingActions);
            Trace.traceEnd(64);
        } else if (ActivityThread.DEBUG_ORDER) {
            Slog.d(TAG, "Activity relaunch cancelled");
        }
    }

    @Override // android.app.servertransaction.BaseClientRequest
    public void postExecute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        client.reportRelaunch(token, pendingActions);
    }

    private ActivityRelaunchItem() {
    }

    public static ActivityRelaunchItem obtain(List<ResultInfo> pendingResults, List<ReferrerIntent> pendingNewIntents, int configChanges, MergedConfiguration config, boolean preserveWindow) {
        ActivityRelaunchItem instance = (ActivityRelaunchItem) ObjectPool.obtain(ActivityRelaunchItem.class);
        if (instance == null) {
            instance = new ActivityRelaunchItem();
        }
        instance.mPendingResults = pendingResults;
        instance.mPendingNewIntents = pendingNewIntents;
        instance.mConfigChanges = configChanges;
        instance.mConfig = config;
        instance.mPreserveWindow = preserveWindow;
        return instance;
    }

    @Override // android.app.servertransaction.ObjectPoolItem
    public void recycle() {
        this.mPendingResults = null;
        this.mPendingNewIntents = null;
        this.mConfigChanges = 0;
        this.mConfig = null;
        this.mPreserveWindow = false;
        this.mActivityClientRecord = null;
        ObjectPool.recycle(this);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.mPendingResults, flags);
        dest.writeTypedList(this.mPendingNewIntents, flags);
        dest.writeInt(this.mConfigChanges);
        dest.writeTypedObject(this.mConfig, flags);
        dest.writeBoolean(this.mPreserveWindow);
    }

    private ActivityRelaunchItem(Parcel in) {
        this.mPendingResults = in.createTypedArrayList(ResultInfo.CREATOR);
        this.mPendingNewIntents = in.createTypedArrayList(ReferrerIntent.CREATOR);
        this.mConfigChanges = in.readInt();
        this.mConfig = (MergedConfiguration) in.readTypedObject(MergedConfiguration.CREATOR);
        this.mPreserveWindow = in.readBoolean();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActivityRelaunchItem other = (ActivityRelaunchItem) o;
        if (!Objects.equals(this.mPendingResults, other.mPendingResults) || !Objects.equals(this.mPendingNewIntents, other.mPendingNewIntents) || this.mConfigChanges != other.mConfigChanges || !Objects.equals(this.mConfig, other.mConfig) || this.mPreserveWindow != other.mPreserveWindow) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (((((((((17 * 31) + Objects.hashCode(this.mPendingResults)) * 31) + Objects.hashCode(this.mPendingNewIntents)) * 31) + this.mConfigChanges) * 31) + Objects.hashCode(this.mConfig)) * 31) + (this.mPreserveWindow ? 1 : 0);
    }

    public String toString() {
        return "ActivityRelaunchItem{pendingResults=" + this.mPendingResults + ",pendingNewIntents=" + this.mPendingNewIntents + ",configChanges=" + this.mConfigChanges + ",config=" + this.mConfig + ",preserveWindow" + this.mPreserveWindow + "}";
    }
}
