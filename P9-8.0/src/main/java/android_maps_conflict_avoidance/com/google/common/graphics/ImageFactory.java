package android_maps_conflict_avoidance.com.google.common.graphics;

import java.io.IOException;

public interface ImageFactory {
    GoogleImage createImage(int i, int i2);

    GoogleImage createImage(int i, int i2, boolean z);

    GoogleImage createImage(byte[] bArr, int i, int i2);

    GoogleImage createUnscaledImage(String str) throws IOException;
}
