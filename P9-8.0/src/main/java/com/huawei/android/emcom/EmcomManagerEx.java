package com.huawei.android.emcom;

import android.emcom.EmailInfo;
import android.emcom.EmcomManager;
import android.emcom.VideoInfo;
import android.os.RemoteException;
import android.util.Log;

public class EmcomManagerEx {
    private static final String TAG = "EmcomManagerEx";
    private static EmcomManagerEx mEmcomManagerEx;

    public static synchronized EmcomManagerEx getInstance() {
        EmcomManagerEx emcomManagerEx;
        synchronized (EmcomManagerEx.class) {
            if (mEmcomManagerEx == null) {
                mEmcomManagerEx = new EmcomManagerEx();
            }
            emcomManagerEx = mEmcomManagerEx;
        }
        return emcomManagerEx;
    }

    public static void notifyEmailData(Object obj) throws RemoteException {
        if (obj instanceof EmailInfo) {
            EmailInfo eci = (EmailInfo) obj;
            Log.d(TAG, "notifyEmailData eci=" + eci);
            EmcomManager.getInstance().notifyEmailData(eci);
            return;
        }
        Log.d(TAG, "illegal EmailData");
    }

    public static void notifyVideoData(Object obj) throws RemoteException {
        if (obj instanceof VideoInfo) {
            VideoInfo vci = (VideoInfo) obj;
            Log.e(TAG, "notifyVideoData vci = " + vci);
            EmcomManager.getInstance().notifyVideoData(vci);
            return;
        }
        Log.d(TAG, "illegal VideolData");
    }

    public void responseForParaUpgrade(int paratype, int pathtype, int result) {
        EmcomManager.getInstance().responseForParaUpgrade(paratype, pathtype, result);
        Log.i(TAG, "responseForParaUpgrade: paratype = " + paratype + ", pathtype = " + pathtype + ", result = " + result);
    }
}
