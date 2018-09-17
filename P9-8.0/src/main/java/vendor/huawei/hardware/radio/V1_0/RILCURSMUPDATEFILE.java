package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILCURSMUPDATEFILE {
    public int appType;
    public int fileId;
    public final RILCURSMFILEPARAMS fileParams = new RILCURSMFILEPARAMS();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILCURSMUPDATEFILE.class) {
            return false;
        }
        RILCURSMUPDATEFILE other = (RILCURSMUPDATEFILE) otherObject;
        return this.appType == other.appType && this.fileId == other.fileId && HidlSupport.deepEquals(this.fileParams, other.fileParams);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.appType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.fileId))), Integer.valueOf(HidlSupport.deepHashCode(this.fileParams))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".appType = ");
        builder.append(RILCURSMAPPTYPEENUM.toString(this.appType));
        builder.append(", .fileId = ");
        builder.append(this.fileId);
        builder.append(", .fileParams = ");
        builder.append(this.fileParams);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<RILCURSMUPDATEFILE> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILCURSMUPDATEFILE> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILCURSMUPDATEFILE _hidl_vec_element = new RILCURSMUPDATEFILE();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.appType = _hidl_blob.getInt32(0 + _hidl_offset);
        this.fileId = _hidl_blob.getInt32(4 + _hidl_offset);
        this.fileParams.readEmbeddedFromParcel(parcel, _hidl_blob, 8 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILCURSMUPDATEFILE> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILCURSMUPDATEFILE) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.appType);
        _hidl_blob.putInt32(4 + _hidl_offset, this.fileId);
        this.fileParams.writeEmbeddedToBlob(_hidl_blob, 8 + _hidl_offset);
    }
}
