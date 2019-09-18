package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class HardwareConfigModem {
    public int maxData;
    public int maxStandby;
    public int maxVoice;
    public int rat;
    public int rilModel;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != HardwareConfigModem.class) {
            return false;
        }
        HardwareConfigModem other = (HardwareConfigModem) otherObject;
        if (this.rilModel == other.rilModel && this.rat == other.rat && this.maxVoice == other.maxVoice && this.maxData == other.maxData && this.maxStandby == other.maxStandby) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rilModel))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rat))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxVoice))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxData))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxStandby)))});
    }

    public final String toString() {
        return "{" + ".rilModel = " + this.rilModel + ", .rat = " + this.rat + ", .maxVoice = " + this.maxVoice + ", .maxData = " + this.maxData + ", .maxStandby = " + this.maxStandby + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(20), 0);
    }

    public static final ArrayList<HardwareConfigModem> readVectorFromParcel(HwParcel parcel) {
        ArrayList<HardwareConfigModem> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 20), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            HardwareConfigModem _hidl_vec_element = new HardwareConfigModem();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 20));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.rilModel = _hidl_blob.getInt32(0 + _hidl_offset);
        this.rat = _hidl_blob.getInt32(4 + _hidl_offset);
        this.maxVoice = _hidl_blob.getInt32(8 + _hidl_offset);
        this.maxData = _hidl_blob.getInt32(12 + _hidl_offset);
        this.maxStandby = _hidl_blob.getInt32(16 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(20);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<HardwareConfigModem> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 20);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 20));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.rilModel);
        _hidl_blob.putInt32(4 + _hidl_offset, this.rat);
        _hidl_blob.putInt32(8 + _hidl_offset, this.maxVoice);
        _hidl_blob.putInt32(12 + _hidl_offset, this.maxData);
        _hidl_blob.putInt32(16 + _hidl_offset, this.maxStandby);
    }
}
