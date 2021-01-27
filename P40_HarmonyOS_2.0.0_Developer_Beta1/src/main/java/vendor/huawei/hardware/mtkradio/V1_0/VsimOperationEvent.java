package vendor.huawei.hardware.mtkradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class VsimOperationEvent {
    public String data = new String();
    public int dataLength;
    public int eventId;
    public int result;
    public int transactionId;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != VsimOperationEvent.class) {
            return false;
        }
        VsimOperationEvent other = (VsimOperationEvent) otherObject;
        if (this.transactionId == other.transactionId && this.eventId == other.eventId && this.result == other.result && this.dataLength == other.dataLength && HidlSupport.deepEquals(this.data, other.data)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.transactionId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.eventId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.result))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.dataLength))), Integer.valueOf(HidlSupport.deepHashCode(this.data)));
    }

    public final String toString() {
        return "{.transactionId = " + this.transactionId + ", .eventId = " + this.eventId + ", .result = " + this.result + ", .dataLength = " + this.dataLength + ", .data = " + this.data + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<VsimOperationEvent> readVectorFromParcel(HwParcel parcel) {
        ArrayList<VsimOperationEvent> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            VsimOperationEvent _hidl_vec_element = new VsimOperationEvent();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.transactionId = _hidl_blob.getInt32(_hidl_offset + 0);
        this.eventId = _hidl_blob.getInt32(_hidl_offset + 4);
        this.result = _hidl_blob.getInt32(_hidl_offset + 8);
        this.dataLength = _hidl_blob.getInt32(_hidl_offset + 12);
        this.data = _hidl_blob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.data.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<VsimOperationEvent> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.transactionId);
        _hidl_blob.putInt32(4 + _hidl_offset, this.eventId);
        _hidl_blob.putInt32(8 + _hidl_offset, this.result);
        _hidl_blob.putInt32(12 + _hidl_offset, this.dataLength);
        _hidl_blob.putString(16 + _hidl_offset, this.data);
    }
}
