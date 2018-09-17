package tmsdkobf;

public class lq {
    public static byte[] aO(int i) {
        return new byte[]{(byte) ((byte) (i & 255)), (byte) ((byte) ((i >> 8) & 255)), (byte) ((byte) ((i >> 16) & 255)), (byte) ((byte) ((i >> 24) & 255))};
    }

    public static byte[] at(String str) {
        int length = str.length() / 2;
        byte[] bArr = new byte[length];
        char[] toCharArray = str.toCharArray();
        for (int i = 0; i < length; i++) {
            int i2 = i * 2;
            bArr[i] = (byte) ((byte) ((b(toCharArray[i2]) << 4) | b(toCharArray[i2 + 1])));
        }
        return bArr;
    }

    private static byte b(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte[] bJ(String str) {
        byte[] bArr = new byte[str.length()];
        char[] toCharArray = str.toCharArray();
        for (int i = 0; i < str.length(); i++) {
            bArr[i] = (byte) ((byte) toCharArray[i]);
        }
        return bArr;
    }

    public static final String bytesToHexString(byte[] bArr) {
        if (bArr == null) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer(bArr.length);
        for (byte b : bArr) {
            String toHexString = Integer.toHexString(b & 255);
            if (toHexString.length() < 2) {
                stringBuffer.append(0);
            }
            stringBuffer.append(toHexString.toUpperCase());
        }
        return stringBuffer.toString();
    }

    public static final String bytesToString(byte[] bArr) {
        if (bArr == null) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer(bArr.length);
        for (byte b : bArr) {
            stringBuffer.append((char) b);
        }
        return stringBuffer.toString().toUpperCase();
    }

    public static int k(byte[] bArr) {
        return bArr.length == 4 ? (((bArr[0] & 255) | ((bArr[1] & 255) << 8)) | ((bArr[2] & 255) << 16)) | ((bArr[3] & 255) << 24) : 0;
    }
}
