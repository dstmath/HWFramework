package com.huawei.ims;

import android.content.Context;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.huawei.annotation.HwSystemApi;
import java.util.HashMap;

@HwSystemApi
public class ImsManagerExt {
    private static final HashMap<Integer, ImsManagerExt> sImsManagerExtInstances = new HashMap<>();
    private ImsManager mImsManager;

    public void setImsManager(ImsManager imsManager) {
        this.mImsManager = imsManager;
    }

    public static ImsManagerExt getInstance(Context context, int subId) {
        synchronized (sImsManagerExtInstances) {
            if (sImsManagerExtInstances.containsKey(Integer.valueOf(subId))) {
                return sImsManagerExtInstances.get(Integer.valueOf(subId));
            }
            ImsManager mgr = ImsManager.getInstance(context, subId);
            ImsManagerExt mgrEx = new ImsManagerExt();
            mgrEx.setImsManager(mgr);
            sImsManagerExtInstances.put(Integer.valueOf(subId), mgrEx);
            return mgrEx;
        }
    }

    public static boolean isNonTtyOrTtyOnVolteEnabled(Context context) {
        return ImsManager.isNonTtyOrTtyOnVolteEnabled(context);
    }

    public static boolean isVolteEnabledByPlatform(Context context) {
        return ImsManager.isVolteEnabledByPlatform(context);
    }

    public static boolean isVtEnabledByPlatform(Context context) {
        return ImsManager.isVtEnabledByPlatform(context);
    }

    public static boolean isVtEnabledByUser(Context context) {
        return ImsManager.isVtEnabledByUser(context);
    }

    public static boolean isWfcEnabledByUser(Context context) {
        return ImsManager.isWfcEnabledByUser(context);
    }

    public boolean isWfcEnabledByUser() {
        return this.mImsManager.isWfcEnabledByUser();
    }

    public static int getWfcMode(Context context) {
        return ImsManager.getWfcMode(context);
    }

    public static int getWfcMode(Context context, boolean roaming) {
        return ImsManager.getWfcMode(context, roaming);
    }

    public int getWfcMode(boolean roaming) {
        return this.mImsManager.getWfcMode(roaming);
    }

    public int getWfcMode() {
        return this.mImsManager.getWfcMode();
    }

    public static void setWfcSetting(Context context, boolean enabled) {
        ImsManager.setWfcSetting(context, enabled);
    }

    public static void setWfcMode(Context context, int wfcMode) {
        ImsManager.setWfcMode(context, wfcMode);
    }

    public void setWfcMode(int wfcMode) {
        this.mImsManager.setWfcMode(wfcMode);
    }

    public static void setWfcMode(Context context, int wfcMode, boolean roaming) {
        ImsManager.setWfcMode(context, wfcMode, roaming);
    }

    public void setWfcMode(int wfcMode, boolean roaming) {
        this.mImsManager.setWfcMode(wfcMode, roaming);
    }

    public ImsConfigEx getConfigInterface() throws ImsExceptionEx {
        try {
            ImsConfigEx imsConfigEx = new ImsConfigEx();
            imsConfigEx.setImsConfig(this.mImsManager.getConfigInterface());
            return imsConfigEx;
        } catch (ImsException e) {
            throw getImsExceptionEx(e);
        }
    }

    public static boolean isWfcRoamingEnabledByUser(Context context) {
        return ImsManager.isWfcRoamingEnabledByUser(context);
    }

    public static void setWfcRoamingSetting(Context context, boolean enabled) {
        ImsManager.setWfcRoamingSetting(context, enabled);
    }

    public static boolean isWfcEnabledByPlatform(Context context) {
        return ImsManager.isWfcEnabledByPlatform(context);
    }

    public static void updateImsServiceConfig(Context context, int phoneId, boolean force) {
        ImsManager.updateImsServiceConfig(context, phoneId, force);
    }

    public void changeMmTelCapability(int capability, int radioTech, boolean isEnabled) throws ImsExceptionEx {
        try {
            this.mImsManager.changeMmTelCapability(capability, radioTech, isEnabled);
        } catch (ImsException e) {
            throw getImsExceptionEx(e);
        }
    }

    public boolean isImsServiceProxyNull() {
        return this.mImsManager.getImsServiceProxy() == null;
    }

    public boolean isImsServiceProxyBinderAlive() {
        if (isImsServiceProxyNull()) {
            return false;
        }
        return this.mImsManager.getImsServiceProxy().isBinderAlive();
    }

    public void createImsServiceProxy(MmTelFeatureConnectionEx imsServiceProxy) {
        this.mImsManager.createImsServiceProxy(imsServiceProxy.getMmTelFeatureConnection());
    }

    public void setEnhanced4gLteModeSetting(boolean enabled) {
        this.mImsManager.setEnhanced4gLteModeSetting(enabled);
    }

    private ImsExceptionEx getImsExceptionEx(ImsException e) {
        if (e.getCause() == null) {
            return new ImsExceptionEx(e.getMessage(), e.getCode());
        }
        return new ImsExceptionEx(e.getMessage(), e.getCause(), e.getCode());
    }
}
