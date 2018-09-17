package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import com.android.server.wifi.WifiConfigManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class RttResult {
    public final byte[] addr = new byte[6];
    public int burstDurationInMs;
    public int burstNum;
    public int distanceInMm;
    public int distanceSdInMm;
    public int distanceSpreadInMm;
    public final WifiInformationElement lci = new WifiInformationElement();
    public final WifiInformationElement lcr = new WifiInformationElement();
    public int measurementNumber;
    public int negotiatedBurstNum;
    public byte numberPerBurstPeer;
    public byte retryAfterDuration;
    public int rssi;
    public int rssiSpread;
    public long rtt;
    public long rttSd;
    public long rttSpread;
    public final WifiRateInfo rxRate = new WifiRateInfo();
    public int status;
    public int successNumber;
    public long timeStampInUs;
    public final WifiRateInfo txRate = new WifiRateInfo();
    public int type;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RttResult.class) {
            return false;
        }
        RttResult other = (RttResult) otherObject;
        return HidlSupport.deepEquals(this.addr, other.addr) && this.burstNum == other.burstNum && this.measurementNumber == other.measurementNumber && this.successNumber == other.successNumber && this.numberPerBurstPeer == other.numberPerBurstPeer && this.status == other.status && this.retryAfterDuration == other.retryAfterDuration && this.type == other.type && this.rssi == other.rssi && this.rssiSpread == other.rssiSpread && HidlSupport.deepEquals(this.txRate, other.txRate) && HidlSupport.deepEquals(this.rxRate, other.rxRate) && this.rtt == other.rtt && this.rttSd == other.rttSd && this.rttSpread == other.rttSpread && this.distanceInMm == other.distanceInMm && this.distanceSdInMm == other.distanceSdInMm && this.distanceSpreadInMm == other.distanceSpreadInMm && this.timeStampInUs == other.timeStampInUs && this.burstDurationInMs == other.burstDurationInMs && this.negotiatedBurstNum == other.negotiatedBurstNum && HidlSupport.deepEquals(this.lci, other.lci) && HidlSupport.deepEquals(this.lcr, other.lcr);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.addr)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.burstNum))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.measurementNumber))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.successNumber))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.numberPerBurstPeer))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.status))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.retryAfterDuration))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.type))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rssi))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rssiSpread))), Integer.valueOf(HidlSupport.deepHashCode(this.txRate)), Integer.valueOf(HidlSupport.deepHashCode(this.rxRate)), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.rtt))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.rttSd))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.rttSpread))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.distanceInMm))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.distanceSdInMm))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.distanceSpreadInMm))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.timeStampInUs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.burstDurationInMs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.negotiatedBurstNum))), Integer.valueOf(HidlSupport.deepHashCode(this.lci)), Integer.valueOf(HidlSupport.deepHashCode(this.lcr))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".addr = ");
        builder.append(Arrays.toString(this.addr));
        builder.append(", .burstNum = ");
        builder.append(this.burstNum);
        builder.append(", .measurementNumber = ");
        builder.append(this.measurementNumber);
        builder.append(", .successNumber = ");
        builder.append(this.successNumber);
        builder.append(", .numberPerBurstPeer = ");
        builder.append(this.numberPerBurstPeer);
        builder.append(", .status = ");
        builder.append(RttStatus.toString(this.status));
        builder.append(", .retryAfterDuration = ");
        builder.append(this.retryAfterDuration);
        builder.append(", .type = ");
        builder.append(RttType.toString(this.type));
        builder.append(", .rssi = ");
        builder.append(this.rssi);
        builder.append(", .rssiSpread = ");
        builder.append(this.rssiSpread);
        builder.append(", .txRate = ");
        builder.append(this.txRate);
        builder.append(", .rxRate = ");
        builder.append(this.rxRate);
        builder.append(", .rtt = ");
        builder.append(this.rtt);
        builder.append(", .rttSd = ");
        builder.append(this.rttSd);
        builder.append(", .rttSpread = ");
        builder.append(this.rttSpread);
        builder.append(", .distanceInMm = ");
        builder.append(this.distanceInMm);
        builder.append(", .distanceSdInMm = ");
        builder.append(this.distanceSdInMm);
        builder.append(", .distanceSpreadInMm = ");
        builder.append(this.distanceSpreadInMm);
        builder.append(", .timeStampInUs = ");
        builder.append(this.timeStampInUs);
        builder.append(", .burstDurationInMs = ");
        builder.append(this.burstDurationInMs);
        builder.append(", .negotiatedBurstNum = ");
        builder.append(this.negotiatedBurstNum);
        builder.append(", .lci = ");
        builder.append(this.lci);
        builder.append(", .lcr = ");
        builder.append(this.lcr);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(192), 0);
    }

    public static final ArrayList<RttResult> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RttResult> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * WifiConfigManager.SCAN_CACHE_ENTRIES_MAX_SIZE), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RttResult _hidl_vec_element = new RttResult();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * WifiConfigManager.SCAN_CACHE_ENTRIES_MAX_SIZE));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        long _hidl_array_offset_0 = _hidl_offset + 0;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
            this.addr[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        this.burstNum = _hidl_blob.getInt32(8 + _hidl_offset);
        this.measurementNumber = _hidl_blob.getInt32(12 + _hidl_offset);
        this.successNumber = _hidl_blob.getInt32(16 + _hidl_offset);
        this.numberPerBurstPeer = _hidl_blob.getInt8(20 + _hidl_offset);
        this.status = _hidl_blob.getInt32(24 + _hidl_offset);
        this.retryAfterDuration = _hidl_blob.getInt8(28 + _hidl_offset);
        this.type = _hidl_blob.getInt32(32 + _hidl_offset);
        this.rssi = _hidl_blob.getInt32(36 + _hidl_offset);
        this.rssiSpread = _hidl_blob.getInt32(40 + _hidl_offset);
        this.txRate.readEmbeddedFromParcel(parcel, _hidl_blob, 44 + _hidl_offset);
        this.rxRate.readEmbeddedFromParcel(parcel, _hidl_blob, 64 + _hidl_offset);
        this.rtt = _hidl_blob.getInt64(88 + _hidl_offset);
        this.rttSd = _hidl_blob.getInt64(96 + _hidl_offset);
        this.rttSpread = _hidl_blob.getInt64(104 + _hidl_offset);
        this.distanceInMm = _hidl_blob.getInt32(112 + _hidl_offset);
        this.distanceSdInMm = _hidl_blob.getInt32(116 + _hidl_offset);
        this.distanceSpreadInMm = _hidl_blob.getInt32(120 + _hidl_offset);
        this.timeStampInUs = _hidl_blob.getInt64(128 + _hidl_offset);
        this.burstDurationInMs = _hidl_blob.getInt32(136 + _hidl_offset);
        this.negotiatedBurstNum = _hidl_blob.getInt32(140 + _hidl_offset);
        this.lci.readEmbeddedFromParcel(parcel, _hidl_blob, 144 + _hidl_offset);
        this.lcr.readEmbeddedFromParcel(parcel, _hidl_blob, 168 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(WifiConfigManager.SCAN_CACHE_ENTRIES_MAX_SIZE);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RttResult> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * WifiConfigManager.SCAN_CACHE_ENTRIES_MAX_SIZE);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RttResult) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * WifiConfigManager.SCAN_CACHE_ENTRIES_MAX_SIZE));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        long _hidl_array_offset_0 = _hidl_offset + 0;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.addr[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_blob.putInt32(8 + _hidl_offset, this.burstNum);
        _hidl_blob.putInt32(12 + _hidl_offset, this.measurementNumber);
        _hidl_blob.putInt32(16 + _hidl_offset, this.successNumber);
        _hidl_blob.putInt8(20 + _hidl_offset, this.numberPerBurstPeer);
        _hidl_blob.putInt32(24 + _hidl_offset, this.status);
        _hidl_blob.putInt8(28 + _hidl_offset, this.retryAfterDuration);
        _hidl_blob.putInt32(32 + _hidl_offset, this.type);
        _hidl_blob.putInt32(36 + _hidl_offset, this.rssi);
        _hidl_blob.putInt32(40 + _hidl_offset, this.rssiSpread);
        this.txRate.writeEmbeddedToBlob(_hidl_blob, 44 + _hidl_offset);
        this.rxRate.writeEmbeddedToBlob(_hidl_blob, 64 + _hidl_offset);
        _hidl_blob.putInt64(88 + _hidl_offset, this.rtt);
        _hidl_blob.putInt64(96 + _hidl_offset, this.rttSd);
        _hidl_blob.putInt64(104 + _hidl_offset, this.rttSpread);
        _hidl_blob.putInt32(112 + _hidl_offset, this.distanceInMm);
        _hidl_blob.putInt32(116 + _hidl_offset, this.distanceSdInMm);
        _hidl_blob.putInt32(120 + _hidl_offset, this.distanceSpreadInMm);
        _hidl_blob.putInt64(128 + _hidl_offset, this.timeStampInUs);
        _hidl_blob.putInt32(136 + _hidl_offset, this.burstDurationInMs);
        _hidl_blob.putInt32(140 + _hidl_offset, this.negotiatedBurstNum);
        this.lci.writeEmbeddedToBlob(_hidl_blob, 144 + _hidl_offset);
        this.lcr.writeEmbeddedToBlob(_hidl_blob, 168 + _hidl_offset);
    }
}
