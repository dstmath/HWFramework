package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import com.android.internal.telephony.AbstractPhoneBase;
import java.util.ArrayList;
import java.util.Objects;

public final class CellInfo {
    public final ArrayList<CellInfoCdma> cdma = new ArrayList();
    public int cellInfoType;
    public final ArrayList<CellInfoGsm> gsm = new ArrayList();
    public final ArrayList<CellInfoLte> lte = new ArrayList();
    public boolean registered;
    public final ArrayList<CellInfoTdscdma> tdscdma = new ArrayList();
    public long timeStamp;
    public int timeStampType;
    public final ArrayList<CellInfoWcdma> wcdma = new ArrayList();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CellInfo.class) {
            return false;
        }
        CellInfo other = (CellInfo) otherObject;
        return this.cellInfoType == other.cellInfoType && this.registered == other.registered && this.timeStampType == other.timeStampType && this.timeStamp == other.timeStamp && HidlSupport.deepEquals(this.gsm, other.gsm) && HidlSupport.deepEquals(this.cdma, other.cdma) && HidlSupport.deepEquals(this.lte, other.lte) && HidlSupport.deepEquals(this.wcdma, other.wcdma) && HidlSupport.deepEquals(this.tdscdma, other.tdscdma);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cellInfoType))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.registered))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.timeStampType))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.timeStamp))), Integer.valueOf(HidlSupport.deepHashCode(this.gsm)), Integer.valueOf(HidlSupport.deepHashCode(this.cdma)), Integer.valueOf(HidlSupport.deepHashCode(this.lte)), Integer.valueOf(HidlSupport.deepHashCode(this.wcdma)), Integer.valueOf(HidlSupport.deepHashCode(this.tdscdma))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".cellInfoType = ");
        builder.append(CellInfoType.toString(this.cellInfoType));
        builder.append(", .registered = ");
        builder.append(this.registered);
        builder.append(", .timeStampType = ");
        builder.append(TimeStampType.toString(this.timeStampType));
        builder.append(", .timeStamp = ");
        builder.append(this.timeStamp);
        builder.append(", .gsm = ");
        builder.append(this.gsm);
        builder.append(", .cdma = ");
        builder.append(this.cdma);
        builder.append(", .lte = ");
        builder.append(this.lte);
        builder.append(", .wcdma = ");
        builder.append(this.wcdma);
        builder.append(", .tdscdma = ");
        builder.append(this.tdscdma);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(104), 0);
    }

    public static final ArrayList<CellInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CellInfo> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * AbstractPhoneBase.EVENT_ECC_NUM), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellInfo _hidl_vec_element = new CellInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * AbstractPhoneBase.EVENT_ECC_NUM));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        this.cellInfoType = _hidl_blob.getInt32(0 + _hidl_offset);
        this.registered = _hidl_blob.getBool(4 + _hidl_offset);
        this.timeStampType = _hidl_blob.getInt32(8 + _hidl_offset);
        this.timeStamp = _hidl_blob.getInt64(16 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((24 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 64), _hidl_blob.handle(), (24 + _hidl_offset) + 0, true);
        this.gsm.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellInfoGsm _hidl_vec_element = new CellInfoGsm();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 64));
            this.gsm.add(_hidl_vec_element);
        }
        _hidl_vec_size = _hidl_blob.getInt32((40 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), (40 + _hidl_offset) + 0, true);
        this.cdma.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellInfoCdma _hidl_vec_element2 = new CellInfoCdma();
            _hidl_vec_element2.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
            this.cdma.add(_hidl_vec_element2);
        }
        _hidl_vec_size = _hidl_blob.getInt32((56 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 72), _hidl_blob.handle(), (56 + _hidl_offset) + 0, true);
        this.lte.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellInfoLte _hidl_vec_element3 = new CellInfoLte();
            _hidl_vec_element3.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 72));
            this.lte.add(_hidl_vec_element3);
        }
        _hidl_vec_size = _hidl_blob.getInt32((72 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), (72 + _hidl_offset) + 0, true);
        this.wcdma.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellInfoWcdma _hidl_vec_element4 = new CellInfoWcdma();
            _hidl_vec_element4.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            this.wcdma.add(_hidl_vec_element4);
        }
        _hidl_vec_size = _hidl_blob.getInt32((88 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), (88 + _hidl_offset) + 0, true);
        this.tdscdma.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellInfoTdscdma _hidl_vec_element5 = new CellInfoTdscdma();
            _hidl_vec_element5.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            this.tdscdma.add(_hidl_vec_element5);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(AbstractPhoneBase.EVENT_ECC_NUM);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CellInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * AbstractPhoneBase.EVENT_ECC_NUM);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CellInfo) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * AbstractPhoneBase.EVENT_ECC_NUM));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        _hidl_blob.putInt32(0 + _hidl_offset, this.cellInfoType);
        _hidl_blob.putBool(4 + _hidl_offset, this.registered);
        _hidl_blob.putInt32(8 + _hidl_offset, this.timeStampType);
        _hidl_blob.putInt64(16 + _hidl_offset, this.timeStamp);
        int _hidl_vec_size = this.gsm.size();
        _hidl_blob.putInt32((24 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((24 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 64);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CellInfoGsm) this.gsm.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 64));
        }
        _hidl_blob.putBlob((24 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.cdma.size();
        _hidl_blob.putInt32((40 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((40 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 40);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CellInfoCdma) this.cdma.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 40));
        }
        _hidl_blob.putBlob((40 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.lte.size();
        _hidl_blob.putInt32((56 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((56 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 72);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CellInfoLte) this.lte.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 72));
        }
        _hidl_blob.putBlob((56 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.wcdma.size();
        _hidl_blob.putInt32((72 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((72 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 56);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CellInfoWcdma) this.wcdma.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob((72 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.tdscdma.size();
        _hidl_blob.putInt32((88 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((88 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 56);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CellInfoTdscdma) this.tdscdma.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob((88 + _hidl_offset) + 0, childBlob);
    }
}
