package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class Call {
    public byte als;
    public int index;
    public boolean isMT;
    public boolean isMpty;
    public boolean isVoice;
    public boolean isVoicePrivacy;
    public String name = new String();
    public int namePresentation;
    public String number = new String();
    public int numberPresentation;
    public int state;
    public int toa;
    public final ArrayList<UusInfo> uusInfo = new ArrayList();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != Call.class) {
            return false;
        }
        Call other = (Call) otherObject;
        return this.state == other.state && this.index == other.index && this.toa == other.toa && this.isMpty == other.isMpty && this.isMT == other.isMT && this.als == other.als && this.isVoice == other.isVoice && this.isVoicePrivacy == other.isVoicePrivacy && HidlSupport.deepEquals(this.number, other.number) && this.numberPresentation == other.numberPresentation && HidlSupport.deepEquals(this.name, other.name) && this.namePresentation == other.namePresentation && HidlSupport.deepEquals(this.uusInfo, other.uusInfo);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.state))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.index))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.toa))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isMpty))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isMT))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.als))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isVoice))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isVoicePrivacy))), Integer.valueOf(HidlSupport.deepHashCode(this.number)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.numberPresentation))), Integer.valueOf(HidlSupport.deepHashCode(this.name)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.namePresentation))), Integer.valueOf(HidlSupport.deepHashCode(this.uusInfo))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".state = ");
        builder.append(CallState.toString(this.state));
        builder.append(", .index = ");
        builder.append(this.index);
        builder.append(", .toa = ");
        builder.append(this.toa);
        builder.append(", .isMpty = ");
        builder.append(this.isMpty);
        builder.append(", .isMT = ");
        builder.append(this.isMT);
        builder.append(", .als = ");
        builder.append(this.als);
        builder.append(", .isVoice = ");
        builder.append(this.isVoice);
        builder.append(", .isVoicePrivacy = ");
        builder.append(this.isVoicePrivacy);
        builder.append(", .number = ");
        builder.append(this.number);
        builder.append(", .numberPresentation = ");
        builder.append(CallPresentation.toString(this.numberPresentation));
        builder.append(", .name = ");
        builder.append(this.name);
        builder.append(", .namePresentation = ");
        builder.append(CallPresentation.toString(this.namePresentation));
        builder.append(", .uusInfo = ");
        builder.append(this.uusInfo);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(88), 0);
    }

    public static final ArrayList<Call> readVectorFromParcel(HwParcel parcel) {
        ArrayList<Call> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 88), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            Call _hidl_vec_element = new Call();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 88));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.state = _hidl_blob.getInt32(0 + _hidl_offset);
        this.index = _hidl_blob.getInt32(4 + _hidl_offset);
        this.toa = _hidl_blob.getInt32(8 + _hidl_offset);
        this.isMpty = _hidl_blob.getBool(12 + _hidl_offset);
        this.isMT = _hidl_blob.getBool(13 + _hidl_offset);
        this.als = _hidl_blob.getInt8(14 + _hidl_offset);
        this.isVoice = _hidl_blob.getBool(15 + _hidl_offset);
        this.isVoicePrivacy = _hidl_blob.getBool(16 + _hidl_offset);
        this.number = _hidl_blob.getString(24 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.number.getBytes().length + 1), _hidl_blob.handle(), (24 + _hidl_offset) + 0, false);
        this.numberPresentation = _hidl_blob.getInt32(40 + _hidl_offset);
        this.name = _hidl_blob.getString(48 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.name.getBytes().length + 1), _hidl_blob.handle(), (48 + _hidl_offset) + 0, false);
        this.namePresentation = _hidl_blob.getInt32(64 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((72 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), (72 + _hidl_offset) + 0, true);
        this.uusInfo.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            UusInfo _hidl_vec_element = new UusInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            this.uusInfo.add(_hidl_vec_element);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(88);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<Call> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 88);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((Call) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 88));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.state);
        _hidl_blob.putInt32(4 + _hidl_offset, this.index);
        _hidl_blob.putInt32(8 + _hidl_offset, this.toa);
        _hidl_blob.putBool(12 + _hidl_offset, this.isMpty);
        _hidl_blob.putBool(13 + _hidl_offset, this.isMT);
        _hidl_blob.putInt8(14 + _hidl_offset, this.als);
        _hidl_blob.putBool(15 + _hidl_offset, this.isVoice);
        _hidl_blob.putBool(16 + _hidl_offset, this.isVoicePrivacy);
        _hidl_blob.putString(24 + _hidl_offset, this.number);
        _hidl_blob.putInt32(40 + _hidl_offset, this.numberPresentation);
        _hidl_blob.putString(48 + _hidl_offset, this.name);
        _hidl_blob.putInt32(64 + _hidl_offset, this.namePresentation);
        int _hidl_vec_size = this.uusInfo.size();
        _hidl_blob.putInt32((72 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((72 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((UusInfo) this.uusInfo.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob((72 + _hidl_offset) + 0, childBlob);
    }
}
