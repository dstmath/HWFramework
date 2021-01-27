package huawei.android.security.facerecognition;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.hwpartsecurity.BuildConfig;

public class RemoveParam implements Parcelable {
    public static final Parcelable.Creator<RemoveParam> CREATOR = new Parcelable.Creator<RemoveParam>() {
        /* class huawei.android.security.facerecognition.RemoveParam.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RemoveParam createFromParcel(Parcel in) {
            return new RemoveParam(in);
        }

        @Override // android.os.Parcelable.Creator
        public RemoveParam[] newArray(int size) {
            return new RemoveParam[size];
        }
    };
    private int mFaceId;
    private String mOwner;
    private long mReqId;
    private int mUserId;

    public RemoveParam(int faceId, int userId, long reqId, String owner) {
        this.mFaceId = faceId;
        this.mUserId = userId;
        this.mReqId = reqId;
        this.mOwner = getStringOrDefault(owner);
    }

    private RemoveParam(Parcel in) {
        this.mFaceId = in.readInt();
        this.mUserId = in.readInt();
        this.mReqId = in.readLong();
        this.mOwner = getStringOrDefault(in.readString());
    }

    public int getFaceId() {
        return this.mFaceId;
    }

    public int getUserId() {
        return this.mUserId;
    }

    public long getReqId() {
        return this.mReqId;
    }

    public String getOwner() {
        return this.mOwner;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mFaceId);
        out.writeInt(this.mUserId);
        out.writeLong(this.mReqId);
        out.writeString(this.mOwner);
    }

    private String getStringOrDefault(String readString) {
        return readString == null ? BuildConfig.FLAVOR : readString;
    }
}
