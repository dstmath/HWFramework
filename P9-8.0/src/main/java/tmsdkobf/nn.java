package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import tmsdk.common.utils.f;

public class nn {
    public static <T extends JceStruct> T a(byte[] bArr, T -l_3_R, boolean z) {
        if (bArr == null || -l_3_R == null) {
            return null;
        }
        if (z) {
            -l_3_R = -l_3_R.newInit();
        }
        try {
            -l_3_R.recyle();
            -l_3_R.readFrom(s(bArr));
            return -l_3_R;
        } catch (Exception e) {
            f.e("JceStructUtil", "getJceStruct exception: " + e);
            return null;
        }
    }

    public static byte[] d(JceStruct jceStruct) {
        JceOutputStream jceOutputStream = new JceOutputStream();
        jceOutputStream.setServerEncoding("UTF-8");
        jceStruct.writeTo(jceOutputStream);
        return jceOutputStream.toByteArray();
    }

    public static bx fR() {
        return new bx();
    }

    public static cf r(byte[] bArr) {
        JceStruct a = a(bArr, new cf(), false);
        return a != null ? (cf) a : null;
    }

    private static JceInputStream s(byte[] bArr) {
        JceInputStream jceInputStream = new JceInputStream(bArr);
        jceInputStream.setServerEncoding("UTF-8");
        return jceInputStream;
    }
}
