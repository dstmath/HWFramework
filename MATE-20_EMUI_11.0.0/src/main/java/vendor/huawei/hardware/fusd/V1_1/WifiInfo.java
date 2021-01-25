package vendor.huawei.hardware.fusd.V1_1;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class WifiInfo {
    public byte[] bssid = new byte[6];
    public byte rssi;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != WifiInfo.class) {
            return false;
        }
        WifiInfo other = (WifiInfo) otherObject;
        if (HidlSupport.deepEquals(this.bssid, other.bssid) && this.rssi == other.rssi) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.bssid)), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.rssi))));
    }

    public final String toString() {
        return "{.bssid = " + Arrays.toString(this.bssid) + ", .rssi = " + ((int) this.rssi) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(7), 0);
    }

    public static final ArrayList<WifiInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<WifiInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 7), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            WifiInfo _hidl_vec_element = new WifiInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 7));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.copyToInt8Array(0 + _hidl_offset, this.bssid, 6);
        this.rssi = _hidl_blob.getInt8(6 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(7);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<WifiInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 7);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 7));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        long _hidl_array_offset_0 = 0 + _hidl_offset;
        byte[] _hidl_array_item_0 = this.bssid;
        if (_hidl_array_item_0 == null || _hidl_array_item_0.length != 6) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_0, _hidl_array_item_0);
        _hidl_blob.putInt8(6 + _hidl_offset, this.rssi);
    }
}
