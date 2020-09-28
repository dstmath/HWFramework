package android.telephony.ims;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.PhoneNumberUtils;
import java.util.Arrays;

@SystemApi
public final class ImsSuppServiceNotification implements Parcelable {
    public static final Parcelable.Creator<ImsSuppServiceNotification> CREATOR = new Parcelable.Creator<ImsSuppServiceNotification>() {
        /* class android.telephony.ims.ImsSuppServiceNotification.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ImsSuppServiceNotification createFromParcel(Parcel in) {
            return new ImsSuppServiceNotification(in);
        }

        @Override // android.os.Parcelable.Creator
        public ImsSuppServiceNotification[] newArray(int size) {
            return new ImsSuppServiceNotification[size];
        }
    };
    private static final String TAG = "ImsSuppServiceNotification";
    public final int code;
    public final String[] history;
    public final int index;
    public final int notificationType;
    public final String number;
    public final int type;

    public ImsSuppServiceNotification(int notificationType2, int code2, int index2, int type2, String number2, String[] history2) {
        this.notificationType = notificationType2;
        this.code = code2;
        this.index = index2;
        this.type = type2;
        this.number = number2;
        this.history = history2;
    }

    public ImsSuppServiceNotification(Parcel in) {
        this.notificationType = in.readInt();
        this.code = in.readInt();
        this.index = in.readInt();
        this.type = in.readInt();
        this.number = in.readString();
        this.history = in.createStringArray();
    }

    public String toString() {
        return "{ notificationType=" + this.notificationType + ", code=" + this.code + ", index=" + this.index + ", type=" + this.type + ", number=" + PhoneNumberUtils.toLogSafePhoneNumber(this.number) + ", history=" + PhoneNumberUtils.toLogSafePhoneNumber(Arrays.toString(this.history)) + " }";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.notificationType);
        out.writeInt(this.code);
        out.writeInt(this.index);
        out.writeInt(this.type);
        out.writeString(this.number);
        out.writeStringArray(this.history);
    }
}
