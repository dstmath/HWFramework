package vendor.huawei.hardware.fusd.V1_2;

import android.hardware.gnss.V1_0.GnssLocation;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class requestCellDb {
    public GnssLocation location = new GnssLocation();
    public ArrayList<cellPair> pairs = new ArrayList<>();
    public int requestSize;
    public ArrayList<WifiScanResult> result = new ArrayList<>();
    public int type;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != requestCellDb.class) {
            return false;
        }
        requestCellDb other = (requestCellDb) otherObject;
        if (this.type == other.type && this.requestSize == other.requestSize && HidlSupport.deepEquals(this.location, other.location) && HidlSupport.deepEquals(this.result, other.result) && HidlSupport.deepEquals(this.pairs, other.pairs)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.type))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.requestSize))), Integer.valueOf(HidlSupport.deepHashCode(this.location)), Integer.valueOf(HidlSupport.deepHashCode(this.result)), Integer.valueOf(HidlSupport.deepHashCode(this.pairs)));
    }

    public final String toString() {
        return "{.type = " + this.type + ", .requestSize = " + this.requestSize + ", .location = " + this.location + ", .result = " + this.result + ", .pairs = " + this.pairs + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(104), 0);
    }

    public static final ArrayList<requestCellDb> readVectorFromParcel(HwParcel parcel) {
        ArrayList<requestCellDb> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 104), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            requestCellDb _hidl_vec_element = new requestCellDb();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 104));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.type = _hidl_blob.getInt32(_hidl_offset + 0);
        this.requestSize = _hidl_blob.getInt32(_hidl_offset + 4);
        this.location.readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_offset + 8);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 72 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), _hidl_offset + 72 + 0, true);
        this.result.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            WifiScanResult _hidl_vec_element = new WifiScanResult();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            this.result.add(_hidl_vec_element);
        }
        int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 88 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 48), _hidl_blob.handle(), _hidl_offset + 88 + 0, true);
        this.pairs.clear();
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            cellPair _hidl_vec_element2 = new cellPair();
            _hidl_vec_element2.readEmbeddedFromParcel(parcel, childBlob2, (long) (_hidl_index_02 * 48));
            this.pairs.add(_hidl_vec_element2);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(104);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<requestCellDb> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 104);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 104));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(_hidl_offset + 0, this.type);
        _hidl_blob.putInt32(_hidl_offset + 4, this.requestSize);
        this.location.writeEmbeddedToBlob(_hidl_blob, _hidl_offset + 8);
        int _hidl_vec_size = this.result.size();
        _hidl_blob.putInt32(_hidl_offset + 72 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 72 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.result.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
        }
        _hidl_blob.putBlob(_hidl_offset + 72 + 0, childBlob);
        int _hidl_vec_size2 = this.pairs.size();
        _hidl_blob.putInt32(_hidl_offset + 88 + 8, _hidl_vec_size2);
        _hidl_blob.putBool(_hidl_offset + 88 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 48);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            this.pairs.get(_hidl_index_02).writeEmbeddedToBlob(childBlob2, (long) (_hidl_index_02 * 48));
        }
        _hidl_blob.putBlob(_hidl_offset + 88 + 0, childBlob2);
    }
}
