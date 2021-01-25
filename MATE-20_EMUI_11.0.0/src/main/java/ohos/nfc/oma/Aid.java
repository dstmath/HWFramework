package ohos.nfc.oma;

import java.util.Arrays;
import java.util.Locale;
import ohos.media.camera.params.Metadata;

public class Aid {
    public static final String AID_CRS = "a00000015143525300";
    public static final String AID_PPSE = "325041592e5359532e4444463031";
    public static final String AID_PSE = "315041592e5359532e4444463031";
    private static final int MAX_AID_BYTES = 16;
    private static final int MIN_AID_BYTES = 5;
    private byte[] mAidValue = null;

    public Aid(byte[] bArr, int i, int i2) {
        int i3;
        if (bArr != null && (i3 = i2 + i) <= bArr.length) {
            this.mAidValue = Arrays.copyOfRange(bArr, i, i3);
        }
    }

    public boolean isAidValid() {
        byte[] bArr = this.mAidValue;
        return bArr != null && bArr.length >= 5 && bArr.length <= 16;
    }

    public byte[] getAidBytes() {
        byte[] bArr = this.mAidValue;
        return bArr != null ? Arrays.copyOf(bArr, bArr.length) : new byte[0];
    }

    public static byte[] hexStringToBytes(String str) {
        if (str == null || str.length() == 0) {
            return new byte[0];
        }
        int length = str.length();
        if (length % 2 != 0) {
            str = '0' + str;
            length++;
        }
        byte[] bArr = new byte[(length / 2)];
        for (int i = 0; i < length; i += 2) {
            bArr[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return bArr;
    }

    public static String byteArrayToHexString(byte[] bArr) {
        if (bArr == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bArr.length; i++) {
            sb.append(String.format(Locale.ENGLISH, "%02x", Integer.valueOf(bArr[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE)));
        }
        return sb.toString();
    }
}
