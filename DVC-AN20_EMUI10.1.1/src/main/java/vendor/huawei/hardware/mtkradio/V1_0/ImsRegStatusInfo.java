package vendor.huawei.hardware.mtkradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class ImsRegStatusInfo {
    public int account_id;
    public int error_code;
    public String error_msg = new String();
    public int expire_time;
    public int report_type;
    public String uri = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != ImsRegStatusInfo.class) {
            return false;
        }
        ImsRegStatusInfo other = (ImsRegStatusInfo) otherObject;
        if (this.report_type == other.report_type && this.account_id == other.account_id && this.expire_time == other.expire_time && this.error_code == other.error_code && HidlSupport.deepEquals(this.uri, other.uri) && HidlSupport.deepEquals(this.error_msg, other.error_msg)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.report_type))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.account_id))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.expire_time))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.error_code))), Integer.valueOf(HidlSupport.deepHashCode(this.uri)), Integer.valueOf(HidlSupport.deepHashCode(this.error_msg)));
    }

    public final String toString() {
        return "{" + ".report_type = " + this.report_type + ", .account_id = " + this.account_id + ", .expire_time = " + this.expire_time + ", .error_code = " + this.error_code + ", .uri = " + this.uri + ", .error_msg = " + this.error_msg + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<ImsRegStatusInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<ImsRegStatusInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ImsRegStatusInfo _hidl_vec_element = new ImsRegStatusInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.report_type = _hidl_blob.getInt32(_hidl_offset + 0);
        this.account_id = _hidl_blob.getInt32(_hidl_offset + 4);
        this.expire_time = _hidl_blob.getInt32(_hidl_offset + 8);
        this.error_code = _hidl_blob.getInt32(_hidl_offset + 12);
        this.uri = _hidl_blob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.uri.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
        this.error_msg = _hidl_blob.getString(_hidl_offset + 32);
        parcel.readEmbeddedBuffer((long) (this.error_msg.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<ImsRegStatusInfo> _hidl_vec) {
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
        _hidl_blob.putInt32(0 + _hidl_offset, this.report_type);
        _hidl_blob.putInt32(4 + _hidl_offset, this.account_id);
        _hidl_blob.putInt32(8 + _hidl_offset, this.expire_time);
        _hidl_blob.putInt32(12 + _hidl_offset, this.error_code);
        _hidl_blob.putString(16 + _hidl_offset, this.uri);
        _hidl_blob.putString(32 + _hidl_offset, this.error_msg);
    }
}
