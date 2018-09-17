package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILRADIOSYSINFO {
    public int lockState;
    public int roamStatus;
    public int simState;
    public int srvDomain;
    public int srvStatus;
    public int sysMode;
    public int sysSubmode;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILRADIOSYSINFO.class) {
            return false;
        }
        RILRADIOSYSINFO other = (RILRADIOSYSINFO) otherObject;
        return this.sysSubmode == other.sysSubmode && this.srvStatus == other.srvStatus && this.srvDomain == other.srvDomain && this.roamStatus == other.roamStatus && this.sysMode == other.sysMode && this.simState == other.simState && this.lockState == other.lockState;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sysSubmode))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.srvStatus))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.srvDomain))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.roamStatus))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sysMode))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.simState))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.lockState)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".sysSubmode = ");
        builder.append(this.sysSubmode);
        builder.append(", .srvStatus = ");
        builder.append(this.srvStatus);
        builder.append(", .srvDomain = ");
        builder.append(this.srvDomain);
        builder.append(", .roamStatus = ");
        builder.append(this.roamStatus);
        builder.append(", .sysMode = ");
        builder.append(this.sysMode);
        builder.append(", .simState = ");
        builder.append(this.simState);
        builder.append(", .lockState = ");
        builder.append(this.lockState);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(28), 0);
    }

    public static final ArrayList<RILRADIOSYSINFO> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILRADIOSYSINFO> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 28), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILRADIOSYSINFO _hidl_vec_element = new RILRADIOSYSINFO();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 28));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.sysSubmode = _hidl_blob.getInt32(0 + _hidl_offset);
        this.srvStatus = _hidl_blob.getInt32(4 + _hidl_offset);
        this.srvDomain = _hidl_blob.getInt32(8 + _hidl_offset);
        this.roamStatus = _hidl_blob.getInt32(12 + _hidl_offset);
        this.sysMode = _hidl_blob.getInt32(16 + _hidl_offset);
        this.simState = _hidl_blob.getInt32(20 + _hidl_offset);
        this.lockState = _hidl_blob.getInt32(24 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(28);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILRADIOSYSINFO> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 28);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILRADIOSYSINFO) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 28));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.sysSubmode);
        _hidl_blob.putInt32(4 + _hidl_offset, this.srvStatus);
        _hidl_blob.putInt32(8 + _hidl_offset, this.srvDomain);
        _hidl_blob.putInt32(12 + _hidl_offset, this.roamStatus);
        _hidl_blob.putInt32(16 + _hidl_offset, this.sysMode);
        _hidl_blob.putInt32(20 + _hidl_offset, this.simState);
        _hidl_blob.putInt32(24 + _hidl_offset, this.lockState);
    }
}
