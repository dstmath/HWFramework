package android.hardware;

import android.hardware.Camera;
import android.os.Parcel;
import android.os.Parcelable;

public class CameraInfo implements Parcelable {
    public static final Parcelable.Creator<CameraInfo> CREATOR = new Parcelable.Creator<CameraInfo>() {
        public CameraInfo createFromParcel(Parcel in) {
            CameraInfo info = new CameraInfo();
            info.readFromParcel(in);
            return info;
        }

        public CameraInfo[] newArray(int size) {
            return new CameraInfo[size];
        }
    };
    public Camera.CameraInfo info = new Camera.CameraInfo();

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.info.facing);
        out.writeInt(this.info.orientation);
    }

    public void readFromParcel(Parcel in) {
        this.info.facing = in.readInt();
        this.info.orientation = in.readInt();
    }
}
