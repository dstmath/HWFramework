package ohos.agp.render.opengl;

import java.nio.Buffer;
import ohos.agp.render.opengl.adapter.ETC1Adapter;

public class ETC1 {
    public static boolean isValid(Buffer buffer) {
        return ETC1Adapter.isValid(buffer);
    }

    public static int etc1PkmGetWidth(Buffer buffer) {
        return ETC1Adapter.etc1PkmGetWidth(buffer);
    }

    public static int etc1PkmGetHeight(Buffer buffer) {
        return ETC1Adapter.etc1PkmGetHeight(buffer);
    }

    public static int etc1GetEncodedDataSize(int i, int i2) {
        return ETC1Adapter.etc1GetEncodedDataSize(i, i2);
    }
}
