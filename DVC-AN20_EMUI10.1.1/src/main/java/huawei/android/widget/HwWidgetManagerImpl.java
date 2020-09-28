package huawei.android.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.HwWidgetManager;

public class HwWidgetManagerImpl implements HwWidgetManager {
    private static HwWidgetManager sInstance = new HwWidgetManagerImpl();

    public static HwWidgetManager getDefault() {
        return sInstance;
    }

    public LayoutInflater.Factory createWidgetFactoryHuaWei(Context context, String packageName) {
        return new WidgetFactoryHuaWei(context, packageName);
    }
}
