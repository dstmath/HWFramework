package android.util.apk;

import java.nio.ByteBuffer;
import java.security.DigestException;

interface DataDigester {
    void consume(ByteBuffer byteBuffer) throws DigestException;
}
