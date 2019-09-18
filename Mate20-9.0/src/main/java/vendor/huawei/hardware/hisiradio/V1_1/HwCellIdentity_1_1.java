package vendor.huawei.hardware.hisiradio.V1_1;

import android.hardware.radio.V1_0.CellIdentityCdma;
import android.hardware.radio.V1_0.CellIdentityGsm;
import android.hardware.radio.V1_0.CellIdentityLte;
import android.hardware.radio.V1_0.CellIdentityTdscdma;
import android.hardware.radio.V1_0.CellIdentityWcdma;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import com.android.internal.telephony.AbstractPhoneBase;
import java.util.ArrayList;
import java.util.Objects;

public final class HwCellIdentity_1_1 {
    public final ArrayList<CellIdentityCdma> cellIdentityCdma = new ArrayList<>();
    public final ArrayList<CellIdentityGsm> cellIdentityGsm = new ArrayList<>();
    public final ArrayList<CellIdentityLte> cellIdentityLte = new ArrayList<>();
    public final ArrayList<HwCellIdentityNr> cellIdentityNr = new ArrayList<>();
    public final ArrayList<CellIdentityTdscdma> cellIdentityTdscdma = new ArrayList<>();
    public final ArrayList<CellIdentityWcdma> cellIdentityWcdma = new ArrayList<>();
    public int cellInfoType;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != HwCellIdentity_1_1.class) {
            return false;
        }
        HwCellIdentity_1_1 other = (HwCellIdentity_1_1) otherObject;
        if (this.cellInfoType == other.cellInfoType && HidlSupport.deepEquals(this.cellIdentityGsm, other.cellIdentityGsm) && HidlSupport.deepEquals(this.cellIdentityWcdma, other.cellIdentityWcdma) && HidlSupport.deepEquals(this.cellIdentityCdma, other.cellIdentityCdma) && HidlSupport.deepEquals(this.cellIdentityLte, other.cellIdentityLte) && HidlSupport.deepEquals(this.cellIdentityTdscdma, other.cellIdentityTdscdma) && HidlSupport.deepEquals(this.cellIdentityNr, other.cellIdentityNr)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cellInfoType))), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityGsm)), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityWcdma)), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityCdma)), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityLte)), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityTdscdma)), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityNr))});
    }

    public final String toString() {
        return "{" + ".cellInfoType = " + HwCellInfoType_1_1.toString(this.cellInfoType) + ", .cellIdentityGsm = " + this.cellIdentityGsm + ", .cellIdentityWcdma = " + this.cellIdentityWcdma + ", .cellIdentityCdma = " + this.cellIdentityCdma + ", .cellIdentityLte = " + this.cellIdentityLte + ", .cellIdentityTdscdma = " + this.cellIdentityTdscdma + ", .cellIdentityNr = " + this.cellIdentityNr + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(104), 0);
    }

    public static final ArrayList<HwCellIdentity_1_1> readVectorFromParcel(HwParcel parcel) {
        ArrayList<HwCellIdentity_1_1> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * AbstractPhoneBase.EVENT_ECC_NUM), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            HwCellIdentity_1_1 _hidl_vec_element = new HwCellIdentity_1_1();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * AbstractPhoneBase.EVENT_ECC_NUM));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwParcel hwParcel = parcel;
        HwBlob hwBlob = _hidl_blob;
        this.cellInfoType = hwBlob.getInt32(_hidl_offset + 0);
        int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 8 + 8);
        int _hidl_vec_size2 = _hidl_vec_size;
        HwBlob childBlob = hwParcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
        this.cellIdentityGsm.clear();
        int _hidl_index_0 = 0;
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            CellIdentityGsm _hidl_vec_element = new CellIdentityGsm();
            _hidl_vec_element.readEmbeddedFromParcel(hwParcel, childBlob, (long) (_hidl_index_02 * 48));
            this.cellIdentityGsm.add(_hidl_vec_element);
        }
        int _hidl_vec_size3 = hwBlob.getInt32(_hidl_offset + 24 + 8);
        HwBlob childBlob2 = hwParcel.readEmbeddedBuffer((long) (_hidl_vec_size3 * 48), _hidl_blob.handle(), _hidl_offset + 24 + 0, true);
        this.cellIdentityWcdma.clear();
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
            CellIdentityWcdma _hidl_vec_element2 = new CellIdentityWcdma();
            _hidl_vec_element2.readEmbeddedFromParcel(hwParcel, childBlob2, (long) (_hidl_index_03 * 48));
            this.cellIdentityWcdma.add(_hidl_vec_element2);
        }
        int _hidl_vec_size4 = hwBlob.getInt32(_hidl_offset + 40 + 8);
        HwBlob childBlob3 = hwParcel.readEmbeddedBuffer((long) (_hidl_vec_size4 * 20), _hidl_blob.handle(), _hidl_offset + 40 + 0, true);
        this.cellIdentityCdma.clear();
        for (int _hidl_index_04 = 0; _hidl_index_04 < _hidl_vec_size4; _hidl_index_04++) {
            CellIdentityCdma _hidl_vec_element3 = new CellIdentityCdma();
            _hidl_vec_element3.readEmbeddedFromParcel(hwParcel, childBlob3, (long) (_hidl_index_04 * 20));
            this.cellIdentityCdma.add(_hidl_vec_element3);
        }
        int _hidl_vec_size5 = hwBlob.getInt32(_hidl_offset + 56 + 8);
        HwBlob childBlob4 = hwParcel.readEmbeddedBuffer((long) (_hidl_vec_size5 * 48), _hidl_blob.handle(), _hidl_offset + 56 + 0, true);
        this.cellIdentityLte.clear();
        for (int _hidl_index_05 = 0; _hidl_index_05 < _hidl_vec_size5; _hidl_index_05++) {
            CellIdentityLte _hidl_vec_element4 = new CellIdentityLte();
            _hidl_vec_element4.readEmbeddedFromParcel(hwParcel, childBlob4, (long) (_hidl_index_05 * 48));
            this.cellIdentityLte.add(_hidl_vec_element4);
        }
        int _hidl_vec_size6 = hwBlob.getInt32(_hidl_offset + 72 + 8);
        HwBlob childBlob5 = hwParcel.readEmbeddedBuffer((long) (_hidl_vec_size6 * 48), _hidl_blob.handle(), _hidl_offset + 72 + 0, true);
        this.cellIdentityTdscdma.clear();
        for (int _hidl_index_06 = 0; _hidl_index_06 < _hidl_vec_size6; _hidl_index_06++) {
            CellIdentityTdscdma _hidl_vec_element5 = new CellIdentityTdscdma();
            _hidl_vec_element5.readEmbeddedFromParcel(hwParcel, childBlob5, (long) (_hidl_index_06 * 48));
            this.cellIdentityTdscdma.add(_hidl_vec_element5);
        }
        int _hidl_vec_size7 = hwBlob.getInt32(_hidl_offset + 88 + 8);
        HwBlob childBlob6 = hwParcel.readEmbeddedBuffer((long) (_hidl_vec_size7 * 48), _hidl_blob.handle(), 0 + _hidl_offset + 88, true);
        this.cellIdentityNr.clear();
        while (true) {
            int _hidl_index_07 = _hidl_index_0;
            if (_hidl_index_07 < _hidl_vec_size7) {
                HwCellIdentityNr _hidl_vec_element6 = new HwCellIdentityNr();
                _hidl_vec_element6.readEmbeddedFromParcel(hwParcel, childBlob6, (long) (_hidl_index_07 * 48));
                this.cellIdentityNr.add(_hidl_vec_element6);
                _hidl_index_0 = _hidl_index_07 + 1;
            } else {
                return;
            }
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(AbstractPhoneBase.EVENT_ECC_NUM);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<HwCellIdentity_1_1> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * AbstractPhoneBase.EVENT_ECC_NUM);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * AbstractPhoneBase.EVENT_ECC_NUM));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        hwBlob.putInt32(_hidl_offset + 0, this.cellInfoType);
        int _hidl_vec_size = this.cellIdentityGsm.size();
        hwBlob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
        int _hidl_index_0 = 0;
        hwBlob.putBool(_hidl_offset + 8 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size; _hidl_index_02++) {
            this.cellIdentityGsm.get(_hidl_index_02).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_02 * 48));
        }
        hwBlob.putBlob(_hidl_offset + 8 + 0, childBlob);
        int _hidl_vec_size2 = this.cellIdentityWcdma.size();
        hwBlob.putInt32(_hidl_offset + 24 + 8, _hidl_vec_size2);
        hwBlob.putBool(_hidl_offset + 24 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 48);
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size2; _hidl_index_03++) {
            this.cellIdentityWcdma.get(_hidl_index_03).writeEmbeddedToBlob(childBlob2, (long) (_hidl_index_03 * 48));
        }
        hwBlob.putBlob(_hidl_offset + 24 + 0, childBlob2);
        int _hidl_vec_size3 = this.cellIdentityCdma.size();
        hwBlob.putInt32(_hidl_offset + 40 + 8, _hidl_vec_size3);
        hwBlob.putBool(_hidl_offset + 40 + 12, false);
        HwBlob childBlob3 = new HwBlob(_hidl_vec_size3 * 20);
        for (int _hidl_index_04 = 0; _hidl_index_04 < _hidl_vec_size3; _hidl_index_04++) {
            this.cellIdentityCdma.get(_hidl_index_04).writeEmbeddedToBlob(childBlob3, (long) (_hidl_index_04 * 20));
        }
        hwBlob.putBlob(_hidl_offset + 40 + 0, childBlob3);
        int _hidl_vec_size4 = this.cellIdentityLte.size();
        hwBlob.putInt32(_hidl_offset + 56 + 8, _hidl_vec_size4);
        hwBlob.putBool(_hidl_offset + 56 + 12, false);
        HwBlob childBlob4 = new HwBlob(_hidl_vec_size4 * 48);
        for (int _hidl_index_05 = 0; _hidl_index_05 < _hidl_vec_size4; _hidl_index_05++) {
            this.cellIdentityLte.get(_hidl_index_05).writeEmbeddedToBlob(childBlob4, (long) (_hidl_index_05 * 48));
        }
        hwBlob.putBlob(_hidl_offset + 56 + 0, childBlob4);
        int _hidl_vec_size5 = this.cellIdentityTdscdma.size();
        hwBlob.putInt32(_hidl_offset + 72 + 8, _hidl_vec_size5);
        hwBlob.putBool(_hidl_offset + 72 + 12, false);
        HwBlob childBlob5 = new HwBlob(_hidl_vec_size5 * 48);
        for (int _hidl_index_06 = 0; _hidl_index_06 < _hidl_vec_size5; _hidl_index_06++) {
            this.cellIdentityTdscdma.get(_hidl_index_06).writeEmbeddedToBlob(childBlob5, (long) (_hidl_index_06 * 48));
        }
        hwBlob.putBlob(_hidl_offset + 72 + 0, childBlob5);
        int _hidl_vec_size6 = this.cellIdentityNr.size();
        hwBlob.putInt32(_hidl_offset + 88 + 8, _hidl_vec_size6);
        hwBlob.putBool(_hidl_offset + 88 + 12, false);
        HwBlob childBlob6 = new HwBlob(_hidl_vec_size6 * 48);
        while (true) {
            int _hidl_index_07 = _hidl_index_0;
            if (_hidl_index_07 < _hidl_vec_size6) {
                this.cellIdentityNr.get(_hidl_index_07).writeEmbeddedToBlob(childBlob6, (long) (_hidl_index_07 * 48));
                _hidl_index_0 = _hidl_index_07 + 1;
            } else {
                hwBlob.putBlob(_hidl_offset + 88 + 0, childBlob6);
                return;
            }
        }
    }
}
