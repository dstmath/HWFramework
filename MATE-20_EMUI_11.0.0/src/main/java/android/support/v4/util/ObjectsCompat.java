package android.support.v4.util;

import android.os.Build;
import android.support.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public class ObjectsCompat {
    private ObjectsCompat() {
    }

    public static boolean equals(@Nullable Object a, @Nullable Object b) {
        if (Build.VERSION.SDK_INT >= 19) {
            return Objects.equals(a, b);
        }
        return a == b || (a != null && a.equals(b));
    }

    public static int hashCode(@Nullable Object o) {
        if (o != null) {
            return o.hashCode();
        }
        return 0;
    }

    public static int hash(@Nullable Object... values) {
        if (Build.VERSION.SDK_INT >= 19) {
            return Objects.hash(values);
        }
        return Arrays.hashCode(values);
    }
}
