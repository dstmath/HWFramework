package android.support.v4.widget;

import android.os.Build.VERSION;
import android.support.annotation.RequiresApi;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class PopupWindowCompat {
    static final PopupWindowCompatBaseImpl IMPL;

    static class PopupWindowCompatBaseImpl {
        private static Method sGetWindowLayoutTypeMethod;
        private static boolean sGetWindowLayoutTypeMethodAttempted;
        private static Method sSetWindowLayoutTypeMethod;
        private static boolean sSetWindowLayoutTypeMethodAttempted;

        PopupWindowCompatBaseImpl() {
        }

        public void showAsDropDown(PopupWindow popup, View anchor, int xoff, int yoff, int gravity) {
            if ((GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(anchor)) & 7) == 5) {
                xoff -= popup.getWidth() - anchor.getWidth();
            }
            popup.showAsDropDown(anchor, xoff, yoff);
        }

        public void setOverlapAnchor(PopupWindow popupWindow, boolean overlapAnchor) {
        }

        public boolean getOverlapAnchor(PopupWindow popupWindow) {
            return false;
        }

        public void setWindowLayoutType(PopupWindow popupWindow, int layoutType) {
            if (!sSetWindowLayoutTypeMethodAttempted) {
                try {
                    sSetWindowLayoutTypeMethod = PopupWindow.class.getDeclaredMethod("setWindowLayoutType", new Class[]{Integer.TYPE});
                    sSetWindowLayoutTypeMethod.setAccessible(true);
                } catch (Exception e) {
                }
                sSetWindowLayoutTypeMethodAttempted = true;
            }
            if (sSetWindowLayoutTypeMethod != null) {
                try {
                    sSetWindowLayoutTypeMethod.invoke(popupWindow, new Object[]{Integer.valueOf(layoutType)});
                } catch (Exception e2) {
                }
            }
        }

        public int getWindowLayoutType(PopupWindow popupWindow) {
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

    @RequiresApi(19)
    static class PopupWindowCompatApi19Impl extends PopupWindowCompatBaseImpl {
        PopupWindowCompatApi19Impl() {
        }

        public void showAsDropDown(PopupWindow popup, View anchor, int xoff, int yoff, int gravity) {
            popup.showAsDropDown(anchor, xoff, yoff, gravity);
        }
    }

    @RequiresApi(21)
    static class PopupWindowCompatApi21Impl extends PopupWindowCompatApi19Impl {
        private static final String TAG = "PopupWindowCompatApi21";
        private static Field sOverlapAnchorField;

        PopupWindowCompatApi21Impl() {
        }

        static {
            try {
                sOverlapAnchorField = PopupWindow.class.getDeclaredField("mOverlapAnchor");
                sOverlapAnchorField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.i(TAG, "Could not fetch mOverlapAnchor field from PopupWindow", e);
            }
        }

        public void setOverlapAnchor(PopupWindow popupWindow, boolean overlapAnchor) {
            if (sOverlapAnchorField != null) {
                try {
                    sOverlapAnchorField.set(popupWindow, Boolean.valueOf(overlapAnchor));
                } catch (IllegalAccessException e) {
                    Log.i(TAG, "Could not set overlap anchor field in PopupWindow", e);
                }
            }
        }

        public boolean getOverlapAnchor(PopupWindow popupWindow) {
            if (sOverlapAnchorField != null) {
                try {
                    return ((Boolean) sOverlapAnchorField.get(popupWindow)).booleanValue();
                } catch (IllegalAccessException e) {
                    Log.i(TAG, "Could not get overlap anchor field in PopupWindow", e);
                }
            }
            return false;
        }
    }

    @RequiresApi(23)
    static class PopupWindowCompatApi23Impl extends PopupWindowCompatApi21Impl {
        PopupWindowCompatApi23Impl() {
        }

        public void setOverlapAnchor(PopupWindow popupWindow, boolean overlapAnchor) {
            popupWindow.setOverlapAnchor(overlapAnchor);
        }

        public boolean getOverlapAnchor(PopupWindow popupWindow) {
            return popupWindow.getOverlapAnchor();
        }

        public void setWindowLayoutType(PopupWindow popupWindow, int layoutType) {
            popupWindow.setWindowLayoutType(layoutType);
        }

        public int getWindowLayoutType(PopupWindow popupWindow) {
            return popupWindow.getWindowLayoutType();
        }
    }

    static {
        if (VERSION.SDK_INT >= 23) {
            IMPL = new PopupWindowCompatApi23Impl();
        } else if (VERSION.SDK_INT >= 21) {
            IMPL = new PopupWindowCompatApi21Impl();
        } else if (VERSION.SDK_INT >= 19) {
            IMPL = new PopupWindowCompatApi19Impl();
        } else {
            IMPL = new PopupWindowCompatBaseImpl();
        }
    }

    private PopupWindowCompat() {
    }

    public static void showAsDropDown(PopupWindow popup, View anchor, int xoff, int yoff, int gravity) {
        IMPL.showAsDropDown(popup, anchor, xoff, yoff, gravity);
    }

    public static void setOverlapAnchor(PopupWindow popupWindow, boolean overlapAnchor) {
        IMPL.setOverlapAnchor(popupWindow, overlapAnchor);
    }

    public static boolean getOverlapAnchor(PopupWindow popupWindow) {
        return IMPL.getOverlapAnchor(popupWindow);
    }

    public static void setWindowLayoutType(PopupWindow popupWindow, int layoutType) {
        IMPL.setWindowLayoutType(popupWindow, layoutType);
    }

    public static int getWindowLayoutType(PopupWindow popupWindow) {
        return IMPL.getWindowLayoutType(popupWindow);
    }
}
