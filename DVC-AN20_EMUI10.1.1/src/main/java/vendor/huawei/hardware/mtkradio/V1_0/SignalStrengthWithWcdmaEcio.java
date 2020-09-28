package vendor.huawei.hardware.mtkradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class SignalStrengthWithWcdmaEcio {
    public int cdma_dbm;
    public int cdma_ecio;
    public int evdo_dbm;
    public int evdo_ecio;
    public int evdo_signalNoiseRatio;
    public int gsm_bitErrorRate;
    public int gsm_signalStrength;
    public int lte_cqi;
    public int lte_rsrp;
    public int lte_rsrq;
    public int lte_rssnr;
    public int lte_signalStrength;
    public int tdscdma_rscp;
    public int wcdma_ecio;
    public int wcdma_rscp;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != SignalStrengthWithWcdmaEcio.class) {
            return false;
        }
        SignalStrengthWithWcdmaEcio other = (SignalStrengthWithWcdmaEcio) otherObject;
        if (this.gsm_signalStrength == other.gsm_signalStrength && this.gsm_bitErrorRate == other.gsm_bitErrorRate && this.wcdma_rscp == other.wcdma_rscp && this.wcdma_ecio == other.wcdma_ecio && this.cdma_dbm == other.cdma_dbm && this.cdma_ecio == other.cdma_ecio && this.evdo_dbm == other.evdo_dbm && this.evdo_ecio == other.evdo_ecio && this.evdo_signalNoiseRatio == other.evdo_signalNoiseRatio && this.lte_signalStrength == other.lte_signalStrength && this.lte_rsrp == other.lte_rsrp && this.lte_rsrq == other.lte_rsrq && this.lte_rssnr == other.lte_rssnr && this.lte_cqi == other.lte_cqi && this.tdscdma_rscp == other.tdscdma_rscp) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.gsm_signalStrength))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.gsm_bitErrorRate))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.wcdma_rscp))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.wcdma_ecio))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cdma_dbm))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cdma_ecio))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.evdo_dbm))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.evdo_ecio))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.evdo_signalNoiseRatio))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.lte_signalStrength))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.lte_rsrp))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.lte_rsrq))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.lte_rssnr))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.lte_cqi))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.tdscdma_rscp))));
    }

    public final String toString() {
        return "{" + ".gsm_signalStrength = " + this.gsm_signalStrength + ", .gsm_bitErrorRate = " + this.gsm_bitErrorRate + ", .wcdma_rscp = " + this.wcdma_rscp + ", .wcdma_ecio = " + this.wcdma_ecio + ", .cdma_dbm = " + this.cdma_dbm + ", .cdma_ecio = " + this.cdma_ecio + ", .evdo_dbm = " + this.evdo_dbm + ", .evdo_ecio = " + this.evdo_ecio + ", .evdo_signalNoiseRatio = " + this.evdo_signalNoiseRatio + ", .lte_signalStrength = " + this.lte_signalStrength + ", .lte_rsrp = " + this.lte_rsrp + ", .lte_rsrq = " + this.lte_rsrq + ", .lte_rssnr = " + this.lte_rssnr + ", .lte_cqi = " + this.lte_cqi + ", .tdscdma_rscp = " + this.tdscdma_rscp + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(60), 0);
    }

    public static final ArrayList<SignalStrengthWithWcdmaEcio> readVectorFromParcel(HwParcel parcel) {
        ArrayList<SignalStrengthWithWcdmaEcio> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 60), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            SignalStrengthWithWcdmaEcio _hidl_vec_element = new SignalStrengthWithWcdmaEcio();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 60));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.gsm_signalStrength = _hidl_blob.getInt32(0 + _hidl_offset);
        this.gsm_bitErrorRate = _hidl_blob.getInt32(4 + _hidl_offset);
        this.wcdma_rscp = _hidl_blob.getInt32(8 + _hidl_offset);
        this.wcdma_ecio = _hidl_blob.getInt32(12 + _hidl_offset);
        this.cdma_dbm = _hidl_blob.getInt32(16 + _hidl_offset);
        this.cdma_ecio = _hidl_blob.getInt32(20 + _hidl_offset);
        this.evdo_dbm = _hidl_blob.getInt32(24 + _hidl_offset);
        this.evdo_ecio = _hidl_blob.getInt32(28 + _hidl_offset);
        this.evdo_signalNoiseRatio = _hidl_blob.getInt32(32 + _hidl_offset);
        this.lte_signalStrength = _hidl_blob.getInt32(36 + _hidl_offset);
        this.lte_rsrp = _hidl_blob.getInt32(40 + _hidl_offset);
        this.lte_rsrq = _hidl_blob.getInt32(44 + _hidl_offset);
        this.lte_rssnr = _hidl_blob.getInt32(48 + _hidl_offset);
        this.lte_cqi = _hidl_blob.getInt32(52 + _hidl_offset);
        this.tdscdma_rscp = _hidl_blob.getInt32(56 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(60);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<SignalStrengthWithWcdmaEcio> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 60);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 60));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.gsm_signalStrength);
        _hidl_blob.putInt32(4 + _hidl_offset, this.gsm_bitErrorRate);
        _hidl_blob.putInt32(8 + _hidl_offset, this.wcdma_rscp);
        _hidl_blob.putInt32(12 + _hidl_offset, this.wcdma_ecio);
        _hidl_blob.putInt32(16 + _hidl_offset, this.cdma_dbm);
        _hidl_blob.putInt32(20 + _hidl_offset, this.cdma_ecio);
        _hidl_blob.putInt32(24 + _hidl_offset, this.evdo_dbm);
        _hidl_blob.putInt32(28 + _hidl_offset, this.evdo_ecio);
        _hidl_blob.putInt32(32 + _hidl_offset, this.evdo_signalNoiseRatio);
        _hidl_blob.putInt32(36 + _hidl_offset, this.lte_signalStrength);
        _hidl_blob.putInt32(40 + _hidl_offset, this.lte_rsrp);
        _hidl_blob.putInt32(44 + _hidl_offset, this.lte_rsrq);
        _hidl_blob.putInt32(48 + _hidl_offset, this.lte_rssnr);
        _hidl_blob.putInt32(52 + _hidl_offset, this.lte_cqi);
        _hidl_blob.putInt32(56 + _hidl_offset, this.tdscdma_rscp);
    }
}
