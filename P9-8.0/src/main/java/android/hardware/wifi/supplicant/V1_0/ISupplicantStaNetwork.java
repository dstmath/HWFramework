package android.hardware.wifi.supplicant.V1_0;

import android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork.getIdCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork.getInterfaceNameCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork.getTypeCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.ReasonCode;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.StatusCode;
import android.hidl.base.V1_0.DebugInfo;
import android.hidl.base.V1_0.IBase;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwBinder.DeathRecipient;
import android.os.IHwInterface;
import android.os.RemoteException;
import android.os.SystemProperties;
import com.android.server.wifi.HalDeviceManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public interface ISupplicantStaNetwork extends ISupplicantNetwork {
    public static final String kInterfaceName = "android.hardware.wifi.supplicant@1.0::ISupplicantStaNetwork";

    public static final class AuthAlgMask {
        public static final int LEAP = 4;
        public static final int OPEN = 1;
        public static final int SHARED = 2;

        public static final String toString(int o) {
            if (o == 1) {
                return "OPEN";
            }
            if (o == 2) {
                return "SHARED";
            }
            if (o == 4) {
                return "LEAP";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList();
            int flipped = 0;
            if ((o & 1) == 1) {
                list.add("OPEN");
                flipped = 1;
            }
            if ((o & 2) == 2) {
                list.add("SHARED");
                flipped |= 2;
            }
            if ((o & 4) == 4) {
                list.add("LEAP");
                flipped |= 4;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class EapMethod {
        public static final int AKA = 5;
        public static final int AKA_PRIME = 6;
        public static final int PEAP = 0;
        public static final int PWD = 3;
        public static final int SIM = 4;
        public static final int TLS = 1;
        public static final int TTLS = 2;
        public static final int WFA_UNAUTH_TLS = 7;

        public static final String toString(int o) {
            if (o == 0) {
                return "PEAP";
            }
            if (o == 1) {
                return "TLS";
            }
            if (o == 2) {
                return "TTLS";
            }
            if (o == 3) {
                return "PWD";
            }
            if (o == 4) {
                return "SIM";
            }
            if (o == 5) {
                return "AKA";
            }
            if (o == 6) {
                return "AKA_PRIME";
            }
            if (o == 7) {
                return "WFA_UNAUTH_TLS";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList();
            int flipped = 0;
            if ((o & 0) == 0) {
                list.add("PEAP");
                flipped = 0;
            }
            if ((o & 1) == 1) {
                list.add("TLS");
                flipped |= 1;
            }
            if ((o & 2) == 2) {
                list.add("TTLS");
                flipped |= 2;
            }
            if ((o & 3) == 3) {
                list.add("PWD");
                flipped |= 3;
            }
            if ((o & 4) == 4) {
                list.add("SIM");
                flipped |= 4;
            }
            if ((o & 5) == 5) {
                list.add("AKA");
                flipped |= 5;
            }
            if ((o & 6) == 6) {
                list.add("AKA_PRIME");
                flipped |= 6;
            }
            if ((o & 7) == 7) {
                list.add("WFA_UNAUTH_TLS");
                flipped |= 7;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class EapPhase2Method {
        public static final int AKA = 6;
        public static final int AKA_PRIME = 7;
        public static final int GTC = 4;
        public static final int MSPAP = 2;
        public static final int MSPAPV2 = 3;
        public static final int NONE = 0;
        public static final int PAP = 1;
        public static final int SIM = 5;

        public static final String toString(int o) {
            if (o == 0) {
                return "NONE";
            }
            if (o == 1) {
                return "PAP";
            }
            if (o == 2) {
                return "MSPAP";
            }
            if (o == 3) {
                return "MSPAPV2";
            }
            if (o == 4) {
                return "GTC";
            }
            if (o == 5) {
                return "SIM";
            }
            if (o == 6) {
                return "AKA";
            }
            if (o == 7) {
                return "AKA_PRIME";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList();
            int flipped = 0;
            if ((o & 0) == 0) {
                list.add("NONE");
                flipped = 0;
            }
            if ((o & 1) == 1) {
                list.add("PAP");
                flipped |= 1;
            }
            if ((o & 2) == 2) {
                list.add("MSPAP");
                flipped |= 2;
            }
            if ((o & 3) == 3) {
                list.add("MSPAPV2");
                flipped |= 3;
            }
            if ((o & 4) == 4) {
                list.add("GTC");
                flipped |= 4;
            }
            if ((o & 5) == 5) {
                list.add("SIM");
                flipped |= 5;
            }
            if ((o & 6) == 6) {
                list.add("AKA");
                flipped |= 6;
            }
            if ((o & 7) == 7) {
                list.add("AKA_PRIME");
                flipped |= 7;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class GroupCipherMask {
        public static final int CCMP = 16;
        public static final int GTK_NOT_USED = 16384;
        public static final int TKIP = 8;
        public static final int WEP104 = 4;
        public static final int WEP40 = 2;

        public static final String toString(int o) {
            if (o == 2) {
                return "WEP40";
            }
            if (o == 4) {
                return "WEP104";
            }
            if (o == 8) {
                return "TKIP";
            }
            if (o == 16) {
                return "CCMP";
            }
            if (o == 16384) {
                return "GTK_NOT_USED";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList();
            int flipped = 0;
            if ((o & 2) == 2) {
                list.add("WEP40");
                flipped = 2;
            }
            if ((o & 4) == 4) {
                list.add("WEP104");
                flipped |= 4;
            }
            if ((o & 8) == 8) {
                list.add("TKIP");
                flipped |= 8;
            }
            if ((o & 16) == 16) {
                list.add("CCMP");
                flipped |= 16;
            }
            if ((o & 16384) == 16384) {
                list.add("GTK_NOT_USED");
                flipped |= 16384;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class KeyMgmtMask {
        public static final int FT_EAP = 32;
        public static final int FT_PSK = 64;
        public static final int IEEE8021X = 8;
        public static final int NONE = 4;
        public static final int OSEN = 32768;
        public static final int WPA_EAP = 1;
        public static final int WPA_PSK = 2;

        public static final String toString(int o) {
            if (o == 1) {
                return "WPA_EAP";
            }
            if (o == 2) {
                return "WPA_PSK";
            }
            if (o == 4) {
                return "NONE";
            }
            if (o == 8) {
                return "IEEE8021X";
            }
            if (o == 32) {
                return "FT_EAP";
            }
            if (o == 64) {
                return "FT_PSK";
            }
            if (o == 32768) {
                return "OSEN";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList();
            int flipped = 0;
            if ((o & 1) == 1) {
                list.add("WPA_EAP");
                flipped = 1;
            }
            if ((o & 2) == 2) {
                list.add("WPA_PSK");
                flipped |= 2;
            }
            if ((o & 4) == 4) {
                list.add("NONE");
                flipped |= 4;
            }
            if ((o & 8) == 8) {
                list.add("IEEE8021X");
                flipped |= 8;
            }
            if ((o & 32) == 32) {
                list.add("FT_EAP");
                flipped |= 32;
            }
            if ((o & 64) == 64) {
                list.add("FT_PSK");
                flipped |= 64;
            }
            if ((o & 32768) == 32768) {
                list.add("OSEN");
                flipped |= 32768;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class NetworkResponseEapSimGsmAuthParams {
        public final byte[] kc = new byte[8];
        public final byte[] sres = new byte[4];

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != NetworkResponseEapSimGsmAuthParams.class) {
                return false;
            }
            NetworkResponseEapSimGsmAuthParams other = (NetworkResponseEapSimGsmAuthParams) otherObject;
            return HidlSupport.deepEquals(this.kc, other.kc) && HidlSupport.deepEquals(this.sres, other.sres);
        }

        public final int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.kc)), Integer.valueOf(HidlSupport.deepHashCode(this.sres))});
        }

        public final String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            builder.append(".kc = ");
            builder.append(Arrays.toString(this.kc));
            builder.append(", .sres = ");
            builder.append(Arrays.toString(this.sres));
            builder.append("}");
            return builder.toString();
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(12), 0);
        }

        public static final ArrayList<NetworkResponseEapSimGsmAuthParams> readVectorFromParcel(HwParcel parcel) {
            ArrayList<NetworkResponseEapSimGsmAuthParams> _hidl_vec = new ArrayList();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 12), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                NetworkResponseEapSimGsmAuthParams _hidl_vec_element = new NetworkResponseEapSimGsmAuthParams();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 12));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            int _hidl_index_0_0;
            long _hidl_array_offset_0 = _hidl_offset + 0;
            for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 8; _hidl_index_0_0++) {
                this.kc[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
                _hidl_array_offset_0++;
            }
            _hidl_array_offset_0 = _hidl_offset + 8;
            for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 4; _hidl_index_0_0++) {
                this.sres[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
                _hidl_array_offset_0++;
            }
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(12);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NetworkResponseEapSimGsmAuthParams> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 12);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                ((NetworkResponseEapSimGsmAuthParams) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 12));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            int _hidl_index_0_0;
            long _hidl_array_offset_0 = _hidl_offset + 0;
            for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 8; _hidl_index_0_0++) {
                _hidl_blob.putInt8(_hidl_array_offset_0, this.kc[_hidl_index_0_0]);
                _hidl_array_offset_0++;
            }
            _hidl_array_offset_0 = _hidl_offset + 8;
            for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 4; _hidl_index_0_0++) {
                _hidl_blob.putInt8(_hidl_array_offset_0, this.sres[_hidl_index_0_0]);
                _hidl_array_offset_0++;
            }
        }
    }

    public static final class NetworkResponseEapSimUmtsAuthParams {
        public final byte[] ck = new byte[16];
        public final byte[] ik = new byte[16];
        public final ArrayList<Byte> res = new ArrayList();

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != NetworkResponseEapSimUmtsAuthParams.class) {
                return false;
            }
            NetworkResponseEapSimUmtsAuthParams other = (NetworkResponseEapSimUmtsAuthParams) otherObject;
            return HidlSupport.deepEquals(this.res, other.res) && HidlSupport.deepEquals(this.ik, other.ik) && HidlSupport.deepEquals(this.ck, other.ck);
        }

        public final int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.res)), Integer.valueOf(HidlSupport.deepHashCode(this.ik)), Integer.valueOf(HidlSupport.deepHashCode(this.ck))});
        }

        public final String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            builder.append(".res = ");
            builder.append(this.res);
            builder.append(", .ik = ");
            builder.append(Arrays.toString(this.ik));
            builder.append(", .ck = ");
            builder.append(Arrays.toString(this.ck));
            builder.append("}");
            return builder.toString();
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
        }

        public static final ArrayList<NetworkResponseEapSimUmtsAuthParams> readVectorFromParcel(HwParcel parcel) {
            ArrayList<NetworkResponseEapSimUmtsAuthParams> _hidl_vec = new ArrayList();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                NetworkResponseEapSimUmtsAuthParams _hidl_vec_element = new NetworkResponseEapSimUmtsAuthParams();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            int _hidl_index_0_0;
            int _hidl_vec_size = _hidl_blob.getInt32((0 + _hidl_offset) + 8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (0 + _hidl_offset) + 0, true);
            this.res.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                this.res.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
            }
            long _hidl_array_offset_0 = _hidl_offset + 16;
            for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 16; _hidl_index_0_0++) {
                this.ik[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
                _hidl_array_offset_0++;
            }
            _hidl_array_offset_0 = _hidl_offset + 32;
            for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 16; _hidl_index_0_0++) {
                this.ck[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
                _hidl_array_offset_0++;
            }
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(48);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NetworkResponseEapSimUmtsAuthParams> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                ((NetworkResponseEapSimUmtsAuthParams) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            int _hidl_index_0_0;
            int _hidl_vec_size = this.res.size();
            _hidl_blob.putInt32((0 + _hidl_offset) + 8, _hidl_vec_size);
            _hidl_blob.putBool((0 + _hidl_offset) + 12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.res.get(_hidl_index_0)).byteValue());
            }
            _hidl_blob.putBlob((0 + _hidl_offset) + 0, childBlob);
            long _hidl_array_offset_0 = _hidl_offset + 16;
            for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 16; _hidl_index_0_0++) {
                _hidl_blob.putInt8(_hidl_array_offset_0, this.ik[_hidl_index_0_0]);
                _hidl_array_offset_0++;
            }
            _hidl_array_offset_0 = _hidl_offset + 32;
            for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 16; _hidl_index_0_0++) {
                _hidl_blob.putInt8(_hidl_array_offset_0, this.ck[_hidl_index_0_0]);
                _hidl_array_offset_0++;
            }
        }
    }

    public static final class PairwiseCipherMask {
        public static final int CCMP = 16;
        public static final int NONE = 1;
        public static final int TKIP = 8;

        public static final String toString(int o) {
            if (o == 1) {
                return "NONE";
            }
            if (o == 8) {
                return "TKIP";
            }
            if (o == 16) {
                return "CCMP";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList();
            int flipped = 0;
            if ((o & 1) == 1) {
                list.add("NONE");
                flipped = 1;
            }
            if ((o & 8) == 8) {
                list.add("TKIP");
                flipped |= 8;
            }
            if ((o & 16) == 16) {
                list.add("CCMP");
                flipped |= 16;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class ParamSizeLimits {
        public static final int PSK_PASSPHRASE_MAX_LEN_IN_BYTES = 63;
        public static final int PSK_PASSPHRASE_MIN_LEN_IN_BYTES = 8;
        public static final int SSID_MAX_LEN_IN_BYTES = 32;
        public static final int WEP104_KEY_LEN_IN_BYTES = 13;
        public static final int WEP40_KEY_LEN_IN_BYTES = 5;
        public static final int WEP_KEYS_MAX_NUM = 4;

        public static final String toString(int o) {
            if (o == 32) {
                return "SSID_MAX_LEN_IN_BYTES";
            }
            if (o == 8) {
                return "PSK_PASSPHRASE_MIN_LEN_IN_BYTES";
            }
            if (o == 63) {
                return "PSK_PASSPHRASE_MAX_LEN_IN_BYTES";
            }
            if (o == 4) {
                return "WEP_KEYS_MAX_NUM";
            }
            if (o == 5) {
                return "WEP40_KEY_LEN_IN_BYTES";
            }
            if (o == 13) {
                return "WEP104_KEY_LEN_IN_BYTES";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList();
            int flipped = 0;
            if ((o & 32) == 32) {
                list.add("SSID_MAX_LEN_IN_BYTES");
                flipped = 32;
            }
            if ((o & 8) == 8) {
                list.add("PSK_PASSPHRASE_MIN_LEN_IN_BYTES");
                flipped |= 8;
            }
            if ((o & 63) == 63) {
                list.add("PSK_PASSPHRASE_MAX_LEN_IN_BYTES");
                flipped |= 63;
            }
            if ((o & 4) == 4) {
                list.add("WEP_KEYS_MAX_NUM");
                flipped |= 4;
            }
            if ((o & 5) == 5) {
                list.add("WEP40_KEY_LEN_IN_BYTES");
                flipped |= 5;
            }
            if ((o & 13) == 13) {
                list.add("WEP104_KEY_LEN_IN_BYTES");
                flipped |= 13;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class ProtoMask {
        public static final int OSEN = 8;
        public static final int RSN = 2;
        public static final int WPA = 1;

        public static final String toString(int o) {
            if (o == 1) {
                return "WPA";
            }
            if (o == 2) {
                return "RSN";
            }
            if (o == 8) {
                return "OSEN";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList();
            int flipped = 0;
            if ((o & 1) == 1) {
                list.add("WPA");
                flipped = 1;
            }
            if ((o & 2) == 2) {
                list.add("RSN");
                flipped |= 2;
            }
            if ((o & 8) == 8) {
                list.add("OSEN");
                flipped |= 8;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class Proxy implements ISupplicantStaNetwork {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of android.hardware.wifi.supplicant@1.0::ISupplicantStaNetwork]@Proxy";
            }
        }

        public void getId(getIdCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getInterfaceName(getInterfaceNameCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getType(getTypeCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus registerCallback(ISupplicantStaNetworkCallback callback) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            if (callback != null) {
                iHwBinder = callback.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setSsid(ArrayList<Byte> ssid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt8Vector(ssid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setBssid(byte[] bssid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            long _hidl_array_offset_0 = 0;
            for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
                _hidl_blob.putInt8(_hidl_array_offset_0, bssid[_hidl_index_0_0]);
                _hidl_array_offset_0++;
            }
            _hidl_request.writeBuffer(_hidl_blob);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setScanSsid(boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setKeyMgmt(int keyMgmtMask) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(keyMgmtMask);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setProto(int protoMask) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(protoMask);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setAuthAlg(int authAlgMask) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(authAlgMask);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setGroupCipher(int groupCipherMask) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(groupCipherMask);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setPairwiseCipher(int pairwiseCipherMask) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(pairwiseCipherMask);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setPskPassphrase(String psk) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(psk);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setPsk(byte[] psk) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(32);
            long _hidl_array_offset_0 = 0;
            for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 32; _hidl_index_0_0++) {
                _hidl_blob.putInt8(_hidl_array_offset_0, psk[_hidl_index_0_0]);
                _hidl_array_offset_0++;
            }
            _hidl_request.writeBuffer(_hidl_blob);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setWepKey(int keyIdx, ArrayList<Byte> wepKey) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(keyIdx);
            _hidl_request.writeInt8Vector(wepKey);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setWepTxKeyIdx(int keyIdx) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(keyIdx);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setRequirePmf(boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setEapMethod(int method) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(method);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setEapPhase2Method(int method) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(method);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setEapIdentity(ArrayList<Byte> identity) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt8Vector(identity);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setEapAnonymousIdentity(ArrayList<Byte> identity) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt8Vector(identity);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setEapPassword(ArrayList<Byte> password) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt8Vector(password);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setEapCACert(String path) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(path);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setEapCAPath(String path) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(path);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setEapClientCert(String path) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(path);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setEapPrivateKeyId(String id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(26, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setEapSubjectMatch(String match) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(match);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(27, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setEapAltSubjectMatch(String match) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(match);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(28, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setEapEngine(boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(29, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setEapEngineID(String id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(30, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setEapDomainSuffixMatch(String match) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(match);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(31, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setProactiveKeyCaching(boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(32, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setIdStr(String idStr) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(idStr);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(33, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus setUpdateIdentifier(int id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(34, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public void getSsid(getSsidCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(35, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getBssid(getBssidCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(36, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                byte[] _hidl_out_bssid = new byte[6];
                HwBlob _hidl_blob = _hidl_reply.readBuffer(6);
                long _hidl_array_offset_0 = 0;
                for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
                    _hidl_out_bssid[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
                    _hidl_array_offset_0++;
                }
                cb.onValues(_hidl_out_status, _hidl_out_bssid);
            } finally {
                _hidl_reply.release();
            }
        }

        public void getScanSsid(getScanSsidCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(37, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readBool());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getKeyMgmt(getKeyMgmtCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(38, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getProto(getProtoCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(39, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getAuthAlg(getAuthAlgCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(40, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getGroupCipher(getGroupCipherCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(41, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getPairwiseCipher(getPairwiseCipherCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(42, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getPskPassphrase(getPskPassphraseCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(43, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getPsk(getPskCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(44, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                byte[] _hidl_out_psk = new byte[32];
                HwBlob _hidl_blob = _hidl_reply.readBuffer(32);
                long _hidl_array_offset_0 = 0;
                for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 32; _hidl_index_0_0++) {
                    _hidl_out_psk[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
                    _hidl_array_offset_0++;
                }
                cb.onValues(_hidl_out_status, _hidl_out_psk);
            } finally {
                _hidl_reply.release();
            }
        }

        public void getWepKey(int keyIdx, getWepKeyCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(keyIdx);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(45, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getWepTxKeyIdx(getWepTxKeyIdxCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(46, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getRequirePmf(getRequirePmfCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(47, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readBool());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getEapMethod(getEapMethodCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(48, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getEapPhase2Method(getEapPhase2MethodCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(49, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getEapIdentity(getEapIdentityCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(50, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getEapAnonymousIdentity(getEapAnonymousIdentityCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(51, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getEapPassword(getEapPasswordCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(52, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getEapCACert(getEapCACertCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(53, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getEapCAPath(getEapCAPathCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(54, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getEapClientCert(getEapClientCertCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(55, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getEapPrivateKeyId(getEapPrivateKeyIdCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(56, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getEapSubjectMatch(getEapSubjectMatchCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(57, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getEapAltSubjectMatch(getEapAltSubjectMatchCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(58, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getEapEngine(getEapEngineCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(59, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readBool());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getEapEngineID(getEapEngineIDCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(60, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getEapDomainSuffixMatch(getEapDomainSuffixMatchCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(61, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getIdStr(getIdStrCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(62, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getWpsNfcConfigurationToken(getWpsNfcConfigurationTokenCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(63, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus enable(boolean noConnect) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeBool(noConnect);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(64, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus disable() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(65, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus select() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(66, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus sendNetworkEapSimGsmAuthResponse(ArrayList<NetworkResponseEapSimGsmAuthParams> params) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            NetworkResponseEapSimGsmAuthParams.writeVectorToParcel(_hidl_request, params);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(67, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus sendNetworkEapSimGsmAuthFailure() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(68, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus sendNetworkEapSimUmtsAuthResponse(NetworkResponseEapSimUmtsAuthParams params) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            params.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(69, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus sendNetworkEapSimUmtsAutsResponse(byte[] auts) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(14);
            long _hidl_array_offset_0 = 0;
            for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 14; _hidl_index_0_0++) {
                _hidl_blob.putInt8(_hidl_array_offset_0, auts[_hidl_index_0_0]);
                _hidl_array_offset_0++;
            }
            _hidl_request.writeBuffer(_hidl_blob);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(70, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus sendNetworkEapSimUmtsAuthFailure() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(71, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public SupplicantStatus sendNetworkEapIdentityResponse(ArrayList<Byte> identity) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt8Vector(identity);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(72, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<String> interfaceChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256067662, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<String> _hidl_out_descriptors = _hidl_reply.readStringVector();
                return _hidl_out_descriptors;
            } finally {
                _hidl_reply.release();
            }
        }

        public String interfaceDescriptor() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256136003, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                String _hidl_out_descriptor = _hidl_reply.readString();
                return _hidl_out_descriptor;
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256398152, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<byte[]> _hidl_out_hashchain = new ArrayList();
                HwBlob _hidl_blob = _hidl_reply.readBuffer(16);
                int _hidl_vec_size = _hidl_blob.getInt32(8);
                HwBlob childBlob = _hidl_reply.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
                _hidl_out_hashchain.clear();
                for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                    Object _hidl_vec_element = new byte[32];
                    long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                    for (int _hidl_index_1_0 = 0; _hidl_index_1_0 < 32; _hidl_index_1_0++) {
                        _hidl_vec_element[_hidl_index_1_0] = childBlob.getInt8(_hidl_array_offset_1);
                        _hidl_array_offset_1++;
                    }
                    _hidl_out_hashchain.add(_hidl_vec_element);
                }
                return _hidl_out_hashchain;
            } finally {
                _hidl_reply.release();
            }
        }

        public void setHALInstrumentation() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256462420, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean linkToDeath(DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        public void ping() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256921159, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public DebugInfo getDebugInfo() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257049926, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                DebugInfo _hidl_out_info = new DebugInfo();
                _hidl_out_info.readFromParcel(_hidl_reply);
                return _hidl_out_info;
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifySyspropsChanged() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257120595, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean unlinkToDeath(DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public interface getGroupCipherCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    public interface getPairwiseCipherCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    public interface getPskPassphraseCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    public interface getPskCallback {
        void onValues(SupplicantStatus supplicantStatus, byte[] bArr);
    }

    public interface getWepKeyCallback {
        void onValues(SupplicantStatus supplicantStatus, ArrayList<Byte> arrayList);
    }

    public interface getWepTxKeyIdxCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    public interface getRequirePmfCallback {
        void onValues(SupplicantStatus supplicantStatus, boolean z);
    }

    public interface getEapMethodCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    public interface getEapPhase2MethodCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    public interface getEapIdentityCallback {
        void onValues(SupplicantStatus supplicantStatus, ArrayList<Byte> arrayList);
    }

    public interface getEapAnonymousIdentityCallback {
        void onValues(SupplicantStatus supplicantStatus, ArrayList<Byte> arrayList);
    }

    public interface getEapPasswordCallback {
        void onValues(SupplicantStatus supplicantStatus, ArrayList<Byte> arrayList);
    }

    public interface getEapCACertCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    public interface getEapCAPathCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    public interface getEapClientCertCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    public interface getEapPrivateKeyIdCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    public interface getEapSubjectMatchCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    public interface getEapAltSubjectMatchCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    public interface getEapEngineCallback {
        void onValues(SupplicantStatus supplicantStatus, boolean z);
    }

    public interface getEapEngineIDCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    public interface getEapDomainSuffixMatchCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    public interface getIdStrCallback {
        void onValues(SupplicantStatus supplicantStatus, String str);
    }

    public interface getWpsNfcConfigurationTokenCallback {
        void onValues(SupplicantStatus supplicantStatus, ArrayList<Byte> arrayList);
    }

    public interface getSsidCallback {
        void onValues(SupplicantStatus supplicantStatus, ArrayList<Byte> arrayList);
    }

    public interface getBssidCallback {
        void onValues(SupplicantStatus supplicantStatus, byte[] bArr);
    }

    public interface getScanSsidCallback {
        void onValues(SupplicantStatus supplicantStatus, boolean z);
    }

    public interface getKeyMgmtCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    public interface getProtoCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    public interface getAuthAlgCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    public static abstract class Stub extends HwBinder implements ISupplicantStaNetwork {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList(Arrays.asList(new String[]{ISupplicantStaNetwork.kInterfaceName, ISupplicantNetwork.kInterfaceName, IBase.kInterfaceName}));
        }

        public final String interfaceDescriptor() {
            return ISupplicantStaNetwork.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList(Arrays.asList(new byte[][]{new byte[]{(byte) -79, (byte) 46, (byte) -16, (byte) -67, (byte) -40, (byte) -92, (byte) -46, (byte) 71, (byte) -88, (byte) -90, (byte) -23, (byte) 96, (byte) -78, (byte) 39, (byte) -19, (byte) 50, (byte) 56, (byte) 63, (byte) 43, (byte) 2, (byte) 65, (byte) -11, (byte) 93, (byte) 103, (byte) -4, (byte) -22, (byte) 110, (byte) -1, (byte) 106, (byte) 103, (byte) 55, (byte) -6}, new byte[]{(byte) -51, (byte) -96, (byte) 16, (byte) 8, (byte) -64, (byte) 105, (byte) 34, (byte) -6, (byte) 55, (byte) -63, (byte) 33, (byte) 62, (byte) -101, (byte) -72, (byte) 49, (byte) -95, (byte) 9, (byte) -77, (byte) 23, (byte) 69, (byte) 50, Byte.MIN_VALUE, (byte) 86, (byte) 22, (byte) -5, (byte) 113, (byte) 97, (byte) -19, (byte) -60, (byte) 3, (byte) -122, (byte) 111}, new byte[]{(byte) -67, (byte) -38, (byte) -74, (byte) 24, (byte) 77, (byte) 122, (byte) 52, (byte) 109, (byte) -90, (byte) -96, (byte) 125, (byte) -64, (byte) -126, (byte) -116, (byte) -15, (byte) -102, (byte) 105, (byte) 111, (byte) 76, (byte) -86, (byte) 54, (byte) 17, (byte) -59, (byte) 31, (byte) 46, (byte) 20, (byte) 86, (byte) 90, (byte) 20, (byte) -76, (byte) 15, (byte) -39}}));
        }

        public final void setHALInstrumentation() {
        }

        public final boolean linkToDeath(DeathRecipient recipient, long cookie) {
            return true;
        }

        public final void ping() {
        }

        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = -1;
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        public final void notifySyspropsChanged() {
            SystemProperties.reportSyspropChanged();
        }

        public final boolean unlinkToDeath(DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (ISupplicantStaNetwork.kInterfaceName.equals(descriptor)) {
                return this;
            }
            return null;
        }

        public void registerAsService(String serviceName) throws RemoteException {
            registerService(serviceName);
        }

        public String toString() {
            return interfaceDescriptor() + "@Stub";
        }

        public void onTransact(int _hidl_code, HwParcel _hidl_request, HwParcel _hidl_reply, int _hidl_flags) throws RemoteException {
            final HwParcel hwParcel;
            SupplicantStatus _hidl_out_status;
            HwBlob _hidl_blob;
            long _hidl_array_offset_0;
            int _hidl_index_0_0;
            switch (_hidl_code) {
                case 1:
                    _hidl_request.enforceInterface(ISupplicantNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getId(new getIdCallback() {
                        public void onValues(SupplicantStatus status, int id) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt32(id);
                            hwParcel.send();
                        }
                    });
                    return;
                case 2:
                    _hidl_request.enforceInterface(ISupplicantNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getInterfaceName(new getInterfaceNameCallback() {
                        public void onValues(SupplicantStatus status, String name) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeString(name);
                            hwParcel.send();
                        }
                    });
                    return;
                case 3:
                    _hidl_request.enforceInterface(ISupplicantNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getType(new getTypeCallback() {
                        public void onValues(SupplicantStatus status, int type) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt32(type);
                            hwParcel.send();
                        }
                    });
                    return;
                case 4:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = registerCallback(ISupplicantStaNetworkCallback.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 5:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setSsid(_hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 6:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    byte[] bssid = new byte[6];
                    _hidl_blob = _hidl_request.readBuffer(6);
                    _hidl_array_offset_0 = 0;
                    for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
                        bssid[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
                        _hidl_array_offset_0++;
                    }
                    _hidl_out_status = setBssid(bssid);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 7:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setScanSsid(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 8:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setKeyMgmt(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 9:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setProto(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 10:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setAuthAlg(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 11:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setGroupCipher(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 12:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setPairwiseCipher(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 13:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setPskPassphrase(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 14:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    byte[] psk = new byte[32];
                    _hidl_blob = _hidl_request.readBuffer(32);
                    _hidl_array_offset_0 = 0;
                    for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 32; _hidl_index_0_0++) {
                        psk[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
                        _hidl_array_offset_0++;
                    }
                    _hidl_out_status = setPsk(psk);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 15:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setWepKey(_hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 16:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setWepTxKeyIdx(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 17:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setRequirePmf(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 18:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setEapMethod(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 19:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setEapPhase2Method(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 20:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setEapIdentity(_hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 21:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setEapAnonymousIdentity(_hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 22:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setEapPassword(_hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 23:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setEapCACert(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 24:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setEapCAPath(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 25:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setEapClientCert(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 26:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setEapPrivateKeyId(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 27:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setEapSubjectMatch(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 28:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setEapAltSubjectMatch(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 29:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setEapEngine(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 30:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setEapEngineID(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 31:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setEapDomainSuffixMatch(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 32:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setProactiveKeyCaching(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 33:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setIdStr(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 34:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = setUpdateIdentifier(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 35:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getSsid(new getSsidCallback() {
                        public void onValues(SupplicantStatus status, ArrayList<Byte> ssid) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt8Vector(ssid);
                            hwParcel.send();
                        }
                    });
                    return;
                case ReasonCode.STA_LEAVING /*36*/:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getBssid(new getBssidCallback() {
                        public void onValues(SupplicantStatus status, byte[] bssid) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            HwBlob _hidl_blob = new HwBlob(6);
                            long _hidl_array_offset_0 = 0;
                            for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
                                _hidl_blob.putInt8(_hidl_array_offset_0, bssid[_hidl_index_0_0]);
                                _hidl_array_offset_0++;
                            }
                            hwParcel.writeBuffer(_hidl_blob);
                            hwParcel.send();
                        }
                    });
                    return;
                case 37:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getScanSsid(new getScanSsidCallback() {
                        public void onValues(SupplicantStatus status, boolean enabled) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeBool(enabled);
                            hwParcel.send();
                        }
                    });
                    return;
                case 38:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getKeyMgmt(new getKeyMgmtCallback() {
                        public void onValues(SupplicantStatus status, int keyMgmtMask) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt32(keyMgmtMask);
                            hwParcel.send();
                        }
                    });
                    return;
                case 39:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getProto(new getProtoCallback() {
                        public void onValues(SupplicantStatus status, int protoMask) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt32(protoMask);
                            hwParcel.send();
                        }
                    });
                    return;
                case StatusCode.INVALID_IE /*40*/:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getAuthAlg(new getAuthAlgCallback() {
                        public void onValues(SupplicantStatus status, int authAlgMask) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt32(authAlgMask);
                            hwParcel.send();
                        }
                    });
                    return;
                case StatusCode.GROUP_CIPHER_NOT_VALID /*41*/:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getGroupCipher(new getGroupCipherCallback() {
                        public void onValues(SupplicantStatus status, int groupCipherMask) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt32(groupCipherMask);
                            hwParcel.send();
                        }
                    });
                    return;
                case StatusCode.PAIRWISE_CIPHER_NOT_VALID /*42*/:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getPairwiseCipher(new getPairwiseCipherCallback() {
                        public void onValues(SupplicantStatus status, int pairwiseCipherMask) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt32(pairwiseCipherMask);
                            hwParcel.send();
                        }
                    });
                    return;
                case StatusCode.AKMP_NOT_VALID /*43*/:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getPskPassphrase(new getPskPassphraseCallback() {
                        public void onValues(SupplicantStatus status, String psk) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeString(psk);
                            hwParcel.send();
                        }
                    });
                    return;
                case StatusCode.UNSUPPORTED_RSN_IE_VERSION /*44*/:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getPsk(new getPskCallback() {
                        public void onValues(SupplicantStatus status, byte[] psk) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            HwBlob _hidl_blob = new HwBlob(32);
                            long _hidl_array_offset_0 = 0;
                            for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 32; _hidl_index_0_0++) {
                                _hidl_blob.putInt8(_hidl_array_offset_0, psk[_hidl_index_0_0]);
                                _hidl_array_offset_0++;
                            }
                            hwParcel.writeBuffer(_hidl_blob);
                            hwParcel.send();
                        }
                    });
                    return;
                case 45:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getWepKey(_hidl_request.readInt32(), new getWepKeyCallback() {
                        public void onValues(SupplicantStatus status, ArrayList<Byte> wepKey) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt8Vector(wepKey);
                            hwParcel.send();
                        }
                    });
                    return;
                case 46:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getWepTxKeyIdx(new getWepTxKeyIdxCallback() {
                        public void onValues(SupplicantStatus status, int keyIdx) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt32(keyIdx);
                            hwParcel.send();
                        }
                    });
                    return;
                case 47:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getRequirePmf(new getRequirePmfCallback() {
                        public void onValues(SupplicantStatus status, boolean enabled) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeBool(enabled);
                            hwParcel.send();
                        }
                    });
                    return;
                case 48:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getEapMethod(new getEapMethodCallback() {
                        public void onValues(SupplicantStatus status, int method) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt32(method);
                            hwParcel.send();
                        }
                    });
                    return;
                case 49:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getEapPhase2Method(new getEapPhase2MethodCallback() {
                        public void onValues(SupplicantStatus status, int method) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt32(method);
                            hwParcel.send();
                        }
                    });
                    return;
                case 50:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getEapIdentity(new getEapIdentityCallback() {
                        public void onValues(SupplicantStatus status, ArrayList<Byte> identity) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt8Vector(identity);
                            hwParcel.send();
                        }
                    });
                    return;
                case 51:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getEapAnonymousIdentity(new getEapAnonymousIdentityCallback() {
                        public void onValues(SupplicantStatus status, ArrayList<Byte> identity) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt8Vector(identity);
                            hwParcel.send();
                        }
                    });
                    return;
                case 52:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getEapPassword(new getEapPasswordCallback() {
                        public void onValues(SupplicantStatus status, ArrayList<Byte> password) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt8Vector(password);
                            hwParcel.send();
                        }
                    });
                    return;
                case 53:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getEapCACert(new getEapCACertCallback() {
                        public void onValues(SupplicantStatus status, String path) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeString(path);
                            hwParcel.send();
                        }
                    });
                    return;
                case 54:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getEapCAPath(new getEapCAPathCallback() {
                        public void onValues(SupplicantStatus status, String path) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeString(path);
                            hwParcel.send();
                        }
                    });
                    return;
                case 55:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getEapClientCert(new getEapClientCertCallback() {
                        public void onValues(SupplicantStatus status, String path) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeString(path);
                            hwParcel.send();
                        }
                    });
                    return;
                case 56:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getEapPrivateKeyId(new getEapPrivateKeyIdCallback() {
                        public void onValues(SupplicantStatus status, String id) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeString(id);
                            hwParcel.send();
                        }
                    });
                    return;
                case 57:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getEapSubjectMatch(new getEapSubjectMatchCallback() {
                        public void onValues(SupplicantStatus status, String match) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeString(match);
                            hwParcel.send();
                        }
                    });
                    return;
                case 58:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getEapAltSubjectMatch(new getEapAltSubjectMatchCallback() {
                        public void onValues(SupplicantStatus status, String match) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeString(match);
                            hwParcel.send();
                        }
                    });
                    return;
                case 59:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getEapEngine(new getEapEngineCallback() {
                        public void onValues(SupplicantStatus status, boolean enabled) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeBool(enabled);
                            hwParcel.send();
                        }
                    });
                    return;
                case 60:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getEapEngineID(new getEapEngineIDCallback() {
                        public void onValues(SupplicantStatus status, String id) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeString(id);
                            hwParcel.send();
                        }
                    });
                    return;
                case 61:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getEapDomainSuffixMatch(new getEapDomainSuffixMatchCallback() {
                        public void onValues(SupplicantStatus status, String match) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeString(match);
                            hwParcel.send();
                        }
                    });
                    return;
                case 62:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getIdStr(new getIdStrCallback() {
                        public void onValues(SupplicantStatus status, String idStr) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeString(idStr);
                            hwParcel.send();
                        }
                    });
                    return;
                case 63:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getWpsNfcConfigurationToken(new getWpsNfcConfigurationTokenCallback() {
                        public void onValues(SupplicantStatus status, ArrayList<Byte> token) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt8Vector(token);
                            hwParcel.send();
                        }
                    });
                    return;
                case 64:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = enable(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 65:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = disable();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ReasonCode.MESH_CHANNEL_SWITCH_UNSPECIFIED /*66*/:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = select();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case StatusCode.REQ_REFUSED_SSPN /*67*/:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = sendNetworkEapSimGsmAuthResponse(NetworkResponseEapSimGsmAuthParams.readVectorFromParcel(_hidl_request));
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case StatusCode.REQ_REFUSED_UNAUTH_ACCESS /*68*/:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = sendNetworkEapSimGsmAuthFailure();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 69:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    NetworkResponseEapSimUmtsAuthParams params = new NetworkResponseEapSimUmtsAuthParams();
                    params.readFromParcel(_hidl_request);
                    _hidl_out_status = sendNetworkEapSimUmtsAuthResponse(params);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 70:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    byte[] auts = new byte[14];
                    _hidl_blob = _hidl_request.readBuffer(14);
                    _hidl_array_offset_0 = 0;
                    for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 14; _hidl_index_0_0++) {
                        auts[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
                        _hidl_array_offset_0++;
                    }
                    _hidl_out_status = sendNetworkEapSimUmtsAutsResponse(auts);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 71:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = sendNetworkEapSimUmtsAuthFailure();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case StatusCode.INVALID_RSNIE /*72*/:
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    _hidl_out_status = sendNetworkEapIdentityResponse(_hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 256067662:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    ArrayList<String> _hidl_out_descriptors = interfaceChain();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeStringVector(_hidl_out_descriptors);
                    _hidl_reply.send();
                    return;
                case 256131655:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 256136003:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    String _hidl_out_descriptor = interfaceDescriptor();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeString(_hidl_out_descriptor);
                    _hidl_reply.send();
                    return;
                case 256398152:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    ArrayList<byte[]> _hidl_out_hashchain = getHashChain();
                    _hidl_reply.writeStatus(0);
                    _hidl_blob = new HwBlob(16);
                    int _hidl_vec_size = _hidl_out_hashchain.size();
                    _hidl_blob.putInt32(8, _hidl_vec_size);
                    _hidl_blob.putBool(12, false);
                    HwBlob hwBlob = new HwBlob(_hidl_vec_size * 32);
                    for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                        long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                        for (int _hidl_index_1_0 = 0; _hidl_index_1_0 < 32; _hidl_index_1_0++) {
                            hwBlob.putInt8(_hidl_array_offset_1, ((byte[]) _hidl_out_hashchain.get(_hidl_index_0))[_hidl_index_1_0]);
                            _hidl_array_offset_1++;
                        }
                    }
                    _hidl_blob.putBlob(0, hwBlob);
                    _hidl_reply.writeBuffer(_hidl_blob);
                    _hidl_reply.send();
                    return;
                case 256462420:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    setHALInstrumentation();
                    return;
                case 257049926:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    DebugInfo _hidl_out_info = getDebugInfo();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_info.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 257120595:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    notifySyspropsChanged();
                    return;
                default:
                    return;
            }
        }
    }

    IHwBinder asBinder();

    SupplicantStatus disable() throws RemoteException;

    SupplicantStatus enable(boolean z) throws RemoteException;

    void getAuthAlg(getAuthAlgCallback getauthalgcallback) throws RemoteException;

    void getBssid(getBssidCallback getbssidcallback) throws RemoteException;

    DebugInfo getDebugInfo() throws RemoteException;

    void getEapAltSubjectMatch(getEapAltSubjectMatchCallback geteapaltsubjectmatchcallback) throws RemoteException;

    void getEapAnonymousIdentity(getEapAnonymousIdentityCallback geteapanonymousidentitycallback) throws RemoteException;

    void getEapCACert(getEapCACertCallback geteapcacertcallback) throws RemoteException;

    void getEapCAPath(getEapCAPathCallback geteapcapathcallback) throws RemoteException;

    void getEapClientCert(getEapClientCertCallback geteapclientcertcallback) throws RemoteException;

    void getEapDomainSuffixMatch(getEapDomainSuffixMatchCallback geteapdomainsuffixmatchcallback) throws RemoteException;

    void getEapEngine(getEapEngineCallback geteapenginecallback) throws RemoteException;

    void getEapEngineID(getEapEngineIDCallback geteapengineidcallback) throws RemoteException;

    void getEapIdentity(getEapIdentityCallback geteapidentitycallback) throws RemoteException;

    void getEapMethod(getEapMethodCallback geteapmethodcallback) throws RemoteException;

    void getEapPassword(getEapPasswordCallback geteappasswordcallback) throws RemoteException;

    void getEapPhase2Method(getEapPhase2MethodCallback geteapphase2methodcallback) throws RemoteException;

    void getEapPrivateKeyId(getEapPrivateKeyIdCallback geteapprivatekeyidcallback) throws RemoteException;

    void getEapSubjectMatch(getEapSubjectMatchCallback geteapsubjectmatchcallback) throws RemoteException;

    void getGroupCipher(getGroupCipherCallback getgroupciphercallback) throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    void getIdStr(getIdStrCallback getidstrcallback) throws RemoteException;

    void getKeyMgmt(getKeyMgmtCallback getkeymgmtcallback) throws RemoteException;

    void getPairwiseCipher(getPairwiseCipherCallback getpairwiseciphercallback) throws RemoteException;

    void getProto(getProtoCallback getprotocallback) throws RemoteException;

    void getPsk(getPskCallback getpskcallback) throws RemoteException;

    void getPskPassphrase(getPskPassphraseCallback getpskpassphrasecallback) throws RemoteException;

    void getRequirePmf(getRequirePmfCallback getrequirepmfcallback) throws RemoteException;

    void getScanSsid(getScanSsidCallback getscanssidcallback) throws RemoteException;

    void getSsid(getSsidCallback getssidcallback) throws RemoteException;

    void getWepKey(int i, getWepKeyCallback getwepkeycallback) throws RemoteException;

    void getWepTxKeyIdx(getWepTxKeyIdxCallback getweptxkeyidxcallback) throws RemoteException;

    void getWpsNfcConfigurationToken(getWpsNfcConfigurationTokenCallback getwpsnfcconfigurationtokencallback) throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    boolean linkToDeath(DeathRecipient deathRecipient, long j) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    void ping() throws RemoteException;

    SupplicantStatus registerCallback(ISupplicantStaNetworkCallback iSupplicantStaNetworkCallback) throws RemoteException;

    SupplicantStatus select() throws RemoteException;

    SupplicantStatus sendNetworkEapIdentityResponse(ArrayList<Byte> arrayList) throws RemoteException;

    SupplicantStatus sendNetworkEapSimGsmAuthFailure() throws RemoteException;

    SupplicantStatus sendNetworkEapSimGsmAuthResponse(ArrayList<NetworkResponseEapSimGsmAuthParams> arrayList) throws RemoteException;

    SupplicantStatus sendNetworkEapSimUmtsAuthFailure() throws RemoteException;

    SupplicantStatus sendNetworkEapSimUmtsAuthResponse(NetworkResponseEapSimUmtsAuthParams networkResponseEapSimUmtsAuthParams) throws RemoteException;

    SupplicantStatus sendNetworkEapSimUmtsAutsResponse(byte[] bArr) throws RemoteException;

    SupplicantStatus setAuthAlg(int i) throws RemoteException;

    SupplicantStatus setBssid(byte[] bArr) throws RemoteException;

    SupplicantStatus setEapAltSubjectMatch(String str) throws RemoteException;

    SupplicantStatus setEapAnonymousIdentity(ArrayList<Byte> arrayList) throws RemoteException;

    SupplicantStatus setEapCACert(String str) throws RemoteException;

    SupplicantStatus setEapCAPath(String str) throws RemoteException;

    SupplicantStatus setEapClientCert(String str) throws RemoteException;

    SupplicantStatus setEapDomainSuffixMatch(String str) throws RemoteException;

    SupplicantStatus setEapEngine(boolean z) throws RemoteException;

    SupplicantStatus setEapEngineID(String str) throws RemoteException;

    SupplicantStatus setEapIdentity(ArrayList<Byte> arrayList) throws RemoteException;

    SupplicantStatus setEapMethod(int i) throws RemoteException;

    SupplicantStatus setEapPassword(ArrayList<Byte> arrayList) throws RemoteException;

    SupplicantStatus setEapPhase2Method(int i) throws RemoteException;

    SupplicantStatus setEapPrivateKeyId(String str) throws RemoteException;

    SupplicantStatus setEapSubjectMatch(String str) throws RemoteException;

    SupplicantStatus setGroupCipher(int i) throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    SupplicantStatus setIdStr(String str) throws RemoteException;

    SupplicantStatus setKeyMgmt(int i) throws RemoteException;

    SupplicantStatus setPairwiseCipher(int i) throws RemoteException;

    SupplicantStatus setProactiveKeyCaching(boolean z) throws RemoteException;

    SupplicantStatus setProto(int i) throws RemoteException;

    SupplicantStatus setPsk(byte[] bArr) throws RemoteException;

    SupplicantStatus setPskPassphrase(String str) throws RemoteException;

    SupplicantStatus setRequirePmf(boolean z) throws RemoteException;

    SupplicantStatus setScanSsid(boolean z) throws RemoteException;

    SupplicantStatus setSsid(ArrayList<Byte> arrayList) throws RemoteException;

    SupplicantStatus setUpdateIdentifier(int i) throws RemoteException;

    SupplicantStatus setWepKey(int i, ArrayList<Byte> arrayList) throws RemoteException;

    SupplicantStatus setWepTxKeyIdx(int i) throws RemoteException;

    boolean unlinkToDeath(DeathRecipient deathRecipient) throws RemoteException;

    static ISupplicantStaNetwork asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof ISupplicantStaNetwork)) {
            return (ISupplicantStaNetwork) iface;
        }
        ISupplicantStaNetwork proxy = new Proxy(binder);
        try {
            for (String descriptor : proxy.interfaceChain()) {
                if (descriptor.equals(kInterfaceName)) {
                    return proxy;
                }
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    static ISupplicantStaNetwork castFrom(IHwInterface iface) {
        return iface == null ? null : asInterface(iface.asBinder());
    }

    static ISupplicantStaNetwork getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static ISupplicantStaNetwork getService() throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, HalDeviceManager.HAL_INSTANCE_NAME));
    }
}
