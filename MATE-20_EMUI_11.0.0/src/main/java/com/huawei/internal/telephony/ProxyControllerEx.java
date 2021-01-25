package com.huawei.internal.telephony;

import android.telephony.RadioAccessFamily;
import com.android.internal.telephony.ProxyController;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ProxyControllerEx {
    public static final String MODEM_0 = "0";
    private static ProxyController sProxyController;
    private static ProxyControllerEx sProxyControllerEx;

    private ProxyControllerEx() {
        sProxyController = ProxyController.getInstance();
    }

    public static synchronized ProxyControllerEx getInstance() {
        ProxyControllerEx proxyControllerEx;
        synchronized (ProxyControllerEx.class) {
            if (sProxyControllerEx == null) {
                sProxyControllerEx = new ProxyControllerEx();
            }
            proxyControllerEx = sProxyControllerEx;
        }
        return proxyControllerEx;
    }

    public boolean setRadioCapability(int expectedMainSlotId, int cdmaSimSlotId) {
        ProxyController proxyController = sProxyController;
        if (proxyController != null) {
            return proxyController.setRadioCapability(expectedMainSlotId, cdmaSimSlotId);
        }
        return false;
    }

    public boolean setRadioCapability(RadioAccessFamilyEx[] rafs) {
        if (sProxyController == null) {
            return false;
        }
        RadioAccessFamily[] radioAccessFamilies = new RadioAccessFamily[rafs.length];
        for (int i = 0; i < radioAccessFamilies.length; i++) {
            radioAccessFamilies[i] = rafs[i].getRadioAccessFamily();
        }
        return sProxyController.setRadioCapability(radioAccessFamilies);
    }

    public void syncRadioCapability(int mainStackPhoneId) {
        ProxyController proxyController = sProxyController;
        if (proxyController != null) {
            proxyController.syncRadioCapability(mainStackPhoneId);
        }
    }

    public void retrySetRadioCapabilities() {
        ProxyController proxyController = sProxyController;
        if (proxyController != null) {
            proxyController.retrySetRadioCapabilities();
        }
    }

    public int getMaxRafSupported() {
        ProxyController proxyController = sProxyController;
        if (proxyController != null) {
            return proxyController.getMaxRafSupported();
        }
        return 0;
    }

    public int getMinRafSupported() {
        ProxyController proxyController = sProxyController;
        if (proxyController != null) {
            return proxyController.getMinRafSupported();
        }
        return 0;
    }
}
