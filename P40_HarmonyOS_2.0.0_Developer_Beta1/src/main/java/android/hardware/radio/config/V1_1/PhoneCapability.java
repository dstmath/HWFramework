package android.hardware.radio.config.V1_1;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class PhoneCapability {
    public boolean isInternetLingeringSupported;
    public ArrayList<ModemInfo> logicalModemList = new ArrayList<>();
    public byte maxActiveData;
    public byte maxActiveInternetData;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != PhoneCapability.class) {
            return false;
        }
        PhoneCapability other = (PhoneCapability) otherObject;
        if (this.maxActiveData == other.maxActiveData && this.maxActiveInternetData == other.maxActiveInternetData && this.isInternetLingeringSupported == other.isInternetLingeringSupported && HidlSupport.deepEquals(this.logicalModemList, other.logicalModemList)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.maxActiveData))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.maxActiveInternetData))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isInternetLingeringSupported))), Integer.valueOf(HidlSupport.deepHashCode(this.logicalModemList)));
    }

    public final String toString() {
        return "{.maxActiveData = " + ((int) this.maxActiveData) + ", .maxActiveInternetData = " + ((int) this.maxActiveInternetData) + ", .isInternetLingeringSupported = " + this.isInternetLingeringSupported + ", .logicalModemList = " + this.logicalModemList + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<PhoneCapability> readVectorFromParcel(HwParcel parcel) {
        ArrayList<PhoneCapability> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            PhoneCapability _hidl_vec_element = new PhoneCapability();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.maxActiveData = _hidl_blob.getInt8(_hidl_offset + 0);
        this.maxActiveInternetData = _hidl_blob.getInt8(_hidl_offset + 1);
        this.isInternetLingeringSupported = _hidl_blob.getBool(_hidl_offset + 2);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 8 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
        this.logicalModemList.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ModemInfo _hidl_vec_element = new ModemInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 1));
            this.logicalModemList.add(_hidl_vec_element);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<PhoneCapability> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt8(_hidl_offset + 0, this.maxActiveData);
        _hidl_blob.putInt8(1 + _hidl_offset, this.maxActiveInternetData);
        _hidl_blob.putBool(2 + _hidl_offset, this.isInternetLingeringSupported);
        int _hidl_vec_size = this.logicalModemList.size();
        _hidl_blob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 8 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.logicalModemList.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 1));
        }
        _hidl_blob.putBlob(8 + _hidl_offset + 0, childBlob);
    }
}
