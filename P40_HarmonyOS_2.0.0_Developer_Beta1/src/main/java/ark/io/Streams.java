package ark.io;

import java.io.IOException;
import java.io.InputStream;

public final class Streams {
    public static byte[] readFullyNoClose(InputStream inputStream) throws IOException {
        return libcore.io.Streams.readFullyNoClose(inputStream);
    }
}
