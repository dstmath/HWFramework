package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public final class UssdResponse implements Parcelable {
    public static final Parcelable.Creator<UssdResponse> CREATOR = new Parcelable.Creator<UssdResponse>() {
        public UssdResponse createFromParcel(Parcel in) {
            return new UssdResponse(in.readString(), TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in));
        }

        public UssdResponse[] newArray(int size) {
            return new UssdResponse[size];
        }
    };
    private CharSequence mReturnMessage;
    private String mUssdRequest;

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUssdRequest);
        TextUtils.writeToParcel(this.mReturnMessage, dest, 0);
    }

    public String getUssdRequest() {
        return this.mUssdRequest;
    }

    public CharSequence getReturnMessage() {
        return this.mReturnMessage;
    }

    public int describeContents() {
        return 0;
    }

    public UssdResponse(String ussdRequest, CharSequence returnMessage) {
        this.mUssdRequest = ussdRequest;
        this.mReturnMessage = returnMessage;
    }
}
