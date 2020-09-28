package android.hardware.thermal.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CpuUsage {
    public long active;
    public boolean isOnline;
    public String name = new String();
    public long total;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CpuUsage.class) {
            return false;
        }
        CpuUsage other = (CpuUsage) otherObject;
        if (HidlSupport.deepEquals(this.name, other.name) && this.active == other.active && this.total == other.total && this.isOnline == other.isOnline) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.name)), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.active))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.total))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isOnline))));
    }

    public final String toString() {
        return "{" + ".name = " + this.name + ", .active = " + this.active + ", .total = " + this.total + ", .isOnline = " + this.isOnline + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(40), 0);
    }

    public static final ArrayList<CpuUsage> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CpuUsage> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CpuUsage _hidl_vec_element = new CpuUsage();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.name = _hidl_blob.getString(_hidl_offset + 0);
        parcel.readEmbeddedBuffer((long) (this.name.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, false);
        this.active = _hidl_blob.getInt64(16 + _hidl_offset);
        this.total = _hidl_blob.getInt64(24 + _hidl_offset);
        this.isOnline = _hidl_blob.getBool(32 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(40);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CpuUsage> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 40);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 40));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(0 + _hidl_offset, this.name);
        _hidl_blob.putInt64(16 + _hidl_offset, this.active);
        _hidl_blob.putInt64(24 + _hidl_offset, this.total);
        _hidl_blob.putBool(32 + _hidl_offset, this.isOnline);
    }
}
