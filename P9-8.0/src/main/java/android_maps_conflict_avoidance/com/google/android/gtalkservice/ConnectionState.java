package android_maps_conflict_avoidance.com.google.android.gtalkservice;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.OverlayItem;

public final class ConnectionState implements Parcelable {
    public static final Creator<ConnectionState> CREATOR = new Creator<ConnectionState>() {
        public ConnectionState createFromParcel(Parcel source) {
            return new ConnectionState(source);
        }

        public ConnectionState[] newArray(int size) {
            return new ConnectionState[size];
        }
    };
    private volatile int mState;

    public ConnectionState(Parcel source) {
        this.mState = source.readInt();
    }

    public final String toString() {
        return toString(this.mState);
    }

    public static final String toString(int state) {
        switch (state) {
            case 1:
                return "RECONNECTION_SCHEDULED";
            case OverlayItem.ITEM_STATE_SELECTED_MASK /*2*/:
                return "CONNECTING";
            case LayoutParams.LEFT /*3*/:
                return "AUTHENTICATED";
            case OverlayItem.ITEM_STATE_FOCUSED_MASK /*4*/:
                return "ONLINE";
            default:
                return "IDLE";
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mState);
    }

    public int describeContents() {
        return 0;
    }
}
