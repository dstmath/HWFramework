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
    public final ArrayList<WifiInformationElement> informationElements = new ArrayList<>();
    public int rssi;
    public final ArrayList<Byte> ssid = new ArrayList<>();
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
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.timeStampInUs))), Integer.valueOf(HidlSupport.deepHashCode(this.ssid)), Integer.valueOf(HidlSupport.deepHashCode(this.bssid)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rssi))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.frequency))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.beaconPeriodInMs))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.capability))), Integer.valueOf(HidlSupport.deepHashCode(this.informationElements))});
    }

    public final String toString() {
        return "{" + ".timeStampInUs = " + this.timeStampInUs + ", .ssid = " + this.ssid + ", .bssid = " + Arrays.toString(this.bssid) + ", .rssi = " + this.rssi + ", .frequency = " + this.frequency + ", .beaconPeriodInMs = " + this.beaconPeriodInMs + ", .capability = " + this.capability + ", .informationElements = " + this.informationElements + "}";
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
        HwBlob hwBlob = _hidl_blob;
        this.timeStampInUs = hwBlob.getInt64(_hidl_offset + 0);
        int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 8 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
        this.ssid.clear();
        int _hidl_index_0 = 0;
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size; _hidl_index_02++) {
            this.ssid.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_02 * 1))));
        }
        hwBlob.copyToInt8Array(_hidl_offset + 24, this.bssid, 6);
        this.rssi = hwBlob.getInt32(_hidl_offset + 32);
        this.frequency = hwBlob.getInt32(_hidl_offset + 36);
        this.beaconPeriodInMs = hwBlob.getInt16(_hidl_offset + 40);
        this.capability = hwBlob.getInt16(_hidl_offset + 42);
        int _hidl_vec_size2 = hwBlob.getInt32(_hidl_offset + 48 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 24), _hidl_blob.handle(), _hidl_offset + 48 + 0, true);
        this.informationElements.clear();
        while (true) {
            int _hidl_index_03 = _hidl_index_0;
            if (_hidl_index_03 < _hidl_vec_size2) {
                WifiInformationElement _hidl_vec_element = new WifiInformationElement();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob2, (long) (_hidl_index_03 * 24));
                this.informationElements.add(_hidl_vec_element);
                _hidl_index_0 = _hidl_index_03 + 1;
            } else {
                HwParcel hwParcel = parcel;
                return;
            }
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
        HwBlob hwBlob = _hidl_blob;
        hwBlob.putInt64(_hidl_offset + 0, this.timeStampInUs);
        int _hidl_vec_size = this.ssid.size();
        hwBlob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
        hwBlob.putBool(_hidl_offset + 8 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), this.ssid.get(_hidl_index_0).byteValue());
        }
        hwBlob.putBlob(_hidl_offset + 8 + 0, childBlob);
        hwBlob.putInt8Array(_hidl_offset + 24, this.bssid);
        hwBlob.putInt32(_hidl_offset + 32, this.rssi);
        hwBlob.putInt32(_hidl_offset + 36, this.frequency);
        hwBlob.putInt16(_hidl_offset + 40, this.beaconPeriodInMs);
        hwBlob.putInt16(_hidl_offset + 42, this.capability);
        int _hidl_vec_size2 = this.informationElements.size();
        hwBlob.putInt32(_hidl_offset + 48 + 8, _hidl_vec_size2);
        int _hidl_index_02 = 0;
        hwBlob.putBool(_hidl_offset + 48 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 24);
        while (true) {
            int _hidl_index_03 = _hidl_index_02;
            if (_hidl_index_03 < _hidl_vec_size2) {
                this.informationElements.get(_hidl_index_03).writeEmbeddedToBlob(childBlob2, (long) (_hidl_index_03 * 24));
                _hidl_index_02 = _hidl_index_03 + 1;
            } else {
                hwBlob.putBlob(_hidl_offset + 48 + 0, childBlob2);
                return;
            }
        }
    }
}
