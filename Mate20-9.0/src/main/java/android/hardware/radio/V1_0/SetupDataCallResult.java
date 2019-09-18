package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class SetupDataCallResult {
    public int active;
    public String addresses = new String();
    public int cid;
    public String dnses = new String();
    public String gateways = new String();
    public String ifname = new String();
    public int mtu;
    public String pcscf = new String();
    public int status;
    public int suggestedRetryTime;
    public String type = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != SetupDataCallResult.class) {
            return false;
        }
        SetupDataCallResult other = (SetupDataCallResult) otherObject;
        if (this.status == other.status && this.suggestedRetryTime == other.suggestedRetryTime && this.cid == other.cid && this.active == other.active && HidlSupport.deepEquals(this.type, other.type) && HidlSupport.deepEquals(this.ifname, other.ifname) && HidlSupport.deepEquals(this.addresses, other.addresses) && HidlSupport.deepEquals(this.dnses, other.dnses) && HidlSupport.deepEquals(this.gateways, other.gateways) && HidlSupport.deepEquals(this.pcscf, other.pcscf) && this.mtu == other.mtu) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.status))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.suggestedRetryTime))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cid))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.active))), Integer.valueOf(HidlSupport.deepHashCode(this.type)), Integer.valueOf(HidlSupport.deepHashCode(this.ifname)), Integer.valueOf(HidlSupport.deepHashCode(this.addresses)), Integer.valueOf(HidlSupport.deepHashCode(this.dnses)), Integer.valueOf(HidlSupport.deepHashCode(this.gateways)), Integer.valueOf(HidlSupport.deepHashCode(this.pcscf)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.mtu)))});
    }

    public final String toString() {
        return "{" + ".status = " + DataCallFailCause.toString(this.status) + ", .suggestedRetryTime = " + this.suggestedRetryTime + ", .cid = " + this.cid + ", .active = " + this.active + ", .type = " + this.type + ", .ifname = " + this.ifname + ", .addresses = " + this.addresses + ", .dnses = " + this.dnses + ", .gateways = " + this.gateways + ", .pcscf = " + this.pcscf + ", .mtu = " + this.mtu + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(120), 0);
    }

    public static final ArrayList<SetupDataCallResult> readVectorFromParcel(HwParcel parcel) {
        ArrayList<SetupDataCallResult> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 120), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            SetupDataCallResult _hidl_vec_element = new SetupDataCallResult();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 120));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.status = hwBlob.getInt32(_hidl_offset + 0);
        this.suggestedRetryTime = hwBlob.getInt32(_hidl_offset + 4);
        this.cid = hwBlob.getInt32(_hidl_offset + 8);
        this.active = hwBlob.getInt32(_hidl_offset + 12);
        this.type = hwBlob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.type.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
        this.ifname = hwBlob.getString(_hidl_offset + 32);
        parcel.readEmbeddedBuffer((long) (this.ifname.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, false);
        this.addresses = hwBlob.getString(_hidl_offset + 48);
        parcel.readEmbeddedBuffer((long) (this.addresses.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 48 + 0, false);
        this.dnses = hwBlob.getString(_hidl_offset + 64);
        parcel.readEmbeddedBuffer((long) (this.dnses.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 64 + 0, false);
        this.gateways = hwBlob.getString(_hidl_offset + 80);
        parcel.readEmbeddedBuffer((long) (this.gateways.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 80 + 0, false);
        this.pcscf = hwBlob.getString(_hidl_offset + 96);
        parcel.readEmbeddedBuffer((long) (this.pcscf.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 96 + 0, false);
        this.mtu = hwBlob.getInt32(_hidl_offset + 112);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(120);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<SetupDataCallResult> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 120);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 120));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.status);
        _hidl_blob.putInt32(4 + _hidl_offset, this.suggestedRetryTime);
        _hidl_blob.putInt32(8 + _hidl_offset, this.cid);
        _hidl_blob.putInt32(12 + _hidl_offset, this.active);
        _hidl_blob.putString(16 + _hidl_offset, this.type);
        _hidl_blob.putString(32 + _hidl_offset, this.ifname);
        _hidl_blob.putString(48 + _hidl_offset, this.addresses);
        _hidl_blob.putString(64 + _hidl_offset, this.dnses);
        _hidl_blob.putString(80 + _hidl_offset, this.gateways);
        _hidl_blob.putString(96 + _hidl_offset, this.pcscf);
        _hidl_blob.putInt32(112 + _hidl_offset, this.mtu);
    }
}
