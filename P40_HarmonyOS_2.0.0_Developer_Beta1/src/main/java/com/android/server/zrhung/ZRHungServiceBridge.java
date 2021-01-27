package com.android.server.zrhung;

import android.content.Context;
import android.util.Log;
import com.android.server.SystemService;
import java.lang.reflect.InvocationTargetException;

public class ZRHungServiceBridge extends SystemService {
    private static final String TAG = "ZRHungServiceBridge";
    private Context context;
    private IZRHungService zrHungServiceEx;

    public ZRHungServiceBridge(Context context2) {
        super(context2);
        this.context = context2;
        init();
    }

    private void init() {
        this.zrHungServiceEx = (IZRHungService) loadClass("com.android.server.zrhung.ZRHungService", this.context);
        if (this.zrHungServiceEx == null) {
            Log.i(TAG, "ZRHungService is null");
            this.zrHungServiceEx = (IZRHungService) loadClass("com.android.commgmt.zrhung.DefaultZrHungService", this.context);
        }
    }

    public void onStart() {
        Log.d(TAG, "ZRHungServiceBridge on start");
        if (this.zrHungServiceEx != null) {
            Log.i(TAG, "ZRHungServiceEx not null");
            this.zrHungServiceEx.onStart();
        }
    }

    public void onBootPhase(int phase) {
        Log.d(TAG, "ZRHungServiceBridge on bootphase");
        if (this.zrHungServiceEx != null) {
            Log.i(TAG, "ZRHungServiceEx not null");
            this.zrHungServiceEx.onBootPhase(phase);
        }
    }

    private static Object loadClass(String className, Context context2) {
        if (className == null) {
            return null;
        }
        try {
            return Class.forName(className).getConstructor(Context.class).newInstance(context2);
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
