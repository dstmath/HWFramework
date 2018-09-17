package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class RilIpv6AddrInfo {
    public final byte[] clipV6 = new byte[16];
    public final byte[] dhcpV6 = new byte[16];
    public final byte[] gateV6 = new byte[16];
    public final byte[] netmaskV6 = new byte[16];
    public final byte[] pDnsV6 = new byte[16];
    public final byte[] sDnsV6 = new byte[16];

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RilIpv6AddrInfo.class) {
            return false;
        }
        RilIpv6AddrInfo other = (RilIpv6AddrInfo) otherObject;
        return HidlSupport.deepEquals(this.clipV6, other.clipV6) && HidlSupport.deepEquals(this.netmaskV6, other.netmaskV6) && HidlSupport.deepEquals(this.gateV6, other.gateV6) && HidlSupport.deepEquals(this.dhcpV6, other.dhcpV6) && HidlSupport.deepEquals(this.pDnsV6, other.pDnsV6) && HidlSupport.deepEquals(this.sDnsV6, other.sDnsV6);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.clipV6)), Integer.valueOf(HidlSupport.deepHashCode(this.netmaskV6)), Integer.valueOf(HidlSupport.deepHashCode(this.gateV6)), Integer.valueOf(HidlSupport.deepHashCode(this.dhcpV6)), Integer.valueOf(HidlSupport.deepHashCode(this.pDnsV6)), Integer.valueOf(HidlSupport.deepHashCode(this.sDnsV6))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".clipV6 = ");
        builder.append(Arrays.toString(this.clipV6));
        builder.append(", .netmaskV6 = ");
        builder.append(Arrays.toString(this.netmaskV6));
        builder.append(", .gateV6 = ");
        builder.append(Arrays.toString(this.gateV6));
        builder.append(", .dhcpV6 = ");
        builder.append(Arrays.toString(this.dhcpV6));
        builder.append(", .pDnsV6 = ");
        builder.append(Arrays.toString(this.pDnsV6));
        builder.append(", .sDnsV6 = ");
        builder.append(Arrays.toString(this.sDnsV6));
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(96), 0);
    }

    public static final ArrayList<RilIpv6AddrInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RilIpv6AddrInfo> _hidl_vec = new ArrayList();
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
        int _hidl_index_0_0;
        long _hidl_array_offset_0 = _hidl_offset + 0;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 16; _hidl_index_0_0++) {
            this.clipV6[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 16;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 16; _hidl_index_0_0++) {
            this.netmaskV6[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 32;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 16; _hidl_index_0_0++) {
            this.gateV6[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 48;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 16; _hidl_index_0_0++) {
            this.dhcpV6[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 64;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 16; _hidl_index_0_0++) {
            this.pDnsV6[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 80;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 16; _hidl_index_0_0++) {
            this.sDnsV6[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
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
            ((RilIpv6AddrInfo) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 96));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0_0;
        long _hidl_array_offset_0 = _hidl_offset + 0;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 16; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.clipV6[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 16;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 16; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.netmaskV6[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 32;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 16; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.gateV6[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 48;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 16; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.dhcpV6[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 64;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 16; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.pDnsV6[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 80;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 16; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.sDnsV6[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
    }
}
