package vendor.huawei.hardware.hwdisplay.displayengine.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class DisplayEngineEyeProtectionParam {
    public float bGamma;
    public boolean enable;
    public float gGamma;
    public float rGamma;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != DisplayEngineEyeProtectionParam.class) {
            return false;
        }
        DisplayEngineEyeProtectionParam other = (DisplayEngineEyeProtectionParam) otherObject;
        return this.enable == other.enable && this.rGamma == other.rGamma && this.gGamma == other.gGamma && this.bGamma == other.bGamma;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.enable))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.rGamma))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.gGamma))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.bGamma)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".enable = ");
        builder.append(this.enable);
        builder.append(", .rGamma = ");
        builder.append(this.rGamma);
        builder.append(", .gGamma = ");
        builder.append(this.gGamma);
        builder.append(", .bGamma = ");
        builder.append(this.bGamma);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(16), 0);
    }

    public static final ArrayList<DisplayEngineEyeProtectionParam> readVectorFromParcel(HwParcel parcel) {
        ArrayList<DisplayEngineEyeProtectionParam> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 16), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            DisplayEngineEyeProtectionParam _hidl_vec_element = new DisplayEngineEyeProtectionParam();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 16));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.enable = _hidl_blob.getBool(0 + _hidl_offset);
        this.rGamma = _hidl_blob.getFloat(4 + _hidl_offset);
        this.gGamma = _hidl_blob.getFloat(8 + _hidl_offset);
        this.bGamma = _hidl_blob.getFloat(12 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(16);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<DisplayEngineEyeProtectionParam> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 16);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((DisplayEngineEyeProtectionParam) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 16));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putBool(0 + _hidl_offset, this.enable);
        _hidl_blob.putFloat(4 + _hidl_offset, this.rGamma);
        _hidl_blob.putFloat(8 + _hidl_offset, this.gGamma);
        _hidl_blob.putFloat(12 + _hidl_offset, this.bGamma);
    }
}
