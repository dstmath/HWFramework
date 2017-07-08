package tmsdk.common.module.update;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdkobf.jf;

/* compiled from: Unknown */
public class UpdateInfo extends jf implements Parcelable {
    public static final Creator<UpdateInfo> CREATOR = null;
    public String checkSum;
    public Object data1;
    public Object data2;
    public String downNetName;
    public int downSize;
    public byte downType;
    public int downnetType;
    public int errorCode;
    public String errorMsg;
    public String fileName;
    public int fileSize;
    public long flag;
    public int rssi;
    public int sdcardStatus;
    public byte success;
    public int timestamp;
    public int type;
    public String url;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.update.UpdateInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.update.UpdateInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.update.UpdateInfo.<clinit>():void");
    }

    public UpdateInfo() {
        this.checkSum = "";
        this.timestamp = -1;
        this.success = (byte) 1;
        this.downSize = -1;
        this.errorCode = 0;
        this.downnetType = 0;
        this.downNetName = "";
        this.errorMsg = "";
        this.rssi = -1;
        this.sdcardStatus = -1;
        this.fileSize = 0;
    }

    public UpdateInfo(Parcel parcel) {
        this.checkSum = "";
        this.timestamp = -1;
        this.success = (byte) 1;
        this.downSize = -1;
        this.errorCode = 0;
        this.downnetType = 0;
        this.downNetName = "";
        this.errorMsg = "";
        this.rssi = -1;
        this.sdcardStatus = -1;
        this.fileSize = 0;
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
