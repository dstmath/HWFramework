package android.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.security.InvalidParameterException;

public class GpsNavigationMessageEvent implements Parcelable {
    public static final Creator<GpsNavigationMessageEvent> CREATOR = new Creator<GpsNavigationMessageEvent>() {
        public GpsNavigationMessageEvent createFromParcel(Parcel in) {
            return new GpsNavigationMessageEvent((GpsNavigationMessage) in.readParcelable(getClass().getClassLoader()));
        }

        public GpsNavigationMessageEvent[] newArray(int size) {
            return new GpsNavigationMessageEvent[size];
        }
    };
    public static int STATUS_GPS_LOCATION_DISABLED = 2;
    public static int STATUS_NOT_SUPPORTED = 0;
    public static int STATUS_READY = 1;
    private final GpsNavigationMessage mNavigationMessage;

    public interface Listener {
        void onGpsNavigationMessageReceived(GpsNavigationMessageEvent gpsNavigationMessageEvent);

        void onStatusChanged(int i);
    }

    public GpsNavigationMessageEvent(GpsNavigationMessage message) {
        if (message == null) {
            throw new InvalidParameterException("Parameter 'message' must not be null.");
        }
        this.mNavigationMessage = message;
    }

    public GpsNavigationMessage getNavigationMessage() {
        return this.mNavigationMessage;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(this.mNavigationMessage, flags);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("[ GpsNavigationMessageEvent:\n\n");
        builder.append(this.mNavigationMessage.toString());
        builder.append("\n]");
        return builder.toString();
    }
}
