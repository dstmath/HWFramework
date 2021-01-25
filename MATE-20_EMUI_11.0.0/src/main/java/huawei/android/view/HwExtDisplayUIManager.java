package huawei.android.view;

import android.graphics.Rect;
import android.os.RemoteException;
import android.widget.RemoteViews;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.util.SlogEx;
import huawei.android.content.HwContextEx;
import huawei.android.view.IHwExtDisplayUI;
import java.util.List;

public class HwExtDisplayUIManager {
    private static final String TAG = "HwExtDisplayUIManager";

    private static IHwExtDisplayUI getService() {
        return IHwExtDisplayUI.Stub.asInterface(ServiceManagerEx.getService(HwContextEx.HW_EXT_DISPLAY_SERVICE));
    }

    public static void executeSideAnimation(int type, boolean isStart) {
        try {
            IHwExtDisplayUI service = getService();
            if (service != null) {
                service.executeSideAnimation(type, isStart);
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "ExtDisplayUI binder error!");
        }
    }

    public static void addCustomViews(List<Rect> rects, List<RemoteViews> customViews) {
        try {
            IHwExtDisplayUI service = getService();
            if (service != null) {
                service.addCustomViews(rects, customViews);
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "ExtDisplayUI binder error!");
        }
    }

    public static void removeCustomViews(List<Rect> rects) {
        try {
            IHwExtDisplayUI service = getService();
            if (service != null) {
                service.removeCustomViews(rects);
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "ExtDisplayUI binder error!");
        }
    }

    public static void setTouchMapping(List<Rect> fromRects, List<Rect> toRects) {
        try {
            IHwExtDisplayUI service = getService();
            if (service != null) {
                service.setTouchMapping(fromRects, toRects);
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "ExtDisplayUI binder error!");
        }
    }
}
