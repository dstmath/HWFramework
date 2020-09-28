package android.emcom;

import android.os.Parcel;
import android.os.Parcelable;

public class OnehopSendDataPara implements Parcelable {
    public static final Parcelable.Creator<OnehopSendDataPara> CREATOR = new Parcelable.Creator<OnehopSendDataPara>() {
        /* class android.emcom.OnehopSendDataPara.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OnehopSendDataPara createFromParcel(Parcel pa) {
            return new OnehopSendDataPara(pa);
        }

        @Override // android.os.Parcelable.Creator
        public OnehopSendDataPara[] newArray(int size) {
            return new OnehopSendDataPara[size];
        }
    };
    public byte[] data;
    public String deviceId;
    public boolean encrypt;
    public boolean hasContinue;
    public int len;
    public String moduleName;
    public String reservedInfo;
    public int type;

    public OnehopSendDataPara() {
    }

    public OnehopSendDataPara(Parcel pa) {
        this.deviceId = pa.readString();
        this.type = pa.readInt();
        boolean z = true;
        this.encrypt = pa.readByte() != 0;
        this.moduleName = pa.readString();
        this.data = pa.createByteArray();
        this.len = pa.readInt();
        this.hasContinue = pa.readByte() == 0 ? false : z;
        this.reservedInfo = pa.readString();
    }

    public void writeToParcel(Parcel pa, int flags) {
        pa.writeString(this.deviceId);
        pa.writeInt(this.type);
        pa.writeByte(this.encrypt ? (byte) 1 : 0);
        pa.writeString(this.moduleName);
        pa.writeByteArray(this.data);
        pa.writeInt(this.len);
        pa.writeByte(this.hasContinue ? (byte) 1 : 0);
        pa.writeString(this.reservedInfo);
    }

    public void readFromParcel(Parcel pa) {
        this.deviceId = pa.readString();
        this.type = pa.readInt();
        boolean z = true;
        this.encrypt = pa.readByte() != 0;
        this.moduleName = pa.readString();
        this.data = pa.createByteArray();
        this.len = pa.readInt();
        if (pa.readByte() == 0) {
            z = false;
        }
        this.hasContinue = z;
        this.reservedInfo = pa.readString();
    }

    public int describeContents() {
        return 0;
    }

    public final void copyFrom(OnehopSendDataPara sdp) {
        this.deviceId = sdp.deviceId;
        this.type = sdp.type;
        this.encrypt = sdp.encrypt;
        this.moduleName = sdp.moduleName;
        this.data = sdp.data;
        this.len = sdp.len;
        this.hasContinue = sdp.hasContinue;
        this.reservedInfo = sdp.reservedInfo;
    }

    public String toString() {
        return "hash: " + hashCode() + ",type: " + this.type + ",encrypt: " + this.encrypt + ",moduleName: " + this.moduleName + ",len: " + this.len + ",hasContinue: " + this.hasContinue;
    }

    public void recycle() {
        this.deviceId = "";
        this.type = 0;
        this.encrypt = false;
        this.moduleName = null;
        this.data = null;
        this.len = 0;
        this.hasContinue = false;
        this.reservedInfo = "";
    }
}
