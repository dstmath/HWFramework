package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class StaRoamingConfig {
    public final ArrayList<byte[]> bssidBlacklist = new ArrayList<>();
    public final ArrayList<byte[]> ssidWhitelist = new ArrayList<>();

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
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.bssidBlacklist)), Integer.valueOf(HidlSupport.deepHashCode(this.ssidWhitelist))});
    }

    public final String toString() {
        return "{" + ".bssidBlacklist = " + this.bssidBlacklist + ", .ssidWhitelist = " + this.ssidWhitelist + "}";
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
        HwBlob hwBlob = _hidl_blob;
        int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 0 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 6), _hidl_blob.handle(), _hidl_offset + 0 + 0, true);
        this.bssidBlacklist.clear();
        int _hidl_index_0 = 0;
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size; _hidl_index_02++) {
            byte[] _hidl_vec_element = new byte[6];
            childBlob.copyToInt8Array((long) (_hidl_index_02 * 6), _hidl_vec_element, 6);
            this.bssidBlacklist.add(_hidl_vec_element);
        }
        int _hidl_vec_size2 = hwBlob.getInt32(_hidl_offset + 16 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 32), _hidl_blob.handle(), _hidl_offset + 16 + 0, true);
        this.ssidWhitelist.clear();
        while (true) {
            int _hidl_index_03 = _hidl_index_0;
            if (_hidl_index_03 < _hidl_vec_size2) {
                byte[] _hidl_vec_element2 = new byte[32];
                childBlob2.copyToInt8Array((long) (_hidl_index_03 * 32), _hidl_vec_element2, 32);
                this.ssidWhitelist.add(_hidl_vec_element2);
                _hidl_index_0 = _hidl_index_03 + 1;
            } else {
                return;
            }
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
        HwBlob hwBlob = _hidl_blob;
        int _hidl_vec_size = this.bssidBlacklist.size();
        hwBlob.putInt32(_hidl_offset + 0 + 8, _hidl_vec_size);
        hwBlob.putBool(_hidl_offset + 0 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 6);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8Array((long) (_hidl_index_0 * 6), this.bssidBlacklist.get(_hidl_index_0));
        }
        hwBlob.putBlob(_hidl_offset + 0 + 0, childBlob);
        int _hidl_vec_size2 = this.ssidWhitelist.size();
        hwBlob.putInt32(_hidl_offset + 16 + 8, _hidl_vec_size2);
        int _hidl_index_02 = 0;
        hwBlob.putBool(_hidl_offset + 16 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 32);
        while (true) {
            int _hidl_index_03 = _hidl_index_02;
            if (_hidl_index_03 < _hidl_vec_size2) {
                childBlob2.putInt8Array((long) (_hidl_index_03 * 32), this.ssidWhitelist.get(_hidl_index_03));
                _hidl_index_02 = _hidl_index_03 + 1;
            } else {
                hwBlob.putBlob(_hidl_offset + 16 + 0, childBlob2);
                return;
            }
        }
    }
}
