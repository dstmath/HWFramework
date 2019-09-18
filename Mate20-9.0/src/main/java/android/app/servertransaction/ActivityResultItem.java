package android.app.servertransaction;

import android.app.ClientTransactionHandler;
import android.app.ResultInfo;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Trace;
import java.util.List;
import java.util.Objects;

public class ActivityResultItem extends ClientTransactionItem {
    public static final Parcelable.Creator<ActivityResultItem> CREATOR = new Parcelable.Creator<ActivityResultItem>() {
        public ActivityResultItem createFromParcel(Parcel in) {
            return new ActivityResultItem(in);
        }

        public ActivityResultItem[] newArray(int size) {
            return new ActivityResultItem[size];
        }
    };
    private List<ResultInfo> mResultInfoList;

    public void execute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        Trace.traceBegin(64, "activityDeliverResult");
        client.handleSendResult(token, this.mResultInfoList, "ACTIVITY_RESULT");
        Trace.traceEnd(64);
    }

    private ActivityResultItem() {
    }

    public static ActivityResultItem obtain(List<ResultInfo> resultInfoList) {
        ActivityResultItem instance = (ActivityResultItem) ObjectPool.obtain(ActivityResultItem.class);
        if (instance == null) {
            instance = new ActivityResultItem();
        }
        instance.mResultInfoList = resultInfoList;
        return instance;
    }

    public void recycle() {
        this.mResultInfoList = null;
        ObjectPool.recycle(this);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.mResultInfoList, flags);
    }

    private ActivityResultItem(Parcel in) {
        this.mResultInfoList = in.createTypedArrayList(ResultInfo.CREATOR);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.mResultInfoList, ((ActivityResultItem) o).mResultInfoList);
    }

    public int hashCode() {
        return this.mResultInfoList.hashCode();
    }

    public String toString() {
        return "ActivityResultItem{resultInfoList=" + this.mResultInfoList + "}";
    }
}
