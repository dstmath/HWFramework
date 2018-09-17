package vendor.huawei.hardware.radio.V1_0;

import android.hardware.radio.V1_0.CallState;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILImsCall {
    public byte als;
    public final RILImsCallDetails callDetails = new RILImsCallDetails();
    public int index;
    public int isECOnference;
    public byte isMT;
    public byte isMpty;
    public byte isVoice;
    public byte isVoicePrivacy;
    public String name = new String();
    public int namePresentation;
    public String number = new String();
    public int numberPresentation;
    public int peerVideoSupport;
    public int state;
    public int toa;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILImsCall.class) {
            return false;
        }
        RILImsCall other = (RILImsCall) otherObject;
        return this.state == other.state && this.index == other.index && this.toa == other.toa && this.isMpty == other.isMpty && this.isMT == other.isMT && this.als == other.als && this.isVoice == other.isVoice && this.isVoicePrivacy == other.isVoicePrivacy && HidlSupport.deepEquals(this.number, other.number) && this.numberPresentation == other.numberPresentation && HidlSupport.deepEquals(this.name, other.name) && this.namePresentation == other.namePresentation && HidlSupport.deepEquals(this.callDetails, other.callDetails) && this.isECOnference == other.isECOnference && this.peerVideoSupport == other.peerVideoSupport;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.state))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.index))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.toa))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.isMpty))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.isMT))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.als))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.isVoice))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.isVoicePrivacy))), Integer.valueOf(HidlSupport.deepHashCode(this.number)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.numberPresentation))), Integer.valueOf(HidlSupport.deepHashCode(this.name)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.namePresentation))), Integer.valueOf(HidlSupport.deepHashCode(this.callDetails)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.isECOnference))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.peerVideoSupport)))});
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
        builder.append(this.numberPresentation);
        builder.append(", .name = ");
        builder.append(this.name);
        builder.append(", .namePresentation = ");
        builder.append(this.namePresentation);
        builder.append(", .callDetails = ");
        builder.append(this.callDetails);
        builder.append(", .isECOnference = ");
        builder.append(this.isECOnference);
        builder.append(", .peerVideoSupport = ");
        builder.append(this.peerVideoSupport);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(88), 0);
    }

    public static final ArrayList<RILImsCall> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILImsCall> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 88), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILImsCall _hidl_vec_element = new RILImsCall();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 88));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.state = _hidl_blob.getInt32(0 + _hidl_offset);
        this.index = _hidl_blob.getInt32(4 + _hidl_offset);
        this.toa = _hidl_blob.getInt32(8 + _hidl_offset);
        this.isMpty = _hidl_blob.getInt8(12 + _hidl_offset);
        this.isMT = _hidl_blob.getInt8(13 + _hidl_offset);
        this.als = _hidl_blob.getInt8(14 + _hidl_offset);
        this.isVoice = _hidl_blob.getInt8(15 + _hidl_offset);
        this.isVoicePrivacy = _hidl_blob.getInt8(16 + _hidl_offset);
        this.number = _hidl_blob.getString(24 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.number.getBytes().length + 1), _hidl_blob.handle(), 0 + (24 + _hidl_offset), false);
        this.numberPresentation = _hidl_blob.getInt32(40 + _hidl_offset);
        this.name = _hidl_blob.getString(48 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.name.getBytes().length + 1), _hidl_blob.handle(), 0 + (48 + _hidl_offset), false);
        this.namePresentation = _hidl_blob.getInt32(64 + _hidl_offset);
        this.callDetails.readEmbeddedFromParcel(parcel, _hidl_blob, 68 + _hidl_offset);
        this.isECOnference = _hidl_blob.getInt32(76 + _hidl_offset);
        this.peerVideoSupport = _hidl_blob.getInt32(80 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(88);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILImsCall> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 88);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILImsCall) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 88));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.state);
        _hidl_blob.putInt32(4 + _hidl_offset, this.index);
        _hidl_blob.putInt32(8 + _hidl_offset, this.toa);
        _hidl_blob.putInt8(12 + _hidl_offset, this.isMpty);
        _hidl_blob.putInt8(13 + _hidl_offset, this.isMT);
        _hidl_blob.putInt8(14 + _hidl_offset, this.als);
        _hidl_blob.putInt8(15 + _hidl_offset, this.isVoice);
        _hidl_blob.putInt8(16 + _hidl_offset, this.isVoicePrivacy);
        _hidl_blob.putString(24 + _hidl_offset, this.number);
        _hidl_blob.putInt32(40 + _hidl_offset, this.numberPresentation);
        _hidl_blob.putString(48 + _hidl_offset, this.name);
        _hidl_blob.putInt32(64 + _hidl_offset, this.namePresentation);
        this.callDetails.writeEmbeddedToBlob(_hidl_blob, 68 + _hidl_offset);
        _hidl_blob.putInt32(76 + _hidl_offset, this.isECOnference);
        _hidl_blob.putInt32(80 + _hidl_offset, this.peerVideoSupport);
    }
}
