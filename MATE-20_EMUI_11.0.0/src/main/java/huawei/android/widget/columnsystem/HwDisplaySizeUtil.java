package huawei.android.widget.columnsystem;

import android.graphics.Rect;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;

public class HwDisplaySizeUtil {
    private static final String CLASS_NAME_EXTDISPLAYSIZEUTILEX = "com.huawei.android.view.ExtDisplaySizeUtilEx";
    private static final String METHOD_NAME_GETDISPLAYSAFEINSETSSIZE = "getDisplaySafeInsets";
    private static final String TAG = "HwDisplaySizeUtil";

    private HwDisplaySizeUtil() {
    }

    public static Rect getDisplaySafeInsets() {
        try {
            Object object = Class.forName(CLASS_NAME_EXTDISPLAYSIZEUTILEX).getMethod(METHOD_NAME_GETDISPLAYSAFEINSETSSIZE, new Class[0]).invoke(null, new Object[0]);
            if (object instanceof Rect) {
                return (Rect) object;
            }
            Log.e(TAG, "getDisplaySafeInsets: object is not Rect");
            return new Rect();
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "getDisplaySafeInsets: class not found");
            return new Rect();
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "getDisplaySafeInsets: method not found");
            return new Rect();
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "getDisplaySafeInsets: illegal access exception");
            return new Rect();
        } catch (InvocationTargetException e4) {
            Log.e(TAG, "getDisplaySafeInsets: invocation target exception");
            return new Rect();
        }
    }
}
