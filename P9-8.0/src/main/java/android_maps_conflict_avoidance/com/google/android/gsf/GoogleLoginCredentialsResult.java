package android_maps_conflict_avoidance.com.google.android.gsf;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class GoogleLoginCredentialsResult implements Parcelable {
    public static final Creator<GoogleLoginCredentialsResult> CREATOR = new Creator<GoogleLoginCredentialsResult>() {
        public GoogleLoginCredentialsResult createFromParcel(Parcel in) {
            return new GoogleLoginCredentialsResult(in, null);
        }

        public GoogleLoginCredentialsResult[] newArray(int size) {
            return new GoogleLoginCredentialsResult[size];
        }
    };
    private String mAccount;
    private Intent mCredentialsIntent;
    private String mCredentialsString;

    /* synthetic */ GoogleLoginCredentialsResult(Parcel in, GoogleLoginCredentialsResult -this1) {
        this(in);
    }

    public GoogleLoginCredentialsResult() {
        this.mCredentialsString = null;
        this.mCredentialsIntent = null;
        this.mAccount = null;
    }

    public int describeContents() {
        return this.mCredentialsIntent != null ? this.mCredentialsIntent.describeContents() : 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mAccount);
        out.writeString(this.mCredentialsString);
        if (this.mCredentialsIntent != null) {
            out.writeInt(1);
            this.mCredentialsIntent.writeToParcel(out, 0);
            return;
        }
        out.writeInt(0);
    }

    private GoogleLoginCredentialsResult(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.mAccount = in.readString();
        this.mCredentialsString = in.readString();
        int hasIntent = in.readInt();
        this.mCredentialsIntent = null;
        if (hasIntent == 1) {
            this.mCredentialsIntent = new Intent();
            this.mCredentialsIntent.readFromParcel(in);
            this.mCredentialsIntent.setExtrasClassLoader(getClass().getClassLoader());
        }
    }
}
