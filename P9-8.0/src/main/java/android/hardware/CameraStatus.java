package android.hardware;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class CameraStatus implements Parcelable {
    public static final Creator<CameraStatus> CREATOR = new Creator<CameraStatus>() {
        public CameraStatus createFromParcel(Parcel in) {
            CameraStatus status = new CameraStatus();
            status.readFromParcel(in);
            return status;
        }

        public CameraStatus[] newArray(int size) {
            return new CameraStatus[size];
        }
    };
    public String cameraId;
    public int status;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.cameraId);
        out.writeInt(this.status);
    }

    public void readFromParcel(Parcel in) {
        this.cameraId = in.readString();
        this.status = in.readInt();
    }
}
