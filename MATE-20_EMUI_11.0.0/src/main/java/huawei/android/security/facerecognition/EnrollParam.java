package huawei.android.security.facerecognition;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.hwpartsecurity.BuildConfig;
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
    private static final int LENGTH_OF_TOKEN = 69;
    private byte[] mAuthToken;
    private int mFlags;
    private String mOwner;
    private long mReqId;
    private int mUserId;

    public EnrollParam(byte[] authToken, int flags, int userId, long reqId, String owner) {
        this.mAuthToken = getByteArrayOrDefault(authToken);
        this.mFlags = flags;
        this.mUserId = userId;
        this.mReqId = reqId;
        this.mOwner = getStringOrDefault(owner);
    }

    private EnrollParam(Parcel in) {
        this.mAuthToken = getByteArrayOrDefault(in.createByteArray());
        this.mFlags = in.readInt();
        this.mUserId = in.readInt();
        this.mReqId = in.readLong();
        this.mOwner = getStringOrDefault(in.readString());
    }

    public byte[] getAuthToken() {
        return this.mAuthToken;
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
        out.writeByteArray(this.mAuthToken);
        out.writeInt(this.mFlags);
        out.writeInt(this.mUserId);
        out.writeLong(this.mReqId);
        out.writeString(this.mOwner);
    }

    private String getStringOrDefault(String readString) {
        return readString == null ? BuildConfig.FLAVOR : readString;
    }

    private byte[] getByteArrayOrDefault(byte[] readByteArray) {
        if (readByteArray == null || readByteArray.length != LENGTH_OF_TOKEN) {
            return new byte[0];
        }
        return Arrays.copyOf(readByteArray, readByteArray.length);
    }
}
