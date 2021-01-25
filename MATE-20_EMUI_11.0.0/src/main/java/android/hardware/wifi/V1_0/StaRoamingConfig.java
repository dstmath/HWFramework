package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class StaRoamingConfig {
    public ArrayList<byte[]> bssidBlacklist = new ArrayList<>();
    public ArrayList<byte[]> ssidWhitelist = new ArrayList<>();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != StaRoamingConfig.class) {
            return false;
        }
        StaRoamingConfig other = (StaRoamingConfig) otherObject;
        if (HidlSupport.deepEquals(this.bssidBlacklist, other.bssidBlacklist) && HidlSupport.deepEquals(this.ssidWhitelist, other.ssidWhitelist)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.bssidBlacklist)), Integer.valueOf(HidlSupport.deepHashCode(this.ssidWhitelist)));
    }

    public final String toString() {
        return "{.bssidBlacklist = " + this.bssidBlacklist + ", .ssidWhitelist = " + this.ssidWhitelist + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<StaRoamingConfig> readVectorFromParcel(HwParcel parcel) {
        ArrayList<StaRoamingConfig> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            StaRoamingConfig _hidl_vec_element = new StaRoamingConfig();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 0 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 6), _hidl_blob.handle(), _hidl_offset + 0 + 0, true);
        this.bssidBlacklist.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            byte[] _hidl_vec_element = new byte[6];
            childBlob.copyToInt8Array((long) (_hidl_index_0 * 6), _hidl_vec_element, 6);
            this.bssidBlacklist.add(_hidl_vec_element);
        }
        int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 16 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 32), _hidl_blob.handle(), _hidl_offset + 16 + 0, true);
        this.ssidWhitelist.clear();
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            byte[] _hidl_vec_element2 = new byte[32];
            childBlob2.copyToInt8Array((long) (_hidl_index_02 * 32), _hidl_vec_element2, 32);
            this.ssidWhitelist.add(_hidl_vec_element2);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<StaRoamingConfig> _hidl_vec) {
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
        int _hidl_vec_size = this.bssidBlacklist.size();
        _hidl_blob.putInt32(_hidl_offset + 0 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 0 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 6);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            long _hidl_array_offset_1 = (long) (_hidl_index_0 * 6);
            byte[] _hidl_array_item_1 = this.bssidBlacklist.get(_hidl_index_0);
            if (_hidl_array_item_1 == null || _hidl_array_item_1.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            childBlob.putInt8Array(_hidl_array_offset_1, _hidl_array_item_1);
        }
        _hidl_blob.putBlob(_hidl_offset + 0 + 0, childBlob);
        int _hidl_vec_size2 = this.ssidWhitelist.size();
        _hidl_blob.putInt32(_hidl_offset + 16 + 8, _hidl_vec_size2);
        _hidl_blob.putBool(_hidl_offset + 16 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 32);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            long _hidl_array_offset_12 = (long) (_hidl_index_02 * 32);
            byte[] _hidl_array_item_12 = this.ssidWhitelist.get(_hidl_index_02);
            if (_hidl_array_item_12 == null || _hidl_array_item_12.length != 32) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            childBlob2.putInt8Array(_hidl_array_offset_12, _hidl_array_item_12);
        }
        _hidl_blob.putBlob(_hidl_offset + 16 + 0, childBlob2);
    }
}
