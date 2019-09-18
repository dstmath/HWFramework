package android.cover;

import android.os.Parcel;
import android.os.Parcelable;

public final class HallState implements Parcelable {
    public static final int COVER_HALL_CLOSE = 1;
    public static final int COVER_HALL_OPEN = 0;
    public static final Parcelable.Creator<HallState> CREATOR = new Parcelable.Creator<HallState>() {
        public HallState createFromParcel(Parcel source) {
            return new HallState(source);
        }

        public HallState[] newArray(int size) {
            return new HallState[size];
        }
    };
    public static final int SLIDE_HALL_CLOSE = 0;
    public static final int SLIDE_HALL_OPEN = 2;
    public static final int TYPE_HALL_COVER = 0;
    public static final int TYPE_HALL_SLIDE = 1;
    public int state;
    public long timestamp;
    public int type;

    public String toString() {
        return "HallState{type=" + this.type + ", state=" + this.state + ", timestamp=" + this.timestamp + "}";
    }

    public HallState() {
    }

    public HallState(Parcel in) {
        this.type = in.readInt();
        this.state = in.readInt();
        this.timestamp = in.readLong();
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        this.type = in.readInt();
        this.state = in.readInt();
        this.timestamp = in.readLong();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeInt(this.state);
        dest.writeLong(this.timestamp);
    }
}
