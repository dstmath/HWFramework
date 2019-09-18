package vendor.huawei.hardware.eid.V1_1;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class INIT_CTID_TA_MSG_S {
    public final int[] cmd_list = new int[32];
    public int cmdlist_cnt;
    public final byte[] ta_path = new byte[512];
    public int ta_path_len;
    public final byte[] uuid = new byte[16];
    public int uuid_len;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != INIT_CTID_TA_MSG_S.class) {
            return false;
        }
        INIT_CTID_TA_MSG_S other = (INIT_CTID_TA_MSG_S) otherObject;
        if (HidlSupport.deepEquals(this.uuid, other.uuid) && this.uuid_len == other.uuid_len && HidlSupport.deepEquals(this.ta_path, other.ta_path) && this.ta_path_len == other.ta_path_len && HidlSupport.deepEquals(this.cmd_list, other.cmd_list) && this.cmdlist_cnt == other.cmdlist_cnt) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.uuid)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.uuid_len))), Integer.valueOf(HidlSupport.deepHashCode(this.ta_path)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ta_path_len))), Integer.valueOf(HidlSupport.deepHashCode(this.cmd_list)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cmdlist_cnt)))});
    }

    public final String toString() {
        return "{" + ".uuid = " + Arrays.toString(this.uuid) + ", .uuid_len = " + this.uuid_len + ", .ta_path = " + Arrays.toString(this.ta_path) + ", .ta_path_len = " + this.ta_path_len + ", .cmd_list = " + Arrays.toString(this.cmd_list) + ", .cmdlist_cnt = " + this.cmdlist_cnt + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(668), 0);
    }

    public static final ArrayList<INIT_CTID_TA_MSG_S> readVectorFromParcel(HwParcel parcel) {
        ArrayList<INIT_CTID_TA_MSG_S> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 668), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            INIT_CTID_TA_MSG_S _hidl_vec_element = new INIT_CTID_TA_MSG_S();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 668));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.copyToInt8Array(0 + _hidl_offset, this.uuid, 16);
        this.uuid_len = _hidl_blob.getInt32(16 + _hidl_offset);
        _hidl_blob.copyToInt8Array(20 + _hidl_offset, this.ta_path, 512);
        this.ta_path_len = _hidl_blob.getInt32(532 + _hidl_offset);
        _hidl_blob.copyToInt32Array(536 + _hidl_offset, this.cmd_list, 32);
        this.cmdlist_cnt = _hidl_blob.getInt32(664 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(668);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<INIT_CTID_TA_MSG_S> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 668);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 668));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt8Array(0 + _hidl_offset, this.uuid);
        _hidl_blob.putInt32(16 + _hidl_offset, this.uuid_len);
        _hidl_blob.putInt8Array(20 + _hidl_offset, this.ta_path);
        _hidl_blob.putInt32(532 + _hidl_offset, this.ta_path_len);
        _hidl_blob.putInt32Array(536 + _hidl_offset, this.cmd_list);
        _hidl_blob.putInt32(664 + _hidl_offset, this.cmdlist_cnt);
    }
}
