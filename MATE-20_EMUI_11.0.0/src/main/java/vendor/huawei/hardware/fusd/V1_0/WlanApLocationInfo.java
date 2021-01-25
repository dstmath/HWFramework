package vendor.huawei.hardware.fusd.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class WlanApLocationInfo {
    public int apChannelFrequency;
    public LppeBitString apMACAddress = new LppeBitString();
    public byte apSignalStrength;
    public byte apSignalStrengthDelta;
    public boolean isApChannelFrequency;
    public boolean isApSignalStrength;
    public boolean isApSignalStrengthDelta;
    public boolean isRelativeTimeStamp;
    public int servingFlag;
    public short usRelativeTimeStamp;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != WlanApLocationInfo.class) {
            return false;
        }
        WlanApLocationInfo other = (WlanApLocationInfo) otherObject;
        if (this.isRelativeTimeStamp == other.isRelativeTimeStamp && this.usRelativeTimeStamp == other.usRelativeTimeStamp && this.servingFlag == other.servingFlag && HidlSupport.deepEquals(this.apMACAddress, other.apMACAddress) && this.isApSignalStrength == other.isApSignalStrength && this.apSignalStrength == other.apSignalStrength && this.isApChannelFrequency == other.isApChannelFrequency && this.apChannelFrequency == other.apChannelFrequency && this.isApSignalStrengthDelta == other.isApSignalStrengthDelta && this.apSignalStrengthDelta == other.apSignalStrengthDelta) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isRelativeTimeStamp))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.usRelativeTimeStamp))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.servingFlag))), Integer.valueOf(HidlSupport.deepHashCode(this.apMACAddress)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isApSignalStrength))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.apSignalStrength))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isApChannelFrequency))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.apChannelFrequency))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isApSignalStrengthDelta))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.apSignalStrengthDelta))));
    }

    public final String toString() {
        return "{.isRelativeTimeStamp = " + this.isRelativeTimeStamp + ", .usRelativeTimeStamp = " + ((int) this.usRelativeTimeStamp) + ", .servingFlag = " + this.servingFlag + ", .apMACAddress = " + this.apMACAddress + ", .isApSignalStrength = " + this.isApSignalStrength + ", .apSignalStrength = " + ((int) this.apSignalStrength) + ", .isApChannelFrequency = " + this.isApChannelFrequency + ", .apChannelFrequency = " + this.apChannelFrequency + ", .isApSignalStrengthDelta = " + this.isApSignalStrengthDelta + ", .apSignalStrengthDelta = " + ((int) this.apSignalStrengthDelta) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<WlanApLocationInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<WlanApLocationInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            WlanApLocationInfo _hidl_vec_element = new WlanApLocationInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.isRelativeTimeStamp = _hidl_blob.getBool(0 + _hidl_offset);
        this.usRelativeTimeStamp = _hidl_blob.getInt16(2 + _hidl_offset);
        this.servingFlag = _hidl_blob.getInt32(4 + _hidl_offset);
        this.apMACAddress.readEmbeddedFromParcel(parcel, _hidl_blob, 8 + _hidl_offset);
        this.isApSignalStrength = _hidl_blob.getBool(32 + _hidl_offset);
        this.apSignalStrength = _hidl_blob.getInt8(33 + _hidl_offset);
        this.isApChannelFrequency = _hidl_blob.getBool(34 + _hidl_offset);
        this.apChannelFrequency = _hidl_blob.getInt32(36 + _hidl_offset);
        this.isApSignalStrengthDelta = _hidl_blob.getBool(40 + _hidl_offset);
        this.apSignalStrengthDelta = _hidl_blob.getInt8(41 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<WlanApLocationInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putBool(0 + _hidl_offset, this.isRelativeTimeStamp);
        _hidl_blob.putInt16(2 + _hidl_offset, this.usRelativeTimeStamp);
        _hidl_blob.putInt32(4 + _hidl_offset, this.servingFlag);
        this.apMACAddress.writeEmbeddedToBlob(_hidl_blob, 8 + _hidl_offset);
        _hidl_blob.putBool(32 + _hidl_offset, this.isApSignalStrength);
        _hidl_blob.putInt8(33 + _hidl_offset, this.apSignalStrength);
        _hidl_blob.putBool(34 + _hidl_offset, this.isApChannelFrequency);
        _hidl_blob.putInt32(36 + _hidl_offset, this.apChannelFrequency);
        _hidl_blob.putBool(40 + _hidl_offset, this.isApSignalStrengthDelta);
        _hidl_blob.putInt8(41 + _hidl_offset, this.apSignalStrengthDelta);
    }
}
