package com.huawei.vui;

import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.lighteffect.ILightEffectService;

public class VuiManager {
    private static final String LIGHT_EFFECT_SERVICE = "HwLightEffectService";
    private static final String TAG = "VuiManager";
    public static final int VUI_STATE_ANSWERING = 4;
    public static final int VUI_STATE_COORDINATE = 7;
    public static final int VUI_STATE_FINISH = 5;
    public static final int VUI_STATE_GUI_EXIT = 6;
    public static final int VUI_STATE_LISTENING = 2;
    public static final int VUI_STATE_PROCESSING = 3;
    public static final int VUI_STATE_SLEEP = 0;
    public static final int VUI_STATE_WAKEUP = 1;
    static IBinder iBinder = new Binder();
    private static final Object mInstanceSync = new Object();
    private static volatile VuiManager sSelf = null;
    private static ILightEffectService sService;

    private VuiManager() {
    }

    public static VuiManager getInstance() {
        if (sSelf == null) {
            synchronized (VuiManager.class) {
                if (sSelf == null) {
                    sSelf = new VuiManager();
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

    public static void updateState(int state) {
        if (getLightEffectService() != null) {
            try {
                sService.updateVuiState(state, iBinder);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while update vui state");
            }
        }
    }
}
