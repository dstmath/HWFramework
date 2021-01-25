package vendor.huawei.hardware.qcomradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILUICCKSNAFAUTHPARAMS {
    public String impi = new String();
    public int impiLen;
    public String nafId = new String();
    public int nafLen;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILUICCKSNAFAUTHPARAMS.class) {
            return false;
        }
        RILUICCKSNAFAUTHPARAMS other = (RILUICCKSNAFAUTHPARAMS) otherObject;
        if (this.nafLen == other.nafLen && HidlSupport.deepEquals(this.nafId, other.nafId) && this.impiLen == other.impiLen && HidlSupport.deepEquals(this.impi, other.impi)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.nafLen))), Integer.valueOf(HidlSupport.deepHashCode(this.nafId)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.impiLen))), Integer.valueOf(HidlSupport.deepHashCode(this.impi)));
    }

    public final String toString() {
        return "{.nafLen = " + this.nafLen + ", .nafId = " + this.nafId + ", .impiLen = " + this.impiLen + ", .impi = " + this.impi + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<RILUICCKSNAFAUTHPARAMS> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILUICCKSNAFAUTHPARAMS> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILUICCKSNAFAUTHPARAMS _hidl_vec_element = new RILUICCKSNAFAUTHPARAMS();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.nafLen = _hidl_blob.getInt32(_hidl_offset + 0);
        this.nafId = _hidl_blob.getString(_hidl_offset + 8);
        parcel.readEmbeddedBuffer((long) (this.nafId.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 8 + 0, false);
        this.impiLen = _hidl_blob.getInt32(_hidl_offset + 24);
        this.impi = _hidl_blob.getString(_hidl_offset + 32);
        parcel.readEmbeddedBuffer((long) (this.impi.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILUICCKSNAFAUTHPARAMS> _hidl_vec) {
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
        _hidl_blob.putInt32(0 + _hidl_offset, this.nafLen);
        _hidl_blob.putString(8 + _hidl_offset, this.nafId);
        _hidl_blob.putInt32(24 + _hidl_offset, this.impiLen);
        _hidl_blob.putString(32 + _hidl_offset, this.impi);
    }
}
