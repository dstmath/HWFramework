package vendor.huawei.hardware.mtkradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class PktFilter {
    public String address = new String();
    public int bitmap;
    public int direction;
    public int flowLabel;
    public int id;
    public int localPortHigh;
    public int localPortLow;
    public String mask = new String();
    public int networkPfIdentifier;
    public int precedence;
    public int protocolNextHeader;
    public int remotePortHigh;
    public int remotePortLow;
    public int spi;
    public int tos;
    public int tosMask;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != PktFilter.class) {
            return false;
        }
        PktFilter other = (PktFilter) otherObject;
        if (this.id == other.id && this.precedence == other.precedence && this.direction == other.direction && this.networkPfIdentifier == other.networkPfIdentifier && this.bitmap == other.bitmap && HidlSupport.deepEquals(this.address, other.address) && HidlSupport.deepEquals(this.mask, other.mask) && this.protocolNextHeader == other.protocolNextHeader && this.localPortLow == other.localPortLow && this.localPortHigh == other.localPortHigh && this.remotePortLow == other.remotePortLow && this.remotePortHigh == other.remotePortHigh && this.spi == other.spi && this.tos == other.tos && this.tosMask == other.tosMask && this.flowLabel == other.flowLabel) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.id))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.precedence))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.direction))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.networkPfIdentifier))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.bitmap))), Integer.valueOf(HidlSupport.deepHashCode(this.address)), Integer.valueOf(HidlSupport.deepHashCode(this.mask)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.protocolNextHeader))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.localPortLow))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.localPortHigh))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.remotePortLow))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.remotePortHigh))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.spi))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.tos))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.tosMask))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.flowLabel))));
    }

    public final String toString() {
        return "{" + ".id = " + this.id + ", .precedence = " + this.precedence + ", .direction = " + this.direction + ", .networkPfIdentifier = " + this.networkPfIdentifier + ", .bitmap = " + this.bitmap + ", .address = " + this.address + ", .mask = " + this.mask + ", .protocolNextHeader = " + this.protocolNextHeader + ", .localPortLow = " + this.localPortLow + ", .localPortHigh = " + this.localPortHigh + ", .remotePortLow = " + this.remotePortLow + ", .remotePortHigh = " + this.remotePortHigh + ", .spi = " + this.spi + ", .tos = " + this.tos + ", .tosMask = " + this.tosMask + ", .flowLabel = " + this.flowLabel + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(96), 0);
    }

    public static final ArrayList<PktFilter> readVectorFromParcel(HwParcel parcel) {
        ArrayList<PktFilter> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 96), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            PktFilter _hidl_vec_element = new PktFilter();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 96));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.id = _hidl_blob.getInt32(_hidl_offset + 0);
        this.precedence = _hidl_blob.getInt32(_hidl_offset + 4);
        this.direction = _hidl_blob.getInt32(_hidl_offset + 8);
        this.networkPfIdentifier = _hidl_blob.getInt32(_hidl_offset + 12);
        this.bitmap = _hidl_blob.getInt32(_hidl_offset + 16);
        this.address = _hidl_blob.getString(_hidl_offset + 24);
        parcel.readEmbeddedBuffer((long) (this.address.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 24 + 0, false);
        this.mask = _hidl_blob.getString(_hidl_offset + 40);
        parcel.readEmbeddedBuffer((long) (this.mask.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 40 + 0, false);
        this.protocolNextHeader = _hidl_blob.getInt32(_hidl_offset + 56);
        this.localPortLow = _hidl_blob.getInt32(_hidl_offset + 60);
        this.localPortHigh = _hidl_blob.getInt32(_hidl_offset + 64);
        this.remotePortLow = _hidl_blob.getInt32(_hidl_offset + 68);
        this.remotePortHigh = _hidl_blob.getInt32(_hidl_offset + 72);
        this.spi = _hidl_blob.getInt32(_hidl_offset + 76);
        this.tos = _hidl_blob.getInt32(_hidl_offset + 80);
        this.tosMask = _hidl_blob.getInt32(_hidl_offset + 84);
        this.flowLabel = _hidl_blob.getInt32(_hidl_offset + 88);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(96);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<PktFilter> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 96);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 96));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.id);
        _hidl_blob.putInt32(4 + _hidl_offset, this.precedence);
        _hidl_blob.putInt32(8 + _hidl_offset, this.direction);
        _hidl_blob.putInt32(12 + _hidl_offset, this.networkPfIdentifier);
        _hidl_blob.putInt32(16 + _hidl_offset, this.bitmap);
        _hidl_blob.putString(24 + _hidl_offset, this.address);
        _hidl_blob.putString(40 + _hidl_offset, this.mask);
        _hidl_blob.putInt32(56 + _hidl_offset, this.protocolNextHeader);
        _hidl_blob.putInt32(60 + _hidl_offset, this.localPortLow);
        _hidl_blob.putInt32(64 + _hidl_offset, this.localPortHigh);
        _hidl_blob.putInt32(68 + _hidl_offset, this.remotePortLow);
        _hidl_blob.putInt32(72 + _hidl_offset, this.remotePortHigh);
        _hidl_blob.putInt32(76 + _hidl_offset, this.spi);
        _hidl_blob.putInt32(80 + _hidl_offset, this.tos);
        _hidl_blob.putInt32(84 + _hidl_offset, this.tosMask);
        _hidl_blob.putInt32(88 + _hidl_offset, this.flowLabel);
    }
}
