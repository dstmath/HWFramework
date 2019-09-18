package org.ifaa.android.manager.face;

import android.content.Context;
import android.util.Log;
import org.ifaa.android.manager.face.IFAAFaceManager;

public class IFAAFaceManagerV1Impl extends IFAAFaceManagerV1 {
    private static final int DEFAULT_FLAG = 1;
    private static final int FACE_AUTH_VERSION_V1 = 1;
    private static final int IFAA_OP_FAIL = -1;
    private static final int IFAA_OP_SUCCESS = 0;
    private static final String LOG_TAG = "IFAAFaceManagerV1Impl";

    public IFAAFaceManagerV1Impl(Context context) {
        IFAAFaceRecognizeManager.createInstance(context);
    }

    public void authenticate(int reqID, int flag, IFAAFaceManager.AuthenticatorCallback callback) {
        IFAAFaceRecognizeManager frManager = IFAAFaceRecognizeManager.getInstance();
        if (frManager == null) {
            Log.e(LOG_TAG, "IFAAFaceRecognizeManager is null");
        } else if (callback == null) {
            Log.e(LOG_TAG, "callback empty");
        } else {
            frManager.setAuthCallback(callback);
            Log.i(LOG_TAG, "authenicating reqID is " + reqID + "flag is " + flag);
            int ret = frManager.init();
            if (ret != 0) {
                Log.e(LOG_TAG, "init failed returning " + ret);
                return;
            }
            IFAAFaceRecognizeManager.getFRManager().authenticate(reqID, 1, null);
        }
    }

    public int cancel(int reqID) {
        Log.i(LOG_TAG, "canceling...");
        if (IFAAFaceRecognizeManager.getInstance() == null) {
            Log.e(LOG_TAG, "IFAAFaceRecognizeManager is null");
            return -1;
        }
        IFAAFaceRecognizeManager.getFRManager().cancelAuthenticate(reqID);
        return 0;
    }

    public int getVersion() {
        return 1;
    }
}
