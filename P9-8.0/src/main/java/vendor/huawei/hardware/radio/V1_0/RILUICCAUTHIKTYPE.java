package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILUICCAUTHIKTYPE {
    public String ik = new String();
    public int ikLen;
    public int ikPresent;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILUICCAUTHIKTYPE.class) {
            return false;
        }
        RILUICCAUTHIKTYPE other = (RILUICCAUTHIKTYPE) otherObject;
        return this.ikPresent == other.ikPresent && this.ikLen == other.ikLen && HidlSupport.deepEquals(this.ik, other.ik);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ikPresent))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ikLen))), Integer.valueOf(HidlSupport.deepHashCode(this.ik))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".ikPresent = ");
        builder.append(this.ikPresent);
        builder.append(", .ikLen = ");
        builder.append(this.ikLen);
        builder.append(", .ik = ");
        builder.append(this.ik);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<RILUICCAUTHIKTYPE> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILUICCAUTHIKTYPE> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILUICCAUTHIKTYPE _hidl_vec_element = new RILUICCAUTHIKTYPE();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.ikPresent = _hidl_blob.getInt32(_hidl_offset + 0);
        this.ikLen = _hidl_blob.getInt32(4 + _hidl_offset);
        this.ik = _hidl_blob.getString(_hidl_offset + 8);
        parcel.readEmbeddedBuffer((long) (this.ik.getBytes().length + 1), _hidl_blob.handle(), 0 + (_hidl_offset + 8), false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILUICCAUTHIKTYPE> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILUICCAUTHIKTYPE) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.ikPresent);
        _hidl_blob.putInt32(4 + _hidl_offset, this.ikLen);
        _hidl_blob.putString(8 + _hidl_offset, this.ik);
    }
}
