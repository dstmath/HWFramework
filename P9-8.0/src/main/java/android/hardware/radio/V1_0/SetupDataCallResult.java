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
        return this.status == other.status && this.suggestedRetryTime == other.suggestedRetryTime && this.cid == other.cid && this.active == other.active && HidlSupport.deepEquals(this.type, other.type) && HidlSupport.deepEquals(this.ifname, other.ifname) && HidlSupport.deepEquals(this.addresses, other.addresses) && HidlSupport.deepEquals(this.dnses, other.dnses) && HidlSupport.deepEquals(this.gateways, other.gateways) && HidlSupport.deepEquals(this.pcscf, other.pcscf) && this.mtu == other.mtu;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.status))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.suggestedRetryTime))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cid))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.active))), Integer.valueOf(HidlSupport.deepHashCode(this.type)), Integer.valueOf(HidlSupport.deepHashCode(this.ifname)), Integer.valueOf(HidlSupport.deepHashCode(this.addresses)), Integer.valueOf(HidlSupport.deepHashCode(this.dnses)), Integer.valueOf(HidlSupport.deepHashCode(this.gateways)), Integer.valueOf(HidlSupport.deepHashCode(this.pcscf)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.mtu)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".status = ");
        builder.append(DataCallFailCause.toString(this.status));
        builder.append(", .suggestedRetryTime = ");
        builder.append(this.suggestedRetryTime);
        builder.append(", .cid = ");
        builder.append(this.cid);
        builder.append(", .active = ");
        builder.append(this.active);
        builder.append(", .type = ");
        builder.append(this.type);
        builder.append(", .ifname = ");
        builder.append(this.ifname);
        builder.append(", .addresses = ");
        builder.append(this.addresses);
        builder.append(", .dnses = ");
        builder.append(this.dnses);
        builder.append(", .gateways = ");
        builder.append(this.gateways);
        builder.append(", .pcscf = ");
        builder.append(this.pcscf);
        builder.append(", .mtu = ");
        builder.append(this.mtu);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(120), 0);
    }

    public static final ArrayList<SetupDataCallResult> readVectorFromParcel(HwParcel parcel) {
        ArrayList<SetupDataCallResult> _hidl_vec = new ArrayList();
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
        this.status = _hidl_blob.getInt32(0 + _hidl_offset);
        this.suggestedRetryTime = _hidl_blob.getInt32(4 + _hidl_offset);
        this.cid = _hidl_blob.getInt32(8 + _hidl_offset);
        this.active = _hidl_blob.getInt32(12 + _hidl_offset);
        this.type = _hidl_blob.getString(16 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.type.getBytes().length + 1), _hidl_blob.handle(), 0 + (16 + _hidl_offset), false);
        this.ifname = _hidl_blob.getString(32 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.ifname.getBytes().length + 1), _hidl_blob.handle(), 0 + (32 + _hidl_offset), false);
        this.addresses = _hidl_blob.getString(48 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.addresses.getBytes().length + 1), _hidl_blob.handle(), 0 + (48 + _hidl_offset), false);
        this.dnses = _hidl_blob.getString(64 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.dnses.getBytes().length + 1), _hidl_blob.handle(), 0 + (64 + _hidl_offset), false);
        this.gateways = _hidl_blob.getString(80 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.gateways.getBytes().length + 1), _hidl_blob.handle(), 0 + (80 + _hidl_offset), false);
        this.pcscf = _hidl_blob.getString(96 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.pcscf.getBytes().length + 1), _hidl_blob.handle(), 0 + (96 + _hidl_offset), false);
        this.mtu = _hidl_blob.getInt32(112 + _hidl_offset);
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
            ((SetupDataCallResult) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 120));
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
