package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class RILImsMapconImsaMessage {
    public final byte[] msgContext = new byte[4];
    public int msgId;
    public int msgLen;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILImsMapconImsaMessage.class) {
            return false;
        }
        RILImsMapconImsaMessage other = (RILImsMapconImsaMessage) otherObject;
        return this.msgId == other.msgId && this.msgLen == other.msgLen && HidlSupport.deepEquals(this.msgContext, other.msgContext);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.msgId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.msgLen))), Integer.valueOf(HidlSupport.deepHashCode(this.msgContext))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".msgId = ");
        builder.append(this.msgId);
        builder.append(", .msgLen = ");
        builder.append(this.msgLen);
        builder.append(", .msgContext = ");
        builder.append(Arrays.toString(this.msgContext));
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(12), 0);
    }

    public static final ArrayList<RILImsMapconImsaMessage> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILImsMapconImsaMessage> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 12), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILImsMapconImsaMessage _hidl_vec_element = new RILImsMapconImsaMessage();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 12));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.msgId = _hidl_blob.getInt32(0 + _hidl_offset);
        this.msgLen = _hidl_blob.getInt32(4 + _hidl_offset);
        long _hidl_array_offset_0 = _hidl_offset + 8;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 4; _hidl_index_0_0++) {
            this.msgContext[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(12);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILImsMapconImsaMessage> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 12);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILImsMapconImsaMessage) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 12));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.msgId);
        _hidl_blob.putInt32(4 + _hidl_offset, this.msgLen);
        long _hidl_array_offset_0 = _hidl_offset + 8;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 4; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.msgContext[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
    }
}
