package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILDsFlowInfoReport {
    public String currDsTime = new String();
    public String currRxFlow = new String();
    public String currTxFlow = new String();
    public String qosRxRate = new String();
    public String qosTxRate = new String();
    public String rxRate = new String();
    public String txRate = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILDsFlowInfoReport.class) {
            return false;
        }
        RILDsFlowInfoReport other = (RILDsFlowInfoReport) otherObject;
        return HidlSupport.deepEquals(this.currDsTime, other.currDsTime) && HidlSupport.deepEquals(this.txRate, other.txRate) && HidlSupport.deepEquals(this.rxRate, other.rxRate) && HidlSupport.deepEquals(this.currTxFlow, other.currTxFlow) && HidlSupport.deepEquals(this.currRxFlow, other.currRxFlow) && HidlSupport.deepEquals(this.qosTxRate, other.qosTxRate) && HidlSupport.deepEquals(this.qosRxRate, other.qosRxRate);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.currDsTime)), Integer.valueOf(HidlSupport.deepHashCode(this.txRate)), Integer.valueOf(HidlSupport.deepHashCode(this.rxRate)), Integer.valueOf(HidlSupport.deepHashCode(this.currTxFlow)), Integer.valueOf(HidlSupport.deepHashCode(this.currRxFlow)), Integer.valueOf(HidlSupport.deepHashCode(this.qosTxRate)), Integer.valueOf(HidlSupport.deepHashCode(this.qosRxRate))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".currDsTime = ");
        builder.append(this.currDsTime);
        builder.append(", .txRate = ");
        builder.append(this.txRate);
        builder.append(", .rxRate = ");
        builder.append(this.rxRate);
        builder.append(", .currTxFlow = ");
        builder.append(this.currTxFlow);
        builder.append(", .currRxFlow = ");
        builder.append(this.currRxFlow);
        builder.append(", .qosTxRate = ");
        builder.append(this.qosTxRate);
        builder.append(", .qosRxRate = ");
        builder.append(this.qosRxRate);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(112), 0);
    }

    public static final ArrayList<RILDsFlowInfoReport> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILDsFlowInfoReport> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 112), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILDsFlowInfoReport _hidl_vec_element = new RILDsFlowInfoReport();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 112));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.currDsTime = _hidl_blob.getString(0 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.currDsTime.getBytes().length + 1), _hidl_blob.handle(), 0 + (0 + _hidl_offset), false);
        this.txRate = _hidl_blob.getString(16 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.txRate.getBytes().length + 1), _hidl_blob.handle(), 0 + (16 + _hidl_offset), false);
        this.rxRate = _hidl_blob.getString(32 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.rxRate.getBytes().length + 1), _hidl_blob.handle(), 0 + (32 + _hidl_offset), false);
        this.currTxFlow = _hidl_blob.getString(48 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.currTxFlow.getBytes().length + 1), _hidl_blob.handle(), 0 + (48 + _hidl_offset), false);
        this.currRxFlow = _hidl_blob.getString(64 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.currRxFlow.getBytes().length + 1), _hidl_blob.handle(), 0 + (64 + _hidl_offset), false);
        this.qosTxRate = _hidl_blob.getString(80 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.qosTxRate.getBytes().length + 1), _hidl_blob.handle(), 0 + (80 + _hidl_offset), false);
        this.qosRxRate = _hidl_blob.getString(96 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.qosRxRate.getBytes().length + 1), _hidl_blob.handle(), 0 + (96 + _hidl_offset), false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(112);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILDsFlowInfoReport> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 112);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILDsFlowInfoReport) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 112));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(0 + _hidl_offset, this.currDsTime);
        _hidl_blob.putString(16 + _hidl_offset, this.txRate);
        _hidl_blob.putString(32 + _hidl_offset, this.rxRate);
        _hidl_blob.putString(48 + _hidl_offset, this.currTxFlow);
        _hidl_blob.putString(64 + _hidl_offset, this.currRxFlow);
        _hidl_blob.putString(80 + _hidl_offset, this.qosTxRate);
        _hidl_blob.putString(96 + _hidl_offset, this.qosRxRate);
    }
}
