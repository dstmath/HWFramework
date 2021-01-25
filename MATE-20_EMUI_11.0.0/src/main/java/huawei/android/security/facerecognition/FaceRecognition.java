package huawei.android.security.facerecognition;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.hwpartsecurity.BuildConfig;

public class FaceRecognition implements Parcelable {
    public static final Parcelable.Creator<FaceRecognition> CREATOR = new Parcelable.Creator<FaceRecognition>() {
        /* class huawei.android.security.facerecognition.FaceRecognition.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public FaceRecognition createFromParcel(Parcel in) {
            return new FaceRecognition(in);
        }

        @Override // android.os.Parcelable.Creator
        public FaceRecognition[] newArray(int size) {
            return new FaceRecognition[size];
        }
    };
    private long mDeviceId;
    private int mFaceId;
    private CharSequence mName;
    private int mUserId;

    public FaceRecognition(CharSequence name, int userId, int faceId, long deviceId) {
        this.mName = getStringOrDefault(name);
        this.mUserId = userId;
        this.mFaceId = faceId;
        this.mDeviceId = deviceId;
    }

    private FaceRecognition(Parcel in) {
        this.mName = getStringOrDefault(in.readString());
        this.mUserId = in.readInt();
        this.mFaceId = in.readInt();
        this.mDeviceId = in.readLong();
    }

    public CharSequence getName() {
        return this.mName;
    }

    public int getFaceId() {
        return this.mFaceId;
    }

    public int getUserId() {
        return this.mUserId;
    }

    public long getDeviceId() {
        return this.mDeviceId;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mName.toString());
        out.writeInt(this.mUserId);
        out.writeInt(this.mFaceId);
        out.writeLong(this.mDeviceId);
    }

    private CharSequence getStringOrDefault(CharSequence readString) {
        return readString == null ? BuildConfig.FLAVOR : readString;
    }
}
