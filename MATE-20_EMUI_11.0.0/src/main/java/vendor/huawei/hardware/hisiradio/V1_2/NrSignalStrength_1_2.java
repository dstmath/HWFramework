package vendor.huawei.hardware.hisiradio.V1_2;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class NrSignalStrength_1_2 {
    public int cqi;
    public int rsrp;
    public int rsrq;
    public int rssnr;
    public int signalStrength;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NrSignalStrength_1_2.class) {
            return false;
        }
        NrSignalStrength_1_2 other = (NrSignalStrength_1_2) otherObject;
        if (this.signalStrength == other.signalStrength && this.rsrp == other.rsrp && this.rsrq == other.rsrq && this.rssnr == other.rssnr && this.cqi == other.cqi) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.signalStrength))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rsrp))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rsrq))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rssnr))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cqi))));
    }

    public final String toString() {
        return "{.signalStrength = " + this.signalStrength + ", .rsrp = " + this.rsrp + ", .rsrq = " + this.rsrq + ", .rssnr = " + this.rssnr + ", .cqi = " + this.cqi + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(20), 0);
    }

    public static final ArrayList<NrSignalStrength_1_2> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NrSignalStrength_1_2> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 20), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NrSignalStrength_1_2 _hidl_vec_element = new NrSignalStrength_1_2();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 20));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.signalStrength = _hidl_blob.getInt32(0 + _hidl_offset);
        this.rsrp = _hidl_blob.getInt32(4 + _hidl_offset);
        this.rsrq = _hidl_blob.getInt32(8 + _hidl_offset);
        this.rssnr = _hidl_blob.getInt32(12 + _hidl_offset);
        this.cqi = _hidl_blob.getInt32(16 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(20);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NrSignalStrength_1_2> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 20);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 20));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.signalStrength);
        _hidl_blob.putInt32(4 + _hidl_offset, this.rsrp);
        _hidl_blob.putInt32(8 + _hidl_offset, this.rsrq);
        _hidl_blob.putInt32(12 + _hidl_offset, this.rssnr);
        _hidl_blob.putInt32(16 + _hidl_offset, this.cqi);
    }
}
