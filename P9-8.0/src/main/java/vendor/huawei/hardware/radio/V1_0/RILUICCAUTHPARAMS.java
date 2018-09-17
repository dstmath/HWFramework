package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILUICCAUTHPARAMS {
    public String auth = new String();
    public int authLen;
    public String rand = new String();
    public int randLen;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILUICCAUTHPARAMS.class) {
            return false;
        }
        RILUICCAUTHPARAMS other = (RILUICCAUTHPARAMS) otherObject;
        return this.randLen == other.randLen && HidlSupport.deepEquals(this.rand, other.rand) && this.authLen == other.authLen && HidlSupport.deepEquals(this.auth, other.auth);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.randLen))), Integer.valueOf(HidlSupport.deepHashCode(this.rand)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.authLen))), Integer.valueOf(HidlSupport.deepHashCode(this.auth))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".randLen = ");
        builder.append(this.randLen);
        builder.append(", .rand = ");
        builder.append(this.rand);
        builder.append(", .authLen = ");
        builder.append(this.authLen);
        builder.append(", .auth = ");
        builder.append(this.auth);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<RILUICCAUTHPARAMS> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILUICCAUTHPARAMS> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILUICCAUTHPARAMS _hidl_vec_element = new RILUICCAUTHPARAMS();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.randLen = _hidl_blob.getInt32(0 + _hidl_offset);
        this.rand = _hidl_blob.getString(8 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.rand.getBytes().length + 1), _hidl_blob.handle(), 0 + (8 + _hidl_offset), false);
        this.authLen = _hidl_blob.getInt32(24 + _hidl_offset);
        this.auth = _hidl_blob.getString(32 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.auth.getBytes().length + 1), _hidl_blob.handle(), 0 + (32 + _hidl_offset), false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILUICCAUTHPARAMS> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILUICCAUTHPARAMS) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.randLen);
        _hidl_blob.putString(8 + _hidl_offset, this.rand);
        _hidl_blob.putInt32(24 + _hidl_offset, this.authLen);
        _hidl_blob.putString(32 + _hidl_offset, this.auth);
    }
}
