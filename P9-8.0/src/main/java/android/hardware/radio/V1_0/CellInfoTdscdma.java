package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CellInfoTdscdma {
    public final CellIdentityTdscdma cellIdentityTdscdma = new CellIdentityTdscdma();
    public final TdScdmaSignalStrength signalStrengthTdscdma = new TdScdmaSignalStrength();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CellInfoTdscdma.class) {
            return false;
        }
        CellInfoTdscdma other = (CellInfoTdscdma) otherObject;
        return HidlSupport.deepEquals(this.cellIdentityTdscdma, other.cellIdentityTdscdma) && HidlSupport.deepEquals(this.signalStrengthTdscdma, other.signalStrengthTdscdma);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityTdscdma)), Integer.valueOf(HidlSupport.deepHashCode(this.signalStrengthTdscdma))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".cellIdentityTdscdma = ");
        builder.append(this.cellIdentityTdscdma);
        builder.append(", .signalStrengthTdscdma = ");
        builder.append(this.signalStrengthTdscdma);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<CellInfoTdscdma> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CellInfoTdscdma> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellInfoTdscdma _hidl_vec_element = new CellInfoTdscdma();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.cellIdentityTdscdma.readEmbeddedFromParcel(parcel, _hidl_blob, 0 + _hidl_offset);
        this.signalStrengthTdscdma.readEmbeddedFromParcel(parcel, _hidl_blob, 48 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(56);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CellInfoTdscdma> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 56);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CellInfoTdscdma) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        this.cellIdentityTdscdma.writeEmbeddedToBlob(_hidl_blob, 0 + _hidl_offset);
        this.signalStrengthTdscdma.writeEmbeddedToBlob(_hidl_blob, 48 + _hidl_offset);
    }
}
