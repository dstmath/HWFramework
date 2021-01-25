package android.app;

import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class WaitResult implements Parcelable {
    public static final Parcelable.Creator<WaitResult> CREATOR = new Parcelable.Creator<WaitResult>() {
        /* class android.app.WaitResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WaitResult createFromParcel(Parcel source) {
            return new WaitResult(source);
        }

        @Override // android.os.Parcelable.Creator
        public WaitResult[] newArray(int size) {
            return new WaitResult[size];
        }
    };
    public static final int INVALID_DELAY = -1;
    public static final int LAUNCH_STATE_COLD = 1;
    public static final int LAUNCH_STATE_HOT = 3;
    public static final int LAUNCH_STATE_WARM = 2;
    public int launchState;
    public int result;
    public boolean timeout;
    public long totalTime;
    public ComponentName who;

    @Retention(RetentionPolicy.SOURCE)
    public @interface LaunchState {
    }

    public WaitResult() {
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.result);
        dest.writeInt(this.timeout ? 1 : 0);
        ComponentName.writeToParcel(this.who, dest);
        dest.writeLong(this.totalTime);
        dest.writeInt(this.launchState);
    }

    private WaitResult(Parcel source) {
        this.result = source.readInt();
        this.timeout = source.readInt() != 0;
        this.who = ComponentName.readFromParcel(source);
        this.totalTime = source.readLong();
        this.launchState = source.readInt();
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "WaitResult:");
        pw.println(prefix + "  result=" + this.result);
        pw.println(prefix + "  timeout=" + this.timeout);
        pw.println(prefix + "  who=" + this.who);
        pw.println(prefix + "  totalTime=" + this.totalTime);
        pw.println(prefix + "  launchState=" + this.launchState);
    }

    public static String launchStateToString(int type) {
        if (type == 1) {
            return "COLD";
        }
        if (type == 2) {
            return "WARM";
        }
        if (type == 3) {
            return "HOT";
        }
        return "UNKNOWN (" + type + ")";
    }
}
