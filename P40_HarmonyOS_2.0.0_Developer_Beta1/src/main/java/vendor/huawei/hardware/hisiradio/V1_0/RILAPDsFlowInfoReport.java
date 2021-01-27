package vendor.huawei.hardware.hisiradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILAPDsFlowInfoReport {
    public String currDsTime = new String();
    public String currRxFlow = new String();
    public String currTxFlow = new String();
    public String rxRate = new String();
    public String totalRxFlow = new String();
    public String totalTxFlow = new String();
    public String txRate = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILAPDsFlowInfoReport.class) {
            return false;
        }
        RILAPDsFlowInfoReport other = (RILAPDsFlowInfoReport) otherObject;
        if (HidlSupport.deepEquals(this.currDsTime, other.currDsTime) && HidlSupport.deepEquals(this.txRate, other.txRate) && HidlSupport.deepEquals(this.rxRate, other.rxRate) && HidlSupport.deepEquals(this.currTxFlow, other.currTxFlow) && HidlSupport.deepEquals(this.currRxFlow, other.currRxFlow) && HidlSupport.deepEquals(this.totalTxFlow, other.totalTxFlow) && HidlSupport.deepEquals(this.totalRxFlow, other.totalRxFlow)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.currDsTime)), Integer.valueOf(HidlSupport.deepHashCode(this.txRate)), Integer.valueOf(HidlSupport.deepHashCode(this.rxRate)), Integer.valueOf(HidlSupport.deepHashCode(this.currTxFlow)), Integer.valueOf(HidlSupport.deepHashCode(this.currRxFlow)), Integer.valueOf(HidlSupport.deepHashCode(this.totalTxFlow)), Integer.valueOf(HidlSupport.deepHashCode(this.totalRxFlow)));
    }

    public final String toString() {
        return "{.currDsTime = " + this.currDsTime + ", .txRate = " + this.txRate + ", .rxRate = " + this.rxRate + ", .currTxFlow = " + this.currTxFlow + ", .currRxFlow = " + this.currRxFlow + ", .totalTxFlow = " + this.totalTxFlow + ", .totalRxFlow = " + this.totalRxFlow + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(112), 0);
    }

    public static final ArrayList<RILAPDsFlowInfoReport> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILAPDsFlowInfoReport> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 112), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILAPDsFlowInfoReport _hidl_vec_element = new RILAPDsFlowInfoReport();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 112));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.currDsTime = _hidl_blob.getString(_hidl_offset + 0);
        parcel.readEmbeddedBuffer((long) (this.currDsTime.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, false);
        this.txRate = _hidl_blob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.txRate.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
        this.rxRate = _hidl_blob.getString(_hidl_offset + 32);
        parcel.readEmbeddedBuffer((long) (this.rxRate.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, false);
        this.currTxFlow = _hidl_blob.getString(_hidl_offset + 48);
        parcel.readEmbeddedBuffer((long) (this.currTxFlow.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 48 + 0, false);
        this.currRxFlow = _hidl_blob.getString(_hidl_offset + 64);
        parcel.readEmbeddedBuffer((long) (this.currRxFlow.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 64 + 0, false);
        this.totalTxFlow = _hidl_blob.getString(_hidl_offset + 80);
        parcel.readEmbeddedBuffer((long) (this.totalTxFlow.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 80 + 0, false);
        this.totalRxFlow = _hidl_blob.getString(_hidl_offset + 96);
        parcel.readEmbeddedBuffer((long) (this.totalRxFlow.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 96 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(112);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILAPDsFlowInfoReport> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 112);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 112));
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
        _hidl_blob.putString(80 + _hidl_offset, this.totalTxFlow);
        _hidl_blob.putString(96 + _hidl_offset, this.totalRxFlow);
    }
}
