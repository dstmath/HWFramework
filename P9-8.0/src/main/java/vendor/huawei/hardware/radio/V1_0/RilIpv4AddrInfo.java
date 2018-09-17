package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class RilIpv4AddrInfo {
    public final byte[] clipV4 = new byte[4];
    public final byte[] dhcpV4 = new byte[4];
    public final byte[] gateV4 = new byte[4];
    public final byte[] netmaskV4 = new byte[4];
    public final byte[] pDnsV4 = new byte[4];
    public final byte[] sDnsV4 = new byte[4];

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RilIpv4AddrInfo.class) {
            return false;
        }
        RilIpv4AddrInfo other = (RilIpv4AddrInfo) otherObject;
        return HidlSupport.deepEquals(this.clipV4, other.clipV4) && HidlSupport.deepEquals(this.netmaskV4, other.netmaskV4) && HidlSupport.deepEquals(this.gateV4, other.gateV4) && HidlSupport.deepEquals(this.dhcpV4, other.dhcpV4) && HidlSupport.deepEquals(this.pDnsV4, other.pDnsV4) && HidlSupport.deepEquals(this.sDnsV4, other.sDnsV4);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.clipV4)), Integer.valueOf(HidlSupport.deepHashCode(this.netmaskV4)), Integer.valueOf(HidlSupport.deepHashCode(this.gateV4)), Integer.valueOf(HidlSupport.deepHashCode(this.dhcpV4)), Integer.valueOf(HidlSupport.deepHashCode(this.pDnsV4)), Integer.valueOf(HidlSupport.deepHashCode(this.sDnsV4))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".clipV4 = ");
        builder.append(Arrays.toString(this.clipV4));
        builder.append(", .netmaskV4 = ");
        builder.append(Arrays.toString(this.netmaskV4));
        builder.append(", .gateV4 = ");
        builder.append(Arrays.toString(this.gateV4));
        builder.append(", .dhcpV4 = ");
        builder.append(Arrays.toString(this.dhcpV4));
        builder.append(", .pDnsV4 = ");
        builder.append(Arrays.toString(this.pDnsV4));
        builder.append(", .sDnsV4 = ");
        builder.append(Arrays.toString(this.sDnsV4));
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<RilIpv4AddrInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RilIpv4AddrInfo> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RilIpv4AddrInfo _hidl_vec_element = new RilIpv4AddrInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0_0;
        long _hidl_array_offset_0 = _hidl_offset + 0;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 4; _hidl_index_0_0++) {
            this.clipV4[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 4;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 4; _hidl_index_0_0++) {
            this.netmaskV4[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 8;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 4; _hidl_index_0_0++) {
            this.gateV4[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 12;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 4; _hidl_index_0_0++) {
            this.dhcpV4[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 16;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 4; _hidl_index_0_0++) {
            this.pDnsV4[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 20;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 4; _hidl_index_0_0++) {
            this.sDnsV4[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RilIpv4AddrInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RilIpv4AddrInfo) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0_0;
        long _hidl_array_offset_0 = _hidl_offset + 0;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 4; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.clipV4[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 4;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 4; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.netmaskV4[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 8;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 4; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.gateV4[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 12;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 4; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.dhcpV4[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 16;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 4; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.pDnsV4[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 20;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 4; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.sDnsV4[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
    }
}
