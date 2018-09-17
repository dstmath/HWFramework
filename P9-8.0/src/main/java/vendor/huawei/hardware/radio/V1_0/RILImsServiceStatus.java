package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class RILImsServiceStatus {
    public final RILImsStatusForAccTech[] acctechstatus = new RILImsStatusForAccTech[1];
    public int callType;
    public int isValid;
    public int nAcctechstatus;
    public int srvStatus;
    public final RILImsUserData userData = new RILImsUserData();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILImsServiceStatus.class) {
            return false;
        }
        RILImsServiceStatus other = (RILImsServiceStatus) otherObject;
        return this.isValid == other.isValid && this.callType == other.callType && this.srvStatus == other.srvStatus && HidlSupport.deepEquals(this.userData, other.userData) && this.nAcctechstatus == other.nAcctechstatus && HidlSupport.deepEquals(this.acctechstatus, other.acctechstatus);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.isValid))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.callType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.srvStatus))), Integer.valueOf(HidlSupport.deepHashCode(this.userData)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.nAcctechstatus))), Integer.valueOf(HidlSupport.deepHashCode(this.acctechstatus))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".isValid = ");
        builder.append(this.isValid);
        builder.append(", .callType = ");
        builder.append(RILImsCallType.toString(this.callType));
        builder.append(", .srvStatus = ");
        builder.append(RILImsStatusType.toString(this.srvStatus));
        builder.append(", .userData = ");
        builder.append(this.userData);
        builder.append(", .nAcctechstatus = ");
        builder.append(this.nAcctechstatus);
        builder.append(", .acctechstatus = ");
        builder.append(Arrays.toString(this.acctechstatus));
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(64), 0);
    }

    public static final ArrayList<RILImsServiceStatus> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILImsServiceStatus> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 64), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILImsServiceStatus _hidl_vec_element = new RILImsServiceStatus();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 64));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.isValid = _hidl_blob.getInt32(0 + _hidl_offset);
        this.callType = _hidl_blob.getInt32(4 + _hidl_offset);
        this.srvStatus = _hidl_blob.getInt32(8 + _hidl_offset);
        this.userData.readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_offset + 16);
        this.nAcctechstatus = _hidl_blob.getInt32(40 + _hidl_offset);
        long _hidl_array_offset_0 = _hidl_offset + 44;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 1; _hidl_index_0_0++) {
            this.acctechstatus[_hidl_index_0_0] = new RILImsStatusForAccTech();
            this.acctechstatus[_hidl_index_0_0].readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_array_offset_0);
            _hidl_array_offset_0 += 16;
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(64);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILImsServiceStatus> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 64);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILImsServiceStatus) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 64));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.isValid);
        _hidl_blob.putInt32(4 + _hidl_offset, this.callType);
        _hidl_blob.putInt32(8 + _hidl_offset, this.srvStatus);
        this.userData.writeEmbeddedToBlob(_hidl_blob, _hidl_offset + 16);
        _hidl_blob.putInt32(40 + _hidl_offset, this.nAcctechstatus);
        long _hidl_array_offset_0 = _hidl_offset + 44;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 1; _hidl_index_0_0++) {
            this.acctechstatus[_hidl_index_0_0].writeEmbeddedToBlob(_hidl_blob, _hidl_array_offset_0);
            _hidl_array_offset_0 += 16;
        }
    }
}
