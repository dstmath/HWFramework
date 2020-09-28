package vendor.huawei.hardware.hisiradio.V1_2;

import android.hardware.radio.V1_0.ApnAuthType;
import android.hardware.radio.V1_0.ApnTypes;
import android.hardware.radio.V1_0.DataProfileId;
import android.hardware.radio.V1_0.DataProfileInfoType;
import android.hardware.radio.V1_0.RadioAccessFamily;
import android.hardware.radio.V1_4.PdpProtocolType;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class DataProfileInfo {
    public String apn = new String();
    public int authType;
    public int bearerBitmap;
    public boolean enabled;
    public int maxConns;
    public int maxConnsTime;
    public int mtu;
    public String password = new String();
    public boolean persistent;
    public boolean preferred;
    public int profileId;
    public int protocol;
    public int roamingProtocol;
    public String sNssai = new String();
    public int sscMode;
    public int supportedApnTypesBitmap;
    public int type;
    public String user = new String();
    public int waitTime;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != DataProfileInfo.class) {
            return false;
        }
        DataProfileInfo other = (DataProfileInfo) otherObject;
        if (this.profileId == other.profileId && HidlSupport.deepEquals(this.apn, other.apn) && this.protocol == other.protocol && this.roamingProtocol == other.roamingProtocol && this.authType == other.authType && HidlSupport.deepEquals(this.user, other.user) && HidlSupport.deepEquals(this.password, other.password) && this.type == other.type && this.maxConnsTime == other.maxConnsTime && this.maxConns == other.maxConns && this.waitTime == other.waitTime && this.enabled == other.enabled && HidlSupport.deepEquals(Integer.valueOf(this.supportedApnTypesBitmap), Integer.valueOf(other.supportedApnTypesBitmap)) && HidlSupport.deepEquals(Integer.valueOf(this.bearerBitmap), Integer.valueOf(other.bearerBitmap)) && this.mtu == other.mtu && this.preferred == other.preferred && this.persistent == other.persistent && HidlSupport.deepEquals(this.sNssai, other.sNssai) && this.sscMode == other.sscMode) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.profileId))), Integer.valueOf(HidlSupport.deepHashCode(this.apn)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.protocol))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.roamingProtocol))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.authType))), Integer.valueOf(HidlSupport.deepHashCode(this.user)), Integer.valueOf(HidlSupport.deepHashCode(this.password)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.type))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxConnsTime))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxConns))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.waitTime))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.enabled))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.supportedApnTypesBitmap))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.bearerBitmap))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.mtu))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.preferred))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.persistent))), Integer.valueOf(HidlSupport.deepHashCode(this.sNssai)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sscMode))));
    }

    public final String toString() {
        return "{" + ".profileId = " + DataProfileId.toString(this.profileId) + ", .apn = " + this.apn + ", .protocol = " + PdpProtocolType.toString(this.protocol) + ", .roamingProtocol = " + PdpProtocolType.toString(this.roamingProtocol) + ", .authType = " + ApnAuthType.toString(this.authType) + ", .user = " + this.user + ", .password = " + this.password + ", .type = " + DataProfileInfoType.toString(this.type) + ", .maxConnsTime = " + this.maxConnsTime + ", .maxConns = " + this.maxConns + ", .waitTime = " + this.waitTime + ", .enabled = " + this.enabled + ", .supportedApnTypesBitmap = " + ApnTypes.dumpBitfield(this.supportedApnTypesBitmap) + ", .bearerBitmap = " + RadioAccessFamily.dumpBitfield(this.bearerBitmap) + ", .mtu = " + this.mtu + ", .preferred = " + this.preferred + ", .persistent = " + this.persistent + ", .sNssai = " + this.sNssai + ", .sscMode = " + this.sscMode + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(136), 0);
    }

    public static final ArrayList<DataProfileInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<DataProfileInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 136), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            DataProfileInfo _hidl_vec_element = new DataProfileInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 136));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.profileId = _hidl_blob.getInt32(_hidl_offset + 0);
        this.apn = _hidl_blob.getString(_hidl_offset + 8);
        parcel.readEmbeddedBuffer((long) (this.apn.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 8 + 0, false);
        this.protocol = _hidl_blob.getInt32(_hidl_offset + 24);
        this.roamingProtocol = _hidl_blob.getInt32(_hidl_offset + 28);
        this.authType = _hidl_blob.getInt32(_hidl_offset + 32);
        this.user = _hidl_blob.getString(_hidl_offset + 40);
        parcel.readEmbeddedBuffer((long) (this.user.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 40 + 0, false);
        this.password = _hidl_blob.getString(_hidl_offset + 56);
        parcel.readEmbeddedBuffer((long) (this.password.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 56 + 0, false);
        this.type = _hidl_blob.getInt32(_hidl_offset + 72);
        this.maxConnsTime = _hidl_blob.getInt32(_hidl_offset + 76);
        this.maxConns = _hidl_blob.getInt32(_hidl_offset + 80);
        this.waitTime = _hidl_blob.getInt32(_hidl_offset + 84);
        this.enabled = _hidl_blob.getBool(_hidl_offset + 88);
        this.supportedApnTypesBitmap = _hidl_blob.getInt32(_hidl_offset + 92);
        this.bearerBitmap = _hidl_blob.getInt32(_hidl_offset + 96);
        this.mtu = _hidl_blob.getInt32(_hidl_offset + 100);
        this.preferred = _hidl_blob.getBool(_hidl_offset + 104);
        this.persistent = _hidl_blob.getBool(_hidl_offset + 105);
        this.sNssai = _hidl_blob.getString(_hidl_offset + 112);
        parcel.readEmbeddedBuffer((long) (this.sNssai.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 112 + 0, false);
        this.sscMode = _hidl_blob.getInt32(_hidl_offset + 128);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(136);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<DataProfileInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 136);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 136));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.profileId);
        _hidl_blob.putString(8 + _hidl_offset, this.apn);
        _hidl_blob.putInt32(24 + _hidl_offset, this.protocol);
        _hidl_blob.putInt32(28 + _hidl_offset, this.roamingProtocol);
        _hidl_blob.putInt32(32 + _hidl_offset, this.authType);
        _hidl_blob.putString(40 + _hidl_offset, this.user);
        _hidl_blob.putString(56 + _hidl_offset, this.password);
        _hidl_blob.putInt32(72 + _hidl_offset, this.type);
        _hidl_blob.putInt32(76 + _hidl_offset, this.maxConnsTime);
        _hidl_blob.putInt32(80 + _hidl_offset, this.maxConns);
        _hidl_blob.putInt32(84 + _hidl_offset, this.waitTime);
        _hidl_blob.putBool(88 + _hidl_offset, this.enabled);
        _hidl_blob.putInt32(92 + _hidl_offset, this.supportedApnTypesBitmap);
        _hidl_blob.putInt32(96 + _hidl_offset, this.bearerBitmap);
        _hidl_blob.putInt32(100 + _hidl_offset, this.mtu);
        _hidl_blob.putBool(104 + _hidl_offset, this.preferred);
        _hidl_blob.putBool(105 + _hidl_offset, this.persistent);
        _hidl_blob.putString(112 + _hidl_offset, this.sNssai);
        _hidl_blob.putInt32(128 + _hidl_offset, this.sscMode);
    }
}
