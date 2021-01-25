package android.nfc;

import android.os.Parcel;
import android.os.Parcelable;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class NdefMessage implements Parcelable {
    public static final Parcelable.Creator<NdefMessage> CREATOR = new Parcelable.Creator<NdefMessage>() {
        /* class android.nfc.NdefMessage.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NdefMessage createFromParcel(Parcel in) {
            NdefRecord[] records = new NdefRecord[in.readInt()];
            in.readTypedArray(records, NdefRecord.CREATOR);
            return new NdefMessage(records);
        }

        @Override // android.os.Parcelable.Creator
        public NdefMessage[] newArray(int size) {
            return new NdefMessage[size];
        }
    };
    private final NdefRecord[] mRecords;

    public NdefMessage(byte[] data) throws FormatException {
        if (data != null) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            this.mRecords = NdefRecord.parse(buffer, false);
            if (buffer.remaining() > 0) {
                throw new FormatException("trailing data");
            }
            return;
        }
        throw new NullPointerException("data is null");
    }

    public NdefMessage(NdefRecord record, NdefRecord... records) {
        if (record != null) {
            for (NdefRecord r : records) {
                if (r == null) {
                    throw new NullPointerException("record cannot be null");
                }
            }
            this.mRecords = new NdefRecord[(records.length + 1)];
            NdefRecord[] ndefRecordArr = this.mRecords;
            ndefRecordArr[0] = record;
            System.arraycopy(records, 0, ndefRecordArr, 1, records.length);
            return;
        }
        throw new NullPointerException("record cannot be null");
    }

    public NdefMessage(NdefRecord[] records) {
        if (records.length >= 1) {
            for (NdefRecord r : records) {
                if (r == null) {
                    throw new NullPointerException("records cannot contain null");
                }
            }
            this.mRecords = records;
            return;
        }
        throw new IllegalArgumentException("must have at least one record");
    }

    public NdefRecord[] getRecords() {
        return this.mRecords;
    }

    public int getByteArrayLength() {
        int length = 0;
        for (NdefRecord r : this.mRecords) {
            length += r.getByteLength();
        }
        return length;
    }

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(getByteArrayLength());
        int i = 0;
        while (i < this.mRecords.length) {
            boolean me = false;
            boolean mb = i == 0;
            if (i == this.mRecords.length - 1) {
                me = true;
            }
            this.mRecords[i].writeToByteBuffer(buffer, mb, me);
            i++;
        }
        return buffer.array();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRecords.length);
        dest.writeTypedArray(this.mRecords, flags);
    }

    public int hashCode() {
        return Arrays.hashCode(this.mRecords);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            return Arrays.equals(this.mRecords, ((NdefMessage) obj).mRecords);
        }
        return false;
    }

    public String toString() {
        return "NdefMessage " + Arrays.toString(this.mRecords);
    }
}
