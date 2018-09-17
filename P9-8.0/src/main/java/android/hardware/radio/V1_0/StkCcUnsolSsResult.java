package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class StkCcUnsolSsResult {
    public final ArrayList<CfData> cfData = new ArrayList();
    public int requestType;
    public int result;
    public int serviceClass;
    public int serviceType;
    public final ArrayList<SsInfoData> ssInfo = new ArrayList();
    public int teleserviceType;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != StkCcUnsolSsResult.class) {
            return false;
        }
        StkCcUnsolSsResult other = (StkCcUnsolSsResult) otherObject;
        return this.serviceType == other.serviceType && this.requestType == other.requestType && this.teleserviceType == other.teleserviceType && HidlSupport.deepEquals(Integer.valueOf(this.serviceClass), Integer.valueOf(other.serviceClass)) && this.result == other.result && HidlSupport.deepEquals(this.ssInfo, other.ssInfo) && HidlSupport.deepEquals(this.cfData, other.cfData);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.serviceType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.requestType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.teleserviceType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.serviceClass))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.result))), Integer.valueOf(HidlSupport.deepHashCode(this.ssInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.cfData))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".serviceType = ");
        builder.append(SsServiceType.toString(this.serviceType));
        builder.append(", .requestType = ");
        builder.append(SsRequestType.toString(this.requestType));
        builder.append(", .teleserviceType = ");
        builder.append(SsTeleserviceType.toString(this.teleserviceType));
        builder.append(", .serviceClass = ");
        builder.append(SuppServiceClass.dumpBitfield(this.serviceClass));
        builder.append(", .result = ");
        builder.append(RadioError.toString(this.result));
        builder.append(", .ssInfo = ");
        builder.append(this.ssInfo);
        builder.append(", .cfData = ");
        builder.append(this.cfData);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<StkCcUnsolSsResult> readVectorFromParcel(HwParcel parcel) {
        ArrayList<StkCcUnsolSsResult> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            StkCcUnsolSsResult _hidl_vec_element = new StkCcUnsolSsResult();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        this.serviceType = _hidl_blob.getInt32(0 + _hidl_offset);
        this.requestType = _hidl_blob.getInt32(4 + _hidl_offset);
        this.teleserviceType = _hidl_blob.getInt32(8 + _hidl_offset);
        this.serviceClass = _hidl_blob.getInt32(12 + _hidl_offset);
        this.result = _hidl_blob.getInt32(16 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((24 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 16), _hidl_blob.handle(), (24 + _hidl_offset) + 0, true);
        this.ssInfo.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            SsInfoData _hidl_vec_element = new SsInfoData();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 16));
            this.ssInfo.add(_hidl_vec_element);
        }
        _hidl_vec_size = _hidl_blob.getInt32((40 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 16), _hidl_blob.handle(), (40 + _hidl_offset) + 0, true);
        this.cfData.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CfData _hidl_vec_element2 = new CfData();
            _hidl_vec_element2.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 16));
            this.cfData.add(_hidl_vec_element2);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(56);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<StkCcUnsolSsResult> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 56);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((StkCcUnsolSsResult) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        _hidl_blob.putInt32(0 + _hidl_offset, this.serviceType);
        _hidl_blob.putInt32(4 + _hidl_offset, this.requestType);
        _hidl_blob.putInt32(8 + _hidl_offset, this.teleserviceType);
        _hidl_blob.putInt32(12 + _hidl_offset, this.serviceClass);
        _hidl_blob.putInt32(16 + _hidl_offset, this.result);
        int _hidl_vec_size = this.ssInfo.size();
        _hidl_blob.putInt32((24 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((24 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 16);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((SsInfoData) this.ssInfo.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 16));
        }
        _hidl_blob.putBlob((24 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.cfData.size();
        _hidl_blob.putInt32((40 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((40 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 16);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CfData) this.cfData.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 16));
        }
        _hidl_blob.putBlob((40 + _hidl_offset) + 0, childBlob);
    }
}
