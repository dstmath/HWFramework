package com.huawei.vmock.fpSendCmd2Hal;

import android.os.RemoteException;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.biometrics.fingerprint.V2_2.IExtBiometricsFingerprint;

public class fpSendCmd2Hal {
    public static void main(String[] args) {
        IExtBiometricsFingerprint mDaemonEx = null;
        if (args.length < 1) {
            System.err.println("fpSendCmd2Hal need parameter.");
            return;
        }
        try {
            mDaemonEx = IExtBiometricsFingerprint.getService();
        } catch (NoSuchElementException e) {
            System.err.println("fpSendCmd2Hal get service fail - 1.");
        } catch (RemoteException e2) {
            System.err.println("fpSendCmd2Hal get service fail - 2.");
        }
        if (mDaemonEx != null) {
            try {
                if (mDaemonEx.sendCmdToHal(Integer.parseInt(args[0])) < 0) {
                    System.err.println("fpSendCmd2Hal sendCmdToHal fail - 1.");
                }
            } catch (RemoteException e3) {
                System.err.println("fpSendCmd2Hal sendCmdToHal fail - 2.");
            }
        } else {
            System.err.println("fpSendCmd2Hal get service fail - 3.");
        }
    }
}
