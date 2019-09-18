package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class HardwareConfig {
    public final ArrayList<HardwareConfigModem> modem = new ArrayList<>();
    public final ArrayList<HardwareConfigSim> sim = new ArrayList<>();
    public int state;
    public int type;
    public String uuid = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != HardwareConfig.class) {
            return false;
        }
        HardwareConfig other = (HardwareConfig) otherObject;
        if (this.type == other.type && HidlSupport.deepEquals(this.uuid, other.uuid) && this.state == other.state && HidlSupport.deepEquals(this.modem, other.modem) && HidlSupport.deepEquals(this.sim, other.sim)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.type))), Integer.valueOf(HidlSupport.deepHashCode(this.uuid)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.state))), Integer.valueOf(HidlSupport.deepHashCode(this.modem)), Integer.valueOf(HidlSupport.deepHashCode(this.sim))});
    }

    public final String toString() {
        return "{" + ".type = " + HardwareConfigType.toString(this.type) + ", .uuid = " + this.uuid + ", .state = " + HardwareConfigState.toString(this.state) + ", .modem = " + this.modem + ", .sim = " + this.sim + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(64), 0);
    }

    public static final ArrayList<HardwareConfig> readVectorFromParcel(HwParcel parcel) {
        ArrayList<HardwareConfig> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 64), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            HardwareConfig _hidl_vec_element = new HardwareConfig();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 64));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwParcel hwParcel = parcel;
        HwBlob hwBlob = _hidl_blob;
        this.type = hwBlob.getInt32(_hidl_offset + 0);
        this.uuid = hwBlob.getString(_hidl_offset + 8);
        hwParcel.readEmbeddedBuffer((long) (this.uuid.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 8 + 0, false);
        this.state = hwBlob.getInt32(_hidl_offset + 24);
        int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 32 + 8);
        int _hidl_vec_size2 = _hidl_vec_size;
        HwBlob childBlob = hwParcel.readEmbeddedBuffer((long) (_hidl_vec_size * 20), _hidl_blob.handle(), _hidl_offset + 32 + 0, true);
        this.modem.clear();
        int _hidl_index_0 = 0;
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            HardwareConfigModem _hidl_vec_element = new HardwareConfigModem();
            _hidl_vec_element.readEmbeddedFromParcel(hwParcel, childBlob, (long) (_hidl_index_02 * 20));
            this.modem.add(_hidl_vec_element);
        }
        int _hidl_vec_size3 = hwBlob.getInt32(_hidl_offset + 48 + 8);
        HwBlob childBlob2 = hwParcel.readEmbeddedBuffer((long) (_hidl_vec_size3 * 16), _hidl_blob.handle(), 0 + _hidl_offset + 48, true);
        this.sim.clear();
        while (true) {
            int _hidl_index_03 = _hidl_index_0;
            if (_hidl_index_03 < _hidl_vec_size3) {
                HardwareConfigSim _hidl_vec_element2 = new HardwareConfigSim();
                _hidl_vec_element2.readEmbeddedFromParcel(hwParcel, childBlob2, (long) (_hidl_index_03 * 16));
                this.sim.add(_hidl_vec_element2);
                _hidl_index_0 = _hidl_index_03 + 1;
            } else {
                return;
            }
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(64);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<HardwareConfig> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 64);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 64));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        hwBlob.putInt32(_hidl_offset + 0, this.type);
        hwBlob.putString(_hidl_offset + 8, this.uuid);
        hwBlob.putInt32(_hidl_offset + 24, this.state);
        int _hidl_vec_size = this.modem.size();
        hwBlob.putInt32(_hidl_offset + 32 + 8, _hidl_vec_size);
        hwBlob.putBool(_hidl_offset + 32 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 20);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.modem.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 20));
        }
        hwBlob.putBlob(_hidl_offset + 32 + 0, childBlob);
        int _hidl_vec_size2 = this.sim.size();
        hwBlob.putInt32(_hidl_offset + 48 + 8, _hidl_vec_size2);
        int _hidl_index_02 = 0;
        hwBlob.putBool(_hidl_offset + 48 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 16);
        while (true) {
            int _hidl_index_03 = _hidl_index_02;
            if (_hidl_index_03 < _hidl_vec_size2) {
                this.sim.get(_hidl_index_03).writeEmbeddedToBlob(childBlob2, (long) (_hidl_index_03 * 16));
                _hidl_index_02 = _hidl_index_03 + 1;
            } else {
                hwBlob.putBlob(_hidl_offset + 48 + 0, childBlob2);
                return;
            }
        }
    }
}
