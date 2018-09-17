package android.bluetooth.le;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public final class ScanResult implements Parcelable {
    public static final Creator<ScanResult> CREATOR = null;
    private BluetoothDevice mDevice;
    private int mRssi;
    private ScanRecord mScanRecord;
    private long mTimestampNanos;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.bluetooth.le.ScanResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.bluetooth.le.ScanResult.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.ScanResult.<clinit>():void");
    }

    public ScanResult(BluetoothDevice device, ScanRecord scanRecord, int rssi, long timestampNanos) {
        this.mDevice = device;
        this.mScanRecord = scanRecord;
        this.mRssi = rssi;
        this.mTimestampNanos = timestampNanos;
    }

    private ScanResult(Parcel in) {
        readFromParcel(in);
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (this.mDevice != null) {
            dest.writeInt(1);
            this.mDevice.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        if (this.mScanRecord != null) {
            dest.writeInt(1);
            dest.writeByteArray(this.mScanRecord.getBytes());
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.mRssi);
        dest.writeLong(this.mTimestampNanos);
    }

    private void readFromParcel(Parcel in) {
        if (in.readInt() == 1) {
            this.mDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(in);
        }
        if (in.readInt() == 1) {
            this.mScanRecord = ScanRecord.parseFromBytes(in.createByteArray());
        }
        this.mRssi = in.readInt();
        this.mTimestampNanos = in.readLong();
    }

    public int describeContents() {
        return 0;
    }

    public BluetoothDevice getDevice() {
        return this.mDevice;
    }

    public ScanRecord getScanRecord() {
        return this.mScanRecord;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public long getTimestampNanos() {
        return this.mTimestampNanos;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mDevice, Integer.valueOf(this.mRssi), this.mScanRecord, Long.valueOf(this.mTimestampNanos)});
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ScanResult other = (ScanResult) obj;
        if (!Objects.equals(this.mDevice, other.mDevice) || this.mRssi != other.mRssi || !Objects.equals(this.mScanRecord, other.mScanRecord)) {
            z = false;
        } else if (this.mTimestampNanos != other.mTimestampNanos) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return "ScanResult{mDevice=" + this.mDevice + ", mScanRecord=" + Objects.toString(this.mScanRecord) + ", mRssi=" + this.mRssi + ", mTimestampNanos=" + this.mTimestampNanos + '}';
    }
}
