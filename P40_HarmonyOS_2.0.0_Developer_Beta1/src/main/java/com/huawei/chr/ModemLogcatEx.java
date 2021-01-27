package com.huawei.chr;

import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.modemlogcat.V1_0.IModemlogcat;
import vendor.huawei.hardware.modemlogcat.V1_0.IMsgCallback;

public class ModemLogcatEx {
    private static final String TAG = "ModemLogcatEx";
    private IModemlogcat modemLogcat;

    private ModemLogcatEx(IModemlogcat modemLogcat2) {
        this.modemLogcat = modemLogcat2;
    }

    public static ModemLogcatEx getService() throws RemoteException, NoSuchElementException {
        return new ModemLogcatEx(IModemlogcat.getService());
    }

    public void registerMsgCallback(MsgCallbackEx callbackAtEx, MsgCallbackEx callbackAtModemEx, MsgCallbackEx callbackOmEx) throws RemoteException {
        if (this.modemLogcat == null) {
            Log.e(TAG, "registerMsgCallback: modemLogcat is null.");
            return;
        }
        IMsgCallback callbackOm = null;
        IMsgCallback callbackAt = callbackAtEx == null ? null : callbackAtEx.getMsgCallback();
        IMsgCallback callbackAtModem = callbackAtModemEx == null ? null : callbackAtModemEx.getMsgCallback();
        if (callbackOmEx != null) {
            callbackOm = callbackOmEx.getMsgCallback();
        }
        this.modemLogcat.registerMsgCallback(callbackAt, callbackAtModem, callbackOm);
    }

    public boolean linkToDeath(DeathRecipientEx deathRecipient, long cookie) throws RemoteException {
        IModemlogcat iModemlogcat = this.modemLogcat;
        if (iModemlogcat != null) {
            return iModemlogcat.linkToDeath(deathRecipient.getDeathRecipient(), cookie);
        }
        Log.e(TAG, "linkToDeath: modemLogcat is null.");
        return false;
    }

    public void receiveAtMsgFromApk(ArrayList<Byte> msg, int length) throws RemoteException {
        IModemlogcat iModemlogcat = this.modemLogcat;
        if (iModemlogcat == null) {
            Log.e(TAG, "receiveAtMsgFromApk: modemLogcat is null.");
        } else {
            iModemlogcat.receiveAtMsgFromApk(msg, length);
        }
    }

    public void receiveAtModemMsgFromApk(ArrayList<Byte> msg, int length) throws RemoteException {
        IModemlogcat iModemlogcat = this.modemLogcat;
        if (iModemlogcat == null) {
            Log.e(TAG, "receiveAtModemMsgFromApk: modemLogcat is null.");
        } else {
            iModemlogcat.receiveAtModemMsgFromApk(msg, length);
        }
    }

    public void receiveOmMsgFromApk(ArrayList<Byte> msg, int length) throws RemoteException {
        IModemlogcat iModemlogcat = this.modemLogcat;
        if (iModemlogcat == null) {
            Log.e(TAG, "receiveOmMsgFromApk: modemLogcat is null.");
        } else {
            iModemlogcat.receiveOmMsgFromApk(msg, length);
        }
    }
}
