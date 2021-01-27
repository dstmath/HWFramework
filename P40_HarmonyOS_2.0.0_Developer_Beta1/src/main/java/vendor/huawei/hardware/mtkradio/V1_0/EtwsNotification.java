package vendor.huawei.hardware.mtkradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class EtwsNotification {
    public int messageId;
    public String plmnId = new String();
    public String securityInfo = new String();
    public int serialNumber;
    public int warningType;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != EtwsNotification.class) {
            return false;
        }
        EtwsNotification other = (EtwsNotification) otherObject;
        if (this.warningType == other.warningType && this.messageId == other.messageId && this.serialNumber == other.serialNumber && HidlSupport.deepEquals(this.plmnId, other.plmnId) && HidlSupport.deepEquals(this.securityInfo, other.securityInfo)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.warningType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.messageId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.serialNumber))), Integer.valueOf(HidlSupport.deepHashCode(this.plmnId)), Integer.valueOf(HidlSupport.deepHashCode(this.securityInfo)));
    }

    public final String toString() {
        return "{.warningType = " + this.warningType + ", .messageId = " + this.messageId + ", .serialNumber = " + this.serialNumber + ", .plmnId = " + this.plmnId + ", .securityInfo = " + this.securityInfo + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<EtwsNotification> readVectorFromParcel(HwParcel parcel) {
        ArrayList<EtwsNotification> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            EtwsNotification _hidl_vec_element = new EtwsNotification();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.warningType = _hidl_blob.getInt32(_hidl_offset + 0);
        this.messageId = _hidl_blob.getInt32(_hidl_offset + 4);
        this.serialNumber = _hidl_blob.getInt32(_hidl_offset + 8);
        this.plmnId = _hidl_blob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.plmnId.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
        this.securityInfo = _hidl_blob.getString(_hidl_offset + 32);
        parcel.readEmbeddedBuffer((long) (this.securityInfo.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<EtwsNotification> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.warningType);
        _hidl_blob.putInt32(4 + _hidl_offset, this.messageId);
        _hidl_blob.putInt32(8 + _hidl_offset, this.serialNumber);
        _hidl_blob.putString(16 + _hidl_offset, this.plmnId);
        _hidl_blob.putString(32 + _hidl_offset, this.securityInfo);
    }
}
