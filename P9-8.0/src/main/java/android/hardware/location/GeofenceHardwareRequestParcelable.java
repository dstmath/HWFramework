package android.hardware.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public final class GeofenceHardwareRequestParcelable implements Parcelable {
    public static final Creator<GeofenceHardwareRequestParcelable> CREATOR = new Creator<GeofenceHardwareRequestParcelable>() {
        public GeofenceHardwareRequestParcelable createFromParcel(Parcel parcel) {
            if (parcel.readInt() != 0) {
                Log.e("GeofenceHardwareRequest", String.format("Invalid Geofence type: %d", new Object[]{Integer.valueOf(parcel.readInt())}));
                return null;
            }
            GeofenceHardwareRequest request = GeofenceHardwareRequest.createCircularGeofence(parcel.readDouble(), parcel.readDouble(), parcel.readDouble());
            request.setLastTransition(parcel.readInt());
            request.setMonitorTransitions(parcel.readInt());
            request.setUnknownTimer(parcel.readInt());
            request.setNotificationResponsiveness(parcel.readInt());
            request.setSourceTechnologies(parcel.readInt());
            return new GeofenceHardwareRequestParcelable(parcel.readInt(), request);
        }

        public GeofenceHardwareRequestParcelable[] newArray(int size) {
            return new GeofenceHardwareRequestParcelable[size];
        }
    };
    private int mId;
    private GeofenceHardwareRequest mRequest;

    public GeofenceHardwareRequestParcelable(int id, GeofenceHardwareRequest request) {
        this.mId = id;
        this.mRequest = request;
    }

    public int getId() {
        return this.mId;
    }

    public double getLatitude() {
        return this.mRequest.getLatitude();
    }

    public double getLongitude() {
        return this.mRequest.getLongitude();
    }

    public double getRadius() {
        return this.mRequest.getRadius();
    }

    public int getMonitorTransitions() {
        return this.mRequest.getMonitorTransitions();
    }

    public int getUnknownTimer() {
        return this.mRequest.getUnknownTimer();
    }

    public int getNotificationResponsiveness() {
        return this.mRequest.getNotificationResponsiveness();
    }

    public int getLastTransition() {
        return this.mRequest.getLastTransition();
    }

    int getType() {
        return this.mRequest.getType();
    }

    int getSourceTechnologies() {
        return this.mRequest.getSourceTechnologies();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("id=");
        builder.append(this.mId);
        builder.append(", type=");
        builder.append(this.mRequest.getType());
        builder.append(", latitude=");
        builder.append(this.mRequest.getLatitude());
        builder.append(", longitude=");
        builder.append(this.mRequest.getLongitude());
        builder.append(", radius=");
        builder.append(this.mRequest.getRadius());
        builder.append(", lastTransition=");
        builder.append(this.mRequest.getLastTransition());
        builder.append(", unknownTimer=");
        builder.append(this.mRequest.getUnknownTimer());
        builder.append(", monitorTransitions=");
        builder.append(this.mRequest.getMonitorTransitions());
        builder.append(", notificationResponsiveness=");
        builder.append(this.mRequest.getNotificationResponsiveness());
        builder.append(", sourceTechnologies=");
        builder.append(this.mRequest.getSourceTechnologies());
        return builder.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(getType());
        parcel.writeDouble(getLatitude());
        parcel.writeDouble(getLongitude());
        parcel.writeDouble(getRadius());
        parcel.writeInt(getLastTransition());
        parcel.writeInt(getMonitorTransitions());
        parcel.writeInt(getUnknownTimer());
        parcel.writeInt(getNotificationResponsiveness());
        parcel.writeInt(getSourceTechnologies());
        parcel.writeInt(getId());
    }
}
