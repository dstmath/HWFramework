package android.hardware.radio.V1_2;

import android.hardware.radio.V1_1.RadioAccessSpecifier;
import android.hardware.radio.V1_1.ScanType;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class NetworkScanRequest {
    public boolean incrementalResults;
    public int incrementalResultsPeriodicity;
    public int interval;
    public int maxSearchTime;
    public ArrayList<String> mccMncs = new ArrayList<>();
    public ArrayList<RadioAccessSpecifier> specifiers = new ArrayList<>();
    public int type;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NetworkScanRequest.class) {
            return false;
        }
        NetworkScanRequest other = (NetworkScanRequest) otherObject;
        if (this.type == other.type && this.interval == other.interval && HidlSupport.deepEquals(this.specifiers, other.specifiers) && this.maxSearchTime == other.maxSearchTime && this.incrementalResults == other.incrementalResults && this.incrementalResultsPeriodicity == other.incrementalResultsPeriodicity && HidlSupport.deepEquals(this.mccMncs, other.mccMncs)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.type))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.interval))), Integer.valueOf(HidlSupport.deepHashCode(this.specifiers)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxSearchTime))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.incrementalResults))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.incrementalResultsPeriodicity))), Integer.valueOf(HidlSupport.deepHashCode(this.mccMncs)));
    }

    public final String toString() {
        return "{" + ".type = " + ScanType.toString(this.type) + ", .interval = " + this.interval + ", .specifiers = " + this.specifiers + ", .maxSearchTime = " + this.maxSearchTime + ", .incrementalResults = " + this.incrementalResults + ", .incrementalResultsPeriodicity = " + this.incrementalResultsPeriodicity + ", .mccMncs = " + this.mccMncs + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<NetworkScanRequest> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NetworkScanRequest> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NetworkScanRequest _hidl_vec_element = new NetworkScanRequest();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.type = _hidl_blob.getInt32(_hidl_offset + 0);
        this.interval = _hidl_blob.getInt32(_hidl_offset + 4);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 8 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 72), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
        this.specifiers.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RadioAccessSpecifier _hidl_vec_element = new RadioAccessSpecifier();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 72));
            this.specifiers.add(_hidl_vec_element);
        }
        this.maxSearchTime = _hidl_blob.getInt32(_hidl_offset + 24);
        this.incrementalResults = _hidl_blob.getBool(_hidl_offset + 28);
        this.incrementalResultsPeriodicity = _hidl_blob.getInt32(_hidl_offset + 32);
        int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 40 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 16), _hidl_blob.handle(), _hidl_offset + 40 + 0, true);
        this.mccMncs.clear();
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            new String();
            String _hidl_vec_element2 = childBlob2.getString((long) (_hidl_index_02 * 16));
            parcel.readEmbeddedBuffer((long) (_hidl_vec_element2.getBytes().length + 1), childBlob2.handle(), (long) ((_hidl_index_02 * 16) + 0), false);
            this.mccMncs.add(_hidl_vec_element2);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(56);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NetworkScanRequest> _hidl_vec) {
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
        _hidl_blob.putInt32(_hidl_offset + 0, this.type);
        _hidl_blob.putInt32(_hidl_offset + 4, this.interval);
        int _hidl_vec_size = this.specifiers.size();
        _hidl_blob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 8 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 72);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.specifiers.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 72));
        }
        _hidl_blob.putBlob(_hidl_offset + 8 + 0, childBlob);
        _hidl_blob.putInt32(_hidl_offset + 24, this.maxSearchTime);
        _hidl_blob.putBool(_hidl_offset + 28, this.incrementalResults);
        _hidl_blob.putInt32(_hidl_offset + 32, this.incrementalResultsPeriodicity);
        int _hidl_vec_size2 = this.mccMncs.size();
        _hidl_blob.putInt32(_hidl_offset + 40 + 8, _hidl_vec_size2);
        _hidl_blob.putBool(_hidl_offset + 40 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 16);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            childBlob2.putString((long) (_hidl_index_02 * 16), this.mccMncs.get(_hidl_index_02));
        }
        _hidl_blob.putBlob(_hidl_offset + 40 + 0, childBlob2);
    }
}
