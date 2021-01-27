package android.emcom;

import android.emcom.SmartcareInfos;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.android.os.storage.StorageManagerExt;

public class EmailInfo extends SmartcareInfos.SmartcareBaseInfo implements Parcelable {
    private static final int AND_DEFAULT = 255;
    public static final Parcelable.Creator<EmailInfo> CREATOR = new Parcelable.Creator<EmailInfo>() {
        /* class android.emcom.EmailInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EmailInfo createFromParcel(Parcel pa) {
            return new EmailInfo(pa);
        }

        @Override // android.os.Parcelable.Creator
        public EmailInfo[] newArray(int size) {
            if (size > 0) {
                return new EmailInfo[size];
            }
            return new EmailInfo[0];
        }
    };
    private static final byte FLAG_DEFAULT = 1;
    private static final int LATENCY_DEFAULT = -1;
    private static final int THROUGHOUT_DEFAULT = -1;
    private static final byte TYPE_DEFAULT = -1;
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
        if (pa != null) {
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
    }

    @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
    public void addToInfos(SmartcareInfos sci) {
        if (sci != null) {
            super.addToInfos(sci);
            sci.emailInfo = this;
        }
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel pa, int flags) {
        if (pa != null) {
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
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel pa) {
        if (pa != null) {
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
    }

    public final void copyFrom(EmailInfo emailInfo) {
        if (emailInfo != null) {
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
    }

    @Override // java.lang.Object
    public String toString() {
        return "hash: " + hashCode() + ",app: " + this.appName + ",type: " + ((int) this.type) + ",latency: " + this.latency + ",successFlag: " + ((int) this.successFlag) + ",abnormalDroppingFlag: " + ((int) this.abnormalDroppingFlag) + ",emailStartDate: " + this.emailStartDate + ",emailStartTime: " + this.emailStartTime + ",emailEndTime: " + this.emailEndTime + ",,protocolType: " + ((int) this.protocolType) + ",,emailThrouput: " + this.emailThrouput + ",";
    }

    @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
    public void recycle() {
        super.recycle();
        this.appName = StorageManagerExt.INVALID_KEY_DESC;
        this.type = TYPE_DEFAULT;
        this.latency = -1;
        this.successFlag = FLAG_DEFAULT;
        this.abnormalDroppingFlag = 0;
        this.hostName = StorageManagerExt.INVALID_KEY_DESC;
        this.emailStartDate = 0;
        this.emailStartTime = 0;
        this.emailEndTime = 0;
        this.protocolType = TYPE_DEFAULT;
        this.emailThrouput = -1;
    }
}
