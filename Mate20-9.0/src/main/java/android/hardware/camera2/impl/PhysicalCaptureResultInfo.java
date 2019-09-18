package android.hardware.camera2.impl;

import android.os.Parcel;
import android.os.Parcelable;

public class PhysicalCaptureResultInfo implements Parcelable {
    public static final Parcelable.Creator<PhysicalCaptureResultInfo> CREATOR = new Parcelable.Creator<PhysicalCaptureResultInfo>() {
        public PhysicalCaptureResultInfo createFromParcel(Parcel in) {
            return new PhysicalCaptureResultInfo(in);
        }

        public PhysicalCaptureResultInfo[] newArray(int size) {
            return new PhysicalCaptureResultInfo[size];
        }
    };
    private String cameraId;
    private CameraMetadataNative cameraMetadata;

    private PhysicalCaptureResultInfo(Parcel in) {
        readFromParcel(in);
    }

    public PhysicalCaptureResultInfo(String cameraId2, CameraMetadataNative cameraMetadata2) {
        this.cameraId = cameraId2;
        this.cameraMetadata = cameraMetadata2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.cameraId);
        this.cameraMetadata.writeToParcel(dest, flags);
    }

    public void readFromParcel(Parcel in) {
        this.cameraId = in.readString();
        this.cameraMetadata = new CameraMetadataNative();
        this.cameraMetadata.readFromParcel(in);
    }

    public String getCameraId() {
        return this.cameraId;
    }

    public CameraMetadataNative getCameraMetadata() {
        return this.cameraMetadata;
    }
}
