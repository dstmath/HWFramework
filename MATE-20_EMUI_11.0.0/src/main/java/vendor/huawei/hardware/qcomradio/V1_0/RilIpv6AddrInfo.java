package vendor.huawei.hardware.qcomradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class RilIpv6AddrInfo {
    public byte[] clipV6 = new byte[16];
    public byte[] dhcpV6 = new byte[16];
    public byte[] gateV6 = new byte[16];
    public byte[] netmaskV6 = new byte[16];
    public byte[] pDnsV6 = new byte[16];
    public byte[] sDnsV6 = new byte[16];

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RilIpv6AddrInfo.class) {
            return false;
        }
        RilIpv6AddrInfo other = (RilIpv6AddrInfo) otherObject;
        if (HidlSupport.deepEquals(this.clipV6, other.clipV6) && HidlSupport.deepEquals(this.netmaskV6, other.netmaskV6) && HidlSupport.deepEquals(this.gateV6, other.gateV6) && HidlSupport.deepEquals(this.dhcpV6, other.dhcpV6) && HidlSupport.deepEquals(this.pDnsV6, other.pDnsV6) && HidlSupport.deepEquals(this.sDnsV6, other.sDnsV6)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.clipV6)), Integer.valueOf(HidlSupport.deepHashCode(this.netmaskV6)), Integer.valueOf(HidlSupport.deepHashCode(this.gateV6)), Integer.valueOf(HidlSupport.deepHashCode(this.dhcpV6)), Integer.valueOf(HidlSupport.deepHashCode(this.pDnsV6)), Integer.valueOf(HidlSupport.deepHashCode(this.sDnsV6)));
    }

    public final String toString() {
        return "{.clipV6 = " + Arrays.toString(this.clipV6) + ", .netmaskV6 = " + Arrays.toString(this.netmaskV6) + ", .gateV6 = " + Arrays.toString(this.gateV6) + ", .dhcpV6 = " + Arrays.toString(this.dhcpV6) + ", .pDnsV6 = " + Arrays.toString(this.pDnsV6) + ", .sDnsV6 = " + Arrays.toString(this.sDnsV6) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(96), 0);
    }

    public static final ArrayList<RilIpv6AddrInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RilIpv6AddrInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 96), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RilIpv6AddrInfo _hidl_vec_element = new RilIpv6AddrInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 96));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.copyToInt8Array(0 + _hidl_offset, this.clipV6, 16);
        _hidl_blob.copyToInt8Array(16 + _hidl_offset, this.netmaskV6, 16);
        _hidl_blob.copyToInt8Array(32 + _hidl_offset, this.gateV6, 16);
        _hidl_blob.copyToInt8Array(48 + _hidl_offset, this.dhcpV6, 16);
        _hidl_blob.copyToInt8Array(64 + _hidl_offset, this.pDnsV6, 16);
        _hidl_blob.copyToInt8Array(80 + _hidl_offset, this.sDnsV6, 16);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(96);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RilIpv6AddrInfo> _hidl_vec) {
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
        long _hidl_array_offset_0 = 0 + _hidl_offset;
        byte[] _hidl_array_item_0 = this.clipV6;
        if (_hidl_array_item_0 == null || _hidl_array_item_0.length != 16) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_0, _hidl_array_item_0);
        long _hidl_array_offset_02 = 16 + _hidl_offset;
        byte[] _hidl_array_item_02 = this.netmaskV6;
        if (_hidl_array_item_02 == null || _hidl_array_item_02.length != 16) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_02, _hidl_array_item_02);
        long _hidl_array_offset_03 = 32 + _hidl_offset;
        byte[] _hidl_array_item_03 = this.gateV6;
        if (_hidl_array_item_03 == null || _hidl_array_item_03.length != 16) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_03, _hidl_array_item_03);
        long _hidl_array_offset_04 = 48 + _hidl_offset;
        byte[] _hidl_array_item_04 = this.dhcpV6;
        if (_hidl_array_item_04 == null || _hidl_array_item_04.length != 16) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_04, _hidl_array_item_04);
        long _hidl_array_offset_05 = 64 + _hidl_offset;
        byte[] _hidl_array_item_05 = this.pDnsV6;
        if (_hidl_array_item_05 == null || _hidl_array_item_05.length != 16) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_05, _hidl_array_item_05);
        long _hidl_array_offset_06 = 80 + _hidl_offset;
        byte[] _hidl_array_item_06 = this.sDnsV6;
        if (_hidl_array_item_06 == null || _hidl_array_item_06.length != 16) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_06, _hidl_array_item_06);
    }
}
