package vendor.huawei.hardware.fusd.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class WlanApRequestLocInfo {
    public LppeBitString additionalRequestedMeasurements = new LppeBitString();
    public boolean isAdditionalRequestedMeasurements;
    public LppeBitString requestedMeasurements = new LppeBitString();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != WlanApRequestLocInfo.class) {
            return false;
        }
        WlanApRequestLocInfo other = (WlanApRequestLocInfo) otherObject;
        if (HidlSupport.deepEquals(this.requestedMeasurements, other.requestedMeasurements) && this.isAdditionalRequestedMeasurements == other.isAdditionalRequestedMeasurements && HidlSupport.deepEquals(this.additionalRequestedMeasurements, other.additionalRequestedMeasurements)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.requestedMeasurements)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isAdditionalRequestedMeasurements))), Integer.valueOf(HidlSupport.deepHashCode(this.additionalRequestedMeasurements)));
    }

    public final String toString() {
        return "{.requestedMeasurements = " + this.requestedMeasurements + ", .isAdditionalRequestedMeasurements = " + this.isAdditionalRequestedMeasurements + ", .additionalRequestedMeasurements = " + this.additionalRequestedMeasurements + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<WlanApRequestLocInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<WlanApRequestLocInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            WlanApRequestLocInfo _hidl_vec_element = new WlanApRequestLocInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.requestedMeasurements.readEmbeddedFromParcel(parcel, _hidl_blob, 0 + _hidl_offset);
        this.isAdditionalRequestedMeasurements = _hidl_blob.getBool(24 + _hidl_offset);
        this.additionalRequestedMeasurements.readEmbeddedFromParcel(parcel, _hidl_blob, 32 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(56);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<WlanApRequestLocInfo> _hidl_vec) {
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
        this.requestedMeasurements.writeEmbeddedToBlob(_hidl_blob, 0 + _hidl_offset);
        _hidl_blob.putBool(24 + _hidl_offset, this.isAdditionalRequestedMeasurements);
        this.additionalRequestedMeasurements.writeEmbeddedToBlob(_hidl_blob, 32 + _hidl_offset);
    }
}
