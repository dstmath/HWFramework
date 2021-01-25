package android.support.v4.widget;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.PopupMenu;

public final class PopupMenuCompat {
    private PopupMenuCompat() {
    }

    @Nullable
    public static View.OnTouchListener getDragToOpenListener(@NonNull Object popupMenu) {
        if (Build.VERSION.SDK_INT >= 19) {
            return ((PopupMenu) popupMenu).getDragToOpenListener();
        }
        return null;
    }
}
