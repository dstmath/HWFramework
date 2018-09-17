package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILVtFlowInfoReport {
    public String currRxFlow = new String();
    public String currTxFlow = new String();
    public String currVtTime = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILVtFlowInfoReport.class) {
            return false;
        }
        RILVtFlowInfoReport other = (RILVtFlowInfoReport) otherObject;
        return HidlSupport.deepEquals(this.currVtTime, other.currVtTime) && HidlSupport.deepEquals(this.currTxFlow, other.currTxFlow) && HidlSupport.deepEquals(this.currRxFlow, other.currRxFlow);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.currVtTime)), Integer.valueOf(HidlSupport.deepHashCode(this.currTxFlow)), Integer.valueOf(HidlSupport.deepHashCode(this.currRxFlow))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".currVtTime = ");
        builder.append(this.currVtTime);
        builder.append(", .currTxFlow = ");
        builder.append(this.currTxFlow);
        builder.append(", .currRxFlow = ");
        builder.append(this.currRxFlow);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<RILVtFlowInfoReport> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILVtFlowInfoReport> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILVtFlowInfoReport _hidl_vec_element = new RILVtFlowInfoReport();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.currVtTime = _hidl_blob.getString(0 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.currVtTime.getBytes().length + 1), _hidl_blob.handle(), 0 + (0 + _hidl_offset), false);
        this.currTxFlow = _hidl_blob.getString(16 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.currTxFlow.getBytes().length + 1), _hidl_blob.handle(), 0 + (16 + _hidl_offset), false);
        this.currRxFlow = _hidl_blob.getString(32 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.currRxFlow.getBytes().length + 1), _hidl_blob.handle(), 0 + (32 + _hidl_offset), false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILVtFlowInfoReport> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILVtFlowInfoReport) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(0 + _hidl_offset, this.currVtTime);
        _hidl_blob.putString(16 + _hidl_offset, this.currTxFlow);
        _hidl_blob.putString(32 + _hidl_offset, this.currRxFlow);
    }
}
