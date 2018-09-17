package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class VoiceRegStateResult {
    public final CellIdentity cellIdentity = new CellIdentity();
    public boolean cssSupported;
    public int defaultRoamingIndicator;
    public int rat;
    public int reasonForDenial;
    public int regState;
    public int roamingIndicator;
    public int systemIsInPrl;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != VoiceRegStateResult.class) {
            return false;
        }
        VoiceRegStateResult other = (VoiceRegStateResult) otherObject;
        return this.regState == other.regState && this.rat == other.rat && this.cssSupported == other.cssSupported && this.roamingIndicator == other.roamingIndicator && this.systemIsInPrl == other.systemIsInPrl && this.defaultRoamingIndicator == other.defaultRoamingIndicator && this.reasonForDenial == other.reasonForDenial && HidlSupport.deepEquals(this.cellIdentity, other.cellIdentity);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.regState))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rat))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.cssSupported))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.roamingIndicator))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.systemIsInPrl))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.defaultRoamingIndicator))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.reasonForDenial))), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentity))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".regState = ");
        builder.append(RegState.toString(this.regState));
        builder.append(", .rat = ");
        builder.append(this.rat);
        builder.append(", .cssSupported = ");
        builder.append(this.cssSupported);
        builder.append(", .roamingIndicator = ");
        builder.append(this.roamingIndicator);
        builder.append(", .systemIsInPrl = ");
        builder.append(this.systemIsInPrl);
        builder.append(", .defaultRoamingIndicator = ");
        builder.append(this.defaultRoamingIndicator);
        builder.append(", .reasonForDenial = ");
        builder.append(this.reasonForDenial);
        builder.append(", .cellIdentity = ");
        builder.append(this.cellIdentity);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(120), 0);
    }

    public static final ArrayList<VoiceRegStateResult> readVectorFromParcel(HwParcel parcel) {
        ArrayList<VoiceRegStateResult> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 120), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            VoiceRegStateResult _hidl_vec_element = new VoiceRegStateResult();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 120));
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
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(120);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<VoiceRegStateResult> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 120);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((VoiceRegStateResult) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 120));
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
    }
}
