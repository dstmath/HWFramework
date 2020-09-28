package vendor.huawei.hardware.hisiradio.V1_2;

import android.hardware.radio.V1_0.RegState;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class HwVoiceRegStateResult_1_2 {
    public HwCellIdentity_1_2 cellIdentity = new HwCellIdentity_1_2();
    public boolean cssSupported;
    public int defaultRoamingIndicator;
    public int nsaState;
    public int rat;
    public int reasonForDenial;
    public int regState;
    public int roamingIndicator;
    public int systemIsInPrl;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != HwVoiceRegStateResult_1_2.class) {
            return false;
        }
        HwVoiceRegStateResult_1_2 other = (HwVoiceRegStateResult_1_2) otherObject;
        if (this.regState == other.regState && this.rat == other.rat && this.cssSupported == other.cssSupported && this.roamingIndicator == other.roamingIndicator && this.systemIsInPrl == other.systemIsInPrl && this.defaultRoamingIndicator == other.defaultRoamingIndicator && this.reasonForDenial == other.reasonForDenial && HidlSupport.deepEquals(this.cellIdentity, other.cellIdentity) && this.nsaState == other.nsaState) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.regState))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rat))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.cssSupported))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.roamingIndicator))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.systemIsInPrl))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.defaultRoamingIndicator))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.reasonForDenial))), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentity)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.nsaState))));
    }

    public final String toString() {
        return "{" + ".regState = " + RegState.toString(this.regState) + ", .rat = " + this.rat + ", .cssSupported = " + this.cssSupported + ", .roamingIndicator = " + this.roamingIndicator + ", .systemIsInPrl = " + this.systemIsInPrl + ", .defaultRoamingIndicator = " + this.defaultRoamingIndicator + ", .reasonForDenial = " + this.reasonForDenial + ", .cellIdentity = " + this.cellIdentity + ", .nsaState = " + this.nsaState + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(144), 0);
    }

    public static final ArrayList<HwVoiceRegStateResult_1_2> readVectorFromParcel(HwParcel parcel) {
        ArrayList<HwVoiceRegStateResult_1_2> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 144), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            HwVoiceRegStateResult_1_2 _hidl_vec_element = new HwVoiceRegStateResult_1_2();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 144));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.regState = _hidl_blob.getInt32(0 + _hidl_offset);
        this.rat = _hidl_blob.getInt32(4 + _hidl_offset);
        this.cssSupported = _hidl_blob.getBool(8 + _hidl_offset);
        this.roamingIndicator = _hidl_blob.getInt32(12 + _hidl_offset);
        this.systemIsInPrl = _hidl_blob.getInt32(16 + _hidl_offset);
        this.defaultRoamingIndicator = _hidl_blob.getInt32(20 + _hidl_offset);
        this.reasonForDenial = _hidl_blob.getInt32(24 + _hidl_offset);
        this.cellIdentity.readEmbeddedFromParcel(parcel, _hidl_blob, 32 + _hidl_offset);
        this.nsaState = _hidl_blob.getInt32(136 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(144);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<HwVoiceRegStateResult_1_2> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 144);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 144));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.regState);
        _hidl_blob.putInt32(4 + _hidl_offset, this.rat);
        _hidl_blob.putBool(8 + _hidl_offset, this.cssSupported);
        _hidl_blob.putInt32(12 + _hidl_offset, this.roamingIndicator);
        _hidl_blob.putInt32(16 + _hidl_offset, this.systemIsInPrl);
        _hidl_blob.putInt32(20 + _hidl_offset, this.defaultRoamingIndicator);
        _hidl_blob.putInt32(24 + _hidl_offset, this.reasonForDenial);
        this.cellIdentity.writeEmbeddedToBlob(_hidl_blob, 32 + _hidl_offset);
        _hidl_blob.putInt32(136 + _hidl_offset, this.nsaState);
    }
}
