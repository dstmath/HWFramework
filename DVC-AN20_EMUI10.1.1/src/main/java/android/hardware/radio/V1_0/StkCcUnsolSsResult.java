package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class StkCcUnsolSsResult {
    public ArrayList<CfData> cfData = new ArrayList<>();
    public int requestType;
    public int result;
    public int serviceClass;
    public int serviceType;
    public ArrayList<SsInfoData> ssInfo = new ArrayList<>();
    public int teleserviceType;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != StkCcUnsolSsResult.class) {
            return false;
        }
        StkCcUnsolSsResult other = (StkCcUnsolSsResult) otherObject;
        if (this.serviceType == other.serviceType && this.requestType == other.requestType && this.teleserviceType == other.teleserviceType && HidlSupport.deepEquals(Integer.valueOf(this.serviceClass), Integer.valueOf(other.serviceClass)) && this.result == other.result && HidlSupport.deepEquals(this.ssInfo, other.ssInfo) && HidlSupport.deepEquals(this.cfData, other.cfData)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.serviceType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.requestType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.teleserviceType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.serviceClass))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.result))), Integer.valueOf(HidlSupport.deepHashCode(this.ssInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.cfData)));
    }

    public final String toString() {
        return "{" + ".serviceType = " + SsServiceType.toString(this.serviceType) + ", .requestType = " + SsRequestType.toString(this.requestType) + ", .teleserviceType = " + SsTeleserviceType.toString(this.teleserviceType) + ", .serviceClass = " + SuppServiceClass.dumpBitfield(this.serviceClass) + ", .result = " + RadioError.toString(this.result) + ", .ssInfo = " + this.ssInfo + ", .cfData = " + this.cfData + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<StkCcUnsolSsResult> readVectorFromParcel(HwParcel parcel) {
        ArrayList<StkCcUnsolSsResult> _hidl_vec = new ArrayList<>();
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
        this.serviceType = _hidl_blob.getInt32(_hidl_offset + 0);
        this.requestType = _hidl_blob.getInt32(_hidl_offset + 4);
        this.teleserviceType = _hidl_blob.getInt32(_hidl_offset + 8);
        this.serviceClass = _hidl_blob.getInt32(_hidl_offset + 12);
        this.result = _hidl_blob.getInt32(_hidl_offset + 16);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 24 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 16), _hidl_blob.handle(), _hidl_offset + 24 + 0, true);
        this.ssInfo.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            SsInfoData _hidl_vec_element = new SsInfoData();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 16));
            this.ssInfo.add(_hidl_vec_element);
        }
        int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 40 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 16), _hidl_blob.handle(), _hidl_offset + 40 + 0, true);
        this.cfData.clear();
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            CfData _hidl_vec_element2 = new CfData();
            _hidl_vec_element2.readEmbeddedFromParcel(parcel, childBlob2, (long) (_hidl_index_02 * 16));
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
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(_hidl_offset + 0, this.serviceType);
        _hidl_blob.putInt32(_hidl_offset + 4, this.requestType);
        _hidl_blob.putInt32(_hidl_offset + 8, this.teleserviceType);
        _hidl_blob.putInt32(_hidl_offset + 12, this.serviceClass);
        _hidl_blob.putInt32(_hidl_offset + 16, this.result);
        int _hidl_vec_size = this.ssInfo.size();
        _hidl_blob.putInt32(_hidl_offset + 24 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 24 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 16);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.ssInfo.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 16));
        }
        _hidl_blob.putBlob(_hidl_offset + 24 + 0, childBlob);
        int _hidl_vec_size2 = this.cfData.size();
        _hidl_blob.putInt32(_hidl_offset + 40 + 8, _hidl_vec_size2);
        _hidl_blob.putBool(_hidl_offset + 40 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 16);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            this.cfData.get(_hidl_index_02).writeEmbeddedToBlob(childBlob2, (long) (_hidl_index_02 * 16));
        }
        _hidl_blob.putBlob(_hidl_offset + 40 + 0, childBlob2);
    }
}
