package android.net.wifi;

import android.annotation.UnsupportedAppUsage;
import android.net.wifi.hwUtil.HwWifiSsidEx;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Locale;

public class WifiSsid implements Parcelable {
    @UnsupportedAppUsage
    public static final Parcelable.Creator<WifiSsid> CREATOR = new Parcelable.Creator<WifiSsid>() {
        /* class android.net.wifi.WifiSsid.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiSsid createFromParcel(Parcel in) {
            WifiSsid ssid = new WifiSsid();
            int length = in.readInt();
            byte[] b = new byte[length];
            in.readByteArray(b);
            ssid.octets.write(b, 0, length);
            ssid.oriSsid = HwWifiSsidEx.readOriSsid(in);
            return ssid;
        }

        @Override // android.os.Parcelable.Creator
        public WifiSsid[] newArray(int size) {
            return new WifiSsid[size];
        }
    };
    private static final int HEX_RADIX = 16;
    @UnsupportedAppUsage
    public static final String NONE = "<unknown ssid>";
    private static final String TAG = "WifiSsid";
    @UnsupportedAppUsage
    public final ByteArrayOutputStream octets;
    public String oriSsid;

    private WifiSsid() {
        this.octets = new ByteArrayOutputStream(32);
        this.oriSsid = "";
    }

    public static WifiSsid createFromByteArray(byte[] ssid) {
        WifiSsid wifiSsid = new WifiSsid();
        if (ssid != null) {
            wifiSsid.octets.write(ssid, 0, ssid.length);
        }
        return wifiSsid;
    }

    @UnsupportedAppUsage
    public static WifiSsid createFromAsciiEncoded(String asciiEncoded) {
        WifiSsid a = new WifiSsid();
        a.convertToBytes(asciiEncoded);
        a.oriSsid = "P\"" + asciiEncoded + "\"";
        return a;
    }

    public static WifiSsid createFromHex(String hexStr) {
        int val;
        WifiSsid a = new WifiSsid();
        if (hexStr == null) {
            return a;
        }
        if (hexStr.startsWith("0x") || hexStr.startsWith("0X")) {
            hexStr = hexStr.substring(2);
        }
        for (int i = 0; i < hexStr.length() - 1; i += 2) {
            try {
                val = Integer.parseInt(hexStr.substring(i, i + 2), 16);
            } catch (NumberFormatException e) {
                val = 0;
            }
            a.octets.write(val);
        }
        return a;
    }

    private void convertToBytes(String asciiEncoded) {
        int val;
        int i = 0;
        while (i < asciiEncoded.length()) {
            char c = asciiEncoded.charAt(i);
            if (c != '\\') {
                this.octets.write(c);
                i++;
            } else {
                i++;
                if (i >= asciiEncoded.length()) {
                    this.octets.write(92);
                } else {
                    char charAt = asciiEncoded.charAt(i);
                    if (charAt == '\"') {
                        this.octets.write(34);
                        i++;
                    } else if (charAt == '\\') {
                        this.octets.write(92);
                        i++;
                    } else if (charAt == 'e') {
                        this.octets.write(27);
                        i++;
                    } else if (charAt == 'n') {
                        this.octets.write(10);
                        i++;
                    } else if (charAt == 'r') {
                        this.octets.write(13);
                        i++;
                    } else if (charAt == 't') {
                        this.octets.write(9);
                        i++;
                    } else if (charAt != 'x') {
                        switch (charAt) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                                int val2 = asciiEncoded.charAt(i) - '0';
                                i++;
                                if (asciiEncoded.charAt(i) >= '0' && asciiEncoded.charAt(i) <= '7') {
                                    val2 = ((val2 * 8) + asciiEncoded.charAt(i)) - 48;
                                    i++;
                                }
                                if (asciiEncoded.charAt(i) >= '0' && asciiEncoded.charAt(i) <= '7') {
                                    val2 = ((val2 * 8) + asciiEncoded.charAt(i)) - 48;
                                    i++;
                                }
                                this.octets.write(val2);
                                continue;
                        }
                    } else {
                        i++;
                        try {
                            val = Integer.parseInt(asciiEncoded.substring(i, i + 2), 16);
                        } catch (NumberFormatException e) {
                            val = -1;
                        } catch (StringIndexOutOfBoundsException e2) {
                            val = -1;
                        }
                        if (val < 0) {
                            int val3 = Character.digit(asciiEncoded.charAt(i), 16);
                            if (val3 >= 0) {
                                this.octets.write(val3);
                                i++;
                            }
                        } else {
                            this.octets.write(val);
                            i += 2;
                        }
                    }
                }
            }
        }
    }

    public String toString() {
        byte[] ssidBytes = this.octets.toByteArray();
        if (this.octets.size() <= 0 || isArrayAllZeroes(ssidBytes)) {
            return "";
        }
        return HwWifiSsidEx.encodingWithCharset(ssidBytes);
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof WifiSsid)) {
            return false;
        }
        return Arrays.equals(this.octets.toByteArray(), ((WifiSsid) thatObject).octets.toByteArray());
    }

    public int hashCode() {
        return Arrays.hashCode(this.octets.toByteArray());
    }

    private boolean isArrayAllZeroes(byte[] ssidBytes) {
        for (byte b : ssidBytes) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isHidden() {
        return isArrayAllZeroes(this.octets.toByteArray());
    }

    @UnsupportedAppUsage
    public byte[] getOctets() {
        return this.octets.toByteArray();
    }

    public String getHexString() {
        String out = "0x";
        byte[] ssidbytes = getOctets();
        for (int i = 0; i < this.octets.size(); i++) {
            out = out + String.format(Locale.US, "%02x", Byte.valueOf(ssidbytes[i]));
        }
        if (this.octets.size() > 0) {
            return out;
        }
        return null;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.octets.size());
        dest.writeByteArray(this.octets.toByteArray());
        HwWifiSsidEx.writeOriSsid(this.oriSsid, dest);
    }
}
