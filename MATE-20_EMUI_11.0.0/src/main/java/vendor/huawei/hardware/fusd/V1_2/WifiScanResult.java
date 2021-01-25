package vendor.huawei.hardware.fusd.V1_2;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class WifiScanResult {
    public int bootTimeHigh;
    public int bootTimeLow;
    public int frequency;
    public ArrayList<Byte> mac = new ArrayList<>();
    public int rssi;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != WifiScanResult.class) {
            return false;
        }
        WifiScanResult other = (WifiScanResult) otherObject;
        if (this.rssi == other.rssi && this.frequency == other.frequency && this.bootTimeLow == other.bootTimeLow && this.bootTimeHigh == other.bootTimeHigh && HidlSupport.deepEquals(this.mac, other.mac)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rssi))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.frequency))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.bootTimeLow))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.bootTimeHigh))), Integer.valueOf(HidlSupport.deepHashCode(this.mac)));
    }

    public final String toString() {
        return "{.rssi = " + this.rssi + ", .frequency = " + this.frequency + ", .bootTimeLow = " + this.bootTimeLow + ", .bootTimeHigh = " + this.bootTimeHigh + ", .mac = " + this.mac + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<WifiScanResult> readVectorFromParcel(HwParcel parcel) {
        ArrayList<WifiScanResult> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            WifiScanResult _hidl_vec_element = new WifiScanResult();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.rssi = _hidl_blob.getInt32(_hidl_offset + 0);
        this.frequency = _hidl_blob.getInt32(_hidl_offset + 4);
        this.bootTimeLow = _hidl_blob.getInt32(_hidl_offset + 8);
        this.bootTimeHigh = _hidl_blob.getInt32(_hidl_offset + 12);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 16 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, true);
        this.mac.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.mac.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<WifiScanResult> _hidl_vec) {
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
        _hidl_blob.putInt32(_hidl_offset + 0, this.rssi);
        _hidl_blob.putInt32(4 + _hidl_offset, this.frequency);
        _hidl_blob.putInt32(_hidl_offset + 8, this.bootTimeLow);
        _hidl_blob.putInt32(_hidl_offset + 12, this.bootTimeHigh);
        int _hidl_vec_size = this.mac.size();
        _hidl_blob.putInt32(_hidl_offset + 16 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 16 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), this.mac.get(_hidl_index_0).byteValue());
        }
        _hidl_blob.putBlob(16 + _hidl_offset + 0, childBlob);
    }
}
