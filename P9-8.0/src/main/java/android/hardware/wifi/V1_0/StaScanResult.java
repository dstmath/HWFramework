package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class StaScanResult {
    public short beaconPeriodInMs;
    public final byte[] bssid = new byte[6];
    public short capability;
    public int frequency;
    public final ArrayList<WifiInformationElement> informationElements = new ArrayList();
    public int rssi;
    public final ArrayList<Byte> ssid = new ArrayList();
    public long timeStampInUs;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != StaScanResult.class) {
            return false;
        }
        StaScanResult other = (StaScanResult) otherObject;
        return this.timeStampInUs == other.timeStampInUs && HidlSupport.deepEquals(this.ssid, other.ssid) && HidlSupport.deepEquals(this.bssid, other.bssid) && this.rssi == other.rssi && this.frequency == other.frequency && this.beaconPeriodInMs == other.beaconPeriodInMs && this.capability == other.capability && HidlSupport.deepEquals(this.informationElements, other.informationElements);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.timeStampInUs))), Integer.valueOf(HidlSupport.deepHashCode(this.ssid)), Integer.valueOf(HidlSupport.deepHashCode(this.bssid)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rssi))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.frequency))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.beaconPeriodInMs))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.capability))), Integer.valueOf(HidlSupport.deepHashCode(this.informationElements))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".timeStampInUs = ");
        builder.append(this.timeStampInUs);
        builder.append(", .ssid = ");
        builder.append(this.ssid);
        builder.append(", .bssid = ");
        builder.append(Arrays.toString(this.bssid));
        builder.append(", .rssi = ");
        builder.append(this.rssi);
        builder.append(", .frequency = ");
        builder.append(this.frequency);
        builder.append(", .beaconPeriodInMs = ");
        builder.append(this.beaconPeriodInMs);
        builder.append(", .capability = ");
        builder.append(this.capability);
        builder.append(", .informationElements = ");
        builder.append(this.informationElements);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(64), 0);
    }

    public static final ArrayList<StaScanResult> readVectorFromParcel(HwParcel parcel) {
        ArrayList<StaScanResult> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 64), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            StaScanResult _hidl_vec_element = new StaScanResult();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 64));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        this.timeStampInUs = _hidl_blob.getInt64(0 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((8 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (8 + _hidl_offset) + 0, true);
        this.ssid.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.ssid.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        long _hidl_array_offset_0 = _hidl_offset + 24;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
            this.bssid[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        this.rssi = _hidl_blob.getInt32(32 + _hidl_offset);
        this.frequency = _hidl_blob.getInt32(36 + _hidl_offset);
        this.beaconPeriodInMs = _hidl_blob.getInt16(40 + _hidl_offset);
        this.capability = _hidl_blob.getInt16(42 + _hidl_offset);
        _hidl_vec_size = _hidl_blob.getInt32((48 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), (48 + _hidl_offset) + 0, true);
        this.informationElements.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            WifiInformationElement _hidl_vec_element = new WifiInformationElement();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            this.informationElements.add(_hidl_vec_element);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(64);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<StaScanResult> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 64);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((StaScanResult) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 64));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        _hidl_blob.putInt64(0 + _hidl_offset, this.timeStampInUs);
        int _hidl_vec_size = this.ssid.size();
        _hidl_blob.putInt32((8 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((8 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.ssid.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((8 + _hidl_offset) + 0, childBlob);
        long _hidl_array_offset_0 = _hidl_offset + 24;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.bssid[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_blob.putInt32(32 + _hidl_offset, this.rssi);
        _hidl_blob.putInt32(36 + _hidl_offset, this.frequency);
        _hidl_blob.putInt16(40 + _hidl_offset, this.beaconPeriodInMs);
        _hidl_blob.putInt16(42 + _hidl_offset, this.capability);
        _hidl_vec_size = this.informationElements.size();
        _hidl_blob.putInt32((48 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((48 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 24);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((WifiInformationElement) this.informationElements.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob((48 + _hidl_offset) + 0, childBlob);
    }
}
