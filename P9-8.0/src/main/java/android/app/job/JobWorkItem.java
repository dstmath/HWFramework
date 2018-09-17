package android.app.job;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class JobWorkItem implements Parcelable {
    public static final Creator<JobWorkItem> CREATOR = new Creator<JobWorkItem>() {
        public JobWorkItem createFromParcel(Parcel in) {
            return new JobWorkItem(in);
        }

        public JobWorkItem[] newArray(int size) {
            return new JobWorkItem[size];
        }
    };
    int mDeliveryCount;
    Object mGrants;
    final Intent mIntent;
    int mWorkId;

    public JobWorkItem(Intent intent) {
        this.mIntent = intent;
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    public int getDeliveryCount() {
        return this.mDeliveryCount;
    }

    public void bumpDeliveryCount() {
        this.mDeliveryCount++;
    }

    public void setWorkId(int id) {
        this.mWorkId = id;
    }

    public int getWorkId() {
        return this.mWorkId;
    }

    public void setGrants(Object grants) {
        this.mGrants = grants;
    }

    public Object getGrants() {
        return this.mGrants;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append("JobWorkItem{id=");
        sb.append(this.mWorkId);
        sb.append(" intent=");
        sb.append(this.mIntent);
        if (this.mDeliveryCount != 0) {
            sb.append(" dcount=");
            sb.append(this.mDeliveryCount);
        }
        sb.append("}");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (this.mIntent != null) {
            out.writeInt(1);
            this.mIntent.writeToParcel(out, 0);
        } else {
            out.writeInt(0);
        }
        out.writeInt(this.mDeliveryCount);
        out.writeInt(this.mWorkId);
    }

    JobWorkItem(Parcel in) {
        if (in.readInt() != 0) {
            this.mIntent = (Intent) Intent.CREATOR.createFromParcel(in);
        } else {
            this.mIntent = null;
        }
        this.mDeliveryCount = in.readInt();
        this.mWorkId = in.readInt();
    }
}
