package vendor.huawei.hardware.hwdisplay.displayengine.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class DisplayEngineAreParam {
    public int level;
    public int mode;
    public int threshold;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != DisplayEngineAreParam.class) {
            return false;
        }
        DisplayEngineAreParam other = (DisplayEngineAreParam) otherObject;
        return this.mode == other.mode && this.level == other.level && this.threshold == other.threshold;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.mode))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.level))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.threshold)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".mode = ");
        builder.append(this.mode);
        builder.append(", .level = ");
        builder.append(this.level);
        builder.append(", .threshold = ");
        builder.append(this.threshold);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(12), 0);
    }

    public static final ArrayList<DisplayEngineAreParam> readVectorFromParcel(HwParcel parcel) {
        ArrayList<DisplayEngineAreParam> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 12), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            DisplayEngineAreParam _hidl_vec_element = new DisplayEngineAreParam();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 12));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.mode = _hidl_blob.getInt32(0 + _hidl_offset);
        this.level = _hidl_blob.getInt32(4 + _hidl_offset);
        this.threshold = _hidl_blob.getInt32(8 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(12);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<DisplayEngineAreParam> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 12);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((DisplayEngineAreParam) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 12));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.mode);
        _hidl_blob.putInt32(4 + _hidl_offset, this.level);
        _hidl_blob.putInt32(8 + _hidl_offset, this.threshold);
    }
}
