package android.nfc;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class NdefMessage implements Parcelable {
    public static final Creator<NdefMessage> CREATOR = null;
    private final NdefRecord[] mRecords;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.nfc.NdefMessage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.nfc.NdefMessage.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.nfc.NdefMessage.<clinit>():void");
    }

    public NdefMessage(byte[] data) throws FormatException {
        if (data == null) {
            throw new NullPointerException("data is null");
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.mRecords = NdefRecord.parse(buffer, false);
        if (buffer.remaining() > 0) {
            throw new FormatException("trailing data");
        }
    }

    public NdefMessage(NdefRecord record, NdefRecord... records) {
        if (record == null) {
            throw new NullPointerException("record cannot be null");
        }
        for (NdefRecord r : records) {
            if (r == null) {
                throw new NullPointerException("record cannot be null");
            }
        }
        this.mRecords = new NdefRecord[(records.length + 1)];
        this.mRecords[0] = record;
        System.arraycopy(records, 0, this.mRecords, 1, records.length);
    }

    public NdefMessage(NdefRecord[] records) {
        if (records.length < 1) {
            throw new IllegalArgumentException("must have at least one record");
        }
        for (NdefRecord r : records) {
            if (r == null) {
                throw new NullPointerException("records cannot contain null");
            }
        }
        this.mRecords = records;
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
            this.mRecords[i].writeToByteBuffer(buffer, i == 0, i == this.mRecords.length + -1);
            i++;
        }
        return buffer.array();
    }

    public int describeContents() {
        return 0;
    }

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
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Arrays.equals(this.mRecords, ((NdefMessage) obj).mRecords);
    }

    public String toString() {
        return "NdefMessage " + Arrays.toString(this.mRecords);
    }
}
