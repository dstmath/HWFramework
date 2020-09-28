package vendor.huawei.hardware.mtkradio.V1_0;

import android.hardware.radio.V1_0.CallForwardInfoStatus;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CallForwardInfoEx {
    public String number = new String();
    public int reason;
    public int serviceClass;
    public int status;
    public int timeSeconds;
    public String timeSlotBegin = new String();
    public String timeSlotEnd = new String();
    public int toa;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CallForwardInfoEx.class) {
            return false;
        }
        CallForwardInfoEx other = (CallForwardInfoEx) otherObject;
        if (this.status == other.status && this.reason == other.reason && this.serviceClass == other.serviceClass && this.toa == other.toa && HidlSupport.deepEquals(this.number, other.number) && this.timeSeconds == other.timeSeconds && HidlSupport.deepEquals(this.timeSlotBegin, other.timeSlotBegin) && HidlSupport.deepEquals(this.timeSlotEnd, other.timeSlotEnd)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.status))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.reason))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.serviceClass))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.toa))), Integer.valueOf(HidlSupport.deepHashCode(this.number)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.timeSeconds))), Integer.valueOf(HidlSupport.deepHashCode(this.timeSlotBegin)), Integer.valueOf(HidlSupport.deepHashCode(this.timeSlotEnd)));
    }

    public final String toString() {
        return "{" + ".status = " + CallForwardInfoStatus.toString(this.status) + ", .reason = " + this.reason + ", .serviceClass = " + this.serviceClass + ", .toa = " + this.toa + ", .number = " + this.number + ", .timeSeconds = " + this.timeSeconds + ", .timeSlotBegin = " + this.timeSlotBegin + ", .timeSlotEnd = " + this.timeSlotEnd + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(72), 0);
    }

    public static final ArrayList<CallForwardInfoEx> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CallForwardInfoEx> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 72), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CallForwardInfoEx _hidl_vec_element = new CallForwardInfoEx();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 72));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.status = _hidl_blob.getInt32(_hidl_offset + 0);
        this.reason = _hidl_blob.getInt32(_hidl_offset + 4);
        this.serviceClass = _hidl_blob.getInt32(_hidl_offset + 8);
        this.toa = _hidl_blob.getInt32(_hidl_offset + 12);
        this.number = _hidl_blob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.number.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
        this.timeSeconds = _hidl_blob.getInt32(_hidl_offset + 32);
        this.timeSlotBegin = _hidl_blob.getString(_hidl_offset + 40);
        parcel.readEmbeddedBuffer((long) (this.timeSlotBegin.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 40 + 0, false);
        this.timeSlotEnd = _hidl_blob.getString(_hidl_offset + 56);
        parcel.readEmbeddedBuffer((long) (this.timeSlotEnd.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 56 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(72);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CallForwardInfoEx> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 72);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 72));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.status);
        _hidl_blob.putInt32(4 + _hidl_offset, this.reason);
        _hidl_blob.putInt32(8 + _hidl_offset, this.serviceClass);
        _hidl_blob.putInt32(12 + _hidl_offset, this.toa);
        _hidl_blob.putString(16 + _hidl_offset, this.number);
        _hidl_blob.putInt32(32 + _hidl_offset, this.timeSeconds);
        _hidl_blob.putString(40 + _hidl_offset, this.timeSlotBegin);
        _hidl_blob.putString(56 + _hidl_offset, this.timeSlotEnd);
    }
}
