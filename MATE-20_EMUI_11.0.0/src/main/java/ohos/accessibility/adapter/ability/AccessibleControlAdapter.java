package ohos.accessibility.adapter.ability;

import android.accessibilityservice.GestureDescription;
import android.accessibilityservice.IAccessibilityServiceConnection;
import android.content.pm.ParceledListSlice;
import android.graphics.Path;
import android.graphics.Region;
import android.os.RemoteException;
import android.view.accessibility.AccessibilityInteractionClient;
import java.util.List;
import ohos.accessibility.ability.GesturePathDefine;
import ohos.accessibility.utils.LogUtil;
import ohos.agp.utils.Rect;

public class AccessibleControlAdapter {
    private static final String TAG = "AccessibleControlAdapter";

    private AccessibleControlAdapter() {
    }

    public static void enableSoftKeyBoardCallback(int i, boolean z) {
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(i);
        if (connection != null) {
            try {
                connection.setSoftKeyboardCallbackEnabled(z);
            } catch (RemoteException unused) {
                LogUtil.error(TAG, "enableSoftKeyBoardCallback error");
            }
        }
    }

    public static int getSoftKeyBoardShowMode(int i) {
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(i);
        if (connection == null) {
            return 0;
        }
        try {
            return connection.getSoftKeyboardShowMode();
        } catch (RemoteException unused) {
            LogUtil.error(TAG, "getSoftKeyBoardShowMode error");
            return 0;
        }
    }

    public static boolean setSoftKeyBoardShowMode(int i, int i2) {
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(i);
        LogUtil.info(TAG, "setSoftKeyBoardShowMode connection:" + connection);
        if (connection == null) {
            return false;
        }
        try {
            return connection.setSoftKeyboardShowMode(i2);
        } catch (RemoteException unused) {
            LogUtil.error(TAG, "setSoftKeyBoardShowMode error");
            return false;
        }
    }

    public static float getDisplayResizeScale(int i, int i2) {
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(i);
        if (connection == null) {
            return 1.0f;
        }
        try {
            return connection.getMagnificationScale(i2);
        } catch (RemoteException unused) {
            LogUtil.error(TAG, "getDisplayResizeScale error");
            return 1.0f;
        }
    }

    public static float getDisplayResizeCenterX(int i, int i2) {
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(i);
        if (connection == null) {
            return 0.0f;
        }
        try {
            return connection.getMagnificationCenterX(i2);
        } catch (RemoteException unused) {
            LogUtil.error(TAG, "getDisplayResizeCenterX error");
            return 0.0f;
        }
    }

    public static float getDisplayResizeCenterY(int i, int i2) {
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(i);
        if (connection == null) {
            return 0.0f;
        }
        try {
            return connection.getMagnificationCenterY(i2);
        } catch (RemoteException unused) {
            LogUtil.error(TAG, "getDisplayResizeCenterY error");
            return 0.0f;
        }
    }

    public static boolean setScale(int i, int i2, float f, boolean z) {
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(i);
        if (connection == null) {
            return false;
        }
        try {
            return connection.setMagnificationScaleAndCenter(i2, f, Float.NaN, Float.NaN, z);
        } catch (RemoteException unused) {
            LogUtil.error(TAG, "setScale error");
            return false;
        }
    }

    public static boolean setCenter(int i, int i2, float f, float f2, boolean z) {
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(i);
        if (connection == null) {
            return false;
        }
        try {
            return connection.setMagnificationScaleAndCenter(i2, Float.NaN, f, f2, z);
        } catch (RemoteException unused) {
            LogUtil.error(TAG, "setCenter error");
            return false;
        }
    }

    public static Rect getDisplayResizeRect(int i, int i2) {
        Rect rect = new Rect();
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(i);
        if (connection != null) {
            try {
                Region magnificationRegion = connection.getMagnificationRegion(i2);
                if (!(magnificationRegion == null || magnificationRegion.getBounds() == null)) {
                    rect.set(magnificationRegion.getBounds().left, magnificationRegion.getBounds().top, magnificationRegion.getBounds().right, magnificationRegion.getBounds().bottom);
                }
            } catch (RemoteException unused) {
                LogUtil.error(TAG, "getDisplayResizeRect error");
            }
        }
        return rect;
    }

    public static boolean resetDisplayResizeToDefault(int i, int i2, boolean z) {
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(i);
        if (connection == null) {
            return false;
        }
        try {
            return connection.resetMagnification(i2, z);
        } catch (RemoteException unused) {
            LogUtil.error(TAG, "resetDisplayResizeToDefault error");
            return false;
        }
    }

    public static void setDisplayResizeCallbackEnabled(int i, int i2, boolean z) {
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(i);
        if (connection != null) {
            try {
                connection.setMagnificationCallbackEnabled(i2, z);
            } catch (RemoteException unused) {
                LogUtil.error(TAG, "setDisplayResizeCallbackEnabled error");
            }
        }
    }

    public static boolean dispatchGesture(int i, List<GesturePathDefine> list, int i2) {
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(i);
        if (connection == null) {
            LogUtil.info(TAG, "dispatchGesture failed,connection is null");
            return false;
        }
        try {
            connection.sendGesture(i2, new ParceledListSlice(GestureDescription.MotionEventGenerator.getGestureStepsFromGestureDescription(getGestureDescription(list), 100)));
            return true;
        } catch (RemoteException unused) {
            LogUtil.error(TAG, "sendGesture error");
            return false;
        }
    }

    private static GestureDescription getGestureDescription(List<GesturePathDefine> list) {
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(getGestureStrokes(list));
        return builder.build();
    }

    private static GestureDescription.StrokeDescription getGestureStrokes(List<GesturePathDefine> list) {
        GestureDescription.StrokeDescription strokeDescription;
        if (list == null || list.isEmpty()) {
            return null;
        }
        int size = list.size();
        if (size > 0) {
            Path path = new Path();
            path.moveTo(list.get(0).getStartPositon().getPositionX(), list.get(0).getStartPositon().getPositionY());
            path.lineTo(list.get(0).getEndPosition().getPositionX(), list.get(0).getEndPosition().getPositionY());
            strokeDescription = size == 1 ? new GestureDescription.StrokeDescription(path, 0, (long) list.get(0).getDurationTime(), false) : new GestureDescription.StrokeDescription(path, 0, (long) list.get(0).getDurationTime(), true);
        } else {
            strokeDescription = null;
        }
        for (int i = 1; i < size; i++) {
            Path path2 = new Path();
            path2.moveTo(list.get(i).getStartPositon().getPositionX(), list.get(i).getStartPositon().getPositionY());
            path2.lineTo(list.get(i).getEndPosition().getPositionX(), list.get(i).getEndPosition().getPositionY());
            if (i != size - 1) {
                strokeDescription.continueStroke(path2, (long) list.get(i - 1).getDurationTime(), (long) list.get(i).getDurationTime(), true);
            } else {
                strokeDescription.continueStroke(path2, (long) list.get(i - 1).getDurationTime(), (long) list.get(i).getDurationTime(), false);
            }
        }
        return strokeDescription;
    }
}
