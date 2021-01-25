package vendor.huawei.hardware.mtkradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class ImsConfParticipant {
    public String display_text = new String();
    public String end_point = new String();
    public String entity = new String();
    public String status = new String();
    public String user_addr = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != ImsConfParticipant.class) {
            return false;
        }
        ImsConfParticipant other = (ImsConfParticipant) otherObject;
        if (HidlSupport.deepEquals(this.user_addr, other.user_addr) && HidlSupport.deepEquals(this.end_point, other.end_point) && HidlSupport.deepEquals(this.entity, other.entity) && HidlSupport.deepEquals(this.display_text, other.display_text) && HidlSupport.deepEquals(this.status, other.status)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.user_addr)), Integer.valueOf(HidlSupport.deepHashCode(this.end_point)), Integer.valueOf(HidlSupport.deepHashCode(this.entity)), Integer.valueOf(HidlSupport.deepHashCode(this.display_text)), Integer.valueOf(HidlSupport.deepHashCode(this.status)));
    }

    public final String toString() {
        return "{.user_addr = " + this.user_addr + ", .end_point = " + this.end_point + ", .entity = " + this.entity + ", .display_text = " + this.display_text + ", .status = " + this.status + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(80), 0);
    }

    public static final ArrayList<ImsConfParticipant> readVectorFromParcel(HwParcel parcel) {
        ArrayList<ImsConfParticipant> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 80), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ImsConfParticipant _hidl_vec_element = new ImsConfParticipant();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 80));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.user_addr = _hidl_blob.getString(_hidl_offset + 0);
        parcel.readEmbeddedBuffer((long) (this.user_addr.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, false);
        this.end_point = _hidl_blob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.end_point.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
        this.entity = _hidl_blob.getString(_hidl_offset + 32);
        parcel.readEmbeddedBuffer((long) (this.entity.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, false);
        this.display_text = _hidl_blob.getString(_hidl_offset + 48);
        parcel.readEmbeddedBuffer((long) (this.display_text.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 48 + 0, false);
        this.status = _hidl_blob.getString(_hidl_offset + 64);
        parcel.readEmbeddedBuffer((long) (this.status.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 64 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(80);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<ImsConfParticipant> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 80);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 80));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(0 + _hidl_offset, this.user_addr);
        _hidl_blob.putString(16 + _hidl_offset, this.end_point);
        _hidl_blob.putString(32 + _hidl_offset, this.entity);
        _hidl_blob.putString(48 + _hidl_offset, this.display_text);
        _hidl_blob.putString(64 + _hidl_offset, this.status);
    }
}
