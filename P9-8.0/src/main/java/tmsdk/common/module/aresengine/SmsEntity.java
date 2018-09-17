package tmsdk.common.module.aresengine;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SmsEntity extends TelephonyEntity implements Parcelable {
    public static final Creator<SmsEntity> CREATOR = new Creator<SmsEntity>() {
        /* renamed from: aR */
        public SmsEntity[] newArray(int i) {
            return new SmsEntity[i];
        }

        /* renamed from: f */
        public SmsEntity createFromParcel(Parcel parcel) {
            return new SmsEntity(parcel);
        }
    };
    public static final int PROTOCOL_TYPE_MMS = 1;
    public static final int PROTOCOL_TYPE_SMS = 0;
    public static final int PROTOCOL_TYPE_WAPPUSH = 2;
    public static final int SMS_TYPE_IN = 1;
    public static final int SMS_TYPE_SENT = 2;
    public String body;
    public long date;
    public String discriminator;
    public int errorCode;
    public String fromCard;
    public int locked;
    public int messageBox;
    public byte mmsDeliveryReport;
    public int mmsErrorType;
    public long mmsExpiry;
    public byte mmsLocked;
    public byte mmsMessageBox;
    public int mmsMessageSize;
    public int mmsMessageType;
    public byte mmsRead;
    public byte mmsReadReport;
    public String mmsSubject;
    public int mmsSubjectCharset;
    public long msgId;
    public long person;
    public int protocolType;
    public transient Intent raw;
    public int read;
    public int replyPathPresent;
    public boolean seen;
    public String serviceCenter;
    public int status;
    public String subject;
    public long threadId;
    public int type;

    public SmsEntity() {
        this.read = 0;
        this.protocolType = 0;
        this.date = System.currentTimeMillis();
    }

    protected SmsEntity(Parcel parcel) {
        this.id = parcel.readInt();
        this.phonenum = parcel.readString();
        this.name = parcel.readString();
        this.body = parcel.readString();
        this.date = parcel.readLong();
        this.type = parcel.readInt();
        this.protocolType = parcel.readInt();
        this.read = parcel.readInt();
        this.fromCard = parcel.readString();
        this.raw = (Intent) parcel.readParcelable(Intent.class.getClassLoader());
        this.threadId = parcel.readLong();
        this.msgId = parcel.readLong();
        this.person = parcel.readLong();
        this.status = parcel.readInt();
        this.replyPathPresent = parcel.readInt();
        this.subject = parcel.readString();
        this.serviceCenter = parcel.readString();
        this.locked = parcel.readInt();
        this.errorCode = parcel.readInt();
        this.seen = parcel.readInt() == 1;
        this.messageBox = parcel.readInt();
        this.discriminator = parcel.readString();
        this.mmsSubject = parcel.readString();
        this.mmsSubjectCharset = parcel.readInt();
        this.mmsMessageSize = parcel.readInt();
        this.mmsExpiry = parcel.readLong();
        this.mmsMessageType = parcel.readInt();
        this.mmsMessageBox = (byte) parcel.readByte();
        this.mmsDeliveryReport = (byte) parcel.readByte();
        this.mmsReadReport = (byte) parcel.readByte();
        this.mmsRead = (byte) parcel.readByte();
        this.mmsErrorType = parcel.readInt();
        this.mmsLocked = (byte) parcel.readByte();
    }

    public SmsEntity(SmsEntity smsEntity) {
        super(smsEntity);
        this.body = smsEntity.body;
        this.date = smsEntity.date;
        this.type = smsEntity.type;
        this.protocolType = smsEntity.protocolType;
        this.read = smsEntity.read;
        this.raw = smsEntity.raw;
        this.fromCard = smsEntity.fromCard;
        this.threadId = smsEntity.threadId;
        this.msgId = smsEntity.msgId;
        this.person = smsEntity.person;
        this.status = smsEntity.status;
        this.replyPathPresent = smsEntity.replyPathPresent;
        this.subject = smsEntity.subject;
        this.serviceCenter = smsEntity.serviceCenter;
        this.locked = smsEntity.locked;
        this.errorCode = smsEntity.errorCode;
        this.seen = smsEntity.seen;
        this.messageBox = smsEntity.messageBox;
        this.discriminator = smsEntity.discriminator;
        this.mmsSubject = smsEntity.mmsSubject;
        this.mmsSubjectCharset = smsEntity.mmsSubjectCharset;
        this.mmsMessageSize = smsEntity.mmsMessageSize;
        this.mmsExpiry = smsEntity.mmsExpiry;
        this.mmsMessageType = smsEntity.mmsMessageType;
        this.mmsMessageBox = (byte) smsEntity.mmsMessageBox;
        this.mmsDeliveryReport = (byte) smsEntity.mmsDeliveryReport;
        this.mmsReadReport = (byte) smsEntity.mmsReadReport;
        this.mmsRead = (byte) smsEntity.mmsRead;
        this.mmsErrorType = smsEntity.mmsErrorType;
        this.mmsLocked = (byte) smsEntity.mmsLocked;
    }

    public static byte[] marshall(SmsEntity smsEntity) {
        Parcel obtain = Parcel.obtain();
        smsEntity.writeToParcel(obtain, 0);
        byte[] marshall = obtain.marshall();
        obtain.recycle();
        return marshall;
    }

    public static SmsEntity unmarshall(byte[] bArr) {
        Parcel obtain = Parcel.obtain();
        obtain.unmarshall(bArr, 0, bArr.length);
        obtain.setDataPosition(0);
        SmsEntity smsEntity = new SmsEntity(obtain);
        obtain.recycle();
        return smsEntity;
    }

    public int describeContents() {
        return 0;
    }

    public String getAddress() {
        return this.phonenum;
    }

    public String getBody() {
        return this.body;
    }

    public long getDate() {
        return this.date;
    }

    public String getDiscriminator() {
        return this.discriminator;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getFromSimVaule() {
        return this.fromCard;
    }

    public long getId() {
        return (long) this.id;
    }

    public int getLocked() {
        return this.locked;
    }

    public byte[] getMmsContentLocation() {
        return null;
    }

    public short getMmsDeliveryReport() {
        return (short) this.mmsDeliveryReport;
    }

    public int getMmsErrorType() {
        return this.mmsErrorType;
    }

    public long getMmsExpiry() {
        return this.mmsExpiry;
    }

    public byte getMmsLocked() {
        return this.mmsLocked;
    }

    public short getMmsMessageBox() {
        return (short) this.mmsMessageBox;
    }

    public int getMmsMessageSize() {
        return this.mmsMessageSize;
    }

    public short getMmsMessageType() {
        return (short) this.mmsMessageType;
    }

    public byte getMmsRead() {
        return this.mmsRead;
    }

    public short getMmsReadReport() {
        return (short) this.mmsReadReport;
    }

    public String getMmsSubject() {
        return this.mmsSubject;
    }

    public int getMmsSubjectCharset() {
        return this.mmsSubjectCharset;
    }

    public byte[] getMmsTransactionId() {
        return null;
    }

    public long getMsgId() {
        return this.msgId;
    }

    public int getMsgbox() {
        return this.messageBox;
    }

    public long getPerson() {
        return this.person;
    }

    public int getProtocol() {
        return this.protocolType;
    }

    public int getRead() {
        return this.read;
    }

    public int getReplyPathPresent() {
        return this.replyPathPresent;
    }

    public String getServiceCenter() {
        return this.serviceCenter;
    }

    public int getStatus() {
        return this.status;
    }

    public String getSubject() {
        return this.subject;
    }

    public long getThreadId() {
        return this.threadId;
    }

    public int getType() {
        return this.type;
    }

    public boolean isSeen() {
        return this.seen;
    }

    public void setAddress(String str) {
        this.phonenum = str;
    }

    public void setBody(String str) {
        if (str == null) {
            str = "";
        }
        this.body = str;
    }

    public void setSeen(int i) {
        boolean z = true;
        if (i != 1) {
            z = false;
        }
        this.seen = z;
    }

    public void setStatus(int i) {
        this.status = i;
    }

    public void setThreadId(long j) {
        this.threadId = j;
    }

    public void writeToParcel(Parcel parcel, int i) {
        int i2 = 0;
        parcel.writeInt(this.id);
        parcel.writeString(this.phonenum);
        parcel.writeString(this.name);
        parcel.writeString(this.body);
        parcel.writeLong(this.date);
        parcel.writeInt(this.type);
        parcel.writeInt(this.protocolType);
        parcel.writeInt(this.read);
        parcel.writeString(this.fromCard);
        parcel.writeParcelable(this.raw, 0);
        parcel.writeLong(this.threadId);
        parcel.writeLong(this.msgId);
        parcel.writeLong(this.person);
        parcel.writeInt(this.status);
        parcel.writeInt(this.replyPathPresent);
        parcel.writeString(this.subject);
        parcel.writeString(this.serviceCenter);
        parcel.writeInt(this.locked);
        parcel.writeInt(this.errorCode);
        if (this.seen) {
            i2 = 1;
        }
        parcel.writeInt(i2);
        parcel.writeInt(this.messageBox);
        parcel.writeString(this.discriminator);
        parcel.writeString(this.mmsSubject);
        parcel.writeInt(this.mmsSubjectCharset);
        parcel.writeInt(this.mmsMessageSize);
        parcel.writeLong(this.mmsExpiry);
        parcel.writeInt(this.mmsMessageType);
        parcel.writeByte(this.mmsMessageBox);
        parcel.writeByte(this.mmsDeliveryReport);
        parcel.writeByte(this.mmsReadReport);
        parcel.writeByte(this.mmsRead);
        parcel.writeInt(this.mmsErrorType);
        parcel.writeByte(this.mmsLocked);
    }
}
