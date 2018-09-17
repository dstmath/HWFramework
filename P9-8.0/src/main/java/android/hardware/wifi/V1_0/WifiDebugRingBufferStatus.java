package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class WifiDebugRingBufferStatus {
    public int flags;
    public int freeSizeInBytes;
    public int ringId;
    public String ringName = new String();
    public int sizeInBytes;
    public int verboseLevel;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != WifiDebugRingBufferStatus.class) {
            return false;
        }
        WifiDebugRingBufferStatus other = (WifiDebugRingBufferStatus) otherObject;
        return HidlSupport.deepEquals(this.ringName, other.ringName) && this.flags == other.flags && this.ringId == other.ringId && this.sizeInBytes == other.sizeInBytes && this.freeSizeInBytes == other.freeSizeInBytes && this.verboseLevel == other.verboseLevel;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.ringName)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.flags))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ringId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sizeInBytes))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.freeSizeInBytes))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.verboseLevel)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".ringName = ");
        builder.append(this.ringName);
        builder.append(", .flags = ");
        builder.append(this.flags);
        builder.append(", .ringId = ");
        builder.append(this.ringId);
        builder.append(", .sizeInBytes = ");
        builder.append(this.sizeInBytes);
        builder.append(", .freeSizeInBytes = ");
        builder.append(this.freeSizeInBytes);
        builder.append(", .verboseLevel = ");
        builder.append(this.verboseLevel);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(40), 0);
    }

    public static final ArrayList<WifiDebugRingBufferStatus> readVectorFromParcel(HwParcel parcel) {
        ArrayList<WifiDebugRingBufferStatus> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            WifiDebugRingBufferStatus _hidl_vec_element = new WifiDebugRingBufferStatus();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.ringName = _hidl_blob.getString(_hidl_offset + 0);
        parcel.readEmbeddedBuffer((long) (this.ringName.getBytes().length + 1), _hidl_blob.handle(), 0 + (_hidl_offset + 0), false);
        this.flags = _hidl_blob.getInt32(16 + _hidl_offset);
        this.ringId = _hidl_blob.getInt32(20 + _hidl_offset);
        this.sizeInBytes = _hidl_blob.getInt32(24 + _hidl_offset);
        this.freeSizeInBytes = _hidl_blob.getInt32(28 + _hidl_offset);
        this.verboseLevel = _hidl_blob.getInt32(32 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(40);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<WifiDebugRingBufferStatus> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 40);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((WifiDebugRingBufferStatus) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 40));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(0 + _hidl_offset, this.ringName);
        _hidl_blob.putInt32(16 + _hidl_offset, this.flags);
        _hidl_blob.putInt32(20 + _hidl_offset, this.ringId);
        _hidl_blob.putInt32(24 + _hidl_offset, this.sizeInBytes);
        _hidl_blob.putInt32(28 + _hidl_offset, this.freeSizeInBytes);
        _hidl_blob.putInt32(32 + _hidl_offset, this.verboseLevel);
    }
}
