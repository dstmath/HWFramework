package vendor.huawei.hardware.qcomradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CellInfo {
    public ArrayList<CellInfoCdma> cdma = new ArrayList<>();
    public int cellInfoType;
    public ArrayList<CellInfoGsm> gsm = new ArrayList<>();
    public ArrayList<CellInfoLte> lte = new ArrayList<>();
    public boolean registered;
    public ArrayList<CellInfoTdscdma> tdscdma = new ArrayList<>();
    public long timeStamp;
    public int timeStampType;
    public ArrayList<CellInfoWcdma> wcdma = new ArrayList<>();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CellInfo.class) {
            return false;
        }
        CellInfo other = (CellInfo) otherObject;
        if (this.cellInfoType == other.cellInfoType && this.registered == other.registered && this.timeStampType == other.timeStampType && this.timeStamp == other.timeStamp && HidlSupport.deepEquals(this.gsm, other.gsm) && HidlSupport.deepEquals(this.cdma, other.cdma) && HidlSupport.deepEquals(this.lte, other.lte) && HidlSupport.deepEquals(this.wcdma, other.wcdma) && HidlSupport.deepEquals(this.tdscdma, other.tdscdma)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cellInfoType))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.registered))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.timeStampType))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.timeStamp))), Integer.valueOf(HidlSupport.deepHashCode(this.gsm)), Integer.valueOf(HidlSupport.deepHashCode(this.cdma)), Integer.valueOf(HidlSupport.deepHashCode(this.lte)), Integer.valueOf(HidlSupport.deepHashCode(this.wcdma)), Integer.valueOf(HidlSupport.deepHashCode(this.tdscdma)));
    }

    public final String toString() {
        return "{.cellInfoType = " + CellInfoType.toString(this.cellInfoType) + ", .registered = " + this.registered + ", .timeStampType = " + TimeStampType.toString(this.timeStampType) + ", .timeStamp = " + this.timeStamp + ", .gsm = " + this.gsm + ", .cdma = " + this.cdma + ", .lte = " + this.lte + ", .wcdma = " + this.wcdma + ", .tdscdma = " + this.tdscdma + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(104), 0);
    }

    public static final ArrayList<CellInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CellInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 104), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellInfo _hidl_vec_element = new CellInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 104));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.cellInfoType = _hidl_blob.getInt32(_hidl_offset + 0);
        this.registered = _hidl_blob.getBool(_hidl_offset + 4);
        this.timeStampType = _hidl_blob.getInt32(_hidl_offset + 8);
        this.timeStamp = _hidl_blob.getInt64(_hidl_offset + 16);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 24 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 64), _hidl_blob.handle(), _hidl_offset + 24 + 0, true);
        this.gsm.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellInfoGsm _hidl_vec_element = new CellInfoGsm();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 64));
            this.gsm.add(_hidl_vec_element);
        }
        int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 40 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 40), _hidl_blob.handle(), _hidl_offset + 40 + 0, true);
        this.cdma.clear();
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            CellInfoCdma _hidl_vec_element2 = new CellInfoCdma();
            _hidl_vec_element2.readEmbeddedFromParcel(parcel, childBlob2, (long) (_hidl_index_02 * 40));
            this.cdma.add(_hidl_vec_element2);
        }
        int _hidl_vec_size3 = _hidl_blob.getInt32(_hidl_offset + 56 + 8);
        HwBlob childBlob3 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size3 * 72), _hidl_blob.handle(), _hidl_offset + 56 + 0, true);
        this.lte.clear();
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
            CellInfoLte _hidl_vec_element3 = new CellInfoLte();
            _hidl_vec_element3.readEmbeddedFromParcel(parcel, childBlob3, (long) (_hidl_index_03 * 72));
            this.lte.add(_hidl_vec_element3);
        }
        int _hidl_vec_size4 = _hidl_blob.getInt32(_hidl_offset + 72 + 8);
        HwBlob childBlob4 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size4 * 56), _hidl_blob.handle(), _hidl_offset + 72 + 0, true);
        this.wcdma.clear();
        for (int _hidl_index_04 = 0; _hidl_index_04 < _hidl_vec_size4; _hidl_index_04++) {
            CellInfoWcdma _hidl_vec_element4 = new CellInfoWcdma();
            _hidl_vec_element4.readEmbeddedFromParcel(parcel, childBlob4, (long) (_hidl_index_04 * 56));
            this.wcdma.add(_hidl_vec_element4);
        }
        int _hidl_vec_size5 = _hidl_blob.getInt32(_hidl_offset + 88 + 8);
        HwBlob childBlob5 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size5 * 56), _hidl_blob.handle(), _hidl_offset + 88 + 0, true);
        this.tdscdma.clear();
        for (int _hidl_index_05 = 0; _hidl_index_05 < _hidl_vec_size5; _hidl_index_05++) {
            CellInfoTdscdma _hidl_vec_element5 = new CellInfoTdscdma();
            _hidl_vec_element5.readEmbeddedFromParcel(parcel, childBlob5, (long) (_hidl_index_05 * 56));
            this.tdscdma.add(_hidl_vec_element5);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(104);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CellInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 104);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 104));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(_hidl_offset + 0, this.cellInfoType);
        _hidl_blob.putBool(_hidl_offset + 4, this.registered);
        _hidl_blob.putInt32(_hidl_offset + 8, this.timeStampType);
        _hidl_blob.putInt64(_hidl_offset + 16, this.timeStamp);
        int _hidl_vec_size = this.gsm.size();
        _hidl_blob.putInt32(_hidl_offset + 24 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 24 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 64);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.gsm.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 64));
        }
        _hidl_blob.putBlob(_hidl_offset + 24 + 0, childBlob);
        int _hidl_vec_size2 = this.cdma.size();
        _hidl_blob.putInt32(_hidl_offset + 40 + 8, _hidl_vec_size2);
        _hidl_blob.putBool(_hidl_offset + 40 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 40);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            this.cdma.get(_hidl_index_02).writeEmbeddedToBlob(childBlob2, (long) (_hidl_index_02 * 40));
        }
        _hidl_blob.putBlob(_hidl_offset + 40 + 0, childBlob2);
        int _hidl_vec_size3 = this.lte.size();
        _hidl_blob.putInt32(_hidl_offset + 56 + 8, _hidl_vec_size3);
        _hidl_blob.putBool(_hidl_offset + 56 + 12, false);
        HwBlob childBlob3 = new HwBlob(_hidl_vec_size3 * 72);
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
            this.lte.get(_hidl_index_03).writeEmbeddedToBlob(childBlob3, (long) (_hidl_index_03 * 72));
        }
        _hidl_blob.putBlob(_hidl_offset + 56 + 0, childBlob3);
        int _hidl_vec_size4 = this.wcdma.size();
        _hidl_blob.putInt32(_hidl_offset + 72 + 8, _hidl_vec_size4);
        _hidl_blob.putBool(_hidl_offset + 72 + 12, false);
        HwBlob childBlob4 = new HwBlob(_hidl_vec_size4 * 56);
        for (int _hidl_index_04 = 0; _hidl_index_04 < _hidl_vec_size4; _hidl_index_04++) {
            this.wcdma.get(_hidl_index_04).writeEmbeddedToBlob(childBlob4, (long) (_hidl_index_04 * 56));
        }
        _hidl_blob.putBlob(_hidl_offset + 72 + 0, childBlob4);
        int _hidl_vec_size5 = this.tdscdma.size();
        _hidl_blob.putInt32(_hidl_offset + 88 + 8, _hidl_vec_size5);
        _hidl_blob.putBool(_hidl_offset + 88 + 12, false);
        HwBlob childBlob5 = new HwBlob(_hidl_vec_size5 * 56);
        for (int _hidl_index_05 = 0; _hidl_index_05 < _hidl_vec_size5; _hidl_index_05++) {
            this.tdscdma.get(_hidl_index_05).writeEmbeddedToBlob(childBlob5, (long) (_hidl_index_05 * 56));
        }
        _hidl_blob.putBlob(_hidl_offset + 88 + 0, childBlob5);
    }
}
