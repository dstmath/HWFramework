package huawei.android.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.HwWidgetManager;

public class HwWidgetManagerImpl implements HwWidgetManager {
    private static final String TAG = null;
    private static HwWidgetManager mInstance = new HwWidgetManagerImpl();

    public LayoutInflater.Factory createWidgetFactoryHuaWei(Context context, String packageName) {
        return new WidgetFactoryHuaWei(context, packageName);
    }

    public static HwWidgetManager getDefault() {
        return mInstance;
    }
}
