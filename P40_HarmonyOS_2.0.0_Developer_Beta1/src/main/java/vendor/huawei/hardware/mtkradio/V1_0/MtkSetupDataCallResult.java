package vendor.huawei.hardware.mtkradio.V1_0;

import android.hardware.radio.V1_0.SetupDataCallResult;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class MtkSetupDataCallResult {
    public SetupDataCallResult dcr = new SetupDataCallResult();
    public int rat;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != MtkSetupDataCallResult.class) {
            return false;
        }
        MtkSetupDataCallResult other = (MtkSetupDataCallResult) otherObject;
        if (HidlSupport.deepEquals(this.dcr, other.dcr) && this.rat == other.rat) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.dcr)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rat))));
    }

    public final String toString() {
        return "{.dcr = " + this.dcr + ", .rat = " + this.rat + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(128), 0);
    }

    public static final ArrayList<MtkSetupDataCallResult> readVectorFromParcel(HwParcel parcel) {
        ArrayList<MtkSetupDataCallResult> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 128), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            MtkSetupDataCallResult _hidl_vec_element = new MtkSetupDataCallResult();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 128));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.dcr.readEmbeddedFromParcel(parcel, _hidl_blob, 0 + _hidl_offset);
        this.rat = _hidl_blob.getInt32(120 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(128);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<MtkSetupDataCallResult> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 128);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 128));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        this.dcr.writeEmbeddedToBlob(_hidl_blob, 0 + _hidl_offset);
        _hidl_blob.putInt32(120 + _hidl_offset, this.rat);
    }
}
