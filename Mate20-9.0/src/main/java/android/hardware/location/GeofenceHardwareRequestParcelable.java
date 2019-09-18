package android.hardware.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public final class GeofenceHardwareRequestParcelable implements Parcelable {
    public static final Parcelable.Creator<GeofenceHardwareRequestParcelable> CREATOR = new Parcelable.Creator<GeofenceHardwareRequestParcelable>() {
        public GeofenceHardwareRequestParcelable createFromParcel(Parcel parcel) {
            int geofenceType = parcel.readInt();
            if (geofenceType != 0) {
                Log.e("GeofenceHardwareRequest", String.format("Invalid Geofence type: %d", new Object[]{Integer.valueOf(geofenceType)}));
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

    /* access modifiers changed from: package-private */
    public int getType() {
        return this.mRequest.getType();
    }

    /* access modifiers changed from: package-private */
    public int getSourceTechnologies() {
        return this.mRequest.getSourceTechnologies();
    }

    public String toString() {
        return "id=" + this.mId + ", type=" + this.mRequest.getType() + ", latitude=" + this.mRequest.getLatitude() + ", longitude=" + this.mRequest.getLongitude() + ", radius=" + this.mRequest.getRadius() + ", lastTransition=" + this.mRequest.getLastTransition() + ", unknownTimer=" + this.mRequest.getUnknownTimer() + ", monitorTransitions=" + this.mRequest.getMonitorTransitions() + ", notificationResponsiveness=" + this.mRequest.getNotificationResponsiveness() + ", sourceTechnologies=" + this.mRequest.getSourceTechnologies();
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
