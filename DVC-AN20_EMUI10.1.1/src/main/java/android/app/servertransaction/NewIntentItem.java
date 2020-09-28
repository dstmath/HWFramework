package android.app.servertransaction;

import android.annotation.UnsupportedAppUsage;
import android.app.ClientTransactionHandler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Trace;
import com.android.internal.content.ReferrerIntent;
import java.util.List;
import java.util.Objects;

public class NewIntentItem extends ClientTransactionItem {
    public static final Parcelable.Creator<NewIntentItem> CREATOR = new Parcelable.Creator<NewIntentItem>() {
        /* class android.app.servertransaction.NewIntentItem.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NewIntentItem createFromParcel(Parcel in) {
            return new NewIntentItem(in);
        }

        @Override // android.os.Parcelable.Creator
        public NewIntentItem[] newArray(int size) {
            return new NewIntentItem[size];
        }
    };
    @UnsupportedAppUsage
    private List<ReferrerIntent> mIntents;
    private boolean mResume;

    @Override // android.app.servertransaction.ClientTransactionItem
    public int getPostExecutionState() {
        return this.mResume ? 3 : -1;
    }

    @Override // android.app.servertransaction.BaseClientRequest
    public void execute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        Trace.traceBegin(64, "activityNewIntent");
        client.handleNewIntent(token, this.mIntents);
        Trace.traceEnd(64);
    }

    private NewIntentItem() {
    }

    public static NewIntentItem obtain(List<ReferrerIntent> intents, boolean resume) {
        NewIntentItem instance = (NewIntentItem) ObjectPool.obtain(NewIntentItem.class);
        if (instance == null) {
            instance = new NewIntentItem();
        }
        instance.mIntents = intents;
        instance.mResume = resume;
        return instance;
    }

    @Override // android.app.servertransaction.ObjectPoolItem
    public void recycle() {
        this.mIntents = null;
        this.mResume = false;
        ObjectPool.recycle(this);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBoolean(this.mResume);
        dest.writeTypedList(this.mIntents, flags);
    }

    private NewIntentItem(Parcel in) {
        this.mResume = in.readBoolean();
        this.mIntents = in.createTypedArrayList(ReferrerIntent.CREATOR);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NewIntentItem other = (NewIntentItem) o;
        if (this.mResume != other.mResume || !Objects.equals(this.mIntents, other.mIntents)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (((17 * 31) + (this.mResume ? 1 : 0)) * 31) + this.mIntents.hashCode();
    }

    public String toString() {
        return "NewIntentItem{intents=" + this.mIntents + ",resume=" + this.mResume + "}";
    }
}
