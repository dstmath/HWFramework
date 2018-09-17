package tmsdkobf;

import com.qq.taf.jce.JceStruct;
import java.lang.ref.WeakReference;

public class go {
    private static Object lock = new Object();
    private static go oz;

    public static go aU() {
        if (oz == null) {
            synchronized (lock) {
                if (oz == null) {
                    oz = new go();
                }
            }
        }
        return oz;
    }

    public WeakReference<kd> b(int i, int i2, int i3, long j, long j2, int i4, JceStruct jceStruct, byte[] bArr, JceStruct jceStruct2, int i5, jy jyVar, jz jzVar, long j3, long j4) {
        return gs.bc().b(i, i2, i3, j, j2, i4, jceStruct, bArr, jceStruct2, i5, jyVar, jzVar, j3, j4);
    }
}
