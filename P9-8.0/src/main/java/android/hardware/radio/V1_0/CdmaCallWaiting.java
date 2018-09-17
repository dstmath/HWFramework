package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CdmaCallWaiting {
    public String name = new String();
    public String number = new String();
    public int numberPlan;
    public int numberPresentation;
    public int numberType;
    public final CdmaSignalInfoRecord signalInfoRecord = new CdmaSignalInfoRecord();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CdmaCallWaiting.class) {
            return false;
        }
        CdmaCallWaiting other = (CdmaCallWaiting) otherObject;
        return HidlSupport.deepEquals(this.number, other.number) && this.numberPresentation == other.numberPresentation && HidlSupport.deepEquals(this.name, other.name) && HidlSupport.deepEquals(this.signalInfoRecord, other.signalInfoRecord) && this.numberType == other.numberType && this.numberPlan == other.numberPlan;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.number)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.numberPresentation))), Integer.valueOf(HidlSupport.deepHashCode(this.name)), Integer.valueOf(HidlSupport.deepHashCode(this.signalInfoRecord)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.numberType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.numberPlan)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".number = ");
        builder.append(this.number);
        builder.append(", .numberPresentation = ");
        builder.append(CdmaCallWaitingNumberPresentation.toString(this.numberPresentation));
        builder.append(", .name = ");
        builder.append(this.name);
        builder.append(", .signalInfoRecord = ");
        builder.append(this.signalInfoRecord);
        builder.append(", .numberType = ");
        builder.append(CdmaCallWaitingNumberType.toString(this.numberType));
        builder.append(", .numberPlan = ");
        builder.append(CdmaCallWaitingNumberPlan.toString(this.numberPlan));
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<CdmaCallWaiting> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CdmaCallWaiting> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CdmaCallWaiting _hidl_vec_element = new CdmaCallWaiting();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.number = _hidl_blob.getString(0 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.number.getBytes().length + 1), _hidl_blob.handle(), 0 + (0 + _hidl_offset), false);
        this.numberPresentation = _hidl_blob.getInt32(16 + _hidl_offset);
        this.name = _hidl_blob.getString(24 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.name.getBytes().length + 1), _hidl_blob.handle(), 0 + (24 + _hidl_offset), false);
        this.signalInfoRecord.readEmbeddedFromParcel(parcel, _hidl_blob, 40 + _hidl_offset);
        this.numberType = _hidl_blob.getInt32(44 + _hidl_offset);
        this.numberPlan = _hidl_blob.getInt32(48 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(56);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CdmaCallWaiting> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 56);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CdmaCallWaiting) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(0 + _hidl_offset, this.number);
        _hidl_blob.putInt32(16 + _hidl_offset, this.numberPresentation);
        _hidl_blob.putString(24 + _hidl_offset, this.name);
        this.signalInfoRecord.writeEmbeddedToBlob(_hidl_blob, 40 + _hidl_offset);
        _hidl_blob.putInt32(44 + _hidl_offset, this.numberType);
        _hidl_blob.putInt32(48 + _hidl_offset, this.numberPlan);
    }
}
