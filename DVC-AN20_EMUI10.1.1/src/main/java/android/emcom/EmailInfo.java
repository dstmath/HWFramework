package android.emcom;

import android.emcom.SmartcareInfos;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.uikit.effect.BuildConfig;

public class EmailInfo extends SmartcareInfos.SmartcareBaseInfo implements Parcelable {
    public static final Parcelable.Creator<EmailInfo> CREATOR = new Parcelable.Creator<EmailInfo>() {
        /* class android.emcom.EmailInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EmailInfo createFromParcel(Parcel pa) {
            return new EmailInfo(pa);
        }

        @Override // android.os.Parcelable.Creator
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

    public EmailInfo() {
    }

    public EmailInfo(Parcel pa) {
        this.appName = pa.readString();
        this.type = (byte) (pa.readInt() & 255);
        this.latency = pa.readInt();
        this.successFlag = (byte) (pa.readInt() & 255);
        this.abnormalDroppingFlag = (byte) (pa.readInt() & 255);
        this.hostName = pa.readString();
        this.emailStartDate = pa.readInt();
        this.emailStartTime = pa.readInt();
        this.emailEndTime = pa.readInt();
        this.protocolType = (byte) (pa.readInt() & 255);
        this.emailThrouput = pa.readInt();
    }

    @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
    public void addToInfos(SmartcareInfos sci) {
        super.addToInfos(sci);
        sci.emailInfo = this;
    }

    public void writeToParcel(Parcel pa, int flags) {
        pa.writeString(this.appName);
        pa.writeInt(this.type);
        pa.writeInt(this.latency);
        pa.writeInt(this.successFlag);
        pa.writeInt(this.abnormalDroppingFlag);
        pa.writeString(this.hostName);
        pa.writeInt(this.emailStartDate);
        pa.writeInt(this.emailStartTime);
        pa.writeInt(this.emailEndTime);
        pa.writeInt(this.protocolType);
        pa.writeInt(this.emailThrouput);
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel pa) {
        this.appName = pa.readString();
        this.type = (byte) (pa.readInt() & 255);
        this.latency = (short) pa.readInt();
        this.successFlag = (byte) (pa.readInt() & 255);
        this.abnormalDroppingFlag = (byte) (pa.readInt() & 255);
        this.hostName = pa.readString();
        this.emailStartDate = pa.readInt();
        this.emailStartTime = pa.readInt();
        this.emailEndTime = pa.readInt();
        this.protocolType = (byte) (pa.readInt() & 255);
        this.emailThrouput = pa.readInt();
    }

    public final void copyFrom(EmailInfo emailInfo) {
        this.appName = emailInfo.appName;
        this.type = emailInfo.type;
        this.latency = emailInfo.latency;
        this.successFlag = emailInfo.successFlag;
        this.abnormalDroppingFlag = emailInfo.abnormalDroppingFlag;
        this.hostName = emailInfo.hostName;
        this.emailStartDate = emailInfo.emailStartDate;
        this.emailStartTime = emailInfo.emailStartTime;
        this.emailEndTime = emailInfo.emailEndTime;
        this.protocolType = emailInfo.protocolType;
        this.emailThrouput = emailInfo.emailThrouput;
    }

    public String toString() {
        return "hash: " + hashCode() + ",app: " + this.appName + ",type: " + ((int) this.type) + ",latency: " + this.latency + ",successFlag: " + ((int) this.successFlag) + ",abnormalDroppingFlag: " + ((int) this.abnormalDroppingFlag) + ",emailStartDate: " + this.emailStartDate + ",emailStartTime: " + this.emailStartTime + ",emailEndTime: " + this.emailEndTime + "," + ",protocolType: " + ((int) this.protocolType) + "," + ",emailThrouput: " + this.emailThrouput + ",";
    }

    @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
    public void recycle() {
        super.recycle();
        this.appName = BuildConfig.FLAVOR;
        this.type = -1;
        this.latency = -1;
        this.successFlag = 1;
        this.abnormalDroppingFlag = 0;
        this.hostName = BuildConfig.FLAVOR;
        this.emailStartDate = 0;
        this.emailStartTime = 0;
        this.emailEndTime = 0;
        this.protocolType = -1;
        this.emailThrouput = -1;
    }
}
