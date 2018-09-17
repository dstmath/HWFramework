package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILImsMtStatusReport {
    public int callStatus;
    public int failCause;
    public String pNumber = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILImsMtStatusReport.class) {
            return false;
        }
        RILImsMtStatusReport other = (RILImsMtStatusReport) otherObject;
        return this.failCause == other.failCause && this.callStatus == other.callStatus && HidlSupport.deepEquals(this.pNumber, other.pNumber);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.failCause))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.callStatus))), Integer.valueOf(HidlSupport.deepHashCode(this.pNumber))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".failCause = ");
        builder.append(this.failCause);
        builder.append(", .callStatus = ");
        builder.append(this.callStatus);
        builder.append(", .pNumber = ");
        builder.append(this.pNumber);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<RILImsMtStatusReport> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILImsMtStatusReport> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILImsMtStatusReport _hidl_vec_element = new RILImsMtStatusReport();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.failCause = _hidl_blob.getInt32(_hidl_offset + 0);
        this.callStatus = _hidl_blob.getInt32(4 + _hidl_offset);
        this.pNumber = _hidl_blob.getString(_hidl_offset + 8);
        parcel.readEmbeddedBuffer((long) (this.pNumber.getBytes().length + 1), _hidl_blob.handle(), 0 + (_hidl_offset + 8), false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILImsMtStatusReport> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILImsMtStatusReport) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.failCause);
        _hidl_blob.putInt32(4 + _hidl_offset, this.callStatus);
        _hidl_blob.putString(8 + _hidl_offset, this.pNumber);
    }
}
