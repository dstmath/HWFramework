package android.view.autofill;

import android.os.Bundle;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public final class Helper {
    public static final String REDACTED = "[REDACTED]";
    public static boolean sDebug = false;
    public static boolean sVerbose = false;

    static StringBuilder append(StringBuilder builder, Bundle bundle) {
        if (bundle == null || (sDebug ^ 1) != 0) {
            builder.append("N/A");
        } else if (sVerbose) {
            Set<String> keySet = bundle.keySet();
            builder.append("[Bundle with ").append(keySet.size()).append(" extras:");
            for (String key : keySet) {
                Object obj = bundle.get(key);
                builder.append(' ').append(key).append('=');
                if (obj instanceof Object[]) {
                    obj = Arrays.toString((Objects[]) obj);
                }
                builder.append(obj);
            }
            builder.append(']');
        } else {
            builder.append(REDACTED);
        }
        return builder;
    }

    private Helper() {
        throw new UnsupportedOperationException("contains static members only");
    }
}
