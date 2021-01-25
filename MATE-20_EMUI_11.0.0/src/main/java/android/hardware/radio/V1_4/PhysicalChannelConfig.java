package android.hardware.radio.V1_4;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class PhysicalChannelConfig {
    public android.hardware.radio.V1_2.PhysicalChannelConfig base = new android.hardware.radio.V1_2.PhysicalChannelConfig();
    public ArrayList<Integer> contextIds = new ArrayList<>();
    public int physicalCellId;
    public int rat;
    public RadioFrequencyInfo rfInfo = new RadioFrequencyInfo();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != PhysicalChannelConfig.class) {
            return false;
        }
        PhysicalChannelConfig other = (PhysicalChannelConfig) otherObject;
        if (HidlSupport.deepEquals(this.base, other.base) && this.rat == other.rat && HidlSupport.deepEquals(this.rfInfo, other.rfInfo) && HidlSupport.deepEquals(this.contextIds, other.contextIds) && this.physicalCellId == other.physicalCellId) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.base)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rat))), Integer.valueOf(HidlSupport.deepHashCode(this.rfInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.contextIds)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.physicalCellId))));
    }

    public final String toString() {
        return "{.base = " + this.base + ", .rat = " + RadioTechnology.toString(this.rat) + ", .rfInfo = " + this.rfInfo + ", .contextIds = " + this.contextIds + ", .physicalCellId = " + this.physicalCellId + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<PhysicalChannelConfig> readVectorFromParcel(HwParcel parcel) {
        ArrayList<PhysicalChannelConfig> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            PhysicalChannelConfig _hidl_vec_element = new PhysicalChannelConfig();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.base.readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_offset + 0);
        this.rat = _hidl_blob.getInt32(_hidl_offset + 8);
        this.rfInfo.readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_offset + 12);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 24 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 4), _hidl_blob.handle(), _hidl_offset + 24 + 0, true);
        this.contextIds.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.contextIds.add(Integer.valueOf(childBlob.getInt32((long) (_hidl_index_0 * 4))));
        }
        this.physicalCellId = _hidl_blob.getInt32(_hidl_offset + 40);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<PhysicalChannelConfig> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        this.base.writeEmbeddedToBlob(_hidl_blob, _hidl_offset + 0);
        _hidl_blob.putInt32(_hidl_offset + 8, this.rat);
        this.rfInfo.writeEmbeddedToBlob(_hidl_blob, _hidl_offset + 12);
        int _hidl_vec_size = this.contextIds.size();
        _hidl_blob.putInt32(_hidl_offset + 24 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 24 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 4);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt32((long) (_hidl_index_0 * 4), this.contextIds.get(_hidl_index_0).intValue());
        }
        _hidl_blob.putBlob(24 + _hidl_offset + 0, childBlob);
        _hidl_blob.putInt32(40 + _hidl_offset, this.physicalCellId);
    }
}
