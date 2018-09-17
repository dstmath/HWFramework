package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILDsFlowInfoResponse {
    public String lastDsTime = new String();
    public String lastRxFlow = new String();
    public String lastTxFlow = new String();
    public String totalDsTime = new String();
    public String totalRxFlow = new String();
    public String totalTxFlow = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILDsFlowInfoResponse.class) {
            return false;
        }
        RILDsFlowInfoResponse other = (RILDsFlowInfoResponse) otherObject;
        return HidlSupport.deepEquals(this.lastDsTime, other.lastDsTime) && HidlSupport.deepEquals(this.lastTxFlow, other.lastTxFlow) && HidlSupport.deepEquals(this.lastRxFlow, other.lastRxFlow) && HidlSupport.deepEquals(this.totalDsTime, other.totalDsTime) && HidlSupport.deepEquals(this.totalTxFlow, other.totalTxFlow) && HidlSupport.deepEquals(this.totalRxFlow, other.totalRxFlow);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.lastDsTime)), Integer.valueOf(HidlSupport.deepHashCode(this.lastTxFlow)), Integer.valueOf(HidlSupport.deepHashCode(this.lastRxFlow)), Integer.valueOf(HidlSupport.deepHashCode(this.totalDsTime)), Integer.valueOf(HidlSupport.deepHashCode(this.totalTxFlow)), Integer.valueOf(HidlSupport.deepHashCode(this.totalRxFlow))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".lastDsTime = ");
        builder.append(this.lastDsTime);
        builder.append(", .lastTxFlow = ");
        builder.append(this.lastTxFlow);
        builder.append(", .lastRxFlow = ");
        builder.append(this.lastRxFlow);
        builder.append(", .totalDsTime = ");
        builder.append(this.totalDsTime);
        builder.append(", .totalTxFlow = ");
        builder.append(this.totalTxFlow);
        builder.append(", .totalRxFlow = ");
        builder.append(this.totalRxFlow);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(96), 0);
    }

    public static final ArrayList<RILDsFlowInfoResponse> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILDsFlowInfoResponse> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 96), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILDsFlowInfoResponse _hidl_vec_element = new RILDsFlowInfoResponse();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 96));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.lastDsTime = _hidl_blob.getString(0 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.lastDsTime.getBytes().length + 1), _hidl_blob.handle(), 0 + (0 + _hidl_offset), false);
        this.lastTxFlow = _hidl_blob.getString(16 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.lastTxFlow.getBytes().length + 1), _hidl_blob.handle(), 0 + (16 + _hidl_offset), false);
        this.lastRxFlow = _hidl_blob.getString(32 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.lastRxFlow.getBytes().length + 1), _hidl_blob.handle(), 0 + (32 + _hidl_offset), false);
        this.totalDsTime = _hidl_blob.getString(48 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.totalDsTime.getBytes().length + 1), _hidl_blob.handle(), 0 + (48 + _hidl_offset), false);
        this.totalTxFlow = _hidl_blob.getString(64 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.totalTxFlow.getBytes().length + 1), _hidl_blob.handle(), 0 + (64 + _hidl_offset), false);
        this.totalRxFlow = _hidl_blob.getString(80 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.totalRxFlow.getBytes().length + 1), _hidl_blob.handle(), 0 + (80 + _hidl_offset), false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(96);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILDsFlowInfoResponse> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 96);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILDsFlowInfoResponse) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 96));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(0 + _hidl_offset, this.lastDsTime);
        _hidl_blob.putString(16 + _hidl_offset, this.lastTxFlow);
        _hidl_blob.putString(32 + _hidl_offset, this.lastRxFlow);
        _hidl_blob.putString(48 + _hidl_offset, this.totalDsTime);
        _hidl_blob.putString(64 + _hidl_offset, this.totalTxFlow);
        _hidl_blob.putString(80 + _hidl_offset, this.totalRxFlow);
    }
}
