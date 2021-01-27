package vendor.huawei.hardware.fusd.V1_2;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class requestWifiDb {
    public int fenceId;
    public int requestSize;
    public int reserved1;
    public int reserved2;
    public ArrayList<WifiScanResult> result = new ArrayList<>();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != requestWifiDb.class) {
            return false;
        }
        requestWifiDb other = (requestWifiDb) otherObject;
        if (this.fenceId == other.fenceId && this.requestSize == other.requestSize && this.reserved1 == other.reserved1 && this.reserved2 == other.reserved2 && HidlSupport.deepEquals(this.result, other.result)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.fenceId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.requestSize))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.reserved1))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.reserved2))), Integer.valueOf(HidlSupport.deepHashCode(this.result)));
    }

    public final String toString() {
        return "{.fenceId = " + this.fenceId + ", .requestSize = " + this.requestSize + ", .reserved1 = " + this.reserved1 + ", .reserved2 = " + this.reserved2 + ", .result = " + this.result + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<requestWifiDb> readVectorFromParcel(HwParcel parcel) {
        ArrayList<requestWifiDb> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            requestWifiDb _hidl_vec_element = new requestWifiDb();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.fenceId = _hidl_blob.getInt32(_hidl_offset + 0);
        this.requestSize = _hidl_blob.getInt32(_hidl_offset + 4);
        this.reserved1 = _hidl_blob.getInt32(_hidl_offset + 8);
        this.reserved2 = _hidl_blob.getInt32(_hidl_offset + 12);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 16 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), _hidl_offset + 16 + 0, true);
        this.result.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            WifiScanResult _hidl_vec_element = new WifiScanResult();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            this.result.add(_hidl_vec_element);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<requestWifiDb> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(_hidl_offset + 0, this.fenceId);
        _hidl_blob.putInt32(4 + _hidl_offset, this.requestSize);
        _hidl_blob.putInt32(_hidl_offset + 8, this.reserved1);
        _hidl_blob.putInt32(_hidl_offset + 12, this.reserved2);
        int _hidl_vec_size = this.result.size();
        _hidl_blob.putInt32(_hidl_offset + 16 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 16 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.result.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
        }
        _hidl_blob.putBlob(16 + _hidl_offset + 0, childBlob);
    }
}
