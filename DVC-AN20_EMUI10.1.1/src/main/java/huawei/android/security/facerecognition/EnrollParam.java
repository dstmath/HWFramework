package huawei.android.security.facerecognition;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;

public class EnrollParam implements Parcelable {
    public static final Parcelable.Creator<EnrollParam> CREATOR = new Parcelable.Creator<EnrollParam>() {
        /* class huawei.android.security.facerecognition.EnrollParam.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EnrollParam createFromParcel(Parcel in) {
            return new EnrollParam(in);
        }

        @Override // android.os.Parcelable.Creator
        public EnrollParam[] newArray(int size) {
            return new EnrollParam[size];
        }
    };
    private byte[] mAuthToken;
    private int mFlags;
    private String mOwner;
    private long mReqId;
    private int mUserId;

    public EnrollParam(byte[] authToken, int flags, int userId, long reqId, String owner) {
        this.mAuthToken = Arrays.copyOf(authToken, authToken.length);
        this.mFlags = flags;
        this.mUserId = userId;
        this.mReqId = reqId;
        this.mOwner = owner;
    }

    private EnrollParam(Parcel in) {
        this.mAuthToken = in.createByteArray();
        this.mFlags = in.readInt();
        this.mUserId = in.readInt();
        this.mReqId = in.readLong();
        this.mOwner = in.readString();
    }

    public byte[] getAuthToken() {
        byte[] bArr = this.mAuthToken;
        return Arrays.copyOf(bArr, bArr.length);
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByteArray(this.mAuthToken);
        out.writeInt(this.mFlags);
        out.writeInt(this.mUserId);
        out.writeLong(this.mReqId);
        out.writeString(this.mOwner);
    }
}
