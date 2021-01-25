package huawei.android.security.facerecognition;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.hwpartsecurity.BuildConfig;

public class AuthParam implements Parcelable {
    public static final Parcelable.Creator<AuthParam> CREATOR = new Parcelable.Creator<AuthParam>() {
        /* class huawei.android.security.facerecognition.AuthParam.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AuthParam createFromParcel(Parcel in) {
            return new AuthParam(in);
        }

        @Override // android.os.Parcelable.Creator
        public AuthParam[] newArray(int size) {
            return new AuthParam[size];
        }
    };
    private int mFlags;
    private String mOwner;
    private long mReqId;
    private int mUserId;

    public AuthParam(int flags, int userId, long reqId, String owner) {
        this.mFlags = flags;
        this.mUserId = userId;
        this.mReqId = reqId;
        this.mOwner = getStringOrDefault(owner);
    }

    private AuthParam(Parcel in) {
        this.mFlags = in.readInt();
        this.mUserId = in.readInt();
        this.mReqId = in.readLong();
        this.mOwner = getStringOrDefault(in.readString());
    }

    public int getFlag() {
        return this.mFlags;
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
        out.writeInt(this.mFlags);
        out.writeInt(this.mUserId);
        out.writeLong(this.mReqId);
        out.writeString(this.mOwner);
    }

    private String getStringOrDefault(String readString) {
        return readString == null ? BuildConfig.FLAVOR : readString;
    }
}
