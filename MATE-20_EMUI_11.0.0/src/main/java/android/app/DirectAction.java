package android.app;

import android.content.LocusId;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;

public final class DirectAction implements Parcelable {
    public static final Parcelable.Creator<DirectAction> CREATOR = new Parcelable.Creator<DirectAction>() {
        /* class android.app.DirectAction.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DirectAction createFromParcel(Parcel in) {
            return new DirectAction(in);
        }

        @Override // android.os.Parcelable.Creator
        public DirectAction[] newArray(int size) {
            return new DirectAction[size];
        }
    };
    public static final String KEY_ACTIONS_LIST = "actions_list";
    private IBinder mActivityId;
    private final Bundle mExtras;
    private final String mID;
    private final LocusId mLocusId;
    private int mTaskId;

    public DirectAction(String id, Bundle extras, LocusId locusId) {
        this.mID = (String) Preconditions.checkStringNotEmpty(id);
        this.mExtras = extras;
        this.mLocusId = locusId;
    }

    public void setSource(int taskId, IBinder activityId) {
        this.mTaskId = taskId;
        this.mActivityId = activityId;
    }

    public DirectAction(DirectAction original) {
        this.mTaskId = original.mTaskId;
        this.mActivityId = original.mActivityId;
        this.mID = original.mID;
        this.mExtras = original.mExtras;
        this.mLocusId = original.mLocusId;
    }

    private DirectAction(Parcel in) {
        this.mTaskId = in.readInt();
        this.mActivityId = in.readStrongBinder();
        this.mID = in.readString();
        this.mExtras = in.readBundle();
        String idString = in.readString();
        this.mLocusId = idString != null ? new LocusId(idString) : null;
    }

    public int getTaskId() {
        return this.mTaskId;
    }

    public IBinder getActivityId() {
        return this.mActivityId;
    }

    public String getId() {
        return this.mID;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public LocusId getLocusId() {
        return this.mLocusId;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public int hashCode() {
        return this.mID.hashCode();
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        return this.mID.equals(((DirectAction) other).mID);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mTaskId);
        dest.writeStrongBinder(this.mActivityId);
        dest.writeString(this.mID);
        dest.writeBundle(this.mExtras);
        dest.writeString(this.mLocusId.getId());
    }

    public static final class Builder {
        private Bundle mExtras;
        private String mId;
        private LocusId mLocusId;

        public Builder(String id) {
            Preconditions.checkNotNull(id);
            this.mId = id;
        }

        public Builder setExtras(Bundle extras) {
            this.mExtras = extras;
            return this;
        }

        public Builder setLocusId(LocusId locusId) {
            this.mLocusId = locusId;
            return this;
        }

        public DirectAction build() {
            return new DirectAction(this.mId, this.mExtras, this.mLocusId);
        }
    }
}
