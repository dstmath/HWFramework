package android.emcom;

import android.emcom.SmartcareInfos.SmartcareBaseInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class EmailInfo extends SmartcareBaseInfo implements Parcelable {
    public static final Creator<EmailInfo> CREATOR = new Creator<EmailInfo>() {
        public EmailInfo createFromParcel(Parcel in) {
            return new EmailInfo(in);
        }

        public EmailInfo[] newArray(int size) {
            return new EmailInfo[size];
        }
    };
    public byte abnormalDroppingFlag;
    public String appName;
    public int emailEndTime;
    public int emailStartDate;
    public int emailStartTime;
    public int emailThrouput;
    public String hostName;
    public int latency;
    public byte protocolType;
    public byte successFlag;
    public byte type;

    public EmailInfo(Parcel in) {
        this.appName = in.readString();
        this.type = (byte) (in.readInt() & 255);
        this.latency = in.readInt();
        this.successFlag = (byte) (in.readInt() & 255);
        this.abnormalDroppingFlag = (byte) (in.readInt() & 255);
        this.hostName = in.readString();
        this.emailStartDate = in.readInt();
        this.emailStartTime = in.readInt();
        this.emailEndTime = in.readInt();
        this.protocolType = (byte) (in.readInt() & 255);
        this.emailThrouput = in.readInt();
    }

    public void addToInfos(SmartcareInfos cis) {
        super.addToInfos(cis);
        cis.emailInfo = this;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.appName);
        dest.writeInt(this.type);
        dest.writeInt(this.latency);
        dest.writeInt(this.successFlag);
        dest.writeInt(this.abnormalDroppingFlag);
        dest.writeString(this.hostName);
        dest.writeInt(this.emailStartDate);
        dest.writeInt(this.emailStartTime);
        dest.writeInt(this.emailEndTime);
        dest.writeInt(this.protocolType);
        dest.writeInt(this.emailThrouput);
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        this.appName = in.readString();
        this.type = (byte) (in.readInt() & 255);
        this.latency = (short) in.readInt();
        this.successFlag = (byte) (in.readInt() & 255);
        this.abnormalDroppingFlag = (byte) (in.readInt() & 255);
        this.hostName = in.readString();
        this.emailStartDate = in.readInt();
        this.emailStartTime = in.readInt();
        this.emailEndTime = in.readInt();
        this.protocolType = (byte) (in.readInt() & 255);
        this.emailThrouput = in.readInt();
    }

    public final void copyFrom(EmailInfo o) {
        this.appName = o.appName;
        this.type = o.type;
        this.latency = o.latency;
        this.successFlag = o.successFlag;
        this.abnormalDroppingFlag = o.abnormalDroppingFlag;
        this.hostName = o.hostName;
        this.emailStartDate = o.emailStartDate;
        this.emailStartTime = o.emailStartTime;
        this.emailEndTime = o.emailEndTime;
        this.protocolType = o.protocolType;
        this.emailThrouput = o.emailThrouput;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("hash: ");
        sb.append(hashCode());
        sb.append(",app: ");
        sb.append(this.appName);
        sb.append(",type: ");
        sb.append(this.type);
        sb.append(",latency: ");
        sb.append(this.latency);
        sb.append(",successFlag: ");
        sb.append(this.successFlag);
        sb.append(",abnormalDroppingFlag: ");
        sb.append(this.abnormalDroppingFlag);
        sb.append(",hostName: ");
        sb.append(this.hostName);
        sb.append(",emailStartDate: ");
        sb.append(this.emailStartDate);
        sb.append(",emailStartTime: ");
        sb.append(this.emailStartTime);
        sb.append(",emailEndTime: ");
        sb.append(this.emailEndTime);
        sb.append(",");
        sb.append(",protocolType: ");
        sb.append(this.protocolType);
        sb.append(",");
        sb.append(",emailThrouput: ");
        sb.append(this.emailThrouput);
        sb.append(",");
        return sb.toString();
    }
}
