package javax.microedition.khronos.opengles;

import java.nio.IntBuffer;

public interface GL10Ext extends GL {
    int glQueryMatrixxOES(IntBuffer intBuffer, IntBuffer intBuffer2);

    int glQueryMatrixxOES(int[] iArr, int i, int[] iArr2, int i2);
}
