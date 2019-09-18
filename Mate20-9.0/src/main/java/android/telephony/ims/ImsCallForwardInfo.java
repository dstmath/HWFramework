package android.telephony.ims;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public final class ImsCallForwardInfo implements Parcelable {
    public static final Parcelable.Creator<ImsCallForwardInfo> CREATOR = new Parcelable.Creator<ImsCallForwardInfo>() {
        public ImsCallForwardInfo createFromParcel(Parcel in) {
            return new ImsCallForwardInfo(in);
        }

        public ImsCallForwardInfo[] newArray(int size) {
            return new ImsCallForwardInfo[size];
        }
    };
    public int mCondition;
    public int mEndHour = 0;
    public int mEndMinute = 0;
    public String mNumber;
    public int mServiceClass;
    public int mStartHour = 0;
    public int mStartMinute = 0;
    public int mStatus;
    public int mTimeSeconds;
    public int mToA;

    public ImsCallForwardInfo() {
    }

    public ImsCallForwardInfo(int condition, int status, int toA, int serviceClass, String number, int replyTimerSec) {
        this.mCondition = condition;
        this.mStatus = status;
        this.mToA = toA;
        this.mServiceClass = serviceClass;
        this.mNumber = number;
        this.mTimeSeconds = replyTimerSec;
    }

    public ImsCallForwardInfo(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

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
