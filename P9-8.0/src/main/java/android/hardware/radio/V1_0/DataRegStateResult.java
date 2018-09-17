package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import com.android.internal.telephony.AbstractPhoneBase;
import java.util.ArrayList;
import java.util.Objects;

public final class DataRegStateResult {
    public final CellIdentity cellIdentity = new CellIdentity();
    public int maxDataCalls;
    public int rat;
    public int reasonDataDenied;
    public int regState;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != DataRegStateResult.class) {
            return false;
        }
        DataRegStateResult other = (DataRegStateResult) otherObject;
        return this.regState == other.regState && this.rat == other.rat && this.reasonDataDenied == other.reasonDataDenied && this.maxDataCalls == other.maxDataCalls && HidlSupport.deepEquals(this.cellIdentity, other.cellIdentity);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.regState))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rat))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.reasonDataDenied))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxDataCalls))), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentity))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".regState = ");
        builder.append(RegState.toString(this.regState));
        builder.append(", .rat = ");
        builder.append(this.rat);
        builder.append(", .reasonDataDenied = ");
        builder.append(this.reasonDataDenied);
        builder.append(", .maxDataCalls = ");
        builder.append(this.maxDataCalls);
        builder.append(", .cellIdentity = ");
        builder.append(this.cellIdentity);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(104), 0);
    }

    public static final ArrayList<DataRegStateResult> readVectorFromParcel(HwParcel parcel) {
        ArrayList<DataRegStateResult> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * AbstractPhoneBase.EVENT_ECC_NUM), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            DataRegStateResult _hidl_vec_element = new DataRegStateResult();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * AbstractPhoneBase.EVENT_ECC_NUM));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.regState = _hidl_blob.getInt32(0 + _hidl_offset);
        this.rat = _hidl_blob.getInt32(4 + _hidl_offset);
        this.reasonDataDenied = _hidl_blob.getInt32(8 + _hidl_offset);
        this.maxDataCalls = _hidl_blob.getInt32(12 + _hidl_offset);
        this.cellIdentity.readEmbeddedFromParcel(parcel, _hidl_blob, 16 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(AbstractPhoneBase.EVENT_ECC_NUM);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<DataRegStateResult> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * AbstractPhoneBase.EVENT_ECC_NUM);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((DataRegStateResult) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * AbstractPhoneBase.EVENT_ECC_NUM));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.regState);
        _hidl_blob.putInt32(4 + _hidl_offset, this.rat);
        _hidl_blob.putInt32(8 + _hidl_offset, this.reasonDataDenied);
        _hidl_blob.putInt32(12 + _hidl_offset, this.maxDataCalls);
        this.cellIdentity.writeEmbeddedToBlob(_hidl_blob, 16 + _hidl_offset);
    }
}
