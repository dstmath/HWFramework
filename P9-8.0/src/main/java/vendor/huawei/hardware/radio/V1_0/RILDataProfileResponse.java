package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILDataProfileResponse {
    public String apn = new String();
    public int authType;
    public int cid;
    public String passwd = new String();
    public String protocol = new String();
    public String user = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILDataProfileResponse.class) {
            return false;
        }
        RILDataProfileResponse other = (RILDataProfileResponse) otherObject;
        return HidlSupport.deepEquals(this.protocol, other.protocol) && HidlSupport.deepEquals(this.apn, other.apn) && this.authType == other.authType && HidlSupport.deepEquals(this.user, other.user) && HidlSupport.deepEquals(this.passwd, other.passwd) && this.cid == other.cid;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.protocol)), Integer.valueOf(HidlSupport.deepHashCode(this.apn)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.authType))), Integer.valueOf(HidlSupport.deepHashCode(this.user)), Integer.valueOf(HidlSupport.deepHashCode(this.passwd)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cid)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".protocol = ");
        builder.append(this.protocol);
        builder.append(", .apn = ");
        builder.append(this.apn);
        builder.append(", .authType = ");
        builder.append(this.authType);
        builder.append(", .user = ");
        builder.append(this.user);
        builder.append(", .passwd = ");
        builder.append(this.passwd);
        builder.append(", .cid = ");
        builder.append(this.cid);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(80), 0);
    }

    public static final ArrayList<RILDataProfileResponse> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILDataProfileResponse> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 80), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILDataProfileResponse _hidl_vec_element = new RILDataProfileResponse();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 80));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.protocol = _hidl_blob.getString(0 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.protocol.getBytes().length + 1), _hidl_blob.handle(), 0 + (0 + _hidl_offset), false);
        this.apn = _hidl_blob.getString(16 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.apn.getBytes().length + 1), _hidl_blob.handle(), 0 + (16 + _hidl_offset), false);
        this.authType = _hidl_blob.getInt32(32 + _hidl_offset);
        this.user = _hidl_blob.getString(40 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.user.getBytes().length + 1), _hidl_blob.handle(), 0 + (40 + _hidl_offset), false);
        this.passwd = _hidl_blob.getString(56 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.passwd.getBytes().length + 1), _hidl_blob.handle(), 0 + (56 + _hidl_offset), false);
        this.cid = _hidl_blob.getInt32(72 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(80);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILDataProfileResponse> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 80);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILDataProfileResponse) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 80));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(0 + _hidl_offset, this.protocol);
        _hidl_blob.putString(16 + _hidl_offset, this.apn);
        _hidl_blob.putInt32(32 + _hidl_offset, this.authType);
        _hidl_blob.putString(40 + _hidl_offset, this.user);
        _hidl_blob.putString(56 + _hidl_offset, this.passwd);
        _hidl_blob.putInt32(72 + _hidl_offset, this.cid);
    }
}
