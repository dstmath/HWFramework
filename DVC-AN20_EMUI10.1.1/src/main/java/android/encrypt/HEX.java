package android.encrypt;

public class HEX {
    public static String encode(byte[] btData, int iLen) {
        StringBuffer l_stHex = new StringBuffer();
        if (btData == null) {
            return null;
        }
        if (iLen <= 0 || iLen > btData.length) {
            iLen = btData.length;
        }
        for (int ii = 0; ii < iLen; ii++) {
            String l_stTmp = Integer.toHexString(btData[ii] & 255);
            if (l_stTmp.length() == 1) {
                l_stTmp = "0" + l_stTmp;
            }
            l_stHex.append(l_stTmp.toUpperCase());
        }
        return l_stHex.toString();
    }

    public static byte[] decode(String stData) {
        if (stData == null) {
            return null;
        }
        int l_iLen = stData.length();
        if (l_iLen % 2 != 0) {
            return null;
        }
        String l_stData = stData.toUpperCase();
        for (int ii = 0; ii < l_iLen; ii++) {
            char l_cTmp = l_stData.charAt(ii);
            if (('0' > l_cTmp || l_cTmp > '9') && ('A' > l_cTmp || l_cTmp > 'F')) {
                return null;
            }
        }
        int l_iLen2 = l_iLen / 2;
        byte[] l_btData = new byte[l_iLen2];
        byte[] l_btTmp = new byte[2];
        int jj = 0;
        for (int ii2 = 0; ii2 < l_iLen2; ii2++) {
            int jj2 = jj + 1;
            l_btTmp[0] = (byte) l_stData.charAt(jj);
            jj = jj2 + 1;
            l_btTmp[1] = (byte) l_stData.charAt(jj2);
            for (int kk = 0; kk < 2; kk++) {
                if (65 > l_btTmp[kk] || l_btTmp[kk] > 70) {
                    l_btTmp[kk] = (byte) (l_btTmp[kk] - 48);
                } else {
                    l_btTmp[kk] = (byte) (l_btTmp[kk] - 55);
                }
            }
            l_btData[ii2] = (byte) ((l_btTmp[0] << 4) | l_btTmp[1]);
        }
        return l_btData;
    }

    private HEX() {
    }
}
