package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class WpsResult implements Parcelable {
    public static final Creator<WpsResult> CREATOR = new Creator<WpsResult>() {
        public WpsResult createFromParcel(Parcel in) {
            WpsResult result = new WpsResult();
            result.status = Status.valueOf(in.readString());
            result.pin = in.readString();
            return result;
        }

        public WpsResult[] newArray(int size) {
            return new WpsResult[size];
        }
    };
    public String pin;
    public Status status;

    public enum Status {
        SUCCESS,
        FAILURE,
        IN_PROGRESS
    }

    public WpsResult() {
        this.status = Status.FAILURE;
        this.pin = null;
    }

    public WpsResult(Status s) {
        this.status = s;
        this.pin = null;
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(" status: ").append(this.status.toString());
        sbuf.append(10);
        sbuf.append(" pin: ").append(this.pin);
        sbuf.append("\n");
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public WpsResult(WpsResult source) {
        if (source != null) {
            this.status = source.status;
            this.pin = source.pin;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.status.name());
        dest.writeString(this.pin);
    }
}
