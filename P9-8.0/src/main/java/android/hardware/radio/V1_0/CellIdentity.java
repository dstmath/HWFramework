package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CellIdentity {
    public final ArrayList<CellIdentityCdma> cellIdentityCdma = new ArrayList();
    public final ArrayList<CellIdentityGsm> cellIdentityGsm = new ArrayList();
    public final ArrayList<CellIdentityLte> cellIdentityLte = new ArrayList();
    public final ArrayList<CellIdentityTdscdma> cellIdentityTdscdma = new ArrayList();
    public final ArrayList<CellIdentityWcdma> cellIdentityWcdma = new ArrayList();
    public int cellInfoType;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CellIdentity.class) {
            return false;
        }
        CellIdentity other = (CellIdentity) otherObject;
        return this.cellInfoType == other.cellInfoType && HidlSupport.deepEquals(this.cellIdentityGsm, other.cellIdentityGsm) && HidlSupport.deepEquals(this.cellIdentityWcdma, other.cellIdentityWcdma) && HidlSupport.deepEquals(this.cellIdentityCdma, other.cellIdentityCdma) && HidlSupport.deepEquals(this.cellIdentityLte, other.cellIdentityLte) && HidlSupport.deepEquals(this.cellIdentityTdscdma, other.cellIdentityTdscdma);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cellInfoType))), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityGsm)), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityWcdma)), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityCdma)), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityLte)), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityTdscdma))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".cellInfoType = ");
        builder.append(CellInfoType.toString(this.cellInfoType));
        builder.append(", .cellIdentityGsm = ");
        builder.append(this.cellIdentityGsm);
        builder.append(", .cellIdentityWcdma = ");
        builder.append(this.cellIdentityWcdma);
        builder.append(", .cellIdentityCdma = ");
        builder.append(this.cellIdentityCdma);
        builder.append(", .cellIdentityLte = ");
        builder.append(this.cellIdentityLte);
        builder.append(", .cellIdentityTdscdma = ");
        builder.append(this.cellIdentityTdscdma);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(88), 0);
    }

    public static final ArrayList<CellIdentity> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CellIdentity> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 88), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellIdentity _hidl_vec_element = new CellIdentity();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 88));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        this.cellInfoType = _hidl_blob.getInt32(0 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((8 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), (8 + _hidl_offset) + 0, true);
        this.cellIdentityGsm.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellIdentityGsm _hidl_vec_element = new CellIdentityGsm();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            this.cellIdentityGsm.add(_hidl_vec_element);
        }
        _hidl_vec_size = _hidl_blob.getInt32((24 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), (24 + _hidl_offset) + 0, true);
        this.cellIdentityWcdma.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellIdentityWcdma _hidl_vec_element2 = new CellIdentityWcdma();
            _hidl_vec_element2.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            this.cellIdentityWcdma.add(_hidl_vec_element2);
        }
        _hidl_vec_size = _hidl_blob.getInt32((40 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 20), _hidl_blob.handle(), (40 + _hidl_offset) + 0, true);
        this.cellIdentityCdma.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellIdentityCdma _hidl_vec_element3 = new CellIdentityCdma();
            _hidl_vec_element3.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 20));
            this.cellIdentityCdma.add(_hidl_vec_element3);
        }
        _hidl_vec_size = _hidl_blob.getInt32((56 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), (56 + _hidl_offset) + 0, true);
        this.cellIdentityLte.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellIdentityLte _hidl_vec_element4 = new CellIdentityLte();
            _hidl_vec_element4.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            this.cellIdentityLte.add(_hidl_vec_element4);
        }
        _hidl_vec_size = _hidl_blob.getInt32((72 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), (72 + _hidl_offset) + 0, true);
        this.cellIdentityTdscdma.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellIdentityTdscdma _hidl_vec_element5 = new CellIdentityTdscdma();
            _hidl_vec_element5.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            this.cellIdentityTdscdma.add(_hidl_vec_element5);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(88);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CellIdentity> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 88);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CellIdentity) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 88));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        _hidl_blob.putInt32(0 + _hidl_offset, this.cellInfoType);
        int _hidl_vec_size = this.cellIdentityGsm.size();
        _hidl_blob.putInt32((8 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((8 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CellIdentityGsm) this.cellIdentityGsm.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob((8 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.cellIdentityWcdma.size();
        _hidl_blob.putInt32((24 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((24 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 48);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CellIdentityWcdma) this.cellIdentityWcdma.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob((24 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.cellIdentityCdma.size();
        _hidl_blob.putInt32((40 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((40 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 20);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CellIdentityCdma) this.cellIdentityCdma.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 20));
        }
        _hidl_blob.putBlob((40 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.cellIdentityLte.size();
        _hidl_blob.putInt32((56 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((56 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 48);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CellIdentityLte) this.cellIdentityLte.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob((56 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.cellIdentityTdscdma.size();
        _hidl_blob.putInt32((72 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((72 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 48);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CellIdentityTdscdma) this.cellIdentityTdscdma.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob((72 + _hidl_offset) + 0, childBlob);
    }
}
