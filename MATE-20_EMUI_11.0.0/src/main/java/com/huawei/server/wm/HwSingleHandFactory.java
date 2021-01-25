package com.huawei.server.wm;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.android.server.wm.WindowManagerServiceEx;

public class HwSingleHandFactory {
    public static final String SINGLE_HAND_FACTORY_IMPL_NAME = "com.huawei.server.wm.HwSingleHandFactoryImpl";
    private static final String TAG = "HwSingleHandFactory";
    private static HwSingleHandFactory mFactory;

    public static HwSingleHandFactory loadFactory(String factoryName) {
        HwSingleHandFactory hwSingleHandFactory = mFactory;
        if (hwSingleHandFactory != null) {
            return hwSingleHandFactory;
        }
        Object object = loadFactoryImpl(factoryName);
        Log.i(TAG, "factoryName：" + factoryName);
        if (object == null || !(object instanceof HwSingleHandFactory)) {
            mFactory = new HwSingleHandFactory();
        } else {
            mFactory = (HwSingleHandFactory) object;
        }
        if (mFactory != null) {
            Log.i(TAG, "add " + factoryName + " to memory.");
            return mFactory;
        }
        throw new RuntimeException("can't load any basic platform factory");
    }

    private static Object loadFactoryImpl(String factoryName) {
        if (factoryName == null) {
            return null;
        }
        try {
            return Class.forName(factoryName).newInstance();
        } catch (ClassNotFoundException e) {
            Log.i(TAG, "loadFactory() ClassNotFoundException !");
            return null;
        } catch (InstantiationException e2) {
            Log.e(TAG, "loadFactory() InstantiationException !");
            return null;
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "loadFactory() IllegalAccessException !");
            return null;
        } catch (Exception e4) {
            Log.e(TAG, "loadFactory() Exception !");
            return null;
        }
    }

    public IHwSingleHandAdapter getHwSingleHandAdapter(Context context, Handler handler, Handler uiHandler, WindowManagerServiceEx service) {
        return DefaultHwSingleHandAdapter.getDefault();
    }

    public HwSingleHandContentExBridgeEx getHwSingleHandContentEx(WindowManagerServiceEx serviceEx) {
        return new HwSingleHandContentExBridgeEx(serviceEx);
    }
}
