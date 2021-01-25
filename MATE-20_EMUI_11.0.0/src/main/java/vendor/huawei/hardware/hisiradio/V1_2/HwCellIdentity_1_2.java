package vendor.huawei.hardware.hisiradio.V1_2;

import android.hardware.radio.V1_0.CellIdentityCdma;
import android.hardware.radio.V1_0.CellIdentityGsm;
import android.hardware.radio.V1_0.CellIdentityLte;
import android.hardware.radio.V1_0.CellIdentityTdscdma;
import android.hardware.radio.V1_0.CellIdentityWcdma;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;
import vendor.huawei.hardware.hisiradio.V1_1.HwCellInfoType_1_1;

public final class HwCellIdentity_1_2 {
    public ArrayList<CellIdentityCdma> cellIdentityCdma = new ArrayList<>();
    public ArrayList<CellIdentityGsm> cellIdentityGsm = new ArrayList<>();
    public ArrayList<CellIdentityLte> cellIdentityLte = new ArrayList<>();
    public ArrayList<HwCellIdentityNr_1_2> cellIdentityNr = new ArrayList<>();
    public ArrayList<CellIdentityTdscdma> cellIdentityTdscdma = new ArrayList<>();
    public ArrayList<CellIdentityWcdma> cellIdentityWcdma = new ArrayList<>();
    public int cellInfoType;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != HwCellIdentity_1_2.class) {
            return false;
        }
        HwCellIdentity_1_2 other = (HwCellIdentity_1_2) otherObject;
        if (this.cellInfoType == other.cellInfoType && HidlSupport.deepEquals(this.cellIdentityGsm, other.cellIdentityGsm) && HidlSupport.deepEquals(this.cellIdentityWcdma, other.cellIdentityWcdma) && HidlSupport.deepEquals(this.cellIdentityCdma, other.cellIdentityCdma) && HidlSupport.deepEquals(this.cellIdentityLte, other.cellIdentityLte) && HidlSupport.deepEquals(this.cellIdentityTdscdma, other.cellIdentityTdscdma) && HidlSupport.deepEquals(this.cellIdentityNr, other.cellIdentityNr)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cellInfoType))), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityGsm)), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityWcdma)), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityCdma)), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityLte)), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityTdscdma)), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityNr)));
    }

    public final String toString() {
        return "{.cellInfoType = " + HwCellInfoType_1_1.toString(this.cellInfoType) + ", .cellIdentityGsm = " + this.cellIdentityGsm + ", .cellIdentityWcdma = " + this.cellIdentityWcdma + ", .cellIdentityCdma = " + this.cellIdentityCdma + ", .cellIdentityLte = " + this.cellIdentityLte + ", .cellIdentityTdscdma = " + this.cellIdentityTdscdma + ", .cellIdentityNr = " + this.cellIdentityNr + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(104), 0);
    }

    public static final ArrayList<HwCellIdentity_1_2> readVectorFromParcel(HwParcel parcel) {
        ArrayList<HwCellIdentity_1_2> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 104), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            HwCellIdentity_1_2 _hidl_vec_element = new HwCellIdentity_1_2();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 104));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.cellInfoType = _hidl_blob.getInt32(_hidl_offset + 0);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 8 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
        this.cellIdentityGsm.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellIdentityGsm _hidl_vec_element = new CellIdentityGsm();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            this.cellIdentityGsm.add(_hidl_vec_element);
        }
        int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 24 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 48), _hidl_blob.handle(), _hidl_offset + 24 + 0, true);
        this.cellIdentityWcdma.clear();
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            CellIdentityWcdma _hidl_vec_element2 = new CellIdentityWcdma();
            _hidl_vec_element2.readEmbeddedFromParcel(parcel, childBlob2, (long) (_hidl_index_02 * 48));
            this.cellIdentityWcdma.add(_hidl_vec_element2);
        }
        int _hidl_vec_size3 = _hidl_blob.getInt32(_hidl_offset + 40 + 8);
        HwBlob childBlob3 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size3 * 20), _hidl_blob.handle(), _hidl_offset + 40 + 0, true);
        this.cellIdentityCdma.clear();
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
            CellIdentityCdma _hidl_vec_element3 = new CellIdentityCdma();
            _hidl_vec_element3.readEmbeddedFromParcel(parcel, childBlob3, (long) (_hidl_index_03 * 20));
            this.cellIdentityCdma.add(_hidl_vec_element3);
        }
        int _hidl_vec_size4 = _hidl_blob.getInt32(_hidl_offset + 56 + 8);
        HwBlob childBlob4 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size4 * 48), _hidl_blob.handle(), _hidl_offset + 56 + 0, true);
        this.cellIdentityLte.clear();
        for (int _hidl_index_04 = 0; _hidl_index_04 < _hidl_vec_size4; _hidl_index_04++) {
            CellIdentityLte _hidl_vec_element4 = new CellIdentityLte();
            _hidl_vec_element4.readEmbeddedFromParcel(parcel, childBlob4, (long) (_hidl_index_04 * 48));
            this.cellIdentityLte.add(_hidl_vec_element4);
        }
        int _hidl_vec_size5 = _hidl_blob.getInt32(_hidl_offset + 72 + 8);
        HwBlob childBlob5 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size5 * 48), _hidl_blob.handle(), _hidl_offset + 72 + 0, true);
        this.cellIdentityTdscdma.clear();
        for (int _hidl_index_05 = 0; _hidl_index_05 < _hidl_vec_size5; _hidl_index_05++) {
            CellIdentityTdscdma _hidl_vec_element5 = new CellIdentityTdscdma();
            _hidl_vec_element5.readEmbeddedFromParcel(parcel, childBlob5, (long) (_hidl_index_05 * 48));
            this.cellIdentityTdscdma.add(_hidl_vec_element5);
        }
        int _hidl_vec_size6 = _hidl_blob.getInt32(_hidl_offset + 88 + 8);
        HwBlob childBlob6 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size6 * 56), _hidl_blob.handle(), _hidl_offset + 88 + 0, true);
        this.cellIdentityNr.clear();
        for (int _hidl_index_06 = 0; _hidl_index_06 < _hidl_vec_size6; _hidl_index_06++) {
            HwCellIdentityNr_1_2 _hidl_vec_element6 = new HwCellIdentityNr_1_2();
            _hidl_vec_element6.readEmbeddedFromParcel(parcel, childBlob6, (long) (_hidl_index_06 * 56));
            this.cellIdentityNr.add(_hidl_vec_element6);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(104);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<HwCellIdentity_1_2> _hidl_vec) {
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
        int _hidl_vec_size = this.cellIdentityGsm.size();
        _hidl_blob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 8 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.cellIdentityGsm.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob(_hidl_offset + 8 + 0, childBlob);
        int _hidl_vec_size2 = this.cellIdentityWcdma.size();
        _hidl_blob.putInt32(_hidl_offset + 24 + 8, _hidl_vec_size2);
        _hidl_blob.putBool(_hidl_offset + 24 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 48);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            this.cellIdentityWcdma.get(_hidl_index_02).writeEmbeddedToBlob(childBlob2, (long) (_hidl_index_02 * 48));
        }
        _hidl_blob.putBlob(_hidl_offset + 24 + 0, childBlob2);
        int _hidl_vec_size3 = this.cellIdentityCdma.size();
        _hidl_blob.putInt32(_hidl_offset + 40 + 8, _hidl_vec_size3);
        _hidl_blob.putBool(_hidl_offset + 40 + 12, false);
        HwBlob childBlob3 = new HwBlob(_hidl_vec_size3 * 20);
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
            this.cellIdentityCdma.get(_hidl_index_03).writeEmbeddedToBlob(childBlob3, (long) (_hidl_index_03 * 20));
        }
        _hidl_blob.putBlob(_hidl_offset + 40 + 0, childBlob3);
        int _hidl_vec_size4 = this.cellIdentityLte.size();
        _hidl_blob.putInt32(_hidl_offset + 56 + 8, _hidl_vec_size4);
        _hidl_blob.putBool(_hidl_offset + 56 + 12, false);
        HwBlob childBlob4 = new HwBlob(_hidl_vec_size4 * 48);
        for (int _hidl_index_04 = 0; _hidl_index_04 < _hidl_vec_size4; _hidl_index_04++) {
            this.cellIdentityLte.get(_hidl_index_04).writeEmbeddedToBlob(childBlob4, (long) (_hidl_index_04 * 48));
        }
        _hidl_blob.putBlob(_hidl_offset + 56 + 0, childBlob4);
        int _hidl_vec_size5 = this.cellIdentityTdscdma.size();
        _hidl_blob.putInt32(_hidl_offset + 72 + 8, _hidl_vec_size5);
        _hidl_blob.putBool(_hidl_offset + 72 + 12, false);
        HwBlob childBlob5 = new HwBlob(_hidl_vec_size5 * 48);
        for (int _hidl_index_05 = 0; _hidl_index_05 < _hidl_vec_size5; _hidl_index_05++) {
            this.cellIdentityTdscdma.get(_hidl_index_05).writeEmbeddedToBlob(childBlob5, (long) (_hidl_index_05 * 48));
        }
        _hidl_blob.putBlob(_hidl_offset + 72 + 0, childBlob5);
        int _hidl_vec_size6 = this.cellIdentityNr.size();
        _hidl_blob.putInt32(_hidl_offset + 88 + 8, _hidl_vec_size6);
        _hidl_blob.putBool(_hidl_offset + 88 + 12, false);
        HwBlob childBlob6 = new HwBlob(_hidl_vec_size6 * 56);
        for (int _hidl_index_06 = 0; _hidl_index_06 < _hidl_vec_size6; _hidl_index_06++) {
            this.cellIdentityNr.get(_hidl_index_06).writeEmbeddedToBlob(childBlob6, (long) (_hidl_index_06 * 56));
        }
        _hidl_blob.putBlob(_hidl_offset + 88 + 0, childBlob6);
    }
}
