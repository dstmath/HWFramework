package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class StaScanResult {
    public short beaconPeriodInMs;
    public byte[] bssid = new byte[6];
    public short capability;
    public int frequency;
    public ArrayList<WifiInformationElement> informationElements = new ArrayList<>();
    public int rssi;
    public ArrayList<Byte> ssid = new ArrayList<>();
    public long timeStampInUs;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != StaScanResult.class) {
            return false;
        }
        StaScanResult other = (StaScanResult) otherObject;
        if (this.timeStampInUs == other.timeStampInUs && HidlSupport.deepEquals(this.ssid, other.ssid) && HidlSupport.deepEquals(this.bssid, other.bssid) && this.rssi == other.rssi && this.frequency == other.frequency && this.beaconPeriodInMs == other.beaconPeriodInMs && this.capability == other.capability && HidlSupport.deepEquals(this.informationElements, other.informationElements)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.timeStampInUs))), Integer.valueOf(HidlSupport.deepHashCode(this.ssid)), Integer.valueOf(HidlSupport.deepHashCode(this.bssid)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rssi))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.frequency))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.beaconPeriodInMs))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.capability))), Integer.valueOf(HidlSupport.deepHashCode(this.informationElements)));
    }

    public final String toString() {
        return "{.timeStampInUs = " + this.timeStampInUs + ", .ssid = " + this.ssid + ", .bssid = " + Arrays.toString(this.bssid) + ", .rssi = " + this.rssi + ", .frequency = " + this.frequency + ", .beaconPeriodInMs = " + ((int) this.beaconPeriodInMs) + ", .capability = " + ((int) this.capability) + ", .informationElements = " + this.informationElements + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(64), 0);
    }

    public static final ArrayList<StaScanResult> readVectorFromParcel(HwParcel parcel) {
        ArrayList<StaScanResult> _hidl_vec = new ArrayList<>();
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
        this.timeStampInUs = _hidl_blob.getInt64(_hidl_offset + 0);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 8 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
        this.ssid.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.ssid.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        _hidl_blob.copyToInt8Array(_hidl_offset + 24, this.bssid, 6);
        this.rssi = _hidl_blob.getInt32(_hidl_offset + 32);
        this.frequency = _hidl_blob.getInt32(_hidl_offset + 36);
        this.beaconPeriodInMs = _hidl_blob.getInt16(_hidl_offset + 40);
        this.capability = _hidl_blob.getInt16(_hidl_offset + 42);
        int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 48 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 24), _hidl_blob.handle(), _hidl_offset + 48 + 0, true);
        this.informationElements.clear();
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            WifiInformationElement _hidl_vec_element = new WifiInformationElement();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob2, (long) (_hidl_index_02 * 24));
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
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 64));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt64(_hidl_offset + 0, this.timeStampInUs);
        int _hidl_vec_size = this.ssid.size();
        _hidl_blob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 8 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), this.ssid.get(_hidl_index_0).byteValue());
        }
        _hidl_blob.putBlob(_hidl_offset + 8 + 0, childBlob);
        long _hidl_array_offset_0 = _hidl_offset + 24;
        byte[] _hidl_array_item_0 = this.bssid;
        if (_hidl_array_item_0 == null || _hidl_array_item_0.length != 6) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_0, _hidl_array_item_0);
        _hidl_blob.putInt32(_hidl_offset + 32, this.rssi);
        _hidl_blob.putInt32(_hidl_offset + 36, this.frequency);
        _hidl_blob.putInt16(_hidl_offset + 40, this.beaconPeriodInMs);
        _hidl_blob.putInt16(_hidl_offset + 42, this.capability);
        int _hidl_vec_size2 = this.informationElements.size();
        _hidl_blob.putInt32(_hidl_offset + 48 + 8, _hidl_vec_size2);
        _hidl_blob.putBool(_hidl_offset + 48 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 24);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            this.informationElements.get(_hidl_index_02).writeEmbeddedToBlob(childBlob2, (long) (_hidl_index_02 * 24));
        }
        _hidl_blob.putBlob(_hidl_offset + 48 + 0, childBlob2);
    }
}
