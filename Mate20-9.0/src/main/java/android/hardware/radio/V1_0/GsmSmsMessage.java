package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class GsmSmsMessage {
    public String pdu = new String();
    public String smscPdu = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != GsmSmsMessage.class) {
            return false;
        }
        GsmSmsMessage other = (GsmSmsMessage) otherObject;
        if (HidlSupport.deepEquals(this.smscPdu, other.smscPdu) && HidlSupport.deepEquals(this.pdu, other.pdu)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.smscPdu)), Integer.valueOf(HidlSupport.deepHashCode(this.pdu))});
    }

    public final String toString() {
        return "{" + ".smscPdu = " + this.smscPdu + ", .pdu = " + this.pdu + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<GsmSmsMessage> readVectorFromParcel(HwParcel parcel) {
        ArrayList<GsmSmsMessage> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            GsmSmsMessage _hidl_vec_element = new GsmSmsMessage();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.smscPdu = hwBlob.getString(_hidl_offset + 0);
        parcel.readEmbeddedBuffer((long) (this.smscPdu.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, false);
        this.pdu = hwBlob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.pdu.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<GsmSmsMessage> _hidl_vec) {
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
        _hidl_blob.putString(0 + _hidl_offset, this.smscPdu);
        _hidl_blob.putString(16 + _hidl_offset, this.pdu);
    }
}
