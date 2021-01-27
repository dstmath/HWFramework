package ohos.agp.render.opengl.adapter;

import android.opengl.ETC1;
import java.nio.Buffer;
import ohos.annotation.SystemApi;

@SystemApi
public class ETC1Adapter {
    private static final int INITIAL_OFFSET_VALUE = 0;

    public static boolean isValid(Buffer buffer) {
        return ETC1.isValid(buffer);
    }

    public static int etc1PkmGetWidth(Buffer buffer) {
        return ETC1.getWidth(buffer);
    }

    public static int etc1PkmGetHeight(Buffer buffer) {
        return ETC1.getHeight(buffer);
    }

    public static int etc1GetEncodedDataSize(int i, int i2) {
        return ETC1.getEncodedDataSize(i, i2);
    }
}
