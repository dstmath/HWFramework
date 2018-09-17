package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RadioCapability {
    public String logicalModemUuid = new String();
    public int phase;
    public int raf;
    public int session;
    public int status;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RadioCapability.class) {
            return false;
        }
        RadioCapability other = (RadioCapability) otherObject;
        return this.session == other.session && this.phase == other.phase && HidlSupport.deepEquals(Integer.valueOf(this.raf), Integer.valueOf(other.raf)) && HidlSupport.deepEquals(this.logicalModemUuid, other.logicalModemUuid) && this.status == other.status;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.session))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.phase))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.raf))), Integer.valueOf(HidlSupport.deepHashCode(this.logicalModemUuid)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.status)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".session = ");
        builder.append(this.session);
        builder.append(", .phase = ");
        builder.append(RadioCapabilityPhase.toString(this.phase));
        builder.append(", .raf = ");
        builder.append(RadioAccessFamily.dumpBitfield(this.raf));
        builder.append(", .logicalModemUuid = ");
        builder.append(this.logicalModemUuid);
        builder.append(", .status = ");
        builder.append(RadioCapabilityStatus.toString(this.status));
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(40), 0);
    }

    public static final ArrayList<RadioCapability> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RadioCapability> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RadioCapability _hidl_vec_element = new RadioCapability();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.session = _hidl_blob.getInt32(_hidl_offset + 0);
        this.phase = _hidl_blob.getInt32(4 + _hidl_offset);
        this.raf = _hidl_blob.getInt32(8 + _hidl_offset);
        this.logicalModemUuid = _hidl_blob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.logicalModemUuid.getBytes().length + 1), _hidl_blob.handle(), 0 + (_hidl_offset + 16), false);
        this.status = _hidl_blob.getInt32(32 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(40);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RadioCapability> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 40);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RadioCapability) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 40));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.session);
        _hidl_blob.putInt32(4 + _hidl_offset, this.phase);
        _hidl_blob.putInt32(8 + _hidl_offset, this.raf);
        _hidl_blob.putString(16 + _hidl_offset, this.logicalModemUuid);
        _hidl_blob.putInt32(32 + _hidl_offset, this.status);
    }
}
