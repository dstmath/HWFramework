package android.app;

import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.PrintWriter;

public class WaitResult implements Parcelable {
    public static final Parcelable.Creator<WaitResult> CREATOR = new Parcelable.Creator<WaitResult>() {
        public WaitResult createFromParcel(Parcel source) {
            return new WaitResult(source);
        }

        public WaitResult[] newArray(int size) {
            return new WaitResult[size];
        }
    };
    public ComponentName origin;
    public int result;
    public long thisTime;
    public boolean timeout;
    public long totalTime;
    public ComponentName who;

    public WaitResult() {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.result);
        dest.writeInt(this.timeout ? 1 : 0);
        ComponentName.writeToParcel(this.who, dest);
        dest.writeLong(this.thisTime);
        dest.writeLong(this.totalTime);
        ComponentName.writeToParcel(this.origin, dest);
    }

    private WaitResult(Parcel source) {
        this.result = source.readInt();
        this.timeout = source.readInt() != 0;
        this.who = ComponentName.readFromParcel(source);
        this.thisTime = source.readLong();
        this.totalTime = source.readLong();
        this.origin = ComponentName.readFromParcel(source);
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "WaitResult:");
        pw.println(prefix + "  result=" + this.result);
        pw.println(prefix + "  timeout=" + this.timeout);
        pw.println(prefix + "  who=" + this.who);
        pw.println(prefix + "  thisTime=" + this.thisTime);
        pw.println(prefix + "  totalTime=" + this.totalTime);
        pw.println(prefix + "  origin=" + this.origin);
    }
}
