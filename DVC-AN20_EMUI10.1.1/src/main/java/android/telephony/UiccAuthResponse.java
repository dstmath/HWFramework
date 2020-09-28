package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;

public class UiccAuthResponse implements Parcelable {
    public static final int AUTH_RESP_FAIL = 1;
    public static final int AUTH_RESP_SUCCESS = 0;
    public static final int AUTH_RESP_SYNC_FAIL = 2;
    public static final int AUTH_RESP_UNSUPPORTED = 3;
    public static final Parcelable.Creator<UiccAuthResponse> CREATOR = new Parcelable.Creator<UiccAuthResponse>() {
        /* class android.telephony.UiccAuthResponse.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UiccAuthResponse createFromParcel(Parcel in) {
            return new UiccAuthResponse(in);
        }

        @Override // android.os.Parcelable.Creator
        public UiccAuthResponse[] newArray(int size) {
            return new UiccAuthResponse[size];
        }
    };
    public int mResult;
    public UiccAuthChallenge mUiccAuthChallenge;
    public UiccAuthResponseData mUiccAuthSyncFail;

    public static class UiccAuthChallenge {
        public UiccAuthResponseData mCkData;
        public UiccAuthResponseData mIkData;
        public UiccAuthResponseData mResData;
    }

    public static class UiccAuthResponseData {
        public byte[] data;
        public int len;
        public int present;
    }

    public UiccAuthResponse() {
        this.mUiccAuthChallenge = new UiccAuthChallenge();
        this.mUiccAuthSyncFail = new UiccAuthResponseData();
    }

    private UiccAuthResponse(Parcel in) {
        this.mUiccAuthChallenge = new UiccAuthChallenge();
        this.mUiccAuthSyncFail = new UiccAuthResponseData();
        this.mResult = in.readInt();
        if (this.mResult == 0) {
            this.mUiccAuthChallenge.mResData = new UiccAuthResponseData();
            this.mUiccAuthChallenge.mResData.present = in.readInt();
            this.mUiccAuthChallenge.mResData.len = in.readInt();
            if (this.mUiccAuthChallenge.mResData.len > 0) {
                this.mUiccAuthChallenge.mResData.data = in.createByteArray();
            } else {
                this.mUiccAuthChallenge.mResData.data = null;
                in.readInt();
            }
            this.mUiccAuthChallenge.mIkData = new UiccAuthResponseData();
            this.mUiccAuthChallenge.mIkData.present = in.readInt();
            this.mUiccAuthChallenge.mIkData.len = in.readInt();
            if (this.mUiccAuthChallenge.mIkData.len > 0) {
                this.mUiccAuthChallenge.mIkData.data = in.createByteArray();
            } else {
                this.mUiccAuthChallenge.mIkData.data = null;
                in.readInt();
            }
            this.mUiccAuthChallenge.mCkData = new UiccAuthResponseData();
            this.mUiccAuthChallenge.mCkData.present = in.readInt();
            this.mUiccAuthChallenge.mCkData.len = in.readInt();
            if (this.mUiccAuthChallenge.mCkData.len > 0) {
                this.mUiccAuthChallenge.mCkData.data = in.createByteArray();
                return;
            }
            this.mUiccAuthChallenge.mCkData.data = null;
            in.readInt();
            return;
        }
        this.mUiccAuthSyncFail.present = in.readInt();
        this.mUiccAuthSyncFail.len = in.readInt();
        if (this.mUiccAuthSyncFail.len > 0) {
            this.mUiccAuthSyncFail.data = in.createByteArray();
            return;
        }
        this.mUiccAuthSyncFail.data = null;
        in.readInt();
    }

    public int getResult() {
        return this.mResult;
    }

    public UiccAuthChallenge getAuthChallenge() {
        return this.mUiccAuthChallenge;
    }

    public UiccAuthResponseData getAuthSyncFail() {
        return this.mUiccAuthSyncFail;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mResult);
        if (this.mResult == 0) {
            out.writeInt(this.mUiccAuthChallenge.mResData.present);
            out.writeInt(this.mUiccAuthChallenge.mResData.len);
            if (this.mUiccAuthChallenge.mResData.len != 0) {
                out.writeByteArray(this.mUiccAuthChallenge.mResData.data, 0, this.mUiccAuthChallenge.mResData.len);
            } else {
                out.writeInt(0);
            }
            out.writeInt(this.mUiccAuthChallenge.mIkData.present);
            out.writeInt(this.mUiccAuthChallenge.mIkData.len);
            if (this.mUiccAuthChallenge.mIkData.len != 0) {
                out.writeByteArray(this.mUiccAuthChallenge.mIkData.data, 0, this.mUiccAuthChallenge.mIkData.len);
            } else {
                out.writeInt(0);
            }
            out.writeInt(this.mUiccAuthChallenge.mCkData.present);
            out.writeInt(this.mUiccAuthChallenge.mCkData.len);
            if (this.mUiccAuthChallenge.mCkData.len != 0) {
                out.writeByteArray(this.mUiccAuthChallenge.mCkData.data, 0, this.mUiccAuthChallenge.mCkData.len);
            } else {
                out.writeInt(0);
            }
        } else {
            out.writeInt(this.mUiccAuthSyncFail.present);
            out.writeInt(this.mUiccAuthSyncFail.len);
            if (this.mUiccAuthSyncFail.len != 0) {
                out.writeByteArray(this.mUiccAuthSyncFail.data, 0, this.mUiccAuthSyncFail.len);
            } else {
                out.writeInt(0);
            }
        }
    }

    public UiccAuthResponseData getResData() {
        return this.mUiccAuthChallenge.mResData;
    }

    public UiccAuthResponseData getIkData() {
        return this.mUiccAuthChallenge.mIkData;
    }

    public UiccAuthResponseData getCkData() {
        return this.mUiccAuthChallenge.mCkData;
    }
}
