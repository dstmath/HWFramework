package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CellIdentityTdscdma {
    public int cid;
    public int cpid;
    public int lac;
    public String mcc = new String();
    public String mnc = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CellIdentityTdscdma.class) {
            return false;
        }
        CellIdentityTdscdma other = (CellIdentityTdscdma) otherObject;
        if (HidlSupport.deepEquals(this.mcc, other.mcc) && HidlSupport.deepEquals(this.mnc, other.mnc) && this.lac == other.lac && this.cid == other.cid && this.cpid == other.cpid) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.mcc)), Integer.valueOf(HidlSupport.deepHashCode(this.mnc)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.lac))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cid))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cpid)))});
    }

    public final String toString() {
        return "{" + ".mcc = " + this.mcc + ", .mnc = " + this.mnc + ", .lac = " + this.lac + ", .cid = " + this.cid + ", .cpid = " + this.cpid + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<CellIdentityTdscdma> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CellIdentityTdscdma> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellIdentityTdscdma _hidl_vec_element = new CellIdentityTdscdma();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.mcc = hwBlob.getString(_hidl_offset + 0);
        parcel.readEmbeddedBuffer((long) (this.mcc.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, false);
        this.mnc = hwBlob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.mnc.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
        this.lac = hwBlob.getInt32(_hidl_offset + 32);
        this.cid = hwBlob.getInt32(_hidl_offset + 36);
        this.cpid = hwBlob.getInt32(_hidl_offset + 40);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CellIdentityTdscdma> _hidl_vec) {
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
        _hidl_blob.putString(0 + _hidl_offset, this.mcc);
        _hidl_blob.putString(16 + _hidl_offset, this.mnc);
        _hidl_blob.putInt32(32 + _hidl_offset, this.lac);
        _hidl_blob.putInt32(36 + _hidl_offset, this.cid);
        _hidl_blob.putInt32(40 + _hidl_offset, this.cpid);
    }
}
