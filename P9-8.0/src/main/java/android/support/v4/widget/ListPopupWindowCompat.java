package android.support.v4.widget;

import android.os.Build.VERSION;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ListPopupWindow;

public final class ListPopupWindowCompat {
    private ListPopupWindowCompat() {
    }

    @Deprecated
    public static OnTouchListener createDragToOpenListener(Object listPopupWindow, View src) {
        return createDragToOpenListener((ListPopupWindow) listPopupWindow, src);
    }

    public static OnTouchListener createDragToOpenListener(ListPopupWindow listPopupWindow, View src) {
        if (VERSION.SDK_INT >= 19) {
            return listPopupWindow.createDragToOpenListener(src);
        }
        return null;
    }
}
