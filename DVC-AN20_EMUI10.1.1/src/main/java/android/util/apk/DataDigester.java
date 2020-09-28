package android.util.apk;

import java.nio.ByteBuffer;
import java.security.DigestException;

/* access modifiers changed from: package-private */
public interface DataDigester {
    void consume(ByteBuffer byteBuffer) throws DigestException;
}
