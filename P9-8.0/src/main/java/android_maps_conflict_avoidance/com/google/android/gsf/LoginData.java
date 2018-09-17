package android_maps_conflict_avoidance.com.google.android.gsf;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class LoginData implements Parcelable {
    public static final Creator<LoginData> CREATOR = new Creator<LoginData>() {
        public LoginData createFromParcel(Parcel in) {
            return new LoginData(in, null);
        }

        public LoginData[] newArray(int size) {
            return new LoginData[size];
        }
    };
    public String mAuthtoken;
    public String mCaptchaAnswer;
    public byte[] mCaptchaData;
    public String mCaptchaMimeType;
    public String mCaptchaToken;
    public String mEncryptedPassword;
    public int mFlags;
    public String mJsonString;
    public String mOAuthAccessToken;
    public String mPassword;
    public String mService;
    public String mSid;
    public Status mStatus;
    public String mUsername;

    public enum Status {
        SUCCESS,
        ACCOUNT_DISABLED,
        BAD_USERNAME,
        BAD_REQUEST,
        LOGIN_FAIL,
        SERVER_ERROR,
        MISSING_APPS,
        NO_GMAIL,
        NETWORK_ERROR,
        CAPTCHA,
        CANCELLED,
        DELETED_GMAIL,
        OAUTH_MIGRATION_REQUIRED,
        DMAGENT
    }

    /* synthetic */ LoginData(Parcel in, LoginData -this1) {
        this(in);
    }

    public LoginData() {
        this.mUsername = null;
        this.mEncryptedPassword = null;
        this.mPassword = null;
        this.mService = null;
        this.mCaptchaToken = null;
        this.mCaptchaData = null;
        this.mCaptchaMimeType = null;
        this.mCaptchaAnswer = null;
        this.mFlags = 0;
        this.mStatus = null;
        this.mJsonString = null;
        this.mSid = null;
        this.mAuthtoken = null;
        this.mOAuthAccessToken = null;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mUsername);
        out.writeString(this.mEncryptedPassword);
        out.writeString(this.mPassword);
        out.writeString(this.mService);
        out.writeString(this.mCaptchaToken);
        if (this.mCaptchaData == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(this.mCaptchaData.length);
            out.writeByteArray(this.mCaptchaData);
        }
        out.writeString(this.mCaptchaMimeType);
        out.writeString(this.mCaptchaAnswer);
        out.writeInt(this.mFlags);
        if (this.mStatus == null) {
            out.writeString(null);
        } else {
            out.writeString(this.mStatus.name());
        }
        out.writeString(this.mJsonString);
        out.writeString(this.mSid);
        out.writeString(this.mAuthtoken);
        out.writeString(this.mOAuthAccessToken);
    }

    private LoginData(Parcel in) {
        this.mUsername = null;
        this.mEncryptedPassword = null;
        this.mPassword = null;
        this.mService = null;
        this.mCaptchaToken = null;
        this.mCaptchaData = null;
        this.mCaptchaMimeType = null;
        this.mCaptchaAnswer = null;
        this.mFlags = 0;
        this.mStatus = null;
        this.mJsonString = null;
        this.mSid = null;
        this.mAuthtoken = null;
        this.mOAuthAccessToken = null;
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.mUsername = in.readString();
        this.mEncryptedPassword = in.readString();
        this.mPassword = in.readString();
        this.mService = in.readString();
        this.mCaptchaToken = in.readString();
        int len = in.readInt();
        if (len == -1) {
            this.mCaptchaData = null;
        } else {
            this.mCaptchaData = new byte[len];
            in.readByteArray(this.mCaptchaData);
        }
        this.mCaptchaMimeType = in.readString();
        this.mCaptchaAnswer = in.readString();
        this.mFlags = in.readInt();
        String status = in.readString();
        if (status == null) {
            this.mStatus = null;
        } else {
            this.mStatus = Status.valueOf(status);
        }
        this.mJsonString = in.readString();
        this.mSid = in.readString();
        this.mAuthtoken = in.readString();
        this.mOAuthAccessToken = in.readString();
    }
}
