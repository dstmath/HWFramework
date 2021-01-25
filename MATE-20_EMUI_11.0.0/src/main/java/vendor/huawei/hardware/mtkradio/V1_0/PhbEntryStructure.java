package vendor.huawei.hardware.mtkradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class PhbEntryStructure {
    public String alphaId = new String();
    public int index;
    public String number = new String();
    public int ton;
    public int type;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != PhbEntryStructure.class) {
            return false;
        }
        PhbEntryStructure other = (PhbEntryStructure) otherObject;
        if (this.type == other.type && this.index == other.index && HidlSupport.deepEquals(this.number, other.number) && this.ton == other.ton && HidlSupport.deepEquals(this.alphaId, other.alphaId)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.type))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.index))), Integer.valueOf(HidlSupport.deepHashCode(this.number)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ton))), Integer.valueOf(HidlSupport.deepHashCode(this.alphaId)));
    }

    public final String toString() {
        return "{.type = " + this.type + ", .index = " + this.index + ", .number = " + this.number + ", .ton = " + this.ton + ", .alphaId = " + this.alphaId + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<PhbEntryStructure> readVectorFromParcel(HwParcel parcel) {
        ArrayList<PhbEntryStructure> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            PhbEntryStructure _hidl_vec_element = new PhbEntryStructure();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.type = _hidl_blob.getInt32(_hidl_offset + 0);
        this.index = _hidl_blob.getInt32(_hidl_offset + 4);
        this.number = _hidl_blob.getString(_hidl_offset + 8);
        parcel.readEmbeddedBuffer((long) (this.number.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 8 + 0, false);
        this.ton = _hidl_blob.getInt32(_hidl_offset + 24);
        this.alphaId = _hidl_blob.getString(_hidl_offset + 32);
        parcel.readEmbeddedBuffer((long) (this.alphaId.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<PhbEntryStructure> _hidl_vec) {
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
        _hidl_blob.putInt32(0 + _hidl_offset, this.type);
        _hidl_blob.putInt32(4 + _hidl_offset, this.index);
        _hidl_blob.putString(8 + _hidl_offset, this.number);
        _hidl_blob.putInt32(24 + _hidl_offset, this.ton);
        _hidl_blob.putString(32 + _hidl_offset, this.alphaId);
    }
}
