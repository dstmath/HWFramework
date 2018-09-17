package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class StaRoamingConfig {
    public final ArrayList<byte[]> bssidBlacklist = new ArrayList();
    public final ArrayList<byte[]> ssidWhitelist = new ArrayList();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != StaRoamingConfig.class) {
            return false;
        }
        StaRoamingConfig other = (StaRoamingConfig) otherObject;
        return HidlSupport.deepEquals(this.bssidBlacklist, other.bssidBlacklist) && HidlSupport.deepEquals(this.ssidWhitelist, other.ssidWhitelist);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.bssidBlacklist)), Integer.valueOf(HidlSupport.deepHashCode(this.ssidWhitelist))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".bssidBlacklist = ");
        builder.append(this.bssidBlacklist);
        builder.append(", .ssidWhitelist = ");
        builder.append(this.ssidWhitelist);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<StaRoamingConfig> readVectorFromParcel(HwParcel parcel) {
        ArrayList<StaRoamingConfig> _hidl_vec = new ArrayList();
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
        int _hidl_index_0;
        byte[] _hidl_vec_element;
        long _hidl_array_offset_1;
        int _hidl_index_1_0;
        int _hidl_vec_size = _hidl_blob.getInt32((0 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 6), _hidl_blob.handle(), (0 + _hidl_offset) + 0, true);
        this.bssidBlacklist.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec_element = new byte[6];
            _hidl_array_offset_1 = (long) (_hidl_index_0 * 6);
            for (_hidl_index_1_0 = 0; _hidl_index_1_0 < 6; _hidl_index_1_0++) {
                _hidl_vec_element[_hidl_index_1_0] = childBlob.getInt8(_hidl_array_offset_1);
                _hidl_array_offset_1++;
            }
            this.bssidBlacklist.add(_hidl_vec_element);
        }
        _hidl_vec_size = _hidl_blob.getInt32((16 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), (16 + _hidl_offset) + 0, true);
        this.ssidWhitelist.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec_element = new byte[32];
            _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
            for (_hidl_index_1_0 = 0; _hidl_index_1_0 < 32; _hidl_index_1_0++) {
                _hidl_vec_element[_hidl_index_1_0] = childBlob.getInt8(_hidl_array_offset_1);
                _hidl_array_offset_1++;
            }
            this.ssidWhitelist.add(_hidl_vec_element);
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
            ((StaRoamingConfig) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        long _hidl_array_offset_1;
        int _hidl_index_1_0;
        int _hidl_vec_size = this.bssidBlacklist.size();
        _hidl_blob.putInt32((0 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((0 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 6);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_array_offset_1 = (long) (_hidl_index_0 * 6);
            for (_hidl_index_1_0 = 0; _hidl_index_1_0 < 6; _hidl_index_1_0++) {
                childBlob.putInt8(_hidl_array_offset_1, ((byte[]) this.bssidBlacklist.get(_hidl_index_0))[_hidl_index_1_0]);
                _hidl_array_offset_1++;
            }
        }
        _hidl_blob.putBlob((0 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.ssidWhitelist.size();
        _hidl_blob.putInt32((16 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((16 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 32);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
            for (_hidl_index_1_0 = 0; _hidl_index_1_0 < 32; _hidl_index_1_0++) {
                childBlob.putInt8(_hidl_array_offset_1, ((byte[]) this.ssidWhitelist.get(_hidl_index_0))[_hidl_index_1_0]);
                _hidl_array_offset_1++;
            }
        }
        _hidl_blob.putBlob((16 + _hidl_offset) + 0, childBlob);
    }
}
