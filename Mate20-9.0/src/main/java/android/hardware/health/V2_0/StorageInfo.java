package android.hardware.health.V2_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class StorageInfo {
    public final StorageAttribute attr = new StorageAttribute();
    public short eol;
    public short lifetimeA;
    public short lifetimeB;
    public String version = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != StorageInfo.class) {
            return false;
        }
        StorageInfo other = (StorageInfo) otherObject;
        if (HidlSupport.deepEquals(this.attr, other.attr) && this.eol == other.eol && this.lifetimeA == other.lifetimeA && this.lifetimeB == other.lifetimeB && HidlSupport.deepEquals(this.version, other.version)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.attr)), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.eol))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.lifetimeA))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.lifetimeB))), Integer.valueOf(HidlSupport.deepHashCode(this.version))});
    }

    public final String toString() {
        return "{" + ".attr = " + this.attr + ", .eol = " + this.eol + ", .lifetimeA = " + this.lifetimeA + ", .lifetimeB = " + this.lifetimeB + ", .version = " + this.version + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<StorageInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<StorageInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            StorageInfo _hidl_vec_element = new StorageInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        HwParcel hwParcel = parcel;
        this.attr.readEmbeddedFromParcel(hwParcel, hwBlob, _hidl_offset + 0);
        this.eol = hwBlob.getInt16(_hidl_offset + 24);
        this.lifetimeA = hwBlob.getInt16(_hidl_offset + 26);
        this.lifetimeB = hwBlob.getInt16(_hidl_offset + 28);
        this.version = hwBlob.getString(_hidl_offset + 32);
        hwParcel.readEmbeddedBuffer((long) (this.version.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<StorageInfo> _hidl_vec) {
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
        this.attr.writeEmbeddedToBlob(_hidl_blob, 0 + _hidl_offset);
        _hidl_blob.putInt16(24 + _hidl_offset, this.eol);
        _hidl_blob.putInt16(26 + _hidl_offset, this.lifetimeA);
        _hidl_blob.putInt16(28 + _hidl_offset, this.lifetimeB);
        _hidl_blob.putString(32 + _hidl_offset, this.version);
    }
}
