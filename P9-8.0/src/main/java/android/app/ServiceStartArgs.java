package android.app;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ServiceStartArgs implements Parcelable {
    public static final Creator<ServiceStartArgs> CREATOR = new Creator<ServiceStartArgs>() {
        public ServiceStartArgs createFromParcel(Parcel in) {
            return new ServiceStartArgs(in);
        }

        public ServiceStartArgs[] newArray(int size) {
            return new ServiceStartArgs[size];
        }
    };
    public final Intent args;
    public final int flags;
    public final int startId;
    public final boolean taskRemoved;

    public ServiceStartArgs(boolean _taskRemoved, int _startId, int _flags, Intent _args) {
        this.taskRemoved = _taskRemoved;
        this.startId = _startId;
        this.flags = _flags;
        this.args = _args;
    }

    public String toString() {
        return "ServiceStartArgs{taskRemoved=" + this.taskRemoved + ", startId=" + this.startId + ", flags=0x" + Integer.toHexString(this.flags) + ", args=" + this.args + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.taskRemoved ? 1 : 0);
        out.writeInt(this.startId);
        out.writeInt(flags);
        if (this.args != null) {
            out.writeInt(1);
            this.args.writeToParcel(out, 0);
            return;
        }
        out.writeInt(0);
    }

    public ServiceStartArgs(Parcel in) {
        boolean z = false;
        if (in.readInt() != 0) {
            z = true;
        }
        this.taskRemoved = z;
        this.startId = in.readInt();
        this.flags = in.readInt();
        if (in.readInt() != 0) {
            this.args = (Intent) Intent.CREATOR.createFromParcel(in);
        } else {
            this.args = null;
        }
    }
}
