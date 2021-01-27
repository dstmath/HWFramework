package vendor.huawei.hardware.hisiradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CsgNetworkInfo_1_1 {
    public String csgId = new String();
    public String csgId_name = new String();
    public int csgId_type;
    public int csgType;
    public boolean isConnected;
    public String longName = new String();
    public int networkRat;
    public String plmn = new String();
    public int rsrp;
    public int rsrq;
    public String shortName = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CsgNetworkInfo_1_1.class) {
            return false;
        }
        CsgNetworkInfo_1_1 other = (CsgNetworkInfo_1_1) otherObject;
        if (HidlSupport.deepEquals(this.csgId, other.csgId) && this.csgId_type == other.csgId_type && HidlSupport.deepEquals(this.csgId_name, other.csgId_name) && this.networkRat == other.networkRat && HidlSupport.deepEquals(this.plmn, other.plmn) && HidlSupport.deepEquals(this.longName, other.longName) && HidlSupport.deepEquals(this.shortName, other.shortName) && this.rsrp == other.rsrp && this.rsrq == other.rsrq && this.csgType == other.csgType && this.isConnected == other.isConnected) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.csgId)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.csgId_type))), Integer.valueOf(HidlSupport.deepHashCode(this.csgId_name)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.networkRat))), Integer.valueOf(HidlSupport.deepHashCode(this.plmn)), Integer.valueOf(HidlSupport.deepHashCode(this.longName)), Integer.valueOf(HidlSupport.deepHashCode(this.shortName)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rsrp))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rsrq))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.csgType))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isConnected))));
    }

    public final String toString() {
        return "{.csgId = " + this.csgId + ", .csgId_type = " + this.csgId_type + ", .csgId_name = " + this.csgId_name + ", .networkRat = " + this.networkRat + ", .plmn = " + this.plmn + ", .longName = " + this.longName + ", .shortName = " + this.shortName + ", .rsrp = " + this.rsrp + ", .rsrq = " + this.rsrq + ", .csgType = " + CSGTYPE.toString(this.csgType) + ", .isConnected = " + this.isConnected + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(112), 0);
    }

    public static final ArrayList<CsgNetworkInfo_1_1> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CsgNetworkInfo_1_1> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 112), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CsgNetworkInfo_1_1 _hidl_vec_element = new CsgNetworkInfo_1_1();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 112));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.csgId = _hidl_blob.getString(_hidl_offset + 0);
        parcel.readEmbeddedBuffer((long) (this.csgId.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, false);
        this.csgId_type = _hidl_blob.getInt32(_hidl_offset + 16);
        this.csgId_name = _hidl_blob.getString(_hidl_offset + 24);
        parcel.readEmbeddedBuffer((long) (this.csgId_name.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 24 + 0, false);
        this.networkRat = _hidl_blob.getInt32(_hidl_offset + 40);
        this.plmn = _hidl_blob.getString(_hidl_offset + 48);
        parcel.readEmbeddedBuffer((long) (this.plmn.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 48 + 0, false);
        this.longName = _hidl_blob.getString(_hidl_offset + 64);
        parcel.readEmbeddedBuffer((long) (this.longName.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 64 + 0, false);
        this.shortName = _hidl_blob.getString(_hidl_offset + 80);
        parcel.readEmbeddedBuffer((long) (this.shortName.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 80 + 0, false);
        this.rsrp = _hidl_blob.getInt32(_hidl_offset + 96);
        this.rsrq = _hidl_blob.getInt32(_hidl_offset + 100);
        this.csgType = _hidl_blob.getInt32(_hidl_offset + 104);
        this.isConnected = _hidl_blob.getBool(_hidl_offset + 108);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(112);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CsgNetworkInfo_1_1> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 112);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 112));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(0 + _hidl_offset, this.csgId);
        _hidl_blob.putInt32(16 + _hidl_offset, this.csgId_type);
        _hidl_blob.putString(24 + _hidl_offset, this.csgId_name);
        _hidl_blob.putInt32(40 + _hidl_offset, this.networkRat);
        _hidl_blob.putString(48 + _hidl_offset, this.plmn);
        _hidl_blob.putString(64 + _hidl_offset, this.longName);
        _hidl_blob.putString(80 + _hidl_offset, this.shortName);
        _hidl_blob.putInt32(96 + _hidl_offset, this.rsrp);
        _hidl_blob.putInt32(100 + _hidl_offset, this.rsrq);
        _hidl_blob.putInt32(104 + _hidl_offset, this.csgType);
        _hidl_blob.putBool(108 + _hidl_offset, this.isConnected);
    }
}
