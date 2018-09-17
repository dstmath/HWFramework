package tmsdk.common.module.update;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdkobf.ib;

public class UpdateInfo extends ib implements Parcelable {
    public static final Creator<UpdateInfo> CREATOR = new Creator<UpdateInfo>() {
        /* renamed from: bI */
        public UpdateInfo[] newArray(int i) {
            return new UpdateInfo[i];
        }

        /* renamed from: o */
        public UpdateInfo createFromParcel(Parcel parcel) {
            return new UpdateInfo(parcel);
        }
    };
    public String checkSum = "";
    public Object data1;
    public Object data2;
    public String downNetName = "";
    public int downSize = -1;
    public byte downType;
    public int downnetType = 0;
    public int errorCode = 0;
    public String errorMsg = "";
    public String fileName;
    public int fileSize = 0;
    public long flag;
    public int mFileID = 0;
    public int rssi = -1;
    public int sdcardStatus = -1;
    public byte success = (byte) 1;
    public int timestamp = -1;
    public int type;
    public String url;

    public UpdateInfo(Parcel parcel) {
        readFromParcel(parcel);
    }

    private void readFromParcel(Parcel parcel) {
        this.id = parcel.readInt();
        this.flag = parcel.readLong();
        this.type = parcel.readInt();
        this.url = parcel.readString();
        this.fileName = parcel.readString();
        this.checkSum = parcel.readString();
        this.timestamp = parcel.readInt();
        this.success = (byte) parcel.readByte();
        this.downSize = parcel.readInt();
        this.downType = (byte) parcel.readByte();
        this.errorCode = parcel.readInt();
        this.downnetType = parcel.readInt();
        this.downNetName = parcel.readString();
        this.errorMsg = parcel.readString();
        this.rssi = parcel.readInt();
        this.sdcardStatus = parcel.readInt();
        this.fileSize = parcel.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.id);
        parcel.writeLong(this.flag);
        parcel.writeInt(this.type);
        parcel.writeString(this.url);
        parcel.writeString(this.fileName);
        parcel.writeString(this.checkSum);
        parcel.writeInt(this.timestamp);
        parcel.writeByte(this.success);
        parcel.writeInt(this.downSize);
        parcel.writeByte(this.downType);
        parcel.writeInt(this.errorCode);
        parcel.writeInt(this.downnetType);
        parcel.writeString(this.downNetName);
        parcel.writeString(this.errorMsg);
        parcel.writeInt(this.rssi);
        parcel.writeInt(this.sdcardStatus);
        parcel.writeInt(this.fileSize);
    }
}
