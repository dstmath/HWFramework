package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CdmaBroadcastSmsConfigInfo {
    public int language;
    public boolean selected;
    public int serviceCategory;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CdmaBroadcastSmsConfigInfo.class) {
            return false;
        }
        CdmaBroadcastSmsConfigInfo other = (CdmaBroadcastSmsConfigInfo) otherObject;
        if (this.serviceCategory == other.serviceCategory && this.language == other.language && this.selected == other.selected) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.serviceCategory))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.language))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.selected)))});
    }

    public final String toString() {
        return "{" + ".serviceCategory = " + this.serviceCategory + ", .language = " + this.language + ", .selected = " + this.selected + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(12), 0);
    }

    public static final ArrayList<CdmaBroadcastSmsConfigInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CdmaBroadcastSmsConfigInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 12), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CdmaBroadcastSmsConfigInfo _hidl_vec_element = new CdmaBroadcastSmsConfigInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 12));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.serviceCategory = _hidl_blob.getInt32(0 + _hidl_offset);
        this.language = _hidl_blob.getInt32(4 + _hidl_offset);
        this.selected = _hidl_blob.getBool(8 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(12);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CdmaBroadcastSmsConfigInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 12);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 12));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.serviceCategory);
        _hidl_blob.putInt32(4 + _hidl_offset, this.language);
        _hidl_blob.putBool(8 + _hidl_offset, this.selected);
    }
}
