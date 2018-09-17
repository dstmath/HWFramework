package android.support.v4.interfaces;

import android.content.Context;
import android.util.Log;
import android.widget.Scroller;

public class HwControlFactory {
    private static final String TAG = "HwControlFactory";
    private static Factory sFactory;

    public interface Factory {
        HwViewPager newHwViewPager(Context context);
    }

    public interface HwViewPager {
        boolean canScrollEdge();

        Scroller createScroller(Context context);

        float scrollEdgeBound(boolean z, float f, float f2, float f3);

        void tabScrollerFollowed(int i, float f);
    }

    public static HwViewPager getHwViewPager(Context context) {
        Factory factory = getHwFactoryImpl();
        if (factory == null) {
            return null;
        }
        return factory.newHwViewPager(context);
    }

    private static Factory getHwFactoryImpl() {
        if (sFactory != null) {
            return sFactory;
        }
        synchronized (HwControlFactory.class) {
            try {
                Class allimpl = Class.forName("huawei.support.v4.view.HwFactoryImpl");
                if (allimpl != null) {
                    sFactory = (Factory) allimpl.newInstance();
                }
            } catch (ClassNotFoundException e) {
                Log.w(TAG, ": reflection exception is " + e);
            } catch (InstantiationException e2) {
                Log.w(TAG, ": reflection exception is " + e2);
            } catch (IllegalAccessException e3) {
                Log.w(TAG, ": reflection exception is " + e3);
            }
        }
        if (sFactory == null) {
            Log.w(TAG, ": failes to get AllImpl object");
        }
        return sFactory;
    }
}
