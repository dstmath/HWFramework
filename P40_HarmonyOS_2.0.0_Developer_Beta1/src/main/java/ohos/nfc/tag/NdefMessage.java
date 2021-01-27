package ohos.nfc.tag;

import java.nio.ByteBuffer;
import java.util.Arrays;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class NdefMessage implements Sequenceable {
    private MessageRecord[] mRecords;

    public NdefMessage(byte[] bArr) throws IllegalArgumentException {
        if (bArr != null) {
            ByteBuffer wrap = ByteBuffer.wrap(bArr);
            this.mRecords = MessageRecord.parse(wrap, false);
            if (wrap.remaining() > 0) {
                throw new IllegalArgumentException("trailing data");
            }
            return;
        }
        throw new NullPointerException("data is null");
    }

    public NdefMessage(MessageRecord[] messageRecordArr) {
        if (messageRecordArr == null || messageRecordArr.length < 1) {
            throw new IllegalArgumentException("records illegal when init NdefMessage");
        }
        for (MessageRecord messageRecord : messageRecordArr) {
            if (messageRecord == null) {
                throw new NullPointerException("record cannot be null");
            }
        }
        this.mRecords = new MessageRecord[messageRecordArr.length];
        System.arraycopy(messageRecordArr, 0, this.mRecords, 0, messageRecordArr.length);
    }

    public MessageRecord[] getRecords() {
        return this.mRecords;
    }

    public int getAllRecordsLength() {
        int i = 0;
        for (MessageRecord messageRecord : this.mRecords) {
            i += messageRecord.getAllRecordsLength();
        }
        return i;
    }

    public int hashCode() {
        return Arrays.hashCode(this.mRecords);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass() && (obj instanceof NdefMessage)) {
            return Arrays.equals(this.mRecords, ((NdefMessage) obj).mRecords);
        }
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        MessageRecord[] messageRecordArr = this.mRecords;
        if (messageRecordArr == null) {
            return true;
        }
        int length = messageRecordArr.length;
        parcel.writeInt(length);
        parcel.writeInt(length);
        for (int i = 0; i < length; i++) {
            parcel.writeSequenceable(this.mRecords[i]);
        }
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt < 0) {
            return true;
        }
        MessageRecord[] messageRecordArr = new MessageRecord[readInt];
        if (readInt == parcel.readInt()) {
            for (int i = 0; i < readInt; i++) {
                parcel.readSequenceable(this.mRecords[i]);
            }
            this.mRecords = messageRecordArr;
        }
        return true;
    }
}
