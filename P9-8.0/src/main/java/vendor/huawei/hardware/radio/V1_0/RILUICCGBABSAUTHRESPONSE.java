package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILUICCGBABSAUTHRESPONSE {
    public int authStatus;
    public final RILUICCAUTHRESPSYNCFAILTYPE authSyncfail = new RILUICCAUTHRESPSYNCFAILTYPE();
    public final RILUICCAUTHRESTYPE resData = new RILUICCAUTHRESTYPE();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILUICCGBABSAUTHRESPONSE.class) {
            return false;
        }
        RILUICCGBABSAUTHRESPONSE other = (RILUICCGBABSAUTHRESPONSE) otherObject;
        return this.authStatus == other.authStatus && HidlSupport.deepEquals(this.resData, other.resData) && HidlSupport.deepEquals(this.authSyncfail, other.authSyncfail);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.authStatus))), Integer.valueOf(HidlSupport.deepHashCode(this.resData)), Integer.valueOf(HidlSupport.deepHashCode(this.authSyncfail))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".authStatus = ");
        builder.append(RILUICCAUTHRESPSTATUSTYPEENUM.toString(this.authStatus));
        builder.append(", .resData = ");
        builder.append(this.resData);
        builder.append(", .authSyncfail = ");
        builder.append(this.authSyncfail);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<RILUICCGBABSAUTHRESPONSE> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILUICCGBABSAUTHRESPONSE> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILUICCGBABSAUTHRESPONSE _hidl_vec_element = new RILUICCGBABSAUTHRESPONSE();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.authStatus = _hidl_blob.getInt32(0 + _hidl_offset);
        this.resData.readEmbeddedFromParcel(parcel, _hidl_blob, 8 + _hidl_offset);
        this.authSyncfail.readEmbeddedFromParcel(parcel, _hidl_blob, 32 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(56);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILUICCGBABSAUTHRESPONSE> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 56);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILUICCGBABSAUTHRESPONSE) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.authStatus);
        this.resData.writeEmbeddedToBlob(_hidl_blob, 8 + _hidl_offset);
        this.authSyncfail.writeEmbeddedToBlob(_hidl_blob, 32 + _hidl_offset);
    }
}
