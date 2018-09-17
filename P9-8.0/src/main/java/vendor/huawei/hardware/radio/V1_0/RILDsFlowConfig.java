package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILDsFlowConfig {
    public int oper;
    public int reportFlag;
    public int threshold;
    public int totalThreshold;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILDsFlowConfig.class) {
            return false;
        }
        RILDsFlowConfig other = (RILDsFlowConfig) otherObject;
        return this.reportFlag == other.reportFlag && this.threshold == other.threshold && this.totalThreshold == other.totalThreshold && this.oper == other.oper;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.reportFlag))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.threshold))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.totalThreshold))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.oper)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".reportFlag = ");
        builder.append(this.reportFlag);
        builder.append(", .threshold = ");
        builder.append(this.threshold);
        builder.append(", .totalThreshold = ");
        builder.append(this.totalThreshold);
        builder.append(", .oper = ");
        builder.append(this.oper);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(16), 0);
    }

    public static final ArrayList<RILDsFlowConfig> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILDsFlowConfig> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 16), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILDsFlowConfig _hidl_vec_element = new RILDsFlowConfig();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 16));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.reportFlag = _hidl_blob.getInt32(0 + _hidl_offset);
        this.threshold = _hidl_blob.getInt32(4 + _hidl_offset);
        this.totalThreshold = _hidl_blob.getInt32(8 + _hidl_offset);
        this.oper = _hidl_blob.getInt32(12 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(16);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILDsFlowConfig> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 16);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILDsFlowConfig) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 16));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.reportFlag);
        _hidl_blob.putInt32(4 + _hidl_offset, this.threshold);
        _hidl_blob.putInt32(8 + _hidl_offset, this.totalThreshold);
        _hidl_blob.putInt32(12 + _hidl_offset, this.oper);
    }
}
