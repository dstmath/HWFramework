package com.huawei.android.server;

import android.content.Context;
import android.os.Binder;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.SystemService;
import java.lang.reflect.InvocationTargetException;

public class HwWatchConnectivityServiceBridge extends SystemService {
    private static final String IMPL_CLASS_NAME = "com.huawei.android.server.HwWatchConnectivityService";
    private static final String SERVICE_NAME = "HwWatchConnectivityService";
    private static final String TAG = "HwWatchConnectivityServiceBridge";
    private Context mContext;
    private IHwWatchConnectivityServiceEx mSystemServiceExt;

    public HwWatchConnectivityServiceBridge(Context context) {
        super(context);
        initImplClass(context);
        this.mContext = context;
    }

    public void onStart() {
        publishBinderService(SERVICE_NAME, new Binder());
        Log.i(TAG, "onStart");
        if (this.mSystemServiceExt == null) {
            initImplClass(this.mContext);
        }
        IHwWatchConnectivityServiceEx iHwWatchConnectivityServiceEx = this.mSystemServiceExt;
        if (iHwWatchConnectivityServiceEx != null) {
            iHwWatchConnectivityServiceEx.onStart();
        }
    }

    public void onBootPhase(int phase) {
        HwWatchConnectivityServiceBridge.super.onBootPhase(phase);
        Log.i(TAG, "onBootPhase " + phase);
        if (this.mSystemServiceExt == null) {
            initImplClass(this.mContext);
        }
        IHwWatchConnectivityServiceEx iHwWatchConnectivityServiceEx = this.mSystemServiceExt;
        if (iHwWatchConnectivityServiceEx != null) {
            iHwWatchConnectivityServiceEx.onBootPhase(phase);
        }
    }

    private void initImplClass(Context context) {
        Object systemServiceExt = loadClass(IMPL_CLASS_NAME, context);
        if (systemServiceExt instanceof IHwWatchConnectivityServiceEx) {
            this.mSystemServiceExt = (IHwWatchConnectivityServiceEx) systemServiceExt;
        }
        if (this.mSystemServiceExt == null) {
            Log.e(TAG, "Failed to find implement of HwWatchConnectivityService.");
        }
    }

    private static Object loadClass(String className, Context context) {
        if (TextUtils.isEmpty(className) || context == null) {
            return null;
        }
        try {
            return Class.forName(className).getConstructor(Context.class).newInstance(context);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Class Not found Exception");
            return null;
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "Illegal access exception");
            return null;
        } catch (InstantiationException e3) {
            Log.e(TAG, "Instantiation exception");
            return null;
        } catch (NoSuchMethodException e4) {
            Log.e(TAG, "NoSuchMethodException exception");
            return null;
        } catch (InvocationTargetException e5) {
            Log.e(TAG, "InvocationTargetException exception");
            return null;
        }
    }
}
