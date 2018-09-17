package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CellInfoCdma {
    public final CellIdentityCdma cellIdentityCdma = new CellIdentityCdma();
    public final CdmaSignalStrength signalStrengthCdma = new CdmaSignalStrength();
    public final EvdoSignalStrength signalStrengthEvdo = new EvdoSignalStrength();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CellInfoCdma.class) {
            return false;
        }
        CellInfoCdma other = (CellInfoCdma) otherObject;
        return HidlSupport.deepEquals(this.cellIdentityCdma, other.cellIdentityCdma) && HidlSupport.deepEquals(this.signalStrengthCdma, other.signalStrengthCdma) && HidlSupport.deepEquals(this.signalStrengthEvdo, other.signalStrengthEvdo);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityCdma)), Integer.valueOf(HidlSupport.deepHashCode(this.signalStrengthCdma)), Integer.valueOf(HidlSupport.deepHashCode(this.signalStrengthEvdo))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".cellIdentityCdma = ");
        builder.append(this.cellIdentityCdma);
        builder.append(", .signalStrengthCdma = ");
        builder.append(this.signalStrengthCdma);
        builder.append(", .signalStrengthEvdo = ");
        builder.append(this.signalStrengthEvdo);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(40), 0);
    }

    public static final ArrayList<CellInfoCdma> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CellInfoCdma> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellInfoCdma _hidl_vec_element = new CellInfoCdma();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.cellIdentityCdma.readEmbeddedFromParcel(parcel, _hidl_blob, 0 + _hidl_offset);
        this.signalStrengthCdma.readEmbeddedFromParcel(parcel, _hidl_blob, 20 + _hidl_offset);
        this.signalStrengthEvdo.readEmbeddedFromParcel(parcel, _hidl_blob, 28 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(40);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CellInfoCdma> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 40);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CellInfoCdma) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 40));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        this.cellIdentityCdma.writeEmbeddedToBlob(_hidl_blob, 0 + _hidl_offset);
        this.signalStrengthCdma.writeEmbeddedToBlob(_hidl_blob, 20 + _hidl_offset);
        this.signalStrengthEvdo.writeEmbeddedToBlob(_hidl_blob, 28 + _hidl_offset);
    }
}
