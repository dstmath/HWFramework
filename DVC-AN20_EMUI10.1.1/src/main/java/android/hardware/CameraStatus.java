package android.hardware;

import android.os.Parcel;
import android.os.Parcelable;

public class CameraStatus implements Parcelable {
    public static final Parcelable.Creator<CameraStatus> CREATOR = new Parcelable.Creator<CameraStatus>() {
        /* class android.hardware.CameraStatus.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CameraStatus createFromParcel(Parcel in) {
            CameraStatus status = new CameraStatus();
            status.readFromParcel(in);
            return status;
        }

        @Override // android.os.Parcelable.Creator
        public CameraStatus[] newArray(int size) {
            return new CameraStatus[size];
        }
    };
    public String cameraId;
    public int status;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.cameraId);
        out.writeInt(this.status);
    }

    public void readFromParcel(Parcel in) {
        this.cameraId = in.readString();
        this.status = in.readInt();
    }
}
