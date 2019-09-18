package com.huawei.wallet.sdk.common.apdu.oma;

import com.huawei.wallet.sdk.common.log.LogC;

public class NFCOpeningStatusHandler {
    private static String TAG = NFCOpeningStatusHandler.class.getSimpleName();
    private static final NFCOpeningStatusHandler instance = new NFCOpeningStatusHandler();
    private boolean isOpeningNfc = false;

    private NFCOpeningStatusHandler() {
    }

    public static NFCOpeningStatusHandler getInstance() {
        return instance;
    }

    public void checkNFCOpening() {
        if (this.isOpeningNfc) {
            synchronized (instance) {
                while (this.isOpeningNfc) {
                    try {
                        instance.wait();
                    } catch (InterruptedException e) {
                        LogC.e(TAG + " :interrupted exception", (Throwable) e, true);
                    } catch (Exception e2) {
                        LogC.e(TAG + " :exception", (Throwable) e2, true);
                    }
                }
            }
        }
    }

    public void notifyNFCOpened() {
        synchronized (instance) {
            this.isOpeningNfc = false;
            instance.notifyAll();
        }
    }

    public void setOpeningNfc(boolean isOpeningNfc2) {
        this.isOpeningNfc = isOpeningNfc2;
    }

    public boolean isOpeningNfc() {
        return this.isOpeningNfc;
    }
}
