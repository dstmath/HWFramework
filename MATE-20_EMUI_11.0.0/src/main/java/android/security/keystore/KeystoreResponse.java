package android.security.keystore;

import android.os.Parcel;
import android.os.Parcelable;

public class KeystoreResponse implements Parcelable {
    public static final Parcelable.Creator<KeystoreResponse> CREATOR = new Parcelable.Creator<KeystoreResponse>() {
        /* class android.security.keystore.KeystoreResponse.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public KeystoreResponse createFromParcel(Parcel in) {
            return new KeystoreResponse(in.readInt(), in.readString());
        }

        @Override // android.os.Parcelable.Creator
        public KeystoreResponse[] newArray(int size) {
            return new KeystoreResponse[size];
        }
    };
    public final int error_code_;
    public final String error_msg_;

    protected KeystoreResponse(int error_code, String error_msg) {
        this.error_code_ = error_code;
        this.error_msg_ = error_msg;
    }

    public final int getErrorCode() {
        return this.error_code_;
    }

    public final String getErrorMessage() {
        return this.error_msg_;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.error_code_);
        out.writeString(this.error_msg_);
    }
}
