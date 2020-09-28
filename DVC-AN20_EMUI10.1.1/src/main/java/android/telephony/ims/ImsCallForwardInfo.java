package android.telephony.ims;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SystemApi
public final class ImsCallForwardInfo implements Parcelable {
    public static final int CDIV_CF_REASON_ALL = 4;
    public static final int CDIV_CF_REASON_ALL_CONDITIONAL = 5;
    public static final int CDIV_CF_REASON_BUSY = 1;
    public static final int CDIV_CF_REASON_NOT_LOGGED_IN = 6;
    public static final int CDIV_CF_REASON_NOT_REACHABLE = 3;
    public static final int CDIV_CF_REASON_NO_REPLY = 2;
    public static final int CDIV_CF_REASON_UNCONDITIONAL = 0;
    public static final Parcelable.Creator<ImsCallForwardInfo> CREATOR = new Parcelable.Creator<ImsCallForwardInfo>() {
        /* class android.telephony.ims.ImsCallForwardInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ImsCallForwardInfo createFromParcel(Parcel in) {
            return new ImsCallForwardInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public ImsCallForwardInfo[] newArray(int size) {
            return new ImsCallForwardInfo[size];
        }
    };
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_NOT_ACTIVE = 0;
    public static final int TYPE_OF_ADDRESS_INTERNATIONAL = 145;
    public static final int TYPE_OF_ADDRESS_UNKNOWN = 129;
    @UnsupportedAppUsage
    public int mCondition;
    public int mEndHour = 0;
    public int mEndMinute = 0;
    @UnsupportedAppUsage
    public String mNumber;
    @UnsupportedAppUsage
    public int mServiceClass;
    public int mStartHour = 0;
    public int mStartMinute = 0;
    @UnsupportedAppUsage
    public int mStatus;
    @UnsupportedAppUsage
    public int mTimeSeconds;
    @UnsupportedAppUsage
    public int mToA;

    @Retention(RetentionPolicy.SOURCE)
    public @interface CallForwardReasons {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface CallForwardStatus {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface TypeOfAddress {
    }

    @UnsupportedAppUsage
    public ImsCallForwardInfo() {
    }

    public ImsCallForwardInfo(int reason, int status, int toA, int serviceClass, String number, int replyTimerSec) {
        this.mCondition = reason;
        this.mStatus = status;
        this.mToA = toA;
        this.mServiceClass = serviceClass;
        this.mNumber = number;
        this.mTimeSeconds = replyTimerSec;
    }

    public ImsCallForwardInfo(Parcel in) {
        readFromParcel(in);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mCondition);
        out.writeInt(this.mStatus);
        out.writeInt(this.mToA);
        out.writeString(this.mNumber);
        out.writeInt(this.mTimeSeconds);
        out.writeInt(this.mServiceClass);
        out.writeInt(this.mStartHour);
        out.writeInt(this.mStartMinute);
        out.writeInt(this.mEndHour);
        out.writeInt(this.mEndMinute);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", Condition: ");
        sb.append(this.mCondition);
        sb.append(", Status: ");
        sb.append(this.mStatus == 0 ? "disabled" : "enabled");
        sb.append(", ToA: ");
        sb.append(this.mToA);
        sb.append(", Service Class: ");
        sb.append(this.mServiceClass);
        sb.append(", Number=");
        sb.append(this.mNumber);
        sb.append(", Time (seconds): ");
        sb.append(this.mTimeSeconds);
        sb.append(", mStartHour=");
        sb.append(this.mStartHour);
        sb.append(", mStartMinute=");
        sb.append(this.mStartMinute);
        sb.append(", mEndHour=");
        sb.append(this.mEndHour);
        sb.append(", mEndMinute");
        sb.append(this.mEndMinute);
        return sb.toString();
    }

    private void readFromParcel(Parcel in) {
        this.mCondition = in.readInt();
        this.mStatus = in.readInt();
        this.mToA = in.readInt();
        this.mNumber = in.readString();
        this.mTimeSeconds = in.readInt();
        this.mServiceClass = in.readInt();
        this.mStartHour = in.readInt();
        this.mStartMinute = in.readInt();
        this.mEndHour = in.readInt();
        this.mEndMinute = in.readInt();
    }

    public int getCondition() {
        return this.mCondition;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public int getToA() {
        return this.mToA;
    }

    public int getServiceClass() {
        return this.mServiceClass;
    }

    public String getNumber() {
        return this.mNumber;
    }

    public int getTimeSeconds() {
        return this.mTimeSeconds;
    }
}
