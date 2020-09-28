package vendor.huawei.hardware.mtkradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class Qos {
    public int dlGbr;
    public int dlMbr;
    public int qci;
    public int ulGbr;
    public int ulMbr;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != Qos.class) {
            return false;
        }
        Qos other = (Qos) otherObject;
        if (this.qci == other.qci && this.dlGbr == other.dlGbr && this.ulGbr == other.ulGbr && this.dlMbr == other.dlMbr && this.ulMbr == other.ulMbr) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.qci))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.dlGbr))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ulGbr))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.dlMbr))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ulMbr))));
    }

    public final String toString() {
        return "{" + ".qci = " + this.qci + ", .dlGbr = " + this.dlGbr + ", .ulGbr = " + this.ulGbr + ", .dlMbr = " + this.dlMbr + ", .ulMbr = " + this.ulMbr + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(20), 0);
    }

    public static final ArrayList<Qos> readVectorFromParcel(HwParcel parcel) {
        ArrayList<Qos> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 20), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            Qos _hidl_vec_element = new Qos();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 20));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.qci = _hidl_blob.getInt32(0 + _hidl_offset);
        this.dlGbr = _hidl_blob.getInt32(4 + _hidl_offset);
        this.ulGbr = _hidl_blob.getInt32(8 + _hidl_offset);
        this.dlMbr = _hidl_blob.getInt32(12 + _hidl_offset);
        this.ulMbr = _hidl_blob.getInt32(16 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(20);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<Qos> _hidl_vec) {
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
        _hidl_blob.putInt32(0 + _hidl_offset, this.qci);
        _hidl_blob.putInt32(4 + _hidl_offset, this.dlGbr);
        _hidl_blob.putInt32(8 + _hidl_offset, this.ulGbr);
        _hidl_blob.putInt32(12 + _hidl_offset, this.dlMbr);
        _hidl_blob.putInt32(16 + _hidl_offset, this.ulMbr);
    }
}
