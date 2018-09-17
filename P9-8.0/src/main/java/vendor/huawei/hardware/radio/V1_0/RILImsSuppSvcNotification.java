package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILImsSuppSvcNotification {
    public int callForwardReason;
    public int callId;
    public int code;
    public int index;
    public int notificationType;
    public String number = new String();
    public int type;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILImsSuppSvcNotification.class) {
            return false;
        }
        RILImsSuppSvcNotification other = (RILImsSuppSvcNotification) otherObject;
        return this.notificationType == other.notificationType && this.code == other.code && this.callId == other.callId && this.index == other.index && this.type == other.type && HidlSupport.deepEquals(this.number, other.number) && this.callForwardReason == other.callForwardReason;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.notificationType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.code))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.callId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.index))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.type))), Integer.valueOf(HidlSupport.deepHashCode(this.number)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.callForwardReason)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".notificationType = ");
        builder.append(this.notificationType);
        builder.append(", .code = ");
        builder.append(this.code);
        builder.append(", .callId = ");
        builder.append(this.callId);
        builder.append(", .index = ");
        builder.append(this.index);
        builder.append(", .type = ");
        builder.append(this.type);
        builder.append(", .number = ");
        builder.append(this.number);
        builder.append(", .callForwardReason = ");
        builder.append(this.callForwardReason);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<RILImsSuppSvcNotification> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILImsSuppSvcNotification> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILImsSuppSvcNotification _hidl_vec_element = new RILImsSuppSvcNotification();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.notificationType = _hidl_blob.getInt32(_hidl_offset + 0);
        this.code = _hidl_blob.getInt32(4 + _hidl_offset);
        this.callId = _hidl_blob.getInt32(8 + _hidl_offset);
        this.index = _hidl_blob.getInt32(12 + _hidl_offset);
        this.type = _hidl_blob.getInt32(16 + _hidl_offset);
        this.number = _hidl_blob.getString(_hidl_offset + 24);
        parcel.readEmbeddedBuffer((long) (this.number.getBytes().length + 1), _hidl_blob.handle(), 0 + (_hidl_offset + 24), false);
        this.callForwardReason = _hidl_blob.getInt32(40 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILImsSuppSvcNotification> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILImsSuppSvcNotification) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.notificationType);
        _hidl_blob.putInt32(4 + _hidl_offset, this.code);
        _hidl_blob.putInt32(8 + _hidl_offset, this.callId);
        _hidl_blob.putInt32(12 + _hidl_offset, this.index);
        _hidl_blob.putInt32(16 + _hidl_offset, this.type);
        _hidl_blob.putString(24 + _hidl_offset, this.number);
        _hidl_blob.putInt32(40 + _hidl_offset, this.callForwardReason);
    }
}
