package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILUICCAUTHAUTSTYPE {
    public String auts = new String();
    public int autsLen;
    public int autsPresent;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILUICCAUTHAUTSTYPE.class) {
            return false;
        }
        RILUICCAUTHAUTSTYPE other = (RILUICCAUTHAUTSTYPE) otherObject;
        return this.autsPresent == other.autsPresent && this.autsLen == other.autsLen && HidlSupport.deepEquals(this.auts, other.auts);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.autsPresent))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.autsLen))), Integer.valueOf(HidlSupport.deepHashCode(this.auts))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".autsPresent = ");
        builder.append(this.autsPresent);
        builder.append(", .autsLen = ");
        builder.append(this.autsLen);
        builder.append(", .auts = ");
        builder.append(this.auts);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<RILUICCAUTHAUTSTYPE> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILUICCAUTHAUTSTYPE> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILUICCAUTHAUTSTYPE _hidl_vec_element = new RILUICCAUTHAUTSTYPE();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.autsPresent = _hidl_blob.getInt32(_hidl_offset + 0);
        this.autsLen = _hidl_blob.getInt32(4 + _hidl_offset);
        this.auts = _hidl_blob.getString(_hidl_offset + 8);
        parcel.readEmbeddedBuffer((long) (this.auts.getBytes().length + 1), _hidl_blob.handle(), 0 + (_hidl_offset + 8), false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILUICCAUTHAUTSTYPE> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILUICCAUTHAUTSTYPE) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.autsPresent);
        _hidl_blob.putInt32(4 + _hidl_offset, this.autsLen);
        _hidl_blob.putString(8 + _hidl_offset, this.auts);
    }
}
