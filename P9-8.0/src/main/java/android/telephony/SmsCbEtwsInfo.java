package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.format.Time;
import com.android.internal.telephony.uicc.IccUtils;
import java.util.Arrays;

public class SmsCbEtwsInfo implements Parcelable {
    public static final Creator<SmsCbEtwsInfo> CREATOR = new Creator<SmsCbEtwsInfo>() {
        public SmsCbEtwsInfo createFromParcel(Parcel in) {
            return new SmsCbEtwsInfo(in);
        }

        public SmsCbEtwsInfo[] newArray(int size) {
            return new SmsCbEtwsInfo[size];
        }
    };
    public static final int ETWS_WARNING_TYPE_EARTHQUAKE = 0;
    public static final int ETWS_WARNING_TYPE_EARTHQUAKE_AND_TSUNAMI = 2;
    public static final int ETWS_WARNING_TYPE_OTHER_EMERGENCY = 4;
    public static final int ETWS_WARNING_TYPE_TEST_MESSAGE = 3;
    public static final int ETWS_WARNING_TYPE_TSUNAMI = 1;
    public static final int ETWS_WARNING_TYPE_UNKNOWN = -1;
    private final boolean mActivatePopup;
    private final boolean mEmergencyUserAlert;
    private final boolean mPrimary;
    private final byte[] mWarningSecurityInformation;
    private final int mWarningType;

    public SmsCbEtwsInfo(int warningType, boolean emergencyUserAlert, boolean activatePopup, boolean primary, byte[] warningSecurityInformation) {
        this.mWarningType = warningType;
        this.mEmergencyUserAlert = emergencyUserAlert;
        this.mActivatePopup = activatePopup;
        this.mPrimary = primary;
        this.mWarningSecurityInformation = warningSecurityInformation;
    }

    SmsCbEtwsInfo(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.mWarningType = in.readInt();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mEmergencyUserAlert = z;
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mActivatePopup = z;
        if (in.readInt() == 0) {
            z2 = false;
        }
        this.mPrimary = z2;
        this.mWarningSecurityInformation = in.createByteArray();
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeInt(this.mWarningType);
        if (this.mEmergencyUserAlert) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mActivatePopup) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.mPrimary) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeByteArray(this.mWarningSecurityInformation);
    }

    public int getWarningType() {
        return this.mWarningType;
    }

    public boolean isEmergencyUserAlert() {
        return this.mEmergencyUserAlert;
    }

    public boolean isPopupAlert() {
        return this.mActivatePopup;
    }

    public boolean isPrimary() {
        return this.mPrimary;
    }

    public long getPrimaryNotificationTimestamp() {
        if (this.mWarningSecurityInformation == null || this.mWarningSecurityInformation.length < 7) {
            return 0;
        }
        int year = IccUtils.gsmBcdByteToInt(this.mWarningSecurityInformation[0]);
        int month = IccUtils.gsmBcdByteToInt(this.mWarningSecurityInformation[1]);
        int day = IccUtils.gsmBcdByteToInt(this.mWarningSecurityInformation[2]);
        int hour = IccUtils.gsmBcdByteToInt(this.mWarningSecurityInformation[3]);
        int minute = IccUtils.gsmBcdByteToInt(this.mWarningSecurityInformation[4]);
        int second = IccUtils.gsmBcdByteToInt(this.mWarningSecurityInformation[5]);
        byte tzByte = this.mWarningSecurityInformation[6];
        int timezoneOffset = IccUtils.gsmBcdByteToInt((byte) (tzByte & -9));
        if ((tzByte & 8) != 0) {
            timezoneOffset = -timezoneOffset;
        }
        Time time = new Time(Time.TIMEZONE_UTC);
        time.year = year + 2000;
        time.month = month - 1;
        time.monthDay = day;
        time.hour = hour;
        time.minute = minute;
        time.second = second;
        return time.toMillis(true) - ((long) (((timezoneOffset * 15) * 60) * 1000));
    }

    public byte[] getPrimaryNotificationSignature() {
        if (this.mWarningSecurityInformation == null || this.mWarningSecurityInformation.length < 50) {
            return null;
        }
        return Arrays.copyOfRange(this.mWarningSecurityInformation, 7, 50);
    }

    public String toString() {
        return "SmsCbEtwsInfo{warningType=" + this.mWarningType + ", emergencyUserAlert=" + this.mEmergencyUserAlert + ", activatePopup=" + this.mActivatePopup + '}';
    }

    public int describeContents() {
        return 0;
    }
}
