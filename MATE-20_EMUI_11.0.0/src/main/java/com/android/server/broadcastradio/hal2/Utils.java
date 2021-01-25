package com.android.server.broadcastradio.hal2;

import android.os.RemoteException;

/* access modifiers changed from: package-private */
public class Utils {
    private static final String TAG = "BcRadio2Srv.utils";

    /* access modifiers changed from: package-private */
    public interface FuncThrowingRemoteException<T> {
        T exec() throws RemoteException;
    }

    /* access modifiers changed from: package-private */
    public interface VoidFuncThrowingRemoteException {
        void exec() throws RemoteException;
    }

    Utils() {
    }

    static FrequencyBand getBand(int freq) {
        if (freq < 30) {
            return FrequencyBand.UNKNOWN;
        }
        if (freq < 500) {
            return FrequencyBand.AM_LW;
        }
        if (freq < 1705) {
            return FrequencyBand.AM_MW;
        }
        if (freq < 30000) {
            return FrequencyBand.AM_SW;
        }
        if (freq < 60000) {
            return FrequencyBand.UNKNOWN;
        }
        if (freq < 110000) {
            return FrequencyBand.FM;
        }
        return FrequencyBand.UNKNOWN;
    }

    static <T> T maybeRethrow(FuncThrowingRemoteException<T> r) {
        try {
            return r.exec();
        } catch (RemoteException ex) {
            ex.rethrowFromSystemServer();
            return null;
        }
    }

    static void maybeRethrow(VoidFuncThrowingRemoteException r) {
        try {
            r.exec();
        } catch (RemoteException ex) {
            ex.rethrowFromSystemServer();
        }
    }
}
