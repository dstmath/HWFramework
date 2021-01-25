package android.support.v4.widget;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class PopupWindowCompat {
    private static final String TAG = "PopupWindowCompatApi21";
    private static Method sGetWindowLayoutTypeMethod;
    private static boolean sGetWindowLayoutTypeMethodAttempted;
    private static Field sOverlapAnchorField;
    private static boolean sOverlapAnchorFieldAttempted;
    private static Method sSetWindowLayoutTypeMethod;
    private static boolean sSetWindowLayoutTypeMethodAttempted;

    private PopupWindowCompat() {
    }

    public static void showAsDropDown(@NonNull PopupWindow popup, @NonNull View anchor, int xoff, int yoff, int gravity) {
        if (Build.VERSION.SDK_INT >= 19) {
            popup.showAsDropDown(anchor, xoff, yoff, gravity);
            return;
        }
        int xoff1 = xoff;
        if ((GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(anchor)) & 7) == 5) {
            xoff1 -= popup.getWidth() - anchor.getWidth();
        }
        popup.showAsDropDown(anchor, xoff1, yoff);
    }

    public static void setOverlapAnchor(@NonNull PopupWindow popupWindow, boolean overlapAnchor) {
        if (Build.VERSION.SDK_INT >= 23) {
            popupWindow.setOverlapAnchor(overlapAnchor);
        } else if (Build.VERSION.SDK_INT >= 21) {
            if (!sOverlapAnchorFieldAttempted) {
                try {
                    sOverlapAnchorField = PopupWindow.class.getDeclaredField("mOverlapAnchor");
                    sOverlapAnchorField.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    Log.i(TAG, "Could not fetch mOverlapAnchor field from PopupWindow", e);
                }
                sOverlapAnchorFieldAttempted = true;
            }
            if (sOverlapAnchorField != null) {
                try {
                    sOverlapAnchorField.set(popupWindow, Boolean.valueOf(overlapAnchor));
                } catch (IllegalAccessException e2) {
                    Log.i(TAG, "Could not set overlap anchor field in PopupWindow", e2);
                }
            }
        }
    }

    public static boolean getOverlapAnchor(@NonNull PopupWindow popupWindow) {
        if (Build.VERSION.SDK_INT >= 23) {
            return popupWindow.getOverlapAnchor();
        }
        if (Build.VERSION.SDK_INT < 21) {
            return false;
        }
        if (!sOverlapAnchorFieldAttempted) {
            try {
                sOverlapAnchorField = PopupWindow.class.getDeclaredField("mOverlapAnchor");
                sOverlapAnchorField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.i(TAG, "Could not fetch mOverlapAnchor field from PopupWindow", e);
            }
            sOverlapAnchorFieldAttempted = true;
        }
        if (sOverlapAnchorField == null) {
            return false;
        }
        try {
            return ((Boolean) sOverlapAnchorField.get(popupWindow)).booleanValue();
        } catch (IllegalAccessException e2) {
            Log.i(TAG, "Could not get overlap anchor field in PopupWindow", e2);
            return false;
        }
    }

    public static void setWindowLayoutType(@NonNull PopupWindow popupWindow, int layoutType) {
        if (Build.VERSION.SDK_INT >= 23) {
            popupWindow.setWindowLayoutType(layoutType);
            return;
        }
        if (!sSetWindowLayoutTypeMethodAttempted) {
            try {
                sSetWindowLayoutTypeMethod = PopupWindow.class.getDeclaredMethod("setWindowLayoutType", Integer.TYPE);
                sSetWindowLayoutTypeMethod.setAccessible(true);
            } catch (Exception e) {
            }
            sSetWindowLayoutTypeMethodAttempted = true;
        }
        if (sSetWindowLayoutTypeMethod != null) {
            try {
                sSetWindowLayoutTypeMethod.invoke(popupWindow, Integer.valueOf(layoutType));
            } catch (Exception e2) {
            }
        }
    }

    public static int getWindowLayoutType(@NonNull PopupWindow popupWindow) {
        if (Build.VERSION.SDK_INT >= 23) {
            return popupWindow.getWindowLayoutType();
        }
        if (!sGetWindowLayoutTypeMethodAttempted) {
            try {
                sGetWindowLayoutTypeMethod = PopupWindow.class.getDeclaredMethod("getWindowLayoutType", new Class[0]);
                sGetWindowLayoutTypeMethod.setAccessible(true);
            } catch (Exception e) {
            }
            sGetWindowLayoutTypeMethodAttempted = true;
        }
        if (sGetWindowLayoutTypeMethod != null) {
            try {
                return ((Integer) sGetWindowLayoutTypeMethod.invoke(popupWindow, new Object[0])).intValue();
            } catch (Exception e2) {
            }
        }
        return 0;
    }
}
