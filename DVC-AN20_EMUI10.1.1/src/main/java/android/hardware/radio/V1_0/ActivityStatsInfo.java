package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class ActivityStatsInfo {
    public int idleModeTimeMs;
    public int rxModeTimeMs;
    public int sleepModeTimeMs;
    public int[] txmModetimeMs = new int[5];

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != ActivityStatsInfo.class) {
            return false;
        }
        ActivityStatsInfo other = (ActivityStatsInfo) otherObject;
        if (this.sleepModeTimeMs == other.sleepModeTimeMs && this.idleModeTimeMs == other.idleModeTimeMs && HidlSupport.deepEquals(this.txmModetimeMs, other.txmModetimeMs) && this.rxModeTimeMs == other.rxModeTimeMs) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sleepModeTimeMs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.idleModeTimeMs))), Integer.valueOf(HidlSupport.deepHashCode(this.txmModetimeMs)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rxModeTimeMs))));
    }

    public final String toString() {
        return "{" + ".sleepModeTimeMs = " + this.sleepModeTimeMs + ", .idleModeTimeMs = " + this.idleModeTimeMs + ", .txmModetimeMs = " + Arrays.toString(this.txmModetimeMs) + ", .rxModeTimeMs = " + this.rxModeTimeMs + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<ActivityStatsInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<ActivityStatsInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ActivityStatsInfo _hidl_vec_element = new ActivityStatsInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.sleepModeTimeMs = _hidl_blob.getInt32(0 + _hidl_offset);
        this.idleModeTimeMs = _hidl_blob.getInt32(4 + _hidl_offset);
        _hidl_blob.copyToInt32Array(8 + _hidl_offset, this.txmModetimeMs, 5);
        this.rxModeTimeMs = _hidl_blob.getInt32(28 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<ActivityStatsInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.sleepModeTimeMs);
        _hidl_blob.putInt32(4 + _hidl_offset, this.idleModeTimeMs);
        long _hidl_array_offset_0 = 8 + _hidl_offset;
        int[] _hidl_array_item_0 = this.txmModetimeMs;
        if (_hidl_array_item_0 == null || _hidl_array_item_0.length != 5) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt32Array(_hidl_array_offset_0, _hidl_array_item_0);
        _hidl_blob.putInt32(28 + _hidl_offset, this.rxModeTimeMs);
    }
}
