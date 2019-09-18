package vendor.huawei.hardware.qcomradio.V1_0;

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
        if (HidlSupport.deepEquals(this.clipV4, other.clipV4) && HidlSupport.deepEquals(this.netmaskV4, other.netmaskV4) && HidlSupport.deepEquals(this.gateV4, other.gateV4) && HidlSupport.deepEquals(this.dhcpV4, other.dhcpV4) && HidlSupport.deepEquals(this.pDnsV4, other.pDnsV4) && HidlSupport.deepEquals(this.sDnsV4, other.sDnsV4)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.clipV4)), Integer.valueOf(HidlSupport.deepHashCode(this.netmaskV4)), Integer.valueOf(HidlSupport.deepHashCode(this.gateV4)), Integer.valueOf(HidlSupport.deepHashCode(this.dhcpV4)), Integer.valueOf(HidlSupport.deepHashCode(this.pDnsV4)), Integer.valueOf(HidlSupport.deepHashCode(this.sDnsV4))});
    }

    public final String toString() {
        return "{" + ".clipV4 = " + Arrays.toString(this.clipV4) + ", .netmaskV4 = " + Arrays.toString(this.netmaskV4) + ", .gateV4 = " + Arrays.toString(this.gateV4) + ", .dhcpV4 = " + Arrays.toString(this.dhcpV4) + ", .pDnsV4 = " + Arrays.toString(this.pDnsV4) + ", .sDnsV4 = " + Arrays.toString(this.sDnsV4) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<RilIpv4AddrInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RilIpv4AddrInfo> _hidl_vec = new ArrayList<>();
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
        _hidl_blob.copyToInt8Array(0 + _hidl_offset, this.clipV4, 4);
        _hidl_blob.copyToInt8Array(4 + _hidl_offset, this.netmaskV4, 4);
        _hidl_blob.copyToInt8Array(8 + _hidl_offset, this.gateV4, 4);
        _hidl_blob.copyToInt8Array(12 + _hidl_offset, this.dhcpV4, 4);
        _hidl_blob.copyToInt8Array(16 + _hidl_offset, this.pDnsV4, 4);
        _hidl_blob.copyToInt8Array(20 + _hidl_offset, this.sDnsV4, 4);
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
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt8Array(0 + _hidl_offset, this.clipV4);
        _hidl_blob.putInt8Array(4 + _hidl_offset, this.netmaskV4);
        _hidl_blob.putInt8Array(8 + _hidl_offset, this.gateV4);
        _hidl_blob.putInt8Array(12 + _hidl_offset, this.dhcpV4);
        _hidl_blob.putInt8Array(16 + _hidl_offset, this.pDnsV4);
        _hidl_blob.putInt8Array(20 + _hidl_offset, this.sDnsV4);
    }
}
