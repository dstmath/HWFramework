package android.net.wifi;

import android.bluetooth.BluetoothAssignedNumbers;
import android.bluetooth.BluetoothAvrcp;
import android.media.ToneGenerator;
import android.net.ProxyInfo;
import android.net.wifi.ScanResult.InformationElement;
import android.os.BatteryStats;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.rms.HwSysResource;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Locale;

public class WifiSsid implements Parcelable {
    public static final Creator<WifiSsid> CREATOR = null;
    private static final int HEX_RADIX = 16;
    public static final String NONE = "<unknown ssid>";
    private static final String TAG = "WifiSsid";
    public final ByteArrayOutputStream octets;
    public String oriSsid;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiSsid.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiSsid.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiSsid.<clinit>():void");
    }

    private WifiSsid() {
        this.octets = new ByteArrayOutputStream(32);
        this.oriSsid = ProxyInfo.LOCAL_EXCL_LIST;
    }

    public static WifiSsid createFromAsciiEncoded(String asciiEncoded) {
        WifiSsid a = new WifiSsid();
        a.convertToBytes(asciiEncoded);
        a.oriSsid = "P\"" + asciiEncoded + "\"";
        return a;
    }

    public static WifiSsid createFromHex(String hexStr) {
        WifiSsid a = new WifiSsid();
        if (hexStr == null) {
            return a;
        }
        if (hexStr.startsWith("0x") || hexStr.startsWith("0X")) {
            hexStr = hexStr.substring(2);
        }
        for (int i = 0; i < hexStr.length() - 1; i += 2) {
            int val;
            try {
                val = Integer.parseInt(hexStr.substring(i, i + 2), HEX_RADIX);
            } catch (NumberFormatException e) {
                val = 0;
            }
            a.octets.write(val);
        }
        return a;
    }

    private void convertToBytes(String asciiEncoded) {
        int i = 0;
        while (i < asciiEncoded.length()) {
            char c = asciiEncoded.charAt(i);
            switch (c) {
                case ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK /*92*/:
                    i++;
                    if (i < asciiEncoded.length()) {
                        int val;
                        switch (asciiEncoded.charAt(i)) {
                            case HwSysResource.APPMNGWHITELIST /*34*/:
                                this.octets.write(34);
                                i++;
                                break;
                            case BatteryStats.STEP_LEVEL_INITIAL_MODE_SHIFT /*48*/:
                            case ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_PING_RING /*49*/:
                            case InformationElement.EID_EXTENDED_SUPPORTED_RATES /*50*/:
                            case ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_PAT6 /*51*/:
                            case ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_PAT7 /*52*/:
                            case ToneGenerator.TONE_CDMA_HIGH_L /*53*/:
                            case ToneGenerator.TONE_CDMA_MED_L /*54*/:
                            case ToneGenerator.TONE_CDMA_LOW_L /*55*/:
                                val = asciiEncoded.charAt(i) - 48;
                                i++;
                                if (asciiEncoded.charAt(i) >= '0' && asciiEncoded.charAt(i) <= '7') {
                                    val = ((val * 8) + asciiEncoded.charAt(i)) - 48;
                                    i++;
                                }
                                if (asciiEncoded.charAt(i) >= '0' && asciiEncoded.charAt(i) <= '7') {
                                    val = ((val * 8) + asciiEncoded.charAt(i)) - 48;
                                    i++;
                                }
                                this.octets.write(val);
                                break;
                            case ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK /*92*/:
                                this.octets.write(92);
                                i++;
                                break;
                            case HwSysResource.MAINSERVICES /*101*/:
                                this.octets.write(27);
                                i++;
                                break;
                            case BluetoothAssignedNumbers.SUMMIT_DATA_COMMUNICATIONS /*110*/:
                                this.octets.write(10);
                                i++;
                                break;
                            case BluetoothAvrcp.PASSTHROUGH_ID_F2 /*114*/:
                                this.octets.write(13);
                                i++;
                                break;
                            case BluetoothAvrcp.PASSTHROUGH_ID_F4 /*116*/:
                                this.octets.write(9);
                                i++;
                                break;
                            case BluetoothAssignedNumbers.NIKE /*120*/:
                                i++;
                                try {
                                    val = Integer.parseInt(asciiEncoded.substring(i, i + 2), HEX_RADIX);
                                } catch (NumberFormatException e) {
                                    val = -1;
                                }
                                if (val >= 0) {
                                    this.octets.write(val);
                                    i += 2;
                                    break;
                                }
                                val = Character.digit(asciiEncoded.charAt(i), HEX_RADIX);
                                if (val < 0) {
                                    break;
                                }
                                this.octets.write(val);
                                i++;
                                break;
                            default:
                                break;
                        }
                    }
                    this.octets.write(92);
                    break;
                default:
                    this.octets.write(c);
                    i++;
                    break;
            }
        }
    }

    public String toString() {
        byte[] ssidBytes = this.octets.toByteArray();
        if (this.octets.size() <= 0 || isArrayAllZeroes(ssidBytes)) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
        String defaultCharset = "UTF-8";
        String[] charsets = new String[]{"UTF-8", "GBK", "US-ASCII", "UTF-16"};
        for (int i = 0; i < charsets.length; i++) {
            if (isEncodedWithCharset(ssidBytes, charsets[i])) {
                return encodingWithCharset(ssidBytes, charsets[i]);
            }
        }
        return encodingWithCharset(ssidBytes, defaultCharset);
    }

    private boolean isArrayAllZeroes(byte[] ssidBytes) {
        for (byte b : ssidBytes) {
            if (b != null) {
                return false;
            }
        }
        return true;
    }

    public boolean isHidden() {
        return isArrayAllZeroes(this.octets.toByteArray());
    }

    public byte[] getOctets() {
        return this.octets.toByteArray();
    }

    public String getHexString() {
        String out = "0x";
        byte[] ssidbytes = getOctets();
        for (int i = 0; i < this.octets.size(); i++) {
            out = out + String.format(Locale.US, "%02x", new Object[]{Byte.valueOf(ssidbytes[i])});
        }
        return this.octets.size() > 0 ? out : null;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.octets.size());
        dest.writeByteArray(this.octets.toByteArray());
        try {
            byte[] buff = this.oriSsid.getBytes("UTF-8");
            dest.writeInt(buff.length);
            if (buff.length > 0) {
                dest.writeByteArray(buff);
            }
        } catch (UnsupportedEncodingException e) {
            dest.writeInt(0);
        }
    }

    private boolean isEncodedWithCharset(byte[] buff, String charsetName) {
        try {
            if (Charset.isSupported(charsetName)) {
                byte[] newBuff = new String(buff, charsetName).getBytes(charsetName);
                if (buff.length == newBuff.length) {
                    for (int i = 0; i < buff.length; i++) {
                        if (buff[i] != newBuff[i]) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        } catch (UnsupportedEncodingException e) {
        }
        return false;
    }

    private String encodingWithCharset(byte[] buff, String charsetName) {
        CharsetDecoder decoder = Charset.forName(charsetName).newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        CharBuffer out = CharBuffer.allocate(buff.length * 5);
        CoderResult result = decoder.decode(ByteBuffer.wrap(buff), out, true);
        out.flip();
        if (result.isError()) {
            return NONE;
        }
        return out.toString();
    }
}
