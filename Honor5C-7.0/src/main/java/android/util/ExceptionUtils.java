package android.util;

import java.io.IOException;

public class ExceptionUtils {
    private static final String PREFIX_IO = "\u2603";

    public static RuntimeException wrap(IOException e) {
        throw new IllegalStateException(PREFIX_IO + e.getMessage());
    }

    public static void maybeUnwrapIOException(RuntimeException e) throws IOException {
        if ((e instanceof IllegalStateException) && e.getMessage().startsWith(PREFIX_IO)) {
            throw new IOException(e.getMessage().substring(PREFIX_IO.length()));
        }
    }

    public static String getCompleteMessage(String msg, Throwable t) {
        StringBuilder builder = new StringBuilder();
        if (msg != null) {
            builder.append(msg).append(": ");
        }
        builder.append(t.getMessage());
        while (true) {
            t = t.getCause();
            if (t == null) {
                return builder.toString();
            }
            builder.append(": ").append(t.getMessage());
        }
    }

    public static String getCompleteMessage(Throwable t) {
        return getCompleteMessage(null, t);
    }
}
