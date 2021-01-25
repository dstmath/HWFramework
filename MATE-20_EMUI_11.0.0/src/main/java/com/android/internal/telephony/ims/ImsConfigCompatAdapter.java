package com.android.internal.telephony.ims;

import android.os.RemoteException;
import android.telephony.ims.stub.ImsConfigImplBase;
import android.util.Log;
import com.android.ims.internal.IImsConfig;

public class ImsConfigCompatAdapter extends ImsConfigImplBase {
    public static final int FAILED = 1;
    public static final int SUCCESS = 0;
    private static final String TAG = "ImsConfigCompatAdapter";
    public static final int UNKNOWN = -1;
    private final IImsConfig mOldConfigInterface;

    public ImsConfigCompatAdapter(IImsConfig config) {
        this.mOldConfigInterface = config;
    }

    public int setConfig(int item, int value) {
        try {
            if (this.mOldConfigInterface.setProvisionedValue(item, value) == 0) {
                return 0;
            }
            return 1;
        } catch (RemoteException e) {
            Log.w(TAG, "setConfig: item=" + item + " value=" + value + "failed: " + e.getMessage());
            return 1;
        }
    }

    public int setConfig(int item, String value) {
        try {
            if (this.mOldConfigInterface.setProvisionedStringValue(item, value) == 0) {
                return 0;
            }
            return 1;
        } catch (RemoteException e) {
            Log.w(TAG, "setConfig: item=" + item + " value=" + value + "failed: " + e.getMessage());
            return 1;
        }
    }

    public int getConfigInt(int item) {
        try {
            int value = this.mOldConfigInterface.getProvisionedValue(item);
            if (value != -1) {
                return value;
            }
            return -1;
        } catch (RemoteException e) {
            Log.w(TAG, "getConfigInt: item=" + item + "failed: " + e.getMessage());
        }
    }

    public String getConfigString(int item) {
        try {
            return this.mOldConfigInterface.getProvisionedStringValue(item);
        } catch (RemoteException e) {
            Log.w(TAG, "getConfigInt: item=" + item + "failed: " + e.getMessage());
            return null;
        }
    }
}
