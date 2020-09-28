package com.huawei.lighteffect;

import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.lighteffect.ILightEffectService;

public class LightEffectManager {
    private static final String LIGHT_EFFECT_SERVICE = "HwLightEffectService";
    private static final String MUSIC_LIGHT_SUPPORT = "ro.homevision.music_light";
    public static final int SWITCH_TYPE_ADAPTER = 1;
    public static final int SWITCH_TYPE_MUSIC = 0;
    private static final String TAG = "LightEffectManager";
    static IBinder iBinder = new Binder();
    private static final Object mInstanceSync = new Object();
    private static volatile LightEffectManager sSelf = null;
    private static ILightEffectService sService;

    private LightEffectManager() {
    }

    public static LightEffectManager getInstance() {
        if (sSelf == null) {
            synchronized (LightEffectManager.class) {
                if (sSelf == null) {
                    sSelf = new LightEffectManager();
                }
            }
        }
        return sSelf;
    }

    private static ILightEffectService getLightEffectService() {
        synchronized (mInstanceSync) {
            if (sService != null) {
                return sService;
            }
            sService = ILightEffectService.Stub.asInterface(ServiceManager.getService(LIGHT_EFFECT_SERVICE));
            return sService;
        }
    }

    public static void enableLightEffect(boolean enable, int switchType) {
        if (getLightEffectService() != null) {
            try {
                Log.i(TAG, "switchType is :" + switchType);
                if (switchType == 0) {
                    sService.updateMusicEffect(enable, iBinder);
                } else if (switchType == 1) {
                    sService.updateAdapterEffect(enable);
                } else {
                    Log.e(TAG, "Incorrect input parameters");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while update music state");
            }
        }
    }

    public static void enableLightSwitch(boolean enable) {
        if (getLightEffectService() != null) {
            try {
                sService.updateSettingEffect(enable);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while update setting state");
            }
        }
    }
}
