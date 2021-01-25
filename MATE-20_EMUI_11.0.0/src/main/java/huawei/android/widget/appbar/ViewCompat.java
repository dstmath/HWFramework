package huawei.android.widget.appbar;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ViewCompat {
    public static final int TYPE_NON_TOUCH = 1;
    public static final int TYPE_TOUCH = 0;

    @Retention(RetentionPolicy.SOURCE)
    public @interface NestedScrollType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ScrollAxis {
    }
}
