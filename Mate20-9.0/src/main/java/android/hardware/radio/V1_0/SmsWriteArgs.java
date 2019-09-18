package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class SmsWriteArgs {
    public String pdu = new String();
    public String smsc = new String();
    public int status;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != SmsWriteArgs.class) {
            return false;
        }
        SmsWriteArgs other = (SmsWriteArgs) otherObject;
        if (this.status == other.status && HidlSupport.deepEquals(this.pdu, other.pdu) && HidlSupport.deepEquals(this.smsc, other.smsc)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.status))), Integer.valueOf(HidlSupport.deepHashCode(this.pdu)), Integer.valueOf(HidlSupport.deepHashCode(this.smsc))});
    }

    public final String toString() {
        return "{" + ".status = " + SmsWriteArgsStatus.toString(this.status) + ", .pdu = " + this.pdu + ", .smsc = " + this.smsc + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(40), 0);
    }

    public static final ArrayList<SmsWriteArgs> readVectorFromParcel(HwParcel parcel) {
        ArrayList<SmsWriteArgs> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            SmsWriteArgs _hidl_vec_element = new SmsWriteArgs();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.status = hwBlob.getInt32(_hidl_offset + 0);
        this.pdu = hwBlob.getString(_hidl_offset + 8);
        parcel.readEmbeddedBuffer((long) (this.pdu.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 8 + 0, false);
        this.smsc = hwBlob.getString(_hidl_offset + 24);
        parcel.readEmbeddedBuffer((long) (this.smsc.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 24 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(40);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<SmsWriteArgs> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 40);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 40));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.status);
        _hidl_blob.putString(8 + _hidl_offset, this.pdu);
        _hidl_blob.putString(24 + _hidl_offset, this.smsc);
    }
}
