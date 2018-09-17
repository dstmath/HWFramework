package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SmsCbCmasInfo implements Parcelable {
    public static final int CMAS_CATEGORY_CBRNE = 10;
    public static final int CMAS_CATEGORY_ENV = 7;
    public static final int CMAS_CATEGORY_FIRE = 5;
    public static final int CMAS_CATEGORY_GEO = 0;
    public static final int CMAS_CATEGORY_HEALTH = 6;
    public static final int CMAS_CATEGORY_INFRA = 9;
    public static final int CMAS_CATEGORY_MET = 1;
    public static final int CMAS_CATEGORY_OTHER = 11;
    public static final int CMAS_CATEGORY_RESCUE = 4;
    public static final int CMAS_CATEGORY_SAFETY = 2;
    public static final int CMAS_CATEGORY_SECURITY = 3;
    public static final int CMAS_CATEGORY_TRANSPORT = 8;
    public static final int CMAS_CATEGORY_UNKNOWN = -1;
    public static final int CMAS_CERTAINTY_LIKELY = 1;
    public static final int CMAS_CERTAINTY_OBSERVED = 0;
    public static final int CMAS_CERTAINTY_UNKNOWN = -1;
    public static final int CMAS_CLASS_CHILD_ABDUCTION_EMERGENCY = 3;
    public static final int CMAS_CLASS_CMAS_EXERCISE = 5;
    public static final int CMAS_CLASS_EXTREME_THREAT = 1;
    public static final int CMAS_CLASS_OPERATOR_DEFINED_USE = 6;
    public static final int CMAS_CLASS_PRESIDENTIAL_LEVEL_ALERT = 0;
    public static final int CMAS_CLASS_REQUIRED_MONTHLY_TEST = 4;
    public static final int CMAS_CLASS_SEVERE_THREAT = 2;
    public static final int CMAS_CLASS_UNKNOWN = -1;
    public static final int CMAS_RESPONSE_TYPE_ASSESS = 6;
    public static final int CMAS_RESPONSE_TYPE_AVOID = 5;
    public static final int CMAS_RESPONSE_TYPE_EVACUATE = 1;
    public static final int CMAS_RESPONSE_TYPE_EXECUTE = 3;
    public static final int CMAS_RESPONSE_TYPE_MONITOR = 4;
    public static final int CMAS_RESPONSE_TYPE_NONE = 7;
    public static final int CMAS_RESPONSE_TYPE_PREPARE = 2;
    public static final int CMAS_RESPONSE_TYPE_SHELTER = 0;
    public static final int CMAS_RESPONSE_TYPE_UNKNOWN = -1;
    public static final int CMAS_SEVERITY_EXTREME = 0;
    public static final int CMAS_SEVERITY_SEVERE = 1;
    public static final int CMAS_SEVERITY_UNKNOWN = -1;
    public static final int CMAS_URGENCY_EXPECTED = 1;
    public static final int CMAS_URGENCY_IMMEDIATE = 0;
    public static final int CMAS_URGENCY_UNKNOWN = -1;
    public static final Creator<SmsCbCmasInfo> CREATOR = new Creator<SmsCbCmasInfo>() {
        public SmsCbCmasInfo createFromParcel(Parcel in) {
            return new SmsCbCmasInfo(in);
        }

        public SmsCbCmasInfo[] newArray(int size) {
            return new SmsCbCmasInfo[size];
        }
    };
    private final int mCategory;
    private final int mCertainty;
    private final int mMessageClass;
    private final int mResponseType;
    private final int mSeverity;
    private final int mUrgency;

    public SmsCbCmasInfo(int messageClass, int category, int responseType, int severity, int urgency, int certainty) {
        this.mMessageClass = messageClass;
        this.mCategory = category;
        this.mResponseType = responseType;
        this.mSeverity = severity;
        this.mUrgency = urgency;
        this.mCertainty = certainty;
    }

    SmsCbCmasInfo(Parcel in) {
        this.mMessageClass = in.readInt();
        this.mCategory = in.readInt();
        this.mResponseType = in.readInt();
        this.mSeverity = in.readInt();
        this.mUrgency = in.readInt();
        this.mCertainty = in.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mMessageClass);
        dest.writeInt(this.mCategory);
        dest.writeInt(this.mResponseType);
        dest.writeInt(this.mSeverity);
        dest.writeInt(this.mUrgency);
        dest.writeInt(this.mCertainty);
    }

    public int getMessageClass() {
        return this.mMessageClass;
    }

    public int getCategory() {
        return this.mCategory;
    }

    public int getResponseType() {
        return this.mResponseType;
    }

    public int getSeverity() {
        return this.mSeverity;
    }

    public int getUrgency() {
        return this.mUrgency;
    }

    public int getCertainty() {
        return this.mCertainty;
    }

    public String toString() {
        return "SmsCbCmasInfo{messageClass=" + this.mMessageClass + ", category=" + this.mCategory + ", responseType=" + this.mResponseType + ", severity=" + this.mSeverity + ", urgency=" + this.mUrgency + ", certainty=" + this.mCertainty + '}';
    }

    public int describeContents() {
        return 0;
    }
}
