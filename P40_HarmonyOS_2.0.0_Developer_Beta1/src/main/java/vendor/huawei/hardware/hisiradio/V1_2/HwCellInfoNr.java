package vendor.huawei.hardware.hisiradio.V1_2;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class HwCellInfoNr {
    public HwCellIdentityNr_1_2 cellIdentityNr = new HwCellIdentityNr_1_2();
    public int nrScs;
    public NrSignalStrength_1_2 signalStrengthNr = new NrSignalStrength_1_2();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != HwCellInfoNr.class) {
            return false;
        }
        HwCellInfoNr other = (HwCellInfoNr) otherObject;
        if (HidlSupport.deepEquals(this.cellIdentityNr, other.cellIdentityNr) && HidlSupport.deepEquals(this.signalStrengthNr, other.signalStrengthNr) && this.nrScs == other.nrScs) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityNr)), Integer.valueOf(HidlSupport.deepHashCode(this.signalStrengthNr)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.nrScs))));
    }

    public final String toString() {
        return "{.cellIdentityNr = " + this.cellIdentityNr + ", .signalStrengthNr = " + this.signalStrengthNr + ", .nrScs = " + Scs.toString(this.nrScs) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(80), 0);
    }

    public static final ArrayList<HwCellInfoNr> readVectorFromParcel(HwParcel parcel) {
        ArrayList<HwCellInfoNr> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 80), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            HwCellInfoNr _hidl_vec_element = new HwCellInfoNr();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 80));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.cellIdentityNr.readEmbeddedFromParcel(parcel, _hidl_blob, 0 + _hidl_offset);
        this.signalStrengthNr.readEmbeddedFromParcel(parcel, _hidl_blob, 56 + _hidl_offset);
        this.nrScs = _hidl_blob.getInt32(76 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(80);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<HwCellInfoNr> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 80);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 80));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        this.cellIdentityNr.writeEmbeddedToBlob(_hidl_blob, 0 + _hidl_offset);
        this.signalStrengthNr.writeEmbeddedToBlob(_hidl_blob, 56 + _hidl_offset);
        _hidl_blob.putInt32(76 + _hidl_offset, this.nrScs);
    }
}
