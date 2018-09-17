package android.hardware.radio.V1_0;

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
    public String mvnoMatchData = new String();
    public int mvnoType;
    public String password = new String();
    public int profileId;
    public String protocol = new String();
    public String roamingProtocol = new String();
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
        return this.profileId == other.profileId && HidlSupport.deepEquals(this.apn, other.apn) && HidlSupport.deepEquals(this.protocol, other.protocol) && HidlSupport.deepEquals(this.roamingProtocol, other.roamingProtocol) && this.authType == other.authType && HidlSupport.deepEquals(this.user, other.user) && HidlSupport.deepEquals(this.password, other.password) && this.type == other.type && this.maxConnsTime == other.maxConnsTime && this.maxConns == other.maxConns && this.waitTime == other.waitTime && this.enabled == other.enabled && HidlSupport.deepEquals(Integer.valueOf(this.supportedApnTypesBitmap), Integer.valueOf(other.supportedApnTypesBitmap)) && HidlSupport.deepEquals(Integer.valueOf(this.bearerBitmap), Integer.valueOf(other.bearerBitmap)) && this.mtu == other.mtu && this.mvnoType == other.mvnoType && HidlSupport.deepEquals(this.mvnoMatchData, other.mvnoMatchData);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.profileId))), Integer.valueOf(HidlSupport.deepHashCode(this.apn)), Integer.valueOf(HidlSupport.deepHashCode(this.protocol)), Integer.valueOf(HidlSupport.deepHashCode(this.roamingProtocol)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.authType))), Integer.valueOf(HidlSupport.deepHashCode(this.user)), Integer.valueOf(HidlSupport.deepHashCode(this.password)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.type))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxConnsTime))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxConns))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.waitTime))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.enabled))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.supportedApnTypesBitmap))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.bearerBitmap))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.mtu))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.mvnoType))), Integer.valueOf(HidlSupport.deepHashCode(this.mvnoMatchData))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".profileId = ");
        builder.append(DataProfileId.toString(this.profileId));
        builder.append(", .apn = ");
        builder.append(this.apn);
        builder.append(", .protocol = ");
        builder.append(this.protocol);
        builder.append(", .roamingProtocol = ");
        builder.append(this.roamingProtocol);
        builder.append(", .authType = ");
        builder.append(ApnAuthType.toString(this.authType));
        builder.append(", .user = ");
        builder.append(this.user);
        builder.append(", .password = ");
        builder.append(this.password);
        builder.append(", .type = ");
        builder.append(DataProfileInfoType.toString(this.type));
        builder.append(", .maxConnsTime = ");
        builder.append(this.maxConnsTime);
        builder.append(", .maxConns = ");
        builder.append(this.maxConns);
        builder.append(", .waitTime = ");
        builder.append(this.waitTime);
        builder.append(", .enabled = ");
        builder.append(this.enabled);
        builder.append(", .supportedApnTypesBitmap = ");
        builder.append(ApnTypes.dumpBitfield(this.supportedApnTypesBitmap));
        builder.append(", .bearerBitmap = ");
        builder.append(RadioAccessFamily.dumpBitfield(this.bearerBitmap));
        builder.append(", .mtu = ");
        builder.append(this.mtu);
        builder.append(", .mvnoType = ");
        builder.append(MvnoType.toString(this.mvnoType));
        builder.append(", .mvnoMatchData = ");
        builder.append(this.mvnoMatchData);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(152), 0);
    }

    public static final ArrayList<DataProfileInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<DataProfileInfo> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 152), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            DataProfileInfo _hidl_vec_element = new DataProfileInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 152));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.profileId = _hidl_blob.getInt32(0 + _hidl_offset);
        this.apn = _hidl_blob.getString(8 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.apn.getBytes().length + 1), _hidl_blob.handle(), 0 + (8 + _hidl_offset), false);
        this.protocol = _hidl_blob.getString(24 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.protocol.getBytes().length + 1), _hidl_blob.handle(), 0 + (24 + _hidl_offset), false);
        this.roamingProtocol = _hidl_blob.getString(40 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.roamingProtocol.getBytes().length + 1), _hidl_blob.handle(), 0 + (40 + _hidl_offset), false);
        this.authType = _hidl_blob.getInt32(56 + _hidl_offset);
        this.user = _hidl_blob.getString(64 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.user.getBytes().length + 1), _hidl_blob.handle(), 0 + (64 + _hidl_offset), false);
        this.password = _hidl_blob.getString(80 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.password.getBytes().length + 1), _hidl_blob.handle(), 0 + (80 + _hidl_offset), false);
        this.type = _hidl_blob.getInt32(96 + _hidl_offset);
        this.maxConnsTime = _hidl_blob.getInt32(100 + _hidl_offset);
        this.maxConns = _hidl_blob.getInt32(104 + _hidl_offset);
        this.waitTime = _hidl_blob.getInt32(108 + _hidl_offset);
        this.enabled = _hidl_blob.getBool(112 + _hidl_offset);
        this.supportedApnTypesBitmap = _hidl_blob.getInt32(116 + _hidl_offset);
        this.bearerBitmap = _hidl_blob.getInt32(120 + _hidl_offset);
        this.mtu = _hidl_blob.getInt32(124 + _hidl_offset);
        this.mvnoType = _hidl_blob.getInt32(128 + _hidl_offset);
        this.mvnoMatchData = _hidl_blob.getString(136 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.mvnoMatchData.getBytes().length + 1), _hidl_blob.handle(), 0 + (136 + _hidl_offset), false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(152);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<DataProfileInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 152);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((DataProfileInfo) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 152));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.profileId);
        _hidl_blob.putString(8 + _hidl_offset, this.apn);
        _hidl_blob.putString(24 + _hidl_offset, this.protocol);
        _hidl_blob.putString(40 + _hidl_offset, this.roamingProtocol);
        _hidl_blob.putInt32(56 + _hidl_offset, this.authType);
        _hidl_blob.putString(64 + _hidl_offset, this.user);
        _hidl_blob.putString(80 + _hidl_offset, this.password);
        _hidl_blob.putInt32(96 + _hidl_offset, this.type);
        _hidl_blob.putInt32(100 + _hidl_offset, this.maxConnsTime);
        _hidl_blob.putInt32(104 + _hidl_offset, this.maxConns);
        _hidl_blob.putInt32(108 + _hidl_offset, this.waitTime);
        _hidl_blob.putBool(112 + _hidl_offset, this.enabled);
        _hidl_blob.putInt32(116 + _hidl_offset, this.supportedApnTypesBitmap);
        _hidl_blob.putInt32(120 + _hidl_offset, this.bearerBitmap);
        _hidl_blob.putInt32(124 + _hidl_offset, this.mtu);
        _hidl_blob.putInt32(128 + _hidl_offset, this.mvnoType);
        _hidl_blob.putString(136 + _hidl_offset, this.mvnoMatchData);
    }
}
