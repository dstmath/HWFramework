package android_maps_conflict_avoidance.com.google.android.gtalkservice;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.OverlayItem;

public final class ConnectionError implements Parcelable {
    public static final Creator<ConnectionError> CREATOR = new Creator<ConnectionError>() {
        public ConnectionError createFromParcel(Parcel source) {
            return new ConnectionError(source);
        }

        public ConnectionError[] newArray(int size) {
            return new ConnectionError[size];
        }
    };
    private int mError;

    public ConnectionError(Parcel source) {
        this.mError = source.readInt();
    }

    public final String toString() {
        return toString(this.mError);
    }

    public static final String toString(int state) {
        switch (state) {
            case 1:
                return "NO NETWORK";
            case OverlayItem.ITEM_STATE_SELECTED_MASK /*2*/:
                return "CONNECTION FAILED";
            case LayoutParams.LEFT /*3*/:
                return "UNKNOWN HOST";
            case OverlayItem.ITEM_STATE_FOCUSED_MASK /*4*/:
                return "AUTH FAILED";
            case LayoutParams.RIGHT /*5*/:
                return "AUTH EXPIRED";
            case 6:
                return "HEARTBEAT TIMEOUT";
            case 7:
                return "SERVER FAILED";
            case 8:
                return "SERVER REJECT - RATE LIMIT";
            case 10:
                return "UNKNOWN";
            default:
                return "NO ERROR";
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mError);
    }

    public int describeContents() {
        return 0;
    }
}
