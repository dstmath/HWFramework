package android.hardware.broadcastradio.V2_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class Announcement {
    public ProgramSelector selector = new ProgramSelector();
    public byte type;
    public ArrayList<VendorKeyValue> vendorInfo = new ArrayList<>();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != Announcement.class) {
            return false;
        }
        Announcement other = (Announcement) otherObject;
        if (HidlSupport.deepEquals(this.selector, other.selector) && this.type == other.type && HidlSupport.deepEquals(this.vendorInfo, other.vendorInfo)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.selector)), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.type))), Integer.valueOf(HidlSupport.deepHashCode(this.vendorInfo)));
    }

    public final String toString() {
        return "{.selector = " + this.selector + ", .type = " + AnnouncementType.toString(this.type) + ", .vendorInfo = " + this.vendorInfo + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<Announcement> readVectorFromParcel(HwParcel parcel) {
        ArrayList<Announcement> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            Announcement _hidl_vec_element = new Announcement();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.selector.readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_offset + 0);
        this.type = _hidl_blob.getInt8(_hidl_offset + 32);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 40 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), _hidl_offset + 40 + 0, true);
        this.vendorInfo.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            VendorKeyValue _hidl_vec_element = new VendorKeyValue();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            this.vendorInfo.add(_hidl_vec_element);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(56);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<Announcement> _hidl_vec) {
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
        this.selector.writeEmbeddedToBlob(_hidl_blob, _hidl_offset + 0);
        _hidl_blob.putInt8(32 + _hidl_offset, this.type);
        int _hidl_vec_size = this.vendorInfo.size();
        _hidl_blob.putInt32(_hidl_offset + 40 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 40 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.vendorInfo.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
        }
        _hidl_blob.putBlob(40 + _hidl_offset + 0, childBlob);
    }
}
