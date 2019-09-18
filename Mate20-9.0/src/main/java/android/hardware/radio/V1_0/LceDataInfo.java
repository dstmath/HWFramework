package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class LceDataInfo {
    public byte confidenceLevel;
    public int lastHopCapacityKbps;
    public boolean lceSuspended;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != LceDataInfo.class) {
            return false;
        }
        LceDataInfo other = (LceDataInfo) otherObject;
        if (this.lastHopCapacityKbps == other.lastHopCapacityKbps && this.confidenceLevel == other.confidenceLevel && this.lceSuspended == other.lceSuspended) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.lastHopCapacityKbps))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.confidenceLevel))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.lceSuspended)))});
    }

    public final String toString() {
        return "{" + ".lastHopCapacityKbps = " + this.lastHopCapacityKbps + ", .confidenceLevel = " + this.confidenceLevel + ", .lceSuspended = " + this.lceSuspended + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(8), 0);
    }

    public static final ArrayList<LceDataInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<LceDataInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 8), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            LceDataInfo _hidl_vec_element = new LceDataInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 8));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.lastHopCapacityKbps = _hidl_blob.getInt32(0 + _hidl_offset);
        this.confidenceLevel = _hidl_blob.getInt8(4 + _hidl_offset);
        this.lceSuspended = _hidl_blob.getBool(5 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(8);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<LceDataInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 8);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 8));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.lastHopCapacityKbps);
        _hidl_blob.putInt8(4 + _hidl_offset, this.confidenceLevel);
        _hidl_blob.putBool(5 + _hidl_offset, this.lceSuspended);
    }
}
