package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CdmaInformationRecord {
    public ArrayList<CdmaT53AudioControlInfoRecord> audioCtrl = new ArrayList<>();
    public ArrayList<CdmaT53ClirInfoRecord> clir = new ArrayList<>();
    public ArrayList<CdmaDisplayInfoRecord> display = new ArrayList<>();
    public ArrayList<CdmaLineControlInfoRecord> lineCtrl = new ArrayList<>();
    public int name;
    public ArrayList<CdmaNumberInfoRecord> number = new ArrayList<>();
    public ArrayList<CdmaRedirectingNumberInfoRecord> redir = new ArrayList<>();
    public ArrayList<CdmaSignalInfoRecord> signal = new ArrayList<>();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CdmaInformationRecord.class) {
            return false;
        }
        CdmaInformationRecord other = (CdmaInformationRecord) otherObject;
        if (this.name == other.name && HidlSupport.deepEquals(this.display, other.display) && HidlSupport.deepEquals(this.number, other.number) && HidlSupport.deepEquals(this.signal, other.signal) && HidlSupport.deepEquals(this.redir, other.redir) && HidlSupport.deepEquals(this.lineCtrl, other.lineCtrl) && HidlSupport.deepEquals(this.clir, other.clir) && HidlSupport.deepEquals(this.audioCtrl, other.audioCtrl)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.name))), Integer.valueOf(HidlSupport.deepHashCode(this.display)), Integer.valueOf(HidlSupport.deepHashCode(this.number)), Integer.valueOf(HidlSupport.deepHashCode(this.signal)), Integer.valueOf(HidlSupport.deepHashCode(this.redir)), Integer.valueOf(HidlSupport.deepHashCode(this.lineCtrl)), Integer.valueOf(HidlSupport.deepHashCode(this.clir)), Integer.valueOf(HidlSupport.deepHashCode(this.audioCtrl)));
    }

    public final String toString() {
        return "{" + ".name = " + CdmaInfoRecName.toString(this.name) + ", .display = " + this.display + ", .number = " + this.number + ", .signal = " + this.signal + ", .redir = " + this.redir + ", .lineCtrl = " + this.lineCtrl + ", .clir = " + this.clir + ", .audioCtrl = " + this.audioCtrl + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(120), 0);
    }

    public static final ArrayList<CdmaInformationRecord> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CdmaInformationRecord> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 120), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CdmaInformationRecord _hidl_vec_element = new CdmaInformationRecord();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 120));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.name = _hidl_blob.getInt32(_hidl_offset + 0);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 8 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 16), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
        this.display.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CdmaDisplayInfoRecord _hidl_vec_element = new CdmaDisplayInfoRecord();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 16));
            this.display.add(_hidl_vec_element);
        }
        int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 24 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 24), _hidl_blob.handle(), _hidl_offset + 24 + 0, true);
        this.number.clear();
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            CdmaNumberInfoRecord _hidl_vec_element2 = new CdmaNumberInfoRecord();
            _hidl_vec_element2.readEmbeddedFromParcel(parcel, childBlob2, (long) (_hidl_index_02 * 24));
            this.number.add(_hidl_vec_element2);
        }
        int _hidl_vec_size3 = _hidl_blob.getInt32(_hidl_offset + 40 + 8);
        HwBlob childBlob3 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size3 * 4), _hidl_blob.handle(), _hidl_offset + 40 + 0, true);
        this.signal.clear();
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
            CdmaSignalInfoRecord _hidl_vec_element3 = new CdmaSignalInfoRecord();
            _hidl_vec_element3.readEmbeddedFromParcel(parcel, childBlob3, (long) (_hidl_index_03 * 4));
            this.signal.add(_hidl_vec_element3);
        }
        int _hidl_vec_size4 = _hidl_blob.getInt32(_hidl_offset + 56 + 8);
        HwBlob childBlob4 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size4 * 32), _hidl_blob.handle(), _hidl_offset + 56 + 0, true);
        this.redir.clear();
        for (int _hidl_index_04 = 0; _hidl_index_04 < _hidl_vec_size4; _hidl_index_04++) {
            CdmaRedirectingNumberInfoRecord _hidl_vec_element4 = new CdmaRedirectingNumberInfoRecord();
            _hidl_vec_element4.readEmbeddedFromParcel(parcel, childBlob4, (long) (_hidl_index_04 * 32));
            this.redir.add(_hidl_vec_element4);
        }
        int _hidl_vec_size5 = _hidl_blob.getInt32(_hidl_offset + 72 + 8);
        HwBlob childBlob5 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size5 * 4), _hidl_blob.handle(), _hidl_offset + 72 + 0, true);
        this.lineCtrl.clear();
        for (int _hidl_index_05 = 0; _hidl_index_05 < _hidl_vec_size5; _hidl_index_05++) {
            CdmaLineControlInfoRecord _hidl_vec_element5 = new CdmaLineControlInfoRecord();
            _hidl_vec_element5.readEmbeddedFromParcel(parcel, childBlob5, (long) (_hidl_index_05 * 4));
            this.lineCtrl.add(_hidl_vec_element5);
        }
        int _hidl_vec_size6 = _hidl_blob.getInt32(_hidl_offset + 88 + 8);
        HwBlob childBlob6 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size6 * 1), _hidl_blob.handle(), _hidl_offset + 88 + 0, true);
        this.clir.clear();
        for (int _hidl_index_06 = 0; _hidl_index_06 < _hidl_vec_size6; _hidl_index_06++) {
            CdmaT53ClirInfoRecord _hidl_vec_element6 = new CdmaT53ClirInfoRecord();
            _hidl_vec_element6.readEmbeddedFromParcel(parcel, childBlob6, (long) (_hidl_index_06 * 1));
            this.clir.add(_hidl_vec_element6);
        }
        int _hidl_vec_size7 = _hidl_blob.getInt32(_hidl_offset + 104 + 8);
        HwBlob childBlob7 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size7 * 2), _hidl_blob.handle(), _hidl_offset + 104 + 0, true);
        this.audioCtrl.clear();
        for (int _hidl_index_07 = 0; _hidl_index_07 < _hidl_vec_size7; _hidl_index_07++) {
            CdmaT53AudioControlInfoRecord _hidl_vec_element7 = new CdmaT53AudioControlInfoRecord();
            _hidl_vec_element7.readEmbeddedFromParcel(parcel, childBlob7, (long) (_hidl_index_07 * 2));
            this.audioCtrl.add(_hidl_vec_element7);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(120);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CdmaInformationRecord> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 120);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 120));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(_hidl_offset + 0, this.name);
        int _hidl_vec_size = this.display.size();
        _hidl_blob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 8 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 16);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.display.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 16));
        }
        _hidl_blob.putBlob(_hidl_offset + 8 + 0, childBlob);
        int _hidl_vec_size2 = this.number.size();
        _hidl_blob.putInt32(_hidl_offset + 24 + 8, _hidl_vec_size2);
        _hidl_blob.putBool(_hidl_offset + 24 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 24);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            this.number.get(_hidl_index_02).writeEmbeddedToBlob(childBlob2, (long) (_hidl_index_02 * 24));
        }
        _hidl_blob.putBlob(_hidl_offset + 24 + 0, childBlob2);
        int _hidl_vec_size3 = this.signal.size();
        _hidl_blob.putInt32(_hidl_offset + 40 + 8, _hidl_vec_size3);
        _hidl_blob.putBool(_hidl_offset + 40 + 12, false);
        HwBlob childBlob3 = new HwBlob(_hidl_vec_size3 * 4);
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
            this.signal.get(_hidl_index_03).writeEmbeddedToBlob(childBlob3, (long) (_hidl_index_03 * 4));
        }
        _hidl_blob.putBlob(_hidl_offset + 40 + 0, childBlob3);
        int _hidl_vec_size4 = this.redir.size();
        _hidl_blob.putInt32(_hidl_offset + 56 + 8, _hidl_vec_size4);
        _hidl_blob.putBool(_hidl_offset + 56 + 12, false);
        HwBlob childBlob4 = new HwBlob(_hidl_vec_size4 * 32);
        for (int _hidl_index_04 = 0; _hidl_index_04 < _hidl_vec_size4; _hidl_index_04++) {
            this.redir.get(_hidl_index_04).writeEmbeddedToBlob(childBlob4, (long) (_hidl_index_04 * 32));
        }
        _hidl_blob.putBlob(_hidl_offset + 56 + 0, childBlob4);
        int _hidl_vec_size5 = this.lineCtrl.size();
        _hidl_blob.putInt32(_hidl_offset + 72 + 8, _hidl_vec_size5);
        _hidl_blob.putBool(_hidl_offset + 72 + 12, false);
        HwBlob childBlob5 = new HwBlob(_hidl_vec_size5 * 4);
        for (int _hidl_index_05 = 0; _hidl_index_05 < _hidl_vec_size5; _hidl_index_05++) {
            this.lineCtrl.get(_hidl_index_05).writeEmbeddedToBlob(childBlob5, (long) (_hidl_index_05 * 4));
        }
        _hidl_blob.putBlob(_hidl_offset + 72 + 0, childBlob5);
        int _hidl_vec_size6 = this.clir.size();
        _hidl_blob.putInt32(_hidl_offset + 88 + 8, _hidl_vec_size6);
        _hidl_blob.putBool(_hidl_offset + 88 + 12, false);
        HwBlob childBlob6 = new HwBlob(_hidl_vec_size6 * 1);
        for (int _hidl_index_06 = 0; _hidl_index_06 < _hidl_vec_size6; _hidl_index_06++) {
            this.clir.get(_hidl_index_06).writeEmbeddedToBlob(childBlob6, (long) (_hidl_index_06 * 1));
        }
        _hidl_blob.putBlob(_hidl_offset + 88 + 0, childBlob6);
        int _hidl_vec_size7 = this.audioCtrl.size();
        _hidl_blob.putInt32(_hidl_offset + 104 + 8, _hidl_vec_size7);
        _hidl_blob.putBool(_hidl_offset + 104 + 12, false);
        HwBlob childBlob7 = new HwBlob(_hidl_vec_size7 * 2);
        for (int _hidl_index_07 = 0; _hidl_index_07 < _hidl_vec_size7; _hidl_index_07++) {
            this.audioCtrl.get(_hidl_index_07).writeEmbeddedToBlob(childBlob7, (long) (_hidl_index_07 * 2));
        }
        _hidl_blob.putBlob(_hidl_offset + 104 + 0, childBlob7);
    }
}
