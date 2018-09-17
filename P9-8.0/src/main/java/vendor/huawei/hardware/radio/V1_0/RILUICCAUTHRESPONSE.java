package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import com.android.internal.telephony.AbstractPhoneBase;
import java.util.ArrayList;
import java.util.Objects;

public final class RILUICCAUTHRESPONSE {
    public final RILUICCAUTHRESPCHALLENGETYPE authChallenge = new RILUICCAUTHRESPCHALLENGETYPE();
    public int authStatus;
    public final RILUICCAUTHRESPSYNCFAILTYPE authSyncfail = new RILUICCAUTHRESPSYNCFAILTYPE();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILUICCAUTHRESPONSE.class) {
            return false;
        }
        RILUICCAUTHRESPONSE other = (RILUICCAUTHRESPONSE) otherObject;
        return this.authStatus == other.authStatus && HidlSupport.deepEquals(this.authChallenge, other.authChallenge) && HidlSupport.deepEquals(this.authSyncfail, other.authSyncfail);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.authStatus))), Integer.valueOf(HidlSupport.deepHashCode(this.authChallenge)), Integer.valueOf(HidlSupport.deepHashCode(this.authSyncfail))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".authStatus = ");
        builder.append(RILUICCAUTHRESPSTATUSTYPEENUM.toString(this.authStatus));
        builder.append(", .authChallenge = ");
        builder.append(this.authChallenge);
        builder.append(", .authSyncfail = ");
        builder.append(this.authSyncfail);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(104), 0);
    }

    public static final ArrayList<RILUICCAUTHRESPONSE> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILUICCAUTHRESPONSE> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * AbstractPhoneBase.EVENT_ECC_NUM), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILUICCAUTHRESPONSE _hidl_vec_element = new RILUICCAUTHRESPONSE();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * AbstractPhoneBase.EVENT_ECC_NUM));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.authStatus = _hidl_blob.getInt32(0 + _hidl_offset);
        this.authChallenge.readEmbeddedFromParcel(parcel, _hidl_blob, 8 + _hidl_offset);
        this.authSyncfail.readEmbeddedFromParcel(parcel, _hidl_blob, 80 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(AbstractPhoneBase.EVENT_ECC_NUM);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILUICCAUTHRESPONSE> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * AbstractPhoneBase.EVENT_ECC_NUM);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILUICCAUTHRESPONSE) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * AbstractPhoneBase.EVENT_ECC_NUM));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.authStatus);
        this.authChallenge.writeEmbeddedToBlob(_hidl_blob, 8 + _hidl_offset);
        this.authSyncfail.writeEmbeddedToBlob(_hidl_blob, 80 + _hidl_offset);
    }
}
